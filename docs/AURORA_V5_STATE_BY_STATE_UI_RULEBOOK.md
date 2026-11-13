# AURORA V5 — Source-Verified Dynamic Island Design Constitution

**Status:** Proposed implementation contract / independent review baseline. **This does not indicate the Android code has been modified or the device has been tested in this turn.**

**Repository inspected:** `Harshil258/DYNAMIC_NOTCH_JETPACK`, ref `codex/aurora-v43-reference-ui` (not the older `main` or `codex/task-05-layered-mini-islands` branch).  **Primary files reviewed:** `DynamicIslandPill.kt`, `IslandFamilyGeometry.kt`, `IslandContentViewport.kt`, `AppTheme.kt`, `PlaygroundGallery.kt`, `FigmaReferenceFixtures.kt`, `MusicPlayerIsland.kt`, `OngoingCallIsland.kt`, `IncomingCallIsland.kt`, `LayeredMiniIslands.kt`. The authoritative asset source is `Dynamic Island states reference from figma/` (26 exported PNG references) and the owner-confirmed Minimal SVG.

**Important source hierarchy:** (1) owner correction/UX-C01–C15, (2) architect-approved PD-001 independent timers and behavior engine contract, (3) production permissions/action capabilities, (4) direct reference image, (5) family exemplar, (6) proposed Android layout. Measured pixels in a PNG screenshot are **not** automatically dp unless the exported frame, crop and scale are known. Existing V4.3 geometry is a starting target, not proof of visual compliance.

## V5.0 — Why existing gallery is not a pass

**Confirmed source defects / risks:**

- `DynamicIslandPill.kt::NotificationWithMusicCompactContent` uses album art → `Spacer(9.dp)` → `Box(weight(1f)) { LiveEqualizerMini(...) }`. A `Box` without explicit `contentAlignment=Alignment.CenterEnd` puts the equalizer at the START of the flexible area. Screenshot: visual waveform beside the album art instead of anchored to the RIGHT end of the main capsule. The `NotificationWithMusicSideContent` bubble holds the notification count; the main reference is a *single media capsule*, so compare the main capsule independently and document the extra companion as a product-only extension.
- `CompactIslandLayout()` uses `Arrangement.spacedBy(gap, Alignment.CenterHorizontally)` and consequently centers the group of leading/trailing children. This can produce arbitrary left and right free space; it is not equivalent to independent START and END gravity. Replace with a two-anchor layout that retains a flexible spacer/weight and measures leading/trailing widths.
- `MusicCompactContent` already uses `SpaceBetween`; its correctness must not be presumed from the generic helper. Audit both paths. `CallCompactContent` uses independent `SpaceBetween` and has a trailing waveform; validate its leading icon/text inset and trailing meter inset against `Call Time.png`.
- `FigmaReferenceFixtures.figmaNodeId()` maps unrelated demo states to the same Figma image (for example `MusicCompact` and `NotificationWithMusicCompact`; `TimerExpanded` and `TimerImage`). Reuse makes sense as a **family exemplar**, but a gallery label must never claim a direct state reference in those cases.
- `PlaygroundGallery` live view injects a density factor via `LocalDensity(Density(min(density,availableWidth/width)))`, while the reference PNG is displayed with `.size(target.width.dp,target.height.dp).clip(...)` outside the same render context. Side-by-side **is not a calibrated pixel parity test** until both branches are normalized to the same dp→px scale, content fixture, crop mask, optical baseline and settled animation frame.
- Gallery's green metric checks only the outer `width × height` (`abs(deltaW),abs(deltaH)<0.6`). It says nothing about icon x/y, text baselines, spacing, colors, corner rendering, gradients, wave count or wrong controls. It must display **geometry PASS only**, not reference-parity PASS.
- `IslandFamilyGeometry.resolve()` scales expanded width as `targetWidth * availableWidth / 393`. Treat this as a **proposed responsive policy**, not a valid universal scale for different Figma source canvases. Source family and screen-safe insets must decide expanded width; font scaling must change text and layout, not blindly shrink every visual anchor.
- `OngoingCallIslandExpanded` currently initializes local `isMuted`/`isSpeaker`/`isVideoOn`/`isSharePlay` booleans with `remember(variant)` and immediately flips them on tap; source call-controller confirmation must govern actual displayed status, and unsupported call capabilities must not appear functional. Its End Call handler invokes `onEndCall()` followed by `onControlAction(CallEnd)`; audit for duplicate end-call dispatch (single source action expected).
- `IncomingCallIsland` and `OngoingCallIslandExpanded` are different components. The gallery's `CallExpanded` maps to an **active** call and `CallAvatars` is not a substitute for the distinct **incoming/ringing** state. Add full runtime/event tests for ringing → answer → connected (compact/expanded) → ended and interrupted-notification restoration.
- Owner Minimal is the **108×32 empty red `#DC0917` opacity 0.60 capsule**, not the 38×38 glyph or a cyan chat icon. Preserve hardware-cutout adaptation in production. `Idle` camera-coupled black silhouette remains a separate state.

