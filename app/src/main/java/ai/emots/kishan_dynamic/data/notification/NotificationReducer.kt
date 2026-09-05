package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/** Immutable notification queue consumed by the island state coordinator. */
data class NotificationQueueState(
    val notifications: List<NotificationInfo> = emptyList(),
    val activeNotificationId: String? = null
) {
    val activeNotification: NotificationInfo?
        get() = notifications.firstOrNull { it.id == activeNotificationId }
            ?: notifications.firstOrNull()

    val activeIndex: Int
        get() = notifications.indexOfFirst { it.id == activeNotification?.id }.coerceAtLeast(0)
}

sealed interface NotificationEvent {
    data class Posted(
        val notification: NotificationInfo,
        val isSilentUpdate: Boolean = false
    ) : NotificationEvent

    data class Removed(val notificationId: String) : NotificationEvent
    data class Expired(val notificationId: String) : NotificationEvent
    data class Selected(val notificationId: String) : NotificationEvent
    data object DismissActive : NotificationEvent
    data object Next : NotificationEvent
    data object Previous : NotificationEvent
    data object Cleared : NotificationEvent
}

/**
 * Pure reducer for notification ordering, deduplication, merging, and lifecycle.
 * Android services and Compose never mutate the queue directly, which keeps
 * priority behavior deterministic and testable.
 */
class NotificationReducer {

    fun reduce(state: NotificationQueueState, event: NotificationEvent): NotificationQueueState {
        return when (event) {
            is NotificationEvent.Posted -> {
                val incoming = event.notification
                val existingIndex = findMatchingIndex(state.notifications, incoming)

                val updatedList = if (existingIndex != -1) {
                    val existing = state.notifications[existingIndex]
                    val merged = mergeNotification(existing, incoming)

                    // Content change check
                    val contentChanged = existing.title != merged.title ||
                        existing.text != merged.text ||
                        existing.expandedText != merged.expandedText

                    val isOngoingProgressOnly = incoming.isOngoing && !contentChanged

                    if (isOngoingProgressOnly || event.isSilentUpdate) {
                        // Update in place silently without bumping priority or moving to top
                        state.notifications.toMutableList().apply {
                            set(existingIndex, merged)
                        }
                    } else {
                        // Remove from old position, prepend to top
                        val mutable = state.notifications.toMutableList()
                        mutable.removeAt(existingIndex)
                        mutable.add(0, merged)
                        sortQueue(mutable)
                    }
                } else {
                    // New notification: add to queue and sort
                    val combined = listOf(incoming) + state.notifications
                    sortQueue(combined)
                }

                // Cap queue to 20 notifications to prevent unbounded memory growth
                val cappedList = updatedList.take(MAX_NOTIFICATIONS)

                val newActiveId = if (event.isSilentUpdate && state.activeNotificationId != null) {
                    // If silent update (e.g. progress tick), preserve currently active notification
                    state.activeNotificationId
                } else if (existingIndex != -1 && incoming.isOngoing && existingIndex != 0) {
                    // Ongoing progress on non-active item keeps current active item
                    state.activeNotificationId ?: incoming.id
                } else {
                    incoming.id
                }

                NotificationQueueState(cappedList, newActiveId)
            }

            is NotificationEvent.Removed -> {
                val remaining = state.notifications.filterNot { it.id == event.notificationId }
                NotificationQueueState(
                    notifications = remaining,
                    activeNotificationId = remaining.firstOrNull()?.id
                )
            }

            is NotificationEvent.Expired -> {
                val remaining = state.notifications.filterNot { it.id == event.notificationId }
                NotificationQueueState(
                    notifications = remaining,
                    activeNotificationId = remaining.firstOrNull()?.id
                )
            }

            is NotificationEvent.DismissActive -> {
                val currentActiveId = state.activeNotification?.id ?: return state
                val remaining = state.notifications.filterNot { it.id == currentActiveId }
                NotificationQueueState(
                    notifications = remaining,
                    activeNotificationId = remaining.firstOrNull()?.id
                )
            }

            is NotificationEvent.Next -> {
                if (state.notifications.size <= 1) return state
                val currentIndex = state.activeIndex
                val nextIndex = (currentIndex + 1) % state.notifications.size
                state.copy(activeNotificationId = state.notifications[nextIndex].id)
            }

            is NotificationEvent.Previous -> {
                if (state.notifications.size <= 1) return state
                val currentIndex = state.activeIndex
                val prevIndex = if (currentIndex <= 0) state.notifications.lastIndex else currentIndex - 1
                state.copy(activeNotificationId = state.notifications[prevIndex].id)
            }

            is NotificationEvent.Selected -> {
                if (state.notifications.none { it.id == event.notificationId }) state
                else state.copy(activeNotificationId = event.notificationId)
            }

            NotificationEvent.Cleared -> NotificationQueueState()
        }
    }

    private fun sortQueue(list: List<NotificationInfo>): List<NotificationInfo> {
        return list.sortedWith(
            compareByDescending<NotificationInfo> { it.isPriority }
                .thenByDescending { it.isOngoing }
                .thenByDescending { it.timestamp }
        )
    }

    /**
     * Matching priority for Android notification deduplication:
     * 1. Exact ID match (sbn.key)
     * 2. Package + non-blank sender title (same chat / sender in WhatsApp, Instagram, Telegram)
     * 3. GroupKey + non-blank title (Android group grouping)
     */
    fun findMatchingIndex(
        notifications: List<NotificationInfo>,
        incoming: NotificationInfo
    ): Int {
        // 1. Exact ID
        val exactIndex = notifications.indexOfFirst { it.id == incoming.id }
        if (exactIndex != -1) return exactIndex

        // 2. Package + non-blank sender title
        if (incoming.title.isNotBlank()) {
            val pkgTitleIndex = notifications.indexOfFirst {
                it.packageName == incoming.packageName && it.title == incoming.title
            }
            if (pkgTitleIndex != -1) return pkgTitleIndex
        }

        // 3. GroupKey + non-blank title
        if (incoming.title.isNotBlank()) {
            val groupIndex = notifications.indexOfFirst {
                it.groupKey == incoming.groupKey && it.title == incoming.title
            }
            if (groupIndex != -1) return groupIndex
        }

        return -1
    }

    /**
     * Intelligently merges fields from an existing notification with an incoming update.
     * Preserves actionable buttons (Reply, Mark as Read) and images if the incoming update omitted them.
     */
    fun mergeNotification(
        existing: NotificationInfo,
        incoming: NotificationInfo
    ): NotificationInfo {
        val mergedActions = if (incoming.actions.isNotEmpty()) {
            incoming.actions
        } else {
            existing.actions
        }

        val mergedImage = incoming.imagePath ?: existing.imagePath
        val mergedTitle = incoming.title.ifBlank { existing.title }
        val mergedText = incoming.text.ifBlank { existing.text }
        val mergedExpandedText = incoming.expandedText.ifBlank { existing.expandedText }
        val mergedSubText = incoming.subText ?: existing.subText
        val mergedInboxLines = if (incoming.inboxLines.isNotEmpty()) incoming.inboxLines else existing.inboxLines

        return incoming.copy(
            id = incoming.id,
            title = mergedTitle,
            text = mergedText,
            expandedText = mergedExpandedText,
            subText = mergedSubText,
            inboxLines = mergedInboxLines,
            imagePath = mergedImage,
            actions = mergedActions
        )
    }

    companion object {
        private const val MAX_NOTIFICATIONS = 20
    }
}
