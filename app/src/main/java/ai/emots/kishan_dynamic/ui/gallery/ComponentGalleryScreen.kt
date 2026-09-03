package ai.emots.kishan_dynamic.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppChip
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppInfoRow
import ai.emots.kishan_dynamic.ui.kit.AppLargeTitle
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppProgressBar
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTile
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Internal design-system reference. Every component here is the same one
 * the product screens use, so it doubles as a light/dark regression check.
 */
@Composable
fun ComponentGalleryScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var switchOn by remember { mutableStateOf(true) }
    var segmentIndex by remember { mutableIntStateOf(0) }
    var sliderValue by remember { mutableFloatStateOf(60f) }
    var chipIndex by remember { mutableIntStateOf(1) }
    var tileOn by remember { mutableStateOf(true) }

    AppScreen {
        AppLargeTitle(
            title = "Design system",
            subtitle = "Every shared component, in one place.",
            trailing = {
                AppStatusPill(
                    text = if (isDarkTheme) "Dark" else "Light",
                    color = AppTheme.colors.accent,
                    showDot = false,
                    modifier = Modifier.clip(RoundedCornerShape(AppTheme.radius.pill))
                )
            }
        )

        AppButton(
            text = if (isDarkTheme) "Switch to light theme" else "Switch to dark theme",
            glyph = AppleGlyph.Sparkles,
            style = AppButtonStyle.Secondary,
            onClick = onToggleTheme
        )

        AppSectionSpacer()

        AppSectionTitle("Buttons")
        AppCard {
            AppButton(text = "Primary", glyph = AppleGlyph.Check, onClick = {})
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppButton(text = "Secondary", style = AppButtonStyle.Secondary, onClick = {})
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppButton(text = "Tonal", style = AppButtonStyle.Tonal, onClick = {})
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppButton(text = "Destructive", style = AppButtonStyle.Destructive, onClick = {})
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppButton(text = "Disabled", enabled = false, onClick = {})
        }

        AppSectionSpacer()

        AppSectionTitle("Rows")
        AppListCard {
            AppNavRow(
                title = "Navigation row",
                subtitle = "With subtitle, value and chevron",
                value = "Value",
                glyph = AppleGlyph.Settings,
                accent = AppTheme.colors.accent,
                onClick = {}
            )
            AppRowDivider()
            AppToggleRow(
                title = "Toggle row",
                subtitle = "With a trailing switch",
                glyph = AppleGlyph.Power,
                checked = switchOn,
                onCheckedChange = { switchOn = it }
            )
            AppRowDivider()
            AppInfoRow(title = "Info row", value = "Read only")
        }

        AppSectionSpacer()

        AppSectionTitle("Selection")
        AppCard {
            AppSegmented(
                options = listOf("One", "Two", "Three"),
                selectedIndex = segmentIndex,
                onOptionSelected = { segmentIndex = it }
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                listOf("Music", "Calls", "Alerts", "Battery").forEachIndexed { index, label ->
                    AppChip(
                        label = label,
                        selected = chipIndex == index,
                        onClick = { chipIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                AppTile(
                    label = "Wi-Fi",
                    glyph = AppleGlyph.Wifi,
                    selected = tileOn,
                    modifier = Modifier.weight(1f),
                    onClick = { tileOn = !tileOn }
                )
                AppTile(
                    label = "Torch",
                    glyph = AppleGlyph.Torch,
                    selected = !tileOn,
                    modifier = Modifier.weight(1f),
                    onClick = { tileOn = !tileOn }
                )
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Values")
        AppCard {
            AppSlider(
                title = "Slider",
                value = sliderValue,
                valueRange = 0f..100f,
                valueFormatter = { "${it.toInt()}%" },
                onValueChange = { sliderValue = it }
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppProgressBar(progress = sliderValue / 100f)
        }

        AppSectionSpacer()

        AppSectionTitle("Status")
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                AppStatusPill(text = "Success", color = AppTheme.colors.success)
                AppStatusPill(text = "Warning", color = AppTheme.colors.warning)
                AppStatusPill(text = "Error", color = AppTheme.colors.error)
                AppStatusPill(text = "Info", color = AppTheme.colors.info)
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Palette")
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                ColorSwatch("Bg", AppTheme.colors.background)
                ColorSwatch("Surface", AppTheme.colors.surface)
                ColorSwatch("Elevated", AppTheme.colors.surfaceElevated)
                ColorSwatch("Accent", AppTheme.colors.accent)
                ColorSwatch("Success", AppTheme.colors.success)
                ColorSwatch("Warning", AppTheme.colors.warning)
                ColorSwatch("Error", AppTheme.colors.error)
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Type scale")
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                AppText("Heading 1", style = AppTheme.typography.h1)
                AppText("Heading 2", style = AppTheme.typography.h2)
                AppText("Heading 3", style = AppTheme.typography.h3)
                AppText("Body text", style = AppTheme.typography.body)
                AppText("Body small", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                AppText("Caption", style = AppTheme.typography.caption, color = AppTheme.colors.textTertiary)
            }
        }

        AppFootnote("Everything on this screen reads from AppTheme tokens only.")
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(color)
                .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.md))
        )
        AppText(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
    }
}
