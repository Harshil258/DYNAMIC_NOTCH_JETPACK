# Dynamic Island Reference Design Principles & Layout Ruleset

**Author:** Senior Android / Jetpack Compose Visual Systems Architect  
**Design Authority:** Reference Figma & Vector Specifications (`Components.svg`, `Compact.svg`, `Expanded.svg`, `Minimal.svg`, `Group 3.png`, `Group 4.png`, `Group 5.png`)  
**Target Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Application Scope:** All Dynamic Island presentations, overlays, cards, layered mini-islands, and preview fixtures.

---

## 1. Core Architectural Model: The 4-Slot Anatomy

Every Dynamic Island presentation is derived from the canonical 4-slot layout architecture defined in `Components.svg`:

```
+-------------------------------------------------------------------------+
|                                                                         |
|   +-----------+       +-------------------------+       +-----------+   |
|   |           |       |   HARDWARE CUTOUT ZONE  |       |           |   |
|   |  LEADING  |       +-------------------------+       |  TRAILING |   |
|   |   SLOT    |       |       CENTER SLOT       |       |   SLOT    |   |
|   |           |       +-------------------------+       |           |   |
|   +-----------+                                         +-----------+   |
|                                                                         |
|   +-----------------------------------------------------------------+   |
|   |                           BOTTOM SLOT                           |   |
|   +-----------------------------------------------------------------+   |
|                                                                         |
+-------------------------------------------------------------------------+
```

### The 4 Semantic Content Slots:
1. **`Leading Slot` (Left Anchor):**
   - **Compact:** 18–24dp primary identity icon (app icon, circular album art, caller avatar, system glyph).
   - **Expanded:** 44–52dp rounded square album artwork (`12dp` corner radius), 40dp circular contact avatar, or 32dp circular status icon badge.
2. **`Trailing Slot` (Right Anchor):**
   - **Compact:** Dynamic indicator (animated audio equalizer wave bars, battery percentage ring, circular countdown progress, accept/decline action targets).
   - **Expanded:** Secondary status badge, AirPlay audio route icon, info disclosure `ⓘ`, or call state indicator.
3. **`Center Slot` (Core Identity & Context):**
   - **Compact:** Preserves physical camera cutout clearance; displays compact text or countdown when cutout width allows.
   - **Expanded:** Primary title (Bold 14–15sp), subtitle / metadata (Muted 12–13sp), route info, or sensor tracking status.
4. **`Bottom Slot` (Action & Progression Span):**
   - **Expanded Only:** Full-width scrubbers (audio/video progress), media transport controls (`⏮`, `▶`, `⏭`), flight progression lines, sparkline charts, and pill action buttons (`43–44dp` capsule buttons).

---

## 2. Canonical Spatial Tokens & Dimensional Scale

Every measurement below is derived directly from the vector layers of the Figma reference files. These are exact design tokens—never round them to arbitrary numbers.

### A. Compact & Minimal Presentation Tokens
| Token | Canonical Value | Reference Source | Description |
| :--- | :---: | :--- | :--- |
| **`compactHeight`** | **36.67 dp** | `Compact Presentation.svg` | Height for every compact, minimal, and split presentation. |
| **`compactCorner`** | **18.335 dp** | `Compact Presentation.svg` | Mathematical 50% capsule corner radius (`height / 2`). |
| **`compactIdleWidth`** | **126.0 dp** | `Components.svg` | Width of the idle, resting Dynamic Island capsule. |
| **`compactMinWidth`** | **126.0 dp** | `Compact Presentation.svg` | Minimum allowable width for any compact pill. |
| **`compactMaxWidth`** | **250.0 dp** | `Compact Presentation.svg` | Maximum width before transitioning to an expanded sheet. |
| **`splitMainWidth`** | **156.0 dp** | `Compact Presentation.svg` | Width of the primary pill in dual-activity split presentation. |
| **`splitBubbleSize`**| **36.67 dp** | `Minimal.svg` | Diameter of the detached trailing circular bubble (`rx = 18.335dp`). |
| **`splitGap`** | **11.0 dp** | `Compact Presentation.svg` | Canonical horizontal gap between the main pill and trailing bubble. |
| **`minimalBubbleSize`**| **38.0 dp** | `Minimal.svg` | Diameter of standalone detached minimal activity bubble (`rx = 19dp`). |
| **`compactHorizontalInset`**| **12.0 dp** | `Compact.svg` | Outer edge padding for leading and trailing content. |
| **`compactContentGap`** | **8.0 dp** | `Compact.svg` | Space between icon and adjacent text label in compact mode. |

