package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * System BroadcastReceiver monitoring device charging events and battery level changes.
 */
class BatteryStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val batteryStatus: Intent? = context?.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 80

                val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                val isFastCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

                IslandStateManager.postCharging(batteryPercent = percent, isFastCharging = isFastCharging)
            }
        }
    }
}
