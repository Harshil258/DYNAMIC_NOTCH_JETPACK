package ai.emots.kishan_dynamic.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

enum class AutoStartOem(val displayName: String) {
    XIAOMI("MIUI"),
    HUAWEI("EMUI"),
    ONEPLUS("OxygenOS"),
    OPPO("ColorOS"),
    VIVO("FuntouchOS"),
    SAMSUNG("Samsung"),
    OTHER("")
}

/** Pure manufacturer classification used by the settings-intent boundary. */
object AutoStartSettings {
    fun oemFor(manufacturer: String, brand: String): AutoStartOem {
        val make = manufacturer.lowercase()
        val family = brand.lowercase()
        return when {
            make.contains("xiaomi") || family.contains("xiaomi") ||
                family.contains("redmi") || family.contains("poco") -> AutoStartOem.XIAOMI
            make.contains("huawei") || family.contains("huawei") || family.contains("honor") ->
                AutoStartOem.HUAWEI
            make.contains("oneplus") || family.contains("oneplus") -> AutoStartOem.ONEPLUS
            make.contains("oppo") || family.contains("oppo") || family.contains("realme") ->
                AutoStartOem.OPPO
            make.contains("vivo") || family.contains("vivo") -> AutoStartOem.VIVO
            make.contains("samsung") || family.contains("samsung") -> AutoStartOem.SAMSUNG
            else -> AutoStartOem.OTHER
        }
    }

    fun oemForDevice(): AutoStartOem = oemFor(Build.MANUFACTURER, Build.BRAND)

    fun isSupported(context: Context): Boolean = candidates(context).any { canResolve(context, it) }

    /** Opens the best OEM screen, falling back to this app's details page. */
    fun open(context: Context): Boolean {
        val target = candidates(context).firstOrNull { canResolve(context, it) }
        return try {
            context.startActivity((target ?: fallback(context)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            target != null
        } catch (_: Exception) {
            runCatching {
                context.startActivity(fallback(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
        }
    }

    private fun candidates(context: Context): List<Intent> = when (oemForDevice()) {
        AutoStartOem.XIAOMI -> listOf(
            component("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            }
        )
        AutoStartOem.HUAWEI -> listOf(
            component("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            component("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupAppControlActivity")
        )
        AutoStartOem.ONEPLUS -> listOf(
            component("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
            Intent("com.oneplus.action.STARTUP_MANAGER")
        )
        AutoStartOem.OPPO -> listOf(
            component("com.coloros.phonemanager", "com.coloros.phonemanager.feature.startup.StartupActivity"),
            component("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            component("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
        )
        AutoStartOem.VIVO -> listOf(
            component("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            component("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        )
        AutoStartOem.SAMSUNG -> listOf(
            component("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
        )
        AutoStartOem.OTHER -> emptyList()
    }

    private fun component(packageName: String, className: String): Intent =
        Intent().setComponent(ComponentName(packageName, className))

    private fun canResolve(context: Context, intent: Intent): Boolean =
        intent.resolveActivity(context.packageManager) != null

    private fun fallback(context: Context): Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
}
