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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

data class CallSummaryMockData(
    val contactName: String,
    val phoneNumber: String,
    val durationFormatted: String,
    val callType: String,
    val timestamp: String
)

/**
 * Post-Call Summary Dialog showing duration analytics, contact details, and speed actions.
 */
@Composable
fun CallSummaryDialog(
    mockData: CallSummaryMockData,
    onDismiss: () -> Unit
) {
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
                            text = "Call Summary",
                            style = AuroraTheme.typography.titleMedium,
                            color = AuroraTheme.colors.textPrimary
                        )
                        AuroraIconButton(
                            icon = Icons.Rounded.Close,
                            onClick = onDismiss,
                            size = 32.dp
                        )
                    }

                    // Caller Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(AuroraTheme.colors.primary.copy(alpha = 0.2f))
                                    .border(width = 1.dp, color = AuroraTheme.colors.primary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mockData.contactName.take(1),
                                    style = AuroraTheme.typography.headlineMedium,
                                    color = AuroraTheme.colors.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(AuroraTheme.spacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mockData.contactName,
                                    style = AuroraTheme.typography.titleMedium,
                                    color = AuroraTheme.colors.textPrimary
                                )
                                Text(
                                    text = mockData.phoneNumber,
                                    style = AuroraTheme.typography.bodySmall,
                                    color = AuroraTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(AuroraTheme.spacing.xxs))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)
                                ) {
                                    AuroraBadge(
                                        text = mockData.durationFormatted,
                                        backgroundColor = AuroraTheme.colors.success.copy(alpha = 0.2f),
                                        textColor = AuroraTheme.colors.success
                                    )
                                    Text(
                                        text = "• ${mockData.timestamp}",
                                        style = AuroraTheme.typography.labelSmall,
                                        color = AuroraTheme.colors.textTertiary
                                    )
                                }
                            }
                        }
                    }

                    // 4 Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                    ) {
                        CallActionItem(icon = Icons.Rounded.Call, label = "Redial", onClick = onDismiss, modifier = Modifier.weight(1f))
                        CallActionItem(icon = Icons.Rounded.Message, label = "Message", onClick = onDismiss, modifier = Modifier.weight(1f))
                        CallActionItem(icon = Icons.Rounded.PersonAdd, label = "Save", onClick = onDismiss, modifier = Modifier.weight(1f))
                        CallActionItem(icon = Icons.Rounded.ContentCopy, label = "Copy", onClick = onDismiss, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))

                    AuroraButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Close Summary", style = AuroraTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(AuroraTheme.shapes.cardSmall)
            .background(AuroraTheme.colors.glassSurfaceBrush)
            .border(width = AuroraTheme.elevation.hairlineBorder, brush = AuroraTheme.colors.glassBorderBrush, shape = AuroraTheme.shapes.cardSmall)
            .clickable(onClick = onClick)
            .padding(AuroraTheme.spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AuroraTheme.colors.primary, modifier = Modifier.size(20.dp))
            Text(text = label, style = AuroraTheme.typography.labelSmall, color = AuroraTheme.colors.textSecondary)
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Call Summary Dialog - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun CallSummaryDialogDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        CallSummaryDialog(
            mockData = CallSummaryMockData(
                contactName = "Sarah Jenkins",
                phoneNumber = "+1 (555) 392-8411",
                durationFormatted = "04:18",
                callType = "Incoming Call",
                timestamp = "Today, 10:45 AM"
            ),
            onDismiss = {}
        )
    }
}
