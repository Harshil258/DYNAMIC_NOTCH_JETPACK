package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Animated waveform visualization for music player
 */
@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    color: Color? = null,
    gradient: List<Color>? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    // 6 Bars (Thicker and Shorter look)
    val animations = (0..5).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + (index * 130),
                    easing = FastOutSlowInEasing,
                    delayMillis = index * 50
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }
    
    // Enhanced Gradient: Light to Dark Pink (Matches image provided)
    val activeGradient = when {
        gradient != null -> gradient
        color != null -> listOf(color, color)
        else -> listOf(
            Color(0xFFFF85C2), // Lighter Pink
            Color(0xFFFF2976)  // Darker Pink/Red
        )
    }
    
    // Taller and narrower for "thin and long" look
    Canvas(modifier = modifier.size(32.dp, 26.dp)) {
        val barCount = animations.size
        // Standard spacing
        val spacingPx = 2.dp.toPx() 
        val availableWidth = size.width - ((barCount - 1) * spacingPx)
        val barWidth = availableWidth / barCount
        
        val maxHeight = size.height
        
        animations.forEachIndexed { index, animState ->
            val heightFraction = animState.value
            val barHeight = maxHeight * heightFraction
            
            val x = index * (barWidth + spacingPx)
            val y = (maxHeight - barHeight) / 2f
            
            // Create a gradient specific to this bar's height/position
            val barBrush = Brush.verticalGradient(
                colors = activeGradient,
                startY = 0f, 
                endY = maxHeight
            )
            
            drawRoundRect(
                brush = barBrush,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}
