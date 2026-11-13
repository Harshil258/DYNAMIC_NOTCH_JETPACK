# Aurora V6 — Gallery Provenance & Visual QA Report

**Classification:** Visual QA, Provenance Integrity & Gallery Architecture Report  
**Date:** November 2026  
**Target:** PlaygroundGallery, FigmaReferenceFixtures, Visual QA Gates  

---

## 1. Executive Summary

In Aurora V6, the 60-state `PlaygroundGallery` was overhauled from a misleading comparison tool into a **truthful, production-grounded visual inspection harness**. Prior to V6, a fundamental conflict existed between reported provenance (some reports claimed "19 direct / 41 family", while code contained 28 direct, 10 family, 22 no-source) and unreferenced states were rendered as giant blank black boxes that consumed over half of the user's viewport.

Aurora V6 achieves:
1. **Full Provenance Reconciliation:** Exact 28 DIRECT / 10 FAMILY_ONLY / 22 NO_SOURCE alignment across code, fixtures, and documentation.
2. **Elimination of the Black Dummy Box:** `NO_SOURCE` states now render an elegant, compact derived-design specification card alongside the live component.
3. **Honest Exemplar Attribution:** `FAMILY_ONLY` states explicitly state: `"Derived from <Family> (<NodeId>) · not an exact state match"`.
4. **Three-Gate Quality Model:** Clear separation between outer Geometry accuracy, Internal Composition, and Behavior/Capability safety.

---

## 2. Reconciled Provenance Breakdown (60 Total States)

```
60 Total Dynamic Island States
├── 28 DIRECT (46.7%)
│   └── 1:1 Figma asset match. Validated with side-by-side and 1× scale overlay.
├── 10 FAMILY_ONLY (16.7%)
│   └── Derived from parent family exemplar (e.g. TimerCompact from TimerExpanded 19:1022).
└── 22 NO_SOURCE (36.6%)
    └── Handcrafted derived product designs based on Aurora V6 slot and gravity rules.
```

### 2.1 Direct Reference Assets (28 States)
- `Minimal` (`minimal-user-ref`)
- `MusicCompact` (`19:979`), `MusicExpanded` (`19:1033`)
- `CallCompact` (`19:991`), `CallExpanded` (`19:1036`)
- `FaceTimeCallExpanded` (`19:1035`), `SharedMediaCallExpanded` (`19:1037`), `OngoingCallBanner` (`19:1027`)
- `NotificationCompact` (`27:1021`), `NotificationExpanded` (`27:992`), `NotificationImage` (`27:992`)
- `SilentModeExpanded` (`19:1030`), `BluetoothConnected` (`27:682`)
- `TimerExpanded` (`19:1022`)
- `VoiceMemoExpanded` (`19:1024`), `ScreenRecordingExpanded` (`19:1025`), `ShortcutExpanded` (`19:1026`)
- `AirPodsConnected` (`19:1023`), `SatelliteConnected` (`19:1028`), `FindMyAlert` (`19:1029`), `MovedToIPhone` (`19:1031`)
- `VideoRemoteExpanded` (`19:1038`), `AirDropActivity` (`19:1021`), `ColorOptions` (`19:1034`)
- `AirplaneAlert` (`19:1039`), `ScreenMirroringAlert` (`19:1040`), `MobileDataAlert` (`19:1041`), `TransitRouteAlert` (`19:1042`)

### 2.2 Family Exemplar Assets (10 States)
- `NotificationStacked` (Family: `27:1021`)
- `NotificationWithMusicCompact` (Family: `19:979`)
- `RingerModeExpanded` (Family: `19:1030`)
- `TimerCompact` (Family: `19:1022`)
- `DeliveryCompact` (Family: `27:1080`), `DeliveryExpanded` (Family: `27:1101`)
- `FlightCompact` (Family: `27:1080`), `FlightExpanded` (Family: `27:1101`)
- `NavigationCompact` (Family: `19:1019`)
- `CallAvatars` (Family: `19:1027`)

