package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
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
import ai.emots.kishan_dynamic.ui.screens.PermissionItemData
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Composable
fun PermissionExplanationSheet(
    permission: PermissionItemData,
    onDismiss: () -> Unit,
    onGrantClick: () -> Unit
) {
    AppSheet(
        onDismiss = onDismiss,
        title = permission.title,
        subtitle = if (permission.isRequired) "Required" else "Recommended",
        glyph = permission.glyph
    ) {
        AppText(
            text = permission.description,
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.success.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Shield, tint = AppTheme.colors.success, size = 13.dp)
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                AppText(
                    text = "How your data is used",
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary
                )
            }
            AppText(
                text = permission.privacyDetail,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Grant access",
            glyph = AppleGlyph.Check,
            onClick = onGrantClick
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Not now",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}
