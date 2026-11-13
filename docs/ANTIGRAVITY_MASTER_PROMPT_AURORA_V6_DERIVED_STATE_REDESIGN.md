# ANTIGRAVITY — AURORA V6: PROFESSIONAL REDESIGN OF ALL NON-REFERENCED DYNAMIC ISLAND STATES

**Assigned by:** Principal Product Architect & Independent Examiner (ChatGPT)  
**Assignee:** Antigravity cloud/local Android engineering agent — assume the role of Principal UI/UX Product Designer + Staff Jetpack Compose Engineer + Android capability architect + Test/Visual QA lead.  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Required starting branch:** `codex/aurora-v5-pixel-parity-review`  
**Verified review commit:** `b613666c973973d43871f3b8ae9c14e8d7814bca`  
**Owner's local Figma asset folder:** `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/Dynamic Island states reference from figma`  
**Current known failed screenshot:** `evidence/CallSummaryExpanded_user_failure.png` from this package.

**Source documents supplied alongside this prompt:**

1. `AURORA_V6_DERIVED_STATES_DESIGN_RULEBOOK.md` — authoritative, actionable V6 product/design constitution.
2. `AURORA_V6_60_STATE_PROVENANCE_AND_DESIGN_MATRIX.csv` — exactly 60 enum rows, all 32 derived-state recipes.
3. Existing `AURORA_V5_STATE_BY_STATE_UI_RULEBOOK.md` and V5 geometry/UX docs (preserve non-conflicting verified reference rules).
4. Owner's original Figma export images/SVGs. Their specific measured patterns outrank derived proposals for DIRECT states.
5. Existing UX-C01–C15, architect PD-001 independent child timers, product behavior/acceptance matrix and actual Android code.

## 0. YOUR PRODUCT GOAL — NO EXCUSES

The owner rejects the current generic, cramped, novice-looking Islands. You must **implement the complete correction in real Android Compose source code**, including the 22 `NO_SOURCE` and 10 `FAMILY_ONLY` states. The absence of a direct Figma reference is not permission to design a random card or leave an empty Gallery sample. As product designer, derive a polished, specific layout from the right Aurora design family with clear data semantics and genuine user utility.

**Never substitute a mockup, README or screenshot for actual changes.** Do not stop after an audit. Do not claim a design is reference-perfect if its state lacks a direct reference. Do not report ALL GATES PASSED when the supplied screenshot disproves even the geometry gate.

## 1. Confirm the real state inventory and evidence before editing

- Fetch/inspect current reviewed branch and compare with local checkout; do not silently switch to `main` or use old stale files.
- Read every enum value in `DynamicIslandPill.kt`, geometry targets/resolver, render routes, `IslandPresentationMapper`, call summary renderer/actions, reference fixtures, Gallery, all related theme tokens, behavior engine and source services.
- Read actual files in the Figma folder. Crop black capsule bounds independently of transparent margins/shadows and identify source asset, canvas scale, corner shape, text/item gravity.
- At the reviewed commit, `FigmaReferenceFixtures.referenceProvenance` lists **28 DIRECT / 10 FAMILY_ONLY / 22 NO_SOURCE**. Its V5 report says **19 DIRECT / 41 family**, a contradiction. Reconcile classifications from actual assets, correct claims, and produce exact provenance per state. It is unacceptable to mark analogous imagery as a DIRECT match.
- Reproduce the attached screenshot state `7/60 CallSummaryExpanded`. Note `367×112` target and `366.8×108.1` rendered. Capture all inner text and action bounds too.

## 2. P0 — Fix CallSummaryExpanded as a real professional post-call experience

Review `CallSummaryIsland.kt` and call-state origin, including source phase/record and all actions.

**The current design is rejected:** it shows post-call details with the words “Incoming call”, a bright green `02:45` that resembles an active timer, and four equal Redial/SMS/WhatsApp/Done pills. The direct reference is missing; design an authentic Aurora-family summary, not a contact-management dashboard.

Implement a distinct post-call visual blueprint:

