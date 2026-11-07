# Dynamic Island for Android — Master Product Behavior & Motion Specification

**Version:** 1.0 — 8 November 2026  
**Owner-approved decisions:** UX-C01–UX-C10, UX-C12–UX-C15; UX-C11 is treated as the owner's stated layered-islands direction (not a selected letter).  
**Design decisions completed by product architect:** PD-001–PD-038 below.  
**Status:** Authoritative PRODUCT TARGET; **not a statement that source code currently implements it**. Platform-dependent capabilities require proof on real devices.

## 1. Product philosophy: what the user's answers imply

1. **Protect attention:** Never replace a notification someone is currently reading merely because a normal new message arrives.
2. **Non-destructive by default:** Closing a visual surface is not the same as removing an island item, dismissing an Android notification, or ending a call/timer.
3. **Useful at every density:** Idle single tap = Quick Controls; one notification = one detailed card; many = horizontal pager; persistent activities get their own controls.
4. **Beautiful but restrained:** iOS-quality black cutout-anchored geometry, tactile continuous morphing, readable data, adaptive mini-island stack; do not cover the rest of the screen with 12 banners.
5. **Restore context:** Preserve the selected notification by *stable key*, including after insertions, updates, short interruptions, and collapsed/reopened sessions.
6. **Android-native honesty:** Overlay, notification data, call controls, inline replies, background execution and lock-screen access are conditional. Prefer safe fallback over pretending to offer unavailable system privilege.
7. **Power without extra questions:** Sensible configurable defaults. Require owner input only for irreversible or fundamental commercial/product tradeoffs, not routine physics/timer parameters.

## 2. Human-behavior models and safeguards

| Persona | Intent / behavior | Main risk | System response |
|---|---|---|---|
| Fast glance | Briefly reads a banner, then resumes work | Interruptions linger | Transient preview, smooth automatic collapse, no system notification cancellation |
| Deep reader | Keeps an expanded card/pager open | New alert steals context | Pin selected key, push ordinary arrivals into animated mini-islands beneath |
| High-volume communicator | 10–50 notifications, several apps/conversations | Cognitive overload and index-jumps | Newest-first virtualized pager, clear count, stable selection, overflow, grouping when trustworthy |
| Multitasker | Music + Maps + timer + incoming call | Wrong content priority, lost activity | Deterministic arbitration, small companion affordance, suspend/restore focus |
| One-handed user | Touches near status bar while walking | Accidental dismiss, inaccessible controls | Generous safe hit areas, forgiving gestures, no destructive swipe, accessible alternatives |
| Privacy-conscious user | Locked screen, OTP, bank, confidential messages | Content leakage over other apps | Redaction by default, opt-in content detail, no raw payload persistence, explicit permission checks |

## 3. Grounding and Apple comparison

- Apple officially specifies Live Activity **compact, minimal and expanded** presentations; compact/minimal tap can launch the associated app, and touch-and-hold expands; brief expansion can occur during Live Activity updates. Android notification previews and the vertical *child-mini-island stack* in this specification are **OUR CUSTOM FEATURES**, not asserted official iOS notification behavior.
- Use Apple geometry/spacing/motion principles as **references**, not iOS point values copied blindly into Android dp. Use real Android cutout/insets and visual tests on target hardware.
- Primary references: https://developer.apple.com/design/human-interface-guidelines/live-activities ; https://developer.apple.com/documentation/activitykit/displaying-live-data-with-live-activities ; https://developer.android.com/reference/android/view/WindowManager.LayoutParams ; https://developer.android.com/reference/android/service/notification/NotificationListenerService ; https://developer.android.com/develop/ui/compose/layouts/pager ; https://developer.android.com/develop/ui/compose/animation/quick-guide .

## 4. Formal model — DO NOT proliferate monolithic `IslandState` subclasses

Maintain existing `IslandState` and production data path as far as practical. Introduce *orthogonal derived presentation policy* rather than a wholesale untested rewrite:

```
Canonical notification reducer (single source of truth; framework SBN key)
  + current media/call/timer/navigation/system activity signals
  + user preferences/privacy + platform capability flags
  + immutable interaction/session state (selectedKey, ownership, replyDraft)
  + scheduled transient events (with cancellable tokens)
        -> pure BehaviorDecision(state, effects, presentation, reason)
        -> production state manager + SAME playground policy engine
        -> one real shared renderer and one platform-window adapter
```

