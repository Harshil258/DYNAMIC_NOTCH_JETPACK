package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The colour half of the Dynamic Island design system.
 *
 * Two groups live here and nothing else should be hard-coded in the island
 * components:
 *
 *  - **System** — the iOS 17 dark-mode system palette. These are the colours
 *    Apple uses for semantic accents (a green answer button, a red end-call
 *    button, an orange low-power warning) and for the grouped-background
 *    greys.
 *  - **Reference** — values sampled directly out of the Figma export in
 *    `design/reference/ios17-dynamic-island/`, including the iOS 17 spectrum
 *    wallpaper embedded in the `iPhone 15 Pro` frames. Where the export
 *    disagrees with the published system palette the export wins, because the
 *    export is what the layouts were measured against.
 */
object IslandColors {

    // ---------------------------------------------------------------------
    // iOS 17 system palette (dark appearance)
    // ---------------------------------------------------------------------
    val Red = Color(0xFFFF453A)
    val Orange = Color(0xFFFF9F0A)
    val Yellow = Color(0xFFFFD60A)
    val Green = Color(0xFF30D158)
    val Mint = Color(0xFF63E6E2)
    val Teal = Color(0xFF40C8E0)
    val Cyan = Color(0xFF64D2FF)
    val Blue = Color(0xFF0A84FF)
    val Indigo = Color(0xFF5E5CE6)
    val Purple = Color(0xFFBF5AF2)
    val Pink = Color(0xFFFF375F)
    val Brown = Color(0xFFAC8E68)

    /** Neutral ramp: systemGray .. systemGray6, dark appearance. */
    val Gray = Color(0xFF8E8E93)
    val Gray2 = Color(0xFF636366)
    val Gray3 = Color(0xFF48484A)
    val Gray4 = Color(0xFF3A3A3C)
    val Gray5 = Color(0xFF2C2C2E)
    val Gray6 = Color(0xFF1C1C1E)

    // ---------------------------------------------------------------------
    // Sampled from the reference artboards
    // ---------------------------------------------------------------------
    /** End-call / decline capsule fill. */
    val CapsuleRed = Color(0xFFFA3532)
    /** Answer / active-call capsule fill. */
    val CapsuleGreen = Color(0xFF37C058)
    /** Timer and delivery accents. */
    val CapsuleOrange = Color(0xFFFB8B28)
    /** Navigation, transit and flight accents. */
    val CapsuleCyan = Color(0xFF37A3DE)

    /** Timer ring sweep, `Dynamic Island-1.svg`. */
    val TimerCyan = Color(0xFF67EBF5)
    val TimerBlue = Color(0xFF2A86E6)

    /** Equalizer gradient, `Minimal.svg` paint0_linear. */
    val EqualizerPink = Color(0xFFF84BAB)
    val EqualizerBlue = Color(0xFFB4CDFB)

    /** Type ramp used inside the island. */
    val TextPrimary = Color(0xFFEBEBF0)
    val TextSecondary = Color(0xFF8E8D94)
    val TextTertiary = Color(0xFF9A9A9A)

    /** Glass / capsule button fills. */
    val ButtonGlass = Color(0xFF2A292D)
    val ButtonNeutral = Color(0xFF2C2C2D)
    val ButtonCyan = Color(0xFF1A1C2D)
    val ButtonRed = Color(0xFF1D1011)
    val Track = Color(0xB23F3F3F)

    /**
     * iOS 17 spectrum wallpaper, sampled top-to-bottom from the raster
     * embedded in `iPhone 15 Pro.svg`. Used at low opacity behind the island
     * stage and at full strength on the launcher icon.
     */
    val WallpaperCrimson = Color(0xFF7C0004)
    val WallpaperEmber = Color(0xFFFF5200)
    val WallpaperRose = Color(0xFFFF466A)
    val WallpaperViolet = Color(0xFFAD54FA)
    val WallpaperCyan = Color(0xFF01C1F9)

    // ---------------------------------------------------------------------
    // Avatar / artwork gradients — system colours only, paired the way the
    // Contacts and Music placeholders do it.
    // ---------------------------------------------------------------------
    val AvatarGradients: List<List<Color>> = listOf(
        listOf(Blue, Indigo),
        listOf(Purple, Pink),
        listOf(Orange, Yellow),
        listOf(Green, Mint),
        listOf(Pink, Red),
        listOf(Cyan, Blue),
        listOf(Indigo, Purple),
        listOf(Teal, Green),
    )
}
