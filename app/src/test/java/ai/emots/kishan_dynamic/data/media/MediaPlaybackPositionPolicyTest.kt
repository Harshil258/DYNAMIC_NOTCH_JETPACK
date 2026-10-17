package ai.emots.kishan_dynamic.data.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaPlaybackPositionPolicyTest {
    @Test fun advancesAtSessionSpeed() {
        assertEquals(5000L, MediaPlaybackPositionPolicy.position(1000, 10000, true, 2f, 100, 2100))
    }
    @Test fun pausedAndUnstampedSessionsDoNotAdvance() {
        assertEquals(1000L, MediaPlaybackPositionPolicy.position(1000, 10000, false, 1f, 100, 2100))
        assertEquals(1000L, MediaPlaybackPositionPolicy.position(1000, 10000, true, 1f, 0, 2100))
    }
    @Test fun clampsDurationAndHandlesInvalidValues() {
        assertEquals(2000L, MediaPlaybackPositionPolicy.position(1000, 2000, true, 1f, 100, 9000))
        assertEquals(0L, MediaPlaybackPositionPolicy.position(-1, 0, true, Float.NaN, 100, 9000))
        assertEquals(1000L, MediaPlaybackPositionPolicy.position(1000, 0, true, 1f, 9000, 100))
    }
}
