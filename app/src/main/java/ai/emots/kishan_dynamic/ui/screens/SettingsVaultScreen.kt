package ai.emots.kishan_dynamic.ui.screens

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
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.dialogs.FeedbackDialog
import ai.emots.kishan_dynamic.ui.dialogs.ProPaywallSheet
import ai.emots.kishan_dynamic.ui.dialogs.RatingDialog
import ai.emots.kishan_dynamic.ui.dialogs.SoftUpdateDialog
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
import ai.emots.kishan_dynamic.ui.theme.AppTheme
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
    onNavigatePermissions: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val savedLanguage by preferences.languageCode.collectAsState(initial = "en")

    var showPaywall by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showRating by remember { mutableStateOf(false) }
    var showUpdate by remember { mutableStateOf(false) }
    var selectedPlanIndex by remember { mutableIntStateOf(2) }

    val languages = remember {
        listOf(
            "en" to "English",
            "hi" to "हिंदी",
            "es" to "Español",
            "pt" to "Português",
            "ar" to "العربية",
            "zh" to "中文",
            "ja" to "日本語",
            "fr" to "Français"
        )
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
            subtitle = "Membership, language and support."
        )

        // ---------------------------------------------------------------
        // Membership
        // ---------------------------------------------------------------
        AppSectionTitle("Membership")
        AppCard {
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
                    text = if (isProActive) "Active" else "Free",
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
                    onClick = { showPaywall = true },
                    glyph = AppleGlyph.Crown
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                AppButton(
                    text = "Watch an ad for a day pass",
                    onClick = { scope.launch { preferences.setProActive(true) } },
                    style = AppButtonStyle.Secondary
                )
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Appearance
        // ---------------------------------------------------------------
        AppSectionTitle("Appearance")
        AppListCard {
            AppInfoRow(
                title = "Theme",
                value = "Follows system",
            )
            AppRowDivider()
            AppInfoRow(
                title = "Island style",
                value = "Classic"
            )
        }
        AppFootnote("The island automatically matches your system light or dark appearance.")

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Language
        // ---------------------------------------------------------------
        AppSectionTitle("Language")
        AppListCard {
            languages.forEachIndexed { index, (code, name) ->
                LanguageRow(
                    name = name,
                    selected = savedLanguage == code,
                    onClick = { scope.launch { preferences.setLanguageCode(code) } }
                )
                if (index < languages.lastIndex) AppRowDivider()
            }
        }

        AppSectionSpacer()

        // ---------------------------------------------------------------
        // Permissions & support
        // ---------------------------------------------------------------
        AppSectionTitle("Support")
        AppListCard {
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
                title = "Check for updates",
                glyph = AppleGlyph.Reset,
                accent = AppTheme.colors.accent,
                onClick = { showUpdate = true }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("About")
        AppListCard {
            AppInfoRow(title = "Version", value = "1.0.0")
            AppRowDivider()
            AppNavRow(title = "Privacy policy", onClick = {})
            AppRowDivider()
            AppNavRow(title = "Terms of service", onClick = {})
        }
        AppFootnote("All notification and call data is processed on your device and never uploaded.")
    }

    if (showPaywall) {
        ProPaywallSheet(
            onDismiss = { showPaywall = false },
            onSelectPlan = {
                scope.launch { preferences.setProActive(true) }
                showPaywall = false
            }
        )
    }
    if (showFeedback) {
        FeedbackDialog(onDismiss = { showFeedback = false })
    }
    if (showRating) {
        RatingDialog(onDismiss = { showRating = false }, onRateClick = { showRating = false })
    }
    if (showUpdate) {
        SoftUpdateDialog(
            onUpdateClick = { showUpdate = false },
            onDismiss = { showUpdate = false }
        )
    }
}

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
                if (selected) AppTheme.colors.accent.copy(alpha = 0.14f)
                else AppTheme.colors.surfaceVariant
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

@Composable
private fun LanguageRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .heightIn(min = 52.dp)
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = name,
            style = AppTheme.typography.body,
            color = if (selected) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            ai.emots.kishan_dynamic.ui.components.AppleIcon(
                glyph = AppleGlyph.Check,
                tint = AppTheme.colors.accent,
                size = 16.dp
            )
        }
    }
}
