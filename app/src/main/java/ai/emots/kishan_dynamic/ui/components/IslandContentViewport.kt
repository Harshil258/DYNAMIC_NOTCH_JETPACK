package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Measures content at the destination width, independently of the animated shell.
 * Expanded children receive an unbounded content height from verticalScroll;
 * fillMaxSize can no longer manufacture black space from the shell's old height.
 * Only the viewport is bounded by the display. Tall accessibility content scrolls.
 */
@Composable
internal fun IslandContentViewport(
    targetWidth: Dp,
    compact: Boolean,
    compactHeight: Dp,
    maxExpandedHeight: Dp,
    onContentHeight: (Dp) -> Unit,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    Layout(content = {
        Box(modifier = if (compact) Modifier else Modifier.verticalScroll(scrollState),
            contentAlignment = if (compact) androidx.compose.ui.Alignment.Center else androidx.compose.ui.Alignment.TopStart) { content() }
    }) { measurables, constraints ->
        val width = targetWidth.roundToPx().coerceAtLeast(1)
        val heightLimit = if (compact) compactHeight.roundToPx() else maxExpandedHeight.roundToPx()
        val child = measurables.single().measure(Constraints(
            minWidth = width, maxWidth = width,
            minHeight = if (compact) heightLimit else 0,
            maxHeight = heightLimit.coerceAtLeast(1)
        ))
        if (!compact) onContentHeight(child.height.toDp())
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place at the final width throughout the morph: text never rewraps per frame.
            child.placeRelative((constraints.maxWidth - width) / 2,
                ((constraints.maxHeight - child.height) / 2).coerceAtLeast(0))
        }
    }
}
