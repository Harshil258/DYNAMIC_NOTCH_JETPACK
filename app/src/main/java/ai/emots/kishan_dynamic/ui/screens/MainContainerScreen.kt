package ai.emots.kishan_dynamic.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.dialogs.ExitConfirmationDialog
import ai.emots.kishan_dynamic.ui.dialogs.ForceUpdateDialog
import ai.emots.kishan_dynamic.ui.dialogs.RatingDialog
import ai.emots.kishan_dynamic.ui.dialogs.SoftUpdateDialog
import ai.emots.kishan_dynamic.ui.gallery.ComponentGalleryScreen
import ai.emots.kishan_dynamic.ui.kit.AppBackground
import ai.emots.kishan_dynamic.ui.motion.AppMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppThemeMode
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.review.RatingPromptRepository
import ai.emots.kishan_dynamic.data.premium.AdConsentPolicy
import ai.emots.kishan_dynamic.data.premium.AppAdConsentRuntime
import ai.emots.kishan_dynamic.data.premium.InterstitialAdConfiguration
import ai.emots.kishan_dynamic.data.premium.InterstitialAdFrequencyPolicy
import ai.emots.kishan_dynamic.data.premium.InterstitialAdFrequencyRepository
import ai.emots.kishan_dynamic.data.premium.MobileAdsInterstitialGateway
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.data.release.UpdateCheckRepository
import ai.emots.kishan_dynamic.data.release.UpdateDecision
import ai.emots.kishan_dynamic.data.release.UpdatePolicy
import ai.emots.kishan_dynamic.data.release.UpdateType
import ai.emots.kishan_dynamic.ui.platform.openPlayStore
import ai.emots.kishan_dynamic.ui.platform.openUpdateDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SubScreen {
    Premium,
    DisplaySettings,
    Language,
    NotificationSettings,
    MusicSettings,
    CallSettings,
    BatterySettings,
    SoundSettings,
    ThemeSettings,
    QuickControl,
    PermissionHub,
    ComponentGallery,
    About
}

private data class NavTab(
    val label: String,
    val glyph: AppleGlyph
)

/** Height reserved at the bottom of every root screen for the floating dock. */
val BottomDockInset = 92.dp

