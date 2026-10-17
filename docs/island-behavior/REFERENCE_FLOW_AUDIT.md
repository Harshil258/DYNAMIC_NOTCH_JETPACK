# Reference flow audit — 11 October 2026

Reference: `/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14`.

## Evidence and adaptation

The reference routes Android notifications and MediaSession callbacks through
`NotificationService`, then broadcasts into `AccessibilityService`. Its sealed
`IslandState` drives `DynamicNotchParent`; `SystemStateManager` supplies quick
controls. We retain our direct listener, reducer, service and existing visual
language rather than transplanting its broadcast architecture or branding.

The reference's `Notification.expandedByUser`, `triggerAutoExpansion`, and
`UserExperiencePolicy` distinguish a timed source-driven peek from deliberate
interaction. Our previous `isExpanded` flag represented both, so expiry treated
automatic peeks as user-owned. The model now records ownership separately.

## Implemented behavior

- Automatic notification peeks close after the configured display duration.
  They collapse first; clearable ordinary notifications subsequently expire from
  the island queue, not from Android's notification shade.
- Ongoing/non-clearable sources collapse their automatic peek but remain queued.
- Explicit notification opening, page selection and reply/action interaction
  hold the notification open. New arrivals preserve the selected notification.
- Collapsing via the expansion toggle schedules expiry after publishing the
  collapsed state, avoiding an early return against the previous expanded state.
- Media progress advances from MediaSession's monotonic timestamp and playback
  speed, stops while paused, and clamps to the track duration.
- Empty/destroyed media sessions remove stale music presentation.
- Reduced-motion preferences reach the main application as well as the overlay;
  root navigation does not animate when reduced motion is enabled.
- Recognized ongoing navigation, recording and other mapped live sources are
  classified before the generic foreground-service noise filter.

## Verification and remaining gates

Pure JVM regression tests cover media time extrapolation, pause, missing
timestamps, invalid speed, duration bounds, and automatic-preview ownership.
Existing tests continue to cover reducers, priority, privacy and interactions.

Verified on 11 October: 162 JVM tests passed with no skips/failures/errors;
`:app:assembleDebug` and `:app:lintDebug` completed successfully. Device
acceptance has not been performed for this change set.

This is a targeted source audit, not proof that every reference source or every
device scenario has been validated. Remaining research includes the full call,
cricket, system-control, onboarding and settings flows. Real-device acceptance
must exercise notification bursts, replies during a peek, ongoing source
removal, media session replacement, DND/lock-screen privacy, and reduced-motion
animation paths. No reference signing keys or credentials are copied.
