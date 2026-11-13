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
import ai.emots.kishan_dynamic.ui.motion.pressScale
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
    val playing = isPlaying
    // Output-route metadata is unavailable here; tapping Settings is not a connection event.

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
        val compactControlRow = maxWidth < ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaTransportSafeWidth
        val mediaHorizontalInset = if (compactControlRow) ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.lg else ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaAlbumArtLeftInset
        val transportLeadingInset = (maxWidth - ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaTransportSafeWidth).coerceIn(0.dp, ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaTransportLeadingInset)
        val albumArtSize = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaAlbumArtSize

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = mediaHorizontalInset, end = mediaHorizontalInset, top = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaTopInset, bottom = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaBottomInset),
            verticalArrangement = Arrangement.spacedBy(ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaRegionGap)
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
                        .clip(RoundedCornerShape(ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaAlbumArtCorner))
                        .background(Color(0xFF1C1C1E))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaAlbumArtCorner)),
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

                Spacer(modifier = Modifier.width(ai.emots.kishan_dynamic.ui.theme.AppTheme.island.mediaTitleGap))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title.ifBlank { "Now playing" },
                            color = Color.White,
                            fontSize = 16.sp, lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )

                        // Explicit metadata is not supplied by this source model; no fabricated badge.

                    }

                    Spacer(modifier = Modifier.height(2.dp * scale))

                    Text(
                        text = artist.ifBlank { "Unknown Artist" },
                        color = Color(0xFF9A9A9A),
                        fontSize = 14.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp * scale))

                // Equalizer on right
                LiveEqualizerMini(
                    width = 20.dp,
                    height = 23.dp,
                    animated = playing
                )
            }

            // ROW 2: Scrubber timeline: currentTime | Progress Bar | remainingTime
            if (showScrubber) {
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Normal,
                        lineHeight = 18.sp
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
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Normal,
                        lineHeight = 18.sp
                    )
                }
                }
            }

            // ROW 3: Transport controls directly on black OLED surface
            // Clean, balanced vector glyphs without bloated grey circle containers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = transportLeadingInset),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind (44dp touch area, 22dp glyph)
                if (canRenderIslandControl(IslandControlAction.MusicPrevious)) {
                    val prevInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .pressScale(prevInteraction, pressedScale = 0.94f)
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                interactionSource = prevInteraction,
                                indication = null,
                                onClick = { progress = 0f; onPrevious() }
                            )
                            .semantics { contentDescription = "Previous track" },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Backward,
                            tint = Color.White,
                            size = 29.dp
                        )
                    }
                }

                // Play / Pause (Centerpiece: Solid white glyph directly on black OLED surface, matching Group 3.png & Expanded.svg)
                if (canRenderIslandControl(IslandControlAction.MusicPlayPause)) {
                    val playInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size((48.dp * scale).coerceAtLeast(48.dp))
                            .pressScale(playInteraction, pressedScale = 0.94f)
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                interactionSource = playInteraction,
                                indication = null,
                                onClick = onPlayPause
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
                }

                // Forward (44dp touch area, 22dp glyph)
                if (canRenderIslandControl(IslandControlAction.MusicNext)) {
                    val nextInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .pressScale(nextInteraction, pressedScale = 0.94f)
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                interactionSource = nextInteraction,
                                indication = null,
                                onClick = { progress = (progress + 0.05f).coerceAtMost(1f); onNext() }
                            )
                            .semantics { contentDescription = "Next track" },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Forward,
                            tint = Color.White,
                            size = 29.dp
                        )
                    }
                }

                // AirPlay / Route (44dp touch area, 19dp glyph)
                if (!compactControlRow && canRenderIslandControl(IslandControlAction.MusicAirPlay)) {
                    val airPlayInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .pressScale(airPlayInteraction, pressedScale = 0.94f)
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                interactionSource = airPlayInteraction,
                                indication = null,
                                onClick = onAirPlay
                            )
                            .semantics { contentDescription = "Audio output" },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.AirPlay,
                            tint = Color.White,
                            size = 21.dp
                        )
                    }
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
    activeColor: Color = Color.White.copy(alpha = .8f),
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
        val trackHeight = 6.5.dp.toPx()
        val cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)

        // 1. Inactive background track (semi-transparent white)
        drawRoundRect(
            color = Color(0xFF3F3F3F).copy(alpha = .7f),
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

        // Figma uses a flat played segment with no extra circular thumb.

    }
}
