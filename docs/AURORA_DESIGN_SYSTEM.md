# Aurora Dynamic Island — Design System Specification

## 1. Overview & Visual Philosophy

Aurora Dynamic Island delivers an Apple-grade, fluid, and distraction-free Dynamic Island experience natively on Android. The design system combines iOS precision with Material Design 3 engineering, emphasizing:

- **Executive Craftsmanship:** Obsidian and midnight surfaces with controlled spectral highlights (Aurora Cyan `#00F2FE` and Aurora Violet `#7F00FF`).
- **Complete Theme Parity:** Native dark mode (obsidian/deep-slate) and light mode (porcelain/pure-white) support without washed-out cards or low-contrast elements.
- **Physical Plausibility & Fluid Motion:** Spring curves and ease-out quint transitions that mirror physical Apple Dynamic Island mechanics.
- **Content Hierarchy:** High information density where needed, progressive disclosure, and clear visual separation between primary interactions, metadata, and legal terms.

---

## 2. Color Palette & Gradients

### 2.1 Theme Palette
| Token | Dark Mode Value | Light Mode Value | Description |
|---|---|---|---|
| `background` | `#0B0F19` (Obsidian Midnight) | `#F8F9FD` (Porcelain Slate) | Base screen background |
| `surface` | `#131B2E` (Deep Slate) | `#FFFFFF` (Pure White) | Primary card and container fill |
| `surfaceElevated` | `#1A243D` (Luminous Slate) | `#F1F4FA` (Soft Cloud) | Nested tiles, dialogs, sheets |
| `textPrimary` | `#F8FAFC` (Pure Frost) | `#0F172A` (Obsidian Ink) | Primary headlines and high-priority labels |
| `textSecondary` | `#94A3B8` (Muted Slate) | `#475569` (Charcoal Slate) | Subtitles, body descriptions |
| `textTertiary` | `#64748B` (Subtle Slate) | `#94A3B8` (Muted Slate) | Footnotes, legal disclaimers, disabled states |
| `border` | `#1E293B` (Subtle Obsidian) | `#E2E8F0` (Porcelain Border) | Unselected card and divider borders |
| `borderSelected` | `#00F2FE` (Aurora Cyan) | `#00C2D8` (Vibrant Cyan) | Active card selection highlight |

### 2.2 Brand & Accents
| Token | Hex Value | Purpose |
|---|---|---|
| `auroraCyan` | `#00F2FE` | Primary brand accent, selected radio buttons, audio visualizers |
| `auroraViolet` | `#7F00FF` | Secondary spectral accent, badge endpoints, hero gradients |
| `auroraMagenta` | `#E040FB` | Tertiary accent, subtle ambient glows |
| `gold` | `#FFD700` | Crown icons, Pro badges, high-value highlights |
| `success` | `#30D158` | Active entitlement pills, checkmarks, call indicators |
| `error` | `#FF453A` | System warnings, cancellation notices |

### 2.3 Spectral Gradients
```kotlin
// Hero card background with directional depth
fun AppColors.auroraHeroGradient(): Brush = Brush.verticalGradient(
    colors = if (isDark) listOf(Color(0xFF0F172A), Color(0xFF020617))
             else listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
)

// Selected plan card luminous fill
fun AppColors.auroraProCardGradient(selected: Boolean): Brush = when {
    selected && isDark -> Brush.linearGradient(
        listOf(Color(0x2800F2FE), Color(0x187F00FF), Color(0x08131B2E))
    )
    selected && !isDark -> Brush.linearGradient(
        listOf(Color(0x1500F2FE), Color(0x107F00FF), Color(0xFFFFFFFF))
    )
    else -> Brush.linearGradient(listOf(surface, surface))
}

// Glowing selection border
fun AppColors.auroraBorderBrush(selected: Boolean): Brush = when {
    selected -> Brush.linearGradient(listOf(auroraCyan, auroraViolet))
    else -> Brush.linearGradient(listOf(border, border))
}

// High-converting primary CTA gradient
fun AppColors.auroraActionGradient(): Brush = Brush.horizontalGradient(
    colors = listOf(auroraCyan, Color(0xFF4FACFE), auroraViolet)
)
```

---

## 3. Typography Scale

Typography uses the platform standard font with precise optical tracking and weights:

| Style | Size | Weight | Tracking | Purpose |
|---|---|---|---|---|
| `h1` | 32sp | Bold | -0.5sp | Screen hero titles, major announcements |
| `h2` | 24sp | Bold | -0.2sp | Section headers, modal titles |
| `h3` | 18sp | SemiBold | 0sp | Card headlines, plan titles |
| `body` | 15sp | Regular | 0sp | Main readable content, descriptions |
| `bodySmall` | 13sp | Regular | 0.1sp | Benefit descriptions, row labels |
| `caption` | 11.5sp | Medium | 0.2sp | Disclosures, badges, timestamps |

---

## 4. Spacing & Elevation Grid

Aurora strictly adheres to an 8dp baseline grid with 4dp sub-steps:

