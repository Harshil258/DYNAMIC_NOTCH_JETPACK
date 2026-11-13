package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.icons.drawIslandVector
import ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts
import ai.emots.kishan_dynamic.ui.theme.LocalAppTypography
import ai.emots.kishan_dynamic.ui.theme.LocalAppFonts
import ai.emots.kishan_dynamic.ui.theme.withIslandReferenceFonts
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import ai.emots.kishan_dynamic.ui.motion.AppMotion
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    CallSummaryWhatsApp,
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
    LiveActivityPrimary,
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
internal fun IslandDemoState.usesCompanionBubble(): Boolean =
    this == IslandDemoState.NotificationWithMusicCompact

/** One geometry resolver keeps state heights identical across every surface. */
internal fun IslandDemoState.resolvedHeight(tokens: AppIslandTokens): Dp =
    when (this) {
        IslandDemoState.Minimal -> tokens.minimalCapsuleHeight
        IslandDemoState.Idle, IslandDemoState.MusicCompact, IslandDemoState.NotificationCompact, IslandDemoState.NotificationStacked -> IslandFamilyGeometry.target(this).height.dp
        else -> if (isCompactPresentation()) tokens.compactHeight else IslandFamilyGeometry.target(this).height.dp
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
/**
 * Defers animated size reads to the layout phase, eliminating 60-120fps recomposition churn
 * of DynamicIslandPill and all child content during dimension springs (AUR-P1-014).
 */
private fun Modifier.animatedIslandDimensions(
    widthProvider: () -> androidx.compose.ui.unit.Dp,
    heightProvider: () -> androidx.compose.ui.unit.Dp
): Modifier = layout { measurable, constraints ->
    val wPx = widthProvider().roundToPx().coerceIn(0, constraints.maxWidth)
    val hPx = heightProvider().roundToPx().coerceIn(0, constraints.maxHeight)
    val placeable = measurable.measure(
        androidx.compose.ui.unit.Constraints.fixed(wPx, hPx)
    )
    layout(wPx, hPx) {
        placeable.place(0, 0)
    }
}

/**
 * Sizes Quick Controls to its rendered rows: one tile row is the base, and
 * each additional populated row contributes only its content height and gap.
 */
fun actionControlExpandedHeight(
    systemTilesCount: Int,
    hasCustomActions: Boolean,
    hasApps: Boolean,
    hasContacts: Boolean
): Dp {
    var base = 158.dp
    if (systemTilesCount > 4) base += 51.dp
    if (hasCustomActions) base += 51.dp
    if (hasApps) base += 53.dp
    if (hasContacts) base += 53.dp
    return base
}

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
    onCompanionLongPress: () -> Unit = onLongPress,
    mainContentDescription: String = "Dynamic Island",
    companionContentDescription: String = "Open companion activity",
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
    notificationActiveIndex: Int = 0,
    unseenNotificationCount: Int = notifications.size,
    replyingNotificationId: String? = null,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onNotificationAction: (NotificationInfo, NotificationActionInfo) -> Unit = { _, _ -> },
    onNotificationSelect: (String) -> Unit = {},
    onNotificationDismiss: (String) -> Unit = {},
    callContact: ContactInfo? = null,
    callParticipants: List<ContactInfo> = emptyList(),
    callSummary: CallRecord? = null,
    callDurationSeconds: Long = 0L,
    showCallDuration: Boolean = true,
    callIsDialing: Boolean = false,
    callCanDecline: Boolean = true,
    callIsMuted: Boolean? = null,
    callIsSpeaker: Boolean? = null,
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
    liveActivityShowChronometer: Boolean = false,
    liveActivityChronometerBase: Long = 0L,
    liveActivityChronometerCountDown: Boolean = false,
    liveActivitySourceBacked: Boolean = false,
    liveActivityActionLabel: String = "",
    liveActivityInfo: ai.emots.kishan_dynamic.data.model.LiveActivityInfo? = null,
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
    onOpenNotifications: () -> Unit = {},
    flatTop: Boolean = false,
    backgroundArgb: Int = 0xFF000000.toInt(),
    foregroundArgb: Int = 0xFFFFFFFF.toInt(),
    cameraCutoutMode: Boolean = false,
    cutoutSizePx: Int = 80,
    readablePresentationToken: Long? = null,
    onPresentationReadable: (Long) -> Unit = {},
    availableControlActions: Set<IslandControlAction>? = null
) {
    val renderSettings = LocalIslandRenderSettings.current
    val motionOverride = renderSettings.motion
    val reduceMotion = motionOverride?.enabled == false || ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val companionInteractionSource = remember { MutableInteractionSource() }
    val islandView = androidx.compose.ui.platform.LocalView.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val isCompanionPressed by companionInteractionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.96f else 1.0f,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: ai.emots.kishan_dynamic.ui.motion.AppMotion.pressSpring(),
        label = "island_press"
    )
    val companionPressScale by animateFloatAsState(
        targetValue = if (isCompanionPressed && !reduceMotion) 0.92f else 1f,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: ai.emots.kishan_dynamic.ui.motion.AppMotion.pressSpring(),
        label = "companion_press"
    )
    val islandStiffness = if (fastAnimations) 760f else 380f
    val contentDuration = if (reduceMotion) 80 else if (fastAnimations) 130 else 200

    // Interactive upward drag dismissal with Apple rubber-band physics
    val dragOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // -------------------------------------------------------------------------
    // Family-specific Figma geometry on the393dp reference canvas.
    // Compact notification capsules prefer189dp with separate edge regions;
    // expanded sheets follow their family canvas and measured content height.
    // -------------------------------------------------------------------------
    val islandTokens = ai.emots.kishan_dynamic.ui.theme.AppTheme.island
    val compactContentInset = if (state in setOf(IslandDemoState.NotificationCompact, IslandDemoState.NotificationStacked))
        islandTokens.notificationCompactInset else islandTokens.compactHorizontalInset
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val maxExpanded = IslandFamilyGeometry.resolve(IslandDemoState.MusicExpanded, screenWidth.value).width.dp
    val shellExpandedInset = if (!liveActivitySourceBacked && state in setOf(
        IslandDemoState.DeliveryExpanded, IslandDemoState.FlightExpanded,
        IslandDemoState.SportsExpanded, IslandDemoState.FlightTrackerExpanded
    )) islandTokens.expandedContentInset else 0.dp
    val isSplit = state.usesCompanionBubble()
    val isCompact = state.isCompactPresentation()
    val safeHorizontalScale = horizontalScale.coerceIn(0.8f, 1.2f)

    val activeCompactNotification = notifications.getOrNull(notificationActiveIndex) ?: notifications.firstOrNull()
    val compactLabel = compactNotificationLabel(activeCompactNotification, unseenNotificationCount)
    val compactTextStyle = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.caption.copy(
        color = Color.White, fontWeight = FontWeight.SemiBold
    )
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val compactLabelWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
        textMeasurer.measure(compactLabel, compactTextStyle, maxLines = 1).size.width.toDp()
    }
    val notificationWidth = islandTokens.compactHorizontalInset * 2 + islandTokens.compactIconSize +
        islandTokens.compactContentGap + compactLabelWidth
    val familyGeometry = IslandFamilyGeometry.resolve(state, screenWidth.value)
    val sportsGlanceWidth = if (state == IslandDemoState.SportsCompact && androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f)
        with(androidx.compose.ui.platform.LocalDensity.current) {
            textMeasurer.measure("RMA 2  1 MCI", compactTextStyle.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold), maxLines = 1).size.width.toDp()
        } + islandTokens.compactHorizontalInset * 2 + islandTokens.compactContentGap else 0.dp
    val baseWidth = if (state == IslandDemoState.NotificationCompact || state == IslandDemoState.NotificationStacked)
        maxOf(notificationWidth, islandTokens.notificationCompactWidth) else maxOf(familyGeometry.width.dp, sportsGlanceWidth)
    val isCutoutDot = cameraCutoutMode && (state == IslandDemoState.Minimal || state == IslandDemoState.Idle)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cutoutDotSizeDp = with(density) { (cutoutSizePx.toFloat() / density.density).dp }

    val splitExtras = if (isSplit) islandTokens.splitGap + islandTokens.sideSize else 0.dp
    val maxMainWidth = (maxExpanded - splitExtras).coerceAtLeast(islandTokens.compactHeight)
    val requestedWidth = if (isCompact && state != IslandDemoState.NotificationCompact &&
        state != IslandDemoState.NotificationStacked) baseWidth * safeHorizontalScale else baseWidth
    val naturalTargetWidth = if (isCutoutDot) {
        cutoutDotSizeDp.coerceIn(24.dp, 56.dp)
    } else if (isCompact) {
        ai.emots.kishan_dynamic.data.model.IslandGeometryConstraints.compactWidth(
            requestedWidth.value, screenWidth.value, islandTokens.compactStatusSideReserve.value,
            splitExtras.value, islandTokens.compactMaxWidth.value
        ).dp
    } else requestedWidth.coerceAtMost(maxMainWidth)
    val widthBudget = if (isCompact) {
        (screenWidth - islandTokens.compactStatusSideReserve * 2 - splitExtras).coerceAtLeast(1.dp)
            .coerceAtMost(islandTokens.compactMaxWidth)
    } else maxMainWidth
    val targetWidth = (renderSettings.width ?: naturalTargetWidth)
        .coerceAtLeast(renderSettings.minimumWidth ?: 0.dp).coerceAtMost(widthBudget)

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
    val activeNotification = notifications.getOrNull(notificationActiveIndex) ?: notifications.firstOrNull()
    var isNotificationCardExpanded by remember { mutableStateOf(false) }
    val isReplyingNotification = state == IslandDemoState.NotificationExpanded &&
        replyingNotificationId != null &&
        (activeNotification?.id == replyingNotificationId || notifications.any { it.id == replyingNotificationId })
    val hasLargePicture = state == IslandDemoState.NotificationExpanded &&
        activeNotification?.picturePath != null
    val isNotificationExpanded = isNotificationCardExpanded || isReplyingNotification || hasLargePicture

    LaunchedEffect(state, notifications.isEmpty()) {
        if (state != IslandDemoState.NotificationExpanded || notifications.isEmpty()) {
            isNotificationCardExpanded = false
        }
    }

    val measuredHeights = remember { mutableStateMapOf<IslandDemoState, Dp>() }
    val maxContentHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp -
        islandTokens.topInset - islandTokens.expandedContentInset * 2).coerceAtLeast(48.dp)
    val naturalTargetHeight = if (isCutoutDot) {
        cutoutDotSizeDp.coerceIn(24.dp, 56.dp)
    } else if (!isCompact) {
        (if (state in setOf(
                IslandDemoState.NotificationExpanded, IslandDemoState.CallExpanded,
                // These cards have no direct atlas specimen. Their measured content,
                // including recipe-owned padding, determines the surface height.
                IslandDemoState.PremiumExpiryExpanded,
                IslandDemoState.DeliveryExpanded, IslandDemoState.FlightExpanded,
                IslandDemoState.SportsExpanded, IslandDemoState.TransportActivity,
                IslandDemoState.FlightTrackerExpanded, IslandDemoState.SubscriptionPricing,
                IslandDemoState.MovieCard, IslandDemoState.TimerImage,
                IslandDemoState.ActionControlExpanded, IslandDemoState.ColorOptions,
                IslandDemoState.CallSummaryExpanded,
                IslandDemoState.AirplaneAlert, IslandDemoState.ScreenMirroringAlert,
                IslandDemoState.MobileDataAlert, IslandDemoState.TransitRouteAlert
            ))
            measuredHeights[state] ?: state.resolvedHeight(islandTokens)
        else maxOf(measuredHeights[state] ?: 0.dp, state.resolvedHeight(islandTokens)))
            .coerceIn(islandTokens.expandedMinHeight, maxContentHeight)
    } else {
        state.resolvedHeight(islandTokens)
    }
    val targetHeight = (renderSettings.height ?: naturalTargetHeight)
        .coerceAtLeast(1.dp).coerceAtMost(renderSettings.maximumHeight ?: maxContentHeight)

    // Authentic Apple continuous squircle corner morphing:
    // - Compact / Split states: half of compact height (36.67 / 2 = 18.33dp) for full pill capsule
    // - 86dp Alert states: 43dp (86 / 2) for standard iOS alert capsule
    // - Expanded Activity Sheets (140-240dp): 42dp Apple continuous squircle
    val targetCornerRadiusDp = if (isCutoutDot) cutoutDotSizeDp.coerceIn(24.dp, 56.dp) / 2
        else if ((state == IslandDemoState.NotificationExpanded || state == IslandDemoState.NotificationImage) && targetHeight >= 120.dp) 44.dp
        else IslandFamilyGeometry.resolve(state, screenWidth.value, contentHeight = targetHeight.value).radius.dp

    val animatedCornerRadiusDp by animateDpAsState(
        targetValue = renderSettings.corner ?: targetCornerRadiusDp,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: AppMotion.cornerMorphSpring(),
        label = "island_corner_radius"
    )

    val cornerRadiusPx = with(density) { animatedCornerRadiusDp.toPx() }
    val hasFlatTop = flatTop && !isCutoutDot
    val cornerSmoothing by animateFloatAsState(
        targetValue = IslandFamilyGeometry.cornerSmoothing(state),
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else AppMotion.cornerMorphSpring(),
        label = "island_corner_smoothing"
    )
    val cornerRadius = remember(cornerRadiusPx, hasFlatTop, cornerSmoothing) {
        continuousIslandShape(cornerRadiusPx, hasFlatTop, cornerSmoothing)
    }

    // Apple Liquid Morphing Springs for Dimensions
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: spring(dampingRatio = 0.82f, stiffness = islandStiffness),
        label = "island_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: spring(dampingRatio = 0.82f, stiffness = islandStiffness),
        label = "island_height"
    )

    // Acknowledge real settled frames; lifetime scheduling remains exclusively in the engine.
    LaunchedEffect(readablePresentationToken, targetWidth, targetHeight) {
        val token = readablePresentationToken ?: return@LaunchedEffect
        androidx.compose.runtime.snapshotFlow {
            kotlin.math.abs(animatedWidth.value - targetWidth.value) < 0.5f &&
                kotlin.math.abs(animatedHeight.value - targetHeight.value) < 0.5f
        }.collect { settled ->
            if (settled) {
                androidx.compose.runtime.withFrameNanos { }
                androidx.compose.runtime.withFrameNanos { }
                onPresentationReadable(token)
            }
        }
    }

    // Dynamic Island Split Companion Bubble Springs (Blob separation & liquid merge)
    val targetGap = if (isSplit) islandTokens.splitGap else 0.dp
    val targetSideSize = if (isSplit) islandTokens.sideSize else 0.dp

    val animatedGap by animateDpAsState(
        targetValue = targetGap,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: if (isSplit) AppMotion.bubbleSplitSpring() else AppMotion.bubbleMergeSpring(),
        label = "bubble_gap"
    )

    val animatedSideSize by animateDpAsState(
        targetValue = targetSideSize,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap()
            else motionOverride?.spec() ?: if (isSplit) AppMotion.bubbleSplitSpring() else AppMotion.bubbleMergeSpring(),
        label = "bubble_side_size"
    )

    // Breathing Ambient Specular Aura (iOS Dynamic Island glow)
    val auraAlphaState: androidx.compose.runtime.State<Float> = if (reduceMotion) {
        remember { mutableStateOf(0.08f) }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
        infiniteTransition.animateFloat(
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
    }

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

    Box(contentAlignment = Alignment.TopCenter) {
        // Soft Ambient Aura Bloom
        Box(
            modifier = Modifier
                .animatedIslandDimensions({ animatedWidth }, { animatedHeight })
                .graphicsLayer {
                    translationY = dragOffsetY.value
                    alpha = if (isCompact) 0f else auraAlphaState.value * 0.10f
                }
                .clip(cornerRadius)
                .background(auraColor)
        )

        Row(
            modifier = modifier
                .pointerInput(onSwipeDismiss, isNotificationExpanded, state, notifications.size, isReplyingNotification) {
                    if (state == IslandDemoState.NotificationExpanded &&
                        (notifications.size > 1 || isReplyingNotification)) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { },
                        onVerticalDrag = { change, dragAmount ->
                            coroutineScope.launch {
                                val current = dragOffsetY.value
                                // Apple iOS rubber-band drag resistance (55% tracking coefficient)
                                val updated = (current + dragAmount * 0.55f).coerceIn(-100f, 60f)
                                dragOffsetY.snapTo(updated)
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            if (dragOffsetY.value > 24f && state == IslandDemoState.NotificationExpanded && !isNotificationExpanded) {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                isNotificationCardExpanded = true
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(0f, animationSpec = AppMotion.islandSpring())
                                }
                            } else if (dragOffsetY.value < -28f && state == IslandDemoState.NotificationExpanded && isNotificationExpanded && !isReplyingNotification) {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                isNotificationCardExpanded = false
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(0f, animationSpec = AppMotion.islandSpring())
                                }
                            } else if (dragOffsetY.value < -36f) {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.KEYBOARD_TAP
                                    )
                                }
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(
                                        targetValue = -100f,
                                        animationSpec = AppMotion.islandSpring()
                                    )
                                    dragOffsetY.snapTo(0f)
                                }
                                onSwipeDismiss()
                            } else {
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = AppMotion.islandSpring()
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = AppMotion.islandSpring()
                                )
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.Top
        ) {
            // Main Island Pill Body
            val pillBgColor = when (state) {
                IslandDemoState.Minimal -> Color(0xFFDC0917).copy(alpha = 0.60f)
                else -> Color(backgroundArgb)
            }
            Box(
                modifier = Modifier
                    .islandInspectableRegion("Shell")
                    .then(Modifier.semantics { testTag = "island_surface" })
                    .animatedIslandDimensions({ animatedWidth }, { animatedHeight })
                    .graphicsLayer {
                        scaleX = pressScale
                        // Tactile squeeze during upward drag
                        val dragSqueeze = if (dragOffsetY.value < 0f) {
                            (1f - (-dragOffsetY.value / 350f)).coerceIn(0.88f, 1f)
                        } else 1f
                        scaleY = pressScale * dragSqueeze
                        translationY = dragOffsetY.value
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                    }
                    .shadow(
                        elevation = 0.dp,
                        shape = cornerRadius,
                        ambientColor = Color.Black.copy(alpha = 0.7f),
                        spotColor = Color.Black.copy(alpha = 0.9f)
                    )
                    .clip(cornerRadius)
                    .semantics {
                        if (isCompact) {
                            contentDescription = mainContentDescription
                            role = Role.Button
                        }
                    }
                    .then(if (isCompact) Modifier.combinedClickable(
                        enabled = true,
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
                    ) else Modifier)
                    .background(pillBgColor)
                    .padding(
                        horizontal = if (isCompact) compactContentInset else shellExpandedInset,
                        vertical = if (isCompact) 0.dp else shellExpandedInset
                    ),
                contentAlignment = if (isCompact) Alignment.Center else Alignment.TopCenter
            ) {
                CompositionLocalProvider(LocalContentColor provides Color(foregroundArgb), LocalIslandAvailableControls provides availableControlActions,
                    LocalAppTypography provides ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.withIslandReferenceFonts(),
                    LocalAppFonts provides ai.emots.kishan_dynamic.ui.theme.AppTheme.fonts.copy(body = IslandReferenceFonts.Text, display = IslandReferenceFonts.Display, mono = IslandReferenceFonts.Text),
                    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = IslandReferenceFonts.Text,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false))) {
                    val shellHorizontalInset = if (isCompact) compactContentInset else shellExpandedInset
                    IslandContentViewport(
                        targetWidth = (targetWidth - shellHorizontalInset * 2).coerceAtLeast(1.dp),
                        compact = isCompact,
                        compactHeight = targetHeight,
                        maxExpandedHeight = minOf(maxContentHeight, renderSettings.maximumHeight ?: maxContentHeight,
                            renderSettings.height?.takeIf { !isCompact } ?: maxContentHeight) - if (shellHorizontalInset > 0.dp && !isCompact) islandTokens.expandedContentInset * 2 else 0.dp,
                        onContentHeight = { height ->
                            val total = height + if (shellHorizontalInset > 0.dp && !isCompact) islandTokens.expandedContentInset * 2 else 0.dp
                            if (measuredHeights[state] != total) measuredHeights[state] = total
                        }
                    ) {
                    AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        val transform = if (motionOverride?.enabled == false) {
                            androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                        } else if (reduceMotion) {
                            fadeIn(tween(contentDuration)) togetherWith fadeOut(tween(contentDuration))
                        } else {
                            (motionOverride?.enter() ?: AppMotion.islandContentEnter()) togetherWith
                                (motionOverride?.exit() ?: AppMotion.islandContentExit())
                        }
                        transform using androidx.compose.animation.SizeTransform(clip = false) { _, _ ->
                            androidx.compose.animation.core.snap()
                        }
                    },
                    modifier = Modifier,
                    label = "island_content_morph"
                ) { targetState ->
                    if (liveActivitySourceBacked) {
                        if (targetState.isCompactPresentation()) {
                            SourceBackedLiveActivityCompactContent(
                                state = targetState,
                                title = liveActivityTitle,
                                subtitle = liveActivitySubtitle,
                                progress = liveActivityProgress,
                                showChronometer = liveActivityShowChronometer,
                                chronometerBase = liveActivityChronometerBase,
                                chronometerCountDown = liveActivityChronometerCountDown
                            )
                        } else {
                            SourceBackedLiveActivityExpandedContent(
                                state = targetState,
                                title = liveActivityTitle,
                                subtitle = liveActivitySubtitle,
                                progress = liveActivityProgress,
                                showChronometer = liveActivityShowChronometer,
                                chronometerBase = liveActivityChronometerBase,
                                chronometerCountDown = liveActivityChronometerCountDown,
                                actionLabel = liveActivityActionLabel,
                                onControlAction = onControlAction
                            )
                        }
                    } else when (targetState) {
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
                            isDialing = callIsDialing,
                            isMutedState = callIsMuted,
                            isSpeakerState = callIsSpeaker
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
                            isDialing = callIsDialing,
                            isMutedState = callIsMuted,
                            isSpeakerState = callIsSpeaker
                        )
                        IslandDemoState.SharedMediaCallExpanded -> FaceTimeAudioExpandedContent(
                            variant = OngoingCallVariant.SharedMedia,
                            onControlAction = onControlAction,
                            isMutedState = callIsMuted,
                            isSpeakerState = callIsSpeaker
                        )
                        IslandDemoState.OngoingCallBanner -> OngoingCallBannerContent()
                        IslandDemoState.NotificationCompact -> NotificationCompactContent(
                            notifications.getOrNull(notificationActiveIndex) ?: notifications.firstOrNull(),
                            unseenNotificationCount
                        )
                        IslandDemoState.NotificationStacked -> NotificationStackedMainContent(
                            notifications = notifications,
                            activeIndex = notificationActiveIndex,
                            unseenCount = unseenNotificationCount
                        )
                        IslandDemoState.NotificationWithMusicCompact -> NotificationWithMusicCompactContent(
                            notifications = notifications,
                            activeIndex = notificationActiveIndex,
                            track = musicTrack,
                            isPlaying = musicIsPlaying,
                            unseenCount = unseenNotificationCount
                        )
                        IslandDemoState.NotificationExpanded -> NotificationExpandedContent(
                            notifications = notifications,
                            activeIndex = notificationActiveIndex,
                            isCardExpanded = isNotificationExpanded,
                            onToggleExpand = { isNotificationCardExpanded = !isNotificationCardExpanded },
                            replyingNotificationId = replyingNotificationId,
                            replyAction = replyAction,
                            onSendReply = onSendReply,
                            onCancelReply = onCancelReply,
                            onNotificationAction = onNotificationAction,
                            onNotificationSelect = onNotificationSelect,
                            onNotificationDismiss = onNotificationDismiss,
                            onHeightChange = { h ->
                                measuredHeights[IslandDemoState.NotificationExpanded] = h
                            }
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
                            isConnecting = bluetoothIsConnecting || targetState == IslandDemoState.BluetoothConnecting
                        )
                        IslandDemoState.ActionControlExpanded -> {
                            val effectiveStates = if (actionSystemStates.isEmpty()) {
                                mapOf(
                                    ActionSystemTile.WIFI to true,
                                    ActionSystemTile.BLUETOOTH to true,
                                    ActionSystemTile.MOBILE_DATA to false,
                                    ActionSystemTile.TORCH to true,
                                    ActionSystemTile.LOCATION to true,
                                    ActionSystemTile.DO_NOT_DISTURB to false
                                )
                            } else {
                                actionSystemStates
                            }
                            ActionControlExpandedContent(
                                systemTiles = actionSystemTiles,
                                systemStates = effectiveStates,
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
                        }
                        IslandDemoState.PremiumExpiryExpanded -> PremiumExpiryExpandedContent(
                            hoursRemaining = premiumHoursRemaining,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.ChargingCompact -> ChargingCompactContent(batteryPercent)
                        IslandDemoState.ChargingExpanded -> ChargingExpandedContent(batteryPercent)
                        IslandDemoState.LowBatteryCompact -> LowBatteryCompactContent(batteryPercent)
                        IslandDemoState.LowBatteryExpanded -> LowBatteryExpandedContent(batteryPercent)
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
                        IslandDemoState.DeliveryCompact -> ai.emots.kishan_dynamic.ui.components.live.DeliveryIslandCompact(liveActivityInfo)
                        IslandDemoState.DeliveryExpanded -> ai.emots.kishan_dynamic.ui.components.live.DeliveryIslandExpanded(
                            info = liveActivityInfo,
                            progressOverride = liveActivityProgress,
                            onCallAction = { onControlAction(IslandControlAction.CallAccept) }
                        )
                        IslandDemoState.FlightCompact -> ai.emots.kishan_dynamic.ui.components.live.FlightIslandCompact(liveActivityInfo)
                        IslandDemoState.FlightExpanded -> ai.emots.kishan_dynamic.ui.components.live.FlightIslandExpanded(
                            info = liveActivityInfo,
                            progressOverride = liveActivityProgress
                        )
                        IslandDemoState.SportsCompact -> ai.emots.kishan_dynamic.ui.components.live.SportsIslandCompact(liveActivityInfo)
                        IslandDemoState.SportsExpanded -> ai.emots.kishan_dynamic.ui.components.live.SportsIslandExpanded(
                            info = liveActivityInfo
                        )
                        IslandDemoState.NavigationCompact -> ai.emots.kishan_dynamic.ui.components.live.NavigationIslandCompact(liveActivityInfo)
                        IslandDemoState.TimerImage -> TimerImageReferenceContent(timerLabel)
                        IslandDemoState.NotificationImage -> NotificationImageReferenceContent(
                            notifications,
                            notificationActiveIndex,
                            onHeightChange = { h ->
                                measuredHeights[IslandDemoState.NotificationImage] = h
                            }
                        )
                        IslandDemoState.CallAvatars -> if (callParticipants.size > 1) GroupCallReferenceContent(callParticipants) else IncomingCallIsland(
                            name = callContact?.name ?: "Unknown caller",
                            label = callContact?.phoneNumber?.takeIf { it.isNotBlank() } ?: "Mobile",
                            avatarUri = callContact?.avatarUri,
                            onAccept = { onControlAction(IslandControlAction.CallAccept) },
                            onDecline = { onControlAction(IslandControlAction.CallDecline) },
                            canDecline = callCanDecline
                        )
                        IslandDemoState.TransportActivity -> if (liveActivityInfo != null && liveActivityInfo.kind == ai.emots.kishan_dynamic.data.model.LiveActivityKind.NAVIGATION) {
                            ai.emots.kishan_dynamic.ui.components.live.NavigationIslandExpanded(
                                info = liveActivityInfo,
                                onControlAction = onControlAction
                            )
                        } else {
                            TransportReferenceContent(info = liveActivityInfo, onControlAction = onControlAction)
                        }
                        IslandDemoState.AirplaneAlert -> AirplaneAlertContent(onControlAction)
                        IslandDemoState.FlightTrackerExpanded -> FlightTrackerReferenceContent(liveActivityInfo, liveActivityProgress)
                        IslandDemoState.ScreenMirroringAlert -> ScreenMirroringContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.SubscriptionPricing -> SubscriptionReferenceContent(
                            onUpgrade = { onControlAction(IslandControlAction.PremiumBuy) },
                            onDismiss = { onControlAction(IslandControlAction.CallSummaryDismiss) }
                        )
                        IslandDemoState.MobileDataAlert -> MobileDataContent(onControlAction)
                        IslandDemoState.MovieCard -> MovieReferenceContent(
                            onOpenTicket = { onControlAction(IslandControlAction.LiveActivityPrimary) }
                        )
                        IslandDemoState.TransitRouteAlert -> TransitRouteContent(
                            title = liveActivityTitle,
                            subtitle = liveActivitySubtitle,
                            onControlAction = onControlAction
                        )
                        IslandDemoState.ColorOptions -> ColorOptionsReferenceContent()
                    }
                }
                    }
            }
        }

            // Companion split bubble with authentic Apple liquid separation and merge springs
            if (animatedGap > 0.5.dp) {
                Spacer(modifier = Modifier.width(animatedGap))
            }
            if (animatedSideSize > 0.5.dp) {
                val bubbleProgress = (animatedSideSize / islandTokens.sideSize).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .size(animatedSideSize)
                        .graphicsLayer {
                            scaleX = companionPressScale * bubbleProgress
                            scaleY = companionPressScale * bubbleProgress
                            alpha = bubbleProgress
                            translationY = dragOffsetY.value
                        }
                        .shadow(
                            elevation = 0.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black,
                            spotColor = auraColor.copy(alpha = 0.5f * bubbleProgress)
                        )
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = companionContentDescription
                            role = Role.Button
                        }
                        .combinedClickable(
                            interactionSource = companionInteractionSource,
                            indication = null,
                            onClick = {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.KEYBOARD_TAP
                                    )
                                }
                                onCompanionTap()
                            },
                            onLongClick = {
                                if (hapticEnabled) {
                                    islandView.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.LONG_PRESS
                                    )
                                }
                                onCompanionLongPress()
                            }
                        )
                        .background(pillBgColor)
                        .border(
                            width = 0.75.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x38FFFFFF), Color(0x06FFFFFF))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color(foregroundArgb), LocalIslandAvailableControls provides availableControlActions,
                    LocalAppTypography provides ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.withIslandReferenceFonts(),
                    LocalAppFonts provides ai.emots.kishan_dynamic.ui.theme.AppTheme.fonts.copy(body = IslandReferenceFonts.Text, display = IslandReferenceFonts.Display, mono = IslandReferenceFonts.Text),
                    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = IslandReferenceFonts.Text,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false))) {
                        AnimatedContent(
                        targetState = state,
                        transitionSpec = {
                            if (reduceMotion) {
                                fadeIn(tween(contentDuration)) togetherWith fadeOut(tween(contentDuration))
                            } else {
                                AppMotion.bubbleContentEnter() togetherWith AppMotion.bubbleContentExit()
                            }
                        },
                        label = "bubble_content_morph"
                    ) { bubbleState ->
                        when (bubbleState) {
                            IslandDemoState.CallCompact -> {
                                OngoingCallIslandSide()
                            }
                            IslandDemoState.TimerCompact -> {
                                TimerProgressRing(progress = 0.72f, size = 21.dp, strokeWidth = 2.5.dp)
                            }
                            IslandDemoState.Minimal -> {
                                AppleIcon(
                                    glyph = AppleGlyph.Controls,
                                    tint = IslandColors.CapsuleCyan,
                                    size = 17.dp
                                )
                            }
                            IslandDemoState.NotificationStacked -> {
                                NotificationStackedSideContent(
                                    notifications = notifications,
                                    activeIndex = notificationActiveIndex
                                )
                            }
                            IslandDemoState.NotificationWithMusicCompact -> {
                                NotificationWithMusicSideContent(
                                    notifications = notifications,
                                    activeIndex = notificationActiveIndex,
                                    track = musicTrack,
                                    isPlaying = musicIsPlaying,
                                    unseenCount = unseenNotificationCount
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
}

@Composable
private fun SourceBackedLiveActivityCompactContent(
    state: IslandDemoState,
    title: String,
    subtitle: String,
    progress: Float?,
    showChronometer: Boolean,
    chronometerBase: Long,
    chronometerCountDown: Boolean
) {
    val (glyph, tint) = sourceBackedIdentity(state)
    CompactIslandLayout(
        leading = { AppleIcon(glyph = glyph, tint = tint, size = 16.dp) },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = title.ifBlank { "Live activity" },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 105.dp)
                )
                if (showChronometer && chronometerBase > 0L) {
                    NotificationChronometerText(
                        baseElapsedRealtime = chronometerBase,
                        countDown = chronometerCountDown,
                        fontSize = 9.sp,
                        color = tint
                    )
                } else {
                    val status = progress?.let { "${(it.coerceIn(0f, 1f) * 100).roundToInt()}%" }
                        ?: subtitle.takeIf { it.isNotBlank() }
                    if (status != null) {
                    Text(
                        text = status,
                        color = tint,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 105.dp)
                    )
                    }
                }
            }
        }
    )
}

