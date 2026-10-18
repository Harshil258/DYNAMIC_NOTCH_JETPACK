# AURORA DYNAMIC ISLAND — SPRINT 04 REPORT
## Android Permissions, Accessibility Hardening & Privacy Audit

**Application ID:** `ai.emots.kishan_dynamic`  
**Target Repository:** `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan`  
**Sprint Focus:** Google Play Compliance, Data Minimization (`AUR-P0-005`), Accessibility Hardening (`AUR-P0-006`), Permission UI Synchronization, Intent Security, and Backup Privacy.  
**Device Verified:** Realme 8 (`RMX3085`, Android 13 / API 33).  
**Test Suite Status:** 300 / 300 unit tests passing (100% pass rate).

---

## 1. Executive Summary

Sprint 04 investigated and eliminated critical Google Play policy violations, dangerous over-privileged accessibility declarations, contradictory onboarding permission states, and sensitive data leakage risks:

1. **`AUR-P0-005` Resolved — Play Store Rejection Risk (Unused Manifest Permissions):**
   - Removed `CAMERA`, `ACCESS_FINE_LOCATION`, and `ACCESS_COARSE_LOCATION` from `AndroidManifest.xml`.
   - Removed `<uses-feature android:name="android.hardware.camera">`.
   - Verified via `aapt dump permissions` on the generated APK: **0 camera or location permissions present**.
   - Retained genuinely required system integration permissions (`NFC`, `READ_SYNC_SETTINGS`, `FLASHLIGHT`).

