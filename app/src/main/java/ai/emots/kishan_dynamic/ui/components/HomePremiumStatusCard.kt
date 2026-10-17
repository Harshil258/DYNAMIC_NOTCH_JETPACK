package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ai.emots.kishan_dynamic.ui.theme.AppTheme

private object StatusCardColors {
    val gold = Color(0xFFF59E0B)
    val cyan = Color(0xFF06B6D4)
    val purple = Color(0xFF8B5CF6)
    val green = Color(0xFF10B981)
}

@Composable
fun HomePremiumStatusCard(
    snapshot: EntitlementSnapshot,
    onNavigateToPremium: () -> Unit,
    onOpenRewardDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else AppTheme.colors.textSecondary

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    if (snapshot.isPro) listOf(StatusCardColors.gold.copy(alpha = 0.5f), StatusCardColors.purple.copy(alpha = 0.4f))
                    else listOf(StatusCardColors.purple.copy(alpha = 0.25f), StatusCardColors.cyan.copy(alpha = 0.25f))
                ),
                shape = RoundedCornerShape(AppTheme.radius.xl)
            ),
        padding = AppTheme.spacing.lg
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .background(StatusCardColors.gold.copy(alpha = pulseGlow), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (snapshot.isPro) listOf(StatusCardColors.gold, StatusCardColors.purple)
                                    else listOf(StatusCardColors.purple, StatusCardColors.cyan)
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

                Spacer(modifier = Modifier.width(14.dp))

                // Center Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            color = textPrimary
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
                                        EntitlementSource.WEEKLY -> StatusCardColors.gold
                                        EntitlementSource.INSTALL_TRIAL -> StatusCardColors.cyan
                                        EntitlementSource.REWARDED_ACCESS -> StatusCardColors.green
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
                                color = if (snapshot.source == EntitlementSource.FREE) textSecondary else Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Watch Ad Pass Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(onClick = onOpenRewardDialog),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Play,
                                tint = StatusCardColors.cyan,
                                size = 14.dp
                            )
                            AppText(
                                text = "Watch Ad (+1 Day)",
                                style = AppTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                        }
                    }

                    // Upgrade to Pro Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(StatusCardColors.gold, StatusCardColors.purple)
                                )
                            )
                            .clickable(onClick = onNavigateToPremium),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Crown,
                                tint = Color.White,
                                size = 14.dp
                            )
                            AppText(
                                text = if (snapshot.isPro) "Keep Pro" else "Get Aurora Pro",
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
