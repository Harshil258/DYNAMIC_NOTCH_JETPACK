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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
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
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

/**
 * Live tab — one segmented selector drives both the island stage
 * and the detail card underneath, so the two always agree.
 */
@Composable
fun LiveActivitiesScreen() {
    val activityTypes = listOf(
        "Music", "Delivery", "Sports", "Flight", "Timer", "Navigation",
        "Voice Memo", "Screen Record", "Shortcut", "Focus Mode", "AirDrop",
        "AirPods", "Satellite", "Find My", "Moved to iPhone", "Video Remote",
        "Airplane", "Mirroring", "Mobile Data", "Transit"
    )
    var selectedActivityIndex by remember { mutableIntStateOf(0) }

    var isMusicPlaying by remember { mutableStateOf(true) }
    var musicProgress by remember { mutableFloatStateOf(42f) }
    var timerRemainingSeconds by remember { mutableIntStateOf(5 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var deliveryProgress by remember { mutableFloatStateOf(0.62f) }
    var isDeliveryRunning by remember { mutableStateOf(false) }
    var navigationProgress by remember { mutableFloatStateOf(0f) }
    var isNavigationRunning by remember { mutableStateOf(false) }
    var sportsMinute by remember { mutableIntStateOf(78) }
    var isSportsRunning by remember { mutableStateOf(false) }
    var flightProgress by remember { mutableFloatStateOf(0.46f) }
    var isFlightRunning by remember { mutableStateOf(false) }
    var isCatalogRunning by remember { mutableStateOf(false) }
    val timerRunningByCoordinator by IslandStateManager.timerRunning.collectAsState(initial = false)

    DisposableEffect(Unit) {
        onDispose {
            IslandStateManager.clearLiveActivity("preview-timer")
            IslandStateManager.clearLiveActivity("preview-delivery")
            IslandStateManager.clearLiveActivity("preview-navigation")
            IslandStateManager.clearLiveActivity("preview-sports")
            IslandStateManager.clearLiveActivity("preview-flight")
            IslandStateManager.clearLiveActivity("preview-catalog")
        }
    }

    LaunchedEffect(selectedActivityIndex) {
        if (selectedActivityIndex != 4) {
            isTimerRunning = false
            IslandStateManager.clearLiveActivity("preview-timer")
        }
        if (selectedActivityIndex != 1) {
            isDeliveryRunning = false
            IslandStateManager.clearLiveActivity("preview-delivery")
        }
        if (selectedActivityIndex != 5) {
            isNavigationRunning = false
            IslandStateManager.clearLiveActivity("preview-navigation")
        }
        if (selectedActivityIndex != 2) {
            isSportsRunning = false
            IslandStateManager.clearLiveActivity("preview-sports")
        }
        if (selectedActivityIndex != 3) {
            isFlightRunning = false
            IslandStateManager.clearLiveActivity("preview-flight")
        }
        if (selectedActivityIndex < 6) {
            isCatalogRunning = false
            IslandStateManager.clearLiveActivity("preview-catalog")
        }
    }

    LaunchedEffect(isCatalogRunning, selectedActivityIndex) {
        if (selectedActivityIndex >= 6 && isCatalogRunning) {
            IslandStateManager.postLiveActivity(catalogActivity(selectedActivityIndex - 6))
        } else {
            IslandStateManager.clearLiveActivity("preview-catalog")
        }
    }

    LaunchedEffect(isTimerRunning, timerRunningByCoordinator, selectedActivityIndex) {
        if (selectedActivityIndex == 4 && isTimerRunning) {
            while (timerRemainingSeconds > 0) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = "preview-timer",
                        kind = LiveActivityKind.TIMER,
                        title = "Timer",
                        subtitle = formatTimer(timerRemainingSeconds),
                        progress = timerRemainingSeconds / (5f * 60f),
                        isExpanded = true,
                        isRunning = timerRunningByCoordinator
                    )
                )
                kotlinx.coroutines.delay(1000L)
                if (timerRunningByCoordinator) timerRemainingSeconds -= 1
            }
            isTimerRunning = false
            IslandStateManager.clearLiveActivity("preview-timer")
        }
    }

    LaunchedEffect(isDeliveryRunning, selectedActivityIndex) {
        if (selectedActivityIndex == 1 && isDeliveryRunning) {
            while (deliveryProgress < 1f) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = "preview-delivery",
                        kind = LiveActivityKind.DELIVERY,
                        title = "Delivery",
                        subtitle = deliverySubtitle(deliveryProgress),
                        progress = deliveryProgress,
                        isExpanded = true
                    )
                )
                kotlinx.coroutines.delay(1500L)
                deliveryProgress = (deliveryProgress + 0.06f).coerceAtMost(1f)
            }
            IslandStateManager.postLiveActivity(
                LiveActivityInfo(
                    id = "preview-delivery",
                    kind = LiveActivityKind.DELIVERY,
                    title = "Delivery",
                    subtitle = "Delivered",
                    progress = 1f,
                    isExpanded = true
                )
            )
            isDeliveryRunning = false
        }
    }

    LaunchedEffect(isNavigationRunning, selectedActivityIndex) {
        if (selectedActivityIndex == 5 && isNavigationRunning) {
            while (navigationProgress < 1f) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = "preview-navigation",
                        kind = LiveActivityKind.NAVIGATION,
                        title = "Directions",
                        subtitle = navigationSubtitle(navigationProgress),
                        progress = navigationProgress,
                        isExpanded = true
                    )
                )
                kotlinx.coroutines.delay(1200L)
                navigationProgress = (navigationProgress + 0.08f).coerceAtMost(1f)
            }
            IslandStateManager.postLiveActivity(
                LiveActivityInfo(
                    id = "preview-navigation",
                    kind = LiveActivityKind.NAVIGATION,
                    title = "Directions",
                    subtitle = "Arrived",
                    progress = 1f,
                    isExpanded = true
                )
            )
            isNavigationRunning = false
        }
    }

    LaunchedEffect(isSportsRunning, selectedActivityIndex) {
        if (selectedActivityIndex == 2 && isSportsRunning) {
            while (sportsMinute < 90) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = "preview-sports",
                        kind = LiveActivityKind.SPORTS,
                        title = "Champions League",
                        subtitle = sportsSubtitle(sportsMinute),
                        progress = sportsMinute / 90f,
                        isExpanded = true
                    )
                )
                kotlinx.coroutines.delay(2000L)
                sportsMinute += 1
            }
            IslandStateManager.postLiveActivity(
                LiveActivityInfo(
                    id = "preview-sports",
                    kind = LiveActivityKind.SPORTS,
                    title = "Champions League",
                    subtitle = "Full time · 2–1",
                    progress = 1f,
                    isExpanded = true
                )
            )
            isSportsRunning = false
        }
    }

    LaunchedEffect(isFlightRunning, selectedActivityIndex) {
        if (selectedActivityIndex == 3 && isFlightRunning) {
            while (flightProgress < 1f) {
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = "preview-flight",
                        kind = LiveActivityKind.FLIGHT,
                        title = "DL 492",
                        subtitle = flightSubtitle(flightProgress),
                        progress = flightProgress,
                        isExpanded = true
                    )
                )
                kotlinx.coroutines.delay(1800L)
                flightProgress = (flightProgress + 0.05f).coerceAtMost(1f)
            }
            IslandStateManager.postLiveActivity(
                LiveActivityInfo(
                    id = "preview-flight",
                    kind = LiveActivityKind.FLIGHT,
                    title = "DL 492",
                    subtitle = "Landed at JFK",
                    progress = 1f,
                    isExpanded = true
                )
            )
            isFlightRunning = false
        }
    }

    val activeIslandState = when (selectedActivityIndex) {
        0 -> if (isMusicPlaying) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
        1 -> IslandDemoState.DeliveryExpanded
        2 -> if (isSportsRunning) IslandDemoState.SportsExpanded else IslandDemoState.SportsCompact
        3 -> if (isFlightRunning) IslandDemoState.FlightExpanded else IslandDemoState.FlightCompact
        4 -> if (isTimerRunning) IslandDemoState.TimerExpanded else IslandDemoState.TimerCompact
        5 -> if (isNavigationRunning) IslandDemoState.ColorOptions else IslandDemoState.NavigationCompact
        else -> catalogDemoState(selectedActivityIndex - 6)
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
                timerLabel = formatTimer(timerRemainingSeconds),
                timerProgress = (timerRemainingSeconds / (5f * 60f)).coerceIn(0f, 1f),
                timerRunning = timerRunningByCoordinator,
                onTap = {
                    if (selectedActivityIndex == 0) isMusicPlaying = !isMusicPlaying
                },
                onControlAction = { action ->
                    when (action) {
                        IslandControlAction.TimerToggle -> {
                            if (!isTimerRunning) {
                                isTimerRunning = true
                                IslandStateManager.setTimerRunning(true)
                            } else {
                                IslandStateManager.toggleTimer()
                            }
                        }
                        IslandControlAction.TimerCancel -> {
                            isTimerRunning = false
                            IslandStateManager.cancelTimer()
                            IslandStateManager.clearLiveActivity("preview-timer")
                        }
                        IslandControlAction.MusicPlayPause -> isMusicPlaying = !isMusicPlaying
                        IslandControlAction.ScreenMirroringStop,
                        IslandControlAction.MobileDataOk,
                        IslandControlAction.TransitEndRoute,
                        IslandControlAction.VoiceMemoStop,
                        IslandControlAction.ScreenRecordingStop,
                        IslandControlAction.AirDropPause,
                        IslandControlAction.MovedUndo -> {
                            isCatalogRunning = false
                            IslandStateManager.clearLiveActivity("preview-catalog")
                        }
                        else -> Unit
                    }
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

                1 -> DeliveryActivityCard(
                    progress = deliveryProgress,
                    isRunning = isDeliveryRunning,
                    onToggle = {
                        if (deliveryProgress >= 1f) deliveryProgress = 0.62f
                        isDeliveryRunning = !isDeliveryRunning
                    },
                    onReset = {
                        isDeliveryRunning = false
                        deliveryProgress = 0.62f
                        IslandStateManager.clearLiveActivity("preview-delivery")
                    }
                )
                2 -> SportsActivityCard(
                    minute = sportsMinute,
                    isRunning = isSportsRunning,
                    onToggle = {
                        if (sportsMinute >= 90) sportsMinute = 0
                        isSportsRunning = !isSportsRunning
                    },
                    onReset = {
                        isSportsRunning = false
                        sportsMinute = 78
                        IslandStateManager.clearLiveActivity("preview-sports")
                    }
                )
                3 -> FlightActivityCard(
                    progress = flightProgress,
                    isRunning = isFlightRunning,
                    onToggle = {
                        if (flightProgress >= 1f) flightProgress = 0.46f
                        isFlightRunning = !isFlightRunning
                    },
                    onReset = {
                        isFlightRunning = false
                        flightProgress = 0.46f
                        IslandStateManager.clearLiveActivity("preview-flight")
                    }
                )
                4 -> TimerActivityCard(
                    remainingSeconds = timerRemainingSeconds,
                    isRunning = isTimerRunning && timerRunningByCoordinator,
                    onToggle = {
                        if (timerRemainingSeconds <= 0) timerRemainingSeconds = 5 * 60
                        if (!isTimerRunning) {
                            isTimerRunning = true
                            IslandStateManager.setTimerRunning(true)
                        } else {
                            IslandStateManager.toggleTimer()
                        }
                    },
                    onReset = {
                        isTimerRunning = false
                        timerRemainingSeconds = 5 * 60
                        IslandStateManager.cancelTimer()
                        IslandStateManager.clearLiveActivity("preview-timer")
                    }
                )

                5 -> NavigationActivityCard(
                    progress = navigationProgress,
                    isRunning = isNavigationRunning,
                    onToggle = {
                        if (navigationProgress >= 1f) navigationProgress = 0f
                        isNavigationRunning = !isNavigationRunning
                    },
                    onReset = {
                        isNavigationRunning = false
                        navigationProgress = 0f
                        IslandStateManager.clearLiveActivity("preview-navigation")
                    }
                )
                else -> CatalogActivityCard(
                    title = activityTypes[index],
                    kind = catalogKind(index - 6),
                    isRunning = isCatalogRunning,
                    onToggle = { isCatalogRunning = !isCatalogRunning },
                    onReset = {
                        isCatalogRunning = false
                        IslandStateManager.clearLiveActivity("preview-catalog")
                    }
                )
            }
        }

        AppFootnote("Live Activities are previews. Real events from supported apps appear automatically.")
    }
}

