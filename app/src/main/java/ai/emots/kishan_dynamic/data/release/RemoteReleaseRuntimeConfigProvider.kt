package ai.emots.kishan_dynamic.data.release

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Small injectable boundary so transport can be tested without a network call. */
fun interface ReleaseConfigFetcher {
    suspend fun fetch(endpoint: String): String
}

/** HTTPS/HTTP transport with bounded timeouts and no retry loop in the UI. */
class UrlConnectionReleaseConfigFetcher(
    private val connectTimeoutMillis: Int = 4_000,
    private val readTimeoutMillis: Int = 4_000
) : ReleaseConfigFetcher {
    override suspend fun fetch(endpoint: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("Release config request failed: HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Decodes only the app-owned release switches. Unknown fields are ignored so
 * the endpoint can evolve without coupling UI code to a backend schema.
 */
object ReleaseRuntimeConfigDecoder {
    private val jsonField = Regex(
        "\"([^\"]+)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|true|false|-?\\d+)"
    )

    fun decode(payload: String, fallback: ReleaseRuntimeConfig = ReleaseRuntimeConfig()): ReleaseRuntimeConfig {
        require(payload.trim().startsWith("{")) { "Release config must be a JSON object." }
        val fields = jsonField.findAll(payload).associate { match ->
            match.groupValues[1] to match.groupValues[2]
        }

        fun raw(vararg keys: String): String? = keys.firstNotNullOfOrNull(fields::get)
        fun booleanValue(default: Boolean, vararg keys: String): Boolean =
            raw(*keys)?.toBooleanStrictOrNull() ?: default
        fun stringValue(default: String, vararg keys: String): String =
            raw(*keys)?.removeSurrounding("\"")?.unescapeJson() ?: default
        fun intValue(default: Int, vararg keys: String): Int =
            raw(*keys)?.toIntOrNull() ?: default

        val featuresRegex = Regex("\"(?:features|premiumFeatures|premium_features)\"\\s*:\\s*\\[([^\\]]*)\\]")
        val featuresList = featuresRegex.find(payload)?.groupValues?.get(1)?.let { rawFeatures ->
            Regex("\"([^\"]+)\"").findAll(rawFeatures).map { it.groupValues[1] }.toList()
        } ?: fallback.premiumFeatures

        return ReleaseRuntimeConfig(
            adsEnabled = booleanValue(fallback.adsEnabled, "adsEnabled", "ads_enabled", "isAdsEnabled"),
            ratingEnabled = booleanValue(fallback.ratingEnabled, "ratingEnabled", "rating_enabled", "isRatingDialogEnabled"),
            updateEnabled = booleanValue(fallback.updateEnabled, "updateEnabled", "update_enabled"),
            latestVersionCode = intValue(
                fallback.latestVersionCode,
                "latestVersionCode",
                "latest_version_code"
            ).coerceAtLeast(0),
            minimumVersionCode = intValue(
                fallback.minimumVersionCode,
                "minimumVersionCode",
                "minimum_version_code"
            ).coerceAtLeast(0),
            updateNotes = stringValue(fallback.updateNotes, "updateNotes", "update_notes"),
            updateUrl = stringValue(fallback.updateUrl, "updateUrl", "update_url"),
            updateCheckIntervalHours = intValue(
                fallback.updateCheckIntervalHours,
                "updateCheckIntervalHours",
                "update_check_interval_hours"
            ).coerceIn(1, 168),
            rewardedPassEnabled = booleanValue(
                fallback.rewardedPassEnabled,
                "rewardedPassEnabled",
                "rewarded_pass_enabled"
            ),
            analyticsEnabled = booleanValue(fallback.analyticsEnabled, "analyticsEnabled", "analytics_enabled"),
            analyticsEndpoint = stringValue(
                fallback.analyticsEndpoint,
                "analyticsEndpoint",
                "analytics_endpoint"
            ),
            rewardedPassHours = intValue(
                fallback.rewardedPassHours,
                "rewardedPassHours",
                "rewarded_pass_hours"
            ).coerceIn(1, 168),
            mobileAdsAppId = stringValue(fallback.mobileAdsAppId, "mobileAdsAppId", "mobile_ads_app_id"),
            bannerAdUnitId = stringValue(fallback.bannerAdUnitId, "bannerAdUnitId", "banner_ad_unit_id", "admobBannerId"),
            nativeAdUnitId = stringValue(fallback.nativeAdUnitId, "nativeAdUnitId", "native_ad_unit_id", "admobNativeId"),
            interstitialAdUnitId = stringValue(
                fallback.interstitialAdUnitId,
                "interstitialAdUnitId",
                "interstitial_ad_unit_id",
                "admobInterstitialId",
                "splashInterstitialId"
            ),
            appOpenAdUnitId = stringValue(
                fallback.appOpenAdUnitId,
                "appOpenAdUnitId",
                "app_open_ad_unit_id",
                "admobAppOpenId"
            ),
            rewardedAdUnitId = stringValue(fallback.rewardedAdUnitId, "rewardedAdUnitId", "rewarded_ad_unit_id", "admobRewardedId"),
            resumeAdEnabled = booleanValue(
                fallback.resumeAdEnabled,
                "resumeAdEnabled",
                "resume_ad_enabled",
                "isResumeAdEnabled"
            ),
            preloadAppOpen = booleanValue(
                fallback.preloadAppOpen,
                "preloadAppOpen",
                "preload_app_open",
                "isPreloadAppOpen"
            ),
            isSplashInterstitialEnabled = booleanValue(
                fallback.isSplashInterstitialEnabled,
                "isSplashInterstitialEnabled",
                "is_splash_interstitial_enabled"
            ),
            isBackInterstitialEnabled = booleanValue(
                fallback.isBackInterstitialEnabled,
                "isBackInterstitialEnabled",
                "is_back_interstitial_enabled"
            ),
            isExitNativeEnabled = booleanValue(
                fallback.isExitNativeEnabled,
                "isExitNativeEnabled",
                "is_exit_native_enabled"
            ),
            isPreloadInterstitial = booleanValue(
                fallback.isPreloadInterstitial,
                "isPreloadInterstitial",
                "is_preload_interstitial"
            ),
            isPreloadNative = booleanValue(
                fallback.isPreloadNative,
                "isPreloadNative",
                "is_preload_native"
            ),
            isPreloadBanner = booleanValue(
                fallback.isPreloadBanner,
                "isPreloadBanner",
                "is_preload_banner"
            ),
            bigNativePercentage = intValue(
                fallback.bigNativePercentage,
                "bigNativePercentage",
                "big_native_percentage"
            ).coerceIn(0, 100),
            screenCounterForInterstitial = intValue(
                fallback.screenCounterForInterstitial,
                "screenCounterForInterstitial",
                "screen_counter_for_interstitial"
            ).coerceIn(0, 1000),
            clickCounterForInterstitial = intValue(
                fallback.clickCounterForInterstitial,
                "clickCounterForInterstitial",
                "click_counter_for_interstitial"
            ).coerceIn(0, 1000),
            adsStartFromScreen = intValue(
                fallback.adsStartFromScreen,
                "adsStartFromScreen",
                "ads_start_from_screen"
            ).coerceAtLeast(0),
            adsTestMode = booleanValue(fallback.adsTestMode, "adsTestMode", "ads_test_mode", "isTestModeEnabled"),
            premiumEnabled = booleanValue(fallback.premiumEnabled, "premiumEnabled", "premium_enabled", "isPremiumEnabled"),
            weeklyPlanId = stringValue(fallback.weeklyPlanId, "weeklyPlanId", "weekly_plan_id"),
            monthlyPlanId = stringValue(fallback.monthlyPlanId, "monthlyPlanId", "monthly_plan_id"),
            yearlyPlanId = stringValue(fallback.yearlyPlanId, "yearlyPlanId", "yearly_plan_id"),
            lifetimePlanId = stringValue(fallback.lifetimePlanId, "lifetimePlanId", "lifetime_plan_id"),
            premiumFeatures = featuresList,
            trustSectionVisible = booleanValue(fallback.trustSectionVisible, "trustSectionVisible", "isTrustSectionVisible", "trust_section_visible", "isVisible"),
            trustSectionTitle = stringValue(fallback.trustSectionTitle, "trustSectionTitle", "title"),
            trustStat1Value = stringValue(fallback.trustStat1Value, "trustStat1Value", "stat1Value"),
            trustStat1Label = stringValue(fallback.trustStat1Label, "trustStat1Label", "stat1Label"),
            trustStat2Value = stringValue(fallback.trustStat2Value, "trustStat2Value", "stat2Value"),
            trustStat2Label = stringValue(fallback.trustStat2Label, "trustStat2Label", "stat2Label"),
            trustStat3Value = stringValue(fallback.trustStat3Value, "trustStat3Value", "stat3Value"),
            trustStat3Label = stringValue(fallback.trustStat3Label, "trustStat3Label", "stat3Label"),
            premiumInstallTrialDays = intValue(
                fallback.premiumInstallTrialDays,
                "premiumInstallTrialDays",
                "premium_install_trial_days",
                "trial_duration_days",
                "trialDurationDays",
                "rewardAdFreeDays",
                "reward_ad_free_days"
            ).coerceIn(0, 365),
            premiumExpiryIslandEnabled = booleanValue(fallback.premiumExpiryIslandEnabled, "premiumExpiryIslandEnabled", "premium_expiry_island_enabled"),
            premiumRewardedAccessEnabled = booleanValue(fallback.premiumRewardedAccessEnabled, "premiumRewardedAccessEnabled", "premium_rewarded_access_enabled"),
            premiumRewardDaysPerAd = intValue(
                fallback.premiumRewardDaysPerAd,
                "premiumRewardDaysPerAd",
                "premium_reward_days_per_ad",
                "rewardAdFreeDays",
                "reward_ad_free_days"
            ).coerceIn(1, 30),
            rewardedAdCooldownSeconds = intValue(fallback.rewardedAdCooldownSeconds, "rewardedAdCooldownSeconds", "rewarded_ad_cooldown_seconds").coerceIn(0, 300),
            rewardedAccessMaxVisibleDays = intValue(fallback.rewardedAccessMaxVisibleDays, "rewardedAccessMaxVisibleDays", "rewarded_access_max_visible_days").coerceIn(1, 30),
            premiumPaywallEnabled = booleanValue(fallback.premiumPaywallEnabled, "premiumPaywallEnabled", "premium_paywall_enabled"),
            premiumWeeklyEnabled = booleanValue(fallback.premiumWeeklyEnabled, "premiumWeeklyEnabled", "premium_weekly_enabled"),
            premiumMonthlyEnabled = booleanValue(fallback.premiumMonthlyEnabled, "premiumMonthlyEnabled", "premium_monthly_enabled"),
            premiumYearlyEnabled = booleanValue(fallback.premiumYearlyEnabled, "premiumYearlyEnabled", "premium_yearly_enabled"),
            premiumLifetimeEnabled = booleanValue(fallback.premiumLifetimeEnabled, "premiumLifetimeEnabled", "premium_lifetime_enabled"),
            callEndPremiumControlEnabled = booleanValue(fallback.callEndPremiumControlEnabled, "callEndPremiumControlEnabled", "call_end_premium_control_enabled"),
            premiumFeatureGatesEnabled = booleanValue(fallback.premiumFeatureGatesEnabled, "premiumFeatureGatesEnabled", "premium_feature_gates_enabled"),
            islandIdleAutoHideEnabled = booleanValue(fallback.islandIdleAutoHideEnabled, "islandIdleAutoHideEnabled", "island_idle_auto_hide_enabled")
        )
    }

    private fun String.unescapeJson(): String = replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
}

/**
 * App-owned remote provider. A blank endpoint is intentionally a no-op, so a
 * build without release infrastructure remains deterministic and offline-safe.
 */
class RemoteReleaseRuntimeConfigProvider(
    private val endpoint: String,
    initial: ReleaseRuntimeConfig = ReleaseRuntimeConfig(),
    private val fetcher: ReleaseConfigFetcher = UrlConnectionReleaseConfigFetcher()
) : ReleaseRuntimeConfigProvider {
    private val snapshot = MutableStateFlow(initial)
    private val _lastRefreshError = MutableStateFlow<String?>(null)

    override val config: StateFlow<ReleaseRuntimeConfig> = snapshot.asStateFlow()
    override val lastRefreshError: StateFlow<String?> = _lastRefreshError.asStateFlow()

    override suspend fun refresh() {
        if (endpoint.isBlank()) return

        runCatching {
            ReleaseRuntimeConfigDecoder.decode(fetcher.fetch(endpoint), snapshot.value)
        }.onSuccess { next ->
            snapshot.value = next
            _lastRefreshError.value = null
        }.onFailure { error ->
            _lastRefreshError.value = error.message ?: "Release config refresh failed."
        }
    }
}
