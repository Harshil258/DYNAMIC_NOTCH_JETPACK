# AURORA V6 — Professional Design Constitution for Unreferenced and Family-Derived Dynamic Island States

**Status:** Principal product-design specification and implementation acceptance contract; does not claim 32 derived states are source-measured or currently implemented correctly.  
**App:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Reviewed commit:** `b613666c973973d43871f3b8ae9c14e8d7814bca` (`codex/aurora-v5-pixel-parity-review`)  
**Required references:** `Dynamic Island states reference from figma/`, approved UX-C01–C15 plus architect PD-001 independent timers, V4.3/V5 geometry rules, Android actual source and physical-device evidence.

## 0. Decision and evidence governance

1. **Owner-provided direct reference wins for visual geometry** when measured at the right 1× canvas. It does not override Android capabilities, privacy, the real cutout, usability, large text, or approved functional rules.
2. **Owner-approved UX C01–C15 and architect PD-001** govern event behavior; no UI redesign may introduce a competing notification queue, fake call/session state, timer, arbitrary source action or permission.
3. **For a missing exact reference, do NOT fake one.** Design a distinct semantically appropriate layout derived from a verified family. Record a design rationale, proposed numbers, and why a similarly rendered state is an inappropriate template.
4. **Implementation facts come from the checked-out branch**, not stale reports. A gallery state is not proof of production integration. Design success does not imply a valid Android source or action capability.
5. **Provenance is explicit:** `DIRECT` (exact/claimed asset; still requires comparison), `FAMILY_ONLY` (analogous pattern, not pixel-parity evidence), `NO_SOURCE` (new design proposal requiring explicit product QA). The current debug fixture source classifies **28 direct, 10 family-only, 22 no-source = 60**; earlier V5 report's “19/41” classification conflicts and must be reconciled to the actual source before reporting.
6. **No invented reference data as real runtime data.** Debug fixture copy is permissible only in an explicit Playground/gallery fixture and must never leak into a production service, external action, OS permission control, or user notification.
7. **A derived state must be designed, not left blank or squeezed into a generic card.** The gallery should show a real readable implementation and an honest annotated family explanation, not a giant `NO_SOURCE` empty black pill impersonating a reference.
8. **No project cricket fixtures.** Sports layouts may use a non-cricket synthetic sport example in debug; real scores require legitimate source data.

## 1. Observed code and screenshot defects — mandatory P0 fixes

