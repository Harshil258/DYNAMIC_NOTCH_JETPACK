package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.ui.motion.AppMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * =============================================================================
 * DYNAMIC NOTCH PARENT
 *
 * A 1:1 recreation of the iOS 17 Dynamic Island.
 *
 * Geometry is taken from the reference design (iPhone 15 Pro, 393pt wide):
 *   * the idle pill is 126 x 37.33pt — it is NEVER stretched to the screen
 *   * the trailing live-activity bubble is a 37.33pt circle, 8pt away
 *   * expanded sheets grow to at most 371pt with 44pt continuous corners
 *   * the whole island hangs 11pt below the top edge of the display
 *
 * Motion is top-anchored: the island grows *downwards* out of the pill, the
 * way the real hardware cutout does, and every dimension is driven by the
 * same spring so width, height and corner radius stay physically coupled.
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val tokens = AppTheme.island

    val expandedWidth = tokens.expandedWidth(screenWidth)
    val deviceScale = tokens.deviceScale(screenWidth)

    val compactHeight = tokens.compactHeight * deviceScale
    val compactWidth = tokens.compactWidth * deviceScale
    val mediaCompactWidth = tokens.mediaCompactWidth * deviceScale
    val sideBubbleSize = tokens.sideSize * deviceScale
    val splitGap = tokens.splitGap * deviceScale

    // -------------------------------------------------------------------------
    // Presentation model
    // -------------------------------------------------------------------------

    /** Split mode = compact capsule plus a detached trailing bubble. */
    val isSplit = when (state) {
        is IslandState.Charging -> true
        is IslandState.Music -> !state.isExpanded
        is IslandState.OngoingCall -> !state.isExpanded
        is IslandState.NotificationWithMusic -> true
        else -> false
    }

    val isExpandedSheet = when (state) {
        is IslandState.Music -> state.isExpanded
        is IslandState.OngoingCall -> state.isExpanded
        is IslandState.Notification -> state.isExpanded
        is IslandState.IncomingCall -> true
        else -> false
    }

    /**
     * In split mode the capsule shrinks so that capsule + gap + bubble equals
     * the idle pill footprint — exactly how iOS splits the cutout.
     */
    val splitCapsuleWidth = (compactWidth - splitGap - sideBubbleSize)
        .coerceAtLeast(compactHeight)

    val targetMainWidth = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Minimal -> compactHeight // a lone circle, like iOS "minimal"
        is IslandState.Music -> if (state.isExpanded) expandedWidth else mediaCompactWidth - splitGap - sideBubbleSize
        is IslandState.IncomingCall -> expandedWidth
        is IslandState.OngoingCall -> if (state.isExpanded) expandedWidth else splitCapsuleWidth
        is IslandState.Notification -> if (state.isExpanded) expandedWidth else compactWidth
        is IslandState.Charging -> splitCapsuleWidth
        is IslandState.NotificationWithMusic -> splitCapsuleWidth
        is IslandState.RingerMode -> compactWidth
        else -> compactWidth
    }

    val targetHeight = when (state) {
        is IslandState.Hidden -> 0.dp
        is IslandState.Music -> if (state.isExpanded) tokens.musicExpandedHeight else compactHeight
        is IslandState.IncomingCall -> tokens.incomingCallHeight
        is IslandState.OngoingCall -> if (state.isExpanded) tokens.callExpandedHeight else compactHeight
        is IslandState.Notification -> if (state.isExpanded) tokens.notificationExpandedHeight else compactHeight
        is IslandState.RingerMode -> compactHeight
        else -> compactHeight
    }

    val targetCorner = if (isExpandedSheet) {
        tokens.expandedCorner
    } else {
        tokens.compactCorner * deviceScale
    }

    val targetSideSize = if (isSplit) sideBubbleSize else 0.dp
    val targetGap = if (isSplit) splitGap else 0.dp

    // -------------------------------------------------------------------------
    // Physics — one spring family so every dimension stays coupled
    // -------------------------------------------------------------------------

    val animatedMainWidth by animateDpAsState(
        targetValue = targetMainWidth,
        animationSpec = AppMotion.islandSpring(),
        label = "island_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = AppMotion.islandSpring(),
        label = "island_height"
    )
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = AppMotion.islandSpring(),
        label = "island_corner"
    )
    val animatedSideSize by animateDpAsState(
        targetValue = targetSideSize,
        animationSpec = AppMotion.islandSpring(),
        label = "island_bubble"
    )
    val animatedGap by animateDpAsState(
        targetValue = targetGap,
        animationSpec = AppMotion.islandSpring(),
        label = "island_gap"
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    /** iOS squeezes the island very slightly when you touch it. */
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = AppMotion.pressSpring(),
        label = "island_press"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier.padding(top = tokens.topInset),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            // -----------------------------------------------------------------
            // Main capsule / sheet
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .width(animatedMainWidth)
                    .height(animatedHeight)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                        // Grow downwards out of the hardware cutout.
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .clip(RoundedCornerShape(animatedCorner))
                    .background(Color.Black)
                    .islandSpecular(animatedCorner)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onIslandTap
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        IslandAnimations.contentEnter() togetherWith IslandAnimations.contentExit()
                    },
                    label = "island_content"
                ) { targetState ->
                    IslandContent(
                        state = targetState,
                        onIslandTap = onIslandTap,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onAcceptCall = onAcceptCall,
                        onDeclineCall = onDeclineCall,
                        onEndCall = onEndCall
                    )
                }
            }

            // -----------------------------------------------------------------
            // Trailing live-activity bubble
            // -----------------------------------------------------------------
            if (animatedSideSize > 0.dp) {
                Spacer(modifier = Modifier.width(animatedGap))

                Box(
                    modifier = Modifier
                        .size(animatedSideSize)
                        .graphicsLayer {
                            scaleX = pressScale
                            scaleY = pressScale
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = onIslandTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        is IslandState.Charging -> ChargingIslandSide(percentage = state.batteryPercent)
                        is IslandState.Music -> CompactMusicIslandSide()
                        is IslandState.OngoingCall -> OngoingCallIslandSide()
                        else -> Spacer(Modifier.size(0.dp))
                    }
                }
            }
        }
    }
}