## V5.1 — Non-negotiable universal layout rules

| Rule | Requirement | Failure condition |
|---|---|---|
| G-01 Surface role | The main Island background is the ONLY primary black surface, with family-specific true intrinsic width/height/radius. Child/companion surfaces are independently bounded. | Generic black 367-wide card reused for compact, or parent wrapper mistaken as surface. |
| G-02 Two-end compact gravity | `START` slot and `END` slot are anchored to fixed family insets; flexible center = remaining space, **never centered group**. Visual center on Y. | `Arrangement.Center` used for two opposing slots; waveform sitting beside left artwork. |
| G-03 Exact inset ownership | Apply each horizontal and vertical inset **exactly once**, owned by the highest state-specific content layout. Shell is not permitted to add duplicate padding. | `padding(12.dp)` on parent and additional `padding(12.dp)` on child unintentionally. |
| G-04 Width equation | `shellWidth = leftInset + measuredLeading + requiredGap + flexibleMiddle + measuredTrailing + rightInset`. Clamp to cutout/system insets and actual available window; handle text overflow without shoving critical metric. | Arbitrary percentage spacers in tightly measured compact references. |
| G-05 Text gravity | One-line name/title and optional metadata share one START anchor; numerical countdown/time align END with tabular/fixed-width metrics. Vertical visual centers are shared with glyph center. | Baseline drift, text at x inconsistent with subtitle, number moves side-to-side. |
| G-06 Shape corners | Minimal: r15.9018 (108×32); compact: r≈half height; contextual 86dp: r43; tall activity: r42 unless specific reference differs. Stable continuous shape algorithm and antialiasing. | Radius computed from width rather than family, or global radius on all 60. |
| G-07 Waveform whitelist | **Default = no waveform.** If allowed, exactly one waveform of documented size and slot. Never draw two live waveforms in the same main capsule; no animated wave when playback paused/no audio source. | Extra magenta waveform after album art or any decorative wave in unrelated activity. |
| G-08 Button authority | Buttons appear only for real capabilities; positions follow reference row grouping, not generic two-button footer. Visual controls may be <48dp only when a **safe contained touch target** provides accessibility without intercepting outside-app touches. | Disabled action presented as tappable, overlap, or invented OS control. |
| G-09 Dynamic width | Title ellipsizes only after reserving icons, actions, number and safe padding. At 320dp, increase height or reduce secondary content—not shrink essential numerals or reduce touch safety. | Sender truncates below recognition while empty black area remains elsewhere. |
| G-10 Source fidelity | All galleries use deterministic fixture data matching the reference (same icon, time, text, cover, call state); any family-only exemplar is explicitly marked. | Reference shows album/wave on right, live shows extra unread badge but both labeled “PIXEL MATCH”. |
| G-11 Interaction owner | Visual layer cannot own its own queue, timer, call state or source action. Existing `IslandBehaviorEngine` and runtime `IslandStateManager` remain canonical. | Pixel polish changes notification visibility, source removal or call state. |
| G-12 Motion | Text anchors remain fixed during morph; animate shell and elements with a stable key, avoid remeasurement and target drift; freeze state and reduced motion for comparison screenshots. | New data triggers waveform teleport/recomposition, repeated action, or text bleed. |
| G-13 Privacy | Redact according to source sensitivity, current lock/privacy policy and screen-sharing context. Never show private OTP by a layout-only tweak. | Reference data bleeds into a real privacy-redacted production state. |
| G-14 Touch/overlay | Real production uses `TYPE_ACCESSIBILITY_OVERLAY`. Window touch rectangle and cutout insets are measured; transparent internal areas do not promise pass-through. | Reference-perfect demo while real overlay blocks underlying app. |
| G-16 Real call-control state | Mute, Speaker, Video, SharePlay and End Call UI are derived from source-confirmed capabilities/results, not speculative local booleans. Dispatch each critical action once, and disable pending actions until confirmed. | A tap flips mute visually when OS call controller did not actually change it; End Call fires twice. |
| G-15 Stage-only QA | `60-state Gallery` is debug: preview can share production renderer, but synthetic reference icons, fake call actions and debug metadata MUST NOT ship into production call/system paths. | Debug fixture changes actual call source or OS shade. |

