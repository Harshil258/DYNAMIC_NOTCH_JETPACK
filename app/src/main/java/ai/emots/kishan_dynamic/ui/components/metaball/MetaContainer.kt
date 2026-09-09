package ai.emots.kishan_dynamic.ui.components.metaball

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Container that applies the fluid metaball shader effect to all its children.
 *
 * Provides the organic blob merging/splitting effect when the main island and
 * companion bubble separate on Android 13+ (API 33+), with graceful fallback
 * on older versions.
 */
@Composable
fun MetaContainer(
    modifier: Modifier = Modifier,
    cutoff: Float = 0.5f,
    content: @Composable BoxScope.() -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val metaShader = remember { RuntimeShader(MetaballShaderSource) }

        Box(
            modifier = modifier.graphicsLayer {
                metaShader.setFloatUniform("cutoff", cutoff)
                renderEffect = RenderEffect.createRuntimeShaderEffect(
                    metaShader,
                    "composable"
                ).asComposeRenderEffect()
            },
            content = content
        )
    } else {
        Box(
            modifier = modifier,
            content = content
        )
    }
}
