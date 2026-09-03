package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Silent / Ringer mode capsule island matching reference app
 */
@Composable
fun RingerModeIsland(
    isSilent: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            AppleIcon(
                glyph = if (isSilent) AppleGlyph.BellSlash else AppleGlyph.Bell,
                tint = if (isSilent) Color(0xFFFF453A) else Color.White,
                size = 28.dp
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppText(
                    text = if (isSilent) "SilentMode" else "Ringer",
                    style = AppTheme.typography.islandSubtitle,
                    color = Color.White.copy(alpha = 0.6f)
                )
                AppText(
                    text = if (isSilent) "On" else "Ring",
                    style = AppTheme.typography.islandTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF2C2C2E))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = if (isSilent) "Unmute" else "Silent",
                style = AppTheme.typography.button,
                color = Color.White
            )
        }
    }
}
