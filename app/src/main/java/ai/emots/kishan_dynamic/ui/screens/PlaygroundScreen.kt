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
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
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
import kotlinx.coroutines.launch

private data class PlaygroundStateItem(
    val label: String,
    val headline: String,
    val detail: String,
    val supportsExpand: Boolean,
    val compactState: IslandDemoState,
    val expandedState: IslandDemoState
)

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                hasNotification = PermissionUtils.isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionsGranted = hasAccessibility && hasNotification

    var selectedIndex by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }

    val states = remember {
        listOf(
            PlaygroundStateItem(
                label = "Music",
                headline = "Now playing",
                detail = "Glass Animals · Heat Waves",
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
        IslandDemoState.CallAvatars -> 156.dp

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

        IslandDemoState.MusicExpanded -> 238.dp

        IslandDemoState.ColorOptions -> 248.dp

        else -> 156.dp
    }

    val serviceRunning = isIslandEnabled && permissionsGranted

    AppScreen(
        bottomInset = BottomDockInset,
        horizontalGutter = 0.dp
    ) {
        // ---------------------------------------------------------------
        // Live stage (Authentic Full-Bleed Apple iOS 17 Viewport)
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
                }
            )
        }

        // Padded controls deck
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Title Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = "Dynamic Island",
                        style = AppTheme.typography.h2,
                        color = AppTheme.colors.textPrimary
                    )
                    AppText(
                        text = "Preview every state with iOS 17 fidelity.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
                AppIconButton(
                    glyph = AppleGlyph.Settings,
                    onClick = onNavigateVault,
                    contentDescription = "Settings"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive state headline & controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppText(
                    text = currentItem.headline,
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))
                AppText(
                    text = currentItem.detail,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stable Compact / Expanded Toggle Row (height 38dp, no jumping)
                Box(
                    modifier = Modifier.height(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentItem.supportsExpand) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(AppTheme.colors.surfaceVariant)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            AppPillToggle(
                                label = "Compact",
                                selected = !isExpanded,
                                onClick = { isExpanded = false }
                            )
                            AppPillToggle(
                                label = "Expanded",
                                selected = isExpanded,
                                onClick = { isExpanded = true }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(AppTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AppText(
                                text = if (currentItem.compactState == IslandDemoState.Idle) "Hardware Cutout" else "Full Alert Sheet",
                                style = AppTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Chip Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                states.forEachIndexed { index, item ->
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
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = if (serviceRunning) "Island is active" else "Setup required",
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
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
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                AppStatusPill(
                    text = if (serviceRunning) "Ready" else "Action needed",
                    color = if (serviceRunning) AppTheme.colors.success else AppTheme.colors.warning
                )
            }

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
        // Shortcuts
        // ---------------------------------------------------------------
        AppSectionTitle("Experiences")
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

        // ---------------------------------------------------------------
        // Service switch
        // ---------------------------------------------------------------
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
}

@Composable
private fun AppPillToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) AppTheme.colors.surface else Color.Transparent
    val textColor = if (selected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary
    Box(
        modifier = Modifier
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
