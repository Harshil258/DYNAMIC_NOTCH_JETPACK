package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * OEM Battery Guardian Dialog to safeguard the notch overlay from aggressive background cleaners.
 */
@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onOpenBatterySettings: () -> Unit = {}
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AuroraTheme.colors.warning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = AuroraTheme.colors.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Battery Guardian",
                                style = AuroraTheme.typography.titleMedium,
                                color = AuroraTheme.colors.textPrimary
                            )
                        }

                        AuroraIconButton(icon = Icons.Rounded.Close, onClick = onDismiss, size = 32.dp)
                    }

                    Text(
                        text = "Some Android manufacturers (Samsung, Xiaomi, Vivo, Huawei) aggressively close background overlays when screen turns off.",
                        style = AuroraTheme.typography.bodyMedium,
                        color = AuroraTheme.colors.textSecondary
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = AuroraTheme.spacing.sm) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "RECOMMENDED SETTINGS:", style = AuroraTheme.typography.labelSmall, color = AuroraTheme.colors.primary)
                            Text(text = "• Set App Battery Usage to 'Unrestricted'", style = AuroraTheme.typography.bodySmall, color = AuroraTheme.colors.textPrimary)
                            Text(text = "• Enable 'Autostart' in System Settings (Xiaomi/MIUI)", style = AuroraTheme.typography.bodySmall, color = AuroraTheme.colors.textPrimary)
                            Text(text = "• Exclude Aurora from Memory Cleaner", style = AuroraTheme.typography.bodySmall, color = AuroraTheme.colors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))

                    AuroraButton(
                        onClick = {
                            onOpenBatterySettings()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Open Battery Settings", style = AuroraTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Battery Dialog - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun BatteryDialogDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        BatteryOptimizationDialog(onDismiss = {})
    }
}
