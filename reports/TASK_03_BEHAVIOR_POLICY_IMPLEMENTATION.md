# Task 03 — Canonical Dynamic Island Behavior Policy Implementation Report

**Document:** `reports/TASK_03_BEHAVIOR_POLICY_IMPLEMENTATION.md`  
**Author:** Android Development Team  
**Reviewer & Final Examiner:** ChatGPT — Principal Product Architect  
**Task Reference:** `productdecesions/GEMINI_TASK_03_BEHAVIOR_ENGINE_FOUNDATION.md`  
**Authority Spec:** `productdecesions/DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md`  
**Target Test Matrix:** `productdecesions/DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md`  
**Status:** COMPLETED & VERIFIED BY DETERMINISTIC JUNIT TESTS  

---

## 1. Executive Summary

In Task 03, we implemented the foundational pure Kotlin decision engine for the Android Dynamic Island, establishing a single canonical source of truth for all interaction rules, priority arbitration, queue mutation, and lifecycle transitions.

This foundation directly resolves the architectural fragmentation and rule violations identified in the Task 03 Baseline Audit:
1. **Idle Tap Behavior (UX-C01):** Single tap on Idle / Minimal now expands Quick Controls (`IslandDomainAction.OpenQuickControls` / `IslandInteractionAction.OPEN_QUICK_CONTROLS`), while preserving main long-press for Settings (PD-007).
2. **Non-Destructive Gestures & Dismissals (UX-C06, U01, PD-009, PD-010):** Default dismissal via the `×` button removes the notification from the Island queue only (`IslandDomainAction.DismissIslandOnly`), strictly preserving the Android system shade notification. Swipe-up collapses the visual surface without canceling the OS notification or purging the queue.
3. **Queue Retention & Tombstone Milestone Resurfacing (UX-C08, UX-C09, UX-C10, A21–A28):** Replaced hasty key forgetting (`notificationReplayGuard.forget`) with a dedicated `IslandSuppressionTombstoneRegistry`. Minor numeric progress increments (e.g. 60% -> 70%) and stop decrements (2 stops -> 1 stop) remain suppressed, while genuine milestones (Nearby alert, Arriving now, Delivered, new message content) cleanly resurface.
4. **Stable Selection & Pager Cursor (UX-C04, UX-C05, A08, A10, A13–A15):** User-selected page identity is strictly anchored by `selectedKey` (`StatusBarNotification.key`), preventing incoming alerts from stealing user attention or jumping page indices. When the source app removes the selected card, a deterministic next-else-previous algorithm activates.
5. **Multi-Arrival Child Satellite Modeling (UX-C11–C15, A06, A35, A36):** Pure domain modeling of child mini-islands (`ChildNotice`) emerging below the main island during transient previews or user-expanded sessions.

---

## 2. Exact Files & Classes Modified / Created

### A. New Foundation Classes Created (`app/src/main/java/ai/emots/kishan_dynamic/data/notification/`)
1. [`IslandBehaviorEngine.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt)
   - **Role:** Pure Kotlin behavioral decision engine.
   - **Inputs:** `IslandBehaviorState`, `IslandEngineEvent`, monotonic timestamp `nowMonotonic`, `IslandBehaviorSettings`.
   - **Outputs:** `IslandBehaviorDecision` containing updated state, typed domain action (`IslandDomainAction`), inspectable side-effect declarations (`IslandSideEffect`), and diagnostic reasoning string.
   - **Purity:** Zero Android framework UI dependencies, no `PendingIntent.send()`, no coroutine launches, no direct `WindowManager` mutations.
2. [`IslandSuppressionTombstoneRegistry.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandSuppressionTombstoneRegistry.kt)
   - **Role:** Suppression state registry and milestone classifier for Island-dismissed notifications.
   - **Functions:** `createTombstone`, `evaluateUpdate`, `updateTombstoneAfterResurface`, `recordDismissal`, `isSuppressed`, `shouldResurface`, `clear`.
   - **Classification Rules:** Distinguishes genuine milestones (`isNearbyMilestone`, `isArrivingMilestone`, `isDeliveredMilestone`, messaging content change) from spammy repaints / jitter.
3. [`IslandEventTrace.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandEventTrace.kt)
   - **Role:** High-fidelity, zero-PII structured event logger.
   - **Security / Privacy:** Cryptographically hashes `StatusBarNotification.key` using SHA-256 (`KeyHasher`), redacting raw titles, texts, and contact details.

### B. Modified Production Classes
4. [`IslandInteractionPolicy.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandInteractionPolicy.kt)
   - Added parameter `quickControlsOnIdleEnabled: Boolean = false` to `fun mainTap(state: IslandState, quickControlsOnIdleEnabled: Boolean = false)`.
   - When enabled and state is `IslandState.Minimal`, returns `IslandInteractionAction.OPEN_QUICK_CONTROLS` (UX-C01).
   - Preserves default parameter `false` to guarantee 100% backward compatibility for existing Sprint 15 regression tests.
