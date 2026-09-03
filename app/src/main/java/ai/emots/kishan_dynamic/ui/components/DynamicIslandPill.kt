package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class IslandDemoState {
    Minimal,
    MusicCompact,
    MusicExpanded,
    CallCompact,
    CallExpanded,
    NotificationCompact,
    NotificationExpanded,
    ChargingCompact,
    ChargingExpanded,
    TimerCompact,
    TimerExpanded,
    DeliveryCompact,
    DeliveryExpanded,
    FlightCompact,
    FlightExpanded,
    SportsCompact,
    SportsExpanded,
    NavigationCompact
}

/**
 * Authentic Apple iOS Dynamic Island Components
 * Reconstructed 1:1 from official iOS reference screenshots:
 *
 * 1. Music Expanded (media_1788458385303.png):
 *    - 52dp Album Art Squircle (Heat Waves) + [E] Explicit Badge + "Grass Animals"
 *    - Neon Magenta 5-bar Jumping Equalizer on right
 *    - Middle Scrubber Row: "0:50" | [==== progress ====] | "-3:11" all on ONE horizontal line!
 *    - Bottom Row: Solid white [ << ] [ ▶ ] [ >> ] [ AirPlay ] directly on black (NO container circles!)
 *    - Height: 192dp, Radius: 44dp squircle
 *
 * 2. Incoming Call Expanded (media_1788458397515.png):
 *    - 52dp Circular Avatar + "Mobile" (gray) & "Tamia Castillo" (bold white)
 *    - Red Decline Button (#FF3B30, 50dp circle) + Green Accept Button (#34C759, 50dp circle)
 *    - Height: 96dp capsule
 *
 * 3. Silent Mode Expanded (media_1788458403251.png):
 *    - White BellSlash glyph + "SilentMode" & "On"
 *    - Apple Charcoal Pill [ Unmute ] (#2C2C2E)
 *    - Height: 96dp capsule
 *
 * 4. Timer Expanded (media_1788458407179.png):
 *    - Left: Orange Pause button (50dp, #5C2B00) + Charcoal Cancel "X" button (50dp, #3A3A3C)
 *    - Right: "Timer" (orange) + "3:35" (large bold orange 32sp)
 *    - Height: 96dp capsule
 */
