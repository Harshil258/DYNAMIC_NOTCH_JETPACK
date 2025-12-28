package com.notch.dynamicislanddemo.catalog

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium animated background with sophisticated dark gradient
 * Creates a luxury, high-end effect perfect for a wallpaper app
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier, backdrop: LayerBackdrop) {
    // Infinite animations for multi-layered movement
    val infiniteTransition = rememberInfiniteTransition(label = "premium_background")
    
    // Primary gradient rotation
    val primaryAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primary_angle"
    )
    
    // Secondary gradient rotation (opposite direction)
    val secondaryAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondary_angle"
    )
    
    // Tertiary subtle movement
    val tertiaryOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tertiary_offset"
    )
    
    // Color intensity pulse
    val colorIntensity by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_intensity"
    )
    
    // Floating particles animation
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_offset"
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Base dark gradient layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0a0a0f),
                            Color(0xFF12121a),
                            Color(0xFF0f0f14)
                        )
                    )
                )
        )
        
        // Multi-layered premium gradient mesh
        Canvas(modifier = Modifier.layerBackdrop(backdrop).fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            
            // Primary gradient orb (deep purple to violet)
            val primaryRad = primaryAngle * (PI / 180f).toFloat()
            val primaryX = centerX + cos(primaryRad) * width * 0.3f
            val primaryY = centerY + sin(primaryRad) * height * 0.25f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3a1c71).copy(alpha = 0.4f * colorIntensity),
                        Color(0xFF1c1535).copy(alpha = 0.25f * colorIntensity),
                        Color.Transparent
                    ),
                    center = Offset(primaryX, primaryY),
                    radius = width * 0.6f
                ),
                center = Offset(primaryX, primaryY),
                radius = width * 0.6f
            )
            
            // Secondary gradient orb (deep blue to cyan)
            val secondaryRad = secondaryAngle * (PI / 180f).toFloat()
            val secondaryX = centerX + cos(secondaryRad) * width * 0.25f
            val secondaryY = centerY + sin(secondaryRad) * height * 0.3f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0f2027).copy(alpha = 0.35f * colorIntensity),
                        Color(0xFF203a43).copy(alpha = 0.2f * colorIntensity),
                        Color.Transparent
                    ),
                    center = Offset(secondaryX, secondaryY),
                    radius = width * 0.55f
                ),
                center = Offset(secondaryX, secondaryY),
                radius = width * 0.55f
            )
            
            // Tertiary accent orb (deep pink/magenta)
            val tertiaryX = centerX + (tertiaryOffset - 0.5f) * width * 0.4f
            val tertiaryY = height * 0.3f + tertiaryOffset * height * 0.15f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2c1338).copy(alpha = 0.3f * colorIntensity),
                        Color(0xFF1a0b1f).copy(alpha = 0.15f * colorIntensity),
                        Color.Transparent
                    ),
                    center = Offset(tertiaryX, tertiaryY),
                    radius = width * 0.45f
                ),
                center = Offset(tertiaryX, tertiaryY),
                radius = width * 0.45f
            )
            
            // Ambient glow in corners
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = width * 0.4f
                ),
                center = Offset(0f, 0f),
                radius = width * 0.4f
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF16213e).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(width, height),
                    radius = width * 0.4f
                ),
                center = Offset(width, height),
                radius = width * 0.4f
            )
            
            // Floating particles/stars effect
            drawFloatingParticles(particleOffset, colorIntensity)
        }
        
        // Top subtle gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.1f),
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.15f)
                        )
                    )
                )
        )
    }
}

/**
 * Draw floating particle effects for added visual richness
 */
private fun DrawScope.drawFloatingParticles(offset: Float, intensity: Float) {
    val particleCount = 20
    val width = size.width
    val height = size.height
    
    for (i in 0 until particleCount) {
        val angle = (offset + i * (360f / particleCount)) * (PI / 180f).toFloat()
        val radius = (i % 3 + 1) * width * 0.2f
        val x = width / 2f + cos(angle) * radius
        val y = height / 2f + sin(angle) * radius
        
        // Subtle particle glow
        val particleAlpha = (0.05f + abs(sin(angle)) * 0.1f) * intensity
        drawCircle(
            color = Color.White.copy(alpha = particleAlpha),
            center = Offset(x, y),
            radius = 2f + (i % 3) * 1.5f
        )
    }
}
