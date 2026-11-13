# TASK V6C — AURORA: COMPLETE 60-STATE CONTENT-ADAPTIVE UI REWORK + INDEPENDENT V6 AUDIT REMEDIATION

**Assigned by:** Principal Product Architect & Independent Code/Visual Examiner  
**Assignee:** Gemini or Antigravity acting as Principal Product Designer, Staff Android Jetpack Compose Engineer, Android System Integrations Engineer, Motion Designer, Accessibility Engineer and Independent Visual QA Lead  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Required baseline branch:** `codex/aurora-v6-product-design-review`  
**Exact examined commit:** `6898bdcc1cf5a010ab5624b9d221eaf1e0e8d809`  
**Owner-provided reference folder:** `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/Dynamic Island states reference from figma`  
**Source behavioral authority:** `DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md`, owner-approved UX-C01–C15 and architect-selected independent child timer PD-001/C16, previous V4.3/V5 geometry rules and owner-confirmed Minimal reference.  
**Companion required inputs:** `AURORA_V6_GITHUB_INDEPENDENT_AUDIT_COMBINED.md` and `AURORA_V6C_60_STATE_ADAPTIVE_DESIGN_CONTRACT.csv`.  
**Additional owner evidence:** the **five newly supplied screenshots** showing excess black/empty space, poor optical grouping, oversized Island surfaces and compact/expanded reference patterns.

> **Non-negotiable outcome:** A human should immediately understand the content, trust the actions and see a balanced, naturally sized Island. It must not look like an oversized fixed rectangle populated with a few tiny objects. A green `W×H PASS` label is not acceptance. Correct physical camera/cutout, content alignment, lifecycle behavior and true Android capabilities outrank any arbitrary template dimensions.

**Work on actual source code.** This is NOT a request for a design proposal, another SVG pack, a Figma mockup, an audit-only report, or a mass enum rename. Implement and validate every reachable production state and all 60 Gallery fixtures, without compromising existing app behavior. Do not stop after a few example screens. Commit and push a review branch only; never merge main or ship a release.

---

## 0. Stop any false assumptions before work begins

1. The V6 `FINAL_REVIEW.md` claims all 32 derived states redesigned, 60/60 passed and 60fps without drops. Independent GitHub review does **NOT** substantiate these claims.
2. Direct compare of the V6 60-row audit CSV with `IslandFamilyGeometry.targets`: **33 of 60 targets disagree by >0.5 units; 25 disagree by >=5 units**. See audit attachment. **Source target vs report target must be adjudicated against original reference and actual measured runtime; do not blindly make code match the CSV.**
3. The V6 commit contains **9 newly submitted screenshot assets from only 8 distinct state names**, not visual proof for 60 states. Count tests ≠ visual tests. Reports with fake `bounds="[...]"` excerpts are not raw UIAutomator dumps.
4. Real physical `TYPE_ACCESSIBILITY_OVERLAY` binding, touch pass-through, service recovery and post-call lock-screen privacy remain release blockers without legitimate physical verification. Debug Gallery or debug app overlay cannot prove production.
5. Owner Minimal is a **108×32 horizontal red capsule, radius 15.9018, #DC0917 at 60% opacity, no text or icon**. It is NOT a 38×38 circle or automatically a two-part bubble. Production Idle/cutout silhouette is a different semantic state.
6. Preserve every correct existing V6 change; use tests to detect regression.

### First actions

- Verify current checkout, exact commit and branch; create `codex/aurora-v6c-adaptive-ui-examiner-review` from the V6 branch. Do not start on `main` or old V5.
- Inspect all source images/SVGs and crop real capsule edges (exclude outer source transparent pixels and shadows). Extract source dimensions, font/baseline positions and button/slot bounds where measurable. Do not treat iOS Marketing widgets or full phone screenshots as 367dp caps by default.
- Read all 60 `IslandDemoState` enum cases, main `DynamicIslandPill.kt`, `IslandFamilyGeometry.kt`, `IslandPresentationMapper.kt`, `DynamicNotchParent.kt`, service render path, `ReferenceActivityFamilies.kt`, `CallSummaryIsland.kt`, the call/media/timer/navigation/system/notification specialized renderers, `PlaygroundGallery.kt`, `FigmaReferenceFixtures.kt`, `LayeredMiniIslands.kt`, notification BehaviorEngine and actual platform action handlers. Trace **the real production caller for each state**.
- Open the five newly supplied screenshots at **native resolution**, not the chat thumbnails. Mark where content starts/ends, center of visual mass, distances between content groups, empty slots, cutout interference and whether the reference actually calls for breathing room. List state-specific reasons for emptiness before changing dimensions.
- Produce an initial defect inventory: `state → source availability → current natural content bounds → actual shell bounds → unjustified void/spacing → root cause → selected content fit remedy`.

---

## 1. THE NEW RULE: CONTENT-OWNED LAYOUT, NOT TEMPLATE-OWNED EMPTY SPACE

### 1.1 Global design constitution

