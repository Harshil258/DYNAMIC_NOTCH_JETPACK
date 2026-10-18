package ai.emots.kishan_dynamic.data.analytics

import ai.emots.kishan_dynamic.data.premium.EntitlementSnapshot
import ai.emots.kishan_dynamic.data.premium.EntitlementSource
import ai.emots.kishan_dynamic.data.premium.FormattedPlanDetails
import com.android.billingclient.api.Purchase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Sprint 11 verification suite:
 * Tests the complete GA4 typed event catalog, schema validation, zero-PII contract,
 * user property syncing, screen deduplication, idempotent purchase conversions,
 * ILAR ad revenue reporting, and billing crashlytics rate limiting.
 */
class Sprint11AnalyticsAndCrashlyticsTest {

    private lateinit var tracker: FakeAnalyticsTracker
    private lateinit var deduplicator: ScreenDeduplicator
    private lateinit var conversionTracker: SubscriptionConversionTracker
    private lateinit var revenueTracker: AdMobRevenueTracker
    private lateinit var billingReporter: BillingCrashlyticsReporter

    @Before
    fun setUp() {
        tracker = FakeAnalyticsTracker(throwOnValidationError = false)
        deduplicator = ScreenDeduplicator(tracker)
        conversionTracker = SubscriptionConversionTracker(tracker)
        revenueTracker = AdMobRevenueTracker(tracker)
        billingReporter = BillingCrashlyticsReporter(
            analyticsTracker = tracker,
            appVersion = "1.0.0",
            billingLibraryVersion = "8.3.0",
            minCooldownMillis = 60_000L
        )
    }

    // =========================================================================
    // 1. Schema Validation & Zero-PII Contract (AUR-P2-028)
    // =========================================================================

    @Test
    fun testValidEventParametersPassValidation() {
        val rawParams = mapOf(
            AnalyticsParamNames.PLAN_ID to "pro_yearly",
            AnalyticsParamNames.VALUE to 599.0,
            AnalyticsParamNames.CURRENCY to "INR",
            "is_valid" to true
        )

        val sanitized = AnalyticsSchemaValidator.validateAndSanitize(
            eventName = AnalyticsEventNames.PURCHASE,
            parameters = rawParams,
            throwOnError = true
        )

        assertEquals(4, sanitized.size)
        assertEquals("pro_yearly", sanitized[AnalyticsParamNames.PLAN_ID])
        assertEquals(599.0, sanitized[AnalyticsParamNames.VALUE])
        assertEquals("INR", sanitized[AnalyticsParamNames.CURRENCY])
        assertEquals(true, sanitized["is_valid"])
    }

    @Test
    fun testZeroPiiSanitizerDropsDisallowedSensitiveKeys() {
        val piiParams = mapOf(
            "notification_title" to "Incoming message from Mom",
            "message_body" to "OTP is 482910",
            "sender_contact" to "John Doe",
            "user_phone" to "+919876543210",
            "bank_balance" to "₹54,000",
            "allowed_metric" to "count_5"
        )

        // Production mode (throwOnError = false): Disallowed keys dropped silently
        val sanitized = AnalyticsSchemaValidator.validateAndSanitize(
            eventName = "test_event",
            parameters = piiParams,
            throwOnError = false
        )

        assertEquals(1, sanitized.size)
        assertEquals("count_5", sanitized["allowed_metric"])
        assertFalse(sanitized.containsKey("notification_title"))
        assertFalse(sanitized.containsKey("message_body"))
        assertFalse(sanitized.containsKey("sender_contact"))
        assertFalse(sanitized.containsKey("user_phone"))
        assertFalse(sanitized.containsKey("bank_balance"))
    }

    @Test
    fun testZeroPiiSanitizerThrowsInStrictTestMode() {
        val piiParams = mapOf(
            "user_email" to "surya@example.com"
        )

        assertThrows(IllegalArgumentException::class.java) {
            AnalyticsSchemaValidator.validateAndSanitize(
                eventName = "test_event",
                parameters = piiParams,
                throwOnError = true
            )
        }
    }

