package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =============================================================================
// RAW COLOR CONSTANTS (Internal Palette)
// =============================================================================

// Root Dark Void & Atmospheric Nebula Blooms
internal val RawBgBaseDark = Color(0xFF000000)
internal val RawBgGradientStartDark = Color(0xFF030407)
internal val RawBgGradientEndDark = Color(0xFF000000)
internal val RawBgRadialBloomDark = Color(0xFF183F74)
internal val RawBgOceanicBloomDark = Color(0xFF063A4B)

// Luxury Card Glass Surfaces (Matte Obsidian Glass)
internal val RawCardSurfaceDark = Color(0xE6121317)
internal val RawCardSurfaceSubtleDark = Color(0xD90C0D11)
internal val RawCardSurfaceHighlightDark = Color(0xFF1A1C22)
internal val RawCardBorderHighlightDark = Color(0x20FFFFFF)
internal val RawCardBorderSubtleDark = Color(0x0AFFFFFF)

// Luxury Brand & Nebula Glow Accents
internal val RawBrandPrimary = Color(0xFF7C8CFF)
internal val RawBrandPrimarySoft = Color(0xFF4D9CFF)
internal val RawBrandLavender = Color(0xFFB7C4FF)
internal val RawBrandAuraPink = Color(0xFF74D7FF)
internal val RawBrandGold = Color(0xFFFFAE19)

// Accents
internal val RawAccentCyan = Color(0xFF06B6D4)
internal val RawAccentEmerald = Color(0xFF10B981)
internal val RawAccentAmber = Color(0xFFF59E0B)
internal val RawAccentRose = Color(0xFFF43F5E)
internal val RawAccentBlue = Color(0xFF3B82F6)

// Text Hierarchy
internal val RawTextPrimaryDark = Color(0xFFF5F7FA)
internal val RawTextSecondaryDark = Color(0xFFA4A8B2)
internal val RawTextTertiaryDark = Color(0xFF686D78)

// Root Light Palette (Titanium & Frosted Pearlescent)
internal val RawBgBaseLight = Color(0xFFF7F5FC)
internal val RawBgGradientStartLight = Color(0xFFF8F9FB)
internal val RawBgGradientEndLight = Color(0xFFEAF3FC)
internal val RawBgRadialBloomLight = Color(0xFFB7D9F7)
internal val RawGlassLightSurface = Color(0xC8FFFFFF)
internal val RawGlassLightBorder = Color(0x1F000000)
internal val RawGlassLightScrim = Color(0x26000000)

internal val RawTextPrimaryLight = Color(0xFF111318)
internal val RawTextSecondaryLight = Color(0xFF5F6368)
internal val RawTextTertiaryLight = Color(0xFF858A92)

// =============================================================================
// SEMANTIC COLOR TOKENS
// =============================================================================

@Immutable
data class AuroraColors(
    val isDark: Boolean,
    // Root Backgrounds
    val backgroundBase: Color,
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val backgroundRadialBloom: Color,
    
    // Glass Surface Tokens
    val glassSurface: Color,
    val glassSurfaceSubtle: Color,
    val glassSurfaceStrong: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val glassScrim: Color,
    
    // Island Physical Tokens
    val islandBackground: Color,
    val islandBorder: Color,
    val islandCutoutRing: Color,
    
    // Brand Tokens
    val primary: Color,
    val primarySoft: Color,
    val lavender: Color,
    val auraPink: Color,
    
    // Semantic Accents
    val cyan: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    
    // Text Tokens
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    
    // Controls & Interactions
    val controlTrack: Color,
    val controlThumb: Color,
    val controlActive: Color,
    val divider: Color
) {
    /**
     * Atmospheric full-screen background gradient brush.
     */
    val rootBackgroundBrush: Brush
        get() = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    backgroundGradientStart,
                    backgroundBase,
                    Color(0xFF090810)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    backgroundGradientStart,
                    backgroundBase,
                    backgroundGradientEnd
                )
            )
        }

    /**
     * Luminous brand accent gradient brush.
     */
    val brandGradientBrush: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(primary, primarySoft, auraPink)
        )

    /**
     * Frosted glass surface subtle gradient.
     */
    val glassSurfaceBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                glassHighlight,
                glassSurface,
                glassSurfaceSubtle
            )
        )

    /**
     * Hairline specular border highlight brush.
     */
    val glassBorderBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                glassBorder,
                glassBorder.copy(alpha = 0.05f)
            )
        )
}

// =============================================================================
// DEFAULT COLOR PALETTES (Dark & Light)
// =============================================================================

val DarkAuroraColors = AuroraColors(
    isDark = true,
    backgroundBase = RawBgBaseDark,
    backgroundGradientStart = RawBgGradientStartDark,
    backgroundGradientEnd = RawBgGradientEndDark,
    backgroundRadialBloom = RawBgRadialBloomDark,
    
    glassSurface = RawCardSurfaceDark,
    glassSurfaceSubtle = RawCardSurfaceSubtleDark,
    glassSurfaceStrong = RawCardSurfaceHighlightDark,
    glassBorder = RawCardBorderHighlightDark,
    glassHighlight = RawCardSurfaceHighlightDark,
    glassScrim = Color(0x66000000),
    
    islandBackground = Color(0xFF000000),
    islandBorder = Color(0x20FFFFFF),
    islandCutoutRing = Color(0x337C3AED),
    
    primary = RawBrandPrimary,
    primarySoft = RawBrandPrimarySoft,
    lavender = RawBrandLavender,
    auraPink = RawBrandAuraPink,
    
    cyan = RawAccentCyan,
    success = RawAccentEmerald,
    warning = RawBrandGold,
    error = RawAccentRose,
    info = RawAccentBlue,
    
    textPrimary = RawTextPrimaryDark,
    textSecondary = RawTextSecondaryDark,
    textTertiary = RawTextTertiaryDark,
    textOnAccent = Color.White,
    
    controlTrack = Color(0xFF202228),
    controlThumb = Color.White,
    controlActive = RawBrandPrimary,
    divider = Color(0x14FFFFFF)
)

val LightAuroraColors = AuroraColors(
    isDark = false,
    backgroundBase = RawBgBaseLight,
    backgroundGradientStart = RawBgGradientStartLight,
    backgroundGradientEnd = RawBgGradientEndLight,
    backgroundRadialBloom = RawBgRadialBloomLight,
    
    glassSurface = RawGlassLightSurface,
    glassSurfaceSubtle = Color(0xE6FFFFFF),
    glassSurfaceStrong = Color(0xF2FFFFFF),
    glassBorder = RawGlassLightBorder,
    glassHighlight = Color(0x80FFFFFF),
    glassScrim = RawGlassLightScrim,
    
    islandBackground = Color(0xFF0D0D14),
    islandBorder = Color(0x228A5CFF),
    islandCutoutRing = Color(0x668A5CFF),
    
    primary = RawBrandPrimarySoft,
    primarySoft = RawBrandPrimary,
    lavender = Color(0xFF0B57D0),
    auraPink = Color(0xFF4D9CFF),
    
    cyan = Color(0xFF00BFA5),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    error = Color(0xFFE11D48),
    info = Color(0xFF2563EB),
    
    textPrimary = RawTextPrimaryLight,
    textSecondary = RawTextSecondaryLight,
    textTertiary = RawTextTertiaryLight,
    textOnAccent = Color.White,
    
    controlTrack = Color(0x1F8A5CFF),
    controlThumb = Color.White,
    controlActive = RawBrandPrimarySoft,
    divider = Color(0x1F161226)
)

val LocalAuroraColors = staticCompositionLocalOf { DarkAuroraColors }
