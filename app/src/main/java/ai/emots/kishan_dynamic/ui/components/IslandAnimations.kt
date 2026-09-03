package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

object IslandAnimations {
    val ContainerSpringDp: SpringSpec<Dp> = spring(
        dampingRatio = 0.65f,
        stiffness = 300f
    )
    
    val ContentEnterSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.65f,
        stiffness = 350f
    )
    
    val DefaultSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 300f
    )
    
    val mainContentEnterTransition: EnterTransition = fadeIn(
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    
    val mainContentExitTransition: ExitTransition = fadeOut(
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
}

fun Modifier.islandEntranceAnimation(): Modifier = this
