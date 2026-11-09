# AURORA — Dynamic Island Ruleset for the Jetpack Compose AI Agent

**Version:** 4.3 — 9 November 2026 (architect-reviewed implementation baseline)
**Supersedes:** V4.2, V4.1, V4.0 of this file and the V3 Figma-oriented contract. Original reference measurements are preserved; implementation corrections and source-mapped geometry are documented in §28–§30.
**Scope:** This document is the authoritative **Compose visual/layout/interaction specification** for the Aurora Dynamic Island on Android. It does **not** replace the production behavior engine (§3.2), the production overlay window contract (§3.1), or owner-approved behavior decisions (§11). Where this document and an approved behavior decision disagree, the hierarchy in §0.2 wins. Every duration quoted here is an architect-selected trial default collected in §12.1 — none is an SVG measurement and none is an Apple specification.
**Document purpose:** This file specifies presentation/interaction targets. The separate authorized Codex implementation task may modify Android source code and tests; it must preserve the actual reference files and must not overwrite original Figma/SVG assets. The document alone does not authorize release.

---

## 0. How to use this document

### 0.1 Rule classification legend
Every normative statement carries one of these labels. Do not treat a proposed value as measured, or a measured value as owner-approved.

| Label | Meaning |
|---|---|
| **[OWNER]** | Explicit owner-approved decision or later owner correction. Highest product authority. |
| **[MEASURED]** | Extracted from owner/community reference files with a layout-engine probe (exact bbox/radius/fill). Visual truth for looks. |
| **[ARCH]** | Architect-selected behavioral default from the Aurora Master Product Behavior Specification or reviewed baseline. |
| **[DERIVED]** | Implementation constraint derived from measured geometry or platform facts (e.g. width caps, slot gravity). |
| **[PROPOSED]** | Implementation value without measurement evidence (springs, scaling policy, some radii, some type sizes). Must be validated on rendered frames/devices. |
| **[PLATFORM]** | Platform/OEM-dependent or currently unverified. Requires device evidence before claim. |

### 0.2 Source-of-truth hierarchy
1. **[OWNER]** Explicit owner-approved UX decisions and later corrections (UX-C01–C15, the owner-confirmed Minimal capsule specimen, rejection of the 39/108-state packs, the no-cricket constraint).
2. **[ARCH]** Aurora Master Product Behavior Specification and architect-selected behavior defaults — including **PD-001** (independent child timers), **PD-005** (deliberate collapse retires children), **PD-026** (compact-media preview policy), queue caps, every trial timing (§12.1), priority order, bounded restoration (§15.2) and persona defaults. **UX-C16 is architect-selected PD-001, not an independently owner-approved choice**: its behavior stands unchanged, its authority label is [ARCH].
3. **[PLATFORM]** Existing production Android architecture and verified platform constraints (`IslandOverlayService`, accessibility overlay, behavior engine, notification access).
4. **[MEASURED]** Owner-supplied reference SVG/PNG geometry.
5. **[MEASURED]** Other Figma/community reference examples.
6. **[PROPOSED]** Implementation values where no measurement or decision exists.

V4.3 governs **how each Island looks and how each presentation behaves at the Compose layer**. It must never silently override items 1–3, and never invent platform privileges.

### 0.3 Hard rules (violating any = rejected output)
1. The island is a semantic slot system (§4), not a black rectangle with text in it. No absolute `.offset()` layout inside a surface except the cutout overlay and intentional decorative overlays.
2. The camera cutout is real hardware (§5). Read real insets; never draw a fake hole; never force a surface smaller than the physical cutout.
3. Compact widths are intrinsic and capped by available width (**[DERIVED]**); never stretch a compact pill to expanded width.
4. Text is measured before the surface animates (§19); anchors never teleport.
5. No invented actions (**[OWNER]**): a control exists only when its real capability exists (§17). Red is never generic decoration (§8.2).
6. Real `Text` and vector icons only; no outlined-path text, no emoji glyphs (**[OWNER]**).
7. Essential numerals never ellipsize; titles/subtitles ellipsize at end (**[ARCH]**).
8. Accessibility is structural (§20): merged semantics, real roles, no duplicate announcements, ≥48 dp hit targets where feasible without unsafe overlay interception.
9. The rejected 39/108-state SVG packs remain rejected as a design system (**[OWNER]**). Design coverage comes from the real 60-state enum (§16) plus approved interaction variants — the packs are not 108 production states.
10. Presentation never mutates canonical state: the Compose layer renders above `IslandBehaviorEngine` (§3.2); no competing state machine, no OS-notification side effects from animations (§19.4, §24).
11. Pager identity is the **selected notification key**, never its numeric page index (§13). A new arrival may legitimately shift the number (5 → 6) while the same key stays selected and the viewport does not jump.
12. Deliberate Main collapse retires visible children and their timer callbacks (**PD-005**, §14). No child draws underneath a now-compact surface; a stale callback never re-expands the Island or changes the selected key.
13. A legitimate ringing call is **never** silently hidden behind an inline-reply editor (§15.3). Editor ownership protects the draft and the interaction, it does not suppress call presentation.

### 0.4 Evidence and timing authority
- **Timing:** the **behavioral lifetimes** (previews, child lifetimes, touch grace, restoration windows) are **[ARCH] trial defaults** collected in §12.1; **visual spring/fade durations** are separate **[PROPOSED] trial motion values** in §19.1. Neither is measured from an SVG or guaranteed Apple timing. Engine policy owns behavior timers; the animation layer owns only visual transitions and never starts a competing notification-expiry timer.
- **Verification:** implementation evidence and verification evidence are different claims. The six-tier ladder in §16.1 governs every status label in this document. Device-specific T4 instrumentation evidence exists for a limited subset (Task 05/06 reports; 14 test passes reported), but **no per-state blanket T4 inference** is permitted. Genuine T5 production accessibility-overlay verification is still BLOCKED and T6 release readiness has not been established.

---

## 1. Evidence inventory

| Source | Classification | Proves |
|---|---|---|
| Owner Minimal SVG, viewBox `0 0 108 32` | **[OWNER]** | Minimal capsule specimen: 108×32, drawn silhouette height 31.8037, radius ≈15.9018, fill `#DC0917` fill-opacity 0.6, **no text, no icon** |
| `Dynamic Island/Minimal.svg` | **[MEASURED]** | 25 minimal glyph tokens / companion indicators: 38×38 r19 black circles, one semantic glyph each (§9.A) |
| `Dynamic Island/Compact.svg` | **[MEASURED]** | 11 contextual-compact specimens 367×86 r43 with real slot content (§9.B) |
| `Dynamic Island/Expanded.svg` | **[MEASURED]** | 10 expanded specimens h=142–185.36 r42; CTA pills 335×43 r21.5 and 161.5×43 pairs (§9.C–F) |
| `{Minimal,Compact,Expanded} Presentation.svg` | **[MEASURED]** | Cutout↔silhouette coupling per state on 430 and 393 canvases (§5) |
| `Components.svg` | **[MEASURED]** | Canonical slot diagram: Leading/Center/Trailing/Bottom, cutout overlay, 6.5u content inset (§4) |
| `Prototype.svg` | **[MEASURED]** | Android adaptation: left-punch coupling, detached companion bubble, two-line large compact 408×96 (§5, §9.B) |
| Figma community pack (430-wide) | **[MEASURED]** | 3-state progression sheets: idle pill 127×36 r18; compact pills 120–210×36 r18; expanded cards 366×{80,95,126,208} r40–46 (§9.H) |
| `Component 2/3/5.svg` | **[MEASURED]** | Waveform glyph component, 3 palettes × 2 sizes (§10) |
| `Group 3.png` | **[OWNER]** | Expanded media (Selfless / The New Abnormal) + dashboard-scale stock widget (ChainX) |
| `Group 4.png` | **[OWNER]** | Compact media in status bar + dashboard-scale Screen Time widget |
| `Group 5.png` | **[OWNER]** | Compact stock island + dashboard-scale flight / stock / fitness widgets with dual CTAs |
| Production code (reviewed baseline) | **[PLATFORM]** | `IslandOverlayService : AccessibilityService`, `TYPE_ACCESSIBILITY_OVERLAY`, `FLAG_NOT_TOUCH_MODAL`, `FLAG_WATCH_OUTSIDE_TOUCH`, `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`, `IslandBehaviorEngine`, `IslandStateManager`, `NotificationReducer`, `IslandSuppressionTombstoneRegistry`, canonical queue (max 50), `StateFlow` presentation, preview/child timers with generation tokens, Playground integration, horizontal pager, layered Mini-Island renderer, `IslandDemoState` (60 states) |

**Units and reference fidelity [DERIVED]:** The SVG figures are reference design units (`u`) from **different** 393u/430u canvases, not one universal physical `dp` calibration. Do **not** apply `screenWidthDp/430` to 393-canvas specimens: it would wrongly shrink the atlas 367u surface to ~335dp on its own 393dp canvas. Each geometry family must retain its own `referenceCanvasWidth`; see §28.1 for a correct baseline-preserving resolver. Never scale font `sp`, stroke weights or effective touch targets just to force content into a smaller canvas.

---

## 2. Surface taxonomy (corrected)

Five distinct objects. Never conflate them.

| # | Object | Geometry | Classification | Content model |
|---|---|---|---|---|
| 1 | **Owner-confirmed Minimal capsule** | 108×32; drawn height 31.8037; r≈15.9018 | **[OWNER]** — geometry **and** fill `#DC0917` @0.6 are owner-confirmed reference truth. Do not downgrade the reference colour into an unapproved guess; only its hardware adaptation (cutout union, contrast, ship-mode rendering) is **[DERIVED]** | **None.** No text, no icon. Never invent content, never substitute the 38×38 glyph-token family, and never describe this reference geometry as an already-shipped runtime implementation (§16.1) |
| 2 | **Idle camera-absorbing silhouette** | 127×36 r18 centered, camera hole 18–20u dia inside at right-of-center (community); 126×36.67 r18.34 cutout footprint (presentation) | **[MEASURED]** | Empty silhouette; tap → Quick Controls (UX-C01) |
| 3 | **Minimal glyph token** | 38×38 r19 black circle + one 16–24u glyph | **[MEASURED]** | Reusable glyph/companion indicator vocabulary (§9.A) |
| 4 | **Detached companion bubble** | 36.7u dia circle, gap 11u from main pill edge, same top anchor | **[MEASURED]** | Secondary concurrent activity (one glyph token or waveform) |
| 5 | **Compact activity/notification pill** | h 36–37 (r18) or contextual 86 (r43); widths 120–250 intrinsic; large-compact 408×96 | **[MEASURED]** | Full slot recipes (§9.B) |

**Cutout adaptation rule [DERIVED]:** when the physical cutout is larger than the 108×32 reference (or larger than 36u high), the visible surface = union(real cutout bounds, content bounds) and never smaller than the cutout; the 108×32 specimen remains the ideal/cutout-free design reference. Corner radius follows the family lookup (§6.2), clamped so the silhouette stays pill-continuous around the cutout.

---

## 3. Production architecture compatibility

### 3.1 Overlay window contract **[PLATFORM]**
Production uses, and V4.3 preserves:
- `IslandOverlayService : AccessibilityService` with a genuine service binding (user-enabled in Accessibility settings; the window exists only while the service is bound).
- `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY`.
- `FLAG_NOT_TOUCH_MODAL` + `FLAG_WATCH_OUTSIDE_TOUCH`.
- Existing cutout placement/resizing logic; `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` in the reviewed baseline.

Rules:
- **Do not mandate `TYPE_APPLICATION_OVERLAY` in production.** An optional debug/testing surface using `TYPE_APPLICATION_OVERLAY` may exist, clearly labeled non-production, with different permissions (overlay permission vs accessibility enablement) and different touch behavior.
- **Do not switch cutout mode to `ALWAYS` unconditionally.** `ALWAYS` only after per-device/API verification that content is never painted into unsafe cutout regions; until then keep the reviewed baseline.
- **Touch pass-through is not assumed, and the three touch classes are not equivalent.** (a) *Outside the overlay window rectangle* — reaches the app below only where `FLAG_NOT_TOUCH_MODAL` is honored on that device/API; (b) *inside the window rectangle but in a transparent gutter* (space between the visible surface and the window bounds) — **intercepted by default** unless the window is resized to the visible surface or a proven touch-region mechanism excludes it; (c) *on an actual interactive Island surface* — handled by the Island. Never claim all three pass through automatically. Measure the real window bounds and validate (a) and (b) **separately** on hardware (§22 cases 29, 30, 61).
- **Production `TYPE_ACCESSIBILITY_OVERLAY` testing remains unverified in the reviewed baseline.** No enum, renderer or unit test may be converted into a tier 4–6 verification claim (§16.1).
- **Hardware validation status: BLOCKED.** The production accessibility service was unbound during earlier acceptance testing; final on-device touch/pass-through validation remains BLOCKED until evidence exists. No PASS claim may cite this document alone.
- Document overlay teardown/restart paths (service stop, permission revocation, settings toggle) and OEM restrictions (background-start limits, accessibility-service kill policies, battery-saver overlay limits) as **[PLATFORM]** test obligations (§22 cases 25, 26, 40).

### 3.2 Behavior engine ownership **[PLATFORM]/[ARCH]**
The project already owns behavior in: `IslandBehaviorEngine`, `IslandStateManager`, `NotificationReducer`, `IslandSuppressionTombstoneRegistry`, stable notification-key selection, the canonical notification queue (max **50** eligible), `StateFlow`-based presentation, preview/child timers with generation-token correlation, Playground behavior-engine integration, the horizontal notification pager, and the layered Mini-Island renderer. **Actual reviewed observable type:** `IslandStateManager.behaviorPresentation: StateFlow<IslandBehaviorState>`. `IslandPresentationState` is a suggested presentation-only DTO, **not** an existing engine type.

- The semantic slots and visual families in this document are a **presentation layer above** that engine. Compose consumes `IslandStateManager.behaviorPresentation: StateFlow<IslandBehaviorState>` (or a read-only, explicit and tested projection of it). The engine does **not** currently expose a class literally named `IslandPresentationState`. The renderer does not compute queue order, suppression, promotion, or timer expiry itself.
- **No competing state machine.** Do not introduce a second monolithic enum that duplicates engine state.
- **Orthogonal state dimensions** (cooperating, not one enum): active activity domains; main presentation mode (Idle/Minimal/Compact/LargeCompact/Expanded/Pager); current interaction owner (none/user-pager/reply-editor/call/system); selected notification key; notification queue; pending vs visible child Mini-Islands; transient timer ownership + generation tokens; reply editing state; privacy/lock-screen state; platform capabilities (§17); cutout/window geometry.
- **Playground parity [ARCH]:** the Playground must drive the same engine; no source/render divergence between production and Playground (§24).

---

## 4. Slot system

### 4.1 Canonical diagram **[MEASURED]** (from `Components.svg`)
Reference card 408×160 r44; cutout overlay 128×38.7 centered, overlapping the top edge; inner content rect inset **6.5u**; row divider splits top row (≈102u) from `Bottom` band (≈55u); column dividers at x=198/x=406 give `Leading | Center | Trailing`; a sub-divider inside Center reserves the **cutout zone** above Center content; a center→bottom link allows Center content to flow into `Bottom`.

### 4.2 Slot gravity contract **[DERIVED]**
| Slot | Horizontal | Vertical | Rules |
|---|---|---|---|
| `Leading` | `Start` after content padding | `CenterVertically` | Intrinsic bounds; never `SpaceEvenly`. Glyph, avatar, ring, or 1–2 action buttons |
| `Center` | fill between Leading/Trailing (`weight(1f)`) | `CenterVertically` | 1 line (compact) or 2-line group centered as a unit; title+subtitle share one start x |
| `Trailing` | `End` before content padding | `CenterVertically` | Metric, ring, waveform, 1–2 circle buttons, or small pill; x stable when Center text changes |
| `Bottom` | stretch across content insets | `Bottom`, above inset | Transport rows, CTA pills, control rows, scrubbers, maneuver strips; only when the family defines one |
| Cutout zone | centered | top edge | Reserved; compact content never renders under it |

