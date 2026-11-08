# TASK UI-02B — Final Design Tokens & Visual Design System

**Developer:** Gemini — Senior Android UI/UX Engineer  
**Examiner:** ChatGPT — Principal Product Architect  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Base Branch:** `visual-redesign/task-ui-02-premium-system`  
**Active Review Branch:** `visual-redesign/task-ui-02b-premium-refinement`  
**Target Calibration:** Realme 8 (`RMX3085`, 360×800 dp, density 3.0×)

---

## 1. Island Geometry Tokens (`AppIslandTokens`)

The geometry tokens in `app/src/main/java/ai/emots/kishan_dynamic/ui/theme/AppTheme.kt` define the canonical layout contract for all Dynamic Island shells, content containers, satellites, and interactive targets.

| Token Identifier | UI-01 (Legacy) | UI-02 (Interim) | UI-02B (Final) | Description / Role |
| :--- | :---: | :---: | :---: | :--- |
| `compactHeight` | 52.7 dp | 36.67 dp | **36.67 dp** | Canonical height for all compact/minimal states (110 px @ 3.0x) |
| `compactCorner` | 26.35 dp | 18.335 dp | **18.335 dp** | Mathematical 50% capsule radius for compact presentation |
| `compactHorizontalInset` | 16.0 dp | 12.0 dp | **12.0 dp** | Shell horizontal padding for compact leading/trailing items |
| `compactContentGap` | 12.0 dp | 8.0 dp | **8.0 dp** | Inner spacing between icon and text in compact mode |
| `compactWidth` | 152.0 dp | 126.0 dp | **126.0 dp** | Baseline idle notch width |
| `compactMaxWidth` | 280.0 dp | 250.0 dp | **250.0 dp** | Hard upper bound on compact island expansion |
| `compactStatusSideReserve` | 64.0 dp | 72.0 dp | **72.0 dp** | Safe budget reserved for system status bar indicators |
| `expandedCorner` | 42.0 dp | 42.0 dp | **42.0 dp** | Full sheet expanded corner radius |
| `expandedMaxWidth` | 408.0 dp | 408.0 dp | **408.0 dp** | Max width on large phones / foldables |
| `expandedSideMargin` | 11.0 dp | 11.0 dp | **11.0 dp** | Standard horizontal inset from display edge |
| `standardExpandedHeight`| 86.0 dp | 86.0 dp | **86.0 dp** | Standard notification / ringer / alert sheet height |
| `alertHeight` | — | — | **54.0 dp** | **NEW:** System alert capsule height (battery, charging, ringer) |
| `musicExpandedHeight` | 192.0 dp | 177.0 dp | **177.0 dp** | Full music sheet height (scrubber, album art, controls) |
| `callExpandedHeight` | 185.0 dp | 166.0 dp | **166.0 dp** | Full ongoing call sheet height (avatar, status, controls) |
| `actionControlHeight` | 50.0 dp | 34.0 dp | **32.0 dp** | Action chip pill height (IslandActionChip) |
| `actionControlCorner` | 25.0 dp | 17.0 dp | **16.0 dp** | Half-height corner radius for action chips |
| `actionControlIconSize` | 20.0 dp | 16.0 dp | **15.0 dp** | Icon glyph dimension inside action chips |
| `actionControlPaddingHorizontal` | 16.0 dp | 12.0 dp | **12.0 dp** | Horizontal text padding inside action chips |
| `utilityButtonSize` | 48.0 dp | 28.0 dp | **24.0 dp** | Circular utility controls (close 'X', collapse chevron) |
| `utilityButtonCorner` | 24.0 dp | 14.0 dp | **12.0 dp** | Circular corner radius for utility buttons |
| `satelliteHeight` | 52.7 dp | 38.0 dp | **32.0 dp** | Subordinate layered mini-island capsule height |
| `satelliteCorner` | 26.35 dp | 19.0 dp | **16.0 dp** | Half-height corner radius for satellite pills |
| `satelliteIconSize` | 24.0 dp | 18.0 dp | **16.0 dp** | App icon dimension inside satellite mini-islands |
| `satelliteWidthMin` | 280.0 dp | 260.0 dp | **260.0 dp** | Minimum width bound for satellite child |
| `satelliteWidthMax` | 338.0 dp | 312.0 dp | **312.0 dp** | Maximum width bound for satellite child |
| `overflowHeight` | 44.0 dp | 28.0 dp | **24.0 dp** | Layered mini-island overflow affordance chip (+N more) |
| `overflowCorner` | 22.0 dp | 14.0 dp | **12.0 dp** | Half-height corner radius for overflow pill |
| `minimumActionTarget` | 48.0 dp | 48.0 dp | **48.0 dp** | Mandatory minimum accessible touch bound |

---

## 2. Typography Token Scale

All typography tokens adhere to strict iOS optical hierarchy, with reduced tracking for high legibility on 360dp screens:

| Typography Role | Font Size | Font Weight | Line Height | Letter Spacing | Color (Dark Theme) |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Header Title** | 17.0 sp | Bold (700) | 22.0 sp | -0.4 sp | `#FFFFFF` |
| **Track / Contact Title**| 15.0 sp | SemiBold (600)| 20.0 sp | -0.3 sp | `#FFFFFF` |
| **Notification Title** | 14.5 sp | Bold (700) | 18.0 sp | -0.25 sp | `#FFFFFF` |
| **Body Message** | 12.5 sp | Normal (400) | 17.0 sp | -0.1 sp | `#DDDDDD` |
| **Subtitle / Status** | 12.5 sp | Normal (400) | 16.0 sp | 0.0 sp | `#898989` |
| **App Name Header** | 12.0 sp | Medium (500) | 15.0 sp | 0.0 sp | `#A4A4A9` |
| **Satellite Title** | 11.5 sp | SemiBold (600)| 14.0 sp | -0.1 sp | `#FFFFFF` |
| **Satellite Snippet** | 11.0 sp | Normal (400) | 14.0 sp | 0.0 sp | `#A4A4A9` |
| **Monospace Timestamps**| 11.0 sp | Bold (700) | 14.0 sp | 0.0 sp | `#FFFFFF` (Mono) |
| **Pagination Badge** | 10.0 sp | SemiBold (600)| 12.0 sp | 0.0 sp | `#DDDDDD` |

---

## 3. Surface & Interaction Color Palette

| Token Role | Hex Color / Alpha | Purpose / Usage |
| :--- | :--- | :--- |
| `OLED True Black` | `#000000` | Background of all Dynamic Island shells and satellites |
| `Island Surface Glass` | `#1C1C1E` (alpha 0.90) | Glassmorphic surface backing for interactive containers |
| `Specular Border` | `#FFFFFF` (alpha 0.15) | 0.5dp crisp edge defining islands against wallpaper |
| `Subtle Specular Border`| `#FFFFFF` (alpha 0.08) | Secondary partition border |
| `Primary Action Blue` | `#0A84FF` | Primary button fill (e.g. OTP "Copy", Reply) |
| `Primary Centerpiece` | `#FFFFFF` | Centerpiece Play/Pause button in Music Player |
| `Call Active Green` | `#34C759` | Accept incoming call button fill |
| `Call Decline Red` | `#FA3532` | End call / Decline call button fill |
| `Action Chip Charcoal` | `#2C2C2E` | Secondary action chip pill background |
| `Button Glass Inactive`| `Color(0x333A3A3C)` | Circular glass buttons in ongoing call sheet |
| `Scrubber Track` | `#3A3A3C` (alpha 0.35) | Background track for audio progress bar |
| `Scrubber Progress` | `#FFFFFF` | Filled foreground progress bar |
