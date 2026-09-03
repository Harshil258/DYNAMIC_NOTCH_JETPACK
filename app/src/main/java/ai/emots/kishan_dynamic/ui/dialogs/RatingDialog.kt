package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRateClick: (Int) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }

    AppDialog(
        onDismiss = onDismiss,
        title = "Enjoying the app?",
        subtitle = "Your review helps us keep improving.",
        glyph = AppleGlyph.Star,
        accent = AppTheme.colors.gold
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (star in 1..5) {
                val isSelected = star <= selectedStars
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f),
                    label = "star_scale"
                )
                AppleIcon(
                    glyph = AppleGlyph.Star,
                    tint = if (isSelected) AppTheme.colors.gold else AppTheme.colors.disabled,
                    size = 34.dp,
                    modifier = Modifier
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedStars = star }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        AppButton(
            text = "Rate on Play Store",
            glyph = AppleGlyph.Star,
            onClick = { onRateClick(selectedStars) }
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Not now",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}
