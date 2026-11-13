# AURORA V6D — PREMIUM ACTIONS, ICONOGRAPHY, AND INTERACTIVE STATE QUALITY

**STATUS:** Implementation assignment. **Do not treat this document as evidence of implementation.**

**Role:** Principal Product Designer + Staff Android Jetpack Compose Engineer + Motion Designer + Mobile Accessibility & Visual QA Engineer.

**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`

**Start from the latest review branch:** `codex/aurora-v6c-adaptive-ui-review`, independently observed at `30ed4e9bd1df78152b17a7225938e60b98d26664` when this task was prepared. Check the remote HEAD before editing; if newer, reconcile changes rather than resetting them. Do not merge to `main`.

**Required inputs:**
- Original folder: `/Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/Dynamic Island states reference from figma`
- `GEMINI_ANTIGRAVITY_COMBINED_V6C_MASTER_REWORK_PROMPT.md` and V6C adaptive state contract.
- Two owner screenshots attached with this task: current **CallSummaryExpanded** in the 60-state Gallery and a separate **SMART ACTIONS** marketing image showing polished incoming-call, WhatsApp, video/media, and navigation surfaces.
- The latest production renderer, demo fixtures, capability adapter, and call/notification lifecycle code.
- `AURORA_V6D_60_STATE_ACTION_AND_CONTENT_AUDIT.csv` in this package.

## 0 — Owner's real goal (read before coding)

The owner is still unhappy despite passing geometry reports because the **information, controls and icons inside the Island lack professional visual hierarchy**. Buttons currently look grey/disabled, their glyphs are tiny, actions are too equally weighted, padding and baselines feel inconsistent, and some states look like tiny generic dashboard cards rather than an island.

Rebuild the **action design system**, then correct every real state that uses it. Do not merely recolor one screenshot or make buttons larger. Read the entire V6C task and state contracts; these requirements extend them. The owner's marketing image is **quality and composition inspiration**, not proof of real Android control capabilities or an exact Figma reference. Original direct Figma exports remain authoritative for their own geometry/content; the marketing image illustrates visual rhythm, saturated functional accents, crisp glyphs and button differentiation.

**Definition of done:** On-device Islands communicate (a) what happened, (b) what matters most, and (c) what can actually be done, within a small, optically balanced surface. Every relevant control is available, meaningfully named, accessible and safely wired. No decorative fake control or invisible no-op.

## 1 — Verified baseline source audit (review again on your actual HEAD)

1. `app/src/main/java/ai/emots/kishan_dynamic/ui/components/CallSummaryIsland.kt` currently renders a row of **Redial + SMS + Done** with weights `1.0f/0.7f/0.8f`; `CallSummaryActionButton` uses a **13dp glyph**, **36dp visual-height pill** in a 48dp wrapper, and muted surfaces (`#222B3A` and `IslandColors.Gray6`). All three have similar emphasis. Result: low-information action overload and disabled-looking visuals.
2. `CallSummaryActionButton` calls `queryIntentActivities(...).getOrDefault(true)`. Capability lookup failure cannot be treated as proof of availability. Replace with explicit, conservative capability determination; avoid action loss due to modern Android package visibility by using a genuinely supported intent/dispatch strategy and truthfully documenting fallback. No `getOrDefault(true)` as a safety decision.
3. The existing `IslandControlAvailability.kt` uses a composition local where **null means demo mode**. Production already provides a capability set. Keep these contexts separate, and ensure preview mode never silently claims production action availability.
4. The source already contains `IncomingCallIsland`, `OngoingCallIslandExpanded`, Quick Controls, media, notifications and derived activity layouts. Inspect and reuse them; **do not introduce a new competing Island renderer or behavior engine**.
5. V6C's report claims 525 JVM tests and production accessibility-overlay testing remains blocked pending legitimate authorization. Claims are NOT independently rerun by this task. The new visual pass must test its own code and report actual evidence. Do not relabel debug application-overlay passes as production accessibility-overlay passes.
6. A previous V6 audit identified 33 geometry-report inconsistencies. V6C claimed reconciliation. Verify current code-derived matrix versus current report rather than blindly inheriting the old failure or falsely assuming every live screen is pixel-perfect.

