package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.dialogs.CallHistorySheet
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryDialog
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryMockData
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.ui.unit.dp

@Composable
fun CallSettingsScreen(
    onBack: () -> Unit
) {
    var isCallBannerEnabled by remember { mutableStateOf(true) }
    var isCallTimerEnabled by remember { mutableStateOf(true) }
    var isCallSummaryEnabled by remember { mutableStateOf(true) }
    var isExpandedCall by remember { mutableStateOf(true) }

    var showCallSummaryDialog by remember { mutableStateOf(false) }
    var showCallHistorySheet by remember { mutableStateOf(false) }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Phone calls",
            subtitle = "Caller HUD and live call timer",
            onBack = onBack
        )

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (isExpandedCall) "Tap the island to collapse" else "Tap the island to expand",
            minHeight = 190.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedCall) IslandDemoState.CallExpanded else IslandDemoState.CallCompact,
                onTap = { isExpandedCall = !isExpandedCall }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("During a call")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppToggleRow(
                title = "Incoming call banner",
                subtitle = "Caller name, photo, answer and decline",
                glyph = AppleGlyph.Phone,
                checked = isCallBannerEnabled,
                onCheckedChange = { isCallBannerEnabled = it }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Call duration timer",
                subtitle = "Live ticking timer while the call is active",
                glyph = AppleGlyph.Timer,
                checked = isCallTimerEnabled,
                onCheckedChange = { isCallTimerEnabled = it }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Summary when a call ends",
                subtitle = "Duration with redial and message shortcuts",
                glyph = AppleGlyph.History,
                checked = isCallSummaryEnabled,
                onCheckedChange = { isCallSummaryEnabled = it }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Preview")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            AppButton(
                text = "Call summary",
                glyph = AppleGlyph.Phone,
                style = AppButtonStyle.Secondary,
                fillMaxWidth = false,
                modifier = Modifier.weight(1f),
                onClick = { showCallSummaryDialog = true }
            )
            AppButton(
                text = "Call history",
                glyph = AppleGlyph.History,
                style = AppButtonStyle.Secondary,
                fillMaxWidth = false,
                modifier = Modifier.weight(1f),
                onClick = { showCallHistorySheet = true }
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        AppButton(
            text = "Test an incoming call",
            glyph = AppleGlyph.Phone,
            onClick = { isExpandedCall = true }
        )
        AppFootnote("Call details are read on device only and are never stored or uploaded.")
    }

    if (showCallSummaryDialog) {
        CallSummaryDialog(
            mockData = CallSummaryMockData(
                contactName = "Sarah Connor",
                phoneNumber = "+1 (555) 019-2834",
                durationFormatted = "04:12",
                callType = "Incoming call",
                timestamp = "Just now"
            ),
            onDismiss = { showCallSummaryDialog = false }
        )
    }

    if (showCallHistorySheet) {
        CallHistorySheet(onDismiss = { showCallHistorySheet = false })
    }
}
