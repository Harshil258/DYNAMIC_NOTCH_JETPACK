# UI_01 — Realme 8 Hardware Scaling Diagnostics & Root-Cause Analysis

**Device:** Realme 8 (`RMX3085`)  
**OS:** Android 13 (`SDK 33`, Build: `RMX3085_11_F.15`)  
**Examiner:** ChatGPT — Principal Product Architect  
**Branch:** `visual-qa/task-ui-01-inventory`  
**Date:** November 8, 2026  

---

## 1. Hardware & WindowManager Physical Ground Truth

Every metric below is extracted directly from the physical hardware via `adb shell dumpsys display` and `adb shell dumpsys window displays`:

```
Model: RMX3085
Brand: realme (Manufacturer: realme, Device: RMX3085L1)
Android Version: 13 (SDK 33)
Physical Screen Size: 1080 x 2400 pixels
Physical Display Density: 480 dpi (density scale factor = 3.0)
System Font Scale: 1.0 (Standard default)
Display Refresh Rate: 60.0 Hz
Color Mode: 0 (Supported modes: [0])
Display Cutout Type: Single Punch-Hole (Top-Left)
Status Bar Inset: Rect(0, 110 - 0, 0) -> Height = 110 px (36.67 dp)
Navigation Bar Inset: Height = 132 px (44.0 dp)
Cutout Bounding Box: Rect(-4, 0 - 154, 110) px -> [-1.33, 0.0, 51.33, 36.67] dp
Cutout Path Spec: M -544,0 L -544,110 L -386,110 L -386,0 Z
```

### Logical Viewport Calculations:
$$\text{Width (dp)} = \frac{1080\text{ px}}{3.0} = 360.0\text{ dp}$$
$$\text{Height (dp)} = \frac{2400\text{ px}}{3.0} = 800.0\text{ dp}$$
$$\text{Usable Height (without system bars)} = 800 - 36.67\text{ (status)} - 44.0\text{ (nav)} = 719.33\text{ dp}$$

---

## 2. Display Cutout Geometry & Hardware Disparity

The Realme 8 utilizes a single circular punch-hole selfie camera located at the **top-left corner**:
* **X-range:** `-4` to `154` px (`-1.33` to `51.33` dp).
* **Y-range:** `0` to `110` px (`0.0` to `36.67` dp).
* **Center coordinates:** $X = 75\text{ px}$ ($25.0\text{ dp}$), $Y = 55\text{ px}$ ($18.33\text{ dp}$).
* **Diameter:** $110\text{ px}$ ($36.67\text{ dp}$).

### The Core Physical Disparity:
In Apple iOS, the Dynamic Island encapsulates the **centered** TrueDepth / pill camera array (`x = center`, `y = 11dp`, width `126dp`, height `36.67dp`).
On the Realme 8:
1. The physical camera is positioned at $X = 25\text{ dp}$ (far left).
2. The Android Dynamic Island in this codebase is horizontally centered at $X = 180\text{ dp}$ ($X_{\text{start}} \approx 93.5\text{ dp}$, $X_{\text{end}} \approx 266.5\text{ dp}$).
3. **Result:** The centered black island capsule floats in completely empty OLED pixels at the top-center, while the actual physical camera cutout sits visibly unmasked on the far left. This makes the island appear visually foreign, oversized, and intrusive because it does not mask the hardware hole it is supposed to camouflage.

---

## 3. Root-Cause Analysis: Why Does the UI Look "Oversized"?

The product owner observed that on the Realme 8, "the Dynamic Islands, buttons, typography, spacing, and overall application interface appear oversized, poorly aligned, and visually unprofessional."

We systematically investigated four hypotheses:

### Hypothesis A: Android OS Display Zoom or Accessibility Text Scaling?
* **Investigation:** Inspected `settings get system font_scale` and `wm density`.
* **Finding:** `font_scale = 1.0` and `wm density = 480`. OS display zoom is at 100% factory default.
* **Verdict:** **RULED OUT**. The OS is not applying display magnification.

### Hypothesis B: Jetpack Compose LocalDensity Calculation Error?
* **Investigation:** Inspected Compose `LocalDensity.current.density`.
* **Finding:** Density is exactly `3.0f`. Every `1.dp` maps to exactly `3.px`. `1.sp` maps to `3.px`. Subpixel rounding matches hardware rasterization.
* **Verdict:** **RULED OUT**. Compose scaling is mathematically exact.

### Hypothesis C: Internal Codebase Token Inflation (The Actual Root Cause)
* **Investigation:** Analyzed `AppIslandTokens.kt`, `AppTheme.kt`, `AppButton.kt`, `AppControls.kt`, and `MainContainerScreen.kt`.
* **Finding:** The inflation is caused entirely by hardcoded, oversized dimensions in the application design tokens:

