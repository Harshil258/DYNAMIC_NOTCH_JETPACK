# AURORA V7.1 — Root Cause Analysis & Code Diff Report

**Component**: Dynamic Island Expanded Notification Pager & Geometry Coordinator  
**Branch**: `codex/aurora-v7-1-notification-height-swipe-review`  
**Verified On**: Realme 8 (`RMX3085`, Android 12, Build RMX3085L1, ADB `192.168.29.20:5555`)  
**Status**: VERIFIED & READY FOR EXAMINATION  

---

## 1. Executive Summary & Root Cause Breakdown

In previous Aurora iterations (V5–V6D), users and automated QA observed persistent layout instability in `NotificationExpanded`:
1. **The 86 → 104.8 → 86dp Height Oscillation**:
   - During horizontal or vertical drag gestures, the outer Dynamic Island pill abruptly expanded or snapped to intermediate heights (104.8dp) and then snapped back to 86dp or collapsed into smaller shells.
   - **Root Cause**:
     1. Multi-line notifications (such as the 3-line Mamad text) naturally measured `104.8dp` when unconstrained.
     2. As the user paged between cards, asynchronous `onSizeChanged` callbacks continuously fired into `measuredHeights[activeId]`.
     3. The outer `DynamicIslandPill` used `measuredHeights[IslandDemoState.NotificationExpanded]` directly, causing feedback loops: measuring content triggered parent height animation, which in turn altered child available constraints and triggered remeasurement.
     4. The pager lacked a zero-delay pre-measurement baseline (`estimateNotificationPageHeight`), leaving `activePageHeight` dependent on initial layout passes.
     5. During drag, `activePageHeight` interpolated linearly between current and target without anchoring to `maxOf(currentHeight, lerp(...))`. When swiping toward a shorter card, the shell collapsed prematurely, truncating visible content while the user was still viewing the taller card.

2. **Vertical vs Horizontal Swiping Conflict**:
   - `VerticalPager` was previously used for notification queuing, while the outer playground stage background registered a global `detectHorizontalDragGestures`.
   - In production and playground, drag gestures conflicted with Android notification shade gestures and stage background listeners.
   - Programmatic page index synchronization (`LaunchedEffect(notificationActiveIndex)`) echoed back into `onNotificationSelect`, creating cyclic recompositions and bouncing selections.

3. **Figma 27:992 Contradiction & Picture Wipe**:
   - Node `27:992` in Figma is explicitly a `366.0 × 126.0dp` card with an inset `103 × 86dp` photo thumbnail (R44 squircle).
   - In V6, `PlaygroundGallery.kt` artificially stripped `picturePath = null` for `NotificationExpanded` in an attempt to force it to fit 86dp, but Mamad's 3-line text still expanded to 104.8dp, producing an 18.8dp delta warning.
   - The system lacked distinct fixture modes to test both the exact Figma 27:992 reference (126dp R44) and text-only alerts (86dp R43).

---

## 2. Architectural Solution Implemented in V7.1

1. **One Authoritative Height Coordinator**:
   - Introduced `estimateNotificationPageHeight(item, isReplying)` providing synchronous zero-latency height estimation:
     - `NotificationTemplateType.PICTURE` → `126.dp`
     - `NotificationTemplateType.NAVIGATION` → `120.dp`
     - `NotificationTemplateType.TRANSPORT` → `112.dp`
     - `NotificationTemplateType.STANDARD` (1-2 lines) → `86.dp`
     - `NotificationTemplateType.STANDARD` (≥3 lines) → `104.8.dp`
     - `isReplying = true` → `166.dp`
   - Added a `0.5f` deadband on `pageHeights[item.id]` updates to break measurement feedback loops.
   - Enforced non-shrinking safe drag height:
     $$\text{safeDragHeight} = \max(\text{currentHeight}, \text{lerp}(\text{currentHeight}, \text{targetHeight}, |\text{offsetFraction}|))$$
     The shell never collapses below the height of the currently visible card during drag gestures.

2. **Stable Horizontal Pager & Single-Commit Settlement**:
   - Replaced `VerticalPager` with `HorizontalPager` with stable notification identity keys (`key = { it.id }`).
   - Decoupled programmatic index changes from user gestures using a `userPaging` latch.
   - Settlement committed strictly upon gesture completion (`!isScrollInProgress` and settled page change via `snapshotFlow`), eliminating duplicate callbacks.

3. **Truthful 60-State Playground Fixtures**:
   - Removed artificial `picturePath = null` wipe.
   - Added `notificationFixtureMode` (Exact Figma 27:992 at 126dp, Short Text at 86dp, Multi-line, Media).
   - Added queue sizing selector (1, 3, 12, 25, 50 items) with dynamic `+ Add` and `Clear` capabilities.
   - Excluded notification states from stage background drag interception.

---

## 3. Complete Source Code Diff Summary

### A. `NotificationIsland.kt`
- Implemented `HorizontalPager` replacing `VerticalPager`.
- Added `estimateNotificationPageHeight()` for zero-delay pre-measurement.
- Implemented `safeDragHeight = maxOf(currentHeight, lerp(...))` to prevent viewport collapse during swipe.
- Bound right-hand dot indicator to `dotActiveIndex` to prevent dot jitter during drag.
- Added `0.5f` threshold deadband on `pageHeights` cache.
- Integrated `IslandActionPill` with semantic styling, in-flight debounce, and ≥48dp touch targets.

### B. `DynamicIslandPill.kt`
- Connected `onHeightChange` callback in `NotificationExpandedContent` and `NotificationImageReferenceContent`.
- Dynamic corner radius calculation: resolves `44.dp` for `height >= 120.dp` and `43.dp` for `86.dp`.

### C. `IslandFamilyGeometry.kt`
- Corner radius resolution: `44f` for contextual alerts with height in `[120f, 142f)`, `42f` for `height >= 142f`, and `43f` for `86f`.

### D. `IslandState.kt`
- Added `val isDestructive: Boolean = false` to `NotificationActionInfo`.

### E. `PlaygroundGallery.kt`
- Preserved `picturePath` for node `27:992`.
- Added fixture mode selector (Figma 27:992 126dp, Short Text 86dp, Multi-line, Media).
- Added queue sizing controls (1, 3, 12, 25, 50).
- Excluded notification cards from outer stage horizontal drag listener.

### F. Tests
- Created `AuroraV71NotificationPagerTest.kt` verifying all 22 scenarios (H01-H22).
- Updated instrumented tests (`AuroraFigmaDetailInstrumentedTest.kt` and `LayeredMiniIslandsInstrumentedTest.kt`) to use horizontal swipes.
