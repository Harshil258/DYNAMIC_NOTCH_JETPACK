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
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onOpenBatterySettings: () -> Unit = {}
) {
    val steps = listOf(
        "Set battery usage to Unrestricted",
        "Enable Autostart (Xiaomi, Oppo, Vivo)",
        "Exclude the app from memory cleaners"
    )

    AppDialog(
        onDismiss = onDismiss,
        title = "Keep the island alive",
        subtitle = "Some manufacturers close background overlays.",
        glyph = AppleGlyph.Battery,
        accent = AppTheme.colors.warning
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.warning)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    AppText(
                        text = step,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Open battery settings",
            glyph = AppleGlyph.Power,
            onClick = {
                onOpenBatterySettings()
                onDismiss()
            }
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Later",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}
