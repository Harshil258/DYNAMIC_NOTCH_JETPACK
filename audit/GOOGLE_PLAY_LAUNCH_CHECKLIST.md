# AURORA DYNAMIC ISLAND — GOOGLE PLAY LAUNCH CHECKLIST
## Pre-Release Compliance, Data Safety, Permission Declarations & Store Readiness

**Package Name:** `ai.emots.kishan_dynamic`  
**Target SDK:** 35 (Android 15 Ready) | **Min SDK:** 24 (Android 7.0)  
**Developer Entity:** Surya Developer (`suryadevelopers07@gmail.com`)  
**Audit Status:** Release Candidate Approved (Pre-launch Sign-off)

---

## 1. Google Play Data Safety Disclosures

Google Play requires complete and accurate Data Safety disclosures regarding what user data is collected, shared, and stored.

### Data Collection & Sharing Declaration:
| Data Category | Specific Data Type | Collected? | Shared with Third Parties? | Handled Locally On-Device Only? | Security & Processing Purpose |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Personal Info** | Name, email address, phone number | **NO** | **NO** | YES (Caller phone numbers formatted locally for dialer/WhatsApp; never stored remotely) | Zero personal info collected |
| **Messages** | SMS, WhatsApp, notification text | **NO** | **NO** | **YES (100% on-device)** | Notification text processed in memory for island rendering; zero PII sent to servers or analytics |
| **Financial Info** | UPI IDs, card info, bank balances | **NO** | **NO** | **YES (Masked on lockscreen)** | Payment notification detection for temporary HUD; monetary balances redacted; zero financial data collected |
| **Location** | Precise or coarse location | **NO** | **NO** | **N/A** | Zero location permissions declared or requested (`AUR-P0-005` resolved) |
| **Photos & Videos** | Photos, videos, camera | **NO** | **NO** | **N/A** | Zero camera permissions declared; system camera launched via Intent |
| **App Activity** | App interactions, screen views | **YES** | **NO** | Analytics Only | GA4 anonymized product events (e.g., `screen_view`, `feature_discovery`); deduplicated; zero PII |
| **Diagnostics** | Crash logs, performance metrics | **YES** | **NO** | Diagnostics Only | Firebase Crashlytics rate-limited non-fatal billing error diagnostics; zero user data attached |
| **Device Identifiers** | Advertising ID (AAID) | **YES** | **YES (Google AdMob / UMP)** | Ad Serving Only | Standard Google Mobile Ads Next-Gen SDK for ad serving and UMP consent compliance |

### Security Practices Declaration:
- **Data Encrypted in Transit:** YES (All network communications via HTTPS/TLS 1.3).
- **Data Deletion Request:** Supported via email to `suryadevelopers07@gmail.com`.
- **Zero-PII Compliance Contract:** Enforced via `AnalyticsSchemaValidator.kt` and `LockScreenPrivacyPolicy.kt`.

---

## 2. Google Play Permissions Justification & Declarations

Google Play enforces strict scrutiny on high-privilege permissions. Below are the definitive justifications for each declared permission:

### High-Privilege Declarations:

1. **`SYSTEM_ALERT_WINDOW` (Display over other apps)**
   - **Core Purpose:** The central feature of the application is a floating Dynamic Island overlay that renders status indicators, timers, playback controls, and incoming notifications over other applications and the Android home screen.
   - **Policy Compliance:** The overlay respects `FLAG_NOT_TOUCHABLE` when idle or hidden (`AUR-P1-010`), never blocks system emergency dialogues, and contains zero advertisements in the floating surface (Rule 15).

2. **`BIND_ACCESSIBILITY_SERVICE` (Accessibility API)**
   - **Declaration Justification:** Required solely to detect window state transitions (`typeWindowStateChanged | typeWindowsChanged`) to adjust overlay visibility during fullscreen video playback and lockscreen states.
   - **Hardened Configuration (`AUR-P0-006`):**
     - `canPerformGestures="false"` (Zero simulated touches or gestures).
     - `canRetrieveWindowContent="false"` (Zero screen scraping, zero inspection of typed text or window hierarchies).
     - Privacy Disclosure: Transparently explained in onboarding, Settings, and Privacy Policy.

3. **`READ_PHONE_STATE`**
   - **Core Purpose:** Required to observe incoming and ongoing telephony states (`EXTRA_STATE_RINGING`, `EXTRA_STATE_OFFHOOK`, `EXTRA_STATE_IDLE`) to display the phone call heads-up pill and provide post-call summaries.
   - **Policy Compliance:** Declared with appropriate user education in `PermissionsScreen.kt`. Never reads call audio or stores contact books remotely.

