package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpenAdPolicyTest {
    private val configuration = AppOpenAdConfiguration(
        enabled = true,
        appId = "app-id",
        adUnitId = "unit-id"
    )

    @Test
    fun freshnessUsesConfiguredFourHourWindow() {
        assertTrue(AppOpenAdPolicy.isFresh(1_000L, 1_000L, 4_000L))
        assertTrue(AppOpenAdPolicy.isFresh(1_000L, 5_000L, 4_000L))
        assertFalse(AppOpenAdPolicy.isFresh(1_000L, 5_001L, 4_000L))
        assertFalse(AppOpenAdPolicy.isFresh(0L, 1_000L, 4_000L))
    }

    @Test
    fun coldLaunchBlockedAndPremiumSessionsNeverShow() {
        assertFalse(AppOpenAdPolicy.shouldShow(configuration, true, false, false, true, false))
        assertFalse(AppOpenAdPolicy.shouldShow(configuration, false, true, false, true, false))
        assertFalse(AppOpenAdPolicy.shouldShow(configuration, false, false, true, true, false))
        assertFalse(AppOpenAdPolicy.shouldShow(configuration, false, false, false, true, true))
    }

    @Test
    fun eligibleResumeRequiresFreshLoadedAd() {
        assertTrue(AppOpenAdPolicy.shouldShow(configuration, false, false, false, true, false))
        assertFalse(AppOpenAdPolicy.shouldShow(configuration, false, false, false, false, false))
        assertFalse(
            AppOpenAdPolicy.shouldShow(
                configuration.copy(enabled = false),
                false,
                false,
                false,
                true,
                false
            )
        )
    }
}