**State axes:** (a) active domain/activities; (b) presentation {HIDDEN, IDLE, COMPACT, TRANSIENT_EXPANDED, USER_EXPANDED, REPLY}; (c) owner {SYSTEM_EVENT, USER, INTERRUPTION}; (d) current selected notification **key**, never only page index; (e) mini-island list with per-item animation/timer state; (f) touch/gesture ownership; (g) device privacy/window capability. The stack is a *presentation surface*, not a second notification database.

**Event types:** NotificationPosted, NotificationUpdated, NotificationRemoved, NotificationRankingChanged, Tap, LongPress, SwipeUp, SwipeDown, SwipeLeft/Right, OutsideTouch, Back, MiniTapped, ActionTriggered, ReplyStarted/Sent/Cancelled, TimerElapsed, ActivityStarted/Updated/Ended, ScreenLocked/Unlocked, OverlayPermissionChanged, Focus/DndChanged, ConfigurationChanged, ProcessRestored.

**Reducer invariants:** deterministic + idempotent; versioned scheduled jobs invalidated on state change; no loss of `selectedKey` due to insertion; stale image/action callbacks cannot resurrect removed items; suppress duplicate show events; no actions sent during playground simulation; presentation completion cannot cancel an OS notification.

## 5. Authoritative owner decisions — preserved unchanged

| ID | Scenario | Approved behavior |
|---|---|---|
| UX-C01 | Idle + no notifications/activities + compact single tap | **Open Quick Controls** |
| UX-C02 | Exactly one eligible notification, no other activity + compact single tap | Expand detailed notification in island |
| UX-C03 | Twelve pending notifications, no other activity + compact single tap | Expand horizontal notification pager, newest first |
| UX-C04 | User reading a pager page + new ordinary notification arrives | Keep selected page, show subtle New indication, update count |
| UX-C05 | Source app removes the *selected* notification from expanded pager | Select next; if last select previous; keep pager expanded, update count |
| UX-C06 | User taps × on expanded notification | Behavior configurable; **default remove from Island queue only**, leave Android shade untouched |
| UX-C07 | User removes final island notification, no other activity | Immediately return to Idle Compact; single tap uses UX-C01 |
| UX-C08 | Island-dismissed notification with same identity updates | Resurface for meaningful content/status/new message, not minor progress/metadata |
| UX-C09 | Delivery resurface classification | Major milestones plus first Nearby alert; not every stop/percentage change |
| UX-C10 | Compact with 12 notifications + previously dismissed delivery receives major milestone | Temporarily expand milestone, then collapse, reinsert/retain queue item (13 total) |
| UX-C11 | New ordinary notification while main content expanded | **Owner-requested direction:** separate, child mini-island emerges below main rather than replacing current content. Exact subrules below are architect defaults. |
| UX-C12 | Single tap a child mini-island | Seamlessly promote it into Main Expanded; old main remains in queue/pager |
| UX-C13 | Several arriving child notifications | Adaptive: small screen 1–2 visible, larger up to 3; +N overflow; do not obstruct main |
| UX-C14 | New child arrives | Newest mini-island immediately below main, previous children slide down |
| UX-C15 | Child idle | Smart Adaptive Timeout then visual merge; pause while child is touched; keep queue and main selection |

## 6. Architect-selected defaults, not separately owner-approved

