package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.repository.CallHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * BroadcastReceiver for phone telephony state changes (Ringing, Offhook, Idle).
 */
class TelephonyStateReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeContact: ContactInfo? = null
    private var activeStartedAtMillis: Long = 0L
    private var activeDirection: CallDirection? = null
    private var wentOffHook = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                ?: CallScreeningCoordinator.currentNumber()
                ?: "Unknown Caller"

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val contact = context.resolveContact(incomingNumber)
                    activeContact = contact
                    activeStartedAtMillis = System.currentTimeMillis()
                    activeDirection = CallDirection.INCOMING
                    wentOffHook = false
                    context?.applicationContext?.let { appContext ->
                        scope.launch {
                            if (AuroraPreferences(appContext).callBannerEnabled.first()) {
                                IslandStateManager.postIncomingCall(contact)
                            }
                        }
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    val contact = activeContact ?: context.resolveContact(incomingNumber)
                    activeContact = contact
                    if (activeStartedAtMillis == 0L) activeStartedAtMillis = System.currentTimeMillis()
                    if (activeDirection == null) activeDirection = CallDirection.OUTGOING
                    wentOffHook = true
                    context?.applicationContext?.let { appContext ->
                        scope.launch {
                            val preferences = AuroraPreferences(appContext)
                            if (preferences.callBannerEnabled.first()) {
                                IslandStateManager.postOngoingCall(
                                    contact = contact,
                                    durationSeconds = 0L,
                                    showDuration = preferences.callTimerEnabled.first(),
                                    isDialing = activeDirection == CallDirection.OUTGOING
                                )
                            }
                        }
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    val callWasConnected = wentOffHook
                    val contact = activeContact
                    if (contact != null) {
                        val duration = if (wentOffHook) {
                            ((System.currentTimeMillis() - activeStartedAtMillis) / 1000L).coerceAtLeast(0L)
                        } else 0L
                        val direction = if (wentOffHook) activeDirection ?: CallDirection.OUTGOING else CallDirection.MISSED
                        val record = CallRecord(
                            id = "${activeStartedAtMillis}-${contact.phoneNumber}",
                            contactName = contact.name,
                            phoneNumber = contact.phoneNumber,
                            avatarUri = contact.avatarUri,
                            startedAtMillis = activeStartedAtMillis,
                            durationSeconds = duration,
                            direction = direction
                        )
                        context?.applicationContext?.let { appContext ->
                            scope.launch {
                                CallHistoryRepository(appContext).add(record)
                                val preferences = AuroraPreferences(appContext)
                                if (PremiumFeaturePolicy.callSummaryEnabled(
                                        isProActive = preferences.isProActive.first(),
                                        requested = preferences.callSummaryEnabled.first()
                                    )
                                ) {
                                    IslandStateManager.postCallSummary(record)
                                }
                            }
                        }
                    }
                    activeContact = null
                    activeStartedAtMillis = 0L
                    activeDirection = null
                    wentOffHook = false
                    CallScreeningCoordinator.clear()
                    if (callWasConnected) IslandStateManager.endCall()
                    else IslandStateManager.declineIncomingCall()
                }
            }
        }
    }

    private fun Context?.resolveContact(phoneNumber: String): ContactInfo {
        val fallback = ContactInfo(name = phoneNumber, phoneNumber = phoneNumber)
        if (this == null || phoneNumber == "Unknown Caller" ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) return fallback

        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )
        return runCatching {
            contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                ContactInfo(
                    name = cursor.getString(0).orEmpty().ifBlank { phoneNumber },
                    phoneNumber = phoneNumber,
                    avatarUri = cursor.getString(1)
                )
            } ?: fallback
        }.getOrDefault(fallback)
    }
}
