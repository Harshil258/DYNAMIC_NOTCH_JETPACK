# Approved Implementation Roadmap Candidate

## Overview
This roadmap organizes the remaining Dynamic Island engineering work into 8 sequential, independently verifiable milestones. In accordance with strict instructions, **no implementation has begun**; this document serves as the candidate plan submitted for Technical Examiner approval.

---

## Milestone 1: Complete & Accurate Playground State Coverage

### Objective:
Expand `PlaygroundCase` from 19 mock fixtures to cover all 50 production island variants and 35 functional states, ensuring that every composable rendered by `DynamicIslandPill` can be selected and inspected directly in the Playground UI.

### Files Likely to Change:
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/DynamicIslandPlayground.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundRenderer.kt`
- `app/src/testDebug/java/ai/emots/kishan_dynamic/playground/PlaygroundControllerTest.kt`

### Verification Commands:
```sh
./gradlew :app:testDebugUnitTest --tests "ai.emots.kishan_dynamic.playground.PlaygroundControllerTest"
adb -s SERIAL shell am instrument -w -r -e class ai.emots.kishan_dynamic.DynamicIslandPlaygroundTest com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner
```

### Acceptance Criteria:
- `PlaygroundCase` enum includes distinct cases for all 19 `LiveActivityKind` entries and system alerts (Delivery, Flight, Sports, Shortcuts, AirPods, Satellite, Find My, Handoff, Video Remote, Airplane, Mirroring, Mobile Data, Transit, Call Summary, Volume HUDs).
- States tab in the Playground UI organizes cases into categorized expandable sections (Media & Calls, Notifications, Live Activities, System Alerts).
- Every case dispatches a valid `IslandState` that renders without crashing or blank layout.
- `PlaygroundControllerTest` asserts that all 50 variants map correctly through `resolveOverlayDemoState()`.

### Dependencies:
None (Baseline is established).

---

## Milestone 2: Realistic Notification Payload Fixture & Replay Infrastructure

### Objective:
Implement a platform-independent serialized notification DTO (`RecordedNotificationPayload`) and fixture ingestion engine, allowing real captured notification payloads (e.g. Swiggy delivery, Google Maps navigation, WhatsApp messaging threads) to be loaded and processed through production normalization and live activity mapping.

### Files Likely to Change:
- `app/src/main/java/ai/emots/kishan_dynamic/data/notification/RecordedNotificationPayload.kt` (New)
- `app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationPayloadExtractor.kt` (New)
- `app/src/main/java/ai/emots/kishan_dynamic/service/IslandNotificationListener.kt` (Refactor extraction seam)
- `app/src/debug/assets/fixtures/*.json` (Bundled real payload dumps)
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/DynamicIslandPlayground.kt`

### Verification Commands:
```sh
./gradlew :app:testDebugUnitTest --tests "ai.emots.kishan_dynamic.data.notification.NotificationPayloadExtractorTest"
```

### Acceptance Criteria:
- Pure Kotlin `RecordedNotificationPayload` serializes and deserializes via `kotlinx.serialization` or `org.json`.
- `toNotificationInfo()` accurately maps titles, inbox lines, progress, chronometers, and action metadata without framework `StatusBarNotification` dependencies.
- Playground **Stress Tests** tab contains a "Load Fixture" dropdown with at least 5 realistic presets (Swiggy order, Maps turn, WhatsApp group, OTP alert, Spotify track).
- Extracted payloads successfully trigger `NotificationLiveActivityMapper.map()` into delivery, navigation, or media states.

### Dependencies:
Milestone 1 completed.

---

## Milestone 3: Full Interaction & Gesture Testing

### Objective:
Resolve the confirmed double-tap leakage and long-press cancellation defects in `DynamicIslandPill`, and add comprehensive automated Compose UI gesture tests validating vertical drag thresholds and touch target bounds.

### Files Likely to Change:
- `app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt`
- `app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandInteractionPolicy.kt`
- `app/src/androidTest/java/ai/emots/kishan_dynamic/IslandLayoutInstrumentedTest.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/DynamicIslandPlayground.kt` (Add gesture log panel)

### Verification Commands:
```sh
adb -s SERIAL shell am instrument -w -r -e class ai.emots.kishan_dynamic.IslandLayoutInstrumentedTest com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner
```

### Acceptance Criteria:
- `DynamicIslandPill` explicitly defines double-tap semantics (e.g. debouncing successive clicks within 300ms to prevent accidental dismissal of newly expanded cards).
- Long-press touch slop disambiguation prevents vertical micro-drifts from aborting long-press initiation.
- Automated Compose UI test simulates rapid double-tapping on compact notification and asserts the card does NOT dismiss.
- Automated Compose UI test simulates vertical dragging past `-40px` and asserts `onSwipeDismiss` is invoked.
- Playground UI features a scrollable 50-item Event & Gesture Log stream in the Stress Tests tab.

### Dependencies:
Milestone 1 and 2 completed.

---

## Milestone 4: Animation & Transition Testing

### Objective:
Verify all 8 transition pathways, lock touch targets during spring dimension shifts, and ensure animations converge to stable endpoints without layout snapping or frame stutter.

### Files Likely to Change:
- `app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt`
- `app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandContentViewport.kt`
- `app/src/main/java/ai/emots/kishan_dynamic/ui/motion/AppMotion.kt`
- `app/src/testDebug/java/ai/emots/kishan_dynamic/playground/PlaygroundControllerTest.kt`

### Verification Commands:
```sh
./gradlew :app:testDebugUnitTest --tests "ai.emots.kishan_dynamic.playground.PlaygroundControllerTest"
tools/test-island-playground.sh SERIAL
```

### Acceptance Criteria:
- Interactive touch targets inside expanding cards are gated against pointer clicks during active spring translation (frames 0–15).
- All 8 transitions (Idle ↔ Compact, Compact ↔ Expanded, Hidden ↔ Visible, Single ↔ Multiple) replay cleanly at 0.25x, 0.5x, 1x, and 2x speeds without visual glitches.
- Automated tests assert that spring animations terminate cleanly at exact token target heights and widths.

### Dependencies:
Milestone 3 completed.

---

## Milestone 5: Live Geometry Editing & Preset Persistence

### Objective:
Implement persistent storage for Playground geometry and animation parameters, allowing developers to save, load, and export fine-tuned configurations.

### Files Likely to Change:
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/DynamicIslandPlayground.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundPresetStore.kt` (New)

### Verification Commands:
```sh
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```

### Acceptance Criteria:
- Developers can save custom slider configurations (e.g. "Pixel 8 Pro Tuning", "Compact Notch Fit") with a name.
- Presets persist across process death and Activity recreation in `context.filesDir/presets/`.
- "Export as AppTheme Tokens" button generates a formatted Kotlin code snippet reflecting the active geometry.

### Dependencies:
Milestones 1 and 4 completed.

---

## Milestone 6: Visual Refinement & Screenshot Baseline Expansion

### Objective:
Establish golden screenshot baselines for all major production families to permanently prevent the "tiny collapsed badge" and "oversized expanded card" regressions.

### Files Likely to Change:
- `docs/design/playground-goldens/*.png` (Expanded baselines)
- `tools/check-island-playground-screenshots.py`
- `app/src/androidTest/java/ai/emots/kishan_dynamic/IslandLayoutInstrumentedTest.kt`

### Verification Commands:
```sh
python3 tools/check-island-playground-screenshots.py --serial SERIAL
```

### Acceptance Criteria:
- Capture and commit goldens for:
  - Compact single notification at 320dp and 360dp
  - Expanded notification card at 360dp and 393dp
  - Music Player (compact and expanded)
  - Active Call (compact and expanded)
  - Quick Controls grid
  - Cutout clearance overlay with left and centered camera holes
- Visual comparison script asserts mean RGB delta $\le 0.25$ and changed pixel fraction $\le 0.005$ across all frames.

### Dependencies:
Milestone 5 completed.

---

## Milestone 7: Physical-Device Verification

### Objective:
Validate the floating overlay window on real hardware across different Android OEM skins, hardware cutouts, and font scaling settings.

### Execution Scope:
- Physical device testing on Realme 8 (Android 13) and secondary connected devices.
- Validation of outside-tap dismissal, 3-minute watchdog auto-stop, keyguard hiding, and hardware notch alignment.

### Verification Commands:
```sh
tools/test-island-playground.sh SERIAL
adb -s SERIAL shell am start -n com.island.dynamicnotch/ai.emots.kishan_dynamic.playground.DynamicIslandPlaygroundActivity
```

### Acceptance Criteria:
- Physical overlay positions accurately beneath hardware cutout using `DisplayCutout.boundingRects`.
- Tapping outside the floating overlay collapses it reliably on real touch screens.
- Locking device hides overlay immediately; unlocking restores it.
- Host automation runner records clean `device_status: passed` and `visual_status: passed`.

### Dependencies:
Milestones 1 through 6 completed.

---

## Milestone 8: Final Production Promotion & Release Isolation Audit

### Objective:
Promote approved geometry and animation token constants into production defaults and verify 100% release isolation.

### Files Likely to Change:
- `app/src/main/java/ai/emots/kishan_dynamic/ui/theme/AppTheme.kt`
- `app/src/main/java/ai/emots/kishan_dynamic/ui/motion/AppMotion.kt`
- `app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandGeometryConstraints.kt`

### Verification Commands:
```sh
./gradlew :app:assembleRelease :app:processReleaseMainManifest
python3 -c "
from pathlib import Path
manifest = Path('app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml').read_text()
assert 'playground' not in manifest and 'Island Lab' not in manifest
print('RELEASE MANIFEST IS 100% ISOLATED')
"
```

### Acceptance Criteria:
- Final production values promoted deliberately from validated Playground presets.
- Full test suite passes: 408+ JVM tests, device instrumentation, and visual baselines.
- Release APK contains zero references to debug activities, services, or assets.

### Dependencies:
Milestones 1 through 7 successfully completed and approved by Examiner.
