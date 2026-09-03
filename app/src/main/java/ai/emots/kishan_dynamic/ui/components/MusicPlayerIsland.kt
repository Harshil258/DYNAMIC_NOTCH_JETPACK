package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerIsland(
    title: String = "Heat Waves",
    artist: String = "Glass Animals",
    isPlaying: Boolean = true,
    currentTime: String = "0:50",
    remainingTime: String = "-3:11",
    initialProgress: Float = 0.21f,
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onSeek: (Float) -> Unit = {}
) {
    var progress by remember { mutableFloatStateOf(initialProgress) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(1000)
                if (!isDragging) {
                    progress = (progress + 0.004f).coerceAtMost(1f)
                }
            }
        }
    }

    // Figma Expanded-1.svg / Expanded.svg shows:
    // Album art: 52dp squircle (rx=13dp)
    // Title: 17sp Bold, Artist: 13sp Gray
    // Equalizer: Pink/Magenta waveform
    // Scrubber: Time | Progress | Time
    // Controls: ◀◀ ▶ ▶▶ AirPlay - solid white icons on black
    // Compact: album art + track title + waveform

    val hPad = 16.dp
    val vPad = 14.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = hPad, vertical = vPad),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP ROW: Album Art Squircle + Title & Artist + Pink Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 52dp Album Art Squircle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE879F9), Color(0xFF818CF8), Color(0xFF38BDF8))
                                )
                            )
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Music, tint = Color.White, size = 20.dp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(7.dp))

                            // Apple [E] Explicit Badge
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(Color(0xFF8E8E93)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "E",
                                    color = Color(0xFF000000),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = artist,
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Neon Pink/Magenta Warm-up Waveform
                LiveEqualizerMini(color = Color(0xFFFA2D48))
            }

            // MIDDLE ROW: Scrubber Slider with Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = currentTime,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                // Linear Progress Track
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.32f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Text(
                    text = remainingTime,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // BOTTOM ROW: Media Controls (Backward, Play Pause, Forward, AirPlay)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.clickable(onClick = onPrevious)
                ) {
                    AppleIcon(glyph = AppleGlyph.Backward, tint = Color.White, size = 26.dp)
                }

                // Play/Pause icon
                Box(
                    modifier = Modifier.clickable(onClick = onPlayPause)
                ) {
                    AppleIcon(
                        glyph = if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play,
                        tint = Color.White,
                        size = 32.dp
                    )
                }

                Box(
                    modifier = Modifier.clickable(onClick = onNext)
                ) {
                    AppleIcon(glyph = AppleGlyph.Forward, tint = Color.White, size = 26.dp)
                }

                AppleIcon(glyph = AppleGlyph.AirPlay, tint = Color.White, size = 24.dp)
            }
    }
}

/**
 * Pixel-perfect iOS audio scrubber with thin 3.5dp track line and 8dp circle thumb
 */
@Composable
fun AppleAudioScrubber(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    },
                    onDragEnd = { onProgressChangeFinished() },
                    onDragCancel = { onProgressChangeFinished() }
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val trackHeight = 3.5.dp.toPx()
        val cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)

        // 1. Inactive background track (semi-transparent white)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(0f, centerY - trackHeight / 2f),
            size = Size(w, trackHeight),
            cornerRadius = cornerRadius
        )

        // 2. Active played track (solid white)
        val activeWidth = (w * progress).coerceIn(0f, w)
        if (activeWidth > 0f) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(activeWidth, trackHeight),
                cornerRadius = cornerRadius
            )
        }

        // 3. Crisp Apple circle thumb (8dp)
        val thumbRadius = 4.dp.toPx()
        drawCircle(
            color = Color.White,
            radius = thumbRadius,
            center = Offset(activeWidth, centerY)
        )
    }
}

