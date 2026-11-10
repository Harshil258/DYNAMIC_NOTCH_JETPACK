package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Figma corner smoothing, following figma-squircle (MIT, Tien Pham).
 * See third_party/figma-squircle-LICENSE and docs/AURORA_FIGMA_VISUAL_RULES.md.
 */
internal data class IslandCornerCurve(
    val radius: Float, val smoothing: Float, val reach: Float,
    val a: Float, val b: Float, val c: Float, val d: Float, val arcDegrees: Float
)

internal fun islandCornerCurve(radius: Float, budget: Float, smoothing: Float = 1f): IslandCornerCurve {
    val r = radius.coerceIn(0f, budget.coerceAtLeast(0f))
    if (r == 0f) return IslandCornerCurve(0f,0f,0f,0f,0f,0f,0f,0f)
    val s = smoothing.coerceIn(0f,1f).coerceAtMost((budget/r-1f).coerceAtLeast(0f))
    val p = ((1f+s)*r).coerceAtMost(budget)
    val arc = 90f*(1f-s)
    fun radians(value: Float) = Math.toRadians(value.toDouble())
    val chord = (sin(radians(arc/2))*r*sqrt(2.0)).toFloat()
    val tangent = r*tan(radians((90f-arc)/4))
    val c = (tangent*cos(radians(45f*s))).toFloat()
    val d = (c*tan(radians(45f*s))).toFloat()
    val b = (p-chord-c-d)/3f
    return IslandCornerCurve(r,s,p,2f*b,b,c,d,arc)
}

internal fun continuousIslandShape(radiusPx: Float, flatTop: Boolean, smoothing: Float): Shape = GenericShape { size, _ ->
    addPath(islandContinuousPath(size,radiusPx,flatTop,smoothing))
}

internal fun islandContinuousPath(size: Size, radiusPx: Float, flatTop: Boolean = false, smoothing: Float = 1f): Path {
    val w=size.width.coerceAtLeast(0f);val h=size.height.coerceAtLeast(0f)
    val q=islandCornerCurve(radiusPx,minOf(w,h)/2f,smoothing)
    return Path().apply {
        if (q.radius==0f) { addRect(Rect(0f,0f,w,h));return@apply }
        val (r,s,p,a,b,c,d,arc)=q
        fun circular(rect: Rect, start: Float) {
            if (arc>0f) arcTo(rect,start+45f*s,arc,false)
        }
        if (flatTop) moveTo(w,0f) else {
            moveTo(w-p,0f)
            relativeCubicTo(a,0f,a+b,0f,a+b+c,d)
            circular(Rect(w-2*r,0f,w,2*r),270f)
            relativeCubicTo(d,c,d,b+c,d,a+b+c)
        }
        lineTo(w,h-p)
        relativeCubicTo(0f,a,0f,a+b,-d,a+b+c)
        circular(Rect(w-2*r,h-2*r,w,h),0f)
        relativeCubicTo(-c,d,-b-c,d,-a-b-c,d)
        lineTo(p,h)
        relativeCubicTo(-a,0f,-a-b,0f,-a-b-c,-d)
        circular(Rect(0f,h-2*r,2*r,h),90f)
        relativeCubicTo(-d,-c,-d,-b-c,-d,-a-b-c)
        if (flatTop) { lineTo(0f,0f);lineTo(w,0f) } else {
            lineTo(0f,p)
            relativeCubicTo(0f,-a,0f,-a-b,d,-a-b-c)
            circular(Rect(0f,0f,2*r,2*r),180f)
            relativeCubicTo(c,-d,b+c,-d,a+b+c,-d)
        }
        close()
    }
}
