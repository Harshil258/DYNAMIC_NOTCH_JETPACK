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
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.timerVerticalInset
import ai.emots.kishan_dynamic.ui.theme.timerControlGap
import ai.emots.kishan_dynamic.ui.theme.timerCancelColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

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
            if (androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1.3f) Text(
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
            fontFamily = ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts.Text
        )
    }
}

@Composable
internal fun TimerExpandedContent(label: String, timerRunning: Boolean, onControlAction: (IslandControlAction) -> Unit = {}) {
    val paused = !timerRunning
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f || maxWidth < 290.dp
        val controls: @Composable () -> Unit = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.island.timerControlGap)) {
                if (canRenderIslandControl(IslandControlAction.TimerToggle)) androidx.compose.material3.IconButton(
                    onClick = { onControlAction(IslandControlAction.TimerToggle) },
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                        .background(IslandColors.CapsuleOrange.copy(alpha = if (paused) .85f else .45f))
                        .semantics { contentDescription = if (paused) "Resume timer" else "Pause timer" }
                ) {
                    val glyph = if (paused) AppleGlyph.Play else AppleGlyph.Pause
                    AppleIcon(glyph, tint = if (paused) Color.White else IslandColors.CapsuleOrange, size = glyph.figmaInkSize())
                }
                if (canRenderIslandControl(IslandControlAction.TimerCancel)) androidx.compose.material3.IconButton(
                    onClick = { onControlAction(IslandControlAction.TimerCancel) },
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(IslandColors.timerCancelColor.copy(alpha = .7f))
                        .semantics { contentDescription = "Cancel timer" }
                ) { AppleIcon(AppleGlyph.Close, tint = Color.White, size = AppleGlyph.Close.figmaInkSize()) }
            }
        }
        val clock: @Composable () -> Unit = {
            if (stacked) Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Text(if (paused) "Paused" else "Timer", color = IslandColors.CapsuleOrange,
                    style = AppTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 18.sp))
                Text(label, color = IslandColors.CapsuleOrange, maxLines = 1, softWrap = false,
                    style = AppTheme.typography.islandTime.copy(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold))
            } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Text(if (paused) "Paused" else "Timer", modifier = Modifier.alignByBaseline(), color = IslandColors.CapsuleOrange,
                    style = AppTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 18.sp))
                Text(label, modifier = Modifier.alignByBaseline(), color = IslandColors.CapsuleOrange, maxLines = 1, softWrap = false,
                    style = AppTheme.typography.islandTime.copy(fontSize = 32.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
            }
        }
        if (stacked) Column(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.island.timerVerticalInset),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) { clock(); controls() }
        else Row(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.island.timerVerticalInset)
            .heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            controls(); clock()
        }
    }
}