### 4.3 Flexibility rules **[DERIVED]**
- Slots are **optional**: a state populates only the slots its family defines (e.g. Find-My alert has no trailing; transit alert has no center subtitle).
- Nested rows and two-line groups are first-class: `Center` may be a `Column`; `Bottom` may contain nested `Row`s (transport, CTA pair, maneuver strip + route line).
- Specialized family recipes (§9) override generic slot filling: music scrubber row, navigation maneuver strip, multi-control call row, waveform graphics, layered children.
- Fixed-width critical metrics: timers/counters/remaining-time occupy reserved-width boxes so numeric updates never resize backgrounds or shift siblings (**[ARCH]**).
- Measured reference coordinates in §9 are **calibration data**, not hard-coded offsets: dynamic Compose layouts reproduce the relationships (insets, gaps, pitches), not literal x/y.
- RTL mirrors reading order except numeric progress rails and direction-critical navigation semantics (**[ARCH]**).
- Large-text fallback (§20.3): measure → grow height within family budget → drop secondary metadata/actions; never clip critical labels.
- Transition anchors: each slot declares its collapse/expand anchor (leading glyph center, trailing metric end, bottom-row center) so morphs interpolate from stable points (§19).

---

## 5. Cutout coupling and silhouettes **[MEASURED]**

Cutout footprint: 126×36.67 r18.34 (393 canvas) / 128×38.7 (430 canvas). Community idle pill hole: 18–20u dia, right-of-center.

| Silhouette | Measured geometry | Compose rule |
|---|---|---|
| Owner Minimal capsule | 108×32, r15.9018 | Reference specimen (§2.1); adapt per §2 adaptation rule |
| Idle silhouette | 127×36 r18 centered; hole inside | `max(cutoutWidth,127.dp)` × `max(cutoutHeight,36.dp)`, clip `h/2` |
| Minimal + cap | cutout footprint + one 30u rounded cap (156 total) | Grow only on the side carrying the live glyph |
| Companion bubble | 36.7u dia, gap 11u | Secondary concurrent activity; same top anchor |
| Compact pill | 120/127/155/189/209/210/222/230/250 × 36–37, r18 | Intrinsic width, capped by `Wavail` |
| Large compact (2-line) | 408×96 (Prototype frame 3); leading ring 58 dia; label 11.6u grey above time 20.4u white; trailing waveform 27.3×12.5 | **Width clamp [DERIVED]:** `min(408u, Wavail)`; on narrower devices shrink to `Wavail`, never exceed it |
| Expanded | 367×{142…185.36} r42 (atlas); 408×160 r44 flush-top (presentation); 366×{80,95,126,208} r40–46 (community) | **Per-family margins — no universal value:** atlas 367u on a 393u canvas ⇒ ≈13u; presentation 408u on 430u ⇒ ≈11u; community 366u on 430u ⇒ 32u. Top anchor at status-bar top (y 0–11u); radius per family lookup (§6.2) |

**Expanded responsive-width policy [DERIVED]:** do **not** apply one universal side margin to all reference families — the three families above disagree by design. Each family keeps its own measured relationship to its reference canvas:
- *Atlas family* (367u @393u): near-edge card, ≈13u margins; on other widths scale the margin proportionally, keeping the card visually near-edge.
- *Presentation family* (408u @430u): tightest margins (≈11u); this is the widest surface and the 408u clamp in the large-compact row applies here too.
- *Community family* (366u @430u): inset card with 32u margins; preserve the inset character, do not stretch it to the atlas family's near-edge look.
- All families are capped by **real available screen width**. Use each family’s **own reference canvas** for proportional width and per-family margins as formalized in §28.1, not the old global `screenWidthDp/430` factor. Where the screen is narrower (320 dp devices, split-screen, landscape), shrink the shell or reflow slots, **never** clip essential numerals or reduce safe touch targets simply to match a screenshot.

**Anchoring [DERIVED]:** horizontal center = screen center unless the device cutout is off-center; with a left punch, the compact leading slot sits over the punch (Prototype frames 1–2: ring at x=161.5 inside a pill starting at x=152). Read `WindowInsets.displayCutout`; never assume centered.

---

## 6. Size and radius tokens

### 6.1 Controls and chips **[MEASURED]**
| Element | Size | Radius | Fill |
|---|---|---|---|
| Full-width CTA pill | 335×43 (16u inset in 367) | 21.5 | neutral `#2C2C2D` · blue-tint `#1A1C2D` · red-tint `#1D1011` |
| Side-by-side CTA pair | 161.5×43 each, gap 12 | 21.5 | left neutral, right tinted |
| Small trailing pill ("Unmute") | 79×34 | 17 | `#2C2C2D` |
| Undo pill ("Moved to iPhone") | 70×34 | 17 | `#1E2854`, glyph `#37A3DE` |
| Circle action (compact) | 52 / 50.5 / 48.6 / 44 / 41.4 / 40 dia | h/2 | solid semantic (`#FA3532`, `#37C058`, `#DB4445`, `#34C759`) or ring `stroke white 2.5` |
| Circle action (expanded row) | 51.7 / 50.6 / 49.7 dia, pitch 64.3 (gap ≈12.6) | h/2 | active `#FFFFFF`+black glyph · idle `#2A292D`+white glyph · end `#FA3532` |
| Progress ring (trailing) | 51.3 / 48.6 / 41 / 17.5 dia | — | track tint + accent arc, stroke 2.5–6 |
| Album art | 53×53 r8 (atlas) · 60×60 r16 (community) | 8 / 16 | image |
| Avatar compact call | 56 dia (atlas) · 48 r24 (community) | circle | image |
| Avatar expanded call | 44.3 dia | circle | image |
| Comment thumbnail | 103×86 r24 | 24 | image |
| Device/vehicle thumb | 20×13.3 · 32×20 r10 · 30×20 r10 · 47×31.3 | 10 | image |
| Scrubber rail | 240×6.5 (atlas) / 270.5×6 (community) | 3 | track `#3F3F3F`@70% or `#2A2A2A`; progress `#FFFFFF`@80% |
| Waveform bar | 1.8–3 wide, rx 0.9–1.5, heights 1.8–27 | bar/2 | per-bar color/opacity (§10) |

### 6.2 Radius lookup (family-specific; replaces any universal formula) **[MEASURED]** unless marked
| Surface height | Radius | Source |
|---|---|---|
| 34 (small pill) | 17 | measured |
| 36–37 (compact pill) | 18 | measured |
| 43 (CTA) | 21.5 | measured |
| 80 (community call card) | 40 | measured |
| 86 (contextual compact/alert) | 43 | measured |
| 95 (connected card) | 46 | measured |
| 96 (large compact) | 44 | **[PROPOSED]** (visually consistent; validate) |
| 126 (comment card) | 44 | measured |
| 142–185.36 (atlas expanded families) | 42 | measured |
| 160 (presentation expanded) | 44 | measured |
| 208 (community music card) | 44 | measured |

Rules: true 34/36–37/43/80/86-height pill families follow approximately r = h/2; **95-height (r46) and 96-height (proposed r44) are explicit exceptions**. Use the table, not a formula. Card families (h ≥ 126) use their fixed family radius; **never** derive r = h/2 for cards. Contextual 86u alert surfaces are a separate family from 36–37u compact pills and must not share their tokens.

### 6.3 Touch targets **[PROPOSED]/[PLATFORM]**
Interactive controls get ≥48 dp hit targets where feasible, via centered content inside `sizeIn(min=48.dp)`; invisible touch padding must not widen the overlay's intercepted region unsafely (§3.1). Verify on device; where 48 dp is infeasible (dense expanded control rows), document the exception and compensate with spacing.

---

## 7. Typography roles **[PROPOSED]** (validate on rendered frames; reference SVG text is outlined paths)

| Role | Size | Weight | Lines | Overflow | Color |
|---|---|---|---|---|---|
| Compact status/label | 12–14sp | Medium | 1 | end ellipsis | white or accent |
| Compact trailing metric (timer) | 17–24sp | Semibold, tabular | 1 | never ellipsize | accent |
| Contextual title / caller / track | 15–17sp | Semibold | 1 | end ellipsis | white |
| Contextual subtitle / artist / medium | 12–14sp | Regular | 1 | end ellipsis | grey |
| Uppercase status tag | 11–12sp | Semibold, tracked | 1 | end ellipsis | accent green |
| Elapsed / remaining | 11–12sp | Medium, tabular | 1 | never ellipsize | grey |
| Navigation distance | 24–32sp | Semibold, tabular | 1 | scale before clip | white |
| CTA label | 14–16sp | Medium | 1 | ellipsis only with full a11y label | white or tint accent, centered |
| Mini-Island identity / preview | 11–13sp | Medium | 1 | end ellipsis | white / grey |
| Widget headline value | 28–40sp | Semibold, tabular | 1 | scale before clip | white |

Calibration (measured glyph-height proxies): compact title 12.2–16.8u; subtitle 10–12u; CTA label 13.3–17u; large-compact time 20.4u; widget value ≈34u.
**Gravity contracts [DERIVED]:** subtitle start x == title start x (measured 97.9 vs 98.4); trailing metric x never shifts on title change; RTL per §4.3. Tabular figures via Compose `TextStyle(fontFeatureSettings = "tnum")` where the chosen font supports it; do not assume a `FontFeatureSettings` constructor exists.

---

## 8. Color tokens

### 8.1 Palette **[MEASURED]**
| Role | Tokens |
|---|---|
| Surface | `#000000` |
| Primary text / active glyph | `#FFFFFF`, `#F0F0F5` |
| Secondary text | `#A4A4A9`, `#9A9A9A`, `#838388`, `#818383`, `#7B7B7D`, `#7D7D7D`, `#8F8F8F` |
| Disabled / track grey | `#5A5A5A`, `#3F3F3F`, `#2A2A2A`, `#2C2C2D`, `#2A292D` |
| Red family | `#FA3532` (atlas), `#DB4445` (community decline), red-tint CTA bg `#1D1011`, owner Minimal specimen `#DC0917`@0.6 (**[OWNER]** reference truth, not a proposed guess — §2 row 1) |
| Green family | `#37C058`, `#34C759`, `#00E46C`, `#A1FC85`, green-tint bg `#0B2F16`, ring tracks `#13351F`, `#173B2A` |
| Timer / charging | `#FB8B28`, `#E49046` |
| Transfer / nav / link | `#37A3DE`, `#64D2FF`, `#90C9F9`, `#9AD4F2`, blue-tint bg `#1A1C2D`, `#1E2854` |
| Music / media accent | magenta waveform gradient; community `#C75266` |
| Focus / DND | `#504BCA` |
| Annotation / guide layer only (never ship **as guide ink**) | `#FF2D55`, `#9747FF`, and `#64D2FF` **when used as slot-guide/annotation ink** |

**Context classification rule [DERIVED]:** `#64D2FF` legitimately appears in two roles. As **content** fill/stroke inside a shipped specimen (transfer/nav/link accent) it is a usable semantic blue (§8.1 "Transfer / nav / link"). As **guide-layer ink** (slot dividers, cutout overlays, measurement annotation in `Components.svg`/presentation files) it must never reach production. Classify by *layer and role in the source file*, not by banning the hex universally; the same applies to any colour that appears in both layers.

### 8.2 Red and multi-accent semantics (corrected) **[OWNER]/[ARCH]**
Red is permitted **only** for: (a) recording-in-progress indicators (waveform, dot, duration); (b) stop / end / decline destructive controls; (c) warning or critical status where reference evidence supports it (timer-alert ring, low-battery/critical, red-tinted destructive CTA); (d) the exact owner-confirmed Minimal capsule specimen. Red is never used to brighten an otherwise empty card.
The "single semantic accent" rule applies to the **status/identity accent** of a surface. Distinct functional control colors may coexist when their meanings differ (green Accept + red Decline; orange pause + grey dismiss; green arc + white tick). Do not globally recolor reference designs.
Colour is never the sole signal: pair with icon + text (**[ARCH]**, accessibility).

---

## 9. Presentation families — measured compositions

### 9.A Minimal glyph tokens **[MEASURED]**
25 tokens, 38×38 r19 black circle + centered glyph: music waveform (magenta); AirDrop rings (`#37A3DE` stroke 1.2, 3 arcs + 3.7 dot); phone (`#37C058`); timer ring "7S" (green arc 2.5 + track 1.9); timer (`#FB8B28`); timer alert (`#FA3532`); video cam; FaceID; nav arrow (`#37A3DE`); voice waveform red + 7 grey dots; record dot 12 dia; bell / check / lock / unlock (`#F0F0F5`); link (`#37C058`); moon (`#504BCA`); car (`#504BCA`); FindMy (ring `#3E4140` 0.74 + dot 6.5 + white 5); waveform white; copy (`#37A3DE`); person-broadcast; broadcast (`#37C058`); train (`#9AD4F2`).
Usage: companion bubbles, minimal live indicators, and leading glyphs. The owner Minimal capsule (§2.1) carries none of these.

### 9.B Compact recipes (contextual 367×86 r43; rel coords = calibration) **[MEASURED]**
1. **AirDrop/transfer** — leading rings 52.9×49.9 @(17.5,16.5) + 20×20 r2 thumb @(52,51); center inline "AirDrop" white + "from Chi" grey, one baseline y45.3; trailing ring 51.3 dia stroke `#37A3DE` 4.67 @50% + 22×22 inner glyph.
2. **Timer w/ actions** — leading 52.2 dia pause `#FB8B28`@45% + dismiss `#838388`@70%; trailing right-aligned "Timer" 12.1u + "3:35" 23.6u `#FB8B28`.
3. **AirPods connected** — leading glyph 44 dia; center "Connected" grey 10.5 / name white 10.9; trailing ring 48.6 stroke green 4.42 + "75%" 10.2 green.
4. **Voice recording** — leading 13–16 bars 3w rx1.5 `#FA3532` h10–27 + 11 grey dots 3×3 @40%; trailing "00:06" 13.3 red + stop 50.5 dia ring white 2.5 w/ 24×24 red square.
5. **Screen recording** — red dot 12 + "00:06" red line 1; "Screen Recording" white 16.8 line 2; same stop button.
6. **Shortcut done** — layers glyph 33.7×28.4 (lower @40%); "Shortcut"/"100%"; trailing 47.8 dia `#F0F0F5` disc w/ check.
7. **Incoming call** — avatar 56 dia; "Mobile" grey 9.8 / "Tamia Castillo" white 12.2; trailing 52 dia decline `#FA3532` + accept `#37C058`, gap 7.6.
8. **Satellite** — 56 dia ring glyph (`#3E4140` 1.62 track, green arc, white ticks); "CONNECTED" green 9.6 caps / "Keep pointing at Satelite" white 14.9; trailing 41.4 dia `#0B2F16` + green message glyph.
9. **Find My alert** — 56 dia concentric teal ring; single white line 16.7; no trailing.
10. **Silent mode** — bell-slash 22.9×24.7; "SilentMode" grey / "On" white; trailing pill 79×34 r17 "Unmute".
11. **Moved to iPhone** — AirPods glyph 44; single grey line 10.6; trailing pill 70×34 r17 `#1E2854` + `#37A3DE` undo 16.8.
Community 36-high variants: media = art 22×22 r6 + gradient waveform 17×18; call = green phone 12 + green "0:00" + multicolor waveform 28.1×16.8; connected = thumb 20×13.3 + ring 17.5 stroke 2.5; comment = avatar 20 r10 + circled count 15; lyft = logo 32×20 r10 + pink waveform `#C75266` 1.55.

### 9.C Expanded media (367×177 r42) **[MEASURED]**
Header: art 53×53 r8 @(24,32); title white h14.7 @(98.4,41.5); artist `#9A9A9A` h10.7 @(97.9,63.1); waveform gradient 19.8×22.4 @(312.1,47.5).
Scrub row: elapsed grey 23.5×8.8 @(25.2,98.3); rail 240×6.5 `#3F3F3F`@70% @(63,99.8); progress white@80% width=fraction·240; remaining grey @(317.2,98.4).
Transport y≈129–157: prev 28.3×16 @108.2; play 25×28 @179.8 (primary); next 28.3×16 @244.4; output 20.2×19.9 @304.9. Video variant: ±15s circles 24.4×26.7, art placeholder `#2C2C2D` + TV subline. Owner Group 3 confirms magenta progress + 3-button transport + top-right waveform.

### 9.D Expanded call (367×166–172.64 r42) **[MEASURED]**
Header: avatar 44.3 @(29,34); name white h12.2 @(82.3,41); medium grey h10.6 @(82.8,58.9); info 17.9 `#838388` @(318,47.3).
Control row y=92.3: five circles pitch 64.3, dia 49.7–51.7; active=white+black glyph, idle=`#2A292D`+white glyph, end always last slot `#FA3532`. Control set varies by medium (AirPods/speaker/mic/video/screen-share/broadcast) — never a fixed five-button picture.

