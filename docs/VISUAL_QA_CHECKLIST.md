# Aurora Dynamic Island — Visual & UX Quality Assurance Checklist

Use this checklist during code review, visual regression testing, and pre-release audits.

---

## 1. Visual Hierarchy & Brand Identity

- [ ] **Hero Card Impact:** The hero card communicates the brand identity and showcases live, animated Dynamic Island functionality without generic flat white cards.
- [ ] **Spectral Gradients:** Aurora Cyan (`#00F2FE`) and Aurora Violet (`#7F00FF`) accents are applied with restraint; never overpowering or vibrating against backgrounds.
- [ ] **Theme Parity:**
  - [ ] Dark Mode displays deep obsidian (`#0B0F19`) backgrounds and slate surfaces (`#131B2E`) with high contrast white text.
  - [ ] Light Mode displays crisp porcelain (`#F8F9FD`) backgrounds with pure white cards (`#FFFFFF`), subtle borders (`#E2E8F0`), and dark ink text (`#0F172A`).
  - [ ] Cards in Light Mode are distinct from the background with clear 0.5dp borders.

---

## 2. Layout, Docking & Screen Insets

- [ ] **Sticky Dock Clearance:** Screens with sticky bottom actions (such as `PremiumScreen` and `MainContainerScreen`) provide sufficient `bottomInset` (e.g., `72.dp` or `BottomDockInset`) so that the lowest scrollable items and legal footnotes are completely readable above the dock.
- [ ] **Edge-to-Edge Navigation:** Content respects `WindowInsets.safeDrawing`. Status bar and navigation bar insets are never clipped or doubled.
- [ ] **IME Padding:** Form inputs, search fields, and sliders include `Modifier.imePadding()` so keyboard invocation never occludes active controls.
- [ ] **Pinned Headers:** `AppTopBar` remains pinned at the top while body content scrolls underneath. Back button and screen title never scroll off-screen.

---

## 3. Overlay & Collision Prevention

- [ ] **Commercial Surface Suppression:** The floating Dynamic Island overlay is 100% suppressed (window invisible, 0dp bounds, `FLAG_NOT_TOUCHABLE`) whenever a paywall (`PremiumScreen`, `ProPaywallSheet`) is active.
- [ ] **Accessibility Shortcut Guidance:** The permission onboarding sheet explicitly instructs users to keep Android's OEM "Accessibility Button / Shortcut" toggle disabled, preventing unwanted system floating buttons.
- [ ] **Zero Ads on Overlays:** Advertisements never appear within the floating island overlay, call operations, or critical permission dialogs.

---

## 4. Accessibility & Touch Targets

- [ ] **48dp Minimum Target:** All interactive controls (buttons, plan selector cards, mode chips, sliders, switches) have a minimum touch target height and width of 48dp.
- [ ] **Color Contrast:** All body text meets WCAG AA 4.5:1 minimum contrast. Caption text meets 3:1 minimum contrast against its immediate container.
- [ ] **Reduced Motion Support:** Respects `LocalReducedMotion.current`. All waveform animations and layout transitions fall back to instant transitions when enabled.
- [ ] **Semantic Roles:** Custom clickable rows define appropriate `Role` (`Role.RadioButton`, `Role.Button`, `Role.Tab`).

---

## 5. Paywall & Play Billing Integrity

- [ ] **Live Currency Formatting:** Prices display localized currency symbols (e.g., `₹199`, `$4.99`, `€4.49`) from `ProductDetails`. No hardcoded strings.
- [ ] **Transparent Terms:** Auto-renewal terms, billing cycle duration, and Google Play cancellation instructions are visible before initiating purchase.
- [ ] **Idempotent Conversions:** Purchasing triggers `SubscriptionConversionTracker` with transaction deduplication to protect Google Ads and GA4 ROAS accuracy.
- [ ] **Restore Purchases:** "Restore purchases" button is prominently visible and provides clear user feedback upon query completion.
