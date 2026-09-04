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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.foundation.layout.widthIn
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
import ai.emots.kishan_dynamic.ui.theme.AppIslandTokens

enum class IslandDemoState {
    Idle,
    Minimal,
    MusicCompact,
    MusicExpanded,
    CallCompact,
    CallExpanded,
    NotificationCompact,
    NotificationExpanded,
    SilentModeCompact,
    SilentModeExpanded,
    ChargingCompact,
    ChargingExpanded,
    LowBatteryCompact,
    LowBatteryExpanded,
    TimerCompact,
    TimerExpanded,
    DeliveryCompact,
    DeliveryExpanded,
    FlightCompact,
    FlightExpanded,
    SportsCompact,
    SportsExpanded,
    NavigationCompact,
    // Audited Figma prototypes
    TimerImage,           // Timer with cyan-blue progress ring
    NotificationImage,    // Notification card with thumbnail
    CallAvatars,          // Incoming call with avatars
    TransportActivity,    // Transport activity
    FlightTrackerExpanded,// Flight route tracker
    SubscriptionPricing,  // Pricing / mobile options
    MovieCard,            // Transit card
    ColorOptions,         // Route options
    AirplaneAlert,        // Dynamic Island-5.svg: Turn Off Airplane Mode
    ScreenMirroringAlert, // Dynamic Island-6.svg: Screen Mirroring MacBook Pro
    MobileDataAlert,      // Dynamic Island-7.svg: Mobile Data to use Wi-Fi
    TransitRouteAlert     // Dynamic Island-8.svg: Prague Main Train Station
}

/** States that share the single canonical compact-island height. */
internal fun IslandDemoState.isCompactPresentation(): Boolean = when (this) {
    IslandDemoState.Idle,
    IslandDemoState.Minimal,
    IslandDemoState.MusicCompact,
    IslandDemoState.CallCompact,
    IslandDemoState.ChargingCompact,
    IslandDemoState.LowBatteryCompact,
    IslandDemoState.SilentModeCompact,
    IslandDemoState.NotificationCompact,
    IslandDemoState.TimerCompact,
    IslandDemoState.DeliveryCompact,
    IslandDemoState.FlightCompact,
    IslandDemoState.SportsCompact,
    IslandDemoState.NavigationCompact -> true
    else -> false
}

