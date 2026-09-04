package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.motion.AppMotion
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

internal fun batteryColorFor(percentage: Int): Color = when {
    percentage >= 20 -> IslandColors.Green
    percentage >= 10 -> IslandColors.Orange
    else -> IslandColors.Red
}

/**
 * CHARGING — leading capsule.
 *
 * The compact split capsule only has room for a percentage, exactly like the
 * iOS charging live activity. The ring lives in the trailing bubble.
 */
@Composable
fun ChargingIslandMain(
    percentage: Int,
    isLowBattery: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = batteryColorFor(percentage)
    val glow by rememberBreathing(min = 0.55f, max = 1f, durationMillis = 1800)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AppleIcon(
            glyph = if (isLowBattery) AppleGlyph.Battery else AppleGlyph.Power,
            tint = color,
            size = 13.dp,
            modifier = Modifier.graphicsLayer { alpha = if (isLowBattery) 1f else glow }
        )
        AppText(
            text = "$percentage%",
            style = AppTheme.typography.islandSubtitle,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * iOS-style battery glyph: rounded shell, nub, animated inner fill.
 */
@Composable
fun BatteryIcon(
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val fill by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = AppMotion.settleSpring(),
        label = "battery_fill"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val bodyWidth = width * 0.85f
        val cornerRadius = height * 0.35f

        val nubWidth = width * 0.08f
        val nubHeight = height * 0.35f
        val nubX = bodyWidth + 1.5.dp.toPx()
        val nubY = (height - nubHeight) / 2

        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, height),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(nubX, nubY),
            size = Size(nubWidth, nubHeight),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )

        val fillPadding = 1.5.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val fillHeight = height - fillPadding * 2
        val fillCorner = (cornerRadius - fillPadding).coerceAtLeast(0f)

        if (fill > 0f) {
            drawRoundRect(
                color = color,
                topLeft = Offset(fillPadding, fillPadding),
                size = Size((maxFillWidth * fill).coerceAtLeast(fillHeight), fillHeight),
                cornerRadius = CornerRadius(fillCorner, fillCorner)
            )
        }
    }
}

/**
 * CHARGING — trailing bubble: a hand-drawn progress ring so it stays crisp at
 * the true 37pt bubble size (Material's indicator is too heavy here).
 */
@Composable
fun ChargingIslandSide(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val ringColor = batteryColorFor(percentage)
    val progress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = AppMotion.settleSpring(),
        label = "charge_ring"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val stroke = 2.6.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = ringColor.copy(alpha = 0.22f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        AppleIcon(
            glyph = AppleGlyph.Power,
            tint = ringColor,
            size = 10.dp
        )
    }
}
