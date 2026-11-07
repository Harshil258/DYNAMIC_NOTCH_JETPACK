# Task 02B — Definitive Product Owner Decision Register (Ready for Review)

**Document:** `reports/TASK_02B_OWNER_DECISIONS_READY.md`  
**Author:** Android Development Team  
**Reviewer & Final Examiner:** ChatGPT — Principal Product Architect  
**Status:** Verified, Validated & Ready for Owner Consultation  
**Total Decision Count:** Exactly **35 Distinct Decisions**  
**Code Modifications:** None (`0` lines in `app/src/main/`)

---

## 1. Owner High-Level Mandates & Decision Taxonomy

### The 3 Owner-Approved Architectural Mandates:
The product owner has established three non-negotiable core pillars:
1. **Mandate 1 (Temporary Expansion):** Incoming eligible notifications must automatically expand into a temporary preview card and then automatically collapse back to compact form.
2. **Mandate 2 (Safe Non-Destructive Collapse):** Tapping outside the expanded notification card or swiping upward on it must collapse the island back to compact presentation **without canceling the notification** in the Android system shade.
3. **Mandate 3 (Horizontal Pagination):** When multiple eligible notifications are pending, the expanded island must support horizontal paging across cards.

*Note: While the above 3 principles are approved as high-level product requirements, all implementation parameters (exact timeout seconds, queue eviction mechanics, pagination quotas, gesture thresholds) remain subject to owner review below.*

### The 5-Tier Decision Status Taxonomy:
Every decision in this register is classified under one of five strict statuses:
- **`[OWNER_APPROVED]`**: Formally mandated by the product owner as a fixed requirement.
- **`[RECOMMENDED_PENDING]`**: Proposed by the engineering/design team with a default choice awaiting owner approval.
- **`[TECHNICAL_VERIFICATION_REQUIRED]`**: Dependent on hardware or OS constraints requiring physical proof before final sign-off.
- **`[OWNER_REJECTED]`**: Explicitly reviewed and declined by the product owner.
- **`[SUPERSEDED]`**: Replaced or rendered obsolete by a prior architectural decision.

---

## 2. Priority & Dependency Overview

