package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType

/**
 * Dynamic Notch Parent composable with authentic iOS top-anchored morphing physics.
 * Direct architectural alignment with the reference app.
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val islandTokens = ai.emots.kishan_dynamic.ui.theme.AppTheme.island
    val expandedWidth = islandTokens.expandedWidth(screenWidth)

    val compactHeight = islandTokens.compactHeight
    val compactWidth = islandTokens.compactWidth
    val mediaCompactWidth = islandTokens.mediaCompactWidth
    val sideBubbleSize = islandTokens.sideSize
    val splitGap = islandTokens.splitGap

    // Determine if split mode (main + side bubble) is active
    val isSplit = when (state) {
        is IslandState.Charging -> true
        is IslandState.Music -> !state.isExpanded
        is IslandState.OngoingCall -> !state.isExpanded
        is IslandState.NotificationWithMusic -> true
        else -> false
    }

    // Target width for the main island
    val targetMainWidth = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Minimal -> compactWidth
        is IslandState.Music -> if (state.isExpanded) expandedWidth else mediaCompactWidth
        is IslandState.IncomingCall -> expandedWidth
        is IslandState.OngoingCall -> if (state.isExpanded) expandedWidth else mediaCompactWidth
        is IslandState.Notification -> if (state.isExpanded) expandedWidth else compactWidth
        is IslandState.Charging -> (expandedWidth - splitGap - sideBubbleSize).coerceAtLeast(200.dp)
        is IslandState.NotificationWithMusic -> (expandedWidth - splitGap - sideBubbleSize).coerceAtLeast(200.dp)
        is IslandState.RingerMode -> expandedWidth
        else -> expandedWidth
    }

    // Target height for the main island
    val targetHeight = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Minimal -> compactHeight
        is IslandState.Music -> if (state.isExpanded) islandTokens.musicExpandedHeight else compactHeight
        is IslandState.IncomingCall -> islandTokens.incomingCallHeight
        is IslandState.OngoingCall -> if (state.isExpanded) islandTokens.callExpandedHeight else compactHeight
        is IslandState.Notification -> if (state.isExpanded) islandTokens.notificationExpandedHeight else compactHeight
        is IslandState.Charging -> compactHeight
        is IslandState.NotificationWithMusic -> compactHeight
        is IslandState.RingerMode -> islandTokens.ringerExpandedHeight
        else -> compactHeight
    }

    // Target side bubble size
    val targetSideSize = if (isSplit) sideBubbleSize else 0.dp
    val targetGap = if (isSplit) splitGap else 0.dp

    val springSpec = IslandAnimations.ContainerSpringDp

    val animatedMainWidth by animateDpAsState(
        targetValue = targetMainWidth,
        animationSpec = springSpec,
        label = "main_island_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = springSpec,
        label = "main_island_height"
    )

    val animatedSideSize by animateDpAsState(
        targetValue = targetSideSize,
        animationSpec = springSpec,
        label = "side_bubble_size"
    )

    val animatedGap by animateDpAsState(
        targetValue = targetGap,
        animationSpec = springSpec,
        label = "split_gap"
    )

    // Corner shape: Capsules for status/call notifications, Squircles (44dp) for rich media/cards
    val cornerShape = when {
        state is IslandState.IncomingCall -> RoundedCornerShape(percent = 50)
        state is IslandState.RingerMode -> RoundedCornerShape(percent = 50)
        state is IslandState.Music && state.isExpanded -> RoundedCornerShape(44.dp)
        state is IslandState.OngoingCall && state.isExpanded -> RoundedCornerShape(44.dp)
        state is IslandState.Notification && state.isExpanded -> RoundedCornerShape(44.dp)
        else -> RoundedCornerShape(percent = 50)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            // Main Island Container
            Box(
                modifier = Modifier
                    .width(animatedMainWidth)
                    .height(animatedHeight)
                    .clip(cornerShape)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onIslandTap
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        IslandAnimations.mainContentEnterTransition togetherWith IslandAnimations.mainContentExitTransition
                    },
                    label = "island_content"
                ) { targetState ->
                    when (targetState) {
                        is IslandState.Hidden -> Spacer(Modifier.size(0.dp))
                        is IslandState.Minimal -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34C759))
                                    )
                                    Text(
                                        text = "Active",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        is IslandState.Music -> {
                            if (targetState.isExpanded) {
                                MusicPlayerIsland(
                                    title = targetState.track.title,
                                    artist = targetState.track.artist,
                                    isPlaying = targetState.isPlaying,
                                    onPlayPause = onPlayPause,
                                    onNext = onNext,
                                    onPrevious = onPrevious
                                )
                            } else {
                                CompactMusicIslandMain(
                                    title = targetState.track.title,
                                    artist = targetState.track.artist,
                                    isPlaying = targetState.isPlaying
                                )
                            }
                        }
                        is IslandState.IncomingCall -> {
                            IncomingCallIsland(
                                name = targetState.contact.name,
                                label = "Mobile",
                                onAccept = onAcceptCall,
                                onDecline = onDeclineCall
                            )
                        }
                        is IslandState.OngoingCall -> {
                            if (targetState.isExpanded) {
                                OngoingCallIslandExpanded(
                                    name = targetState.contact.name,
                                    label = "Phone",
                                    duration = "${targetState.durationSeconds / 60}:${(targetState.durationSeconds % 60).toString().padStart(2, '0')}",
                                    onEndCall = onEndCall
                                )
                            } else {
                                OngoingCallIslandMain(
                                    duration = "${targetState.durationSeconds / 60}:${(targetState.durationSeconds % 60).toString().padStart(2, '0')}"
                                )
                            }
                        }
                        is IslandState.Charging -> {
                            ChargingIslandMain(
                                percentage = targetState.batteryPercent,
                                isLowBattery = targetState.batteryPercent < 20
                            )
                        }
                        is IslandState.Notification -> {
                            if (targetState.isExpanded) {
                                val notif = targetState.notifications.firstOrNull()
                                NotificationIslandExpanded(
                                    appName = notif?.appName ?: "WhatsApp",
                                    title = notif?.title ?: "John Doe",
                                    message = notif?.text ?: "Hey! Are we still meeting at 5:00 PM?",
                                    onDismiss = onIslandTap
                                )
                            } else {
                                val notif = targetState.notifications.firstOrNull()
                                CompactNotificationIslandMain(
                                    appName = notif?.appName ?: "WhatsApp",
                                    sender = notif?.title ?: "John Doe"
                                )
                            }
                        }
                        is IslandState.RingerMode -> {
                            RingerModeIsland(
                                isSilent = targetState.mode == RingerModeType.SILENT
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Dynamic Island",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Split Mode Side Bubble (Gap + Bubble)
            if (animatedSideSize > 0.dp) {
                Spacer(modifier = Modifier.width(animatedGap))

                Box(
                    modifier = Modifier
                        .size(animatedSideSize)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onIslandTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        is IslandState.Charging -> {
                            ChargingIslandSide(percentage = state.batteryPercent)
                        }
                        is IslandState.Music -> {
                            CompactMusicIslandSide()
                        }
                        is IslandState.OngoingCall -> {
                            OngoingCallIslandSide()
                        }
                        else -> Spacer(Modifier.size(0.dp))
                    }
                }
            }
        }
    }
}
