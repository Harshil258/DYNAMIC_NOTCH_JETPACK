package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Public theme access point for all UI components and screens.
 * Strict zero hardcoding policy: always read from AuroraTheme.
 */
object AuroraTheme {
    val colors: AuroraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraColors.current

    val typography: AuroraTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraTypography.current

    val shapes: AuroraShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraShapes.current

    val spacing: AuroraSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraSpacing.current

    val elevation: AuroraElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraElevation.current

    val motion: AuroraMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalAuroraMotion.current
}

@Composable
fun AuroraIslandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AppTheme(darkTheme = darkTheme) {
        val colors = if (darkTheme) DarkAuroraColors else LightAuroraColors
        val typography = AuroraTypography()
        val shapes = AuroraShapes()
        val spacing = AuroraSpacing()
        val elevation = AuroraElevation()
        val motion = AuroraMotion()

        // Bridge into Material3 color scheme for system widgets
        val materialColorScheme = if (darkTheme) darkColorScheme(
            primary = colors.primary, secondary = colors.lavender, tertiary = colors.cyan,
            background = colors.backgroundBase, surface = colors.glassSurface,
            surfaceContainer = colors.glassSurfaceStrong, onPrimary = Color.Black,
            onBackground = colors.textPrimary, onSurface = colors.textPrimary,
            outline = colors.glassBorder, error = colors.error
        ) else lightColorScheme(
            primary = colors.primary, secondary = colors.lavender, tertiary = colors.cyan,
            background = colors.backgroundBase, surface = colors.glassSurface,
            surfaceContainer = colors.glassSurfaceStrong, onPrimary = Color.White,
            onBackground = colors.textPrimary, onSurface = colors.textPrimary,
            outline = colors.glassBorder, error = colors.error
        )

        CompositionLocalProvider(
            LocalAuroraColors provides colors,
            LocalAuroraTypography provides typography,
            LocalAuroraShapes provides shapes,
            LocalAuroraSpacing provides spacing,
            LocalAuroraElevation provides elevation,
            LocalAuroraMotion provides motion
        ) {
            MaterialTheme(
                colorScheme = materialColorScheme,
                content = content
            )
        }
    }
}