- **Intentional whitespace is good; dead empty space is not.** Preserve deliberate source spacing in EXACT/DIRECT reference specimens. Reject random hollow surfaces when data/actions are missing.
- Each screen has **one outer shell**, **one owner of shell padding**, **explicit independent slot anchors**, and **content-measured height**. Do not stack horizontal insets in parent + child + reference wrapper.
- Distinct semantic **START / IDENTITY / OPTIONAL STATUS (END) / OPTIONAL SECONDARY / OPTIONAL ACTIONS / OPTIONAL PROGRESS** slots. Absence of any slot means the slot occupies **zero visible space**. No empty Row/Box/Spacer/Divider representing a missing source field.
- Content hierarchy always: urgent status/identity → primary live metric → one-line supporting context → legitimate action(s). Expand only when each extra region adds user value.
- Keep caption/meta alignments intentional: same left baseline for title and subtitle; one fixed-width END metric for changing time/count/percentage; center icons optically on the line, not just mathematically.
- Compact: top/bottom space visually balanced around 36–37dp capsule, START icon vs END metric separated by flexible space; no waveform near the START icon unless the **particular verified reference** says so.
- Contextual: a 2-line alert generally has leading icon/avatar, aligned text group, optional trailing metric/action. Do not assign 86/112/144/162dp solely based on enum if data only occupies one line.
- Expanded: real header, optional middle content, real bottom controls/progress. Never use `Arrangement.SpaceBetween` to manufacture huge vertical gaps between two small groups.
- A clipped or illegibly squeezed UI is not a fix. The Island should **grow naturally**, switch to compact/summary mode, or use a bounded accessible scrolling region. A short message should not get the same giant card as a long illustrated reply.
- All content and icon fallback states must be styled; if no real source exists, **no production state should present a plausible fabricated live journey, score, ticket or source action**.

### 1.2 Measured geometry policy

Let usable screen width = actual overlay-safe viewport minus real insets, excluding physical cutout unsafe geometry. Measure actual visible elements and reserve tap safety correctly; do not use a global 393→430 scale multiplier.

Use a **single canonical geometry engine**. For derived states, choose the smallest attractive variant containing the actual visible content and valid actions. For EXACT DIRECT specimen comparison, preserve original reference W/H/R with matching fixture data. For real activity content, choose a validated responsive variant. Separate those two modes explicitly so reference parity is not falsely marked FAIL when showing other genuine data.

Conceptual layout procedure (implement using appropriate Compose measurement primitives, not naïve fixed formulas):

```text
START width  = rendered icon/avatar/art + its true surrounding padding
END width    = stable reserved width of real numeric status / button
MIDDLE width = text measured at chosen lines/font + optional inline badge
neededWidth  = START + meaningful gaps + MIDDLE + END + shellInsets
neededHeight = topPadding + max(header height) + optionalRowsHeights
               + only the gaps that separate ACTUALLY VISIBLE rows + bottomPadding
finalWidth   = safeBound(neededWidth, realCutoutEnvelope, referenceFamilyMax)
finalHeight  = safeBound(neededHeight, realCutoutEnvelope, viewportHeight)
```

Do not let an optional action, empty secondary subtitle, `info=null`, absent progress, non-working source or child notice reserve blank geometry. Start with content, then choose the visual variant, then animate the shell **around it**.

### 1.3 Whitespace and margins QA

- Track per-state **occupied content bounds**, **outer shell bounds**, **top/bottom/left/right visible padding**, **inter-line gap**, **inter-region gap** and **visual center of mass** with `onGloballyPositioned` or Compose testing semantics. Do not infer correct gravity from width alone.
- Review every vertical gap disproportionately larger than adjacent intentional spacing (e.g. >1.5–2× the documented family gap) when there is no real separator or visual grouping reason. This is a **review trigger**, not a universal auto-fail: iOS media composition deliberately uses some negative space.
- Pilot trial spacing: compact outer inset ~10–14dp, icon-to-label gap ~6–9dp; expanded side padding ~14–20dp, primary-to-secondary text ~2–5dp, between functional groups ~8–12dp, action gap ~6–8dp. **Reference-specific measured values override these trial numbers**. Keep RTL logical START/END rather than hard-coded LEFT/RIGHT except directional navigation glyph meaning.
- Never place a 36dp visible action next to another if their real hit bounds overlap. Design 48dp safe targets where physically feasible, with overflow menu or stacked actions for narrow/large-text devices. Do not enlarge the *window's touch rectangle* into unrelated foreground app regions for hidden padding.
- If the content doesn't use a giant shell, **shrink the shell, hide the irrelevant row, or choose a compact/contextual presentation**. Do not add filler text, fake progress, fake actions or decorative waves just to fill it.
- All dynamic counters use tabular digits, stable slot width and transitions that do not jump text or move adjacent icons.

### 1.4 Family rules and the direct-reference exception

- `Minimal`: owner 108×32 R15.9018, empty red capsule, only camera-aware hardware adaptations. Separate production Idle black silhouette and detachable companion glyphs.
- `Compact`: ~36.67dp height R18.335 family, **content-sensitive width** and hardware cutout. Music Compact 190dp specimen, Call Compact 152dp specimen, notification compact intrinsic; never scale all to 190dp. No large blank center created by centering content as a group.
- `ContextualAlert`: Atlas direct nominal 367×86 R43 at 393dp for EXACT matched fixtures. SHORT REAL status variations may use smaller contextual designs if no body/actions, but cannot be labeled exact reference match.
- `ActivityExpanded`: different reference heights by purpose (e.g. Music177, Call166, Navigation~185, recording/system alerts as source). Actual content owns the height unless the particular DIRECT reference has deliberately measured fixed geometry for its matching fixture. Extra real content can grow within safe vertical budget.
- `CallSummaryExpanded`: **NO_SOURCE**, existing 367×112 is only the normal demo baseline. If no actions, shorter and elegant; if huge contact name/2x font, taller with safe touch regions. No misleading live green timer.
- `TransportActivity`: **NO_SOURCE**; do not force 148dp if only two meaningful lines. It may require ~92–116dp for a concise alert, and up to ~148dp for true progress corridor + milestone. These are proposed **trial envelopes**, not iOS/Figma measurements. Eliminate `heightIn(min=148.dp)` + `Arrangement.SpaceBetween` inflation for short data.
- `MovieCard`: **NO_SOURCE**; show real poster + ticket detail + working deep link only. A poster 76dp high plus natural insets might need ~100–120dp, not an always-empty 142dp box. If no poster/CTA, layout must reflow compactly and honestly; no fake seat numbers.
- `SubscriptionPricing`: **NO_SOURCE** in-app monetization only; no fake Google Play checkout, no reserved premium promo overlay, no interruption of urgent events.
- Reference-specific `ScreenMirroringAlert`, `MobileDataAlert`, `AirplaneAlert`, `TransitRouteAlert`: **their Figma reference geometry remains valid for their exact source image**. When genuine Android notification contains fewer fields or different actions, choose a separately named adaptive variation with content-derived height; do not silently overwrite the direct specimen for marketing/side-by-side parity.

