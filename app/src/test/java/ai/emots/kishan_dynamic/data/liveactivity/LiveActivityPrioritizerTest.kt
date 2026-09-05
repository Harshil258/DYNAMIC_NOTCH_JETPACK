package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveActivityPrioritizerTest {
    @Test
    fun higherPriorityUtilityAlertWinsOverTimer() {
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

        assertEquals(airplane, LiveActivityPrioritizer.primary(listOf(timer, airplane)))
    }

    @Test
    fun newerSourceWinsWhenPriorityIsEqual() {
        val older = LiveActivityInfo(
            id = "older",
            kind = LiveActivityKind.AIRPODS,
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
}
