package ai.emots.kishan_dynamic.playground

import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Debug data only: never reads notifications, contacts, billing, preferences or media sessions. */
enum class PlaygroundCase(val title: String) {
    Hidden("Hidden / inactive"), Idle("Idle capsule"), Minimal("Minimal notification"),
    Compact("Compact notification"), Expanded("Expanded notification"), Music("Music playback"),
    IncomingCall("Incoming call"), ActiveCall("Active call"), Timer("Timer / countdown"),
    Charging("Charging"), LowBattery("Low battery warning"), Navigation("Navigation"),
    Recording("Recording"), Multiple("Multiple activities"), Success("Success"), Warning("Warning"),
    Error("Error / retry"), Controls("Quick controls"), Access("Pro access message")
}
enum class CameraPreset { None, Left, Center, Pill }
data class PlaygroundDevice(val name: String, val width: Int, val density: Float, val camera: CameraPreset)
val playgroundDevices = listOf(
    PlaygroundDevice("Small · 320dp", 320, 3f, CameraPreset.Left),
    PlaygroundDevice("Normal · 360dp", 360, 3f, CameraPreset.Left),
    PlaygroundDevice("393dp · centered", 393, 2.75f, CameraPreset.Center),
    PlaygroundDevice("Large · 430dp", 430, 3f, CameraPreset.Pill),
    PlaygroundDevice("Landscape · 640dp", 640, 2f, CameraPreset.Left)
)
data class PlaygroundOptions(
    val deviceIndex: Int = 1, val width: Int = 360, val density: Float = 3f,
    val camera: CameraPreset = CameraPreset.Left, val fontScale: Float = 1f, val rtl: Boolean = false,
    val reducedMotion: Boolean = false, val guides: Boolean = true,
    val longText: Boolean = false, val missingIcon: Boolean = false, val missingText: Boolean = false,
    val actionCount: Int = 2, val motionOverride: Boolean = false,
    val motion: IslandMotionSettings = IslandMotionSettings(),
    val manualSize: Boolean = false, val widthOverride: Float = 160f, val heightOverride: Float = 80f,
    val minimumWidth: Float = 0f, val maximumWidth: Float = 408f, val maximumHeight: Float = 600f,
    val compactHeight: Float = 36.67f, val horizontalPadding: Float = 12f,
    val verticalPadding: Float = 16f, val iconSize: Float = 20f, val textGap: Float = 8f,
    val corner: Float = 24f, val overrideCorner: Boolean = false,
    val cameraClearance: Float = 4f, val actionSize: Float = 50f, val actionGap: Float = 12f
)
data class PlaygroundSnapshot(
    val selected: PlaygroundCase = PlaygroundCase.Idle,
    val state: IslandState = IslandState.Minimal,
    val options: PlaygroundOptions = PlaygroundOptions(),
    val feedback: String = "Mock data · production renderer",
    val timerSeconds: Int = 215, val recordingSeconds: Int = 42,
    val replay: Int = 0, val sequenceRunning: Boolean = false, val eventCount: Int = 0
)
data class PlaygroundCheck(val name: String, val passed: Boolean, val detail: String)

