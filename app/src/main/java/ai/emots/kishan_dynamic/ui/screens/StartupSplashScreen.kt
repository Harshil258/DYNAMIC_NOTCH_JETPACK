package ai.emots.kishan_dynamic.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppBackground
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/** Branded startup surface shown while persisted routing is being resolved. */
@Composable
fun StartupSplashScreen() {
    val transition = rememberInfiniteTransition(label = "startup_splash")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startup_splash_pulse"
    )

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(pulse)
                    .shadow(
                        elevation = AppTheme.elevation.raised,
                        shape = RoundedCornerShape(AppTheme.radius.xl),
                        ambientColor = AppTheme.colors.glowPrimary,
                        spotColor = AppTheme.colors.glowSecondary
                    )
                    .background(
                        brush = Brush.linearGradient(
                            listOf(AppTheme.colors.accent, AppTheme.colors.info)
                        ),
                        shape = RoundedCornerShape(AppTheme.radius.xl)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Notch,
                    tint = Color.White,
                    size = 42.dp
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppText(
                text = "Dynamic Island",
                style = AppTheme.typography.h1,
                color = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            AppText(
                text = "Your essentials, quietly in the moment.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = AppTheme.colors.accent,
                trackColor = AppTheme.colors.borderSubtle,
                strokeWidth = 2.dp
            )
        }
    }
}
