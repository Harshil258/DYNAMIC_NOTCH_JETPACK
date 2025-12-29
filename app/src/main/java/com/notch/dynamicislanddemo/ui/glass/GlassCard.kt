package com.notch.dynamicislanddemo.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.capsule.ContinuousRoundedRectangle

/**
 * Reusable glass card component with Apple-inspired design
 */
@Composable
fun GlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    padding: Dp = 16.dp,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val density = LocalDensity.current
    val defaultContainerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.4f)
    } else {
        Color(0xFF1C1C1E).copy(alpha = 0.5f)
    }
    
    val finalContainerColor = containerColor ?: defaultContainerColor
    
    
    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(with(density) { cornerRadius.toPx() }) },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                },
                highlight = {
                    Highlight(
                        style = HighlightStyle.Default(
                            angle = 45f,
                            falloff = 2f
                        ),
                        alpha = if (isLightTheme) 0.3f else 0.5f
                    )
                },
                onDrawSurface = {
                    drawRect(finalContainerColor)
                }
            )
            .padding(padding)
    ) {
        content()
    }
}

/**
 * Glass card without backdrop effects (for static content)
 */
@Composable
fun SimpleGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    padding: Dp = 16.dp,
    containerColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val defaultContainerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.15f)
    } else {
        Color(0xFF2C2C2E).copy(alpha = 0.6f)
    }
    
    val finalContainerColor = containerColor ?: defaultContainerColor
    
    Box(
        modifier = modifier
            .background(
                color = finalContainerColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .padding(padding)
    ) {
        content()
    }
}
