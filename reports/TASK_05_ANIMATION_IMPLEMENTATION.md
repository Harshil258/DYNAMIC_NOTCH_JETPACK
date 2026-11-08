# Task 05 — Animation implementation

Shared source: `app/src/main/java/ai/emots/kishan_dynamic/ui/components/LayeredMiniIslands.kt`.

`MiniNotificationIsland` renders a small black, rounded surface with package artwork/fallback, app identity, sender/title, and up to two preview lines. Aurora spacing and typography tokens define its hierarchy. Main content remains larger and dominant. No glow, decorative gradients or shadow layers were introduced.

`LayeredMiniIslands` owns a keyed lazy stack, with `animateItem` placement from `AppMotion.islandSpring()`. Newest children insert at the top; unchanged cards retain identity. At most three outgoing rendered identities remain temporarily for merge animation; this is animation bookkeeping, not a notification queue.

Sequence: QUEUED → EMERGING → SETTLING → VISIBLE. Shape develops via top-anchored vertical scale and downward translation; copy opacity follows the icon/surface using `AppMotion.StaggerStep`. Exit reverses toward the main edge, then removes the visual identity. Touch uses TOUCH_HELD / RELEASED; tap uses PROMOTING followed by an identity-validated main selection after `AppMotion.DurationMedium`. This prevents the child and selected main from visibly showing the same item during promotion.

Reduced motion snaps geometry and removes translation/scale while preserving semantic state changes. All timings/springs use existing AppMotion primitives. No independent infinite animation or coroutine per queued notification was added. Timers remain bounded by pending capacity and are cancelled on removal, selection, collapse and owner teardown.

This is an Android-specific notification presentation inspired by Apple's craft. It is not claimed to be an official iOS general-notification stack.
