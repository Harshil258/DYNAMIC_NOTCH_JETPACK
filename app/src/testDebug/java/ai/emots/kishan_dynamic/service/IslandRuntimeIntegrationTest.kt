package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.data.notification.*
import ai.emots.kishan_dynamic.playground.PlaygroundCase
import ai.emots.kishan_dynamic.playground.PlaygroundController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Task 03C Mandatory Runtime State Synchronization & Playground Parity Integration Tests.
 *
 * Validates the 10 canonical runtime scenarios through real production state management
 * (IslandStateManager) and debug Playground controller (PlaygroundController) entry points.
 */
class IslandRuntimeIntegrationTest {

    private val now = 10_000L

    private fun sampleNotification(
        id: String,
        title: String = "Test notification $id",
        text: String = "Test message body for $id",
        packageName: String = "com.google.android.apps.messaging",
        appName: String = "Messages",
        isOngoing: Boolean = false,
        progress: Int = 0,
        progressMax: Int = 0,
        isClearable: Boolean = true,
        category: String = "msg"
    ) = NotificationInfo(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        timestamp = System.currentTimeMillis(),
        actions = listOf(NotificationActionInfo("reply", "Reply")),
        isOngoing = isOngoing,
        progress = progress,
        progressMax = progressMax,
        isClearable = isClearable,
        category = category
    )

    @Before
    fun setUp() {
        IslandStateManager.resetForTesting()
    }

    // =========================================================================
    // Test 1: New notification → expanded preview → timeout → compact → next
    // =========================================================================
    @Test
    fun test_01_newNotification_transientPreview_timeout_compact_nextNotification() {
        val notif1 = sampleNotification("notif-1", "Incoming Message 1")
        IslandStateManager.postNotification(notif1, autoExpand = true, displaySeconds = 3)

        // 1. Ingest into idle -> auto-expanded transient preview
        val current1 = IslandStateManager.currentState.value as IslandState.Notification
        assertTrue("Initial arrival must auto-expand into preview", current1.isExpanded)
        assertFalse("Transient preview must not be marked expandedByUser", current1.expandedByUser)
        assertEquals(IslandInteractionOwnership.TRANSIENT_PREVIEW, IslandStateManager.behaviorState.ownership)
        val token1 = IslandStateManager.behaviorState.activeToken
        assertTrue("Preview timer token must be positive", token1 > 0L)

        // 2. Preview timeout fires
        val timeoutDecision = IslandStateManager.dispatchBehaviorEvent(
            IslandEngineEvent.PreviewTimeout(token1),
            nowMonotonic = now + 4000L
        )
        assertEquals(IslandDomainAction.CollapseToCompact, timeoutDecision.action)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)

        // Apply visual collapse to compact (mirroring real timer execution)
        IslandStateManager.collapseExpanded()
        val compactState = IslandStateManager.currentState.value as IslandState.Notification
        assertFalse("Island must collapse to compact after preview timeout", compactState.isExpanded)
        assertEquals(1, compactState.notifications.size)

        // 3. Next notification arrives from compact
        val notif2 = sampleNotification("notif-2", "Incoming Message 2")
        IslandStateManager.postNotification(notif2, autoExpand = true, displaySeconds = 3)

