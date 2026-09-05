package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppChip
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppIconButton
import ai.emots.kishan_dynamic.ui.kit.AppLargeTitle
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient
import kotlinx.coroutines.launch

private data class PlaygroundStateItem(
    val label: String,
    val headline: String,
    val detail: String,
    val supportsExpand: Boolean,
    val compactState: IslandDemoState,
    val expandedState: IslandDemoState
)

private fun PlaygroundStateItem.experienceCategory(): String = when (label) {
    "Music", "Video remote" -> "Media"
    "Incoming call", "Active call", "FaceTime alt", "Shared call", "Call banner" -> "Calls"
    "Delivery", "Flight", "Sports", "Navigation" -> "Live"
    "Voice memo", "Screen rec", "Shortcuts" -> "Capture"
    else -> "System"
}

/**
 * Island tab — the live preview hub.
 * Structure: title → live stage → state picker → setup status → shortcuts → service switch.
 */
@Composable
fun PlaygroundScreen(
    onNavigateDisplaySettings: () -> Unit,
    onNavigateNotificationSettings: () -> Unit,
    onNavigateMusicSettings: () -> Unit,
    onNavigateCallSettings: () -> Unit,
    onNavigateBatterySettings: () -> Unit,
    onNavigateSoundSettings: () -> Unit,
    onNavigateQuickControl: () -> Unit,
    onNavigatePermissions: () -> Unit,
    onNavigateVault: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val isIslandEnabled by preferences.islandEnabled.collectAsState(initial = true)

    var hasAccessibility by remember { mutableStateOf(false) }
    var hasNotification by remember { mutableStateOf(false) }

    fun refreshHealth() {
        hasAccessibility = PermissionUtils.isAccessibilityServiceWorking(context)
        hasNotification = PermissionUtils.isNotificationListenerWorking(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        refreshHealth()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshHealth()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                refreshHealth()
            }
        }
        val filter = IntentFilter(PermissionUtils.ACTION_PERMISSIONS_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val permissionsGranted = hasAccessibility && hasNotification

    var selectedIndex by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }

    val states = remember {
        listOf(
            PlaygroundStateItem(
                label = "Music",
                headline = "Now playing",
                detail = "Grass Animals · Heat Waves",
                supportsExpand = true,
                compactState = IslandDemoState.MusicCompact,
                expandedState = IslandDemoState.MusicExpanded
            ),
            PlaygroundStateItem(
                label = "Incoming call",
                headline = "Incoming call",
                detail = "Tamia Castillo · Mobile",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallAvatars
            ),
            PlaygroundStateItem(
                label = "Active call",
                headline = "Call in progress",
                detail = "02:45 · HD voice",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallExpanded
            ),
            PlaygroundStateItem(
                label = "FaceTime alt",
                headline = "FaceTime Audio variant",
                detail = "Alternate active-call control layout",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.FaceTimeCallExpanded
            ),
            PlaygroundStateItem(
                label = "Shared call",
                headline = "Shared media call",
                detail = "Asia Wild Nature · For me",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.SharedMediaCallExpanded
            ),
            PlaygroundStateItem(
                label = "Alerts",
                headline = "New notification",
                detail = "WhatsApp · Tamia Castillo",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationCompact,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Charging",
                headline = "Fast charging",
                detail = "85% · 65W Warp charge",
                supportsExpand = true,
                compactState = IslandDemoState.ChargingCompact,
                expandedState = IslandDemoState.ChargingExpanded
            ),
            PlaygroundStateItem(
                label = "Low battery",
                headline = "Low battery",
                detail = "14% remaining · Connect charger",
                supportsExpand = true,
                compactState = IslandDemoState.LowBatteryCompact,
                expandedState = IslandDemoState.LowBatteryExpanded
            ),
            PlaygroundStateItem(
                label = "Timer",
                headline = "Timer",
                detail = "03:35 remaining",
                supportsExpand = true,
                compactState = IslandDemoState.TimerCompact,
                expandedState = IslandDemoState.TimerExpanded
            ),
            PlaygroundStateItem(
                label = "Silent",
                headline = "Silent mode",
                detail = "Ringer switched off",
                supportsExpand = true,
                compactState = IslandDemoState.SilentModeCompact,
                expandedState = IslandDemoState.SilentModeExpanded
            ),
            PlaygroundStateItem(
                label = "Idle",
                headline = "Idle island",
                detail = "126pt × 36.67pt hardware cutout, exactly as the two device references draw it",
                supportsExpand = false,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.Idle
            ),
            PlaygroundStateItem(
                label = "Delivery",
                headline = "Live activity · Delivery",
                detail = "DoorDash · Order arriving in 12 mins",
                supportsExpand = true,
                compactState = IslandDemoState.DeliveryCompact,
                expandedState = IslandDemoState.DeliveryExpanded
            ),
            PlaygroundStateItem(
                label = "Flight",
                headline = "Flight tracker",
                detail = "United UA 824 · SFO to JFK",
                supportsExpand = true,
                compactState = IslandDemoState.FlightCompact,
                expandedState = IslandDemoState.FlightExpanded
            ),
            PlaygroundStateItem(
                label = "Sports",
                headline = "Live sports",
                detail = "Lakers 108 - 102 Warriors · Q4 2:14",
                supportsExpand = true,
                compactState = IslandDemoState.SportsCompact,
                expandedState = IslandDemoState.SportsExpanded
            ),
            PlaygroundStateItem(
                label = "Navigation",
                headline = "Maps navigation",
                detail = "In 500m · Turn right on Market St",
                supportsExpand = true,
                compactState = IslandDemoState.NavigationCompact,
                expandedState = IslandDemoState.ColorOptions
            ),
            PlaygroundStateItem(
                label = "Mirroring",
                headline = "Screen mirroring",
                detail = "Connected to MacBook Pro",
                supportsExpand = false,
                compactState = IslandDemoState.ScreenMirroringAlert,
                expandedState = IslandDemoState.ScreenMirroringAlert
            ),
            PlaygroundStateItem(
                label = "Transit",
                headline = "Transit alert",
                detail = "Platform 3 · Train leaves in 4m",
                supportsExpand = false,
                compactState = IslandDemoState.TransitRouteAlert,
                expandedState = IslandDemoState.TransitRouteAlert
            ),
            PlaygroundStateItem(
                label = "Voice memo",
                headline = "Recording memo",
                detail = "0:12 · Live waveform trace",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.VoiceMemoExpanded
            ),
            PlaygroundStateItem(
                label = "Screen rec",
                headline = "Screen recording",
                detail = "0:03 · Recording screen session",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.ScreenRecordingExpanded
            ),
            PlaygroundStateItem(
                label = "Shortcuts",
                headline = "Running shortcut",
                detail = "Daily Summary · Automation in progress",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.ShortcutExpanded
            ),
            PlaygroundStateItem(
                label = "Call banner",
                headline = "Ongoing call",
                detail = "Tamia Castillo · 02:45",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.OngoingCallBanner
            ),
            PlaygroundStateItem(
                label = "Focus / DND",
                headline = "Do Not Disturb",
                detail = "Focus mode turned on",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.FocusModeExpanded
            ),
            PlaygroundStateItem(
                label = "Video remote",
                headline = "WWDC23 Keynote",
                detail = "Apple · 15s skip transport controls",
                supportsExpand = true,
                compactState = IslandDemoState.MusicCompact,
                expandedState = IslandDemoState.VideoRemoteExpanded
            ),
            PlaygroundStateItem(
                label = "Airplane mode",
                headline = "Airplane mode alert",
                detail = "Turn off airplane mode to access data",
                supportsExpand = false,
                compactState = IslandDemoState.AirplaneAlert,
                expandedState = IslandDemoState.AirplaneAlert
            ),
            PlaygroundStateItem(
                label = "Mobile data",
                headline = "Mobile data alert",
                detail = "Turn off mobile data to use Wi-Fi",
                supportsExpand = false,
                compactState = IslandDemoState.MobileDataAlert,
                expandedState = IslandDemoState.MobileDataAlert
            ),
            PlaygroundStateItem(
                label = "AirDrop",
                headline = "AirDrop activity",
                detail = "3 Photos · Receiving from Chris",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.AirDropActivity
            ),
            PlaygroundStateItem(
                label = "AirPods",
                headline = "AirPods connected",
                detail = "Ladislav's AirPods · 75% battery",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.AirPodsConnected
            ),
            PlaygroundStateItem(
                label = "Satellite",
                headline = "Satellite connection",
                detail = "Connected · Keep pointing at Satellite",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.SatelliteConnected
            ),
            PlaygroundStateItem(
                label = "Find My",
                headline = "Find My iPhone alert",
                detail = "Target radar pulse locator",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.FindMyAlert
            ),
            PlaygroundStateItem(
                label = "Moved to iPhone",
                headline = "AirPods hand-off",
                detail = "Audio moved to iPhone · Undo",
                supportsExpand = true,
                compactState = IslandDemoState.Idle,
                expandedState = IslandDemoState.MovedToIPhone
            )
        )
    }

    val currentItem = states[selectedIndex]
    val activeState = remember(selectedIndex, isExpanded) {
        if (isExpanded && currentItem.supportsExpand) {
            currentItem.expandedState
        } else if (!currentItem.supportsExpand) {
            currentItem.expandedState
        } else {
            currentItem.compactState
        }
    }

    val isCompactPresentation = when (activeState) {
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

    val stageTargetHeight: Dp = when (activeState) {
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
        IslandDemoState.NavigationCompact -> 116.dp

        IslandDemoState.NotificationExpanded,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.TimerExpanded,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.NotificationImage,
        IslandDemoState.CallAvatars,
        IslandDemoState.OngoingCallBanner,
        IslandDemoState.VoiceMemoExpanded,
        IslandDemoState.ScreenRecordingExpanded,
        IslandDemoState.ShortcutExpanded,
        IslandDemoState.FocusModeExpanded,
        IslandDemoState.AirDropActivity,
        IslandDemoState.AirPodsConnected,
        IslandDemoState.SatelliteConnected,
        IslandDemoState.FindMyAlert,
        IslandDemoState.MovedToIPhone -> 156.dp

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
        IslandDemoState.MovieCard -> 198.dp

        IslandDemoState.CallExpanded -> 218.dp

        IslandDemoState.FaceTimeCallExpanded,
        IslandDemoState.SharedMediaCallExpanded -> 224.dp

        IslandDemoState.MusicExpanded,
        IslandDemoState.VideoRemoteExpanded -> 238.dp

        IslandDemoState.ColorOptions -> 248.dp

        else -> 156.dp
    }

    val serviceRunning = isIslandEnabled && permissionsGranted
    val categories = remember { listOf("All", "Media", "Calls", "Live", "Capture", "System") }
    var selectedCategory by remember { mutableStateOf("All") }
    val visibleStates = states.filter { item ->
        selectedCategory == "All" || item.experienceCategory() == selectedCategory
    }

    AppScreen(
        bottomInset = BottomDockInset
    ) {
        AppLargeTitle(
            title = "Island",
            subtitle = "Live status, controls, and device previews",
            trailing = {
                AppIconButton(
                    glyph = AppleGlyph.Settings,
                    onClick = onNavigateVault,
                    contentDescription = "Open settings"
                )
            }
        )

        // ---------------------------------------------------------------
        // Live stage — the island itself remains unchanged.
        // ---------------------------------------------------------------
        AppStage(
            modifier = Modifier.appReveal(0),
            caption = if (currentItem.supportsExpand) {
                if (isExpanded) "Tap the island to collapse" else "Tap the island to expand"
            } else null,
            targetHeight = stageTargetHeight,
            showStatusBar = isCompactPresentation
        ) {
            DynamicIslandPill(
                state = activeState,
                onTap = {
                    if (currentItem.supportsExpand) {
                        isExpanded = !isExpanded
                    }
                },
                onControlAction = { action ->
                    when (action) {
                        IslandControlAction.CallAccept -> {
                            selectedIndex = states.indexOfFirst { it.label == "Active call" }.coerceAtLeast(0)
                            isExpanded = true
                        }
                        IslandControlAction.CallDecline,
                        IslandControlAction.CallEnd,
                        IslandControlAction.TimerCancel,
                        IslandControlAction.VoiceMemoStop,
                        IslandControlAction.ScreenRecordingStop,
                        IslandControlAction.ScreenMirroringStop,
                        IslandControlAction.MobileDataOk,
                        IslandControlAction.TransitEndRoute -> {
                            selectedIndex = states.indexOfFirst { it.label == "Idle" }.coerceAtLeast(0)
                            isExpanded = true
                        }
                        IslandControlAction.MovedUndo -> {
                            selectedIndex = states.indexOfFirst { it.label == "AirPods" }.coerceAtLeast(0)
                            isExpanded = true
                        }
                        IslandControlAction.OpenSettings,
                        IslandControlAction.MobileDataSettings,
                        IslandControlAction.ShortcutOpen,
                        IslandControlAction.AirPodsOpen,
                        IslandControlAction.SatelliteMessage -> onNavigateQuickControl()
                        else -> Unit
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        // Calm summary row: the selected experience is visible without repeating
        // the large page title or adding another heavy card.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.layout.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "Now previewing",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = currentItem.label,
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary
                )
            }
            AppStatusPill(
                text = if (serviceRunning) "Ready" else "Setup needed",
                color = if (serviceRunning) AppTheme.colors.success else AppTheme.colors.warning
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Stable Compact / Expanded control. This only changes the surrounding
        // playground state; the island implementation itself is untouched.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.zero),
            contentAlignment = Alignment.Center
        ) {
            if (currentItem.supportsExpand) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(percent = 50))
                        .background(AppTheme.colors.surfaceGradient())
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AppPillToggle(
                        label = "Compact",
                        selected = !isExpanded,
                        onClick = { isExpanded = false },
                        modifier = Modifier.weight(1f)
                    )
                    AppPillToggle(
                        label = "Expanded",
                        selected = isExpanded,
                        onClick = { isExpanded = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                AppText(
                    text = if (currentItem.compactState == IslandDemoState.Idle) "Hardware cutout" else "Full alert sheet",
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Experience browser — category filtering keeps the home screen calm
        // while every audited island remains one tap away.
        // ---------------------------------------------------------------
        AppSectionTitle("Preview")
        AppListCard(modifier = Modifier.appReveal(1)) {
            Column(
                modifier = Modifier.padding(
                    horizontal = AppTheme.layout.cardPadding,
                    vertical = AppTheme.spacing.md
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AppText(
                            text = "Choose an experience",
                            style = AppTheme.typography.body,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        )
                        AppText(
                            text = "${states.size} reference states available",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textTertiary
                        )
                    }
                    AppText(
                        text = currentItem.label,
                        style = AppTheme.typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    categories.forEach { category ->
                        AppChip(
                            label = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    visibleStates.forEach { item ->
                        val index = states.indexOf(item)
                        AppChip(
                            label = item.label,
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                isExpanded = true
                            }
                        )
                    }
                }
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Setup status
        // ---------------------------------------------------------------
        AppSectionTitle("Status")
        AppCard(modifier = Modifier.appReveal(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = if (serviceRunning) "Island is active" else "Setup required",
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                AppStatusPill(
                    text = if (serviceRunning) "Ready" else "Action needed",
                    color = if (serviceRunning) AppTheme.colors.success else AppTheme.colors.warning
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            AppText(
                text = if (serviceRunning) {
                    "The island is running above your apps."
                } else {
                    "Grant accessibility and notification access so the island can appear over your apps."
                },
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            if (!permissionsGranted) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                AppButton(
                    text = "Complete setup",
                    onClick = onNavigatePermissions,
                    glyph = AppleGlyph.Shield
                )
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Settings grouped like an iOS Settings page.
        // ---------------------------------------------------------------
        AppSectionTitle("Customize")
        AppListCard(modifier = Modifier.appReveal(3)) {
            AppNavRow(
                title = "Notifications",
                subtitle = "Expand alerts into the island",
                glyph = AppleGlyph.Bell,
                accent = AppTheme.colors.info,
                onClick = onNavigateNotificationSettings
            )
            AppRowDivider()
            AppNavRow(
                title = "Calls",
                subtitle = "Caller HUD and live call controls",
                glyph = AppleGlyph.Phone,
                accent = AppTheme.colors.success,
                onClick = onNavigateCallSettings
            )
            AppRowDivider()
            AppNavRow(
                title = "Music & media",
                subtitle = "Now playing with visualiser",
                glyph = AppleGlyph.Music,
                accent = AppTheme.colors.accent,
                onClick = onNavigateMusicSettings
            )
            AppRowDivider()
            AppNavRow(
                title = "Battery & charging",
                subtitle = "Charging and low-power alerts",
                glyph = AppleGlyph.Battery,
                accent = AppTheme.colors.warning,
                onClick = onNavigateBatterySettings
            )
            AppRowDivider()
            AppNavRow(
                title = "Sound & haptics",
                subtitle = "Ringer, volume HUD, and pulse animation",
                glyph = AppleGlyph.Speaker,
                accent = AppTheme.colors.secondary,
                onClick = onNavigateSoundSettings
            )
            AppRowDivider()
            AppNavRow(
                title = "Quick controls",
                subtitle = "System toggles and shortcuts",
                glyph = AppleGlyph.Controls,
                accent = AppTheme.colors.gold,
                onClick = onNavigateQuickControl
            )
            AppRowDivider()
            AppNavRow(
                title = "Notch alignment",
                subtitle = "Calibrate around your camera",
                glyph = AppleGlyph.Notch,
                accent = AppTheme.colors.secondary,
                onClick = onNavigateDisplaySettings
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Service")
        AppListCard(modifier = Modifier.appReveal(4)) {
            AppToggleRow(
                title = "Enable Dynamic Island",
                subtitle = if (isIslandEnabled) "Running over your apps" else "Currently paused",
                glyph = AppleGlyph.Power,
                checked = isIslandEnabled,
                onCheckedChange = { checked ->
                    scope.launch { preferences.setIslandEnabled(checked) }
                }
            )
        }
        AppFootnote("Turning the service off keeps your settings but hides the island everywhere.")
    }
}

@Composable
private fun AppPillToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) AppTheme.colors.surface else Color.Transparent
    val textColor = if (selected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = label,
            style = AppTheme.typography.caption,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}