@Composable
private fun SourceBackedLiveActivityExpandedContent(
    state: IslandDemoState,
    title: String,
    subtitle: String,
    progress: Float?,
    showChronometer: Boolean,
    chronometerBase: Long,
    chronometerCountDown: Boolean,
    actionLabel: String,
    onControlAction: (IslandControlAction) -> Unit
) {
    val (glyph, tint) = sourceBackedIdentity(state)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = glyph, tint = tint, size = 21.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.ifBlank { "Live activity" },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = IslandColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showChronometer && chronometerBase > 0L) {
                    NotificationChronometerText(
                        baseElapsedRealtime = chronometerBase,
                        countDown = chronometerCountDown,
                        fontSize = 12.sp,
                        color = tint
                    )
                }
            }
            if (actionLabel.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(tint.copy(alpha = 0.2f))
                        .clickable { onControlAction(IslandControlAction.LiveActivityPrimary) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        color = tint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
        if (progress != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(IslandColors.Gray5)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(tint)
                )
            }
        }
    }
}

private fun sourceBackedIdentity(state: IslandDemoState): Pair<AppleGlyph, Color> = when (state) {
    IslandDemoState.TimerCompact,
    IslandDemoState.TimerExpanded -> AppleGlyph.Timer to IslandColors.CapsuleOrange
    IslandDemoState.NavigationCompact,
    IslandDemoState.TransportActivity -> AppleGlyph.Maps to IslandColors.Blue
    IslandDemoState.ColorOptions -> AppleGlyph.Sparkles to IslandColors.Purple
    IslandDemoState.VoiceMemoExpanded,
    IslandDemoState.ScreenRecordingExpanded -> AppleGlyph.Record to IslandColors.CapsuleRed
    IslandDemoState.AirDropActivity -> AppleGlyph.AirDrop to IslandColors.CapsuleCyan
    IslandDemoState.ScreenMirroringAlert -> AppleGlyph.ScreenMirroring to IslandColors.CapsuleCyan
    IslandDemoState.TransitRouteAlert -> AppleGlyph.TransitTrain to IslandColors.CapsuleRed
    IslandDemoState.FlightCompact,
    IslandDemoState.FlightExpanded -> AppleGlyph.Airplane to IslandColors.CapsuleCyan
    IslandDemoState.DeliveryCompact,
    IslandDemoState.DeliveryExpanded -> AppleGlyph.Location to IslandColors.Orange
    IslandDemoState.SportsCompact,
    IslandDemoState.SportsExpanded -> AppleGlyph.Star to IslandColors.Purple
    IslandDemoState.SatelliteConnected -> AppleGlyph.Satellite to IslandColors.Green
    IslandDemoState.FindMyAlert -> AppleGlyph.Location to IslandColors.Green
    else -> AppleGlyph.Sparkles to IslandColors.CapsuleCyan
}

