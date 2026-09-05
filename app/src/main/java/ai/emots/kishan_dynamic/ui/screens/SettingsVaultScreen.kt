package ai.emots.kishan_dynamic.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.AppLanguages
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.RewardedAdConfiguration
import ai.emots.kishan_dynamic.data.premium.PremiumAccessResult
import ai.emots.kishan_dynamic.data.premium.MobileAdsRewardedPassGateway
import ai.emots.kishan_dynamic.data.premium.PlayBillingPremiumAccessGateway
import ai.emots.kishan_dynamic.data.premium.RewardedPassResult
import ai.emots.kishan_dynamic.data.premium.RewardedPassPolicy
import ai.emots.kishan_dynamic.data.premium.BannerAdConfiguration
import ai.emots.kishan_dynamic.data.premium.NativeAdConfiguration
import ai.emots.kishan_dynamic.data.premium.AdConsentPolicy
import ai.emots.kishan_dynamic.data.premium.AppAdConsentRuntime
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.data.release.ReleaseConfigRefreshPolicy
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AdaptiveBannerAdSlot
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.RemoveAdsInlineCta
import ai.emots.kishan_dynamic.ui.components.NativeAdCard
import ai.emots.kishan_dynamic.ui.dialogs.FeedbackDialog
import ai.emots.kishan_dynamic.ui.dialogs.LegalDocumentDialog
import ai.emots.kishan_dynamic.ui.dialogs.NoInternetDialog
import ai.emots.kishan_dynamic.ui.dialogs.ProPaywallSheet
import ai.emots.kishan_dynamic.ui.dialogs.RatingDialog
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppInfoRow
import ai.emots.kishan_dynamic.ui.kit.AppLargeTitle
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.platform.openPlayStore
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppThemeMode
import ai.emots.kishan_dynamic.ui.theme.accentGradient
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient
import kotlinx.coroutines.launch

private data class PricingPlan(
    val id: String,
    val title: String,
    val price: String,
    val badge: String? = null
)

/**
 * Settings tab — membership, language, support and about,
 * all in the same grouped-card language as the rest of the app.
 */
