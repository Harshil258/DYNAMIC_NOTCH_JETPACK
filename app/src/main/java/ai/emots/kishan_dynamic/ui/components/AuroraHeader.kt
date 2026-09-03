package ai.emots.kishan_dynamic.ui.components

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * Screen Header with tactile back button, title, and optional trailing action.
 */
@Composable
fun AuroraScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AuroraTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            onBack?.let { backClick ->
                AuroraIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = backClick,
                    size = AuroraTheme.spacing.huge
                )
                Spacer(modifier = Modifier.width(AuroraTheme.spacing.md))
            }

            Column {
                Text(
                    text = title,
                    style = AuroraTheme.typography.headlineMedium,
                    color = AuroraTheme.colors.textPrimary
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = AuroraTheme.typography.bodySmall,
                        color = AuroraTheme.colors.textSecondary
                    )
                }
            }
        }

        trailingAction?.let {
            Spacer(modifier = Modifier.width(AuroraTheme.spacing.md))
            it()
        }
    }
}

/**
 * Section Header with leading icon, section title, and optional badge.
 */
@Composable
fun AuroraSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AuroraTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AuroraTheme.colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = AuroraTheme.colors.primary,
                        modifier = Modifier.size(AuroraTheme.spacing.iconSmall)
                    )
                }
            }
            Text(
                text = title,
                style = AuroraTheme.typography.titleMedium,
                color = AuroraTheme.colors.textPrimary
            )
            badgeText?.let {
                AuroraBadge(text = it)
            }
        }

        if (actionText != null && onActionClick != null) {
            AuroraButton(
                onClick = onActionClick,
                variant = ButtonVariant.Ghost,
                shape = AuroraTheme.shapes.buttonSmall,
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = actionText,
                    style = AuroraTheme.typography.labelMedium,
                    color = AuroraTheme.colors.primary
                )
            }
        }
    }
}

/**
 * Bottom Sheet Container with specular grab handle.
 */
@Composable
fun AuroraBottomSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AuroraTheme.shapes.modal)
            .background(AuroraTheme.colors.backgroundBase)
            .border(
                width = AuroraTheme.elevation.hairlineBorder,
                brush = AuroraTheme.colors.glassBorderBrush,
                shape = AuroraTheme.shapes.modal
            )
            .padding(horizontal = AuroraTheme.spacing.screenGutter, vertical = AuroraTheme.spacing.md)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(AuroraTheme.colors.textTertiary.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.height(AuroraTheme.spacing.lg))
            content()
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Headers Dark Preview", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun HeadersDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuroraScreenHeader(
                title = "Island Studio",
                subtitle = "Fine-tune physical notch alignment",
                onBack = {},
                trailingAction = {
                    AuroraIconButton(icon = Icons.Rounded.Tune, onClick = {})
                }
            )

            AuroraSectionHeader(
                title = "Live Visualizers",
                icon = Icons.Rounded.Tune,
                badgeText = "EXPERIMENTAL",
                actionText = "Reset All",
                onActionClick = {}
            )

            AuroraBottomSheet {
                Text(
                    text = "Configure Action Tiles",
                    style = AuroraTheme.typography.titleLarge,
                    color = AuroraTheme.colors.textPrimary
                )
            }
        }
    }
}
