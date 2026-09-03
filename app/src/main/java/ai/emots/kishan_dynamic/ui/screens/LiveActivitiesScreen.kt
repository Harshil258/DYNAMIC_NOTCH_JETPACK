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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppleSegmentedControl
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySlider
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveActivitiesScreen() {
    val scope = rememberCoroutineScope()
    val activityTypes = listOf("Music", "Delivery", "Sports", "Flight")
    var selectedActivityIndex by remember { mutableIntStateOf(0) }

    // Music State
    var isMusicPlaying by remember { mutableStateOf(true) }
    var musicProgress by remember { mutableFloatStateOf(42f) }

    // Dispatch feedback
    var dispatchedToast by remember { mutableStateOf<String?>(null) }

    // Island State tied to activity selection (Figma authentic components)
    val activeIslandState = when (selectedActivityIndex) {
        0 -> if (isMusicPlaying) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
        1 -> IslandDemoState.DeliveryExpanded
        2 -> IslandDemoState.SportsExpanded
        3 -> IslandDemoState.FlightExpanded
        else -> IslandDemoState.MusicExpanded
    }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.layout.sectionGap)
        ) {
            // =================================================================
            // SCREEN HEADER
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                AppText(
                    text = "Live Activity",
                    style = AppTheme.typography.h1,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                AppText(
                    text = "Real-time interactive widgets synced with your phone's notch",
                    style = AppTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93)
                )
            }

            // =================================================================
            // 1. MINIMAL ACTIVITY SELECTOR
            // =================================================================
            AppleSegmentedControl(
                options = activityTypes,
                selectedIndex = selectedActivityIndex,
                onOptionSelected = { selectedActivityIndex = it }
            )

            // =================================================================
            // 2. LIVE NOTCH STAGE IN OPEN SPACE (NO CARD ENCLOSURE)
            // =================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                DynamicIslandPill(
                    state = activeIslandState,
                    onTap = {
                        if (selectedActivityIndex == 0) {
                            isMusicPlaying = !isMusicPlaying
                        }
                    }
                )
            }

            // =================================================================
            // 3. AUTHENTIC APPLE LIVE ACTIVITY CARDS
            // =================================================================
            AnimatedContent(
                targetState = selectedActivityIndex,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "live_activity_card_morph"
            ) { activityIdx ->
                when (activityIdx) {
                    0 -> MusicLiveActivityCard(
                        isPlaying = isMusicPlaying,
                        progress = musicProgress,
                        onTogglePlay = { isMusicPlaying = !isMusicPlaying },
                        onSeek = { musicProgress = it }
                    )
                    1 -> DeliveryLiveActivityCard()
                    2 -> SportsLiveActivityCard()
                    3 -> FlightLiveActivityCard()
                }
            }

            // =================================================================
            // 4. INTERACTIVE ISLAND ACTIONS
            // =================================================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppleButton(
                    text = "Send Activity to Dynamic Island",
                    onClick = {
                        scope.launch {
                            dispatchedToast = "Live Activity pinned to Dynamic Island!"
                            delay(3000)
                            dispatchedToast = null
                        }
                    },
                    glyph = AppleGlyph.Sparkles,
                    style = AppleButtonStyle.PRIMARY,
                    paddingVertical = 15.dp
                )

                if (dispatchedToast != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F2E20))
                            .border(1.dp, AppTheme.colors.success.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 16.dp)
                            AppText(
                                text = dispatchedToast ?: "",
                                style = AppTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.success
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENTS: AUTHENTIC APPLE LIVE ACTIVITIES
// =============================================================================

@Composable
private fun MusicLiveActivityCard(
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_activity_rot")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rot"
    )

    LuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF3366), Color(0xFF8B5CF6), Color(0xFF00F5D4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(if (isPlaying) discRotation else 0f)
                                .clip(CircleShape)
                                .background(Color(0xFF140F22)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 16.dp)
                        }
                    }

                    Column {
                        AppText(
                            text = "Starboy (feat. Daft Punk)",
                            style = AppTheme.typography.h3,
                            color = Color.White
                        )
                        AppText(
                            text = "The Weeknd • Starboy (Deluxe)",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1B2C22))
                        .border(1.dp, Color(0x3310B981), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    AppText(
                        text = "SPOTIFY",
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.success
                    )
                }
            }

            // Timeline Scrubber
            LuxurySlider(
                title = "Scrub Track Timeline",
                value = progress,
                valueRange = 0f..100f,
                valueFormatter = {
                    val currentSec = (it * 2.3f).toInt()
                    val remSec = 230 - currentSec
                    "${currentSec / 60}:${(currentSec % 60).toString().padStart(2, '0')} / -${remSec / 60}:${(remSec % 60).toString().padStart(2, '0')}"
                },
                onValueChange = onSeek
            )

            // Apple Media Control Disc
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1A2C))
                        .border(1.dp, Color(0x18FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.ChevronLeft, tint = Color.White, size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play,
                        tint = Color.Black,
                        size = 22.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1A2C))
                        .border(1.dp, Color(0x18FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.ChevronRight, tint = Color.White, size = 20.dp)
                }
            }
        }
    }
}

