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
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.ActionCustomShortcut
import ai.emots.kishan_dynamic.data.model.ActionAppShortcut
import ai.emots.kishan_dynamic.data.model.IslandActivationPolicy
import ai.emots.kishan_dynamic.data.model.IslandActivationStatus
import ai.emots.kishan_dynamic.ui.components.IslandActivationCard
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.data.analytics.AnalyticsEventNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
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
    "Music", "Music (Paused)", "Video remote" -> "Media"
    "Incoming call", "Active call", "Dialing call", "Call summary", "FaceTime alt", "Shared call", "Call banner" -> "Calls"
    "Alerts", "Stacked alerts", "Alerts + Music" -> "Alerts"
    "Delivery", "Flight", "Sports", "Navigation" -> "Live"
    "Voice memo", "Screen rec", "Shortcuts" -> "Capture"
    "Quick Controls" -> "Controls"
    else -> "System"
}

/**
 * Island tab — the live preview hub.
 * Structure: title → activation card → live stage → state picker → customization.
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
    onNavigateVault: () -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }
    val analyticsTracker = remember { FirebaseAnalyticsTracker(context) }
    val entitlementManager = remember { ai.emots.kishan_dynamic.data.premium.EntitlementManager.getInstance(context) }
    val entitlementSnapshot by entitlementManager.entitlement.collectAsState()
    var showRewardDialog by remember { mutableStateOf(false) }

    val isIslandEnabled by preferences.islandEnabled.collectAsState(initial = true)

    var hasAccessibilityGranted by remember { mutableStateOf(false) }
    var hasAccessibilityConnected by remember { mutableStateOf(false) }
    var hasNotificationGranted by remember { mutableStateOf(false) }
    var hasNotificationConnected by remember { mutableStateOf(false) }

    fun refreshHealth() {
        hasAccessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(context)
        hasAccessibilityConnected = PermissionUtils.isAccessibilityServiceWorking(context)
        hasNotificationGranted = PermissionUtils.isNotificationListenerEnabled(context)
        hasNotificationConnected = PermissionUtils.isNotificationListenerWorking(context)
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

    val activationSnapshot = remember(
        hasAccessibilityGranted,
        hasAccessibilityConnected,
        hasNotificationGranted,
        hasNotificationConnected,
        isIslandEnabled
    ) {
        IslandActivationPolicy.evaluate(
            overlayGranted = hasAccessibilityGranted,
            overlayConnected = hasAccessibilityConnected,
            notificationsGranted = hasNotificationGranted,
            notificationsConnected = hasNotificationConnected,
            islandEnabled = isIslandEnabled
        )
    }

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
                label = "Music (Paused)",
                headline = "Media paused",
                detail = "Album art split bubble · Paused",
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
                label = "Dialing call",
                headline = "Outgoing call",
                detail = "Calling… · Tamia Castillo",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallExpanded
            ),
            PlaygroundStateItem(
                label = "Active call",
                headline = "Call in progress",
                detail = "02:45 · HD voice & audio waveform bubble",
                supportsExpand = true,
                compactState = IslandDemoState.CallCompact,
                expandedState = IslandDemoState.CallExpanded
            ),
            PlaygroundStateItem(
                label = "Call summary",
                headline = "Call ended",
                detail = "04:12 · Redial & Message options",
                supportsExpand = false,
                compactState = IslandDemoState.CallSummaryExpanded,
                expandedState = IslandDemoState.CallSummaryExpanded
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
                headline = "Notification Pager",
                detail = "Horizontal swipeable cards · Maps, Uber, Chrome, Chat",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationCompact,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Stacked alerts",
                headline = "3 pending notifications",
                detail = "Queue indicator bubble · Multi-card swipe",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationStacked,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Alerts + Music",
                headline = "Coexistence split",
                detail = "Track in pill · Alert count badge in bubble",
                supportsExpand = true,
                compactState = IslandDemoState.NotificationWithMusicCompact,
                expandedState = IslandDemoState.NotificationExpanded
            ),
            PlaygroundStateItem(
                label = "Quick Controls",
                headline = "Action Control Center",
                detail = "Wi-Fi, Bluetooth, Flashlight, Sliders & Shortcuts",
                supportsExpand = true,
                compactState = IslandDemoState.Minimal,
                expandedState = IslandDemoState.ActionControlExpanded
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
        IslandDemoState.NotificationStacked,
        IslandDemoState.NotificationWithMusicCompact,
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact,
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting,
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
        IslandDemoState.NotificationStacked,
        IslandDemoState.NotificationWithMusicCompact,
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact,
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting,
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

        IslandDemoState.CallSummaryExpanded -> 168.dp

        IslandDemoState.ActionControlExpanded -> 280.dp

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

    val serviceRunning = activationSnapshot.isRunning
    val categories = remember { listOf("All", "Media", "Calls", "Alerts", "Live", "Capture", "Controls", "System") }
    var selectedCategory by remember { mutableStateOf("All") }
    val visibleStates = remember(selectedCategory, states) {
        states.filter { item ->
            selectedCategory == "All" || item.experienceCategory() == selectedCategory
        }
    }

    AppScreen(
        bottomInset = BottomDockInset
    ) {
        // ---------------------------------------------------------------
        // 1. Product identity & Brand Header
        // ---------------------------------------------------------------
        AppLargeTitle(
            title = "Dynamic Island",
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
        // 1.5. Aurora Pro Status Card (FIRST VIEWPORT DISCOVERABILITY)
        // Shows Pro Activated, 7-day trial countdown, or Ad Pass reward options
        // ---------------------------------------------------------------
        ai.emots.kishan_dynamic.ui.components.HomePremiumStatusCard(
            snapshot = entitlementSnapshot,
            onNavigateToPremium = onNavigateToPremium,
            onOpenRewardDialog = { showRewardDialog = true },
            modifier = Modifier.appReveal(0)
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // ---------------------------------------------------------------
        // 2. Real Island Activation Card (FIRST VIEWPORT PRIORITY)
        // Immediately visible on every supported screen without scrolling.
        // Accurately reflects runtime service connection and user preference.
        // ---------------------------------------------------------------
        IslandActivationCard(
            snapshot = activationSnapshot,
            onPrimaryAction = {
                when (activationSnapshot.status) {
                    IslandActivationStatus.SETUP_REQUIRED,
                    IslandActivationStatus.PERMISSION_NEEDED -> onNavigatePermissions()
                    IslandActivationStatus.SERVICE_PAUSED -> PermissionUtils.openAccessibilitySettings(context)
                    IslandActivationStatus.READY_OFF -> {
                        analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_STATE_TOGGLED, mapOf("enabled" to true))
                        scope.launch { preferences.setIslandEnabled(true) }
                    }
                    IslandActivationStatus.RUNNING -> {
                        analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_STATE_TOGGLED, mapOf("enabled" to false))
                        scope.launch { preferences.setIslandEnabled(false) }
                    }
                    else -> onNavigatePermissions()
                }
            },
            onToggle = { enabled ->
                analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_STATE_TOGGLED, mapOf("enabled" to enabled))
                scope.launch { preferences.setIslandEnabled(enabled) }
            },
            modifier = Modifier.appReveal(0)
        )

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // 3. Live Stage & Interactive Preview (Secondary to actual product activation)
        // ---------------------------------------------------------------
        AppSectionTitle("Live Preview")
        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (currentItem.supportsExpand) {
                if (isExpanded) "Tap the island to collapse" else "Tap the island to expand"
            } else null,
            targetHeight = stageTargetHeight,
            showStatusBar = isCompactPresentation
        ) {
            DynamicIslandPill(
                state = activeState,
                musicIsPlaying = currentItem.label != "Music (Paused)",
                callIsDialing = currentItem.label == "Dialing call",
                callSummary = if (currentItem.label == "Call summary") {
                    CallRecord(
                        id = "call_summary_demo",
                        contactName = "Tamia Castillo",
                        phoneNumber = "+1 (555) 019-2834",
                        startedAtMillis = System.currentTimeMillis() - 252_000L,
                        durationSeconds = 252L,
                        direction = CallDirection.INCOMING
                    )
                } else null,
                actionCustomActions = if (currentItem.label == "Quick Controls") {
                    listOf(
                        ActionCustomShortcut(actionId = "torch", label = "Flashlight", iconKey = "torch", order = 0),
                        ActionCustomShortcut(actionId = "lock", label = "Lock Screen", iconKey = "lock", order = 1)
                    )
                } else emptyList(),
                actionApps = if (currentItem.label == "Quick Controls") {
                    listOf(
                        ActionAppShortcut(packageName = "com.google.android.apps.maps", appName = "Maps", order = 0),
                        ActionAppShortcut(packageName = "com.spotify.music", appName = "Spotify", order = 1),
                        ActionAppShortcut(packageName = "com.whatsapp", appName = "WhatsApp", order = 2)
                    )
                } else emptyList(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        text = currentItem.label,
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
            val isSimulation = currentItem.label in listOf("AirDrop", "Satellite", "Find My", "Moved to iPhone", "Video remote", "Screen mirroring")
            if (isSimulation) {
                AppStatusPill(
                    text = "Simulation",
                    color = AppTheme.colors.info
                )
            } else {
                AppStatusPill(
                    text = if (serviceRunning) "Live Preview" else "Preview only",
                    color = if (serviceRunning) AppTheme.colors.success else AppTheme.colors.textTertiary
                )
            }
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
        // 4. Experience browser — category filtering keeps the screen calm
        // while every audited island remains one tap away.
        // ---------------------------------------------------------------
        AppSectionTitle("Experiences")
        AppListCard(modifier = Modifier.appReveal(2)) {
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
        // 5. Settings grouped like an iOS Settings page.
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
        AppFootnote("Dynamic Island runs securely on-device and respects your system privacy.")
    }

    if (showRewardDialog) {
        ai.emots.kishan_dynamic.ui.dialogs.AdRewardDialog(
            onDismiss = { showRewardDialog = false },
            onNavigateToPremium = onNavigateToPremium
        )
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
