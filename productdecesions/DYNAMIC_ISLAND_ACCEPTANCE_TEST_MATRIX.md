# Dynamic Island — Multi-Directional Acceptance Scenarios

**Status:** Product acceptance targets, NOT test execution results. **Key**: A = automatic normal event, U = user action, C = critical event, P = platform/lifecycle. Each test must run through the canonical production policy in the Playground; platform-only interactions additionally need an actual overlay / device test. Confirm notification identity, selectedKey, screen bounds, queue count, action dispatch, and final UI state.

| ID | Context / event sequence | Expected observable result | Test priority |
|---|---|---|---|
| A01 | Idle, zero active items -> tap | Quick Controls open (C01) | P0 |
| A02 | Compact one ordinary message -> tap | Expanded message details, OS notification untouched (C02) | P0 |
| A03 | Compact 12 eligible notification keys -> tap | Horizontal pager opens, newest at first (C03) | P0 |
| A04 | Compact 50 mixed messages -> tap | Pager handles all eligible keys without duplicates or blank cards | P0 |
| A05 | Compact 1 message -> auto preview completes | Compact badge still represents notification; no OS cancellation | P0 |
| A06 | Transient preview reading; normal second arrives | Main preview stays; child emerges below; counts update | P0 |
| A07 | Transient preview -> timeout while child exists | Main and child merge/collapse visually; both queue keys retained | P0 |
| A08 | User-expanded pager page five -> new normal message | Selected key/page remains; New indicator and child emerge (C04) | P0 |
| A09 | User-expanded pager page five -> 3 notifications in 100ms | Main unchanged; children newest-first with safe overflow | P0 |
| A10 | User-expanded pager page five -> ranking reorder | Selected stable key unchanged | P0 |
| A11 | User-expanded pager page five -> same-key minor update | Card content updates in place; no new page, no new child | P0 |
| A12 | User-expanded pager page five -> new attachment/artwork | Key unchanged, async artwork cannot overwrite newer record | P1 |
| A13 | Source removes currently selected middle pager item | Next surviving key selected; remains expanded (C05) | P0 |
| A14 | Source removes currently selected final pager item | Previous surviving key selected (C05) | P0 |
| A15 | Source removes non-selected item | Current selected key unchanged; count decrements | P0 |
| A16 | Source removes final eligible item, no activity | Idle compact; tap Quick Controls | P0 |
| A17 | User clicks ×, default setting | Removes Island entry only; OS shade notification remains (C06) | P0 |
| A18 | User clicks ×, optional OS-dismiss, clearable & permitted | Cancel original Android notification exactly once; if unsupported explain failure | P0 |
| A19 | User clicks ×, OS-dismiss chosen but non-clearable | Never claim success; preserve source item or Island-only fallback per explicit safety policy | P0 |
| A20 | User × removes last island entry, 0 activities | Directly idle compact (C07) | P0 |
| A21 | User Island-dismisses same key -> progress 60->70 | Suppressed, no expansion or new child (C08/C09) | P0 |
| A22 | User Island-dismisses delivery -> first '2 stops away' | Meaningful resurrection once, with preview if compact (C09/C10) | P0 |
| A23 | Same delivery -> 2 stops -> 1 stop | Do not repeatedly resurrect (C09) | P0 |
| A24 | Same delivery -> arriving now | Meaningful resurfacing; maintain unique queue key | P0 |
| A25 | Same delivery -> delivered | Meaningful milestone; display with non-misleading completed state | P0 |
| A26 | Same delivery -> ETA +/- 1 min | Suppressed unless real threshold/event justifies status change | P1 |
| A27 | Same-key messaging reply adds new message timestamp/body | Resurface only on genuinely new content, not timestamp-only repaints | P0 |
| A28 | Source removes key; later reposts genuinely new key | Treat as new notification; suppression for removed key cleared | P0 |
| A29 | Expand main; second new child arrives | Child appears directly below main with emerging motion (C11/C14) | P0 |
| A30 | Two existing children; third arrives | Third becomes top, old children move down smoothly | P0 |
| A31 | Small screen stack 4 arrivals | Adaptive 1–2 visible children; other new items +N | P0 |
| A32 | Large screen stack 5 arrivals | At most 3 visible children; overflow +N | P0 |
| A33 | Four children while keyboard visible | Stack safely shrinks/suppresses; does not cover reply UI | P0 |
| A34 | Tap Telegram child during WhatsApp main | Telegram morphs into main; WhatsApp stays in pager (C12) | P0 |
| A35 | Child expires untouched | Visually merges into main, queue unchanged (C15) | P0 |
| A36 | New child enters while old child near timeout | Old independent timer not restarted (architect C16) | P0 |
| A37 | Finger held on child past timeout | Child remains visible while touch active | P0 |
| A38 | Release touched child | Smart grace then visual merge, no queue deletion | P0 |
| A39 | Source removes child midway through entry animation | Cancel animation; no ghost child/touch target | P0 |
| A40 | Burst of 25 notifications | Queue bounded by actual active items; compact +N indicator, no animation storm | P0 |
| U01 | Swipe up expanded notification | Collapse only; OS shade and queue unchanged | P0 |
| U02 | Tap outside user-expanded pager on supported device | Collapse without stealing underlying button touch | P0-device |
| U03 | Tap outside while full-width transparent animation layer exists | Underlying target remains clickable, or safe explicitly documented fallback | P0-device |
| U04 | Compact notification rapidly double tapped | Only intended single action; new × never accidentally triggered | P0-device |
| U05 | Long press with small finger drift | Recognized within native touch slop; no unintended swipe | P0-device |
| U06 | Diagonal swipe inside pager | Exactly one intent: horizontal page OR vertical collapse, never both | P0-device |
| U07 | Pager fast fling right, reverse mid-gesture | Stable key settles to one valid page without snapping out | P1-device |
| U08 | Swipe with RTL layout | Correct logical direction and accurate indicator | P1-device |
| U09 | Tap × while card is animating | Only intended stable target is actionable; no neighbor hit | P0-device |
| U10 | Main expanded empty background tapped | No destructive action or accidental app switch | P0 |
| U11 | Tap explicit notification Open action | Uses available content intent, handles failure without dropping item | P1-device |
| U12 | Reply action supports RemoteInput -> send | Sent once, no duplicate dispatch; UI updates on actual result | P0-device |
| U13 | Reply button not offered by source | No fake Reply UI; honest fallback | P0 |
| U14 | Typing reply -> new normal notification | Keyboard/focus and selected key stay; silent child/badge only | P0-device |
| U15 | Typing reply -> outside tap | IME closes and island collapses safely; ephemeral draft rule honored | P1-device |
| U16 | Explicit Back while user-expanded | Collapse to compact; no OS shade cancel | P0-device |
| U17 | Press and hold idle | Opens customization/settings; idle single tap remains Quick Controls | P1 |
| U18 | TalkBack pager next action | Moves to next valid key without requiring a swipe | P0-device |
| C01 | Main normal preview -> real incoming call | Call surface preempts; current notification preserved | P0-device |
| C02 | User pager open -> short declined call | Returns to prior selected key if still active, unlocked | P0 |
| C03 | Long call ends | Restore compact with active activities, not stale expanded overlay | P1 |
| C04 | Call source lacks answer PendingIntent | Hide/disable unsupported answer action; safe app/system handoff | P0-device |
| C05 | Ongoing music + normal message | Brief message preview then music primary + notification affordance | P0 |
| C06 | User-expanded media + new message | Controls remain visible; message is child mini | P0 |
| C07 | Music + timer completes | Timer completion visible with priority, music remains active | P0 |
| C08 | Navigation turn-now + low-priority email | Nav stays visible; email queued, no attention hijack | P0 |
| C09 | Screen recording active + new message | Recording indicator persists; sensitive content policy applied | P1 |
| C10 | Incoming call during reply | Don't send draft; suspend safely; show system-capable call controls | P0-device |
| C11 | Flight/sports/delivery live update | Update in place without duplicating keys or forcing unrelated main navigation | P1 |
| C12 | Battery low/charging/volume HUD while pager open | Coalesce transient HUD, don't overwrite selected notification | P1 |
| P01 | Lock screen activated during expanded sensitive message | Hide/redact content immediately, no stale touch overlay | P0-device |
| P02 | Unlock while queue still active | Safely restore compact, never expose stale sensitive text automatically | P0-device |
| P03 | Notification Access revoked | No crashes/ghost notifications; user-friendly settings guidance in own app | P0-device |
| P04 | Overlay permission revoked | Remove window immediately and report capability disabled safely | P0-device |
| P05 | Listener restarts with active notifications | Reconcile state using actual keys, no phantom duplicates or re-alert storm | P0-device |
| P06 | Phone rotates / cutout offset changes | Main and children reposition into safe bounds, no overlap with status icons | P0-device |
| P07 | 320dp screen @200% text | No clipped title/actions; adaptive child budget | P0-device |
| P08 | 430dp screen + RTL | Correct bounds, ordering, text, pager gestures | P0-device |
| P09 | Full screen game/video + user preference minimize | No persistent visual blockage; system gestures work | P1-device |
| P10 | Android 12+ touch security/protected application | No untrusted-touch workaround; safe fallback when taps blocked | P0-device |
| P11 | Battery saver/background process kill | Gracefully restore after permitted restart, no illegal foreground launch | P1-device |
| P12 | Android notification group summary and children | Correct eligible count, no duplicate visible messages | P0 |
| P13 | Notification from alternate Android user/profile | No key collision or private-data crossover | P1-device |
| P14 | Paired wearable/media device connects and disconnects | Accurate hardware HUD only if permission and data available | P1-device |
| P15 | Reduced-motion setting or 0x animator scale | Content remains functional; no inaccessible motion-dependent state | P0-device |
| P16 | Screen recording, privacy mode enabled | Sensitive mini child text redacted; icons/counters remain useful | P0-device |
| P17 | Source notification's PendingIntent canceled before click | No crash, no false success, queue unchanged as appropriate | P0-device |
| P18 | 50 notifications, pager middle, process rotation | Stable selection restores by key when lifecycle permits | P1-device |
| P19 | Child touch on split-window transparent gap | Underlying app gesture passes through, child's actions still respond | P0-device |
| P20 | Stress 100 rapid post/update/remove events | Final reducer snapshot deterministic; all timers canceled for removed keys | P0 |

## Evidence required for each test

- Stable ID / test name and source file.
- Precondition, actual events, exact expected and actual state.
- Automated JUnit/Compose assertion or reproducible on-device gesture with device/build/version.
- Screenshots for visual layouts and short frame recording or timestamps for motion (redacted fixtures only).
- Clear PASS / FAIL / BLOCKED / NOT RUN; claims about APIs must not be confused with device proof.
- Must assert **Android shade notification intact** where test expects Island-only behavior.

## Device matrix minimum

Realme 8 Android 13 (from earlier reports, claimed but not independently verified), plus at least one Android 12+ alternate OEM device and a different cutout/screen geometry; CI emulators across 320/393/430dp, 1x/2x font, RTL, 0.5x/1x/2x animation scales. Device-specific tests are BLOCKED, not PASSED, if hardware unavailable.
