package com.notch.dynamicislanddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationDestination.Home.route

    val currentDestination = remember(currentRoute) {
        NavigationDestination.values.find { it.route == currentRoute } ?: NavigationDestination.Home
    }
    
    val currentTabIndex = when (currentDestination) {
        NavigationDestination.Home -> 0
        NavigationDestination.Preview -> 1
        NavigationDestination.Settings -> 2
        NavigationDestination.About -> 3
        else -> 0
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
                        NavHost(
                            navController = navController,
                            startDestination = NavigationDestination.Home.route,
                            enterTransition = {
                                slideInHorizontally { it } + fadeIn()
                            },
                            exitTransition = {
                                slideOutHorizontally { -it } + fadeOut()
                            },
                            popEnterTransition = {
                                slideInHorizontally { -it } + fadeIn()
                            },
                            popExitTransition = {
                                slideOutHorizontally { it } + fadeOut()
                            }
                        ) {
                            composable(NavigationDestination.Home.route) {
                                HomeScreen(
                                    backdrop = backdrop,
                                    onNavigate = { route ->
                                        navController.navigate(route)
                                    }
                                )
                            }
                            composable(NavigationDestination.Preview.route) { PreviewScreen(backdrop) }
                            composable(NavigationDestination.Settings.route) { SettingsScreen(backdrop) }
                            composable(NavigationDestination.About.route) { AboutScreen(backdrop) }
                            composable(NavigationDestination.DisplaySettings.route) {
                                com.notch.dynamicislanddemo.ui.screens.DisplaySettingsScreen(
                                    backdrop = backdrop,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                    
                    // Bottom navigation bar - Only show if current destination is a tab
                    if (currentDestination.isTab) {
                        AppNavigationBar(
                            selectedTab = currentTabIndex,
                            onTabSelected = { index ->
                                val destination = when (index) {
                                    0 -> NavigationDestination.Home
                                    1 -> NavigationDestination.Preview
                                    2 -> NavigationDestination.Settings
                                    3 -> NavigationDestination.About
                                    else -> NavigationDestination.Home
                                }
                                navController.navigate(destination.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },
                            backdrop = backdrop
                        )
                    }
                }
            }
        }
    }
}