### B. Expanded Presentation Tokens
| Token | Canonical Value | Reference Source | Description |
| :--- | :---: | :--- | :--- |
| **`expandedWidth`** | **367.0 dp** | `Expanded.svg` | Standard expanded sheet width on standard 393dp width devices. |
| **`expandedMaxWidth`** | **408.0 dp** | `Expanded.svg` | Hard ceiling width on larger / Pro Max displays (prevents tablet banners). |
| **`expandedSideMargin`**| **11.0 dp** | `Expanded.svg` | Horizontal display margin on each side (`(393 - 367) / 2 = 13dp`, min 11dp). |
| **`expandedCorner`** | **42.0 dp** | `Expanded.svg` | Continuous Apple squircle corner radius for expanded sheets. |
| **`expandedPadding`** | **16.0 dp** | `Expanded.svg` | Interior content inset padding (horizontal and vertical). |
| **`topDisplayInset`** | **11.0 dp** | `Expanded.svg` | Distance from the physical top screen edge to the island top edge. |

### C. Standard Height Budgets by Island Archetype
| Archetype | Canonical Height | Reference Source | Visual Components |
| :--- | :---: | :--- | :--- |
| **Single-Action Compact Alert** | **86.0 dp** | `Compact.svg` (`rx = 43dp`) | AirDrop transfer, single-line alert with circular status. |
| **System Alert (Single Button)** | **144.0 – 148.0 dp** | `Expanded.svg` (`rx = 42dp`) | Airplane mode, Screen Mirroring, Battery low alert. |
| **Split Dual-Action Alert** | **162.0 dp** | `Expanded.svg` (`rx = 42dp`) | Alert with two side-by-side capsule buttons. |
| **Ongoing Telephony / Call Sheet**| **166.0 – 168.0 dp** | `Expanded.svg` (`rx = 42dp`) | Avatar, caller info, 5 circular control buttons (standard & FaceTime). |
| **Music / Media Player Sheet** | **177.0 dp** | `Expanded.svg` (`rx = 42dp`) | 52dp album art, title, equalizer, scrubber, transport controls, AirPlay. |
| **Turn-by-Turn Navigation Sheet**| **185.0 dp** | `Expanded.svg` (`rx = 42dp`) | Lane arrows, distance, highway shield, destination info. |
| **Multi-Card Layered Stacks** | **Auto / 12dp gap** | `Group 3/4/5.png` | Primary sheet + subordinate squircle cards stacked below. |

---

## 3. Action Buttons & Interactive Controls Contract

Derived from `Expanded.svg` (lines 682–1040) and `Group 5.png`:

```
Full-Width Capsule Button (335dp x 43dp, rx = 21.5dp):
+-------------------------------------------------------------------------+
|                             Action Label                                |
+-------------------------------------------------------------------------+

Split Dual Capsule Buttons (161.5dp each, 12dp gap, rx = 21.5dp):
+-----------------------------------+     +-------------------------------+
|             Button 1              |     |            Button 2           |
+-----------------------------------+     +-------------------------------+
```

### Action Button Standards:
1. **Button Geometry:**
   - **Height:** **43.0 dp** (standard) / **44.0 dp** (minimum accessibility target).
   - **Corner Radius:** **21.5 dp** (exact 100% full capsule curve, `height / 2`).
   - **Full-Width Action:** `335.0 dp` width (`367dp - 2 × 16dp`).
   - **Split Dual Action:** `161.5 dp` width each, separated by exactly **12.0 dp** gap (`(335 - 12) / 2 = 161.5dp`).
