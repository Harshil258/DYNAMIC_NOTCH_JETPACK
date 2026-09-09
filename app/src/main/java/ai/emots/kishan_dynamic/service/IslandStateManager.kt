package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.IslandPresentationOwnershipPolicy
import ai.emots.kishan_dynamic.data.battery.BatterySurfacePolicy
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.liveactivity.LiveActivityPrioritizer
import ai.emots.kishan_dynamic.data.liveactivity.LiveActivitySurfacePolicy
import ai.emots.kishan_dynamic.data.liveactivity.LiveActivitySourceActionPolicy
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.notification.NotificationEvent
import ai.emots.kishan_dynamic.data.notification.NotificationInterruptionPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationPresentationDecision
import ai.emots.kishan_dynamic.data.notification.NotificationQueueState
import ai.emots.kishan_dynamic.data.notification.NotificationReducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Thread-safe State Manager & Priority Reducer for Dynamic Island.
 */
object IslandStateManager {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var dismissTimerJob: Job? = null
    private var callDurationJob: Job? = null
    private val liveActivityExpiryJobs = mutableMapOf<String, Job>()

    private val _currentState = MutableStateFlow<IslandState>(IslandState.Minimal)
    val currentState: StateFlow<IslandState> = _currentState.asStateFlow()

    // Cached sub-states to resume when transient overlays expire
    private var activeMusicTrack: MusicTrack? = null
    private var isMusicPlaying: Boolean = false
    private var activeCallContact: ContactInfo? = null
    private var callTimerEnabled = true
    private var pendingIncomingCall: ContactInfo? = null
    private var callStartedAtMillis: Long = 0L
    private var pendingCallSummary: ai.emots.kishan_dynamic.data.model.CallRecord? = null
    private var interruptedByIncomingCall: IslandState? = null
    private var interruptedOngoingCall: IslandState.OngoingCall? = null
    private var interruptedByCriticalNotification: IslandState? = null
    private val activeLiveActivities = linkedMapOf<String, LiveActivityInfo>()
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()
    private val _observedMediaVolume = MutableStateFlow<Float?>(null)
    val observedMediaVolume: StateFlow<Float?> = _observedMediaVolume.asStateFlow()
    private val _observedRingerVolume = MutableStateFlow<Float?>(null)
    val observedRingerVolume: StateFlow<Float?> = _observedRingerVolume.asStateFlow()
    private val notificationReducer = NotificationReducer()
    private var notificationQueue = NotificationQueueState()
    private var notificationAutoExpand = false
    private var notificationDisplaySeconds = 5
    private val specializedNotificationIds = mutableSetOf<String>()

    fun postIncomingCall(contact: ContactInfo) {
        rememberPresentationBeforeIncomingCall()
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        pendingCallSummary = null
        pendingIncomingCall = contact
        if (interruptedOngoingCall == null) activeCallContact = null
        _currentState.value = IslandState.IncomingCall(contact)
    }

    fun postNotificationIncomingCall(
        contact: ContactInfo,
        notificationId: String,
        acceptActionId: String,
        declineActionId: String?
    ) {
        rememberPresentationBeforeIncomingCall()
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        pendingCallSummary = null
        pendingIncomingCall = contact
        if (interruptedOngoingCall == null) activeCallContact = null
        _currentState.value = IslandState.IncomingCall(
            contact = contact,
            sourceNotificationId = notificationId,
            acceptActionId = acceptActionId,
            declineActionId = declineActionId
        )
    }