@Composable
private fun DeliveryLiveActivityCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "scooter_glide")
    val scooterOffset by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scooter_x"
    )

    LuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header: Restaurant & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    AppText(
                        text = "Chipotle Mexican Grill",
                        style = AppTheme.typography.h3,
                        color = Color.White
                    )
                    AppText(
                        text = "Order #8492 • 2 Burrito Bowls",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF2E2414))
                        .border(1.dp, Color(0x40F59E0B), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    AppText(
                        text = "ON THE WAY",
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.gold
                    )
                }
            }

            // Animated Route Progress Line
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText(text = "Picked Up", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                    AppText(text = "Arriving in 12 mins (9:24 PM)", style = AppTheme.typography.caption, fontWeight = FontWeight.Bold, color = AppTheme.colors.secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1A2C))
                ) {
                    // Active Gradient Progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scooterOffset)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppTheme.colors.primary, AppTheme.colors.secondary)
                                )
                            )
                    )
                }
            }

            // Courier Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF171424))
                    .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Maps, tint = AppTheme.colors.secondary, size = 18.dp)
                    }

                    Column {
                        AppText(text = "Marco • Vespa Sprint", style = AppTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        AppText(text = "4.9 ★ • 1,240 Deliveries", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.success.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Phone, tint = AppTheme.colors.success, size = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SportsLiveActivityCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "match_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "match_dot"
    )

    LuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header with Live Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppText(
                    text = "UEFA Champions League • Semi-Final",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(dotAlpha)
                            .clip(CircleShape)
                            .background(AppTheme.colors.success)
                    )
                    AppText(
                        text = "78' LIVE",
                        style = AppTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.success,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Scoreboard Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Team 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26203D)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(text = "RMA", style = AppTheme.typography.h3, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText(text = "Real Madrid", style = AppTheme.typography.caption, fontWeight = FontWeight.Medium, color = Color.White)
                }

                // Score
                AppText(
                    text = "2  -  1",
                    style = AppTheme.typography.h1,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                // Team 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF192C3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(text = "MCI", style = AppTheme.typography.h3, color = AppTheme.colors.info)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText(text = "Man City", style = AppTheme.typography.caption, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }

            // Scorers Timeline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF151322))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText(text = "⚽ Vini Jr. 34', Bellingham 67'", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                    AppText(text = "⚽ De Bruyne 51'", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun FlightLiveActivityCard() {
    LuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppText(
                    text = "Delta Air Lines • DL 492",
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF162B20))
                        .border(1.dp, Color(0x3310B981), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    AppText(
                        text = "ON TIME",
                        style = AppTheme.typography.badge,
                        color = AppTheme.colors.success
                    )
                }
            }

            // Airport Codes & Airplane Arc
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    AppText(text = "SFO", style = AppTheme.typography.h1, color = Color.White)
                    AppText(text = "San Francisco", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.width(36.dp).height(2.dp).background(Color(0x33FFFFFF)))
                    AppleIcon(glyph = AppleGlyph.Airplane, tint = AppTheme.colors.info, size = 20.dp)
                    Box(modifier = Modifier.width(36.dp).height(2.dp).background(Color(0x33FFFFFF)))
                }

                Column(horizontalAlignment = Alignment.End) {
                    AppText(text = "JFK", style = AppTheme.typography.h1, color = Color.White)
                    AppText(text = "New York", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                }
            }

            // Flight Details Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF151322))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    AppText(text = "GATE", style = AppTheme.typography.badge, color = AppTheme.colors.textSecondary)
                    AppText(text = "B14", style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    AppText(text = "TERMINAL", style = AppTheme.typography.badge, color = AppTheme.colors.textSecondary)
                    AppText(text = "T2", style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    AppText(text = "ALTITUDE", style = AppTheme.typography.badge, color = AppTheme.colors.textSecondary)
                    AppText(text = "36,000 ft", style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    AppText(text = "REMAINING", style = AppTheme.typography.badge, color = AppTheme.colors.textSecondary)
                    AppText(text = "2h 15m", style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = AppTheme.colors.secondary)
                }
            }
        }
    }
}