2. **Background Tint Treatments:**
   - **Dark Neutral:** `#2C2C2E` / `#2C2C2D` (Secondary system actions: `Open Settings`, `Dismiss`).
   - **Indigo / Connected Tint:** `#1A1C2D` with `#5E5CE6` text (Screen Mirroring, Bluetooth, AirPlay).
   - **Crimson / Alert Tint:** `#1D1011` with `#FF453A` text (End Call, Stop Recording, Critical Alerts).
   - **Primary Solid:** White `#FFFFFF` with black text `#000000` (Prominent CTA).
   - **Accent Lavender:** `#7B61FF` / `#8B5CF6` with white text (Share, Special Live Actions).
3. **Touch Targets:**
   - All interactive touch targets must satisfy minimum 44dp bounding boxes via `Modifier.minimumInteractiveComponentSize()` or bounded clip targets.

---

## 4. Media Player & Scrubber Specifications

Derived from `Expanded.svg` (top-left) and `Group 3.png`:

1. **Album Artwork:**
   - Square `52 × 52 dp` with **12.0 dp** corner radius.
   - Subtle `1dp` inner border stroke (`#1E293B` or `rgba(255, 255, 255, 0.1)`).
2. **Equalizer Waveform:**
   - Positioned at the top-right trailing slot.
   - 4 to 5 vertical bars with rounded caps (`rx = 1.5dp`).
   - Animated heights (`8dp` to `24dp`), colored in vibrant gradient magenta (`#FF2D55` / `#D33682`).
3. **Scrubber / Progress Bar:**
   - Total Height: **4.0 dp** (with rounded ends).
   - Active Track: High-contrast accent color (e.g., magenta `#FF2D55` or white `#FFFFFF`).
   - Inactive Track: Dark translucent track `#2C2C2E`.
   - Timestamps: Left timestamp (elapsed, e.g. `0:44`) and Right timestamp (remaining, e.g. `-3:00`) set in **11sp** tabular digits (`#8E8E93`).
4. **Transport Controls:**
   - Previous Track (`⏮`): 22dp solid white glyph.
   - Play / Pause (`▶` / `⏸`): 28dp solid white glyph.
   - Next Track (`⏭`): 22dp solid white glyph.
   - Audio Routing (`AirPlay`): 20dp icon pinned to trailing right.
   - Centered horizontal alignment with balanced spacing.

---

## 5. Ongoing & Incoming Call Specifications

Derived from `Compact.svg`, `Expanded.svg`, and `Group 3.png`:

1. **Incoming Call (Compact):**
   - Left: Circular contact avatar (28dp diameter) with caller name.
   - Right: Circular decline button (red `#FF3B30`, 32dp diameter) + Circular accept button (green `#34C759`, 32dp diameter).
2. **Ongoing Call (Expanded Sheet — 166dp Height):**
   - **Header Row:** Circular contact avatar (`40dp` diameter) on left, Caller Name (Bold 15sp white), Subtitle (`FaceTime Audio` or Call duration in muted 12sp), Info circular button (`ⓘ`, 22dp) on right.
   - **Bottom Row (5 Circular Buttons):**
     1. Audio Source (Speaker / AirPods icon in 44dp circular button).
     2. Mute (Microphone icon in 44dp circular button).
     3. Video Toggle (Camera icon in 44dp circular button).
     4. SharePlay (SharePlay icon in 44dp circular button).
     5. End Call (Red `#FF3B30` filled 44dp circular button with white phone icon).

---

## 6. Layered Mini-Islands & Multi-Card Stacking Contract

Derived from `Group 3.png`, `Group 4.png`, and `Group 5.png`:

1. **Visual Hierarchy:**
   - Primary Island remains anchored to the display cutout at the top.
   - Subordinate cards stack vertically below the primary island.
   - **Inter-Card Spacing:** Exactly **10.0 – 12.0 dp** vertical separation.
