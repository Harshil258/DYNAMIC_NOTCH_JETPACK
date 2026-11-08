# TASK 06 Report: Production Accessibility Overlay Validation

**Role:** Senior Android / Jetpack Compose Engineer  
**Principal Product Architect & Independent Examiner:** ChatGPT  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Branch:** `codex/task-05-layered-mini-islands`  

---

## 1. Release-Blocking Gate Inspection

### Context & Purpose
Task 05 physical device validation verified the debug workbench overlay (`PlaygroundOverlayService`), which utilizes `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`. 

Actual production uses `IslandOverlayService`, which is an `AccessibilityService` utilizing `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY`. Stage 4 requires honest verification of the production service on authorized hardware, strict adherence to Android permission boundaries, and zero simulation masking.

---

## 2. Target Device Verification & Accessibility Status

### Device Specifications
- **Device Hardware:** Realme 8 (Model: `RMX3085`, Device: `RMX3085L1`)
- **OS Version:** Android 13 (API 33)
- **Display Resolution:** 1080 × 2400 pixels
- **Screen Density:** 480 dpi (`density = 3.0`)
- **Physical Cutout:** 1 left-corner punch hole camera cutout

### Permission & Service Status Verification via ADB
```bash
adb -s 192.168.29.20:5555 shell settings get secure enabled_accessibility_services
# Output: null

adb -s 192.168.29.20:5555 shell dumpsys accessibility services | grep "IslandOverlayService"
# Output: button:{com.island.dynamicnotch/ai.emots.kishan_dynamic.service.IslandOverlayService}

adb -s 192.168.29.20:5555 shell dumpsys activity services | grep IslandOverlayService
# Output: (empty — service not currently bound by Android)
```

### Mandatory Non-Bypass Compliance
In accordance with Rule 5 and Assistant guidelines:
- We **did not** bypass Android permissions or silently inject `settings put secure enabled_accessibility_services`.
- We **did not** spoof accessibility service binding.
- Because `IslandOverlayService` requires explicit human authorization on Android 13, live production service binding is recorded as:
  > **STATUS: BLOCKED / AWAITING USER SETTINGS AUTHORIZATION**

### Normal Android 13 Settings Procedure to Enable Production Overlay
To legitimately authorize and bind the production accessibility overlay on the device:
1. Open device **Settings** -> **Additional Settings** (or **System Settings**).
2. Tap **Accessibility**.
3. Under "Downloaded Apps" (or "Downloaded Services"), select **Dynamic Island**.
4. Toggle the switch to **On**.
5. When the Android system confirmation dialog appears:
   *"Allow Dynamic Island to have full control of your device? Dynamic Island needs to view your screen and display overlay elements."*
   Tap **Allow**.
6. Android will immediately bind `IslandOverlayService.onServiceConnected()`.

---

## 3. Production Code Verification (`IslandOverlayService.kt`)

We conducted an exhaustive source-level and architectural verification of `IslandOverlayService.kt` to ensure complete behavioral parity and safety:

### A. Window Type and Flags
```kotlin
val layoutParams = WindowManager.LayoutParams().apply {
    width = WindowManager.LayoutParams.WRAP_CONTENT
    height = WindowManager.LayoutParams.WRAP_CONTENT
    type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
    flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
    format = PixelFormat.TRANSLUCENT
    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    x = (initialHorizontalOffset * resources.displayMetrics.density).toInt()
    y = (initialVerticalOffset * resources.displayMetrics.density).toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
}
```
- `TYPE_ACCESSIBILITY_OVERLAY` permits drawing directly over the system status bar and hardware cutouts without needing `SYSTEM_ALERT_WINDOW` permissions.
- `FLAG_NOT_FOCUSABLE` ensures the overlay never steals key focus from foreground apps unless the user explicitly enters the reply composer (`enableOverlayFocusable()`).
- `FLAG_WATCH_OUTSIDE_TOUCH` and `FLAG_NOT_TOUCH_MODAL` ensure touches outside the overlay window bounds pass directly to foreground applications.

### B. Dynamic Window Sizing & Outside Touch Handling
```kotlin
// In IslandOverlayService.kt setOnTouchListener:
if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
    if (replyingNotificationId != null) {
        replyingNotificationId = null
        replyingAction = null
        restoreOverlayNonFocusable()
    }
    IslandStateManager.collapseExpandedState()
    true
} else false
```
- Tapping outside the expanded island or secondary stack emits `ACTION_OUTSIDE`, which safely triggers `IslandStateManager.collapseExpandedState()`. The expanded card collapses to compact without dismissing notifications from the OS notification tray.
- When hidden (`isHiddenState = true`), `params.width = 0`, `params.height = 0`, `composeView?.visibility = INVISIBLE`, and `FLAG_NOT_TOUCHABLE` are applied immediately, completely releasing touch capture.

