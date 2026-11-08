# TASK 04B — Test Evidence & Verification Report

**Assigned by:** ChatGPT — Principal Product Architect and Final Examiner  
**Developer:** Gemini  
**Repository:** `Harshil258/DYNAMIC_NOTCH_JETPACK`  
**Base Commit:** `0e51d49288bd77f8fdec0b88924d0518431c6de1`  
**Final Commit SHA:** `8a55f0537ffca769ec77a2a170ba1883425a8af7`  
**Verification Date:** 2026-11-08  

---

## 1. Build Verification Summary

All 4 required Gradle commands executed cleanly without warnings-as-errors or failures:

| Command | Status | Result / Timing |
| :--- | :--- | :--- |
| `./gradlew :app:compileDebugKotlin` | **SUCCESS** | Compiled cleanly in 42s |
| `./gradlew :app:testDebugUnitTest --rerun-tasks` | **SUCCESS** | **467 tests passed, 0 failed, 0 skipped** (1m 3s) |
| `./gradlew :app:assembleDebug` | **SUCCESS** | Debug APK built in 19s |
| `./gradlew :app:assembleDebugAndroidTest` | **SUCCESS** | Instrumented Test APK built in 10s |

---

## 2. Test Suites & Real Test Counts

Total Unit Tests Executed: **467 tests**  
Total Unit Tests Passed: **467 tests**  
Failures: **0**  
Errors: **0**  
Skipped: **0**  

### Key Test Suites Breakdown

1. **`IslandRuntimeIntegrationTest` (20 tests, 0 failures):**
   - `test_01_newNotification_transientPreview_timeout_compact_nextNotification` (PASS)
   - `test_02_twelveNotifications_tap_expandedPager_selectPage5_newMessage_page5RemainsSelected` (PASS)
   - `test_03_dismissOneNotification_minorUpdateSuppressed_majorMilestoneResurfaces` (PASS)
   - `test_04_childTouch_release_oldTimeoutFires_refreshedChildRemains` (PASS)
   - `test_05_activeCallInterruptsNotification_callEnds_correctPresentationRestored` (PASS)
   - `test_06_updateArtwork_nextEvent_artworkAndQueueRemainConsistent` (PASS)
   - `test_07_fiftyNotifications_noSilentLoss_duplicateIdentity_orSelectionCorruption` (PASS)
   - `test_08_playgroundAndProductionEquivalentEvents_equivalentDecisionsAndQueue` (PASS)
   - `test_09_outsideTapAndSwipeUpCollapseNonDestructively_engineStateNotStale` (PASS)
   - `test_10_repeatedSourceRemovalAndUserDismissalEvents_remainIdempotent` (PASS)
   - `test_11_previewEligibility_autoExpandFalse_staysCompactAndOwnershipSystemIdle` (PASS)
   - `test_12_childTimerCleanup_and_overflowAccounting` (PASS)
   - `test_13_deterministic_previewTimeout_collapsesBothEngineAndPresentationWithoutManualCalls` (PASS)
   - **`test_14_productionEntryPoint_fullPreviewLifecycle_singleAuthoritativeScheduler` (NEW, PASS):** Verifies `postNotification(autoExpand=true)` enters `TRANSIENT_PREVIEW`, advances 3500ms virtual time, real timer callback fires `PreviewTimeout`, transitions engine to `COMPACT`, updates rendered state to `isExpanded = false`, and keeps notification queued with zero manual collapse calls.
   - **`test_15_explicitCollapse_cancelsPreviewTimer_and_doesNotDuplicateTimeout` (NEW, PASS):** Verifies user swipe-up / outside-tap cancels the preview timer and does not trigger duplicate callbacks.
   - **`test_16_userExpansion_cancelsPreviewTimer_preservesUserExpandedState` (NEW, PASS):** Verifies user interaction (tapping compact or selecting page) transitions to `PAGER_ACTIVE` / `USER_EXPANDED` and does not auto-collapse when preview timer expires.
   - **`test_17_callInterruption_cancelsPreviewTimer_and_restoresStateAfterCall` (NEW, PASS):** Verifies incoming call preempts preview, cancels timer, and properly restores interrupted notification presentation after call ends.
   - **`test_18_secondIncomingNotification_spawnsChildNotice_mainPreviewTimerCompletes` (NEW, PASS):** Verifies that while preview is active, a second incoming notification becomes a child satellite notice (UX-C11 / A06), and main preview timer completes and merges children.
   - **`test_19_staleTimerCallback_ignored_dueToTokenValidation` (NEW, PASS):** Verifies correlation token check rejects outdated `PreviewTimeout` callbacks.
   - **`test_20_olderHighPriorityOtp_and_newerOrdinaryMessage_pagerOpensOnNewerMessage` (NEW, PASS):** Verifies that when an older OTP (`time = 1000`) and a newer chat message (`time = 2000`) coexist in the queue, the pager opens on the newer chat message (UX-C03), while separate OTP priority handling remains functional.

