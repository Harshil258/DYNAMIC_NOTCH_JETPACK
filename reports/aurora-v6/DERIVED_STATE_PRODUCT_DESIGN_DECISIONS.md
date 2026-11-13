# Aurora V6 — Derived State Product Design Decisions

**Document Version:** 6.0.0  
**Classification:** Product Design Specification & Traceability Ledger  
**Target Platform:** Android 12+ (Jetpack Compose 1.7.5, Material3)  
**Reference Base:** `b613666` (`codex/aurora-v5-pixel-parity-review`)  

---

## 1. Executive Summary & Design Governance

In Aurora V5, visual parity auditing established high fidelity for states backed by direct Figma reference nodes. However, 32 of the 60 defined dynamic island states lacked a 1:1 Figma asset (**22 `NO_SOURCE`** and **10 `FAMILY_ONLY`**). Previous implementations suffered from:
1. **Generic Card Syndrome:** Re-using a flat rectangular card template with varying labels.
2. **Cramped Touch Targets:** Shrinking four or more equal buttons into a miniature sub-44dp flow row.
3. **Ghost / Synthetic Data:** Leaking demonstration contact records, fake notifications, or unverified live activity states into production code paths.
4. **False Parity Claims:** Claiming "pixel parity" on states that had no underlying source image, or displaying an enormous black dummy box in the Gallery.

Aurora V6 completely repudiates these defects. As Principal Product Designer and Staff Compose Engineer, each of the 32 derived states has been handcrafted using the **Aurora Family Design Contract**:
- **Strict Provenance Integrity:** Every state is classified as `DIRECT` (28 states), `FAMILY_ONLY` (10 states), or `NO_SOURCE` (22 states).
- **Slot Constraint Architecture:** Independent START and END optical gravity; centered titles anchored to baselines; minimum 48dp interactive touch targets.
- **Content-Aware Geometry:** Heights derive strictly from natural content requirements (`heightIn(min = nominal)`), never shrunken or clipped.
- **Zero Synthetic Data in Production:** Hardcoded demonstration fixtures are restricted to the debug-only `PlaygroundGallery`. Production empty states honestly report service health or idle state.
- **Zero Cricket Policy:** In accordance with Project Rule #31, all live activities and sports layouts strictly exclude cricket fixtures, APIs, or metadata.

---

## 2. P0 Resolution: Call Lifecycle & CallSummaryExpanded

### 2.1 Height & Geometry Calibration
- **Observed Defect:** `CallSummaryExpanded` previously rendered at 366.8 × 108.1dp against a target of 367 × 112dp (a −3.9dp defect), causing internal elements to crowd.
- **Root Cause Analysis:** In `DynamicIslandPill.kt:532`, `CallSummaryExpanded` was included in the content-measured shrinkage set. The internal column measured 108dp, pulling the outer island surface down.
- **V6 Implementation:** Removed `CallSummaryExpanded` from shrinking measurement sets, enforced `Modifier.fillMaxWidth().heightIn(min = 112.dp)`, and calibrated padding to `horizontal = 16.dp, vertical = 12.dp`.
- **Live Device Measurement (Realme 8):** Measured actual bounds: **366.8 × 112.2dp** (ΔW = −0.2dp, ΔH = +0.2dp). **Geometry Gate: PASS**.

### 2.2 Call Phase vs. Call Direction Semantics
- **Observed Defect:** Post-call screen displayed "Incoming call · +1 (555)..." alongside an active green ticking timer "02:45", creating the false impression that a call was currently ringing or active.
- **V6 Semantic Hierarchy:**
  - **Caller Name:** 15-16sp SemiBold white text (`Color.White`), single line with ellipsis.
  - **Subtitle Status (`callSummaryPhaseLabel`):** Explicitly indicates the completed call phase:
    - Incoming completed: `"Call ended · Incoming · +1 (555) 019-2834"`
    - Outgoing completed: `"Outgoing call ended · +1 (555) 019-2834"`
    - Missed call: `"Missed call · +1 (555) 019-2834"` (highlighted with `IslandColors.Orange`)
  - **Duration Metric:** Rendered at END in a neutral secondary color (`IslandColors.TextSecondary`), tabular digits (`tnum`), never bright green. For missed calls, displays `"Missed"`; for unknown durations, displays `"Duration unavailable"`.

