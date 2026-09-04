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

/**
 * Physical geometry of the Dynamic Island, measured 1:1 against the
 * iPhone 15 Pro (393 x 852 pt) reference used by the iOS 17 Dynamic Island
 * component library.
 *
 * Every number below is a real iOS point value — do NOT round them to
 * "nice" numbers, the authenticity of the island depends on them:
 *
 *   idle pill          126.0 x 37.33   corner 18.67 (perfect capsule)
 *   compact split      ~ pill + 8pt gap + 37.33 bubble
 *   minimal bubble     37.33 diameter
 *   expanded sheet     371.0 wide, corner 44
 *   top inset          11.0 from the top edge of the display
 */
data class AppIslandTokens(
    /** True iOS capsule height. */
    val compactHeight: Dp = 37.33.dp,
    /** True iOS idle capsule width — never stretch this to the screen. */
    val compactWidth: Dp = 126.dp,
    /** Compact media capsule: from iPhone 15 Pro-1.svg (190pt x 38pt). */
    val mediaCompactWidth: Dp = 190.dp,
    /** Trailing bubble used in split (live-activity) mode (Minimal.svg: 36.67pt circle). */
    val sideSize: Dp = 36.67.dp,
    /** Gap between the capsule and the trailing bubble (iOS Figma uses 11pt in Minimal.svg). */
    val splitGap: Dp = 11.dp,
    /** Distance from the top edge of the display to the island (Figma: 10-11pt). */
    val topInset: Dp = 11.dp,
    /** Capsule corner radius (exact half of the height = true capsule). */
    val compactCorner: Dp = 18.67.dp,
    /** Expanded sheet corner radius (Figma uses 42pt for sheets, 44pt for 96dp/160dp capsules). */
    val expandedCorner: Dp = 42.dp,
    /** Maximum expanded width from Pro Max 430px-2.svg (408pt on 430pt screen). */
    val expandedMaxWidth: Dp = 408.dp,
    /** Horizontal breathing room kept on each side when expanded (11pt on Pro Max, 13pt on 15 Pro). */
    val expandedSideMargin: Dp = 11.dp,
    /** Incoming call expanded height (96pt in phone context, matches Figma). */
    val incomingCallHeight: Dp = 96.dp,
    /** Ringer / notification expanded height. */
    val ringerExpandedHeight: Dp = 96.dp,
    /** Music expanded height (full sheet with scrubber and controls, from iPhone 15 Pro.svg). */
    val musicExpandedHeight: Dp = 177.dp,
    /** Call expanded height (full sheet with 5 action buttons, from Figma Dynamic Island-2/3.svg). */
    val callExpandedHeight: Dp = 166.dp,
    /** Notification expanded height (96pt in phone context, matches Figma). */
    val notificationExpandedHeight: Dp = 96.dp,
    /** Reference device the geometry above was measured on (iPhone 15 Pro). */
    val referenceScreenWidth: Dp = 393.dp
) {
    /**
     * Expanded width for the current display.
     *
     * The island grows to the device width minus the iOS side margins, but is
     * hard-capped at [expandedMaxWidth] so it never becomes an edge-to-edge
     * banner on tablets or foldables.
     */
    fun expandedWidth(screenWidth: Dp): Dp {
        val available = screenWidth - expandedSideMargin * 2
        return available.coerceIn(300.dp, expandedMaxWidth)
    }

    /**
     * Scale factor to keep the island physically correct on displays that are
     * narrower than the reference device (small phones), so a 126pt pill still
     * reads as a 126pt pill relative to the screen.
     */
    fun deviceScale(screenWidth: Dp): Float {
        val raw = screenWidth / referenceScreenWidth
        return raw.coerceIn(0.86f, 1f)
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

val LightColorTokens = AppColorTokens(
    background = Color(0xFFF8F9FB),
    surface = Color(0xEFFFFFFF),
    surfaceVariant = Color(0xFFF0F2F5),
    surfaceElevated = Color(0xFFFFFFFF),
    primary = Color(0xFF111318),
    secondary = Color(0xFF646870),
    accent = Color(0xFF0B57D0),
    textPrimary = Color(0xFF111318),
    textSecondary = Color(0xFF5F6368),
    textTertiary = Color(0xFF858A92),
    border = Color(0x16000000),
    borderSubtle = Color(0x0C000000),
    success = Color(0xFF17833D),
    warning = Color(0xFFB85C00),
    error = Color(0xFFBA1A1A),
    info = Color(0xFF0B57D0),
    gold = Color(0xFF9A6700),
    disabled = Color(0xFFD5D8DE)
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
            fontWeight = FontWeight.SemiBold,
            fontSize = (30f * scaleFactor).sp,
            lineHeight = (36f * scaleFactor).sp,
            letterSpacing = (-0.7).sp
        ),
        h2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20f * scaleFactor).sp,
            lineHeight = (26f * scaleFactor).sp,
            letterSpacing = (-0.35).sp
        ),
        h3 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16f * scaleFactor).sp,
            lineHeight = (22f * scaleFactor).sp
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
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val windowSize = when {
        screenWidth < 380.dp -> AppWindowSize.Compact
        screenWidth < 600.dp -> AppWindowSize.Medium
        else -> AppWindowSize.Expanded
    }

    val colors = if (darkTheme) DarkColorTokens else LightColorTokens
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