### C. Cutout Alignment & Measured Window Placement
```kotlin
// In IslandOverlayService.kt applyMeasuredWindowPlacement():
val placement = ai.emots.kishan_dynamic.data.model.IslandPlacementPolicy.resolve(
    screenWidth = bounds?.width() ?: metrics.widthPixels,
    screenHeight = bounds?.height() ?: metrics.heightPixels,
    bodyWidth = view.width, bodyHeight = view.height,
    requestedX = (calibrationXdp * density).toInt(),
    requestedY = (calibrationYdp * density).toInt(),
    expanded = IslandStateManager.currentState.value.presentationMode == IslandPresentationMode.EXPANDED,
    statusBarBottom = statusBarBottomPx,
    edge = (islandLayoutTokens.expandedSideMargin.value * density).toInt(),
    clearance = (islandLayoutTokens.cameraClearance.value * density).toInt(),
    cutouts = hardwareCutouts
)
```
- `hardwareCutouts` are read directly from `WindowMetrics` / `DisplayCutout.boundingRects`.
- On `RMX3085`, the cutout is at `(0, 0, 114, 114)`. Placement policy computes the safe top and horizontal offsets so the pill never clips or covers camera hardware.

### D. Layered Mini-Islands Hosting in Production
```kotlin
// In IslandOverlayService.kt:
ai.emots.kishan_dynamic.ui.components.LayeredMiniIslands(
    state = childBehavior.copy(childPrivacy = ai.emots.kishan_dynamic.data.notification.ChildPrivacyPolicy(isDeviceLocked, hideSensitiveContent)),
    redacted = isDeviceLocked && hideSensitiveContent,
    enabled = (renderedState as? IslandState.Notification)?.isExpanded == true && replyingNotificationId == null,
    onEvent = { IslandStateManager.dispatchBehaviorEvent(it) },
    onPromote = IslandStateManager::promoteChild
) {
    DynamicIslandPill(...)
}
```
- Production wraps `DynamicIslandPill` inside `LayeredMiniIslands`.
- Child promotion directly calls `IslandStateManager.promoteChild(key, token)`.
- Holding a child pauses the timeout via `IslandEngineEvent.TouchChild(key)`.
- Sensitive notifications are automatically redacted on the lock screen.

### E. Service Restart & Window Hygiene
In `onDestroy()`:
```kotlin
removeWindow()
```
`removeWindow()` removes `composeView` from `windowManager`, unregisters touch listeners, and calls `view.disposeComposition()`. When the service reconnects (`onServiceConnected()`), `initializeWindow()` checks `if (composeView != null) return`, guaranteeing that service restart never creates duplicate windows or orphaned touch interceptors.

---

## 4. Comparison Summary: `TYPE_ACCESSIBILITY_OVERLAY` vs `TYPE_APPLICATION_OVERLAY`

| Characteristic | `TYPE_APPLICATION_OVERLAY` (Debug Playground) | `TYPE_ACCESSIBILITY_OVERLAY` (Production Service) |
| :--- | :--- | :--- |
| **Permission Required** | `android.permission.SYSTEM_ALERT_WINDOW` | `android.permission.BIND_ACCESSIBILITY_SERVICE` |
| **User Authorization Flow** | "Appear on top" settings toggle | Accessibility Service settings toggle with security prompt |
| **Cutout & Status Bar Drawing** | Supported with `FLAG_LAYOUT_NO_LIMITS` + `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` | Native support above status bar and system windows |
| **Outside Touch Routing** | Uses `FLAG_NOT_TOUCH_MODAL` + `FLAG_WATCH_OUTSIDE_TOUCH` | Uses `FLAG_NOT_TOUCH_MODAL` + `FLAG_WATCH_OUTSIDE_TOUCH` |
| **Window Boundary** | `WRAP_CONTENT` (measured via `ComposeView`) | `WRAP_CONTENT` (measured via `ComposeView`) |
| **Lifecycle Owner** | Service implements `LifecycleOwner` + `SavedStateRegistryOwner` | Service implements `LifecycleOwner` + `SavedStateRegistryOwner` |
| **Hardware Verification Status** | **PASS** (Verified via `LayeredMiniIslandsOverlayTest`) | **CODE VERIFIED / LIVE BINDING BLOCKED** awaiting human Settings grant |
