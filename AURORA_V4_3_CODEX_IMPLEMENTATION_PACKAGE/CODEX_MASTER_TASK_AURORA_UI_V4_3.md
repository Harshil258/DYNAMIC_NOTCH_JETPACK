# CODEX MASTER IMPLEMENTATION TASK — AURORA DYNAMIC ISLAND, ALL STATES

**Assignment:** Complete a production-quality, reference-faithful Jetpack Compose UI rebuild of **all 60 `IslandDemoState` variants** and all approved real behavior/interaction states. This is an **implementation task**, not a report-only design audit.

**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK` (private)
**Required starting branch:** `codex/task-05-layered-mini-islands`
**Last examiner-reviewed Task 06 commit:** `c5e98d76f118bdb16211e36ab85bf44bd5c28505`
**Do not base this work on an older `main` tree.** Confirm current branch SHA and investigate changes newer than that base before editing. If review branch has evolved, merge/rebase safely only after verifying compatibility; never discard user work.

**Design authority — files supplied with this task:**
1. `AURORA_DYNAMIC_ISLAND_COMPOSE_RULESET_V4_3_ARCHITECT_REVIEWED.md` — normative reference geometry, color, spacing, slot gravity, behavior, accessibility, tests, and release blockers; read §§0–30 in full.
2. `AURORA_60_STATE_GEOMETRY_TARGETS.csv` — **one row per source enum state**, exact baseline family and slot recipe; implement all 60.
3. Original owner Figma/SVG reference exports if actually available in the working tree or supplied attachments. Inspect their real pixels/layers; never invent dimensions or substitute the rejected generic 39/108-state SVG packs.

If a reference asset is absent, use the specific measured numbers and instructions in V4.3. Mark *reference-perfect pixel comparison* unavailable for that asset, but continue implementation using grounded rules. Do not claim visual matches you cannot verify. Keep all original owner references untouched.

## Non-negotiable mission

The owner rejected the prior UI because it looked like generic black cards with arbitrary icons/text and wrong widths. The new app must be composed like an actual Dynamic Island: precisely camera-anchored, content-driven, different designs for every activity family, aligned and usable typography, correct text gravity, correct controls, deliberate motion, no touch theft, and logical fallback under real Android restrictions.

**This is not done if you only change dimensions and radii.** Every state must have a semantic layout recipe; meaningful content from the real source or clearly labeled synthetic Playground fixture; properly aligned leading/center/trailing/bottom slots; safe Android action gating; and screenshot/measurement evidence.

### Absolute prohibitions

- Do not replace or bypass `IslandBehaviorEngine`, `IslandStateManager`, `NotificationReducer`, suppression tombstones, token-correlated timers, or the existing horizontal pager.
- Do not add another notification database or recompute business decisions inside Compose.
- Do not convert everything to one generic notification card with a title and two fake buttons.
- Do not scale text/touch targets merely to fit a fixed drawing; do not hard-code screen-center/camera-offset assumptions.
- Do not use placeholder emojis, outlined-path text, imaginary Apple-exclusive APIs, unsupported control actions, or arbitrary global gradients/glows.
- Do not claim 60 real integrations; some enum states are demos/references only. Implement **visual states** for all; enable system behavior only with genuine capabilities.
- Do not suppress, dismiss, or clear actual Android notifications on a visual collapse or timer expiry. `×` defaults to Island-only dismissal.
- Do not deploy a release, publish the app, modify external apps, alter secure Android settings, merge into `main`, or rewrite unrelated screens.
- Do not quietly mark BLOCKED or NOT RUN hardware tests as PASS.

## PHASE 0 — Inspect, baseline, and stage work

1. Read both input files and examine the entire existing UI flow. Start by examining:
   - `app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/ui/components/LayeredMiniIslands.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/ui/theme/AppTheme.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/service/IslandOverlayService.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt`
   - `app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt`
   - `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt`
   - `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundRenderer.kt`
2. Confirm exact base SHA and current Gradle project structure. Inventory all 60 enum states programmatically; map each to renderer, fixture, data-source capability, target family, current geometry and target geometry from CSV. Save this audit to `reports/AURORA_V43_BASELINE_AND_STATE_MAP.md`.
3. Record test baseline without falsely claiming physical verification. Capture baseline Playground screenshots from available emulator/device. Keep tests and unrelated features working.
4. Make a focused UI-refactor plan; work through **all** phases, do not stop after the first attractive screenshot. Record deviations, source limitations and actual test proof.

## PHASE 1 — Build the geometry and responsive surface foundation (P0)

1. Implement shared **family-specific geometry tokens**, not a single `height=145dp`, universal radius, or global `screenWidth/430` scale.
2. Use the **per-family reference canvas** from V4.3 §28.1:
   - Atlas: **393-wide reference, surface 367**, ~13 each side.
   - Presentation: **430-wide reference, surface 408**, ~11 each side.
   - Community: **430-wide reference, surface 366**, 32 each side.
   - Owner Minimal: **108×32, R15.9018, `#DC0917` at 60% opacity, no text/icon**. Keep this distinct from 38×38 minimal glyph circles.
   - True status compact: **36–37 high, R18–18.335**, intrinsic 120–250 widths according to content/state.
   - Contextual alert: **367×86, R43**.
   - Expanded activity: **367×142–185.36, R42** when that family applies.
