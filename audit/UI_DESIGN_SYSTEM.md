# AURORA DESIGN SYSTEM SPECIFICATION (v2.0)
## Unified Obsidian & Porcelain Tokens, Motion Physics & Component Architecture

**Package:** `com.island.dynamicnotch` (`ai.emots.kishan_dynamic`)  
**Design Lead:** Aurora System UI & Jetpack Compose Design-System Architect  
**Status:** PRODUCTION STANDARD — APPLIED ACROSS ALL 52 APP SURFACES

---

## 1. Design Philosophy: Restrained Atmospheric Elegance

Aurora Dynamic Island is a high-precision Android system UI enhancement. Its visual language avoids generic flat templates, garish neon borders, and excessive decorative clutter. Every visual element adheres to three foundational tenets:

1. **Gradients are Brand Accents, Not Layout Fillers:** Gradients are reserved for high-impact surfaces (the Aurora Pro Hero, active selection radios, ambient preview backgrounds, and primary CTAs). Standard cards and informational panels use solid elevated surfaces with subtle specular hairline borders.
2. **True Dark and Light Theme Parity:**
   - **Aurora Obsidian (Dark Theme):** Built on an obsidian deep ink foundation (`#070A12` / `#0A1020`), elevated surfaces (`#121A2C`), and controlled aurora illumination (Cyan `#22CFE4`, Electric Blue `#3478F6`, Violet `#7757EA`). Text is near-white (`#F7FAFF`) with secondary labels in slate (`#BBC5D8`).
   - **Aurora Porcelain (Light Theme):** A cool-neutral canvas (`#F6F8FC`), crisp elevated cards (`#FFFFFF`), high-contrast ink text (`#121827`), subtle borders (`#E2E8F0`), and twilight slate secondary text (`#596578`). Controls never look washed out or disabled.
3. **Single Visual Component Parity:** The floating system overlay and in-app preview stages share identical layout composables and token definitions. Only data provenance (live platform broadcast vs illustrative simulation) differs.

---

## 2. Color Palette & Token Hierarchy

### 2.1 Backgrounds & Surfaces
| Token Name | Aurora Obsidian (Dark) | Aurora Porcelain (Light) | Role & Usage |
| :--- | :--- | :--- | :--- |
| `AppTheme.colors.background` | `#070A12` | `#F6F8FC` | Root screen background canvas |
| `AppTheme.colors.surface` | `#0F1524` | `#FFFFFF` | Standard card container |
| `AppTheme.colors.surfaceElevated` | `#161F33` | `#FFFFFF` | Modals, dialogs, elevated chips |
| `AppTheme.colors.surfaceVariant` | `#1D2840` | `#EEF2F9` | Inner input fields, unselected chips |
| `AppTheme.colors.border` | `0x24FFFFFF` | `#E2E8F0` | Structural card borders (0.5dp – 1dp) |
| `AppTheme.colors.borderStrong` | `0x44FFFFFF` | `#CBD5E1` | Selected active borders (1.5dp) |

### 2.2 Typography & Ink Contrast
| Token Name | Aurora Obsidian (Dark) | Aurora Porcelain (Light) | Contrast Ratio |
| :--- | :--- | :--- | :--- |
| `AppTheme.colors.textPrimary` | `#F7FAFF` | `#121827` | ≥ 12.5:1 (AAA) |
| `AppTheme.colors.textSecondary` | `#BBC5D8` | `#596578` | ≥ 6.2:1 (AA) |
| `AppTheme.colors.textTertiary` | `#7E8BA4` | `#8C97A8` | ≥ 4.5:1 (AA) |
| `AppTheme.colors.textOnAccent` | `#FFFFFF` | `#FFFFFF` | High contrast on primary buttons |