| ID | Rule / condition | Selected default and rationale |
|---|---|---|
| PD-001 | Mini-island timers (previous pending UX-C16) | **Independent per child.** New arrivals do not restart older countdowns; touch pause, release 2s grace. Prevents endless stack on busy devices. |
| PD-002 | Mini visible lifetime | 5s normal; 7s important milestone; start when entry settles; user touch pauses; once resumed allow remaining time + 2s grace; configurable after usability tests. |
| PD-003 | Auto preview lifetime | 3.5s for short ordinary preview; 5s for longer readable content; cap 6s; start when content becomes readable; touch pauses, release allows 3s grace. These are trial defaults, not Apple's timings. |
| PD-004 | User-invoked expansion | Remains expanded until explicit collapse/outside tap/back or device state change; no forced 3s timer while reading or typing. |
| PD-005 | Incoming alert while **transient** expanded | Keep existing main preview; show next eligible ordinary notification as mini-child; main auto timer continues; when main collapses, children merge visually and stay in queue. |
| PD-006 | Tap the current main while user-expanded | Tap actual content/link may invoke **explicit Open App** affordance; empty background is no-op; never accidentally dismiss. |
| PD-007 | Idle long press | Open app island customization/settings, preserving UX-C01 single-tap Quick Controls. |
| PD-008 | Double tap | No standalone destructive shortcut; treat as one semantic action until product data supports a distinct benefit. Defend against second tap hitting newly appeared controls by stable gesture-target identity, not global 300ms blackout. |
| PD-009 | Compact swipe up | Do not destroy or clear notifications. By default no-op/subtle squeeze for idle and queued notifications; active activities remain in place. |
| PD-010 | Expanded swipe up | Collapse to previous compact arrangement, preserve queue and ongoing activity; exception user is editing reply: first protect/exit reply safely. |
| PD-011 | Compact swipe down | User opens associated detail/pager where gesture recognizer has priority and it does not compete with system shade; feature can be disabled per OEM test. Tap remains primary. |
| PD-012 | Expanded horizontal swipe | Pages eligible notifications only; preserve stable selected key. On normal activity cards, do not invent horizontal actions. |
| PD-013 | Expanded swipe down | No destructive action. Let scrollable child content and system gestures behave normally; do not fake system shade programmatic control. |
| PD-014 | Tap outside expanded island | Collapse and pass touch to underlying app **only when safely verified on that window/OS**; for reply, close IME and collapse while keeping in-memory unsent draft for up to 60s if unlocked. |
| PD-015 | New ordinary arrival while USER pager open | Add to canonical queue, persistent selection key, latest child mini-island and subtle indicator. Never page-snap. |
| PD-016 | Pagination on first open | Newest eligible notification first; manual page navigation thereafter pins selection; across brief re-expansion restore last key if still valid in same session, otherwise newest. |
| PD-017 | Pager count | Count **Island-eligible queued items**, not all Android shade entries and not inferred unread totals. Group summaries are not double-counted. |
| PD-018 | Source removes selected | UX-C05 next-else-previous; if 0 items and no activities -> UX-C07 idle; if others active -> restore active primary. |
| PD-019 | User removes selected via × | UX-C06 default Island-only, same next-else-previous selection; keep suppression entry keyed by original notification; optional OS cancellation only on explicit user setting and `isClearable` + valid permission. |
| PD-020 | Reappearance after Island-only removal | Suppress same-key insignificant changes; significant text/new-message or semantic milestone may resurrect; no fabricated semantic certainty. |
| PD-021 | Grouped messaging | Use Android group/conversation keys when reliable. One card per actual notification, optional thread grouping; no synthetic suppression of distinct messages. |
| PD-022 | Notifications posted in bursts | One initial transient primary preview; subsequent ordinary arrivals within preview become mini-items, rate-limit entry animations; queue all eligible unique items. |
| PD-023 | Child mini stack overflow | Visible 1–3 by available safe vertical space. +N counts other new, unmerged child notices; tapping overflow opens/preserves pager rather than extra full-height list. |
| PD-024 | Mini child removal/update | Change text in place for same key; remove animated child on source removal; if child expires, merge visually without removing queue; no duplicate child on minor updates. |
| PD-025 | Main promoted via mini tap | Child morphs into main; prior main returns to queue and its selected key can be restored by pager; do not mark read automatically. |
| PD-026 | Ordinary notification vs active music | Auto-preview is allowed if user is not interacting with media; after timeout restore music compact with notification companion/count. If music expanded by user, ordinary new message appears as child and does not replace controls. |
| PD-027 | Calls | Ringing call preempts ordinary notifications and passive previews only when legitimate Android call signal/actions exist; never interrupt carrier/phone system UI or fake answer/decline capability. End/reject restores safe compact activity, or previous pager if interruption <10s and user was reading it, unlocked. |
| PD-028 | Timer/alarm completion | Prominent but not equivalent to call; show critical event over passive preview; if reply editor active, avoid stealing keyboard, show obvious alert affordance with accessible fallback. |
| PD-029 | Navigation | Keep ongoing navigation compact primary or companion depending live priorities. Next-turn/turn-now updates are promoted above routine messages, but not above ringing calls. Do not claim Maps exposes full route actions unless available. |
| PD-030 | Recording/screen recording | Persistent clearly visible indicator whenever detectable; never stop recording on collapse; no unsupported device-private controls. |
| PD-031 | Charging/volume/ringer/Bluetooth | Short low-interruption transient HUD, coalesced rather than queued as unread messages. Never occupy notification pager unless meaningful actual posted notification. |
| PD-032 | Privacy | Hide sensitive message bodies by default when locked, mirroring/recording when needed, and for OTP/financial previews where classification is reasonably reliable; allow user privacy preferences. |
| PD-033 | DND, silent channels, focus | Respect system priorities and user choices. Silent/ongoing low-priority noise defaults to no auto-expand; may remain accessible in pager only if configured and eligible. |
| PD-034 | Notifications actions | Only display actions genuinely provided by source notification (`PendingIntent`, `RemoteInput`) and allowed by platform; otherwise show Open app, not fake buttons. |
| PD-035 | Action touch geometry | At least 48dp effective target where practical with safe spacing; max two primary actions per card + real overflow if more available. |
| PD-036 | OEM/cutout and game/video | Auto-position within safe insets, hide/minimize by game/full-screen choice, no unauthorized status bar replacement, no invisible full-width interception. |
| PD-037 | Process/permission failure | Restore from live active notification set on listener reconnection, clear obsolete ephemeral state and secrets, show actionable permission indicator only within own UI; no crash/ghost island. |
| PD-038 | Reduced motion and high font scale | Maintain logical transitions but replace morphs with rapid fades/geometry-safe changes when reduced motion requested; 200% font, RTL, talkback and landscape must remain functional. |

