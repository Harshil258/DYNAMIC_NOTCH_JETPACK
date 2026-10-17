package ai.emots.kishan_dynamic.data.notification

/** Dispatch is not delivery acknowledgement from the source app. */
enum class NotificationDispatchOutcome { UNAVAILABLE, FAILED, HANDED_TO_APP }

object NotificationActionDispatch {
    fun attempt(available: Boolean, dispatch: () -> Unit): NotificationDispatchOutcome {
        if (!available) return NotificationDispatchOutcome.UNAVAILABLE
        return try {
            dispatch()
            NotificationDispatchOutcome.HANDED_TO_APP
        } catch (_: Exception) {
            NotificationDispatchOutcome.FAILED
        }
    }
}
