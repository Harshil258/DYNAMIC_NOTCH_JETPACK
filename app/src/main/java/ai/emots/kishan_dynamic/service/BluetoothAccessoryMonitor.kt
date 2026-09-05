package ai.emots.kishan_dynamic.service

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Observes connected Android audio profiles and promotes only known AirPods
 * devices to the reference-style accessory activity. Other Bluetooth devices
 * stay on the generic connection banner owned by the ACL receiver.
 */
class BluetoothAccessoryMonitor(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private var monitorJob: Job? = null
    private var lastSnapshot: AirPodsSnapshot? = null
    private val batteryByAddress = mutableMapOf<String, Int>()

    fun onBatteryLevel(device: BluetoothDevice?, level: Int) {
        if (device == null || level !in 0..100) return
        batteryByAddress[device.address] = level
        scope.launch { sync() }
    }

    fun start() {
        if (monitorJob?.isActive == true) return
        monitorJob = scope.launch {
            while (isActive) {
                sync()
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun close() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun sync() {
        val snapshot = readSnapshot()
        if (snapshot == lastSnapshot) return
        lastSnapshot = snapshot

        if (snapshot == null) {
            IslandStateManager.clearLiveActivity(AIRPODS_ACTIVITY_ID)
            return
        }

        val batteryText = snapshot.batteryPercent?.let { "Connected · $it%" } ?: "Connected"
        IslandStateManager.postLiveActivity(
            LiveActivityInfo(
                id = AIRPODS_ACTIVITY_ID,
                kind = LiveActivityKind.AIRPODS,
                title = snapshot.deviceName,
                subtitle = batteryText,
                progress = snapshot.batteryPercent?.div(100f),
                isExpanded = true
            )
        )
    }

    private fun readSnapshot(): AirPodsSnapshot? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return null

        val manager = appContext.getSystemService(BluetoothManager::class.java) ?: return null
        val profiles = buildList {
            add(BluetoothProfile.A2DP)
            add(BluetoothProfile.HEADSET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(BluetoothProfile.LE_AUDIO)
        }
        val devices = profiles
            .flatMap { profile -> runCatching { manager.getConnectedDevices(profile) }.getOrDefault(emptyList()) }
            .distinctBy { it.address }
        val device = devices.firstOrNull { isAirPods(it) } ?: return null
        return AirPodsSnapshot(
            deviceAddress = device.address,
            deviceName = runCatching { device.name }.getOrNull().orEmpty().ifBlank { "AirPods" },
            batteryPercent = readBatteryPercent(device)
        )
    }

    @SuppressLint("MissingPermission")
    private fun isAirPods(device: BluetoothDevice): Boolean =
        runCatching { device.name.orEmpty().lowercase() }.getOrDefault("").contains("airpod")

    private fun readBatteryPercent(device: BluetoothDevice): Int? {
        return batteryByAddress[device.address]
    }

    private data class AirPodsSnapshot(
        val deviceAddress: String,
        val deviceName: String,
        val batteryPercent: Int?
    )

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1500L
        const val AIRPODS_ACTIVITY_ID = "bluetooth-airpods"
    }
}