2. **Card Geometry:**
   - Width: Matches the primary island (`367.0 dp`).
   - Corner Radius: **42.0 dp** continuous squircle matching the main island.
   - Background: Pure solid black `#000000` with subtle elevation stroke (`1dp` at `#1E293B`).
3. **Card Types Demonstrated in Reference:**
   - **Stocks / Market Live Activity:** Brand circular icon, ticker, full-width sparkline chart with green gradient fill and dashed baseline, current price (`$8,01`) and green delta (`+1.24%`).
   - **Weekly Screen Time / Histogram:** Large metric headline (`3h 40m`), 7 vertical rounded bar tracks with stacked pink segments, day-of-week abbreviations.
   - **Flight / Travel Tracker:** Departure airport & time on left, green flight progression line with centered airplane badge (`ON TIME`), arrival airport & time on right.
   - **Fitness / Step Activity:** Walking icon, step count (`9,124`), step equalizer visualizer, dual action capsule buttons (`+ Share +`, `+ Action +`).

---

## 7. Typography & Color Tokens

### Typography Scale (San Francisco / System Sans):
| Role | Size | Weight | Color | Usage |
| :--- | :---: | :---: | :---: | :--- |
| **Headline / Metric** | **28–34 sp** | Bold / Heavy | `#FFFFFF` | Large values (`$8,01`, `3h 40m`, `8.048`) |
| **Card Title** | **14–15 sp** | SemiBold | `#FFFFFF` | Primary title (`Selfless`, `Tesla Inc.`, `Tamia Castillo`) |
| **Card Subtitle** | **12–13 sp** | Regular / Medium | `#8E8E93` | Metadata (`The New Abnormal`, `TSLA`, `FaceTime Audio`) |
| **Timestamp / Micro** | **10–11 sp** | Medium | `#8E8E93` | Scrubbers (`0:44`, `-3:00`), Day labels (`Sun`, `Mon`) |
| **Status / Delta** | **11–12 sp** | SemiBold | `#30D158` / `#FF453A` | Positive (`+90,67`, `+1.24%`) or Negative deltas |
| **Button Label** | **14–15 sp** | SemiBold | `#FFFFFF` | Action button text (`Open Settings`, `Stop Mirroring`) |

### Color Palette (Obsidian & Vivid Accents):
- **Base Surface:** Pitch Black `#000000` (Expanded sheets & compact pills)
- **Subtle Stroke:** `#1E293B` (1dp border for card separation)
- **Glass Button Track:** `#2C2C2E` / `#2C2C2D`
- **Active Green:** `#30D158` / `#34C759` (Calls, battery, stocks, flight on-time)
- **Accent Magenta / Pink:** `#FF2D55` (Music wave, scrubber, screen time)
- **Warning Orange:** `#FF9F0A` / `#FF9500` (Timer, low battery, airplane mode)
- **Critical Red:** `#FF453A` / `#FF3B30` (End call, voice recording, urgent alert)
- **System Blue / Indigo:** `#0A84FF` / `#5E5CE6` (AirDrop, navigation, screen mirroring)

---

## 8. Implementation Verification & Enforcement Checklist

Before shipping any Dynamic Island composable, ensure:
1. [ ] Compact height is strictly `36.67dp` with `18.335dp` corner radius.
2. [ ] Detached minimal bubbles are `36.67–38dp` diameter with `18.335–19dp` radius.
3. [ ] Expanded sheets use `42dp` continuous corner radius with `16dp` inner padding.
4. [ ] Action buttons are `43–44dp` height with `21.5dp` capsule radius (full width 335dp or split 161.5dp + 12dp gap).
5. [ ] Media player features `52dp` album art (`12dp` radius), 4dp scrubber, magenta wave, and centered white transport controls.
6. [ ] Ongoing call sheet features 5 circular buttons (`44dp` diameter).
7. [ ] Layered mini-islands stack below the main island with `10–12dp` vertical gap and matching `42dp` corner radius.
8. [ ] Zero hardcoded layout hacks; all geometry derived from `AppTheme.island` and `AppTheme.spacing`.
