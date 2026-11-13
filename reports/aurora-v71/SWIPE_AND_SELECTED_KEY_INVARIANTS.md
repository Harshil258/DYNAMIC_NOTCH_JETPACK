# AURORA V7.1 — Swipe Gestures, Paging Mechanics & Selected-Key Invariants

**Component**: Horizontal Notification Pager & Queue Navigation  
**Branch**: `codex/aurora-v7-1-notification-height-swipe-review`  
**Verified On**: Realme 8 (`RMX3085`, Android 12)  
**Status**: VERIFIED  

---

## 1. Migration from VerticalPager to HorizontalPager

In earlier versions, `VerticalPager` was used for multi-notification queuing:
- **Flaws**:
  1. Collided with the Android system notification shade drag down gesture.
  2. Collided with internal card vertical scrollability when long notification bodies or action sheets were displayed.
  3. Swiping down on the card to dismiss the Island conflicted with the vertical paging drag listener.
- **V7.1 Architecture**:
  - Replaced with standard Compose `HorizontalPager`.
  - Paging is naturally orthogonal to vertical notification dismissal (swipe-up to dismiss).
  - Paging is naturally orthogonal to internal message text scrolling.
  - Test tags retained for backward compatibility: `notification_vertical_pager`, `notification_horizontal_pager`, and `notification_vertical_indicator`.

---

## 2. Decoupled Programmatic Echo & Single Settlement Commit

Previously, a cyclic feedback loop existed between `notificationActiveIndex` and `onNotificationSelect`:
```kotlin
// The Old Broken Pattern
LaunchedEffect(notificationActiveIndex) {
    pagerState.animateScrollToPage(notificationActiveIndex)
}
LaunchedEffect(pagerState.currentPage) {
    onNotificationSelect(notifications[pagerState.currentPage].id)
}
```
When the user dragged the pager:
1. `pagerState.currentPage` changed mid-drag.
2. `onNotificationSelect` was invoked.
3. The parent composable recomposed with an updated `notificationActiveIndex`.
4. The parent `LaunchedEffect` triggered `animateScrollToPage` *while the user was still dragging*, fighting the user's touch and snapping back or jumping pages.

### V7.1 Settlement Architecture
```kotlin
// Track whether the user is actively driving the pager
LaunchedEffect(pagerState.isScrollInProgress) {
    if (pagerState.isScrollInProgress) {
        userPaging = true
    } else {
        userPaging = false
    }
}

// Programmatic sync only when NOT user-driven
LaunchedEffect(notificationActiveIndex, notifications.size) {
    if (!userPaging && notificationActiveIndex in 0 until pagerState.pageCount) {
        if (pagerState.currentPage != notificationActiveIndex) {
            pagerState.scrollToPage(notificationActiveIndex)
        }
    }
}

// Settlement commits strictly once on settledPage change when idle
LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
        .distinctUntilChanged()
        .collect { page ->
            if (!pagerState.isScrollInProgress && page in notifications.indices) {
                val settledId = notifications[page].id
                if (settledId != lastSettledNotificationId) {
                    lastSettledNotificationId = settledId
                    onNotificationSelect(settledId)
                }
            }
        }
}
```

This guarantees:
1. User drag gestures are never interrupted by programmatic echoes.
2. `onNotificationSelect` fires **exactly once** per page turn, only when the page has settled.
3. Zero duplicate callbacks or jitter.

---

## 3. Stable Notification Key Preservation

When new notifications arrive while the user is viewing or dragging page $k$:
1. Every page in `HorizontalPager` is keyed strictly by its unique notification identity:
   ```kotlin
   HorizontalPager(
       state = pagerState,
       key = { page -> notifications.getOrNull(page)?.id ?: page.toString() },
       ...
   )
   ```
2. When a new notification arrives at index 0, the active notification's key is preserved. Compose's pager internally shifts indices without jumping to the newest notification.
3. Source removals: if the currently selected notification is dismissed from Android system tray, the pager gracefully falls back to the adjacent item without null-pointer crashes or ghost items.

---

## 4. Dot Indicator Synchronization

The right-hand indicator dots represent queue depth (clamped to 5 dots max for UI elegance).
- During drag, reading raw `pagerState.currentPage` caused the active indicator dot to rapidly toggle before the user had committed the turn.
- In V7.1, the active dot index is pinned to `dotActiveIndex = pagerState.settledPage.coerceIn(0, totalCount - 1)`.
- The dot smoothly updates only when a page settlement occurs, matching the iOS Dynamic Island indicator physics.
