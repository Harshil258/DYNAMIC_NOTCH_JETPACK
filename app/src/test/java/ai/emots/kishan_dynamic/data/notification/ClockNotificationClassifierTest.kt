package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClockNotificationClassifierTest {

    @Test
    fun recognizesClockFamiliesOnlyWhenCustomContentExists() {
        assertEquals(
            ClockNotificationKind.STOPWATCH,
            classifyClockNotification("com.google.android.deskclock", "Stopwatch", true)
        )
        assertEquals(
            ClockNotificationKind.TIMER,
            classifyClockNotification("com.google.android.deskclock", "Timer", true)
        )
        assertNull(
            classifyClockNotification("com.google.android.deskclock", "Timer", false)
        )
    }

    @Test
    fun ignoresUnrelatedChannels() {
        assertNull(
            classifyClockNotification("com.example.clock", "alarm", true)
        )
    }
}