## V5.2 — Absolute geometry vs responsive behavior

A **reference geometry** is a measured state-family target at its specified canvas, not a universal Android fixed dimension. Treat dp and source SVG units as comparable at a 1× design canvas, then explicitly calibrate device density, display scaling and real cutout. Never treat a reference PNG's transparent margins or baked shadows as content. If a PNG contains a system screenshot rather than an isolated capsule, identify/crop the actual capsule first. When a direct reference is absent, write `FAMILY_DERIVED` and have the owner review its first render; do not assert pixel parity for an invented reference.

Family rules:

- **S0 Minimal owner reference:** 108×32 r15.9018 `#DC0917` at alpha .60; empty, no glyph, no split; physical hardware safety may require a larger camera-aware black idle silhouette. This owner specimen does not automatically replace runtime Idle behavior.
- **S0 Idle hardware silhouette:** approximately 127×36 r18 (current reference family); black, empty, camera-centered and click → Quick Controls. No separate waveform, status label, fake badge or glow.
- **S1 Compact anchored:** state-specific widths (132/152/160/172/189/190/196/222dp or intrinsic), mostly 36–38dp high, half-height radius. START and END independently pinned; two meaningful content clusters maximum. A split variant reserves a separate side capsule (36.67dp, gap ≈11dp) when authorized.
- **S2 Contextual alert:** nominal 367×86 r43 on 393-wide reference; content-inset based one-row identity + optional trailing action. More lines, images, or 2-row actions require measured content-driven height; never silently clip to 86.
- **S3 Call activity:** Ringing 367×86 with Answer/Decline; connected 367×166–173 with identity and supported controls; compact connected 152×36.67; summary 367×112. These are distinct runtime substates, not interchangeable aesthetic skins.
- **S4 Media expanded:** nominal 367×177 r42; top art 53×53 at start inset 24, title/artist aligned, waveform at top TRAILING; middle chronological time/scrubber row; bottom standalone transport controls (no generic grey button circles).
- **S5 Activity expanded:** 367-wide family 112–210 height (per state), r42 when tall. One shared black surface, state-specific data rows, progress, controls. Do not invent progress for a source that lacks it.
- **S6 Mini-Islands / secondary child notices:** user-attention-only under expanded main; subordinate width `min(main−32dp, appropriate screen-safe max)`, source-validated, adapt 1–3 visible. Product implementation has independent visual timers; the mini stack is NOT a default under compact media. A visual timeout merges without OS/queue dismissal.

**Important proportional protection:** A 367×86 Figma artboard that includes two 52dp circular actions has only 34dp remaining outside those circles. This requires a carefully measured horizontal row. A 367×144 Screen Mirroring example has its full CTA in the LOWER region, not a generic centered row with blank 50dp beneath it. Use measured content slots, do not add extra vertical padding when the reference already accounts for it.

## V5.3 — Waveform position / content eligibility

