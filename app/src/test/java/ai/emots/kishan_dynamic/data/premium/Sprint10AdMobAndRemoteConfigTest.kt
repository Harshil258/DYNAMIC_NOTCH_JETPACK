package ai.emots.kishan_dynamic.data.premium

import ai.emots.kishan_dynamic.data.analytics.AdMobRevenueTracker
import ai.emots.kishan_dynamic.data.analytics.AnalyticsEventNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsParamNames
import ai.emots.kishan_dynamic.data.analytics.FakeAnalyticsTracker
import ai.emots.kishan_dynamic.data.preferences.BillingEntitlementRecord
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfigDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class Sprint10AdMobAndRemoteConfigTest {

    // =========================================================================
    // 1. UMP CONSENT & PRIVACY OPTIONS RECOVERY (AUR-P1-024)
    // =========================================================================

    @Test
    fun consentPolicyAllowsAdsOnlyWhenStatusReadyAndCanRequestAdsTrue() {
        // Not requested
        val notRequested = AdConsentSnapshot(status = AdConsentStatus.NOT_REQUESTED, canRequestAds = false)
        assertFalse(AdConsentPolicy.canRequestAds(notRequested))

        // Loading
        val loading = AdConsentSnapshot(status = AdConsentStatus.LOADING, canRequestAds = false)
        assertFalse(AdConsentPolicy.canRequestAds(loading))

        // Failed
        val failed = AdConsentSnapshot(status = AdConsentStatus.FAILED, canRequestAds = false, errorMessage = "Error")
        assertFalse(AdConsentPolicy.canRequestAds(failed))

        // Ready but user denied consent (non-EEA or denied)
        val readyDenied = AdConsentSnapshot(status = AdConsentStatus.READY, canRequestAds = false)
        assertFalse(AdConsentPolicy.canRequestAds(readyDenied))

        // Ready and consent granted
        val readyGranted = AdConsentSnapshot(status = AdConsentStatus.READY, canRequestAds = true)
        assertTrue(AdConsentPolicy.canRequestAds(readyGranted))
    }

    @Test
    fun consentPolicyAccuratelyReflectsPrivacyOptionsRequirement() {
        val requiredSnapshot = AdConsentSnapshot(
            status = AdConsentStatus.READY,
            canRequestAds = true,
            privacyOptionsRequired = true
        )
        assertTrue(AdConsentPolicy.isPrivacyOptionsRequired(requiredSnapshot))

        val notRequiredSnapshot = AdConsentSnapshot(
            status = AdConsentStatus.READY,
            canRequestAds = true,
            privacyOptionsRequired = false
        )
        assertFalse(AdConsentPolicy.isPrivacyOptionsRequired(notRequiredSnapshot))
    }

    // =========================================================================
    // 2. AD SURFACE POLICY & TRIAL VS PAID AD MONETIZATION (AUR-P1-025)
    // =========================================================================

    @Test
    fun freeUsersReceiveInAppBannerAndNativeAds() {
        val adsEnabled = true
        val isAdFree = false
        val adUnitConfigured = true
        val canRequestAds = true

        assertTrue(AdSurfacePolicy.shouldShowBanner(adsEnabled, isAdFree, adUnitConfigured, canRequestAds))
        assertTrue(AdSurfacePolicy.shouldShowNative(adsEnabled, isAdFree, adUnitConfigured, canRequestAds))
    }

    @Test
    fun trialAndRewardedUsersReceiveInAppBannerAndNativeAds() {
        // AUR-P1-025: 7-day install trial and rewarded pass users have full feature access,
        // but are NOT ad-free in the companion app.
        val trialSnapshot = EntitlementSnapshot(
            source = EntitlementSource.INSTALL_TRIAL,
            isPro = true,
            isAdFree = false
        )
        val rewardedSnapshot = EntitlementSnapshot(
            source = EntitlementSource.REWARDED_ACCESS,
            isPro = true,
            isAdFree = false
        )

        assertTrue("Trial users must show banner ads", AdSurfacePolicy.shouldShowBanner(
            adsEnabled = true,
            isAdFree = trialSnapshot.isAdFree,
            adUnitConfigured = true,
            canRequestAds = true
        ))
        assertTrue("Trial users must show native ads", AdSurfacePolicy.shouldShowNative(
            adsEnabled = true,
            isAdFree = trialSnapshot.isAdFree,
            adUnitConfigured = true,
            canRequestAds = true
        ))

        assertTrue("Rewarded users must show banner ads", AdSurfacePolicy.shouldShowBanner(
            adsEnabled = true,
            isAdFree = rewardedSnapshot.isAdFree,
            adUnitConfigured = true,
            canRequestAds = true
        ))
        assertTrue("Rewarded users must show native ads", AdSurfacePolicy.shouldShowNative(
            adsEnabled = true,
            isAdFree = rewardedSnapshot.isAdFree,
            adUnitConfigured = true,
            canRequestAds = true
        ))
    }

    @Test
    fun paidProSubscribersSuppressAllInAppAdsOneHundredPercent() {
        val paidSubscribers = listOf(
            EntitlementSnapshot(source = EntitlementSource.WEEKLY, isPro = true, isAdFree = true),
            EntitlementSnapshot(source = EntitlementSource.MONTHLY, isPro = true, isAdFree = true),
            EntitlementSnapshot(source = EntitlementSource.YEARLY, isPro = true, isAdFree = true),
            EntitlementSnapshot(source = EntitlementSource.LIFETIME, isPro = true, isAdFree = true)
        )

        for (subscriber in paidSubscribers) {
            assertFalse(
                "Paid subscriber (${subscriber.source}) must suppress banner ads",
                AdSurfacePolicy.shouldShowBanner(
                    adsEnabled = true,
                    isAdFree = subscriber.isAdFree,
                    adUnitConfigured = true,
                    canRequestAds = true
                )
            )
            assertFalse(
                "Paid subscriber (${subscriber.source}) must suppress native ads",
                AdSurfacePolicy.shouldShowNative(
                    adsEnabled = true,
                    isAdFree = subscriber.isAdFree,
                    adUnitConfigured = true,
                    canRequestAds = true
                )
            )
        }
    }

    @Test
    fun adsSuppressedWhenConsentMissingOrAdUnitEmpty() {
        // Without consent
        assertFalse(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = false, adUnitConfigured = true, canRequestAds = false))
        assertFalse(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = false, adUnitConfigured = true, canRequestAds = false))

        // Without ad unit ID
        assertFalse(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = false, adUnitConfigured = false, canRequestAds = true))
        assertFalse(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = false, adUnitConfigured = false, canRequestAds = true))

        // When master ads toggle is false
        assertFalse(AdSurfacePolicy.shouldShowBanner(adsEnabled = false, isAdFree = false, adUnitConfigured = true, canRequestAds = true))
        assertFalse(AdSurfacePolicy.shouldShowNative(adsEnabled = false, isAdFree = false, adUnitConfigured = true, canRequestAds = true))
    }

    // =========================================================================
    // 3. INTERSTITIAL FREQUENCY & ZERO ARTIFICIAL DELAY (AUR-P2-030)
    // =========================================================================

    @Test
    fun interstitialFrequencyRequiresMinimumNinetySecondsCooldown() {
        val now = 100_000L
        val screenCount = 4
        val screenEvery = 4
        val startFromScreen = 0

        // 89 seconds elapsed -> cooldown NOT passed
        assertFalse(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = screenCount,
                screenEvery = screenEvery,
                startFromScreen = startFromScreen,
                lastShownMillis = now - 89_000L,
                nowMillis = now
            )
        )

        // 90 seconds elapsed -> cooldown passed
        assertTrue(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = screenCount,
                screenEvery = screenEvery,
                startFromScreen = startFromScreen,
                lastShownMillis = now - 90_000L,
                nowMillis = now
            )
        )
    }

    @Test
    fun interstitialFrequencyRequiresMinimumFourScreensAndRespectsStartFromScreen() {
        val now = 200_000L
        val lastShown = now - 120_000L // Cooldown passed

        // Before startFromScreen
        assertFalse(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = 2,
                screenEvery = 4,
                startFromScreen = 3,
                lastShownMillis = lastShown,
                nowMillis = now
            )
        )

        // Screen counter not a multiple of 4
        assertFalse(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = 5,
                screenEvery = 4,
                startFromScreen = 0,
                lastShownMillis = lastShown,
                nowMillis = now
            )
        )

        // Screen counter multiple of 4
        assertTrue(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = 8,
                screenEvery = 4,
                startFromScreen = 0,
                lastShownMillis = lastShown,
                nowMillis = now
            )
        )
    }

    // =========================================================================
    // 4. APP OPEN AD POLICY: FRESHNESS, COLD-LAUNCH & FULLSCREEN GUARD
    // =========================================================================

    @Test
    fun appOpenPolicyEnforcesFourHourFreshnessLimit() {
        val now = System.currentTimeMillis()
        val fourHoursMillis = TimeUnit.HOURS.toMillis(4)

        // Fresh ad (1 hour old)
        assertTrue(AppOpenAdPolicy.isFresh(loadedAtMillis = now - TimeUnit.HOURS.toMillis(1), nowMillis = now, maxAgeMillis = fourHoursMillis))

        // Stale ad (4 hours + 1 second old)
        assertFalse(AppOpenAdPolicy.isFresh(loadedAtMillis = now - (fourHoursMillis + 1000L), nowMillis = now, maxAgeMillis = fourHoursMillis))

        // Uninitialized ad
        assertFalse(AppOpenAdPolicy.isFresh(loadedAtMillis = 0L, nowMillis = now, maxAgeMillis = fourHoursMillis))
    }

    @Test
    fun appOpenPolicyBlocksColdLaunchAndFullscreenCollisions() {
        val config = AppOpenAdConfiguration(
            enabled = true,
            appId = "test_app_id",
            adUnitId = "test_unit_id"
        )

        // Normal foreground resume when ad is ready -> Should show
        assertTrue(
            AppOpenAdPolicy.shouldShow(
                configuration = config,
                isColdLaunch = false,
                isBlocked = false,
                isPremium = false,
                isAdAvailable = true,
                isAnotherFullscreenAdShowing = false
            )
        )

        // Cold launch splash -> Must NOT show
        assertFalse(
            AppOpenAdPolicy.shouldShow(
                configuration = config,
                isColdLaunch = true,
                isBlocked = false,
                isPremium = false,
                isAdAvailable = true,
                isAnotherFullscreenAdShowing = false
            )
        )

        // When another full-screen ad (e.g. interstitial or rewarded) is showing -> Must NOT show
        assertFalse(
            AppOpenAdPolicy.shouldShow(
                configuration = config,
                isColdLaunch = false,
                isBlocked = false,
                isPremium = false,
                isAdAvailable = true,
                isAnotherFullscreenAdShowing = true
            )
        )

        // When blocked (e.g. permission screen or call screen active) -> Must NOT show
        assertFalse(
            AppOpenAdPolicy.shouldShow(
                configuration = config,
                isColdLaunch = false,
                isBlocked = true,
                isPremium = false,
                isAdAvailable = true,
                isAnotherFullscreenAdShowing = false
            )
        )
    }

    // =========================================================================
    // 5. FULLSCREEN AD COORDINATOR MUTUAL EXCLUSION
    // =========================================================================

    @Test
    fun fullscreenAdCoordinatorPreventsConcurrentFullscreenAds() {
        val leaseA = Any()
        val leaseB = Any()

        // Initially no full-screen ad is active
        assertFalse(FullscreenAdCoordinator.isShowing())

        // Acquire lease A
        assertTrue(FullscreenAdCoordinator.tryAcquire(leaseA))
        assertTrue(FullscreenAdCoordinator.isShowing())

        // Lease B cannot acquire while lease A is holding
        assertFalse(FullscreenAdCoordinator.tryAcquire(leaseB))

        // Release lease A
        FullscreenAdCoordinator.release(leaseA)
        assertFalse(FullscreenAdCoordinator.isShowing())

        // Now lease B can acquire
        assertTrue(FullscreenAdCoordinator.tryAcquire(leaseB))
        assertTrue(FullscreenAdCoordinator.isShowing())

        // Cleanup
        FullscreenAdCoordinator.release(leaseB)
        assertFalse(FullscreenAdCoordinator.isShowing())
    }

    // =========================================================================
    // 6. GRANULAR ILAR PLACEMENT TAGGING (AUR-P1-026)
    // =========================================================================

    @Test
    fun revenueTrackerAttachesGranularPlacementDimensions() {
        val fakeTracker = FakeAnalyticsTracker()
        val revenueTracker = AdMobRevenueTracker(fakeTracker)

        // 1. Exit dialog native ad
        revenueTracker.trackAdRevenue(
            valueMicros = 1_500_000L,
            currencyCode = "USD",
            precisionTypeName = "ESTIMATED",
            adFormat = "native",
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            placement = "exit_dialog"
        )

        // 2. Settings banner ad
        revenueTracker.trackAdRevenue(
            valueMicros = 500_000L,
            currencyCode = "USD",
            precisionTypeName = "PRECISE",
            adFormat = "banner",
            adUnitId = "ca-app-pub-3940256099942544/6300978111",
            placement = "settings_banner"
        )

        // 3. Tab switch interstitial ad
        revenueTracker.trackAdRevenue(
            valueMicros = 3_000_000L,
            currencyCode = "USD",
            precisionTypeName = "ESTIMATED",
            adFormat = "interstitial",
            adUnitId = "ca-app-pub-3940256099942544/1033173712",
            placement = "screen_transition"
        )

        val events = fakeTracker.getEvents(AnalyticsEventNames.AD_IMPRESSION)
        assertEquals(3, events.size)

        assertEquals("exit_dialog", events[0].parameters[AnalyticsParamNames.PLACEMENT])
        assertEquals("native", events[0].parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals(1.5, events[0].parameters[AnalyticsParamNames.VALUE] as Double, 0.001)

        assertEquals("settings_banner", events[1].parameters[AnalyticsParamNames.PLACEMENT])
        assertEquals("banner", events[1].parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals(0.5, events[1].parameters[AnalyticsParamNames.VALUE] as Double, 0.001)

        assertEquals("screen_transition", events[2].parameters[AnalyticsParamNames.PLACEMENT])
        assertEquals("interstitial", events[2].parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals(3.0, events[2].parameters[AnalyticsParamNames.VALUE] as Double, 0.001)
    }

    // =========================================================================
    // 7. REMOTE CONFIG SCHEMA RESILIENCE & PRODUCTION PARSING (AUR-P1-023)
    // =========================================================================

    @Test
    fun decoderParsesCompleteReferenceCurrentRemoteConfigPayload() {
        val referencePayload = """
        {
            "isAdsEnabled": true,
            "isTestModeEnabled": false,
            "admobBannerId": "ca-app-pub-4592759972304958/9884587664",
            "admobInterstitialId": "ca-app-pub-4592759972304958/9483422409",
            "admobNativeId": "ca-app-pub-4592759972304958/2528741163",
            "admobAppOpenId": "ca-app-pub-4592759972304958/4437207434",
            "admobRewardedId": "ca-app-pub-4592759972304958/7864434685",
            "splashInterstitialId": "/6499/example/interstitial",
            "isBackInterstitialEnabled": false,
            "isResumeAdEnabled": true,
            "isSplashInterstitialEnabled": true,
            "isExitNativeEnabled": true,
            "bigNativePercentage": 50,
            "isPreloadInterstitial": true,
            "isPreloadNative": false,
            "isPreloadBanner": true,
            "isPreloadAppOpen": true,
            "clickCounterForInterstitial": 30,
            "screenCounterForInterstitial": 2,
            "adsStartFromScreen": 0,
            "isOnboardingEnabled": true,
            "isLanguagePageEnabled": true,
            "isRatingDialogEnabled": true,
            "premium_config": {
                "isPremiumEnabled": true,
                "weeklyPlanId": "com.dynamicnotchnotificationbar.dynamicbar.weekly",
                "monthlyPlanId": "com.dynamicnotchnotificationbar.dynamicbar.monthly",
                "yearlyPlanId": "com.dynamicnotchnotificationbar.dynamicbar.yearly",
                "lifetimePlanId": "com.dynamicnotchnotificationbar.dynamicbar.lifetime",
                "rewardAdFreeDays": 7,
                "premiumFeatures": [
                    "quick_control",
                    "theme_settings",
                    "sound_settings",
                    "call_end_disable",
                    "compact_music",
                    "notification_duration",
                    "display_horizontal_offset"
                ],
                "isTrustSectionVisible": true,
                "trustSectionTitle": "Trusted by Thousands",
                "trustStat1Value": "1.1K+",
                "trustStat1Label": "Happy Users",
                "trustStat2Value": "4.9+",
                "trustStat2Label": "Ratings",
                "trustStat3Value": "30+",
                "trustStat3Label": "Reviews"
            }
        }
        """.trimIndent()

        val decoded = ReleaseRuntimeConfigDecoder.decode(referencePayload)

        assertTrue(decoded.adsEnabled)
        assertFalse(decoded.adsTestMode)
        assertEquals("ca-app-pub-4592759972304958/9884587664", decoded.bannerAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/9483422409", decoded.interstitialAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/2528741163", decoded.nativeAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/4437207434", decoded.appOpenAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/7864434685", decoded.rewardedAdUnitId)
        assertTrue(decoded.resumeAdEnabled)
        assertTrue(decoded.isSplashInterstitialEnabled)
        assertFalse(decoded.isBackInterstitialEnabled)
        assertTrue(decoded.isExitNativeEnabled)
        assertEquals(50, decoded.bigNativePercentage)
        assertEquals(2, decoded.screenCounterForInterstitial)
        assertEquals(30, decoded.clickCounterForInterstitial)
        assertEquals(0, decoded.adsStartFromScreen)

        // Nested premium_config resolution
        assertTrue(decoded.premiumEnabled)
        assertEquals("com.dynamicnotchnotificationbar.dynamicbar.weekly", decoded.weeklyPlanId)
        assertEquals("com.dynamicnotchnotificationbar.dynamicbar.monthly", decoded.monthlyPlanId)
        assertEquals("com.dynamicnotchnotificationbar.dynamicbar.yearly", decoded.yearlyPlanId)
        assertEquals("com.dynamicnotchnotificationbar.dynamicbar.lifetime", decoded.lifetimePlanId)
        assertEquals(7, decoded.premiumInstallTrialDays)
        assertTrue(decoded.trustSectionVisible)
        assertEquals("Trusted by Thousands", decoded.trustSectionTitle)
        assertEquals("1.1K+", decoded.trustStat1Value)
        assertEquals("Happy Users", decoded.trustStat1Label)
        assertEquals(7, decoded.premiumFeatures.size)
        assertTrue(decoded.premiumFeatures.contains("quick_control"))
        assertTrue(decoded.premiumFeatures.contains("theme_settings"))
    }

    @Test
    fun decoderFallsBackGracefullyOnMalformedPayload() {
        val fallback = ReleaseRuntimeConfig(
            adsEnabled = false,
            bannerAdUnitId = "fallback_banner",
            premiumInstallTrialDays = 7
        )

        val malformedJson = """{ "ads_enabled": not_a_valid_json """
        val decoded = ReleaseRuntimeConfigDecoder.decode(malformedJson, fallback)

        assertFalse(decoded.adsEnabled)
        assertEquals("fallback_banner", decoded.bannerAdUnitId)
        assertEquals(7, decoded.premiumInstallTrialDays)
    }
}
