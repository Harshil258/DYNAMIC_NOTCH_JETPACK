package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandColorPicker
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.PrecisePixelControl
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedVerticalOffset by preferences.verticalOffset.collectAsState(
        initial = AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP
    )
    val savedHorizontalOffset by preferences.horizontalOffset.collectAsState(
        initial = AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP
    )
    val savedWidthScale by preferences.widthScale.collectAsState(
        initial = AuroraPreferences.DEFAULT_WIDTH_SCALE
    )
    val savedNotchFlatTop by preferences.notchFlatTop.collectAsState(initial = false)
    val savedNotchBgArgb by preferences.notchBackgroundArgb.collectAsState(initial = 0xFF000000.toInt())
    val savedNotchFgArgb by preferences.notchForegroundArgb.collectAsState(initial = 0xFFFFFFFF.toInt())
    val savedIdleAutoHide by preferences.idleAutoHideEnabled.collectAsState(initial = false)
    val savedIdleTimeout by preferences.idleAutoHideTimeoutSeconds.collectAsState(initial = 10)
    val savedCutoutMode by preferences.cameraCutoutMode.collectAsState(initial = false)
    val savedCutoutSize by preferences.cutoutSizePx.collectAsState(initial = 80)

    var verticalOffset by remember(savedVerticalOffset) {
        mutableFloatStateOf(savedVerticalOffset.toFloat())
    }
    var horizontalOffset by remember(savedHorizontalOffset) {
        mutableFloatStateOf(savedHorizontalOffset.toFloat())
    }
    var widthScale by remember(savedWidthScale) {
        mutableFloatStateOf(savedWidthScale * 100f)
    }
    var selectedCutoutIndex by remember { mutableIntStateOf(1) }

    AppScreen(
        topBar = {
            AppTopBar(
                title = "Notch alignment",
                subtitle = "Fit the island around your camera cutout",
                onBack = onBack
            )
        }
    ) {

        // -------------------------------------------------------------------
        // 1. Alignment stage: simulated punch hole with real-time feedback
        // -------------------------------------------------------------------
        AppSectionTitle("Live Notch Preview")
        AppCard(padding = AppTheme.spacing.md) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.lg))
                    .background(AppTheme.colors.background)
                    .border(
                        0.5.dp,
                        AppTheme.colors.border,
                        RoundedCornerShape(AppTheme.radius.lg)
                    )
            ) {
                // Simulated camera punch hole
                Box(
                    modifier = Modifier
                        .align(
                            when (selectedCutoutIndex) {
                                0 -> Alignment.TopStart
                                2 -> Alignment.TopEnd
                                else -> Alignment.TopCenter
                            }
                        )
                        .offset(
                            x = if (selectedCutoutIndex == 1) 0.dp else 22.dp * (if (selectedCutoutIndex == 0) 1 else -1),
                            y = 14.dp
                        )
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.textPrimary.copy(alpha = 0.25f))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(
                            x = horizontalOffset.dp,
                            y = verticalOffset.dp
                        )
                ) {
                    DynamicIslandPill(
                        state = IslandDemoState.Minimal,
                        horizontalScale = widthScale / 100f,
                        flatTop = savedNotchFlatTop,
                        backgroundArgb = savedNotchBgArgb,
                        foregroundArgb = savedNotchFgArgb,
                        cameraCutoutMode = savedCutoutMode,
                        cutoutSizePx = savedCutoutSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            AppText(
                text = "Adjust the sliders and steppers until the pill sits perfectly around your camera cutout.",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textTertiary
            )
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // Quick Presets
        // -------------------------------------------------------------------
        AppSectionTitle("Quick calibration presets")
        AppCard(modifier = Modifier.appReveal(1)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppText(
                    text = "One-tap presets configured for popular Android cutouts and display shapes.",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppButton(
                        text = "Center Hole",
                        glyph = AppleGlyph.Notch,
                        style = AppButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCutoutIndex = 1
                            verticalOffset = 12f
                            horizontalOffset = 0f
                            widthScale = 100f
                            scope.launch {
                                preferences.setVerticalOffset(12)
                                preferences.setHorizontalOffset(0)
                                preferences.setWidthScale(1.0f)
                                preferences.setNotchFlatTop(false)
                            }
                        }
                    )
                    AppButton(
                        text = "Left Hole",
                        glyph = AppleGlyph.ChevronLeft,
                        style = AppButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCutoutIndex = 0
                            verticalOffset = 12f
                            horizontalOffset = -80f
                            widthScale = 105f
                            scope.launch {
                                preferences.setVerticalOffset(12)
                                preferences.setHorizontalOffset(-80)
                                preferences.setWidthScale(1.05f)
                                preferences.setNotchFlatTop(false)
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppButton(
                        text = "Monobrow",
                        glyph = AppleGlyph.Notch,
                        style = AppButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCutoutIndex = 1
                            verticalOffset = 0f
                            horizontalOffset = 0f
                            widthScale = 115f
                            scope.launch {
                                preferences.setVerticalOffset(0)
                                preferences.setHorizontalOffset(0)
                                preferences.setWidthScale(1.15f)
                                preferences.setNotchFlatTop(true)
                            }
                        }
                    )
                    AppButton(
                        text = "Compact",
                        glyph = AppleGlyph.Controls,
                        style = AppButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCutoutIndex = 1
                            verticalOffset = 10f
                            horizontalOffset = 0f
                            widthScale = 90f
                            scope.launch {
                                preferences.setVerticalOffset(10)
                                preferences.setHorizontalOffset(0)
                                preferences.setWidthScale(0.9f)
                                preferences.setNotchFlatTop(false)
                            }
                        }
                    )
                }
            }
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 2. Camera Cutout Position
        // -------------------------------------------------------------------
        AppSectionTitle("Camera position")
        AppCard(modifier = Modifier.appReveal(2)) {
            AppSegmented(
                options = listOf("Left", "Centre", "Right"),
                selectedIndex = selectedCutoutIndex,
                onOptionSelected = { selectedCutoutIndex = it }
            )
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 3. Notch Style (Normal vs Monobrow)
        // -------------------------------------------------------------------
        AppSectionTitle("Notch shape")
        AppCard(modifier = Modifier.appReveal(2)) {
            AppSegmented(
                options = listOf("Normal (Capsule)", "Monobrow (Flat Top)"),
                selectedIndex = if (savedNotchFlatTop) 1 else 0,
                onOptionSelected = { index ->
                    scope.launch { preferences.setNotchFlatTop(index == 1) }
                }
            )
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 4. Precision Pixel Controls (100% Free Basic Calibration)
        // -------------------------------------------------------------------
        AppSectionTitle("Precision alignment")
        AppCard(modifier = Modifier.appReveal(3)) {
            PrecisePixelControl(
                title = "Distance from top",
                value = verticalOffset.roundToInt(),
                range = -20..80,
                unit = "dp",
                onChange = { value ->
                    verticalOffset = value.toFloat()
                    scope.launch { preferences.setVerticalOffset(value) }
                }
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            PrecisePixelControl(
                title = "Horizontal offset",
                value = horizontalOffset.roundToInt(),
                range = -200..200,
                unit = "dp",
                onChange = { value ->
                    horizontalOffset = value.toFloat()
                    scope.launch { preferences.setHorizontalOffset(value) }
                }
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            PrecisePixelControl(
                title = "Island width",
                value = widthScale.roundToInt(),
                range = 70..140,
                unit = "%",
                onChange = { value ->
                    widthScale = value.toFloat()
                    scope.launch { preferences.setWidthScale(value / 100f) }
                }
            )
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 5. Island Colors
        // -------------------------------------------------------------------
        AppSectionTitle("Island colors")
        AppCard(modifier = Modifier.appReveal(4)) {
            IslandColorPicker(
                title = "Background color",
                argb = savedNotchBgArgb,
                onSave = { color ->
                    scope.launch { preferences.setNotchBackgroundArgb(color) }
                }
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            IslandColorPicker(
                title = "Foreground / Text color",
                argb = savedNotchFgArgb,
                onSave = { color ->
                    scope.launch { preferences.setNotchForegroundArgb(color) }
                }
            )
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 6. Auto-Hide & Camera Cutout Mode
        // -------------------------------------------------------------------
        AppSectionTitle("Display behavior")
        AppCard(modifier = Modifier.appReveal(5)) {
            AppToggleRow(
                title = "Auto-hide when idle",
                subtitle = "Hide notch when there is no active notification or media",
                glyph = AppleGlyph.Moon,
                checked = savedIdleAutoHide,
                onCheckedChange = { checked ->
                    scope.launch { preferences.setIdleAutoHideEnabled(checked) }
                }
            )

            if (savedIdleAutoHide) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                AppSlider(
                    title = "Auto-hide delay",
                    value = savedIdleTimeout.toFloat(),
                    valueRange = 5f..30f,
                    valueFormatter = { "${it.roundToInt()}s" },
                    onValueChange = { value ->
                        scope.launch { preferences.setIdleAutoHideTimeoutSeconds(value.roundToInt()) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            AppToggleRow(
                title = "Camera cutout alignment mode",
                subtitle = "Compress into a circular dot centered on camera hole",
                glyph = AppleGlyph.Camera,
                checked = savedCutoutMode,
                onCheckedChange = { checked ->
                    scope.launch { preferences.setCameraCutoutMode(checked) }
                }
            )

            if (savedCutoutMode) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                PrecisePixelControl(
                    title = "Cutout dot size",
                    value = savedCutoutSize,
                    range = 40..140,
                    unit = "px",
                    onChange = { value ->
                        scope.launch { preferences.setCutoutSizePx(value) }
                    }
                )
            }
        }

        AppSectionSpacer()

        // -------------------------------------------------------------------
        // 7. Reset to Defaults
        // -------------------------------------------------------------------
        AppButton(
            text = "Reset to defaults",
            glyph = AppleGlyph.Reset,
            style = AppButtonStyle.Secondary,
            onClick = {
                verticalOffset = AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP.toFloat()
                horizontalOffset = AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP.toFloat()
                widthScale = 100f
                selectedCutoutIndex = 1
                scope.launch {
                    preferences.resetNotchDisplayToDefaults()
                }
            }
        )
        AppFootnote("All basic notch display & alignment controls are 100% free.")
    }
}
