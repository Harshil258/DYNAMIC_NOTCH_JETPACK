# Google Analytics 4 (GA4) & Firebase Analytics Event Catalog

## Overview
This document specifies the centralized analytics tracking schema for the **Android Dynamic Island** application (`ai.emots.kishan_dynamic`). All events are routed through `AnalyticsTracker` (`FirebaseAnalyticsTracker` in production, `FakeAnalyticsTracker` in automated test suites).

---

## 1. Screen View Architecture & Deduplication

Screen views are tracked using the canonical GA4 `screen_view` event. To prevent duplicate events caused by Jetpack Compose recomposition or redundant state emissions, all screens are mediated through `ScreenDeduplicator`.

| Screen Name | Trigger | Screen Class | Parameters |
| :--- | :--- | :--- | :--- |
| `language_selection` | Initial language picker visible | `MainActivity` | `screen_name`, `screen_class` |
| `onboarding` | 3-step feature onboarding carousel visible | `MainActivity` | `screen_name`, `screen_class` |
| `permission_hub` | Accessibility & Notification setup visible | `MainActivity` | `screen_name`, `screen_class` |
| `action_island_editor` | Dedicated quick action & app shortcut editor | `MainActivity` | `screen_name`, `screen_class` |
| `component_gallery` | Design system component catalog | `MainActivity` | `screen_name`, `screen_class` |
| `main_tab_island` | Root Tab 0: Real Island Activation & Live Stage | `MainActivity` | `screen_name`, `screen_class` |
| `main_tab_features` | Root Tab 1: Action Island shortcuts & quick controls | `MainActivity` | `screen_name`, `screen_class` |
| `main_tab_live` | Root Tab 2: Live Activities & simulated states | `MainActivity` | `screen_name`, `screen_class` |
| `main_tab_settings` | Root Tab 3: Settings Vault & Preferences | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_premium` | Pro paywall & subscription purchase sheet | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_display_settings` | Notch alignment, size & geometry calibration | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_notification_settings`| Notification filters & privacy rules | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_music_settings` | Music player & live visualizer controls | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_call_settings` | Incoming call & telecom display controls | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_battery_settings` | Battery saver whitelist & auto-start guide | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_sound_settings` | Ringer & volume HUD settings | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_theme_settings` | Island themes & color palettes | `MainActivity` | `screen_name`, `screen_class` |
| `subscreen_about` | App version, privacy policy & terms | `MainActivity` | `screen_name`, `screen_class` |

---

## 2. Onboarding & Setup Funnel Events

| Event Name | Type | Trigger | Parameters | Example Values |
| :--- | :--- | :--- | :--- | :--- |
| `onboarding_started` | Custom | Onboarding screen initial presentation | None | — |
| `onboarding_step_viewed` | Custom | User swipes/views an onboarding slide | `step_index` (Int)<br>`step_name` (String) | `step_index=0`, `step_name="Alerts"` |
| `onboarding_step_completed`| Custom | User taps "Continue" on an onboarding step | `step_index` (Int)<br>`step_name` (String) | `step_index=1`, `step_name="Media"` |
| `onboarding_completed` | Custom | User reaches end or taps "Skip" | `skipped` (Boolean)<br>`step_index` (Int) | `skipped=false`, `step_index=2` |
| `permission_flow_started` | Custom | Setup/Permission screen presented | None | — |
| `permission_result` | Custom | Permission requested or permission state checked | `permission_type` (String)<br>`action` (String)<br>`granted` (Boolean) | `permission_type="accessibility"`, `action="requested"` |
| `island_activation_started` | Custom | User clicks finish setup CTA | None | — |
| `island_activated` | Custom | Accessibility overlay service establishes runtime connection | None | — |
| `island_activation_failed` | Custom | Service connection dropped or missing permission | `reason` (String) | `reason="service_unbind"` |

---

## 3. Dynamic Island Core Interaction Events (Zero PII)

> [!IMPORTANT]
> In strict compliance with product privacy standards, no notification text, message bodies, sender identities, contact names, phone numbers, OTPs, or browsing data are ever logged.

| Event Name | Trigger | Parameters | Example Values |
| :--- | :--- | :--- | :--- |
| `island_state_toggled` | User toggles island ON/OFF from Home card | `enabled` (Boolean) | `enabled=true` |
| `island_expand` | User taps compact island overlay to expand | `source` (String) | `source="overlay_tap"` |
| `island_collapse` | Expanded island collapses back to compact | None | — |
| `island_swipe_dismiss` | User swipes up on the island to dismiss current alert | None | — |
| `media_control_used` | User taps playback control on expanded media island | `action` (String) | `action="musicplaypause"`, `action="musicnext"` |
| `quick_control_tapped` | User taps quick action system tile in expanded island | `tile` (String) | `tile="flashlight"`, `tile="screenshot"` |
| `preview_started` | User previews an island demo state on the Live stage | `item` (String) | `item="AirDrop"` |

