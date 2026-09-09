package ai.emots.kishan_dynamic.ui.components.metaball

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies a custom blur effect to enable organic fluid metaball blending.
 *
 * Uses hardware RenderEffect.createBlurEffect on Android 12+ (API 31+).
 */
fun Modifier.customBlur(blur: Float): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.then(
            graphicsLayer {
                renderEffect = RenderEffect
                    .createBlurEffect(
                        blur,
                        blur,
                        Shader.TileMode.DECAL
                    )
                    .asComposeRenderEffect()
            }
        )
    } else {
        this
    }
}
