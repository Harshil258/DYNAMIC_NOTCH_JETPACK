package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics

data class AppOpenAdConfiguration(
    val enabled: Boolean,
    val appId: String,
    val adUnitId: String,
    val preload: Boolean = true,
    val maxAgeMillis: Long = DEFAULT_MAX_AGE_MILLIS
) {
    companion object {
        const val TEST_APP_ID = BannerAdConfiguration.TEST_APP_ID
        const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
        const val DEFAULT_MAX_AGE_MILLIS = 4L * 60L * 60L * 1000L

        fun fromReleaseConfig(
            config: ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig,
            canRequestAds: Boolean
        ): AppOpenAdConfiguration {
            val appId = config.mobileAdsAppId.ifBlank { if (config.adsTestMode) TEST_APP_ID else "" }
            val adUnitId = config.appOpenAdUnitId.ifBlank {
                if (config.adsTestMode) TEST_APP_OPEN_ID else ""
            }
            return AppOpenAdConfiguration(
                enabled = config.adsEnabled && config.resumeAdEnabled && canRequestAds &&
                    appId.isNotBlank() && adUnitId.isNotBlank(),
                appId = appId,
                adUnitId = adUnitId,
                preload = config.preloadAppOpen
            )
        }
    }
}

object AppOpenAdPolicy {
    fun shouldLoad(configuration: AppOpenAdConfiguration): Boolean =
        configuration.enabled

    fun isFresh(loadedAtMillis: Long, nowMillis: Long, maxAgeMillis: Long): Boolean =
        loadedAtMillis > 0L && nowMillis >= loadedAtMillis &&
            nowMillis - loadedAtMillis <= maxAgeMillis

    fun shouldShow(
        configuration: AppOpenAdConfiguration,
        isColdLaunch: Boolean,
        isBlocked: Boolean,
        isPremium: Boolean,
        isAdAvailable: Boolean,
        isAnotherFullscreenAdShowing: Boolean
    ): Boolean = configuration.enabled && !isColdLaunch && !isBlocked && !isPremium &&
        isAdAvailable && !isAnotherFullscreenAdShowing
}

/** Consent-gated, freshness-aware app-open surface for later foreground resumes. */
class MobileAdsAppOpenGateway(
    context: Context,
    private val configuration: AppOpenAdConfiguration,
    private val analytics: AnalyticsSink = AppAnalytics,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val fullscreenLease = Any()
    private var activeAd: AppOpenAd? = null
    private var loadedAtMillis = 0L
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
                if (configuration.preload) preload()
            }
        }
    }

    fun preload() {
        if (closed || !AppOpenAdPolicy.shouldLoad(configuration) || !initialized ||
            loading || isAdAvailable()
        ) return

        loading = true
        AppOpenAd.load(
            AdRequest.Builder(configuration.adUnitId).build(),
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    loading = false
                    activeAd = ad
                    loadedAtMillis = nowMillis()
                    analytics.track(AnalyticsEvent("app_open_ad_loaded"))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    analytics.track(
                        AnalyticsEvent(
                            "app_open_ad_load_failed",
                            mapOf("error_code" to error.code.toString())
                        )
                    )
                }
            }
        )
    }

    fun isAdAvailable(): Boolean {
        val available = activeAd != null && AppOpenAdPolicy.isFresh(
            loadedAtMillis = loadedAtMillis,
            nowMillis = nowMillis(),
            maxAgeMillis = configuration.maxAgeMillis
        )
        if (!available && activeAd != null) {
            activeAd = null
            loadedAtMillis = 0L
        }
        return available
    }

    fun showIfReady(
        activity: Activity,
        isColdLaunch: Boolean,
        isBlocked: Boolean,
        isPremium: Boolean,
        onComplete: () -> Unit = {}
    ) {
        if (closed || activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        val adAvailable = isAdAvailable()
        if (!AppOpenAdPolicy.shouldShow(
                configuration = configuration,
                isColdLaunch = isColdLaunch,
                isBlocked = isBlocked,
                isPremium = isPremium,
                isAdAvailable = adAvailable,
                isAnotherFullscreenAdShowing = FullscreenAdCoordinator.isShowing()
            )
        ) {
            if (!adAvailable) preload()
            onComplete()
            return
        }

        if (!FullscreenAdCoordinator.tryAcquire(fullscreenLease)) {
            onComplete()
            return
        }

        val ad = activeAd ?: run {
            FullscreenAdCoordinator.release(fullscreenLease)
            onComplete()
            preload()
            return
        }
        activeAd = null
        loadedAtMillis = 0L
        var completed = false
        fun complete() {
            if (completed) return
            completed = true
            FullscreenAdCoordinator.release(fullscreenLease)
            onComplete()
            preload()
        }

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() = complete()

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                analytics.track(
                    AnalyticsEvent(
                        "app_open_ad_show_failed",
                        mapOf("error_code" to error.code.toString())
                    )
                )
                complete()
            }

            override fun onAdShowedFullScreenContent() = Unit
            override fun onAdClicked() = Unit
            override fun onAdImpression() {
                analytics.track(AnalyticsEvent("app_open_ad_impression"))
            }
        }
        analytics.track(AnalyticsEvent("app_open_ad_started"))
        runCatching { ad.show(activity) }.onFailure { complete() }
    }

    override fun close() {
        closed = true
        activeAd = null
        loadedAtMillis = 0L
        loading = false
        FullscreenAdCoordinator.release(fullscreenLease)
    }
}
