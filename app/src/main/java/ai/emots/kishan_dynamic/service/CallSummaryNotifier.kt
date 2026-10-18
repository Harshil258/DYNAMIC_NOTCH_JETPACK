package ai.emots.kishan_dynamic.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.MainActivity
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord

/**
 * Dispatches high-priority heads-up notifications for call summaries.
 * Serves as the guaranteed multi-surface fallback when the overlay service is
 * not active or when background activity launch is restricted by Android 10+.
 */
object CallSummaryNotifier {

    const val CHANNEL_ID = "channel_call_summary"
    private const val NOTIFICATION_ID = 4001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Call Summary"
            val descriptionText = "Displays duration and follow-up actions when a phone call ends"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showCallSummaryNotification(context: Context, record: CallRecord) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val durationFormatted = when {
            record.direction == CallDirection.MISSED -> "Missed"
            else -> {
                val mins = record.durationSeconds / 60L
                val secs = record.durationSeconds % 60L
                "%02d:%02d".format(mins, secs)
            }
        }

        val callType = when (record.direction) {
            CallDirection.INCOMING -> "Incoming call"
            CallDirection.OUTGOING -> "Outgoing call"
            CallDirection.MISSED -> "Missed call"
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(MainActivity.EXTRA_OPEN_CALL_SUMMARY, true)
            putExtra("call_record_id", record.id)
            putExtra("call_contact_name", record.contactName)
            putExtra("call_phone_number", record.phoneNumber)
            putExtra("call_duration_seconds", record.durationSeconds)
            putExtra("call_direction", record.direction.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            record.id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(record.contactName)
            .setContentText("$callType · $durationFormatted")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        val rawNumber = record.phoneNumber.trim()
        val isValidNumber = rawNumber.isNotBlank() &&
            !rawNumber.equals("Unknown Caller", ignoreCase = true) &&
            !rawNumber.equals("Unknown", ignoreCase = true)

        if (isValidNumber) {
            // Action 1: Redial
            val dialIntent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${Uri.encode(rawNumber)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val redialPendingIntent = PendingIntent.getActivity(
                context,
                record.id.hashCode() + 1,
                dialIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_call, "Redial", redialPendingIntent)

            // Action 2: Message (SMS)
            val smsIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.parse("smsto:${Uri.encode(rawNumber)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val smsPendingIntent = PendingIntent.getActivity(
                context,
                record.id.hashCode() + 2,
                smsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_dialog_email, "Message", smsPendingIntent)

            // Action 3: WhatsApp (if applicable)
            val waFormatted = CallActionLauncher.formatWhatsAppNumber(rawNumber)
            if (waFormatted != null) {
                val waUri = Uri.parse("https://wa.me/$waFormatted")
                val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (CallActionLauncher.isWhatsAppInstalled(context)) {
                        setPackage(CallActionLauncher.PACKAGE_WHATSAPP)
                    }
                }
                val waPendingIntent = PendingIntent.getActivity(
                    context,
                    record.id.hashCode() + 3,
                    waIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_send, "WhatsApp", waPendingIntent)
            }
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun dismiss(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }
}
