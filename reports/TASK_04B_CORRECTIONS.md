# TASK 04B — Focused Pager Correctness & Timer Ownership Corrections

**Assigned by:** ChatGPT — Principal Product Architect and Final Examiner  
**Developer:** Gemini  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Base Commit:** `0e51d49288bd77f8fdec0b88924d0518431c6de1`  
**Final Commit SHA:** `8a55f0537ffca769ec77a2a170ba1883425a8af7`  
**Task Verdict Addressed:** Focused REWORK REQUIRED  

---

## 1. Executive Summary

Task 04B resolves the three specific defects identified by the independent examiner in Task 04:
1. **Guaranteed Newest-First Pager Behavior:** Replaced priority-first queue sorting in `NotificationReducer` with strict chronological (newest-first) sorting and deterministic tie-breaking. Opening the compact Dynamic Island (`handleTapCompact`) now selects the newest notification in full compliance with owner-approved `UX-C03`. Critical interruption policies and duration boosts for OTP/financial alerts remain functional via separate policies (`NotificationInterruptionPolicy` and `NotificationPriorityPolicy`).
2. **Single-Authority Preview Expiry:** Disentangled the transient system UI dismiss timer (`dismissTimerJob`) from the engine-authoritative notification preview timer (`previewTimerJob`). `cancelDismissTimer()` no longer cancels `previewTimerJob`, and legacy `scheduleNotificationExpiry()` has been silenced as a no-op. Notification transient previews are now solely initiated by the engine's `StartPreviewTimer` side effect, tracked by monotonic tokens, and collapsed to compact via `PreviewTimeout` callbacks.
3. **Always-Visible Pager Indicators in Multi-Notification State:** Removed the misleading `isCardExpanded` gate on the pager dots inside `NotificationIslandExpandedPager`. The iOS-style sliding dot indicator (up to 7 dots) is now rendered for all multi-notification states (2+ notifications) without requiring any extra user gesture. Dedicated bottom clearance (26dp+) was added across all card templates (Standard unexpanded, Standard expanded, Picture, Navigation, Transport, Progress), and spring animations now gracefully degrade to `snap()` when `LocalReducedMotion.current` is active.

---

## 2. Root Cause Analysis & Precise Code Changes

### Issue 1: Newest-First Pager Ordering (UX-C03)

#### Root Cause
In `NotificationReducer.kt`, `sortQueue()` formerly prioritized `isFinancialOrOtp`, `isPriority`, `importance`, and `isOngoing` ahead of `timestamp`. Consequently, an older high-importance alert (such as an OTP with `time = 1000`) was ordered before a newer chat message (`time = 2000`). When the user tapped compact, `handleTapCompact` chose index 0, opening the pager on the older alert rather than the newest incoming notification.

