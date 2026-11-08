package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Animated sweep-gradient progress ring for timer / chronometer activities.
 */
@Composable
fun TimerProgressRing(
    progress: Float = 0.72f,
    size: Dp = 21.dp,
    strokeWidth: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        // Dark track
        drawCircle(
            color = IslandColors.Gray6,
            radius = (this.size.minDimension - strokePx) / 2f,
            style = Stroke(width = strokePx)
        )
        // Sweep gradient cyan to blue
        val brush = Brush.sweepGradient(
            listOf(
                IslandColors.TimerCyan,
                IslandColors.TimerBlue,
                IslandColors.TimerCyan
            )
        )
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
internal fun TimerCompactContent(label: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimerProgressRing(progress = progress, size = 18.dp, strokeWidth = 2.5.dp)
            Text(
                text = "Timer",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = IslandColors.CapsuleOrange
            )
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IslandColors.CapsuleOrange,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
internal fun TimerExpandedContent(
    label: String,
    timerRunning: Boolean,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    val isPaused = !timerRunning
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (16.dp * scale)
        val vPad = (12.dp * scale)
        val btnSize = (42.dp * scale).coerceAtLeast(36.dp)
        val iconSize = (20.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Pause Button (52dp translucent orange) + Cancel Button (52dp charcoal neutral)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.5.dp * scale)
            ) {
                Box(
                    modifier = Modifier
                        .size(btnSize)
                        .clip(CircleShape)
                        .background(Color(0xFFFB8B28).copy(alpha = if (isPaused) 0.85f else 0.45f))
                        .clickable {
                            onControlAction(IslandControlAction.TimerToggle)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (isPaused) AppleGlyph.Play else AppleGlyph.Pause,
                        tint = if (isPaused) Color.White else Color(0xFFFB8B28),
                        size = iconSize
                    )
                }

                Box(
                    modifier = Modifier
                        .size(btnSize)
                        .clip(CircleShape)
                        .background(Color(0xFF838388).copy(alpha = 0.7f))
                        .clickable { onControlAction(IslandControlAction.TimerCancel) },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Close,
                        tint = Color.White,
                        size = iconSize
                    )
                }
            }

            // RIGHT: Label "Timer" (#FB8B28, 12sp) + Time (#FB8B28 bold 30sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale)
            ) {
                Text(
                    text = "Timer",
                    color = IslandColors.CapsuleOrange,
                    fontSize = (12f * scale).sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isPaused) "Paused" else label,
                    color = IslandColors.CapsuleOrange,
                    fontSize = (25f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
