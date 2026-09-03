package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Silky ambient wave ribbons flowing smoothly across the bottom of the screen.
 * Recreates the ethereal wave graphics seen in premium AI interfaces.
 */
@Composable
fun AmbientRibbonWave(
    modifier: Modifier = Modifier,
    height: Dp = 90.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val h = size.height

        // Wave 1: Oceanic Deep Cyan
        val path1 = Path().apply {
            moveTo(0f, h * 0.5f)
            val steps = 40
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * width
                val y = (h * 0.5f) + (h * 0.28f * sin((i.toFloat() / steps) * Math.PI * 2.5 + phase).toFloat())
                lineTo(x, y)
            }
            lineTo(width, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x3300F5D4),
                    Color(0x100A2533),
                    Color.Transparent
                )
            )
        )

        // Wave 2: Royal Violet
        val path2 = Path().apply {
            moveTo(0f, h * 0.65f)
            val steps = 40
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * width
                val y = (h * 0.60f) + (h * 0.24f * sin((i.toFloat() / steps) * Math.PI * 2.0 - phase * 0.8f).toFloat())
                lineTo(x, y)
            }
            lineTo(width, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x407C3AED),
                    Color(0x1A4C1D95),
                    Color.Transparent
                )
            )
        )
    }
}
