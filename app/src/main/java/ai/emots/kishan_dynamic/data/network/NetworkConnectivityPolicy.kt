package ai.emots.kishan_dynamic.data.network

data class NetworkConnectivityState(
    val isWifiConnected: Boolean = false,
    val isWifiValidated: Boolean = false,
    val isCellularConnected: Boolean = false,
    val isCellularValidated: Boolean = false,
    val isCellularDataEnabled: Boolean? = null,
    val isAirplaneMode: Boolean = false
) {
    /** True when internet access is validated, or when Wi-Fi is actively connected without airplane mode. */
    val hasInternet: Boolean
        get() = isWifiValidated || isCellularValidated || (isWifiConnected && !isAirplaneMode)

    /** True when neither Wi-Fi nor Cellular transport is available. */
    val isCompletelyOffline: Boolean
        get() = !isWifiConnected && !isCellularConnected
}

sealed class NetworkAlertDecision {
    object None : NetworkAlertDecision()
    data class ShowAirplaneAlert(val title: String, val subtitle: String) : NetworkAlertDecision()
    data class ShowOfflineAlert(val title: String, val subtitle: String) : NetworkAlertDecision()
    object DismissAlert : NetworkAlertDecision()
}

/**
 * Pure policy deciding Dynamic Island network alert presentations.
 *
 * Adheres strictly to iOS Dynamic Island principles:
 * 1. Routine background connectivity states never block the screen.
 * 2. Mobile data being off while Wi-Fi internet is active is NOT an error.
 * 3. Never pop up network alerts during app onboarding or setup phases.
 * 4. Genuine offline alerts are temporary status announcements, not permanent blocking dialogs.
 */
object NetworkConnectivityPolicy {

    fun evaluate(
        previousState: NetworkConnectivityState?,
        currentState: NetworkConnectivityState,
        isSetupOrPermissionPhase: Boolean = false
    ): NetworkAlertDecision {
        // 1. Suppress intrusive alerts during initial onboarding / setup / permissions grant
        if (isSetupOrPermissionPhase) {
            return NetworkAlertDecision.None
        }

        // 2. Airplane mode handling
        if (currentState.isAirplaneMode) {
            val wasAirplane = previousState?.isAirplaneMode == true
            return if (!wasAirplane) {
                NetworkAlertDecision.ShowAirplaneAlert(
                    title = "Airplane Mode",
                    subtitle = "Turn off Airplane Mode or use Wi-Fi to access data."
                )
            } else {
                NetworkAlertDecision.None
            }
        }

        // If transitioning out of Airplane mode, immediately dismiss airplane alert
        if (previousState?.isAirplaneMode == true && !currentState.isAirplaneMode) {
            return NetworkAlertDecision.DismissAlert
        }

        // 3. Validated internet is available (via Wi-Fi or Cellular)
        if (currentState.hasInternet) {
            // Dismiss any existing offline or mobile-data alert
            val hadAlertCondition = previousState != null &&
                (!previousState.hasInternet || previousState.isAirplaneMode)
            return if (hadAlertCondition) {
                NetworkAlertDecision.DismissAlert
            } else {
                NetworkAlertDecision.None
            }
        }

        // 4. Device transitioned to completely offline
        if (currentState.isCompletelyOffline) {
            val wasOnline = previousState?.hasInternet == true
            if (wasOnline) {
                return if (currentState.isCellularDataEnabled == false) {
                    NetworkAlertDecision.ShowOfflineAlert(
                        title = "No Internet Connection",
                        subtitle = "Turn on mobile data or Wi-Fi to access data."
                    )
                } else {
                    NetworkAlertDecision.ShowOfflineAlert(
                        title = "No Internet Connection",
                        subtitle = "Check your connection or connect to Wi-Fi."
                    )
                }
            }
        }

        return NetworkAlertDecision.None
    }
}
