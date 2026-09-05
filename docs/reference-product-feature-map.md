# Reference product feature map

Reference audited: `/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14`

This is a product and behavior audit only. It is intentionally not a code or architecture transplant. Our implementation should use independent domain models, repositories, state reducers, and Compose UI components.

## 1. Action Island / Quick Controls

The reference calls the expanded control surface **Action Control**. It is the most important feature beyond the basic notification island.

### Control types

- System toggles: mobile data, Wi-Fi, Bluetooth, dark mode, torch, airplane mode, Do Not Disturb, rotation lock, hotspot, location, screenshot, auto-brightness, power saver, sync, and NFC.
- Installed-app shortcuts: discover launchable apps from the device, search them, add/remove them, enable/disable them, and reorder them.
- Favorite contacts: read contacts, choose favorites, display contact photo/name, reorder them, and call them from the island.
- Custom actions: a label, icon, and action identifier for future app-defined shortcuts.
- Notification entry point: show a badge/count and open the notification surface.
- Brightness and volume sliders.
- Utility actions: lock, camera, settings, edit, and app/contact launch.

### Action Island editor behavior

- Preview the island with the current selection before saving.
- Separate app shortcuts and favorite contacts into editable sections.
- Search installed apps.
- Preserve order and selection across launches.
- Handle missing/uninstalled packages without crashing.
- Request contacts permission only when the contact editor is opened.
- Gate premium-only customization through a clear upgrade state.

## 2. Island experiences

- Resting/minimal and hidden states.
- Music playback: compact art/title/equalizer, expanded art/progress/previous/play-next/AirPlay.
- Incoming call: caller identity, accept, decline.
- Ongoing/dialing call: caller, duration, mute, speaker, end call, and call action intents.
- Notifications: compact app/priority indicators, expanded pager/cards, app icon, title, body, image, progress, chronometer, and notification actions.
- Notification + music coexistence: each surface retains its own destination.
- Charging and low battery: transient percentage/charging state with return to the active base state.
- Ringer mode: normal, vibrate, and silent confirmation with expanded mute/unmute behavior.
- Ringer and media volume HUDs with animated level feedback.
- Bluetooth connection: device name, connection state, and battery when available.
- Premium expiry/upsell state.

Our current Playground additionally contains Apple-style sample activities such as delivery, flight, sports, navigation, voice memo, screen recording, AirDrop, AirPods, satellite, Find My, screen mirroring, and data alerts. Those are a separate prototype catalog, not confirmed production state families in the reference app.

## 3. State, priority, and gesture rules

- Calls have higher priority than ordinary notifications.
- Music remains available when notifications arrive; the state reducer composes them rather than dropping one.
- Transient system states auto-dismiss and restore the highest-priority active state.
- Tap expands or opens the active experience.
- Long press opens Action Island / Quick Controls.
- Outside touch collapses or dismisses according to the active state.
- Swipe-to-dismiss is supported for dismissible states.
- State changes animate through a spring/morph transition with content cross-fade.
- All actions should be explicit events with testable transitions, not click-only visual mutations.

## 4. Notification and media ingestion

- Notification listener integration.
- Grouping and removal reconciliation.
- Priority notification classification for navigation, transport, calls, and similar ongoing experiences.
- Progress and indeterminate-progress notifications.
- Chronometer/timer notifications.
- Media-style notification extraction, playback state, position, duration, and album art.
- Notification action execution and reply/action-intent preservation.

## 5. Call experience

- Phone-state detection for ringing, outgoing/dialing, connected, and ended calls.
- Optional call-screening integration.
- Caller identity and contact photo lookup.
- Call duration tracking.
- Call history.
- Post-call summary with duration, timestamps, contact actions, and add/edit contact shortcuts.

## 6. App shell and setup

- Splash and onboarding.
- Accessibility-service setup.
- Notification-listener setup.
- Optional phone/call and contacts permissions.
- Preview/catalog screen.
- Home/dashboard screen.
- Network-aware startup/release recovery when configured remote services are unavailable.
- Settings areas for display, notifications, music, calls, battery, sound, theme, language, and behavior.
- Dark, light, and system theme modes.
- Multi-language support.
- About, feedback, rating, privacy, and app-sharing surfaces.

## 7. Monetization and operational concerns

- Premium entitlement and feature gating.
- Premium screen and restore-purchases path.
- Premium expiry surface on the island.
- Rewarded-ad unlock path and ad-free state.
- Remote configuration for premium and ads.
- Analytics events for state display, expansion, dismissal, feature gates, and settings changes.

