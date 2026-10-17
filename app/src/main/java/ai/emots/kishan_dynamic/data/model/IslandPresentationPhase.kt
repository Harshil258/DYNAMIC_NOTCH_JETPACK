package ai.emots.kishan_dynamic.data.model

import ai.emots.kishan_dynamic.data.notification.NotificationEventKind
import ai.emots.kishan_dynamic.data.notification.NotificationEventPolicy

enum class IslandPresentationPhase { IDLE, TRANSIENT, COMPACT_ONGOING, MINIMAL_MULTI_ACTIVITY, EXPANDED, DISMISSING }

/** Derived from the one coordinator state, never an independent mutable state machine. */
fun IslandState.presentationPhase(transitioningOut: Boolean = false): IslandPresentationPhase = when {
    transitioningOut -> IslandPresentationPhase.DISMISSING
    this is IslandState.Minimal || this is IslandState.Hidden -> IslandPresentationPhase.IDLE
    presentationMode == IslandPresentationMode.EXPANDED -> IslandPresentationPhase.EXPANDED
    this is IslandState.NotificationWithMusic -> IslandPresentationPhase.MINIMAL_MULTI_ACTIVITY
    this is IslandState.Notification -> if (notifications.getOrNull(activeIndex)?.let {
        NotificationEventPolicy.evaluate(it).kind == NotificationEventKind.ONGOING
    } == true) IslandPresentationPhase.COMPACT_ONGOING else IslandPresentationPhase.TRANSIENT
    isOngoing -> IslandPresentationPhase.COMPACT_ONGOING
    else -> IslandPresentationPhase.TRANSIENT
}
