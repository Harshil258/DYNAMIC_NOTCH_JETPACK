# Complete Dynamic Island Production & Playground Inventory (Corrected)

## Overview & Methodology
This inventory documents every Dynamic Island state, visual variant, and activity family capable of being rendered by the Android application codebase. Every entry reflects actual source code in `app/src/main/` and is compared directly against the debug workbench in `app/src/debug/`.

The application architecture models islands through a unified sealed state hierarchy:
- **Domain State:** [`ai.emots.kishan_dynamic.data.model.IslandState`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L76) (16 sealed subclasses) + [`LiveActivityKind`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/LiveActivityModels.kt#L6) (19 enum entries) = **35 functional state types**, expanding to **50 distinct layout/behavioral variants**.
- **Coordinator & Reducer:** [`ai.emots.kishan_dynamic.service.IslandStateManager`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt#L35)
- **Presentation Mapping:** [`ai.emots.kishan_dynamic.ui.components.IslandPresentationMapper`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandPresentationMapper.kt#L6)
- **Canonical Renderer:** [`ai.emots.kishan_dynamic.ui.components.DynamicIslandPill`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt#L370)

---

## Master Inventory of Production Islands

### 1. Inactive / Hidden
- **Production ID:** `PROD-01`
- **Purpose:** Total suppression of the island overlay (e.g. keyguard locked without lockscreen permission, paywall visible, or island disabled).
- **Source File & Class/Function:** [`IslandState.Hidden`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L77); `PlaygroundRenderer.kt:77` wraps in `AnimatedVisibility(visible = source != IslandState.Hidden)`.
- **Triggering Event:** Service startup when inactive, user disable toggle, device locked with `showOnLockScreen = false`, or `IslandStateManager.setPaywallActive(true)`.
- **Data Source:** System lifecycle / Settings.
- **Collapsed / Compact Appearance:** 0dp × 0dp (hidden).
- **Expanded Appearance:** N/A.
- **Supported Actions:** None.
- **Available Animations:** Fade + scale exit (`islandVisibilityExit`).
- **Dismissal & Timeout:** Stays hidden until active event arrives.
- **Playground Support:** Testable via `PlaygroundCase.Hidden`.
- **Implementation Parity:** Shared Compose visibility wrapper; isolated from production lifecycle.
- **Status:** IMPLEMENTED (Actual).

---

### 2. Idle Capsule / Minimal Dot
- **Production ID:** `PROD-02`
- **Purpose:** Unobtrusive hardware camera cutout anchor capsule when no background or transient activities are active.
- **Source File & Class/Function:** [`IslandState.Minimal`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L78); `DynamicIslandPill.kt:1044` `IdleCutoutContent()`, line 1045 `MinimalPillContent()`.
- **Triggering Event:** Background service idle state; all notifications/activities dismissed or expired.
- **Data Source:** `IslandStateManager._currentState` fallback.
- **Collapsed Appearance:** 24dp × 24dp circular dot (or cutout dimension) matching camera hole.
- **Compact Appearance:** Capsule pill (default `compactWidth` 76dp × 36.67dp).
- **Expanded Appearance:** None directly from tap; long-press expands Quick Controls.
- **Supported Actions:** Long-press opens Quick Controls (`IslandInteractionPolicy.mainLongPress`).
- **Available Animations:** Spring morph (`islandSpring()`), idle breathing aura.
- **Dismissal & Timeout:** Persistent while island is enabled.
- **Playground Support:** Testable via `PlaygroundCase.Idle`.
- **Implementation Parity:** Shared Compose renderer; in-memory controller dispatch.
- **Status:** IMPLEMENTED (Actual).

---

### 3. Music Playback (Compact & Expanded)
- **Production IDs:** `PROD-03` (Compact), `PROD-04` (Expanded)
- **Purpose:** Live audio playback controls and track details (Spotify, YouTube Music, Apple Music, etc.).
- **Source File & Class/Function:** [`IslandState.Music`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L80); [`MusicPlayerIsland.kt:43`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/MusicPlayerIsland.kt#L43); `DynamicIslandPill.kt:1046-1053`.
- **Triggering Event:** `MediaSessionManager.OnActiveSessionsChangedListener` or Media notification.
- **Data Source:** [`MediaPlaybackRegistry`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/MediaPlaybackRegistry.kt#L23) reading `MediaController.PlaybackState` and `MediaMetadata`.
- **Compact Appearance:** Album artwork thumbnail on leading edge, animated live waveform on trailing edge.
- **Expanded Appearance:** Full media card (177dp height): Album artwork, Track Title, Artist, Scrubber slider with elapsed/total time, Previous, Play/Pause, Next, and AirPlay/Audio Output routing glyph.
- **Supported Actions:** Tap expands; Long-press opens Quick Controls; Expanded buttons: `MusicPrevious`, `MusicPlayPause`, `MusicNext`, audio seek scrubber.
- **Available Animations:** Fluid spring dimension expansion, waveform bar pulse, scrub bar drag animation.
- **Dismissal & Timeout:** When paused, a 3500ms debounce timer (`pausedMusicJob` in `IslandStateManager:646`) runs before dismissing.
- **Playground Support:** Testable via `PlaygroundCase.Music` (compact) and manual/sequence expansion.
- **Implementation Parity:** Shared renderer; media commands in playground update local mock position/state without calling system `MediaSession`.
- **Status:** IMPLEMENTED (Actual).

---

### 4. Ongoing Call (Active Call)
- **Production IDs:** `PROD-05` (Compact), `PROD-06` (Expanded)
- **Purpose:** Real-time cellular telephony or VoIP call indicator with active duration ticker.
- **Source File & Class/Function:** [`IslandState.OngoingCall`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L86); [`OngoingCallIsland.kt:44`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/OngoingCallIsland.kt#L44); `DynamicIslandPill.kt:1055-1067`.
- **Triggering Event:** Telephony off-hook state or VoIP ongoing notification (`CallNotificationPolicy.isOngoing`).
- **Data Source:** [`CallSessionCoordinator`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/CallSessionCoordinator.kt#L332) or `IslandNotificationListener`.
- **Compact Appearance:** Green phone icon with live ticking duration timer (e.g. "01:24") or pulsing green dot when dialing.
- **Expanded Appearance:** Contact Avatar, Contact Name, Call Status / Duration, Speaker toggle, Mute microphone toggle, Video toggle, End Call red circular button (166dp height).
- **Supported Actions:** Tap toggles expansion (`IslandInteractionPolicy.mainTap`); Expanded controls: `CallEnd`, `CallToggleSpeaker`, `CallToggleMute`.
- **Available Animations:** Spring expand/collapse, green waveform aura, red button tap compression.
- **Dismissal & Timeout:** Dismisses immediately when telephony state transitions to `IDLE` or VoIP notification removed, transitioning directly to Call Summary.
- **Playground Support:** Testable via `PlaygroundCase.ActiveCall`.
- **Implementation Parity:** Shared renderer; mock clock updates call duration locally without telephony bindings.
- **Status:** IMPLEMENTED (Actual).

---

### 5. Incoming Call
- **Production ID:** `PROD-07`
- **Purpose:** Heads-up alert for ringing inbound calls with caller details and accept/decline actions.
- **Source File & Class/Function:** [`IslandState.IncomingCall`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L102); [`IncomingCallIsland.kt:51`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/IncomingCallIsland.kt#L51); `DynamicIslandPill.kt:1212-1216`.
- **Triggering Event:** Inbound telephony `EXTRA_STATE_RINGING` or VoIP notification with answer actions (`CallNotificationPolicy.isIncoming`).
- **Data Source:** [`CallSessionCoordinator`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/CallSessionCoordinator.kt#L268) / `TelephonyManager`.
- **Compact Appearance:** N/A (Always expanded presentation mode).
- **Expanded Appearance:** Contact avatar / initials, caller name, "Incoming Call" / "mobile" label, circular Red Decline button and circular Green Accept button.
- **Supported Actions:** `CallAccept` (`IslandControlAction.CallAccept`), `CallDecline` (`IslandControlAction.CallDecline`).
- **Available Animations:** Scale-in spring bounce, pulsing glowing green ring.
- **Dismissal & Timeout:** Dismisses on call answer (transitions to Ongoing Call) or call reject/missed (dismisses to idle or Call Summary).
- **Playground Support:** Testable via `PlaygroundCase.IncomingCall`.
- **Implementation Parity:** Shared renderer; mock actions switch state locally to `ActiveCall` or `Idle`.
- **Status:** IMPLEMENTED (Actual).

---

### 6. Call Summary
- **Production ID:** `PROD-08`
- **Purpose:** Post-call retrospective sheet providing redial, WhatsApp shortcut, messaging, or dismissal.
- **Source File & Class/Function:** [`IslandState.CallSummary`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L98); [`CallSummaryIsland.kt:39`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/CallSummaryIsland.kt#L39); `DynamicIslandPill.kt:1068`.
- **Triggering Event:** Disconnection of an active telephony call.
- **Data Source:** `CallSessionCoordinator.notifyCallEnded` via `CallHistoryRepository`.
- **Compact Appearance:** N/A (Always expanded presentation mode).
- **Expanded Appearance:** Contact avatar, contact name, total call duration (e.g. "Call ended · 3m 42s"), and action buttons: Call Back, Message, WhatsApp, Dismiss.
- **Supported Actions:** `CallSummaryRedial`, `CallSummaryMessage`, `CallSummaryWhatsApp`, `CallSummaryDismiss`.
- **Available Animations:** Smooth card expansion and auto-fade exit.
- **Dismissal & Timeout:** Automatically auto-dismisses after 6000ms (`CALL_SUMMARY_DISPLAY_MILLIS` in `IslandStateManager:1402`).
- **Playground Support:** Partially testable in code (`resolveOverlayDemoState:11`); **MISSING direct selector in `PlaygroundCase.entries`**.
- **Implementation Parity:** Shared renderer; missing UI trigger.
- **Status:** IMPLEMENTED in production; MISSING direct trigger in Playground.

---

### 7. Standard Notifications (Single, Stacked, Expanded, Inline Reply)
- **Production IDs:** `PROD-09` (Single Compact), `PROD-10` (Stacked Compact), `PROD-11` (Expanded), `PROD-12` (Inline Reply)
- **Purpose:** Display incoming push notifications, chat messages, system updates, and downloads with inline actions and inline text reply.
- **Source File & Class/Function:** [`IslandState.Notification`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L111); [`NotificationIsland.kt:54`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt#L54); `DynamicIslandPill.kt:1085-1114`.
- **Triggering Event:** `IslandNotificationListener.onNotificationPosted`.
- **Data Source:** Android `StatusBarNotification` via `NotificationContentResolver` and `NotificationReducer`.
- **Compact Appearance:** App icon on leading edge; notification title/text on trailing edge. If multiple notifications are stacked: App icon on leading edge, unseen counter bubble (e.g. "+3") on trailing edge (`NotificationStacked`).
- **Expanded Appearance:** App icon + App Name header, Sender/Title, Message body, picture attachment (if present), horizontal action buttons (Reply, Mark Read, Snooze), or full `InlineReplyBox` text field.
- **Supported Actions:** Single tap expands (`IslandInteractionPolicy.mainTap`); companion tap selects next stacked notification; swipe up dismisses (`onSwipeDismiss`); action button clicks dispatch via `NotificationActionRegistry.send`; inline reply sends via `NotificationActionRegistry.sendReply`.
- **Available Animations:** Authentic Apple pill-to-card expansion spring, message line crossfade, text cursor blink, send button pop.
- **Dismissal & Timeout:** Configurable timeout (default 5000ms via `NotificationDisplayDurationPolicy` in `IslandStateManager.scheduleNotificationExpiry`); swipes dismiss immediately.
- **Playground Support:** Testable via `PlaygroundCase.Minimal`, `PlaygroundCase.Compact`, `PlaygroundCase.Expanded`, `PlaygroundCase.Success`, `PlaygroundCase.Warning`, `PlaygroundCase.Error`.
- **Implementation Parity:** Shared renderer; actions update local state without firing Android `PendingIntent`.
- **Status:** IMPLEMENTED (Actual).

---

### 8. Split Island (Notification with Music)
- **Production ID:** `PROD-13`
- **Purpose:** Apple-style dual-bubble liquid split displaying active media on one side and a concurrent notification or alert on the other.
- **Source File & Class/Function:** [`IslandState.NotificationWithMusic`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L119); `DynamicIslandPill.kt:1094-1100` & lines 1243-1297 (companion split bubble).
- **Triggering Event:** Incoming notification arrives while `MediaPlaybackRegistry` is active.
- **Data Source:** `IslandStateManager.postNotification` evaluating `activeMusicTrack != null`.
- **Compact Appearance:** Main pill on left showing Notification app icon + message title; separate detached circular companion bubble on right with album art or animated music equalizer.
- **Expanded Appearance:** Expands to whichever activity is tapped (main tap opens media or notifications depending on play state; companion tap opens the secondary).
- **Supported Actions:** Main tap, companion tap, long-press opens Quick Controls, swipe dismiss.
- **Available Animations:** Liquid separation physics (`animatedGap`, `animatedSideSize` with Apple liquid separation spring in `DynamicIslandPill.kt:1243-1297`).
- **Dismissal & Timeout:** When notification expires, companion bubble merges smoothly back into the main music pill.
- **Playground Support:** Testable via `PlaygroundCase.Multiple` and sequence "Multiple".
- **Implementation Parity:** Shared renderer; in-memory mock dispatch.
- **Status:** IMPLEMENTED (Actual).

---

### 9. Battery Charging & Low Battery Alert
- **Production IDs:** `PROD-14` (Normal Charging), `PROD-15` (Low Battery <20%)
- **Purpose:** System power alert indicating charger connection, fast charging status, and critical battery warnings (< 20%).
- **Source File & Class/Function:** [`IslandState.Charging`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L126); [`ChargingIsland.kt:36`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/ChargingIsland.kt#L36); `DynamicIslandPill.kt:1158-1167`.
- **Triggering Event:** `BatteryStateReceiver.onReceive` (`Intent.ACTION_POWER_CONNECTED`, `ACTION_BATTERY_LOW`).
- **Data Source:** Android BatteryManager.
- **Compact Appearance:** Green battery glyph with lightning bolt on leading side, battery percentage (e.g. "85%") in green on trailing side. For low battery: Amber/Red battery glyph with red percentage (e.g. "12%").
- **Expanded Appearance:** Large battery ring progress indicator, percentage label, "Fast Charging" or "Low Battery Warning" subtitle.
- **Supported Actions:** Transient confirmation; long-press opens Quick Controls.
- **Available Animations:** Smooth scale pop, pulsing green/amber bolt.
- **Dismissal & Timeout:** Automatically auto-dismisses after 3500ms (`IslandStateManager.postCharging` line 841).
- **Playground Support:** Testable via `PlaygroundCase.Charging` and `PlaygroundCase.LowBattery`.
- **Implementation Parity:** Shared renderer; mock values.
- **Status:** IMPLEMENTED (Actual).

---

### 10. Ringer Mode Switch (Silent, Vibrate, Normal)
- **Production IDs:** `PROD-16` (Silent), `PROD-17` (Vibrate), `PROD-18` (Normal), `PROD-19` (Expanded)
- **Purpose:** Apple-style hardware ringer/mute switch notification banner.
- **Source File & Class/Function:** [`IslandState.RingerMode`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L131); [`RingerModeIsland.kt:43`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/RingerModeIsland.kt#L43); `DynamicIslandPill.kt:1115-1119`.
- **Triggering Event:** `AudioManager.RINGER_MODE_CHANGED_ACTION` broadcast.
- **Data Source:** System `AudioManager`.
- **Compact Appearance:** Red Bell with slash ("Silent"), Vibrate waves glyph ("Vibrate"), or Gray Bell ("Ring").
- **Expanded Appearance:** Expanded card showing Bell glyph, text description ("Silent Mode · On" / "Normal Mode"), and ringer toggle.
- **Supported Actions:** Tap toggles expansion; long-press opens Quick Controls.
- **Available Animations:** Bell strike-through animation, spring pop.
- **Dismissal & Timeout:** Auto-dismisses after 2500ms (`IslandStateManager.postRingerMode:912`).
- **Playground Support:** Partially testable in code (`resolveOverlayDemoState:21`); **MISSING direct trigger in `PlaygroundCase.entries`**.
- **Implementation Parity:** Shared renderer; missing UI trigger.
- **Status:** IMPLEMENTED in production; MISSING direct trigger in Playground.

---

### 11. System Volume HUD (Media & Ringer Volume)
- **Production IDs:** `PROD-20` (Ringer Volume), `PROD-21` (Media Volume)
- **Purpose:** In-island replacement for OEM volume slider HUD.
- **Source File & Class/Function:** [`IslandState.MediaVolume`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L140) & [`IslandState.RingerVolume`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L136); `DynamicIslandPill.kt:1120-1129`.
- **Triggering Event:** Hardware volume button press observed via `AudioVolumeObserver`.
- **Data Source:** ContentObserver on `Settings.System.VOLUME_MUSIC` / `VOLUME_RING`.
- **Compact Appearance:** Speaker / Bell glyph on leading edge, horizontal volume progress bar on trailing edge.
- **Expanded Appearance:** N/A (HUD is compact-only).
- **Supported Actions:** Passive transient indicator.
- **Available Animations:** Horizontal fill spring, level pulse.
- **Dismissal & Timeout:** Auto-dismisses after 2000ms (`IslandStateManager.postVolumeLevel:896`).
- **Playground Support:** Partially testable in code; **MISSING direct trigger in `PlaygroundCase.entries`**.
- **Implementation Parity:** Shared renderer; missing UI trigger.
- **Status:** IMPLEMENTED in production; MISSING direct trigger in Playground.

---

### 12. Bluetooth Accessory Connectivity
- **Production IDs:** `PROD-22` (Connected), `PROD-23` (Connecting)
- **Purpose:** AirPods / Bluetooth headphones connection alert showing device name and battery percentage.
- **Source File & Class/Function:** [`IslandState.BluetoothDevice`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L144); `DynamicIslandPill.kt:1130-1135`.
- **Triggering Event:** `BluetoothDevice.ACTION_ACL_CONNECTED` or `BluetoothProfile.ACTION_CONNECTION_STATE_CHANGED`.
- **Data Source:** Android BluetoothAdapter.
- **Compact Appearance:** Headphones glyph on leading edge; device name (e.g. "AirPods Pro") + battery level on trailing edge.
- **Expanded Appearance:** N/A.
- **Supported Actions:** None.
- **Available Animations:** Icon rotation / scale pop.
- **Dismissal & Timeout:** Auto-dismisses after 3000ms (`IslandStateManager.postBluetoothDevice:938`).
- **Playground Support:** Partially testable in code; **MISSING direct trigger in `PlaygroundCase.entries`**.
- **Implementation Parity:** Shared renderer; missing UI trigger.
- **Status:** IMPLEMENTED in production; MISSING direct trigger in Playground.

---

### 13. Quick Controls (Action Island)
- **Production ID:** `PROD-24`
- **Purpose:** Apple Control Center-inspired system utility grid providing one-touch access to Wi-Fi, Bluetooth, Flashlight, Hotspot, Sound mode, volume/brightness sliders, and app/contact shortcuts.
- **Source File & Class/Function:** [`IslandState.ActionControl`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L151); [`ActionControlIsland.kt:52`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/ActionControlIsland.kt#L52); `DynamicIslandPill.kt:1136`.
- **Triggering Event:** Deliberate user long-press on any compact island (`IslandInteractionPolicy.mainLongPress`).
- **Data Source:** [`ActionIslandRepository`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/repository/ActionIslandRepository.kt) & [`ActionSystemController`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/service/ActionSystemController.kt).
- **Compact Appearance:** N/A (Always expanded presentation mode).
- **Expanded Appearance:** Glassmorphic grid: 4 system toggle tiles (Wi-Fi, BT, Flashlight, Ring/Vibrate), live Brightness and Media Volume sliders, pinned App shortcut chips, quick Contact chips, and active notification badge counter.
- **Supported Actions:** Tile toggles (`onSystemTileClick`), volume/brightness drags, shortcut launches, outside tap collapse.
- **Available Animations:** Large sheet spring expansion, tile press compression, slider fill animation.
- **Dismissal & Timeout:** Sticky / User-Owned (No auto-timeout; dismisses only via outside tap, swipe up, or explicit close).
- **Playground Support:** Testable via `PlaygroundCase.Controls`.
- **Implementation Parity:** Shared renderer; mock tiles update local feedback.
- **Status:** IMPLEMENTED (Actual).

---

### 14. Pro Access Expiry Island
- **Production ID:** `PROD-25`
- **Purpose:** Compliant, non-intrusive paywall reminder and rewarded pass renewal interface.
- **Source File & Class/Function:** [`IslandState.PremiumExpiry`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/data/model/IslandState.kt#L156); [`ActionControlIsland.kt:469`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/main/java/ai/emots/kishan_dynamic/ui/components/ActionControlIsland.kt#L469); `DynamicIslandPill.kt:1154`.
- **Triggering Event:** Trial or rewarded pass expiry (`EntitlementManager`).
- **Data Source:** `EntitlementManager` & `PremiumExpiryPolicy`.
- **Compact Appearance:** N/A.
- **Expanded Appearance:** Obsidian card with crown icon, "Aurora Pro access" title, hours remaining copy, "Watch Ads" (+24h) secondary button, and "Buy Premium" primary CTA.
- **Supported Actions:** `PremiumWatchAd` (`IslandControlAction.PremiumWatchAd`), `PremiumBuy` (`IslandControlAction.PremiumBuy`).
- **Available Animations:** Subtle glow pulse, spring expansion.
- **Dismissal & Timeout:** Dismisses on button click, outside tap, or entitlement refresh.
- **Playground Support:** Testable via `PlaygroundCase.Access`.
- **Implementation Parity:** Shared renderer; mock buttons.
- **Status:** IMPLEMENTED (Actual).

---

### 15–33. Live Activity Family (`IslandState.LiveActivity` with `LiveActivityKind`)

All live activities are managed via `IslandState.LiveActivity` containing `LiveActivityInfo` and discriminated by `LiveActivityKind`.

| Production ID | Live Activity Kind | Sub-Type & Source File | Compact UI | Expanded UI | Playground Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PROD-26** | **TIMER** (Compact) | `LiveActivityKind.TIMER` / `DynamicIslandPill:1168` | Orange timer ring + "03:35" | N/A | Testable (`PlaygroundCase.Timer` collapsed) |
| **PROD-27** | **TIMER** (Expanded) | `LiveActivityKind.TIMER` / `DynamicIslandPill:1169` | N/A | Expanded timer with progress ring, pause/start, reset (85.73dp) | Testable (`PlaygroundCase.Timer`) |
| **PROD-28** | **DELIVERY** (Compact) | `LiveActivityKind.DELIVERY` / `DynamicIslandPill:1194`| Orange bag glyph + ETA ("22 min") | N/A | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-29** | **DELIVERY** (Expanded) | `LiveActivityKind.DELIVERY` / `DynamicIslandPill:1195`| N/A | Restaurant, status, progress bar, Call Partner button | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-30** | **FLIGHT** (Compact) | `LiveActivityKind.FLIGHT` / `DynamicIslandPill:1200` | Cyan airplane glyph + "AA 142" | N/A | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-31** | **FLIGHT** (Expanded) | `LiveActivityKind.FLIGHT` / `DynamicIslandPill:1201` | N/A | Flight route tracker, gate, "ON TIME" status badge | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-32** | **SPORTS** (Compact) | `LiveActivityKind.SPORTS` / `DynamicIslandPill:1205` | Purple star glyph + "2 - 1" | N/A | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-33** | **SPORTS** (Expanded) | `LiveActivityKind.SPORTS` / `DynamicIslandPill:1206` | N/A | Teams, live minute ("74' LIVE"), score card (Rule 31: Zero Cricket) | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-34** | **NAVIGATION** (Compact) | `LiveActivityKind.NAVIGATION` / `DynamicIslandPill:1209`| Blue arrow glyph + "200 m" | N/A | Testable (`PlaygroundCase.Navigation` collapsed) |
| **PROD-35** | **NAVIGATION** (Expanded)| `LiveActivityKind.NAVIGATION` / `DynamicIslandPill:1233`| N/A | Direction maneuver arrow, next street, remaining ETA | Testable (`PlaygroundCase.Navigation`) |
| **PROD-36** | **VOICE_MEMO** | `LiveActivityKind.VOICE_MEMO` / `DynamicIslandPill:1170` | Red microphone dot / waveform | Red waveform visualizer, elapsed time, Stop button | Testable (`PlaygroundCase.Recording`) |
| **PROD-37** | **SCREEN_RECORDING** | `LiveActivityKind.SCREEN_RECORDING` / `DynamicIslandPill:1171`| Red recording dot + timer | Red recording dot, elapsed timer, Stop button | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-38** | **SHORTCUT** | `LiveActivityKind.SHORTCUT` / `DynamicIslandPill:1172` | Cyan sparkle glyph | Shortcut title, animated progress spinner, checkmark | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-39** | **FOCUS_MODE** | `LiveActivityKind.FOCUS_MODE` / `DynamicIslandPill:1173`| Purple crescent moon | Focus mode banner: "Do Not Disturb · On" | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-40** | **AIR_DROP** | `LiveActivityKind.AIR_DROP` / `DynamicIslandPill:1174` | Cyan AirDrop waves glyph + % | File transfer card, filename, progress bar, cancel | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-41** | **AIRPODS** | `LiveActivityKind.AIRPODS` / `DynamicIslandPill:1175` | White AirPods glyph + battery ring | AirPods connected card, L/R/Case battery breakdown | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-42** | **SATELLITE** | `LiveActivityKind.SATELLITE` / `DynamicIslandPill:1181` | Green satellite dish glyph | Radar alignment guidance, "CONNECTED Keep pointing at Satellite" | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-43** | **FIND_MY** | `LiveActivityKind.FIND_MY` / `DynamicIslandPill:1186` | Green beacon pulse glyph | "Find My iPhone Alert" glowing pulse card | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-44** | **MOVED_TO_IPHONE** | `LiveActivityKind.MOVED_TO_IPHONE` / `DynamicIslandPill:1190`| Cyan audio route glyph | "AirPods Moved to iPhone" alert with Undo button | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-45** | **VIDEO_REMOTE** | `LiveActivityKind.VIDEO_REMOTE` / `DynamicIslandPill:1054`| Media glyph + 15s skip | Video scrub card with 15-second skip backward/forward | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-46** | **AIRPLANE_ALERT** | `LiveActivityKind.AIRPLANE_ALERT` / `DynamicIslandPill:1218`| Orange airplane glyph | "Turn Off Airplane Mode" prompt card | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-47** | **SCREEN_MIRRORING_ALERT**| `LiveActivityKind.SCREEN_MIRRORING_ALERT` / `DynamicIslandPill:1220`| Cyan screen mirroring glyph | "Screen Mirroring MacBook Pro" with Stop Casting button | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-48** | **MOBILE_DATA_ALERT** | `LiveActivityKind.MOBILE_DATA_ALERT` / `DynamicIslandPill:1226`| Green cellular tower glyph | "Mobile Data Required to use Wi-Fi" alert with Settings CTA | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-49** | **TRANSIT_ROUTE_ALERT** | `LiveActivityKind.TRANSIT_ROUTE_ALERT` / `DynamicIslandPill:1228`| Red train glyph | "Prague Main Train Station" transit arrival card | **MISSING direct trigger in `PlaygroundCase`** |
| **PROD-50** | **PROGRESS_DOWNLOAD** | `NotificationProgressCard` / `NotificationIsland.kt:873` | Progress bar + % on compact | Download card with file progress, speed, and cancel | Testable (`PlaygroundCase.Success`) |
