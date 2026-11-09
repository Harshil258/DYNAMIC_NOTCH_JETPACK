# Actual Figma comparison and Android correction

Source: [Aurora page5:2](https://www.figma.com/design/dmQdDCYXcy910vWZLfOqqz/?node-id=5-2).

The official connector identified its connected account but rejected the design read at the Starter MCP quota. The supplied token successfully accessed this file through Figma REST and was saved in macOS Keychain (`codex.aurora.figma`). It is absent from source, exports, reports and logs. Reusable extractor: `tools/extract-aurora-figma.py`; it reads Keychain without printing the credential.

[All62original Figma renders](figma-islands.html), [original layer metadata](reference-node-5-2.json), [manifest](manifest.json), and [learned mandatory rules](../../docs/AURORA_FIGMA_VISUAL_RULES.md).

The old intrinsic icon/count rule directly explains the reported55dp notification. It has been superseded by the measured189×36 compact specimen27:1021, with safe-budget clamping and content separated across the capsule. Expanded notifications now have one app/sender/message identity block instead of duplicate icon/header rows. Notification and three-action audio-call height follows actual content. The call's three supported controls form a centered group; the five-action Figma call remains a separate comparison, with no fabricated Android video/share actions.

On-device manual checks after the main correction: compact189.09×36.65dp; a message without actions366.88×84.99dp; standard three-action call366.88×152.44dp. The Lab's matching360dp Reply/Mark-as-read fixture is336×127dp, compared with336×157dp in the user's before screenshot. These are synthetic fixtures, not carrier-call or production accessibility-overlay acceptance.

Before comparisons:

- [Compact](compact-before-comparison.png)
- [Expanded message](message-before-comparison.png)
- [Call](call-before-comparison.png)

Fresh baseline/200% RTL contact sheets cover all60 enum states. The review identified additional call-summary action wrapping and descriptive-alert ellipsis at200%; these receive responsive fixes before the final device rerun. Original PNG exports retain shadow effect bounds; comparison boards crop to measured surface bounds. Figma call19:1036 has367×166 surface bounds and399×198 effect bounds.

Validation results and final comparison links are appended after the final run. No claim of60 external integrations or universal pixel-perfect parity is made. Some reference content is iOS-specific; actual Android source artwork/capabilities remain authoritative. Production accessibility service currently has no bound/enabled services.

## Final results

- Main source compilation,502 JVM tests (zero failures/errors/skips), debug app and instrumentation APK assemblies pass. The updated app is installed on the USB Realme.
- Four Gallery tests pass:1200 configurations (60 states ×5 virtual widths ×4 text scales),120 baseline/200% RTL captures, draft isolation, capability gating and source-acknowledged playback.
-15distinct device checks pass across the combined suite and targeted layered rerun. The first combined run had14passes and one outdated redaction-copy expectation; the corrected layered class passes all7tests and explicitly asserts private sender/body absence. Logs are retained; no failed run was erased.
- [22current Figma/Android comparisons](comparisons.html), [requested three-state board](requested-states-side-by-side.png), [current60-state Gallery](../aurora-v43/gallery.html), [validation summary](validation-summary.json).
- Normal and200% RTL sheets were visually inspected. Call-summary actions now remain whole in two columns, and the four descriptive system alerts wrap. Dynamic source images and supported-action differences are explicitly labelled.

Performance is **not accepted as smooth**: the hardware ActivityScenario benchmark recorded2056frames,647janky (31.47%); gfxinfo p50/p90/p99=28/57/101ms. The frame-stat ring buffer contains118valid samples, with end-to-end p50/p90/p99=39.84/44.81/85.88ms. The benchmark test verifies completion/sample count, not a jank-quality threshold. See [raw benchmark](animation-benchmark.txt), [frame stats](animation-framestats.txt), [performance summary](performance-summary.json). Post-suite meminfo found no process, so no memory or delta is claimed. Debug Lab includes instrumentation and diagnostics; this is not a production-overlay benchmark.

Production TYPE_ACCESSIBILITY_OVERLAY, real carrier call/RemoteInput and TalkBack acceptance remain unverified because the app's accessibility service is unbound. Existing iOS-only reference demos are not represented as newly supported Android integrations. These corrections are a measured visual improvement, not a blanket pixel-perfect or release-readiness claim.
