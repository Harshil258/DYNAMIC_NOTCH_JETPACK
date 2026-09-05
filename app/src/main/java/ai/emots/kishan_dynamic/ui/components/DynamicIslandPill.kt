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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.theme.AppIslandTokens
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.data.model.ActionAppShortcut
import ai.emots.kishan_dynamic.data.model.ActionCustomShortcut
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionUtilityAction
import ai.emots.kishan_dynamic.data.model.ActionContactShortcut
import ai.emots.kishan_dynamic.data.model.defaultActionSystemTiles
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy
import kotlin.math.roundToInt

enum class IslandDemoState {
    Idle,
    Minimal,
    MusicCompact,
    MusicExpanded,
    CallCompact,
    CallExpanded,
    CallSummaryExpanded,
    FaceTimeCallExpanded,
    SharedMediaCallExpanded,
    OngoingCallBanner,    // Custom ongoing-call banner with waveform bubble
    NotificationCompact,
    NotificationStacked,
    NotificationWithMusicCompact,
    NotificationExpanded,
    SilentModeCompact,
    SilentModeExpanded,
    RingerModeExpanded,
    VibrateModeCompact,
    NormalModeCompact,
    RingerVolumeCompact,
    MediaVolumeCompact,
    BluetoothConnected,
    BluetoothConnecting,
    ActionControlExpanded,
    PremiumExpiryExpanded,
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
    VoiceMemoExpanded,    // Dynamic Island-3.svg: Red waveform + Stop button
    ScreenRecordingExpanded, // Dynamic Island-4.svg: Red dot + timer + Stop button
    ShortcutExpanded,     // Dynamic Island-5.svg: Shortcuts spinner / checkmark
    FocusModeExpanded,    // Custom Focus / Do Not Disturb banner
    AirPodsConnected,     // Dynamic Island-2.svg: AirPods connected banner with 75% battery ring
    SatelliteConnected,   // Dynamic Island-7.svg: Satellite connection radar + "CONNECTED Keep pointing at Satellite"
    FindMyAlert,          // Dynamic Island-8.svg: Find My iPhone Alert glowing pulse
    MovedToIPhone,        // Dynamic Island-10.svg: AirPods Moved to iPhone alert with undo button
    VideoRemoteExpanded,  // Dynamic Island-16.svg: 15s skip video player
    AirDropActivity,      // Dynamic Island.svg: AirDrop transfer / receiving
    // Audited Figma prototypes
    TimerImage,           // Timer with cyan-blue progress ring
    NotificationImage,    // Notification card with thumbnail
    CallAvatars,          // Incoming call with avatars
    TransportActivity,    // Transport activity
    FlightTrackerExpanded,// Flight route tracker
    SubscriptionPricing,  // Pricing / mobile options
    MovieCard,            // Transit card
    ColorOptions,         // Route options
    AirplaneAlert,        // Dynamic Island-17.svg: Turn Off Airplane Mode
    ScreenMirroringAlert, // Dynamic Island-18.svg: Screen Mirroring MacBook Pro
    MobileDataAlert,      // Dynamic Island-19.svg: Mobile Data to use Wi-Fi
    TransitRouteAlert     // Dynamic Island-20.svg: Prague Main Train Station
}

/** Actions exposed by the reference islands' tappable controls. */
enum class IslandControlAction {
    TimerToggle,
    TimerCancel,
    MusicPrevious,
    MusicPlayPause,
    MusicNext,
    MusicAirPlay,
    VideoSkipBackward,
    VideoPlayPause,
    VideoSkipForward,
    CallAccept,
    CallDecline,
    CallToggleSpeaker,
    CallToggleMute,
    CallToggleVideo,
    CallToggleSharePlay,
    CallEnd,
    CallSummaryRedial,
    CallSummaryMessage,
    CallSummaryDismiss,
    SilentToggle,
    OpenSettings,
    ScreenMirroringStop,
    MobileDataOk,
    MobileDataSettings,
    TransitEndRoute,
    VoiceMemoStop,
    ScreenRecordingStop,
    ShortcutOpen,
    AirDropPause,
    AirPodsOpen,
    SatelliteMessage,
    LiveActivityOpen,
    MovedUndo,
    NavigationLeft,
    NavigationRight,
    NavigationStraight,
    PremiumWatchAd,
    PremiumBuy
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
    IslandDemoState.NotificationStacked,
    IslandDemoState.NotificationWithMusicCompact,
    IslandDemoState.TimerCompact,
    IslandDemoState.VibrateModeCompact,
    IslandDemoState.NormalModeCompact,
    IslandDemoState.RingerVolumeCompact,
    IslandDemoState.MediaVolumeCompact,
    IslandDemoState.BluetoothConnected,
    IslandDemoState.BluetoothConnecting,
    IslandDemoState.DeliveryCompact,
    IslandDemoState.FlightCompact,
    IslandDemoState.SportsCompact,
    IslandDemoState.NavigationCompact -> true
    else -> false
}

/** States whose compact presentation owns a detached companion bubble. */
internal fun IslandDemoState.usesCompanionBubble(): Boolean = this == IslandDemoState.Minimal ||
    this == IslandDemoState.MusicCompact ||
    this == IslandDemoState.CallCompact ||
    this == IslandDemoState.ChargingCompact ||
    this == IslandDemoState.LowBatteryCompact ||
    this == IslandDemoState.SilentModeCompact ||
    this == IslandDemoState.VibrateModeCompact ||
    this == IslandDemoState.NormalModeCompact ||
    this == IslandDemoState.NotificationStacked ||
    this == IslandDemoState.NotificationWithMusicCompact

