package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppLargeTitle
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.theme.AppTheme

private data class FeatureEntry(
    val title: String,
    val description: String,
    val status: String,
    val glyph: AppleGlyph,
    val accent: Color,
    val onClick: () -> Unit
)

/**
 * Features tab — every island behaviour, grouped into two clear sections
 * so the list never reads as one long undifferentiated wall.
 */
@Composable
fun StudioScreen(
    onNavigateDisplay: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateMusic: () -> Unit,
    onNavigateCalls: () -> Unit,
    onNavigateBattery: () -> Unit,
    onNavigateQuickControl: () -> Unit
) {
    val colors = AppTheme.colors

    val behaviours = remember(colors) {
        listOf(
            FeatureEntry(
                title = "Notifications",
                description = "Heads-up banners from your messaging apps",
                status = "On",
                glyph = AppleGlyph.Bell,
                accent = colors.info,
                onClick = onNavigateNotifications
            ),
            FeatureEntry(
                title = "Music & media",
                description = "Album art, visualiser and transport controls",
                status = "On",
                glyph = AppleGlyph.Music,
                accent = colors.accent,
                onClick = onNavigateMusic
            ),
            FeatureEntry(
                title = "Phone calls",
                description = "Caller HUD, live timer and call actions",
                status = "On",
                glyph = AppleGlyph.Phone,
                accent = colors.success,
                onClick = onNavigateCalls
            ),
            FeatureEntry(
                title = "Battery & charging",
                description = "Charging animation and low-power alerts",
                status = "On",
                glyph = AppleGlyph.Battery,
                accent = colors.warning,
                onClick = onNavigateBattery
            )
        )
    }

    val system = remember(colors) {
        listOf(
            FeatureEntry(
                title = "Quick controls",
                description = "System toggles and shortcut tiles",
                status = "7 tiles",
                glyph = AppleGlyph.Controls,
                accent = colors.gold,
                onClick = onNavigateQuickControl
            ),
            FeatureEntry(
                title = "Notch alignment",
                description = "Match the island to your camera cutout",
                status = "Calibrated",
                glyph = AppleGlyph.Notch,
                accent = colors.secondary,
                onClick = onNavigateDisplay
            )
        )
    }

    AppScreen(bottomInset = BottomDockInset) {
        AppLargeTitle(
            title = "Features",
            subtitle = "Choose what the island reacts to and how it looks."
        )

        AppSectionTitle("Island behaviour")
        AppListCard {
            behaviours.forEachIndexed { index, item ->
                AppNavRow(
                    title = item.title,
                    subtitle = item.description,
                    value = item.status,
                    glyph = item.glyph,
                    accent = item.accent,
                    onClick = item.onClick
                )
                if (index < behaviours.lastIndex) AppRowDivider()
            }
        }

        AppSectionSpacer()

        AppSectionTitle("System & layout")
        AppListCard {
            system.forEachIndexed { index, item ->
                AppNavRow(
                    title = item.title,
                    subtitle = item.description,
                    value = item.status,
                    glyph = item.glyph,
                    accent = item.accent,
                    onClick = item.onClick
                )
                if (index < system.lastIndex) AppRowDivider()
            }
        }

        AppFootnote("Each feature has its own live preview so you can see changes before applying them.")
    }
}
