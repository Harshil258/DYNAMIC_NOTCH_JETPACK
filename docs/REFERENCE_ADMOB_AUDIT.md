# Reference Project AdMob & Firebase Remote Config Comprehensive Audit

## Executive Summary

This document presents a comprehensive technical audit of the reference Android project located at:
`/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14`

The reference application implements a centralized, production-grade monetization system powered by the **Next-Gen Google Mobile Ads SDK** (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1`), **User Messaging Platform (UMP) SDK 4.0.0**, and **Firebase Remote Config** (BOM `33.7.0`) via JSON schemas (`adsdetailsnew` and `premium_config`), backed by AndroidX Preferences DataStore.

This audit establishes the baseline architectural specifications, lifecycle patterns, frequency management mechanisms, and failsafe policies to be adapted and implemented within **Aurora Dynamic Island**.

---

## 1. Project Specifications & Dependencies

### Reference Application Stack
- **Project Path:** `/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14`
- **Application ID:** `com.dynamicnotchnotificationbar.dynamicbar`
- **Compile / Target SDK:** 35 / 35 (Min SDK: 26)
- **Kotlin:** 2.0.21
- **Gradle:** 8.9.0 / AGP 8.7.2
- **UI Framework:** Jetpack Compose (BOM `2024.10.01`) + View interop for AdMob Native & Banner views

### Monitization & Remote Configuration Dependencies
```kotlin
// Google Mobile Ads Next-Gen SDK (NOT legacy play-services-ads)
implementation("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1")
implementation("com.google.android.ump:user-messaging-platform:4.0.0")

// Firebase BOM & Remote Config
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-config-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Google Play Billing
implementation("com.android.billingclient:billing-ktx:8.3.0")

// Local Persistence for Remote Config & Counters
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## 2. Remote Configuration Architecture

### 2.1 Remote Keys & JSON Payload Structure
The reference project bundles its entire ad and monetization rules into a unified, versioned JSON payload under the primary key `adsdetailsnew`, supplemented by `premium_config`:

1. **`adsdetailsnew`**: Master configuration object parsed into `AdsConfig`:
   - **Global Toggles:**
     - `isAdsEnabled` (Boolean): Master kill switch for all advertising.
     - `isTestModeEnabled` (Boolean): Enforces official Google test ad unit IDs.
     - `adsStartFromScreen` (Int, default: 0): Minimum screen transitions before full-screen ads begin.
   - **Format-Specific Ad Unit IDs:**
     - `admobBannerId` (String): e.g. `ca-app-pub-4592759972304958/9884587664`
     - `admobInterstitialId` (String): e.g. `ca-app-pub-4592759972304958/9483422409`
     - `admobNativeId` (String): e.g. `ca-app-pub-4592759972304958/2528741163`
     - `admobAppOpenId` (String): e.g. `ca-app-pub-4592759972304958/4437207434`
     - `admobRewardedId` (String): e.g. `ca-app-pub-4592759972304958/7864434685`
   - **Feature & Placement Toggles:**
     - `isResumeAdEnabled` (Boolean): Controls App Open ads upon process foreground resume.
     - `isSplashInterstitialEnabled` (Boolean): Controls interstitial display on cold start splash.
     - `isBackInterstitialEnabled` (Boolean): Controls interstitial display when pressing back.
     - `isExitNativeEnabled` (Boolean): Displays a native ad inside the exit confirmation dialog.
   - **Throttling & Frequency Counters:**
     - `clickCounterForInterstitial` (Int, default: 30): Click threshold for interstitial triggers.
     - `screenCounterForInterstitial` (Int, default: 2): Screen navigation transition frequency.
   - **Preloading Flags:**
     - `isPreloadInterstitial` (Boolean, default: true)
     - `isPreloadNative` (Boolean, default: true)
     - `isPreloadBanner` (Boolean, default: true)
     - `isPreloadAppOpen` (Boolean, default: true)
   - **UI Variations:**
     - `bigNativePercentage` (Int, default: 0): Ratio of large (media) vs compact (icon-only) native ads.
   - **Nested `PremiumConfig`:**
     - `isPremiumEnabled` (Boolean): Paywall availability.
     - Plan identifiers: `weeklyPlanId`, `monthlyPlanId`, `yearlyPlanId`, `lifetimePlanId`.
     - `features`: Array of unlocked premium feature keys.
     - `trustSection`: Title, statistics (`1.1K+`, `4.9+`, `30+`), and social proof toggles.

### 2.2 Lifecycle & Fetching Policy (`RemoteConfigManager.kt`)
- **Initialization:** Sets minimum fetch interval to `0` seconds in debug builds and `3600` seconds (1 hour) in release builds.
- **Fetch Strategy:** Calls `firebaseRemoteConfig.fetchAndActivate()` wrapped in a strict 6-second timeout (`withTimeoutOrNull(6000)`).
- **Fallback Hierarchy:**
  1. Remote Config fetched and parsed successfully.
  2. If fetch fails or times out, loads cached payload from `AdsDataStore`.
  3. If cache is empty, uses hardcoded default `AdsConfig()`.
- **Local Persistence (`AdsDataStore.kt`):** Uses AndroidX Preferences DataStore to store:
  - `ads_config`: Raw JSON payload.
  - `click_counter`: Persisted user interaction count.
  - `screen_counter`: Persisted screen transition count.
  - `config_fetched`: Boolean indicator of successful fetch.
  - `last_fetch_time`: Timestamp of last successful refresh.

---

## 3. Deep Audit of Ad Formats & Managers

### 3.1 App Open Ads (`AppOpenAdManager.kt`)
- **SDK Class:** `com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd`
- **Trigger:** Process lifecycle foregrounding observed via `DefaultLifecycleObserver` on `ProcessLifecycleOwner.get().lifecycle`.
- **Freshness & Expiry:** Enforces a 4-hour validity limit (`AD_EXPIRY_HOURS = 4`). Expired ads are discarded and reloaded.
- **Preloading & Capacity:** Preloads 1 active ad into memory (preloading attempt capped at 2 consecutive retries).
- **Mutual Exclusion & Collision Avoidance:**
  - **Cold Launch Exclusion:** Skipped on cold startup (`isColdLaunch = true`) to prevent collision with the splash flow or initial onboarding.
  - **Permission Gate:** Suppressed when `isPermissionScreenActive = true`.
  - **Call Screen Gate:** Suppressed on call-related activities (`CallSummaryActivity`, `CallHistoryActivity`).
  - **Full-Screen Collision Gate:** Suppressed if an interstitial or rewarded ad is currently showing (`isShowingFullScreenAd = true`).
  - **Pro User Bypass:** Completely suppressed if `preManager.isAdFree() == true`.

### 3.2 Interstitial Ads (`InterstitialAdManager.kt`)
- **SDK Class:** `com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd`
- **Preloading:** Maintains 1 cached ad in memory. Automatically invokes `preload()` upon ad dismissal or failure.
- **Loading UX:** Features `showAdWithLoader(activity, onAdDismissed)`. Displays a glassmorphic `AdLoadingDialog` with animated pulsing dots for up to 6 seconds while checking ad readiness or awaiting a pending load.
- **Trigger Evaluation:**
  - Increments `screenCounter` on screen transitions. When `screenCounter % config.screenCounterForInterstitial == 0`, ad is eligible.
  - Increments `clickCounter` on interactive actions. When `clickCounter % config.clickCounterForInterstitial == 0`, ad is eligible.
  - Respects `adsStartFromScreen`: blocks all interstitials until the user has navigated at least `adsStartFromScreen` screens.
- **Mutual Exclusion:** Sets `AdsManager.isShowingFullScreenAd = true` while showing; blocks App Open ads from firing during dismiss transitions.

### 3.3 Native Ads (`NativeAdManager.kt` & `NativeAdCard.kt`)
- **SDK Classes:** `com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd`, `NativeAdLoader`, `NativeAdView`, `MediaView`, `AdChoicesView`
- **Preloading & Cache:** Preloads 1 cached ad (`MAX_CACHED_ADS = 1`).
- **Reuse Prevention:** Implements `showNativeAdOnce(placement)`. Native ads rendered in one view are never reused elsewhere, eliminating AdMob policy violations related to recycled native views.
- **UI Variants (`NativeAdCard.kt`):**
  - `NativeAdSize.Medium`: Compact two-row layout (Icon + Info on row 1, Action CTA on row 2).
  - `NativeAdSize.Big`: Full media layout with `MediaView`, headline, advertiser badge, body copy, AdChoices badge, and high-contrast CTA button.
  - **Shimmer State:** Shows `NativeAdShimmer` matching the card dimensions while loading.
  - **Collapsibility:** Emits 0dp height / collapses completely when load fails or when user is ad-free.

### 3.4 Banner & Adaptive Banner Ads (`BannerAdManager.kt` & `BannerAdView.kt`)
- **SDK Classes:** `com.google.android.libraries.ads.mobile.sdk.banner.BannerAd`, `AdView`, `BannerAdRequest`, `AdSize`
- **Adaptive Sizing:** Calculates current window width and uses `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)`.
- **Refresh Callback:** Implements `BannerAdRefreshCallback` for logging and telemetry.
- **Lifecycle Destruction:** Enforces explicit `bannerAdState?.destroy()` within Compose `DisposableEffect`.

### 3.5 Rewarded Ads (`RewardedAdManager.kt`)
- **SDK Class:** `com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd`
- **Reward Model:** Upon user watching a complete rewarded ad, grants a 7-day temporary ad-free pass (`RewardedPassPolicy`).
- **Entitlement Verification:** Persists reward expiry timestamp locally. Checked via `preManager.isAdFree()`.

---

## 4. Architectural Comparison: Reference vs. Aurora

| Dimension | Reference Application (`DynamicNotificationBar_VC14`) | Current Aurora Application (`dynamic_kishan`) | Target Enhanced State for Aurora |
| :--- | :--- | :--- | :--- |
| **GMA SDK Generation** | Next-Gen SDK (`ads-mobile-sdk:1.2.1`) | Next-Gen SDK (`ads-mobile-sdk:1.2.1`) | **Aligned:** Maintain Next-Gen SDK. |
| **Remote Config Source** | Firebase Remote Config (`firebase-config-ktx`) | HTTP URL fetcher (`RemoteReleaseRuntimeConfigProvider`) | **Upgrade:** Implement native `FirebaseRemoteConfig` fetching `adsdetailsnew` JSON with local fallback. |
| **Persistence Layer** | AndroidX Preferences DataStore | SharedPreferences (`AuroraPreferences`, `ad_frequency`) | **Retain & Extend:** Use SharedPreferences / DataStore for atomic counter & JSON caching. |
| **Ad Coordinator** | Centralized Singleton `AdsManager` | Distributed Gateways (`MobileAds*Gateway`) | **Refactor:** Create centralized `AdManager` coordinator while maintaining clean Gateway boundaries. |
| **Interstitial Loader** | Glassmorphic `AdLoadingDialog` with 6s timeout | Immediate show attempt without loader | **Implement:** Add glassmorphic `AdLoadingDialog` for smooth interstitial transitions. |
| **Native Ad Display** | Shimmer, Medium/Big sizes, Gradient border | Standard Card with static layout | **Adopt:** Multi-size `NativeAdCard` (`Medium` & `Big`) with shimmer placeholder. |
| **Exit Dialog Ad** | Controlled by `isExitNativeEnabled` | Plain confirmation dialog | **Implement:** Add optional native ad in `ExitConfirmationDialog` gated by `isExitNativeEnabled`. |
| **Zero-Overlay Policy** | Ad-free check in Activity/UI | Strict architectural rule: zero ads in overlay | **Maintain 100%:** Floating overlay service has 0 ad dependencies. |
| **Revenue Tracking** | Basic logging | ILAR via `AdMobRevenueTracker` & GA4 | **Preserve & Unify:** Route all format impressions to `AdMobRevenueTracker`. |

---

## 5. Architectural Implementation Roadmap for Aurora

1. **Remote Configuration Migration:**
   - Define strongly typed `AdConfiguration` matching reference JSON schema (`adsdetailsnew`).
   - Implement `FirebaseRemoteConfigManager` supporting atomic JSON fetch with local cache fallback and 6-second timeout.
   - Update `AppReleaseRuntimeConfig` to bridge Firebase Remote Config into Aurora's `ReleaseRuntimeConfig`.

2. **Core Ad Architecture Upgrades:**
   - Create unified `AdManager` coordinating `AppOpenAdManager`, `InterstitialAdManager`, `NativeAdManager`, `BannerAdManager`, and `RewardedAdManager`.
   - Implement glassmorphic `AdLoadingDialog` for interstitial transitions with 6-second timeout.
   - Add `Medium` and `Big` size options to `NativeAdCard` with shimmer loading states.
   - Enhance `ExitConfirmationDialog` to render a native ad when `isExitNativeEnabled` is active and user is not Pro.

3. **Strict Policy & Safety Gates:**
   - Ensure zero ads on floating dynamic island overlay.
   - Ensure zero ads on paywall screens (`isPaywallActive`).
   - Ensure 100% ad suppression when Pro is active or rewarded pass is valid.
   - Ensure App Open ads are suppressed on cold launch, during permissions flow, and when full-screen ads are active.

4. **Testing & Device Validation:**
   - Unit tests covering configuration parsing, counter policies, and eligibility gates.
   - Device build, installation, and visual validation on Realme 8 test hardware.