### 9.E Expanded alert with CTA (367×142–162 r42) **[MEASURED]**
Single CTA: glyph 30.8×27.8 @(35.7,34.7); title white 1–2 lines x83.7; CTA 335×43 r21.5 @(16,H−59), centered label 17u.
Two CTAs: 161.5×43 @x16 and x189.5 gap 12; left neutral+white label, right tinted+accent label.
Trailing-status variant: title + grey subtitle + trailing glyph 25.4×14.3; tinted CTA with accent label ("Stop Mirroring" `#37A3DE` on `#1A1C2D`).
Destructive variant: CTA `#1D1011`, label `#FA3532` ("End Route").

### 9.F Navigation (367×185.36 r42) **[MEASURED]**
Maneuver strip y≈46–80: four arrows; upcoming `#838388`; next maneuver white and largest (33×27.7). Distance "90 ft" white 41.2×13.8 @(18.5,95.9). Shield image 51×39 @(17,122.4) + road "North" white 13.5 + city grey 10.1. Hierarchy: distance > road > context.

### 9.G Live Activity expanded layouts (corrected coverage) **[ARCH]**
Island-native expanded layouts are **supported** for delivery, flight, transport, navigation, sports and timer when genuine source data exists — readable, content-aware summaries with real actions where available. Grammar from owner widgets (Group 3/4/5), trimmed to island scale:
- **Delivery:** milestone/proximity/timeline row + CTA; resurfacing per UX-C09.
- **Flight:** codes grey caps / cities white; route line + plane glyph disc; departure/status/arrival row (green).
- **Sports:** team/score/time or match state; **no cricket content** (project constraint **[OWNER]**).
- **Transit/transport:** station + platform/route + meaningful action ("End Route").
- **Timer:** large tabular countdown + pause/stop per real capability.
- **Stock/data charts:** chart only when activity data warrants; dashboard-scale ChainX/Screen-Time compositions remain **widget-scale, separate from Island layouts** (**[OWNER]** grammar reference only).
Do not invent background integrations or live feeds (§17).

### 9.H Community expanded cards (430 canvas, card x=32 w=366) **[MEASURED]**
Incoming call 366×80 r40: avatar 48 r24 @16,16; sub grey 10 @(78,24.1); name white 13.1 @(77.2,42.1); decline 40 r20 `#DB4445` @254,20; accept 40 r20 `#34C759` @306,20.
Connected 366×95 r46: thumb 47×31.3 @24,32; label grey 10.2; name white 15.9; ring 41 dia stroke 6 track `#173B2A` arc `#00E46C` + value inside.
Comment 366×126 r44: thumb 103×86 r24 @20,20; name white 11.4; body grey 3 lines 45.4; circled count 15 top-right; 3-dot column 4 dia (middle full opacity).
Music 366×208 r44: art 60 r16 @20,24; title 13.8; artist `#7D7D7D` 13.8; waveform 17×18; times 7.3; rail 270.5×6 r3 `#2A2A2A` + white progress 138.7; transport six glyphs y152 (shuffle/prev/play/next/repeat + output 23×23).

### 9.I Quick Controls and pager surfaces **[ARCH]**
Idle single tap opens **Quick Controls** (UX-C01): a grid/row of real system toggles and app shortcuts governed by §17 capabilities; not the main application, not a no-op. Pager surface: one notification → single-detail card; ≥2 → horizontal pager card with `k of N` and ≤7 sliding dots (§13). The `ActionControlExpanded` demo state is a **T1 Playground fixture renderer** for this surface — the fixture label describes its evidence tier, and does **not** imply the Idle → Quick Controls product flow is unsupported (§16.1).

---

## 10. Waveform glyph component **[MEASURED]**
Vertical bars width 1.8–3u, rx=width/2, pitch ≈3.05u, heights 1.5–27u mirrored about center; silent tail = 3×3 dots @40%. Palettes: recording `#FA3532` (dots `#5A5A5A`); lyft `#C75266`; call multicolor (`#A1FC85`/`#E49046`/white); music magenta gradient; idle grey. Sizes: full ≈27u (expanded/compact trailing), small 12–17u (status). One `Canvas` composable driven by `List<Float>` amplitudes + palette; animate amplitudes, never swap drawables.

---

## 11. Owner-approved UX decision contract UX-C01–C16 **[OWNER]** unless noted

| ID | Preconditions | Event | Main Island Result | Child Result | Queue Result | Android OS Side Effect | Animation | Acceptance Test |
|---|---|---|---|---|---|---|---|---|
| C01 | Idle, no notification/activity | Single tap | Opens Quick Controls | none | unchanged | none | expand to controls surface | Tap idle with 0 notifications → Quick Controls, not app, not no-op |
| C02 | Exactly 1 eligible compact notification | Single tap | Expands that notification's detail in-Island | none | unchanged | none | compact→expanded morph | 1 notification tap shows its detail |
| C03 | ≥2 eligible notifications | Single tap | Opens newest-first horizontal pager | none | unchanged | none | compact→pager morph | 12 notifications → pager opens on newest, `1 of 12` |
| C04 | User reading notification key `msg-5` (currently at position 5) | Ordinary notification arrives and is inserted ahead in newest-first order | **Selected key `msg-5` stays selected** and its content does not jump; its numeric position may legitimately become 6; `k of N` re-derives from the new canonical ordering (`6 of 13`); subtle New indication | optional child per C11 (§14 precondition) | appended newest-first | none | badge/count update only; no page re-bind | Key `msg-5` still on screen; label reads `6 of 13`; no visual hijack |
| C05 | Pager open | Source removes selected notification | Next item selected; if none, previous; pager stays open while items remain | unaffected | item removed | none | content cross-fade, no jump | Removal at page k lands on k+1 else k−1 |
| C06 | Any Island notification | × action | Island-only removal by default; Android shade preserved | child merge independent | item removed from Island queue | **none** unless explicit user setting + clearable notification + capability | exit/merge animation | Shade still shows notification after × |
| C07 | Last **Island-eligible** notification disappears, no ongoing live activity | Source removal **or Island-only dismissal** empties eligible Island queue | Returns to Idle Compact | **all children merge/exit; no orphaned child draws** | Island eligible queue empty; an Island-only tombstone may remain while the source Android notification is still active | **No OS dismissal** for Island-only removal; source removal is an external source event | collapse to idle; child timer callbacks cancelled/retired against generation tokens | Both source-removal and Island-only-last-dismiss return Idle; the latter keeps its OS shade notification and suppression tombstone |
| C08 | Island-dismissed notification | Meaningful content/status change | May resurface | may re-emerge as child | tombstone lifted for that key | none | preview emergence | Meaningful update resurfaces; minor paint does not |
| C09 | Delivery activity dismissed | Milestone or first Nearby alert (**meaningful**) | Resurfaces: when Main is compact → **transient main preview** (§12.1 timings) then restore compact + badge/count | child **only** if the §14 precondition holds (an eligible expanded main is occupied and priority allows) | queue order preserved | none | preview emergence | Milestone / first Nearby alert resurfaces; routine stop-count decrements and ETA jitter do not |
| C10 | 12 queued, Main compact | Major milestone from a dismissed item | Temporary priority preview on the main surface, then collapse to compact with truthful count | **no child stack required when compact**; a transient child appears only if the §14 precondition holds | **13 eligible after append** | none | preview then settle | Count truthful (13) with no child stack; preview collapses to compact |
| C11 | Main **expanded** and user attention occupied (precondition: an eligible expanded main presentation is occupied **and** §15 priority allows a child) | Ordinary notification arrives | Content being read unchanged | animated secondary Mini-Island below main | appended | none | child emergence | Reading position intact. A **compact** main never spawns a child stack — it uses a transient preview or passive indication (PD-026, §15.1) |
| C12 | Child visible | Tap child | Child promoted to Main Expanded | promoted child leaves stack | selection key = child's key | none | promotion morph | Previous main reachable via pager |
| C13 | Children present | Screen-size evaluation | unchanged | 1–2 visible small screens, ≤3 large; rest in accessible overflow. **If zero child cards and no overflow chip fit safely**, overflow stays reachable via the Main Pager or a semantic accessibility action (§14, §20.1) — never an off-screen chip | unchanged | none | reflow | Overflow count = pending new arrivals; on a short screen the overflow is still reachable without drawing off-screen |
| C14 | Children visible | New child arrives | unchanged | newest emerges directly below Main; older shift down preserving identity | unchanged | none | shift animation | Order and identity stable |
| C15 | Child visible | Timer/interaction | unchanged | smart adaptive lifetime; touch pauses timer; visual merge never removes Android notification or queue entry | entry preserved | none | pause/merge | Merge then check shade + queue |
| C16 / PD-001 **[ARCH]** (not owner-approved) | Multiple children | New arrival | unchanged | each child keeps independent timer; new arrival must not restart existing children; stale-token rejection; touch-release grace | unchanged | none | none for unrelated children | Burst arrivals: existing countdowns untouched |

Architect-selected defaults referenced above (trial, tunable **[ARCH]**): normal child ≈5 s, important milestone ≈7 s, touch-release grace ≈2 s, ordinary short preview ≈3.5 s, longer readable preview ≈5 s (cap ≈6 s), call-restoration window ≈10 s. Full table and authority in **§12.1**.

**Meaningful vs not meaningful [ARCH]:** *meaningful* = major delivery milestone, first Nearby/proximity alert, status-class change, user-visible state transition. *Not meaningful* = routine stop-count decrements, ETA jitter, percentage ticks, repaint-only updates. Only meaningful events lift a suppression tombstone (C08/C09) or justify a priority preview (C10).

**Governance note:** UX-C16's independent-timer rule is architect-selected **PD-001**, not an independently owner-approved choice. The behavior is unchanged; only its authority classification is corrected (§0.2).

---

## 12. Notification lifecycle and queue invariants **[ARCH]**

Distinct behaviors: first arrival; transient automatic preview; user-expanded notification; pager open; new notification during transient preview; new notification during user-owned pager reading (C04); same-key update (content refresh, no new page); source removal (C05/C07); Island-only dismissal (C06); optional OS cancellation (explicit setting + clearable + capability only); meaningful-update resurfacing (C08/C09); minor-update suppression (tombstone retained); notification action failure (surface error state, no silent retry loops); reply start/send/cancel/restore (draft preserved across arrivals); preview expiry; child expiry; interrupted presentation restoration (post-call/post-privacy); permission revocation + source reconciliation (no corrupted state, no ghosts).

Invariants: max **50** retained eligible notifications; framework notification identity (key) — never page index or synthetic unread counts; newest-first applied on first pager opening; selected stable key preserved across insertions/ordinary updates; never auto-mark-read; suppression tombstones survive insignificant updates and are cleaned on source removal; Android dismissal, Island collapse, and child animation completion are three independent events — none implies another.

### 12.1 Behavioral timing defaults **[ARCH]**
All values below are **architect-selected trial defaults** from the Aurora Master Product Behavior Specification. They are **not** SVG measurements and **not** Apple specifications. They are tunable without any visual-spec change; presentation code must read them from a single authoritative policy rather than hard-coding competing numbers.

| Behavior | Default | Notes |
|---|---|---|
| Ordinary short transient preview | **≈3.5 s** | compact-main previews (PD-026 §15.1), glance-class content |
| Longer readable preview | **≈5 s, capped near ≈6 s** | previews carrying text the user is expected to read |
| Preview expiry start | when content is **readable** (rendered + settled) | never at queue-entry time; a slow render extends the preview, it never truncates it |
| User-expanded content | **no automatic short expiry** while the user is reading | collapse only on user action, preemption (§15) or source removal |
| Normal Mini-Island (child) | **≈5 s** | independent per child (PD-001 / C16) |
| Important Mini-Island (child) | **≈7 s** | milestone / urgent class |
| Child touch pause | resume with **remaining valid lifetime + ≈2 s grace** | a refreshed child is never dismissed by an already-expired timer |
| Incoming-call restoration | **bounded**, conditional on a valid **unlocked** context | pager/editor restoration window ≈10 s from interrupter end (§15.2); outside it → next valid state, no ghost |
| Outside tap / Back during inline reply | **protect the unsent draft** | up to ≈60 seconds of in-memory unlocked-session retention after safe collapse, if policy permits; discard on logout/lock/privacy invalidation, never auto-send |

---

## 13. Horizontal pager contract **[ARCH]**

Layout: 1 notification → single-detail presentation; ≥2 → real horizontal pager (`HorizontalPager`, stable key-based pages). Opens newest first. `k of N` label + accessible current-page announcement; ≤7 sliding indicator dots for large queues (windowed around current). Efficient composition at 12/25/50 items (`beyondBoundsPageCount` minimal, lazy content).
Interaction: horizontal swipe = browse only, never dismissal; swipe-up = non-destructive collapse; outside tap / Back = safe collapse when supported; reply composer consumes gestures so typing never pages; double tap never double-dispatches.
Selection identity: the invariant is the **selected notification key**, never its numeric page index. Pages are addressed by key, so reordering never re-binds a different notification into the user's viewport (key-stable horizontal paging). An insertion ahead of the selected item legitimately shifts its position (5 → 6) while the same key stays selected, the visible content does not jump, and `k of N` re-derives from the new canonical ordering (`6 of 13`) — the label follows the ordering, the selection does not.
Selection rules: user-selected key preserved across insertions and ordinary updates; selected-item removal → next else previous, pager stays open while items remain (C05); non-selected removal → no page jump and no key change; same-key update → current content updates in place with neither key nor position changing; empty queue → truthful Idle/Quick Controls, never synthetic insertion.

---

## 14. Mini-Island lifecycle and independent timers **[ARCH]** (PD-001, PD-005; UX-C11–C16 behavior — C16's authority is [ARCH], not owner-approved)

States: `QUEUED → EMERGING → SETTLING → VISIBLE → EXITING → MERGED`, with interaction alternatives `TOUCH_HELD`, `RELEASED`, `PROMOTING`, `SOURCE_REMOVED`, `INTERRUPTED`, `PRIVACY_REDACTED`, `RETIRED_ON_COLLAPSE`.
- **Creation precondition [ARCH]:** a child Mini-Island is created **only** when an eligible **expanded** main presentation is already occupied (user-expanded notification, pager, expanded activity) **and** the §15 priority policy allows a child. A compact main never spawns a vertical child stack — it uses a brief transient preview or a passive compact indication instead (PD-026, §15.1).
- **Deliberate Main collapse (PD-005) [ARCH]:** when the Main Island is deliberately collapsed, its visible children must **visually merge or safely exit**; the underlying eligible notifications **remain in the canonical queue**; **no child continues drawing underneath a now-compact surface**; associated visual timer callbacks are cancelled / invalidated / retired against their generation tokens; **stale callbacks must never re-expand the Island or alter the selected key**. Independent timers apply **while children are actually visible**. A temporary call interruption is **not** a deliberate collapse: it is handled separately (§15.2–15.3). Children hide/suspend animation, but their **absolute monotonic expiry deadlines continue** unless an individual child is held; only non-expired, still-valid children may restore. This avoids extending all child lifetimes through a long phone call.
- Identity: per-child stable notification key + generation token; timers correlate to tokens; stale callbacks (expired token/generation) are ignored (**C16**).
- Timer start: on `SETTLING` completion (visible settle), not on arrival queueing; each child counts independently; new arrivals never restart existing children.
- Touch: `TOUCH_HELD` pauses that child's timer; `RELEASED` resumes with the remaining valid lifetime plus the ≈2 s grace (§12.1, tunable); a refreshed child is never dismissed by an expired timer.
- Insertion: newest-first, emerging directly below Main (C14); older children shift down preserving identity; visible budget 1–2 small / ≤3 large screens (C13); overflow `+N` counts **pending new child arrivals only**, never the canonical queue size. **Low-height fallback [ARCH]:** if zero child cards *and* no overflow chip can fit safely (short screens, low available height, IME open), the overflow remains accessible through the **Main Pager** or an explicit **semantic accessibility action** (§20.1) — never render an off-screen `+N` chip, and never draw children over other apps just to make the chip fit.
- Promotion: child tap → `PROMOTING` → Main Expanded; previous main selection remains reachable via pager (C12); if the child's source disappears mid-promotion, fall back to next/previous without crash.
- Bursts: 10 and 25 arrival bursts must keep counts truthful, animations non-blocking, and low-height devices defer children instead of covering apps.
- Interruption: an incoming call preempts; children hide/suspend their **visual presentation** (not their monotonic expiry deadline) rather than retiring immediately. On call end, restoration is **bounded and conditional** (§15.2): only valid non-expired children restore inside the restoration window on an unlocked device with their source present; expired/removed children never resurrect. Individual `TOUCH_HELD` deadlines remain explicitly paused per §14 touch rule.
- Merge/exit: visual merge or expiry never removes the Android notification or canonical queue entry (C15/C06); no duplicate notification or action dispatch during any transition.
Four separate collections: canonical queue (≤50); pending newly-arrived child notices; currently visible children; hidden child overflow.

