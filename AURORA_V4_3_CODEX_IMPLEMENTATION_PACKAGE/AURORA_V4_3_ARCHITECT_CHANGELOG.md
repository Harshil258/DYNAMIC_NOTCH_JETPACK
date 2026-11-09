# Aurora V4.3 — Architect Corrections and Coding Handoff

## Source

The user's `AURORA_DYNAMIC_ISLAND_COMPOSE_RULESET_V4(2).md` is V4.2 (786 lines). It has been preserved unchanged. The corrected V4.3 document is a new file; all source-referenced geometry and the existing 64-scenario matrix remain in it.

## Confirmed defects corrected in V4.3

| # | Why V4.2 would risk an incorrect implementation | Correction made |
|---|---|---|
| 1 | Universal `screenWidth/430` scale makes a 367u atlas surface shrink to about 335dp on its own 393dp design canvas | Separate 393/430 per-family baseline in §28.1; restore correct widths before content/inset adaptation |
| 2 | No explicit size and content-gravity recipe for every one of 60 enum states (many called generically `alert family`) | Added full 60-row size/radius/slot contract in §29; separate CSV for Codex |
| 3 | `IslandPresentationState` is described as if already exposed by the engine | Clarified real `StateFlow<IslandBehaviorState>` in state manager; projection optional only |
| 4 | UX-C07 notes incorrectly say only source removal empties the queue | Both last eligible Island-only dismissal and source removal return to Idle; OS shade and tombstone remain on Island-only removal |
| 5 | During incoming call `hide/pause` of all child notices conflicts with expired children not resurrecting | Suspend visual rendering while preserving absolute monotonic deadline; restore only still valid children |
| 6 | `FontFeatureSettings("tnum")` isn't the normal Compose text style API | Fixed to `TextStyle(fontFeatureSettings = "tnum")` |
| 7 | Labeling 95/96dp radius token as roughly half-height without caveat | Added explicit family exceptions; no universal radius formula |
| 8 | V4.2 treats all T4 evidence as nonexistent, although earlier test reports include limited device instrumentation | Clarified limited Task 05/06 14-test evidence versus unverified per-state coverage; T5 production accessibility-overlay remains blocked |
| 9 | Runtime `IslandDemoState.Minimal` uses old 36.67dp/split-pill behavior while owner's Minimal SVG is a 108×32 red capsule with no text | Corrected implementation target and explicitly separated owner reference, runtime mapping, black Idle, and 38×38 glyph token |
| 10 | Physical CTA height 43dp and target 48dp accessible hit area could lead to giant invisible touch surfaces | Separate visible shape from effective target, bounded by real overlay window |
| 11 | Stale V4.1 version labels and instruction to edit only Markdown would misdirect Codex | Updated normative version and distinguished the ruleset from its authorized coding brief |
| 12 | Music compact/notification and `NotificationStacked` rows could imply a vertical child stack under Compact | Restated compact preview/passive behavior; child stacks only when an eligible expanded main is occupied |
| 13 | Trial behavior timings and visual spring values were described as one source | Behavior duration lives in §12.1; visual spring/fades live in §19.1, neither is measured from SVGs |

## Quantitative consistency checks

- 60 unique real `IslandDemoState` rows preserved; 60 matching geometry/slot targets provided.
- 64 unique numbered scenarios (§22) preserved, no missing scenario numbers.
- Six persona sections preserved.
- All owner UX-C01–C15 behaviors + architect-selected independent child timer PD-001 preserved.
- Minimal: exact owner reference `108×32`, R15.9018, `#DC0917` at 60%, no text/icon.
- Source-derived atlas: `367×86 R43`, music `367×177 R42`, call `367×166 R42`, navigation `367×185.36 R42`, CTA `335×43 R21.5`.
- Current code branch: `codex/task-05-layered-mini-islands`; Task 06 base commit `c5e98d76f118bdb16211e36ab85bf44bd5c28505`.
- Actual coding work has **not** been run in this review. Source code remains unchanged; the strict Codex task specifies implementation plus proof.

## Deliverables to attach to Codex together

1. `AURORA_DYNAMIC_ISLAND_COMPOSE_RULESET_V4_3_ARCHITECT_REVIEWED.md` — full design/behavior authority.
2. `AURORA_60_STATE_GEOMETRY_TARGETS.csv` — exact per-enum geometry and slot mapping.
3. `CODEX_MASTER_TASK_AURORA_UI_V4_3.md` — implementation instructions and mandatory QA.

Codex should commit only to a private review branch and provide a 60-state visual atlas plus 64-scenario verification matrix. Production accessibility-overlay verification is still release-blocking until legitimately granted and observed on a device.
