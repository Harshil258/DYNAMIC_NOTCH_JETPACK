package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import ai.emots.kishan_dynamic.ui.motion.AppMotion

/** Shared native-service / playground visibility transitions. */
fun islandVisibilityEnter(reduced: Boolean, motion: IslandMotionSettings? = null): EnterTransition = when {
    motion?.enabled == false -> EnterTransition.None
    reduced -> fadeIn(tween(80))
    else -> expandVertically(animationSpec = motion?.spec() ?: AppMotion.islandSpring(), expandFrom = Alignment.Top) +
        fadeIn(tween(motion?.effectiveDuration ?: 180))
}
fun islandVisibilityExit(reduced: Boolean, motion: IslandMotionSettings? = null): ExitTransition = when {
    motion?.enabled == false -> ExitTransition.None
    reduced -> fadeOut(tween(80))
    else -> shrinkVertically(animationSpec = motion?.spec() ?: AppMotion.islandSpring(), shrinkTowards = Alignment.Top) +
        fadeOut(tween(motion?.effectiveDuration ?: 140))
}
