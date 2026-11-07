# Task 02A — Dynamic Island State Machine & Gesture Interaction Contract

**Document:** `reports/TASK_02A_STATE_GESTURE_CONTRACT.md`  
**Author:** Android Development Team  
**Reviewer & Examiner:** ChatGPT — Principal Product Architect and Final Examiner  
**Status:** Complete Architectural Specification  
**Code Modifications:** None (`0` lines in `app/src/main/`)

---

## 1. Overview & Architectural Philosophy

Previous iterations suffered from state explosion: attempting to model every combination of screen width, notification count, and active media as a discrete monolithic state class.

This contract replaces monolithic states with an **Orthogonal Multi-Dimensional Model**. Any instantaneous state of the Dynamic Island is represented as a tuple of four independent orthogonal dimensions:

$$\text{Island State} = \langle \text{Domain State}, \text{Presentation Mode}, \text{Interaction Flag}, \text{Queue Cursor} \rangle$$

By decoupling *what data is being presented* (Domain) from *how large it is rendered* (Presentation) and *what the user is currently doing* (Interaction), we ensure complete predictability across all 16 lifecycle situations and prevent unhandled edge cases.

---

## 2. The Orthogonal 4-Dimensional State Model

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        ORTHOGONAL 4-DIMENSIONAL STATE MODEL                            │
├─────────────────────────┬─────────────────────────┬────────────────────────────────────┤
│ Dimension               │ Type / Sealed Hierarchy │ Allowed Values & Semantic Meaning  │
├─────────────────────────┼─────────────────────────┼────────────────────────────────────┤
│ 1. Domain State         │ `IslandDomainState`     │ • `None` (Idle / System Cutout)    │
│    (What data is active)│                         │ • `SystemAlert` (Hardware, Volume) │
│                         │                         │ • `OngoingCall` (Active Telephony) │
│                         │                         │ • `IncomingCall` (Ringing Phone)   │
│                         │                         │ • `MediaPlayback` (Now Playing)    │
│                         │                         │ • `LiveActivity` (Timer, Nav, Run) │
│                         │                         │ • `Notification` (Single App Alert)│
│                         │                         │ • `MultiActivity` (Dual Active)    │
├─────────────────────────┼─────────────────────────┼────────────────────────────────────┤
│ 2. Presentation Mode    │ `IslandPresentationMode`│ • `Hidden` (0dp x 0dp or detached) │
│    (How large it renders│                         │ • `MinimalDot` (36.67dp circle)    │
│                         │                         │ • `CompactCapsule` (36.67dp body)  │
│                         │                         │ • `TemporaryPreview` (Auto-timer)  │
│                         │                         │ • `UserExpandedCard` (User opened) │
│                         │                         │ • `SplitDoubleIsland` (Pill + Dot) │
├─────────────────────────┼─────────────────────────┼────────────────────────────────────┤
│ 3. Interaction Flag     │ `IslandInteractionState`│ • `Resting` (No active touch)      │
│    (User touch/motion)  │                         │ • `TrackingDrag` (Swipe gesture)   │
│                         │                         │ • `PagingHorizontal` (Pager swipe) │
│                         │                         │ • `InlineReplying` (IME focused)   │
│                         │                         │ • `AnimatingTransition` (In flight)│
├─────────────────────────┼─────────────────────────┼────────────────────────────────────┤
│ 4. Queue Cursor         │ `QueueCursor`           │ • `currentIndex: Int`              │
│    (Multi-item browsing)│                         │ • `totalItems: Int`                │
│                         │                         │ • `activeKey: String?`             │
└─────────────────────────┴─────────────────────────┴────────────────────────────────────┘
```

---

## 3. Mapping the 16 Required Behavioral Situations

The table below maps each of the 16 core behavioral requirements directly into this orthogonal model:

| # | Behavioral Situation | Domain State | Presentation Mode | Interaction Flag | Queue Cursor | Description & Lifecycle Trigger |
|:---:|---|---|---|---|---|---|
| **1** | **Hidden / Inactive** | `None` | `Hidden` | `Resting` | `[0, 0]` | Overlay detached or 0dp; screen cutout exposed natively. |
| **2** | **Idle Island** | `None` | `CompactCapsule` | `Resting` | `[0, 0]` | Ambient black capsule hugging hardware camera cutout. |
| **3** | **Compact Activity** | `LiveActivity` / `Media` | `CompactCapsule` | `Resting` | `[0, 1]` | Single ongoing background task active (Timer, Spotify). |
| **4** | **Minimal Activity** | `LiveActivity` | `MinimalDot` | `Resting` | `[1, 2]` | Secondary activity detached circle when 2 items run. |
| **5** | **Temporary Expanded Preview** | `Notification` | `TemporaryPreview` | `Resting` | `[1, N]` | Auto-expanded on incoming notification; 3.0s timer active. |
| **6** | **User-Opened Expanded** | `DomainState` | `UserExpandedCard` | `Resting` | `[k, N]` | User opened via touch-and-hold; auto-timer disabled. |
| **7** | **Multi-Notification Pager**| `Notification` | `UserExpandedCard` | `PagingHorizontal`| `[k, 5]` | User swiping horizontally across 5 pending notifications. |
| **8** | **Music + Concurrent Notif** | `MultiActivity` | `SplitDoubleIsland` | `Resting` | `[1, 2]` | Music in leading/trailing; notification in minimal dot. |
| **9** | **Active Call** | `OngoingCall` | `CompactCapsule` | `Resting` | `[0, 1]` | Green call icon, timer duration, audio waveform. |
| **10** | **Incoming Call** | `IncomingCall` | `UserExpandedCard` | `Resting` | `[0, 1]` | Urgent preemption: caller avatar, Accept/Decline buttons. |
| **11** | **Inline Reply** | `Notification` | `UserExpandedCard` | `InlineReplying` | `[k, N]` | RemoteInput text field focused; Android soft keyboard open. |
| **12** | **Activity Interruption** | `IncomingCall` | `UserExpandedCard` | `Resting` | `[0, 1]` | High-priority event preempts current preview; saves state. |
| **13** | **Animation in Progress** | `Any` | `Transitioning` | `AnimatingTransition`| `Cursor` | Spring physics actively morphing width/height (380ms). |
| **14** | **Automatic Collapse** | `Notification` | `CompactCapsule` | `Resting` | `[k, N]` | 3.0s timer elapsed; card collapses, item preserved in queue.|
| **15** | **User-Requested Collapse** | `DomainState` | `CompactCapsule` | `TrackingDrag` | `Cursor` | User swiped up or tapped outside to collapse card. |
| **16** | **Dismissal & Eviction** | `DomainState` | `Compact` / `Idle` | `Resting` | `[k, N-1]` | User tapped Close button; item evicted from queue. |

---

## 4. End-to-End Walkthrough: The Required WhatsApp Example

### Baseline Scenario:
The island is in `Idle` mode (`None`, `CompactCapsule`, `Resting`).  
A WhatsApp notification arrives from "Alice": *"Are we still meeting at 3 PM?"*

```
[Time 0.0s]  WhatsApp SBN arrives at IslandNotificationListener
             ├── Extract content: app="WhatsApp", sender="Alice", message="Are we..."
             ├── Ingest into IslandNotificationQueue (Key="wa_01", Rank=High)
             └── State becomes: <Notification, TemporaryPreview, Resting, Cursor[1, 1]>

[Time 0.0s]  Island animates from CompactCapsule (180x36.67dp) to TemporaryPreview (371x140dp)
             ├── Physics: Spring (stiffness=380f, damping=0.82f, duration ~380ms)
             ├── Top row: WhatsApp icon, "WhatsApp · Alice", 10dp Close button
             ├── Body row: "Are we still meeting at 3 PM?" (max 2 lines, ellipsis)
             ├── Actions row: [Reply] [Mark as Read]
             └── Coroutine launches: AutoCollapseTimer(delay = 3000ms)

[Time 3.0s]  AutoCollapseTimer expires without user interaction
             ├── State transitions to: <Notification, CompactCapsule, Resting, Cursor[1, 1]>
             ├── Spring morphs smoothly from 371x140dp to 210x36.67dp
             ├── Left slot: WhatsApp green icon (20dp)
             ├── Right slot: Unread indicator badge ("1")
             ├── Queue Status: Item "wa_01" REMAINS in IslandNotificationQueue
             └── Android Status: Original Android StatusBarNotification is UNTOUCHED
```

---

### Edge Case Analysis:

#### 1. Second notification arrives after 1.0 second (from "Bob" via Telegram):
- **Behavior:** The existing temporary preview for Alice is **not** abruptly destroyed. 
- **Mechanism:** Telegram notification is ingested into `IslandNotificationQueue` at position 2. A subtle inline badge pops on the card header: `1 of 2 · New from Bob`. The auto-collapse timer is reset to **+2.0s**. The user can swipe left to view Bob immediately or let both collapse into the compact capsule badge (`"2"`).

#### 2. Incoming phone call interrupts the preview:
- **Behavior:** Urgent preemption (`P0 Priority`).
- **Mechanism:** The WhatsApp preview is immediately halted. WhatsApp state is preserved in `IslandNotificationQueue` without loss. The island instantly morphs into `IncomingCall` expanded presentation (Caller ID, Accept/Decline). When the call ends or is declined, the island restores the surviving WhatsApp notification in `CompactCapsule` mode.

#### 3. User is reading the message (finger touching the card):
- **Behavior:** The auto-collapse timer **pauses immediately** on `ACTION_DOWN`.
- **Mechanism:** As long as the user's finger maintains contact or moves within the card, the countdown is frozen. Upon `ACTION_UP`, a fresh 3.0s grace timer begins before collapsing.

#### 4. User touches and holds the island:
- **Behavior:** Promotion to permanent user expansion.
- **Mechanism:** Holding the island for $\ge 400\text{ms}$ transitions `PresentationMode` from `TemporaryPreview` to `UserExpandedCard`. The auto-collapse timer is **canceled completely**. The card will now remain open indefinitely until the user explicitly swipes up, taps outside, or interacts.

#### 5. User is typing a reply (Inline Reply active):
- **Behavior:** Auto-collapse is **100% disabled**; overlay window sets `SOFT_INPUT_ADJUST_RESIZE`.
- **Mechanism:** Clicking [Reply] opens an inline `BasicTextField` with focus requested. The Android software keyboard appears. The island adjusts its vertical offset to remain visible above the IME. Auto-collapse remains dormant until the user either taps Send (which dispatches via `RemoteInput`) or dismisses the keyboard.

#### 6. User swipes up on the card:
- **Behavior:** **Collapse to Compact** (NOT destructive dismissal).
- **Mechanism:** Swiping up collapses the card back to `CompactCapsule`. The WhatsApp notification **remains active in the queue** and visible as a compact badge (`"1"`). The original Android notification in the system shade is **not canceled**.

#### 7. User taps outside the expanded card:
- **Behavior:** Immediate collapse to compact.
- **Mechanism:** Overlay window with `FLAG_WATCH_OUTSIDE_TOUCH` intercepts `MotionEvent.ACTION_OUTSIDE`. The card animates back to `CompactCapsule`. The tap passes through transparently to the underlying third-party app.

#### 8. Device locks (Screen off / Keyguard):
- **Behavior:** Overlay immediately hides and tears down active touch windows.
- **Mechanism:** `IslandOverlayService` listens for `ACTION_SCREEN_OFF`. The overlay visibility is set to `GONE`. Touch listeners are disabled. When the device is unlocked, the island restores in `CompactCapsule` mode with unread indicators intact.

#### 9. Notification removed by source application (WhatsApp marked read on desktop):
- **Behavior:** Immediate queue eviction and smooth animation.
- **Mechanism:** `IslandNotificationListener.onNotificationRemoved` fires with WhatsApp's key. The item is evicted from `IslandNotificationQueue`. If it was the only item, the island animates smoothly from `CompactCapsule` to `Idle` (or `Hidden`). If the card was currently expanded, it slides away with a cross-fade to the next available item.

---

## 5. Strict Distinction of Lifecycle Operations

To eliminate developer confusion, the following five operations are formally separated:

```
┌───────────────────────────────┬─────────────────────────────────────────────────────────────┐
│ Operation                     │ Exact Architectural Impact & Scope                          │
├───────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ 1. Collapse                   │ Hides the expanded card; transitions PresentationMode to    │
│                               │ CompactCapsule. Activity and notification queue UNCHANGED.  │
├───────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ 2. Dismiss Island Preview     │ Closes the temporary 3.0s preview immediately, returning to │
│                               │ compact capsule. Notification remains queued in island.     │
├───────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ 3. Remove From Queue          │ Evicts the notification from IslandNotificationQueue. Island│
│                               │ badge decrements or returns to Idle. System shade UNTOUCHED.│
├───────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ 4. Cancel Android Notification│ Calls NotificationListenerService.cancelNotification(key).  │
│                               │ Removes the notification from the Android system shade.     │
├───────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ 5. End Live Activity          │ Terminates an ongoing background session (stops timer,      │
│                               │ releases audio focus, cancels navigation route).            │
└───────────────────────────────┴─────────────────────────────────────────────────────────────┘
```

---

## 6. Complete Gesture Interaction Specification

Every supported user interaction is formalized under the contract:  
$$\text{Current State} \xrightarrow{\text{User Gesture}} \text{Action (Current vs. Proposed)} \xrightarrow{\text{Animation}} \text{Target State} \xrightarrow{\text{Lifecycle Impact}}$$

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       DEFINITIVE GESTURE DISPATCH MATRIX                                               │
├───────────────────┬──────────────┬────────────────────────┬────────────────────────┬─────────────────┬─────────────────┤
│ Current State     │ User Gesture │ Production Behavior    │ Proposed Behavior      │ Target State    │ Lifecycle Impact│
├───────────────────┼──────────────┼────────────────────────┼────────────────────────┼─────────────────┼─────────────────┤
│ Idle Capsule      │ Single Tap   │ NONE (No action)       │ Quick Controls / Bounce│ UserExpanded    │ None            │
│ Idle Capsule      │ Long Press   │ OPEN_QUICK_CONTROLS    │ Settings Vault / Calib │ Settings Screen │ None            │
│ Idle Capsule      │ Double Tap   │ Unhandled (2x click)   │ Ignored / Safe Debounce│ Idle Capsule    │ None            │
│ Idle Capsule      │ Swipe Up     │ Hides overlay (0dp)    │ Hides overlay (0dp)    │ Hidden          │ None            │
├───────────────────┼──────────────┼────────────────────────┼────────────────────────┼─────────────────┼─────────────────┤
│ Compact Notif     │ Single Tap   │ OPEN_NOTIFICATIONS     │ Launch Owning App      │ Compact / App   │ None            │
│ Compact Notif     │ Long Press   │ OPEN_QUICK_CONTROLS    │ Expand Notif Card      │ UserExpanded    │ None            │
│ Compact Notif     │ Double Tap   │ Leakage to Dismiss btn │ Debounced Expansion    │ UserExpanded    │ None            │
│ Compact Notif     │ Swipe Up     │ CANCEL_NOTIFICATION    │ Collapse to Compact    │ Compact / Idle  │ Preserved in SBN│
│ Compact Notif     │ Swipe Left   │ Unhandled (Static)     │ Next Queue Item        │ Compact Notif   │ Next in Queue   │
│ Compact Notif     │ Swipe Right  │ Unhandled (Static)     │ Prev Queue Item        │ Compact Notif   │ Prev in Queue   │
├───────────────────┼──────────────┼────────────────────────┼────────────────────────┼─────────────────┼─────────────────┤
│ Expanded Card     │ Single Tap   │ Launch Owning App      │ Launch Owning App      │ Compact / App   │ None            │
│ Expanded Card     │ Swipe Up     │ CANCEL_NOTIFICATION    │ Collapse to Compact    │ Compact Notif   │ Preserved in SBN│
│ Expanded Card     │ Swipe Down   │ Expands System Shade   │ Expands System Shade   │ System UI Shade │ None            │
│ Expanded Card     │ Swipe Left   │ Unhandled (Static +N)  │ Next Notif (Pager)     │ Expanded (Next) │ Cursor Next     │
│ Expanded Card     │ Swipe Right  │ Unhandled (Static +N)  │ Prev Notif (Pager)     │ Expanded (Prev) │ Cursor Prev     │
│ Expanded Card     │ Tap Outside  │ Passes touch + collapse│ Passes touch + collapse│ Compact Notif   │ Preserved in SBN│
│ Expanded Card     │ Action Click │ Execute Action / Reply │ Execute Action / Reply │ Expanded / Comp │ Intent Sent     │
│ Expanded Card     │ Close (x)    │ Evict from Island Queue│ Evict from Island Queue│ Next / Compact  │ Evicted from DI │
├───────────────────┼──────────────┼────────────────────────┼────────────────────────┼─────────────────┼─────────────────┤
│ Detached Dot      │ Single Tap   │ Swap with Primary Pill │ Swap with Primary Pill │ Compact (Swap)  │ Priority Changed│
│ Detached Dot      │ Long Press   │ OPEN_QUICK_CONTROLS    │ Expand Secondary Item  │ UserExpanded    │ None            │
├───────────────────┼──────────────┼────────────────────────┼────────────────────────┼─────────────────┼─────────────────┤
│ Mid-Animation     │ Any Tap      │ Dropped / Hitbox Drift │ Staged Entry / Target  │ Final Target    │ Safe Buffer     │
└───────────────────┴──────────────┴────────────────────────┴────────────────────────┴─────────────────┴─────────────────┘
```

---

## 7. Crucial Gesture Policies & Hardening Architecture

### A. Swipe-Up Default: Non-Destructive Collapse
- **Production Defect:** In current production (`IslandOverlayService.kt:699`), swiping up calls `IslandNotificationListener.instance?.cancelNotification(notificationId)`, which cancels the notification in the Android system shade.
- **Mandated Policy:** **Swipe-Up = Collapse to Compact**. Swiping up transitions presentation to `CompactCapsule`, preserving the notification in `NotificationReducer` and leaving the system shade completely untouched.

### B. Double-Tap Hardening: Staged Button Entry & Explicit Double-Tap
- **Retraction of 300ms Universal Lockout:** A blanket 300ms touch blackout is retracted as it introduces input lag and violates accessibility responsiveness.
- **Adopted Architectural Solution:**
  1. **Staged Button Entry:** Action buttons and the Close `(x)` button remain inactive and faded out until the spring animation reaches settling velocity ($t \ge 300\text{ms}$).
  2. **Explicit Double-Tap Handling:** `combinedClickable` includes an explicit `onDoubleClick = { /* no-op or expand */ }` lambda, preventing the second tap from registering as a distinct click event on the newly composed layout coordinates.

### C. Gesture Arbitration: Horizontal Paging vs. Vertical Collapse
- **Retraction of 1.5x Directional Threshold:** A fixed multiplier is insufficient for real-world curved thumb swipes and nested scroll containers.
- **Adopted Architectural Solution:**
  - Evaluates touch angle $\theta = \arctan(|\Delta y| / |\Delta x|)$ on initial displacement $> 8\text{dp}$.
  - $\theta < 30^\circ$: Locked exclusively to `HorizontalPager`.
  - $\theta > 60^\circ$ and $\Delta y < 0$: Locked exclusively to Vertical Collapse.
  - Intermediate angles ($30^\circ \le \theta \le 60^\circ$): Consumption deferred until displacement exceeds 16dp.
  - Integrates with `NestedScrollConnection` so reaching the bounds of the pager does not jerk or drag the parent window.
