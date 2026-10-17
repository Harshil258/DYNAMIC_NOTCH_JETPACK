package ai.emots.kishan_dynamic.data.release

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseProviderContractTest {

    @Test
    fun localConfigDefaultsToSafeOfflineValues() = runBlocking {
        val config = LocalReleaseRuntimeConfigProvider().config.first()

        assertEquals(false, config.adsEnabled)
        assertEquals(true, config.ratingEnabled)
        assertEquals(false, config.updateEnabled)
        assertEquals(false, config.rewardedPassEnabled)
        assertEquals(false, config.analyticsEnabled)
    }

    @Test
    fun analyticsSinkPreservesStructuredProviderEvents() {
        val sink = InMemoryAnalyticsSink()
        sink.track(AnalyticsEvent("premium_purchase_started", mapOf("plan" to "yearly")))

        assertEquals("premium_purchase_started", sink.snapshot().single().name)
        assertTrue(sink.snapshot().single().parameters.containsKey("plan"))
    }

    @Test
    fun decoderAcceptsRemoteCamelCaseAndSnakeCaseFields() {
        val config = ReleaseRuntimeConfigDecoder.decode(
            """
            {
              "ads_enabled": true,
              "rating_enabled": false,
              "update_enabled": true,
              "latest_version_code": 4,
              "minimumVersionCode": 3,
              "update_notes": "Faster overlay\nBetter controls",
              "update_url": "https://updates.example.test/aurora",
              "rewardedPassEnabled": true,
              "rewarded_pass_hours": 36,
              "mobileAdsAppId": "app-id",
              "rewarded_ad_unit_id": "unit-id",
              "adsTestMode": false
              ,"analytics_endpoint": "https://analytics.example.test/events"
            }
            """.trimIndent()
        )

        assertTrue(config.adsEnabled)
        assertEquals(false, config.ratingEnabled)
        assertEquals(true, config.updateEnabled)
        assertEquals(4, config.latestVersionCode)
        assertEquals(3, config.minimumVersionCode)
        assertEquals("Faster overlay\nBetter controls", config.updateNotes)
        assertEquals("https://updates.example.test/aurora", config.updateUrl)
        assertTrue(config.rewardedPassEnabled)
        assertEquals(36, config.rewardedPassHours)
        assertEquals("app-id", config.mobileAdsAppId)
        assertEquals("unit-id", config.rewardedAdUnitId)
        assertEquals(false, config.adsTestMode)
        assertEquals("https://analytics.example.test/events", config.analyticsEndpoint)
    }

    @Test
    fun decoderPreservesBannerAdUnitId() {
        val config = ReleaseRuntimeConfigDecoder.decode(
            "{\"adsEnabled\":true,\"mobileAdsAppId\":\"app-id\",\"banner_ad_unit_id\":\"banner-id\"}"
        )

        assertEquals("banner-id", config.bannerAdUnitId)
    }

    @Test
    fun decoderPreservesNativeAdUnitId() {
        val config = ReleaseRuntimeConfigDecoder.decode(
            "{\"adsEnabled\":true,\"native_ad_unit_id\":\"native-id\"}"
        )

        assertEquals("native-id", config.nativeAdUnitId)
    }

    @Test
    fun decoderPreservesInterstitialFrequencyControls() {
        val config = ReleaseRuntimeConfigDecoder.decode(
            """
            {
              "interstitial_ad_unit_id": "interstitial-id",
              "screen_counter_for_interstitial": 6,
              "clickCounterForInterstitial": 5,
              "ads_start_from_screen": 2
            }
            """.trimIndent()
        )

        assertEquals("interstitial-id", config.interstitialAdUnitId)
        assertEquals(6, config.screenCounterForInterstitial)
        assertEquals(5, config.clickCounterForInterstitial)
        assertEquals(2, config.adsStartFromScreen)
    }

    @Test
    fun decoderPreservesAppOpenControls() {
        val config = ReleaseRuntimeConfigDecoder.decode(
            """
            {
              "app_open_ad_unit_id": "app-open-id",
              "resumeAdEnabled": false,
              "preload_app_open": false
            }
            """.trimIndent()
        )

        assertEquals("app-open-id", config.appOpenAdUnitId)
        assertEquals(false, config.resumeAdEnabled)
        assertEquals(false, config.preloadAppOpen)
    }

    @Test
    fun analyticsJsonEscapesEventValues() {
        val json = AnalyticsEventJson.encode(
            AnalyticsEvent("screen_viewed", mapOf("title" to "Theme \"preview\""))
        )

        assertTrue(json.contains("Theme \\\"preview\\\""))
        assertTrue(json.startsWith("{\"name\":\"screen_viewed\""))
    }

    @Test
    fun remoteProviderKeepsLastGoodSnapshotWhenRefreshFails() = runBlocking {
        var shouldFail = false
        val provider = RemoteReleaseRuntimeConfigProvider(
            endpoint = "https://config.example.test/app.json",
            fetcher = ReleaseConfigFetcher {
                if (shouldFail) error("offline")
                "{\"adsEnabled\":true,\"rewardedPassEnabled\":true}"
            }
        )

        provider.refresh()
        assertTrue(provider.config.first().adsEnabled)
        shouldFail = true
        provider.refresh()

        assertTrue(provider.config.first().rewardedPassEnabled)
        assertEquals("offline", provider.lastRefreshError.first())
    }

    @Test
    fun blankEndpointDoesNotInvokeTransport() = runBlocking {
        var fetchCount = 0
        val provider = RemoteReleaseRuntimeConfigProvider(
            endpoint = "",
            fetcher = ReleaseConfigFetcher {
                fetchCount += 1
                "{}"
            }
        )

        provider.refresh()

        assertEquals(0, fetchCount)
        assertNull(provider.lastRefreshError.first())
    }

    @Test
    fun offlinePromptRequiresConfiguredEndpointAndRefreshError() {
        assertTrue(
            ReleaseConfigRefreshPolicy.shouldPrompt(
                endpoint = "https://config.example.test/app.json",
                error = "offline"
            )
        )
        assertEquals(
            false,
            ReleaseConfigRefreshPolicy.shouldPrompt(
                endpoint = "",
                error = "offline"
            )
        )
        assertEquals(
            false,
            ReleaseConfigRefreshPolicy.shouldPrompt(
                endpoint = "https://config.example.test/app.json",
                error = null
            )
        )
    }

    @Test
    fun testDecodingReferenceProjectAdsDetailsNewPayload() {
        val referenceJson = """
        {
          "adsdetailsnew": {
            "isAdsEnabled": true,
            "isTestModeEnabled": true,
            "admobBannerId": "ca-app-pub-4592759972304958/9884587664",
            "admobInterstitialId": "ca-app-pub-4592759972304958/9483422409",
            "admobNativeId": "ca-app-pub-4592759972304958/2528741163",
            "admobAppOpenId": "ca-app-pub-4592759972304958/4437207434",
            "admobRewardedId": "ca-app-pub-4592759972304958/7864434685",
            "isResumeAdEnabled": true,
            "isSplashInterstitialEnabled": true,
            "isBackInterstitialEnabled": true,
            "isExitNativeEnabled": true,
            "clickCounterForInterstitial": 30,
            "screenCounterForInterstitial": 2,
            "adsStartFromScreen": 1,
            "isPreloadInterstitial": true,
            "isPreloadNative": true,
            "isPreloadBanner": true,
            "isPreloadAppOpen": true,
            "bigNativePercentage": 80,
            "premium_config": {
              "isPremiumEnabled": true,
              "weeklyPlanId": "dynamic_weekly",
              "monthlyPlanId": "dynamic_monthly",
              "yearlyPlanId": "dynamic_yearly",
              "lifetimePlanId": "dynamic_lifetime",
              "features": [
                "quick_control",
                "theme_settings",
                "sound_settings"
              ],
              "trustSection": {
                "title": "Trusted by Thousands",
                "stat1Value": "1.1K+",
                "stat1Label": "Happy Users",
                "stat2Value": "4.9+",
                "stat2Label": "Ratings",
                "stat3Value": "30+",
                "stat3Label": "Reviews",
                "isVisible": true
              }
            }
          }
        }
        """.trimIndent()

        val config = ReleaseRuntimeConfigDecoder.decode(referenceJson)

        assertTrue(config.adsEnabled)
        assertTrue(config.adsTestMode)
        assertEquals("ca-app-pub-4592759972304958/9884587664", config.bannerAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/9483422409", config.interstitialAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/2528741163", config.nativeAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/4437207434", config.appOpenAdUnitId)
        assertEquals("ca-app-pub-4592759972304958/7864434685", config.rewardedAdUnitId)
        assertTrue(config.resumeAdEnabled)
        assertTrue(config.isSplashInterstitialEnabled)
        assertTrue(config.isBackInterstitialEnabled)
        assertTrue(config.isExitNativeEnabled)
        assertEquals(30, config.clickCounterForInterstitial)
        assertEquals(2, config.screenCounterForInterstitial)
        assertEquals(1, config.adsStartFromScreen)
        assertTrue(config.isPreloadInterstitial)
        assertTrue(config.isPreloadNative)
        assertTrue(config.isPreloadBanner)
        assertTrue(config.preloadAppOpen)
        assertEquals(80, config.bigNativePercentage)
        assertTrue(config.premiumEnabled)
        assertEquals("dynamic_weekly", config.weeklyPlanId)
        assertEquals(listOf("quick_control", "theme_settings", "sound_settings"), config.premiumFeatures)
        assertTrue(config.trustSectionVisible)
        assertEquals("Trusted by Thousands", config.trustSectionTitle)
        assertEquals("1.1K+", config.trustStat1Value)
    }
}