| State | Leading | Trailing | Rule |
|---|---|---|---|
| Call Compact (connected) | Phone glyph + tabular elapsed | **ONE** voice waveform if source/reference mode | 12dp-ish leading inset, right inset from source. Main waveform must remain right-pinned; call changes to dialing/ended hides it. |
| Music Compact (playing) | 22–24dp album art | **ONE** media equalizer | No intermediate text/waveform unless shown by same matched reference; `Spacer(weight=1f)` anchors trailing equalizer. |
| Notification + Music Compact | Music album art at START | **ONE** media equalizer at END | Additional notification **companion bubble** is a separate sibling; isolate main capsule for single-capsule reference matching. No equalizer beside art. |
| Music Expanded | Top-row artwork + text START | Equalizer in top-row right | Progress and transport appear in separate rows below. |
| Ongoing Call Banner | Caller identity START | ONE badge with call waveform when supported | Never show Answer/Decline during active call. |
| Voice Memo Recording | Recording waveform at leading region only | Timer + verified Stop at trailing | Red record semantics; no second decorative waveform. |
| Video Remote | Video thumbnail/identity START | Audio indicator only if the reference/source includes it | Never use recording wave as default. |
| ALL other 53 states | Their own meaningful glyph/content | Their own meaningful metric/action | Waveform forbidden unless a new owner-approved reference explicitly requires one. |

**No waveform mirroring:** In a split presentation, do not clone an equalizer in both main and companion unless an explicit reference and product rule require two (none currently does). An available equalizer component is **not** a license to display it in unrelated states.

## V5.4 — Exhaustive 60-state rule matrix

Each row is individually normative. `REFERENCE` is either direct, analogous family-only, or explicitly absent. `FORBIDDEN` is a testable violation, not optional guidance. `Slot layout` defines intended START/CENTER/TRAILING/BOTTOM roles. `WaveformPolicy=FORBIDDEN` takes precedence over accidental animation components.

(See `AURORA_V5_60_STATE_RENDER_CONTRACT.csv` for the complete 60 rows table).

## V5.5 — Mandatory behavior invariants (all six personas)

**User types:** quick-glance, focused reader, heavy-notification, multitasker, power user, privacy/accessibility. Each must pass: no selected-page hijack, no duplicate actions, no unexpected OS dismissal, no unintended call interruption loss, responsive large text, keyboard/IME draft preservation, and stable main/mini presentation.

1. Idle tap → Quick Controls (UX-C01). One notification tap → expand inline (C02). Multiple tap → newest-first stable-key pager (C03). Reading selected key is never hijacked by ordinary arrival (C04). Source removal selects next/else previous (C05).
2. Close `×` defaults Island-only (C06); last removal returns Idle when no activity (C07). Meaningful updates resurface, minor metadata does not (C08–C10); first delivery nearby/milestone preview is transient and retained in queue.
3. Incoming ordinary notification while user-expanded main engaged → new secondary child below main (C11). Child tap promotes (C12). 1–2 small, max 3 large, proper +N for pending arrivals only (C13). Newest insert, older shift (C14). Smart child timeout touch-pause, visually merge, underlying queue remains (C15). Independent child generation/tokens (PD-001 / architect-selected C16).
4. Compact music + ordinary message → eligible temporary main preview then music compact restored; expanded user-owned music → child mini not interruption hijack (PD-026). Critical ringing call preempts as valid source; restored state and unsent reply draft preserved where possible, bounded by privacy validity.
5. Call lifecycle: `RINGING` (86dp Answer/Decline) → `CONNECTING` (if needed no false elapsed) → `ACTIVE_COMPACT` (elapsed, compact) ↔ `ACTIVE_EXPANDED` (capability-backed controls) → `ENDING/ENDED` (summary only if meaningful) → prior valid state. Never render incoming controls on active state; real event confirmations decide state, not animation completion.
6. Active timer, recording, navigation and other Live Activities never impersonate external apps. Read real capability metadata before showing buttons. No call or media control as fake gallery fallback in production.
7. Main deliberate collapse → visible children merge/retire and visual timers invalidated; queue remains, no OS cancellation. Call interruption → temporary suspension, not forced wholesale child deletion; no expired child reappears.
8. Privacy/lockscreen/OTP/financial redaction takes precedence over pixel matching on reference screenshot. TalkBack focus, reduced motion, 1.5/2× font, RTL, 320/360/393/430dp widths, left/center/right cutouts validated on screen.

## V5.6 — True visual evidence: no more misleading green badges