// =============================================================================
// REUSABLE COMPACT LAYOUT
// =============================================================================

@Composable
internal fun CompactIslandLayout(
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .wrapContentSize(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart
        ) {
            leading()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .wrapContentSize(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
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
    // Owner reference is 108×32 R15.9018 empty red #DC0917 at 60% opacity, zero content.
    Box(modifier = Modifier.fillMaxSize())
}

@Composable
private fun MusicCompactContent(track: MusicTrack?, isPlaying: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading: Album art 22dp squircle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
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

        Spacer(modifier = Modifier.weight(1f, fill = false))

        // Trailing: Reference equalizer (geometry measured off Minimal.svg / DI-11)
        LiveEqualizerMini(width = 17.dp, height = 19.dp, animated = isPlaying)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading: Phone handset glyph + Call Duration matching Figma Call Time.png
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AppleIcon(
                glyph = AppleGlyph.Phone,
                tint = IslandColors.CapsuleGreen,
                size = 13.dp
            )
            val durationText = when {
                isDialing -> "Calling…"
                showDuration -> {
                    val m = (durationSeconds / 60L).coerceAtLeast(0L)
                    val s = (durationSeconds % 60L).coerceAtLeast(0L)
                    "%d:%02d".format(m, s)
                }
                else -> "0:00"
            }
            Text(
                text = durationText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = IslandColors.CapsuleGreen,
                fontFamily = IslandReferenceFonts.Text,
                maxLines = 1,
                softWrap = false
            )
        }

        // Trailing: Live audio waveform animation (orange/green/white/orange bars)
        if (!isDialing && showDuration) {
            WaveformAnimation(
                modifier = Modifier.size(width = 22.dp, height = 13.dp),
                gradient = listOf(
                    Color(0xFFFF9500),
                    IslandColors.CapsuleGreen,
                    Color.White,
                    Color(0xFFFF9500)
                ),
                animated = true
            )
        }
    }
}

