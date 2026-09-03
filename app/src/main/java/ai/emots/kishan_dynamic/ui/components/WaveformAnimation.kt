package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    
    val activeGradient = when {
        gradient != null -> gradient
        color != null -> listOf(color, color)
        else -> listOf(
            Color(0xFFFF85C2), // Lighter Pink
            Color(0xFFFF2976)  // Darker Pink/Red
        )
    }
    
    Canvas(modifier = modifier.size(32.dp, 26.dp)) {
        val barCount = animations.size
        val spacingPx = 2.dp.toPx() 
        val availableWidth = size.width - ((barCount - 1) * spacingPx)
        val barWidth = availableWidth / barCount
        val maxHeight = size.height
        
        animations.forEachIndexed { index, animState ->
            val heightFraction = animState.value
            val barHeight = maxHeight * heightFraction
            
            val x = index * (barWidth + spacingPx)
            val y = (maxHeight - barHeight) / 2f
            
            val barBrush = Brush.verticalGradient(
                colors = activeGradient,
                startY = 0f, 
                endY = maxHeight
            )
            
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

/**
 * Waveform Bars for Ongoing Call
 */
@Composable
fun WaveformBarsAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "callWaveform")
    
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar5"
    )
    
    val iOSGreen = Color(0xFF30D158)
    val waveformGradient = listOf(
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        iOSGreen
    )
    
    Canvas(modifier = modifier.size(width = 32.dp, height = 16.dp)) {
        val barWidth = 2.5.dp.toPx()
        val gap = 2.dp.toPx()
        val maxHeight = size.height
        val centerY = size.height / 2
        
        val bars = listOf(bar1, bar2, bar3, bar4, bar5)
        
        bars.forEachIndexed { index, scale ->
            val x = index * (barWidth + gap)
            val barHeight = maxHeight * scale * 0.8f
            val topY = centerY - barHeight / 2
            
            val gradientIndex = (index.toFloat() / bars.size * (waveformGradient.size - 1)).toInt()
            val barColor = waveformGradient.getOrElse(gradientIndex) { iOSGreen }
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