2. **`NotificationReducerTest` (13 tests, 0 failures):**
   - **`newestFirstDeterminesQueueOrderRegardlessOfPriority` (NEW, PASS):** Verifies that `normal` notification with `time = 20` precedes `priority` notification with `time = 10`.
   - **`deterministicTieBreakingForEqualTimestamps` (NEW, PASS):** Verifies deterministic `id` tie-breaking when timestamps are identical.
   - `repostReplacesTheSameNotification` (PASS)
   - `removalRestoresTheNextNotification` (PASS)
   - `removingAnotherNotificationPreservesExpandedSelection` (PASS)
   - `removingSelectedNotificationChoosesNearestSurvivor` (PASS)
   - `sameSenderWithDifferentAndroidKeysRemainsTwoSources` (PASS)
   - `exactUpdateRemovesActionsNoLongerExposedBySource` (PASS)
   - `dismissActiveRemovesActiveAndAdvancesToNext` (PASS)
   - `queuePagingNextAndPreviousCycles` (PASS)
   - `cappingPreservesActiveNotificationEvenIfOlder` (PASS)
   - `cappingPreservesOngoingWork` (PASS)
   - `cappingEvictsOldestOrdinaryItems` (PASS)

3. **`NotificationAndMediaRealWorldScenarioTest` (15 tests, 0 failures):**
   - **`olderHighPriorityOtpAndNewerOrdinaryMessageCoexistence` (NEW, PASS):** Posts older OTP and newer chat; asserts queue order is `["newer_chat", "older_otp"]`, `activeNotificationId == "newer_chat"`, and `handleTapCompact` selects `newer_chat` while `NotificationPriorityPolicy` and `NotificationInterruptionPolicy` still recognize the OTP as critical.

4. **`NotificationEngineeringRegressionTest` (12 tests, 0 failures):**
   - **`newestArrivalBecomesActiveRegardlessOfImportance` (UPDATED, PASS):** Verifies newest arrival (`time = 100`) becomes active over older arrival (`time = 1`) under UX-C03.

5. **`PlaygroundControllerTest` (6 tests, 0 failures):**
   - All scenarios and multi-queue cases pass.

---

## 3. Compose UI Instrumented Test Verification

