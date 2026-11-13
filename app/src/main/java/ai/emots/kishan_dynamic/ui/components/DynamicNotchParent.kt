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
    onEndCall: () -> Unit = {},
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    val demoState = mapIslandStateToDemoState(state)
    val liveActivity = (state as? IslandState.LiveActivity)?.activity

    DynamicIslandPill(
        state = demoState,
        modifier = modifier,
        onTap = onIslandTap,
        callContact = when (state) {
            is IslandState.IncomingCall -> state.contact
            is IslandState.OngoingCall -> state.contact
            else -> null
        },
        callCanDecline = (state as? IslandState.IncomingCall) != null,
        callSummary = (state as? IslandState.CallSummary)?.record,
        callIsDialing = (state as? IslandState.OngoingCall)?.isDialing == true,
        liveActivityTitle = liveActivity?.title.orEmpty(),
        liveActivitySubtitle = liveActivity?.subtitle.orEmpty(),
        liveActivityProgress = liveActivity?.progress,
        liveActivityShowChronometer = liveActivity?.showChronometer == true,
        liveActivityChronometerBase = liveActivity?.chronometerBaseElapsedRealtime ?: 0L,
        liveActivityChronometerCountDown = liveActivity?.chronometerCountDown == true,
        liveActivitySourceBacked = liveActivity?.sourceNotificationId != null,
        liveActivityInfo = liveActivity,
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
            onControlAction(action)
        }
    )
}

/**
 * Maps legacy or service [IslandState] to authentic Apple [IslandDemoState].
 * Delegates to centralized [resolveOverlayDemoState] to ensure single source of truth.
 */
fun mapIslandStateToDemoState(state: IslandState): IslandDemoState = resolveOverlayDemoState(state, unseenCount = 0)
