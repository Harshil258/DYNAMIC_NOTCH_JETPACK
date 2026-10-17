package ai.emots.kishan_dynamic.data.model

/** Identifies presentations explicitly opened by the user and therefore sticky. */
object IslandPresentationOwnershipPolicy {

    fun isUserOwned(state: IslandState): Boolean = when (state) {
        is IslandState.ActionControl -> true
        is IslandState.Notification -> state.isExpanded && state.expandedByUser
        is IslandState.Music -> state.isExpanded
        is IslandState.LiveActivity -> state.activity.isExpanded && state.activity.expandedByUser && !state.activity.kind.isTransientAlert
        is IslandState.RingerMode -> state.isExpanded
        else -> false
    }

    /** Low-priority system confirmations must wait behind calls and live ongoing work. */
    fun blocksTransient(state: IslandState): Boolean = isUserOwned(state) ||
        state is IslandState.IncomingCall ||
        state is IslandState.OngoingCall ||
        (state is IslandState.LiveActivity && !state.activity.kind.isTransientAlert)
}
