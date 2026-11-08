# UI_01 — Visual Defect Observations & UX Design Audit

**Device:** Realme 8 (`RMX3085`, 360×800 dp, 1080×2400 px, Density: 3.0×)  
**Examiner:** ChatGPT — Principal Product Architect  
**Branch:** `visual-qa/task-ui-01-inventory`  
**Date:** November 8, 2026  

---

## 1. Executive Summary

Based on hardware screenshot evidence collected across all 60 Island states, 20 notification scenarios, and 21 application screens, this document categorizes and analyzes every visual defect contributing to the owner's observation that the UI appears **oversized, misaligned, and unpolished** on the Realme 8.

The defects are **NOT** caused by Android OS display scaling (verified `font_scale = 1.0`, `wm density = 480`). They are the direct result of:
1. **Uncalibrated Island Geometry:** Compact height of $52.7\text{ dp}$ is $43.7\%$ taller than Apple's $36.67\text{ dp}$ benchmark.
2. **Hardware Hole Mismatch:** A horizontally centered island on a phone whose physical punch-hole camera is at the far top-left.
3. **Bulky Application Design Tokens:** Fixed $56\text{ dp}$ buttons, $92\text{ dp}$ bottom dock insets, and $16\text{–}20\text{ dp}$ card paddings on a narrow $360\text{ dp}$ screen.

---

## 2. Comprehensive Defect Register

### Defect 01: Compact Island Pill Height Inflation (+43.7%)
* **Severity:** **CRITICAL (P0)**
* **Category:** Island Geometry & Sizing
* **Observed Dimensions:** $52.7\text{ dp}$ ($158\text{ px}$) measured on device across all compact states (`Idle`, `MusicCompact`, `NotificationCompact`, `CallCompact`).
* **Reference Benchmark:** $36.67\text{ dp}$ ($110\text{ px}$) — exact height of the Realme 8 status bar and Apple Dynamic Island.
* **Disparity:** $+16.03\text{ dp}$ ($+43.7\%$).
* **Visual Impact:** The compact island does not look like an integrated notch embellishment. Instead, it looks like a thick, bulbous black rectangle intruding into the status bar area, pushing down below the status bar baseline by $16\text{ dp}$.
* **Evidence Files:** [Idle-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/02-idle-and-system/Idle-crop.png), [NotificationCompact-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/03-compact-and-minimal/NotificationCompact-crop.png).

---

### Defect 02: Compact Island Minimum Width Inflation (+37.3%)
* **Severity:** **HIGH (P1)**
* **Category:** Island Geometry & Sizing
* **Observed Dimensions:** Minimum resting width is $173.0\text{ dp}$ ($519\text{ px}$), expanding to $212.0\text{ dp}$ ($636\text{ px}$) for volume/Bluetooth HUDs.
* **Reference Benchmark:** $126.0\text{ dp}$ ($378\text{ px}$) standard Apple compact pill width.
* **Disparity:** $+47.0\text{ dp}$ ($+37.3\%$).
* **Visual Impact:** Spanning nearly half of the entire screen width ($173\text{ dp}$ out of $360\text{ dp} = 48.1\%$), the pill dominates the top viewport even when presenting minimal information (e.g., an idle dot or single icon).
* **Evidence Files:** [NormalModeCompact-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/02-idle-and-system/NormalModeCompact-crop.png), [BluetoothConnected-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/02-idle-and-system/BluetoothConnected-crop.png).

---

### Defect 03: Hardware Camera Cutout Disalignment (Left Hole vs Center Pill)
* **Severity:** **CRITICAL (P0)**
* **Category:** Hardware Calibration & Physical Alignment
* **Observed Reality:**
  * Realme 8 punch-hole bounds: $X = -1.33\text{ dp}$ to $51.33\text{ dp}$ (left).
  * Dynamic Island bounds: $X = 93.5\text{ dp}$ to $266.5\text{ dp}$ (center).
* **Visual Impact:** The entire visual premise of the Dynamic Island is to disguise the physical camera hole. On the Realme 8, the physical camera hole sits completely exposed and visible on the left, while an artificial black pill floats awkwardly in the center of the display.
* **Evidence Files:** [realme8-diagnostics.txt](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/01-device-info/realme8-diagnostics.txt), [display-cutout-bounds.json](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/01-device-info/display-cutout-bounds.json).

---

### Defect 04: Oversized Primary Action Buttons (`AppButton.kt`)
* **Severity:** **HIGH (P1)**
* **Category:** Application Design Tokens
* **Observed Dimensions:** Height $56.0\text{ dp}$, horizontal padding $24.0\text{ dp}$, vertical padding $16.0\text{ dp}$, font size $16\text{ sp}$.
* **Reference Benchmark:** iOS Standard $44.0\text{–}48.0\text{ dp}$ button height.
* **Visual Impact:** On screens with stacked primary actions (e.g. Onboarding, PermissionHub, Paywall), two buttons consume $112\text{ dp}$ plus spacing, consuming nearly $20\%$ of the active vertical content height. The buttons feel disproportionately large and cartoonish.
* **Evidence Files:** [02-onboarding.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/02-onboarding.png), [03-permission-hub.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/03-permission-hub.png).

---

