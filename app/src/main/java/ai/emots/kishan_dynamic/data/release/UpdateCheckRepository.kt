package ai.emots.kishan_dynamic.data.release

import android.content.Context

/** Persists update-check cadence and the last optional version the user deferred. */
class UpdateCheckRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "aurora_update_checks",
        Context.MODE_PRIVATE
    )

    fun shouldCheck(nowMillis: Long, intervalHours: Int): Boolean {
        val lastCheckedAt = preferences.getLong(KEY_LAST_CHECKED_AT, 0L)
        val intervalMillis = intervalHours.coerceIn(1, 168).toLong() * 60L * 60L * 1000L
        return lastCheckedAt <= 0L || nowMillis - lastCheckedAt >= intervalMillis
    }

    fun markChecked(nowMillis: Long) {
        preferences.edit().putLong(KEY_LAST_CHECKED_AT, nowMillis).apply()
    }

    fun dismissedVersionCode(): Int = preferences.getInt(KEY_DISMISSED_VERSION, 0)

    fun markDismissed(versionCode: Int) {
        preferences.edit().putInt(KEY_DISMISSED_VERSION, versionCode).apply()
    }

    private companion object {
        const val KEY_LAST_CHECKED_AT = "last_checked_at"
        const val KEY_DISMISSED_VERSION = "dismissed_version"
    }
}