**1. Establish reference provenance** for each state: `DIRECT_ASSET`, `FAMILY_ANALOG`, `NO_DIRECT_ASSET`. `FigmaReferenceFixtures` currently maps some unrelated states to the same bitmap. Annotate gallery accordingly. Never call family/derived fixtures a direct reference match.

**2. Normalize density and crop**: PNG transparent region/shadow ≠ surface. Crop actual visible capsule, record bboxes, black-surface mask and shadow mask; normalize main source render AND reference to the *same* `px/dp` factor and same settled animation time. The current Side-by-Side gallery uses different density/render paths; fix it before trusting visual differences. Compare main-only when reference shows main-only; compare complete split stack separately against an authorized split reference.

**3. Unify fixture content**: caller, time, album art, package icon, message text, sender, playback status and child count must match exact reference. Use deterministic paused/playing, recording and connected flags; freeze animation for static visual parity. Never compare a real unknown device app icon to Figma icon as pixel parity.

**4. Evaluate FOUR independent passes:** (a) surface geometry, (b) content geometry: icon/label/wave/action bbox, baselines, gaps, (c) rendering: colors/shadows/radius/icon strokes, (d) behavior/action states. Geometry green does not imply B/C/D pass.

**5. Metrics:** on one normalized device viewport, shape bounds target ±1dp; icon/group bounds and major anchors ±2dp; title/subtitle baselines ±2dp when font family/rendering equivalent; zero unjustified content overlaps; no unauthorized waveform or control. Pixel MAE / perceptual difference are supplemental, not substitutes for measured region anchors. If original font family unknown, state `FONT_UNVERIFIED` instead of fake precision.

**6. Evidence for each of 60 states:** source PNG crop or family exemplar clearly labeled; actual Compose settled screen; alpha overlay; difference image; expected and actual shape dimensions; left/right/start/end gap measurements; text baselines; waveform inventory; 320/360/393/430 widths (family-specific sampling plus all 60 at 393). No direct source = no pixel-pass claim.

**7. Tool use — Jugg plus pixel capture:** Repository includes `.agents/skills/jugg-android-dev-loop/`. Use it with its scenario routing, incremental compile policy, project status, `deploy`, `layout-dump`, `view-locate`, `view-inspect`, `tap`, `wait-logs`. Jugg provides **view hierarchy, dp bounds and interaction evidence**; it does **not** currently expose a public screenshot command or a public `figma-layout-verify` call. Pair it with Android instrumented screenshot capture / authorized `adb exec-out screencap -p`, pixel analysis and Compose screenshot golden tests. Never write claims about Jugg auto-calculating visual parity. If the target overlay is not visible through the in-app hierarchy, use source-backed Compose test tags and supported instrumentation; do not fake hierarchy evidence.

**8. Guard production:** compare debug Playground using the same production composables; production `TYPE_ACCESSIBILITY_OVERLAY` device validation remains independently release-blocking until that service is legitimately enabled and measured. Debug `TYPE_APPLICATION_OVERLAY` does not substitute.

## V5.7 — Minimum automated evidence / test gates

- `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest` must pass after modifying source. Execute device instrumentation where connected and allowed.
- A deterministic screenshot baseline for **all 60 enumerated states**, a stricter pixel/geometry contract for DIRECT sources, and a separate state-transition gallery for ringing/active/call-end, music paused/playing, notification singles/pager, Mini child 1–3 and overflow.
- Mandatory negative tests: wave at left of media+notification; double wave; no equalizer source but playing visual; leading/trailing gravity collapse; wrong main-only vs split reference; gallery mismatch flagged despite outer width green; action unavailable shown as clickable; selectedKey shift; child timers after collapse; inline reply draft loss; overflow off-screen; no content source; no fake call action; app-underlay touch interception.
- Must report test categories honestly: JVM, Compose device, application overlay, real bound production accessibility overlay. Never mark release READY from gallery green numbers alone.

## V5.8 — Corrections ledger / explicit non-goals

The rules above are **proposed implementation corrections** derived from the reviewed source and owner screenshots, not code changes already applied. No approval of the prior rejected generic SVG packs. Do not modify design/reference originals. Do not re-introduce cricket fixtures. Do not replace the existing engine or notification listener. Do not label speculative platform actions as supported. Do not merge into main/release until authorized.