    /** Clears a notification-backed call while its source app handles the action. */
    fun resolveNotificationIncomingCall(
        notificationId: String? = null,
        restoreInterrupted: Boolean = true
    ) {
        val current = _currentState.value as? IslandState.IncomingCall
        if (notificationId != null && current?.sourceNotificationId != notificationId) return
        pendingIncomingCall = null
        dismissTimerJob?.cancel()
        if (!restoreInterrupted ||
            (!restoreInterruptedOngoingCall() && !restoreInterruptedPresentation())
        ) {
            if (!restoreInterrupted) {
                interruptedByIncomingCall = null
                interruptedOngoingCall = null
            }
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun postNotificationOngoingCall(
        contact: ContactInfo,
        notificationId: String,
        endActionId: String,
        isDialing: Boolean
    ) {
        interruptedByIncomingCall = null
        interruptedOngoingCall = null
        activeCallContact = contact
        callTimerEnabled = true
        pendingIncomingCall = null
        callStartedAtMillis = if (isDialing) 0L else System.currentTimeMillis()
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        _currentState.value = IslandState.OngoingCall(
            contact = contact,
            durationSeconds = 0L,
            showDuration = true,
            isExpanded = false,
            isDialing = isDialing,
            sourceNotificationId = notificationId,
            endActionId = endActionId
        )
        if (!isDialing) startCallDurationTicker()
    }

    fun resolveNotificationOngoingCall(notificationId: String? = null) {
        val current = _currentState.value as? IslandState.OngoingCall
        if (notificationId != null && current?.sourceNotificationId != notificationId) return
        if (current?.sourceNotificationId == null && notificationId != null) return
        endCall()
    }

    fun postOngoingCall(
        contact: ContactInfo,
        durationSeconds: Long,
        showDuration: Boolean = true,
        isDialing: Boolean = false
    ) {
        interruptedByIncomingCall = null
        interruptedOngoingCall = null
        activeCallContact = contact
        callTimerEnabled = showDuration
        pendingIncomingCall = null
        callStartedAtMillis = if (isDialing) {
            0L
        } else {
            System.currentTimeMillis() - (durationSeconds.coerceAtLeast(0L) * 1000L)
        }
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        _currentState.value = IslandState.OngoingCall(
            contact = contact,
            durationSeconds = durationSeconds,
            showDuration = showDuration,
            isExpanded = false,
            isDialing = isDialing
        )
        if (!isDialing) {
            startCallDurationTicker()
        }
    }

    /** Reconciles call-style notification copy with the telephony state. */
    fun updateCallDialing(isDialing: Boolean) {
        val current = _currentState.value as? IslandState.OngoingCall ?: return
        if (current.isDialing == isDialing) return

        callDurationJob?.cancel()
        if (isDialing) {
            callStartedAtMillis = 0L
            _currentState.value = current.copy(isDialing = true, durationSeconds = 0L)
        } else {
            callStartedAtMillis = System.currentTimeMillis()
            _currentState.value = current.copy(isDialing = false, durationSeconds = 0L)
            startCallDurationTicker()
        }
    }

    private fun startCallDurationTicker() {
        callDurationJob = scope.launch {
            while (true) {
                delay(1000L)
                val current = _currentState.value as? IslandState.OngoingCall ?: break
                if (current.isDialing) continue
                val elapsed = ((System.currentTimeMillis() - callStartedAtMillis) / 1000L)
                    .coerceAtLeast(0L)
                _currentState.value = current.copy(durationSeconds = elapsed)
            }
        }
    }

    fun acceptIncomingCall() {
        pendingIncomingCall?.let {
            interruptedByIncomingCall = null
            interruptedOngoingCall = null
            postOngoingCall(it, durationSeconds = 0L, showDuration = callTimerEnabled)
        }
    }

    fun declineIncomingCall() {
        pendingIncomingCall = null
        callDurationJob?.cancel()
        callDurationJob = null
        dismissTimerJob?.cancel()
        if (restoreInterruptedOngoingCall()) return
        activeCallContact = null
        if (!restoreInterruptedPresentation()) {
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun endCall() {
        val wasCallActive = activeCallContact != null ||
            pendingIncomingCall != null ||
            _currentState.value is IslandState.IncomingCall ||
            _currentState.value is IslandState.OngoingCall
        activeCallContact = null
        pendingIncomingCall = null
        callDurationJob?.cancel()
        callDurationJob = null
        interruptedByIncomingCall = null
        interruptedOngoingCall = null
        if (!wasCallActive && IslandPresentationOwnershipPolicy.isUserOwned(_currentState.value)) return
        dismissTimerJob?.cancel()
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postCallSummary(record: ai.emots.kishan_dynamic.data.model.CallRecord) {
        pendingCallSummary = record
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.CallSummary(record)
        scheduleCallSummaryExpiry()
    }

    fun clearCallSummary() {
        pendingCallSummary = null
        dismissTimerJob?.cancel()
        dismissTimerJob = null
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postNotification(notification: NotificationInfo, autoExpand: Boolean, displaySeconds: Int) {
        notificationAutoExpand = autoExpand
        notificationDisplaySeconds = displaySeconds.coerceIn(1, 60)

        // Edge case: If a missed-call alert arrives while in an incoming or ongoing call state,
        // the caller has hung up or disconnected. Clean up the call so the missed call alert can display.
        val isMissedCall = notification.category.equals("missed_call", ignoreCase = true) ||
            notification.title.contains("Missed call", ignoreCase = true) ||
            notification.text.contains("Missed call", ignoreCase = true)
        if (isMissedCall && (pendingIncomingCall != null || _currentState.value is IslandState.IncomingCall)) {
            pendingIncomingCall = null
            activeCallContact = null
            callDurationJob?.cancel()
            callDurationJob = null
            if (!restoreInterruptedPresentation()) {
                _currentState.value = IslandState.Minimal
            }
        }

        val currentPresentation = _currentState.value
        val presentationDecision = NotificationInterruptionPolicy.decide(
            current = currentPresentation,
            incoming = notification
        )
        if (presentationDecision == NotificationPresentationDecision.PRESENT &&
            NotificationInterruptionPolicy.isCriticalInterruption(notification) &&
            IslandPresentationOwnershipPolicy.isUserOwned(currentPresentation) &&
            interruptedByCriticalNotification == null
        ) {
            interruptedByCriticalNotification = currentPresentation
        }
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Posted(
                notification = notification,
                isSilentUpdate = presentationDecision ==
                    NotificationPresentationDecision.UPDATE_EXPANDED_NOTIFICATION ||
                    (presentationDecision == NotificationPresentationDecision.DEFER &&
                        currentPresentation.visibleNotificationIsCritical())
            )
        )
        activeLiveActivities[notification.id]?.let { activity ->
            activeLiveActivities[notification.id] = activity.copy(
                sourceNotificationId = notification.id,
                sourceActionId = LiveActivitySourceActionPolicy.primaryActionId(notification),
                sourceActions = notification.actions,
                sourceHasContentIntent = notification.hasContentIntent
            )
            val visible = _currentState.value as? IslandState.LiveActivity
            if (visible?.activity?.id == notification.id) {
                _currentState.value = visible.copy(
                    activity = activeLiveActivities.getValue(notification.id)
                )
            }
        }
        applyNotificationPresentation(presentationDecision)
    }

    /** Keeps the source notification queued while its richer activity owns the island. */
    fun postNotificationBackedLiveActivity(activity: LiveActivityInfo) {
        specializedNotificationIds += activity.id
        postLiveActivity(activity.copy(sourceNotificationId = activity.id))
    }

    fun removeNotification(notificationId: String) {
        val removedWasVisibleCritical = notificationQueue.activeNotification?.let { active ->
            active.id == notificationId && NotificationInterruptionPolicy.isCriticalInterruption(active)
        } == true
        val presentationDecision = NotificationInterruptionPolicy.decide(_currentState.value)
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Removed(notificationId)
        )
        if (removedWasVisibleCritical && !hasQueuedCriticalNotification() &&
            restoreCriticalInterruptedPresentation()
        ) return
        applyNotificationPresentation(presentationDecision)
    }

    fun reconcileNotificationSources(activeNotificationIds: Set<String>) {
        val visibleCriticalId = notificationQueue.activeNotification
            ?.takeIf(NotificationInterruptionPolicy::isCriticalInterruption)
            ?.id
        val presentationDecision = NotificationInterruptionPolicy.decide(_currentState.value)
        notificationQueue.notifications
            .map { it.id }
            .filterNot(activeNotificationIds::contains)
            .forEach { notificationId ->
                notificationQueue = notificationReducer.reduce(
                    notificationQueue,
                    NotificationEvent.Removed(notificationId)
                )
                specializedNotificationIds.remove(notificationId)
            }
        if (visibleCriticalId != null && visibleCriticalId !in activeNotificationIds &&
            !hasQueuedCriticalNotification() && restoreCriticalInterruptedPresentation()
        ) return
        applyNotificationPresentation(presentationDecision)
    }

    fun clearNotificationSources() {
        val hadVisibleCritical = notificationQueue.activeNotification
            ?.let(NotificationInterruptionPolicy::isCriticalInterruption) == true
        val presentationDecision = NotificationInterruptionPolicy.decide(_currentState.value)
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Cleared
        )
        specializedNotificationIds.clear()
        if (hadVisibleCritical && restoreCriticalInterruptedPresentation()) return
        interruptedByCriticalNotification = null
        applyNotificationPresentation(presentationDecision)
    }

    fun selectNotification(notificationId: String) {
        val expanded = _currentState.value as? IslandState.Notification
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Selected(notificationId)
        )
        if (expanded?.isExpanded == true) {
            _currentState.value = expanded.copy(
                notifications = notificationQueue.notifications,
                activeIndex = notificationQueue.activeIndex
            )
        } else {
            renderNotificationSurface()
        }
    }

    fun selectNextNotification() {
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Next
        )
        renderNotificationSurface()
        scheduleNotificationExpiry()
    }

    fun selectPreviousNotification() {
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Previous
        )
        renderNotificationSurface()
        scheduleNotificationExpiry()
    }

