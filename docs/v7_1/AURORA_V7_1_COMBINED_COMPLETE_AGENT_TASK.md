# AURORA V7.1 — COMPLETE NOTIFICATION PAGER IMPLEMENTATION TASK

**This file is the ONE authoritative task to give Gemini / Antigravity.** V7.1 addendum takes precedence over prior V7 where constraints differ.

---

# AURORA V7.1 — Notification Pager Minimum Height, Stable Size Morph & Premium Swipe

**Status:** P0 implementation amendment; must be executed together with the complete Aurora V7 notification pager task.  
**Owner evidence:** Four supplied gallery screenshots (November 13, 2026; gallery state 14/60 `NotificationExpanded`).  
**GitHub baseline independently inspected:** `Harshil258/DYNAMIC_NOTCH_JETPACK`, `codex/aurora-v6d-premium-actions-review`, SHA `19a20c281b219408b4aa55d86f6aa3b4373a5650`.  
**Important:** Screenshots may represent a newer **uncommitted/unpushed local implementation**. Check `git status`, current HEAD and diff first; preserve legitimate newer changes. The confirmed source findings below describe the above GitHub SHA, not unseen local changes.

## Owner's non-negotiable outcome

The expanded multi-notification Island must feel like one continuous, premium interface: 86dp-class reference geometry for appropriate short/contextual alerts, a **real minimum height**, taller when actual content needs more room, and no arbitrary height reset or abrupt vertical bounce as the user swipes between pages. A swipe should end on exactly one page. The island surface, content, indicator and overlay window must agree on the same selected notification and measured height. The selected key must survive incoming notifications and restore correctly after interruptions. The short/long/large-image variants must be designed intentionally rather than abusing one universal fixed card.

### Screenshot-proven failure to reproduce

Gallery `NotificationExpanded` at 393dp viewport and 100% font scale shows:

- `Mamad` plus three lines: **366.8 × 104.8dp** reported, **18.8dp above** the 367 × 86dp source target.
- `Project update` single line: **366.8 × 86.0dp**.
- `Mamad` three lines again: **366.8 × 104.8dp** after switching back.
- The owner reports that **swipe/resize animation is not professional** and the transition does not settle naturally. A static screenshot does **not** establish its exact frame-level failure; capture a device video/frame trace before claiming a specific animation mechanism.
- Gallery reference picture and Android fixture content are not comparable when one side lacks the source thumbnail or uses another title/body; calibrate content and scale first.

### Confirmed implementation-level root causes at baseline SHA

1. `NotificationIsland.kt`, `NotificationIslandExpandedPager()`: `VerticalPager` wraps `Column(fillMaxSize().verticalScroll(...))`. Vertical page gestures and vertical body scroll compete. UX explicitly approved a **horizontal** notification pager.
2. `pageHeights` is keyed only by notification ID. `activePageHeight` reads **`pagerState.currentPage` during motion**, falls back to `86.dp` (or `126.dp` whenever `picturePath != null`), and publishes height through `LaunchedEffect` to a second outer-shell animation. That creates a plausible default→measured size jump and mid-drag changes. Establish with tracing, not guesswork.
3. `DynamicIslandPill.kt` has a separate measured state height, `animateDpAsState` for the outer surface, and a global `isNotificationCardExpanded` flag not keyed to the active notification. Align size ownership and prevent state from leaking between pages.
4. `AppTheme.kt` sets `notificationImageHeight=86.dp` **plus** `notificationContentInset=20.dp` top and bottom: the intrinsic result is at least **126dp**, irreconcilable with a direct Figma **367×86dp** thumbnail layout. Do **not** lower a generic `minHeight` just to clip 40dp; create separate source-measured *thumbnail alert* and genuine *expanded large-media* recipes.
5. `PlaygroundGallery.kt` intentionally passes `referenceMessages.map { it.copy(picturePath = null) }` for `NotificationExpanded`. That invalidates screenshot comparison to Figma `27:992`. Use the exact reference fixture title, message, thumbnail and selected key.
6. Existing instrumented indicator test checks visible `notification_vertical_pager`/indicator but **not** actual swipe settling, interrupted dragging, page-height morphing or action taps. Add real gesture and pixel evidence.

## P0-01 — A real, state-aware minimum-height contract

Implement **one authoritative content-height policy**; names/tokens below are normative behavior, not a demand for a particular class name.

