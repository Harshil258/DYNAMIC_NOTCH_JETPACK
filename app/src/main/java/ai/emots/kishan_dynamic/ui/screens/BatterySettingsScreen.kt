package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.dialogs.BatteryOptimizationDialog
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.ui.unit.dp

@Composable
fun BatterySettingsScreen(
    onBack: () -> Unit
) {
    var isChargingAnimationEnabled by remember { mutableStateOf(true) }
    var isLowBatteryAlertEnabled by remember { mutableStateOf(true) }
    var isExpandedCharging by remember { mutableStateOf(true) }
    var showOptimizationDialog by remember { mutableStateOf(false) }

    AppScreen {
        AppTopBar(
            title = "Battery & charging",
            subtitle = "Charging animation and low-power alerts",
            onBack = onBack
        )

        AppStage(
            caption = if (isExpandedCharging) "Expanded charging HUD" else "Compact battery pill",
            minHeight = 190.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedCharging) IslandDemoState.ChargingExpanded
                else IslandDemoState.ChargingCompact,
                onTap = { isExpandedCharging = !isExpandedCharging }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Alerts")
        AppListCard {
            AppToggleRow(
                title = "Charging animation",
                subtitle = "Show a pulsing ring and wattage when plugged in",
                glyph = AppleGlyph.Battery,
                checked = isChargingAnimationEnabled,
                onCheckedChange = { isChargingAnimationEnabled = it }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Low battery warning",
                subtitle = "Heads-up reminder at 15% and 10%",
                glyph = AppleGlyph.Power,
                checked = isLowBatteryAlertEnabled,
                onCheckedChange = { isLowBatteryAlertEnabled = it }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Keep the island alive")
        AppListCard {
            AppNavRow(
                title = "Background guardian guide",
                subtitle = "Stop aggressive battery savers from closing the service",
                glyph = AppleGlyph.Shield,
                accent = AppTheme.colors.info,
                onClick = { showOptimizationDialog = true }
            )
        }
        AppFootnote("Samsung, Xiaomi, Vivo and OnePlus devices need this step for reliable behaviour.")

        AppSectionSpacer()

        AppButton(
            text = "Test charging animation",
            glyph = AppleGlyph.Battery,
            onClick = { isExpandedCharging = true }
        )
    }

    if (showOptimizationDialog) {
        BatteryOptimizationDialog(onDismiss = { showOptimizationDialog = false })
    }
}
