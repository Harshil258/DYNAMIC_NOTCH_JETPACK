package com.notch.dynamicislanddemo.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext

/**
 * iOS-like press animation modifier
 */
fun Modifier.pressAnimation(
    enabled: Boolean = true,
    onTap: (() -> Unit)? = null,
    withHaptic: Boolean = true
) = composed {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "press_scale"
    )
    
    this
        .scale(scale)
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (withHaptic) {
                            HapticFeedback.light(context)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onTap?.invoke()
                    }
                )
            }
        }
}
