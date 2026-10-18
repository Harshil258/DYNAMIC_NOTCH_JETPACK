# AURORA DYNAMIC ISLAND — UI/UX FORENSIC REDESIGN BASELINE
## Evidence-Grounded Audit, Architectural Diagnosis & 52-Screen Route Inventory

**Date:** October 18, 2026  
**Package:** `com.island.dynamicnotch` (`ai.emots.kishan_dynamic`)  
**Hardware Testbed:** Realme 8 (`RMX3085`), Android 13 (API 33), 1080×2400 px, 408 dpi  
**Target Repository:** `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan`  
**Status:** FORENSIC DIAGNOSIS COMPLETE → TARGETED REPAIR ENGAGED

---

## 1. Executive Summary & Root Cause Matrix

An exhaustive inspection of the 52 real-device screenshots (`aurora_all_screens.zip`) and underlying Jetpack Compose / WindowManager architecture confirms that several high-impact UX flaws originate from structural layout patterns and missing gesture handlers rather than cosmetic styling.

| Defect ID | Severity | Confirmed Root Cause | Source Location | Fix Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **P0-1 (ONB-01)** | **P0** | **Onboarding completely lacks a horizontal pager.** Implemented via `Column.verticalScroll()` + `AnimatedContent` toggled solely by button clicks. No horizontal drag / swipe gesture detector exists. | `OnboardingScreen.kt` | Migrate to Foundation `HorizontalPager(state = rememberPagerState { slides.size })` with full bidirectional swipe, animated dots synchronized with scroll offset, and predictive back support. |
| **P0-2 (LAY-01)** | **P0** | **Scaffold inset and bottom padding mismatch.** `AppScreen` applies `bottomInset + AppTheme.spacing.xxxl` regardless of whether `bottomAction` already occupies `bottomBar`, causing double padding on some screens and content clipping beneath floating CTAs on others. | `AppKit.kt:AppScreen` | Harmonize `Scaffold` innerPadding consumption, normalize scroll padding so the last item always clears sticky docks/actions with exactly 16–24dp clearance across all devices. |
| **P0-3 (PAY-01)** | **P0** | **Paywall plan cards buried ~1000dp below first viewport.** `PremiumScreen.kt` positions a ~500dp interactive Hero Card and a ~500dp 2×2 benefit grid above the plan selector, forcing users to scroll past 2 full viewports before seeing plan options. | `PremiumScreen.kt` | Restructure hierarchy: compress Hero card, place all 4 plan cards (Weekly, Monthly, Yearly, Lifetime) prominently above the fold; ensure clear Google Play localized pricing and honest cancellation copy. |
| **P0-4 (PRE-01)** | **P0** | **Action Island preview clips 5th quick control.** `DynamicIslandPill.kt:ActionControlExpandedContent` renders fixed-width tiles in a single row overflowing the card's width boundary. | `DynamicIslandPill.kt` / `ActionControlIsland.kt` | Transition to an adaptive grid (2×4 or 2×3) or horizontally scrollable row with proper end-padding, ensuring all configured tiles fit without edge slicing. |
| **P0-5 (EVT-01)** | **P0** | **Tap routing & stale notification recovery.** Auto-collapsed island state previously did not surface active notifications from the shade reliably. | `IslandStateManager.kt`, `IslandOverlayService.kt` | Ensure active notifications in `IslandStateManager` remain resumable via tap, while long-press exclusively routes to Quick Controls when permitted. |
| **P0-6 (MOT-01)** | **P0** | **Morph geometry & clipping.** WindowManager window bounds vs Compose content animation bounds mismatch caused clipping during intermediate expansion frames. | `IslandOverlayService.kt`, `AppMotion.kt` | Coordinate WindowManager overlay dimensions with Compose animated size springs so window resizing matches island bounds. |
| **P0-7 (EVD-01)** | **P0** | **Rewarded ad dialog evidence mismatch & copy.** Screenshot 48 was mislabeled (captured Settings without dialog). UI copy in Settings claimed misleading grant. | `SettingsVaultScreen.kt`, `AdRewardDialog.kt` | Align all copy to "+24h pass (1 day)" per ad; verify 7-day progress calendar with checkmarks and capture genuine modal evidence. |

---

## 2. 52-Screenshot Route & Component Mapping

