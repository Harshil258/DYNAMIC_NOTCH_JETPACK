# Application-Wide UI/UX Audit & Screen Inventory

**Date:** 11 October 2026  
**Auditor:** Principal Android Engineer & Senior Mobile UX Architect  
**Target:** Android Dynamic Island (`ai.emots.kishan_dynamic`)  
**Standard:** iOS Dynamic Island Interaction Principles × Material Design 3 × Android System UI Guidelines  

---

## 1. Complete Screen Inventory

| ID | Destination / Screen | Architecture / Container | Primary User Goal | Primary Action | Navigation Pattern | Scrolling Model | Current Layout Pattern |
|:---|:---|:---|:---|:---|:---|:---|:---|
| **SCR-01** | `StartupSplashScreen` | `MainActivity` root route | App initialization & session startup | Automatic transition | None (automatic) | Non-scrolling | Full-screen brand stage with progress |
| **SCR-02** | `LanguageScreen` | `MainActivity` / `SubScreen` | Choose preferred interface language | Select language & Continue | Back (sub-screen) or Next (onboarding) | Scrollable list | Search header + radio language list + fixed bottom button |
| **SCR-03** | `OnboardingScreen` | `MainActivity` root route | Understand core value proposition | Continue / Get Started (or Skip) | Step-by-step pager or Skip to Hub | Scrollable slide body | Hero illustrations + pager dots + bottom CTAs |
| **SCR-04** | `PermissionScreen` | `MainActivity` / `SubScreen` | Grant required overlay & notification access | "Continue" or "Grant Required" | Back / Up navigation | Vertically scrollable list | Progress card + required cards + recommended cards + fixed bottom CTA |
| **SCR-05** | `PlaygroundScreen` ("Island") | Root Tab 0 in `MainContainerScreen` | Preview Dynamic Island behaviors & test states | Select experience & toggle compact/expanded | Bottom dock (Tab 0) + Settings icon | Vertical scroll with stage | Large preview stage + category chips + state pills + service status card |
| **SCR-06** | `StudioScreen` ("Features") | Root Tab 1 in `MainContainerScreen` | Discover & access feature configurations | Open feature detail settings | Bottom dock (Tab 1) | Vertical scroll | Categorized feature cards leading to sub-screens |
| **SCR-07** | `LiveActivitiesScreen` ("Live") | Root Tab 2 in `MainContainerScreen` | Explore & simulate Live Activities | Select activity & trigger demo | Bottom dock (Tab 2) | Vertical scroll | Activity selector + interactive stage + control card |
| **SCR-08** | `SettingsVaultScreen` ("Settings") | Root Tab 3 in `MainContainerScreen` | Manage system settings, themes & preferences | Configure preferences / Open sub-screens | Bottom dock (Tab 3) | Vertical scroll | Grouped settings list cards + Pro banner |
| **SCR-09** | `DisplaySettingsScreen` (Notch) | `SubScreen.DisplaySettings` | Align island capsule with physical camera punch hole | Adjust sliders (X, Y, Scale) & Save/Reset | Top bar Back button | Vertical scroll | Top preview stage + position segmented + adjustment sliders |
| **SCR-10** | `NotificationSettingsScreen` | `SubScreen.NotificationSettings` | Configure notification island behavior & filters | Toggle switches & set privacy options | Top bar Back button | Vertical scroll | Grouped setting rows with toggles and sliders |
| **SCR-11** | `MusicSettingsScreen` | `SubScreen.MusicSettings` | Customize music player island & waveform styles | Toggle music island & select visualizer | Top bar Back button | Vertical scroll | Preview stage + toggle switches + audio visualizer choices |
| **SCR-12** | `CallSettingsScreen` | `SubScreen.CallSettings` | Configure phone call island & contact options | Toggle incoming/outgoing call overlays | Top bar Back button | Vertical scroll | Call banner preview + permissions helper + options |
| **SCR-13** | `BatterySettingsScreen` | `SubScreen.BatterySettings` | Configure charging animation & low battery alert | Toggle charging & battery alerts | Top bar Back button | Vertical scroll | Charging animation preview + threshold sliders |
| **SCR-14** | `SoundSettingsScreen` | `SubScreen.SoundSettings` | Configure volume and ringer mode changes | Toggle ringer & volume alerts | Top bar Back button | Vertical scroll | Volume HUD preview + options |
| **SCR-15** | `ThemeSettingsScreen` | `SubScreen.ThemeSettings` | Switch between System, Light, and Dark modes | Select theme mode | Top bar Back button | Vertical scroll | Radio list cards with theme previews |
| **SCR-16** | `QuickControlScreen` | `SubScreen.QuickControl` / Intent | Customize shortcut buttons in expanded island | Add/remove/reorder shortcuts | Top bar Back button | Vertical scroll | Grid of action buttons + contact shortcuts |
| **SCR-17** | `PremiumScreen` | `SubScreen.Premium` / Sheet | Upgrade to Aurora Pro / Unlock features | Purchase subscription / Lifetime pass | Top bar Close / Back | Vertical scroll | Benefit cards + plan selector + fixed purchase CTA |
| **SCR-18** | `AboutScreen` | `SubScreen.About` | View app version, privacy policy, and rate | Rate on Google Play / Share app | Top bar Back button | Vertical scroll | App icon + version + legal links + developer info |
| **SCR-19** | `ComponentGalleryScreen` | `SubScreen.ComponentGallery` | Inspect all design system components | Test interactive UI controls | Top bar Back button | Vertical scroll | Comprehensive gallery of buttons, cards, typography, controls |

