package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.icons.FigmaExactGlyphCatalog
import ai.emots.kishan_dynamic.ui.icons.IslandIcon
import ai.emots.kishan_dynamic.ui.icons.IslandVector
import ai.emots.kishan_dynamic.ui.icons.IslandVectorCatalog
import ai.emots.kishan_dynamic.ui.icons.drawIslandVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's semantic symbol set.
 *
 * Every glyph resolves to real vector geometry harvested from the iOS 17
 * Dynamic Island Figma exports (`design/reference/`), or — where the reference
 * sheets have no equivalent — to a symbol authored on the same 24pt grid with
 * matching optical weight. Source SVGs live in `design/icons/`; the Compose
 * path data is generated into [IslandVectorCatalog] by
 * `python3 tools/extract_icons.py`.
 */
enum class AppleGlyph {
    // Dynamic Island / brand
    Notch,

    // Media
    Music,
    Play,
    Pause,
    Backward,
    Forward,
    SkipBack15,
    SkipForward15,
    AirPlay,
    Speaker,
    Mute,
    AirPods,
    Equalizer,
    Waveform,
    WaveformRecording,

    // Calls
    Phone,
    EndCall,
    Microphone,
    FaceTimeVideo,
    SharePlay,
    SharePlayScreen,
    Info,
    Chat,
    Close,

    // Status / live activities
    Bell,
    BellSlash,
    Battery,
    Charging,
    Timer,
    Record,
    Stop,
    Lock,
    Unlock,
    Link,
    Moon,
    Car,
    FaceId,
    Satellite,
    AirDrop,
    Shortcut,
    Undo,
    Check,
    Location,

    // System / navigation
    Airplane,
    ScreenMirroring,
    Laptop,
    PersonalHotspot,
    Cellular,
    TransitTrain,
    NavigationLeft,
    NavigationRight,
    NavigationStraight,
    Maps,
    ChevronLeft,
    ChevronRight,
    Controls,
    Settings,
    Power,
    Shield,
    Wifi,
    Bluetooth,
    Torch,
    Rotate,
    Expand,
    Reset,
    History,
    Star,
    Sparkles,
    Crown,
    Heart,
    Palette,
    Camera,
    Search,
    Globe,
}

