package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.IslandState

/** Shared presentation mapping used by the real service and debug fixtures. */
fun resolveOverlayDemoState(state: IslandState, unseenCount: Int): IslandDemoState {
        return when (state) {
            is IslandState.Hidden, is IslandState.Minimal -> IslandDemoState.Idle
            is IslandState.Music -> if (state.isExpanded) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
            is IslandState.OngoingCall -> if (state.isExpanded) IslandDemoState.CallExpanded else IslandDemoState.CallCompact
            is IslandState.CallSummary -> IslandDemoState.CallSummaryExpanded
            is IslandState.IncomingCall -> IslandDemoState.CallAvatars
            is IslandState.Notification -> when {
                state.isExpanded -> IslandDemoState.NotificationExpanded
                unseenCount <= 0 -> IslandDemoState.Idle
                unseenCount > 1 -> IslandDemoState.NotificationStacked
                else -> IslandDemoState.NotificationCompact
            }
            is IslandState.NotificationWithMusic -> IslandDemoState.NotificationWithMusicCompact
            is IslandState.Charging -> if (state.batteryPercent < 20) IslandDemoState.LowBatteryCompact else IslandDemoState.ChargingCompact
            is IslandState.RingerMode -> if (state.isExpanded) {
                IslandDemoState.RingerModeExpanded
            } else when (state.mode) {
                ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT -> IslandDemoState.SilentModeCompact
                ai.emots.kishan_dynamic.data.model.RingerModeType.VIBRATE -> IslandDemoState.VibrateModeCompact
                ai.emots.kishan_dynamic.data.model.RingerModeType.NORMAL -> IslandDemoState.NormalModeCompact
            }
            is IslandState.RingerVolume -> IslandDemoState.RingerVolumeCompact
            is IslandState.MediaVolume -> IslandDemoState.MediaVolumeCompact
            is IslandState.BluetoothDevice -> if (state.isConnecting) {
                IslandDemoState.BluetoothConnecting
            } else {
                IslandDemoState.BluetoothConnected
            }
            is IslandState.ActionControl -> IslandDemoState.ActionControlExpanded
            is IslandState.PremiumExpiry -> IslandDemoState.PremiumExpiryExpanded
            is IslandState.LiveActivity -> state.activity.toDemoState()
        }
    }

private fun ai.emots.kishan_dynamic.data.model.LiveActivityInfo.toDemoState(): IslandDemoState {
        return when (kind) {
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER -> if (isExpanded) IslandDemoState.TimerExpanded else IslandDemoState.TimerCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.DELIVERY -> if (isExpanded) IslandDemoState.DeliveryExpanded else IslandDemoState.DeliveryCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FLIGHT -> if (isExpanded) IslandDemoState.FlightExpanded else IslandDemoState.FlightCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SPORTS -> if (isExpanded) IslandDemoState.SportsExpanded else IslandDemoState.SportsCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.NAVIGATION -> if (isExpanded) IslandDemoState.ColorOptions else IslandDemoState.NavigationCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VOICE_MEMO -> if (isExpanded) IslandDemoState.VoiceMemoExpanded else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_RECORDING -> if (isExpanded) IslandDemoState.ScreenRecordingExpanded else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SHORTCUT -> if (isExpanded) IslandDemoState.ShortcutExpanded else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FOCUS_MODE -> if (isExpanded) IslandDemoState.FocusModeExpanded else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIR_DROP -> if (isExpanded) IslandDemoState.AirDropActivity else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPODS -> if (isExpanded) IslandDemoState.AirPodsConnected else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SATELLITE -> if (isExpanded) IslandDemoState.SatelliteConnected else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FIND_MY -> if (isExpanded) IslandDemoState.FindMyAlert else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOVED_TO_IPHONE -> if (isExpanded) IslandDemoState.MovedToIPhone else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VIDEO_REMOTE -> if (isExpanded) IslandDemoState.VideoRemoteExpanded else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPLANE_ALERT -> if (isExpanded) IslandDemoState.AirplaneAlert else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_MIRRORING_ALERT -> if (isExpanded) IslandDemoState.ScreenMirroringAlert else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOBILE_DATA_ALERT -> if (isExpanded) IslandDemoState.MobileDataAlert else IslandDemoState.Idle
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TRANSIT_ROUTE_ALERT -> if (isExpanded) IslandDemoState.TransitRouteAlert else IslandDemoState.Idle
        }
    }

