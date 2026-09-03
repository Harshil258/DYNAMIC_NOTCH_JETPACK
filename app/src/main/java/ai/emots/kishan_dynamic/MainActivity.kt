package ai.emots.kishan_dynamic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ai.emots.kishan_dynamic.ui.gallery.ComponentGalleryScreen
import ai.emots.kishan_dynamic.ui.screens.MainContainerScreen
import ai.emots.kishan_dynamic.ui.screens.OnboardingScreen
import ai.emots.kishan_dynamic.ui.screens.PermissionScreen
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme

enum class AppNavigationRoute {
    MainHub,
    Onboarding,
    PermissionHub,
    ComponentGallery
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        val initiallyDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        window.navigationBarColor = if (initiallyDark) android.graphics.Color.BLACK else android.graphics.Color.rgb(248, 249, 251)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = !initiallyDark
            isAppearanceLightStatusBars = !initiallyDark
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            SideEffect {
                window.navigationBarColor = if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.rgb(248, 249, 251)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightNavigationBars = !isDarkTheme
                    isAppearanceLightStatusBars = !isDarkTheme
                }
            }
            var currentRoute by remember { mutableStateOf(AppNavigationRoute.MainHub) }

            AuroraIslandTheme(darkTheme = isDarkTheme) {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "root_navigation"
                ) { route ->
                    when (route) {
                        AppNavigationRoute.MainHub -> {
                            MainContainerScreen()
                        }
                        AppNavigationRoute.Onboarding -> {
                            OnboardingScreen(
                                onComplete = { currentRoute = AppNavigationRoute.PermissionHub },
                                onSkip = { currentRoute = AppNavigationRoute.MainHub }
                            )
                        }
                        AppNavigationRoute.PermissionHub -> {
                            PermissionScreen(
                                onContinue = { currentRoute = AppNavigationRoute.MainHub },
                                onBack = { currentRoute = AppNavigationRoute.Onboarding }
                            )
                        }
                        AppNavigationRoute.ComponentGallery -> {
                            ComponentGalleryScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}