@Composable
fun DynamicIslandPill(
    state: IslandDemoState,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.pressSpring(),
        label = "island_press"
    )

    // -------------------------------------------------------------------------
    // TRUE iOS GEOMETRY
    //
    // Measured against the iPhone 15 Pro reference (393pt wide). The compact
    // presentations sit between the idle 126pt pill and ~160pt — they are
    // deliberately NOT screen-width banners. Only genuinely expanded sheets
    // reach the 371pt maximum.
    // -------------------------------------------------------------------------
    val islandTokens = ai.emots.kishan_dynamic.ui.theme.AppTheme.island
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val maxExpanded = islandTokens.expandedWidth(screenWidth)

    val targetWidth: Dp = when (state) {
        IslandDemoState.Minimal -> 126.dp
        IslandDemoState.MusicCompact -> 134.dp
        IslandDemoState.CallCompact -> 140.dp
        IslandDemoState.ChargingCompact -> 132.dp
        IslandDemoState.NotificationCompact -> 152.dp
        IslandDemoState.TimerCompact -> 132.dp
        IslandDemoState.DeliveryCompact -> 140.dp
        IslandDemoState.FlightCompact -> 146.dp
        IslandDemoState.SportsCompact -> 144.dp
        IslandDemoState.NavigationCompact -> 150.dp
        // Expanded sheets: the device width minus iOS side margins, capped.
        else -> maxExpanded
    }

    // Exact Apple prototype heights per state type:
    // - Compact: 37dp
    // - Capsule Expanded (Call, Silent/Alerts, Timer, Charging): 96dp (228px @2x spec)
    // - Full Card Expanded (Music): 192dp (418px @2x spec)
    // - Full Activity Expanded (Delivery, Flight, Sports): 164dp
    val targetHeight: Dp = when (state) {
        IslandDemoState.Minimal,
        IslandDemoState.MusicCompact,
        IslandDemoState.CallCompact,
        IslandDemoState.ChargingCompact,
        IslandDemoState.NotificationCompact,
        IslandDemoState.TimerCompact,
        IslandDemoState.DeliveryCompact,
        IslandDemoState.FlightCompact,
        IslandDemoState.SportsCompact,
        IslandDemoState.NavigationCompact -> 37.33.dp

        IslandDemoState.CallExpanded,
        IslandDemoState.NotificationExpanded,
        IslandDemoState.TimerExpanded,
        IslandDemoState.ChargingExpanded -> 92.dp

        IslandDemoState.MusicExpanded -> 176.dp

        IslandDemoState.DeliveryExpanded,
        IslandDemoState.FlightExpanded,
        IslandDemoState.SportsExpanded -> 160.dp
    }

    // Curvature: 50% capsule for compact & medium expanded capsules, 44dp squircle for full cards
    val cornerRadius = when (state) {
        IslandDemoState.MusicExpanded,
        IslandDemoState.DeliveryExpanded,
        IslandDemoState.FlightExpanded,
        IslandDemoState.SportsExpanded -> RoundedCornerShape(44.dp)
        else -> RoundedCornerShape(percent = 50)
    }

    val isSplit = state == IslandDemoState.CallCompact || state == IslandDemoState.TimerCompact

    // Apple Liquid Morphing Springs
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.islandSpring(),
        label = "island_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.islandSpring(),
        label = "island_height"
    )

    // Breathing Ambient Specular Aura
    val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    val auraColor = when (state) {
        IslandDemoState.CallCompact, IslandDemoState.CallExpanded -> Color(0xFF10B981)
        IslandDemoState.ChargingCompact, IslandDemoState.ChargingExpanded -> Color(0xFF34C759)
        IslandDemoState.MusicCompact, IslandDemoState.MusicExpanded -> Color(0xFFFA2D48)
        IslandDemoState.NotificationCompact, IslandDemoState.NotificationExpanded -> Color(0xFF8E8E93)
        IslandDemoState.TimerCompact, IslandDemoState.TimerExpanded -> Color(0xFFFF9500)
        IslandDemoState.DeliveryCompact, IslandDemoState.DeliveryExpanded -> Color(0xFFF59E0B)
        IslandDemoState.FlightCompact, IslandDemoState.FlightExpanded -> Color(0xFF00F5D4)
        IslandDemoState.SportsCompact, IslandDemoState.SportsExpanded -> Color(0xFF8B5CF6)
        else -> Color(0xFF6D82FF)
    }

    Box(contentAlignment = Alignment.Center) {
        // Soft Ambient Aura Bloom
        Box(
            modifier = Modifier
                .width(animatedWidth + 8.dp)
                .height(animatedHeight + 8.dp)
                .clip(cornerRadius)
                .background(auraColor.copy(alpha = auraAlpha * 0.22f))
        )

        Row(
            modifier = modifier
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                    // Grow downwards out of the cutout, like the real island.
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Island Pill Body
            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .shadow(
                        elevation = 14.dp,
                        shape = cornerRadius,
                        ambientColor = Color.Black.copy(alpha = 0.7f),
                        spotColor = Color.Black.copy(alpha = 0.9f)
                    )
                    .clip(cornerRadius)
                    .background(Color(0xFF000000))
                    .border(
                        width = 0.75.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x38FFFFFF), Color(0x06FFFFFF))
                        ),
                        shape = cornerRadius
                    )
                    .padding(
                        horizontal = when (state) {
                            IslandDemoState.MusicExpanded -> 20.dp
                            IslandDemoState.CallExpanded,
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded -> 18.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded -> 16.dp
                            else -> 11.dp
                        },
                        vertical = when (state) {
                            IslandDemoState.MusicExpanded -> 16.dp
                            IslandDemoState.CallExpanded,
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded -> 12.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded -> 12.dp
                            else -> 0.dp
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(150))
                    },
                    label = "island_content_morph"
                ) { targetState ->
                    when (targetState) {
                        IslandDemoState.Minimal -> MinimalPillContent()
                        IslandDemoState.MusicCompact -> MusicCompactContent()
                        IslandDemoState.MusicExpanded -> MusicExpandedContent()
                        IslandDemoState.CallCompact -> CallCompactContent()
                        IslandDemoState.CallExpanded -> CallExpandedContent()
                        IslandDemoState.NotificationCompact -> NotificationCompactContent()
                        IslandDemoState.NotificationExpanded -> NotificationExpandedContent()
                        IslandDemoState.ChargingCompact -> ChargingCompactContent()
                        IslandDemoState.ChargingExpanded -> ChargingExpandedContent()
                        IslandDemoState.TimerCompact -> TimerCompactContent()
                        IslandDemoState.TimerExpanded -> TimerExpandedContent()
                        IslandDemoState.DeliveryCompact -> DeliveryCompactContent()
                        IslandDemoState.DeliveryExpanded -> DeliveryExpandedContent()
                        IslandDemoState.FlightCompact -> FlightCompactContent()
                        IslandDemoState.FlightExpanded -> FlightExpandedContent()
                        IslandDemoState.SportsCompact -> SportsCompactContent()
                        IslandDemoState.SportsExpanded -> SportsExpandedContent()
                        IslandDemoState.NavigationCompact -> NavigationCompactContent()
                    }
                }
            }

            // Companion Split Bubble (media_1788455788286.png: 11dp gap + 37dp circular bubble)
            AnimatedVisibility(visible = isSplit) {
                Row {
                    Spacer(modifier = Modifier.width(11.dp))
                    Box(
                        modifier = Modifier
                            .size(37.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black,
                                spotColor = auraColor.copy(alpha = 0.5f)
                            )
                            .clip(CircleShape)
                            .background(Color(0xFF000000))
                            .border(
                                width = 0.75.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0x38FFFFFF), Color(0x06FFFFFF))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state == IslandDemoState.CallCompact) {
                            AppleIcon(glyph = AppleGlyph.Phone, tint = Color(0xFF10B981), size = 15.dp)
                        } else if (state == IslandDemoState.TimerCompact) {
                            AppleIcon(glyph = AppleGlyph.Timer, tint = Color(0xFFFF9500), size = 15.dp)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// REUSABLE COMPACT LAYOUT
// =============================================================================

@Composable
private fun CompactIslandLayout(
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(37.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            leading()
        }
        Box(contentAlignment = Alignment.CenterEnd) {
            trailing()
        }
    }
}

// =============================================================================
// AUTHENTIC EXPANDED & COMPACT ISLAND IMPLEMENTATIONS
// =============================================================================

@Composable
private fun MinimalPillContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(Color(0xFF08080C))
                .border(1.dp, Color(0x22FFFFFF), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759))
        )
    }
}

