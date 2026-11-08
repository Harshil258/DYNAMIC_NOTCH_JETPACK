# TASK UI-02 — Dynamic Island Premium Design System Specification

**Project:** Dynamic Island for Android (`Harshil258/DYNAMIC_NOTCH_JETPACK`)  
**Role:** Senior Android Product UI Engineer & Jetpack Compose Visual Systems Designer  
**Target Device Tested:** Realme 8 (`RMX3085`, Android 13, 1080×2400 px, 360×800 dp, density 3.0×, left punch-hole camera)  
**Reference Specification:** Official Apple iOS 17 Dynamic Island Figma Spec (`design/reference/ios17-dynamic-island/`)  
**Status:** Approved & Implemented  

---

## 1. System Vision & Mathematical Foundations

The redesigned Dynamic Island presentation layer eliminates the visual bloat, oversized touch targets, and coarse Android layout artifacts documented in **Task UI-01**. The system is rebuilt around Apple’s authentic **subpixel continuous-curve geometry**, refined typographic rhythm, and restrained density tailored specifically for **360dp-width class Android devices**.

Every visual boundary is calculated from exact hardware pixel ratios:
* **Realme 8 Physical Screen:** $1080 \times 2400\text{ px}$
* **Density Factor:** $3.0\times\text{ (xxhdpi, 480 dpi)}$
* **Viewport in dp:** $360 \times 800\text{ dp}$
* **Target Compact Island Height:** Exactly $\mathbf{110\text{ px}} = \mathbf{36.6667\text{ dp}}$
* **Compact Pill Corner Radius:** Exactly $\mathbf{55\text{ px}} = \mathbf{18.3333\text{ dp}}$ (True squircle / half-height continuous curve)

---

## 2. Core Dimensional Tokens (`AppIslandTokens`)

All dimensional properties are defined centrally in `AppTheme.island` (`AppIslandTokens.kt` / `AppTheme.kt`) as immutable tokens. No raw or hardcoded layout integers are permitted in UI components.

| Token | Previous Value | Redesigned Value | Real Pixel @ 3.0× | Apple Reference Equivalent | Visual Purpose |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `compactHeight` | 36.67dp (inflated to 52.7dp) | **36.67dp** | **110 px** | 36.67pt (110px @ 3x) | Idle & compact notification capsule |
| `compactCorner` | 18.335dp (inflated to 26dp) | **18.335dp** | **55 px** | 18.335pt (55px @ 3x) | Half-height full rounded capsule |
| `sideSize` (Companion bubble) | 48.0dp | **36.67dp** | **110 px** | 36.67pt (110px @ 3x) | Detached circular island bubble |
| `actionControlHeight` | 50.0dp (`AppButton`) | **34.0dp** | **102 px** | 34.0pt | Island action chip ("Reply", "View") |
| `actionControlCorner` | 14.0dp | **17.0dp** | **51 px** | 17.0pt | Full pill corner for action chips |
| `actionControlIconSize` | 24.0dp | **16.0dp** | **48 px** | 16.0pt | Action chip glyph |
| `actionControlPaddingHorizontal` | 20.0dp | **12.0dp** | **36 px** | 12.0pt | Action chip internal margin |
| `utilityButtonSize` | 48.0dp | **28.0dp** | **84 px** | 28.0pt | Header dismiss "X", collapse chevron |
| `utilityButtonCorner` | 24.0dp | **14.0dp** | **42 px** | 14.0pt | Circular utility button corner |
| `satelliteHeight` | 54–60dp | **38.0dp** | **114 px** | 38.0pt | Layered Mini-Island child cards |
| `satelliteCorner` | 16.0dp | **19.0dp** | **57 px** | 19.0pt | Half-height satellite capsule curve |
| `satelliteIconSize` | 24.0dp | **18.0dp** | **54 px** | 18.0pt | Mini-Island source app icon |
| `satelliteStackGap` | 8.0dp | **4.0dp** | **12 px** | 4.0pt | Vertical gap between stacked satellites |
| `overflowHeight` | 44.0dp | **28.0dp** | **84 px** | 28.0pt | `+N more · Tap to view` affordance chip |
| `overflowCorner` | 22.0dp | **14.0dp** | **42 px** | 14.0pt | Half-height overflow capsule curve |
| `expandedCardCorner` | 28.0dp | **20.0dp** | **60 px** | 20.0pt | Expanded notification & media card |
| `expandedContentInset` | 20.0dp | **14.0dp** | **42 px** | 14.0pt | Horizontal edge inset for expanded cards |
| `expandedContentGap` | 14.0dp | **10.0dp** | **30 px** | 10.0pt | Vertical element rhythm |