### 2.3 Brand Accents & Semantics
- **Aurora Cyan:** `#22CFE4` (Active live activities, toggles, success accents)
- **Aurora Electric Blue:** `#3478F6` (Primary buttons, links, navigation indicators)
- **Aurora Violet:** `#7757EA` (Action island, pro hero illumination)
- **Success Green:** `#30D158` (Active calls, battery charging, granted permissions)
- **Warning Amber:** `#FFB340` (Trial countdown, pending permissions, review requests)
- **Destructive Red:** `#FF453A` (Critical alerts, call disconnects)

---

## 3. Typographic Scale

All typography uses Android system fonts (`FontFamily.Default` or Roboto/Sans-serif) with optical sizing and line-height constraints that scale safely up to 200% under Android accessibility settings.

| Style Name | Font Size | Line Height | Weight | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `AppTheme.typography.h1` | 26sp | 32sp | Bold (`FontWeight.Bold`) | Screen large titles, onboarding headings |
| `AppTheme.typography.h2` | 20sp | 26sp | SemiBold (`FontWeight.SemiBold`) | Major section headers, paywall titles |
| `AppTheme.typography.h3` | 16sp | 22sp | SemiBold (`FontWeight.SemiBold`) | Card titles, plan names |
| `AppTheme.typography.body` | 15sp | 21sp | Normal (`FontWeight.Normal`) | Primary body copy, toggle titles |
| `AppTheme.typography.bodySmall` | 13.5sp | 19sp | Normal (`FontWeight.Normal`) | Secondary descriptions, sub-labels |
| `AppTheme.typography.caption` | 12sp | 16sp | Normal / Medium | Footnotes, status pills, microcopy |
| `AppTheme.typography.badge` | 10sp | 13sp | Bold (`FontWeight.Bold`) | Plan badges ("SAVE 50%"), status tags |
| `AppTheme.typography.button` | 15sp | 20sp | SemiBold (`FontWeight.SemiBold`) | Primary & secondary button text |

---

## 4. Spacing, Density & Insets Contract

### 4.1 Base Spacing Grid (4/8 pt base)
- `xxs`: `2.dp`
- `xs`: `4.dp`
- `sm`: `8.dp`
- `md`: `12.dp`
- `lg`: `16.dp`
- `xl`: `20.dp`
- `xxl`: `24.dp`
- `xxxl`: `32.dp`

### 4.2 Insets & Scaffold Rule (P0-2 Resolution)
1. **Root Screen Scaffolding:** Always uses `AppScreen`.
2. **Content Window Insets:** Managed via `WindowInsets.safeDrawing`.
3. **Bottom Insets Resolution:**
   - When `bottomAction != null` (e.g. PermissionScreen, PremiumScreen): The bottom bar already occupies `innerPadding.bottom`. The content Column applies exactly `AppTheme.spacing.lg` (16dp) bottom padding. Double bottom padding is prohibited.
   - When `bottomAction == null` (e.g. Root Tabs with floating dock): The content Column applies `bottomInset + AppTheme.spacing.lg` (`BottomDockInset = 88.dp` + 16dp = `104.dp`), ensuring the final list item is 100% visible and interactive above the floating dock.
4. **Touch Targets:** All interactive controls maintain minimum 48×48dp accessible touch targets via `heightIn(min = 48.dp)` or `size(48.dp)`.

---

## 5. Motion & Physics Contract

### 5.1 Centralized Motion Specifications (`AppMotion.kt`)
- **Press Interaction:** `spring(dampingRatio = 0.82f, stiffness = 420f)` with scale `0.96f` on tap down.
- **Island State Transition (Morph):** `spring(dampingRatio = 0.78f, stiffness = 340f)` for continuous squircle interpolation.
- **Card Reveal:** Staggered `slideInVertically` + `fadeIn` (`tween(280ms, easing = FastOutSlowInEasing)`).
- **Reduced Motion Contract:** All animations observe `LocalReducedMotion.current`. When reduced motion is enabled, springs fall back to `snap()` and animated content appears instantly without disorienting transforms.
