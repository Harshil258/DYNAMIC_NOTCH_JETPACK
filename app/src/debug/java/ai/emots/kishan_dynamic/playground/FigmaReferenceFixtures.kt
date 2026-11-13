package ai.emots.kishan_dynamic.playground

import android.content.Context
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.IslandDemoState

enum class FigmaReferenceProvenance(val label: String) {
    DIRECT("DIRECT"),
    FAMILY_ONLY("FAMILY_ONLY"),
    NO_SOURCE("NO_SOURCE")
}

/** Original reference content for visual comparison only. Excluded from release builds. */
object FigmaReferenceFixtures {
    const val packageName = "figma.reference"
    fun resourceUri(context: Context, id: Int) = "android.resource://${context.packageName}/$id"
    fun messages(context: Context) = listOf(
        NotificationInfo("figma-previous", packageName, "Figma", "Design team", "The previous update is ready.",
            timestamp = 1, picturePath = resourceUri(context, R.drawable.figma_reference_message)),
        NotificationInfo("figma-selected", packageName, "Figma", "Mamad", "Haji ina chie zadi\nto baz dahane mano\nservis kardi",
            timestamp = 2, picturePath = resourceUri(context, R.drawable.figma_reference_message)),
        NotificationInfo("figma-next", packageName, "Figma", "Project update", "The next update is ready.",
            timestamp = 3, picturePath = resourceUri(context, R.drawable.figma_reference_message))
    )
    fun caller(context: Context) = ContactInfo("Tamia Castillo", "", resourceUri(context, R.drawable.figma_reference_caller))
    fun music(context: Context) = MusicTrack("Heat Waves", "Grass Animals", packageName,
        durationMs = 241000, positionMs = 50000, albumArtUri = resourceUri(context, R.drawable.music_album_art))

    fun referenceProvenance(state: IslandDemoState): FigmaReferenceProvenance = when (state) {
        IslandDemoState.Idle -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.Minimal -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.MusicCompact -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.MusicExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.CallCompact -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.CallExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.CallSummaryExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.FaceTimeCallExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.SharedMediaCallExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.OngoingCallBanner -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.NotificationCompact -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.NotificationStacked -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.NotificationWithMusicCompact -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.NotificationExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.SilentModeCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.SilentModeExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.RingerModeExpanded -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.VibrateModeCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.NormalModeCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.RingerVolumeCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.MediaVolumeCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.BluetoothConnected -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.BluetoothConnecting -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.ActionControlExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.PremiumExpiryExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.ChargingCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.ChargingExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.LowBatteryCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.LowBatteryExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.TimerCompact -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.TimerExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.DeliveryCompact -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.DeliveryExpanded -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.FlightCompact -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.FlightExpanded -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.SportsCompact -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.SportsExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.NavigationCompact -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.VoiceMemoExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.ScreenRecordingExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.ShortcutExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.FocusModeExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.AirPodsConnected -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.SatelliteConnected -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.FindMyAlert -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.MovedToIPhone -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.VideoRemoteExpanded -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.AirDropActivity -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.TimerImage -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.NotificationImage -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.CallAvatars -> FigmaReferenceProvenance.FAMILY_ONLY
        IslandDemoState.TransportActivity -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.FlightTrackerExpanded -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.SubscriptionPricing -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.MovieCard -> FigmaReferenceProvenance.NO_SOURCE
        IslandDemoState.ColorOptions -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.AirplaneAlert -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.ScreenMirroringAlert -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.MobileDataAlert -> FigmaReferenceProvenance.DIRECT
        IslandDemoState.TransitRouteAlert -> FigmaReferenceProvenance.DIRECT
    }