---

## 2. Dialog and Sheet Inventory

| ID | Dialog / Sheet | Trigger | User Purpose | Primary Action | Secondary Action |
|:---|:---|:---|:---|:---|:---|
| **DLG-01** | `PermissionExplanationSheet` | Tapping any permission row | Understand why the permission is required & grant it | "Allow Access" | "Maybe Later" / Dismiss |
| **DLG-02** | `ProPaywallSheet` | Tapping a locked Pro feature | Upgrade to Pro to unlock capability | "Unlock Pro" | "Restore Purchases" / Dismiss |
| **DLG-03** | `ExitConfirmationDialog` | Pressing Back on Root Hub | Confirm leaving the app or keeping island service active | "Stay in app" | "Exit" |
| **DLG-04** | `BatteryOptimizationDialog` | Battery setting toggle | Request disabling battery restrictions | "Open Battery Settings" | "Later" |
| **DLG-05** | `RatingDialog` | Triggered after engagement | Rate app on Google Play | "Rate 5 Stars" | "Not Now" |
| **DLG-06** | `FeedbackDialog` | Settings -> Send Feedback | Submit bug report or feature request | "Send Feedback" | "Cancel" |
| **DLG-07** | `CallHistorySheet` | Call settings / Quick control | View recent calls captured in island | Tap contact to call | Dismiss |
| **DLG-08** | `CallSummaryDialog` | Call disconnects | View call duration and quick redial | "Call Back" | "Dismiss" |
| **DLG-09** | `LegalDocumentDialog` | Privacy Policy / Terms link | Read complete legal terms | Close | None |
| **DLG-10** | `NoInternetDialog` | Network lost during active flow | Inform user of offline state | "Try Again" | "Settings" |
| **DLG-11** | `UpdateDialog` (Force/Soft) | Release config version mismatch | Download update from Google Play | "Update Now" | "Later" (Soft only) |

---

## 3. Systematic UX Defect Audit

### P0 (Critical Blockers & Overlay Traps)

1. **DEF-00: Home Screen Hides the Product's Main Action (First Viewport Failure)**
   - **Screen/Area:** `PlaygroundScreen.kt` (Root Tab 0 "Island")
   - **Root Cause:** Visual demonstration (`AppStage`) and experience selectors were prioritized over core product activation. First-time users saw an attractive preview but no obvious activation control. Setup status ("Complete setup") was buried 7 sections down (~600dp below the fold), giving the false impression that the product was already running or broken.
   - **Resolution:** Redesigned the Home screen information architecture around First Viewport Priority. Placed `IslandActivationCard` immediately below `AppLargeTitle`. Evaluated real system conditions via `IslandActivationPolicy` (distinguishing permissions, runtime service connection, and user preference). Provided direct contextual CTA ("Turn on Island", "Set up Dynamic Island", "Reactivate Island", or toggle switch). Removed redundant duplicate status and service cards at the bottom.

2. **DEF-01: Dynamic Island Traps Touches During Routine Background State Changes**
   - **Screen/Area:** Global Window Overlay (`IslandOverlayService.kt` / `SystemLiveActivityMonitor.kt`)
   - **Root Cause:** A 1500ms continuous polling loop queried `TelephonyManager.isDataEnabled` without checking validated Wi-Fi. It emitted an unexpiring `MOBILE_DATA_ALERT` with `isExpanded = true` and `expiresAtMillis = null`. The overlay layout used `expandedWidthPx` with `WRAP_CONTENT` height (~162dp tall) spanning `[24,126][1056,636]`, intercepting all touches over the permissions and settings screens.
   - **Resolution:** Replaced polling with event-driven `ConnectivityManager.NetworkCallback`. Evaluated Wi-Fi internet validity before cellular status via `NetworkConnectivityPolicy`. Enforced 4000ms auto-expiry on transient alerts. Wrapped window bounds to `WRAP_CONTENT` when compact (`611 x 134 px`) and set `FLAG_NOT_TOUCHABLE` with 0x0 size when hidden.

