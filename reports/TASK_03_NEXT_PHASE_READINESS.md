# Task 03 — Next Phase Readiness & UI Handoff Report

**Document:** `reports/TASK_03_NEXT_PHASE_READINESS.md`  
**Author:** Android Development Team  
**Reviewer & Final Examiner:** ChatGPT — Principal Product Architect  
**Task Reference:** `productdecesions/GEMINI_TASK_03_BEHAVIOR_ENGINE_FOUNDATION.md`  
**Authority Spec:** `productdecesions/DYNAMIC_ISLAND_PRODUCT_BEHAVIOR_SPEC_V1.md`  
**Target Test Matrix:** `productdecesions/DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md`  
**Status:** READY FOR TASK 04 ARCHITECT EXAMINATION & PLANNING  

---

## 1. Foundation Accomplishments in Task 03

The Task 03 foundation provides a complete, deterministic, pure Kotlin decision engine (`IslandBehaviorEngine`) that models:
1. Pure interaction ownership: `SYSTEM_IDLE`, `TRANSIENT_PREVIEW`, `USER_EXPANDED`, `PAGER_ACTIVE`, `REPLY_ACTIVE`, `INTERRUPTED`.
2. Pure presentation targets: `HIDDEN`, `IDLE`, `COMPACT`, `TRANSIENT_PREVIEW`, `USER_EXPANDED`, `PAGER`, `REPLY`, `QUICK_CONTROLS`.
3. Stable item identity via `selectedKey` (`StatusBarNotification.key`), preventing index drift and selection theft during insertions, updates, and dismissals.
4. Clean milestone classification in `IslandSuppressionTombstoneRegistry`, separating spammy repaints/jitter from genuine milestones.
5. Ingest and arbitration of secondary child satellite notices (`ChildNotice`) without stealing focus from active cards or pagers.
6. Non-destructive dismissal semantics: Island `×` dismiss removes from Island only; swipe-up collapses without canceling OS notifications.

---

## 2. Inventory of UI / Presentation Gaps for Task 04

The following capabilities are fully modeled in the pure behavioral decision layer, but their UI / rendering surfaces in Jetpack Compose are **NOT YET IMPLEMENTED**:

| Feature / Requirement | Behavioral Engine Status | UI Layer Status (`app/src/main/`) | Required Work in Task 04 |
|---|---|---|---|
| **Horizontal Notification Pager (UX-C03 / PD-016)** | **IMPLEMENTED** (`IslandPresentationTarget.PAGER`, `selectedKey`, `SelectPage`, `NextPage`, `PreviousPage`) | **NOT YET IMPLEMENTED** (`NotificationIsland.kt` currently renders only a single expanded card with chevron buttons, not a native `HorizontalPager`). | Author a gesture-driven `HorizontalPager` with bounded spring snapping, active dot indicator / page count `k of n`, and stable page identity binding. |
| **Layered Child Mini-Islands (UX-C11 / UX-C14)** | **IMPLEMENTED** (`ChildNotice`, `childNotices: List<ChildNotice>`, `UpsertChildNotice`) | **NOT YET IMPLEMENTED** (`DynamicIslandPill.kt` renders only the single main pill and optional companion pill; no secondary detached satellite cards below). | Author detached rounded mini-card composable emerging below the main island, sliding older children down, with independent lifetime timers. |
| **Mini-Island Tap Promotion (UX-C12 / A34)** | **IMPLEMENTED** (`IslandEngineEvent.TapChild`, `ExpandNotification`) | **NOT YET IMPLEMENTED** (No click target on non-existent mini-island). | Wire tap on child card to promote key to main expanded surface via smooth morph animation. |
| **Mini-Island Visual Merge (UX-C15 / A35)** | **IMPLEMENTED** (`IslandEngineEvent.ChildTimeout`, visual drop without queue removal) | **NOT YET IMPLEMENTED** (No animation merging child into main). | Implement cohesive upward collapse/merge animation into main island when child expires. |
| **Safe Reply IME Docking (UX-C04 / PD-014)** | **IMPLEMENTED** (`REPLY_ACTIVE`, draft retention policy) | **PARTIALLY IMPLEMENTED** (`NotificationReplyActivity` exists as separate translucent fallback; in-overlay inline reply exists but IME resizing needs tuning). | Audit IME insets and ensure child stack collapses when reply editor is active (PD-033). |

---

## 3. Window Topology & Touch Dispatch Architectural Analysis

Before Task 04 implements the visual rendering of secondary mini-islands and the horizontal pager, the following critical window-layer considerations must be planned:

### A. Single Overlay Window vs. Multi-Window Topology
- **Recommendation:** Keep a **Single Overlay Window** managed by `IslandOverlayService`.
  - Creating separate `WindowManager` windows for each child mini-island creates severe window synchronization latency, z-ordering races, and unpredictable OEM window management quirks.
  - A single overlay window with a dynamic bounds calculator (measuring main card height + spacing + visible child cards) allows Jetpack Compose to manage all spring morphs, repositioning, and layout animations within one cohesive frame budget.

### B. Touch Target & Outside Tap Pass-Through
- **The Touch-Modal Challenge:**
  - When the island is compact or idle, `FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE` allows touches outside the island pill to pass through to underlying applications.
  - When expanded with a pager and multiple child mini-islands, the overlay height increases. If the transparent regions between the main island and children intercept touches, background apps (e.g. browser navigation or keyboard) will feel unresponsive.
  - **Task 04 Contract:** The overlay view must use a custom `LayoutCoordinates` / touch-delegate bounds check (`IslandContentViewport`) so that only genuine black island pills and cards consume touch events, transparent gaps remain click-through, and outside taps reliably collapse the island (UX-C06, PD-014).

---

## 4. Prerequisite Architectural Inputs for Task 04

To begin Task 04 without rework, the developer will require from the Principal Product Architect:
1. **Approval of Task 03 Engine & Test Evidence:** Confirmation that `IslandBehaviorEngine` and `IslandSuppressionTombstoneRegistry` serve as the authorized foundation.
2. **Spring Physics & Spacing Spec for Mini-Islands:** Exact dp spacing between main expanded card and first child mini-card (e.g. 8dp), corner radius (e.g. 18dp), and spring stiffness/damping ratio.
3. **Pager Paging Physics:** Whether horizontal drag should use standard Android `PagerDefaults.flingBehavior()` or custom velocity snapping.
4. **Authorization to Begin Task 04:** Explicit prompt from the user to proceed.
