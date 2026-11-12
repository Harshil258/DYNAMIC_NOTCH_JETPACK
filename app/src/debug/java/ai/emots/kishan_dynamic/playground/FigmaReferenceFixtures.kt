package ai.emots.kishan_dynamic.playground

import android.content.Context
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.data.model.*

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

    fun figmaNodeId(state: ai.emots.kishan_dynamic.ui.components.IslandDemoState): String? = when (state) {
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.Idle -> "27:1050"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.Minimal -> "19:992"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MusicCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MusicExpanded -> "19:1033"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.CallCompact -> "19:991"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.CallExpanded -> "19:1036"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.CallSummaryExpanded -> "19:1036"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FaceTimeCallExpanded -> "19:1035"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SharedMediaCallExpanded -> "19:1037"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.OngoingCallBanner -> "19:1027"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NotificationCompact -> "27:1021"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NotificationStacked -> "27:1021"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NotificationWithMusicCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NotificationExpanded -> "27:992"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SilentModeCompact -> "19:991"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SilentModeExpanded -> "19:1030"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.RingerModeExpanded -> "19:1030"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.VibrateModeCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NormalModeCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.RingerVolumeCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MediaVolumeCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.BluetoothConnected -> "27:682"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.BluetoothConnecting -> "27:682"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ActionControlExpanded -> "27:912"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.PremiumExpiryExpanded -> "19:1030"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ChargingCompact -> "19:991"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ChargingExpanded -> "19:1023"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.LowBatteryCompact -> "19:991"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.LowBatteryExpanded -> "19:1023"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.TimerCompact -> "19:979"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.TimerExpanded -> "19:1022"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.DeliveryCompact -> "27:1080"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.DeliveryExpanded -> "27:1101"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FlightCompact -> "27:1080"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FlightExpanded -> "19:1039"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SportsCompact -> "27:1080"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SportsExpanded -> "19:1039"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NavigationCompact -> "19:1019"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.VoiceMemoExpanded -> "19:1024"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ScreenRecordingExpanded -> "19:1025"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ShortcutExpanded -> "19:1026"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FocusModeExpanded -> "19:1030"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.AirPodsConnected -> "19:1023"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SatelliteConnected -> "19:1028"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FindMyAlert -> "19:1029"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MovedToIPhone -> "19:1031"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.VideoRemoteExpanded -> "19:1038"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.AirDropActivity -> "19:1021"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.TimerImage -> "19:1022"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.NotificationImage -> "27:992"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.CallAvatars -> "19:1027"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.TransportActivity -> "27:1101"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.FlightTrackerExpanded -> "19:1039"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.SubscriptionPricing -> "19:1041"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MovieCard -> "19:1042"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ColorOptions -> "19:1034"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.AirplaneAlert -> "19:1039"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.ScreenMirroringAlert -> "19:1040"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.MobileDataAlert -> "19:1041"
        ai.emots.kishan_dynamic.ui.components.IslandDemoState.TransitRouteAlert -> "19:1042"
    }

    fun loadFigmaBitmap(context: Context, state: ai.emots.kishan_dynamic.ui.components.IslandDemoState): android.graphics.Bitmap? {
        val nodeId = figmaNodeId(state) ?: return null
        val filename = "figma_ref/figma-${nodeId.replace(':', '-')}.png"
        return try {
            context.assets.open(filename).use { android.graphics.BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }
}
