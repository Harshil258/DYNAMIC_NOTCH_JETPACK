package ai.emots.kishan_dynamic.data.premium

import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NativeAdConfiguration(
    val enabled: Boolean,
    val appId: String,
    val adUnitId: String
) {
    companion object {
        const val TEST_APP_ID = BannerAdConfiguration.TEST_APP_ID
        const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

        fun fromReleaseConfig(
            config: ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
        ): NativeAdConfiguration {
            val appId = config.mobileAdsAppId.ifBlank { if (config.adsTestMode) TEST_APP_ID else "" }
            val adUnitId = config.nativeAdUnitId.ifBlank { if (config.adsTestMode) TEST_NATIVE_ID else "" }
            return NativeAdConfiguration(
                enabled = config.adsEnabled && appId.isNotBlank() && adUnitId.isNotBlank(),
                appId = appId,
                adUnitId = adUnitId
            )
        }
    }
}

sealed interface NativeAdStatus {
    data object Idle : NativeAdStatus
    data object Loading : NativeAdStatus
    data class Ready(val ad: NativeAd) : NativeAdStatus
    data class Failed(val message: String) : NativeAdStatus
    data object Unavailable : NativeAdStatus
}

/** Lifecycle-owned native-ad loader; it never decides placement or premium state. */
class MobileAdsNativeAdGateway(
    context: Context,
    private val configuration: NativeAdConfiguration,
    private val analytics: AnalyticsSink = AppAnalytics
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val _status = MutableStateFlow<NativeAdStatus>(
        if (configuration.enabled) NativeAdStatus.Idle else NativeAdStatus.Unavailable
    )
    val status: StateFlow<NativeAdStatus> = _status.asStateFlow()

    private var activeAd: NativeAd? = null
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
        _status.value = NativeAdStatus.Loading
        val request = NativeAdRequest.Builder(
            configuration.adUnitId,
            listOf(NativeAd.NativeAdType.NATIVE)
        ).build()
        NativeAdLoader.load(request, object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(ad: NativeAd) {
                loading = false
                activeAd = ad
                _status.value = NativeAdStatus.Ready(ad)
                analytics.track(AnalyticsEvent("native_ad_loaded"))
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loading = false
                _status.value = NativeAdStatus.Failed(error.message)
                analytics.track(
                    AnalyticsEvent(
                        "native_ad_load_failed",
                        mapOf("error_code" to error.code.toString())
                    )
                )
            }
        })
    }

    override fun close() {
        closed = true
        activeAd?.destroy()
        activeAd = null
        loading = false
    }
}
