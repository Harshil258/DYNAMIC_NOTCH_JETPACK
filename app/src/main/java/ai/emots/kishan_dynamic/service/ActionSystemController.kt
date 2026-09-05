package ai.emots.kishan_dynamic.service

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.app.NotificationManager
import android.net.wifi.WifiManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.ActionSystemSnapshot
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionTileExecutionPath
import ai.emots.kishan_dynamic.data.model.ActionTileExecutionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Safe command boundary for Action Island system tiles.
 *
 * Android does not grant an ordinary accessibility overlay permission to
 * silently mutate every system setting. Where direct mutation is restricted,
 * the command opens the narrowest relevant system panel instead.
 */
class ActionSystemController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    // Camera hardware is optional for this app. Keep the Action Island alive
    // on devices where the Torch tile cannot exist.
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val _snapshot = MutableStateFlow(ActionSystemSnapshot())
    val snapshot: StateFlow<ActionSystemSnapshot> = _snapshot
    private var torchEnabled = false
    private val refreshScope = CoroutineScope(Dispatchers.Main + Job())
    private var refreshJob: Job? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            torchEnabled = enabled
            refresh()
        }
    }

    init {
        runCatching {
            cameraManager?.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
        }
        refresh()
        refreshJob = refreshScope.launch {
            while (isActive) {
                delay(1500L)
                refresh()
            }
        }
    }

    fun execute(tile: ActionSystemTile) {
        when (tile) {
            ActionSystemTile.TORCH -> toggleTorch()
            ActionSystemTile.WIFI -> toggleWifiOrOpenSettings()
            ActionSystemTile.BLUETOOTH -> toggleBluetoothOrOpenSettings()
            ActionSystemTile.DO_NOT_DISTURB -> toggleDndOrOpenSettings()
            ActionSystemTile.ROTATION_LOCK -> toggleRotationLockOrOpenSettings()
            ActionSystemTile.AUTO_BRIGHTNESS -> toggleAutoBrightnessOrOpenSettings()
            ActionSystemTile.SYNC -> toggleSyncOrOpenSettings()
            else -> openTileSettings(tile)
        }
        refresh()
    }

    fun refresh() {
        _snapshot.value = ActionSystemSnapshot(
            values = ActionSystemTile.entries.associateWith { readState(it) }
        )
    }

    fun close() {
        runCatching { cameraManager?.unregisterTorchCallback(torchCallback) }
        refreshJob?.cancel()
        refreshScope.coroutineContext[Job]?.cancel()
    }

    private fun readState(tile: ActionSystemTile): Boolean? = runCatching {
        when (tile) {
            ActionSystemTile.WIFI -> wifiEnabled()
            ActionSystemTile.BLUETOOTH -> bluetoothAdapter()?.isEnabled
            ActionSystemTile.MOBILE_DATA -> if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getSystemService(android.telephony.TelephonyManager::class.java)?.isDataEnabled == true
                } else {
                    Settings.Global.getInt(context.contentResolver, "mobile_data", 0) == 1
                }
            } else null
            ActionSystemTile.TORCH -> torchEnabled
            ActionSystemTile.LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.getSystemService(android.location.LocationManager::class.java)?.isLocationEnabled == true
            } else Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
            ActionSystemTile.ROTATION_LOCK -> Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 0
            ActionSystemTile.AIRPLANE_MODE -> Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
            ActionSystemTile.DO_NOT_DISTURB -> dndState()
            ActionSystemTile.HOTSPOT -> null
            ActionSystemTile.SCREENSHOT -> null
            ActionSystemTile.DARK_MODE -> context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
            ActionSystemTile.AUTO_BRIGHTNESS -> Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            ActionSystemTile.POWER_SAVER -> context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true
            ActionSystemTile.SYNC -> ContentResolver.getMasterSyncAutomatically()
            ActionSystemTile.NFC -> NfcAdapter.getDefaultAdapter(context)?.isEnabled
        }
    }.getOrNull()

    @Suppress("DEPRECATION")
    private fun wifiEnabled(): Boolean? = runCatching {
        context.applicationContext.getSystemService(WifiManager::class.java)?.isWifiEnabled
    }.getOrNull()

    @Suppress("DEPRECATION")
    private fun toggleWifiOrOpenSettings() {
        if (ActionTileExecutionPolicy.wifi(Build.VERSION.SDK_INT) != ActionTileExecutionPath.Direct) {
            openTileSettings(ActionSystemTile.WIFI)
            return
        }
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        if (wifiManager == null) {
            openTileSettings(ActionSystemTile.WIFI)
            return
        }
        runCatching {
            check(wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled))
        }.onFailure { openTileSettings(ActionSystemTile.WIFI) }
    }

    private fun bluetoothAdapter() = context.getSystemService(BluetoothManager::class.java)?.adapter

    private fun toggleBluetoothOrOpenSettings() {
        val adapter = bluetoothAdapter()
        val canConnect = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (adapter == null || !canConnect) {
            openTileSettings(ActionSystemTile.BLUETOOTH)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openTileSettings(ActionSystemTile.BLUETOOTH)
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                if (adapter.isEnabled) adapter.disable() else adapter.enable()
            }.onFailure { openTileSettings(ActionSystemTile.BLUETOOTH) }
        }
    }

    private fun toggleTorch() {
        val manager = cameraManager ?: return
        val cameraId = runCatching { manager.cameraIdList.firstOrNull() }.getOrNull() ?: return
        runCatching { manager.setTorchMode(cameraId, !torchEnabled) }
    }

    private fun dndEnabled(): Boolean {
        return dndState() == true
    }

    private fun dndState(): Boolean? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val manager = context.getSystemService(NotificationManager::class.java) ?: return null
        if (!manager.isNotificationPolicyAccessGranted) return null
        return manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private fun toggleDndOrOpenSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.isNotificationPolicyAccessGranted) {
            openTileSettings(ActionSystemTile.DO_NOT_DISTURB)
            return
        }
        runCatching {
            manager.setInterruptionFilter(
                if (dndEnabled()) NotificationManager.INTERRUPTION_FILTER_ALL
                else NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )
        }.onFailure { openTileSettings(ActionSystemTile.DO_NOT_DISTURB) }
    }

    private fun toggleSyncOrOpenSettings() {
        runCatching {
            ContentResolver.setMasterSyncAutomatically(
                !ContentResolver.getMasterSyncAutomatically()
            )
        }.onFailure { openTileSettings(ActionSystemTile.SYNC) }
    }

    private fun toggleRotationLockOrOpenSettings() {
        if (ActionTileExecutionPolicy.writableSystemSetting(Settings.System.canWrite(context)) != ActionTileExecutionPath.Direct) {
            openTileSettings(ActionSystemTile.ROTATION_LOCK)
            return
        }
        runCatching {
            val isLocked = Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                1
            ) == 0
            check(
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    if (isLocked) 1 else 0
                )
            )
        }.onFailure { openTileSettings(ActionSystemTile.ROTATION_LOCK) }
    }

    private fun toggleAutoBrightnessOrOpenSettings() {
        if (ActionTileExecutionPolicy.writableSystemSetting(Settings.System.canWrite(context)) != ActionTileExecutionPath.Direct) {
            openTileSettings(ActionSystemTile.AUTO_BRIGHTNESS)
            return
        }
        runCatching {
            val isAutomatic = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            check(
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (isAutomatic) {
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    } else {
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    }
                )
            )
        }.onFailure { openTileSettings(ActionSystemTile.AUTO_BRIGHTNESS) }
    }

    private fun openTileSettings(tile: ActionSystemTile) {
        val action = when (tile) {
            ActionSystemTile.WIFI -> Settings.ACTION_WIFI_SETTINGS
            ActionSystemTile.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            ActionSystemTile.MOBILE_DATA -> Settings.ACTION_WIRELESS_SETTINGS
            ActionSystemTile.TORCH -> Settings.ACTION_SETTINGS
            ActionSystemTile.LOCATION -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            ActionSystemTile.ROTATION_LOCK -> Settings.ACTION_DISPLAY_SETTINGS
            ActionSystemTile.AIRPLANE_MODE -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            ActionSystemTile.DO_NOT_DISTURB -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            ActionSystemTile.HOTSPOT -> Settings.ACTION_WIRELESS_SETTINGS
            ActionSystemTile.SCREENSHOT -> Settings.ACTION_SETTINGS
            ActionSystemTile.DARK_MODE -> Settings.ACTION_DISPLAY_SETTINGS
            ActionSystemTile.AUTO_BRIGHTNESS -> Settings.ACTION_DISPLAY_SETTINGS
            ActionSystemTile.POWER_SAVER -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            ActionSystemTile.SYNC -> Settings.ACTION_SYNC_SETTINGS
            ActionSystemTile.NFC -> Settings.ACTION_NFC_SETTINGS
        }
        runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun openSettings() {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    /**
     * Android does not expose Apple's AirPlay picker as a portable intent.
     * Bluetooth/audio-output settings are the narrowest platform-owned route
     * for choosing an external playback destination on supported devices.
     */
    fun openAudioOutputSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun executeCustomAction(actionId: String) {
        when (actionId) {
            "open_settings" -> openSettings()
            "open_camera" -> launchActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            "open_notifications" -> launchActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            "open_calendar" -> launchActivity(Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI))
        }
    }

    fun readBrightness(): Float {
        val value = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }.getOrDefault(128)
        return (value / 255f).coerceIn(0f, 1f)
    }

    fun setBrightness(level: Float): Boolean {
        if (!Settings.System.canWrite(context)) {
            openWriteSettings()
            return false
        }
        return runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (level.coerceIn(0f, 1f) * 255f).toInt()
            )
        }.getOrDefault(false)
    }

    fun readMediaVolume(): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max.toFloat()
    }

    fun setMediaVolume(level: Float) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (level.coerceIn(0f, 1f) * max).toInt(),
            0
        )
    }

    private fun openWriteSettings() {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun launchActivity(intent: Intent) {
        runCatching {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
