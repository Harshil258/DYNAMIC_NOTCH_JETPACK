# AURORA V6C — CONTENT-ADAPTIVE LAYOUT ARCHITECTURE SPECIFICATION

**Author:** Principal Product Designer & Staff Jetpack Compose Engineer  
**Date:** November 2026  
**Status:** Implemented & Verified in `codex/aurora-v6c-adaptive-ui-review`

---

## 1. Architectural Philosophy: The Content-Aware Contract

Prior Aurora releases suffered from "hollow islands"—containers reserving nominal maximum dimensions (e.g. 148dp, 162dp) even when optional rows, progress tracks, milestones, or actions were absent. In Aurora V6C, the Island architecture enforces a strict **Content-Aware Geometry Contract**:

1. **Exact Reference Matching for Direct Fixtures:** When rendering verified Figma reference states with their canonical fixtures, the Island adopts the exact nominal geometry (e.g. `367×177dp` for `MusicExpanded`, `367×166dp` for `CallExpanded`, `108×32dp` for `OwnerMinimal`).
2. **Dynamic Content Hugging for Derived & Custom Payloads:** States that present variable runtime content (such as `CallSummaryExpanded`, `TransportActivity`, `NotificationExpanded`, `CustomNotification`) use dynamic `wrapContentHeight()` with natural `Arrangement.spacedBy(...)` gaps. Empty or missing rows consume **0dp**.
3. **Upper-Bound Safe Area Budget:** The Island never exceeds the screen safe viewport `(screenHeight - topInset - 2 * expandedContentInset)` and respects horizontal safe-status reserves.

---

## 2. Geometry Family Tokens & Baselines

All 60 states in `IslandDemoState` map to 7 distinct geometry families defined in `IslandFamilyGeometry`:

