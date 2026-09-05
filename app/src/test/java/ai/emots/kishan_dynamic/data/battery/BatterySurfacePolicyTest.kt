package ai.emots.kishan_dynamic.data.battery

import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatterySurfacePolicyTest {
    @Test
    fun batteryOwnsOnlyChargingSurface() {
        assertTrue(BatterySurfacePolicy.ownsSurface(IslandState.Charging(42)))
        assertFalse(BatterySurfacePolicy.ownsSurface(IslandState.RingerMode(RingerModeType.SILENT)))
        assertFalse(BatterySurfacePolicy.ownsSurface(IslandState.MediaVolume(0.5f)))
    }
}
