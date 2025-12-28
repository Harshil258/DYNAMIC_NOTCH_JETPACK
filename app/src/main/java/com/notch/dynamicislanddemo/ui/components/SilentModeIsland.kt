package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Silent Mode island with haptic feedback
 */
@Composable
fun SilentModeIsland(
    state: IslandState.SilentMode,
    onToggle: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeOff,
                    contentDescription = "Silent Mode",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = "SilentMode",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (state.isOn) "On" else "Off",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Button(
                onClick = {
                    HapticFeedback.medium(context)
                    onToggle()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF48484A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                modifier = Modifier.pressAnimation()
            ) {
                Text(
                    text = if (state.isOn) "Unmute" else "Mute",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
