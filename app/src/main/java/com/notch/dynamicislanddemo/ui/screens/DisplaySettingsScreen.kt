package com.notch.dynamicislanddemo.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.notch.dynamicislanddemo.ui.components.NavigationDestination
import com.notch.dynamicislanddemo.ui.glass.*
import com.notch.dynamicislanddemo.utils.HapticFeedback
import kotlinx.coroutines.launch

@Composable
fun DisplaySettingsScreen(
    backdrop: Backdrop,
    onBack: () -> Unit,
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
    
    val verticalOffset by preferencesDataStore.verticalOffset.collectAsState(initial = 12)
    val horizontalOffset by preferencesDataStore.horizontalOffset.collectAsState(initial = 0)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { 
                    HapticFeedback.light(context)
                    onBack() 
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryTextColor
                )
            }
            
            BasicText(
                text = "Display Positioning",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        // Island Positioning Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            padding = 0.dp
        ) {
            Column {
                BasicText(
                    text = "Adjust Position",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                GlassSettingSliderItem(
                    title = "Vertical Offset",
                    subtitle = "Move island up or down",
                    value = verticalOffset.toFloat(),
                    onValueChange = { 
                        scope.launch { preferencesDataStore.setVerticalOffset(it.toInt()) }
                    },
                    valueRange = -50f..500f,
                    backdrop = backdrop
                )
                
                GlassSettingSliderItem(
                    title = "Horizontal Offset",
                    subtitle = "Move island left or right",
                    value = horizontalOffset.toFloat(),
                    onValueChange = { 
                        scope.launch { preferencesDataStore.setHorizontalOffset(it.toInt()) }
                    },
                    valueRange = -200f..200f,
                    backdrop = backdrop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassTextButton(
                        text = "Reset Position",
                        onClick = {
                            HapticFeedback.medium(context)
                            scope.launch {
                                preferencesDataStore.setVerticalOffset(12)
                                preferencesDataStore.setHorizontalOffset(0)
                            }
                        },
                        backdrop = backdrop,
                        style = GlassButtonStyle.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Information
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (isLightTheme) {
                Color(0xFF007AFF).copy(alpha = 0.1f)
            } else {
                Color(0xFF0A84FF).copy(0.2f)
            }
        ) {
            BasicText(
                text = "Adjust the sliders to perfectly align the island with your device's camera or notch. The default vertical offset of 12 provides a clean gap from the top of the screen.",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}