/** One geometry resolver keeps state heights identical across every surface. */
internal fun IslandDemoState.resolvedHeight(tokens: AppIslandTokens): Dp {
    if (isCompactPresentation()) return tokens.compactHeight

    return when (this) {
        IslandDemoState.NotificationExpanded,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.TimerExpanded,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.NotificationImage,
        IslandDemoState.CallAvatars -> tokens.standardExpandedHeight

        IslandDemoState.TransitRouteAlert,
        IslandDemoState.MovieCard -> 142.dp

        IslandDemoState.ScreenMirroringAlert,
        IslandDemoState.FlightTrackerExpanded,
        IslandDemoState.DeliveryExpanded,
        IslandDemoState.FlightExpanded,
        IslandDemoState.SportsExpanded -> 144.dp

        IslandDemoState.AirplaneAlert,
        IslandDemoState.TransportActivity -> 148.dp

        IslandDemoState.MobileDataAlert,
        IslandDemoState.SubscriptionPricing -> 162.dp

        IslandDemoState.CallExpanded -> tokens.callExpandedHeight
        IslandDemoState.MusicExpanded -> tokens.musicExpandedHeight
        IslandDemoState.ColorOptions -> 185.3.dp
        else -> tokens.standardExpandedHeight
    }
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
 *    - Height: 177dp, Radius: 42dp squircle
 *
 * 2. Incoming Call Expanded (media_1788458397515.png):
 *    - 52dp Circular Avatar + "Mobile" (gray) & "Tamia Castillo" (bold white)
 *    - Red Decline Button (#FF3B30, 50dp circle) + Green Accept Button (#34C759, 50dp circle)
 *    - Height: 86dp capsule
 *
 * 3. Silent Mode Expanded (media_1788458403251.png):
 *    - White BellSlash glyph + "SilentMode" & "On"
 *    - Apple Charcoal Pill [ Unmute ] (#2C2C2E)
 *    - Height: 86dp capsule
 *
 * 4. Timer Expanded (media_1788458407179.png):
 *    - Left: Orange Pause button (50dp, #5C2B00) + Charcoal Cancel "X" button (50dp, #3A3A3C)
 *    - Right: "Timer" (orange) + "3:35" (large bold orange 32sp)
 *    - Height: 86dp capsule
 */
@Composable
fun DynamicIslandPill(
    state: IslandDemoState,
    modifier: Modifier = Modifier,
    /** Width calibration only. Height remains tied to the shared iOS token. */
    horizontalScale: Float = 1f,
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
    val isSplit = state == IslandDemoState.Minimal
    val isCompact = state.isCompactPresentation()
    val safeHorizontalScale = horizontalScale.coerceIn(0.8f, 1.2f)

    val baseWidth: Dp = when (state) {
        IslandDemoState.Idle -> islandTokens.compactWidth
        IslandDemoState.Minimal -> islandTokens.splitMainWidth
        IslandDemoState.MusicCompact -> islandTokens.mediaCompactWidth
        IslandDemoState.CallCompact -> 152.dp
        IslandDemoState.ChargingCompact -> 132.dp
        IslandDemoState.LowBatteryCompact -> 132.dp
        IslandDemoState.SilentModeCompact -> 152.dp
        IslandDemoState.NotificationCompact -> 152.dp
        IslandDemoState.TimerCompact -> islandTokens.timerCompactWidth
        IslandDemoState.DeliveryCompact -> 160.dp
        IslandDemoState.FlightCompact -> 160.dp
        IslandDemoState.SportsCompact -> 156.dp
        IslandDemoState.NavigationCompact -> 156.dp
        IslandDemoState.TimerImage -> 168.dp
        IslandDemoState.NotificationImage -> 160.dp
        IslandDemoState.CallAvatars -> maxExpanded
        // Expanded sheets: the device width minus iOS side margins, capped.
        else -> maxExpanded
    }
    val splitExtras = if (isSplit) islandTokens.splitGap + islandTokens.sideSize else 0.dp
    val maxMainWidth = (maxExpanded - splitExtras).coerceAtLeast(islandTokens.compactHeight)
    val requestedWidth = if (isCompact) baseWidth * safeHorizontalScale else baseWidth
    val targetWidth = requestedWidth.coerceAtMost(maxMainWidth)

    // Exact Apple prototype heights per state type (from Figma):
    // - Compact / Minimal: 36.67dp (shared hardware-height token)
    // - Standard Expanded (Timer, Silent, Call, Notification): 86dp
    // - Full Activity Sheets (measured from Figma SVGs):
    //   - Transit: 142dp
    //   - Screen Mirroring: 144dp
    //   - Airplane Alert: 148dp
    //   - Mobile Data / Hotspot: 162dp
    //   - Active Call (5 action buttons): 166dp
    //   - Music Player (scrubber & controls): 177dp
    //   - Turn-by-Turn Navigation: 185.3dp
    val targetHeight = state.resolvedHeight(islandTokens)

    // Corner curvature: 42dp for full sheets, 43-44dp for 86dp capsules, 50% for compact
    val cornerRadius = when (state) {
        IslandDemoState.Idle,
        IslandDemoState.Minimal,
        IslandDemoState.MusicCompact,
        IslandDemoState.CallCompact,
        IslandDemoState.ChargingCompact,
        IslandDemoState.LowBatteryCompact,
        IslandDemoState.SilentModeCompact,
        IslandDemoState.NotificationCompact,
        IslandDemoState.TimerCompact,
        IslandDemoState.DeliveryCompact,
        IslandDemoState.FlightCompact,
        IslandDemoState.SportsCompact,
        IslandDemoState.NavigationCompact -> RoundedCornerShape(percent = 50)

        IslandDemoState.NotificationExpanded,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.TimerExpanded,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.NotificationImage,
        IslandDemoState.CallAvatars -> RoundedCornerShape(percent = 50)

        else -> RoundedCornerShape(islandTokens.expandedCorner)
    }

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

    // Breathing Ambient Specular Aura (iOS Dynamic Island glow)
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
        IslandDemoState.Idle -> Color(0xFF4C6FFF)
        IslandDemoState.CallCompact, IslandDemoState.CallExpanded -> Color(0xFF37C058)
        IslandDemoState.ChargingCompact, IslandDemoState.ChargingExpanded -> Color(0xFF34C759)
        IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded -> Color(0xFFFF453A)
        IslandDemoState.MusicCompact, IslandDemoState.MusicExpanded -> Color(0xFFFA2D48)
        IslandDemoState.SilentModeCompact, IslandDemoState.SilentModeExpanded -> Color(0xFFFF453A)
        IslandDemoState.NotificationCompact, IslandDemoState.NotificationExpanded -> Color(0xFF25D366)
        IslandDemoState.TimerCompact, IslandDemoState.TimerExpanded -> Color(0xFF2A86E6)
        IslandDemoState.DeliveryCompact, IslandDemoState.DeliveryExpanded -> Color(0xFFF59E0B)
        IslandDemoState.FlightCompact, IslandDemoState.FlightExpanded -> Color(0xFF00F5D4)
        IslandDemoState.SportsCompact, IslandDemoState.SportsExpanded -> Color(0xFF8B5CF6)
        IslandDemoState.TimerImage -> Color(0xFF67EBF5)
        IslandDemoState.NotificationImage -> Color(0xFF8E8E93)
        IslandDemoState.CallAvatars -> Color(0xFF37C058)
        IslandDemoState.TransportActivity, IslandDemoState.AirplaneAlert -> Color(0xFFFB8B28)
        IslandDemoState.FlightTrackerExpanded, IslandDemoState.ScreenMirroringAlert -> Color(0xFF37A3DE)
        IslandDemoState.SubscriptionPricing, IslandDemoState.MobileDataAlert -> Color(0xFF37C058)
        IslandDemoState.MovieCard, IslandDemoState.TransitRouteAlert -> Color(0xFFFA3532)
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
                            IslandDemoState.MusicExpanded -> 18.dp
                            IslandDemoState.CallExpanded -> 16.dp
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.SilentModeExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded,
                            IslandDemoState.LowBatteryExpanded -> 18.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded,
                            IslandDemoState.AirplaneAlert,
                            IslandDemoState.TransportActivity,
                            IslandDemoState.ScreenMirroringAlert,
                            IslandDemoState.FlightTrackerExpanded,
                            IslandDemoState.MobileDataAlert,
                            IslandDemoState.SubscriptionPricing,
                            IslandDemoState.TransitRouteAlert,
                            IslandDemoState.MovieCard,
                            IslandDemoState.ColorOptions -> 16.dp
                            else -> 11.dp
                        },
                        vertical = when (state) {
                            IslandDemoState.MusicExpanded -> 16.dp
                            IslandDemoState.CallExpanded -> 14.dp
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.SilentModeExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded,
                            IslandDemoState.LowBatteryExpanded -> 12.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded,
                            IslandDemoState.AirplaneAlert,
                            IslandDemoState.TransportActivity,
                            IslandDemoState.ScreenMirroringAlert,
                            IslandDemoState.FlightTrackerExpanded,
                            IslandDemoState.MobileDataAlert,
                            IslandDemoState.SubscriptionPricing,
                            IslandDemoState.TransitRouteAlert,
                            IslandDemoState.MovieCard,
                            IslandDemoState.ColorOptions -> 14.dp
                            else -> 0.dp
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(tween(200, easing = ai.emots.kishan_dynamic.ui.motion.AppMotion.EaseIslandContent)) togetherWith
                            fadeOut(tween(200, easing = ai.emots.kishan_dynamic.ui.motion.AppMotion.EaseIslandContent))
                    },
                    label = "island_content_morph"
                ) { targetState ->
                    when (targetState) {
                        IslandDemoState.Idle -> IdleCutoutContent()
                        IslandDemoState.Minimal -> MinimalPillContent()
                        IslandDemoState.MusicCompact -> MusicCompactContent()
                        IslandDemoState.MusicExpanded -> MusicExpandedContent()
                        IslandDemoState.CallCompact -> CallCompactContent()
                        IslandDemoState.CallExpanded -> FaceTimeAudioExpandedContent()
                        IslandDemoState.NotificationCompact -> NotificationCompactContent()
                        IslandDemoState.NotificationExpanded -> NotificationExpandedContent()
                        IslandDemoState.SilentModeCompact -> SilentModeCompactContent()
                        IslandDemoState.SilentModeExpanded -> SilentModeExpandedContent()
                        IslandDemoState.ChargingCompact -> ChargingCompactContent()
                        IslandDemoState.ChargingExpanded -> ChargingExpandedContent()
                        IslandDemoState.LowBatteryCompact -> LowBatteryCompactContent()
                        IslandDemoState.LowBatteryExpanded -> LowBatteryExpandedContent()
                        IslandDemoState.TimerCompact -> TimerCompactContent()
                        IslandDemoState.TimerExpanded -> TimerExpandedContent()
                        IslandDemoState.DeliveryCompact -> DeliveryCompactContent()
                        IslandDemoState.DeliveryExpanded -> DeliveryExpandedContent()
                        IslandDemoState.FlightCompact -> FlightCompactContent()
                        IslandDemoState.FlightExpanded -> FlightExpandedContent()
                        IslandDemoState.SportsCompact -> SportsCompactContent()
                        IslandDemoState.SportsExpanded -> SportsExpandedContent()
                        IslandDemoState.NavigationCompact -> NavigationCompactContent()
                        IslandDemoState.TimerImage -> TimerWithImageCompactContent()
                        IslandDemoState.NotificationImage -> NotificationImageCompactContent()
                        IslandDemoState.CallAvatars -> CallExpandedContent()
                        IslandDemoState.TransportActivity,
                        IslandDemoState.AirplaneAlert -> AirplaneAlertContent()
                        IslandDemoState.FlightTrackerExpanded,
                        IslandDemoState.ScreenMirroringAlert -> ScreenMirroringContent()
                        IslandDemoState.SubscriptionPricing,
                        IslandDemoState.MobileDataAlert -> MobileDataContent()
                        IslandDemoState.MovieCard,
                        IslandDemoState.TransitRouteAlert -> TransitRouteContent()
                        IslandDemoState.ColorOptions -> TurnByTurnNavigationContent()
                    }
                }
            }

            // Companion split bubble: canonical 11dp gap + 36.67dp circular bubble.
            AnimatedVisibility(visible = isSplit) {
                Row {
                    Spacer(modifier = Modifier.width(islandTokens.splitGap))
                    Box(
                        modifier = Modifier
                            .size(islandTokens.sideSize)
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
                        when (state) {
                            IslandDemoState.CallCompact -> {
                                AppleIcon(glyph = AppleGlyph.Phone, tint = Color(0xFF37C058), size = 15.dp)
                            }
                            IslandDemoState.TimerCompact, IslandDemoState.Minimal -> {
                                TimerProgressRing(progress = 0.72f, size = 21.dp, strokeWidth = 2.5.dp)
                            }
                            else -> {
                                AppleIcon(glyph = AppleGlyph.Timer, tint = Color(0xFF2A86E6), size = 15.dp)
                            }
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
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
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
private fun IdleCutoutContent() {
    // Pure Apple hardware cutout: 126pt OLED black, completely clean.
    Box(modifier = Modifier.fillMaxSize())
}

@Composable
private fun MinimalPillContent() {
    // Figma Minimal.svg uses the shared 36.67pt compact height.
    // Not a split pill - just a breathing status dot
    val pulse by ai.emots.kishan_dynamic.ui.motion.rememberBreathing(
        min = 0.85f,
        max = 1f,
        durationMillis = 2200
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(Color(0xFF34C759).copy(alpha = pulse))
    )
}

// -----------------------------------------------------------------------------
// 1. MUSIC (APPLE MUSIC / SPOTIFY) - MATCHING Compact.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun MusicCompactContent() {
    // Compact.svg shows: Album art + Track title + Equalizer all in compact
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
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

            Spacer(modifier = Modifier.width(8.dp))

            // Track title (compact shows truncated title)
            Text(
                text = "Heat Waves",
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 70.dp)
            )
        }

        // Neon Magenta 5-bar Equalizer (matches Figma Compact.svg exactly)
        LiveEqualizerMini(color = Color(0xFFFA2D48))
    }
}

@Composable
private fun MusicExpandedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP ROW: Album Art + Title & Artist + Pink Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 52dp Album Art Squircle
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

            // Title + Artist + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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
                                color = Color(0xFF000000),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Glass Animals",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Neon Pink/Magenta Waveform
                LiveEqualizerMini(color = Color(0xFFFA2D48))
            }
        }
    }

        // MIDDLE ROW: Scrubber Slider with Time
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

        // BOTTOM ROW: Media Controls (Backward, Play/Pause, Forward, AirPlay)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppleIcon(glyph = AppleGlyph.Backward, tint = Color.White, size = 26.dp)

            // Play/Pause icon — Figma Expanded music sheet shows the Play glyph ▶
            AppleIcon(
                glyph = AppleGlyph.Play,
                tint = Color.White,
                size = 32.dp
            )

            AppleIcon(glyph = AppleGlyph.Forward, tint = Color.White, size = 26.dp)

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
// 3. NOTIFICATION ALERT (WHATSAPP / MESSAGES)
// -----------------------------------------------------------------------------
@Composable
private fun NotificationCompactContent() {
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Bell, tint = Color.White, size = 11.dp)
            }
        },
        trailing = {
            Text(
                text = "Tamia · 2 msgs",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.widthIn(max = 100.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun NotificationExpandedContent(
    appName: String = "WhatsApp",
    sender: String = "Tamia Castillo",
    message: String = "Hey, are you free tonight? 🍕"
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Bell, tint = Color.White, size = 22.dp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = appName,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = sender,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = message,
                    color = Color(0xFFC7C7CC),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF2C2C2E))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reply",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 3B. SILENT MODE ALERT - MATCHING Dynamic Island-2.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun SilentModeCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.BellSlash, tint = Color(0xFFFF453A), size = 13.dp)
        },
        trailing = {
            Text(
                text = "Silent",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF453A)
            )
        }
    )
}

