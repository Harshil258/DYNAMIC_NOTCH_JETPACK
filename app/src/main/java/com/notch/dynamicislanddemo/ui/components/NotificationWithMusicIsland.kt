package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.models.IslandState
import kotlinx.coroutines.delay


/**
 * Notification with music island for split layout
 * Shows notification in main island, music waveform in side circular island
 */
@Composable
fun NotificationWithMusicIslandMain(state: IslandState.NotificationWithMusic) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (state.isPlaying) {
            // PLAYING: Show Music Player (Album Art + Waveform)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album art (Left)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF7B61FF), Color(0xFF00D1FF))
                            )
                        )
                    )
                    
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Waveform animation (Right)
                WaveformAnimation(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp),
                    gradient = listOf(Color(0xFFFF0080), Color(0xFFFF4DB8))
                )
            }
        } else {
            // PAUSED: Show Notification Content (Icon + Title)
            // Show the first notification's app icon and title
            val notification = state.notifications.firstOrNull()
            if (notification != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // App Icon
                     Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF5E5CE6), Color(0xFF7D7AFF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notification.appName.take(1),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Title / Message
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationWithMusicIslandSide(state: IslandState.NotificationWithMusic) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isPlaying) {
                // PLAYING: Show Notification count badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4A4A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.notifications.size.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // PAUSED: Show Album Art (Circle)
                Box(
                    modifier = Modifier
                        .size(37.dp) // Full size of the bubble
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                     // Placeholder Gradient for Album Art
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF7B61FF), Color(0xFF00D1FF))
                            )
                        )
                    )
                    
//                    // Center Icon (Note if available)
//                    Icon(
//                        imageVector = androidx.compose.material.icons.filled.MusicNote,
//                        contentDescription = null,
//                        tint = Color.White.copy(alpha = 0.7f),
//                        modifier = Modifier.size(16.dp)
//                    )
                }
            }
        }
    }
}
