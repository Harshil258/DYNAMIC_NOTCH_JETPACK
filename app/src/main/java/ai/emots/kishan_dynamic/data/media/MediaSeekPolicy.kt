package ai.emots.kishan_dynamic.data.media

/** Keeps media seek commands bounded and independent from Android controllers. */
object MediaSeekPolicy {
    fun targetPosition(currentMs: Long, durationMs: Long, deltaMs: Long): Long {
        val target = currentMs + deltaMs
        return if (durationMs > 0L) target.coerceIn(0L, durationMs) else target.coerceAtLeast(0L)
    }
}