---

## 2. P0 AUDIT FINDINGS — IMPLEMENT, DON'T EXPLAIN AWAY

### P0-A — Repair exact geometry provenance, all 60

Reconcile actual `IslandFamilyGeometry.targets`, its resolver, canonical render target, Gallery and `reports/aurora-v6/SIXTY_STATE_PROVENANCE_AND_VISUAL_AUDIT.csv`. Known 33 mismatches, 25 >=5. Examples:

| State | Current code target | V6 claimed target |
|---|---|---|
| CallCompact | 152×36.67 | 190×36.7 |
| ActionControlExpanded | 367×210 | 210×44 |
| SilentModeCompact | 152×36.67 | 53.3×36.7 |
| ChargingCompact | 132×36.67 | 190×36.7 |
| SportsExpanded | 367×144 | 367×94 |
| NotificationImage | 367×126 | 367×86 |
| AirplaneAlert | 367×148 | 367×86 |
| ScreenMirroringAlert | 367×144 | 367×86 |
| MobileDataAlert | 367×162 | 367×86 |
| TransitRouteAlert | 367×142 | 367×86 |

Don't blanket-change code to CSV. Establish one **reference/owner measured** geometry table and a **real-data adaptive variant** table. Generate reports from those sources and actual Compose measurements programmatically. Report `Reference`, `Proposed Derived`, `Actual`, `Pass/Fail/Blocked`, `Reason` for every state/variant.

### P0-B — Fix Call Summary privacy immediately

Trace `IslandOverlayService` → `CallSummary.record` → `DynamicIslandPill` → `CallSummaryExpandedContent`. Current service redacts incoming/ongoing contact but passes Call Summary `CallRecord` directly; `LockScreenPrivacyPolicy` lacks call-summary redaction. Add **presentation-only** summary redaction on locked or configured sensitive displays. Remove exposed contact name, full phone number, avatar and unavailable actions/semantics while redacted; never mutate underlying call history. Test status-bar/lock-screen/TalkBack/logging and unlock transition on device, including process restore and source disappearance. This is **release-blocking**.

### P0-C — Prevent fake / no-op action controls

1. `MovieReferenceContent(onOpenTicket = {})` currently displays active-looking View Ticket with default no-op; `DynamicIslandPill` doesn't bind it. Remove/hide/disable in production until an actual ticket-provider action exists. Debug Gallery may show a **clearly labeled, noninteractive preview** action.
2. `SubscriptionReferenceContent(onUpgrade = {}, onDismiss = {})` likewise exposes fake buttons. Wire real Billing plan state and click handlers or show in-app noninteractive state. Never inject sales calls into the system Island.
3. `TransportReferenceContent` hardcodes `Route 24 · Central`, `Market Street`, `Central Station`, `3 min`, etc. Keep sample data only in debug fixtures. `DynamicNotchParent` currently maps expanded navigation to `TransportActivity` without passing `liveActivityInfo`; audit every production host and remove fake transit fallback.
4. In `NavigationIslandExpanded`, unknown maneuver MUST NOT default to `NavigationRight`.
5. `CallSummaryExpandedContent` currently decides `canRedial` from a number containing digits and `canRenderIslandControl()` defaults to `true` when capability set is null. Require actual action-handler/capability confirmation and verify fallback on intent launch failure. `DynamicNotchParent` has `else -> Unit` for some controls; no rendered button may call a no-op through that host.
6. Respect OS role/permission and platform capability for calls, RemoteInput, notifications, recording, DND, mirroring, Bluetooth, timers, focus and media. Never imply a system action succeeded merely because a UI button was pressed.

### P0-D — Actually implement all 32 derived states and test all 60

At V6, 28 DIRECT / 10 FAMILY_ONLY / 22 NO_SOURCE are recorded, but only 9 new PNGs from 8 state names were committed. A CSV row is not UI code; a geometry constant test is not layout evidence. For every one of the 32 derived states, identify actual production Composable, source type, user purpose, unique slot recipe, adaptive geometry, feasible controls, fallback and proof. Change specialized composables as needed (including currently unchanged ones); do not merely update labels or data tables. For 28 DIRECT, re-verify them against their real references and protect previous parity.

### P0-E — Eliminate hardcoded empty rows and over-large minimum heights

Find and audit every use of `heightIn(min = nominal)`, `requiredHeight`, `size`, `fillMaxHeight`, `Arrangement.SpaceBetween`, unnecessary `.weight(1f)`, empty footer/progress, fixed spacer, extra nested padding and text baseline offset in all 60 production renderers. Explain/replace layout-only fixed heights that generate the owner's reported hollow black surfaces. Preserve measured reference geometries **only in exact-fixture parity mode**. When no source row exists, that row must not occupy height. Measure and animate new shell bounds appropriately.

---

## 3. NOTIFICATIONS MUST BE ADAPTABLE TO ANY REAL APP CONTENT

Design the notification view as a real **content model**, not hardcoded WhatsApp/Gmail sample card:

