# AURORA — Reference-Faithful Dynamic Island Layout Contract

**Version:** 3.0 — 9 November 2026  
**Status:** Corrective product-design specification. **Supersedes the visual approach used in all previous 39-state and 108-state generated SVG packs.**  
**Evidence:** Owner-exported `Dynamic Island/Minimal.svg`, `Compact.svg`, `Expanded.svg`; `Minimal Presentation.svg`, `Compact Presentation.svg`, `Expanded Presentation.svg`, `Components.svg`, `Prototype.svg`; three reference PNGs (`Group 3–5`); later direct 108×32 Minimal SVG; owner-approved UX-C01–C16.

## 0. Critical correction

The previous 108-state pack is **REJECTED** as a product design deliverable. Merely drawing 108 differently sized rounded black rectangles, putting a title in the top-left and generic buttons along the bottom, does not reproduce these reference designs. The pack lacks a formal gravity system, typography roles, content hierarchy, semantic controls, content-measuring constraints, and evidence of correct text reflow. Do not reuse or bulk-import its visuals as a design system. It may remain only an inventory of proposed state names.

The design task is **not** “108 unique SVG screenshots.” It is **one coherent set of reusable semantic presentation families** with state-specific information architecture and real constraints, instantiated and tested for the approved activity states.

## 1. What is verified in the owner references

### Source-measured geometry (not interpolated)

| Source element | Dimensions in reference viewBox units | Rounding | Evidence/qualification |
|---|---|---|---|
| User-confirmed **Minimal** capsule | 108 × 32; drawn silhouette is 31.8037 high | Shape radius 15.9018 | Later direct SVG, red `#DC0917` with `fill-opacity=0.6`; no text/icons provided in source |
| Primary true compact capsule | 126 × 36.67 | Radius ≈ 18.34 | `Compact Presentation.svg`; alternate widths must be based on content and actual cutout |
| Contextual alert capsule | 367 × 86 | Radius 43 | `Compact.svg` atlas; this file contains **contextual expanded** examples, not all 36.67-high compact pills |
| Media player surface | 367 × 177 | Radius 42 | `Expanded.svg` atlas |
| Full ongoing-call control surface | 367 × 166 | Radius 42 | `Expanded.svg` atlas |
| Turn-by-turn route surface | 367 × 185.36 | Radius 42 | `Expanded.svg` atlas |
| Live/transit activity surfaces | 367 × 142–148 | Radius ~42 | Different content families, **not** a universal 145-high card |
| Contextual full-width CTA | 335 × 43 | Radius 21.5 | In 367-wide surface: 16 inset each side |
| Side-by-side CTA option | 161.5 × 43 each | Radius 21.5 | Two equal actions separated by ~12 |
| Media album art | 53 × 53 | Radius 8 | Actual expanded music composition; left inset 24 |
| Separate circular companion indicator | 38 × 38 | Radius 19 | Part of older *minimal glyph atlas*. **Not** the user-confirmed Minimal state |

**Units warning:** These are **SVG design units**, not guaranteed to equal Android `dp` on every device. Source filenames refer to **atlases** containing many examples; do not interpret the atlas canvas size as the size of one widget.

**Typography warning:** Most reference SVG text is outlined as vector paths. The exports do not establish the original font family, font sizes, line heights, or text baseline metrics. Therefore the text settings below are **design implementation proposals**, not falsely claimed exact extraction.

## 2. The gravity system: explicit layout anchors

“Gravity” means where each region is anchored when its siblings or content dimensions change. **No universal center of the card**: different elements have independent anchors.

