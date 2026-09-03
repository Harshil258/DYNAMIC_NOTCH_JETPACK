package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.ButtonVariant
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * In-App Rating & Review Dialog.
 */
@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRateClick: (Int) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraTheme.spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AuroraIconButton(icon = Icons.Rounded.Close, onClick = onDismiss, size = 32.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AuroraTheme.colors.warning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AuroraTheme.colors.warning,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Enjoying Aurora Island?",
                        style = AuroraTheme.typography.headlineSmall,
                        color = AuroraTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your 5-star review supports ongoing development and helps us deliver faster updates and new features.",
                        style = AuroraTheme.typography.bodySmall,
                        color = AuroraTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    // 5-Star Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            val isSelected = star <= selectedStars
                            val starScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.18f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.5f),
                                label = "star_scale"
                            )
                            Icon(
                                imageVector = if (isSelected) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Rate $star",
                                tint = if (isSelected) AuroraTheme.colors.warning else AuroraTheme.colors.textTertiary,
                                modifier = Modifier
                                    .size(38.dp)
                                    .scale(starScale)
                                    .clickable { selectedStars = star }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                    ) {
                        AuroraButton(
                            onClick = onDismiss,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Not Now", style = AuroraTheme.typography.labelMedium, color = AuroraTheme.colors.textSecondary)
                        }

                        AuroraButton(
                            onClick = { onRateClick(selectedStars) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Rate 5 Stars ⭐", style = AuroraTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Rating Dialog - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun RatingDialogDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        RatingDialog(onDismiss = {}, onRateClick = {})
    }
}
