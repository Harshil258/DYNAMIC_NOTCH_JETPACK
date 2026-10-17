package ai.emots.kishan_dynamic.data.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdMobRevenueTrackerTest {

    private lateinit var tracker: FakeAnalyticsTracker
    private lateinit var revenueTracker: AdMobRevenueTracker

    @Before
    fun setUp() {
        tracker = FakeAnalyticsTracker()
        revenueTracker = AdMobRevenueTracker(tracker)
    }

    @Test
    fun testTrackAdRevenueConvertsMicrosAndParametersAccurately() {
        // 1_750_000 micros = $1.75
        revenueTracker.trackAdRevenue(
            valueMicros = 1_750_000L,
            currencyCode = "USD",
            precisionTypeName = "ESTIMATED",
            adFormat = "interstitial",
            adUnitId = "ca-app-pub-3940256099942544/1033173712",
            placement = "tab_switch"
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.AD_IMPRESSION))
        val event = tracker.getEvents(AnalyticsEventNames.AD_IMPRESSION).first()

        assertEquals(1.75, event.parameters[AnalyticsParamNames.VALUE] as Double, 0.0001)
        assertEquals("USD", event.parameters[AnalyticsParamNames.CURRENCY])
        assertEquals("admob", event.parameters[AnalyticsParamNames.AD_PLATFORM])
        assertEquals("interstitial", event.parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals("ca-app-pub-3940256099942544/1033173712", event.parameters[AnalyticsParamNames.AD_UNIT_ID])
        assertEquals("tab_switch", event.parameters[AnalyticsParamNames.PLACEMENT])
        assertEquals("ESTIMATED", event.parameters[AnalyticsParamNames.PRECISION_TYPE])
    }

    @Test
    fun testTrackAdRevenueSmallFractionalMicros() {
        // 25_000 micros = 0.025 currency units
        revenueTracker.trackAdRevenue(
            valueMicros = 25_000L,
            currencyCode = "INR",
            precisionTypeName = "PRECISE",
            adFormat = "banner",
            adUnitId = "ca-app-pub-3940256099942544/6300978111",
            placement = "bottom_banner"
        )

        val event = tracker.getEvents(AnalyticsEventNames.AD_IMPRESSION).first()
        assertEquals(0.025, event.parameters[AnalyticsParamNames.VALUE] as Double, 0.0001)
        assertEquals("INR", event.parameters[AnalyticsParamNames.CURRENCY])
        assertEquals("banner", event.parameters[AnalyticsParamNames.AD_FORMAT])
        assertEquals("PRECISE", event.parameters[AnalyticsParamNames.PRECISION_TYPE])
    }
}
