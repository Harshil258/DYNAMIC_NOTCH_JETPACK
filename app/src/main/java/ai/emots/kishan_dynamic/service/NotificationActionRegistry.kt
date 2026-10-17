package ai.emots.kishan_dynamic.service

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.concurrent.ConcurrentHashMap
import ai.emots.kishan_dynamic.data.notification.NotificationActionDispatch
import ai.emots.kishan_dynamic.data.notification.NotificationDispatchOutcome

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
    private val nextActionVersion = java.util.concurrent.atomic.AtomicLong()

    /** Reuse only the same slot AND actual PendingIntent identity, never a hash. */
    fun idFor(notificationId: String, index: Int, intent: PendingIntent): String {
        val prefix = "$index:"
        return actions[notificationId]?.entries?.firstOrNull {
            it.key.startsWith(prefix) && it.value.pendingIntent == intent
        }?.key ?: "$prefix${nextActionVersion.incrementAndGet()}"
    }

    fun replace(notificationId: String, intents: Map<String, Target>) {
        if (intents.isEmpty()) actions.remove(notificationId)
        else actions[notificationId] = intents
    }

    fun send(notificationId: String, actionId: String): Boolean {
        val target = actions[notificationId]?.get(actionId) ?: return false
        return (NotificationActionDispatch.attempt(true) { target.pendingIntent.send() } ==
            NotificationDispatchOutcome.HANDED_TO_APP).also { sent ->
            if (!sent) removeAction(notificationId, actionId)
        }
    }

    fun hasReplyTarget(notificationId: String, actionId: String): Boolean =
        actions[notificationId]?.get(actionId)?.remoteInputs?.any { it.allowFreeFormInput } == true

    fun sendReply(
        context: Context,
        notificationId: String,
        actionId: String,
        reply: String
    ): Boolean {
        val target = actions[notificationId]?.get(actionId) ?: return false
        val textInputs = target.remoteInputs.filter { it.allowFreeFormInput }
        if (textInputs.isEmpty() || reply.isBlank()) return false

        val results = Bundle().apply {
            textInputs.forEach { input ->
                putCharSequence(input.resultKey, reply)
            }
        }
        val fillInIntent = Intent()
        RemoteInput.addResultsToIntent(textInputs.toTypedArray(), fillInIntent, results)
        return (NotificationActionDispatch.attempt(true) {
            target.pendingIntent.send(context, 0, fillInIntent)
        } == NotificationDispatchOutcome.HANDED_TO_APP).also { sent ->
            if (!sent) removeAction(notificationId, actionId)
        }
    }

    fun remove(notificationId: String) {
        actions.remove(notificationId)
    }

    fun clear() {
        actions.clear()
    }

    private fun removeAction(notificationId: String, actionId: String) {
        actions.computeIfPresent(notificationId) { _, current ->
            current.toMutableMap().apply { remove(actionId) }.takeIf { it.isNotEmpty() }
        }
    }
}
