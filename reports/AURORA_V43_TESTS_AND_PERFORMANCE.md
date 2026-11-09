# V4.3 tests and performance — updated after Figma correction

Latest main-source compilation, `testDebugUnitTest`, debug APK and AndroidTest APK assembly pass. **502JVM tests,0failures/errors/skips**. [Build log](aurora-figma/final-build.log), [JUnit XML](aurora-v43/test-results/), [validation summary](aurora-figma/validation-summary.json).

Fresh physical Realme shared-renderer gallery: **1200synthetic configurations**,60states ×5virtual widths (320/360/393/430/640, last with360dp landscape height) ×4font scales (1/1.3/1.5/2;2usesRTL).120PNG captures and [measurements](aurora-v43/measurements.csv) replace the interim source's images. Font/width are fixture configuration overrides, not proof of actual OEM rotation, split-screen or system font settings.

15distinct device checks pass across the combined run and targeted rerun:4Gallery,1responsive-layout,7layered-island,2pager,1hardware animation-sample test. Initial combined run:14passed,1failed an outdated “New notification” copy assertion. The renderer shows “Unlock to view”; the updated test also checks that private sender/body are absent. Its full7-test class passes on rerun. [Initial log](aurora-figma/device-suite-initial.log), [rerun](aurora-figma/device-layered-rerun.log), [test build](aurora-figma/test-assertion-build.log).

Visual proof: [22actual Figma/Android comparisons](aurora-figma/comparisons.html), [all62Figma specimens](aurora-figma/figma-islands.html), [60state Android gallery](aurora-v43/gallery.html). See [learned rules](../docs/AURORA_FIGMA_VISUAL_RULES.md) for intentional Android adaptations and source identities/actions.

**Frame pacing still requires work.** Synthetic debug Lab hardware benchmark:2056frames,647janky (31.47%); gfxinfo p50/p90/p99=28/57/101ms.118valid ring-buffer samples: end-to-end p50/p90/p99=39.84/44.81/85.88ms. Passing the sample-count test does not mean smooth60fps. No baseline-relative regression claim or production-overlay extrapolation is made. [Raw evidence](aurora-figma/animation-benchmark.txt), [frame stats](aurora-figma/animation-framestats.txt), [summary](aurora-figma/performance-summary.json). Post-suite memory query found no process; no memory delta is claimed.

Production accessibility service remains unbound. Carrier calls, real source RemoteInput/media, native overlay touch classes, keyguard and TalkBack remain separate acceptance gates. The earlier960configuration capture and placeholder-assertion failure are historical, preserved in `aurora-v43/interim-device-run.log`.
