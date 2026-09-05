package ai.emots.kishan_dynamic.data.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSeekPolicyTest {
    @Test
    fun seekIsClampedToTrackBounds() {
        assertEquals(0L, MediaSeekPolicy.targetPosition(5_000L, 60_000L, -15_000L))
        assertEquals(60_000L, MediaSeekPolicy.targetPosition(55_000L, 60_000L, 15_000L))
    }

    @Test
    fun unknownDurationStillAllowsForwardAndBackwardSeeking() {
        assertEquals(0L, MediaSeekPolicy.targetPosition(5_000L, 0L, -15_000L))
        assertEquals(20_000L, MediaSeekPolicy.targetPosition(5_000L, 0L, 15_000L))
    }
}
