package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.playground.FigmaReferenceFixtures
import ai.emots.kishan_dynamic.playground.FigmaReferenceProvenance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FigmaProvenanceAndV6MatrixTest {

    @Test
    fun totalStateCountIsExactlySixty() {
        assertEquals(60, IslandDemoState.entries.size)
    }

    @Test
    fun provenanceBreakdownMatchesReconciledV6Inventory() {
        val allStates = IslandDemoState.entries
        val directStates = allStates.filter {
            FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.DIRECT
        }
        val familyStates = allStates.filter {
            FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.FAMILY_ONLY
        }
        val noSourceStates = allStates.filter {
            FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.NO_SOURCE
        }

        assertEquals("DIRECT states count must be 28", 28, directStates.size)
        assertEquals("FAMILY_ONLY states count must be 10", 10, familyStates.size)
        assertEquals("NO_SOURCE states count must be 22", 22, noSourceStates.size)
        assertEquals("Total sum must be 60", 60, directStates.size + familyStates.size + noSourceStates.size)
    }

    @Test
    fun directStatesHaveExactNodeIds() {
        IslandDemoState.entries
            .filter { FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.DIRECT }
            .forEach { state ->
                val nodeId = FigmaReferenceFixtures.figmaNodeId(state)
                assertNotNull("DIRECT state $state must have non-null Figma node id", nodeId)
            }
    }

    @Test
    fun familyOnlyStatesHaveExemplarNodeIds() {
        IslandDemoState.entries
            .filter { FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.FAMILY_ONLY }
            .forEach { state ->
                val nodeId = FigmaReferenceFixtures.figmaNodeId(state)
                assertNotNull("FAMILY_ONLY state $state must have non-null exemplar node id", nodeId)
            }
    }

    @Test
    fun noSourceStatesHaveNullNodeIds() {
        IslandDemoState.entries
            .filter { FigmaReferenceFixtures.referenceProvenance(it) == FigmaReferenceProvenance.NO_SOURCE }
            .forEach { state ->
                val nodeId = FigmaReferenceFixtures.figmaNodeId(state)
                assertNull("NO_SOURCE state $state must have null node id", nodeId)
            }
    }

    @Test
    fun keyDerivedStatesGeometryTargetsConformToV6Recipe() {
        val callSummary = IslandFamilyGeometry.target(IslandDemoState.CallSummaryExpanded)
        assertEquals(367f, callSummary.width, 0.001f)
        assertEquals(112f, callSummary.height, 0.001f)
        assertEquals(42f, callSummary.radius, 0.001f)

        val transport = IslandFamilyGeometry.target(IslandDemoState.TransportActivity)
        assertEquals(367f, transport.width, 0.001f)
        assertEquals(148f, transport.height, 0.001f)

        val movie = IslandFamilyGeometry.target(IslandDemoState.MovieCard)
        assertEquals(367f, movie.width, 0.001f)
        assertEquals(142f, movie.height, 0.001f)

        val subscription = IslandFamilyGeometry.target(IslandDemoState.SubscriptionPricing)
        assertEquals(367f, subscription.width, 0.001f)
        assertEquals(162f, subscription.height, 0.001f)

        val colorOptions = IslandFamilyGeometry.target(IslandDemoState.ColorOptions)
        assertEquals(367f, colorOptions.width, 0.001f)
        assertEquals(185.3f, colorOptions.height, 0.001f)
    }
}
