# Dynamic Island Design System — Comprehensive Figma Audit & Rule Book

## Reference: iOS 17 Dynamic Island Components (Figma Community)
**Source:** https://www.figma.com/design/z7GyjPFoVfAlEgL3AhaYEr/iOS17-Dynamic-Island-Components--Community-  
**Primary export directory:** `design/reference/ios17-dynamic-island/`
**Secondary export directory:** `design/reference/ios17-dynamic-island-alt/`
**Extracted asset set:** `design/icons/` (see `design/ASSETS.md`)

> The two Figma export folders previously sat in the repository root as
> `iOS17 Dynamic Island Components (Community) (3)/` and
> `islandfigmacomponentofios/`. They now live under `design/reference/` with the
> twelve byte-identical duplicates removed; every original filename is
> unchanged, so all references below still resolve.

---

## 1. DEVICE FRAMES & SENSOR CUTOUT GEOMETRY

All measurements below are extracted directly from the Figma SVGs:

| Frame / Component | Viewport (W × H) | Hardware Island Position | Island Size (Idle) | Corner Radius |
|-------------------|------------------|--------------------------|-------------------|---------------|
| **iPhone 15 Pro** | 393 × 852 pt | `x = 133.5, y = 11 pt` | 126 × 36.67 pt | 18.335 pt |
| **iPhone 15 Pro Max** | 430 × 932 pt | `x = 152.0, y = 11 pt` | 126 × 36.67 pt | 18.335 pt |
| **Hardware Sensor Cutout** | Sensor Ellipse + Camera | `w = 125 pt, h = 35.67 pt` | Centered at `y = 11 pt` | 17.835 pt |

> [!IMPORTANT]
> **Key Metric:** The base height of all compact and minimal Dynamic Island capsules is **`36.67 pt`** and is rendered as **`36.67 dp`** in Jetpack Compose. Corner radius is always 50% of the rendered height, making it a mathematically perfect capsule.

### 1.1 Five supplied frame exports — measured coverage

The percentages below describe the visible black island geometry relative to
each complete phone canvas. They are useful for auditing, not as CSS/Compose
sizing inputs: iOS keeps compact geometry physically stable while screen sizes
change.

| SVG | Presentation | Island bounds | Width of screen | Height of screen | Top offset |
|---|---|---:|---:|---:|---:|
| `iPhone 15 Pro.svg` | Expanded media | `367 × 177` at `(14, 10)` | `93.384%` | `20.775%` | `1.174%` |
| `iPhone 15 Pro-1.svg` | Compact media | `190 × 38` at `(102, 10)` | `48.346%` | `4.460%` | `1.174%` |
| `Minimal.svg` | Complete split group | `203.67 × 36.67` at `(113, 11)` | `47.365%` | `3.935%` | `1.180%` |
| `Minimal.svg` | Main capsule only | `156 × 36.67` | `36.279%` | `3.935%` | `1.180%` |
| `Minimal.svg` | Detached bubble only | `36.67 × 36.67` | `8.528%` | `3.935%` | `1.180%` |
| `Pro Max 430px-1.svg` | Complete split group | `203.67 × 36.67` at `(122, 11)` | `47.365%` | `3.935%` | `1.180%` |
| `Pro Max 430px-2.svg` | Expanded sheet | `408 × 160` at `(11, 0)` | `94.884%` | `17.167%` | `0%` |

The compact exports vary from `36.67pt` to `38pt` because of Figma/export
rounding. `Compact Presentation.svg` explicitly uses `126 × 36.67pt` on both
393pt and 430pt device frames, so the app uses `36.67dp` as the canonical
compact height. Idle, minimal, and every compact activity consume that single
token. Expanded activities intentionally retain content-specific heights;
forcing a music player and a one-line alert to the same height would not match
iOS.

### 1.2 Lovable TSX cross-check

The live Lovable project was read in Brave and compared with these exports. Its
`states.ts` confirms the same 21 expanded assets and records each source canvas,
black-capsule rectangle, corner radius, and 38pt minimal-bubble asset. Its
`DynamicIsland.tsx` renders the SVG at the source canvas size and crops it with
`left = -x` and `top = -y`, which preserves the artwork's exact internal
coordinates. The Android implementation keeps that geometry as shared tokens
and state families because raw web `<img>` cropping is not available in
Jetpack Compose without an SVG runtime.