/** One geometry resolver keeps state heights identical across every surface. */
internal fun IslandDemoState.resolvedHeight(tokens: AppIslandTokens): Dp {
    if (isCompactPresentation()) return tokens.compactHeight

    return when (this) {
        IslandDemoState.TimerExpanded -> 85.73.dp

        IslandDemoState.ActionControlExpanded -> 180.dp
        IslandDemoState.PremiumExpiryExpanded -> 112.dp

        IslandDemoState.NotificationExpanded,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.RingerModeExpanded,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.NotificationImage,
        IslandDemoState.CallAvatars,
        IslandDemoState.OngoingCallBanner,
        IslandDemoState.VoiceMemoExpanded,
        IslandDemoState.ScreenRecordingExpanded,
        IslandDemoState.FocusModeExpanded,
        IslandDemoState.AirDropActivity,
        IslandDemoState.AirPodsConnected,
        IslandDemoState.SatelliteConnected,
        IslandDemoState.FindMyAlert,
        IslandDemoState.MovedToIPhone -> tokens.standardExpandedHeight

        IslandDemoState.CallSummaryExpanded -> 112.dp

        IslandDemoState.ShortcutExpanded -> 85.08.dp

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
        IslandDemoState.FaceTimeCallExpanded -> 168.dp
        IslandDemoState.SharedMediaCallExpanded -> 172.64.dp
        IslandDemoState.MusicExpanded,
        IslandDemoState.VideoRemoteExpanded -> tokens.musicExpandedHeight
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
 *    - Reference 6-bar equalizer on the right
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicIslandPill(
    state: IslandDemoState,
    modifier: Modifier = Modifier,
    /** Width calibration only. Height remains tied to the shared iOS token. */
    horizontalScale: Float = 1f,
    onTap: () -> Unit = {},
    onCompanionTap: () -> Unit = onTap,
    onLongPress: () -> Unit = {},
    onSwipeDismiss: () -> Unit = {},
    onControlAction: (IslandControlAction) -> Unit = {},
    actionApps: List<ActionAppShortcut> = emptyList(),
    actionSystemTiles: List<ActionSystemTile> = defaultActionSystemTiles,
    actionSystemStates: Map<ActionSystemTile, Boolean?> = emptyMap(),
    actionContacts: List<ActionContactShortcut> = emptyList(),
    actionCustomActions: List<ActionCustomShortcut> = emptyList(),
    onActionAppClick: (String) -> Unit = {},
    onActionContactClick: (String) -> Unit = {},
    onCustomActionClick: (String) -> Unit = {},
    onSystemTileClick: (ActionSystemTile) -> Unit = {},
    notifications: List<NotificationInfo> = emptyList(),
    onNotificationAction: (NotificationInfo, NotificationActionInfo) -> Unit = { _, _ -> },
    onNotificationSelect: (String) -> Unit = {},
    onNotificationDismiss: (String) -> Unit = {},
    callContact: ContactInfo? = null,
    callSummary: CallRecord? = null,
    callDurationSeconds: Long = 0L,
    showCallDuration: Boolean = true,
    callIsDialing: Boolean = false,
    musicTrack: MusicTrack? = null,
    musicIsPlaying: Boolean = true,
    musicScrubberEnabled: Boolean = true,
    pulseScale: Float = 1.15f,
    pulseDurationMillis: Int = 450,
    hapticEnabled: Boolean = true,
    fastAnimations: Boolean = false,
    onMusicSeek: (Float) -> Unit = {},
    timerLabel: String = "03:35",
    timerProgress: Float = 0.72f,
    timerRunning: Boolean = true,
    premiumHoursRemaining: Int = 12,
    liveActivityTitle: String = "",
    liveActivitySubtitle: String = "",
    liveActivityProgress: Float? = null,
    ringerMode: RingerModeType = RingerModeType.SILENT,
    ringerVolumeLevel: Float = 0.5f,
    brightnessLevel: Float = 0.5f,
    mediaVolumeLevel: Float = 0.5f,
    mediaVolumeHudLevel: Float = 0.5f,
    bluetoothDeviceName: String = "Bluetooth device",
    bluetoothBatteryPercent: Int? = null,
    bluetoothIsConnecting: Boolean = false,
    batteryPercent: Int = 85,
    onBrightnessChange: (Float) -> Unit = {},
    onMediaVolumeChange: (Float) -> Unit = {},
    actionNotificationCount: Int = 0,
    onUtilityAction: (ActionUtilityAction) -> Unit = {},
    onOpenNotifications: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val islandView = androidx.compose.ui.platform.LocalView.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.pressSpring(),
        label = "island_press"
    )
    val islandStiffness = if (fastAnimations) 760f else 460f
    val contentDuration = if (fastAnimations) 130 else 200

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
    val isSplit = state.usesCompanionBubble()
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
        IslandDemoState.NotificationCompact,
        IslandDemoState.NotificationStacked -> 152.dp
        IslandDemoState.NotificationWithMusicCompact -> islandTokens.mediaCompactWidth
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact -> 172.dp
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting -> 196.dp
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
    val targetHeight = if (state == IslandDemoState.ActionControlExpanded) {
        var base = 180.dp
        if (actionCustomActions.isNotEmpty()) base += 52.dp
        if (actionApps.isNotEmpty()) base += 52.dp
        if (actionContacts.isNotEmpty()) base += 52.dp
        base
    } else {
        state.resolvedHeight(islandTokens)
    }

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
        IslandDemoState.NotificationStacked,
        IslandDemoState.NotificationWithMusicCompact,
        IslandDemoState.TimerCompact,
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact,
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting,
        IslandDemoState.DeliveryCompact,
        IslandDemoState.FlightCompact,
        IslandDemoState.SportsCompact,
        IslandDemoState.NavigationCompact -> RoundedCornerShape(percent = 50)

        IslandDemoState.NotificationExpanded,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.TimerExpanded,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.PremiumExpiryExpanded,
        IslandDemoState.NotificationImage,
        IslandDemoState.CallAvatars,
        IslandDemoState.OngoingCallBanner,
        IslandDemoState.VoiceMemoExpanded,
        IslandDemoState.ScreenRecordingExpanded,
        IslandDemoState.FocusModeExpanded,
        IslandDemoState.ShortcutExpanded,
        IslandDemoState.AirDropActivity,
        IslandDemoState.AirPodsConnected,
        IslandDemoState.SatelliteConnected,
        IslandDemoState.FindMyAlert,
        IslandDemoState.MovedToIPhone -> RoundedCornerShape(percent = 50)

        else -> RoundedCornerShape(islandTokens.expandedCorner)
    }

    // Apple Liquid Morphing Springs
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.73f, stiffness = islandStiffness),
        label = "island_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(dampingRatio = 0.73f, stiffness = islandStiffness),
        label = "island_height"
    )

    // Breathing Ambient Specular Aura (iOS Dynamic Island glow)
    val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                if (fastAnimations) 1400 else 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    val auraColor = when (state) {
        IslandDemoState.Idle -> IslandColors.Blue
        IslandDemoState.CallCompact,
        IslandDemoState.CallExpanded,
        IslandDemoState.FaceTimeCallExpanded,
        IslandDemoState.SharedMediaCallExpanded,
        IslandDemoState.OngoingCallBanner -> IslandColors.CapsuleGreen
        IslandDemoState.ChargingCompact, IslandDemoState.ChargingExpanded -> IslandColors.Green
        IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded,
        IslandDemoState.VoiceMemoExpanded, IslandDemoState.ScreenRecordingExpanded -> IslandColors.CapsuleRed
        IslandDemoState.MusicCompact, IslandDemoState.MusicExpanded, IslandDemoState.VideoRemoteExpanded -> IslandColors.Purple
        IslandDemoState.SilentModeCompact, IslandDemoState.SilentModeExpanded -> IslandColors.Red
        IslandDemoState.NotificationCompact,
        IslandDemoState.NotificationStacked,
        IslandDemoState.NotificationExpanded -> Color(0xFF25D366)
        IslandDemoState.NotificationWithMusicCompact -> IslandColors.Purple
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact,
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting -> IslandColors.CapsuleCyan
        IslandDemoState.ActionControlExpanded -> IslandColors.Indigo
        IslandDemoState.PremiumExpiryExpanded -> IslandColors.TimerCyan
        IslandDemoState.TimerCompact, IslandDemoState.TimerExpanded -> IslandColors.TimerBlue
        IslandDemoState.DeliveryCompact, IslandDemoState.DeliveryExpanded -> IslandColors.Orange
        IslandDemoState.FlightCompact, IslandDemoState.FlightExpanded,
        IslandDemoState.ShortcutExpanded, IslandDemoState.AirDropActivity -> IslandColors.CapsuleCyan
        IslandDemoState.SportsCompact, IslandDemoState.SportsExpanded -> IslandColors.Purple
        IslandDemoState.FocusModeExpanded -> IslandColors.Indigo
        IslandDemoState.TimerImage -> IslandColors.TimerCyan
        IslandDemoState.NotificationImage -> IslandColors.Gray
        IslandDemoState.CallAvatars -> IslandColors.CapsuleGreen
        IslandDemoState.TransportActivity, IslandDemoState.AirplaneAlert -> IslandColors.CapsuleOrange
        IslandDemoState.FlightTrackerExpanded, IslandDemoState.ScreenMirroringAlert -> IslandColors.CapsuleCyan
        IslandDemoState.SubscriptionPricing, IslandDemoState.MobileDataAlert -> IslandColors.CapsuleGreen
        IslandDemoState.MovieCard, IslandDemoState.TransitRouteAlert -> IslandColors.CapsuleRed
        else -> IslandColors.Indigo
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
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (hapticEnabled) {
                            islandView.performHapticFeedback(
                                android.view.HapticFeedbackConstants.KEYBOARD_TAP
                            )
                        }
                        onTap()
                    },
                    onLongClick = {
                        if (hapticEnabled) {
                            islandView.performHapticFeedback(
                                android.view.HapticFeedbackConstants.LONG_PRESS
                            )
                        }
                        onLongPress()
                    }
                )
                .pointerInput(onSwipeDismiss) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            totalDrag += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            if (totalDrag < -32f) {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.KEYBOARD_TAP
                                    )
                                }
                                onSwipeDismiss()
                            }
                        }
                    )
                },
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
                    .background(Color.Black)
                    .border(
                        width = 0.75.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x38FFFFFF), Color(0x06FFFFFF))
                        ),
                        shape = cornerRadius
                    )
                    .padding(
                        horizontal = when (state) {
                            IslandDemoState.MusicExpanded,
                            IslandDemoState.VideoRemoteExpanded,
                            IslandDemoState.CallExpanded,
                            IslandDemoState.FaceTimeCallExpanded,
                            IslandDemoState.SharedMediaCallExpanded,
                            IslandDemoState.OngoingCallBanner,
                            IslandDemoState.CallAvatars,
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.SilentModeExpanded,
                            IslandDemoState.ActionControlExpanded,
                            IslandDemoState.PremiumExpiryExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded,
                            IslandDemoState.LowBatteryExpanded,
                            IslandDemoState.VoiceMemoExpanded,
                            IslandDemoState.ScreenRecordingExpanded,
                            IslandDemoState.ShortcutExpanded,
                            IslandDemoState.FocusModeExpanded,
                            IslandDemoState.AirDropActivity,
                            IslandDemoState.AirPodsConnected,
                            IslandDemoState.SatelliteConnected,
                            IslandDemoState.FindMyAlert,
                            IslandDemoState.MovedToIPhone,
                            IslandDemoState.AirplaneAlert,
                            IslandDemoState.ScreenMirroringAlert,
                            IslandDemoState.MobileDataAlert,
                            IslandDemoState.TransitRouteAlert,
                            IslandDemoState.ColorOptions -> 0.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded,
                            IslandDemoState.TransportActivity,
                            IslandDemoState.FlightTrackerExpanded,
                            IslandDemoState.SubscriptionPricing,
                            IslandDemoState.MovieCard -> 16.dp
                            else -> 11.dp
                        },
                        vertical = when (state) {
                            IslandDemoState.MusicExpanded,
                            IslandDemoState.VideoRemoteExpanded,
                            IslandDemoState.CallExpanded,
                            IslandDemoState.FaceTimeCallExpanded,
                            IslandDemoState.SharedMediaCallExpanded,
                            IslandDemoState.OngoingCallBanner,
                            IslandDemoState.CallAvatars,
                            IslandDemoState.NotificationExpanded,
                            IslandDemoState.SilentModeExpanded,
                            IslandDemoState.ActionControlExpanded,
                            IslandDemoState.PremiumExpiryExpanded,
                            IslandDemoState.TimerExpanded,
                            IslandDemoState.ChargingExpanded,
                            IslandDemoState.LowBatteryExpanded,
                            IslandDemoState.VoiceMemoExpanded,
                            IslandDemoState.ScreenRecordingExpanded,
                            IslandDemoState.ShortcutExpanded,
                            IslandDemoState.FocusModeExpanded,
                            IslandDemoState.AirDropActivity,
                            IslandDemoState.AirPodsConnected,
                            IslandDemoState.SatelliteConnected,
                            IslandDemoState.FindMyAlert,
                            IslandDemoState.MovedToIPhone,
                            IslandDemoState.AirplaneAlert,
                            IslandDemoState.ScreenMirroringAlert,
                            IslandDemoState.MobileDataAlert,
                            IslandDemoState.TransitRouteAlert,
                            IslandDemoState.ColorOptions -> 0.dp
                            IslandDemoState.DeliveryExpanded,
                            IslandDemoState.FlightExpanded,
                            IslandDemoState.SportsExpanded,
                            IslandDemoState.TransportActivity,
                            IslandDemoState.FlightTrackerExpanded,
                            IslandDemoState.SubscriptionPricing,
                            IslandDemoState.MovieCard -> 14.dp
                            else -> 0.dp
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(tween(contentDuration, easing = ai.emots.kishan_dynamic.ui.motion.AppMotion.EaseIslandContent)) togetherWith
                            fadeOut(tween(contentDuration, easing = ai.emots.kishan_dynamic.ui.motion.AppMotion.EaseIslandContent))
                    },
                    label = "island_content_morph"
                ) { targetState ->
                    when (targetState) {
                        IslandDemoState.Idle -> IdleCutoutContent()
                        IslandDemoState.Minimal -> MinimalPillContent()
                        IslandDemoState.MusicCompact -> MusicCompactContent(musicTrack, musicIsPlaying)
                        IslandDemoState.MusicExpanded -> MusicExpandedContent(
                            track = musicTrack,
                            isPlaying = musicIsPlaying,
                            onControlAction = onControlAction,
                            onSeek = onMusicSeek,
                            showScrubber = musicScrubberEnabled
                        )
                        IslandDemoState.VideoRemoteExpanded -> VideoRemoteExpandedContent(onControlAction)
                        IslandDemoState.CallCompact -> CallCompactContent(
                            durationSeconds = callDurationSeconds,
                            showDuration = showCallDuration,
                            isDialing = callIsDialing
                        )
                        IslandDemoState.CallExpanded -> FaceTimeAudioExpandedContent(
                            variant = OngoingCallVariant.Standard,
                            onControlAction = onControlAction,
                            name = callContact?.name ?: "Tamia Castillo",
                            duration = if (showCallDuration) formatCallDuration(callDurationSeconds) else "On call",
                            avatarUri = callContact?.avatarUri,
                            isDialing = callIsDialing
                        )
                        IslandDemoState.CallSummaryExpanded -> CallSummaryExpandedContent(
                            record = callSummary,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.FaceTimeCallExpanded -> FaceTimeAudioExpandedContent(
                            variant = OngoingCallVariant.FaceTime,
                            onControlAction = onControlAction,
                            name = callContact?.name ?: "Tamia Castillo",
                            duration = if (showCallDuration) formatCallDuration(callDurationSeconds) else "On call",
                            avatarUri = callContact?.avatarUri,
                            isDialing = callIsDialing
                        )
                        IslandDemoState.SharedMediaCallExpanded -> FaceTimeAudioExpandedContent(
                            variant = OngoingCallVariant.SharedMedia,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.OngoingCallBanner -> OngoingCallBannerContent()
                        IslandDemoState.NotificationCompact -> NotificationCompactContent(notifications.firstOrNull())
                        IslandDemoState.NotificationStacked -> NotificationStackedMainContent(notifications)
                        IslandDemoState.NotificationWithMusicCompact -> NotificationWithMusicCompactContent(
                            notifications = notifications,
                            track = musicTrack,
                            isPlaying = musicIsPlaying
                        )
                        IslandDemoState.NotificationExpanded -> NotificationExpandedContent(
                            notifications = notifications,
                            onNotificationAction = onNotificationAction,
                            onNotificationSelect = onNotificationSelect,
                            onNotificationDismiss = onNotificationDismiss
                        )
                        IslandDemoState.SilentModeCompact -> SilentModeCompactContent()
                        IslandDemoState.SilentModeExpanded -> SilentModeExpandedContent(onControlAction)
                        IslandDemoState.RingerModeExpanded -> RingerModeExpandedContent(ringerMode, onControlAction)
                        IslandDemoState.VibrateModeCompact -> VibrateModeCompactContent()
                        IslandDemoState.NormalModeCompact -> NormalModeCompactContent()
                        IslandDemoState.RingerVolumeCompact -> RingerVolumeCompactContent(
                            level = ringerVolumeLevel,
                            pulseScale = pulseScale,
                            pulseDurationMillis = pulseDurationMillis
                        )
                        IslandDemoState.MediaVolumeCompact -> MediaVolumeCompactContent(
                            level = mediaVolumeHudLevel,
                            pulseScale = pulseScale,
                            pulseDurationMillis = pulseDurationMillis
                        )
                        IslandDemoState.BluetoothConnected,
                        IslandDemoState.BluetoothConnecting -> BluetoothConnectedContent(
                            deviceName = bluetoothDeviceName,
                            batteryPercent = bluetoothBatteryPercent,
                            isConnecting = bluetoothIsConnecting
                        )
                        IslandDemoState.ActionControlExpanded -> ActionControlExpandedContent(
                            systemTiles = actionSystemTiles,
                            systemStates = actionSystemStates,
                            apps = actionApps,
                            contacts = actionContacts,
                            customActions = actionCustomActions,
                            onAppClick = onActionAppClick,
                            onContactClick = onActionContactClick,
                            onCustomActionClick = onCustomActionClick,
                            onSystemTileClick = onSystemTileClick,
                            brightnessLevel = brightnessLevel,
                            mediaVolumeLevel = mediaVolumeLevel,
                            onBrightnessChange = onBrightnessChange,
                            onMediaVolumeChange = onMediaVolumeChange,
                            notificationCount = actionNotificationCount,
                            onUtilityAction = onUtilityAction,
                            onOpenNotifications = onOpenNotifications
                        )
                        IslandDemoState.PremiumExpiryExpanded -> PremiumExpiryExpandedContent(
                            hoursRemaining = premiumHoursRemaining,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.ChargingCompact -> ChargingIslandMain(
                            percentage = batteryPercent,
                            isLowBattery = false
                        )
                        IslandDemoState.ChargingExpanded -> ChargingExpandedContent()
                        IslandDemoState.LowBatteryCompact -> ChargingIslandMain(
                            percentage = batteryPercent,
                            isLowBattery = true
                        )
                        IslandDemoState.LowBatteryExpanded -> LowBatteryExpandedContent()
                        IslandDemoState.TimerCompact -> TimerCompactContent(timerLabel, timerProgress)
                        IslandDemoState.TimerExpanded -> TimerExpandedContent(timerLabel, timerRunning, onControlAction)
                        IslandDemoState.VoiceMemoExpanded -> VoiceMemoExpandedContent(onControlAction)
                        IslandDemoState.ScreenRecordingExpanded -> ScreenRecordingExpandedContent(onControlAction)
                        IslandDemoState.ShortcutExpanded -> ShortcutExpandedContent(onControlAction)
                        IslandDemoState.FocusModeExpanded -> FocusModeExpandedContent()
                        IslandDemoState.AirDropActivity -> AirDropActivityContent(onControlAction)
                        IslandDemoState.AirPodsConnected -> AirPodsConnectedContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            batteryProgress = liveActivityProgress,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.SatelliteConnected -> SatelliteConnectionContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.FindMyAlert -> FindMyAlertContent(
                            title = liveActivityTitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.MovedToIPhone -> MovedToIPhoneContent(
                            title = liveActivityTitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.DeliveryCompact -> DeliveryCompactContent()
                        IslandDemoState.DeliveryExpanded -> DeliveryExpandedContent()
                        IslandDemoState.FlightCompact -> FlightCompactContent(liveActivityTitle, liveActivitySubtitle)
                        IslandDemoState.FlightExpanded -> FlightExpandedContent(liveActivityTitle, liveActivitySubtitle, liveActivityProgress)
                        IslandDemoState.SportsCompact -> SportsCompactContent(liveActivityTitle, liveActivitySubtitle)
                        IslandDemoState.SportsExpanded -> SportsExpandedContent(liveActivityTitle, liveActivitySubtitle)
                        IslandDemoState.NavigationCompact -> NavigationCompactContent()
                        IslandDemoState.TimerImage -> TimerWithImageCompactContent()
                        IslandDemoState.NotificationImage -> NotificationImageCompactContent()
                        IslandDemoState.CallAvatars -> CallExpandedContent(onControlAction, callContact)
                        IslandDemoState.TransportActivity,
                        IslandDemoState.AirplaneAlert -> AirplaneAlertContent(onControlAction)
                        IslandDemoState.FlightTrackerExpanded,
                        IslandDemoState.ScreenMirroringAlert -> ScreenMirroringContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.SubscriptionPricing,
                        IslandDemoState.MobileDataAlert -> MobileDataContent(onControlAction)
                        IslandDemoState.MovieCard,
                        IslandDemoState.TransitRouteAlert -> TransitRouteContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.ColorOptions -> TurnByTurnNavigationContent(onControlAction)
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
                            .clickable {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.KEYBOARD_TAP
                                    )
                                }
                                onCompanionTap()
                            }
                            .background(Color.Black)
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
                                AppleIcon(glyph = AppleGlyph.Phone, tint = IslandColors.CapsuleGreen, size = 15.dp)
                            }
                            IslandDemoState.TimerCompact, IslandDemoState.Minimal -> {
                                TimerProgressRing(progress = 0.72f, size = 21.dp, strokeWidth = 2.5.dp)
                            }
                            IslandDemoState.NotificationStacked -> {
                                NotificationStackedSideContent(notifications)
                            }
                            IslandDemoState.NotificationWithMusicCompact -> {
                                NotificationWithMusicSideContent(
                                    notifications = notifications,
                                    track = musicTrack,
                                    isPlaying = musicIsPlaying
                                )
                            }
                            IslandDemoState.ChargingCompact,
                            IslandDemoState.LowBatteryCompact -> {
                                ChargingIslandSide(percentage = batteryPercent)
                            }
                            IslandDemoState.SilentModeCompact,
                            IslandDemoState.VibrateModeCompact,
                            IslandDemoState.NormalModeCompact -> {
                                RingerModeSideContent(ringerMode)
                            }
                            IslandDemoState.MusicCompact -> {
                                MusicCompactSideContent(
                                    track = musicTrack,
                                    isPlaying = musicIsPlaying
                                )
                            }
                            else -> {
                                AppleIcon(glyph = AppleGlyph.Timer, tint = IslandColors.TimerBlue, size = 15.dp)
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
            .background(IslandColors.Green.copy(alpha = pulse))
    )
}

// -----------------------------------------------------------------------------
// 1. MUSIC (APPLE MUSIC / SPOTIFY) - MATCHING Compact.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun MusicCompactContent(track: MusicTrack?, isPlaying: Boolean) {
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
                            listOf(IslandColors.Purple, IslandColors.Indigo, IslandColors.Cyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (track?.albumArtUri != null) {
                    UriArtwork(
                        uri = track.albumArtUri,
                        fallbackRes = ai.emots.kishan_dynamic.R.drawable.music_album_art,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 10.dp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Track title (compact shows truncated title)
            Text(
                text = track?.title ?: "Now playing",
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 70.dp)
            )
        }

        // Reference equalizer (geometry measured off Minimal.svg / DI-11)
        LiveEqualizerMini(animated = isPlaying)
    }
}

@Composable
private fun MusicCompactSideContent(track: MusicTrack?, isPlaying: Boolean) {
    if (isPlaying) {
        LiveEqualizerMini(animated = true)
    } else {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(IslandColors.Purple, IslandColors.Indigo, IslandColors.CapsuleCyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (track?.albumArtUri != null) {
                UriArtwork(
                    uri = track.albumArtUri,
                    fallbackRes = ai.emots.kishan_dynamic.R.drawable.music_album_art,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 15.dp)
            }
        }
    }
}

@Composable
private fun MusicExpandedContent(
    track: MusicTrack?,
    isPlaying: Boolean,
    onControlAction: (IslandControlAction) -> Unit = {},
    onSeek: (Float) -> Unit = {},
    showScrubber: Boolean = true
) {
    // The supplied export is the paused/player-ready state; the play target
    // becomes a pause target only after the user activates it.
    MusicPlayerIsland(
        title = track?.title ?: "Now playing",
        artist = track?.artist ?: "",
        albumArtUri = track?.albumArtUri,
        isPlaying = isPlaying,
        currentTime = formatTrackTime(track?.positionMs ?: 0L),
        remainingTime = "-${formatTrackTime(((track?.durationMs ?: 0L) - (track?.positionMs ?: 0L)).coerceAtLeast(0L))}",
        initialProgress = if ((track?.durationMs ?: 0L) > 0L) {
            ((track?.positionMs ?: 0L).toFloat() / (track?.durationMs ?: 1L).toFloat()).coerceIn(0f, 1f)
        } else 0f,
        onPlayPause = { onControlAction(IslandControlAction.MusicPlayPause) },
        onPrevious = { onControlAction(IslandControlAction.MusicPrevious) },
        onNext = { onControlAction(IslandControlAction.MusicNext) },
        onAirPlay = { onControlAction(IslandControlAction.MusicAirPlay) },
        onSeek = onSeek,
        showScrubber = showScrubber
    )
}

private fun formatTrackTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

// -----------------------------------------------------------------------------
// 2. INCOMING CALL - MATCHING Dynamic Island-6.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun CallCompactContent(
    durationSeconds: Long,
    showDuration: Boolean,
    isDialing: Boolean
) {
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(IslandColors.CapsuleGreen),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 11.dp)
            }
        },
        trailing = {
            Text(
                text = when {
                    isDialing -> "Calling…"
                    showDuration -> formatCallDuration(durationSeconds)
                    else -> "On call"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.CapsuleGreen,
                fontFamily = FontFamily.Monospace
            )
        }
    )
}

