package ai.emots.kishan_dynamic.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkConnectivityPolicyTest {

    @Test
    fun wiFiActiveWithCellularDisabledDoesNotShowMobileDataAlert() {
        val previous = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true,
            isCellularConnected = false,
            isCellularDataEnabled = false
        )
        val current = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true,
            isCellularConnected = false,
            isCellularDataEnabled = false
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertEquals(NetworkAlertDecision.None, decision)
    }

    @Test
    fun grantsOfPhonePermissionDuringWifiConnectedDoesNotTriggerAlert() {
        // Simulates granting READ_PHONE_STATE on the Permissions screen:
        // previousState had cellularDataEnabled = null, currentState has cellularDataEnabled = false
        val previous = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true,
            isCellularConnected = false,
            isCellularDataEnabled = null
        )
        val current = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true,
            isCellularConnected = false,
            isCellularDataEnabled = false
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertEquals(NetworkAlertDecision.None, decision)
    }

    @Test
    fun setupOrPermissionsPhaseSuppressesAllNetworkAlerts() {
        val previous = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true
        )
        val current = NetworkConnectivityState(
            isWifiConnected = false,
            isWifiValidated = false,
            isCellularConnected = false,
            isCellularDataEnabled = false
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = true // In setup phase
        )

        assertEquals(NetworkAlertDecision.None, decision)
    }

    @Test
    fun transitioningToAirplaneModeShowsAirplaneAlert() {
        val previous = NetworkConnectivityState(
            isWifiConnected = true,
            isAirplaneMode = false
        )
        val current = NetworkConnectivityState(
            isWifiConnected = false,
            isAirplaneMode = true
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertTrue(decision is NetworkAlertDecision.ShowAirplaneAlert)
        val alert = decision as NetworkAlertDecision.ShowAirplaneAlert
        assertEquals("Airplane Mode", alert.title)
    }

    @Test
    fun exitingAirplaneModeDismissesAlert() {
        val previous = NetworkConnectivityState(
            isAirplaneMode = true
        )
        val current = NetworkConnectivityState(
            isAirplaneMode = false,
            isWifiConnected = true,
            isWifiValidated = true
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertEquals(NetworkAlertDecision.DismissAlert, decision)
    }

    @Test
    fun onlineToCompletelyOfflineTransitionShowsOfflineAlert() {
        val previous = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true
        )
        val current = NetworkConnectivityState(
            isWifiConnected = false,
            isWifiValidated = false,
            isCellularConnected = false,
            isCellularDataEnabled = false
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertTrue(decision is NetworkAlertDecision.ShowOfflineAlert)
        val alert = decision as NetworkAlertDecision.ShowOfflineAlert
        assertEquals("No Internet Connection", alert.title)
    }

    @Test
    fun networkRecoveryDismissesAlert() {
        val previous = NetworkConnectivityState(
            isWifiConnected = false,
            isCellularConnected = false
        )
        val current = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = previous,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertEquals(NetworkAlertDecision.DismissAlert, decision)
    }

    @Test
    fun coldStartWithNoPreviousStateDoesNotShowSpuriousAlert() {
        val current = NetworkConnectivityState(
            isWifiConnected = true,
            isWifiValidated = true
        )

        val decision = NetworkConnectivityPolicy.evaluate(
            previousState = null,
            currentState = current,
            isSetupOrPermissionPhase = false
        )

        assertEquals(NetworkAlertDecision.None, decision)
    }
}
