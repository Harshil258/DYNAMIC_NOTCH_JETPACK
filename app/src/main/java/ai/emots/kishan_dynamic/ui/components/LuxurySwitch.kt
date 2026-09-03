package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

/**
 * Custom Apple-grade fluid toggle switch.
 * Features a gradient fill when enabled, specular glass rim, and smooth spring knob motion.
 */
@Composable
fun LuxurySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val thumbPadding = 3.dp

    val maxOffset = trackWidth - thumbSize - (thumbPadding * 2)

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else 0.dp,
        animationSpec = spring<Dp>(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_thumb_offset"
    )

    val trackBorderColor by animateColorAsState(
        targetValue = if (checked) Color(0x406D82FF) else AppTheme.colors.border,
        animationSpec = tween(200),
        label = "switch_track_border"
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(AuroraTokens.Palette.primary, Color(0xFF4D9CFF))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surface)
                    )
                }
            )
            .border(1.dp, trackBorderColor, RoundedCornerShape(100.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            }
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceElevated)
                .border(1.dp, AppTheme.colors.border, CircleShape)
        )
    }
}