@Composable
private fun SilentModeExpandedContent(
    appName: String = "Silent Mode",
    title: String = "On"
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.BellSlash,
                tint = Color(0xFFFF453A),
                size = 28.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = appName,
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
// 4. TIMER - MATCHING Compact.svg & Expanded.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
fun TimerProgressRing(
    progress: Float = 0.72f,
    size: Dp = 21.dp,
    strokeWidth: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        // Dark track
        drawCircle(
            color = Color(0xFF1B2F44),
            radius = (this.size.minDimension - strokePx) / 2f,
            style = Stroke(width = strokePx)
        )
        // Sweep gradient cyan to blue
        val brush = Brush.sweepGradient(
            listOf(
                Color(0xFF67EBF5),
                Color(0xFF2A86E6),
                Color(0xFF67EBF5)
            )
        )
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun TimerCompactContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimerProgressRing(progress = 0.72f, size = 21.dp, strokeWidth = 3.dp)
        Text(
            text = "01:45",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TimerExpandedContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            TimerProgressRing(progress = 0.72f, size = 56.dp, strokeWidth = 5.dp)

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Timer",
                    color = Color(0xFF8E8D94),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "01:45",
                    color = Color(0xFFEBEBF0),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Circular Pause Action Button from Expanded.svg
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A292D)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = AppleGlyph.Pause, tint = Color.White, size = 18.dp)
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
// 5B. LOW BATTERY ALERT
// -----------------------------------------------------------------------------
@Composable
private fun LowBatteryCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Battery, tint = Color(0xFFFF453A), size = 18.dp)
        },
        trailing = {
            Text(
                text = "14% !",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF453A),
                fontFamily = FontFamily.Monospace
            )
        }
    )
}

