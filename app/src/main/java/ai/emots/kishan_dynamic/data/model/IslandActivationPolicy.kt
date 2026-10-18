package ai.emots.kishan_dynamic.data.model

/**
 * Represents the genuine operational lifecycle and activation state of the Dynamic Island.
 * Distinguishes permissions, runtime service connection, and user preference.
 */
enum class IslandActivationStatus {
    SETUP_REQUIRED,       // Initial setup incomplete (neither core permission granted)
    PERMISSION_NEEDED,    // Accessibility or notification access revoked/missing
    SERVICE_PAUSED,       // Accessibility permission granted in Settings, but Android killed/paused runtime service
    READY_OFF,            // Permissions granted and active, but island switch is OFF
    STARTING,             // Activation in progress
    RUNNING,              // Overlay actively running above apps, island switch is ON
    ERROR                 // Unexpected failure
}

data class IslandActivationSnapshot(
    val status: IslandActivationStatus,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val badgeType: BadgeType,
    val primaryActionText: String,
    val canToggleDirectly: Boolean,
    val isRunning: Boolean
) {
    enum class BadgeType {
        SUCCESS, WARNING, ERROR, NEUTRAL
    }
}

object IslandActivationPolicy {
    fun evaluate(
        overlayGranted: Boolean,
        overlayConnected: Boolean,
        notificationsGranted: Boolean,
        notificationsConnected: Boolean,
        islandEnabled: Boolean,
        isStarting: Boolean = false
    ): IslandActivationSnapshot {
        return when {
            isStarting -> IslandActivationSnapshot(
                status = IslandActivationStatus.STARTING,
                title = "Starting…",
                subtitle = "Connecting Dynamic Island overlay…",
                badgeLabel = "Starting",
                badgeType = IslandActivationSnapshot.BadgeType.NEUTRAL,
                primaryActionText = "Starting…",
                canToggleDirectly = false,
                isRunning = false
            )
            !overlayGranted && !notificationsGranted -> IslandActivationSnapshot(
                status = IslandActivationStatus.SETUP_REQUIRED,
                title = "Setup required",
                subtitle = "Setup required to display the island above your apps.",
                badgeLabel = "Setup required",
                badgeType = IslandActivationSnapshot.BadgeType.WARNING,
                primaryActionText = "Set up Dynamic Island",
                canToggleDirectly = false,
                isRunning = false
            )
            !overlayGranted || !notificationsGranted -> IslandActivationSnapshot(
                status = IslandActivationStatus.PERMISSION_NEEDED,
                title = "Permission needed",
                subtitle = if (!overlayGranted) {
                    "Accessibility access is needed to show the island."
                } else {
                    "Notification access is needed to show incoming alerts."
                },
                badgeLabel = "Action needed",
                badgeType = IslandActivationSnapshot.BadgeType.WARNING,
                primaryActionText = "Grant required access",
                canToggleDirectly = false,
                isRunning = false
            )
            !islandEnabled -> IslandActivationSnapshot(
                status = IslandActivationStatus.READY_OFF,
                title = "Island is off",
                subtitle = "All permissions ready. Island is turned off.",
                badgeLabel = "Off",
                badgeType = IslandActivationSnapshot.BadgeType.NEUTRAL,
                primaryActionText = "Turn on Island",
                canToggleDirectly = true,
                isRunning = false
            )
            !overlayConnected -> IslandActivationSnapshot(
                status = IslandActivationStatus.SERVICE_PAUSED,
                title = "Island paused",
                subtitle = "Android has paused the required service in the background.",
                badgeLabel = "Paused",
                badgeType = IslandActivationSnapshot.BadgeType.ERROR,
                primaryActionText = "Reactivate Island",
                canToggleDirectly = false,
                isRunning = false
            )
            else -> IslandActivationSnapshot(
                status = IslandActivationStatus.RUNNING,
                title = "Island is on",
                subtitle = "Running above your apps and ready for interactions.",
                badgeLabel = "Running",
                badgeType = IslandActivationSnapshot.BadgeType.SUCCESS,
                primaryActionText = "Turn off Island",
                canToggleDirectly = true,
                isRunning = true
            )
        }
    }
}
