package ai.emots.kishan_dynamic.ui.gallery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppChip
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppInfoRow
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.kit.AppProgressBar
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTile
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient

private enum class GallerySurface(
    val label: String,
    val description: String
) {
    Toggle("Toggle", "Animated binary control with a clear state."),
    Slider("Slider", "Continuous value control with a readable value."),
    BottomTabs("Bottom tabs", "Selection that preserves context while switching."),
    Dialog("Dialog", "Focused decision surface with scrim and dismiss behavior."),
    ControlCenter("Control center", "Compact system controls with live-looking feedback."),
    Magnifier("Magnifier", "A tactile zoom-style control for precise selection."),
    Glass("Glass playground", "Gradient glass surfaces tuned from shared tokens."),
    Luminance("Adaptive luminance", "Surface contrast that remains legible across themes."),
    Blur("Progressive blur", "A calm fade that keeps content readable behind chrome."),
    Scroll("Scroll container", "A bounded scroll surface for focused content."),
    LazyScroll("Lazy scroll", "A long list that renders through a bounded lazy surface.")
}

/**
 * Internal design-system reference. Every component here is the same one
 * the product screens use, so it doubles as a light/dark regression check.
 */
@Composable
fun ComponentGalleryScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit = {}
) {
    var switchOn by remember { mutableStateOf(true) }
    var segmentIndex by remember { mutableIntStateOf(0) }
    var sliderValue by remember { mutableFloatStateOf(60f) }
    var chipIndex by remember { mutableIntStateOf(1) }
    var tileOn by remember { mutableStateOf(true) }
    var selectedSurfaceIndex by remember { mutableIntStateOf(0) }
    var showSurfaceDialog by remember { mutableStateOf(false) }
    var showSurfaceSheet by remember { mutableStateOf(false) }

    AppScreen {
        AppTopBar(
            title = "Component gallery",
            subtitle = "Our interactive reference surface",
            onBack = onBack,
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

        AppSectionTitle("Reference playground")
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                GallerySurface.entries.forEachIndexed { index, surface ->
                    AppChip(
                        label = surface.label,
                        selected = selectedSurfaceIndex == index,
                        onClick = { selectedSurfaceIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            AnimatedContent(
                targetState = GallerySurface.entries[selectedSurfaceIndex],
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "gallery_surface_preview"
            ) { surface ->
                GallerySurfacePreview(
                    surface = surface,
                    onOpenDialog = { showSurfaceDialog = true },
                    onOpenSheet = { showSurfaceSheet = true }
                )
            }
        }

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

    if (showSurfaceDialog) {
        AppDialog(
            onDismiss = { showSurfaceDialog = false },
            title = "Our dialog surface",
            subtitle = "A focused decision in the Aurora language.",
            glyph = AppleGlyph.Sparkles,
            accent = AppTheme.colors.accent
        ) {
            AppText(
                text = "Dialogs use the same radius, gradient, scrim, and typography tokens as the rest of the app.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppButton(
                text = "Confirm",
                glyph = AppleGlyph.Check,
                onClick = { showSurfaceDialog = false }
            )
        }
    }

    if (showSurfaceSheet) {
        AppSheet(
            onDismiss = { showSurfaceSheet = false },
            title = "Our sheet surface",
            subtitle = "Long-form actions stay comfortable to scan.",
            glyph = AppleGlyph.Controls,
            accent = AppTheme.colors.info
        ) {
            AppText(
                text = "This is the same bottom-sheet pattern used by premium, permission, and action surfaces.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppButton(
                text = "Done",
                onClick = { showSurfaceSheet = false }
            )
        }
    }
}

@Composable
private fun GallerySurfacePreview(
    surface: GallerySurface,
    onOpenDialog: () -> Unit,
    onOpenSheet: () -> Unit
) {
    var toggle by remember(surface) { mutableStateOf(true) }
    var value by remember(surface) { mutableFloatStateOf(0.58f) }
    var tab by remember(surface) { mutableIntStateOf(0) }
    val tabs = listOf("Island", "Focus", "Live")
    val treatmentBrush = if (surface == GallerySurface.Luminance && !toggle) {
        Brush.linearGradient(listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surface))
    } else {
        AppTheme.colors.accentGradient()
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        AppText(
            text = surface.description,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary
        )

        when (surface) {
            GallerySurface.Toggle -> {
                AppToggleRow(
                    title = "Adaptive glow",
                    subtitle = if (toggle) "Surface is responding" else "Surface is resting",
                    glyph = AppleGlyph.Sparkles,
                    checked = toggle,
                    onCheckedChange = { toggle = it }
                )
            }

            GallerySurface.Slider -> {
                AppSlider(
                    title = "Intensity",
                    value = value * 100f,
                    valueRange = 0f..100f,
                    valueFormatter = { "${it.toInt()}%" },
                    onValueChange = { value = it / 100f }
                )
            }

            GallerySurface.BottomTabs -> {
                AppSegmented(
                    options = tabs,
                    selectedIndex = tab,
                    onOptionSelected = { tab = it }
                )
                AppStatusPill(
                    text = "Showing ${tabs[tab]}",
                    color = AppTheme.colors.accent,
                    showDot = false
                )
            }

            GallerySurface.Dialog -> AppButton(
                text = "Open dialog preview",
                glyph = AppleGlyph.Info,
                onClick = onOpenDialog
            )

            GallerySurface.ControlCenter -> {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                    AppTile(
                        label = "Wi-Fi",
                        glyph = AppleGlyph.Wifi,
                        selected = toggle,
                        modifier = Modifier.weight(1f),
                        onClick = { toggle = !toggle }
                    )
                    AppTile(
                        label = "Torch",
                        glyph = AppleGlyph.Torch,
                        selected = !toggle,
                        modifier = Modifier.weight(1f),
                        onClick = { toggle = !toggle }
                    )
                }
                AppSlider(
                    title = "Brightness",
                    value = value * 100f,
                    valueRange = 0f..100f,
                    valueFormatter = { "${it.toInt()}%" },
                    onValueChange = { value = it / 100f }
                )
            }

            GallerySurface.Magnifier -> {
                AppText(
                    text = "${(value * 200).toInt()}%",
                    style = AppTheme.typography.h1,
                    color = AppTheme.colors.accent
                )
                AppSlider(
                    title = "Zoom",
                    value = value * 100f,
                    valueRange = 0f..100f,
                    valueFormatter = { "${(it * 2).toInt()}%" },
                    onValueChange = { value = it / 100f }
                )
            }

            GallerySurface.Glass,
            GallerySurface.Luminance,
            GallerySurface.Blur -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clip(RoundedCornerShape(AppTheme.radius.card))
                        .background(treatmentBrush)
                        .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.card)),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = when (surface) {
                            GallerySurface.Glass -> "Gradient glass"
                            GallerySurface.Luminance -> if (toggle) "High contrast" else "Soft contrast"
                            else -> "Progressive fade"
                        },
                        style = AppTheme.typography.h3,
                        color = Color.White
                    )
                }
                AppToggleRow(
                    title = "Live treatment",
                    subtitle = "Tune the surface without leaving the preview.",
                    glyph = AppleGlyph.Sparkles,
                    checked = toggle,
                    onCheckedChange = { toggle = it }
                )
            }

            GallerySurface.Scroll -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    repeat(8) { index ->
                        AppInfoRow(title = "Scroll item ${index + 1}", value = "Token-driven")
                    }
                }
            }

            GallerySurface.LazyScroll -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    items((1..8).toList()) { index ->
                        AppInfoRow(title = "Lazy item $index", value = "Rendered on demand")
                    }
                }
            }
        }

        if (surface != GallerySurface.Dialog) {
            AppButton(
                text = "Preview as bottom sheet",
                style = AppButtonStyle.Secondary,
                glyph = AppleGlyph.Controls,
                onClick = onOpenSheet
            )
        }
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
