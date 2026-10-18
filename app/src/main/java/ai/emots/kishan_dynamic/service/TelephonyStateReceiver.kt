package ai.emots.kishan_dynamic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * BroadcastReceiver for phone telephony state changes (Ringing, Offhook, Idle).
 *
 * Implements a completely stateless forwarder to [CallSessionCoordinator].
 * Uses [goAsync] to guarantee asynchronous contact lookup and state dispatch
 * without blocking the main thread or risking receiver destruction before completion.
 */
class TelephonyStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val pendingResult = goAsync()
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            CallSessionCoordinator.onPhoneStateChanged(
                context = context.applicationContext,
                stateStr = stateStr,
                incomingNumberExtra = incomingNumber,
                onComplete = {
                    runCatching { pendingResult.finish() }
                }
            )
        }
    }
}
