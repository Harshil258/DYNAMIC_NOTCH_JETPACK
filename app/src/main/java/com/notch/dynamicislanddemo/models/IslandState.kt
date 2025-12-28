package com.notch.dynamicislanddemo.models

/**
 * Sealed class representing different states of the Dynamic Island
 */
sealed class IslandState {
    /**
     * Minimal/compact state - small black pill
     */
    data object Minimal : IslandState()
    
    /**
     * Hidden state - island is invisible
     */
    data object Hidden : IslandState()

    
    /**
     * Multitask state - split pill showing two activities
     */
    data class Multitask(
        val leftActivity: ActivityInfo,
        val rightActivity: ActivityInfo
    ) : IslandState()
    
    /**
     * Compact music player state (split layout)
     */
    data class CompactMusic(
        val track: MusicTrack,
        val isPlaying: Boolean
    ) : IslandState()
    
    /**
     * Ongoing phone call state (split layout)
     */
    data class OngoingCall(
        val duration: String,
        val contactName: String
    ) : IslandState()
    
    /**
     * Battery/Charging state (split layout)
     */
    data class Charging(
        val percentage: Int,
        val isLowBattery: Boolean = false
    ) : IslandState()
    
    /**
     * Music player expanded state
     */
    data class MusicPlayer(
        val track: MusicTrack,
        val isPlaying: Boolean
    ) : IslandState()
    
    /**
     * Incoming call expanded state
     */
    data class IncomingCall(
        val caller: ContactInfo,
        val actions: List<NotificationAction> = emptyList(),
        val originalActions: List<ActionParsable>? = null
    ) : IslandState()
    
    /**
     * Notification state - for app notifications
     */
    data class Notification(
        val notifications: List<NotificationInfo>
    ) : IslandState()
    
    /**
     * Notification with active music - split pill showing notification with music indicator
     */
    data class NotificationWithMusic(
        val notifications: List<NotificationInfo>,
        val track: MusicTrack,
        val isPlaying: Boolean
    ) : IslandState()
    
    /**
     * Action Control Center - iOS-style quick controls
     */
    data class ActionControl(
        val config: ActionControlConfig,
        val notificationCount: Int = 0
    ) : IslandState()
    
    /**
     * Silent Mode notification state
     */
    data class SilentMode(
        val isOn: Boolean
    ) : IslandState()
}

/**
 * Music track information
 */
data class MusicTrack(
    val title: String,
    val artist: String,
    val albumArtUrl: String?,
    val currentTime: String, // Format: "0:50"
    val totalDuration: String, // Format: "4:01"
    val progress: Float, // 0.0 to 1.0
    val rawDuration: Long = 0L
)

/**
 * Contact information for incoming calls
 */
data class ContactInfo(
    val name: String,
    val label: String, // "Mobile", "iPhone", "Home", etc.
    val photoUrl: String?
)

/**
 * Notification information
 */
data class NotificationInfo(
    val id: String,
    val groupKey: String = id, // For matching and updating notifications
    val category: String? = null, // e.g., "call", "transport", etc.
    val template: String? = null, // e.g., "MediaStyle", "InboxStyle", etc.
    val appName: String,
    val appIcon: Int? = null, // Resource ID for dummy data
    val senderIcon: Int? = null, // Resource ID for dummy data
    val title: String,
    val message: String,
    val bigText: String = "",
    val subText: String = "",
    val timestamp: String,
    val actions: List<NotificationAction> = emptyList(),
    val picture: Int? = null, // Resource ID for dummy picture
    val originalActions: List<ActionParsable>? = null, // Reference to original actions for execution
    val tapAction: android.app.PendingIntent? = null
)

/**
 * Notification action button
 */
data class NotificationAction(
    val label: String,
    val type: NotificationActionType
)

enum class NotificationActionType {
    ARCHIVE,
    MARK_AS_READ,
    REPLY,
    DELETE,
    OPEN
}

/**
 * Activity information for multitask island
 */
data class ActivityInfo(
    val type: ActivityType,
    val iconResId: String? = null
)

enum class ActivityType {
    MUSIC,
    TIMER,
    VOICE_RECORDING,
    NAVIGATION,
    SCREEN_RECORDING
}
