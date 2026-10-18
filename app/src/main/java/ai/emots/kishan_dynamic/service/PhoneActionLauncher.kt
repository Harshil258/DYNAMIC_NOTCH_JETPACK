package ai.emots.kishan_dynamic.service

import android.content.Context

/**
 * Platform boundary for user-initiated calls.
 * Delegates to [CallActionLauncher] for unified permission checking and dialing.
 */
object PhoneActionLauncher {

    fun actionFor(hasCallPhonePermission: Boolean): String =
        CallActionLauncher.callActionFor(hasCallPhonePermission)

    fun openCall(context: Context, phoneNumber: String): Boolean =
        CallActionLauncher.openCall(context, phoneNumber)
}
