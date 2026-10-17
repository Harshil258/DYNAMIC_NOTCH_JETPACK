package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveActivityPrioritizerTest {
    @Test
    fun activeTimerWinsOverLowPriorityUtilityPrompt() {
        val timer = LiveActivityInfo(
            id = "timer",
            kind = LiveActivityKind.TIMER,
            title = "Timer",
            startedAtMillis = 100L
        )
        val airplane = LiveActivityInfo(
            id = "airplane",
            kind = LiveActivityKind.AIRPLANE_ALERT,
            title = "Airplane Mode",
            startedAtMillis = 1L
        )

        assertEquals(timer, LiveActivityPrioritizer.primary(listOf(timer, airplane)))
    }

    @Test
    fun newerSourceWinsWhenPriorityIsEqual() {
        val older = LiveActivityInfo(
            id = "older",
            kind = LiveActivityKind.AIR_DROP,
            title = "AirPods",
            startedAtMillis = 100L
        )
        val newer = LiveActivityInfo(
            id = "newer",
            kind = LiveActivityKind.AIR_DROP,
            title = "Quick Share",
            startedAtMillis = 200L
        )

        assertEquals(newer, LiveActivityPrioritizer.primary(listOf(older, newer)))
    }

    @Test
    fun navigationWinsOverRecordingAndNotificationsCannotDisplaceIt() {
        val navigation = LiveActivityInfo("nav", LiveActivityKind.NAVIGATION, "Turn left")
        val recording = LiveActivityInfo("record", LiveActivityKind.SCREEN_RECORDING, "Recording")

        assertEquals(navigation, LiveActivityPrioritizer.primary(listOf(recording, navigation)))
    }

    @Test
    fun expiredActivityIsIgnoredByPrioritizer() {
        val expiredAlert = LiveActivityInfo(
            id = "expired",
            kind = LiveActivityKind.MOBILE_DATA_ALERT,
            title = "Mobile Data",
            expiresAtMillis = System.currentTimeMillis() - 1000L
        )
        val activeTimer = LiveActivityInfo(
            id = "timer",
            kind = LiveActivityKind.TIMER,
            title = "Timer",
            expiresAtMillis = System.currentTimeMillis() + 60_000L
        )

        assertEquals(activeTimer, LiveActivityPrioritizer.primary(listOf(expiredAlert, activeTimer)))
        assertEquals(null, LiveActivityPrioritizer.primary(listOf(expiredAlert)))
    }
}
