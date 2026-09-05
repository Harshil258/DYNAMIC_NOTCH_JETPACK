package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdSurfacePolicyTest {
    @Test
    fun freeConfiguredUserCanSeeBanner() {
        assertTrue(AdSurfacePolicy.shouldShowBanner(true, false, true))
    }

    @Test
    fun premiumUserNeverSeesBanner() {
        assertFalse(AdSurfacePolicy.shouldShowBanner(true, true, true))
    }

    @Test
    fun missingConfigurationKeepsBannerHidden() {
        assertFalse(AdSurfacePolicy.shouldShowBanner(true, false, false))
        assertFalse(AdSurfacePolicy.shouldShowBanner(false, false, true))
    }

    @Test
    fun consentMustBeReadyBeforeBannerIsRequested() {
        assertFalse(AdSurfacePolicy.shouldShowBanner(true, false, true, canRequestAds = false))
        assertFalse(AdSurfacePolicy.shouldShowNative(true, false, true, canRequestAds = false))
    }

    @Test
    fun consentPolicyFailsClosedForUnknownOrFailedState() {
        assertFalse(AdConsentPolicy.canRequestAds(AdConsentSnapshot()))
        assertFalse(
            AdConsentPolicy.canRequestAds(
                AdConsentSnapshot(status = AdConsentStatus.FAILED, canRequestAds = true)
            )
        )
        assertTrue(
            AdConsentPolicy.canRequestAds(
                AdConsentSnapshot(status = AdConsentStatus.READY, canRequestAds = true)
            )
        )
    }
}
