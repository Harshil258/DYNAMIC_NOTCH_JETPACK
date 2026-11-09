# V4.3 behavior, scenarios and personas

Owner UX-C01–C15 and architect PD-001 remain the behavior authority. Existing engine,
reducer, suppression registry, canonical50 queue, keyed pager and child renderer retained.
PreviewReadable(token) starts the engine timer only after renderer settled frames;
stale/duplicate acknowledgements are ignored. Engine call restoration filters surviving
sources and expired child deadlines. These changes require passing regression proof.

Personas evaluated: quick glance (readable-start), focused reader (stable keys/drafts),
heavy user (queue/overflow), multitasker (call/media arbitration), power user
(Quick Controls/gestures), privacy/accessibility (redaction/RTL/large text).
Full persona acceptance remains PARTIAL until the device and integration gates pass.

| Scenario | Initial | Event | Required verification | Evaluation |
|---|---|---|---|---|
| 1 | Idle, 0 notif | tap | UNIT+DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 2 | 1 notif compact | tap | UNIT+VISUAL | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 3 | 12 notif | tap | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 4 | pager open, selected key `msg-5` at position 5 | 13th arrives, inserted ahead | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 5 | pager open | selected source removed | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 6 | expanded | × | UNIT+DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 7 | last Island notification, children pending | **source removes** it, no live activity | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 8 | dismissed key | minor update | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 9 | dismissed delivery, **Main compact** | major milestone (meaningful) | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 10 | main expanded | 1 new | UNIT+VISUAL | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 11 | main expanded | 3 new | UNIT+VISUAL | NOT RUN — full scenario requires separate execution |
| 12 | idle | burst 25 | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 13 | children + queue 12 | inspect `+N` | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 14 | child visible | tap child | UNIT+VISUAL | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 15 | child TOUCH_HELD | stale timeout fires | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 16 | main expanded + 2 visible children | **deliberate** user collapse (PD-005) | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 17 | pager open, selected key `msg-5` | incoming ringing call | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 18 | call ends **inside** the bounded window (≈10 s), unlocked, source still valid | restore | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 19 | music **expanded by the user** | ordinary notification | UNIT+VISUAL | NOT RUN — full scenario requires separate execution |
| 20 | nav compact | turn-now during preview | UNIT | NOT RUN — full scenario requires separate execution |
| 21 | reply editor open | notif arrives | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 22 | child emerging | IME opens | DEVICE | NOT RUN — full scenario requires separate execution |
| 23 | child PROMOTING | source disappears | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 24 | locked | sensitive notif | DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 25 | expanded | notification access revoked | DEVICE | BLOCKED — production service/device access unavailable |
| 26 | running | accessibility service stopped/restarted | DEVICE | BLOCKED — production service/device access unavailable |
| 27 | any | fontScale 2.0 | VISUAL+DEVICE | NOT RUN — full scenario requires separate execution |
| 28 | RTL | nav + pager use | VISUAL+DEVICE | NOT RUN — full scenario requires separate execution |
| 29 | 320 dp / low available height | children arrive and **neither a child card nor an overflow chip fits safely** | DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 30 | overlay up | touch **outside the overlay window rectangle** (§3.1 class a) | DEVICE (BLOCKED until hw evidence) | BLOCKED — production service/device access unavailable |
| 31 | mid-morph | user tap | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 32 | expanded action | PendingIntent invalid | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 33 | notif without RemoteInput | reply UI | UNIT | NOT RUN — full scenario requires separate execution |
| 34 | DND on | ordinary notif | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 35 | active call | charging HUD | UNIT | NOT RUN — full scenario requires separate execution |
| 36 | recording | ordinary notif | UNIT+VISUAL | NOT RUN — full scenario requires separate execution |
| 37 | pager reading key `msg-5` | same-key artwork/content update | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 38 | queue 50 | 51st arrives | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 39 | Playground | engine callbacks | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 40 | revoked+restored | process restoration | DEVICE | BLOCKED — production service/device access unavailable |
| 41 | Quick Glance persona | preview expiry | UNIT+VISUAL | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 42 | reply editor open, unsent draft | ringing call arrives | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 43 | Heavy persona | 25-burst then prune | UNIT | NOT RUN — full scenario requires separate execution |
| 44 | Multitask persona | music+nav+timer+call sequence | UNIT+VISUAL | NOT RUN — full scenario requires separate execution |
| 45 | Power persona | swipe-up on idle/compact + config variants | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 46 | Privacy persona | lock + OTP + TalkBack | DEVICE | NOT RUN — full scenario requires separate execution |
| 47 | one-handed | child promotion via a11y action | DEVICE | NOT RUN — full scenario requires separate execution |
| 48 | split-screen/landscape | any presentation | DEVICE | NOT RUN — full scenario requires separate execution |
| 49 | music **compact**, user not interacting | eligible ordinary message (PD-026) | UNIT+VISUAL | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 50 | music compact, **auto-preview disabled or DND/privacy suppressing** | eligible ordinary message | UNIT+DEVICE | NOT RUN — full scenario requires separate execution |
| 51 | music compact, **user actively interacting** (scrub/transport/output) | ordinary message | UNIT | NOT RUN — full scenario requires separate execution |
| 52 | music **expanded by user** | 3 ordinary messages | UNIT+VISUAL | NOT RUN — full scenario requires separate execution |
| 53 | main expanded + 2 children, one `TOUCH_HELD` | **deliberate collapse during child touch/animation** | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 54 | reply editor with unsent draft | **outside tap / Back** | UNIT+DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 55 | reply editor open → ringing call | call ends **after** the ≈10 s bounded window | UNIT | NOT RUN — full scenario requires separate execution |
| 56 | reply editor open → ringing call | call ends inside window but the notification was **removed** or **privacy state changed** | UNIT | NOT RUN — full scenario requires separate execution |
| 57 | Main **compact**, 12 queued | meaningful delivery milestone resurfaces (C10) | UNIT | NOT RUN — full scenario requires separate execution |
| 58 | suppressed source | **routine** stop decrement / ETA jitter | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 59 | preview content slow to render | preview timer evaluation | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 60 | user-expanded notification | user reads for 90 s | UNIT | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 61 | overlay up | touch **inside the window rectangle but in a transparent gutter** (§3.1 class b) | DEVICE (BLOCKED until hw evidence) | NOT RUN — full scenario requires separate execution |
| 62 | **unlocked** device, privacy setting/classification requires redaction | OTP or financial notification arrives | UNIT+DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 63 | `canDismissOS` false (listener unbound or notification not clearable) | × action | UNIT+DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |
| 64 | low available height, children pending | no child card and no overflow chip fits | DEVICE | PARTIAL — related JVM regression passes; full scenario remains unverified |