- At the matching 393dp/100%-scale reference viewport, the standard contextual alert has a **minimum visual height of 86dp** and radius **R43**. Never render a shorter expanded notification or a one-frame collapsed/0dp placeholder during a page switch.
- `targetHeight = clamp(max(referenceFamilyMinHeight, measuredRequiredContentHeight, requiredControlAndIndicatorHeight), availableSafeOverlayHeight)`. Measurement includes **actual** typography, leading image, message rows, actions and insets. If required height exceeds safe height, use internal scroll/overflow without clipping actions or extending outside display/window.
- **86dp is a baseline/floor, not a universal max.** A real three-line message may be >86dp if the selected fonts, image or accessibility settings require it. Equally, do not accept an avoidable 104.8dp for the exact direct-source fixture if the underlying design measurements show it should fit 86dp: correct font metrics, thumbnail recipe and insets, then remeasure.
- A *small leading thumbnail contextual alert* must be a distinct measured recipe; do not equate `picturePath != null` with `LargeMedia`. A true large image/media attachment may start at 126dp or content-derived height, but only when the presentation actually requires it. The Figma direct thumbnail must not be coerced to 126dp by the generic large-image rule.
- Optional actions, image slots, subtitles, progress rows and reply composer occupy **zero layout height** when unavailable. No fake reserved rows. Actions and content must not overlap count rail.
- Normal compact is its own 36–37dp family. Do not apply expanded 86dp to compact or 108×32 owner Minimal reference.
- A 48dp accessible action hit target is logical/interactive space, not a license to grow every visual button or intercept transparent overlay gutters.
- Radius and window geometry remain tied to the actual animated shell; preserve R43 at exactly 86dp and family-specific source radius policies for larger heights. No one-frame rectangular clipping.
- Prefer a content layout recipe and measured intrinsic height, not a list of hard-coded per-message heights. Define exact reference-fixture overrides only when true pixel-parity evidence supports them, never arbitrary blank-space fillers.

## P0-02 — One height owner; zero feedback oscillation

A **single presentation coordinator** must own shell target height and be derived from the selected stable notification ID, validated per-notification measured content and current layout constraints. `PagerState` should only own ongoing gesture/animation; it must not independently own business selection or persistent target geometry.

- Cache intrinsic sizes by `(notification stable key, content revision/meaningful text+media signature, available content width, font scale, layout direction, template, action-set version)` and invalidate only affected entries. A same-key text/media/action update must invalidate its cache; a clock tick should not resize unchanged text.
- Premeasure/compose the visible and adjacent pages before an in-flight transition; for larger lists, maintain bounded cache, not all 50 heavyweight page compositions.
- While finger is dragging, **never shrink** the shell to an unknown page's 86dp placeholder. If the incoming adjacent page is taller, provide enough space for visible content with a controlled/premeasured expansion. On return to the original page, keep stable geometry; avoid transient double-shrink/grow.
- **Commit only on a settled page's stable key**, after the fling/snap finishes. Then interpolate the previous stable height → new measured height using one coordinated, interruption-safe size/morph transition. Defer shrink until the destination is measured and selected. A new swipe cancels/re-targets the old transition cleanly.
- No asynchronous `onSizeChanged` → outer target → child remeasurement feedback loop, repeated 86↔105↔86 pulses, stale per-key heights, or redundant height callbacks (<1dp noise). Trace before/after values, keys and event causes.
- `isCardExpanded`, large-picture status, reply state and any detail expansion must be keyed to the active notification's ID or resolved by explicit owner state. No expansion inherited by another page.
- The overlay window must be updated safely/atomically with the actual shell bounds. No detached transparent hitbox, touching the foreground app outside the Island, or off-screen content.

## P0-03 — Correct premium paging gestures and swipe animation

- **HorizontalPager** for moving across notifications; page content can scroll **vertically** when it genuinely exceeds available height. One axis owns each gesture.
- Use Compose's documented `currentPage` for immediate UI proximity, `targetPage` for animations/prefetch, and `settledPage` for final selection commits. Stable page keys must be actual notification IDs, never position-index identity.
- Single slow drag below the threshold must return to origin; a successful gesture must settle fully to exactly one next/previous page with no half-page stuck offsets.
- Fast fling, interrupted swipe, directional reversal, diagonal drags, rapid repeated gestures, and scroll while a notification arrives must never duplicate a selection callback or bounce to the wrong message.
- UX-C04: while the user is reading `msg-5`, a newer ordinary message appears: preserve `msg-5`. Its numeric `k of N` position may change, its ID must not.
- UX-C05: removing selected page chooses next at same index, else previous. Nonselected removal never steals selection. Source update must not create duplicate pages.
- Commit page selection on **settlement** exactly once. Suppress auto-reconciliation during active user drag. Programmatic key correction after queue mutation must not dispatch fake user-selection events.
- Motion must use controlled fling/spring behavior consistent with the Compose dependency in this repo. No competing `scrollToPage`/`animateScrollToPage` coroutines, residual page fractions, overshoot revealing an invalid card, or arbitrary hardcoded jump on release.
- Long body scrolling must not page horizontally accidentally, and horizontal pager swiping must not close/dismiss Android notifications. Swipe-up collapse must remain non-destructive. Long-press, outside tap, reply editor, child promotions and action taps retain correct gesture ownership.
- In reduced-motion mode, use safe, instant settling (not a broken halfway animation); preserve content and selection semantics.

## P0-04 — Fix direct-reference visual parity AND dynamic real-world notifications

