# Notification engineering audit — 17 October 2026

## 1. Reference evidence

Reference APK: `/Volumes/Harshil Mac/SURYA_DEVELOPERS/SURYA_DYNAMIC_ISLAND/DynamicNotificationBar_VC14/app/build/outputs/apk/debug/app-debug.apk`.
Static `apkanalyzer` reports package `com.dynamicnotchnotificationbar.dynamicbar`,
version 1.14/code 14, min SDK 24, target SDK 36. SHA-256:
`0f8ac51f4f0191ef9f5a0932669185d6a1645ec610ccc39493cdff2f38d6219a`.

Decompiled `UserExperiencePolicy.shouldAutoExpand(ZZZZ)Z` confirms the guard:
enabled AND auto-expand AND no active call AND no user interaction. This is
static evidence, not observation of runtime notification behavior.

The corresponding reference source's notification pager uses stable source IDs
and adjacent-page precomposition. It also automatically scrolls to page zero
after content changes when not dragging/replying. Its deduplication first tries
partial Android ID + title before the exact key. We retain exact Android keys,
do not copy these title heuristics, and preserve selected content across updates.
Reference page heights are indexed by position; ours now follow notification ID.
No reference code/assets, signing keys or credentials were transplanted.

## 2. Existing architecture and targeted boundaries

Android callbacks → `IslandNotificationListener` (extras/actions/ranking) →
`NotificationContentResolver` (copy normalization) → notification domain policies
and `NotificationReducer` (source queue/selection/grouping/priority) →
`IslandStateManager` (presentation and restoration) → `IslandOverlayService`
(platform window/commands/privacy) → `NotificationIslandExpandedPager`.

Platform PendingIntents stay in `NotificationActionRegistry`, outside immutable
UI models. The new presentation, sensitivity, dispatch, pager-selection and
reply-lifecycle policies are pure and small. No DI framework, module, dependency
or speculative service rewrite is introduced. The listener/state manager remain
large orchestration files; this is not a claim of a complete architecture rewrite.

## 3. Defects fixed

| Priority | Root cause | Implemented behavior |
|---|---|---|
| P1 | Group-summary suppression matched group text without app identity. | Summary/child matching requires package AND group key. Different applications cannot suppress each other. |
| P1 | Reposts filled blank fields with old private copy/art. | Incoming full source snapshot is authoritative, including removed/redacted fields and actions. |
| P1 | Content updates selected another page. | Updates preserve selection; new quiet arrivals do not displace the highest-priority queued source. |
| P1 | Protecting every ongoing item defeated the queue limit. | Maximum 20 island items even during ongoing bursts; selected item protected, then highest-ordered ongoing items. Eviction affects only island presentation, not Android notifications. |
| P1 | Pager was indexed by position; page-change effects emitted during programmatic synchronization. | Stable ID keys; selection committed after user scrolling settles, by visible key rather than stale index; removed keys ignored. |
| P1 | Height cache reused another page's measurements. | Measurements keyed by ID and pruned on removal. No content-update animation to page zero. |
| P1 | App names/phrases such as “exit” or “your ride” selected specialized cards. | Pure capability-based classification: real progress, chronometer/timer, ongoing navigation, picture, generic fallback. Android `transport` is not treated as a taxi reservation. |
| P1 | Public OTP/payment copy could bypass user-enabled lock-screen masking. | Additional conservative sensitive-copy classification, while preserving platform sensitivity. No payment result is inferred or fabricated. |
| P1 | Reply editor closed regardless of dispatch result. | Close only after handoff; failure keeps draft/editor and gives feedback. Handoff is not delivery acknowledgement. |
| P1 | Any RemoteInput was presented as a free-text reply. | Text editor requires allowFreeFormInput; unsupported choice/data-only inputs are not rendered as supported controls. |
| P1 | Action index reused after action reorder could target another intent. | Versioned action IDs compare actual PendingIntent identity and slot; stale UI cannot address a replacement intent through a reused index. |
| P1 | Editor focus survived removal/action replacement/privacy redaction. | Pure reply lifecycle check closes invalid editors and restores non-focusable overlay flags. Draft uses saveable state within stable page identity. |
| P1 | Legacy notification priority ignored channel ranking. | Capture current ranking importance, suppress blocked importance, order by importance within priority class; fallback to legacy priority only if ranking unavailable. |

## 4. Multiple notifications and ongoing work

Same key updates one source; different keys remain separate even if sender/title
match. Child arrivals replace only their own app's summary. Summary-only sources
remain eligible until children exist. Removing another page preserves selection;
removing the selected page picks a surviving neighbor in the existing reducer.
Selected source identity is independent of expanded presentation and timer expiry.

Existing `expandedByUser`, interruption, auto-expiry and ongoing-activity restore
policies are preserved. The pager change does not create a permanent expanded
state or reset source-driven animation whenever text changes. Existing visual
tokens, island geometry and reduced-motion preferences are retained.

## 5. Compatibility / positive fixture matrix

