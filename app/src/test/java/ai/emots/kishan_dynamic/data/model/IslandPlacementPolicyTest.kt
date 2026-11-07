package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.*
import org.junit.Test

class IslandPlacementPolicyTest {
    private fun place(width: Int = 393, height: Int = 80, x: Int = 0, y: Int = 11,
                      expanded: Boolean = false, holes: List<IslandCutout> = emptyList()) =
        IslandPlacementPolicy.resolve(width, 850, if (expanded) width - 22 else 72, height,
            x, y, expanded, 28, 11, 4, holes)

    @Test fun noCutoutCreatesNoInventedCameraClearance() {
        assertEquals(IslandPlacement(0, 11), place())
    }
    @Test fun leftCameraDoesNotCreateBlankSpaceInCenteredContent() {
        assertEquals(11, place(holes = listOf(IslandCutout(18, 0, 42, 28))).top)
    }
    @Test fun centeredAndPillCamerasCannotCoverNotificationContent() {
        listOf(IslandCutout(184, 0, 208, 28), IslandCutout(155, 0, 237, 35)).forEach { hole ->
            assertEquals(hole.bottom + 4, place(holes = listOf(hole)).top)
        }
    }
    @Test fun everyExpandedWidthSitsBelowStatusIconsAndCamera() {
        listOf(320, 360, 393, 430, 720).forEach { width ->
            val hole = IslandCutout(width / 2 - 16, 0, width / 2 + 16, 38)
            assertEquals(42, place(width = width, expanded = true, holes = listOf(hole)).top)
        }
    }
    @Test fun userCalibrationNeverPlacesWindowBeyondDisplayEdges() {
        for (width in listOf(320, 360, 393, 430)) for (x in listOf(-1000, 1000)) {
            val result = place(width = width, x = x)
            val left = width / 2 + result.centerOffsetX - 36
            assertTrue(left >= 10)
            assertTrue(left + 72 <= width - 10)
        }
    }
    @Test fun rotationUsesNewWindowAndCutoutCoordinates() {
        val portrait = place(expanded = true, holes = listOf(IslandCutout(180, 0, 212, 32)))
        val landscape = IslandPlacementPolicy.resolve(850, 393, 408, 150, 0, 11, true,
            24, 11, 4, listOf(IslandCutout(0, 180, 32, 212)))
        assertEquals(36, portrait.top)
        assertEquals(28, landscape.top)
        assertEquals(0, landscape.centerOffsetX)
    }
}
