# Dynamic Island UI/UX Design System & Usability Rulebook

**Version:** 2.0.0  
**Effective Date:** 12 October 2026  
**Audience:** All Android Engineers, Compose UI Contributors, and Autonomous AI Coding Agents  
**Authority:** Mandatory Engineering & Usability Standard for `ai.emots.kishan_dynamic`  

---

## Core Philosophy

For notification intake, presentation ownership, protected surfaces and runtime
acceptance, also follow [ISLAND_BEHAVIOR_RULES.md](ISLAND_BEHAVIOR_RULES.md).

Our product mission is to provide an **iOS Dynamic Island-inspired experience on Android** that adheres to the highest standards of mobile product design, platform integration, accessibility, and functional reliability.

1. **Behave Intelligently:** The Dynamic Island must never obstruct the user, trap touches, or display stale alerts.
2. **First Viewport Priority:** The initial visible area of every screen must prioritize the information and actions required for its primary purpose.
3. **Clarity Over Clutter:** Interfaces must be self-explanatory with predictable visual hierarchy and no contradictory states.
4. **Robust Platform Discipline:** Use supported Android APIs, handle system insets accurately, and respect system lifecycles.

---

## RULE 01 — No Hidden Primary Actions

Critical actions must never be buried under preview panels, decorative cards, or secondary information.

- **Dashboard / Home Screen (`PlaygroundScreen`):** The primary activation control must be placed prominently at the top of the initial viewport immediately below the brand header. Users must be able to see whether the island is on/off and activate or configure it within 5 seconds without scrolling.
- **Onboarding and Task Flows (`PermissionScreen`, `OnboardingScreen`):** Use `AppScreen`'s dedicated `bottomAction` slot to pin primary actions ("Continue", "Grant Required Access", "Complete Setup") above the navigation bar.
- **No Scrolling Assumptions:** Never assume users will scroll past previews to discover how to turn on or operate the core product.

---

## RULE 02 — Stable Headers

Every screen must have an intentional, stable header strategy.

- **Detail & Configuration Screens:** Must use a stable, pinned app bar (`AppTopBar`) placed in `AppScreen`'s `topBar` slot rather than inside a vertically scrollable column.
- **Back Navigation:** Must always remain visible, discoverable, and accessible with a minimum 44dp touch target.
- **Screen Identity:** The screen title and back navigation must never scroll off-screen or disappear when the user scrolls through settings or calibration sliders (e.g. Notch Alignment).
- **Root Editorial Screens:** Large titles (`AppLargeTitle`) are reserved for primary root tabs (`Island`, `Features`, `Live`, `Settings`) where editorial breathing room is appropriate.

---

## RULE 03 — Consistent Navigation

Bottom navigation must be predictable across primary destinations.

- **Active Tab Integrity:** The floating navigation dock must accurately highlight the active tab without lag or desynchronization.
- **Stable Positioning:** Positioned above the system navigation bar respecting `WindowInsets.safeDrawing`.
- **No Occlusion:** Root tab screens must supply `bottomInset = BottomDockInset` (96dp) to `AppScreen` so content at the bottom of the list is never obscured beneath the floating dock.
- **Predictable Back Behavior:** Pressing Back from sub-screens pops the back stack to the parent screen; pressing Back on root tabs prompts standard exit confirmation.

---

## RULE 04 — No Unreadable Text

Never compress controls until labels truncate, clip, or become unreadable.

- **`AppSegmented`:** Use ONLY for 2 to 4 mutually exclusive choices with short labels (e.g., "Left", "Centre", "Right" or "Compact", "Expanded").
- **`AppScrollableTabs`:** Use for 5+ top-level categories or dynamic collections (e.g., the 20 Live Activity options). Must support smooth horizontal scrolling, auto-scroll to selection, and uncompressed labels.
- **Automatic Fallback Guard:** `AppSegmented` must automatically delegate rendering to `AppScrollableTabs` if `items.size > 4` to permanently prevent label compression regressions.
- **No Text Shrinking Hacks:** Never solve layout overflow by reducing font sizes below readable thresholds (< 11sp).

---

## RULE 05 — No Excessive Empty Space

Every major area of the interface must justify its allocated space.