- Target **367×112dp, R42** at the reference 393dp canvas; adapt height only if real content/font scaling requires it. Document measured actual shell W/H/R.
- Top row: 36–40dp avatar START; caller name 15–16sp semibold with correct truncation; second line `Call ended · Incoming`, `Outgoing call ended` or `Missed call` based on actual phase. Static verified duration END, tabular, neutral. Do not animate elapsed after termination. Unknown/inferred duration must be honest.
- Bottom: one useful primary action if available (usually Call back/Redial), plus unobtrusive dismissal. Messaging app actions only if resolvable/capability-backed and explicitly discoverable, NOT four equal default CTAs. If no action, provide a clean summary with dismiss.
- Extract synthetic sample identity into debug data. `record == null` must not cause Tamia/phone/02:45 or actionable contact shortcuts in production.
- Accessibility 1.0/1.5/2.0 font scaling: no clipped text or four-button wrap; if necessary naturally increase height instead of lying about the 112dp target. Verify TalkBack order.
- Tests: incoming answered→active→ended summary; missed; rejected; outgoing; duration unknown; inferred duration; no phone number; source action unavailable; lock-screen privacy; expiry/removal/restoration; no duplicated call action.

**Hard gate:** A 4-button cramped layout, stale “Incoming call” phrasing, fake WhatsApp CTA or ≈−3.9dp unexplained height difference is an immediate FAIL.

## 3. P0 — Repair broken production mapping and data ownership

A. `IslandPresentationMapper`: `LiveActivityKind.NAVIGATION` expanded currently maps to `IslandDemoState.ColorOptions`. This is structurally wrong. Route to a legitimate navigation-expanded renderer with maneuver, distance/route fields and real interaction capabilities; keep `ColorOptions` exclusively for intentional color customization/debug previews. Add mapping regression tests.

B. `DynamicIslandPill.kt` -> `NotificationExpandedContent`: when `notifications` is empty, main source includes a synthetic sample list. Remove this from production. Gallery fixtures may inject explicit samples, but actual empty queue must yield the approved Idle/Quick Controls behavior. Verify no ghost notifications.

C. `IslandPresentationMapper`: `IslandState.IncomingCall` currently maps to `CallAvatars`, which may imply group callers rather than genuine ringing. Trace the actual Android call phase and implement a real ringing renderer with capability-based Answer/Decline, distinct from connected compact/expanded and ended summary. Preserve source-confirmed call controls; never fake the OS role.

D. `IslandFamilyGeometry.resolve`: noncompact width currently scales by `t.width*availableWidth/393`. It expands a 367dp family to ≈402dp at 430dp without evidence. Replace with family-defined max width and safe margins; preserve owner reference geometry at 393dp and legitimate different families at 430dp. No global width multiplier. Ensure one owner of insets and no padding stacking.

E. For main-source controls, don't implement fake toggle success or generic app action. Inspect `CallSummary`, connectivity, Quick Controls, ScreenMirroring, MobileData, Focus, Timer, Call, external messaging and media capabilities. Capability false → omit/disable with honest semantics, not clickable counterfeit.

F. Preserve the existing canonical `IslandBehaviorEngine`, `IslandStateManager`, queue/tombstone registry, pager, child-notice tokenized timers and privacy. Do not replace architecture or duplicate timers as part of UI redesign.

## 4. P1 — Implement each of the 32 derived-state recipes

Use the attached 60-row CSV; the subset `FAMILY_ONLY` and `NO_SOURCE` is a **required named checklist**. Do not assign all 32 to a single generic `Column` or `ContentCard` and swap labels. Build reusable **primitives** (icon+metric compact, two-line contextual, call-phase, timer, route/ETA, two-team score, useful action) and state-specific **compositions**.

Families and UX decisions:

