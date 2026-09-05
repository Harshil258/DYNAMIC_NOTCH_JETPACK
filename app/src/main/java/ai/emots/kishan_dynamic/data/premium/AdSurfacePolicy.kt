package ai.emots.kishan_dynamic.data.premium

/** Pure visibility rules shared by every app-owned ad surface. */
object AdSurfacePolicy {
    private fun shouldShow(
        adsEnabled: Boolean,
        isProActive: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean
    ): Boolean = adsEnabled && !isProActive && adUnitConfigured && canRequestAds

    fun shouldShowBanner(
        adsEnabled: Boolean,
        isProActive: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean = true
    ): Boolean = shouldShow(adsEnabled, isProActive, adUnitConfigured, canRequestAds)

    fun shouldShowNative(
        adsEnabled: Boolean,
        isProActive: Boolean,
        adUnitConfigured: Boolean,
        canRequestAds: Boolean = true
    ): Boolean = shouldShow(adsEnabled, isProActive, adUnitConfigured, canRequestAds)
}
