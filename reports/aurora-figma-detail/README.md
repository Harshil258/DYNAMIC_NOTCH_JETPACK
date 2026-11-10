# Device / Figma detail audit

This audit supersedes the claim that all60 Android states are pixel-identical to62 Figma specimens. The former gallery reused historical APK captures and paired unrelated nodes (for example, airplane-mode with sports/flight and mobile-data with subscription pricing). Its60 entries are now classified rather than accepted.

The original atlas metadata and62 exports remain under `../aurora-figma/`. The file has three pages: Design System, State Atlas, Device Screens & Motion. This audit's direct recipe comparisons use the supplied State Atlas page5:2; absence of a verified mapping is not proof no design exists elsewhere.

## Implemented corrections

-37 semantic glyphs use original outlined Figma paths with source provenance, native dimensions and no extra optical inset.
- Bundled official Inter4.1 Text/Display static fonts and OFL, as requested; release rendering needs no network font download.
- Notification cards use a vertical pager, a vertical3-dot rail, source-scoped reply drafts and source identity selection. Local Android resource/content image URIs now decode correctly.
- Reference-specific call, music, timer and notification typography, button discs, spacing and family heights replace generalized assumptions. Real source capability gates remain authoritative.
- Unsupported collapsed live-activity templates use neutral Idle rather than the red diagnostic owner specimen, guarded by a JVM regression.

## Reading the visual evidence

Open `all60.html` for all60 responsive device states. Open `comparisons.html` for same-content physical-device renderer captures and amplified pixel differences. `pixel-metrics.json` records geometry deltas, RGB error and foreground overlap (union of pixels with a channel above40/255, excluding the dark preview background). Neither low average error nor a matching outer rectangle certifies visual parity. Android crops retain native size; only original Figma exports are normalized to2pixels/design unit. Figma export alpha is composited on black. Synthetic reference identities and portraits live exclusively in debug resources.

Known intentional/residual differences include Inter versus SF Pro; source-supported audio controls versus the5-control reference demo; source-backed playback progress versus inconsistent static Figma progress; a1dp image-message width difference; fractional-dp raster rounding; incoming caller-image crop differences; and reference shadow versus the required shadow-free native overlay. Call subtitles differ for the ordinary mobile-call adaptation. All metrics remain REVIEW_REQUIRED rather than falsely reporting exact matches.

These are shared production Compose renderer captures on the physical Realme phone. They do not certify the actual accessibility overlay window, real carrier calls, RemoteInput delivery, lockscreen/privacy behavior or TalkBack. Production accessibility was not bound during this audit. No release or public push is authorized by these results.


## Verification record

The full device regression rerun passed17/17 tests after updating the obsolete horizontal-swipe assertion to the requested vertical pager. It covers1200 responsive configurations (60states ×5widths ×4text scales), source capability gates, media acknowledgement, child-island behavior, privacy redaction, draft isolation and pager indicators. A final focused capture rerun verifies the notification8dp inset and the actual single incoming-call recipe. Final logs and capture hashes accompany this report. JVM regression suite:503tests,0failures.

An initial paging regression was reproduced: the displayed page advanced while selected source identity stayed unchanged. Nested child scrolling did not emit the parent pager drag interaction. Tracking nested user scroll resolves this and preserves source selection during updates, insertion/removal and reply ownership.

## Review findings

| Finding | Resolution / remaining status |
|---|---|
| Gallery claimed exact matches using unrelated nodes and stale capture files | Corrected60-state classification; removed automatic fresh verdict based only on file existence; separate hashed evidence |
| Visible pager page could differ from selected source | Fixed nested-scroll ownership; device regression verifies source identity, insertion/update/removal, LTR/RTL and reply ownership |
| Unsupported collapsed live activities exposed the diagnostic red owner | Neutral Idle fallback; dedicated JVM regression |
| Generic optical inset and uniform icon sizing changed the original glyphs |37 original outlined exports; native glyph sizes in changed reference recipes; remaining authored glyphs not certified |
| Compact notification contents used12dp instead of8dp inset | Family-specific8dp token; fresh physical-device comparison |
| Timer controls used24dp icons and insufficient top inset | Native15×19/19×19 glyphs,17dp inset and baseline alignment |
| Incoming call used24dp icons and invented avatar border | Native glyph sizes and border removal; final focused recapture |
| SF Pro missing | Owner-approved Inter substitute bundled with OFL; typography remains visibly different |
| Supposedly exact derived states have no verified matching specimen | Explicitly unaccepted; source mapping review required before asserting parity |
| Actual accessibility overlay and real system interactions | Not verified; shared-renderer evidence does not substitute for platform acceptance |