## 7. Precedence / Arbitration Contract

Order of precedence for *presentation* (not destructive removal):

1. **System and privacy suppression** — locked/secure/permission-revoked/notch unavailable: no sensitive overlay, no unsafe touch regions.
2. **Explicit user interaction ownership** — active reply editor and touched controls are protected against ordinary notification preemption; critical system events may request attention without silently sending/canceling reply.
3. **Ringing real call** — highest ordinary interrupt level; show only supported controls, yield to OS in-call UI where required.
4. **Safety/urgent timer or next navigation instruction** — high salience, bounded. Avoid hiding system safety interfaces.
5. **Manually expanded selected notification or Live Activity** — pinned from ordinary new events.
6. **Ongoing navigation, media, call duration, recording, delivery** — retain compact indicators and stable main/companion layout.
7. **Ordinary notification preview** — transient only when allowed, queued and clustered on bursts.
8. **Low priority transients (volume, charging, Bluetooth)** — brief HUD, coalesced; never overwrite active reply/user focus.
9. **Idle** — compact black cutout anchor; single tap Quick Controls.

Do not equate notification channel importance with full emergency authority; use a typed, explainable policy. If the system supplies only incomplete signals, do not invent high-priority classification.

### Priority examples

| Active context | New event | Default outcome |
|---|---|---|
| Idle | Ordinary message | Temporary expanded preview -> compact with queue item |
| Compact 12 messages | Major delivery milestone | UX-C10 temporary preview -> compact + 13 queue entries |
| User-expanded pager on page 5 | Ordinary message | UX-C04 pin selected page + newest-first child mini + queue increment |
| Transient delivery preview | New WhatsApp | Main delivery stays + child WhatsApp mini; independent timers |
| Main expanded with children | New Instagram | UX-C14 add top child; older shift down; UX-C13 clipping/overflow |
| Main expanded with mini Telegram | Tap Telegram mini | UX-C12 promote with morph; original main remains in pager |
| User-expanded main or reply | Incoming phone call | Call preempts if valid signal; suspend focus/draft; safe restoration |
| Compact music | New normal message | Temporary message preview, then music restored + badge/companion |
| Expanded music controls | New normal message | Do not steal media controls; show child notification |
| Idle | Charging connected | Short transient battery HUD -> idle, no notification queue pollution |
| Expanded pager selected item | Source removes selected | UX-C05 next / previous; keep expanded |
| Expanded pager last item | User removes Island-only | UX-C07 idle, source OS notification remains |
| Expired child mini | Any main expanded content | Visual merge only; pager queue unchanged |

## 8. Gesture and action contract

| Context | Single tap | Long press | Double tap | Swipe up | Horizontal swipe | Outside tap / Back |
|---|---|---|---|---|---|---|
| Idle, 0 notifications/activities | **Quick Controls (UX-C01)** | Customization/settings | No special action | No destructive action | No-op | N/A |
| Compact 1 notification, no activities | **Expand card (UX-C02)** | Expand same card (redundant but intuitive) | One logical tap, safe no double dispatch | No deletion | No pager action | N/A |
| Compact >=2 notifications, no activities | **Open pager, newest first (UX-C03)** | Same pager | One logical tap | No deletion | Disabled until expanded | N/A |
| Compact Music/Call/Timer/Nav | Expand *that* activity's controls/details if supported | Expand/full details | No special action | Minimize only if safe, never end | Companion selected by its own target | N/A |
| Main user-expanded message/pager | Explicit content-open or action | Context menu only if useful | Safe, no inadvertent × | **Collapse, preserve queue** | **Pager** | **Collapse, preserve queue** |
| Main temporary preview | Action/content targets | Convert to USER_EXPANDED, timer off | Safe | Collapse | If >1, page only after user takes ownership | Collapse |
| Child mini-island | **Promote to main (UX-C12)** | Optional preview/details promotion | One logical tap | Not destructive | No hidden pager capture | N/A |
| Inline reply | Text/action controls | Text selection | No destructive action | Protect draft and collapse safely | Pager locked while editing | Close IME and collapse; keep ephemeral draft |
| Incoming call | Answer/decline only if real supported action | N/A | Never send duplicate | Minimize/hide *visual* where safe, not decline call | N/A | Preserve OS call affordances, do not auto-reject |