- For exact Figma `27:992`, use **`figma-selected` / `Mamad`** with the exact three-line text, actual reference thumbnail, count 3 and the correct page dot. Show live/reference at the same effective pixel-per-dp, same black-mask crop, settled animation phase, density and font scale.
- Do **not** null `picturePath` in the Gallery. It is valid to use a separate text-only fixture for custom-notification testing; never label it DIRECT Figma parity.
- Distinguish thumbnail (crop, measured size, corner radii/insets), app icon (small circular icon) and true media photo. Avoid treating all `picturePath` values as 126dp BigPicture styles.
- The right count/dot rail must be reserved once and be vertically anchored to the current shell; dots should follow the active settled key (bounded window when queue is large), not a temporarily swiped page.
- Make same-family layouts for 1-line and 3-line text, large body, long title, 0–3 genuine actions, group/inbox, navigation/progress/transport, avatar-only and picture. No text clipping or exaggerated blank panels. Use visual style from reference, user legibility from content, never fake source information.
- Buttons/icons come from typed **actual source actions**, not English substring matching, and are only visible with genuinely available capabilities. `RemoteInput` reply only where source permits. Do not silently remove the OS notification with visual collapse.

## P0-05 — Tests and objective evidence; no screenshot-only PASS

Run a focused regression suite with actual Compose user swipe gestures and view geometry capture. At minimum test:

| ID | Scenario | Required result |
|---|---|---|
| H01 | 1 text-only notification | Expanded stable at **≥86dp**; readable, no phantom media/action gap |
| H02 | 3-line Figma `Mamad` fixture | Correct thumbnail/font/insets; reference-sized **86dp** where physically feasible, else explain measured discrepancy; no clip |
| H03 | Short → long → short pages | No intermediate fallback shrink or height oscillation; final per-page correct dimensions |
| H04 | 86dp page → legitimately 112–150dp page | Tightly coordinated shell+content morph; no truncated row during transition |
| H05 | Long → short with partial canceled swipe | Original selection/height remain; no shrink-then-grow glitch |
| H06 | 3 notifications, one slow horizontal drag | Single full-page settle; correct selected key and dot |
| H07 | 3 notifications, 50% drag then reverse | Returns/settles predictably without stuck fraction or false event |
| H08 | Fast fling and 100 consecutive swipes | Zero half-page states, zero wrong key, zero duplicate callbacks |
| H09 | Page 5 of 12, new item inserted while dragging | Stable key when committed, no jump to newest, correct `k of N` |
| H10 | Selected removed mid-transition | Next at position or previous, stable pager and no ghost content |
| H11 | Nonselected removed, same-key text updated | No unwanted selected-page jump; correct size cache invalidation |
| H12 | Actual image/text switch | No 86↔126 classification error based solely on `picturePath` |
| H13 | 0, 1, 3, 12, 25, 50 queue | Correct visual states, indicator counts, stable bounded rendering |
| H14 | Actual Reply/Cancel/Send plus IME | Preserve draft and input focus, no pager gesture hijack |
| H15 | Source action icon, unavailable PendingIntent | No fabricated action; deterministic failure, safe recovery |
| H16 | Incoming call interruption then end | Restore valid prior selection and measured UI without ghost item |
| H17 | 320/360/393/430dp; font 1.0/1.3/1.5/2.0 | Height ≥ family minimum; no clipped text/actions or offscreen controls |
| H18 | RTL, reduced motion, 90° rotation | Correct start/end logic and exact settled ID; no broken spring |
| H19 | Actual production AccessibilityService bound | Window bounds and app touch pass-through **device tested**, else BLOCKED |
| H20 | Gallery versus production same fixture | Same production renderer, correct sample identity and geometry; direct-reference overlay/diff valid |
| H21 | Two consecutive new arrivals during size morph | No restart loop, no page hijack, no stale height applied |
| H22 | Reopen expanded after swipe-up collapse | Queue retained, correct selected stable ID, no fake dismissal |

Record screen videos of **full swipe sequences**, including finger down/midpoint/release/snap/settled state, not only before/after screenshots. Capture timestamped `selectedKey`, `settledPage`, `currentPage`, `isScrollInProgress`, computed min/required/target/measured height and shell animation target for representative runs; redact personal data.

**Pass only when:** All current executable unit/Compose tests pass; the 22 cases above are measured PASS or precisely BLOCKED (physical-service only); at matched reference fixture, inside layout, geometry and icons meet the source contract; 100-swipe stress has zero partial-page locks; no data/privacy regression; no unverified service or FPS claims. Any failed P0 case means REWORK.

## Implementation sequence and deliverables

1. Check working branch, SHA, dirty status and any local V7 changes. Preserve newer local work and cherry-pick only when safe.
2. Trace `IslandBehaviorEngine → IslandStateManager → IslandOverlayService → DynamicIslandPill → NotificationIslandExpandedPager → action registry` and Gallery; diagram selected ID and both height feedback paths.
3. Implement orientation/gesture ownership, stable-key lifecycle and per-notification height management.
4. Implement source-measured reference thumbnail family and compact/action/rail slot consistency.
5. Add proper content-size/morph motion and accessibility/reply/overlay integration.
6. Run JVM tests, Compose device tests and authorized overlay/device scenario recordings; independently inspect actual screenshots and measurements.
7. Produce **`reports/aurora-v71/`** with: `ROOT_CAUSE_AND_CODE_DIFF.md`, `HEIGHT_MEASUREMENT_AND_MORPH.md`, `SWIPE_AND_SELECTED_KEY_INVARIANTS.md`, `FIGMA_27_992_DIRECT_PARITY.md`, `22_SCENARIO_TEST_EVIDENCE.md`, `DEVICE_JUGG_SCREEN_RECORDINGS.md`, `FINAL_HANDOFF.md`, plus useful comparison images/logs. Include code-file+line references and PASS/FAIL/BLOCKED with executed commands and device IDs.
8. Commit and push to a new private review branch. Do not merge to main, release, or claim completion from mocked/synthetic tests alone. Stop for independent examination.

