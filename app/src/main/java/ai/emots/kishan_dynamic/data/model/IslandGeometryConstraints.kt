package ai.emots.kishan_dynamic.data.model

/** Shared shell bounds; width calibration never scales text, height or targets. */
object IslandGeometryConstraints {
    fun expandedWidth(windowWidth: Float, margin: Float, maximum: Float): Float =
        (windowWidth - margin * 2).coerceAtLeast(0f).coerceAtMost(maximum)
    fun compactWidth(requested: Float, windowWidth: Float, reservedSide: Float,
                     companionWidth: Float, maximum: Float): Float =
        requested.coerceAtLeast(0f).coerceAtMost(
            (windowWidth - reservedSide * 2 - companionWidth).coerceAtLeast(0f).coerceAtMost(maximum)
        )
}