- **Content-Aware Preview Heights:** Preview containers must adapt to the height of their content. Compact 36.67dp pills should be presented in a compact 116dp stage; expanded activities should use proportional 156dp – 238dp stages.
- **Eliminate Empty Black/White Voids:** Avoid giant fixed-height preview cards that push primary controls below the fold.
- **Useful Information Density:** Whitespace must improve clarity and scannability, not hide functionality.

---

## RULE 06 — One Global Spacing System

All spacing, margins, and paddings must consume the centralized `AppTheme.spacing` token scale:

| Token | Dimension | Purpose |
|:---|:---|:---|
| `AppTheme.spacing.xxs` | `2.dp` | Micro-offsets, badge borders |
| `AppTheme.spacing.xs` | `4.dp` | Inline icon gaps, tight text pairings |
| `AppTheme.spacing.sm` | `8.dp` | Card element spacing, chip padding |
| `AppTheme.spacing.md` | `12.dp` | Standard row padding, internal card gutters |
| `AppTheme.spacing.lg` | `16.dp` | Primary screen gutters, card separation |
| `AppTheme.spacing.xl` | `20.dp` | Section spacing, top bar bottom padding |
| `AppTheme.spacing.xxl` | `24.dp` | Large section headers, editorial margins |
| `AppTheme.spacing.xxxl` | `32.dp` | Screen bottom clearance, hero spacing |

**Rule:** Never introduce ad-hoc hardcoded padding values (`13.dp`, `17.dp`) in layout code.

---

## RULE 07 — Consistent Typography

All text elements must consume semantic typography tokens defined in `AppTheme.typography`:

- **Display & Large Titles:** `AppTheme.typography.h1` (Bold, 30sp, tracking -0.5sp)
- **Screen & Section Titles:** `AppTheme.typography.h2` (SemiBold, 20sp, tracking -0.3sp)
- **Card & Group Headings:** `AppTheme.typography.h3` (SemiBold, 17sp)
- **Body Text:** `AppTheme.typography.body` (Regular, 15sp)
- **Supporting Descriptions:** `AppTheme.typography.bodySmall` (Regular, 13sp)
- **Interactive Button Text:** `AppTheme.typography.button` (Medium/SemiBold, 15sp)
- **Captions & Secondary Notes:** `AppTheme.typography.caption` (Regular, 12sp)
- **Island Metrics & Timers:** `AppTheme.typography.islandTime` (SemiBold Monospace, 14sp)

**Rule:** Always support system font scale up to 200% without text clipping.

---

## RULE 08 — Consistent Colors

All colors must be sourced from semantic tokens in `AppTheme.colors`:

- **Background & Surfaces:** `background`, `surface`, `surfaceElevated`, `surfaceVariant`
- **Text:** `textPrimary`, `textSecondary`, `textTertiary`
- **Accents:** `accent`, `info`, `gold`
- **Feedback & Semantics:** `success` (active/on), `warning` (action needed), `error` (paused/failed)
- **Controls & Dividers:** `border`, `controlTrack`, `controlThumb`, `disabled`

**Rule:** Never define hardcoded hex colors or invent disparate definitions of success and warning across screens.

---

## RULE 09 — Consistent Components

Equivalent UI elements across all screens must reuse canonical design kit components:

- **Scaffolding:** `AppScreen`, `AppTopBar`, `AppLargeTitle`, `AppStage`
- **Controls:** `AppButton`, `AppSwitch`, `AppSlider`, `AppScrollableTabs`, `AppSegmented`, `AppChip`
- **Content Surfaces:** `AppCard`, `AppListCard`, `AppNavRow`, `AppToggleRow`, `AppInfoRow`, `AppStatusPill`
- **Island Components:** `DynamicIslandPill`, `IslandActivationCard`

---

## RULE 10 — Content-Aware Layouts

Avoid rigid fixed heights on text- or card-heavy components.

- **Dynamic Growth:** Allow containers to grow using `heightIn(min = ...)` rather than hardcoded heights.
- **No Positioning Hacks:** Never use fixed pixel offsets or hardcoded margins to disguise layout flaws. Use proper Compose alignment and weights.
- **Multilingual Resilience:** Accommodate label expansion in languages such as German or Spanish without truncating critical data.

---

## RULE 11 — Correct Scrolling

Every screen must have an explicit, collision-free scrolling model:

- **Prevent Nested Scroll Conflicts:** Never nest scrollable rows inside unconstrained scrollable parents without explicit bounded heights.
- **Safe Inset Awareness:** Always consume `WindowInsets.safeDrawing`, navigation bar insets, and `Modifier.imePadding()` on text input sheets.
- **No Occluded Items:** Fixed action bars and floating docks must have corresponding bottom content clearance.

---

## RULE 12 — Clear State Feedback

Every meaningful user action must produce immediate, unambiguous feedback:

- **Action States:** Starting, Enabled, Disabled, Connecting, Paused, Failed, Completed.
- **No Phantom Progress:** Do not show indefinite progress spinners for instantaneous local preference changes.
- **Honest Feedback:** Never indicate "Ready" if an underlying system service is paused or unconfigured.

---

## RULE 13 — No Fake Functionality

Strictly demarcate real system integrations from simulated visual demonstrations:

- **Simulated Capabilities:** Features inspired by iOS hardware that cannot execute natively on Android (AirDrop, Satellite SOS, Find My) must carry an explicit "Simulation" badge and clarifying footnote.
- **Native Android Features:** Features backed by real Android APIs (Media Playback via `MediaSessionManager`, Phone Calls via `TelecomManager`, Charging via `BatteryManager`, Notifications via `NotificationListenerService`) must operate genuinely without simulation mocks.

---

## RULE 14 — Correct Action Hierarchy

Every task-oriented screen must provide exactly one visually dominant primary action:

- **Primary:** High-contrast solid button (`AppButtonStyle.Primary`) for the main affirmative path ("Set up Dynamic Island", "Turn on Island", "Save").
- **Secondary / Subordinate:** Tonal or border button (`AppButtonStyle.Secondary` or `AppButtonStyle.Tonal`) for optional actions ("Preview", "Later").
- **Destructive Actions:** Explicit red tint (`AppButtonStyle.Destructive`) for permanent deletions or full resets.

---

## RULE 15 — Accessibility

Full compliance with Android Accessibility standards:

- **Touch Targets:** Minimum 48dp × 48dp interactive bounding boxes (using `minimumInteractiveComponentSize()`).
- **Semantic Roles:** Explicitly declare `Role.Button`, `Role.Switch`, `Role.Tab` via Compose semantics.
- **TalkBack Descriptions:** Meaningful, concise `contentDescription` on all icon buttons and status indicators.
- **Motion Reduction:** Respect `LocalReducedMotion` by replacing spring translations with instantaneous alpha fades.

---

## RULE 16 — Responsive Design

Interfaces must adapt gracefully across device diversity:

- **Screen Widths:** 320dp (compact), 360dp (standard), 412dp (large), and unfolded tablets.
- **Display Cutouts:** Notch calibration tool must support center punch hole, left punch hole, pill cutout, and waterdrop notches.
- **Navigation Modes:** Seamlessly support 3-button navigation, 2-button navigation, and gesture navigation without layout collisions.

---

## RULE 17 — Predictable Gestures

Touch and gesture interactions must be intuitive and forgiving:

- **Tap to Expand / Collapse:** Tapping the compact island expands details; tapping outside or swiping collapses it.
- **Swipe-to-Dismiss:** Swiping up on the island dismisses transient alerts without killing background services.
- **Visible Alternatives:** Never rely solely on hidden multi-finger gestures for core workflows.

---

## RULE 18 — Functional Error Recovery

Every failure state must offer an actionable recovery path:

- **Missing Permission:** One-tap button navigating directly to the system permission screen.
- **Service Paused by OS:** One-tap "Reactivate Island" action opening accessibility settings or battery optimization whitelist.
- **Network Lost:** Informative dialog with "Try Again" and "Open Settings" recovery actions.

---

## RULE 19 — Purposeful Animation

Motion must be smooth, restrained, and purposeful:

- **Fluid Springs:** Use `AppMotion.islandSpring()` (dampingRatio 0.82f, stiffness 380f) for island transitions.
- **No Visual Jitter:** Dimension transitions must be smoothly interpolated via `animateDpAsState` rather than abrupt jumps.
- **Respect User Settings:** Completely disable spring physics when the system or app "Reduce Motion" setting is active.

---

## RULE 20 — No Contradictory UI States

A screen must never display contradictory status information:

- **Centralized Derivation:** Derive island operational status strictly through `IslandActivationPolicy`.
- **Coherent Controls:** When the service is paused or unconfigured, do not display a raw switch; display the required action button.
- **Synchronized Previews:** Live preview badges must agree with real background overlay health.

