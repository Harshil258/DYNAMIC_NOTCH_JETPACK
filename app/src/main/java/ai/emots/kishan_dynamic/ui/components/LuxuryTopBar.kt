package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/** Compact editorial navigation header with a 48dp accessible back target. */
@Composable
fun LuxuryTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "back_press")

    Row(
        modifier = modifier.fillMaxWidth().padding(top = AppTheme.spacing.md, bottom = AppTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color(0xE61A1B1F))
                .border(0.75.dp, Color(0x28FFFFFF), CircleShape)
                .clickable(source, indication = null, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(AppleGlyph.ChevronLeft, tint = AppTheme.colors.textPrimary, size = 19.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            AppText(title, style = AppTheme.typography.h2, color = AppTheme.colors.textPrimary, maxLines = 1)
            AppText(subtitle, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary, maxLines = 2)
        }
        trailingContent?.invoke()
    }
}
