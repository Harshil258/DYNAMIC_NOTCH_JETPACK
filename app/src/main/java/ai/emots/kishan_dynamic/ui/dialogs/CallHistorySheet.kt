package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.theme.AppTheme

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

@Composable
fun CallHistorySheet(
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit = {}
) {
    var callLogs by remember {
        mutableStateOf(
            listOf(
                CallLogItem("1", "Sarah Jenkins", "+1 555 392 8411", "04:18", "Today, 10:45", CallLogType.INCOMING),
                CallLogItem("2", "Marcus Vance", "+1 555 831 2940", "01:02", "Today, 09:12", CallLogType.OUTGOING),
                CallLogItem("3", "Unknown", "+1 555 664 0021", "—", "Yesterday, 21:04", CallLogType.MISSED),
                CallLogItem("4", "Tamia Castillo", "+1 555 019 8834", "12:40", "Yesterday, 17:20", CallLogType.INCOMING)
            )
        )
    }

    AppSheet(
        onDismiss = onDismiss,
        title = "Call history",
        subtitle = if (callLogs.isEmpty()) "No records" else "${callLogs.size} recent calls"
    ) {
        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "No calls logged yet",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textTertiary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                callLogs.forEach { item ->
                    CallLogRow(item = item, onCallClick = { onCallClick(item.phoneNumber) })
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Clear history",
            glyph = AppleGlyph.Reset,
            style = AppButtonStyle.Secondary,
            enabled = callLogs.isNotEmpty(),
            onClick = { callLogs = emptyList() }
        )
    }
}

@Composable
private fun CallLogRow(
    item: CallLogItem,
    onCallClick: () -> Unit
) {
    val accent = when (item.type) {
        CallLogType.INCOMING -> AppTheme.colors.success
        CallLogType.OUTGOING -> AppTheme.colors.accent
        CallLogType.MISSED -> AppTheme.colors.error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.lg))
            .background(AppTheme.colors.surfaceVariant)
            .padding(AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = AppleGlyph.Phone, tint = accent, size = 16.dp)
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = item.contactName,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1
            )
            AppText(
                text = item.timestamp,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                maxLines = 1
            )
        }

        if (item.type != CallLogType.MISSED) {
            AppText(
                text = item.durationFormatted,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textTertiary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.success.copy(alpha = 0.16f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCallClick
                ),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = AppleGlyph.Phone, tint = AppTheme.colors.success, size = 15.dp)
        }
    }
}
