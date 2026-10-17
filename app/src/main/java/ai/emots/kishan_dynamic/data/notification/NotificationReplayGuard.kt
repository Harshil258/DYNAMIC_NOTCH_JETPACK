package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/** Bounded, memory-only hashes prevent an expired preview replaying unchanged copy. */
class NotificationReplayGuard(private val capacity: Int = 80) {
    private val signatures = linkedMapOf<String, String>()
    fun isDuplicate(source: NotificationInfo): Boolean {
        val bytes = java.security.MessageDigest.getInstance("SHA-256")
            .digest(source.copy(timestamp = 0L).toString().toByteArray())
        val signature = bytes.joinToString("") { "%02x".format(it) }
        val duplicate = signatures[source.id] == signature
        signatures.remove(source.id)
        signatures[source.id] = signature
        while (signatures.size > capacity.coerceAtLeast(1)) signatures.remove(signatures.keys.first())
        return duplicate
    }
    fun forget(id: String) { signatures.remove(id) }
    fun clear() { signatures.clear() }
}
