package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.sin
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * The shared spatial canvas for every screen.
 *
 * The foundation stays true black while slow, low-luminance spectral light moves below the
 * content. Motion is intentionally long and asynchronous: it should feel alive in peripheral
 * vision, never like a looping decoration. Canvas rendering keeps the effect resolution and
 * device-size independent without bitmap assets.
 */
@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ambient_spectrum")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_phase"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_breathe"
    )

    val isDark = AuroraTheme.colors.isDark
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraTheme.colors.backgroundBase)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Force one composited layer so gradients remain smooth on older GPUs.
                .graphicsLayer { alpha = 0.98f }
        ) {
            drawAtmosphere(if (animated) phase else 0.42f, if (animated) breathe else 1f, isDark)
        }
        content()
    }
}

private fun DrawScope.drawAtmosphere(phase: Float, breathe: Float, isDark: Boolean) {
    val w = size.width
    val h = size.height

    if (!isDark) drawRect(Color(0xFFF8F9FB))
    // Stable Gemini-like horizon. It anchors the layout even when animation is disabled.
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.52f to Color.Transparent,
                0.76f to if (isDark) Color(0x080B2B5B) else Color(0x102A8BF2),
                1.00f to if (isDark) Color(0x24114D73) else Color(0x523FA9F5)
            )
        )
    )

    // Three independently drifting spectral pools. Their low alpha protects text contrast.
    val drift = sin(phase * Math.PI.toFloat() * 2f)
    ambientPool(
        center = Offset(w * (0.14f + phase * 0.18f), h * 0.97f),
        radius = w * 0.82f * breathe,
        core = if (isDark) Color(0x343F2B96) else Color(0x283E8FF4)
    )
    ambientPool(
        center = Offset(w * (0.88f - phase * 0.22f), h * (0.88f + drift * 0.025f)),
        radius = w * 0.74f,
        core = if (isDark) Color(0x2A006E83) else Color(0x2439C2E4)
    )
    ambientPool(
        center = Offset(w * (0.50f + drift * 0.12f), h * 1.08f),
        radius = w * 0.64f * (2f - breathe),
        core = if (isDark) Color(0x2A174EA6) else Color(0x283A72E8)
    )

    // A nearly invisible top bloom gives tall and foldable layouts depth without tinting chrome.
    ambientPool(
        center = Offset(w * 0.72f, -h * 0.08f),
        radius = w * 0.68f,
        core = if (isDark) Color(0x0D50398A) else Color(0x0A79AFFF)
    )
}

private fun DrawScope.ambientPool(center: Offset, radius: Float, core: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to core,
                0.46f to core.copy(alpha = core.alpha * 0.36f),
                1f to Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius,
        blendMode = BlendMode.Screen
    )
}