- Payloads: app/source icon, sender/title, body text, multi-line `bigText`, grouped inbox lines, image/picture (if permitted), inline RemoteInput action, messaging group count, priority/milestone, timestamp, progress/activity, unavailable/missing icon, source removed, zero text, Unicode emojis and localized/RTL content.
- Compact never attempts to squeeze arbitrary text into a 36dp capsule; icon + minimum identity/count or short preview only. Stable END count/metric, not center-drifting.
- Transient expanded: concise message identity and readable preview; height based on 1 vs 2 vs 3 meaningful lines; actions included only when source supports them. Short one-line message must not retain blank image/footer/reply-space.
- User-expanded pager: newest-first on entry; preserve selected **notification stable key** across new arrivals, not its numeric page index. Pager region may grow/scroll within safe screen height for custom data. No accidental swipe-to-dismiss, no unread marking, no app launch on normal compact tap.
- Inline reply: real RemoteInput, safe insets/IME and draft, no accidental pager swipe or swallowed incoming call; if keyboard and cutout reduce height, scroll within main panel and defer children.
- Photo/GIF: picture row only when permitted, loaded and non-redacted. Avoid reserving placeholder blank image while loading/failed; use safe modest skeleton only when deliberately timed.
- Delivery milestone: first nearby and major updates can resurface; do not revive for every tiny ETA/stop increment. 12 queued + one meaningful milestone → 13 eligible items after preview without forcing a child under compact main.
- Child Mini-Islands under **user-expanded** main only when policy allows; independent timer generations, newest-first, 1–3 adaptive, pending-only overflow count, touch hold pause, release grace, promotion, safe merge; user-selected main unaffected. Main deliberate collapse visually merges children without deleting queue or Android shade.
- Notification source removal/Island-only dismissal/optional OS cancellation remain three separate operations; × defaults Island-only.
- Privacy before layout: lock/OTP/financial/private redact data, images, notifications and actions/semantics. The redacted view must **shrink to actual safe content**, not leave vacant confidential image/text space.
- Stress fixtures **must include**: 1-character, 20-character, 100-character, 500-character, Arabic/Hindi/Gujarati, emoji/combining marks, Latin LTR and RTL, empty payload, three-line sender/title, chat group, giant image, single/three/zero actions, long action labels, 1/12/25/50 queued, interrupted reply, disconnected source, privacy hidden, font scales 1/1.3/1.5/2.

---

## 4. OWNER-APPROVED PRODUCT BEHAVIOR MUST NOT REGRESS

Keep all prior UX decisions in their correct authority order:

- C01 Idle tap opens useful Quick Controls.
- C02 One compact notification tap opens in-Island detail, not source app.
- C03 Many notifications open newest-first horizontal pager.
- C04 New ordinary notification while reading page 5 does **not** hijack selected key; count updates and child/new cue appears.
- C05 Source deletes selected item → next at that index else previous; pager remains expanded when items remain.
- C06 × default removes **Island queue only**; optional Android shade dismissal only when expressly enabled/capable.
- C07 Last eligible item removed with no active activity → truthful Idle.
- C08+C09 Dismissed items reappear only for meaningful changes/delivery milestones.
- C10 Major resurfaced delivery milestone temporarily previews and remains queued.
- C11–C14 Expanded main supports secondary children, promotion, adaptive visible count, newest-first emergence and shifting; does not disturb main.
- C15 Child touch pauses adaptive timeout; merge is visual, non-destructive.
- C16 / PD-001 independent child timers; new arrivals do not reset old child timers; stale callbacks rejected.

Respect 6 personas: quick glance, focused reader, heavy-notification user, multitasker, power user, privacy/accessibility user. Calls, music, navigation, timer, recording, notifications and privacy have contextual priority. Legitimate incoming call safely preempts with restoration of valid prior state and reply draft, not app privilege fiction.

The UI implementation must be **presentation-only**, driven by existing `IslandBehaviorEngine`, `IslandStateManager`, queue, suppression tombstones and effect/token timers; no parallel UI-only notification policy engine.

---

## 5. IMPLEMENTATION ARCHITECTURE — CHANGE REAL CODE, NOT JUST THE GALLERY

- Build a reusable adaptive slot/geometry layer using normal Compose measurement and `onGloballyPositioned` instrumentation. The renderer should receive semantic slots and actual source capability snapshot, not tell the user to supply fixed widths manually.
- Make `IslandFamilyGeometry` the single canonical geometry logic or introduce a well-tested successor; remove duplicated contradictory targets in Gallery and alternative hosts. Avoid a huge monolithic enum-driven magic-number switch: keep source-specific recipes and verified family tokens.
- **One owner for padding**: root shell controls outer bounds and camera alignment; leaf composes content insets at most once, with explicit subcomponent spacing. Do not stack default material3 minimum spacing without validating.
- Replace `heightIn(min = 148.dp)` and `Arrangement.SpaceBetween` over empty content in derived widgets with intentional `wrapContentHeight`, measured-row placement and content-aware variant selection. Retain upper-bound safe viewport, support large text/landscape, and prevent oscillating geometry during animations or asynchronous image load.
- Handle option transitions robustly: if optional content appears/disappears, animate between independently measured sizes without flashing stale old content, shifting active touch hitbox to a wrong control or accidentally dispatching duplicate action. No large invisible touch rectangle.
- Avoid all fake `onClick = {}` defaults in production-visible composables; if source action absent, do not render the button as actionable. Tests must catch invisible no-ops.
- Fix `IslandPresentationMapper` vs `DynamicNotchParent.mapIslandStateToDemoState` Minimal disagreement with canonical policy. Owner Minimal visual reference and production Idle are separate states with distinct semantics.
- Actual production `TYPE_ACCESSIBILITY_OVERLAY` must be preserved; no debug `TYPE_APPLICATION_OVERLAY` substitution. `WindowManager` touches outside real bounds should pass through only where Android actually allows; transparent gutters inside bounds do not automatically pass through. Legitimate grant required.
- Do not make monetization screens high-priority overlays. No external Apple-only features shown as real Android controls without real integration.

### Specific code paths that must be audited

