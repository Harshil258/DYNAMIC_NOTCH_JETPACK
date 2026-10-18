package ai.emots.kishan_dynamic.data.premium

import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.SubscriptionConversionTracker
import ai.emots.kishan_dynamic.data.preferences.BillingEntitlementRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class EntitlementManagerTest {

    private val inactiveTrial = TrialSnapshot(isActive = false)
    private val inactiveReward = RewardedAccessSnapshot(isActive = false)

    // 1. Weekly purchase never becomes Lifetime
    @Test
    fun weeklyPurchaseNeverBecomesLifetime() {
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(7)

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = expiresAt,
            planId = "weekly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false
        )

        assertEquals(EntitlementSource.WEEKLY, snapshot.source)
        assertEquals("Weekly", snapshot.planDisplayName)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertFalse(snapshot.source == EntitlementSource.LIFETIME)
        assertEquals(expiresAt, snapshot.expiresAtMillis)
    }

    // 2. Monthly purchase never becomes Lifetime
    @Test
    fun monthlyPurchaseNeverBecomesLifetime() {
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(30)

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = expiresAt,
            planId = "monthly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false
        )

        assertEquals(EntitlementSource.MONTHLY, snapshot.source)
        assertEquals("Monthly", snapshot.planDisplayName)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertFalse(snapshot.source == EntitlementSource.LIFETIME)
    }

    // 3. Yearly purchase never becomes Lifetime
    @Test
    fun yearlyPurchaseNeverBecomesLifetime() {
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(365)

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = expiresAt,
            planId = "yearly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false
        )

        assertEquals(EntitlementSource.YEARLY, snapshot.source)
        assertEquals("Yearly", snapshot.planDisplayName)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertFalse(snapshot.source == EntitlementSource.LIFETIME)
    }

    // 4. Verified Lifetime purchase remains permanent
    @Test
    fun verifiedLifetimePurchaseRemainsPermanent() {
        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = null,
            planId = "lifetime",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = true
        )

        assertEquals(EntitlementSource.LIFETIME, snapshot.source)
        assertEquals("Lifetime", snapshot.planDisplayName)
        assertTrue(snapshot.isActive)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertNull(snapshot.expiresAtMillis)
        assertEquals(9999, snapshot.remainingDays)
        assertFalse(snapshot.shouldShowExpiryIsland())
    }

    // 5. Missing expiry does not prove Lifetime
    @Test
    fun missingExpiryDoesNotProveLifetime() {
        // Defect AUR-P0-001 regression test: A weekly purchase with missing proExpiresAt must NEVER become Lifetime
        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = null,
            planId = "weekly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false,
            lastVerifiedAt = System.currentTimeMillis()
        )

        assertEquals(EntitlementSource.WEEKLY, snapshot.source)
        assertEquals("Weekly", snapshot.planDisplayName)
        assertFalse(snapshot.source == EntitlementSource.LIFETIME)
        assertTrue(snapshot.remainingDays <= 7)
    }

    // 6. Canceled subscription retains entitlement until actual expiry
    @Test
    fun canceledSubscriptionRetainsEntitlementUntilActualExpiry() {
        val now = System.currentTimeMillis()
        val validExpiry = now + TimeUnit.DAYS.toMillis(4)

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = validExpiry,
            planId = "monthly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false,
            isAutoRenewing = false // user canceled auto-renewal in Google Play Subscriptions
        )

        assertTrue(snapshot.isActive)
        assertTrue(snapshot.isPro)
        assertFalse(snapshot.isAutoRenewing)
        assertEquals(EntitlementSource.MONTHLY, snapshot.source)
        assertTrue(snapshot.remainingDays in 3..4)
    }

    // 7. Expired subscription loses subscription-derived access
    @Test
    fun expiredSubscriptionLosesSubscriptionDerivedAccess() {
        val now = System.currentTimeMillis()
        val expiredTime = now - TimeUnit.HOURS.toMillis(2)

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true, // local preference may lag before queryPurchasesAsync
            proExpiresAt = expiredTime,
            planId = "weekly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false
        )

        assertFalse(snapshot.isPro)
        assertEquals(EntitlementSource.FREE, snapshot.source)
        assertEquals("Free", snapshot.planDisplayName)
        assertTrue(snapshot.shouldShowExpiryIsland())
    }

    // 8. Pending purchase does not unlock paid access prematurely
    @Test
    fun pendingPurchaseDoesNotUnlockPaidAccessPrematurely() {
        val snapshot = EntitlementSnapshot(
            source = EntitlementSource.WEEKLY,
            isPro = true,
            isPendingPayment = true,
            planDisplayName = "Weekly"
        )

        assertFalse("Pending purchase must not be active", snapshot.isActive)
        assertTrue(snapshot.isPendingPayment)
        assertTrue(snapshot.shouldShowExpiryIsland())
    }

    // 9. Refund or revocation removes entitlement appropriately
    @Test
    fun refundOrRevocationRemovesEntitlementAppropriately() {
        val snapshot = EntitlementManager.reconcile(
            isPaidPro = false,
            proExpiresAt = null,
            planId = null,
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false,
            reconciliationState = "revoked"
        )

        assertFalse(snapshot.isPro)
        assertFalse(snapshot.isAdFree)
        assertEquals(EntitlementSource.FREE, snapshot.source)
        assertEquals("Free", snapshot.planDisplayName)
        assertEquals("revoked", snapshot.reconciliationState)
    }

    // 10. Installation trial does not override paid access
    @Test
    fun installationTrialDoesNotOverridePaidAccess() {
        val now = System.currentTimeMillis()
        val activeTrial = TrialSnapshot(
            isActive = true,
            expiresAtMillis = now + TimeUnit.DAYS.toMillis(6),
            remainingDays = 6
        )

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = now + TimeUnit.DAYS.toMillis(30),
            planId = "monthly",
            trialSnapshot = activeTrial,
            rewardedSnapshot = inactiveReward,
            isLifetime = false
        )

        assertEquals(EntitlementSource.MONTHLY, snapshot.source)
        assertTrue(snapshot.isAdFree)
        assertFalse(snapshot.isTrial)
    }

    // 11. Rewarded access does not replace verified paid access
    @Test
    fun rewardedAccessDoesNotReplaceVerifiedPaidAccess() {
        val now = System.currentTimeMillis()
        val activeReward = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = now + TimeUnit.DAYS.toMillis(3),
            remainingDays = 3
        )

        val snapshot = EntitlementManager.reconcile(
            isPaidPro = true,
            proExpiresAt = now + TimeUnit.DAYS.toMillis(7),
            planId = "weekly",
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = activeReward,
            isLifetime = false
        )

        assertEquals(EntitlementSource.WEEKLY, snapshot.source)
        assertTrue(snapshot.isAdFree) // Paid pro remains ad-free, unlike pure rewarded access
        assertFalse(snapshot.isRewarded)
    }

    // 12. Restoration correctly identifies purchased products
    @Test
    fun restorationCorrectlyIdentifiesPurchasedProducts() {
        val catalog = PremiumBillingCatalog.default()

        val weekly = catalog.planByProductId("dynamic_island_weekly")
        val monthly = catalog.planByProductId("dynamic_island_monthly")
        val yearly = catalog.planByProductId("dynamic_island_yearly")
        val lifetime = catalog.planByProductId("dynamic_island_lifetime")

        assertEquals("weekly", weekly?.planId)
        assertEquals("monthly", monthly?.planId)
        assertEquals("yearly", yearly?.planId)
        assertEquals("lifetime", lifetime?.planId)
    }

    // 13. Duplicate purchase callbacks do not duplicate transactions or conversions
    @Test
    fun duplicatePurchaseCallbacksDoNotDuplicateTransactionsOrConversions() {
        val fakeTracker = object : AnalyticsTracker {
            var eventCount = 0
            override fun logEvent(name: String, parameters: Map<String, Any>) {
                eventCount++
            }
            override fun logScreen(screenName: String, screenClass: String) {}
            override fun setUserProperty(name: String, value: String) {}
            override fun setUserId(userId: String?) {}
        }

        val conversionTracker = SubscriptionConversionTracker(fakeTracker)

        val firstAttempt = conversionTracker.trackVerifiedPurchase("GPA.1234-5678-9012-34567", "weekly", "₹39")
        val duplicateAttempt = conversionTracker.trackVerifiedPurchase("GPA.1234-5678-9012-34567", "weekly", "₹39")

        assertTrue("First verified purchase event should be tracked", firstAttempt)
        assertFalse("Duplicate verified purchase event must be blocked (idempotent)", duplicateAttempt)
    }

    // 14. Offline startup handles previously verified ownership safely within 7d cache
    @Test
    fun offlineStartupHandlesPreviouslyVerifiedOwnershipSafelyWithin7DayCache() {
        val now = System.currentTimeMillis()
        val verified2DaysAgo = now - TimeUnit.DAYS.toMillis(2)

        val snapshot = EntitlementManager.reconcile(
            billing = BillingEntitlementRecord(
                isPaidPro = true,
                proExpiresAt = null,
                proSource = "play_billing_weekly",
                planId = "weekly",
                productId = "dynamic_island_weekly",
                isLifetime = false,
                lastVerifiedAt = verified2DaysAgo,
                isAutoRenewing = true,
                reconciliationState = "verified"
            ),
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward
        )

        assertTrue(snapshot.isActive)
        assertTrue(snapshot.isPro)
        assertEquals(EntitlementSource.WEEKLY, snapshot.source)
        assertTrue("Remaining offline tolerance days must be between 4 and 5", snapshot.remainingDays in 4..5)
    }

    // 15. Legacy incorrectly classified entitlement is reconciled correctly
    @Test
    fun legacyIncorrectlyClassifiedEntitlementIsReconciledCorrectly() {
        // A legacy user having source="play_billing" without a verified lifetime flag or plan
        val snapshot = EntitlementManager.reconcile(
            billing = BillingEntitlementRecord(
                isPaidPro = true,
                proExpiresAt = null,
                proSource = "play_billing",
                planId = null,
                productId = null,
                isLifetime = false,
                lastVerifiedAt = System.currentTimeMillis(),
                isAutoRenewing = false,
                reconciliationState = "pending_reconciliation"
            ),
            trialSnapshot = inactiveTrial,
            rewardedSnapshot = inactiveReward
        )

        // Must default to WEEKLY subscription tolerance, NEVER LIFETIME!
        assertFalse("Legacy ambiguous entitlement must never map to Lifetime", snapshot.source == EntitlementSource.LIFETIME)
        assertEquals(EntitlementSource.WEEKLY, snapshot.source)
        assertEquals("Weekly", snapshot.planDisplayName)
        assertEquals("pending_reconciliation", snapshot.reconciliationState)
    }
}
