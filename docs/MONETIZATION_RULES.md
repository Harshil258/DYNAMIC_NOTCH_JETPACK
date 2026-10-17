# Google AdMob & Monetization Rules: Ethical Advertising Standard

**Document:** `docs/MONETIZATION_RULES.md`  
**Authority:** Mandatory Ad Placement, Lifecycle, and User Experience Standard for `ai.emots.kishan_dynamic`  
**Applicability:** All Google AdMob integrations, ad slots, and revenue mechanisms

---

## 1. Zero-Overlay Ad Tolerance (Critical Restriction)

> [!CAUTION]
> **UNDER ABSOLUTELY NO CIRCUMSTANCE MAY ADVERTISEMENTS BE DISPLAYED INSIDE OR ATTACHED TO THE FLOATING DYNAMIC ISLAND OVERLAY, OVER OTHER APPLICATIONS, ON THE LOCKSCREEN, OR WITHIN SYSTEM NOTIFICATIONS.**

- The Dynamic Island is an operating system utility surface. Injecting ads into the overlay violates Google Play policies, creates severe touch obstruction, triggers immediate uninstalls, and risks account suspension.
- All ad placements must reside strictly within the standard Activity window of our application (`SettingsVaultScreen`, `StudioScreen`).

---

## 2. Permitted vs. Forbidden Placements

### Permitted Surfaces
1. **Settings Vault Bottom Slot (`AdaptiveBannerAdSlot`):** Anchored adaptive banner positioned at the bottom of the scrollable settings list with semantic spacing.
2. **Features Discovery Card (`NativeAdCard`):** Visibly distinct card styled with `AppTheme` tokens, clearly displaying the required "Ad" or "Sponsored" attribution badge.
3. **Opt-In Rewarded Pass:** Voluntary user action on the Premium screen to unlock temporary Pro access (e.g., 24h pass) by watching an ad.

### Strictly Forbidden Surfaces
- Inside or over `IslandOverlayService`.
- Over other third-party apps or system UI.
- On the `PlaygroundScreen` (Home Dashboard).
- During first-time onboarding or `PermissionScreen`.
- While answering, declining, or conducting phone calls.
- Over critical navigation buttons, back buttons, or sliders.
- During active Google Play checkout.
- On cold launch or immediately upon app startup.
- Every N button clicks inside settings (strictly prohibited).

---

## 3. Resilient Ad Failure Handling

1. **No Blank Holes:** If an ad fails to load (`AdLoadResult.Failure`), the ad slot must collapse cleanly to `0dp` height. Never leave an empty, blank gray/white box in the UI.
2. **No Blocking Navigation:** If an ad is loading or preloading, navigation and user actions must NEVER be blocked or delayed.
3. **No Retries in Rapid Loops:** Ad reload attempts must follow exponential backoff. Never poll or loop requesting ads when offline.

---

## 4. Pro Tier Immediate Ad Suppression

1. **Centralized Ad Gating:** All ad slots (`AdaptiveBannerAdSlot`, `NativeAdCard`, `MobileAdsAppOpenGateway`, `MobileAdsInterstitialGateway`) must check:
   ```kotlin
   if (isProActive) return // Zero ad views created, zero network requests
   ```
2. **Immediate Removal:** When a user completes a purchase or activates a valid pass, currently loaded ad views must be destroyed immediately without requiring an app restart.

---

## 5. Consent & Privacy (UMP SDK)

1. **Google UMP Integration:** The User Messaging Platform (UMP) SDK must handle privacy consent in the European Economic Area (EEA), UK, and other applicable jurisdictions.
2. **Ad Requests Honor Consent:** `AdConsentPolicy.canRequestAds(consentState)` must return `true` before any AdMob initialization or ad loading begins.
3. **No Sensitive Data Leaks:** Never transmit notification text, contact names, package names, or device identifiers to ad networks.
