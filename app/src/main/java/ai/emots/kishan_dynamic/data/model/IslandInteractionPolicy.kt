package ai.emots.kishan_dynamic.data.model

/** Semantic destinations for gestures; the service performs the Android side effects. */
enum class IslandInteractionAction {
    NONE,
    OPEN_QUICK_CONTROLS,
    OPEN_NOTIFICATIONS,
    OPEN_NEXT_NOTIFICATION,
    OPEN_MUSIC,
    TOGGLE_EXPANSION
}

object IslandInteractionPolicy {

    fun mainTap(state: IslandState): IslandInteractionAction = when (state) {
        is IslandState.Minimal -> IslandInteractionAction.OPEN_QUICK_CONTROLS
        is IslandState.Notification -> if (state.isExpanded) {
            IslandInteractionAction.NONE
        } else {
            IslandInteractionAction.OPEN_NOTIFICATIONS
        }
        is IslandState.NotificationWithMusic -> if (state.isPlaying) {
            IslandInteractionAction.OPEN_MUSIC
        } else {
            IslandInteractionAction.OPEN_NOTIFICATIONS
        }
        is IslandState.Music -> if (state.isExpanded) {
            IslandInteractionAction.NONE
        } else {
            IslandInteractionAction.OPEN_MUSIC
        }
        is IslandState.OngoingCall -> if (state.isExpanded) {
            IslandInteractionAction.NONE
        } else {
            IslandInteractionAction.TOGGLE_EXPANSION
        }
        is IslandState.LiveActivity -> if (state.activity.isExpanded) {
            IslandInteractionAction.NONE
        } else {
            IslandInteractionAction.TOGGLE_EXPANSION
        }
        is IslandState.RingerMode -> if (state.isExpanded) {
            IslandInteractionAction.NONE
        } else {
            IslandInteractionAction.TOGGLE_EXPANSION
        }
        is IslandState.Hidden,
        is IslandState.IncomingCall,
        is IslandState.CallSummary,
        is IslandState.ActionControl,
        is IslandState.Charging,
        is IslandState.RingerVolume,
        is IslandState.MediaVolume,
        is IslandState.BluetoothDevice,
        is IslandState.PremiumExpiry -> IslandInteractionAction.NONE
    }

    fun companionTap(state: IslandState): IslandInteractionAction = when (state) {
        is IslandState.Notification -> if (!state.isExpanded && state.notifications.size > 1) {
            IslandInteractionAction.OPEN_NEXT_NOTIFICATION
        } else {
            mainTap(state)
        }
        is IslandState.NotificationWithMusic -> if (state.isPlaying) {
            IslandInteractionAction.OPEN_NOTIFICATIONS
        } else {
            IslandInteractionAction.OPEN_MUSIC
        }
        is IslandState.Music,
        is IslandState.OngoingCall,
        is IslandState.LiveActivity -> mainTap(state)
        is IslandState.Minimal -> IslandInteractionAction.OPEN_QUICK_CONTROLS
        else -> IslandInteractionAction.NONE
    }

    fun mainLongPress(state: IslandState): IslandInteractionAction = mainTap(state)

    fun companionLongPress(state: IslandState): IslandInteractionAction = companionTap(state)
}
