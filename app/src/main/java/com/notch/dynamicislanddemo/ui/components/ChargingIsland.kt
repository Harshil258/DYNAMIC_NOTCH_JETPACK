package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.models.IslandState
import kotlinx.coroutines.delay

/**
 * Charging/Battery island for split layout (status + percentage in main, badge in side)
 */
@Composable
fun ChargingIslandMain(state: IslandState.Charging) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }
    
    // Color logic
    val batteryColor = when {
        state.percentage >= 20 -> Color(0xFF34C759) // Green
        state.percentage >= 10 -> Color(0xFFFF9500) // Orange
        else -> Color(0xFFFF3B30) // Red
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status text
            Text(
                text = if (state.isLowBattery) "Low Battery" else "Charging",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Percentage text
                Text(
                    text = "${state.percentage}%",
                    color = batteryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Battery icon (Horizontal)
                // Rotating BatteryFull logic
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.BatteryFull,
                        contentDescription = null,
                        tint = batteryColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(90f)
                    )
                    // If charging, show bolt? Standard Batt icon doesn't have bolt when Full.
                    // For simplicity, using tint. If detailed needed, we need a custom drawable.
                    // The user wants "exact", so let's stick to the battery shape which is key.
                }
            }
        }
    }
}

@Composable
fun ChargingIslandSide(state: IslandState.Charging) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }
    
    // Color logic
    val ringColor = when {
        state.percentage >= 20 -> Color(0xFF34C759)
        state.percentage >= 10 -> Color(0xFFFF9500)
        else -> Color(0xFFFF3B30)
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
            // Progress Ring
            CircularProgressIndicator(
                progress = { state.percentage / 100f },
                modifier = Modifier.size(37.dp).padding(2.dp),
                color = ringColor,
                trackColor = ringColor.copy(alpha = 0.3f),
                strokeWidth = 3.dp,
            )
            
            Text(
                text = "${state.percentage}",
                color = ringColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
