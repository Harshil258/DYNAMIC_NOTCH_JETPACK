# V4.3 tests and performance

Latest source validation: `:app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` passed in 5m03s. JVM XML totals: **502 tests, 0 failures, 0 errors, 0 skipped**. See [build log](aurora-v43/final-build.log) and [JUnit XML](aurora-v43/test-results/).

Coverage includes all60 geometry mappings, responsive constraints, readable-start preview timing and stale acknowledgements, canonical queue/pager ownership, call restoration with original child deadlines, source/action-scoped memory-only draft expiry, unlocked sensitive-content redaction, and the debug controller's preview scheduler/reply-call restoration. The renderer and both device APKs compile.

Earlier physical Realme run: `captureEveryStateAndResponsiveBounds` passed **960 synthetic configurations** (60 states ×4 viewport widths ×4 font scales), generating120 PNGs. The second test failed because its empty-editor assertion also matched the placeholder text. It was corrected to assert EditableText semantics; the correction compiles but has not run on-device. See [interim device log](aurora-v43/interim-device-run.log).

The final device suite now has four tests: 1200 gallery configurations (five virtual widths including landscape), draft identity/restoration, absence of unsupported media/call targets, and playback status requiring source acknowledgement. **Final device suite NOT RUN**: the phone disconnected before APK installation; Wi-Fi reconnect returned connection refused and diagnosis found no attached device or installed AVD. Previous captures are explicitly INTERIM and predate the final layout/capability fixes.

Performance: **NOT RUN on latest source**. No final frame-time percentiles, jank rate, memory delta, or production overlay touch result is available. Existing benchmark code remains available, but no earlier metric is represented as this change's result. Synthetic Compose activity captures cannot establish TYPE_ACCESSIBILITY_OVERLAY frame pacing or touch dispatch.

Reproduce after reconnecting the selected phone:

```sh
adb devices -l
adb -s SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s SERIAL install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s SERIAL shell am instrument -w -r -e class ai.emots.kishan_dynamic.AuroraV43GalleryTest com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner
adb -s SERIAL pull /sdcard/Android/data/com.island.dynamicnotch/files/aurora-v43/ reports/
```

Use only the user's selected serial if more than one device is attached. Production accessibility service was unbound at the last connected inspection; platform acceptance additionally needs user-enabled service, actual source capabilities and controlled native touch evidence.
