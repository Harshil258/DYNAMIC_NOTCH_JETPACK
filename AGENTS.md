# Autonomous Coding Agent Instructions — Android Dynamic Island

**Playground-first workflow:** Before further production island geometry or animation tuning, use the debug-only [Dynamic Island Playground](docs/DYNAMIC_ISLAND_PLAYGROUND.md), run its checks, and inspect captures. Playground overrides must not write production preferences.

**Mandatory island layout contract:** Read [docs/ISLAND_LAYOUT_RULES.md](docs/ISLAND_LAYOUT_RULES.md) before changing island geometry or content. It supersedes reference-only fixed heights, universal compact minimum widths, single-line-only expanded copy, and 44dp touch-target guidance.

All coding agents working on this codebase must adhere to the permanent design, architecture, and reliability standards established for this project.

Before making any changes to UI components, screens, services, or overlay lifecycle code, you **MUST** read and follow:

👉 **[`docs/UI_UX_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/UI_UX_RULES.md)**  
👉 **[`DYNAMIC_ISLAND_RULEBOOK.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/DYNAMIC_ISLAND_RULEBOOK.md)**

👉 **[`docs/ISLAND_BEHAVIOR_RULES.md`](docs/ISLAND_BEHAVIOR_RULES.md)** — mandatory intake, lifecycle, interaction, privacy and verification contract.  
👉 **[`docs/PREMIUM_PRODUCT_RULES.md`](docs/PREMIUM_PRODUCT_RULES.md)** — mandatory Free vs. Pro feature matrix, entitlement verification, and paywall rules.  
👉 **[`docs/MONETIZATION_RULES.md`](docs/MONETIZATION_RULES.md)** — strict AdMob ad placements, zero-overlay ad policy, and UMP consent standards.  
👉 **[`docs/PRODUCT_EXPERIENCE_RESEARCH.md`](docs/PRODUCT_EXPERIENCE_RESEARCH.md)** — global product benchmarks (Apple, Spotify, YouTube Premium, Duolingo, Notion).  
👉 **[`docs/ANALYTICS_EVENT_CATALOG.md`](docs/ANALYTICS_EVENT_CATALOG.md)** — complete GA4 event schema, parameters, screen deduplication, and user properties.  
👉 **[`docs/FIREBASE_CRASHLYTICS.md`](docs/FIREBASE_CRASHLYTICS.md)** — Crashlytics configuration, Play Billing connection drop reporting, and rate limiting.  
👉 **[`docs/GOOGLE_ADS_CONVERSIONS.md`](docs/GOOGLE_ADS_CONVERSIONS.md)** — Google Ads conversion actions, purchase idempotency, and ROAS measurement.  
👉 **[`docs/ADMOB_MEASUREMENT.md`](docs/ADMOB_MEASUREMENT.md)** — Impression-Level Ad Revenue (ILAR) tracking via `onAdPaid(adValue)`.  
👉 **[`docs/PRIVACY_AND_CONSENT.md`](docs/PRIVACY_AND_CONSENT.md)** — Google Consent Mode v2, UMP integration, and strict zero-PII contract.  
👉 **[`docs/REFERENCE_ADMOB_AUDIT.md`](docs/REFERENCE_ADMOB_AUDIT.md)** — comprehensive audit and comparison with the reference monetization project.  
👉 **[`docs/ADMOB_ARCHITECTURE.md`](docs/ADMOB_ARCHITECTURE.md)** — complete GMA Next-Gen SDK architecture, gateways, and ILAR measurement.  
👉 **[`docs/FIREBASE_REMOTE_CONFIG.md`](docs/FIREBASE_REMOTE_CONFIG.md)** — Firebase Remote Config schema (`adsdetailsnew`), 6s timeout, and fallback rules.  
👉 **[`docs/AD_PLACEMENT_RULES.md`](docs/AD_PLACEMENT_RULES.md)** — strict ad placement rules, quarantine zones, and 0dp collapsibility.  
👉 **[`docs/MONETIZATION_TEST_MATRIX.md`](docs/MONETIZATION_TEST_MATRIX.md)** — comprehensive monetization and Remote Config test matrix.  
👉 **[`docs/AURORA_DESIGN_SYSTEM.md`](docs/AURORA_DESIGN_SYSTEM.md)** — complete Aurora design tokens, dark/light theme parity, and component specifications.  
👉 **[`docs/PREMIUM_PAYWALL_GUIDELINES.md`](docs/PREMIUM_PAYWALL_GUIDELINES.md)** — premium paywall architecture, interactive island preview, and conversion psychology.  
👉 **[`docs/VISUAL_QA_CHECKLIST.md`](docs/VISUAL_QA_CHECKLIST.md)** — visual and UX quality assurance checklist.  
👉 **[`docs/PREMIUM_ENTITLEMENT_RULES.md`](docs/PREMIUM_ENTITLEMENT_RULES.md)** — 7-tier entitlement engine, priority resolution, DataStore persistence, and tampering mitigation.  
👉 **[`docs/INSTALL_TRIAL_RULES.md`](docs/INSTALL_TRIAL_RULES.md)** — 7-day install trial contract, monotonic clock validation, Remote Config duration overrides.  
👉 **[`docs/REWARDED_ACCESS_RULES.md`](docs/REWARDED_ACCESS_RULES.md)** — 24h per ad stacking, 15-second cooldown timer, 7-day calendar checkmark progression, and ILAR tracking.  
👉 **[`docs/REMOTE_CONFIG_KEYS.md`](docs/REMOTE_CONFIG_KEYS.md)** — complete key inventory, JSON structure, and 6-second timeout contract.  
👉 **[`docs/GOOGLE_PLAY_BILLING.md`](docs/GOOGLE_PLAY_BILLING.md)** — Weekly, Monthly, Yearly, Lifetime plan catalog, localized price display, and purchase idempotency.  
👉 **[`docs/FEATURE_ENTITLEMENT_MATRIX.md`](docs/FEATURE_ENTITLEMENT_MATRIX.md)** — complete Free vs. Pro comparison matrix and policy rules.  
👉 **[`docs/DYNAMIC_ISLAND_BEHAVIOR.md`](docs/DYNAMIC_ISLAND_BEHAVIOR.md)** — notch calibration, custom RGB colors, idle auto-hide, and Expiry Island contract.