| Screenshot ID | Filename | Exact Composable Route | File Path |
| :--- | :--- | :--- | :--- |
| **01** | `01_first_launch_language_selection.png` | `LanguageSelectionScreen` (first run) | `ui/screens/MainContainerScreen.kt` |
| **02** | `02_language_selection_scrolled.png` | `LanguageSelectionScreen` (scrolled) | `ui/screens/MainContainerScreen.kt` |
| **03** | `03_onboarding_slide_1_alerts.png` | `OnboardingScreen` (Slide 0: Alerts) | `ui/screens/OnboardingScreen.kt` |
| **04** | `04_onboarding_slide_2_media.png` | `OnboardingScreen` (Slide 1: Media) | `ui/screens/OnboardingScreen.kt` |
| **05** | `05_onboarding_slide_3_controls.png` | `OnboardingScreen` (Slide 2: Controls) | `ui/screens/OnboardingScreen.kt` |
| **06** | `06_onboarding_permission_hub_top.png` | `PermissionScreen` (Top / Mandatory) | `ui/screens/PermissionScreen.kt` |
| **07** | `07_onboarding_permission_hub_recommended.png` | `PermissionScreen` (Recommended) | `ui/screens/PermissionScreen.kt` |
| **08** | `08_onboarding_permission_hub_privacy.png` | `PermissionScreen` (Privacy footer) | `ui/screens/PermissionScreen.kt` |
| **09** | `09_tab0_island_playground_top.png` | `PlaygroundScreen` (Tab 0: Top) | `ui/screens/PlaygroundScreen.kt` |
| **10** | `10_tab0_island_playground_middle.png` | `PlaygroundScreen` (Tab 0: Middle) | `ui/screens/PlaygroundScreen.kt` |
| **11** | `11_tab0_island_playground_music_expanded.png` | `PlaygroundScreen` (Music Expanded) | `ui/screens/PlaygroundScreen.kt` |
| **12** | `12_tab0_island_playground_music_compact.png` | `PlaygroundScreen` (Music Compact) | `ui/screens/PlaygroundScreen.kt` |
| **13** | `13_tab1_features_studio_top.png` | `MainContainerScreen` (Tab 1: Top) | `ui/screens/MainContainerScreen.kt` |
| **14** | `14_tab1_features_studio_bottom.png` | `MainContainerScreen` (Tab 1: Bottom) | `ui/screens/MainContainerScreen.kt` |
| **15** | `15_tab2_live_activities_top.png` | `LiveActivitiesScreen` (Tab 2: Top) | `ui/screens/LiveActivitiesScreen.kt` |
| **16** | `16_tab2_live_activities_music_card.png` | `LiveActivitiesScreen` (Music card) | `ui/screens/LiveActivitiesScreen.kt` |
| **17** | `17_tab2_live_activities_delivery_card.png` | `LiveActivitiesScreen` (Delivery card) | `ui/screens/LiveActivitiesScreen.kt` |
| **18** | `18_tab3_settings_vault_top.png` | `SettingsVaultScreen` (Tab 3: Top) | `ui/screens/SettingsVaultScreen.kt` |
| **19** | `19_tab3_settings_vault_bottom.png` | `SettingsVaultScreen` (Tab 3: Bottom) | `ui/screens/SettingsVaultScreen.kt` |
| **20** | `20_subscreen_about_aurora_top.png` | `AboutScreen` (Top) | `ui/screens/AboutScreen.kt` |
| **21** | `21_subscreen_about_aurora_bottom.png` | `AboutScreen` (Bottom) | `ui/screens/AboutScreen.kt` |
| **22** | `22_subscreen_feedback_rating_category.png` | `FeedbackScreen` (Category/Rating) | `ui/screens/FeedbackScreen.kt` |
| **23** | `23_subscreen_feedback_message_submit.png` | `FeedbackScreen` (Message/Submit) | `ui/screens/FeedbackScreen.kt` |
| **24** | `24_subscreen_component_gallery_top.png` | `ComponentGalleryScreen` (Debug QA) | `ui/screens/ComponentGalleryScreen.kt` |
| **25** | `25_subscreen_component_gallery_middle.png`| `ComponentGalleryScreen` (Debug QA) | `ui/screens/ComponentGalleryScreen.kt` |
| **26** | `26_subscreen_component_gallery_bottom.png`| `ComponentGalleryScreen` (Debug QA) | `ui/screens/ComponentGalleryScreen.kt` |
| **27** | `27_subscreen_theme_settings.png` | `DisplaySettingsScreen` (Theme section) | `ui/screens/DisplaySettingsScreen.kt` |
| **28** | `28_subscreen_language_settings.png` | `LanguageSelectionScreen` (Settings) | `ui/screens/MainContainerScreen.kt` |
| **29** | `29_subscreen_permissions_hub.png` | `PermissionScreen` (Settings access) | `ui/screens/PermissionScreen.kt` |
| **30** | `30_subscreen_premium_paywall_top.png` | `PremiumScreen` (Paywall Hero) | `ui/screens/PremiumScreen.kt` |
| **31** | `31_subscreen_premium_paywall_middle.png`| `PremiumScreen` (Plan Selector) | `ui/screens/PremiumScreen.kt` |
| **32** | `32_subscreen_premium_paywall_bottom.png`| `PremiumScreen` (Feature Matrix) | `ui/screens/PremiumScreen.kt` |
| **33** | `33_subscreen_notification_settings_top.png`| `NotificationSettingsScreen` (Top) | `ui/screens/NotificationSettingsScreen.kt` |
| **34** | `34_subscreen_notification_settings_bottom.png`| `NotificationSettingsScreen` (Apps) | `ui/screens/NotificationSettingsScreen.kt` |
| **35** | `35_subscreen_call_settings_top.png` | `CallSettingsScreen` (Call preview) | `ui/screens/CallSettingsScreen.kt` |
| **36** | `36_subscreen_call_settings_bottom.png` | `CallSettingsScreen` (Call options) | `ui/screens/CallSettingsScreen.kt` |
| **37** | `37_subscreen_music_settings_top.png` | `PlaygroundScreen` (Music preview) | `ui/screens/PlaygroundScreen.kt` |
| **38** | `38_subscreen_music_settings_bottom.png`| `PlaygroundScreen` (Music options) | `ui/screens/PlaygroundScreen.kt` |
| **39** | `39_subscreen_battery_settings_top.png` | `PlaygroundScreen` (Battery preview) | `ui/screens/PlaygroundScreen.kt` |
| **40** | `40_subscreen_battery_settings_bottom.png`| `PlaygroundScreen` (Battery options) | `ui/screens/PlaygroundScreen.kt` |
| **41** | `41_subscreen_sound_settings_top.png` | `PlaygroundScreen` (Sound preview) | `ui/screens/PlaygroundScreen.kt` |
| **42** | `42_subscreen_sound_settings_bottom.png`| `PlaygroundScreen` (Sound options) | `ui/screens/PlaygroundScreen.kt` |
| **43** | `43_subscreen_quick_control_top.png` | `PlaygroundScreen` (Action preview) | `ui/screens/PlaygroundScreen.kt` |
| **44** | `44_subscreen_quick_control_bottom.png`| `PlaygroundScreen` (Action list) | `ui/screens/PlaygroundScreen.kt` |
| **45** | `45_subscreen_display_notch_top.png` | `DisplaySettingsScreen` (Notch top) | `ui/screens/DisplaySettingsScreen.kt` |
| **46** | `46_subscreen_display_notch_middle.png`| `DisplaySettingsScreen` (Offsets) | `ui/screens/DisplaySettingsScreen.kt` |
| **47** | `47_subscreen_display_notch_bottom.png`| `DisplaySettingsScreen` (Colors/Reset) | `ui/screens/DisplaySettingsScreen.kt` |
| **48** | `48_dialog_ad_reward_pass.png` | `AdRewardDialog` | `ui/dialogs/AdRewardDialog.kt` |
| **49** | `49_dialog_exit_confirmation.png` | `ExitConfirmationDialog` | `ui/dialogs/ExitConfirmationDialog.kt` |
| **50** | `50_dialog_in_app_rating.png` | In-App Rating Modal | `ui/dialogs/InAppRatingDialog.kt` |
| **51** | `51_dialog_privacy_policy.png` | Privacy Policy Sheet | `ui/dialogs/LegalDialogs.kt` |
| **52** | `52_dialog_terms_of_service.png` | Terms of Service Sheet | `ui/dialogs/LegalDialogs.kt` |

