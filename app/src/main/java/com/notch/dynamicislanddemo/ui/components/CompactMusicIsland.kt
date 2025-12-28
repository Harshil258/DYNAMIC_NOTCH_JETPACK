package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notch.dynamicislanddemo.models.IslandState
import kotlinx.coroutines.delay

/**
 * Compact music island for split layout (main + side bubble)
 */
@Composable
fun CompactMusicIslandMain(state: IslandState.CompactMusic) {
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
                    .clip(RoundedCornerShape(8.dp)) // iOS style rounded corners for album art
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                // Image 1/New Image shows the album art here
                // For demo, we use a gradient background with icon
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
    }
}

@Composable
fun CompactMusicIslandSide(state: IslandState.CompactMusic) {
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
            // Using a static icon like the image showing a waveform-like circle
            // But detailed implementation suggests the side view is the activity indicator.
            // Image 2 shows a circular island with a pink waveform inside.
            WaveformAnimation(
                modifier = Modifier.size(24.dp, 16.dp),
                gradient = listOf(Color(0xFFFF0080), Color(0xFFFF4DB8))
            )
        }
    }
}