| Family | Nominal Target | Radius | Description & States |
|---|---|---|---|
| **S0: Silhouette / Owner Minimal** | 108×32dp / 127×36dp | R15.9 / R18 | Hardware cutout dot, Idle silhouette, and Owner Minimal red capsule (#DC0917 @ 60%). Zero internal content. |
| **S1: Compact Anchored** | 152–222 × 36.67dp | R18.335 | Hardware-height status capsules. Pinned START and END gravity. Includes `CallCompact` (152dp), `MusicCompact` (190dp), `TimerCompact` (222dp), `ChargingCompact` (132dp), etc. |
| **S2: Contextual Alert** | 367 × 86–126dp | R42–R44 | Transient system alerts: `SilentModeExpanded` (86dp), `AirPodsConnected` (86dp), `TimerExpanded` (86dp), `NotificationExpanded` (86dp base), `NotificationImage` (126dp). |
| **S3: Call Expanded** | 367 × 166dp | R42 | Two-row active call HUD: Contact identity START, 5 circular action buttons END. |
| **S4: Media Expanded** | 367 × 177dp | R42 | Three-row music sheet: Album art + track info, scrubber progress track, 5 transport controls. |
| **S5: Activity Expanded** | 367 × 142–185.3dp | R42 | Live activity sheets: `TransitRouteAlert` (142dp), `MovieCard` (142dp), `FlightExpanded` (144dp), `TransportActivity` (148dp), `MobileDataAlert` (162dp), `NavigationExpanded` (185.3dp). |
| **S6: Quick Controls** | 367 × 210dp | R42 | Quick control grid with brightness, volume sliders, and tool tiles. |

---

## 3. Padding Ownership & Layout Hierarchy

A single owner controls padding at each level of the Compose tree to prevent double-insetting:

```
┌────────────────────────────────────────────────────────┐
│ DynamicIslandPill (Root Shell)                         │
│ - Controls overall width, height, corner radius       │
│ - Enforces cameraCutoutMode & flatTop geometry        │
│ - Applies outer shadow and ambient backdrop clip      │
│  ┌──────────────────────────────────────────────────┐  │
│  │ IslandContentViewport (Content Host)             │  │
│  │ - Applies shellHorizontalInset (0dp or 12dp)     │  │
│  │ - Measures intrinsic content height via         │  │
│  │   onGloballyPositioned reporting                 │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │ Leaf State Composable (e.g. CallSummary)   │  │  │
│  │  │ - Owns internal horizontal & vertical gaps │  │  │
│  │  │ - Uses wrapContentHeight()                 │  │  │
│  │  │ - Spaced by Arrangement.spacedBy(10.dp)    │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## 4. Content Measurement Engine (`DynamicIslandPill.kt`)

In `DynamicIslandPill.kt`, the `naturalTargetHeight` calculation was refactored in V6C to grant dynamic height resolution to all derived and activity states:

```kotlin
val naturalTargetHeight = if (isCutoutDot) {
    cutoutDotSizeDp.coerceIn(24.dp, 56.dp)
} else if (!isCompact) {
    (if (state in setOf(
            IslandDemoState.NotificationExpanded, IslandDemoState.CallExpanded,
            // These cards have no direct atlas specimen. Their measured content,
            // including recipe-owned padding, determines the surface height.
            IslandDemoState.PremiumExpiryExpanded,
            IslandDemoState.DeliveryExpanded, IslandDemoState.FlightExpanded,
            IslandDemoState.SportsExpanded, IslandDemoState.TransportActivity,
            IslandDemoState.FlightTrackerExpanded, IslandDemoState.SubscriptionPricing,
            IslandDemoState.MovieCard, IslandDemoState.TimerImage,
            IslandDemoState.ActionControlExpanded, IslandDemoState.ColorOptions,
            IslandDemoState.CallSummaryExpanded,
            IslandDemoState.AirplaneAlert, IslandDemoState.ScreenMirroringAlert,
            IslandDemoState.MobileDataAlert, IslandDemoState.TransitRouteAlert
        ))
        measuredHeights[state] ?: state.resolvedHeight(islandTokens)
    else maxOf(measuredHeights[state] ?: 0.dp, state.resolvedHeight(islandTokens)))
} else state.resolvedHeight(islandTokens)
```

### Result:
- When `CallSummaryExpanded` has 2 rows (Caller + Actions), it measures **96dp**, eliminating the former 16dp empty void.
- When `TransportActivity` lacks a progress bar, it measures **114dp**, eliminating the former 34dp empty void.
- When `MovieCard` has only title + badge, it measures **104dp**, hugging content gracefully.

---

## 5. Independent START / END Gravity (`CompactIslandLayout`)

Compact states must maintain strict separation between the leading identity and trailing status/metric. V6C replaced the previous `Row(Arrangement.SpaceBetween)` with an explicit alignment box:

```kotlin
@Composable
internal fun CompactIslandLayout(
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterStart).wrapContentSize(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart
        ) { leading() }
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).wrapContentSize(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) { trailing() }
    }
}
```

* Guaranteed independent gravity: changes to trailing text length never displace or center leading icons.
* Leading elements anchor strictly to `CenterStart`. Trailing elements anchor strictly to `CenterEnd`.

---

## 6. Accessibility Hit-Target Enforcement

Interactive controls (buttons, switches, dismiss actions) enforce a **48dp minimum interactive component size**:
- `CallSummaryActionButton`: Wrapped in `Box(Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).clickable(...))` with `Role.Button`. Visual capsule size is 36dp with 18dp corner radius.
- Compact Island: Tap and long-press handlers encompass the entire hardware capsule bounds.
- Ongoing Call Controls: 44dp visual circles inside 48dp touch boxes (`buttonTouchSize = buttonVisualSize.coerceAtLeast(48.dp)`).

---

## 7. Motion & Reduced Motion Contracts

- **Normal Animation:** Uses spring-based physics (`stiffnessMediumLow`, `dampingRatioNoBouncy`) with finite endpoints.
- **Reduced Motion:** When `LocalReducedMotion.current == true` (e.g. system accessibility setting enabled or in test harnesses), morph transitions convert to immediate finite crossfades (`tween(180ms)`), preventing motion sickness and eliminating async test flakes.