**Routing:** Gesture recognizers must claim touch ownership after appropriate platform touch slop; Compose pager must not fight parent upward-collapse detection. A pointer cannot dispatch both paging and collapse. A tap is associated with the target *at pointer-down*, so a second rapid tap never activates a newly formed destructive button. Do not block all legitimate touches for 300ms.

## 9. Animated child islands and motion language

- **Visual structure:** black, near-black child surfaces emerging from main island's lower edge; compact but legible. Rounded shapes/corners consistent within family, restrained shadow/edge light, color accent only in app icon/live indicator, no generic neon glass everywhere.
- **New child:** start as shallow attached dark bump just beneath main surface -> detach vertically by a small gap -> expand to measured final chip/card dimensions -> icon/title fade in late. Use interruptible spring; initial trial movement 220–340ms (not an official Apple requirement).
- **Arrival while 2/3 visible:** new child enters immediately below main; older child positions interpolate down. Lowest displaced past cap visually merges while remaining queued. Never draw beyond safe screen budget.
- **Child tap:** child's position/shape transforms into main bounds without temporary duplicate card; previous main content crossfades into pager queue representation; stable key swap at a deterministic commit point.
- **Child expiry:** reverse visual movement and fade under main, no notification removal or user selection change.
- **Burst:** coalesce many arrivals in one frame, animate newest visible child and compact '+N' increase; no dozen individual animations; no persistent relayout churn.
- **No abrupt snapping** on keyboard appearance, rotation, sudden child expiry, system UI changes or 200% font. Animate only when safe; support reduced motion and accessibility motion scaling.
- **Trial layout targets:** compact height near 37dp where device safe; expanded width = min(available width - safe margins, 408dp); main expanded grows to content but bounded by available height; mini vertical budget <= min(3 visible, available safe viewport) and should not cover status/system controls or occupied keyboard. Exact pixel tokens require screenshots/device testing.
- **Performance:** prefer draw/transform animations (`graphicsLayer`, `Animatable`) over remeasuring entire overlay each frame where feasible; instrument jank on low-end phone; never promise constant 60 fps from a setting alone.

## 10. Notification data, suppression, and classification

**Identity:** Use real platform `StatusBarNotification.key` for identity; retain user/profile segregation. Treat `postTime`, text and action changes as *updates to an existing key* rather than a new identity. Stable pager keys must not shift on in-place updates.

**Item fields (derived or normalized):** stable key, package/user identity, category/channel importance, event timestamp, text/title/subtext, conversation/group key, remote-input/action capabilities, attachment/artwork reference, progress/chronometer, clearable/ongoing, privacy classification, semantic milestone if reliable, content hash, last acknowledged content fingerprint, island-only suppression state, queue eligibility and mutation sequence.

**Source removal** always clears queue item and suppression entry. **User Island-only dismissal** creates a suppression record for same key/fingerprint, without canceling Android shade. **Significant update** may revive only when reliable change detected. For delivery: first nearby alert (for that journey/key), arriving, delivered, failed, canceled or comparable clear milestone; percentage, one-stop decrement, ETA jitter and artwork-only changes do not revive. If semantic classification uncertain, prefer not to re-alert repeatedly; show updated content only when manually opened. For messaging, actual new message content/message timestamp when available can revive; ignore timestamp-only repaint.

**Group eligibility:** ignore empty summaries that double-count children, respect notification channel importance, do not put ongoing system service noise in pager by default. Preserve accessibility label/identity. Count refers only to retained *eligible* queue items, not speculative unread state or hidden sensitive notifications.

**Lifetime:** Queue mirrors active eligible notification records, subject to explicit Island-only suppression. Do not permanently store raw message text; persistent preferences can store policy not content. Clean up suppression for source removal, app reinstall, user switch or listener reconnection as appropriate.