**Do not merely change a constant to `minHeight(86.dp)` or force every pager state to `112.dp`.** Solve the identity, measurement, intrinsic layout, window and motion system that created the defect. Avoid new ad hoc state machines competing with `IslandBehaviorEngine`.


---

# Original V7 implementation contract (still mandatory)

# AURORA V7 — Production Multiple-Notification Pager, Layout, Actions & Lifecycle

**Status:** Mandatory implementation assignment for Gemini / Antigravity / Codex. **No new design questionnaire.**

**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`

**Reviewed latest branch:** `codex/aurora-v6d-premium-actions-review`

**Reviewed commit:** `19a20c281b219408b4aa55d86f6aa3b4373a5650`

**Local source reference folder:**
`/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/Dynamic Island states reference from figma`

**New owner screenshots:** `image(20261113-115911).png` and `image(20261113-120021).png` (user-provided evidence in chat; obtain files from owner/project attachments if needed).

**Prior contracts:** Preserve all owner-approved UX-C01–C15, architect-selected PD-001 independent child timers, the Aurora V4.3/V5/V6/V6C/V6D rules, the 60-state Gallery, privacy and call/media/navigation behavior. **This task supersedes only conflicting notification-pager orientation, gesture arbitration, image-fixture, and page-size rules.**

## 0. Product outcome — what the owner actually requires

Turn the *real* multi-notification Dynamic Island into a professional Android interaction: fluid horizontal paging; no half-settled content; per-page measured adaptive size; imagery and text matching reference where a direct Figma specimen exists; real action icons and safe source-backed actions; stable notification identity and queue; non-destructive gestures; proper overlay touch behavior. The Gallery and production overlay **must share the same renderer and engine semantics**. Do not produce a cosmetic Gallery-only patch or another report that claims success based on `367×86` outer geometry.

The reference's right-side count/dot rail is a visual convention, **not authorization to change the previously approved horizontal paging direction**. Horizontal drag switches notifications. Vertical drag inside long content reads that notification; safe upward collapse gesture is separately arbitrated. Do not mix those three interactions.

## 1. Confirmed GitHub defects (read source before changing)

### P0-A: The pager orientation contradicts product UX

- `app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt`, `NotificationIslandExpandedPager()` uses **`VerticalPager`** (around line 1146 on inspected commit), despite owner-approved horizontal pager.
- Each page contains `Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()))` (around 1159), and a custom `nestedScroll` observer sets `userPaging = true` on **any Y delta** (around 1135). This mixes page navigation, inner reading and selection ownership.
- The existing `NotificationPagerIndicatorInstrumentedTest.kt` expects `notification_vertical_pager` / `notification_vertical_indicator` and does not test a real swipe, settle, or page 5 plus insertion. Replace and expand these tests.

**Fix:** Use `HorizontalPager` with stable source keys and version-compatible controlled fling/snap. Read long content via a per-page vertical scroll; page navigation must never depend on vertical swipe. Do not rename tags without updating tests, semantics and all callers.

### P0-B: Competing state ownership and page-change effects

`NotificationIslandExpandedPager()` has separate `activeIndex`, `selectedId`, `userPaging`, `pagerState.currentPage`, `settledPage`, `snapshotFlow(isScrollInProgress)` and `LaunchedEffect(selectedId, notificationIds)` that can call `scrollToPage(...)`. `onUserInteraction` in `DynamicIslandPill.kt` also invokes a selection callback. `IslandStateManager.selectNotification()` mutates UI state after dispatching `SelectPage` to `IslandBehaviorEngine`.

**Fix:** The behavior engine's **stable `selectedKey` / queue identity** is the authoritative selection. `PagerState` is a presentation/gesture state, never a second persistent selection authority. User selection is **committed exactly once on settled-page identity**, not on mid-gesture `currentPage`, animation frames, or page count updates. Programmatic reconciliation must keep the existing selected key visible after insertions, update, or removal; it must not call a user-selection callback or re-trigger infinite scroll effects. Use a version/token or cause discriminator where necessary. Verify restoration after interruption and re-composition.

### P0-C: The Gallery falsifies image parity

- `app/src/debug/java/ai/emots/kishan_dynamic/playground/FigmaReferenceFixtures.kt` supplies three messages with `picturePath` and specifically selects Figma specimen `27:992`, with a large image.
- `PlaygroundGallery.kt` passes `referenceMessages.map { it.copy(picturePath = null) }` when `selected == NotificationExpanded` (around line 456). This deletes the reference media from the live render.

**Fix:** Preserve the image when comparing against the Figma reference and feed *identical* synthetic title, message, image, count and selected page into both sides. Provide separate gallery toggles: `Exact Reference Fixture`, `Realistic Text-only`, `Large Image`, `Custom Payload Stress`. A demonstration fixture cannot silently rewrite or discard `picturePath`. Never rely on an app-icon-only frame as pixel-parity proof for the image specimen. In production, images must come only from actual source data and safe cached decoding.

### P0-D: Height ownership causes jitter and truncation risk

`NotificationIslandExpandedPager()` keeps page heights in a mutable map, uses default 86dp (126dp with picture), renders a `height(viewportHeight)` viewport, and invokes `onHeightChange` asynchronously. `DynamicIslandPill.kt` stores a measured state height and morphs the outer shell. The screenshot shows 87.6dp and then 86dp for the same nominal 86dp design. This mechanism needs a stable measurement/animation contract rather than a hardcoded minimum or jump during drag.

**Fix:** Content-aware two-phase measurement (or a proven equivalent) keyed by **notification identity + content revision + font scale + width bucket + template**. Each page has intrinsic height. Clamp to real available overlay height considering cutout, window, screen, IME and accessibility. Keep size stable **during** the page drag; animate from previous page height to the new settled page's measured height **after** a valid page commit. No snapping to 86dp, oversized empty surface, rendering offscreen, clipped buttons, feedback loops or stale offscreen-page measurements. A 367×86 exact reference fixture should measure/settle within tolerance at the matching 393dp test viewport. Content-heavy variants may be taller; their dimensions must not claim exact reference parity.

### P1-E: Notification action visuals are text-heuristic-driven

`NotificationActionButton()` in `NotificationIsland.kt` guesses icons, semantics and colors with English substring checks (`Reply`, `Open`, `Cancel`, etc.). This fails on localized/custom labels and cannot prove actual capability. Existing `NotificationActionRegistry` stores source PendingIntents and RemoteInput; use it correctly.

**Fix:** Derive a typed **UI action presentation** from genuine source action metadata/IDs and supported capability, separate from localized display label. Use the V6D `IslandActionSystem` tokens (or consistent shared equivalents). Only display source-backed actions; `Reply` only when a real RemoteInput is available. If there is a real content intent, a valid `Open` affordance may be offered; never invent Mark as Read, Like, Open, Delete or Reply. Do not trigger OS dismissal on a visual collapse. Prevent duplicated taps via in-flight dispatch guards. Display clear enabled/disabled semantics and appropriate icon optical weight; at least 48dp feasible touch areas without unsafe overlay window capture.

### P1-F: Gallery tests are not production proofs

A test checking indicator existence does not prove that horizontal swipes, paging bounds, resizing or notification actions work on a device. Gallery synthetic actions must not be mistaken for production actions.

**Fix:** Add actual Compose gesture tests, engine integration tests and legitimate production-overlay hardware checks. Distinguish *debug renderer success*, *real notification listener success*, and *actual bound TYPE_ACCESSIBILITY_OVERLAY success*.

## 2. One professional notification component system

Implement a reusable renderer with content-specific variants, not 60 copy-pasted card layouts:

1. **Idle (0 items):** compact minimal/Quick Controls on single tap, no synthetic message creation.
2. **Compact (1 eligible):** source icon + single glance label, intrinsic width and height ~36–37dp; tap expands within Island.
3. **Compact (2–50):** newest/current glance with true count. Tap opens horizontally paged expanded view, newest-first upon first opening.
4. **Expanded standard message:** leading artwork/avatar/media (if actually supplied), name/title and two-line intelligent preview, trailing count/indicator rail, and *only* supported actions.
5. **Expanded picture:** measured image box and text columns like the direct Figma `27:992` specimen; actual picture path, correct clipping and body reflow.
6. **Expanded conversation/grouped:** aggregated message lines with sender, count, coherent grouping. Do not duplicate the same logical source as a group summary plus child.
7. **Expanded rich content/long message:** responsive longer text and vertical scroll within selected page, with a visible affordance; horizontal switch independent.
8. **Expanded actionable:** source-backed contextual buttons, sensible primary/secondary weights, no generic grey button row.
9. **Inline reply:** stable page, preserved draft, IME focus/window handling, non-destructive cancel, accurate send-result feedback, disabled horizontal pager while typing unless deliberate navigation affordance exists.
10. **Template-specific ongoing delivery/nav/timer/progress/transport:** meaningful state and metrics, source-confirmed actions, no generic notification card where a richer template is genuinely supported.
11. **Mini-Island arrival:** only when the user owns an occupied expanded main view and policy allows; newest-first child stack, independent tokens, touch-to-promote, queue safe.
12. **Accessibility/Privacy fallback:** sensitive redaction, TalkBack logical actions, reduced motion, RTL, large text, and unavailable-source handling.

All twelve recipes must use shared typography, spacing, radius, iconography, action styling and input policies. Do not distort the exact direct-reference specimen to fit unrelated payloads. For content-changing variants, let size adapt with a clear max viewport/scroll fallback.

## 3. Gesture ownership — deterministic input contract

| Gesture / context | Required result |
|---|---|
| Horizontal drag across expanded card | Move to prior/next notification, settle on a **single full page**, no in-between rest position |
| Fast short horizontal flick | Snap to one valid page; no skip across multiple pages unless a separately documented policy explicitly permits it |
| Slow drag then release before threshold | Either return smoothly to same page or snap to adjacent one based on velocity/distance; never linger at partial offset |
| Vertical drag inside long message | Scroll body only when there is more body to read; do not change selected notification |
| Vertical gesture at top/bottom / outside message | Preserve existing non-destructive collapse semantics using explicit arbitration; never cancel Android notification merely because of a swipe |
| Tap on page action | Invoke exactly that real source action once, never page-swipe or expand simultaneously |
| Reply editing | Protect draft and focus, no accidental paging or collapsing; Back/outside behavior according to approved reply lifecycle |
| New ordinary notification while page 5 selected | Preserve its stable source key and visible content; new item enters queue, count/dots update, optional new child below |
| Source removes selected page | Select item at same index if present, else previous; preserve pager open if items remain |
| Source removes a nonselected page | Keep selected key and content unchanged |
| Child Mini-Island tap | Promote child key, previous selection remains in pager; avoid duplicate items |
| Call preempts pager | Suspend selection/scroll; restore still-valid key after call according to approved conditional restoration policy |
| Multiple concurrent gestures | Single pointer/gesture owner, well-defined touch slop and cancellation |

Use `PagerState.settledPage` / `isScrollInProgress` appropriately; **do not commit user selection from `currentPage` while a drag is still in progress**. Keep a stable `key` for each page. Do not fight `animateScrollToPage` with a concurrent state-sync `scrollToPage`; account for collection insertions/removals during drag and animations. Follow Android's documented pager API for the exact Compose dependency version in the project.

**Do not implement a vertical pager merely to imitate the reference's vertical dots.** Dots may remain vertically arranged. Their state comes from the same selected key, and they need an explicit accessible `k of N` equivalent and non-intercepting hit bounds.

## 4. Resizing and layout requirements

- Measure actual intrinsic content based on **real** data; use content-size constraints, not an unconditional 367×86 for all messages.
- Preserve direct reference shape at reference content, including image size, margin/gutters, app artwork, title/subtitle baseline, trailing count and dots; measure on an identical viewport.
- On a text-only, short message, do not reserve a photo's 103×86 slot or its padding.
- When picture exists, do not silently switch it to a tiny circle just to stay at 86dp.
- Source metadata changes must not cause multi-frame geometry jitter or flash of wrong image.
- If a page is measured offscreen, cache safely; remove stale cache when identity/revision or window constraints change.
- The first page display must not grow/shrink through placeholder sizes after a few frames; stage premeasurement or a stable first-size estimate.
- When selected page changes, animate shell height after settled selection (not during finger tracking), with interruption-safe spring, bounded duration and reduced-motion snap alternative.
- Excessive content scrolls **inside** the page; action buttons remain reachable, avoid cutoff and overdraw.
- Respect 320/360/393/430dp widths and font scales 1.0/1.3/1.5/2.0, RTL, landscape, and camera-left/center/right cutout positions.
- Reserve right-side count rail only when it is visible; avoid a permanent huge blank gutter at font scale or no-indicator case.
- Child stack and parent window resize together while not creating invisible touch-capturing regions.
- Never claim that a black screen behind the Island is actual app UI; the Gallery stage must render each specimen at its natural content size.

## 5. Real actions and lifecycle

Keep `NotificationActionRegistry`, `NotificationReducer`, `IslandBehaviorEngine`, `IslandStateManager` and production `IslandOverlayService` as canonical. Do not introduce new notification queues, timers or hidden indexes inside the Compose pager.

- Notification IDs are stable keys; `activeIndex` is derived/display-only.
- Cap queue at 50 eligible notifications; never lose the selected page while reordering under capacity.
- Preserve group-summary deduplication and meaningful-update tombstones.
- `×` defaults to **Island-only** dismissal, retaining the Android shade; optional OS dismiss is explicitly user-configured and permission/capability-gated.
- `onActionSelected` must map to the real PendingIntent action `notificationId + actionId`; revalidate action existence at activation time.
- Reply needs a valid RemoteInput and must keep draft if dispatch fails or source disappears.
- Do not mark Android notifications read simply because a page was visible.
- If notification source removes selected item during scroll, finish on an eligible nearest item; never show a ghost card.
- DND/privacy and priority policies suppress/alter preview appropriately, but do not corrupt the canonical queue.
- Mini-Island visual expiry doesn't remove source notification, and each child has its own safe timer generation.
- Treat production accessibility service binding separately from a debug TYPE_APPLICATION_OVERLAY test; never bypass Android permissions.

## 6. Pixel-parity and Gallery corrections

The user's two screenshots show a 367×86 test card. It displays an icon-only circle and one- or two-line text, while the direct Figma fixture shows a large square image beside multi-line text. This is **not** a valid pixel match even if outer width/height are green.

1. Remove the special-case `picturePath = null` in `PlaygroundGallery.kt` for direct `NotificationExpanded` image-reference comparisons.
2. Provide a **literal exact-fixture mode** for Figma `27:992`: same selected notification (fixture ID `figma-selected`), same title `Mamad`, same message, same thumbnail, same count and dots. Ensure Gallery's `notificationActiveIndex = 1` actually settles on `figma-selected` when first shown.
3. Add separate **text-only/ordinary message** and **custom payload** modes. These are professional derived designs, not exact Figma image matches.
4. Compare normalized screenshots at matched density/scaling/animation settled phase: crop outer Island only; show reference, live, 50/50 overlay and difference heatmap.
5. Measure and record shell bounds, text baselines, image region bounds, left/right optical margins, icon size, rail inset, bottom padding, actions and clipped pixels. Pixel mismatch can't be waved away by a green shell metric.
6. Label reports `DIRECT`, `FAMILY_ONLY` or `NO_SOURCE` honestly. Do not label synthetic fixture behavior as production success.
7. Add Gallery buttons to inject 1, 3, 12, 25, 50 notifications and simulate additions/removal, **using the same behavior engine and pager renderer**. No alternate demo-only paging logic.
8. Remove unrelated Gallery card/button layout clutter that obscures the Island while reviewing; allow an immersive isolated preview and a direct comparison mode.

## 7. Required implementation sequence

### Phase A — Baseline and tracing

Identify every call path from `IslandNotificationListener` → reducer → behavior engine → manager `StateFlow` → `IslandOverlayService` → `DynamicIslandPill` → `NotificationIslandExpandedPager` → source action dispatch. Document the exact selected-key, `activeIndex`, page-state and height feedback loops. Capture baseline device video for the owner's failure (partial scroll and wrong content).

### Phase B — Pager architecture fix (P0)

Replace vertical page navigation with horizontal paging. Separate vertical body scrolling. Ensure true single-gesture ownership, key-stable selection, full-page snap/settle, no duplicate callbacks and safe effects around insertion/removal and recomposition. No `onNotificationSelect` while only the page fraction is changing.

### Phase C — Content-size architecture (P0)

Implement per-key measured content/cache with valid invalidation and committed-page height transitions. Correct scroll/body and action constraints. Integrate with the single outer Island shell morph and overlay window size. No oscillation or unexpected partial card.

### Phase D — Direct reference & action system (P0/P1)

Restore picture fixture, align reference artwork, typography and indicator. Build proper action semantic icons and capability-backed rows for all relevant templates. No action-label English parsing as final semantic source.

### Phase E — Regression across other activities

Preserve 60-state Gallery contracts and V6D primary/secondary/destructive action styles. Test music compact/expanded during notifications, calls over pager/reply, navigation priority, child stack, touch safety and return to correct presentation.

### Phase F — Device/Jugg visual and functional verification

Install debug build on authorized device. Use project's Jugg skill for UI hierarchy/view inspection where applicable; obtain Android screenshot capture/recordings through supported device tooling. Capture before/after swipe sequences and real system notification tests. Do not claim Jugg itself implements pixel-diff if it does not.

## 8. Required executable test matrix (minimum)

All the following must be run or truthfully BLOCKED:

| ID | Scenario | Critical invariant |
|---|---|---|
| N01 | 0 items, idle tap | Quick Controls; no fabricated source |
| N02 | 1 notification, tap | One correctly-sized detailed card |
| N03 | 3 notifications, initial display | Newest key; correct count and indicators |
| N04 | 3 notifications, horizontal swipe left/right | Exactly one valid settled page, no midway stop |
| N05 | Short slow drag with release | Returns or settles predictably; no partial page |
| N06 | Rapid repeated flings | No state lag, skipped keys, stuck half-offset or duplicate selection events |
| N07 | Long text, vertical scroll | Reads text without changing notification page |
| N08 | Diagonal drag and opposite-direction drag | Correct touch owner; no accidental dismiss |
| N09 | Page 5 of 12 + new item | Same selected stable key and content; count becomes 13 |
| N10 | Page 5 + two arrivals during drag | One final valid settled selection; no jump/restart |
| N11 | Remove selected item | Next at index else previous; expanded remains if items exist |
| N12 | Remove nonselected item | Selected key unchanged |
| N13 | Remove last item | Idle; no ghost dots/timers |
| N14 | Same source update | In-place stable identity unless meaningful resurfacing policy allows otherwise |
| N15 | 50-item queue with rapid inserts/removals | Capacity, selected key, no OOB/crash, smooth lazy rendering |
| N16 | Direct Figma picture fixture | Image remains; same selected ID, same count, measured parity |
| N17 | Text-only 1 line | No phantom image gap or forced large height |
| N18 | Long sender + 5-line body | Readable, no clipping and safe scroll |
| N19 | Picture + actions | Image fit, action reachable, measured expanded height |
| N20 | Grouped WhatsApp/Gmail | No duplicate summary/child; sensible sender hierarchy |
| N21 | Real source Reply action | Open IME; draft survives minor arrivals; sends exactly once |
| N22 | No RemoteInput on notification | No fake Reply action |
| N23 | Valid notification source action | Correct PendingIntent dispatched once |
| N24 | Label translated/customized | Correct semantics, icon and color unaffected by language |
| N25 | Island-only dismiss | Android shade unaffected by default |
| N26 | Optional OS dismiss unavailable | No OS cancellation; truthful UI feedback |
| N27 | Music expanded + new ordinary notification | Main unchanged; child appears; timer independent |
| N28 | Child tapped while pager active | Child promoted, prior main preserved in queue |
| N29 | Call interrupts page 5 + restore | Valid selected key restored conditionally |
| N30 | Reply editor + incoming call | Draft preserved, safe call priority |
| N31 | Locked/sensitive notification | Proper content redaction |
| N32 | Font scale 1.3/1.5/2.0 and 320dp | No inaccessible controls/clipped content |
| N33 | RTL horizontal paging | Semantically correct direction and accessible controls |
| N34 | Rotation/window resize mid-swipe | Consistent selected key; no crashes/gutters |
| N35 | Touch outside genuine overlay window | Underlying app receives touches; no invisible interception |
| N36 | Overlay service unbind/rebind | No duplicate window, stale pager, or phantom notifications |
| N37 | Test selected gallery fixture index 1 | `figma-selected` visible and indicator correct |
| N38 | Repeat 100 swipes in device loop | 0 stuck half-page offsets; 0 duplicate callbacks; correct keys |

Add tests for real per-template navigation, transfer, timer, media and notification Image cases as implementation discovers genuine capabilities. Test with owner-approved synthetic payloads in Playground; real source data in production must not be fabricated.

## 9. Visual QA and numeric gates

- On matching 393dp reference viewport with same fixture: outer shell within ±1dp; important image/icon bounds and text baselines within ~2dp where cross-platform font rendering makes it feasible. Otherwise report measured residuals rather than declaring PASS.
- On every completed pager drag, final page **offset settles at zero / within expected Compose snap epsilon**; do not accept a visually half-visible next card.
- No more than one source selection action per settled user page transition; reconciliation/initial mount must not be counted as user selection.
- State count/dot/`k of N` use the authoritative queue/selected key and update after insertion, removal and page commit.
- No viewport height oscillation from asynchronous page measurement. At rest and identical content, geometry must be stable. During transition, animation remains bounded and predictable.
- Short-text derived notifications have no big unused black region; picture/complex content can legitimately be taller.
- Enabled actions look enabled; icon weight and alignment follow V6D shared design system; source capability always wins over cosmetic expectations.
- No accidental Android-shade dismissal, ghost card, lost user-owned page, lost reply draft, dead action, or privacy leak.
- Performance measurement must include sample count, device, jank, frame durations and the actual swipe/resize scenario. Don't claim 60/120fps from tiny unrelated samples.

## 10. Evidence package + completion protocol

Create a dedicated **new review branch** from latest V6D (do not force-push previous review work). After code changes, run `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, real Compose instrumentation on authorized device and at least one real-notification flow when permissions are legitimately granted.