4. **`POST_NOTIFICATIONS` (Android 13+)**
   - **Core Purpose:** Displays the persistent foreground service notification required by Android to ensure the overlay service remains reliable and is not killed by the OS.

5. **`FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` (Android 14+)**
   - **Subtype:** `specialUse`
   - **Justification:** Continuous floating island overlay and system heads-up notification delivery.

### Removed Permissions Audit (`AUR-P0-005`):
- `CAMERA`: **REMOVED** from Manifest. Camera launched via system Intent only.
- `ACCESS_FINE_LOCATION`: **REMOVED** from Manifest.
- `ACCESS_COARSE_LOCATION`: **REMOVED** from Manifest.

---

## 3. Google Play Policy Compliance & Policy Centers

- **Target API Level:** Target SDK 35 satisfies Google Play's annual target API level requirements.
- **EU User Consent & GDPR (UMP):** Integrated with Google User Messaging Platform (UMP). Includes a permanently reachable "Privacy & Ad Consent" button in the About screen to reopen the consent form at any time (`AUR-P1-024`).
- **Subscription Transparency Policy:**
  - Clear localized pricing displayed before initiating billing (Weekly ₹39, Monthly ₹59, Yearly ₹99, Lifetime ₹149).
  - Explicit subscription renewal terms visible in the paywall.
  - No fabricated countdown timers or fake scarcity badges (`AUR-P1-031`).
  - Guaranteed 140dp bottom scroll clearance to prevent occlusion of cancellation disclosures.
  - Direct deep link to Google Play Subscriptions management provided.
- **Children's Policy:** App is not directed to children under 13 (Target age group: 13+).
- **Rule 31 Zero-Cricket Compliance:** Zero cricket scores, APIs, or match trackers are present in the app.

---

## 4. Google Play Store Listing Deliverables

| Asset | Requirement | Production Specification | Status |
| :--- | :--- | :--- | :---: |
| **App Title** | Max 30 chars | `Aurora Dynamic Island` | READY |
| **Short Description** | Max 80 chars | `Smart floating island notifications, media controls, and status heads-up.` | READY |
| **Full Description** | Max 4,000 chars | Comprehensive description covering Dynamic Island notch calibration, smart notifications, media playback HUD, post-call summary, 7-day free trial, and privacy-first local processing. Attributed to Surya Developer. | READY |
| **App Icon** | 512 x 512 PNG, 32-bit | Obsidian black background with glowing cyan/magenta pill glyph | READY |
| **Feature Graphic** | 1024 x 500 JPEG/PNG | Aurora glassmorphic pill showcase with live notification and media waveform | READY |
| **Phone Screenshots** | Min 2, Max 8 per form | High-resolution 1080x2400 screenshots across Dark & Light modes (`sprint13_home.png`, `sprint13_settings.png`, etc.) | READY |
| **Category** | Personalization / Tools | Personalization | READY |
| **Content Rating** | IARC Questionnaire | Everyone (PEGI 3) | READY |
| **Contact Email** | Public developer email | `suryadevelopers07@gmail.com` | READY |
| **Privacy Policy URL** | Hosted HTTPS URL | `https://suryadevelopers.github.io/aurora/privacy-policy.html` | READY |

---

## 5. Release Candidate Deployment Checklist

- [x] All 32 audit issues verified resolved or documented.
- [x] Full unit test suite passes 100% (386/386 tests, 0 failures).
- [x] Debug component gallery quarantined behind `isDebugBuild` (`AUR-P1-027`).
- [x] `buildConfig = true` enabled in `app/build.gradle.kts`.
- [x] ProGuard/R8 optimization rules verified in `app/proguard-rules.pro`.
- [x] Physical device smoke test verified on Realme 8 (Android 13).
- [x] Developer attribution verified: Surya Developer (`suryadevelopers07@gmail.com`).
- [ ] Generate production signing keystore (`aurora-release-key.jks`) — *Action required by user*.
- [ ] Configure `signingConfigs.release` in `app/build.gradle.kts` — *Action required by user*.
- [ ] Run `./gradlew bundleRelease` to generate release `.aab` bundle — *Action required by user*.
- [ ] Upload `.aab` and ProGuard mapping file to Google Play Console Internal Testing track — *Action required by user*.
