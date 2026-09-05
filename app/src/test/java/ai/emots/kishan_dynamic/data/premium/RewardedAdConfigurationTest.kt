package ai.emots.kishan_dynamic.data.premium

import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardedAdConfigurationTest {
    @Test
    fun debugConfigurationUsesGoogleTestPlacements() {
        val config = RewardedAdConfiguration.forBuild(isDebug = true)

        assertTrue(config.enabled)
        assertEquals(RewardedAdConfiguration.TEST_APP_ID, config.appId)
        assertEquals(RewardedAdConfiguration.TEST_REWARDED_ID, config.adUnitId)
    }

    @Test
    fun releaseConfigurationIsOptIn() {
        val config = RewardedAdConfiguration.forBuild(isDebug = false)

        assertFalse(config.enabled)
        assertEquals("", config.appId)
        assertEquals("", config.adUnitId)
    }

    @Test
    fun runtimeConfigCanEnableAConfiguredProductionPlacement() {
        val config = RewardedAdConfiguration.fromReleaseConfig(
            ReleaseRuntimeConfig(
                adsEnabled = true,
                rewardedPassEnabled = true,
                mobileAdsAppId = "production-app",
                rewardedAdUnitId = "production-unit",
                rewardedPassHours = 48
            )
        )

        assertTrue(config.enabled)
        assertEquals("production-app", config.appId)
        assertEquals("production-unit", config.adUnitId)
        assertEquals(48, config.passHours)
    }

    @Test
    fun runtimeConfigStaysDisabledUntilConsentAllowsRequests() {
        val config = RewardedAdConfiguration.fromReleaseConfig(
            ReleaseRuntimeConfig(
                adsEnabled = true,
                rewardedPassEnabled = true,
                mobileAdsAppId = "production-app",
                rewardedAdUnitId = "production-unit"
            ),
            canRequestAds = false
        )

        assertFalse(config.enabled)
    }
}
