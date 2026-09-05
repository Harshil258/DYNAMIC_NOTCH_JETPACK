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
    fun deduplicatesSameSenderAndPreservesExistingActions() {
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

        assertEquals(1, state.notifications.size)
        val merged = state.notifications.first()
        assertEquals("Alice", merged.title)
        assertEquals("Are you free?", merged.text)
        assertEquals(1, merged.actions.size)
        assertEquals("Reply", merged.actions.first().label)
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
}
