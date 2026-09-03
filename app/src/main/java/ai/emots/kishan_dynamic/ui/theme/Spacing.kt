package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AuroraSpacing(
    // 4pt Grid Units
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 40.dp,
    val epic: Dp = 48.dp,

    // Layout Specific Spaces
    val screenGutter: Dp = 16.dp,
    val cardPadding: Dp = 18.dp,
    val cardPaddingSmall: Dp = 12.dp,
    val sectionGap: Dp = 20.dp,
    val itemGap: Dp = 10.dp,
    
    // Component Touch Targets & Dimensions
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 52.dp,
    val buttonHeightSmall: Dp = 38.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    
    // Island Physical Tokens
    val islandCompactHeight: Dp = 36.dp,
    val islandCompactWidth: Dp = 130.dp,
    val islandMediaCompactWidth: Dp = 200.dp,
    val islandSplitGap: Dp = 8.dp,
    val islandSplitBubbleSize: Dp = 36.dp,
    val islandEdgeMargin: Dp = 10.dp
)

val LocalAuroraSpacing = staticCompositionLocalOf { AuroraSpacing() }
