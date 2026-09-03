package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedAutoExpand by preferences.autoExpand.collectAsState(initial = true)
    val savedDuration by preferences.displayDuration.collectAsState(initial = 5)
    val savedSwipeUp by preferences.swipeUpDismiss.collectAsState(initial = true)

    var isSimulating by remember { mutableStateOf(false) }
    val durations = listOf("2s", "4s", "6s", "8s", "10s")
    val durationValues = listOf(2, 4, 6, 8, 10)
    val selectedDurationIndex = durationValues.indexOf(savedDuration).coerceAtLeast(0)

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
                title = "Notifications & Messages",
                subtitle = "Heads-up popups for WhatsApp, Telegram & apps",
                onBack = onBack
            )

            // Live Island Preview Stage
            LuxuryCard(padding = 20.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DynamicIslandPill(
                        state = if (savedAutoExpand || isSimulating) IslandDemoState.NotificationExpanded
                        else IslandDemoState.NotificationCompact
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (savedAutoExpand) "Auto-Expanded Heads-Up Mode" else "Compact Pill Mode",
                        fontSize = 12.sp,
                        color = Color(0xFF25D366),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Toggles Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Switch 1: Auto-Expand
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Auto-Expand on New Message",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Instantly open expanded preview showing message text and sender avatar",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = savedAutoExpand,
                            onCheckedChange = {
                                scope.launch { preferences.setAutoExpand(it) }
                            }
                        )
                    }

                    // Switch 2: Swipe up to dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Swipe Up to Dismiss",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Flick the island upward with your thumb to dismiss it early",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = savedSwipeUp,
                            onCheckedChange = {
                                scope.launch { preferences.setSwipeUpDismiss(it) }
                            }
                        )
                    }
                }
            }

            // How Long to Show Alert with Apple Segmented Control
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "How Long to Show Alert",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuroraTokens.TextColor.primary
                    )
                    Text(
                        text = "Time the island remains visible before smoothly collapsing",
                        fontSize = 11.5.sp,
                        color = AuroraTokens.TextColor.secondary
                    )

                    AppleSegmentedControl(
                        options = durations,
                        selectedIndex = selectedDurationIndex,
                        onOptionSelected = { idx ->
                            scope.launch { preferences.setDisplayDuration(durationValues[idx]) }
                        }
                    )
                }
            }

            // Test Simulation Apple Button
            AppleButton(
                text = if (isSimulating) "Simulating WhatsApp Alert..." else "Test WhatsApp Notification",
                onClick = {
                    scope.launch {
                        isSimulating = true
                        delay(3500)
                        isSimulating = false
                    }
                },
                glyph = AppleGlyph.Play,
                style = AppleButtonStyle.SUCCESS,
                paddingVertical = 15.dp
            )
        }
    }
}
