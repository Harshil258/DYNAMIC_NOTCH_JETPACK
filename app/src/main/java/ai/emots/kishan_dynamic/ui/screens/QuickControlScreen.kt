package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppTile
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme

private data class QuickToggle(
    val name: String,
    val glyph: AppleGlyph,
    val isEnabled: Boolean
)

@Composable
fun QuickControlScreen(
    onBack: () -> Unit
) {
    var isQuickControlsEnabled by remember { mutableStateOf(true) }
    var isVolumeHudEnabled by remember { mutableStateOf(true) }

    var toggles by remember {
        mutableStateOf(
            listOf(
                QuickToggle("Wi-Fi", AppleGlyph.Wifi, true),
                QuickToggle("Bluetooth", AppleGlyph.Bluetooth, true),
                QuickToggle("Torch", AppleGlyph.Torch, false),
                QuickToggle("Location", AppleGlyph.Location, true),
                QuickToggle("Rotate", AppleGlyph.Rotate, false),
                QuickToggle("Mute", AppleGlyph.Mute, false),
                QuickToggle("Airplane", AppleGlyph.Airplane, false),
                QuickToggle("Timer", AppleGlyph.Timer, false)
            )
        )
    }

    val activeCount = toggles.count { it.isEnabled }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Quick controls",
            subtitle = "System toggles and the volume HUD",
            onBack = onBack
        )

        AppSectionTitle("Behaviour")
        AppListCard(modifier = Modifier.appReveal(1)) {
            AppToggleRow(
                title = "Control centre on the island",
                subtitle = "Long press the island to open your shortcuts",
                glyph = AppleGlyph.Controls,
                checked = isQuickControlsEnabled,
                onCheckedChange = { isQuickControlsEnabled = it }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Volume & brightness HUD",
                subtitle = "Show a readout when you press the volume keys",
                glyph = AppleGlyph.Speaker,
                checked = isVolumeHudEnabled,
                onCheckedChange = { isVolumeHudEnabled = it }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Shortcut tiles · $activeCount active")
        AppCard(modifier = Modifier.appReveal(2)) {
            AppText(
                text = "Tap a tile to add or remove it from the island control centre.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                toggles.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        rowItems.forEach { item ->
                            AppTile(
                                label = item.name,
                                glyph = item.glyph,
                                selected = item.isEnabled,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    toggles = toggles.map {
                                        if (it.name == item.name) it.copy(isEnabled = !it.isEnabled) else it
                                    }
                                }
                            )
                        }
                        // Keep the last row aligned to the same 4-column grid.
                        repeat(4 - rowItems.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        AppFootnote("Some toggles open the matching system panel on Android 13 and newer.")
    }
}
