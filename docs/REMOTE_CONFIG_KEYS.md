# Aurora Dynamic Island — Firebase Remote Config Keys & Schema

## 1. Overview & Operational Architecture
All dynamic configurations for Aurora Dynamic Island are managed through Firebase Remote Config. The app implements a **6-second network timeout** contract (`withTimeoutOrNull(6000)`) with instantaneous fallback to local defaults and cached values.

The system supports both structured JSON configurations (under the `adsdetailsnew` key) and flat root fallback parameters.

---

## 2. Master Key Inventory

| Parameter Key | Type | Default Value | Description |
| :--- | :---: | :---: | :--- |
| `adsdetailsnew` | `String (JSON)` | `{}` | Complete AdMob ad unit IDs, placement frequencies, and format switches. |
| `trial_duration_days` | `Long` / `Int` | `7` | Duration of initial installation full-feature trial in days. |
| `premium_install_trial_days` | `Long` / `Int` | `7` | Alternate/legacy key for trial duration in days. |
| `rewarded_pass_hours` | `Long` / `Int` | `24` | Duration granted per completed rewarded ad impression (hours). |
| `premium_expiry_island_enabled` | `Boolean` | `true` | Enables posting `IslandState.PremiumExpiry` on notch tap when trial/reward expires. |
| `admob_rewarded_cooldown_seconds` | `Int` | `15` | Minimum seconds required between consecutive rewarded ad viewings. |
| `ads_enabled` | `Boolean` | `true` | Master advertising kill-switch across the entire application. |
| `app_open_enabled` | `Boolean` | `true` | Controls App Open ad display on cold/warm startup. |
| `interstitial_screen_every` | `Int` | `3` | Number of screen transitions between interstitial ad triggers. |
| `interstitial_start_screen` | `Int` | `2` | Initial screen index after which interstitial impressions can begin. |
| `interstitial_loading_timeout_sec` | `Int` | `6` | Glassmorphic `AdLoadingDialog` dismissal timeout if ad presentation delays. |
| `banner_enabled` | `Boolean` | `true` | Controls collapsible adaptive banner ad slot in settings vault. |
| `native_settings_enabled` | `Boolean` | `true` | Controls native advanced ad in settings vault. |
| `big_native_percentage` | `Int` | `50` | Probability percentage (0-100) of rendering Big vs Medium native ad template. |
| `exit_dialog_native_enabled` | `Boolean` | `true` | Enables native ad card inside back-press exit confirmation dialog. |
| `update_enabled` | `Boolean` | `true` | Master in-app update checking switch. |
| `latest_version_code` | `Int` | Current versionCode | Version code of the latest published release. |
| `min_supported_version_code` | `Int` | Current versionCode | Minimum version code required (forces hard update if local < min). |
| `update_type` | `String` | `"soft"` | Update enforcement type: `"soft"` (dismissible) or `"hard"` (blocking). |
| `update_url` | `String` | Play Store URL | Target URL for manual or store updates. |
| `rating_prompt_enabled` | `Boolean` | `true` | Enables automatic Google Play in-app review prompts. |
| `rating_prompt_delay_days` | `Int` | `3` | Days of usage required before showing rating prompt. |
| `rating_min_sessions` | `Int` | `5` | Minimum app sessions required before showing rating prompt. |

---

## 3. `adsdetailsnew` JSON Payload Structure
```json
{
  "ads_enabled": true,
  "app_open_ad_id": "ca-app-pub-3940256099942544/9257395921",
  "app_open_enabled": true,
  "interstitial_ad_id": "ca-app-pub-3940256099942544/1033173712",
  "interstitial_enabled": true,
  "interstitial_screen_every": 3,
  "interstitial_start_screen": 2,
  "interstitial_loading_timeout_sec": 6,
  "rewarded_ad_id": "ca-app-pub-3940256099942544/5224354917",
  "rewarded_enabled": true,
  "rewarded_pass_hours": 24,
  "rewarded_cooldown_seconds": 15,
  "banner_ad_id": "ca-app-pub-3940256099942544/9214589741",
  "banner_enabled": true,
  "native_ad_id": "ca-app-pub-3940256099942544/2247696110",
  "native_settings_enabled": true,
  "big_native_percentage": 50,
  "exit_dialog_native_enabled": true,
  "trial_duration_days": 7,
  "premium_expiry_island_enabled": true
}
```

---

## 4. Fallback & Decoding Resilience
- `ReleaseRuntimeConfigDecoder` parses the `adsdetailsnew` JSON string first.
- If JSON parsing fails or keys are omitted, it automatically falls back to flat root parameters.
- If Remote Config is completely offline or exceeds the 6-second timeout, compiled in-app defaults are used with zero UI interruption.
