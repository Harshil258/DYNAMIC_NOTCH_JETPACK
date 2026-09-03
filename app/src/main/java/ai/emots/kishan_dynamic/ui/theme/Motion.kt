package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

@Immutable
data class AuroraMotion(
    // Durations (ms)
    val durationInstant: Int = 100,
    val durationFast: Int = 180,
    val durationNormal: Int = 260,
    val durationExpand: Int = 380,
    val durationAtmosphere: Int = 3000,
    
    // Standard Easings
    val easeOutCubic: Easing = CubicBezierEasing(0.215f, 0.610f, 0.355f, 1.000f),
    val easeInOutBack: Easing = CubicBezierEasing(0.680f, -0.550f, 0.265f, 1.550f),
    val standardEasing: Easing = FastOutSlowInEasing
) {
    companion object {
        // =====================================================================
        // SPRING ANIMATION SPECIFICATIONS (iOS / Futuristic Physics)
        // =====================================================================

        /**
         * Organic expand spring for morphing island container (~380ms with slight overshoot)
         */
        val ExpandSpringDp: SpringSpec<Dp> = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        )

        val ExpandSpringFloat: SpringSpec<Float> = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        )

        /**
         * Snappy dismiss/collapse spring (~180ms decisive snap)
         */
        val DismissSpringDp: SpringSpec<Dp> = spring(
            dampingRatio = 0.90f,
            stiffness = 550f
        )

        val DismissSpringFloat: SpringSpec<Float> = spring(
            dampingRatio = 0.90f,
            stiffness = 550f
        )

        /**
         * Tactile press reaction spring
         */
        val PressSpringFloat: SpringSpec<Float> = spring(
            dampingRatio = 0.70f,
            stiffness = 600f
        )

        /**
         * Volume / alert pulse reaction spring
         */
        val PulseSpringFloat: SpringSpec<Float> = spring(
            dampingRatio = 0.60f,
            stiffness = 300f
        )

        /**
         * Smooth position translation spring
         */
        val PositionSpringOffset: SpringSpec<IntOffset> = spring(
            dampingRatio = 0.80f,
            stiffness = 400f
        )
    }
}

val LocalAuroraMotion = staticCompositionLocalOf { AuroraMotion() }