---

## RULE 21 — Progressive Disclosure

Present essential information and core actions first:

- **Top of Page:** Brand identity, operational status, primary toggle/setup action.
- **Middle of Page:** Interactive preview and experience selector.
- **Bottom of Page / Sub-screens:** Deep configuration options (Notch alignment, audio visualizers, call sensitivities).

---

## RULE 22 — Respect Platform Behavior

Adhere strictly to Android platform APIs and security requirements:

- **Overlay Window Management:** Use `TYPE_ACCESSIBILITY_OVERLAY` or `TYPE_APPLICATION_OVERLAY` responsibly.
- **Touch Pass-Through:** In compact and hidden states, shrink window layout params to wrap visible content or apply `FLAG_NOT_TOUCHABLE` with 0×0 dimensions so touches pass freely to underlying apps.
- **Transient Alert Auto-Expiry:** Informational alerts (charging, ringer mode, Bluetooth, mobile data) must automatically collapse or dismiss within 4000ms.

---

## RULE 23 — Preserve User Context

Never destroy user work or state unnecessarily:

- **Tab Switching:** Preserve scroll positions and preview selections across bottom tabs.
- **Settings Navigation:** Returning from Android system settings must refresh permission status immediately without resetting user selections.
- **Configuration Changes:** Retain entered notch calibration sliders during device orientation changes.

---

## RULE 24 — Performance Is Part of UX

Maintain 60/120fps fluid rendering and minimal battery impact:

- **Recomposition Skipping:** Use `@Immutable` data models and stateless composables with hoisted lambda callbacks.
- **Zero Polling Loops:** Use event-driven `NetworkCallback`, `BroadcastReceiver`, and `SharedFlow` rather than CPU-burning polling loops.
- **Lifecycle Cleanups:** Cancel coroutine scopes and unregister broadcast receivers immediately upon service or screen disposal.

---

## RULE 25 — Every Screen Must Pass a UX Quality Gate

Before any screen or UI modification is merged, verify:

1. **First Viewport:** Primary action and identity are immediately clear without scrolling.
2. **Header Stability:** Navigation and titles remain pinned during scrolling.
3. **Control Readability:** No compressed or truncated text labels.
4. **Touch Target Standard:** All interactive controls satisfy 48dp minimum targets.
5. **State Accuracy:** Status badges and actions strictly reflect genuine system readiness.
6. **Device Validation:** Physically verified on a connected Android device or verified through automated unit tests.

---

## RULE 26 — Notification Identity, Capability and Action Integrity

- Scope summary/child grouping to the source package and Android group key;
  use the full notification key for updates, never matching sender text alone.
- Full source updates are authoritative: removed copy, images and actions must
  not be resurrected from an earlier snapshot.
- Pager pages and height caches use stable notification IDs. Commit user
  selection only for a settled surviving key, not intermediate/stale indices.
- Render specialized notifications from real metadata/capabilities, not brand
  names or arbitrary words in a message. Unknown sources use a generic card.
- Free-text replies require an actual free-form RemoteInput. Action identity
  must not be a reusable slot pointing to a replacement PendingIntent.
- Handoff is not delivery confirmation. Failed reply dispatch keeps the editor;
  removed/redacted sources close it and release keyboard/window focus.
- Keep JVM fixtures, Android framework instrumentation and real-app acceptance
  explicitly separate in engineering reports. Never substitute one for another.

---

## RULE 27 — Aurora Brand & Design System Parity

Refer to [AURORA_DESIGN_SYSTEM.md](AURORA_DESIGN_SYSTEM.md) for complete visual tokens:
- **Light & Dark Mode Parity:** Both dark (obsidian/slate) and light (porcelain/cloud) modes must exhibit high-contrast, intentional design without washed-out cards or flat unbranded grey backgrounds.
- **Controlled Spectral Highlights:** Reserve Aurora Cyan (`#00F2FE`) and Aurora Violet (`#7F00FF`) for primary brand accents, active selection states, and live dynamic island visualizers.
- **Component Reusability:** Authors must use components from `ui/kit/` (`AppKit`, `AppControls`, `AppPaywallComponents`). Ad-hoc cards, inline gradients, and custom button implementations are prohibited.

---

## RULE 28 — Paywall Integrity & Zero Overlay Collisions