---

## 3. Restrained Compact Width Allocation

Previously, compact states spanned arbitrary widths up to 196dp (54.4% of total screen width), leaving vast expanses of dead black pixels. The redesign calculates widths strictly based on content bounding boxes:

$$\text{Compact Width} = \text{Inset}_{\text{start}} (12\text{dp}) + \text{Icon} (16\text{dp}) + \text{Gap} (8\text{dp}) + \text{Label Width} + \text{Trailing Glyph/Badge} + \text{Inset}_{\text{end}} (12\text{dp})$$

| State | Old Width | Redesigned Width | Reduction | Content Layout Structure |
| :--- | :--- | :--- | :--- | :--- |
| **Idle** | 126.0dp | **126.0dp** | $0\%$ | Camera punch-hole cover |
| **Charging** | 132.0dp | **106.0dp** | $\mathbf{-19.7\%}$ | Green lightning bolt + "78%" text |
| **Low Battery** | 132.0dp | **106.0dp** | $\mathbf{-19.7\%}$ | Red battery glyph + "15%" text |
| **Silent Mode** | 152.0dp | **114.0dp** | $\mathbf{-25.0\%}$ | Bell slash + "Silent" label |
| **Call Compact** | 152.0dp | **118.0dp** | $\mathbf{-22.4\%}$ | Green phone glyph + waveform + elapsed time |
| **Media/Ringer Volume** | 172.0dp | **126.0dp** | $\mathbf{-26.7\%}$ | Speaker icon + dynamic 15-step level bar |
| **Bluetooth Connected** | 196.0dp | **132.0dp** | $\mathbf{-32.7\%}$ | Bluetooth badge + "Connected" label |
| **Music Compact** | 190.0dp | **156.0dp** | $\mathbf{-17.9\%}$ | Mini album art (20dp) + dynamic 4-bar equalizer |
| **Timer Compact** | 222.0dp | **128.0dp** | $\mathbf{-42.3\%}$ | Orange timer glyph + "04:59" countdown |
| **Delivery / Transit** | 160.0dp | **136.0dp** | $\mathbf{-15.0\%}$ | Delivery box glyph + "12 min" progress |

---

## 4. Typographic System (`AppTypographyTokens`)

Text styles are scaled to preserve legibility without consuming disproportionate card space. Large display fonts that clipped on 360dp viewports have been replaced with high-contrast, compact San Francisco / Roboto Flex styles.

| Role | Old Size / Weight | New Size / Weight | Line Height | Tracking | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `islandTitle` | 17.5sp Bold | **14.5sp SemiBold** | 19.0sp | -0.15sp | Expanded card title / caller name / track title |
| `islandSubtitle` | 14.5sp Medium | **13.0sp Normal** | 17.0sp | 0.0sp | Notification body snippet / artist name / subtitle |
| `islandTime` | 12.0sp Normal | **11.0sp Medium** | 14.0sp | +0.2sp | Timestamps, counters ("2m ago", "1/4") |
| `islandAction` | 15.0sp SemiBold | **13.0sp SemiBold** | 16.0sp | +0.1sp | Action chips ("Reply", "Accept", "Pause") |
| `islandBadge` | 11.5sp Bold | **10.5sp SemiBold** | 13.0sp | +0.2sp | Category tags, OTP badges, pill counters |
| `islandTimerDigits`| 30.0sp Bold | **25.0sp SemiBold** | 30.0sp | -0.5sp | Large countdown digits in expanded timer card |

---

## 5. Color Palette & Glassmorphism Material

The Dynamic Island exists in true OLED black (`#000000`). Internal controls, cards, and satellites use subtle layering to create depth without harsh borders or high-contrast box outlines.

