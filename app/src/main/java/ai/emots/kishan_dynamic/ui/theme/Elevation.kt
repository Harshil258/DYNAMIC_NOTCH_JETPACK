package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AuroraElevation(
    val cardElevation: Dp = 0.dp,
    val modalElevation: Dp = 16.dp,
    val islandElevation: Dp = 12.dp,
    
    // Ambient Glow Intensities
    val glowBlurSubtle: Dp = 16.dp,
    val glowBlurMedium: Dp = 32.dp,
    val glowBlurProminent: Dp = 64.dp,
    
    // Hairline Specular Borders
    val hairlineBorder: Dp = 1.dp,
    val activeBorder: Dp = 1.5.dp
)

val LocalAuroraElevation = staticCompositionLocalOf { AuroraElevation() }