3. **DEF-02: Hidden Continue Button During First-Time Permissions Flow**
   - **Screen/Area:** `PermissionScreen.kt`
   - **Root Cause:** Continue action was appended at the very bottom of a scrollable column of 6 permission cards. On standard viewport sizes (h < 800dp), the button was completely offscreen below the fold, giving the impression that the app was stuck.
   - **Resolution:** Lifted primary action into `AppScreen`'s persistent `bottomAction` bar with safe drawing and navigation insets.

4. **DEF-12: Missing AdMob APPLICATION_ID in AndroidManifest.xml (Crash Vulnerability)**
   - **Screen/Area:** `app/src/main/AndroidManifest.xml`
   - **Root Cause:** The Google Mobile Ads SDK is present in `build.gradle.kts` (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1`), and initialized in `MobileAdsBannerGateway` / `MobileAdsNativeAdGateway` / `AppOpenAdGateway`. Without `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="..." />`, the app will crash with an `IllegalStateException` on launch if ads are initialized.
   - **Resolution:** Add Google's test application ID (`ca-app-pub-3940256099942544~3347511713`) to `AndroidManifest.xml` with configuration override support.

5. **DEF-13: MainActivity Sets FLAG_KEEP_SCREEN_ON and Unrestricted Lockscreen Turn-On**
   - **Screen/Area:** `MainActivity.kt`
   - **Root Cause:** `MainActivity.kt` called `window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`, `setShowWhenLocked(true)`, and `setTurnScreenOn(true)` unconditionally. This drains battery drastically by preventing normal screen sleep while the user browses settings, and turns the screen on over the lockscreen.
   - **Resolution:** Remove `FLAG_KEEP_SCREEN_ON`, `setShowWhenLocked(true)`, and `setTurnScreenOn(true)` from `MainActivity`. Lockscreen display is exclusively managed by `IslandOverlayService` according to `LockScreenPrivacyPolicy`.

---

### P1 (Major Usability & Navigation Defects)

6. **DEF-03: Header Disappears When Scrolling Notch Alignment & Sub-Screens**
   - **Screen/Area:** `DisplaySettingsScreen.kt`, `PermissionScreen.kt`, and sub-screens
   - **Root Cause:** `AppTopBar` was placed directly inside the vertically scrollable content `Column(Modifier.verticalScroll())`. As the user scrolled to adjust fine-tuning sliders or view lower cards, the header (including back button, title, and screen identity) scrolled off-screen.
   - **Resolution:** Elevate `AppTopBar` into `AppScreen`'s dedicated `topBar` slot in `Scaffold`. Pinned app bar retains navigation discoverability, back navigation, and screen identity throughout scrolling.

7. **DEF-04: Extreme Tab Label Compression on Live Activities Screen**
   - **Screen/Area:** `LiveActivitiesScreen.kt`
   - **Root Cause:** 20 distinct activities were crammed into a single fixed `AppSegmented` control (`Row { weight(1f) }`). On a 360dp phone, each tab received ~18dp width, compressing text into unreadable 1-letter fragments.
   - **Resolution:** Introduce `AppScrollableTabs` with smooth horizontal scrolling, auto-centering on selection, adequate touch targets (min 44dp), and clear active pill indicators.

8. **DEF-05: Misleading Presentation of Simulated iOS Features**
   - **Screen/Area:** `LiveActivitiesScreen.kt`, `PlaygroundScreen.kt`
   - **Root Cause:** Simulated items like "AirDrop", "Satellite", and "Find My" were presented with identical visual weight as genuine Android integrations (Media, Calls, Battery), confusing users about actual device capabilities.
   - **Resolution:** Clearly label simulated demonstrations with an explicit "Simulation" or "Demo" badge and informational footnotes clarifying platform capabilities.

9. **DEF-06: Disconnected Media State Between Island, Compact, and Expanded Views**
   - **Screen/Area:** `MusicSettingsScreen.kt`, `PlaygroundScreen.kt`, and `IslandOverlayService.kt`
   - **Root Cause:** Independent state variables tracked playback progress and pause state, resulting in zeroed timers or conflicting play/pause glyphs.
   - **Resolution:** Centralize media playback presentation through `MediaPlaybackRegistry` and unified `IslandDemoState` models.

10. **DEF-14: Hardcoded Mock Prices in ProPaywallSheet & PremiumScreen without Live Google Play Query**
    - **Screen/Area:** `PremiumScreen.kt`, `ProPaywallSheet.kt`, `PlayBillingPremiumAccessGateway.kt`
    - **Root Cause:** Plans hardcoded static Indian Rupee values ("₹39", "₹59", "₹199", "₹499"). Product details were only queried asynchronously after tapping purchase, preventing global users from seeing localized pricing, currency codes, and trial eligibility upfront.
    - **Resolution:** Upgrade Play Billing integration to pre-fetch `ProductDetails` for configured subscription and in-app product IDs, exposing a unified `StateFlow<Map<String, FormattedPlanDetails>>` so UI displays genuine localized store prices and trial terms with graceful fallback.

11. **DEF-15: Fragmented Entitlement Checking and Missing Centralized EntitlementManager**
    - **Screen/Area:** Application-wide UI and data layers
    - **Root Cause:** Multiple screens directly read `preferences.isProActive` or `PremiumFeaturePolicy` independently. There is no unified `canUseFeature(featureId, entitlementState)` model or centralized `EntitlementManager` providing a single source of truth.
    - **Resolution:** Implement `EntitlementManager` with `canUseFeature(featureId)` contract, tracking lifetime purchases, active subscriptions, grace periods, and rewarded passes.

12. **DEF-16: Aggressive Interstitial Ad Frequency on Navigation Clicks**
    - **Screen/Area:** `MainContainerScreen.kt`, `InterstitialAdGateway.kt`
    - **Root Cause:** `InterstitialAdFrequencyPolicy` showed full-screen interstitial ads every 3 button clicks (`clickCount % 3 == 0`). This aggressive pacing severely degraded user experience during normal configuration.
    - **Resolution:** Calibrate interstitial policy to eliminate click-based interruptions during normal configuration. Restrict full-screen ads to intentional tab transitions with a respectful cooldown and a minimum screen counter threshold (>= 8 transitions), and never during onboarding, setup, or calibration.

---

### P2 (Layout Inefficiencies & Visual Inconsistencies)

13. **DEF-07: Oversized White Containers with Excessive Blank Area in Previews**
    - **Screen/Area:** `PlaygroundScreen.kt`, `DisplaySettingsScreen.kt`
    - **Root Cause:** Preview stages had arbitrarily high fixed min-heights (e.g. 240dp - 280dp) even for compact 36.67dp pills, resulting in large empty white voids that pushed essential controls below the fold.
    - **Resolution:** Content-aware stage heights: compact states occupy a snug 116dp container; expanded cards scale proportionally to their content height (156dp - 238dp).

14. **DEF-08: Inconsistent Spacing Across Cards and Headers**
    - **Screen/Area:** Application-wide
    - **Root Cause:** Hardcoded padding values (`10.dp`, `15.dp`, `18.dp`, `25.dp`) scattered across screen implementations rather than referencing semantic tokens.
    - **Resolution:** Standardize all padding and margins to `AppTheme.spacing` (`xs=4dp`, `sm=8dp`, `md=12dp`, `lg=16dp`, `xl=20dp`, `xxl=24dp`, `xxxl=32dp`).

15. **DEF-09: Redundant Status Badges Cluttering Dashboard**
    - **Screen/Area:** `PlaygroundScreen.kt`
    - **Root Cause:** Multiple "Ready", "Island is active", and "Active" pills duplicated each other within a 200dp vertical span.
    - **Resolution:** Consolidate status into a single cohesive service status card at the bottom of the dashboard.

16. **DEF-17: Paywall Missing Free vs. Pro Feature Comparison Matrix**
    - **Screen/Area:** `PremiumScreen.kt`, `ProPaywallSheet.kt`
    - **Root Cause:** Paywall only listed Pro benefits with checkmarks without clearly articulating what users retain in Free vs. what unlocks with Pro, making the value proposition opaque.
    - **Resolution:** Add a structured Free vs. Pro comparison card clearly showing core Dynamic Island, notifications, and basic music included in Free, and advanced visualizers, custom notch offsets, unlimited quick controls, and ad removal in Pro.

17. **DEF-18: AdMob Native Ad MediaView Height Below Google 120dp Video Minimum**
    - **Screen/Area:** `NativeAdCard.kt` (`MediaView`)
    - **Root Cause:** MediaView height was hardcoded to `88dp`, triggering Google AdMob Native Ad Validator overlay violation ("MediaView is too small for video: must be at least 120x120dp").
    - **Resolution:** Increased MediaView height to `140dp` and bound `ad.mediaContent` directly without redundant nullable cast. Fully compliant with Google Play AdMob policy.

---

### P3 (Cosmetic Refinements)

17. **DEF-10: Inconsistent Border Opacities on Card Edges in Light/Dark Theme**
    - **Screen/Area:** `AppKit.kt` cards and dialogs
    - **Resolution:** Standardized border stroke to `0.5.dp` with semantic `AppTheme.colors.border` and `borderSubtle`.

18. **DEF-11: Slider Track Contrast in Dark Theme**
    - **Screen/Area:** `AppControls.kt` (`AppSlider`)
    - **Resolution:** Enhanced slider track background contrast against elevated card surfaces.

---

## 4. User Journey Validation Matrix

### Journey 1 — Fresh Installation & Onboarding
1. **Launch:** App opens on `StartupSplashScreen`, transitions to `OnboardingScreen` / `PermissionScreen`.
2. **First-Screen Understanding:** First-time user sees clear value proposition without distraction.
3. **Setup Visibility:** `PermissionScreen` features persistent "Continue" button pinned above navigation insets.
4. **Granting Access:** Tapping permission rows opens system settings; returning refreshes readiness cards.
5. **Arrival on Home:** User lands on `PlaygroundScreen` where `IslandActivationCard` immediately displays "Island is on" or "Turn on Island" in the initial viewport without scrolling.
6. **Result: SUCCESS — Zero hunting for activation.**

### Journey 2 — Returning User, Island Off
1. **Launch:** App opens to Tab 0 (`PlaygroundScreen`).
2. **Immediate Status:** `IslandActivationCard` displays "Island is off" with "Turn Island On" switch/button in the first viewport.
3. **One-Tap Activation:** User flips switch or taps "Turn on Island"; overlay begins running immediately.
4. **Result: SUCCESS — Intent satisfied in under 3 seconds.**

### Journey 3 — Island Running
1. **Launch:** User opens application.
2. **Immediate Status:** `IslandActivationCard` displays "Island is on" with green "Running" status pill and "Live Overlay Enabled" switch.
3. **Interaction:** User explores live previews, selects experiences, or toggles off the island if desired.
4. **Result: SUCCESS — State clarity verified.**

### Journey 4 — Service Paused by Android OS
1. **System Event:** OEM battery killer terminates accessibility service in background.
2. **App Launch:** `IslandActivationPolicy` detects `hasAccessibilityGranted == true` but `hasAccessibilityConnected == false`.
3. **Accurate Diagnosis:** Card shows "Island paused" (Red "Paused" pill) and "Android has paused the required service in the background."
4. **Recovery Action:** Primary CTA is "Reactivate Island" which opens Accessibility Settings.
5. **Return:** Resuming the app re-evaluates service health; card updates to "Island is on".
6. **Result: SUCCESS — Honest error recovery path, no false "Ready" state.**

### Journey 5 — Notch Alignment & Calibration
1. **Navigate:** Home -> Customize -> "Notch alignment".
2. **Header Stability:** Pinned `AppTopBar` retains back navigation and title while user scrolls through calibration sliders.
3. **Calibration:** Sliders adjust X, Y, and Scale smoothly; reset button returns to hardware cutout baseline.
4. **Result: SUCCESS — Header remains stable, settings persist.**

### Journey 6 — Live Activities Simulation
1. **Navigate:** Bottom Dock -> Tab 2 ("Live").
2. **Category Selection:** `AppScrollableTabs` smoothly scrolls through all 20 activity types without text compression.
3. **Honest Demarcation:** Non-native iOS activities (AirDrop, Satellite, Find My) display prominent "Simulation" badge.
4. **Result: SUCCESS — Full tab label readability and simulation transparency.**

### Journey 7 — Music & Media Playback
1. **Event:** Audio playback starts on device (Spotify / YouTube Music).
2. **Island Presentation:** Island enters compact pill with album art and live equalizer waveform.
3. **Expansion:** Tapping expands the island to media controls with scrubber, track title, and transport actions.
4. **Dismissal:** Pausing and stopping playback collapses island back to idle cutout.
5. **Result: SUCCESS — Synchronized playback state, zero stuck windows.**

### Journey 8 — System Interruption & Device Rotation
1. **Events:** Screen lock (`ACTION_SCREEN_OFF`), screen unlock (`ACTION_USER_PRESENT`), orientation change.
2. **Behavior:** Island pauses rendering on lock screen (unless lock screen privacy setting allows), resumes on unlock without losing context.
3. **Result: SUCCESS — Resilient lifecycle handling.**

