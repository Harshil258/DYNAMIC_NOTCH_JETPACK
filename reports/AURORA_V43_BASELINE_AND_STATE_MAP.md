# Aurora V4.3 baseline and source map

Baseline: `817853f`, branch `visual-redesign/task-ui-02b-premium-refinement`.
The required reviewed commit `c5e98d76f118bdb16211e36ab85bf44bd5c28505` is an ancestor.
Task05 branch is `23f75e5b69df217630d9d6940f65b4e1333bfb4f`; five subsequent commits were retained.
Review branch: `codex/aurora-v43-reference-ui`. No main merge or release.

Authority: all three original package files preserved under `AURORA_V4_3_CODEX_IMPLEMENTATION_PACKAGE/`.
Real source enum has exactly60 entries. Geometry is now explicit in `IslandFamilyGeometry.kt`.
Production dispatch remains in `DynamicIslandPill.kt`, source-backed rendering takes precedence.

Baseline defects: expanded family width used universal22dp margin; image variants were136dp;
compact music156dp/call118dp/timer128dp; Minimal inherited36.67dp and companion.
Several reference states aliased unrelated airplane/mirroring/mobile-data/transit cards.

| State | Target | Composition | Authority |
|---|---|---|---|
| Idle | 127×36 R18 | empty surface; align to measured cutout | MEASURED |
| Minimal | 108×32 R15.9018 | no content; owner red #DC0917 at 0.60 | OWNER |
| MusicCompact | 190×36.67 R18.335 | L artwork 22, T waveform 17; no fabricated title crowding | IMPLEMENTATION |
| MusicExpanded | 367×177 R42 | L art 53×53 at x24; C aligned track/artist; T waveform; B scrubber+transport | MEASURED |
| CallCompact | 152×36.67 R18.335 | L phone/active glyph; T stable time; controls only if space | IMPLEMENTATION |
| CallExpanded | 367×166 R42 | L avatar 44; C caller+medium; B 5 capability-gated controls, pitch64 | MEASURED |
| CallSummaryExpanded | 367×112 R42 | L call icon; C identity+duration; optional valid callback CTA | IMPLEMENTATION |
| FaceTimeCallExpanded | 367×168 R42 | L avatar; C video-call identity; B available video/call actions | IMPLEMENTATION |
| SharedMediaCallExpanded | 367×172.64 R42 | L call identity; C active shared-media label; B valid controls | IMPLEMENTATION |
| OngoingCallBanner | 367×86 R43 | L call status; C caller+elapsed; T action when supported | IMPLEMENTATION |
| NotificationCompact | intrinsic 120–250×36.67 R18.335 | L app glyph; C short label; T numeric count; no 3-line message | IMPLEMENTATION |
| NotificationStacked | main 120–250×36.67 R18.335; expanded stack uses MINI | compact main is a count cue; children ONLY under expanded main | IMPLEMENTATION |
| NotificationWithMusicCompact | 190×36.67 R18.335 + 36.7 bubble, gap11 | L media; T alert count; preserve media identity | IMPLEMENTATION |
| NotificationExpanded | 367×86–162 R43 (86); R42 (142+) | L app icon; C sender+message; B actual actions/reply; pager fixed header | MIXED |
| SilentModeCompact | 152×36.67 R18.335 | L bell-slash; T silent status | IMPLEMENTATION |
| SilentModeExpanded | 367×86 R43 | L glyph; C Silent mode/On; T 79×34 Unmute if supported | MEASURED |
| RingerModeExpanded | 367×86 R43 | L ringer glyph; C mode/level; T real control | IMPLEMENTATION |
| VibrateModeCompact | 172×36.67 R18.335 | L vibrate glyph; T mode | IMPLEMENTATION |
| NormalModeCompact | 172×36.67 R18.335 | L sound glyph; T active mode | IMPLEMENTATION |
| RingerVolumeCompact | 172×36.67 R18.335 | L sound glyph; T level with fixed-width numerals | IMPLEMENTATION |
| MediaVolumeCompact | 172×36.67 R18.335 | L media sound; T level with fixed-width numerals | IMPLEMENTATION |
| BluetoothConnected | 196×36.67 R18.335; contextual 366×95 R46 | compact L device/T state; contextual L thumb/C name/T ring | MIXED |
| BluetoothConnecting | 196×36.67 R18.335 | L BT glyph; T spinner/progress, no fake cancel | IMPLEMENTATION |
| ActionControlExpanded | 367×210 R42, dynamic height | real capabilities only; grid aligns L/C/T, target48 each | IMPLEMENTATION |
| PremiumExpiryExpanded | 367×112 R42 (proposal) | L entitlement glyph; C expiry; B real settings action; low priority | PROPOSED |
| ChargingCompact | 132×36.67 R18.335 | L charging bolt; T percentage fixed-width | IMPLEMENTATION |
| ChargingExpanded | 367×86 R43 | L battery icon; C charge label/ETA; T percent | IMPLEMENTATION |
| LowBatteryCompact | 132×36.67 R18.335 | L battery; T percent/status | IMPLEMENTATION |
| LowBatteryExpanded | 367×86 R43 | L warning/battery; C actionable message; T if valid CTA | IMPLEMENTATION |
| TimerCompact | 222×36.67 R18.335 | L timer ring; T tabular countdown fixed width | IMPLEMENTATION |
| TimerExpanded | 367×85.73 R43 | L pause + cancel as real actions; T Timer label + countdown | MEASURED |
| DeliveryCompact | 160×36.67 R18.335 | L package; T meaningful milestone | IMPLEMENTATION |
| DeliveryExpanded | 367×144 R42 | L package/status; C milestone/ETA; B supported Track action | IMPLEMENTATION |
| FlightCompact | 160×36.67 R18.335 | L flight glyph; T code/time | IMPLEMENTATION |
| FlightExpanded | 367×144 R42 | L flight identity; C route/gate/status; B real source action | IMPLEMENTATION |
| SportsCompact | 156×36.67 R18.335 | L team/sport glyph; T score/time; no cricket fixture | IMPLEMENTATION |
| SportsExpanded | 367×144 R42 | L teams; C score/state; B supported Open source | IMPLEMENTATION |
| NavigationCompact | 156×36.67 R18.335; optional 408×96 R44 | L maneuver; T metric; large variant two-line only if justified | MIXED |
| VoiceMemoExpanded | 367×86 R43 | L red waveform; T elapsed/real Stop action if app-owned | MEASURED |
| ScreenRecordingExpanded | 367×86 R43 | L red record dot; C Recording/elapsed; T real stop action | MEASURED |
| ShortcutExpanded | 367×85.08 R43 | L shortcut glyph; C result/progress; T completion status | MEASURED |
| FocusModeExpanded | 367×86 R43 | L focus glyph; C state; T settings/capability-backed CTA | IMPLEMENTATION |
| AirPodsConnected | 367×86 R43 (atlas) / 366×95 R46 (community) | L earbuds/device; C name + Connected; T battery ring | MEASURED |
| SatelliteConnected | 367×86 R43 | L satellite ring; C connected/status; T real action or none | MEASURED |
| FindMyAlert | 367×86 R43 | L concentric ring; C single line alert; no forced trailing | MEASURED |
| MovedToIPhone | 367×86 R43 | L device glyph; C handoff status; T Undo 70×34 if valid | MEASURED |
| VideoRemoteExpanded | 367×177 R42 | L video thumb; C playing text; B scrub ±15s and real output controls | MEASURED |
| AirDropActivity | 367×86 R43 | L rings + thumbnail; C transfer/from sender; T progress ring | MEASURED |
| TimerImage | 367×86 R43 (contextual); small compact only if separate variant | L timer image; C timer identity; T stable time | IMPLEMENTATION |
| NotificationImage | 367×86–126 R43/R44 by content | L thumbnail if safe; C sender/body; T count; no clipping | MIXED |
| CallAvatars | 367×86 R43; expandable to 166 R42 if real actions | L multi-avatar; C group/caller; T real controls | IMPLEMENTATION |
| TransportActivity | 367×148 R42 | L vehicle/route; C ETA/status; B real supported action | IMPLEMENTATION |
| FlightTrackerExpanded | 367×144–162 R42 | L route glyph; C departure/arrival; B status and source CTA | IMPLEMENTATION |
| SubscriptionPricing | 367×162 R42 | L app promo; C price/benefit; B honest optional CTA; never intrusive | IMPLEMENTATION |
| MovieCard | 367×142 R42 | L poster; C film/session; B actual ticket action or none | IMPLEMENTATION |
| ColorOptions | 367×185.3 R42 | swatches in intentional rows; Playground-only fixture | IMPLEMENTATION |
| AirplaneAlert | 367×148 R42 | L airplane glyph; C status explanation; B settings CTA only | MEASURED |
| ScreenMirroringAlert | 367×144 R42 | L screen icon; C mirroring status; T supported stop CTA | MEASURED |
| MobileDataAlert | 367×162 R42 | L network glyph; C message; B two real CTA pills | MEASURED |
| TransitRouteAlert | 367×142 R42 | L train; C route/station; B validated End Route CTA | MEASURED |