    @Test
    fun testZeroPiiWhitelistedExceptionsPermitted() {
        val whitelistedParams = mapOf(
            AnalyticsParamNames.STEP_NAME to "Permissions Step",
            AnalyticsParamNames.ITEM_NAME to "Pro Lifetime",
            AnalyticsParamNames.SCREEN_NAME to "settings_display"
        )

        val sanitized = AnalyticsSchemaValidator.validateAndSanitize(
            eventName = "step_view",
            parameters = whitelistedParams,
            throwOnError = true
        )

        assertEquals(3, sanitized.size)
        assertEquals("Permissions Step", sanitized[AnalyticsParamNames.STEP_NAME])
        assertEquals("Pro Lifetime", sanitized[AnalyticsParamNames.ITEM_NAME])
        assertEquals("settings_display", sanitized[AnalyticsParamNames.SCREEN_NAME])
    }

    @Test
    fun testTypedAnalyticsEventHierarchyLogging() {
        tracker.logEvent(AnalyticsEvent.OnboardingStarted)
        tracker.logEvent(AnalyticsEvent.OnboardingStepViewed(stepIndex = 1, stepName = "alerts"))
        tracker.logEvent(AnalyticsEvent.PermissionResult(permissionType = "accessibility", action = "grant", granted = true))
        tracker.logEvent(AnalyticsEvent.IslandActivated)
        tracker.logEvent(AnalyticsEvent.ThemeSelected(theme = "Obsidian Aurora"))
        tracker.logEvent(AnalyticsEvent.SettingChanged(settingName = "vertical_offset", value = 14))

        assertTrue(tracker.hasEvent(AnalyticsEventNames.ONBOARDING_STARTED))
        assertTrue(tracker.hasEvent(AnalyticsEventNames.ONBOARDING_STEP_VIEWED))
        assertTrue(tracker.hasEvent(AnalyticsEventNames.PERMISSION_RESULT))
        assertTrue(tracker.hasEvent(AnalyticsEventNames.ISLAND_ACTIVATED))
        assertTrue(tracker.hasEvent(AnalyticsEventNames.THEME_SELECTED))
        assertTrue(tracker.hasEvent("setting_changed"))

        val settingEvent = tracker.getEvents("setting_changed").first()
        assertEquals("vertical_offset", settingEvent.parameters["setting_name"])
        assertEquals("14", settingEvent.parameters["setting_value"])
    }

    // =========================================================================
    // 2. User Properties Synchronization (AUR-P2-029)
    // =========================================================================

    @Test
    fun testUserPropertiesForFreeUser() {
        val freeSnapshot = EntitlementSnapshot(
            source = EntitlementSource.FREE,
            isPro = false,
            remainingDays = 0
        )

        val userPropertyManager = AnalyticsUserPropertyManager(
            analyticsTracker = tracker,
            installCohortOverride = "2026_10"
        )

        userPropertyManager.applyUserProperties(
            snapshot = freeSnapshot,
            installCohort = "2026_10",
            themeMode = "dark",
            languageCode = "en"
        )

        assertEquals("free", tracker.userProperties[AnalyticsUserProperties.ENTITLEMENT_TIER])
        assertEquals("false", tracker.userProperties[AnalyticsUserProperties.IS_PRO])
        assertEquals("none", tracker.userProperties[AnalyticsUserProperties.TRIAL_DAY])
        assertEquals("2026_10", tracker.userProperties[AnalyticsUserProperties.INSTALL_COHORT])
        assertEquals("dark", tracker.userProperties[AnalyticsUserProperties.APP_THEME])
        assertEquals("en", tracker.userProperties[AnalyticsUserProperties.APP_LANGUAGE])
    }

