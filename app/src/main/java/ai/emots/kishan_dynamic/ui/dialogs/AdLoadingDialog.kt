package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.animation.core.EaseInOutCubic
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Premium glass-styled loading dialog shown while preparing full-screen interstitial ads.
 * Conforms to Aurora design tokens with soft pulsing status dots and non-disruptive feedback.
 */
@Composable
fun AdLoadingDialog(
    isVisible: Boolean
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = { /* Modal locked during ad load */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 180.dp, max = 240.dp)
                .clip(RoundedCornerShape(AppTheme.radius.xl))
                .background(AppTheme.colors.surfaceElevated.copy(alpha = 0.96f))
                .border(
                    width = 0.5.dp,
                    color = AppTheme.colors.border,
                    shape = RoundedCornerShape(AppTheme.radius.xl)
                )
                .padding(horizontal = AppTheme.spacing.xl, vertical = AppTheme.spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PulsingDotsIndicator(
                    color = AppTheme.colors.accent,
                    modifier = Modifier.padding(vertical = AppTheme.spacing.xs)
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                AppText(
                    text = "Loading...",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PulsingDotsIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ad_loader_dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val delay = index * 160

            val scale by transition.animateFloat(
                initialValue = 0.65f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = delay,
                        easing = EaseInOutCubic
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )

            val alpha by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = delay,
                        easing = EaseInOutCubic
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_$index"
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
