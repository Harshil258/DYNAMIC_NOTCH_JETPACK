# Release Readiness & Google Play Store Compliance Checklist

**Document:** `docs/RELEASE_READINESS.md`  
**Purpose:** Pre-flight engineering audit, Google Play Policy compliance checklist, ProGuard/R8 verification, and store listing readiness for `ai.emots.kishan_dynamic`

---

## 1. Technical & Architecture Pre-Flight

- [x] **Compile & Build Success:** `./gradlew assembleDebug` and `./gradlew bundleRelease` pass without compilation errors.
- [x] **Unit & Regression Tests:** `./gradlew testDebugUnitTest` runs with 100% pass rate across core policies, state reducers, geometry constraints, and entitlement policies.
- [x] **AdMob SDK Manifest Integration:** Verified `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="@string/admob_app_id" />` present in `AndroidManifest.xml` (resolving DEF-12).
- [x] **Battery & Screen Wake Locks:** Removed `FLAG_KEEP_SCREEN_ON` and unrestricted lockscreen flags from `MainActivity` (resolving DEF-13).
- [x] **Zero Touch Trapping:** Window bounds strictly wrap content (`WRAP_CONTENT`), with `FLAG_NOT_TOUCHABLE` when idle/hidden.
- [x] **Adaptive Layouts:** Verified layout adaptability across phone screen sizes (360dp, 400dp, 600dp) and font scaling up to 200%.

---

## 2. Google Play Billing & Monetization Policy Compliance

- [x] **Play Billing Library Version:** Uses modern Google Play Billing `com.android.billingclient:billing-ktx:8.3.0`.
- [x] **Pending Purchases:** `enablePendingPurchases()` is declared with `enableOneTimeProducts()` and `enablePrepaidPlans()`. Pending purchases do not prematurely grant Pro access.
- [x] **Purchase Acknowledgment:** All purchases are acknowledged within 3 days (immediately upon confirmation) via `client.acknowledgePurchase`.
- [x] **Live Localized Pricing:** Prices, currencies, and trial periods are pre-fetched directly from `ProductDetails` rather than hardcoded mock strings.
- [x] **Transparent Subscription Terms:** Paywall clearly states auto-renewal terms, billing frequency, and offers a direct link to Google Play Subscriptions management.
- [x] **Restore Purchases:** Prominent, functional "Restore Purchases" button available on `PremiumScreen` and `AboutScreen`.

---

## 3. Google Play Policy & Data Safety Declarations

1. **Accessibility Service Declaration:**
   - App declares Accessibility Service solely to render the floating interactive dynamic island over the camera cutout.
   - App displays an explicit, prominent in-app disclosure prior to requesting the user to enable Accessibility in system settings (`PermissionExplanationSheet`).
   - App does NOT collect, store, or transmit user keystrokes, personal credentials, or financial data.
2. **Notification Listener Service Declaration:**
   - App accesses notifications solely to display active media playback, incoming calls, and message alert pills.
   - Diagnostics strictly hash source IDs (`SHA-256`); notification titles, bodies, and contact details are processed strictly on-device in memory and never sent to external servers or ad networks.
3. **Data Safety Form:**
   - *Data Collected:* None sold. Crash logs and basic aggregate app interactions collected anonymously via Firebase Analytics.
   - *Security Practices:* All network communication (release config, analytics) encrypted over HTTPS.

---

## 4. Staged Rollout Strategy

1. **Internal Testing Track:** Verify complete purchasing flow with Google Play License Testers and test cards (`always approves`, `declined`, `pending`).
2. **Closed Testing (Alpha):** 50–100 testers across diverse Android OEMs (Samsung One UI, Google Pixel, Xiaomi MIUI/HyperOS, Realme UI, OnePlus OxygenOS, Vivo FuntouchOS) to validate battery efficiency and overlay stability.
3. **Open Staged Rollout:** 5% -> 15% -> 50% -> 100% while actively monitoring Google Play Vitals (crash rate < 0.1%, ANR rate < 0.05%).
