package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind

/** Maps an externally selected live-video route into our app-owned island model. */
fun mapScreenMirroringRoute(
    isDefaultRoute: Boolean,
    routeName: String?,
    routeDescription: String? = null
): LiveActivityInfo? {
    if (isDefaultRoute) return null

    val destination = routeName.orEmpty().trim().ifBlank {
        routeDescription.orEmpty().trim()
    }
    if (destination.isBlank()) return null

    return LiveActivityInfo(
        id = "media-route-screen-mirroring",
        kind = LiveActivityKind.SCREEN_MIRRORING_ALERT,
        title = "Screen mirroring",
        subtitle = destination,
        isExpanded = false
    )
}
