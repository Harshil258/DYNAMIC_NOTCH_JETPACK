package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * RINGER HUD.
 *
 * On iOS this is one of the smallest island presentations: the ring/silent
 * glyph on the leading edge and a single word on the trailing edge, inside a
 * capsule barely wider than the idle pill. It never becomes a full sheet.
 */
@Composable
fun RingerModeIsland(
    isSilent: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accent = if (isSilent) Color(0xFFFF453A) else Color(0xFFFFFFFF)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppleIcon(
            glyph = if (isSilent) AppleGlyph.BellSlash else AppleGlyph.Bell,
            tint = accent,
            size = 17.dp
        )

        AppText(
            text = if (isSilent) "Silent" else "Ring",
            style = AppTheme.typography.islandSubtitle,
            color = accent.copy(alpha = 0.95f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