Refer to [PREMIUM_PAYWALL_GUIDELINES.md](PREMIUM_PAYWALL_GUIDELINES.md) and [VISUAL_QA_CHECKLIST.md](VISUAL_QA_CHECKLIST.md):
- **Overlay Suppression:** The floating Dynamic Island overlay must be 100% suppressed (`isPaywallActive = true`) on all paywalls (`PremiumScreen`, `ProPaywallSheet`) to prevent CTA occlusion.
- **Sticky Dock Clearance:** Scrollable content must specify adequate `bottomInset` (e.g. `72.dp`) to ensure the lowest legal footnotes and action buttons scroll completely clear of sticky conversion docks.
- **Demonstrate Before Demanding:** Paywalls must feature live, interactive Dynamic Island sandboxes demonstrating value before requiring purchase.
- **Honest Play Store Terms:** Always display live localized prices from Google Play Billing with clear recurring terms and an accessible "Restore purchases" action.

---

## RULE 29 — Ad Placement Discipline & 0dp Collapsibility

Refer to [AD_PLACEMENT_RULES.md](AD_PLACEMENT_RULES.md) and [ADMOB_ARCHITECTURE.md](ADMOB_ARCHITECTURE.md):
- **Zero Ads in Floating Overlays:** The floating Dynamic Island window managed by `FloatingIslandService` must NEVER render or trigger any ad format.
- **0dp Failure Collapse:** When an ad fails to load or when a user is ad-free (Pro or Rewarded Pass), native ad cards and banner slots MUST collapse completely to 0dp height and 0dp margin. Never leave empty grey rectangles or layout blanks.
- **Shimmer While Loading:** Always render `NativeAdShimmer` matching the card's target dimensions (`Medium` or `Big`) while an ad request is pending to eliminate Cumulative Layout Shift (CLS).
- **Accidental Click Clearance:** Always maintain a minimum 16dp spacing between native ad CTA buttons and primary app navigation elements.

---

## RULE 30 — Interstitial Grace & Ad Loading UX

- **Pulsing Glass Loader:** Before presenting an interstitial ad, display `AdLoadingDialog` with subtle pulsing Aurora dots. Never snap abruptly to black screens.
- **Strict 6-Second Timeout:** If an interstitial fails to load within 6.0 seconds, dismiss the dialog immediately and permit the user's intended navigation without penalty.
- **Zero Cold-Launch Ads:** Never trigger App Open or Interstitial ads on initial cold launch or during initial permissions setup.

---

## RULE 31 — 7-Day Installation Trial, Rewarded Passes & First-Viewport Entitlement
Refer to [PREMIUM_ENTITLEMENT_RULES.md](PREMIUM_ENTITLEMENT_RULES.md) and [INSTALL_TRIAL_RULES.md](INSTALL_TRIAL_RULES.md):
- **First Viewport Status Card (`HomePremiumStatusCard`):** The Home screen must immediately display active entitlement (7-Day Trial with countdown, Rewarded Pass with countdown, Pro Activated, or Free tier) in the first viewport above permissions and preview controls.
- **24-Hour Rewarded Ad Pass (`AdRewardDialog`):** Non-paying users can stack 24h passes by watching rewarded ads, complete with a 15-second cooldown timer and 7-day calendar checkmark progress.
- **Proactive Expiry Island (`IslandState.PremiumExpiry`):** When a free or expired user taps the notch, surface a dedicated floating island with direct "Watch Ads" and "Buy Premium" buttons.

---

## RULE 32 — Sub-Pixel Hardware Calibration & Custom RGB Controls
Refer to [DYNAMIC_ISLAND_BEHAVIOR.md](DYNAMIC_ISLAND_BEHAVIOR.md):
- **Sub-Pixel Steppers (`PrecisePixelControl`):** All notch alignment and offset sliders must provide `[−]` and `[+]` stepping buttons for exact hardware punch-hole alignment.
- **Universal Free Calibration:** Basic vertical positioning, width scaling, normal notch style, and camera cutout selection are 100% free forever.
- **Custom RGB Accent Engine (`IslandColorPicker`):** Pro users can customize full 32-bit ARGB background and foreground colors with real-time ARGB hex and channel sliders.
- **Idle Auto-Hide:** Provide configurable idle collapse timeout (3s to 60s) with automatic re-expansion on system notifications, charging, or volume changes.