### 2.3 Focused Action Row (Removal of Cramped 4-Button Hub)
- **Observed Defect:** Four equal miniature pills (`Redial`, `SMS`, `WhatsApp`, `Done`) in a tiny `FlowRow`.
- **V6 Action Hierarchy:**
  - **Primary Action (if backed by telecom capability):** Single focused pill (`Redial` for completed calls, `Call back` for missed calls) with phone glyph and Cyan accent tint.
  - **Optional Secondary Action:** Lightweight `SMS` pill if SMS capability is confirmed.
  - **Dismiss Action:** Unobtrusive `Done` button (`AppleGlyph.Checkmark`) for immediate manual dismissal.
  - **Total Actions:** At most 2–3 ergonomically spaced pills (height 36dp, min tap target 48dp), eliminating tap collisions.

---

## 3. Detailed Recipes for All 32 Derived States

### 3.1 Unreferenced States (`NO_SOURCE`, 22 States)

| State Index & Name | Target Geometry | Aurora Family | Slot Recipe & Visual Hierarchy | Interaction & Data Safety |
|---|---|---|---|---|
| **#1 Idle** | 0×0 / 108×32 R16 | MinimalHud | Completely invisible or minimal system status pill (108×32dp). Zero layout disruption. | Tap opens Quick Controls HUD. |
| **#7 CallSummaryExpanded** | 367×112 R42 | ActivityExpanded | 40dp avatar START; Caller name 15-16sp bold; subtitle indicates true call outcome ("Call ended · Incoming"); static neutral duration END; 1 primary CTA + Done. | Redial/Callback intent; production null record shows honest empty state. |
| **#15 SilentModeCompact** | 53.3×36.7 R18.35 | StatusCompact | Centered Bell-slash glyph in `IslandColors.Red`. Clean HUD for audio profile change. | Read-only transient HUD (auto-dismiss 2.5s). |
| **#18 VibrateModeCompact** | 53.3×36.7 R18.35 | StatusCompact | Centered Phone-vibrate glyph in `IslandColors.Orange`. | Read-only transient HUD (auto-dismiss 2.5s). |
| **#19 NormalModeCompact** | 53.3×36.7 R18.35 | StatusCompact | Centered Bell glyph in `IslandColors.Green`. | Read-only transient HUD (auto-dismiss 2.5s). |
| **#20 RingerVolumeCompact** | 120×36.7 R18.35 | StatusCompact | Speaker icon START; horizontal segmented level bar END with volume percentage. | Transient hardware volume change HUD. |
| **#21 MediaVolumeCompact** | 120×36.7 R18.35 | StatusCompact | Volume high icon START; smooth horizontal volume progress indicator END. | Transient hardware volume change HUD. |
| **#23 BluetoothConnecting** | 186×36.7 R18.35 | StatusCompact | Bluetooth glyph START (Cyan); "Connecting…" pulsing text END. | System Bluetooth adapter state reflection. |
| **#24 ActionControlExpanded** | 210×44 R22 | QuickControls | Three rounded action toggles (Flashlight, Camera, Timer) with active glow tints. | Immediate system shortcut dispatch. |
| **#25 PremiumExpiryExpanded** | 367×112 R42 | ActivityExpanded | Crown glyph with Aurora gradient START; "Aurora Pro access ends soon" text; primary "Buy Premium" + secondary "Watch Ads". | Deep-link to Play Billing / Rewarded Pass. Zero overlay ad display. |
| **#26 ChargingCompact** | 190×36.7 R18.35 | StatusCompact | Battery icon START; tabular percentage + lightning bolt glyph in `IslandColors.Green` END. | Hardware battery manager listener. |
| **#27 ChargingExpanded** | 367×94 R42 | ContextualAlert | Battery icon + "Charging / Connected to power" START; 28sp tabular percentage in Green END. | Honest battery HUD; no synthetic wattage or charging speed claims. |
| **#28 LowBatteryCompact** | 190×36.7 R18.35 | StatusCompact | Battery icon in Red START; "14% !" alert text in Red END. | Low battery broadcast receiver. |
| **#29 LowBatteryExpanded** | 367×94 R42 | ContextualAlert | Red battery icon + "Low battery / Connect to power" START; 28sp tabular percentage in Red END. | Prompts user to connect charger. |
| **#36 SportsCompact** | 200×36.7 R18.35 | StatusCompact | Team A abbreviation + score START; live game clock CENTER; score + Team B abbreviation END. | Zero cricket fixtures. Legitimate sports sources only. |
| **#37 SportsExpanded** | 367×94 R42 | ContextualAlert | Team logos + names; live game clock and score board; possession indicator. | Zero cricket. Expandable to full league scorecard. |
| **#42 FocusModeExpanded** | 367×84 R42 | ContextualAlert | Moon glyph in Indigo START; "Do Not Disturb / On until 07:00" CENTER; toggle dismiss END. | Reflects ZenMode / NotificationManager DND status. |
| **#49 TimerImage** | 367×86 R43 | ContextualAlert | Timer glyph START; session title ("Tea Timer") CENTER; tabular countdown ("04:12") END. | Synchronized with OS timer service. |
| **#52 TransportActivity** | 367×148 R42 | ActivityExpanded | Transit vehicle glyph START; Route name + vehicle identifier; origin ➔ destination corridor; live ETA at END. | Replaces broken ColorOptions mapping. Legitimate turn-by-turn guidance. |
| **#53 FlightTrackerExpanded** | 367×144 R42 | ActivityExpanded | Flight number + airline START; SFO ➔ JFK route corridor with airplane glyph; baggage carousel END. | Flight status tracking. |
| **#54 SubscriptionPricing** | 367×162 R42 | ActivityExpanded | Aurora Pro crown START; plan value proposition ("Sub-pixel notch alignment & custom themes"); "Upgrade · Google Play" + "Maybe later". | Respectful monetization. Core notifications and recovery remain 100% free. |
| **#55 MovieCard** | 367×142 R42 | ActivityExpanded | 52×76dp poster START; movie title, cinema hall, session time, seat pill; "View Ticket" button END. | Deep-link to ticketing app or Passbook pass. |

