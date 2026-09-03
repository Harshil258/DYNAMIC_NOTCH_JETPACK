package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class AuroraShapes(
    val pill: CornerBasedShape = RoundedCornerShape(999.dp),
    val modal: CornerBasedShape = RoundedCornerShape(32.dp),
    val card: CornerBasedShape = RoundedCornerShape(24.dp),
    val cardSmall: CornerBasedShape = RoundedCornerShape(16.dp),
    val controlTile: CornerBasedShape = RoundedCornerShape(18.dp),
    val chip: CornerBasedShape = RoundedCornerShape(12.dp),
    val button: CornerBasedShape = RoundedCornerShape(999.dp),
    val buttonSmall: CornerBasedShape = RoundedCornerShape(14.dp),
    val islandCompact: CornerBasedShape = RoundedCornerShape(999.dp),
    val islandExpanded: CornerBasedShape = RoundedCornerShape(36.dp)
)

val LocalAuroraShapes = staticCompositionLocalOf { AuroraShapes() }