3. **Do not reproduce the previous width bug**: at 393dp canvas an atlas `367u` item must target approximately **367dp**, not 335dp. The 430-to-393 rescale is NOT a universal rule. Handle real safe insets exactly once.
4. The current source uses `AppIslandTokens.compactHeight=36.67dp` and width selection inside `DynamicIslandPill.kt`, but mixes demo state classification and presentation families. Replace the classification with a typed, testable geometry family resolver **while preserving public component/event APIs as feasible**. Make current enum-to-geometry mapping explicit. Treat an expanded notification with an image as a different measured content configuration, not a forced generic 86dp pill.
5. Use actual `WindowInsets.displayCutout`, available window bounds, status-bar/system insets and the project's calibration preferences. Physically protect the camera; keep left/right/center cutouts safe. Avoid fake black punched holes or blindly centered offsets.
6. No clipping on 320/360/393/430dp screens, RTL, landscape, cutout changes, split-screen, and 1.0/1.3/1.5/2.0 fontScale. Reflow second actions or hide nonessential metadata when space is insufficient. Do not shrink touch targets to 20dp to preserve a screenshot.
7. Geometry resolver **must be unit-testable without Compose rendering**. Add tests for exact baseline widths/radii, content-aware bounds, fontScale growth, insets, and off-center cutouts. At a reference viewport with standard content, measured outer surface bounds should match targets to within ~1dp when feasible; any legitimate deviation must be explained and evidenced.

## PHASE 2 — Actual slot-based composition and typography (P0)

Use semantic **Leading / Center / Trailing / Bottom** with optional slots and correct gravity; specialized families may contain nested rows/columns. These slots are **not** a mandate to fill four boxes in every state.

- Leading is start-aligned and vertically centered as a group; use accurate app icons/avatar/art/progress ring shapes, not meaningless circles.
- Center uses available width and aligns the title and subtitle at the same start x. A 2-line block is centered vertically as a unit. Width is measured from content, text does not jump to a new line on a timer tick.
- Trailing stays anchored at the end. Timers, counts, progress and percentages use tabular figures and reserved-width boxes; rapidly changing values do not resize the whole Island.
- Bottom is for genuine media controls, call action rows, navigation strips, CTA pills, or a scrubber. Only render it if the state family requires it.
- Use real Compose `Text` and reusable Android vector assets/paths as appropriate; no fake outlined SVG text, emoji and generic icon placeholders. Use existing app typography conventions (Roboto/platform fonts where suitable). SVG outlined text was not a reliable proof of a font family; do not pretend exact font matching is verified.
- Follow V4.3 §7 text sizes, roles and `TextStyle(fontFeatureSettings = "tnum")` when supported; body/title contrasts appropriate on true black.
- Long sender names, URLs, Gujarati + English mixed messages, RTL, dense notification text, absent avatar/icon, huge numbers, 2.0× text and TalkBack must be handled deliberately. Essential numerals must remain legible, nonessential text may end-ellipsize. No `SpaceEvenly` for critical alignment.
- `43dp` is a **measured visual CTA height**, while an effective interactive target should be ≥48dp where safe. Provide the target *inside the real overlay footprint*, not invisible full-screen touch capture.

