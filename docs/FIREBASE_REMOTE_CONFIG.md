# Aurora Dynamic Island — Firebase Remote Config Architecture & Schema

**Version:** 2.0.0  
**Effective Date:** 17 October 2026  
**Audience:** Android Engineers, Backend/Monetization Architects, and Autonomous Coding Agents  
**Authority:** Mandatory Specification for Remote Configuration in `ai.emots.kishan_dynamic`  

---

## 1. Overview & Architectural Goals

**Firebase Remote Config** enables dynamic runtime control over advertising behavior, feature flags, UI experiments, and subscription offerings without requiring app store updates.

In **Aurora Dynamic Island**, Remote Config is designed with four strict engineering requirements:
1. **Zero Cold-Start Freezes:** Strict 6-second timeout on network fetches; cold launches never block on network responses.
2. **Deterministic Fallbacks:** Tiered fallback hierarchy ensuring the app functions reliably in offline or airplane mode.
3. **Reactive Flow Integration:** Remote configuration is exposed via Kotlin `StateFlow<ReleaseRuntimeConfig>`, allowing Jetpack Compose screens to react cleanly to updates.
4. **Backward & Forward Compatibility:** Seamlessly parses both unified JSON schemas (`adsdetailsnew`, `premium_config`) and individual flat key-value overrides.

```mermaid
flowchart TD
    subgraph Remote["Firebase Backend"]
        FRC["Firebase Remote Config Service\n('adsdetailsnew' & 'premium_config')"]
    end

    subgraph Client["Aurora Application Runtime"]
        FRP["FirebaseRemoteConfigRuntimeProvider"]
        DEC["ReleaseRuntimeConfigDecoder"]
        CACHE["Local Cache (SharedPreferences / DataStore)"]
        DEF["DefaultLocalReleaseRuntimeConfigProvider (Hardcoded Defaults)"]
        STF["config: StateFlow<ReleaseRuntimeConfig>"]
    end

    subgraph Consumers["Subsystems & Screens"]
        STF --> ADM["AdManager & Ad Gateways"]
        STF --> ENTM["EntitlementManager & Paywall"]
        STF --> UI["Jetpack Compose UI (collectAsState)"]
    end

    FRC -->|fetchAndActivate (6s timeout)| FRP
    FRP -->|Success| DEC
    FRP -->|Timeout / Network Fail| CACHE
    CACHE -->|Empty / Parse Fail| DEF
    DEC --> CACHE
    DEC --> STF
    DEF --> STF
```

---

## 2. Remote Keys & Schema Specification

### 2.1 Primary Master Key: `adsdetailsnew`
The reference monetization model encapsulates advertising configuration in a single, versioned JSON object under the key `adsdetailsnew`:

```json
{
  "isAdsEnabled": true,
  "isTestModeEnabled": false,
  "adsStartFromScreen": 0,
  "admobBannerId": "ca-app-pub-3940256099942544/6300978111",
  "admobInterstitialId": "ca-app-pub-3940256099942544/1033173712",
  "admobNativeId": "ca-app-pub-3940256099942544/2247696110",
  "admobAppOpenId": "ca-app-pub-3940256099942544/9257395921",
  "admobRewardedId": "ca-app-pub-3940256099942544/5224354917",
  "isResumeAdEnabled": true,
  "isSplashInterstitialEnabled": false,
  "isBackInterstitialEnabled": true,
  "isExitNativeEnabled": true,
  "clickCounterForInterstitial": 30,
  "screenCounterForInterstitial": 3,
  "isPreloadInterstitial": true,
  "isPreloadNative": true,
  "isPreloadBanner": true,
  "isPreloadAppOpen": true,
  "bigNativePercentage": 50,
  "premium_config": {
    "isPremiumEnabled": true,
    "weeklyPlanId": "aurora_pro_weekly",
    "monthlyPlanId": "aurora_pro_monthly",
    "yearlyPlanId": "aurora_pro_yearly",
    "lifetimePlanId": "aurora_pro_lifetime",
    "features": ["all_islands", "ad_free", "custom_theme", "pro_animations"],
    "trustSection": {
      "title": "Trusted by 1M+ Users",
      "rating": "4.9+",
      "reviews": "30K+"
    }
  }
}
```

### 2.2 Schema Parameter Definitions

