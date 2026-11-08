# TASK 07 Report: Animation Performance & Frame Latency Breakdown

**Role:** Senior Android QA, Jetpack Compose and Accessibility Engineer  
**Principal Product Architect:** ChatGPT  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Base Branch:** `codex/task-05-layered-mini-islands`  
**Base Commit:** `c5e98d76f118bdb16211e36ab85bf44bd5c28505`  

---

## 1. Executive Summary & Testbed Hardware

Animation performance was profiled on target hardware (`Realme 8`, Android 13) during a continuous multi-cycle benchmark (`LayeredMiniIslandsAnimationBenchmarkTest`) executing 160 frames across main island expansion/collapse, subordinate satellite emergence, horizontal pager swiping, tap promotion, and visual merge transitions.

Raw performance data was extracted directly via `dumpsys gfxinfo com.island.dynamicnotch` and archived in `docs/design/task07-evidence/animation-benchmark.txt`.

### Physical Device Environment
- **Device Model:** Realme 8 (`RMX3085`, Board: `RMX3085L1`)
- **Android OS:** Android 13 (API 33, Build: `SP1A.210812.016`)
- **Display Resolution:** 1080 × 2400 pixels
- **Display Refresh Rate:** 60 Hz (Target frame budget: 16.67 ms)
- **Screen Density:** 480 dpi (`density = 3.0`)
- **Graphics Pipeline:** Skia (OpenGL)

---

## 2. Complete Raw Frame Metrics (`animation-benchmark.txt`)

In strict compliance with ChatGPT's instruction:
> *"Distinguish CPU/GPU frame timings from actual end-to-end frame duration. Do not assert smooth 60 FPS solely from a GPU median."*

The complete, unvarnished frame statistics from the physical device are reported below:

### Overall End-to-End Frame Latency (UI Thread + RenderThread + GPU)
| Metric | Measured Value | Budget (60 FPS) | Assessment |
| :--- | :---: | :---: | :---: |
| **Total Frames Rendered** | **160** | > 150 frames | **SAMPLE SUFFICIENT** |
| **Janky Frames (Frame Deadline Missed)** | **121 (75.62%)** | < 5% | **JANK DETECTED (HIGH)** |
| **Janky Frames (Legacy)** | **123 (76.88%)** | < 5% | **JANK DETECTED (HIGH)** |
| **50th Percentile (Median Frame Duration)** | **150 ms** | < 16.67 ms | **EXCEEDS BUDGET** |
| **90th Percentile Frame Duration** | **600 ms** | < 33.3 ms | **EXCEEDS BUDGET** |
| **95th Percentile Frame Duration** | **800 ms** | < 50.0 ms | **EXCEEDS BUDGET** |
| **99th Percentile Frame Duration** | **1800 ms** | < 100.0 ms | **EXCEEDS BUDGET** |
| **Number Missed Vsync Events** | **103** | 0 | **HIGH** |
| **Number High Input Latency** | **99** | 0 | **HIGH** |
| **Number Slow UI Thread** | **103** | 0 | **PRIMARY BOTTLENECK** |
| **Number Slow Bitmap Uploads** | **3** | 0 | **LOW (MINIMAL)** |
| **Number Slow Issue Draw Commands** | **121** | 0 | **ELEVATED BY UI STALL** |

### Isolated GPU Pipeline Latency (Skia OpenGL Rasterization)
| Metric | Measured Value | Budget (60 FPS) | Assessment |
| :--- | :---: | :---: | :---: |
| **50th Percentile GPU Render Time** | **14.0 ms** | < 16.67 ms | **OPTIMAL (PASS)** |
| **90th Percentile GPU Render Time** | **22.0 ms** | < 33.3 ms | **ACCEPTABLE (PASS)** |
| **95th Percentile GPU Render Time** | **23.0 ms** | < 33.3 ms | **ACCEPTABLE (PASS)** |
| **99th Percentile GPU Render Time** | **24.0 ms** | < 50.0 ms | **ACCEPTABLE (PASS)** |
| **Maximum Observed GPU Render Time** | **24.0 ms** | < 50.0 ms | **ZERO GPU SPIKES > 24ms** |

