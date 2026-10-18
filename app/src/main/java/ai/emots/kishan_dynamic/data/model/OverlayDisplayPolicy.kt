package ai.emots.kishan_dynamic.data.model

/**
 * Pure visibility contract for the overlay service.
 *
 * Keeping this decision outside Compose makes lock-screen and display-setting
 * behavior deterministic and independently testable.
 */
data class OverlayDisplayPolicy(
    val islandEnabled: Boolean,
    val alwaysOnTop: Boolean,
    val showOnLockScreen: Boolean,
    val deviceLocked: Boolean,
    val systemSurfaceActive: Boolean = false
) {
    fun shouldRender(
        isHiddenState: Boolean,
        isIdleState: Boolean = false
    ): Boolean {
        if (!islandEnabled || systemSurfaceActive) return false
        if (deviceLocked && !showOnLockScreen) return false
        if (isHiddenState) return false
        if (!alwaysOnTop && isIdleState) return false
        return true
    }
}
