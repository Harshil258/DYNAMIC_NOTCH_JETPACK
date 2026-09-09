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
                if (incoming.isGroupSummary && state.notifications.any { existing ->
                        !existing.isGroupSummary && existing.groupKey == incoming.groupKey
                    }
                ) {
                    return state
                }
                val sourceNotifications = if (!incoming.isGroupSummary) {
                    state.notifications.filterNot { existing ->
                        existing.isGroupSummary && existing.groupKey == incoming.groupKey
                    }
                } else {
                    state.notifications
                }
                val existingIndex = findMatchingIndex(sourceNotifications, incoming)

                val updatedList = if (existingIndex != -1) {
                    val existing = sourceNotifications[existingIndex]
                    val merged = mergeNotification(existing, incoming)

                    // Content change check
                    val contentChanged = existing.title != merged.title ||
                        existing.text != merged.text ||
                        existing.expandedText != merged.expandedText

                    val isOngoingProgressOnly = incoming.isOngoing && !contentChanged

                    if (isOngoingProgressOnly || event.isSilentUpdate) {
                        // Update in place silently without bumping priority or moving to top
                        sourceNotifications.toMutableList().apply {
                            set(existingIndex, merged)
                        }
                    } else {
                        // Remove from old position, prepend to top
                        val mutable = sourceNotifications.toMutableList()
                        mutable.removeAt(existingIndex)
                        mutable.add(0, merged)
                        sortQueue(mutable)
                    }
                } else {
                    // New notification: add to queue and sort
                    val combined = listOf(incoming) + sourceNotifications
                    sortQueue(combined)
                }

                val requestedActiveId = if (event.isSilentUpdate && state.activeNotificationId != null) {
                    // If silent update (e.g. progress tick), preserve currently active notification
                    state.activeNotificationId
                } else if (existingIndex != -1 && incoming.isOngoing && existingIndex != 0) {
                    // Ongoing progress on non-active item keeps current active item
                    state.activeNotificationId ?: incoming.id
                } else {
                    incoming.id
                }

                // Keep the queue bounded without evicting the page being read or
                // authoritative ongoing work such as navigation and transfers.
                val cappedList = capQueue(updatedList, requestedActiveId)
                val newActiveId = requestedActiveId.takeIf { activeId ->
                    cappedList.any { it.id == activeId }
                } ?: cappedList.firstOrNull()?.id

                NotificationQueueState(cappedList, newActiveId)
            }

            is NotificationEvent.Removed -> {
                removeNotification(state, event.notificationId)
            }

            is NotificationEvent.Expired -> {
                removeNotification(state, event.notificationId)
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

    private fun capQueue(
        notifications: List<NotificationInfo>,
        protectedActiveId: String?
    ): List<NotificationInfo> {
        if (notifications.size <= MAX_NOTIFICATIONS) return notifications

        val protectedIds = notifications
            .filter { it.isOngoing || it.id == protectedActiveId }
            .mapTo(linkedSetOf()) { it.id }
        val remainingCapacity = (MAX_NOTIFICATIONS - protectedIds.size).coerceAtLeast(0)
        notifications.asSequence()
            .filterNot { it.id in protectedIds }
            .take(remainingCapacity)
            .forEach { protectedIds += it.id }
        return notifications.filter { it.id in protectedIds }
    }

    private fun removeNotification(
        state: NotificationQueueState,
        notificationId: String
    ): NotificationQueueState {
        val removedIndex = state.notifications.indexOfFirst { it.id == notificationId }
        if (removedIndex == -1) return state

        val remaining = state.notifications.filterNot { it.id == notificationId }
        val currentSelectionSurvives = state.activeNotificationId != notificationId &&
            remaining.any { it.id == state.activeNotificationId }
        val nextActiveId = if (currentSelectionSurvives) {
            state.activeNotificationId
        } else {
            remaining.getOrNull(removedIndex.coerceAtMost(remaining.lastIndex))?.id
                ?: remaining.lastOrNull()?.id
        }
        return NotificationQueueState(
            notifications = remaining,
            activeNotificationId = nextActiveId
        )
    }

    /**
     * Android's StatusBarNotification key is the only safe update identity.
     * Sender/title heuristics can merge two genuinely separate messages and
     * leave their remove/action callbacks pointing at the wrong source.
     */
    fun findMatchingIndex(
        notifications: List<NotificationInfo>,
        incoming: NotificationInfo
    ): Int {
        return notifications.indexOfFirst { it.id == incoming.id }
    }

    /**
     * Merges display fields for an update to the same Android notification key.
     * Actions are authoritative on every update so removed PendingIntents never
     * remain visible as dead controls.
     */
    fun mergeNotification(
        existing: NotificationInfo,
        incoming: NotificationInfo
    ): NotificationInfo {
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
            actions = incoming.actions
        )
    }

    companion object {
        private const val MAX_NOTIFICATIONS = 20
    }
}
