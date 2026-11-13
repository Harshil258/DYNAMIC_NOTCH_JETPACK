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
