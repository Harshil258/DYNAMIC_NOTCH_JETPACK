package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/** Pure interpretation of call-style notification copy for call-state sync. */
object CallNotificationPolicy {

    fun isCall(notification: NotificationInfo): Boolean =
        notification.category.equals("call", ignoreCase = true) ||
            notification.category.equals("msg_call", ignoreCase = true) ||
            notification.template.contains("CallStyle", ignoreCase = true) ||
            notification.title.contains("incoming call", ignoreCase = true) ||
            notification.title.contains("ongoing call", ignoreCase = true)

    fun isIncoming(notification: NotificationInfo): Boolean {
        if (!isCall(notification)) return false
        return notification.actions.any { action ->
            action.label.contains("answer", ignoreCase = true) ||
                action.label.contains("accept", ignoreCase = true)
        } || notification.title.contains("incoming", ignoreCase = true)
    }

    fun incomingActionIds(notification: NotificationInfo): Pair<String?, String?> {
        if (!isIncoming(notification)) return null to null
        val accept = notification.actions.firstOrNull { action ->
            action.label.contains("answer", ignoreCase = true) ||
                action.label.contains("accept", ignoreCase = true)
        }?.id
        val decline = notification.actions.firstOrNull { action ->
            action.label.contains("decline", ignoreCase = true) ||
                action.label.contains("reject", ignoreCase = true) ||
                action.label.contains("dismiss", ignoreCase = true)
        }?.id
        return accept to decline
    }

    fun isOngoing(notification: NotificationInfo): Boolean =
        isCall(notification) && !isIncoming(notification) &&
            (ongoingActionId(notification) != null ||
                listOf(notification.title, notification.text, notification.subText.orEmpty())
                    .any { copy ->
                        copy.contains("ongoing", ignoreCase = true) ||
                            copy.contains("connected", ignoreCase = true) ||
                            copy.contains("call in progress", ignoreCase = true)
                    })

    fun ongoingActionId(notification: NotificationInfo): String? {
        if (!isCall(notification) || isIncoming(notification)) return null
        return notification.actions.firstOrNull { action ->
            action.label.contains("hang up", ignoreCase = true) ||
                action.label.contains("end call", ignoreCase = true) ||
                action.label.contains("end", ignoreCase = true) ||
                action.label.contains("terminate", ignoreCase = true)
        }?.id
    }

    fun isDialing(notification: NotificationInfo): Boolean {
        if (!isCall(notification)) return false
        val copy = listOf(notification.title, notification.text, notification.subText.orEmpty())
            .joinToString(" ")
        return dialingWords.any { copy.contains(it, ignoreCase = true) }
    }

    private val dialingWords = setOf("calling", "dialing", "ringing")
}