| Issue | Grounding | Product consequence | Required correction |
|---|---|---|---|
| `CallSummaryExpanded` is below nominal height | User screenshot: target `367×112`, actual `366.8×108.1`, ΔH≈−3.9dp | A claimed outer-layout rule fails; content appears crowded | Match measured target with accurate content height; adapt if a11y requires growth. Don't hide red Δ behind tolerance. |
| Post-call heading reports “Incoming call” with duration | `CallSummaryIsland.kt` derives `callType` from `CallDirection.INCOMING`; screenshot shows "Incoming call · +1 (555)..." after ending | Sounds like currently ringing when it is already over | Distinguish call **direction** from **phase**: “Call ended · Incoming” / “Incoming · ended”, or short neutral post-call status. Duration static/accuracy-aware, not live green ticking. |
| Four equal CTAs crammed into tiny call summary | `CallSummaryIsland.kt`: four `FlowRow` buttons (Redial, SMS, WhatsApp, Done), 36dp visual each | Mini contact hub instead of focused summary; error-prone taps | One primary useful backed action at most, one lightweight dismiss, optional overflow/secondary only if valid; no four equal pills. |
| Fake caller/number/duration when `record == null` | `CallSummaryIsland.kt`: `isDemoPreview` sets Tamia, sample phone, 02:45; also allows contact actions | Demo data may masquerade as real depending on call path | Render fixtures only through debug data setup. Production null→no call summary or truthful no-record presentation. |
| Derived black placeholder dwarfs the real Island | `PlaygroundGallery.kt` displays an empty `NO_SOURCE` comparison region | UI QA obscured, no credible family review | Replace with small "No direct Figma match · family-derived" source panel, a measured derived-layout diagram, tokens, and explicit QA badge—not a fake comparison image. |
| Noncompact 367px references globally scale wider on wider screens | `IslandFamilyGeometry.resolve`: `t.width * available / 393f` | E.g. 430dp viewport can inflate nominal 367dp to ≈402dp even where family has no such reference | Use family-specific maximum content width and side margins; do not blindly upscale all expanded states. Distinct references at 430 are their own families. |
| Production navigation expanded maps to palette debug screen | `IslandPresentationMapper.kt` maps `LiveActivityKind.NAVIGATION` expanded to `IslandDemoState.ColorOptions` | A user requesting navigation might see colors, not directions | Introduce/route a real navigation-expanded presentation; preserve `ColorOptions` for palette-only debug or intentional customization. Regression test. |
| Main `NotificationExpandedContent` has synthesized notifications when its input list is empty | `DynamicIslandPill.kt` empty-list fallback contains a large fake message list | Production can display ghost/demo notifications | Move samples to debug fixture injector; empty production queue → Idle/Quick Controls, never fabricate entries. |
| `CallAvatars` is used as incoming-call renderer | `IslandPresentationMapper.kt`: `IslandState.IncomingCall → IslandDemoState.CallAvatars` | Group-caller style and real ringing may be conflated | Distinguish `RINGING`, group incoming and active conference; real incoming must show capability-backed Answer/Decline. |
| Existing reports mark everything passed despite unverified derived visuals | `reports/AURORA_V5_FINAL_VERDICT.md` vs user-provided screenshot | False quality gate | Reclassify visual claims by provenance; no pixel-match claim without direct asset, verified cropping/density and side-by-side evidence. |

## 2. Common visual language and layout math

**Primary appearance:** one solid deep black capsule, no generic stacked nested black cards, no oversized empty top/bottom fields, no random gradients, arbitrary glows or iOS control copies without the capability. Preserve family-defined radius and smooth outline; icons from real vectors, images from valid source, proper optical center. The main Island is dominant over secondary Mini-Islands.

**Reference target geometry at 393dp design-equivalent canvas (not unconditional physical sizes):**

| Family | Baseline target | Radius | Layout rule |
|---|---|---|---|
| Owner Minimal visual specimen | 108×32 | 15.9018 | Empty `#DC0917` at opacity 0.60, no text/icon. Separate from runtime camera-aware Idle. |
| Idle | ~127×36 | 18 | Camera-coupled black silhouette, compact tap→Quick Controls. |
| Compact status | State-specific 132/152/156/160/172/189/190/196/222×36.67 | ~18.335 | START anchored icon/art/identity; END anchored metric/status; flexible gap in middle. |
| Contextual alert | 367×85–86 | ~43 | Three optional regions: icon START, title/subtitle START shared baseline, right status/action END. |
| Post-call/other short activity | 367×112 | 42 | Header + maximum one useful compact action row; fixed 112 only if content meets touch/text requirements. |
| Delivery, flight, sports, transport activity | 367×142–148 | 42 | Domain-specific status header + real data visualization/one grounded action. |
| Call/video expanded | 367×166–173 | 42 | Identity/header + capability-backed controls, never ringing controls during connected call. |
| Music expanded | 367×177 | 42 | Art 53×53 + aligned title/artist, scrubber, transport controls. |
| Quick Controls | 367×210 proposal | 42 | Limited useful grid; panel grows when accessibility requires. |
| Mini-Island | parent minus ~32 wide, 50–52 high | ~20 | Newest directly under main; max 1–3 visible; overflow counts pending children. |

