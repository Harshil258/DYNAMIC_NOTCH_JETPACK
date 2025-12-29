package com.notch.dynamicislanddemo.ui.screens

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.data.PreferencesDataStore
import com.notch.dynamicislanddemo.ui.glass.GlassCard
import com.notch.dynamicislanddemo.ui.glass.GlassSettingToggleItem
import com.notch.dynamicislanddemo.ui.glass.GlassSettingSliderItem
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesDataStore = remember { PreferencesDataStore(context) }
    
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    val secondaryTextColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.7f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    // Settings state
    val isAlwaysOnTop by preferencesDataStore.isAlwaysOnTop.collectAsState(initial = true)
    val showOnLockScreen by preferencesDataStore.showOnLockScreen.collectAsState(initial = true)
    val hapticFeedback by preferencesDataStore.hapticFeedback.collectAsState(initial = true)
    val autoExpand by preferencesDataStore.autoExpand.collectAsState(initial = true)
    val showBatteryNotifications by preferencesDataStore.showBatteryNotifications.collectAsState(initial = true)
    val compactMusicControls by preferencesDataStore.compactMusicControls.collectAsState(initial = false)
    val animationSpeed by preferencesDataStore.animationSpeed.collectAsState(initial = true) // true = normal, false = fast
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        BasicText(
            text = "Settings",
            style = TextStyle(
                color = primaryTextColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        // Display Settings
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            padding = 0.dp
        ) {
            Column {
                // Section Header
                BasicText(
                    text = "Display",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                GlassSettingToggleItem(
                    title = "Always on Top",
                    subtitle = "Keep island above all apps",
                    isChecked = isAlwaysOnTop,
                    onCheckedChange = { scope.launch { preferencesDataStore.setAlwaysOnTop(it) } },
                    backdrop = backdrop
                )
                
                GlassSettingToggleItem(
                    title = "Show on Lock Screen",
                    subtitle = "Display island when device is locked",
                    isChecked = showOnLockScreen,
                    onCheckedChange = { scope.launch { preferencesDataStore.setShowOnLockScreen(it) } },
                    backdrop = backdrop
                )
                
                GlassSettingToggleItem(
                    title = "Normal Animation Speed",
                    subtitle = if (animationSpeed) "Smooth animations" else "Fast animations",
                    isChecked = animationSpeed,
                    onCheckedChange = { scope.launch { preferencesDataStore.setAnimationSpeed(it) } },
                    backdrop = backdrop
                )
            }
        }
        
        // Behavior Settings
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            padding = 0.dp
        ) {
            Column {
                BasicText(
                    text = "Behavior",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                GlassSettingToggleItem(
                    title = "Auto Expand",
                    subtitle = "Automatically expand new notifications",
                    isChecked = autoExpand,
                    onCheckedChange = { scope.launch { preferencesDataStore.setAutoExpand(it) } },
                    backdrop = backdrop
                )
                
                GlassSettingToggleItem(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on interactions",
                    isChecked = hapticFeedback,
                    onCheckedChange = { scope.launch { preferencesDataStore.setHapticFeedback(it) } },
                    backdrop = backdrop
                )
                
                GlassSettingToggleItem(
                    title = "Compact Music Controls",
                    subtitle = "Use simplified music player",
                    isChecked = compactMusicControls,
                    onCheckedChange = { scope.launch { preferencesDataStore.setCompactMusicControls(it) } },
                    backdrop = backdrop
                )
            }
        }
        
        // Notifications Settings
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            padding = 0.dp
        ) {
            Column {
                BasicText(
                    text = "Notifications",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                GlassSettingToggleItem(
                    title = "Battery Notifications",
                    subtitle = "Show charging and battery status",
                    isChecked = showBatteryNotifications,
                    onCheckedChange = { scope.launch { preferencesDataStore.setShowBatteryNotifications(it) } },
                    backdrop = backdrop
                )
            }
        }
        
        // Info Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicText(
                text = "Dynamic Island v1.0.0",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            BasicText(
                text = "Settings are saved automatically",
                style = TextStyle(
                    color = secondaryTextColor.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

