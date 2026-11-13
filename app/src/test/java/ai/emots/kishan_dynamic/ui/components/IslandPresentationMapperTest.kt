package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class IslandPresentationMapperTest {
    @Test fun unsupportedCompactActivitiesNeverExposeDiagnosticRedOwner() {
        val dedicated = setOf(LiveActivityKind.TIMER, LiveActivityKind.DELIVERY, LiveActivityKind.FLIGHT,
            LiveActivityKind.SPORTS, LiveActivityKind.NAVIGATION)
        LiveActivityKind.entries.filterNot { it in dedicated }.forEach { kind ->
            val state = IslandState.LiveActivity(LiveActivityInfo("source", kind, "Activity", isExpanded = false))
            assertEquals(kind.name, IslandDemoState.Idle, resolveOverlayDemoState(state, 0))
        }
    }

    @Test fun navigationExpandedMapsToTransportActivityNeverColorOptions() {
        val navExpanded = IslandState.LiveActivity(LiveActivityInfo("nav_pkg", LiveActivityKind.NAVIGATION, "Navigation", isExpanded = true))
        val resolved = resolveOverlayDemoState(navExpanded, 0)
        assertEquals(IslandDemoState.TransportActivity, resolved)
        org.junit.Assert.assertNotEquals(IslandDemoState.ColorOptions, resolved)
    }

    @Test fun incomingCallMapsToCallAvatarsForRingingPresentation() {
        val incoming = IslandState.IncomingCall(ContactInfo("Alice Smith", "+1234567890"), declineActionId = "action_decline")
        val resolved = resolveOverlayDemoState(incoming, 0)
        assertEquals(IslandDemoState.CallAvatars, resolved)
    }

    @Test fun ongoingConnectedCallMapsToCallExpandedWhenExpanded() {
        val ongoingExpanded = IslandState.OngoingCall(ContactInfo("Bob Jones", "+1987654321"), durationSeconds = 120, isExpanded = true)
        val resolved = resolveOverlayDemoState(ongoingExpanded, 0)
        assertEquals(IslandDemoState.CallExpanded, resolved)
    }

    @Test fun ongoingConnectedCallMapsToCallCompactWhenCompact() {
        val ongoingCompact = IslandState.OngoingCall(ContactInfo("Bob Jones", "+1987654321"), durationSeconds = 120, isExpanded = false)
        val resolved = resolveOverlayDemoState(ongoingCompact, 0)
        assertEquals(IslandDemoState.CallCompact, resolved)
    }
}
