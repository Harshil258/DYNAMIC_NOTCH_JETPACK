package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationReducerTest {
    private val reducer = NotificationReducer()

    private fun notification(id: String, priority: Boolean, time: Long) = NotificationInfo(
        id = id,
        packageName = "test.$id",
        appName = id,
        title = id,
        text = "message",
        timestamp = time,
        isPriority = priority
    )

    @Test
    fun priorityNotificationsBecomeActiveWithoutDroppingExistingItems() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("normal", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("priority", true, 10)))

        assertEquals(listOf("priority", "normal"), state.notifications.map { it.id })
        assertEquals("priority", state.activeNotificationId)
    }

    @Test
    fun repostReplacesTheSameNotification() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("download", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("download", false, 30)))

        assertEquals(1, state.notifications.size)
        assertEquals(30L, state.notifications.single().timestamp)
    }

    @Test
    fun removalRestoresTheNextNotification() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("one", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("two", false, 30)))
        state = reducer.reduce(state, NotificationEvent.Removed("two"))

        assertEquals(listOf("one"), state.notifications.map { it.id })
        assertEquals("one", state.activeNotificationId)
        assertTrue(state.activeNotification != null)
    }

    @Test
    fun removingAnotherNotificationPreservesExpandedSelection() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("first", false, 10)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("middle", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("last", false, 30)))
        state = reducer.reduce(state, NotificationEvent.Selected("middle"))

        state = reducer.reduce(state, NotificationEvent.Removed("last"))

        assertEquals("middle", state.activeNotificationId)
    }

    @Test
    fun removingSelectedNotificationChoosesNearestSurvivor() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("first", false, 10)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("middle", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("last", false, 30)))
        state = reducer.reduce(state, NotificationEvent.Selected("middle"))

        state = reducer.reduce(state, NotificationEvent.Removed("middle"))

        assertEquals("first", state.activeNotificationId)
    }

    @Test
    fun sameSenderWithDifferentAndroidKeysRemainsTwoSources() {
        var state = NotificationQueueState()
        val action = ai.emots.kishan_dynamic.data.model.NotificationActionInfo(id = "reply", label = "Reply", isReply = true)
        val initial = notification("msg1", false, 100).copy(
            packageName = "com.whatsapp",
            title = "Alice",
            text = "Hey there",
            actions = listOf(action)
        )
        state = reducer.reduce(state, NotificationEvent.Posted(initial))

        // WhatsApp sends follow-up with different key or empty actions
        val update = notification("msg2", false, 110).copy(
            packageName = "com.whatsapp",
            title = "Alice",
            text = "Are you free?",
            actions = emptyList() // Omitted in sub-update
        )
        state = reducer.reduce(state, NotificationEvent.Posted(update))

        assertEquals(2, state.notifications.size)
        assertEquals(listOf("msg2", "msg1"), state.notifications.map { it.id })
    }

    @Test
    fun exactUpdateRemovesActionsNoLongerExposedBySource() {
        val action = ai.emots.kishan_dynamic.data.model.NotificationActionInfo(
            id = "reply",
            label = "Reply",
            isReply = true
        )
        var state = reducer.reduce(
            NotificationQueueState(),
            NotificationEvent.Posted(notification("message", false, 100).copy(actions = listOf(action)))
        )

        state = reducer.reduce(
            state,
            NotificationEvent.Posted(notification("message", false, 110).copy(actions = emptyList()))
        )

        assertTrue(state.notifications.single().actions.isEmpty())
    }

    @Test
    fun dismissActiveRemovesActiveAndAdvancesToNext() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("first", false, 10)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("second", false, 20)))

        assertEquals("second", state.activeNotificationId)
        state = reducer.reduce(state, NotificationEvent.DismissActive)

        assertEquals(listOf("first"), state.notifications.map { it.id })
        assertEquals("first", state.activeNotificationId)
    }

    @Test
    fun queuePagingNextAndPreviousCycles() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("a", false, 10)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("b", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("c", false, 30)))

        // Initial active is newest: "c" (order: c, b, a)
        assertEquals("c", state.activeNotificationId)

        // Next moves to b
        state = reducer.reduce(state, NotificationEvent.Next)
        assertEquals("b", state.activeNotificationId)

        // Next moves to a
        state = reducer.reduce(state, NotificationEvent.Next)
        assertEquals("a", state.activeNotificationId)

        // Next wraps around to c
        state = reducer.reduce(state, NotificationEvent.Next)
        assertEquals("c", state.activeNotificationId)

        // Previous moves to a
        state = reducer.reduce(state, NotificationEvent.Previous)
        assertEquals("a", state.activeNotificationId)
    }

    @Test
    fun ongoingSilentUpdateUpdatesInPlaceWithoutOverridingActiveNotification() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("activeChat", true, 50)))
        val download = notification("dl", false, 40).copy(
            isOngoing = true,
            progress = 10,
            progressMax = 100
        )
        state = reducer.reduce(state, NotificationEvent.Posted(download))

        // Set active to the active chat
        state = reducer.reduce(state, NotificationEvent.Selected("activeChat"))
        assertEquals("activeChat", state.activeNotificationId)

        // Silent progress update for download
        val downloadProgress = download.copy(progress = 50)
        state = reducer.reduce(state, NotificationEvent.Posted(downloadProgress, isSilentUpdate = true))

        assertEquals("activeChat", state.activeNotificationId)
        val updatedDl = state.notifications.first { it.id == "dl" }
        assertEquals(50, updatedDl.progress)
    }

    @Test
    fun silentPostAddsNotificationWithoutChangingExpandedSelection() {
        var state = NotificationQueueState()
        state = reducer.reduce(state, NotificationEvent.Posted(notification("first", false, 10)))
        state = reducer.reduce(state, NotificationEvent.Posted(notification("selected", false, 20)))
        state = reducer.reduce(state, NotificationEvent.Selected("first"))

        state = reducer.reduce(
            state,
            NotificationEvent.Posted(
                notification = notification("new", false, 30),
                isSilentUpdate = true
            )
        )

        assertEquals("first", state.activeNotificationId)
        assertEquals(listOf("new", "selected", "first"), state.notifications.map { it.id })
    }

    @Test
    fun queueCapPreservesSelectedPageAndOngoingWork() {
        var state = NotificationQueueState()
        repeat(20) { index ->
            state = reducer.reduce(
                state,
                NotificationEvent.Posted(notification("item-$index", false, index.toLong()))
            )
        }
        state = reducer.reduce(state, NotificationEvent.Selected("item-0"))
        val ongoing = notification("navigation", true, 100).copy(isOngoing = true)

        state = reducer.reduce(
            state,
            NotificationEvent.Posted(ongoing, isSilentUpdate = true)
        )

        assertEquals(20, state.notifications.size)
        assertEquals("item-0", state.activeNotificationId)
        assertTrue(state.notifications.any { it.id == "navigation" })
        assertTrue(state.notifications.any { it.id == "item-0" })
    }

    @Test
    fun childNotificationReplacesGroupSummaryWithoutDuplication() {
        val summary = notification("summary", false, 10).copy(
            groupKey = "messages",
            isGroupSummary = true
        )
        val child = notification("child", false, 20).copy(groupKey = "messages")
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(summary))

        state = reducer.reduce(state, NotificationEvent.Posted(child))

        assertEquals(listOf("child"), state.notifications.map { it.id })
    }

    @Test
    fun lateGroupSummaryIsIgnoredWhenChildrenAlreadyExist() {
        val child = notification("child", false, 20).copy(groupKey = "messages")
        val summary = notification("summary", false, 30).copy(
            groupKey = "messages",
            isGroupSummary = true
        )
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(child))

        state = reducer.reduce(state, NotificationEvent.Posted(summary))

        assertEquals(listOf("child"), state.notifications.map { it.id })
    }
}
