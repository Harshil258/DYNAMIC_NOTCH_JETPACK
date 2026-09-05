package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BannerAdConfiguration(
    val enabled: Boolean,
    val appId: String,
    val adUnitId: String
) {
    companion object {
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

        fun fromReleaseConfig(
            config: ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
        ): BannerAdConfiguration {
            val appId = config.mobileAdsAppId.ifBlank { if (config.adsTestMode) TEST_APP_ID else "" }
            val adUnitId = config.bannerAdUnitId.ifBlank { if (config.adsTestMode) TEST_BANNER_ID else "" }
            return BannerAdConfiguration(
                enabled = config.adsEnabled && appId.isNotBlank() && adUnitId.isNotBlank(),
                appId = appId,
                adUnitId = adUnitId
            )
        }
    }
}

sealed interface BannerAdStatus {
    data object Idle : BannerAdStatus
    data object Loading : BannerAdStatus
    data class Ready(val ad: BannerAd) : BannerAdStatus
    data class Failed(val message: String) : BannerAdStatus
    data object Unavailable : BannerAdStatus
}

/** Lifecycle-owned adapter for adaptive banners. */
class MobileAdsBannerGateway(
    context: Context,
    private val configuration: BannerAdConfiguration,
    private val adSize: AdSize,
    private val analytics: AnalyticsSink = AppAnalytics
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _status = MutableStateFlow<BannerAdStatus>(
        if (configuration.enabled) BannerAdStatus.Idle else BannerAdStatus.Unavailable
    )
    val status: StateFlow<BannerAdStatus> = _status.asStateFlow()

    private var activeAd: BannerAd? = null
    private var loading = false
    private var closed = false
    private var mobileAdsInitialized = false

    init {
        if (configuration.enabled) {
            MobileAds.initialize(
                appContext,
                InitializationConfig.Builder(configuration.appId).build()
            ) {
                mobileAdsInitialized = true
                preload()
            }
        }
    }

    fun preload() {
        if (closed || !configuration.enabled || !mobileAdsInitialized || loading || activeAd != null) return
        loading = true
        _status.value = BannerAdStatus.Loading
        scope.launch {
            when (val result = BannerAd.load(
                BannerAdRequest.Builder(configuration.adUnitId, adSize).build()
            )) {
                is AdLoadResult.Success -> {
                    loading = false
                    activeAd = result.ad
                    _status.value = BannerAdStatus.Ready(result.ad)
                    analytics.track(AnalyticsEvent("banner_ad_loaded"))
                }

                is AdLoadResult.Failure -> {
                    loading = false
                    _status.value = BannerAdStatus.Failed(result.error.message)
                    analytics.track(
                        AnalyticsEvent(
                            "banner_ad_load_failed",
                            mapOf("error_code" to result.error.code.toString())
                        )
                    )
                }
            }
        }
    }

    fun attach(adView: AdView, activity: Activity) {
        activeAd?.let { adView.registerBannerAd(it, activity) }
    }

    override fun close() {
        closed = true
        activeAd?.destroy()
        activeAd = null
        loading = false
        scope.cancel()
    }
}