/**
 * A whisper-thin highlight along the top edge, mimicking the way light catches
 * the glass around the real cutout. Costs one gradient, adds a lot of realism.
 */
private fun Modifier.islandSpecular(corner: androidx.compose.ui.unit.Dp): Modifier =
    this.drawWithContent {
        drawContent()
        val sheen = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.055f),
                Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.42f
        )
        drawRect(brush = sheen)
        // Soft rim so the island separates from a pure-black wallpaper.
        val rim = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.05f)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = maxOf(size.width, size.height) * 0.62f
        )
        drawRect(brush = rim)
    }

/** All island payloads in one place so the parent stays readable. */
@Composable
private fun IslandContent(
    state: IslandState,
    onIslandTap: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
    onEndCall: () -> Unit
) {
    when (state) {
        is IslandState.Hidden -> Spacer(Modifier.size(0.dp))

        is IslandState.Minimal -> MinimalIslandDot()

        is IslandState.Music -> {
            if (state.isExpanded) {
                MusicPlayerIsland(
                    title = state.track.title,
                    artist = state.track.artist,
                    isPlaying = state.isPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious
                )
            } else {
                CompactMusicIslandMain(
                    title = state.track.title,
                    artist = state.track.artist,
                    isPlaying = state.isPlaying
                )
            }
        }

        is IslandState.IncomingCall -> IncomingCallIsland(
            name = state.contact.name,
            label = "Mobile",
            onAccept = onAcceptCall,
            onDecline = onDeclineCall
        )

        is IslandState.OngoingCall -> {
            val duration = formatDuration(state.durationSeconds)
            if (state.isExpanded) {
                OngoingCallIslandExpanded(
                    name = state.contact.name,
                    label = "Phone",
                    duration = duration,
                    onEndCall = onEndCall
                )
            } else {
                OngoingCallIslandMain(duration = duration)
            }
        }

        is IslandState.Charging -> ChargingIslandMain(
            percentage = state.batteryPercent,
            isLowBattery = state.batteryPercent < 20
        )

        is IslandState.Notification -> {
            val notification = state.notifications.firstOrNull()
            if (state.isExpanded) {
                NotificationIslandExpanded(
                    appName = notification?.appName ?: "WhatsApp",
                    title = notification?.title ?: "John Doe",
                    message = notification?.text ?: "Hey! Are we still meeting at 5:00 PM?",
                    onDismiss = onIslandTap
                )
            } else {
                CompactNotificationIslandMain(
                    appName = notification?.appName ?: "WhatsApp",
                    sender = notification?.title ?: "John Doe"
                )
            }
        }

        is IslandState.RingerMode -> RingerModeIsland(
            isSilent = state.mode == RingerModeType.SILENT
        )

        else -> Spacer(Modifier.size(0.dp))
    }
}

/** The "minimal" presentation: a single breathing status dot. */
@Composable
private fun MinimalIslandDot() {
    val pulse by ai.emots.kishan_dynamic.ui.motion.rememberBreathing(
        min = 0.72f,
        max = 1f,
        durationMillis = 2200
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .graphicsLayer {
                    alpha = pulse
                    scaleX = 0.9f + 0.1f * pulse
                    scaleY = 0.9f + 0.1f * pulse
                }
                .clip(CircleShape)
                .background(Color(0xFF30D158))
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainder = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$remainder"
}
