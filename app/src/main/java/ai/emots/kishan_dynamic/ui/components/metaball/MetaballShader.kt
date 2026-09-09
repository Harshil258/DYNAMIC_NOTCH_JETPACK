package ai.emots.kishan_dynamic.ui.components.metaball

import org.intellij.lang.annotations.Language

/**
 * AGSL Shader for authentic Apple-style fluid metaball organic merging.
 *
 * This shader creates the fluid "goo-like" effect by:
 * 1. Sampling the blurred composable layer
 * 2. Evaluating pixel alpha against an optical cutoff threshold
 * 3. Quantizing pixels above threshold to solid alpha and discarding below
 *
 * Used on Android 13+ (API 33+) with RuntimeShader.
 */
@Language("AGSL")
const val MetaballShaderSource = """
    uniform shader composable;
    uniform float cutoff;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord);
        float alpha = color.a;
        
        if (alpha > cutoff) {
            alpha = 1.0;
        } else {
            alpha = 0.0;
        }
        
        color = half4(color.r, color.g, color.b, alpha);
        return color;
    }
"""
