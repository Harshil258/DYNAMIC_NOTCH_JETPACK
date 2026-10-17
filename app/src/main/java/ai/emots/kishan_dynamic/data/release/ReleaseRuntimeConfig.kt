package ai.emots.kishan_dynamic.data.release

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Safe app-owned defaults for release-controlled switches. A remote provider
 * can replace this snapshot without leaking network/config concerns into UI.
 */
data class ReleaseRuntimeConfig(
    val adsEnabled: Boolean = false,
    val ratingEnabled: Boolean = true,
    val updateEnabled: Boolean = false,
    val latestVersionCode: Int = 0,
    val minimumVersionCode: Int = 0,
    val updateNotes: String = "",
    val updateUrl: String = "",
    val updateCheckIntervalHours: Int = 24,
    val rewardedPassEnabled: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val analyticsEndpoint: String = "",
    val rewardedPassHours: Int = 7 * 24,
    val mobileAdsAppId: String = "",
    val bannerAdUnitId: String = "",
    val nativeAdUnitId: String = "",
    val interstitialAdUnitId: String = "",
    val appOpenAdUnitId: String = "",
    val rewardedAdUnitId: String = "",
    val resumeAdEnabled: Boolean = true,
    val preloadAppOpen: Boolean = true,
    val isSplashInterstitialEnabled: Boolean = false,
    val isBackInterstitialEnabled: Boolean = false,
    val isExitNativeEnabled: Boolean = true,
    val isPreloadInterstitial: Boolean = true,
    val isPreloadNative: Boolean = true,
    val isPreloadBanner: Boolean = true,
    val bigNativePercentage: Int = 100,
    val screenCounterForInterstitial: Int = 4,
    val clickCounterForInterstitial: Int = 30,
    val adsStartFromScreen: Int = 0,
    val adsTestMode: Boolean = false,
    val premiumEnabled: Boolean = true,
    val weeklyPlanId: String = "dynamic_island_weekly",
    val monthlyPlanId: String = "dynamic_island_monthly",
    val yearlyPlanId: String = "dynamic_island_yearly",
    val lifetimePlanId: String = "dynamic_island_lifetime",
    val premiumFeatures: List<String> = listOf(
        "quick_control",
        "theme_settings",
        "sound_settings",
        "call_end_disable",
        "compact_music",
        "notification_duration",
        "display_horizontal_offset"
    ),
    val trustSectionVisible: Boolean = true,
    val trustSectionTitle: String = "Trusted by Thousands",
    val trustStat1Value: String = "1.1K+",
    val trustStat1Label: String = "Happy Users",
    val trustStat2Value: String = "4.9+",
    val trustStat2Label: String = "Ratings",
    val trustStat3Value: String = "30+",
    val trustStat3Label: String = "Reviews",
    val premiumInstallTrialDays: Int = 7,
    val premiumExpiryIslandEnabled: Boolean = true,
    val premiumRewardedAccessEnabled: Boolean = true,
    val premiumRewardDaysPerAd: Int = 1,
    val rewardedAdCooldownSeconds: Int = 15,
    val rewardedAccessMaxVisibleDays: Int = 7,
    val premiumPaywallEnabled: Boolean = true,
    val premiumWeeklyEnabled: Boolean = true,
    val premiumMonthlyEnabled: Boolean = true,
    val premiumYearlyEnabled: Boolean = true,
    val premiumLifetimeEnabled: Boolean = true,
    val callEndPremiumControlEnabled: Boolean = true,
    val premiumFeatureGatesEnabled: Boolean = true,
    val islandIdleAutoHideEnabled: Boolean = true
)

interface ReleaseRuntimeConfigProvider {
    val config: StateFlow<ReleaseRuntimeConfig>
    val lastRefreshError: StateFlow<String?>
    suspend fun refresh()
}

/** Offline default until a release build supplies remote configuration. */
class LocalReleaseRuntimeConfigProvider(
    initial: ReleaseRuntimeConfig = ReleaseRuntimeConfig()
) : ReleaseRuntimeConfigProvider {
    private val snapshot = MutableStateFlow(initial)

    override val config: StateFlow<ReleaseRuntimeConfig> = snapshot
    override val lastRefreshError: StateFlow<String?> = MutableStateFlow(null)

    override suspend fun refresh() = Unit
}

/** One process-wide release snapshot shared by settings and ad navigation. */
object AppReleaseRuntimeConfig {
    @Volatile
    private var sharedProvider: ReleaseRuntimeConfigProvider? = null

    @Synchronized
    fun provider(context: Context, isDebugBuild: Boolean): ReleaseRuntimeConfigProvider {
        sharedProvider?.let { return it }
        val initial = if (isDebugBuild) {
            ReleaseRuntimeConfig(
                adsEnabled = true,
                rewardedPassEnabled = true,
                adsTestMode = true
            )
        } else {
            ReleaseRuntimeConfig()
        }
        return FirebaseRemoteConfigRuntimeProvider(
            context = context.applicationContext,
            isDebug = isDebugBuild,
            initial = initial
        ).also { sharedProvider = it }
    }
}
