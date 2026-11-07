# Master Dynamic Island Gesture & Interaction Matrix (Corrected)

## 1. Overview of Gesture Detection Seams

User interactions in the Dynamic Island are processed through two distinct gesture layers:

1. **Window-Level Pointer Listener ([`IslandOverlayService.kt:498-511`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandOverlayService.kt#L498-L511) & [`PlaygroundOverlayService.kt:85-87`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundOverlayService.kt#L85-L87)):**
   - Intercepts `MotionEvent.ACTION_OUTSIDE` when the user touches anywhere outside the floating overlay bounds.
   - Automatically collapses expanded cards back to compact or minimal mode and closes open inline reply keyboards.

2. **Composable-Level Gestures ([`DynamicIslandPill.kt:774-889`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt#L774-L889) & `1270-1289`):**
   - **Vertical Drag (`detectVerticalDragGestures`):** Rubber-bands vertical translation up to +60px (downward) and -100px (upward). Dragging upward past the `-40f` threshold fires `onSwipeDismiss()`. Downward drags snap back to zero via `islandSpring()` without action.
   - **Taps & Long-Presses (`Modifier.combinedClickable`):** Attached to the main pill and companion split bubble. `onClick` invokes `onTap()` / `onCompanionTap()`; `onLongClick` invokes `onLongPress()` / `onCompanionLongPress()`.
   - **Double-Tap:** **NOT IMPLEMENTED** (`onDoubleClick` is omitted).
   - **Horizontal Drag:** **NOT IMPLEMENTED** (no horizontal drag detector).

---

## 2. Exhaustive Island × State × Gesture Matrix

| Island & State | Single Tap | Double Tap | Long Press | Swipe Up | Swipe Down | Swipe Left | Swipe Right | Action Button Click | Outside Tap |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Idle / Minimal Capsule** | **NONE** (No action) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss** (Hides island) | *UNSUPPORTED* (Snaps to 0) | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Notification (Compact - Single)** | **Expand Card** (`OPEN_NOTIFICATIONS`) | *UNSUPPORTED* (Fires 2 single taps) | **Open Quick Controls** | **Dismiss & Cancel Notification** | *UNSUPPORTED* (Snaps to 0) | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Notification (Compact - Stacked)** | **Expand Card** (`OPEN_NOTIFICATIONS`) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss & Cancel Notification** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | Companion tap: **Select Next Notification** | **NONE** |
| **Notification (Expanded Card)** | **NONE** (Consumes touch inside card) | *UNSUPPORTED* | **NONE** | **Dismiss Card & Notification** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Dispatches Action / Opens Inline Reply** | **Collapse to Compact** |
| **Notification (Inline Reply Mode)** | **NONE** | *UNSUPPORTED* | **NONE** | **Dismiss Card** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Send Reply / Cancel Reply** | **Cancel Reply & Collapse** |
| **Music (Compact)** | **Expand Media Player** (`OPEN_MUSIC`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | Compact play/pause (if enabled in settings) | **NONE** |
| **Music (Expanded Player)** | **NONE** | *UNSUPPORTED* | **NONE** | **Collapse to Compact** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Prev / Play-Pause / Next / Seek Scrubber** | **Collapse to Compact** |
| **Split: Notification + Music** | Main: **Open Music**; Companion: **Expand Notification** | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss Active Notification** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Ongoing Call (Compact)** | **Expand Call Sheet** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** (Call stays active) | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Ongoing Call (Expanded Sheet)** | **NONE** | *UNSUPPORTED* | **NONE** | **Collapse to Compact** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **End Call / Mute / Speaker / Video** | **Collapse to Compact** |
| **Incoming Call (Expanded Sheet)** | **NONE** | *UNSUPPORTED* | **NONE** | **Decline / Ignore Call** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Accept (Green) / Decline (Red)** | **NONE** (Ringing stays visible) |
| **Call Summary (Expanded Sheet)** | **NONE** | *UNSUPPORTED* | **NONE** | **Dismiss Summary** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Redial / Message / WhatsApp / Dismiss** | **Dismiss Summary** |
| **Charging / Low Battery (Compact)**| **NONE** (Transient confirmation) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss Immediately** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Ringer Mode (Compact)** | **Expand Ringer Sheet** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss Immediately** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Volume HUD (Media / Ringer)** | **NONE** (Transient HUD) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss Immediately** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Bluetooth Device (Compact)** | **NONE** (Transient banner) | *UNSUPPORTED* | **Open Quick Controls** | **Dismiss Immediately** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Quick Controls (Action Control)** | **NONE** | *UNSUPPORTED* | **NONE** | **Close Quick Controls** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Toggle Tiles / Sliders / App Chips** | **Close Quick Controls** |
| **Pro Access Expiry (Expanded)** | **NONE** | *UNSUPPORTED* | **NONE** | **Dismiss Alert** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Watch Ads / Buy Premium** | **Dismiss Alert** |
| **Live Activity: Timer (Compact)** | **Expand Timer** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Live Activity: Timer (Expanded)** | **NONE** | *UNSUPPORTED* | **NONE** | **Collapse to Compact** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Start-Pause / Reset Timer** | **Collapse to Compact** |
| **Live Activity: Delivery (Compact)** | **Expand Delivery** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Live Activity: Delivery (Expanded)**| **NONE** | *UNSUPPORTED* | **NONE** | **Collapse to Compact** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Call Delivery Partner** | **Collapse to Compact** |
| **Live Activity: Navigation (Compact)**| **Expand Navigation** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | N/A | **NONE** |
| **Live Activity: Navigation (Expanded)**| **NONE** | *UNSUPPORTED* | **NONE** | **Collapse to Compact** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | Direction steps / Maneuver preview | **Collapse to Compact** |
| **Live Activity: Voice Memo / Recording** | **Expand Recording** (`TOGGLE_EXPANSION`) | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Stop Recording Button** | **Collapse to Compact** |
| **Live Activity: Screen Mirroring Alert**| **Expand Alert** | *UNSUPPORTED* | **Open Quick Controls** | **Collapse to Minimal** | *UNSUPPORTED* | *UNSUPPORTED* | *UNSUPPORTED* | **Stop Casting / Mirroring** | **Collapse to Compact** |

---

## 3. Gesture Defect Analysis & Verification

### Defect 1: Double-Tap Tap-Through / Accidental Dismissal Leakage
- **Source Code:**
  - [`DynamicIslandPill.kt:869-889`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt#L869-L889): `combinedClickable` with `onClick = { onTap() }` on compact pill.
  - [`NotificationIsland.kt:281-296`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt#L281-L296): `onDismiss` close button placed at the top-right of the expanded card.
- **Suspected Failure Mechanism:**
  - Compose `combinedClickable` omits `onDoubleClick`. When a user rapidly taps twice on the right side of a compact notification:
    1. Tap 1 triggers `onTap()`, dispatching `OPEN_NOTIFICATIONS` and switching state to `isExpanded = true`.
    2. Compose immediately recomposes with `NotificationIslandExpanded`.
    3. Tap 2 lands within 100–200ms at the exact same screen coordinate.
    4. At that screen coordinate, the 50dp circular Dismiss button (`NotificationIsland.kt:284`) is now composed.
    5. Tap 2 hits the Dismiss button, calling `onNotificationDismiss` and deleting/canceling the notification immediately.
- **Exact Reproduction Steps:**
  1. Open the Playground and select `Compact` notification.
  2. Double-tap rapidly (two taps within 200ms) on the right side of the pill.
  3. Observe that the notification starts to expand and immediately disappears, returning to `Idle`.
- **Expected Behavior:** Double-tapping should expand the notification and ignore the second tap, or toggle expansion smoothly.
- **Actual Behavior:** The second tap executes the dismiss action on the newly rendered sheet.
- **Classification:** **CONFIRMED**.

---

### Defect 2: Long-Press Cancellation During Vertical Drag Micro-Drift
- **Source Code:**
  - [`DynamicIslandPill.kt:774-889`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt#L774-L889): `pointerInput` with `detectVerticalDragGestures` is chained before `combinedClickable`.
- **Suspected Failure Mechanism:**
  - `detectVerticalDragGestures` starts consuming pointer changes as soon as vertical displacement exceeds the system touch slop (~8dp / 18px).
  - A long-press requires holding the pointer for ~400ms (`ViewConfiguration.getLongPressTimeout()`).
  - If a user's finger micro-drifts vertically by >8dp during the 400ms hold, `detectVerticalDragGestures` consumes the change (`change.consume()`), which sends a cancel event to `combinedClickable`.
  - `onLongClick` is never executed; instead, the island translates vertically by a few pixels and snaps back.
- **Exact Reproduction Steps:**
  1. Touch down on the compact island to initiate a long-press.
  2. While holding down, gently slide the finger vertically upward by 10–15px (below the 40px dismiss threshold).
  3. Hold for 500ms and release.
- **Expected Behavior:** If the vertical drag does not reach the -40px dismissal threshold, a long-press hold should still open Quick Controls.
- **Actual Behavior:** `detectVerticalDragGestures` consumes the touch stream, canceling `combinedClickable`; Quick Controls never opens.
- **Classification:** **CONFIRMED**.

---

### Defect 3: Mid-Animation Touch Target Displacement
- **Source Code:**
  - [`DynamicIslandPill.kt:1001-1015`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt#L1001-L1015) (`AnimatedContent` inside `IslandContentViewport`) and `AppMotion.islandSpring()`.
- **Suspected Failure Mechanism:**
  - During the 350–450ms spring expansion from compact width (76dp) to expanded width (380dp), child buttons are actively translating and scaling.
  - Taps landing during frames 1–15 (0–250ms) can hit the pill container or adjacent buttons because their spatial hit-test rectangles are moving every frame.
- **Exact Reproduction Steps:**
  1. In the Animation Lab, set animation speed to `0.25x` (slowing the transition to ~1500ms).
  2. Select `ActiveCall` or `Music` and tap to expand.
  3. Attempt to tap an action button (e.g. End Call or Play/Pause) mid-expansion while it is moving across the screen.
- **Expected Behavior:** Interactive targets should either have stable pre-placed touch zones or interaction should be gated until the spring settles.
- **Actual Behavior:** Clicks land on the moving container or wrong buttons during transition.
- **Classification:** **CONFIRMED**.
