package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.*
import org.junit.Test

/** Synthetic platform-shaped fixtures, not external-app integration tests. */
class NotificationEngineeringRegressionTest {
    private val reducer = NotificationReducer()
    private fun item(id: String, pkg: String = "test.messages", time: Long = 1) =
        NotificationInfo(id, pkg, "Test app", "Alice", "Hello", timestamp = time, groupKey = "shared")

    @Test fun groupNamesCannotMergeDifferentApplications() {
        val summary = item("summary", "test.mail").copy(isGroupSummary = true)
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(summary))
        state = reducer.reduce(state, NotificationEvent.Posted(item("message", "test.chat")))
        assertEquals(setOf("summary", "message"), state.notifications.map { it.id }.toSet())
        state = reducer.reduce(state, NotificationEvent.Posted(item("child", "test.mail")))
        assertEquals(setOf("message", "child"), state.notifications.map { it.id }.toSet())
    }

    @Test fun sourceCanRemovePrivateCopyAndArtworkOnUpdate() {
        val old = item("a").copy(expandedText = "Private", subText = "Secret",
            imagePath = "private.png", picturePath = "photo.png", inboxLines = listOf("Private"))
        val replacement = item("a").copy(title = "", text = "", expandedText = "")
        assertEquals(replacement, reducer.mergeNotification(old, replacement))
    }

    @Test fun updateToAnotherConversationKeepsSelection() {
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(item("a")))
        state = reducer.reduce(state, NotificationEvent.Posted(item("b", time = 2)))
        state = reducer.reduce(state, NotificationEvent.Selected("a"))
        state = reducer.reduce(state, NotificationEvent.Posted(item("b", time = 3).copy(text = "Updated")))
        assertEquals("a", state.activeNotificationId)
        assertEquals(2, state.notifications.size)
    }

    @Test fun quieterArrivalDoesNotStealHighImportanceSelection() {
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(
            item("urgent").copy(isPriority = true, importance = 4)))
        state = reducer.reduce(state, NotificationEvent.Posted(item("quiet", time = 100).copy(importance = 1)))
        assertEquals("urgent", state.activeNotificationId)
    }

    @Test fun largeOngoingBurstRemainsBoundedAndRetainsSelection() {
        var state = reducer.reduce(NotificationQueueState(), NotificationEvent.Posted(item("selected")))
        repeat(1000) { i ->
            state = reducer.reduce(state, NotificationEvent.Posted(
                item("ongoing-$i", time = i.toLong()).copy(isOngoing = true), isSilentUpdate = true))
            assertTrue(state.notifications.size <= NotificationReducer.MAX_NOTIFICATIONS)
            assertEquals("selected", state.activeNotificationId)
            assertTrue(state.activeIndex in state.notifications.indices)
            assertEquals(state.notifications.size, state.notifications.map { it.id }.toSet().size)
        }
        state.notifications.toList().forEach {
            state = reducer.reduce(state, NotificationEvent.Removed(it.id))
        }
        assertTrue(state.notifications.isEmpty())
        assertNull(state.activeNotification)
    }

    @Test fun multilingualEmptyAndDuplicateFixturesRemainIndependent() {
        val fixtures = listOf("", "👩🏽‍💻🎉", "مرحبا", "नमस्ते", "first\nsecond", "x".repeat(10000))
        var state = NotificationQueueState()
        fixtures.forEachIndexed { index, body ->
            val source = item("source-$index").copy(text = body)
            repeat(3) { state = reducer.reduce(state, NotificationEvent.Posted(source)) }
        }
        assertEquals(fixtures.size, state.notifications.size)
        assertEquals(fixtures.toSet(), state.notifications.map { it.text }.toSet())
    }

    @Test fun actionDispatchDoesNotClaimDeliveryAndFailsSafely() {
        var calls = 0
        assertEquals(NotificationDispatchOutcome.UNAVAILABLE,
            NotificationActionDispatch.attempt(false) { calls++ })
        assertEquals(0, calls)
        assertEquals(NotificationDispatchOutcome.FAILED,
            NotificationActionDispatch.attempt(true) { throw IllegalStateException("Expired target") })
        assertEquals(NotificationDispatchOutcome.HANDED_TO_APP,
            NotificationActionDispatch.attempt(true) { calls++ })
        assertEquals(1, calls)
    }

    @Test fun classificationUsesCapabilitiesNotBrandOrMessageProse() {
        val newsletter = item("news").copy(appName = "Maps newsletter", text = "Turn right at the exit to your ride")
        assertEquals(NotificationPresentationType.STANDARD, NotificationPresentationPolicy.classify(newsletter))
        assertEquals(NotificationPresentationType.STANDARD, NotificationPresentationPolicy.classify(item("media").copy(category = "transport")))
        assertEquals(NotificationPresentationType.NAVIGATION, NotificationPresentationPolicy.classify(item("nav").copy(category = "navigation", isOngoing = true)))
        assertEquals(NotificationPresentationType.PROGRESS, NotificationPresentationPolicy.classify(item("download").copy(progressMax = 100)))
        assertEquals(NotificationPresentationType.TIMER, NotificationPresentationPolicy.classify(item("timer").copy(showChronometer = true)))
        assertEquals(NotificationPresentationType.PICTURE, NotificationPresentationPolicy.classify(item("photo").copy(picturePath = "test.png")))
    }

    @Test fun publicOtpAndPaymentCopyStillReceivePrivacyProtection() {
        assertTrue(NotificationSensitiveContentPolicy.requiresProtection(false, "Verification code", "Test fixture"))
        assertTrue(NotificationSensitiveContentPolicy.requiresProtection(false, "Payment pending", "Test fixture"))
        assertTrue(NotificationSensitiveContentPolicy.requiresProtection(true, "", ""))
        assertFalse(NotificationSensitiveContentPolicy.requiresProtection(false, "Calendar", "Team meeting"))
    }

    @Test fun settledPagerIdentitySurvivesInsertionAndRejectsRemovedPage() {
        assertEquals(1, NotificationPagerSelectionPolicy.settledIndex(listOf("new", "selected", "old"), "selected"))
        assertEquals(0, NotificationPagerSelectionPolicy.settledIndex(listOf("selected", "old"), "selected"))
        assertNull(NotificationPagerSelectionPolicy.settledIndex(listOf("old"), "selected"))
        assertNull(NotificationPagerSelectionPolicy.settledIndex(emptyList(), "selected"))
        assertNull(NotificationPagerSelectionPolicy.settledIndex(listOf("a"), 0))
    }

    @Test fun replyEditorClosesOnRemovalRevocationOrActionReplacement() {
        val reply = ai.emots.kishan_dynamic.data.model.NotificationActionInfo("reply-v1", "Reply", true)
        val source = item("a").copy(actions = listOf(reply))
        assertTrue(NotificationReplyLifecyclePolicy.canKeepEditor("a", "reply-v1", listOf(source)))
        assertFalse(NotificationReplyLifecyclePolicy.canKeepEditor("a", "reply-v1", emptyList()))
        assertFalse(NotificationReplyLifecyclePolicy.canKeepEditor("a", "reply-v1", listOf(source.copy(actions = emptyList()))))
        assertFalse(NotificationReplyLifecyclePolicy.canKeepEditor("a", "reply-v1", listOf(source.copy(actions = listOf(reply.copy(id = "reply-v2"))))))
    }
}
