package ai.emots.kishan_dynamic.service

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps notification action intents outside of Compose. The listener owns the
 * platform objects; the island only receives a stable action id and asks this
 * registry to execute it.
 */
object NotificationActionRegistry {
    data class Target(
        val pendingIntent: PendingIntent,
        val remoteInputs: List<RemoteInput> = emptyList()
    )

    private val actions = ConcurrentHashMap<String, Map<String, Target>>()

    fun replace(notificationId: String, intents: Map<String, Target>) {
        if (intents.isEmpty()) actions.remove(notificationId)
        else actions[notificationId] = intents
    }

    fun send(notificationId: String, actionId: String): Boolean {
        val target = actions[notificationId]?.get(actionId) ?: return false
        return runCatching { target.pendingIntent.send() }.isSuccess
    }

    fun hasReplyTarget(notificationId: String, actionId: String): Boolean =
        actions[notificationId]?.get(actionId)?.remoteInputs?.isNotEmpty() == true

    fun sendReply(
        context: Context,
        notificationId: String,
        actionId: String,
        reply: String
    ): Boolean {
        val target = actions[notificationId]?.get(actionId) ?: return false
        if (target.remoteInputs.isEmpty() || reply.isBlank()) return false

        val results = Bundle().apply {
            target.remoteInputs.forEach { input ->
                putCharSequence(input.resultKey, reply)
            }
        }
        val fillInIntent = Intent()
        RemoteInput.addResultsToIntent(target.remoteInputs.toTypedArray(), fillInIntent, results)
        return runCatching {
            target.pendingIntent.send(context, 0, fillInIntent)
        }.isSuccess
    }

    fun remove(notificationId: String) {
        actions.remove(notificationId)
    }
}
