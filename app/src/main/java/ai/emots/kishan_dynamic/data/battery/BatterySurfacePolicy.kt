package ai.emots.kishan_dynamic.data.battery

import ai.emots.kishan_dynamic.data.model.IslandState

/**
 * Keeps battery-source cleanup scoped to the surface that battery events own.
 * Other transient HUDs must survive a charger disconnect or a low-battery
 * preference change.
 */
object BatterySurfacePolicy {
    fun ownsSurface(state: IslandState): Boolean = state is IslandState.Charging
}