| Category | Domain/renderer behavior | Verification boundary |
|---|---|---|
| WhatsApp/Telegram/Instagram/SMS | Separate keys, source grouping, Unicode text, original available actions and text reply inputs | Synthetic reducer/policy fixtures only; no real accounts tested |
| Gmail/email | Summary + children and expanded copy; no merging from equal display titles | Synthetic grouping/update fixtures only |
| Payments/UPI/OTP | Preserve literal source copy, conservative lock-screen protection; never infer success | Synthetic sensitive-copy + existing privacy tests |
| Music/YouTube/Spotify | Existing MediaSession path and presentation restoration; `transport` no longer means ride | Existing media policy tests, not live-session E2E |
| Calls/calendar | Existing call/critical-interruption policies retained | Existing unit tests, no live call/event generated |
| Maps/navigation | Ongoing source with navigation metadata uses navigation renderer | Pure classification fixture |
| Downloads | Actual progress capabilities choose progress renderer | Pure classification and existing update/reducer tests |
| Shopping/delivery/unknown apps | Generic fallback unless actual supported capability supplied | Generic/multilingual fixtures; no fabricated tracking data |
| System events | Existing real monitors retained, including newer network/transient fixes already in dirty checkout | Not changed by this notification patch |

## 6. Negative and regression coverage

`NotificationEngineeringRegressionTest` covers:

- Cross-app identical group names and same-app summary replacement.
- Source removal of old body/subtext/inbox/art and exact update identity.
- Another conversation updating while selected content stays stable.
- Quiet arrivals behind high-importance content.
- 1,000 ongoing updates: bounded size, stable selected ID, valid index, unique
  sources, followed by complete source removal.
- Empty text, emoji, Arabic, Hindi, multiline and 10,000-character content;
  duplicate callbacks do not create duplicate sources.
- Missing action, throwing dispatch boundary and honest successful handoff.
- False-positive app-name/prose classification, actual progress/timer/picture/nav.
- Public verification/payment text receiving extra sensitivity protection.
- Pager insertion/removal identity and rejection of invalid/stale page keys.
- Reply removal, access-cleared source list, removed action and replaced action ID.

Two old summary tests used different package names for summary and child while
expecting a merge. They were corrected to model a real same-app group; a new
cross-app regression permanently asserts the opposite behavior. The initial test
run exposed this fixture problem; it was not suppressed or baselined.

These are deterministic JVM tests. Fake dispatch exceptions are NOT a tested
Android canceled PendingIntent; pure pager policy tests are NOT Compose gestures.

## 7. Device and framework acceptance

Initially `adb devices` returned no devices and `emulator -list-avds` returned no
AVDs. Later a Realme RMX3085 appeared over USB (`X4Y9QSOZ4DEADEDM`) and Wi-Fi
(same serial, not a second phone). Installed the updated debug APK successfully
with `adb install -r`, preserving app data, and requested MainActivity launch.
The notification listener is present in Android's live-listener list. Initial
capture was black and the overlay unbound; later MainActivity rendered, the
overlay bound and Android reported no crashed accessibility service. The secure
keyguard remained showing/input-restricted, so private notification content and
full gestures/reply acceptance could not be established. Asked the user to
unlock/open the app. Screenshots show the main dashboard and redacted overlay;
these are a startup smoke check, not proof of pager correctness.

Attempted two benign shell posts; shell argument parsing resolved them to one
source key (`com.android.shell`, tag `QA`), not two independent pages. This does
not count as a successful two-source UI test. Inspected only that synthetic
source, then removed it using a targeted rightward swipe in Android's shade.
Confirmed no `com.android.shell` notification key remained; did not use Clear
All or cancel other notifications. No WhatsApp, Instagram, Google Pay, Gmail, Telegram,
Phone or other source application was driven. No real payment, reply, call or
notification-access mutation was performed. October 11 device checks are older
evidence and do not validate this patch. A black capture is not a passing UI test.

Before release, use a connected authorized device to exercise real MessagingStyle
and grouped sources, canceled PendingIntents, choice/data-only RemoteInput,
updates/removal during a swipe, action replacement while composing, lock/unlock,
permission revocation/rebind, service restart/process death, rapid mixed calls +
music + messages, 320dp width, 200% fonts, landscape, IME and TalkBack. Verify
rendered source actions against the source notification before executing them.

## 8. Build discipline and outstanding limits

Related edits were batched; targeted unit tests precede final incremental APK
assembly/lint. No clean builds or Gradle configuration/dependency changes.
Final validation: `:app:testDebugUnitTest` passed 201 tests, zero skips, failures
or errors. `:app:assembleDebug :app:lintDebug` completed successfully; lint reports
zero errors and 81 warnings. `git diff --check` passed. An earlier targeted run
failed two malformed summary fixtures; corrected realistic package identities
and added cross-app isolation coverage before the passing runs. No failures
were suppressed or baselined.

Remaining: live ranking-change callbacks (importance currently sampled on post),
complete Android MessagingStyle sender/message extraction, systematic framework
instrumentation/Compose UI tests, strict artwork retention/key-collision audit,
and device gesture/frame-timing acceptance. Sensitivity keyword matching is
conservative and not a multilingual financial/OTP detector; platform sensitivity
still applies to all languages. Inline replies may still be restricted by Android
or source apps. The 20-item island queue intentionally is not a replacement for
the full Android notification shade. This is a substantial targeted reliability
pass, not proof of every production scenario.

## 9. Platform references

- Pager keys/current vs settled page: https://developer.android.com/develop/ui/compose/layouts/pager
- RemoteInput capabilities/results: https://developer.android.com/reference/android/app/RemoteInput

Claude Mem list: `notification-engineering-2026-10-17` retains findings,
implementation progress and explicit runtime acceptance gates.
