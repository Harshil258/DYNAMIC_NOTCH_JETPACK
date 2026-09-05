package ai.emots.kishan_dynamic.data.review

import android.content.Context

data class RatingPromptState(
    val appOpenCount: Int,
    val hasRated: Boolean,
    val dismissalCount: Int,
    val lastDismissedAtMillis: Long
)

/** Persists review cadence independently from the UI and billing state. */
class RatingPromptRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "aurora_rating_prompt",
        Context.MODE_PRIVATE
    )
    private var appOpenRecordedForProcess = false

    @Synchronized
    fun recordAppOpenOnce(): Int {
        if (appOpenRecordedForProcess) return preferences.getInt(KEY_APP_OPEN_COUNT, 0)
        val nextCount = preferences.getInt(KEY_APP_OPEN_COUNT, 0) + 1
        preferences.edit().putInt(KEY_APP_OPEN_COUNT, nextCount).apply()
        appOpenRecordedForProcess = true
        return nextCount
    }

    fun state(): RatingPromptState = RatingPromptState(
        appOpenCount = preferences.getInt(KEY_APP_OPEN_COUNT, 0),
        hasRated = preferences.getBoolean(KEY_HAS_RATED, false),
        dismissalCount = preferences.getInt(KEY_DISMISSAL_COUNT, 0),
        lastDismissedAtMillis = preferences.getLong(KEY_LAST_DISMISSED_AT, 0L)
    )

    fun shouldPrompt(nowMillis: Long = System.currentTimeMillis(), enabled: Boolean = true): Boolean {
        val current = state()
        return RatingPromptPolicy.shouldPrompt(
            appOpenCount = current.appOpenCount,
            hasRated = current.hasRated,
            dismissalCount = current.dismissalCount,
            lastDismissedAtMillis = current.lastDismissedAtMillis,
            nowMillis = nowMillis,
            enabled = enabled
        )
    }

    fun markRated() {
        preferences.edit().putBoolean(KEY_HAS_RATED, true).apply()
    }

    fun recordDismissed(nowMillis: Long = System.currentTimeMillis()) {
        val nextDismissalCount = preferences.getInt(KEY_DISMISSAL_COUNT, 0) + 1
        preferences.edit()
            .putInt(KEY_DISMISSAL_COUNT, nextDismissalCount)
            .putLong(KEY_LAST_DISMISSED_AT, nowMillis)
            .apply()
    }

    private companion object {
        const val KEY_APP_OPEN_COUNT = "app_open_count"
        const val KEY_HAS_RATED = "has_rated"
        const val KEY_DISMISSAL_COUNT = "dismissal_count"
        const val KEY_LAST_DISMISSED_AT = "last_dismissed_at"
    }
}