| Region | Horizontal gravity | Vertical gravity | Layout rule |
|---|---|---|---|
| Cutout-coupled compact capsule | Anchored to measured physical camera/cutout geometry | Status-bar safe-area alignment | Camera is not a decorative mock element; obey real Android insets |
| Compact leading identity glyph | **Start** | **CenterVertically** | Own intrinsic glyph bounds, never spread with `SpaceEvenly` |
| Compact main text/status | After leading glyph; uses remaining width | **CenterVertically** | One primary line; short label; no fixed x-coordinate after dynamic icon |
| Compact trailing metric/action | **End** | **CenterVertically** | Timer/counter/connection/ring state retains right alignment as label length changes |
| Standard contextual left identity | **Start**, inside edge inset | **CenterVertically** as a group | Icon may be larger; two-line block must be vertically centered as a *group* |
| Standard contextual text column | After icon, constrained between end action | **CenterVertically** | Title and subtitle follow baselines, 1-line title + 1-line subtitle default |
| Standard contextual trailing control | **End** | **CenterVertically** | Do not render decorative actions in empty space |
| Expanded music artwork | **TopStart** | **Top**, aligned to header | Artwork remains 53-square regardless of title length |
| Expanded music track/artist | Immediately right of artwork | **Header center** | 2-line identity block; never overlaps end waveform/status |
| Expanded music waveform/output icon | **TopEnd** | Vertically centered within header | Semantic status independent of title truncation |
| Expanded music elapsed/remaining labels | **Start**/**End** of timeline rail | Same scrubber row | Label widths and track bar resize independently |
| Expanded music transport controls | Balanced within bottom control row | **Bottom**, above inset | Previous / Play-Pause / Next with larger primary target; output control at end |
| Expanded call identity | Start/leading avatar and text | **Top/header** | Caller name is primary; call medium/status secondary |
| Expanded call controls | **Evenly distributed constrained control slots** | **Bottom** | Actual call actions only; red End anchored as last action |
| Navigation maneuver | **Start**, arrow+distance | **Top/primary content** | Large turn-distance and street identity lead, route context secondary |
| Alert primary CTA | Stretch within horizontal insets | **Bottom** | A 335-wide style in 367 reference, not a fake full-width text row |
| Child Mini-Island | Centered under Main, width main minus ~32 (Android product target) | **Below main**, newest first | Child height and typography derived from available height; touch target remains accessible |

Implementation principle: keep *internal* layout via nested Auto Layout / Compose Row+Column+Box, never by arbitrary absolute coordinates. Absolute positioning is appropriate only at the outer screen/cutout anchor or for intentional decorative overlays.

## 3. Typography and overflow: explicit text rules (proposed, to validate visually)

These are **proposed first-pass values**, because the original font metadata is not available from the exports. Start from Android Roboto / the project's defined AppTheme typography, not arbitrary emoji or placeholder glyphs. Validate each in real rendered frames.

| Text role | Starting size | Weight | Line count | Overflow | Alignment |
|---|---|---|---|---|---|
| Compact status/time | 12–14sp | Medium/semibold | 1 | End ellipsis, except important numerical timer | Start or End by semantic slot |
| Contextual title | 15–17sp | Semibold | 1 | End ellipsis | Start |
| Contextual subtitle | 12–14sp | Regular | 1–2 only if height supports | End ellipsis | Start |
| Music title / caller title | 15–17sp | Medium/semibold | 1 | End ellipsis | Start |
| Media artist / call subline | 12–14sp | Regular | 1 | End ellipsis | Start |
| Elapsed / remaining time | 11–12sp | Medium; tabular numerals | 1 | **Never** ellipsize essential time; reserve width | Start / End |
| Navigation distance/countdown | 24–32sp | Semibold, tabular numerals | 1 | Scale/resize before clipping | Context-specific |
| CTA title | 14–16sp | Medium | 1–2 only if action target can expand | Ellipsis only with full accessible label | Center |
| Mini-Island identity | 11–12sp | Medium | 1 | End ellipsis | Start |
| Mini-Island message preview | 12–13sp | Medium/regular | 1 | End ellipsis | Start |

Enforce `fontScale` 1.0, 1.3, 1.5, 2.0 tests. At large font scales: measure, expand eligible surface height up to safe budget, then reduce secondary metadata and/or action count; **do not silently clip critical labels**. Provide TalkBack content descriptions with full text. Use semantic icons as vector/components, not surrogate emoji characters.

**Text gravity contracts:** a music artist line starts exactly at the title column's x; status label and action never force title to recenter; numerical trailing label never moves on ordinary title text change; RTL mirrors reading order except true numeric media progress and direction-critical/navigation semantics.

## 4. Semantic structure by presentation family

### A. Minimal — owner-provided 108×32

Preserve 108×32 capsule silhouette; source is translucent red fill at 60%. The standalone SVG supplied **contains neither text nor an icon**. Hence its glyph/content model is currently unspecified: never invent its behavior or equate it with the 38×38 circular companion.

### B. True Compact — active glance

One compact row: `[leading icon or live signal] [1 short semantic label or essential metric] [optional trailing metric]`. At most one action if the reference and runtime context justify it. 36.67-high design baseline; width **measured from content**, constrained by cutout and safe-area budgets. Avoid full subtitle and button rows.

Actual owner compact reference examples are **not one generic pattern**:
- AirDrop/transfer: large recognizable leading symbol + short source identity.
- Timer: orange "Timer" plus strong tabular `3:35` trailing.
- Live recording: red duration and independent bounded stop control.
- Incoming call: leading caller identity + separate red/green controls.
- AirPods/connection: secondary "Connected", heavier device name + battery ring.
- Silent mode: muted icon, status + one constrained action.

### C. Contextual 86-high alert

A 3-region composition, when needed: `leading 30–40 icon | one/two-line primary column | trailing small action/status`. If an alert has a CTA, anchor CTA in bottom inset; keep the alert's identity and action within its actual measured height. **Do not** add generic Reply and Open buttons to every alert.

### D. Expanded media 367×177

Owner SVG composition demonstrates: top identity row with 53×53 album art (left inset 24, R8), title/artist column and trailing waveform; scrubber/time region; bottom transport controls. Treat scrubber as a real active seek range with elapsed/remaining labels, not a decorative rectangle. Album art, text and controls occupy independent semantic zones.

### E. Expanded call 367×166

Top caller identity; action controls arranged evenly across bottom with semantic color. Red hang-up at last slot. Vary controls by phone call/FaceTime/shared-media context; do not treat all call states as one static five-button picture.

### F. Navigation 367×185.36

The next turn and distance win visual hierarchy. Arrow/distance/road labels are actual structured route information; maintain primary vertical reading order under incoming notifications. Do not replace this with generic navigation icon plus subtitle.

### G. Live Activity 367×142–162

Delivery: milestone/proximity/timeline. Flight: route, timing, gate/status. Sports: team/score/time or match state. Transit: station and platform/route, with meaningful action. Data/stock charts: chart if activity data warrants it. **Large stock and Screen Time PNG references are full-size widgets**; they show visual grammar, not evidence that all those rows belong inside a Dynamic Island.

### H. User's Android layered child Mini-Islands

Not a standard Apple notification model; it is the app's approved differentiation. Main selected notification retains its key. Child arrivals use newest-first order; only 1–2 visible on small screens, ≤3 on large; hidden overflow counts *pending new arrivals*. Child visual merge never dismisses Android notification. Child text stays readable at >=48dp tap height; independent timer tokens survive arrival bursts. At low height, defer children/overflow rather than block apps underneath.

## 5. Main sizing and expansion behavior

1. Determine available width `Wavail` after left/right safe insets and system/cutout constraints.
2. Compact width is **intrinsic** from `[padding + leading + gap + main measured text + gap + trailing + padding]`; cap by `Wavail`. Do not stretch to 367 just because another specimen is 367.
3. Expanded width is `min(targetReferenceFamilyWidth, Wavail)`; on 393-wide iPhone source specimens this leaves ~11–13 units side breathing room. Actual Android notch position governs horizontal anchoring.
4. Expanded height is the minimum that fully fits required content, subject to family's reference *baseline* and available viewport. If the message/reply needs more content height, expand within safety budget; if not enough, truncate *secondary* content, prioritize actions, and preserve access to full content.
5. Corners: for compact pill, radius ≈ height/2. For tall expanded cards, **do not** scale radius with height; use the reference's ~42 corner radius.
6. Text measures before surface animates. Surface morphs toward calculated size; content never teleports to wrong anchors.
7. Multi-activity split bubble must be separate and driven by actual concurrent activity; no arbitrary second bubble.
8. Accessibility touch targets can extend safely inside the overlay without inflating the visible icons. Never claim transparent overlays pass touches without physical window validation.

## 6. Semantic color rules

- Main surface black; gradients/glows only where **present in the specific reference**, not a global decoration.
- White is primary content. Grey is secondary context. A **single semantic accent** indicates activity/status (red active recording/error/end; green active/connected/call; orange timer or charging; cyan transfer/navigation; magenta music waveform). Specific source colors vary; reuse extracted art/token sets.
- Destructive red action only for destructive meaning. Do not use red merely to brighten an otherwise empty card.
- Colour is not the sole signal: text and icon communicate activity.

## 7. Figma component implementation contract

Native editable Figma design should comprise reusable **component families**, not 108 detached SVG drawings:

- `Island/Minimal` (owner-corrected exact silhouette; no invented text)
- `Island/Compact/IdentityMetric`, `Island/Compact/Timer`, `Island/Compact/SplitAction`, `Island/Compact/Progress`
- `Island/Alert/IdentityStatus`, `Island/Alert/InlineCTA`, `Island/Alert/TwoActions`
- `Island/Media/Transport`
- `Island/Call/Incoming`, `Island/Call/Ongoing`, `Island/Call/Summary`
- `Island/Navigation/NextTurn`
- `Island/LiveActivity/Delivery`, `/Flight`, `/Sports`, `/Transit`
- `Island/Notification/Single`, `/Pager`, `/Reply`
- `Island/MiniStack/Child`, `/Overflow`, `/Promotion`
- `Island/Privacy/Redacted`

Use top-level **Auto Layout** and nested rows, fixed/fill/hug constraints intentionally; text remains text (not outlined paths); icon is reusable instance; values are component properties or instance overrides; actions are semantic buttons. Document width/height/radius and padding separately for each family. Preserve the owner's original reference boards untouched.

## 8. Quality gate before more states are created

Do **not** make another mass-export pack until three anchor designs are rebuilt and checked:

1. **Compact timer or status**: correct leading/trailing gravity, no awkward empty space, readable at 1.5× and 2×.
2. **Expanded media**: artwork/title/artist/waveform, scrubber, time labels, all media controls with correct anchors; compare at 100% reference scale.
3. **Expanded call or live activity**: correct context-specific hierarchy and action placement, full text at large font scale, no overlaps.

Each anchor must pass: no clipped text; no fake generic actions; correct icon source/semantics; no inconsistent glyph sizes; correct dynamic title truncation; stable trailing metrics; correct responsive width/height; suitable contrast; 48dp-equivalent hit targets when possible; screenshot compared to **actual supplied reference**. Only then derive the remaining states using the proven semantic families.

## 9. Current delivery status and limitation

This document is a **source-grounded correction specification**, not a claim that native Figma components have been updated. Direct Figma editing previously returned a Starter plan MCP usage-limit error. The prior 39/108 SVG packs remain **visually rejected** until rebuilt against this contract. Neither hand-written source SVGs nor the original Figma export can identify original editable font style details with certainty, so Figma native text metrics still need visual verification once direct file access is restored.
