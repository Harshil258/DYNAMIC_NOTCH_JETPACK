# Task 03 — Baseline Audit & Behavioral Engine Integration Plan

**Document:** `reports/TASK_03_BASELINE_AND_INTEGRATION_PLAN.md`  
**Author:** Android Development Team  
**Reviewer & Final Examiner:** ChatGPT — Principal Product Architect  
**Status:** Pre-Implementation Baseline & Architectural Integration Plan  
**Target Spec Authority:** `productdecesions/DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md`  
**Acceptance Test Authority:** `productdecesions/DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md`

---

## 1. Source Baseline & Repository Inventory

### A. Git Repository State
- **Branch:** `main` (tracking `origin/main`, ahead by 2 local commits).
- **Gradle Task Baseline:** `:app:testDebugUnitTest` executes cleanly.
  - **Test Classes:** 76 JUnit test classes.
  - **Total Tests:** 408 unit tests.
  - **Failures / Errors:** 0.
  - **Skipped:** 0.
- **Build Variants:**
  - `debug` (Includes `app/src/debug/` with `DynamicIslandPlayground`, `DynamicIslandPlaygroundActivity`, `PlaygroundController`).
  - `release` (Strictly excludes debug playground activities via manifest verification).

---

## 2. Codebase Mapping: Production Data & Presentation Pipeline

```
[ Android Framework ]
StatusBarNotification ──► IslandNotificationListener.onNotificationPosted()
                                    │
                                    ▼
[ Ingestion & Normalization ]
NotificationContentResolver.resolveNotificationContent() ──► NotificationInfo
                                    │
                                    ▼
[ Existing State Reducer ]
NotificationReducer.reduce(NotificationQueueState, NotificationEvent)
                                    │
                                    ▼
[ State Coordinator ]
IslandStateManager ◄──► IslandInteractionPolicy (Touch Routing)
        │
        ├──────────────────────────┬─────────────────────────┐
        ▼                          ▼                         ▼
DynamicIslandPill           Overlay Window            PlaygroundController
(Shared Compose UI)     (IslandOverlayService)      (Debug Workbench Seam)
```

### Exact Component Inventory:
1. **`NotificationReducer.kt`** (`ai.emots.kishan_dynamic.data.notification.NotificationReducer`):
   - Pure, immutable Kotlin reducer managing `NotificationQueueState(notifications: List<NotificationInfo>, activeNotificationId: String?)`.
   - Event types: `Posted`, `Removed`, `Expired`, `Selected`, `DismissActive`, `Next`, `Previous`, `ArtworkUpdated`, `Cleared`.
   - Uses `NotificationInfo.id` (`StatusBarNotification.key`) as canonical identity.
2. **`IslandStateManager.kt`** (`ai.emots.kishan_dynamic.service.IslandStateManager`):
   - Thread-safe singleton coordinator managing `_currentState: MutableStateFlow<IslandState>` and background jobs (dismiss timers, call duration, media timers).
   - Ingests via `postNotification()`, `removeNotification()`, `reconcileNotificationSources()`, `handleSwipeDismiss()`, `dismissActiveNotification()`.
3. **`IslandInteractionPolicy.kt`** (`ai.emots.kishan_dynamic.data.model.IslandInteractionPolicy`):
   - Maps physical user gestures (`mainTap`, `companionTap`, `mainLongPress`, `companionLongPress`) to `IslandInteractionAction` (`NONE`, `OPEN_QUICK_CONTROLS`, `OPEN_NOTIFICATIONS`, `OPEN_NEXT_NOTIFICATION`, `OPEN_MUSIC`, `TOGGLE_EXPANSION`).
4. **`IslandOverlayService.kt`** (`ai.emots.kishan_dynamic.service.IslandOverlayService`):
   - Android `AccessibilityService` that creates the physical overlay window via `WindowManager.addView()` and hosts `ComposeView` with `DynamicIslandPill`.
   - Executes external platform side effects (`PendingIntent.send()`, `cancelNotification()`, global actions).
5. **`PlaygroundController.kt`** (`ai.emots.kishan_dynamic.playground.PlaygroundController`):
   - Debug-only controller that binds mock scenarios to `DynamicIslandPill` via `IslandInteractionPolicy`.

---

## 3. Current Codebase Violations of Owner UX Rules

