# Aurora Dynamic Island — Monetization & Remote Config Test Matrix

**Version:** 2.0.0  
**Effective Date:** 17 October 2026  
**Target Hardware:** Realme 8 (RMX3085, Android 13, API 33) & Automated JVM Unit Test Suite  
**Authority:** Mandatory Verification Protocol for `ai.emots.kishan_dynamic`  

---

## 1. Overview

This document specifies the comprehensive Quality Assurance test matrix for all monetization pathways, Google Mobile Ads Next-Gen formats, Firebase Remote Config ingestion, and user entitlement states in Aurora Dynamic Island.

---

## 2. Test Matrix 1: User Entitlement & Ad Suppression

| Test ID | User Tier / State | Expected Ad Behavior | Overlay Behavior | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| **ENT-01** | Free Tier (No active subscription, no pass) | All enabled ad formats active (App Open, Interstitial, Native, Banner). | Floating island active over other apps; 0 ads in overlay. | **PASS** (Device Verified) |
| **ENT-02** | Aurora Pro (Monthly / Yearly / Lifetime) | 100% ad bypass. All ad cards collapse to 0dp. Interstitials and App Open blocked. | Floating island active; all Pro customization unlocked. | **PASS** (Unit & Device) |
| **ENT-03** | Rewarded Pass (Valid within 7 days) | 100% ad bypass identical to Pro subscriber. | Floating island active. | **PASS** (Unit Verified) |
| **ENT-04** | Rewarded Pass (Expired > 7 days) | Reverts to Free Tier ad eligibility automatically. | Floating island active. | **PASS** (Unit Verified) |
| **ENT-05** | Paywall Screen Active (`isPaywallActive`) | All ad requests blocked. Floating overlay suppressed to prevent CTA overlap. | Overlay hidden until paywall dismissed. | **PASS** (Device Verified) |

---

## 3. Test Matrix 2: Remote Config Ingestion & Fallbacks

| Test ID | Scenario | Network Condition | Expected Config Resolution | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| **RCF-01** | Cold Launch with Connectivity | Online (Wi-Fi / Cellular) | Fetches `adsdetailsnew` JSON via `FirebaseRemoteConfig`, updates `StateFlow`. | **PASS** (Device Verified) |
| **RCF-02** | Fetch Timeout (> 6000ms) | Artificial High Latency | Timeout fires at 6.0s; falls back to locally cached payload without UI freeze. | **PASS** (Unit Verified) |
| **RCF-03** | Cold Launch Offline (First Install) | Airplane Mode | Cache empty; cleanly falls back to `DefaultLocalReleaseRuntimeConfigProvider`. | **PASS** (Unit Verified) |
| **RCF-04** | Corrupted / Malformed JSON | Mocked invalid JSON payload | Decoder catches `JSONException` cleanly and preserves previous valid config. | **PASS** (Unit Verified) |
| **RCF-05** | Flat Fallback Key Ingestion | Missing `adsdetailsnew` | Decoder parses individual flat keys (`is_ads_enabled`, `admob_interstitial_id`). | **PASS** (Unit Verified) |

---

## 4. Test Matrix 3: Google Mobile Ads Next-Gen Format Verification

| Format | Trigger / Placement | Key Requirements | Test Criteria | Status |
| :--- | :--- | :--- | :--- | :--- |
| **App Open** | Process returns to foreground from background. | Cold launch skipped; 4h expiry enforced; suppressed on paywall/onboarding. | 1. Background app for 5s and resume -> Ad shows (if free).<br>2. Cold launch -> Splash/Home appears instantly without ad. | **PASS** |
| **Interstitial** | Navigating between sub-screens or exiting tool. | Throttled by `screenCounter` (3) & `clickCounter` (30); `AdLoadingDialog` shown. | 1. Navigate screens -> Counter increments.<br>2. Reaching threshold triggers `AdLoadingDialog` (pulsing dots).<br>3. Interstitial displays and completes. | **PASS** |
| **Native Ad (Medium)** | Exit Confirmation Dialog (`isExitNativeEnabled`). | Single-use view registration; 0dp collapse on failure. | 1. Press Back on root tab -> Exit dialog displays Medium native card.<br>2. Pro user sees only confirmation buttons. | **PASS** (Device Verified) |
| **Native Ad (Big)** | Settings Vault scrollable footer. | 16:9 MediaView, high contrast CTA, Aurora theme, shimmer while loading. | 1. Open Settings -> Shimmer appears while loading.<br>2. Ad loads -> MediaView and CTA render cleanly without overflow. | **PASS** (Device Verified) |
| **Banner Ad** | Anchored adaptive banner at view bottom. | Matches screen width; destroyed in `DisposableEffect`. | 1. Rotates with orientation change.<br>2. No memory leak on view exit. | **PASS** |
| **Rewarded Pass** | Settings Vault / Paywall "7-Day Ad-Free Pass". | User watches complete video; grants 7-day pass. Abandoning grants 0 pass. | 1. Complete watch -> Toast confirms pass; ads collapse.<br>2. Dismiss halfway -> No pass granted. | **PASS** |

---

## 5. Test Matrix 4: Policy & Safety Compliance

| Policy Item | Compliance Rule | Verification Method | Result |
| :--- | :--- | :--- | :--- |
| **Zero Floating Overlay Ads** | No ads inside `FloatingIslandService` overlay window. | Source inspection + runtime overlay inspection over homescreen and 3rd-party apps. | **100% COMPLIANT** |
| **Zero Lockscreen Ads** | No ads displayed on device lockscreen. | Lock device while app running -> Screen wakes without ads. | **100% COMPLIANT** |
| **Zero Phone Call Interruption** | Interstitials blocked during incoming or ongoing calls. | Simulate phone call state -> Interstitials suppressed. | **100% COMPLIANT** |
| **Accidental Click Clearance** | Minimum 16dp spacing between native ad CTA and app navigation. | Layout inspection in Jetpack Compose layout inspector. | **100% COMPLIANT** |
| **Impression Revenue Tracking** | Every impression reports ILAR via `onAdPaid`. | Telemetry logs inspect `ad_impression` events sent to Firebase Analytics. | **100% COMPLIANT** |

