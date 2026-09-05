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

        return ReleaseRuntimeConfig(
            adsEnabled = booleanValue(fallback.adsEnabled, "adsEnabled", "ads_enabled"),
            ratingEnabled = booleanValue(fallback.ratingEnabled, "ratingEnabled", "rating_enabled"),
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
            bannerAdUnitId = stringValue(fallback.bannerAdUnitId, "bannerAdUnitId", "banner_ad_unit_id"),
            nativeAdUnitId = stringValue(fallback.nativeAdUnitId, "nativeAdUnitId", "native_ad_unit_id"),
            interstitialAdUnitId = stringValue(
                fallback.interstitialAdUnitId,
                "interstitialAdUnitId",
                "interstitial_ad_unit_id"
            ),
            appOpenAdUnitId = stringValue(
                fallback.appOpenAdUnitId,
                "appOpenAdUnitId",
                "app_open_ad_unit_id"
            ),
            rewardedAdUnitId = stringValue(fallback.rewardedAdUnitId, "rewardedAdUnitId", "rewarded_ad_unit_id"),
            resumeAdEnabled = booleanValue(
                fallback.resumeAdEnabled,
                "resumeAdEnabled",
                "resume_ad_enabled"
            ),
            preloadAppOpen = booleanValue(
                fallback.preloadAppOpen,
                "preloadAppOpen",
                "preload_app_open"
            ),
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
            adsTestMode = booleanValue(fallback.adsTestMode, "adsTestMode", "ads_test_mode")
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
    val lastRefreshError: StateFlow<String?> = _lastRefreshError.asStateFlow()

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
