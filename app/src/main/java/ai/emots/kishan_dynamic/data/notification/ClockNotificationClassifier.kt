package ai.emots.kishan_dynamic.data.notification

enum class ClockNotificationKind(val category: String, val fallbackTitle: String) {
    STOPWATCH("stopwatch", "Stopwatch"),
    TIMER("timer", "Timer")
}

/**
 * Identifies Android Clock custom-notification families without binding the
 * domain model to a package name or to another application's implementation.
 */
fun classifyClockNotification(
    packageName: String,
    channelId: String?,
    hasCustomContent: Boolean,
    notificationCategory: String? = null,
    title: String? = null
): ClockNotificationKind? {
    val categoryLower = notificationCategory.orEmpty().lowercase()
    if (categoryLower == "stopwatch") return ClockNotificationKind.STOPWATCH
    if (categoryLower == "alarm" && (channelId?.contains("timer", ignoreCase = true) == true || title?.contains("timer", ignoreCase = true) == true)) {
        return ClockNotificationKind.TIMER
    }

    if (!hasCustomContent && notificationCategory.isNullOrBlank()) return null

    val source = "${packageName.lowercase()} ${channelId.orEmpty().lowercase()} ${categoryLower} ${title.orEmpty().lowercase()}"
    return when {
        "stopwatch" in source -> ClockNotificationKind.STOPWATCH
        "timer" in source -> ClockNotificationKind.TIMER
        else -> null
    }
}
