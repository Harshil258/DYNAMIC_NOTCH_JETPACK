package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind

/** Maps the Android satellite modem's enabled signal into our app-owned model. */
fun satelliteActivityFor(enabled: Boolean): LiveActivityInfo? = if (enabled) {
    LiveActivityInfo(
        id = "system-satellite",
        kind = LiveActivityKind.SATELLITE,
        title = "Satellite",
        subtitle = "Satellite connection ready",
        isExpanded = false
    )
} else {
    null
}
