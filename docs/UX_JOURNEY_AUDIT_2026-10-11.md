# UX and setup journey audit — 11 October 2026

## Journey and architecture

`MainActivity` selects Language → Onboarding → Permissions → Main hub. Skip
allows previewing the hub; its setup card returns users to Permissions. The hub
hosts Island, Features, Live and Settings, with settings/details presented by
`MainContainerScreen`. Most screens share `AppScreen`; dialogs and sheets use
`AppDialog`/`AppSheet`. Settings changes are applied immediately rather than
requiring a Save button. Existing island visuals and business rules are retained.

## Findings and implementation

| Severity | Screen/pattern | Root cause and user impact | Resolution |
|---|---|---|---|
| P0 | Permissions | Continue was the last item in a long scrolling column; initial viewport had no next step. | Shared Scaffold `bottomAction` reserves measured space, respects safe drawing/IME insets, and keeps action independent of scrolling. |
| P0 | Permissions readiness | Granted-count policy ignored runtime connection; notification working state wasn't used. “Ready” contradicted recovery warning. | Pure `SetupReadiness` requires both access and connected services; primary action enables missing access, reconnects a service, or continues. All combinations tested. |
| P0 | Service health | Disconnect/destroy did not consistently notify visible screens. | Overlay unbind/destroy and listener destroy publish health-change broadcasts; resume refresh plus a bounded foreground settling window handles Settings returns. |
| P1 | Language onboarding | Continue hidden after the complete language list. | Same fixed bottom action; search/list content can scroll independently and move above keyboard. |
| P1 | Permissions completion | Extra confirmation sheet required a second action; dismissing it completed setup implicitly. | Continue completes directly, rechecks runtime health, prevents duplicate taps and exposes persistence errors. |
| P1 | Permissions optional access | Recommended permissions looked mandatory; OEM settings visit was labeled granted; phone permission and screening role could launch together. | Explicit optional copy; OEM visit is not a grant; screening is a separate optional user action; phone permission set is API-aware. |
| P1 | Premium | Purchase action below benefits and plan list, status could be offscreen. | Fixed contextual purchase action with selected price/billing note and immediate action feedback; existing billing gateway unchanged. No purchase made during testing. |
| P1 | Onboarding | Non-scrollable weighted presentation could overflow on small/landscape screens; page lost on rotation. | Stage scrolls while navigation remains outside; saved page and back-to-previous-slide behavior. |
| P1 | Root navigation | Route/tab/detail state recreated on rotation, potentially sending users back into onboarding or away from Settings. | Saveable route/tab/detail state; startup route resolution only when no restored route exists. |
| P1 | Sheets/dialogs | Fixed 520dp body cap ignored header and small available height; keyboard could cover controls. | Sheets bound to actual safe viewport and scroll complete content; IME-aware modal padding. |
| P1 | Shared accessibility | Custom switch lacked switch role/state; icon descriptions were accepted but unused. | Toggle semantics, Material minimum interactive size, button roles, 48dp icon/info targets and explicit descriptions. |

## Core-screen audit coverage

| Area | Navigation/action/layout assessment |
|---|---|
| Startup splash | No user decision required; loading route resolves in MainActivity. Rotation preserves resolved route. |
| Language, onboarding, permissions | Linear next actions audited and improved above. Optional permissions never enter required readiness. |
| Island dashboard | Setup recovery is prominent; health uses working-service checks. Existing floating dock clearance retained. |
| Features/Studio, Live, Settings | Shared scrolling/insets and bottom dock clearance inspected. Actions lead to details or apply settings immediately; no artificial Save action added. |
| Display, notifications, music, calls, battery, sound, themes | Shared back navigation and scrolling inspected. Configuration is immediately persisted; previews/history are contextual secondary actions, not completion gates. Shared switch/back semantics improved. |
| Quick controls | Immediate config and searchable contacts; shared IME insets improve contact input reachability. Permission and premium recovery remain contextual. |
| Premium | Fixed contextual primary action; reward/restore remain secondary. Play purchases, entitlement and provider behavior are outside this layout acceptance. |
| About/component gallery | Informational/reference navigation; shared safe screen frame/back controls apply. |
| Permission explanations, contact/history, feedback, premium, rating, exit, update dialogs | Shared modal bounds and keyboard handling improved; dismiss/back affordances inspected. Long content scrolls; no blanket sticky CTA added to every dialog. |

This is a source-level navigation/layout/accessibility audit with targeted device
checks, not a claim of exhaustive interaction testing of every feature.

## Runtime evidence and verification

- Connected Realme RMX2001 (1080×2400, density 480, font scale 1.0), USB and
  wireless connections to the same model. Used USB for testing.
- Before-change screenshot reproduced missing initial Continue and conflicting
  “Ready”/paused-service copy. Android dumpsys showed overlay enabled but in the
  crashed-service set, with no bound overlay; notification listener was bound.
- Installed the new APK without clearing app data. Inspected Island, Settings,
  Permissions and normal Android Settings navigation. The Permissions footer is
  visible initially and at the end of a scrolled list, above three-button system
  navigation. Both reconnect guidance and connected/Continue presentation were
  observed during service reconnection after installation.
- Tapped Continue with background access still ungranted: returned directly to
  Settings, without the old completion sheet. Returning from Android Display
  Settings preserved the Permissions route. Back and Close have named button
  nodes in the UI hierarchy; full spoken TalkBack traversal was not tested.
- OEM shell restrictions rejected font/rotation writes. A normal font-settings
  visit did not change the font scale. Verified final font scale remains 1.0 and
  user rotation remains 0; display dimensions/density were not changed. Larger
  text, landscape, smaller displays and gesture navigation are **not runtime
  verified**. No app data reset, purchase or notification-access revocation.
- One combined incremental Gradle invocation completed successfully:
  `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug`. 165 JVM tests,
  zero failures/errors; lint zero errors and 80 warnings. `git diff --check`
  passed. No clean build, speculative build configuration change, or per-edit
  build was used.

## Acceptance matrix

Missing overlay → enable overlay; missing notification access → enable
notification access; enabled but disconnected overlay → reactivate overlay;
enabled but disconnected listener → reactivate notification access; both
connected → Continue, regardless of optional access. Connection flags without
their corresponding access grant never count as ready. The screen does not
claim Android killed a service merely because it hasn't connected yet.

## Remaining device gates

Full TalkBack spoken traversal, measured contrast across every color variant,
all OEM recovery mechanisms, actual missing/revoked notification access, gesture
navigation modes on other devices, billing/rewarded flows, and every live-island
interaction require further acceptance. System settings failures currently log
fallback errors; broad platform-settings error feedback remains follow-up work.
No new dependency or Gradle configuration changes are needed.

Device inspection also revealed an existing P1 overlay issue: the automatically
expanded mobile-data alert can remain over app/Android Settings headers even
while Wi-Fi is available. This is independent of the fixed screen footer and
should be investigated with connectivity, transient-alert ownership and source
action routing; it was not redesigned or suppressed during this layout patch.
The screenshot captures contain that real overlay, not a mocked clean state.

Layout follows Android's documented Scaffold padding consumption and safe/IME
inset handling: https://developer.android.com/develop/ui/compose/system/insets-ui
