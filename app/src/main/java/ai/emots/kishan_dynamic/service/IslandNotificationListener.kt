package ai.emots.kishan_dynamic.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Android NotificationListenerService implementation that catches incoming alerts and
 * dispatches them to the Dynamic Island engine.
 */
class IslandNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var preferences: AuroraPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = AuroraPreferences(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Ignore our own notifications
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Skip persistent ongoing system notifications unless it's media or call
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val notifInfo = NotificationInfo(
            id = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = title ?: appName,
            text = text ?: "",
            timestamp = sbn.postTime,
            isPriority = notification.priority >= Notification.PRIORITY_HIGH,
            category = notification.category ?: "general"
        )

        scope.launch {
            val isEnabled = preferences.islandEnabled.first()
            if (!isEnabled) return@launch

            val autoExpand = preferences.autoExpand.first()
            val durationSeconds = preferences.displayDuration.first()

            IslandStateManager.postNotification(
                notification = notifInfo,
                autoExpand = autoExpand,
                displaySeconds = durationSeconds
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
