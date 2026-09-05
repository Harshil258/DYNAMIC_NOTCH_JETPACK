package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppSlider
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
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val horizontalOffsetEnabled = PremiumFeaturePolicy.horizontalOffsetEnabled(isProActive)

    val savedVerticalOffset by preferences.verticalOffset.collectAsState(
        initial = AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP
    )
    val savedHorizontalOffset by preferences.horizontalOffset.collectAsState(
        initial = AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP
    )
    val savedWidthScale by preferences.widthScale.collectAsState(
        initial = AuroraPreferences.DEFAULT_WIDTH_SCALE
    )

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

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Notch alignment",
            subtitle = "Fit the island around your camera cutout",
            onBack = onBack
        )

        // -------------------------------------------------------------------
        // Alignment stage: a simulated top-of-screen with a real punch hole.
        // -------------------------------------------------------------------
        AppSectionTitle("Preview")
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
                // Simulated camera punch hole.
                Box(
                    modifier = Modifier
                        .align(
                            when (selectedCutoutIndex) {
                                0 -> Alignment.TopStart
                                2 -> Alignment.TopEnd
                                else -> Alignment.TopCenter
                            }
                        )
                        .offset(x = if (selectedCutoutIndex == 1) 0.dp else 22.dp * (if (selectedCutoutIndex == 0) 1 else -1), y = 14.dp)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.textPrimary.copy(alpha = 0.25f))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                    .offset(
                        x = if (horizontalOffsetEnabled) horizontalOffset.dp else AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP.dp,
                        y = verticalOffset.dp
                    )
                ) {
                    DynamicIslandPill(
                        state = IslandDemoState.Minimal,
                        horizontalScale = widthScale / 100f
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            AppText(
                text = "Adjust until the pill sits neatly over your camera.",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textTertiary
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Camera position")
        AppCard(modifier = Modifier.appReveal(1)) {
            AppSegmented(
                options = listOf("Left", "Centre", "Right"),
                selectedIndex = selectedCutoutIndex,
                onOptionSelected = { selectedCutoutIndex = it }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Fine tuning")
        AppCard(modifier = Modifier.appReveal(2)) {
            AppSlider(
                title = "Distance from top",
                value = verticalOffset,
                valueRange = -10f..60f,
                valueFormatter = { "${it.roundToInt()} dp" },
                onValueChange = { value ->
                    verticalOffset = value
                    scope.launch { preferences.setVerticalOffset(value.roundToInt()) }
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppSlider(
                title = "Horizontal offset",
                value = horizontalOffset,
                valueRange = -200f..200f,
                enabled = horizontalOffsetEnabled,
                trailingLabel = if (!horizontalOffsetEnabled) "PRO" else null,
                onLockedInteraction = onNavigateToPremium,
                valueFormatter = { "${it.roundToInt()} dp" },
                onValueChange = { value ->
                    horizontalOffset = value
                    scope.launch { preferences.setHorizontalOffset(value.roundToInt()) }
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppSlider(
                title = "Island width",
                value = widthScale,
                valueRange = 80f..120f,
                valueFormatter = { "${it.roundToInt()}%" },
                onValueChange = { value ->
                    widthScale = value
                    scope.launch { preferences.setWidthScale(value / 100f) }
                }
            )
        }

        AppSectionSpacer()

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
                    preferences.setVerticalOffset(AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP)
                    if (horizontalOffsetEnabled) {
                        preferences.setHorizontalOffset(AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP)
                    }
                    preferences.setWidthScale(AuroraPreferences.DEFAULT_WIDTH_SCALE)
                }
            }
        )
        AppFootnote("These values are applied to the overlay immediately.")
    }
}
