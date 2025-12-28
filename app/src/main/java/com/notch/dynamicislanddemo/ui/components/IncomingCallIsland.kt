package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kyant.capsule.ContinuousCapsule
import com.notch.dynamicislanddemo.models.IslandState
import com.notch.dynamicislanddemo.utils.HapticFeedback
import com.notch.dynamicislanddemo.utils.pressAnimation
import kotlinx.coroutines.delay

/**
 * Incoming Call island with horizontal layout and haptic feedback
 */
@Composable
fun IncomingCallIsland(
    state: IslandState.IncomingCall,
    onAction: (CallAction) -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Contact photo and info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(ContinuousCapsule)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFFF8C42)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.caller.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.caller.label,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = state.caller.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
            
            // Action buttons with haptics
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = {
                        HapticFeedback.heavy(context)
                        onAction(CallAction.Decline)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .pressAnimation()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                IconButton(
                    onClick = {
                        HapticFeedback.success(context)
                        onAction(CallAction.Accept)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759))
                        .pressAnimation()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
