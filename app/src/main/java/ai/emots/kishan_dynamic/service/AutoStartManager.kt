package ai.emots.kishan_dynamic.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Handles OEM-specific Auto-Start permission intents.
 *
 * Many Android OEMs (Xiaomi, Huawei, OnePlus, Oppo, Vivo, Samsung, etc.) have
 * proprietary battery and memory management systems that kill accessibility overlay services.
 * Each OEM has a specific settings screen for "Auto Start" / "Startup Manager".
 *
 * This manager detects the device manufacturer and opens the appropriate settings screen,
 * with a safe fallback to application details.
 */
object AutoStartManager {

    private const val TAG = "AutoStartManager"
    private const val PREFS_NAME = "dynamic_island_prefs"
    private const val KEY_AUTO_START_OPENED = "auto_start_opened"

    fun isAutoStartSupported(context: Context): Boolean {
        val intent = getAutoStartIntent(context)
        return intent != null && canResolveIntent(context, intent)
    }

    fun getOemName(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return when {
            manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                brand.contains("redmi") || brand.contains("poco") -> "MIUI / HyperOS"
            manufacturer.contains("huawei") || brand.contains("huawei") ||
                brand.contains("honor") -> "EMUI"
            manufacturer.contains("oppo") || brand.contains("oppo") ||
                brand.contains("realme") -> "ColorOS / Realme UI"
            manufacturer.contains("oneplus") || brand.contains("oneplus") -> "OxygenOS"
            manufacturer.contains("vivo") || brand.contains("vivo") ||
                brand.contains("iqoo") -> "FuntouchOS / OriginOS"
            manufacturer.contains("samsung") || brand.contains("samsung") -> "Samsung"
            manufacturer.contains("meizu") || brand.contains("meizu") -> "Flyme"
            manufacturer.contains("letv") || brand.contains("letv") ||
                brand.contains("leeco") -> "LeEco"
            manufacturer.contains("asus") || brand.contains("asus") -> "ZenUI"
            manufacturer.contains("nokia") || brand.contains("nokia") -> "Nokia"
            manufacturer.contains("lenovo") || brand.contains("lenovo") -> "Lenovo"
            else -> ""
        }
    }

