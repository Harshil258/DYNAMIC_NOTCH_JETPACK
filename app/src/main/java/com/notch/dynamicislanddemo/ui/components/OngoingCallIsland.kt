package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.models.IslandState
import kotlinx.coroutines.delay

/**
 * Ongoing call island for split layout (timer + waveform in main, phone icon in side)
 */
@Composable
fun OngoingCallIslandMain(state: IslandState.OngoingCall) {
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
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Phone icon - Green, no circle background
                Icon(
                    imageVector = Icons.Filled.Phone, // Handset icon
                    contentDescription = null,
                    tint = Color(0xFF34C759),
                    modifier = Modifier.size(16.dp)
                )
                
                // Timer text - Green
                Text(
                    text = state.duration,
                    color = Color(0xFF34C759),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Waveform animation - Multicolor (Green/Orange)
            // Note: We need to update WaveformAnimation to support custom colors or just pass tint
            // For now passing modifier, assumed Waveform handles color. 
            // Since Waveform is shared, we'll need to check if we can tint it.
            // The current Waveform implementation uses fixed colors.
            // We will need to update WaveformAnimation later or modifying it now.
            // Let's assume we update Waveform to take color parameters or we overlay.
            // Ideally we'd pass colors to WaveformAnimation.
            // Since I can't change Waveform signature in THIS tool call effectively without viewing it strictly again,
            // I'll stick to the layout change and update colors if the param exists (it didn't before).
            // Actually, I removed the params because they didn't exist.
            // I should use a Box with a masked gradient if possible, or just accept the pink for a moment?
            // No, user said "exact". I MUST update WaveformAnimation too.
            // I'll add the layout first.
            // Waveform animation - Multicolor (Green/Orange)
            WaveformAnimation(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp),
                gradient = listOf(Color(0xFF34C759), Color(0xFFFF9500))
            )
        }
    }
}

@Composable
fun OngoingCallIslandSide(state: IslandState.OngoingCall) {
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
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                tint = Color(0xFF34C759),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
