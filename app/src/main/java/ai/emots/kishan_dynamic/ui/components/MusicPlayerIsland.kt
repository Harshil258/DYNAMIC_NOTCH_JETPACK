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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
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

    val isCompact = ai.emots.kishan_dynamic.ui.theme.AppTheme.windowSize == ai.emots.kishan_dynamic.ui.theme.AppWindowSize.Compact
    val albumArtSize = if (isCompact) 64.dp else 72.dp
    val controlSpacing = if (isCompact) 28.dp else 36.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.xl,
                vertical = ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.lg
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Album Art, Song Info, Waveform
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art squircle
            Box(
                modifier = Modifier
                    .size(albumArtSize)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFB18CFD),
                                Color(0xFFF095FF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Music,
                    tint = Color.White,
                    size = if (isCompact) 28.dp else 32.dp
                )
            }

            Spacer(modifier = Modifier.width(if (isCompact) 12.dp else 16.dp))

            // Track metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        text = title,
                        style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandTitle,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .basicMarquee()
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = "E",
                            style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandBadge,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                AppText(
                    text = artist,
                    style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandSubtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Waveform
            if (isPlaying) {
                WaveformAnimation(
                    modifier = Modifier
                        .size(if (isCompact) 28.dp else 32.dp, 24.dp)
                        .padding(top = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
        }

        // Inline scrubber row: Time - Scrubber - Time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = currentTime,
                style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandTime,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.width(36.dp)
            )

            AppleAudioScrubber(
                progress = progress,
                onProgressChange = {
                    isDragging = true
                    progress = it
                },
                onProgressChangeFinished = {
                    isDragging = false
                    onSeek(progress)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height(24.dp)
            )

            AppText(
                text = remainingTime,
                style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandTime,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.width(42.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        // Playback controls (Rewind, Play/Pause, Forward, AirPlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(controlSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(28.dp)
                ) {
                    RewindIcon(color = Color.White, modifier = Modifier.fillMaxSize())
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isPlaying) {
                        PauseIcon(color = Color.White, modifier = Modifier.fillMaxSize())
                    } else {
                        PlayIcon(color = Color.White, modifier = Modifier.fillMaxSize())
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(28.dp)
                ) {
                    FastForwardIcon(color = Color.White, modifier = Modifier.fillMaxSize())
                }
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
            ) {
                AirPlayIcon(color = Color.White, modifier = Modifier.fillMaxSize())
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

