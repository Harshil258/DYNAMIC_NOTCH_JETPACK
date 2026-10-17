package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.IslandPresentationMode
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.defaultTransientDurationMillis
import ai.emots.kishan_dynamic.data.model.isOngoing
import ai.emots.kishan_dynamic.data.model.isTransientAlert
import ai.emots.kishan_dynamic.data.model.presentationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveActivityLifecyclePolicyTest {

    @Test
    fun ongoingActivitiesVersusTransientAlertsClassification() {
        // Genuine ongoing background tasks
        assertFalse(LiveActivityKind.TIMER.isTransientAlert)
        assertFalse(LiveActivityKind.NAVIGATION.isTransientAlert)
        assertFalse(LiveActivityKind.VOICE_MEMO.isTransientAlert)
        assertFalse(LiveActivityKind.SCREEN_RECORDING.isTransientAlert)
        assertFalse(LiveActivityKind.SCREEN_MIRRORING_ALERT.isTransientAlert)
        assertFalse(LiveActivityKind.DELIVERY.isTransientAlert)
        assertFalse(LiveActivityKind.FLIGHT.isTransientAlert)
        assertFalse(LiveActivityKind.SPORTS.isTransientAlert)

        // Temporary status announcements
        assertTrue(LiveActivityKind.MOBILE_DATA_ALERT.isTransientAlert)
        assertTrue(LiveActivityKind.AIRPLANE_ALERT.isTransientAlert)
        assertTrue(LiveActivityKind.AIRPODS.isTransientAlert)
        assertTrue(LiveActivityKind.FIND_MY.isTransientAlert)
        assertTrue(LiveActivityKind.MOVED_TO_IPHONE.isTransientAlert)
        assertTrue(LiveActivityKind.SHORTCUT.isTransientAlert)
    }

    @Test
    fun defaultTransientDurationsAreCalibrated() {
        assertEquals(4000L, LiveActivityKind.MOBILE_DATA_ALERT.defaultTransientDurationMillis)
        assertEquals(4000L, LiveActivityKind.AIRPLANE_ALERT.defaultTransientDurationMillis)
        assertEquals(3000L, LiveActivityKind.AIRPODS.defaultTransientDurationMillis)
        assertEquals(2500L, LiveActivityKind.SHORTCUT.defaultTransientDurationMillis)
    }

    @Test
    fun presentationModesAreExplicitlyReflected() {
        assertEquals(IslandPresentationMode.HIDDEN, IslandState.Hidden.presentationMode)
        assertEquals(IslandPresentationMode.COMPACT, IslandState.Minimal.presentationMode)

        val compactActivity = IslandState.LiveActivity(
            LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer", isExpanded = false)
        )
        assertEquals(IslandPresentationMode.COMPACT, compactActivity.presentationMode)

        val expandedActivity = IslandState.LiveActivity(
            LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer", isExpanded = true)
        )
        assertEquals(IslandPresentationMode.EXPANDED, expandedActivity.presentationMode)
    }

    @Test
    fun ongoingActivityStateReporting() {
        val timer = IslandState.LiveActivity(
            LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer")
        )
        assertTrue(timer.isOngoing)

        val mobileDataAlert = IslandState.LiveActivity(
            LiveActivityInfo("net", LiveActivityKind.MOBILE_DATA_ALERT, "Mobile Data")
        )
        assertFalse(mobileDataAlert.isOngoing)

        val airplaneAlert = IslandState.LiveActivity(
            LiveActivityInfo("plane", LiveActivityKind.AIRPLANE_ALERT, "Airplane Mode")
        )
        assertFalse(airplaneAlert.isOngoing)
    }

    @Test
    fun transientAlertsAreNotStickyUnderOwnershipPolicy() {
        val userOwnedTimer = IslandState.LiveActivity(
            LiveActivityInfo("timer", LiveActivityKind.TIMER, "Timer", isExpanded = true, expandedByUser = true)
        )
        assertTrue(ai.emots.kishan_dynamic.data.model.IslandPresentationOwnershipPolicy.isUserOwned(userOwnedTimer))

        val transientAlert = IslandState.LiveActivity(
            LiveActivityInfo("net", LiveActivityKind.MOBILE_DATA_ALERT, "Mobile Data", isExpanded = true, expandedByUser = true)
        )
        assertFalse(ai.emots.kishan_dynamic.data.model.IslandPresentationOwnershipPolicy.isUserOwned(transientAlert))
        assertFalse(ai.emots.kishan_dynamic.data.model.IslandPresentationOwnershipPolicy.blocksTransient(transientAlert))
    }
}