@Composable
private fun CallExpandedContent(
    onControlAction: (IslandControlAction) -> Unit = {},
    contact: ContactInfo? = null,
    canDecline: Boolean = true
) {
    IncomingCallIsland(
        name = contact?.name ?: "Tamia Castillo",
        label = "Mobile",
        avatarUri = contact?.avatarUri,
        onAccept = { onControlAction(IslandControlAction.CallAccept) },
        onDecline = { onControlAction(IslandControlAction.CallDecline) },
        canDecline = canDecline
    )
}

// -----------------------------------------------------------------------------
// 3. NOTIFICATION ALERT (WHATSAPP / MESSAGES)
// -----------------------------------------------------------------------------
internal fun compactNotificationLabel(notification: NotificationInfo?, unseenCount: Int): String = when {
    notification?.isOngoing == true && notification.progressMax > 0 ->
        "${(notification.progress.toLong() * 100 / notification.progressMax).coerceIn(0, 100)}%"
    unseenCount > 99 -> "99+"
    else -> unseenCount.coerceAtLeast(0).toString()
}

@Composable
private fun NotificationCompactContent(notification: NotificationInfo?, unseenCount: Int) {
    val tokens = ai.emots.kishan_dynamic.ui.theme.AppTheme.island
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PackageIcon(
            packageName = notification?.packageName,
            modifier = Modifier.size(tokens.compactIconSize).clip(CircleShape).islandInspectableRegion("Icon"),
            contentDescription = "${notification?.appName ?: "Notification"} icon"
        )
        if (notification?.isOngoing == true && notification.progressMax > 0)
            Text(compactNotificationLabel(notification, unseenCount), style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.caption,
                color = Color.White, maxLines = 1)
        else NotificationCountSymbol(unseenCount)
    }
}

