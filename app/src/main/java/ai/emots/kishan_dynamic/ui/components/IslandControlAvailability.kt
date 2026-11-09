package ai.emots.kishan_dynamic.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Null means a clearly labeled reference/demo. Production supplies actual source capabilities. */
internal val LocalIslandAvailableControls = staticCompositionLocalOf<Set<IslandControlAction>?> { null }
@Composable internal fun canRenderIslandControl(action: IslandControlAction): Boolean =
    LocalIslandAvailableControls.current?.contains(action) ?: true
