package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.data.model.RingerModeType

/**
 * RINGER HUD (Compact).
 *
 * On iOS this is a compact presentation: the ring/silent glyph on the leading edge
 * and a single word on the trailing edge.
 */
@Composable
fun RingerModeIsland(
    isSilent: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accent = if (isSilent) IslandColors.CapsuleRed else Color.White

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppleIcon(
            glyph = if (isSilent) AppleGlyph.BellSlash else AppleGlyph.Bell,
            tint = accent,
            size = 17.dp
        )

        AppText(
            text = if (isSilent) "Silent" else "Ring",
            style = AppTheme.typography.islandSubtitle,
            color = accent.copy(alpha = 0.95f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/** Shared mode-aware glyph used by both compact and expanded ringer surfaces. */
@Composable
fun RingerModeGlyph(
    mode: RingerModeType,
    tint: Color,
    glyphSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    when (mode) {
        RingerModeType.SILENT -> AppleIcon(
            glyph = AppleGlyph.BellSlash,
            tint = tint,
            size = glyphSize,
            modifier = modifier
        )
        RingerModeType.NORMAL -> AppleIcon(
            glyph = AppleGlyph.Bell,
            tint = tint,
            size = glyphSize,
            modifier = modifier
        )
        RingerModeType.VIBRATE -> Canvas(modifier = modifier.size(glyphSize)) {
            val strokeWidth = (size.minDimension * 0.1f).coerceAtLeast(1f)
            val bodyWidth = size.width * 0.34f
            val bodyHeight = size.height * 0.54f
            val bodyLeft = (size.width - bodyWidth) / 2f
            val bodyTop = (size.height - bodyHeight) / 2f
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            drawRoundRect(
                color = tint,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(strokeWidth * 1.5f),
                style = stroke
            )
            drawArc(
                color = tint,
                startAngle = 220f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(0f, size.height * 0.22f),
                size = Size(size.width * 0.35f, size.height * 0.56f),
                style = stroke
            )
            drawArc(
                color = tint,
                startAngle = -40f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(size.width * 0.65f, size.height * 0.22f),
                size = Size(size.width * 0.35f, size.height * 0.56f),
                style = stroke
            )
        }
    }
}

/**
 * SILENT MODE ALERT (86dp capsule) matching `Dynamic Island-9.svg`.
 *
 * Left: BellSlash icon (24dp white)
 * Center: "Silent Mode" (#A4A4A9, 12sp) + "On" (white bold 17sp)
 * Right: Dark pill badge (79x34dp, rx=17dp, #2C2C2D) with "Silent" / "Unmute"
 */
@Composable
fun RingerModeIslandExpanded(
    mode: RingerModeType = RingerModeType.SILENT,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSilent = mode == RingerModeType.SILENT
    val modeLabel = when (mode) {
        RingerModeType.SILENT -> "Silent mode"
        RingerModeType.VIBRATE -> "Vibrate mode"
        RingerModeType.NORMAL -> "Ringer on"
    }
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (24.dp * scale)
        val vPad = (16.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RingerModeGlyph(
                    mode = mode,
                    tint = Color.White,
                    glyphSize = 26.dp * scale
                )

                Spacer(modifier = Modifier.width(16.dp * scale))

                    Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    AppText(
                        text = modeLabel,
                        style = AppTheme.typography.caption,
                        fontSize = (12f * scale).sp,
                        color = Color(0xFFA4A4A9),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    AppText(
                        text = when (mode) {
                            RingerModeType.SILENT -> "On"
                            RingerModeType.VIBRATE -> "Active"
                            RingerModeType.NORMAL -> "Active"
                        },
                        style = AppTheme.typography.islandTitle,
                        fontSize = (17.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RIGHT: Dark pill badge (79dp x 34dp, rx=17dp, #2C2C2D) with Unmute button text
            Box(
                modifier = Modifier
                    .width(79.dp * scale)
                    .height(34.dp * scale)
                    .clip(RoundedCornerShape(17.dp * scale))
                    .background(Color(0xFF2C2C2D))
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                    Text(
                        text = if (mode == RingerModeType.NORMAL) "Mute" else "Unmute",
                    color = Color.White,
                    fontSize = (14f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
