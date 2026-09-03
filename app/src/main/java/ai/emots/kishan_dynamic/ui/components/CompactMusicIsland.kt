package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact music main pill
 */
@Composable
fun CompactMusicIslandMain(
    title: String = "Heat Waves",
    artist: String = "Glass Animals",
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .basicMarquee()
            )
            Text(
                text = " • $artist",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isPlaying) {
            WaveformAnimation(modifier = Modifier.size(24.dp, 16.dp))
        } else {
            AppleIcon(
                glyph = AppleGlyph.Pause,
                tint = Color.White.copy(alpha = 0.6f),
                size = 14.dp
            )
        }
    }
}

/**
 * Compact music side bubble with album art thumbnail
 */
@Composable
fun CompactMusicIslandSide(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB18CFD),
                        Color(0xFFF095FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(
            glyph = AppleGlyph.Music,
            tint = Color.White,
            size = 18.dp
        )
    }
}
