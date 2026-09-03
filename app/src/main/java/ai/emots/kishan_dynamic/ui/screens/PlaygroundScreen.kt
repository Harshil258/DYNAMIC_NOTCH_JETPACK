package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.DummyData
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicNotchParent
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class PreviewStateItem(
    val label: String,
    val headline: String,
    val detail: String,
    val indicatorColor: Color,
    val stateGenerator: (Boolean) -> IslandState
)

/**
 * Redesigned from first principles:
 * Minimal, spatial, editorial, and calm.
 * Eliminates all card-based visual noise, neon borders, and dashboard tiles.
 * The Dynamic Island floats freely in open space as the living hero of the experience.
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

    val arePermissionsGranted = hasAccessibility && hasNotification

    var selectedIndex by remember { mutableStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }

    val states = remember {
        listOf(
            PreviewStateItem(
                label = "Music",
                headline = "Music playback",
                detail = "Glass Animals • Heat Waves",
                indicatorColor = Color(0xFF0A84FF),
                stateGenerator = { exp ->
                    IslandState.Music(
                        track = DummyData.sampleMusicTracks[0],
                        isPlaying = true,
                        isExpanded = exp
                    )
                }
            ),
            PreviewStateItem(
                label = "Call",
                headline = "Incoming call",
                detail = "Harshil Vekariya • Mobile",
                indicatorColor = Color(0xFF30D158),
                stateGenerator = {
                    IslandState.IncomingCall(contact = DummyData.sampleContacts[0])
                }
            ),
            PreviewStateItem(
                label = "Active Call",
                headline = "Ongoing call",
                detail = "02:45 • High definition audio",
                indicatorColor = Color(0xFF30D158),
                stateGenerator = { exp ->
                    IslandState.OngoingCall(
                        contact = DummyData.sampleContacts[0],
                        durationSeconds = 165L,
                        isExpanded = exp
                    )
                }
            ),
            PreviewStateItem(
                label = "Alerts",
                headline = "Notification",
                detail = "WhatsApp • 2 new messages",
                indicatorColor = Color(0xFF0A84FF),
                stateGenerator = { exp ->
                    IslandState.Notification(
                        notifications = DummyData.sampleNotifications,
                        isExpanded = exp
                    )
                }
            ),
            PreviewStateItem(
                label = "Charging",
                headline = "Fast charging",
                detail = "85% • SuperVOOC Warp HUD",
                indicatorColor = Color(0xFFFF9F0A),
                stateGenerator = {
                    IslandState.Charging(batteryPercent = 85, isFastCharging = true)
                }
            ),
            PreviewStateItem(
                label = "Low Battery",
                headline = "Low battery",
                detail = "14% remaining",
                indicatorColor = Color(0xFFFF453A),
                stateGenerator = {
                    IslandState.Charging(batteryPercent = 14, isFastCharging = false)
                }
            ),
            PreviewStateItem(
                label = "Silent",
                headline = "Silent mode",
                detail = "Ringer switched off",
                indicatorColor = Color(0xFFFF453A),
                stateGenerator = { exp ->
                    IslandState.RingerMode(
                        mode = RingerModeType.SILENT,
                        isExpanded = exp
                    )
                }
            )
        )
    }

    val currentItem = states[selectedIndex]
    val activeState = remember(selectedIndex, isExpanded) {
        currentItem.stateGenerator(isExpanded)
    }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            // =================================================================
            // 1. MINIMAL FLOATING APP IDENTITY
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White)
                    )
                    AppText(
                        text = "Dynamic Island",
                        style = AppTheme.typography.body,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateVault() },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Settings,
                        tint = Color(0xFF8E8E93),
                        size = 18.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // 2. HERO: DYNAMIC ISLAND LIVING IN OPEN SPACE (NO CARDS)
            // =================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                DynamicNotchParent(
                    state = activeState,
                    onIslandTap = {
                        isExpanded = !isExpanded
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // =================================================================
            // 3. REFINED SYSTEM STATUS READOUT
            // =================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(currentItem.indicatorColor)
                    )
                    AppText(
                        text = currentItem.headline,
                        style = AppTheme.typography.h3,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AppText(
                    text = currentItem.detail,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =================================================================
            // 4. CALM FLOATING STATE SELECTOR
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                states.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) Color(0xFF1C1C1E)
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                selectedIndex = index
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        AppText(
                            text = item.label,
                            style = AppTheme.typography.caption,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF636366)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // =================================================================
            // 5. IN-LINE SETUP (IF PERMISSIONS ARE MISSING, NO RECTANGULAR CARD)
            // =================================================================
            if (!arePermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.warning)
                        )
                        AppText(
                            text = "Setup required",
                            style = AppTheme.typography.body,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AppText(
                        text = "Accessibility and notification access allow the island to hover over your apps.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E22))
                            .clickable { onNavigatePermissions() }
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                    ) {
                        AppText(
                            text = "Enable Dynamic Island",
                            style = AppTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // =================================================================
            // 6. EDITORIAL VERTICAL FEATURE LIST ("EXPERIENCES")
            // =================================================================
            AppText(
                text = "Experiences",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF636366),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            EditorialFeatureRow(
                title = "Notifications",
                subtitle = "Expand important alerts into your island.",
                onClick = onNavigateNotificationSettings
            )
            EditorialDivider()

            EditorialFeatureRow(
                title = "Calls",
                subtitle = "Incoming calls and live call controls.",
                onClick = onNavigateCallSettings
            )
            EditorialDivider()

            EditorialFeatureRow(
                title = "Music & Media",
                subtitle = "Now playing with live visual feedback.",
                onClick = onNavigateMusicSettings
            )
            EditorialDivider()

            EditorialFeatureRow(
                title = "Battery & Charging",
                subtitle = "Battery status and charging animations.",
                onClick = onNavigateBatterySettings
            )
            EditorialDivider()

            EditorialFeatureRow(
                title = "Quick Controls",
                subtitle = "System toggles and rapid shortcuts.",
                onClick = onNavigateQuickControl
            )
            EditorialDivider()

            EditorialFeatureRow(
                title = "Alignment",
                subtitle = "Calibrate island position around camera.",
                onClick = onNavigateDisplaySettings
            )

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 7. SYSTEM SERVICE CONTROL
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIslandEnabled && arePermissionsGranted) AppTheme.colors.success
                                else AppTheme.colors.disabled
                            )
                    )
                    Column {
                        AppText(
                            text = "Dynamic Island",
                            style = AppTheme.typography.body,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AppText(
                            text = if (isIslandEnabled) "Running and available over your apps" else "Service currently paused",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }

                LuxurySwitch(
                    checked = isIslandEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { preferences.setIslandEnabled(checked) }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorialFeatureRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            AppText(
                text = title,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            AppText(
                text = subtitle,
                style = AppTheme.typography.bodySmall,
                color = Color(0xFF8E8E93)
            )
        }

        AppText(
            text = "→",
            style = AppTheme.typography.body,
            color = Color(0xFF48484A),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun EditorialDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0x0EFFFFFF))
    )
}
