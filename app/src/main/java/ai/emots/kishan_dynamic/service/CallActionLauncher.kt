package ai.emots.kishan_dynamic.service

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

/**
 * Robust platform launcher for call-related actions:
 * - Direct dial / action call with CALL_PHONE permission handling
 * - SMS with prefilled recipient
 * - WhatsApp direct chat via https://wa.me/ with international number normalization
 * - Save contact via ContactsContract
 * - Clipboard copy with feedback
 */
object CallActionLauncher {

    const val PACKAGE_WHATSAPP = "com.whatsapp"
    const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    /**
     * Determines whether ACTION_CALL (direct call) or ACTION_DIAL (open dialer)
     * should be used based on permission.
     */
    fun callActionFor(hasCallPhonePermission: Boolean): String =
        if (hasCallPhonePermission) Intent.ACTION_CALL else Intent.ACTION_DIAL

    /**
     * Initiates a phone call or opens the dialer with the phone number.
     */
    fun openCall(context: Context?, phoneNumber: String): Boolean {
        if (context == null) return false
        val trimmed = phoneNumber.trim()
        if (trimmed.isBlank() || trimmed.equals("Unknown Caller", ignoreCase = true)) return false
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        return runCatching {
            val intent = Intent(
                callActionFor(hasPermission),
                Uri.parse("tel:${Uri.encode(trimmed)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Opens the default SMS messaging application with the recipient pre-filled.
     */
    fun openSms(context: Context?, phoneNumber: String): Boolean {
        if (context == null) return false
        val trimmed = phoneNumber.trim()
        if (trimmed.isBlank() || trimmed.equals("Unknown Caller", ignoreCase = true)) return false
        return runCatching {
            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.parse("smsto:${Uri.encode(trimmed)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Normalizes a phone number for the WhatsApp wa.me API:
     * - Strips all non-digit characters (spaces, dashes, parentheses, plus signs)
     * - Strips leading double zeros "00"
     * - Strips leading single zero for 11-digit numbers (common domestic trunk prefix)
     * - If 10 digits without country code, prefixes default Indian country code "91"
     * - Validates length is between 10 and 15 digits
     * - Returns null if the number is invalid, unknown, or too short
     */
    fun formatWhatsAppNumber(rawNumber: String, defaultCountryCode: String = "91"): String? {
        val trimmed = rawNumber.trim()
        if (trimmed.isBlank() ||
            trimmed.equals("Unknown Caller", ignoreCase = true) ||
            trimmed.equals("Unknown", ignoreCase = true) ||
            trimmed.equals("Private Number", ignoreCase = true)
        ) {
            return null
        }

        // Keep only digits
        var digits = trimmed.filter { it.isDigit() }
        if (digits.isBlank()) return null

        // Handle leading double-zeros (international prefix)
        if (digits.startsWith("00")) {
            digits = digits.substring(2)
        }

        // Handle single leading zero for domestic calls (e.g. 09876543210 -> 9876543210)
        if (digits.startsWith("0") && digits.length == 11) {
            digits = digits.substring(1)
        }

        // If standard 10-digit mobile number, prefix default country code (e.g. 91)
        if (digits.length == 10 && !defaultCountryCode.isBlank()) {
            digits = defaultCountryCode.filter { it.isDigit() } + digits
        }

        // Valid international telephone numbers have between 10 and 15 digits
        if (digits.length in 10..15) {
            return digits
        }

        return null
    }

    /**
     * Checks if WhatsApp or WhatsApp Business is installed on the device.
     */
    fun isWhatsAppInstalled(context: Context?): Boolean {
        if (context == null) return false
        val pm = context.packageManager
        return runCatching {
            pm.getPackageInfo(PACKAGE_WHATSAPP, 0)
            true
        }.getOrElse {
            runCatching {
                pm.getPackageInfo(PACKAGE_WHATSAPP_BUSINESS, 0)
                true
            }.getOrDefault(false)
        }
    }

    /**
     * Opens a direct WhatsApp chat with the specified number using the wa.me protocol.
     * Safely falls back to web or generic app chooser if direct package intent is unavailable.
     */
    fun openWhatsApp(context: Context?, phoneNumber: String): Boolean {
        if (context == null) return false
        val formattedNumber = formatWhatsAppNumber(phoneNumber) ?: return false
        val uri = Uri.parse("https://wa.me/$formattedNumber")

        // Try direct launch targeting WhatsApp app if installed
        val pm = context.packageManager
        val targetPackage = when {
            runCatching { pm.getPackageInfo(PACKAGE_WHATSAPP, 0); true }.getOrDefault(false) -> PACKAGE_WHATSAPP
            runCatching { pm.getPackageInfo(PACKAGE_WHATSAPP_BUSINESS, 0); true }.getOrDefault(false) -> PACKAGE_WHATSAPP_BUSINESS
            else -> null
        }

        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (targetPackage != null) {
                    setPackage(targetPackage)
                }
            }
            context.startActivity(intent)
            true
        }.recoverCatching {
            // Fallback to generic ACTION_VIEW without package restriction
            val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
            true
        }.getOrDefault(false)
    }

    /**
     * Copies the phone number to the system clipboard.
     */
    fun copyToClipboard(context: Context?, phoneNumber: String): Boolean {
        if (context == null) return false
        val trimmed = phoneNumber.trim()
        if (trimmed.isBlank() || trimmed.equals("Unknown Caller", ignoreCase = true)) return false
        return runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Phone number", trimmed))
            true
        }.getOrDefault(false)
    }

    /**
     * Opens the system contact insertion screen to save the caller.
     */
    fun openSaveContact(context: Context?, name: String, phoneNumber: String): Boolean {
        if (context == null) return false
        val trimmedNumber = phoneNumber.trim()
        val trimmedName = name.trim().takeUnless {
            it.equals("Unknown Caller", ignoreCase = true) || it.equals(trimmedNumber, ignoreCase = true)
        }.orEmpty()

        return runCatching {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                if (trimmedName.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.NAME, trimmedName)
                }
                if (trimmedNumber.isNotBlank() && !trimmedNumber.equals("Unknown Caller", ignoreCase = true)) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, trimmedNumber)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
