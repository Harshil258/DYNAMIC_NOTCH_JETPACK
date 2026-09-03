package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    AppTheme {
        // Aurora Island is a true-black spatial product, not a conventional light/dark app.
        // One canonical palette prevents white flashes in overlays and keeps every route cohesive.
        val colors = DarkAuroraColors
        val typography = AuroraTypography()
        val shapes = AuroraShapes()
        val spacing = AuroraSpacing()
        val elevation = AuroraElevation()
        val motion = AuroraMotion()

        // Bridge into Material3 color scheme for system widgets
        val materialColorScheme = darkColorScheme(
            primary = colors.primary,
            secondary = colors.lavender,
            tertiary = colors.cyan,
            background = colors.backgroundBase,
            surface = colors.glassSurface,
            surfaceContainer = colors.glassSurfaceStrong,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.glassBorder,
            error = colors.error
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
