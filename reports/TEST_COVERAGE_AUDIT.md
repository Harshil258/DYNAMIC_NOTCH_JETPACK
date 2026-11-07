# Automated Test Suite Coverage & Verification Audit (Corrected)

## 1. Actual Verification Execution & Command Evidence

The entire automated test suite was executed against the local repository and the connected physical device:
- **Device Tested:** Realme 8 (RMX3085), Android 13 (API 33), Build `RMX3085_11_F.06`
- **Execution Command:**
  ```sh
  tools/test-island-playground.sh X4Y9QSOZ4DEADEDM
  ```
- **Exit Code:** `0` (SUCCESS)

### Verified Results Breakdown:
- **Build & Compilation:** `PASS` (`:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:processReleaseMainManifest`)
- **JVM Unit Tests:** `408 / 408 PASSED` (0 failed, 0 skipped across 76 test classes)
- **Device Instrumentation Tests:** `4 / 4 PASSED` (`ai.emots.kishan_dynamic.IslandLayoutInstrumentedTest` & `ai.emots.kishan_dynamic.DynamicIslandPlaygroundTest`)
- **Visual Regression Tests:** `3 / 3 PASSED` (`expiry_320`, `compact_count_393`, `expiry_320_200pct_rtl`)
- **Release Manifest Isolation:** `PASS` (0 playground components in release merged manifest)

---

## 2. Granular Test Group Audit

### Group A: JVM Unit Tests (408 Tests across 76 Classes)
- **Command:** `./gradlew :app:testDebugUnitTest --console=plain`
- **Target Code:** Production data policies, reducers, token math, geometry constraints, and playground controller logic.

| Test Class | Key Methods | Production vs. Mock | What Was Asserted | What Was NOT Asserted |
| :--- | :--- | :--- | :--- | :--- |
| **`DynamicIslandGeometryTest`** | `allCollapsedStatesUseTheSharedCompactClassification`, `timerUsesTheReferenceExportHeight`, `everyCollapsedStateResolvesToExactlyOneHeightToken` | Production tokens & enums | Asserts 21 compact states resolve to `tokens.compactHeight`; asserts exact reference heights for Timer (85.73dp), Call (166dp), Music (177dp). | Does NOT measure Compose rendering, font scaling, or actual pixel layout passes. |
| **`AppIslandTokensTest`** | `expandedWidth_matchesReferencePhoneMargins`, `expandedWidth_isCappedOnTabletsAndFoldables` | Production layout tokens | Asserts expanded widths scale proportionally and clamp at 408dp max on foldables/tablets. | Does NOT test runtime Compose layout constraints. |
| **`IslandPlacementPolicyTest`** | `centeredAndPillCamerasCannotCoverNotificationContent`, `everyExpandedWidthSitsBelowStatusIconsAndCamera` | Production placement engine | Asserts cutout clearances and top offsets place the window safely below camera cutouts. | Does NOT test actual window manager surface placement on device. |
| **`IslandInteractionPolicyTest`** | `compactLongPressOpensQuickControls`, `singleTapExpandsSourceAndMinimalTapDoesNothing`, `stackedCompanionOpensNextNotificationInPager` | Production interaction policy | Asserts semantic actions (`OPEN_NOTIFICATIONS`, `OPEN_MUSIC`, `TOGGLE_EXPANSION`, `OPEN_QUICK_CONTROLS`, `NONE`) for gestures. | Does NOT test actual pointer event dispatching, tap timing, or double-tap behavior. |
| **`NotificationReducerTest`** | `newNotificationPrependsAndBumpsSelection`, `isOngoingProgressUpdatePreservesPositionAndSelection`, `exactUpdateRemovesActionsNoLongerExposedBySource` | Production reducer | Asserts queue deduplication, in-place progress update, sorting, and action replacement. | Does NOT test asynchronous artwork decoding or Android notification extras parsing. |
| **`NotificationConcurrencyAndLifecycleStressTest`** | `scenarioA_burstProcessingTwentyFiveNotificationsRemainsBoundedAndSafe`, `scenarioD_notificationUpdateReplacesInPlaceWithoutReorderingOrLosingSelection` | Production coordinator | Asserts thread-safe mutations under 25-event bursts, late artwork drop, and call interruption restoration. | Does NOT test Compose frame drops or GPU rendering during bursts. |
| **`PlaygroundControllerTest`** | `everyFixtureUsesARealStateAndPresentationMapping`, `motionSpeedsChangeTheActualFiniteAnimationSpecAndReachStableEndpoints`, `rapidEventsRemainBoundedAndFinishAtTheLastSource` | Playground mock controller | Asserts mock state mapping, finite animation spec convergence at 0.25x-2x speeds, and 30-event burst completion. | Exercises `PlaygroundController` mock logic only, not production `IslandStateManager`. |