## 2 — Immutable product rules

Preserve **UX-C01–C15 plus architect-selected independent child-timer rule PD-001/C16**, the original call/music/notification priority and restoration contract, 50-item stable-key newest-first pager, source-aware notification actions, island-only dismiss default, user-focused expanded presentations, independent Mini-Island timers, meaningful-update resurfacing, sensitive redaction, reply-draft preservation and accessibility.

Never: render a native OS action without actual capability; manufacture a notification/media/call event in production; duplicate a user action; cancel an Android notification on visual collapse; steal the page being read; or use a full-screen touch shield around a floating accessibility overlay. Preserve real `TYPE_ACCESSIBILITY_OVERLAY` semantics and owner settings authorization.

## 3 — Action taxonomy: one shared visual language, not one generic button

Implement reusable **semantically typed** primitives, supporting both Figma-matched sizes and adaptive family variants:

| Style | When | Appearance | Icon/text |
|---|---|---|---|
| `PRIMARY` | Best next available action, e.g. verified Redial or Reply | Clearly differentiated tinted or higher-luminance capsule; restrained source-color accent | High-contrast crisp glyph + 12–14sp medium/semibold label |
| `POSITIVE` | Accept a legitimately ringing call | Green circular action with white phone glyph | 48–56dp visible diameter per verified reference; label through TalkBack |
| `DESTRUCTIVE` | Decline or end call, Stop recording when real | Red circular or capsule depending on reference; white icon | Never confusingly share the same neutral action style |
| `SECONDARY` | Open supported source, message where genuinely relevant | Subdued but clearly **enabled**, contrast-preserving dark tinted surface | Readable label, controlled glyph size |
| `TERTIARY` | Dismiss/Done or overflow | Quiet text/icon with intentional hit target; may be a small outlined icon-only target | Do not visually compete with the primary action |
| `TOGGLE` | Real supported mute/speaker/play state | State-dependent selected/unselected visual treatment | Reflect source-confirmed state; pending/failed states must be clear |
| `PASSIVE` | Status/timer/progress with no supported action | **Not a button** | No misleading chevrons, click shadows or colored CTAs |
| `OVERFLOW` | More valid actions than fit | Small accessible more-menu/expanded affordance if platform and space permit | Order from most useful to least; do not squeeze every action |

**Candidate baseline tokens (design proposals, not measurements):** visual capsule height 36–42dp with ≥48dp hit target where safe; circular call action 48–56dp; simple action glyph 17–20dp optical size; action label 12–14sp Medium/SemiBold; text/icon gap 7–9dp; horizontal content padding 12–16dp; gaps 8–10dp. Direct Figma reference measurements override these numbers for the visual surface. Maintain accessible hit bounds without silently expanding the touchable overlay window into unwanted screen regions.

**Avoid:** icon 13dp as the universal rule; low-contrast grey text on black that resembles disabled; identical three-pill sizing for unrelated priorities; random saturated outlines on every state; hard-coded shadow/glow across all buttons; generic circle placeholders; mixed outline and filled icon families; text and icons with mismatched baseline or weight; narrow label ellipsis like `Red...`.

**Visual components to centralize, where appropriate:** `IslandActionPill`, `IslandIconAction`, `IslandControlRow`, `IslandActionStyle`, `IslandSemanticAccent`, `IslandActionCapability`, and preview/test fixtures. Names are suggestions, not mandates. Do not create unnecessary layers of abstractions or public composables for one-off designs.

## 4 — Type, icon and layout tokens

**Information order:** Source identity > user-facing event/state > secondary context > timestamp/progress > useful action. If some data is absent, **remove the unused region**, rather than reserving blank space.