        val current2 = IslandStateManager.currentState.value as IslandState.Notification
        assertTrue("Second notification must auto-expand into fresh preview", current2.isExpanded)
        assertEquals("Queue must retain both notifications", 2, IslandStateManager.notificationSourceIds().size)
        assertTrue("New token must be strictly greater than token1", IslandStateManager.behaviorState.activeToken > token1)
    }

    // =========================================================================
    // Test 2: 12 notifications → tap → pager → select page 5 → new message → page 5 pinned
    // =========================================================================
    @Test
    fun test_02_twelveNotifications_tap_expandedPager_selectPage5_newMessage_page5RemainsSelected() {
        for (i in 1..12) {
            IslandStateManager.postNotification(
                sampleNotification("notif-$i", "Message $i"),
                autoExpand = false,
                displaySeconds = 3
            )
        }
        assertEquals(12, IslandStateManager.notificationSourceIds().size)

        // User taps compact island to open horizontal pager
        IslandStateManager.openNotifications(selectNext = false)
        assertEquals(IslandInteractionOwnership.PAGER_ACTIVE, IslandStateManager.behaviorState.ownership)

        // Select page 5
        IslandStateManager.selectNotification("notif-5")
        assertEquals("notif-5", IslandStateManager.behaviorState.selectedKey)

        // 13th notification arrives while user is interacting on page 5
        val notif13 = sampleNotification("notif-13", "Burst update 13")
        IslandStateManager.postNotification(notif13, autoExpand = true, displaySeconds = 3)

        // Page 5 selection MUST remain pinned (UX-C04 / PD-004)
        assertEquals("Page 5 must stay selected despite new arrival", "notif-5", IslandStateManager.behaviorState.selectedKey)
        assertEquals("Pager ownership must be preserved", IslandInteractionOwnership.PAGER_ACTIVE, IslandStateManager.behaviorState.ownership)

        // 13th notification must spawn into secondary child notices
        assertTrue("New notification must be added as satellite child notice",
            IslandStateManager.behaviorState.childNotices.any { it.key == "notif-13" })
        assertEquals(13, IslandStateManager.notificationSourceIds().size)
    }

    // =========================================================================
    // Test 3: Dismiss one notification → minor update suppressed → milestone resurfaces
    // =========================================================================
    @Test
    fun test_03_dismissOneNotification_minorUpdateSuppressed_majorMilestoneResurfaces() {
        val tracking = sampleNotification(
            id = "delivery-pkg",
            title = "Driver assigned",
            text = "Your order is being picked up",
            progress = 10,
            progressMax = 100
        )
        IslandStateManager.postNotification(tracking, autoExpand = true, displaySeconds = 3)
        assertTrue(IslandStateManager.notificationSourceIds().contains("delivery-pkg"))

        // User dismisses from Island only (creates tombstone per UX-C08)
        IslandStateManager.onUserDismissNotification("delivery-pkg", requestOsDismiss = false)
        assertFalse("Dismissed item must be removed from queue", IslandStateManager.notificationSourceIds().contains("delivery-pkg"))
        assertTrue("Tombstone must be recorded", IslandSuppressionTombstoneRegistry.isSuppressed("delivery-pkg"))

        // Minor progress update arrives (15% vs 10%)
        val minorUpdate = tracking.copy(title = "Driver on the way", progress = 15)
        IslandStateManager.postNotification(minorUpdate, autoExpand = true, displaySeconds = 3)

        assertFalse("Minor progress update must remain suppressed", IslandStateManager.notificationSourceIds().contains("delivery-pkg"))

        // Major delivery milestone arrives (100% complete)
        val milestoneUpdate = tracking.copy(title = "Order delivered!", text = "Package at your door", progress = 100)
        IslandStateManager.postNotification(milestoneUpdate, autoExpand = true, displaySeconds = 3)

        assertTrue("Major milestone must resurface into queue", IslandStateManager.notificationSourceIds().contains("delivery-pkg"))
        val active = IslandStateManager.currentState.value as IslandState.Notification
        assertTrue("Resurfaced milestone must display in expanded preview", active.isExpanded)
        assertEquals("Order delivered!", active.notifications.first { it.id == "delivery-pkg" }.title)
    }

    // =========================================================================
    // Test 4: Child touch → release → old timeout fires → refreshed child remains
    // =========================================================================
    @Test
    fun test_04_childTouch_release_oldTimeoutFires_refreshedChildRemains() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Expanded)

        // Ingest a second notification to spawn child notice
        val childNotif = sampleNotification("child-item", "Satellite notice")
        controller.postSyntheticNotification(childNotif)

        val child1 = controller.engineState.childNotices.first { it.key == "child-item" }
        val token1 = child1.token
        assertTrue("Initial child token must be set", token1 > 0L)
        assertFalse("Child must start unpaused", child1.isPaused)

        // User touches child notice: pauses dismissal
        controller.touchChild("child-item")
        val touchedChild = controller.engineState.childNotices.first { it.key == "child-item" }
        assertTrue("Touching child must pause its dismissal", touchedChild.isPaused)
        assertEquals(IslandInteractionOwnership.CHILD_INTERACTION, controller.engineState.ownership)

        // User releases child notice: restarts timer with new generation token
        controller.releaseChild("child-item")
        val releasedChild = controller.engineState.childNotices.first { it.key == "child-item" }
        assertFalse("Releasing child must unpause dismissal", releasedChild.isPaused)
        val token2 = releasedChild.token
        assertTrue("Released child must receive a fresh token", token2 > token1)

        // Stale timer callback with token1 fires
        val staleDecision = controller.dispatchEngineEvent(
            IslandEngineEvent.ChildTimeout("child-item", token1),
            nowMonotonic = now + 8000L
        )
        assertEquals("stale_child_token_ignored", staleDecision.reason)
        assertTrue("Refreshed child must NOT be removed by stale callback",
            controller.engineState.childNotices.any { it.key == "child-item" })

        // Fresh timer callback with token2 fires
        val validDecision = controller.dispatchEngineEvent(
            IslandEngineEvent.ChildTimeout("child-item", token2),
            nowMonotonic = now + 12000L
        )
        assertEquals("child_expired_visual_merge", validDecision.reason)
        assertTrue("Child notice must visually merge on valid timeout",
            controller.engineState.childNotices.none { it.key == "child-item" })
        assertTrue("Underlying notification remains in canonical queue",
            controller.engineState.queue.notifications.any { it.id == "child-item" })
    }

    // =========================================================================
    // Test 5: Active call interrupts notification → call ends → presentation restored
    // =========================================================================
    @Test
    fun test_05_activeCallInterruptsNotification_callEnds_correctPresentationRestored() {
        val notif = sampleNotification("notif-call-restore", "Important chat")
        IslandStateManager.postNotification(notif, autoExpand = true, displaySeconds = 5)

        val initialPresentation = IslandStateManager.currentState.value
        assertTrue("Initial presentation must be notification", initialPresentation is IslandState.Notification)

        // Incoming call interrupts
        IslandStateManager.postIncomingCall(ContactInfo("Jane Doe", "555-0199"))
        assertTrue("Current state must show incoming call", IslandStateManager.currentState.value is IslandState.IncomingCall)
        assertEquals(IslandInteractionOwnership.INTERRUPTED, IslandStateManager.behaviorState.ownership)

        // Call accepted -> OngoingCall
        IslandStateManager.acceptIncomingCall()
        assertTrue("Current state must show ongoing call", IslandStateManager.currentState.value is IslandState.OngoingCall)

        // Call ends
        IslandStateManager.endCall()

        // Notification presentation must be fully restored!
        val restoredPresentation = IslandStateManager.currentState.value
        assertTrue("Interrupted notification presentation must be restored after call ends",
            restoredPresentation is IslandState.Notification)
        assertEquals(1, IslandStateManager.notificationSourceIds().size)
    }

    // =========================================================================
    // Test 6: Update artwork → next event → artwork and queue remain consistent
    // =========================================================================
    @Test
    fun test_06_updateArtwork_nextEvent_artworkAndQueueRemainConsistent() {
        val notif = sampleNotification("art-item", "Photo message")
        IslandStateManager.postNotification(notif, autoExpand = false, displaySeconds = 5)

        // Artwork loaded asynchronously
        IslandStateManager.updateNotificationArtwork("art-item", "icon_v2.png", "picture_v2.png")

        val inQueue = IslandStateManager.behaviorState.queue.notifications.first { it.id == "art-item" }
        assertEquals("icon_v2.png", inQueue.imagePath)
        assertEquals("picture_v2.png", inQueue.picturePath)

        // Next event arrives (e.g. user taps compact)
        IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.TapCompact, nowMonotonic = now + 1000L)

        val preservedQueue = IslandStateManager.behaviorState.queue.notifications.first { it.id == "art-item" }
        assertEquals("Artwork must be preserved across subsequent engine events", "icon_v2.png", preservedQueue.imagePath)
        assertEquals("Picture must be preserved across subsequent engine events", "picture_v2.png", preservedQueue.picturePath)
    }

    // =========================================================================
    // Test 7: 50 notifications → no silent loss, duplicate identity, or corruption
    // =========================================================================
    @Test
    fun test_07_fiftyNotifications_noSilentLoss_duplicateIdentity_orSelectionCorruption() {
        for (i in 1..55) {
            IslandStateManager.postNotification(
                sampleNotification("item-$i", "Bulk Notification $i"),
                autoExpand = false,
                displaySeconds = 2
            )
        }

        val queue = IslandStateManager.behaviorState.queue
        assertEquals("Queue must be strictly capped at MAX_NOTIFICATIONS (50)",
            NotificationReducer.MAX_NOTIFICATIONS, queue.notifications.size)
        assertEquals(50, queue.notifications.size)

        val uniqueIds = queue.notifications.map { it.id }.toSet()
        assertEquals("All retained notifications must have distinct identities", 50, uniqueIds.size)

        // Valid active selection
        assertNotNull("Active notification ID must be valid", queue.activeNotificationId)
        assertTrue("Active key must point to an item in the queue", uniqueIds.contains(queue.activeNotificationId))

        // Overflow counts
        assertEquals("totalPendingCount must derive from real queue (50 - 1 = 49)", 49, IslandStateManager.behaviorState.totalPendingCount)
        assertEquals("overflowCount must be real outstanding minus visible children", 47, IslandStateManager.behaviorState.overflowCount(visibleChildCount = 2))
    }

    // =========================================================================
    // Test 8: Playground and production equivalent events → equivalent decisions
    // =========================================================================
    @Test
    fun test_08_playgroundAndProductionEquivalentEvents_equivalentDecisionsAndQueue() = runBlocking {
        val controller = PlaygroundController(this)
        controller.select(PlaygroundCase.Idle)

        val n1 = sampleNotification("shared-1", "Notice 1")
        val n2 = sampleNotification("shared-2", "Notice 2")

        // 1. Ingest N1 on production & playground
        val prodD1 = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.IngestNotification(n1), nowMonotonic = now)
        val playD1 = controller.dispatchEngineEvent(IslandEngineEvent.IngestNotification(n1), nowMonotonic = now)
        assertEquals(prodD1.action, playD1.action)
        assertEquals(prodD1.state.ownership, playD1.state.ownership)

        // 2. Ingest N2 on production & playground
        val prodD2 = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.IngestNotification(n2), nowMonotonic = now + 500L)
        val playD2 = controller.dispatchEngineEvent(IslandEngineEvent.IngestNotification(n2), nowMonotonic = now + 500L)
        assertEquals(prodD2.action, playD2.action)
        assertEquals(prodD2.state.queue.notifications.size, playD2.state.queue.notifications.size)

        // 3. Tap compact on production & playground
        val prodTap = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.TapCompact, nowMonotonic = now + 1000L)
        val playTap = controller.dispatchEngineEvent(IslandEngineEvent.TapCompact, nowMonotonic = now + 1000L)
        assertEquals(prodTap.action, playTap.action)
        assertEquals(prodTap.state.targetPresentation, playTap.state.targetPresentation)

        // 4. Select page 2 on production & playground
        val prodSel = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.SelectPage("shared-2"), nowMonotonic = now + 1500L)
        val playSel = controller.dispatchEngineEvent(IslandEngineEvent.SelectPage("shared-2"), nowMonotonic = now + 1500L)
        assertEquals(prodSel.state.selectedKey, playSel.state.selectedKey)
        assertEquals("shared-2", playSel.state.selectedKey)

        // 5. Swipe up collapse on production & playground
        val prodSwipe = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.SwipeUp, nowMonotonic = now + 2000L)
        val playSwipe = controller.dispatchEngineEvent(IslandEngineEvent.SwipeUp, nowMonotonic = now + 2000L)
        assertEquals(prodSwipe.action, playSwipe.action)
        assertEquals(IslandPresentationTarget.COMPACT, playSwipe.state.targetPresentation)
        assertEquals(2, controller.snapshot.value.state.let { (it as IslandState.Notification).notifications.size })
    }

    // =========================================================================
    // Test 9: Outside tap and swipe-up both collapse non-destructively
    // =========================================================================
    @Test
    fun test_09_outsideTapAndSwipeUpCollapseNonDestructively_engineStateNotStale() {
        val n1 = sampleNotification("swipe-1")
        val n2 = sampleNotification("swipe-2")
        IslandStateManager.postNotification(n1, autoExpand = false, displaySeconds = 3)
        IslandStateManager.postNotification(n2, autoExpand = false, displaySeconds = 3)

        // Open pager
        IslandStateManager.openNotifications(selectNext = false)
        assertTrue((IslandStateManager.currentState.value as IslandState.Notification).isExpanded)
        assertEquals(IslandInteractionOwnership.PAGER_ACTIVE, IslandStateManager.behaviorState.ownership)

        // Swipe-up collapses non-destructively
        IslandStateManager.collapseExpanded()
        assertFalse((IslandStateManager.currentState.value as IslandState.Notification).isExpanded)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)
        assertEquals(2, IslandStateManager.notificationSourceIds().size)

        // Re-open pager
        IslandStateManager.openNotifications(selectNext = false)
        assertTrue((IslandStateManager.currentState.value as IslandState.Notification).isExpanded)

        // Outside tap collapses non-destructively
        val outsideTapDecision = IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.OutsideTap, nowMonotonic = now + 3000L)
        assertEquals(IslandDomainAction.CollapseToCompact, outsideTapDecision.action)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)
        assertEquals(2, IslandStateManager.notificationSourceIds().size)
    }

    // =========================================================================
    // Test 10: Repeated source removal and user dismissal events remain idempotent
    // =========================================================================
    @Test
    fun test_10_repeatedSourceRemovalAndUserDismissalEvents_remainIdempotent() {
        val n1 = sampleNotification("idem-1")
        val n2 = sampleNotification("idem-2")
        IslandStateManager.postNotification(n1, autoExpand = false, displaySeconds = 3)
        IslandStateManager.postNotification(n2, autoExpand = false, displaySeconds = 3)
        assertEquals(2, IslandStateManager.notificationSourceIds().size)

        // 1. Repeated source removal
        IslandStateManager.removeNotification("idem-1")
        assertEquals(1, IslandStateManager.notificationSourceIds().size)

        // Second removal for same key must be a safe, idempotent no-op
        IslandStateManager.removeNotification("idem-1")
        assertEquals(1, IslandStateManager.notificationSourceIds().size)
        assertFalse(IslandStateManager.notificationSourceIds().contains("idem-1"))

        // 2. Repeated user dismissal
        IslandStateManager.onUserDismissNotification("idem-2", requestOsDismiss = false)
        assertEquals(0, IslandStateManager.notificationSourceIds().size)
        assertTrue(IslandSuppressionTombstoneRegistry.isSuppressed("idem-2"))

        // Second dismissal for same key must be a safe, idempotent no-op
        IslandStateManager.onUserDismissNotification("idem-2", requestOsDismiss = false)
        assertEquals(0, IslandStateManager.notificationSourceIds().size)
        assertTrue(IslandSuppressionTombstoneRegistry.isSuppressed("idem-2"))
        assertEquals(IslandState.Minimal, IslandStateManager.currentState.value)
    }

    // =========================================================================
    // Stage 0A: Notification preview eligibility & autoExpand=false verification
    // =========================================================================
    @Test
    fun test_11_previewEligibility_autoExpandFalse_staysCompactAndOwnershipSystemIdle() {
        val notif = sampleNotification("notif-compact", "Compact Only")
        IslandStateManager.postNotification(notif, autoExpand = false, displaySeconds = 3)

        val current = IslandStateManager.currentState.value as IslandState.Notification
        assertFalse("Notification posted with autoExpand=false must NOT expand", current.isExpanded)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)
        assertEquals(IslandPresentationTarget.COMPACT, IslandStateManager.behaviorState.targetPresentation)

        // Subsequent notification posted with autoExpand=false also stays compact and does not spawn child mini-island
        val notif2 = sampleNotification("notif-compact-2", "Second Compact")
        IslandStateManager.postNotification(notif2, autoExpand = false, displaySeconds = 3)

        val current2 = IslandStateManager.currentState.value as IslandState.Notification
        assertFalse("Second notification must also remain compact", current2.isExpanded)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)
        assertEquals(0, IslandStateManager.behaviorState.childNotices.size)
        assertEquals(2, IslandStateManager.behaviorState.canonicalQueueCount)
    }

    // =========================================================================
    // Stage 0C: Child timer cleanup and overflow accounting verification
    // =========================================================================
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun test_12_childTimerCleanup_and_overflowAccounting() = kotlinx.coroutines.test.runTest {
        IslandStateManager.setScopeForTesting(this)

        // User expands island with notif-1
        val notif1 = sampleNotification("notif-1", "Main Notification")
        IslandStateManager.postNotification(notif1, autoExpand = true, displaySeconds = 3)
        IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.TapCompact) // USER_EXPANDED

        assertEquals(IslandInteractionOwnership.USER_EXPANDED, IslandStateManager.behaviorState.ownership)

        // Ingest 5 child notifications into user-expanded island
        for (i in 2..6) {
            IslandStateManager.postNotification(sampleNotification("child-$i", "Child $i"), autoExpand = true, displaySeconds = 3)
        }

        // Canonical queue count is 6 (1 main + 5 children)
        assertEquals(6, IslandStateManager.behaviorState.canonicalQueueCount)

        // maxChildNotices is 4 -> childNotices size is 4, child-2 was evicted
        assertEquals(4, IslandStateManager.behaviorState.pendingChildNoticesCount)

        // visibleChildCount with limit 2 is 2
        assertEquals(2, IslandStateManager.behaviorState.visibleChildCount(limit = 2))

        // childOverflowCount with limit 2 is (4 - 2) = 2 (NOT counting queued background items)
        assertEquals(2, IslandStateManager.behaviorState.childOverflowCount(limit = 2))

        // Tap child-6 to promote it -> child-6 is removed from childNotices and its timer canceled
        IslandStateManager.dispatchBehaviorEvent(IslandEngineEvent.TapChild("child-6"))
        assertEquals("child-6", IslandStateManager.behaviorState.selectedKey)
        assertFalse(IslandStateManager.behaviorState.childNotices.any { it.key == "child-6" })

        // Collapse island -> all remaining child notices are cleared and their timers canceled
        IslandStateManager.collapseExpanded()
        assertEquals(0, IslandStateManager.behaviorState.pendingChildNoticesCount)
        assertEquals(6, IslandStateManager.behaviorState.canonicalQueueCount)
    }

    // =========================================================================
    // Stage 0D: Deterministic timer integration test advancing virtual time
    // =========================================================================
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun test_13_deterministic_previewTimeout_collapsesBothEngineAndPresentationWithoutManualCalls() = kotlinx.coroutines.test.runTest {
        IslandStateManager.setScopeForTesting(this)

        val notif = sampleNotification("notif-auto", "Auto Message")
        IslandStateManager.postNotification(notif, autoExpand = true, displaySeconds = 3)

        val before = IslandStateManager.currentState.value as IslandState.Notification
        assertTrue("Initial state must be expanded preview", before.isExpanded)
        assertEquals(IslandInteractionOwnership.TRANSIENT_PREVIEW, IslandStateManager.behaviorState.ownership)

        // Advance virtual time past the 3-second preview duration
        testScheduler.advanceTimeBy(3500L)
        testScheduler.runCurrent()

        // Verify BOTH engine ownership and visible presentation collapsed to compact WITHOUT manual fallback calls!
        val after = IslandStateManager.currentState.value as IslandState.Notification
        assertFalse("Visible presentation must collapse to compact automatically", after.isExpanded)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, IslandStateManager.behaviorState.ownership)
        assertEquals(IslandPresentationTarget.COMPACT, IslandStateManager.behaviorState.targetPresentation)
        assertEquals("Queue must retain the notification in compact mode", 1, IslandStateManager.notificationSourceIds().size)
    }
}