class PlaygroundController(private val scope: CoroutineScope) {
    private val mutable = MutableStateFlow(PlaygroundSnapshot())
    val snapshot = mutable.asStateFlow()
    private var sequence: Job? = null
    private var clock: Job? = null
    val checks = MutableStateFlow<List<PlaygroundCheck>>(emptyList())
    fun options(change: (PlaygroundOptions) -> PlaygroundOptions) {
        mutable.update { it.copy(options = change(it.options)) }
        // Refresh mock copy immediately, keeping the current expansion when possible.
        val current = mutable.value
        mutable.update { it.copy(state = mockState(current.selected, current.options,
            expanded = current.state.presentationMode == IslandPresentationMode.EXPANDED)) }
    }
    fun select(case: PlaygroundCase) { cancelSequence(); emit(case) }
    private fun emit(case: PlaygroundCase) {
        mutable.update { it.copy(selected = case, state = mockState(case, it.options),
            feedback = "${case.title} · simulated", eventCount = it.eventCount + 1) }
        startClock()
    }
    fun startClock() {
        clock?.cancel()
        if (mutable.value.selected !in setOf(PlaygroundCase.Timer, PlaygroundCase.Music, PlaygroundCase.Multiple, PlaygroundCase.ActiveCall, PlaygroundCase.Recording)) return
        clock = scope.launch {
            while (isActive) {
                delay(1000)
                mutable.update { current ->
                    when (val state = current.state) {
                        is IslandState.Music -> if (state.isPlaying) current.copy(state = state.copy(track = state.track.copy(positionMs = (state.track.positionMs + 1000).coerceAtMost(state.track.durationMs)))) else current
                        is IslandState.OngoingCall -> current.copy(state = state.copy(durationSeconds = state.durationSeconds + 1))
                        is IslandState.LiveActivity -> when {
                            state.activity.kind == LiveActivityKind.TIMER && state.activity.isRunning -> current.copy(timerSeconds = (current.timerSeconds - 1).coerceAtLeast(0))
                            state.activity.kind == LiveActivityKind.VOICE_MEMO -> current.copy(recordingSeconds = current.recordingSeconds + 1)
                            else -> current
                        }
                        else -> current
                    }
                }
            }
        }
    }
    fun stopClock() { clock?.cancel(); clock = null }
    fun selectNotification(id: String) { mutable.update { current ->
        val state = current.state as? IslandState.Notification ?: return@update current
        val index = state.notifications.indexOfFirst { it.id == id }
        if (index < 0) current else current.copy(state = state.copy(activeIndex = index))
    } }
    fun replay() {
        cancelSequence()
        val restore = mutable.value.state
        mutable.update { it.copy(state = IslandState.Minimal, feedback = "Replaying real state transition", sequenceRunning = true) }
        sequence = scope.launch {
            delay((300 / mutable.value.options.motion.speed).toLong())
            mutable.update { it.copy(state = restore, replay = it.replay + 1, sequenceRunning = false) }
        }
    }
    fun message(text: String) { mutable.update { it.copy(feedback = text) } }
    fun interaction(companion: Boolean = false, longPress: Boolean = false) {
        val state = mutable.value.state
        val action = when {
            longPress -> IslandInteractionPolicy.mainLongPress(state)
            companion -> IslandInteractionPolicy.companionTap(state)
            else -> IslandInteractionPolicy.mainTap(state, quickControlsOnIdleEnabled = true)
        }
        when (action) {
            IslandInteractionAction.NONE -> message("No action for this production interaction policy")
            IslandInteractionAction.OPEN_QUICK_CONTROLS -> select(PlaygroundCase.Controls)
            IslandInteractionAction.OPEN_MUSIC -> mutable.update { it.copy(state = IslandState.Music(mockMusic, true)) }
            IslandInteractionAction.OPEN_NOTIFICATIONS -> mutable.update {
                val notifications = when (val source = it.state) {
                    is IslandState.Notification -> source.notifications
                    is IslandState.NotificationWithMusic -> source.notifications
                    else -> listOf(mockNotification(it.options))
                }
                it.copy(state = IslandState.Notification(notifications, true))
            }
            IslandInteractionAction.OPEN_NEXT_NOTIFICATION -> mutable.update {
                val source = it.state as? IslandState.Notification
                if (source == null) it else it.copy(state = source.copy(activeIndex = (source.activeIndex + 1) % source.notifications.size))
            }
            IslandInteractionAction.TOGGLE_EXPANSION -> expand(true)
        }
    }
    fun expand(expanded: Boolean) { mutable.update { current ->
        current.copy(state = when (val state = current.state) {
            is IslandState.Notification -> state.copy(isExpanded = expanded, expandedByUser = expanded)
            is IslandState.Music -> state.copy(isExpanded = expanded)
            is IslandState.OngoingCall -> state.copy(isExpanded = expanded)
            is IslandState.LiveActivity -> state.copy(activity = state.activity.copy(isExpanded = expanded))
            is IslandState.RingerMode -> state.copy(isExpanded = expanded)
            else -> if (expanded) state else IslandState.Minimal
        })
    } }
    fun control(action: IslandControlAction) {
        when (action) {
            IslandControlAction.MusicPlayPause -> mutable.update { val state = it.state as? IslandState.Music
                if (state == null) it else it.copy(state = state.copy(isPlaying = !state.isPlaying)) }
            IslandControlAction.MusicNext, IslandControlAction.MusicPrevious -> mutable.update { current ->
                val source = current.state as? IslandState.Music
                if (source == null) current else current.copy(state = source.copy(track = source.track.copy(
                    title = if (action == IslandControlAction.MusicNext) "Northern Lights" else "Midnight Drive", positionMs = 0)))
            }
            IslandControlAction.NavigationLeft, IslandControlAction.NavigationRight, IslandControlAction.NavigationStraight -> mutable.update { current ->
                val source = current.state as? IslandState.LiveActivity
                if (source == null) current else current.copy(state = source.copy(activity = source.activity.copy(
                    maneuverType = when (action) { IslandControlAction.NavigationLeft -> "left"; IslandControlAction.NavigationRight -> "right"; else -> "straight" })))
            }
            IslandControlAction.CallAccept -> select(PlaygroundCase.ActiveCall)
            IslandControlAction.CallDecline, IslandControlAction.CallEnd, IslandControlAction.TimerCancel,
            IslandControlAction.VoiceMemoStop, IslandControlAction.ScreenRecordingStop -> select(PlaygroundCase.Idle)
            IslandControlAction.TimerToggle -> mutable.update { val state = it.state as? IslandState.LiveActivity
                if (state == null) it else it.copy(state = state.copy(activity = state.activity.copy(isRunning = !state.activity.isRunning))) }
            else -> message("Simulated ${action.name} · no Android, ad or purchase action executed")
        }
    }
    fun runSequence(name: String) {
        cancelSequence()
        val cases = when (name) {
            "Visibility" -> listOf(PlaygroundCase.Hidden, PlaygroundCase.Idle, PlaygroundCase.Compact, PlaygroundCase.Hidden, PlaygroundCase.Idle)
            "Multiple" -> listOf(PlaygroundCase.Music, PlaygroundCase.Multiple, PlaygroundCase.Expanded, PlaygroundCase.Music)
            "Notification swap" -> listOf(PlaygroundCase.Compact, PlaygroundCase.Warning, PlaygroundCase.Success, PlaygroundCase.Error)
            else -> listOf(PlaygroundCase.Idle, PlaygroundCase.Compact, PlaygroundCase.Expanded, PlaygroundCase.Compact, PlaygroundCase.Idle)
        }
        sequence = scope.launch {
            mutable.update { it.copy(sequenceRunning = true) }
            try { cases.forEach { emit(it); delay((900 / mutable.value.options.motion.speed).toLong()) } }
            finally { mutable.update { it.copy(sequenceRunning = false) } }
        }
    }
    fun burst() {
        cancelSequence()
        sequence = scope.launch {
            mutable.update { it.copy(sequenceRunning = true) }
            try {
                for (i in 1..30) {
                    val notification = mockNotification(mutable.value.options).copy(id = "mock-$i", title = "Mock update $i")
                    mutable.update { it.copy(selected = PlaygroundCase.Compact,
                        state = IslandState.Notification(listOf(notification)), eventCount = it.eventCount + 1,
                        feedback = "Burst: $i / 30 · latest snapshot") }
                    delay(60)
                }
            } finally { mutable.update { it.copy(sequenceRunning = false) } }
        }
    }
    fun cancelSequence() { sequence?.cancel(); sequence = null; mutable.update { it.copy(sequenceRunning = false) } }
    fun runChecks() { checks.value = playgroundSelfChecks() }
}

