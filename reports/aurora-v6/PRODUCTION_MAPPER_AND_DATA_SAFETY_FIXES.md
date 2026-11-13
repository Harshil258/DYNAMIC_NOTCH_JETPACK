# Aurora V6 — Production Mapper and Data Safety Fixes Report

**Classification:** Architectural Data Safety & Mapping Integrity Report  
**Date:** November 2026  
**Target:** IslandPresentationMapper, DynamicIslandPill, IslandFamilyGeometry  

---

## 1. Executive Summary

In Aurora V6, three high-severity structural defects in production mapping, synthetic data leaks, and responsive width calculations were permanently resolved:
1. **Navigation Expanded Route to Debug Palette (`ColorOptions`):** `LiveActivityKind.NAVIGATION` in expanded mode was mistakenly mapped to `IslandDemoState.ColorOptions`. Fixed to route to legitimate turn-by-turn navigation (`IslandDemoState.TransportActivity` / `NavigationIslandExpanded`).
2. **Ghost Notification Injection:** When `notifications` was empty, `NotificationExpandedContent` contained 140 lines of fallback synthetic messages (`demo_meesup`, `demo_calendar`, etc.). Fixed by purging all synthetic fallback messages from production code.
3. **Uncalibrated Global Width Upscaling:** `IslandFamilyGeometry.resolve()` multiplied expanded widths by `availableWidth / 393f`, inflating 367dp targets to ~402dp on 430dp displays. Fixed with `minOf(t.width, available)`.

---

## 2. Navigation Expanded Mapping Resolution

### 2.1 The Issue
In `IslandPresentationMapper.kt` and `DynamicNotchParent.kt`:
```kotlin
// Defective V5 mapping:
is IslandState.LiveActivity -> {
    if (state.info.kind == LiveActivityKind.NAVIGATION && state.info.isExpanded) {
        IslandDemoState.ColorOptions // WRONG: routes user navigation to theme color swatches!
    }
}
```
If a user initiated turn-by-turn navigation (e.g. Google Maps) and tapped the dynamic island to expand, they were greeted by 6 theme color swatches rather than maneuver guidance, distance, and route ETA.

### 2.2 The V6 Fix
- Re-routed `LiveActivityKind.NAVIGATION` when expanded to `IslandDemoState.TransportActivity` (which renders `NavigationIslandExpanded` with maneuver arrow, route corridor, distance to turn, and ETA).
- Reserved `IslandDemoState.ColorOptions` exclusively for intentional palette customization and Playground previews (`ColorOptionsReferenceContent()`).
- Updated all consumer call sites:
  - `IslandPresentationMapper.kt`
  - `DynamicNotchParent.kt`
  - `LiveActivitiesScreen.kt`
  - `PlaygroundCatalog.kt`
  - `PlaygroundModel.kt`
- Added unit tests in `IslandPresentationMapperTest.kt`:
  - `navigationExpandedMapsToTransportActivityNeverColorOptions()` (PASS)

---

## 3. Ghost Notification Fallback Removal

### 3.1 The Issue
In `DynamicIslandPill.kt`, `NotificationExpandedContent()` included a 140-line hardcoded block of synthetic messages (`demo_meesup`, `demo_calendar`, etc.) that executed whenever `notifications.isEmpty()`. In production overlay mode, if the notification queue was empty or transitioning, users saw fictitious messaging previews from people they did not know.

### 3.2 The V6 Fix
- Purged the entire 140-line synthetic list from `DynamicIslandPill.kt`.
- When `notifications.isEmpty()` in production, `NotificationExpandedContent` returns an honest, clean empty box (`"No notifications"`) or allows the island to smoothly collapse to idle.
- Synthetic demo notifications are now strictly confined to `FigmaReferenceFixtures.messages(context)` for visual testing in `PlaygroundGallery`.

---

## 4. Uncalibrated 430dp Width Scaling Fix

### 4.1 The Issue
In `IslandFamilyGeometry.kt`:
```kotlin
// Defective V5 scaling:
val w = if (isCompact) t.width else (t.width * available / 393f).coerceAtMost(available)
```
On a 430dp viewport (e.g. iPhone 14 Pro Max / Pixel 7 Pro / modern Android flagships), `367f * 430f / 393f` calculated to **401.5dp**. No Figma reference or design specification authorized stretching 367dp capsules to 402dp; doing so caused icons and content baselines to float excessively away from the margins.

### 4.2 The V6 Fix
- Replaced the global width multiplier with family-grounded max width:
```kotlin
val w = if (isCompact) minOf(t.width, available) else minOf(t.width, available)
```
- Preserves the exact 367dp nominal width on 393dp, 411dp, and 430dp screens with generous, comfortable horizontal safety margins.
- On narrow viewports (320dp, 360dp), `minOf(t.width, available)` gracefully downscales to fit the screen without clipping.
- Added unit test in `IslandFamilyGeometryTest.kt`:
  - `expanded367dpFamilyDoesNotScaleUpAt430dpCanvas()` (PASS)

---

## 5. Verification Matrix

| Component | Target Behavior | Pre-V6 State | V6 Verified State | Test Evidence |
|---|---|---|---|---|
| `IslandPresentationMapper` | Navigation Expanded Mapping | Mapped to `ColorOptions` | Maps to `TransportActivity` | `IslandPresentationMapperTest` |
| `DynamicIslandPill` | Empty notification queue | Rendered 140 lines of fake messages | Honest empty / quick collapse | `compileDebugKotlin`, UI inspection |
| `IslandFamilyGeometry` | 430dp viewport width | Inflated 367dp to 401.5dp | Locked at 367.0dp nominal | `IslandFamilyGeometryTest` |
| `IncomingCall` Routing | Ringing call routing | Ambiguous avatar stack | `IncomingCallIsland` with Decline/Accept | `IslandPresentationMapperTest` |
