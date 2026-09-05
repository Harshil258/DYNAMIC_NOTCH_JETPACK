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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import ai.emots.kishan_dynamic.data.premium.AdConsentPolicy
import ai.emots.kishan_dynamic.data.premium.AppAdConsentRuntime
import ai.emots.kishan_dynamic.data.premium.AppOpenAdConfiguration
import ai.emots.kishan_dynamic.data.premium.MobileAdsAppOpenGateway
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.ui.gallery.ComponentGalleryScreen
import ai.emots.kishan_dynamic.ui.screens.MainContainerScreen
import ai.emots.kishan_dynamic.ui.screens.OnboardingScreen
import ai.emots.kishan_dynamic.ui.screens.PermissionScreen
import ai.emots.kishan_dynamic.ui.screens.LanguageScreen
import ai.emots.kishan_dynamic.ui.screens.QuickControlScreen
import ai.emots.kishan_dynamic.ui.screens.StartupSplashScreen
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.model.StartupRoute
import ai.emots.kishan_dynamic.data.model.initialStartupRoute
import ai.emots.kishan_dynamic.data.model.startupRouteForIntent
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppThemeMode
import ai.emots.kishan_dynamic.ui.localization.LocalAppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppNavigationRoute {
    MainHub,
    ActionIslandEditor,
    LanguageSelection,
    Onboarding,
    PermissionHub,
    ComponentGallery
}

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_ACTION_ISLAND_EDITOR = "open_action_island_editor"
        const val EXTRA_OPEN_PREMIUM = "open_premium"
        const val EXTRA_OPEN_REWARDED_PASS = "open_rewarded_pass"
        const val EXTRA_OPEN_LIVE_ACTIVITY = "open_live_activity"
    }

    @Volatile
    private var appOpenGateway: MobileAdsAppOpenGateway? = null
    @Volatile
    private var appOpenBlocked = true
    @Volatile
    private var appOpenPremium = true
    private var appOpenColdLaunch = true

    override fun onStart() {
        super.onStart()
        val shouldAttemptResumeAd = !appOpenColdLaunch
        appOpenColdLaunch = false
        if (shouldAttemptResumeAd) {
            appOpenGateway?.showIfReady(
                activity = this,
                isColdLaunch = false,
                isBlocked = appOpenBlocked,
                isPremium = appOpenPremium
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        val openActionIslandEditor = intent.getBooleanExtra(EXTRA_OPEN_ACTION_ISLAND_EDITOR, false)
        val openPremium = intent.getBooleanExtra(EXTRA_OPEN_PREMIUM, false)
        val openRewardedPass = intent.getBooleanExtra(EXTRA_OPEN_REWARDED_PASS, false)
        val openLiveActivity = intent.getBooleanExtra(EXTRA_OPEN_LIVE_ACTIVITY, false)
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
            val context = LocalContext.current
            val preferences = remember { AuroraPreferences(context) }
            val scope = rememberCoroutineScope()
            val isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val releaseConfigProvider = remember(isDebugBuild) {
                AppReleaseRuntimeConfig.provider(context, isDebugBuild)
            }
            val releaseConfig by releaseConfigProvider.config.collectAsState()
            val consentGateway = remember(isDebugBuild) {
                AppAdConsentRuntime.gateway(
                    context = context,
                    debugGeography = if (isDebugBuild) {
                        com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                    } else null
                )
            }
            val consentSnapshot by consentGateway.state.collectAsState()
            val canRequestAds = AdConsentPolicy.canRequestAds(consentSnapshot)
            val appOpenConfiguration = remember(releaseConfig, canRequestAds) {
                AppOpenAdConfiguration.fromReleaseConfig(releaseConfig, canRequestAds)
            }
            val isProActive by preferences.isProActive.collectAsState(initial = false)
            val savedThemeMode by preferences.themeMode.collectAsState(initial = AppThemeMode.System.storageValue)
            val savedLanguage by preferences.languageCode.collectAsState(initial = "en")
            val requestedThemeMode = AppThemeMode.fromStorage(savedThemeMode)
            val themeMode = if (
                PremiumFeaturePolicy.isLocked(PremiumFeaturePolicy.THEME_SETTINGS, isProActive)
            ) {
                AppThemeMode.System
            } else {
                requestedThemeMode
            }
            val isDarkTheme = when (themeMode) {
                AppThemeMode.System -> systemDark
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }
            var currentRoute by remember { mutableStateOf<AppNavigationRoute?>(null) }

            LaunchedEffect(releaseConfigProvider) {
                releaseConfigProvider.refresh()
            }
            LaunchedEffect(consentGateway, releaseConfig.adsEnabled) {
                if (releaseConfig.adsEnabled) {
                    consentGateway.request(this@MainActivity)
                }
            }
            LaunchedEffect(appOpenConfiguration) {
                val nextGateway = MobileAdsAppOpenGateway(context, appOpenConfiguration)
                appOpenGateway?.close()
                appOpenGateway = nextGateway
            }
            LaunchedEffect(isProActive) {
                appOpenPremium = isProActive
            }
            LaunchedEffect(currentRoute) {
                appOpenBlocked = currentRoute != AppNavigationRoute.MainHub
            }
            DisposableEffect(Unit) {
                onDispose {
                    appOpenGateway?.close()
                    appOpenGateway = null
                }
            }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                val baseStartupRoute = initialStartupRoute(
                    onboardingCompleted = preferences.onboardingCompleted.first(),
                    languageSelectionCompleted = preferences.languageSelectionCompleted.first()
                )
                currentRoute = when (startupRouteForIntent(baseStartupRoute, openActionIslandEditor)) {
                    StartupRoute.LANGUAGE_SELECTION -> AppNavigationRoute.LanguageSelection
                    StartupRoute.ONBOARDING -> AppNavigationRoute.Onboarding
                    StartupRoute.MAIN_HUB -> AppNavigationRoute.MainHub
                    StartupRoute.ACTION_ISLAND_EDITOR -> AppNavigationRoute.ActionIslandEditor
                }
            }

            AppTheme(darkTheme = isDarkTheme) {
                val navigationBarArgb = AppTheme.colors.background.toArgb()
                val appIsDark = AppTheme.isDark
                SideEffect {
                    window.navigationBarColor = navigationBarArgb
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightNavigationBars = !appIsDark
                        isAppearanceLightStatusBars = !appIsDark
                    }
                }
                CompositionLocalProvider(LocalAppLanguage provides savedLanguage) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "root_navigation"
                    ) { route ->
                        when (route) {
                        null -> StartupSplashScreen()
                        AppNavigationRoute.MainHub -> {
                            MainContainerScreen(
                                initialTab = when {
                                    openPremium || openRewardedPass -> 3
                                    openLiveActivity -> 2
                                    else -> 0
                                },
                                openPremiumOnLaunch = openPremium,
                                openRewardedPassOnLaunch = openRewardedPass,
                                themeMode = themeMode,
                                onThemeModeChange = { mode ->
                                    scope.launch { preferences.setThemeMode(mode.storageValue) }
                                },
                                onExitRequested = { finishAndRemoveTask() }
                            )
                        }
                        AppNavigationRoute.ActionIslandEditor -> {
                            QuickControlScreen(onBack = { currentRoute = AppNavigationRoute.MainHub })
                        }
                        AppNavigationRoute.LanguageSelection -> {
                            LanguageScreen(
                                onBack = {},
                                onComplete = { currentRoute = AppNavigationRoute.Onboarding }
                            )
                        }
                        AppNavigationRoute.Onboarding -> {
                            OnboardingScreen(
                                onComplete = {
                                    scope.launch { preferences.setOnboardingCompleted(true) }
                                    currentRoute = AppNavigationRoute.PermissionHub
                                },
                                onSkip = {
                                    scope.launch { preferences.setOnboardingCompleted(true) }
                                    currentRoute = AppNavigationRoute.MainHub
                                }
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
                                onToggleTheme = {
                                    val nextMode = if (isDarkTheme) AppThemeMode.Light else AppThemeMode.Dark
                                    scope.launch { preferences.setThemeMode(nextMode.storageValue) }
                                }
                            )
                        }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        appOpenGateway?.close()
        appOpenGateway = null
        super.onDestroy()
    }
}