// -----------------------------------------------------------------------------
// 1. MUSIC (APPLE MUSIC / SPOTIFY) - MATCHING media_1788458385303.png EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun MusicCompactContent() {
    CompactIslandLayout(
        leading = {
            // Album Art Squircle 20dp
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE0A8FF), Color(0xFF818CF8), Color(0xFF38BDF8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 10.dp)
            }
        },
        trailing = {
            // Neon Magenta 4-bar Equalizer
            LiveEqualizerMini(color = Color(0xFFFA2D48))
        }
    )
}

@Composable
private fun MusicExpandedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP ROW: Album Art + Title & Explicit Badge & Artist + Pink Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 52dp Album Art Squircle (Glass Animals Dreamland style)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE879F9), Color(0xFF818CF8), Color(0xFF38BDF8))
                            )
                        )
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 20.dp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Heat Waves",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        // Apple [E] Explicit Badge
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Color(0xFF8E8E93)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "E",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Grass Animals",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Neon Pink/Magenta Waveform Equalizer
            LiveEqualizerMini(color = Color(0xFFFA2D48))
        }

        // MIDDLE ROW: Scrubber Slider with Elapsed and Remaining Time on the SAME line
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "0:50",
                fontSize = 12.sp,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )

            // Linear Progress Track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.32f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Text(
                text = "-3:11",
                fontSize = 12.sp,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }

        // BOTTOM ROW: Pure Solid White Media Controls directly on Black (NO circle container!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Track [ ◀◀ ]
            AppleIcon(glyph = AppleGlyph.Backward, tint = Color.White, size = 26.dp)

            // Play Triangle [ ▶ ] (Large solid white vector triangle)
            AppleIcon(glyph = AppleGlyph.Play, tint = Color.White, size = 32.dp)

            // Next Track [ ▶▶ ]
            AppleIcon(glyph = AppleGlyph.Forward, tint = Color.White, size = 26.dp)

            // AirPlay Audio Route
            AppleIcon(glyph = AppleGlyph.AirPlay, tint = Color.White, size = 24.dp)
        }
    }
}

// -----------------------------------------------------------------------------
// 2. INCOMING CALL - MATCHING media_1788458397515.png EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun CallCompactContent() {
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 11.dp)
            }
        },
        trailing = {
            Text(
                text = "02:45",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                fontFamily = FontFamily.Monospace
            )
        }
    )
}

@Composable
private fun CallExpandedContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Avatar + "Mobile" & "Tamia Castillo"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFEA580C), Color(0xFF9A3412))
                        )
                    )
                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TC",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Mobile",
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tamia Castillo",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // RIGHT: Red Decline Button + Green Accept Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Red Decline Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30)),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.rotate(135f)) {
                    AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 22.dp)
                }
            }

            // Green Accept Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 22.dp)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. SILENT MODE / NOTIFICATION - MATCHING media_1788458403251.png EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun NotificationCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.BellSlash, tint = Color.White, size = 16.dp)
        },
        trailing = {
            Text(text = "Silent", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8E8E93))
        }
    )
}

@Composable
private fun NotificationExpandedContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Slash Bell + "SilentMode" & "On"
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.BellSlash,
                tint = Color.White,
                size = 28.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "SilentMode",
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "On",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // RIGHT: Apple Dark Charcoal Glass Pill Button [ Unmute ]
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF2C2C2E))
                .padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Unmute",
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 4. TIMER - MATCHING media_1788458407179.png EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun TimerCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Timer, tint = Color(0xFFFF9500), size = 16.dp)
        },
        trailing = {
            Text(
                text = "3:35",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9500),
                fontFamily = FontFamily.Monospace
            )
        }
    )
}

