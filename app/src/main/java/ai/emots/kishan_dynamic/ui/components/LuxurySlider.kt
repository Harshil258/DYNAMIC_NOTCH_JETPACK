package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens
import kotlin.math.roundToInt

/**
 * Luxury gradient slider matching high-end iOS / Dynamic Island controls.
 */
@Composable
fun LuxurySlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedFraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AuroraTokens.TextColor.primary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF281E44))
                    .border(1.dp, Color(0x30A78BFA), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = valueFormatter(value),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuroraTokens.TextColor.accent
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current
            val thumbRadiusPx = with(density) { 11.dp.toPx() }
            val availableWidth = (widthPx - thumbRadiusPx * 2).coerceAtLeast(1f)

            // Inactive track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF1F1A2F))
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(100.dp))
            )

            // Active track
            val activeTrackWidthDp = with(density) { (normalizedFraction * availableWidth + thumbRadiusPx).toDp() }
            Box(
                modifier = Modifier
                    .width(activeTrackWidthDp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AuroraTokens.Palette.primary, AuroraTokens.Palette.primaryLight)
                        )
                    )
            )

            // Thumb
            val thumbOffsetPx = normalizedFraction * availableWidth
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = thumbOffsetPx.roundToInt(), y = 0) }
                    .size(24.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.6f),
                        spotColor = AuroraTokens.Palette.primary.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, AuroraTokens.Palette.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AuroraTokens.Palette.primary)
                )
            }

            // Gesture detector over the whole bar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newFraction = ((offset.x - thumbRadiusPx) / availableWidth).coerceIn(0f, 1f)
                            val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newFraction = ((change.position.x - thumbRadiusPx) / availableWidth).coerceIn(0f, 1f)
                            val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
            )
        }
    }
}