## PHASE 3 — 60/60 source-state visual reconstruction (P0)

Implement **every row** in `AURORA_60_STATE_GEOMETRY_TARGETS.csv`. The detailed state-to-slot matrix in V4.3 §29 is mandatory. Do not skip obscure states, do not mark concept-only variants as integrated platform functionality, and do not use identical black cards with swapped titles as a shortcut.

### Required differentiated activity compositions

**Minimal and Idle**
- Owner Minimal specimen exact 108×32 red at 60% opacity, no icon/text; independent of black 38×38 glyph atlas and actual cutout-absorbing Idle. Maintain runtime semantic distinction instead of treating `IslandDemoState.Minimal` as automatically equivalent to Idle or a detached bubble.
- Idle compact approx 127×36, R18; tap Quick Controls, long-press Settings/Calibration; idle/compact swipe up safely no-op/subtle squeeze by default.

**Music**
- Compact ~190×36.67, album-art/identity and trailing waveform; expanded **367×177 R42** with **53×53 art**, correctly aligned track/artist, timeline **240×6.5** with fixed-width elapsed/remaining metrics, and bottom transport row aligned at measured control centers. Transport glyphs are **not generic circular buttons**; actions reflect real MediaSession capabilities, progress seeking if supported, and correct pause/play/skip. Keep content aligned and album art appropriately cropped.
- Separate video remote controls and media+notification companion logic; expanded music is not hijacked by ordinary messages.

**Calls**
- Incoming compact contextual **367×86 R43** (or true compact where meaningful) with caller avatar, medium/number, and distinct green Accept/red Decline **only if supported**. Ongoing expanded call **367×166 R42**, avatar 44.3, caller identity, actions up to five with circular diameter ~50/pitch ~64.3, end action visually distinct. Do not fake answer/decline, stop a real call from swipe-up, or cover native call/emergency UI.
- Separate call summary, conference avatars, shared-media, video/VoIP concepts; show graceful reference-only Playground variants when real Telecom role/capability unavailable.

**Timers, charging, ringer, Bluetooth, status HUD**
- Timer action capsule **367×85.73 R43**, leading Pause/Cancel only if app-owned, trailing Timer label and big fixed-width orange time. Compact timer intrinsic ~222×36.67.
- Charging/low battery: compact 132×36.67 and contextual 367×86 where needed, real battery status without fake toggles; align battery/progress and percent.
- Ringer/silent/normal/vibrate and volume compact 152/172×36.67; silent expanded 367×86 with glyph + aligned two-line status + 79×34 Unmute only if available; never spam repeated status.
- Bluetooth connecting/connected compact 196×36.67; connected contextual alternative 366×95 R46, ring and device name; status from permissions/device. Surface must not look like music player just with a swapped title.

**Navigation, delivery, flight, sports, transport and other live activities**
- Navigation expanded **367×185.36 R42** with maneuver hierarchy, next-turn arrow, fixed-width distance, road shield/name and destination context. Do not fabricate Maps data.
- Delivery/flight/sports use specific timelines, route/score/status rows and real source-supported CTAs. **No cricket fixtures**. No 145dp uniform activity template; distinct family composition and truthful data availability.
- Transport/flight tracker, screen recording, voice memo, shortcut, AirDrop-style transfer, AirPods-style battery, Find My/satellite demos, screen mirroring, airplane/mobile data and subscription states use their own **source-specific §29 recipes**. Apple-exclusive states must remain reference-only or show truthful Android equivalents.
- True Mini-Island rows remain compact independent surfaces, not another ordinary Android heads-up banner.

