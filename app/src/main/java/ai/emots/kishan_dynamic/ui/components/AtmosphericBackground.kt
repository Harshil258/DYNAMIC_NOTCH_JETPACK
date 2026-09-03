package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Editorial near-true black background with an extremely subtle, atmospheric midnight blue
 * aura emerging gently from the bottom of the canvas, inspired by Gemini & Apple dark spatial design.
 * Creates natural depth and spatial presence without decorative noise or neon saturation.
 */
@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft ethereal midnight glow rising naturally from the bottom edge
            val bottomRadialCenter = Offset(width * 0.5f, height * 1.08f)
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0x280E1C38),   // Gentle midnight indigo
                        0.45f to Color(0x14081020),  // Deep quiet twilight
                        0.80f to Color(0x05040812),
                        1.0f to Color.Transparent
                    ),
                    center = bottomRadialCenter,
                    radius = width * 1.1f
                ),
                radius = width * 1.1f,
                center = bottomRadialCenter
            )
        }

        content()
    }
}
