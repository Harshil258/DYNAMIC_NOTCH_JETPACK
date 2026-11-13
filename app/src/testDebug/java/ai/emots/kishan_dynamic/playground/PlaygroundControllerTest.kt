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
        assertEquals(IslandDemoState.Minimal, resolveOverlayDemoState(IslandState.Minimal, 0))
        assertEquals(IslandDemoState.Idle, resolveOverlayDemoState(IslandState.Hidden, 0))
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

    @Test fun notificationScenariosLoadCorrectCapacityAndPresentation() = runBlocking {
        val controller = PlaygroundController(this)

        // 0 -> Idle
        controller.loadNotificationScenario(0)
        assertEquals(PlaygroundCase.Idle, controller.snapshot.value.selected)
        assertEquals(IslandState.Minimal, controller.snapshot.value.state)

        // 1 -> Single card expanded
        controller.loadNotificationScenario(1)
        val s1 = controller.snapshot.value.state as IslandState.Notification
        assertTrue(s1.isExpanded)
        assertEquals(1, s1.notifications.size)

        // 2 -> Pager with 2 items
        controller.loadNotificationScenario(2)
        val s2 = controller.snapshot.value.state as IslandState.Notification
        assertTrue(s2.isExpanded)
        assertEquals(2, s2.notifications.size)

        // 12, 25, 50
        for (count in listOf(12, 25, 50)) {
            controller.loadNotificationScenario(count)
            val sc = controller.snapshot.value.state as IslandState.Notification
            assertTrue(sc.isExpanded)
            assertEquals(count, sc.notifications.size)
            assertEquals(count, controller.engineState.canonicalQueueCount)
        }
    }

    @Test fun playgroundInteractiveScenariosOperateDeterministically() = runBlocking {
        val controller = PlaygroundController(this)

        // Page 5 arrival scenario
        controller.scenarioPage5Arrival()
        assertEquals("mock-5", controller.engineState.selectedKey)
        assertEquals(13, controller.engineState.canonicalQueueCount)
        assertEquals(1, controller.engineState.pendingChildNoticesCount)

        // Same-key update scenario
        controller.scenarioSameKeyUpdate()
        val updated = controller.engineState.queue.notifications.first { it.id == "mock-5" }
        assertEquals("Updated in-place!", updated.title)
        assertEquals("mock-5", controller.engineState.selectedKey)

        // Non-selected background removal
        val beforeCount = controller.engineState.canonicalQueueCount
        controller.scenarioNonSelectedRemoval()
        assertEquals(beforeCount - 1, controller.engineState.canonicalQueueCount)
        assertEquals("mock-5", controller.engineState.selectedKey)

        // Selected removal
        controller.scenarioSelectedRemoval()
        assertNotEquals("mock-5", controller.engineState.selectedKey)

        // Call preempt and restore
        controller.scenarioCallRestore()
        assertEquals(ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.INTERRUPTED, controller.engineState.ownership)
        controller.scenarioCallEndRestore()
        assertEquals(ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.PAGER_ACTIVE, controller.engineState.ownership)
    }
    @Test fun layoutObservationCannotReplaceAnotherSelectedFixture() = runBlocking {
        val controller = PlaygroundController(this)
        controller.layeredScenario(1)
        controller.select(PlaygroundCase.Music)
        val music = controller.snapshot.value.state
        controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ChildVisibleLimitChanged(0))
        assertEquals(music, controller.snapshot.value.state)
        assertEquals(PlaygroundCase.Music, controller.snapshot.value.selected)
        assertTrue(controller.snapshot.value.behavior.childNotices.isEmpty())
        controller.stopClock()
    }

    @Test fun playgroundDismissCallbackPreservesRemainingQueueAndSelection() = runBlocking {
        val controller = PlaygroundController(this)
        controller.loadNotificationScenario(3)
        assertEquals(3, controller.engineState.canonicalQueueCount)
        val initialSelected = controller.engineState.selectedKey
        assertNotNull(initialSelected)

        // Dismiss the selected notification via dismissNotification (invoked by onNotificationDismiss)
        controller.dismissNotification(initialSelected!!)
        assertEquals(2, controller.engineState.canonicalQueueCount)
        val remainingState = controller.snapshot.value.state as IslandState.Notification
        assertEquals(2, remainingState.notifications.size)
        assertTrue(remainingState.isExpanded)
        assertNotEquals(initialSelected, controller.engineState.selectedKey)

        // Dismiss the second
        val secondSelected = controller.engineState.selectedKey!!
        controller.dismissNotification(secondSelected)
        assertEquals(1, controller.engineState.canonicalQueueCount)

        // Dismiss the final notification -> cleanly returns to Idle capsule
        val lastSelected = controller.engineState.selectedKey!!
        controller.dismissNotification(lastSelected)
        assertEquals(0, controller.engineState.canonicalQueueCount)
        assertEquals(PlaygroundCase.Idle, controller.snapshot.value.selected)
        assertEquals(IslandState.Minimal, controller.snapshot.value.state)
    }

    @Test fun playgroundOpenNotificationsCallbackPreservesExistingQueue() = runBlocking {
        val controller = PlaygroundController(this)
        controller.loadNotificationScenario(4)
        assertEquals(4, controller.engineState.canonicalQueueCount)
        val originalIds = controller.engineState.queue.notifications.map { it.id }

        // Switch to quick controls (which retains queue in engine)
        controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact)
        // Call openNotifications() callback (which is wired to onOpenNotifications in Quick Controls)
        controller.openNotifications()

        // Verify the 4 original notifications were NOT replaced by a synthetic single card!
        assertEquals(4, controller.engineState.canonicalQueueCount)
        assertEquals(originalIds, controller.engineState.queue.notifications.map { it.id })
        val state = controller.snapshot.value.state as IslandState.Notification
        assertEquals(4, state.notifications.size)
        assertTrue(state.isExpanded)
    }

    @Test fun playgroundChildPromotionAndSwipeUpCollapseUseSharedEngine() = runBlocking {
        val controller = PlaygroundController(this)
        controller.layeredScenario(3)
        val initialSelected = controller.engineState.selectedKey
        assertEquals(3, controller.engineState.pendingChildNoticesCount)

        // Child touch & release
        val targetChild = controller.engineState.childNotices.first().key
        controller.touchChild(targetChild)
        assertTrue(controller.engineState.childNotices.first { it.key == targetChild }.isPaused)
        controller.releaseChild(targetChild)

        // Child promotion
        controller.promoteChild(targetChild)
        assertEquals(targetChild, controller.engineState.selectedKey)
        assertEquals(targetChild, controller.engineState.queue.activeNotificationId)
        assertTrue(controller.engineState.childNotices.none { it.key == targetChild })

        // Non-destructive swipe-up collapse
        controller.expand(false)
        val compactState = controller.snapshot.value.state as IslandState.Notification
        assertFalse(compactState.isExpanded)
        assertEquals(4, controller.engineState.canonicalQueueCount) // Main + 3 arrivals retained in queue!
        assertTrue(controller.engineState.childNotices.isEmpty()) // Satellite children cleared on collapse
    }

    @Test fun openNotificationsTruthfullyPreservesQueueAcross0_1_12_and_50_Items() = runBlocking {
        val controller = PlaygroundController(this)

        // 1. Zero notifications in queue (Empty queue)
        controller.select(PlaygroundCase.Controls)
        assertEquals(0, controller.engineState.canonicalQueueCount)
        assertEquals(PlaygroundCase.Controls, controller.snapshot.value.selected)

        // Call openNotifications() on empty queue
        controller.openNotifications()

        // Truthful assertion: Zero notifications synthesized implicitly!
        assertEquals(0, controller.engineState.canonicalQueueCount)
        assertTrue(controller.engineState.queue.notifications.isEmpty())
        assertTrue(controller.snapshot.value.feedback.contains("No notifications in queue"))

        // 2. Exactly 1 notification in queue
        controller.loadNotificationScenario(1)
        assertEquals(1, controller.engineState.canonicalQueueCount)
        val singleId = controller.engineState.queue.notifications.first().id

        // Open notifications with 1 item
        controller.openNotifications()
        assertEquals(1, controller.engineState.canonicalQueueCount)
        assertEquals(singleId, controller.engineState.queue.notifications.first().id)
        val s1 = controller.snapshot.value.state as IslandState.Notification
        assertEquals(1, s1.notifications.size)
        assertTrue(s1.isExpanded)

        // 3. Exactly 12 notifications in queue
        controller.loadNotificationScenario(12)
        assertEquals(12, controller.engineState.canonicalQueueCount)
        val ids12 = controller.engineState.queue.notifications.map { it.id }

        // Open notifications with 12 items
        controller.openNotifications()
        assertEquals(12, controller.engineState.canonicalQueueCount)
        assertEquals(ids12, controller.engineState.queue.notifications.map { it.id })
        val s12 = controller.snapshot.value.state as IslandState.Notification
        assertEquals(12, s12.notifications.size)
        assertTrue(s12.isExpanded)

        // 4. Maximum canonical 50 notifications in queue
        controller.loadNotificationScenario(50)
        assertEquals(50, controller.engineState.canonicalQueueCount)
        val ids50 = controller.engineState.queue.notifications.map { it.id }

        // Open notifications with 50 items
        controller.openNotifications()
        assertEquals(50, controller.engineState.canonicalQueueCount)
        assertEquals(ids50, controller.engineState.queue.notifications.map { it.id })
        val s50 = controller.snapshot.value.state as IslandState.Notification
        assertEquals(50, s50.notifications.size)
        assertTrue(s50.isExpanded)
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test fun readablePreviewEffectUsesOneSandboxScheduler() = kotlinx.coroutines.test.runTest {
        val controller = PlaygroundController(this)
        val source = mockNotification(PlaygroundOptions())
        controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(source))
        val token = controller.engineState.activeToken
        testScheduler.advanceTimeBy(10_000); testScheduler.runCurrent()
        assertEquals(ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.TRANSIENT_PREVIEW, controller.engineState.ownership)
        controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviewReadable(token))
        testScheduler.advanceTimeBy(3501); testScheduler.runCurrent()
        assertEquals(ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.COMPACT, controller.engineState.targetPresentation)
        controller.stopClock()
    }
    @Test fun sandboxReplyAndCallUseCanonicalOwnershipAndRetainOnlyValidDraft() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Expanded)
        val source = controller.engineState.queue.notifications.single()
        val action = source.actions.first { it.isReply }
        controller.startReply(source.id, action)
        controller.replyDrafts.write(source.id, action.id, "Unsent fixture draft")
        controller.scenarioCallRestore()
        assertEquals(source.id, controller.snapshot.value.replyingNotificationId)
        controller.scenarioCallEndRestore()
        assertEquals(ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.REPLY, controller.engineState.targetPresentation)
        assertEquals("Unsent fixture draft", controller.replyDrafts.read(source.id, action.id))
        controller.removeSourceNotification(source.id)
        assertEquals("", controller.replyDrafts.read(source.id, action.id))
        assertEquals(null, controller.snapshot.value.replyingNotificationId)
        controller.stopClock()
    }
}
