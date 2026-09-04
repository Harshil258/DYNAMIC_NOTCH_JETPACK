package ai.emots.kishan_dynamic.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.concurrent.ConcurrentHashMap

/**
 * One contour of an icon, expressed as SVG path data on the shared 24 x 24 grid.
 *
 * The data comes straight out of the iOS 17 Dynamic Island Figma exports (see
 * `design/reference/`), so proportions, stroke weights and corner radii are the
 * real Apple ones rather than an approximation.
 */
@Immutable
data class IslandPath(
    val d: String,
    val stroked: Boolean = false,
    val strokeWidth: Float = 0f,
    val evenOdd: Boolean = false,
    val alpha: Float = 1f,
)

/** A complete symbol: one or more contours sharing the 24pt viewport. */
@Immutable
data class IslandVector(
    val name: String,
    val paths: List<IslandPath>,
)

private val pathCache = ConcurrentHashMap<String, Path>()

/** Parsed once per unique contour; the result is immutable while drawing. */
private fun IslandPath.resolve(): Path = pathCache.getOrPut("$evenOdd|$d") {
    PathParser().parsePathString(d).toPath().apply {
        fillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero
    }
}

/**
 * Paints a symbol into an existing [DrawScope], fitted and centred inside
 * [side] pixels. Use this from components that already own a Canvas.
 */
fun DrawScope.drawIslandVector(
    vector: IslandVector,
    tint: Color,
    side: Float = size.minDimension,
    topLeft: Offset = Offset(
        x = (size.width - side) / 2f,
        y = (size.height - side) / 2f,
    ),
    alpha: Float = 1f,
) {
    val scale = side / IslandVectorCatalog.VIEWPORT
    withTransform({
        translate(topLeft.x, topLeft.y)
        scale(scale, scale, pivot = Offset.Zero)
    }) {
        vector.paths.forEach { contour ->
            val path = contour.resolve()
            if (contour.stroked) {
                drawPath(
                    path = path,
                    color = tint,
                    alpha = contour.alpha * alpha,
                    style = Stroke(
                        width = contour.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            } else {
                drawPath(
                    path = path,
                    color = tint,
                    alpha = contour.alpha * alpha,
                    style = Fill,
                )
            }
        }
    }
}

/** Renders a symbol at [size], tinted with [tint]. */
@Composable
fun IslandIcon(
    vector: IslandVector,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 20.dp,
    alpha: Float = 1f,
) {
    Canvas(modifier = modifier.size(size)) {
        drawIslandVector(vector = vector, tint = tint, alpha = alpha)
    }
}
