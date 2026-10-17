package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.IslandActivationSnapshot
import ai.emots.kishan_dynamic.data.model.IslandActivationStatus
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppGlyphBadge
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppSwitch
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Prominent, compact Island Activation Card placed in the initial viewport of the Home screen.
 * Accurately displays real system readiness, service connection, and provides the primary contextual action.
 */
@Composable
fun IslandActivationCard(
    snapshot: IslandActivationSnapshot,
    onPrimaryAction: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (snapshot.badgeType) {
        IslandActivationSnapshot.BadgeType.SUCCESS -> AppTheme.colors.success
        IslandActivationSnapshot.BadgeType.WARNING -> AppTheme.colors.warning
        IslandActivationSnapshot.BadgeType.ERROR -> AppTheme.colors.error
        IslandActivationSnapshot.BadgeType.NEUTRAL -> AppTheme.colors.textSecondary
    }

    val glyph = when (snapshot.status) {
        IslandActivationStatus.RUNNING -> AppleGlyph.Power
        IslandActivationStatus.READY_OFF -> AppleGlyph.Power
        IslandActivationStatus.SERVICE_PAUSED -> AppleGlyph.Reset
        IslandActivationStatus.SETUP_REQUIRED,
        IslandActivationStatus.PERMISSION_NEEDED -> AppleGlyph.Shield
        IslandActivationStatus.STARTING -> AppleGlyph.Reset
        IslandActivationStatus.ERROR -> AppleGlyph.Info
    }

    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header: Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppGlyphBadge(
                    glyph = glyph,
                    tint = badgeColor,
                    size = 38.dp
                )
                Spacer(modifier = Modifier.size(AppTheme.spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = snapshot.title,
                        style = AppTheme.typography.h3,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                    AppText(
                        text = if (snapshot.isRunning) "Dynamic Island Active" else "Dynamic Island Status",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textTertiary
                    )
                }
                AppStatusPill(
                    text = snapshot.badgeLabel,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            // Subtitle / Informative text
            AppText(
                text = snapshot.subtitle,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            // Primary Contextual Action Area
            if (snapshot.canToggleDirectly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText(
                        text = if (snapshot.isRunning) "Live Overlay Enabled" else "Turn Island On",
                        style = AppTheme.typography.body,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.textPrimary
                    )
                    AppSwitch(
                        checked = snapshot.isRunning,
                        onCheckedChange = onToggle
                    )
                }
            } else {
                AppButton(
                    text = snapshot.primaryActionText,
                    onClick = onPrimaryAction,
                    glyph = glyph,
                    style = if (snapshot.status == IslandActivationStatus.SERVICE_PAUSED) {
                        AppButtonStyle.Primary
                    } else {
                        AppButtonStyle.Primary
                    },
                    fillMaxWidth = true
                )
            }
        }
    }
}
