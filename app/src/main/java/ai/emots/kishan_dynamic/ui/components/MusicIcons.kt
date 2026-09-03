package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS-style Rewind Icon (Double triangles pointing left)
 */
@Composable
fun RewindIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        
        val padding = w * 0.08f
        val triangleWidth = (w - padding * 2) * 0.55f
        
        val path1 = Path().apply {
            moveTo(w - padding, h * 0.15f)
            lineTo(w - padding, h * 0.85f)
            lineTo(w - padding - triangleWidth, h * 0.5f)
            close()
        }
        
        val overlap = triangleWidth * 0.15f
        val path2 = Path().apply {
            moveTo(w - padding - triangleWidth + overlap, h * 0.15f)
            lineTo(w - padding - triangleWidth + overlap, h * 0.85f)
            lineTo(padding, h * 0.5f)
            close()
        }
        
        drawPath(path2, color)
        drawPath(path1, color)
    }
}

/**
 * iOS-style Fast Forward Icon (Double triangles pointing right)
 */
@Composable
fun FastForwardIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        
        val padding = w * 0.08f
        val triangleWidth = (w - padding * 2) * 0.55f
        
        val path1 = Path().apply {
            moveTo(padding, h * 0.15f)
            lineTo(padding, h * 0.85f)
            lineTo(padding + triangleWidth, h * 0.5f)
            close()
        }
        
        val overlap = triangleWidth * 0.15f
        val path2 = Path().apply {
            moveTo(padding + triangleWidth - overlap, h * 0.15f)
            lineTo(padding + triangleWidth - overlap, h * 0.85f)
            lineTo(w - padding, h * 0.5f)
            close()
        }
        
        drawPath(path1, color)
        drawPath(path2, color)
    }
}

/**
 * iOS-style Play Icon (Large filled triangle)
 */
@Composable
fun PlayIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        
        val leftPadding = w * 0.2f
        val rightPadding = w * 0.1f
        val topPadding = h * 0.1f
        
        val path = Path().apply {
            moveTo(leftPadding, topPadding)
            lineTo(leftPadding, h - topPadding)
            lineTo(w - rightPadding, h * 0.5f)
            close()
        }
        
        drawPath(path, color)
    }
}

/**
 * iOS-style Pause Icon (Two vertical bars)
 */
@Composable
fun PauseIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        
        val barWidth = w * 0.22f
        val gap = w * 0.18f
        val topPadding = h * 0.15f
        val cornerRadius = barWidth * 0.15f
        
        val leftBarStart = (w - barWidth * 2 - gap) / 2
        drawRoundRect(
            color = color,
            topLeft = Offset(leftBarStart, topPadding),
            size = Size(barWidth, h - topPadding * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
        
        drawRoundRect(
            color = color,
            topLeft = Offset(leftBarStart + barWidth + gap, topPadding),
            size = Size(barWidth, h - topPadding * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
    }
}

/**
 * iOS-style AirPlay Icon (Triangle with concentric arcs)
 */
@Composable
fun AirPlayIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val centerX = w / 2
        
        val triangleHeight = h * 0.35f
        val triangleWidth = w * 0.4f
        val triangleTop = h * 0.55f
        
        val trianglePath = Path().apply {
            moveTo(centerX, triangleTop)
            lineTo(centerX - triangleWidth / 2, h * 0.95f)
            lineTo(centerX + triangleWidth / 2, h * 0.95f)
            close()
        }
        drawPath(trianglePath, color)
        
        val strokeWidth = w * 0.08f
        val arcStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        
        val innerRadius = w * 0.18f
        drawArc(
            color = color,
            startAngle = 220f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centerX - innerRadius, h * 0.25f - innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            style = arcStyle
        )
        
        val middleRadius = w * 0.32f
        drawArc(
            color = color,
            startAngle = 220f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centerX - middleRadius, h * 0.25f - middleRadius),
            size = Size(middleRadius * 2, middleRadius * 2),
            style = arcStyle
        )
        
        val outerRadius = w * 0.46f
        drawArc(
            color = color,
            startAngle = 220f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centerX - outerRadius, h * 0.25f - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = arcStyle
        )
    }
}

/**
 * Waveform visualization bars
 */
@Composable
fun WaveformBars(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF1493),
    barCount: Int = 5,
    heights: List<Float> = listOf(0.4f, 0.7f, 1f, 0.6f, 0.3f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val barWidth = w / (barCount * 2 - 1)
        val maxBarHeight = h * 0.9f
        
        heights.forEachIndexed { index, heightFraction ->
            val barHeight = maxBarHeight * heightFraction
            val x = index * barWidth * 2
            val y = (h - barHeight) / 2
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