@Composable
private fun NotificationStackedMainContent(
    notifications: List<NotificationInfo>, activeIndex: Int, unseenCount: Int
) = NotificationCompactContent(notifications.getOrNull(activeIndex) ?: notifications.firstOrNull(), unseenCount)

@Composable
private fun NotificationStackedSideContent(
    notifications: List<NotificationInfo>,
    activeIndex: Int
) {
    val secondary = if (notifications.size > 1) {
        notifications[(activeIndex.coerceIn(0, notifications.lastIndex) + 1) % notifications.size]
    } else {
        null
    }
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

@Composable
private fun NotificationWithMusicCompactContent(
    notifications: List<NotificationInfo>,
    activeIndex: Int,
    track: MusicTrack?,
    isPlaying: Boolean,
    unseenCount: Int
) {
    val notification = notifications.getOrNull(activeIndex) ?: notifications.firstOrNull()
    if (isPlaying) {
        CompactIslandLayout(
            leading = {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
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
                        AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 14.dp)
                    }
                }
            },
            trailing = {
                LiveEqualizerMini(width = 17.dp, height = 19.dp, animated = isPlaying)
            }
        )
    } else {
        CompactIslandLayout(
            leading = {
                if (notification != null) {
                    PackageIcon(
                        packageName = notification.packageName,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape),
                        contentDescription = "${notification.appName} icon"
                    )
                }
            },
            trailing = {
                if (unseenCount > 0) {
                    Text(
                        text = unseenCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C))
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }
        )
    }
}

