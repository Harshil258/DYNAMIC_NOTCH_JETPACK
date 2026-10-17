# Aurora Dynamic Island — Floating Island Behavior, Calibration & Interaction Contract

## 1. Physical Notch Calibration & Alignment Architecture
Android devices feature diverse display hardware: centered punch holes, left-aligned camera holes, teardrop notches, and pill cutouts. Aurora Dynamic Island provides exact, hardware-aligned calibration:

1. **Sub-Pixel Precision Controls (`PrecisePixelControl`)**:
   - Sliders combined with granular `[−]` and `[+]` stepping buttons allow single-pixel and single-percentage adjustments.
   - **Vertical Offset**: `0dp` to `120dp` (default `12dp`).
   - **Horizontal Offset**: `-60dp` to `+60dp` (default `0dp`, Pro feature).
   - **Width Scale**: `60%` to `150%` (default `100%`).
2. **Notch Styling**:
   - **Normal Floating Pill**: Fully rounded pill floating below or around the status bar.
   - **Monobrow (Flat-Top)**: Top edge hugs the upper bezel flush with the physical screen frame (`notchFlatTop`).
3. **Custom RGB Color Engine (`IslandColorPicker`)**:
   - Supports custom 32-bit ARGB background and foreground accent colors.
   - Live ARGB hex preview with interactive Red, Green, and Blue channel sliders.
4. **Idle Auto-Hide**:
   - When enabled (`idleAutoHideEnabled`), the island collapses and fades out after an idle timeout (`idleAutoHideTimeoutSeconds`, 3s to 60s).
   - Automatically re-expands upon incoming notifications, media playback, charging connection, or volume changes.

---

## 2. Expiry Island Overlay Contract (`IslandState.PremiumExpiry`)
When a free user's 7-day trial or rewarded pass expires:

1. **Trigger Condition**:
   - User taps the dynamic island notch when `snapshot.isPro == false` and `premiumExpiryIslandEnabled == true`.
   - Proactive alert: when a trial or pass expires during active device usage.
2. **Island Visual Presentation**:
   - **Icon**: Aurora Golden Crown (`AppleGlyph.Crown`).
   - **Title**: `"Aurora Pro Access"`
   - **Subtitle**: `"Trial expired · Unlock or watch ads"`
   - **Action Button 1**: `"Watch Ads"` (opens `AdRewardDialog` bottom sheet).
   - **Action Button 2**: `"Buy Premium"` (opens `PremiumScreen` paywall).
3. **Zero Advertising Contamination**:
   - The Expiry Island is a native Jetpack Compose floating pill. It contains **no advertising SDK code**, banners, or sponsored links.

---

## 3. Strict Scope Constraints
> [!IMPORTANT]
> **Zero Cricket Policy**
> Under no circumstance may any cricket score, cricket API, live cricket match notification, or cricket-related UI be implemented or reintroduced into Aurora Dynamic Island. All live activities are restricted to generic system notifications, timers, and authorized media playback.
