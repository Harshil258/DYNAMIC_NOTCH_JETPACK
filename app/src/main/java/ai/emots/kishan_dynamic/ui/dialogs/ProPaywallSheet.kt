package ai.emots.kishan_dynamic.ui.dialogs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

private data class PaywallPlan(
    val id: String,
    val title: String,
    val price: String,
    val note: String,
    val badge: String? = null
)

@Composable
fun ProPaywallSheet(
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    var selectedPlanIndex by remember { mutableIntStateOf(2) }

    val plans = remember {
        listOf(
            PaywallPlan("weekly", "Weekly", "₹39", "Billed every week"),
            PaywallPlan("monthly", "Monthly", "₹59", "Billed every month"),
            PaywallPlan("yearly", "Yearly", "₹199", "Billed once a year", "Save 70%"),
            PaywallPlan("lifetime", "Lifetime", "₹499", "One-time payment", "Best value")
        )
    }

    val benefits = listOf(
        "No ads, anywhere",
        "Every island style and colour",
        "All visualiser palettes",
        "Priority updates and early access"
    )

    AppSheet(
        onDismiss = onDismiss,
        title = "Unlock Pro",
        subtitle = "Everything, with nothing in the way.",
        glyph = AppleGlyph.Crown,
        accent = AppTheme.colors.gold
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceGradient())
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.success.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 11.dp)
                    }
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    AppText(
                        text = benefit,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            plans.forEachIndexed { index, plan ->
                PlanRow(
                    plan = plan,
                    selected = selectedPlanIndex == index,
                    onClick = { selectedPlanIndex = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Continue with ${plans[selectedPlanIndex].title}",
            glyph = AppleGlyph.Crown,
            onClick = { onSelectPlan(plans[selectedPlanIndex].id) }
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Maybe later",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        AppText(
            text = "Cancel anytime in Google Play subscriptions.",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
    }
}

@Composable
private fun PlanRow(
    plan: PaywallPlan,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    Row(
        modifier = Modifier
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
            .heightIn(min = 60.dp)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (selected) AppTheme.colors.accent else AppTheme.colors.surfaceElevated
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                        AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.onAccent, size = 11.dp)
            }
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = plan.title,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary
            )
            AppText(
                text = plan.note,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary
            )
        }

        if (plan.badge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppTheme.colors.gold.copy(alpha = 0.16f))
                    .padding(horizontal = AppTheme.spacing.sm, vertical = 2.dp)
            ) {
                AppText(
                    text = plan.badge,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.gold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        }

        AppText(
            text = plan.price,
            style = AppTheme.typography.h3,
            color = AppTheme.colors.textPrimary
        )
    }
}