The TSX idle constant is `126 × 37.33` while the Figma device-frame exports use
`126 × 36.67`. That is export rounding, not a second runtime height. Compose
normalizes all compact/minimal states to `36.67dp`, including the split bubble,
so state changes never create a one-pixel vertical jump. The TSX motion values
(`stiffness: 460`, `damping: 32`, `mass: 1.05`, 200ms cubic content reveal) are
mirrored by `AppMotion` with the equivalent Compose damping ratio and easing.

---

## 2. STATE CLASSIFICATIONS & FIGMA AUDIT

### 2.1 Idle Pill State
- **Audit Source:** `ios17-dynamic-island-alt/Pro Max 430px.svg`
- **Width:** `126.0 dp`
- **Height:** `36.67 dp`
- **Corner Radius:** `18.335 dp` (Capsule 50%)
- **Content:** Pure black pill obscuring the camera and TrueDepth sensors. Specular border `1dp` with 12% opacity white.

---

### 2.2 Minimal State (Split Bubble)
- **Audit Source:** `Dynamic Island/Minimal.svg`, `ios17-dynamic-island-alt/Pro Max 430px-1.svg`
- **Left Capsule (Main Pill):**
  - **Width:** `156.0 dp` (starts at `x = 122`, ends at `x = 278` in 430pt frame)
  - **Height:** `36.67 dp`
  - **Corner Radius:** `18.335 dp`
- **Right Detached Bubble:**
  - **Width × Height:** `36.67 × 36.67 dp` (starts at `x = 289`, ends at `x = 325.67`)
  - **Corner Radius:** `18.335 dp` (Perfect circle)
- **Separation Gap:** presentation/prototype exports repeatedly use **`11pt`**; one concrete iPhone 15 Pro export uses `6pt`. The app uses the repeated **`11dp`** token.
- **Content:** Leading primary app status in main pill; secondary live activity glyph (e.g. Timer ring countdown) isolated inside the right bubble.

---

### 2.3 Compact States (Unified Live Activity Pill)
- **Audit Source:** `ios17-dynamic-island-alt/Compact.svg`, `ios17-dynamic-island-alt/Pro Max 430px.svg`
- **Width:** Dynamically sized to content:
  - **Compact Timer:** `222.0 dp` (`Compact.svg`: `x = 104 to 326`)
  - **Compact Media:** `180.0 - 220.0 dp`
  - **Compact Call:** `160.0 - 190.0 dp`
  - **Maximum Compact Width:** `250.0 dp` (`Pro Max 430px.svg`: `x = 90 to 340`)
- **Height:** `36.67 dp`
- **Corner Radius:** `18.335 dp` (Capsule 50%)
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

`Components.svg` makes those regions measurable on its `408 × 160`, `r44`
template:

- Inner safe boundary: `395 × 147` at `6.5pt` from the shell.
- Sensor/exclusion guide: `125 × 35.67`, `r17.835`, centered at the top.
- Leading/center boundary: `x ≈ 99.5pt` inside the shell.
- Center/trailing boundary: `x ≈ 307.5pt` inside the shell.
- Center slot: approximately `y=37.17-67.5pt` inside the shell.
- Bottom region begins at about `y=103.5pt` and spans the full safe width.

The iPhone screenshots also contain a smaller red dashed `106 × 29.8037`
guide. It is an annotation/exclusion overlay, not the visible island body, and
must not be used to size the runtime capsule.

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

### 2.5 Extended 30-SVG corpus map

The following filenames are scoped to
`design/reference/ios17-dynamic-island/Dynamic Island/`. This matters
because the older export folder reuses some filenames for different states.
All measurements describe the black body only and exclude the SVG canvas and
its 8pt blurred drop-shadow padding.