    fun figmaNodeId(state: IslandDemoState): String? = when (state) {
        IslandDemoState.Idle -> null
        IslandDemoState.Minimal -> "minimal-user-ref"
        IslandDemoState.MusicCompact -> "19:979"
        IslandDemoState.MusicExpanded -> "19:1033"
        IslandDemoState.CallCompact -> "19:991"
        IslandDemoState.CallExpanded -> "19:1036"
        IslandDemoState.CallSummaryExpanded -> null
        IslandDemoState.FaceTimeCallExpanded -> "19:1035"
        IslandDemoState.SharedMediaCallExpanded -> "19:1037"
        IslandDemoState.OngoingCallBanner -> "19:1027"
        IslandDemoState.NotificationCompact -> "27:1021"
        IslandDemoState.NotificationStacked -> "27:1021"
        IslandDemoState.NotificationWithMusicCompact -> "19:979"
        IslandDemoState.NotificationExpanded -> "27:992"
        IslandDemoState.SilentModeCompact -> null
        IslandDemoState.SilentModeExpanded -> "19:1030"
        IslandDemoState.RingerModeExpanded -> "19:1030"
        IslandDemoState.VibrateModeCompact -> null
        IslandDemoState.NormalModeCompact -> null
        IslandDemoState.RingerVolumeCompact -> null
        IslandDemoState.MediaVolumeCompact -> null
        IslandDemoState.BluetoothConnected -> "27:682"
        IslandDemoState.BluetoothConnecting -> null
        IslandDemoState.ActionControlExpanded -> null
        IslandDemoState.PremiumExpiryExpanded -> null
        IslandDemoState.ChargingCompact -> null
        IslandDemoState.ChargingExpanded -> null
        IslandDemoState.LowBatteryCompact -> null
        IslandDemoState.LowBatteryExpanded -> null
        IslandDemoState.TimerCompact -> "19:1022"
        IslandDemoState.TimerExpanded -> "19:1022"
        IslandDemoState.DeliveryCompact -> "27:1080"
        IslandDemoState.DeliveryExpanded -> "27:1101"
        IslandDemoState.FlightCompact -> "27:1080"
        IslandDemoState.FlightExpanded -> "27:1101"
        IslandDemoState.SportsCompact -> null
        IslandDemoState.SportsExpanded -> null
        IslandDemoState.NavigationCompact -> "19:1019"
        IslandDemoState.VoiceMemoExpanded -> "19:1024"
        IslandDemoState.ScreenRecordingExpanded -> "19:1025"
        IslandDemoState.ShortcutExpanded -> "19:1026"
        IslandDemoState.FocusModeExpanded -> null
        IslandDemoState.AirPodsConnected -> "19:1023"
        IslandDemoState.SatelliteConnected -> "19:1028"
        IslandDemoState.FindMyAlert -> "19:1029"
        IslandDemoState.MovedToIPhone -> "19:1031"
        IslandDemoState.VideoRemoteExpanded -> "19:1038"
        IslandDemoState.AirDropActivity -> "19:1021"
        IslandDemoState.TimerImage -> null
        IslandDemoState.NotificationImage -> "27:992"
        IslandDemoState.CallAvatars -> "19:1027"
        IslandDemoState.TransportActivity -> null
        IslandDemoState.FlightTrackerExpanded -> null
        IslandDemoState.SubscriptionPricing -> null
        IslandDemoState.MovieCard -> null
        IslandDemoState.ColorOptions -> "19:1034"
        IslandDemoState.AirplaneAlert -> "19:1039"
        IslandDemoState.ScreenMirroringAlert -> "19:1040"
        IslandDemoState.MobileDataAlert -> "19:1041"
        IslandDemoState.TransitRouteAlert -> "19:1042"
    }

    fun loadFigmaBitmap(context: Context, state: IslandDemoState): android.graphics.Bitmap? {
        val nodeId = figmaNodeId(state) ?: return null
        val filename = if (nodeId == "minimal-user-ref") {
            "figma_ref/figma-minimal-user-ref.png"
        } else {
            "figma_ref/figma-${nodeId.replace(':', '-')}.png"
        }
        return try {
            context.assets.open(filename).use { android.graphics.BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }
}
