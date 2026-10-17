# Aurora Pro — Premium Paywall Architecture & Guidelines

## 1. Core Principles

Aurora Pro delivers a premium monetization experience that respects user intelligence, complies with Google Play Developer policies, and maximizes long-term subscriber lifetime value (LTV).

1. **Demonstrate Before Demanding:** Users must experience or visually preview the premium capability on the paywall itself before being asked to pay.
2. **Transparent, Localized Pricing:** All prices are dynamically retrieved via Google Play Billing `ProductDetails`. Never display mock currency or hardcoded dollar signs to international users.
3. **Zero Overlay Collisions:** Floating system overlays must never obscure paywall cards, plan selectors, or purchase buttons.
4. **Honest Value & Zero Fake Scarcity:** No artificial countdown timers, fake "only 2 left" badges, or manipulative exit traps.
5. **Frictionless Entitlement Recovery:** The "Restore purchases" action must always remain prominent and directly accessible without deep navigation.

---

## 2. Information Architecture of the Paywall

The paywall is structured in six intentional vertical tiers:

```
┌────────────────────────────────────────────────────────┐
│  Pinned AppTopBar ("Aurora Pro" + Subtitle + Back)     │
├────────────────────────────────────────────────────────┤
│  1. AuroraProHeroCard                                  │
│     - Badges: [AURORA PRO] [LIFETIME / PASS]           │
│     - Headline & Sub-headline                          │
│     - Live Dynamic Island Stage (Music, Call, Actions) │
│     - Interactive Demo Mode Switcher                   │
├────────────────────────────────────────────────────────┤
│  2. 2x2 Value-Driven Benefit Grid                      │
│     - [Fluid Island Engine]    [Action Island Pro]     │
│     - [Sub-Pixel Calibration]  [100% Ad-Free]          │
├────────────────────────────────────────────────────────┤
│  3. Plan Selector Cards                                │
│     - Weekly / Monthly / Yearly (SAVE 70%) / Lifetime   │
│     - Radio Checkmark + Monthly Equivalent Breakdown   │
│     - Rewarded Pass Option + Restore Purchases Button  │
├────────────────────────────────────────────────────────┤
│  4. Free vs. Pro Comparison Matrix                     │
│     - 7 Core Feature Rows (Included vs Unlimited)      │
├────────────────────────────────────────────────────────┤
│  5. Subscription Management (if already subscribed)    │
│     - Direct deep-link to Google Play Subscriptions    │
├────────────────────────────────────────────────────────┤
│  6. Transparent Google Play Disclosures                │
│     - Auto-renewal and cancellation terms              │
├────────────────────────────────────────────────────────┤
│  Sticky Conversion Bottom Dock                         │
│     - Selected Plan Title & Price                      │
│     - Aurora Pro CTA Button ("Continue with [Plan]")   │
│     - Trust Footnote: "Secured by Google Play"         │
└────────────────────────────────────────────────────────┘
```

---

## 3. Dynamic Island Preview Stage

The paywall hero (`AuroraProHeroCard`) contains an embedded, live interactive Dynamic Island sandbox:

- **Music Mode:** Shows live track title, artist, album art glyph, and dynamic cyan equalizer bars animated via `WaveformAnimation`.
- **Call Mode:** Shows caller identity, elapsed call timer, and active audio waveform.
- **Actions Mode:** Shows quick system shortcuts (torch, camera) demonstrating Action Island Pro capabilities.
- **Mode Switcher:** Users can tap between modes to visually verify the fluidity of the island transitions in real time.

---

## 4. Overlay Suppression Implementation

To eliminate visual interference where the floating accessibility overlay previously rendered over commercial content:

1. `IslandStateManager.kt` exposes:
   ```kotlin
   val isPaywallActive: StateFlow<Boolean>
   fun setPaywallActive(active: Boolean)
   ```
2. When any paywall enters composition (`PremiumScreen`, `ProPaywallSheet`), a `DisposableEffect` activates paywall mode:
   ```kotlin
   DisposableEffect(Unit) {
       IslandStateManager.setPaywallActive(true)
       onDispose { IslandStateManager.setPaywallActive(false) }
   }
   ```
3. `IslandOverlayService.kt` monitors `isPaywallActive`. When true, the window parameters are immediately set to `View.INVISIBLE`, width 0, height 0, and `FLAG_NOT_TOUCHABLE`.

---

## 5. Free vs Pro Entitlement Matrix

| Feature | Free Tier | Pro Tier | Enforcement Rule |
|---|---|---|---|
| **Core Dynamic Island** | Included | Included | Never gate basic island functionality |
| **Notification Pager** | Included | Included | Never gate critical alerts |
| **Basic Music Controls** | Included | Included | Free users enjoy core playback |
| **Custom Animation Themes** | Default theme only | All 12+ themes | Enforce via `FeatureEntitlementPolicy` |
| **Notch Calibration** | Basic presets | Sub-pixel sliders | Fine adjustments gated |
| **Action Island Shortcuts** | Max 3 pinned | Unlimited | Enforce in `ActionIslandManager` |
| **Custom Display Duration** | Fixed (3.5s) | Slider (1.0s - 10s) | Enforce in settings |
| **In-App Advertising** | Standard AdMob | 100% Ad-Free | Ads disabled instantly upon Pro activation |

---

## 6. Conversion Measurement & Event Schema

Every paywall interaction is tracked for Google Analytics 4 and Google Ads:

- `premium_screen_viewed(source)`: Logged when paywall opens, with deduplication.
- `plan_selected(plan_id, price)`: Logged on plan card tap.
- `subscription_initiated(plan_id)`: Logged when user taps "Continue with [Plan]".
- `subscription_purchased(transaction_id, plan_id, value, currency)`: Idempotently tracked via `SubscriptionConversionTracker`.
- `rewarded_pass_started()`: Logged when user elects to watch a rewarded video.