private fun catalogKind(index: Int): LiveActivityKind = when (index) {
    0 -> LiveActivityKind.VOICE_MEMO
    1 -> LiveActivityKind.SCREEN_RECORDING
    2 -> LiveActivityKind.SHORTCUT
    3 -> LiveActivityKind.FOCUS_MODE
    4 -> LiveActivityKind.AIR_DROP
    5 -> LiveActivityKind.AIRPODS
    6 -> LiveActivityKind.SATELLITE
    7 -> LiveActivityKind.FIND_MY
    8 -> LiveActivityKind.MOVED_TO_IPHONE
    9 -> LiveActivityKind.VIDEO_REMOTE
    10 -> LiveActivityKind.AIRPLANE_ALERT
    11 -> LiveActivityKind.SCREEN_MIRRORING_ALERT
    12 -> LiveActivityKind.MOBILE_DATA_ALERT
    else -> LiveActivityKind.TRANSIT_ROUTE_ALERT
}

private fun catalogDemoState(index: Int): IslandDemoState = when (catalogKind(index)) {
    LiveActivityKind.VOICE_MEMO -> IslandDemoState.VoiceMemoExpanded
    LiveActivityKind.SCREEN_RECORDING -> IslandDemoState.ScreenRecordingExpanded
    LiveActivityKind.SHORTCUT -> IslandDemoState.ShortcutExpanded
    LiveActivityKind.FOCUS_MODE -> IslandDemoState.FocusModeExpanded
    LiveActivityKind.AIR_DROP -> IslandDemoState.AirDropActivity
    LiveActivityKind.AIRPODS -> IslandDemoState.AirPodsConnected
    LiveActivityKind.SATELLITE -> IslandDemoState.SatelliteConnected
    LiveActivityKind.FIND_MY -> IslandDemoState.FindMyAlert
    LiveActivityKind.MOVED_TO_IPHONE -> IslandDemoState.MovedToIPhone
    LiveActivityKind.VIDEO_REMOTE -> IslandDemoState.VideoRemoteExpanded
    LiveActivityKind.AIRPLANE_ALERT -> IslandDemoState.AirplaneAlert
    LiveActivityKind.SCREEN_MIRRORING_ALERT -> IslandDemoState.ScreenMirroringAlert
    LiveActivityKind.MOBILE_DATA_ALERT -> IslandDemoState.MobileDataAlert
    LiveActivityKind.TRANSIT_ROUTE_ALERT -> IslandDemoState.TransitRouteAlert
    else -> IslandDemoState.Minimal
}

