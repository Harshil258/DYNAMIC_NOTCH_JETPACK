package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ai.emots.kishan_dynamic.data.model.DummyData
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicNotchParent
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
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch

private data class PreviewStateItem(
    val label: String,
    val headline: String,
    val detail: String,
    val supportsExpand: Boolean,
    val stateGenerator: (Boolean) -> IslandState
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
            PreviewStateItem(
                label = "Music",
                headline = "Now playing",
                detail = "Glass Animals · Heat Waves",
                supportsExpand = true,
                stateGenerator = { expanded ->
                    IslandState.Music(
                        track = DummyData.sampleMusicTracks[0],
                        isPlaying = true,
                        isExpanded = expanded
                    )
                }
            ),
            PreviewStateItem(
                label = "Incoming call",
                headline = "Incoming call",
                detail = "Tamia Castillo · Mobile",
                supportsExpand = false,
                stateGenerator = { IslandState.IncomingCall(contact = DummyData.sampleContacts[0]) }
            ),
            PreviewStateItem(
                label = "Active call",
                headline = "Call in progress",
                detail = "02:45 · HD voice",
                supportsExpand = true,
                stateGenerator = { expanded ->
                    IslandState.OngoingCall(
                        contact = DummyData.sampleContacts[0],
                        durationSeconds = 165L,
                        isExpanded = expanded
                    )
                }
            ),
            PreviewStateItem(
                label = "Alerts",
                headline = "New notification",
                detail = "WhatsApp · 2 messages",
                supportsExpand = true,
                stateGenerator = { expanded ->
                    IslandState.Notification(
                        notifications = DummyData.sampleNotifications,
                        isExpanded = expanded
                    )
                }
            ),
            PreviewStateItem(
                label = "Charging",
                headline = "Fast charging",
                detail = "85% · Warp charge",
                supportsExpand = false,
                stateGenerator = { IslandState.Charging(batteryPercent = 85, isFastCharging = true) }
            ),
            PreviewStateItem(
                label = "Low battery",
                headline = "Low battery",
                detail = "14% remaining",
                supportsExpand = false,
                stateGenerator = { IslandState.Charging(batteryPercent = 14, isFastCharging = false) }
            ),
            PreviewStateItem(
                label = "Silent",
                headline = "Silent mode",
                detail = "Ringer switched off",
                supportsExpand = true,
                stateGenerator = { expanded ->
                    IslandState.RingerMode(mode = RingerModeType.SILENT, isExpanded = expanded)
                }
            )
        )
    }

    val currentItem = states[selectedIndex]
    val activeState = remember(selectedIndex, isExpanded) {
        currentItem.stateGenerator(isExpanded)
    }

    val serviceRunning = isIslandEnabled && permissionsGranted

    AppScreen(bottomInset = BottomDockInset) {
        AppLargeTitle(
            title = "Dynamic Island",
            subtitle = "Preview every state before it appears on your screen.",
            trailing = {
                AppIconButton(
                    glyph = AppleGlyph.Settings,
                    onClick = onNavigateVault,
                    contentDescription = "Settings"
                )
            }
        )

        // ---------------------------------------------------------------
        // Live stage
        // ---------------------------------------------------------------
        AppStage(
            caption = if (currentItem.supportsExpand) {
                if (isExpanded) "Tap the island to collapse" else "Tap the island to expand"
            } else null,
            minHeight = 210.dp
        ) {
            DynamicNotchParent(
                state = activeState,
                onIslandTap = { if (currentItem.supportsExpand) isExpanded = !isExpanded }
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

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
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            AppText(
                text = currentItem.detail,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

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
        AppCard {
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
        AppListCard {
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
        AppListCard {
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