@Composable
fun MainContainerScreen(
    initialTab: Int = 0,
    openPremiumOnLaunch: Boolean = false,
    openRewardedPassOnLaunch: Boolean = false,
    themeMode: AppThemeMode = AppThemeMode.System,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onExitRequested: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkTheme = AppTheme.isDark
    val scope = rememberCoroutineScope()
    val isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val releaseConfigProvider = remember(isDebugBuild) {
        AppReleaseRuntimeConfig.provider(context, isDebugBuild)
    }
    val releaseConfig by releaseConfigProvider.config.collectAsState()
    val consentGateway = remember(isDebugBuild) {
        AppAdConsentRuntime.gateway(
            context,
            debugGeography = if (isDebugBuild) {
                com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
            } else {
                null
            }
        )
    }
    val consentSnapshot by consentGateway.state.collectAsState()
    val canRequestAds = AdConsentPolicy.canRequestAds(consentSnapshot)
    val interstitialConfiguration = remember(releaseConfig, canRequestAds) {
        InterstitialAdConfiguration.fromReleaseConfig(releaseConfig, canRequestAds)
    }
    val interstitialGateway = remember(interstitialConfiguration) {
        MobileAdsInterstitialGateway(context, interstitialConfiguration)
    }
    val frequencyRepository = remember { InterstitialAdFrequencyRepository(context) }
    val ratingPromptRepository = remember { RatingPromptRepository(context) }
    val updateCheckRepository = remember { UpdateCheckRepository(context) }
    val preferences = remember { AuroraPreferences(context) }
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var currentSubScreen by remember(openPremiumOnLaunch) {
        mutableStateOf<SubScreen?>(if (openPremiumOnLaunch) SubScreen.Premium else null)
    }
    var showExitDialog by remember { mutableStateOf(false) }
    var showRatingPrompt by remember { mutableStateOf(false) }
    var updateDecision by remember { mutableStateOf<UpdateDecision?>(null) }

    LaunchedEffect(releaseConfigProvider) {
        releaseConfigProvider.refresh()
    }
    LaunchedEffect(consentGateway, releaseConfig.adsEnabled) {
        if (releaseConfig.adsEnabled) {
            (context as? Activity)?.let(consentGateway::request)
        }
    }
    LaunchedEffect(releaseConfig.analyticsEnabled, releaseConfig.analyticsEndpoint) {
        AppAnalytics.configure(releaseConfig.analyticsEnabled, releaseConfig.analyticsEndpoint)
    }
    LaunchedEffect(releaseConfig) {
        if (!releaseConfig.updateEnabled) return@LaunchedEffect
        val now = System.currentTimeMillis()
        if (!updateCheckRepository.shouldCheck(now, releaseConfig.updateCheckIntervalHours)) {
            return@LaunchedEffect
        }
        updateCheckRepository.markChecked(now)
        val decision = evaluateUpdate(context, releaseConfig)
        updateDecision = decision.takeUnless {
            it.type == UpdateType.SOFT &&
                updateCheckRepository.dismissedVersionCode() == it.latestVersionCode
        }
    }
    LaunchedEffect(Unit) {
        ratingPromptRepository.recordAppOpenOnce()
    }
    LaunchedEffect(selectedTab, currentSubScreen, releaseConfig.ratingEnabled) {
        showRatingPrompt = false
        if (selectedTab != 0 || currentSubScreen != null || !releaseConfig.ratingEnabled) {
            return@LaunchedEffect
        }
        delay(60_000L)
        if (ratingPromptRepository.shouldPrompt(enabled = releaseConfig.ratingEnabled)) {
            showRatingPrompt = true
        }
    }
    DisposableEffect(interstitialGateway) {
        onDispose { interstitialGateway.close() }
    }

    fun selectTab(nextTab: Int) {
        if (nextTab == selectedTab) return
        val activity = context as? Activity
        scope.launch {
            val screenCount = frequencyRepository.recordScreenChange()
            val clickCount = frequencyRepository.recordNavigationClick()
            val shouldShow = !isProActive && InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = screenCount,
                clickCount = clickCount,
                screenEvery = interstitialConfiguration.screenEvery,
                clickEvery = interstitialConfiguration.clickEvery,
                startFromScreen = interstitialConfiguration.startFromScreen
            )
            if (shouldShow && activity != null) {
                interstitialGateway.showIfReady(activity) { selectedTab = nextTab }
            } else {
                selectedTab = nextTab
            }
        }
    }

    fun requestUpdateCheck() {
        scope.launch {
            releaseConfigProvider.refresh()
            val snapshot = releaseConfigProvider.config.first()
            val now = System.currentTimeMillis()
            updateCheckRepository.markChecked(now)
            val decision = evaluateUpdate(context, snapshot)
            updateDecision = decision.takeUnless {
                it.type == UpdateType.SOFT &&
                    updateCheckRepository.dismissedVersionCode() == it.latestVersionCode
            }
        }
    }

    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }
    BackHandler(enabled = currentSubScreen == null && !showExitDialog) {
        showExitDialog = true
    }

    val tabs = remember {
        listOf(
            NavTab("Island", AppleGlyph.Notch),
            NavTab("Features", AppleGlyph.Controls),
            NavTab("Live", AppleGlyph.Music),
            NavTab("Settings", AppleGlyph.Settings)
        )
    }

    AppBackground {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (targetState != null) {
                    AppMotion.pushEnter() togetherWith AppMotion.pushExit()
                } else {
                    AppMotion.popEnter() togetherWith AppMotion.popExit()
                }
            },
            label = "sub_screen_navigator"
        ) { sub ->
            when (sub) {
                SubScreen.Premium -> PremiumScreen(onBack = { currentSubScreen = null })
                SubScreen.DisplaySettings -> DisplaySettingsScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.Language -> LanguageScreen(onBack = { currentSubScreen = null })
                SubScreen.NotificationSettings -> NotificationSettingsScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.MusicSettings -> MusicSettingsScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.CallSettings -> CallSettingsScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.BatterySettings -> BatterySettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.SoundSettings -> SoundSettingsScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.ThemeSettings -> ThemeSettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.QuickControl -> QuickControlScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToPremium = { currentSubScreen = SubScreen.Premium }
                )
                SubScreen.PermissionHub -> PermissionScreen(
                    onContinue = { currentSubScreen = null },
                    onBack = { currentSubScreen = null }
                )
                SubScreen.ComponentGallery -> ComponentGalleryScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        onThemeModeChange(
                            if (isDarkTheme) AppThemeMode.Light else AppThemeMode.Dark
                        )
                    },
                    onBack = { currentSubScreen = null }
                )
                SubScreen.About -> AboutScreen(
                    onBack = { currentSubScreen = null },
                    onRate = { openPlayStore(context) },
                    onShare = { context.shareAuroraApp() }
                )

                null -> Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            AppMotion.fadeThroughEnter() togetherWith AppMotion.fadeThroughExit()
                        },
                        label = "root_tab_switch"
                    ) { tab ->
                        when (tab) {
                            0 -> PlaygroundScreen(
                                onNavigateDisplaySettings = { currentSubScreen = SubScreen.DisplaySettings },
                                onNavigateNotificationSettings = { currentSubScreen = SubScreen.NotificationSettings },
                                onNavigateMusicSettings = { currentSubScreen = SubScreen.MusicSettings },
                                onNavigateCallSettings = { currentSubScreen = SubScreen.CallSettings },
                                onNavigateBatterySettings = { currentSubScreen = SubScreen.BatterySettings },
                                onNavigateSoundSettings = { currentSubScreen = SubScreen.SoundSettings },
                                onNavigateQuickControl = { currentSubScreen = SubScreen.QuickControl },
                                onNavigatePermissions = { currentSubScreen = SubScreen.PermissionHub },
                                onNavigateVault = { selectTab(3) }
                            )

                            1 -> StudioScreen(
                                onNavigateDisplay = { currentSubScreen = SubScreen.DisplaySettings },
                                onNavigateNotifications = { currentSubScreen = SubScreen.NotificationSettings },
                                onNavigateMusic = { currentSubScreen = SubScreen.MusicSettings },
                                onNavigateCalls = { currentSubScreen = SubScreen.CallSettings },
                                onNavigateBattery = { currentSubScreen = SubScreen.BatterySettings },
                                onNavigateSound = { currentSubScreen = SubScreen.SoundSettings },
                                onNavigateQuickControl = { currentSubScreen = SubScreen.QuickControl }
                            )

                            2 -> LiveActivitiesScreen()
                            3 -> SettingsVaultScreen(
                                onNavigatePermissions = { currentSubScreen = SubScreen.PermissionHub },
                                onNavigateLanguage = { currentSubScreen = SubScreen.Language },
                                onNavigateTheme = { currentSubScreen = SubScreen.ThemeSettings },
                                onNavigateGallery = { currentSubScreen = SubScreen.ComponentGallery },
                                onNavigateAbout = { currentSubScreen = SubScreen.About },
                                onNavigatePremium = { currentSubScreen = SubScreen.Premium },
                                onCheckForUpdates = ::requestUpdateCheck,
                                openPremiumOnLaunch = false,
                                openRewardedPassOnLaunch = openRewardedPassOnLaunch,
                                themeMode = themeMode,
                                onThemeModeChange = onThemeModeChange
                            )
                        }
                    }

                    BottomNavigationDock(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onSelect = ::selectTab,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onExit = {
                showExitDialog = false
                onExitRequested()
            }
        )
    }
    if (showRatingPrompt) {
        RatingDialog(
            onDismiss = {
                ratingPromptRepository.recordDismissed()
                showRatingPrompt = false
            },
            onRateClick = {
                ratingPromptRepository.markRated()
                showRatingPrompt = false
                openPlayStore(context)
            }
        )
    }
    when (updateDecision?.type) {
        UpdateType.FORCE -> ForceUpdateDialog(
            versionCode = updateDecision?.latestVersionCode,
            notes = updateDecision?.notes.orEmpty(),
            onUpdateClick = {
                openUpdateDestination(context, releaseConfig.updateUrl)
            }
        )
        UpdateType.SOFT -> SoftUpdateDialog(
            versionCode = updateDecision?.latestVersionCode,
            changes = updateDecision?.notes.orEmpty(),
            onUpdateClick = {
                updateDecision = null
                openUpdateDestination(context, releaseConfig.updateUrl)
            },
            onDismiss = {
                updateDecision?.latestVersionCode?.let(updateCheckRepository::markDismissed)
                updateDecision = null
            }
        )
        UpdateType.NONE, null -> Unit
    }
}

