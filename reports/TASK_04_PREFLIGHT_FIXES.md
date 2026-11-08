# Task 04 Report: Foundation Preflight Fixes (Stage 0)

**Author:** Gemini (Senior Android / Jetpack Compose Engineer)  
**Examiner:** ChatGPT (Principal Product Architect & Final Examiner)  
**Task:** Task 04 — Premium Horizontal Notification Pager & Runtime Preflight  
**Status:** COMPLETE & VERIFIED  

---

## 1. Executive Summary

Prior to implementing the Horizontal Notification Pager UI, all mandatory Stage 0 defects identified by the Principal Product Architect were rigorously addressed in the canonical behavior engine (`IslandBehaviorEngine`) and production runtime coordinator (`IslandStateManager`).

The engine and presenter now maintain strict bidirectional agreement regarding preview eligibility, timer lifecycles, and child satellite notice accounting.

---

## 2. Stage 0A — Respect Notification Preview Eligibility

### The Issue
Previously, `IslandBehaviorEngine.handleIngest` unconditionally transitioned intake from `IDLE` or `COMPACT` into `IslandInteractionOwnership.TRANSIENT_PREVIEW` and `IslandPresentationTarget.TRANSIENT_PREVIEW`, and emitted `StartPreviewTimer`. However, notifications posted with `autoExpand = false` were rendered compact by `IslandStateManager`. Consequently:
1. The engine believed the island was in `TRANSIENT_PREVIEW`, while the presenter was compact.
2. Subsequent notifications arriving while compact were mistakenly classified under rule UX-C11 / A06 as secondary child mini-islands, rather than normal compact intake.

### The Fix
1. **Added `autoExpand: Boolean = true` to `IslandEngineEvent.IngestNotification`**:
   [`IslandBehaviorEngine.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt)
   ```kotlin
   data class IngestNotification(
       val notification: NotificationInfo,
       val isRestored: Boolean = false,
       val isMilestone: Boolean = false,
       val autoExpand: Boolean = true
   ) : IslandEngineEvent
   ```

2. **Branching in `handleIngest`**:
   When `!event.autoExpand`:
   ```kotlin
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
   ```
   The engine immediately sets `ownership = SYSTEM_IDLE`, `targetPresentation = COMPACT`, and emits zero timers.

3. **Synchronized Computation in `IslandStateManager.postNotification`**:
   [`IslandStateManager.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt)
   `presentationDecision` and `shouldAutoExpand` are computed *before* event dispatch:
   ```kotlin
   val shouldAutoExpand = autoExpand && existing == null &&
       decision.kind == ai.emots.kishan_dynamic.data.notification.NotificationEventKind.TRANSIENT &&
       presentationDecision == NotificationPresentationDecision.PRESENT
   notificationAutoExpand = shouldAutoExpand

   dispatchBehaviorEvent(
       if (existing != null) {
           ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.UpdateNotification(notification)
       } else {
           ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.IngestNotification(
               notification,
               isMilestone = shouldResurface,
               autoExpand = shouldAutoExpand
           )
       }
   )
   ```

---

## 3. Stage 0B — Unify Preview Expiry Lifecycle

### The Issue
Previously, two competing timers operated concurrently:
- `previewTimerJob` launched by `executeEngineEffects` from `StartPreviewTimer`.
- `dismissTimerJob` launched by `scheduleNotificationExpiry`.

This caused race conditions, differing tokens, and redundant coroutine allocations.

### The Fix
1. **Engine as Single Authority**:
   `StartPreviewTimer(token, durationMs)` is the sole driver of the transient preview lifecycle.
2. **`scheduleNotificationExpiry()` Guarded**:
   ```kotlin
   private fun scheduleNotificationExpiry() {
       if (previewTimerJob?.isActive == true) return
       ...
   }
   ```
   If the engine's preview timer is active, no duplicate timer is launched.
3. **Cancellation Propagation**:
   - `CancelTimer` cleans up `previewTimerJob`.
   - User interactions (`handleTapCompact`, `handleSelectPage`, `handleNextPage`, `handlePreviousPage`, `handleSwipeUp`, `handleOutsideTap`, `handleIncomingCall`) emit `CancelTimer` when interrupting a `TRANSIENT_PREVIEW`, ensuring no dangling timers fire.

---

## 4. Stage 0C — Child Timer Cleanup & Overflow Accounting

### The Issue
1. Child timers were not deterministically canceled upon eviction, promotion, or collapse.
2. `overflowCount` in `IslandBehaviorState` was incorrectly computing `queue.notifications.size - 1 - visibleChildCount`, counting old background queued notifications as child mini-islands.

### The Fix
1. **Added `IslandSideEffect.CancelChildTimer(childKey: String)`**:
   Emitted during:
   - Eviction beyond `maxChildNotices` in `handleIngest`.
   - Child promotion in `handleTapChild`.
   - Notification removal in `handleRemove`.
   - Source reconciliation in `handleReconcile`.
   - User dismissal in `handleCloseButton`.
   - Island collapse in `handleSwipeUp`, `handleOutsideTap`, `handlePreviewTimeout`.
   - Preemption in `handleIncomingCall`.

2. **Clean Job Cancellation in `IslandStateManager.executeEngineEffects`**:
   ```kotlin
   is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.CancelChildTimer -> {
       childTimerJobs[effect.childKey]?.cancel()
       childTimerJobs.remove(effect.childKey)
   }
   ```

3. **Decoupled Queue vs. Mini-Island Overflow**:
   [`IslandBehaviorEngine.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt)
   ```kotlin
   val canonicalQueueCount: Int
       get() = queue.notifications.size

   val pendingChildNoticesCount: Int
       get() = childNotices.size

   fun visibleChildCount(limit: Int = 2): Int =
       minOf(childNotices.size, limit)

   fun childOverflowCount(limit: Int = 2): Int =
       (childNotices.size - limit).coerceAtLeast(0)
   ```
   The `+N` badge on the mini-island stack reflects `childOverflowCount(limit)`, derived strictly from `childNotices.size`, never from queued background items.

---

## 5. Stage 0D — Deterministic Timer Integration Test

### Verification via Virtual Time
[`IslandRuntimeIntegrationTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/testDebug/java/ai/emots/kishan_dynamic/service/IslandRuntimeIntegrationTest.kt) was extended with `test_13_deterministic_previewTimeout_collapsesBothEngineAndPresentationWithoutManualCalls`:
- Configures `IslandStateManager.setScopeForTesting(this)` with a `TestScope`.
- Ingests notification with `autoExpand = true, displaySeconds = 3`.
- Verifies initial state is `isExpanded = true` and `ownership = TRANSIENT_PREVIEW`.
- Advances virtual time via `testScheduler.advanceTimeBy(3500L)` and `testScheduler.runCurrent()`.
- Verifies that **both** engine ownership (`SYSTEM_IDLE`) and visible presentation (`isExpanded = false`) collapse to compact **without calling `collapseExpanded()` or manual fallback dispatchers**.
