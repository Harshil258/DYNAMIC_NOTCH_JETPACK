package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/** Reference visual pill inside a real 48dp action target, entirely within the island. */
@Composable
internal fun IslandPillAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    background: Color = IslandColors.ButtonNeutral, textColor: Color = Color.White,
    visualHeight: Dp = AppTheme.island.ctaHeight) {
    Box(modifier.heightIn(min = AppTheme.island.minimumActionTarget)
        .clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().heightIn(min = visualHeight)
            .clip(RoundedCornerShape(visualHeight / 2)).background(background)
            .padding(horizontal = AppTheme.spacing.md), contentAlignment = Alignment.Center) {
            Text(text, color = textColor, style = AppTheme.typography.button,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
