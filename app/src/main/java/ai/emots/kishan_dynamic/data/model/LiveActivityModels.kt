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
        LiveActivityKind.AIRPLANE_ALERT,
        LiveActivityKind.MOBILE_DATA_ALERT -> 90
        LiveActivityKind.VOICE_MEMO,
        LiveActivityKind.SCREEN_RECORDING,
        LiveActivityKind.AIR_DROP,
        LiveActivityKind.AIRPODS -> 80
        LiveActivityKind.TIMER,
        LiveActivityKind.DELIVERY,
        LiveActivityKind.FLIGHT,
        LiveActivityKind.SPORTS,
        LiveActivityKind.NAVIGATION,
        LiveActivityKind.FOCUS_MODE -> 70
        LiveActivityKind.SHORTCUT,
        LiveActivityKind.MOVED_TO_IPHONE,
        LiveActivityKind.VIDEO_REMOTE,
        LiveActivityKind.SATELLITE,
        LiveActivityKind.FIND_MY,
        LiveActivityKind.SCREEN_MIRRORING_ALERT,
        LiveActivityKind.TRANSIT_ROUTE_ALERT -> 60
    }

@Immutable
data class LiveActivityInfo(
    val id: String,
    val kind: LiveActivityKind,
    val title: String,
    val subtitle: String = "",
    val progress: Float? = null,
    val isExpanded: Boolean = false,
    val isRunning: Boolean = true,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long? = null,
    /** Optional notification source retained outside the Compose layer. */
    val sourceNotificationId: String? = null,
    /** Optional source action, such as Open or Undo, for this activity. */
    val sourceActionId: String? = null
)
