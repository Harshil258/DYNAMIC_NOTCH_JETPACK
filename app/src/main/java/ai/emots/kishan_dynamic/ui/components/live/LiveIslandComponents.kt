package ai.emots.kishan_dynamic.ui.components.live

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import androidx.compose.material3.ripple

/**
 * Shared status badge for Live Islands (e.g. "ON THE WAY", "ON TIME", "78' LIVE").
 */
@Composable
fun IslandStatusBadge(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 9.sp,
    showDot: Boolean = false,
    isPulsing: Boolean = false
) {
    val pulseAlpha = if (isPulsing && !LocalReducedMotion.current) {
        val transition = rememberInfiniteTransition(label = "badge_pulse")
        val alpha by transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
        alpha
    } else 1f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(tint.copy(alpha = 0.16f))
            .border(0.8.dp, tint.copy(alpha = 0.35f * pulseAlpha), RoundedCornerShape(percent = 50))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .clip(CircleShape)
                        .background(tint)
                )
            }
            Text(
                text = text.uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = tint,
                letterSpacing = 0.4.sp
            )
        }
    }
}

/**
 * Proportional progress bar for live activities with optional trailing head indicator.
 */
@Composable
fun IslandLiveProgressBar(
    progress: Float,
    tint: Color,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 4.dp,
    trackColor: Color = IslandColors.Gray4
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeProgress)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(tint.copy(alpha = 0.8f), tint)
                    )
                )
        )
    }
}

/**
 * Interactive action chip within expanded islands (e.g. Phone call, Directions, Pause).
 */
@Composable
fun IslandActionChip(
    glyph: AppleGlyph,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    iconSize: Dp = 15.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size.coerceAtLeast(AppTheme.island.minimumActionTarget))
            .semantics {
                role = Role.Button
                if (contentDescription != null) this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(size).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
            AppleIcon(glyph = glyph, tint = iconTint, size = iconSize)
        }
    }
}

/**
 * Pulsing live dot for sports and real-time activities.
 */
@Composable
fun IslandLivePulseIndicator(
    color: Color = IslandColors.Green,
    size: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    val scale = if (LocalReducedMotion.current) 1f else {
    val transition = rememberInfiniteTransition(label = "live_pulse")
    val animatedScale by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
        animatedScale
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(color)
    )
}

/** Identity retains its width when larger type makes a trailing status unsuitable. */
@Composable
internal fun IslandAdaptiveHeader(identity: @Composable () -> Unit, status: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 300.dp || LocalDensity.current.fontScale > 1.3f) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                identity()
                status()
            }
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Box(Modifier.weight(1f)) { identity() }
                status()
            }
        }
    }
}
