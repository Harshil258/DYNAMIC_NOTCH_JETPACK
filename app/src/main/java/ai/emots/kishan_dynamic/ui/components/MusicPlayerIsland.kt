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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val albumArtSize = (53.dp * scale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp * scale, bottom = 12.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP ROW: 53dp Album Art (rx=8dp) at X=24dp + Title/Artist + Live Equalizer (rx=35.1dp from right edge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp * scale, end = 35.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 53dp Album Art (rx=8dp matching Dynamic Island-11.svg at X=24dp, Y=32dp)
                    Box(
                        modifier = Modifier
                            .size(albumArtSize)
                            .clip(RoundedCornerShape(8.dp * scale))
                            .background(Color.Black)
                            .border(0.75.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp * scale)),
                        contentAlignment = Alignment.Center
                    ) {
                        UriArtwork(
                            uri = albumArtUri,
                            fallbackRes = R.drawable.music_album_art,
                            contentDescription = "Album art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    // Gap between Album Art right edge (77dp) and Title left edge (98.4dp) = 21.4dp
                    Spacer(modifier = Modifier.width(21.4.dp * scale))

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = (16f * scale).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(6.2.dp * scale))

                            // Apple [E] Explicit Badge (15x15, rx=3.5 matching Dynamic Island-11.svg)
                            Box(
                                modifier = Modifier
                                    .size(15.dp * scale)
                                    .clip(RoundedCornerShape(3.5.dp * scale))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "E",
                                    color = Color.Black,
                                    fontSize = (8f * scale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Gap between Title bottom (54.5dp) and Artist top (63.1dp) = 8.6dp (Spacer 5dp)
                        Spacer(modifier = Modifier.height(5.dp * scale))

                        Text(
                            text = artist,
                            color = Color(0xFF9A9A9A),
                            fontSize = (13.5f * scale).sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                // Reference equalizer (Minimal.svg / Dynamic Island-11.svg: 19.8x22.4dp at X=312..332dp)
                LiveEqualizerMini(
                    width = 20.dp * scale,
                    height = 22.dp * scale,
                    animated = playing
                )
            }

            // MIDDLE ROW: Scrubber Slider with Time (Dynamic Island-11: 240x6.5 bar, rx=3.25, 25dp side padding)
            if (showScrubber) Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp * scale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTime,
                    fontSize = (11f * scale).sp,
                    color = Color(0xFF9A9A9A),
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                // Exact 14.25dp gap from time to track
                Spacer(modifier = Modifier.width(14.dp * scale))

                // Linear Progress Track (6.5dp height with 3.25dp radius)
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
                        .height(18.dp * scale)
                )

                // Exact 14.24dp gap from track to time
                Spacer(modifier = Modifier.width(14.dp * scale))

                Text(
                    text = remainingTime,
                    fontSize = (11f * scale).sp,
                    color = Color(0xFF9A9A9A),
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // BOTTOM ROW: Authentic iOS Transport Trio (Play centered at X=192.3dp with +8.8dp optical offset) + AirPlay Pinned at X=315dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp * scale)
            ) {
                // Centered 3-button playback controls
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 8.8.dp * scale),
                    horizontalArrangement = Arrangement.spacedBy(27.dp * scale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous (size 36dp renders 28.3x16dp vector matching Dynamic Island-11.svg)
                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = 0f
                                onPrevious()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Backward, tint = Color.White, size = 36.dp * scale)
                    }

                    // Play/Pause icon (size 40dp renders 25x28dp vector matching Dynamic Island-11.svg)
                    Box(
                        modifier = Modifier
                            .size(44.dp * scale)
                            .clickable {
                                playing = !playing
                                onPlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (playing) AppleGlyph.Pause else AppleGlyph.Play,
                            tint = Color.White,
                            size = 40.dp * scale
                        )
                    }

                    // Next (size 36dp renders 28.3x16dp vector matching Dynamic Island-11.svg)
                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = (progress + 0.05f).coerceAtMost(1f)
                                onNext()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Forward, tint = Color.White, size = 36.dp * scale)
                    }
                }

                // AirPlay / Route Picker Icon pinned to center at X=315dp (end padding = 32dp * scale, size = 25.5dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                    .padding(end = 32.dp * scale)
                    .size(40.dp * scale)
                        .clickable {
                            isAirPlayConnected = !isAirPlayConnected
                            onAirPlay()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.AirPlay,
                        tint = if (isAirPlayConnected) Color(0xFF37A3DE) else Color.White,
                        size = 25.5.dp * scale
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