private fun catalogActivity(index: Int): LiveActivityInfo = when (val kind = catalogKind(index)) {
    LiveActivityKind.VOICE_MEMO -> LiveActivityInfo("preview-catalog", kind, "Voice Memos", "Recording · 00:18", isExpanded = true)
    LiveActivityKind.SCREEN_RECORDING -> LiveActivityInfo("preview-catalog", kind, "Screen Recording", "00:18", isExpanded = true)
    LiveActivityKind.SHORTCUT -> LiveActivityInfo("preview-catalog", kind, "Good morning", "Running shortcut", isExpanded = true)
    LiveActivityKind.FOCUS_MODE -> LiveActivityInfo("preview-catalog", kind, "Personal Focus", "On until 7:00 PM", isExpanded = true)
    LiveActivityKind.AIR_DROP -> LiveActivityInfo("preview-catalog", kind, "AirDrop", "Receiving from MacBook Pro", progress = 0.68f, isExpanded = true)
    LiveActivityKind.AIRPODS -> LiveActivityInfo("preview-catalog", kind, "AirPods Pro", "Connected · 92%", isExpanded = true)
    LiveActivityKind.SATELLITE -> LiveActivityInfo("preview-catalog", kind, "Satellite", "Connected", isExpanded = true)
    LiveActivityKind.FIND_MY -> LiveActivityInfo("preview-catalog", kind, "Find My", "AirTag nearby", isExpanded = true)
    LiveActivityKind.MOVED_TO_IPHONE -> LiveActivityInfo("preview-catalog", kind, "Moved to iPhone", "Continue on iPhone", isExpanded = true)
    LiveActivityKind.VIDEO_REMOTE -> LiveActivityInfo("preview-catalog", kind, "Now Playing", "15 second remote", isExpanded = true)
    LiveActivityKind.AIRPLANE_ALERT -> LiveActivityInfo("preview-catalog", kind, "Airplane Mode", "Turn off Airplane Mode", isExpanded = true)
    LiveActivityKind.SCREEN_MIRRORING_ALERT -> LiveActivityInfo("preview-catalog", kind, "Screen Mirroring", "MacBook Pro", isExpanded = true)
    LiveActivityKind.MOBILE_DATA_ALERT -> LiveActivityInfo("preview-catalog", kind, "Mobile Data", "Use Wi-Fi to access data", isExpanded = true)
    LiveActivityKind.TRANSIT_ROUTE_ALERT -> LiveActivityInfo("preview-catalog", kind, "Prague Main Station", "Arrive in 12 min", isExpanded = true)
    else -> LiveActivityInfo("preview-catalog", kind, kind.name, isExpanded = true)
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
                    AppleIcon(glyph = AppleGlyph.Music, tint = AppTheme.colors.onAccent, size = 15.dp)
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
                    tint = AppTheme.colors.onAccent,
                    size = 22.dp
                )
            }
            TransportButton(glyph = AppleGlyph.Forward, onClick = {})
        }
    }
}

