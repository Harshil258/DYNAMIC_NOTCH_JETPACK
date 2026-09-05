# Reference island use-case audit

Reference audited: `/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14`

This document records the behavior found in the reference app. It is a use-case inventory, not a visual specification. Our `AppTheme`, UI tokens, typography, gradients, spacing, and audited Dynamic Island geometry remain the implementation source of truth.

## Confirmed island states

| State | Use case | Presentation and interaction |
| --- | --- | --- |
| Minimal / Hidden | Resting cutout and feature-disabled state | Minimal is the resting island; Hidden removes the overlay. |
| Music | Active media playback | Compact track/art presentation; tap expands to album art, progress, transport controls, and AirPlay. |
| OngoingCall | Active or dialing phone call | Compact caller + duration state; expanded controls include mute, speaker, and end-call actions. |
| IncomingCall | Incoming phone call | Expanded caller identity with accept and decline actions. |
| Notification | One or more notifications | Compact app/priority indicators; expanded pager/card presentation with notification actions and optional progress/chronometer content. |
| NotificationWithMusic | Notifications while media is available | Split state preserves both notification access and music access; the primary/side surface opens the related experience. |
| Charging | Charger connected | Transient battery/charging state with percentage and charging animation; low battery is visually distinguished. |
| RingerMode | Normal, vibrate, or silent changed | Compact mode-change confirmation; tap expands to a mute/unmute action. |
| RingerVolume | Ringer volume changed | Transient bell indicator with level slider/value. |
| MediaVolume | Media volume changed | Transient speaker indicator with level slider/value. |
| BluetoothDevice | Bluetooth device connected | Transient device name, connection status, and optional battery level. |
| ActionControl | Quick controls | Expanded control surface for Wi-Fi, Bluetooth, mobile data, airplane mode, torch, focus/DND, brightness, volume, app shortcuts, and favorite contacts. |
| PremiumExpiry | Premium access is expiring | Expanded conversion state with watch-ad and buy-premium actions. |

## Confirmed system and service triggers

- Notification access: incoming, removed, grouped, priority, progress, chronometer, media, and call notifications.
- Accessibility/service events: tap, long press, outside touch, and swipe-to-dismiss behavior.
- Audio events: ringer mode, ringer volume, and media volume changes.
- Optional Android call-screening role: caller identity capture when the legacy phone-state broadcast does not include a number.
- Battery events: charger connected/disconnected and low-battery presentation.
- Bluetooth events: device connection with device metadata and battery when available.
- Quick-control actions: toggles, brightness, volume, app launch, and contact call shortcuts.

## Reference interaction rules to preserve as behavior

- Calls take priority over ordinary notifications.
- Music and notifications can coexist instead of replacing each other.
- Music and phone presentation preferences are persisted and applied by the live producers, not only by their preview screens.
- Transient states auto-dismiss and restore the highest-priority active base state.
- Taps expand the current supported state; long press opens quick controls; outside touch collapses/dismisses where appropriate.
- Each action is represented as an explicit state transition rather than a visual-only change.

## Scope boundary

The reference repository does not provide separate production states for delivery, sports, flight, navigation, voice memo, screen recording, AirDrop, AirPods, satellite, Find My, or other Apple sample activities. They are not counted as reference-app use cases in this audit. Our Live tab exposes the broader catalog as explicitly labelled app-owned previews, and the implementation now adds Android producers or strict notification-backed adapters where the platform exposes a trustworthy source. Find My and handoff remain intentionally source-specific gaps rather than fabricated universal integrations.
