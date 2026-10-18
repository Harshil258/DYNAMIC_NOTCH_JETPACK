package ai.emots.kishan_dynamic.data.premium

/** Pure visibility rules shared by every app-owned ad surface. */
object AdSurfacePolicy {
    private fun shouldShow(
        adsEnabled: Boolean,
        isAdFree: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean
    ): Boolean = adsEnabled && !isAdFree && adUnitConfigured && canRequestAds

    fun shouldShowBanner(
        adsEnabled: Boolean,
        isAdFree: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean = true
    ): Boolean = shouldShow(adsEnabled, isAdFree, adUnitConfigured, canRequestAds)

    fun shouldShowNative(
        adsEnabled: Boolean,
        isAdFree: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean = true
    ): Boolean = shouldShow(adsEnabled, isAdFree, adUnitConfigured, canRequestAds)
}
