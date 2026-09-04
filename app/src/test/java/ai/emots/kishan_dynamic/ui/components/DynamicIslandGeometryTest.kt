package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.theme.AppIslandTokens
import ai.emots.kishan_dynamic.data.model.IslandState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicIslandGeometryTest {
    private val tokens = AppIslandTokens()

    @Test
    fun allCollapsedStatesUseTheSharedCompactClassification() {
        val compactStates = IslandDemoState.entries.filter { it.isCompactPresentation() }

        assertEquals(13, compactStates.size)
        assertTrue(IslandDemoState.Idle.isCompactPresentation())
        assertTrue(IslandDemoState.Minimal.isCompactPresentation())
        assertTrue(IslandDemoState.MusicCompact.isCompactPresentation())
    }

    @Test
    fun everyCollapsedStateResolvesToExactlyOneHeightToken() {
        IslandDemoState.entries
            .filter { it.isCompactPresentation() }
            .forEach { state ->
                assertEquals(tokens.compactHeight, state.resolvedHeight(tokens))
            }
    }

    @Test
    fun repeatedNotificationCapsulesResolveToThe86PointFamily() {
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.TimerExpanded.resolvedHeight(tokens))
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.NotificationExpanded.resolvedHeight(tokens))
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.CallAvatars.resolvedHeight(tokens))
    }

    @Test
    fun minimalServiceStateUsesTheSplitPresentation() {
        assertEquals(IslandDemoState.Minimal, mapIslandStateToDemoState(IslandState.Minimal))
    }
}
