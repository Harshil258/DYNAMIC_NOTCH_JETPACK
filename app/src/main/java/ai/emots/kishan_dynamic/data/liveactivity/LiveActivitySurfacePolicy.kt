package ai.emots.kishan_dynamic.data.liveactivity

/**
 * Decides when a notification-backed activity owns the visible surface.
 * The notification remains in the queue; this only chooses its richer island
 * presentation until the user explicitly opens notifications.
 */
object LiveActivitySurfacePolicy {
    fun usesSpecializedSurface(
        activeNotificationId: String?,
        specializedNotificationIds: Set<String>
    ): Boolean = activeNotificationId != null && activeNotificationId in specializedNotificationIds
}