**Quick Controls and 50-notification experience**
- Real Idle→Quick Controls flow; grid with user-useful controls and honest capability/disabled state. It must not create synthetic notifications when the queue is empty.
- One notification: single detail; 2–50: horizontally swipable, lazy pager. Newest first initial selection, page key stable through insertion/removal, exact `k of N` and ≤7 sliding dots, no user page hijack. No extra click to reveal dots, no bogus buttons, no forced app launch.

## PHASE 4 — Real production wiring, not fake preview-only UI (P0)

- Source engine owns the queue, status, selected key, suppression, timers, priorities and actions. Existing `IslandStateManager.behaviorPresentation` is a **`StateFlow<IslandBehaviorState>`**. Use it or a documented, tested immutable UI-only projection; do **not** invent an already-present class called `IslandPresentationState`.
- Renderer remains stateless from business-policy perspective. All tap/hold/swipe/action intents flow back through the existing engine/controller. Preserve task 03–06 fixes including keyboard reply, key-stable pager, suppression, source removal and one authoritative timeout path.
- IMPORTANT: preview expiry must begin after actual visible/readable settle rather than posting event; build an engine event or tested equivalent acknowledgment. Child timeout must begin after its own settle, remain independent and reject stale generation callbacks; no arbitrary coroutine timer inside a composable that duplicates engine effects.
- Validate `×` Island-only, optional OS dismiss only with explicit preference + clearable + actual bound NotificationListenerService capability. No `NotificationManager` hack for other-app notifications.
- Integrate real capabilities only when available: `RemoteInput` reply, PendingIntents, MediaSession, battery/BT, Telecom. Null/canceled actions should show truthful fallback without crash. Playground must never trigger external PendingIntents or OS cancellation.
- Live-source completion/removal never resurrects ghost pages/children. Process restart and permission revocation reconcile the queue and redaction, not stale screen fixtures.

## PHASE 5 — Premium motion and layered Mini-Islands (P0)

- Preserve owner behavior UX-C01–C15 and architect-selected PD-001 independent timers (sometimes called UX-C16). Do not turn it into fake design-only animation.
- True `LayeredMiniIslands` target: child visible height ~50–52dp, R20, vertical gap ~6dp, width `(measuredMainWidth−32dp)` appropriately bounded 240–336 only if real viewport supports it. Newest child appears immediately beneath expanded main; up to 1–2 on smaller screen and max 3 larger; remaining count is pending **new arrivals**, not all 50 queued notifications.
- Sequence: `QUEUED → EMERGING → SETTLING → VISIBLE → EXITING → MERGED`. Child touch pauses **only its timer**; release resumes remaining life plus ~2s grace. Tap promotes child to main with no duplicate content; previous main remains in pager. Old children smoothly shift downward. Child expiry/merge never removes queue or Android notification.
- When main is deliberately collapsed, all visible children visually retire and callbacks become stale; no child remains rendered beneath compact main. When call interrupts, hide children and preserve monotonic deadlines; only nonexpired valid children may restore. Device lock redacts content even during motion.
- Use continuous interruptible shape/size animations and controlled content stagger, no spring overbounce that shifts action hitboxes. Reduced-motion equivalent must remain fully usable. Avoid full-window remeasure on every animation frame; measure content before morph and use stable IDs.
- If short height/IME means zero children/overflow fit, make pending content available through the pager or an accessible control. No overlay covering keyboard or app controls to force a child chip.

## PHASE 6 — Strict UX contracts, calls/media/privacy, and all six personas (P0)