## 11. Platform windows, security, and action safety

`TYPE_APPLICATION_OVERLAY` is not a privileged iOS Dynamic Island. `FLAG_NOT_TOUCH_MODAL` sends touches OUTSIDE the **actual window bounds** behind it; transparency inside a large full-width window does not automatically create pass-through touch holes. `FLAG_WATCH_OUTSIDE_TOUCH` yields a special outside event, not a full gesture stream. Prototype bounds for primary and child islands and consider separately bounded touchable windows, or an explicitly non-touchable animation-only layer whose pass-through is verified; respect Android 12+ untrusted-overlay rules. If safe outside-tap detection fails on OEM, prefer safe alternate collapse (up swipe, Back, close control) over trapping touches.

Do not overlay sensitive/system permission dialogs, secure authentication entry, lockscreen content or call/emergency UIs in an unsafe way. Notification access must be explicit and revocable. `PendingIntent` can be absent/canceled; `RemoteInput` works only if source offers it; phone actions vary by dialer and role; media controls depend on active session. All user actions must be idempotent where possible and provide failure feedback.

## 12. Accessibility, preferences, and metrics

**Defaults exposed in settings:** notification preview on/off and duration, category filters, × behavior (Island only / Android too when clearable), privacy previews, idle capsule visibility in full-screen/games, animation intensity/motion, mini duration/visible count (advanced), compact positioning/calibration. Keep settings manageable: Basic vs Advanced; no hundreds of toggles.

**Accessibility:** semantic announcements for new mini count without reading sensitive body, explicit next/previous pager actions for TalkBack, RTL ordering test, controls at useful touch sizes, contrast in bright sunlight, 200% text scalable without clipping, prefer reduced motion where system requests. No reliance only on color, velocity or horizontal gesture.

**Telemetry (opt-in/local first):** rates of inadvertent dismiss, failed tap-through, missed mini taps, animation jank, stale queue keys; no message body logs. Build debug-only event timeline with timestamps, selectedKey, event type and state, never raw protected content by default.

## 13. Cross-axes test coverage matrix methodology

Exhaustiveness is **combinatorial**, not a single linear story:

- Notification density: 0, 1, 2, 3–4, 5–12, 13–50, grouped, many updates to same key.
- Domain: none, normal message, sensitive/OTP, group message, progress/delivery, music, call, timer, navigation, recording, system HUD, dual activity.
- UI: hidden, idle, compact, transient expanded, user expanded, horizontal paging, mini-child stack, reply IME, transitional animation.
- Events: new, update minor, update material, source remove, user Island-only remove, optional OS cancel, TTL expire, action success/fail, call start/end, lock, permission revoke, window resize.
- Gestures: tap, double, hold, up/down, horizontal, drag diagonal, action, child tap, outside, Back, TalkBack alternatives.
- Environment: small/large/punch-hole/left/right/foldable, font 1x/2x, LTR/RTL, DND, battery saver, screen recording, Android 12+, OEM-specific.

The companion `DYNAMIC_ISLAND_ACCEPTANCE_TEST_MATRIX.md` enumerates representative pairwise and risk-targeted scenarios. The team must expand coverage automatically from these axes; **a row marked Implemented does not mean Verified until there is test evidence**.

## 14. Phased implementation strategy and handoff

**Phase 0:** Read completed Gemini 02B corrected audit and UX-C11 research; reconcile this owner-authoritative target with reality; tests and current behavior baseline. Do not blindly adopt Gemini's unverified 'verified' claims.

**Phase 1:** Create/use canonical pure interaction-policy/reducer with stable key, safe collapse semantics, auto preview owner state, event trace, unit tests. Wire to production AND playground without changing overlay layout yet. *Next assignment: Task 03.*

**Phase 2:** One/many notification pager; stable-key selection, update/removal behavior, classification/suppression and realistic debug fixtures. Device gesture tests.

**Phase 3:** Layered animated mini-islands in debug renderer + overlay touch-prototype; adaptive stacking +N, timers, tap-to-promote and reflow; verify tap-through before prod flag.

**Phase 4:** Activity arbitration: media/calls/timers/navigation/recording/HUD/dual activity, true action dispatch/capability fallbacks, interruption restoration.

**Phase 5:** Visual polish: camera cutout geometry, motion tokens, animation previews, accessibility, type scaling, low-end performance, screenshots.

**Phase 6:** OEM/Android version matrix, privacy, lock, DND, IME, battery saver, production instrumentation, release isolation and rollout gating.

