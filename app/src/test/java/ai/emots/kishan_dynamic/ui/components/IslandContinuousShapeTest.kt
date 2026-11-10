package ai.emots.kishan_dynamic.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandContinuousShapeTest {
    @Test fun referenceFamiliesKeepTheirMeasuredSmoothingRatherThanOneUniversalShape() {
        assertEquals(0f,IslandFamilyGeometry.cornerSmoothing(IslandDemoState.NotificationImage),0f)
        assertEquals(.6f,IslandFamilyGeometry.cornerSmoothing(IslandDemoState.CallExpanded),0f)
        assertEquals(1f,IslandFamilyGeometry.cornerSmoothing(IslandDemoState.MusicExpanded),0f)
        assertEquals(1f,IslandFamilyGeometry.cornerSmoothing(IslandDemoState.ColorOptions),0f)
    }
    @Test fun shortCapsulesRetainCircularEnds() {
        for (radius in listOf(16f,18f,18.335f,19f,43f)) {
            val q=islandCornerCurve(radius,radius)
            assertEquals(radius,q.reach,0.0001f)
            assertEquals(0f,q.smoothing,0.0001f)
            assertEquals(90f,q.arcDegrees,0.0001f)
        }
    }

    @Test fun measuredNavigationCornerHasContinuousTransitionsAtItsReferenceAnchors() {
        val q=islandCornerCurve(42f,185.36053f/2f)
        assertEquals(84f,q.reach,0.0001f)
        assertEquals(0f,q.arcDegrees,0.0001f)
        assertEquals(q.c,q.d,0.0001f)
        // Both axes reach the next straight edge without a gap or an overshoot.
        assertEquals(q.reach,q.a+q.b+q.c+q.d,0.0001f)
        assertEquals(283f,367f-q.reach,0.0001f)
    }

    @Test fun oversizedRadiiAndSmallSurfacesCannotProduceOverlappingCornersOrNaN() {
        for (budget in listOf(0f,.25f,18f,43f,63f,92.68f,220f)) {
            for (radius in listOf(0f,1f,18f,42f,44f,999f)) {
                val q=islandCornerCurve(radius,budget)
                assertTrue(q.radius.isFinite() && q.reach.isFinite())
                assertTrue(q.radius>=0f && q.radius<=budget)
                assertTrue(q.reach>=0f && q.reach<=budget+.0001f)
                assertTrue(q.smoothing in 0f..1f)
                assertTrue(q.arcDegrees in 0f..90f)
                assertTrue(q.a.isFinite() && q.b.isFinite() && q.c.isFinite() && q.d.isFinite())
            }
        }
    }
}