@Composable
private fun TimerExpandedContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Orange Pause Button (50dp) + Charcoal Cancel "X" Button (50dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Orange Pause Circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5C2B00)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Pause, tint = Color(0xFFFF9500), size = 18.dp)
            }

            // Dark Charcoal Cancel Circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Close, tint = Color.White, size = 18.dp)
            }
        }

        // RIGHT: "Timer" + "3:35"
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Timer",
                color = Color(0xFFFF9500),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 3.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "3:35",
                color = Color(0xFFFF9500),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 5. BATTERY / FAST CHARGING
// -----------------------------------------------------------------------------
@Composable
private fun ChargingCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Battery, tint = Color(0xFF34C759), size = 18.dp)
        },
        trailing = {
            Text(text = "85% ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34C759), fontFamily = FontFamily.Monospace)
        }
    )
}

@Composable
private fun ChargingExpandedContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Battery, tint = Color(0xFF34C759), size = 26.dp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(text = "Fast Warp Charging", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "65W • 14 min until full", color = Color(0xFF34C759), fontSize = 12.sp)
            }
        }

        Text(
            text = "85%",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF34C759),
            fontFamily = FontFamily.Monospace
        )
    }
}

// -----------------------------------------------------------------------------
// 6. DELIVERY (DOORDASH / UBER EATS)
// -----------------------------------------------------------------------------
@Composable
private fun DeliveryCompactContent() {
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Torch, tint = Color.Black, size = 11.dp)
            }
        },
        trailing = {
            Text(text = "12m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        }
    )
}

@Composable
private fun DeliveryExpandedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Torch, tint = Color(0xFFF59E0B), size = 22.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Chipotle Mexican Grill", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Order #8492 • 2 items", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "ON THE WAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.70f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Picked up", fontSize = 10.sp, color = Color(0xFF8E8E93))
                Text(text = "Arriving in 12 mins", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Marco • Vespa Sprint • 4.9 ★", fontSize = 12.sp, color = Color.White)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 14.dp)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 7. FLIGHT TRACKER (DELTA AIR LINES)
// -----------------------------------------------------------------------------
@Composable
private fun FlightCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Airplane, tint = Color(0xFF00F5D4), size = 16.dp)
        },
        trailing = {
            Text(text = "2h 15m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F5D4))
        }
    )
}

@Composable
private fun FlightExpandedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Delta Air Lines • DL 492", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "San Francisco ➔ New York", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "ON TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "SFO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .height(3.dp)
                    .background(Color(0xFF3A3A3C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .fillMaxHeight()
                        .background(Color(0xFF00F5D4))
                )
            }
            Text(text = "JFK", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Gate B14 • Terminal 2", fontSize = 11.sp, color = Color(0xFF8E8E93))
            Text(text = "Alt: 36,000 ft • 2h 15m left", fontSize = 11.sp, color = Color(0xFF00F5D4), fontWeight = FontWeight.SemiBold)
        }
    }
}

// -----------------------------------------------------------------------------
// 8. SPORTS MATCH (UEFA CHAMPIONS LEAGUE)
// -----------------------------------------------------------------------------
@Composable
private fun SportsCompactContent() {
    CompactIslandLayout(
        leading = {
            Text(text = "RMA 2", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "1 MCI", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Text(text = "78'", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        }
    )
}

@Composable
private fun SportsExpandedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "UEFA Champions League", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Text(text = "78' LIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "Real Madrid", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Vini Jr 34', Jude 67'", fontSize = 10.sp, color = Color(0xFF8E8E93))
            }

            Text(
                text = "2  -  1",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24),
                fontFamily = FontFamily.Monospace
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Man City", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "De Bruyne 51'", fontSize = 10.sp, color = Color(0xFF8E8E93))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1C182A))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Quarter-Final • 2nd Leg (Agg: 4 - 4)", fontSize = 10.5.sp, color = Color(0xFF8E8E93))
        }
    }
}

// -----------------------------------------------------------------------------
// NAVIGATION (COMPACT)
// -----------------------------------------------------------------------------
@Composable
private fun NavigationCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Maps, tint = Color(0xFF3B82F6), size = 16.dp)
        },
        trailing = {
            Text(text = "200m", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
        }
    )
}

// -----------------------------------------------------------------------------
// EQUALIZER
// -----------------------------------------------------------------------------
@Composable
private fun LiveEqualizerMini(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(530, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(470, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(18.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height((18 * h1).dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).height((18 * h2).dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).height((18 * h3).dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).height((18 * h4).dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).height((18 * h5).dp).clip(CircleShape).background(color))
    }
}
