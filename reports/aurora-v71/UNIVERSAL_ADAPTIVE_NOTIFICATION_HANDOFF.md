# Universal Adaptive Notification Island — Final Handoff (Aurora V7.1)

Production app: `com.island.dynamicnotch` · Branch: `codex/aurora-v7-1-notification-height-swipe-review`
Device evidence: Realme RMX3085 (Android 13, 1080×2400 @480dpi = 360×800dp), plus Compose viewport overrides 320/360/393/430dp × font 1.0/1.3/1.5/2.0 × LTR/RTL.

## 1. What was built

The Island now renders **any Android notification** through one universal, content-adaptive renderer instead of per-type bespoke layouts:

```
IslandNotificationListener (intake: title/text/bigText, EXTRA_MESSAGES, pictures,
    progress, chronometer, actions, group/conversation flags, privacy redaction)
  → IslandBehaviorEngine (canonical queue, selection, mini-island lifecycle)
  → IslandStateManager → IslandOverlayService → DynamicIslandPill
  → NotificationIslandExpandedPager (horizontal paging, height morph, rail)
  → AdaptiveNotificationCard  ← single universal renderer (NotificationCard.kt)
  → NotificationLayoutPolicy  ← recipe selection from available content
```

`NotificationLayoutPolicy.recipe()` (app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationLayoutPolicy.kt:61) picks the first matching recipe:

| Recipe | Trigger | Verified device height @360dp |
|---|---|---|
| PROGRESS | progress extras present | 171dp (63%), 115dp (indeterminate) |
| TIMER | chronometer/countdown | 199.7dp |
| NAVIGATION | nav-style ongoing + text | 178dp |
| THUMBNAIL_ALERT | small picture + conversation/text | 126dp (Figma 27:992 parity) |
| LARGE_MEDIA | big picture / media style | 351dp |
| GROUP_SUMMARY | group summary flag | 184.7dp |
| CONVERSATION | MessagingStyle people/messages | 281.7dp |
| INBOX_DIGEST | inbox-style multi-line | 322dp |
| TEXT_ALERT | fallback for everything else | 86dp short → 379dp long copy |

Unknown/empty sources fall back to TEXT_ALERT with app icon + app name; an entirely empty payload renders the clean 86dp fallback (capture `12-empty-fallback`). No recipe produces empty space, overlap, or clipped text at any tested viewport/scale: content wraps, actions stack vertically below 240dp content width or above 1.3 font scale, and every interactive target keeps ≥48dp.

Shared foundation reused across Minimal/Compact/Expanded/Notifications/Calls/Music/Live Activities: `AppTheme` tokens, `IslandFamilyGeometry` (367dp family canvas, 86dp expanded floor, corner radii), `IslandActionSystem` pills, `AppleIcon` glyph set, `LayeredMiniIslands` satellites.

## 2. Defects found and fixed during device verification