#### 1. Dynamic Island Capsule Height:
* **Apple Reference:** $36.67\text{ dp}$ ($110\text{ px}$ on Realme 8).
* **Aurora App Codebase:** $52.7\text{ dp}$ ($158\text{ px}$ on Realme 8).
* **Inflation:** $+16.03\text{ dp}$ ($+43.7\%$ oversized). The compact island is nearly 1.5× as tall as Apple's compact pill!

#### 2. Dynamic Island Capsule Width:
* **Apple Reference:** $126.0\text{ dp}$ ($378\text{ px}$).
* **Aurora App Codebase:** $173.0\text{ dp}$ ($519\text{ px}$) to $212.0\text{ dp}$ ($636\text{ px}$).
* **Inflation:** $+47.0\text{ dp}$ ($+37.3\%$ to $+68.3\%$ oversized).

#### 3. Primary Buttons (`AppButton.kt`):
* **Standard Apple iOS button height:** $44.0\text{ dp}$ to $50.0\text{ dp}$.
* **Aurora AppButton height:** $56.0\text{ dp}$ with $24\text{ dp}$ horizontal padding and $16\text{ dp}$ vertical padding.
* **Inflation:** Two stacked buttons consume $112\text{ dp} + 12\text{ dp} = 124\text{ dp}$ of vertical viewport, eating $17.2\%$ of the usable screen height!

#### 4. Fixed Viewport Reserves:
* Status bar: $36.67\text{ dp}$
* TopBar: $56.0\text{ dp}$
* Bottom dock (`BottomDockInset`): $92.0\text{ dp}$
* **Total reserved chrome:** $184.67\text{ dp}$ out of $800\text{ dp}$ ($23.1\%$ of the display is permanently locked chrome).
* When large cards with $16\text{ dp}$ padding and $56\text{ dp}$ buttons are placed in the remaining $615.33\text{ dp}$, content quickly runs out of room, forcing excessive vertical scrolling and creating a heavy, cramped impression.

---

## 4. Platform Dimension Comparison Table

| Property | Apple iPhone 14 Pro | Realme 8 (Target Hardware) | Aurora App Implementation | Disparity vs Apple Reference |
|---|---|---|---|---|
| **Screen Resolution** | 1179 × 2556 px | 1080 × 2400 px | 1080 × 2400 px | — |
| **Display Density** | ~460 ppi (3.0×) | 409 dpi / 480 density (3.0×) | 3.0× | — |
| **Logical Viewport** | 393 × 852 dp | 360 × 800 dp | 360 × 800 dp | Realme is 33dp narrower |
| **Status Bar Height** | 54.0 dp (162 px) | 36.67 dp (110 px) | 36.67 dp | Realme bar is 17.33dp shorter |
| **Cutout Position** | Centered pill | Top-left hole | Centered pill (floating) | Hardware mismatch |
| **Compact Island Height** | **36.67 dp** (110 px) | 36.67 dp (110 px) | **52.7 dp** (158 px) | **+16.03 dp (+43.7%)** |
| **Compact Island Width** | **126.0 dp** (378 px) | 126.0 dp (378 px) | **173.0 dp** (519 px) | **+47.0 dp (+37.3%)** |
| **Compact Corner Radius** | **18.33 dp** (55 px) | 18.33 dp (55 px) | **26.3 dp** (79 px) | **+7.97 dp (+43.5%)** |
| **Primary Button Height** | 48.0 dp | 48.0 dp | **56.0 dp** | **+8.0 dp (+16.7%)** |
| **Bottom Navigation Dock** | 49.0 dp (tab bar) | 48.0 dp | **64.0 dp** (+ 28dp fade = 92dp) | **+15.0 dp (+30.6%)** |

---

## 5. Architectural Recommendation for Subsequent Redesign

1. **Re-align Compact Island Tokens to True 36.67dp Height:**
   Update `AppIslandTokens` so `compactHeight = 36.67.dp` (110px @ 3.0×), exactly matching the status bar height on the Realme 8.
2. **Reduce Compact Pill Width to 126dp:**
   Allow the compact island to rest at $126\text{ dp}$ default width, scaling dynamically only when content demands it.
3. **Offer Notch Pinning Mode for Left Punch-Hole:**
   Provide an option to anchor the compact pill to $X = 0$ with $52\text{ dp}$ width so it directly surrounds the physical Realme 8 punch-hole camera.
4. **Scale Down Buttons from 56dp to 48dp:**
   Reduce primary button height from $56\text{ dp}$ to $48\text{ dp}$ (meets WCAG 48dp minimum touch target while freeing substantial vertical space).
5. **Optimize Bottom Dock Reservation:**
   Streamline `BottomDockInset` from $92\text{ dp}$ to $72\text{ dp}$ to expand the active content viewport by $20\text{ dp}$.