### Defect 05: Excessive Fixed Vertical Chrome Insets (92dp Bottom Dock)
* **Severity:** **HIGH (P1)**
* **Category:** Layout & Viewport Management
* **Observed Dimensions:** `BottomDockInset` is set to $92.0\text{ dp}$ ($64\text{ dp}$ dock $+ 28\text{ dp}$ blur gradient). TopBar is $56.0\text{ dp}$. Status bar is $36.67\text{ dp}$.
* **Total Reserved Chrome:** $184.67\text{ dp}$ out of $800.0\text{ dp}$ ($23.1\%$ locked).
* **Visual Impact:** Leaves only $615.33\text{ dp}$ for content. When combined with $16\text{ dp}$ card margins and large headers, cards are squeezed and require constant vertical scrolling.
* **Evidence Files:** [04-main-hub-tab0-island.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/04-main-hub-tab0-island.png), [07-main-hub-tab3-settings.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/07-main-hub-tab3-settings.png).

---

### Defect 06: Expanded Card Typography Truncation & Line-Height Bloat under Font Scaling
* **Severity:** **HIGH (P1)**
* **Category:** Accessibility & Typography
* **Observed Dimensions:**
  * Font Scale 1.0×: Island height is $160.0\text{ dp}$.
  * Font Scale 1.5×: Island height expands to $204.7\text{ dp}$ ($+27.9\%$).
  * Font Scale 2.0×: Island height balloons to $269.0\text{ dp}$ ($+68.1\%$).
* **Visual Impact:** Under $1.5\times$ and $2.0\times$ font scaling, the expanded notification card consumes more than a third of the screen. Action buttons are pushed against the bottom curve, and secondary text lines wrap and collide with the action row.
* **Evidence Files:** [scale-font-150-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/10-accessibility-and-scaling/scale-font-150-crop.png), [scale-font-200-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/10-accessibility-and-scaling/scale-font-200-crop.png).

---

### Defect 07: Heavy Card Padding on Narrow 360dp Viewport
* **Severity:** **MEDIUM (P2)**
* **Category:** Component Styling
* **Observed Dimensions:** Outer screen padding $16\text{ dp}$, card internal padding $16\text{–}20\text{ dp}$.
* **Net Usable Width Inside Cards:** $360 - 2(16) - 2(16) = 296\text{ dp}$.
* **Visual Impact:** Setting row titles and values wrap onto multiple lines unnecessarily because $64\text{ dp}$ of horizontal space is consumed entirely by padding.
* **Evidence Files:** [08-display-settings.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/08-display-settings.png), [11-notification-settings.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/09-app-pages/11-notification-settings.png).

---

### Defect 08: Layered Satellite Mini-Island Vertical Stacking Footprint
* **Severity:** **MEDIUM (P2)**
* **Category:** Task 05 Mini-Islands
* **Observed Dimensions:** Each satellite is $48.0\text{ dp}$ high with $6.0\text{ dp}$ vertical spacing.
* **Stack Height for 3 Satellites:** $3(48) + 2(6) = 156.0\text{ dp}$.
* **Total Height (Main Card + 3 Satellites):** $172\text{ dp} + 156\text{ dp} = 328.0\text{ dp}$ ($41.0\%$ of screen height).
* **Visual Impact:** On a $360\times 800\text{ dp}$ screen, having nearly half the screen occupied by notification capsules blocks underlying content excessively.
* **Evidence Files:** [03-three-mini-islands-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/05-layered-mini-islands/03-three-mini-islands-crop.png).

---

### Defect 09: Action Target Crowding on 320dp Width
* **Severity:** **MEDIUM (P2)**
* **Category:** Responsiveness & Boundary Clamping
* **Observed Dimensions:** At $320\text{ dp}$ simulated screen width, the island clamps to $298.0\text{ dp}$, leaving only $11.0\text{ dp}$ side margins.
* **Visual Impact:** Action buttons inside expanded cards touch adjacent controls with $<8\text{ dp}$ gap, violating touch target comfort guidelines.
* **Evidence Files:** [width-320dp-compact-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/10-accessibility-and-scaling/width-320dp-compact-crop.png).

---

### Defect 10: Heavy Shadow and Aura Glow Bleed on Dark Obsidian Background
* **Severity:** **MINOR (P3)**
* **Category:** Visual Craft & Contrast
* **Observed Dimensions:** Dynamic Island pill uses aura glow spot shadows with opacity up to $0.90$.
* **Visual Impact:** On pure obsidian black OLED screens, the deep black island pill blends into the background, while the colored aura glow spreads unevenly, creating a hazy border rather than a crisp Apple-like boundary.
* **Evidence Files:** [ChargingExpanded-crop.png](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/reports/visual-audit/02-idle-and-system/ChargingExpanded-crop.png).

---

## 3. Summary of Findings for Subsequent Redesign

1. **Compact Island Height:** Must be reduced from $52.7\text{ dp}$ to true $36.67\text{ dp}$ ($110\text{ px}$).
2. **Compact Island Width:** Must be slimmed down from $173\text{ dp}$ to $126\text{ dp}$ resting width.
3. **Primary Buttons:** Must be streamlined from $56\text{ dp}$ to $48\text{ dp}$ with $16\text{ dp}$ horizontal padding.
4. **Bottom Dock:** Inset reservation must be reduced from $92\text{ dp}$ to $72\text{ dp}$.
5. **Card Padding:** Reduce standard card padding from $16\text{–}20\text{ dp}$ to $12\text{ dp}$ on screens $<380\text{ dp}$ width to reclaim $16\text{ dp}$ of horizontal content width.
6. **Notch Camouflage Mode:** Introduce an optional left-aligned camera anchor mode for phones with left punch-holes like the Realme 8.
