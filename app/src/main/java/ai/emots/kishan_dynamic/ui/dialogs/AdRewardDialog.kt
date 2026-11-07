package ai.emots.kishan_dynamic.ui.dialogs

import android.app.Activity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.data.premium.AdConsentPolicy
import ai.emots.kishan_dynamic.data.premium.AppAdConsentRuntime
import ai.emots.kishan_dynamic.data.premium.AdConsentStatus
import ai.emots.kishan_dynamic.data.premium.RewardedPassPolicy
import ai.emots.kishan_dynamic.data.premium.EntitlementManager
import ai.emots.kishan_dynamic.data.premium.MobileAdsRewardedPassGateway
import ai.emots.kishan_dynamic.data.premium.RewardedAdConfiguration
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun AdRewardDialog(
    onDismiss: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val entitlementManager = remember { EntitlementManager.getInstance(context) }
    val rewardedManager = entitlementManager.rewardedAccessManager

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
    LaunchedEffect(releaseConfig.adsEnabled) {
        if (releaseConfig.adsEnabled) activity?.let(consentGateway::request)
    }

    val snapshot by rewardedManager.rewardedSnapshotFlow.collectAsState(
        initial = ai.emots.kishan_dynamic.data.premium.RewardedAccessSnapshot()
    )

    var isAdLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRewardCelebration by remember { mutableStateOf(false) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    // Cooldown countdown loop
    LaunchedEffect(snapshot.cooldownRemainingSeconds) {
        cooldownSeconds = snapshot.cooldownRemainingSeconds
        while (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    // Auto-dismiss celebration after 4 seconds
    LaunchedEffect(showRewardCelebration) {
        if (showRewardCelebration) {
            delay(4000L)
            showRewardCelebration = false
        }
    }

    val isDark = AppTheme.isDark
    val cardBg = AppTheme.colors.surface
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = AppTheme.colors.textSecondary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .background(AppTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Dialog Body Card
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(0.92f)
                    .heightIn(max = maxHeight * 0.94f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(cardBg)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume clicks inside card
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row with Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Crown,
                            tint = AppTheme.colors.gold,
                            size = 18.dp
                        )
                        AppText(
                            text = "Reward Pass",
                            style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.gold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                            .clickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Close,
                            tint = textSecondary,
                            size = 14.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Sparkle / Crown Icon Badge
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .blur(18.dp)
                            .background(AppTheme.colors.auroraViolet.copy(alpha = 0.35f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Sparkles,
                            tint = Color.White,
                            size = 32.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AppText(
                    text = "Want more free premium days?",
                    style = AppTheme.typography.h2,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                AppText(
                    text = "Watch an ad to unlock ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)} of full Aurora Pro access. Stack days anytime!",
                    style = AppTheme.typography.bodySmall,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 7-DAY PROGRESS ROW
                if (snapshot.checkmarks.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        snapshot.checkmarks.forEachIndexed { index, (label, unlocked) ->
                            val isNext = !unlocked && snapshot.checkmarks.take(index).all { it.second }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .then(
                                            when {
                                                unlocked -> Modifier.background(
                                                    Brush.linearGradient(
                                                        listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                                    )
                                                )
                                                isNext -> Modifier
                                                    .background(
                                                        if (isDark) Color.White.copy(alpha = 0.08f)
                                                        else Color.Black.copy(alpha = 0.05f)
                                                    )
                                                    .border(
                                                        1.5.dp,
                                                        Brush.linearGradient(
                                                            listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                                        ),
                                                        CircleShape
                                                    )
                                                else -> Modifier.background(
                                                    if (isDark) Color.White.copy(alpha = 0.05f)
                                                    else Color.Black.copy(alpha = 0.04f)
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (unlocked) {
                                        AppleIcon(
                                            glyph = AppleGlyph.Check,
                                            tint = Color.White,
                                            size = 16.dp
                                        )
                                    } else {
                                        BasicText(
                                            text = "${index + 1}",
                                            style = TextStyle(
                                                color = if (isNext) AppTheme.colors.auroraCyan else textSecondary.copy(alpha = 0.6f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                BasicText(
                                    text = label,
                                    style = TextStyle(
                                        color = if (unlocked) AppTheme.colors.auroraViolet else textSecondary.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        fontWeight = if (unlocked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Total Free Premium Days Remaining Pill
                val remainingDays = snapshot.remainingDays
                val remainingText = if (remainingDays > 0) {
                    "$remainingDays ${if (remainingDays == 1) "Day" else "Days"}"
                } else {
                    "None"
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                            .border(
                                0.5.dp,
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppText(
                                text = "Total Free Premium:",
                                style = AppTheme.typography.caption,
                                color = textSecondary
                            )
                            AppText(
                                text = remainingText,
                                style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.auroraCyan
                            )
                        }
                    }

                    if (snapshot.extraDaysStacked > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppTheme.colors.auroraViolet.copy(alpha = 0.15f))
                                .border(
                                    0.5.dp,
                                    AppTheme.colors.auroraViolet.copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            AppText(
                                text = "+${snapshot.extraDaysStacked} stacked",
                                style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.auroraViolet
                            )
                        }
                    }
                }

                if (snapshot.isActive && snapshot.expiresAtMillis > 0L) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val expiryDateStr: String = remember(snapshot.expiresAtMillis) {
                        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(snapshot.expiresAtMillis))
                    }
                    AppText(
                        text = "Expires $expiryDateStr",
                        style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                        color = textSecondary.copy(alpha = 0.8f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppText(
                        text = errorMessage ?: "",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Watch Ad CTA Button
                val canWatch = cooldownSeconds <= 0 && !isAdLoading &&
                    rewardedConfiguration.enabled && canRequestAds && activity != null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .then(
                            if (canWatch) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                    )
                                )
                            } else {
                                Modifier.background(
                                    if (isDark) Color.White.copy(alpha = 0.12f)
                                    else Color.Black.copy(alpha = 0.08f)
                                )
                            }
                        )
                        .clickable(enabled = canWatch, role = Role.Button) {
                            if (activity == null || !canWatch) return@clickable
                            errorMessage = null
                            isAdLoading = true

                            rewardedGateway.show(
                                activity = activity,
                                onRewarded = {
                                    isAdLoading = false
                                    showRewardCelebration = true
                                    cooldownSeconds = releaseConfig.rewardedAdCooldownSeconds
                                },
                                onDismissed = {
                                    isAdLoading = false
                                },
                                onFailed = { err ->
                                    isAdLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isAdLoading -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        cooldownSeconds > 0 -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Timer,
                                tint = textSecondary,
                                size = 16.dp
                            )
                            AppText(
                                text = "Next ad in ${cooldownSeconds}s",
                                style = AppTheme.typography.button,
                                color = textSecondary
                            )
                        }
                        !rewardedConfiguration.enabled -> AppText(
                            text = "Rewarded passes are currently unavailable",
                            style = AppTheme.typography.button,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                        !canRequestAds -> AppText(
                            text = if (consentSnapshot.status == AdConsentStatus.LOADING) {
                                "Preparing ad consent…"
                            } else {
                                "Ad consent is required before a rewarded pass"
                            },
                            style = AppTheme.typography.caption,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                        else -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Play,
                                tint = Color.White,
                                size = 16.dp
                            )
                            AppText(
                                text = "Watch Ad (+1 Day)",
                                style = AppTheme.typography.button,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Get Pro Forever Text Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button) {
                            onDismiss()
                            onNavigateToPremium()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppText(
                        text = "Or unlock lifetime Pro without ads →",
                        style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.auroraViolet
                    )
                }
            }

            // Celebration Toast Overlay
            AnimatedVisibility(
                visible = showRewardCelebration,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.5.dp, AppTheme.colors.gold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppText(
                            text = "🎉 ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)} Pro Unlocked!",
                            style = AppTheme.typography.h3,
                            color = AppTheme.colors.gold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = "All features are active for ${RewardedPassPolicy.durationLabel(rewardedConfiguration.passHours)}.",
                            style = AppTheme.typography.bodySmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                    )
                                )
                                .heightIn(min = 48.dp)
                                .clickable(role = Role.Button) {
                                    onDismiss()
                                    onNavigateToPremium()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            AppText(
                                text = "Keep it forever →",
                                style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