@Composable
private fun LowBatteryExpandedContent() {
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
                    .background(Color(0xFFFF453A).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Battery, tint = Color(0xFFFF453A), size = 26.dp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(text = "Low Battery", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "14% remaining • Connect charger", color = Color(0xFFFF453A), fontSize = 12.sp)
            }
        }

        Text(
            text = "14%",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF453A),
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
internal fun LiveEqualizerMini(color: Color) {
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

// =============================================================================
// NEW FIGMA DYNAMIC ISLAND STATES (1-8)
// Extracted from Figma SVGs: islandfigmacomponentofios/Dynamic Island-*.svg
// =============================================================================

// -----------------------------------------------------------------------------
// AvatarCircle helper — reusable gradient avatar with initials
// Used by IncomingCallTwoAvatars (DI-3)
// -----------------------------------------------------------------------------
@Composable
private fun AvatarCircle(initials: String, gradientColors: List<Color>) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -----------------------------------------------------------------------------
// DI-1: TIMER WITH IMAGE — Dynamic Island-1.svg
// 86dp standard expanded capsule.
// Left: gradient image placeholder (square) + time label.
// Right: Canvas clock-face graphic + Play/Pause toggle buttons.
// Figma shows: image block, "15:30 — 30:00" timer label, circular clock with
// hands, and Play/Pause glyph buttons on dark bases.
// -----------------------------------------------------------------------------
@Composable
private fun TimerWithImageCompactContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT COLUMN: image placeholder + timer label
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            // Gradient image square (replaces AppleIcon photo glyph)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF667781), Color(0xFF8B99A1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Camera,
                    tint = Color.White.copy(alpha = 0.7f),
                    size = 16.dp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "15:30",
                    color = Color(0xFFFF9500),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " — 30:00",
                    color = Color(0xFF8E8E93),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // RIGHT COLUMN: Canvas clock + Play/Pause buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Canvas-drawn analog clock face
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val r = (size.minDimension / 2) - 4

                    // Clock outline
                    drawCircle(
                        Color.White.copy(alpha = 0.15f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )
                    // Hour markers (12 dots)
                    for (i in 0 until 12) {
                        val angle = Math.toRadians(i * 30.0)
                        val dx = Math.cos(angle).toFloat() * (r - 4)
                        val dy = Math.sin(angle).toFloat() * (r - 4)
                        drawCircle(
                            Color.White.copy(alpha = 0.5f),
                            radius = 1.5f,
                            center = Offset(cx + dx, cy + dy)
                        )
                    }
                    // Clock hands — ~15:30 position
                    // Hour hand (~52.5 degrees from 12)
                    val hourAngle = Math.toRadians(90.0 + 15.0 * 2.5)
                    drawLine(
                        Color.White,
                        Offset(cx, cy),
                        Offset(
                            cx + Math.cos(hourAngle).toFloat() * r * 0.5f,
                            cy + Math.sin(hourAngle).toFloat() * r * 0.5f
                        ),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    // Minute hand (~180 degrees)
                    val minAngle = Math.toRadians(90.0 + 30.0 * 6.0)
                    drawLine(
                        Color.White,
                        Offset(cx, cy),
                        Offset(
                            cx + Math.cos(minAngle).toFloat() * r * 0.75f,
                            cy + Math.sin(minAngle).toFloat() * r * 0.75f
                        ),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    // Center dot
                    drawCircle(Color.White, radius = 2.5f, center = Offset(cx, cy))
                }
            }

            // Play/Pause toggle row on dark bases
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Pause,
                        tint = Color(0xFFFF9500),
                        size = 12.dp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Forward,
                        tint = Color.White,
                        size = 12.dp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DI-2: NOTIFICATION WITH IMAGE — Dynamic Island-2.svg
// 86dp standard expanded capsule with image thumbnail,
// sender/labels, and message preview.
// -----------------------------------------------------------------------------
@Composable
private fun NotificationImageCompactContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Image thumbnail (16:9 aspect, rounded)
        Box(
            modifier = Modifier
                .size(36.dp, 20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2B599A), Color(0xFF5B8DEF)))),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = AppleGlyph.Chat,
                tint = Color.White,
                size = 12.dp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Messages",
                    color = Color(0xFF8E8E93),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "△",
                    color = Color(0xFF30D158),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Dad",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Almost there! Don't forget to bring...",
                color = Color(0xFF8E8E93),
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// DI-3: INCOMING CALL WITH TWO AVATARS — Dynamic Island-3.svg
// 86dp standard expanded capsule showing two gradient avatar circles (TM/OC) and
// an "Incoming Call" label.
// Figma shows: two 24dp circular gradient avatars + "Incoming Call" text.
// -----------------------------------------------------------------------------
@Composable
private fun IncomingCallTwoAvatarsContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(
                initials = "TM",
                gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C42))
            )
            Spacer(modifier = Modifier.width(8.dp))
            AvatarCircle(
                initials = "OC",
                gradientColors = listOf(Color(0xFF30D158), Color(0xFF0AC4B0))
            )
        }

        Text(
            text = "Incoming Call",
            color = Color(0xFF8E8E93),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// -----------------------------------------------------------------------------
// DI-4: TRANSPORT LIVE ACTIVITY — Dynamic Island-4.svg
// 148dp activity sheet: airplane icon + "2h 15m" + route label.
// Figma shows airplane glyph, duration, and "San Francisco → New York" route.
// -----------------------------------------------------------------------------
@Composable
private fun TransportLiveActivityCompactContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.Airplane,
                tint = Color(0xFF00F5D4),
                size = 14.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "2h 15m",
                color = Color(0xFF00F5D4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = "San Francisco ➔ New York",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -----------------------------------------------------------------------------
// DI-5: FLIGHT TRACKER DETAIL — Dynamic Island-5.svg
// 144dp activity sheet: Delta flight info with ON TIME badge, SFO→JFK progress bar,
// gate/altitude details.
// Figma shows: airline label, ON TIME pill, route progress bar, gate +
// altitude row, plus an airplane icon.
// -----------------------------------------------------------------------------
@Composable
private fun FlightTrackerDetailCompactContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // TOP ROW: airline label + ON TIME badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppleIcon(
                    glyph = AppleGlyph.Airplane,
                    tint = Color.White,
                    size = 13.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delta • DL 492",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        Color(0xFF10B981).copy(alpha = 0.4f),
                        RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ON TIME",
                    color = Color(0xFF10B981),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ROUTE PROGRESS BAR: SFO → JFK with teal fill at 60%
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SFO",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .padding(horizontal = 6.dp)
                    .background(Color(0xFF3A3A3C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .background(Color(0xFF00F5D4))
                )
            }
            Text(
                text = "JFK",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM ROW: Gate + Altitude
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Gate B14 • Terminal 2",
                color = Color(0xFF8E8E93),
                fontSize = 9.5.sp
            )
            Text(
                text = "Alt: 36,000 ft",
                color = Color(0xFF00F5D4),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// DI-6: DELIVERY ORDER — Dynamic Island-6.svg
// 144dp activity sheet: Chipotle-style order card with 70% progress bar, driver info,
// and phone button.
// Figma shows: torch/logo icon, "Order #..." text, progress bar, "Picked up"
// status, driver name + star rating + phone button.
// -----------------------------------------------------------------------------
@Composable
private fun DeliveryOrderCompactContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // HEADER: logo + order id
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Torch,
                        tint = Color.Black,
                        size = 13.dp
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Order #8492",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        Color(0xFFF59E0B).copy(alpha = 0.4f),
                        RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "PICKED UP",
                    color = Color(0xFFF59E0B),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // PROGRESS BAR at 70%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF3A3A3C))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight()
                    .background(Color(0xFFF59E0B))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // DRIVER INFO + PHONE BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Marco • Vespa Sprint • 4.9★",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Phone,
                    tint = Color.White,
                    size = 12.dp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DI-7: SPORTS SCORE — Dynamic Island-7.svg
// 144dp activity sheet: "UEFA Champions League" live match card with LIVE dot,
// Real Madrid 2-1 Man City, match details.
// Figma shows: league title, LIVE indicator, score, team names + scorers,
// and "Quarter-Final • 2nd Leg" footer.
// -----------------------------------------------------------------------------
@Composable
private fun SportsScoreCompactContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // HEADER: league + LIVE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UEFA Champions League",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "78'",
                    color = Color(0xFF10B981),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // SCORE ROW with teams
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Real Madrid",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vini Jr 34', Jude 67'",
                    color = Color(0xFF8E8E93),
                    fontSize = 8.5.sp
                )
            }
            Text(
                text = "2 - 1",
                color = Color(0xFFFBBF24),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Man City",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "De Bruyne 51'",
                    color = Color(0xFF8E8E93),
                    fontSize = 8.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FOOTER
        Text(
            text = "Quarter-Final • 2nd Leg (Agg: 4 - 4)",
            color = Color(0xFF8E8E93),
            fontSize = 9.sp
        )
    }
}

