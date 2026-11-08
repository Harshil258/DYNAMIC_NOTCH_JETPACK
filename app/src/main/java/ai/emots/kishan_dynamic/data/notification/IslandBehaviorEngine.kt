package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Interaction ownership modes determining whether an arriving event can
 * displace or mutate the current visual presentation.
 */
enum class IslandInteractionOwnership {
    SYSTEM_IDLE,
    TRANSIENT_PREVIEW,
    USER_EXPANDED,
    PAGER_ACTIVE,
    REPLY_ACTIVE,
    CHILD_INTERACTION,
    INTERRUPTED
}

/**
 * Pure target presentation state describing how large the island renders.
 */
enum class IslandPresentationTarget {
    HIDDEN,
    IDLE,
    COMPACT,
    TRANSIENT_PREVIEW,
    USER_EXPANDED,
    PAGER,
    REPLY,
    QUICK_CONTROLS
}

/**
 * Explicit domain action output describing what decision was made.
 */
enum class IslandDomainAction {
    OpenQuickControls,
    ExpandNotification,
    OpenPager,
    CollapseToCompact,
    KeepSelectedKey,
    SelectNextOrPrevious,
    UpsertChildNotice,
    ResurfaceMilestone,
    SuppressMinorUpdate,
    DismissIslandOnly,
    RequestSystemDismiss,
    None
}

/**
 * A secondary child satellite notification emerging below the main island.
 * Implements UX-C11, UX-C12, UX-C13, UX-C14, UX-C15.
 */
enum class ChildLifecycle { QUEUED, EMERGING, SETTLING, VISIBLE, TOUCH_HELD, RELEASED, EXITING, MERGED, PROMOTING, MAIN_SELECTED }
data class ChildPrivacyPolicy(val deviceLocked: Boolean = false, val hideSensitiveContent: Boolean = true)

data class ChildNotice(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val expiresAtMonotonic: Long,
    val isMilestone: Boolean = false,
    val isPaused: Boolean = false,
    val token: Long = 0L,
    val remainingMs: Long = 0L,
    val arrivalToken: Long = token,
    val hasSettled: Boolean = false
)

/**
 * Pure side effects emitted by the behavior engine.
 * Never performs platform calls, coroutines, or View updates directly.
 */
sealed interface IslandSideEffect {
    data class CancelAndroidNotification(val notificationId: String) : IslandSideEffect
    data class LaunchContentIntent(val notificationId: String) : IslandSideEffect
    data class StartPreviewTimer(val token: Long, val durationMs: Long) : IslandSideEffect
    data class StartChildTimer(val childKey: String, val token: Long, val durationMs: Long) : IslandSideEffect
    data class CancelChildTimer(val childKey: String) : IslandSideEffect
    data class CancelTimer(val token: Long) : IslandSideEffect
    data class LogTrace(val record: IslandTraceRecord) : IslandSideEffect
}

/**
 * Input events consumed by the canonical behavior engine.
 */
sealed interface IslandEngineEvent {
    data class IngestNotification(
        val notification: NotificationInfo,
        val isRestored: Boolean = false,
        val isMilestone: Boolean = false,
        val autoExpand: Boolean = true
    ) : IslandEngineEvent

    data class UpdateNotification(val notification: NotificationInfo) : IslandEngineEvent
    data class RemoveNotification(val notificationId: String) : IslandEngineEvent
    data class ReconcileSources(val activeKeys: Set<String>) : IslandEngineEvent
    data class ChildPhaseChanged(val key: String, val phase: ChildLifecycle) : IslandEngineEvent
    data class ChildVisibleLimitChanged(val limit: Int) : IslandEngineEvent
    data class PrivacyChanged(val policy: ChildPrivacyPolicy) : IslandEngineEvent
    data object TapCompact : IslandEngineEvent
    data object TapExpandedBody : IslandEngineEvent
    data class TapChild(val childKey: String, val expectedArrivalToken: Long? = null) : IslandEngineEvent
    data class TapCloseButton(val key: String? = null) : IslandEngineEvent
    data object SwipeUp : IslandEngineEvent
    data object OutsideTap : IslandEngineEvent
    data class PreviewTimeout(val token: Long) : IslandEngineEvent
    data class ChildTimeout(val childKey: String, val token: Long) : IslandEngineEvent
    data class SelectPage(val notificationId: String) : IslandEngineEvent
    data object NextPage : IslandEngineEvent
    data object PreviousPage : IslandEngineEvent
    data object IncomingCallPreempt : IslandEngineEvent
    data object CallEnded : IslandEngineEvent
    data object StartReply : IslandEngineEvent
    data object ExitReply : IslandEngineEvent
    data object DoubleTapCompact : IslandEngineEvent
    data class TouchChild(val childKey: String) : IslandEngineEvent
    data class ReleaseChild(val childKey: String) : IslandEngineEvent
    data class ArtworkUpdated(
        val notificationId: String,
        val imagePath: String?,
        val picturePath: String?
    ) : IslandEngineEvent
}

/**
 * Injected user configuration settings for Island behavior.
 */
data class IslandBehaviorSettings(
    val defaultOsDismiss: Boolean = false,
    val maxVisibleChildren: Int = 2,
    val maxChildNotices: Int = 50,
    val previewDurationMs: Long = 3500L,
    val childDurationMs: Long = 5000L,
    val childReleaseGraceMs: Long = 2000L,
    val milestoneDurationMs: Long = 7000L
)

/**
 * Complete immutable snapshot of the Dynamic Island behavioral state.
 */
