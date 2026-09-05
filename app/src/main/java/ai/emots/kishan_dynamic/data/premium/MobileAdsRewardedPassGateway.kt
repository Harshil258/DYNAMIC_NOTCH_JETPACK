package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RewardedAdConfiguration(
    val enabled: Boolean,
    val appId: String,
    val adUnitId: String,
    val passHours: Int = RewardedPassPolicy.DEFAULT_HOURS
) {
    companion object {
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        fun disabled(): RewardedAdConfiguration = RewardedAdConfiguration(
            enabled = false,
            appId = "",
            adUnitId = ""
        )

        /** Debug-only Google test placement; release builds stay opt-in. */
        fun forBuild(isDebug: Boolean): RewardedAdConfiguration = if (isDebug) {
            RewardedAdConfiguration(
                enabled = true,
                appId = TEST_APP_ID,
                adUnitId = TEST_REWARDED_ID
            )
        } else {
            disabled()
        }

        fun fromReleaseConfig(
            config: ReleaseRuntimeConfig,
            canRequestAds: Boolean = true
        ): RewardedAdConfiguration {
            val appId = config.mobileAdsAppId.ifBlank { if (config.adsTestMode) TEST_APP_ID else "" }
            val adUnitId = config.rewardedAdUnitId.ifBlank { if (config.adsTestMode) TEST_REWARDED_ID else "" }
            return RewardedAdConfiguration(
                enabled = config.adsEnabled && config.rewardedPassEnabled && canRequestAds &&
                    appId.isNotBlank() && adUnitId.isNotBlank(),
                appId = appId,
                adUnitId = adUnitId,
                passHours = config.rewardedPassHours.coerceIn(1, 168)
            )
        }
    }
}

/**
 * Independent Mobile Ads adapter. A pass is persisted only after the SDK
 * invokes the reward callback, never when the button is pressed or an ad is
 * merely loaded/shown.
 */
class MobileAdsRewardedPassGateway(
    context: Context,
    private val configuration: RewardedAdConfiguration,
    private val analytics: AnalyticsSink = AppAnalytics,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : RewardedPassGateway {
    private val appContext = context.applicationContext
    private val preferences = AuroraPreferences(appContext)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _status = MutableStateFlow<String?>(null)
    override val status: StateFlow<String?> = _status.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    private var loading = false
    private var pendingActivity: Activity? = null
    private var showWhenReady = false
    private var mobileAdsInitialized = false

    init {
        if (configuration.enabled && configuration.appId.isNotBlank() && configuration.adUnitId.isNotBlank()) {
            MobileAds.initialize(
                appContext,
                InitializationConfig.Builder(configuration.appId).build()
            ) {
                mobileAdsInitialized = true
                preload()
            }
        } else {
            report("Rewarded passes are not configured in this build.")
        }
    }

    override fun preload() {
        if (!isConfigured() || loading || rewardedAd != null) return
        loading = true
        RewardedAd.load(
            AdRequest.Builder(configuration.adUnitId).build(),
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    loading = false
                    report("Rewarded pass is ready.")
                    if (showWhenReady) {
                        pendingActivity?.let(::showLoadedAd)
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    showWhenReady = false
                    pendingActivity = null
                    report(error.message.ifBlank { "Rewarded ad could not be loaded." })
                    analytics.track(
                        AnalyticsEvent(
                            "rewarded_ad_load_failed",
                            mapOf("error_code" to error.code.toString())
                        )
                    )
                }
            }
        )
    }

    override fun show(activity: Activity): RewardedPassResult {
        if (!isConfigured()) {
            return RewardedPassResult.Unavailable(
                "Rewarded passes are not configured in this build."
            )
        }
        if (activity.isFinishing || activity.isDestroyed) {
            return RewardedPassResult.Unavailable("Rewarded passes require an active app screen.")
        }
        val ad = rewardedAd
        if (ad == null) {
            pendingActivity = activity
            showWhenReady = true
            preload()
            return RewardedPassResult.Started("Preparing a rewarded pass…")
        }
        showLoadedAd(activity)
        return RewardedPassResult.Started("Opening rewarded ad…")
    }

    override fun close() {
        rewardedAd = null
        pendingActivity = null
        showWhenReady = false
        scope.coroutineContext.cancel()
    }

    private fun showLoadedAd(activity: Activity) {
        val ad = rewardedAd ?: return
        rewardedAd = null
        pendingActivity = null
        showWhenReady = false
        var rewardGranted = false
        analytics.track(AnalyticsEvent("rewarded_ad_started"))
        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                report(
                    if (rewardGranted) {
                        "Rewarded pass active for ${RewardedPassPolicy.durationLabel(configuration.passHours)}."
                    } else {
                        "Rewarded ad dismissed."
                    }
                )
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                report(error.message.ifBlank { "Rewarded ad could not be shown." })
                analytics.track(
                    AnalyticsEvent(
                        "rewarded_ad_show_failed",
                        mapOf("error_code" to error.code.toString())
                    )
                )
                preload()
            }

            override fun onAdShowedFullScreenContent() = Unit
            override fun onAdClicked() = Unit
            override fun onAdImpression() = Unit
        }
        ad.show(activity) { rewardItem: RewardItem ->
            if (rewardGranted) return@show
            rewardGranted = true
            val expiresAt = RewardedPassPolicy.expiresAt(nowMillis(), configuration.passHours)
            if (expiresAt == null) {
                report("Rewarded pass duration is not configured.")
                return@show
            }
            scope.launch {
                preferences.setProEntitlement(source = "rewarded_pass", expiresAt = expiresAt)
                analytics.track(
                    AnalyticsEvent(
                        "rewarded_ad_earned",
                        mapOf("reward_type" to rewardItem.type, "reward_amount" to rewardItem.amount.toString())
                    )
                )
                report(
                    "Rewarded pass active for ${RewardedPassPolicy.durationLabel(configuration.passHours)}."
                )
            }
        }
    }

    private fun isConfigured(): Boolean =
        configuration.enabled &&
            configuration.appId.isNotBlank() &&
            configuration.adUnitId.isNotBlank() &&
            mobileAdsInitialized

    private fun report(message: String) {
        mainHandler.post { _status.value = message }
    }
}
