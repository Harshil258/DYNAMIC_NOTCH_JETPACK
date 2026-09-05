package ai.emots.kishan_dynamic.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy

/**
 * =============================================================================
 * DYNAMIC NOTCH PARENT
 *
 * Direct bridge to [DynamicIslandPill], ensuring 100% pixel-perfect iOS 17
 * geometry, typography, and liquid morphing animations everywhere.
 * =============================================================================
 */
@Composable
fun DynamicNotchParent(
    state: IslandState,
    modifier: Modifier = Modifier,
    onIslandTap: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onAcceptCall: () -> Unit = {},
    onDeclineCall: () -> Unit = {},
    onEndCall: () -> Unit = {}
) {
    val demoState = mapIslandStateToDemoState(state)

    DynamicIslandPill(
        state = demoState,
        modifier = modifier,
        onTap = onIslandTap,
        callSummary = (state as? IslandState.CallSummary)?.record,
        callIsDialing = (state as? IslandState.OngoingCall)?.isDialing == true,
        ringerMode = (state as? IslandState.RingerMode)?.mode ?: RingerModeType.SILENT,
        ringerVolumeLevel = (state as? IslandState.RingerVolume)?.volumeLevel ?: 0.5f,
        mediaVolumeHudLevel = (state as? IslandState.MediaVolume)?.volumeLevel ?: 0.5f,
        bluetoothDeviceName = (state as? IslandState.BluetoothDevice)?.deviceName ?: "Bluetooth device",
        bluetoothBatteryPercent = (state as? IslandState.BluetoothDevice)?.batteryPercent,
        bluetoothIsConnecting = (state as? IslandState.BluetoothDevice)?.isConnecting == true,
        batteryPercent = (state as? IslandState.Charging)?.batteryPercent ?: 85,
        premiumHoursRemaining = (state as? IslandState.PremiumExpiry)?.hoursRemaining ?: 12,
        onControlAction = { action ->
            when (action) {
                IslandControlAction.CallAccept -> onAcceptCall()
                IslandControlAction.CallDecline -> onDeclineCall()
                IslandControlAction.CallEnd -> onEndCall()
                IslandControlAction.MusicPlayPause -> onPlayPause()
                IslandControlAction.MusicNext -> onNext()
                IslandControlAction.MusicPrevious -> onPrevious()
                else -> Unit
            }
        }
    )
}

/**
 * Maps legacy or service [IslandState] to authentic Apple [IslandDemoState].
 */
fun mapIslandStateToDemoState(state: IslandState): IslandDemoState {
    return when (state) {
        is IslandState.Hidden -> IslandDemoState.Idle
        // Minimal is the two-part presentation from Minimal.svg: a compact
        // main capsule plus the detached 36.67dp activity bubble.
        is IslandState.Minimal -> IslandDemoState.Minimal
        is IslandState.Music -> if (state.isExpanded) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
        is IslandState.IncomingCall -> IslandDemoState.CallAvatars
        is IslandState.OngoingCall -> if (state.isExpanded) IslandDemoState.CallExpanded else IslandDemoState.CallCompact
        is IslandState.CallSummary -> IslandDemoState.CallSummaryExpanded
        is IslandState.Notification -> when {
            state.isExpanded -> IslandDemoState.NotificationExpanded
            state.notifications.size > 1 || state.notifications.any(NotificationPriorityPolicy::isPriority) ->
                IslandDemoState.NotificationStacked
            else -> IslandDemoState.NotificationCompact
        }
        is IslandState.NotificationWithMusic -> IslandDemoState.NotificationWithMusicCompact
        is IslandState.Charging -> if (state.batteryPercent < 20) {
            IslandDemoState.LowBatteryCompact
        } else {
            IslandDemoState.ChargingCompact
        }
        is IslandState.RingerMode -> if (state.isExpanded) {
            IslandDemoState.RingerModeExpanded
        } else if (state.mode == RingerModeType.SILENT) {
            IslandDemoState.SilentModeCompact
        } else if (state.mode == RingerModeType.VIBRATE) {
            IslandDemoState.VibrateModeCompact
        } else {
            IslandDemoState.NormalModeCompact
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
        is IslandState.LiveActivity -> when (state.activity.kind) {
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER -> if (state.activity.isExpanded) IslandDemoState.TimerExpanded else IslandDemoState.TimerCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.DELIVERY -> if (state.activity.isExpanded) IslandDemoState.DeliveryExpanded else IslandDemoState.DeliveryCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FLIGHT -> if (state.activity.isExpanded) IslandDemoState.FlightExpanded else IslandDemoState.FlightCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SPORTS -> if (state.activity.isExpanded) IslandDemoState.SportsExpanded else IslandDemoState.SportsCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.NAVIGATION -> if (state.activity.isExpanded) IslandDemoState.ColorOptions else IslandDemoState.NavigationCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VOICE_MEMO -> IslandDemoState.VoiceMemoExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_RECORDING -> IslandDemoState.ScreenRecordingExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SHORTCUT -> IslandDemoState.ShortcutExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FOCUS_MODE -> IslandDemoState.FocusModeExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIR_DROP -> IslandDemoState.AirDropActivity
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPODS -> IslandDemoState.AirPodsConnected
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SATELLITE -> IslandDemoState.SatelliteConnected
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FIND_MY -> IslandDemoState.FindMyAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOVED_TO_IPHONE -> IslandDemoState.MovedToIPhone
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VIDEO_REMOTE -> IslandDemoState.VideoRemoteExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPLANE_ALERT -> IslandDemoState.AirplaneAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_MIRRORING_ALERT -> IslandDemoState.ScreenMirroringAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOBILE_DATA_ALERT -> IslandDemoState.MobileDataAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TRANSIT_ROUTE_ALERT -> IslandDemoState.TransitRouteAlert
        }
    }
}