/** Maps a semantic glyph onto its generated vector definition. */
val AppleGlyph.vector: IslandVector
    get() = when (this) {
        AppleGlyph.Notch -> IslandVectorCatalog.Notch

        AppleGlyph.Music -> IslandVectorCatalog.MusicNote
        AppleGlyph.Play -> FigmaExactGlyphCatalog.Play
        AppleGlyph.Pause -> FigmaExactGlyphCatalog.Pause
        AppleGlyph.Backward -> FigmaExactGlyphCatalog.Backward
        AppleGlyph.Forward -> FigmaExactGlyphCatalog.Forward
        AppleGlyph.SkipBack15 -> FigmaExactGlyphCatalog.SkipBack15
        AppleGlyph.SkipForward15 -> FigmaExactGlyphCatalog.SkipForward15
        AppleGlyph.AirPlay -> FigmaExactGlyphCatalog.AirPlay
        AppleGlyph.Speaker -> FigmaExactGlyphCatalog.Speaker
        AppleGlyph.Mute -> IslandVectorCatalog.SpeakerSlash
        AppleGlyph.AirPods -> FigmaExactGlyphCatalog.AirPods
        AppleGlyph.Equalizer -> IslandVectorCatalog.Equalizer
        AppleGlyph.Waveform -> FigmaExactGlyphCatalog.Waveform
        AppleGlyph.WaveformRecording -> IslandVectorCatalog.WaveformRecording

        AppleGlyph.Phone -> FigmaExactGlyphCatalog.Phone
        AppleGlyph.EndCall -> FigmaExactGlyphCatalog.EndCall
        AppleGlyph.Microphone -> FigmaExactGlyphCatalog.Microphone
        AppleGlyph.FaceTimeVideo -> FigmaExactGlyphCatalog.FaceTimeVideo
        AppleGlyph.SharePlay -> FigmaExactGlyphCatalog.SharePlay
        AppleGlyph.SharePlayScreen -> FigmaExactGlyphCatalog.SharePlayScreen
        AppleGlyph.Info -> FigmaExactGlyphCatalog.Info
        AppleGlyph.Chat -> FigmaExactGlyphCatalog.Chat
        AppleGlyph.Close -> FigmaExactGlyphCatalog.Close

        AppleGlyph.Bell -> FigmaExactGlyphCatalog.Bell
        AppleGlyph.BellSlash -> FigmaExactGlyphCatalog.BellSlash
        AppleGlyph.Battery -> IslandVectorCatalog.Battery
        AppleGlyph.Charging -> IslandVectorCatalog.Bolt
        AppleGlyph.Timer -> IslandVectorCatalog.Timer
        AppleGlyph.Record -> IslandVectorCatalog.Record
        AppleGlyph.Stop -> IslandVectorCatalog.StopCircle
        AppleGlyph.Lock -> FigmaExactGlyphCatalog.Lock
        AppleGlyph.Unlock -> FigmaExactGlyphCatalog.Unlock
        AppleGlyph.Link -> IslandVectorCatalog.Link
        AppleGlyph.Moon -> FigmaExactGlyphCatalog.Moon
        AppleGlyph.Car -> FigmaExactGlyphCatalog.Car
        AppleGlyph.FaceId -> FigmaExactGlyphCatalog.FaceId
        AppleGlyph.Satellite -> IslandVectorCatalog.Satellite
        AppleGlyph.AirDrop -> IslandVectorCatalog.Airdrop
        AppleGlyph.Shortcut -> IslandVectorCatalog.Shortcut
        AppleGlyph.Undo -> FigmaExactGlyphCatalog.Undo
        AppleGlyph.Check -> FigmaExactGlyphCatalog.Check
        AppleGlyph.Location -> IslandVectorCatalog.Location

        AppleGlyph.Airplane -> FigmaExactGlyphCatalog.Airplane
        AppleGlyph.ScreenMirroring -> FigmaExactGlyphCatalog.ScreenMirroring
        AppleGlyph.Laptop -> FigmaExactGlyphCatalog.Laptop
        AppleGlyph.PersonalHotspot -> FigmaExactGlyphCatalog.PersonalHotspot
        AppleGlyph.Cellular -> FigmaExactGlyphCatalog.Cellular
        AppleGlyph.TransitTrain -> FigmaExactGlyphCatalog.TransitTrain
        AppleGlyph.NavigationLeft -> FigmaExactGlyphCatalog.NavigationLeft
        AppleGlyph.NavigationRight -> FigmaExactGlyphCatalog.NavigationRight
        AppleGlyph.NavigationStraight -> FigmaExactGlyphCatalog.NavigationStraight
        AppleGlyph.Maps -> IslandVectorCatalog.Maps
        AppleGlyph.ChevronLeft -> IslandVectorCatalog.ChevronLeft
        AppleGlyph.ChevronRight -> IslandVectorCatalog.ChevronRight
        AppleGlyph.Controls -> IslandVectorCatalog.Controls
        AppleGlyph.Settings -> IslandVectorCatalog.Settings
        AppleGlyph.Power -> IslandVectorCatalog.Power
        AppleGlyph.Shield -> IslandVectorCatalog.Shield
        AppleGlyph.Wifi -> IslandVectorCatalog.Wifi
        AppleGlyph.Bluetooth -> IslandVectorCatalog.Bluetooth
        AppleGlyph.Torch -> IslandVectorCatalog.Torch
        AppleGlyph.Rotate -> IslandVectorCatalog.Rotate
        AppleGlyph.Expand -> IslandVectorCatalog.Expand
        AppleGlyph.Reset -> IslandVectorCatalog.Reset
        AppleGlyph.History -> IslandVectorCatalog.History
        AppleGlyph.Star -> IslandVectorCatalog.Star
        AppleGlyph.Sparkles -> IslandVectorCatalog.Sparkles
        AppleGlyph.Crown -> IslandVectorCatalog.Crown
        AppleGlyph.Heart -> IslandVectorCatalog.Heart
        AppleGlyph.Palette -> IslandVectorCatalog.Palette
        AppleGlyph.Camera -> IslandVectorCatalog.Camera
        AppleGlyph.Search -> IslandVectorCatalog.Search
        AppleGlyph.Globe -> IslandVectorCatalog.Globe
    }

