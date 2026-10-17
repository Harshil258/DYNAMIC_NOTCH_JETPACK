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
import kotlinx.coroutines.SupervisorJob
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

    private val listenerJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + listenerJob)
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
                if (snapshot == null) {
                    IslandStateManager.stopMusic()
                    return@collectLatest
                }
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
        val currentNotifications = runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray())
        android.util.Log.i(NotificationDiagnostics.TAG, "event=listener_connected snapshotSize=${currentNotifications.size}")
        IslandStateManager.reconcileNotificationSources(currentNotifications.mapTo(mutableSetOf()) { it.key })
        currentNotifications.forEach { processNotification(it, restored = true) }
    }

    override fun onListenerDisconnected() {
        sourceEpochs.clear()
        knownImportance.clear()
        android.util.Log.i(NotificationDiagnostics.TAG, "event=listener_disconnected")
        instance = null
        mappedLiveActivityIds.toList().forEach(IslandStateManager::clearLiveActivity)
        mappedLiveActivityIds.clear()
        IslandStateManager.clearNotificationSources()
        NotificationActionRegistry.clear()
        MediaPlaybackRegistry.clear()
        IslandStateManager.stopMusic()
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
        sourceEpochs.clear()
        knownImportance.clear()
        teardownMediaSessionListener()
        mappedLiveActivityIds.toList().forEach(IslandStateManager::clearLiveActivity)
        mappedLiveActivityIds.clear()
        IslandStateManager.clearNotificationSources()
        NotificationActionRegistry.clear()
        MediaPlaybackRegistry.clear()
        IslandStateManager.stopMusic()
        listenerJob.cancel()
        if (instance == this) {
            instance = null
        }
        PermissionUtils.sendPermissionsChangedBroadcast(this)
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

        MediaPlaybackRegistry.replace(activeController.sessionToken)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        processNotification(sbn, restored = false)
    }

    private val sourceEpochs = ai.emots.kishan_dynamic.data.notification.NotificationSourceEpochs()
    private val knownImportance = mutableMapOf<String, Int>()

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        if (rankingMap == null || instance !== this) return
        val visibleSources = IslandStateManager.notificationSourceIds()
        runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray()).forEach { source ->
            val previous = knownImportance[source.key] ?: return@forEach
            val ranking = Ranking()
            if (rankingMap.getRanking(source.key, ranking) && ranking.importance != previous) {
                NotificationDiagnostics.lifecycle("ranking", source.key, "importance_changed")
                processNotification(source, restored = source.key !in visibleSources)
            }
        }
    }

    private fun processNotification(sbn: StatusBarNotification?, restored: Boolean) {
        if (sbn == null) return

        // Ignore our own notifications
        if (sbn.packageName == packageName) return
        val sourceEpoch = sourceEpochs.begin(sbn.key)

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Skip persistent system noise, while retaining useful live activities
        // such as media, calls, downloads, and chronometers.
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT
        val hasProgress = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) > 0 ||
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


        // Skip empty group summary notifications that lack content
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val rawBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        if (isGroupSummary && rawTitle.isEmpty() && rawText.isEmpty() && rawBigText.isEmpty() && clockKind == null && !isMedia && !isCall) {
            return
        }

        val rawAppName = try {
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }
        val appName = sanitizeAppName(rawAppName, sbn.packageName)
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
        val template = extras.getString(Notification.EXTRA_TEMPLATE).orEmpty()

        val actionIntents = mutableMapOf<String, NotificationActionRegistry.Target>()
        val actions = notification.actions.orEmpty().mapIndexedNotNull { index, action ->
            val label = action.title?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapIndexedNotNull null
            val actionIntent = action.actionIntent ?: return@mapIndexedNotNull null
            // Index alone can dispatch a different action after a source reorder.
            val actionId = NotificationActionRegistry.idFor(sbn.key, index, actionIntent)
            if (!action.remoteInputs.isNullOrEmpty() &&
                action.remoteInputs.none { it.allowFreeFormInput }) return@mapIndexedNotNull null
            actionIntents[actionId] = NotificationActionRegistry.Target(
                pendingIntent = actionIntent,
                remoteInputs = action.remoteInputs?.toList().orEmpty()
            )
            NotificationActionInfo(
                id = actionId,
                label = label,
                isReply = action.remoteInputs?.any { it.allowFreeFormInput } == true
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
        val ranking = android.service.notification.NotificationListenerService.Ranking()
        val ranked = runCatching { currentRanking.getRanking(sbn.key, ranking) }.getOrDefault(false)
        val importance = if (ranked) ranking.importance.coerceIn(0, 5) else 3
        knownImportance[sbn.key] = importance
        if (ranked && importance == 0) {
            NotificationDiagnostics.lifecycle("suppressed", sbn.key, "blocked_ranking")
            sourceEpochs.remove(sbn.key)
            IslandStateManager.clearLiveActivity(sbn.key)
            IslandStateManager.removeNotification(sbn.key)
            NotificationActionRegistry.remove(sbn.key)
            return
        }
        var notifInfo = NotificationInfo(
            id = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = content.title,
            text = content.compactText,
            expandedText = content.expandedText,
            subText = content.subText,
            inboxLines = content.inboxLines,
            timestamp = sbn.postTime,
            isPriority = (if (ranked) importance >= 4 else notification.priority >= Notification.PRIORITY_HIGH) ||
                category == Notification.CATEGORY_CALL ||
                category == Notification.CATEGORY_ALARM ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    category == Notification.CATEGORY_NAVIGATION),
            category = category,
            groupKey = sbn.groupKey ?: sbn.packageName,
            isGroupSummary = isGroupSummary,
            isOngoing = isOngoing,
            isClearable = sbn.isClearable,
            progress = extras.getInt(Notification.EXTRA_PROGRESS, 0).coerceIn(0,
                extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0).coerceAtLeast(0)),
            progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0).coerceAtLeast(0),
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
            template = template,
            isSensitive = ai.emots.kishan_dynamic.data.notification.NotificationSensitiveContentPolicy.requiresProtection(
                notification.visibility != Notification.VISIBILITY_PUBLIC, content.title, content.expandedText
            ),
            hasContentIntent = notification.contentIntent != null,
            actions = resolvedActions,
            importance = importance
        )

        // Classify first: navigation, recording and other live work may have
        // neither a progress bar nor a chronometer. Ignore only unrecognized noise.
        val eventDecision = ai.emots.kishan_dynamic.data.notification.NotificationEventPolicy.evaluate(
            notifInfo, restored, NotificationLiveActivityMapper.map(notifInfo) != null
        )
        NotificationDiagnostics.decision(notifInfo, eventDecision, restored)
        if (eventDecision.kind == ai.emots.kishan_dynamic.data.notification.NotificationEventKind.IGNORED) {
            IslandStateManager.clearLiveActivity(notifInfo.id)
            IslandStateManager.removeNotification(notifInfo.id)
            NotificationActionRegistry.remove(notifInfo.id)
            sourceEpochs.remove(notifInfo.id)
            return
        }
        // Ignored foreground noise must not decode or write artwork on the UI thread.
        notifInfo = notifInfo.copy(imagePath = cacheNotificationAvatar(sbn.key, extras),
            picturePath = cacheNotificationPicture(sbn.key, extras))

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
                        if (!sourceEpochs.isCurrent(sbn.key, sourceEpoch)) return@launch
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
            if (!sourceEpochs.isCurrent(sbn.key, sourceEpoch)) {
                NotificationDiagnostics.lifecycle("dropped", sbn.key, "obsolete_callback")
                return@launch
            }

            (if (eventDecision.kind == ai.emots.kishan_dynamic.data.notification.NotificationEventKind.ONGOING)
                NotificationLiveActivityMapper.map(notifInfo) else null)?.let { activity ->
                mappedLiveActivityIds += activity.id
                IslandStateManager.postNotificationBackedLiveActivity(activity)
            } ?: run {
                mappedLiveActivityIds.remove(notifInfo.id)
                IslandStateManager.clearLiveActivity(notifInfo.id)
            }

            val isSilentProgressUpdate = notifInfo.isOngoing && notifInfo.progressMax > 0
            IslandStateManager.postNotification(
                notification = notifInfo,
                autoExpand = if (eventDecision.kind == ai.emots.kishan_dynamic.data.notification.NotificationEventKind.ONGOING || isSilentProgressUpdate) false else autoExpand,
                displaySeconds = durationSeconds
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        sourceEpochs.remove(sbn.key)
        knownImportance.remove(sbn.key)
        NotificationDiagnostics.lifecycle("removed", sbn.key, "source_removed")
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

    private fun cacheNotificationAvatar(notificationId: String, extras: android.os.Bundle): String? {
        val bitmap = readBitmapExtra(extras, Notification.EXTRA_LARGE_ICON_BIG)
            ?: readBitmapExtra(extras, Notification.EXTRA_LARGE_ICON)
            ?: readIconExtra(extras, Notification.EXTRA_LARGE_ICON_BIG)?.let { drawable -> drawable.toBitmap() }
            ?: readIconExtra(extras, Notification.EXTRA_LARGE_ICON)?.let { drawable -> drawable.toBitmap() }
            ?: extractMessagingSenderIcon(extras)
            ?: return null

        return saveBitmapToCache(bitmap, "${notificationId.hashCode()}_avatar.png")
    }

    private fun cacheNotificationPicture(notificationId: String, extras: android.os.Bundle): String? {
        val bitmap = readBitmapExtra(extras, Notification.EXTRA_PICTURE)
            ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                readIconExtra(extras, Notification.EXTRA_PICTURE_ICON)?.let { it.toBitmap() }
            } else null)
            ?: return null

        return saveBitmapToCache(bitmap, "${notificationId.hashCode()}_picture.png")
    }

    private fun extractMessagingSenderIcon(extras: android.os.Bundle): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            @Suppress("DEPRECATION")
            val person = extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON) as? android.app.Person
            person?.icon?.loadDrawable(this)?.toBitmap()
        }.getOrNull()
    }

    private fun saveBitmapToCache(bitmap: Bitmap, filename: String): String? {
        val directory = File(cacheDir, "notification_artwork").apply { mkdirs() }
        val file = File(directory, filename)
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
        val hash = notificationId.hashCode()
        File(cacheDir, "notification_artwork/${hash}.png").delete()
        File(cacheDir, "notification_artwork/${hash}_avatar.png").delete()
        File(cacheDir, "notification_artwork/${hash}_picture.png").delete()
    }

    @Suppress("DEPRECATION")
    private fun readBitmapExtra(extras: android.os.Bundle, key: String): Bitmap? =
        extras.get(key) as? Bitmap

    @Suppress("DEPRECATION")
    private fun readIconExtra(extras: android.os.Bundle, key: String): Drawable? {
        val icon = extras.get(key) as? android.graphics.drawable.Icon
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

    private fun sanitizeAppName(rawLabel: String, packageName: String): String {
        if (rawLabel.isNotBlank() && !rawLabel.contains(".")) return rawLabel
        val meaningfulParts = packageName.split('.')
            .filter { it.isNotBlank() && it !in setOf("com", "org", "net", "io", "android", "apps", "app") }
        if (meaningfulParts.isEmpty()) return rawLabel.ifBlank { packageName }
        return meaningfulParts.joinToString(" ") { part ->
            part.replace('_', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { w ->
                    w.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
                }
        }
    }
}
