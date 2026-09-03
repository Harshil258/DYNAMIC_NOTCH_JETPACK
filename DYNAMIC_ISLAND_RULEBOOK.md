# Dynamic Island Design System — Comprehensive Figma Audit & Rule Book

## Reference: iOS 17 Dynamic Island Components (Figma Community)
**Source:** https://www.figma.com/design/z7GyjPFoVfAlEgL3AhaYEr/iOS17-Dynamic-Island-Components--Community-  
**Export Directory:** `islandfigmacomponentofios/`

---

## 1. DEVICE FRAMES & SENSOR CUTOUT GEOMETRY

All measurements below are extracted directly from the Figma SVGs:

| Frame / Component | Viewport (W × H) | Hardware Island Position | Island Size (Idle) | Corner Radius |
|-------------------|------------------|--------------------------|-------------------|---------------|
| **iPhone 15 Pro** | 393 × 852 pt | `x = 133.5, y = 11 pt` | 126 × 36.67 pt | 18.335 pt |
| **iPhone 15 Pro Max** | 430 × 932 pt | `x = 152.0, y = 11 pt` | 126 × 36.67 pt | 18.335 pt |
| **Hardware Sensor Cutout** | Sensor Ellipse + Camera | `w = 125 pt, h = 35.67 pt` | Centered at `y = 11 pt` | 17.835 pt |

> [!IMPORTANT]
> **Key Metric:** The base height of all compact and minimal Dynamic Island capsules is **`36.67 pt`** (rendered as **`37.33 dp`** in Jetpack Compose). Corner radius is exactly **`50% of height (18.335 pt / 18.5 dp)`**, making it a mathematically perfect capsule.

---

## 2. STATE CLASSIFICATIONS & FIGMA AUDIT

### 2.1 Idle Pill State
- **Audit Source:** `Pro Max 430px.svg`
- **Width:** `126.0 dp`
- **Height:** `37.33 dp`
- **Corner Radius:** `18.67 dp` (Capsule 50%)
- **Content:** Pure black pill obscuring the camera and TrueDepth sensors. Specular border `1dp` with 12% opacity white.

---

### 2.2 Minimal State (Split Bubble)
- **Audit Source:** `Minimal.svg`, `Pro Max 430px-1.svg`
- **Left Capsule (Main Pill):**
  - **Width:** `156.0 dp` (starts at `x = 122`, ends at `x = 278` in 430pt frame)
  - **Height:** `36.67 dp`
  - **Corner Radius:** `18.335 dp`
- **Right Detached Bubble:**
  - **Width × Height:** `36.67 × 36.67 dp` (starts at `x = 289`, ends at `x = 325.67`)
  - **Corner Radius:** `18.335 dp` (Perfect circle)
- **Separation Gap:** **`11.0 dp`** (exactly `289 - 278 = 11pt`!)
- **Content:** Leading primary app status in main pill; secondary live activity glyph (e.g. Timer ring countdown) isolated inside the right bubble.

---

### 2.3 Compact States (Unified Live Activity Pill)
- **Audit Source:** `Compact.svg`, `Pro Max 430px.svg`
- **Width:** Dynamically sized to content:
  - **Compact Timer:** `222.0 dp` (`Compact.svg`: `x = 104 to 326`)
  - **Compact Media:** `180.0 - 220.0 dp`
  - **Compact Call:** `160.0 - 190.0 dp`
  - **Maximum Compact Width:** `250.0 dp` (`Pro Max 430px.svg`: `x = 90 to 340`)
- **Height:** `37.33 dp`
- **Corner Radius:** `18.67 dp` (Capsule 50%)
- **Leading Element:**
  - Timer: Circular countdown ring (diameter `21 dp`, stroke `3 dp`, cyan `#67EBF5` to blue `#2A86E6` gradient).
  - Media: `24 × 24 dp` album art squircle (`rx = 6 dp`).
  - Call: `24 × 24 dp` green phone / contact glyph.
- **Trailing Element:**
  - Timer: Digits `"01:45"` in SF Pro Display bold.
  - Media: Live jumping equalizer (3 or 4 neon bars in `#FA2D48` or `#F84BAB`).
  - Call: Live duration counter `"02:45"` in `#37C058`.

---

### 2.4 Expanded States (Full Activity Sheets)

The Figma audit reveals that expanded states have different heights and internal structures depending on their functional domain:

```
┌────────────────────────────────────────────────────────────┐
│                    EXPANDED ISLAND BLUEPRINT               │
│                                                            │
│   ┌───────────────┐                  ┌─────────────────┐   │
│   │ LEADING ZONE  │   CUTOUT ZONE    │  TRAILING ZONE  │   │
│   │ (Avatar/Art)  │  (126 × 37 dp)   │ (Waveform/Ring) │   │
│   └───────────────┘                  └─────────────────┘   │
│                                                            │
│   ┌────────────────────────────────────────────────────┐   │
│   │                 CENTER / SCRUBBER ZONE             │   │
│   │        Title, Subtitle, Progress Scrubber          │   │
│   └────────────────────────────────────────────────────┘   │
│                                                            │
│   ┌────────────────────────────────────────────────────┐   │
│   │                 BOTTOM ACTION ROW                  │   │
│   │         Action Buttons (Pills / Circles)           │   │
│   └────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────┘
```

