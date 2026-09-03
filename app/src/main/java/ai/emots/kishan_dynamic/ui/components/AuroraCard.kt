package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraMotion
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * Standard iOS-style Frosted Glass Card with hairline specular highlight.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = AuroraTheme.shapes.card,
    contentPadding: Dp = AuroraTheme.spacing.cardPadding,
    borderWidth: Dp = AuroraTheme.elevation.hairlineBorder,
    borderBrush: Brush = AuroraTheme.colors.glassBorderBrush,
    backgroundBrush: Brush = AuroraTheme.colors.glassSurfaceBrush,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.98f else 1.0f,
        animationSpec = AuroraMotion.PressSpringFloat,
        label = "card_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .scale(animatedScale)
            .clip(shape)
            .background(backgroundBrush)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Premium Glowing Glass Card with ambient radial violet aura.
 */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = AuroraTheme.colors.primary,
    shape: Shape = AuroraTheme.shapes.card,
    contentPadding: Dp = AuroraTheme.spacing.cardPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glowBorderBrush = Brush.linearGradient(
        colors = listOf(
            glowColor.copy(alpha = 0.6f),
            AuroraTheme.colors.auraPink.copy(alpha = 0.3f),
            Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = 0.3f),
                spotColor = glowColor.copy(alpha = 0.4f)
            )
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            contentPadding = contentPadding,
            borderBrush = glowBorderBrush,
            borderWidth = AuroraTheme.elevation.activeBorder,
            onClick = onClick,
            content = content
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Glass Card - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun GlassCardDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Dynamic Notch Studio",
                        style = AuroraTheme.typography.titleLarge,
                        color = AuroraTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))
                    Text(
                        text = "Real-time gesture feedback & continuous morphing geometry.",
                        style = AuroraTheme.typography.bodyMedium,
                        color = AuroraTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Preview(name = "Glow Card - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun GlowCardDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "✨ Aurora Pro Activated",
                        style = AuroraTheme.typography.titleLarge,
                        color = AuroraTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))
                    Text(
                        text = "Lifetime access with unlimited custom island themes.",
                        style = AuroraTheme.typography.bodyMedium,
                        color = AuroraTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Preview(name = "Glass Card - Light", showBackground = true, backgroundColor = 0xFFEDE8F8)
@Composable
private fun GlassCardLightPreview() {
    AuroraIslandTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Titanium Pearlescent Glass",
                        style = AuroraTheme.typography.titleLarge,
                        color = AuroraTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))
                    Text(
                        text = "Ultra-crisp light theme with subtle purple specular accents.",
                        style = AuroraTheme.typography.bodyMedium,
                        color = AuroraTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