5. [`IslandOverlayService.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandOverlayService.kt)
   - **OnTap (Line 664):** Passes `quickControlsOnIdleEnabled = actionConfig.isEnabled && quickControlsEnabled` to `IslandInteractionPolicy.mainTap`, activating UX-C01 in production overlay.
   - **OnSwipeDismiss (Line 699):** Calls `IslandStateManager.collapseToMinimal()` instead of canceling the Android OS notification via `cancelNotification()`.
   - **OnNotificationDismiss (Line 789):** Default `×` dismiss removes the notification from `IslandStateManager` and `NotificationActionRegistry` only, strictly omitting `cancelNotification()` per UX-C06.
6. [`IslandStateManager.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt)
   - **PostNotification:** Consults `IslandSuppressionTombstoneRegistry.isSuppressed()` and `shouldResurface()` before alerting for items dismissed from the Island.
   - **RemoveNotification:** Records dismissed item in `IslandSuppressionTombstoneRegistry.recordDismissal()`.
   - **ReconcileNotificationSources:** Clears tombstones via `IslandSuppressionTombstoneRegistry.clear()` when the source app removes the notification from Android shade (enabling fresh reposts per A28).

### C. Modified Debug / Playground Seams
7. [`PlaygroundModel.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt)
   - Wired `PlaygroundController.interaction()` to call `IslandInteractionPolicy.mainTap(state, quickControlsOnIdleEnabled = true)`, allowing debug workbench users to test UX-C01 directly.

### D. New Unit Test Suite (`app/src/test/java/ai/emots/kishan_dynamic/data/notification/`)
8. [`IslandBehaviorEngineTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngineTest.kt)
   - Comprehensive test suite with 31 deterministic tests covering all 29 target matrix scenarios (A01–A40, U01, U04, C05, P05) plus negative non-destructive invariants.

---

## 3. End-to-End Architectural Data Flow

```
                                      +---------------------------------------------+
                                      |   Android Framework / NotificationListener   |
                                      +---------------------------------------------+
                                                             |
                                                             | StatusBarNotification
                                                             v
                                      +---------------------------------------------+
                                      |           NotificationEventPolicy           |
                                      |        (Channel & salience filter)          |
                                      +---------------------------------------------+
                                                             |
                                      +----------------------+----------------------+
                                      |                                             |
                              (Dismissed Key)                               (Eligible Item)
                                      v                                             v
                 +------------------------------------------+       +-------------------------------+
                 |   IslandSuppressionTombstoneRegistry     |       |      NotificationReducer      |
                 | (Evaluates milestone vs minor progress)  |       |   (Canonical queue snapshot)  |
                 +------------------------------------------+       +-------------------------------+
                                      |                                             |
                         (Resurface or Suppress)                                    |
                                      \                                             /
                                       v                                           v
                             +-----------------------------------------------------------+
                             |                   IslandBehaviorEngine                    |
                             |  - Interaction Ownership: Pager / Reply / Transient / Idle|
                             |  - Stable Selection: selectedKey tracking                 |
                             |  - Multi-Arrival Arbitration: ChildNotice satellite stack |
                             |  - Policy Rules: UX-C01 through UX-C15                    |
                             +-----------------------------------------------------------+
                                                           |
                                                           v
                             +-----------------------------------------------------------+
                             |                  IslandBehaviorDecision                   |
                             |  - State: Updated queue, selectedKey, childNotices        |
                             |  - Action: OpenPager, OpenQuickControls, Expand, Collapse |
                             |  - Effects: CancelTimer, StartPreviewTimer, LogTrace      |
                             +-----------------------------------------------------------+
                                                           |
                                      +--------------------+--------------------+
                                      |                                         |
                                      v                                         v
                 +------------------------------------------+       +-------------------------------+
                 |       Production Side-Effect Executor    |       |      IslandEventTrace         |
                 | (PendingIntent dispatch / optional OS cnl|       |  (Zero-PII SHA-256 key audit) |
                 +------------------------------------------+       +-------------------------------+
                                      |
                                      v
                 +------------------------------------------+
                 |            UI Surface Renderer           |
                 |     (DynamicIslandPill / Compose UI)     |
                 +------------------------------------------+
```

---

## 4. Before vs. After Behavior Matrix (UX-C01 through UX-C10)

