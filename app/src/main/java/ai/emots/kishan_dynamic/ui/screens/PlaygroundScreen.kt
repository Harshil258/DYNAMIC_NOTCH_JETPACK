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
import ai.emots.kishan_dynamic.ui.components.actionControlExpandedHeight
import ai.emots.kishan_dynamic.data.model.defaultActionSystemTiles
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

    val states = remember { defaultPlaygroundCatalog }

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

        IslandDemoState.ActionControlExpanded -> {
            val pillHeight = actionControlExpandedHeight(
                systemTilesCount = defaultActionSystemTiles.size,
                hasCustomActions = true,
                hasApps = true,
                hasContacts = false
            )
            pillHeight + 34.dp
        }

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

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // ---------------------------------------------------------------
        // 2.5. Aurora Pro Status Card (Compact Membership Status)
        // ---------------------------------------------------------------
        ai.emots.kishan_dynamic.ui.components.HomePremiumStatusCard(
            snapshot = entitlementSnapshot,
            onNavigateToPremium = onNavigateToPremium,
            onOpenRewardDialog = { showRewardDialog = true },
            modifier = Modifier.appReveal(1)
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
                musicTrack = ai.emots.kishan_dynamic.data.model.MusicTrack(
                    "Heat Waves", "Grass Animals", durationMs = 241000L, positionMs = 50000L,
                    albumArtUri = "android.resource://${context.packageName}/${ai.emots.kishan_dynamic.R.drawable.music_album_art}"
                ),
                callContact = ai.emots.kishan_dynamic.data.model.ContactInfo("Tamia Castillo", ""),
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
