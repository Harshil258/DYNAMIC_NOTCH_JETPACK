package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Deterministic acceptance tests for the canonical IslandBehaviorEngine.
 * Covers all 29 target scenarios from DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md:
 * - A01, A02, A03, A04, A05, A06, A08, A10, A11, A13, A14, A15, A16, A17, A19, A20, A21, A22, A23, A24, A27, A28, A35, A36, A40
 * - U01, U04, C05, P05
 * - Strict negative assertions for non-destructive behavior, zero unintended OS cancellations,
 *   and stable selection identity.
 */
class IslandBehaviorEngineTest {

    private lateinit var engine: IslandBehaviorEngine
    private var now: Long = 10_000L

    @Before
    fun setUp() {
        engine = IslandBehaviorEngine()
        now = 10_000L
    }

    private fun sampleNotification(
        id: String,
        packageName: String = "com.example.chat",
        appName: String = "Chat",
        title: String = "Alice",
        text: String = "Hello!",
        timestamp: Long = now,
        isClearable: Boolean = true
    ): NotificationInfo = NotificationInfo(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        timestamp = timestamp,
        isClearable = isClearable
    )

    // =========================================================================
    // Scenario A01: Idle, zero active items -> Tap opens Quick Controls (UX-C01)
    // =========================================================================
    @Test
    fun test_A01_idleZeroActiveItemsTap_opensQuickControls() {
        val state = IslandBehaviorState(
            queue = NotificationQueueState(),
            targetPresentation = IslandPresentationTarget.IDLE,
            ownership = IslandInteractionOwnership.SYSTEM_IDLE
        )

        val decision = engine.reduce(state, IslandEngineEvent.TapCompact, now)

        assertEquals(IslandDomainAction.OpenQuickControls, decision.action)
        assertEquals(IslandPresentationTarget.QUICK_CONTROLS, decision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.USER_EXPANDED, decision.state.ownership)
        assertTrue("Must not produce OS side effects", decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario A02: Compact one ordinary message -> Tap expands message details (UX-C02)
    // =========================================================================
    @Test
    fun test_A02_compactOneMessageTap_expandsNotification_osUntouched() {
        val notif = sampleNotification("msg1")
        val state = IslandBehaviorState(
            queue = NotificationQueueState(notifications = listOf(notif), activeNotificationId = notif.id),
            targetPresentation = IslandPresentationTarget.COMPACT,
            ownership = IslandInteractionOwnership.SYSTEM_IDLE
        )

        val decision = engine.reduce(state, IslandEngineEvent.TapCompact, now)

        assertEquals(IslandDomainAction.ExpandNotification, decision.action)
        assertEquals(IslandPresentationTarget.USER_EXPANDED, decision.state.targetPresentation)
        assertEquals("msg1", decision.state.selectedKey)
        assertTrue("OS notification must remain completely untouched", decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario A03: Compact 12 eligible messages -> Tap opens horizontal pager (UX-C03)
    // =========================================================================
    @Test
    fun test_A03_compactTwelveMessagesTap_opensHorizontalPager_newestFirst() {
        var state = IslandBehaviorState()
        for (i in 1..12) {
            val n = sampleNotification("msg$i", timestamp = now + i * 1000L)
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(n), now + i * 1000L).state
        }
        // Collapse to compact
        state = state.copy(
            targetPresentation = IslandPresentationTarget.COMPACT,
            ownership = IslandInteractionOwnership.SYSTEM_IDLE
        )

        assertEquals(12, state.queue.notifications.size)

        val decision = engine.reduce(state, IslandEngineEvent.TapCompact, now)

        assertEquals(IslandDomainAction.OpenPager, decision.action)
        assertEquals(IslandPresentationTarget.PAGER, decision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.PAGER_ACTIVE, decision.state.ownership)
        // Newest notification (msg12) must be first / selected
        assertEquals("msg12", decision.state.selectedKey)
    }

    // =========================================================================
    // Scenario A04: Compact 50 mixed messages -> Pager handles all without duplicates
    // =========================================================================
    @Test
    fun test_A04_compactFiftyMixedMessages_pagerHandlesAllWithoutDuplicates() {
        var state = IslandBehaviorState()
        for (i in 1..50) {
            val n = sampleNotification("bulk_$i", timestamp = now + i * 50L)
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(n), now + i * 50L).state
        }
        // Post duplicates/updates for 5 keys
        for (i in 1..5) {
            val update = sampleNotification("bulk_$i", text = "Updated $i", timestamp = now + 5000L + i)
            state = engine.reduce(state, IslandEngineEvent.UpdateNotification(update), now + 5000L + i).state
        }

        // Queue is bounded by NotificationReducer.MAX_NOTIFICATIONS (20) to prevent memory bloat
        assertEquals("Queue must be safely bounded to 20 notifications without duplication", 20, state.queue.notifications.size)
        assertEquals(20, state.queue.notifications.map { it.id }.distinct().size)

        val decision = engine.reduce(
            state.copy(targetPresentation = IslandPresentationTarget.COMPACT, ownership = IslandInteractionOwnership.SYSTEM_IDLE),
            IslandEngineEvent.TapCompact,
            now + 6000L
        )

        assertEquals(IslandDomainAction.OpenPager, decision.action)
        assertEquals(IslandPresentationTarget.PAGER, decision.state.targetPresentation)
        assertNotNull(decision.state.selectedKey)
    }

    // =========================================================================
    // Scenario A05: Compact 1 message -> Auto-preview completes, compact badge retained (UX-C02)
    // =========================================================================
    @Test
    fun test_A05_compactOneMessage_autoPreviewCompletes_compactBadgeRetained_noOsCancellation() {
        val notif = sampleNotification("msg1")
        val ingestDecision = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif), now)

        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, ingestDecision.state.targetPresentation)
        val token = ingestDecision.state.activeToken