| Token | Size | Application |
|---|---|---|
| `spacing.xxs` | 2dp | Sub-pixel hairline adjustments, bar spacing |
| `spacing.xs` | 4dp | Icon-to-text spacing, waveform bar gaps |
| `spacing.sm` | 8dp | Inner component padding, tight row gaps |
| `spacing.md` | 12dp | Medium list spacing, badge padding |
| `spacing.lg` | 16dp | Standard card internal padding, layout gutters |
| `spacing.xl` | 20dp | Section separation |
| `spacing.xxl` | 24dp | Major section headers separation |
| `spacing.xxxl` | 32dp | Hero card to body spacing |

---

## 5. Shape & Corner Radii

Corner radii follow Apple's squircle-like smooth curves:

- **Large Surfaces (`radius.lg = 20dp`):** Main cards, sheets, paywall hero cards.
- **Medium Surfaces (`radius.md = 14dp`):** Plan selector cards, benefit tiles, buttons.
- **Pills & Chips (`CircleShape`):** Mode toggles, status pills, badge backgrounds, action buttons.

---

## 6. Component Kit

All components are centralized in `ui/kit/`:

1. **`AppScreen`:** The foundation of all screens.
   - Pinned `topBar` slot ensuring title and navigation never scroll off-screen.
   - Edge-to-edge window inset handling (`WindowInsets.safeDrawing`).
   - Responsive max-width constraint (720dp for tablets/foldables).
   - Glassmorphic floating dock `bottomBar` with top hairline divider (`AppTheme.colors.border`) and 12dp elevation shadow for authentic visual separation.
   - Sticky `bottomAction` slot with guaranteed clearance via `bottomInset` (e.g. 140dp on paywall screens) preventing content occlusion.
2. **`AppButton`:** Supports `AppButtonStyle.AuroraPro` (spectral gradient with glow), `Primary`, `Secondary`, and `Destructive`. All touch targets meet or exceed 48dp height with haptic feedback.
3. **`AppPlanSelectorRow`:** Accessible, responsive plan selection card with:
   - High-contrast selection indicator with cyan checkmark radio.
   - Strike-through original prices and bold localized currency amounts from Google Play Billing `ProductDetails`.
   - Authentic value badges (`"Most popular"`, `"Lifetime access"`, `"Best value"`) with zero fabricated percentage discounts.
   - Luminous spectral border (`auroraBorderBrush`) and subtle gradient background fill on selection.
4. **`AuroraProHeroCard`:** Executive obsidian hero featuring:
   - Entitlement-aware state presentation: differentiated badges for Paid Pro (`"Aurora Pro is Active · Permanent access unlocked"`), 7-Day Free Trial (`"7-Day Free Trial Active · [X] days remaining"`), and Free (`"Unlock Aurora Pro"`).
   - Interactive Dynamic Island preview sandbox with live audio waveform animations and mode switching (Music, Call, Quick Actions).
   - Contrast-hardened iOS Status Bar ensuring white glyphs on dark preview stages regardless of app theme.
5. **`AppFreeVsProComparisonCard`:** Transparent, honest comparison matrix:
   - Eliminates guesswork by comparing Free tier boundaries against Pro unlocked capabilities.
   - Apple-style visual checkmark glyphs (`AppleGlyph.Check`), luminous cyan chips for Pro features, and subtle slate chips for Free limits.
   - Zero ambiguous or repetitive textual cells.

---

## 7. Elevation, Insets & Sticky Dock Guidelines

### 7.1 Glassmorphic Dock Elevation
The floating bottom dock (`AppScreen` `bottomBar`) utilizes a dual-layer separation technique:
- A top hairline divider (`Modifier.fillMaxWidth().height(0.8.dp).background(AppTheme.colors.border)`) providing crisp edge definition.
- A 12dp elevation shadow (`Modifier.shadow(elevation = 12.dp, shape = RectangleShape)`) providing organic separation between scrolling content and the pinned action surface.

### 7.2 Scroll Inset Clearance
Screens hosting fixed or floating bottom actions must supply explicit `bottomInset` padding to `AppScreen`:
- Standard Bottom Bar / Dock: `bottomInset = AppTheme.spacing.bottomDockInset` (~80dp).
- Paywall Screen with Multi-line CTA & Disclosures: `bottomInset = 140.dp`. This guarantees all legal terms, auto-renewal disclaimers, and subscription management instructions scroll completely clear of the sticky purchase button without clipping.

---

## 8. Dark & Light Theme Parity & Contrast Standards

### 8.1 Status Bar Contrast in Dark Containers (`IosStatusBar`)
When rendering dark preview stages (such as the Dynamic Island interactive sandbox) in a light theme environment:
- `IosStatusBar` enforces `contentColor = Color.White.copy(alpha = 0.92f)` whenever `isDarkBackground = true`.
- Never fall back to `AppTheme.colors.textPrimary` within dark containers, preventing illegible black-on-black rendering on light-mode devices.

### 8.2 Accessible Contrast Ratios
- Primary text (`textPrimary`) on surface: Minimum 11.5:1 contrast ratio.
- Secondary text (`textSecondary`) on surface: Minimum 5.5:1 contrast ratio (exceeding WCAG AAA 4.5:1).
- Badges and chips: High-contrast fill with matched text opacity ensuring full legibility across 200% system font scaling.