---

## 4. Monetization & Subscription Funnel Events

| Event Name | Type | Trigger | Parameters |
| :--- | :--- | :--- | :--- |
| `paywall_viewed` | Custom | Pro paywall sheet/screen presented to user | `source` (String, e.g. `"premium_screen"`) |
| `plan_selected` | Custom | User selects a plan option from the paywall | `plan_id` (String)<br>`value` (Double)<br>`currency` (String) |
| `begin_checkout` | Standard GA4 | User clicks "Continue with [Plan]" button | `item_id` (String)<br>`item_name` (String)<br>`plan_id` (String)<br>`value` (Double)<br>`currency` (String) |
| `subscription_checkout_started` | Custom | Identical to begin_checkout for custom funnels | `plan_id` (String)<br>`value` (Double)<br>`currency` (String) |
| `purchase` | Standard GA4 | Confirmed, verified, and settled Google Play purchase | `transaction_id` (String)<br>`plan_id` (String)<br>`item_id` (String)<br>`value` (Double)<br>`currency` (String) |
| `entitlement_activated` | Custom | User entitlement upgraded to Pro | `source` (String, e.g. `"play_billing"`)<br>`is_pro` (Boolean: `true`)<br>`plan_id` (String) |
| `purchase_pending` | Custom | Purchase pending external user completion | `plan_id` (String) |
| `purchase_canceled` | Custom | User dismissed the Google Play purchase bottom sheet | `plan_id` (String) |
| `purchase_failed` | Custom | Google Play purchase encountered an error | `plan_id` (String)<br>`response_code` (Int)<br>`reason` (String) |
| `subscription_restored` | Custom | User restored active subscription from Google Play | `source` (String: `"google_play"`) |

---

## 5. AdMob Impression-Level Ad Revenue (ILAR)

Logged on every successful ad impression across all formats using the canonical GA4 `ad_impression` schema.

| Parameter | Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `value` | Double | Revenue in primary currency units (`valueMicros / 1_000_000.0`) | `0.025` |
| `currency` | String | ISO 4217 Currency Code from AdMob | `"USD"`, `"INR"` |
| `ad_platform` | String | Advertising platform identifier | `"admob"` |
| `ad_format` | String | Ad format (`"app_open"`, `"interstitial"`, `"rewarded"`, `"native"`, `"banner"`) | `"interstitial"` |
| `ad_unit_id` | String | Google AdMob Ad Unit ID | `"ca-app-pub-3940256099942544/1033173712"` |
| `placement` | String | Screen or user flow location | `"tab_switch"`, `"pro_pass"` |
| `precision_type` | String | AdMob revenue precision | `"ESTIMATED"`, `"PRECISE"`, `"PUBLISHER_PROVIDED"` |

---

## 6. Diagnostic & Reliability Events

| Event Name | Trigger | Parameters |
| :--- | :--- | :--- |
| `billing_connection_lost` | Google Play Billing service dropped connection | `failure_type` (String)<br>`retry_attempt` (Int)<br>`purchase_flow_active` (Boolean) |
| `billing_connection_restored`| Connection successfully re-established | `retry_attempt` (Int) |
| `billing_connection_failed` | Connection attempt returned non-OK response | `response_code` (Int)<br>`reason` (String)<br>`purchase_flow_active` (Boolean) |
| `billing_product_query_failed`| Query for SKU product details failed | `response_code` (Int)<br>`reason` (String) |
| `billing_purchase_failed` | Purchase listener reported error | `response_code` (Int)<br>`reason` (String)<br>`plan_id` (String) |
| `billing_purchase_recovered` | Recovered pending transaction during re-query | `plan_id` (String) |

---

## 7. User Properties

| Property Name | Values | Purpose |
| :--- | :--- | :--- |
| `is_pro` | `"true"` / `"false"` | Segment subscribers vs. ad-supported free users |
| `app_theme` | `"system"`, `"light"`, `"dark"` | Personalization preference tracking |
| `notch_preset` | `"center"`, `"left"`, `"right"` | Hardware calibration distribution |
| `app_language` | `"en"`, `"es"`, `"hi"`, etc. | Localization usage distribution |