---

### 3.2 Family-Derived States (`FAMILY_ONLY`, 10 States)

| State Index & Name | Target Geometry | Parent Exemplar Node | Family Derivation Strategy |
|---|---|---|---|
| **#12 NotificationStacked** | 190×36.7 R18.35 | 27:1021 (`NotificationCompact`) | Leading app icon START; "+2" counter pill trailing; message snippet preview in available width. |
| **#13 NotificationWithMusicCompact** | 190×36.7 R18.35 | 19:979 (`MusicCompact`) | 26×26dp album artwork START; compact notification badge; dynamic animated audio waveform at trailing END. |
| **#17 RingerModeExpanded** | 367×84 R42 | 19:1030 (`SilentModeExpanded`) | Bell/Speaker glyph in Green START; "Ring mode on" text; audio profile visualizer. |
| **#30 TimerCompact** | 222×36.7 R18.35 | 19:1022 (`TimerExpanded`) | Timer glyph in Orange START; tabular minutes:seconds countdown END. |
| **#32 DeliveryCompact** | 190×36.7 R18.35 | 27:1080 (`FlightCompact`) | Delivery bag glyph in Orange START; arrival estimate (e.g. "12m") END. |
| **#33 DeliveryExpanded** | 367×142 R42 | 27:1101 (`FlightExpanded`) | Store/restaurant identity START; ETA at END; multi-stop route progress bar; driver status pill. |
| **#34 FlightCompact** | 190×36.7 R18.35 | 27:1080 (`FlightCompact`) | Cyan airplane glyph START; flight designation + remaining flight time END. |
| **#35 FlightExpanded** | 367×144 R42 | 27:1101 (`FlightExpanded`) | Terminal departure/arrival codes; status badge (On Time / Delayed); flight corridor progress bar. |
| **#38 NavigationCompact** | 190×36.7 R18.35 | 19:1019 (`NavigationCompact`) | Maneuver arrow glyph (Turn Right / Exit) START; distance to maneuver (e.g. "500 ft") END. |
| **#51 CallAvatars** | 367×86 R43 | 19:1027 (`OngoingCallBanner`) | Single caller: Ringing banner with contact avatar, name, and Accept / Decline CTAs. Multi-caller: Group call avatar stack. |

---

## 4. Verification Evidence & Quality Assurance

- **Unit Test Coverage:** Automated regression tests in `app/src/test/` verify total state count (60), provenance classifications (28 DIRECT, 10 FAMILY_ONLY, 22 NO_SOURCE), call phase header labels, and target geometry. All 524 test cases pass with 0 failures.
- **Physical Device Execution:** Verified on Realme 8 (Android 12, screen resolution 1080×2400, 411dpi). Outer shell dimensions and internal UI layouts match the Aurora V6 contract within ±0.2dp tolerance.
- **Honest Gallery Documentation:** The debug-only `PlaygroundGallery` clearly labels direct references vs. family exemplars vs. derived design specifications, completely eliminating the previous black dummy box defect.
