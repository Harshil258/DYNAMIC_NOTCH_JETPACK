package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// 1. DEVICE ADAPTIVE WINDOW SIZE CLASSIFICATION
// =============================================================================
enum class AppWindowSize {
    Compact,   // Phones < 380dp width (e.g. Small / standard devices)
    Medium,    // Phones 380dp - 600dp (e.g. Pixel, Galaxy S series, Pro Max)
    Expanded   // Tablets / Foldables unfolded > 600dp
}

// =============================================================================
// 2. DESIGN TOKEN DATA STRUCTURES
// =============================================================================

data class AppColorTokens(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceElevated: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color,
    val borderSubtle: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val gold: Color,
    val disabled: Color
)

data class AppTypographyTokens(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
    val islandTitle: TextStyle,
    val islandSubtitle: TextStyle,
    val islandTime: TextStyle,
    val islandBadge: TextStyle,
    val badge: TextStyle
)

data class AppSpacingTokens(
    val zero: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp
)

data class AppRadiusTokens(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 22.dp,
    val card: Dp = 26.dp,
    val pill: Dp = 999.dp
)

data class AppLayoutTokens(
    val windowSize: AppWindowSize,
    val screenGutter: Dp,
    val sectionGap: Dp,
    val cardPadding: Dp,
    val compactCardPadding: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val bottomBarHeight: Dp
)

data class AppIslandTokens(
    val compactHeight: Dp = 37.dp,
    val compactWidth: Dp = 155.dp,
    val mediaCompactWidth: Dp = 195.dp,
    val sideSize: Dp = 37.dp,
    val splitGap: Dp = 10.dp,
    val incomingCallHeight: Dp = 98.dp,
    val ringerExpandedHeight: Dp = 96.dp,
    val musicExpandedHeight: Dp = 210.dp,
    val callExpandedHeight: Dp = 200.dp,
    val notificationExpandedHeight: Dp = 190.dp
) {
    fun expandedWidth(screenWidth: Dp): Dp {
        return (screenWidth - 24.dp).coerceIn(300.dp, 420.dp)
    }
}

// =============================================================================
// 3. COLOR PALETTES (DARK OBSIDIAN LUXURY AS DEFAULT)
// =============================================================================

val DarkColorTokens = AppColorTokens(
    background = Color(0xFF000000),         // Pure True Black (Gemini-Grade Minimalism)
    surface = Color(0xFF101012),            // Restrained Minimal Surface
    surfaceVariant = Color(0xFF18181C),     // Elevated System Surface
    surfaceElevated = Color(0xFF202024),    // Floating Island/Control Surface
    primary = Color(0xFFFFFFFF),            // Clean Monochrome Primary
    secondary = Color(0xFF8E8E93),          // Apple System Gray
    accent = Color(0xFF0A84FF),             // System Accent Blue
    textPrimary = Color(0xFFFFFFFF),        // High Contrast Pure White
    textSecondary = Color(0xFF8E8E93),      // Calm Editorial Muted Gray
    textTertiary = Color(0xFF55555A),       // Quiet Caption
    border = Color(0x14FFFFFF),             // Ultra-Minimal Hairline
    borderSubtle = Color(0x0AFFFFFF),       // Faint Boundary
    success = Color(0xFF30D158),            // Apple System Green (Active)
    warning = Color(0xFFFF9F0A),            // Amber (Battery/Charging)
    error = Color(0xFFFF453A),              // System Red (Critical)
    info = Color(0xFF0A84FF),               // System Blue
    gold = Color(0xFFFFD60A),               // System Yellow
    disabled = Color(0xFF3A3A3C)
)

// =============================================================================
// 4. ADAPTIVE TYPOGRAPHY BUILDER (DEVICE-SCALED)
// =============================================================================

