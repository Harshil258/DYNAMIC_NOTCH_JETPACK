package ai.emots.kishan_dynamic.playground

import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class PlaygroundControllerTest {
    @Test fun everyFixtureUsesARealStateAndPresentationMapping() {
        assertEquals(19, PlaygroundCase.entries.size)
        assertTrue(playgroundSelfChecks().all { it.passed })
        assertEquals(IslandDemoState.NotificationExpanded,
            resolveOverlayDemoState(mockState(PlaygroundCase.Expanded, PlaygroundOptions()), 1))
        assertEquals(IslandDemoState.Idle, resolveOverlayDemoState(IslandState.Minimal, 0))
        assertEquals(IslandDemoState.NotificationWithMusicCompact,
            resolveOverlayDemoState(mockState(PlaygroundCase.Multiple, PlaygroundOptions()), 2))
    }
    @Test fun realInteractionPolicyExpandsAndMockControlsNeverCallExternalHandlers() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Compact)
        controller.interaction()
        assertTrue((controller.snapshot.value.state as IslandState.Notification).isExpanded)
        controller.expand(false)
        assertFalse((controller.snapshot.value.state as IslandState.Notification).isExpanded)
        controller.select(PlaygroundCase.IncomingCall)
        controller.control(IslandControlAction.CallAccept)
        assertTrue(controller.snapshot.value.state is IslandState.OngoingCall)
        controller.control(IslandControlAction.CallEnd)
        assertEquals(IslandState.Minimal, controller.snapshot.value.state)
        controller.control(IslandControlAction.PremiumBuy)
        assertTrue(controller.snapshot.value.feedback.contains("no Android, ad or purchase"))
    }
    @Test fun edgeCaseOptionsRefreshOnlySyntheticContentAndPreserveExpansion() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Expanded)
        controller.options { it.copy(longText = true, missingIcon = true, actionCount = 4) }
        val state = controller.snapshot.value.state as IslandState.Notification
        assertTrue(state.isExpanded)
        assertTrue(state.notifications.single().title.length > 50)
        assertEquals(4, state.notifications.single().actions.size)
        assertEquals("", state.notifications.single().packageName)
    }
    @Test fun replayTraversesRealStatesRatherThanRecreatingACopyOfTheAnimation() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Expanded)
        controller.replay()
        assertEquals(IslandState.Minimal, controller.snapshot.value.state)
        delay(370)
        assertTrue(controller.snapshot.value.state is IslandState.Notification)
        assertEquals(1, controller.snapshot.value.replay)
        assertFalse(controller.snapshot.value.sequenceRunning)
    }
    @Test fun rapidEventsRemainBoundedAndFinishAtTheLastSource() = runBlocking {
        val controller = PlaygroundController(this)
        controller.burst()
        delay(100)
        var elapsed = 0
        while (controller.snapshot.value.sequenceRunning && elapsed < 4000) {
            delay(100)
            elapsed += 100
        }
        val state = controller.snapshot.value.state as IslandState.Notification
        assertEquals("mock-30", state.notifications.first().id)
        assertEquals(30, state.notifications.size)
        assertEquals(30, controller.snapshot.value.eventCount)
        assertFalse(controller.snapshot.value.sequenceRunning)
    }
    @Test fun motionSpeedsChangeTheActualFiniteAnimationSpecAndReachStableEndpoints() {
        for (speed in listOf(.25f, .5f, 1f, 2f)) for (curve in IslandMotionCurve.entries) {
            val motion = IslandMotionSettings(speed = speed, curve = curve)
            val animation = TargetBasedAnimation(motion.spec<Float>(), Float.VectorConverter, 60f, 200f)
            assertEquals(200f, animation.getValueFromNanos(animation.durationNanos + 1), .01f)
        }
        assertEquals(640, IslandMotionSettings(speed = .25f).effectiveDuration)
        assertEquals(80, IslandMotionSettings(speed = 2f).effectiveDuration)
        val disabled = TargetBasedAnimation(IslandMotionSettings(enabled = false).spec<Float>(), Float.VectorConverter, 60f, 200f)
        assertEquals(0L, disabled.durationNanos)
        assertEquals(200f, disabled.getValueFromNanos(0), .01f)
    }
}
