package ai.emots.kishan_dynamic.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient
import kotlinx.coroutines.launch

private data class PremiumPlan(
    val id: String,
    val title: String,
    val price: String,
    val note: String,
    val badge: String? = null
)

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
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val proExpiresAt by preferences.proExpiresAt.collectAsState(initial = null)
    val billingStatus by billingGateway.status.collectAsState(initial = null)
    val rewardedStatus by rewardedGateway.status.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var selectedPlanIndex by remember { mutableIntStateOf(2) }
    var message by remember { mutableStateOf<String?>(null) }

    val plans = remember {
        listOf(
            PremiumPlan("weekly", "Weekly", "₹39", "Billed every week"),
            PremiumPlan("monthly", "Monthly", "₹59", "Billed every month"),
            PremiumPlan("yearly", "Yearly", "₹199", "Billed once a year", "Save 70%"),
            PremiumPlan("lifetime", "Lifetime", "₹499", "One-time payment", "Best value")
        )
    }
    val benefits = remember {
        listOf(
            "Every island state and interaction",
            "Unlimited Action Island shortcuts",
            "Theme, sound, haptic and display controls",
            "Ad-free experience and priority updates"
        )
    }

    DisposableEffect(billingGateway, rewardedGateway) {
        onDispose {
            billingGateway.close()
            rewardedGateway.close()
        }
    }
    LaunchedEffect(releaseConfig.adsEnabled) {
        if (releaseConfig.adsEnabled) activity?.let(consentGateway::request)
    }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Aurora Pro",
            subtitle = "The complete Dynamic Island experience",
            onBack = onBack
        )

        AppCard(modifier = Modifier.appReveal(1)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppTheme.radius.lg))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AppTheme.colors.accent.copy(alpha = 0.26f),
                                AppTheme.colors.gold.copy(alpha = 0.14f),
                                AppTheme.colors.surfaceElevated
                            )
                        )
                    )
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIconBadge()
                        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                        Column {
                            AppText(
                                text = if (isProActive) "Pro is active" else "Unlock Aurora Pro",
                                style = AppTheme.typography.h2,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            AppText(
                                text = when {
                                    proExpiresAt != null -> "Pass · ${remainingHours(proExpiresAt!!)}h remaining"
                                    isProActive -> "Full access is active"
                                    else -> "One calm home for every island state"
                                },
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                    AppStatusPill(
                        text = if (isProActive) "ACTIVE" else "PRO",
                        color = if (isProActive) AppTheme.colors.success else AppTheme.colors.gold,
                        showDot = false
                    )
                }
                AppText(
                    text = "Unlock the polished details that make Aurora feel like part of the phone—not another utility floating above it.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        AppSectionSpacer()
        AppSectionTitle("Included with Pro")
        AppListCard(modifier = Modifier.appReveal(2)) {
            benefits.forEachIndexed { index, benefit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 20.dp)
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    AppText(
                        text = benefit,
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.textPrimary
                    )
                }
                if (index < benefits.lastIndex) AppRowDivider()
            }
        }

        if (!isProActive) {
            AppSectionSpacer()
            AppSectionTitle("Choose your plan")
            plans.forEachIndexed { index, plan ->
                PremiumPlanRow(
                    plan = plan,
                    selected = selectedPlanIndex == index,
                    onClick = { selectedPlanIndex = index },
                    modifier = Modifier.appReveal(3 + index)
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            }
            AppButton(
                text = "Continue with ${plans[selectedPlanIndex].title}",
                glyph = AppleGlyph.Crown,
                onClick = {
                    message = when (val result = billingGateway.startPurchase(plans[selectedPlanIndex].id)) {
                        is PremiumAccessResult.Unavailable -> result.message
                        is PremiumAccessResult.Started -> result.message
                    }
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
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
        } else {
            AppCard(modifier = Modifier.appReveal(3)) {
                AppText(
                    text = "Your entitlement is active on this device. You can manage recurring plans in Google Play.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        AppFootnote("Purchases are verified by Google Play. Rewarded access is granted only after the ad provider confirms the reward.")
    }
}

@Composable
private fun AppIconBadge() {
    Row(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.gold.copy(alpha = 0.18f)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(glyph = AppleGlyph.Crown, tint = AppTheme.colors.gold, size = 22.dp)
    }
}

@Composable
private fun PremiumPlanRow(
    plan: PremiumPlan,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) AppTheme.colors.accentGradient() else AppTheme.colors.surfaceGradient())
            .border(
                if (selected) 1.dp else 0.5.dp,
                if (selected) AppTheme.colors.accent.copy(alpha = 0.6f) else AppTheme.colors.border,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(
            glyph = if (selected) AppleGlyph.Check else AppleGlyph.Star,
            tint = if (selected) Color.White else AppTheme.colors.textTertiary,
            size = 18.dp
        )
        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                plan.title,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else AppTheme.colors.textPrimary
            )
            AppText(
                plan.note,
                style = AppTheme.typography.caption,
                color = if (selected) Color.White.copy(alpha = 0.78f) else AppTheme.colors.textSecondary
            )
        }
        plan.badge?.let {
            AppStatusPill(text = it, color = AppTheme.colors.gold, showDot = false)
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        }
        AppText(
            plan.price,
            style = AppTheme.typography.h3,
            color = if (selected) Color.White else AppTheme.colors.textPrimary
        )
    }
}

private fun remainingHours(expiresAtMillis: Long): Long =
    ((expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L) / (60L * 60L * 1000L)).coerceAtLeast(1L)
