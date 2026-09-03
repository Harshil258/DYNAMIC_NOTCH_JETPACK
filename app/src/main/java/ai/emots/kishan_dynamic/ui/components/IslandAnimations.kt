package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import ai.emots.kishan_dynamic.ui.motion.AppMotion

/**
 * Motion vocabulary for the island itself.
 *
 * The container morph is a spring (it has mass), while the *content* that
 * lives inside it cross-fades with a slight scale so text never appears to
 * pop — matching how iOS swaps island payloads.
 */
object IslandAnimations {

    /** Container morph: width, height, corner radius, bubble size. */
    val ContainerSpringDp: SpringSpec<Dp> = AppMotion.islandSpring()

    val ContentEnterSpring: SpringSpec<Float> = AppMotion.islandContentSpring()

    val DefaultSpring: SpringSpec<Float> = AppMotion.settleSpring()

    /** Content arriving inside the island. */
    fun contentEnter(): EnterTransition =
        fadeIn(tween(220, delayMillis = 60, easing = AppMotion.EaseOutExpo)) +
            scaleIn(
                initialScale = 0.92f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = tween(320, delayMillis = 60, easing = AppMotion.EaseOutExpo)
            )

    /** Content leaving the island — quicker than the arrival, so they overlap. */
    fun contentExit(): ExitTransition =
        fadeOut(tween(120, easing = AppMotion.EaseInOut)) +
            scaleOut(
                targetScale = 0.94f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = tween(160, easing = AppMotion.EaseInOut)
            )

    // Retained for source compatibility with older call sites.
    val mainContentEnterTransition: EnterTransition = fadeIn(
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 700f)
    )

    val mainContentExitTransition: ExitTransition = fadeOut(
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 700f)
    )
}

fun Modifier.islandEntranceAnimation(): Modifier = this
