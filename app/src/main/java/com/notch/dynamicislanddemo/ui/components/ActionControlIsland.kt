package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.R
import com.notch.dynamicislanddemo.models.ControlTile
import com.notch.dynamicislanddemo.models.IslandState
import com.notch.dynamicislanddemo.models.SystemToggleType
import com.notch.dynamicislanddemo.utils.HapticFeedback
import com.notch.dynamicislanddemo.utils.pressAnimation
import kotlinx.coroutines.delay

/**
 * Action Control Center - iOS-style quick controls
 */
@Composable
fun ActionControlIsland(
    state: IslandState.ActionControl,
    onToggle: (SystemToggleType) -> Unit,
    onAppLaunch: (String) -> Unit,
    onContactAction: (String) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLockAction: () -> Unit,
    onCameraAction: () -> Unit,
    onSettingsAction: () -> Unit,
    notificationCount: Int = 0,
    onNotificationsAction: () -> Unit = {}
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C1C1E),
                            Color(0xFF2C2C2E)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top bar: Lock, Edit, Camera, Settings icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TopBarIcon(Icons.Filled.Lock, context) { onLockAction() }
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Notification Button (New)
                    Box(modifier = Modifier.pressAnimation()) {
                        TopBarIcon(Icons.Filled.Notifications, context) { onNotificationsAction() }
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(14.dp)
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = notificationCount.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    TopBarIcon(Icons.Filled.Edit, context) { }
                    TopBarIcon(Icons.Filled.CameraAlt, context) { onCameraAction() }
                    TopBarIcon(Icons.Filled.Settings, context) { onSettingsAction() }
                }
                
                // System toggles grid (first 5 tiles)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.config.tiles.take(5).forEach { tile ->
                        when (tile) {
                            is ControlTile.SystemToggle -> {
                                SystemToggleTile(
                                    tile = tile,
                                    onClick = {
                                        HapticFeedback.medium(context)
                                        onToggle(tile.type)
                                    },
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }
                
                // App shortcuts and extra controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp), // Align with toggles
                    horizontalArrangement = Arrangement.spacedBy(15.dp) // Adjust spacing to match
                ) {
                    state.config.tiles.drop(5).forEach { tile ->
                        when (tile) {
                            is ControlTile.AppShortcut -> {
                                AppShortcutTile(
                                    tile = tile,
                                    onClick = {
                                        HapticFeedback.light(context)
                                        onAppLaunch(tile.packageName)
                                    },
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            is ControlTile.ContactShortcut -> {
                                ContactTile(
                                    tile = tile,
                                    onClick = {
                                        HapticFeedback.light(context)
                                        onContactAction(tile.contactId)
                                    },
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                    
                    // Empty tile for customization
                    EmptyTile(
                        onClick = {
                            HapticFeedback.light(context)
                        },
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f)) // Push slider to bottom

                // Volume/Brightness sliders
                SliderControl(
                    icon = Icons.Filled.VolumeUp,
                    value = state.config.volumeLevel,
                    onValueChange = {
                        HapticFeedback.selection(context)
                        onVolumeChange(it)
                    }
                )
            }
        }
    }
}

@Composable
fun TopBarIcon(icon: ImageVector, context: android.content.Context, onClick: () -> Unit) {
    IconButton(
        onClick = { 
            HapticFeedback.light(context)
            onClick()
        },
        modifier = Modifier
            .size(32.dp)
            .pressAnimation()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SystemToggleTile(
    tile: ControlTile.SystemToggle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    if (tile.isEnabled) 
                        Color(0xFF007AFF) // iOS Blue
                    else 
                        Color(0xFF3A3A3C) // iOS Dark Gray for off state
                )
                .pressAnimation(onTap = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = when (tile.type) {
                        SystemToggleType.MOBILE_DATA -> com.notch.dynamicislanddemo.R.drawable.ic_mobile_data
                        SystemToggleType.WIFI -> R.drawable.ic_wifi
                        SystemToggleType.BLUETOOTH -> R.drawable.ic_bluetooth
                        SystemToggleType.DARK_MODE -> R.drawable.ic_dark_mode
                        SystemToggleType.TORCH -> R.drawable.ic_torch
                        else -> android.R.drawable.ic_menu_help
                    }
                ),
                contentDescription = tile.label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = tile.label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun AppShortcutTile(
    tile: ControlTile.AppShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape) // Changed to Circle to match toggles
            .background(tile.iconColor)
            .pressAnimation(onTap = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.appName.take(1).uppercase(),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContactTile(
    tile: ControlTile.ContactShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5E5CE6),
                        Color(0xFF7D7AFF)
                    )
                )
            )
            .pressAnimation(onTap = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.contactName.take(1).uppercase(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape) // Changed to Circle
            .background(Color(0xFF3A3A3C)) // Match off-state color
            .pressAnimation(onTap = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Edit, // Changed to Edit pencil to match design
            contentDescription = "Edit",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderControl(
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    // Custom Slider implementation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color(0xFF3A3A3C)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background (filled portion)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(value)
                .background(Color.White)
        )
        
        // Icons overlay
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (value > 0.1f) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            
            // Second icon only visible if full or near full, or just keep one icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (value > 0.9f) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(26.dp)
            )
        }
        
        // Invisible Slider for touch handling
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0f), // Invisible but interactive
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
