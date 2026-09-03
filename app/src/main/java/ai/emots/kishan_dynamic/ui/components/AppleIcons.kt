package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppleGlyph {
    Notch,
    Bell,
    Music,
    Phone,
    Battery,
    Controls,
    ChevronLeft,
    ChevronRight,
    Crown,
    Play,
    Pause,
    Check,
    Settings,
    Power,
    Shield,
    Wifi,
    Bluetooth,
    Torch,
    Location,
    Rotate,
    Mute,
    Airplane,
    Expand,
    Reset,
    History,
    Star,
    Sparkles,
    Timer,
    Maps,
    Backward,
    Forward,
    AirPlay,
    Speaker,
    BellSlash,
    Close,
    Search,
    Globe,
    Palette,
    Heart
}

/**
 * Pure custom Apple SF-grade vector iconography rendered via Canvas.
 * Free from any Android Material design artifacts.
 */
@Composable
fun AppleIcon(
    glyph: AppleGlyph,
    tint: Color = Color.White,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (w * 0.09f).coerceAtLeast(1.5f)
        val stroke = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (glyph) {
            AppleGlyph.ChevronLeft -> {
                val path = Path().apply {
                    moveTo(w * 0.65f, h * 0.20f)
                    lineTo(w * 0.35f, h * 0.50f)
                    lineTo(w * 0.65f, h * 0.80f)
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.ChevronRight -> {
                val path = Path().apply {
                    moveTo(w * 0.35f, h * 0.20f)
                    lineTo(w * 0.65f, h * 0.50f)
                    lineTo(w * 0.35f, h * 0.80f)
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Notch -> {
                // Dual pill camera hole
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(w * 0.15f, h * 0.30f, w * 0.85f, h * 0.70f),
                            radiusX = h * 0.20f,
                            radiusY = h * 0.20f
                        )
                    )
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.35f, h * 0.50f), style = Fill)
            }

            AppleGlyph.Bell -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.15f)
                    cubicTo(w * 0.32f, h * 0.15f, w * 0.28f, h * 0.40f, w * 0.25f, h * 0.65f)
                    lineTo(w * 0.18f, h * 0.72f)
                    cubicTo(w * 0.18f, h * 0.75f, w * 0.22f, h * 0.75f, w * 0.25f, h * 0.75f)
                    lineTo(w * 0.75f, h * 0.75f)
                    cubicTo(w * 0.78f, h * 0.75f, w * 0.82f, h * 0.75f, w * 0.82f, h * 0.72f)
                    lineTo(w * 0.75f, h * 0.65f)
                    cubicTo(w * 0.72f, h * 0.40f, w * 0.68f, h * 0.15f, w * 0.50f, h * 0.15f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                // Clapper
                drawArc(
                    color = tint,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.40f, h * 0.78f),
                    size = Size(w * 0.20f, h * 0.12f),
                    style = stroke
                )
            }

            AppleGlyph.Music -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.75f)
                    lineTo(w * 0.32f, h * 0.28f)
                    lineTo(w * 0.78f, h * 0.20f)
                    lineTo(w * 0.78f, h * 0.68f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.25f, h * 0.75f), style = Fill)
                drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.71f, h * 0.68f), style = Fill)
                // Crossbar
                drawLine(
                    color = tint,
                    start = Offset(w * 0.32f, h * 0.34f),
                    end = Offset(w * 0.78f, h * 0.26f),
                    strokeWidth = strokeWidth * 1.5f,
                    cap = StrokeCap.Round
                )
            }

            AppleGlyph.Phone -> {
                val path = Path().apply {
                    moveTo(w * 0.22f, h * 0.35f)
                    cubicTo(w * 0.20f, h * 0.45f, w * 0.35f, h * 0.75f, w * 0.65f, h * 0.80f)
                    lineTo(w * 0.78f, h * 0.68f)
                    cubicTo(w * 0.80f, h * 0.66f, w * 0.78f, h * 0.60f, w * 0.74f, h * 0.58f)
                    lineTo(w * 0.60f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.60f)
                    cubicTo(w * 0.44f, h * 0.54f, w * 0.40f, h * 0.48f, w * 0.36f, h * 0.40f)
                    lineTo(w * 0.44f, h * 0.32f)
                    lineTo(w * 0.38f, h * 0.18f)
                    close()
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Battery -> {
                // Battery shell
                val rect = Rect(w * 0.10f, h * 0.26f, w * 0.78f, h * 0.74f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                // Battery nub
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.82f, h * 0.40f),
                    size = Size(w * 0.08f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                    style = Fill
                )
                // Lightning Bolt inside
                val bolt = Path().apply {
                    moveTo(w * 0.48f, h * 0.32f)
                    lineTo(w * 0.34f, h * 0.52f)
                    lineTo(w * 0.46f, h * 0.52f)
                    lineTo(w * 0.40f, h * 0.68f)
                    lineTo(w * 0.56f, h * 0.46f)
                    lineTo(w * 0.44f, h * 0.46f)
                    close()
                }
                drawPath(bolt, tint, style = Fill)
            }

            AppleGlyph.Controls -> {
                // Apple Control Center: 4 rounded tile glyph
                val tileW = w * 0.36f
                val tileH = h * 0.36f
                val r = w * 0.10f
                drawRoundRect(tint, Offset(w * 0.10f, h * 0.10f), Size(tileW, tileH), CornerRadius(r, r), stroke)
                drawRoundRect(tint, Offset(w * 0.54f, h * 0.10f), Size(tileW, tileH), CornerRadius(r, r), stroke)
                drawRoundRect(tint, Offset(w * 0.10f, h * 0.54f), Size(tileW, tileH), CornerRadius(r, r), stroke)
                drawRoundRect(tint, Offset(w * 0.54f, h * 0.54f), Size(tileW, tileH), CornerRadius(r, r), stroke)
            }

            AppleGlyph.Crown -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.72f)
                    lineTo(w * 0.15f, h * 0.35f)
                    lineTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.50f, h * 0.24f)
                    lineTo(w * 0.65f, h * 0.52f)
                    lineTo(w * 0.85f, h * 0.35f)
                    lineTo(w * 0.85f, h * 0.72f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.15f, h * 0.80f), Offset(w * 0.85f, h * 0.80f), strokeWidth * 1.2f, StrokeCap.Round)
            }

            AppleGlyph.Play -> {
                val path = Path().apply {
                    moveTo(w * 0.30f, h * 0.20f)
                    lineTo(w * 0.80f, h * 0.50f)
                    lineTo(w * 0.30f, h * 0.80f)
                    close()
                }
                drawPath(path, tint, style = Fill)
            }

            AppleGlyph.Pause -> {
                drawLine(tint, Offset(w * 0.35f, h * 0.22f), Offset(w * 0.35f, h * 0.78f), strokeWidth * 2.2f, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.65f, h * 0.22f), Offset(w * 0.65f, h * 0.78f), strokeWidth * 2.2f, StrokeCap.Round)
            }

            AppleGlyph.Check -> {
                val path = Path().apply {
                    moveTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.74f)
                    lineTo(w * 0.80f, h * 0.28f)
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Settings -> {
                drawCircle(tint, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(tint, radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (Math.PI / 180f)
                    val cos = Math.cos(angle).toFloat()
                    val sin = Math.sin(angle).toFloat()
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.50f + cos * w * 0.35f, h * 0.50f + sin * h * 0.35f),
                        end = Offset(w * 0.50f + cos * w * 0.46f, h * 0.50f + sin * h * 0.46f),
                        strokeWidth = strokeWidth * 1.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            AppleGlyph.Power -> {
                drawArc(
                    color = tint,
                    startAngle = -60f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.70f, h * 0.70f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.45f), strokeWidth, StrokeCap.Round)
            }

            AppleGlyph.Shield -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.12f)
                    lineTo(w * 0.82f, h * 0.25f)
                    lineTo(w * 0.82f, h * 0.52f)
                    cubicTo(w * 0.82f, h * 0.74f, w * 0.50f, h * 0.88f, w * 0.50f, h * 0.88f)
                    cubicTo(w * 0.50f, h * 0.88f, w * 0.18f, h * 0.74f, w * 0.18f, h * 0.52f)
                    lineTo(w * 0.18f, h * 0.25f)
                    close()
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Wifi -> {
                drawArc(color = tint, startAngle = 210f, sweepAngle = 120f, useCenter = false, topLeft = Offset(w * 0.12f, h * 0.20f), size = Size(w * 0.76f, h * 0.60f), style = stroke)
                drawArc(color = tint, startAngle = 210f, sweepAngle = 120f, useCenter = false, topLeft = Offset(w * 0.26f, h * 0.35f), size = Size(w * 0.48f, h * 0.40f), style = stroke)
                drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.74f), style = Fill)
            }

            AppleGlyph.Bluetooth -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.12f)
                    lineTo(w * 0.50f, h * 0.88f)
                    lineTo(w * 0.72f, h * 0.70f)
                    lineTo(w * 0.30f, h * 0.35f)
                    lineTo(w * 0.72f, h * 0.30f)
                    lineTo(w * 0.50f, h * 0.12f)
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Torch -> {
                val path = Path().apply {
                    moveTo(w * 0.30f, h * 0.15f)
                    lineTo(w * 0.70f, h * 0.15f)
                    lineTo(w * 0.60f, h * 0.45f)
                    lineTo(w * 0.60f, h * 0.85f)
                    lineTo(w * 0.40f, h * 0.85f)
                    lineTo(w * 0.40f, h * 0.45f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.48f), Offset(w * 0.50f, h * 0.60f), strokeWidth * 1.5f, StrokeCap.Round)
            }

            AppleGlyph.Location -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.88f)
                    cubicTo(w * 0.30f, h * 0.60f, w * 0.22f, h * 0.45f, w * 0.22f, h * 0.34f)
                    cubicTo(w * 0.22f, h * 0.18f, w * 0.35f, h * 0.12f, w * 0.50f, h * 0.12f)
                    cubicTo(w * 0.65f, h * 0.12f, w * 0.78f, h * 0.18f, w * 0.78f, h * 0.34f)
                    cubicTo(w * 0.78f, h * 0.45f, w * 0.70f, h * 0.60f, w * 0.50f, h * 0.88f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.34f), style = Fill)
            }

            AppleGlyph.Rotate -> {
                drawArc(color = tint, startAngle = -30f, sweepAngle = 260f, useCenter = false, topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.70f, h * 0.70f), style = stroke)
                val arrow = Path().apply {
                    moveTo(w * 0.72f, h * 0.15f)
                    lineTo(w * 0.88f, h * 0.30f)
                    lineTo(w * 0.72f, h * 0.45f)
                }
                drawPath(arrow, tint, style = stroke)
            }

            AppleGlyph.Mute -> {
                val speaker = Path().apply {
                    moveTo(w * 0.20f, h * 0.38f)
                    lineTo(w * 0.35f, h * 0.38f)
                    lineTo(w * 0.52f, h * 0.22f)
                    lineTo(w * 0.52f, h * 0.78f)
                    lineTo(w * 0.35f, h * 0.62f)
                    lineTo(w * 0.20f, h * 0.62f)
                    close()
                }
                drawPath(speaker, tint, style = stroke)
                drawLine(tint, Offset(w * 0.65f, h * 0.35f), Offset(w * 0.85f, h * 0.65f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.85f, h * 0.35f), Offset(w * 0.65f, h * 0.65f), strokeWidth, StrokeCap.Round)
            }

            AppleGlyph.Airplane -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.15f)
                    lineTo(w * 0.55f, h * 0.40f)
                    lineTo(w * 0.88f, h * 0.52f)
                    lineTo(w * 0.88f, h * 0.60f)
                    lineTo(w * 0.55f, h * 0.55f)
                    lineTo(w * 0.55f, h * 0.75f)
                    lineTo(w * 0.68f, h * 0.85f)
                    lineTo(w * 0.68f, h * 0.90f)
                    lineTo(w * 0.50f, h * 0.86f)
                    lineTo(w * 0.32f, h * 0.90f)
                    lineTo(w * 0.32f, h * 0.85f)
                    lineTo(w * 0.45f, h * 0.75f)
                    lineTo(w * 0.45f, h * 0.55f)
                    lineTo(w * 0.12f, h * 0.60f)
                    lineTo(w * 0.12f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.40f)
                    close()
                }
                drawPath(path, tint, style = stroke)
            }

            AppleGlyph.Expand -> {
                // Diagonal opposing arrows
                drawLine(tint, Offset(w * 0.20f, h * 0.20f), Offset(w * 0.42f, h * 0.20f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.20f, h * 0.20f), Offset(w * 0.20f, h * 0.42f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.80f, h * 0.80f), Offset(w * 0.58f, h * 0.80f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.80f, h * 0.80f), Offset(w * 0.80f, h * 0.58f), strokeWidth, StrokeCap.Round)
            }

            AppleGlyph.Reset -> {
                drawArc(color = tint, startAngle = -45f, sweepAngle = 280f, useCenter = false, topLeft = Offset(w * 0.18f, h * 0.18f), size = Size(w * 0.64f, h * 0.64f), style = stroke)
                val arrow = Path().apply {
                    moveTo(w * 0.50f, h * 0.08f)
                    lineTo(w * 0.68f, h * 0.20f)
                    lineTo(w * 0.50f, h * 0.32f)
                }
                drawPath(arrow, tint, style = stroke)
            }

            AppleGlyph.History -> {
                drawCircle(tint, radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.26f), Offset(w * 0.50f, h * 0.50f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.68f, h * 0.50f), strokeWidth, StrokeCap.Round)
            }

            AppleGlyph.Star -> {
                val path = Path().apply {
                    val outerR = w * 0.42f
                    val innerR = w * 0.18f
                    val cx = w * 0.50f
                    val cy = w * 0.50f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val angle = (i * 36f - 90f) * (Math.PI / 180f)
                        val x = cx + Math.cos(angle).toFloat() * r
                        val y = cy + Math.sin(angle).toFloat() * r
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, tint, style = Fill)
            }

            AppleGlyph.Sparkles -> {
                // Apple intelligence sparkles
                drawCircle(tint, radius = w * 0.15f, center = Offset(w * 0.38f, h * 0.38f), style = stroke)
                drawCircle(tint, radius = w * 0.09f, center = Offset(w * 0.70f, h * 0.68f), style = stroke)
            }

            AppleGlyph.Timer -> {
                drawCircle(tint, radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.55f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.32f), Offset(w * 0.50f, h * 0.55f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.55f), Offset(w * 0.64f, h * 0.55f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.40f, h * 0.12f), Offset(w * 0.60f, h * 0.12f), strokeWidth * 1.5f, StrokeCap.Round)
            }

            AppleGlyph.Maps -> {
                val arrow = Path().apply {
                    moveTo(w * 0.50f, h * 0.15f)
                    lineTo(w * 0.82f, h * 0.82f)
                    lineTo(w * 0.50f, h * 0.65f)
                    lineTo(w * 0.18f, h * 0.82f)
                    close()
                }
                drawPath(arrow, tint, style = stroke)
            }

            AppleGlyph.Backward -> {
                // Apple media track backward (double triangle pointing left)
                val tri1 = Path().apply {
                    moveTo(w * 0.50f, h * 0.22f)
                    lineTo(w * 0.15f, h * 0.50f)
                    lineTo(w * 0.50f, h * 0.78f)
                    close()
                }
                val tri2 = Path().apply {
                    moveTo(w * 0.85f, h * 0.22f)
                    lineTo(w * 0.50f, h * 0.50f)
                    lineTo(w * 0.85f, h * 0.78f)
                    close()
                }
                drawPath(tri1, tint, style = Fill)
                drawPath(tri2, tint, style = Fill)
            }

            AppleGlyph.Forward -> {
                // Apple media track forward (double triangle pointing right)
                val tri1 = Path().apply {
                    moveTo(w * 0.15f, h * 0.22f)
                    lineTo(w * 0.50f, h * 0.50f)
                    lineTo(w * 0.15f, h * 0.78f)
                    close()
                }
                val tri2 = Path().apply {
                    moveTo(w * 0.50f, h * 0.22f)
                    lineTo(w * 0.85f, h * 0.50f)
                    lineTo(w * 0.50f, h * 0.78f)
                    close()
                }
                drawPath(tri1, tint, style = Fill)
                drawPath(tri2, tint, style = Fill)
            }

            AppleGlyph.AirPlay -> {
                // Apple AirPlay triangle over speaker base
                val tri = Path().apply {
                    moveTo(w * 0.50f, h * 0.52f)
                    lineTo(w * 0.70f, h * 0.82f)
                    lineTo(w * 0.30f, h * 0.82f)
                    close()
                }
                drawPath(tri, tint, style = Fill)
                // Outer rounded monitor/audio rectangle with bottom cutout
                val screenPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.82f)
                    lineTo(w * 0.15f, h * 0.82f)
                    cubicTo(w * 0.10f, h * 0.82f, w * 0.10f, h * 0.75f, w * 0.10f, h * 0.30f)
                    cubicTo(w * 0.10f, h * 0.22f, w * 0.16f, h * 0.18f, w * 0.25f, h * 0.18f)
                    lineTo(w * 0.75f, h * 0.18f)
                    cubicTo(w * 0.84f, h * 0.18f, w * 0.90f, h * 0.22f, w * 0.90f, h * 0.30f)
                    lineTo(w * 0.90f, h * 0.75f)
                    cubicTo(w * 0.90f, h * 0.82f, w * 0.85f, h * 0.82f, w * 0.75f, h * 0.82f)
                    lineTo(w * 0.75f, h * 0.82f)
                }
                drawPath(screenPath, tint, style = stroke)
            }

            AppleGlyph.Speaker -> {
                val cone = Path().apply {
                    moveTo(w * 0.20f, h * 0.38f)
                    lineTo(w * 0.35f, h * 0.38f)
                    lineTo(w * 0.55f, h * 0.20f)
                    lineTo(w * 0.55f, h * 0.80f)
                    lineTo(w * 0.35f, h * 0.62f)
                    lineTo(w * 0.20f, h * 0.62f)
                    close()
                }
                drawPath(cone, tint, style = stroke)
                // sound waves
                drawArc(tint, -45f, 90f, false, Offset(w * 0.45f, h * 0.32f), Size(w * 0.26f, h * 0.36f), style = stroke)
                drawArc(tint, -45f, 90f, false, Offset(w * 0.52f, h * 0.22f), Size(w * 0.40f, h * 0.56f), style = stroke)
            }

            AppleGlyph.BellSlash -> {
                val bell = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.35f, h * 0.16f, w * 0.30f, h * 0.40f, w * 0.26f, h * 0.65f)
                    lineTo(w * 0.20f, h * 0.72f)
                    lineTo(w * 0.80f, h * 0.72f)
                    lineTo(w * 0.74f, h * 0.65f)
                    cubicTo(w * 0.70f, h * 0.40f, w * 0.65f, h * 0.16f, w * 0.50f, h * 0.16f)
                    close()
                }
                drawPath(bell, tint, style = stroke)
                // Clapper
                drawArc(tint, 0f, 180f, false, Offset(w * 0.42f, h * 0.74f), Size(w * 0.16f, h * 0.10f), style = stroke)
                // Diagonal slash
                drawLine(tint, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), strokeWidth * 1.3f, StrokeCap.Round)
            }

            AppleGlyph.Close -> {
                drawLine(tint, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), strokeWidth * 1.4f, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.28f, h * 0.72f), strokeWidth * 1.4f, StrokeCap.Round)
            }

            AppleGlyph.Search -> {
                // SF Symbols "magnifyingglass"
                val r = w * 0.27f
                drawCircle(
                    color = tint,
                    radius = r,
                    center = Offset(w * 0.44f, h * 0.44f),
                    style = stroke
                )
                drawLine(
                    tint,
                    Offset(w * 0.64f, h * 0.64f),
                    Offset(w * 0.86f, h * 0.86f),
                    strokeWidth,
                    StrokeCap.Round
                )
            }

            AppleGlyph.Globe -> {
                val r = w * 0.36f
                val cx = w * 0.5f
                val cy = h * 0.5f
                drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = stroke)
                // Meridian
                val meridian = Path().apply {
                    moveTo(cx, cy - r)
                    cubicTo(cx - r * 0.62f, cy - r * 0.4f, cx - r * 0.62f, cy + r * 0.4f, cx, cy + r)
                    cubicTo(cx + r * 0.62f, cy + r * 0.4f, cx + r * 0.62f, cy - r * 0.4f, cx, cy - r)
                    close()
                }
                drawPath(meridian, tint, style = stroke)
                // Equator
                drawLine(tint, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth, StrokeCap.Round)
            }

            AppleGlyph.Palette -> {
                val r = w * 0.36f
                drawCircle(color = tint, radius = r, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(color = tint, radius = w * 0.07f, center = Offset(w * 0.38f, h * 0.36f), style = Fill)
                drawCircle(color = tint, radius = w * 0.07f, center = Offset(w * 0.64f, h * 0.44f), style = Fill)
                drawCircle(color = tint, radius = w * 0.07f, center = Offset(w * 0.44f, h * 0.66f), style = Fill)
            }

            AppleGlyph.Heart -> {
                val heart = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.05f, h * 0.55f, w * 0.16f, h * 0.16f, w * 0.5f, h * 0.34f)
                    cubicTo(w * 0.84f, h * 0.16f, w * 0.95f, h * 0.55f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(heart, tint, style = stroke)
            }
        }
    }
}
