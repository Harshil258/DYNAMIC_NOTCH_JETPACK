package ai.emots.kishan_dynamic.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.AdConsentPolicy
import ai.emots.kishan_dynamic.data.premium.AppAdConsentRuntime
import ai.emots.kishan_dynamic.data.premium.MobileAdsRewardedPassGateway
import ai.emots.kishan_dynamic.data.premium.PlayBillingPremiumAccessGateway
import ai.emots.kishan_dynamic.data.premium.PremiumAccessResult
import ai.emots.kishan_dynamic.data.premium.RewardedAdConfiguration
import ai.emots.kishan_dynamic.data.premium.RewardedPassPolicy
import ai.emots.kishan_dynamic.data.premium.RewardedPassResult
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFreeVsProComparisonCard
import ai.emots.kishan_dynamic.ui.kit.AppPlanSelectorRow
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSubscriptionDisclosures
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.kit.AuroraBenefitTiles
import ai.emots.kishan_dynamic.ui.kit.AuroraProHeroCard
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val preferences = remember { AuroraPreferences(context) }
    val billingGateway = remember { PlayBillingPremiumAccessGateway(context) }
    val isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val releaseProvider = remember(isDebugBuild) {
        AppReleaseRuntimeConfig.provider(context, isDebugBuild)
    }
    val releaseConfig by releaseProvider.config.collectAsState()
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
        RewardedAdConfiguration.fromReleaseConfig(releaseConfig, canRequestAds)
    }
    val rewardedGateway = remember(rewardedConfiguration) {
        MobileAdsRewardedPassGateway(context, rewardedConfiguration)
    }

    val entitlementManager = remember { ai.emots.kishan_dynamic.data.premium.EntitlementManager.getInstance(context) }
    val entitlementSnapshot by entitlementManager.entitlement.collectAsState()
    val isPaidPro by preferences.isPaidPro.collectAsState(initial = false)
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val proExpiresAt by preferences.proExpiresAt.collectAsState(initial = null)
    val billingStatus by billingGateway.status.collectAsState(initial = null)
    val rewardedStatus by rewardedGateway.status.collectAsState(initial = null)
    val livePlans by billingGateway.plans.collectAsState()

    val scope = rememberCoroutineScope()
    var selectedPlanIndex by remember { mutableIntStateOf(2) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(billingGateway, rewardedGateway) {
        ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(true)
        onDispose {
            ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(false)
            billingGateway.close()
        }
    }

    LaunchedEffect(releaseConfig.adsEnabled) {
        if (releaseConfig.adsEnabled) activity?.let(consentGateway::request)
    }

    LaunchedEffect(Unit) {
        billingGateway.trackPaywallViewed("premium_screen")
    }

    LaunchedEffect(selectedPlanIndex, livePlans) {
        livePlans.getOrNull(selectedPlanIndex)?.let {
            billingGateway.trackPlanSelected(it)
        }
    }

        AppScreen(
            bottomInset = 0.dp,
            topBar = {
                AppTopBar(
                    title = "Aurora Pro",
                    subtitle = "The complete Dynamic Island experience",
                    onBack = onBack
                )
            },
            bottomAction = if (!isPaidPro) ({
                val selected = livePlans.getOrNull(selectedPlanIndex) ?: livePlans.firstOrNull()
                val isDark = AppTheme.isDark
                val summaryTextColor = if (isDark) androidx.compose.ui.graphics.Color.White else AppTheme.colors.textPrimary
                val noteTextColor = if (isDark) androidx.compose.ui.graphics.Color(0xAAFFFFFF) else AppTheme.colors.textSecondary
                val secureTextColor = if (isDark) androidx.compose.ui.graphics.Color(0x88FFFFFF) else AppTheme.colors.textTertiary

                if (selected != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            text = "${selected.title} Plan · ${selected.price}",
                            style = AppTheme.typography.caption.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = summaryTextColor
                        )
                        AppText(
                            text = selected.note,
                            style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                            color = noteTextColor
                        )
                    }
                    Spacer(Modifier.height(AppTheme.spacing.xs))
                    AppButton(
                        text = "Continue with ${selected.title}",
                        glyph = AppleGlyph.Crown,
                        style = AppButtonStyle.AuroraPro,
                        onClick = {
                            message = when (val result = billingGateway.startPurchase(selected.id)) {
                                is PremiumAccessResult.Unavailable -> result.message
                                is PremiumAccessResult.Started -> result.message
                            }
                        }
                    )
                    Spacer(Modifier.height(AppTheme.spacing.xxs))
                    AppText(
                        text = "Secured by Google Play · Cancel anytime in Play Store",
                        style = AppTheme.typography.caption.copy(fontSize = 10.5.sp),
                        color = secureTextColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    AppButton(
                        text = "Retry Google Play Billing",
                        glyph = AppleGlyph.Reset,
                        style = AppButtonStyle.Secondary,
                        onClick = {
                            scope.launch {
                                message = when (val result = billingGateway.restorePurchases()) {
                                    is PremiumAccessResult.Unavailable -> result.message
                                    is PremiumAccessResult.Started -> result.message
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(AppTheme.spacing.xxs))
                    AppText(
                        text = "Billing service connecting · Tap to retry",
                        style = AppTheme.typography.caption.copy(fontSize = 10.5.sp),
                        color = secureTextColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                message?.let {
                    Spacer(Modifier.height(AppTheme.spacing.xs))
                    AppText(it, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                }
            }) else null
        ) {

        // 1. Aurora Pro Hero Section with Live Dynamic Island Interactive Showcase
        AuroraProHeroCard(
            isProActive = isProActive,
            proExpiresAt = proExpiresAt,
            isPaidPro = isPaidPro,
            isTrial = entitlementSnapshot.isTrial,
            trialDaysRemaining = entitlementSnapshot.remainingDays,
            modifier = Modifier.appReveal(1)
        )

        // 2. Choose Your Plan Section — Elevated immediately above the fold for quick comparison
        if (!isPaidPro) {
            AppSectionSpacer()
            AppSectionTitle("Choose your plan")
            if (livePlans.isNotEmpty()) {
                livePlans.forEachIndexed { index, plan ->
                    AppPlanSelectorRow(
                        plan = plan,
                        isSelected = selectedPlanIndex == index,
                        onClick = { selectedPlanIndex = index },
                        modifier = Modifier.appReveal(2 + index)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                }
            } else {
                AppCard(modifier = Modifier.appReveal(2)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        AppText(
                            text = "Connecting to Google Play Store…",
                            style = AppTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        )
                        AppText(
                            text = "Real-time pricing is loading from Google Play Billing. If this takes longer than expected, tap below to retry.",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textSecondary
                        )
                        AppButton(
                            text = "Retry Google Play connection",
                            glyph = AppleGlyph.Reset,
                            style = AppButtonStyle.Secondary,
                            onClick = {
                                scope.launch {
                                    billingGateway.restorePurchases()
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            if (rewardedConfiguration.enabled) {
                AppButton(
                    text = "Watch an ad for a ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)} pass",
                    glyph = AppleGlyph.Play,
                    style = AppButtonStyle.Secondary,
                    onClick = {
                        message = when (val currentActivity = activity) {
                            null -> "Rewarded passes require an active app screen."
                            else -> when (val result = rewardedGateway.show(currentActivity)) {
                                is RewardedPassResult.Unavailable -> result.message
                                is RewardedPassResult.Started -> result.message
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            }

            AppButton(
                text = "Restore purchases",
                glyph = AppleGlyph.Reset,
                style = AppButtonStyle.Secondary,
                onClick = {
                    scope.launch {
                        message = when (val result = billingGateway.restorePurchases()) {
                            is PremiumAccessResult.Unavailable -> result.message
                            is PremiumAccessResult.Started -> result.message
                        }
                    }
                }
            )

            (message ?: billingStatus ?: rewardedStatus)?.let { status ->
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                AppText(status, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
            }
        }

        // 3. Value-Driven Benefit Tiles (2x2 Grid)
        AppSectionSpacer()
        AppSectionTitle("Included with Pro")
        AuroraBenefitTiles(
            modifier = Modifier.appReveal(livePlans.size + 3)
        )

        // 4. Free vs Pro Comparison Matrix
        AppSectionSpacer()
        AppSectionTitle("Free vs Pro Comparison")
        AppFreeVsProComparisonCard(modifier = Modifier.appReveal(livePlans.size + 4))

        // 5. Subscription Management (if user already active)
        if (isPaidPro) {
            AppSectionSpacer()
            AppSectionTitle("Subscription Management")
            AppCard(modifier = Modifier.appReveal(livePlans.size + 5)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    AppText(
                        text = "Your entitlement is active on this device. You can manage or cancel recurring plans in Google Play.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        AppButton(
                            text = "Manage in Google Play",
                            glyph = AppleGlyph.Settings,
                            style = AppButtonStyle.AuroraPro,
                            onClick = { billingGateway.openManageSubscription(context) },
                            modifier = Modifier.weight(1f)
                        )
                        AppButton(
                            text = "Restore",
                            glyph = AppleGlyph.Reset,
                            style = AppButtonStyle.Secondary,
                            onClick = { billingGateway.restorePurchases() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 6. Transparent Play Billing Disclosures & Legal Terms
        AppSectionSpacer()
        AppSubscriptionDisclosures()
    }
}
