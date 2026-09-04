package ai.emots.kishan_dynamic.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType

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
        onTap = onIslandTap
    )
}

/**
 * Maps legacy or service [IslandState] to authentic Apple [IslandDemoState].
 */
fun mapIslandStateToDemoState(state: IslandState): IslandDemoState {
    return when (state) {
        is IslandState.Hidden -> IslandDemoState.Idle
        is IslandState.Minimal -> IslandDemoState.Idle
        is IslandState.Music -> if (state.isExpanded) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
        is IslandState.IncomingCall -> IslandDemoState.CallAvatars
        is IslandState.OngoingCall -> if (state.isExpanded) IslandDemoState.CallExpanded else IslandDemoState.CallCompact
        is IslandState.Notification -> if (state.isExpanded) IslandDemoState.NotificationExpanded else IslandDemoState.NotificationCompact
        is IslandState.NotificationWithMusic -> IslandDemoState.MusicCompact
        is IslandState.Charging -> if (state.batteryPercent < 20) {
            IslandDemoState.LowBatteryCompact
        } else {
            IslandDemoState.ChargingCompact
        }
        is IslandState.RingerMode -> if (state.mode == RingerModeType.SILENT) {
            IslandDemoState.SilentModeCompact
        } else {
            IslandDemoState.Idle
        }
        else -> IslandDemoState.Idle
    }
}