---

## 15. Multi-activity priority and restoration **[ARCH]**

Precedence (contextual, not a license to overwrite user-owned UI):
1. System safety / privacy / permission / secure-screen restrictions.
2. Active explicit user interaction (esp. reply editing) — owns **interaction and content continuity** (the draft, the composer session, the selected key), **not** exclusive rights to the whole surface.
3. Legitimate ringing-call interruption — outranks editor ownership for **presentation prominence**: the call is shown, the editor suspends with its draft preserved (§15.3). Editor ownership is never a license to hide a real call.
4. Urgent timer/alarm; imminent navigation instruction.
5. User-selected expanded notification or Live Activity.
6. Ongoing media, navigation, call duration, recording.
7. Eligible ordinary notification preview.
8. Charging / ringer / volume / Bluetooth HUDs.
9. Idle.

Interruption & restoration matrix (each: preempt → restore the previous valid presentation when the interrupter ends, under the bounded policy in §15.2; never restore a source that disappeared):
expanded music (user-expanded) + new message → Mini-Island below, music controls never replaced (§15.1); compact music + new message while the user is not interacting → brief transient preview, then compact music restored with badge/count or companion (PD-026, §15.1); compact music + new message during active media interaction → focus protected, notification deferred and passively indicated on settle; compact music + preview disabled / DND / privacy → passive compact indication only; music + navigation → nav turn-now preempts compact, music resumes after; timer + incoming call → call preempts, timer compact restores; pager with selected key + incoming call → call takes the main surface with safe prominence, pager restores the **same selected key** (numeric position may differ) inside the §15.2 window; reply editor + incoming call → §15.3: call takes main prominence, editor suspends, draft preserved, bounded restoration on call end; navigation turn-now + ordinary notification → notification deferred/child per §14 precondition; recording + low-priority BT event → recording indicator survives; delivery milestone + many queued → temporary main priority preview then collapse (C09/C10), no child stack required when compact; critical event during child promotion → promotion completes or yields atomically, no half-state; call ending with expired children → children stay gone, main restores the valid selection; activity source disappearing during restoration → next valid presentation, no ghost.
Never fabricate Android actions or privileges to satisfy a restoration (**§17**).

### 15.1 Compact media and ordinary notifications — PD-026 **[ARCH]**
| Main state | User state | Result for an eligible ordinary notification |
|---|---|---|
| Music/media **compact** | not interacting | **brief transient preview** over the compact surface (≈3.5 s, §12.1) → after timeout restore the compact media state with an appropriate notification **badge/count or companion** |
| Music/media **compact** | **actively interacting** (scrub, transport, output change, volume drag) | protect the user's focus: no preview swap; the notification stays queued and is indicated passively once the interaction settles |
| Music/media **expanded by the user** | any | media controls are **never replaced**; Mini-Islands appear underneath per §14 |
| Music/media compact | auto-preview disabled, **or** DND/privacy suppresses previews | retain a **passive compact indication** (badge/count/companion) instead of a preview |

A **vertical Mini-Island stack under a compact main surface is not the universal default.** Children require the §14 creation precondition (an occupied eligible expanded main plus priority allowance). Compact mains use a transient preview or a passive indication instead.

### 15.2 Bounded restoration policy **[ARCH]**
Restoration after an interrupter (call, privacy change, system preemption) is **bounded and conditional**, never unconditional:
- The device must be **unlocked** and the current privacy state must still permit the content.
- The prior source must **still exist** (notification not removed, activity still live). Otherwise restore the **next valid** state — never a ghost card.
- Pager / reply-editor restoration window ≈**10 s** from interrupter end. Inside it, restore the same **selected key** (its numeric position may legitimately differ, §13). Outside it, fall back truthfully to compact / queue / Idle.
- Inline-reply drafts survive via **short-lived in-memory retention** (§12.1): never auto-sent, never silently discarded, and dropped without ceremony once the retention window or privacy state invalidates them.
- Children restore per §14 (valid sources only; expired ones do not resurrect).

### 15.3 Call ↔ inline-reply transition table **[ARCH]**
| # | Current | Event | Main result | Draft / editor result | OS side effect | On interrupter end |
|---|---|---|---|---|---|---|
| 1 | reply editor open, unsent draft | legitimate ringing call | **call takes the main surface with safe prominence** — never silently hidden behind the editor | editor suspends; draft + keyboard/composer session context preserved in memory | none — no auto-send, no cancel, no discard | restore the editor if inside the §15.2 window, unlocked, and the source is still valid |
| 2 | call on main | user answers or declines | OS/dialer-owned call UI; the Island renders only capability-backed controls (§17) | draft untouched | none | as row 1 |
| 3 | call ends | window valid, source present, unlocked | editor restored with the draft and session context where possible | draft intact | none | — |
| 4 | call ends | window expired **or** source removed **or** privacy state changed | **next valid** presentation (compact / queue / Idle) | draft expires with the retention policy; never auto-sent | none | no ghost card, no stale editor |
| 5 | reply editor open | ordinary notification (no call) | editor keeps interaction ownership (priority 2) | draft intact | none | unchanged |

Never impersonate the OS call UI, and never fabricate answer/decline controls the platform does not grant (§17). A ringing call is **not** suppressed by reply-editor ownership; conversely, presenting the call must not destroy the draft.

---

## 16. Source-state coverage matrix (60 `IslandDemoState` values)

### 16.1 Evidence ladder — six separate claims **[PLATFORM]**
These are six different assertions. Never collapse them into one word, and never infer a higher tier from a lower one.

| Tier | Claim | Evidence required | Reviewed-baseline status |
|---|---|---|---|
| T1 | Source enum / UI present | `IslandDemoState` value + a renderer composable | present for all 60 rows |
| T2 | Runtime data-source integration | a real session / listener / system source feeding that state | present for some rows only |
| T3 | Feature or unit-test evidence | passing engine/unit tests for that state's behavior | must be cited per row; not blanket-assumed |
| T4 | Real device / instrumented evidence | observed on-device or instrumented run, tied to a specific state, device and test log | **Limited Task 05/06 instrumentation evidence exists (reported 14 device passes); NO blanket coverage or per-row T4 assignments** |
| T5 | Production accessibility-overlay verification | `TYPE_ACCESSIBILITY_OVERLAY` service bound and validated (§3.1) | **BLOCKED** |
| T6 | Release readiness | T1–T5 plus the §24 gates | **NO for every row** |

**Status column semantics (V4.2):** the column reports the highest **implementation** tier (T1/T2/T3) plus a scope qualifier. It is **never** a verification claim. Qualifiers: `fixture` = T1 Playground fixture renderer — this does **not** imply the corresponding product flow is unsupported · `partial` = integration incomplete · `ref` = reference-only visual concept · `platform` = platform-dependent capability · `blocked` = intentionally not shippable (policy). Enum existence ≠ integration; renderer existence ≠ device verification; unit tests ≠ production verification.

Columns: State | Domain | Mode | Slot recipe | Geometry family | Required data | Supported actions | Priority (§15) | Interruption | Privacy | Fallback | Implementation tier (see §16.1).

| State | Domain | Mode | Slot recipe | Geometry family | Required data | Actions | Prio | Interruption | Privacy | Fallback | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Idle | system | Idle | none | idle silhouette 127×36 | none | tap→Quick Controls, long-press→settings | 9 | preempted by all | n/a | n/a | T2 (silhouette + tap routing); Quick Controls surface integration pending (§9.I) |
| Minimal | system | Minimal | owner capsule, **no content** | 108×32 ref | **none for reference shape**; runtime event mapping separate | none | 6–8 | preempted | n/a | idle | **owner-confirmed reference target**; reviewed runtime `IslandDemoState.Minimal` still uses old split-pill geometry — implementation must migrate and verify (§28.2), not assume shipped |
| MusicCompact | media | Compact | glyph+track identity+waveform | 190×36.67 pill | media session | play/pause if session | 6 | brief preview or passive cue for ordinary msgs, **not child stack on compact** (PD-026) | n/a | idle | T2 |
| MusicExpanded | media | Expanded | §9.C | 367×177 r42 | media session + metadata | transport, seek, output | 6 | preempt→restore | artwork redacted on lock per policy | MusicCompact | T2 |
| CallCompact | call | Compact | avatar+name+accept/decline | 36–37/86 | telecom call state | answer/decline | 3 | preempts | caller per lock policy | idle | T2 |
| CallExpanded | call | Expanded | §9.D | 367×166 r42 | telecom + capabilities | mute/video/end per capability | 3–6 | preempts | redact name on lock if set | CallCompact | T2 |
| CallSummaryExpanded | call | Expanded | summary header+CTA | alert family | call log event | callback/dismiss | 7 | ordinary | redact | compact | T2 partial |
| FaceTimeCallExpanded | call | Expanded | §9.D video set | 367×172.64 | video-call source | video controls | 3 | preempts | redact | CallExpanded | ref T1 (Android equivalent per dialer/role) |
| SharedMediaCallExpanded | call | Expanded | §9.D + media row | 367×172.64 | call + media session | media+call controls | 3–6 | preempts | redact | CallExpanded | ref T1 |
| OngoingCallBanner | call | Compact | duration+mute/end | 36–37 pill | telecom duration | mute/end | 6 | preempted by ringing | redact duration opt. | idle | T2 |
| NotificationCompact | notification | Compact | app glyph+title+time | 36–37/86 | notification | expand/dismiss | 7 | child rules | redact body | idle | T2 |
| NotificationStacked | notification | Compact + children (only when expanded) | compact count on main; actual children from shared `LayeredMiniIslands` | true compact 36–37; MINI ~50 | queue + pending child notices | promote/merge when expanded | 7 | C11–C16 | redact | single | T2, not a second queue |
| NotificationWithMusicCompact | media+notification | Compact | music+badge/companion | 190×36.67 plus optional companion | session+queue | expand | 6–7 | compact preview/passive cue per PD-026; child only under expanded main | redact | music compact | T2 |
| NotificationExpanded | notification | Expanded | title/body/actions/reply | 367×142–162 | notification actions | action/reply/× | 5 | user-owned | redact on lock | compact | T2 |
| SilentModeCompact | system HUD | Compact | bell-slash+state+Unmute | 36–37/86 | ringer mode | unmute if capable | 8 | preempted | n/a | idle | platform (T1–T2, capability-dependent) |
| SilentModeExpanded | system HUD | Expanded | state+CTA | alert family | ringer mode | change mode | 8 | preempted | n/a | compact | platform (T1–T2, capability-dependent) |
| RingerModeExpanded | system HUD | Expanded | mode+CTA | alert family | ringer mode | change | 8 | preempted | n/a | compact | platform (T1–T2, capability-dependent) |
| VibrateModeCompact | system HUD | Compact | glyph+state | 36–37 | ringer mode | change | 8 | preempted | n/a | idle | platform (T1–T2, capability-dependent) |
| NormalModeCompact | system HUD | Compact | glyph+state | 36–37 | ringer mode | none/change | 8 | preempted | n/a | idle | platform (T1–T2, capability-dependent) |
| RingerVolumeCompact | system HUD | Compact | glyph+level ring | 36–37 | volume stream | none | 8 | preempted | n/a | idle | platform (T1–T2, capability-dependent) |
| MediaVolumeCompact | system HUD | Compact | glyph+level ring | 36–37 | volume stream | none | 8 | preempted | n/a | idle | platform (T1–T2, capability-dependent) |
| BluetoothConnected | system HUD | Compact | device glyph+name+ring | 36–37/86 | BT profile | disconnect if capable | 8 | preempted | device name opt. redact | idle | T2 |
| BluetoothConnecting | system HUD | Compact | glyph+progress ring | 36–37 | BT state | cancel | 8 | preempted | n/a | idle | T2 |
| ActionControlExpanded | system | Expanded | control grid | alert family | toggle capabilities | toggles | 2–8 | user-owned | n/a | compact | T1 **fixture renderer** — the Idle → Quick Controls product flow (UX-C01) is specified and **not** unsupported; T2 integration pending |
| PremiumExpiryExpanded | app promo | Expanded | message+CTA | alert family | subscription state | open settings | 7 (never above 1–6) | never interrupts urgent | n/a | none | T2 (policy-limited) |
| ChargingCompact | power | Compact | bolt+percent | 36–37 | battery | none | 8 | preempted | n/a | idle | T2 |
| ChargingExpanded | power | Expanded | percent+details | alert family | battery | battery settings | 8 | preempted | n/a | compact | T2 |
| LowBatteryCompact | power | Compact | bolt+percent (red if critical) | 36–37 | battery | none | 8 | preempted | n/a | idle | T2 |
| LowBatteryExpanded | power | Expanded | percent+CTA | alert family | battery | battery saver | 8 | preempted | n/a | compact | T2 |
| TimerCompact | timer | Compact | ring+tabular time | 36–37/large-compact | own timer | pause/stop if owner | 4–6 | preempted by call | n/a | idle | T2 |
| TimerExpanded | timer | Expanded | big time+controls | 367×142–162 | own timer | pause/stop | 4 | preempts ordinary | n/a | compact | T2 |
| DeliveryCompact | live activity | Compact | glyph+milestone | 36–37 | delivery source | expand | 7 | child rules | address redact | idle | T1 fixture / ref until real source |
| DeliveryExpanded | live activity | Expanded | timeline+CTA | 367×142–162 | delivery source | contact/track | 5–7 | user-owned | address redact | compact | T1 fixture / ref until real source |
| FlightCompact | live activity | Compact | code+status | 36–37 | flight source | expand | 7 | child rules | n/a | idle | ref T1 |
| FlightExpanded | live activity | Expanded | §9.G flight | 367×142–162 | flight source | gate/status actions | 5–7 | user-owned | n/a | compact | ref T1 |
| SportsCompact | live activity | Compact | teams+score | 36–37 | sports source (no cricket) | expand | 7 | child rules | n/a | idle | ref T1 |
| SportsExpanded | live activity | Expanded | score+state | 367×142–162 | sports source | none/refresh | 5–7 | user-owned | n/a | compact | ref T1 |
| NavigationCompact | navigation | Compact | arrow+distance | 36–37/large-compact | nav source | expand | 4–6 | preempts ordinary | destinations redact | idle | platform (T1–T2, capability-dependent) |
| VoiceMemoExpanded | recording | Expanded | waveform+time+stop | 367×142–162 | recorder session | stop/pause | 6 | preempted by call | n/a | compact | ref T1 (Android recorder equivalent) |
| ScreenRecordingExpanded | recording | Expanded | red dot+time+stop | 367×142–162 | recorder state | stop | 6 | survives ordinary notif. | implies privacy mode | compact | platform (T1–T2, capability-dependent) |
| ShortcutExpanded | automation | Expanded | glyph+progress+check | alert family | shortcut run | open app | 7 | ordinary | n/a | compact | ref T1 |
| FocusModeExpanded | focus | Expanded | moon+state+CTA | alert family | DND/focus state | toggle if capable | 8 | preempted | n/a | compact | platform (T1–T2, capability-dependent) |
| AirPodsConnected | audio | Compact/Expanded | glyph+name+ring | 36–37/95 card | BT audio | none | 8 | preempted | device name opt. | idle | ref T1 (Android BT equivalent) |
| SatelliteConnected | connectivity | Compact | ring glyph+status | 36–37/86 | satellite source | message | 6–7 | child rules | n/a | idle | ref T1 |
| FindMyAlert | alert | Compact | concentric ring+title | 36–37/86 | find-source | locate | 7 | ordinary | location redact | idle | ref T1 |
| MovedToIPhone | audio | Compact | glyph+grey line+undo pill | 36–37/86 | BT handoff event | undo if capable | 8 | preempted | n/a | idle | ref T1 |
| VideoRemoteExpanded | media | Expanded | §9.C video variant | 367×177 | media session (TV) | transport, output | 6 | preempt→restore | n/a | compact | ref T1 |
| AirDropActivity | transfer | Compact | rings+source+ring | 36–37/86 | share/nearby source | accept/decline | 6–7 | ordinary | sender redact opt. | idle | ref T1 (Android share equivalent) |
| TimerImage | timer | Compact | art/thumb+time | 36–37 | own timer | pause/stop | 4–6 | per timer | n/a | TimerCompact | T1 fixture (visual variant) |
| NotificationImage | notification | Compact/Expanded | thumb+title | 36–37/126 card | notification image | expand | 7 | child rules | image redacted on lock | text-only | T1 fixture (visual variant) |
| CallAvatars | call | Compact/Expanded | multi-avatar header | 86/166 | conference state | answer/decline | 3 | preempts | redact | CallCompact | T1 fixture (visual variant) |
| TransportActivity | live activity | Compact | vehicle thumb+eta | 36–37 | ride source | expand | 7 | child rules | plate/driver redact | idle | ref T1 |
| FlightTrackerExpanded | live activity | Expanded | §9.G flight + map row | 367×162 | flight source | actions | 5–7 | user-owned | n/a | FlightCompact | ref T1 |
| SubscriptionPricing | app promo | Expanded | pricing+CTA | alert family | subscription data | open | 7 (never above 1–6) | never interrupts | n/a | none | blocked (promotion policy) |
| MovieCard | content | Expanded | poster+title+CTA | 126–162 card | content source | open | 7 | ordinary | n/a | compact | ref T1 |
| ColorOptions | debug | Expanded | swatch row | alert family | none | select | n/a | n/a | n/a | n/a | T1 fixture |
| AirplaneAlert | alert | Expanded | plane glyph+2-line title+CTA | 367×148 | connectivity state | open settings | 4–8 | preempted by call | n/a | compact | ref T1 (trigger platform-dependent) |
| ScreenMirroringAlert | alert | Expanded | glyph+sub+trailing+tinted CTA | 367×144 | cast state | stop mirroring | 6–8 | preempted | implies privacy | compact | platform (T1–T2, capability-dependent) |
| MobileDataAlert | alert | Expanded | glyph+body+2 CTAs | 367×162 | connectivity state | OK/Settings | 4–8 | preempted | n/a | compact | ref T1 |
| TransitRouteAlert | alert | Expanded | train glyph+title+destructive CTA | 367×142 | transit source | End Route | 5–7 | user-owned | route redact opt. | compact | ref T1 |

