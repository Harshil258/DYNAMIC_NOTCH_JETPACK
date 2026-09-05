package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionSystemTileTest {

    @Test
    fun referenceSystemTileCatalogHasUniqueStableKeys() {
        val tiles = ActionSystemTile.entries

        assertEquals(15, tiles.size)
        assertEquals(tiles.size, tiles.map { it.storageKey }.toSet().size)
        assertTrue(tiles.map { it.storageKey }.containsAll(
            listOf("wifi", "bluetooth", "mobile_data", "torch", "location", "rotation_lock", "airplane_mode", "do_not_disturb", "hotspot", "screenshot", "dark_mode", "auto_brightness", "power_saver", "sync", "nfc")
        ))
    }

    @Test
    fun defaultsRemainUsefulAndStable() {
        assertEquals(
            listOf(
                ActionSystemTile.WIFI,
                ActionSystemTile.BLUETOOTH,
                ActionSystemTile.MOBILE_DATA,
                ActionSystemTile.TORCH,
                ActionSystemTile.LOCATION,
                ActionSystemTile.DO_NOT_DISTURB
            ),
            defaultActionSystemTiles
        )
    }

    @Test
    fun snapshotDistinguishesOffFromActionOnlyTiles() {
        val snapshot = ActionSystemSnapshot(
            values = mapOf(
                ActionSystemTile.WIFI to false,
                ActionSystemTile.TORCH to true,
                ActionSystemTile.SCREENSHOT to null
            )
        )

        assertEquals(false, snapshot.stateOf(ActionSystemTile.WIFI))
        assertEquals(true, snapshot.stateOf(ActionSystemTile.TORCH))
        assertEquals(null, snapshot.stateOf(ActionSystemTile.SCREENSHOT))
    }

    @Test
    fun restrictedWifiVersionsUseSettingsInsteadOfClaimingDirectControl() {
        assertEquals(ActionTileExecutionPath.Direct, ActionTileExecutionPolicy.wifi(28))
        assertEquals(ActionTileExecutionPath.SettingsFallback, ActionTileExecutionPolicy.wifi(29))
        assertEquals(ActionTileExecutionPath.SettingsFallback, ActionTileExecutionPolicy.wifi(35))
    }

    @Test
    fun writableSettingsControlsOnlyUseDirectPathWhenPermissionIsGranted() {
        assertEquals(
            ActionTileExecutionPath.Direct,
            ActionTileExecutionPolicy.writableSystemSetting(canWriteSettings = true)
        )
        assertEquals(
            ActionTileExecutionPath.SettingsFallback,
            ActionTileExecutionPolicy.writableSystemSetting(canWriteSettings = false)
        )
    }
}