A rigorous source audit identifies six foundational discrepancies between the existing code and the approved `DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md`:

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   DISCREPANCY AUDIT: CODE vs. OWNER SPEC                                         │
├─────────┬──────────────────────┬───────────────────────────────┬───────────────────────────────┬─────────────────┤
│ Rule ID │ Behavior Focus       │ Current Code Behavior         │ Owner-Approved Spec Target    │ Source Location │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C01  │ Idle Single Tap      │ Returns `NONE` (No action)    │ Opens Quick Controls          │ IslandInteract- │
│         │                      │                               │                               │ ionPolicy.kt:16 │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C03  │ Compact >=2 Notifs   │ Expands generic notification  │ Expands Horizontal Pager with │ IslandInteract- │
│         │                      │ list; no pager cursor tracking│ newest notification first     │ ionPolicy.kt:20 │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C04  │ User Pager Open +    │ Can shift active item or drop │ Pins selected key; updates    │ IslandState-    │
│         │ Incoming Arrival     │ preview if expandedByUser=true│ count and queues child notice │ Manager.kt:453  │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C06  │ User Taps Close (x)  │ Calls cancelNotification() on │ Default remove from Island    │ IslandOverlay-  │
│         │                      │ Android system shade!         │ queue ONLY; keep shade intact │ Service.kt:796  │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C08/ │ Suppression & Major  │ Replay guard forgets key on   │ Suppress minor updates; only  │ IslandState-    │
│ UX-C09  │ Delivery Milestones  │ remove; minor reposts re-alert│ major milestones resurrect    │ Manager.kt:494  │
├─────────┼──────────────────────┼───────────────────────────────┼───────────────────────────────┼─────────────────┤
│ UX-C11/ │ Emerging Child       │ Replaces or updates in-place; │ Layered satellite mini-cards  │ Missing Policy  │
│ UX-C14  │ Satellite Cards      │ no child mini-island modeling │ modeled in domain decisions   │ Representation  │
└─────────┴──────────────────────┴───────────────────────────────┴───────────────────────────────┴─────────────────┤
```

### Detailed Evidence:
1. **Violation 1 (UX-C01):** `IslandInteractionPolicy.kt:16` explicitly specifies:
   ```kotlin
   fun mainTap(state: IslandState): IslandInteractionAction = when (state) {
       is IslandState.Minimal -> IslandInteractionAction.NONE // VIOLATION of UX-C01
   ```
   *Required Fix:* Must return `IslandInteractionAction.OPEN_QUICK_CONTROLS` for `IslandState.Minimal` (when no activities or notifications are queued).
2. **Violation 2 (UX-C06):** In `IslandOverlayService.kt:796`:
   ```kotlin
   onNotificationDismiss = { notificationId ->
       if (notification?.isClearable == true) {
           IslandStateManager.removeNotification(notificationId)
           NotificationActionRegistry.remove(notificationId)
           IslandNotificationListener.instance?.cancelNotification(notificationId) // VIOLATION of UX-C06
       }
   }
   ```
   *Required Fix:* Default dismissal must evict the item from the Island queue only, retaining the system notification in the Android shade unless the user has explicitly configured OS dismissal.
3. **Violation 3 (UX-C08/C09):** In `IslandStateManager.kt:494`:
   ```kotlin
   fun removeNotification(notificationId: String) {
       notificationReplayGuard.forget(notificationId) // VIOLATION: Forgets key completely
   ```
   *Required Fix:* An Island-dismissed notification must retain a tombstone/suppression record so subsequent minor updates (ETA $\pm 1\text{m}$, progress $60\% \to 70\%$) do not re-alert the user. Only major delivery milestones (first Nearby alert, Arriving, Delivered) or genuinely new messages may resurrect the notification.

---

## 4. Minimal Safe Migration & Architecture Plan

To satisfy the **Foundation Only** boundary and avoid parallel state machines or UI breakage, we introduce a pure domain layer:

### Architectural Additions:
1. **`IslandBehaviorDecisionEngine.kt`** (Pure Kotlin Policy Adapter):
   - Single source of truth for all domain transitions, gesture resolutions, and queue mutations.
   - Evaluates:
     $$\langle \text{QueueState}, \text{ActiveDomainState}, \text{SelectedKey}, \text{InteractionOwnership}, \text{SuppressionTombstones}, \text{IncomingEvent} \rangle$$
   - Emits pure, serializable decision models:
     `BehaviorDecision(action, updatedQueue, updatedSelection, targetPresentation, effects, reason)`
2. **`IslandSuppressionTombstoneRegistry.kt`**:
   - Manages Island-only dismissals without touching Android `StatusBarNotification`.
   - Distinguishes minor updates from major milestones (Uber/Amazon "Nearby", "Arriving", "Delivered", or new chat message timestamps).
3. **`IslandEventTrace.kt`**:
   - Structured debug trace logging (event type, redacted key, item count, before/after mode, reason branch). Zero PII or message bodies recorded.
4. **Adapter Wiring into `IslandStateManager` and `IslandInteractionPolicy`**:
   - Both production intake and `PlaygroundController` delegate to `IslandBehaviorDecisionEngine`.
   - Side effects (`PendingIntent.send()`, `cancelNotification()`) are isolated behind a strict `IslandEffectExecutor` interface that no-ops during playground simulations.

---

## 5. Scope Boundaries for Task 03

- **In Scope (Task 03):**
  - Canonical pure decision engine implementing UX-C01 through UX-C15 logic.
  - Non-destructive dismissal policy (Island-only by default).
  - Milestone resurrection policy for deliveries and messaging.
  - Stable key selection (preserving `activeNotificationId`).
  - Child mini-island policy modeling (domain representation only).
  - Deterministic JUnit test suite covering all 29 target acceptance scenarios.
- **Out of Scope (Deferred to Future Tasks):**
  - No changes to overlay touch-window topology or `WindowManager` flags.
  - No animated Compose rendering of mini-islands (modeled in policy only).
  - No full Compose `HorizontalPager` UI overhaul.
  - No modifications to design tokens, shaders, or spring physics.

---

**Baseline audit complete. Proceeding to implementation of Step 2 (Canonical Pure Behavior Engine).**
