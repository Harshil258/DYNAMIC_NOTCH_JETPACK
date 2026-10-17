package ai.emots.kishan_dynamic.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.network.NetworkAlertDecision
import ai.emots.kishan_dynamic.data.network.NetworkConnectivityPolicy
import ai.emots.kishan_dynamic.data.network.NetworkConnectivityState
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Bridges system network connectivity into the Dynamic Island lifecycle.
 *
 * Uses event-driven [ConnectivityManager.NetworkCallback] and system broadcast receivers
 * rather than unconstrained polling. Follows iOS Dynamic Island UX:
 * - If Wi-Fi is active, disabled cellular data is NOT treated as an error.
 * - Suppressed during onboarding and permissions setup.
 * - Genuine offline alerts are temporary status announcements with calibrated timeouts.
 * - Cleans up immediately when connectivity recovers or service unbinds.
 */
class SystemLiveActivityMonitor(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val preferences = AuroraPreferences(appContext)

    private var previousState: NetworkConnectivityState? = null
    private var isRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            reconcile()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            reconcile()
        }

        override fun onLost(network: Network) {
            reconcile()
        }
    }

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> reconcile()
            }
        }
    }

    fun start() {
        if (isRegistered) return
        isRegistered = true

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = android.net.NetworkRequest.Builder().build()
                connectivityManager?.registerNetworkCallback(request, networkCallback)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        ContextCompat.registerReceiver(
            appContext,
            systemReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Read initial state safely without triggering premature alerts on launch
        previousState = readCurrentNetworkState()
    }

    fun close() {
        if (!isRegistered) return
        isRegistered = false

        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
        runCatching { appContext.unregisterReceiver(systemReceiver) }

        IslandStateManager.clearLiveActivity(AIRPLANE_ACTIVITY_ID)
        IslandStateManager.clearLiveActivity(MOBILE_DATA_ACTIVITY_ID)
    }

    private fun reconcile() {
        scope.launch(Dispatchers.IO) {
            val setupDone = preferences.setupDone.first()
            val isSetupOrPermissionPhase = !setupDone || !PermissionUtils.areCorePermissionsGranted(appContext)
            val currentState = readCurrentNetworkState()

            val decision = NetworkConnectivityPolicy.evaluate(
                previousState = previousState,
                currentState = currentState,
                isSetupOrPermissionPhase = isSetupOrPermissionPhase
            )
            previousState = currentState

            scope.launch(Dispatchers.Main) {
                applyDecision(decision)
            }
        }
    }

    private fun applyDecision(decision: NetworkAlertDecision) {
        when (decision) {
            is NetworkAlertDecision.None -> Unit
            is NetworkAlertDecision.DismissAlert -> {
                IslandStateManager.clearLiveActivity(AIRPLANE_ACTIVITY_ID)
                IslandStateManager.clearLiveActivity(MOBILE_DATA_ACTIVITY_ID)
            }
            is NetworkAlertDecision.ShowAirplaneAlert -> {
                IslandStateManager.clearLiveActivity(MOBILE_DATA_ACTIVITY_ID)
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = AIRPLANE_ACTIVITY_ID,
                        kind = LiveActivityKind.AIRPLANE_ALERT,
                        title = decision.title,
                        subtitle = decision.subtitle,
                        isExpanded = true,
                        expandedByUser = false,
                        expiresAtMillis = System.currentTimeMillis() + TRANSIENT_ALERT_DURATION_MILLIS
                    )
                )
            }
            is NetworkAlertDecision.ShowOfflineAlert -> {
                IslandStateManager.clearLiveActivity(AIRPLANE_ACTIVITY_ID)
                IslandStateManager.postLiveActivity(
                    LiveActivityInfo(
                        id = MOBILE_DATA_ACTIVITY_ID,
                        kind = LiveActivityKind.MOBILE_DATA_ALERT,
                        title = decision.title,
                        subtitle = decision.subtitle,
                        isExpanded = true,
                        expandedByUser = false,
                        expiresAtMillis = System.currentTimeMillis() + TRANSIENT_ALERT_DURATION_MILLIS
                    )
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun readCurrentNetworkState(): NetworkConnectivityState {
        val cm = connectivityManager ?: return NetworkConnectivityState()
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        var isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        var isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        var isWifiValidated = isWifi && caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        var isCellularValidated = isCellular && caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        if (!isWifi && !isCellular) {
            for (network in cm.allNetworks) {
                val netCaps = cm.getNetworkCapabilities(network) ?: continue
                if (netCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    isWifi = true
                    if (netCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        isWifiValidated = true
                    }
                }
                if (netCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    isCellular = true
                    if (netCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        isCellularValidated = true
                    }
                }
            }
        }

        val isAirplane = readAirplaneMode()
        val isCellularDataEnabled = readMobileDataEnabled()

        return NetworkConnectivityState(
            isWifiConnected = isWifi,
            isWifiValidated = isWifiValidated,
            isCellularConnected = isCellular,
            isCellularValidated = isCellularValidated,
            isCellularDataEnabled = isCellularDataEnabled,
            isAirplaneMode = isAirplane
        )
    }

    private fun readAirplaneMode(): Boolean = runCatching {
        Settings.Global.getInt(
            appContext.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }.getOrDefault(false)

    private fun readMobileDataEnabled(): Boolean? {
        if (ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) return null

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.getSystemService(TelephonyManager::class.java)?.isDataEnabled
            } else {
                Settings.Global.getInt(appContext.contentResolver, "mobile_data", 0) == 1
            }
        }.getOrNull()
    }

    companion object {
        const val TRANSIENT_ALERT_DURATION_MILLIS = 4000L
        const val AIRPLANE_ACTIVITY_ID = "system-airplane-mode"
        const val MOBILE_DATA_ACTIVITY_ID = "system-mobile-data"
    }
}
