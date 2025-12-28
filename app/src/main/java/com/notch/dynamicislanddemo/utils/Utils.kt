package com.notch.dynamicislanddemo.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object Utils {
    const val FROM_NOTIFICATION_SERVICE = "ccom.notch.dynamicislanddemo.FROM_NOTIFICATION_SERVICE."

    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName)
                return true
        }
        return false
    }
    
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return listeners?.contains(pkgName) == true
    }
}
