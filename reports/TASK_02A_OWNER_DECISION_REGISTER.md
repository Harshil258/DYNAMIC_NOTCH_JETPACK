# Task 02A — Android Feasibility, Owner Decision Register & Acceptance Test Scenarios

**Document:** `reports/TASK_02A_OWNER_DECISION_REGISTER.md`  
**Author:** Android Development Team  
**Reviewer & Examiner:** ChatGPT — Principal Product Architect and Final Examiner  
**Status:** Complete Architectural Register & Test Catalog  
**Code Modifications:** None (`0` lines in `app/src/main/`)

---

## PART 1: Android API Feasibility Assessment

### 1. Core Platform API Evaluation Matrix

| Android API / Component | Official Documentation / Class | Capability Classification | Technical Notes & Implementation Reality |
|---|---|:---:|---|
| **Notification Ingestion** | [`NotificationListenerService`](https://developer.android.com/reference/android/service/notification/NotificationListenerService) | **Supported with Public APIs** | Ingests `StatusBarNotification` via `onNotificationPosted` and `onNotificationRemoved`. Requires user to grant Notification Access. |
| **Notification Payload** | [`StatusBarNotification`](https://developer.android.com/reference/android/service/notification/StatusBarNotification) | **Supported with Public APIs** | Extracts title, text, subText, icons, actions, categories (`CATEGORY_MESSAGE`, `CATEGORY_CALL`), and progress extras. |
| **Inline Reply** | [`RemoteInput`](https://developer.android.com/reference/android/app/RemoteInput) | **Supported with Public APIs** | Dispatches free-form text input back to owning application via `RemoteInput.addResultsToIntent()` bundled into `action.actionIntent.send()`. |
| **Media Playback** | [`MediaSessionManager`](https://developer.android.com/reference/android/media/session/MediaSessionManager) | **Supported with Public APIs** | Controls playback (Play/Pause, Skip, Previous), track title, artist, and album art via `MediaController.TransportControls`. Requires Notification Access. |
| **Overlay Window** | [`WindowManager.LayoutParams`](https://developer.android.com/reference/android/view/WindowManager.LayoutParams) | **Supported with Public APIs** | Uses `TYPE_APPLICATION_OVERLAY`. Requires `SYSTEM_ALERT_WINDOW` permission ("Draw over other apps"). |
| **Pass-through Touches** | `FLAG_NOT_TOUCH_MODAL` & `FLAG_WATCH_OUTSIDE_TOUCH` | **Supported with Public APIs** | Allows touches inside island bounds to be intercepted while touches outside dispatch `MotionEvent.ACTION_OUTSIDE` to the overlay and continue down to background apps. |
| **Hardware Cutout** | [`DisplayCutout`](https://developer.android.com/reference/android/view/DisplayCutout) | **Supported with Public APIs** | Reads physical camera cutout bounding rectangles via `WindowInsets.getDisplayCutout()`. Exposes exact pixel coordinates and safe insets. |
| **Telephony Integration** | [`TelecomManager`](https://developer.android.com/reference/android/telecom/TelecomManager) / In-Call | **Permission / Role Restricted** | Answering/ending phone calls programmatically requires `ANSWER_PHONE_CALLS` permission or `Default Dialer` role. Fallback: Dispatches call notification's explicit `PendingIntent` actions. |
| **Gesture Navigation** | Jetpack Compose `HorizontalPager` | **Supported with Public APIs** | Off-the-shelf Foundation pager. Requires custom touch-slop arbitration to prevent conflicts with overlay vertical swipe-to-collapse. |
| **System Shade Status** | Notification dismissal / shade control | **OEM / Device Dependent** | Calling `cancelNotification(key)` removes item from system shade. Collapsing system status bar (`StatusBarManager.collapsePanels()`) requires undocumented hidden APIs. |

---

### 2. Answers to the 5 Mandatory Feasibility Questions

#### Question 1: Outside Tap — Can the overlay collapse while a tap on the underlying app still performs its intended action?
- **Classification:** **Supported with Public APIs.**
- **Technical Mechanism:** 
  The overlay window is configured with `WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL` combined with `FLAG_WATCH_OUTSIDE_TOUCH`. 
  When the user taps outside the expanded card, Android delivers a `MotionEvent.ACTION_OUTSIDE` to the island's root view. Simultaneously, Android dispatches the actual touch (`ACTION_DOWN` / `ACTION_UP`) directly to the focused window of the underlying application.
- **Verification Proof:** Confirmed on physical test device Realme 8 (Android 13). Tapping outside an expanded island collapses the card while immediately clicking the underlying app's button in a single touch.

#### Question 2: Auto-Collapse — Can a timer collapse the island without removing the notification, and how does it pause/reset during interactions?
- **Classification:** **Supported with Public APIs.**
- **Technical Mechanism:** 
  The auto-collapse timer is managed by a coroutine job (`Job`) in the presentation coordinator. 
  When the card expands, the job starts a 3000ms delay. 
  The root Compose surface wraps its content in a `Modifier.pointerInput` with `awaitPointerEventScope`. On `PointerEventType.Press`, the job is paused/canceled. On `PointerEventType.Release`, a fresh 3000ms countdown launches. 
  When the timer expires, it updates `IslandPresentationMode` from `TemporaryPreview` to `CompactCapsule`. The notification remains untouched in `IslandNotificationQueue`.
- **Verification Proof:** Demonstrated in unit test suite `NotificationConcurrencyAndLifecycleStressTest`.

#### Question 3: Horizontal Pager — Can horizontal swipes coexist with vertical collapse and system navigation gestures?
- **Classification:** **Requires Proof on Actual Devices (PROTOTYPE OBSERVED).**
- **Technical Mechanism:** 
  Compose's nested gesture system allows directional trajectory arbitration. 
  By evaluating touch angle $\theta = \arctan(|\Delta y| / |\Delta x|)$ on initial pointer displacement $> 8\text{dp}$, pointer consumption is routed to `HorizontalPager` when $\theta < 30^\circ$, or to the vertical collapse handler when $\theta > 60^\circ$ and $\Delta y < 0$. Diagonal touches defer consumption until displacement exceeds 16dp.
  System back gestures (swiping from screen edges) are avoided because the expanded island is centered horizontally with at least 11dp clearance from screen boundaries (`Outer Width = Screen Width - 22dp`).
- **Verification Proof:** Tested across Realme 8 gesture navigation; formal proof plan across curved thumb trajectories is scheduled for Task 03.

#### Question 4: Notification Replay — Can serialized notification fixtures exercise production logic without duplicating code?
- **Classification:** **Supported with Public APIs.**
- **Technical Mechanism:** 
  By using pure Kotlin `NotificationInfo` and replaying through the production [`NotificationReducer.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt), captured JSON fixtures exercise the exact same resolution pipeline used by live notifications.
- **Verification Proof:** Validated via automated tests `NotificationPlacementPolicyTest` and `NotificationVisibilityStateTest`.

#### Question 5: Calls and Media — Which actions can actually be executed through public APIs?
- **Classification:** **Capability-Dependent with Fallbacks.**
- **Technical Mechanism:** 
  - **Media Controls:** Supported via `MediaController.TransportControls` when an active `MediaSessionCompat` exists with valid `PlaybackState`. If transport controls are unresponsive, the system falls back to dispatching `KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE`.
  - **Phone Calls:** Rather than requesting dangerous `ANSWER_PHONE_CALLS` permissions or requiring the default phone app role, the island checks for explicit `PendingIntent` actions attached to the incoming call notification. If unavailable or blocked by keyguard, the system falls back to launching the carrier dialer intent.
- **Verification Proof:** Verified on WhatsApp and GSM cellular calls on Realme 8; capability check required before rendering action buttons.

---

## PART 2: Master Product Owner Decision Register

The following register organizes all pending product behavior choices into 8 thematic areas. Each decision is presented with concrete trade-offs, Apple reference behavior, Android feasibility, and a recommended default.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              DECISION REGISTER INDEX                                   │
├─────────┬────────────────────────────────────┬──────────┬──────────────────────────────┤
│ Area    │ Topic                              │ Dec Count│ Priority                     │
├─────────┼────────────────────────────────────┼──────────┼──────────────────────────────┤
│ Area A  │ Idle Island Behavior               │ 4 Decs   │ P0 (Essential Core UX)       │
│ Area B  │ Notification Arrival & Preview     │ 5 Decs   │ P0 (Essential Core UX)       │
│ Area C  │ Expanded Island Interactions       │ 5 Decs   │ P0 (Essential Core UX)       │
│ Area D  │ Multiple Notifications & Pager     │ 5 Decs   │ P1 (Differentiating Feature) │
│ Area E  │ Media Playback & Phone Calls       │ 4 Decs   │ P1 (Differentiating Feature) │
│ Area F  │ Live Activities & Background Tasks │ 4 Decs   │ P1 (Differentiating Feature) │
│ Area G  │ Visual Geometry & Motion Physics   │ 4 Decs   │ P2 (Polish & Refinement)     │
│ Area H  │ Privacy, Lockscreen & OEM Fallbacks│ 4 Decs   │ P2 (Polish & Refinement)     │
└─────────┴────────────────────────────────────┴──────────┴──────────────────────────────┘
```

---

### Area A: Idle Island Behavior

#### Decision `DEC-IDLE-01`: Idle Island Visibility & Form Factor
- **Scenario:** The device has no active media, no running timers, and no unread notifications.
- **Choices:**
  - **Choice 1 (Recommended):** Persistent Ambient Capsule. Always render a sleek black capsule around the hardware camera cutout to mimic Apple's continuous island look.
  - **Choice 2:** Dynamic Auto-Hide. Completely hide the overlay window when idle (`0dp x 0dp`), exposing the native screen cutout until an active event occurs.
  - **Choice 3:** User-Configurable Toggle in Settings (Default: Persistent Capsule).
- **Recommended Default:** **Choice 3** (Default: Persistent Capsule with Auto-Hide toggle).
- **Reason:** Apple enthusiasts prefer the persistent black pill identity, while Android purists often dislike static overlays that cover screen pixels during gaming or video viewing.
- **Current App Behavior:** Persistent capsule displayed by default; hide option exists in settings.
- **Apple Behavior:** `[APPLE OBSERVED]` On iPhone, the Dynamic Island is always physically present because it surrounds the OLED TrueDepth sensor.
- **Android Feasibility:** Fully supported via `WindowManager.updateViewLayout`.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-IDLE-02`: Single Tap on Idle Capsule
- **Scenario:** The user single-taps the persistent idle capsule when no background tasks are active.
- **Choices:**
  - **Choice 1 (Recommended):** Open Quick Action Controls. Expands a compact sheet with quick toggles (Flashlight, Screenshot, Volume, Settings).
  - **Choice 2:** No-Op (Haptic Bump Only). Capsule plays a subtle elastic bounce animation without opening any UI.
  - **Choice 3:** Open Host Application (Aurora Dynamic Island main screen).
- **Recommended Default:** **Choice 1** (Open Quick Action Controls).
- **Reason:** Transforms dead screen space into a high-utility control center, providing immediate tactile value.
- **Current App Behavior:** Expands Quick Controls card (`IslandState.ActionControl`).
- **Apple Behavior:** `[APPLE VERIFIED]` On iOS, tapping an idle Dynamic Island does nothing (or plays a subtle spring squeeze).
- **Android Feasibility:** Fully supported.
- **Dependencies:** `DEC-IDLE-01`.
- **Priority:** **P0**.

#### Decision `DEC-IDLE-03`: Long Press on Idle Capsule
- **Scenario:** The user presses and holds (400ms) on the idle capsule.
- **Choices:**
  - **Choice 1 (Recommended):** Open Settings Vault / Island Calibration screen.
  - **Choice 2:** Open Quick Action Controls.
  - **Choice 3:** Trigger Android Lock Screen / Turn Screen Off.
- **Recommended Default:** **Choice 1** (Open Settings Vault).
- **Reason:** Provides an instant, discoverable shortcut for users to calibrate notch alignment without hunting for the app icon in the launcher.
- **Current App Behavior:** Launches Settings Vault activity.
- **Apple Behavior:** `[APPLE VERIFIED]` No action on iOS.
- **Android Feasibility:** Fully supported via `context.startActivity()`.
- **Dependencies:** `DEC-IDLE-01`.
- **Priority:** **P1**.

#### Decision `DEC-IDLE-04`: Swipe Up on Idle Capsule
- **Scenario:** The user flicks upward on the persistent idle capsule.
- **Choices:**
  - **Choice 1 (Recommended):** Temporarily Hide Island. Collapses the island to `0dp` until the next notification or media event occurs.
  - **Choice 2:** Ignore / No-Op.
- **Recommended Default:** **Choice 1** (Temporarily Hide Island).
- **Reason:** Gives users an intuitive, physical gesture to dismiss the capsule when watching fullscreen content or playing games.
- **Current App Behavior:** Hides the island until next event.
- **Apple Behavior:** `[APPLE UNVERIFIED]` Not present on iOS.
- **Android Feasibility:** Fully supported via vertical drag listener.
- **Dependencies:** `DEC-IDLE-01`.
- **Priority:** **P1**.

---

### Area B: Notification Arrival & Preview

#### Decision `DEC-NOTIF-01`: Automatic Expansion on Notification Arrival
- **Scenario:** A new notification arrives while the user is using another application.
- **Choices:**
  - **Choice 1 (Recommended):** Temporary Expanded Preview. Island automatically morphs into an expanded preview card displaying sender, message, and actions.
  - **Choice 2:** Compact Capsule Only. Island expands only to a compact pill displaying the app icon and unread counter, requiring a touch-and-hold to read details.
  - **Choice 3:** Heads-Up Replacement. Replaces system heads-up banners with the island preview.
- **Recommended Default:** **Choice 1** (Temporary Expanded Preview).
- **Reason:** Delivers the signature "magical" Dynamic Island experience that Android users install the app to achieve.
- **Current App Behavior:** Automatically expands to full card.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple does NOT expand for normal notifications. However, for hardware alerts and incoming calls, it automatically expands temporarily.
- **Android Feasibility:** Fully supported via `IslandNotificationListener`.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-NOTIF-02`: Preview Display Duration Before Auto-Collapse
- **Scenario:** The island has automatically expanded to show an incoming notification preview.
- **Choices:**
  - **Choice 1:** Short Glance (2.0 seconds).
  - **Choice 2 (Recommended):** Standard Reading Window (3.0 seconds).
  - **Choice 3:** Extended Window (4.5 seconds).
- **Recommended Default:** **Choice 2** (3.0 seconds).
- **Reason:** 3.0 seconds provides adequate time for the human eye to parse a sender and one line of text without lingering long enough to obstruct foreground app tasks.
- **Current App Behavior:** Hardcoded ~3.5 seconds.
- **Apple Behavior:** `[APPLE OBSERVED]` System alerts expand for 2.0–3.0 seconds on iOS.
- **Android Feasibility:** Fully supported via coroutine delay.
- **Dependencies:** `DEC-NOTIF-01`.
- **Priority:** **P0**.

#### Decision `DEC-NOTIF-03`: Notification Priority & Silent Channel Filtering
- **Scenario:** A low-priority, ongoing, or silent notification arrives (e.g., weather sync, step counter, background sync).
- **Choices:**
  - **Choice 1 (Recommended):** Strict Filtering. Never expand the island for silent channels, low-priority channels, or ongoing background service notifications.
  - **Choice 2:** Show Silent Notifications in Compact Mode Only (no auto-expansion).
  - **Choice 3:** Ingest Everything.
- **Recommended Default:** **Choice 1** (Strict Filtering).
- **Reason:** Prevents annoying, spammy auto-expansions from background sync services.
- **Current App Behavior:** Partially filtered in `IslandNotificationListener`.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple requires explicit developer authorization for Live Activities; ambient background updates never interrupt.
- **Android Feasibility:** Fully supported by checking `sbn.notification.priority`, `sbn.isOngoing`, and channel importance.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-NOTIF-04`: Subsequent Notification Arrival During Active Preview
- **Scenario:** While the island is displaying Alice's message, Bob sends a Telegram message 1.0 second later.
- **Choices:**
  - **Choice 1 (Recommended):** Queue Insertion with Contextual Badge. Keep Alice's message visible, append Bob to the queue, update the counter badge to `1 of 2 · New from Bob`, and extend the auto-collapse timer by +2.0s.
  - **Choice 2:** Immediate Preemption. Abruptly morph the card to display Bob's message, replacing Alice immediately.
  - **Choice 3:** Ignore Bob until Alice collapses.
- **Recommended Default:** **Choice 1** (Queue Insertion with Contextual Badge).
- **Reason:** Prevents jarring text flashing and allows the user to finish reading Alice's message before paging to Bob.
- **Current App Behavior:** Replaces content immediately, causing visual flashing.
- **Apple Behavior:** `[APPLE UNVERIFIED]`
- **Android Feasibility:** Fully supported via `IslandNotificationQueue`.
- **Dependencies:** `DEC-NOTIF-01`, `DEC-NOTIF-02`.
- **Priority:** **P1**.

#### Decision `DEC-NOTIF-05`: Post-Collapse Destination: Compact Capsule vs. Hidden
- **Scenario:** The temporary 3.0s preview timer expires without user interaction.
- **Choices:**
  - **Choice 1 (Recommended):** Collapse to Compact Notification Capsule. Island shrinks to a 36.67dp pill showing the app icon and unread count (`"1"`).
  - **Choice 2:** Collapse to Idle Cutout Capsule (hide notification indicator).
  - **Choice 3:** Hide Completely.
- **Recommended Default:** **Choice 1** (Collapse to Compact Notification Capsule).
- **Reason:** Preserves awareness of the pending message at a glance, allowing the user to expand it later at their convenience.
- **Current App Behavior:** Collapses to compact pill.
- **Apple Behavior:** `[APPLE VERIFIED]` Live Activities stay in compact presentation until finished.
- **Android Feasibility:** Fully supported.
- **Dependencies:** `DEC-NOTIF-02`.
- **Priority:** **P0**.

---

### Area C: Expanded Island Interactions

#### Decision `DEC-EXP-01`: Outside Tap Behavior on Expanded Card
- **Scenario:** The user has an expanded card open (either preview or user-opened) and taps on the background screen outside the card bounds.
- **Choices:**
  - **Choice 1 (Recommended):** Collapse to Compact + Pass-Through Click. The card collapses to compact, while the user's tap immediately registers on the underlying app.
  - **Choice 2:** Collapse Only (Modal Block). The card collapses, but the underlying tap is consumed/blocked to prevent accidental clicks.
  - **Choice 3:** Dismiss and Evict from Queue.
- **Recommended Default:** **Choice 1** (Collapse to Compact + Pass-Through Click).
- **Reason:** Non-intrusive; matches native Android system dialog behavior and never frustrates the user by requiring a double tap to interact with their underlying app.
- **Current App Behavior:** Pass-through click with collapse.
- **Apple Behavior:** `[APPLE OBSERVED]` Tapping outside an expanded Live Activity collapses it back to compact without stealing touches.
- **Android Feasibility:** Fully supported via `FLAG_NOT_TOUCH_MODAL` and `FLAG_WATCH_OUTSIDE_TOUCH`.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-EXP-02`: Swipe-Up Gesture on Expanded Card
- **Scenario:** The user swipes upward on an expanded notification card.
- **Choices:**
  - **Choice 1 (Recommended):** Collapse to Compact. Shrinks the card back to the compact pill, preserving the notification in the queue.
  - **Choice 2:** Dismiss & Cancel Android Notification. Removes the notification from the island and cancels it from the Android system shade.
  - **Choice 3:** Remove from Island Queue Only. Removes from island, leaves in system shade.
- **Recommended Default:** **Choice 1** (Collapse to Compact).
- **Reason:** Eliminates accidental notification deletion. Swiping up is the natural physical inverse of pulling down; it should collapse the view, not destroy data.
- **Current App Behavior:** Currently executes destructive dismissal (Defect).
- **Apple Behavior:** `[APPLE OBSERVED]` Swiping up on an expanded presentation collapses it back to compact.
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-EXP-03`: Single Tap on Expanded Card Body
- **Scenario:** The user taps on the text body or background of an expanded notification card (not on an action button).
- **Choices:**
  - **Choice 1 (Recommended):** Launch Owning Application. Fires the notification's primary `contentIntent` and collapses the island.
  - **Choice 2:** No-Op (Action buttons only). Tapping background does nothing to prevent accidental launches.
- **Recommended Default:** **Choice 1** (Launch Owning Application).
- **Reason:** Standard mobile paradigm: tapping a notification card opens the corresponding chat or email.
- **Current App Behavior:** Launches owning app.
- **Apple Behavior:** `[APPLE VERIFIED]` Tapping an expanded Live Activity opens the parent app.
- **Android Feasibility:** Fully supported via `sbn.notification.contentIntent.send()`.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-EXP-04`: Close `(x)` Button Policy
- **Scenario:** The expanded card displays a subtle circular Close `(x)` button at the top right.
- **Choices:**
  - **Choice 1 (Recommended):** Evict from Island Queue Only. Removes the card from the island deck, but leaves the notification safe in the Android system shade.
  - **Choice 2:** Full Dismissal. Removes from island AND cancels the notification in the system shade.
  - **Choice 3:** Hide Close Button completely (rely exclusively on swipe gestures).
- **Recommended Default:** **Choice 1** (Evict from Island Queue Only).
- **Reason:** Protects user data integrity. If a user dismisses an island card, they can still review the notification later in their system shade.
- **Current App Behavior:** Evicts from island queue.
- **Apple Behavior:** `[APPLE UNVERIFIED]`
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-EXP-05`: Action Button Row Layout & Quota
- **Scenario:** An incoming notification contains 3 or 4 action intents (e.g., "Reply", "Mark Read", "Mute", "Delete").
- **Choices:**
  - **Choice 1 (Recommended):** Max 2 Primary Actions + Overflow. Render top 2 actions as sleek touch targets; additional actions placed in a `[...]` overflow menu.
  - **Choice 2:** Horizontal Scrollable Row for all actions.
  - **Choice 3:** Show only the first action (Reply).
- **Recommended Default:** **Choice 1** (Max 2 Primary Actions + Overflow).
- **Reason:** Prevents visual clutter, maintains concentric island margins, and guarantees touch targets meet the 48dp accessibility standard.
- **Current App Behavior:** Renders up to 3 buttons horizontally, which cramps text on small screens.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple HIG strongly advises limiting Live Activities to 1 or 2 essential direct actions.
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P1**.

---

### Area D: Multiple Notifications & Horizontal Pager

#### Decision `DEC-PAGER-01`: Enabling the Horizontal Notification Pager
- **Scenario:** More than one eligible notification is present in `IslandNotificationQueue`.
- **Choices:**
  - **Choice 1 (Recommended):** Enable Horizontal Paging. When expanded, user can swipe left/right between cards with fluid parallax animation.
  - **Choice 2:** Single Top Notification Only. Only show the highest priority item; user must dismiss it to see the next item.
  - **Choice 3:** Vertical Scroll List.
- **Recommended Default:** **Choice 1** (Enable Horizontal Paging).
- **Reason:** Flagship Android differentiator. Provides a delightfully fast way to triage messages without pulling down the giant system shade.
- **Current App Behavior:** Not implemented (shows static `+N` badge).
- **Apple Behavior:** `[APPLE VERIFIED]` Not present on iOS.
- **Android Feasibility:** Fully supported via Compose `HorizontalPager`.
- **Dependencies:** `DEC-NOTIF-01`.
- **Priority:** **P0**.

#### Decision `DEC-PAGER-02`: Maximum Number of Pageable Notifications
- **Scenario:** A user has 25 unread notifications in their Android system shade.
- **Choices:**
  - **Choice 1:** Cap at 3 items.
  - **Choice 2 (Recommended):** Cap at 5 items.
  - **Choice 3:** Cap at 10 items.
  - **Choice 4:** Unlimited (Page through all).
- **Recommended Default:** **Choice 2** (Cap at 5 items).
- **Reason:** 5 items provides excellent triage utility while bounding memory consumption, layout measurement overhead, and cognitive overload.
- **Current App Behavior:** N/A.
- **Apple Behavior:** `[APPLE UNVERIFIED]`
- **Android Feasibility:** Fully supported via queue sorting and `.take(5)`.
- **Dependencies:** `DEC-PAGER-01`.
- **Priority:** **P1**.

#### Decision `DEC-PAGER-03`: Pager Inactivity Auto-Collapse Duration
- **Scenario:** The user has expanded the multi-notification pager and is browsing cards, but stops interacting.
- **Choices:**
  - **Choice 1:** 8.0 seconds.
  - **Choice 2 (Recommended):** 15.0 seconds.
  - **Choice 3:** Never auto-collapse while pager is open (require explicit swipe-up or outside tap).
- **Recommended Default:** **Choice 2** (15.0 seconds).
- **Reason:** 15 seconds allows comfortable reading time without risking permanently obscuring foreground apps if the user leaves their phone on a desk.
- **Current App Behavior:** N/A.
- **Apple Behavior:** `[APPLE OBSERVED]` User-expanded Live Activities do not auto-collapse on iOS.
- **Android Feasibility:** Fully supported via coroutine delay reset on touch.
- **Dependencies:** `DEC-PAGER-01`.
- **Priority:** **P1**.

#### Decision `DEC-PAGER-04`: Current Page Eviction Animation & Flow
- **Scenario:** The user is on Page 2 of 4 and taps `[Mark as Read]`.
- **Choices:**
  - **Choice 1 (Recommended):** Slide Out + Snap to Next. Page 2 scales down and fades out; Page 3 smoothly slides into the active viewport position.
  - **Choice 2:** Instant snap without animation.
  - **Choice 3:** Collapse island immediately upon action.
- **Recommended Default:** **Choice 1** (Slide Out + Snap to Next).
- **Reason:** Keeps the user in their browsing flow so they can continue reading remaining messages.
- **Current App Behavior:** N/A.
- **Apple Behavior:** `[APPLE UNVERIFIED]`
- **Android Feasibility:** Fully supported via Compose `AnimatedContent` / `HorizontalPager`.
- **Dependencies:** `DEC-PAGER-01`.
- **Priority:** **P1**.

#### Decision `DEC-PAGER-05`: Compact Pill Representation for Multiple Notifications
- **Scenario:** The island collapses to compact mode while 4 notifications remain in the queue.
- **Choices:**
  - **Choice 1 (Recommended):** Primary Icon + Number Badge. Leading shows most recent app icon; trailing shows pill badge (`"+3"` or `"4"`).
  - **Choice 2:** Dual Stacked Icons. Leading shows 2 overlapping 16dp icons; trailing shows unread count.
  - **Choice 3:** Cycle through icons periodically (every 5 seconds).
- **Recommended Default:** **Choice 1** (Primary Icon + Number Badge).
- **Reason:** Cleanest visual presentation that respects the strict 36.67dp compact height and avoids visual noise.
- **Current App Behavior:** Displays primary icon + count badge.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple uses primary icon + status indicator in compact mode.
- **Android Feasibility:** Fully supported.
- **Dependencies:** `DEC-PAGER-01`.
- **Priority:** **P1**.

---

### Area E: Media Playback & Phone Calls

#### Decision `DEC-MEDIA-01`: Priority Between Active Music and Incoming Notifications
- **Scenario:** Spotify is currently playing (island in compact music mode). A WhatsApp message arrives.
- **Choices:**
  - **Choice 1 (Recommended):** Split Double Island. The compact pill hosts Music (album art + waveform); a detached circular bubble pops on the right hosting the WhatsApp icon for 4.0s.
  - **Choice 2:** Temporary Preemption. WhatsApp temporarily expands over the full island for 3.0s, then collapses back to compact Music.
  - **Choice 3:** Music takes 100% priority; notification stays in system shade.
- **Recommended Default:** **Choice 2** (Temporary Preemption for 3.0s, then restore Split or Music).
- **Reason:** Allows the user to read the urgent incoming message preview immediately, then seamlessly returns them to their ongoing music controls.
- **Current App Behavior:** Currently forces full expanded notification card.
- **Apple Behavior:** `[APPLE VERIFIED]` On iOS, when two activities run, Dynamic Island renders a compact pill (Activity 1) + detached circle (Activity 2).
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-MEDIA-02`: Single Tap on Compact Music Island
- **Scenario:** The island is displaying ongoing media playback (album art + audio visualizer).
- **Choices:**
  - **Choice 1 (Recommended):** Open Music Application (Spotify / YouTube Music).
  - **Choice 2:** Toggle Play / Pause in-place.
  - **Choice 3:** Expand to Full Music Player Card.
- **Recommended Default:** **Choice 1** (Open Music Application).
- **Reason:** Aligns with standard mobile conventions (Single Tap = Launch App; Long Press = Expand Controls).
- **Current App Behavior:** Configurable in settings (defaults to launch app).
- **Apple Behavior:** `[APPLE VERIFIED]` Single tap launches Now Playing app on iOS.
- **Android Feasibility:** Fully supported via `MediaSession.sessionActivity`.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-MEDIA-03`: Long Press on Compact Music Island
- **Scenario:** User touches and holds (400ms) on the compact music island.
- **Choices:**
  - **Choice 1 (Recommended):** Expand Interactive Media Card. Displays large album art, track title, artist, seek slider, and Play/Pause/Skip buttons.
  - **Choice 2:** No-Op.
- **Recommended Default:** **Choice 1** (Expand Interactive Media Card).
- **Reason:** Delivers the beloved Apple Dynamic Island expanded music experience.
- **Current App Behavior:** Expands music card (`IslandState.Music`).
- **Apple Behavior:** `[APPLE VERIFIED]` Touch and hold expands Now Playing controls on iOS.
- **Android Feasibility:** Fully supported via `MediaController.TransportControls`.
- **Dependencies:** `DEC-MEDIA-02`.
- **Priority:** **P0**.

#### Decision `DEC-MEDIA-04`: Incoming Call Preemption Policy
- **Scenario:** A phone call arrives (GSM or WhatsApp audio).
- **Choices:**
  - **Choice 1 (Recommended):** Absolute P0 Preemption. All notifications, media, and timers are immediately superseded. The island expands to the full Call Card (Caller Avatar, Name, Accept [Green], Decline [Red]).
  - **Choice 2:** Compact Pill with caller name scrolling.
- **Recommended Default:** **Choice 1** (Absolute P0 Preemption).
- **Reason:** A ringing phone call is the highest-urgency real-time event on a smartphone.
- **Current App Behavior:** Expands incoming call card.
- **Apple Behavior:** `[APPLE VERIFIED]` CallKit calls expand the Dynamic Island with Accept/Decline buttons.
- **Android Feasibility:** Fully supported via notification action PendingIntents.
- **Dependencies:** None.
- **Priority:** **P0**.

---

### Area F: Live Activities & Background Tasks

#### Decision `DEC-LIVE-01`: Timer & Stopwatch Presentation
- **Scenario:** User sets a countdown timer in the clock app.
- **Choices:**
  - **Choice 1 (Recommended):** Persistent Compact Timer with Real-Time Countdown. Displays orange timer glyph on leading side and countdown (`MM:SS`) on trailing side. Long press expands to pause/cancel.
  - **Choice 2:** Notification preview only.
- **Recommended Default:** **Choice 1** (Persistent Compact Timer with Real-Time Countdown).
- **Reason:** One of the most celebrated Apple Dynamic Island use cases.
- **Current App Behavior:** Implemented in `IslandState.Timer`.
- **Apple Behavior:** `[APPLE VERIFIED]` Native iOS Clock timer runs continuously in Dynamic Island.
- **Android Feasibility:** Supported via chronometer / timer notification parsing.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-LIVE-02`: Navigation Turn-by-Turn Maneuvers
- **Scenario:** Google Maps is actively navigating in background.
- **Choices:**
  - **Choice 1 (Recommended):** Compact Directional Arrow + Next Distance. When approaching a turn (<500m), island briefly expands to show street name and lane guidance, then collapses back to compact.
  - **Choice 2:** Continuous Expanded Navigation Card.
  - **Choice 3:** Exclude navigation from island (rely on Google Maps PIP).
- **Recommended Default:** **Choice 1** (Compact Directional Arrow + Proximity Auto-Expansion).
- **Reason:** Provides timely driving cues without blocking the user's screen continuously.
- **Current App Behavior:** Basic compact navigation pill.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple Maps displays turn maneuvers dynamically.
- **Android Feasibility:** Supported by parsing Google Maps persistent notification extras.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-LIVE-03`: Voice Recording & Microphone Indicator
- **Scenario:** A voice memo, call recorder, or WhatsApp audio note is recording in background.
- **Choices:**
  - **Choice 1 (Recommended):** Red Recording Dot + Audio Waveform. Compact pill shows blinking red dot and live audio visualizer. Long press expands to stop/pause.
  - **Choice 2:** Static icon only.
- **Recommended Default:** **Choice 1** (Red Recording Dot + Audio Waveform).
- **Reason:** Essential privacy transparency and instant recording controls.
- **Current App Behavior:** Implemented in `IslandState.Recording`.
- **Apple Behavior:** `[APPLE VERIFIED]` iOS Voice Memos renders active recording waveform in Dynamic Island.
- **Android Feasibility:** Supported via recording notification listeners.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-LIVE-04`: Multi-Activity Arbitration (More than 2 Active Tasks)
- **Scenario:** Spotify is playing, Timer is running, and a WhatsApp message arrives.
- **Choices:**
  - **Choice 1 (Recommended):** Two Visible + Remainder Queued. Main capsule shows Timer (Time-sensitive); Detached dot shows Music; WhatsApp displays temporary 3.0s preview over main capsule, then returns to Timer + Music.
  - **Choice 2:** Strict FIFO rotation.
- **Recommended Default:** **Choice 1** (Two Visible + Remainder Queued).
- **Reason:** Perfectly matches Apple's 2-activity hardware limit while gracefully handling transient alerts.
- **Current App Behavior:** Collapses into single combined state.
- **Apple Behavior:** `[APPLE VERIFIED]` Dynamic Island displays at most 2 activities simultaneously.
- **Android Feasibility:** Fully supported.
- **Dependencies:** `DEC-MEDIA-01`.
- **Priority:** **P1**.

---

### Area G: Visual Geometry & Motion Physics

#### Decision `DEC-VISUAL-01`: Compact Pill Baseline Height
- **Scenario:** Sizing the resting compact Dynamic Island pill.
- **Choices:**
  - **Choice 1 (Recommended):** 36.67dp. Exact Apple HIG token specification.
  - **Choice 2:** 40.0dp. Slightly larger for oversized Android camera punch-holes.
  - **Choice 3:** Dynamically match status bar height.
- **Recommended Default:** **Choice 1** (36.67dp).
- **Reason:** Strict compliance with [`docs/ISLAND_LAYOUT_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/ISLAND_LAYOUT_RULES.md) and Apple HIG token parity.
- **Current App Behavior:** Standardized to 36.67dp in recent refactor.
- **Apple Behavior:** `[APPLE VERIFIED]` Exact height is 36.67pt across iPhone 14/15/16 Pro.
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-VISUAL-02`: Expanded Card Maximum Height
- **Scenario:** Sizing the expanded notification and multi-notification pager card.
- **Choices:**
  - **Choice 1:** Cap at 160dp (Strict Apple HIG expanded ceiling).
  - **Choice 2 (Recommended):** Cap at 220dp with internal scrolling.
  - **Choice 3:** Dynamic Wrap Content (no cap).
- **Recommended Default:** **Choice 2** (Cap at 220dp with internal scrolling).
- **Reason:** Android notifications often include longer text and 2 rows of action buttons; 220dp accommodates these comfortably without occluding the bottom half of the phone.
- **Current App Behavior:** Varies from 140dp to 280dp.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple HIG specifies 84–160pt height range for expanded Live Activities.
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-VISUAL-03`: Standard Spring Motion Curves
- **Scenario:** Specifying the physics parameters for island expansion, collapse, and morphing.
- **Choices:**
  - **Choice 1 (Recommended):** Stiffness = 380f, Damping Ratio = 0.82f. (Balanced Apple-like fluid spring, ~380ms duration).
  - **Choice 2:** Snappy (Stiffness = 500f, Damping = 0.85f, ~250ms duration).
  - **Choice 3:** Linear Easing (300ms tween).
- **Recommended Default:** **Choice 1** (Stiffness = 380f, Damping = 0.82f).
- **Reason:** Established in [`docs/ISLAND_LAYOUT_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/ISLAND_LAYOUT_RULES.md); provides the signature organic bounce without feeling sluggish.
- **Current App Behavior:** Uses 380f / 0.82f in `IslandVisibilityTransitions.kt`.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple mandates fluid, interruptible spring animations.
- **Android Feasibility:** Fully supported via Compose `spring()`.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-VISUAL-04`: Reduced Motion Accessibility Compliance
- **Scenario:** The user has enabled "Remove animations" in Android Accessibility settings.
- **Choices:**
  - **Choice 1 (Recommended):** Instant Cut / Fast Fade (100ms alpha cross-fade, zero geometry spring scaling).
  - **Choice 2:** Retain gentle tween.
- **Recommended Default:** **Choice 1** (Instant Cut / Fast Fade).
- **Reason:** Mandatory accessibility compliance under [`docs/UI_UX_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/UI_UX_RULES.md) Rule 4.
- **Current App Behavior:** Supported via `LocalReducedMotion`.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple HIG mandates non-animated alternatives when Reduce Motion is enabled.
- **Android Feasibility:** Fully supported.
- **Dependencies:** None.
- **Priority:** **P2**.

---

### Area H: Privacy, Lockscreen & OEM Fallbacks

#### Decision `DEC-PRIV-01`: Lockscreen Notification Content Redaction
- **Scenario:** Device is locked (Keyguard active), and a private message arrives.
- **Choices:**
  - **Choice 1 (Recommended):** Redact Body & Actions. Show only app icon and "New Notification"; hide message body and sender until device is unlocked.
  - **Choice 2:** Hide Island Completely on Lockscreen.
  - **Choice 3:** Display Full Content (User opt-in only).
- **Recommended Default:** **Choice 1** (Redact Body & Actions).
- **Reason:** Essential privacy protection against shoulder surfing while preserving glanceable notification alerts.
- **Current App Behavior:** Basic redaction support.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple redacts notification content on Lock Screen until Face ID authenticates.
- **Android Feasibility:** Supported via `KeyguardManager.isKeyguardLocked()`.
- **Dependencies:** None.
- **Priority:** **P0**.

#### Decision `DEC-PRIV-02`: Sensitive Notification Blacklisting (Banking / OTPs)
- **Scenario:** A notification arrives containing a two-factor authentication code (OTP) or banking transaction.
- **Choices:**
  - **Choice 1 (Recommended):** Strict Category / Keyword Filter. Suppress auto-expansion for notifications flagged with `CATEGORY_PROMO`, banking apps, or text containing OTP/verification patterns.
  - **Choice 2:** Treat all notifications equally.
- **Recommended Default:** **Choice 1** (Strict Category / Keyword Filter).
- **Reason:** Prevents sensitive financial and security codes from being prominently projected in expanded floating windows.
- **Current App Behavior:** Basic blacklisting in settings.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple guidelines strictly prohibit sensitive information in Live Activities.
- **Android Feasibility:** Supported via Regex filter in `NotificationFilterPolicy`.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-PRIV-03`: Off-Center & Left Punch-Hole Camera Adaptation
- **Scenario:** The device has a left-corner camera punch-hole (e.g., OnePlus, older Realme, Samsung A-series) rather than a centered cutout.
- **Choices:**
  - **Choice 1 (Recommended):** Centered Floating Island with Cutout Offset Calibration. Maintain the Dynamic Island centered at the top of the display, calibrated to clear status bar icons.
  - **Choice 2:** Anchor Island to the Left Cutout (Asymmetric expansion).
  - **Choice 3:** User Choice in Settings Vault.
- **Recommended Default:** **Choice 1** (Centered Floating Island with Cutout Offset Calibration).
- **Reason:** Anchoring to the far left leaves the right side unbalanced and collides with left-aligned notification status icons. A centered island creates a harmonious, intentional UI anchor regardless of physical hole position.
- **Current App Behavior:** Centered island with notch calibration options.
- **Apple Behavior:** `[APPLE VERIFIED]` Apple devices feature centered TrueDepth cutouts.
- **Android Feasibility:** Fully supported via `IslandPlacementPolicy`.
- **Dependencies:** None.
- **Priority:** **P1**.

#### Decision `DEC-PRIV-04`: Overlay Behavior Over Secure / Fullscreen Apps (Banking / Camera)
- **Scenario:** The user opens a banking application or launches the camera.
- **Choices:**
  - **Choice 1 (Recommended):** Auto-Suppress Overlay. Island immediately hides when a secure or camera package becomes the foreground task.
  - **Choice 2:** Remain visible.
- **Recommended Default:** **Choice 1** (Auto-Suppress Overlay).
- **Reason:** Protects against Android security flags (`FLAG_SECURE`) and ensures the camera viewfinder is never occluded.
- **Current App Behavior:** Handled via app exclusion list.
- **Apple Behavior:** `[APPLE OBSERVED]` Island blends seamlessly into the native black status bar.
- **Android Feasibility:** Supported via accessibility service window change events or exclusion package tracking.
- **Dependencies:** None.
- **Priority:** **P2**.

---

## PART 3: 20 Concrete Acceptance Scenarios

The following 20 scenarios define end-to-end user journeys from trigger to lifecycle resolution. Every scenario specifies preconditions, inputs, expected states, animations, lifecycle outcomes, playground test setups, and automated pass/fail assertions.

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   20 ACCEPTANCE SCENARIOS INDEX                                  │
├─────┬──────────────────────────────────────────────────────┬────────────────┬────────────────────┤
│ #   │ Scenario Name                                        │ Domain Focus   │ Pending Decisions  │
├─────┼──────────────────────────────────────────────────────┼────────────────┼────────────────────┤
│ SC01│ Single Notification Arrival, Preview & Auto-Collapse │ Notification   │ NOTIF-01, NOTIF-02 │
│ SC02│ Expanded Notification Collapses After Outside Tap    │ Interaction    │ EXP-01             │
│ SC03│ Expanded Notification Collapses After Swipe Up       │ Gesture Policy │ EXP-02             │
│ SC04│ User Opens Compact Notification via Touch-and-Hold   │ Interaction    │ NOTIF-05, MEDIA-02 │
│ SC05│ Rapid Double-Tap During Expansion Transition         │ Defect Guard   │ VISUAL-03          │
│ SC06│ Long-Press with Minor Finger Movement (Slop Guard)   │ Defect Guard   │ VISUAL-03          │
│ SC07│ Music Playback Interrupted by Incoming Message       │ Multi-Activity │ MEDIA-01           │
│ SC08│ Five-Notification Horizontal Pager Browsing          │ Pager Engine   │ PAGER-01, PAGER-02 │
│ SC09│ New Notification Arrival While Pager is Open         │ Pager Engine   │ PAGER-01, NOTIF-04 │
│ SC10│ Current Pager Item Eviction via Action Button        │ Pager Engine   │ PAGER-04           │
│ SC11│ Incoming Phone Call Preempts Expanded Notification   │ Telephony      │ MEDIA-04           │
│ SC12│ Phone Call Terminates and Restores Previous Activity │ Telephony      │ MEDIA-04           │
│ SC13│ Notification Action Opens Focused Inline Reply       │ RemoteInput    │ EXP-05             │
│ SC14│ Outside Tap While Inline Reply Keyboard is Open      │ IME & Window   │ EXP-01             │
│ SC15│ Timer Completion Alert During Active Music Playback  │ Live Activity  │ LIVE-01, MEDIA-01  │
│ SC16│ Source Notification Content In-Place Update          │ Data Pipeline  │ NOTIF-04           │
│ SC17│ External Notification Removal from System Shade      │ Lifecycle      │ EXP-04             │
│ SC18│ High-Frequency Notification Burst Stress Test        │ Concurrency    │ NOTIF-04           │
│ SC19│ Device Screen Off / Lock / Unlock Cycle              │ System State   │ PRIV-01            │
│ SC20│ Hardware Cutout Offset & Re-calibration              │ Geometry       │ PRIV-03, VISUAL-01 │
└─────┴──────────────────────────────────────────────────────┴────────────────┴────────────────────┘
```

---

### Scenario SC01: Single Notification Arrival, Preview & Auto-Collapse
- **Preconditions:** Island is in `Idle` mode (`None`, `CompactCapsule`, `Resting`).
- **Input Events:** Ingest WhatsApp message (`key="wa_01"`, sender="Alice", message="Hello").
- **Expected States:** 
  1. $t=0$: Transitions to `TemporaryPreview` (width=371dp, height=140dp).
  2. $t=3.0\text{s}$: Transitions to `CompactCapsule` (width=210dp, height=36.67dp).
- **Animation Expectations:** Spring curve (stiffness=380f, damping=0.82f) morphing smoothly from compact to preview and back to compact. Zero abrupt size jumps.
- **Action Outcomes:** No intent dispatched.
- **Notification Lifecycle Effects:** "wa_01" remains active in `IslandNotificationQueue`. System shade notification untouched.
- **Playground Setup:** `PlaygroundCase.Compact` or replay fixture `wa_message.json`.
- **Automated Test Opportunities:** Unit test asserting state flow after 3000ms delay; Compose layout test asserting bounds at $t=1.5\text{s}$ and $t=3.5\text{s}$.
- **Pass/Fail Conditions:** PASS if preview displays for exactly 3000ms $\pm 100\text{ms}$ and settles to 36.67dp compact pill with unread badge "1".
- **Pending Decision IDs:** `DEC-NOTIF-01`, `DEC-NOTIF-02`, `DEC-NOTIF-05`.

---

### Scenario SC02: Expanded Notification Collapses After Outside Tap
- **Preconditions:** Notification preview is expanded on screen (`UserExpandedCard`).
- **Input Events:** Inject `MotionEvent.ACTION_OUTSIDE` at coordinate $(x=10, y=800)$ outside island bounds.
- **Expected States:** Island immediately transitions from `UserExpandedCard` to `CompactCapsule`.
- **Animation Expectations:** 380f spring collapse over ~380ms.
- **Action Outcomes:** Outside tap event is passed through to the underlying application; no intent dispatched from island.
- **Notification Lifecycle Effects:** Notification remains in `IslandNotificationQueue`.
- **Playground Setup:** Opt-in overlay mode enabled on test device.
- **Automated Test Opportunities:** Instrumented test checking `WindowManager` outside touch listener.
- **Pass/Fail Conditions:** PASS if island collapses to compact and underlying app receives click in $\le 50\text{ms}$.
- **Pending Decision IDs:** `DEC-EXP-01`.

---

### Scenario SC03: Expanded Notification Collapses After Swipe Up
- **Preconditions:** Notification card is expanded on screen (`UserExpandedCard`).
- **Input Events:** User performs vertical upward drag: $\Delta y = -40\text{dp}$ over 150ms.
- **Expected States:** Transitions from `UserExpandedCard` to `CompactCapsule`.
- **Animation Expectations:** Fluid collapse animation following finger velocity.
- **Action Outcomes:** No cancel intent sent.
- **Notification Lifecycle Effects:** Notification is **preserved** in `IslandNotificationQueue`. Notification in Android system shade is **NOT canceled**.
- **Playground Setup:** `PlaygroundCase.Expanded`.
- **Automated Test Opportunities:** Compose gesture test injecting upward swipe and asserting queue count $>0$.
- **Pass/Fail Conditions:** PASS if notification remains in queue and system shade after upward swipe.
- **Pending Decision IDs:** `DEC-EXP-02`.

---

### Scenario SC04: User Opens Compact Notification via Touch-and-Hold
- **Preconditions:** Island is in `CompactCapsule` mode displaying a WhatsApp unread badge (`"1"`).
- **Input Events:** User presses and holds the pill for 450ms.
- **Expected States:** Transitions from `CompactCapsule` to `UserExpandedCard`.
- **Animation Expectations:** Haptic tick at 400ms followed by fluid outward spring expansion.
- **Action Outcomes:** Auto-collapse timer is **disabled**. Card remains open indefinitely.
- **Notification Lifecycle Effects:** Queue unchanged.
- **Playground Setup:** `PlaygroundCase.Compact`.
- **Automated Test Opportunities:** Compose `performTouchInput { down(); advanceEventTime(450); up() }`.
- **Pass/Fail Conditions:** PASS if island expands and remains expanded $>10$ seconds without collapsing.
- **Pending Decision IDs:** `DEC-NOTIF-05`, `DEC-EXP-01`.

---

### Scenario SC05: Rapid Double-Tap During Expansion Transition
- **Preconditions:** Island is in `CompactCapsule` mode.
- **Input Events:** User taps rapidly twice on the right edge: Tap 1 at $t=0$, Tap 2 at $t=120\text{ms}$.
- **Expected States:** Island expands to `UserExpandedCard`. Tap 2 is **safely debounced** and consumed as a no-op.
- **Animation Expectations:** Continuous spring expansion without hitching.
- **Action Outcomes:** Close `(x)` button at top-right does **NOT** fire.
- **Notification Lifecycle Effects:** Notification remains safely displayed and queued.
- **Playground Setup:** `PlaygroundCase.Compact`.
- **Automated Test Opportunities:** Compose test dispatching two clicks separated by 120ms at $(x=160\text{dp}, y=18\text{dp})$.
- **Pass/Fail Conditions:** PASS if notification is NOT dismissed and card remains expanded.
- **Pending Decision IDs:** `DEC-VISUAL-03`.

---

### Scenario SC06: Long-Press with Minor Finger Movement (Slop Guard)
- **Preconditions:** Island is in `CompactCapsule` mode.
- **Input Events:** User touches down, moves finger vertically by $+10\text{dp}$ (exceeding 8dp touch slop), and holds for 500ms before lifting.
- **Expected States:** Island transitions to `UserExpandedCard`. Long-press recognizer does **NOT** abort.
- **Animation Expectations:** Haptic feedback at 400ms followed by spring expansion.
- **Action Outcomes:** Successful expansion.
- **Notification Lifecycle Effects:** Queue unchanged.
- **Playground Setup:** `PlaygroundCase.Compact`.
- **Automated Test Opportunities:** Compose touch test injecting $(0,0) \to (0, 10\text{dp})$ over 500ms.
- **Pass/Fail Conditions:** PASS if island successfully expands despite minor vertical finger drift.
- **Pending Decision IDs:** `DEC-VISUAL-03`.

---

### Scenario SC07: Music Playback Interrupted by Incoming Message
- **Preconditions:** Spotify is playing (Island in compact `Music` mode: album art + waveform).
- **Input Events:** Ingest Telegram message from "David".
- **Expected States:**
  1. $t=0$: Island morphs to `TemporaryPreview` displaying Telegram message for 3.0s.
  2. $t=3.0\text{s}$: Island auto-collapses to `SplitDoubleIsland` (Music in main pill, Telegram in detached minimal circle).
- **Animation Expectations:** Spring morphing from pill to expanded card, then split into primary pill + companion dot.
- **Action Outcomes:** Music playback continues uninterrupted in background.
- **Notification Lifecycle Effects:** Telegram added to queue.
- **Playground Setup:** `PlaygroundCase.Multiple` or `PlaygroundCase.Music`.
- **Automated Test Opportunities:** Instrumented test checking `SplitDoubleIsland` composition.
- **Pass/Fail Conditions:** PASS if music continues playing and both activities are visible at $t=3.5\text{s}$.
- **Pending Decision IDs:** `DEC-MEDIA-01`, `DEC-LIVE-04`.

---

### Scenario SC08: Five-Notification Horizontal Pager Browsing
- **Preconditions:** 5 notifications in queue (WhatsApp, Gmail, Telegram, Amazon, Calendar). Island is expanded in `UserExpandedCard` mode.
- **Input Events:** User swipes left across the card: $\Delta x = -120\text{dp}$ over 200ms.
- **Expected States:** Pager smoothly scrolls from Page 1 (WhatsApp) to Page 2 (Gmail). Header updates: `2 of 5`.
- **Animation Expectations:** Snapping spring physics (damping=0.85f) with 15% parallax text transition.
- **Action Outcomes:** Viewport locks onto Gmail card.
- **Notification Lifecycle Effects:** Active cursor index updates $0 \to 1$.
- **Playground Setup:** Mock multi-notification queue with 5 items.
- **Automated Test Opportunities:** Compose pager test asserting `pagerState.currentPage == 1`.
- **Pass/Fail Conditions:** PASS if Page 2 snaps into place cleanly without vertical jitter or collapse.
- **Pending Decision IDs:** `DEC-PAGER-01`, `DEC-PAGER-02`.

---

### Scenario SC09: New Notification Arrival While Pager is Open
- **Preconditions:** Pager is open on Page 2 (Gmail) of 5. User is actively looking at the card.
- **Input Events:** Ingest Slack notification from "Manager".
- **Expected States:** Active page remains pinned to Gmail. Header updates: `2 of 6`. Subtle bottom banner appears: `New from Manager`.
- **Animation Expectations:** Counter animates smoothly; zero scroll offset jumping.
- **Action Outcomes:** No displacement of user's active reading context.
- **Notification Lifecycle Effects:** Slack inserted into queue based on priority weighting.
- **Playground Setup:** Pager active at index 1; trigger dynamic event insertion.
- **Automated Test Opportunities:** Inject payload while asserting `pagerState.currentPage == 1`.
- **Pass/Fail Conditions:** PASS if active page does not change and counter increments to 6.
- **Pending Decision IDs:** `DEC-PAGER-01`, `DEC-NOTIF-04`.

---

### Scenario SC10: Current Pager Item Eviction via Action Button
- **Preconditions:** Pager open on Page 1 (WhatsApp) of 4.
- **Input Events:** User taps `[Mark as Read]` action button.
- **Expected States:** WhatsApp card scales down and fades out (200ms). Page 2 (Gmail) slides into the primary focus position. Header updates: `1 of 3`.
- **Animation Expectations:** Slide-and-fade exit transition.
- **Action Outcomes:** `actionIntent.send()` fires for WhatsApp.
- **Notification Lifecycle Effects:** WhatsApp evicted from `IslandNotificationQueue`.
- **Playground Setup:** Pager with 4 items.
- **Automated Test Opportunities:** Assert queue count decreases from 4 to 3 and active item becomes Gmail.
- **Pass/Fail Conditions:** PASS if WhatsApp is evicted, action is dispatched, and Gmail takes focus smoothly.
- **Pending Decision IDs:** `DEC-PAGER-04`, `DEC-EXP-04`.

---

### Scenario SC11: Incoming Phone Call Preempts Expanded Notification
- **Preconditions:** User is reading an expanded WhatsApp notification card.
- **Input Events:** Incoming cellular call from "Mom" (`CATEGORY_CALL`, ringing state).
- **Expected States:** Immediate P0 preemption. WhatsApp card is instantly replaced by `IncomingCall` expanded presentation (Mom's contact card, [Decline], [Accept]).
- **Animation Expectations:** Fast, fluid morphing transition (<200ms).
- **Action Outcomes:** Call ringtone plays.
- **Notification Lifecycle Effects:** WhatsApp state is saved into `SuspendedPagerState`; zero data lost.
- **Playground Setup:** Expanded notification active; trigger `PlaygroundCase.IncomingCall`.
- **Automated Test Opportunities:** Assert call state preempts notification within 50ms.
- **Pass/Fail Conditions:** PASS if call UI appears instantly and WhatsApp is preserved in memory.
- **Pending Decision IDs:** `DEC-MEDIA-04`.

---

### Scenario SC12: Phone Call Terminates and Restores Previous Activity
- **Preconditions:** Active call just ended; previous WhatsApp notification was preserved in `SuspendedPagerState`.
- **Input Events:** Telephony state transitions to `IDLE` / call disconnected.
- **Expected States:** Island morphs smoothly from Call card back to `CompactCapsule` displaying the WhatsApp unread badge.
- **Animation Expectations:** Smooth collapse transition (380ms).
- **Action Outcomes:** Call window teardown.
- **Notification Lifecycle Effects:** WhatsApp restored as active island item.
- **Playground Setup:** Call active with suspended state; trigger call end.
- **Automated Test Opportunities:** Verify restoration of suspended state upon call finish.
- **Pass/Fail Conditions:** PASS if island returns to compact WhatsApp badge with zero crashes.
- **Pending Decision IDs:** `DEC-MEDIA-04`.

---

### Scenario SC13: Notification Action Opens Focused Inline Reply
- **Preconditions:** Expanded WhatsApp notification card is visible.
- **Input Events:** User taps `[Reply]` action button.
- **Expected States:** Card expands height to 200dp. `BasicTextField` gains focus. Android software keyboard (IME) displays. Auto-collapse timer is **disabled**.
- **Animation Expectations:** Height animation accommodates text field; keyboard slides up smoothly.
- **Action Outcomes:** Focus requested on `FocusRequester`.
- **Notification Lifecycle Effects:** Pager gestures locked while typing.
- **Playground Setup:** `PlaygroundCase.Expanded`.
- **Automated Test Opportunities:** Compose test asserting text field is focused and IME inset is respected.
- **Pass/Fail Conditions:** PASS if keyboard appears and card does not auto-collapse while typing.
- **Pending Decision IDs:** `DEC-EXP-05`.

---

### Scenario SC14: Outside Tap While Inline Reply Keyboard is Open
- **Preconditions:** User has inline reply text field open with keyboard active.
- **Input Events:** User taps outside the island overlay.
- **Expected States:** Keyboard hides. Island collapses to `CompactCapsule`. Typed text draft is preserved in memory.
- **Animation Expectations:** Smooth keyboard exit and pill collapse.
- **Action Outcomes:** Keyboard dismissed via `SoftwareKeyboardController.hide()`.
- **Notification Lifecycle Effects:** Notification preserved; draft retained if user re-expands within 60s.
- **Playground Setup:** Inline reply active.
- **Automated Test Opportunities:** Assert keyboard dismissal on outside tap.
- **Pass/Fail Conditions:** PASS if keyboard hides and island collapses cleanly.
- **Pending Decision IDs:** `DEC-EXP-01`.

---

### Scenario SC15: Timer Completion Alert During Active Music Playback
- **Preconditions:** Spotify is playing in compact mode. Background timer reaches `00:00`.
- **Input Events:** Timer expiration event posted (`CATEGORY_ALARM`).
- **Expected States:** Island expands into an animated Orange Timer Completion Card (pulsing icon, "Timer Done", [Dismiss]).
- **Animation Expectations:** Attention-grabbing elastic bounce.
- **Action Outcomes:** System alarm haptic vibration plays.
- **Notification Lifecycle Effects:** When user taps [Dismiss], island collapses back to compact Music.
- **Playground Setup:** `PlaygroundCase.Music` active; fire timer alert.
- **Automated Test Opportunities:** Verify priority preemption of timer over media.
- **Pass/Fail Conditions:** PASS if timer alert displays prominently and returns to music on dismiss.
- **Pending Decision IDs:** `DEC-LIVE-01`, `DEC-MEDIA-01`.

---

### Scenario SC16: Source Notification Content In-Place Update
- **Preconditions:** Amazon delivery card is open on screen showing *"Arriving in 15 mins (2 stops away)"*.
- **Input Events:** Amazon app posts updated notification: *"Arriving now (Driver at door)"*.
- **Expected States:** Card height and container remain stable. Text label cross-fades smoothly to new message.
- **Animation Expectations:** 150ms cross-fade; zero layout jump.
- **Action Outcomes:** No scroll displacement.
- **Notification Lifecycle Effects:** `StableKey` matches; payload updated in-place in queue.
- **Playground Setup:** Progress card active; inject updated payload with matching key.
- **Automated Test Opportunities:** Assert text change occurs without triggering card re-expansion.
- **Pass/Fail Conditions:** PASS if text updates in-place smoothly without layout flash.
- **Pending Decision IDs:** `DEC-NOTIF-04`.

---

### Scenario SC17: External Notification Removal from System Shade
- **Preconditions:** User has 3 notifications in island. User pulls down native Android notification shade and swipes away Gmail.
- **Input Events:** `IslandNotificationListener.onNotificationRemoved` fires for Gmail key.
- **Expected States:** Gmail is evicted from `IslandNotificationQueue`. Total count updates `3 \to 2`. Compact badge decrements.
- **Animation Expectations:** Smooth number transition on badge.
- **Action Outcomes:** Clean eviction.
- **Notification Lifecycle Effects:** Island state stays perfectly synchronized with system shade.
- **Playground Setup:** Multi-notification active; trigger removal of secondary item.
- **Automated Test Opportunities:** Assert queue count decreases and remaining items shift correctly.
- **Pass/Fail Conditions:** PASS if item is evicted with zero crashes or orphan references.
- **Pending Decision IDs:** `DEC-EXP-04`.

---

### Scenario SC18: High-Frequency Notification Burst Stress Test
- **Preconditions:** Island is in `Idle` mode.
- **Input Events:** Inject 30 synthetic notifications across 10 different applications within 1500ms (50ms interval).
- **Expected States:** Island state machine serializes updates without crashing. The first notification expands; subsequent notifications are queued silently without visual stutter or OOM.
- **Animation Expectations:** Stable frame rate ($\ge 60\text{fps}$); zero dropped frames or unbounded recompositions.
- **Action Outcomes:** Queue caps at maximum capacity (e.g., 5 or 10 items).
- **Notification Lifecycle Effects:** Lowest priority / oldest items evicted gracefully.
- **Playground Setup:** `playground_burstStressTest` in `DynamicIslandPlaygroundTest`.
- **Automated Test Opportunities:** Automated stress test executing 30 rapid state transitions.
- **Pass/Fail Conditions:** PASS if 30 events complete with 0 unhandled exceptions and memory remains bounded.
- **Pending Decision IDs:** `DEC-NOTIF-04`, `DEC-PAGER-02`.

---

### Scenario SC19: Device Screen Off / Lock / Unlock Cycle
- **Preconditions:** Island is displaying compact media or notification capsule.
- **Input Events:** 
  1. Power button pressed (`ACTION_SCREEN_OFF`).
  2. Device locked (`isKeyguardLocked() == true`).
  3. Screen turns on (`ACTION_SCREEN_ON`).
  4. Device unlocked via fingerprint.
- **Expected States:**
  - Screen Off: Island overlay view set to `GONE`; touch listeners detached.
  - Locked: Redacted summary mode (or hidden).
  - Unlocked: Restores full compact capsule immediately.
- **Animation Expectations:** Clean fade-in on unlock; zero flickering.
- **Action Outcomes:** System resources released while screen is off.
- **Notification Lifecycle Effects:** State preserved across lock cycle.
- **Playground Setup:** Physical test device lock/unlock cycle.
- **Automated Test Opportunities:** Lifecycle state machine unit test.
- **Pass/Fail Conditions:** PASS if overlay does not draw while screen is off and restores properly upon unlock.
- **Pending Decision IDs:** `DEC-PRIV-01`.

---

### Scenario SC20: Hardware Cutout Offset & Re-calibration
- **Preconditions:** Device has an offset camera cutout (e.g. left punch-hole or sub-pixel misalignment).
- **Input Events:** User adjusts notch horizontal offset slider in Settings Vault from $0\text{dp} \to -24\text{dp}$.
- **Expected States:** Island pill shifts horizontally in real time, centering precisely over the physical camera lens.
- **Animation Expectations:** Real-time 60fps tracking without lag.
- **Action Outcomes:** Offset saved to persistent DataStore.
- **Notification Lifecycle Effects:** All future compact and expanded presentations respect the new horizontal anchor coordinate.
- **Playground Setup:** Geometry Lab sliders in `DynamicIslandPlayground`.
- **Automated Test Opportunities:** Instrumented test checking window layout params update on calibration change.
- **Pass/Fail Conditions:** PASS if overlay repositioning updates smoothly and persists across service restarts.
- **Pending Decision IDs:** `DEC-PRIV-03`, `DEC-VISUAL-01`.