    fun dismissActiveNotification(): String? {
        val active = notificationQueue.activeNotification ?: return null
        if (!active.isClearable) return null
        val activeId = active.id
        removeNotification(activeId)
        NotificationActionRegistry.remove(activeId)
        return activeId
    }

    fun postMusicPlayback(track: MusicTrack, isPlaying: Boolean) {
        activeMusicTrack = track
        isMusicPlaying = isPlaying

        if (IslandPresentationOwnershipPolicy.isUserOwned(_currentState.value) &&
            _currentState.value !is IslandState.Music
        ) return

        // Music is a cached base layer. Its callbacks only mutate a media-owned
        // presentation or an existing notification/media split.
        if (_currentState.value is IslandState.Minimal) {
            _currentState.value = IslandState.Music(
                track = track,
                isPlaying = isPlaying,
                isExpanded = false
            )
        } else if (_currentState.value is IslandState.Music) {
            val current = _currentState.value as IslandState.Music
            _currentState.value = current.copy(track = track, isPlaying = isPlaying)
        } else if (_currentState.value is IslandState.Notification ||
            _currentState.value is IslandState.NotificationWithMusic
        ) {
            renderNotificationSurface()
        }
    }

    fun stopMusic(packageName: String? = null) {
        if (packageName != null && activeMusicTrack?.packageName != packageName) return
        activeMusicTrack = null
        isMusicPlaying = false
        if (_currentState.value !is IslandState.Music &&
            _currentState.value !is IslandState.NotificationWithMusic
        ) return
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postLiveActivity(activity: LiveActivityInfo) {
        val existingActivity = activeLiveActivities[activity.id]
        val normalizedActivity = if (activity.kind == LiveActivityKind.TIMER) {
            val isSameTimer = existingActivity?.kind == LiveActivityKind.TIMER
            if (!isSameTimer) _timerRunning.value = activity.isRunning
            activity.copy(isRunning = _timerRunning.value)
        } else {
            activity
        }.let { normalized ->
            if (existingActivity?.isExpanded == true) normalized.copy(isExpanded = true) else normalized
        }

        // Remove/reinsert so a refreshed source becomes the most recent source
        // when priorities are equal, without duplicating its registered ID.
        activeLiveActivities.remove(normalizedActivity.id)
        activeLiveActivities[normalizedActivity.id] = normalizedActivity
        liveActivityExpiryJobs.remove(normalizedActivity.id)?.cancel()
        if (normalizedActivity.expiresAtMillis != null) {
            val delayMillis = (normalizedActivity.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            liveActivityExpiryJobs[normalizedActivity.id] = scope.launch {
                delay(delayMillis)
                clearLiveActivity(normalizedActivity.id)
            }
        }
        val visibleActivity = (_currentState.value as? IslandState.LiveActivity)?.activity
        val anotherExpandedActivityOwnsSurface = visibleActivity?.isExpanded == true &&
            visibleActivity.id != normalizedActivity.id
        if (!IslandPresentationOwnershipPolicy.isUserOwned(_currentState.value) ||
            visibleActivity?.id == normalizedActivity.id
        ) {
            if (!anotherExpandedActivityOwnsSurface) renderLiveActivitySurface()
        }
    }

    fun toggleTimer() {
        val current = activeLiveActivities.values.firstOrNull { it.kind == LiveActivityKind.TIMER } ?: return
        _timerRunning.value = !_timerRunning.value
        val updated = current.copy(isRunning = _timerRunning.value)
        activeLiveActivities[updated.id] = updated
        renderLiveActivitySurface()
    }

    fun setTimerRunning(running: Boolean) {
        _timerRunning.value = running
        val current = activeLiveActivities.values.firstOrNull { it.kind == LiveActivityKind.TIMER }
        if (current != null) {
            val updated = current.copy(isRunning = running)
            activeLiveActivities[updated.id] = updated
            renderLiveActivitySurface()
        }
    }

    fun toggleLiveActivityRunning(activityId: String) {
        val current = activeLiveActivities[activityId] ?: return
        val updated = current.copy(isRunning = !current.isRunning)
        activeLiveActivities[activityId] = updated
        val visible = _currentState.value as? IslandState.LiveActivity
        if (visible?.activity?.id == activityId) {
            _currentState.value = visible.copy(activity = updated)
        }
    }

    fun cancelTimer() {
        val current = activeLiveActivities.values.firstOrNull { it.kind == LiveActivityKind.TIMER }
        if (current != null) {
            _timerRunning.value = false
            clearLiveActivity(current.id)
        }
    }

    fun postPremiumExpiry(hoursRemaining: Int) {
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.PremiumExpiry(hoursRemaining.coerceAtLeast(1))
        dismissTimerJob = scope.launch {
            delay(4500L)
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun postFocusMode(enabled: Boolean) {
        if (enabled) {
            postLiveActivity(
                LiveActivityInfo(
                    id = "focus-mode",
                    kind = ai.emots.kishan_dynamic.data.model.LiveActivityKind.FOCUS_MODE,
                    title = "Focus",
                    subtitle = "Do Not Disturb is on",
                    isExpanded = false
                )
            )
        } else {
            clearLiveActivity("focus-mode")
        }
    }

    fun clearLiveActivity(activityId: String? = null) {
        val presentationBeforeRemoval = _currentState.value
        val targetId = activityId
            ?: (_currentState.value as? IslandState.LiveActivity)?.activity?.id
            ?: primaryLiveActivity()?.id
        if (targetId == null) {
            if (activityId == "preview-timer") _timerRunning.value = false
            return
        }
        val removed = activeLiveActivities.remove(targetId)
        specializedNotificationIds.remove(targetId)
        liveActivityExpiryJobs.remove(targetId)?.cancel()
        if (removed?.kind == LiveActivityKind.TIMER || targetId == "preview-timer") {
            _timerRunning.value = false
        }
        if (removed == null && activityId != null) return
        val removedActivityWasVisible =
            (presentationBeforeRemoval as? IslandState.LiveActivity)?.activity?.id == targetId
        if (IslandPresentationOwnershipPolicy.isUserOwned(presentationBeforeRemoval) &&
            !removedActivityWasVisible
        ) return
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postCharging(batteryPercent: Int, isFastCharging: Boolean = true) {
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) {
            return
        }
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.Charging(batteryPercent, isFastCharging)
        dismissTimerJob = scope.launch {
            delay(3500L)
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun clearTransientSystemState() {
        if (_currentState.value is IslandState.Charging ||
            _currentState.value is IslandState.RingerMode ||
            _currentState.value is IslandState.RingerVolume ||
            _currentState.value is IslandState.MediaVolume
        ) {
            dismissTimerJob?.cancel()
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    /**
     * Clears only the battery-owned transient surface. Battery preference
     * changes and recovery broadcasts must not dismiss an unrelated ringer or
     * volume HUD that happened to be visible at the same time.
     */
    fun clearChargingState() {
        if (BatterySurfacePolicy.ownsSurface(_currentState.value)) {
            dismissTimerJob?.cancel()
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun postVolumeLevel(level: Float, isRinger: Boolean = false) {
        val normalizedLevel = level.coerceIn(0f, 1f)
        if (isRinger) _observedRingerVolume.value = normalizedLevel
        else _observedMediaVolume.value = normalizedLevel
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) {
            return
        }
        dismissTimerJob?.cancel()
        _currentState.value = if (isRinger) {
            IslandState.RingerVolume(normalizedLevel)
        } else {
            IslandState.MediaVolume(normalizedLevel)
        }
        dismissTimerJob = scope.launch {
            delay(2000L)
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun postRingerMode(mode: RingerModeType) {
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.RingerMode(mode = mode, isExpanded = false)
        dismissTimerJob = scope.launch {
            delay(2500L)
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun postBluetoothDevice(
        deviceName: String,
        batteryPercent: Int? = null,
        isConnecting: Boolean = false
    ) {
        if (IslandPresentationOwnershipPolicy.blocksTransient(_currentState.value)) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.BluetoothDevice(
            deviceName = deviceName,
            isConnected = !isConnecting,
            batteryPercent = batteryPercent,
            isConnecting = isConnecting
        )
        dismissTimerJob = scope.launch {
            delay(3000L)
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun clearBluetoothDevice() {
        if (_currentState.value is IslandState.BluetoothDevice) {
            dismissTimerJob?.cancel()
            resumeBaseState()
            scheduleNotificationExpiry()
        }
    }

    fun collapseToMinimal() {
        handleSwipeDismiss()
    }

    /** Collapses interactive surfaces before lock-screen rendering without ending their sources. */
    fun onDeviceLocked() {
        interruptedByIncomingCall = null
        interruptedByCriticalNotification = null
        dismissTimerJob?.cancel()
        val current = _currentState.value
        when (current) {
            is IslandState.ActionControl -> {
                resumeBaseState()
            }
            is IslandState.Notification -> if (current.isExpanded) {
                if (activeMusicTrack != null) {
                    _currentState.value = IslandState.NotificationWithMusic(
                        notifications = notificationQueue.notifications,
                        track = activeMusicTrack!!,
                        isPlaying = isMusicPlaying,
                        activeIndex = notificationQueue.activeIndex
                    )
                } else {
                    _currentState.value = current.copy(
                        notifications = notificationQueue.notifications,
                        isExpanded = false,
                        activeIndex = notificationQueue.activeIndex
                    )
                }
            }
            is IslandState.Music -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            }
            is IslandState.OngoingCall -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            }
            is IslandState.LiveActivity -> if (current.activity.isExpanded) {
                val activity = current.activity.copy(isExpanded = false)
                activeLiveActivities[activity.id] = activity
                _currentState.value = current.copy(activity = activity)
            }
            is IslandState.RingerMode -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            }
            else -> Unit
        }
    }

    fun onDeviceUnlocked() {
        scheduleNotificationExpiry()
    }

    fun handleSwipeDismiss(): String? {
        var dismissedNotificationId: String? = null
        val current = _currentState.value
        when (current) {
            is IslandState.Notification -> {
                if (current.isExpanded) {
                    dismissTimerJob?.cancel()
                    if (activeMusicTrack != null) {
                        _currentState.value = IslandState.NotificationWithMusic(
                            notifications = current.notifications,
                            track = activeMusicTrack!!,
                            isPlaying = isMusicPlaying,
                            activeIndex = notificationQueue.activeIndex
                        )
                    } else {
                        _currentState.value = current.copy(isExpanded = false)
                    }
                    scheduleNotificationExpiry()
                } else if (notificationQueue.activeNotification?.isClearable == true) {
                    dismissTimerJob?.cancel()
                    dismissedNotificationId = dismissActiveNotification()
                }
            }
            is IslandState.NotificationWithMusic -> {
                if (notificationQueue.activeNotification?.isClearable == true) {
                    dismissTimerJob?.cancel()
                    dismissedNotificationId = dismissActiveNotification()
                }
            }
            is IslandState.Music -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            }
            is IslandState.OngoingCall -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            }
            is IslandState.ActionControl -> {
                dismissTimerJob?.cancel()
                resumeBaseState()
                scheduleNotificationExpiry()
            }
            is IslandState.LiveActivity -> if (current.activity.isExpanded) {
                val activity = current.activity.copy(isExpanded = false)
                activeLiveActivities[activity.id] = activity
                _currentState.value = current.copy(activity = activity)
            }
            is IslandState.RingerMode -> if (current.isExpanded) {
                _currentState.value = current.copy(isExpanded = false)
            } else {
                dismissTimerJob?.cancel()
                resumeBaseState()
                scheduleNotificationExpiry()
            }
            is IslandState.Charging,
            is IslandState.RingerVolume,
            is IslandState.MediaVolume,
            is IslandState.BluetoothDevice,
            is IslandState.CallSummary,
            is IslandState.PremiumExpiry -> {
                dismissTimerJob?.cancel()
                resumeBaseState()
                scheduleNotificationExpiry()
            }
            is IslandState.Hidden,
            is IslandState.Minimal,
            is IslandState.IncomingCall -> Unit
        }
        return dismissedNotificationId
    }

    fun openActionControl() {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.ActionControl(
            isExpanded = true,
            notificationCount = notificationQueue.notifications.size
        )
    }

    fun openNotifications(selectNext: Boolean = false) {
        if (notificationQueue.notifications.isNotEmpty()) {
            dismissTimerJob?.cancel()
            if (selectNext && notificationQueue.notifications.size > 1) {
                notificationQueue = notificationReducer.reduce(
                    notificationQueue,
                    NotificationEvent.Next
                )
            }
            _currentState.value = notificationState().let { state ->
                when (state) {
                    is IslandState.Notification -> state.copy(isExpanded = true)
                    is IslandState.NotificationWithMusic -> IslandState.Notification(
                        notifications = state.notifications,
                        isExpanded = true,
                        activeIndex = notificationQueue.activeIndex
                    )
                    else -> state
                }
            }
        } else {
            resumeBaseState()
        }
    }

    fun openMusic() {
        val track = activeMusicTrack ?: return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.Music(
            track = track,
            isExpanded = true,
            isPlaying = isMusicPlaying
        )
    }

    fun toggleExpansion() {
        val current = _currentState.value
        _currentState.value = when (current) {
            is IslandState.Music -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.OngoingCall -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.RingerMode -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.Notification -> {
                val willExpand = !current.isExpanded
                if (willExpand) {
                    dismissTimerJob?.cancel() // Suspend auto-collapse while reading or replying
                } else {
                    scheduleNotificationExpiry()
                }
                current.copy(isExpanded = willExpand)
            }
            is IslandState.NotificationWithMusic -> {
                dismissTimerJob?.cancel()
                IslandState.Notification(
                    notifications = current.notifications,
                    isExpanded = true,
                    activeIndex = 0
                )
            }
            is IslandState.LiveActivity -> current.copy(
                activity = current.activity.copy(isExpanded = !current.activity.isExpanded)
            ).also { updated ->
                activeLiveActivities[updated.activity.id] = updated.activity
            }
            is IslandState.Minimal -> {
                dismissTimerJob?.cancel()
                IslandState.ActionControl(
                    isExpanded = true,
                    notificationCount = notificationQueue.notifications.size
                )
            }
            is IslandState.ActionControl -> {
                resumeBaseState()
                scheduleNotificationExpiry()
                _currentState.value
            }
            else -> current
        }
    }

    fun collapseExpandedState() {
        val current = _currentState.value
        when (current) {
            is IslandState.ActionControl -> {
                resumeBaseState()
                scheduleNotificationExpiry()
            }
            is IslandState.Notification -> {
                if (current.isExpanded) {
                    if (activeMusicTrack != null) {
                        _currentState.value = IslandState.NotificationWithMusic(
                            notifications = current.notifications,
                            track = activeMusicTrack!!,
                            isPlaying = isMusicPlaying,
                            activeIndex = notificationQueue.activeIndex
                        )
                    } else {
                        _currentState.value = current.copy(isExpanded = false)
                    }
                    scheduleNotificationExpiry()
                }
            }
            is IslandState.Music -> {
                if (current.isExpanded) {
                    _currentState.value = current.copy(isExpanded = false)
                }
            }
            is IslandState.OngoingCall -> {
                if (current.isExpanded) {
                    _currentState.value = current.copy(isExpanded = false)
                }
            }
            is IslandState.RingerMode -> {
                if (current.isExpanded) {
                    _currentState.value = current.copy(isExpanded = false)
                }
            }
            is IslandState.LiveActivity -> {
                if (current.activity.isExpanded) {
                    val updated = current.activity.copy(isExpanded = false)
                    activeLiveActivities[updated.id] = updated
                    _currentState.value = current.copy(activity = updated)
                }
            }
            is IslandState.Charging,
            is IslandState.RingerVolume,
            is IslandState.MediaVolume,
            is IslandState.BluetoothDevice,
            is IslandState.CallSummary,
            is IslandState.PremiumExpiry -> {
                resumeBaseState()
            }
            else -> Unit
        }
    }

    private fun resumeBaseState() {
        dismissTimerJob?.cancel()
        val nextState = when {
            activeCallContact != null -> IslandState.OngoingCall(
                contact = activeCallContact!!,
                showDuration = callTimerEnabled,
                isDialing = false
            )
            primaryLiveActivity() != null -> IslandState.LiveActivity(primaryLiveActivity()!!)
            pendingCallSummary != null -> IslandState.CallSummary(pendingCallSummary!!)
            notificationQueue.notifications.isNotEmpty() -> notificationState()
            activeMusicTrack != null -> IslandState.Music(activeMusicTrack!!, isPlaying = isMusicPlaying)
            else -> IslandState.Minimal
        }
        _currentState.value = nextState
        if (nextState is IslandState.CallSummary) scheduleCallSummaryExpiry()
    }

    private fun rememberPresentationBeforeIncomingCall() {
        val current = _currentState.value
        if (current is IslandState.OngoingCall) {
            interruptedOngoingCall = current
            return
        }
        if (IslandPresentationOwnershipPolicy.isUserOwned(current)) {
            interruptedByIncomingCall = current
        }
    }

    private fun restoreInterruptedOngoingCall(): Boolean {
        val interrupted = interruptedOngoingCall ?: return false
        interruptedOngoingCall = null
        interruptedByIncomingCall = null
        activeCallContact = interrupted.contact
        callTimerEnabled = interrupted.showDuration
        callStartedAtMillis = if (interrupted.isDialing) {
            0L
        } else {
            System.currentTimeMillis() - interrupted.durationSeconds.coerceAtLeast(0L) * 1000L
        }
        _currentState.value = interrupted
        if (!interrupted.isDialing) startCallDurationTicker()
        return true
    }

    private fun restoreInterruptedPresentation(): Boolean {
        val interrupted = interruptedByIncomingCall ?: return false
        interruptedByIncomingCall = null
        val restored = when (interrupted) {
            is IslandState.ActionControl -> interrupted.copy(
                notificationCount = notificationQueue.notifications.size
            )
            is IslandState.Notification -> if (notificationQueue.notifications.isNotEmpty()) {
                interrupted.copy(
                    notifications = notificationQueue.notifications,
                    activeIndex = notificationQueue.activeIndex
                )
            } else null
            is IslandState.Music -> activeMusicTrack?.let { track ->
                interrupted.copy(track = track, isPlaying = isMusicPlaying)
            }
            is IslandState.LiveActivity -> activeLiveActivities[interrupted.activity.id]?.let { activity ->
                IslandState.LiveActivity(activity.copy(isExpanded = true))
            }
            is IslandState.RingerMode -> interrupted
            else -> null
        } ?: return false
        _currentState.value = restored
        return true
    }

    private fun restoreCriticalInterruptedPresentation(): Boolean {
        val interrupted = interruptedByCriticalNotification ?: return false
        interruptedByCriticalNotification = null
        val restored = when (interrupted) {
            is IslandState.ActionControl -> interrupted.copy(
                notificationCount = notificationQueue.notifications.size
            )
            is IslandState.Notification -> if (notificationQueue.notifications.isNotEmpty()) {
                interrupted.copy(
                    notifications = notificationQueue.notifications,
                    activeIndex = notificationQueue.activeIndex
                )
            } else null
            is IslandState.Music -> activeMusicTrack?.let { track ->
                interrupted.copy(track = track, isPlaying = isMusicPlaying)
            }
            is IslandState.LiveActivity -> activeLiveActivities[interrupted.activity.id]?.let { activity ->
                IslandState.LiveActivity(activity.copy(isExpanded = true))
            }
            is IslandState.RingerMode -> interrupted
            else -> null
        } ?: return false
        dismissTimerJob?.cancel()
        _currentState.value = restored
        return true
    }

    private fun IslandState.visibleNotificationIsCritical(): Boolean {
        val visible = when (this) {
            is IslandState.Notification -> notifications.getOrNull(activeIndex)
            is IslandState.NotificationWithMusic -> notifications.getOrNull(activeIndex)
            else -> null
        }
        return visible?.let(NotificationInterruptionPolicy::isCriticalInterruption) == true
    }

    private fun hasQueuedCriticalNotification(): Boolean =
        notificationQueue.notifications.any(NotificationInterruptionPolicy::isCriticalInterruption)

    private fun scheduleCallSummaryExpiry() {
        dismissTimerJob?.cancel()
        dismissTimerJob = scope.launch {
            delay(CALL_SUMMARY_DISPLAY_MILLIS)
            clearCallSummary()
        }
    }

    private fun primaryLiveActivity(): LiveActivityInfo? =
        LiveActivityPrioritizer.primary(activeLiveActivities.values)

    private fun renderLiveActivitySurface() {
        val current = _currentState.value
        if (IslandPresentationOwnershipPolicy.isUserOwned(current) &&
            current !is IslandState.LiveActivity
        ) return
        if (_currentState.value is IslandState.IncomingCall ||
            _currentState.value is IslandState.OngoingCall
        ) return
        val activity = primaryLiveActivity()
        if (activity != null) {
            _currentState.value = IslandState.LiveActivity(activity)
        } else {
            resumeBaseState()
        }
    }

    private fun renderNotificationSurface() {
        // Calls always win visually, but posted notifications remain queued and
        // are rendered as soon as the call ends.
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) {
            return
        }
        if (notificationQueue.notifications.isEmpty()) {
            resumeBaseState()
            return
        }
        if (LiveActivitySurfacePolicy.usesSpecializedSurface(
                notificationQueue.activeNotification?.id,
                specializedNotificationIds
            )
        ) {
            renderLiveActivitySurface()
            return
        }
        _currentState.value = notificationState()
    }

    /** Applies a queue change without allowing background work to steal a user-owned surface. */
    private fun applyNotificationPresentation(decision: NotificationPresentationDecision) {
        when (decision) {
            NotificationPresentationDecision.KEEP_QUICK_CONTROLS -> {
                val controls = _currentState.value as? IslandState.ActionControl ?: return
                _currentState.value = controls.copy(
                    notificationCount = notificationQueue.notifications.size
                )
            }
            NotificationPresentationDecision.UPDATE_EXPANDED_NOTIFICATION -> {
                val expanded = _currentState.value as? IslandState.Notification
                if (expanded == null || notificationQueue.notifications.isEmpty()) {
                    resumeBaseState()
                    scheduleNotificationExpiry()
                } else {
                    _currentState.value = expanded.copy(
                        notifications = notificationQueue.notifications,
                        activeIndex = notificationQueue.activeIndex
                    )
                }
            }
            NotificationPresentationDecision.DEFER -> Unit
            NotificationPresentationDecision.PRESENT -> {
                dismissTimerJob?.cancel()
                renderNotificationSurface()
                scheduleNotificationExpiry()
            }
        }
    }

    private fun notificationState(): IslandState {
        val active = notificationQueue.activeNotification ?: return IslandState.Minimal
        return if (activeMusicTrack != null) {
            IslandState.NotificationWithMusic(
                notifications = notificationQueue.notifications,
                track = activeMusicTrack!!,
                isPlaying = isMusicPlaying,
                activeIndex = notificationQueue.activeIndex
            )
        } else {
            IslandState.Notification(
                notifications = notificationQueue.notifications,
                isExpanded = notificationAutoExpand,
                activeIndex = notificationQueue.notifications.indexOfFirst { it.id == active.id }
                    .coerceAtLeast(0)
            )
        }
    }

    private fun scheduleNotificationExpiry() {
        if (notificationQueue.notifications.isEmpty()) return
        if (_currentState.value !is IslandState.Notification &&
            _currentState.value !is IslandState.NotificationWithMusic
        ) return
        // Suspend auto-expiry if the user has expanded the notification to read / reply
        if ((_currentState.value as? IslandState.Notification)?.isExpanded == true) return
        dismissTimerJob?.cancel()
        val activeId = notificationQueue.activeNotification?.id ?: return
        val activeNotification = notificationQueue.activeNotification ?: return
        if (activeNotification.isOngoing || !activeNotification.isClearable) return
        dismissTimerJob = scope.launch {
            delay(notificationDisplaySeconds * 1000L)
            val expiredWasCritical = notificationQueue.activeNotification?.let { active ->
                active.id == activeId && NotificationInterruptionPolicy.isCriticalInterruption(active)
            } == true
            notificationQueue = notificationReducer.reduce(
                notificationQueue,
                NotificationEvent.Expired(activeId)
            )
            if (expiredWasCritical && !hasQueuedCriticalNotification() &&
                restoreCriticalInterruptedPresentation()
            ) {
                Unit
            } else if (notificationQueue.notifications.isEmpty()) {
                resumeBaseState()
            } else {
                renderNotificationSurface()
                scheduleNotificationExpiry()
            }
        }
    }

    private const val CALL_SUMMARY_DISPLAY_MILLIS = 6000L
}
