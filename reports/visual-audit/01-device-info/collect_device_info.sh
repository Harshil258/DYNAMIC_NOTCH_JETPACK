#!/bin/bash
SERIAL="X4Y9QSOZ4DEADEDM"
OUT="reports/visual-audit/01-device-info/realme8-diagnostics.txt"
echo "=== REALME 8 HARDWARE & DISPLAY DIAGNOSTICS ===" > "$OUT"
echo "Date: $(date)" >> "$OUT"
echo "" >> "$OUT"

echo "--- BUILD & DEVICE INFO ---" >> "$OUT"
echo "Model: $(adb -s $SERIAL shell getprop ro.product.model)" >> "$OUT"
echo "Brand: $(adb -s $SERIAL shell getprop ro.product.brand)" >> "$OUT"
echo "Manufacturer: $(adb -s $SERIAL shell getprop ro.product.manufacturer)" >> "$OUT"
echo "Device: $(adb -s $SERIAL shell getprop ro.product.device)" >> "$OUT"
echo "Android Version: $(adb -s $SERIAL shell getprop ro.build.version.release)" >> "$OUT"
echo "SDK: $(adb -s $SERIAL shell getprop ro.build.version.sdk)" >> "$OUT"
echo "Build ID: $(adb -s $SERIAL shell getprop ro.build.display.id)" >> "$OUT"
echo "" >> "$OUT"

echo "--- WINDOW MANAGER METRICS ---" >> "$OUT"
echo "Physical Size: $(adb -s $SERIAL shell wm size)" >> "$OUT"
echo "Physical Density: $(adb -s $SERIAL shell wm density)" >> "$OUT"
echo "Font Scale: $(adb -s $SERIAL shell settings get system font_scale)" >> "$OUT"
echo "" >> "$OUT"

echo "--- DISPLAY CUTOUT & INSETS ---" >> "$OUT"
adb -s $SERIAL shell dumpsys display | grep -E "DisplayDeviceInfo|DisplayCutout|mDefaultViewport|uniqueId" >> "$OUT"
echo "" >> "$OUT"

echo "--- WINDOW MANAGER INSETS (STATUS BAR & NAV BAR) ---" >> "$OUT"
adb -s $SERIAL shell dumpsys window displays | grep -E "DisplayFrames|InsetsState|mDisplayCutout|statusBar|navigationBar" >> "$OUT"
echo "" >> "$OUT"

echo "--- ACCESSIBILITY SERVICES STATUS ---" >> "$OUT"
echo "enabled_accessibility_services: $(adb -s $SERIAL shell settings get secure enabled_accessibility_services)" >> "$OUT"
echo "accessibility_enabled: $(adb -s $SERIAL shell settings get secure accessibility_enabled)" >> "$OUT"
echo "" >> "$OUT"

echo "--- INSTALLED PACKAGES CHECK ---" >> "$OUT"
adb -s $SERIAL shell pm list packages | grep "com.island.dynamicnotch" >> "$OUT"

echo "Device info collection complete."
