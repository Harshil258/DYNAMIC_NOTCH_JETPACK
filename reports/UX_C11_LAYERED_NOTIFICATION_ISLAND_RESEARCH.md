# UX-C11 Addendum — Layered Notification Island Architecture & Design Specification

**Document:** `reports/UX_C11_LAYERED_NOTIFICATION_ISLAND_RESEARCH.md`  
**Author:** Android Development Team (Principal Compose Engineer, Senior UI/UX Designer, Mobile Animation Specialist)  
**Reviewer & Final Examiner:** ChatGPT — Principal Product Architect  
**Status:** Complete Architectural Research & Specification  
**Classification:** `[ANDROID CUSTOM / FLAGSHIP CONCEPT INNOVATION]`  
**Code Modifications:** None (`0` lines in `app/src/main/`)

---

## 1. Executive Summary & Concept Vision

When the primary Dynamic Island is actively expanded (e.g., the user is reading an important email, controlling media, or reviewing a delivery), the arrival of a new, unrelated notification creates a severe UX dilemma:

- **Dilemma A (Abrupt Replacement):** Replacing the active card immediately destroys the user's reading or typing context.
- **Dilemma B (Silent Hiding):** Hiding the new notification entirely inside a static counter badge (`+1`) robs the user of the glanceable value they installed the app to receive.

### The UX-C11 Innovation: "Layered Satellite Mini-Islands"
We introduce an **Android-Exclusive, Flagship Innovation**: **Layered Notification Mini-Cards**.

Instead of overwriting the expanded island, an incoming notification causes an animated secondary mini-island to **emerge fluidly from the bottom edge of the main island**, detach like a liquid droplet, expand into a sleek, rounded obsidian mini-card (`48dp` height), and present the incoming sender, icon, and summary. Subsequent notifications form a carefully balanced, animated vertical stack below the main island, preserving the active card's horizontal pager position and reading context with 100% fidelity.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MAIN EXPANDED ISLAND                            │
│  [ WhatsApp Icon ]  Alice · 1 of 5                   [ (x) Close ]     │
│  "Are we still meeting at 3 PM today?"                                 │
│  [ Reply ]   [ Mark as Read ]                                          │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ (8dp Air Gap)
       ┌───────────────────────────┴────────────────────────────┐
       │  [ Telegram ] Bob: "Server deployment complete!"  1m   │  ◄── Satellite Mini-Card 1
       └───────────────────────────┬────────────────────────────┘
                                   │ (6dp Air Gap)
           ┌───────────────────────┴────────────────────────┐
           │  [ Slack ] Team: "New PR submitted #402"  now  │      ◄── Satellite Mini-Card 2
           └────────────────────────────────────────────────┘
```

---

## 2. Apple iOS 18/27 Verification vs. Third-Party Concept Design

A critical requirement of this investigation is establishing what is genuine Apple behavior versus third-party conceptual explorations.

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 APPLE REALITY vs. CONCEPT TAXONOMY                                     │
├─────────────────────────┬──────────────────────┬───────────────────────────────────────────────────────┤
│ Behavior Dimension      │ Status Tag           │ Evidence & Platform Reality                           │
├─────────────────────────┼──────────────────────┼───────────────────────────────────────────────────────┤
│ Dynamic Island Detached │ [APPLE VERIFIED]     │ Apple official HIG supports a single detached minimal │
│ Companion Bubble        │                      │ circle (36.67pt) flanking the camera HORIZONTALLY     │
│                         │                      │ when 2 Live Activities run. Never stacks vertically.  │
├─────────────────────────┼──────────────────────┼───────────────────────────────────────────────────────┤
│ Notification Drop Below │ [APPLE OBSERVED]     │ Push notifications on iOS drop as separate system     │
│ Dynamic Island          │                      │ banners BELOW the island; they do NOT attach, detach, │
│                         │                      │ or stack as sub-islands from the island itself.       │
├─────────────────────────┼──────────────────────┼───────────────────────────────────────────────────────┤
│ 3D Perspective Stacks   │ [APPLE VERIFIED]     │ iOS Lock Screen / Notification Center features card   │
│                         │                      │ stacking with peeking tabs, but strictly in the shade,│
│                         │                      │ never floating over active applications.              │
├─────────────────────────┼──────────────────────┼───────────────────────────────────────────────────────┤
│ Vertical Satellite      │ [ANDROID CUSTOM /    │ Third-party concept designers (Dribbble/X concepts)   │
│ Multi-Island Stack      │  CONCEPT INNOVATION] │ have explored "liquid droplet sub-islands." Apple has │
│                         │                      │ NEVER implemented this in iOS. This is an exclusive   │
│                         │                      │ Android innovation for Aurora Dynamic Island.         │
└─────────────────────────┴──────────────────────┴───────────────────────────────────────────────────────┘
```