@Composable
private fun NotificationWithMusicSideContent(
    notifications: List<NotificationInfo>,
    activeIndex: Int,
    track: MusicTrack?,
    isPlaying: Boolean,
    unseenCount: Int
) {
    if (isPlaying) {
        val notification = notifications.getOrNull(activeIndex) ?: notifications.firstOrNull()
        Box(modifier = Modifier.size(22.dp)) {
            PackageIcon(
                packageName = notification?.packageName,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentDescription = "${notification?.appName ?: "Notification"} icon"
            )
            if (unseenCount > 0) {
                Text(
                    text = unseenCount.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
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
    activeIndex: Int,
    isCardExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    replyingNotificationId: String? = null,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onNotificationAction: (NotificationInfo, NotificationActionInfo) -> Unit,
    onNotificationSelect: (String) -> Unit,
    onNotificationDismiss: (String) -> Unit,
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null
) {
    val displayList = remember(notifications) { notifications }
    if (displayList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notifications",
                color = IslandColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    NotificationIslandExpandedPager(
        notifications = displayList,
        activeIndex = activeIndex,
        isCardExpanded = isCardExpanded,
        onToggleExpand = onToggleExpand?.let { toggle ->
            {
                displayList.getOrNull(activeIndex)?.let { onNotificationSelect(it.id) }
                toggle()
            }
        },
        replyingNotificationId = replyingNotificationId,
        replyAction = replyAction,
        onSendReply = onSendReply,
        onCancelReply = onCancelReply,
        onUserInteraction = {
            displayList.getOrNull(activeIndex)?.let { onNotificationSelect(it.id) }
        },
        onPageChanged = { newIdx ->
            // Pager emits its initial/programmatic page as well as user changes.
            // Do not turn an automatic peek into a sticky interaction on mount.
            if (newIdx in 0..displayList.lastIndex && newIdx != activeIndex) {
                onNotificationSelect(displayList[newIdx].id)
            }
        },
        onActionSelected = { notif, action ->
            onNotificationAction(notif, action)
        },
        onDismiss = { notif ->
            onNotificationDismiss(notif.id)
        },
        onHeightChange = onHeightChange
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
    val largeText = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
    val pulse = if (LocalReducedMotion.current) 1f else {
    val pulseTransition = rememberInfiniteTransition(label = "volume_pulse")
    val animatedPulse by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseScale.coerceIn(1f, 1.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMillis.coerceIn(100, 1000)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "volume_pulse_scale"
    )
        animatedPulse
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        AppleIcon(
            glyph = glyph,
            tint = tint,
            size = 16.dp,
            modifier = Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!largeText) Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = "${(level * 100).toInt()}%", color = tint, fontSize = 10.sp, fontFamily = IslandReferenceFonts.Text, maxLines = 1, softWrap = false)
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
    if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) {
        CompactIslandLayout(
            leading = { AppleIcon(AppleGlyph.Bluetooth, tint = IslandColors.CapsuleCyan,
                size = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.compactIconSize) },
            trailing = { Text(if (isConnecting) "Connecting…" else "Connected", maxLines = 1,
                color = Color.White, style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.caption) }
        )
        return
    }
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

// -----------------------------------------------------------------------------
// 5. BATTERY / FAST CHARGING
// -----------------------------------------------------------------------------
@Composable
private fun ChargingCompactContent(percentage: Int) {
    val value = percentage.coerceIn(0, 100)
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF30D158).copy(alpha = 0.22f))
                    .border(0.5.dp, Color(0xFF30D158).copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Charging, tint = Color(0xFF30D158), size = 12.dp)
            }
        },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$value%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = IslandReferenceFonts.Text
                )
                MiniBatteryCapsule(
                    percentage = value,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(width = 18.dp, height = 9.dp)
                )
            }
        }
    )
}

