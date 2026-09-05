package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

/** Persisted system actions that can appear in the expanded Action Island. */
enum class ActionSystemTile(
    val storageKey: String,
    val title: String
) {
    WIFI("wifi", "Wi-Fi"),
    BLUETOOTH("bluetooth", "Bluetooth"),
    MOBILE_DATA("mobile_data", "Mobile data"),
    TORCH("torch", "Torch"),
    LOCATION("location", "Location"),
    ROTATION_LOCK("rotation_lock", "Rotation lock"),
    AIRPLANE_MODE("airplane_mode", "Airplane"),
    DO_NOT_DISTURB("do_not_disturb", "Focus"),
    HOTSPOT("hotspot", "Hotspot"),
    SCREENSHOT("screenshot", "Screenshot"),
    DARK_MODE("dark_mode", "Dark mode"),
    AUTO_BRIGHTNESS("auto_brightness", "Auto brightness"),
    POWER_SAVER("power_saver", "Power saver"),
    SYNC("sync", "Sync"),
    NFC("nfc", "NFC")
}

/**
 * Describes whether a system tile can be changed in-process on this Android
 * version, or must hand control to the platform settings surface.
 */
enum class ActionTileExecutionPath {
    Direct,
    SettingsFallback
}

object ActionTileExecutionPolicy {
    fun wifi(apiLevel: Int): ActionTileExecutionPath =
        if (apiLevel < 29) ActionTileExecutionPath.Direct
        else ActionTileExecutionPath.SettingsFallback

    fun writableSystemSetting(canWriteSettings: Boolean): ActionTileExecutionPath =
        if (canWriteSettings) ActionTileExecutionPath.Direct
        else ActionTileExecutionPath.SettingsFallback
}

enum class ActionUtilityAction {
    LOCK,
    SETTINGS,
    CAMERA,
    EDIT
}

/** App-owned action presets exposed through the customizable Action Island. */
enum class ActionCustomPreset(
    val actionId: String,
    val title: String,
    val iconKey: String
) {
    OPEN_SETTINGS("open_settings", "Settings", "settings"),
    OPEN_CAMERA("open_camera", "Camera", "camera"),
    OPEN_NOTIFICATIONS("open_notifications", "Notifications", "bell"),
    OPEN_CALENDAR("open_calendar", "Calendar", "calendar")
}

@Immutable
data class ActionCustomShortcut(
    val actionId: String,
    val label: String,
    val iconKey: String,
    val order: Int
)

@Immutable
data class ActionAppShortcut(
    val packageName: String,
    val appName: String,
    val order: Int,
    val isEnabled: Boolean = true
)

@Immutable
data class ActionContactShortcut(
    val contactId: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val order: Int,
    val isEnabled: Boolean = true
)

@Immutable
data class ActionIslandConfig(
    val isEnabled: Boolean = true,
    val systemTiles: List<ActionSystemTile> = defaultActionSystemTiles,
    val apps: List<ActionAppShortcut> = emptyList(),
    val contacts: List<ActionContactShortcut> = emptyList(),
    val customActions: List<ActionCustomShortcut> = emptyList(),
    val maxApps: Int = 8,
    val maxContacts: Int = 6,
    val maxCustomActions: Int = 4
)

@Immutable
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

@Immutable
data class DeviceContactInfo(
    val contactId: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String?
)

/**
 * Read-only snapshot used by the overlay so a quick-control chip reflects the device,
 * instead of being styled as active merely because it is configured.
 * A null value means the tile is an action-only control (for example Screenshot).
 */
@Immutable
data class ActionSystemSnapshot(
    val values: Map<ActionSystemTile, Boolean?> = emptyMap()
) {
    fun stateOf(tile: ActionSystemTile): Boolean? = values[tile]
}

val defaultActionSystemTiles: List<ActionSystemTile> = listOf(
    ActionSystemTile.WIFI,
    ActionSystemTile.BLUETOOTH,
    ActionSystemTile.MOBILE_DATA,
    ActionSystemTile.TORCH,
    ActionSystemTile.LOCATION,
    ActionSystemTile.DO_NOT_DISTURB
)