@Composable
private fun CatalogActivityCard(
    title: String,
    kind: LiveActivityKind,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val detail = when (kind) {
        LiveActivityKind.VOICE_MEMO -> "Recording controls"
        LiveActivityKind.SCREEN_RECORDING -> "Capture status"
        LiveActivityKind.SHORTCUT -> "Automation progress"
        LiveActivityKind.FOCUS_MODE -> "Focus status"
        LiveActivityKind.AIR_DROP -> "Transfer progress"
        LiveActivityKind.AIRPODS -> "Accessory status"
        LiveActivityKind.SATELLITE -> "Connection status"
        LiveActivityKind.FIND_MY -> "Nearby item alert"
        LiveActivityKind.MOVED_TO_IPHONE -> "Handoff prompt"
        LiveActivityKind.VIDEO_REMOTE -> "Playback remote"
        LiveActivityKind.AIRPLANE_ALERT -> "System alert"
        LiveActivityKind.SCREEN_MIRRORING_ALERT -> "Mirroring status"
        LiveActivityKind.MOBILE_DATA_ALERT -> "Connectivity alert"
        LiveActivityKind.TRANSIT_ROUTE_ALERT -> "Route status"
        else -> "Island preview"
    }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(title, style = AppTheme.typography.h3, color = AppTheme.colors.textPrimary)
                AppText(
                    text = "$detail · app-owned preview",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
            AppStatusPill(
                text = if (isRunning) "Live" else "Ready",
                color = if (isRunning) AppTheme.colors.success else AppTheme.colors.textTertiary
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Stop preview" else "Start preview",
                glyph = if (isRunning) AppleGlyph.Stop else AppleGlyph.Play,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
        }
    }
}

