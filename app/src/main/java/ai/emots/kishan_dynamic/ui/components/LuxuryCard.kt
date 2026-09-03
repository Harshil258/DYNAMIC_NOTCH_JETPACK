package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

/** Quiet, content-first surface shared by every settings and activity screen. */
@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    shape: Shape = AuroraTokens.Shape.cardLarge,
    padding: Dp = AppTheme.layout.cardPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = AuroraTokens.Motion.springSnappy,
        label = "surface_press"
    )
    val click = if (onClick != null) Modifier.clickable(
        interactionSource = interactions,
        indication = null,
        onClick = onClick
    ) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(AppTheme.colors.surfaceElevated, AppTheme.colors.surface)
                )
            )
            .border(
                0.75.dp,
                Brush.verticalGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle)),
                shape
            )
            .then(click)
            .padding(padding),
        content = content
    )
}