#### Blueprint Breakdown by State:

| Expanded State | Figma SVG Reference | Width | Height | Corner Radius | Layout & Components |
|---|---|---|---|---|---|
| **Timer Expanded** | `Expanded.svg` | `367 - 408 dp` | **`96.0 dp`** | `44.0 dp` | **Leading:** 58dp Timer ring with `#67EBF5`->`#2A86E6` gradient.<br>**Center:** "Timer" (`#8E8D94`) + "01:45" (`#EBEBF0`, 32sp).<br>**Trailing:** Pause/Stop action button. |
| **Media Player Expanded** | `Dynamic Island.svg` | `367 - 408 dp` | **`177.0 dp`** | `42.0 dp` | **Top:** 53dp Album squircle + "Heat Waves" + "Glass Animals" + 6-bar Pink equalizer (`#F84BAB`->`#B4CDFB`).<br>**Middle:** "0:50" \| 240dp Slider \| "-3:11".<br>**Bottom:** 4 Action icons: Previous, Play/Pause, Next, AirPlay. |
| **FaceTime Audio / Active Call** | `Dynamic Island-2.svg` & `Dynamic Island-3.svg` | `367 - 408 dp` | **`166.0 - 168.0 dp`** | `42.0 dp` | **Top:** 44dp Avatar circle + "Tamia Castillo" + "FaceTime Audio" (`#838388`) + (i) info button.<br>**Bottom:** 5 Circular Action Buttons (50dp): Speaker, Mic, Video, SharePlay, End Call (`#FA3532`). |
| **Media Audio Call Session** | `Dynamic Island-4.svg` | `367 - 408 dp` | **`172.6 dp`** | `42.0 dp` | **Top:** 53dp Album Art + Track "Asia Wild" + Live scrub bar.<br>**Bottom:** 5 Circular Action Buttons: Speaker, Mic, Video, SharePlay, Close (X in `#FA3532`). |
| **Airplane Mode Alert** | `Dynamic Island-5.svg` | `367 - 408 dp` | **`148.0 dp`** | `42.0 dp` | **Top:** Orange Airplane icon (`#FB8B28`) + "Turn Off Airplane Mode" + "to Access Data".<br>**Bottom:** Full-width 43dp charcoal pill button "Open Settings" (`#2C2C2D`). |
| **Screen Mirroring (AirPlay)** | `Dynamic Island-6.svg` | `367 - 408 dp` | **`144.0 dp`** | `42.0 dp` | **Top:** Cyan Dual Screen icon (`#37A3DE`) + "Screen Mirroring" + "MacBook Pro".<br>**Bottom:** Full-width 43dp dark cyan pill button "Stop Mirroring" (`#1A1C2D`, text `#37A3DE`). |
| **Personal Hotspot / Mobile Data** | `Dynamic Island-7.svg` | `367 - 408 dp` | **`162.0 dp`** | `42.0 dp` | **Top:** Green Antenna icon (`#37C058`) + "Mobile Data" + "Turn off Mobile Data to use Wi-Fi".<br>**Bottom:** Dual 43dp pill buttons: "OK" (`#2C2C2D`) and "Settings" (`#1A1C2D`, text `#37A3DE`). |
| **Transit / Train Route** | `Dynamic Island-8.svg` | `367 - 408 dp` | **`142.0 dp`** | `42.0 dp` | **Top:** White Train cabin icon + "Prague Main Train Station".<br>**Bottom:** Full-width 43dp dark red pill button "End Route" (`#1D1011`, text `#FA3532`). |
| **Turn-by-Turn Navigation** | `Dynamic Island-1.svg` | `367 - 408 dp` | **`185.3 dp`** | `42.0 dp` | **Top:** 4 Direction tabs (Turn Left, Straight, Merge, Turn Right).<br>**Middle:** "90 ft", "North", "San Francisco".<br>**Bottom:** 51 × 39 dp route map preview thumbnail. |

---

## 3. COLOR PALETTE & TOKENS (Extracted from SVGs)