**Suggested derived-family inner tokens (proposed, not source-measured):** Compact START inset 10–12dp; END inset 10–12dp; icon region 16–22dp; text-icon gap 6–9dp; key digits tabular. Contextual 86dp: inset 16–20dp horizontally, 11–14dp vertically; 24–36dp icon, two-line text group, trailing metric/action only if supported. Activity 112–210dp: 16–20dp side inset, 10–14dp top/bottom, 7–10dp intra-row gap; deliberate no more than 1–2 action zones unless reference/proven use case requires more. Touch semantics target ≥48dp within actual bounded window or adapt height, never invisibly intercept underlying apps.

**Width equation:** `targetShellWidth = min(familyReferenceMaxAtThisCanvas, realAvailableWidth - safeStart - safeEnd)` for fixed-reference families. Compact width `= max(cutoutRequiredWidth, leadingInset+measuredStart+requiredFlexGap+measuredEnd+trailingInset)` within safe width. **Never uniformly multiply every 367dp target by currentWidth/393.** Keep exactly one owner of insets; don't add duplicate padding at outer shell and child. A 367dp family on a 393dp safe reference canvas has ~13dp outer gutters. At 320dp, compact/hud may shorten content or naturally grow height under accessibility needs.

**Text rules:** Titles 14–16sp effective, primary hierarchy 600/700 weight where appropriate; subtitles 11–12sp with readable contrast; metrics 13–17sp fixed-width tabular numbers and END alignment; ellipsize secondary text first, never hide call identity, next-navigation maneuver, essential timer or safety meaning; primary and secondary lines share exact START x. These are **derived trial tokens**, refine with the local reference fonts and screenshot evidence—do not mislabel them as measured Figma values.

**Motion rules:** Use existing AppMotion, not arbitrary spring on every text element. Preserve START/END anchors during width morph, hide waveforms without real media/recording activity, avoid horizontal layout drift as timers change. No child visual expiry removes canonical queue. Freeze motion for screenshot tests, then test actual motion separately on a real device.

**Colors:** Semantics, not decoration. Green for verified connected/success when applicable, red for stop/destructive/recording plus owner Minimal specimen, cyan/blue for actionable/status where references prescribe, amber for warning. Two distinct functional call actions may have red and green simultaneously. Never paint a static completed call duration green solely because a connected-call timer was green.

**Content & action ownership:** All state text, number, package name, progress, icon, availability, read/unread, call phase and active controls are from source-backed model or explicit debug fixture. UI never makes up a phone number, flight gate, current ETA, live score, WhatsApp availability, permissions or successful OS toggle. A visually attractive disabled state must explain why it's unavailable where useful.

## 3. Post-call summary — fully specified redesign

**Semantics:** `CallSummaryExpanded` exists only after call termination or a true missed/rejected outcome. It is not incoming/ringing and does not show active call controls. `CallDirection` and `CallPhase` are orthogonal. The source provides name, verified duration accuracy, direction, number, capability outcomes and privacy policy.

**Nominal visual specimen:** 367×112 R42; horizontal 16–18dp, vertical 10–12dp. Header row 38–42dp: avatar 36–40dp START; identity group next, first line caller name 15–16sp semibold; second line `Call ended • Incoming`, `Outgoing call ended`, or `Missed call` 11–12sp. Static duration aligned END, 13–14sp tabular, neutral; show `≈` if inferred, `Duration unavailable` only if absolutely needed, omit duration entirely for missed. Never show a ticking green elapsed metric.

**Actions:** A short visual row after 6–8dp gap: **one primary action** based on real capability (`Call back` or `Redial` if number/intent accessible), and a low-emphasis dismiss close or Done. If secondary messaging is available and product-discoverable, place behind a legitimate menu/open-details pathway; do not show four equal 36dp pills. Dismiss is non-destructive to OS history. Use 48dp reachable semantics within the allowed rectangle; if accessibility text grows, grow height explicitly (e.g. ≥124dp) rather than squeezing or wrapping the action row into the 112dp specimen. If the user cannot redial, show a compact status and safe close without disabled clutter.

