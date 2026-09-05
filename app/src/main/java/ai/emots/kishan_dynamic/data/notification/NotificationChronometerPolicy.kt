package ai.emots.kishan_dynamic.data.notification

/** Pure time rules shared by notification ingestion and island rendering. */
object NotificationChronometerPolicy {

    fun baseFromWallClock(
        notificationWhenMillis: Long,
        nowWallClockMillis: Long,
        nowElapsedRealtimeMillis: Long,
        enabled: Boolean
    ): Long {
        if (!enabled || notificationWhenMillis <= 0L) return 0L
        return nowElapsedRealtimeMillis - (nowWallClockMillis - notificationWhenMillis)
    }

    fun elapsedSeconds(
        baseElapsedRealtimeMillis: Long,
        nowElapsedRealtimeMillis: Long,
        countDown: Boolean
    ): Long {
        if (baseElapsedRealtimeMillis <= 0L) return 0L
        val delta = if (countDown) {
            baseElapsedRealtimeMillis - nowElapsedRealtimeMillis
        } else {
            nowElapsedRealtimeMillis - baseElapsedRealtimeMillis
        }
        return (delta / 1_000L).coerceAtLeast(0L)
    }

    fun format(seconds: Long): String {
        val safeSeconds = seconds.coerceAtLeast(0L)
        val hours = safeSeconds / 3_600L
        val minutes = (safeSeconds % 3_600L) / 60L
        val remainder = safeSeconds % 60L
        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, remainder)
        } else {
            "%02d:%02d".format(minutes, remainder)
        }
    }
}
