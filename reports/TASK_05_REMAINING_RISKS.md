# Task 05 — Remaining risks and examination gate

Task 05 stops here for independent examination; no release is published.

- **Production accessibility-window route: NOT RUN / BLOCKED.** After APK reinstall Android reports `Bound services:{}` for the production accessibility service. The real native smoke test exercises the debug application-overlay adapter with the shared renderer; it does not certify the production accessibility window. No production-ready touch-safety claim is made.
- Single-window capture includes deliberate rectangular child hit rows and rounded-corner clearance. Per-pixel pass-through is not implemented or claimed. OEM touch routing requires physical evidence; see the touch-safety report.
- A Realme test device cannot validate every manufacturer's cutout, foldable, landscape or keyboard behavior. Logical size/font fixtures are simulations, not additional physical devices.
- **Real inline reply + IME, actual carrier calls, physical lock/unlock, device rotation and TalkBack: NOT RUN.** Domain/Compose fixtures cover their state/privacy constraints, not those external platform journeys.
- No 60/120fps, jank-free, battery or accessibility certification is inferred from unit tests or screenshots. Perfetto/frame metrics, TalkBack focus/timeouts and multi-OEM runs remain examination work unless explicitly evidenced.
- Mock call interruption, source removal, redaction and reply ownership are deterministic fixtures. They do not claim real WhatsApp/Telegram/phone-service end-to-end acceptance or remote reply delivery.
- Pending registry capacity is bounded by retained canonical identity (50); queue-capacity eviction retires the corresponding transient arrival. Expired/overflowed visual cards do not cancel Android notifications.
- Existing permanent project rules, monetization/entitlement behavior and other island families remain in scope for regression checks; no real purchase, ad, call or source-notification cancellation is part of the lab.