**Missing record:** no fake Tamia/number/02:45 in production. Populate deterministic fixture through `app/src/debug` only; make Gallery say "Sample call record". Ended summary is low-priority ephemeral, user-expanded remains as policy permits; restore prior valid user-owned pager/music state when it closes. Source number may be sensitive; redact on lock screen and screen share according to policy.

**Device acceptance:** With 393dp/1× fixture, target 367×112±1dp outer shape when eligible; all measured text and actions remain inside by ≥10dp; avatar/name/second-line START alignment, duration END; no content below baseline. Under 1.5× and 2× fonts, height grows and actions reflow safely; no ellipsis of call's meaningful outcome; 4-button grid is forbidden. Include incoming/connected/ended/missed/declined/outgoing/unknown-duration tests.

## 4. Derived-state recipes — all 32 known `FAMILY_ONLY` and `NO_SOURCE`

This matrix is the authoritative starting brief for each currently unreferenced state. Dimensions are **reference-family targets**, not measurements of missing Figma assets. Proposed layouts require product visual review, but engineering must implement and test them instead of leaving placeholders.

| State | Provenance | Nominal geometry | Content composition | Correct action model | Do not do |
|---|---|---|---|---|---|
| Idle | NO_SOURCE | 127×36 R18 | No text/icon. Black camera-aware silhouette; tap Quick Controls; never synthesize queue. | None | No placeholder labels or secondary bubble. |
| CallSummaryExpanded | NO_SOURCE | 367×112 R42; grow only when needed | Top: 36–40dp avatar START; identity 15–16sp and ended/missed context 11–12sp START; valid elapsed duration 13–14sp END. Bottom: one primary action + optional compact dismiss; actions 32–36 visual and safe semantic target. | Call back/Redial only if verified contact/capability; Done/close; optional message in secondary context, not 4 equal CTA. | Do not say “Incoming call” as if still ringing; no green live elapsed timer; no fake WhatsApp/SMS as defaults; never 4 36dp equal actions. |
| NotificationStacked | FAMILY_ONLY | 189×36.67 R18.335 | Main app icon START, count/latest cue END; no inline message or second waveform. | Tap newest-first pager; child stack only if main already expanded. | No +N from all old queued items, no child below compact. |
| NotificationWithMusicCompact | FAMILY_ONLY | 190×36.67 R18.335 + companion 36.7 R18.35 gap≈11 | Main: album art 20–22dp START, ONE waveform 17–20dp END only while playing. Companion: notification icon/count centered. | Tap main→music; companion→notifications as supported. | No waveform beside album art, no duplicate wave, no phantom count. |
| SilentModeCompact | NO_SOURCE | 152×36.67 R18.335 | Bell-slash 17–18dp START; Silent 11–12sp END. 10–12dp inset, vertical center. | Tap contextual detail only if available. | No waveform, no unmute button inside compact. |
| RingerModeExpanded | FAMILY_ONLY | 367×86 R43 | Ringer glyph START, current mode and level/title START, optional backed action END. | Open appropriate settings or supported mode control. | No button without capability; no static 79px CTA at narrow width. |
| VibrateModeCompact | NO_SOURCE | 172×36.67 R18.335 | Vibration glyph START; Vibrate 11–12sp END, stable anchors. | Read mode; supported controls only outside compact. | No equalizer waveform, no timer. |
| NormalModeCompact | NO_SOURCE | 172×36.67 R18.335 | Bell glyph START; Ringer on END; semantic green only if actual enabled. | Tap volume/mode details if supported. | No fake live audio, no centered pair. |
| RingerVolumeCompact | NO_SOURCE | 172×36.67 R18.335 | Speaker START; right-aligned numeric percentage END in tabular digit cell. | OS-backed volume state; read-only unless permitted. | Digits may not shove icon; no waveform. |
| MediaVolumeCompact | NO_SOURCE | 172×36.67 R18.335 | Media-speaker START; numeric % END with fixed-width metric. | OS-backed volume state. | No waveform standing in for loudness; no speculative mute. |
| BluetoothConnecting | NO_SOURCE | 196×36.67 R18.335 | BT/device glyph START; Connecting… / progress END; ellipsis must reserve metric area. | Status only; cancellation only if exposed by source. | No fake Connected, battery, Cancel. |
| ActionControlExpanded | NO_SOURCE | 367×210 R42 baseline; content-adaptive | Compact header then 2×2 grid of <=4 actual capabilities; each control unique icon/title/status, 48dp minimum target inside bounded window; no empty grid cells. | Only actual Quick Controls; permission-gated and truthful unavailable state. | No generic settings dump; no external/system toggles without privilege; no overlap. |
| PremiumExpiryExpanded | NO_SOURCE | 367×112 R42 proposed | App-branded small icon; Aurora Pro + actual expiry; concise one-line benefit/status; one modest manage CTA. | Manage plan only when authenticated/user initiated. | Never mimic OS warning, auto-preempt a call, or use fake countdown. |
| ChargingCompact | NO_SOURCE | 132×36.67 R18.335 | Bolt START; battery percent END in tabular 2–3 digit cell; subtle color only. | Passive, short expiry; source battery level. | No fake ETA or waveform. |
| ChargingExpanded | NO_SOURCE | 367×86 R43 | Battery glyph START; Charging/title + verified battery info START; percentage/OS ETA END when available. | No action by default; optional battery settings. | Do not invent minutes-to-full, no 177dp generic music layout. |
| LowBatteryCompact | NO_SOURCE | 132×36.67 R18.335 | Battery icon START; real percent END; warning tint only when threshold crossed. | Passive glance. | No extra CTA, no music waveform. |
| LowBatteryExpanded | NO_SOURCE | 367×86 R43 | Battery status icon START; Low Battery title and actual %/guidance; no footer unless supported. | Battery settings via valid intent if user initiates. | No auto enabling battery saver, fabricated fast-charge. |
| TimerCompact | FAMILY_ONLY | 222×36.67 R18.335 | Timer ring START, tabular mm:ss END; reserve maximum digits, show source pause accurately. | Tap supported timer detail. | No jumping digits, no waveform, no ticking paused timer. |
| DeliveryCompact | FAMILY_ONLY | 160×36.67 R18.335 | Package/driver glyph START; nearest meaningful ETA/milestone END, no stop-jitter. | Tap source detail where available. | No fake live driver position. |
| DeliveryExpanded | FAMILY_ONLY | 367×144 R42 | Brand/status top; meaningful current milestone; verified ETA/destination lower; progress only if source. | Track only if valid source PendingIntent. | Do not invent nearby-driver countdown, or interrupt on every stop. |
| FlightCompact | FAMILY_ONLY | 160×36.67 R18.335 | Plane/flight code START; gate/time END only if verified; reserve stable short text. | Tap actual flight detail when supported. | No fake status or cutoff. |
| FlightExpanded | FAMILY_ONLY | 367×144 R42 | Flight ID header; departure/arrival code row; status and gate/ETA row when provided. | Open actual flight source only if exposed. | No copy/paste delivery card, no fake gate. |
| SportsCompact | NO_SOURCE | 156×36.67 R18.335 | Team/competition abbreviation START; stable score/time END, reserve text to avoid reflow. | Real source only; demo non-cricket football fixture. | No invented live score or waveform. |
| SportsExpanded | NO_SOURCE | 367×144 R42 | League/status header; two team identities + central score; time/footer; no overloaded stats. | Source open only if backed by intent. | No fictitious live feed, no cricket content. |
| NavigationCompact | FAMILY_ONLY | 156×36.67 R18.335 | Physical maneuver glyph START; unit-formatted fixed-width distance END, text-safe RTL. | Tap turn guidance details, preserve direction. | No 2-line text crammed into 36dp, no decorative wave. |
| FocusModeExpanded | NO_SOURCE | 367×86 R43 | Focus glyph START; active focus name + muted status START; trailing status/control only if supported. | Read-only if DND access absent. | No silent OS DND toggle or fake disable button. |
| TimerImage | NO_SOURCE | 367×86 R43 | Timer or actual provided picture START; task label and fixed-width countdown; right progress only if source. | Pause/cancel only with timer controller; otherwise static demo labeled. | No fake image if no source; no speculative timer actions. |
| CallAvatars | FAMILY_ONLY | 367×86 R43 / expanded as needed | Separate group roster from incoming-call identity: incoming should present caller(s) + real Answer/Decline; group active shows participant count/status only. | Answer/Decline only in RINGING and backed by controller; active controls only after CONNECTED. | Do not map every incoming call to generic group avatars; do not reuse active-call layout for ringing. |
| TransportActivity | NO_SOURCE | 367×148 R42 | Route/vehicle glyph; origin→destination, real ETA at END; next milestone below; progress only if measurable. | Open real trip/route intent if present. | No made-up 3 min platform, progress or End Route for unknown provider. |
| FlightTrackerExpanded | NO_SOURCE | 367×144 R42 | Airline/flight number TOP; origin-code ↔ destination-code MAIN; verified boarding/gate/time BOTTOM. | Open airline/flight source if source capability exists. | No fake SFO–JFK gate, inferred ETA, waveform. |
| SubscriptionPricing | NO_SOURCE | 367×162 R42 proposal; in-app only | Aurora branding, real plan name, local price if loaded, 1 primary CTA; secondary dismiss. | Open subscription flow only after explicit user request. | No intrusive overlay, no invented prices, no fake urgency. |
| MovieCard | NO_SOURCE | 367×142 R42 | Poster 45–56 wide if genuine; movie/session, real venue/date and seat; one ticket/open action if exists. | Ticket details intent only when source-backed. | No invented booking, movie, or seat. In-gallery sample marked synthetic. |

