# Island fidelity: 18-point implementation and acceptance ledger

Date: 17 October 2026. Complements NOTIFICATION_ENGINEERING_AUDIT_2026-10-17.md;
that document records the earlier 201-test batch, not the current final count.
Status is deliberately split between implementation and runtime acceptance.

## Root cause evidence

Before changes, the real launcher source had importance 3, ongoing flag 0x2,
progress 0/max 0, no running chronometer. Presence of progress extras was treated
as progress, bypassing the background filter. All ongoing/non-clearable sources
were exempt from expiry. Reconnect replayed old shade notifications. Compact
content rendered the full title/app name. These combined into persistent launcher
text; it was not a font or horizontal-padding defect.

After install, privacy-safe IslandEngine logs recorded:

```
event=listener_connected snapshotSize=39
event=classify id=5c52d4e56c25 package=com.android.launcher category=general importance=3 ongoing=true clearable=false summary=false kind=IGNORED reason=background_without_activity restored=true
event=queued id=9773265245b3 reason=kind=TRANSIENT size=1 presentation=EXPANDED
event=queued id=f3df285d1e24 reason=kind=TRANSIENT size=2 presentation=EXPANDED
event=expired id=9773265245b3 reason=presentation_deadline
event=expired id=f3df285d1e24 reason=presentation_deadline
event=overlay_context suppressed=true
```

The two queue sources were explicitly synthetic Shell fixtures, not messaging
accounts. Their original notifications remained in Android's shade after island
expiry. Launcher also remained in Android's shade: filtering did not cancel it.

## Every requested section

| # | Implementation / evidence | Acceptance still required |
|---|---|---|
| 1 | Actual launcher metadata isolated; capability filter, fixed deadlines, compact count/icon and neutral idle corrected | Recheck other OEM launchers |
| 2 | Apple official Live Activities/DynamicIsland guidance read; reference APK identity and source flow rechecked | Reference runtime parity not claimed |
| 3 | Derived IDLE/TRANSIENT/COMPACT_ONGOING/MINIMAL_MULTI_ACTIVITY/EXPANDED/DISMISSING phases; existing coordinator remains sole owner | Framework interruption sequences |
| 4 | Metadata eligibility, meaningful progress, silent/blocked filtering, completion and restored-source distinction | Real app metadata diversity |
| 5 | Hashed-ID, normalized-category decision/lifecycle diagnostics; actual device evidence above | Release log-volume review |
| 6 | Monotonic deadlines, replay guard, async source epochs, ranking changes and removal invalidation | Process death/rebind stress |
| 7 | Compact icon/count or progress; no persistent full notification label; reduced compact aura; idle no fake companion | All renderer screenshots, 200% fonts |
| 8 | Shared shell clamps and token-backed compact reserve; pure width tests across five viewports and 11 families | Actual cutouts/status icons, landscape/display scaling |
| 9 | Existing interruptible AppMotion/reduced-motion flow retained; duplicates no longer re-expand/reset deadline | Frame timing and rapid physical toggles |
| 10 | Exact source keys, bounded reducer, stable pager selection/drafts/measurement; two source IDs observed | Multiple physical swipes and deletion mid-swipe |
| 11 | Existing versioned PendingIntent dispatch, free-form RemoteInput gate, truthful failures and generic fallback | Real reply/read/media/canceled-intent execution |
| 12 | Public window metadata suppresses protected surfaces; hidden touch bounds zero and NOT_TOUCHABLE; final Realme shade capture has no overlay | Other OEMs, IME and permission-dialog cycles |
| 13 | All 20 scenarios accounted for below, with honest test levels | Pending real-source cases are not passes |
| 14 | Negative/stress ledger below; new policy regressions plus existing engineering fixtures | Framework failures and resource pressure |
| 15 | Small pure eligibility, replay, epoch, phase, geometry and surface policies; platform intents stay outside UI models | Large orchestration files intentionally not rewritten |
| 16 | Final 213 tests, incremental assembly/lint and diff check pass; no clean/dependency/configuration changes | Runtime gates remain separate |
| 17 | ISLAND_BEHAVIOR_RULES.md created; AGENTS/UI_UX_RULES linked | Future contributors must maintain contract |
| 18 | This ledger plus prior audit and Claude Mem retain evidence and remaining gates | Production acceptance remains open |

## Positive scenarios: all 20

“JVM” means deterministic synthetic policy/reducer fixtures, not real source-app
integration. Existing tests are reused where appropriate; not every row has a
new dedicated test. No real messages/payments/calls were sent.

