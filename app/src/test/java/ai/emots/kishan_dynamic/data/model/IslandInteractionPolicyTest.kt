package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class IslandInteractionPolicyTest {

    @Test
    fun mainAndCompanionOwnDifferentSourcesInMusicNotificationSplit() {
        val playing = IslandState.NotificationWithMusic(
            notifications = listOf(notification("one")),
            track = MusicTrack("Song", "Artist"),
            isPlaying = true
        )
        assertEquals(IslandInteractionAction.OPEN_MUSIC, IslandInteractionPolicy.mainTap(playing))
        assertEquals(IslandInteractionAction.OPEN_NOTIFICATIONS, IslandInteractionPolicy.companionTap(playing))

        val paused = playing.copy(isPlaying = false)
        assertEquals(IslandInteractionAction.OPEN_NOTIFICATIONS, IslandInteractionPolicy.mainTap(paused))
        assertEquals(IslandInteractionAction.OPEN_MUSIC, IslandInteractionPolicy.companionTap(paused))
    }

    @Test
    fun stackedCompanionOpensNextNotificationInPager() {
        val state = IslandState.Notification(
            notifications = listOf(notification("one"), notification("two"))
        )
        assertEquals(
            IslandInteractionAction.OPEN_NEXT_NOTIFICATION,
            IslandInteractionPolicy.companionTap(state)
        )
    }

    @Test
    fun compactLongPressExpandsItsSourceInsteadOfOpeningControls() {
        val music = IslandState.Music(MusicTrack("Song", "Artist"))
        assertEquals(IslandInteractionAction.OPEN_MUSIC, IslandInteractionPolicy.mainLongPress(music))

        val notification = IslandState.Notification(listOf(notification("one")))
        assertEquals(
            IslandInteractionAction.OPEN_NOTIFICATIONS,
            IslandInteractionPolicy.mainLongPress(notification)
        )
    }

    @Test
    fun expandedBackgroundTapDoesNothing() {
        val expanded = IslandState.Notification(listOf(notification("one")), isExpanded = true)
        assertEquals(IslandInteractionAction.NONE, IslandInteractionPolicy.mainTap(expanded))
        assertEquals(IslandInteractionAction.NONE, IslandInteractionPolicy.mainLongPress(expanded))
    }

    private fun notification(id: String) = NotificationInfo(
        id = id,
        packageName = "test.$id",
        appName = "Messages",
        title = id,
        text = "Hello"
    )
}
