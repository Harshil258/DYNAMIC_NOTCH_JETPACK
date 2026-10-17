# Premium Product & Entitlement Rules: Dynamic Island Pro

**Document:** `docs/PREMIUM_PRODUCT_RULES.md`  
**Authority:** Mandatory Engineering Contract for Free vs. Pro Tiers, Feature Gating, and Entitlement Integrity  
**Applicability:** All screens, services, state reducers, and subscription flows in `ai.emots.kishan_dynamic`

---

## 1. Core Freemium Philosophy

1. **Utility Precedes Monetization:** The application must deliver complete, reliable, and delightful Dynamic Island utility to free users. Never cripple core functionality, introduce deliberate delays, or show fake system errors to coerce payment.
2. **Honest Premium Value:** Subscriptions must provide sustained, ongoing value (advanced personalization, priority processing, unlimited custom action island capabilities, and complete ad removal) rather than locking essential utility.
3. **Single Source of Truth:** All entitlement decisions must route through `EntitlementManager` and `FeatureEntitlementPolicy`. Never scatter ad-hoc Boolean checks or local storage overrides across individual composables.
4. **Never Gate Essentials:** Security, privacy, core overlay activation, basic notifications, basic music controls, service recovery, and purchase restoration must NEVER be gated behind a paywall.

---

## 2. Feature Entitlement Matrix

| Feature ID | Category | Free Tier | Pro Tier | Enforcement Point |
| :--- | :--- | :--- | :--- | :--- |
| `core_island_activation` | Essential | Fully Available | Fully Available | `IslandActivationPolicy` (Never Gated) |
| `basic_notifications` | Essential | Unlimited Ingestion & Pager | Unlimited Ingestion & Pager | `NotificationReducer` (Never Gated) |
| `basic_music_controls` | Essential | Play, Pause, Next, Prev | Play, Pause, Next, Prev | `MediaPlaybackRegistry` (Never Gated) |
| `basic_notch_alignment` | Essential | X, Y & Cutout Baseline | X, Y & Cutout Baseline | `DisplaySettingsScreen` (Never Gated) |
| `call_presentation` | Essential | Incoming & Ongoing Call HUD | Incoming & Ongoing Call HUD | `IslandOverlayService` (Never Gated) |
| `charging_presentation` | Essential | Battery % & Charging Status | Battery % & Charging Status | `BatteryStateReceiver` (Never Gated) |
| `bluetooth_presentation`| Essential | Device Connection Status | Device Connection Status | `BluetoothAccessoryMonitor` (Never Gated) |
| `privacy_controls` | Essential | Redact Sensitive Notifications | Redact Sensitive Notifications | `LockScreenPrivacyPolicy` (Never Gated) |
| `restore_purchases` | Essential | Available to All Users | Available to All Users | `PlayBillingPremiumAccessGateway` |
| `theme_settings` | Customization | System Default Colors | All Accent Palettes & Visualizers| `ThemeSettingsScreen` / `AppTheme` |
| `display_horizontal_offset`| Customization| Center / Baseline Offset | Fine-Grained Horizontal Sliders | `DisplaySettingsScreen` / `IslandOverlayService` |
| `sound_settings` | Personalization| Standard Audio HUD | Custom Haptic & Audio Feedback | `SoundSettingsScreen` / `DynamicIslandPill` |
| `compact_music` | Convenience | Standard Expanded Scrubbing | Ultra-Compact Pill Scrubber | `MusicSettingsScreen` / `DynamicIslandPill` |
| `notification_duration` | Customization| 2s, 3s, 5s Options | 10s, 15s Extended Durations | `NotificationSettingsScreen` |
| `quick_control` | Power User | Up to 3 Pinned Apps, 2 Contacts| Unlimited Apps, Contacts & Tiles| `QuickControlScreen` / `ActionIslandRepository` |
| `ad_free` | Experience | Non-intrusive In-App Ads | 100% Zero In-App Ads Everywhere| `AdaptiveBannerAdSlot`, `NativeAdCard`, etc. |

---

## 3. Entitlement State Lifecycle

```
[Cold Launch / App Resume]
          │
          ▼
[BillingClient Connects]
          │
          ├──> Success ──> Query Owned Purchases (SUBS & INAPP)
          │                      │
          │                      ├──> PURCHASED ──> Verify/Acknowledge ──> Set Pro Active (source = "play_billing")
          │                      ├──> PENDING   ──> Mark Pending Payment (Do NOT grant Pro; inform user)
          │                      └──> NONE      ──> Check Rewarded Pass (expiresAtMillis > now?)
          │                                              │
          │                                              ├──> Valid Pass ──> Set Pro Active (source = "rewarded_pass")
          │                                              └──> Expired/None ──> Set Free Tier Active
          │
          └──> Offline / Unavailable ──> Fallback to Cached Entitlement (AuroraPreferences)
```

1. **Purchased State Authority:** Only purchases with `PurchaseState.PURCHASED` verified and acknowledged via Google Play activate Pro entitlement.
2. **Pending Transactions:** When Google Play returns `PurchaseState.PENDING`, the app displays:  
   *"Payment is pending bank verification. Premium will activate once payment settles."*  
   Pro features remain locked until Google Play confirms final settlement.
3. **Offline Grace:** Previously verified entitlements cached in encrypted DataStore remain valid offline.

---

## 4. Paywall Rules & Anti-Dark-Pattern Directives

1. **Contextual Upgrades Only:** Do not display a full-screen blocking paywall on first launch or during onboarding. Upgrades must be introduced in context when a user interacts with a Pro feature (e.g., selecting a Pro theme or adding a 4th pinned app).
2. **Live Localized Pricing:** All plan prices must be fetched dynamically from Google Play Billing `ProductDetails` (`pricingPhases.formattedPrice`). Never display hardcoded currency strings as live store prices.
3. **Transparent Comparison:** Paywalls must provide an honest side-by-side comparison (`AppFreeVsProComparisonCard`) articulating both what is free and what is Pro.
4. **Obvious Dismissal:** Paywalls must always feature a clear "Maybe later" or close button with adequate touch target (min 44dp). Never trap users.
5. **No Deceptive Tactics:**
   - NO artificial countdown timers ("Offer expires in 04:59").
   - NO fabricated subscriber counts or false testimonials.
   - NO hidden auto-renewal disclosures; explicitly state: *"Subscriptions renew automatically unless canceled in Google Play Subscriptions."*