- **S0 Minimal and Idle:** Owner Minimal red `108×32` with no content; separate black camera-aware Idle. Idle tap opens genuine Quick Controls.
- **S1 Compact:** intrinsic widths 132–222dp per state; ~36.67dp tall R18.335. Leading glyph/album at START; right numeric/time/status at END. No centered conglomerate. Values have stable tabular width and ellipsize only secondary labels. Album+wave states: single equalizer at trailing edge only during true audio playback.
- **S2 Contextual ~367×86 R43:** meaningful primary/subtitle group, real status, only valid trailing CTA. Exact icon/text START positions, no mass scale or oversized footer.
- **S3 Call phase:** genuine ringing, connected compact, connected expanded, and ended summary. Do not use a post-call duration as incoming UI.
- **S4 Media/navigation special:** distinct music scrubber/transport and navigation maneuver/route hierarchy. No generic message card or color palette substituting navigation.
- **S5 Domain-specific 112–210dp:** delivery milestone, flight gate/route, sport score, transit, ticket, paid plan, system HUD and Quick Controls have their own content layout and priority. No synthetic source data or privileged action.
- **Child Mini-Islands:** preserve newest-first, 1–3 visible, pending-only overflow, tap promotion, independent timers and non-destructive queue handling. Do not hide main pager selection.

For every one of the 32 entries:

1. Define actual semantic content fields and source-availability model.
2. Author a deterministic layout with explicit top/start/end/bottom anchors, measured content priority and action visibility.
3. Define compact/expanded behavior or an explicit reason this state only has one meaningful form.
4. State what happens if icon/text/metric/action/data is missing.
5. Render at 320/360/393/430dp, font scales 1.0/1.5/2.0, LTR/RTL, reduced motion.
6. Mark `FAMILY_DERIVED` / `NO_SOURCE_PRODUCT_DESIGN` and do not claim exact Figma parity.
7. Review on device and iterate until professional quality, not just within outer width/height tolerance.

The owner expects you to make ordinary product design decisions from the approved family rules. **Do not interrupt with repeated one-by-one questions** or default to filler UI.

## 5. Correct the 60-State Gallery so visual QA becomes honest and useful

- Keep the existing real shared production renderer in the Gallery. Do not create a parallel screenshot-only replica.
- Distinguish `DIRECT / FAMILY_ONLY / NO_SOURCE`. Reconcile false DIRECT statuses from actual original assets.
- **DIRECT:** actual reference crop, live image, side-by-side, 50% blended overlay, diff heatmap; correct 1× px/dp scale and typography. Geometry pass AND composition pass AND behavior pass reported separately.
- **FAMILY_ONLY:** label `Derived from <family reference>; not an exact state match`. Show family exemplar at its own native scale, plus the fully rendered derived Island and annotated semantic layout. No invented pixel-match score.
- **NO_SOURCE:** replace giant blank black "No direct Figma asset" with compact honest provenance/inset diagram and current product-design specimen; do not pretend a missing reference is a visual asset. The live component must still be beautifully rendered.
- A green `Actual 366.8×...` metric is only W/H accuracy, never approval of text, spacing, icon, action layout or real-state behavior. Include title/subtitle baseline/anchor and action overlap checks. Hide internal measurements from production presentation.
- Gallery state selection, sidebar chips, long labels and navigation prev/next must not truncate critical state names or obscure the actual sample. Prefer adaptive scrollers and smaller metadata. The gallery is debug, not shipping UI.

## 6. Mandatory interaction rules from prior owner decisions

Preserve the complete approved UX-C01–C15 + PD-001 contract:

- Idle tap → Quick Controls; one compact notification tap → expanded detail; multiple → newest-first 50-item HorizontalPager.
- Keep selected notification key while ordinary arrivals occur; source removal picks adjacent valid key; default Island-only dismissal preserves OS shade; suppress repeat minor updates; meaningful delivery milestone may reappear.
- New ordinary notification while expanded and occupied creates its own Mini-Island below without hijack; newest-first stack; visible adaptive 1–3 plus accurate pending arrival overflow; independent token-correlated timers, touch pause, merge without queue removal; child tap promotes.
- Call preemption, media, timers, urgent navigation, recording and reply-editing priorities are guarded; restore only valid previous content. Do not lose active reply drafts; no actions doubled during transitions.
- Screen/privacy/accessibility safety overrides source-image fidelity. `TYPE_ACCESSIBILITY_OVERLAY` is production; debug app overlay does NOT prove identical touch routing.

Cover the six prior personas: quick glance, deep reader, heavy-notification, multitasking, power user, and privacy/accessibility. Evaluate each no-source state from these perspectives: readability, primary task, interruptibility, gesture safety, missing data, recovery.