## 5. Behavior/persona compatibility — preserve the existing product

**Six personas:** (1) Quick Glance: brief readable preview and fast passive collapse; (2) Focused Reader: preserve stable selection and inline-reply draft; (3) Heavy Notification: newest-first 50-item pager and accurate pending-child overflow; (4) Multitasker: audio, call, timer and navigation arbitration/restoration; (5) Power User: real Quick Controls and intentional OS-dismiss preferences, not fake toggles; (6) Privacy/Accessibility: lock/redaction/TalkBack, reduced motion, RTL, font scaling, large safe targets.

**UX invariants:** Idle tap→Quick Controls; one message tap→detail; multiple→newest-first key-stable pager; ordinary arrivals never hijack user reading; selected source removal chooses next else previous; default `×` hides in Island only, OS shade untouched; meaningful-update reappearance, not incidental repaint; child only while occupied expanded main, tap child promotes, visible child range 1–3 adaptive, newest first, independent correlated timers, touch pause and expiry merge without deleting canonical notification; call preemption with bounded valid restoration. A call summary cannot preempt an ongoing ringing call and must never fake a ringing call.

**Priority:** privacy/permissions, legitimate calls/alarm/navigation urgency, active reply or protected user-owned presentation (with safe call interruption), user-selected pager/live activity, media/recording, ordinary notification preview, small system HUD, idle. Keep existing policy implementation; change only where source mapping contradicts it. Preview and child timing remain engine-owned, not Composable-owned.

