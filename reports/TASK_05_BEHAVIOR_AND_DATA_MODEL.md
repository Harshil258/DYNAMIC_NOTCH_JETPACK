# Task 05 — Behavior and data model

Base: `3e47d3f9686473b2c17b3e038d3d6c8f61e8f5ce`. Branch: `codex/task-05-layered-mini-islands`.

## Canonical ownership

- `app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt` remains the only behavior reducer. `NotificationReducer` retains its 50-item canonical queue and newest-first horizontal pager.
- `IslandStateManager.behaviorPresentation` exposes the immutable engine snapshot as read-only `StateFlow`. The production overlay collects it. Debug `PlaygroundSnapshot.behavior` publishes the same reducer's state atomically with mock presentation updates; the renderer never polls a mutable field.
- `ChildNotice` is a bounded transient pending-arrival registry, independent of both queue history and rendered cards. Default capacity is 50, and pending identities must survive in the canonical queue. Canonical queue eviction also removes pending entries. No old queued message is reclassified as a new arrival.
- Each child has a monotonic deadline, timer-correlation token, and separate arrival identity. New arrivals do not restart older timers. A child receives its visible readability budget once on first settling; never-rendered overflow expires against its arrival TTL rather than living indefinitely. Same-key copy updates preserve deadline and order. Hold cancels only that child's timer; release resumes its remaining duration plus the master specification’s 2s release grace with a fresh correlation token. Stale callbacks cannot expire reused keys.
- Main selection stays pinned under ordinary arrivals. Promotion selects the existing queue identity, removes its child presentation, and keeps the previous main in the pager. Delayed promotion validates arrival identity, so removal and key reuse cannot promote a replacement source.
- Source removal/reconciliation prune the registry. Collapse and calls merge/hide children without cancelling Android sources. Calls restore from the live queue rather than an obsolete suspended queue; expired children never return.
- `childLifecycles` publishes bounded presentation phases; `childPrivacy` publishes lock/redaction policy. Lifecycle entries are capped at 53 (50 pending plus outgoing presentation bookkeeping), not an unbounded event history. The sandbox event trace retains 80 entries.

## Projection

`app/src/main/java/ai/emots/kishan_dynamic/ui/components/LayeredMiniIslands.kt` projects pending arrivals into a visible subset. Available height and font scale bound the count. Widths below 393dp show at most two; larger logical screens at most three. Insufficient height suppresses child cards while the canonical pager remains available. `+N` counts only additional pending arrivals and offers the next hidden child.

Privacy projection uses `LockScreenPrivacyPolicy`; lock transitions additionally conceal outgoing child copy. No notification body, sender, OTP, or action payload is logged.

## Explicit migration of obsolete assertions

Task 04B's functional selection, queue, timer and pager tests remain. Two older test assertions assuming the superseded four-pending-child cap were updated to expect all fresh pending arrivals. This is required by Task 05's overflow model; it is not removal of test coverage.
