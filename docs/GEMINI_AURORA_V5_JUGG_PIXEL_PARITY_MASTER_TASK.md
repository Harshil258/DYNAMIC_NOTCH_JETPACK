# GEMINI TASK — AURORA V5 Pixel-Parity Enforcement and Jugg Live UI Loop

**Role:** Staff Android Compose Engineer + Principal UX Visual QA + Interaction Reliability Examiner. Do the code, not just write another plan.

**Repo:** `Harshil258/DYNAMIC_NOTCH_JETPACK` and the owner's local copy at `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan`.

**Base:** Start from the newest reviewed work on `codex/aurora-v43-reference-ui` (check actual commit and worktree before edits); use a separate review branch. DO NOT silently return to `main` or an older Task 05 branch.

**Reference source:** `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/Dynamic Island states reference from figma` (26 owner-exported PNGs; preserve originals). The owner-confirmed Minimal SVG is also authoritative.

**Attached rule inputs:** `AURORA_V5_STATE_BY_STATE_UI_RULEBOOK.md`, `AURORA_V5_60_STATE_RENDER_CONTRACT.csv`, previous V4.3 rules for measured content geometry, owner Master Behavior Specification UX-C01–C15/PD-001, and supplied device screenshots of CallCompact and NotificationWithMusicCompact.

## Mission / quality bar

Actual goal: **every Dynamic Island state must be internally composed like its Figma reference**, not merely have matching shell W×H. Review and fix every source of left/right whitespace, misanchored text, misplaced waveform, wrong control, and inconsistent state behavior. The two screenshots are not isolated anomalies; use them as regression examples across all 60 states. Preserve the user's already-approved UX decisions. Do not just change 9dp Spacer and declare completion.

## Stage 1 — Inspect full repository and reference assets (required, with concrete report)

1. Identify the HEAD commit, existing uncommitted changes, build/variant and installed Jugg skill. Read `IslandFamilyGeometry.kt`, `DynamicIslandPill.kt`, `AppTheme.kt`, `IslandContentViewport.kt`, `PlaygroundGallery.kt`, `FigmaReferenceFixtures.kt`, `NotificationIsland.kt`, `LayeredMiniIslands.kt`, call/media/system/live renderer files, actual behavior engine and runtime service.
2. Inventory 60 `IslandDemoState` enum values and assign a unique rule from the attached CSV. Each renderer must have leading/center/trailing/bottom regions, reference priority, expected bounds/geometry, source-based actions, and explicit forbidden elements.
3. List every owner reference PNG, crop the real Island surfaces with their transparent/shadow bounds recorded, compare the pixel colors/positions in the source to the live Compose hierarchy. Do not invent dpi measurements from raw transparent PNG pixels.
4. Audit every wave/equalizer/voice activity call site; produce a table mapping exactly which source-state and slot may display a waveform. Default deny.
5. Keep a before/after evidence index; use source screenshots and the actual Figma PNGs, not generative approximations.

## Stage 2 — Fix the demonstrated failures first (P0)

**A. NotificationWithMusicCompact**: Source code currently uses artwork → 9dp Spacer → weighted Box with equalizer in its default START alignment. Make the MAIN compact surface `album art START | flexible empty center | single waveform END`. No waveform beside the artwork. The separate companion notification bubble is a product extension: maintain its own right-hand 36.67dp bounds and 11dp gap, but do not contaminate a reference comparison of the main-only capsule. Keep media paused/playing logical.

**B. Generic compact helper**: Existing `CompactIslandLayout()` uses center-arranged groups. Replace with robust fixed START/END anchors, exact family padding applied once and a measured flexible center. Do not allow `weight(1f)` to move a trailing icon to the left. Apply this discipline even to direct `Row(SpaceBetween)` components; verify call timer, app icon, count and equalizer.

**C. CallCompact**: Keep phone+stable elapsed START and source-backed call audio meter END, compare against `Call Time.png` at normalized same scale. No duplicate wave and no timer ticking until actual connection; no invented call action. Ensure leading inset and timer x-position are measured, and right meter x-position is independent of timer length.

**C2. Active call control truth**: `OngoingCallIslandExpanded` toggles local `isMuted`, `isSpeaker`, `isVideoOn`, `isSharePlay` immediately, and its End Call handler calls two callbacks. Convert control visual states to source-confirmed state and capability availability. Pending action should not masquerade as success. Prevent double CallEnd dispatch. Use incoming/ringing controls only in `IncomingCallIsland`, not for active call.

**D. Minimal**: Owner reference is 108×32 R15.9018 empty red `#DC0917` at 60% opacity, no chat icon. Keep Idle separate; don't break camera-cutout safety or UX-C01.

**E. Gallery**: The current `FigmaReferenceFixtures` maps some different states to the same bitmap, and `PlaygroundGallery` scales live/reference in different branches. Fix calibration and annotate `DIRECT`, `FAMILY_ONLY`, `NO_SOURCE`. Green `Actual W×H` shall mean geometry only, not reference-parity PASS. When comparing a source showing main-only, crop/isolate the live main and compare split bubble separately. Remove false “pixel perfect” labels and fix conflicting synthetic fixture content.

## Stage 3 — Implement all 60 rules and cohesive design families

For every row of the attached 60-state contract:

1. Apply the measured or expressly derived family W×H×R. Use true content-aware size, not forced broad banners, repeated double padding, global width multipliers or empty black filler.
2. Establish semantic slots and exact constraints. Leading icon must be anchored by source inset; title/subtitle to same x; trailing metric/control independently aligned; bottom controls in their own measured row. Text uses proper font, line-height, typography and ellipsis. Numeric values use tabular digits and retain right anchor.
3. Apply the per-state `WaveformPolicy` explicitly. Exactly one allowed waveform max, no motion when paused/blocked, no unrelated decorative wave in alerts, volume, flights, notifications, connected devices or charging.
4. Use the correct artwork/avatar/icon/progress shape, brand appearance when source permits, radius, edge inset, button shape, and visual weight. Do not use generic fallback circle/gradient for a direct reference unless original unavailable—then mark visual limitation.
5. When the source has a compact/expanded/active variation, implement distinct layouts and a valid transition, not just scale one card. For calls, explicitly validate ringing Answer/Decline vs connected controls vs ongoing compact, ending and restoration. Incoming call is a runtime state, not necessarily a 61st `IslandDemoState` enum value.
6. Real actions and privacy must remain correct. Do not alter canonical notification queue, selected key, independent child timers, privacy-redaction, source removal, gestures, or overlay permissions while adjusting visuals.
7. Non-reference states are family-derived—must look like the same professional system, but never mark them as a direct Figma pixel match without a direct exemplar.

## Stage 4 — Live Jugg plus screenshot evidence loop (mandatory)

1. Read the repository's `.agents/skills/jugg-android-dev-loop/SKILL.md` and its mandated flow. Use Jugg for incremental build/deploy and actual `layout-dump`, `view-locate`, `view-inspect`, tap/swipe and post-action dumps when Jugg's ViewHierarchy service is available. Do not invent or guess an auto-run entry: none was specified by the owner. Choose the normal compile/deploy flow or existing instrumentation hooks according to the skill.
2. Build/deploy to the authorized phone, launch the actual 60-state Gallery, select each state, capture the settled real component and locate its semantic region bounds. Implement stable `testTag`/semantics keys for meaningful regions; use actual Android hierarchy and normalized dp measurements.
3. **Jugg does not publicly expose a screenshot or `figma-layout-verify` command.** For screenshots use authorized `adb exec-out screencap -p`, Compose UI test `captureToImage`, or supported instrumented screenshot tooling. Store actual reference/live/overlay/diff images in versioned QA artifacts. Screenshot capture must freeze animation for static comparisons; provide motion frame sequences separately.
4. Compare **same content, same viewport, same device density/scale, same crop/mask, same animation phase, same font availability**. Properly crop transparency and shadow. Record leading/trailing icon center, text baselines, action target corners, radius, black surface bbox and independent wave count. Check negative space asymmetry against reference.
5. For a direct reference: aim ≤1dp shell-bound difference, ≤2dp major anchor difference, ≤2dp baseline difference where font metrics are actually comparable. No overlap, wrongful waveform, fabricated control, or clipped text. Unknown exact fonts are flagged—not falsely passed. For derived states use family gate and professional visual review, not fake 1:1 PNG parity.
6. Iterate until actual image comparison passes; a green outer-dimensions label is **not** completion. Do not do a single screenshot and move on. For 60 states, capture a fully labeled state matrix; keep failed screenshots in a `before/` folder for traceability and record corrections.

## Stage 5 — Behavioral / accessibility / overlay regression

Preserve UX-C01–C15, PD-001; non-destructive Island-only dismissal, 50-item stable-key newest-first pager, selected page never hijacked, media+message ownership, mini children 1–3 and overflow, independent child timers, tap promotion, restoring from actual call events, and lock-screen redaction.

Test 320/360/393/430dp, font 1/1.3/1.5/2, LTR/RTL, left/center/right camera cutouts, dark/light verification stages, no icon, long name, changing seconds/digits, paused/playback, real action capability absent, rapid events, double taps, small height, IME reply, 12/25/50 queue burst. Device production `TYPE_ACCESSIBILITY_OVERLAY` must be legitimately bound for a production touch-safety PASS; a debug application overlay is insufficient.

## Stage 6 — Required output, gate and STOP

Create reports:

- `reports/AURORA_V5_STATE_60_RULE_COMPLIANCE.md` (60 rows, before/after actual geometry and placement status)
- `reports/AURORA_V5_WAVEFORM_GRAVITY_AUDIT.md` (each wave call site, whitelist proof, screenshot evidence)
- `reports/AURORA_V5_REFERENCE_PARITY.md` (source crop IDs, normalized scale, overlays, diffs, measured anchors, failure trends)
- `reports/AURORA_V5_JUGG_DEVICE_SESSION.md` (actual CLI invocations and `layout-dump`/`view-locate` evidence, if device available)
- `reports/AURORA_V5_INTERACTION_REGRESSION.md` (engine, call, pager, children, privacy, 6 personas)
- `reports/AURORA_V5_FINAL_VERDICT.md` (verified/direct/family-derived/blocked classifications, unresolved issues)

Include 60 rendered screenshots at 393 baseline; representative alternative sizes for each family; direct-reference side-by-side and overlay/diff; all real Gradle/Android tests; exact source diff; git SHA and branch. Do not declare all 60 “pixel perfect” if any unverified or mismatched; mark honest FAIL/BLOCKED, then correct the implementation where possible.

**Implementation authority:** This task grants you permission to edit the Aurora application source on a review branch; it does NOT grant permission to modify reference image originals, delete user data, bypass permissions, run real destructive external app actions, merge to main or publish a release. Commit and push the review branch when completed, then STOP for independent examiner review.

**Hard completion criterion:** 60 valid state recipes and actual screen renders, 0 forbidden waveform/placement violations, a calibrated comparison harness, passing regression/build tests, and genuine evidence for every claimed direct-reference PASS. Pixel parity remains subject to real font/device differences and must not be fraudulently overstated.
