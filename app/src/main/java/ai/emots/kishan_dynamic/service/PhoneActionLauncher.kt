package ai.emots.kishan_dynamic.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * Platform boundary for user-initiated calls.
 *
 * Direct calling is used only after Android has granted CALL_PHONE. The
 * dialer remains the honest fallback for optional permissions and devices
 * where the app is not allowed to place calls itself.
 */
object PhoneActionLauncher {

    fun actionFor(hasCallPhonePermission: Boolean): String =
        if (hasCallPhonePermission) Intent.ACTION_CALL else Intent.ACTION_DIAL

    fun openCall(context: Context, phoneNumber: String): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        return runCatching {
            context.startActivity(
                Intent(
                    actionFor(hasPermission),
                    Uri.parse("tel:${Uri.encode(phoneNumber)}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.isSuccess
    }
}
