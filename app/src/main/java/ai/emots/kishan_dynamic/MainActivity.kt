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
import ai.emots.kishan_dynamic.data.analytics.AnalyticsConsentManager
import ai.emots.kishan_dynamic.data.analytics.AnalyticsUserPropertyManager
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.LocalAnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.LocalScreenDeduplicator
import ai.emots.kishan_dynamic.data.analytics.ScreenDeduplicator
import ai.emots.kishan_dynamic.data.premium.AdConsentStatus
import ai.emots.kishan_dynamic.ui.theme.AppThemeMode
import ai.emots.kishan_dynamic.ui.localization.LocalAppLanguage
import ai.emots.kishan_dynamic.service.CallActionLauncher
import ai.emots.kishan_dynamic.service.CallSessionCoordinator
import ai.emots.kishan_dynamic.service.CallSummaryNotifier
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryDialog
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryData
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
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
        const val EXTRA_OPEN_CALL_SUMMARY = "open_call_summary"
        const val EXTRA_POST_PREMIUM_EXPIRY = "post_premium_expiry"

        @Volatile
        var isActivityInForeground = false
            private set
    }

    private var pendingCallSummaryFromIntent by mutableStateOf<CallRecord?>(null)

    override fun onResume() {
        super.onResume()
        isActivityInForeground = true
    }

    override fun onPause() {
        isActivityInForeground = false
        super.onPause()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractCallSummaryFromIntent(intent)
        extractExpiryFromIntent(intent)
    }

    private fun extractExpiryFromIntent(intent: android.content.Intent) {
        if (intent.hasExtra(EXTRA_POST_PREMIUM_EXPIRY)) {
            val hours = intent.getIntExtra(EXTRA_POST_PREMIUM_EXPIRY, 0)
            ai.emots.kishan_dynamic.service.IslandStateManager.postPremiumExpiry(hours)
        }
    }

    private fun extractCallSummaryFromIntent(intent: android.content.Intent) {
        if (intent.getBooleanExtra(EXTRA_OPEN_CALL_SUMMARY, false)) {
            val recordId = intent.getStringExtra("call_record_id")
            val contactName = intent.getStringExtra("call_contact_name") ?: "Unknown Caller"
            val phoneNumber = intent.getStringExtra("call_phone_number") ?: "Unknown Caller"
            val durationSeconds = intent.getLongExtra("call_duration_seconds", 0L)
            val directionName = intent.getStringExtra("call_direction") ?: "MISSED"
            val direction = runCatching { CallDirection.valueOf(directionName) }
                .getOrDefault(CallDirection.MISSED)

            pendingCallSummaryFromIntent = CallRecord(
                id = recordId ?: "${System.currentTimeMillis()}-$phoneNumber",
                contactName = contactName,
                phoneNumber = phoneNumber,
                startedAtMillis = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                direction = direction
            )
        }
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
        enableEdgeToEdge()
        extractCallSummaryFromIntent(intent)
        extractExpiryFromIntent(intent)
        val openActionIslandEditor = intent.getBooleanExtra(EXTRA_OPEN_ACTION_ISLAND_EDITOR, false)
        val openPremium = intent.getBooleanExtra(EXTRA_OPEN_PREMIUM, false)
        val openRewardedPass = intent.getBooleanExtra(EXTRA_OPEN_REWARDED_PASS, false)
        val openLiveActivity = intent.getBooleanExtra(EXTRA_OPEN_LIVE_ACTIVITY, false)
        val initiallyDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.navigationBarColor = if (initiallyDark) android.graphics.Color.BLACK else android.graphics.Color.rgb(248, 249, 251)
        }
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
            val isAdFree by preferences.isAdFree.collectAsState(initial = false)
            val savedThemeMode by preferences.themeMode.collectAsState(initial = AppThemeMode.System.storageValue)
            val savedLanguage by preferences.languageCode.collectAsState(initial = "en")
            val reduceMotion by preferences.reduceMotion.collectAsState(initial = false)
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
            val analyticsTracker = remember { FirebaseAnalyticsTracker(context) }
            val screenDeduplicator = remember { ScreenDeduplicator(analyticsTracker) }
            val consentManager = remember { AnalyticsConsentManager(context) }
            val userPropertyManager = remember { AnalyticsUserPropertyManager(context, analyticsTracker) }

            LaunchedEffect(userPropertyManager) {
                userPropertyManager.syncImmediate()
                userPropertyManager.startObserving()
            }

            LaunchedEffect(consentSnapshot) {
                if (consentSnapshot.status == AdConsentStatus.READY) {
                    consentManager.updateConsent(
                        analyticsStorageGranted = true,
                        adStorageGranted = consentSnapshot.canRequestAds,
                        adUserDataGranted = consentSnapshot.canRequestAds,
                        adPersonalizationGranted = consentSnapshot.canRequestAds
                    )
                }
            }

            var currentRoute by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<AppNavigationRoute?>(null) }

            LaunchedEffect(currentRoute) {
                when (currentRoute) {
                    AppNavigationRoute.LanguageSelection -> screenDeduplicator.onScreenVisible("language_selection")
                    AppNavigationRoute.Onboarding -> screenDeduplicator.onScreenVisible("onboarding")
                    AppNavigationRoute.PermissionHub -> screenDeduplicator.onScreenVisible("permission_hub")
                    AppNavigationRoute.ActionIslandEditor -> screenDeduplicator.onScreenVisible("action_island_editor")
                    AppNavigationRoute.ComponentGallery -> screenDeduplicator.onScreenVisible("component_gallery")
                    AppNavigationRoute.MainHub -> Unit
                    null -> Unit
                }
            }

            LaunchedEffect(releaseConfigProvider) {
                releaseConfigProvider.refresh()
            }
            LaunchedEffect(releaseConfig) {
                val entitlementManager = ai.emots.kishan_dynamic.data.premium.EntitlementManager.getInstance(context)
                entitlementManager.trialManager.checkOrStartTrial(releaseConfig)
                entitlementManager.rewardedAccessManager.cleanupExpiredDays()
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
            LaunchedEffect(isAdFree) {
                appOpenPremium = isAdFree
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
                if (currentRoute != null) return@LaunchedEffect
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
                    @Suppress("DEPRECATION")
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        window.navigationBarColor = navigationBarArgb
                        window.statusBarColor = android.graphics.Color.TRANSPARENT
                    }
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightNavigationBars = !appIsDark
                        isAppearanceLightStatusBars = !appIsDark
                    }
                }
                CompositionLocalProvider(
                    LocalAppLanguage provides savedLanguage,
                    ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion provides reduceMotion,
                    LocalAnalyticsTracker provides analyticsTracker,
                    LocalScreenDeduplicator provides screenDeduplicator
                ) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = {
                            if (reduceMotion) {
                                androidx.compose.animation.EnterTransition.None togetherWith
                                    androidx.compose.animation.ExitTransition.None
                            } else fadeIn() togetherWith fadeOut()
                        },
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

                    val activeCallSummary by CallSessionCoordinator.activeCallSummary.collectAsState()
                    val summaryToShow = pendingCallSummaryFromIntent ?: activeCallSummary
                    if (summaryToShow != null) {
                        CallSummaryDialog(
                            data = CallSummaryData(
                                contactName = summaryToShow.contactName,
                                phoneNumber = summaryToShow.phoneNumber,
                                durationFormatted = if (summaryToShow.direction == CallDirection.MISSED) "Missed" else {
                                    "%02d:%02d".format(summaryToShow.durationSeconds / 60L, summaryToShow.durationSeconds % 60L)
                                },
                                callType = when (summaryToShow.direction) {
                                    CallDirection.INCOMING -> "Incoming call"
                                    CallDirection.OUTGOING -> "Outgoing call"
                                    CallDirection.MISSED -> "Missed call"
                                },
                                timestamp = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(summaryToShow.startedAtMillis)),
                                avatarUri = summaryToShow.avatarUri
                            ),
                            onDismiss = {
                                pendingCallSummaryFromIntent = null
                                CallSessionCoordinator.clearActiveCallSummary()
                                CallSummaryNotifier.dismiss(context)
                            },
                            onRedial = { CallActionLauncher.openCall(context, summaryToShow.phoneNumber) },
                            onMessage = { CallActionLauncher.openSms(context, summaryToShow.phoneNumber) },
                            onWhatsApp = { CallActionLauncher.openWhatsApp(context, summaryToShow.phoneNumber) },
                            onSave = { CallActionLauncher.openSaveContact(context, summaryToShow.contactName, summaryToShow.phoneNumber) },
                            onCopy = { CallActionLauncher.copyToClipboard(context, summaryToShow.phoneNumber) }
                        )
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