1. **Pager settle-commit race (P1).** The settle collector consumed the gesture flag on the first `isScrollInProgress == false` emission, which can be the momentary gap between drag-end and fling-start. It committed an *intermediate* page and swallowed the final settle, leaving the engine's `selectedKey`/`activeIndex` diverged from the visible page — the selected notification then rendered both expanded and as a mini satellite. Fix: a 120ms quiet-window debounce before consuming the flag (`NotificationIsland.kt:64`, `NotificationIsland.kt:425-441`); the final settled page now commits exactly once per gesture. Regression: `LayeredMiniIslandsInstrumentedTest.verticalPagerRemainsInteractiveWithChildren` asserts engine selection, `queue.activeIndex`, pager `stateDescription` and mini-stack exclusion agree after every swipe, across four consecutive gestures.
2. **Rail touch dead-zone (P1).** The indicator rail reserve was applied as `padding` on the `HorizontalPager` itself, so swipes starting under the count/dots rail never reached the pager (default test `swipeLeft` starts at the node's right edge and was a silent no-op). Fix: the pager hit region now spans the full island width; the rail reserve pads each page's *content* instead (`NotificationIsland.kt:504`, `NotificationIsland.kt:516`). Visual contract unchanged; the whole surface is swipeable.
3. **Expiry paywall clipping at 200% font (P1).** Fixed-height 38dp capsules and an always-side-by-side action row clipped labels at large font/RTL. Fix: growable capsules (`heightIn(min=38dp)` inside 48dp touch targets), vertical stacking below 240dp or above 1.3 scale, subtitle no longer single-line (`ActionControlIsland.kt`, shared `PremiumExpiryButton`). Verified legible at 320dp × 2.0 × RTL.
4. **Stale layout assertion.** `IslandLayoutInstrumentedTest` asserted a fixed 21dp gutter that contradicted the 367dp family canvas at 320/360dp; replaced with the `min(367, width)` canvas contract from `IslandFamilyGeometry`.
5. **Capture paths vs. Realme permissions.** `appops`/`pm grant`/external-storage writes are blocked on this OEM; all capture tests write to app `filesDir` and are pulled via `run-as tar`, so evidence collection needs no privileged grants.
6. **Call summary ellipsis regression (P1).** The V6 call-summary redesign reintroduced `maxLines = 1` + ellipsis on the caller name and subtitle, clipping long contact identities at 320dp × 200% — the exact defect `CallSummaryLayoutInstrumentedTest.callerAndDurationRemainCompleteAtLargeType` guards. Restored free wrapping in `CallSummaryIsland.kt` (wrap-not-truncate contract); duration stays below the wrapped name via `IslandAdaptiveHeader`.
7. **Stale playground expectations.** The workbench tests asserted the pre-redesign expiry title "Aurora Pro access" (now "Aurora Pro") and a 140dp minimum-width override that no longer widens the 189dp-preferred compact capsule; updated to the current copy and a 240dp override (below the 250dp compact cap).
8. **Stale template assertion.** `ExampleInstrumentedTest` asserted the source package instead of the real `applicationId` (`com.island.dynamicnotch`); corrected.
9. **False "stale pager page" failure (test expectation).** `AuroraFigmaDetailInstrumentedTest` updated only `title` on the selected source and expected the new headline. Conversation sources headline from `conversationTitle` (`NotificationLayoutPolicy.headline`, `NotificationLayoutPolicy.kt:134-138`), and the Figma fixture's selected item is a conversation titled "Mamad" — so the unchanged headline was correct behavior, not a frozen composition. Device telemetry confirmed the pager was on the right page with the right count. Fix: the test now updates the fields the card actually renders (`title`, `conversationTitle`, `text`), asserts both the new headline and body appear, and asserts the old headline is gone — a real staleness guard. Added JVM lock: `headline_prefersConversationTitleOnlyForConversations` now asserts a title-only change cannot alter a conversation headline.
10. **Benchmark timeout under load.** `LayeredMiniIslandsAnimationBenchmarkTest` used a flat 60s latch for a ~39s choreographed 12-cycle sequence; it expired during the full marathon on a loaded/thermally-throttled device. Fix: wait on reported cycle progress with a 30s no-progress stall detector and a 180s ceiling, so load no longer produces false failures while a genuine hang still fails fast. The ≥500-frame performance gate is unchanged.

## 3. Test results

### JVM unit suite (final code)
`./gradlew testDebugUnitTest` → **588 tests, 0 failures, 0 errors, 0 skipped** (92 suites), including `AuroraV71NotificationPagerTest` (H01–H22 height/morph/pager invariants) and `NotificationLayoutPolicyTest` (recipe selection, action typing, height estimation).

### Instrumented suite on device (final code)
Run via `installDebug installDebugAndroidTest` + `am instrument -w` (Jugg `enabledAndroidTest=false`; Gradle connected tasks uninstall APKs post-run on this setup, so direct instrumentation is used).

<!-- INSTRUMENTED_TOTALS -->

### Responsive measurement matrix
`AuroraV43GalleryTest.captureEveryStateAndResponsiveBounds`: 60 states × 5 viewports × 4 font scales = **1200 measurements** (`device/aurora-v43/measurements.csv`), zero overflow, zero empty-shell rows; every state respects the 86dp floor and 367dp canvas.

### Real-world adaptive captures
`AdaptiveNotificationCaptureTest`: **27 captures** (15 content modes, 12 also in a 3-notification queue) with `measurements.json`; minimum height 86dp, Figma thumbnail exactly 126dp, big-picture 351dp, long-copy 379dp — all bounded by screen height minus 64dp.

### Visual inventory
`VisualInventoryCaptureTest`: 60 state captures, 20 scenario captures, layered mini-island frames, accessibility/scaling set, 21 in-app screen captures (291 PNGs under `device/visual-redesign/`), plus `11-measurements/state-measurements.json` (60 entries with pixel+dp bounds).

## 4. Screenshot index (relative to this file)

| Evidence | Path |
|---|---|
| Figma 27:992 thumbnail parity | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/00-figma-thumbnail-crop.png` |
| Short text 86dp floor | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/01-short-text-crop.png` |
| Inbox digest multi-message | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/04-inbox-digest-crop.png` |
| Download progress 63% | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/05-download-63-crop.png` |
| Big picture media | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/10-big-picture-crop.png` |
| Conversation (MessagingStyle) | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/11-conversation-crop.png` |
| Empty/unknown fallback | `universal-adaptive/device/visual-redesign/06-adaptive-realworld/12-empty-fallback-crop.png` |
| Pager swipe, no duplicate mini | `universal-adaptive/device/task05-evidence/pager-swipe-with-children.png` |
| Return swipe to main | `universal-adaptive/device/task05-evidence/pager-return-to-main.png` |
| Mini stack emergence/merge frames | `universal-adaptive/device/task05-evidence/emerging-frame.png`, `merge-frame.png` |
| 25-arrival overflow satellite | `universal-adaptive/device/task05-evidence/overflow-25-arrivals.png` |
| 200% font RTL + redaction | `universal-adaptive/device/task05-evidence/narrow-large-text-rtl.png`, `narrow-large-text-redaction.png` |
| Expiry 200% RTL legibility | `universal-adaptive/device/island-layout-qa/expiry_320.png` |
| 60-state gallery sample | `universal-adaptive/device/aurora-v43/` (120 PNGs + measurements.csv) |

## 5. Approved behaviors preserved

Horizontal notification paging with key-stable pages and single settle commit; Mini-Islands emergence/promotion/merge with retained exit visuals and overflow counting; call lifecycle states; swipe gestures (now full-surface); inline reply with per-source draft persistence and gesture ownership; lock-screen privacy redaction; reduced-motion snapping; 48dp touch targets; accessibility pager actions ("Previous/Next notification") and `stateDescription` announcements.

## 6. Limitations and disclosures

- **Evidence tiers.** State/scenario/adaptive captures are Compose fixtures rendered on the real device (`DEVICE_COMPOSE`), not live third-party notifications over the overlay. Live-overlay behavior is exercised by `LayeredMiniIslandsOverlayTest` and manual device runs; OEM status-bar icon compatibility remains a device-acceptance item.
- **MessagingStyle on SDK stubs.** Instrumented fixtures cannot construct real `Notification.MessagingStyle` parcels; conversation intake is verified through `EXTRA_MESSAGES`/people extras paths in `IslandNotificationListener` and JVM policy tests. Real-app conversation rendering should be spot-checked on a device with live WhatsApp/Telegram traffic.
- **Device constraints.** Realme blocks `appops`, `pm grant`, `svc power stayon`, `settings put`; screen sleep kills Compose test hierarchies, so runs use a 2s keep-awake heartbeat and `filesDir` captures pulled with `run-as`. Gradle connected tasks uninstall APKs afterwards; direct `am instrument` is the reliable path.
- **Jugg/HotSwan.** `enabledAndroidTest=false` → Gradle/adb fallback; HotSwan extraction adds ~60s per build; an earlier D8 dev-only crash required a full `gradle-build` fallback (resolved).
- **Queue order.** Newest-first queue places the oldest/main notification on the last page; swiping right moves toward newer arrivals. This is the approved intake order, not a defect.
- **Settle grace.** The 120ms commit debounce adds ≤120ms latency between visual settle and engine selection; imperceptible in use and required to reject the drag→fling handoff gap.

## 7. Reproduction

```bash
./gradlew testDebugUnitTest                      # 588 JVM tests
./gradlew installDebug installDebugAndroidTest   # device APKs (no post-run uninstall)
adb shell am instrument -w com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner
adb exec-out run-as com.island.dynamicnotch tar -cf - -C files visual-redesign task05-evidence island-layout-qa | tar -xf - -C reports/aurora-v71/universal-adaptive/device
```