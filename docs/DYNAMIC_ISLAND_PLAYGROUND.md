# Dynamic Island Playground — debug only

Open **Island Lab (Debug)** from the launcher after installing `app-debug.apk`, or:

```sh
adb -s DEVICE_SERIAL shell am start -n com.island.dynamicnotch/ai.emots.kishan_dynamic.playground.DynamicIslandPlaygroundActivity
```

The Activity, mock controller, overlay service, catalog and report asset live in
`app/src/debug/`. They do not exist in release. Normal app navigation is unchanged.

## Workflow

Use this Lab before any further production geometry or animation tuning:

1. Select a mock state and device preset.
2. Inspect measured bounds, camera clearance, padding zones and centerlines.
3. Try large text, RTL, long/missing copy and source actions.
4. Use Animation Lab for spring/easing, duration, speed, replay and sequences.
5. Run stress cases and automated validation.
6. Review captures, then deliberately promote accepted values to production defaults.

Lab values are in-memory inputs, not writes to real island preferences. Starting
the Lab does not finalize or apply a new production design.

## Sections

- **Live Preview:** scaled phone viewport, status bar, left/center/pill/no-camera
  presets, 320/360/393/430dp and landscape, density, font size, RTL and guides.
- **States:** 19 mock cases covering hidden/idle/notification variants, media,
  incoming/active calls, countdown, battery, navigation, recording, simultaneous
  activities, success/warning/error, quick controls and the access message.
- **Animations:** real renderer specs, spring stiffness/damping, Smooth or Linear
  duration/easing, 0.25×/0.5×/1×/2× transition speed, replay, stop and sequences.
  Production defaults remain active until overridden. Duration controls content
  fades in spring mode; spring geometry is governed by stiffness and damping.
  Disabling motion also disables waveform/press motion through Reduced Motion.
- **Geometry:** measured shell/action/icon rectangles, content-inset bands, size
  overrides, min/max bounds, padding, icon/text gaps, corners and action targets.
  Production window/status-side budgets still clamp requests. Some artwork and
  specialized reference controls own their own geometry; the token slider is
  explicitly scoped to compact/alert icons.
- **Stress Tests:** long or missing notification data, multiple actions, repeated
  expand/collapse, visibility, source swaps, multi-activity sequences and a bounded
  30-event burst. The mock clock advances timer/media/call/recording data locally.
- **Test Results:** immediate pure-policy checks plus a timestamped bundled host
  report and per-case details. The in-app button does not pretend to run Gradle,
  instrumentation, TalkBack or a pixel comparison.

## Shared production code

The Lab uses `DynamicIslandPill`, its actual content components,
`resolveOverlayDemoState`, `IslandInteractionPolicy`, shared visibility transitions,
`IslandMotionSettings`, `IslandPlacementPolicy` and the real App Kit controls.
`LocalIslandRenderSettings`, theme token providers and an optional geometry observer
are configuration/inspection seams. Their defaults preserve normal rendering.
There is no second implementation of the island in the Lab.

The isolated controller supplies mock `IslandState` values. It does not connect to
real notification intake, Telecom, MediaSession, billing, ad SDKs or contacts.
Mock call, media, action and upgrade buttons update local state/feedback only.

## Physical overlay mode

Tap **Start physical overlay** in Live Preview. Android overlay access is required.
This creates a debug-only application-overlay window, with a foreground notification
containing **Stop preview**. It also stops automatically after 3 minutes. Stop it
from the pinned Lab action or the notification. No mock overlay is shown on the
keyguard/screen-off surface. The normal production overlay is temporarily suppressed
while the Lab/physical preview is active and its prior suppression value is restored.

Physical mode reads actual display density/font configuration and public cutout /
status-bar metrics. Simulated device presets remain simulation-only. It does not
change the device’s display settings, make calls, control actual playback or show ads.

## Run automation

```sh
# Build + JVM tests + release isolation. Device/visual checks marked not_run.
tools/test-island-playground.sh

# Full suite on one explicitly selected device (avoids running twice on USB + Wi-Fi).
tools/test-island-playground.sh X4Y9QSOZ4DEADEDM
```

Reports are in `build/reports/island-playground/`. The script packages a debug-only
snapshot asset for Test Results. Install the final APK afterward to see that report.
Build failures explicitly mark JVM XML as possibly cached; failed/skipped device
checks are never converted to a pass.

Three deterministic reduced-motion Compose frames have pixel baselines in
`docs/design/playground-goldens/`. Comparison uses only the Python standard library;
it exports only synthetic fixtures. It fails on size changes, missing baselines,
mean RGB delta > 0.25, or more than 0.5% of pixels changing by > 8 levels.
Only after reviewing an intentional change, update baselines explicitly:

```sh
python3 tools/check-island-playground-screenshots.py --serial DEVICE_SERIAL --update-baselines
```

Baselines were produced on the connected Realme / Android 13 with density/font
controlled by the fixture. Another OS/font renderer may require a separately
reviewed baseline. These are not evidence of real call/media delivery, frame-time
performance, TalkBack traversal or universal camera/OEM compatibility.

## Layered Mini-Islands Lab (Task 05)

The debug **Mini-Islands** tab drives the canonical behavior engine and shared production child renderer. It offers 1/2/3 children, 10/25 bursts, 50 queued items, independent expiry, touch pause/release, source removal, promotion, calls and redaction. Existing device/font/RTL/reduced-motion controls apply. Presentation observations cannot replace a selected media/call fixture.

See [Task 05 evidence](../reports/TASK_05_TEST_AND_VISUAL_EVIDENCE.md) and [window limitations](../reports/TASK_05_OVERLAY_AND_TOUCH_SAFETY.md). Native application-window tests are distinct from production accessibility-window acceptance.