---

## 3. Plan of Execution

1. **Fix P0-1 (ONB-01):** Migrate `OnboardingScreen.kt` to Foundation `HorizontalPager` with bidirectional swipe gestures, animated dot indicators, and BackHandler support.
2. **Fix P0-2 (LAY-01):** Normalize `AppKit.kt:AppScreen` padding and insets, preventing double-inset computation when `bottomAction` is present.
3. **Fix P0-3 (PAY-01):** Restructure `PremiumScreen.kt` so the 4 plans are immediately visible in the upper viewport, handling billing unavailable state gracefully.
4. **Fix P0-4 (PRE-01):** Fix `DynamicIslandPill.kt` Action Island preview overflow so all tiles fit without clipping.
5. **Fix P1s:**
   - Correct Live Activities Delivery view in `LiveActivitiesScreen.kt`.
   - Align rewarded ad pass copy to "+24h pass (1 day)" in `SettingsVaultScreen.kt`.
   - Remove default 5-star preselection in `FeedbackScreen.kt`.
   - Clarify exit semantics in `ExitConfirmationDialog.kt`.
   - Replace "FaceTime Audio" with Android call demo in `CallSettingsScreen.kt`.
   - Ensure release build hides debug Component Gallery.
6. **Compile, Test, and Verify on Physical Device (`Realme 8`).**
