package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.premium.PlayBillingPremiumAccessGateway
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppPlanSelectorRow
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

@Composable
fun ProPaywallSheet(
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    val context = LocalContext.current
    val billingGateway = remember { PlayBillingPremiumAccessGateway(context) }
    val plans by billingGateway.plans.collectAsState()
    var selectedPlanIndex by remember { mutableIntStateOf(2) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(true)
        onDispose {
            ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(false)
            billingGateway.close()
        }
    }

    val benefits = listOf(
        "100% ad-free experience across the entire app",
        "Every island style, animation preset, and theme",
        "Unlimited custom Action Island shortcuts & contacts",
        "Fine-grained notch calibration & custom display duration"
    )

    AppSheet(
        onDismiss = onDismiss,
        title = "Unlock Aurora Pro",
        subtitle = "Fluid, distraction-free Dynamic Island utility.",
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
                AppPlanSelectorRow(
                    plan = plan,
                    isSelected = selectedPlanIndex == index,
                    onClick = { selectedPlanIndex = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        val selectedPlan = plans.getOrNull(selectedPlanIndex) ?: plans.firstOrNull()
        if (selectedPlan != null) {
            AppButton(
                text = "Continue with ${selectedPlan.title}",
                glyph = AppleGlyph.Crown,
                style = AppButtonStyle.AuroraPro,
                onClick = { onSelectPlan(selectedPlan.id) }
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Maybe later",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        AppText(
            text = "Purchases are securely processed by Google Play. Cancel anytime in Google Play Subscriptions.",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textTertiary
        )
    }
}