// -----------------------------------------------------------------------------
// DI-8: NAVIGATION ROUTE — Dynamic Island-8.svg
// Shared 36.67dp compact height: location arrow, "200m" distance,
// chevron, and route dots.
// Figma shows: LocationArrow glyph, "200m" label, ChevronRight, and three
// route-dot indicators.
// -----------------------------------------------------------------------------
@Composable
private fun NavigationRouteCompactContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.Location,
                tint = Color(0xFF3B82F6),
                size = 16.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "200m",
                color = Color(0xFF3B82F6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.ChevronRight,
                tint = Color(0xFF3B82F6),
                size = 14.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Three route dots
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.25f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AUDITED FIGMA PROTOTYPE COMPOSABLES
// -----------------------------------------------------------------------------

@Composable
private fun FaceTimeAudioExpandedContent() {
    OngoingCallIslandExpanded(
        name = "Tamia Castillo",
        label = "FaceTime Audio",
        duration = "02:45"
    )
}

@Composable
private fun AirplaneAlertContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB8B28).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Airplane, tint = Color(0xFFFB8B28), size = 22.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Turn Off Airplane Mode",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "to Access Data",
                    color = Color(0xFF818383),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF2C2C2D)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open Settings",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScreenMirroringContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF37A3DE).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.ScreenMirroring, tint = Color(0xFF37A3DE), size = 22.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Screen Mirroring",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "MacBook Pro",
                    color = Color(0xFF818383),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AppleIcon(glyph = AppleGlyph.AirPlay, tint = Color(0xFF37A3DE), size = 22.dp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF1A1C2D)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Stop Mirroring",
                color = Color(0xFF37A3DE),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MobileDataContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF37C058).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.PersonalHotspot, tint = Color(0xFF37C058), size = 22.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mobile Data",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Turn off Mobile Data to use Wi-Fi",
                    color = Color(0xFF818383),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(43.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF2C2C2D)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "OK", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(43.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF1A1C2D)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Settings", color = Color(0xFF37A3DE), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TransitRouteContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.TransitTrain, tint = Color.White, size = 22.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prague Main Train Station",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Arriving at Platform 3",
                    color = Color(0xFF818383),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF1D1011)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "End Route",
                color = Color(0xFFFA3532),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TurnByTurnNavigationContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Turn directions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppleIcon(glyph = AppleGlyph.NavigationLeft, tint = Color(0xFF34C759), size = 24.dp)
                AppleIcon(glyph = AppleGlyph.ChevronRight, tint = Color(0xFF8E8D94), size = 20.dp)
                AppleIcon(glyph = AppleGlyph.NavigationRight, tint = Color(0xFF8E8D94), size = 24.dp)
            }
            Text(
                text = "ETA 12m",
                color = Color(0xFF34C759),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // MIDDLE: Instruction
        Column {
            Text(
                text = "In 90 ft • Turn Left",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "North • San Francisco",
                color = Color(0xFF8E8D94),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // BOTTOM: Route strip preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1C1C1E))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Market St & 4th Ave",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "2.4 mi remaining",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp
                )
            }
        }
    }
}
