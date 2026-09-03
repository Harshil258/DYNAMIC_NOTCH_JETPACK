package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
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
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val autoExpand by preferences.autoExpand.collectAsState(initial = true)
    val duration by preferences.displayDuration.collectAsState(initial = 5)
    val swipeUpDismiss by preferences.swipeUpDismiss.collectAsState(initial = true)

    var isSimulating by remember { mutableStateOf(false) }

    val durationLabels = listOf("2s", "4s", "6s", "8s", "10s")
    val durationValues = listOf(2, 4, 6, 8, 10)
    val selectedDurationIndex = durationValues
        .indexOfFirst { it == duration }
        .let { if (it < 0) 1 else it }

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
            AppSegmented(
                options = durationLabels,
                selectedIndex = selectedDurationIndex,
                onOptionSelected = { index ->
                    scope.launch { preferences.setDisplayDuration(durationValues[index]) }
                }
            )
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
