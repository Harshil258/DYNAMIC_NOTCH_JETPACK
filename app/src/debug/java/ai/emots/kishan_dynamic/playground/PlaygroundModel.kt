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
    val verticalPadding: Float = 14f, val iconSize: Float = 18f, val textGap: Float = 8f,
    val corner: Float = 20f, val overrideCorner: Boolean = false,
    val cameraClearance: Float = 4f, val actionSize: Float = 34f, val actionGap: Float = 10f
)
data class PlaygroundSnapshot(
    val selected: PlaygroundCase = PlaygroundCase.Idle,
    val state: IslandState = IslandState.Minimal,
    val options: PlaygroundOptions = PlaygroundOptions(),
    val feedback: String = "Mock data · production renderer",
    val replyingNotificationId: String? = null, val replyAction: NotificationActionInfo? = null,
    val timerSeconds: Int = 215, val recordingSeconds: Int = 42,
    val behavior: ai.emots.kishan_dynamic.data.notification.IslandBehaviorState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(),
    val layeredRedaction: Boolean = false, val eventTrace: List<String> = emptyList(),
    val replay: Int = 0, val sequenceRunning: Boolean = false, val eventCount: Int = 0
)
data class PlaygroundCheck(val name: String, val passed: Boolean, val detail: String)

class PlaygroundController(
    private val scope: CoroutineScope,
    private val behaviorEngine: ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngine = ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngine()
) {
    private val mutable = MutableStateFlow(PlaygroundSnapshot())
    val snapshot = mutable.asStateFlow()
    val replyDrafts = ai.emots.kishan_dynamic.data.notification.ReplyDraftStore()
    private var previewTimer: Job? = null
    private var previewTimerToken: Long? = null
    private fun cancelPreviewTimer() { previewTimer?.cancel(); previewTimer = null; previewTimerToken = null }
    private val childTimers = mutableMapOf<String, Job>()
    private var sequence: Job? = null
    private var clock: Job? = null
    val checks = MutableStateFlow<List<PlaygroundCheck>>(emptyList())
    var engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState()
        private set

    fun dispatchEngineEvent(
        event: ai.emots.kishan_dynamic.data.notification.IslandEngineEvent,
        nowMonotonic: Long = System.nanoTime() / 1_000_000L
    ): ai.emots.kishan_dynamic.data.notification.IslandBehaviorDecision {
        val decision = behaviorEngine.reduce(engineState, event, nowMonotonic)
        engineState = decision.state
        if (event is ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PrivacyChanged)
            replyDrafts.privacyRestricted(event.policy.deviceLocked)
        replyDrafts.retainSources(engineState.queue.notifications.map { it.id }.toSet())
        val replyId = mutable.value.replyingNotificationId
        if (replyId != null && (engineState.childPrivacy.deviceLocked || engineState.queue.notifications.none { it.id == replyId }))
            mutable.update { it.copy(replyingNotificationId = null, replyAction = null) }
        childTimers.keys.filterNot { k -> engineState.childNotices.any { it.key == k } }.forEach { childTimers.remove(it)?.cancel() }
        decision.effects.forEach { effect ->
            when(effect) {
                is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.StartPreviewTimer -> {
                    cancelPreviewTimer()
                    previewTimerToken = effect.token
                    previewTimer = scope.launch {
                        delay(effect.durationMs)
                        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviewTimeout(effect.token))
                    }
                }
                is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.CancelTimer -> if (previewTimerToken == effect.token) cancelPreviewTimer()

                is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.StartChildTimer -> {
                    childTimers.remove(effect.childKey)?.cancel()
                    childTimers[effect.childKey] = scope.launch {
                        delay(effect.durationMs)
                        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ChildTimeout(effect.childKey, effect.token))
                    }
                }
                is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.CancelChildTimer -> childTimers.remove(effect.childKey)?.cancel()
                else -> Unit // Sandbox never executes Android cancellation or external PendingIntents.
            }
        }
        val trace = "${event.javaClass.simpleName}: ${decision.reason}"
        if (event is ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ChildPhaseChanged ||
            event is ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ChildVisibleLimitChanged ||
            event is ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PrivacyChanged) {
            // Presentation observation must not replace a selected music/call/demo fixture.
            mutable.update { it.copy(behavior = engineState, eventTrace = (listOf(trace) + it.eventTrace).take(80), eventCount = it.eventCount + 1) }
        } else syncStateFromEngine(decision, trace)
        return decision
    }

    private fun syncStateFromEngine(decision: ai.emots.kishan_dynamic.data.notification.IslandBehaviorDecision? = null, trace: String? = null) {
        val q = engineState.queue
        val presentation = engineState.targetPresentation
        val currentSnap = mutable.value
        val newState = when {
            engineState.ownership == ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.INTERRUPTED ->
                IslandState.IncomingCall(ai.emots.kishan_dynamic.data.model.ContactInfo("Mock caller", ""))
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.QUICK_CONTROLS -> {
                IslandState.ActionControl(notificationCount = q.notifications.size)
            }
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.TRANSIENT_PREVIEW ||
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.USER_EXPANDED ||
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.PAGER ||
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.REPLY -> {
                if (q.notifications.isNotEmpty()) {
                    IslandState.Notification(
                        notifications = q.notifications,
                        isExpanded = true,
                        activeIndex = q.activeIndex,
                        expandedByUser = (presentation != ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.TRANSIENT_PREVIEW)
                    )
                } else IslandState.Minimal
            }
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.COMPACT -> {
                if (q.notifications.isNotEmpty()) {
                    IslandState.Notification(
                        notifications = q.notifications,
                        isExpanded = false,
                        activeIndex = q.activeIndex
                    )
                } else IslandState.Minimal
            }
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.IDLE ||
            presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.HIDDEN -> {
                if (presentation == ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.HIDDEN) {
                    IslandState.Hidden
                } else {
                    IslandState.Minimal
                }
            }
            else -> currentSnap.state
        }
        val newSelected = when (newState) {
            is IslandState.ActionControl -> PlaygroundCase.Controls
            is IslandState.Notification -> if (newState.isExpanded) PlaygroundCase.Expanded else PlaygroundCase.Compact
            is IslandState.Minimal -> PlaygroundCase.Idle
            is IslandState.Hidden -> PlaygroundCase.Hidden
            else -> currentSnap.selected
        }
        val actionText = decision?.action?.name ?: "Synchronized"
        mutable.update {
            it.copy(
                behavior = engineState,
                eventTrace = if (trace == null) it.eventTrace else (listOf(trace) + it.eventTrace).take(80),
                selected = newSelected,
                state = newState,
                feedback = "Engine: $actionText · ${engineState.ownership.name} · ${q.notifications.size} queued",
                eventCount = it.eventCount + 1
            )
        }
    }

    fun options(change: (PlaygroundOptions) -> PlaygroundOptions) {
        mutable.update { it.copy(options = change(it.options)) }
        // Refresh mock copy immediately, keeping the current expansion when possible.
        val current = mutable.value
        if (current.behavior.childNotices.isNotEmpty() || current.behavior.queue.notifications.size > 1) {
            syncStateFromEngine()
            return
        }
        mutable.update { it.copy(state = mockState(current.selected, current.options,
            expanded = current.state.presentationMode == IslandPresentationMode.EXPANDED)) }
    }

    fun select(case: PlaygroundCase) {
        cancelPreviewTimer()
        replyDrafts.retainSources(emptySet())
        replyDrafts.privacyRestricted(mutable.value.layeredRedaction)
        mutable.update { it.copy(replyingNotificationId = null, replyAction = null) }
        cancelSequence()
        childTimers.values.forEach { it.cancel() }; childTimers.clear()
        when (case) {
            PlaygroundCase.Idle -> {
                engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState()
            }
            PlaygroundCase.Hidden -> {
                engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(
                    targetPresentation = ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.HIDDEN
                )
            }
            PlaygroundCase.Compact, PlaygroundCase.Minimal -> {
                val n = mockNotification(mutable.value.options)
                engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(
                    queue = ai.emots.kishan_dynamic.data.notification.NotificationQueueState(listOf(n)),
                    selectedKey = n.id,
                    targetPresentation = ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.COMPACT
                )
            }
            PlaygroundCase.Expanded -> {
                val n = mockNotification(mutable.value.options)
                engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(
                    queue = ai.emots.kishan_dynamic.data.notification.NotificationQueueState(listOf(n)),
                    selectedKey = n.id,
                    ownership = ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.USER_EXPANDED,
                    targetPresentation = ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.USER_EXPANDED
                )
            }
            PlaygroundCase.Controls -> {
                engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(
                    ownership = ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.USER_EXPANDED,
                    targetPresentation = ai.emots.kishan_dynamic.data.notification.IslandPresentationTarget.QUICK_CONTROLS
                )
            }
            else -> { engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState() }
        }
        engineState = engineState.copy(childPrivacy = ai.emots.kishan_dynamic.data.notification.ChildPrivacyPolicy(mutable.value.layeredRedaction, true))
        emit(case)
    }

    private fun emit(case: PlaygroundCase) {
        mutable.update { it.copy(selected = case, behavior = engineState, state = mockState(case, it.options),
            feedback = "${case.title} · simulated fixture", eventCount = it.eventCount + 1) }
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

    fun stopClock() { cancelPreviewTimer(); clock?.cancel(); clock = null; childTimers.values.forEach { it.cancel() }; childTimers.clear() }

    fun selectNotification(id: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.SelectPage(id))
    }

    fun openNotifications(selectNext: Boolean = false) {
        if (engineState.queue.notifications.isEmpty()) {
            message("No notifications in queue · empty state retained")
            return
        }
        if (selectNext && engineState.queue.notifications.size > 1) {
            dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.NextPage)
        } else {
            dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact)
        }
    }

    fun dismissActiveNotification() {
        val activeId = engineState.selectedKey ?: engineState.queue.activeNotificationId
        if (activeId != null) {
            dismissNotification(activeId)
        } else {
            select(PlaygroundCase.Idle)
        }
    }

    fun dismissNotification(id: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCloseButton(id))
    }

    fun removeSourceNotification(id: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.RemoveNotification(id))
    }

    fun postSyntheticNotification(notification: NotificationInfo = mockNotification(mutable.value.options)) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(notification))
    }

    fun touchChild(childKey: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TouchChild(childKey))
    }

    fun releaseChild(childKey: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ReleaseChild(childKey))
    }

    fun childTimeout(childKey: String, token: Long) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ChildTimeout(childKey, token))
    }

    fun promoteChild(childKey: String) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapChild(childKey))
    }

    fun previewTimeout(token: Long = engineState.activeToken) {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviewTimeout(token))
    }

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
            else -> IslandInteractionPolicy.mainTap(state)
        }
        when (action) {
            IslandInteractionAction.NONE -> message("No action for this production interaction policy")
            IslandInteractionAction.OPEN_QUICK_CONTROLS -> {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact)
            }
            IslandInteractionAction.OPEN_MUSIC -> mutable.update { it.copy(state = IslandState.Music(mockMusic, true)) }
            IslandInteractionAction.OPEN_NOTIFICATIONS -> {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact)
            }
            IslandInteractionAction.OPEN_NEXT_NOTIFICATION -> {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.NextPage)
            }
            IslandInteractionAction.TOGGLE_EXPANSION -> expand(true)
        }
    }

    fun expand(expanded: Boolean) {
        if (!expanded) {
            if (mutable.value.state is IslandState.Notification) {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.SwipeUp)
                return
            }
        } else {
            if (mutable.value.state is IslandState.Notification || mutable.value.state is IslandState.Minimal) {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact)
                return
            }
        }
        mutable.update { current ->
            current.copy(state = when (val state = current.state) {
                is IslandState.Notification -> state.copy(isExpanded = expanded, expandedByUser = expanded)
                is IslandState.Music -> state.copy(isExpanded = expanded)
                is IslandState.OngoingCall -> state.copy(isExpanded = expanded)
                is IslandState.LiveActivity -> state.copy(activity = state.activity.copy(isExpanded = expanded))
                is IslandState.RingerMode -> state.copy(isExpanded = expanded)
                else -> if (expanded) state else IslandState.Minimal
            })
        }
    }

    fun startReply(id: String, action: NotificationActionInfo) {
        val rawSource = engineState.queue.notifications.firstOrNull { it.id == id } ?: return
        val source = ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy.notification(rawSource, mutable.value.layeredRedaction, true)
        if (!source.actions.any { it.id == action.id && it.isReply }) return
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.SelectPage(id))
        mutable.update { it.copy(replyingNotificationId = id, replyAction = action) }
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.StartReply)
        message("Mock reply editor · no external message can be sent")
    }

    fun finishReply(simulatedSend: Boolean) {
        if (engineState.ownership == ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.INTERRUPTED) return
        val id = mutable.value.replyingNotificationId
        if (simulatedSend && id != null) replyDrafts.clear(id)
        mutable.update { it.copy(replyingNotificationId = null, replyAction = null) }
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ExitReply)
        message(if (simulatedSend) "Mock send acknowledged · no message sent" else "Draft retained in unlocked memory for up to 60 seconds")
    }

    fun control(action: IslandControlAction) {
        if (engineState.ownership == ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.INTERRUPTED) {
            if (action == IslandControlAction.CallAccept) {
                mutable.update { it.copy(state = IslandState.OngoingCall(ContactInfo("Mock caller", ""), durationSeconds = 0L)) }
                return
            }
            if (action == IslandControlAction.CallEnd || action == IslandControlAction.CallDecline) {
                dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.CallEnded)
                return
            }
        }

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
            var delivered = 0
            try {
                for (i in 1..30) {
                    val notification = mockNotification(mutable.value.options).copy(id = "mock-$i", title = "Mock update $i")
                    dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(notification))
                    delivered++
                    delay(60)
                }
            } finally { mutable.update { it.copy(sequenceRunning = false,
                feedback = "$delivered / 30 mock arrivals · ${engineState.canonicalQueueCount} retained") } }
        }
    }

    fun layeredScenario(count: Int) {
        loadNotificationScenario(1)
        repeat(count) { addLayeredArrival() }
    }
    private var arrivalId = 0
    fun addLayeredArrival() {
        arrivalId++
        val apps = listOf("Telegram", "Gmail", "Instagram")
        val app = apps[(arrivalId - 1) % apps.size]
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(
            mockNotification(mutable.value.options).copy(id = "layer-$arrivalId", appName = app,
                packageName = when(app) { "Telegram" -> "org.telegram.messenger"; "Gmail" -> "com.google.android.gm"; else -> "com.instagram.android" },
                title = "Alex · update $arrivalId", text = "Synthetic message preview. Your main reading stays in place.", timestamp = System.currentTimeMillis() + arrivalId)))
    }
    fun layeredRedaction() {
        mutable.update { it.copy(layeredRedaction = !it.layeredRedaction) }
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PrivacyChanged(
            ai.emots.kishan_dynamic.data.notification.ChildPrivacyPolicy(mutable.value.layeredRedaction, true)))
    }

    fun loadNotificationScenario(count: Int, preserveSequence: Boolean = false) {
        cancelPreviewTimer()
        replyDrafts.retainSources(emptySet())
        replyDrafts.privacyRestricted(mutable.value.layeredRedaction)
        mutable.update { it.copy(replyingNotificationId = null, replyAction = null) }
        if (!preserveSequence) cancelSequence()
        childTimers.values.forEach { it.cancel() }; childTimers.clear()
        engineState = ai.emots.kishan_dynamic.data.notification.IslandBehaviorState()
        if (count == 0) {
            select(PlaygroundCase.Idle)
            return
        }
        val list = (1..count).map { i ->
            mockNotification(mutable.value.options).copy(
                id = "mock-$i",
                title = "Notification $i",
                text = "Message content for item $i of $count."
            )
        }
        for (item in list.reversed()) {
            engineState = behaviorEngine.reduce(
                engineState,
                ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(item, autoExpand = false),
                0L
            ).state
        }
        val expandDecision = behaviorEngine.reduce(
            engineState,
            ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact,
            0L
        )
        engineState = expandDecision.state
        syncStateFromEngine(expandDecision)
        message("Loaded $count notifications scenario (${if (count == 1) "Single card" else "Horizontal Pager"})")
    }

    fun scenarioPage5Arrival() {
        loadNotificationScenario(12)
        val targetId = "mock-5"
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.SelectPage(targetId))
        val newNotice = mockNotification(mutable.value.options).copy(
            id = "mock-new-arrival",
            title = "New Arrival while on Page 5",
            text = "Notice spawned while reader is inspecting page 5."
        )
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(newNotice, autoExpand = true))
        message("Page 5 arrival: reading selection pinned at $targetId")
    }

    fun scenarioSelectedRemoval() {
        val selected = engineState.selectedKey ?: "mock-1"
        dismissNotification(selected)
        message("Selected removal: dismissed $selected, neighbor selected")
    }

    fun scenarioNonSelectedRemoval() {
        val active = engineState.selectedKey
        val nonSelected = engineState.queue.notifications.firstOrNull { it.id != active }?.id
        if (nonSelected != null) {
            removeSourceNotification(nonSelected)
            message("Non-selected removal: removed $nonSelected, active $active kept")
        }
    }

    fun scenarioSameKeyUpdate() {
        val active = engineState.selectedKey ?: "mock-1"
        val updated = mockNotification(mutable.value.options).copy(
            id = active,
            title = "Updated in-place!",
            text = "Content refreshed at ${System.currentTimeMillis() % 10000}ms without changing page."
        )
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.UpdateNotification(updated))
        message("Same-key update applied in-place to $active")
    }

    fun scenarioCallRestore() {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IncomingCallPreempt)
        message("Incoming call preempted island")
    }

    fun scenarioCallEndRestore() {
        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.CallEnded)
        message("Call ended: island restored")
    }

    fun cancelSequence() { sequence?.cancel(); sequence = null; mutable.update { it.copy(sequenceRunning = false) } }

    fun runLayeredAnimationBenchmark(
        cycles: Int = 12,
        onProgress: ((currentCycle: Int, totalCycles: Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ): Job {
        cancelSequence()
        val job = scope.launch {
            mutable.update { it.copy(sequenceRunning = true, feedback = "Running Layered Animation Benchmark ($cycles cycles)...") }
            try {
                options { it.copy(reducedMotion = false, motionOverride = false, guides = false) }
                delay(200)

                for (c in 1..cycles) {
                    onProgress?.invoke(c, cycles)
                    mutable.update { it.copy(feedback = "Benchmark cycle $c / $cycles in progress...") }

                    // Phase 1: Initialize multi-notification scenario (6 items, expanded island)
                    loadNotificationScenario(6, preserveSequence = true)
                    delay(320)

                    // Phase 2: Sequential child arrivals (1, 2, 3 children emerge and settle)
                    for (a in 1..3) {
                        addLayeredArrival()
                        delay(260)
                    }

                    // Phase 3: Pager swiping across pages (3 forward, 1 back)
                    for (p in 1..3) {
                        dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.NextPage)
                        delay(220)
                    }
                    dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviousPage)
                    delay(220)

                    // Phase 4: Child promotion (promotes first child into main island)
                    val childKey = engineState.childNotices.firstOrNull()?.key
                    if (childKey != null) {
                        promoteChild(childKey)
                        delay(360)
                    }

                    // Phase 5: Child dismissal / merge
                    val remainingChild = engineState.childNotices.firstOrNull()?.key
                    if (remainingChild != null) {
                        dismissNotification(remainingChild)
                        delay(260)
                    }

                    // Phase 6: Swipe-up collapse to compact pill
                    expand(false)
                    delay(300)

                    // Phase 7: Re-expand island
                    expand(true)
                    delay(340)
                }
                mutable.update { it.copy(feedback = "Layered Animation Benchmark completed ($cycles cycles)") }
            } finally {
                mutable.update { it.copy(sequenceRunning = false) }
                onComplete?.invoke()
            }
        }
        sequence = job
        return job
    }

    fun runChecks() { checks.value = playgroundSelfChecks() }
}