| Field Name | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `isAdsEnabled` | Boolean | `true` | Master kill switch for all advertising formats across the app. |
| `isTestModeEnabled` | Boolean | `false` | When true, forces official Google Mobile Ads test ad unit IDs. |
| `adsStartFromScreen` | Int | `0` | Number of screen navigations before full-screen ads begin triggering. |
| `admobBannerId` | String | Test Banner | AdMob ad unit ID for adaptive banner ads. |
| `admobInterstitialId` | String | Test Interstitial | AdMob ad unit ID for interstitial transitions. |
| `admobNativeId` | String | Test Native | AdMob ad unit ID for native cards (Medium & Big). |
| `admobAppOpenId` | String | Test App Open | AdMob ad unit ID for foreground resume App Open ads. |
| `admobRewardedId` | String | Test Rewarded | AdMob ad unit ID for 7-day rewarded ad-free pass. |
| `isResumeAdEnabled` | Boolean | `true` | Toggles App Open ads on process foreground resumption. |
| `isSplashInterstitialEnabled` | Boolean | `false` | Controls whether an interstitial is shown on initial splash (disabled by default for smooth UX). |
| `isBackInterstitialEnabled` | Boolean | `true` | Controls whether pressing back on sub-screens can trigger an interstitial. |
| `isExitNativeEnabled` | Boolean | `true` | Enables a native ad inside the back-press exit confirmation dialog. |
| `clickCounterForInterstitial` | Int | `30` | Number of user clicks/taps required before an interstitial is eligible. |
| `screenCounterForInterstitial` | Int | `3` | Number of screen transitions required before an interstitial is eligible. |
| `isPreloadInterstitial` | Boolean | `true` | Enables proactive background preloading for interstitials. |
| `isPreloadNative` | Boolean | `true` | Enables proactive background preloading for native ads. |
| `isPreloadBanner` | Boolean | `true` | Enables proactive background preloading for banners. |
| `isPreloadAppOpen` | Boolean | `true` | Enables proactive background preloading for App Open ads. |
| `bigNativePercentage` | Int | `50` | Percentage (0-100) determining the frequency of `Big` vs `Medium` native ads. |
| `premium_config.isPremiumEnabled`| Boolean | `true` | Master toggle controlling visibility of Aurora Pro paywalls. |

---

## 3. Fetching Strategy & Lifecycle Policy

### 3.1 Fetch Intervals
- **Debug Builds:** `fetchIntervalSeconds = 0` (instant refresh on every launch for rapid QA).
- **Release Builds:** `fetchIntervalSeconds = 3600` (1 hour cache to conserve device battery and network bandwidth).

### 3.2 6-Second Network Timeout
```kotlin
val fetchResult = withTimeoutOrNull(6000L) {
    firebaseRemoteConfig.fetchAndActivate().await()
}
```
If the network response exceeds 6 seconds, the operation times out cleanly without throwing an unhandled exception or freezing the application startup.

### 3.3 The Tiered Fallback Hierarchy
1. **Tier 1 (Fresh Remote Fetch):** Parse newly fetched Remote Config parameters if successful.
2. **Tier 2 (Cached Remote Payload):** If fetch times out or fails (e.g. offline), retrieve the most recently cached JSON payload from local persistence.
3. **Tier 3 (Local Static Defaults):** If cache is empty (first cold start with no connectivity), use hardcoded default `ReleaseRuntimeConfig` values.

---

## 4. Decoder Architecture (`ReleaseRuntimeConfigDecoder.kt`)

The decoder supports two structural representations without external serialization libraries:
1. **Nested JSON Object:** Directly decodes the `adsdetailsnew` JSON object and its nested `premium_config` sub-object.
2. **Flat Key-Value Fallbacks:** If `adsdetailsnew` is absent, decodes individual Remote Config string/boolean keys (e.g. `is_ads_enabled`, `admob_interstitial_id`).

### Unit Test Coverage
The decoder is verified with 100% test pass rates in `ReleaseProviderContractTest`:
- `testDefaultLocalProviderContract`: Ensures default fallback configuration is valid.
- `testDecoderParsesFlatPayload`: Validates individual string/boolean parsing.
- `testDecodingReferenceProjectAdsDetailsNewPayload`: Validates the full nested `adsdetailsnew` schema.

---

## 5. Security & Verification Rules

1. **Never Ship Production Secrets in Remote Config:**
   - Ad unit IDs and feature toggles are public configurations. Never place API keys, private signing certificates, or sensitive secrets in Remote Config.
2. **Test Unit ID Enforcement in Debug:**
   - In debug builds, `ReleaseRuntimeConfig.isTestModeEnabled` defaults to `true` or is automatically overridden to enforce Google test IDs.
3. **No Dynamic Code Evaluation:**
   - Remote Config parameters only toggle pre-compiled flags and layout variants. Dynamic script execution or remote class loading is strictly forbidden under Google Play policies.
