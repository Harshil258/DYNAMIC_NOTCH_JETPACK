package com.notch.dynamicislanddemo.models

/**
 * Dummy data for testing Dynamic Island states
 */
object DummyData {
    
    val sampleMusicTracks = listOf(
        MusicTrack(
            title = "Heat Waves",
            artist = "Glass Animals",
            albumArtUrl = null,
            currentTime = "0:50",
            totalDuration = "4:01",
            progress = 0.21f,
            rawDuration = 241000L
        ),
        MusicTrack(
            title = "Blinding Lights",
            artist = "The Weeknd",
            albumArtUrl = null,
            currentTime = "1:23",
            totalDuration = "3:20",
            progress = 0.42f,
            rawDuration = 200000L
        ),
        MusicTrack(
            title = "Levitating",
            artist = "Dua Lipa",
            albumArtUrl = null,
            currentTime = "2:15",
            totalDuration = "3:23",
            progress = 0.66f,
            rawDuration = 203000L
        )
    )
    
    val sampleContacts = listOf(
        ContactInfo(
            name = "Tamia Castillo",
            label = "Mobile",
            photoUrl = null
        ),
        ContactInfo(
            name = "John Appleseed",
            label = "iPhone",
            photoUrl = null
        ),
        ContactInfo(
            name = "Sarah Wilson",
            label = "Home",
            photoUrl = null
        )
    )
    
    val musicActivity = ActivityInfo(
        type = ActivityType.MUSIC
    )
    
    val timerActivity = ActivityInfo(
        type = ActivityType.TIMER
    )
    
    val voiceRecordingActivity = ActivityInfo(
        type = ActivityType.VOICE_RECORDING
    )
    
    val navigationActivity = ActivityInfo(
        type = ActivityType.NAVIGATION
    )
    
    val sampleNotifications = listOf(
        NotificationInfo(
            id = "1",
            appName = "Instagram",
            appIcon = null,
            timestamp = "2m ago",
            senderIcon = null,
            title = "harshilvekariya12345",
            subText = "10233",
            message = "Naa naa",
            actions = listOf(
                NotificationAction("Like", NotificationActionType.REPLY),
                NotificationAction("Reply", NotificationActionType.REPLY)
            )
        ),
        NotificationInfo(
            id = "2",
            appName = "JioHotstar",
            appIcon = null,
            timestamp = "Just now",
            senderIcon = null,
            title = "Based On Your Taste 🎬",
            subText = "Watch Next",
            message = "Watch The Marvels Today!",
            bigText = "Watch The Marvels Today!",
            picture = android.R.drawable.ic_menu_gallery, // Dummy picture
            actions = listOf(
                NotificationAction("WATCH NOW", NotificationActionType.OPEN)
            )
        ),
        NotificationInfo(
            id = "3",
            appName = "WhatsApp",
            appIcon = null,
            timestamp = "15m ago",
            senderIcon = null,
            title = "John Doe",
            message = "Hey! Are we still meeting at 5?",
            actions = listOf(
                NotificationAction("Reply", NotificationActionType.REPLY),
                NotificationAction("Mark as Read", NotificationActionType.MARK_AS_READ)
            )
        )
    )
    
    val sampleActionControl = ActionControlConfig(
        tiles = listOf(
            ControlTile.SystemToggle(
                type = SystemToggleType.MOBILE_DATA,
                isEnabled = true,
                label = "Mobile D..."
            ),
            ControlTile.SystemToggle(
                type = SystemToggleType.WIFI,
                isEnabled = true,
                label = "Wi-Fi"
            ),
            ControlTile.SystemToggle(
                type = SystemToggleType.BLUETOOTH,
                isEnabled = false,
                label = "Bluetooth"
            ),
            ControlTile.SystemToggle(
                type = SystemToggleType.DARK_MODE,
                isEnabled = true,
                label = "Dark Mo..."
            ),
            ControlTile.SystemToggle(
                type = SystemToggleType.TORCH,
                isEnabled = false,
                label = "Torch"
            ),
            ControlTile.AppShortcut(
                appName = "VLC",
                packageName = "org.videolan.vlc",
                iconColor = androidx.compose.ui.graphics.Color(0xFFFF9500)
            ),
            ControlTile.AppShortcut(
                appName = "Android",
                packageName = "com.android",
                iconColor = androidx.compose.ui.graphics.Color(0xFF34C759)
            )
        ),
        brightnessLevel = 0.6f,
        volumeLevel = 0.5f
    )
}
