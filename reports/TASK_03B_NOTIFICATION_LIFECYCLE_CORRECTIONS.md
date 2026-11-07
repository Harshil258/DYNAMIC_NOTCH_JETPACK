# TASK 03B — Notification Lifecycle Corrections & Milestone Architecture

**Examiner:** ChatGPT — Principal Product Architect and Independent Examiner  
**Developer:** Gemini  
**Scope:** Canonical queue capacity, lifecycle separation, non-destructive collapse, and milestone evaluation  
**Timestamp:** 2026-11-08  

---

## 1. Resolution of the 50-Notification Capacity Mismatch (A04)

### 1.1. Root Cause Analysis
During Sprint 02, `NotificationReducer.kt` introduced a conservative hardcoded bounding constant:
```kotlin
const val MAX_NOTIFICATIONS = 20
```
This was created to prevent unbounded heap memory growth during notification flooding tests before virtualized paging was planned. However, the authoritative Dynamic Island Product Acceptance Matrix explicitly mandates:
> **A04: 50 eligible notifications must be accessible through the pager without duplicates or blank cards.**

When 50 distinct eligible notifications arrived in production, the queue silently dropped 30 notifications, capping at 20 items. This directly violated A04.

### 1.2. The Correction
1. In [`NotificationReducer.kt:20`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt#L20), `MAX_NOTIFICATIONS` was increased from `20` to `50`:
   ```kotlin
   const val MAX_NOTIFICATIONS = 50
   ```
2. The queue retaining logic now retains all distinct notifications up to 50:
   - Evaluated across scale steps: **0, 1, 12, 25, and 50 notifications**.
   - Verified that all 50 items are retained with distinct IDs, preserving the active selection (`activeNotificationId`) without reordering.
   - Bursts exceeding 50 items (e.g., 60 items) are safely capped at exactly 50, protecting system resources while satisfying the product specification.
3. Tests updated and verified:
   - [`NotificationReducerTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/data/notification/NotificationReducerTest.kt): `queueCapPreservesSelectedPageAndOngoingWork` verified at 50 items.
   - [`NotificationEngineeringRegressionTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/data/notification/NotificationEngineeringRegressionTest.kt): `largeOngoingBurstRemainsBoundedAndRetainsSelection` verified at 50 items.
   - [`NotificationAndMediaRealWorldScenarioTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/service/NotificationAndMediaRealWorldScenarioTest.kt): Scaling across 1, 5, 25, and 50 items verified.
   - [`NotificationConcurrencyAndLifecycleStressTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/test/java/ai/emots/kishan_dynamic/service/NotificationConcurrencyAndLifecycleStressTest.kt): Burst processing of 25 items preserves all 25 without dropping; bursts to 60 items bound at 50.

---

## 2. Separation of the 5 Notification Lifecycle Events

The application strictly distinguishes between five distinct notification events to prevent accidental suppression and ensure correct Android OS synchronization:

| Event # | Trigger Source | State Manager Method | Behavior Engine Event | Tombstone Created? | OS Notification Cancelled? |
|:---:|---|---|---|:---:|:---:|
| **1** | Source App Posts / Updates | `postNotification()` | `IngestNotification` / `UpdateNotification` | No (Clears if milestone resurfaces) | No |
| **2** | Source App Removes (Status Bar) | `onSourceNotificationRemoved()` | `RemoveNotification` | **No — CLEARS tombstone (`A28`)** | No |
| **3** | User Clicks `×` on Island | `onUserDismissNotification()` | `TapCloseButton` | **YES (`UX-C06`, `A17`)** | Configurable (Defaults to `false`) |
| **4** | User Dismisses from Android Shade | `onSourceNotificationRemoved()` | `RemoveNotification` | No — Android shade clears it | Already removed by user |
| **5** | Source App Opened (Content Intent) | `openSourceApp()` | `TapExpandedBody` | No (Source app auto-cancels) | No |

### 2.1. Critical Tombstone Invariant: Source Removal vs User Dismissal
- If a user dismisses a notification from the Island (`×` button), a tombstone is recorded to prevent spammy minor updates (e.g., download progress 60% → 70%).
- If the source app removes the notification (e.g., user cleared it in the system shade, or the app completed its background work), [`IslandStateManager.onSourceNotificationRemoved(id)`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt#L538-L554) calls:
  ```kotlin
  IslandSuppressionTombstoneRegistry.clear(notificationId)
  ```
  **Result:** When the app later posts a new notification with the same ID, it is treated as a genuinely new notification (`A28`), alerting the user immediately.

---

## 3. Non-Destructive Swipe-Up & Collapse Semantics

Previous versions suffered from a destructive bug where swipe-up on an expanded notification called `dismissActiveNotification()`, destroying the notification from the queue and attempting OS cancellation.

Under the new architecture, swipe-up triggers `IslandStateManager.collapseExpanded()`, enforcing non-destructive collapse:

```
┌───────────────────────────────────────┬───────────────────────────────────────┬───────────────────────────────┐
│ Expanded State                        │ Result After Swipe-Up / Outside-Tap   │ Queue / OS State              │
├───────────────────────────────────────┼───────────────────────────────────────┼───────────────────────────────┤
│ Notification (1 item, expanded)       │ Notification (1 item, compact capsule)│ Queue intact (1 item), OS OK  │
│ Notification Pager (12 items, expand) │ Notification (12 items, compact badge)│ Queue intact (12 items), OS OK│
│ Notification with active Music        │ NotificationWithMusic (compact)       │ Notification & Music intact   │
│ Music expanded controls               │ Music (compact album + waveform)      │ Playback & Track intact       │
│ Live Activity (Timer expanded)        │ Live Activity (Timer compact)         │ Timer countdown running       │
│ Ongoing Call expanded controls        │ Ongoing Call (compact pill)           │ Call duration intact          │
│ Compact Notification (already compact)│ Non-destructive NO-OP (returns null)  │ Zero notifications destroyed  │
│ Incoming Call alert                   │ Leaves incoming call intact           │ Ringing / Vibrating untouched │
└───────────────────────────────────────┴───────────────────────────────────────┴───────────────────────────────┘
```

**Verification:** Validated by `IslandBehaviorEngineTest.test_stateManagerNonDestructiveCollapse_forAllStates` and `test_U01_swipeUpExpandedNotification_collapseOnly_osShadeAndQueueUnchanged`.

---

## 4. Optional OS-Dismiss Preference & Tombstones

In [`AuroraPreferences.kt:33`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/preferences/AuroraPreferences.kt#L33):
```kotlin
val KEY_ISLAND_DISMISS_CANCELS_OS = booleanPreferencesKey("island_dismiss_cancels_os")
```
- **Default value:** `false` (Island-only dismissal by default, per `UX-C06`).
- **Safety guarantee:** Even if enabled, ongoing/non-clearable notifications (media playback, timers, active calls) NEVER emit `CancelAndroidNotification` (`A19`).

---

## 5. Milestone Resurfacing Rules & Validation Matrix

When a notification is dismissed by the user from the Island, minor updates must be suppressed, while critical milestones resurface:

| Scenario | Incoming Notification Content | Evaluation Result | Island Behavior |
|---|---|---|---|
| Progress 60% → 70% | `Order preparing (70%)`, `progress=70` | `SuppressedMinorUpdate` | **Suppressed**; island remains idle (`A21`) |
| 1st Nearby Alert | `Driver is 2 stops away` | `ResurfaceMilestone` | **Resurfaces once**; expands transient peek (`A22`) |
| 2nd Nearby Decrement | `Driver is 1 stop away` | `SuppressedMinorUpdate` | **Suppressed**; no repeated alert spam (`A23`) |
| Arriving Now | `Driver is arriving now!` | `ResurfaceMilestone` | **Resurfaces**; expands transient peek (`A24`) |
| Delivered / Completed | `Your order has been delivered` | `ResurfaceMilestone` | **Resurfaces**; informs user of completion |
| Minor ETA Jitter | `Driver 2 stops away (ETA 12:04)` | `SuppressedMinorUpdate` | **Suppressed**; ETA fluctuations filtered |
| Conversation Message | `com.whatsapp`, new message text | `ResurfaceMilestone` | **Resurfaces**; genuine new chat content (`A27`) |
| Repaint with same text| Same title & text, timestamp update | `SuppressedMinorUpdate` | **Suppressed**; no visual disruption |
| Localized / Unknown | Unrecognized delivery text | Conservative fallback | Kept in queue; does not interrupt |

**Verification:** Validated by `IslandBehaviorEngineTest.test_milestoneResurfacing_comprehensiveSuite` with 100% assertions passing.
