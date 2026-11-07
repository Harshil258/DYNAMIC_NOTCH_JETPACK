package ai.emots.kishan_dynamic.data.notification

/**
 * Tracks Aurora's read state independently from Android's active notification queue.
 * It never invokes a source action or cancels a platform notification.
 */
class NotificationVisibilityState {
    private val activeIds = linkedSetOf<String>()
    private val seenIds = mutableSetOf<String>()

    /** Syncs after queue mutations. New IDs become unseen; existing IDs keep their read state. */
    fun synchronize(activeNotificationIds: Collection<String>) {
        val nextIds = activeNotificationIds.toCollection(linkedSetOf())
        val newlyActive = nextIds - activeIds
        activeIds.retainAll(nextIds)
        seenIds.retainAll(nextIds)
        seenIds.removeAll(newlyActive)
        activeIds.addAll(nextIds)
    }

    /** Marks only an active source as seen in Aurora. */
    fun markSeen(notificationId: String): Boolean =
        notificationId in activeIds && seenIds.add(notificationId)

    fun isActive(notificationId: String): Boolean = notificationId in activeIds

    fun isSeen(notificationId: String): Boolean = notificationId in seenIds

    fun unseenIds(): Set<String> = activeIds.filterNot(seenIds::contains).toSet()

    fun unseenCount(): Int = activeIds.count { it !in seenIds }

    fun clear() {
        activeIds.clear()
        seenIds.clear()
    }
}
