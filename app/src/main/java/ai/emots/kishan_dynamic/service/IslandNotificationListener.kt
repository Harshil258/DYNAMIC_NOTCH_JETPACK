package ai.emots.kishan_dynamic.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.TextView
import java.io.File
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.liveactivity.NotificationLiveActivityMapper
import ai.emots.kishan_dynamic.data.notification.ClockNotificationKind
import ai.emots.kishan_dynamic.data.notification.NotificationChronometerPolicy
import ai.emots.kishan_dynamic.data.notification.CallNotificationPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationDisplayDurationPolicy
import ai.emots.kishan_dynamic.data.notification.classifyClockNotification
import ai.emots.kishan_dynamic.data.notification.resolveNotificationContent
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Android NotificationListenerService implementation that catches incoming alerts and
 * dispatches them to the Dynamic Island engine.
 */
class IslandNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "IslandNotificationListener"

        @Volatile
        var instance: IslandNotificationListener? = null
            private set
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private val mappedLiveActivityIds = mutableSetOf<String>()
    private lateinit var preferences: AuroraPreferences
    private var mediaSessionManager: MediaSessionManager? = null

    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handleActiveSessionsChanged(controllers)
    }

    override fun onCreate() {
        super.onCreate()
        preferences = AuroraPreferences(applicationContext)
        MediaPlaybackRegistry.initialize(applicationContext)
        scope.launch {
            MediaPlaybackRegistry.snapshot.collectLatest { snapshot ->
                snapshot ?: return@collectLatest
                if (!preferences.musicIslandEnabled.first()) {
                    IslandStateManager.stopMusic(snapshot.packageName)
                    return@collectLatest
                }
                IslandStateManager.postMusicPlayback(
                    track = MusicTrack(
                        title = snapshot.title,
                        artist = snapshot.artist,
                        packageName = snapshot.packageName,
                        durationMs = snapshot.durationMs,
                        positionMs = snapshot.positionMs,
                        albumArtUri = snapshot.albumArtUri
                    ),
                    isPlaying = snapshot.isPlaying
                )
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        PermissionUtils.sendPermissionsChangedBroadcast(this)
        setupMediaSessionListener()
        // Rehydrate the reducer after the listener process/service reconnects;
        // Android does not guarantee that every existing notification is
        // reposted as a new callback.
        activeNotifications.orEmpty().forEach(::onNotificationPosted)
    }

    override fun onListenerDisconnected() {
        instance = null
        mappedLiveActivityIds.toList().forEach(IslandStateManager::clearLiveActivity)
        mappedLiveActivityIds.clear()
        teardownMediaSessionListener()
        super.onListenerDisconnected()
        PermissionUtils.sendPermissionsChangedBroadcast(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching {
                requestRebind(ComponentName(this, IslandNotificationListener::class.java))
            }
        }
    }

    override fun onDestroy() {
        teardownMediaSessionListener()
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    private fun setupMediaSessionListener() {
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, IslandNotificationListener::class.java)
            mediaSessionManager?.addOnActiveSessionsChangedListener(activeSessionsListener, componentName)
            val initialSessions = mediaSessionManager?.getActiveSessions(componentName)
            handleActiveSessionsChanged(initialSessions)
            Log.d(TAG, "MediaSessionListener setup complete, initial sessions count=${initialSessions?.size ?: 0}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup MediaSessionListener", e)
        }
    }

    private fun teardownMediaSessionListener() {
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(activeSessionsListener)
        } catch (e: Exception) {
            // ignore
        }
        mediaSessionManager = null
    }

    private fun handleActiveSessionsChanged(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            MediaPlaybackRegistry.clear()
            IslandStateManager.stopMusic()
            return
        }

        val activeController = controllers.firstOrNull { controller ->
            val state = controller.playbackState?.state
            state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
        } ?: controllers.first()

        activeController.sessionToken?.let { token ->
            MediaPlaybackRegistry.replace(token)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Ignore our own notifications
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Skip persistent system noise, while retaining useful live activities
        // such as media, calls, downloads, and chronometers.
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT
        val hasProgress = extras.containsKey(Notification.EXTRA_PROGRESS) ||
            extras.containsKey(Notification.EXTRA_PROGRESS_MAX) ||
            extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
        val showChronometer = extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false)
        val chronometerCountDown = extras.getBoolean("android.chronometerCountDown", false)
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notification.channelId else null
        val callType = extras.getInt("android.callType", 0)
        val isCall = notification.category == Notification.CATEGORY_CALL || callType > 0
        val clockKind = classifyClockNotification(
            packageName = sbn.packageName,
            channelId = channelId,
            hasCustomContent = notification.contentView != null
        )
        val customClockCopy = clockKind?.let { extractCustomClockCopy(notification, it) }

        if (isOngoing && !isMedia && !isCall && !hasProgress && !showChronometer && clockKind == null) return

        // Skip empty group summary notifications that lack content
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val rawBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        if (isGroupSummary && rawTitle.isEmpty() && rawText.isEmpty() && rawBigText.isEmpty() && clockKind == null && !isMedia && !isCall) {
            return
        }

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }
        val content = resolveNotificationContent(
            appName = appName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: customClockCopy?.title,
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: customClockCopy?.body,
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
            conversationTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            } else null,
            inboxLines = extras.getCharSequenceArray("android.textLines")
                ?.mapNotNull { it?.toString() }
                .orEmpty()
        )
        val imagePath = cacheNotificationImage(sbn.key, extras)
        val template = extras.getString(Notification.EXTRA_TEMPLATE).orEmpty()

        val actionIntents = mutableMapOf<String, NotificationActionRegistry.Target>()
        val actions = notification.actions.orEmpty().mapIndexedNotNull { index, action ->
            val label = action.title?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapIndexedNotNull null
            val actionId = index.toString()
            action.actionIntent?.let {
                actionIntents[actionId] = NotificationActionRegistry.Target(
                    pendingIntent = it,
                    remoteInputs = action.remoteInputs?.toList().orEmpty()
                )
            }
            NotificationActionInfo(
                id = actionId,
                label = label,
                isReply = !action.remoteInputs.isNullOrEmpty()
            )
        }
        notification.contentIntent?.let {
            actionIntents["open"] = NotificationActionRegistry.Target(it)
        }
        val resolvedActions = if (actions.isEmpty() && notification.contentIntent != null) {
            listOf(NotificationActionInfo(id = "open", label = "Open"))
        } else {
            actions
        }
        NotificationActionRegistry.replace(sbn.key, actionIntents)

        val category = notification.category ?: clockKind?.category ?: if (callType > 0) Notification.CATEGORY_CALL else "general"
        val notifInfo = NotificationInfo(
            id = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = content.title,
            text = content.compactText,
            expandedText = content.expandedText,
            subText = content.subText,
            inboxLines = content.inboxLines,
            timestamp = sbn.postTime,
            isPriority = notification.priority >= Notification.PRIORITY_HIGH ||
                category == Notification.CATEGORY_CALL ||
                category == Notification.CATEGORY_MESSAGE ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    category == Notification.CATEGORY_NAVIGATION),
            category = category,
            groupKey = sbn.groupKey ?: sbn.packageName,
            isOngoing = isOngoing,
            isClearable = sbn.isClearable,
            progress = extras.getInt(Notification.EXTRA_PROGRESS, 0),
            progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0),
            isProgressIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false),
            showChronometer = showChronometer || customClockCopy?.hasChronometer == true,
            chronometerBaseElapsedRealtime = customClockCopy?.chronometerBaseElapsedRealtime
                ?.takeIf { it > 0L }
                ?: NotificationChronometerPolicy.baseFromWallClock(
                    notificationWhenMillis = notification.`when`,
                    nowWallClockMillis = System.currentTimeMillis(),
                    nowElapsedRealtimeMillis = SystemClock.elapsedRealtime(),
                    enabled = showChronometer
                ),
            chronometerCountDown = customClockCopy?.chronometerCountDown ?: chronometerCountDown,
            channelId = channelId.orEmpty(),
            imagePath = imagePath,
            template = template,
            hasContentIntent = notification.contentIntent != null,
            actions = resolvedActions
        )

        val incomingCallActionIds = CallNotificationPolicy.incomingActionIds(notifInfo)
        val acceptActionId = incomingCallActionIds.first
        if ((CallNotificationPolicy.isIncoming(notifInfo) || callType == 1) && acceptActionId != null) {
            IslandStateManager.postNotificationIncomingCall(
                contact = ContactInfo(
                    name = notifInfo.title.ifBlank { notifInfo.appName },
                    phoneNumber = "",
                    avatarUri = notifInfo.imagePath
                ),
                notificationId = notifInfo.id,
                acceptActionId = acceptActionId,
                declineActionId = incomingCallActionIds.second
            )
            return
        }

        // A CallStyle source owns the incoming island only while it exposes an
        // answer action. Once the source becomes ongoing/connected, or stops
        // advertising call actions, release that transient island so the
        // native telephony producer or normal notification reducer can take
        // over.
        IslandStateManager.resolveNotificationIncomingCall(notifInfo.id)

        // Real-life edge case: If a missed-call alert arrives, any pending or ongoing call has ended
        val isMissedCall = notifInfo.category.equals("missed_call", ignoreCase = true) ||
            notifInfo.title.contains("Missed call", ignoreCase = true) ||
            notifInfo.text.contains("Missed call", ignoreCase = true)
        if (isMissedCall) {
            IslandStateManager.resolveNotificationIncomingCall(notifInfo.id)
            IslandStateManager.resolveNotificationOngoingCall(notifInfo.id)
        }

        val ongoingActionId = CallNotificationPolicy.ongoingActionId(notifInfo)
        if ((CallNotificationPolicy.isOngoing(notifInfo) || callType == 2) && ongoingActionId != null) {
            val current = IslandStateManager.currentState.value
            if (current !is ai.emots.kishan_dynamic.data.model.IslandState.OngoingCall ||
                current.sourceNotificationId != null
            ) {
                IslandStateManager.postNotificationOngoingCall(
                    contact = ContactInfo(
                        name = notifInfo.title.ifBlank { notifInfo.appName },
                        phoneNumber = "",
                        avatarUri = notifInfo.imagePath
                    ),
                    notificationId = notifInfo.id,
                    endActionId = ongoingActionId,
                    isDialing = CallNotificationPolicy.isDialing(notifInfo)
                )
            } else {
                IslandStateManager.updateCallDialing(CallNotificationPolicy.isDialing(notifInfo))
            }
            return
        } else if (CallNotificationPolicy.isOngoing(notifInfo) || callType == 2) {
            // VoIP call ended remotely (no more ongoing hang-up actions)
            IslandStateManager.resolveNotificationOngoingCall(notifInfo.id)
        }

        // Call notifications are the authoritative source for the short
        // outgoing-call phase on devices that expose "Calling", "Dialing",
        // or "Ringing" copy. Reconcile only an existing telephony call so a
        // normal notification can never manufacture a call state.
        if (CallNotificationPolicy.isCall(notifInfo) && resolvedActions.isNotEmpty()) {
            IslandStateManager.updateCallDialing(
                isDialing = CallNotificationPolicy.isDialing(notifInfo)
            )
        }

        scope.launch {
            val isEnabled = preferences.islandEnabled.first()
            if (!isEnabled) return@launch

            if (isMedia) {
                val track = mediaTrack(notification, sbn.packageName)
                if (track != null) {
                    if (preferences.musicIslandEnabled.first()) {
                        IslandStateManager.postMusicPlayback(track, mediaIsPlaying(notification))
                    }
                    return@launch
                }
            }

            if (content.title.isBlank() && content.compactText.isBlank() && content.expandedText.isBlank()) return@launch

            val autoExpand = preferences.autoExpand.first()
            val durationSeconds = NotificationDisplayDurationPolicy.sanitize(
                preferences.displayDuration.first()
            )

            NotificationLiveActivityMapper.map(notifInfo)?.let { activity ->
                mappedLiveActivityIds += activity.id
                IslandStateManager.postNotificationBackedLiveActivity(activity)
            } ?: run {
                mappedLiveActivityIds.remove(notifInfo.id)
                IslandStateManager.clearLiveActivity(notifInfo.id)
            }

            val isSilentProgressUpdate = notifInfo.isOngoing && notifInfo.progressMax > 0
            IslandStateManager.postNotification(
                notification = notifInfo,
                autoExpand = if (isSilentProgressUpdate) false else autoExpand,
                displaySeconds = durationSeconds
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        NotificationActionRegistry.remove(sbn.key)
        IslandStateManager.resolveNotificationIncomingCall(sbn.key)
        IslandStateManager.resolveNotificationOngoingCall(sbn.key)
        if (sbn.notification?.category == Notification.CATEGORY_TRANSPORT) {
            MediaPlaybackRegistry.stopIfPackage(sbn.packageName)
            IslandStateManager.stopMusic(sbn.packageName)
        }
        removeCachedNotificationImage(sbn.key)
        mappedLiveActivityIds.remove(sbn.key)
        IslandStateManager.clearLiveActivity(sbn.key)
        IslandStateManager.removeNotification(sbn.key)
    }

    private data class CustomClockCopy(
        val title: String,
        val body: String?,
        val hasChronometer: Boolean,
        val chronometerBaseElapsedRealtime: Long,
        val chronometerCountDown: Boolean
    )

    private fun extractCustomClockCopy(
        notification: Notification,
        kind: ClockNotificationKind
    ): CustomClockCopy? {
        val contentView = notification.contentView ?: return null
        return runCatching {
            val root = contentView.apply(this, FrameLayout(this))
            val texts = mutableListOf<String>()
            var hasChronometer = false
            var chronometerBaseElapsedRealtime = 0L
            var chronometerCountDown = false

            fun collect(view: View) {
                when (view) {
                    is Chronometer -> {
                        hasChronometer = true
                        chronometerBaseElapsedRealtime = view.base
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            chronometerCountDown = view.isCountDown
                        }
                        view.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(texts::add)
                    }
                    is TextView -> {
                        view.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(texts::add)
                    }
                }
                if (view is ViewGroup) {
                    for (index in 0 until view.childCount) {
                        collect(view.getChildAt(index))
                    }
                }
            }

            collect(root)
            if (!hasChronometer && texts.isEmpty()) return@runCatching null

            CustomClockCopy(
                title = kind.fallbackTitle,
                body = texts.joinToString(" · ").ifBlank { null },
                hasChronometer = hasChronometer,
                chronometerBaseElapsedRealtime = chronometerBaseElapsedRealtime,
                chronometerCountDown = chronometerCountDown
            )
        }.getOrNull()
    }

    private fun mediaTrack(notification: Notification, packageName: String): MusicTrack? {
        val extras = notification.extras ?: return null
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
        }
        token?.let(MediaPlaybackRegistry::replace)
        val metadata = token?.let { runCatching { MediaController(this, it).metadata }.getOrNull() }
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (title.isNullOrBlank()) return null
        return MusicTrack(
            title = title,
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            packageName = packageName,
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            positionMs = token?.let { runCatching { MediaController(this, it).playbackState?.position ?: 0L }.getOrDefault(0L) }
                ?: 0L,
            albumArtUri = metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
        )
    }

    private fun cacheNotificationImage(notificationId: String, extras: android.os.Bundle): String? {
        val bitmap = readBitmapExtra(extras, Notification.EXTRA_PICTURE)
            ?: readBitmapExtra(extras, Notification.EXTRA_LARGE_ICON_BIG)
            ?: readBitmapExtra(extras, Notification.EXTRA_LARGE_ICON)
            ?: readIconExtra(extras)?.let { drawable -> drawable.toBitmap() }
            ?: return null

        val directory = File(cacheDir, "notification_artwork").apply { mkdirs() }
        val file = File(directory, "${notificationId.hashCode()}.png")
        return runCatching {
            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            directory.listFiles()
                ?.sortedByDescending { it.lastModified() }
                ?.drop(40)
                ?.forEach { it.delete() }
            file.absolutePath
        }.getOrNull()
    }

    private fun removeCachedNotificationImage(notificationId: String) {
        File(cacheDir, "notification_artwork/${notificationId.hashCode()}.png").delete()
    }

    private fun readBitmapExtra(extras: android.os.Bundle, key: String): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(key, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(key) as? Bitmap
        }

    private fun readIconExtra(extras: android.os.Bundle): Drawable? {
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_LARGE_ICON_BIG, android.graphics.drawable.Icon::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_LARGE_ICON_BIG) as? android.graphics.drawable.Icon
        }
        return runCatching { icon?.loadDrawable(this) }.getOrNull()
    }

    private fun Drawable.toBitmap(): Bitmap {
        val width = intrinsicWidth.coerceAtLeast(1)
        val height = intrinsicHeight.coerceAtLeast(1)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }

    private fun mediaIsPlaying(notification: Notification): Boolean {
        val extras = notification.extras ?: return true
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
        }
        val state = token?.let { runCatching { MediaController(this, it).playbackState?.state }.getOrNull() }
        return state == null || state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
    }
}