val mockMusic = MusicTrack("Midnight Drive", "Aurora Studio · mock media", durationMs = 240000, positionMs = 75000)
fun mockNotification(options: PlaygroundOptions) = NotificationInfo(
    id = "mock-message", packageName = if (options.missingIcon) "" else "com.google.android.apps.messaging",
    appName = "Messages · mock", title = when { options.missingText -> ""; options.longText -> "A deliberately long notification title to inspect wrapping and alignment"; else -> "Design review" },
    text = when { options.missingText -> ""; options.longText -> "Please review the revised island spacing, button alignment, and animation transitions before the next build."; else -> "The latest preview is ready to review." },
    actions = listOf("Reply", "Mark as read", "Open details", "Snooze").take(options.actionCount)
        .mapIndexed { index, label -> NotificationActionInfo("mock-action-$index", label, isReply = index == 0) }
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
            PlaygroundCase.Navigation -> IslandDemoState.TransportActivity
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
    add(PlaygroundCheck("Idle tap policy", IslandInteractionPolicy.mainTap(IslandState.Minimal) == IslandInteractionAction.OPEN_QUICK_CONTROLS, "Idle tap opens Quick Controls per UX-C01"))
    val engine = ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngine()
    val idleDecision = engine.reduce(ai.emots.kishan_dynamic.data.notification.IslandBehaviorState(), ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapCompact, 0L)
    add(PlaygroundCheck("Shared Behavior Engine", idleDecision.action == ai.emots.kishan_dynamic.data.notification.IslandDomainAction.OpenQuickControls, "Playground shares canonical IslandBehaviorEngine policy"))
    add(PlaygroundCheck("Queue Capacity", ai.emots.kishan_dynamic.data.notification.NotificationReducer.MAX_NOTIFICATIONS == 50, "Capacity is 50 per Acceptance Matrix A04"))
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