- `ui/components/DynamicIslandPill.kt` — state cases, measured heights, reference/derived content, notification pager, compact END anchor, shell transitions.
- `ui/components/IslandFamilyGeometry.kt` — true family geometry vs content variants; avoid fixed minima in derived states.
- `ui/components/IslandPresentationMapper.kt` and `DynamicNotchParent.kt` — one real presentation map, valid data propagation and action dispatch; specifically Minimal and navigation.
- `ui/components/ReferenceActivityFamilies.kt` — hardcoded transit/movie/subscription layout, empty callbacks and `Arrangement.SpaceBetween`.
- `ui/components/CallSummaryIsland.kt` — privacy, natural height, status, capability/CTA, accessible 48dp hit bounds.
- `ui/components/live/NavigationIsland.kt` — neutral unknown maneuver; no phantom turn right; no false route info.
- `ui/components/ActionControlIsland.kt`, `PowerStatusIsland.kt`, `RingerModeIsland.kt`, `OngoingCallIsland.kt`, `SystemAlertsIsland.kt`, `MusicPlayerIsland.kt`, notification renderers and any state-specific components — prove all 60 real paths have balanced compositions.
- `service/IslandOverlayService.kt`, `service/IslandStateManager.kt`, `data/model/LockScreenPrivacyPolicy.kt` — privacy/accessibility/call-record and source action/capability delivery.
- `debug/.../PlaygroundGallery.kt` / `FigmaReferenceFixtures.kt` — truthful evidence, same live production renderer, no hidden special UI that production lacks, no pixel-parity label for family-derived states.

---

## 6. TEST THE REAL VISUAL PRODUCT, NOT ENUM COUNTS

### 6.1 Evidence inventory required

Produce **all 60 current-commit renders** at 393dp / font 1 / LTR baseline, correctly cropped to the actual visible Island, with detected outer bounds, corner radius, icon bounds, text baseline/line count, START/END padding, individual group vertical gaps, action bounds, source provenance, observed content type, measurement errors and PASS/FAIL. Additional mandatory variants:

- For **all 32 derived states**: short content / normal content / missing optional content, with source validity documented; baseline device screenshot for each.
- For each family plus high-risk states: 320/360/393/430dp, font 1/1.3/1.5/2, LTR/RTL, landscape/cutout positions and low-height/IME where relevant. Focus 2x text on summaries, multi-actions, notifications, music and transit/flight widgets.
- Direct Figma states: exact specimen screenshot **side-by-side, normalized 50% overlay, pixel difference** with genuine imported source asset, same content, same viewport and animation-settled phase; 1dp shell and ~2dp internal reference tolerance when physically comparable. If mismatch, mark FAIL and fix.
- Derived states: **do not claim pixel-perfect Figma parity** with nonexistent source. Instead submit first-principles design-review scorecard: information utility, whitespace balance, typography, optical gravity, no dead rows, content adaptivity, action validity, accessibility, privacy, motion safety, source proof.
- Real notification scenarios: short/long/image/reply/grouped/financial/private, 12→13 arrivals, 50 queue, selected pager key stability, source removal, inline reply keyboard, child promotion/pause/expiry, calls+music+navigation priorities.
- Source errors: null data, permission revoked, invalid PendingIntent, nonexistent contact, no media session, app force-stopped, service restart and device lock/unlock.

### 6.2 Measurement and calibration tooling

- Detect local Jugg skill and its installed commands from the repository. If available, use Jugg to install/launch/inspect live Android UI hierarchy and click/gesture paths; use ADB screenshots and your own deterministic pixel/semantic-coordinate analyzer for actual image comparison. Never invent Jugg commands or call debug-only tests "production overlay validated".
- Use source images cropped to **visible capsule**; exclude transparent margins/shadows and normalize pixel density and outer frame at equivalent `dp`. Do NOT compare full device screenshot to isolated source crop without calibration.
- Record per-element Compose `LayoutCoordinates` in debug-only QA hooks. Report `shell bounds`, `content bounds`, `text baselines`, `visible action bounds`, `semantic touch rectangles`, `vertical gaps`, `occupied area` and `visual centroid`. Human-verify deliberate negative space; not everything needs high occupancy.
- Add build-time or test-time detector that flags: empty optional rows, huge unowned gaps, text clipping, button overlap, fake callback, wrong source binding, unreadable title, incorrect display state, horizontally centered compact content, duplicate waveform and shell targeting an unrelated enum.
- Unit tests assert state-specific slot/data invariant and geometry **against the SAME authoritative contract**; the report matrix must be generated from actual current code/captures, not copied by hand. Self-verifying tests against a bad constant do not prove reference matching.
- Before/after multi-content screenshots must show **smaller** Island when data is shorter, and appropriately larger Island with long content. Demonstrate live transition between content variants rather than only static screenshots.

### 6.3 Required concrete scenario set

A. Idle with zero items → Quick Controls and no ghost notification.  
B. One plain notification with 20-character body → concise small preview.  
C. Same source 500-character body → expansion + safe clipping/scroll, no oversized blank sides.  
D. Big-picture notification with/without picture and loading failure.  
E. User replying with IME open + a new ordinary notification.  
F. Pager page 5 of 12 + 13th arrival: selected key preserved and count correct.  
G. × default Island only vs optional OS-dismiss setting.  
H. Source notification removed during expanded/promotion animation.  
I. Music compact + ordinary arrival vs user-expanded music + child Mini-Island.  
J. Call ringing → connected compact → expanded → ended summary → valid previous activity restoration.  
K. Call Summary locked sensitive mode: no caller identity/number/action leakage in any visible or accessibility text.  
L. Transport with all fields vs only title/ETA vs no source; compare heights and eliminate empty progress corridor.  
M. MovieCard with genuine poster/action, no poster, no action, no source; no fake ticket booking.  
N. Charging 87% / battery 9%, no source ETA, optional text; no unused rows.  
O. Sports 2 teams, very long team names, short status and zero supported data.  
P. Unknown navigation maneuver: MUST NOT show right-turn.  
Q. No controls for expired remote media/call: no fake tappable button.  
R. Font 2.0, 320dp width, RTL and TalkBack on three adjacent actions; measurable safe action regions.  
S. Main collapse while 1–3 child timers alive; no orphan ghost/stale timeout.  
T. Actual production bound `TYPE_ACCESSIBILITY_OVERLAY` on physical device: touch routing outside rectangle, safe cutout, lock/unlock, shade, service restart.  