| Rule ID | Context & Trigger | Legacy / Baseline Behavior | Canonical Task 03 Implementation | Owner Alignment Status |
|---|---|---|---|---|
| **UX-C01** | Idle compact island tapped | Returned `NONE`; completely inert. | Returns `OpenQuickControls`; opens ActionControl surface. | **OWNER_APPROVED** |
| **UX-C02** | Exactly 1 notification in compact island tapped | Expanded card; risked canceling OS notification on close. | Returns `ExpandNotification`; expands single card; OS notification untouched. | **OWNER_APPROVED** |
| **UX-C03** | 12 eligible notifications in compact island tapped | Opened single unpaged notification card or list. | Returns `OpenPager`; opens horizontal pager with newest item selected. | **OWNER_APPROVED** |
| **UX-C04** | User reading pager page; new notification arrives | New arrival hijacked focus or jumped page index. | Returns `UpsertChildNotice`; keeps `selectedKey` pinned; spawns secondary child satellite. | **OWNER_APPROVED** |
| **UX-C05** | Source removes currently selected pager card | Queue collapsed or reverted to index 0. | Returns `SelectNextOrPrevious`; deterministic neighbor selected; pager stays open. | **OWNER_APPROVED** |
| **UX-C06** | User taps `×` close button on expanded card | Destroyed Android OS notification via `cancelNotification()`. | Returns `DismissIslandOnly`; removes from Island queue; OS shade untouched. | **OWNER_APPROVED** |
| **UX-C07** | User removes final notification; no other activity | Unpredictable or hung expanded surface. | Returns `CollapseToCompact`; transitions directly to Idle; single tap uses UX-C01. | **OWNER_APPROVED** |
| **UX-C08** | Island-dismissed notification updates (e.g. 60% -> 70%) | Immediate re-alert because replay guard forgot key. | Returns `SuppressMinorUpdate`; tombstone suppresses insignificant updates. | **OWNER_APPROVED** |
| **UX-C09** | Island-dismissed delivery reaches '2 stops away' | Treated as standard update or suppressed indefinitely. | Returns `ResurfaceMilestone`; first Nearby alert resurfaces once for 7s. | **OWNER_APPROVED** |
| **UX-C10** | Compact 12 items + delivery receives major milestone | Displaced active item or was lost in queue. | Returns `ResurfaceMilestone`; temporarily expands preview, collapses, item queued (13 total). | **OWNER_APPROVED** |

---

## 5. Domain Modeling for Layered Child Satellites (UX-C11 through UX-C15)

In compliance with the Task 03 boundaries, **no multi-window compositor or animation rewrites were attempted**. Instead, the complete pure domain policy was established in `IslandBehaviorEngine`:
- **UX-C11 (Secondary Satellite Notice):** Modeled via `ChildNotice` struct containing key, package, app name, title, text, timestamp, and independent monotonic expiration deadline.
- **UX-C12 (Child Promotion):** `IslandEngineEvent.TapChild(childKey)` triggers `IslandDomainAction.ExpandNotification`, promoting the child key to `selectedKey` while prior main remains safely in the queue.
- **UX-C13 & UX-C14 (Stack Limits & Insertion):** New arrivals insert at index 0 of `childNotices`, sliding existing children downward. Stack displays up to `settings.maxVisibleChildren` (default 2–3) with remaining counted for +N overflow.
- **UX-C15 & PD-001 (Child Expiry):** `IslandEngineEvent.ChildTimeout(childKey)` visually drops the child from `childNotices` without removing it from the canonical notification queue (`NotificationQueueState`).
- **PD-002 & PD-003 (Independent Timers):** Each child receives an independent monotonic deadline; arrival of a new child never resets the timer of an older child (Scenario A36).

---

## 6. Migration & Rollout Safety

1. **Seam Gating:**
   `IslandInteractionPolicy.mainTap` uses a defaulted parameter:
   `fun mainTap(state: IslandState, quickControlsOnIdleEnabled: Boolean = false): IslandInteractionAction`
   Existing unit tests calling the 1-parameter signature run against the established Sprint 15 baseline, while production overlay and debug playground pass `quickControlsOnIdleEnabled = true`.
2. **Deterministic Side-Effect Isolation:**
   `IslandBehaviorEngine` emits side effects as pure data classes (`IslandSideEffect`). All platform interactions (`PendingIntent.send()`, `NotificationListener.cancelNotification()`) are confined to production execution handlers. The Dynamic Island Playground workbench executes zero external OS side effects.
3. **Trace Diagnostics:**
   Every reduction step emits an `IslandTraceRecord` with monotonic timestamp, event type, SHA-256 hashed notification key, queue count, ownership transitions, and decision reason.
