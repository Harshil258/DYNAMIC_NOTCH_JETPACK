package ai.emots.kishan_dynamic.data.model

enum class SetupAction { ENABLE_OVERLAY, ENABLE_NOTIFICATIONS, REACTIVATE_OVERLAY, REACTIVATE_NOTIFICATIONS, CONTINUE }

data class SetupReadiness(
    val overlayGranted: Boolean,
    val overlayConnected: Boolean,
    val notificationsGranted: Boolean,
    val notificationsConnected: Boolean
) {
    val action: SetupAction get() = when {
        !overlayGranted -> SetupAction.ENABLE_OVERLAY
        !notificationsGranted -> SetupAction.ENABLE_NOTIFICATIONS
        !overlayConnected -> SetupAction.REACTIVATE_OVERLAY
        !notificationsConnected -> SetupAction.REACTIVATE_NOTIFICATIONS
        else -> SetupAction.CONTINUE
    }
    val ready: Boolean get() = action == SetupAction.CONTINUE
    val workingRequiredCount: Int get() = listOf(
        overlayGranted && overlayConnected, notificationsGranted && notificationsConnected
    ).count { it }
}