---

## Non-Negotiable Instructions for Future Agents

1. **Read UI/UX Rules First:** Always consult [`docs/UI_UX_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/UI_UX_RULES.md) before authoring or modifying any Jetpack Compose screen or component.
2. **Reuse Existing Design Tokens & Kit Components:** Use tokens in `AppTheme.spacing`, `AppTheme.colors`, and `AppTheme.typography`. Reuse components from `AppKit.kt` (`AppScreen`, `AppCard`, `AppTopBar`) and `AppControls.kt` (`AppButton`, `AppScrollableTabs`, `AppSegmented`, `AppSlider`). Never invent ad-hoc colors, hardcoded paddings, or duplicate buttons.
3. **Avoid Hardcoded Positioning Hacks:** Never use fixed pixel offsets or hardcoded margins to disguise layout flaws. Use proper Compose layout constraints, flexible weights, and alignment.
4. **Preserve Accessibility:** Ensure interactive touch targets meet the 48dp minimum standard. Add meaningful `contentDescription` and accessibility roles (`Role.Button`, `Role.Switch`, `Role.Tab`). Respect `LocalReducedMotion`.
5. **Verify Headers & Navigation:** Detail and configuration screens must use pinned, stable app bars (`AppTopBar` in `AppScreen`'s `topBar` slot). The back button and screen identity must never scroll off-screen.
6. **Verify Scroll & Inset Behavior:** Respect `WindowInsets.safeDrawing`, system navigation bars, and IME insets (`Modifier.imePadding()`). Provide `bottomInset = BottomDockInset` on root tabs so content is never occluded beneath the floating dock.
7. **Verify Action Visibility:** Primary actions on setup, onboarding, or task-oriented screens must remain visible and accessible (use `AppScreen`'s `bottomAction` slot).
8. **Verify Component Responsiveness:** Test and verify that screens adapt gracefully to small phones (< 360dp width), normal phones, large phones, and font scaling up to 200%.
9. **Keep State Representation Accurate:** UI state must strictly reflect genuine underlying system and service health. Never display "Ready" when a background service is inactive, and never show a misleading offline warning when valid Wi-Fi is connected.
10. **Check for Related UX Regressions:** When modifying a screen or component, review connected user flows and verify that existing interactions are not compromised.
11. **Add Regression Tests for Significant Defects:** For any P0 or P1 defect resolved, implement a dedicated unit or UI regression test to permanently guard against recurrence.
12. **Avoid Unnecessary Build Executions:** Batch code edits logically. Use static inspection and targeted unit tests (`./gradlew testDebugUnitTest`) before running full assembly builds (`./gradlew assembleDebug`). Avoid redundant `clean` commands.
13. **Prioritize First Viewport & Activation Integrity:** The initial visible area of every screen must prioritize the information and actions required for its primary purpose. On the Home screen, the core product activation control must be immediately discoverable without scrolling. UI state must strictly derive from `IslandActivationPolicy` to ensure zero contradictory states.
14. **Enforce Entitlement Single Source of Truth:** All Free vs. Pro feature checks must consult `EntitlementManager` and `FeatureEntitlementPolicy`. Never scatter hardcoded booleans or local preference overrides across composables. Never gate essential recovery, privacy, or core island controls.
15. **Zero Ads in Floating Overlays or High-Stakes Surfaces:** Under no circumstance may advertisements appear in the floating dynamic island overlay, over third-party applications, on the lockscreen, or during call operations. All in-app ads must be strictly quarantined, collapsible to 0dp on failure, and 100% disabled immediately upon Pro activation.
16. **Honest, Localized Store Billing:** All paywalls must display live localized currency from Google Play Billing `ProductDetails`, support pending purchases honestly, avoid fake scarcity/countdown timers, and provide direct links to Google Play Subscriptions management.
17. **Centralized Analytics Architecture & Screen Deduplication:** All GA4 and Firebase Analytics events must route through `AnalyticsTracker`. Screen views must use `ScreenDeduplicator` to prevent redundant `screen_view` events triggered by Jetpack Compose recompositions.
18. **Idempotent Purchase Conversion Tracking:** Purchase conversion events for Google Ads and GA4 must route exclusively through `SubscriptionConversionTracker`. Purchases must be deduplicated by transaction ID / purchase token to prevent inflated ROAS or duplicate purchase counts.
19. **Strict Zero-PII Collection Contract:** Under no circumstance may notification titles, notification message bodies, sender identities, contact names, phone numbers, OTPs, or financial details be collected in any analytics event or diagnostic key.
20. **Crashlytics Grouping Integrity & Rate-Limiting:** Diagnostic exceptions recorded to Firebase Crashlytics must use stable, unparameterized exception messages to avoid report fragmentation. Dynamic values (retry counts, timestamps, SDK versions) must be set via Crashlytics custom keys. Connection drop reporting must be rate-limited to avoid reporting floods.
21. **Impression-Level Ad Revenue (ILAR):** All AdMob ad impressions across App Open, Interstitial, Rewarded Pass, Native, and Banner formats must report real-time ad value via `AdMobRevenueTracker.trackAdRevenue` to GA4 `ad_impression`.
22. **Zero Floating Overlay Collisions on Paywalls:** Floating dynamic island overlays must be 100% suppressed (`IslandStateManager.setPaywallActive(true)`) while any paywall or purchase flow is visible.
23. **Dark and Light Theme Parity:** Maintain uncompromising contrast and craft across both dark (obsidian `#0B0F19`) and light (porcelain `#F8F9FD`) themes.
24. **Demonstrate Before Demanding:** Paywall surfaces must feature interactive Dynamic Island sandboxes demonstrating value before requiring purchase.
25. **Google Mobile Ads Next-Gen & AdMob Architecture:** All advertising must strictly use the Google Mobile Ads Next-Gen SDK (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1`). Ad lifecycles must be managed via centralized gateways, include preloading with 4-hour validity expiration, show smooth glassmorphic `AdLoadingDialog` for interstitials with 6-second timeout, feature single-use native ad registration, and report real-time ILAR via `AdMobRevenueTracker`.
26. **Firebase Remote Config & 6-Second Timeout Contract:** Dynamic runtime configurations must parse both `adsdetailsnew` JSON payloads and flat fallback keys via `ReleaseRuntimeConfigDecoder`. All remote fetch operations must enforce a strict 6-second timeout (`withTimeoutOrNull(6000)`) with graceful fallback to cached data and local defaults. Cold startup must never block or freeze on remote configuration.
27. **7-Day Install Trial & Clock Rollback Protection:** New installs receive a 7-day full-access trial managed by `TrialManager`. Monotonic clock tracking (`lastObservedTimestamp`) prevents system clock manipulation. Device reboots preserve trial duration.
28. **24-Hour Stackable Rewarded Passes with 15s Cooldown:** Non-paying users can watch rewarded video ads to earn 24h Pro feature passes managed by `RewardedAccessManager`. Passes stack seamlessly (+24h/ad), enforce a 15-second cooldown timer between views, and track progress across a 7-day calendar checkmark interface. Real-time ILAR must report to GA4.
29. **Hardware Notch Calibration & Custom RGB Colors:** Sub-pixel notch calibration (`PrecisePixelControl`), normal vs monobrow notch styling, custom RGB ARGB colors (`IslandColorPicker`), and idle auto-hide must be supported. Basic notch alignment must remain 100% free.
30. **Developer Attribution & Support:** Application identity, About screen, Feedback screen, and legal policies must permanently attribute developer "Surya Developer" and support email `suryadevelopers07@gmail.com`.
31. **Strict Scope Constraint — Zero Cricket:** Under no circumstance may any cricket score, cricket API, live cricket match notification, or cricket-related UI be implemented or reintroduced into Aurora Dynamic Island. All live activities are restricted to generic system notifications, timers, and authorized media playback.