data class IslandBehaviorState(
    val queue: NotificationQueueState = NotificationQueueState(),
    val activeDomain: IslandState = IslandState.Minimal,
    val selectedKey: String? = null,
    val ownership: IslandInteractionOwnership = IslandInteractionOwnership.SYSTEM_IDLE,
    val targetPresentation: IslandPresentationTarget = IslandPresentationTarget.IDLE,
    val childNotices: List<ChildNotice> = emptyList(),
    val childLifecycles: Map<String, ChildLifecycle> = emptyMap(),
    val childPrivacy: ChildPrivacyPolicy = ChildPrivacyPolicy(),
    val visibleChildLimit: Int = 2,
    val tombstones: Map<String, IslandTombstone> = emptyMap(),
    val activeToken: Long = 0L,
    val nextChildToken: Long = 1L,
    val previousOwnership: IslandInteractionOwnership = IslandInteractionOwnership.SYSTEM_IDLE,
    val suspendedBeforeCall: IslandBehaviorState? = null
) {
    val canonicalQueueCount: Int
        get() = queue.notifications.size

    val pendingChildOverflowCount: Int
        get() = (childNotices.size - visibleChildLimit).coerceAtLeast(0)

    val pendingChildNoticesCount: Int
        get() = childNotices.size

    fun visibleChildCount(limit: Int = 2): Int =
        minOf(childNotices.size, limit)

    fun childOverflowCount(limit: Int = 2): Int =
        (childNotices.size - limit).coerceAtLeast(0)

    val totalPendingCount: Int
        get() = (queue.notifications.size - 1).coerceAtLeast(0)

    fun overflowCount(visibleChildCount: Int): Int =
        (queue.notifications.size - 1 - visibleChildCount).coerceAtLeast(0)
}

/**
 * Explicit decision bundle returned by the behavior engine.
 */
data class IslandBehaviorDecision(
    val state: IslandBehaviorState,
    val action: IslandDomainAction,
    val effects: List<IslandSideEffect> = emptyList(),
    val reason: String = ""
)

/**
 * Canonical Dynamic Island pure behavior decision engine.
 * Single source of truth for all interaction rules, priority arbitration,
 * queue mutation, and lifecycle transitions.
 */
