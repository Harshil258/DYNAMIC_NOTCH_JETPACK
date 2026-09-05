package ai.emots.kishan_dynamic.service

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Bridges system utility state into the same live-activity pipeline as the
 * other source adapters. The monitor only emits when a value changes, so a
 * disabled setting cannot continually reset the island's presentation.
 */
class SystemLiveActivityMonitor(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private var monitorJob: Job? = null
    private var lastAirplaneMode: Boolean? = null
    private var lastMobileDataEnabled: Boolean? = null

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
        val airplaneMode = readAirplaneMode()
        val mobileDataEnabled = readMobileDataEnabled()

        if (lastAirplaneMode != airplaneMode) {
            lastAirplaneMode = airplaneMode
            if (airplaneMode) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = AIRPLANE_ACTIVITY_ID,
                        kind = LiveActivityKind.AIRPLANE_ALERT,
                        title = "Airplane Mode",
                        subtitle = "Turn off Airplane Mode or use Wi-Fi to access data.",
                        isExpanded = true
                    )
                )
            } else {
                IslandStateManager.clearLiveActivity(AIRPLANE_ACTIVITY_ID)
            }
        }

        // Airplane mode already explains the unavailable connection. Do not
        // stack a second alert while the radio is intentionally disabled.
        if (airplaneMode) {
            IslandStateManager.clearLiveActivity(MOBILE_DATA_ACTIVITY_ID)
            // Re-evaluate the data radio when airplane mode is released, even
            // if its value remained disabled while the radios were suspended.
            lastMobileDataEnabled = null
            return
        }

        if (lastMobileDataEnabled != mobileDataEnabled) {
            lastMobileDataEnabled = mobileDataEnabled
            when (mobileDataEnabled) {
                false -> IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = MOBILE_DATA_ACTIVITY_ID,
                        kind = LiveActivityKind.MOBILE_DATA_ALERT,
                        title = "Mobile Data is Turned Off",
                        subtitle = "Turn on mobile data or use Wi-Fi to access data.",
                        isExpanded = true
                    )
                )
                true -> IslandStateManager.clearLiveActivity(MOBILE_DATA_ACTIVITY_ID)
                null -> Unit
            }
        }
    }

    private fun readAirplaneMode(): Boolean = runCatching {
        Settings.Global.getInt(
            appContext.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }.getOrDefault(false)

    private fun readMobileDataEnabled(): Boolean? {
        if (ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.READ_PHONE_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return null

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.getSystemService(TelephonyManager::class.java)?.isDataEnabled
            } else {
                Settings.Global.getInt(appContext.contentResolver, "mobile_data", 0) == 1
            }
        }.getOrNull()
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1500L
        const val AIRPLANE_ACTIVITY_ID = "system-airplane-mode"
        const val MOBILE_DATA_ACTIVITY_ID = "system-mobile-data"
    }
}