---

### Group B: Device Instrumentation Tests (4 Tests on AndroidJUnitRunner)
- **Command:**
  ```sh
  adb -s X4Y9QSOZ4DEADEDM shell am instrument -w -r -e class ai.emots.kishan_dynamic.IslandLayoutInstrumentedTest,ai.emots.kishan_dynamic.DynamicIslandPlaygroundTest com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner
  ```
- **Target Code:** Jetpack Compose rendering on real Android device runtime (Realme 8, Android 13).

| Test Class & Method | Production vs. Mock | What Was Asserted | What Was NOT Asserted |
| :--- | :--- | :--- | :--- |
| **`IslandLayoutInstrumentedTest.expiryAndCountFitAllWidthsAndFontSizesWithWorkingActions`** | Production Compose components (`DynamicIslandPill`) on synthetic fixtures | **52 Compose layout permutations tested:**<br>• Permutations: 4 widths (320, 360, 393, 430dp) × 2 font scales (1f, 2f) × 2 directions (LTR, RTL).<br>• Asserts `surface.width <= width - 21f` (gutters respected).<br>• Asserts title is above actions (`title.bottom <= watch.top`).<br>• Asserts all interactive buttons have $\ge 48\text{dp}$ touch target bounds.<br>• Asserts media buttons do not overlap.<br>• Asserts action callbacks execute on click. | Does NOT test real notification data from system listener; does NOT assert Live Activities (Delivery, Flight, Sports, Satellite, AirPods, etc.); does NOT test vertical drag gestures or swipe thresholds. |
| **`DynamicIslandPlaygroundTest.debugWorkbenchUsesTheActualRendererForEveryMockFamily`** | Production Compose components within Playground workbench | • Iterates through all 19 `PlaygroundCase` entries.<br>• Asserts `playground_preview` and `island_surface` exist.<br>• Asserts Pro access card renders at 200% font scale and RTL.<br>• Asserts all local policy checks pass. | Does NOT assert visual pixel layout fidelity for each case; does NOT test the 33 unexposed production variants. |
| **`DynamicIslandPlaygroundTest.geometryAndMotionControlsAffectTheProductionSurface`** | Production Compose components within Playground workbench | • Overrides `minimumWidth = 140f` and asserts measured Compose `boundsInRoot.width` increases.<br>• Runs 30-event burst and asserts completion. | Does NOT assert visual rendering quality or text clipping under extreme overrides. |
| **`DynamicIslandPlaygroundTest.workbenchTabsAndAccessibleKnobsAreInteractive`** | Playground UI tabs and sliders | • Switches between "Animations" and "States" tabs.<br>• Adjusts damping ratio slider to 0.95 via accessibility action.<br>• Clicks primary button; selects `PlaygroundCase.Access`.<br>• Captures `access-preview.png`. | Does NOT test physical overlay window touch events. |

---

### Group C: Visual Screenshot Regression (3 Baseline Checks)
- **Command:** `python3 tools/check-island-playground-screenshots.py --serial X4Y9QSOZ4DEADEDM`
- **Frames Checked:**
  1. `expiry_320.png`: 320dp width, 1.0 font scale, LTR
  2. `compact_count_393.png`: 393dp width, 1.0 font scale, LTR (stacked notification counter)
  3. `expiry_320_200pct_rtl.png`: 320dp width, 2.0 font scale, RTL
- **Comparison Engine:** Pure Python standard library PNG decoder comparing exact pixels against `docs/design/playground-goldens/`.
- **Pass Criteria:** Mean RGB delta $\le 0.25$, changed pixel fraction $\le 0.005$.
- **Actual Result:** `PASSED` (0.0 changed pixel fraction, mean RGB delta $< 0.18$).

### Critical Screenshot Coverage Gaps:
While the 3 checked frames passed, **calling overall visual rendering verified is incorrect**. Major screenshot gaps remain:
1. **Tiny Collapsed Badge Appearance:** No screenshot baseline exists for single-line compact notifications at 320dp or 360dp to guard against the reported "tiny badge" regression.
2. **Oversized Expanded Cards:** Only the Pro Expiry card is checked at 320dp. Expanded Music Player, Active Call, and Notification cards are NOT captured or visually asserted.
3. **Live Activity Visuals:** Delivery, Flight, Sports, Navigation, and Timer have ZERO screenshot baseline coverage.
4. **Camera Cutout Collisions:** No visual baseline exists with an active camera cutout overlay (Left, Center, or Pill) to verify that content clearance padding (`cameraClearance`) prevents physical pixel occlusion.
