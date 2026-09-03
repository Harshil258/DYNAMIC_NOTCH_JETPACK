package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single Source of Truth: Centralized Design Tokens for Aurora Island.
 * Any theme, gradient, color, spacing, or control change is configured here.
 */
object AuroraTokens {

    // =========================================================================
    // 1. ATMOSPHERIC BACKGROUND TOKENS
    // =========================================================================
    object Background {
        val baseVoid = Color(0xFF090710) // Ultra-deep obsidian velvet
        val nebulaViolet = Color(0x42532A86) // Top radial atmospheric light pool
        val nebulaTeal = Color(0x350A3648)   // Bottom-right oceanic teal pool
        val nebulaVioletSecondary = Color(0x1F301650)
        val centerTop = Offset(0.50f, 0.12f)
        val centerBottom = Offset(0.78f, 0.88f)
    }

    // =========================================================================
    // 2. SURFACE & GLASS TOKENS
    // =========================================================================
    object Surface {
        val cardBase = Color(0xFF161224)
        val cardGradientTop = Color(0xFF1A152A)
        val cardGradientBottom = Color(0xFF110E1C)
        val cardElevated = Color(0xFF221B36)
        val cardSubtle = Color(0xFF14111E)
        val glassTranslucent = Color(0x18FFFFFF)
        val glassInteractive = Color(0x24FFFFFF)

        val cardBrush = Brush.verticalGradient(
            listOf(cardGradientTop, cardGradientBottom)
        )

        val stageBrush = Brush.verticalGradient(
            listOf(Color(0xFF151222), Color(0xFF0D0B16))
        )
    }

    // =========================================================================
    // 3. SPECULAR HIGHLIGHT & BORDER TOKENS
    // =========================================================================
    object Border {
        val hairlineSpecular = Brush.verticalGradient(
            listOf(
                Color(0x38FFFFFF), // Top specular rim highlight
                Color(0x0EFFFFFF),
                Color(0x02FFFFFF)
            )
        )

        val activeGradient = Brush.horizontalGradient(
            listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
        )

        val borderSubtle = Color(0x14FFFFFF)
        val borderFocused = Color(0xFF7C3AED)
        val borderError = Color(0xFFEF4444).copy(alpha = 0.5f)
    }

    // =========================================================================
    // 4. BRAND & PALETTE ACCENT TOKENS
    // =========================================================================
    object Palette {
        val primary = Color(0xFF7C3AED)          // Royal Violet
        val primaryLight = Color(0xFFA78BFA)     // Lavender Glow
        val secondary = Color(0xFF00F5D4)        // Electric Cyan
        val accent = Color(0xFFEC4899)           // Neon Pink
        val gold = Color(0xFFF59E0B)             // Amber Gold
        val success = Color(0xFF10B981)          // Emerald Green
        val warning = Color(0xFFFBBF24)          // Solar Yellow
        val error = Color(0xFFEF4444)            // Coral Red
        val info = Color(0xFF38BDF8)             // Sky Blue
        val disabled = Color(0xFF6B657D)         // Muted Slate

        val primaryGradient = Brush.horizontalGradient(
            listOf(primary, Color(0xFF9333EA))
        )

        val alertGradient = Brush.horizontalGradient(
            listOf(success, Color(0xFF059669))
        )

        val chargingGradient = Brush.horizontalGradient(
            listOf(warning, Color(0xFFD97706))
        )
    }

    // =========================================================================
    // 5. TYPOGRAPHY COLOR TOKENS
    // =========================================================================
    object TextColor {
        val primary = Color(0xFFF8F7FC)   // Crisp porcelain white
        val secondary = Color(0xFF9E98B0) // Soft lavender ash
        val tertiary = Color(0xFF6B657D)  // Muted deep slate
        val accent = Color(0xFFA78BFA)    // Radiant purple accent
        val proGold = Color(0xFFFBBF24)   // Golden crown accent
    }

    // =========================================================================
    // 6. SHAPE TOKENS
    // =========================================================================
    object Shape {
        val cardLarge = RoundedCornerShape(26.dp)
        val cardMedium = RoundedCornerShape(20.dp)
        val cardSmall = RoundedCornerShape(14.dp)
        val capsule = RoundedCornerShape(100.dp)
        val circle = CircleShape
    }

    // =========================================================================
    // 7. SPACING TOKENS
    // =========================================================================
    object Spacing {
        val screenGutter: Dp = 20.dp
        val sectionGap: Dp = 20.dp
        val itemGap: Dp = 12.dp
        val cardPadding: Dp = 18.dp
        val microGap: Dp = 6.dp
    }

    // =========================================================================
    // 8. MOTION & SPRING TOKENS
    // =========================================================================
    object Motion {
        val springSnappy = spring<Float>(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMedium
        )
        val springGentle = spring<Float>(
            dampingRatio = 0.70f,
            stiffness = Spring.StiffnessLow
        )
        const val durationFast = 200
        const val durationNormal = 320
    }
}