- Created [`NotificationPagerIndicatorInstrumentedTest.kt`](file:///Volumes/ALLPROJECTS/KishanAccount/kishan_dynamic_island/dynamic_kishan/app/src/androidTest/java/ai/emots/kishan_dynamic/NotificationPagerIndicatorInstrumentedTest.kt):
  - `multiNotificationPager_showsIndicatorLabel_withoutExtraGesture`: Renders `NotificationIslandExpandedPager` with 2 notifications and verifies the `"1 of 2"` indicator label exists immediately on first composition.
  - `singleNotificationPager_doesNotShowIndicatorLabel`: Renders `NotificationIslandExpandedPager` with 1 notification and asserts that no page indicator label is rendered.
- **Compilation & Packaging:** Verified via `./gradlew :app:assembleDebugAndroidTest`, which successfully compiles and packages the test APK (`BUILD SUCCESSFUL in 10s`).
- **Device Execution Status:** **BLOCKED / NOT RUN** on physical hardware or live emulator, adhering strictly to user guidelines requiring explicit user confirmation before targeting connected devices (`192.168.29.20:5555`).

---

## 4. Reviewable Git Diff

```diff
diff --git a/app/src/androidTest/java/ai/emots/kishan_dynamic/NotificationPagerIndicatorInstrumentedTest.kt b/app/src/androidTest/java/ai/emots/kishan_dynamic/NotificationPagerIndicatorInstrumentedTest.kt
new file mode 100644
index 0000000..9dfbca0
--- /dev/null
+++ b/app/src/androidTest/java/ai/emots/kishan_dynamic/NotificationPagerIndicatorInstrumentedTest.kt
@@ -0,0 +1,72 @@
+package ai.emots.kishan_dynamic
+
+import androidx.compose.ui.test.junit4.createComposeRule
+import androidx.compose.ui.test.onNodeWithText
+import androidx.test.ext.junit.runners.AndroidJUnit4
+import ai.emots.kishan_dynamic.data.model.NotificationInfo
+import ai.emots.kishan_dynamic.ui.components.NotificationIslandExpandedPager
+import ai.emots.kishan_dynamic.ui.theme.AppTheme
+import org.junit.Rule
+import org.junit.Test
+import org.junit.runner.RunWith
+
+@RunWith(AndroidJUnit4::class)
+class NotificationPagerIndicatorInstrumentedTest {
+
+    @get:Rule
+    val compose = createComposeRule()
+
+    private fun sample(id: String, title: String) = NotificationInfo(
+        id = id,
+        packageName = "com.test.$id",
+        appName = "TestApp",
+        title = title,
+        text = "Body for $id",
+        timestamp = System.currentTimeMillis()
+    )
+
+    @Test
+    fun multiNotificationPager_showsIndicatorLabel_withoutExtraGesture() {
+        val notifications = listOf(
+            sample("msg1", "First Message"),
+            sample("msg2", "Second Message")
+        )
+
+        compose.setContent {
+            AppTheme {
+                NotificationIslandExpandedPager(
+                    notifications = notifications,
+                    activeIndex = 0,
+                    isCardExpanded = false, // Must show indicator without card expansion gesture
+                    onActionSelected = { _, _ -> },
+                    onDismiss = {}
+                )
+            }
+        }
+
+        compose.onNodeWithText("TestApp · 1 of 2", substring = true, useUnmergedTree = true).assertExists()
+    }
+
+    @Test
+    fun singleNotificationPager_doesNotShowIndicatorLabel() {
+        val notifications = listOf(
+            sample("single", "Single Message")
+        )
+
+        compose.setContent {
+            AppTheme {
+                NotificationIslandExpandedPager(
+                    notifications = notifications,
+                    activeIndex = 0,
+                    isCardExpanded = false,
+                    onActionSelected = { _, _ -> },
+                    onDismiss = {}
+                )
+            }
+        }
+
+        compose.onNodeWithText("1 of 1", substring = true, useUnmergedTree = true).assertDoesNotExist()
+    }
+}
diff --git a/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt b/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt
index 4fe80a4..e35428c 100644
--- a/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt
+++ b/app/src/main/java/ai/emots/kishan_dynamic/data/notification/IslandBehaviorEngine.kt
@@ -463,6 +463,10 @@ class IslandBehaviorEngine(
 
         // Check selectedKey resolution (UX-C05 / A13 / A14 / A15)
         val wasSelected = state.selectedKey == notificationId
+        val cancelTimerEffect = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW && wasSelected) {
+            listOf(IslandSideEffect.CancelTimer(state.activeToken))
+        } else emptyList()
+        val removeEffects = cancelChildEffect + cancelTimerEffect
 
         if (!wasSelected) {
             // Non-selected item removed: selectedKey stays valid, count decrements (A15)
@@ -491,7 +495,7 @@ class IslandBehaviorEngine(
                     tombstones = updatedTombstones
                 ),
                 action = IslandDomainAction.CollapseToCompact,
-                effects = cancelChildEffect,
+                effects = removeEffects,
                 reason = "final_item_removed_return_idle"
             )
         }
@@ -513,7 +517,7 @@ class IslandBehaviorEngine(
                 tombstones = updatedTombstones
             ),
             action = IslandDomainAction.SelectNextOrPrevious,
-            effects = cancelChildEffect,
+            effects = removeEffects,
             reason = "selected_item_removed_select_neighbor"
         )
     }
@@ -534,6 +538,11 @@ class IslandBehaviorEngine(
             .map { IslandSideEffect.CancelChildTimer(it.key) }
         val updatedTombstones = state.tombstones.filterKeys { it in activeKeys }
 
+        val wasSelected = state.selectedKey != null && state.selectedKey !in activeKeys
+        val cancelTimerEffects = if (state.ownership == IslandInteractionOwnership.TRANSIENT_PREVIEW && wasSelected) {
+            listOf(IslandSideEffect.CancelTimer(state.activeToken))
+        } else emptyList()
+
         val newSelected = if (state.selectedKey in activeKeys) {
             state.selectedKey
         } else {
@@ -555,7 +564,7 @@ class IslandBehaviorEngine(
                 tombstones = updatedTombstones
             ),
             action = IslandDomainAction.KeepSelectedKey,
-            effects = cancelChildEffects,
+            effects = cancelChildEffects + cancelTimerEffects,
             reason = "reconcile_active_sources"
         )
     }
@@ -585,6 +594,7 @@ class IslandBehaviorEngine(
                 IslandBehaviorDecision(
                     state = state.copy(
                         selectedKey = targetKey,
+                        queue = state.queue.copy(activeNotificationId = targetKey),
                         ownership = IslandInteractionOwnership.USER_EXPANDED,
                         targetPresentation = IslandPresentationTarget.USER_EXPANDED
                     ),
@@ -595,10 +605,14 @@ class IslandBehaviorEngine(
             }
             // UX-C03 / A03: >= 2 notifications -> Open horizontal pager, newest first
             else -> {
-                val newestKey = state.queue.notifications.first().id
+                val newestNotification = state.queue.notifications.maxWithOrNull(
+                    compareBy<NotificationInfo> { it.timestamp }.thenBy { it.id }
+                ) ?: state.queue.notifications.first()
+                val newestKey = newestNotification.id
                 IslandBehaviorDecision(
                     state = state.copy(
                         selectedKey = newestKey,
+                        queue = state.queue.copy(activeNotificationId = newestKey),
                         ownership = IslandInteractionOwnership.PAGER_ACTIVE,
                         targetPresentation = IslandPresentationTarget.PAGER
                     ),
diff --git a/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt b/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt
index f5ebafe..2c82de0 100644
--- a/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt
+++ b/app/src/main/java/ai/emots/kishan_dynamic/data/notification/NotificationReducer.kt
@@ -174,11 +174,8 @@ class NotificationReducer {
 
     private fun sortQueue(list: List<NotificationInfo>): List<NotificationInfo> {
         return list.sortedWith(
-            compareByDescending<NotificationInfo> { NotificationPriorityPolicy.isFinancialOrOtp(it) }
-                .thenByDescending { it.isPriority }
-                .thenByDescending { it.importance }
-                .thenByDescending { it.isOngoing }
-                .thenByDescending { it.timestamp }
+            compareByDescending<NotificationInfo> { it.timestamp }
+                .thenByDescending { it.id }
         )
     }
diff --git a/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt b/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt
index b092ae8..c2ad79a 100644
--- a/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt
+++ b/app/src/main/java/ai/emots/kishan_dynamic/service/IslandStateManager.kt
@@ -62,8 +62,11 @@ object IslandStateManager {
         dismissTimerGeneration++
         dismissTimerJob?.cancel()
         dismissTimerJob = null
+    }
+
+    private fun cancelPreviewTimer() {
         previewTimerJob?.cancel()
         previewTimerJob = null
     }
@@ -110,7 +113,7 @@ object IslandStateManager {
         for (effect in effects) {
             when (effect) {
                 is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.StartPreviewTimer -> {
-                    previewTimerJob?.cancel()
+                    cancelPreviewTimer()
                     val token = effect.token
                     previewTimerJob = scope.launch {
                         delay(effect.durationMs)
@@ -122,6 +125,9 @@ object IslandStateManager {
                                 if (current is IslandState.Notification && current.isExpanded && !current.expandedByUser) {
                                     _currentState.value = current.copy(isExpanded = false)
                                     notificationAutoExpand = false
                                 }
+                                if (!hasQueuedCriticalNotification()) {
+                                    restoreCriticalInterruptedPresentation()
+                                }
                             }
                         }
                     }
@@ -145,8 +151,7 @@ object IslandStateManager {
                     childTimerJobs.remove(effect.childKey)
                 }
                 is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.CancelTimer -> {
-                    previewTimerJob?.cancel()
-                    previewTimerJob = null
+                    cancelPreviewTimer()
                 }
                 is ai.emots.kishan_dynamic.data.notification.IslandSideEffect.CancelAndroidNotification -> {
@@ -191,6 +196,7 @@ object IslandStateManager {
     fun resetForTesting() {
         synchronized(stateLock) {
             cancelDismissTimer()
+            cancelPreviewTimer()
             cancelPausedMusicTimer()
@@ -508,7 +514,6 @@ object IslandStateManager {
             pendingCallSummary = null
             cancelDismissTimer()
             resumeBaseState()
-            scheduleNotificationExpiry()
         }
     }
@@ -723,6 +728,7 @@ object IslandStateManager {
                 expandedByUser = true
             )
             cancelDismissTimer()
+            cancelPreviewTimer()
         }
     }
@@ -730,7 +736,6 @@ object IslandStateManager {
         synchronized(stateLock) {
             dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.NextPage)
             renderNotificationSurface()
-            scheduleNotificationExpiry()
         }
     }
@@ -738,7 +743,6 @@ object IslandStateManager {
         synchronized(stateLock) {
             dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviousPage)
             renderNotificationSurface()
-            scheduleNotificationExpiry()
         }
     }
@@ -1563,7 +1567,6 @@ object IslandStateManager {
                 val expanded = _currentState.value as? IslandState.Notification
                 if (expanded == null || notificationQueue.notifications.isEmpty()) {
                     resumeBaseState()
-                    scheduleNotificationExpiry()
                 } else {
                     _currentState.value = expanded.copy(
@@ -1574,7 +1577,6 @@ object IslandStateManager {
             NotificationPresentationDecision.UPDATE_NOTIFICATION_QUEUE -> {
                 if (notificationQueue.notifications.isEmpty()) {
                     resumeBaseState()
-                    scheduleNotificationExpiry()
                 } else {
                     when (val current = _currentState.value) {
@@ -1597,7 +1599,6 @@ object IslandStateManager {
             NotificationPresentationDecision.PRESENT -> {
                 cancelDismissTimer()
                 renderNotificationSurface()
-                scheduleNotificationExpiry()
             }
         }
     }
@@ -1629,6 +1630,10 @@ object IslandStateManager {
         if (notificationVisibility.markSeen(notificationId)) {
             _unseenNotificationCount.value = notificationVisibility.unseenCount()
         }
     }
+
+    private fun scheduleNotificationExpiry() {
+        // No-op: Notification preview expiry is solely and authoritatively driven
+        // by IslandBehaviorEngine's StartPreviewTimer effect via previewTimerJob.
+    }
diff --git a/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt b/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt
index d7a52f2..8a4e8d3 100644
--- a/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt
+++ b/app/src/main/java/ai/emots/kishan_dynamic/ui/components/NotificationIsland.kt
@@ -504,7 +504,12 @@ fun NotificationStandardCard(
             Column(
                 modifier = Modifier.fillMaxWidth().wrapContentHeight()
                     .onSizeChanged { size -> with(density) { onHeightChange?.invoke(size.height.toDp()) } }
-                    .padding(AppTheme.island.expandedContentInset),
+                    .padding(
+                        start = AppTheme.island.expandedContentInset,
+                        end = AppTheme.island.expandedContentInset,
+                        top = AppTheme.island.expandedContentInset,
+                        bottom = if (pagerLabel != null) AppTheme.island.expandedContentInset + 14.dp else AppTheme.island.expandedContentInset
+                    ),
                 verticalArrangement = Arrangement.spacedBy(AppTheme.island.expandedContentGap)
             ) {
@@ -666,7 +671,12 @@ fun NotificationNavigationCard(
                     with(density) {
-                        onHeightChange?.invoke(size.height.toDp() + (if (pagerLabel != null) 12.dp else 0.dp))
+                        onHeightChange?.invoke(size.height.toDp())
                     }
                 }
-                .padding(horizontal = hPad, vertical = vPad),
+                .padding(
+                    start = hPad,
+                    end = hPad,
+                    top = vPad,
+                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
+                ),
@@ -779,7 +789,12 @@ fun NotificationTransportCard(
                     with(density) {
-                        onHeightChange?.invoke(size.height.toDp() + (if (pagerLabel != null) 12.dp else 0.dp))
+                        onHeightChange?.invoke(size.height.toDp())
                     }
                 }
-                .padding(horizontal = hPad, vertical = vPad),
+                .padding(
+                    start = hPad,
+                    end = hPad,
+                    top = vPad,
+                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
+                ),
@@ -899,7 +914,12 @@ fun NotificationProgressCard(
                     with(density) {
-                        onHeightChange?.invoke(size.height.toDp() + (if (pagerLabel != null) 12.dp else 0.dp))
+                        onHeightChange?.invoke(size.height.toDp())
                     }
                 }
-                .padding(horizontal = hPad, vertical = vPad),
+                .padding(
+                    start = hPad,
+                    end = hPad,
+                    top = vPad,
+                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
+                ),
@@ -1674,8 +1694,8 @@ fun NotificationIslandExpandedPager(
         // Pager Indicator Dots (authentic iOS UIPageControl style with sliding window)
-        // Rendered exclusively when card is expanded with reserved bottom clearance below action pills
-        if (isCardExpanded && notifications.size > 1 && replyingNotificationId == null) {
+        // Displayed for multi-notification expanded pager (2+ notifications) without requiring extra gesture
+        if (notifications.size > 1 && replyingNotificationId == null) {
             val totalPages = notifications.size
             val maxVisibleDots = 7
             val (startIndex, endIndex) = if (totalPages <= maxVisibleDots) {
@@ -1686,6 +1706,8 @@ fun NotificationIslandExpandedPager(
                 start to (start + maxVisibleDots - 1)
             }
 
+            val reducedMotion = ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion.current
+
             Row(
                 modifier = Modifier
@@ -1697,22 +1719,47 @@ fun NotificationIslandExpandedPager(
                 for (i in startIndex..endIndex) {
                     val isCurrent = i == currentPage
                     val isEdge = (i == startIndex && startIndex > 0) || (i == endIndex && endIndex < totalPages - 1)
-                    val dotSize = when {
+                    val targetDotSize = when {
                         isCurrent -> 6.5.dp
                         isEdge -> 3.5.dp
                         else -> 5.dp
                     }
-                    val alpha = when {
+                    val targetAlpha = when {
                         isCurrent -> 1f
                         isEdge -> 0.22f
                         else -> 0.45f
                     }
+
+                    val dotSize by androidx.compose.animation.core.animateDpAsState(
+                        targetValue = targetDotSize,
+                        animationSpec = if (reducedMotion) {
+                            androidx.compose.animation.core.snap()
+                        } else {
+                            androidx.compose.animation.core.spring(
+                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
+                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
+                            )
+                        },
+                        label = "dotSize_$i"
+                    )
+                    val dotAlpha by androidx.compose.animation.core.animateFloatAsState(
+                        targetValue = targetAlpha,
+                        animationSpec = if (reducedMotion) {
+                            androidx.compose.animation.core.snap()
+                        } else {
+                            androidx.compose.animation.core.spring(
+                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
+                            )
+                        },
+                        label = "dotAlpha_$i"
+                    )
+
                     Box(
                         modifier = Modifier
                             .padding(horizontal = 2.5.dp)
                             .size(dotSize)
                             .clip(CircleShape)
-                            .background(Color.White.copy(alpha = alpha))
+                            .background(Color.White.copy(alpha = dotAlpha))
                     )
                 }
             }
```
