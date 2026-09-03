package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import ai.emots.kishan_dynamic.data.model.ContactInfo

/**
 * BroadcastReceiver for phone telephony state changes (Ringing, Offhook, Idle).
 */
class TelephonyStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Unknown Caller"

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val contact = ContactInfo(
                        name = incomingNumber,
                        phoneNumber = incomingNumber
                    )
                    IslandStateManager.postIncomingCall(contact)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    val contact = ContactInfo(
                        name = incomingNumber,
                        phoneNumber = incomingNumber
                    )
                    IslandStateManager.postOngoingCall(contact, durationSeconds = 0L)
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    IslandStateManager.endCall()
                }
            }
        }
    }
}
