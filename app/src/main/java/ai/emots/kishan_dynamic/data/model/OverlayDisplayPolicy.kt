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
    val deviceLocked: Boolean
) {
    fun shouldRender(isHiddenState: Boolean): Boolean =
        islandEnabled && alwaysOnTop &&
            (showOnLockScreen || !deviceLocked) &&
            !isHiddenState
}
