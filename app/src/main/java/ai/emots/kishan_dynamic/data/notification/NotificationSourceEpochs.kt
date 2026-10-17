package ai.emots.kishan_dynamic.data.notification

/** Invalidates suspended ingestion after remove, replacement, disconnect or restart. */
class NotificationSourceEpochs {
    private var sequence = 0L
    private val current = mutableMapOf<String, Long>()
    @Synchronized fun begin(key: String): Long = (++sequence).also { current[key] = it }
    @Synchronized fun isCurrent(key: String, epoch: Long): Boolean = current[key] == epoch
    @Synchronized fun remove(key: String) { current.remove(key) }
    @Synchronized fun clear() { current.clear() }
}
