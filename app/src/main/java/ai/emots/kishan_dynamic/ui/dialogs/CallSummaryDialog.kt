package ai.emots.kishan_dynamic.ui.dialogs

import android.widget.Toast
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val timestamp: String,
    val avatarUri: String? = null
)

@Deprecated("Use CallSummaryData for production call records")
typealias CallSummaryMockData = CallSummaryData

/**
 * Redesigned, Aurora-grade Call Summary bottom sheet dialog.
 * Features:
 * - Obsidian dark / Porcelain light theme contrast parity
 * - Elevated profile card with status pill and duration
 * - 5 high-utility follow-up actions: Redial, SMS, WhatsApp, Save Contact, Copy Number
 * - 48dp accessible touch targets with fluid spring interaction scale
 */
@Composable
fun CallSummaryDialog(
    data: CallSummaryData,
    onDismiss: () -> Unit,
    onRedial: () -> Unit = {},
    onMessage: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onSave: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    val context = LocalContext.current
    val isMissed = data.durationFormatted.equals("Missed", ignoreCase = true) ||
        data.callType.contains("Missed", ignoreCase = true)

    val rawNumber = data.phoneNumber.trim()
    val hasValidNumber = rawNumber.isNotBlank() &&
        !rawNumber.equals("Unknown Caller", ignoreCase = true) &&
        !rawNumber.equals("Unknown", ignoreCase = true)

    AppSheet(
        onDismiss = onDismiss,
        title = "Call summary",
        subtitle = data.callType
    ) {
        // Caller Profile Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.xl))
                .background(AppTheme.colors.surfaceGradient())
                .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.xl))
                .padding(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMissed) AppTheme.colors.warning.copy(alpha = 0.16f)
                        else AppTheme.colors.accent.copy(alpha = 0.16f)
                    )
                    .border(
                        1.dp,
                        if (isMissed) AppTheme.colors.warning.copy(alpha = 0.32f)
                        else AppTheme.colors.accent.copy(alpha = 0.32f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = data.contactName.trim().take(1).uppercase().ifBlank { "?" },
                    style = AppTheme.typography.h2,
                    color = if (isMissed) AppTheme.colors.warning else AppTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = data.contactName,
                    style = AppTheme.typography.h3,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasValidNumber && data.contactName != data.phoneNumber) {
                    AppText(
                        text = data.phoneNumber,
                        style = AppTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = AppTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                AppText(
                    text = data.timestamp,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            AppStatusPill(
                text = data.durationFormatted,
                color = if (isMissed) AppTheme.colors.warning else AppTheme.colors.success,
                showDot = !isMissed
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Quick Actions Row (Redial, SMS, WhatsApp, Save, Copy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            CallActionItem(
                glyph = AppleGlyph.Phone,
                label = "Redial",
                tint = Color(0xFF34C759), // iOS call green
                enabled = hasValidNumber,
                onClick = { onRedial(); onDismiss() },
                modifier = Modifier.weight(1f)
            )
            CallActionItem(
                glyph = AppleGlyph.Chat,
                label = "SMS",
                tint = Color(0xFF007AFF), // iOS message blue
                enabled = hasValidNumber,
                onClick = { onMessage(); onDismiss() },
                modifier = Modifier.weight(1f)
            )
            CallActionItem(
                glyph = AppleGlyph.Chat,
                label = "WhatsApp",
                tint = Color(0xFF25D366), // WhatsApp brand green
                enabled = hasValidNumber,
                onClick = { onWhatsApp(); onDismiss() },
                modifier = Modifier.weight(1f)
            )
            CallActionItem(
                glyph = AppleGlyph.Star,
                label = "Save",
                tint = Color(0xFFFF9500), // iOS amber
                enabled = hasValidNumber,
                onClick = { onSave(); onDismiss() },
                modifier = Modifier.weight(1f)
            )
            CallActionItem(
                glyph = AppleGlyph.Link,
                label = "Copy",
                tint = AppTheme.colors.textSecondary,
                enabled = hasValidNumber,
                onClick = {
                    onCopy()
                    Toast.makeText(context, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
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
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "call_action_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(AppTheme.radius.lg))
            .background(AppTheme.colors.surfaceGradient())
            .border(
                0.5.dp,
                AppTheme.colors.border.copy(alpha = if (enabled) 1f else 0.4f),
                RoundedCornerShape(AppTheme.radius.lg)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = AppTheme.spacing.md, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = if (enabled) 0.12f else 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = glyph,
                tint = if (enabled) tint else AppTheme.colors.textTertiary,
                size = 18.dp
            )
        }
        AppText(
            text = label,
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = if (enabled) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
