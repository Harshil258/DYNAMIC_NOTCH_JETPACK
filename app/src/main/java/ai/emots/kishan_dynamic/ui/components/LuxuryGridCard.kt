package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Luxury dark squircle card matching the 2-column grid in reference design.
 * Features Apple spring touch compression and custom SF-style vector glyphs.
 */
@Composable
fun LuxuryGridCard(
    title: String,
    subtitle: String,
    glyph: AppleGlyph,
    glyphTint: Color = AppTheme.colors.primary,
    isPro: Boolean = false,
    statusText: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "grid_card_scale"
    )

    val shape = RoundedCornerShape(22.dp)

    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF261D3D),
                Color(0xFF1B142D)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF191428),
                Color(0xFF120E1E)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                AppTheme.colors.primary,
                Color(0x40A78BFA),
                Color.Transparent
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x22FFFFFF),
                Color(0x08FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 0.8.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Circular Glyphs + Crown (if Pro)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon in soft dark frosted disc
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF241E36))
                        .border(
                            width = 1.dp,
                            color = Color(0x22A78BFA),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = glyph,
                        tint = glyphTint,
                        size = 20.dp
                    )
                }

                if (isPro) {
                    AppleIcon(
                        glyph = AppleGlyph.Crown,
                        tint = AppTheme.colors.gold,
                        size = 16.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Text: Title and Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(
                    text = title,
                    style = AppTheme.typography.h3,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 2,
                    lineHeight = 15.sp
                )

                if (statusText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    AppText(
                        text = statusText,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