    @Test
    fun testUserPropertiesForInstallTrialUser() {
        val trialSnapshot = EntitlementSnapshot(
            source = EntitlementSource.INSTALL_TRIAL,
            isPro = true,
            isTrial = true,
            remainingDays = 5 // Day 3 of 7 (8 - 5 = 3)
        )

        val userPropertyManager = AnalyticsUserPropertyManager(
            analyticsTracker = tracker,
            installCohortOverride = "2026_10"
        )

        userPropertyManager.applyUserProperties(
            snapshot = trialSnapshot,
            installCohort = "2026_10",
            themeMode = "system",
            languageCode = "hi"
        )

        assertEquals("install_trial", tracker.userProperties[AnalyticsUserProperties.ENTITLEMENT_TIER])
        assertEquals("true", tracker.userProperties[AnalyticsUserProperties.IS_PRO])
        assertEquals("day_3", tracker.userProperties[AnalyticsUserProperties.TRIAL_DAY])
        assertEquals("system", tracker.userProperties[AnalyticsUserProperties.APP_THEME])
        assertEquals("hi", tracker.userProperties[AnalyticsUserProperties.APP_LANGUAGE])
    }

    @Test
    fun testUserPropertiesForPaidSubscriber() {
        val yearlySnapshot = EntitlementSnapshot(
            source = EntitlementSource.YEARLY,
            isPro = true,
            isTrial = false,
            remainingDays = 365
        )

        val userPropertyManager = AnalyticsUserPropertyManager(
            analyticsTracker = tracker,
            installCohortOverride = "2026_09"
        )

        userPropertyManager.applyUserProperties(
            snapshot = yearlySnapshot,
            installCohort = "2026_09",
            themeMode = "light",
            languageCode = "en"
        )

        assertEquals("yearly", tracker.userProperties[AnalyticsUserProperties.ENTITLEMENT_TIER])
        assertEquals("true", tracker.userProperties[AnalyticsUserProperties.IS_PRO])
        assertEquals("none", tracker.userProperties[AnalyticsUserProperties.TRIAL_DAY])
        assertEquals("2026_09", tracker.userProperties[AnalyticsUserProperties.INSTALL_COHORT])
        assertEquals("light", tracker.userProperties[AnalyticsUserProperties.APP_THEME])
    }

    // =========================================================================
    // 3. Screen View Recomposition Deduplication (AUR-P3-022)
    // =========================================================================

    @Test
    fun testScreenDeduplicatorRecompositionGuard() {
        // Simulate 10 rapid recompositions of the same screen
        repeat(10) {
            val wasLogged = deduplicator.onScreenVisible("main_tab_island")
            if (it == 0) assertTrue(wasLogged) else assertFalse(wasLogged)
        }

        // Exactly one screen_view event must be recorded
        assertEquals(1, tracker.screens.size)
        assertEquals("main_tab_island", tracker.screens.first().name)
    }

    @Test
    fun testScreenDeduplicatorTabSwitchingTransitions() {
        deduplicator.onScreenVisible("main_tab_island")
        deduplicator.onScreenVisible("main_tab_features")
        deduplicator.onScreenVisible("subscreen_premium")
        deduplicator.onScreenVisible("main_tab_island")

        assertEquals(4, tracker.screens.size)
        assertEquals("main_tab_island", tracker.screens[0].name)
        assertEquals("main_tab_features", tracker.screens[1].name)
        assertEquals("subscreen_premium", tracker.screens[2].name)
        assertEquals("main_tab_island", tracker.screens[3].name)
    }

    // =========================================================================
    // 4. Idempotent Purchase Conversion Tracking & State Assertion
    // =========================================================================

    @Test
    fun testNonPurchasedStateDoesNotTriggerPurchaseConversion() {
        val tracked = conversionTracker.trackVerifiedPurchase(
            purchaseState = Purchase.PurchaseState.PENDING,
            transactionId = "GPA.PENDING-123",
            planId = "pro_yearly",
            formattedPrice = "₹599"
        )
        assertFalse("Pending purchase must NEVER fire purchase event", tracked)
        assertFalse(tracker.hasEvent(AnalyticsEventNames.PURCHASE))
    }

    @Test
    fun testPurchasedStateTriggersConversionAndIsIdempotent() {
        // 1. First emission: must track
        val firstTracked = conversionTracker.trackVerifiedPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            transactionId = "GPA.CONFIRMED-999",
            planId = "pro_lifetime",
            formattedPrice = "₹999"
        )
        assertTrue(firstTracked)
        assertEquals(1, tracker.getEvents(AnalyticsEventNames.PURCHASE).size)

