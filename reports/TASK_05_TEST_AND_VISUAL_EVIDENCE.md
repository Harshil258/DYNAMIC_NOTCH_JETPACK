# Task 05 — Test and visual evidence

Required base commit: `3e47d3f9686473b2c17b3e038d3d6c8f61e8f5ce`.
Review branch: `codex/task-05-layered-mini-islands`. Final commit is recorded in the submission; use `git rev-parse HEAD` on that branch. No remote push or release publication.

## Environment and evidence boundaries

Realme RMX3085, Android 13 / API 33, 1080×2400, density 480dpi. Authorized connected target: `192.168.29.20:5555`. Direct Codex/ADB exploration and AndroidX Compose instrumentation were used. ARTEMIS doctor confirmed missing model credentials; owner explicitly requested Codex itself as fallback. No credentials were requested through chat.

All scenario content is synthetic. No live message, OTP, contact or external PendingIntent is used in fixture captures. Simulated logical widths/font scales are labeled as such, not additional physical devices.

## Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleDebug
./gradlew :app:assembleDebugAndroidTest
```

The commands were batched where appropriate. Final source verification and device results are recorded below after the last run. No clean, release build or publish command is required.

## Coverage

| Acceptance | Evidence |
|---|---|
| Main selection pinned; old history excluded from overflow | `LayeredMiniIslandsTest.oldQueueIsNotPendingOverflow`, existing runtime page-5 scenario |
| Stable newest insertion below main, adaptive card count | Shared projection unit test; Compose insertion/bounds and narrow-font tests |
| 10/25 bursts and bounded 50-item pager | Domain burst up to 250 arrivals, runtime 50 retention, lab 10/25 and 50 controls |
| Independent correlated deadlines, hold/release, readable budget once | Domain deadline/update, sibling expiry, touch/grace, reused key and first-settle tests |
| Promotion, original main retained, expiry queue retention | Domain and Compose tap/expiry tests |
| Removal during promotion/key reuse | Domain correlation and Compose delayed tap/removal fixture |
| Calls don't resurrect children or removed sources | Domain live-queue call restoration; existing runtime call tests; mock lab controls |
| Reply ownership and collapse non-destructive | Domain reply+25 arrivals+collapse; existing reply lifecycle tests |
| Locked privacy | Projection test and narrow large-text redacted Compose capture |
| Shared production/Lab behavior | Same reducer and renderer; existing runtime equivalence test |
| Android cancellation never caused by merge/promotion | Negative emitted-effect assertions; lab has no external-effect executor |
| Horizontal pager with children | Compose swipe test plus existing pager-indicator device tests |
| Native window touch routing, OEM cutouts, real IME/calls/TalkBack/performance | Only the specifically recorded device checks below; remaining risks stay unverified |

## Captures

`reports/task05-evidence/` contains synthetic fixture PNGs exported from device app storage. The fixture uses the real `DynamicIslandPill`, real pager and shared child renderer. Emerging and merge frames are animation snapshots, not a frame-rate benchmark. Native overlay captures/recordings, when present, are labeled separately.

## Final results

- **PASS: 481 JVM tests**, zero failures/errors/skips. Includes 35 behavior-engine tests, 20 runtime integration tests, 13 new layered-domain tests, 9 Playground controller tests and 16 reducer tests.
- **PASS: requested forced unit rerun** (`--rerun-tasks`), 480 tests at that checkpoint. A subsequent fixture-preservation regression brought the final full incremental suite to 481, all passing.
- **PASS: compileDebugKotlin, assembleDebug, assembleDebugAndroidTest** on final sources. `git diff --check` passes. No lint or performance certification is claimed.
- **PASS: 13 Compose/device tests**: seven new layered tests, two existing pager-indicator tests, three existing Lab tests, and the existing layout test covering 68 rendering combinations.
- **PASS: 1 separate plain Android native-window test**. The application-overlay adapter used the real shared renderer and real window frame clock. The native window wrapped the stack; a dynamically located (ADB-verified coordinate fallback available) injected Stop-button touch reached the underlying app outside that rectangle. Canonical queue count remained four after stopping.
- **NOT RUN / BLOCKED: production accessibility-window routing.** Android reports the production accessibility service unbound after reinstall. The native application-window result is not represented as production accessibility-window certification.
- **NOT RUN:** actual carrier calls, real source inline reply/IME, physical lock/unlock, rotation, TalkBack and frame pacing. Their unit/render fixtures are not substituted for real-platform acceptance.

Logs: `task05-evidence/compose-device.log`, `native-window-device.log`, `forced-unit-tests.log`, `final-build-and-tests.log`, and `instrumentation-build.log`. Machine-readable totals: `task05-evidence/results.json`.

Earlier failed runs found a missing lazy-item wait in a test, a preview-stage clipping defect, a Compose test-frame-clock limitation for service windows, an accessibility lookup limitation in the native test, and lost burst completion feedback. These were corrected and the complete device suites rerun successfully; failed interim captures/logs are not presented as final acceptance.

Review reports: [behavior/data](TASK_05_BEHAVIOR_AND_DATA_MODEL.md), [motion](TASK_05_ANIMATION_IMPLEMENTATION.md), [overlay/touch](TASK_05_OVERLAY_AND_TOUCH_SAFETY.md), [Playground](TASK_05_PLAYGROUND_VALIDATION.md), [remaining risks](TASK_05_REMAINING_RISKS.md). Reviewable source diff: `task05-evidence/TASK_05_SOURCE.patch` (zero-context unified diff; use `git apply --unidiff-zero` against the required base if applying it separately). The branch commit provides the normal full-context diff.
