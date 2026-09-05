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
}
