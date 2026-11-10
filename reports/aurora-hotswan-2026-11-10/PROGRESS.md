# All-state HotSwan correction — active, not accepted

The complete objective remains all60 states, proper sizing in the actual app and phone, matching Figma where a specimen exists and deliberately inferred consistent designs elsewhere. `state-review.json` enumerates all60; none is visually accepted yet.

## Authoritative findings and changes

- The user-provided Home preview shows Voice Memo content packed against the top of an86dp capsule. Its unbounded `fillMaxSize` content only measured the small control row. Both recording recipes now own16dp top/bottom padding,54dp stop controls,24dp stop squares and18sp Regular reference text. Their rows wrap content rather than inherit a preview's arbitrary height. Voice waveform has the measured123dp ink region and no timer offset hack.
- HotSwan was installed but its licensing service was absent. Installed the official JetBrains Marketplace Licensing plugin, restarted Studio, and the user activated the trial. The local control API then became available.
- The installed app crashed at startup: `Composer.shouldExecute(boolean,int)` was missing. Confirmed the method is absent in cached Compose1.7.1 and present in1.9.2/1.10.0. Aligned the project on official Compose BOM2025.10.00 (runtime/ui/foundation1.9.3 and Material3 1.4.0). One baseline APK build succeeded and was installed. This baseline rebuild is required by the library/runtime change; UI iterations are to use HotSwan thereafter.
- New baseline launches its process and writes HotSwan runtime port8601. HotSwan WATCHING and runtime-health available=true, caughtCount=0, degraded=false verified. USB disconnected during the work; Wi-Fi is confirmed as the SAME physical device by `ro.serialno=X4Y9QSOZ4DEADEDM`.
- Actual visual review is currently blocked by the phone's lock screen. User was asked to unlock normally; no PIN/pattern requested. Black captures are explicitly unavailable, not accepted screenshots. No bypass or security-setting change.
- Home catalog currently references46/60 raw enum states;14 states are absent from Home. Quick Controls incorrectly uses the diagnostic red Minimal when collapsed. Quick Controls is now mapped to Idle and the pager description says vertical; the hot reload is in progress and visual verification is pending. The14 absent Home states still need coverage/review.

## Remaining work

