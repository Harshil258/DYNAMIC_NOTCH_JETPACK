package ai.emots.kishan_dynamic.ui.components

import org.junit.Assert.*
import org.junit.Test

class IslandFamilyGeometryTest {
    @Test fun everySourceStateHasGeometryAndSafeNarrowBounds() {
        assertEquals(IslandDemoState.entries.toSet(), IslandFamilyGeometry.targets.keys)
        IslandDemoState.entries.forEach { state ->
            listOf(80f, 320f,360f,393f,430f).forEach { width ->
                val g = IslandFamilyGeometry.resolve(state,width)
                assertTrue("$state at $width", g.width in 0f..width)
                assertTrue(g.radius <= minOf(g.width,g.height)/2)
                assertTrue(g.height > 0)
            }
        }
    }
    @Test fun atlasBaselineNeverUsesThe430Canvas() {
        val music = IslandFamilyGeometry.resolve(IslandDemoState.MusicExpanded,393f)
        assertEquals(367f,music.width,0.001f)
        assertEquals(177f,music.height,0.001f)
        assertEquals(42f,music.radius,0.001f)
        assertEquals(166f,IslandFamilyGeometry.resolve(IslandDemoState.CallExpanded,393f).height,0.001f)
    }
    @Test fun minimalHasIndependentOwnerGeometryWithoutCompanion() {
        val g = IslandFamilyGeometry.resolve(IslandDemoState.Minimal,393f)
        assertEquals(IslandSurfaceGeometry(108f,32f,15.9018f),g)
        assertFalse(IslandDemoState.Minimal.usesCompanionBubble())
    }
    @Test fun realCutoutAndMeasuredLargeContentCannotBeClippedToReference() {
        val g=IslandFamilyGeometry.resolve(IslandDemoState.Minimal,320f,cutoutWidth=130f,cutoutHeight=40f)
        assertEquals(130f,g.width,0.001f); assertEquals(40f,g.height,0.001f)
        val large=IslandFamilyGeometry.resolve(IslandDemoState.NotificationExpanded,320f,fontScale=2f,contentHeight=240f)
        assertEquals(240f,large.height,0.001f); assertEquals(42f,large.radius,0.001f)
    }
    @Test fun compactDoesNotScaleWithCanvasAndImageIsNotCompact() {
        assertEquals(222f,IslandFamilyGeometry.resolve(IslandDemoState.TimerCompact,430f).width,0.001f)
        assertEquals(IslandGeometryFamily.ContextualAlert,IslandFamilyGeometry.target(IslandDemoState.TimerImage).family)
    }
}
