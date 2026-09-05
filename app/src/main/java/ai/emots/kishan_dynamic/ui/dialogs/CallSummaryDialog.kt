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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

data class CallSummaryData(
    val contactName: String,
    val phoneNumber: String,
    val durationFormatted: String,
    val callType: String,
    val timestamp: String
)

@Deprecated("Use CallSummaryData for production call records")
typealias CallSummaryMockData = CallSummaryData

@Composable
fun CallSummaryDialog(
    data: CallSummaryData,
    onDismiss: () -> Unit,
    onRedial: () -> Unit = {},
    onMessage: () -> Unit = {},
    onSave: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    AppSheet(
        onDismiss = onDismiss,
        title = "Call summary",
        subtitle = data.callType
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceGradient())
                .padding(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = data.contactName.take(1).uppercase(),
                    style = AppTheme.typography.h2,
                    color = AppTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = data.contactName,
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1
                )
                AppText(
                    text = data.phoneNumber,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                AppText(
                    text = data.timestamp,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textTertiary
                )
            }

            AppStatusPill(
                text = data.durationFormatted,
                color = AppTheme.colors.success,
                showDot = false
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            CallActionItem(glyph = AppleGlyph.Phone, label = "Redial", onClick = { onRedial(); onDismiss() }, modifier = Modifier.weight(1f))
            CallActionItem(glyph = AppleGlyph.Bell, label = "Message", onClick = { onMessage(); onDismiss() }, modifier = Modifier.weight(1f))
            CallActionItem(glyph = AppleGlyph.Star, label = "Save", onClick = { onSave(); onDismiss() }, modifier = Modifier.weight(1f))
            CallActionItem(glyph = AppleGlyph.History, label = "Copy", onClick = { onCopy(); onDismiss() }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Done",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}

@Composable
private fun CallActionItem(
    glyph: AppleGlyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppTheme.radius.lg))
            .background(AppTheme.colors.surfaceGradient())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = AppTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        AppleIcon(glyph = glyph, tint = AppTheme.colors.accent, size = 19.dp)
        AppText(
            text = label,
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.textSecondary,
            maxLines = 1
        )
    }
}
