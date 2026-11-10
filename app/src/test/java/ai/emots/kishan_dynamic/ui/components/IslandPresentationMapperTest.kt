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
}