**Release blockers:** no ghost notifications after remove; no accidental shade cancellation; no click theft on underlying apps; no leaked lock-screen content; no unverified 'always works' actions; no regression of C01–C15; no user-ownership page jumps; no source-app actions in debug mock mode; no unverified giant transparent overlay touch surface.

## 15. Product governance

- **Owner approved:** C01–C10 and C12–C15, plus the intent in C11.
- **Architect defaults chosen:** PD-001–PD-038; revise from usability evidence, not from repetitive owner questionnaires.
- **When to ask the owner:** irreversible payment or privacy policy, a tradeoff that significantly affects user safety, or a brand-level interaction that cannot be resolved with observed user intent. Otherwise **choose a default, document it, implement and test**.
- **Gemini role:** implement only one bounded phase at a time, with source diff, tests, physical screenshots and evidence; ChatGPT acts as examiner and approves progression.
- **Verification reality:** Uploaded Task 02A files were reviewed. The user says Task 02B and the UX-C11 research addendum are complete, but those newer report contents and the actual source repository are not available in this response, so no implementation or verification is asserted here.

## 16. Family-by-family coverage (source-audit 50-variant catalog)

The prior audit claims a 50-row variant matrix across 16 core domain types and 19 Live Activity kinds. Those counts are an inventory claim, not 50 demonstrated independent integrations. Every listed family must be mapped into a consistent presentation policy and receive a capability label before release:

| Family (including audited variants) | Compact/idle/default | Expanded behavior / controls | Important exception / device proof |
|---|---|---|---|
| Hidden + Idle/Minimal | Hide if explicitly disabled or privacy/security demands; otherwise ambient capsule | Idle single tap Quick Controls; idle long press settings | Cutout/hide-on-fullscreen; no invisible touch surface |
| One notification, multiple stacked, progress/download, inline reply | App glyph/counter; auto-preview for eligible posts | One detail card or horizontal pager; real actions only; progress updates in place; IME reply | Never cancel OS shade on collapse; pager selected key stable |
| Music compact/expanded | Real media session artwork and playback icon | Play/pause, seek/skip only if supported | Paused/external session changes; maintain notification indicator |
| Ongoing / incoming call / call summary | Persistent call timer and clear incoming call signal | Only verified call actions; summary time-bounded | Not all VoIP/carrier dialers expose action intents; do not promise call interception |
| Charging normal/low battery | Non-persistent HUD or alert; low battery higher salience | Details optional, no fake recharge control | Battery state and threshold debouncing; app cannot replace OEM UI |
| Ringer silent/vibrate/normal + volume HUDs | Brief mode and level transition, coalesced | Only real accessible controls | Respect device settings/permission/DND; avoid repetitive animation |
| Bluetooth connecting/connected | Brief trusted-device indicator | Device detail only when API/permission supplies it | Avoid spoofed pairing flow |
| Quick Controls | Only in response to UX-C01 or explicit user action | Available controls; disabled states explained | Permissions: flashlight etc. may be unsupported/in use |
| Pro/access expiry | User-relevant status only | Explicit non-deceptive entitlement action | Never interrupt active call/reply or masquerade as urgent system UI |
| Timer | Persistent countdown with compact text | Start/pause/reset only for app-owned or explicitly supported timer | Do not claim control of unrelated Clock app timer without valid action |
| Delivery | Compact time/status when reliable | Status detail, track action only when provided | UX-C08–C10 meaningful milestones, first Nearby alert, dedupe |
| Flight | Status/gate/time if notification provides reliable data | Relevant detail or Open App | No live flight data if source doesn't provide it |
| Sports | Team icons/score if reliable update source exists | Match detail/Open App | No invented scores or unofficial feeds |
| Navigation / Transit route | Persistent upcoming instruction if source shares it | Turn/ETA/destination details when available | Avoid pretending to replace Maps turn navigation with inaccessible data |
| Voice memo / screen recording | Accurate persistent indicator | Real app-owned controls only | Never start/stop another app's recording via fake controls |
| Shortcuts runner | Run/progress/success/fail | Open owning app or permitted shortcut action | No unauthorized automation/background action |
| Focus mode | Brief status indicator, may suppress normal previews | Explain state and settings access | Respect Android DND access; no fake control over other apps |
| AirDrop / AirPods / Moved-to-iPhone | Apple-brand-specific notions are **not assumed Android platform capabilities** | Prefer meaningful Android equivalent (Quick Share, Bluetooth earbuds, migration app) when integration actually exists | No fabricated Apple proprietary protocol or service capability |
| Satellite / Find My | Show only actual Android service/status information if publicly available | Safe deep-link to supported service | Never simulate an SOS or emergency request as though performed |
| Video remote / screen mirroring | Status of actual connected session | Supported controls only | Google Cast or OEM capabilities vary |
| Airplane / mobile data alerts | Short status transition | Supported app settings destination, not silent toggle when restricted | Network access may be role/permission dependent |

