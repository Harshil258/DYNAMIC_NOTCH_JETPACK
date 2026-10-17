package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.*
import org.junit.Test

class IslandSystemGeometryTest {
    @Test fun conceptualPhasesAreDerivedAndDoNotConfuseUnreadWithOngoing() {
        val source = NotificationInfo("a", "test.app", "App", "Sender", "Body")
        assertEquals(IslandPresentationPhase.IDLE, IslandState.Minimal.presentationPhase())
        assertEquals(IslandPresentationPhase.DISMISSING, IslandState.Hidden.presentationPhase(true))
        assertEquals(IslandPresentationPhase.TRANSIENT, IslandState.Notification(listOf(source)).presentationPhase())
        assertEquals(IslandPresentationPhase.EXPANDED, IslandState.Notification(listOf(source), isExpanded = true).presentationPhase())
        assertEquals(IslandPresentationPhase.COMPACT_ONGOING, IslandState.Music(MusicTrack("", "")).presentationPhase())
        assertEquals(IslandPresentationPhase.MINIMAL_MULTI_ACTIVITY,
            IslandState.NotificationWithMusic(listOf(source), MusicTrack("", "")).presentationPhase())
    }
    @Test fun systemWindowsWinAndOwnOverlayDoesNotSuppressItself() {
        assertTrue(OverlaySystemSurfacePolicy.shouldSuppress(true, null, false))
        assertTrue(OverlaySystemSurfacePolicy.shouldSuppress(false, "com.android.systemui", false))
        assertTrue(OverlaySystemSurfacePolicy.shouldSuppress(false, "com.google.android.permissioncontroller", false))
        assertFalse(OverlaySystemSurfacePolicy.shouldSuppress(false, "test.app", false))
        assertFalse(OverlaySystemSurfacePolicy.shouldSuppress(true, "test.app", true))
        assertFalse(OverlayDisplayPolicy(true, true, true, false, systemSurfaceActive = true).shouldRender(false))
    }

    @Test fun everyShellFamilyFitsSmallNormalLargeAndLandscapeBudgets() {
        listOf(240f, 320f, 360f, 412f, 720f).forEach { width ->
            listOf(126f, 132f, 152f, 156f, 160f, 168f, 172f, 190f, 196f, 222f, 250f).forEach { requested ->
                listOf(0f, 47.67f).forEach { companion ->
                    val result = IslandGeometryConstraints.compactWidth(requested, width, 72f, companion, 250f)
                    assertTrue(result >= 0f)
                    assertTrue(result + companion <= width - 144f + 0.01f)
                    assertTrue(result <= requested && result <= 250f)
                }
            }
            val expanded = IslandGeometryConstraints.expandedWidth(width, 11f, 408f)
            assertTrue(expanded <= width - 22f && expanded <= 408f)
        }
        assertEquals(0f, IslandGeometryConstraints.expandedWidth(10f, 11f, 408f), 0f)
    }
}
