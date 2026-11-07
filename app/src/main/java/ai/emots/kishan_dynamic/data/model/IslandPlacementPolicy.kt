package ai.emots.kishan_dynamic.data.model

/** Public display-cutout rectangles in screen pixels; no device-name exceptions. */
data class IslandCutout(val left: Int, val top: Int, val right: Int, val bottom: Int)
data class IslandPlacement(val centerOffsetX: Int, val top: Int)

object IslandPlacementPolicy {
    /**
     * Content never occupies physical camera pixels. Expanded controls sit below
     * the status bar; compact content moves below only an intersecting cutout.
     * Calibration is in dp and is converted once at the Android boundary.
     */
    fun resolve(
        screenWidth: Int, screenHeight: Int, bodyWidth: Int, bodyHeight: Int,
        requestedX: Int, requestedY: Int, expanded: Boolean, statusBarBottom: Int,
        edge: Int, clearance: Int, cutouts: List<IslandCutout>
    ): IslandPlacement {
        val width = bodyWidth.coerceIn(0, screenWidth.coerceAtLeast(0))
        val left = ((screenWidth - width) / 2 + requestedX)
            .coerceIn(edge.coerceAtMost((screenWidth - width) / 2).coerceAtLeast(0),
                (screenWidth - width - edge).coerceAtLeast((screenWidth - width) / 2))
        var top = maxOf(requestedY, if (expanded) statusBarBottom + clearance else 0)
        cutouts.sortedBy { it.top }.forEach { hole ->
            if (left < hole.right + clearance && left + width > hole.left - clearance &&
                top < hole.bottom + clearance && top + bodyHeight > hole.top - clearance) {
                top = hole.bottom + clearance
            }
        }
        top = top.coerceIn(0, (screenHeight - bodyHeight).coerceAtLeast(0))
        return IslandPlacement(left + width / 2 - screenWidth / 2, top)
    }
}