val mockMusic = MusicTrack("Midnight Drive", "Aurora Studio · mock media", durationMs = 240000, positionMs = 75000)
fun mockNotification(options: PlaygroundOptions) = NotificationInfo(
    id = "mock-message", packageName = if (options.missingIcon) "" else "com.google.android.apps.messaging",
    appName = "Messages · mock", title = when { options.missingText -> ""; options.longText -> "A deliberately long notification title to inspect wrapping and alignment"; else -> "Design review" },
    text = when { options.missingText -> ""; options.longText -> "Please review the revised island spacing, button alignment, and animation transitions before the next build."; else -> "The latest preview is ready to review." },
    actions = listOf("Reply", "Mark as read", "Open details", "Snooze").take(options.actionCount)
        .mapIndexed { index, label -> NotificationActionInfo("mock-action-$index", label) }
)
fun mockState(case: PlaygroundCase, options: PlaygroundOptions, expanded: Boolean? = null): IslandState {
    val notification = mockNotification(options)
    fun activity(kind: LiveActivityKind, title: String, subtitle: String) = IslandState.LiveActivity(LiveActivityInfo(
        id = "mock-${kind.name}", kind = kind, title = title, subtitle = subtitle, progress = .65f,
        isExpanded = expanded ?: true, primaryMetric = "03:35", maneuverType = "right", isLiveSource = false))
    return when (case) {
        PlaygroundCase.Hidden -> IslandState.Hidden
        PlaygroundCase.Idle -> IslandState.Minimal
        PlaygroundCase.Minimal, PlaygroundCase.Compact -> IslandState.Notification(listOf(notification), expanded ?: false)
        PlaygroundCase.Expanded -> IslandState.Notification(listOf(notification), expanded ?: true)
        PlaygroundCase.Music -> IslandState.Music(mockMusic, expanded ?: false)
        PlaygroundCase.IncomingCall -> IslandState.IncomingCall(ContactInfo(if (options.longText) "Alex Morgan · very long mock caller identity" else "Alex Morgan", ""))
        PlaygroundCase.ActiveCall -> IslandState.OngoingCall(ContactInfo("Alex Morgan", ""), 83, isExpanded = expanded ?: true)
        PlaygroundCase.Timer -> activity(LiveActivityKind.TIMER, "Focus timer", "03:35 remaining · simulated")
        PlaygroundCase.Charging -> IslandState.Charging(85)
        PlaygroundCase.LowBattery -> IslandState.Charging(12)
        PlaygroundCase.Navigation -> activity(LiveActivityKind.NAVIGATION, "Turn right in 200 m", "Market Street · 12 min remaining")
        PlaygroundCase.Recording -> activity(LiveActivityKind.VOICE_MEMO, "Voice recording", "00:42 · simulated microphone")
        PlaygroundCase.Multiple -> IslandState.NotificationWithMusic(listOf(notification, notification.copy(id = "mock-2", title = "Second mock activity")), mockMusic)
        PlaygroundCase.Success -> IslandState.Notification(listOf(notification.copy(title = "Download complete", text = "Your mock export is ready.", progress = 100, progressMax = 100)), expanded ?: true)
        PlaygroundCase.Warning -> IslandState.Notification(listOf(notification.copy(title = "Connection warning", text = "A simulated weak connection. No network setting changes.")), expanded ?: true)
        PlaygroundCase.Error -> IslandState.Notification(listOf(notification.copy(title = "Could not finish", text = "Simulated error. Tap Retry to test feedback.", actions = listOf(NotificationActionInfo("mock-retry", "Retry")))), expanded ?: true)
        PlaygroundCase.Controls -> IslandState.ActionControl(notificationCount = 2)
        PlaygroundCase.Access -> IslandState.PremiumExpiry(0)
    }
}
fun playgroundSelfChecks(): List<PlaygroundCheck> = buildList {
    for (case in PlaygroundCase.entries) {
        val state = mockState(case, PlaygroundOptions())
        val presentation = resolveOverlayDemoState(state, 1)
        val expected = when (case) {
            PlaygroundCase.Hidden, PlaygroundCase.Idle -> IslandDemoState.Idle
            PlaygroundCase.Minimal, PlaygroundCase.Compact -> IslandDemoState.NotificationCompact
            PlaygroundCase.Expanded, PlaygroundCase.Success, PlaygroundCase.Warning, PlaygroundCase.Error -> IslandDemoState.NotificationExpanded
            PlaygroundCase.Music -> IslandDemoState.MusicCompact
            PlaygroundCase.IncomingCall -> IslandDemoState.CallAvatars
            PlaygroundCase.ActiveCall -> IslandDemoState.CallExpanded
            PlaygroundCase.Timer -> IslandDemoState.TimerExpanded
            PlaygroundCase.Charging -> IslandDemoState.ChargingCompact
            PlaygroundCase.LowBattery -> IslandDemoState.LowBatteryCompact
            PlaygroundCase.Navigation -> IslandDemoState.ColorOptions
            PlaygroundCase.Recording -> IslandDemoState.VoiceMemoExpanded
            PlaygroundCase.Multiple -> IslandDemoState.NotificationWithMusicCompact
            PlaygroundCase.Controls -> IslandDemoState.ActionControlExpanded
            PlaygroundCase.Access -> IslandDemoState.PremiumExpiryExpanded
        }
        add(PlaygroundCheck("${case.title}: state mapping", presentation == expected, presentation.name))
    }
    for (width in listOf(320f, 360f, 393f, 430f)) {
        val expanded = IslandGeometryConstraints.expandedWidth(width, 11f, 408f)
        add(PlaygroundCheck("${width.toInt()}dp bounds", expanded <= width - 22f && expanded > 0, "Expanded budget ${expanded.toInt()}dp"))
    }
    val compact = mockState(PlaygroundCase.Compact, PlaygroundOptions())
    add(PlaygroundCheck("Notification tap policy", IslandInteractionPolicy.mainTap(compact) == IslandInteractionAction.OPEN_NOTIFICATIONS, "Uses production IslandInteractionPolicy"))
    val hole = IslandCutout(170, 0, 190, 32)
    val placement = IslandPlacementPolicy.resolve(360, 800, 80, 48, 0, 11, false, 28, 11, 4, listOf(hole))
    add(PlaygroundCheck("Camera clearance", placement.top >= 36, "Compact content avoids physical camera pixels"))
}

object PlaygroundSession {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val controller = PlaygroundController(scope)
    val overlayRunning = MutableStateFlow(false)
    val physicalInfo = MutableStateFlow("Physical overlay not running")
    private var activityVisible = false
    private var previousSuppression: Boolean? = null
    fun activity(visible: Boolean) { activityVisible = visible; sync() }
    fun overlay(running: Boolean) { overlayRunning.value = running; sync() }
    private fun sync() {
        val manager = ai.emots.kishan_dynamic.service.IslandStateManager
        if (activityVisible || overlayRunning.value) {
            if (previousSuppression == null) { previousSuppression = manager.isPaywallActive.value; controller.startClock() }
            manager.setPaywallActive(true)
        } else {
            previousSuppression?.let(manager::setPaywallActive)
            previousSuppression = null
            controller.stopClock(); controller.cancelSequence()
        }
    }
}