Implement every UX decision table entry in V4.3 §11, every activity-arbitration case in §§15–15.3, and every persona failure mode in §21:

1. **Fast glance:** legible auto-preview; short ~3.5s or longer ~5s (max 6), counted **from readable presentation**, then non-destructive compact residual.
2. **Focused reader:** selected key preserved, pager page number may shift but content doesn't; reply draft survives arrivals, calls and brief outside-tap/Back (~60s in unlocked memory, subject to privacy); user-expanded content does not auto-expire.
3. **Heavy notification user:** 50 eligible queued, no duplicates/ghosts, accurate counts, +N only for pending child notices, responsive multi-item pager.
4. **Multitasker:** expanded music + ordinary arrival gives child; compact music (not actively controlled) can preview briefly and restore media with companion; ordinary alert never trumps call; nav turn-now and timers have bounded urgent priority, recording indicator persists.
5. **Power user:** Quick Controls, status calibration, optional OS-dismiss preference, helpful accessible settings, predictable gestures, no destructive double taps.
6. **Privacy/accessibility:** lock state, OTP/finance even unlocked if private preference, recording/mirroring, TalkBack and stable focus, system reduced motion, fontScale 200%, one-handed safe touch and bidi/RTL.

Calls: a valid incoming call must have safe prominence during inline reply, but preserve the draft/IME session; restore previous valid selected key if unlocked/source still exists inside ~10s after interruption. No fake dialer control and never accidentally reject a call.

## PHASE 7 — Playground, visual acceptance and measurable proof (P0)

### Every visual state is testable

- Add a **gallery of exactly all 60 enum states** and a separate behavior lab for Idle, Minimal, multiple notifications (1/12/25/50), read page 5 + arrival, source removal, × suppression, child 1/2/3 + overflow, child touch/promotion/expiry, call/media/nav/timer, privacy, large text, RTL, cutout position and short-height/IME. Use the **shared production composables**, not duplicated fake cards.
- Make synthetic fixtures realistic but non-sensitive and labeled. No interactions in Playground may launch real external intents/cancel shade notifications.
- Each gallery entry must show its actual surface footprint and measure width, height and radius on-screen; provide a toggle showing reference target and current measured deltas. Captures must be at native reference-scale, not arbitrary 0.5× screenshots.
- Include a before/after screenshot catalog by family, and an all-60 atlas, with each frame titled and sized. Check both natural height and fontScale-2 fallback without false “pixel perfect” claims.

### Mandatory device/simulator matrix

- Screen widths: 320 / 360 / 393 / 430 dp; landscape; split screen; left/center/right punch-hole and waterdrop profiles; notches physically measured on a device where possible.
- 0/1/2/12/25/50 notifications, same-key update, source removal, Island-only vs OS removal, readable preview time, bursts and stale timers.
- UI fontScale 1.0 / 1.3 / 1.5 / 2.0; long names; mixed Gujarati/English, RTL; reduced motion, TalkBack focus, color/contrast.
- Active call, reply/IME, music compact vs user-expanded, timer, urgent navigation, power HUD and lock/privacy conditions.
- Real foreground app touches **outside window**, **transparent gutters inside window**, **visible interactive Island**—classify and verify each separately.

### Required automated acceptance

