package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

/** Reference-backed activity families supported by the island coordinator. */
enum class LiveActivityKind {
    TIMER,
    DELIVERY,
    FLIGHT,
    SPORTS,
    NAVIGATION,
    VOICE_MEMO,
    SCREEN_RECORDING,
    SHORTCUT,
    FOCUS_MODE,
    AIR_DROP,
    AIRPODS,
    SATELLITE,
    FIND_MY,
    MOVED_TO_IPHONE,
    VIDEO_REMOTE,
    AIRPLANE_ALERT,
    SCREEN_MIRRORING_ALERT,
    MOBILE_DATA_ALERT,
    TRANSIT_ROUTE_ALERT
}

/** Higher values win when several live sources are active at once. */
internal val LiveActivityKind.displayPriority: Int
    get() = when (this) {
        LiveActivityKind.NAVIGATION,
        LiveActivityKind.SATELLITE -> 100
        LiveActivityKind.VOICE_MEMO,
        LiveActivityKind.SCREEN_RECORDING,
        LiveActivityKind.SCREEN_MIRRORING_ALERT -> 95
        LiveActivityKind.TIMER,
        LiveActivityKind.FIND_MY -> 90
        LiveActivityKind.AIR_DROP -> 85
        LiveActivityKind.TRANSIT_ROUTE_ALERT -> 80
        LiveActivityKind.DELIVERY -> 75
        LiveActivityKind.FLIGHT -> 70
        LiveActivityKind.SPORTS -> 65
        LiveActivityKind.VIDEO_REMOTE -> 60
        LiveActivityKind.AIRPLANE_ALERT,
        LiveActivityKind.MOBILE_DATA_ALERT -> 40
        LiveActivityKind.FOCUS_MODE -> 30
        LiveActivityKind.AIRPODS,
        LiveActivityKind.MOVED_TO_IPHONE -> 20
        LiveActivityKind.SHORTCUT -> 10
    }

/** True when the activity is a temporary status announcement rather than an ongoing task. */
val LiveActivityKind.isTransientAlert: Boolean
    get() = when (this) {
        LiveActivityKind.MOBILE_DATA_ALERT,
        LiveActivityKind.AIRPLANE_ALERT,
        LiveActivityKind.AIRPODS,
        LiveActivityKind.FIND_MY,
        LiveActivityKind.MOVED_TO_IPHONE,
        LiveActivityKind.SHORTCUT -> true
        else -> false
    }

/** Calibrated display durations for transient alert categories. */
val LiveActivityKind.defaultTransientDurationMillis: Long
    get() = when (this) {
        LiveActivityKind.SHORTCUT -> 2500L
        LiveActivityKind.AIRPODS,
        LiveActivityKind.MOVED_TO_IPHONE -> 3000L
        LiveActivityKind.AIRPLANE_ALERT,
        LiveActivityKind.MOBILE_DATA_ALERT,
        LiveActivityKind.FIND_MY -> 4000L
        else -> 3500L
    }

@Immutable
data class LiveActivityInfo(
    val id: String,
    val kind: LiveActivityKind,
    val title: String,
    val subtitle: String = "",
    val progress: Float? = null,
    val showChronometer: Boolean = false,
    val chronometerBaseElapsedRealtime: Long = 0L,
    val chronometerCountDown: Boolean = false,
    val isExpanded: Boolean = false,
    val isRunning: Boolean = true,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long? = null,
    val expandedByUser: Boolean = isExpanded,
    /** Optional notification source retained outside the Compose layer. */
    val sourceNotificationId: String? = null,
    /** Optional source action, such as Open or Undo, for this activity. */
    val sourceActionId: String? = null,
    /** All real actions exposed by the source notification. */
    val sourceActions: List<NotificationActionInfo> = emptyList(),
    /** Whether the notification exposes a separate content destination. */
    val sourceHasContentIntent: Boolean = false
)
