package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandPresentationOwnershipPolicyTest {

    @Test
    fun controlsAndExpandedContentAreUserOwned() {
        assertTrue(IslandPresentationOwnershipPolicy.isUserOwned(IslandState.ActionControl()))
        assertTrue(
            IslandPresentationOwnershipPolicy.isUserOwned(
                IslandState.Notification(listOf(notification()), isExpanded = true)
            )
        )
        assertTrue(
            IslandPresentationOwnershipPolicy.isUserOwned(
                IslandState.Music(MusicTrack("Song", "Artist"), isExpanded = true)
            )
        )
        assertTrue(
            IslandPresentationOwnershipPolicy.isUserOwned(
                IslandState.LiveActivity(
                    LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer", "01:00", isExpanded = true)
                )
            )
        )
    }

    @Test
    fun compactContentIsSourceOwned() {
        assertFalse(IslandPresentationOwnershipPolicy.isUserOwned(IslandState.Minimal))
        assertFalse(
            IslandPresentationOwnershipPolicy.isUserOwned(
                IslandState.Notification(listOf(notification()), isExpanded = false)
            )
        )
        assertFalse(
            IslandPresentationOwnershipPolicy.isUserOwned(
                IslandState.Music(MusicTrack("Song", "Artist"), isExpanded = false)
            )
        )
    }

    @Test
    fun liveWorkBlocksTransientConfirmationsEvenWhenCompact() {
        assertTrue(
            IslandPresentationOwnershipPolicy.blocksTransient(
                IslandState.LiveActivity(
                    LiveActivityInfo("navigation", LiveActivityKind.NAVIGATION, "Turn left")
                )
            )
        )
        assertFalse(
            IslandPresentationOwnershipPolicy.blocksTransient(
                IslandState.Music(MusicTrack("Song", "Artist"), isExpanded = false)
            )
        )
    }

    private fun notification() = NotificationInfo(
        id = "message",
        packageName = "test.messages",
        appName = "Messages",
        title = "Alice",
        text = "Hello"
    )
}
