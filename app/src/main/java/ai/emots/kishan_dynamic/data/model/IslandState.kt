package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class MusicTrack(
    val title: String,
    val artist: String,
    val packageName: String = "com.spotify.music",
    val durationMs: Long = 240000L,
    val positionMs: Long = 75000L,
    val albumArtUri: String? = null
)

@Immutable
data class ContactInfo(
    val name: String,
    val phoneNumber: String,
    val avatarUri: String? = null
)

@Immutable
data class NotificationInfo(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val expandedText: String = text,
    val subText: String? = null,
    val inboxLines: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isPriority: Boolean = false,
    val category: String = "general",
    val groupKey: String = packageName,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val progress: Int = 0,
    val progressMax: Int = 0,
    val isProgressIndeterminate: Boolean = false,
    val showChronometer: Boolean = false,
    /** Base in SystemClock.elapsedRealtime() units for a live notification timer. */
    val chronometerBaseElapsedRealtime: Long = 0L,
    val chronometerCountDown: Boolean = false,
    val channelId: String = "",
    val imagePath: String? = null,
    val template: String = "",
    /** True when the source notification exposes a tap destination. */
    val hasContentIntent: Boolean = false,
    val actions: List<NotificationActionInfo> = emptyList()
)

@Immutable
data class NotificationActionInfo(
    val id: String,
    val label: String,
    val isReply: Boolean = false
)

enum class RingerModeType {
    NORMAL,
    VIBRATE,
    SILENT
}

/**
 * Universal Sealed Hierarchy representing every Dynamic Island State.
 */
sealed class IslandState {
    object Hidden : IslandState()
    object Minimal : IslandState()

    data class Music(
        val track: MusicTrack,
        val isExpanded: Boolean = false,
        val isPlaying: Boolean = true
    ) : IslandState()

    data class OngoingCall(
        val contact: ContactInfo,
        val durationSeconds: Long = 0L,
        val showDuration: Boolean = true,
        val isExpanded: Boolean = false,
        /** True while an outgoing call is still ringing and not connected. */
        val isDialing: Boolean = false,
        /** Optional source notification and hang-up action for VoIP calls. */
        val sourceNotificationId: String? = null,
        val endActionId: String? = null
    ) : IslandState()

    data class CallSummary(
        val record: CallRecord
    ) : IslandState()

    data class IncomingCall(
        val contact: ContactInfo,
        /** Optional source notification and action ids for VoIP CallStyle calls. */
        val sourceNotificationId: String? = null,
        val acceptActionId: String? = null,
        val declineActionId: String? = null
    ) : IslandState()

    data class Notification(
        val notifications: List<NotificationInfo>,
        val isExpanded: Boolean = false,
        val activeIndex: Int = 0
    ) : IslandState()

    data class NotificationWithMusic(
        val notifications: List<NotificationInfo>,
        val track: MusicTrack,
        val isPlaying: Boolean = true
    ) : IslandState()

    data class Charging(
        val batteryPercent: Int,
        val isFastCharging: Boolean = true
    ) : IslandState()

    data class RingerMode(
        val mode: RingerModeType,
        val isExpanded: Boolean = false
    ) : IslandState()

    data class RingerVolume(
        val volumeLevel: Float
    ) : IslandState()

    data class MediaVolume(
        val volumeLevel: Float
    ) : IslandState()

    data class BluetoothDevice(
        val deviceName: String,
        val isConnected: Boolean = true,
        val batteryPercent: Int? = null,
        val isConnecting: Boolean = false
    ) : IslandState()

    data class ActionControl(
        val isExpanded: Boolean = true,
        val notificationCount: Int = 0
    ) : IslandState()

    data class PremiumExpiry(
        val hoursRemaining: Int = 12
    ) : IslandState()

    data class LiveActivity(
        val activity: LiveActivityInfo
    ) : IslandState()
}
