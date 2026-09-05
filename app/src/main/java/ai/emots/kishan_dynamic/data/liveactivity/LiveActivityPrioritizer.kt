package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.displayPriority

/** Selects the one activity that can occupy the single visible island surface. */
object LiveActivityPrioritizer {
    fun primary(activities: Collection<LiveActivityInfo>): LiveActivityInfo? =
        activities.maxWithOrNull(
            compareBy<LiveActivityInfo> { it.kind.displayPriority }
                .thenBy { it.startedAtMillis }
        )
}
