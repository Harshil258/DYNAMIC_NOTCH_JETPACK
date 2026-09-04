package ai.emots.kishan_dynamic.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

/**
 * =============================================================================
 * APP MOTION — one motion language for the entire product.
 *
 * Everything that moves in this app pulls its timing from here so that the
 * whole product feels like it was choreographed by one hand:
 *
 *  * Springs describe *physical* movement (islands, presses, sheets).
 *  * Easings describe *stylistic* movement (fades, shimmers, ambience).
 *  * Reveal helpers give every screen the same, staggered entrance.
 * =============================================================================
 */
object AppMotion {

    // -------------------------------------------------------------------------
    // Easings — Apple-flavoured curves.
    // -------------------------------------------------------------------------

    /** Standard "enter the screen" curve: fast out, gentle settle. */
    val EaseOutExpo: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /** Symmetrical curve for state changes that are neither entering nor leaving. */
    val EaseInOut: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** Lovable/iOS content curve: quick reveal with a soft settle. */
    val EaseIslandContent: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** Slow, ambient curve used by background blooms and shimmers. */
    val EaseAmbient: Easing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

    // -------------------------------------------------------------------------
    // Durations (ms)
    // -------------------------------------------------------------------------

    const val DurationFast = 180
    const val DurationMedium = 320
    const val DurationSlow = 520
    const val StaggerStep = 55L

    // -------------------------------------------------------------------------
    // Springs
    // -------------------------------------------------------------------------

    /**
     * The island morph, calibrated to the Lovable reference spring
     * (stiffness 460, damping 32, mass 1.05). Compose exposes damping ratio
     * rather than a raw damping coefficient; 0.73 is the equivalent ratio.
     */
    fun <T> islandSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.73f,
        stiffness = 460f
    )

    /** Content appearing inside the island: snappier than the container. */
    fun <T> islandContentSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.85f,
        stiffness = 700f
    )

    /** Buttons, cards, rows — immediate tactile feedback. */
    fun <T> pressSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.62f,
        stiffness = Spring.StiffnessMediumLow * 3f
    )

    /** Values that settle without bouncing: progress, sliders, colours. */
    fun <T> settleSpring(): SpringSpec<T> = spring(
        dampingRatio = 1f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Playful spring for selection indicators and pills. */
    fun <T> selectionSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.7f,
        stiffness = 500f
    )

    // -------------------------------------------------------------------------
    // Shared transitions
    // -------------------------------------------------------------------------

    fun fadeThroughEnter(delayMillis: Int = 0): EnterTransition =
        fadeIn(tween(DurationMedium, delayMillis, EaseOutExpo)) +
            scaleIn(
                initialScale = 0.97f,
                animationSpec = tween(DurationMedium, delayMillis, EaseOutExpo)
            )

    fun fadeThroughExit(): ExitTransition =
        fadeOut(tween(DurationFast, easing = EaseInOut)) +
            scaleOut(targetScale = 0.99f, animationSpec = tween(DurationFast, easing = EaseInOut))

    /** Push a detail screen in from the trailing edge, iOS style. */
    fun pushEnter(): EnterTransition =
        slideInHorizontally(tween(DurationMedium, easing = EaseOutExpo)) { it } +
            fadeIn(tween(DurationMedium, easing = EaseOutExpo))

    fun pushExit(): ExitTransition =
        slideOutHorizontally(tween(DurationMedium, easing = EaseOutExpo)) { -it / 5 } +
            fadeOut(tween(DurationFast))

    fun popEnter(): EnterTransition =
        slideInHorizontally(tween(DurationMedium, easing = EaseOutExpo)) { -it / 5 } +
            fadeIn(tween(DurationMedium, easing = EaseOutExpo))

    fun popExit(): ExitTransition =
        slideOutHorizontally(tween(DurationMedium, easing = EaseOutExpo)) { it } +
            fadeOut(tween(DurationFast))
}

// =============================================================================
// PRESS PHYSICS
// =============================================================================

/**
 * Adds the app-wide tactile press response to any element.
 * Returns the scale state so callers can share one interaction source.
 */
@Composable
fun rememberPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = AppMotion.pressSpring(),
        label = "press_scale"
    )
}

/** Convenience modifier: scale down while pressed, spring back on release. */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    val scale by rememberPressScale(interactionSource, pressedScale, enabled)
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// =============================================================================
// ENTRANCE / REVEAL
// =============================================================================

/**
 * Staggered entrance used by every screen section.
 *
 * @param index position of the element in its screen; drives the delay.
 */
fun Modifier.appReveal(
    index: Int = 0,
    slideDp: Dp = Dp.Unspecified,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * AppMotion.StaggerStep)
        visible = true
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = AppMotion.DurationSlow,
            easing = AppMotion.EaseOutExpo
        ),
        label = "reveal_progress"
    )

    val resolvedSlide = if (slideDp.isSpecified) slideDp else 18.dp
    val slidePx = with(LocalDensity.current) { resolvedSlide.toPx() }

    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * slidePx
        scaleX = 0.985f + 0.015f * progress
        scaleY = 0.985f + 0.015f * progress
        transformOrigin = TransformOrigin(0.5f, 0f)
    }
}

/** Offset helper for reveals that need to slide from the side. */
fun revealOffset(progress: Float, distancePx: Int): IntOffset =
    IntOffset(x = ((1f - progress) * distancePx).toInt(), y = 0)

// =============================================================================
// AMBIENT MOTION
// =============================================================================

/** Slow breathing value in [min, max] — used by glows, blooms and badges. */
@Composable
fun rememberBreathing(
    min: Float = 0.85f,
    max: Float = 1f,
    durationMillis: Int = 3200
): State<Float> {
    val transition = rememberInfiniteTransition(label = "breathing")
    return transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = AppMotion.EaseAmbient),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_value"
    )
}

/** Endless 0f..1f sweep used to drive shimmer highlights. */
@Composable
fun rememberShimmerProgress(durationMillis: Int = 1600): State<Float> {
    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = AppMotion.EaseAmbient),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )
}

/** A moving highlight brush for premium "pro" surfaces. */
@Composable
fun rememberShimmerBrush(
    baseColor: Color,
    highlightColor: Color,
    widthPx: Float
): Brush {
    val progress by rememberShimmerProgress()
    val travel = widthPx * 2f
    val start = -widthPx + travel * progress
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = androidx.compose.ui.geometry.Offset(start, 0f),
        end = androidx.compose.ui.geometry.Offset(start + widthPx, widthPx)
    )
}

/** Convenience alias so screens can read as documentation. */
typealias MotionSpec<T> = FiniteAnimationSpec<T>