#### Solution
- Modified `sortQueue()` in [`NotificationReducer.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt#L175-L182) to sort strictly by `timestamp` descending, with deterministic `id` descending tie-breaking:
  ```kotlin
  private fun sortQueue(list: List<NotificationInfo>): List<NotificationInfo> {
      return list.sortedWith(
          compareByDescending<NotificationInfo> { it.timestamp }
              .thenByDescending { it.id }
      )
  }
  ```
- In [`IslandBehaviorEngine.handleTapCompact`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt#L605-L620), explicitly select the newest notification using `maxWithOrNull(compareBy<NotificationInfo> { it.timestamp }.thenBy { it.id })`, ensuring `selectedKey` and `queue.activeNotificationId` both point to the latest message.
- Preserved separate critical interruption logic: `NotificationInterruptionPolicy.isCriticalInterruption` and `NotificationPriorityPolicy.recommendedDisplaySeconds` (minimum 8s display) operate independently of queue order.

---

### Issue 2: Truly Single-Authority Preview Expiry

#### Root Cause
In `IslandStateManager.kt`, `cancelDismissTimer()` previously cancelled both `dismissTimerJob` and `previewTimerJob`. When a new notification arrived with `autoExpand = true`:
1. `IngestNotification` emitted `StartPreviewTimer`, launching `previewTimerJob`.
2. `applyNotificationPresentation(PRESENT)` immediately called `cancelDismissTimer()`, cancelling the freshly started `previewTimerJob`.
3. `applyNotificationPresentation(PRESENT)` called legacy `scheduleNotificationExpiry()`, which saw `previewTimerJob?.isActive == false` and scheduled `dismissTimerJob`.
This resulted in split timer authority, race conditions, and legacy timers silently replacing engine-authoritative timers.

#### Solution
- Separated `cancelDismissTimer()` and `cancelPreviewTimer()` in [`IslandStateManager.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt#L59-L67):
  ```kotlin
  private fun cancelDismissTimer() {
      dismissTimerGeneration++
      dismissTimerJob?.cancel()
      dismissTimerJob = null
  }

  private fun cancelPreviewTimer() {
      previewTimerJob?.cancel()
      previewTimerJob = null
  }
  ```
- Silenced legacy `scheduleNotificationExpiry()` as a complete no-op.
- In `applyNotificationPresentation(PRESENT)`, only `cancelDismissTimer()` is invoked (to dismiss volume/charging/ringer overlays), leaving `previewTimerJob` running uninterrupted.
- In `executeEngineEffects`:
  - `StartPreviewTimer` cancels any prior preview job and launches `previewTimerJob`.
  - On delay completion, dispatches `PreviewTimeout(token)` inside `synchronized(stateLock)`.
  - When `CollapseToCompact` is returned, synchronizes rendered state (`_currentState.value = current.copy(isExpanded = false)`) and restores any interrupted background state if no queued critical alerts remain.
  - `CancelTimer` calls `cancelPreviewTimer()`.
- In `IslandBehaviorEngine.kt`:
  - Added `CancelTimer` emission to `handleRemove` and `handleReconcile` when the item currently in transient preview is removed or revoked.

---

### Issue 3: Pager Indicator Display & Accessibility

#### Root Cause
In `NotificationIsland.kt:1675`, the sliding indicator dots row was guarded by:
```kotlin
if (isCardExpanded && notifications.size > 1 && replyingNotificationId == null)
```
`isCardExpanded` represented deep internal card expansion (body text / full image toggle), NOT whether the island itself was expanded into pager mode. As a result, when the island expanded into horizontal pager mode with multiple notifications, the dots were completely invisible unless the user performed a secondary gesture to toggle internal card expansion. Furthermore, smaller card templates lacked bottom clearance, creating overlap risk.

#### Solution
- Updated the guard in [`NotificationIslandExpandedPager`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt#L1693-L1696) to:
  ```kotlin
  if (notifications.size > 1 && replyingNotificationId == null)
  ```
- Added `LocalReducedMotion` support: dot size (`6.5dp` active, `3.5dp` edge, `5dp` default) and alpha (`1.0f` active, `0.22f` edge, `0.45f` default) animate via `animateDpAsState` and `animateFloatAsState` using bouncy spring specs, but snap instantly with `snap()` when reduced motion is preferred.
- Added dedicated bottom padding across all card templates when `pagerLabel != null`:
  - `NotificationStandardCard` unexpanded: `bottom = AppTheme.island.expandedContentInset + 14.dp`
  - `NotificationStandardCard` expanded: `bottom = 26.dp * scale`
  - `NotificationPictureCard`: `bottom = 26.dp * scale`
  - `NotificationNavigationCard`: `bottom = vPad + 14.dp`
  - `NotificationTransportCard`: `bottom = vPad + 14.dp`
  - `NotificationProgressCard`: `bottom = vPad + 14.dp`
  - All cards report their true height through `onHeightChange` so the outer container accurately sizes with zero overlap.

---

## 3. Preserved Architectures & Scope Integrity

As mandated by Task 04 and Task 04B instructions:
- Preserved existing `HorizontalPager` with `beyondViewportPageCount = 1` and user scroll enabled.
- Preserved stable notification ID key binding (`key = { page -> notifications.getOrNull(page)?.id ?: page.toString() }`).
- Preserved inline reply composable and dismiss buttons with tombstone tracking.
- Preserved 50-notification canonical retention (`NotificationReducer.MAX_NOTIFICATIONS = 50`).
- Preserved all Playground scenarios, options, burst tests, and checks.
- Preserved zero-cricket constraint (Rule 31).
- **Did not implement Task 05 layered Mini-Island UI composables.**

---

## 4. Modified Source Files

| File | Changes |
| :--- | :--- |
| [`NotificationReducer.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt) | Updated `sortQueue()` to newest-first (`timestamp` desc, `id` desc tie-breaking). |
| [`IslandBehaviorEngine.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt) | Updated `handleTapCompact` to select newest item for 2+ items; added `CancelTimer` on remove/reconcile during preview. |
| [`IslandStateManager.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt) | Separated `cancelDismissTimer` from `cancelPreviewTimer`; eliminated `scheduleNotificationExpiry` in favor of single `previewTimerJob`. |
| [`NotificationIsland.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt) | Removed `isCardExpanded` gate from pager dots; added `LocalReducedMotion` spring/snap support; added bottom clearance across all card templates. |
| [`NotificationReducerTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/data/notification/NotificationReducerTest.kt) | Added tests for newest-first ordering and deterministic tie-breaking. |
| [`NotificationEngineeringRegressionTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/data/notification/NotificationEngineeringRegressionTest.kt) | Updated test to reflect UX-C03 newest-first queue active selection. |
| [`NotificationAndMediaRealWorldScenarioTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/service/NotificationAndMediaRealWorldScenarioTest.kt) | Added test for coexistence of older high-priority OTP and newer ordinary message verifying newest first and separate priority policies. |
| [`IslandRuntimeIntegrationTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/testDebug/java/ai/emots/kishan_dynamic/service/IslandRuntimeIntegrationTest.kt) | Added tests 14–20 covering production entry point, explicit collapse, user expansion, call interruption, second notification, stale timers, and OTP coexistence. |
| [`NotificationPagerIndicatorInstrumentedTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/androidTest/java/ai/emots/kishan_dynamic/NotificationPagerIndicatorInstrumentedTest.kt) | Added Compose UI instrumented test verifying multi-card indicator presence and single-card indicator omission. |
