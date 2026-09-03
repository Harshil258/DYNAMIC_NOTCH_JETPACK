package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallMissed
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AuroraBadge
import ai.emots.kishan_dynamic.ui.components.AuroraBottomSheet
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

enum class CallLogType {
    INCOMING,
    OUTGOING,
    MISSED
}

data class CallLogItem(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val durationFormatted: String,
    val timestamp: String,
    val type: CallLogType
)

/**
 * Call History Log Bottom Sheet displaying recent calls with quick callbacks.
 */
@Composable
fun CallHistorySheet(
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit = {}
) {
    var callLogs by remember {
        mutableStateOf(
            listOf(
                CallLogItem("1", "Sarah Jenkins", "+1 (555) 392-8411", "04:18", "Today, 10:45 AM", CallLogType.INCOMING),
                CallLogItem("2", "Marcus Vance", "+1 (555) 831-2940", "01:02", "Today, 09:12 AM", CallLogType.OUTGOING),
                CallLogItem("3", "Elena Rostova", "+1 (555) 201-9482", "00:00", "Yesterday, 06:30 PM", CallLogType.MISSED),
                CallLogItem("4", "David K.", "+1 (555) 749-1120", "12:45", "Yesterday, 02:15 PM", CallLogType.INCOMING)
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraTheme.spacing.md),
            contentAlignment = Alignment.BottomCenter
        ) {
            AuroraBottomSheet {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Call History",
                            style = AuroraTheme.typography.titleMedium,
                            color = AuroraTheme.colors.textPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)) {
                            if (callLogs.isNotEmpty()) {
                                AuroraIconButton(
                                    icon = Icons.Rounded.DeleteOutline,
                                    onClick = { callLogs = emptyList() },
                                    size = 32.dp
                                )
                            }
                            AuroraIconButton(
                                icon = Icons.Rounded.Close,
                                onClick = onDismiss,
                                size = 32.dp
                            )
                        }
                    }

                    if (callLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No call records logged yet",
                                style = AuroraTheme.typography.bodySmall,
                                color = AuroraTheme.colors.textTertiary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                        ) {
                            items(callLogs) { item ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = AuroraTheme.spacing.sm
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Type Icon & Details
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (item.type) {
                                                            CallLogType.INCOMING -> AuroraTheme.colors.success.copy(alpha = 0.15f)
                                                            CallLogType.OUTGOING -> AuroraTheme.colors.cyan.copy(alpha = 0.15f)
                                                            CallLogType.MISSED -> AuroraTheme.colors.error.copy(alpha = 0.15f)
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (item.type) {
                                                        CallLogType.INCOMING -> Icons.AutoMirrored.Rounded.CallReceived
                                                        CallLogType.OUTGOING -> Icons.AutoMirrored.Rounded.CallMade
                                                        CallLogType.MISSED -> Icons.AutoMirrored.Rounded.CallMissed
                                                    },
                                                    contentDescription = null,
                                                    tint = when (item.type) {
                                                        CallLogType.INCOMING -> AuroraTheme.colors.success
                                                        CallLogType.OUTGOING -> AuroraTheme.colors.cyan
                                                        CallLogType.MISSED -> AuroraTheme.colors.error
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = item.contactName,
                                                    style = AuroraTheme.typography.titleSmall,
                                                    color = AuroraTheme.colors.textPrimary
                                                )
                                                Text(
                                                    text = "${item.phoneNumber} • ${item.timestamp}",
                                                    style = AuroraTheme.typography.bodySmall,
                                                    color = AuroraTheme.colors.textSecondary
                                                )
                                            }
                                        }

                                        // Duration & Callback Action
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                                        ) {
                                            if (item.type != CallLogType.MISSED) {
                                                Text(
                                                    text = item.durationFormatted,
                                                    style = AuroraTheme.typography.monoNumbers,
                                                    color = AuroraTheme.colors.textSecondary
                                                )
                                            }
                                            AuroraIconButton(
                                                icon = Icons.Rounded.Call,
                                                onClick = { onCallClick(item.phoneNumber) },
                                                size = 36.dp
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

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Call History - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun CallHistoryDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        CallHistorySheet(onDismiss = {})
    }
}
