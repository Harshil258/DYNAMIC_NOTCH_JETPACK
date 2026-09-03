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
    val timestamp: Long = System.currentTimeMillis(),
    val isPriority: Boolean = false,
    val category: String = "general"
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
        val isExpanded: Boolean = false
    ) : IslandState()

    data class IncomingCall(
        val contact: ContactInfo
    ) : IslandState()

    data class Notification(
        val notifications: List<NotificationInfo>,
        val isExpanded: Boolean = false,
        val activeIndex: Int = 0
    ) : IslandState()

    data class NotificationWithMusic(
        val notification: NotificationInfo,
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
        val batteryPercent: Int? = 85
    ) : IslandState()

    data class ActionControl(
        val isExpanded: Boolean = true
    ) : IslandState()

    data class PremiumExpiry(
        val hoursRemaining: Int = 12
    ) : IslandState()
}
