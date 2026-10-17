package ai.emots.kishan_dynamic.data.media

/** PlaybackState timestamps use elapsed realtime, not wall-clock time. */
object MediaPlaybackPositionPolicy {
    fun position(positionMs: Long, durationMs: Long, playing: Boolean,
                 speed: Float, updatedAtMs: Long, nowMs: Long): Long {
        val elapsed = if (playing && updatedAtMs > 0 && speed.isFinite())
            (nowMs - updatedAtMs).coerceAtLeast(0L).toDouble() * speed else 0.0
        val result = (positionMs.coerceAtLeast(0L).toDouble() + elapsed)
            .coerceAtLeast(0.0).toLong()
        return if (durationMs > 0) result.coerceAtMost(durationMs) else result
    }
}
