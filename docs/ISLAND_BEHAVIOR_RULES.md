# Island behavior contract

Read together with UI_UX_RULES.md and DYNAMIC_ISLAND_RULEBOOK.md. This is the
behavioral authority; visual reference dimensions remain subject to actual
Android window constraints. Changes require regression tests, not package-name
exceptions or screenshot-only offsets.

## Intake and ownership

- Android notification keys identify sources. Content, titles, indices and app
  names never identify a conversation or action.
- Blocked sources are excluded. Silent sources do not interrupt. An ongoing flag
  alone is not a live activity: require actual progress, a running chronometer,
  supported live metadata or a recognized activity capability.
- Progress extras containing zero/zero are not progress. Completed progress is
  a temporary completion event, not a permanent activity.
- Listener reconnection restores genuine ongoing work, not historical transient
  messages from the shade. Source removal invalidates suspended ingestion work.
- Filtering/queue eviction must not cancel the original Android notification.
- One coordinator owns presentation, interaction ownership, priority and restore.
  Presentation phases are derived from its state, never a second mutable owner.

## State and interaction

Idle → eligible transient → expiry → idle or surviving ongoing work.
Ongoing compact → user expansion → collapse → ongoing compact.
User-owned controls, swipes and reply editors resist unrelated arrivals; critical
call policy may interrupt and then restores surviving underlying work.

- Duplicate snapshots do not re-expand, reset deadlines or resurrect expired
  presentations. Deadline accounting is monotonic and bounded.
- Compact notifications show an icon and count/progress, not permanent names.
  Details and actual available actions belong in expanded content.
- Main/companion interactions address their own source. Do not turn an idle pill
  into a fake two-activity presentation. Keep existing configured tap/long-press
  behavior; do not launch an arbitrary application when a source action is absent.
- Pager identity, selection, measurements and drafts follow stable source IDs.
  New quiet sources must not reset the page being read. Removing the selected
  source selects a surviving neighbor; stale page callbacks are ignored.
- Only render source-provided actions or supported media capabilities. Reply
  needs free-form RemoteInput. Successful dispatch means handed off, not delivered.
  Failure retains the draft; action replacement/removal invalidates stale controls.

## Geometry, motion and privacy

- All families use shared shell tokens and width constraints. Clamp expanded
  content to the current window and compact content to a status-side budget.
  OEM icon positions are not exposed reliably: validate on actual devices.
- Motion must be interruptible and respect reduced motion. Content updates are
  not new presentations. Protected surfaces hide immediately, without waiting
  for an exit animation; hidden windows must not retain visible buffers or touches.
- Public accessibility window type/package metadata may suppress the overlay
  over focused System UI/permission surfaces. Do not inspect private view IDs,
  notification copy or hidden shade APIs. Unknown OEM behavior is an acceptance
  gate, not a universal compatibility claim.
- Lock-screen policy must redact private content and remove reply focus. Never
  bypass Android authentication to perform tests.
- Diagnostics may include hashed source IDs, package, normalized category,
  ranking, flags, decision and lifecycle. Never log bodies, sender names, OTPs,
  raw keys/group keys, reply drafts or PendingIntent contents.

## Verification

Run targeted policy/reducer tests before a batched incremental build. For each
acceptance case label JVM fixture, actual device, real source app, or pending.
Pure tests do not prove gestures, framework exceptions, animation frame timing,
TalkBack, 200% fonts, process death or external delivery. Keep pending cases in
Claude Mem and the requirement audit; do not mark them passed by inference.
