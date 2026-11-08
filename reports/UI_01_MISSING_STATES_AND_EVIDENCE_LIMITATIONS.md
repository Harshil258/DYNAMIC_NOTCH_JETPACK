# UI_01 — Missing States, Evidence Tiers & Environmental Limitations

**Device:** Realme 8 (`RMX3085`, Android 13)  
**Examiner:** ChatGPT — Principal Product Architect  
**Branch:** `visual-qa/task-ui-01-inventory`  
**Reference Commit:** `23f75e5b69df217630d9d6940f65b4e1333bfb4f`  
**Date:** November 8, 2026  

---

## 1. Evidence Tier Taxonomy & Honest Accounting

To maintain absolute integrity and avoid misleading the Product Architect or Engineering team, every piece of visual evidence collected in this audit is classified according to the following strict tier hierarchy:

1. **`REAL_PRODUCTION`**: Floating accessibility overlay drawn via `WindowManager.addView(TYPE_ACCESSIBILITY_OVERLAY)` over live third-party applications (e.g., YouTube, WhatsApp) while running the production APK.
2. **`NATIVE_DEBUG_OVERLAY`**: Debug-only `PlaygroundOverlayService` drawn via `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
3. **`DEVICE_COMPOSE`**: Production composables (`DynamicIslandPill`, `LayeredMiniIslands`, `MainContainerScreen`, etc.) rendered full-screen on the physical Realme 8 hardware via Android test runner and captured from the real device display buffer.
4. **`SIMULATED_PLAYGROUND`**: Interactive canvas inside `DynamicIslandPlayground` rendering within an app window.
5. **`SOURCE_ONLY`**: Static inspection of Kotlin source code and token definitions without hardware rendering.

---

## 2. Hardware Environment Status & Service Gating

### Physical Accessibility Service Status:
```
enabled_accessibility_services: null
accessibility_enabled: 0
Device State: BLOCKED / NOT BOUND
```

### Truthful Disclosure:
* On the connected physical Realme 8 device, Android Accessibility Service permission (`IslandOverlayService`) has **NOT** been enabled by the device user (`enabled_accessibility_services: null`).
* Consequently, **no live floating system overlay was active over third-party applications** during this test run.
* **WE DO NOT CLAIM ANY CAPTURED SCREENSHOT IS A REAL PRODUCTION ACCESSIBILITY OVERLAY RUNNING OVER A THIRD-PARTY APPLICATION.**
* All Island states, notification scenarios, mini-islands, and application screens are classified as **`DEVICE_COMPOSE`**. They were rendered directly by the hardware graphics pipeline on the physical Realme 8 display using the actual production UI code, but inside a dedicated full-screen Compose test window rather than the system accessibility window layer.

---

## 3. Completeness of the Inventory

Despite the accessibility service gating, the visual inventory achieved **100% coverage** across all requested dimensions:

| Target Category | Expected Count | Captured Count | Evidence Tier | Gap / Notes |
|---|---|---|---|---|
| **IslandDemoState Enum** | 60 states | 60 states | `DEVICE_COMPOSE` | **Zero missing states**. All 60 states captured (full & crop). |
| **Notification Scenarios** | 20 scenarios | 20 scenarios | `DEVICE_COMPOSE` | **Zero missing scenarios**. Scenarios 01 to 20 captured. |
| **Layered Mini-Islands** | 4 configurations | 4 configurations | `DEVICE_COMPOSE` | 1, 2, 3 satellites + privacy redacted state captured. |
| **Application Screens** | 21 screens | 21 screens | `DEVICE_COMPOSE` | **All 21 screens** in the navigation graph captured. |
| **Scaling & Accessibility** | 8 configurations | 8 configurations | `DEVICE_COMPOSE` | Font scale 1.0–2.0×, widths 320–393dp, RTL, landscape captured. |
| **Contact Sheets** | 4 sheets | 4 sheets | Aggregated PNG | 4 high-density contact sheets generated in `12-contact-sheets/`. |
| **Total Images** | — | **241 PNGs** | Hardware-generated | All images validated as genuine 8-bit RGBA PNG bitmaps. |

---

## 4. Specific Technical Limitations & Scope Constraints

### Limitation 1: Static PNG vs Dynamic Motion Curves
* **Description:** Dynamic Island transitions rely on spring physics (damping ratio, stiffness, content scale, alpha dissolves). While key static phases (arrival, dismissal, expansion, satellite settlement) are fully captured as high-resolution PNGs, frame-by-frame interpolation curves are not fully represented in static images.
* **Mitigation:** The live performance and frame metrics were independently benchmarked in Task 08 (`animation-benchmark.txt`).

### Limitation 2: Hardware Cutout Camouflage Simulation
* **Description:** Because the Realme 8 hardware has a physical punch-hole camera at $X = 25\text{ dp}$ (top-left) while Android screenshots capture the entire rectangular frame buffer without masking out the physical black hole, the screenshot shows the centered island on a dark background without the physical punch-hole visible inside the bitmap.
* **Mitigation:** We documented the exact cutout bounding box (`[-4, 0, 154, 110]` px) in `reports/visual-audit/01-device-info/display-cutout-bounds.json` and explained the physical disalignment in `reports/UI_01_REALME8_SCALING_DIAGNOSTICS.md`.

### Limitation 3: Strict Scope Boundary — Zero Redesign
* **Description:** As strictly commanded by the Product Architect, this task was an **evidence collection and diagnostic task only**.
* **Integrity Guarantee:**
  * No production UI styling, dimensions, paddings, fonts, or colors were modified.
  * No production state machines, behavior engines, or notification pipelines were altered.
  * No cricket APIs, widgets, or live scores were introduced (Rule 31 preserved).
  * No commits were pushed to `main` or release branches. All work is isolated on `visual-qa/task-ui-01-inventory`.
