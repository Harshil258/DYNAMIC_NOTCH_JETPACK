package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationInterruptionPolicyTest {

    @Test
    fun quickControlsRemainVisible() {
        assertDecision(
            IslandState.ActionControl(notificationCount = 2),
            NotificationPresentationDecision.KEEP_QUICK_CONTROLS
        )
    }

    @Test
    fun expandedNotificationUpdatesWithoutChangingUserSurface() {
        assertDecision(
            IslandState.Notification(listOf(notification()), isExpanded = true),
            NotificationPresentationDecision.UPDATE_EXPANDED_NOTIFICATION
        )
    }

    @Test
    fun compactMusicCanBecomeNotificationMusicSplit() {
        assertDecision(
            IslandState.Music(MusicTrack("Song", "Artist")),
            NotificationPresentationDecision.PRESENT
        )
    }

    @Test
    fun expandedMusicDefersNotification() {
        assertDecision(
            IslandState.Music(MusicTrack("Song", "Artist"), isExpanded = true),
            NotificationPresentationDecision.DEFER
        )
    }

    @Test
    fun liveWorkAndCallsCannotBeReplaced() {
        assertDecision(
            IslandState.LiveActivity(
                LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer", "01:00")
            ),
            NotificationPresentationDecision.DEFER
        )
        assertDecision(
            IslandState.IncomingCall(ContactInfo("Alice", "+1")),
            NotificationPresentationDecision.DEFER
        )
        assertDecision(
            IslandState.OngoingCall(ContactInfo("Alice", "+1")),
            NotificationPresentationDecision.DEFER
        )
    }

    @Test
    fun transientConfirmationFinishesBeforeNotificationAppears() {
        assertDecision(
            IslandState.Charging(batteryPercent = 80),
            NotificationPresentationDecision.DEFER
        )
    }

    @Test
    fun authoritativeAlarmCanInterruptControlsButNotACall() {
        val alarm = notification().copy(category = "alarm", title = "Wake up")
        assertEquals(
            NotificationPresentationDecision.PRESENT,
            NotificationInterruptionPolicy.decide(IslandState.ActionControl(), alarm)
        )
        assertEquals(
            NotificationPresentationDecision.DEFER,
            NotificationInterruptionPolicy.decide(
                IslandState.OngoingCall(ContactInfo("Alice", "+1")),
                alarm
            )
        )
    }

    @Test
    fun ordinaryNotificationCannotReplaceVisibleCriticalAlert() {
        val alarm = notification().copy(id = "alarm", category = "alarm", title = "Wake up")
        assertEquals(
            NotificationPresentationDecision.DEFER,
            NotificationInterruptionPolicy.decide(
                IslandState.Notification(listOf(alarm)),
                notification().copy(id = "message-2")
            )
        )
    }

    private fun assertDecision(
        state: IslandState,
        expected: NotificationPresentationDecision
    ) {
        assertEquals(expected, NotificationInterruptionPolicy.decide(state))
    }

    private fun notification() = NotificationInfo(
        id = "message",
        packageName = "test.messages",
        appName = "Messages",
        title = "Alice",
        text = "Hello"
    )
}