private fun typographyFor(windowSize: AppWindowSize): AppTypographyTokens {
    val scaleFactor = when (windowSize) {
        AppWindowSize.Compact -> 0.92f
        AppWindowSize.Medium -> 1.0f
        AppWindowSize.Expanded -> 1.12f
    }

    return AppTypographyTokens(
        h1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (20f * scaleFactor).sp,
            lineHeight = (26f * scaleFactor).sp,
            letterSpacing = (-0.3).sp
        ),
        h2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16f * scaleFactor).sp,
            lineHeight = (22f * scaleFactor).sp,
            letterSpacing = (-0.2).sp
        ),
        h3 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14f * scaleFactor).sp,
            lineHeight = (19f * scaleFactor).sp
        ),
        body = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14f * scaleFactor).sp,
            lineHeight = (20f * scaleFactor).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (12.5f * scaleFactor).sp,
            lineHeight = (17f * scaleFactor).sp
        ),
        button = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13.5f * scaleFactor).sp,
            lineHeight = (18f * scaleFactor).sp
        ),
        caption = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11f * scaleFactor).sp,
            lineHeight = (15f * scaleFactor).sp
        ),
        islandTitle = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (17.5f * scaleFactor).sp,
            lineHeight = (22f * scaleFactor).sp
        ),
        islandSubtitle = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5f * scaleFactor).sp,
            lineHeight = (18f * scaleFactor).sp
        ),
        islandTime = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * scaleFactor).sp,
            lineHeight = (15f * scaleFactor).sp
        ),
        islandBadge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (9.5f * scaleFactor).sp,
            lineHeight = (12f * scaleFactor).sp
        ),
        badge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (10.5f * scaleFactor).sp,
            letterSpacing = 0.6.sp
        )
    )
}

// =============================================================================
// 5. ADAPTIVE LAYOUT BUILDER (DEVICE-SCALED)
// =============================================================================

private fun layoutFor(windowSize: AppWindowSize): AppLayoutTokens {
    return when (windowSize) {
        AppWindowSize.Compact -> AppLayoutTokens(
            windowSize = windowSize,
            screenGutter = 12.dp,
            sectionGap = 14.dp,
            cardPadding = 14.dp,
            compactCardPadding = 10.dp,
            iconSmall = 16.dp,
            iconMedium = 20.dp,
            iconLarge = 24.dp,
            bottomBarHeight = 64.dp
        )
        AppWindowSize.Medium -> AppLayoutTokens(
            windowSize = windowSize,
            screenGutter = 16.dp,
            sectionGap = 18.dp,
            cardPadding = 18.dp,
            compactCardPadding = 12.dp,
            iconSmall = 18.dp,
            iconMedium = 22.dp,
            iconLarge = 28.dp,
            bottomBarHeight = 72.dp
        )
        AppWindowSize.Expanded -> AppLayoutTokens(
            windowSize = windowSize,
            screenGutter = 24.dp,
            sectionGap = 24.dp,
            cardPadding = 22.dp,
            compactCardPadding = 16.dp,
            iconSmall = 20.dp,
            iconMedium = 26.dp,
            iconLarge = 32.dp,
            bottomBarHeight = 80.dp
        )
    }
}

// =============================================================================
// 6. COMPOSITION LOCALS
// =============================================================================

val LocalAppColors = staticCompositionLocalOf { DarkColorTokens }
val LocalAppTypography = staticCompositionLocalOf { typographyFor(AppWindowSize.Medium) }
val LocalAppSpacing = staticCompositionLocalOf { AppSpacingTokens() }
val LocalAppRadius = staticCompositionLocalOf { AppRadiusTokens() }
val LocalAppLayout = staticCompositionLocalOf { layoutFor(AppWindowSize.Medium) }
val LocalAppIsland = staticCompositionLocalOf { AppIslandTokens() }
val LocalAppWindowSize = staticCompositionLocalOf { AppWindowSize.Medium }

// =============================================================================
// 7. SINGLE SOURCE OF TRUTH ACCESSOR: AppTheme
// =============================================================================

object AppTheme {
    val colors: AppColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val spacing: AppSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current

    val radius: AppRadiusTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppRadius.current

    val layout: AppLayoutTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppLayout.current

    val island: AppIslandTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppIsland.current

    val windowSize: AppWindowSize
        @Composable
        @ReadOnlyComposable
        get() = LocalAppWindowSize.current
}

// =============================================================================
// 8. ROOT THEME PROVIDER
// =============================================================================

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val windowSize = when {
        screenWidth < 380.dp -> AppWindowSize.Compact
        screenWidth < 600.dp -> AppWindowSize.Medium
        else -> AppWindowSize.Expanded
    }

    val colors = DarkColorTokens
    val typography = typographyFor(windowSize)
    val layout = layoutFor(windowSize)
    val spacing = AppSpacingTokens()
    val radius = AppRadiusTokens()
    val island = AppIslandTokens()

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppSpacing provides spacing,
        LocalAppRadius provides radius,
        LocalAppLayout provides layout,
        LocalAppIsland provides island,
        LocalAppWindowSize provides windowSize,
        content = content
    )
}
