package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * System BroadcastReceiver that responds to boot completion and app update events.
 *
 * Ensures that if the Dynamic Island is enabled, runtime permissions and background
 * services are promptly verified and re-synchronized.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action ?: return
        Log.i(TAG, "onReceive: action=$action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                scope.launch {
                    val preferences = AuroraPreferences(context)
                    val isEnabled = preferences.islandEnabled.first()
                    Log.d(TAG, "Boot check: islandEnabled=$isEnabled")

                    if (isEnabled) {
                        PermissionUtils.sendPermissionsChangedBroadcast(context)

                        // If notification listener is granted, prompt Android to rebind
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                            PermissionUtils.isNotificationListenerEnabled(context)
                        ) {
                            runCatching {
                                android.service.notification.NotificationListenerService.requestRebind(
                                    ComponentName(context, IslandNotificationListener::class.java)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
