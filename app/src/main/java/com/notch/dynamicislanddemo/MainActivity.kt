package com.notch.dynamicislanddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.notch.dynamicislanddemo.catalog.AnimatedBackground
import com.notch.dynamicislanddemo.catalog.BackdropDemoScaffold
import com.notch.dynamicislanddemo.catalog.ripple
import com.notch.dynamicislanddemo.ui.components.AppNavigationBar
import com.notch.dynamicislanddemo.ui.components.NavigationDestination
import com.notch.dynamicislanddemo.ui.screens.AboutScreen
import com.notch.dynamicislanddemo.ui.screens.HomeScreen
import com.notch.dynamicislanddemo.ui.screens.PreviewScreen
import com.notch.dynamicislanddemo.ui.screens.SettingsScreen
import com.notch.dynamicislanddemo.ui.theme.DynamicIslandDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        setContent {
            DynamicIslandDemoTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val isLightTheme = !isSystemInDarkTheme()
    var currentDestination by remember { mutableStateOf<NavigationDestination>(NavigationDestination.Home) }
    val currentTabIndex = when (currentDestination) {
        NavigationDestination.Home -> 0
        NavigationDestination.Preview -> 1
        NavigationDestination.Settings -> 2
        NavigationDestination.About -> 3
    }
    
    CompositionLocalProvider(
        LocalIndication provides ripple(color = if (isLightTheme) Color.Black else Color.White)
    ) {
        BackdropDemoScaffold { backdrop ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Premium static background with subtle, sophisticated gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isLightTheme) {
                                    listOf(
                                        Color(0xFFF8F9FA), // Soft White
                                        Color(0xFFE9ECEF), // Light Gray
                                        Color(0xFFDEE2E6)  // Slightly Darker Gray
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF000000), // Pure Black
                                        Color(0xFF0A0A0A), // Very Dark Gray
                                        Color(0xFF141414)  // Dark Gray
                                    )
                                }
                            )
                        )
                )
                
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Main content area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (currentDestination) {
                            NavigationDestination.Home -> HomeScreen(backdrop)
                            NavigationDestination.Preview -> PreviewScreen(backdrop)
                            NavigationDestination.Settings -> SettingsScreen(backdrop)
                            NavigationDestination.About -> AboutScreen(backdrop)
                        }
                    }
                    
                    // Bottom navigation bar
                    AppNavigationBar(
                        selectedTab = currentTabIndex,
                        onTabSelected = { index ->
                            currentDestination = when (index) {
                                0 -> NavigationDestination.Home
                                1 -> NavigationDestination.Preview
                                2 -> NavigationDestination.Settings
                                3 -> NavigationDestination.About
                                else -> NavigationDestination.Home
                            }
                        },
                        backdrop = backdrop
                    )
                }
            }
        }
    }
}