**Shared action abstraction:** Every card surfaces a `CapabilitySet`: `canOpenApp`, `canReply`, `canDismissOS`, `canMediaControl`, `canCallControl`, `canPauseTimer`, `canShowSensitiveContent`. Renderer and policy must omit—not silently fake—unsupported action affordances. New hardware integrations require their own short verification task and should not block core notification features.

## 17. UI/animation quality bar — Apple-inspired, not stock imitation

### Layout anatomy

**Compact:** Camera-safe matte-black surface, one leading app/activity identity, one trailing actionable status/count, visually continuous with camera cutout. No dense tiny text. Main and companion bubble must be clearly distinguishable by function, not arbitrary decoration.

**Transient preview:** A single fluid morph from compact capsule into a bounded readable card. Header is source app + sender/time, body has one to three useful lines, bottom has zero to two *real* actions. Avoid nested close buttons entering exactly where the user just tapped. Return to the same logical compact activity after timeout.

**User expanded pager:** Stable top anchor/cutout with one card in the main surface; title and app identity fixed in predictable place, page `k of n` clear; subtle page dots when count small and numeric count for 12+. Page changes move content horizontally but do **not** move the camera anchor or reframe entire window needlessly. Expand height only enough to fit content within safe viewport.

**Mini-island stack:** Visually emerge beneath the main, with tight staggered spacing; newest first; smaller than main and progressively less prominent. Child typography and iconography meaningful; avoid chat-like 3-line banners if they would crowd foreground app. Overflow `+N` is a visible count, not a hidden offscreen endless stack.

**Multiple activities:** Prefer one main active activity plus a compact secondary indicator when feasible; do not render 12 equally-important islands. An active ongoing recording/call must not disappear silently because a WhatsApp message arrives.

### Motion design contract

| Transition | Meaning | Motion recommendation (trial, tunable) | Failure mode to test |
|---|---|---|---|
| Idle -> compact data | Establish event identity | 180–280ms size/opacity spring | No tiny badge or lost icon |
| Compact -> transient expanded | Invite glance | 260–420ms continuous geometry/content stagger | New Close button never catches second tap |
| Compact -> user expanded | User owns attention | Interruptible spring with stable hit targets | No gesture dispatch mid-layout change |
| Expanded -> compact | Return focus to underlying app | Reverse trajectory 220–380ms | Original notification still present |
| Main -> mini child emerge | New secondary arrival | Attached bump -> detach -> widen, 220–340ms | No covered button/touch theft |
| Mini insertion reorder | Show newest-first | Older children translate down with spring | No jumping/overlapping card |
| Mini expiry merge | Notification remains but child closes | Reverse translation, fade to main boundary | No queue deletion |
| Mini promotion -> main | Deliberate context switch | Geometry morph/identity handoff; page selection commits once | No duplicated buttons, wrong action recipient |
| Pager horizontal | Browse at controlled speed | Native fling with bounded snapping and overscroll | No accidental vertical collapse |
| Phone critical preemption | Attention only when urgent | Faster controlled morph; suppress cosmetic bounce | Cannot deny call action/source UI |
| Progress/timer update | Information refresh | Numeric/color transitions, no full capsule resize | No battery/jank storm |
| Reduced motion | Equivalence without motion | Short fade and instant stable bounds | Identical actions/accessibility |

**Animation implementation principles:** Motion has *continuity* (same object appears to move), *purpose* (draws attention proportionally), *interruptibility*, *no layout/hitbox races*, *frame-budget awareness*, and *quietness under bursts*. Use spring-based Compose animations where appropriate; native pager must drive gesture animation while content transitions cooperate rather than compete.

### Visual review gates

Capture and compare **real device screenshots** of idle, one message compact, auto preview, expanded 1 message, 12-card pager, main+1 mini, main+3 minis/+N, child promotion, music+notification, active call, inline reply+IME, navigation, 320dp/200% text, 430dp/RTL, left/right cutout, and reduced motion. Geometry must be measured against safe insets, not judged from static mocked mockups alone.