        val purchaseEvent = tracker.getEvents(AnalyticsEventNames.PURCHASE).first()
        assertEquals("GPA.CONFIRMED-999", purchaseEvent.parameters[AnalyticsParamNames.TRANSACTION_ID])
        assertEquals("pro_lifetime", purchaseEvent.parameters[AnalyticsParamNames.PLAN_ID])
        assertEquals(999.0, purchaseEvent.parameters[AnalyticsParamNames.VALUE])
        assertEquals("INR", purchaseEvent.parameters[AnalyticsParamNames.CURRENCY])

        // 2. Second emission with same purchase: MUST NOT duplicate
        val secondTracked = conversionTracker.trackVerifiedPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            transactionId = "GPA.CONFIRMED-999",
            planId = "pro_lifetime",
            formattedPrice = "₹999"
        )
        assertFalse("Duplicate confirmed purchase must be suppressed", secondTracked)
        assertEquals(1, tracker.getEvents(AnalyticsEventNames.PURCHASE).size)
    }

    @Test
    fun testPrepopulatedPersistentTransactionIdsPreventConversionDuplicateAcrossRestarts() {
        val persistedTracker = SubscriptionConversionTracker(
            analyticsTracker = tracker,
            initialRecordedTransactions = setOf("GPA.EXISTING-TRANS-42")
        )

        val tracked = persistedTracker.trackVerifiedPurchase(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            transactionId = "GPA.EXISTING-TRANS-42",
            planId = "pro_weekly",
            formattedPrice = "₹49"
        )
        assertFalse("Pre-populated persistent transaction must not be tracked again", tracked)
        assertFalse(tracker.hasEvent(AnalyticsEventNames.PURCHASE))
    }

    // =========================================================================
    // 5. AdMob Impression-Level Ad Revenue (ILAR)
    // =========================================================================

    @Test
    fun testAdMobRevenueTrackerProducesStandardIlarEvent() {
        revenueTracker.trackAdRevenue(
            valueMicros = 2_500_000L, // $2.50
            currencyCode = "USD",
            precisionTypeName = "PRECISE",
            adFormat = "rewarded",
            adUnitId = "ca-app-pub-3940256099942544/5224354917",
            placement = "rewarded_pass_calendar"
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.AD_IMPRESSION))
        val impression = tracker.getEvents(AnalyticsEventNames.AD_IMPRESSION).first()

        assertEquals(2.50, impression.parameters[AnalyticsParamNames.VALUE] as Double, 0.0001)
        assertEquals("USD", impression.parameters[AnalyticsParamNames.CURRENCY])
        assertEquals("admob", impression.parameters[AnalyticsParamNames.AD_PLATFORM])
        assertEquals("rewarded", impression.parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals("ca-app-pub-3940256099942544/5224354917", impression.parameters[AnalyticsParamNames.AD_UNIT_ID])
        assertEquals("rewarded_pass_calendar", impression.parameters[AnalyticsParamNames.PLACEMENT])
        assertEquals("PRECISE", impression.parameters[AnalyticsParamNames.PRECISION_TYPE])
    }

    // =========================================================================
    // 6. Play Billing Diagnostics & Rate Limiting
    // =========================================================================

    @Test
    fun testBillingConnectionLostLogsAnalyticsDiagnostic() {
        billingReporter.onConnectionLost(
            failureType = "service_disconnected",
            retryAttempt = 1,
            purchaseFlowActive = true
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_CONNECTION_LOST))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_CONNECTION_LOST).first()
        assertEquals("service_disconnected", event.parameters[AnalyticsParamNames.FAILURE_TYPE])
        assertEquals(1, event.parameters["retry_attempt"])
        assertEquals(true, event.parameters["purchase_flow_active"])
    }

    @Test
    fun testBillingConnectionRestoredResetsEpisode() {
        billingReporter.onConnectionLost("service_disconnected", 1, false)
        billingReporter.onConnectionRestored(retryAttempt = 2)

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_CONNECTION_RESTORED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_CONNECTION_RESTORED).first()
        assertEquals(2, event.parameters["retry_attempt"])
    }
}
