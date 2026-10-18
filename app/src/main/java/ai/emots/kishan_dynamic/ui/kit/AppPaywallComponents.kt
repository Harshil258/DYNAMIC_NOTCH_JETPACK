package ai.emots.kishan_dynamic.ui.kit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.premium.FormattedPlanDetails
import ai.emots.kishan_dynamic.ui.components.AlbumArtThumb
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.WaveformAnimation
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.auroraActionGradient
import ai.emots.kishan_dynamic.ui.theme.auroraBadgeGradient
import ai.emots.kishan_dynamic.ui.theme.auroraBorderBrush
import ai.emots.kishan_dynamic.ui.theme.auroraHeroGradient
import ai.emots.kishan_dynamic.ui.theme.auroraProCardGradient

/**
 * World-class Aurora Pro Hero with an interactive Dynamic Island preview.
 * Allows prospective subscribers to tap and experience Pro states (Music, Calls, Action Island).
 */
@Composable
fun AuroraProHeroCard(
    isProActive: Boolean,
    proExpiresAt: Long?,
    modifier: Modifier = Modifier,
    isPaidPro: Boolean = false,
    isTrial: Boolean = false,
    trialDaysRemaining: Int = 0
) {
    var previewMode by remember { mutableIntStateOf(0) }
    val cardShape = RoundedCornerShape(AppTheme.radius.card)

    val statusPillText = when {
        isPaidPro -> "PRO ACTIVE"
        isTrial -> "TRIAL ACTIVE"
        isProActive -> "PASS ACTIVE"
        else -> "FULL EXPERIENCE"
    }
    val statusPillColor = when {
        isPaidPro -> AppTheme.colors.success
        isTrial -> AppTheme.colors.auroraCyan
        isProActive -> AppTheme.colors.auroraViolet
        else -> AppTheme.colors.auroraCyan
    }

    val heroTitle = when {
        isPaidPro -> "Aurora Pro is Active"
        isTrial -> "7-Day Free Trial Active"
        isProActive -> "Aurora Pro Pass Active"
        else -> "Unlock Aurora Pro"
    }

    val heroSubtitle = when {
        isPaidPro -> "Permanent access unlocked. All animations and styling enabled."
        isTrial -> if (trialDaysRemaining > 0) {
            "$trialDaysRemaining days remaining in trial · Unlock permanent access anytime."
        } else {
            "7-day trial active · Unlock permanent access anytime."
        }
        proExpiresAt != null -> "Pass active · ${remainingHours(proExpiresAt)}h remaining"
        isProActive -> "All premium animations and customization unlocked."
        else -> "Fluid iOS physics, live visualizers, and zero distractions on Android."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = AppTheme.elevation.card,
                shape = cardShape,
                ambientColor = AppTheme.colors.glowPrimary,
                spotColor = AppTheme.colors.glowSecondary
            )
            .clip(cardShape)
            .background(AppTheme.colors.auroraHeroGradient(), cardShape)
            .border(0.75.dp, AppTheme.colors.auroraSpecular, cardShape)
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Brand Badge and Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Aurora Pro Glowing Pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppTheme.colors.auroraBadgeGradient())
                    .border(0.5.dp, AppTheme.colors.auroraCyan.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = AppTheme.spacing.md, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                AppleIcon(glyph = AppleGlyph.Crown, tint = Color.White, size = 13.dp)
                AppText(
                    text = "AURORA PRO",
                    style = AppTheme.typography.badge,
                    color = Color.White
                )
            }

            AppStatusPill(
                text = statusPillText,
                color = statusPillColor,
                showDot = false
            )
        }

        // Title and Value Statement
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
            AppText(
                text = heroTitle,
                style = AppTheme.typography.h2,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            AppText(
                text = heroSubtitle,
                style = AppTheme.typography.bodySmall,
                color = Color(0xCCFFFFFF)
            )
        }

        // Live Interactive Dynamic Island Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.xl))
                .background(Color(0xFF06080F))
                .border(0.5.dp, Color(0x1FFFFFFF), RoundedCornerShape(AppTheme.radius.xl))
                .padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                // Mini iOS Status Bar
                IosStatusBar(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xs),
                    isDarkBackground = true
                )

                // Dynamic Island Capsule (Interactive Demo)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(0.5.dp, Color(0x33FFFFFF), CircleShape)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = previewMode,
                        transitionSpec = {
                            fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                        },
                        label = "island_preview"
                    ) { mode ->
                        when (mode) {
                            0 -> { // Music Mode
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AlbumArtThumb(seed = "AuroraProTrack", size = 22.dp)
                                    Column {
                                        AppText(
                                            text = "Heat Waves",
                                            style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        AppText(
                                            text = "Glass Animals",
                                            style = AppTheme.typography.caption.copy(fontSize = 9.5.sp),
                                            color = Color(0xAAFFFFFF),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    WaveformAnimation(
                                        gradient = listOf(AppTheme.colors.auroraCyan, AppTheme.colors.auroraViolet),
                                        modifier = Modifier.size(width = 16.dp, height = 12.dp)
                                    )
                                }
                            }
                            1 -> { // Call Mode
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF30D158)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 11.dp)
                                    }
                                    Column {
                                        AppText(
                                            text = "Sarah Jenkins",
                                            style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        AppText(
                                            text = "02:41 · Active Call",
                                            style = AppTheme.typography.caption.copy(fontSize = 9.5.sp),
                                            color = Color(0xAAFFFFFF),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    WaveformAnimation(
                                        gradient = listOf(Color(0xFF30D158), AppTheme.colors.auroraCyan),
                                        modifier = Modifier.size(width = 16.dp, height = 12.dp)
                                    )
                                }
                            }
                            else -> { // Action Island Mode
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AppTheme.colors.auroraViolet),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppleIcon(glyph = AppleGlyph.Sparkles, tint = Color.White, size = 11.dp)
                                    }
                                    AppText(
                                        text = "Action Island Pro",
                                        style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x28FFFFFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppleIcon(glyph = AppleGlyph.Torch, tint = Color.White, size = 9.dp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x28FFFFFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppleIcon(glyph = AppleGlyph.Camera, tint = Color.White, size = 9.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Demo Mode Switcher
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modes = listOf("Music", "Call", "Actions")
                    modes.forEachIndexed { index, label ->
                        val isSelected = previewMode == index
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) AppTheme.colors.auroraActionGradient()
                                    else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x1AFFFFFF)))
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 0.5.dp,
                                    color = if (isSelected) Color.Transparent else Color(0x22FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable { previewMode = index }
                                .padding(horizontal = AppTheme.spacing.md, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AppText(
                                text = label,
                                style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2x2 grid of value-driven benefit tiles communicating Aurora Pro capabilities.
 */
@Composable
fun AuroraBenefitTiles(
    modifier: Modifier = Modifier
) {
    val benefits = listOf(
        Triple(AppleGlyph.Palette, "Fluid Island Engine", "All 12+ live animation themes & fluid visualizers"),
        Triple(AppleGlyph.Shortcut, "Action Island Pro", "Unlimited customizable app shortcuts & quick contacts"),
        Triple(AppleGlyph.Controls, "Custom RGB & Styling", "Dynamic color picker, ambient glow & custom shapes"),
        Triple(AppleGlyph.Shield, "100% Ad-Free", "Pure distraction-free utility with zero advertisements")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            BenefitTile(
                glyph = benefits[0].first,
                title = benefits[0].second,
                subtitle = benefits[0].third,
                accent = AppTheme.colors.auroraCyan,
                modifier = Modifier.weight(1f)
            )
            BenefitTile(
                glyph = benefits[1].first,
                title = benefits[1].second,
                subtitle = benefits[1].third,
                accent = AppTheme.colors.auroraElectricBlue,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            BenefitTile(
                glyph = benefits[2].first,
                title = benefits[2].second,
                subtitle = benefits[2].third,
                accent = AppTheme.colors.auroraViolet,
                modifier = Modifier.weight(1f)
            )
            BenefitTile(
                glyph = benefits[3].first,
                title = benefits[3].second,
                subtitle = benefits[3].third,
                accent = AppTheme.colors.success,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BenefitTile(
    glyph: AppleGlyph,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    val tileBg = if (isDark) Color(0xFF101524) else AppTheme.colors.surfaceElevated
    val tileBorder = if (isDark) Color(0x22FFFFFF) else AppTheme.colors.border
    val titleColor = if (isDark) Color.White else AppTheme.colors.textPrimary
    val subtitleColor = if (isDark) Color(0xAAFFFFFF) else AppTheme.colors.textSecondary

    Column(
        modifier = modifier
            .clip(shape)
            .background(tileBg, shape)
            .border(0.5.dp, tileBorder, shape)
            .padding(AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (isDark) 0.16f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = glyph, tint = accent, size = 15.dp)
        }
        AppText(
            text = title,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = titleColor
        )
        AppText(
            text = subtitle,
            style = AppTheme.typography.caption.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = subtitleColor
        )
    }
}

/**
 * High-converting obsidian subscription plan card with luminous Aurora borders.
 */
@Composable
fun AppPlanSelectorRow(
    plan: FormattedPlanDetails,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(AppTheme.radius.xl)
    val contentDesc = "${plan.title} plan, ${plan.price}, ${plan.note}${if (isSelected) ", selected" else ""}"
    val cardBrush = AppTheme.colors.auroraProCardGradient(isSelected)
    val borderBrush = AppTheme.colors.auroraBorderBrush(isSelected)
    val titleColor = if (isDark) Color.White else AppTheme.colors.textPrimary
    val noteColor = if (isDark) Color(0xBBFFFFFF) else AppTheme.colors.textSecondary
    val priceColor = if (isSelected) {
        if (isDark) AppTheme.colors.auroraCyan else AppTheme.colors.auroraElectricBlue
    } else {
        if (isDark) Color.White else AppTheme.colors.textPrimary
    }
    val bestValueColor = if (isDark) Color(0x88FFFFFF) else AppTheme.colors.textTertiary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBrush, shape)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                brush = borderBrush,
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .heightIn(min = 68.dp)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)
            .semantics { contentDescription = contentDesc },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection radio indicator with Aurora gradient fill
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) AppTheme.colors.auroraActionGradient()
                    else Brush.linearGradient(
                        if (isDark) listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))
                        else listOf(Color(0x0C000000), Color(0x0C000000))
                    )
                )
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else (if (isDark) Color(0x38FFFFFF) else Color(0x22000000)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                AppleIcon(glyph = AppleGlyph.Check, tint = Color.White, size = 13.dp)
            }
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.md))

        // Title, savings badge, and billing frequency
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = plan.title,
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                if (plan.badge != null) {
                    Spacer(modifier = Modifier.width(AppTheme.spacing.xs))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AppTheme.colors.auroraBadgeGradient()
                                else Brush.linearGradient(
                                    listOf(
                                        AppTheme.colors.gold.copy(alpha = 0.16f),
                                        AppTheme.colors.gold.copy(alpha = 0.16f)
                                    )
                                )
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) AppTheme.colors.auroraCyan.copy(alpha = 0.6f) else AppTheme.colors.gold.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .padding(horizontal = AppTheme.spacing.sm, vertical = 2.dp)
                    ) {
                        AppText(
                            text = plan.badge.uppercase(),
                            style = AppTheme.typography.badge,
                            color = if (isSelected) Color.White else AppTheme.colors.gold,
                            maxLines = 1
                        )
                    }
                }
            }
            AppText(
                text = plan.note,
                style = AppTheme.typography.caption,
                color = noteColor
            )
            if (plan.freeTrialPeriod != null) {
                AppText(
                    text = plan.freeTrialPeriod,
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.success
                )
            }
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

        // Formatted localized price with distinct hierarchy
        Column(horizontalAlignment = Alignment.End) {
            AppText(
                text = plan.price,
                style = AppTheme.typography.h3,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )
            val priceSubtext = when (plan.id) {
                "weekly" -> "per week"
                "monthly" -> "per month"
                "yearly" -> "per year"
                "lifetime" -> "one-time"
                else -> ""
            }
            if (priceSubtext.isNotEmpty()) {
                AppText(
                    text = priceSubtext,
                    style = AppTheme.typography.caption.copy(fontSize = 10.sp),
                    color = bestValueColor
                )
            }
        }
    }
}