class IslandBehaviorEngine(
    private val reducer: NotificationReducer = NotificationReducer()
) {

    fun reduce(
        state: IslandBehaviorState,
        event: IslandEngineEvent,
        nowMonotonic: Long,
        settings: IslandBehaviorSettings = IslandBehaviorSettings()
    ): IslandBehaviorDecision {
        val initialOwnership = state.ownership
        val initialPresentation = state.targetPresentation

        val rawDecision = when (event) {
            is IslandEngineEvent.ChildVisibleLimitChanged -> IslandBehaviorDecision(state.copy(visibleChildLimit = event.limit.coerceIn(0, 3)), IslandDomainAction.None)
            is IslandEngineEvent.PrivacyChanged -> IslandBehaviorDecision(state.copy(childPrivacy = event.policy), IslandDomainAction.None)
            is IslandEngineEvent.ChildPhaseChanged -> {
                val phases = state.childLifecycles.toMutableMap()
                if (event.phase == ChildLifecycle.MERGED || event.phase == ChildLifecycle.MAIN_SELECTED) phases.remove(event.key)
                else if (event.key in phases) phases[event.key] = event.phase
                val child = state.childNotices.firstOrNull { it.key == event.key }
                if (event.phase == ChildLifecycle.VISIBLE && child != null && !child.hasSettled) {
                    // Visible readability budget starts once. Overflow still has an arrival TTL.
                    val duration = if (child.isMilestone) settings.milestoneDurationMs else settings.childDurationMs
                    val token = state.nextChildToken
                    val settled = child.copy(hasSettled = true, token = token,
                        expiresAtMonotonic = nowMonotonic + duration,
                        remainingMs = if (child.isPaused) maxOf(child.remainingMs, duration) else child.remainingMs)
                    IslandBehaviorDecision(state.copy(childLifecycles = phases,
                        childNotices = state.childNotices.map { if (it.key == child.key) settled else it },
                        nextChildToken = token + 1), IslandDomainAction.None,
                        effects = if (child.isPaused) emptyList() else listOf(IslandSideEffect.StartChildTimer(child.key, token, duration)))
                } else IslandBehaviorDecision(state.copy(childLifecycles = phases), IslandDomainAction.None)
            }
            is IslandEngineEvent.IngestNotification -> handleIngest(state, event, nowMonotonic, settings)
            is IslandEngineEvent.UpdateNotification -> handleUpdate(state, event, nowMonotonic, settings)
            is IslandEngineEvent.RemoveNotification -> handleRemove(state, event.notificationId)
            is IslandEngineEvent.ReconcileSources -> handleReconcile(state, event.activeKeys)
            is IslandEngineEvent.TapCompact -> handleTapCompact(state)
            is IslandEngineEvent.TapExpandedBody -> handleTapExpandedBody(state)
            is IslandEngineEvent.TapChild -> handleTapChild(state, event.childKey, event.expectedArrivalToken)
            is IslandEngineEvent.TapCloseButton -> handleCloseButton(state, event.key, nowMonotonic, settings)
            is IslandEngineEvent.SwipeUp -> handleSwipeUp(state)
            is IslandEngineEvent.OutsideTap -> handleOutsideTap(state)
            is IslandEngineEvent.PreviewTimeout -> handlePreviewTimeout(state, event.token)
            is IslandEngineEvent.ChildTimeout -> handleChildTimeout(state, event.childKey, event.token)
            is IslandEngineEvent.SelectPage -> handleSelectPage(state, event.notificationId)
            is IslandEngineEvent.NextPage -> handleNextPage(state)
            is IslandEngineEvent.PreviousPage -> handlePreviousPage(state)
            is IslandEngineEvent.IncomingCallPreempt -> handleIncomingCall(state)
            is IslandEngineEvent.CallEnded -> handleCallEnded(state)
            is IslandEngineEvent.StartReply -> handleStartReply(state)
            is IslandEngineEvent.ExitReply -> handleExitReply(state)
            is IslandEngineEvent.DoubleTapCompact -> handleDoubleTapCompact(state)
            is IslandEngineEvent.TouchChild -> handleTouchChild(state, event.childKey, nowMonotonic)
            is IslandEngineEvent.ReleaseChild -> handleReleaseChild(state, event.childKey, nowMonotonic, settings)
            is IslandEngineEvent.ArtworkUpdated -> handleArtworkUpdated(state, event)
        }

        val phases = rawDecision.state.childLifecycles.toMutableMap()
        rawDecision.state.childNotices.forEach {
            phases.putIfAbsent(it.key, ChildLifecycle.QUEUED)
            if (it.isPaused) phases[it.key] = ChildLifecycle.TOUCH_HELD
            else if (event is IslandEngineEvent.ReleaseChild && event.childKey == it.key) phases[it.key] = ChildLifecycle.RELEASED
        }
        state.childNotices.filterNot { old -> rawDecision.state.childNotices.any { it.key == old.key } }.forEach {
            phases[it.key] = if (event is IslandEngineEvent.TapChild && event.childKey == it.key) ChildLifecycle.MAIN_SELECTED else ChildLifecycle.EXITING
        }
        // At most 50 pending + three outgoing render identities. Terminal entries age out deterministically.
        val boundedPhases = phases.entries.toList().takeLast(53).associate { it.key to it.value }
        val retainedKeys = rawDecision.state.queue.notifications.map { it.id }.toSet()
        val decision = rawDecision.copy(state = rawDecision.state.copy(
            ownership = if (rawDecision.state.ownership == IslandInteractionOwnership.CHILD_INTERACTION && rawDecision.state.childNotices.none { it.isPaused }) rawDecision.state.previousOwnership else rawDecision.state.ownership,
            childLifecycles = if (rawDecision.state.ownership == IslandInteractionOwnership.INTERRUPTED ||
                rawDecision.state.targetPresentation in setOf(IslandPresentationTarget.COMPACT, IslandPresentationTarget.IDLE, IslandPresentationTarget.HIDDEN, IslandPresentationTarget.QUICK_CONTROLS)) emptyMap() else boundedPhases,
            childNotices = rawDecision.state.childNotices.filter { it.key in retainedKeys && it.key != rawDecision.state.selectedKey }
        ))
        // Generate debug trace record
        val traceRecord = IslandEventTrace.record(
            nowMonotonic = nowMonotonic,
            eventType = event.javaClass.simpleName,
            rawKey = state.selectedKey,
            queueCount = decision.state.queue.notifications.size,
            ownershipBefore = initialOwnership.name,
            ownershipAfter = decision.state.ownership.name,
            presentationBefore = initialPresentation.name,
            presentationAfter = decision.state.targetPresentation.name,
            action = decision.action.name,
            reason = decision.reason
        )

        return decision.copy(effects = decision.effects + IslandSideEffect.LogTrace(traceRecord))
    }

    private fun handleIngest(
        state: IslandBehaviorState,
        event: IslandEngineEvent.IngestNotification,
        nowMonotonic: Long,
        settings: IslandBehaviorSettings
    ): IslandBehaviorDecision {
        val incoming = event.notification

        // Same-source updates are authoritative copy updates, never fresh arrivals.
        if (state.queue.notifications.any { it.id == incoming.id }) {
            return handleUpdate(state, IslandEngineEvent.UpdateNotification(incoming), nowMonotonic, settings)
        }
        // 1. Classification check
        val classification = NotificationEventPolicy.evaluate(
            incoming,
            restored = event.isRestored,
            recognizedActivity = false
        )
        if (classification.kind == NotificationEventKind.IGNORED) {
            return IslandBehaviorDecision(
                state = state,
                action = IslandDomainAction.SuppressMinorUpdate,
                reason = "ignored_by_event_policy:${classification.reason}"
            )
        }

        // 2. Check tombstone / suppression (UX-C08, UX-C09)
        val existingTombstone = state.tombstones[incoming.id]
        var isMilestoneResurfacing = event.isMilestone
        val updatedTombstones = state.tombstones.toMutableMap()

        if (existingTombstone != null) {
            when (val eval = IslandSuppressionTombstoneRegistry.evaluateUpdate(incoming, existingTombstone)) {
                is MilestoneEvaluation.SuppressedMinorUpdate -> {
                    return IslandBehaviorDecision(
                        state = state,
                        action = IslandDomainAction.SuppressMinorUpdate,
                        reason = "tombstone_suppressed_minor_update"
                    )
                }
                is MilestoneEvaluation.ResurfaceMilestone -> {
                    isMilestoneResurfacing = true
                    updatedTombstones[incoming.id] = IslandSuppressionTombstoneRegistry.updateTombstoneAfterResurface(
                        existingTombstone, incoming, nowMonotonic
                    )
                }
                is MilestoneEvaluation.NotSuppressed -> {
                    updatedTombstones.remove(incoming.id)
                }
            }
        }

        if (state.ownership == IslandInteractionOwnership.INTERRUPTED) {
            val queue = reducer.reduce(state.queue, NotificationEvent.Posted(incoming, isSilentUpdate = true))
            return IslandBehaviorDecision(state.copy(queue = queue), IslandDomainAction.None, reason = "arrival_deferred_during_call")
        }
        // 3. User interaction protection: User-expanded pager, reply active, or child interaction (UX-C04 / PD-004)
        if (state.ownership == IslandInteractionOwnership.USER_EXPANDED ||
            state.ownership == IslandInteractionOwnership.PAGER_ACTIVE ||
            state.ownership == IslandInteractionOwnership.REPLY_ACTIVE ||
            state.ownership == IslandInteractionOwnership.CHILD_INTERACTION
        ) {
            // Ingest silently to queue without changing selectedKey
            val updatedQueue = reducer.reduce(
                state.queue,
                NotificationEvent.Posted(incoming, isSilentUpdate = true)
            )

            // Spawn child satellite mini-notice (UX-C11 / UX-C14)
            val childToken = state.nextChildToken
            val newChild = ChildNotice(
                key = incoming.id,
                packageName = incoming.packageName,
                appName = incoming.appName,
                title = incoming.title,
                text = incoming.text,
                timestamp = incoming.timestamp,
                expiresAtMonotonic = nowMonotonic + settings.childDurationMs,
                isMilestone = isMilestoneResurfacing,
                token = childToken
            )
            val candidateChildren = (listOf(newChild) + state.childNotices.filterNot { it.key == incoming.id })
            val updatedChildren = candidateChildren.filter { child -> updatedQueue.notifications.any { it.id == child.key } }.take(settings.maxChildNotices.coerceIn(0, 50))
            val evicted = candidateChildren.filterNot { it in updatedChildren }
            val cancelEffects = evicted.map { IslandSideEffect.CancelChildTimer(it.key) }

            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    childNotices = updatedChildren,
                    tombstones = updatedTombstones,
                    nextChildToken = childToken + 1
                ),
                action = IslandDomainAction.UpsertChildNotice,
                effects = listOf(IslandSideEffect.StartChildTimer(incoming.id, childToken, settings.childDurationMs)) + cancelEffects,
                reason = "user_expanded_active_pin_selection_spawn_child"
            )
        }

        // 4. Transient preview already reading: spawn child mini-island (A06)
        if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            val updatedQueue = reducer.reduce(
                state.queue,
                NotificationEvent.Posted(incoming, isSilentUpdate = true)
            )
            val childToken = state.nextChildToken
            val newChild = ChildNotice(
                key = incoming.id,
                packageName = incoming.packageName,
                appName = incoming.appName,
                title = incoming.title,
                text = incoming.text,
                timestamp = incoming.timestamp,
                expiresAtMonotonic = nowMonotonic + settings.childDurationMs,
                isMilestone = isMilestoneResurfacing,
                token = childToken
            )
            val candidateChildren = (listOf(newChild) + state.childNotices.filterNot { it.key == incoming.id })
            val updatedChildren = candidateChildren.filter { child -> updatedQueue.notifications.any { it.id == child.key } }.take(settings.maxChildNotices.coerceIn(0, 50))
            val evicted = candidateChildren.filterNot { it in updatedChildren }
            val cancelEffects = evicted.map { IslandSideEffect.CancelChildTimer(it.key) }

            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    childNotices = updatedChildren,
                    tombstones = updatedTombstones,
                    nextChildToken = childToken + 1
                ),
                action = IslandDomainAction.UpsertChildNotice,
                effects = listOf(IslandSideEffect.StartChildTimer(incoming.id, childToken, settings.childDurationMs)) + cancelEffects,
                reason = "transient_preview_active_spawn_child"
            )
        }

        // 5. Default intake from IDLE or COMPACT
        val updatedQueue = reducer.reduce(
            state.queue,
            NotificationEvent.Posted(incoming, isSilentUpdate = false)
        )

        // Stage 0A: Respect notification preview eligibility
        if (!event.autoExpand) {
            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    selectedKey = incoming.id,
                    ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                    targetPresentation = IslandPresentationTarget.COMPACT,
                    tombstones = updatedTombstones
                ),
                action = IslandDomainAction.CollapseToCompact,
                effects = emptyList(),
                reason = "ingest_auto_expand_disabled_stay_compact"
            )
        }

        val nextToken = state.activeToken + 1
        val duration = if (isMilestoneResurfacing) settings.milestoneDurationMs else settings.previewDurationMs

        val nextPresentation = IslandPresentationTarget.TRANSIENT_PREVIEW
        val nextOwnership = IslandInteractionOwnership.TRANSIENT_PREVIEW
        val action = if (isMilestoneResurfacing) IslandDomainAction.ResurfaceMilestone else IslandDomainAction.ExpandNotification

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                selectedKey = incoming.id,
                ownership = nextOwnership,
                targetPresentation = nextPresentation,
                activeToken = nextToken,
                tombstones = updatedTombstones
            ),
            action = action,
            effects = listOf(IslandSideEffect.StartPreviewTimer(nextToken, duration)),
            reason = if (isMilestoneResurfacing) "milestone_resurfaced_transient" else "new_notification_auto_preview"
        )
    }

    private fun handleUpdate(
        state: IslandBehaviorState,
        event: IslandEngineEvent.UpdateNotification,
        nowMonotonic: Long,
        settings: IslandBehaviorSettings
    ): IslandBehaviorDecision {
        val incoming = event.notification
        val existingIndex = state.queue.notifications.indexOfFirst { it.id == incoming.id }

        if (existingIndex == -1) {
            // Not in queue: treat as fresh ingest
            return handleIngest(state, IslandEngineEvent.IngestNotification(incoming), nowMonotonic, settings)
        }

        // In-place update (A11): preserve selectedKey and active page
        val updatedQueue = reducer.reduce(
            state.queue,
            NotificationEvent.Posted(incoming, isSilentUpdate = true)
        )

        // Also update child notice in-place if present
        val updatedChildren = state.childNotices.map { child ->
            if (child.key == incoming.id) {
                child.copy(title = incoming.title, text = incoming.text, timestamp = incoming.timestamp)
            } else child
        }

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                childNotices = updatedChildren
            ),
            action = IslandDomainAction.SuppressMinorUpdate,
            reason = "same_key_in_place_update"
        )
    }

    private fun handleRemove(
        state: IslandBehaviorState,
        notificationId: String
    ): IslandBehaviorDecision {
        // Clear tombstone when source removes notification (A28)
        val updatedTombstones = state.tombstones.filterKeys { it != notificationId }

        // Remove from child notices if present (A39: no ghost child)
        val updatedChildren = state.childNotices.filterNot { it.key == notificationId }
        val cancelChildEffect = if (state.childNotices.any { it.key == notificationId }) {
            listOf(IslandSideEffect.CancelChildTimer(notificationId))
        } else emptyList()

        // Update queue
        val updatedQueue = reducer.reduce(state.queue, NotificationEvent.Removed(notificationId))

        // Check selectedKey resolution (UX-C05 / A13 / A14 / A15)
        val wasSelected = state.selectedKey == notificationId
        val cancelTimerEffect = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW && wasSelected) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()
        val removeEffects = cancelChildEffect + cancelTimerEffect

        if (!wasSelected) {
            // Non-selected item removed: selectedKey stays valid, count decrements (A15)
            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    childNotices = updatedChildren,
                    tombstones = updatedTombstones
                ),
                action = IslandDomainAction.KeepSelectedKey,
                effects = cancelChildEffect,
                reason = "non_selected_removed"
            )
        }

        // Selected item was removed
        if (updatedQueue.notifications.isEmpty()) {
            // Final item removed and no active activities -> return directly to Idle (UX-C07 / A16 / A20)
            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    selectedKey = null,
                    ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                    targetPresentation = IslandPresentationTarget.IDLE,
                    childNotices = emptyList(),
                    tombstones = updatedTombstones
                ),
                action = IslandDomainAction.CollapseToCompact,
                effects = removeEffects,
                reason = "final_item_removed_return_idle"
            )
        }

        // Surviving neighbor selected: next-else-previous (UX-C05 / A13 / A14)
        val nextKey = updatedQueue.activeNotificationId
        val nextPresentation = if (state.ownership == IslandInteractionOwnership.PAGER_ACTIVE) {
            IslandPresentationTarget.PAGER
        } else {
            IslandPresentationTarget.USER_EXPANDED
        }

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                selectedKey = nextKey,
                targetPresentation = nextPresentation,
                childNotices = updatedChildren,
                tombstones = updatedTombstones
            ),
            action = IslandDomainAction.SelectNextOrPrevious,
            effects = removeEffects,
            reason = "selected_item_removed_select_neighbor"
        )
    }

    private fun handleReconcile(
        state: IslandBehaviorState,
        activeKeys: Set<String>
    ): IslandBehaviorDecision {
        var currentQueue = state.queue
        val keysToRemove = state.queue.notifications.map { it.id }.filterNot { it in activeKeys }

        keysToRemove.forEach { key ->
            currentQueue = reducer.reduce(currentQueue, NotificationEvent.Removed(key))
        }

        val updatedChildren = state.childNotices.filter { it.key in activeKeys }
        val cancelChildEffects = state.childNotices.filterNot { it.key in activeKeys }
            .map { IslandSideEffect.CancelChildTimer(it.key) }
        val updatedTombstones = state.tombstones.filterKeys { it in activeKeys }

        val wasSelected = state.selectedKey != null && state.selectedKey !in activeKeys
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW && wasSelected) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        val newSelected = if (state.selectedKey in activeKeys) {
            state.selectedKey
        } else {
            currentQueue.activeNotificationId
        }

        val targetPresentation = if (currentQueue.notifications.isEmpty()) {
            IslandPresentationTarget.IDLE
        } else {
            state.targetPresentation
        }

        return IslandBehaviorDecision(
            state = state.copy(
                queue = currentQueue,
                selectedKey = newSelected,
                targetPresentation = targetPresentation,
                childNotices = updatedChildren,
                tombstones = updatedTombstones
            ),
            action = IslandDomainAction.KeepSelectedKey,
            effects = cancelChildEffects + cancelTimerEffects,
            reason = "reconcile_active_sources"
        )
    }

    private fun handleTapCompact(state: IslandBehaviorState): IslandBehaviorDecision {
        val count = state.queue.notifications.size
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return when {
            // UX-C01 / A01: 0 notifications + idle -> Open Quick Controls
            count == 0 -> {
                IslandBehaviorDecision(
                    state = state.copy(
                        ownership = IslandInteractionOwnership.USER_EXPANDED,
                        targetPresentation = IslandPresentationTarget.QUICK_CONTROLS
                    ),
                    action = IslandDomainAction.OpenQuickControls,
                    effects = cancelTimerEffects,
                    reason = "idle_tap_opens_quick_controls"
                )
            }
            // UX-C02 / A02: Exactly 1 notification -> Expand single card
            count == 1 -> {
                val targetKey = state.queue.notifications.first().id
                IslandBehaviorDecision(
                    state = state.copy(
                        selectedKey = targetKey,
                        queue = state.queue.copy(activeNotificationId = targetKey),
                        ownership = IslandInteractionOwnership.USER_EXPANDED,
                        targetPresentation = IslandPresentationTarget.USER_EXPANDED
                    ),
                    action = IslandDomainAction.ExpandNotification,
                    effects = cancelTimerEffects,
                    reason = "single_notification_expand"
                )
            }
            // UX-C03 / A03: >= 2 notifications -> Open horizontal pager, newest first
            else -> {
                val newestNotification = state.queue.notifications.maxWithOrNull(
                    compareBy<NotificationInfo> { it.timestamp }.thenBy { it.id }
                ) ?: state.queue.notifications.first()
                val newestKey = newestNotification.id
                IslandBehaviorDecision(
                    state = state.copy(
                        selectedKey = newestKey,
                        queue = state.queue.copy(activeNotificationId = newestKey),
                        ownership = IslandInteractionOwnership.PAGER_ACTIVE,
                        targetPresentation = IslandPresentationTarget.PAGER
                    ),
                    action = IslandDomainAction.OpenPager,
                    effects = cancelTimerEffects,
                    reason = "multi_notification_open_pager"
                )
            }
        }
    }

    private fun handleTapExpandedBody(state: IslandBehaviorState): IslandBehaviorDecision {
        val activeKey = state.selectedKey ?: state.queue.activeNotificationId
        return if (activeKey != null) {
            IslandBehaviorDecision(
                state = state,
                action = IslandDomainAction.None,
                effects = listOf(IslandSideEffect.LaunchContentIntent(activeKey)),
                reason = "expanded_body_tap_launch_app"
            )
        } else {
            IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "no_active_key")
        }
    }

    private fun handleTapChild(
        state: IslandBehaviorState,
        childKey: String,
        expectedArrivalToken: Long?
    ): IslandBehaviorDecision {
        if (expectedArrivalToken != null && state.childNotices.none { it.key == childKey && it.arrivalToken == expectedArrivalToken }) return IslandBehaviorDecision(state, IslandDomainAction.None, reason = "stale_child_identity")
        if (state.childNotices.none { it.key == childKey } || state.queue.notifications.none { it.id == childKey }) {
            return IslandBehaviorDecision(state, IslandDomainAction.None, reason = "stale_child_promotion_ignored")
        }
        // UX-C12 / A34: Tap child promotes it to main expanded, old main stays in queue/pager
        val updatedChildren = state.childNotices.filterNot { it.key == childKey }

        return IslandBehaviorDecision(
            state = state.copy(
                selectedKey = childKey,
                queue = reducer.reduce(state.queue, NotificationEvent.Selected(childKey)),
                childNotices = updatedChildren,
                ownership = IslandInteractionOwnership.USER_EXPANDED,
                targetPresentation = IslandPresentationTarget.USER_EXPANDED
            ),
            action = IslandDomainAction.ExpandNotification,
            effects = listOf(IslandSideEffect.CancelChildTimer(childKey)),
            reason = "child_promoted_to_main"
        )
    }

    private fun handleCloseButton(
        state: IslandBehaviorState,
        explicitKey: String?,
        nowMonotonic: Long,
        settings: IslandBehaviorSettings
    ): IslandBehaviorDecision {
        val keyToDismiss = explicitKey ?: state.selectedKey ?: state.queue.activeNotificationId
            ?: return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "no_item_to_dismiss")

        val targetItem = state.queue.notifications.firstOrNull { it.id == keyToDismiss }

        // Evaluate whether system dismissal is enabled and permitted (A17 vs A18 vs A19)
        val canSystemDismiss = settings.defaultOsDismiss && (targetItem?.isClearable == true)

        val cancelChildEffects = if (state.childNotices.any { it.key == keyToDismiss }) {
            listOf(IslandSideEffect.CancelChildTimer(keyToDismiss))
        } else emptyList()

        val effects = (if (canSystemDismiss) {
            listOf(IslandSideEffect.CancelAndroidNotification(keyToDismiss))
        } else {
            emptyList()
        }) + cancelChildEffects

        val action = if (canSystemDismiss) {
            IslandDomainAction.RequestSystemDismiss
        } else {
            IslandDomainAction.DismissIslandOnly
        }

        // Create tombstone for Island-only dismissal (UX-C08, UX-C09, A21)
        val updatedTombstones = state.tombstones.toMutableMap()
        if (targetItem != null) {
            updatedTombstones[keyToDismiss] = IslandSuppressionTombstoneRegistry.createTombstone(targetItem, nowMonotonic)
        }

        // Remove from queue
        val updatedQueue = reducer.reduce(state.queue, NotificationEvent.Removed(keyToDismiss))

        if (updatedQueue.notifications.isEmpty()) {
            // Final item removed -> return to Idle compact directly (UX-C07 / A20)
            return IslandBehaviorDecision(
                state = state.copy(
                    queue = updatedQueue,
                    selectedKey = null,
                    ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                    targetPresentation = IslandPresentationTarget.IDLE,
                    childNotices = emptyList(),
                    tombstones = updatedTombstones
                ),
                action = action,
                effects = effects,
                reason = "dismiss_final_item_return_idle"
            )
        }

        // Surviving item selected
        val nextKey = updatedQueue.activeNotificationId
        val nextPresentation = if (state.ownership == IslandInteractionOwnership.PAGER_ACTIVE) {
            IslandPresentationTarget.PAGER
        } else {
            IslandPresentationTarget.USER_EXPANDED
        }

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                selectedKey = nextKey,
                targetPresentation = nextPresentation,
                tombstones = updatedTombstones
            ),
            action = action,
            effects = effects,
            reason = if (canSystemDismiss) "dismissed_with_os_cancel" else "dismissed_island_only"
        )
    }

    private fun handleSwipeUp(state: IslandBehaviorState): IslandBehaviorDecision {
        // U01 / PD-010: Swipe up collapses card to compact, queue and OS shade UNCHANGED
        val cancelChildEffects = state.childNotices.map { IslandSideEffect.CancelChildTimer(it.key) }
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                targetPresentation = if (state.queue.notifications.isEmpty()) IslandPresentationTarget.IDLE else IslandPresentationTarget.COMPACT,
                childNotices = emptyList() // Children merge visually on collapse (A07)
            ),
            action = IslandDomainAction.CollapseToCompact,
            effects = cancelChildEffects + cancelTimerEffects,
            reason = "swipe_up_collapse_preserve_queue"
        )
    }

    private fun handleOutsideTap(state: IslandBehaviorState): IslandBehaviorDecision {
        // Outside tap collapses card, preserves queue
        val cancelChildEffects = state.childNotices.map { IslandSideEffect.CancelChildTimer(it.key) }
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                targetPresentation = if (state.queue.notifications.isEmpty()) IslandPresentationTarget.IDLE else IslandPresentationTarget.COMPACT,
                childNotices = emptyList()
            ),
            action = IslandDomainAction.CollapseToCompact,
            effects = cancelChildEffects + cancelTimerEffects,
            reason = "outside_tap_collapse_preserve_queue"
        )
    }

    private fun handlePreviewTimeout(
        state: IslandBehaviorState,
        token: Long
    ): IslandBehaviorDecision {
        // Validate correlation token: ignore stale timer from prior event
        if (token != state.activeToken || state.ownership != IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "stale_preview_token_ignored")
        }

        val cancelChildEffects = state.childNotices.map { IslandSideEffect.CancelChildTimer(it.key) }

        // A05 / A07: Preview timeout completes: collapse to compact, merge children visually, keep queue!
        return IslandBehaviorDecision(
            state = state.copy(
                ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                targetPresentation = IslandPresentationTarget.COMPACT,
                childNotices = emptyList() // A07: children visually merge into main
            ),
            action = IslandDomainAction.CollapseToCompact,
            effects = cancelChildEffects,
            reason = "preview_timeout_collapse_merge_children"
        )
    }

    private fun handleChildTimeout(
        state: IslandBehaviorState,
        childKey: String,
        token: Long
    ): IslandBehaviorDecision {
        // A35 / UX-C15: Child expires untouched: visually merges into main, queue unchanged
        val child = state.childNotices.firstOrNull { it.key == childKey }
            ?: return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "child_already_removed")
        if (child.isPaused) {
            return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "child_timeout_ignored_paused")
        }
        if (child.token != token) {
            return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "stale_child_token_ignored")
        }
        val updatedChildren = state.childNotices.filterNot { it.key == childKey }

        return IslandBehaviorDecision(
            state = state.copy(childNotices = updatedChildren),
            action = IslandDomainAction.None,
            reason = "child_expired_visual_merge"
        )
    }

    private fun handleTouchChild(
        state: IslandBehaviorState,
        childKey: String,
        nowMonotonic: Long
    ): IslandBehaviorDecision {
        val child = state.childNotices.firstOrNull { it.key == childKey }
            ?: return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "child_not_found")
        if (child.isPaused) return IslandBehaviorDecision(state, IslandDomainAction.None, reason = "child_already_held")
        val owned = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) IslandInteractionOwnership.USER_EXPANDED else state.ownership
        val prev = if (state.ownership != IslandInteractionOwnership.CHILD_INTERACTION) owned else state.previousOwnership
        val updatedChildren = state.childNotices.map {
            if (it.key == childKey) it.copy(isPaused = true, remainingMs = (it.expiresAtMonotonic - nowMonotonic).coerceAtLeast(1L)) else it
        }
        return IslandBehaviorDecision(
            state = state.copy(
                childNotices = updatedChildren,
                ownership = IslandInteractionOwnership.CHILD_INTERACTION,
                previousOwnership = prev,
                targetPresentation = if (state.targetPresentation == IslandPresentationTarget.TRANSIENT_PREVIEW) IslandPresentationTarget.USER_EXPANDED else state.targetPresentation
            ),
            action = IslandDomainAction.None,
            effects = listOf(IslandSideEffect.CancelChildTimer(childKey)) + if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) listOf(IslandSideEffect.CancelTimer(state.activeToken)) else emptyList(),
            reason = "child_touch_pause_timer"
        )
    }

    private fun handleReleaseChild(
        state: IslandBehaviorState,
        childKey: String,
        nowMonotonic: Long,
        settings: IslandBehaviorSettings
    ): IslandBehaviorDecision {
        val child = state.childNotices.firstOrNull { it.key == childKey }
            ?: return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "child_not_found")
        if (!child.isPaused) return IslandBehaviorDecision(state, IslandDomainAction.None, reason = "child_not_held")
        val nextToken = state.nextChildToken
        val updatedChildren = state.childNotices.map {
            if (it.key == childKey) it.copy(
                isPaused = false,
                token = nextToken,
                expiresAtMonotonic = nowMonotonic + child.remainingMs.coerceAtLeast(1L) + settings.childReleaseGraceMs
            ) else it
        }
        val targetOwnership = if (updatedChildren.any { it.isPaused }) { IslandInteractionOwnership.CHILD_INTERACTION } else if (state.previousOwnership != IslandInteractionOwnership.CHILD_INTERACTION) {
            state.previousOwnership
        } else if (state.queue.notifications.size > 1) {
            IslandInteractionOwnership.PAGER_ACTIVE
        } else {
            IslandInteractionOwnership.SYSTEM_IDLE
        }
        return IslandBehaviorDecision(
            state = state.copy(
                childNotices = updatedChildren,
                ownership = targetOwnership,
                nextChildToken = nextToken + 1
            ),
            action = IslandDomainAction.None,
            effects = listOf(IslandSideEffect.StartChildTimer(childKey, nextToken, child.remainingMs.coerceAtLeast(1L) + settings.childReleaseGraceMs)),
            reason = "child_release_reset_timer"
        )
    }

    private fun handleSelectPage(
        state: IslandBehaviorState,
        notificationId: String
    ): IslandBehaviorDecision {
        if (state.queue.notifications.none { it.id == notificationId }) {
            return IslandBehaviorDecision(state = state, action = IslandDomainAction.None, reason = "invalid_page_key")
        }
        val updatedQueue = reducer.reduce(state.queue, NotificationEvent.Selected(notificationId))
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                childNotices = state.childNotices.filterNot { it.key == notificationId },
                selectedKey = notificationId,
                ownership = IslandInteractionOwnership.PAGER_ACTIVE,
                targetPresentation = IslandPresentationTarget.PAGER
            ),
            action = IslandDomainAction.KeepSelectedKey,
            effects = cancelTimerEffects,
            reason = "page_selected"
        )
    }

    private fun handleNextPage(state: IslandBehaviorState): IslandBehaviorDecision {
        val updatedQueue = reducer.reduce(state.queue, NotificationEvent.Next)
        val newKey = updatedQueue.activeNotificationId
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                selectedKey = newKey,
                ownership = IslandInteractionOwnership.PAGER_ACTIVE,
                targetPresentation = IslandPresentationTarget.PAGER
            ),
            action = IslandDomainAction.KeepSelectedKey,
            effects = cancelTimerEffects,
            reason = "next_page"
        )
    }

    private fun handlePreviousPage(state: IslandBehaviorState): IslandBehaviorDecision {
        val updatedQueue = reducer.reduce(state.queue, NotificationEvent.Previous)
        val newKey = updatedQueue.activeNotificationId
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                queue = updatedQueue,
                selectedKey = newKey,
                ownership = IslandInteractionOwnership.PAGER_ACTIVE,
                targetPresentation = IslandPresentationTarget.PAGER
            ),
            action = IslandDomainAction.KeepSelectedKey,
            effects = cancelTimerEffects,
            reason = "previous_page"
        )
    }

    private fun handleIncomingCall(state: IslandBehaviorState): IslandBehaviorDecision {
        if (state.ownership == IslandInteractionOwnership.INTERRUPTED) return IslandBehaviorDecision(state, IslandDomainAction.None, reason = "call_already_owns_surface")
        // C01: Incoming call preempts ordinary preview, preserves suspended state
        val cancelChildEffects = state.childNotices.map { IslandSideEffect.CancelChildTimer(it.key) }
        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW) {
            listOf(IslandSideEffect.CancelTimer(state.activeToken))
        } else emptyList()

        return IslandBehaviorDecision(
            state = state.copy(
                suspendedBeforeCall = state,
                ownership = IslandInteractionOwnership.INTERRUPTED,
                targetPresentation = IslandPresentationTarget.USER_EXPANDED,
                childNotices = emptyList()
            ),
            action = IslandDomainAction.ExpandNotification,
            effects = cancelChildEffects + cancelTimerEffects,
            reason = "incoming_call_preemption"
        )
    }

    private fun handleCallEnded(state: IslandBehaviorState): IslandBehaviorDecision {
        val restore = state.suspendedBeforeCall

        return if (restore != null && state.queue.notifications.isNotEmpty()) {
            IslandBehaviorDecision(
                state = restore.copy(
                    suspendedBeforeCall = null,
                    queue = state.queue,
                    childPrivacy = state.childPrivacy,
                    selectedKey = restore.selectedKey?.takeIf { key -> state.queue.notifications.any { it.id == key } } ?: state.queue.activeNotificationId,
                    childNotices = emptyList(),
                    ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                    targetPresentation = IslandPresentationTarget.COMPACT
                ),
                action = IslandDomainAction.CollapseToCompact,
                reason = "call_ended_restore_suspended_state"
            )
        } else {
            IslandBehaviorDecision(
                state = state.copy(
                    suspendedBeforeCall = null,
                    ownership = IslandInteractionOwnership.SYSTEM_IDLE,
                    targetPresentation = if (state.queue.notifications.isEmpty()) IslandPresentationTarget.IDLE else IslandPresentationTarget.COMPACT
                ),
                action = IslandDomainAction.CollapseToCompact,
                reason = "call_ended_return_compact"
            )
        }
    }

    private fun handleStartReply(state: IslandBehaviorState): IslandBehaviorDecision {
        return IslandBehaviorDecision(
            state = state.copy(
                ownership = IslandInteractionOwnership.REPLY_ACTIVE,
                targetPresentation = IslandPresentationTarget.REPLY
            ),
            action = IslandDomainAction.None,
            reason = "reply_mode_started"
        )
    }

    private fun handleExitReply(state: IslandBehaviorState): IslandBehaviorDecision {
        return IslandBehaviorDecision(
            state = state.copy(
                ownership = IslandInteractionOwnership.USER_EXPANDED,
                targetPresentation = IslandPresentationTarget.USER_EXPANDED
            ),
            action = IslandDomainAction.None,
            reason = "reply_mode_exited"
        )
    }

    private fun handleDoubleTapCompact(state: IslandBehaviorState): IslandBehaviorDecision {
        // U04: Double-tap on compact pill is safely mapped to a single expand intent
        return handleTapCompact(state)
    }

    private fun handleArtworkUpdated(
        state: IslandBehaviorState,
        event: IslandEngineEvent.ArtworkUpdated
    ): IslandBehaviorDecision {
        val updatedQueue = reducer.reduce(
            state.queue,
            NotificationEvent.ArtworkUpdated(event.notificationId, event.imagePath, event.picturePath)
        )
        return IslandBehaviorDecision(
            state = state.copy(queue = updatedQueue),
            action = IslandDomainAction.None,
            reason = "artwork_updated_in_place"
        )
    }
}
