package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Tracks Island-only dismissals and evaluates whether an incoming update
 * represents an insignificant update (suppressed) or a significant milestone (resurfaces).
 *
 * Implements owner rules UX-C08, UX-C09, UX-C10, and acceptance tests A21–A28.
 */
data class IslandTombstone(
    val key: String,
    val dismissedAtMonotonic: Long,
    val lastTitle: String,
    val lastText: String,
    val lastExpandedText: String,
    val lastTimestamp: Long,
    val lastProgress: Int,
    val isDelivery: Boolean,
    val hasReachedNearbyMilestone: Boolean = false,
    val hasReachedArrivingMilestone: Boolean = false,
    val hasReachedDeliveredMilestone: Boolean = false
)

sealed interface MilestoneEvaluation {
    data object SuppressedMinorUpdate : MilestoneEvaluation
    data class ResurfaceMilestone(val reason: String) : MilestoneEvaluation
    data object NotSuppressed : MilestoneEvaluation
}

object IslandSuppressionTombstoneRegistry {

    private val deliveryCategories = setOf("transport", "delivery", "progress")
    private val globalTombstones = java.util.concurrent.ConcurrentHashMap<String, IslandTombstone>()

    fun recordDismissal(notification: NotificationInfo, nowMonotonic: Long = System.nanoTime() / 1_000_000L) {
        globalTombstones[notification.id] = createTombstone(notification, nowMonotonic)
    }

    fun isSuppressed(key: String): Boolean = globalTombstones.containsKey(key)

    fun shouldResurface(incoming: NotificationInfo, nowMonotonic: Long = System.nanoTime() / 1_000_000L): Boolean {
        val tombstone = globalTombstones[incoming.id] ?: return true
        return when (evaluateUpdate(incoming, tombstone)) {
            is MilestoneEvaluation.ResurfaceMilestone -> {
                globalTombstones[incoming.id] = updateTombstoneAfterResurface(tombstone, incoming, nowMonotonic)
                true
            }
            is MilestoneEvaluation.SuppressedMinorUpdate -> false
            is MilestoneEvaluation.NotSuppressed -> {
                globalTombstones.remove(incoming.id)
                true
            }
        }
    }

    fun clear(key: String) {
        globalTombstones.remove(key)
    }

    fun clearAll() {
        globalTombstones.clear()
    }

    fun isDeliveryNotification(info: NotificationInfo): Boolean {
        val cat = info.category.lowercase()
        val text = "${info.packageName} ${info.title} ${info.text} ${info.expandedText}".lowercase()
        return cat in deliveryCategories ||
            text.contains("delivery") ||
            text.contains("out for delivery") ||
            text.contains("stops away") ||
            text.contains("arriving") ||
            text.contains("driver") ||
            text.contains("order") ||
            text.contains("courier") ||
            text.contains("package") ||
            text.contains("shipment") ||
            text.contains("track")
    }

    fun isNearbyMilestone(info: NotificationInfo): Boolean {
        val text = "${info.title} ${info.text} ${info.expandedText}".lowercase()
        return text.contains("stops away") ||
            text.contains("stop away") ||
            text.contains("nearby") ||
            text.contains("approaching") ||
            text.contains("driver is close") ||
            text.contains("almost there")
    }

    fun isArrivingMilestone(info: NotificationInfo): Boolean {
        val text = "${info.title} ${info.text} ${info.expandedText}".lowercase()
        return text.contains("arriving now") ||
            text.contains("arriving soon") ||
            text.contains("at your door") ||
            text.contains("at delivery address") ||
            text.contains("driver has arrived")
    }

    fun isDeliveredMilestone(info: NotificationInfo): Boolean {
        val text = "${info.title} ${info.text} ${info.expandedText}".lowercase()
        return text.contains("delivered") ||
            text.contains("package delivered") ||
            text.contains("order completed") ||
            text.contains("dropped off")
    }

