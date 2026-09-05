package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Classifies notifications that deserve the live-activity compact treatment.
 *
 * The reference keeps a single timer, navigation, transport, call, or other
 * ongoing notification in the main capsule and gives it a companion bubble.
 * Keeping that decision in the notification domain prevents the overlay and
 * Compose renderer from drifting apart.
 */
object NotificationPriorityPolicy {

    private val priorityCategories = setOf(
        "navigation",
        "call",
        "transport",
        "alarm",
        "timer",
        "stopwatch"
    )

    fun isPriority(notification: NotificationInfo): Boolean =
        notification.isPriority ||
            notification.isOngoing ||
            notification.category in priorityCategories ||
            notification.title.contains("incoming call", ignoreCase = true)
}
