package ai.emots.kishan_dynamic.ui.screens

import ai.emots.kishan_dynamic.ui.components.IslandDemoState

data class PlaygroundStateItem(
    val label: String,
    val headline: String,
    val detail: String,
    val supportsExpand: Boolean,
    val compactState: IslandDemoState,
    val expandedState: IslandDemoState
)

fun PlaygroundStateItem.experienceCategory(): String = when (label) {
    "Music", "Music (Paused)", "Video remote" -> "Media"
    "Incoming call", "Active call", "Dialing call", "Call summary", "FaceTime alt", "Shared call", "Call banner" -> "Calls"
    "Alerts", "Stacked alerts", "Alerts + Music" -> "Alerts"
    "Delivery", "Flight", "Sports", "Navigation" -> "Live"
    "Voice memo", "Screen rec", "Shortcuts" -> "Capture"
    "Quick Controls" -> "Controls"
    else -> "System"
}

val defaultPlaygroundCatalog: List<PlaygroundStateItem> = listOf(
            PlaygroundStateItem(
                label = "Music",
                headline = "Now playing",
                detail = "Grass Animals · Heat Waves",
                supportsExpand = true,
                compactState = IslandDemoState.MusicCompact,
                expandedState = IslandDemoState.MusicExpanded
            ),
            PlaygroundStateItem(
                label = "Music (Paused)",
                headline = "Media paused",
                detail = "Album art split bubble · Paused",
                supportsExpand = true,
                compactState = IslandDemoState.MusicCompact,
                expandedState = IslandDemoState.MusicExpanded
            ),
            PlaygroundStateItem(
                label = "Incoming call",
                headline = "Incoming call",
                detail = "Tamia Castillo · Mobile",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallAvatars
            ),
            PlaygroundStateItem(
                label = "Dialing call",
                headline = "Outgoing call",
                detail = "Calling… · Tamia Castillo",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallExpanded
            ),
            PlaygroundStateItem(
                label = "Active call",
                headline = "Call in progress",
                detail = "02:45 · HD voice & audio waveform bubble",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallExpanded
            ),
            PlaygroundStateItem(
                label = "Call summary",
                headline = "Call ended",
                detail = "04:12 · Redial & Message options",
                supportsExpand = false,
                compactState = IslandDemoState.CallSummaryExpanded,
                expandedState = IslandDemoState.CallSummaryExpanded
            ),
            PlaygroundStateItem(
                label = "FaceTime alt",
                headline = "FaceTime Audio variant",
                detail = "Alternate active-call control layout",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.FaceTimeCallExpanded
            ),
            PlaygroundStateItem(
                label = "Shared call",
                headline = "Shared media call",
                detail = "Asia Wild Nature · For me",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.SharedMediaCallExpanded
            ),
            PlaygroundStateItem(
                label = "Alerts",
                headline = "Notification Pager",
                detail = "Vertical swipeable cards · Maps, Uber, Chrome, Chat",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationCompact,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Stacked alerts",
                headline = "3 pending notifications",
                detail = "Queue indicator bubble · Multi-card swipe",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationStacked,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Alerts + Music",
                headline = "Coexistence split",
                detail = "Track in pill · Alert count badge in bubble",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationWithMusicCompact,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Quick Controls",
                headline = "Action Control Center",
                detail = "Wi-Fi, Bluetooth, Flashlight, Sliders & Shortcuts",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.ActionControlExpanded
            ),
            PlaygroundStateItem(
                label = "Charging",
                headline = "Charging",
                detail = "Battery percentage · Connected to power",
                supportsExpand = true,
                compactState = IslandDemoState.ChargingCompact,
                expandedState = IslandDemoState.ChargingExpanded
            ),
            PlaygroundStateItem(
                label = "Low battery",
                headline = "Low battery",
                detail = "14% remaining · Connect charger",
                supportsExpand = true,
                compactState = IslandDemoState.LowBatteryCompact,
                expandedState = IslandDemoState.LowBatteryExpanded
            ),
            PlaygroundStateItem(
                label = "Timer",
                headline = "Timer",
                detail = "03:35 remaining",
                supportsExpand = true,
                compactState = IslandDemoState.TimerCompact,
                expandedState = IslandDemoState.TimerExpanded
            ),
            PlaygroundStateItem(
                label = "Silent",
                headline = "Silent mode",
                detail = "Ringer switched off",
                supportsExpand = true,
                compactState = IslandDemoState.SilentModeCompact,
                expandedState = IslandDemoState.SilentModeExpanded
            ),
            PlaygroundStateItem(
                label = "Idle",
                headline = "Idle island",
                detail = "126pt × 36.67pt hardware cutout, exactly as the two device references draw it",
                supportsExpand = false,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.Idle
            ),
            PlaygroundStateItem(
                label = "Delivery",
                headline = "Live activity · Delivery",
                detail = "DoorDash · Order arriving in 12 mins",
                supportsExpand = true,
                compactState = IslandDemoState.DeliveryCompact,
                expandedState = IslandDemoState.DeliveryExpanded
            ),
            PlaygroundStateItem(
                label = "Flight",
                headline = "Flight tracker",
                detail = "United UA 824 · SFO to JFK",
                supportsExpand = true,
                compactState = IslandDemoState.FlightCompact,
                expandedState = IslandDemoState.FlightExpanded
            ),
            PlaygroundStateItem(
                label = "Sports",
                headline = "Live sports",
                detail = "Lakers 108 - 102 Warriors · Q4 2:14",
                supportsExpand = true,
                compactState = IslandDemoState.SportsCompact,
                expandedState = IslandDemoState.SportsExpanded
            ),
            PlaygroundStateItem(
                label = "Navigation",
                headline = "Maps navigation",
                detail = "Lane guidance · I-280 North · San Francisco",
                supportsExpand = true,
                compactState = IslandDemoState.NavigationCompact,
                expandedState = IslandDemoState.TransportActivity
            ),
            PlaygroundStateItem(
                label = "Mirroring",
                headline = "Screen mirroring",
                detail = "Connected to MacBook Pro",
                supportsExpand = false,
                compactState = IslandDemoState.ScreenMirroringAlert,
                expandedState = IslandDemoState.ScreenMirroringAlert
            ),
            PlaygroundStateItem(
                label = "Transit",
                headline = "Transit alert",
                detail = "Platform 3 · Train leaves in 4m",
                supportsExpand = false,
                compactState = IslandDemoState.TransitRouteAlert,
                expandedState = IslandDemoState.TransitRouteAlert
            ),
            PlaygroundStateItem(
                label = "Voice memo",
                headline = "Recording memo",
                detail = "0:12 · Live waveform trace",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.VoiceMemoExpanded
            ),
            PlaygroundStateItem(
                label = "Screen rec",
                headline = "Screen recording",
                detail = "0:03 · Recording screen session",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.ScreenRecordingExpanded
            ),
            PlaygroundStateItem(
                label = "Shortcuts",
                headline = "Running shortcut",
                detail = "Daily Summary · Automation in progress",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.ShortcutExpanded
            ),
            PlaygroundStateItem(
                label = "Call banner",
                headline = "Ongoing call",
                detail = "Tamia Castillo · 02:45",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.OngoingCallBanner
            ),
            PlaygroundStateItem(
                label = "Focus / DND",
                headline = "Do Not Disturb",
                detail = "Focus mode turned on",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.FocusModeExpanded
            ),
            PlaygroundStateItem(
                label = "Video remote",
                headline = "WWDC23 Keynote",
                detail = "Apple · 15s skip transport controls",
                supportsExpand = true,
                compactState = IslandDemoState.MusicCompact,
                expandedState = IslandDemoState.VideoRemoteExpanded
            ),
            PlaygroundStateItem(
                label = "Airplane mode",
                headline = "Airplane mode alert",
                detail = "Turn off airplane mode to access data",
                supportsExpand = false,
                compactState = IslandDemoState.AirplaneAlert,
                expandedState = IslandDemoState.AirplaneAlert
            ),
            PlaygroundStateItem(
                label = "Mobile data",
                headline = "Mobile data alert",
                detail = "Turn off mobile data to use Wi-Fi",
                supportsExpand = false,
                compactState = IslandDemoState.MobileDataAlert,
                expandedState = IslandDemoState.MobileDataAlert
            ),
            PlaygroundStateItem(
                label = "AirDrop",
                headline = "AirDrop activity",
                detail = "3 Photos · Receiving from Chris",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.AirDropActivity
            ),
            PlaygroundStateItem(
                label = "AirPods",
                headline = "AirPods connected",
                detail = "Ladislav's AirPods · 75% battery",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.AirPodsConnected
            ),
            PlaygroundStateItem(
                label = "Satellite",
                headline = "Satellite connection",
                detail = "Connected · Keep pointing at Satellite",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.SatelliteConnected
            ),
            PlaygroundStateItem(
                label = "Find My",
                headline = "Find My iPhone alert",
                detail = "Target radar pulse locator",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.FindMyAlert
            ),
            PlaygroundStateItem(
                label = "Moved to iPhone",
                headline = "AirPods hand-off",
                detail = "Audio moved to iPhone · Undo",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.MovedToIPhone
            )
        )
