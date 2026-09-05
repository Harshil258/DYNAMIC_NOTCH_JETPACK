package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.IslandState
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
    private val activeLiveActivities = linkedMapOf<String, LiveActivityInfo>()
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()
    private val notificationReducer = NotificationReducer()
    private var notificationQueue = NotificationQueueState()
    private var notificationAutoExpand = false
    private var notificationDisplaySeconds = 5
    private val specializedNotificationIds = mutableSetOf<String>()

    fun postIncomingCall(contact: ContactInfo) {
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        pendingCallSummary = null
        pendingIncomingCall = contact
        activeCallContact = null
        _currentState.value = IslandState.IncomingCall(contact)
    }

    fun postNotificationIncomingCall(
        contact: ContactInfo,
        notificationId: String,
        acceptActionId: String,
        declineActionId: String?
    ) {
        dismissTimerJob?.cancel()
        callDurationJob?.cancel()
        pendingCallSummary = null
        pendingIncomingCall = contact
        activeCallContact = null
        _currentState.value = IslandState.IncomingCall(
            contact = contact,
            sourceNotificationId = notificationId,
            acceptActionId = acceptActionId,
            declineActionId = declineActionId
        )
    }

    /** Clears a notification-backed call while its source app handles the action. */
    fun resolveNotificationIncomingCall(notificationId: String? = null) {
        val current = _currentState.value as? IslandState.IncomingCall
        if (notificationId != null && current?.sourceNotificationId != notificationId) return
        pendingIncomingCall = null
        dismissTimerJob?.cancel()
        resumeBaseState()
    }

    fun postNotificationOngoingCall(
        contact: ContactInfo,
        notificationId: String,
        endActionId: String,
        isDialing: Boolean
    ) {
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
            postOngoingCall(it, durationSeconds = 0L, showDuration = callTimerEnabled)
        }
    }

    fun declineIncomingCall() {
        pendingIncomingCall = null
        endCall()
    }

    fun endCall() {
        activeCallContact = null
        pendingIncomingCall = null
        callDurationJob?.cancel()
        callDurationJob = null
        dismissTimerJob?.cancel()
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postCallSummary(record: ai.emots.kishan_dynamic.data.model.CallRecord) {
        pendingCallSummary = record
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.CallSummary(record)
        dismissTimerJob = scope.launch {
            delay(CALL_SUMMARY_DISPLAY_MILLIS)
            clearCallSummary()
        }
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
        dismissTimerJob?.cancel()

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
        }

        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Posted(notification)
        )
        activeLiveActivities[notification.id]?.let { activity ->
            activeLiveActivities[notification.id] = activity.copy(
                sourceNotificationId = notification.id,
                sourceActionId = LiveActivitySourceActionPolicy.primaryActionId(notification)
            )
        }
        renderNotificationSurface()
        if (_currentState.value !is IslandState.IncomingCall && _currentState.value !is IslandState.OngoingCall) {
            scheduleNotificationExpiry()
        }
    }

    /** Keeps the source notification queued while its richer activity owns the island. */
    fun postNotificationBackedLiveActivity(activity: LiveActivityInfo) {
        specializedNotificationIds += activity.id
        postLiveActivity(activity.copy(sourceNotificationId = activity.id))
    }

    fun removeNotification(notificationId: String) {
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Removed(notificationId)
        )
        if (notificationQueue.notifications.isEmpty()) {
            dismissTimerJob?.cancel()
        } else {
            scheduleNotificationExpiry()
        }
        renderNotificationSurface()
    }

    fun selectNotification(notificationId: String) {
        notificationQueue = notificationReducer.reduce(
            notificationQueue,
            NotificationEvent.Selected(notificationId)
        )
        renderNotificationSurface()
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

    fun dismissActiveNotification() {
        val activeId = notificationQueue.activeNotification?.id ?: return
        removeNotification(activeId)
        NotificationActionRegistry.remove(activeId)
    }

    fun postMusicPlayback(track: MusicTrack, isPlaying: Boolean) {
        activeMusicTrack = track
        isMusicPlaying = isPlaying

        // Calls and notifications retain priority, but music remains available as
        // the base layer for the notification-with-music presentation.
        if (_currentState.value is IslandState.Minimal || _currentState.value is IslandState.Music) {
            _currentState.value = IslandState.Music(
                track = track,
                isPlaying = isPlaying,
                isExpanded = false
            )
        } else if (notificationQueue.notifications.isNotEmpty()) {
            renderNotificationSurface()
        }
    }

    fun stopMusic(packageName: String? = null) {
        if (packageName != null && activeMusicTrack?.packageName != packageName) return
        activeMusicTrack = null
        isMusicPlaying = false
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
        renderLiveActivitySurface()
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

    fun cancelTimer() {
        val current = activeLiveActivities.values.firstOrNull { it.kind == LiveActivityKind.TIMER }
        if (current != null) {
            _timerRunning.value = false
            clearLiveActivity(current.id)
        }
    }

    fun postPremiumExpiry(hoursRemaining: Int) {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.PremiumExpiry(hoursRemaining.coerceAtLeast(1))
        dismissTimerJob = scope.launch {
            delay(4500L)
            resumeBaseState()
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
        resumeBaseState()
        scheduleNotificationExpiry()
    }

    fun postCharging(batteryPercent: Int, isFastCharging: Boolean = true) {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) {
            return
        }
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.Charging(batteryPercent, isFastCharging)
        dismissTimerJob = scope.launch {
            delay(3500L)
            resumeBaseState()
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
        }
    }

    fun postVolumeLevel(level: Float, isRinger: Boolean = false) {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) {
            return
        }
        dismissTimerJob?.cancel()
        _currentState.value = if (isRinger) IslandState.RingerVolume(level) else IslandState.MediaVolume(level)
        dismissTimerJob = scope.launch {
            delay(2000L)
            resumeBaseState()
        }
    }

    fun postRingerMode(mode: RingerModeType) {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) return
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.RingerMode(mode = mode, isExpanded = false)
        dismissTimerJob = scope.launch {
            delay(2500L)
            resumeBaseState()
        }
    }

    fun postBluetoothDevice(
        deviceName: String,
        batteryPercent: Int? = null,
        isConnecting: Boolean = false
    ) {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) return
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
        }
    }

    fun clearBluetoothDevice() {
        if (_currentState.value is IslandState.BluetoothDevice) {
            dismissTimerJob?.cancel()
            resumeBaseState()
        }
    }

    fun collapseToMinimal() {
        handleSwipeDismiss()
    }

    fun handleSwipeDismiss() {
        dismissTimerJob?.cancel()
        val current = _currentState.value
        when {
            current is IslandState.Notification && current.isExpanded -> {
                if (activeMusicTrack != null) {
                    _currentState.value = IslandState.NotificationWithMusic(
                        notifications = current.notifications,
                        track = activeMusicTrack!!,
                        isPlaying = isMusicPlaying
                    )
                } else {
                    _currentState.value = current.copy(isExpanded = false)
                }
                scheduleNotificationExpiry()
            }
            current is IslandState.Music && current.isExpanded -> {
                _currentState.value = current.copy(isExpanded = false)
            }
            current is IslandState.OngoingCall && current.isExpanded -> {
                _currentState.value = current.copy(isExpanded = false)
            }
            current is IslandState.ActionControl -> {
                resumeBaseState()
            }
            current is IslandState.LiveActivity && current.activity.isExpanded -> {
                val updated = current.activity.copy(isExpanded = false)
                activeLiveActivities[updated.id] = updated
                _currentState.value = current.copy(activity = updated)
            }
            current is IslandState.Charging ||
            current is IslandState.RingerVolume ||
            current is IslandState.MediaVolume ||
            current is IslandState.RingerMode ||
            current is IslandState.BluetoothDevice ||
            current is IslandState.CallSummary ||
            current is IslandState.PremiumExpiry -> {
                resumeBaseState()
            }
            notificationQueue.notifications.isNotEmpty() -> {
                dismissActiveNotification()
            }
            else -> {
                resumeBaseState()
            }
        }
    }

    fun openActionControl() {
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) return
        _currentState.value = IslandState.ActionControl(
            isExpanded = true,
            notificationCount = notificationQueue.notifications.size
        )
    }

    fun openNotifications() {
        if (notificationQueue.notifications.isNotEmpty()) {
            dismissTimerJob?.cancel()
            _currentState.value = notificationState().let { state ->
                when (state) {
                    is IslandState.Notification -> state.copy(isExpanded = true)
                    is IslandState.NotificationWithMusic -> IslandState.Notification(
                        notifications = state.notifications,
                        isExpanded = true,
                        activeIndex = 0
                    )
                    else -> state
                }
            }
        } else {
            resumeBaseState()
        }
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
            is IslandState.Minimal -> IslandState.ActionControl(
                isExpanded = true,
                notificationCount = notificationQueue.notifications.size
            )
            is IslandState.ActionControl -> {
                resumeBaseState()
                _currentState.value
            }
            else -> current
        }
    }

    fun collapseExpandedState() {
        val current = _currentState.value
        when (current) {
            is IslandState.ActionControl -> resumeBaseState()
            is IslandState.Notification -> {
                if (current.isExpanded) {
                    if (activeMusicTrack != null) {
                        _currentState.value = IslandState.NotificationWithMusic(
                            notifications = current.notifications,
                            track = activeMusicTrack!!,
                            isPlaying = isMusicPlaying
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
        _currentState.value = when {
            activeCallContact != null -> IslandState.OngoingCall(
                contact = activeCallContact!!,
                showDuration = callTimerEnabled,
                isDialing = false
            )
            pendingCallSummary != null -> IslandState.CallSummary(pendingCallSummary!!)
            notificationQueue.notifications.isNotEmpty() -> notificationState()
            primaryLiveActivity() != null -> IslandState.LiveActivity(primaryLiveActivity()!!)
            activeMusicTrack != null -> IslandState.Music(activeMusicTrack!!, isPlaying = isMusicPlaying)
            else -> IslandState.Minimal
        }
    }

    private fun primaryLiveActivity(): LiveActivityInfo? =
        LiveActivityPrioritizer.primary(activeLiveActivities.values)

    private fun renderLiveActivitySurface() {
        if (_currentState.value is IslandState.IncomingCall ||
            _currentState.value is IslandState.OngoingCall
        ) return
        if (notificationQueue.notifications.isNotEmpty() &&
            !LiveActivitySurfacePolicy.usesSpecializedSurface(
                notificationQueue.activeNotification?.id,
                specializedNotificationIds
            )
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

    private fun notificationState(): IslandState {
        val active = notificationQueue.activeNotification ?: return IslandState.Minimal
        return if (activeMusicTrack != null) {
            IslandState.NotificationWithMusic(
                notifications = notificationQueue.notifications,
                track = activeMusicTrack!!,
                isPlaying = isMusicPlaying
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
        // Suspend auto-expiry if the user has expanded the notification to read / reply
        if ((_currentState.value as? IslandState.Notification)?.isExpanded == true) return
        dismissTimerJob?.cancel()
        val activeId = notificationQueue.activeNotification?.id ?: return
        dismissTimerJob = scope.launch {
            delay(notificationDisplaySeconds * 1000L)
            notificationQueue = notificationReducer.reduce(
                notificationQueue,
                NotificationEvent.Expired(activeId)
            )
            if (notificationQueue.notifications.isEmpty()) {
                resumeBaseState()
            } else {
                renderNotificationSurface()
                scheduleNotificationExpiry()
            }
        }
    }

    private const val CALL_SUMMARY_DISPLAY_MILLIS = 6000L
}
