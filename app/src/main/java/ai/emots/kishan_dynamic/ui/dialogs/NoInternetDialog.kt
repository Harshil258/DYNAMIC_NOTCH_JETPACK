package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient

/**
 * Network recovery dialog for configured release services.
 *
 * This is intentionally an app-owned recovery surface rather than a copy of
 * the reference implementation: it uses the shared token system, a compact
 * animated signal mark, and leaves the current last-good app configuration
 * usable while the user decides what to do.
 */
@Composable
fun NoInternetDialog(
    isRetrying: Boolean,
    onRetry: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "network_recovery")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "network_recovery_pulse"
    )

    AppDialog(
        onDismiss = onDismiss,
        title = "Connection unavailable",
        subtitle = "The island is still available. We could not refresh release settings right now.",
        glyph = AppleGlyph.Wifi,
        accent = AppTheme.colors.warning
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .scale(pulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppTheme.colors.warning.copy(alpha = 0.32f),
                                AppTheme.colors.warning.copy(alpha = 0.08f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Wifi,
                    tint = AppTheme.colors.warning,
                    size = 34.dp
                )
            }

            AppText(
                text = "Check your connection, then try again. Your last working settings are being kept safely on this device.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            AppButton(
                text = if (isRetrying) "Trying again…" else "Try again",
                glyph = AppleGlyph.Reset,
                enabled = !isRetrying,
                onClick = onRetry
            )
            AppButton(
                text = "Open network settings",
                glyph = AppleGlyph.Settings,
                style = AppButtonStyle.Secondary,
                enabled = !isRetrying,
                onClick = onOpenNetworkSettings
            )
            AppButton(
                text = "Continue offline",
                style = AppButtonStyle.Tonal,
                enabled = !isRetrying,
                onClick = onDismiss
            )
        }
    }
}
