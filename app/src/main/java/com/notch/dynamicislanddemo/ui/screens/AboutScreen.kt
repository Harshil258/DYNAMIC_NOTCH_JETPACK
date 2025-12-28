package com.notch.dynamicislanddemo.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.ui.glass.GlassCard
import com.notch.dynamicislanddemo.ui.glass.SimpleGlassCard

@Composable
fun AboutScreen(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    val secondaryTextColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.7f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Icon & Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Icon Placeholder
            SimpleGlassCard(
                modifier = Modifier.size(100.dp),
                padding = 0.dp,
                containerColor = if (isLightTheme) {
                    Color(0xFF007AFF).copy(alpha = 0.2f)
                } else {
                    Color(0xFF0A84FF).copy(alpha = 0.3f)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "🏝️",
                        style = TextStyle(fontSize = 56.sp)
                    )
                }
            }
            
            BasicText(
                text = "Dynamic Island",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            BasicText(
                text = "Version 1.0.0",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        
        // Description Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BasicText(
                    text = "About",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                BasicText(
                    text = "Bring the iPhone 14 Pro's Dynamic Island experience to your Android device. " +
                            "Enjoy seamless music control, call management, notifications, and more with " +
                            "Apple's signature glass design and smooth animations.",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    )
                )
            }
        }
        
        // Features Card
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BasicText(
                    text = "Features",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                FeatureRow("🎵", "Music Player Control")
                FeatureRow("📞", "Call Management")
                FeatureRow("🔔", "Smart Notifications")
                FeatureRow("🔋", "Battery & Charging Status")
                FeatureRow("🎛️", "Quick Settings Access")
                FeatureRow("✨", "Apple-inspired Glass UI")
            }
        }
        
        // Credits Card
        SimpleGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BasicText(
                    text = "Made with ❤️ for Android",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
                BasicText(
                    text = "Using Backdrop library by Kyant\nInspired by Apple's Dynamic Island",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                )
            }
        }
        
        // Copyright
        BasicText(
            text = "© 2024 Dynamic Island Demo\nAll rights reserved",
            style = TextStyle(
                color = secondaryTextColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        )
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String) {
    val isLightTheme = !isSystemInDarkTheme()
    val textColor = if (isLightTheme) Color.Black else Color.White
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = emoji,
            style = TextStyle(fontSize = 24.sp)
        )
        BasicText(
            text = title,
            style = TextStyle(
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