    fun evaluateUpdate(
        incoming: NotificationInfo,
        tombstone: IslandTombstone?
    ): MilestoneEvaluation {
        if (tombstone == null) return MilestoneEvaluation.NotSuppressed

        // Rule A27: For messaging, actual new message content or newer message timestamp resurrects.
        val isMessaging = !tombstone.isDelivery &&
            (incoming.category in setOf("msg", "message") ||
                NotificationPriorityPolicy.messagingPackages.any { incoming.packageName.contains(it) })

        if (isMessaging) {
            val contentChanged = incoming.text != tombstone.lastText ||
                incoming.expandedText != tombstone.lastExpandedText ||
                incoming.inboxLines != emptyList<String>()
            val timestampNewer = incoming.timestamp > tombstone.lastTimestamp + 1000L

            return if (contentChanged || timestampNewer) {
                MilestoneEvaluation.ResurfaceMilestone("new_message_content")
            } else {
                MilestoneEvaluation.SuppressedMinorUpdate
            }
        }

        // Rule A21–A26: Delivery milestone rules
        if (tombstone.isDelivery) {
            // Check delivered milestone
            if (isDeliveredMilestone(incoming) && !tombstone.hasReachedDeliveredMilestone) {
                return MilestoneEvaluation.ResurfaceMilestone("delivery_delivered")
            }

            // Check arriving milestone
            if (isArrivingMilestone(incoming) && !tombstone.hasReachedArrivingMilestone) {
                return MilestoneEvaluation.ResurfaceMilestone("delivery_arriving")
            }

            // Check nearby milestone (first nearby alert resurrects once, A22 vs A23)
            if (isNearbyMilestone(incoming) && !tombstone.hasReachedNearbyMilestone) {
                return MilestoneEvaluation.ResurfaceMilestone("delivery_first_nearby")
            }

            // Minor progress increments (e.g. 60->70, stop 2->1) or ETA jitter are suppressed (A21, A23, A26)
            return MilestoneEvaluation.SuppressedMinorUpdate
        }

        // General notifications: if text changed meaningfully, resurrect; otherwise suppress minor progress/repaints
        val hasMeaningfulTextChange = incoming.title != tombstone.lastTitle ||
            incoming.text != tombstone.lastText ||
            incoming.expandedText != tombstone.lastExpandedText

        return if (hasMeaningfulTextChange) {
            MilestoneEvaluation.ResurfaceMilestone("content_text_changed")
        } else {
            MilestoneEvaluation.SuppressedMinorUpdate
        }
    }

    fun createTombstone(
        notification: NotificationInfo,
        nowMonotonic: Long
    ): IslandTombstone {
        val isDel = isDeliveryNotification(notification)
        return IslandTombstone(
            key = notification.id,
            dismissedAtMonotonic = nowMonotonic,
            lastTitle = notification.title,
            lastText = notification.text,
            lastExpandedText = notification.expandedText,
            lastTimestamp = notification.timestamp,
            lastProgress = notification.progress,
            isDelivery = isDel,
            hasReachedNearbyMilestone = isDel && isNearbyMilestone(notification),
            hasReachedArrivingMilestone = isDel && isArrivingMilestone(notification),
            hasReachedDeliveredMilestone = isDel && isDeliveredMilestone(notification)
        )
    }

    fun updateTombstoneAfterResurface(
        tombstone: IslandTombstone,
        incoming: NotificationInfo,
        nowMonotonic: Long
    ): IslandTombstone {
        val isDel = tombstone.isDelivery || isDeliveryNotification(incoming)
        return tombstone.copy(
            dismissedAtMonotonic = nowMonotonic,
            lastTitle = incoming.title,
            lastText = incoming.text,
            lastExpandedText = incoming.expandedText,
            lastTimestamp = incoming.timestamp,
            lastProgress = incoming.progress,
            isDelivery = isDel,
            hasReachedNearbyMilestone = tombstone.hasReachedNearbyMilestone || (isDel && isNearbyMilestone(incoming)),
            hasReachedArrivingMilestone = tombstone.hasReachedArrivingMilestone || (isDel && isArrivingMilestone(incoming)),
            hasReachedDeliveredMilestone = tombstone.hasReachedDeliveredMilestone || (isDel && isDeliveredMilestone(incoming))
        )
    }
}
