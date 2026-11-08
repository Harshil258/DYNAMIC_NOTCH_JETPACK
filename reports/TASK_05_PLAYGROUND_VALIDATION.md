# Task 05 — Layered Mini-Islands Lab

Open the debug launcher **Island Lab (Debug)** and scroll the tab strip to **Mini-Islands**.

Sources:
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/DynamicIslandPlayground.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundModel.kt`
- `app/src/debug/java/ai/emots/kishan_dynamic/playground/PlaygroundRenderer.kt`

The lab calls the same `IslandBehaviorEngine`, immutable projection and `LayeredMiniIslands`/`MiniNotificationIsland` used by production. No visually separate fake stack exists. Debug actions never execute Android cancellations or external PendingIntents.

Controls: main + 1/2/3 arrivals; bursts of 10/25; incremental newest insertion; 50-message queue; per-child source removal, hold/release and expiry; tap promotion on the actual child; call interruption/restoration; redaction toggle. Queue, pending count, selected identity and bounded event trace are visible. The existing Geometry and Live Preview tabs supply narrow/wide devices, fonts, RTL and reduced motion. A layout-observation regression test ensures presentation metadata cannot replace selected music/call/demo fixtures. Burst progress reports actual completed arrivals, including partial cancellation. Physical overlay mode also uses this shared renderer and auto-stops after three minutes.

Independent child timers run in the sandbox, honouring cancellation and correlated restart effects. Closing the final lab owner cancels the mock timer registry. No production preference is written.

Direct ADB exploration was used with the owner's explicit fallback instruction. ARTEMIS was unavailable: its CLI doctor reported no multimodal credential. It cannot reuse this Codex session's login. This is not represented as an ARTEMIS run.
