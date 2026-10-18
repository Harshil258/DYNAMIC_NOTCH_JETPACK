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
    fun paidAdFreeUserNeverSeesBanner() {
        assertFalse(AdSurfacePolicy.shouldShowBanner(true, true, true))
    }

    @Test
    fun trialAndRewardedPassUsersStillSeeInAppAds() {
        // AUR-P1-025: Temporary Pro access (trial or rewarded pass) does NOT grant ad-free status
        val isAdFree = false
        assertTrue(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = isAdFree, adUnitConfigured = true))
        assertTrue(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = isAdFree, adUnitConfigured = true))
    }

    @Test
    fun paidProSubscribersSuppressAllAds() {
        // Paid subscribers (weekly, monthly, yearly, lifetime) have isAdFree = true
        val isAdFree = true
        assertFalse(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = isAdFree, adUnitConfigured = true))
        assertFalse(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = isAdFree, adUnitConfigured = true))
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
