package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SatelliteSurfacePolicyTest {
    @Test
    fun enabledSatelliteProducesAnAppOwnedActivity() {
        val activity = satelliteActivityFor(true)

        assertEquals(LiveActivityKind.SATELLITE, activity?.kind)
        assertEquals("Satellite connection ready", activity?.subtitle)
    }

    @Test
    fun disabledSatelliteClearsTheActivity() {
        assertNull(satelliteActivityFor(false))
    }
}
