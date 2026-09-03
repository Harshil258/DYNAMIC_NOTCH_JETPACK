package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppLargeTitle
import ai.emots.kishan_dynamic.ui.kit.AppProgressBar
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Live tab — one segmented selector drives both the island stage
 * and the detail card underneath, so the two always agree.
 */
@Composable
fun LiveActivitiesScreen() {
    val activityTypes = listOf("Music", "Delivery", "Sports", "Flight")
    var selectedActivityIndex by remember { mutableIntStateOf(0) }

    var isMusicPlaying by remember { mutableStateOf(true) }
    var musicProgress by remember { mutableFloatStateOf(42f) }

    val activeIslandState = when (selectedActivityIndex) {
        0 -> if (isMusicPlaying) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
        1 -> IslandDemoState.DeliveryExpanded
        2 -> IslandDemoState.SportsExpanded
        3 -> IslandDemoState.FlightExpanded
        else -> IslandDemoState.MusicExpanded
    }

    AppScreen(bottomInset = BottomDockInset) {
        AppLargeTitle(
            modifier = Modifier.appReveal(0),
            title = "Live Activity",
            subtitle = "Ongoing events that stay pinned to your island."
        )

        AppSegmented(
            options = activityTypes,
            selectedIndex = selectedActivityIndex,
            onOptionSelected = { selectedActivityIndex = it }
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (selectedActivityIndex == 0) "Tap the island to play or pause" else null,
            minHeight = 200.dp
        ) {
            DynamicIslandPill(
                state = activeIslandState,
                onTap = {
                    if (selectedActivityIndex == 0) isMusicPlaying = !isMusicPlaying
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Details")
        AnimatedContent(
            targetState = selectedActivityIndex,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "live_activity_detail"
        ) { index ->
            when (index) {
                0 -> MusicActivityCard(
                    isPlaying = isMusicPlaying,
                    progress = musicProgress,
                    onTogglePlay = { isMusicPlaying = !isMusicPlaying },
                    onSeek = { musicProgress = it }
                )

                1 -> DeliveryActivityCard()
                2 -> SportsActivityCard()
                3 -> FlightActivityCard()
            }
        }

        AppFootnote("Live Activities are previews. Real events from supported apps appear automatically.")
    }
}

// =============================================================================
// MUSIC
// =============================================================================

@Composable
private fun MusicActivityCard(
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "disc")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "disc_rotation"
    )

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.md))
                    .background(
                        Brush.linearGradient(
                            listOf(AppTheme.colors.accent, AppTheme.colors.info)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .rotate(if (isPlaying) rotation else 0f)
                        .clip(CircleShape)
                        .background(AppTheme.colors.background.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 15.dp)
                }
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "Starboy",
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1
                )
                AppText(
                    text = "The Weeknd · Starboy",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            AppStatusPill(
                text = if (isPlaying) "Playing" else "Paused",
                color = if (isPlaying) AppTheme.colors.success else AppTheme.colors.textTertiary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppSlider(
            title = "Position",
            value = progress,
            valueRange = 0f..100f,
            onValueChange = onSeek,
            valueFormatter = { value ->
                val current = (value * 2.3f).toInt()
                val remaining = 230 - current
                "${current / 60}:${(current % 60).toString().padStart(2, '0')} · -${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}"
            }
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransportButton(glyph = AppleGlyph.Backward, onClick = {})
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTogglePlay
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play,
                    tint = Color.White,
                    size = 22.dp
                )
            }
            TransportButton(glyph = AppleGlyph.Forward, onClick = {})
        }
    }
}

@Composable
private fun TransportButton(glyph: AppleGlyph, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceElevated)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(glyph = glyph, tint = AppTheme.colors.textPrimary, size = 18.dp)
    }
}

// =============================================================================
// DELIVERY
// =============================================================================

@Composable
private fun DeliveryActivityCard() {
    val transition = rememberInfiniteTransition(label = "delivery")
    val routeProgress by transition.animateFloat(
        initialValue = 0.62f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "route"
    )

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "Chipotle Mexican Grill",
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1
                )
                AppText(
                    text = "Order #8492 · 2 burrito bowls",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            AppStatusPill(text = "On the way", color = AppTheme.colors.warning)
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText(
                text = "Picked up",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary
            )
            AppText(
                text = "Arrives 9:24 PM",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.accent
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppProgressBar(progress = routeProgress, color = AppTheme.colors.warning)

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Maps, tint = AppTheme.colors.accent, size = 17.dp)
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "Marco · Vespa Sprint",
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textPrimary
                )
                AppText(
                    text = "4.9 rating · 1,240 deliveries",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.success.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Phone, tint = AppTheme.colors.success, size = 16.dp)
            }
        }
    }
}

// =============================================================================
// SPORTS
// =============================================================================

@Composable
private fun SportsActivityCard() {
    val transition = rememberInfiniteTransition(label = "sports")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "live_dot"
    )

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "Champions League · Semi-final",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs + 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(AppTheme.colors.error)
                )
                AppText(
                    text = "78' LIVE",
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.error,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBadge(code = "RMA", name = "Real Madrid", modifier = Modifier.weight(1f))
            AppText(
                text = "2 – 1",
                style = AppTheme.typography.h1,
                color = AppTheme.colors.textPrimary,
                letterSpacing = 1.sp
            )
            TeamBadge(code = "MCI", name = "Man City", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            AppText(
                text = "Vini Jr. 34'  ·  Bellingham 67'",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary
            )
            AppText(
                text = "De Bruyne 51'",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun TeamBadge(code: String, name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = code,
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )
        }
        AppText(
            text = name,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
            maxLines = 1
        )
    }
}

// =============================================================================
// FLIGHT
// =============================================================================

@Composable
private fun FlightActivityCard() {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "Delta Air Lines · DL 492",
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            AppStatusPill(text = "On time", color = AppTheme.colors.success)
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AppText(
                    text = "SFO",
                    style = AppTheme.typography.h1,
                    color = AppTheme.colors.textPrimary
                )
                AppText(
                    text = "San Francisco",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(AppTheme.colors.border)
                )
                AppleIcon(
                    glyph = AppleGlyph.Airplane,
                    tint = AppTheme.colors.accent,
                    size = 20.dp,
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.sm)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(AppTheme.colors.border)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                AppText(
                    text = "JFK",
                    style = AppTheme.typography.h1,
                    color = AppTheme.colors.textPrimary
                )
                AppText(
                    text = "New York",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FlightStat(label = "Gate", value = "B14")
            FlightStat(label = "Terminal", value = "T2")
            FlightStat(label = "Altitude", value = "36,000 ft")
            FlightStat(label = "Remaining", value = "2h 15m")
        }
    }
}

@Composable
private fun FlightStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
        AppText(
            text = label.uppercase(),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
        AppText(
            text = value,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary
        )
    }
}