## 7. Physical/Jugg visual feedback loop — must execute where authorized

Use the installed `.agents/skills/jugg-android-dev-loop/SKILL.md` and actual device/emulator if authorized. Read the skill first, then use supported Jugg methods to build/deploy, inspect view hierarchy, bounds and touch targets. Use ADB/Android screenshot capture and local image tools for actual pixel/diff comparisons; do not pretend Jugg alone can inspect individual Compose pixels without an image.

For each state:

**Design contract → Compose implementation → build → open Gallery state → capture screenshot → annotate target W/H/R and inner rectangles → compare against source or family pattern → fix → repeat.**

At 393dp/1× direct reference, acceptable shell errors generally ≈±1dp and inner source-grounded anchors ≈±2dp. Derived states have no fake pixel target: acceptance is family geometry, readable content, anchored gravity, appropriate action density, realistic data, user task clarity and no overflow. Real device status bar/cutout/text anti-aliasing differences must be documented. Freeze animations for static captures and separately exercise real motion/gestures.

Run Compose UI tests, JVM tests, debug builds and instrumented UI tests on device when possible. Record actual executed counts and blocks; never report passed when no test ran. Performance must be measured on representative frames during transitions, not from a tiny idle-only sample.

## 8. Acceptance and STOP condition

Complete only when:

1. All 60 enum states have truthful provenance classification, geometry, semantic content, data capability, action and fallback checks.
2. All 32 derived states have **approved coherent family-consistent UI implementation**, not blank placeholders or four-button generic cards.
3. `CallSummaryExpanded` screenshot P0 is fixed and the real ringing/active/ended call lifecycle verified.
4. Real navigation expanded is no longer mapped to `ColorOptions` and empty notification queue never displays invented messages.
5. All DIRECT states stay visually faithful (regression-diff checks); no unintentional reference deviations.
6. Real user gestures/priority/timers/OS notification non-destruction remain intact.
7. Gallery label claims correspond to actual evidence, with no-source states having a meaningful annotated derived design, not a black hole.
8. Automated tests and device evidence exist for 320/360/393/430, 1.0/1.5/2.0 font, RTL, reduced motion, privacy, calls, notification bursts, broken/missing capabilities and short-height layouts.
9. No fake WhatsApp/call system operation, source data or duplicate action dispatch; no overlay unsafe touch interception.
10. No release approval until genuine bound production accessibility-overlay and privacy behavior are validated on an authorized device.

## 9. Required handoff files — do not just write “ALL GATES PASSED”

Create these in `reports/aurora-v6/`:

- `DERIVED_STATE_PRODUCT_DESIGN_DECISIONS.md` — specific rationale and pixel/layout spec for all 32 unreferenced states.
- `SIXTY_STATE_PROVENANCE_AND_VISUAL_AUDIT.csv` — all 60 rows, source reference/family, measured shell/internal offsets, actions, PASS/REWORK/BLOCKED status.
- `CALL_LIFECYCLE_AND_SUMMARY_FIX.md` — ringing, connected, compact, expanded, ended, missed, action tests.
- `PRODUCTION_MAPPER_AND_DATA_SAFETY_FIXES.md` — navigation, fake notification fallback, call routing, permission/capability source.
- `GALLERY_PROVENANCE_AND_VISUAL_QA.md` — direct vs family vs no-source representation; annotated comparisons.
- `DEVICE_AND_JUGG_TEST_EVIDENCE.md` — commands, hardware, screenshots, actual test results, blockers.
- `FINAL_REVIEW.md` — commit SHA, exact changed paths, high-severity open issues, family-by-family progress, explicit verdict.

Store screen evidence in organized `reports/aurora-v6/evidence/<state>/` folders; include source crop where allowed, live render, bounds annotation, difference where direct, and a short rationale for derived states. A polished gallery is not proof of production availability; report independently.

**Do not modify unrelated app modules, do not merge to main or publish a release.** Work on a private review branch from the verified base; commit/push all changes and evidence. Stop for ChatGPT's independent examination only after implementing and validating the work. **No completion claim based only on Gradle success, placeholder screenshots or a self-authored PASS report.**