1. Obtain an unlocked phone; inspect the new recording recipes in the actual Home preview and debug gallery.
2. Apply and verify a real HotSwan UI update with unchanged APK install and healthy runtime. `tools/aurora-hotswan.py` uses the installed IDE bridge; it has no build/install command.
3. Work through EVERY pending item in `state-review.json`, including all compact, context, expanded, split and inferred cards. Current old contact sheets expose multiple top-packed contextual rows, excessive fixed-height filler and truncated derived content. Do not reinterpret prior bounds tests or screenshot counts as visual acceptance.
4. Use actual Figma PNGs as well as layer metadata: hidden layers can appear in metadata but not the rendered specimen (for example Find My's hidden distance fields).
5. Check native360dp and narrow/wide/font/RTL conditions, capability gates, touch targets and actual app preview placement. Capture new proof after fixes.
6. Run boundary regression and final JVM/device checks in a batch; unit tests require `-Photswan.dispatchRewriteEnabled=false`. The new HotSwanComposeBoundaryTest guards the observed startup defect and has not run yet.

Official setup references: https://hotswan.dev/docs/troubleshooting and https://hotswan.dev/docs/compatibility. The official Google Maven BOM is retained alongside this report. Credentials/license material do not belong in this report or repository.

Previous5285 captures and503 passing JVM tests belong to earlier code and are not acceptance evidence for this goal. Current HotSwan reload engine reports RELOADING; keep polling that engine rather than restarting on an HTTP observation timeout.

## Latest connection and reload diagnosis

The activated trial is recognized by the IDE bridge. The structural reload log reports a terminal Gradle timeout at120 seconds. The installed HotSwan settings API has no exposed Gradle timeout field; vendor binaries were not modified. The subsequent device heartbeat failed and the Wi-Fi transport stopped answering even simple shell commands. Reconnection failed. ARTEMIS self-diagnosis safely restarted ADB and confirmed no active authorized device and no installed AVD. The same Realme USB serial then appeared offline. The user has been asked to reconnect USB and unlock normally. Current bridge status is ERROR/connected=false, not a successful live reload.

The targeted HotSwanComposeBoundaryTest was started with dispatch rewriting disabled; its result is pending. No new device visual acceptance or screenshot coverage is claimed.

## Device recovery and first confirmed HotSwan delivery

USB returned authorized and the device was unlocked. The focused boundary regression passed (BUILD SUCCESSFUL in1m59s). All60 fresh real-device gallery screenshots were captured with live semantic state verification, and all four contact sheets were inspected. See ../device-gallery-2026-11-10/native-393-100-ltr/index.html. These are baseline captures, not final acceptance, Home preview or production-overlay proof.

The gallery itself used a black160dp canvas merging into every black capsule, and an unbounded segmented toolbar consumed the state list. Changed its stage background to the theme background and bounded the toolbar target. Confirmed IDE-licensed HotSwan delivery: success=true, health=applied, reloadedCount=3; composition settled with0 caught exceptions, process PID12712 unchanged and package lastUpdateTime01:05:46 unchanged. A direct Gradle reload attempt was refused because licensing belongs to the IDE; no authorization bypass was attempted. Retried through the licensed bridge after task outputs were warm and delivery succeeded.

Prepared intrinsic padding and native geometry/text corrections for AirPods, Satellite and FindMy based on the baseline plus visible reference metadata. These are awaiting subsequent HotSwan delivery and visual verification. All60 final visual acceptance entries remain pending.

Second successful HotSwan delivery applied SystemAlertsIsland (2 classes), settled726ms with0 exceptions. AirPodsConnected-live.png, SatelliteConnected-live.png and FindMyAlert-live.png were captured and inspected: vertical balance is improved. AirPods measured86.4dp; precision/reference and large-text review remain pending. These captures preceded the follow-up token extraction. That AppTheme/SystemAlerts reload hit the120s extraction deadline; queued Gallery fixture cleanup is currently reloading. The latest Gallery source removes generic Sample activity/Demonstration overrides so recipe defaults can be compared meaningfully. No final state acceptance is implied.

## Complete core-profile review and follow-up corrections

Both `review-393-100-ltr` and `review-320-200-ltr` contain all60 real-device full screenshots, settled geometry-v2 semantics, observed surface crops, original Figma side-by-side HTML and four contact sheets. All eight sheets were inspected. These are synthetic fixtures using the shared renderer, not native overlay or final acceptance. Earlier `after-*` bounds were sampled before settling; their full PNGs remain valid but their derived comparison HTML was replaced with a warning. The capture tool now retains two equal geometry observations and the crop tool ignores zero-area back-stack surfaces and includes split companions.

The large-text pass contradicted a completion claim: image message text was squeezed beside a fixed thumbnail, timer digits wrapped, expanded caller names were ellipsized, compact volume text clipped, and the sports status crowded the score. Follow-up source changes stack picture messages and timers, adapt the notification rail/count, preserve paused remaining time, wrap expanded caller identities, stack incoming-call actions when necessary, simplify large-text compact status copy and measure sports score width. AirPods' battery ring grows with type and Focus titles wrap. These require new visual verification.

The video-route icon was the audio-routing glyph. The REST export request returned429; extracted only the monitor/triangle subpaths from the cached original `Dynamic Island-16.svg` instead (shape3, first two subpaths,17.102×13.879 ink). The icon is rendered through the shared vector renderer, without adding a new enum or instance field. Its source and bounds are retained in `design/figma-exact/VideoRoute-source.json`.

Adding a getter directly to resident `IslandColors` caused a HotSwan NoSuchMethodError. Removed that member and used a top-level extension instead. The plugin relaunched the app; post-correction health reports0 caught errors. No new APK was installed. The partial `IslandOverlayService` delivery remains a required final reinstall/overlay verification step.

Current follow-up all60 capture: `refined-320-200-ltr` (running; verify the live process and output before treating it complete). Full final-profile/source/Home/overlay verification and final JVM checks remain open.

## Active activity selection and latest batch

The USB phone is unlocked and healthy. The latest AirPods large-type stacking, expanded music wrapping, adaptive Quick Controls columns/48dp utility targets and video-region gap changes were delivered through the licensed IDE bridge (8 classes, composition settled703ms,0 caught throwables). Actual Home music controls are fully visible after scrolling the root page; the initial below-fold view did not establish an AppStage clipping defect, so AppStage was not changed.

A further capture-tool defect was discovered: the bridge returns retained activities with positive bounds, so zero-area filtering alone is insufficient. Both tools now select the Compose root containing the newest gallery header. Older full PNGs remain real captures, but their old crops and scroll-target evidence must not be accepted. The refined320200 crop sheet was regenerated with root isolation. The interrupted latest360100 pass is superseded by active-360-100-ltr, which uses isolated active-root locators and outer-content scroll endpoints.

Current active360100LTR and active320200LTR each have60 unique full-device captures and all4 contact sheets inspected using active-root crops. Native pass has2 bottom endpoints; large pass has20. Wide430100RTL capture is running. Large-type defects still visible in PowerStatus, Sports scoreboard, Video metadata, TimerImage and Transport endpoint labels; these are not accepted. Full JVM suite is running with dispatch rewrite disabled.

Completed three active-root all60 capture profiles:360100LTR,320200LTR,430100RTL. All12contact sheets inspected. Added2call variants (group caller and capability-limited audio controls) and22scroll endpoints across profiles. Root review index and coverage.json link evidence. Full JVM suite passed504tests,0failures/errors/skips in11m9s. No APK install since01:05:46. Remaining large-type and RTL defects are recorded in state-review.json/workstate; screenshots do not establish goal completion.

Adaptive batch authored after the three-profile review: large PowerStatus metric stack; Sports score above team columns at200%; TimerImage/Transport use adaptive header; Transport endpoints separated; Video metadata below thumbnail atlarge and physical timeline LTR; VoiceMemo uses explicit gap, leading-aware waveform and LTR clock. Prewarm compile/extract/shims passed2m23s. First official IDE delivery hit120s Gradle limit; retry active. A read-only jcmd stack located the delay in HotReloadTask.staleDeliveredAppOwnWithSurvivorBytes reading existing interp classes from the external drive. Parallel cache warming is reading unchanged class bytes; no plugin/license changes. Actual light Home VoiceMemo screenshot/XML are saved, before this batch delivery.

Third official delivery succeeded:17classes sent,16applied, render settled985ms with0caughtthrowables. No local-cache symlink/move was performed; prepared copy remains unused. Targeted6state320200 capture is now running with --states. First ChargingExpanded screenshot confirms Connected remains intact and85% occupies its own row.

Fresh adaptive320200 captures verify complete words in battery/video/timer, separate transport endpoints, and sports scoreabove teams. Target capture now records overlapping70%-viewport steps until end, retaining active-root semantics and30%overlap. Sports scroll01 confirms full RMA/MCI glyphs but reveals unequal badge vertical positions from CenterVertically team columns; changed scoreboard row toTop and kept normal-size inline score centered. That single-file HotSwan update is pending verification. RTL VoiceMemo spacing/waveform leading and Video physical timeline verified in fresh430100 screenshots.

## Navigation and shared-corner correction

The legacy ColorOptions enum's null-source palette was inconsistent with Home Navigation and actual Figma19:1034. Restored native lane guidance with original glyphs, native type and48dp targets. Source-backed navigation now wraps supplied fields without global canvas scaling or invented missing roads/lanes. The393100 capture and equal-design-scale comparison confirmed the structural fix and revealed two further defects: cropped shield artwork and circular shell corners. The source shield is now a lossless actualFigma2x crop with provenance. Shared continuous corners follow the MIT figma-squircle geometry, clamped to half-edge budgets; per-recipe smoothing is0 for notifications,0.6 for calls/alerts,1 for music/navigation, and0.61 for several contextual specimens. Metadata is authoritative for these distinctions.

The new shared shape was delivered through HotSwan, but Android subsequently recorded an input-dispatch ANR at17:20:53 forPID19025. The process restarted as16790 and earlier in-memory patches were lost. No Java exception was reported by HotSwan. The continuous393100 capture was aborted and markedINVALID; its frames show the installed old baseline and are not acceptance evidence. New path interpretation is suspected; no readable main-thread trace proves that cause. Shape creation is now remembered by its geometry keys.

One consolidated installed baseline is required to compile new drawing classes, persist previous patches and include the corrected PNG plus previously partial service changes. The first preliminary assembly succeeded but was not installed; the per-recipe smoothing audit found differences before installation. The full JVM suite is running after that correction, then the final baseline will be assembled/installed once. Capture tooling now guards PID changes and verifies installedAPK SHA256; new reports crop reference effect margins and preserve design-equivalent widths.

Before the consolidated build, Home received explicit demo music/caller data and a bounded stage viewport with ReducedMotion support. Debug Gallery adds clearly synthetic source-navigation and long-source-navigation modes for checking the live-data rendering branch. No production preferences are written.
