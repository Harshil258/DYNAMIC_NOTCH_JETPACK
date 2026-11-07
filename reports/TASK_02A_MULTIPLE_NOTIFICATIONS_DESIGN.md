# Task 02A — Android-Exclusive Multi-Notification Horizontal Pager Architecture

**Document:** `reports/TASK_02A_MULTIPLE_NOTIFICATIONS_DESIGN.md`  
**Author:** Android Development Team  
**Reviewer & Examiner:** ChatGPT — Principal Product Architect and Final Examiner  
**Status:** Complete Architectural Specification  
**Code Modifications:** None (`0` lines in `app/src/main/`)

---

## 1. Vision & Android-Exclusive Value Proposition

On iOS, the Dynamic Island never hosts multiple notifications; users must pull down the entire Notification Center to browse unread messages.

On Android, we have the opportunity to deliver a flagship-grade experience: **A Fluid, Horizontal Multi-Notification Pager inside the Dynamic Island**. When multiple notifications arrive, the expanded island transforms into an elegant, swipeable card deck, enabling users to glance, paginate, action, and reply to multiple conversations without leaving their foreground application.

---

## 2. Concrete Reference Scenario: The 5-Notification Deck

To ground the architectural design, we evaluate the system under a realistic, heterogeneous 5-notification payload:

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       THE 5-NOTIFICATION DECK                                          │
├─────┬───────────────────┬─────────────────────┬───────────────────┬──────────────┬─────────────────────┤
│ Pos │ Application       │ Category / Style    │ Content Summary   │ Actions      │ Priority Tier       │
├─────┼───────────────────┼─────────────────────┼───────────────────┼──────────────┼─────────────────────┤
│ 1   │ WhatsApp          │ MessagingStyle      │ Alice: "Meeting?" │ [Reply] [Seen│ P1 (High/Messaging) │
│ 2   │ Gmail             │ BigTextStyle        │ Flight Confirmation[Archive][Trash│ P2 (Normal/Email)   │
│ 3   │ Telegram          │ MessagingStyle      │ Team: "Patch live"│ [Reply] [Mute│ P1 (High/Messaging) │
│ 4   │ Amazon Shopping   │ Progress / Delivery │ Out for delivery  │ [Track Map]  │ P2 (Live Progress)  │
│ 5   │ Google Calendar   │ Event Reminder      │ "Design Review"   │ [Join Call]  │ P1 (Time-Sensitive) │
└─────┴───────────────────┴─────────────────────┴───────────────────┴──────────────┴─────────────────────┘
```

---

## 3. The 16 Architectural Pillars of the Pager Engine

### 1. Notification Queue Ownership & Complete Reducer Reuse
- **Owner:** The notification queue is owned exclusively by the existing production [`NotificationReducer.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt), living within the singleton `IslandOverlayService` scope.
- **State Structure:** Uses the existing production `NotificationQueueState`:
  ```kotlin
  data class NotificationQueueState(
      val notifications: List<NotificationInfo> = emptyList(),
      val activeNotificationId: String? = null
  )
  ```
- **Concurrency & Parity:** Both production overlay services and debug Playground test harnesses dispatch identical `NotificationEvent` instances (`Posted`, `Removed`, `Selected`, `Next`, `Previous`, `DismissActive`) through `NotificationReducer.reduce()`. This guarantees 100% architectural lifecycle parity without creating a second independent queue.

### 2. Authoritative Stable Notification Identity (`StableKey`)
- **Correction:** We retract the proposed custom compound key that appended `postTime`. In Android, `postTime` changes on updates, which breaks deduplication.
- **Authoritative Key:** The identity is strictly bound to `StatusBarNotification.key` (`NotificationInfo.id`), as established in `NotificationReducer.kt`:
  $$\text{StableKey} = \text{StatusBarNotification.key} = \text{"[userId]|[packageName]|[id]|[tag]|[uid]"}$$
- **Update Guarantee:** Content updates (e.g. messaging progress or new chat lines) retain the identical `StableKey`, allowing `NotificationReducer.findMatchingIndex()` to update records in-place without altering the active selection or jumping the pager.

### 3. Selection by Stable Key & Priority Ordering
- **Selection by Key (Not Fragile Index):** Pager selection is tracked via `activeNotificationId: String?` rather than a fragile integer index. If an item at index 0 is removed while the user views index 1, the user remains on the exact item they were reading.
- **Queue Ordering:** Reuses `NotificationReducer.sortQueue()`, sorting by:
  $$\text{OTP / Financial} \succ \text{Priority} \succ \text{Importance} \succ \text{Ongoing} \succ \text{Timestamp}$$

### 4. Unread & Total Counters: Glanceable Navigation UI
- **Header Badging:** The expanded card header displays a unified context pill:
  ```
  [ WhatsApp Icon ]  WhatsApp · Alice · 1 of 5   ● ○ ○ ○ ○   [ (x) Close ]
  ```
- **Indicator Styling:** A horizontal row of rounded pip indicators (6dp active pill, 4dp inactive circles) with smooth spring interpolation on page changes.

### 5. Horizontal Page Transition Animation
- **Motion Spec (Proposed):** Apple-grade spring physics:
  - `stiffness = 400f`, `dampingRatio = 0.85f` (Proposed tuning).
  - Cards slide with **Parallax Layering**: the background container remains stationary while card text, avatars, and action chips translate with a 15% parallax offset and subtle cross-fade (`alpha` $1.0 \to 0.0$ over 180ms).

### 6. Dynamic Height Management
- **The Challenge:** WhatsApp card is 140dp tall; Gmail card is 190dp tall; Amazon progress card is 115dp tall.
- **The Architecture:**
  - The outer island window measures content dynamically via [`IslandContentViewport`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandContentViewport.kt).
  - As the user pages horizontally, the outer pill height smoothly interpolates via `animateDpAsState` to match the target card's intrinsic height.
  - Proposed Minimum height: **110dp**; Proposed Maximum height: **220dp** (excess text scrolls internally within the card to prevent occluding the entire screen).

### 7. New-Notification Insertion While Pager is Open
- **The Problem:** If the user is reading Page 2 (Gmail) and an urgent Slack message arrives, shifting the pager immediately would cause jarring visual dislocation.
- **The Policy:**
  - If the pager is **actively open and touched**, new notifications are inserted into the underlying queue, but the current viewport position is **pinned to the active `activeNotificationId`**.
  - A subtle notification banner pops at the bottom of the card: `New message from Slack (Swipe to view)`.
  - The total counter updates smoothly: `2 of 5` $\to$ `3 of 6`.

### 8. In-Place Notification Updates
- If Amazon posts a progress update while Page 4 is open:
  - The progress bar value animates smoothly from $45\% \to 60\%$ via `animateFloatAsState`.
  - Text label updates from *"Out for delivery"* to *"Driver 2 stops away"* with a cross-fade transition.
  - Zero page displacement or scroll offset jumps because `NotificationReducer` updates the item in-place.

### 9. Removal of the Current Page
- If the user taps `[Mark as Read]` on Page 1 (WhatsApp):
  - WhatsApp dispatches its action intent.
  - Page 1 animates out: scales to $0.9\times$ and fades upward over 200ms.
  - `NotificationReducer` selects the adjacent surviving neighbor (`nextActiveId`), and Page 2 (Gmail) smoothly slides into the primary focus position.
  - Counter updates from `1 of 5` $\to$ `1 of 4`.
  - If the last remaining notification is removed, the island automatically animates back to `CompactCapsule` or `Idle`.

### 10. Removal of Another Page
- If an email is read on a desktop laptop, Android posts `onNotificationRemoved` for Page 2 (Gmail):
  - While the user is looking at Page 1, Gmail is evicted from the queue silently by `NotificationReducer`.
  - The pager indicator updates from `1 of 5` $\to$ `1 of 4`.
  - The user's active reading context on Page 1 is completely undisturbed because selection is keyed to `activeNotificationId`.

### 11. Automatic Collapse Behavior
- **Rule:** While the multi-notification pager is **expanded**, the default 3.0s auto-collapse timer is **PAUSED**.
- **Inactivity Timeout (Proposed):** If no touch, swipe, or interaction occurs for **15.0 seconds (proposed default)**, the expanded pager gracefully collapses back to `CompactCapsule` to avoid permanently blocking the user's screen.
- Every horizontal swipe or tap resets the 15-second inactivity timer.

### 12. Pager Cursor Memory Across Collapse / Re-expansion
- If the user was viewing Page 3 (Telegram) and collapses the island by tapping outside:
  - The compact capsule displays Telegram's blue icon and unread badge (`"5"`).
  - When the user subsequently touches & holds to re-expand, the pager **restores directly to Telegram via `activeNotificationId`** instead of resetting to Page 1.
  - Memory expires after **60 seconds (proposed)** of inactivity, reverting default focus to highest priority.

### 13. Action Buttons per Notification
- Each page extracts and displays up to **2 primary action buttons** directly from `StatusBarNotification.notification.actions`:
  - WhatsApp: `[Reply]` (filled pill) and `[Mark as Read]` (tonal outline).
  - Gmail: `[Archive]` and `[Delete]`.
  - Amazon: `[Track Order]`.
- Additional actions (3+) are accessible via a subtle overflow `[...]` icon.

### 14. Inline Reply in a Selected Page
- Tapping `[Reply]` on Page 1 (WhatsApp) transitions the card into `InlineReplying` mode:
  - Horizontal paging is **strictly locked** to prevent accidental swipes while typing.
  - A sleek glassmorphic `BasicTextField` expands with an autofocus request.
  - Android soft keyboard (IME) displays.
  - Tapping `[Send]` delivers text via Android's `RemoteInput.addResultsToIntent()` and triggers a checkmark send animation before collapsing the card.

### 15. Phone Call Interruption During Pager Browsing
- If a phone call rings while the user is swiping through the pager:
  - **P0 Telephony Preemption:** The island immediately morphs into the full `IncomingCall` presentation.
  - The entire 5-notification deck state and active page index are saved into `SuspendedPagerState`.
  - Zero notifications are lost or dismissed.

### 16. Restoration After Call Termination
- When the user finishes or declines the call:
  - The island verifies whether the 5 notifications are still valid in the system shade.
  - If valid, the island morphs smoothly back into `CompactCapsule` (displaying the unread badge `"5"`) or restores the expanded pager if the call lasted under 5 seconds.

---

## 4. Compose Implementation Evaluation: `HorizontalPager` vs. Custom Drag Stack

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                   IMPLEMENTATION COMPARISON: HORIZONTAL PAGER vs. CUSTOM                 │
├─────────────────────────┬───────────────────────────────┬────────────────────────────────┤
│ Evaluation Criterion    │ Compose HorizontalPager       │ Custom Drag-and-Snap Stack     │
├─────────────────────────┼───────────────────────────────┼────────────────────────────────┤
│ 1. Code Complexity      │ Low (Off-the-shelf Foundation)│ High (Custom touch math & snap)│
│ 2. Dynamic Height Sizing│ Difficult (Requires item wrap)│ Native (Measure active item)   │
│ 3. Directional Locking  │ High risk of vertical conflict│ Native (Math-based dx/dy lock) │
│ 4. WindowManager Touch  │ Can drop touches on edge drag │ Complete pointer stream control│
│ 5. Memory / Page Prefetch│ Native `beyondBoundsPageCount`│ Manual lazy instantiation      │
└─────────────────────────┴───────────────────────────────┴────────────────────────────────┘
```

### Recommendation:
Adopt **Jetpack Compose `HorizontalPager`** with two critical hardening layers:
1. **`beyondBoundsPageCount = 1`**: Keeps adjacent cards pre-composed to guarantee zero frame drops during fast horizontal swipes.
2. **Custom Parent Pointer Filter**: An enclosing `Modifier.pointerInput` intercepts pointer deltas to enforce the **Directional Slop Lock** ($|\Delta x| > 1.5 \times |\Delta y|$) before delegating to `HorizontalPager`, permanently eliminating vertical collapse conflicts.

---

## 5. Notification Eligibility & Filtering Rules

Not all Android notifications belong in the horizontal pager. The queue enforces strict categorization:

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                       NOTIFICATION ELIGIBILITY TAXONOMY                        │
├────────────────────┬──────────────────┬────────────────────────────────────────┤
│ Category           │ Eligibility      │ Presentation Behavior                  │
├────────────────────┼──────────────────┼────────────────────────────────────────┤
│ Messaging (SMS/WA) │ Eligible         │ Standard horizontal pageable card.     │
│ Emails & Reminders │ Eligible         │ Standard horizontal pageable card.     │
│ Ongoing Media      │ Excluded (Sep)   │ Rendered as dedicated Now Playing pill.│
│ Active Telephony   │ Excluded (P0)    │ Dedicated Call Island (Preempts all).  │
│ Progress / Running │ Eligible (Sep)   │ Live Activity card (Timer, Navigation).│
│ Silent / Low-Pri   │ Excluded         │ Ignored by island; remains in shade.   │
│ Group Summary Only │ Excluded         │ Redundant summaries collapsed to child.│
└────────────────────┴──────────────────┴────────────────────────────────────────┘
```
