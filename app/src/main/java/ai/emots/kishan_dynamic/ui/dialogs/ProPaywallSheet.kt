package ai.emots.kishan_dynamic.ui.dialogs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AuroraBadge
import ai.emots.kishan_dynamic.ui.components.AuroraBottomSheet
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.ButtonVariant
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.components.GlowCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * Holographic Crystal Glass Paywall Sheet.
 */
@Composable
fun ProPaywallSheet(
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    var selectedPlanIndex by remember { mutableIntStateOf(2) } // Yearly

    val plans = listOf(
        Triple("Weekly", "₹39/wk", null),
        Triple("Monthly", "₹59/mo", null),
        Triple("Yearly", "₹99/yr", "MOST POPULAR"),
        Triple("Lifetime", "₹149", "SUPER SAVER")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraTheme.spacing.md),
            contentAlignment = Alignment.BottomCenter
        ) {
            AuroraBottomSheet {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuroraBadge(text = "AURORA PRO", backgroundColor = AuroraTheme.colors.primary)
                        AuroraIconButton(icon = Icons.Rounded.Close, onClick = onDismiss, size = 32.dp)
                    }

                    // Title
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Elevate Your Dynamic Notch",
                            style = AuroraTheme.typography.headlineMedium,
                            color = AuroraTheme.colors.textPrimary
                        )
                        Text(
                            text = "Uncompromised performance, zero ads & unlimited customization",
                            style = AuroraTheme.typography.bodySmall,
                            color = AuroraTheme.colors.textSecondary
                        )
                    }

                    // 4 Key Features Showcase
                    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = AuroraTheme.spacing.sm) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FeatureRow(icon = Icons.Rounded.Block, text = "100% Ad-Free Experience")
                            FeatureRow(icon = Icons.Rounded.Palette, text = "Custom Island Skins & Aurora Glow")
                            FeatureRow(icon = Icons.Rounded.GraphicEq, text = "All Waveform & Visualizer Palettes")
                            FeatureRow(icon = Icons.Rounded.AutoAwesome, text = "Priority Updates & Early Access")
                        }
                    }

                    // 2x2 Plan Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        plans.chunked(2).forEachIndexed { rowIndex, rowPlans ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPlans.forEachIndexed { colIndex, planData ->
                                    val planIndex = rowIndex * 2 + colIndex
                                    val isSelected = selectedPlanIndex == planIndex

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(AuroraTheme.shapes.cardSmall)
                                            .background(
                                                if (isSelected) AuroraTheme.colors.primary.copy(alpha = 0.25f)
                                                else AuroraTheme.colors.glassSurfaceSubtle
                                            )
                                            .border(
                                                width = if (isSelected) AuroraTheme.elevation.activeBorder else AuroraTheme.elevation.hairlineBorder,
                                                brush = if (isSelected) AuroraTheme.colors.brandGradientBrush else AuroraTheme.colors.glassBorderBrush,
                                                shape = AuroraTheme.shapes.cardSmall
                                            )
                                            .clickable { selectedPlanIndex = planIndex }
                                            .padding(AuroraTheme.spacing.sm),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            planData.third?.let { badge ->
                                                AuroraBadge(text = badge)
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Text(text = planData.first, style = AuroraTheme.typography.labelSmall, color = AuroraTheme.colors.textSecondary)
                                            Text(text = planData.second, style = AuroraTheme.typography.titleMedium, color = AuroraTheme.colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Terms Summary
                    Text(
                        text = "Cancel anytime via Google Play Subscriptions. One-tap restoration.",
                        style = AuroraTheme.typography.labelSmall,
                        color = AuroraTheme.colors.textTertiary
                    )

                    // CTA
                    AuroraButton(
                        onClick = { onSelectPlan(plans[selectedPlanIndex].first) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Continue with ${plans[selectedPlanIndex].first} (${plans[selectedPlanIndex].second})",
                            style = AuroraTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AuroraTheme.colors.primary, modifier = Modifier.size(16.dp))
        Text(text = text, style = AuroraTheme.typography.bodySmall, color = AuroraTheme.colors.textPrimary)
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Paywall Sheet - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun PaywallSheetDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        ProPaywallSheet(onDismiss = {}, onSelectPlan = {})
    }
}