### Memory & Window Resource Utilization
| Resource | Measured Value | Target Budget | Assessment |
| :--- | :---: | :---: | :---: |
| **Total GPU Memory Footprint** | **12.64 MB** | < 50 MB | **HIGHLY EFFICIENT** |
| **GPU Purgeable Cache** | **8.09 MB** | — | **HEALTHY CACHE** |
| **Texture Cache** | **6.23 MB** | < 20 MB | **OPTIMAL** |
| **RenderTarget Cache** | **4.26 MB** | < 15 MB | **OPTIMAL** |
| **Total CPU Memory (Glyph Cache)** | **289.87 KB** | < 5 MB | **EXCELLENT** |
| **Active ViewRootImpl Count** | **1** | 1 | **NO WINDOW LEAKS** |
| **Attached Views Count** | **7** | < 20 | **MINIMAL NODE TREE** |
| **RenderNode Capacity Used** | **13.75 kB / 45.49 kB** | < 100 kB | **LIGHTWEIGHT** |

---

## 3. Dissecting the Divergence: 14ms GPU vs 150ms Median Total Frame Time

The physical device profiling presents a clear technical divergence:
1. **GPU draw and rasterization pipeline:** Consistently fast, completing in **14ms median** and never exceeding **24ms** across all 160 frames.
2. **End-to-end frame duration:** Reported **121 janky frames (75.62%)** with a median of **150ms** and P90 of **600ms**.

### Technical Root Cause Breakdown:
1. **Synchronous Test Driver Ingestion on Main Thread:**
   In `LayeredMiniIslandsAnimationBenchmarkTest.kt`, the benchmark loop executed `controller.loadNotificationScenario(12)` synchronously on the Android main thread at each test cycle. This triggered simultaneous avatar bitmap extraction, SHA-256 key hashing for 12 items, and StateFlow emissions directly inside the UI thread's looper message queue.
2. **Instrumentation Synchronization Stalls (`Number Slow UI thread: 103`):**
   Between animation steps, the test harness invoked `composeTestRule.waitForIdle()` and `InstrumentationRegistry.getInstrumentation().waitForIdleSync()`. Android Espresso/UIAutomator idling hooks block the UI thread until message queues drain, which Android's `gfxinfo` profiler accurately flags as "Slow UI thread", "Missed Vsync", and "Frame deadline missed".
3. **Pure Compose Animation Spring Execution:**
   When examining the isolated GPU histogram (Section 4), 70.6% of draw passes executed within 15ms. The Compose spring specs (`dampingRatio = 0.82f`, `stiffness = 380f`) update float offsets efficiently without structural layout invalidations. However, when paired with the synchronous instrumentation driver, the overall end-to-end frame rate is throttled by main thread delays.

---

## 4. Full GPU Latency Histogram

Directly from `animation-benchmark.txt` line 26:
```text
GPU Latency Bucket | Frames | Percentage | Cumulative
-------------------|--------|------------|------------
11 ms              |    5   |    3.1%    |    3.1%
12 ms              |    8   |    5.0%    |    8.1%
13 ms              |   31   |   19.4%    |   27.5%
14 ms (Median)     |   47   |   29.4%    |   56.9%  <-- 56.9% <= 14ms
15 ms              |   22   |   13.8%    |   70.6%  <-- 70.6% <= 15ms
16 ms              |    7   |    4.4%    |   75.0%
17 ms              |    3   |    1.9%    |   76.9%
18 ms              |    6   |    3.8%    |   80.6%
19 ms              |    1   |    0.6%    |   81.3%
20 ms              |    3   |    1.9%    |   83.1%
21 ms              |    7   |    4.4%    |   87.5%
22 ms (90th)       |   10   |    6.3%    |   93.8%  <-- 93.8% <= 22ms
23 ms (95th)       |    7   |    4.4%    |   98.1%
24 ms (99th)       |    3   |    1.9%    |  100.0%  <-- 100% <= 24ms
> 24 ms            |    0   |    0.0%    |  100.0%  <-- Zero frames > 24ms
```

---

## 5. Performance Verdict & Truth Statement

1. **GPU Pipeline Health:** **PASS**
   GPU command issuance and Skia rasterization operate cleanly at a **14.0 ms median** (well within the 16.67ms 60 FPS budget), with a low memory footprint (12.64 MB VRAM).
2. **End-to-End Frame Rate under Automated Instrumentation:** **JANK OBSERVED (75.62% Janky Frames)**
   We do **NOT** claim smooth 60 FPS end-to-end performance under the existing test harness. The 150ms median frame duration demonstrates that the automated test driver introduces severe UI thread contention (`103 slow UI thread frames`, `103 missed vsyncs`).
3. **Required Performance Action:**
   To validate genuine 60 FPS user-perceived smoothness without test-induced jank, the test driver must be decoupled from the UI thread (moving scenario loading to a background dispatcher and replacing blocking `waitForIdle()` with choreographer frame polling).
