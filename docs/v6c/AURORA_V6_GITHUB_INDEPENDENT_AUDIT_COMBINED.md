# AURORA V6 — Independent GitHub Examination + Adaptive-Layout Addendum

**Verdict: REWORK REQUIRED. Not approved for merge/release.**

Repository: `Harshil258/DYNAMIC_NOTCH_JETPACK`  
Examined review branch: `codex/aurora-v6-product-design-review`  
Exact commit: `6898bdcc1cf5a010ab5624b9d221eaf1e0e8d809`  
Base commit: `b613666c973973d43871f3b8ae9c14e8d7814bca`  
GitHub comparison: one review commit, 33 files, with Kotlin production changes, limited tests, reports and 9 screenshots (8 unique named state folders).

## Review confidence and limitations

Directly verified by fetching the actual GitHub source, report files, diff metadata and current-commit screenshot assets. No independent `./gradlew`, ADB, Jugg, live notification, production accessibility-service test or frame capture was run by this examiner. Claimed **524 JVM passing tests** and specific on-device dimensions are developer-reported, although corresponding committed screenshot images were inspected. The reviewer does **not** validate claims of 60fps, all 60 states passing or accessibility-overlay safety from the submitted logs alone.

## What is genuinely improved

1. `CallSummaryIsland.kt`: now produces a more coherent completed/missed/outgoing phase label, neutral frozen duration and fewer primary buttons; 112dp nominal minimum. Screenshot for `CallSummaryExpanded` shows reported ~366.8 × 112.2dp outer surface; **shell only**, not full layout acceptance.
2. `IslandPresentationMapper.kt`: expanded NAVIGATION maps away from ColorOptions into `TransportActivity`; `DynamicIslandPill.kt` detects a real NAVIGATION `liveActivityInfo` and invokes `NavigationIslandExpanded`. Real routing is improved.
3. `DynamicIslandPill.kt`: removes the synthetic fallback expanded-notification list when the canonical queue is empty.
4. `IslandFamilyGeometry.kt`: expanded width is capped at the nominal target instead of globally upscaling a 367dp Atlas shell at 430dp.
5. `PlaygroundGallery.kt`: missing-source modes receive a truthful compact specification card rather than the former giant black dummy.
6. Production source still retains its existing behavior engine and the review branch remains unmerged.

## Release-blocking findings

### P0-1 — 60/60 geometry PASS is contradicted by production code

A structured comparison of **all 60** `reports/aurora-v6/SIXTY_STATE_PROVENANCE_AND_VISUAL_AUDIT.csv` `TargetW/TargetH/TargetR` rows with actual `IslandFamilyGeometry.targets` revealed:

- **33 states** disagree by more than 0.5 design units.
- **25 states** disagree by **at least 5 design units**.
- Examples: `CallCompact` **code 152×36.67**, report **190×36.7**; `ActionControlExpanded` **code 367×210**, report **210×44**; `ChargingCompact` **code 132×36.67**, report **190×36.7**; `SportsExpanded` **code 367×144**, report **367×94**; `AirplaneAlert` **code 367×148**, report **367×86**; `ScreenMirroringAlert` **code 367×144**, report **367×86**; `MobileDataAlert` **code 367×162**, report **367×86**; `NotificationImage` **code 367×126**, report **367×86**.

A code target is not automatically correct just because the report is wrong: DIRECT references may supply the correct value, and derived states need content-driven measurements. Each inconsistency must be adjudicated against the actual original asset, user decisions and measured application screenshot. An audit CSV cannot be manually marked PASS at 60 rows without 60 real measured captures.

### P0-2 — Call Summary lock-screen privacy bypass risk

`IslandOverlayService.kt` redacts live caller contact through `LockScreenPrivacyPolicy.contact(...)`, but sends `(renderedState as? IslandState.CallSummary)?.record` directly to `DynamicIslandPill`, where `CallSummaryIsland.kt` renders the original `contactName` and `phoneNumber`. `LockScreenPrivacyPolicy` has no summary-specific CallRecord redactor. **Credible PII exposure risk** on locked/privacy-restricted presentation. Not a confirmed exploit, but release-blocking until source-level remediation and on-device verification.

### P0-3 — Fixed-height and SpaceBetween overuse creates hollow Islands

Evidence from `ReferenceActivityFamilies.kt` and submitted current-commit screenshots:

- `TransportReferenceContent`: `heightIn(min = 148.dp)` plus `Arrangement.SpaceBetween`. Three small groups are spread across a 148dp surface even if source data supports only two meaningful rows. The committed Transport screenshot shows large inter-row voids.
- `MovieReferenceContent`: `heightIn(min = 142.dp)` even when a small poster, title and CTA occupy a much smaller natural content envelope; static fallback film/ticket data masks missing source data.
- `SubscriptionReferenceContent`: `heightIn(min = 162.dp)` with static promotional copy and buttons regardless of actual offer/capability.
- `DynamicIslandPill.kt`: a combination of `measuredHeights` and nominal per-state targets can preserve empty real estate even after content is removed. `IslandFamilyGeometry.resolve()` always imposes `height >= target.height` on every state.

A reference-authentic 367×144 screenshot should remain available for direct reference comparison, but a **different content variant** with fewer rows must use its own truthful, measured dimensions; do not force empty sections simply to retain the largest nominal height. **Never shrink interactive content so far that text or targets clip**.

### P0-4 — Real-world data and action capability integrity