## Our implementation order

1. Action Island domain model, persistence, installed-app discovery, editor, and overlay rendering.
2. Favorite contacts with permission-safe repository and call action.
3. System toggle command layer and brightness/volume controls.
4. Notification reducer: grouping, priority, actions, progress, and chronometer.
5. Media session ingestion and real playback controls.
6. Call lifecycle, action intents, history, and post-call summary.
7. Remaining transient system states and premium/setup flows.

## Current implementation status

- [x] Action Island persistence, installed-app discovery, favorite contacts, custom action presets, per-shortcut enable/disable, ordering, stale-package reconciliation, long-press entry, notification badge and entry point, preview, brightness, media-volume controls, lock/settings/camera utilities, direct editor deep link, screenshot action, and the full reference system-tile catalog.
- [x] Action Island system chips expose a live device snapshot, execute supported torch/DND/Bluetooth/sync/Wi-Fi/rotation-lock/auto-brightness changes where Android permits direct mutation, and route restricted controls to the narrowest available Android settings panel.
- [x] Action Island system-chip snapshots refresh on a lifecycle-scoped cadence and stop when the overlay service is destroyed, keeping external device changes visible without UI-owned system logic.
- [x] Action Island distinguishes active, inactive, and unreadable/action-only system tiles with a visible setup marker; Wi-Fi reflects radio-enabled state rather than only current connectivity, and restricted Sync changes fall back to the platform settings surface instead of throwing or claiming a false state.
- [x] Action Island’s Android integration contract declares launcher discovery visibility and optional camera, call, DND, NFC, sync, location, Wi-Fi, and settings capabilities required by its command boundary; unsupported hardware remains optional.
- [x] Permission setup reflects actual accessibility, notification-listener, phone/call, contacts, and battery-optimization state, with lifecycle refresh after returning from Android settings; favorite contacts place a direct call only when `CALL_PHONE` is granted and otherwise fall back to the dialer.
- [x] Feedback matches the reference support flow: a 1–5 rating and non-empty comment are required, comments are capped at 500 characters, device context is attached locally, and the sheet only closes after an email composer opens successfully; unavailable mail clients remain visible as an actionable error.
- [x] Branded startup splash renders while persisted routing is resolved; first-run onboarding is then selected from persisted state, records completion, and routes new users into the permission hub before the main app; completing the required permission hub also persists the reference-compatible `setup_done` milestone separately.
- [x] First-run language selection is persisted independently and appears before onboarding on a fresh install; completed onboarding installations do not replay it.
- [x] Persisted language now propagates through a shared Compose localization boundary and translates the core shell/settings vocabulary for the reference locale set (Arabic, German, Spanish, French, Hindi, Japanese, Korean, Portuguese, and Chinese), plus Gujarati; untranslated locales and system-provided names remain explicit English fallbacks instead of pretending to be fully localized.
- [x] Incoming-call accept/decline and ongoing-call end, mute, and speaker actions use a dedicated Telecom/audio command boundary and only update island state after a real phone command succeeds; generic phone calls do not expose unsupported video/SharePlay controls, which remain preview-only.
- [x] CallStyle/VoIP incoming notifications are promoted into the incoming-call island with preserved answer/decline PendingIntents; native telephony calls continue through the Telecom boundary.
- [x] Compact ongoing calls now use the detached companion bubble: the main capsule carries the live call timer and the side bubble carries the green call identity.
- [x] Safe system command boundary for Android settings and app/contact launch, including restricted settings routed to their narrowest available Android panels.
- [x] Expanded music output and alert settings controls are connected to platform-owned Android destination/settings surfaces; app-owned stop and acknowledge controls clear their active live-activity state.
- [x] Notification reducer with priority, grouping, InboxStyle conversation lines, custom Clock timer/stopwatch surfaces, removal, expiry, actions, progress, chronometer, and media coexistence; notification display duration uses the reference-compatible 2s/3s/5s/8s/10s choices, keeps 3s/5s free, gates 2s/8s/10s behind Pro, and sanitizes legacy values.
- [x] Notification-with-music split state preserves the full pending queue, keeps notification/music in separate main and detached surfaces, uses album-art/waveform while playing, and exposes the notification count or album art when paused.
- [x] Multi-notification queues now have a dedicated stacked compact island: the primary alert stays readable in the main capsule while the detached bubble exposes the queue count or secondary priority alert.
- [x] Single priority notifications (timer, navigation, transport, call, alarm, stopwatch, or ongoing work) use the same split compact presentation as the reference instead of collapsing into a plain notification pill.
- [x] Expanded notification pager with reducer-backed selection, dismissal, adaptive rendering of every notification action, action execution, and app-opening fallback.
- [x] Notification chronometers preserve monotonic base/countdown metadata at the listener boundary and render live elapsed/countdown values in expanded and stacked island presentations.
- [x] Media-style notification parsing with track, playback, position, duration, artist, and album-art metadata, plus active-session play/pause/skip commands.
- [x] Compact music now uses a dedicated companion-bubble presentation: the main capsule keeps track/equalizer content while the detached bubble carries the live equalizer or paused album-art identity.
- [x] Media session metadata and playback callbacks continuously update the music island after the original media notification, with controller callbacks unregistered when the session is replaced or removed.
- [x] Expanded music seeking is connected to MediaController.seekTo, and source position snapshots drive progress instead of a UI-only fabricated timer.
- [x] Music settings now persist "show island for music" and "scrubbable progress bar"; the notification/media-session producer suppresses media surfaces when disabled and removes the seek affordance when scrubbing is off.
- [x] Music settings also persist compact-controls mode; the live overlay keeps active music in its compact presentation and disables expansion while that mode is enabled.
- [x] Compact music controls are premium-gated at the settings interaction and overlay runtime; stale persisted values cannot unlock the mode after Pro expires.
- [x] The reference premium feature matrix is represented by app-owned capability gates for theme selection, sound/haptic customization, call-end summary suppression, notification duration, compact music controls, Action Island limits, and horizontal offset; locked controls show our PRO treatment and route to our paywall.
- [x] Premium-sensitive runtime settings fail safely after expiry: free users retain baseline sound/call behavior, theme falls back to System, and horizontal offset/custom pulse settings cannot remain active from stale persisted values.
- [x] Phone lifecycle, contact lookup, live call timer, device-local call history, and real-record summary/history screens.
- [x] Outgoing call dialing is modeled separately from connected calls; call-style notification copy reconciles the dialing-to-connected transition and the island shows distinct Calling/connected labels.
- [x] Android CallStyle/VoIP incoming and ongoing notifications now own their corresponding island phases through preserved Answer/Decline/Hang up actions; source removal and incoming-to-ongoing updates release the source-owned state cleanly.
- [x] Call settings now persist banner, live timer, and post-call summary preferences; history remains available even when its visual island surface is disabled.
- [x] Optional Android call-screening role bridge improves caller identity when PHONE_STATE omits the number, while always allowing calls through unchanged.
- [x] Support/about actions are functional: rating and update actions open the platform store with a web fallback, sharing opens the platform chooser, and privacy and terms open themed in-app documents.
- [x] A dedicated About surface now exposes live premium status, device/Android diagnostics, app version code/name, active theme, and reference-style rate/share/feedback actions through our shared token UI.
- [x] A dedicated Aurora Pro surface now exposes live entitlement status, reference-style benefits and plans, Google Play purchase/restore actions, rewarded-pass access, and gradient/token styling; deep links, Settings membership, Quick Control limits, and feature gates route to it without copying the reference implementation.
- [x] Rating now has a reference-compatible local cadence: it records one app open per process, waits for the third open plus the home-screen dwell, persists rated/dismissed state, applies a three-day dismissal cooldown and three-dismissal cap, and is remotely disableable.
- [x] The app-owned component gallery is reachable from Settings and provides an interactive catalog of our shared buttons, rows, selections, tiles, sliders, progress, status pills, palette, typography, and light/dark tokens, plus independent dialog, sheet, control-center, tab, glass, luminance, blur, magnifier, scroll, and lazy-scroll playgrounds inspired by the reference catalog.
- [x] Root-screen exit follows the reference confirmation workflow, while nested settings screens still use direct back navigation.
- [x] Display behavior settings persist always-on-top availability, lock-screen visibility, and smooth/fast overlay animation choice; the overlay service consumes them at runtime.
- [x] Theme settings have a dedicated preview-and-selection screen for System, Light, and Dark modes; changes remain persisted through the shared token theme.
- [x] Display calibration exposes persisted horizontal offset alongside vertical offset and width scaling, with an immediate preview and reset-to-default path.
- [x] Battery settings now persist charging-animation and low-battery alert preferences; the system receiver suppresses disabled transient battery surfaces.
- [x] Charging and low-battery compact states now use live battery percentage data in a split presentation, with tokenized battery content in the main capsule and an animated progress ring in the detached bubble.
- [x] Battery settings include a master notification switch in addition to charging-animation and low-battery controls; the receiver gates both battery event families through it.
- [x] Sound settings now expose persistent ringer, mute, vibrate, volume-HUD, pulse-scale, and pulse-duration controls; the live ringer/volume producers honor them.
- [x] Haptic feedback is persisted and applied to shared island tap, long-press, and swipe-dismiss gestures.
- [x] Ended calls now publish a transient real-record summary island with redial, message, and dismiss actions before restoring the highest-priority base state.
- [x] Call history, summary, and Action Island contact shortcuts share a platform call boundary: `ACTION_CALL` is used only with `CALL_PHONE`, otherwise all entry points fall back to the Android dialer; messaging, contact-save, and clipboard destinations remain available.
- [x] Typed live-activity registry for the prototype activity families already represented by our UI catalog, with app-owned timer, delivery, navigation, sports, and flight producers (including progress, pause/reset, completion, and cleanup).
- [x] Timer pause/resume/cancel is shared between the expanded island, Live tab, and timer card through coordinator state; the previous visual-only timer control path is removed.
- [x] The Live tab exposes the remaining reference-catalog families as typed app-owned previews: voice memo, screen recording, shortcut, focus, AirDrop, AirPods, satellite, Find My, handoff, video remote, airplane, mirroring, mobile-data, and transit alerts.
- [x] Ongoing Android navigation and delivery notifications are mapped through a pure source adapter into Navigation/Delivery live-activity states, and removed notifications clear their corresponding activity.
- [x] Known transit notifications from supported navigation/transit packages are mapped into the Transit live-activity state with source progress and removal cleanup.
- [x] Known flight and sports notifications are mapped through strict package-plus-copy adapters into live Flight/Sports states with source progress and live payload rendering.
- [x] Ongoing screen-recording notifications are mapped into the Screen Recording live-activity state through the same source adapter.
- [x] Known Android voice-recorder notifications are mapped into the Voice Memo live-activity state only when the source package and ongoing recording copy both match.
- [x] Explicit Nearby/Quick Share notifications are mapped into the AirDrop-style transfer activity with source progress when available.
- [x] Strict notification-backed adapters cover optional Satellite, Find My, handoff/Continue-on-phone, and screen-mirroring surfaces when a known system/source package emits matching ongoing copy; native platform producers remain separate because Android exposes no universal API for these states.
- [x] Notification-backed live activities retain their source notification id and a safe Open/Undo/Continue action id, allowing the island to dispatch the originating PendingIntent without exposing platform objects to Compose.
- [x] Android MediaRouter live-video route monitoring promotes a selected external video destination into the screen-mirroring activity and clears it on route removal or service shutdown.
- [x] Live optional-surface payloads reach the expanded island renderer, so route/device/source copy replaces catalog-only placeholder labels while previews retain their reference-style defaults; Video Remote controls seek/play through the active media session and Shortcut Open routes to the app-owned Live surface when no source app intent exists.
- [x] Android interruption-filter broadcasts drive a Focus Mode live-activity state and clear it when Do Not Disturb is turned off.
- [x] Android system polling drives authoritative Airplane Mode and mobile-data-off alerts with edge-triggered live-activity updates and source-specific cleanup.
- [x] Notification-backed activities rehydrate from active notifications after listener reconnects and clear their source IDs when the listener disconnects or a notification is removed.
- [x] Recognized notification-backed activities now own the visible island surface while retaining their source notification in the queue; users can still open the underlying notification explicitly.
- [x] Live activities are registry-backed by stable source ID, with per-source expiry, source-specific cleanup, and deterministic priority when multiple activities coexist.
- [x] Charging/low-battery lifecycle with connect, disconnect, low-battery, recovery, auto-dismiss, and base-state restoration; battery-owned cleanup does not dismiss an unrelated ringer or volume HUD.
- [x] Ringer-mode expansion with state-driven Normal/Vibrate/Silent content and a real mute/unmute system action.
- [x] Compact ringer mode states use a detached mode-specific companion icon for silent, vibrate, and normal states; compact and expanded presentations also share mode-aware iconography and mute/unmute semantics.
- [x] Bluetooth connection lifecycle with connecting/connected/disconnected profile events, real device-name rendering, optional battery metadata, and disconnect restoration.
- [x] Known AirPods audio-profile connections are promoted to a typed live activity with device name and permission-safe battery progress; other Bluetooth devices retain the generic banner.
- [x] Ringer and media volume HUDs render the actual observed level instead of fixed preview values.
- [x] Notification compact, split, and expanded surfaces render the originating app icon with a safe fallback, and expanded content resolves rich big-text, subtext, summary, and conversation-title fallbacks.
- [x] Notification large artwork is cached from supported notification extras and shown in expanded cards with an app-icon fallback.
- [x] Notification reply actions preserve RemoteInput targets and open a focused reply UI that sends the filled action intent.
- [x] Expiring premium pass state and premium-expiry island event with live remaining-hour text, rewarded-pass deep link, and premium purchase deep link from the island actions.
- [x] Premium shortcut limits and upgrade path in the Action Island editor.
- [x] Tap/long-press/swipe-up island gesture paths, with the swipe preference respected by the overlay.
- [x] Android 16's optional SatelliteManager is bridged through a narrow, failure-safe adapter into the Satellite live-activity state; older devices remain silent and retain the notification-backed fallback.
- [ ] Platform-specific producers for Find My and handoff still need source-specific Android APIs; mirroring now has a MediaRouter producer plus a notification fallback. Timer, delivery, navigation, transit, sports, flight, AirDrop, AirPods, Airplane Mode, and mobile-data alerts now have live producers or notification-backed adapters.
- [x] Billing and rewarded-ad actions now terminate at an explicit entitlement-provider boundary; the default build reports unavailable instead of granting fake local Pro access. Restore-purchases is exposed in Settings.
- [x] Google Play Billing is now an independently injectable provider with configurable subscription/lifetime product IDs, purchase acknowledgement, restore queries, Activity-safe launch handling, and entitlement persistence only after Play reports a completed purchase; rewarded ads remain a separate provider.
- [x] Release runtime configuration and structured analytics contracts now exist with safe offline/no-op defaults; Play Billing and rewarded ads use the shared app analytics boundary, which can deliver events through an injectable HTTPS sink without coupling product behavior to telemetry availability.
- [x] Rewarded-pass delivery is now an independent Mobile Ads provider: debug uses Google's test app/placement, the UI handles loading/show/dismiss states, and a configurable entitlement (seven days by default, matching the reference product) is persisted only from the SDK reward callback.
- [x] Free-user adaptive banner surfaces now use an independent Mobile Ads gateway with explicit release/test placement IDs, Pro-aware suppression, loaded-state analytics, and a premium “Remove ads” CTA.
- [x] Free-user native ad cards now use an independent consent-aware loader, registered SDK assets, our gradient/card typography, AdChoices attribution, and the same premium CTA.
- [x] Configured ad surfaces now pass through an independent UMP consent gateway; unknown, failed, or denied consent fails closed and prevents banner/rewarded requests while preserving app functionality.
- [x] Interstitial ad parity now uses the shared release/consent boundaries, persisted screen-and-navigation thresholds, premium suppression, preload, and a non-blocking tab-transition gateway; failed or unavailable ads never block navigation.
- [x] App-open/resume ad parity now uses consent-gated preloading, four-hour freshness, cold-launch and permission-flow suppression, premium suppression, excluded lifecycle surfaces, and a shared full-screen overlap coordinator.
- [x] Remote release configuration now has an app-owned HTTP/JSON provider with injectable transport, bounded timeouts, schema-tolerant camelCase/snake_case decoding, last-good-snapshot retention, and empty-endpoint offline behavior.
- [x] Configured release endpoints now surface a token-driven animated connection-recovery dialog on refresh failure, with retry, Android network-settings routing, and an explicit continue-offline path; blank endpoints remain silent.
- [x] Update handling now has an app-owned remote decision boundary: configured release metadata can produce no-update, optional, or mandatory decisions, optional deferrals are persisted per version, checks are cadence-limited, offline/default builds remain silent, and both Settings and automatic refresh use the same gradient dialogs and Play Store/custom URL destination.
- [x] Background guardian parity now offers both battery-optimization and manufacturer-aware auto-start/startup settings, with resolvable-intent checks and app-details fallback.
- [ ] Release configuration still needs an external analytics sink, a deployed HTTPS endpoint with production AdMob app/placement IDs, and Play Console product/backend verification; release rewarded ads remain disabled until those values are supplied.
