package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Live equalizer, rebuilt from the reference artwork.
 *
 * Geometry is measured from the waveform symbol on
 * `design/reference/ios17-dynamic-island/Dynamic Island/Minimal.svg`: six
 * capsule bars, each `1.42` wide on a `2.96` pitch (so the gap is `1.54`),
 * radius = half the bar width, with the resting heights below. The default
 * tint is the reference gradient `#F84BAB -> #B4CDFB`.
 */
private val BarRestingHeights = floatArrayOf(0.223f, 0.615f, 1.000f, 0.498f, 0.788f, 0.306f)

private const val BAR_WIDTH_UNITS = 1.42f
private const val BAR_GAP_UNITS = 1.54f

/** Reference equalizer gradient, sampled from `paint0_linear` in Minimal.svg. */
val EqualizerGradient: List<Color> = listOf(IslandColors.EqualizerPink, IslandColors.EqualizerBlue)

@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    color: Color? = null,
    gradient: List<Color>? = null,
    animated: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "waveform")

    val levels = BarRestingHeights.mapIndexed { index, resting ->
        if (!animated) {
            null
        } else {
            transition.animateFloat(
                initialValue = (resting * 0.42f).coerceAtLeast(0.14f),
                targetValue = resting,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 380 + index * 90,
                        easing = FastOutSlowInEasing,
                        delayMillis = index * 40,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar$index",
            )
        }
    }

    val colors = when {
        gradient != null -> gradient
        color != null -> listOf(color, color)
        else -> EqualizerGradient
    }

    Canvas(modifier = modifier.defaultMinSize(minWidth = 17.dp, minHeight = 14.dp)) {
        val count = BarRestingHeights.size
        val units = count * BAR_WIDTH_UNITS + (count - 1) * BAR_GAP_UNITS
        val unit = size.width / units
        val barWidth = BAR_WIDTH_UNITS * unit
        val pitch = (BAR_WIDTH_UNITS + BAR_GAP_UNITS) * unit
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)
        // the reference gradient runs across the bars, not down them
        val brush = Brush.horizontalGradient(colors, startX = 0f, endX = size.width)

        BarRestingHeights.forEachIndexed { index, resting ->
            val level = levels[index]?.value ?: resting
            val barHeight = (size.height * level).coerceAtLeast(barWidth)
            drawRoundRect(
                brush = brush,
                topLeft = Offset(index * pitch, (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = radius,
            )
        }
    }
}
