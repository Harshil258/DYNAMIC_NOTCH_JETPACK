package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationVisibilityStateTest {
    @Test
    fun activeNotificationsStartUnseenAndMarkingSeenDoesNotRemoveThem() {
        val state = NotificationVisibilityState()
        state.synchronize(listOf("a", "b", "c"))

        assertEquals(3, state.unseenCount())
        assertTrue(state.markSeen("a"))
        assertEquals(2, state.unseenCount())
        assertTrue(state.isActive("a"))
        assertTrue(state.isSeen("a"))
    }

    @Test
    fun updatesKeepReadStateAndNewSourcesBecomeUnseen() {
        val state = NotificationVisibilityState()
        state.synchronize(listOf("a", "b", "c"))
        state.markSeen("a")
        state.markSeen("b")

        // Same Android notification keys represent updates, not new unread items.
        state.synchronize(listOf("a", "b", "c"))
        assertEquals(1, state.unseenCount())

        // A newly posted source increments unseen without counting old seen entries.
        state.synchronize(listOf("a", "b", "c", "d"))
        assertEquals(2, state.unseenCount())
        assertTrue(state.isSeen("a"))
        assertFalse(state.isSeen("d"))
    }

    @Test
    fun removedSourcesAreForgottenAndARepostStartsUnseen() {
        val state = NotificationVisibilityState()
        state.synchronize(listOf("a", "b"))
        state.markSeen("a")
        state.synchronize(listOf("b"))
        assertFalse(state.isActive("a"))
        assertFalse(state.markSeen("a"))

        state.synchronize(listOf("a", "b"))
        assertEquals(2, state.unseenCount())
        assertFalse(state.isSeen("a"))
    }

    @Test
    fun sourceReconciliationDropsStaleReadAndUnreadState() {
        val state = NotificationVisibilityState()
        state.synchronize(listOf("a", "b", "c"))
        state.markSeen("a")
        state.synchronize(listOf("b"))

        assertEquals(setOf("b"), state.unseenIds())
        state.clear()
        assertEquals(0, state.unseenCount())
        assertTrue(state.unseenIds().isEmpty())
    }
}