```
// Primary Accent Colors
const val AppleRed          = 0xFFFA3532  // End Call, Cancel, Destructive
const val AppleGreen        = 0xFF37C058  // Active Call, Personal Hotspot, Accept
const val AppleOrange       = 0xFFFB8B28  // Airplane Mode, Warning Alerts
const val AppleCyan         = 0xFF37A3DE  // Screen Mirroring, AirPlay Tint
const val AppleTimerCyan    = 0xFF67EBF5  // Timer Ring Start
const val AppleTimerBlue    = 0xFF2A86E6  // Timer Ring End
const val ApplePinkStart    = 0xFFF84BAB  // Music Waveform Gradient Start
const val ApplePinkEnd      = 0xFFB4CDFB  // Music Waveform Gradient End

// Text Colors
const val TextWhitePrimary   = 0xFFFFFFFF  // Primary Titles
const val TextOffWhite       = 0xFFEBEBF0  // Large Numbers (Timer, Counter)
const val TextSecondaryGray  = 0xFF8E8D94  // Labels, Subtitles ("Timer", "FaceTime Audio")
const val TextTertiaryGray   = 0xFF9A9A9A  // Artist, Timestamp counters

// Button & Capsule Backgrounds
const val ButtonGlassDark    = 0xFF2A292D  // 50dp Circle Action Buttons
const val ButtonPillNeutral  = 0xFF2C2C2D  // 43dp Pill Buttons (Settings, OK)
const val ButtonPillCyan     = 0xFF1A1C2D  // 43dp Tinted Cyan Pill ("Stop Mirroring")
const val ButtonPillRed      = 0xFF1D1011  // 43dp Tinted Red Pill ("End Route")
const val TrackBackground    = 0xB23F3F3F  // 70% Dark gray slider track
```

---

## 4. EXTRACTED SVG VECTOR ICONS (Inventory)

All of the following vector paths have been extracted from the Figma SVGs for inclusion in `AppleIcons.kt`:

1. **`Airplane`** (`Dynamic Island-5.svg`):
   Sleek fuselage, swept delta wings, angled tail stabilizer. Perfect for Airplane Mode and Flight tracking.
2. **`ScreenMirroring`** (`Dynamic Island-6.svg`):
   Two overlapping rounded rectangular displays representing AirPlay Screen Mirroring.
3. **`PersonalHotspot`** (`Dynamic Island-7.svg`):
   Central antenna mast with concentric broadcast signal arcs.
4. **`Train` / `Transit`** (`Dynamic Island-8.svg`):
   Front elevation of high-speed metro train with windshield, headlights, and tracks.
5. **`VideoCamera` / `FaceTimeVideo`** (`Dynamic Island-2/3.svg`):
   Rounded camera body with forward-facing trapezoidal lens cone.
6. **`SharePlay`** (`Dynamic Island-2/3.svg`):
   Profile of person in front of broadcast screen with transmission waves.
7. **`Microphone`** (`Dynamic Island-2/3.svg`):
   Capsule mic body, cradle ring, vertical stem, and base.
8. **`EndCall`** (`Dynamic Island-2/3.svg`):
   Horizontal downward-curved handset receiver indicating hangup.
9. **`Info`** (`Dynamic Island-2/3.svg`):
   Outer circle ring containing centered lower-case "i" glyph.
10. **`NavigationTurnLeft` & `NavigationTurnRight`** (`Dynamic Island-1.svg`):
    Bold directional arrows with 90° curvature for navigation directions.
11. **`Media Controls`** (`Dynamic Island.svg`):
    Double-triangle rewind (`<<`), solid play triangle (`▶`), double-triangle forward (`>>`), AirPlay casting monitor with upward-pointing triangle.
12. **`TimerProgressRing`** (`Compact.svg`, `Expanded.svg`):
    Dynamic Canvas-drawn arc with gradient `#67EBF5` to `#2A86E6` and subtle drop shadow glow.

---

## 5. RESPONSIVE CONSTRAINTS & ZERO-CLIPPING RULES

To ensure that **NO island state ever clips, overflows, or gets truncated improperly** on any Android screen size:

1. **Horizontal Scaling:**
   - Always wrap root card content in `BoxWithConstraints`.
   - Maximum width is dynamically derived via `maxWidth.coerceAtMost(maxExpanded)`.
   - For compact states, width is bounded by `widthIn(min = 126.dp, max = 250.dp)`.
2. **Text Ellipsis & Weights:**
   - Every title and subtitle must have `maxLines = 1` and `overflow = TextOverflow.Ellipsis`.
   - Text containers must take `Modifier.weight(1f)` so action buttons and badges always retain their fixed sizing.
3. **Action Button Rows:**
   - 5-button control rows (Call, Audio) must distribute spacing evenly with `Arrangement.SpaceBetween` or `Arrangement.SpaceEvenly`.
   - Button touch targets are minimum `44 × 44 dp` (visual radius `22 - 25 dp`).
4. **Animation Springs:**
   - Morphing between states must use iOS liquid spring physics:
     - Stiffness: `Spring.StiffnessMediumLow` (or `400f`)
     - Damping Ratio: `Spring.DampingRatioLowBouncy` (or `0.78f`)
   - Content fading during size morphing must use `Crossfade` or `AnimatedContent` with `fadeIn(tween(180)) + scaleIn(0.92f)` and `fadeOut(tween(120))`.