@Composable
private fun TransportButton(glyph: AppleGlyph, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceGradient())
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

@Composable
private fun TimerActivityCard(
    remainingSeconds: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText("Focus timer", style = AppTheme.typography.h3, color = AppTheme.colors.textPrimary)
                AppText(
                    text = if (isRunning) "Live on the island" else "Ready to start",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
            AppStatusPill(
                text = formatTimer(remainingSeconds),
                color = if (isRunning) AppTheme.colors.accent else AppTheme.colors.textTertiary,
                showDot = false
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        AppProgressBar(progress = (remainingSeconds / (5f * 60f)).coerceIn(0f, 1f))
        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Pause" else "Start",
                glyph = if (isRunning) AppleGlyph.Pause else AppleGlyph.Play,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
        }
    }
}

private fun formatTimer(seconds: Int): String =
    "%02d:%02d".format(seconds / 60, seconds % 60)

private fun deliverySubtitle(progress: Float): String = when {
    progress >= 1f -> "Delivered"
    progress >= 0.86f -> "Arriving now"
    progress >= 0.72f -> "On the way"
    else -> "Picked up"
}

private fun navigationSubtitle(progress: Float): String = when {
    progress >= 1f -> "Arrived"
    progress >= 0.72f -> "Almost there · 3 min"
    progress >= 0.36f -> "Continue straight · 8 min"
    else -> "Leave now · 12 min"
}