        // Simulate preview timeout
        val timeoutDecision = engine.reduce(ingestDecision.state, IslandEngineEvent.PreviewTimeout(token), now + 3500L)

        assertEquals(IslandDomainAction.CollapseToCompact, timeoutDecision.action)
        assertEquals(IslandPresentationTarget.COMPACT, timeoutDecision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, timeoutDecision.state.ownership)
        assertEquals("Queue item must be retained in compact badge", 1, timeoutDecision.state.queue.notifications.size)
        assertTrue("No OS cancellation must occur on preview completion", timeoutDecision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario A06: Transient preview reading -> Second arrives, child emerges below (UX-C11)
    // =========================================================================
    @Test
    fun test_A06_transientPreviewReading_secondArrives_childEmerges_mainPreviewStays() {
        val notif1 = sampleNotification("msg1", title = "First")
        val state1 = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif1), now).state

        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, state1.targetPresentation)
        assertEquals("msg1", state1.selectedKey)

        // Arrive second notification while user is glancing at preview
        val notif2 = sampleNotification("msg2", title = "Second")
        val decision2 = engine.reduce(state1, IslandEngineEvent.IngestNotification(notif2), now + 1000L)

        assertEquals(IslandDomainAction.UpsertChildNotice, decision2.action)
        // Main preview stays firmly anchored
        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, decision2.state.targetPresentation)
        assertEquals("msg1", decision2.state.selectedKey)
        // Child mini-island emerges
        assertEquals(1, decision2.state.childNotices.size)
        assertEquals("msg2", decision2.state.childNotices.first().key)
        assertEquals(2, decision2.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A08: User-expanded pager page five -> New normal message arrives (UX-C04)
    // =========================================================================
    @Test
    fun test_A08_userExpandedPagerPageFive_newNormalMessage_selectedKeyPinned_childEmerges() {
        var state = IslandBehaviorState()
        for (i in 1..8) {
            val n = sampleNotification("msg$i", timestamp = now + i * 100L)
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(n), now + i * 100L).state
        }
        // User opens pager and explicitly selects page 5 ("msg4")
        state = engine.reduce(state, IslandEngineEvent.TapCompact, now + 1000L).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("msg4"), now + 1100L).state

        assertEquals("msg4", state.selectedKey)
        assertEquals(IslandInteractionOwnership.PAGER_ACTIVE, state.ownership)

        // New normal message arrives
        val incoming = sampleNotification("msg_new", timestamp = now + 2000L)
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(incoming), now + 2000L)

        assertEquals(IslandDomainAction.UpsertChildNotice, decision.action)
        // Selected key MUST stay pinned to msg4 (UX-C04 / PD-015)
        assertEquals("msg4", decision.state.selectedKey)
        assertEquals(IslandInteractionOwnership.PAGER_ACTIVE, decision.state.ownership)
        assertEquals("msg_new", decision.state.childNotices.first().key)
        assertEquals(9, decision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A10: User-expanded pager page five -> Ranking reorder keeps selected key
    // =========================================================================
    @Test
    fun test_A10_userExpandedPager_rankingReorder_selectedStableKeyUnchanged() {
        var state = IslandBehaviorState()
        for (i in 1..6) {
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(sampleNotification("n$i")), now).state
        }
        state = engine.reduce(state, IslandEngineEvent.TapCompact, now).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("n4"), now).state

        assertEquals("n4", state.selectedKey)

        // Simulate notification listener update with ranking reorder
        val updateNotification = sampleNotification("n1", text = "Bumped rank", timestamp = now + 500L)
        val updateDecision = engine.reduce(state, IslandEngineEvent.UpdateNotification(updateNotification), now + 500L)

        assertEquals("n4", updateDecision.state.selectedKey)
        assertEquals(IslandPresentationTarget.PAGER, updateDecision.state.targetPresentation)
    }

    // =========================================================================
    // Scenario A11: User-expanded pager -> Same-key minor update updates in-place
    // =========================================================================
    @Test
    fun test_A11_userExpandedPager_sameKeyMinorUpdate_inPlaceUpdate_noNewPageOrChild() {
        var state = IslandBehaviorState()
        val notif1 = sampleNotification("key1", text = "Initial Text")
        val notif2 = sampleNotification("key2", text = "Second")
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(notif1), now).state
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(notif2), now).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("key1"), now).state

        // Same key minor text update
        val updatedNotif = notif1.copy(text = "Updated Text")
        val decision = engine.reduce(state, IslandEngineEvent.UpdateNotification(updatedNotif), now + 100L)

        assertEquals(IslandDomainAction.SuppressMinorUpdate, decision.action)
        assertEquals("key1", decision.state.selectedKey)
        assertEquals("Updated Text", decision.state.queue.notifications.first { it.id == "key1" }.text)
        assertTrue("No new child notice created for in-place update of key1", decision.state.childNotices.none { it.key == "key1" })
    }

    // =========================================================================
    // Scenario A13: Source removes currently selected middle pager item -> Next selected (UX-C05)
    // =========================================================================
    @Test
    fun test_A13_sourceRemovesCurrentlySelectedMiddlePagerItem_nextNeighborSelected_remainsExpanded() {
        val a = sampleNotification("A")
        val b = sampleNotification("B")
        val c = sampleNotification("C")
        var state = IslandBehaviorState()
        listOf(a, b, c).forEach { state = engine.reduce(state, IslandEngineEvent.IngestNotification(it), now).state }
        state = engine.reduce(state, IslandEngineEvent.TapCompact, now).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("B"), now).state

        assertEquals("B", state.selectedKey)

        // Source removes middle item B
        val decision = engine.reduce(state, IslandEngineEvent.RemoveNotification("B"), now + 500L)

        assertEquals(IslandDomainAction.SelectNextOrPrevious, decision.action)
        // In newest-first order [C, B, A], removing B at index 1 shifts A into index 1
        assertEquals("A", decision.state.selectedKey)
        assertEquals(IslandPresentationTarget.PAGER, decision.state.targetPresentation)
        assertEquals(2, decision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A14: Source removes currently selected final pager item -> Previous selected (UX-C05)
    // =========================================================================
    @Test
    fun test_A14_sourceRemovesCurrentlySelectedFinalPagerItem_previousNeighborSelected() {
        val a = sampleNotification("A")
        val b = sampleNotification("B")
        val c = sampleNotification("C")
        var state = IslandBehaviorState()
        listOf(a, b, c).forEach { state = engine.reduce(state, IslandEngineEvent.IngestNotification(it), now).state }
        state = engine.reduce(state, IslandEngineEvent.TapCompact, now).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("C"), now).state

        assertEquals("C", state.selectedKey)

        // Source removes final item C
        val decision = engine.reduce(state, IslandEngineEvent.RemoveNotification("C"), now + 500L)

        assertEquals(IslandDomainAction.SelectNextOrPrevious, decision.action)
        // Previous neighbor B selected
        assertEquals("B", decision.state.selectedKey)
        assertEquals(IslandPresentationTarget.PAGER, decision.state.targetPresentation)
    }

    // =========================================================================
    // Scenario A15: Source removes non-selected item -> Selected key unchanged
    // =========================================================================
    @Test
    fun test_A15_sourceRemovesNonSelectedItem_selectedKeyUnchanged_countDecrements() {
        val a = sampleNotification("A")
        val b = sampleNotification("B")
        var state = IslandBehaviorState()
        listOf(a, b).forEach { state = engine.reduce(state, IslandEngineEvent.IngestNotification(it), now).state }
        state = engine.reduce(state, IslandEngineEvent.SelectPage("A"), now).state

        val decision = engine.reduce(state, IslandEngineEvent.RemoveNotification("B"), now + 100L)

        assertEquals(IslandDomainAction.KeepSelectedKey, decision.action)
        assertEquals("A", decision.state.selectedKey)
        assertEquals(1, decision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A16: Source removes final eligible item -> Directly idle compact (UX-C07)
    // =========================================================================
    @Test
    fun test_A16_sourceRemovesFinalEligibleItem_noActivity_idleCompact_tapQuickControls() {
        val a = sampleNotification("A")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(a), now).state

        val removeDecision = engine.reduce(state, IslandEngineEvent.RemoveNotification("A"), now + 100L)

        assertEquals(IslandDomainAction.CollapseToCompact, removeDecision.action)
        assertEquals(IslandPresentationTarget.IDLE, removeDecision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, removeDecision.state.ownership)
        assertNull(removeDecision.state.selectedKey)
        assertTrue(removeDecision.state.queue.notifications.isEmpty())

        // UX-C01: Tap on idle opens Quick Controls
        val tapDecision = engine.reduce(removeDecision.state, IslandEngineEvent.TapCompact, now + 200L)
        assertEquals(IslandDomainAction.OpenQuickControls, tapDecision.action)
        assertEquals(IslandPresentationTarget.QUICK_CONTROLS, tapDecision.state.targetPresentation)
    }

    // =========================================================================
    // Scenario A17: User clicks x, default setting -> Island-only removal, OS shade untouched (UX-C06)
    // =========================================================================
    @Test
    fun test_A17_userClicksCloseButton_defaultSetting_islandOnlyDismiss_noOsCancel() {
        val a = sampleNotification("A")
        val b = sampleNotification("B")
        var state = IslandBehaviorState()
        listOf(a, b).forEach { state = engine.reduce(state, IslandEngineEvent.IngestNotification(it), now).state }
        state = engine.reduce(state, IslandEngineEvent.SelectPage("A"), now).state

        val settings = IslandBehaviorSettings(defaultOsDismiss = false)
        val decision = engine.reduce(state, IslandEngineEvent.TapCloseButton("A"), now + 100L, settings)

        assertEquals(IslandDomainAction.DismissIslandOnly, decision.action)
        assertTrue("Underlying Android notification must NOT be canceled by default",
            decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
        assertEquals("B", decision.state.selectedKey)
        assertNotNull("Tombstone must be registered for Island-dismissed key", decision.state.tombstones["A"])
    }

    // =========================================================================
    // Scenario A19: User clicks x, OS-dismiss chosen but non-clearable -> Fallback to Island-only
    // =========================================================================
    @Test
    fun test_A19_userClicksCloseButton_osDismissChosen_nonClearable_fallbackIslandOnly() {
        val nonClearable = sampleNotification("pinned", isClearable = false)
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(nonClearable), now).state
        state = engine.reduce(state, IslandEngineEvent.SelectPage("pinned"), now).state

        // User turned on OS-dismiss preference
        val settings = IslandBehaviorSettings(defaultOsDismiss = true)
        val decision = engine.reduce(state, IslandEngineEvent.TapCloseButton("pinned"), now + 100L, settings)

        assertEquals(IslandDomainAction.DismissIslandOnly, decision.action)
        assertTrue("Must never attempt to cancel non-clearable notification on OS",
            decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario A20: User x removes last island entry, 0 activities -> Directly idle compact (UX-C07)
    // =========================================================================
    @Test
    fun test_A20_userRemovesLastIslandEntryViaCloseButton_directlyIdleCompact() {
        val single = sampleNotification("onlyOne")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(single), now).state

        val decision = engine.reduce(state, IslandEngineEvent.TapCloseButton("onlyOne"), now + 100L)

        assertEquals(IslandDomainAction.DismissIslandOnly, decision.action)
        assertEquals(IslandPresentationTarget.IDLE, decision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, decision.state.ownership)
        assertNull(decision.state.selectedKey)
        assertTrue(decision.state.queue.notifications.isEmpty())
    }

    // =========================================================================
    // Scenario A21: User Island-dismisses same key -> Progress 60->70% suppressed (UX-C08/C09)
    // =========================================================================
    @Test
    fun test_A21_userIslandDismissesSameKey_progressSixtyToSeventy_suppressed() {
        val delivery = sampleNotification("dlv", appName = "Uber Eats", title = "Order Update", text = "Preparing order 60%")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(delivery), now).state

        // User dismisses from Island
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("dlv"), now + 100L).state

        // Minor progress update arrives from source
        val update = delivery.copy(text = "Preparing order 70%")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(update), now + 500L)

        assertEquals(IslandDomainAction.SuppressMinorUpdate, decision.action)
        assertEquals(IslandPresentationTarget.IDLE, decision.state.targetPresentation)
        assertTrue("Minor update must remain suppressed", decision.state.queue.notifications.isEmpty())
    }

    // =========================================================================
    // Scenario A22: Delivery -> First '2 stops away' resurfaces once (UX-C09/C10)
    // =========================================================================
    @Test
    fun test_A22_userIslandDismissesDelivery_firstTwoStopsAway_resurfacesOnce() {
        val delivery = sampleNotification("dlv", appName = "Uber Eats", title = "Courier", text = "On the way")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(delivery), now).state
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("dlv"), now + 100L).state

        // First nearby milestone
        val nearby = delivery.copy(text = "Driver is nearby - 2 stops away")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(nearby), now + 1000L)

        assertEquals(IslandDomainAction.ResurfaceMilestone, decision.action)
        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, decision.state.targetPresentation)
        assertEquals("dlv", decision.state.selectedKey)
        assertEquals(1, decision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A23: Same delivery -> Subsequent stop decrement (2->1) not repeatedly resurfaced
    // =========================================================================
    @Test
    fun test_A23_sameDelivery_subsequentStopDecrement_notRepeatedlyResurrected() {
        val delivery = sampleNotification("dlv", appName = "Uber Eats", title = "Courier", text = "On the way")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(delivery), now).state
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("dlv"), now + 100L).state

        // First nearby milestone resurfaces
        val nearby2Stops = delivery.copy(text = "Driver is nearby - 2 stops away")
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(nearby2Stops), now + 1000L).state

        // User dismisses again or preview completes
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("dlv"), now + 1200L).state

        // Minor stop decrement arrives
        val nearby1Stop = delivery.copy(text = "Driver is nearby - 1 stop away")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(nearby1Stop), now + 2000L)

        assertEquals("Subsequent stop decrement must be suppressed to avoid spam", IslandDomainAction.SuppressMinorUpdate, decision.action)
    }

    // =========================================================================
    // Scenario A24: Same delivery -> Arriving now resurfaces meaningfully (UX-C09)
    // =========================================================================
    @Test
    fun test_A24_sameDelivery_arrivingNow_meaningfulResurfacing() {
        val delivery = sampleNotification("dlv", appName = "Uber Eats", title = "Courier", text = "On the way")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(delivery), now).state
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("dlv"), now + 100L).state

        val arriving = delivery.copy(text = "Driver is arriving now! Please meet outside.")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(arriving), now + 3000L)

        assertEquals(IslandDomainAction.ResurfaceMilestone, decision.action)
        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, decision.state.targetPresentation)
    }

    // =========================================================================
    // Scenario A27: Same-key messaging reply adds new body -> Resurface only on new content
    // =========================================================================
    @Test
    fun test_A27_sameKeyMessagingReply_newTimestampAndBody_resurfacesOnlyOnGenuinelyNewContent() {
        val chat = sampleNotification("chat_bob", appName = "WhatsApp", title = "Bob", text = "Hey there")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(chat), now).state
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("chat_bob"), now + 100L).state

        // Repaint with identical text and timestamp
        val repaint = chat.copy(timestamp = now)
        val repaintDecision = engine.reduce(state, IslandEngineEvent.IngestNotification(repaint), now + 200L)
        assertEquals(IslandDomainAction.SuppressMinorUpdate, repaintDecision.action)

        // Genuinely new message arrives in same thread
        val newMsg = chat.copy(text = "Are you free tonight?", timestamp = now + 5000L)
        val newDecision = engine.reduce(state, IslandEngineEvent.IngestNotification(newMsg), now + 5000L)
        assertEquals(IslandDomainAction.ResurfaceMilestone, newDecision.action)
        assertEquals("Are you free tonight?", newDecision.state.queue.notifications.first().text)
    }

    // =========================================================================
    // Scenario A28: Source removes key, later reposts genuinely new key -> Treated as new
    // =========================================================================
    @Test
    fun test_A28_sourceRemovesKey_laterRepostsGenuinelyNewKey_treatedAsNewNotification() {
        val notif = sampleNotification("temp_key", text = "Alert 1")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif), now).state
        // User dismisses
        state = engine.reduce(state, IslandEngineEvent.TapCloseButton("temp_key"), now + 100L).state
        assertNotNull(state.tombstones["temp_key"])

        // Source explicitly removes key from Android system shade
        state = engine.reduce(state, IslandEngineEvent.RemoveNotification("temp_key"), now + 200L).state
        assertNull("Tombstone must be cleared upon explicit source cancellation", state.tombstones["temp_key"])

        // Later source reposts
        val repost = sampleNotification("temp_key", text = "Alert 1 Reposted")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(repost), now + 1000L)

        assertEquals(IslandDomainAction.ExpandNotification, decision.action)
        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, decision.state.targetPresentation)
    }

    // =========================================================================
    // Scenario A35: Child expires untouched -> Visually merges into main, queue unchanged (UX-C15)
    // =========================================================================
    @Test
    fun test_A35_childExpiresUntouched_visuallyMerges_queueUnchanged() {
        val notif1 = sampleNotification("n1")
        val notif2 = sampleNotification("n2")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif1), now).state
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(notif2), now + 500L).state

        assertEquals(1, state.childNotices.size)
        assertEquals(2, state.queue.notifications.size)

        // Child timeout fires
        val decision = engine.reduce(state, IslandEngineEvent.ChildTimeout("n2"), now + 6000L)

        assertTrue("Child notice must visually merge and be removed from satellites", decision.state.childNotices.isEmpty())
        assertEquals("Notification must remain in canonical queue after child visual expiry", 2, decision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario A36: New child enters while old child near timeout -> Old timer not restarted
    // =========================================================================
    @Test
    fun test_A36_newChildEntersWhileOldChildNearTimeout_independentTimerNotRestarted() {
        val notif1 = sampleNotification("main")
        val child1 = sampleNotification("c1")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif1), now).state
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(child1), now + 500L).state

        val c1Expiry = state.childNotices.first { it.key == "c1" }.expiresAtMonotonic

        // Arrive child2 at T+4000
        val child2 = sampleNotification("c2")
        val decision = engine.reduce(state, IslandEngineEvent.IngestNotification(child2), now + 4000L)

        val updatedC1Expiry = decision.state.childNotices.first { it.key == "c1" }.expiresAtMonotonic
        val c2Expiry = decision.state.childNotices.first { it.key == "c2" }.expiresAtMonotonic

        assertEquals("Older child expiry timestamp must not be restarted by newer arrival", c1Expiry, updatedC1Expiry)
        assertTrue("Newer child must receive independent lifetime", c2Expiry > updatedC1Expiry)
    }

    // =========================================================================
    // Scenario A40: Burst of 25 notifications -> Queue bounded and safe
    // =========================================================================
    @Test
    fun test_A40_burstOfTwentyFiveNotifications_queueBoundedAndSafe() {
        var state = IslandBehaviorState()
        for (i in 1..25) {
            val n = sampleNotification("burst_$i", timestamp = now + i * 10L)
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(n), now + i * 10L).state
        }

        // Queue is bounded to 20 notifications (MAX_NOTIFICATIONS) to avoid unbounded growth
        assertEquals(20, state.queue.notifications.size)
        assertNotNull(state.selectedKey)
        // First became transient preview, remaining child notices queued
        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, state.targetPresentation)
        assertEquals(24, state.childNotices.size)
    }

    // =========================================================================
    // Scenario U01: Swipe up expanded notification -> Collapse only, queue and OS shade unchanged
    // =========================================================================
    @Test
    fun test_U01_swipeUpExpandedNotification_collapseOnly_osShadeAndQueueUnchanged() {
        val notif = sampleNotification("readMe")
        var state = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notif), now).state
        state = engine.reduce(state, IslandEngineEvent.TapCompact, now).state

        assertEquals(IslandPresentationTarget.USER_EXPANDED, state.targetPresentation)

        val decision = engine.reduce(state, IslandEngineEvent.SwipeUp, now + 100L)

        assertEquals(IslandDomainAction.CollapseToCompact, decision.action)
        assertEquals(IslandPresentationTarget.COMPACT, decision.state.targetPresentation)
        assertEquals(IslandInteractionOwnership.SYSTEM_IDLE, decision.state.ownership)
        assertEquals(1, decision.state.queue.notifications.size)
        assertTrue("Swipe up must never cancel Android notification", decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario U04: Compact notification double tapped -> Only single expand target state
    // =========================================================================
    @Test
    fun test_U04_compactNotificationRapidlyDoubleTapped_onlyIntendedSingleAction() {
        val notif = sampleNotification("target")
        val state = IslandBehaviorState(
            queue = NotificationQueueState(notifications = listOf(notif)),
            targetPresentation = IslandPresentationTarget.COMPACT
        )

        val decision = engine.reduce(state, IslandEngineEvent.DoubleTapCompact, now)

        assertEquals(IslandDomainAction.ExpandNotification, decision.action)
        assertEquals(IslandPresentationTarget.USER_EXPANDED, decision.state.targetPresentation)
        assertTrue("Double-tap must not emit OS cancellations or unintended actions",
            decision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    // =========================================================================
    // Scenario C05: Ongoing music + Normal message -> Brief message preview then music compact
    // =========================================================================
    @Test
    fun test_C05_ongoingMusicAndNormalMessage_preservesMusicAndShowsPreview() {
        val musicState = IslandState.Music(MusicTrack("Song", "Artist"))
        val baseState = IslandBehaviorState(
            activeDomain = musicState,
            targetPresentation = IslandPresentationTarget.COMPACT
        )

        val msg = sampleNotification("text_msg")
        val previewDecision = engine.reduce(baseState, IslandEngineEvent.IngestNotification(msg), now)

        assertEquals(IslandPresentationTarget.TRANSIENT_PREVIEW, previewDecision.state.targetPresentation)
        assertEquals("text_msg", previewDecision.state.selectedKey)
        val token = previewDecision.state.activeToken

        // Preview timeout returns to compact preserving music domain
        val timeoutDecision = engine.reduce(previewDecision.state, IslandEngineEvent.PreviewTimeout(token), now + 3500L)

        assertEquals(IslandDomainAction.CollapseToCompact, timeoutDecision.action)
        assertEquals(IslandPresentationTarget.COMPACT, timeoutDecision.state.targetPresentation)
        assertEquals(musicState, timeoutDecision.state.activeDomain)
        assertEquals(1, timeoutDecision.state.queue.notifications.size)
    }

    // =========================================================================
    // Scenario P05: Listener restarts with active notifications -> Reconcile keys deduplicated
    // =========================================================================
    @Test
    fun test_P05_listenerRestartsWithActiveNotifications_reconcileKeysWithoutDuplicates() {
        var state = IslandBehaviorState()
        listOf("A", "B", "C").forEach { id ->
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(sampleNotification(id)), now).state
        }
        assertEquals(3, state.queue.notifications.size)

        // Active set reported by system after reconnect: B and C remain, A was cleared, D is new
        val activeKeys = setOf("B", "C", "D")
        val decision = engine.reduce(state, IslandEngineEvent.ReconcileSources(activeKeys), now + 1000L)

        val remainingIds = decision.state.queue.notifications.map { it.id }
        assertFalse("A must be pruned", remainingIds.contains("A"))
        assertTrue("B must be preserved", remainingIds.contains("B"))
        assertTrue("C must be preserved", remainingIds.contains("C"))
        assertEquals("No duplicate keys allowed", remainingIds.distinct().size, remainingIds.size)
    }

    // =========================================================================
    // Negative Assertions: Guaranteeing Non-Destructive Invariants
    // =========================================================================
    @Test
    fun test_negativeInvariants_zeroUnintendedOsCancellations() {
        val notif1 = sampleNotification("item1")
        val notif2 = sampleNotification("item2")
        var state = IslandBehaviorState()
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(notif1), now).state
        state = engine.reduce(state, IslandEngineEvent.IngestNotification(notif2), now).state

        // 1. Outside tap
        val outsideDecision = engine.reduce(state, IslandEngineEvent.OutsideTap, now)
        assertTrue(outsideDecision.effects.none { it is IslandSideEffect.CancelAndroidNotification })

        // 2. Swipe up
        val swipeDecision = engine.reduce(state, IslandEngineEvent.SwipeUp, now)
        assertTrue(swipeDecision.effects.none { it is IslandSideEffect.CancelAndroidNotification })

        // 3. Page navigation
        val nextDecision = engine.reduce(state, IslandEngineEvent.NextPage, now)
        assertTrue(nextDecision.effects.none { it is IslandSideEffect.CancelAndroidNotification })

        // 4. Default close button (Island-only)
        val closeDecision = engine.reduce(state, IslandEngineEvent.TapCloseButton("item1"), now)
        assertTrue(closeDecision.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }

    @Test
    fun test_negativeInvariants_selectedKeyStabilityOnOrderShifts() {
        var state = IslandBehaviorState()
        for (i in 1..4) {
            state = engine.reduce(state, IslandEngineEvent.IngestNotification(sampleNotification("n$i")), now).state
        }
        state = engine.reduce(state, IslandEngineEvent.SelectPage("n3"), now).state
        assertEquals("n3", state.selectedKey)

        // Adding an item must not change selectedKey
        val newDecision = engine.reduce(state, IslandEngineEvent.IngestNotification(sampleNotification("n_new")), now + 100L)
        assertEquals("n3", newDecision.state.selectedKey)

        // Removing non-selected item must not change selectedKey
        val removeDecision = engine.reduce(state, IslandEngineEvent.RemoveNotification("n1"), now + 200L)
        assertEquals("n3", removeDecision.state.selectedKey)
    }
}
