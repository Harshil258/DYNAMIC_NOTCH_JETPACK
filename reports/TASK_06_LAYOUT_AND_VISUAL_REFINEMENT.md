# TASK 06 Report: Layout & Visual Refinement

**Role:** Senior Android / Jetpack Compose Engineer  
**Principal Product Architect & Independent Examiner:** ChatGPT  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Branch:** `codex/task-05-layered-mini-islands`  

---

## 1. Low-Height & Dangling Overflow Layout Defect

### Root Cause
In Task 05, `childPresentation()` in `LayeredMiniIslands.kt` calculated:
```kotlin
val limit = when {
    !expanded || ownership == IslandInteractionOwnership.INTERRUPTED || availableHeightDp < 96 -> 0
    else -> (availableHeightDp / (96 * fontScale.coerceAtLeast(1f))).toInt().coerceIn(1, maxVisible.coerceIn(1, 3))
}
val overflow = (pending.size - limit).coerceAtLeast(0)
```
When available display height was constrained (e.g. `availableHeightDp < 96dp`, landscape, open soft keyboard, or large status bar cutout), `limit` became `0`, but `overflow` was evaluated as `pending.size - 0 = pending.size`.

In `LayeredMiniIslands.kt`, the overflow affordance check was:
```kotlin
if (enabled && p.overflow > 0) { ... render Text("+X new · view next") ... }
```
Because `p.overflow > 0` was true even when `limit == 0`, `LayeredMiniIslands` rendered an overflow row below the main island even when available height was 0dp, negative, or insufficient to host any cards. This caused an inaccessible, clipping overflow target that forced the `TYPE_ACCESSIBILITY_OVERLAY` window beyond its safe display bounds.

### Budget & Safe Deferral Algorithm
The layout engine was revised with a deterministic height-budget and deferral policy:

1. **Clean Collapse on Zero Limit**:
   ```kotlin
   // If limit == 0, the secondary stack is completely collapsed; do not expose dangling overflow
   val overflow = if (limit == 0) 0 else (pending.size - limit).coerceAtLeast(0)
   ```
   When display height cannot safely display even 1 child card, `limit == 0` and `overflow == 0`. The secondary stack collapses completely. All underlying notifications remain 100% accessible through the horizontal pager.

2. **Overflow Height Verification**:
   Even when `limit > 0`, the overflow affordance is only presented if the measured available height can fit both the visible child cards AND the overflow affordance:
   ```kotlin
   val overflowHeightDp = (36 * config.fontScale.coerceAtLeast(1f) + 6).dp
   val estimatedCardsHeightDp = (children.size * 54 * config.fontScale.coerceAtLeast(1f) + (children.size - 1).coerceAtLeast(0) * 6).dp
   val canFitOverflow = (available.dp >= estimatedCardsHeightDp + overflowHeightDp)

   if (p.overflow > 0 && canFitOverflow) {
       // Render compact centered overflow capsule
   }
   ```
   If remaining vertical space is insufficient, the overflow UI is deferred non-destructively, preventing off-screen target creation or window overextension.

---

## 2. Premium Mini-Island Visual Refinement

### Problem in Task 05 Baseline
In the initial Task 05 implementation, child cards were rendered at `width = mainWidth.toDp()`, matching the exact full width of the main expanded island. Coupled with 32dp icons, 16dp padding, and 3 unconstrained text lines, the secondary cards visually resembled standard Android notification shade banners rather than integrated Apple-quality Dynamic Island satellite capsules.

### Stage 3 Design Refinements Implemented