@Composable
private fun CallExpandedContent(
    onControlAction: (IslandControlAction) -> Unit = {},
    contact: ContactInfo? = null
) {
    IncomingCallIsland(
        name = contact?.name ?: "Tamia Castillo",
        label = "Mobile",
        avatarUri = contact?.avatarUri,
        onAccept = { onControlAction(IslandControlAction.CallAccept) },
        onDecline = { onControlAction(IslandControlAction.CallDecline) }
    )
}

// -----------------------------------------------------------------------------
// 3. NOTIFICATION ALERT (WHATSAPP / MESSAGES)
// -----------------------------------------------------------------------------
@Composable
private fun NotificationCompactContent(notification: NotificationInfo?) {
    CompactIslandLayout(
        leading = {
            PackageIcon(
                packageName = notification?.packageName,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                contentDescription = "${notification?.appName ?: "Notification"} icon"
            )
        },
        trailing = {
            Text(
                text = notification?.let { it.title.ifBlank { it.appName } } ?: "Notification",
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

/**
 * Compact multi-notification presentation inspired by the reference app's
 * stacked state: keep one readable primary alert in the main capsule and move
 * the queue affordance into the detached activity bubble.
 */
@Composable
private fun NotificationStackedMainContent(notifications: List<NotificationInfo>) {
    val primary = notifications.firstOrNull(::isStackPriority) ?: notifications.firstOrNull()
    if (primary == null) return

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageIcon(
            packageName = primary.packageName,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape),
            contentDescription = "${primary.appName} icon"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = primary.title.ifBlank { primary.appName },
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (primary.showChronometer && primary.chronometerBaseElapsedRealtime > 0L) {
                NotificationChronometerText(
                    baseElapsedRealtime = primary.chronometerBaseElapsedRealtime,
                    countDown = primary.chronometerCountDown,
                    fontSize = 9.sp,
                    color = Color(0xFFFF9F0A)
                )
            } else if (primary.isOngoing && primary.progressMax > 0) {
                Text(
                    text = "${(primary.progress * 100 / primary.progressMax).coerceIn(0, 100)}%",
                    color = Color(0xFF64B5F6),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NotificationStackedSideContent(notifications: List<NotificationInfo>) {
    val secondary = notifications.drop(1).firstOrNull(::isStackPriority)
    if (secondary != null) {
        PackageIcon(
            packageName = secondary.packageName,
            modifier = Modifier
                .size(21.dp)
                .clip(CircleShape),
            contentDescription = "${secondary.appName} icon"
        )
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3C)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+${notifications.size}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun isStackPriority(notification: NotificationInfo): Boolean =
    NotificationPriorityPolicy.isPriority(notification)

@Composable
private fun NotificationWithMusicCompactContent(
    notifications: List<NotificationInfo>,
    track: MusicTrack?,
    isPlaying: Boolean
) {
    val notification = notifications.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IslandColors.Purple, IslandColors.Indigo, IslandColors.CapsuleCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (track?.albumArtUri != null) {
                    UriArtwork(
                        uri = track.albumArtUri,
                        fallbackRes = ai.emots.kishan_dynamic.R.drawable.music_album_art,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AppleIcon(glyph = AppleGlyph.Waveform, tint = Color.White, size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.width(9.dp))
            Box(modifier = Modifier.weight(1f)) {
                LiveEqualizerMini(animated = true)
            }
        } else {
            if (notification != null) {
                PackageIcon(
                    packageName = notification.packageName,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentDescription = "${notification.appName} icon"
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = notifications.size.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C))
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
private fun NotificationWithMusicSideContent(
    notifications: List<NotificationInfo>,
    track: MusicTrack?,
    isPlaying: Boolean
) {
    if (isPlaying) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3C)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notifications.size.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(IslandColors.Purple, IslandColors.CapsuleCyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (track?.albumArtUri != null) {
                UriArtwork(
                    uri = track.albumArtUri,
                    fallbackRes = ai.emots.kishan_dynamic.R.drawable.music_album_art,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 15.dp)
            }
        }
    }
}

@Composable
private fun NotificationExpandedContent(
    notifications: List<NotificationInfo>,
    onNotificationAction: (NotificationInfo, NotificationActionInfo) -> Unit,
    onNotificationSelect: (String) -> Unit,
    onNotificationDismiss: (String) -> Unit
) {
    var activeIndex by androidx.compose.runtime.remember(notifications) {
        androidx.compose.runtime.mutableIntStateOf(0)
    }
    if (notifications.isEmpty()) return
    val currentIndex = activeIndex.coerceIn(0, notifications.lastIndex)
    val notification = notifications[currentIndex]
    NotificationIslandExpanded(
        appName = notification.appName,
        appPackageName = notification.packageName,
        imagePath = notification.imagePath,
        sender = notification.title.ifBlank { notification.appName },
        message = listOfNotNull(
            notification.inboxLines.takeIf { it.isNotEmpty() }?.joinToString("\n"),
            notification.subText,
            notification.expandedText.takeIf { it.isNotBlank() }
        ).distinct().joinToString(" · "),
        actionLabel = notification.actions.firstOrNull()?.label ?: "Open",
        onAction = { notification.actions.firstOrNull()?.let { onNotificationAction(notification, it) } },
        actions = notification.actions,
        onActionSelected = { action -> onNotificationAction(notification, action) },
        pagerLabel = if (notifications.size > 1) "${currentIndex + 1}/${notifications.size}" else null,
        onPrevious = if (currentIndex > 0) {
            {
                activeIndex = currentIndex - 1
                onNotificationSelect(notifications[currentIndex - 1].id)
            }
        } else null,
        onNext = if (currentIndex < notifications.lastIndex) {
            {
                activeIndex = currentIndex + 1
                onNotificationSelect(notifications[currentIndex + 1].id)
            }
        } else null,
        onDismiss = { onNotificationDismiss(notification.id) },
                progress = if (notification.progressMax > 0) {
                    notification.progress.toFloat() / notification.progressMax.toFloat()
                } else null,
                isProgressIndeterminate = notification.isProgressIndeterminate,
                showChronometer = notification.showChronometer,
                chronometerBaseElapsedRealtime = notification.chronometerBaseElapsedRealtime,
                chronometerCountDown = notification.chronometerCountDown
            )
}

// -----------------------------------------------------------------------------
// 3B. SILENT MODE ALERT - MATCHING Dynamic Island-9.svg EXACTLY!
// -----------------------------------------------------------------------------
@Composable
private fun SilentModeCompactContent() {
    RingerModeCompactContent(RingerModeType.SILENT)
}

@Composable
private fun SilentModeExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    RingerModeIslandExpanded(
        mode = RingerModeType.SILENT,
        onToggle = { onControlAction(IslandControlAction.SilentToggle) }
    )
}

@Composable
private fun RingerModeExpandedContent(
    mode: RingerModeType,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    RingerModeIslandExpanded(
        mode = mode,
        onToggle = { onControlAction(IslandControlAction.SilentToggle) }
    )
}

// -----------------------------------------------------------------------------
// REFERENCE SYSTEM INDICATORS
// -----------------------------------------------------------------------------

@Composable
private fun VibrateModeCompactContent() {
    RingerModeCompactContent(RingerModeType.VIBRATE)
}

@Composable
private fun NormalModeCompactContent() {
    RingerModeCompactContent(RingerModeType.NORMAL)
}

@Composable
private fun RingerModeCompactContent(mode: RingerModeType) {
    val tint = when (mode) {
        RingerModeType.SILENT -> IslandColors.CapsuleRed
        RingerModeType.VIBRATE -> IslandColors.Orange
        RingerModeType.NORMAL -> Color.White
    }
    val label = when (mode) {
        RingerModeType.SILENT -> "Silent"
        RingerModeType.VIBRATE -> "Vibrate"
        RingerModeType.NORMAL -> "Ringer on"
    }

    CompactIslandLayout(
        leading = { RingerModeGlyph(mode = mode, tint = tint, glyphSize = 16.dp) },
        trailing = {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    )
}

/**
 * Detached companion for the compact ringer HUD. The reference treats the
 * mode glyph as a second island, while the main capsule retains the readable
 * mode label. Keep the mapping state-driven so a live AudioManager update
 * cannot leave the companion on a stale mode.
 */
@Composable
private fun RingerModeSideContent(mode: RingerModeType) {
    val tint = when (mode) {
        RingerModeType.SILENT -> IslandColors.Red
        RingerModeType.VIBRATE -> IslandColors.Orange
        RingerModeType.NORMAL -> Color.White
    }

    RingerModeGlyph(mode = mode, tint = tint, glyphSize = 19.dp)
}

@Composable
private fun RingerVolumeCompactContent(
    level: Float,
    pulseScale: Float,
    pulseDurationMillis: Int
) {
    VolumeIndicatorContent(
        glyph = AppleGlyph.Bell,
        label = "Ringer",
        tint = IslandColors.CapsuleCyan,
        level = level,
        pulseScale = pulseScale,
        pulseDurationMillis = pulseDurationMillis
    )
}

@Composable
private fun MediaVolumeCompactContent(
    level: Float,
    pulseScale: Float,
    pulseDurationMillis: Int
) {
    VolumeIndicatorContent(
        glyph = AppleGlyph.Speaker,
        label = "Volume",
        tint = IslandColors.CapsuleCyan,
        level = level,
        pulseScale = pulseScale,
        pulseDurationMillis = pulseDurationMillis
    )
}

@Composable
private fun SystemIndicatorContent(
    glyph: AppleGlyph,
    label: String,
    tint: Color
) {
    CompactIslandLayout(
        leading = { AppleIcon(glyph = glyph, tint = tint, size = 16.dp) },
        trailing = {
            Text(
                text = label,
                color = tint,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

@Composable
private fun VolumeIndicatorContent(
    glyph: AppleGlyph,
    label: String,
    tint: Color,
    level: Float,
    pulseScale: Float,
    pulseDurationMillis: Int
) {
    val pulseTransition = rememberInfiniteTransition(label = "volume_pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseScale.coerceIn(1f, 1.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMillis.coerceIn(100, 1000)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "volume_pulse_scale"
    )
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        AppleIcon(
            glyph = glyph,
            tint = tint,
            size = 16.dp,
            modifier = Modifier.scale(pulse)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "${(level * 100).toInt()}%", color = tint, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(IslandColors.Gray5)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(level.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(tint.copy(alpha = 0.7f), tint)))
                )
            }
        }
    }
}

@Composable
private fun BluetoothConnectedContent(
    deviceName: String,
    batteryPercent: Int?,
    isConnecting: Boolean
) {
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(IslandColors.CapsuleCyan, IslandColors.Blue))),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Bluetooth, tint = Color.White, size = 13.dp)
            }
        },
        trailing = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = deviceName.ifBlank { "Bluetooth device" },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        isConnecting -> "Connecting…"
                        batteryPercent != null -> "Connected · ${batteryPercent.coerceIn(0, 100)}%"
                        else -> "Connected"
                    },
                    color = IslandColors.CapsuleCyan,
                    fontSize = 9.5.sp
                )
            }
        }
    )
}