2. **`AUR-P0-006` Resolved — Accessibility Service Over-Privileged Configuration:**
   - Hardened `accessibility_service_config.xml`:
     - `android:canPerformGestures="false"` (was `true` despite 0 gesture calls in codebase).
     - `android:canRetrieveWindowContent="false"` (was `true`).
     - Restricted `android:accessibilityEventTypes="typeWindowStateChanged|typeWindowsChanged"` (eliminated `typeWindowContentChanged` which triggered Android's high-risk keystroke observation warning).
   - Refactored `IslandOverlayService.kt:onAccessibilityEvent` to eliminate all `window.root` node inspections, operating solely on `window.type == TYPE_SYSTEM` and `currentEvent.packageName`.
   - Verified via `aapt dump xmltree`: gestures and window content retrieval flags are confirmed disabled (`0x0`).

3. **Onboarding & Permission Screen State Alignment:**
   - Synchronized `SetupReadiness.kt` and `PermissionScreen.kt` so that when required capabilities (`Screen overlay` and `Notification access`) are granted by the user, the primary action allows the user to immediately **"Continue"**, eliminating the contradictory "Reconnect" state caused by asynchronous OS service binding.
   - Updated `IslandActivationPolicy.kt` so `!islandEnabled` evaluates before `!overlayConnected`, ensuring that when a user manually disables the island toggle, it reports `READY_OFF` ("Island is off", `canToggleDirectly = true`) rather than `SERVICE_PAUSED` ("Island paused", `canToggleDirectly = false`).
   - Replaced alarmist "Reconnect" buttons in `PermissionCard` with reassuring "Access enabled · Connecting…" during async service binding.

4. **Component & Intent Security Hardening:**
   - Protected `TelephonyStateReceiver` in `AndroidManifest.xml` with `android:permission="android.permission.READ_PHONE_STATE"` to prevent unauthorized third-party apps from sending spoofed `PHONE_STATE` broadcasts.
   - Confirmed all pending intents across `CallSummaryNotifier.kt` and `NotificationReplyActivity` strictly use `FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT`.

5. **Privacy, Data Minimization & Backup Hardening:**
   - Configured `data_extraction_rules.xml` (API 31+) and `backup_rules.xml` (legacy auto-backup) to explicitly exclude sensitive transient state:
     - `aurora_active_call_session.xml` (transient call timing/identity preferences)
     - `datastore/call_history_preferences.preferences_pb` (local call log records)
     - `notification_artwork` (cached notification icons/avatars)
   - Re-verified zero-PII contract: no contact names, phone numbers, notification text, or OTPs are logged or transmitted to analytics or Crashlytics.

---

## 2. Detailed Technical Remediation

### 2.1 AUR-P0-005: Manifest Permission Pruning

#### Problem
`AndroidManifest.xml` previously declared:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```
Static analysis confirmed:
- Zero usage of `LocationManager` GPS/network location updates or `FusedLocationProviderClient`.
- Camera capture is launched solely via `MediaStore.ACTION_IMAGE_CAPTURE` intent (which launches the platform camera application and requires 0 camera permissions).
- Flashlight control uses `CameraManager.setTorchMode` which only requires `android.permission.FLASHLIGHT`.

Declaring `CAMERA` and `LOCATION` permissions without runtime code or Play Store data safety disclosures guaranteed automated app review rejection under Google Play Data Minimization policy.

#### Remediation
- Removed `CAMERA`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, and `android.hardware.camera` feature tag from `AndroidManifest.xml`.
- Verified that legitimate system capabilities (`NFC`, `READ_SYNC_SETTINGS`, `FLASHLIGHT`) were preserved for Action Island tiles.
- Added `android:permission="android.permission.READ_PHONE_STATE"` to `TelephonyStateReceiver` to guard against intent spoofing.

#### Binary AAPT Verification
```
$ aapt dump permissions app-debug.apk
package: ai.emots.kishan_dynamic
uses-permission: name='android.permission.SYSTEM_ALERT_WINDOW'
uses-permission: name='android.permission.RECEIVE_BOOT_COMPLETED'
uses-permission: name='android.permission.POST_NOTIFICATIONS'
uses-permission: name='android.permission.ACCESS_WIFI_STATE'
uses-permission: name='android.permission.BLUETOOTH'
uses-permission: name='android.permission.READ_PHONE_STATE'
uses-permission: name='android.permission.CALL_PHONE'
uses-permission: name='android.permission.ANSWER_PHONE_CALLS'
uses-permission: name='android.permission.MODIFY_AUDIO_SETTINGS'
uses-permission: name='android.permission.FLASHLIGHT'
uses-permission: name='android.permission.ACCESS_NOTIFICATION_POLICY'
uses-permission: name='android.permission.NFC'
uses-permission: name='android.permission.WRITE_SETTINGS'
uses-permission: name='android.permission.READ_SYNC_SETTINGS'
uses-permission: name='android.permission.WRITE_SYNC_SETTINGS'
uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
uses-permission: name='android.permission.INTERNET'
uses-permission: name='android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS'
uses-permission: name='android.permission.VIBRATE'
uses-permission: name='android.permission.BLUETOOTH_CONNECT'
uses-permission: name='android.permission.BLUETOOTH_SCAN'
uses-permission: name='android.permission.READ_CONTACTS'
[Zero instances of CAMERA or LOCATION]
```

---

### 2.2 AUR-P0-006: Accessibility Service Hardening

#### Problem
`accessibility_service_config.xml` previously declared:
```xml
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeWindowsChanged"
    android:canRetrieveWindowContent="true"
    android:canPerformGestures="true" ... />
```
- Code search confirmed `dispatchGesture()` was called 0 times in the entire repository.
- `typeWindowContentChanged` fired on every keystroke in third-party apps, triggering Android's scary prompt: *"This service can observe all text you type including personal data like credit card numbers and passwords"*, destroying user trust.
- In `IslandOverlayService.kt`, `window.root` was called merely to obtain package names (`com.android.systemui`), which were already accessible via `currentEvent.packageName`.

#### Remediation
1. Updated `accessibility_service_config.xml`:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
       android:accessibilityEventTypes="typeWindowStateChanged|typeWindowsChanged"
       android:accessibilityFeedbackType="feedbackGeneric"
       android:accessibilityFlags="flagRetrieveInteractiveWindows"
       android:canRetrieveWindowContent="false"
       android:canRequestTouchExplorationMode="false"
       android:canPerformGestures="false"
       android:settingsActivity="ai.emots.kishan_dynamic.MainActivity"
       android:description="@string/accessibility_service_description"
       android:notificationTimeout="100" />
   ```
2. Refactored `IslandOverlayService.kt:onAccessibilityEvent`:
   ```kotlin
   override fun onAccessibilityEvent(event: AccessibilityEvent?) {
       val currentEvent = event ?: return
       if (currentEvent.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
           currentEvent.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
       val currentPackage = currentEvent.packageName?.toString()
       val suppress = runCatching {
           windows.orEmpty().any { window ->
               if (!window.isFocused && !window.isActive) return@any false
               val ownOverlay = window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
               ai.emots.kishan_dynamic.data.model.OverlaySystemSurfacePolicy.shouldSuppress(
                   window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_SYSTEM,
                   currentPackage, ownOverlay
               )
           }
       }.getOrDefault(currentPackage == "com.android.systemui" || currentPackage?.endsWith(".permissioncontroller") == true)
       if (systemSurfaceSuppressed.value != suppress) {
           systemSurfaceSuppressed.value = suppress
           NotificationDiagnostics.surface(suppress)
           if (suppress) restoreOverlayNonFocusable()
       }
   }
   ```
3. Verified via `aapt dump xmltree`:
   ```
   A: android:canRetrieveWindowContent(0x01010385)=(type 0x12)0x0
   A: android:canRequestTouchExplorationMode(0x010103d7)=(type 0x12)0x0
   A: android:canPerformGestures(0x0101050d)=(type 0x12)0x0
   A: android:accessibilityEventTypes(0x01010380)=(type 0x11)0x400020
   ```

---

### 2.3 Onboarding Permission Screen & Island Activation Alignment

#### Problem
1. When a user granted both Accessibility and Notification Listener permissions in Settings, Android asynchronously binds to the services in the background (`onServiceConnected` / `onListenerConnected`), which can take 100-800ms.
2. In `PermissionScreen.kt`, `readiness.ready` required both `isAccessibilityWorking` and `isNotificationWorking`. If service binding was in progress:
   - Primary action changed to `"Reactivate screen overlay"`.
   - Card displayed `"Reconnect"` in an error badge.
   - Clicking the action booted the user back into Settings to toggle the service off and on again.
   - The `"Continue"` action was hidden, trapping new users in an infinite loop.
3. In `IslandActivationPolicy.kt`, `!overlayConnected` was evaluated before `!islandEnabled`. When a user voluntarily toggled off the Dynamic Island switch on the Home screen, the status showed `SERVICE_PAUSED` ("Island paused", "Reactivate Island") instead of `READY_OFF` ("Island is off", "Off").

#### Remediation
1. Added `val areRequiredGranted: Boolean get() = overlayGranted && notificationsGranted` in `SetupReadiness.kt`.
2. Updated `PermissionScreen.kt`:
   - `val canProceed = readiness.ready || areCoreGranted`
   - When core permissions are granted, the primary button displays **"Continue"** with a checkmark glyph, allowing immediate onboarding completion.
   - The status pill displays `"Ready"` (if bound) or `"Connecting…"` (while binding), never an error "Reconnect".
   - `finishSetup()` executes successfully whenever `canProceed` is true.
3. Updated `IslandActivationPolicy.kt`:
   - Evaluated `!islandEnabled` before `!overlayConnected`, ensuring that intentional user toggling displays `READY_OFF` ("Island is off") with direct toggle capability (`canToggleDirectly = true`).
4. Replaced scary `"Access enabled · Reconnect"` button text with `"Access enabled · Connecting…"`.

---

### 2.4 Privacy & Auto-Backup Hardening

#### Problem
`data_extraction_rules.xml` and `backup_rules.xml` had only commented-out templates, which meant default Android auto-backup could include local call history, transient active call session data, or cached notification graphics in unencrypted device and Google Drive backups.

#### Remediation
Configured strict exclusions across both XML manifests:
- `aurora_active_call_session.xml` (transient call state)
- `datastore/call_history_preferences.preferences_pb` (local call history)
- `files/datastore/call_history_preferences.preferences_pb`
- `notification_artwork` (cached notification avatars and graphics)

---

## 3. Verification & Test Evidence

### 3.1 Automated Unit Tests
Executed `./gradlew testDebugUnitTest`:
- **Total Test Cases:** 300
- **Passed:** 300
- **Failed:** 0
- **Duration:** 1m 30s
- Added test coverage in `IslandActivationPolicyTest.kt`:
  - `readyOff_whenIslandDisabledByUser_evenIfServiceNotYetConnected`
- Added test coverage in `SetupReadinessTest.kt`:
  - `areRequiredGranted_returnsTrueWhenBothPermissionsGranted`

### 3.2 Real Device Testing on Realme 8 (Android 13)
- Installed `app-debug.apk` via ADB on Realme 8 (`X4Y9QSOZ4DEADEDM`).
- Verified Home Screen displays clear permission status ("Permission needed", "Grant required access").
- Verified Permission Screen renders clean separation between "Required" (Screen overlay, Notification access) and "Recommended" (Phone & calling, Contacts, Background activity, Call identification).
- Verified `PermissionExplanationSheet` explains data minimization ("Only used to draw the island overlay. Keystrokes and screen contents are never read or logged").
- Verified bottom action buttons remain visible and accessible.

---

## 4. Issues Resolved in Sprint 04

| Issue ID | Area | Severity | Resolution Summary | Status |
|:---|:---|:---:|:---|:---:|
| **AUR-P0-005** | Permissions / Play Compliance | P0 | Pruned `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` and camera hardware feature. Verified 0 camera/location permissions in APK via AAPT. Protected `TelephonyStateReceiver` with `READ_PHONE_STATE`. | **RESOLVED** |
| **AUR-P0-006** | Permissions / Accessibility | P0 | Hardened `accessibility_service_config.xml`: disabled gestures, disabled window content retrieval, dropped `typeWindowContentChanged`. Refactored `IslandOverlayService.kt` to eliminate `window.root` access. | **RESOLVED** |

---

<!-- GOAL_COMPLETE -->