private fun sportsSubtitle(minute: Int): String =
    if (minute >= 90) "Full time · 2–1" else "$minute' · 2–1"

private fun flightSubtitle(progress: Float): String = when {
    progress >= 1f -> "Landed at JFK"
    progress >= 0.72f -> "35 min remaining"
    progress >= 0.5f -> "1h 20m remaining"
    else -> "2h 15m remaining"
}

// =============================================================================
// DELIVERY
// =============================================================================

@Composable
private fun DeliveryActivityCard(
    progress: Float,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
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
            AppStatusPill(
                text = when {
                    progress >= 1f -> "Delivered"
                    isRunning -> "On the way"
                    else -> "Ready"
                },
                color = when {
                    progress >= 1f -> AppTheme.colors.success
                    isRunning -> AppTheme.colors.warning
                    else -> AppTheme.colors.textTertiary
                }
            )
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
                text = if (progress >= 1f) "Delivered just now" else "Arrives 9:24 PM",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.accent
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppProgressBar(
            progress = progress,
            color = if (progress >= 1f) AppTheme.colors.success else AppTheme.colors.warning
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Pause" else if (progress >= 1f) "Replay" else "Start",
                glyph = if (isRunning) AppleGlyph.Pause else AppleGlyph.Play,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceGradient())
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
// NAVIGATION
// =============================================================================

@Composable
private fun NavigationActivityCard(
    progress: Float,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.NavigationStraight,
                    tint = AppTheme.colors.accent,
                    size = 20.dp
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "Home to Studio",
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary
                )
                AppText(
                    text = navigationSubtitle(progress),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
            AppStatusPill(
                text = if (progress >= 1f) "Arrived" else "12 min",
                color = if (progress >= 1f) AppTheme.colors.success else AppTheme.colors.accent,
                showDot = false
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText("Home", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
            AppText(
                text = if (progress >= 1f) "Studio" else "Studio · 2.4 km",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.accent
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppProgressBar(
            progress = progress,
            color = if (progress >= 1f) AppTheme.colors.success else AppTheme.colors.accent
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Pause" else if (progress >= 1f) "Replay" else "Start",
                glyph = if (isRunning) AppleGlyph.Pause else AppleGlyph.NavigationStraight,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
        }
    }
}

// =============================================================================
// SPORTS
// =============================================================================

@Composable
private fun SportsActivityCard(
    minute: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
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
                        .scale(if (isRunning) pulse else 1f)
                        .clip(CircleShape)
                        .background(AppTheme.colors.error)
                )
                AppText(
                    text = if (minute >= 90) "FULL TIME" else "$minute' ${if (isRunning) "LIVE" else "PAUSED"}",
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
                .background(AppTheme.colors.surfaceGradient())
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

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Pause" else if (minute >= 90) "Replay" else "Start",
                glyph = if (isRunning) AppleGlyph.Pause else AppleGlyph.Play,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
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
private fun FlightActivityCard(
    progress: Float,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
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
            AppStatusPill(
                text = when {
                    progress >= 1f -> "Landed"
                    isRunning -> "In flight"
                    else -> "On time"
                },
                color = if (progress >= 1f) AppTheme.colors.success else AppTheme.colors.accent
            )
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
                .background(AppTheme.colors.surfaceGradient())
                .padding(AppTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FlightStat(label = "Gate", value = "B14")
            FlightStat(label = "Terminal", value = "T2")
            FlightStat(label = "Altitude", value = if (progress >= 1f) "Ground" else "36,000 ft")
            FlightStat(label = "Remaining", value = flightSubtitle(progress).removeSuffix(" remaining"))
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        AppProgressBar(
            progress = progress,
            color = if (progress >= 1f) AppTheme.colors.success else AppTheme.colors.accent
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppButton(
                text = if (isRunning) "Pause" else if (progress >= 1f) "Replay" else "Start",
                glyph = if (isRunning) AppleGlyph.Pause else AppleGlyph.Airplane,
                onClick = onToggle,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
            AppButton(
                text = "Reset",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                fillMaxWidth = false
            )
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
