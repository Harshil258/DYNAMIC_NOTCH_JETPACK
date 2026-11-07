package ai.emots.kishan_dynamic.data.notification

import java.security.MessageDigest

/**
 * Structured, PII-free debug trace logging for Dynamic Island behavior decisions.
 * Hashes notification keys and logs only operational metadata (event, mode, count, reason).
 */
data class IslandTraceRecord(
    val timestamp: Long,
    val eventType: String,
    val redactedKey: String,
    val queueCount: Int,
    val ownershipBefore: String,
    val ownershipAfter: String,
    val presentationBefore: String,
    val presentationAfter: String,
    val action: String,
    val reason: String
)

object IslandEventTrace {

    private val history = mutableListOf<IslandTraceRecord>()
    private const val MAX_HISTORY = 100

    fun hashKey(key: String): String = runCatching {
        MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray())
            .take(4)
            .joinToString("") { "%02x".format(it) }
    }.getOrDefault("anon")

    @Synchronized
    fun record(
        nowMonotonic: Long,
        eventType: String,
        rawKey: String?,
        queueCount: Int,
        ownershipBefore: String,
        ownershipAfter: String,
        presentationBefore: String,
        presentationAfter: String,
        action: String,
        reason: String
    ): IslandTraceRecord {
        val record = IslandTraceRecord(
            timestamp = nowMonotonic,
            eventType = eventType,
            redactedKey = rawKey?.let(::hashKey) ?: "none",
            queueCount = queueCount,
            ownershipBefore = ownershipBefore,
            ownershipAfter = ownershipAfter,
            presentationBefore = presentationBefore,
            presentationAfter = presentationAfter,
            action = action,
            reason = reason
        )
        if (history.size >= MAX_HISTORY) {
            history.removeAt(0)
        }
        history.add(record)
        return record
    }

    @Synchronized
    fun getRecentHistory(): List<IslandTraceRecord> = history.toList()

    @Synchronized
    fun clear() {
        history.clear()
    }
}
