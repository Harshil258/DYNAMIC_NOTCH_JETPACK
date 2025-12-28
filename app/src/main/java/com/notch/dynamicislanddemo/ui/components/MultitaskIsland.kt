package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.notch.dynamicislanddemo.models.ActivityType
import kotlinx.coroutines.delay

/**
 * Multitask island showing two concurrent activities
 */
@Composable
fun MultitaskIslandMain(activity: com.notch.dynamicislanddemo.models.ActivityInfo) {
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
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (activity.type) {
                            ActivityType.MUSIC -> Brush.linearGradient(
                                colors = listOf(Color(0xFFFF0080), Color(0xFFFF4DB8))
                            )
                            ActivityType.TIMER -> Brush.linearGradient(
                                colors = listOf(Color(0xFFFF9500), Color(0xFFFFBF00))
                            )
                            else -> Brush.linearGradient(
                                colors = listOf(Color(0xFF5E5CE6), Color(0xFF7D7AFF))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.type) {
                        ActivityType.MUSIC -> Icons.Filled.MusicNote
                        ActivityType.TIMER -> Icons.Filled.Timer
                        else -> Icons.Filled.MusicNote
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MultitaskIslandSide(activity: com.notch.dynamicislanddemo.models.ActivityInfo) {
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
                imageVector = when (activity.type) {
                    ActivityType.MUSIC -> Icons.Filled.MusicNote
                    ActivityType.TIMER -> Icons.Filled.Timer
                    else -> Icons.Filled.MusicNote
                },
                contentDescription = null,
                tint = Color(0xFFFF9500),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
