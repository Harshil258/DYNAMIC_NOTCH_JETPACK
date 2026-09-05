package ai.emots.kishan_dynamic.service

import android.content.Context
import android.os.Build
import ai.emots.kishan_dynamic.data.liveactivity.satelliteActivityFor
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Optional Android 16 satellite boundary.
 *
 * The project intentionally compiles against Android 15, so this adapter uses
 * a narrow reflection boundary. Older devices and OEMs without the service are
 * silent; notification-backed satellite surfaces remain available separately.
 */
class SatelliteActivityMonitor(context: Context) {
    private val appContext = context.applicationContext
    private var manager: Any? = null
    private var listener: Any? = null
    private var unregisterMethod: Method? = null

    fun start() {
        if (listener != null || Build.VERSION.SDK_INT < ANDROID_16) return

        runCatching {
            val managerClass = Class.forName("android.telephony.satellite.SatelliteManager")
            val listenerClass = Class.forName("android.telephony.satellite.SatelliteStateChangeListener")
            val satelliteManager = appContext.getSystemService("satellite") ?: return@runCatching
            val stateListener = Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, args ->
                if (method.name == "onEnabledStateChanged") {
                    val enabled = args?.firstOrNull() as? Boolean ?: false
                    satelliteActivityFor(enabled)?.let(IslandStateManager::postLiveActivity)
                        ?: IslandStateManager.clearLiveActivity(ACTIVITY_ID)
                }
                null
            }
            val registerMethod = managerClass.getMethod(
                "registerStateChangeListener",
                java.util.concurrent.Executor::class.java,
                listenerClass
            )
            val unregister = managerClass.getMethod("unregisterStateChangeListener", listenerClass)
            registerMethod.invoke(satelliteManager, appContext.mainExecutor, stateListener)
            manager = satelliteManager
            listener = stateListener
            unregisterMethod = unregister
        }.onFailure {
            manager = null
            listener = null
            unregisterMethod = null
        }
    }

    fun close() {
        val currentManager = manager
        val currentListener = listener
        if (currentManager != null && currentListener != null) {
            runCatching { unregisterMethod?.invoke(currentManager, currentListener) }
        }
        manager = null
        listener = null
        unregisterMethod = null
        IslandStateManager.clearLiveActivity(ACTIVITY_ID)
    }

    private companion object {
        const val ANDROID_16 = 36
        const val ACTIVITY_ID = "system-satellite"
    }
}
