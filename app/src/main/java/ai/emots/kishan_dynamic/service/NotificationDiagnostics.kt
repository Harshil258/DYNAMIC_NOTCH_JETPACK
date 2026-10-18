package ai.emots.kishan_dynamic.service

import android.util.Log
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.NotificationEventDecision

/** Metadata only: never titles, bodies, raw keys, group names, actions or extras. */
object NotificationDiagnostics {
    const val TAG = "IslandEngine"
    fun id(key: String): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(key.toByteArray()).take(6).joinToString("") { "%02x".format(it) }
    fun decision(source: NotificationInfo, decision: NotificationEventDecision, restored: Boolean) {
        val category = source.category.takeIf { it in setOf("call", "msg", "message", "alarm", "event", "transport", "navigation", "progress", "timer", "stopwatch", "email", "sys", "service", "status", "general") } ?: "other"
        runCatching {
            Log.i(TAG, "event=classify id=${id(source.id)} package=${source.packageName} " +
                "category=$category importance=${source.importance} " +
                "ongoing=${source.isOngoing} clearable=${source.isClearable} summary=${source.isGroupSummary} " +
                "kind=${decision.kind} reason=${decision.reason} restored=$restored")
        }
    }
    fun lifecycle(event: String, key: String, reason: String) {
        runCatching { Log.i(TAG, "event=$event id=${id(key)} reason=$reason") }
    }
    fun surface(suppressed: Boolean) {
        runCatching { Log.i(TAG, "event=overlay_context suppressed=$suppressed") }
    }
}