---

## 6. Automated Unit & Integration Test Summary

| Test Class | Purpose | Tests | Result |
| :--- | :--- | :--- | :--- |
| `ReleaseProviderContractTest` | Validates decoder, nested `adsdetailsnew` JSON, and flat key fallbacks. | 3 | **3 / 3 PASS** |
| `AdFrequencyPolicyTest` | Validates click/screen counter thresholds and `adsStartFromScreen`. | 4 | **4 / 4 PASS** |
| `EntitlementPolicyTest` | Validates Pro status, Rewarded Pass expiry, and Free tier ad eligibility. | 5 | **5 / 5 PASS** |
| `PaywallIntegrityTest` | Validates `isPaywallActive` overlay suppression and ad blocking. | 3 | **3 / 3 PASS** |
| **TOTAL** | | **15** | **100% PASS** |

---

---

## 8. Test Matrix 5: 7-Day Trial, Rewarded Stacking & Google Play Billing

| Test ID | Feature Area | Test Scenario | Expected Outcome | Result |
| :--- | :--- | :--- | :--- | :--- |
| **TRIAL-01** | 7-Day Installation Trial | App cold launch on first install | Starts 7-day trial, sets `startTime` and `expiresAt`, unlocks all Pro features. | **PASS** (Unit & Device) |
| **TRIAL-02** | Anti-Tampering Engine | User sets system clock back > 5 min | Monotonic clock detects rollback (`lastObservedTimestamp`), invalidates cheat. | **PASS** (Unit Verified) |
| **TRIAL-03** | Reboot Persistence | Device reboots or app restarts | DataStore timestamps remain intact; trial countdown persists accurately. | **PASS** (Unit & Device) |
| **REW-01** | 24-Hour Pass Stacking | User completes 1 rewarded ad | Adds +24 hours to existing expiry (or now). Stacks cumulatively. | **PASS** (Unit Verified) |
| **REW-02** | 15-Second Cooldown | User attempts immediate second ad | Watch button disabled with live countdown (`Please wait (Xs)...`). | **PASS** (Device Verified) |
| **REW-03** | 7-Day Progress Track | User earns passes across days | Checkmarks light up chronologically in `AdRewardDialog` (7-day window). | **PASS** (Device Verified) |
| **EXP-01** | Expiry Island Overlay | Non-entitled user taps notch | Posts `IslandState.PremiumExpiry(0)` with "Watch Ads" and "Buy Premium". | **PASS** (Unit & Device) |
| **BILL-01** | 4-Tier Plan Catalog | Paywall loaded | Displays Weekly (₹39), Monthly (₹59), Yearly (₹99), Lifetime (₹149). | **PASS** (Device Verified) |
| **BILL-02** | Non-Consumable Lifetime | User purchases lifetime plan | Granted permanent Pro; no expiry timestamp; overrides temporary passes. | **PASS** (Unit Verified) |
| **BILL-03** | Restore Purchases | User taps Restore in Settings | Queries active subscriptions and in-app purchases, synchronizes DataStore. | **PASS** (Device Verified) |

---

## 9. Automated Unit & Integration Test Suite Summary

| Test Class | Purpose | Tests | Result |
| :--- | :--- | :--- | :--- |
| `TrialManagerTest` | Clock rollback detection, snapshot calculations, and duration bounds. | 4 | **PASS** |
| `RewardedAccessManagerTest` | 24h stacking arithmetic, 15s cooldowns, and checkmark progression. | 4 | **PASS** |
| `EntitlementManagerTest` | 7-tier entitlement hierarchy and priority resolution. | 4 | **PASS** |
| `FeatureEntitlementPolicyTest` | Essential free vs Pro feature gating. | 4 | **PASS** |
| `ReleaseProviderContractTest` | Validates decoder, nested `adsdetailsnew` JSON, and flat key fallbacks. | 3 | **PASS** |
| `LiveActivityPrioritizerTest` | Event lifecycle, notification reducer, and island ownership policy. | 6 | **PASS** |
| `TOTAL UNIT TESTS` | Complete Kotlin unit test suite (`./gradlew testDebugUnitTest`) | **25+** | **100% PASS** |

---

## 10. Device Verification Evidence (Realme 8 Hardware)

- **Device Model:** Realme 8 (RMX3085)
- **Android OS:** Android 13 (Realme UI 4.0, API 33)
- **Connection Transport:** ADB over Wi-Fi (`192.168.29.20:5555`)
- **Key Artifacts Captured:**
  - `screen_home_trial_live.png`: First viewport status card with `7-Day Free Trial [TRIAL]`, `7 days remaining`, `[► Watch Ad (+1 Day)]`, and `[👑 Keep Pro]`.
  - `screen_settings_awake.png`: Settings vault showing `7-Day Trial Active`, `[• Trial · 7d]`, and 4-tier plan selector (`Weekly ₹39`, `Monthly ₹59`, `Yearly ₹99`, `Lifetime ₹149`).
  - `screen_reward_dialog_exact.png`: `AdRewardDialog` bottom sheet with 7 date indicator checkmarks, 15-second countdown timer, and `"Watch Ad to Unlock (+24h)"`.
  - `screen_paywall_plans.png`: Full paywall with interactive Dynamic Island sandbox demo and catalog pricing.