| Parameter | Task 05 Baseline | Task 06 Refinement | Product Rationale |
| :--- | :--- | :--- | :--- |
| **Capsule Width** | `mainWidth` (100% full width) | `(mainWidth - 32.dp).coerceIn(240.dp, 336.dp)` | 16dp inset on each side; establishes clear visual hierarchy where the main expanded island is the dominant parent container and child cards are subordinate satellite capsules. |
| **Capsule Height** | ~86dp (unconstrained) | ~50–52dp (min 48dp touch target) | Compact, lightweight geometry that reduces visual clutter while honoring the 48dp accessibility standard. |
| **Corner Radius** | `RoundedCornerShape(32.dp)` | `RoundedCornerShape(20.dp)` | Consistent Apple capsule curvature matching mini-island proportions. |
| **Surface Color** | `Color.Black` | `Color(0xFF0C0E14)` (Obsidian) | Uncompromising Aurora obsidian theme with depth. |
| **Border Stroke** | `1.dp, 12% White` | `1.dp, 10% White` (`Color.White.copy(0.10f)`) | Restrained, non-glaring edge separation against wallpaper. |
| **App Icon Size** | 32dp square | 22dp with 6dp rounded clip | Clean, balanced visual weight that does not dominate the satellite capsule. |
| **Typography Hierarchy** | 3 loose lines (12sp app, 14sp title, 14sp body 2 lines) | 2 tight lines: Line 1 = 11sp App Name (Medium, 60% opacity); Line 2 = 13sp Sender (SemiBold, 100% white) + 12.5sp Preview (75% white) with dot separator | Tight, legible, information-dense preview that eliminates vertical waste. |
| **Vertical Rhythm** | 8dp loose padding | 6dp gap (`Arrangement.spacedBy(6.dp)`), 4dp top inset | Clean vertical cadence below main island bottom edge. |
| **Overflow Affordance** | Full-width rectangle `+X new · view next` | Centered 36dp pill (`+X more · Tap to view`) with 18dp radius and 10% border | Distinctive satellite chip rather than a stretched block. |
| **Motion Physics** | Unstaggered spring | `AppMotion.islandSpring()` (stiffness 460, damping 0.73) with `TransformOrigin(0.5f, 0f)` | Emerges directly from beneath the parent capsule's bottom rim with synchronized alpha and scale-y. |

---

## 3. Touch Routing & Single-Window Tradeoff Documentation

In Android, an overlay window (`WindowManager.LayoutParams`) is strictly rectangular:
- Width = `mainWidth` (the width of the widest element, i.e., the main expanded island).
- Height = `WRAP_CONTENT` (main island height + visible child cards + overflow).

Because the mini-island satellites are centered at `mainWidth - 32.dp`, there are two 16dp horizontal transparent margins flanking each satellite capsule. 

**Engineering Reality & Tradeoff**:
- Within the Android window manager, touches occurring inside the rectangular bounds are dispatched to the `ComposeView`.
- In Jetpack Compose, non-clickable transparent container space (`Box` without click modifiers) does not consume touches; however, because the native window boundary covers this 16dp band, touches in that immediate margin do not pass through to third-party applications behind the overlay.
- Touches falling anywhere outside the window width (i.e. left/right of the main island) or below the bottom edge of the stack pass through seamlessly to underlying apps via `FLAG_NOT_TOUCH_MODAL` and `FLAG_WATCH_OUTSIDE_TOUCH`.
- We explicitly do not fake transparent-pixel touch pass-through, maintaining adherence to platform architecture rules.

---

## 4. Test Verification

### JVM Unit Tests (`LayeredMiniIslandsTest.kt`)
- `zeroHeightOrConstrainedScreenCollapsesChildPresentationWithoutDanglingOverflow`: Verified available heights 80dp, 40dp, 0dp, and -20dp cleanly collapse `visibleLimit = 0` and `overflow = 0`.
- `fontScaleAdaptsSafelyAcross1_0_1_5_and_2_0`: Verified height budget adaptation across 1.0x, 1.5x, and 2.0x font scaling.
- `burstBoundedAtCanonicalCapacityWithNoDuplicateKeys`: Verified 50-notification retention under heavy arrivals.

### On-Device Compose Tests (`LayeredMiniIslandsInstrumentedTest.kt`)
- `three-card-stack.png`: Verified newest-first reordering beneath main expanded island.
- `narrow-large-text-rtl.png`: Verified 320dp width with 2.0x font scale and RTL layout direction.
- `overflow-25-arrivals.png`: Verified compact `+22 more` pill with safe vertical bounds.
