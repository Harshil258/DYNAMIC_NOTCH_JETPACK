package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraMotion
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

enum class ButtonVariant {
    Primary,
    Secondary,
    Ghost,
    Success,
    Danger
}

/**
 * High-aesthetic Aurora Button with spring press physics and glowing gradient fills.
 */
@Composable
fun AuroraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    shape: Shape = AuroraTheme.shapes.button,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = AuroraMotion.PressSpringFloat,
        label = "btn_scale"
    )

    val backgroundBrush: Brush = when (variant) {
        ButtonVariant.Primary -> AuroraTheme.colors.brandGradientBrush
        ButtonVariant.Secondary -> AuroraTheme.colors.glassSurfaceBrush
        ButtonVariant.Ghost -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        ButtonVariant.Success -> Brush.horizontalGradient(
            listOf(AuroraTheme.colors.success, AuroraTheme.colors.cyan)
        )
        ButtonVariant.Danger -> Brush.horizontalGradient(
            listOf(AuroraTheme.colors.error, AuroraTheme.colors.auraPink)
        )
    }

    val borderModifier = when (variant) {
        ButtonVariant.Primary -> Modifier
        ButtonVariant.Secondary -> Modifier.border(
            width = AuroraTheme.elevation.hairlineBorder,
            brush = AuroraTheme.colors.glassBorderBrush,
            shape = shape
        )
        ButtonVariant.Ghost -> Modifier
        ButtonVariant.Success -> Modifier
        ButtonVariant.Danger -> Modifier
    }

    val contentColor = when (variant) {
        ButtonVariant.Primary, ButtonVariant.Success, ButtonVariant.Danger -> Color.White
        ButtonVariant.Secondary -> AuroraTheme.colors.textPrimary
        ButtonVariant.Ghost -> AuroraTheme.colors.primary
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minHeight = AuroraTheme.spacing.buttonHeight)
            .clip(shape)
            .background(backgroundBrush)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = AuroraTheme.spacing.xxl, vertical = AuroraTheme.spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(AuroraTheme.spacing.iconMedium)
                )
                Spacer(modifier = Modifier.width(AuroraTheme.spacing.sm))
            }
            content()
            trailingIcon?.let { icon ->
                Spacer(modifier = Modifier.width(AuroraTheme.spacing.sm))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(AuroraTheme.spacing.iconMedium)
                )
            }
        }
    }
}

/**
 * Tactile circular or squircle Icon Button with glass frosted styling.
 */
@Composable
fun AuroraIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = AuroraTheme.spacing.huge,
    shape: Shape = CircleShape,
    tint: Color = AuroraTheme.colors.textPrimary,
    backgroundBrush: Brush = AuroraTheme.colors.glassSurfaceBrush
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = AuroraMotion.PressSpringFloat,
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .size(size)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = AuroraTheme.elevation.hairlineBorder,
                brush = AuroraTheme.colors.glassBorderBrush,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(AuroraTheme.spacing.iconMedium)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Buttons Dark Preview", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ButtonsDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuroraButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    trailingIcon = Icons.Rounded.ArrowForward
                ) {
                    Text(
                        text = "Continue",
                        style = AuroraTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
                AuroraIconButton(
                    icon = Icons.Rounded.AutoAwesome,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Secondary and Ghost Dark Preview", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun SecondaryGhostDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuroraButton(
                    onClick = {},
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Customize",
                        style = AuroraTheme.typography.labelLarge,
                        color = AuroraTheme.colors.textPrimary
                    )
                }
                AuroraButton(
                    onClick = {},
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Skip Tour",
                        style = AuroraTheme.typography.labelLarge,
                        color = AuroraTheme.colors.primary
                    )
                }
            }
        }
    }
}