## Related JVM evidence by scenario

Passing a related regression below does not establish the full device/visual scenario.

| Scenario | Related executed regression | Result |
|---|---|---|
| 1 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A01_idleZeroActiveItemsTap_opensQuickControls | PASS (JVM scope only) |
| 2 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A02_compactOneMessageTap_expandsNotification_osUntouched | PASS (JVM scope only) |
| 3 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A03_compactTwelveMessagesTap_opensHorizontalPager_newestFirst | PASS (JVM scope only) |
| 4 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A08_userExpandedPagerPageFive_newNormalMessage_selectedKeyPinned_childEmerges | PASS (JVM scope only) |
| 5 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A13_sourceRemovesCurrentlySelectedMiddlePagerItem_nextNeighborSelected_remainsExpanded | PASS (JVM scope only) |
| 6 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A17_userClicksCloseButton_defaultSetting_islandOnlyDismiss_noOsCancel | PASS (JVM scope only) |
| 7 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A16_sourceRemovesFinalEligibleItem_noActivity_idleCompact_tapQuickControls | PASS (JVM scope only) |
| 8 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A21_userIslandDismissesSameKey_progressSixtyToSeventy_suppressed | PASS (JVM scope only) |
| 9 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A22_userIslandDismissesDelivery_firstTwoStopsAway_resurfacesOnce | PASS (JVM scope only) |
| 10 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A06_transientPreviewReading_secondArrives_childEmerges_mainPreviewStays | PASS (JVM scope only) |
| 11 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 12 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A40_burstOfTwentyFiveNotifications_queueBoundedAndSafe | PASS (JVM scope only) |
| 13 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.oldQueueIsNotPendingOverflow | PASS (JVM scope only) |
| 14 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.promotionSelectsExactlyOneExistingPagerIdentity | PASS (JVM scope only) |
| 15 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.expiringSiblingDoesNotUnpauseHeldChildOrCancelAndroid | PASS (JVM scope only) |
| 16 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.replyAndCollapsePreserveQueueAndNeverEmitAndroidCancellation | PASS (JVM scope only) |
| 17 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 18 | ai.emots.kishan_dynamic.data.notification.CallChildRestorationTest.callRestoresSelectedReaderWithoutExtendingChildDeadline | PASS (JVM scope only) |
| 19 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 20 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 21 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 22 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 23 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.stalePromotionAndReusedKeyCannotSelectRemovedSource | PASS (JVM scope only) |
| 24 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.privacyProjectionNeverExposesSensitiveText | PASS (JVM scope only) |
| 25 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 26 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 27 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 28 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 29 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.zeroHeightOrConstrainedScreenCollapsesChildPresentationWithoutDanglingOverflow | PASS (JVM scope only) |
| 30 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 31 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 32 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 33 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 34 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 35 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 36 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 37 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A11_userExpandedPager_sameKeyMinorUpdate_inPlaceUpdate_noNewPageOrChild | PASS (JVM scope only) |
| 38 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.burstBoundedAtCanonicalCapacityWithNoDuplicateKeys | PASS (JVM scope only) |
| 39 | ai.emots.kishan_dynamic.playground.PlaygroundControllerTest.layoutObservationCannotReplaceAnotherSelectedFixture | PASS (JVM scope only) |
| 40 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 41 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A05_compactOneMessage_autoPreviewCompletes_compactBadgeRetained_noOsCancellation | PASS (JVM scope only) |
| 42 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 43 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 44 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 45 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 46 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 47 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 48 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 49 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_C05_ongoingMusicAndNormalMessage_preservesMusicAndShowsPreview | PASS (JVM scope only) |
| 50 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 51 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 52 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 53 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.replyAndCollapsePreserveQueueAndNeverEmitAndroidCancellation | PASS (JVM scope only) |
| 54 | ai.emots.kishan_dynamic.data.notification.ReplyDraftStoreTest.sourceAndActionIdentityRetainDraftOnlyInsideMonotonicWindow | PASS (JVM scope only) |
| 55 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 56 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 57 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 58 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A23_sameDelivery_subsequentStopDecrement_notRepeatedlyResurrected | PASS (JVM scope only) |
| 59 | ai.emots.kishan_dynamic.data.notification.PreviewReadableTest.slowRenderReceivesFullLifetimeAndAcknowledgementIsIdempotent | PASS (JVM scope only) |
| 60 | ai.emots.kishan_dynamic.data.notification.PreviewReadableTest.deliberateReaderHasNoShortExpiryAfterNinetySeconds | PASS (JVM scope only) |
| 61 | No direct mapped JVM test | NOT RUN / requires separate evidence |
| 62 | ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicyTest.unlockedOtpAndFinanceRespectSensitivePreviewPreference | PASS (JVM scope only) |
| 63 | ai.emots.kishan_dynamic.data.notification.IslandBehaviorEngineTest.test_A19_userClicksCloseButton_osDismissChosen_nonClearable_fallbackIslandOnly | PASS (JVM scope only) |
| 64 | ai.emots.kishan_dynamic.data.notification.LayeredMiniIslandsTest.zeroHeightOrConstrainedScreenCollapsesChildPresentationWithoutDanglingOverflow | PASS (JVM scope only) |