@Composable
private fun ActionControlExpandedContent(
    systemTiles: List<ActionSystemTile>,
    systemStates: Map<ActionSystemTile, Boolean?>,
    apps: List<ActionAppShortcut>,
    contacts: List<ActionContactShortcut>,
    customActions: List<ActionCustomShortcut>,
    onAppClick: (String) -> Unit,
    onContactClick: (String) -> Unit,
    onCustomActionClick: (String) -> Unit,
    onSystemTileClick: (ActionSystemTile) -> Unit,
    brightnessLevel: Float,
    mediaVolumeLevel: Float,
    onBrightnessChange: (Float) -> Unit,
    onMediaVolumeChange: (Float) -> Unit,
    notificationCount: Int,
    onUtilityAction: (ActionUtilityAction) -> Unit,
    onOpenNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Quick controls", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = "Your essentials, one tap away", color = IslandColors.TextSecondary, fontSize = 10.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(IslandColors.CapsuleRed.copy(alpha = 0.22f))
                            .clickable(onClick = onOpenNotifications)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$notificationCount ${if (notificationCount == 1) "alert" else "alerts"}",
                            color = IslandColors.CapsuleRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                ActionUtilityButton(
                    glyph = AppleGlyph.Lock,
                    onClick = { onUtilityAction(ActionUtilityAction.LOCK) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Settings,
                    onClick = { onUtilityAction(ActionUtilityAction.SETTINGS) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Camera,
                    onClick = { onUtilityAction(ActionUtilityAction.CAMERA) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Expand,
                    onClick = { onUtilityAction(ActionUtilityAction.EDIT) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            systemTiles.forEach { tile ->
                QuickControlChip(
                    glyph = tile.toGlyph(),
                    label = tile.title,
                    state = systemStates[tile],
                    modifier = Modifier.width(58.dp),
                    onClick = { onSystemTileClick(tile) }
                )
            }
        }
        if (customActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customActions.forEach { action ->
                    ActionCustomChip(
                        action = action,
                        modifier = Modifier.width(58.dp),
                        onClick = { onCustomActionClick(action.actionId) }
                    )
                }
            }
        }
        if (apps.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                apps.take(4).forEach { app ->
                    ActionAppChip(
                        app = app,
                        modifier = Modifier.weight(1f),
                        onClick = { onAppClick(app.packageName) }
                    )
                }
                repeat((4 - apps.take(4).size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (contacts.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                contacts.take(4).forEach { contact ->
                    ActionContactChip(
                        contact = contact,
                        modifier = Modifier.weight(1f),
                        onClick = { onContactClick(contact.phoneNumber) }
                    )
                }
                repeat((4 - contacts.take(4).size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionUtilitySlider(
                glyph = AppleGlyph.Sparkles,
                value = brightnessLevel,
                onValueChange = onBrightnessChange,
                activeColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            ActionUtilitySlider(
                glyph = AppleGlyph.Speaker,
                value = mediaVolumeLevel,
                onValueChange = onMediaVolumeChange,
                activeColor = IslandColors.CapsuleCyan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionUtilityButton(
    glyph: AppleGlyph,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFF25262B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(glyph = glyph, tint = Color.White, size = 12.dp)
    }
}

@Composable
private fun ActionUtilitySlider(
    glyph: AppleGlyph,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val progress = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(0xFF222326))
            .border(0.75.dp, Color(0x22FFFFFF), RoundedCornerShape(percent = 50))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValueChange(newProgress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val newProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(newProgress)
                }
            }
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(activeColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val iconTint = if (progress > 0.16f) Color(0xFF141416) else Color.White
            AppleIcon(glyph = glyph, tint = iconTint, size = 15.dp)

            val textTint = if (progress > 0.82f) Color(0xFF141416) else Color(0xB3FFFFFF)
            Text(
                text = "${(progress * 100).toInt()}%",
                color = textTint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun ActionSystemTile.toGlyph(): AppleGlyph = when (this) {
    ActionSystemTile.WIFI -> AppleGlyph.Wifi
    ActionSystemTile.BLUETOOTH -> AppleGlyph.Bluetooth
    ActionSystemTile.MOBILE_DATA -> AppleGlyph.Globe
    ActionSystemTile.TORCH -> AppleGlyph.Torch
    ActionSystemTile.LOCATION -> AppleGlyph.Location
    ActionSystemTile.ROTATION_LOCK -> AppleGlyph.Rotate
    ActionSystemTile.AIRPLANE_MODE -> AppleGlyph.Airplane
    ActionSystemTile.DO_NOT_DISTURB -> AppleGlyph.Moon
    ActionSystemTile.HOTSPOT -> AppleGlyph.PersonalHotspot
    ActionSystemTile.SCREENSHOT -> AppleGlyph.Camera
    ActionSystemTile.DARK_MODE -> AppleGlyph.Moon
    ActionSystemTile.AUTO_BRIGHTNESS -> AppleGlyph.Sparkles
    ActionSystemTile.POWER_SAVER -> AppleGlyph.Power
    ActionSystemTile.SYNC -> AppleGlyph.Reset
    ActionSystemTile.NFC -> AppleGlyph.Link
}

private fun ActionCustomShortcut.toGlyph(): AppleGlyph = when (iconKey) {
    "camera" -> AppleGlyph.Camera
    "bell" -> AppleGlyph.Bell
    "calendar" -> AppleGlyph.History
    else -> AppleGlyph.Settings
}

@Composable
private fun ActionCustomChip(
    action: ActionCustomShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.TimerCyan.copy(alpha = 0.28f), IslandColors.Purple.copy(alpha = 0.24f))))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        AppleIcon(glyph = action.toGlyph(), tint = IslandColors.TimerCyan, size = 16.dp)
        Text(text = action.label, color = Color.White, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ActionAppChip(
    app: ActionAppShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.Purple.copy(alpha = 0.34f), IslandColors.Indigo.copy(alpha = 0.22f))))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(IslandColors.Purple),
            contentAlignment = Alignment.Center
        ) {
            PackageIcon(
                packageName = app.packageName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentDescription = app.appName
            )
        }
        Text(text = app.appName, color = Color.White, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ActionContactChip(
    contact: ActionContactShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.CapsuleGreen.copy(alpha = 0.32f), IslandColors.CapsuleCyan.copy(alpha = 0.18f))))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ContactAvatar(
            name = contact.name,
            avatarUri = contact.photoUri,
            modifier = Modifier.size(18.dp),
            contentDescription = contact.name
        )
        Text(text = contact.name, color = Color.White, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QuickControlChip(
    glyph: AppleGlyph,
    label: String,
    state: Boolean?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val active = state == true
    val unreadable = state == null
    val accent = when {
        active -> IslandColors.CapsuleCyan
        unreadable -> Color(0xFFFFB340)
        else -> IslandColors.TextSecondary
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    active -> IslandColors.CapsuleCyan.copy(alpha = 0.24f)
                    unreadable -> Color(0xFFFFB340).copy(alpha = 0.14f)
                    else -> IslandColors.Gray6
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(modifier = Modifier.size(16.dp)) {
            AppleIcon(glyph = glyph, tint = accent, size = 16.dp)
            if (unreadable) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB340))
                )
            }
        }
        Text(
            text = label,
            color = if (active) Color.White else accent,
            fontSize = 8.5.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PremiumExpiryExpandedContent(
    hoursRemaining: Int,
    onControlAction: (IslandControlAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(IslandColors.TimerCyan, IslandColors.Purple))),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = AppleGlyph.Crown, tint = Color.White, size = 21.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "Premium ends soon", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = "Keep your island unlocked", color = IslandColors.TextSecondary, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${hoursRemaining.coerceAtLeast(1)}h",
                color = IslandColors.TimerCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Watch ad",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(IslandColors.Gray6)
                        .clickable { onControlAction(IslandControlAction.PremiumWatchAd) }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                )
                Text(
                    text = "Upgrade",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(IslandColors.Purple.copy(alpha = 0.8f))
                        .clickable { onControlAction(IslandControlAction.PremiumBuy) }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. TIMER - MATCHING Dynamic Island-1.svg EXACTLY!
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
            color = IslandColors.Gray6,
            radius = (this.size.minDimension - strokePx) / 2f,
            style = Stroke(width = strokePx)
        )
        // Sweep gradient cyan to blue
        val brush = Brush.sweepGradient(
            listOf(
                IslandColors.TimerCyan,
                IslandColors.TimerBlue,
                IslandColors.TimerCyan
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
private fun TimerCompactContent(label: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimerProgressRing(progress = progress, size = 21.dp, strokeWidth = 3.dp)
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IslandColors.CapsuleOrange,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TimerExpandedContent(
    label: String,
    timerRunning: Boolean,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    val isPaused = !timerRunning
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        // Source -1 positions the two controls at x=42.89 and 108.47 in
        // the 367pt body; the left inset is therefore 16.8pt, not 24pt.
        val hPad = (17.dp * scale)
        val vPad = (16.dp * scale)
        val btnSize = (52.dp * scale)
        val iconSize = (28.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Pause Button (52dp translucent orange) + Cancel Button (52dp charcoal neutral)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.5.dp * scale)
            ) {
                // Translucent Orange Pause/Resume button (52dp, from SVG circle opacity=0.45 fill=#FB8B28)
                Box(
                    modifier = Modifier
                        .size(btnSize)
                        .clip(CircleShape)
                        .background(Color(0xFFFB8B28).copy(alpha = if (isPaused) 0.85f else 0.45f))
                        .clickable {
                            onControlAction(IslandControlAction.TimerToggle)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (isPaused) AppleGlyph.Play else AppleGlyph.Pause,
                        tint = if (isPaused) Color.White else Color(0xFFFB8B28),
                        size = iconSize
                    )
                }

                // Charcoal neutral Cancel button (52dp, from SVG circle opacity=0.7 fill=#838388)
                Box(
                    modifier = Modifier
                        .size(btnSize)
                        .clip(CircleShape)
                        .background(Color(0xFF838388).copy(alpha = 0.7f))
                        .clickable { onControlAction(IslandControlAction.TimerCancel) },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Close,
                        tint = Color.White,
                        size = iconSize
                    )
                }
            }

            // RIGHT: Label "Timer" (#FB8B28, 12sp) + Time "03:35" (#FB8B28 bold 30sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale)
            ) {
                Text(
                    text = "Timer",
                    color = IslandColors.CapsuleOrange,
                    fontSize = (12f * scale).sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isPaused) "Paused" else label,
                    color = IslandColors.CapsuleOrange,
                    fontSize = (30f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            AppleIcon(glyph = AppleGlyph.Battery, tint = IslandColors.Green, size = 18.dp)
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "85%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Green,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(3.dp))
                AppleIcon(glyph = AppleGlyph.Charging, tint = IslandColors.Green, size = 11.dp)
            }
        }
    )
}

@Composable
private fun ChargingExpandedContent() {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (24.dp * scale)
        val vPad = (16.dp * scale)
        val iconBoxSize = (48.dp * scale)
        val iconSize = (26.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(CircleShape)
                        .background(IslandColors.Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Battery, tint = IslandColors.Green, size = iconSize)
                }

                Spacer(modifier = Modifier.width(14.dp * scale))

                Column(verticalArrangement = Arrangement.Center) {
                    Text(text = "Fast Warp Charging", color = Color.White, fontSize = (15f * scale).sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(text = "65W • 14 min until full", color = IslandColors.Green, fontSize = (12f * scale).sp)
                }
            }

            Text(
                text = "85%",
                fontSize = (28f * scale).sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.Green,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 5B. LOW BATTERY ALERT
// -----------------------------------------------------------------------------
@Composable
private fun LowBatteryCompactContent() {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Battery, tint = IslandColors.Red, size = 18.dp)
        },
        trailing = {
            Text(
                text = "14% !",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.Red,
                fontFamily = FontFamily.Monospace
            )
        }
    )
}

@Composable
private fun LowBatteryExpandedContent() {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (24.dp * scale)
        val tPad = (20.dp * scale)
        val bPad = (18.dp * scale)
        val iconBoxSize = (42.dp * scale)
        val iconSize = (26.dp * scale)
        val btnHeight = (43.dp * scale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = hPad, end = hPad, top = tPad, bottom = bPad),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(CircleShape)
                        .background(IslandColors.ButtonRed),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Battery, tint = IslandColors.CapsuleRed, size = iconSize)
                }

                Spacer(modifier = Modifier.width(14.dp * scale))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Low Battery",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "20% Battery Remaining",
                        color = Color(0xFF818383),
                        fontSize = (12.5f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(btnHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(IslandColors.ButtonRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    color = IslandColors.CapsuleRed,
                    fontSize = (15f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
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
                    .background(IslandColors.Orange),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Torch, tint = Color.Black, size = 11.dp)
            }
        },
        trailing = {
            Text(text = "12m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IslandColors.Orange)
        }
    )
}

@Composable
private fun DeliveryExpandedContent() {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

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
                            .size(44.dp * scale)
                            .clip(RoundedCornerShape(11.dp * scale))
                            .background(IslandColors.Orange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Torch, tint = IslandColors.Orange, size = 22.dp * scale)
                    }
                    Spacer(modifier = Modifier.width(12.dp * scale))
                    Column {
                        Text(text = "Chipotle Mexican Grill", color = Color.White, fontSize = (15f * scale).sp, fontWeight = FontWeight.Bold)
                        Text(text = "Order #8492 • 2 items", color = IslandColors.Gray, fontSize = (12f * scale).sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(IslandColors.Orange.copy(alpha = 0.2f))
                        .border(1.dp, IslandColors.Orange.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                ) {
                    Text(text = "ON THE WAY", fontSize = (9f * scale).sp, fontWeight = FontWeight.Bold, color = IslandColors.Orange)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp * scale)
                        .clip(CircleShape)
                        .background(IslandColors.Gray4)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.70f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(IslandColors.Orange)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp * scale))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Picked up", fontSize = (10f * scale).sp, color = IslandColors.Gray)
                    Text(text = "Arriving in 12 mins", fontSize = (10f * scale).sp, color = IslandColors.Orange, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Marco • Vespa Sprint • 4.9", fontSize = (12f * scale).sp, color = Color.White)
                    Spacer(modifier = Modifier.width(3.dp * scale))
                    AppleIcon(glyph = AppleGlyph.Star, tint = IslandColors.Yellow, size = 11.dp * scale)
                }
                Box(
                    modifier = Modifier
                        .size(30.dp * scale)
                        .clip(CircleShape)
                        .background(IslandColors.Green),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 14.dp * scale)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 7. FLIGHT TRACKER (DELTA AIR LINES)
// -----------------------------------------------------------------------------
@Composable
private fun FlightCompactContent(title: String = "", subtitle: String = "") {
    CompactIslandLayout(
        leading = {
            AppleIcon(glyph = AppleGlyph.Airplane, tint = IslandColors.CapsuleCyan, size = 16.dp)
        },
        trailing = {
            Text(
                text = subtitle.ifBlank { "2h 15m" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.CapsuleCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun FlightExpandedContent(
    title: String = "",
    subtitle: String = "",
    progress: Float? = null
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

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
                    Text(
                        text = title.ifBlank { "Delta Air Lines • DL 492" },
                        fontSize = (14f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp * scale)
                    ) {
                        Text(text = subtitle.ifBlank { "San Francisco" }, fontSize = (11.5f * scale).sp, color = IslandColors.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        AppleIcon(
                            glyph = AppleGlyph.NavigationRight,
                            tint = IslandColors.Gray,
                            size = 10.dp * scale
                        )
                        Text(text = "New York", fontSize = (11.5f * scale).sp, color = IslandColors.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(IslandColors.Green.copy(alpha = 0.2f))
                        .border(1.dp, IslandColors.Green.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                ) {
                    Text(text = "ON TIME", fontSize = (9f * scale).sp, fontWeight = FontWeight.Bold, color = IslandColors.Green)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "SFO", fontSize = (18f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp * scale)
                        .height(3.dp * scale)
                        .background(IslandColors.Gray4)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((progress ?: 0.65f).coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .background(IslandColors.CapsuleCyan)
                    )
                }
                Text(text = "JFK", fontSize = (18f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Gate B14 • Terminal 2", fontSize = (11f * scale).sp, color = IslandColors.Gray)
                Text(text = "Alt: 36,000 ft • 2h 15m left", fontSize = (11f * scale).sp, color = IslandColors.CapsuleCyan, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 8. SPORTS MATCH (UEFA CHAMPIONS LEAGUE)
// -----------------------------------------------------------------------------
@Composable
private fun SportsCompactContent(title: String = "", subtitle: String = "") {
    CompactIslandLayout(
        leading = {
            Text(text = title.ifBlank { "RMA 2" }, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = subtitle.ifBlank { "1 MCI" }, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = IslandColors.Yellow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(IslandColors.Green))
                Text(text = "78'", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslandColors.Green)
            }
        }
    )
}

@Composable
private fun SportsExpandedContent(title: String = "", subtitle: String = "") {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title.ifBlank { "UEFA Champions League" }, fontSize = (13.5f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                    Box(modifier = Modifier.size(6.dp * scale).clip(CircleShape).background(IslandColors.Green))
                    Text(text = subtitle.ifBlank { "78' LIVE" }, fontSize = (11f * scale).sp, fontWeight = FontWeight.Bold, color = IslandColors.Green, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Real Madrid", fontSize = (15f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Vini Jr 34', Jude 67'", fontSize = (10f * scale).sp, color = IslandColors.Gray)
                }

                Text(
                    text = "2  -  1",
                    fontSize = (24f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Yellow,
                    fontFamily = FontFamily.Monospace
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Man City", fontSize = (15f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "De Bruyne 51'", fontSize = (10f * scale).sp, color = IslandColors.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp * scale))
                    .background(IslandColors.Gray6)
                    .padding(vertical = 4.dp * scale),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Quarter-Final • 2nd Leg (Agg: 4 - 4)", fontSize = (10.5f * scale).sp, color = IslandColors.Gray)
            }
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
            AppleIcon(glyph = AppleGlyph.Maps, tint = IslandColors.Blue, size = 16.dp)
        },
        trailing = {
            Text(text = "200m", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = IslandColors.Blue)
        }
    )
}

// -----------------------------------------------------------------------------
// EQUALIZER
// -----------------------------------------------------------------------------
@Composable
internal fun LiveEqualizerMini(
    modifier: Modifier = Modifier,
    color: Color? = null,
    width: Dp = 20.dp,
    height: Dp = 18.dp,
    animated: Boolean = true,
) {
    // Single source of truth: the equalizer measured off Minimal.svg / DI-11.
    WaveformAnimation(
        modifier = modifier.size(width = width, height = height),
        color = color,
        animated = animated,
    )
}

// =============================================================================
// NEW FIGMA DYNAMIC ISLAND STATES (1-8)
// Extracted from Figma SVGs: design/reference/ios17-dynamic-island/Dynamic Island/*.svg
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
                            listOf(IslandColors.Gray, IslandColors.Gray)
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
                    color = IslandColors.Orange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " — 30:00",
                    color = IslandColors.Gray,
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
                        .background(IslandColors.Gray4),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Pause,
                        tint = IslandColors.Orange,
                        size = 12.dp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(IslandColors.Gray4),
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
                .background(Brush.linearGradient(listOf(IslandColors.Blue, IslandColors.Blue))),
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
                    color = IslandColors.Gray,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                AppleIcon(
                    glyph = AppleGlyph.Check,
                    tint = IslandColors.Green,
                    size = 8.dp
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
                color = IslandColors.Gray,
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
                gradientColors = listOf(IslandColors.Orange, IslandColors.Yellow)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AvatarCircle(
                initials = "OC",
                gradientColors = listOf(IslandColors.Green, IslandColors.Mint)
            )
        }

        Text(
            text = "Incoming Call",
            color = IslandColors.Gray,
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
                tint = IslandColors.CapsuleCyan,
                size = 14.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "2h 15m",
                color = IslandColors.CapsuleCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "San Francisco",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AppleIcon(
                glyph = AppleGlyph.NavigationRight,
                tint = Color.White,
                size = 9.dp
            )
            Text(
                text = "New York",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
                    .background(IslandColors.Green.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        IslandColors.Green.copy(alpha = 0.4f),
                        RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ON TIME",
                    color = IslandColors.Green,
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
                    .background(IslandColors.Gray4)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .background(IslandColors.CapsuleCyan)
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
                color = IslandColors.Gray,
                fontSize = 9.5.sp
            )
            Text(
                text = "Alt: 36,000 ft",
                color = IslandColors.CapsuleCyan,
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
                        .background(IslandColors.Orange),
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
                    .background(IslandColors.Orange.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        IslandColors.Orange.copy(alpha = 0.4f),
                        RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "PICKED UP",
                    color = IslandColors.Orange,
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
                .background(IslandColors.Gray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight()
                    .background(IslandColors.Orange)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // DRIVER INFO + PHONE BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Marco • Vespa Sprint • 4.9",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(3.dp))
                AppleIcon(glyph = AppleGlyph.Star, tint = IslandColors.Yellow, size = 9.dp)
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(IslandColors.Green),
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
                    modifier = Modifier.size(5.dp).clip(CircleShape).background(IslandColors.Green)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "78'",
                    color = IslandColors.Green,
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
                    color = IslandColors.Gray,
                    fontSize = 8.5.sp
                )
            }
            Text(
                text = "2 - 1",
                color = IslandColors.Yellow,
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
                    color = IslandColors.Gray,
                    fontSize = 8.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FOOTER
        Text(
            text = "Quarter-Final • 2nd Leg (Agg: 4 - 4)",
            color = IslandColors.Gray,
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
                tint = IslandColors.Blue,
                size = 16.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "200m",
                color = IslandColors.Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.ChevronRight,
                tint = IslandColors.Blue,
                size = 14.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Three route dots
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(IslandColors.Blue.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(IslandColors.Blue.copy(alpha = 0.25f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(IslandColors.Blue.copy(alpha = 0.15f))
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AUDITED FIGMA PROTOTYPE COMPOSABLES
// -----------------------------------------------------------------------------

@Composable
private fun FaceTimeAudioExpandedContent(
    variant: OngoingCallVariant,
    onControlAction: (IslandControlAction) -> Unit = {},
    name: String = "Tamia Castillo",
    duration: String = "02:45",
    avatarUri: String? = null,
    isDialing: Boolean = false
) {
    OngoingCallIslandExpanded(
        name = if (variant == OngoingCallVariant.SharedMedia) "Asia Wild Nature" else name,
        avatarUri = if (variant == OngoingCallVariant.SharedMedia) null else avatarUri,
        label = when {
            variant == OngoingCallVariant.SharedMedia -> "For me 〉"
            isDialing -> "Calling…"
            else -> "FaceTime Audio"
        },
        duration = if (isDialing) "Calling…" else duration,
        variant = variant,
        onControlAction = onControlAction
    )
}

@Composable
private fun CallSummaryExpandedContent(
    record: CallRecord?,
    onControlAction: (IslandControlAction) -> Unit
) {
    val name = record?.contactName ?: "Call ended"
    val phoneNumber = record?.phoneNumber.orEmpty()
    val callType = when (record?.direction) {
        CallDirection.INCOMING -> "Incoming call"
        CallDirection.OUTGOING -> "Outgoing call"
        CallDirection.MISSED -> "Missed call"
        null -> "Call summary"
    }
    val duration = when {
        record == null -> ""
        record.direction == CallDirection.MISSED -> "Missed"
        else -> formatCallDuration(record.durationSeconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = name,
                avatarUri = record?.avatarUri,
                modifier = Modifier.size(40.dp),
                contentDescription = name
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (phoneNumber.isBlank()) callType else "$callType · $phoneNumber",
                    color = IslandColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (duration.isNotBlank()) {
                Text(
                    text = duration,
                    color = if (record?.direction == CallDirection.MISSED) IslandColors.Orange else IslandColors.CapsuleGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CallSummaryActionButton(
                glyph = AppleGlyph.Phone,
                label = "Redial",
                onClick = { onControlAction(IslandControlAction.CallSummaryRedial) },
                modifier = Modifier.weight(1f)
            )
            CallSummaryActionButton(
                glyph = AppleGlyph.Chat,
                label = "Message",
                onClick = { onControlAction(IslandControlAction.CallSummaryMessage) },
                modifier = Modifier.weight(1f)
            )
            CallSummaryActionButton(
                glyph = AppleGlyph.Close,
                label = "Done",
                onClick = { onControlAction(IslandControlAction.CallSummaryDismiss) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CallSummaryActionButton(
    glyph: AppleGlyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(IslandColors.Gray6)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(glyph = glyph, tint = IslandColors.CapsuleCyan, size = 14.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatCallDuration(durationSeconds: Long): String {
    val minutes = (durationSeconds / 60).coerceAtLeast(0)
    val seconds = (durationSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun AirplaneAlertContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-17.svg: 367x148, rx=42. Orange airplane icon + "Turn Off Airplane Mode or Use" / "Wi-Fi to Access Data" + 335x43dp Open Settings button (#2C2C2D, rx=21.5)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(10.7.dp * scale))
                AppleIcon(glyph = AppleGlyph.Airplane, tint = Color(0xFFFB8B28), size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turn Off Airplane Mode or Use",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = "Wi-Fi to Access Data",
                        color = Color(0xFFA4A4A9),
                        fontSize = (14f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF2C2C2D))
                    .clickable { onControlAction(IslandControlAction.OpenSettings) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open Settings",
                    color = Color.White,
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ScreenMirroringContent(
    title: String = "",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    // Dynamic Island-18.svg: 367x144, rx=42. Blue Screen Mirroring icon + "Screen Mirroring" / "MacBook Pro" + Laptop glyph + 335x43dp Stop Mirroring button (#1A1C2D, rx=21.5)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.3.dp * scale))
                AppleIcon(glyph = AppleGlyph.ScreenMirroring, tint = Color(0xFF37A3DE), size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "Screen Mirroring" },
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = subtitle.ifBlank { "MacBook Pro" },
                        color = Color(0xFF818383),
                        fontSize = (13.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AppleIcon(
                    glyph = AppleGlyph.Laptop,
                    tint = Color(0xFF8F8F8F),
                    size = 24.dp * scale,
                    modifier = Modifier.padding(end = 15.dp * scale)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF1A1C2D))
                    .clickable { onControlAction(IslandControlAction.ScreenMirroringStop) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stop Mirroring",
                    color = Color(0xFF37A3DE),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MobileDataContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-19.svg: 367x162, rx=42
    // Row 1: Green antenna icon (#37C058) at x=34.8dp, center y=56dp
    //        Gap to text: 19.3dp. Title: "Mobile Data is Turned Off", Subtitle: "Turn on mobile data or use Wi-Fi\nto access data."
    // Row 2: OK (#2C2C2D) + Settings (#1A1C2D, text #37A3DE) buttons: 161.5x43dp each, rx=21.5dp, gap=12dp, bottom margin=18dp, side margins=16dp
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp * scale,
                    end = 16.dp * scale,
                    top = 28.dp * scale,
                    bottom = 18.dp * scale
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inset from button left edge (16dp) to antenna left edge (34.8dp) = 18.8dp
                Spacer(modifier = Modifier.width(18.8.dp * scale))

                // Antenna icon: 31.4dp x 26.5dp in SVG -> size 38dp * scale renders ~30x25dp
                AppleIcon(
                    glyph = AppleGlyph.PersonalHotspot,
                    tint = Color(0xFF37C058),
                    size = 38.dp * scale
                )

                // Gap between antenna right edge and text left edge: 19.3dp
                Spacer(modifier = Modifier.width(19.3.dp * scale))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mobile Data is Turned Off",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp * scale))
                    Text(
                        text = "Turn on mobile data or use Wi-Fi\nto access data.",
                        color = Color(0xFF818383),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = (16.5f * scale).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Dual Buttons: 161.5dp x 43dp, rx=21.5dp, gap=12dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.5.dp * scale))
                        .background(Color(0xFF2C2C2D))
                        .clickable { onControlAction(IslandControlAction.MobileDataOk) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.5.dp * scale))
                        .background(Color(0xFF1A1C2D))
                        .clickable { onControlAction(IslandControlAction.MobileDataSettings) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Settings",
                        color = Color(0xFF37A3DE),
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TransitRouteContent(
    title: String = "Prague Main Train Station",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    // Dynamic Island-20.svg: 367x142, rx=42. White Train icon + "Prague Main Train Station" + 335x43dp End Route button (#1D1011, rx=21.5) with text in #FA3532
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 22.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(glyph = AppleGlyph.TransitTrain, tint = Color.White, size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "Prague Main Train Station" },
                        color = Color.White,
                        fontSize = (16.5f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = IslandColors.TextSecondary,
                            fontSize = (12f * scale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF1D1011))
                    .clickable { onControlAction(IslandControlAction.TransitEndRoute) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End Route",
                    color = Color(0xFFFA3532),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TurnByTurnNavigationContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-12.svg: 4 Equidistant Lane Arrows across top + "90 ft" + Interstate 280 shield badge (51x39dp) + "North" / "San Francisco"
    var activeLane by remember { mutableStateOf(3) }
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp * scale, vertical = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP: 4 Equidistant Lane Guidance Arrows across the full card width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lane 1: Turn Left (inactive #838388)
                AppleIcon(
                    glyph = AppleGlyph.NavigationLeft,
                    tint = if (activeLane == 0) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 0
                        onControlAction(IslandControlAction.NavigationLeft)
                    }
                )
                // Lane 2: Straight (inactive #838388)
                AppleIcon(
                    glyph = AppleGlyph.NavigationStraight,
                    tint = if (activeLane == 1) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 1
                        onControlAction(IslandControlAction.NavigationStraight)
                    }
                )
                // Lane 3: Straight (inactive #838388)
                AppleIcon(
                    glyph = AppleGlyph.NavigationStraight,
                    tint = if (activeLane == 2) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 2
                        onControlAction(IslandControlAction.NavigationStraight)
                    }
                )
                // Lane 4: Turn Right (active white #FFFFFF)
                AppleIcon(
                    glyph = AppleGlyph.NavigationRight,
                    tint = if (activeLane == 3) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 3
                        onControlAction(IslandControlAction.NavigationRight)
                    }
                )
            }

            // MIDDLE: Instruction distance "90 ft" (Dynamic Island-12.svg: x=18.5, bold white)
            Text(
                text = "90 ft",
                color = Color.White,
                fontSize = (26f * scale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            // BOTTOM: Interstate 280 Highway Shield (51x39dp) + "North" / "San Francisco"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interstate 280 Shield
                Box(
                    modifier = Modifier
                        .size(width = 51.dp * scale, height = 39.dp * scale)
                        .clip(RoundedCornerShape(6.dp * scale)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.navigation_shield),
                        contentDescription = "Interstate 280",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "North",
                        color = Color.White,
                        fontSize = (16.5f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "San Francisco",
                        color = Color(0xFFA4A4A9),
                        fontSize = (13.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.1).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OngoingCallBannerContent() {
    OngoingCallIslandBanner(
        name = "Tamia Castillo",
        duration = "02:45"
    )
}

@Composable
private fun VoiceMemoExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-3.svg: 13 active red bars + 1 fading bar + 11 dots + "00:06" (#FA3532) + 50.5dp Stop button
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonSize = (50.5.dp * scale)
        val squareSize = (24.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 20.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Waveform bar sequence (13 active + 1 fading + 11 inactive dots)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                val barHeights = listOf(25, 20, 16, 20, 25, 20, 14, 10, 16, 22, 27, 15, 19)
                barHeights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp * scale)
                            .height((h.toFloat() * scale).dp)
                            .clip(RoundedCornerShape(1.5.dp * scale))
                            .background(Color(0xFFFA3532))
                    )
                }
                // Transition bar
                Box(
                    modifier = Modifier
                        .width(3.dp * scale)
                        .height(12.dp * scale)
                        .clip(RoundedCornerShape(1.5.dp * scale))
                        .background(Color(0xFFFA3532).copy(alpha = 0.4f))
                )
                // 11 Inactive dots
                repeat(11) {
                    Box(
                        modifier = Modifier
                            .size(3.dp * scale)
                            .clip(RoundedCornerShape(1.5.dp * scale))
                            .background(Color(0xFF5A5A5A).copy(alpha = 0.4f))
                    )
                }
            }

            // Middle: Duration 00:06 in red (#FA3532)
            Text(
                text = "00:06",
                color = Color(0xFFFA3532),
                fontSize = (16.5f * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = 31.dp * scale)
            )

            // Right: Stop button (50.5dp circle with 2.5dp stroke and red square)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .border(width = 2.5.dp * scale, color = Color.White, shape = CircleShape)
                        .clickable { onControlAction(IslandControlAction.VoiceMemoStop) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(squareSize)
                        .clip(RoundedCornerShape(4.dp * scale))
                        .background(Color(0xFFFA3532))
                )
            }
        }
    }
}

@Composable
private fun ScreenRecordingExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-4.svg: 12dp red pulsing dot + "00:06" (#FA3532) on row 1, "Screen Recording" on row 2, 50.5dp Stop button
    val pulse by rememberBreathing(min = 0.85f, max = 1.0f, durationMillis = 1000)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonSize = (50.5.dp * scale)
        val squareSize = (24.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 20.dp * scale, top = 16.dp * scale, bottom = 16.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Top row dot + duration, Bottom row label
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp * scale)
                            .clip(CircleShape)
                            .background(Color(0xFFFA3532).copy(alpha = pulse))
                    )
                    Text(
                        text = "00:06",
                        color = Color(0xFFFA3532),
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(3.dp * scale))

                Text(
                    text = "Screen Recording",
                    color = Color.White,
                    fontSize = (15f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp * scale))

            // Right: Stop button (50.5dp circle with 2.5dp stroke and red square)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .border(width = 2.5.dp * scale, color = Color.White, shape = CircleShape)
                        .clickable { onControlAction(IslandControlAction.ScreenRecordingStop) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(squareSize)
                        .clip(RoundedCornerShape(4.dp * scale))
                        .background(Color(0xFFFA3532))
                )
            }
        }
    }
}

@Composable
private fun ShortcutExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-5.svg: Stacked diamond icon + "Shortcut" / "100%" + 47.8dp white checkmark badge (#F0F0F5)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val iconSize = (38.dp * scale)
        val checkBadgeSize = (47.8.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp * scale, vertical = 17.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Stacked Shortcuts Icon
                Box(modifier = Modifier.size(iconSize), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(27.dp * scale)
                            .offset(y = 5.dp * scale)
                            .rotate(45f)
                            .clip(RoundedCornerShape(5.dp * scale))
                            .background(Color(0xFFA4A4A9))
                    )
                    Box(
                        modifier = Modifier
                            .size(27.dp * scale)
                            .offset(y = (-5).dp * scale)
                            .rotate(45f)
                            .clip(RoundedCornerShape(5.dp * scale))
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "Shortcut",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "100%",
                        color = Color(0xFFA4A4A9),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Right: 47.8dp White Circular Badge (#F0F0F5) with black checkmark (from Dynamic Island-5.svg)
            Box(
                modifier = Modifier
                    .size(checkBadgeSize)
                    .clip(CircleShape)
                    .border(3.dp * scale, Color(0xFFF0F0F5), CircleShape)
                    .clickable { onControlAction(IslandControlAction.ShortcutOpen) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(25.dp * scale)) {
                    val stroke = 3.3.dp.toPx()
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.14f, size.height * 0.50f),
                        end = Offset(size.width * 0.39f, size.height * 0.76f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.39f, size.height * 0.76f),
                        end = Offset(size.width * 0.86f, size.height * 0.18f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusModeExpandedContent() {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp * scale, vertical = 15.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color(0xFF5E5CE6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Moon,
                        tint = Color(0xFF5E5CE6),
                        size = 22.dp * scale
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "Do Not Disturb",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "On",
                        color = Color(0xFF37A3DE),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(70.dp * scale)
                    .height(34.dp * scale)
                    .clip(RoundedCornerShape(17.dp * scale))
                    .background(Color(0xFF1E2854)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "On",
                    color = Color(0xFF37A3DE),
                    fontSize = (14f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VideoRemoteExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    // Dynamic Island-16.svg: 367x177, rx=42. 53x53 dark squircle at X=24, Y=32 + "Video" / "Samsung TV" + Equalizer waveform + Scrubber + Centered 15s transport controls + Pinned AirPlay
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.35f) }
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val thumbSize = (53.dp * scale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp * scale, bottom = 12.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ROW 1: Thumbnail (53dp, rx=8dp at X=24dp) + Title/Channel + Live Equalizer (Dynamic Island-16.svg)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp * scale, end = 35.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .clip(RoundedCornerShape(8.dp * scale))
                            .background(Color(0xFF2C2C2D))
                            .border(0.75.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp * scale))
                    )

                    // Gap from thumbnail (77dp) to title (98.4dp) = 21.4dp
                    Spacer(modifier = Modifier.width(21.4.dp * scale))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Video",
                            color = Color.White,
                            fontSize = (16f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(5.dp * scale))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppleIcon(glyph = AppleGlyph.Laptop, tint = Color(0xFF898989), size = 14.dp * scale)
                            Spacer(modifier = Modifier.width(5.dp * scale))
                            Text(
                                text = "Samsung TV",
                                color = Color(0xFF898989),
                                fontSize = (13.5f * scale).sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                LiveEqualizerMini(
                    width = 20.dp * scale,
                    height = 22.dp * scale,
                    color = Color.White,
                    animated = isPlaying
                )
            }

            // ROW 2: Scrubber (Dynamic Island-16: 240x6.5 bar, rx=3.25, 25dp side padding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp * scale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0:50",
                    color = Color(0xFF898989),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                // Exact 14.25dp gap from time to track
                Spacer(modifier = Modifier.width(14.dp * scale))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.5.dp * scale)
                        .clip(RoundedCornerShape(3.25.dp * scale))
                        .background(Color(0xFF3F3F3F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.25.dp * scale))
                            .background(Color.White)
                    )
                }

                // Exact 14.24dp gap from track to time
                Spacer(modifier = Modifier.width(14.dp * scale))

                Text(
                    text = "-3:11",
                    color = Color(0xFF898989),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            // ROW 3: Authentic iOS Transport Trio (Play centered at X=192.3dp with +8.8dp optical offset) + AirPlay Pinned at X=315dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp * scale)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 8.8.dp * scale),
                    horizontalArrangement = Arrangement.spacedBy(27.dp * scale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip back 15s (size 36dp renders 28.3x16dp vector)
                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = (progress - 0.05f).coerceAtLeast(0f)
                                onControlAction(IslandControlAction.VideoSkipBackward)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.SkipBack15,
                            tint = Color.White,
                            size = 36.dp * scale
                        )
                    }

                    // Play (size 40dp renders 25x28dp vector)
                    Box(
                        modifier = Modifier
                            .size(44.dp * scale)
                            .clickable {
                                isPlaying = !isPlaying
                                onControlAction(IslandControlAction.VideoPlayPause)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play,
                            tint = Color.White,
                            size = 40.dp * scale
                        )
                    }

                    // Skip forward 15s (size 36dp renders 28.3x16dp vector)
                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = (progress + 0.05f).coerceAtMost(1f)
                                onControlAction(IslandControlAction.VideoSkipForward)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.SkipForward15,
                            tint = Color.White,
                            size = 36.dp * scale
                        )
                    }
                }

                // AirPlay pinned to center at X=315dp (end padding = 32dp * scale, size = 25.5dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp * scale)
                        .size(40.dp * scale)
                        .clickable { onControlAction(IslandControlAction.MusicAirPlay) },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.AirPlay,
                        tint = Color.White,
                        size = 25.5.dp * scale
                    )
                }
            }
        }
    }
}

@Composable
private fun AirDropActivityContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var isPaused by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)
        val ringSize = (51.33.dp * scale)
        val innerBoxSize = (22.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp * scale, end = 16.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: AirDrop 3 Concentric Radar Arcs + Photo Thumbnail
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // The exported AirDrop glyph includes the three arcs, center dot,
                // and overlapping 20dp photo thumbnail. Keep it as a small
                // sub-asset so the island remains Compose-built and interactive
                // without replacing the whole island with the SVG.
                Image(
                    painter = painterResource(ai.emots.kishan_dynamic.R.drawable.airdrop_icon),
                    contentDescription = "AirDrop",
                    modifier = Modifier.size(radarSize),
                    contentScale = ContentScale.FillBounds
                )

                Spacer(modifier = Modifier.width(10.dp * scale))

                // CENTER: Inline "AirDrop" (White) + "from Chris" (#A4A4A9)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    Text(
                        text = "AirDrop",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "from Chris",
                        color = Color(0xFFA4A4A9),
                        fontSize = (15f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // RIGHT: Circular Progress Ring (cx=339, cy=51, r=25.67, stroke=4.67) + 22x22dp rounded box (rx=6.22)
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clickable {
                        isPaused = !isPaused
                        onControlAction(IslandControlAction.AirDropPause)
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.67f * scale
                    val pad = strokeW / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)

                    // Inactive background ring (opacity 0.5)
                    drawArc(
                        color = Color(0xFF37A3DE).copy(alpha = 0.5f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )

                    // Active progress sweep (from SVG path d="M341.282 25.435C346.8...")
                    drawArc(
                        color = Color(0xFF37A3DE),
                        startAngle = -90f,
                        sweepAngle = 190f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                // Centered 22x22dp rounded square (rx=6.22dp)
                Box(
                    modifier = Modifier
                        .size(innerBoxSize)
                        .clip(RoundedCornerShape(6.22.dp * scale))
                        .background(Color(0xFF37A3DE).copy(alpha = if (isPaused) 0.55f else 1f))
                        .clickable {
                            isPaused = !isPaused
                            onControlAction(IslandControlAction.AirDropPause)
                        }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC ISLAND-2.SVG: AIRPODS CONNECTED
// -----------------------------------------------------------------------------
@Composable
private fun AirPodsConnectedContent(
    title: String = "Ladislav's AirPods",
    subtitle: String = "Connected · 75%",
    batteryProgress: Float? = 0.75f,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)
        val ringSize = (48.6.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp * scale, end = 20.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: 44dp Avatar with AirPods glyph + "Connected" / "Ladislav's AirPods"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.airpods_art),
                        contentDescription = "AirPods",
                        modifier = Modifier.size(30.dp * scale),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = subtitle.substringBefore(" · ").ifBlank { "Connected" },
                        color = Color(0xFFA4A4A9),
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = title.ifBlank { "Ladislav's AirPods" },
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // RIGHT: 48.6dp Circular Battery Ring with 75% text in #37C058
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clickable { onControlAction(IslandControlAction.AirPodsOpen) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.42f * scale
                    val pad = strokeW / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)

                    // Dark green ring background (#13351F)
                    drawArc(
                        color = Color(0xFF13351F),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )

                    // Green progress arc from the platform battery snapshot.
                    drawArc(
                        color = Color(0xFF37C058),
                        startAngle = -90f,
                        sweepAngle = 360f * (batteryProgress ?: 0.75f).coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = batteryProgress?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                    color = Color(0xFF37C058),
                    fontSize = (13f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC ISLAND-7.SVG: SATELLITE CONNECTION
// -----------------------------------------------------------------------------
@Composable
private fun SatelliteConnectionContent(
    title: String = "",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)
        val badgeSize = (41.4.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp * scale, end = 18.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Radar circle with satellite orb
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(radarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.satellite_radar),
                        contentDescription = "Satellite signal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = title.ifBlank { "CONNECTED" },
                        color = Color(0xFF37C058),
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = subtitle.ifBlank { "Keep pointing at Satellite" },
                        color = Color.White,
                        fontSize = (15.5f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // RIGHT: 41.4dp Dark green circle (#0B2F16) with green chat icon (#37C058)
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Color(0xFF0B2F16))
                    .clickable { onControlAction(IslandControlAction.SatelliteMessage) },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Chat,
                    tint = Color(0xFF37C058),
                    size = 18.dp * scale
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC ISLAND-8.SVG: FIND MY IPHONE ALERT
// -----------------------------------------------------------------------------
@Composable
private fun FindMyAlertContent(
    title: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 24.dp * scale)
                .clickable { onControlAction(IslandControlAction.LiveActivityOpen) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing radar pulse target circle
            Box(
                modifier = Modifier.size(radarSize),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(ai.emots.kishan_dynamic.R.drawable.findmy_radar),
                    contentDescription = "Find My signal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(modifier = Modifier.width(14.dp * scale))

            Text(
                text = title.ifBlank { "Find My iPhone Alert" },
                color = Color.White,
                fontSize = (17f * scale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC ISLAND-10.SVG: MOVED TO IPHONE
// -----------------------------------------------------------------------------
@Composable
private fun MovedToIPhoneContent(
    title: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp * scale, end = 20.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.moved_airpods_art),
                        contentDescription = "AirPods",
                        modifier = Modifier.size(30.dp * scale),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(14.dp * scale))

                Text(
                    text = title.ifBlank { "Moved to iPhone" },
                    color = Color(0xFFA4A4A9),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: 70x34dp rounded button (#1E2854) with undo back curved arrow in #37A3DE
            Box(
                modifier = Modifier
                    .width(70.dp * scale)
                    .height(34.dp * scale)
                    .clip(RoundedCornerShape(17.dp * scale))
                    .background(Color(0xFF1E2854))
                    .clickable { onControlAction(IslandControlAction.MovedUndo) },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Undo,
                    tint = Color(0xFF37A3DE),
                    size = 18.dp * scale
                )
            }
        }
    }
}