> [!IMPORTANT]
> **Definitive Clarification:** We explicitly affirm that layered notification islands emerging vertically below the Dynamic Island are **NOT an official Apple feature**. We adopt Apple's design language (concentric radii, pure black OLED obsidian, hair-thin keylines, fluid spring damping), but the interaction architecture is a custom Android innovation.

---

## 3. Visual Design Specification (Apple-Grade Craft)

### A. Surface, Material & Keylines
- **Base Surface:** Deep Obsidian Black (`#000000` / `#0A0A0C` on OLED). 100% opaque core to prevent text bleeding over background applications.
- **Glassmorphic Keyline (Border):** Concentric hairline gradient border matching Apple's dark mode keyline:
  - Width: `0.75dp`.
  - Brush: `Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x08FFFFFF)))`.
- **Shadow & Halo:** Zero boxy window shadows. On high-contrast backgrounds, a subtle ambient elevation glow is drawn strictly inside the clipped surface bounds (`ambientColor = Color(0x22000000)`).

### B. Geometry, Proportions & Tokens
- **Main Island Width:** $371\text{dp}$ (standard) to $408\text{dp}$ (large screens).
- **Satellite Mini-Card Proportions:**
  - **Width:** Scaled slightly inward by $16\text{dp}$ total ($8\text{dp}$ margins on left and right relative to the main island) to establish visual hierarchy and perspective depth:
    $$\text{Width}_{\text{MiniCard}} = \text{Width}_{\text{MainIsland}} - 16\text{dp} = 355\text{dp}$$
  - **Height:** Exactly $48\text{dp}$ (delivering a glanceable 1-line summary while guaranteeing the $48\text{dp}$ minimum interactive touch target).
  - **Corner Radius:** $20\text{dp}$ (strictly concentric to the main island's $24\text{dp}$ / $42\text{dp}$ corners).
  - **Inter-Island Air Gap:**
    - Gap between Main Island and Mini-Card 1: $8\text{dp}$.
    - Gap between Mini-Card 1 and Mini-Card 2: $6\text{dp}$.

### C. Internal Content Typography & Iconography
- **Leading Slot:**
  - App Icon: $22\text{dp} \times 22\text{dp}$, rounded with a $5\text{dp}$ corner radius.
  - Optional small colored status badge ($6\text{dp}$ dot) indicating message urgency.
- **Center Column (Glanceable Summary):**
  - **Line 1 (Sender & App):** `AppTheme.typography.caption` ($12\text{sp}$ / SemiBold / `#FFFFFF`), e.g., *"Telegram · Bob"*.
  - **Line 2 (Message Text):** `AppTheme.typography.body2` ($11\text{sp}$ / Regular / `#A1A1A6`), max 1 line, strict ellipsis truncation (`TextOverflow.Ellipsis`).
- **Trailing Slot:**
  - Relative Timestamp: $10\text{sp}$ / Regular / `#71717A` (e.g., *"now"*, *"2m"*).
  - Subtle interactive dismiss target or action pill.

---

## 4. Stack Capacity Trade-Off Analysis: 2 vs. 3 vs. 4 Mini-Cards

To determine the optimal stack height, we analyze vertical screen footprint, cognitive load, and touch collision across typical Android device heights ($800\text{dp} - 932\text{dp}$):

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                STACK CAPACITY TRADE-OFF COMPARISON                                     │
├──────────────────────────┬──────────────────────┬──────────────────────┬───────────────────────────────┤
│ Evaluation Criterion     │ 2 Mini-Cards         │ 3 Mini-Cards (Rec.)  │ 4 Mini-Cards                  │
├──────────────────────────┼──────────────────────┼──────────────────────┼───────────────────────────────┤
│ Total Vertical Footprint │ ~250dp – 270dp       │ ~304dp – 324dp       │ ~358dp – 380dp                │
│ (% of Screen Height)     │ ~28% of 900dp screen │ ~34% of 900dp screen │ ~42% of 900dp screen          │
├──────────────────────────┼──────────────────────┼──────────────────────┼───────────────────────────────┤
│ Foreground Occlusion     │ Very Low. Keyboard & │ Moderate. Covers top │ Severe. Occludes center screen│
│                          │ inputs fully visible.│ third; inputs clear. │ controls and active forms.    │
├──────────────────────────┼──────────────────────┼──────────────────────┼───────────────────────────────┤
│ Triage Utility           │ Covers 2 chats; high │ Ideal balance; covers│ Handles large bursts, but     │
│                          │ overflow frequency.  │ multi-app burst well.│ creates visual clutter.       │
├──────────────────────────┼──────────────────────┼──────────────────────┼───────────────────────────────┤
│ Window Measurement Cost  │ Lowest CPU/GPU cost. │ Balanced overhead.   │ High; multiple layout passes. │
├──────────────────────────┼──────────────────────┼──────────────────────┼───────────────────────────────┤
│ Visual Elegance          │ Sleek, minimal.      │ Apple-like harmony.  │ Overwhelming "waterfall".     │
└──────────────────────────┴──────────────────────┴──────────────────────┴───────────────────────────────┘
```

### Recommendation & Overflow Architecture:
- **Default Stack Limit:** **Maximum 2 visible mini-cards** (or 3 on screens $>850\text{dp}$).
- **Overflow Representation:** When a 3rd (or 4th) notification arrives:
  - Mini-Card 2 displays a sleek trailing indicator badge: `"+2 more"`.
  - The additional notifications are ingested directly into the existing `NotificationReducer` and become instantly browseable in the main island's horizontal pager upon collapse.

---

## 5. Animation Storyboard & State Machine Lifecycle

The life cycle of a layered satellite mini-island progresses through 7 discrete animation phases governed by cohesive spring physics:

$$\text{Spring Motion Profile: } \text{Stiffness} = 380\text{f}, \quad \text{Damping Ratio} = 0.82\text{f}, \quad \text{Duration} \approx 350\text{ms}$$

```
   [Main Island]
         │
         ▼ (Phase 1: Emerging)
   [Liquid Bulge] ──► (Phase 2: Detaching) ──► [Satellite Mini-Card] (Phase 3: Active)
                                                      │
         ┌────────────────────────────────────────────┴───────────────────────────┐
         ▼ (New Arrival)                                                          ▼ (Timeout/Swipe)
   [Phase 4: Stack Repositioning]                                           [Phase 5: Merging Back]
   Card 1 slides to Slot 2;                                                 Card glides upward into main;
   Card 2 takes Slot 1.                                                     Unread counter increments.
```

### Storyboard Breakdown:

1. **Phase 1: Emerging (Liquid Droplet Formation, $0 - 120\text{ms}$):**
   - The bottom edge of the main expanded card extends downward by $6\text{dp}$ with an organic curve.
   - A compact black pill ($120\text{dp} \times 24\text{dp}$) appears attached to the bottom rim.
2. **Phase 2: Detaching & Snapping ($120 - 240\text{ms}$):**
   - The connection snaps cleanly.
   - The mini-card translates downward across the $8\text{dp}$ air gap to its resting coordinate:
     $$y_{\text{target}} = y_{\text{main\_bottom}} + 8\text{dp}$$
   - Concurrently, its width expands from $120\text{dp}$ outward to $355\text{dp}$, and height expands from $24\text{dp}$ to $48\text{dp}$.
3. **Phase 3: Content Reveal ($200 - 350\text{ms}$):**
   - Icon, sender text, and summary fade in smoothly (`alpha` $0.0 \to 1.0$) with a subtle $4\text{dp}$ upward parallax translation.
4. **Phase 4: Stack Repositioning (On Subsequent Arrival):**
   - Existing Mini-Card 1 translates downward by $54\text{dp}$ ($48\text{dp} + 6\text{dp}$ gap) to occupy Slot 2.
   - Mini-Card 2 emerges from the main island to occupy Slot 1.
   - Motion is choreographed with a $40\text{ms}$ stagger to prevent rigid block movement.
5. **Phase 5: Transient Display Window ($3.0\text{s} - 4.5\text{s}$):**
   - Mini-cards remain resting in the stack for a configurable preview duration (proposed default: $3.5\text{s}$).
   - Any user touch on the screen pauses the auto-collapse timer.
6. **Phase 6: Merging Back (Reabsorption):**
   - When the preview duration expires, the mini-cards slide upward into the bottom rim of the main island.
   - Card scales down to $0.92\times$ and fades out ($180\text{ms}$).
   - The main island's unread badge increments smoothly ($1 \to 2$).
7. **Phase 7: Manual Swipe Dismissal:**
   - User swipes left or right on a mini-card: the card tracks finger velocity, flies off screen, and lower cards slide up smoothly to take the vacated slot.

---

## 6. Queue Integrity & Horizontal Pager Preservation

A foundational rule of this architecture is: **The layered mini-islands do NOT own a separate queue.**

```
┌────────────────────────────────────────────────────────────────────────┐
│                 SINGLE SOURCE OF TRUTH: NotificationReducer            │
├────────────────────────────────────────────────────────────────────────┤
│ • Main Island Active Key:  "sbn_whatsapp_alice"  (Index 0 in Pager)    │
│ • Satellite Slot 1 Key:    "sbn_telegram_bob"    (Index 1 in Pager)    │
│ • Satellite Slot 2 Key:    "sbn_slack_team"      (Index 2 in Pager)    │
│ • Remaining Ingested:      "sbn_gmail_flight"    (Index 3 in Pager)    │
└────────────────────────────────────────────────────────────────────────┘
```

### Guarantees:
1. **Pager Cursor Preservation:** When Bob's Telegram message arrives, the main card currently viewing Alice's WhatsApp message experiences **zero displacement**. `NotificationQueueState.activeNotificationId` remains locked to Alice.
2. **Deterministic Lifecycle Parity:** If the user opens the horizontal pager after the satellite mini-cards merge back, Bob and Slack are already present at Page 2 and Page 3 in proper priority order.
3. **Bidirectional Synchronization:** If Bob's Telegram notification is canceled externally (e.g., dismissed on desktop), `NotificationEvent.Removed("sbn_telegram_bob")` fires, and Satellite Slot 1 automatically animates away in real time.

---

## 7. Edge Cases, Interruption Arbitration & Concurrency

### A. Rapid Notification Bursts (e.g., 8 Messages in 2 Seconds)
- **Problem:** Spawning 8 cascading animation jobs causes thread contention, frame drops, and visual chaos.
- **Solution:** 
  - Slots are capped at **2 visible cards**.
  - Notifications 1 and 2 animate into Slots 1 and 2.
  - Notifications 3 through 8 are ingested silently into `NotificationReducer`.
  - Slot 2 updates its trailing badge dynamically: `"+6 more"`.
  - Memory consumption and layout passes remain strictly bounded $O(1)$.

### B. Inline Reply on Main Card (IME Active)
- **Problem:** If the user is actively typing a reply on the main card with the keyboard open, satellite mini-cards popping below would occlude the text field or keyboard.
- **Solution:**
  - When `IslandInteractionState == InlineReplying`, the satellite mini-island presentation is **100% suppressed**.
  - Incoming messages are routed silently to `NotificationReducer`, and a discrete sound/haptic tick alerts the user without altering the UI.

### C. Incoming Phone Call (P0 Telephony Preemption)
- **Problem:** A phone call arrives while satellite cards are visible.
- **Solution:**
  - Absolute preemption: all satellite mini-cards are dismissed immediately without delay.
  - State is saved into `SuspendedPagerState`.
  - Main island expands to the full Incoming Call card.

### D. In-Place Content Updates (Delivery / Download Progress)
- If an existing mini-card receives a content update (e.g., Amazon delivery ETA update), the card does **not** re-emerge or shift slots. Text cross-fades in-place.

---

## 8. Android Technical Feasibility & WindowManager Architecture

### The Core Technical Challenge:
How can an Android accessibility overlay host both the main expanded island and detached satellite mini-cards without violating Android 12+ security rules or trapping touches in the transparent air gaps?

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                              WINDOW CONFIGURATION EVALUATION MATRIX                                    │
├──────────────────────────┬───────────────────────────────┬─────────────────────────────────────────────┤
│ Window Architecture      │ Advantages                    │ Feasibility Risks & Limitations             │
├──────────────────────────┼───────────────────────────────┼─────────────────────────────────────────────┤
│ Option A: Giant Full-    │ Simplest Compose tree; single │ FATAL RISK on Android 12+ (API 31+).        │
│ Screen Transparent Window│ state holder manages all      │ Transparent window pixels covering the      │
│ (371dp x 350dp)          │ cards and animations.         │ screen trigger FLAG_WINDOW_IS_OBSCURED,     │
│                          │                               │ blocking clicks in banking / settings apps. │
├──────────────────────────┼───────────────────────────────┼─────────────────────────────────────────────┤
│ Option B: Dynamic Window │ Single window that resizes its│ Viable, but the 8dp gap between main and    │
│ Height Wrapping          │ LayoutParams.height to wrap   │ mini-cards is technically inside the window.│
│ (Tightly Wrapped Height) │ the exact active card stack.  │ Must pass touches through inside the gap.   │
├──────────────────────────┼───────────────────────────────┼─────────────────────────────────────────────┤
│ Option C (Recommended):  │ 100% security compliant. Zero │ Requires managing 2 WindowManager views in  │
│ Multi-Window Overlay     │ transparent dead zones. Air   │ IslandOverlayService. Coordinates must be   │
│ (WindowMain + WindowSat) │ gaps pass touches through.    │ synchronized during spring animations.      │
└──────────────────────────┴───────────────────────────────┴─────────────────────────────────────────────┘
```

### Recommended Window Architecture:
Adopt **Option C (Multi-Window Overlay)**:
1. `WindowMain`: Tightly wrapped around the main expanded island ($371\text{dp} \times \text{height}_{\text{main}}$).
2. `WindowSatellite`: A secondary tightly-wrapped overlay window positioned at $y = y_{\text{main\_bottom}} + 8\text{dp}$, wrapping only the satellite stack ($355\text{dp} \times \text{height}_{\text{stack}}$).
- Both windows use `FLAG_NOT_TOUCH_MODAL | FLAG_WATCH_OUTSIDE_TOUCH`.
- **Zero Touch Interference:** The physical display area between and around the islands contains **no window pixels**, guaranteeing 100% compliance with Android 12+ untrusted touch rules.

---

## 9. Proposed Visual & Interaction Acceptance Test Suite for Playground

To validate this architecture in the debug Playground before production promotion, we specify the following 6 test scenarios:

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 PLAYGROUND ACCEPTANCE TEST MATRIX                                      │
├─────┬────────────────────────────────────┬─────────────────────────────────────────────────────────────┤
│ Test│ Scenario Name                      │ Pass / Fail Verification Condition                          │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T1  │ Satellite Emergence While Expanded │ PASS if main card cursor stays at index 0 and satellite     │
│     │                                    │ card detaches cleanly below with 8dp gap.                   │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T2  │ 2-Card Stack Repositioning         │ PASS if 2nd arrival smoothly translates 1st card down to    │
│     │                                    │ Slot 2 and 2nd card takes Slot 1 within 350ms.              │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T3  │ Overflow Badge Clamping (>2 msgs)  │ PASS if 3rd and 4th arrivals do not spawn 3rd card, but     │
│     │                                    │ update trailing badge to "+2 more" cleanly.                 │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T4  │ Swipe-to-Dismiss Satellite Card    │ PASS if swiping satellite card left evicts it from stack and│
│     │                                    │ surviving card glides upward into Slot 1.                   │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T5  │ Auto-Collapse Reabsorption         │ PASS if after 3.5s timeout, satellite cards slide upward    │
│     │                                    │ into main island and unread counter badge increments.       │
├─────┼────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
│ T6  │ Android 12+ Outside Tap Proof      │ PASS if tapping in the 8dp gap between main and satellite   │
│     │                                    │ immediately clicks the underlying background app button.    │
└─────┴────────────────────────────────────┴─────────────────────────────────────────────────────────────┘
```

---

## 10. Unresolved Product Owner Decisions Register (UX-C11 Scope)

In accordance with strict Examiner instructions, the following implementation choices are **NOT assumed as approved** and are formally registered for owner consultation:

### Decision `DEC-SAT-01`: Tap Action on Satellite Mini-Card
- **Scenario:** The user taps on a satellite mini-card while the main card is expanded.
- **Choices:**
  - **Choice A (Recommended):** Promote to Main Island. Swaps the mini-card into the main expanded viewport, moving the previous main card into the pager queue.
  - **Choice B:** Launch Owning Application immediately.
  - **Choice C:** Expand Satellite Card in-place into a full card (accordion expansion).
- **Status:** `[RECOMMENDED_PENDING]`.

### Decision `DEC-SAT-02`: Satellite Stack Lifetime & Auto-Collapse Duration
- **Scenario:** How long should satellite mini-cards remain visible before merging back into the main island?
- **Choices:**
  - **Choice A:** 2.5 seconds (Fast glance).
  - **Choice B (Recommended):** 3.5 seconds (Standard reading duration).
  - **Choice C:** Persistent until main card is collapsed.
- **Status:** `[RECOMMENDED_PENDING]`.

### Decision `DEC-SAT-03`: Maximum Visible Satellite Card Limit
- **Scenario:** Sizing the maximum vertical stack capacity.
- **Choices:**
  - **Choice A (Recommended):** Maximum 2 Mini-Cards + Overflow counter.
  - **Choice B:** Maximum 3 Mini-Cards + Overflow counter.
  - **Choice C:** Single Mini-Card only (no stack; updates in-place).
- **Status:** `[RECOMMENDED_PENDING]`.

### Decision `DEC-SAT-04`: Satellite Swipe Gesture Policy
- **Scenario:** User swipes horizontally on a satellite mini-card.
- **Choices:**
  - **Choice A (Recommended):** Dismiss from Satellite Stack Only (Notification remains in main pager queue and Android shade).
  - **Choice B:** Full Dismissal (Evict from queue and cancel in Android system shade).
  - **Choice C:** Swipe opens inline reply.
- **Status:** `[RECOMMENDED_PENDING]`.

---

## 11. Architectural Summary & Examiner Sign-Off Gate

| Architectural Pillar | Specification & Commitment |
|---|---|
| **Platform Category** | `[ANDROID CUSTOM / FLAGSHIP CONCEPT INNOVATION]` (Not Apple native). |
| **Main Island State** | 100% preserved; active cursor, pager selection, and inputs unaffected. |
| **Visual Craft** | Concentric radii ($20\text{dp}$), $355\text{dp}$ indented width, hairline keyline, $380\text{f}$ spring. |
| **Queue Relationship** | Direct projection of `NotificationReducer`; zero redundant secondary queues. |
| **Security & Windowing**| Multi-Window overlay architecture compliant with Android 12+ untrusted touch rules. |
| **Production Source Code**| **0 lines modified in `app/src/main/`**. Research & specification only. |

**In accordance with instructions, we have STOPPED and submit this report to ChatGPT for examination.**