private data class ComparisonFeatureRow(
    val title: String,
    val freeValue: String?,
    val freeIncluded: Boolean,
    val proValue: String?,
    val proIncluded: Boolean
)

/**
 * Free vs Pro comparison matrix ensuring complete transparency before purchase.
 */
@Composable
fun AppFreeVsProComparisonCard(
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        ComparisonFeatureRow("Dynamic Island Core", null, true, null, true),
        ComparisonFeatureRow("Notification Alerts & Pager", null, true, null, true),
        ComparisonFeatureRow("Basic Music Controls", null, true, null, true),
        ComparisonFeatureRow("Island Themes & Waves", "1 Style", true, "All 12+", true),
        ComparisonFeatureRow("Notch Calibration", "Basic", true, "Sub-Pixel", true),
        ComparisonFeatureRow("Action Island Shortcuts", "3 Pinned", true, "Unlimited", true),
        ComparisonFeatureRow("Custom Alert Duration", "5s Standard", true, "3s – 15s", true),
        ComparisonFeatureRow("Zero Advertisements", null, false, "100% Ad-Free", true)
    )

    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = "Feature",
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.weight(1.8f)
                )
                AppText(
                    text = "Free",
                    style = AppTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.weight(1.2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppleIcon(glyph = AppleGlyph.Crown, tint = AppTheme.colors.auroraCyan, size = 12.dp)
                    AppText(
                        text = "Aurora Pro",
                        style = AppTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.auroraCyan
                    )
                }
            }

            AppRowDivider()

            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = row.title,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.weight(1.8f)
                    )
                    ComparisonStatusCell(
                        text = row.freeValue,
                        isIncluded = row.freeIncluded,
                        isPro = false,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonStatusCell(
                        text = row.proValue,
                        isIncluded = row.proIncluded,
                        isPro = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
                if (index < rows.lastIndex) {
                    AppRowDivider()
                }
            }
        }
    }
}