Also run the **entire prior product's 64+ approved acceptance scenarios** and existing unit/instrumentation suite, not only the 20 examples above.

### 6.4 Evidence honesty

- Run `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` and instrumentation where possible. Attach raw task output and JUnit XML, explicit counts.
- No `PASS` for screenshot-only cases without action/state interaction evidence; no 60fps claim without meaningful gfxinfo/FrameMetrics/Perfetto tracing with sample size and threshold.
- Device permissions cannot be bypassed. If actual production overlay cannot bind, label production touch/privacy `BLOCKED` and leave release gate unresolved.
- A debug fixture may be labeled DEMO, but that does not justify real production UI emitting fake source data/actions.
- Never fabricate benchmark values, screenshot comparisons or test results. Report unresolved failures as failures, not cosmetic issues.

---

## 7. THE COMPLETE 60-STATE DESIGN CONTRACT (MANDATORY)

The attached `AURORA_V6C_60_STATE_ADAPTIVE_DESIGN_CONTRACT.csv` has **exactly 60 UNIQUE rows** and identifies for EVERY enum: source provenance, previous baseline, slot composition, new adaptive/whitespace rule, source-absence behavior, valid action policy, forbidden UI and acceptance gate. Do not omit, merge or silently downgrade any row.

**Interpretation:** `ReferenceGeometry` is a measured *or explicitly derived* previous baseline, not a universal fixed size. The per-state adaptive rule overrides generic fixed-height template logic for real variable content, unless an EXACT source specimen is being compared. The trial bound values are hypotheses to validate on actual screens, NOT "measured Apple sizes".

Below is a compact index; the CSV contains the full state-specific recipes:

| # | State | Provenance | Previous baseline | Adaptive variant governing rule |
|---:|---|---|---|---|
| 1 | `Idle` | NO_SOURCE | 127×36 R18 | Hardware-cutout-driven silhouette; smallest practical hit-safe width, no forced 127dp when no activity; no hidden 0dp tappable window. |
| 2 | `Minimal` | DIRECT | 108×32 R15.9018 | OWNER REFERENCE: preserve empty red 108×32 R15.9018 at reference scale; only enlarge for physical cutout; never add companion or label. |
| 3 | `MusicCompact` | DIRECT | 190×36.67 R18.335 | Preserve reference 190×36.67 at equivalent content; artwork START, waveform END only while actual media plays; avoid center-floating waveform and duplicated bubble. |
| 4 | `MusicExpanded` | DIRECT | 367×177 R42 | Reference 367×177 at 393dp for matching content; if no seek/prev/next capabilities remove unsupported rows and use measured compact variation, not hollow 177dp; large text may grow. |
| 5 | `CallCompact` | DIRECT | 152×36.67 R18.335 | Reference 152×36.67, one leading connected icon + fixed-width elapsed END; no duration before connected, no fake waveform. |
| 6 | `CallExpanded` | DIRECT | 367×166 R42 | 367×166 reference at normal 393dp; controls depend on REAL telecom capability, 48dp touch regions; if fewer controls, reorganize row rather than fill vacant cells. |
| 7 | `CallSummaryExpanded` | NO_SOURCE | 367×112 R42; grow only when needed | NO_SOURCE: typical 367×112 at reference density; use header+optional action; choose shorter 80–100dp presentation with no actions and grow only for 2x text / multiple valid actions; never stretch with SpaceBetween. |
| 8 | `FaceTimeCallExpanded` | DIRECT | 367×168 R42 | Reference 367×168 for source-defined video call; when video controls unavailable use audio-call family or explicitly disabled read-only status rather than empty 168dp. |
| 9 | `SharedMediaCallExpanded` | DIRECT | 367×172.64 R42 | Reference ~367×172.64 when shared media actually active; remove SharePlay controls without real bridge and reflow height; never show empty media strip. |
| 10 | `OngoingCallBanner` | DIRECT | 367×86 R43 | Reference 367×86 only if caller/status needs 2-line display; timer in END fixed metrics; shorten/compact when only duration is available. |
| 11 | `NotificationCompact` | DIRECT | intrinsic 120–250×36.67 R18.335 | Intrinsic content-fitting width within reference family, ~36.67 height; align icon START, tiny status/count END; reserve cutout and never show full long message in compact. |
| 12 | `NotificationStacked` | FAMILY_ONLY | 189×36.67 R18.335 | Compact icon/count-only preview; width based on actual count digits and cutout; child stack only beneath user-expanded main. |
| 13 | `NotificationWithMusicCompact` | FAMILY_ONLY | 190×36.67 R18.335 + companion 36.7 R18.35 gap≈11 | 190×36.67 main + optional 36.67 bubble; artwork START, ONE active waveform END, whitespace intentionally FLEXIBLE; no waveform next to art or when paused. |
| 14 | `NotificationExpanded` | DIRECT | 367×86–162 R43 (86); R42 (142+) | DIRECT reference 367×86 for short one-line fixture; dynamic height for 2–5 message lines, verified actions, inline reply, picture and pager; shrink empty action area, scroll only after safe height budget. |
| 15 | `SilentModeCompact` | NO_SOURCE | 152×36.67 R18.335 | Derived single status may be shorter than historical 152dp, but respect cutout+text; ~36.67 height; avoid empty trailing block; icon may be glyph-only if accessible spoken text. |
| 16 | `SilentModeExpanded` | DIRECT | 367×86 R43 | Direct 367×86 only with actual action/status at source-equivalent layout; if no action use compact status instead of black unused row. |
| 17 | `RingerModeExpanded` | FAMILY_ONLY | 367×86 R43 | Family-derived: 2-line mode and optional real action, 72–94dp content-driven; avoid constant 86dp if only one glyph. |
| 18 | `VibrateModeCompact` | NO_SOURCE | 172×36.67 R18.335 | Derived status compact intrinsic width, up to reference width only when copy requires; no blank unused trailing flex. |
| 19 | `NormalModeCompact` | NO_SOURCE | 172×36.67 R18.335 | Derived mode compact intrinsic width; 36.67 height; do not force V6 CSV 53.3dp when icon+text will clip. |
| 20 | `RingerVolumeCompact` | NO_SOURCE | 172×36.67 R18.335 | Volume icon START + stable END metric or progress; width derived from actual indicator and cutout, not 172/120 forced; 36.67 height. |
| 21 | `MediaVolumeCompact` | NO_SOURCE | 172×36.67 R18.335 | Similar independent media-volume metric; preserve fixed 0–100 range width, avoid placeholder progress. |
| 22 | `BluetoothConnected` | DIRECT | 196×36.67 R18.335; contextual 366×95 R46 | Direct compact 196×36.67 family; contextual 366×95 only if name+battery visible; avoid reserving battery column when absent. |
| 23 | `BluetoothConnecting` | NO_SOURCE | 196×36.67 R18.335 | Derived connecting label/icon anchored; intrinsic 140–196 width; no fixed delay masquerading as real connection. |
| 24 | `ActionControlExpanded` | NO_SOURCE | 367×210 R42 baseline; content-adaptive | DO NOT use V6 CSV 210×44; actual Quick Controls is multirow 367×~210; dynamically measure available enabled rows, shrink when few, and scroll/collapse when many; no offscreen overlays. |
| 25 | `PremiumExpiryExpanded` | NO_SOURCE | 367×112 R42 proposed | Derived low-priority branded promo: concise 1–2 rows and single CTA only if valid billing action; if no real offer, do not allocate 112dp or show in overlay. |
| 26 | `ChargingCompact` | NO_SOURCE | 132×36.67 R18.335 | Battery 0–100% + bolt + cutout intrinsic; 36.67 height, ~90–132 width unless reference says otherwise; avoid unused 190dp. |
| 27 | `ChargingExpanded` | NO_SOURCE | 367×86 R43 | Battery icon + verified percentage and charge status; 70–94dp depending one/two rows; no synthetic charging speed/ETA, use compact when little content. |
| 28 | `LowBatteryCompact` | NO_SOURCE | 132×36.67 R18.335 | Battery-warning icon + stable percent; intrinsic 90–132 with cutout safety; 36.67 height. |
| 29 | `LowBatteryExpanded` | NO_SOURCE | 367×86 R43 | Warning title+real percent/optional contextual action; 70–94dp, no empty bottom row. |
| 30 | `TimerCompact` | FAMILY_ONLY | 222×36.67 R18.335 | Reference family 222×36.67 upper envelope, but use measured digits/ring/cutout to avoid blank mid-space; tabular numeric width fixed as time changes. |
| 31 | `TimerExpanded` | DIRECT | 367×85.73 R43 | Direct 367×85.73 with real pause/reset if supported; shrink to status-only counterpart when no actions, keep reference at equivalent data. |
| 32 | `DeliveryCompact` | FAMILY_ONLY | 160×36.67 R18.335 | 160×36.67 upper family target; show meaningful ETA only, width fit 1-3 digits and cutout; no stop-count jitter. |
| 33 | `DeliveryExpanded` | FAMILY_ONLY | 367×144 R42 | Family 367×144 at full details; if only milestone+ETA, use measured 88–112dp contextual; 128–158 for route/timeline; no empty footer. |
| 34 | `FlightCompact` | FAMILY_ONLY | 160×36.67 R18.335 | Flight number and one verified gate/time metric, content-fit ~160×36.67; no empty field placeholders. |
| 35 | `FlightExpanded` | FAMILY_ONLY | 367×144 R42 | 367×144 reference only with origin/destination/status rows; 90–114 if concise flight alert, no fabricated gate/time. |
| 36 | `SportsCompact` | NO_SOURCE | 156×36.67 R18.335 | Two abbreviations, stable score, game status, content-fit; no forced 200dp or cricket examples. |
| 37 | `SportsExpanded` | NO_SOURCE | 367×144 R42 | Source target 367×144 but derived scoreboard may fit 90–128; add league/period/timeline only with data; center score numerals. |
| 38 | `NavigationCompact` | FAMILY_ONLY | 156×36.67 R18.335 | Maneuver icon + stable distance right, 36.67 tall and content-fit; numeric units never clip. |
| 39 | `VoiceMemoExpanded` | DIRECT | 367×86 R43 | Direct 367×86 with actual recording state/time; waveform only if source recording; no false Stop action. |
| 40 | `ScreenRecordingExpanded` | DIRECT | 367×86 R43 | Direct 367×86 with recording indicator/time and supported Stop only; zero decorative waveform unless source demands. |
| 41 | `ShortcutExpanded` | DIRECT | 367×85.08 R43 | Reference 367×85.08 with verified action progress/completion; no invented success state; collapse if empty result. |
| 42 | `FocusModeExpanded` | NO_SOURCE | 367×86 R43 | Derived source status + safe end time, 72–90dp depending one/two lines; no fake toggle. |
| 43 | `AirPodsConnected` | DIRECT | 367×86 R43 (atlas) / 366×95 R46 (community) | Direct 367×86 or source 366×95 family only with earbuds/battery; no battery segment if data absent. |
| 44 | `SatelliteConnected` | DIRECT | 367×86 R43 | Direct 367×86 only for actual satellite status; if unsupported Android capability, debug fixture only. |
| 45 | `FindMyAlert` | DIRECT | 367×86 R43 | Direct 367×86 with verified device tracking signal; avoid fake direction/sound buttons. |
| 46 | `MovedToIPhone` | DIRECT | 367×86 R43 | Direct 367×86 Apple-brand transfer not native Android; debug-only unless supported equivalent is explicitly implemented. |
| 47 | `VideoRemoteExpanded` | DIRECT | 367×177 R42 | Direct 367×177 with media thumb, seek/transport only for verified source; shrink if no artwork or controls instead of 177dp empty shell. |
| 48 | `AirDropActivity` | DIRECT | 367×86 R43 | Direct 367×86 Apple-only behavior; Android sharing equivalent requires real event; show progress only if source-backed. |
| 49 | `TimerImage` | NO_SOURCE | 367×86 R43 | Derived time/status layout 72–96dp if short label, grow for valid image only; no unconditional 367×86 blank area. |
| 50 | `NotificationImage` | DIRECT | 367×86–126 R43/R44 by content | DIRECT 367×86–126+ depending real picture; include image only if safe/available; hide empty image region; actions/scroll may increase height. |
| 51 | `CallAvatars` | FAMILY_ONLY | 367×86 R43 / expanded as needed | Ring mode reference 367×86 with callers and supported Answer/Decline; group roster needs a different expanded composition; if controls missing use safe banner, not empty buttons. |
| 52 | `TransportActivity` | NO_SOURCE | 367×148 R42 | NO_SOURCE: header + concise corridor + optional progress. Use 92–116dp for 1–2 meaningful rows, 124–148 only when verified 3rd row; remove SpaceBetween vertical inflation. |
| 53 | `FlightTrackerExpanded` | NO_SOURCE | 367×144 R42 | NO_SOURCE: flight identity and route first, then verified gate/baggage only if present; choose 100–144dp; omit null milestones to avoid gap. |
| 54 | `SubscriptionPricing` | NO_SOURCE | 367×162 R42 proposal; in-app only | NO_SOURCE: in-app-only pricing; 120–162dp with real billing plans; never show fake Upgrade clickable and no blocking overlay ads. |
| 55 | `MovieCard` | NO_SOURCE | 367×142 R42 | NO_SOURCE: poster/session row if real poster, 100–142dp content-fit; remove black placeholder space and unused CTA; do not fake Seat G12. |
| 56 | `ColorOptions` | DIRECT | 367×185.3 R42 | Direct 367×185.3 at matched reference; debug/theme preview only, swatches 36dp aligned and no extra empty panels. |
| 57 | `AirplaneAlert` | DIRECT | 367×148 R42 | DIRECT 367×148 at actual Figma fixture; retain source geometry when exact content; for a shorter Android alert, choose different contextual variant rather than stretching. |
| 58 | `ScreenMirroringAlert` | DIRECT | 367×144 R42 | DIRECT 367×144 for source equivalent; shrink Android read-only active mirroring status if no 2nd row/action; no fake Stop button. |
| 59 | `MobileDataAlert` | DIRECT | 367×162 R42 | DIRECT 367×162 source with real title/body/action hierarchy; Android short warning may use 86–112 only if action and constraints permit, otherwise exact matched ref. |
| 60 | `TransitRouteAlert` | DIRECT | 367×142 R42 | DIRECT 367×142 with real route platform and supported action; shrink contextual variant if only one text line; do not coerce transport data into navigation turn. |


