package ai.emots.kishan_dynamic.ui.components

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
        AppleGlyph.Play -> IslandVectorCatalog.Play
        AppleGlyph.Pause -> IslandVectorCatalog.Pause
        AppleGlyph.Backward -> IslandVectorCatalog.Backward
        AppleGlyph.Forward -> IslandVectorCatalog.Forward
        AppleGlyph.SkipBack15 -> IslandVectorCatalog.SkipBack15
        AppleGlyph.SkipForward15 -> IslandVectorCatalog.SkipForward15
        AppleGlyph.AirPlay -> IslandVectorCatalog.AirplayAudio
        AppleGlyph.Speaker -> IslandVectorCatalog.Speaker
        AppleGlyph.Mute -> IslandVectorCatalog.SpeakerSlash
        AppleGlyph.AirPods -> IslandVectorCatalog.Airpods
        AppleGlyph.Equalizer -> IslandVectorCatalog.Equalizer
        AppleGlyph.Waveform -> IslandVectorCatalog.Waveform
        AppleGlyph.WaveformRecording -> IslandVectorCatalog.WaveformRecording

        AppleGlyph.Phone -> IslandVectorCatalog.Phone
        AppleGlyph.EndCall -> IslandVectorCatalog.PhoneDown
        AppleGlyph.Microphone -> IslandVectorCatalog.Microphone
        AppleGlyph.FaceTimeVideo -> IslandVectorCatalog.Video
        AppleGlyph.SharePlay -> IslandVectorCatalog.Shareplay
        AppleGlyph.Info -> IslandVectorCatalog.Info
        AppleGlyph.Chat -> IslandVectorCatalog.Chat
        AppleGlyph.Close -> IslandVectorCatalog.Close

        AppleGlyph.Bell -> IslandVectorCatalog.Bell
        AppleGlyph.BellSlash -> IslandVectorCatalog.BellSlash
        AppleGlyph.Battery -> IslandVectorCatalog.Battery
        AppleGlyph.Charging -> IslandVectorCatalog.Bolt
        AppleGlyph.Timer -> IslandVectorCatalog.Timer
        AppleGlyph.Record -> IslandVectorCatalog.Record
        AppleGlyph.Stop -> IslandVectorCatalog.StopCircle
        AppleGlyph.Lock -> IslandVectorCatalog.Lock
        AppleGlyph.Unlock -> IslandVectorCatalog.Unlock
        AppleGlyph.Link -> IslandVectorCatalog.Link
        AppleGlyph.Moon -> IslandVectorCatalog.Moon
        AppleGlyph.Car -> IslandVectorCatalog.Car
        AppleGlyph.FaceId -> IslandVectorCatalog.FaceId
        AppleGlyph.Satellite -> IslandVectorCatalog.Satellite
        AppleGlyph.AirDrop -> IslandVectorCatalog.Airdrop
        AppleGlyph.Shortcut -> IslandVectorCatalog.Shortcut
        AppleGlyph.Undo -> IslandVectorCatalog.Undo
        AppleGlyph.Check -> IslandVectorCatalog.Check
        AppleGlyph.Location -> IslandVectorCatalog.Location

        AppleGlyph.Airplane -> IslandVectorCatalog.Airplane
        AppleGlyph.ScreenMirroring -> IslandVectorCatalog.ScreenMirroring
        AppleGlyph.Laptop -> IslandVectorCatalog.Laptop
        AppleGlyph.PersonalHotspot -> IslandVectorCatalog.PersonalHotspot
        AppleGlyph.TransitTrain -> IslandVectorCatalog.TransitTrain
        AppleGlyph.NavigationLeft -> IslandVectorCatalog.NavigationLeft
        AppleGlyph.NavigationRight -> IslandVectorCatalog.NavigationRight
        AppleGlyph.NavigationStraight -> IslandVectorCatalog.NavigationStraight
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