- Primary text: typical 15–17sp semibold with measured line-height; title 1 line unless a specific expanded layout allows 2.
- Source/app badge: roughly 10.5–12sp, semibold or medium; prioritize semantic contrast.
- Secondary detail: about 11.5–13sp, cap at 1–2 lines with ellipsis or expand as content requires.
- Timers/metrics: stable tabular figures, single-line end alignment; never animate width per changing digit.
- Icons: source-backed app icons/avatars where available; otherwise one consistent Apple-style/custom vector glyph family in Compose. No Unicode symbols or emoji standing in for functional icons. Use optical alignment—not only equal bounding boxes.
- Named 48dp interactive hit zones for accessibility, hit regions aligned with real surfaces, meaningful TalkBack roles and state descriptions; avoid duplicate semantics for decorative icons.
- Default layout gutters: ~14–20dp around expanded content based on family, 6–10dp between icon/text groups, 8–12dp between rows; **do not apply one guessed number to all 60 states**. Preserve explicit direct-reference positions.
- No `Arrangement.SpaceBetween` as a shortcut for vertically stacking a handful of rows inside a fixed-height activity card. Use measured row gaps, intrinsic content height and well-defined optional slots.

## 5 — Call family: mandatory high-priority redesign and lifecycle

**Do not copy the marketing image as a Call Summary.** The marketing image shows a ringing call: red Decline and green Accept. This is correct only if the source is truly ringing and actions are supported.

### Incoming / Ringing (direct reference)
- Avatar/name/source hierarchy, distinct red Decline and green Accept circular controls anchored at the end.
- Actual `onAccept` and `onDecline` capability; if unavailable, present truthful passive status or real open-dialer affordance instead.
- Never use `Done`, `Redial`, or green running duration as a substitute for incoming actions.
- 367×86 R43 is a reference baseline at the matched 393dp canvas, adapt to safe width/large font. No duplicate callback.

### Active / Connected Compact and Expanded
- Compact: call glyph + tabular elapsed metric, optionally distinct waveform companion when the real activity supports it; stable START/END anchors.
- Expanded: caller identity and actual call controls, with END red and clear Mute/Speaker active states; capability-dependent video/share controls only when supported.
- Control state comes from platform/controller acknowledgements, not local optimistic booleans that can become false after failure.
- Active elapsed timer begins when connected, never on ringing; maintain restoration after interruption.

### Call Summary / Ended (current screenshot's critical fix)
- **Default to exactly one prominent valid follow-up action (e.g. Redial/Call back) and one quiet Done/close action.** SMS moves to secondary overflow only if capability exists and product research justifies it. Do not force three equal buttons in a 112dp capsule.
- Main row: avatar START, caller name clearly stronger than subtitle, neutral static duration END (if known), correct outcome like `Call ended · Incoming`, `Outgoing call ended`, `Missed call`; do not show ringing or fabricated 00:00.
- Buttons: compact distinct primary + tertiary, icon optical size ~18sp/18dp, labels ~12–13sp, no meaningless giant blank backdrop. Prefer 48dp safe hit targets with ~38–42dp visual pills when height budget permits. If all cannot fit, preserve readable content and use a single follow-up action or adaptive height.
- Locked screen: redact caller identity/number/avatar consistently before composing summary. Validate actual renderer under locked state, not just a pure policy test.
- Redial dispatch opens a valid dial intent or source-supported control; SMS only real valid messaging path; Done dismisses **only** the summary. Prevent duplicate dispatch and stale action callbacks.
- Target for exact Gallery specimen `367×112 R42`; when no reference or content changes materially, measured adaptive behavior allowed, logged distinctly. Never blindly squeeze content to hit height.

## 6 — Notification family: functional buttons vary by source

- Compact notification: icon, count or useful text and stable tick; no tiny clickable 'Reply' that cannot be touched.
- Expanded: context row (source + time), strong sender/title, short preview, optionally expanded text/image, then 1–2 **real** actions backed by `Notification.Action`, `PendingIntent` or `RemoteInput` capability where allowed. Use user's source labels where meaningful; never hardcode WhatsApp actions for a generic notification.
- A WhatsApp-like reference may show `Reply` and `Mark as read`. **Only show `Mark as read` if the posted notification actually provides that action**; never fake successful read state.
- If Reply supported: prioritize Reply and retain draft on incoming notification/call, no emoji 'chat icon' substitute; keyboard focus stays safe.
- If neither action exists, show readable information and an actual valid Open affordance only if `contentIntent` is available.
- Stacked/pager: preserve selection key, counts and accessibility. New arrival doesn't steal selected expanded content; Mini-Island promotion preserves queue and user focus.
- Mini-Islands: small readable child preview; no full button row in 50dp child; tap promotes, touch pauses timer. Pending overflow counts only newly pending children.