The 35 decisions are organized into 3 sequential review phases:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              DECISION PHASING & PRIORITIES                             │
├─────────┬──────────────────────┬──────────────┬────────────────────────────────────────┤
│ Phase   │ Priority Tier        │ Count        │ Focus Area                             │
├─────────┼──────────────────────┼──────────────┼────────────────────────────────────────┤
│ Phase 1 │ Priority P0 (Blocker)│ 12 Decisions │ Core Idle, Notification, & Gesture UX  │
│ Phase 2 │ Priority P1 (High)   │ 15 Decisions │ Multi-Notification Pager, Media, Live  │
│ Phase 3 │ Priority P2 (Medium) │ 8 Decisions  │ Visual Tuning, Motion, Privacy & Sec   │
└─────────┴──────────────────────┴──────────────┴────────────────────────────────────────┘
```

---

## Phase 1: Core Product UX (Priority P0)

### Group 1: Idle Island Mechanics

#### Decision `DEC-IDLE-01`: Idle Island Form Factor & Ambient Presence
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The phone is unlocked on the home screen. There are no running media sessions, active timers, or unread notifications.
- **Available Choices:**
  - **Choice A (Recommended):** Persistent Ambient Capsule. Render a sleek black capsule around the camera cutout, matching the iconic Dynamic Island physical identity.
  - **Choice B:** Dynamic Auto-Hide. Completely hide the overlay window (`0dp x 0dp`) when idle, exposing the native screen cutout until an active event occurs.
  - **Choice C:** Persistent Capsule with User Toggle in Settings (Default: Visible).
- **Recommended Default:** **Choice C** (Persistent Capsule by default, toggleable in settings).
- **Reason:** Apple fans expect the continuous island presence, while power users and mobile gamers value the ability to auto-hide during full-screen apps.
- **Impact on Related Decisions:** Prerequisite for all other `DEC-IDLE` interactions.
- **Testable Acceptance Criteria:** If Choice C is selected, island renders at 36.67dp height when idle; toggling auto-hide in settings collapses window to 0dp.

#### Decision `DEC-IDLE-02`: Single Tap on Idle Capsule
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The user single-taps the resting idle capsule when no background tasks are active.
- **Available Choices:**
  - **Choice A (Current Production):** No-Op (Haptic Bump Only). The capsule plays a subtle elastic bounce animation without opening any UI.
  - **Choice B (Recommended):** Open Quick Action Controls (`IslandState.ActionControl`). Expands a compact sheet with quick toggles (Flashlight, Screenshot, Volume, Settings).
  - **Choice C:** Open Host Application (Aurora Dynamic Island main screen).
- **Recommended Default:** **Choice B** (Open Quick Action Controls).
- **Reason:** Transforms passive black cutout pixels into an instant high-utility control center.
- **Impact on Related Decisions:** Governs `DEC-IDLE-03`.
- **Testable Acceptance Criteria:** Single tap on idle pill transitions to `IslandState.ActionControl` within 200ms.

#### Decision `DEC-IDLE-03`: Long Press on Idle Capsule
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The user touches and holds (400ms) on the resting idle capsule.
- **Available Choices:**
  - **Choice A (Current Production):** Open Quick Action Controls (`IslandInteractionAction.OPEN_QUICK_CONTROLS`).
  - **Choice B (Recommended):** Open Settings Vault / Notch Calibration Screen.
  - **Choice C:** Turn Screen Off / Trigger Keyguard.
- **Recommended Default:** **Choice B** (Open Settings Vault / Calibration).
- **Reason:** Provides an instant shortcut for users to calibrate notch alignment without hunting for the app icon in the launcher.
- **Impact on Related Decisions:** Complements `DEC-IDLE-02`.
- **Testable Acceptance Criteria:** 400ms hold on idle pill triggers haptic vibration and launches Settings Vault activity.

---

### Group 2: Notification Arrival & Lifecycle

#### Decision `DEC-NOTIF-01`: Automatic Expansion on Notification Arrival
- **Status:** `[OWNER_APPROVED]` (Requirement Approved; Parameter Tuning Pending)
- **Concrete Scenario:** A new messaging or delivery notification arrives while the user is using another app.
- **Available Choices:**
  - **Choice A (Owner Mandated):** Temporary Expanded Preview. Automatically morphs into an expanded preview card displaying sender, message, and actions.
  - **Choice B:** Compact Capsule Badge Only (Never auto-expand; require user tap).
- **Recommended Default:** **Choice A** (Temporary Expanded Preview).
- **Reason:** Delivers the primary value proposition of the Dynamic Island experience.
- **Impact on Related Decisions:** Governs `DEC-NOTIF-02`, `DEC-NOTIF-05`, `DEC-EXP-01`.
- **Testable Acceptance Criteria:** Incoming SBN triggers transition from `CompactCapsule` to `TemporaryPreview` within 50ms of post event.

#### Decision `DEC-NOTIF-02`: Preview Display Duration Before Auto-Collapse
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The island has automatically expanded to show Alice's WhatsApp message. The user does not touch the screen.
- **Available Choices:**
  - **Choice A:** 2.0 seconds (Fast glance).
  - **Choice B (Recommended):** 3.0 seconds (Standard reading window).
  - **Choice C:** 4.5 seconds (Extended accessibility window).
- **Recommended Default:** **Choice B** (3.0 seconds).
- **Reason:** Allows comfortable reading of sender + 1 line of text without lingering long enough to block foreground typing.
- **Impact on Related Decisions:** Governs auto-collapse timer coroutine delay.
- **Testable Acceptance Criteria:** Preview card remains visible for exactly 3000ms $\pm 100\text{ms}$ before initiating collapse animation.

#### Decision `DEC-NOTIF-03`: Notification Priority & Silent Channel Filtering
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** An app posts a low-priority, ongoing, or silent notification (e.g., Google Play syncing, weather update, step counter).
- **Available Choices:**
  - **Choice A (Recommended):** Strict Filtering. Never expand the island for silent channels, low-priority channels, or ongoing background service notifications.
  - **Choice B:** Ingest and show as compact icon badge only (no auto-expansion).
  - **Choice C:** Ingest everything unconditionally.
- **Recommended Default:** **Choice A** (Strict Filtering).
- **Reason:** Prevents spamming users with intrusive expansions from background sync tasks.
- **Impact on Related Decisions:** Ingestion logic in `IslandNotificationListener`.
- **Testable Acceptance Criteria:** Notifications with `importance < IMPORTANCE_DEFAULT` or `isOngoing == true` (without progress) are ignored by island state machine.

#### Decision `DEC-NOTIF-05`: Post-Collapse Destination: Compact Capsule vs. Hidden
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The 3.0s temporary preview timer expires without user interaction.
- **Available Choices:**
  - **Choice A (Recommended):** Collapse to Compact Notification Capsule. Shrinks to 36.67dp pill showing app icon and unread count badge (`"1"`).
  - **Choice B:** Collapse to Idle Cutout Capsule (hide notification indicator).
  - **Choice C:** Hide Completely (0dp).
- **Recommended Default:** **Choice A** (Collapse to Compact Notification Capsule).
- **Reason:** Preserves unread awareness at a glance, allowing the user to tap-and-hold to expand later.
- **Impact on Related Decisions:** Governs target state of `NotificationEvent.Expired`.
- **Testable Acceptance Criteria:** After collapse, island body height is 36.67dp, displaying 20dp source app icon and unread badge.

---

### Group 3: Expanded Island Interactions

#### Decision `DEC-EXP-01`: Outside Tap Behavior on Expanded Card
- **Status:** `[OWNER_APPROVED]` (Requirement Approved; Touch Modal Mode Pending)
- **Concrete Scenario:** An expanded notification card is open on screen. The user taps on the background screen outside the card.
- **Available Choices:**
  - **Choice A (Owner Mandated):** Collapse to Compact. Shrinks the card to compact pill without canceling notification.
  - **Choice B (Recommended Implementation):** Collapse + Pass-Through Click (`FLAG_WATCH_OUTSIDE_TOUCH`). Collapses card while immediately delivering the click to the underlying app.
  - **Choice C:** Modal Block. Collapses card, but consumes touch to prevent accidental background action.
- **Recommended Default:** **Choice B** (Collapse + Pass-Through Click).
- **Reason:** Non-intrusive; prevents user frustration by not requiring double-taps to interact with underlying apps.
- **Impact on Related Decisions:** `WindowManager.LayoutParams` flags configuration.
- **Testable Acceptance Criteria:** Tap outside island bounds collapses island to compact and triggers background app click in $\le 50\text{ms}$.

#### Decision `DEC-EXP-02`: Swipe-Up Gesture Policy on Expanded Card
- **Status:** `[OWNER_APPROVED]` (Requirement Approved; Dismissal Gesture Pending)
- **Concrete Scenario:** The user swipes upward on an expanded notification card.
- **Available Choices:**
  - **Choice A (Current Production):** Destructive Dismissal. Cancels notification in Android system shade and evicts from queue.
  - **Choice B (Owner Mandated):** Collapse to Compact. Shrinks card to compact pill; preserves notification in queue and system shade.
- **Recommended Default:** **Choice B** (Collapse to Compact).
- **Reason:** Prevents accidental deletion of unread messages during quick gestures.
- **Impact on Related Decisions:** Retracts destructive call to `cancelNotification()` in `IslandOverlayService.kt:700`.
- **Testable Acceptance Criteria:** Upward swipe collapses card to compact; notification remains in queue and Android shade.

#### Decision `DEC-EXP-03`: Single Tap on Expanded Card Body
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** The user taps on the message text or card background of an expanded notification card (not on an action button).
- **Available Choices:**
  - **Choice A (Recommended):** Launch Owning Application. Fires `contentIntent` and collapses island.
  - **Choice B:** No-Op (Action buttons only). Tapping background does nothing.
- **Recommended Default:** **Choice A** (Launch Owning Application).
- **Reason:** Core smartphone convention: tapping a notification card opens the conversation.
- **Impact on Related Decisions:** Dispatches `sbn.notification.contentIntent`.
- **Testable Acceptance Criteria:** Single tap on card body dispatches `contentIntent` and collapses island to compact.

---

### Group 4: Core Media & Preemption

#### Decision `DEC-MEDIA-01`: Priority Between Active Music and Incoming Notifications
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Spotify is playing (compact music island active). A WhatsApp message arrives.
- **Available Choices:**
  - **Choice A (Recommended):** Temporary Preview Preemption. WhatsApp temporarily expands over the island for 3.0s, then collapses into a dual-activity presentation (Music pill + WhatsApp detached bubble).
  - **Choice B:** Instant Split Island. Squeeze music into leading/trailing; pop WhatsApp icon into detached circle immediately without expanded preview.
  - **Choice C:** Music takes 100% priority; notification stays in system shade.
- **Recommended Default:** **Choice A** (Temporary Preview Preemption, then restore dual split).
- **Reason:** Balances urgent glancing needs with continuous media controls.
- **Impact on Related Decisions:** Multi-window overlay coordination.
- **Testable Acceptance Criteria:** WhatsApp previews for 3.0s, then settles to dual-activity state with music playing.

#### Decision `DEC-MEDIA-04`: Incoming Call Preemption Policy
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** A cellular or VoIP phone call arrives while an expanded notification or media is active.
- **Available Choices:**
  - **Choice A (Recommended):** Absolute P0 Preemption. Call immediately supersedes all active previews; island expands to full Incoming Call card (Caller avatar, name, Accept [Green], Decline [Red]).
  - **Choice B:** Compact pill with caller name scrolling.
- **Recommended Default:** **Choice A** (Absolute P0 Preemption).
- **Reason:** Ringing phone calls are the highest urgency event on mobile devices.
- **Impact on Related Decisions:** Saves suspended state in `SuspendedPagerState`.
- **Testable Acceptance Criteria:** Call state preempts notification within 50ms; declining call restores previous state.

---

## Phase 2: Differentiating Features (Priority P1)

### Group 5: Multi-Notification Horizontal Pager

#### Decision `DEC-PAGER-01`: Horizontal Notification Pager Activation
- **Status:** `[OWNER_APPROVED]` (Requirement Approved; Gesture Engine Pending)
- **Concrete Scenario:** 2 or more eligible notifications are active in `NotificationQueueState`.
- **Available Choices:**
  - **Choice A (Owner Mandated):** Enable Horizontal Paging. When expanded, user can swipe left/right across cards.
  - **Choice B:** Single Top Notification Only.
- **Recommended Default:** **Choice A** (Enable Horizontal Paging).
- **Reason:** Major Android-exclusive differentiator over iOS.
- **Impact on Related Decisions:** Governs `DEC-PAGER-02` through `DEC-PAGER-05`.
- **Testable Acceptance Criteria:** Expanding island with $>1$ notification allows horizontal swiping between cards.

#### Decision `DEC-PAGER-02`: Maximum Number of Pageable Cards
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User has 15 unread notifications in system shade.
- **Available Choices:**
  - **Choice A:** Cap at 3 cards.
  - **Choice B (Recommended):** Cap at 5 cards.
  - **Choice C:** Cap at 10 cards.
  - **Choice D:** Unlimited (Page through all).
- **Recommended Default:** **Choice B** (Cap at 5 cards).
- **Reason:** 5 cards provides excellent utility while bounding memory and avoiding infinite scrolling traps.
- **Impact on Related Decisions:** `.take(5)` filter on `NotificationQueueState.notifications`.
- **Testable Acceptance Criteria:** Pager presents at most 5 cards regardless of shade notification count.

#### Decision `DEC-PAGER-03`: Pager Inactivity Auto-Collapse Duration
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User opens multi-notification pager, pages to card 2, but stops interacting.
- **Available Choices:**
  - **Choice A:** 8.0 seconds.
  - **Choice B (Recommended):** 15.0 seconds.
  - **Choice C:** Never auto-collapse while pager is open (require explicit swipe-up or outside tap).
- **Recommended Default:** **Choice B** (15.0 seconds).
- **Reason:** Allows ample reading time while ensuring screen is not permanently occluded if phone is set down.
- **Impact on Related Decisions:** Resettable inactivity coroutine timer.
- **Testable Acceptance Criteria:** Card collapses back to compact after 15s of zero user touch.

#### Decision `DEC-PAGER-04`: Current Page Eviction Animation & Flow
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User is reading Page 1 of 4 and taps `[Mark as Read]`.
- **Available Choices:**
  - **Choice A (Recommended):** Slide Out + Snap to Next. Current card animates out; Page 2 smoothly takes focus.
  - **Choice B:** Instant snap without animation.
  - **Choice C:** Collapse island immediately upon action.
- **Recommended Default:** **Choice A** (Slide Out + Snap to Next).
- **Reason:** Keeps user in their reading flow without abrupt UI termination.
- **Impact on Related Decisions:** Triggers `NotificationEvent.DismissActive`.
- **Testable Acceptance Criteria:** Card scales down/fades out; next item snaps into primary focus; counter updates `1 of 4` $\to$ `1 of 3`.

#### Decision `DEC-PAGER-05`: Compact Pill Representation for Multiple Notifications
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Island collapses to compact mode while 4 notifications remain queued.
- **Available Choices:**
  - **Choice A (Recommended):** Primary Icon + Number Badge. Leading shows most recent app icon; trailing shows badge (`"+3"` or `"4"`).
  - **Choice B:** Dual Stacked Icons (overlapping 16dp icons).
  - **Choice C:** Periodic icon cycling (every 5 seconds).
- **Recommended Default:** **Choice A** (Primary Icon + Number Badge).
- **Reason:** Cleanest presentation that fits the 36.67dp compact height without visual noise.
- **Impact on Related Decisions:** Layout in `CompactNotificationLayout`.
- **Testable Acceptance Criteria:** Compact pill shows 20dp icon + number badge.

---

### Group 6: Media, Calls & Live Activities

#### Decision `DEC-MEDIA-02`: Single Tap on Compact Music Island
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Music is playing in compact mode. User single-taps the pill.
- **Available Choices:**
  - **Choice A (Recommended):** Launch Music App (Spotify/YT Music).
  - **Choice B:** Toggle Play/Pause in-place.
  - **Choice C:** Expand to full music player card.
- **Recommended Default:** **Choice A** (Launch Music App).
- **Reason:** Aligns with standard mobile convention (Single Tap = App Launch; Long Press = Expand Controls).
- **Impact on Related Decisions:** Governs `DEC-MEDIA-03`.
- **Testable Acceptance Criteria:** Single tap opens music app via `sessionActivity`.

#### Decision `DEC-MEDIA-03`: Long Press on Compact Music Island
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User touches and holds (400ms) on compact music island.
- **Available Choices:**
  - **Choice A (Recommended):** Expand Interactive Media Card (Track art, title, seek bar, controls).
  - **Choice B:** Open Quick Controls.
- **Recommended Default:** **Choice A** (Expand Interactive Media Card).
- **Reason:** Delivers the signature Apple-style expanded music controller.
- **Impact on Related Decisions:** Replaces existing Quick Controls mapping in `IslandInteractionPolicy.kt:84`.
- **Testable Acceptance Criteria:** 400ms hold expands full music controls card.

#### Decision `DEC-LIVE-01`: Timer & Stopwatch Presentation
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User sets a countdown timer in Clock app.
- **Available Choices:**
  - **Choice A (Recommended):** Persistent Compact Timer with Real-Time Countdown (`MM:SS`).
  - **Choice B:** Notification preview only.
- **Recommended Default:** **Choice A** (Persistent Compact Timer).
- **Reason:** Core flagship use case with immense daily glanceability.
- **Impact on Related Decisions:** Ingested via `NotificationLiveActivityMapper`.
- **Testable Acceptance Criteria:** Pill shows orange timer icon + live ticking seconds.

#### Decision `DEC-LIVE-02`: Navigation Turn-by-Turn Maneuvers
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Google Maps is navigating in background.
- **Available Choices:**
  - **Choice A (Recommended):** Compact Directional Arrow + Proximity Auto-Expansion (<500m to turn).
  - **Choice B:** Continuous Expanded Navigation Card.
  - **Choice C:** Exclude navigation from island.
- **Recommended Default:** **Choice A** (Compact Arrow + Proximity Expansion).
- **Reason:** Delivers timely driving cues without continuously blocking the screen.
- **Impact on Related Decisions:** Parses Google Maps navigation notification extras.
- **Testable Acceptance Criteria:** Approaching turn expands card briefly, then collapses back to arrow icon.

#### Decision `DEC-LIVE-03`: Voice Recording & Microphone Indicator
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Audio recorder or WhatsApp voice note recording in background.
- **Available Choices:**
  - **Choice A (Recommended):** Red Pulsing Dot + Live Audio Visualizer Waveform.
  - **Choice B:** Static mic icon only.
- **Recommended Default:** **Choice A** (Red Pulsing Dot + Audio Waveform).
- **Reason:** Essential privacy transparency and instant recording controls.
- **Impact on Related Decisions:** Implemented in `IslandState.Recording`.
- **Testable Acceptance Criteria:** Pill renders pulsing red dot and animated audio wave.

#### Decision `DEC-LIVE-04`: Multi-Activity Arbitration (2+ Active Tasks)
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Music is playing, Timer is running, and a WhatsApp message arrives.
- **Available Choices:**
  - **Choice A (Recommended):** Two Visible (Pill + Detached Bubble) + Remainder Queued.
  - **Choice B:** Single combined mega-pill.
  - **Choice C:** Strict FIFO rotation.
- **Recommended Default:** **Choice A** (Two Visible + Remainder Queued).
- **Reason:** Matches Apple's 2-activity hardware limit while gracefully handling transient alerts.
- **Impact on Related Decisions:** Requires dual-window overlay architecture.
- **Testable Acceptance Criteria:** Main capsule shows Timer; detached bubble shows Music; WhatsApp previews temporarily, then returns to Timer + Music.

---

### Group 7: Action Buttons & Secondary Gestures

#### Decision `DEC-EXP-04`: Close `(x)` Button Eviction Scope
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User taps the circular Close `(x)` button on an expanded card.
- **Available Choices:**
  - **Choice A (Recommended):** Evict from Island Queue Only (Leave safe in Android shade).
  - **Choice B:** Full Dismissal (Evict from island AND cancel in system shade).
  - **Choice C:** Hide Close button entirely.
- **Recommended Default:** **Choice A** (Evict from Island Queue Only).
- **Reason:** Protects user data integrity; unread messages remain available in system shade.
- **Impact on Related Decisions:** Triggers `NotificationEvent.DismissActive` without `cancelNotification()`.
- **Testable Acceptance Criteria:** Card disappears from island; notification remains in Android shade.

#### Decision `DEC-EXP-05`: Action Button Row Layout & Quota
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** An incoming notification contains 3 or 4 action intents.
- **Available Choices:**
  - **Choice A (Recommended):** Max 2 Primary Actions + Overflow `[...]`.
  - **Choice B:** Horizontal scrollable row for all actions.
  - **Choice C:** Show only the first action (Reply).
- **Recommended Default:** **Choice A** (Max 2 Primary Actions + Overflow).
- **Reason:** Prevents visual clutter and guarantees touch targets meet 48dp accessibility standard.
- **Impact on Related Decisions:** Card measurement in `NotificationIsland.kt`.
- **Testable Acceptance Criteria:** Renders top 2 actions as 48dp buttons; extra actions accessible via overflow.

#### Decision `DEC-IDLE-04`: Swipe Up on Idle Capsule
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User swipes upward on the resting idle capsule.
- **Available Choices:**
  - **Choice A (Recommended):** Temporarily Hide Island (0dp) until next event.
  - **Choice B:** Ignore / No-Op.
- **Recommended Default:** **Choice A** (Temporarily Hide Island).
- **Reason:** Gives users an intuitive physical gesture to dismiss the capsule during fullscreen tasks.
- **Impact on Related Decisions:** Toggles `isIdleAutoHideActive`.
- **Testable Acceptance Criteria:** Upward swipe collapses window to 0dp; incoming event restores it.

#### Decision `DEC-NOTIF-04`: Subsequent Notification Arrival During Active Preview
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Bob sends a Telegram message while Alice's WhatsApp preview is actively on screen.
- **Available Choices:**
  - **Choice A (Recommended):** Queue Insertion with Contextual Badge (`1 of 2 · New from Bob`) + Timer Extension (+2.0s).
  - **Choice B:** Abrupt Preemption (Immediately replace Alice with Bob).
  - **Choice C:** Ignore Bob until Alice collapses.
- **Recommended Default:** **Choice A** (Queue Insertion + Contextual Badge).
- **Reason:** Prevents jarring text flashing and allows user to finish reading Alice's message.
- **Impact on Related Decisions:** Queue reducer event handling.
- **Testable Acceptance Criteria:** Alice remains on screen; header updates to `1 of 2 · New from Bob`; timer extends by 2s.

---

## Phase 3: Visual Polish, Motion & Privacy (Priority P2)

### Group 8: Visual Geometry & Motion

#### Decision `DEC-VISUAL-01`: Compact Pill Baseline Height
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Sizing the resting compact Dynamic Island pill.
- **Available Choices:**
  - **Choice A (Recommended):** 36.67dp (Exact Apple HIG token specification).
  - **Choice B:** 40.0dp (Slightly larger for oversized Android camera punch-holes).
  - **Choice C:** Dynamically match status bar height.
- **Recommended Default:** **Choice A** (36.67dp).
- **Reason:** Strict compliance with `docs/ISLAND_LAYOUT_RULES.md` and Apple design parity.
- **Impact on Related Decisions:** `AppIslandTokens.compactHeight`.
- **Testable Acceptance Criteria:** Visual body measures exactly 36.67dp.

#### Decision `DEC-VISUAL-02`: Expanded Card Maximum Height
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Sizing the expanded notification and multi-notification pager card.
- **Available Choices:**
  - **Choice A:** Cap at 160dp (Apple HIG ceiling).
  - **Choice B (Recommended):** Cap at 220dp with internal scrolling for overflow text.
  - **Choice C:** Dynamic Wrap Content (no cap).
- **Recommended Default:** **Choice B** (Cap at 220dp with internal scrolling).
- **Reason:** Accommodates Android notifications with longer text and 2 rows of action buttons comfortably.
- **Impact on Related Decisions:** `IslandContentViewport` constraints.
- **Testable Acceptance Criteria:** Card expands to fit content up to 220dp; text $>220\text{dp}$ scrolls internally.

#### Decision `DEC-VISUAL-03`: Staged Button Entry vs. Double-Tap Debounce
- **Status:** `[TECHNICAL_VERIFICATION_REQUIRED]`
- **Concrete Scenario:** Guarding against accidental double-tap clicks onto newly expanded buttons.
- **Available Choices:**
  - **Choice A (Recommended):** Staged Button Entry (Buttons fade in only after spring reaches settling phase at $t \ge 300\text{ms}$).
  - **Choice B:** Universal 300ms tap lockout (blocks all touches during motion).
  - **Choice C:** Explicit double-tap gesture handler in `combinedClickable`.
- **Recommended Default:** **Choice A** (Staged Button Entry) combined with **Choice C**.
- **Reason:** Eliminates double-tap leakage without introducing artificial touch input delays or accessibility violations.
- **Impact on Related Decisions:** Action button composition in `NotificationIsland.kt`.
- **Testable Acceptance Criteria:** Rapid double-tap on compact pill expands card without triggering Close button.

#### Decision `DEC-VISUAL-04`: Reduced Motion Accessibility Compliance
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User has enabled "Remove animations" in Android Accessibility settings.
- **Available Choices:**
  - **Choice A (Recommended):** Instant Cut / Fast Fade (100ms alpha cross-fade, zero geometry spring scaling).
  - **Choice B:** Retain gentle tween.
- **Recommended Default:** **Choice A** (Instant Cut / Fast Fade).
- **Reason:** Mandatory accessibility compliance under `docs/UI_UX_RULES.md`.
- **Impact on Related Decisions:** `LocalReducedMotion` checks in animation composables.
- **Testable Acceptance Criteria:** Enabling reduced motion eliminates spring animations; transitions occur in $\le 100\text{ms}$.

---

### Group 9: Privacy & System Integrations

#### Decision `DEC-PRIV-01`: Lockscreen Notification Content Redaction
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Phone is locked (Keyguard active), and a private message arrives.
- **Available Choices:**
  - **Choice A (Recommended):** Redact Body & Actions (Show only app icon and "New Notification"; hide message body and sender).
  - **Choice B:** Hide Island completely on lockscreen.
  - **Choice C:** Display full content (User opt-in only).
- **Recommended Default:** **Choice A** (Redact Body & Actions).
- **Reason:** Protects private messages against shoulder surfing.
- **Impact on Related Decisions:** Keyguard state checking in `IslandOverlayService`.
- **Testable Acceptance Criteria:** When locked, card displays app icon and "New Message"; message text is hidden.

#### Decision `DEC-PRIV-02`: Sensitive Notification Blacklisting (Banking / OTPs)
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** An SMS arrives containing a bank transaction or 2FA verification code.
- **Available Choices:**
  - **Choice A (Recommended):** Strict Category / Keyword Filter (Suppress auto-expansion for banking apps or OTP text).
  - **Choice B:** Treat all notifications equally.
- **Recommended Default:** **Choice A** (Strict Category / Keyword Filter).
- **Reason:** Prevents sensitive financial and security codes from being projected in floating windows.
- **Impact on Related Decisions:** Filter regex in `NotificationFilterPolicy`.
- **Testable Acceptance Criteria:** Notifications containing OTP regex do not auto-expand.

#### Decision `DEC-PRIV-03`: Off-Center & Left Punch-Hole Camera Adaptation
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** Phone has a left-corner punch-hole camera rather than a centered cutout.
- **Available Choices:**
  - **Choice A (Recommended):** Centered Floating Island with Cutout Offset Calibration.
  - **Choice B:** Anchor Island to the Left Cutout (Asymmetric expansion).
- **Recommended Default:** **Choice A** (Centered Floating Island with Offset Calibration).
- **Reason:** Left-anchored islands look unbalanced and collide with notification icons in the status bar.
- **Impact on Related Decisions:** `IslandPlacementPolicy.kt`.
- **Testable Acceptance Criteria:** Island remains centered at top of display, offset safely from status icons.

#### Decision `DEC-PRIV-04`: Overlay Suppression Over Secure / Camera Apps
- **Status:** `[RECOMMENDED_PENDING]`
- **Concrete Scenario:** User opens a banking app (`FLAG_SECURE`) or launches the camera viewfinder.
- **Available Choices:**
  - **Choice A (Recommended):** Auto-Suppress Overlay (Hide island completely while secure package is in foreground).
  - **Choice B:** Remain visible.
- **Recommended Default:** **Choice A** (Auto-Suppress Overlay).
- **Reason:** Avoids Android security conflicts (`FLAG_WINDOW_IS_OBSCURED`) and never occludes the camera viewfinder.
- **Impact on Related Decisions:** Package monitoring in accessibility service.
- **Testable Acceptance Criteria:** Opening camera or banking app immediately hides island overlay.
