package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AuroraBadge
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.ButtonVariant
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * Critical Force Update Dialog (Non-dismissible).
 */
@Composable
fun ForceUpdateDialog(
    onUpdateClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rocket_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = { /* Force update is non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
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
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AuroraTheme.colors.primary, AuroraTheme.colors.cyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    AuroraBadge(text = "CRITICAL UPDATE", backgroundColor = AuroraTheme.colors.error)

                    Text(
                        text = "Update Required",
                        style = AuroraTheme.typography.headlineMedium,
                        color = AuroraTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "A required performance update for Android 15 and system overlays is available. Please update to continue using Aurora Island.",
                        style = AuroraTheme.typography.bodySmall,
                        color = AuroraTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))

                    AuroraButton(
                        onClick = onUpdateClick,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = Icons.Rounded.RocketLaunch
                    ) {
                        Text(text = "Update via Google Play", style = AuroraTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Soft Update Dialog (Dismissible).
 */
@Composable
fun SoftUpdateDialog(
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuroraBadge(text = "NEW VERSION", backgroundColor = AuroraTheme.colors.primary)
                        AuroraIconButton(icon = Icons.Rounded.Close, onClick = onDismiss, size = 32.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AuroraTheme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SystemUpdate,
                            contentDescription = null,
                            tint = AuroraTheme.colors.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text = "New Update Available",
                        style = AuroraTheme.typography.headlineSmall,
                        color = AuroraTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Experience smoother spring physics, new equalizer animations, and enhanced battery efficiency.",
                        style = AuroraTheme.typography.bodySmall,
                        color = AuroraTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                    ) {
                        AuroraButton(
                            onClick = onDismiss,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Maybe Later", style = AuroraTheme.typography.labelMedium, color = AuroraTheme.colors.textSecondary)
                        }

                        AuroraButton(
                            onClick = onUpdateClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Update Now", style = AuroraTheme.typography.labelMedium, color = Color.White)
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

@Preview(name = "Force Update - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ForceUpdateDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        ForceUpdateDialog(onUpdateClick = {})
    }
}

@Preview(name = "Soft Update - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun SoftUpdateDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        SoftUpdateDialog(onUpdateClick = {}, onDismiss = {})
    }
}