### 2.3 Derived Product Designs (22 States)
- Audio HUDs: `SilentModeCompact`, `VibrateModeCompact`, `NormalModeCompact`, `RingerVolumeCompact`, `MediaVolumeCompact`
- Power Status: `ChargingCompact`, `ChargingExpanded`, `LowBatteryCompact`, `LowBatteryExpanded`
- Hardware & Controls: `Idle`, `BluetoothConnecting`, `ActionControlExpanded`, `FocusModeExpanded`
- Live Activities & Monetization: `CallSummaryExpanded`, `SportsCompact`, `SportsExpanded`, `TimerImage`, `TransportActivity`, `FlightTrackerExpanded`, `SubscriptionPricing`, `MovieCard`, `PremiumExpiryExpanded`

---

## 3. Gallery UI Overhaul: Elimination of the Black Dummy Box

### 3.1 Pre-V6 Black Dummy Problem
In previous versions, selecting a `NO_SOURCE` state in `Side-by-Side` or `Figma Ref` mode rendered a giant black box (`Modifier.size(target.width.dp, target.height.dp).background(Color(0xFF10141D))`) containing the text `"No direct Figma asset (NO_SOURCE)"`. On a 367×112dp or 367×185dp state, this empty box swallowed over half of the screen, obscured real UI review, and gave the impression of a broken app.

### 3.2 V6 Derived Specimen Specification Card
In `PlaygroundGallery.kt`, when `provenance == NO_SOURCE` (or `figmaBitmap == null`), the gallery now renders a compact, elegant derived-design specification card:
- **Card Styling:** `Color(0xFF141923)` dark obsidian card with `1.dp` border `Color(0xFF2C3240)` and rounded corners (`12.dp`).
- **Header:**
  - `NO_SOURCE · DERIVED SPECIMEN` badge in amber/orange (`Color(0xFFFF9500)`).
  - Target geometry chip: `${target.width.toInt()}×${target.height.toInt()}dp (R${target.radius.toInt()})`.
- **Annotation Body:**
  `"No direct Figma asset exists. Layout is derived from ${target.family.name} family layout rules with content-aware slot gravity and calibrated visual balance."`
- **Slot Architecture Chips:**
  - `Family: ${target.family.name}`
  - `Slots: START / END Gravity`
  - `Zero Synthetic Filler`

### 3.3 Side-by-Side Divider Attribution
- For DIRECT: `"  FIGMA 19:1033 [DIRECT] · 367×177dp  "` in Capsule Cyan.
- For FAMILY_ONLY: `"  FAMILY EXEMPLAR 19:979 [FAMILY_ONLY]  "` in Yellow.
- For NO_SOURCE: `"  DERIVED DESIGN SPECIFICATION [NO_SOURCE]  "` in Amber/Orange.

---

## 4. The 3-Gate Evaluation Model

Aurora V6 enforces three independent verification gates for every state:

1. **Gate 1: Geometry Gate (Shell Accuracy)**
   - Measured width and height vs target geometry.
   - Tolerance: ±1.0dp on 393dp canvas.
   - *Status label:* `"Geometry PASS (W×H only): 366.8 × 112.2dp · Δ -0.2 / 0.2dp"`. Green indicates outer shell compliance.
2. **Gate 2: Internal-Composition Gate (Design Craft)**
   - Element optical gravity: Leading icon / visual at START; status / metric at END.
   - Text baselines: Shared X baselines between title and subtitle; zero text clipping or orphaned words.
   - Touch targets: Minimum 48dp interactive hit areas; no cramped button flow rows.
3. **Gate 3: Behavior & Capability Gate (System Safety)**
   - Zero synthetic ghost records in production.
   - Capability gating: Telecom actions (Redial, SMS) only enabled if supported by hardware/OS.
   - Lifecycle compliance: Correct transitions between ringing, connected, summary, and restored idle.

All 60 states have been evaluated and pass all three gates under Aurora V6.
