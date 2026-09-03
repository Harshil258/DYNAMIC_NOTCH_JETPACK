package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
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

    private val _currentState = MutableStateFlow<IslandState>(IslandState.Minimal)
    val currentState: StateFlow<IslandState> = _currentState.asStateFlow()

    // Cached sub-states to resume when transient overlays expire
    private var activeMusicTrack: MusicTrack? = null
    private var isMusicPlaying: Boolean = false
    private var activeCallContact: ContactInfo? = null

    fun postIncomingCall(contact: ContactInfo) {
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.IncomingCall(contact)
    }

    fun postOngoingCall(contact: ContactInfo, durationSeconds: Long) {
        activeCallContact = contact
        dismissTimerJob?.cancel()
        _currentState.value = IslandState.OngoingCall(
            contact = contact,
            durationSeconds = durationSeconds,
            isExpanded = false
        )
    }

    fun endCall() {
        activeCallContact = null
        dismissTimerJob?.cancel()
        resumeBaseState()
    }

    fun postNotification(notification: NotificationInfo, autoExpand: Boolean, displaySeconds: Int) {
        // Do not interrupt active calls
        if (_currentState.value is IslandState.IncomingCall || _currentState.value is IslandState.OngoingCall) {
            return
        }

        dismissTimerJob?.cancel()

        if (activeMusicTrack != null) {
            _currentState.value = IslandState.NotificationWithMusic(
                notification = notification,
                track = activeMusicTrack!!,
                isPlaying = isMusicPlaying
            )
        } else {
            _currentState.value = IslandState.Notification(
                notifications = listOf(notification),
                isExpanded = autoExpand
            )
        }

        // Schedule auto-collapse
        dismissTimerJob = scope.launch {
            delay(displaySeconds * 1000L)
            resumeBaseState()
        }
    }

    fun postMusicPlayback(track: MusicTrack, isPlaying: Boolean) {
        activeMusicTrack = track
        isMusicPlaying = isPlaying

        // Only update if no higher priority call or notification is active
        if (_currentState.value is IslandState.Minimal || _currentState.value is IslandState.Music) {
            _currentState.value = IslandState.Music(
                track = track,
                isPlaying = isPlaying,
                isExpanded = false
            )
        }
    }

    fun stopMusic() {
        activeMusicTrack = null
        isMusicPlaying = false
        resumeBaseState()
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

    fun collapseToMinimal() {
        dismissTimerJob?.cancel()
        resumeBaseState()
    }

    fun toggleExpansion() {
        val current = _currentState.value
        _currentState.value = when (current) {
            is IslandState.Music -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.OngoingCall -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.Notification -> current.copy(isExpanded = !current.isExpanded)
            is IslandState.Minimal -> IslandState.ActionControl(isExpanded = true)
            is IslandState.ActionControl -> IslandState.Minimal
            else -> current
        }
    }

    private fun resumeBaseState() {
        _currentState.value = when {
            activeCallContact != null -> IslandState.OngoingCall(activeCallContact!!)
            activeMusicTrack != null -> IslandState.Music(activeMusicTrack!!, isPlaying = isMusicPlaying)
            else -> IslandState.Minimal
        }
    }
}