## 7 — Media/video/social: differentiated visual components, not fabricated integrations

- Music expanded: artwork, title/artist, real progress/scrubber where supported, Prev/Play/Pause/Next controls according to media session actions; play icon and pause icon represent verified state. Important controls use proper icon-only target alignment, not 13dp decorative glyphs.
- Compact music: artwork START, waveform END, real animated movement only on confirmed playing; silence/stopped should not claim active audio waveform.
- Video/social such as TikTok in marketing: adapt **design language** (thumbnail, title, dedicated controls) but never show `Like`, `Pause` or `Open` unless the actual source provides a legitimate capability. If not: passive media card or source Open action.
- Voice memo/recording: one unmistakable real Stop action in destructive red; running recording glyph/waveform when actual; elapsed time tabular.
- AirPods/Bluetooth: device identity, battery and actual connection status; no optimistic fake toggles.

## 8 — Navigation, Live Activities, system alerts and commerce

- Navigation: directional glyph + maneuver text + distance, optionally road name/progress/ETA; action usually passive until source permits. No unrelated color/options controls. Unknown direction **must not default to an invented right turn**.
- Flight/delivery/sports/transit: one clear key metric or milestone and secondary verified context, not a repeated three-button dashboard. Action count 0–1 ordinarily, based on source.
- Charging/battery/volume/ringer: status-first contextual HUD, normally no CTA; no misleading green/blue buttons.
- Airplane mode/mobile data: informational cause + a **real Settings intent** where possible; one unambiguous primary Open Settings action. Never claim the app can silently toggle a privileged system setting.
- Screen mirroring: simple app/device identity and real Stop control **only if this app owns that session or a supported source action exists**; otherwise Open source/system controls safely. Preserve direct Figma geometry.
- Movie ticket card: poster/title, venue/time and seat; show `View Ticket` only if a real ticket artifact/action is provided. Fixture/demo must say demonstration; production must not show fake 'Cinema 2 · 19:30' or 'G12'.
- Premium expiry/subscription: product-branded low-priority display; Upgrade routes to legitimate premium screen and price source; don't masquerade as system warning.
- Quick Controls: clear labels/icon, source-capability-aware on/off, no unsupported system toggles or invisible click affordances.
- ColorOptions/demo family: don't map real navigation state into palette preview; keep demo variants separate.

## 9 — All 60 states must be audited: action + content + icon + geometry

Use `AURORA_V6D_60_STATE_ACTION_AND_CONTENT_AUDIT.csv` in this package. For **each state**:
1. Identify `DIRECT`, `FAMILY_ONLY`, or `NO_SOURCE` provenance from actual repository fixtures.
2. Review current screenshot and source composable, then fill the matrix with current icon assets, 1–3 most important content fields, action types, actual capability source, visual style and density.
3. Decide how much content is **intrinsic vs optional** and choose exact-reference geometry or adaptive content sizing, respectively. A direct reference's known specimen cannot be called pixel-perfect if it drifts.
4. For short, long, no-icon, no-action and disabled-capability variants, render without ghost whitespace or clipped labels.
5. No unjustified extra visual effects or actions: source and behavioral meaning first.
6. Propagate shared primitives to **all** applicable states, not only the Gallery's #7 screenshot.
7. Preserve compact/expanded/live presentation and the entire approved UX state machine.

## 10 — Visual verification and Jugg loop (REQUIRED)

Use the repo's `.agents/skills/jugg-android-dev-loop/SKILL.md` where available and applicable. Confirm its actual commands and device access; do not invent APIs. Pair Jugg hierarchy/touch inspection with real screenshot capture via supported ADB/Android instrumentation or UI tooling. Do not claim screenshots capture through Jugg itself unless it supports it.

### Test on these canvases
- Widths 320, 360, 393 and 430dp. Extra landscape/split-screen and left/center/right cutout conditions when supported.
- Font scale 1.0, 1.3, 1.5, 2.0; LTR/RTL; light/dark foreground-app backgrounds; reduced motion; lock/unlock; source icon missing; long unicode names; disabled actions; notification images.
- One/two/three action cases, very long localization, no action, failed external intent; rapid tap/double tap and interruption while a button is pressed.