@Composable
private fun ChargingExpandedContent(percentage: Int) {
    PowerStatusExpanded(percentage, low = false)
}

// -----------------------------------------------------------------------------
// 5B. LOW BATTERY ALERT
// -----------------------------------------------------------------------------
@Composable
private fun LowBatteryCompactContent(percentage: Int) {
    val value = percentage.coerceIn(0, 100)
    CompactIslandLayout(
        leading = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF453A).copy(alpha = 0.22f))
                    .border(0.5.dp, Color(0xFFFF453A).copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Battery, tint = Color(0xFFFF453A), size = 13.dp)
            }
        },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$value%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF453A),
                    fontFamily = IslandReferenceFonts.Text
                )
                MiniBatteryCapsule(
                    percentage = value,
                    tint = Color(0xFFFF453A),
                    modifier = Modifier.size(width = 18.dp, height = 9.dp)
                )
            }
        }
    )
}

@Composable
private fun LowBatteryExpandedContent(percentage: Int) {
    PowerStatusExpanded(percentage, low = true)
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
                        Text(text = "Chipotle Mexican Grill", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Order #8492 • 2 items", color = IslandColors.Gray, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(IslandColors.Orange.copy(alpha = 0.2f))
                        .border(1.dp, IslandColors.Orange.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                ) {
                    Text(text = "ON THE WAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslandColors.Orange)
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
                    Text(text = "Picked up", fontSize = 11.sp, color = IslandColors.Gray)
                    Text(text = "Arriving in 12 mins", fontSize = 11.sp, color = IslandColors.Orange, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Marco • Vespa Sprint • 4.9", fontSize = 12.sp, color = Color.White)
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp * scale)
                    ) {
                        Text(text = subtitle.ifBlank { "San Francisco" }, fontSize = 11.5.sp, color = IslandColors.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        AppleIcon(
                            glyph = AppleGlyph.NavigationRight,
                            tint = IslandColors.Gray,
                            size = 10.dp * scale
                        )
                        Text(text = "New York", fontSize = 11.5.sp, color = IslandColors.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(IslandColors.Green.copy(alpha = 0.2f))
                        .border(1.dp, IslandColors.Green.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                ) {
                    Text(text = "ON TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslandColors.Green)
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
                Text(text = "JFK", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Gate B14 • Terminal 2", fontSize = 11.sp, color = IslandColors.Gray)
                Text(text = "Alt: 36,000 ft • 2h 15m left", fontSize = 11.sp, color = IslandColors.CapsuleCyan, fontWeight = FontWeight.SemiBold)
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
                Text(text = title.ifBlank { "UEFA Champions League" }, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                    Box(modifier = Modifier.size(6.dp * scale).clip(CircleShape).background(IslandColors.Green))
                    Text(text = subtitle.ifBlank { "78' LIVE" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslandColors.Green, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Real Madrid", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Vini Jr 34', Jude 67'", fontSize = 11.sp, color = IslandColors.Gray)
                }

                Text(
                    text = "2  -  1",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Yellow,
                    fontFamily = IslandReferenceFonts.Text
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Man City", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "De Bruyne 51'", fontSize = 11.sp, color = IslandColors.Gray)
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
                Text(text = "Quarter-Final • 2nd Leg (Agg: 4 - 4)", fontSize = 11.sp, color = IslandColors.Gray)
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
    if (!animated || LocalReducedMotion.current) {
        androidx.compose.foundation.Canvas(modifier = modifier.size(width, height)) {
            val side = maxOf(size.width, size.height)
            drawIslandVector(
                vector = ai.emots.kishan_dynamic.ui.icons.FigmaExactGlyphCatalog.Waveform,
                tint = color ?: IslandColors.EqualizerPink, side = side,
                brush = if (color != null) null else Brush.linearGradient(
                    listOf(IslandColors.EqualizerPink, IslandColors.EqualizerBlue),
                    start = androidx.compose.ui.geometry.Offset(12.0056f, 12.0916f),
                    end = androidx.compose.ui.geometry.Offset(25.7094f, 12.0916f)))
        }
    } else WaveformAnimation(modifier = modifier.size(width, height), color = color, animated = true)

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
                    fontFamily = IslandReferenceFonts.Text
                )
                Text(
                    text = " — 30:00",
                    color = IslandColors.Gray,
                    fontSize = 10.sp,
                    fontFamily = IslandReferenceFonts.Text
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
                fontFamily = IslandReferenceFonts.Text
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
                fontFamily = IslandReferenceFonts.Text
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
                fontFamily = IslandReferenceFonts.Text
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
    isDialing: Boolean = false,
    isMutedState: Boolean? = null,
    isSpeakerState: Boolean? = null
) {
    OngoingCallIslandExpanded(
        name = if (variant == OngoingCallVariant.SharedMedia) "Asia Wild Nature" else name,
        avatarUri = if (variant == OngoingCallVariant.SharedMedia) null else avatarUri,
        label = when {
            variant == OngoingCallVariant.SharedMedia -> "For me 〉"
            isDialing -> "Calling…"
            variant == OngoingCallVariant.FaceTime -> "FaceTime Audio"
            else -> "Mobile Call"
        },
        duration = if (isDialing) "Calling…" else duration,
        variant = variant,
        isMutedState = isMutedState,
        isSpeakerState = isSpeakerState,
        onControlAction = onControlAction
    )
}
