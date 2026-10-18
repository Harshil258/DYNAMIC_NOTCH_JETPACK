package ai.emots.kishan_dynamic.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Production Firebase Analytics / GA4 implementation of AnalyticsTracker.
 * Safely converts Kotlin parameter maps to Android Bundles and forwards to Firebase SDK.
 */
class FirebaseAnalyticsTracker(
    context: Context,
    private val debugLogger: AnalyticsDebugLogger = DefaultAnalyticsDebugLogger
) : AnalyticsTracker {

    private val appContext = context.applicationContext
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        runCatching {
            firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)
        }.onFailure { e ->
            Log.w(TAG, "Failed to initialize FirebaseAnalytics: ${e.message}")
        }
    }

    override fun logEvent(name: String, parameters: Map<String, Any>) {
        val sanitized = AnalyticsSchemaValidator.validateAndSanitize(name, parameters, throwOnError = false)
        debugLogger.logEvent(name, sanitized)
        val bundle = sanitized.toBundle()
        runCatching {
            firebaseAnalytics?.logEvent(name, bundle)
        }.onFailure { e ->
            Log.w(TAG, "Error logging event $name: ${e.message}")
        }
    }

    override fun logScreen(screenName: String, screenClass: String) {
        debugLogger.logScreen(screenName, screenClass)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        runCatching {
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }.onFailure { e ->
            Log.w(TAG, "Error logging screen $screenName: ${e.message}")
        }
    }

    override fun setUserProperty(name: String, value: String) {
        debugLogger.logUserProperty(name, value)
        runCatching {
            firebaseAnalytics?.setUserProperty(name, value)
        }.onFailure { e ->
            Log.w(TAG, "Error setting user property $name: ${e.message}")
        }
    }

    override fun setUserId(userId: String?) {
        runCatching {
            firebaseAnalytics?.setUserId(userId)
        }.onFailure { e ->
            Log.w(TAG, "Error setting user ID: ${e.message}")
        }
    }

    private fun Map<String, Any>.toBundle(): Bundle {
        val bundle = Bundle()
        for ((key, value) in this) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putDouble(key, value.toDouble())
                is Boolean -> bundle.putBoolean(key, value)
                is Bundle -> bundle.putBundle(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        return bundle
    }

    companion object {
        private const val TAG = "AnalyticsTracker"
    }
}

/**
 * Debug logging contract for analytics visibility.
 */
interface AnalyticsDebugLogger {
    fun logEvent(name: String, parameters: Map<String, Any>)
    fun logScreen(screenName: String, screenClass: String)
    fun logUserProperty(name: String, value: String)
}

object DefaultAnalyticsDebugLogger : AnalyticsDebugLogger {
    private const val TAG = "AnalyticsDebug"

    override fun logEvent(name: String, parameters: Map<String, Any>) {
        Log.d(TAG, "[Event] $name params=$parameters")
    }

    override fun logScreen(screenName: String, screenClass: String) {
        Log.d(TAG, "[Screen] $screenName class=$screenClass")
    }

    override fun logUserProperty(name: String, value: String) {
        Log.d(TAG, "[UserProp] $name = $value")
    }
}
