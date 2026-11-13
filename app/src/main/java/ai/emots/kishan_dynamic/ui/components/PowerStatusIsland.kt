package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.motion.AppMotion
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts

/**
 * High-craft Apple MagSafe / Power HUD for Expanded Charging & Low Battery states.
 * Symmetrical, balanced, professional layout with zero empty black void:
 * - Leading: 46dp frosted squircle with glowing Apple glyph
 * - Center: Title + live breathing status dot + description
 * - Trailing: 48dp circular HUD progress ring with bold percentage inside
 */
@Composable
internal fun PowerStatusExpanded(percentage: Int, low: Boolean) {
    val value = percentage.coerceIn(0, 100)
    val tint = if (low) Color(0xFFFF453A) else Color(0xFF30D158)
    val fontScale = LocalDensity.current.fontScale

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        val stacked = fontScale > 1.35f || maxWidth < 260.dp

        if (stacked) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(tint.copy(alpha = 0.25f), tint.copy(alpha = 0.10f))
                                )
                            )
                            .border(0.75.dp, tint.copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (low) AppleGlyph.Battery else AppleGlyph.Charging,
                            tint = tint,
                            size = 18.dp
                        )
                    }
                    Column {
                        Text(
                            text = if (low) "Low Battery" else "Charging",
                            color = Color.White,
                            style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        )
                        Text(
                            text = if (low) "Connect to power" else "Fast charging",
                            color = Color.White.copy(alpha = 0.70f),
                            style = AppTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                        )
                    }
                }
                Text(
                    text = "$value%",
                    color = tint,
                    style = AppTheme.typography.islandTime.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Leading identity with glowing frosted squircle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(tint.copy(alpha = 0.24f), tint.copy(alpha = 0.08f))
                                )
                            )
                            .border(0.75.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (low) AppleGlyph.Battery else AppleGlyph.Charging,
                            tint = tint,
                            size = 22.dp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = if (low) "Low Battery" else "Charging",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = IslandReferenceFonts.Text,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dotAlpha = rememberBreathing(min = 0.45f, max = 1f, durationMillis = 1600)
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(tint.copy(alpha = if (low) 1f else dotAlpha.value))
                            )
                            Text(
                                text = if (low) "Connect to power" else "Fast charging",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontFamily = IslandReferenceFonts.Text,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Trailing circular MagSafe HUD progress ring
                val sweepProgress by animateFloatAsState(
                    targetValue = (value / 100f).coerceIn(0f, 1f),
                    animationSpec = AppMotion.settleSpring(),
                    label = "power_status_ring"
                )
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(46.dp)) {
                        val strokeWidth = 3.5.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                        // Outer track
                        drawArc(
                            color = Color.White.copy(alpha = 0.12f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress arc
                        if (sweepProgress > 0f) {
                            drawArc(
                                color = tint,
                                startAngle = -90f,
                                sweepAngle = 360f * sweepProgress,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Text(
                        text = "$value%",
                        color = if (low) tint else Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IslandReferenceFonts.Text
                    )
                }
            }
        }
    }
}
