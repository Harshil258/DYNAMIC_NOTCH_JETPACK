package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.models.IslandState
import com.notch.dynamicislanddemo.utils.HapticFeedback
import com.notch.dynamicislanddemo.utils.pressAnimation
import kotlinx.coroutines.delay

/**
 * Music Player expanded state with haptic feedback
 */
@Composable
fun MusicPlayerIsland(
    state: IslandState.MusicPlayer,
    onAction: (MusicAction) -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Album art, song info, waveform
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art
                Box(
                    modifier = Modifier
                        .size(72.dp) // Increased size
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFB18CFD), // Violet
                                    Color(0xFFF095FF)  // Pink
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Album art placeholder or image if available
                    Text(text = "🎵", fontSize = 32.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Song info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title and Explicit Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Scrolling text for title
                        Text(
                            text = state.track.title,
                            color = Color.White,
                            fontSize = 19.sp, // Larger title
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .basicMarquee() // Marquee effect
                        )
                        
                        // Explicit Icon (Mock)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "E",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = state.track.artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Waveform
                WaveformAnimation(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Progress bar section
            Column(modifier = Modifier.padding(top = 10.dp)) {
                // Time labels above slider? No, image has them below or inline? 
                // Image: Time labels are on the sides of the slider? NO. 
                // Image: 0:50 [ Slider ] -3:11. So inline.
                // Wait, reviewing image:
                // User said: "progress bar is okay to have current one."
                // The current one has slider then text below.
                // The image has text on sides? No, looking closely at image provided in prompt:
                // "0:50 [------------] -3:11" 
                // It looks like text is on the same line as slider? Or slider is between text.
                // But user saied "progress bar is okay to have current one."
                // BUT "but all other things has to be same".
                // The current implementation (Step 130) has Slider, then Text Row below it.
                // I will keep the current behavior (Vertical stack) but refine it or stick to image if user demands "exact".
                // The image shows: Time Left ..... Bar ..... Time Right.
                // Actually, standard iOS expanded player usually has Bar then Time below.
                // The provided image thumbnail shows: "0:50 [Bar] -3:11". It's inline.
                // However, user said "progress bar is okay to have current one."
                // I will stick to the existing slider behavior (Slider then time below) to be safe, but improve padding.
                

                var sliderPosition by remember(state.track.progress) { mutableStateOf(state.track.progress) }
                var isDragging by remember { mutableStateOf(false) }
                // Local progress state that updates automatically or via drag
                var localProgress by remember(state.track.progress) { mutableStateOf(state.track.progress) }

                // Calculate duration in seconds
                val totalDurationSeconds = remember(state.track) {
                    if (state.track.rawDuration > 0) {
                        state.track.rawDuration / 1000f
                    } else {
                        parseDurationToSeconds(state.track.totalDuration).toFloat()
                    }
                }

                // Auto-update progress
                LaunchedEffect(state.isPlaying, totalDurationSeconds) {
                    if (state.isPlaying && totalDurationSeconds > 0) {
                        val step = 1f / totalDurationSeconds
                        while (true) {
                            delay(1000)
                            localProgress = (localProgress + step).coerceAtMost(1f)
                        }
                    }
                }

                Slider(
                    value = if (isDragging) sliderPosition else localProgress,
                    onValueChange = { 
                        isDragging = true
                        sliderPosition = it
                        // Update local progress visually while dragging too? 
                        // Usually localProgress stays at playback pos, sliderPosition is the thumb.
                        // But if we want time to update while dragging:
                        // No, typically time updates to slider pos while dragging.
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onAction(MusicAction.Seek(sliderPosition))
                        localProgress = sliderPosition // Optimistic update
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp), // Height for touch
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Calculate current time to display
                    val displayProgress = if (isDragging) sliderPosition else localProgress
                    val currentSeconds = (displayProgress * totalDurationSeconds).toLong()
                    val currentTimeStr = formatSecondsToTime(currentSeconds)

                    Text(
                        text = currentTimeStr,
                        color = Color.White.copy(alpha = 0.5f), // Grey
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "-${state.track.totalDuration}", // Remaining time usually negative
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                         fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Playback controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp) // Bottom padding
            ) {
                // Centered Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(40.dp), // Spacing between controls
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = {
                            HapticFeedback.light(context)
                            onAction(MusicAction.Previous)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .pressAnimation()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious, // Need a filled version ideally
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Play/Pause
                    IconButton(
                        onClick = {
                            HapticFeedback.medium(context)
                            onAction(MusicAction.PlayPause)
                        },
                        modifier = Modifier
                            .size(52.dp) // Larger
                            .pressAnimation()
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Next
                    IconButton(
                        onClick = {
                            HapticFeedback.light(context)
                            onAction(MusicAction.Next)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .pressAnimation()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // AirPlay / Output (Far Right)
//                IconButton(
//                    onClick = {
//                        HapticFeedback.light(context)
//                        onAction(MusicAction.AirPlay)
//                    },
//                    modifier = Modifier
//                        .align(Alignment.CenterEnd)
//                        .size(28.dp)
//                        .pressAnimation()
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Cast, // Or a better icon if available
//                        contentDescription = "AirPlay",
//                        tint = Color.White.copy(alpha = 0.8f), // Slightly dimmed
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
            }
        }
    }
}

// Helper functions for time handling
fun parseDurationToSeconds(durationStr: String): Long {
    val parts = durationStr.split(":")
    if (parts.size == 2) {
        val min = parts[0].toLongOrNull() ?: 0
        val sec = parts[1].toLongOrNull() ?: 0
        return min * 60 + sec
    } else if (parts.size == 3) {
        val hour = parts[0].toLongOrNull() ?: 0
        val min = parts[1].toLongOrNull() ?: 0
        val sec = parts[2].toLongOrNull() ?: 0
        return hour * 3600 + min * 60 + sec
    }
    return 0
}

fun formatSecondsToTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
