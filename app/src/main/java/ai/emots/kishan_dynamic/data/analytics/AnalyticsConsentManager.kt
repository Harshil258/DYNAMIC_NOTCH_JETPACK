package ai.emots.kishan_dynamic.data.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Manages Google Consent Mode settings for Firebase Analytics and AdMob advertising.
 * Ensures data collection respects GDPR/CCPA consent choices and privacy preferences.
 */
class AnalyticsConsentManager(context: Context) {

    private val appContext = context.applicationContext
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        runCatching {
            firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)
        }.onFailure { e ->
            Log.w(TAG, "Failed to get FirebaseAnalytics instance: ${e.message}")
        }
    }

    /**
     * Applies user consent preferences across Google Analytics and Google Advertising.
     */
    fun updateConsent(
        analyticsStorageGranted: Boolean,
        adStorageGranted: Boolean,
        adUserDataGranted: Boolean = adStorageGranted,
        adPersonalizationGranted: Boolean = adStorageGranted
    ) {
        val consentMap = mapOf(
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (analyticsStorageGranted) {
                FirebaseAnalytics.ConsentStatus.GRANTED
            } else {
                FirebaseAnalytics.ConsentStatus.DENIED
            },
            FirebaseAnalytics.ConsentType.AD_STORAGE to if (adStorageGranted) {
                FirebaseAnalytics.ConsentStatus.GRANTED
            } else {
                FirebaseAnalytics.ConsentStatus.DENIED
            },
            FirebaseAnalytics.ConsentType.AD_USER_DATA to if (adUserDataGranted) {
                FirebaseAnalytics.ConsentStatus.GRANTED
            } else {
                FirebaseAnalytics.ConsentStatus.DENIED
            },
            FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to if (adPersonalizationGranted) {
                FirebaseAnalytics.ConsentStatus.GRANTED
            } else {
                FirebaseAnalytics.ConsentStatus.DENIED
            }
        )

        runCatching {
            firebaseAnalytics?.setConsent(consentMap)
            Log.d(TAG, "Applied consent settings: $consentMap")
        }.onFailure { e ->
            Log.w(TAG, "Error applying consent: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "AnalyticsConsent"
    }
}