@Composable
private fun ComparisonStatusCell(
    text: String?,
    isIncluded: Boolean,
    isPro: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            text != null -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppTheme.radius.xs))
                        .background(
                            if (isPro) AppTheme.colors.auroraCyan.copy(alpha = 0.12f)
                            else AppTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    AppText(
                        text = text,
                        style = AppTheme.typography.caption.copy(fontSize = 10.5.sp),
                        fontWeight = if (isPro) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPro) AppTheme.colors.auroraCyan else AppTheme.colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
            isIncluded -> {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPro) AppTheme.colors.auroraCyan.copy(alpha = 0.16f)
                            else AppTheme.colors.textSecondary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Check,
                        tint = if (isPro) AppTheme.colors.auroraCyan else AppTheme.colors.textSecondary,
                        size = 11.dp
                    )
                }
            }
            else -> {
                AppText(
                    text = "—",
                    style = AppTheme.typography.caption.copy(fontSize = 13.sp),
                    color = AppTheme.colors.textTertiary
                )
            }
        }
    }
}

/**
 * Standard legal footnote and disclosure for Play Billing subscriptions.
 */
@Composable
fun AppSubscriptionDisclosures(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        AppText(
            text = "Subscriptions renew automatically unless canceled at least 24 hours before the end of the current billing cycle. You can manage or cancel your subscription at any time in Google Play Account Settings.",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
        AppText(
            text = "Purchases and entitlements are securely verified by Google Play. One-time passes and subscriptions unlock Aurora Pro immediately.",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
    }
}

private fun remainingHours(expiresAtMillis: Long): Long =
    ((expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L) / (60L * 60L * 1000L)).coerceAtLeast(1L)
