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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.ui.layout.ContentScale
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.ui.theme.IslandColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerIsland(
    title: String = "Heat Waves",
    artist: String = "Grass Animals",
    albumArtUri: String? = null,
    isPlaying: Boolean = true,
    currentTime: String = "0:50",
    remainingTime: String = "-3:11",
    initialProgress: Float = 0.34f,
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onAirPlay: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    showScrubber: Boolean = true
) {
    var progress by remember { mutableFloatStateOf(initialProgress) }
    var isDragging by remember { mutableStateOf(false) }
    var playing by remember(isPlaying) { mutableStateOf(isPlaying) }
    var isAirPlayConnected by remember { mutableStateOf(false) }

    LaunchedEffect(initialProgress) {
        if (!isDragging) {
            progress = initialProgress.coerceIn(0f, 1f)
        }
    }

    // Figma Expanded-1.svg / Expanded.svg shows:
    // Album art: 52dp squircle (rx=13dp)
    // Title: 17sp Bold, Artist: 13sp Gray
    // Equalizer: Pink/Magenta waveform
    // Scrubber: Time | Progress | Time
    // Controls: ◀◀ ▶ ▶▶ AirPlay - solid white icons on black
    // Compact: album art + track title + waveform

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val albumArtSize = 48.dp * scale

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp * scale, vertical = 12.dp * scale),
            verticalArrangement = Arrangement.spacedBy(10.dp * scale)
        ) {
            // ROW 1: Album Art (48dp squircle) + Title & Artist Column + Live Equalizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 48dp Album Art squircle
                Box(
                    modifier = Modifier
                        .size(albumArtSize)
                        .clip(RoundedCornerShape(10.dp * scale))
                        .background(Color(0xFF1C1C1E))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp * scale)),
                    contentAlignment = Alignment.Center
                ) {
                    UriArtwork(
                        uri = albumArtUri,
                        fallbackRes = R.drawable.music_album_art,
                        contentDescription = "Album art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title.ifBlank { "Now playing" },
                            color = Color.White,
                            fontSize = (15f * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(5.dp * scale))

                        // Apple [E] Explicit Badge
                        Box(
                            modifier = Modifier
                                .size(13.dp * scale)
                                .clip(RoundedCornerShape(3.dp * scale))
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "E",
                                color = Color.Black,
                                fontSize = (7.5f * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp * scale))

                    Text(
                        text = artist.ifBlank { "Unknown Artist" },
                        color = Color(0xFF9A9A9A),
                        fontSize = (12.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp * scale))

                // Equalizer on right
                LiveEqualizerMini(
                    width = 18.dp * scale,
                    height = 20.dp * scale,
                    animated = playing
                )
            }

            // ROW 2: Scrubber timeline: currentTime | Progress Bar | remainingTime
            if (showScrubber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTime,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(10.dp * scale))

                    AppleAudioScrubber(
                        progress = progress,
                        onProgressChange = { value ->
                            isDragging = true
                            progress = value
                            onSeek(value)
                        },
                        onProgressChangeFinished = {
                            isDragging = false
                            onSeek(progress)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp * scale)
                    )

                    Spacer(modifier = Modifier.width(10.dp * scale))

                    Text(
                        text = remainingTime,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ROW 3: Transport controls directly on black OLED surface
            // Clean, balanced vector glyphs without bloated grey circle containers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp * scale),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind (44dp touch area, 22dp glyph)
                Box(
                    modifier = Modifier
                        .size((44.dp * scale).coerceAtLeast(44.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { progress = 0f; onPrevious() }
                        )
                        .semantics { contentDescription = "Previous track" },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Backward,
                        tint = Color.White,
                        size = 22.dp * scale
                    )
                }

                // Play / Pause (Centerpiece: Solid white glyph directly on black OLED surface, matching Group 3.png & Expanded.svg)
                Box(
                    modifier = Modifier
                        .size((48.dp * scale).coerceAtLeast(48.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { playing = !playing; onPlayPause() }
                        )
                        .semantics { contentDescription = if (playing) "Pause playback" else "Play playback" },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (playing) AppleGlyph.Pause else AppleGlyph.Play,
                        tint = Color.White,
                        size = 28.dp * scale
                    )
                }

                // Forward (44dp touch area, 22dp glyph)
                Box(
                    modifier = Modifier
                        .size((44.dp * scale).coerceAtLeast(44.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { progress = (progress + 0.05f).coerceAtMost(1f); onNext() }
                        )
                        .semantics { contentDescription = "Next track" },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Forward,
                        tint = Color.White,
                        size = 22.dp * scale
                    )
                }

                // AirPlay / Route (44dp touch area, 19dp glyph)
                Box(
                    modifier = Modifier
                        .size((44.dp * scale).coerceAtLeast(44.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isAirPlayConnected = !isAirPlayConnected; onAirPlay() }
                        )
                        .semantics { contentDescription = "Audio output" },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.AirPlay,
                        tint = if (isAirPlayConnected) IslandColors.CapsuleCyan else Color(0xFF8E8E93),
                        size = 19.dp * scale
                    )
                }
            }
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
    activeColor: Color = Color(0xFFFF2D55),
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
            .semantics {
                contentDescription = "Playback position"
                progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
                setProgress { value ->
                    onProgressChange(value.coerceIn(0f, 1f))
                    onProgressChangeFinished()
                    true
                }
            }
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

        // 2. Active played track (vibrant magenta/pink matching Group 3.png)
        val activeWidth = (w * progress).coerceIn(0f, w)
        if (activeWidth > 0f) {
            drawRoundRect(
                color = activeColor,
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
