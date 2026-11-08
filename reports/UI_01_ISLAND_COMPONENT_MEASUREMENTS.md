# UI_01 — Dynamic Island Component Measurements & Geometry Telemetry

**Device:** Realme 8 (`RMX3085`, Density: 3.0× / 480 dpi)  
**Examiner:** ChatGPT — Principal Product Architect  
**Branch:** `visual-qa/task-ui-01-inventory`  
**Date:** November 8, 2026  

---

## 1. Overview of Measured Dimensions

All measurements in this report are extracted directly from hardware execution of `VisualInventoryCaptureTest.kt` on the physical Realme 8. Bounding box coordinates and sizes are recorded in both physical screen pixels ($\text{px}$) and logical density-independent pixels ($\text{dp} = \text{px} / 3.0$).

---

## 2. Core Island Geometry Standards

### A. Compact Pills
* **Measured Hardware Width:** $173.0\text{ dp}$ to $212.0\text{ dp}$ ($519\text{ px}$ to $636\text{ px}$).
* **Measured Hardware Height:** $52.7\text{ dp}$ ($158\text{ px}$).
* **Apple Reference Height:** $36.67\text{ dp}$ ($110\text{ px}$).
* **Apple Reference Width:** $126.0\text{ dp}$ ($378\text{ px}$).
* **Measured Corner Radius:** $26.3\text{ dp}$ ($79\text{ px}$) — computed as $\frac{\text{height}}{2}$.
* **Apple Corner Radius:** $18.33\text{ dp}$ ($55\text{ px}$).
* **Internal Icon Size:** $20.0\text{ dp}$ ($60\text{ px}$).
* **Horizontal Inset:** $12.0\text{ dp}$ ($36\text{ px}$).

### B. Expanded Cards
* **Measured Hardware Width:** $354.0\text{ dp}$ ($1062\text{ px}$) — spans $98.3\%$ of the $360\text{ dp}$ screen width ($3\text{ dp}$ side margins).
* **Measured Hardware Height:**
  * Expanded Notification: $172.0\text{ dp}$ ($516\text{ px}$).
  * Expanded Music Player: $172.0\text{ dp}$ ($516\text{ px}$).
  * Expanded Telephony Call: $172.0\text{ dp}$ ($516\text{ px}$).
  * Timer Expanded: $172.0\text{ dp}$ ($516\text{ px}$).
  * Ongoing Call Banner: $75.3\text{ dp}$ ($226\text{ px}$).
  * System Alert Banners (Silent, AirPods, AirDrop): $75.3\text{ dp}$ ($226\text{ px}$).
  * Charging / Battery Expanded: $129.3\text{ dp}$ ($388\text{ px}$).
  * Live Activities (Delivery, Flight, Sports): $164.0\text{ dp}$ ($492\text{ px}$).
* **Corner Radius:** $43.0\text{ dp}$ ($129\text{ px}$).
* **Apple Expanded Corner Radius:** $39.0\text{ dp}$ ($117\text{ px}$) to $42.0\text{ dp}$.

### C. Layered Satellite Mini-Islands (Task 05)
* **Satellite Capsule Height:** $48.0\text{ dp}$ ($144\text{ px}$).
* **Satellite Width:** $322.0\text{ dp}$ ($966\text{ px}$) — $32\text{ dp}$ inset relative to parent expanded card.
* **Vertical Spacing Between Satellites:** $6.0\text{ dp}$ ($18\text{ px}$).
* **Corner Radius:** $24.0\text{ dp}$ ($72\text{ px}$) — computed as $\frac{\text{height}}{2}$.
* **Internal Icon Size:** $18.0\text{ dp}$ ($54\text{ px}$).

---

## 3. Detailed Component Geometry Table (All 60 States)

