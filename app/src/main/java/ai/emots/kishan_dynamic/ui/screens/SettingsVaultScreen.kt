package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.dialogs.FeedbackDialog
import ai.emots.kishan_dynamic.ui.dialogs.ProPaywallSheet
import ai.emots.kishan_dynamic.ui.dialogs.RatingDialog
import ai.emots.kishan_dynamic.ui.dialogs.SoftUpdateDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme

data class PricingPlanItem(
    val id: String,
    val title: String,
    val price: String,
    val badge: String? = null
)

/**
 * Settings screen redesigned from first principles.
 * Minimal, spatial, editorial, and calm.
 */
@Composable
fun SettingsVaultScreen(
    onBack: (() -> Unit)? = null
) {
    var isProActive by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedPlanIndex by remember { mutableIntStateOf(2) } // Yearly default
    var selectedLanguageCode by remember { mutableStateOf("en") }

    val languages = remember {
        listOf(
            "en" to ("🇺🇸" to "English"),
            "hi" to ("🇮🇳" to "हिंदी"),
            "es" to ("🇪🇸" to "Español"),
            "pt" to ("🇧🇷" to "Português"),
            "ar" to ("🇸🇦" to "العربية"),
            "zh" to ("🇨🇳" to "中文"),
            "ja" to ("🇯🇵" to "日本語"),
            "fr" to ("🇫🇷" to "Français")
        )
    }

    val plans = remember {
        listOf(
            PricingPlanItem("weekly", "Weekly", "₹39/wk"),
            PricingPlanItem("monthly", "Monthly", "₹59/mo"),
            PricingPlanItem("yearly", "Yearly", "₹199/yr", "70% OFF"),
            PricingPlanItem("lifetime", "Lifetime", "₹499", "Best Value")
        )
    }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(bottom = AppTheme.layout.bottomBarHeight + 48.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // =================================================================
            // HEADER
            // =================================================================
            AppText(
                text = "Settings",
                style = AppTheme.typography.h1,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            AppText(
                text = "Preferences, membership & system diagnostics.",
                style = AppTheme.typography.bodySmall,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 1. MEMBERSHIP
            // =================================================================
            AppText(
                text = "Membership",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF636366),
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    AppText(
                        text = if (isProActive) "Dynamic Island Pro" else "Unlock Pro Access",
                        style = AppTheme.typography.body,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    AppText(
                        text = "Permanent ad-free & full experience controls",
                        style = AppTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isProActive) Color(0xFF1C2B1F) else Color(0xFF1C1C1E))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    AppText(
                        text = if (isProActive) "Active" else "Upgrade",
                        style = AppTheme.typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (isProActive) AppTheme.colors.success else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Minimal Plan Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plans.forEachIndexed { index, plan ->
                    val isSelected = selectedPlanIndex == index
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF1C1C20) else Color(0xFF0F0F12))
                            .border(
                                0.5.dp,
                                if (isSelected) Color(0x30FFFFFF) else Color(0x0CFFFFFF),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                selectedPlanIndex = index
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (plan.badge != null) {
                                AppText(
                                    text = plan.badge,
                                    style = AppTheme.typography.islandBadge,
                                    color = if (isSelected) Color.White else Color(0xFF636366)
                                )
                            }
                            AppText(
                                text = plan.title,
                                style = AppTheme.typography.caption,
                                color = Color(0xFF8E8E93)
                            )
                            AppText(
                                text = plan.price,
                                style = AppTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { showPaywallDialog = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "Subscribe via Play",
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { isProActive = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "Watch Ad (Pass)",
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 2. APPEARANCE
            // =================================================================
            AppText(
                text = "Atmosphere",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF636366),
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    AppText(
                        text = "Midnight Spatial Aura",
                        style = AppTheme.typography.body,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    AppText(
                        text = "Pure black OLED canvas with bottom atmospheric glow",
                        style = AppTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }

                AppText(
                    text = "Active",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.success
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 3. LANGUAGE
            // =================================================================
            AppText(
                text = "Language",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF636366),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            languages.forEachIndexed { index, (code, flagAndName) ->
                val isSelected = selectedLanguageCode == code
                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { selectedLanguageCode = code }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppText(text = flagAndName.first, style = AppTheme.typography.body)
                        AppText(
                            text = flagAndName.second,
                            style = AppTheme.typography.body,
                            color = if (isSelected) Color.White else Color(0xFF8E8E93),
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    if (isSelected) {
                        AppText(
                            text = "✓",
                            style = AppTheme.typography.body,
                            color = Color.White
                        )
                    }
                }

                if (index < languages.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0x0EFFFFFF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 4. DIAGNOSTICS & FEEDBACK
            // =================================================================
            AppText(
                text = "Support",
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF636366),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsActionRow(title = "Rate on Play Store", onClick = { showRatingDialog = true })
            SettingsActionDivider()
            SettingsActionRow(title = "Send Feedback & Logs", onClick = { showFeedbackDialog = true })
            SettingsActionDivider()
            SettingsActionRow(title = "Check for Updates", onClick = { showUpdateDialog = true })
            SettingsActionDivider()
            SettingsActionRow(title = "Privacy Policy & Terms", onClick = {})
        }

        if (showPaywallDialog) {
            ProPaywallSheet(
                onDismiss = { showPaywallDialog = false },
                onSelectPlan = {
                    isProActive = true
                    showPaywallDialog = false
                }
            )
        }
        if (showFeedbackDialog) {
            FeedbackDialog(onDismiss = { showFeedbackDialog = false })
        }
        if (showRatingDialog) {
            RatingDialog(
                onDismiss = { showRatingDialog = false },
                onRateClick = { showRatingDialog = false }
            )
        }
        if (showUpdateDialog) {
            SoftUpdateDialog(
                onUpdateClick = { showUpdateDialog = false },
                onDismiss = { showUpdateDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsActionRow(title: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = title,
            style = AppTheme.typography.body,
            color = Color.White
        )
        AppText(
            text = "→",
            style = AppTheme.typography.body,
            color = Color(0xFF48484A)
        )
    }
}

@Composable
private fun SettingsActionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0x0EFFFFFF))
    )
}