**Availability:** Android `TYPE_ACCESSIBILITY_OVERLAY` is the real production service; permission must be legitimately granted and device-tested. Source-less or platform-unsupported demonstrations remain Playground-only with an unambiguous fixture badge. No arbitrary NotificationManager cancellation, unbacked call control, airplane-data switching, Bluetooth management, screen mirroring termination or external app messaging.

## 6. Gallery & provenance QA — stop pretending that missing source means a blank comparison

The Gallery must distinguish `LIVE` vs `REFERENCE` vs `DERIVED DESIGN` and show exactly which artifact or family was used. For DIRECT, source PNG/SVG plus crop/scale calibration, live Compose snapshot, 50% alpha overlay, difference heatmap, outer AND inner measurements. For FAMILY_ONLY, a family exemplar clearly labelled **not the same state** and a proposed layout specification side by side; no pixel-perfect PASS. For NO_SOURCE, the actual final derived design and an annotated measurement diagram with text/CTA/gutters, no fabricated reference artwork or giant empty black capsule. User screenshot for #7 shows approximately half the Gallery preview consumed by a misleading blank Figma asset region: eliminate this presentation.

Use three independent gates:
- **Geometry gate:** target vs measured shell width/height/radius at controlled density; tolerance typically ±1dp at matched reference viewport for direct matches. A derived proposal may flex when text/accessibility requires it; document difference.
- **Internal-composition gate:** icon/shape bounds, start/end/vertical optical gravity, title/subtitle shared x, content gaps and button row alignment. ±2dp only for accurately grounded direct-reference anchors; derived states reviewed for family consistency, readability and touch safety rather than a nonexistent pixel target.
- **Behavior/capability gate:** real state source, accurate text, actions, redaction, stable key, gestures and transition flow. Zero invented available actions, duplicate dispatch or source-unsupported data.

