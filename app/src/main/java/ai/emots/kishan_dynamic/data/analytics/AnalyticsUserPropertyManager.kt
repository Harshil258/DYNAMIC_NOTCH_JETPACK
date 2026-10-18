package ai.emots.kishan_dynamic.data.analytics

import android.content.Context
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.EntitlementManager
import ai.emots.kishan_dynamic.data.premium.EntitlementSnapshot
import ai.emots.kishan_dynamic.data.premium.EntitlementSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages reactive synchronization of low-cardinality GA4 user properties.
 * Resolves AUR-P2-029: Ensures user properties are synced on cold start and
 * updated reactively upon entitlement, theme, or language changes.
 *
 * User properties tracked:
 * - entitlement_tier (e.g. "free", "install_trial", "rewarded_access", "weekly", "monthly", "yearly", "lifetime")
 * - is_pro ("true" / "false")
 * - trial_day ("day_1" .. "day_7", "expired", "none")
 * - install_cohort ("yyyy_MM" month of first install)
 * - app_theme ("system", "dark", "light")
 * - app_language ("en", "hi", etc.)
 */
class AnalyticsUserPropertyManager(
    private val context: Context? = null,
    private val analyticsTracker: AnalyticsTracker,
    private val entitlementManager: EntitlementManager? = context?.let { EntitlementManager.getInstance(it) },
    private val preferences: AuroraPreferences? = context?.let { AuroraPreferences(it) },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val installCohortOverride: String? = null
) {
    private var observationJob: Job? = null

    /**
     * Synchronously/immediately sets baseline user properties on cold startup
     * using current snapshot values so initial analytics events have correct segmentation.
     */
    suspend fun syncImmediate() {
        val em = entitlementManager ?: return
        val prefs = preferences ?: return
        val snapshot = em.entitlement.value
        val installCohort = installCohortOverride ?: computeInstallCohort(context)
        val theme = prefs.themeMode.first()
        val language = prefs.languageCode.first()

        applyUserProperties(
            snapshot = snapshot,
            installCohort = installCohort,
            themeMode = theme,
            languageCode = language
        )
    }

    /**
     * Starts continuous reactive observation of entitlement and preferences changes.
     */
    fun startObserving(): Job? {
        val em = entitlementManager ?: return null
        val prefs = preferences ?: return null
        observationJob?.cancel()
        val installCohort = installCohortOverride ?: computeInstallCohort(context)
        val job = scope.launch {
            combine(
                em.entitlement,
                prefs.themeMode,
                prefs.languageCode
            ) { snapshot, theme, language ->
                applyUserProperties(
                    snapshot = snapshot,
                    installCohort = installCohort,
                    themeMode = theme,
                    languageCode = language
                )
            }.collect {}
        }
        observationJob = job
        return job
    }

    fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    fun applyUserProperties(
        snapshot: EntitlementSnapshot,
        installCohort: String,
        themeMode: String,
        languageCode: String
    ) {
        val tier = snapshot.source.serializedName
        val isPro = snapshot.isActive.toString()
        val trialDay = when {
            snapshot.isTrial -> "day_${(8 - snapshot.remainingDays).coerceIn(1, 7)}"
            snapshot.source == EntitlementSource.INSTALL_TRIAL && snapshot.isExpired -> "expired"
            else -> "none"
        }

        analyticsTracker.setUserProperty(AnalyticsUserProperties.ENTITLEMENT_TIER, tier)
        analyticsTracker.setUserProperty(AnalyticsUserProperties.IS_PRO, isPro)
        analyticsTracker.setUserProperty(AnalyticsUserProperties.TRIAL_DAY, trialDay)
        analyticsTracker.setUserProperty(AnalyticsUserProperties.INSTALL_COHORT, installCohort)
        analyticsTracker.setUserProperty(AnalyticsUserProperties.APP_THEME, themeMode)
        analyticsTracker.setUserProperty(AnalyticsUserProperties.APP_LANGUAGE, languageCode)
    }

    companion object {
        fun computeInstallCohort(context: Context?): String {
            if (context == null) return "unknown"
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val installTime = packageInfo.firstInstallTime
                val sdf = SimpleDateFormat("yyyy_MM", Locale.US)
                sdf.format(Date(installTime))
            } catch (e: Throwable) {
                "unknown"
            }
        }
    }
}
