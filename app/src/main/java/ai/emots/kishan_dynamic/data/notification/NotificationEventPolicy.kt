package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

enum class NotificationEventKind { IGNORED, TRANSIENT, ONGOING }
data class NotificationEventDecision(val kind: NotificationEventKind, val reason: String)

/** A source remaining in the shade is not a reason to occupy the island. */
object NotificationEventPolicy {
    fun evaluate(source: NotificationInfo, restored: Boolean = false,
                 recognizedActivity: Boolean = false): NotificationEventDecision {
        val callOrAlarm = source.category in setOf("call", "alarm", "emergency")
        val progressActive = source.isProgressIndeterminate ||
            (source.progressMax > 0 && source.progress < source.progressMax)
        val progressComplete = !source.isProgressIndeterminate && source.progressMax > 0 && source.progress >= source.progressMax
        val live = source.isOngoing && !progressComplete && (recognizedActivity || progressActive ||
            source.showChronometer || source.chronometerBaseElapsedRealtime > 0 ||
            source.category in setOf("navigation", "timer", "stopwatch", "call", "transport"))
        return when {
            source.importance == 0 -> NotificationEventDecision(NotificationEventKind.IGNORED, "blocked")
            live -> NotificationEventDecision(NotificationEventKind.ONGOING, "active_capability")
            source.importance < 3 && !callOrAlarm -> NotificationEventDecision(NotificationEventKind.IGNORED, "silent")
            source.isOngoing && !callOrAlarm && !(source.progressMax > 0 && source.progress >= source.progressMax) ->
                NotificationEventDecision(NotificationEventKind.IGNORED, "background_without_activity")
            restored && source.category != "call" -> NotificationEventDecision(NotificationEventKind.IGNORED, "old_transient_snapshot")
            else -> NotificationEventDecision(NotificationEventKind.TRANSIENT, "new_relevant_event")
        }
    }

    fun remainingMillis(deadline: Long, now: Long): Long = (deadline - now).coerceAtLeast(0)
    fun deadline(previous: Long?, now: Long, durationMillis: Long): Long =
        previous ?: (now + durationMillis.coerceIn(1000L, 60000L))
}
