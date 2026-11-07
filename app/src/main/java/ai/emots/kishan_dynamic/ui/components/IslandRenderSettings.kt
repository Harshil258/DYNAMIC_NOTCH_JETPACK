package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/** Optional renderer inputs. Null overrides preserve the normal production behavior. */
@Immutable
data class IslandRenderSettings(
    val width: Dp? = null,
    val height: Dp? = null,
    val minimumWidth: Dp? = null,
    val maximumHeight: Dp? = null,
    val corner: Dp? = null,
    val motion: IslandMotionSettings? = null
)

enum class IslandMotionCurve { Spring, Smooth, Linear }

@Immutable
data class IslandMotionSettings(
    val durationMillis: Int = 160,
    val stiffness: Float = 380f,
    val damping: Float = 0.82f,
    val speed: Float = 1f,
    val curve: IslandMotionCurve = IslandMotionCurve.Spring,
    val enabled: Boolean = true
) {
    val effectiveDuration: Int get() = (durationMillis / speed.coerceIn(.25f, 2f)).toInt().coerceAtLeast(1)
    fun <T> spec(): FiniteAnimationSpec<T> = when {
        !enabled -> snap()
        curve == IslandMotionCurve.Spring -> spring(
            stiffness = stiffness.coerceIn(50f, 1500f) * speed.coerceIn(.25f, 2f) * speed.coerceIn(.25f, 2f),
            dampingRatio = damping.coerceIn(.4f, 1.5f))
        else -> tween(effectiveDuration, easing = if (curve == IslandMotionCurve.Linear) LinearEasing else FastOutSlowInEasing)
    }
    fun enter(): EnterTransition = if (!enabled) EnterTransition.None else
        fadeIn(tween(effectiveDuration, delayMillis = (effectiveDuration * .68f).toInt())) +
            scaleIn(initialScale = .92f, animationSpec = spec())
    fun exit(): ExitTransition = if (!enabled) ExitTransition.None else
        fadeOut(tween((effectiveDuration / 2).coerceAtLeast(1))) +
            scaleOut(targetScale = .95f, animationSpec = tween((effectiveDuration / 2).coerceAtLeast(1)))
}

val LocalIslandRenderSettings = staticCompositionLocalOf { IslandRenderSettings() }
