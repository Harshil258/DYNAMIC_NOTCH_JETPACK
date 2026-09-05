package ai.emots.kishan_dynamic.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationManagerCompat

object PermissionUtils {

    const val ACTION_PERMISSIONS_CHANGED = "ai.emots.kishan_dynamic.ACTION_PERMISSIONS_CHANGED"
    private const val TAG = "PermissionUtils"

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, IslandOverlayService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if accessibility permission is granted AND the service is actively
     * connected in runtime. Detects cases where permission is toggled on in settings
     * but Android has killed or frozen the service.
     */
    fun isAccessibilityServiceWorking(context: Context): Boolean {
        return isAccessibilityServiceEnabled(context) && IslandOverlayService.isServiceActuallyConnected
    }

    /**
     * Checks if the notification listener service is enabled via NotificationManagerCompat,
     * Settings.Secure fallback, or active service instance connection.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        // Method 1: NotificationManagerCompat (primary)
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledPackages.contains(context.packageName)) return true

        // Method 2: Check via Settings.Secure fallback
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (listeners?.contains(context.packageName) == true) return true

        // Method 3: Direct runtime instance check
        return IslandNotificationListener.instance != null
    }

    /**
     * Checks if notification listener is both enabled and actively running.
     */
    fun isNotificationListenerWorking(context: Context): Boolean {
        return isNotificationListenerEnabled(context) && IslandNotificationListener.instance != null
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        }
        return true
    }

    fun areCorePermissionsGranted(context: Context): Boolean {
        return isAccessibilityServiceEnabled(context) && isNotificationListenerEnabled(context)
    }

    fun sendPermissionsChangedBroadcast(context: Context) {
        val intent = Intent(ACTION_PERMISSIONS_CHANGED).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "Sent ACTION_PERMISSIONS_CHANGED broadcast")
    }

    /**
     * Direct navigation to the app's accessibility service entry in system settings,
     * attempting Samsung's dedicated service activity first, and adding fragment arguments.
     */
    fun openAccessibilitySettings(context: Context) {
        val showArgs = "${context.packageName}/${IslandOverlayService::class.java.name}"
        val extraFragmentKey = ":settings:fragment_args_key"
        val extraShowFragmentArgs = ":settings:show_fragment_args"

        val bundle = Bundle().apply {
            putString(extraFragmentKey, showArgs)
        }

        val samsungIntent = Intent("com.samsung.accessibility.installed_service").apply {
            putExtra(extraFragmentKey, showArgs)
            putExtra(extraShowFragmentArgs, bundle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (samsungIntent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(samsungIntent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Failed to launch Samsung accessibility intent", e)
            }
        }

        val standardIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            putExtra(extraFragmentKey, showArgs)
            putExtra(extraShowFragmentArgs, bundle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(standardIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to launch standard accessibility settings with args", e)
            val fallbackIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to launch accessibility fallback settings", e2)
            }
        }
    }

    /**
     * Direct navigation to notification listener settings with fragment arguments
     * pointing to our listener service.
     */
    fun openNotificationListenerSettings(context: Context) {
        val showArgs = "${context.packageName}/${IslandNotificationListener::class.java.name}"
        val extraFragmentKey = ":settings:fragment_args_key"
        val extraShowFragmentArgs = ":settings:show_fragment_args"

        val bundle = Bundle().apply {
            putString(extraFragmentKey, showArgs)
        }

        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            putExtra(extraFragmentKey, showArgs)
            putExtra(extraShowFragmentArgs, bundle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open notification settings with args", e)
            val fallbackIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open notification fallback settings", e2)
            }
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to open battery optimization settings", e2)
                }
            }
        }
    }
}