| SVG | State | Black body | Key visual assets |
|---|---|---:|---|
| `Dynamic Island.svg` | AirDrop received | `367 × 86`, `r43` | sender/app tile, blue status dot, blue trailing badge |
| `Dynamic Island-1.svg` | Timer | `367 × 85.73`, `r42.86` | orange pause, gray cancel, orange time |
| `Dynamic Island-2.svg` | AirPods connected | `367 × 86`, `r43` | device artwork, connection label, green battery percent |
| `Dynamic Island-3.svg` | Audio recording | `367 × 86`, `r43` | red waveform, elapsed time, red stop control |
| `Dynamic Island-4.svg` | Screen recording | `367 × 86`, `r43` | red live dot/time, title, red stop control |
| `Dynamic Island-5.svg` | Shortcut complete | `367 × 85.08`, `r42.54` | stacked shortcut mark, progress, check ring |
| `Dynamic Island-6.svg` | Incoming call | `367 × 86`, `r43` | 44pt avatar, red decline and green answer buttons |
| `Dynamic Island-7.svg` | Satellite connected | `367 × 86`, `r43` | satellite/locator artwork, green connected state, message button |
| `Dynamic Island-8.svg` | Find My iPhone alert | `367 × 86`, `r43` | 44pt device/avatar artwork and alert title |
| `Dynamic Island-9.svg` | Silent mode | `367 × 86`, `r43` | bell-slash, state label, 43pt Unmute pill |
| `Dynamic Island-10.svg` | Moved to iPhone | `367 × 86`, `r43` | centered transfer text and blue undo button |
| `Dynamic Island-11.svg` | Music player | `367 × 177`, `r42` | 53pt art, 240 × 6.5 scrubber, media controls, AirPlay |
| `Dynamic Island-12.svg` | Turn-by-turn navigation | `367 × 185.36`, `r42` | four maneuver zones and active/inactive direction glyphs |
| `Dynamic Island-13.svg` | FaceTime Audio | `367 × 168`, `r42` | avatar/info and five 51.73pt action circles |
| `Dynamic Island-14.svg` | FaceTime Audio variant | `367 × 166`, `r42` | avatar/info and five 49.73pt action circles |
| `Dynamic Island-15.svg` | Shared media/call | `367 × 172.64`, `r42` | 53pt art and five 50.64pt action circles |
| `Dynamic Island-16.svg` | Remote video player | `367 × 177`, `r42` | TV thumbnail, 240 × 6.5 scrubber, ±15/play/AirPlay |
| `Dynamic Island-17.svg` | Airplane-mode alert | `367 × 148`, `r42` | orange airplane and one 335 × 43 settings button |
| `Dynamic Island-18.svg` | Screen mirroring | `367 × 144`, `r42` | blue displays and one 335 × 43 stop button |
| `Dynamic Island-19.svg` | Mobile-data alert | `367 × 162`, `r42` | green antenna and two 161.5 × 43 buttons |
| `Dynamic Island-20.svg` | Transit route | `367 × 142`, `r42` | train and one 335 × 43 destructive button |

#### Height families proven by the extended corpus

- **Compact hardware/activity:** `36.67dp` canonical. Content controls width;
  examples are idle `126dp`, split `~199-203.67dp`, media `190dp`, and timer
  `222dp`.
- **Standard expanded capsule:** `86dp` canonical. Eleven one-row/two-line
  system activities repeat `85.08-86dp`; use one `86dp` token in the app.
- **Full expanded sheet:** content-specific `142-185.36dp`; forcing all of
  these to one height would contradict the iOS references.
- **Expanded width:** `371dp` on a 393dp frame and `408dp` on a 430dp frame in
  `Expanded Presentation.svg`, proving `min(windowWidth - 22dp, 408dp)`.
- **Top placement:** compact device frames begin at `y=10-11dp`; use `11dp` as
  the default and retain user/device cutout calibration.

#### Minimal icon sheet inventory

`Dynamic Island/Minimal.svg` contains a 5-row grid of 25 black `38 × 38`
circles. It covers location, phone/call, battery percentage, timer/countdown,
recording, video, Face ID/scanning, transfer, waveform, notification bell,
completion, lock/unlock, link, Focus/moon, driving, satellite/location,
screen mirroring, AirPlay, cellular antenna, and transit/train variants. These
are state glyph references—not a reason to change the shared 36.67dp body
height; artwork should be optically fitted inside that body.

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
   - Treat compact dimensions as density-independent physical geometry, not as
     a percentage of screen width.
   - Maximum expanded width is `min(currentWindowWidth - 22dp, 408dp)`.
   - Never impose a minimum expanded width that is wider than the current
     window.
   - User width calibration changes width only; it must never scale height,
     icons, text, or touch targets.
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
