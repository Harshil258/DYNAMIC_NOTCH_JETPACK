package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandPresentationOwnershipPolicyTest {

    @Test fun automaticPeekIsNotUserOwned() {
        assertFalse(IslandPresentationOwnershipPolicy.isUserOwned(
            IslandState.Notification(listOf(notification()), isExpanded = true, expandedByUser = false)
        ))
    }

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

    @Test
    fun transientAlertsAreNeverUserOwnedEvenWhenExpanded() {
        val mobileDataAlert = IslandState.LiveActivity(
            LiveActivityInfo("net", LiveActivityKind.MOBILE_DATA_ALERT, "Mobile Data", isExpanded = true, expandedByUser = false)
        )
        assertFalse(IslandPresentationOwnershipPolicy.isUserOwned(mobileDataAlert))

        val airplaneAlert = IslandState.LiveActivity(
            LiveActivityInfo("air", LiveActivityKind.AIRPLANE_ALERT, "Airplane", isExpanded = true, expandedByUser = true)
        )
        assertFalse(IslandPresentationOwnershipPolicy.isUserOwned(airplaneAlert))
    }

    @Test
    fun transientAlertsDoNotBlockTransientConfirmations() {
        val mobileDataAlert = IslandState.LiveActivity(
            LiveActivityInfo("net", LiveActivityKind.MOBILE_DATA_ALERT, "Mobile Data", isExpanded = false)
        )
        assertFalse(IslandPresentationOwnershipPolicy.blocksTransient(mobileDataAlert))
    }

    private fun notification() = NotificationInfo(
        id = "message",
        packageName = "test.messages",
        appName = "Messages",
        title = "Alice",
        text = "Hello"
    )
}
