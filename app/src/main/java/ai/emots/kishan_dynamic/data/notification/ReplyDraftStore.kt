package ai.emots.kishan_dynamic.data.notification

/** Unlocked-session memory only. No persistence, analytics, coroutine or notification ownership. */
class ReplyDraftStore(private val now: () -> Long = { System.nanoTime() / 1_000_000L }) {
    private data class Draft(val text: String, val expiresAt: Long)
    private val drafts = linkedMapOf<Pair<String,String>,Draft>()
    private var restricted = false
    @Synchronized fun read(source: String, action: String): String {
        prune()
        return if (restricted) "" else drafts[source to action]?.text.orEmpty()
    }
    @Synchronized fun write(source: String, action: String, text: String) {
        prune()
        if (restricted) return
        drafts[source to action] = Draft(text, now() + 60_000L)
        while (drafts.size > 50) drafts.remove(drafts.keys.first())
    }
    @Synchronized fun clear(source: String) { drafts.keys.removeAll { it.first == source } }
    @Synchronized fun retainSources(sources: Set<String>) { drafts.keys.removeAll { it.first !in sources } }
    @Synchronized fun privacyRestricted(value: Boolean) {
        restricted = value
        if (value) drafts.clear()
    }
    private fun prune() { val time = now(); drafts.entries.removeAll { it.value.expiresAt <= time } }
}

object ReplyDraftMemory { val store = ReplyDraftStore() }
