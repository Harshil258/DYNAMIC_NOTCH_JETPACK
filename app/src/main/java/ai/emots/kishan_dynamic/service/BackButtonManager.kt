package ai.emots.kishan_dynamic.service

import android.os.SystemClock

/**
 * Thread-safe coordinator to temporarily suppress back button handling during
 * critical permission transitions and system settings return.
 */
object BackButtonManager {

    @Volatile
    private var backButtonDisabledUntilElapsedMillis: Long = 0L

    /**
     * Disables back button handling for the specified duration (in milliseconds).
     */
    fun disableBackButtonFor(durationMs: Long = 2000L) {
        backButtonDisabledUntilElapsedMillis = SystemClock.elapsedRealtime() + durationMs
    }

    /**
     * Returns true if the back button is currently suppressed.
     */
    fun isBackButtonDisabled(): Boolean {
        return SystemClock.elapsedRealtime() < backButtonDisabledUntilElapsedMillis
    }

    /**
     * Returns the remaining time in milliseconds during which back button is suppressed.
     */
    fun remainingDisabledTimeMillis(): Long {
        val remaining = backButtonDisabledUntilElapsedMillis - SystemClock.elapsedRealtime()
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Force-enables back button handling immediately.
     */
    fun enableBackButton() {
        backButtonDisabledUntilElapsedMillis = 0L
    }
}
