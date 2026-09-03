package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.dialogs.BatteryOptimizationDialog
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

@Composable
fun BatterySettingsScreen(
    onBack: () -> Unit
) {
    var isChargingAnimationEnabled by remember { mutableStateOf(true) }
    var isLowBatteryAlertEnabled by remember { mutableStateOf(true) }
    var isExpandedCharging by remember { mutableStateOf(true) }
    var showOptimizationDialog by remember { mutableStateOf(false) }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuroraTokens.Spacing.screenGutter)
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(AuroraTokens.Spacing.sectionGap)
        ) {
            // Top Bar
            LuxuryTopBar(
                title = "Battery & Super Charging",
                subtitle = "Charging ripple animation & low power warnings",
                onBack = onBack
            )

            // Live Island Battery Preview
            LuxuryCard(padding = 20.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DynamicIslandPill(
                        state = if (isExpandedCharging) IslandDemoState.ChargingExpanded else IslandDemoState.ChargingCompact,
                        onTap = { isExpandedCharging = !isExpandedCharging }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isExpandedCharging) "Expanded 65W Warp Charge HUD" else "Compact Battery Status Pill",
                        fontSize = 12.sp,
                        color = AuroraTokens.Palette.warning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Toggles Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Switch 1: Charging Animation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show Super Charging Animation",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Pulsing neon ring and wattage indicator when power cable is plugged in",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isChargingAnimationEnabled,
                            onCheckedChange = { isChargingAnimationEnabled = it }
                        )
                    }

                    // Switch 2: Low Battery Warning
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Low Battery Warning (15% & 10%)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Critical battery heads-up reminder before your device turns off",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isLowBatteryAlertEnabled,
                            onCheckedChange = { isLowBatteryAlertEnabled = it }
                        )
                    }
                }
            }

            // Phone Battery Guardian Guide
            LuxuryCard(onClick = { showOptimizationDialog = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AuroraTokens.Palette.info.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Shield,
                                tint = AuroraTokens.Palette.info,
                                size = 20.dp
                            )
                        }

                        Column {
                            Text(
                                text = "Background Guardian Guide",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Prevent Samsung, Xiaomi, Vivo & OnePlus battery killer",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E1A2C))
                            .border(1.dp, Color(0x18FFFFFF), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VIEW GUIDE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuroraTokens.Palette.info
                        )
                    }
                }
            }

            // Simulate Charging Apple Button
            AppleButton(
                text = "Test 65W Warp Charging",
                onClick = { isExpandedCharging = true },
                glyph = AppleGlyph.Battery,
                style = AppleButtonStyle.WARNING,
                paddingVertical = 15.dp
            )
        }

        if (showOptimizationDialog) {
            BatteryOptimizationDialog(
                onDismiss = { showOptimizationDialog = false }
            )
        }
    }
}