- `MovieReferenceContent(onOpenTicket: () -> Unit = {})` shows a clickable **View Ticket** button; `DynamicIslandPill` calls it without a real ticket handler. No-op action in a production-visible route.
- `SubscriptionReferenceContent(onUpgrade: () -> Unit = {}, onDismiss: () -> Unit = {})` displays active-looking Upgrade/Maybe Later controls; route calls defaults without true callbacks.
- `TransportReferenceContent(info = null)` hardcodes `Route 24 · Central`, `Market Street`, `Central Station`, `3 min` and synthetic progress. `DynamicNotchParent` maps NAVIGATION to TransportActivity yet does not pass `liveActivityInfo`; this route may reach synthetic transit content in runtime hosts.
- `NavigationIslandExpanded` defaults a missing/unrecognized maneuver to **Right** (`else -> NavigationRight`), potentially suggesting a wrong turn.
- `CallSummaryExpandedContent` treats a phone number containing digits as `canRedial` and `canRenderIslandControl` defaults to allowed when controls are unspecified. This is not sufficient evidence that an intent/call role is available.
- `DynamicNotchParent` swallows other `IslandControlAction`s with `else -> Unit`; the host should not render buttons wired to no-op callbacks.

Require real source actions with supported intents, otherwise remove the control or identify the fixture as noninteractive debug presentation.

### P0-5 — Full design implementation / test evidence insufficient

There are 60 demo enum states: **28 DIRECT, 10 FAMILY_ONLY, 22 NO_SOURCE** according to the current fixture registry. The commit provides only **9 newly committed screenshot files for 8 unique states**, and does not modify many specialized state renderers. It adds mostly enum/category counts, mapper logic and geometry constant tests, not composable layout, overflow, hit-box, privacy or action dispatch tests for all 60. Claiming all 32 derived states and all 60 visual gates PASSED is **unsubstantiated**.

### P1-6 — Two competing presentation mappers

`resolveOverlayDemoState(IslandState.Minimal)` returns `Idle`, but `DynamicNotchParent.mapIslandStateToDemoState(IslandState.Minimal)` returns `Minimal`. A stale comment says Minimal is a split pill, conflicting with the owner-confirmed **empty 108×32 red capsule at 60% opacity**. Reconcile both hosts without conflating production Idle camera shape with the owner Minimal specimen.

### P1-7 — Accessible targets, text, touch and state interactions unverified

`CallSummaryActionButton` draws `.height(36.dp)` and `.clickable`, with no documented test of a separate safe 48dp effective hit target for three adjacent controls. The current screenshot demonstrates nominal size but not 320dp + 2.0 font-scale and RTL quality. Need instrumented semantic bounds, no overlapping touch regions, and safe accessibility-overlay bounds.

### P1-8 — Jugg / device test evidence overclaims

Developer reports 524/524 JVM passing and Realme 8 captures, which may be accurate; independent Gradle was not executed. No committed V6 evidence proves bound `TYPE_ACCESSIBILITY_OVERLAY`, third-party notification end-to-end handling, lock-screen caller redaction, outside-overlay touches, service rebind or 60fps without dropped frames. Do not infer all that from debug gallery screenshots.

## Product-design correction: remove **unintentional** unused space, not all negative space

The user specifically rejects hollow, oversized Islands and inconsistent gaps/padding. The corrected policy must distinguish:

- **Direct Figma exact fixture**: match reference W/H/R/slot bounds at the **same viewport, font/content and settled animation**. Where its composition genuinely contains intentional breathing room, preserve it; do not shrink solely to maximize fill percentage.
- **Real changing content / source variants**: use intrinsic measured width and height within safe bounds. Optional actions, pictures, progress tracks, secondary labels, route strips and media controls must not reserve space when absent. Fewer rows = smaller Island or a different compact family; more valid content = naturally larger Island or clipped/scrollable safe view, never overlapping.
- **NO_SOURCE / FAMILY_ONLY derived states**: choose baseline dimensions from domain/use case, not a fictional static Apple measurement. Evaluate actual content envelope and artwork/controls. Use per-state trial min/max guards, justified by viewport, gesture surface and cognitive workload.
- **Camera/cutout**: must reserve safe true hardware cutout area; never shrink below physically required silhouette, and never create invisible oversized overlay touch rectangles.

## Decisions for the next task

Preserve genuine V6 improvements, then execute a **single combined V6C rework**: correctness/privacy + all 60 truthful geometry contracts + adaptive whitespace elimination + all 32 missing-family designs + custom notifications + live device visuals + actual action safety. No extra generic 60-card document rewrite. **Physical production accessibility service remains a release gate.**

## Source links

- [V6 commit](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/commit/6898bdcc1cf5a010ab5624b9d221eaf1e0e8d809)
- [Geometry targets](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandFamilyGeometry.kt)
- [Claimed 60-state ledger](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/reports/aurora-v6/SIXTY_STATE_PROVENANCE_AND_VISUAL_AUDIT.csv)
- [Transport, Movie and Subscription renderer](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/app/src/main/java/ai/emots/kishan_dynamic/ui/components/ReferenceActivityFamilies.kt)
- [Privacy policy](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/app/src/main/java/ai/emots/kishan_dynamic/data/model/LockScreenPrivacyPolicy.kt)
- [Production service](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/app/src/main/java/ai/emots/kishan_dynamic/service/IslandOverlayService.kt)
- [Call Summary renderer](https://github.com/Harshil258/DYNAMIC_NOTCH_JETPACK/blob/6898bdc/app/src/main/java/ai/emots/kishan_dynamic/ui/components/CallSummaryIsland.kt)
