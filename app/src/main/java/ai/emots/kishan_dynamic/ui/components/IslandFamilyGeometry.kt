package ai.emots.kishan_dynamic.ui.components

/** V4.3 design-equivalent dp targets. No notification policy or timers live here. */
enum class IslandGeometryFamily { OwnerMinimal, Idle, StatusCompact, ContextualAlert, ActivityExpanded }
data class IslandGeometryTarget(val family: IslandGeometryFamily, val width: Float, val height: Float, val radius: Float)
data class IslandSurfaceGeometry(val width: Float, val height: Float, val radius: Float)

object IslandFamilyGeometry {
    val targets: Map<IslandDemoState, IslandGeometryTarget> = mapOf(
        IslandDemoState.Idle to IslandGeometryTarget(IslandGeometryFamily.Idle, 127.0f, 36.0f, 18.0f),
        IslandDemoState.Minimal to IslandGeometryTarget(IslandGeometryFamily.OwnerMinimal, 108.0f, 32.0f, 15.9018f),
        IslandDemoState.MusicCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 190.0f, 36.67f, 18.335f),
        IslandDemoState.MusicExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 177.0f, 42.0f),
        IslandDemoState.CallCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 152.0f, 36.67f, 18.335f),
        IslandDemoState.CallExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 166.0f, 42.0f),
        IslandDemoState.CallSummaryExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 112.0f, 42.0f),
        IslandDemoState.FaceTimeCallExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 168.0f, 42.0f),
        IslandDemoState.SharedMediaCallExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 172.64f, 42.0f),
        IslandDemoState.OngoingCallBanner to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.NotificationCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 189f, 36.67f, 18.335f),
        IslandDemoState.NotificationStacked to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 189f, 36.67f, 18.335f),
        IslandDemoState.NotificationWithMusicCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 190.0f, 36.67f, 18.335f),
        IslandDemoState.NotificationExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.SilentModeCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 152.0f, 36.67f, 18.335f),
        IslandDemoState.SilentModeExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.RingerModeExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.VibrateModeCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 172.0f, 36.67f, 18.335f),
        IslandDemoState.NormalModeCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 172.0f, 36.67f, 18.335f),
        IslandDemoState.RingerVolumeCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 172.0f, 36.67f, 18.335f),
        IslandDemoState.MediaVolumeCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 172.0f, 36.67f, 18.335f),
        IslandDemoState.BluetoothConnected to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 196.0f, 36.67f, 18.335f),
        IslandDemoState.BluetoothConnecting to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 196.0f, 36.67f, 18.335f),
        IslandDemoState.ActionControlExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 210.0f, 42.0f),
        IslandDemoState.PremiumExpiryExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 112.0f, 42.0f),
        IslandDemoState.ChargingCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 132.0f, 36.67f, 18.335f),
        IslandDemoState.ChargingExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.LowBatteryCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 132.0f, 36.67f, 18.335f),
        IslandDemoState.LowBatteryExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.TimerCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 222.0f, 36.67f, 18.335f),
        IslandDemoState.TimerExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 85.73f, 43.0f),
        IslandDemoState.DeliveryCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 160.0f, 36.67f, 18.335f),
        IslandDemoState.DeliveryExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 144.0f, 42.0f),
        IslandDemoState.FlightCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 160.0f, 36.67f, 18.335f),
        IslandDemoState.FlightExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 144.0f, 42.0f),
        IslandDemoState.SportsCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 156.0f, 36.67f, 18.335f),
        IslandDemoState.SportsExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 144.0f, 42.0f),
        IslandDemoState.NavigationCompact to IslandGeometryTarget(IslandGeometryFamily.StatusCompact, 156.0f, 36.67f, 18.335f),
        IslandDemoState.VoiceMemoExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.ScreenRecordingExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.ShortcutExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 85.08f, 43.0f),
        IslandDemoState.FocusModeExpanded to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.AirPodsConnected to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.SatelliteConnected to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.FindMyAlert to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.MovedToIPhone to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.VideoRemoteExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 177.0f, 42.0f),
        IslandDemoState.AirDropActivity to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.TimerImage to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.NotificationImage to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.CallAvatars to IslandGeometryTarget(IslandGeometryFamily.ContextualAlert, 367.0f, 86.0f, 43.0f),
        IslandDemoState.TransportActivity to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 148.0f, 42.0f),
        IslandDemoState.FlightTrackerExpanded to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 144.0f, 42.0f),
        IslandDemoState.SubscriptionPricing to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 162.0f, 42.0f),
        IslandDemoState.MovieCard to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 142.0f, 42.0f),
        IslandDemoState.ColorOptions to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 185.3f, 42.0f),
        IslandDemoState.AirplaneAlert to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 148.0f, 42.0f),
        IslandDemoState.ScreenMirroringAlert to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 144.0f, 42.0f),
        IslandDemoState.MobileDataAlert to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 162.0f, 42.0f),
        IslandDemoState.TransitRouteAlert to IslandGeometryTarget(IslandGeometryFamily.ActivityExpanded, 367.0f, 142.0f, 42.0f),
    )
    fun target(state: IslandDemoState): IslandGeometryTarget = targets.getValue(state)

    /** Insets are removed exactly once by the caller. Compact content is never canvas-scaled. */
    fun resolve(state: IslandDemoState, availableWidth: Float, fontScale: Float = 1f,
                contentWidth: Float? = null, contentHeight: Float? = null,
                cutoutWidth: Float = 0f, cutoutHeight: Float = 0f): IslandSurfaceGeometry {
        val t = target(state)
        val available = availableWidth.coerceAtLeast(0f)
        val compact = t.family in setOf(IslandGeometryFamily.StatusCompact, IslandGeometryFamily.Idle, IslandGeometryFamily.OwnerMinimal)
        val width = if (compact) maxOf(contentWidth ?: t.width, cutoutWidth).coerceAtMost(available)
            else (t.width * available / 393f).coerceAtMost(available)
        val height = maxOf(t.height, cutoutHeight, contentHeight ?: 0f,
            if (compact) t.height else t.height + (fontScale.coerceAtLeast(1f) - 1f) * 32f)
        val radius = if (state == IslandDemoState.NotificationImage && height >= 126f) 44f else if (height >= 142f && t.family == IslandGeometryFamily.ContextualAlert) 42f else t.radius
        return IslandSurfaceGeometry(width, height, minOf(radius, width / 2f, height / 2f))
    }
}
