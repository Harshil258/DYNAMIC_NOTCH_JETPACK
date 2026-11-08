package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.ui.components.childPresentation
import org.junit.Assert.*
import org.junit.Test

class LayeredMiniIslandsTest {
    private val engine = IslandBehaviorEngine()
    private fun notice(id: String, time: Long = 1) = NotificationInfo(id = id, packageName = "mock.chat", appName = "Chat", title = "Sender $id", text = "Preview", timestamp = time)
    private fun reading(old: Int = 1): IslandBehaviorState {
        var state = IslandBehaviorState()
        repeat(old) { state = engine.reduce(state, IslandEngineEvent.IngestNotification(notice("old-$it", it.toLong()), autoExpand = false), 0).state }
        return engine.reduce(state, IslandEngineEvent.TapCompact, 0).state
    }
    private fun arrivals(state: IslandBehaviorState, count: Int): IslandBehaviorState {
        var result = state
        repeat(count) { result = engine.reduce(result, IslandEngineEvent.IngestNotification(notice("new-$it", 100 + it.toLong())), 100 + it.toLong()).state }
        return result
    }
    @Test fun oldQueueIsNotPendingOverflow() {
        val start = reading(12); val s = arrivals(start, 4)
        assertEquals(start.selectedKey, s.selectedKey)
        assertEquals(16, s.canonicalQueueCount); assertEquals(4, s.pendingChildNoticesCount)
        val p = s.childPresentation(220, 1f, false)
        assertEquals(listOf("new-3", "new-2"), p.children.map { it.key }); assertEquals(2, p.overflow)
    }
    @Test fun adaptiveLimitsHonorHeightAndLargeText() {
        val s = arrivals(reading(), 10)
        assertEquals(1, s.childPresentation(190, 1f, false).children.size)
        assertEquals(2, s.childPresentation(250, 1f, false).children.size)
        assertEquals(3, s.childPresentation(800, 1f, false).children.size)
        assertEquals(1, s.childPresentation(250, 2f, false).children.size)
        assertEquals(0, s.childPresentation(80, 1f, false).children.size)
    }
    @Test fun burstBoundedAtCanonicalCapacityWithNoDuplicateKeys() {
        val s = arrivals(reading(), 250)
        assertEquals(50, s.canonicalQueueCount)
        assertTrue(s.pendingChildNoticesCount <= 49); assertTrue(s.childLifecycles.size <= 53)
        assertEquals(s.pendingChildNoticesCount, s.childNotices.map { it.key }.distinct().size)
        assertTrue(s.childNotices.all { c -> s.queue.notifications.any { it.id == c.key } })
    }
    @Test fun independentDeadlineSurvivesLaterArrivalAndSameKeyUpdate() {
        val s = arrivals(reading(), 1); val c = s.childNotices.single()
        val next = engine.reduce(s, IslandEngineEvent.IngestNotification(notice("other")), 2000).state
        val update = engine.reduce(next, IslandEngineEvent.IngestNotification(notice(c.key).copy(title = "Updated")), 2500).state
        assertEquals(c.expiresAtMonotonic, update.childNotices.first { it.key == c.key }.expiresAtMonotonic)
        assertEquals(c.token, update.childNotices.first { it.key == c.key }.token)
        assertEquals("Updated", update.childNotices.first { it.key == c.key }.title)
    }
    @Test fun touchPreservesRemainingTimeWithoutExtendingOtherChildren() {
        val s = arrivals(reading(), 2); val c = s.childNotices.last()
        val held = engine.reduce(s, IslandEngineEvent.TouchChild(c.key), 1100).state
        val released = engine.reduce(held, IslandEngineEvent.ReleaseChild(c.key), 9000)
        val timer = released.effects.filterIsInstance<IslandSideEffect.StartChildTimer>().single()
        assertEquals(6000, timer.durationMs)
        assertEquals(s.childNotices.first().expiresAtMonotonic, released.state.childNotices.first().expiresAtMonotonic)
        assertEquals(released.state, engine.reduce(released.state, IslandEngineEvent.ChildTimeout(c.key, c.token), 9500).state)
    }
    @Test fun expiringSiblingDoesNotUnpauseHeldChildOrCancelAndroid() {
        val s = arrivals(reading(), 2); val held = s.childNotices.first(); val other = s.childNotices.last()
        val touched = engine.reduce(s, IslandEngineEvent.TouchChild(held.key), 1000).state
        val expiry = engine.reduce(touched, IslandEngineEvent.ChildTimeout(other.key, other.token), 6000)
        assertEquals(3, expiry.state.canonicalQueueCount); assertTrue(expiry.state.childNotices.single().isPaused)
        assertTrue(expiry.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }
    @Test fun promotionSelectsExactlyOneExistingPagerIdentity() {
        val s = arrivals(reading(), 3); val target = s.childNotices.last().key
        val promoted = engine.reduce(s, IslandEngineEvent.TapChild(target), 400)
        assertEquals(target, promoted.state.selectedKey); assertEquals(target, promoted.state.queue.activeNotificationId)
        assertEquals(s.canonicalQueueCount, promoted.state.canonicalQueueCount)
        assertTrue(promoted.state.queue.notifications.any { it.id == s.selectedKey })
        assertTrue(promoted.state.childNotices.none { it.key == target })
        assertTrue(promoted.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }
    @Test fun stalePromotionAndReusedKeyCannotSelectRemovedSource() {
        val s = arrivals(reading(), 1); val c = s.childNotices.single()
        val removed = engine.reduce(s, IslandEngineEvent.RemoveNotification(c.key), 300).state
        assertEquals(removed, engine.reduce(removed, IslandEngineEvent.TapChild(c.key), 400).state)
        val reused = engine.reduce(removed, IslandEngineEvent.IngestNotification(notice(c.key, 500)), 500).state
        assertEquals(reused, engine.reduce(reused, IslandEngineEvent.ChildTimeout(c.key, c.token), 600).state)
    }
    @Test fun callRetainsLiveQueueWithoutResurrectingChildrenOrRemovedSources() {
        val s = arrivals(reading(), 2)
        val call = engine.reduce(s, IslandEngineEvent.IncomingCallPreempt, 200).state
        val removed = engine.reduce(call, IslandEngineEvent.RemoveNotification("new-0"), 300).state
        val during = engine.reduce(removed, IslandEngineEvent.IngestNotification(notice("during")), 400).state
        assertEquals(IslandInteractionOwnership.INTERRUPTED, during.ownership)
        val restored = engine.reduce(during, IslandEngineEvent.CallEnded, 9000).state
        assertTrue(restored.childNotices.isEmpty()); assertTrue(restored.childLifecycles.isEmpty()); assertTrue(restored.queue.notifications.none { it.id == "new-0" })
        assertTrue(restored.queue.notifications.any { it.id == "during" })
    }
    @Test fun replyAndCollapsePreserveQueueAndNeverEmitAndroidCancellation() {
        val s = engine.reduce(reading(), IslandEngineEvent.StartReply, 0).state
        val incoming = arrivals(s, 25)
        assertEquals(s.selectedKey, incoming.selectedKey); assertEquals(IslandInteractionOwnership.REPLY_ACTIVE, incoming.ownership)
        val collapse = engine.reduce(incoming, IslandEngineEvent.SwipeUp, 2000)
        assertEquals(26, collapse.state.canonicalQueueCount); assertTrue(collapse.state.childNotices.isEmpty())
        assertTrue(collapse.effects.none { it is IslandSideEffect.CancelAndroidNotification })
    }
    @Test fun privacyProjectionNeverExposesSensitiveText() {
        val s = arrivals(reading(), 1)
        val locked = engine.reduce(s, IslandEngineEvent.PrivacyChanged(ChildPrivacyPolicy(true, true)), 200).state
        val child = locked.childPresentation(300, 1f, false).children.single()
        assertEquals("New notification", child.text); assertFalse(child.title.contains("Sender"))
    }
    @Test fun touchingTransientChildClaimsReadingAndPublishesOverflowLimit() {
        var s = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(notice("main")), 0).state
        s = arrivals(s, 4)
        val held = engine.reduce(s, IslandEngineEvent.TouchChild("new-3"), 200)
        assertEquals(IslandInteractionOwnership.USER_EXPANDED, held.state.previousOwnership)
        assertTrue(held.effects.any { it is IslandSideEffect.CancelTimer })
        val layout = engine.reduce(held.state, IslandEngineEvent.ChildVisibleLimitChanged(1), 300).state
        assertEquals(1, layout.visibleChildLimit); assertEquals(3, layout.pendingChildOverflowCount)
        val removed = engine.reduce(layout, IslandEngineEvent.RemoveNotification("new-3"), 400).state
        assertEquals(IslandInteractionOwnership.USER_EXPANDED, removed.ownership)
    }

    @Test fun readableBudgetStartsOnceWithoutResettingSiblings() {
        val s = arrivals(reading(), 2)
        val child = s.childNotices.first()
        val settled = engine.reduce(s, IslandEngineEvent.ChildPhaseChanged(child.key, ChildLifecycle.VISIBLE), 1000)
        assertEquals(6000, settled.state.childNotices.first().expiresAtMonotonic)
        assertEquals(s.childNotices.last().expiresAtMonotonic, settled.state.childNotices.last().expiresAtMonotonic)
        val again = engine.reduce(settled.state, IslandEngineEvent.ChildPhaseChanged(child.key, ChildLifecycle.VISIBLE), 2000)
        assertEquals(6000, again.state.childNotices.first().expiresAtMonotonic)
        assertTrue(again.effects.none { it is IslandSideEffect.StartChildTimer })
    }

}
