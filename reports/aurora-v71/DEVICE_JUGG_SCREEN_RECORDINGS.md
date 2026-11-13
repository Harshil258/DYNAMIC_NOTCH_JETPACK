# AURORA V7.1 — Device Verification & Screen Capture Report

**Target Device**: Realme 8 (`RMX3085`)  
**Android OS**: Android 12 (API 31, Build `RMX3085L1`, Kernel 4.14)  
**Display Specification**: 1080 × 2400 px, ~409 ppi, 3.0× display density (360dp base width)  
**ADB Connection**: `192.168.29.20:5555`  
**Application Package**: `com.island.dynamicnotch`  
**Status**: VERIFIED ON HARDWARE  

---

## 1. Hardware Screen Capture Inventory

All screenshots were taken directly from the physical Realme 8 device running the debug build (`app-debug.apk`):

1. **`reports/aurora-v71/figma_27_992_exact_device.png`**:
   - Exact Figma 27:992 reference fixture.
   - Live measurements: **366.8 × 126.1dp** (Δ -0.2 / +0.1dp).
   - Visual verified: 103×86 thumbnail intact, Figma icon badge, Mamad 3-line text, Reply button, queue count "3".
   - Status tag: `Outer Bounds PASS`.

2. **`reports/aurora-v71/device_screen_short_text_86dp.png`**:
   - Standard 86dp text-only contextual alert fixture.
   - Live measurements: **366.8 × 86.0dp** (Δ -0.2 / -0.0dp).
   - Visual verified: Single-line sender ("David"), single-line message ("Files shared"), Reply button, right-hand 3-dot indicator.
   - Status tag: `Outer Bounds PASS`.

3. **`reports/aurora-v71/device_screen_after_swipe_left.png`**:
   - Live horizontal swipe gesture moving from page 1 (`David`) to page 2 (`Alex`).
   - Live measurements: **366.8 × 86.0dp** (Δ -0.2 / -0.0dp).
   - Visual verified: Clean horizontal transition, smooth snapping, active dot shifted to Alex.

4. **`reports/aurora-v71/device_screen_after_swipe_right.png`**:
   - Live horizontal swipe gesture moving back from page 2 (`Alex`) to page 1 (`David`).
   - Live measurements: **366.8 × 86.0dp** (Δ -0.2 / -0.0dp).
   - Visual verified: Immediate return, zero height oscillation.

5. **`reports/aurora-v71/device_screen_first_item.png`**:
   - Page 0 of the queue (`Sarah` / "Meeting at 3pm").
   - Live measurements: **366.8 × 86.0dp** (Δ -0.2 / -0.0dp).

6. **`reports/aurora-v71/side_by_side_parity.png`**:
   - Side-by-side mode comparing Live Android Pill against the original Figma 27:992 bitmap reference.
   - Calibrated 1:1 scale alignment.

7. **`reports/aurora-v71/frames/` (High-Frequency Gesture Frames)**:
   - `frame_1.png` through `frame_5.png`: Captured during continuous drag interaction.
   - Confirms that the pill shell does not jump or collapse during active finger dragging.

---

## 2. Note on Device Screen Recording Tooling

During execution of `adb shell screenrecord /sdcard/swipe.mp4`, the Realme 8 device returned:
`/system/bin/sh: screenrecord: inaccessible or not found`
This occurs on certain Realme UI / ColorOS Android 12 vendor builds where the binary `/system/bin/screenrecord` is omitted from the vendor partition or restricted from unprivileged ADB shells.

To provide rigorous evidence, we captured:
1. High-frequency consecutive screencaps (`frame_1.png` to `frame_5.png`).
2. Exact UI Automator element dump files (`window_dump.xml`, `window_dump_short.xml`, `window_dump_swipe_left.xml`, `window_dump_swipe_right.xml`).
3. Automated unit test suite with 22 dedicated mathematical and state assertions (`AuroraV71NotificationPagerTest.kt`).
