package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.notification.NotificationDisplayDurationPolicy
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val autoExpand by preferences.autoExpand.collectAsState(initial = true)
    val duration by preferences.displayDuration.collectAsState(
        initial = NotificationDisplayDurationPolicy.DEFAULT_SECONDS
    )
    val swipeUpDismiss by preferences.swipeUpDismiss.collectAsState(initial = true)
    val isProActive by preferences.isProActive.collectAsState(initial = false)

    var isSimulating by remember { mutableStateOf(false) }

    val durationValues = NotificationDisplayDurationPolicy.options

    LaunchedEffect(isProActive, duration) {
        if (!isProActive && duration !in NotificationDisplayDurationPolicy.freeOptions) {
            preferences.setDisplayDuration(NotificationDisplayDurationPolicy.DEFAULT_SECONDS)
        }
    }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Notifications",
            subtitle = "Heads-up alerts inside the island",
            onBack = onBack
        )

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (autoExpand || isSimulating) "Expanded preview" else "Compact preview",
            minHeight = 190.dp
        ) {
            DynamicIslandPill(
                state = if (autoExpand || isSimulating) IslandDemoState.NotificationExpanded
                else IslandDemoState.NotificationCompact
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Behaviour")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppToggleRow(
                title = "Auto-expand new messages",
                subtitle = "Open the full preview with sender and message text",
                glyph = AppleGlyph.Bell,
                checked = autoExpand,
                onCheckedChange = { scope.launch { preferences.setAutoExpand(it) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Swipe up to dismiss",
                subtitle = "Flick the island upwards to hide it early",
                glyph = AppleGlyph.Expand,
                checked = swipeUpDismiss,
                onCheckedChange = { scope.launch { preferences.setSwipeUpDismiss(it) } }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Display duration")
        AppCard(modifier = Modifier.appReveal(3)) {
            AppText(
                text = "How long the island stays visible before collapsing.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            durationValues.forEachIndexed { index, seconds ->
                val isLocked = !isProActive && seconds !in NotificationDisplayDurationPolicy.freeOptions
                DurationChoiceRow(
                    seconds = seconds,
                    selected = duration == seconds && !isLocked,
                    locked = isLocked,
                    onClick = {
                        if (isLocked) onNavigateToPremium()
                        else scope.launch { preferences.setDisplayDuration(seconds) }
                    }
                )
                if (index < durationValues.lastIndex) AppRowDivider()
            }
        }

        AppSectionSpacer()

        AppButton(
            text = if (isSimulating) "Simulating alert…" else "Test a notification",
            glyph = AppleGlyph.Play,
            enabled = !isSimulating,
            onClick = {
                scope.launch {
                    isSimulating = true
                    delay(3500)
                    isSimulating = false
                }
            }
        )
        AppFootnote("Only apps you allow in system notification access can appear in the island.")
    }
}

@Composable
private fun DurationChoiceRow(
    seconds: Int,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.sm),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            AppText(
                text = "${seconds}s",
                style = AppTheme.typography.body,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (locked) AppTheme.colors.textTertiary else AppTheme.colors.textPrimary
            )
            if (locked) {
                AppStatusPill(text = "PRO", color = AppTheme.colors.gold, showDot = false)
            }
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) AppTheme.colors.accent else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (selected) AppTheme.colors.accent else AppTheme.colors.border,
                    shape = CircleShape
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.onAccent)
                )
            }
        }
    }
}