/**
 * Draws an iOS-native symbol. Geometry is vector data from the Dynamic Island
 * reference set, so weight and proportion stay correct at every size.
 */
@Composable
fun AppleIcon(
    glyph: AppleGlyph,
    tint: Color = Color.White,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    IslandIcon(
        vector = glyph.vector,
        modifier = modifier,
        tint = tint,
        size = size,
        alpha = alpha,
    )
}

/** Canvas-level variant for components that already own a [DrawScope]. */
fun DrawScope.drawAppleGlyph(
    glyph: AppleGlyph,
    tint: Color,
    side: Float = size.minDimension,
    alpha: Float = 1f,
) {
    drawIslandVector(vector = glyph.vector, tint = tint, side = side, alpha = alpha)
}

/** Native visible ink size from the original Figma SVG, without a second optical inset. */
internal fun AppleGlyph.figmaInkSize(fallback: Dp = 22.dp): Dp = when (this) {
    AppleGlyph.Speaker -> 30.0.dp
    AppleGlyph.Microphone -> 24.0.dp
    AppleGlyph.FaceTimeVideo -> 27.0.dp
    AppleGlyph.SharePlay -> 31.0.dp
    AppleGlyph.EndCall -> 26.0.dp
    AppleGlyph.Info -> 18.0.dp
    AppleGlyph.AirPods -> 32.0.dp
    AppleGlyph.Forward -> 29.0.dp
    AppleGlyph.Backward -> 29.0.dp
    AppleGlyph.Play -> 28.0.dp
    AppleGlyph.AirPlay -> 21.0.dp
    AppleGlyph.Pause -> 19.0.dp
    AppleGlyph.Close -> 19.0.dp
    AppleGlyph.BellSlash -> 25.0.dp
    AppleGlyph.Undo -> 17.0.dp
    AppleGlyph.SkipBack15 -> 27.0.dp
    AppleGlyph.SkipForward15 -> 28.0.dp
    AppleGlyph.Airplane -> 31.0.dp
    AppleGlyph.Chat -> 16.0.dp
    AppleGlyph.Bell -> 17.0.dp
    AppleGlyph.Check -> 20.0.dp
    AppleGlyph.Lock -> 18.0.dp
    AppleGlyph.Unlock -> 18.0.dp
    AppleGlyph.PersonalHotspot -> 23.0.dp
    AppleGlyph.Moon -> 16.0.dp
    AppleGlyph.Car -> 20.0.dp
    AppleGlyph.ScreenMirroring -> 36.0.dp
    AppleGlyph.Laptop -> 26.0.dp
    AppleGlyph.Cellular -> 32.0.dp
    AppleGlyph.TransitTrain -> 38.0.dp
    AppleGlyph.Phone -> 22.0.dp
    AppleGlyph.NavigationLeft -> 33.0.dp
    AppleGlyph.NavigationRight -> 33.0.dp
    AppleGlyph.NavigationStraight -> 34.0.dp
    AppleGlyph.FaceId -> 20.0.dp
    AppleGlyph.Waveform -> 23.0.dp
    AppleGlyph.SharePlayScreen -> 33.24300000000002.dp
    else -> fallback
}