1. Add pure Kotlin geometry resolver tests **for every one of the 60 enum states** (baseline W/H/R and safe width clamping).
2. Add screenshot/golden or robust Compose UI tests for each of the 60 state appearances; if platform capture is unavailable, keep a truthful pending visual-proof matrix with actual generated PNGs where available.
3. Add behavior tests for all **64 scenarios in V4.3 §22**; existing coverage may count after mapping test names to scenario IDs. Do not rewrite passing tests merely to inflate totals.
4. Add test assertions that Compact remains compact, 367×86 contextual is distinct, Minimal is 108×32 owner reference (with safe adaptation), media scrubber/controls align, and CTA 43dp visible shape is still an accessible action.
5. Add timer and process restoration tests for stale tokens, child independence, preview readable-start, interruption/resume and normal user collapse.
6. Assert no unsupported actions are actually dispatched, no notification shade cancel on visual collapse, no queue loss and no user-page hijack.
7. Run and report actual result of:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleDebug
./gradlew :app:assembleDebugAndroidTest
```

Run applicable instrumented suites on an authorized emulator/device. If physical accessibility permissions are needed, **do not** enable them covertly: document the normal user-controlled Settings path and mark production-overlay tests **BLOCKED** until granted. The prior Realme 8 / Android 13 tests of an application-overlay **do not** prove production `TYPE_ACCESSIBILITY_OVERLAY` safety.

### Performance proof

Capture repeated emergence/reorder/merge, main expansion/collapse and pager swipe under realistic load with a meaningful frame sample. Report total frames, 50th/90th/99th end-to-end latency, janky count/percent, memory observations and source of tooling. A 12-frame GPU timing sample cannot prove smooth 60fps. Do not claim a guaranteed refresh rate.

## PHASE 8 — Review, documentation, commit and stop

Create:

1. `reports/AURORA_V43_BASELINE_AND_STATE_MAP.md` — 60-row current vs target, actual code paths.
2. `reports/AURORA_V43_DESIGN_SYSTEM_AND_GEOMETRY.md` — all sizes/radii/colors/typography/constraints, family resolver, measured deltas.
3. `reports/AURORA_V43_60_STATE_VISUAL_GALLERY.md` — all 60 unique screenshot references, implementation status, actual per-state dimensions, reference comparison.
4. `reports/AURORA_V43_UX_BEHAVIOR_AND_PERSONAS.md` — 16 UX decisions, 64 acceptance scenarios, six personas, pass/not-run matrix.
5. `reports/AURORA_V43_TOUCH_PRIVACY_AND_PLATFORM.md` — window classes, privacy and real permission status.
6. `reports/AURORA_V43_TESTS_AND_PERFORMANCE.md` — reproducible commands, XML/log paths, timings, screenshots, frame evidence.
7. `reports/AURORA_V43_FINAL_EXAMINER_HANDOFF.md` — concise executive status: done/partial/blocked with diff, SHA, risks.

Also deliver `reports/AURORA_V43_STATE_AUDIT.csv` with all 60 names, exact expected W/H/R, actual observed W/H/R, measured error, family, renderer, sample fixture, actions, test, and screenshot path.

Use a new branch such as `codex/aurora-v43-reference-ui` based on the review branch. Commit all modified source/tests and reviewable assets/reports. Push to the existing authorized private origin branch only; **do not merge `main`, create a public release, or claim completion without evidence**.

### STRICT FINAL COMPLETION GATE

- No missing enum state from the 60-state UI gallery.
- No state represented only by a generic black card with swapped title and fake CTA.
- Every family has exact, audited width/height/radius targets and responsive fallback.
- Every icon, text baseline, metric, scrubber and action obeys its gravity/slot recipe.
- All production actions real/capability-gated; all reference-only states visibly labeled in Playground.
- Existing notification pager, user focus, independent children, suppression and call/media arbitration regressions all passing.
- No overlapping/cut-off text or child cards at the required responsive configurations.
- Actual cutout coupling verified where real camera geometry is available, and unverified hardware cases honestly blocked.
- No production accessibility-overlay PASS without legitimate bound-service physical proof.

If a portion is technically blocked by missing permissions or absent external integration, **complete the other 60-state visual work anyway**, mark that portion BLOCKED with exact cause, and do not fake it. If a visual state cannot be completed despite a code-supported fixture, mark it REWORK with concrete source path and screenshot rather than silently skipping it.

**STOP at the end of implementation. Return the private branch, full commit SHA, screenshot gallery index, 60-state completion matrix, 64-scenario verification matrix, exact Gradle test totals, and unresolved blockers to ChatGPT for independent examination. No background promises and no self-declared production release approval.**
