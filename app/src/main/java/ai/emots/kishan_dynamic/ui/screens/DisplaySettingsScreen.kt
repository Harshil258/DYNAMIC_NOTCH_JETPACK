package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleSegmentedControl
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySlider
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedVerticalOffset by preferences.verticalOffset.collectAsState(initial = 14)
    val savedWidthScale by preferences.widthScale.collectAsState(initial = 1.0f)

    var verticalOffset by remember(savedVerticalOffset) { mutableFloatStateOf(savedVerticalOffset.toFloat()) }
    var widthScale by remember(savedWidthScale) { mutableFloatStateOf(savedWidthScale * 100f) }

    val cutoutOptions = listOf("Left", "Center", "Right")
    var selectedCutoutIndex by remember { mutableIntStateOf(1) }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(bottom = AppTheme.spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AuroraTokens.Spacing.sectionGap)
        ) {
            // Sleek Top Header
            LuxuryTopBar(
                title = "Camera Notch Alignment",
                subtitle = "Fit island perfectly to your camera hole",
                onBack = onBack
            )

            // Live Hardware Preview Stage
            LuxuryCard(
                shape = AuroraTokens.Shape.cardLarge,
                padding = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        contentAlignment = when (selectedCutoutIndex) {
                            0 -> Alignment.TopStart
                            2 -> Alignment.TopEnd
                            else -> Alignment.TopCenter
                        }
                    ) {
                        // Real camera punch hole
                        Box(
                            modifier = Modifier
                                .offset(y = 2.dp)
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF030306))
                                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        )

                        // Island pill preview
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(x = 0, y = verticalOffset.roundToInt()) }
                                .scale(widthScale / 100f)
                        ) {
                            DynamicIslandPill(state = IslandDemoState.Minimal)
                        }
                    }

                    Text(
                        text = "Live Hardware Alignment Stage",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            // Cutout Hole Position with Apple Segmented Control
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Camera Hole Position",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "Select where your front selfie camera is located",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary
                    )

                    AppleSegmentedControl(
                        options = cutoutOptions,
                        selectedIndex = selectedCutoutIndex,
                        onOptionSelected = { selectedCutoutIndex = it }
                    )
                }
            }

            // Sliders Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    LuxurySlider(
                        title = "Distance from Top Edge",
                        value = verticalOffset,
                        valueRange = -10f..60f,
                        valueFormatter = { "${it.toInt()} dp" },
                        onValueChange = {
                            verticalOffset = it
                            scope.launch { preferences.setVerticalOffset(it.toInt()) }
                        }
                    )

                    LuxurySlider(
                        title = "Island Width Scale",
                        value = widthScale,
                        valueRange = 80f..120f,
                        valueFormatter = { "${it.toInt()}%" },
                        onValueChange = {
                            widthScale = it
                            scope.launch { preferences.setWidthScale(it / 100f) }
                        }
                    )
                }
            }

            // Reset Position Apple Button
            AppleButton(
                text = "Reset Position to Center Defaults",
                onClick = {
                    verticalOffset = 14f
                    widthScale = 100f
                    selectedCutoutIndex = 1
                    scope.launch {
                        preferences.setVerticalOffset(14)
                        preferences.setWidthScale(1.0f)
                    }
                },
                glyph = AppleGlyph.Reset,
                style = AppleButtonStyle.SECONDARY,
                paddingVertical = 14.dp
            )
        }
    }
}
