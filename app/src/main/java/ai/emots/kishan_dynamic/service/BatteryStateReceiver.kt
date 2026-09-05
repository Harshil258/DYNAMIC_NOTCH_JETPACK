package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * System BroadcastReceiver monitoring device charging events and battery level changes.
 */
class BatteryStateReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_BATTERY_LOW -> {
                val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 80

                val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                val isFastCharging = intent.action == Intent.ACTION_POWER_CONNECTED &&
                    chargePlug == BatteryManager.BATTERY_PLUGGED_AC

                scope.launch {
                    val preferences = AuroraPreferences(context)
                    val enabled = preferences.showBatteryNotifications.first() && if (intent.action == Intent.ACTION_POWER_CONNECTED) {
                        preferences.chargingAnimationEnabled.first()
                    } else {
                        preferences.lowBatteryAlertEnabled.first()
                    }
                    if (enabled) {
                        IslandStateManager.postCharging(
                            batteryPercent = percent,
                            isFastCharging = isFastCharging
                        )
                    } else {
                        IslandStateManager.clearChargingState()
                    }
                }
            }
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_OKAY -> IslandStateManager.clearChargingState()
        }
    }
}
