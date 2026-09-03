package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.dialogs.CallHistorySheet
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryDialog
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryMockData
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

@Composable
fun CallSettingsScreen(
    onBack: () -> Unit
) {
    var isCallBannerEnabled by remember { mutableStateOf(true) }
    var isCallSummaryEnabled by remember { mutableStateOf(true) }
    var isCallTimerEnabled by remember { mutableStateOf(true) }
    var isExpandedCall by remember { mutableStateOf(true) }

    var showCallSummaryDialog by remember { mutableStateOf(false) }
    var showCallHistorySheet by remember { mutableStateOf(false) }

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
                title = "Phone Calls & Telephony",
                subtitle = "Caller heads-up banner & active call timer",
                onBack = onBack
            )

            // Live Island Call Preview
            LuxuryCard(padding = 20.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DynamicIslandPill(
                        state = if (isExpandedCall) IslandDemoState.CallExpanded else IslandDemoState.CallCompact,
                        onTap = { isExpandedCall = !isExpandedCall }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isExpandedCall) "Expanded Caller HUD with Answer / Decline" else "Compact Call Timer Bubble",
                        fontSize = 12.sp,
                        color = AppTheme.colors.success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Toggles Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Switch 1: Call Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Incoming Call Heads-Up Banner",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Show caller name, photo, Answer and Decline buttons right on the island",
                                fontSize = 11.5.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        LuxurySwitch(
                            checked = isCallBannerEnabled,
                            onCheckedChange = { isCallBannerEnabled = it }
                        )
                    }

                    // Switch 2: Call Timer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show Call Duration Timer",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Display a live ticking call duration bubble in your notch while on a call",
                                fontSize = 11.5.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        LuxurySwitch(
                            checked = isCallTimerEnabled,
                            onCheckedChange = { isCallTimerEnabled = it }
                        )
                    }

                    // Switch 3: Call Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show Call Summary when Call Ends",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Instant popup card with duration, redial, WhatsApp and SMS shortcuts",
                                fontSize = 11.5.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        LuxurySwitch(
                            checked = isCallSummaryEnabled,
                            onCheckedChange = { isCallSummaryEnabled = it }
                        )
                    }
                }
            }

            // Quick Inspection Actions with Apple Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleButton(
                    text = "Preview Summary",
                    onClick = { showCallSummaryDialog = true },
                    glyph = AppleGlyph.Phone,
                    style = AppleButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    paddingVertical = 12.dp
                )

                AppleButton(
                    text = "Call History",
                    onClick = { showCallHistorySheet = true },
                    glyph = AppleGlyph.History,
                    style = AppleButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    paddingVertical = 12.dp
                )
            }

            // Simulate Incoming Call Apple Button
            AppleButton(
                text = "Test Incoming Phone Call",
                onClick = { isExpandedCall = true },
                glyph = AppleGlyph.Phone,
                style = AppleButtonStyle.SUCCESS,
                paddingVertical = 15.dp
            )
        }

        // Dialogs
        if (showCallSummaryDialog) {
            CallSummaryDialog(
                mockData = CallSummaryMockData(
                    contactName = "Sarah Connor",
                    phoneNumber = "+1 (555) 019-2834",
                    durationFormatted = "04:12",
                    callType = "Incoming Call",
                    timestamp = "Just Now"
                ),
                onDismiss = { showCallSummaryDialog = false }
            )
        }

        if (showCallHistorySheet) {
            CallHistorySheet(
                onDismiss = { showCallHistorySheet = false }
            )
        }
    }
}
