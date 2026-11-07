# GEMINI TASK 03 — Implement the Canonical Dynamic Island Behavior Engine (Foundation)

**Role:** Senior Android / Jetpack Compose Engineer executing a product specification authored by ChatGPT, the Principal Product Architect and Final Examiner.  
**Type:** Production implementation + debug playground integration + deterministic tests.  
**Task boundary:** FOUNDATION ONLY. Do not attempt a one-shot UI rewrite, multi-window overlay overhaul, or animated mini-island rendering in this task.

## Inputs you must read first

1. `DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md` — product behavior is the **design authority** for this task. Owner-approved UX-C01–UX-C15 must not be overridden by earlier assumptions in audit reports. C11 is the owner's layered-island direction, and the product architect has chosen the detailed defaults.
2. `DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md` — high-risk cross-direction test cases and acceptance criteria.
3. Your completed `reports/TASK_02B_EXAMINER_CORRECTIONS.md` and `reports/UX_C11_LAYERED_NOTIFICATION_ISLAND_RESEARCH.md` (or the exact equivalent report paths). The owner says both investigations are complete. Read the real contents from the repository; do not invent missing reports. Reconcile important disagreements with the above approved product specification, documenting them rather than silently replacing owner intent.
4. The actual existing Android repository, particularly `IslandStateManager`, `NotificationReducer`, `IslandInteractionPolicy`, `IslandPresentationMapper`, `DynamicIslandPill`, `IslandOverlayService`, `IslandNotificationListener`, `PlaygroundController` / debug workbench, relevant tests, and build variants.

**If the two new master files have not been added to the project, ask for their contents or report the missing file in your submission. Do not pretend you read them.**

## Product mandate

We are not merely copying a visual notch. We are building a professional Android interaction product for multiple human behavior types: fast glance, deep reading, heavy-notification use, multitasking, one-handed access, privacy/accessibility.

Owner behavior that must survive all future implementations:
- 0 notifications/activities + idle compact single tap -> Quick Controls (UX-C01).
- Exactly 1 eligible notification + compact single tap -> expand notification details in island (UX-C02).
- Multiple notifications (e.g. 12) + compact single tap -> horizontal pager; newest first (UX-C03). Do not implement a fake pager in this task; model intended selection/transition if full UI is future phase.
- While user is reading a pager page, ordinary new notification never steals selected key; queue count and subtle new indication update (UX-C04).
- Source removes selected -> next valid key, else previous; user-expanded remains open (UX-C05).
- × default Island-only dismissal with configurable optional OS notification dismissal; never cancel source notification by default (UX-C06).
- Last item removed and no activity -> idle compact; tap opens Quick Controls (UX-C07).
- Island-dismissed same-key minor updates remain suppressed; meaningful new content and delivery milestones can resurface (UX-C08/C09).
- Compact with many messages + important previously dismissed delivery milestone -> transient preview, return compact, updated item retained (UX-C10).
- Future visual target: layered emerging mini-islands for new notifications while expanded, adaptive 1–3, newest-first, child tap promotes to main, independent smart timeout, visually merge without removing queue. In this task model policy events only, DO NOT claim animated rendering exists.

## Step 1 — Evidence-based source baseline

- Capture `git status`, branch name, Gradle variants and relevant unit/instrumentation test inventory.
- Read and map existing production normalization, reducer, state manager, interaction policy, window placement, and playground seams. Note exact file/function names as found in source (do not rely on old report line numbers).
- Identify current bugs/violations of owner UX rules with explicit reproduction or test evidence. Do not label defects confirmed from source-only speculation.
- Decide the minimal safe migration plan; preserve the existing state hierarchy unless necessary; avoid parallel queue/state machines.

Create `reports/TASK_03_BASELINE_AND_INTEGRATION_PLAN.md` BEFORE modifying code.

## Step 2 — Implement one canonical pure behavioral decision layer

Implement a testable pure Kotlin policy/reducer adapter (reuse existing classes rather than duplicate) with explicit inputs:

- Canonical notification snapshot and stable Android identity (`StatusBarNotification.key` or existing validated equivalent).
- Current domain/ongoing activity and user-intended presentation state.
- Selected notification key and stable pager cursor.
- Interaction ownership (`TRANSIENT_PREVIEW`, `USER_OPENED`, `REPLY`, `INTERRUPTED`).
- New/update/remove event with semantic update type, user × dismissal event, preview timer event.
- Settings/capabilities: default × Island-only, privacy/clearability, no OS actions in simulation.

Outputs must be serializable/inspectable domain decisions such as:

- `ExpandNotification`, `OpenPager`, `CollapseToCompact`, `OpenQuickControls`, `KeepSelectedKey`, `SelectNextOrPrevious`, `UpsertChildNotice`, `ResurfaceMilestone`, `SuppressMinorUpdate`, `DismissIslandOnly`, and separately `RequestSystemDismiss` ONLY when explicitly enabled and allowed.
- Pure notification queue update and selection result.
- UI presentation intent, but no view operations, `PendingIntent.send`, `WindowManager` calls, or coroutines inside pure reducer.

Use event-specific correlation/version tokens so a stale timer or asynchronous image load cannot mutate a newer state. Use an injected monotonic clock/scheduler on the integration side. Underlying Android shade notifications must never be removed by `Collapse`, `PreviewExpired`, or default `DismissIslandOnly`.

Avoid hardcoded long-press/double-tap semantics until respective gesture tests establish safe behavior; no universal 300ms global touch lockout. Do not implement a second independent `IslandNotificationQueue` if `NotificationReducer` already owns canonical items.

## Step 3 — Integrate safely with existing production and debug paths

- Connect production event intake to the SAME policy layer used by playground fixtures. The debug workbench must exercise the real pure reducer and policy, not an unrelated duplicate controller rule.
- Preserve actual `PendingIntent` / `RemoteInput` dispatch behind production-only effect executor; mocks may log effects but MUST NOT trigger outside actions.
- Add debug structured event trace with event type, selectedKey (redacted or hashed), count, state before/after, owner mode and why branch was selected. No raw private body logging.
- Keep existing UI functional and build compile-safe. If a presentation intent has no existing UI (e.g. horizontal pager/mini-child), expose it in debug traces and mark UI as `NOT YET IMPLEMENTED`; do not fake visual PASS.
- When behavior change could break existing production users, add debug-first wiring or clearly gated capability switch for controlled rollout; document exact switch.
- Do not modify Android overlay touch-window topology, full-width transparency, animation tokens, or compositor in this task.

## Step 4 — Deterministic tests, negative tests and privacy

Implement unit tests for at least these cases from the master acceptance matrix:

- A01, A02, A03, A04, A05, A06, A08, A10, A11, A13, A14, A15, A16, A17, A19, A20, A21, A22, A23, A24, A27, A28, A35, A36, A40.
- U01 policy outcome, U04 double-tap-safe target-state outcome (not a fabricated UI test), C05 music preservation, and P05 listener/reducer deduplication in logic.

Additional negative assertions:
- No default Android notification cancellation for preview expiration, visual collapse, child merge, Island-only ×, selected-page updates or app/queue count changes.
- No duplicate notification identities after same-key post/update/remove.
- `selectedKey` stays valid and stable when list order changes; if removed, next-else-previous.
- Exactly one effect for each user action; repeat events are idempotent.
- Major delivery milestone can resurface a suppressed key once; minor numeric changes cannot spam.
- A queued ordinary message never displaces an explicitly user-owned expanded card.
- No synthetic call/media actions executed by playground.
- All existing unit/instrumentation tests must still compile; if behavior expectations changed, identify old tests intentionally updated with a precise rationale.

Run actual commands available in the repository, not assumed tool outputs. When a physical device is unavailable, mark physical-device cases `BLOCKED / NOT RUN`, never `PASS`.

## Step 5 — Deliverables and stop conditions

Create:

1. `reports/TASK_03_BASELINE_AND_INTEGRATION_PLAN.md`
2. `reports/TASK_03_BEHAVIOR_POLICY_IMPLEMENTATION.md` — exact files/classes changed, new flow diagram and migration explanation.
3. `reports/TASK_03_BEHAVIOR_TEST_EVIDENCE.md` — command, exit code, test methods, PASS/FAIL/BLOCKED, remaining limits.
4. `reports/TASK_03_NEXT_PHASE_READINESS.md` — pager and mini-island UI gaps still unimplemented, identified window touch risks, clear Task 04 proposal.

Submission must include:
- `git status --short`, `git diff --stat`, relevant `git diff` snippets and precise changed file paths.
- Test numbers **computed from actual results**, no unsupported 100% claims.
- Before/after behavior table for UX-C01–C10, and policy-only target state for UX-C11–C15.
- Proof that debug and production share pure business policy but platform side effects stay isolated.
- Regressions, unsupported APIs, and tests you could not run.

**STOP after Task 03. Do not begin the full horizontal pager, mini-island motion, new permission requests, or additional implementation phases until ChatGPT reviews this submission.**
