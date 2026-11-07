# Prioritized Implementation Recommendations & Action Plan (Corrected)

## 1. Classification & Triage Framework

Implementation gaps identified across the production Dynamic Island and the Dynamic Island Playground are classified into three strict tiers:

- **TIER 1: Critical Shared-Architecture & Interaction Fixes** (Must be resolved before further visual tuning)
- **TIER 2: Playground Tooling & Testing Infrastructure** (Enables complete, systematic validation)
- **TIER 3: UI Polish, Aesthetics & Optional Enhancements** (Fine-tuning animations and styling)

---

## 2. Tier 1: Critical Shared-Architecture & Interaction Fixes

### 1.1 Double-Tap Disambiguation & Tap Debounce
- **Status:** **CONFIRMED DEFECT**
- **Problem:** `Modifier.combinedClickable` on `DynamicIslandPill` omits `onDoubleClick`. Rapidly tapping on the right side of a compact notification fires two distinct single-tap events: the first expands the card, and the second immediately hits the newly rendered 50dp Dismiss button (`NotificationIsland.kt:284`), causing the notification to vanish instantly.
- **Action Required:**
  1. Define double-tap specification with the Examiner.
  2. Implement an interaction debounce or `onDoubleClick` handling in `DynamicIslandPill.kt` to prevent double-tap leakage into newly expanded layouts.

### 1.2 Touch Target Locking During Spring Transitions
- **Status:** **CONFIRMED DEFECT**
- **Problem:** When expanding from compact (76dp) to expanded (380dp), interactive child buttons (e.g. Call End, Music Play/Pause, Reply) move across the display for 300–450ms. Taps landing mid-flight frequently hit the moving container or adjacent buttons.
- **Action Required:**
  - Gate pointer input on interactive buttons during active spring motion (frames 0–15) until layout bounds stabilize.

### 1.3 Decouple Notification Ingestion for True Payload Replay
- **Status:** **ARCHITECTURAL GAP**
- **Problem:** The playground currently constructs synthetic `NotificationInfo` objects directly, completely bypassing `NotificationContentResolver`, `NotificationArtworkRepository`, and `NotificationLiveActivityMapper`. Real framework `StatusBarNotification` objects cannot be cleanly deserialized across test environments.
- **Action Required:**
  1. Define a platform-independent `RecordedNotificationPayload` serializable data class.
  2. Extract extraction logic from `IslandNotificationListener.kt` into a pure `NotificationPayloadExtractor`.
  3. Allow `PlaygroundController` to ingest serialized JSON dumps of real notifications (Swiggy, Google Maps, WhatsApp, Spotify) and run them through the exact same mapping rules as production.

---

## 3. Tier 2: Playground Tooling & Testing Infrastructure

### 2.1 Expand Playground State Catalog to All 50 Production Variants
- **Status:** **CAPABILITY GAP**
- **Problem:** Production supports 50 distinct variant/kind states, but `PlaygroundCase` only exposes 19. Fourteen live activities and system alerts (Delivery, Flight, Sports, Shortcuts, AirPods, Satellite, Find My, Handoff, Video Remote, Airplane Alert, Mirroring Alert, Mobile Data Alert, Transit, Call Summary) cannot be triggered directly from the Playground UI.
- **Action Required:**
  - Update `PlaygroundModel.kt` and `DynamicIslandPlayground.kt` with catalog buttons for all 50 variants, organized into categorized accordion groups (Media & Telephony, Notifications, Live Activities, System Alerts).

### 2.2 Geometry & Motion Preset Persistence
- **Status:** **CAPABILITY GAP**
- **Problem:** Modifying geometry sliders or spring parameters in the Playground resets as soon as the Activity is destroyed or process is killed.
- **Action Required:**
  - Implement a `PlaygroundPresetStore` saving named configurations to `context.filesDir/presets/` with "Save Preset", "Load Preset", and "Export Kotlin Code" buttons.

### 2.3 Interactive Gesture Stream & Diagnostics Logger
- **Status:** **DIAGNOSTICS GAP**
- **Problem:** The Playground shows only a single-line snapshot feedback string and does not expose a historical log of touch events or state transitions.
- **Action Required:**
  - Add a scrollable 50-item Event Log stream in the Playground's **Stress Tests** tab recording timestamped gestures (e.g. `TAP (340, 48)`, `LONG_PRESS`, `DRAG_START`, `SWIPE_UP_DISMISS`).

### 2.4 Automated Compose Gesture Tests
- **Status:** **TEST COVERAGE GAP**
- **Problem:** Vertical drag rubber-banding and the `-40px` swipe-up dismiss threshold are not covered by any automated UI test.
- **Action Required:**
  - Add parameterized Compose UI tests in `IslandLayoutInstrumentedTest` that simulate vertical swipe-up gestures and assert that `onSwipeDismiss()` is fired.

---

## 4. Tier 3: UI Polish & Optional Enhancements

### 4.1 Visual Regression Baseline Expansion
- **Opportunity:** Currently only 3 fixtures have pixel goldens in `docs/design/playground-goldens/`.
- **Enhancement:** Capture and commit goldens for all major expanded families (Music Player, Active Call, Timer, Quick Controls, Delivery, Navigation) at 360dp, 393dp, and 430dp, permanently guarding against the "tiny badge" and "oversized card" regressions.

### 4.2 Horizontal Swiping for Stacked Notifications
- **Opportunity:** When multiple notifications are stacked (`NotificationStacked`), users currently can only tap to expand or tap the companion counter to cycle.
- **Enhancement:** Add horizontal drag gestures (`detectHorizontalDragGestures`) on the compact pill to swipe between queued notifications with Apple-style fluid momentum.