| State Name | Measured Width (px / dp) | Measured Height (px / dp) | Apple Target Height (dp) | Height Disparity | Corner Radius (dp) | Touch Target (dp) |
|---|---|---|---|---|---|---|
| `Idle` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `Minimal` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `MusicCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `MusicExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `CallCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `CallExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `CallSummaryExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `FaceTimeCallExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `SharedMediaCallExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `OngoingCallBanner` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `NotificationCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `NotificationStacked` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `NotificationWithMusicCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `NotificationExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `SilentModeCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `SilentModeExpanded` | 1062 px / 354.0 dp | 274 px / 91.3 dp | ~70.0 dp | +21.3 dp (+30.4%) | 43.0 dp | Full banner |
| `RingerModeExpanded` | 1062 px / 354.0 dp | 274 px / 91.3 dp | ~70.0 dp | +21.3 dp (+30.4%) | 43.0 dp | Full banner |
| `VibrateModeCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `NormalModeCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `RingerVolumeCompact` | 564 px / 188.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 188.0 |
| `MediaVolumeCompact` | 564 px / 188.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 188.0 |
| `BluetoothConnected` | 636 px / 212.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 212.0 |
| `BluetoothConnecting` | 636 px / 212.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 212.0 |
| `ActionControlExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `PremiumExpiryExpanded` | 1062 px / 354.0 dp | 438 px / 146.0 dp | ~140.0 dp | +6.0 dp (+4.3%) | 28.0 dp | Full card |
| `ChargingCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `ChargingExpanded` | 1062 px / 354.0 dp | 388 px / 129.3 dp | ~110.0 dp | +19.3 dp (+17.5%) | 43.0 dp | Full card |
| `LowBatteryCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `LowBatteryExpanded` | 1062 px / 354.0 dp | 388 px / 129.3 dp | ~110.0 dp | +19.3 dp (+17.5%) | 43.0 dp | Full card |
| `TimerCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `TimerExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `DeliveryCompact` | 528 px / 176.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 176.0 |
| `DeliveryExpanded` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `FlightCompact` | 528 px / 176.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 176.0 |
| `FlightExpanded` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `SportsCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `SportsExpanded` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `NavigationCompact` | 519 px / 173.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 173.0 |
| `VoiceMemoExpanded` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `ScreenRecordingExpanded` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `ShortcutExpanded` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `FocusModeExpanded` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `AirPodsConnected` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `SatelliteConnected` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `FindMyAlert` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `MovedToIPhone` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `VideoRemoteExpanded` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `AirDropActivity` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `TimerImage` | 552 px / 184.0 dp | 158 px / 52.7 dp | 36.67 dp | **+16.03 dp (+43.7%)** | 26.3 dp | 52.7 × 184.0 |
| `NotificationImage` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `CallAvatars` | 1062 px / 354.0 dp | 516 px / 172.0 dp | ~160.0 dp | +12.0 dp (+7.5%) | 43.0 dp | Full card |
| `TransportActivity` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `FlightTrackerExpanded` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `SubscriptionPricing` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `MovieCard` | 1062 px / 354.0 dp | 492 px / 164.0 dp | ~150.0 dp | +14.0 dp (+9.3%) | 43.0 dp | Full card |
| `ColorOptions` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `AirplaneAlert` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `ScreenMirroringAlert` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `MobileDataAlert` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |
| `TransitRouteAlert` | 1062 px / 354.0 dp | 226 px / 75.3 dp | ~68.0 dp | +7.3 dp (+10.7%) | 37.7 dp | Full banner |

---

## 4. Typography Scale & Density Inside the Island

* **Notification App Name / Source:** `11sp` to `12sp` (`33px` to `36px`). Rendered in `AppTheme.colors.textTertiary`.
* **Title / Sender:** `14sp` (`42px`) SemiBold. On 2 lines, causes text truncation if width is restricted.
* **Notification Body:** `13sp` (`39px`) Regular with line-height `18sp`. On 3 lines, body requires `54sp` ($54\text{ dp}$), consuming $31.4\%$ of the total expanded card height.
* **Action Button Text:** `14sp` (`42px`) Medium. Action button height is $44\text{ dp}$ to $48\text{ dp}$.
