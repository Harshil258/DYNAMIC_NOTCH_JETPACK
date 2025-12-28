package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import com.notch.dynamicislanddemo.models.IslandState

/**
 * Main Dynamic Island composable with iOS-like morphing animations
 */
@Composable
fun DynamicIsland(
    state: IslandState,
    modifier: Modifier = Modifier,
    onMusicAction: (MusicAction) -> Unit = {},
    onCallAction: (CallAction) -> Unit = {},
    onSilentModeToggle: () -> Unit = {},
    onNotificationAction: (com.notch.dynamicislanddemo.ui.components.NotificationAction) -> Unit = {},
    onNotificationDismiss: () -> Unit = {},
    onActionControlToggle: (com.notch.dynamicislanddemo.models.SystemToggleType) -> Unit = {},
    onActionControlAppLaunch: (String) -> Unit = {},
    onActionControlContact: (String) -> Unit = {},
    onActionControlBrightness: (Float) -> Unit = {},
    onActionControlVolume: (Float) -> Unit = {},
    onLockAction: () -> Unit = {},
    onCameraAction: () -> Unit = {},
    onSettingsAction: () -> Unit = {},
    onIslandTap: () -> Unit = {},
    onSideIslandTap: () -> Unit = {},
    onNotificationsAction: () -> Unit = {}
) {
    // State for dynamic notification height
    var notificationHeight by remember { mutableStateOf(180.dp) }

    // logic for split mode (Multitask and compact states)
    val isSplit = state is IslandState.Multitask || 
                  state is IslandState.CompactMusic || 
                  state is IslandState.OngoingCall || 
                  state is IslandState.Charging ||
                  state is IslandState.NotificationWithMusic
    
    // Calculate dimensions
    // Main Island Width
    val mainIslandTargetWidth = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Multitask -> 120.dp
        is IslandState.CompactMusic -> 200.dp
        is IslandState.OngoingCall -> 200.dp
        is IslandState.Charging -> 230.dp
        is IslandState.NotificationWithMusic -> 200.dp
        is IslandState.Minimal -> 150.dp
        is IslandState.MusicPlayer -> 390.dp
        is IslandState.IncomingCall -> 390.dp
        is IslandState.Notification -> 390.dp
        is IslandState.ActionControl -> 390.dp
        is IslandState.SilentMode -> 370.dp
    }
    
    // Side Island Size (0 if not split)
    val sideIslandTargetSize = if (isSplit) 37.dp else 0.dp
    
    // Gap size
    val gapTargetSize = if (isSplit) 10.dp else 0.dp

    val targetHeight = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Minimal -> 37.dp
        is IslandState.Multitask -> 37.dp
        is IslandState.CompactMusic -> 37.dp
        is IslandState.OngoingCall -> 37.dp
        is IslandState.Charging -> 37.dp
        is IslandState.NotificationWithMusic -> 37.dp
        is IslandState.MusicPlayer -> 210.dp
        is IslandState.IncomingCall -> 120.dp
        is IslandState.Notification -> notificationHeight
        is IslandState.ActionControl -> 270.dp
        is IslandState.SilentMode -> 90.dp
    }
    
    // Animations
    val animatedMainWidth by animateDpAsState(
        targetValue = mainIslandTargetWidth,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "main_width"
    )
    
    val animatedSideSize by animateDpAsState(
        targetValue = sideIslandTargetSize,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "side_size"
    )
    
    val animatedGap by animateDpAsState(
        targetValue = gapTargetSize,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "gap"
    )
    
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "height"
    )
    
    // Container
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Main Island
        Box(
            modifier = Modifier
                .width(animatedMainWidth)
                .height(animatedHeight)
                .width(animatedMainWidth)
                .height(animatedHeight)
                .clip(ContinuousRoundedRectangle(44.dp))
                .background(Color.Black)
                .clickable { onIslandTap() },
            contentAlignment = Alignment.Center
        ) {
            // Route content based on state
            when (state) {
                is IslandState.Hidden -> Spacer(Modifier.size(0.dp))
                is IslandState.Minimal -> MinimalIsland()
                is IslandState.Multitask -> {
                    MultitaskIslandMain(state.leftActivity)
                }
                is IslandState.CompactMusic -> CompactMusicIslandMain(state)
                is IslandState.OngoingCall -> OngoingCallIslandMain(state)
                is IslandState.Charging -> ChargingIslandMain(state)
                is IslandState.NotificationWithMusic -> NotificationWithMusicIslandMain(state)
                is IslandState.MusicPlayer -> MusicPlayerIsland(
                    state = state,
                    onAction = onMusicAction
                )
                is IslandState.IncomingCall -> IncomingCallIsland(
                    state = state,
                    onAction = onCallAction
                )
                is IslandState.Notification -> NotificationIsland(
                    notifications = state.notifications,
                    onAction = onNotificationAction,
                    onDismiss = onNotificationDismiss,
                    onHeightChange = { newHeight ->
                        notificationHeight = newHeight
                    }
                )
                is IslandState.ActionControl -> ActionControlIsland(
                    state = state,
                    onToggle = onActionControlToggle,
                    onAppLaunch = onActionControlAppLaunch,
                    onContactAction = onActionControlContact,
                    onBrightnessChange = onActionControlBrightness,
                    onVolumeChange = onActionControlVolume,
                    onLockAction = onLockAction,
                    onCameraAction = onCameraAction,
                    onSettingsAction = onSettingsAction,
                    notificationCount = state.notificationCount,
                    onNotificationsAction = onNotificationsAction
                )
                is IslandState.SilentMode -> SilentModeIsland(
                    state = state,
                    onToggle = onSilentModeToggle
                )
            }
        }
        
        // Gap
        if (animatedGap > 0.dp) {
            Spacer(modifier = Modifier.width(animatedGap))
        }
        
        // Side Island (only for split)
        if (animatedSideSize > 0.dp) {
            Box(
                modifier = Modifier
                    .size(animatedSideSize)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black)
                    .clickable { onSideIslandTap() },
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is IslandState.Multitask -> MultitaskIslandSide(state.rightActivity)
//                    is IslandState.CompactMusic -> CompactMusicIslandSide(state)
                    is IslandState.OngoingCall -> OngoingCallIslandSide(state)
                    is IslandState.Charging -> ChargingIslandSide(state)
                    is IslandState.NotificationWithMusic -> NotificationWithMusicIslandSide(state)
                    else -> {}
                }
            }
        }
    }
}

