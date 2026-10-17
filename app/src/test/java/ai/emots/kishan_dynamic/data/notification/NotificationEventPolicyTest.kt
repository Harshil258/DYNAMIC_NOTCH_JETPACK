package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.*
import org.junit.Test

class NotificationEventPolicyTest {
    private fun source() = NotificationInfo("fixture", "test.source", "Test", "", "")

    @Test fun launcherZeroProgressMetadataDoesNotQualifyAsActivity() {
        val launcher = source().copy(isOngoing = true, isClearable = false,
            progress = 0, progressMax = 0, importance = 3)
        assertEquals("background_without_activity", NotificationEventPolicy.evaluate(launcher).reason)
        assertEquals(NotificationEventKind.IGNORED, NotificationEventPolicy.evaluate(launcher).kind)
        // Not a package blacklist: an actual progress source is supported.
        assertEquals(NotificationEventKind.ONGOING,
            NotificationEventPolicy.evaluate(launcher.copy(progressMax = 100, progress = 10)).kind)
    }

    @Test fun silentSystemNoiseIsIgnoredWithoutCancelingSource() {
        listOf("usb", "debug", "search", "background").forEach { id ->
            assertEquals(NotificationEventKind.IGNORED,
                NotificationEventPolicy.evaluate(source().copy(id = id, importance = 1)).kind)
        }
    }

    @Test fun freshMessagesPaymentsMailAndUnknownSourcesAreTransient() {
        listOf("message", "payment", "email", "reminder", "social", "unknown").forEach { category ->
            assertEquals(NotificationEventKind.TRANSIENT,
                NotificationEventPolicy.evaluate(source().copy(category = category)).kind)
        }
        assertEquals(NotificationEventKind.TRANSIENT,
            NotificationEventPolicy.evaluate(source().copy(isClearable = false)).kind)
    }

    @Test fun genuineNavigationTimersTransfersAndMediaCanRemainCompact() {
        listOf("navigation", "timer", "stopwatch", "call", "transport").forEach { category ->
            assertEquals(NotificationEventKind.ONGOING,
                NotificationEventPolicy.evaluate(source().copy(category = category, isOngoing = true)).kind)
        }
        assertEquals(NotificationEventKind.ONGOING, NotificationEventPolicy.evaluate(
            source().copy(isOngoing = true, isProgressIndeterminate = true)).kind)
        assertEquals(NotificationEventKind.TRANSIENT, NotificationEventPolicy.evaluate(
            source().copy(isOngoing = true, progressMax = 100, progress = 100)).kind)
        assertEquals(NotificationEventKind.TRANSIENT, NotificationEventPolicy.evaluate(
            source().copy(isOngoing = true, progressMax = 100, progress = 100), recognizedActivity = true).kind)
    }

    @Test fun restartDoesNotReplayOldOrdinaryNotifications() {
        assertEquals(NotificationEventKind.IGNORED, NotificationEventPolicy.evaluate(source(), restored = true).kind)
        assertEquals(NotificationEventKind.ONGOING, NotificationEventPolicy.evaluate(
            source().copy(category = "navigation", isOngoing = true), restored = true).kind)
    }

    @Test fun blockedSourcesCannotBecomeActivitiesAndNewAlarmsRemainRelevant() {
        assertEquals(NotificationEventKind.IGNORED, NotificationEventPolicy.evaluate(
            source().copy(importance = 0, category = "navigation", isOngoing = true)).kind)
        assertEquals(NotificationEventKind.TRANSIENT, NotificationEventPolicy.evaluate(
            source().copy(importance = 1, category = "alarm")).kind)
    }

    @Test fun repeatedCallbacksNeverExtendPresentationDeadline() {
        var deadline: Long? = null
        repeat(500) { tick -> deadline = NotificationEventPolicy.deadline(deadline, tick * 10L, 4000L) }
        assertEquals(4000L, deadline)
        assertEquals(0L, NotificationEventPolicy.remainingMillis(deadline!!, 5000L))
        assertEquals(2000L, NotificationEventPolicy.remainingMillis(deadline!!, 2000L))
        assertEquals(9000L, NotificationEventPolicy.deadline(null, 5000L, 4000L))
    }

    @Test fun suspendedWorkCannotResurrectRemovedReplacedOrDisconnectedSources() {
        val epochs = NotificationSourceEpochs()
        val first = epochs.begin("a")
        assertTrue(epochs.isCurrent("a", first))
        val second = epochs.begin("a")
        assertFalse(epochs.isCurrent("a", first))
        epochs.remove("a")
        assertFalse(epochs.isCurrent("a", second))
        val repost = epochs.begin("a")
        assertFalse(epochs.isCurrent("a", first))
        assertTrue(epochs.isCurrent("a", repost))
        epochs.clear()
        assertFalse(epochs.isCurrent("a", repost))
    }

    @Test fun expiredUnchangedPreviewCannotReplayButNewContentAndRepostsCan() {
        val guard = NotificationReplayGuard()
        val first = source().copy(text = "Aa")
        assertFalse(guard.isDuplicate(first))
        assertTrue(guard.isDuplicate(first.copy(timestamp = 1000)))
        // Aa/BB collide under String.hashCode; content fingerprints must not.
        assertFalse(guard.isDuplicate(first.copy(text = "BB")))
        guard.forget(first.id)
        assertFalse(guard.isDuplicate(first))
        guard.clear()
        assertFalse(guard.isDuplicate(first))
    }
}
