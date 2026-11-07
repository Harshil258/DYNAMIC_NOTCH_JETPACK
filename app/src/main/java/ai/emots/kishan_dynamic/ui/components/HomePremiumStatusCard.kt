package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.premium.EntitlementSnapshot
import ai.emots.kishan_dynamic.data.premium.EntitlementSource
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Composable
fun HomePremiumStatusCard(
    snapshot: EntitlementSnapshot,
    onNavigateToPremium: () -> Unit,
    onOpenRewardDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = AppTheme.colors.textSecondary

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    if (snapshot.isPro) listOf(AppTheme.colors.gold.copy(alpha = 0.5f), AppTheme.colors.auroraViolet.copy(alpha = 0.4f))
                    else listOf(AppTheme.colors.auroraViolet.copy(alpha = 0.25f), AppTheme.colors.auroraCyan.copy(alpha = 0.25f))
                ),
                shape = RoundedCornerShape(AppTheme.radius.xl)
            ),
        padding = AppTheme.spacing.lg
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Top Row: Icon + Title + Plan Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Orb
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (snapshot.isPro) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .blur(12.dp)
                                .background(AppTheme.colors.gold.copy(alpha = 0.35f), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (snapshot.isPro) listOf(AppTheme.colors.gold, AppTheme.colors.auroraViolet)
                                    else listOf(AppTheme.colors.auroraViolet, AppTheme.colors.auroraCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (snapshot.isPro) AppleGlyph.Crown else AppleGlyph.Sparkles,
                            tint = Color.White,
                            size = 20.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppTheme.spacing.md))

                // Center Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        AppText(
                            text = when (snapshot.source) {
                                EntitlementSource.LIFETIME,
                                EntitlementSource.YEARLY,
                                EntitlementSource.MONTHLY,
                                EntitlementSource.WEEKLY -> "Aurora Pro Activated"
                                EntitlementSource.INSTALL_TRIAL -> "7-Day Free Trial"
                                EntitlementSource.REWARDED_ACCESS -> "Reward Pass Active"
                                EntitlementSource.FREE -> "Aurora Pro"
                            },
                            style = AppTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (snapshot.source) {
                                        EntitlementSource.LIFETIME,
                                        EntitlementSource.YEARLY,
                                        EntitlementSource.MONTHLY,
                                        EntitlementSource.WEEKLY -> AppTheme.colors.gold
                                        EntitlementSource.INSTALL_TRIAL -> AppTheme.colors.auroraCyan
                                        EntitlementSource.REWARDED_ACCESS -> AppTheme.colors.success
                                        EntitlementSource.FREE -> if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            AppText(
                                text = when (snapshot.source) {
                                    EntitlementSource.LIFETIME -> "LIFETIME"
                                    EntitlementSource.YEARLY -> "YEARLY"
                                    EntitlementSource.MONTHLY -> "MONTHLY"
                                    EntitlementSource.WEEKLY -> "WEEKLY"
                                    EntitlementSource.INSTALL_TRIAL -> "TRIAL"
                                    EntitlementSource.REWARDED_ACCESS -> "PASS"
                                    EntitlementSource.FREE -> "FREE"
                                },
                                style = AppTheme.typography.caption.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (snapshot.source == EntitlementSource.FREE) textSecondary else AppTheme.colors.auroraObsidian
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))

                    AppText(
                        text = when (snapshot.source) {
                            EntitlementSource.LIFETIME -> "Permanent Pro access · 100% ad-free island"
                            EntitlementSource.YEARLY,
                            EntitlementSource.MONTHLY,
                            EntitlementSource.WEEKLY -> "Subscription active · 100% ad-free island"
                            EntitlementSource.INSTALL_TRIAL -> "${snapshot.remainingDays} days remaining · All Pro features unlocked"
                            EntitlementSource.REWARDED_ACCESS -> "${snapshot.remainingDays} days remaining · Pro features active"
                            EntitlementSource.FREE -> "Unlock custom themes, sounds, notch calibration & ad-free island"
                        },
                        style = AppTheme.typography.caption,
                        color = textSecondary
                    )
                }
            }

            // Action Buttons
            if (snapshot.source != EntitlementSource.LIFETIME &&
                snapshot.source != EntitlementSource.YEARLY &&
                snapshot.source != EntitlementSource.MONTHLY &&
                snapshot.source != EntitlementSource.WEEKLY
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth < 360.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                            AppButton(
                                text = "Watch ad",
                                glyph = AppleGlyph.Play,
                                style = AppButtonStyle.Tonal,
                                onClick = onOpenRewardDialog
                            )
                            AppButton(
                                text = if (snapshot.isPro) "Keep Pro" else "Get Pro",
                                glyph = AppleGlyph.Crown,
                                style = AppButtonStyle.AuroraPro,
                                onClick = onNavigateToPremium
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                            AppButton(
                                text = "Watch ad",
                                glyph = AppleGlyph.Play,
                                style = AppButtonStyle.Tonal,
                                onClick = onOpenRewardDialog,
                                modifier = Modifier.weight(1f)
                            )
                            AppButton(
                                text = if (snapshot.isPro) "Keep Pro" else "Get Pro",
                                glyph = AppleGlyph.Crown,
                                style = AppButtonStyle.AuroraPro,
                                onClick = onNavigateToPremium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
