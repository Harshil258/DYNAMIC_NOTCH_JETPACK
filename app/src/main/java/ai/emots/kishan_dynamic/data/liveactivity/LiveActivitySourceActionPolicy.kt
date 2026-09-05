package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Chooses one safe source action for a notification-backed live activity.
 * Android PendingIntents remain in the service registry; this policy only
 * returns their stable id for the overlay to dispatch.
 */
object LiveActivitySourceActionPolicy {
    fun primaryActionId(notification: NotificationInfo): String? {
        val semanticAction = notification.actions.firstOrNull { action ->
            action.label.contains("undo", ignoreCase = true) ||
                action.label.contains("revert", ignoreCase = true) ||
                action.label.contains("continue", ignoreCase = true) ||
                action.label.contains("view", ignoreCase = true) ||
                action.label.contains("details", ignoreCase = true)
        }?.id
        return semanticAction ?: "open".takeIf { notification.hasContentIntent }
    }
}
