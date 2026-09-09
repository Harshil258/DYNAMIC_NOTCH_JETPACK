package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.IslandState

/**
 * Decides how a notification queue mutation may affect the surface the user is
 * currently viewing. Source state may always update; presentation state may not.
 */
enum class NotificationPresentationDecision {
    PRESENT,
    KEEP_QUICK_CONTROLS,
    UPDATE_EXPANDED_NOTIFICATION,
    DEFER
}

object NotificationInterruptionPolicy {

    fun decide(
        current: IslandState,
        incoming: ai.emots.kishan_dynamic.data.model.NotificationInfo? = null
    ): NotificationPresentationDecision {
        val visibleNotification = when (current) {
            is IslandState.Notification -> current.notifications.getOrNull(current.activeIndex)
            is IslandState.NotificationWithMusic -> current.notifications.getOrNull(current.activeIndex)
            else -> null
        }
        if (incoming != null && !isCriticalInterruption(incoming) &&
            visibleNotification?.let(::isCriticalInterruption) == true
        ) {
            return NotificationPresentationDecision.DEFER
        }
        if (incoming != null && isCriticalInterruption(incoming) &&
            current !is IslandState.IncomingCall && current !is IslandState.OngoingCall
        ) {
            return NotificationPresentationDecision.PRESENT
        }
        return when (current) {
        is IslandState.ActionControl -> NotificationPresentationDecision.KEEP_QUICK_CONTROLS
        is IslandState.Notification -> if (current.isExpanded) {
            NotificationPresentationDecision.UPDATE_EXPANDED_NOTIFICATION
        } else {
            NotificationPresentationDecision.PRESENT
        }
        is IslandState.Music -> if (current.isExpanded) {
            NotificationPresentationDecision.DEFER
        } else {
            NotificationPresentationDecision.PRESENT
        }
        is IslandState.NotificationWithMusic,
        is IslandState.Minimal -> NotificationPresentationDecision.PRESENT
        is IslandState.IncomingCall,
        is IslandState.OngoingCall,
        is IslandState.CallSummary,
        is IslandState.LiveActivity,
        is IslandState.Charging,
        is IslandState.RingerMode,
        is IslandState.RingerVolume,
        is IslandState.MediaVolume,
        is IslandState.BluetoothDevice,
        is IslandState.PremiumExpiry,
        is IslandState.Hidden -> NotificationPresentationDecision.DEFER
        }
    }

    fun isCriticalInterruption(notification: ai.emots.kishan_dynamic.data.model.NotificationInfo): Boolean {
        val category = notification.category.lowercase()
        val copy = "${notification.title} ${notification.text}".lowercase()
        return category == "alarm" ||
            category == "emergency" ||
            copy.contains("emergency alert") ||
            copy.contains("timer finished") ||
            copy.contains("timer done")
    }
}
