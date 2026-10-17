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

1. **`AppScreen`:** The foundation of all screens. Features pinned `topBar`, edge-to-edge window inset handling (`WindowInsets.safeDrawing`), responsive max-width constraint (720dp for tablets/foldables), and sticky `bottomAction` slot with guaranteed clearance (`bottomInset`).
2. **`AppButton`:** Supports `AppButtonStyle.AuroraPro` (spectral gradient with glow), `Primary`, `Secondary`, and `Destructive`. All touch targets meet or exceed 48dp height.
3. **`AppPlanSelectorRow`:** Accessible, responsive plan card with radio indicator, strike-through original prices, bold localized amounts, and high-contrast savings badges.
4. **`AuroraProHeroCard`:** Executive obsidian hero featuring an interactive Dynamic Island stage with live audio waveform animations and live mode toggling.
5. **`AppFreeVsProComparisonCard`:** Transparent, honest comparison matrix eliminating guesswork before purchase.
