package ai.emots.kishan_dynamic.data.release

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Production-grade Firebase Remote Config provider matching reference project architecture.
 * Fetches "adsdetailsnew" and "premium_config" parameters with strict 6-second timeout,
 * local SharedPreferences cache fallback, and safe offline defaults.
 */
class FirebaseRemoteConfigRuntimeProvider(
    private val context: Context,
    private val isDebug: Boolean,
    initial: ReleaseRuntimeConfig = ReleaseRuntimeConfig(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReleaseRuntimeConfigProvider {

    companion object {
        private const val TAG = "RemoteConfigManager"
        const val PARAM_ADS_DETAILS = "adsdetailsnew"
        const val PARAM_PREMIUM_CONFIG = "premium_config"
        private const val PREFS_CACHE = "aurora_remote_config_cache"
        private const val KEY_CACHED_ADS_JSON = "cached_ads_details_json"
        private const val KEY_LAST_FETCH_TIME = "last_fetch_timestamp"
        private const val FETCH_TIMEOUT_MS = 6_000L
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)

    private val snapshot = MutableStateFlow(loadCachedOrDefault(initial))
    override val config: StateFlow<ReleaseRuntimeConfig> = snapshot.asStateFlow()

    private val _lastRefreshError = MutableStateFlow<String?>(null)
    override val lastRefreshError: StateFlow<String?> = _lastRefreshError.asStateFlow()

    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.w(TAG, "FirebaseApp is not initialized; Remote Config disabled")
                null
            } else {
                val instance = FirebaseRemoteConfig.getInstance()
                val settings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (isDebug) 0L else 3600L)
                    .setFetchTimeoutInSeconds(6L)
                    .build()
                instance.setConfigSettingsAsync(settings)
                instance
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize FirebaseRemoteConfig: ${e.message}", e)
            null
        }
    }

    override suspend fun refresh(): Unit = withContext(dispatcher) {
        val rc = remoteConfig ?: run {
            _lastRefreshError.value = "Firebase Remote Config is not initialized"
            return@withContext
        }

        try {
            val fetchSuccess = withTimeoutOrNull(FETCH_TIMEOUT_MS) {
                rc.fetchAndActivate().await()
            }

            if (fetchSuccess == true || fetchSuccess == false) {
                // Fetch completed (either new values activated or already up to date)
                val adsDetailsJson = rc.getString(PARAM_ADS_DETAILS)
                val premiumConfigJson = rc.getString(PARAM_PREMIUM_CONFIG)

                var current = snapshot.value

                if (adsDetailsJson.isNotBlank()) {
                    runCatching {
                        ReleaseRuntimeConfigDecoder.decode(adsDetailsJson, current)
                    }.onSuccess { decoded ->
                        current = decoded
                        saveCachedJson(adsDetailsJson)
                    }.onFailure { error ->
                        Log.e(TAG, "Error decoding adsdetailsnew JSON", error)
                    }
                }

                if (premiumConfigJson.isNotBlank()) {
                    runCatching {
                        ReleaseRuntimeConfigDecoder.decode(premiumConfigJson, current)
                    }.onSuccess { decoded ->
                        current = decoded
                    }.onFailure { error ->
                        Log.e(TAG, "Error decoding premium_config JSON", error)
                    }
                }

                snapshot.value = current
                _lastRefreshError.value = null
                Log.d(TAG, "Remote config successfully fetched and activated: adsEnabled=${current.adsEnabled}")
            } else {
                // Timed out after 6 seconds
                Log.w(TAG, "Remote config fetch timed out after ${FETCH_TIMEOUT_MS}ms, retaining cache")
                _lastRefreshError.value = "Remote config fetch timed out"
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Remote config fetch failed: ${e.message}", e)
            _lastRefreshError.value = e.message ?: "Remote config fetch failed"
            // Revert to cached or current snapshot
            snapshot.value = loadCachedOrDefault(snapshot.value)
        }
    }

    private fun loadCachedOrDefault(fallback: ReleaseRuntimeConfig): ReleaseRuntimeConfig {
        val cached = preferences.getString(KEY_CACHED_ADS_JSON, null)
        if (!cached.isNullOrBlank()) {
            runCatching {
                ReleaseRuntimeConfigDecoder.decode(cached, fallback)
            }.onSuccess { decoded ->
                Log.d(TAG, "Loaded Remote Config from local persistent cache")
                return decoded
            }.onFailure {
                Log.w(TAG, "Failed to decode cached Remote Config JSON, using fallback defaults", it)
            }
        }
        return fallback
    }

    private fun saveCachedJson(json: String) {
        preferences.edit()
            .putString(KEY_CACHED_ADS_JSON, json)
            .putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
            .apply()
    }
}
