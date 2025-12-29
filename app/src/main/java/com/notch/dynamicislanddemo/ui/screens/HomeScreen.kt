package com.notch.dynamicislanddemo.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.data.PreferencesDataStore
import com.notch.dynamicislanddemo.services.DynamicIslandAccessibilityService
import com.notch.dynamicislanddemo.ui.components.NavigationDestination
import com.notch.dynamicislanddemo.ui.glass.*
import com.notch.dynamicislanddemo.utils.HapticFeedback
import com.notch.dynamicislanddemo.utils.Utils
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    backdrop: Backdrop,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val preferencesDataStore = remember { PreferencesDataStore(context) }
    
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    val secondaryTextColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.7f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    // Permission states
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(false) }
    
    // Check permissions on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = Utils.isAccessibilityServiceEnabled(
                    context, 
                    DynamicIslandAccessibilityService::class.java
                )
                isNotificationEnabled = Utils.isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Collect island enabled state
    val islandEnabled by preferencesDataStore.islandEnabled.collectAsState(initial = true)
    val setupDone by preferencesDataStore.setupDone.collectAsState(initial = false)
    
    val allPermissionsGranted = isAccessibilityEnabled && isNotificationEnabled
    val isMainSwitchInteractable = allPermissionsGranted && setupDone
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicText(
                text = "Dynamic Island",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            BasicText(
                text = "Experience iPhone's Dynamic Island on Android",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Island Toggle Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BasicText(
                    text = "Dynamic Island",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                val subtitle = when {
                    !allPermissionsGranted -> "Permissions required"
                    !setupDone -> "Complete setup below"
                    islandEnabled -> "Island is visible"
                    else -> "Island is hidden"
                }
                
                GlassSettingToggleItem(
                    title = "Show Dynamic Island",
                    subtitle = subtitle,
                    isChecked = islandEnabled,
                    onCheckedChange = { enabled ->
                        if (isMainSwitchInteractable) {
                            HapticFeedback.medium(context)
                            scope.launch {
                                preferencesDataStore.setIslandEnabled(enabled)
                            }
                        } else {
                            HapticFeedback.error(context)
                        }
                    },
                    backdrop = backdrop,
                    enabled = isMainSwitchInteractable
                )
            }
        }
        
        // Display Positioning Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            onClick = { 
                HapticFeedback.light(context)
                onNavigate(NavigationDestination.DisplaySettings.route)
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = "Display Positioning",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    BasicText(
                        text = "Fine-tune island position",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = secondaryTextColor
                )
            }
        }
        
        // Setup Cards (Visible if setup not done OR permissions lost)
        if (!setupDone || !allPermissionsGranted) {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BasicText(
                        text = "Setup Required",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    BasicText(
                        text = "Enable the following permissions to use Dynamic Island:",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Accessibility Service Button
                    if (isAccessibilityEnabled) {
                        BasicText(
                            text = "✅ Accessibility Service Enabled",
                            style = TextStyle(color = Color(0xFF4CAF50), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        )
                    } else {
                        GlassTextButton(
                            text = "Enable Accessibility Service",
                            onClick = {
                                HapticFeedback.light(context)
                                openAccessibilitySettings(context)
                            },
                            backdrop = backdrop,
                            style = GlassButtonStyle.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Notification Access Button
                    if (isNotificationEnabled) {
                         BasicText(
                            text = "✅ Notification Access Enabled",
                            style = TextStyle(color = Color(0xFF4CAF50), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        )
                    } else {
                        GlassTextButton(
                            text = "Enable Notification Access",
                            onClick = {
                                HapticFeedback.light(context)
                                openNotificationSettings(context)
                            },
                            backdrop = backdrop,
                            style = GlassButtonStyle.SECONDARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Finish Setup Button (Only when both permissions granted)
                    if (allPermissionsGranted && !setupDone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassTextButton(
                            text = "Finish Setup",
                            onClick = {
                                HapticFeedback.success(context)
                                scope.launch {
                                    preferencesDataStore.setSetupDone(true)
                                }
                            },
                            backdrop = backdrop,
                            style = GlassButtonStyle.PRIMARY, // Use primary style for finish
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Features Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                padding = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BasicText(
                        text = "🎵",
                        style = TextStyle(fontSize = 40.sp)
                    )
                    BasicText(
                        text = "Music\nControl",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                padding = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BasicText(
                        text = "📞",
                        style = TextStyle(fontSize = 40.sp)
                    )
                    BasicText(
                        text = "Call\nManagement",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                padding = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BasicText(
                        text = "🔔",
                        style = TextStyle(fontSize = 40.sp)
                    )
                    BasicText(
                        text = "Smart\nNotifications",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                padding = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BasicText(
                        text = "🔋",
                        style = TextStyle(fontSize = 40.sp)
                    )
                    BasicText(
                        text = "Battery\nStatus",
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        
        // Info Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (isLightTheme) {
                Color(0xFF007AFF).copy(alpha = 0.1f)
            } else {
                Color(0xFF0A84FF).copy(0.2f)
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicText(
                    text = "💡 Tip",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                BasicText(
                    text = "Tap the 'Preview' tab to test different island states and see how they look on your device.",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
