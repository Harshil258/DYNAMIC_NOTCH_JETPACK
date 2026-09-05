package ai.emots.kishan_dynamic.data.notification

/** Shared notification-display choices used by Settings and the listener. */
object NotificationDisplayDurationPolicy {
    const val DEFAULT_SECONDS = 5
    val options: List<Int> = listOf(2, 3, 5, 8, 10)
    /** Reference-compatible free choices; extended durations require Pro. */
    val freeOptions: Set<Int> = setOf(3, 5)

    fun sanitize(seconds: Int): Int =
        seconds.takeIf { it in options } ?: DEFAULT_SECONDS
}