@Composable
fun SettingsVaultScreen(
    onNavigatePermissions: () -> Unit = {},
    onNavigateLanguage: () -> Unit = {},
    onNavigateTheme: () -> Unit = {},
    onNavigateGallery: () -> Unit = {},
    onNavigateAbout: () -> Unit = {},
    onNavigatePremium: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    openPremiumOnLaunch: Boolean = false,
    openRewardedPassOnLaunch: Boolean = false,
    themeMode: AppThemeMode = AppThemeMode.System,
    onThemeModeChange: (AppThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = remember { AuroraPreferences(context) }
    val premiumGateway = remember { PlayBillingPremiumAccessGateway(context) }
    val isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val releaseEndpoint = remember {
        context.getString(ai.emots.kishan_dynamic.R.string.release_config_endpoint)
    }
    val releaseConfigProvider = remember(isDebugBuild) {
        AppReleaseRuntimeConfig.provider(context, isDebugBuild)
    }
    val releaseConfig by releaseConfigProvider.config.collectAsState()
    val releaseRefreshError by releaseConfigProvider.lastRefreshError.collectAsState(initial = null)
    val bannerConfiguration = remember(releaseConfig) {
        BannerAdConfiguration.fromReleaseConfig(releaseConfig)
    }
    val nativeConfiguration = remember(releaseConfig) {
        NativeAdConfiguration.fromReleaseConfig(releaseConfig)
    }
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
    val rewardedConfiguration = remember(releaseConfig, canRequestAds) {
        RewardedAdConfiguration.fromReleaseConfig(
            releaseConfig,
            canRequestAds = canRequestAds
        )
    }
    val rewardedGateway = remember(rewardedConfiguration) {
        MobileAdsRewardedPassGateway(context, rewardedConfiguration)
    }
    val scope = rememberCoroutineScope()
    var releaseRefreshAttempt by remember { mutableIntStateOf(0) }
    var isRefreshingRelease by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    DisposableEffect(premiumGateway) {
        onDispose { premiumGateway.close() }
    }
    DisposableEffect(rewardedGateway) {
        onDispose { rewardedGateway.close() }
    }
    LaunchedEffect(consentGateway, releaseConfig.adsEnabled) {
        if (releaseConfig.adsEnabled) {
            (context as? Activity)?.let(consentGateway::request)
        }
    }
    LaunchedEffect(releaseConfigProvider, releaseRefreshAttempt) {
        isRefreshingRelease = true
        releaseConfigProvider.refresh()
        isRefreshingRelease = false
    }
    LaunchedEffect(releaseEndpoint, releaseRefreshError) {
        if (ReleaseConfigRefreshPolicy.shouldPrompt(releaseEndpoint, releaseRefreshError)) {
            showNoInternetDialog = true
        }
        if (releaseRefreshError == null) {
            showNoInternetDialog = false
        }
    }
    LaunchedEffect(releaseConfig.analyticsEnabled, releaseConfig.analyticsEndpoint) {
        AppAnalytics.configure(
            enabled = releaseConfig.analyticsEnabled,
            endpoint = releaseConfig.analyticsEndpoint
        )
    }
    val billingStatus by premiumGateway.status.collectAsState(initial = null)
    val rewardedStatus by rewardedGateway.status.collectAsState(initial = null)

    val isProActive by preferences.isProActive.collectAsState(initial = false)
    var bannerLoaded by remember(bannerConfiguration, isProActive, canRequestAds) { mutableStateOf(false) }
    var nativeLoaded by remember(nativeConfiguration, isProActive, canRequestAds) { mutableStateOf(false) }
    val proExpiresAt by preferences.proExpiresAt.collectAsState(initial = null)
    val proSource by preferences.proSource.collectAsState(initial = "none")
    val savedLanguage by preferences.languageCode.collectAsState(initial = "en")
    val alwaysOnTop by preferences.alwaysOnTop.collectAsState(initial = true)
    val showOnLockScreen by preferences.showOnLockScreen.collectAsState(initial = true)
    val animationSpeedNormal by preferences.animationSpeedNormal.collectAsState(initial = true)

    var showPaywall by remember { mutableStateOf(openPremiumOnLaunch) }
    var premiumMessage by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var showRating by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var selectedPlanIndex by remember { mutableIntStateOf(2) }

    LaunchedEffect(openPremiumOnLaunch) {
        if (openPremiumOnLaunch) showPaywall = true
    }

    LaunchedEffect(openRewardedPassOnLaunch, rewardedGateway) {
        if (!openRewardedPassOnLaunch) return@LaunchedEffect
        premiumMessage = when (val activity = context as? Activity) {
            null -> "Rewarded passes require an active app screen."
            else -> when (val result = rewardedGateway.show(activity)) {
                is RewardedPassResult.Unavailable -> result.message
                is RewardedPassResult.Started -> result.message
            }
        }
    }

    val plans = remember {
        listOf(
            PricingPlan("weekly", "Weekly", "₹39"),
            PricingPlan("monthly", "Monthly", "₹59"),
            PricingPlan("yearly", "Yearly", "₹199", "Save 70%"),
            PricingPlan("lifetime", "Lifetime", "₹499", "Best value")
        )
    }

    AppScreen(bottomInset = BottomDockInset) {
        AppLargeTitle(
            title = "Settings",
            subtitle = "Membership, language and support.",
            modifier = Modifier.appReveal(0)
        )

        // ---------------------------------------------------------------
        // Membership
        // ---------------------------------------------------------------
        AppSectionTitle("Membership")
        AppCard(modifier = Modifier.appReveal(1)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = if (isProActive) "Dynamic Island Pro" else "Unlock Pro",
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    AppText(
                        text = "Ad-free, every island style and unlimited customisation.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                AppStatusPill(
                    text = when {
                        !isProActive -> "Free"
                        proExpiresAt != null -> "Pass · ${remainingHours(proExpiresAt!!)}h"
                        else -> "Active"
                    },
                    color = if (isProActive) AppTheme.colors.success else AppTheme.colors.textTertiary
                )
            }

            if (!isProActive) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    plans.forEachIndexed { index, plan ->
                        PlanCard(
                            plan = plan,
                            selected = selectedPlanIndex == index,
                            onClick = { selectedPlanIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                AppButton(
                    text = "Continue with ${plans[selectedPlanIndex].title}",
                    onClick = onNavigatePremium,
                    glyph = AppleGlyph.Crown
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                AppButton(
                    text = "Watch an ad for a ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)} pass",
                    onClick = {
                        premiumMessage = when (val activity = context as? Activity) {
                            null -> "Rewarded passes require an active app screen."
                            else -> when (val result = rewardedGateway.show(activity)) {
                                is RewardedPassResult.Unavailable -> result.message
                                is RewardedPassResult.Started -> result.message
                            }
                        }
                    },
                    style = AppButtonStyle.Secondary
                )
                AppButton(
                    text = "Restore purchases",
                    onClick = {
                        premiumMessage = when (val result = premiumGateway.restorePurchases()) {
                            is PremiumAccessResult.Unavailable -> result.message
                            is PremiumAccessResult.Started -> result.message
                        }
                    },
                    style = AppButtonStyle.Secondary
                )
                (premiumMessage ?: billingStatus ?: rewardedStatus)?.let { message ->
                    AppText(
                        text = message,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textSecondary
                    )
                }
                if (proSource == "rewarded_pass" && proExpiresAt != null) {
                    AppText(
                        text = "A rewarded pass stays active for ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)} on this device.",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textTertiary
                    )
                }
            }
            if (!isProActive && bannerConfiguration.enabled) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                AdaptiveBannerAdSlot(
                    configuration = bannerConfiguration,
                    isProActive = isProActive,
                    canRequestAds = canRequestAds,
                    onAdLoaded = { bannerLoaded = true }
                )
                if (bannerLoaded) {
                    RemoveAdsInlineCta(
                        placement = "settings_membership_banner",
                        modifier = Modifier.align(Alignment.End),
                        onClick = onNavigatePremium
                    )
                }
            }
            if (!isProActive && nativeConfiguration.enabled) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                NativeAdCard(
                    configuration = nativeConfiguration,
                    isProActive = isProActive,
                    canRequestAds = canRequestAds,
                    placement = "settings_membership_native",
                    onAdLoaded = { nativeLoaded = true }
                )
                if (nativeLoaded) {
                    RemoveAdsInlineCta(
                        placement = "settings_membership_native",
                        modifier = Modifier.align(Alignment.End),
                        onClick = onNavigatePremium
                    )
                }
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Appearance
        // ---------------------------------------------------------------
        AppSectionTitle("Appearance")
        AppListCard(modifier = Modifier.appReveal(3)) {
            AppNavRow(
                title = "Theme",
                subtitle = "System, light, or dark appearance",
                value = themeMode.label,
                glyph = AppleGlyph.Moon,
                accent = AppTheme.colors.accent,
                onClick = onNavigateTheme
            )
            AppRowDivider()
            AppInfoRow(
                title = "Island style",
                value = "Classic"
            )
        }
        AppFootnote("Choose a fixed appearance or let the app follow your device setting.")

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Display behavior — persisted and consumed by the overlay service.
        // ---------------------------------------------------------------
        AppSectionTitle("Display behavior")
        AppListCard(modifier = Modifier.appReveal(3)) {
            AppToggleRow(
                title = "Always on top",
                subtitle = "Keep the island available above other apps",
                checked = alwaysOnTop,
                glyph = AppleGlyph.Notch,
                onCheckedChange = { value -> scope.launch { preferences.setAlwaysOnTop(value) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Show on lock screen",
                subtitle = "Allow the island to remain visible while locked",
                checked = showOnLockScreen,
                glyph = AppleGlyph.Lock,
                onCheckedChange = { value -> scope.launch { preferences.setShowOnLockScreen(value) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Smooth animations",
                subtitle = if (animationSpeedNormal) "Relaxed morphing and fades" else "Fast transitions",
                checked = animationSpeedNormal,
                glyph = AppleGlyph.Sparkles,
                onCheckedChange = { value -> scope.launch { preferences.setAnimationSpeedNormal(value) } }
            )
        }
        AppFootnote("Display behavior updates the overlay without changing the island’s audited geometry.")

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Language — lives on its own screen so it can be searched properly.
        // ---------------------------------------------------------------
        AppSectionTitle("Language")
        AppListCard(modifier = Modifier.appReveal(4)) {
            AppNavRow(
                title = "App language",
                subtitle = "Choose from ${AppLanguages.all.size} languages",
                value = AppLanguages.displayName(savedLanguage),
                glyph = AppleGlyph.Globe,
                accent = AppTheme.colors.accent,
                onClick = onNavigateLanguage
            )
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Permissions & support
        // ---------------------------------------------------------------
        AppSectionTitle("Support")
        AppListCard(modifier = Modifier.appReveal(5)) {
            AppNavRow(
                title = "Permissions",
                subtitle = "Review what the island can access",
                glyph = AppleGlyph.Shield,
                accent = AppTheme.colors.success,
                onClick = onNavigatePermissions
            )
            AppRowDivider()
            AppNavRow(
                title = "Rate the app",
                glyph = AppleGlyph.Star,
                accent = AppTheme.colors.gold,
                onClick = { showRating = true }
            )
            AppRowDivider()
            AppNavRow(
                title = "Send feedback",
                glyph = AppleGlyph.Bell,
                accent = AppTheme.colors.info,
                onClick = { showFeedback = true }
            )
            AppRowDivider()
            AppNavRow(
                title = "Component gallery",
                subtitle = "Preview the shared controls and tokens",
                glyph = AppleGlyph.Sparkles,
                accent = AppTheme.colors.accent,
                onClick = onNavigateGallery
            )
            AppRowDivider()
            AppNavRow(
                title = "Share the app",
                subtitle = "Invite someone to try Aurora Dynamic Island",
                glyph = AppleGlyph.Link,
                accent = AppTheme.colors.info,
                onClick = { context.shareApp() }
            )
            AppRowDivider()
            AppNavRow(
                title = "Check for updates",
                glyph = AppleGlyph.Reset,
                accent = AppTheme.colors.accent,
                onClick = onCheckForUpdates
            )
        }

        AppSectionSpacer()

        AppSectionTitle("About")
        AppListCard(modifier = Modifier.appReveal(6)) {
            AppNavRow(
                title = "About Aurora",
                subtitle = "Device diagnostics, version and support",
                glyph = AppleGlyph.Info,
                accent = AppTheme.colors.accent,
                onClick = onNavigateAbout
            )
            AppRowDivider()
            AppInfoRow(title = "Version", value = "1.0.0")
            AppRowDivider()
            AppNavRow(
                title = "Privacy policy",
                subtitle = "How local notification and call data is handled",
                glyph = AppleGlyph.Shield,
                onClick = { showPrivacy = true }
            )
            AppRowDivider()
            AppNavRow(
                title = "Terms of service",
                subtitle = "Use of Aurora Dynamic Island",
                glyph = AppleGlyph.Info,
                onClick = { showTerms = true }
            )
        }
        AppFootnote("All notification and call data is processed on your device and never uploaded.")
    }

    if (showPaywall) {
        ProPaywallSheet(
            onDismiss = { showPaywall = false },
            onSelectPlan = { planId ->
                premiumMessage = when (val result = premiumGateway.startPurchase(planId)) {
                    is PremiumAccessResult.Unavailable -> result.message
                    is PremiumAccessResult.Started -> result.message
                }
                showPaywall = false
            }
        )
    }
    if (showFeedback) {
        FeedbackDialog(onDismiss = { showFeedback = false })
    }
    if (showRating) {
        RatingDialog(
            onDismiss = { showRating = false },
            onRateClick = {
                showRating = false
                openPlayStore(context)
            }
        )
    }
    if (showPrivacy) {
        LegalDocumentDialog(
            title = "Privacy policy",
            paragraphs = listOf(
                "Aurora reads only the notifications, contacts, phone state, and device signals required for the features you enable.",
                "Notification content, caller details, and call history stay on this device. They are not uploaded by the app.",
                "You can revoke any optional permission from Android Settings at any time."
            ),
            onDismiss = { showPrivacy = false }
        )
    }
    if (showTerms) {
        LegalDocumentDialog(
            title = "Terms of service",
            paragraphs = listOf(
                "Aurora Dynamic Island is a personal overlay utility. Use it only on devices and accounts you are authorized to manage.",
                "System controls are routed through Android permissions and settings. The app does not bypass platform restrictions.",
                "Premium, advertising, and release-provider integrations remain disabled until configured for a production release."
            ),
            onDismiss = { showTerms = false }
        )
    }
    if (showNoInternetDialog) {
        NoInternetDialog(
            isRetrying = isRefreshingRelease,
            onRetry = { releaseRefreshAttempt += 1 },
            onOpenNetworkSettings = {
                showNoInternetDialog = false
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                )
            },
            onDismiss = { showNoInternetDialog = false }
        )
    }
}

private fun android.content.Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Try Aurora Dynamic Island — a calm, local-first island for calls, media and notifications.\n" +
                "https://play.google.com/store/apps/details?id=$packageName"
        )
    }
    runCatching {
        startActivity(Intent.createChooser(shareIntent, "Share Aurora Dynamic Island"))
    }
}

private fun remainingHours(expiresAtMillis: Long): Long =
    ((expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L) / (60L * 60L * 1000L)).coerceAtLeast(1L)

@Composable
private fun PlanCard(
    plan: PricingPlan,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(shape)
            .background(
                if (selected) AppTheme.colors.accentGradient()
                else AppTheme.colors.surfaceGradient()
            )
            .border(
                0.5.dp,
                if (selected) AppTheme.colors.accent.copy(alpha = 0.5f) else AppTheme.colors.border,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)
    ) {
        AppText(
            text = plan.title,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
            maxLines = 1
        )
        AppText(
            text = plan.price,
            style = AppTheme.typography.h3,
            color = AppTheme.colors.textPrimary,
            maxLines = 1
        )
        Box(modifier = Modifier.heightIn(min = 18.dp), contentAlignment = Alignment.Center) {
            if (plan.badge != null) {
                AppText(
                    text = plan.badge,
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.accent,
                    maxLines = 1
                )
            }
        }
    }
}
