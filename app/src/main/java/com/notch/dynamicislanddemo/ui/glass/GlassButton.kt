package com.notch.dynamicislanddemo.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.catalog.components.LiquidButton

enum class GlassButtonStyle {
    PRIMARY,
    SECONDARY,
    ACCENT,
    DESTRUCTIVE
}

/**
 * Glass button wrapper using catalog's LiquidButton
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    isInteractive: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    val (tint, surfaceColor) = when (style) {
        GlassButtonStyle.PRIMARY -> {
            val tintColor = if (isLightTheme) {
                Color(0xFF007AFF)
            } else {
                Color(0xFF0A84FF)
            }
            tintColor to Color.Transparent
        }
        GlassButtonStyle.SECONDARY -> {
            Color.Unspecified to if (isLightTheme) {
                Color(0xFFF2F2F7).copy(alpha = 0.8f)
            } else {
                Color(0xFF2C2C2E).copy(alpha = 0.8f)
            }
        }
        GlassButtonStyle.ACCENT -> {
            val tintColor = if (isLightTheme) {
                Color(0xFFFF0080)
            } else {
                Color(0xFFFF2D55)
            }
            tintColor to Color.Transparent
        }
        GlassButtonStyle.DESTRUCTIVE -> {
            val tintColor = if (isLightTheme) {
                Color(0xFFFF3B30)
            } else {
                Color(0xFFFF453A)
            }
            tintColor to Color.Transparent
        }
    }
    
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.height(56.dp),
        isInteractive = isInteractive,
        tint = tint,
        surfaceColor = surfaceColor,
        content = content
    )
}

/**
 * Text-only glass button variant
 */
@Composable
fun GlassTextButton(
    text: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    isInteractive: Boolean = true
) {
    val isLightTheme = !isSystemInDarkTheme()
    val textColor = when (style) {
        GlassButtonStyle.PRIMARY -> Color.White
        GlassButtonStyle.SECONDARY -> if (isLightTheme) Color.Black else Color.White
        GlassButtonStyle.ACCENT -> Color.White
        GlassButtonStyle.DESTRUCTIVE -> Color.White
    }
    
    GlassButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        style = style,
        isInteractive = isInteractive
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