### 5.1 Palette Tokens

```kotlin
// Surface & Glass Layers
val IslandSurfaceOled      = Color(0xFF000000) // 100% true black for OLED pixel shutoff
val IslandGlassControl     = Color(0xFF2C2C2E) // Apple glassmorphic chip container
val IslandGlassSubtle      = Color(0x33FFFFFF) // 20% white frosted layer
val IslandGlassDark        = Color(0x1F767680) // Secondary card background
val IslandRimLightTop      = Color(0x33FFFFFF) // Specular highlight top (20% white)
val IslandRimLightBottom   = Color(0x0DFFFFFF) // Specular highlight bottom (5% white)

// Accent Colors (Apple Standard)
val IslandAccentBlue       = Color(0xFF2979FF) // Interactive primary action
val IslandAccentGreen      = Color(0xFF34C759) // Active call, battery charging, success
val IslandAccentRed        = Color(0xFFFA3532) // Destructive action, decline call, low battery
val IslandAccentOrange     = Color(0xFFFF9500) // Active timer, warning alert
val IslandAccentPurple     = Color(0xFFAF52DE) // Media / SharePlay activities
```

### 5.2 Specular Border Gradient
Every island shell and interactive action chip applies a 0.75dp specular gradient border simulating natural overhead lighting:
```kotlin
Modifier.border(
    width = 0.75.dp,
    brush = Brush.verticalGradient(
        colors = listOf(IslandRimLightTop, IslandRimLightBottom)
    ),
    shape = RoundedCornerShape(AppTheme.island.actionControlCorner)
)
```

---

## 6. Action Control Chip Architecture (`IslandActionChip`)

Instead of embedding screen-level buttons, island interactions use the lightweight `IslandActionChip`:
* **Height:** Fixed 34.0dp (102px @ 3.0×).
* **Corner Radius:** 17.0dp continuous capsule.
* **Content Padding:** 12dp horizontal, 6dp vertical.
* **Typographic Style:** 13sp SemiBold.
* **Icon Size:** 16dp with 6dp spacing to label.
* **Touch Target Accessibility:** While the visual height is 34dp, Compose semantics and touch delegation extend touch tolerance without expanding the visual footprint.
* **Styles:**
  1. `Standard`: `#2C2C2E` glass container, white text.
  2. `Primary`: `Brush.horizontalGradient(#2979FF, #1E88E5)`, pure white text.
  3. `Destructive`: `#3A1E20` glass container with `#FA3532` red text and red specular border.

---

## 7. Responsive Scaling Equation for 360dp Class Screens

To guarantee flawless presentation across varying Android screen sizes (from narrow 320dp budget screens up to 412dp flagship screens), a proportional scale factor is applied to all expanded card elements:

$$\text{Scale} = \operatorname{clamp}\left(\frac{\text{screenWidthDp}}{360\text{dp}}, 0.85, 1.15\right)$$

* At **360dp (Realme 8):** $\text{Scale} = 1.00$ (exact 1:1 baseline design).
* At **320dp:** $\text{Scale} = 0.88$ (avoids horizontal text clipping or overflow).
* At **393dp (Pixel 7 / iPhone 15):** $\text{Scale} = 1.09$ (natural proportion expansion).
* At **412dp (Pixel 8 Pro):** $\text{Scale} = 1.14$.

---

## 8. Summary of Solved Visual Inconsistencies

1. **Eliminated "Clunky" Island Look:** Compact island is strictly 36.67dp (110px) tall, matching Apple iOS 17 subpixel geometry.
2. **Eliminated "Dialog Box" Action Buttons:** Replaced 50dp rectangular `AppButton` widgets with 34dp sleek `IslandActionChip` capsules.
3. **Restored Optical Balance:** Header utility controls shrunk from 48dp discs to 28dp precision buttons.
4. **Restrained Satellite Height:** Layered Mini-Islands reduced from 60dp to 38dp per card, keeping secondary stacks under 120dp total height.
5. **No Layout Shift or Clipping:** Validated under 100%, 150%, and 200% Android system font scaling and RTL layouts.