**Tests across widths:** 320, 360, 393, 430dp; 1.0×, 1.5×, 2.0× fonts; LTR/RTL; minimal notification and max text; missing icon; sparse data; landscape/short height; three-child stack; 50 queue items; reduced motion; status bar and camera cutout offsets. Press/drag targets and actual overlay touch bounds require on-device instrumentation, not screenshot-only inference.

## 7. Required failure tests and release blockers

- CallSummary: screenshot 366.8×108.1 fails target 367×112 and 4 equal CTAs fail layout. No more green metric after call ended. Both actual duration and duration-unknown.
- `NO_SOURCE` gallery cannot show a black dummy pretending to be the Figma source. No false "ALL GATES PASSED" or fake pixel-match label.
- Real incoming/ringing (`IncomingCall`) differs from active connected (`CallExpanded`) and ended (`CallSummary`). Verify `IncomingCall → CallAvatars` mapping does not misrepresent group calls or real Answer/Decline.
- Expanded navigation must not map to `ColorOptions`. The design palette state must not become driving guidance.
- Production empty `NotificationExpanded` must not synthesize demo WhatsApp/messages entries.
- Never automatically width-scale every expanded 367dp reference at 430dp; 393dp source geometry must remain stable.
- Actual real-world call actions, WhatsApp/SMS, system controls and redial must be capability-checked before rendering enabled UI.
- No duplicate call-answer/end, stale child timeout or OS notification cancellation; preserve notification queue and reply drafts.
- No source mismatch between Playground and production renderers.
- No app release PASS while `TYPE_ACCESSIBILITY_OVERLAY` touch and privacy flows remain untested on a legitimately bound device.

## 8. Required design artifacts and traceability

For each of all 60 states, produce a design record with: `state enum`, `provenance`, `reference/family ID`, `semantic purpose`, `call/notification phase`, `data model`, `geometry at reference width`, `start/center/end/bottom slots`, `typography`, `icon size`, `content gaps`, `CTA visibility`, `privacy`, `animation/priority`, `fallback`, `on-device screenshot`, `QA gate outcome`. The 32 state proposals in Section 4 MUST be complete; 28 DIRECT states preserve original measured styling and may require parity fixes separately. At least one screenshot per state, plus call-phase and notification-pager/mini interactions. A pixel match only counts when an exact direct reference and comparison evidence exist.

*Source basis: actual reviewed GitHub files at b613666, owner-provided gallery screenshot of CallSummaryExpanded, original Figma export family and approved Aurora behavior specifications. All design numeric values for missing-reference states are architect proposals unless explicitly labelled source-measured.*