| # | Scenario and required result | Evidence / boundary |
|---|---|---|
| 1 | One WhatsApp message: transient detail and actual available actions | JVM generic message; real WhatsApp pending |
| 2 | Same conversation updates: one key, no timer reset | JVM reducer/replay/deadline; real grouping pending |
| 3 | Different conversations: separate keys, selection retained | JVM identity/selection; real WhatsApp pending |
| 4 | WhatsApp + Instagram: no cross-app group merge | JVM cross-package regression; accounts pending |
| 5 | Gmail + Google Pay: separate copy; no invented financial success | JVM grouping/sensitivity; real apps pending |
| 6 | Music + message: restore surviving media, protect user controls | Existing ownership/media tests; live session pending |
| 7 | Incoming call during playback: call priority | Existing interruption tests; telephony pending |
| 8 | Call ends: restore media only if still active | Existing lifecycle/prioritizer tests; live call pending |
| 9 | Charger connects: brief genuine charging confirmation | Existing monitor inspected; physical cable test pending |
| 10 | Charger disconnects: remove charging state | Existing lifecycle inspected; physical cable test pending |
| 11 | Bluetooth connect/disconnect: genuine accessory lifecycle | Monitor inspected; real accessory pending |
| 12 | Download: actual progress, completion expires | JVM progress/completion and eligibility |
| 13 | Timer: ongoing capability; completion not permanent | JVM timer/classification; actual timer pending |
| 14 | Silent system source: no interruption | Actual System UI importance 1 ignored on restore; JVM fresh silent |
| 15 | System Launcher: no activity without capability | Actual Realme metadata and ignored decision |
| 16 | Shade expansion: no visible/touching overlay | Actual suppression event/zero touch bounds; OEM retained-buffer defect fixed and final shade screenshot visually verified |
| 17 | Lock/unlock: privacy and focus protected | Actual redaction observed; full unlock cycle pending |
| 18 | Background/foreground: source state independent of app | Actual MainActivity relaunch/listener restore smoke; process recovery pending |
| 19 | Multiple page swipes: stable identity and no index errors | JVM pager policy; two device queue sources, gesture E2E pending |
| 20 | Remove while expanded: neighbor selected, stale work discarded | JVM selection/epochs/reply invalidation; framework gesture pending |

## Negative and stress cases: every requested case

| Case | Evidence / remaining gate |
|---|---|
| Hundreds of callbacks | JVM 1,000-update reducer bound and 500-update deadline |
| Duplicate events | JVM replay/content hash including Aa/BB collision fixture |
| Rapid arrival/removal | JVM epoch invalidation, unique keys and final empty queue |
| Empty titles | Existing engineering fixture; actual layout pending |
| Missing icons | Existing renderer fallback inspected; device missing-icon fixture pending |
| Long text | JVM 10,000-character source fixture; pixel clipping pending |
| Emoji only | JVM content fixture; rendered accessibility pending |
| Different languages | JVM Hindi/Unicode fixtures; translated UI acceptance pending |
| RTL | JVM Arabic preserved; actual bidi layout pending |
| Expiry and replacement | Fixed deadline + source epoch tests; scheduler race instrumentation pending |
| Permission revoked expanded | Existing clear/reply policies; actual revocation pending |
| Service destroyed animating | Scope cancellation/disposal inspected; framework test pending |
| Overlay removed twice | Idempotent nullable handle cleanup; framework instrumentation pending |
| WindowManager failure | Guarded platform boundary; injected framework exception pending |
| Rapid expand/collapse | Ownership guards inspected; real gesture stress pending |
| Multiple horizontal swipes | JVM settled-key selection; Compose gesture pending |
| Deleted during swipe | JVM stale-key rejection; physical swipe pending |
| Display size | Pure shell width clamps; device display changes pending |
| Font scaling | Essential action architecture retained; actual 200% layout pending |
| Low memory | Bounded queue/replay and deferred artwork intake; process/resource pressure pending |
| Process restart | Actual APK replacement restores listener without replaying old transients; kill/recovery pending |
| Old source resurfacing | JVM restore/replay; actual 39-source snapshot skipped old transients |
| Stale selection | JVM removed-key and neighbor-selection regressions |
| Ongoing ends unexpectedly | Existing lifecycle removal + new source invalidation; actual live source pending |

## Sources and architectural rationale

- [Apple Live Activities](https://developer.apple.com/design/human-interface-guidelines/live-activities): glanceable bounded tasks, concise compact content, essential updates, supported interactions.
- [Apple DynamicIsland](https://developer.apple.com/documentation/widgetkit/dynamicisland): compact/minimal/expanded presentations. These inspire hierarchy, not hardcoded Android geometry.
- Android accessibility window metadata is a supported but imperfect OEM context signal. No private shade identifiers or notification text is inspected for context detection.

## Validation and limitations

Final batched `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug` passed
after the native-view visibility fix: 213 tests, zero failures/errors/skips;
lint completed with 82 warnings. `git diff --check` passed. Final APK installed
successfully, and `/tmp/island-final-shade.png` shows no island over Quick
Settings. Native window inspection also confirmed zero bounds and NOT_TOUCHABLE
while suppressed. All synthetic Shell notification keys were removed by targeted
swipes; no Clear All or other-source cancellation was used.
No claim of universal pixel perfection, jank-free
motion, completed external-app actions or production readiness is made.

The device can display the app while secure keyguard metadata remains active;
the user confirmed it was unlocked. Tests continued through the visible app,
without treating that flag as a blocker or disabling privacy. A later capture
actually showed the lock screen. Authentication is never bypassed. Full private
pager/action acceptance therefore remains distinct from startup smoke checks.