### Capture independently for each state
- Reference image where `DIRECT`, rendered Island at same density/viewport, 50% overlay + diff heatmap (when comparable), internal bounds/layout tree, actual action capability, outcome on tap, privacy result.
- For derived states, compare against family design tokens and run a qualitative professional review; **never fabricate a pixel-parity score against a nonexistent reference**.
- Distinct scores: exterior geometry, inner density/spacing, typography baseline, icon consistency, action semantics, actual behavior, responsiveness, privacy/accessibility. A green W×H-only score is NOT PASS.

### Required visual questions
- Can a user instantly tell which action is primary?
- Do icons look optically aligned and belong to one family?
- Is enabled text truly readable, unlike disabled grey?
- Are buttons large enough to target without crowding?
- Are actions source-real, nonduplicating and useful?
- Does the surface adapt without oversized voids?
- Is the same state correct before, during and after relevant interruption?

## 11 — Automated and manual acceptance gates

**P0**: misleading actions, accidental OS dismissal, insecure private caller/notification content, duplicate action dispatch, invented status data, unsafe overlay hit window, correct call/music/notification lifecycle.

**P1**: 60/60 audited and renderer outcomes present; all direct-reference variants maintain measured geometry; family-derived visuals coherent; text/icon optical sizing; each context's action style; no unjustified whitespace or clipping at 320–430dp; TalkBack and reduced motion verified where possible.

**P2**: polishing subtle press feedback, elevation and micro-motion that preserve input stability.

Tests must include:
- Call ended displays appropriate source duration/outcome; Redial only if valid; Done always; SMS only if capability and space; idempotent taps.
- Incoming call shows red Decline/green Accept and correct answer/decline transition.
- Active call Mute/Speaker state updates only through actual source; failed action rolls back or indicates failure.
- Generic custom notification with no actions has no fake button; with valid Reply has reply; no `Mark as read` unless source action exists.
- Source media actions respect supported action mask; unrecognized sources don't get invented Like/Pause controls.
- Two rows/long text/reduced height never force three tiny buttons or text clipping.
- Locked Call Summary shows no leaked caller details.
- Pixel comparison against direct Figma for Call, Notification, AirplaneAlert, ScreenMirroring and other mapped assets.
- Pending Mini-Island timers/pager stability not regressed.

Build `:app:compileDebugKotlin`, `:app:testDebugUnitTest --rerun-tasks`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, then execute device instrumentation when authorized. Distinguish failed/skipped/blocked. **Production `TYPE_ACCESSIBILITY_OVERLAY` validation remains a release gate if service cannot be legitimately enabled.**

## 12 — Deliverables

1. `reports/aurora-v6d/01_ACTION_SYSTEM_TOKENS_AND_COMPONENTS.md` — exact classes, icons, tokens and state exceptions.
2. `reports/aurora-v6d/02_60_STATE_ACTION_AND_CONTENT_MATRIX.csv` — complete audit, actual source/fixture, supported actions, measurements, result/status.
3. `reports/aurora-v6d/03_CALL_AND_NOTIFICATION_REAL_ACTIONS.md` — call lifecycle, source validation, preview/dismiss rules.
4. `reports/aurora-v6d/04_VISUAL_REFERENCE_DIFFS.md` — reference-normalized side-by-side evidence with screenshot links.
5. `reports/aurora-v6d/05_RESPONSIVENESS_ACCESSIBILITY.md` — 320–430dp, RTL, font scale, role/action outcomes.
6. `reports/aurora-v6d/06_TEST_LOGS_AND_BLOCKERS.md` — exact executed commands, counts, device type and blockers.
7. `reports/aurora-v6d/07_FINAL_HANDOFF.md` — measurable quality gates and exact Git commit.

Commit/push into a new review branch based on V6C. **No merge to main, no public release.** Stop for independent examination. No premature '100% pixel perfect' claims, no filler screenshot placeholders presented as actual results. No need for owner to choose routine design details: follow these product decisions and actual references.
