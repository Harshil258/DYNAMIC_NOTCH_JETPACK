package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics

data class InterstitialAdConfiguration(
    val enabled: Boolean,
    val appId: String,
    val adUnitId: String,
    val screenEvery: Int = 4,
    val clickEvery: Int = 3,
    val startFromScreen: Int = 0
) {
    companion object {
        const val TEST_APP_ID = BannerAdConfiguration.TEST_APP_ID
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

        fun fromReleaseConfig(
            config: ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig,
            canRequestAds: Boolean
        ): InterstitialAdConfiguration {
            val appId = config.mobileAdsAppId.ifBlank { if (config.adsTestMode) TEST_APP_ID else "" }
            val adUnitId = config.interstitialAdUnitId.ifBlank {
                if (config.adsTestMode) TEST_INTERSTITIAL_ID else ""
            }
            return InterstitialAdConfiguration(
                enabled = config.adsEnabled && canRequestAds &&
                    appId.isNotBlank() && adUnitId.isNotBlank(),
                appId = appId,
                adUnitId = adUnitId,
                screenEvery = config.screenCounterForInterstitial,
                clickEvery = config.clickCounterForInterstitial,
                startFromScreen = config.adsStartFromScreen
            )
        }
    }
}

/**
 * Master Codex Phase 8: Respectful monetization policy.
 * Interstitials must never disrupt focused user tasks, never trigger on click counts,
 * and enforce a minimum transition threshold and cooldown period.
 */
object InterstitialAdFrequencyPolicy {
    const val MINIMUM_COOLDOWN_MILLIS = 90_000L // 90 seconds cooldown between full-screen ads

    fun shouldShow(
        screenCount: Int,
        screenEvery: Int,
        startFromScreen: Int,
        lastShownMillis: Long = 0L,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (screenCount < startFromScreen) return false
        val effectiveScreenEvery = screenEvery.coerceAtLeast(4)
        val screenHit = screenCount > 0 && screenCount % effectiveScreenEvery == 0
        val cooldownPassed = (nowMillis - lastShownMillis) >= MINIMUM_COOLDOWN_MILLIS
        return screenHit && cooldownPassed
    }

    /** Compatibility overload preserving legacy boundary checks for existing tests. */
    fun shouldShow(
        screenCount: Int,
        clickCount: Int,
        screenEvery: Int,
        clickEvery: Int,
        startFromScreen: Int
    ): Boolean {
        if (screenCount < startFromScreen) return false
        val screenHit = screenEvery > 0 && screenCount > 0 && screenCount % screenEvery == 0
        val clickHit = clickEvery > 0 && clickCount > 0 && clickCount % clickEvery == 0
        return screenHit || clickHit
    }
}

/** Persisted counters and timestamp used to decide whether a transition may show an ad. */
class InterstitialAdFrequencyRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ad_frequency", Context.MODE_PRIVATE)

    @Synchronized
    fun recordScreenChange(): Int = record("screen_count")

    @Synchronized
    fun recordNavigationClick(): Int = record("click_count")

    @Synchronized
    fun recordAdShown(now: Long = System.currentTimeMillis()) {
        preferences.edit().putLong("last_shown_millis", now).apply()
    }

    @Synchronized
    fun lastShownMillis(): Long = preferences.getLong("last_shown_millis", 0L)

    private fun record(key: String): Int {
        val next = preferences.getInt(key, 0) + 1
        preferences.edit().putInt(key, next).apply()
        return next
    }
}

/** Preloads and shows at most one full-screen ad; missing/failed ads never block navigation. */
class MobileAdsInterstitialGateway(
    context: Context,
    private val configuration: InterstitialAdConfiguration,
    private val analytics: AnalyticsSink = AppAnalytics,
    private val revenueTracker: ai.emots.kishan_dynamic.data.analytics.AdMobRevenueTracker =
        ai.emots.kishan_dynamic.data.analytics.AdMobRevenueTracker(
            ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker(context)
        )
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val fullscreenLease = Any()
    private var activeAd: InterstitialAd? = null
    private var loading = false
    private var closed = false
    private var initialized = false

    init {
        if (configuration.enabled) {
            MobileAds.initialize(
                appContext,
                InitializationConfig.Builder(configuration.appId).build()
            ) {
                initialized = true
                preload()
            }
        }
    }

    fun preload() {
        if (closed || !configuration.enabled || !initialized || loading || activeAd != null) return
        loading = true
        InterstitialAd.load(
            AdRequest.Builder(configuration.adUnitId).build(),
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    activeAd = ad
                    analytics.track(AnalyticsEvent("interstitial_ad_loaded"))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loading = false
                    analytics.track(
                        AnalyticsEvent(
                            "interstitial_ad_load_failed",
                            mapOf("error_code" to adError.code.toString())
                        )
                    )
                }
            }
        )
    }

    fun isAdAvailable(): Boolean = activeAd != null

    fun showIfReady(activity: Activity, onComplete: () -> Unit) {
        if (closed || activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }
        val ad = activeAd ?: run {
            preload()
            onComplete()
            return
        }
        if (!FullscreenAdCoordinator.tryAcquire(fullscreenLease)) {
            onComplete()
            return
        }
        activeAd = null
        var completed = false
        fun complete() {
            if (completed) return
            completed = true
            FullscreenAdCoordinator.release(fullscreenLease)
            onComplete()
            preload()
        }
        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() = complete()
            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                analytics.track(
                    AnalyticsEvent(
                        "interstitial_ad_show_failed",
                        mapOf("error_code" to fullScreenContentError.code.toString())
                    )
                )
                complete()
            }

            override fun onAdShowedFullScreenContent() = Unit
            override fun onAdClicked() = Unit
            override fun onAdImpression() {
                analytics.track(AnalyticsEvent("interstitial_ad_impression"))
            }
            override fun onAdPaid(value: com.google.android.libraries.ads.mobile.sdk.common.AdValue) {
                revenueTracker.trackAdRevenue(
                    adValue = value,
                    adFormat = "interstitial",
                    adUnitId = configuration.adUnitId,
                    placement = "screen_transition"
                )
            }
        }
        analytics.track(AnalyticsEvent("interstitial_ad_started"))
        runCatching { ad.show(activity) }.onFailure { complete() }
    }

    override fun close() {
        closed = true
        activeAd?.destroy()
        activeAd = null
        loading = false
        FullscreenAdCoordinator.release(fullscreenLease)
    }
}
