package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

object NotificationReplyLifecyclePolicy {
    fun canKeepEditor(notificationId: String?, actionId: String?, sources: List<NotificationInfo>): Boolean =
        notificationId != null && actionId != null && sources.firstOrNull { it.id == notificationId }
            ?.actions?.any { it.id == actionId && it.isReply } == true
}
