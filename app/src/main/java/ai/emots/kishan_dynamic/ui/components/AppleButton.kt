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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

enum class AppleButtonStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    SECONDARY
}

/**
 * Pure Apple iOS pill button with specular top highlight, gradient fill,
 * and tactile spring compression on touch.
 */
@Composable
fun AppleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: AppleGlyph? = null,
    style: AppleButtonStyle = AppleButtonStyle.PRIMARY,
    paddingVertical: Dp = 15.dp,
    paddingHorizontal: Dp = 20.dp,
    fontSize: TextUnit = 14.sp,
    fillMaxWidth: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "apple_btn_scale"
    )

    val (brush, textColor, iconColor) = when (style) {
        AppleButtonStyle.PRIMARY -> Triple(
            Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE5E5EA))),
            Color.Black,
            Color.Black
        )
        AppleButtonStyle.SUCCESS -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF30D158), Color(0xFF28CD41))),
            Color.White,
            Color.White
        )
        AppleButtonStyle.WARNING -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFFFF9F0A), Color(0xFFFF9500))),
            Color.Black,
            Color.Black
        )
        AppleButtonStyle.SECONDARY -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF242428), Color(0xFF1C1C20))),
            Color.White,
            Color.White
        )
    }

    val widthMod = if (fillMaxWidth) modifier.fillMaxWidth() else modifier

    Box(
        modifier = widthMod
            .scale(animatedScale)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(brush)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x55FFFFFF), Color(0x10FFFFFF))
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            glyph?.let {
                AppleIcon(glyph = it, tint = iconColor, size = 18.dp)
            }
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}
