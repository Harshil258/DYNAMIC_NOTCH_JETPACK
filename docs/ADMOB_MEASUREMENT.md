# Google AdMob Measurement & Impression-Level Ad Revenue (ILAR)

## 1. Overview
The **Android Dynamic Island** application implements Impression-Level Ad Revenue (ILAR) tracking via Google AdMob, forwarding impressions in real-time to Google Analytics 4 (GA4) under the canonical `ad_impression` event.

---

## 2. Ad Formats & Revenue Callbacks

All ad formats use the modern Google Mobile Ads SDK (`com.google.android.libraries.ads.mobile.sdk.common.AdValue`):

| Ad Format | Gateway Class | Ad Callback Method | Placement Value |
| :--- | :--- | :--- | :--- |
| **App Open** | `MobileAdsAppOpenGateway` | `AdEventCallback.onAdPaid(adValue)` | `"cold_launch"` / `"warm_resume"` |
| **Interstitial** | `MobileAdsInterstitialGateway` | `AdEventCallback.onAdPaid(adValue)` | `"tab_switch"` |
| **Rewarded Pass** | `MobileAdsRewardedPassGateway` | `AdEventCallback.onAdPaid(adValue)` | `"pro_rewarded_pass"` |
| **Native Ad** | `MobileAdsNativeAdGateway` | `AdEventCallback.onAdPaid(adValue)` | `"settings_native"` / `"features_native"` |
| **Banner Ad** | `MobileAdsBannerGateway` | `AdEventCallback.onAdPaid(adValue)` | `"bottom_dock_banner"` |

---

## 3. Revenue Conversion Formula & GA4 Schema

In `AdMobRevenueTracker`:
```kotlin
val revenueInCurrencyUnits = adValue.valueMicros / 1_000_000.0

val params = mapOf(
    AnalyticsParamNames.VALUE to revenueInCurrencyUnits,
    AnalyticsParamNames.CURRENCY to adValue.currencyCode,
    AnalyticsParamNames.AD_PLATFORM to "admob",
    AnalyticsParamNames.AD_FORMAT to adFormat,
    AnalyticsParamNames.AD_UNIT_ID to adUnitId,
    AnalyticsParamNames.PLACEMENT to placement,
    AnalyticsParamNames.PRECISION_TYPE to adValue.precisionType.name
)

analyticsTracker.logEvent(AnalyticsEventNames.AD_IMPRESSION, params)
```

### Micro-Units Example:
- An impression paying `1,500,000` micros in USD is logged as:
  - `value`: `1.5`
  - `currency`: `"USD"`
  - `ad_platform`: `"admob"`
  - `precision_type`: `"ESTIMATED"`

---

## 4. Strict Zero-Overlay Ad Policy Compliance

As mandated by [`docs/MONETIZATION_RULES.md`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/docs/MONETIZATION_RULES.md):
- **Zero Ads in Overlays:** No ad views, SDK instances, or interstitials are ever presented inside the Accessibility Overlay (`IslandOverlayService`).
- **Immediate Pro Suppression:** All ad gateways immediately suppress ad requests when `isProActive == true`. No ad requests or revenue callbacks fire for active subscribers.
- **Fail-Safe Collapsible Layouts:** Banner and native ad containers collapse to `0.dp` immediately upon ad load error or fill failure.
