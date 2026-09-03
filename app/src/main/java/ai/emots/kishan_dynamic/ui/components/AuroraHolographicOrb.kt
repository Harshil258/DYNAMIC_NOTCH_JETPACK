package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Luminous Holographic Aurora Orb with fluid multi-color neon filament threads.
 * Inspired by high-end Apple / Siri / Cyberpunk holographic sphere aesthetics.
 */
@Composable
fun AuroraHolographicOrb(
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val centerOffset = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) * 0.82f * pulseScale

            // 1. Ambient Radial Atmospheric Bloom behind Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x667C3AED), // Luminous Violet
                        Color(0x33F43F5E), // Rose Magenta
                        Color(0x1A06B6D4), // Cyan
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = centerOffset
            )

            // 2. Deep Obsidian Sphere Shading
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF1E1433),
                    0.65f to Color(0xFF0F0A1E),
                    1.0f to Color(0xFF05030A),
                    center = Offset(centerOffset.x - radius * 0.25f, centerOffset.y - radius * 0.25f),
                    radius = radius
                ),
                radius = radius,
                center = centerOffset
            )

            // 3. Primary Luminous Filament Ring (Magenta & Orange/Gold)
            rotate(rotation1, centerOffset) {
                val path1 = Path().apply {
                    val steps = 60
                    for (i in 0..steps) {
                        val angle = (i.toFloat() / steps) * 2 * Math.PI
                        val r = radius * (0.92f + 0.08f * sin(angle * 3).toFloat())
                        val x = centerOffset.x + (r * cos(angle)).toFloat()
                        val y = centerOffset.y + (r * sin(angle) * 0.55f).toFloat() // squashed 3D tilt
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    path = path1,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF2E93), // Vivid Magenta
                            Color(0xFFFFAE19), // Warm Gold
                            Color(0xFF6D82FF), // Violet
                            Color(0xFFFF2E93)
                        )
                    ),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Secondary Counter-Rotating Filament Ring (Electric Cyan & Purple)
            rotate(rotation2, centerOffset) {
                val path2 = Path().apply {
                    val steps = 60
                    for (i in 0..steps) {
                        val angle = (i.toFloat() / steps) * 2 * Math.PI
                        val r = radius * (0.86f + 0.10f * cos(angle * 2).toFloat())
                        val x = centerOffset.x + (r * cos(angle) * 0.70f).toFloat()
                        val y = centerOffset.y + (r * sin(angle)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    path = path2,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00F5D4), // Electric Cyan
                            Color(0xFF8B5CF6), // Royal Lavender
                            Color(0xFFEC4899), // Pink
                            Color(0xFF00F5D4)
                        )
                    ),
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 5. Specular Top Rim Reflection (Physical Glass Highlight)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x40FFFFFF),
                        Color(0x10FFFFFF),
                        Color.Transparent
                    ),
                    center = Offset(centerOffset.x - radius * 0.35f, centerOffset.y - radius * 0.45f),
                    radius = radius * 0.5f
                ),
                radius = radius * 0.45f,
                center = Offset(centerOffset.x - radius * 0.35f, centerOffset.y - radius * 0.45f)
            )
        }
    }
}
