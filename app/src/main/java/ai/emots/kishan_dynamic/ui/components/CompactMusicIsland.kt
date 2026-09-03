package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * COMPACT MUSIC — leading capsule.
 *
 * On iOS the compact media presentation puts the *album artwork* in the
 * leading capsule and the waveform in the trailing bubble. The capsule is
 * only ~81pt wide, so there is deliberately no text here: text belongs to
 * the expanded sheet.
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
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AlbumArtThumb(seed = title + artist)
    }
}

/** Tiny rounded album artwork derived from the track, so it feels real. */
@Composable
fun AlbumArtThumb(
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 25.dp
) {
    val palette = artworkPalette(seed)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(
            glyph = AppleGlyph.Music,
            tint = Color.White.copy(alpha = 0.92f),
            size = size * 0.52f
        )
    }
}

/**
 * COMPACT MUSIC — trailing bubble: the live waveform.
 */
@Composable
fun CompactMusicIslandSide(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        WaveformAnimation(modifier = Modifier.size(width = 17.dp, height = 14.dp))
    }
}

/** Deterministic two-colour gradient so a given track always looks the same. */
internal fun artworkPalette(seed: String): List<Color> {
    val palettes = listOf(
        listOf(Color(0xFFB18CFD), Color(0xFFF095FF)),
        listOf(Color(0xFFFF6F61), Color(0xFFFFB88C)),
        listOf(Color(0xFF3AC0F5), Color(0xFF5C7CFA)),
        listOf(Color(0xFF30D158), Color(0xFF0AC4B0)),
        listOf(Color(0xFFFF9F0A), Color(0xFFFFD60A)),
        listOf(Color(0xFFFF2D55), Color(0xFFFF7EB3))
    )
    val index = (seed.hashCode().let { if (it == Int.MIN_VALUE) 0 else it }).let {
        kotlin.math.abs(it) % palettes.size
    }
    return palettes[index]
}