Notes: enum existence ≠ integration; renderer existence ≠ device verification; unit tests ≠ production verification (**[PLATFORM]**, §16.1). Sports fixtures exclude cricket (**[OWNER]**). `ref T1` states may ship only with a genuine supported source or as clearly-labeled Playground fixtures. **This matrix does not attribute state-specific T4, T5 or T6 evidence.** Task 05/06 reports recorded 14 device/instrumentation passes for bounded Playground/application-overlay scenarios, but those results do not verify every enum row and do not verify production `TYPE_ACCESSIBILITY_OVERLAY`, which remains BLOCKED (§3.1). Link each real device test explicitly before assigning any state a T4 tier.

---

## 17. Capability and platform-safety contract **[PLATFORM]**

Actions render only when the matching real capability exists:
`canOpenApp` (launchable package/intent), `canReply` (notification `RemoteInput` present — **not universal**), `canDismissOS` (explicit user setting + clearable notification + a real **`NotificationListenerService`** grant — cancellation is `cancelNotification(key)` on the bound listener; an arbitrary `NotificationManager` instance does **not** give this app permission to dismiss another application's notification), `canMediaControl` (active `MediaSession` with the action), `canCallControl` (telecom role/dialer-supported action), `canPauseTimer` (timer owned by this app), `canShowSensitiveContent` (user privacy policy permits it **and** reliable classification does not force redaction — lock state is one trigger, not the whole policy; §20.3).
Rules: media actions bind to real sessions; call controls vary by dialer/role/source — hide unavailable ones; timer controls never pretend to drive unrelated apps; Apple-specific demonstrations (AirDrop, AirPods, Find My) need honest Android equivalents or stay `ref T1`; flight/sports/delivery data only from supported sources — no invented live feeds; premium-expiry and promotions never interrupt urgent tasks (priority ≥7 only) and never disguise themselves as system warnings; invalid `PendingIntent`/action → visible graceful failure, no crash, no silent retry (§22 case 32).
**OS-cancellation path [PLATFORM]:** every OS-dismissal code path must go through the bound `NotificationListenerService`. If the listener is unbound, revoked or the notification is not clearable, `canDismissOS` is **false** and `×` degrades to Island-only removal (UX-C06) — never fall back to a `NotificationManager` call aimed at another app's notification (§22 case 63).

---

## 18. Gesture ownership contract **[OWNER]/[ARCH]**

| Context | Gesture | Result |
|---|---|---|
| Idle | tap | Quick Controls (C01) |
| Idle | long press | settings / calibration |
| Idle/compact | swipe-up | **[ARCH] decided — Master Specification default:** non-destructive **no-op or subtle squeeze**. The earlier decision register's recommended temporary-hide option is **superseded**; apply this default without asking the owner again. A configurable temporary-hide may exist where safe, but it must **never** become the mandatory default. Nothing is destroyed, hidden-by-default or lost |
| Compact, 1 notification | tap | expand detail (C02) |
| Compact, ≥2 | tap | newest-first pager (C03) |
| Compact music/call/timer/nav | tap | corresponding supported activity surface |
| Pager open | horizontal swipe | browse pages only |
| Expanded | swipe-up | non-destructive collapse; being a **deliberate collapse**, visible children merge/exit and their timer callbacks retire (PD-005, §14) |
| Child | tap | promote (C12) |
| Child | touch-and-hold | pause that child's timer; release → remaining lifetime + ≈2 s grace (C15/C16, §12.1) |
| Reply composer | any key/drag | typing and draft protected; pager gestures not captured |
| Outside tap / Back | — | safe collapse when platform supports; never dismissal. **During inline reply:** the unsent draft is protected via short-lived in-memory retention (§12.1) — never auto-sent, never silently discarded |
| Any | double tap | single logical dispatch; no duplicate/destructive repeat |
| Incoming call | answer/decline | exactly one dispatch |
| Empty capsule background | tap | safe no-op or Quick Controls per context |

Pointer rules: respect touch slop; diagonal drags resolve to the dominant axis once; the original pointer-down target owns the gesture through morphs (no retargeting mid-animation, §22 case 31); every gesture has a TalkBack alternative action (§20).

---

## 19. Motion and animation ownership contract

### 19.1 Independently controlled, coordinated channels
Main size morph; corner-radius morph (family lookup targets); content opacity/position transitions per slot anchor; pager swipe physics; child emergence; newest-first child reordering (shift with identity); child merge; child→main promotion; priority preemption and restoration. Each channel is separately interruptible and correlated with stable event/identity tokens (generation tokens from the engine).
**Trial values [PROPOSED, tunable]:** size spring `dampingRatio 0.82, stiffness 380`; content cross-fade 180–220 ms; child emergence 260–320 ms with 40–80 ms stagger; promotion 300–360 ms. These are implementation defaults, **not** values measured from static SVGs.

### 19.2 Measure-then-morph **[DERIVED]**
Content is measured (`onSizeChanged`/`SubcomposeLayout`) before the surface animates to its target; radius animates to the family value; slot anchors interpolate from declared transition anchors (§4.3).

### 19.3 Reduced motion **[ARCH]**
Reduced-motion equivalents: opacity/scale-substituted transitions, no stagger, no bounce; all information states still reachable.

### 19.4 Safety invariants **[OWNER]/[ARCH]**
No expired timer dismisses a refreshed child; no animation completion removes an OS notification; no new arrival resets unrelated child timers; no duplicate actions or accidental taps during morphs (pointer ownership §18); preemption never leaves a half-morphed surface (atomic channel set per event token).

---

## 20. Accessibility and privacy contract

### 20.1 Semantics **[ARCH]**
Decorative glyphs carry no independent semantics (no duplicate announcements); visual children merge into one logical control via `semantics(mergeDescendants = true)` with a complete spoken string (e.g. "Recording, 6 seconds, stop"); real button roles and `stateDescription`; pager exposes explicit next/previous actions plus the selected item's identity and "page k of N" (the number may shift while the selection stays, §13); Mini-Island overflow is an accessible control announcing pending-arrival count **without message bodies**, and **when no child card and no overflow chip fit safely the overflow stays reachable through the Main Pager or an explicit semantic accessibility action** (§14) — an off-screen `+N` chip is never acceptable; TalkBack focus survives animated transitions (stable keys, no node churn); arrival announcements state counts, never sensitive content.

### 20.2 Hit targets and interception **[PROPOSED]/[PLATFORM]**
≥48 dp where feasible; invisible padding must not enlarge unsafe overlay interception (§3.1); dense control rows document exceptions. Touch behaviour must be reasoned about per **class** (§3.1): outside the window rectangle, inside the rectangle but in a transparent gutter, and on an interactive Island surface — each needs its own device evidence, and none may be assumed to pass through.

### 20.3 Text scaling and layout stress **[ARCH]**
fontScale 1.0/1.3/1.5/2.0: measure → grow within family height budget → drop secondary metadata/action count → never clip critical labels; 320 dp narrow screen; 393/430 dp references; landscape and split-screen; left/center/right punch and waterdrop cutouts; RTL (mirror reading order, keep numeric rails and nav direction physical); reduced motion; lock/unlock transitions; **OTP / financial / private-message previews redacted whenever reliable classification plus the user's privacy settings require it — not only when the screen is locked** (lock state is one trigger among several, §17 `canShowSensitiveContent`); screen recording/mirroring implies confidentiality treatment; system DND suppresses low-priority auto previews (§15.1 passive indication instead); battery saver and OEM overlay limits; IME open during child animation; low available height where no child card and no overflow chip fit safely (§14 low-height fallback); permission revoked and service restarted.
**Privacy restrictions always outrank decorative reference fidelity.**

### 20.4 Non-color signals **[ARCH]**
Every status conveyed by colour also has icon/text form; recording has waveform+time; connected has glyph+word; destructive has label.

---

## 21. Persona acceptance matrix **[ARCH]**

### 21.1 Quick Glance User
Goals: read essentials in ≤2 s, auto-collapse without loss. Patterns: transient previews, compact glances. Failure modes: previews too brief/long; collapse loses identity; expiry counted from queue entry so content is never readable. Defaults: transient preview (≈3.5 s ordinary, ≈5 s readable, cap ≈6 s; **expiry starts when content is readable**, §12.1) then compact residue; fast auto-collapse; swipe-up is a non-destructive no-op/squeeze (§18). Gestures: tap to expand when needed; swipe-up never destroys state. Recovery: compact residue re-expands to the same **selected key**. Prefs: preview duration, auto-collapse. A11y: announcements without bodies. Scenarios: (1) preview appears and collapses leaving compact metric; (2) glance timer during call shows ring only; (3) DND suppresses ordinary preview; (4) 200% text still readable compact; (5) slow-rendering preview is not truncated because expiry starts at readable (§22 case 59).

### 21.2 Focused / Deep Reader
Goals: uninterrupted reading, owned selection, draft safety. Patterns: long pager sessions on a chosen notification, reply drafts. Failure modes: selection hijack, draft loss, content swap mid-read, a real call silently buried under the editor. Defaults: **selected-key ownership** (C04, §13) — a new arrival may shift the numeric position while the key stays selected; reply editor owns interaction and content continuity but **not** call suppression (§15.3); user-expanded content has no automatic short expiry while reading (§12.1). Gestures: horizontal browse; swipe-up collapse only. Recovery: bounded post-interruption restore to the same **selected key** (≈10 s window, unlocked, source still valid — §15.2); otherwise the next valid state, never a ghost. Prefs: interruption policy. A11y: focus retention. Scenarios: (1) 13th arrival while key `msg-5` is selected keeps the key, position becomes `6 of 13`; (2) reply draft survives 3 ordinary arrivals; (3) call preempts mid-draft → call gets safe prominence, draft preserved, editor restored inside the window; (4) call ends **after** the window (or the source vanished) → next valid state, no ghost editor; (5) same-key artwork update refreshes content with neither key nor position changing.

### 21.3 Heavy Notification User
Goals: browse 50 items truthfully, reliable counts, managed overflow. Patterns: bursts, queue pruning. Failure modes: wrong `+N`, ghost items, false counts. Defaults: newest-first pager, 50-cap, tombstoned suppression. Gestures: pager browse, × Island-only. Recovery: source reconciliation after revocation. Prefs: OS-dismiss opt-in. A11y: count announcements. Scenarios: (1) 25-burst keeps `+N` = pending children only; (2) 50 retained without false counts; (3) × preserves shade; (4) dismissed item resurfaces only on meaningful change.

### 21.4 Multitasking User
Goals: music+nav+call+timer coexist with sane priority. Patterns: concurrent activities, companions, media playing while messages arrive. Failure modes: wrong preemption, lost restoration, media controls replaced by a message, a vertical child stack appearing under a compact main. Defaults: §15 precedence; **PD-026 media policy (§15.1)** — compact media + ordinary message while not interacting = brief transient preview (≈3.5 s) then compact media restored with badge/count or companion; user-expanded media = controls never replaced, Mini-Islands below; active media interaction = focus protected; preview disabled or DND/privacy = passive compact indication. Companion bubble for a secondary activity. Children only under an occupied eligible expanded main (§14). Gestures: per-context taps. Recovery: restoration matrix §15 + bounded policy §15.2. Prefs: priority overrides none (safety first); auto-preview toggle respected. A11y: state descriptions per activity. Scenarios: (1) music+nav turn-now preempts then restores; (2) timer+call then timer returns; (3) recording survives BT event; (4) two activities → main + companion bubble; (5) compact music + ordinary message → ≈3.5 s preview then compact music with badge/count, **no child stack**; (6) music expanded by user + 3 messages → controls intact, Mini-Islands below; (7) auto-preview disabled / DND → passive compact indication only; (8) user scrubbing media when a message arrives → focus protected, notification deferred and indicated on settle.

### 21.5 Power User
Goals: Quick Controls utility, calibration, explicit OS-dismiss, predictable gestures. Patterns: idle long-press calibration, settings tuning. Failure modes: hidden settings, gesture ambiguity, a "hide" behavior silently becoming the default. Defaults: Quick Controls on idle tap (UX-C01); **swipe-up default = non-destructive no-op / subtle squeeze (§18, decided)** with an optional configurable temporary-hide where safe that is never mandatory. Gestures: full §18 contract. Recovery: teardown/restart paths. Prefs: OS-dismiss (**NotificationListener-based**, §17), child lifetimes and preview durations (§12.1), swipe-up mode. A11y: n/a beyond standard. Scenarios: (1) idle tap → Quick Controls; (2) long-press → calibration; (3) OS-dismiss opt-in actually cancels via the bound listener, and degrades to Island-only removal when unbound; (4) swipe-up config respected while the shipped default stays non-destructive; (5) tuned §12.1 timings are honored without visual-spec changes.

### 21.6 Privacy / Accessibility User
Goals: confidentiality on lock **and** whenever policy requires it, full TalkBack/RTL/large-text/one-handed support. Patterns: locked glances, TalkBack navigation, one-handed reach, OTP and banking notifications. Failure modes: body leakage, duplicate announcements, unreachable controls, redaction applied only on the lock screen. Defaults: **OTP/financial/private previews redacted whenever reliable classification plus privacy settings require it — lock state is one trigger, not the whole policy** (§20.3, §17 `canShowSensitiveContent`); merged semantics; one-handed: controls within reachable band, child stack biased to top and promotable via a11y actions, with overflow reachable through the Main Pager or a semantic action when no chip fits (§14). Gestures: all with TalkBack alternatives. Recovery: permission revocation leaves clean state. Prefs: sensitive-content policy. A11y: §20 entire. Scenarios: (1) OTP redacted on lock **and** redacted while unlocked when classification/policy requires it, full content only when policy allows; (2) TalkBack reads merged control once; (3) RTL pager + nav arrows correct; (4) one-handed promotion of child via a11y action; (5) low-height device where no child and no chip fit — overflow still reachable via pager/a11y action, nothing drawn off-screen.

---

## 22. Interaction scenario matrix (verification)

Columns: Initial | Event | Expected Main | Expected Child | Queue invariant | Gesture/action effect | Accessibility result | Verification (UNIT = engine/unit-testable, DEVICE = real Android validation, VISUAL = screenshot-diff vs reference).

| # | Initial | Event | Main | Child | Queue | Action effect | A11y | Verify |
|---|---|---|---|---|---|---|---|---|
| 1 | Idle, 0 notif | tap | Quick Controls | none | empty | controls open | announced | UNIT+DEVICE |
| 2 | 1 notif compact | tap | expanded detail | none | 1 | expand | merged string | UNIT+VISUAL |
| 3 | 12 notif | tap | pager newest | none | 12 | `1 of 12` | page announced | UNIT |
| 4 | pager open, selected key `msg-5` at position 5 | 13th arrives, inserted ahead | **same key `msg-5` stays selected**; position becomes 6; label `6 of 13`; subtle New indication; no viewport jump | optional child (§14 precondition: user-owned expanded main) | 13 | New badge; selected key untouched | "6 of 13" + title announced | UNIT |
| 5 | pager open | selected source removed | next/prev | unchanged | n−1 | no jump | re-announce | UNIT |
| 6 | expanded | × | Island-removed | unchanged | n−1 | shade preserved | announced | UNIT+DEVICE |
| 7 | last Island notification, children pending | **source removes** it, no live activity | Idle Compact | **all children merge/exit — no orphan draws after the transition** | 0 (entry gone; contrast C06 Island-only suppression, which leaves the shade entry) | idle tap→Quick Controls; retired callbacks cannot re-expand | idle announced | UNIT |
| 8 | dismissed key | minor update | stays suppressed | none | unchanged | none | none | UNIT |
| 9 | dismissed delivery, **Main compact** | major milestone (meaningful) | **transient main preview (≈3.5–5 s) → restore compact + badge/count** | **none — child precondition unmet on a compact main (§14); no child stack required** | +1 → **13 eligible after append** | preview then settle; count truthful | milestone announced without body | UNIT |
| 10 | main expanded | 1 new | unchanged | 1 child | +1 | reading intact | child announced | UNIT+VISUAL |
| 11 | main expanded | 3 new | unchanged | ≤3 visible + overflow | +3 | overflow `+N` | overflow accessible | UNIT+VISUAL |
| 12 | idle | burst 25 | compact/pager truth | budget-limited | 25 | counts truthful | counts only | UNIT |
| 13 | children + queue 12 | inspect `+N` | unchanged | `+N`=pending children | 12 | no queue leak | correct count | UNIT |
| 14 | child visible | tap child | promoted expanded | promoted leaves | selection=child | pager keeps old main | promotion announced | UNIT+VISUAL |
| 15 | child TOUCH_HELD | stale timeout fires | unchanged | child alive | unchanged | no dismissal | n/a | UNIT |
| 16 | main expanded + 2 visible children | **deliberate** user collapse (PD-005) | compact/idle | **children merge or safely exit; nothing draws under the compact surface** | unchanged — eligible notifications remain in the canonical queue | visual timer callbacks cancelled/invalidated against generation tokens; stale callbacks never re-expand or alter the selected key | no orphan announcement | UNIT |
| 17 | pager open, selected key `msg-5` | incoming ringing call | **call takes the main surface with safe prominence** (never hidden behind the pager/editor) | children pause/hide (not retired) per §14 | unchanged; selected key retained for restoration | exactly one answer/decline dispatch; no fabricated OS call UI (§17) | call announced | UNIT+DEVICE |
| 18 | call ends **inside** the bounded window (≈10 s), unlocked, source still valid | restore | pager reopens on the **same selected key** (numeric position may differ); label truthful | valid children return; expired ones do not resurrect | unchanged | selection identity intact (§15.2) | focus restored, key announced | UNIT |
| 19 | music **expanded by the user** | ordinary notification | music controls stay — **never replaced** | Mini-Island below (§14 precondition met) | +1 | transport still works | music merged string | UNIT+VISUAL |
| 20 | nav compact | turn-now during preview | nav preempts | preview deferred | unchanged | nav voice/visual | nav announced | UNIT |
| 21 | reply editor open | notif arrives | editor kept | child | +1 | draft intact | editor focus | UNIT+DEVICE |
| 22 | child emerging | IME opens | unchanged | animation safe | unchanged | no crash/overlap | n/a | DEVICE |
| 23 | child PROMOTING | source disappears | next/prev valid | cleaned | n−1 | no crash | re-announce | UNIT |
| 24 | locked | sensitive notif | redacted | redacted child | unchanged | no body leak | "new notification" only | DEVICE |
| 25 | expanded | notification access revoked | safe idle | cleared | reconciled | no ghosts | state announced | DEVICE |
| 26 | running | accessibility service stopped/restarted | clean restore | rebuilt from queue | intact | no corruption | announced | DEVICE |
| 27 | any | fontScale 2.0 | grown/truncated secondary | readable | unchanged | no clip of critical | full strings | VISUAL+DEVICE |
| 28 | RTL | nav + pager use | mirrored reading, physical rails/arrows | mirrored | unchanged | gestures correct | announcements | VISUAL+DEVICE |
| 29 | 320 dp / low available height | children arrive and **neither a child card nor an overflow chip fits safely** | main only | deferred; **no off-screen `+N` chip drawn**, nothing painted over other apps | pending counted | no app blocking; touches in the window's transparent gutters must not be silently swallowed (§3.1 class b) | overflow reachable via **Main Pager or semantic accessibility action** (§14, §20.1) | DEVICE |
| 30 | overlay up | touch **outside the overlay window rectangle** (§3.1 class a) | unchanged | unchanged | unchanged | app below receives it **only where `FLAG_NOT_TOUCH_MODAL` is honored** on that device/API | n/a | DEVICE (BLOCKED until hw evidence) |
| 31 | mid-morph | user tap | original target owns | unchanged | unchanged | no retarget/dup | single announce | UNIT+DEVICE |
| 32 | expanded action | PendingIntent invalid | graceful failure state | unchanged | unchanged | no crash/retry loop | failure announced | UNIT+DEVICE |
| 33 | notif without RemoteInput | reply UI | reply hidden | n/a | unchanged | no fake reply | n/a | UNIT |
| 34 | DND on | ordinary notif | no auto preview | suppressed child | queued | silence respected | none | UNIT+DEVICE |
| 35 | active call | charging HUD | call kept | HUD child/deferred | unchanged | call priority | call string | UNIT |
| 36 | recording | ordinary notif | recording survives | child | +1 | indicator intact | recording announced | UNIT+VISUAL |
| 37 | pager reading key `msg-5` | same-key artwork/content update | content refreshes in place — **selected key and numeric position both unchanged**, no new page | unchanged | unchanged | no re-selection, no scroll | update announced without body | UNIT |
| 38 | queue 50 | 51st arrives | truthful counts | budget | cap 50, eviction policy | no false counts | counts | UNIT |
| 39 | Playground | engine callbacks | parity with production render | parity | parity | no real OS actions | n/a | UNIT |
| 40 | revoked+restored | process restoration | reconciled presentation | rebuilt | intact | no corruption | announced | DEVICE |
| 41 | Quick Glance persona | preview expiry | compact residue | none | unchanged | residue re-expands | preview announced | UNIT+VISUAL |
| 42 | reply editor open, unsent draft | ringing call arrives | **call gets safe main prominence** (§15.3) — never hidden behind the editor; editor suspends | per policy | unchanged | draft + composer session preserved in memory; **never auto-sent, cancelled or discarded**; no fabricated answer/decline controls | call announced; draft content not read aloud | UNIT+DEVICE |
| 43 | Heavy persona | 25-burst then prune | truthful pager | overflow correct | ≤50 | counts stable | counts | UNIT |
| 44 | Multitask persona | music+nav+timer+call sequence | per §15 | companions correct | unchanged | restorations correct | per-activity strings | UNIT+VISUAL |
| 45 | Power persona | swipe-up on idle/compact + config variants | **default = non-destructive no-op / subtle squeeze (§18)**; configured hide honored where enabled, never mandatory | unchanged | unchanged | no destructive default; no state loss | announced | UNIT+DEVICE |
| 46 | Privacy persona | lock + OTP + TalkBack | redacted | redacted | unchanged | no leakage | generic announce | DEVICE |
| 47 | one-handed | child promotion via a11y action | promoted | promoted leaves | selection=child | a11y path works | action announced | DEVICE |
| 48 | split-screen/landscape | any presentation | clamped to safe area | budget recomputed | unchanged | no overlap | n/a | DEVICE |
| 49 | music **compact**, user not interacting | eligible ordinary message (PD-026) | **brief transient preview ≈3.5 s over the compact surface → compact music restored with badge/count or companion** | **none — a vertical child stack under a compact main is not the default (§15.1)** | +1 | preview expiry starts when content is readable | preview announced without body | UNIT+VISUAL |
| 50 | music compact, **auto-preview disabled or DND/privacy suppressing** | eligible ordinary message | **passive compact indication only** (badge/count/companion); no preview | none | +1 | silence/disabled preference respected | count-only announcement | UNIT+DEVICE |
| 51 | music compact, **user actively interacting** (scrub/transport/output) | ordinary message | music kept; **focus protected**, no preview swap | none | +1 | notification deferred, indicated passively on settle | deferred; count on settle | UNIT |
| 52 | music **expanded by user** | 3 ordinary messages | controls never replaced | Mini-Islands below; budget 1–2 small / ≤3 large; overflow per §14 | +3 | interaction intact | children announced count-only | UNIT+VISUAL |
| 53 | main expanded + 2 children, one `TOUCH_HELD` | **deliberate collapse during child touch/animation** | compact/idle atomically | children retire mid-animation without glitch; the held child's paused timer is **cancelled, not resumed** | unchanged | generation tokens invalidated; stale callback cannot re-expand or alter the selected key | no orphan announcement | UNIT |
| 54 | reply editor with unsent draft | **outside tap / Back** | safe collapse | unchanged | unchanged | **draft protected** via short-lived in-memory retention (§12.1); never auto-sent, never silently discarded | draft content not announced | UNIT+DEVICE |
| 55 | reply editor open → ringing call | call ends **after** the ≈10 s bounded window | next valid state (compact / queue / idle) — **no ghost card**, no stale editor | per policy | unchanged | draft expires with the retention policy; no auto-send | state announced | UNIT |
| 56 | reply editor open → ringing call | call ends inside window but the notification was **removed** or **privacy state changed** | restore the **next valid** presentation, not the ghost editor | per policy | n−1 | no crash, no fabricated restore | re-announce | UNIT |
| 57 | Main **compact**, 12 queued | meaningful delivery milestone resurfaces (C10) | transient main preview → collapse to compact with truthful count | **no child stack required when compact** | **13 eligible** | C10 satisfied without children | "13 notifications" announced | UNIT |
| 58 | suppressed source | **routine** stop decrement / ETA jitter | stays suppressed — not meaningful (§11) | none | unchanged | tombstone retained | none | UNIT |
| 59 | preview content slow to render | preview timer evaluation | expiry begins when content is **readable/settled**, not at queue entry (§12.1) | unchanged | unchanged | no truncated unreadable preview | full string announced | UNIT |
| 60 | user-expanded notification | user reads for 90 s | **no automatic short expiry while the user is reading** | unchanged | unchanged | collapse only on user action or preemption (§15) | unchanged | UNIT |
| 61 | overlay up | touch **inside the window rectangle but in a transparent gutter** (§3.1 class b) | unchanged | unchanged | unchanged | **intercepted by default** unless the window is resized to the visible surface or a proven touch-region mechanism excludes it | n/a | DEVICE (BLOCKED until hw evidence) |
| 62 | **unlocked** device, privacy setting/classification requires redaction | OTP or financial notification arrives | **redacted preview** — classification + policy driven, not lock-only (§20.3) | redacted child | unchanged | no code/amount leak | generic announcement, no digits | UNIT+DEVICE |
| 63 | `canDismissOS` false (listener unbound or notification not clearable) | × action | Island-only removal; **no `NotificationManager` attempt against another app's notification** (§17) | unchanged | n−1 in the Island queue only | Android shade preserved | announced | UNIT+DEVICE |
| 64 | low available height, children pending | no child card and no overflow chip fits | main only | deferred | pending counted | overflow reachable via **Main Pager or semantic accessibility action**; nothing off-screen (§14, §20.1) | action announced | DEVICE |

DEVICE rows citing overlay touch pass-through or accessibility-service behavior remain **BLOCKED** until hardware evidence exists (§3.1); UNIT/VISUAL rows may pass in CI/screenshot diff. **No row in this matrix constitutes universal T4–T6 verification** (§16.1): UNIT/VISUAL are not production-overlay evidence; actual logged device tests may substantiate their specific T4 scope, but a production-overlay DEVICE row may not be reported PASS while the accessibility service remains unbound.

---

## 23. Compose implementation contract

1. **Module shape:** `island/` → `IslandHost` (window owner, §3.1), `IslandSurface` (silhouette + morph channels §19), `slots/` (`LeadingSlot`, `CenterSlot`, `TrailingSlot`, `BottomSlot`, optional per §4.3), `families/` (one file per §9 family), `tokens/` (`IslandColors`, `IslandDimens` incl. radius lookup, `IslandType`), `model/` (`IslandPresentationState` **optional new UI-only projection** mapped from existing `IslandBehaviorState` StateFlow; per-family data classes carrying only rendered fields), `pager/`, `miniisland/`, `quickcontrols/`.
2. **Presentation-only:** composables observe `IslandStateManager.behaviorPresentation` (`StateFlow<IslandBehaviorState>`) or an explicit, tested UI-only projection; they never mutate queue/suppression/timers; all side effects go through engine intents (§3.2).
3. **Window/insets:** per §3.1 and §5; cutout rect drives anchoring; compact height = `max(cutoutHeight, 36.dp)`.
4. **Layout:** `Row`/`Column`/`Box` + `weight`/`Arrangement`; `Bottom` control rows use equal weights with measured pitch gaps; scrubber = `Canvas` rail + drag with fixed-width time boxes; maneuver strip = weighted row with highlighted next item.
5. **Text:** roles per §7, `maxLines`, end ellipsis, `tnum` for numerals; no emoji/outlined text.
6. **RTL/a11y/privacy:** per §4.3, §20.
7. **Previews/tests:** per-family screenshot diff vs reference specimen at 100% scale; fontScale 2 and RTL previews; engine unit tests for §22 UNIT rows; device checklist for DEVICE rows (BLOCKED items labeled).

---

## 24. Quality gates and release-blocking invariants

Gates (per family before ship): no clipped/ellipsized essential text at 1.0–2.0; slot gravities stable under text change; controls semantic and capability-backed; radii/paddings within ±1u of §6; cutout coupling verified left/center/right punch; hit targets per §6.3; screenshot diff vs reference; TalkBack complete sentence per state.

**Release blockers [OWNER]/[ARCH]:** no ghost notifications after source removal; no accidental Android notification cancellation (and no `NotificationManager` path against another app's notification, §17); no hijacking of the user's **selected notification identity** — a shifted numeric position with the same key selected is correct behavior, not a bug (§13); no incorrect child overflow counts; no stale timer callbacks changing selection or re-expanding the Island; no duplicate action dispatch; no lost inline-reply drafts during ordinary arrivals, outside taps, Back, or call preemption (and no auto-sent or silently discarded draft); **no child Mini-Island still drawing after a deliberate Main collapse** (PD-005, §14); **no legitimate ringing call hidden behind an inline-reply editor** (§15.3); **no vertical Mini-Island stack under a compact main used as the default media/notification behavior** (PD-026, §15.1); no private data leakage — on lock **and** whenever classification/privacy policy requires redaction (§20.3); no unsafe overlay touch interception, with each of the three touch classes evidenced separately (§3.1); no unverified platform calls/actions; no corrupted state after permission withdrawal; **no fabricated verification tier — no state described as device-verified, overlay-verified or release-ready without T4–T6 evidence (§16.1)**; **no production-ready claim without genuine production accessibility-overlay testing (currently BLOCKED)**; **no universal expanded side-margin rule applied across reference families** (§5); no source/render divergence between production engine and Playground.

---

## 25. Explicit rejections (do not regress) **[OWNER]**
The 39/108-state bulk SVG packs as a design system; uniform 145-high live-activity card; title-top-left + button-row-bottom generic cards; "same card with changed title" component architecture; emoji glyphs / outlined-path text / invented fonts; global gradients/aurora decoration; fake camera hole as content; fixed 367-wide compact pills; r=h/2 on tall cards; red as generic decoration; invented actions or platform privileges; equating the 60-enum with 60 finished integrations; **numeric page index as selection identity**; **vertical Mini-Island stacks under a compact main as the default media/notification behavior**; **children left drawing (or their timers left running) after a deliberate Main collapse**; **a legitimate ringing call suppressed by reply-editor ownership**; **one universal expanded side margin across reference families**; **`NotificationManager`-based dismissal of another app's notification**; **preview expiry timed from queue entry instead of readable content**; **describing the owner Minimal reference capsule as an already-shipped runtime implementation**; **treating enum, renderer or unit-test existence as production verification**.

---

## 26. Changelog — V4.0 → V4.1 (resolved conflicts)

| # | Conflict in V4.0 | Resolution | Original § | New § |
|---|---|---|---|---|
| 1 | No source-of-truth hierarchy; all values read as "measured" | Hierarchy + 6-label classification added | §0 | §0.1–0.2 |
| 2 | Minimal identity ambiguous (glyph atlas vs capsule) | Five-object taxonomy; owner 108×32 capsule restored as specimen; cutout-adaptation rule | §7.A, §3 | §2, §9.A |
| 3 | Prescribed `TYPE_APPLICATION_OVERLAY` + `ALWAYS` cutout mode | Production accessibility-overlay contract restored; debug overlay optional; SHORT_EDGES baseline; hw validation BLOCKED | §9.1–9.2 | §3.1 |
| 4 | Implied competing state machine | Presentation-layer-above-engine rule; 11 orthogonal state dimensions; Playground parity | §9 | §3.2 |
| 5 | UX decisions missing | Full UX-C01–C16 contract table | (absent) | §11 |
| 6 | Notification lifecycle/pager/mini-island rules thin | Lifecycle behaviors, queue invariants, pager contract, child lifecycle + trial timers | §7.I | §12–14 |
| 7 | No priority/restoration contract | 9-level precedence + 12 interruption/restoration cases | (absent) | §15 |
| 8 | No state inventory | 60-state coverage matrix with status labels | (absent) | §16 |
| 9 | No capability contract | `can*` capability matrix + platform-safety rules | §9 (implicit) | §17 |
| 10 | No gesture contract; swipe-up governance unclear | Gesture table + pointer-ownership + governance note (configurable pending confirmation) | (absent) | §18 |
| 11 | "Red only destructive" conflicted with recording + owner capsule; single-accent too strict | Red exception list; accent rule scoped to status accent | §0.5, §6 | §8.2 |
| 12 | Universal radius formula wrong for 86/95 etc. | Family radius lookup; pill vs card families separated; 408u clamp | §4.1 | §6.2, §5 |
| 13 | Slots read as mandatory 4-slot fill | Optional slots, nested rows, family recipes, calibration-not-offsets | §2 | §4.3 |
| 14 | Single `Animatable` motion model; springs read as measured | Multi-channel interruptible token-correlated motion; springs labeled tunable proposals | §9.4 | §19 |
| 15 | "contentDescription on every glyph" over-announces | Merged semantics, roles, pager actions, overflow, count-only announcements | §0.8, §9.8 | §20.1 |
| 16 | Live Activity expanded forbidden beyond trimmed headers | Island-native expanded layouts for genuine-source activities; dashboards stay separate | §7.G | §9.G |
| 17 | No scenario matrix / blockers | 48-scenario matrix with UNIT/DEVICE/VISUAL methods; 13 release blockers | §10 | §22, §24 |
| 18 | No personas | Six-persona matrix with 4+ scenarios each, one-handed cross-cutting | (absent) | §21 |
| 19 | Test matrix incomplete | Full stress list incl. 320 dp, cutout variants, IME, DND, battery saver, revocation | §10 | §20.3, §22 |
| 20 | Preserved content audit | All V4 measurements retained (inventory, geometry, slots, cutout, type, controls, music/call/nav, waveform, rejections) | §1–11 | §1, §4–10, §25 |

**Self-audit notes:** radius rules now table-only (no formula conflict); red rules include recording/owner capsule; overlay type never mandated as application-overlay in production; engine ownership stated once and referenced; `+N` defined as pending child arrivals in §14 and §22 case 13; BLOCKED status appears wherever hardware evidence is missing (§3.1, §22 rows 26, 30, 40, 46); trial timers and springs labeled tunable proposals; no measurement from V4 deleted (verified against §6.1/§9 tables).

---

## 27. Changelog — V4.1 → V4.2 (final compatibility patch)

| # | Correction | V4.1 conflict | Original § | Revised § |
|---|---|---|---|---|
| 1 | **Stable key vs numeric pager position (P0)** | C04/scenarios asserted "page stays 5"; identity was ambiguous between key and index | §11 C04, §13, §21.2, §22 cases 4/37, §24 | §0.3 rule 11, §11 C04, §13 (selection identity + key-stable paging), §21.2, §22 cases 4/37, §24 blocker |
| 2 | **Compact music + ordinary notification (P0)** | "child or compact swap per priority 7 vs 6" allowed a child stack under a compact main | §15, §21.4, §22 | §15.1 (PD-026 matrix), §14 creation precondition, §11 C11, §21.4, §22 cases 19/49–52 |
| 3 | **Main collapse and child timers (P0)** | Case 16 said "children keep timers" after collapse | §14, §22 case 16 | §0.3 rule 12, §14 PD-005 bullet + `RETIRED_ON_COLLAPSE`, §18 expanded swipe-up, §22 cases 16/53 |
| 4 | **Incoming call during reply editing (P0)** | "editor owns interaction: call shown as banner/child per platform" could hide a real call | §15, §21.2, §22 cases 17/18/42 | §15 precedence 2–3, §15.2 bounded restoration, §15.3 transition table, §0.3 rule 13, §21.2, §22 cases 17/18/42/54–56 |
| 5 | **Delivery, last notification, child preconditions (P1)** | C07/C09/C10 left child/queue results ambiguous on a compact main | §11 C07/C09/C10, §14, §22 cases 7/9 | §11 C07/C09/C10/C11 + meaningful-vs-not note, §14 precondition, §22 cases 7/9/57/58 |
| 6 | **Correct production status claims (P1)** | A single Status column mixed implementation with verification; several rows read `PROD` | §16, §3 | §0.4, §3.1 verification bullet, §16.1 six-tier ladder, all 60 row labels, §16 notes, §9.I, §22 closing note, §24 blocker |
| 7 | **Restore exact behavioral defaults (P1)** | Only child 5 s / 7 s / 2 s existed; previews, readable-start expiry, restoration bounds and draft retention were missing; C16 mislabeled [OWNER]; swipe-up left "pending owner confirmation" | §§0, 12, 14, 15, 18, 21, 22 | §0.2 (PD-001 → [ARCH]), §0.4, §12.1 defaults table, §14, §15.2, §18 swipe-up + outside-tap rows, §11 governance note, §21.1/21.5, §22 cases 45/54/59/60 |
| 8A | **Owner Minimal colour authority** | `#DC0917`@0.6 downgraded to "[PROPOSED] for production" | §2, §8.1 | §2 row 1, §8.1 red family, §16 Minimal row |
| 8B | **Expanded width families** | Universal "margin 11–16u default 12" | §5 | §5 Expanded row + responsive-width policy (atlas ≈13u @393, presentation ≈11u @430, community 32u @430), §24 blocker, §25 |
| 8C | **`#64D2FF` dual role** | Listed as semantic blue *and* banned as annotation-only | §8.1 | §8.1 guide-layer row + context classification rule |
| 8D | **Low-height overflow** | Implied an `+N` chip must always render | §14, §20.1, §22 case 29 | §14 low-height fallback, §11 C13, §20.1, §21.6, §22 cases 29/64 |
| 8E | **Touch pass-through classes** | One undifferentiated "outside surface" claim | §3.1, §20.2, §22 case 30 | §3.1 classes a/b/c, §20.2, §22 cases 29/30/61 |
| 8F | **Sensitive content beyond lock** | Redaction scoped to the lock screen only | §20.3 | §17 `canShowSensitiveContent`, §20.3, §21.6, §22 case 62 |
| 8G | **OS cancellation capability** | `canDismissOS` cited a generic `NotificationManager` capability | §17 | §17 `canDismissOS` + listener-based path rule, §21.5, §22 case 63, §24 blocker |

### 27.1 Self-audit (V4.2)
| Required check | Result |
|---|---|
| All UX-C01–C16 decisions preserved | **Pass** — 16 rows intact; C04/C07/C09/C10/C11/C13 clarified, C16 relabeled [ARCH] PD-001, no decision removed |
| 60 unique source enum rows remain | **Pass** — 60 data rows, only the Status column vocabulary changed |
| Six personas, ≥4 scenarios each | **Pass** — 21.1 (5), 21.2 (5), 21.3 (4), 21.4 (8), 21.5 (5), 21.6 (5) |
| ≥48 numbered acceptance scenarios retained, corrections covered | **Pass** — 1–48 retained (12 rewritten in place), 49–64 added ⇒ 64 total |
| No page-number vs selected-key ambiguity | **Pass** — §0.3 rule 11, §11 C04, §13, §21.2, §22 cases 4/18/37, §24 |
| No compact-music vs expanded-music conflict | **Pass** — §15.1 separates the four cases; §14 precondition forbids a compact-main child stack |
| No children visually active after deliberate collapse | **Pass** — §14 PD-005, §18, §22 cases 16/53, §24 blocker |
| No hidden legitimate incoming call due to reply ownership | **Pass** — §15 precedence 2–3, §15.3, §0.3 rule 13, §22 case 42 |
| No fabricated production verification | **Pass** — §16.1 ladder; T4 unverified, T5 BLOCKED, T6 NO for every row; §22 closing note; §24 blocker |
| No incorrect universal geometry rule | **Pass** — §5 per-family margins + responsive policy; radius table unchanged (§6.2) |
| No misleading touch-pass-through assumption | **Pass** — §3.1 three classes, §20.2, §22 cases 30/61 (both BLOCKED) |
| No loss of measured typography, gravity, waveform or reference geometry | **Pass** — §1, §4.1–4.3, §5 silhouettes, §6.1–6.2, §7 (incl. 97.9/98.4 gravity calibration), §9.A–9.I, §10 all retained verbatim |

**Preserved unchanged from V4.1:** evidence inventory and probe method; five-object taxonomy structure; slot diagram numerics (408×160 r44, cutout 128×38.7, 6.5u inset, dividers x=198/x=406); slot gravity and flexibility rules; radius lookup table; control/chip sizes; typography roles and calibration; palette; families §9.A–§9.I; waveform component; motion channels and tunable springs; Compose module contract; quality gates.


---

## 28. Architect review V4.3 — conflicts resolved before Codex implementation

This section corrects ambiguous or internally inconsistent implementation claims in V4.2 **without changing the owner's measured references or approved UX behavior**. `IMPLEMENTATION` measurements are the current Android code's baseline or explicit migration targets, **not** an owner's SVG measurement. All dimensions below are a **family-specific target at its reference canvas**; responsive measurement always wins over hard-coded values.

### 28.1 Family-width resolver — correct reference canvas (P0)

The earlier `scale = screenWidthDp / 430f` applied globally is **rejected**. It compresses the `367u` atlas specimen (measured on a `393u` canvas) to ~`335dp` **on that same 393dp display**. Preserve each specimen's own reference canvas.

| Family | Reference canvas | Reference width | Per-side visual margin | Height/radius family |
|---|---:|---:|---:|---|
| Owner Minimal | independent SVG 108×32 | 108 | no asserted screen canvas | 32 / 15.9018 |
| Idle silhouette | community 430 / presentation 393 | 127 / 126 | actual cutout union | 36–36.67 / 18–18.335 |
| True compact | source family 393 or 430 | intrinsic 120–250 | protect cutout/status region | 36–37 / 18–18.335 |
| Contextual compact | atlas 393 | 367 | 13 | 86 / 43 |
| Atlas expanded | atlas 393 | 367 | 13 | 142–185.36 / 42 |
| Presentation expanded | presentation 430 | 408 | 11 | 160 / 44 |
| Community expanded | community 430 | 366 | 32 | 80–208 / 40–46 by row |
| Mini child (Android extension) | **not an owner-measured iOS SVG** | `(measuredMainW−32dp)` bounded to `[240,336]` only if it fits | 16dp sides as the initial implementation target | 50–52 / 20; gap 6 |
| Overflow chip (Android extension) | **not owner-measured** | intrinsic, constrained | centered | visual 36–44, effective target ≥44–48 where safe |

Use the following **layout policy**, not a physical pixel-scale assertion:

```kotlin
// familyRefCanvas: 393f for atlas, 430f for presentation/community.
// availableWidthDp excludes ACTUAL horizontal insets/safe bounds.
// Family-specific visual margins are reference *targets*, not extra padding twice.
val proportionalWidth = referenceSurfaceDp * (availableWidthDp / familyRefCanvas)
val targetWidthDp = minOf(
    proportionalWidth,
    availableWidthDp - 2f * scaledFamilyMarginDp
).coerceAtLeast(0f)
// At atlas baseline: 393dp screen → 367dp Island, not 335dp.
// Intrinsic compact: measure glyph + text + metric + insets separately; do not scale an expanded reference.
```

If `availableWidthDp` includes/excludes safe insets differently from the reference canvas, compensate once and test with real cutout/window geometry. Do **not** scale typography or touch targets to maintain a width ratio. If small widths cannot fit 2 CTA pills or 5 call controls, reflow/hide only unsupported secondary actions and provide accessible overflow, not clipping. Verify 320/393/430dp, landscape, off-center cameras, RTL, fontScale 2.0.

### 28.2 Evidence, code reality, and authority corrections

1. **Version:** all normative references above must be read as V4.3; earlier V4.1 labels were clerical carryovers.
2. **StateFlow:** the reviewed real source exposes `IslandStateManager.behaviorPresentation: StateFlow<IslandBehaviorState>`; a `StateFlow<IslandPresentationState>` wrapper is a **permitted optional derived DTO**, not an existing engine API. Do not build a second queue/timer in the DTO.
3. **Render truth:** `IslandDemoState.Minimal` in the reviewed source still inherits the old `36.67dp` compact height and companion geometry. The **owner's** 108×32/no-content red Minimal specimen is the **target reference**, not evidence that the current production implementation already matches. Map semantic Minimal presentation explicitly; keep real Idle and Companion surfaces distinct.
4. **Device evidence:** Task 05/06 reports include 481/487 JVM and 14 limited instrumentation/device test passes. These do not prove 60 distinct states, do not prove T5 accessibility-overlay binding, and do not prove constant 60fps. Annotate results by source and target device.
5. **Timer reality:** reviewed code already uses event effects/tokens; all `PreviewTimeout`/`ChildTimeout` scheduling and cancellation must remain authoritative in the behavior engine. Ensure preview readable/settled acknowledgement and child settled acknowledgement are events (or tested engine-driven equivalents), not ad-hoc Compose delay jobs.
6. **Identity:** selected notification **key** survives resorting and insertion. The `k of N` label may legitimately change.
7. **True compact:** do not let context-specific 367×86 measurement turn every Compact state into a 367-wide banner.
8. **Touch:** measured 43dp CTA **visual** height and ≥48dp desired **effective** target are different quantities; enlargement must occur **inside safe visible window bounds** or via accessible alternative, never a giant invisible overlay.
9. **Timer call interruption:** suspend **visual children**, but preserve monotonic expiry deadlines; never restart all child timers when a call ends.
10. **Playground:** a fixture may show capabilities but must never launch real PendingIntents, cancel Android notifications or fabricate active platform support.
11. **Screen privacy:** owner/reference fidelity never overrides redaction, OEM permissions, secure system UI or physical hitboxes.
12. **Call/audio controls:** there is no universally valid answer/decline/play/stop capability across other apps. Hide unsupported actions in production; keep reference-only mock UI labeled in Playground.

### 28.3 Expanded vs Compact name clarification

`ContextualCompact` (367×86 r43) means **a short expanded context capsule** in the Figma reference system, not the app's true status-bar Compact (36–37 high). In code use distinct family names such as `StatusCompact`, `ContextualAlert`, `ActivityExpanded`, `SourceImageCard`, `ChildMini` to eliminate height/radius ambiguity. A state may offer multiple **explicit** variants; its state identity must not imply one universal size.

### 28.4 Notification last-item removal contract (UX-C07)

**Both** a real source removal and an Island-only `×` removal of the last **Island-eligible** notification return the Island to Idle if no other Live Activity exists. **Only Island-only removal keeps the Android shade item and its suppression tombstone**; source removal reconciles/cleans it. Children and expired timer callbacks must retire. This corrects the former C07 wording that wrongly restricted the behavior to source removal.

---

## 29. Exact 60-state design geometry and slot recipes — implementation targets

**Reading convention:** At the named reference width, dimensions are `W × H` in dp design-equivalent units; final rendering is `min/content-aware` constrained by §28.1 and actual inset bounds. `R` is the outer radius. For taller/variable states, treat height as preferred before safe reflow. A family-specific measurement is [MEASURED] only when the reference actually supports it; otherwise [IMPLEMENTATION]/[PROPOSED] as labeled. All text, numerical metrics, action slots and alignment must be recomputed from semantic content, not placed using absolute x/y coordinates.

| Enum state | Family size target at reference canvas | Leading / Center / Trailing / Bottom slot rule | Source category |
|---|---|---|---|
| `Idle` | 127×36 R18 | empty surface; align to measured cutout | [MEASURED] |
| `Minimal` | 108×32 R15.9018 | no content; owner red #DC0917 at 0.60 | [OWNER] |
| `MusicCompact` | 190×36.67 R18.335 | L artwork 22, T waveform 17; no fabricated title crowding | [IMPLEMENTATION] |
| `MusicExpanded` | 367×177 R42 | L art 53×53 at x24; C aligned track/artist; T waveform; B scrubber+transport | [MEASURED] |
| `CallCompact` | 152×36.67 R18.335 | L phone/active glyph; T stable time; controls only if space | [IMPLEMENTATION] |
| `CallExpanded` | 367×166 R42 | L avatar 44; C caller+medium; B 5 capability-gated controls, pitch64 | [MEASURED] |
| `CallSummaryExpanded` | 367×112 R42 | L call icon; C identity+duration; optional valid callback CTA | [IMPLEMENTATION] |
| `FaceTimeCallExpanded` | 367×168 R42 | L avatar; C video-call identity; B available video/call actions | [IMPLEMENTATION] |
| `SharedMediaCallExpanded` | 367×172.64 R42 | L call identity; C active shared-media label; B valid controls | [IMPLEMENTATION] |
| `OngoingCallBanner` | 367×86 R43 | L call status; C caller+elapsed; T action when supported | [IMPLEMENTATION] |
| `NotificationCompact` | intrinsic 120–250×36.67 R18.335 | L app glyph; C short label; T numeric count; no 3-line message | [IMPLEMENTATION] |
| `NotificationStacked` | main 120–250×36.67 R18.335; expanded stack uses MINI | compact main is a count cue; children ONLY under expanded main | [IMPLEMENTATION] |
| `NotificationWithMusicCompact` | 190×36.67 R18.335 + 36.7 bubble, gap11 | L media; T alert count; preserve media identity | [IMPLEMENTATION] |
| `NotificationExpanded` | 367×86–162 R43 (86); R42 (142+) | L app icon; C sender+message; B actual actions/reply; pager fixed header | [MIXED] |
| `SilentModeCompact` | 152×36.67 R18.335 | L bell-slash; T silent status | [IMPLEMENTATION] |
| `SilentModeExpanded` | 367×86 R43 | L glyph; C Silent mode/On; T 79×34 Unmute if supported | [MEASURED] |
| `RingerModeExpanded` | 367×86 R43 | L ringer glyph; C mode/level; T real control | [IMPLEMENTATION] |
| `VibrateModeCompact` | 172×36.67 R18.335 | L vibrate glyph; T mode | [IMPLEMENTATION] |
| `NormalModeCompact` | 172×36.67 R18.335 | L sound glyph; T active mode | [IMPLEMENTATION] |
| `RingerVolumeCompact` | 172×36.67 R18.335 | L sound glyph; T level with fixed-width numerals | [IMPLEMENTATION] |
| `MediaVolumeCompact` | 172×36.67 R18.335 | L media sound; T level with fixed-width numerals | [IMPLEMENTATION] |
| `BluetoothConnected` | 196×36.67 R18.335; contextual 366×95 R46 | compact L device/T state; contextual L thumb/C name/T ring | [MIXED] |
| `BluetoothConnecting` | 196×36.67 R18.335 | L BT glyph; T spinner/progress, no fake cancel | [IMPLEMENTATION] |
| `ActionControlExpanded` | 367×210 R42, dynamic height | real capabilities only; grid aligns L/C/T, target48 each | [IMPLEMENTATION] |
| `PremiumExpiryExpanded` | 367×112 R42 (proposal) | L entitlement glyph; C expiry; B real settings action; low priority | [PROPOSED] |
| `ChargingCompact` | 132×36.67 R18.335 | L charging bolt; T percentage fixed-width | [IMPLEMENTATION] |
| `ChargingExpanded` | 367×86 R43 | L battery icon; C charge label/ETA; T percent | [IMPLEMENTATION] |
| `LowBatteryCompact` | 132×36.67 R18.335 | L battery; T percent/status | [IMPLEMENTATION] |
| `LowBatteryExpanded` | 367×86 R43 | L warning/battery; C actionable message; T if valid CTA | [IMPLEMENTATION] |
| `TimerCompact` | 222×36.67 R18.335 | L timer ring; T tabular countdown fixed width | [IMPLEMENTATION] |
| `TimerExpanded` | 367×85.73 R43 | L pause + cancel as real actions; T Timer label + countdown | [MEASURED] |
| `DeliveryCompact` | 160×36.67 R18.335 | L package; T meaningful milestone | [IMPLEMENTATION] |
| `DeliveryExpanded` | 367×144 R42 | L package/status; C milestone/ETA; B supported Track action | [IMPLEMENTATION] |
| `FlightCompact` | 160×36.67 R18.335 | L flight glyph; T code/time | [IMPLEMENTATION] |
| `FlightExpanded` | 367×144 R42 | L flight identity; C route/gate/status; B real source action | [IMPLEMENTATION] |
| `SportsCompact` | 156×36.67 R18.335 | L team/sport glyph; T score/time; no cricket fixture | [IMPLEMENTATION] |
| `SportsExpanded` | 367×144 R42 | L teams; C score/state; B supported Open source | [IMPLEMENTATION] |
| `NavigationCompact` | 156×36.67 R18.335; optional 408×96 R44 | L maneuver; T metric; large variant two-line only if justified | [MIXED] |
| `VoiceMemoExpanded` | 367×86 R43 | L red waveform; T elapsed/real Stop action if app-owned | [MEASURED] |
| `ScreenRecordingExpanded` | 367×86 R43 | L red record dot; C Recording/elapsed; T real stop action | [MEASURED] |
| `ShortcutExpanded` | 367×85.08 R43 | L shortcut glyph; C result/progress; T completion status | [MEASURED] |
| `FocusModeExpanded` | 367×86 R43 | L focus glyph; C state; T settings/capability-backed CTA | [IMPLEMENTATION] |
| `AirPodsConnected` | 367×86 R43 (atlas) / 366×95 R46 (community) | L earbuds/device; C name + Connected; T battery ring | [MEASURED] |
| `SatelliteConnected` | 367×86 R43 | L satellite ring; C connected/status; T real action or none | [MEASURED] |
| `FindMyAlert` | 367×86 R43 | L concentric ring; C single line alert; no forced trailing | [MEASURED] |
| `MovedToIPhone` | 367×86 R43 | L device glyph; C handoff status; T Undo 70×34 if valid | [MEASURED] |
| `VideoRemoteExpanded` | 367×177 R42 | L video thumb; C playing text; B scrub ±15s and real output controls | [MEASURED] |
| `AirDropActivity` | 367×86 R43 | L rings + thumbnail; C transfer/from sender; T progress ring | [MEASURED] |
| `TimerImage` | 367×86 R43 (contextual); small compact only if separate variant | L timer image; C timer identity; T stable time | [IMPLEMENTATION] |
| `NotificationImage` | 367×86–126 R43/R44 by content | L thumbnail if safe; C sender/body; T count; no clipping | [MIXED] |
| `CallAvatars` | 367×86 R43; expandable to 166 R42 if real actions | L multi-avatar; C group/caller; T real controls | [IMPLEMENTATION] |
| `TransportActivity` | 367×148 R42 | L vehicle/route; C ETA/status; B real supported action | [IMPLEMENTATION] |
| `FlightTrackerExpanded` | 367×144–162 R42 | L route glyph; C departure/arrival; B status and source CTA | [IMPLEMENTATION] |
| `SubscriptionPricing` | 367×162 R42 | L app promo; C price/benefit; B honest optional CTA; never intrusive | [IMPLEMENTATION] |
| `MovieCard` | 367×142 R42 | L poster; C film/session; B actual ticket action or none | [IMPLEMENTATION] |
| `ColorOptions` | 367×185.3 R42 | swatches in intentional rows; Playground-only fixture | [IMPLEMENTATION] |
| `AirplaneAlert` | 367×148 R42 | L airplane glyph; C status explanation; B settings CTA only | [MEASURED] |
| `ScreenMirroringAlert` | 367×144 R42 | L screen icon; C mirroring status; T supported stop CTA | [MEASURED] |
| `MobileDataAlert` | 367×162 R42 | L network glyph; C message; B two real CTA pills | [MEASURED] |
| `TransitRouteAlert` | 367×142 R42 | L train; C route/station; B validated End Route CTA | [MEASURED] |

### 29.1 Non-enum presentation states requiring their own visual acceptance

| Behavior/UI state | Design target | Rule |
|---|---|---|
| Single notification auto-preview | Contextual 367×86–162, R43 or R42 by actual height | Source icon/header first, up to 3 useful body lines, only real actions; transient 3.5/5s from readability |
| User-opened one notification | Content-driven 367×86–162, height can grow within safe viewport | No automatic timeout; body, reply, OS-destruction separation |
| 2–50 horizontal pager | Same selected-notification family; stable outer anchor | `k of N` on first render, slider indicator ≤7 dots; no page key jump |
| Main + 1 Mini | child nominal `(main−32)` × 50, R20 | New child immediately below main, gap 6, text app+sender+preview if space |
| Main + 2/3 Minis | same child family; 1–2 small screen, ≤3 large | Visible list newest-first, children reflow below without covering other apps |
| Hidden mini overflow | intrinsic +N pill ~36–44 high | Count only pending newly-arrived children; no chip when it cannot fit safely |
| Promoting child | from current child rect to current main rect | stable key; main's previous selection remains in pager |
| Child touch held | identical static geometry while paused | no dismissal from expiry; accessible hold alternatives |
| Child merge on deliberate collapse | child reverse-morph then removed | queue preserved; timers retired, no ghost redraw |
| Ringing call vs inline reply | call contextual or expanded geometry based on actual actions | preserve draft; call prominence; no fake system control |
| User Minimal red capsule | exact specimen 108×32 R15.9018, `#DC0917` @0.6 | no text/icon; hardware union fallback, never substitute 38×38 glyph circle |
| Camera-respecting Idle | approx 127×36 R18 unless hardware requires more | tap Quick Controls, long-press settings, compact up-swipe non-destructive |
| Lock-screen/privacy-redacted | same per-state outer family | redact title/body when required even unlocked by user privacy setting |
| IME + reply + child arrivals | content-driven within available vertical budget | main draft visible if safe; no child covers IME; small-height fallback to pager semantics |
| Reduced-motion / TalkBack variant | same final target geometry | snap/restrained fade, semantics preserve focus, same actions |

### 29.2 Universal layout acceptance checklist

- **No text clipping**: 1.0x/1.3x/1.5x/2.0x fontScale; long English/Gujarati sender/artist/road; numeric times fully legible; proper RTL and bidi.
- **Stable gravity**: leading icon/identity does not drift; title/subtitle start aligned; trailing metric right-aligned with reserved tabular width; controls below and centered; no button created when capability absent.
- **Responsive widths**: 320 / 360 / 393 / 430dp and landscape; respect physical cutouts, status insets, and measured overlay bounds.
- **No generic cards**: each of the 60 enum states must pick an appropriate recipe and display meaningful sample content in Playground, not a repeated placeholder title/button composition.
- **Exact references where known**: music 367×177 R42, call 367×166 R42, timer alert 367×85.73 R43, CTA 335×43 R21.5, navigation 367×185.36 R42, Minimal 108×32 R15.9018.
- **Appearance vs action truth**: every UI state may be previewed with synthetic content, but genuine system actions are conditionally shown only with real source capability.
- **No overlay interception regression**: window bounds around visible main+mini; transparency inside rectangle is not presumed pass-through.

---

## 30. Architect-reviewed quality and evidence protocol (V4.3)

1. **Required canonical artifacts per state:** a semantic recipe, constrained geometry, correct typography/gravity, native vector/bitmap where justified, genuine/disabled capability policy, RTL/large-text fallback, dark-background preview, and at least one generated screenshot or documented rendering block.
2. **Reference image comparison:** capture the actual 393dp or 430dp family baseline at 100%; compare visible surface bounds, radius, padding, text alignment and control centers against the reference. Include delta table with measured pixels/dp. Proposed values may be tuned without falsely claiming a source measurement.
3. **Test matrix:** enumerate all 60 states plus behavior variants in §29.1; test at least 4 primary screen geometries, fontScale 1.0/1.5/2.0, RTL, reduced motion, large content, missing icons, absent actions, disabled permission, locked/unlocked, cutout left/center/right and multiple notifications.
4. **Rendering evidence:** Compose screenshot tests may substantiate visual appearance but not real accessibility overlay pass-through or lock-screen system behavior. Run separate instrumentation/service tests where genuinely available.
5. **Performance evidence:** capture adequate repeated animation frames for emergence/reorder/merge and main expansion; provide jank percentiles/conditions, not just a 12-frame GPU sample or unsupported 60fps claim.
6. **PR handoff requirements:** show exactly which of the 60 source states are visually finished; which are deliberately reference-only; which have real data and actions; unresolved proof; and screenshots of each family in a reviewer-friendly atlas.
7. **Release blocker:** T5 production `TYPE_ACCESSIBILITY_OVERLAY` physically bound/verified is a release requirement. User Settings authorization is mandatory; never bypass or spoof permission and never mark the app release-approved when this test remains blocked.

### 30.1 V4.2 → V4.3 correction log

| Correction | Previous risk | Resolution |
|---|---|---|
| R1 | Global width scale on the 430dp reference shrinks 393dp atlas 367dp surface | Family-specific reference-canvas resolver §28.1 |
| R2 | No exact per-enum geometry, multiple `alert family` placeholders | Full 60-state recipe §29; explicit measured/implementation/proposed labels |
| R3 | `IslandPresentationState` described as an existing engine-exposed type | Actual `behaviorPresentation: StateFlow<IslandBehaviorState>` reflected in §§3/23/28 |
| R4 | UX-C07 last removal incorrectly limited to real source removal | Island-only removal also returns Idle when last eligible, preserving OS shade/tombstone |
| R5 | Paused-interruption children could never expire while interrupted | Pause visual rendering, not monotonic TTL; expire invalid children |
| R6 | SVG 43dp CTA targets and 48dp effective touch requirements were conflated | Distinguish visual and effective hitboxes §28 |
| R7 | `FontFeatureSettings("tnum")` nonexistent-constructor risk | Use real `TextStyle(fontFeatureSettings = "tnum")` |
| R8 | T4 all-UNVERIFIED wording erased limited Task 05/06 device evidence | Separate narrow logged instrumentation proof from T5 blocked |
| R9 | Document still says V4.1 and prohibits Android changes | Correct version and distinguish documentation from authorized Codex implementation |
| R10 | Radius `r≈h/2` generalization falsely covered 95/96 exception | Explicit exception; family lookup preserved |
| R11 | Runtime Minimal enum might be mistaken for fully implemented 108×32 owner reference | Explicit migration target and device-safety caveat |

**Implementation governance:** The attached Codex task defines coding stages and evidence gates. The owner's explicit UX decisions, source geometry and safety restrictions remain authoritative. Do not reclassify visual-state completion as real platform integration.
