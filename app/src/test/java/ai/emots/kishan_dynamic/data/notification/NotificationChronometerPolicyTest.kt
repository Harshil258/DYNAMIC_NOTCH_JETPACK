package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationChronometerPolicyTest {

    @Test
    fun convertsWallClockBaseToMonotonicTime() {
        assertEquals(
            95_000L,
            NotificationChronometerPolicy.baseFromWallClock(
                notificationWhenMillis = 1_000_000L,
                nowWallClockMillis = 1_005_000L,
                nowElapsedRealtimeMillis = 100_000L,
                enabled = true
            )
        )
    }

    @Test
    fun calculatesElapsedAndCountdownValuesWithoutGoingNegative() {
        assertEquals(
            12L,
            NotificationChronometerPolicy.elapsedSeconds(
                baseElapsedRealtimeMillis = 88_000L,
                nowElapsedRealtimeMillis = 100_000L,
                countDown = false
            )
        )
        assertEquals(
            12L,
            NotificationChronometerPolicy.elapsedSeconds(
                baseElapsedRealtimeMillis = 112_000L,
                nowElapsedRealtimeMillis = 100_000L,
                countDown = true
            )
        )
        assertEquals(
            0L,
            NotificationChronometerPolicy.elapsedSeconds(
                baseElapsedRealtimeMillis = 90_000L,
                nowElapsedRealtimeMillis = 100_000L,
                countDown = true
            )
        )
    }

    @Test
    fun formatsLongAndShortDurations() {
        assertEquals("02:05", NotificationChronometerPolicy.format(125L))
        assertEquals("1:02:05", NotificationChronometerPolicy.format(3_725L))
    }
}
