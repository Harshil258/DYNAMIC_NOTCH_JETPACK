# TASK UI-02B — Hardware Screenshot Catalog & Visual Evidence Ledger

**Developer:** Gemini — Senior Android UI/UX Engineer  
**Examiner:** ChatGPT — Principal Product Architect  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Base Branch:** `visual-redesign/task-ui-02-premium-system`  
**Active Review Branch:** `visual-redesign/task-ui-02b-premium-refinement`  
**Evidence Source:** Realme 8 Hardware Captures (`reports/visual-redesign/`)  
**Hardware Profile:** Realme 8 (`RMX3085`, Android 13, 1080×2400 px, 360×800 dp, density 3.0×, USB `X4Y9QSOZ4DEADEDM`)

---

## 1. Evidence Tier Classification Contract

To maintain total transparency and honesty during acceptance, every visual artifact is strictly classified into one of four tiers:

1. **`DEVICE_COMPOSE`**: Rendered inside the Compose test harness directly on physical Realme 8 hardware at native 1080×2400 resolution (density 3.0×). Captures full-screen bitmaps and pixel-accurate island crops.
2. **`DEBUG_PLAYGROUND`**: Interactive in-app lab (`DynamicIslandPlaygroundScreen.kt`) hosted on device with manual scenario toggling, debug overlays, and dimension inspectability.
3. **`NATIVE_APPLICATION_OVERLAY`**: WindowManager overlay floating above the application's activity window, managing touch windows and inset clearance.
4. **`REAL_PRODUCTION_ACCESSIBILITY_OVERLAY`**: Production background service (`DynamicIslandAccessibilityService.kt`) managing the top-level system window (`TYPE_ACCESSIBILITY_OVERLAY`) across arbitrary third-party apps, system launcher, and lockscreen.

---

## 2. Hardware Screenshot Catalog

### Folder 02: Idle & System Alert Capsules (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/02-idle-and-system/`*

| File Name (Crop) | State Measured | Dimensions (dp) | Key Visual Features |
| :--- | :--- | :---: | :--- |
| `LowBatteryExpanded-crop.png` | `LowBatteryExpanded` | **338.0 × 55.0** | Sleek single-row status capsule; 0dp giant button; red battery glyph + 20% |
| `ChargingExpanded-crop.png` | `ChargingExpanded` | **338.0 × 73.7** | Green charging bolt with battery level percentage |
| `SilentModeExpanded-crop.png` | `SilentModeExpanded` | **338.0 × 77.3** | Mute/unmute bell slash glyph with charcoal action pill |
| `RingerModeExpanded-crop.png` | `RingerModeExpanded` | **338.0 × 86.0** | Ringer on status pill |
| `Idle-crop.png` | `Idle` | **126.0 × 36.7** | Centered compact idle notch capsule |
| `Minimal-crop.png` | `Minimal` | **156.0 × 36.7** | Split presentation with 36.67dp circular companion |

### Folder 06: Expanded Notifications (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/06-expanded-notifications/`*

| File Name (Crop) | State Measured | Dimensions (dp) | Key Visual Features |
| :--- | :--- | :---: | :--- |
| `NotificationExpanded-crop.png` | `NotificationExpanded` | **338.0 × 106.3** | Content-driven card, header-docked pager chip (`1 of 3`), 32dp action chips |
| `NotificationImage-crop.png` | `NotificationImage` | **136.0 × 36.7** | Compact notification avatar preview |

### Folder 07: Media & Audio Players (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/07-media-and-audio/`*

| File Name (Crop) | State Measured | Dimensions (dp) | Key Visual Features |
| :--- | :--- | :---: | :--- |
| `MusicExpanded-crop.png` | `MusicExpanded` | **338.0 × 157.0** | Direct-on-OLED controls, 48dp squircle artwork, 38dp white centerpiece play |
| `MusicCompact-crop.png` | `MusicCompact` | **156.0 × 36.7** | Compact album art squircle + animated 6-bar equalizer |
| `VideoRemoteExpanded-crop.png`| `VideoRemoteExpanded`| **338.0 × 177.0** | Apple TV remote media controls |

### Folder 08: Telephony & Timers (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/08-telephony-and-timers/`*

| File Name (Crop) | State Measured | Dimensions (dp) | Key Visual Features |
| :--- | :--- | :---: | :--- |
| `CallExpanded-crop.png` | `CallExpanded` | **338.0 × 120.0** | Balanced 38dp circular glass buttons with 44dp hit targets |
| `FaceTimeCallExpanded-crop.png` | `FaceTimeCallExpanded`| **338.0 × 120.0** | 5-button FaceTime audio sheet (Speaker, Mute, Video, SharePlay, End) |
| `SharedMediaCallExpanded-crop.png`| `SharedMediaCallExpanded`| **338.0 × 120.0**| SharePlay call sheet with artwork |
| `TimerExpanded-crop.png` | `TimerExpanded` | **338.0 × 85.7** | Large orange countdown timer with pause/cancel controls |
| `TimerCompact-crop.png` | `TimerCompact` | **128.0 × 36.7** | Compact timer with live countdown |

### Folder 05: Layered Mini-Islands (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/05-layered-mini-islands/`*

| File Name (Crop) | Scenario | Key Visual Features |
| :--- | :--- | :--- |
| `01-single-child-crop.png` | 1 child satellite | 32dp single-row satellite pill docked tightly beneath main island |
| `02-two-children-crop.png` | 2 child satellites | 2 stacked satellite pills with 4dp vertical gap |
| `03-overflow-more-crop.png` | 2 children + overflow | 2 satellite pills + 24dp `+N more · Tap to view` affordance chip |
| `04-redacted-mini-islands-crop.png`| Locked device state | Redacted "Notification · Hidden" privacy compliance |

### Folder 10: Accessibility & Scaling Variations (`DEVICE_COMPOSE`)
*Location: `reports/visual-redesign/10-accessibility-and-scaling/`*

| File Name | Configuration | Validation Outcome |
| :--- | :--- | :--- |
| `scale-font-100-crop.png` | 100% Font Scale (360dp) | Baseline standard typography |
| `scale-font-150-crop.png` | 150% Font Scale (360dp) | Text scales smoothly without clipping |
| `scale-font-200-crop.png` | 200% Font Scale (360dp) | Maximum accessibility scaling; auto-wraps gracefully |
| `width-320dp-compact-crop.png`| 320dp Display Width | Narrow device layout; 0 horizontal collision |
| `width-393dp-expanded-crop.png`| 393dp Display Width | Large screen layout; capped expansion |
| `layout-rtl-hebrew-crop.png` | RTL Layout Direction | Complete mirroring of icons, text, and action buttons |
| `orientation-landscape-640dp-crop.png`| Landscape Orientation | Safe centering beneath notch |

### Physical Overlay Evidence (`REAL_PRODUCTION_ACCESSIBILITY_OVERLAY`)
*File: `reports/visual-redesign/overlay-physical.png`*
- Native screenshot captured via `adb exec-out screencap -p` from the Realme 8 hardware running the real Dynamic Island system overlay over the device wallpaper and launcher.
