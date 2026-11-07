package ai.emots.kishan_dynamic.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/** Local geometry only. No copy, source IDs, analytics or diagnostic logging. */
interface IslandGeometryObserver {
    fun update(key: Any, region: String, bounds: Rect?)
}
val LocalIslandGeometryObserver = staticCompositionLocalOf<IslandGeometryObserver?> { null }
fun Modifier.islandInspectableRegion(region: String): Modifier = composed {
    val observer = LocalIslandGeometryObserver.current ?: return@composed this
    val key = remember { Any() }
    DisposableEffect(observer, key) { onDispose { observer.update(key, region, null) } }
    this.onGloballyPositioned { observer.update(key, region, it.boundsInRoot()) }
}
