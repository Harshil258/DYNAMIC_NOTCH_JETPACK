# Task 05 — Overlay and touch safety

Production source: `app/src/main/java/ai/emots/kishan_dynamic/service/IslandOverlayService.kt`.

The existing Compose overlay window now contains the shared main-plus-children layout. No new production overlay windows or full-screen touch catchers were added. WindowManager keeps WRAP_CONTENT, existing measured cutout-aware placement, NOT_FOCUSABLE and NOT_TOUCH_MODAL. Existing hidden/suppressed windows keep zero dimensions, INVISIBLE and NOT_TOUCHABLE.

The main stays first and anchored; children add measured height below it. Child count is limited by screen height, font scale and width. Inline reply suppresses children so keyboard/editor work has priority. Calls suppress the stack immediately. The canonical queue remains accessible through the existing pager.

## Transparent pixels are not pass-through

Android dispatches touches against this window's rectangular bounds. Rounded corners and clearances inside that rectangle do not magically pass through. Each child therefore has a full main-width interaction row; the 8dp visual inset/gutter belongs to that child's touch target. The overflow row is a full-width, minimum-48dp action. This bounds extra capture to deliberate card targets rather than a screen-wide transparent layer. Outside the window, NOT_TOUCH_MODAL permits normal app interaction.

This design does **not** claim per-pixel pass-through at rounded corners or independent gap holes. If examination requires that stricter behavior, separate tightly bounded child windows (with additional lifecycle/placement complexity) must be evaluated. Hidden internal-insets APIs/reflection were not used to pretend otherwise.

## Runtime evidence

The Realme debug application-overlay smoke test verifies a bounded window and an injected underlying-app Stop click outside it. Production accessibility service binding was absent after reinstall, so production-window routing is NOT RUN / BLOCKED. See `TASK_05_TEST_AND_VISUAL_EVIDENCE.md` for actual device results. JVM/Compose rendering alone cannot establish production window routing, OEM camera compatibility, TalkBack behavior or frame pacing. Unverified cases remain explicit in `TASK_05_REMAINING_RISKS.md`.
