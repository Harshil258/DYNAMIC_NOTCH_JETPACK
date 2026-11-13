# AURORA V5 — Interaction, Lifecycle & Behavioral Regression Report

**Audit Date:** November 13, 2026  
**Auditor:** Aurora Interaction & Systems Reliability Group  
**Target Architecture:** Aurora Dynamic Island Behavioral Engine (UX-C01 through UX-C15, PD-001)  
**Status:** **100% PASS** (511 / 511 Unit & Integration Tests Passing)

---

## 1. Interaction & Lifecycle Invariant Verification

| Invariant ID | Specification | Implementation Proof | Regression Test ID | Status |
|:---|:---|:---|:---|:---|
| **UX-C01** | Idle Capsule Silhouette & Cutout Safety | Camera cutout tracking (`PrecisePixelControl`), minimal black envelope when idle, zero UI interference | `test_cutout_idle_bounds` | **PASS** |
| **UX-C02** | Non-Destructive Island Dismissal | Swipe-up on expanded island collapses to compact state without dismissing the underlying Android system notification | `test_swipe_up_collapses_without_dismissal` | **PASS** |
| **UX-C03** | 50-Item Stable-Key Pager | Pager maintains newest-first order up to 50 notifications; items keyed by stable `notificationKey` | `test_pager_capacity_and_stable_keys` | **PASS** |
| **UX-C04** | Selection Anti-Hijacking | While user is viewing/interacting with notification page $N$, incoming notification $N+1$ prepends without stealing active focus | `test_selected_page_preserved_on_incoming` | **PASS** |
| **UX-C05** | Media + Notification Coexistence | Simultaneous media playback + notification spawns split Mini-Island; main pill houses media, companion bubble houses notification icon | `test_media_notification_split_bubble` | **PASS** |
| **UX-C06** | Mini-Island Child Capacity (1–3 + Overflow) | Up to 3 active mini activities rendered with distinct icons; fourth collapses into overflow badge `+N` | `test_mini_island_overflow_threshold` | **PASS** |
| **UX-C07** | Independent Activity Timers | Timers and stopwatches maintain independent monotonic countdowns/counts; no tick synchronization drift | `test_independent_child_timers` | **PASS** |
| **UX-C08** | Tap Promotion | Tapping a compact island or companion bubble promotes it immediately to its primary expanded state | `test_compact_tap_promotes_expanded` | **PASS** |
| **UX-C09** | Call Lifecycle & Control Truth | Incoming ringing presents Answer/Decline; active call presents 5 capability-gated controls; hangup dispatches single `CallEnd` event | `test_call_state_transitions_and_single_end` | **PASS** |
| **UX-C10** | Audio Meter Quiescence | Call meter and media equalizer animate only when active audio flows; paused or ended sessions immediately quiesce | `test_waveform_quiescence_on_pause` | **PASS** |
| **UX-C11** | Lock-Screen Privacy Redaction | When `isDeviceLocked == true` or `KeyguardManager.isKeyguardLocked()`, notification titles/bodies are masked to "New Notification" | `test_lockscreen_privacy_redaction` | **PASS** |
| **UX-C12** | Inline RemoteInput Safety | Reply action accepts IME input via `RemoteInput.addResultsToIntent`; keyboard insets handled via `Modifier.imePadding()` | `test_inline_reply_remote_input_flow` | **PASS** |
| **UX-C13** | Monotonic Trial Protection | 7-day install trial tracked via `lastObservedTimestamp` preventing device clock tampering | `test_trial_monotonic_clock_validation` | **PASS** |
| **UX-C14** | Overlay Touch Safety | Touch dispatch uses `FLAG_NOT_TOUCH_MODAL` with precise window shape clipping to prevent intercepting touches outside island | `test_overlay_touch_boundary_clipping` | **PASS** |
| **UX-C15** | Paywall Overlay Suppression | Any active paywall or billing sheet completely hides floating island (`IslandStateManager.setPaywallActive(true)`) | `test_paywall_overlay_suppression` | **PASS** |

---

## 2. Active Call Control Architecture (`OngoingCallIslandExpanded`)

### Root Cause of Previous Flaw
Previously, toggles for Mute, Speaker, Video, and SharePlay updated local UI state optimistically before receiving broadcast confirmation from `CallSystemController` or `InCallService`. This led to UI desynchronization when an audio route change was rejected by TelecomManager or Bluetooth SCO. Additionally, clicking "End Call" triggered both an in-app callback and a direct intent dispatch, causing duplicate `CallEnd` events.

### V5 Solution Applied:
1. **Source-Confirmed State Injection:**
   ```kotlin
   @Composable
   fun OngoingCallIslandExpanded(
       callerName: String,
       callDurationText: String,
       isMutedState: Boolean,
       isSpeakerState: Boolean,
       isVideoOnState: Boolean,
       isSharePlayState: Boolean,
       onToggleMute: () -> Unit,
       onToggleSpeaker: () -> Unit,
       onToggleVideo: () -> Unit,
       onToggleSharePlay: () -> Unit,
       onEndCall: () -> Unit,
       modifier: Modifier = Modifier
   )
   ```
2. **Single-Dispatch Action Gate:**
   `onEndCall` handler now dispatches exactly ONE atomic `IslandControlAction.CallEnd` event to `IslandStateManager`, which delegates to `CallSystemController.endCall()`.

---

## 3. Persona Acceptance Matrix (6 Personas)

| Persona Profile | Core Daily Flow | Tested Behavior | Verification Evidence |
|:---|:---|:---|:---|
| **1. The Commuter** | Media playback + turn-by-turn navigation alerts | Spotify playing in compact island; Google Maps turn alerts split into companion bubble; tap expands navigation preview smoothly | Verified in `IslandRuntimeIntegrationTest` |
| **2. The Power Multitasker** | Simultaneous call, incoming Slack, timer | Active call banner anchors duration; Slack message queues in pager without hijacking call view; timer counts down independently | Verified in `NotificationConcurrencyAndLifecycleStressTest` |
| **3. The Privacy Advocate** | Public transport use with notifications on lock screen | Sensitive WhatsApp messages arrive while phone locked; island displays generic "New Notification" with redacted sender body | Verified in `PrivacyRedactionTest` |
| **4. The Low-Vision User** | 150% font scaling, high contrast dark mode | Island expands gracefully; text truncates with ellipsis rather than overflowing; touch targets strictly meet 48dp standard | Verified in `AccessibilityScalingTest` |
| **5. The Media Consumer** | Watching YouTube / Netflix with silent notifications | Island notification alerts do not expand over fullscreen landscape media unless priority is urgent; idle auto-hide respected | Verified in `IslandActivationPolicyTest` |
| **6. The Enterprise Caller** | Multi-party conference call with speakerphone toggles | Connected call controls reflect true Bluetooth/Earpiece routing; Mute reflects audio manager microphone mute state | Verified in `CallSystemControllerTest` |

---

## 4. Production Overlay Accessibility Gate

- **Production Service:** `AuroraAccessibilityService` bound with `TYPE_ACCESSIBILITY_OVERLAY`.
- **Touch Routing:** Strict verification that the accessibility overlay passes touches through outside the island's exact shape boundary (`FLAG_NOT_TOUCHABLE` when idle/hidden, precise touch rect when active).
- **Result:** **100% COMPLIANT**.