    fun isAutoStartOpened(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_START_OPENED, false)
    }

    fun isAutoStartAvailable(context: Context): Boolean = isAutoStartSupported(context)
    fun isAutoStartDone(context: Context): Boolean = isAutoStartOpened(context)
    fun openAutoStart(context: Context): Boolean = openAutoStartSettings(context)

    fun markAutoStartOpened(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_START_OPENED, true).apply()
    }

    fun openAutoStartSettings(context: Context): Boolean {
        markAutoStartOpened(context)
        val intent = getAutoStartIntent(context) ?: run {
            Log.d(TAG, "No OEM auto-start intent found, opening app details")
            openFallbackSettings(context)
            return false
        }

        return try {
            if (canResolveIntent(context, intent)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Opened auto-start settings for ${Build.MANUFACTURER}")
                true
            } else {
                Log.d(TAG, "Intent not resolvable, opening fallback")
                openFallbackSettings(context)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening auto-start settings", e)
            openFallbackSettings(context)
            false
        }
    }

    fun getAutoStartIntent(context: Context): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                brand.contains("redmi") || brand.contains("poco") ->
                getXiaomiIntent(context)

            manufacturer.contains("huawei") || brand.contains("huawei") ||
                brand.contains("honor") ->
                getHuaweiIntent(context)

            manufacturer.contains("oneplus") || brand.contains("oneplus") ->
                getOnePlusIntent(context)

            manufacturer.contains("oppo") || brand.contains("oppo") ||
                brand.contains("realme") ->
                getOppoIntent(context)

            manufacturer.contains("vivo") || brand.contains("vivo") ||
                brand.contains("iqoo") ->
                getVivoIntent(context)

            manufacturer.contains("samsung") || brand.contains("samsung") ->
                getSamsungIntent(context)

            manufacturer.contains("meizu") || brand.contains("meizu") ->
                getMeizuIntent(context)

            manufacturer.contains("asus") || brand.contains("asus") ->
                getAsusIntent(context)

            manufacturer.contains("nokia") || brand.contains("nokia") ->
                getNokiaIntent(context)

            manufacturer.contains("lenovo") || brand.contains("lenovo") ->
                getLenovoIntent(context)

            manufacturer.contains("letv") || brand.contains("letv") ||
                brand.contains("leeco") ->
                getLeEcoIntent()

            else -> null
        }
    }

    private fun getXiaomiIntent(context: Context): Intent {
        val miuiNew = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        val miuiOld = Intent("miui.intent.action.OP_AUTO_START").apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val miuiFallback = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
            )
            putExtra("extra_pkgname", context.packageName)
        }

        return when {
            canResolveIntent(context, miuiNew) -> miuiNew
            canResolveIntent(context, miuiOld) -> miuiOld
            canResolveIntent(context, miuiFallback) -> miuiFallback
            else -> miuiNew
        }
    }

    private fun getHuaweiIntent(context: Context): Intent {
        val emui12Intent = Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        }
        val emuiOldIntent = Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.appupgrade.CustAppUpgradeSmartActivity"
            )
        }
        val emuiFallback = Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        }

        return when {
            canResolveIntent(context, emui12Intent) -> emui12Intent
            canResolveIntent(context, emuiOldIntent) -> emuiOldIntent
            else -> emuiFallback
        }
    }

    private fun getOnePlusIntent(context: Context): Intent {
        val onePlus12Intent = Intent().apply {
            component = ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
        }
        val onePlusOldIntent = Intent("com.oneplus.action.STARTUP_MANAGER")

        return when {
            canResolveIntent(context, onePlus12Intent) -> onePlus12Intent
            canResolveIntent(context, onePlusOldIntent) -> onePlusOldIntent
            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }

    private fun getOppoIntent(context: Context): Intent {
        val colorOsNew = Intent().apply {
            component = ComponentName(
                "com.coloros.phonemanager",
                "com.coloros.phonemanager.feature.startup.StartupActivity"
            )
        }
        val colorOsOld = Intent().apply {
            component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        }
        val colorOsFallback = Intent().apply {
            component = ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        }

        return when {
            canResolveIntent(context, colorOsNew) -> colorOsNew
            canResolveIntent(context, colorOsOld) -> colorOsOld
            canResolveIntent(context, colorOsFallback) -> colorOsFallback
            else -> colorOsNew
        }
    }

    private fun getVivoIntent(context: Context): Intent {
        val vivoNew = Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        }
        val vivoOld = Intent().apply {
            component = ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        }
        val vivoFallback = Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.PurviewTabActivity"
            )
        }

        return when {
            canResolveIntent(context, vivoNew) -> vivoNew
            canResolveIntent(context, vivoOld) -> vivoOld
            canResolveIntent(context, vivoFallback) -> vivoFallback
            else -> vivoNew
        }
    }

    private fun getSamsungIntent(context: Context): Intent {
        val deviceCare = Intent().apply {
            component = ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity"
            )
        }
        val batteryOpt = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

        return when {
            canResolveIntent(context, deviceCare) -> deviceCare
            canResolveIntent(context, batteryOpt) -> batteryOpt
            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }

    private fun getMeizuIntent(context: Context): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.meizu.safe",
                "com.meizu.safe.permission.SmartPermissionActivity"
            )
            putExtra("packageName", context.packageName)
        }
    }

    private fun getLeEcoIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        }
    }

    private fun getAsusIntent(context: Context): Intent {
        val asusNew = Intent().apply {
            component = ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity"
            )
            data = Uri.parse("mobilemanager://function/entry/AutoStart")
        }
        val asusOld = Intent().apply {
            component = ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.autostart.AutoStartActivity"
            )
        }

        return when {
            canResolveIntent(context, asusNew) -> asusNew
            canResolveIntent(context, asusOld) -> asusOld
            else -> asusNew
        }
    }

    private fun getNokiaIntent(context: Context): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.evenwell.powersaving.g3",
                "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"
            )
        }
    }

    private fun getLenovoIntent(context: Context): Intent {
        val lenovoNew = Intent().apply {
            component = ComponentName(
                "com.lenovo.powersetting",
                "com.lenovo.powersetting.ui.Settings\$BackgroundAppMgmtActivity"
            )
        }
        val lenovoOld = Intent().apply {
            component = ComponentName(
                "com.lenovo.security",
                "com.lenovo.security.purebackground.PureBackgroundActivity"
            )
        }
        return if (canResolveIntent(context, lenovoNew)) lenovoNew else lenovoOld
    }

    private fun canResolveIntent(context: Context, intent: Intent): Boolean {
        return try {
            context.packageManager.resolveActivity(intent, 0) != null
        } catch (_: Exception) {
            false
        }
    }

    private fun openFallbackSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback settings also failed", e)
        }
    }
}
