package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

data class AppleQuickToggleItem(
    val name: String,
    val glyph: AppleGlyph,
    var isEnabled: Boolean
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
                AppleQuickToggleItem("Wi-Fi", AppleGlyph.Wifi, true),
                AppleQuickToggleItem("Bluetooth", AppleGlyph.Bluetooth, true),
                AppleQuickToggleItem("Torch", AppleGlyph.Torch, false),
                AppleQuickToggleItem("Location", AppleGlyph.Location, true),
                AppleQuickToggleItem("Rotate", AppleGlyph.Rotate, false),
                AppleQuickToggleItem("Mute", AppleGlyph.Mute, false),
                AppleQuickToggleItem("Airplane", AppleGlyph.Airplane, false)
            )
        )
    }

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
            // Top Bar
            LuxuryTopBar(
                title = "Control Center & Shortcuts",
                subtitle = "Instant system toggles & volume HUD",
                onBack = onBack
            )

            // Toggles Switch Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Switch 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Enable Control Center on Island",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Long press or tap the island to quickly access device shortcuts",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isQuickControlsEnabled,
                            onCheckedChange = { isQuickControlsEnabled = it }
                        )
                    }

                    // Switch 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Hardware Volume & Brightness HUD",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Show sleek island percentage readout when pressing physical volume keys",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isVolumeHudEnabled,
                            onCheckedChange = { isVolumeHudEnabled = it }
                        )
                    }
                }
            }

            // Quick Toggle Tiles Grid
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Customize Active Shortcut Tiles",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuroraTokens.TextColor.primary
                    )
                    Text(
                        text = "Tap tiles to add or remove them from your island control center",
                        fontSize = 12.sp,
                        color = AuroraTokens.TextColor.secondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        toggles.chunked(3).forEach { rowToggles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowToggles.forEach { item ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val tileScale by animateFloatAsState(
                                        targetValue = if (isPressed) 0.92f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                                        label = "tile_scale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .scale(tileScale)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (item.isEnabled) Color(0xFF2C1E4A)
                                                else Color(0xFF1A1726)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (item.isEnabled) AuroraTokens.Palette.primary else Color(0x10FFFFFF),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                toggles = toggles.map {
                                                    if (it.name == item.name) it.copy(isEnabled = !it.isEnabled)
                                                    else it
                                                }
                                            }
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AppleIcon(
                                                glyph = item.glyph,
                                                tint = if (item.isEnabled) AuroraTokens.Palette.primaryLight else AuroraTokens.TextColor.tertiary,
                                                size = 22.dp
                                            )
                                            Text(
                                                text = item.name,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (item.isEnabled) Color.White else AuroraTokens.TextColor.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