Reports to commit:
- `reports/aurora-v7/01_PAGER_ROOT_CAUSE_AND_EVENT_FLOW.md`
- `reports/aurora-v7/02_HORIZONTAL_GESTURE_AND_SELECTED_KEY_CONTRACT.md`
- `reports/aurora-v7/03_DYNAMIC_PAGE_GEOMETRY_AND_OVERLAY_SAFETY.md`
- `reports/aurora-v7/04_FIGMA_REFERENCE_AND_CUSTOM_PAYLOAD_PARITY.md`
- `reports/aurora-v7/05_REAL_NOTIFICATION_ACTION_AND_PRIVACY.md`
- `reports/aurora-v7/06_EXECUTED_DEVICE_AND_REGRESSION_RESULTS.md`
- `reports/aurora-v7/07_FINAL_EXAMINER_HANDOFF.md`

Attach at least: baseline and after screen recordings of real pager swipes; screenshots of all major page styles; pixel-aligned `27:992` comparison; 0/1/3/12/25/50-item test logs; selected-key transition traces; action dispatch traces (non-PII); actual measured inner/outer bounds; frame stats; exact commit SHA.

Separate `PASS CODE`, `PASS JVM`, `PASS COMPOSE DEVICE`, `PASS REAL NOTIFICATION`, `PASS PRODUCTION ACCESSIBILITY OVERLAY`, `BLOCKED`, `FAIL`. **If production service is not legitimately bound, do not call it production-pass.**

No new questions about owner UX choices. Use documented decisions. If evidence is unavailable, implement best safe version, mark the exact missing acceptance gate BLOCKED, and proceed with other work. Do not stop after an audit, superficial test changes, compiling or moving a few dots.

**STOP after implementation + executed verification. No main-branch merge, no public release, no unapproved permission bypass.**