private fun evaluateUpdate(
    context: android.content.Context,
    config: ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
): UpdateDecision = UpdatePolicy.decide(
    currentVersionCode = runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            ).longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }
    }.getOrDefault(0),
    latestVersionCode = config.latestVersionCode,
    minimumVersionCode = config.minimumVersionCode,
    enabled = config.updateEnabled,
    notes = config.updateNotes.lines().map(String::trim).filter(String::isNotBlank)
)

@Composable
private fun BottomNavigationDock(
    tabs: List<NavTab>,
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Soft fade so scrolling content dissolves behind the dock.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            AppTheme.colors.background.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background)
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(64.dp)
                    .shadow(
                        elevation = AppTheme.elevation.raised,
                        shape = RoundedCornerShape(AppTheme.radius.pill),
                        ambientColor = AppTheme.colors.shadow.copy(alpha = 0.24f),
                        spotColor = AppTheme.colors.shadowStrong.copy(alpha = 0.30f)
                    )
                    .clip(RoundedCornerShape(AppTheme.radius.pill))
                    .background(AppTheme.colors.surfaceGradient(), RoundedCornerShape(AppTheme.radius.pill))
                    .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.pill))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (pressed) 0.93f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                        label = "tab_scale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .scale(scale)
                            .clip(RoundedCornerShape(AppTheme.radius.pill))
                            .background(
                                if (isSelected) AppTheme.colors.surfaceElevated else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interaction,
                                indication = null
                            ) { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppleIcon(
                            glyph = tab.glyph,
                            tint = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
                            size = 19.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = tab.label,
                            style = AppTheme.typography.caption,
                            color = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