---

## 8. Mandatory delivery artifacts and review protocol

Produce a single coherent V6C review branch, code changes, and files:

1. `reports/aurora-v6c/01_V6_INDEPENDENT_FINDINGS_RESOLUTION.md` — every P0/P1 from this task, before/after code line, test proof and honest residual risk.
2. `reports/aurora-v6c/02_ALL_60_STATE_DESIGN_MATRIX.csv` — automatically generated truthful reference/target/actual/variance, slot geometry, adaptive variants, real source/capability, PASS/FAIL/BLOCKED.
3. `reports/aurora-v6c/03_CONTENT_ADAPTIVE_LAYOUT_SPEC.md` — shared geometry algorithm, individual family tokens and state-specific overrides, camera/insets/animations.
4. `reports/aurora-v6c/04_CUSTOM_NOTIFICATION_STRESS_TESTS.md` — 20+ payload variations, real queue/gesture/reply/source lifecycle evidence and zero artificial payload in production.
5. `reports/aurora-v6c/05_60_STATE_VISUAL_QA.md` — 60 screenshots + 32 derived evidence variants + direct ref overlays/diff + named issues and actual disposition.
6. `reports/aurora-v6c/06_PRIVACY_CAPABILITY_CALL_NAVIGATION.md` — lock summary redaction, action intent availability, navigation no-source policy, call lifecycle and post-call contacts, real device source-signal tests.
7. `reports/aurora-v6c/07_DEVICE_JUGG_OVERLAY_PERFORMANCE.md` — real-device model/API/density, exact command logs, actual bound service identity, gesture/touch test logs and measured frame sample or truthful BLOCKED.
8. `reports/aurora-v6c/08_FINAL_EXAMINER_HANDOFF.md` — precise SHA, commit diff, all Gradle results, key before/after visuals, known limitations, explicit P0 gates.

**Expected completion threshold:** all 60 state rows have coherent implementation and visual QA, all 32 derived states have state-specific content-adaptive UX with evidence, all previous approved UX decisions pass, privacy/capability P0 fixed, no fabricated source data, and no dimension-report contradictions. If platform access limits device validation, do maximum source/test work and mark required platform gates BLOCKED; do not claim release-readiness. No merge to main, no release.

**Your task ends after code implementation, evidence capture and commit/push to a review branch. Stop for independent examination. Do NOT simply reply "all 60 passed".**
