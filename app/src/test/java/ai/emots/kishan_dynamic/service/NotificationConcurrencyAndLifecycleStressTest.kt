package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.MusicTrack
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.RingerModeType
import ai.emots.kishan_dynamic.data.notification.NotificationArtworkRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Automated Stress Test Suite for Sprint 02:
 * Covers Scenarios A through J:
 * - Scenario A: Burst processing (25 notifications posted rapidly)
 * - Scenario B: Concurrent state mutations (multi-threaded stress)
 * - Scenario C: Late image result discarded after notification removal
 * - Scenario D: Notification update replacement in-place without reordering
 * - Scenario E: Call interruption and restoration of base state
 * - Scenario F: Process lifecycle / state reset (`resetForTesting`)
 * - Scenario G: Source reconciliation (`reconcileNotificationSources`)
 * - Scenario H: Media restoration upon notification expiry
 * - Scenario I: Resource pressure / bounded artwork cache
 * - Scenario J: Rapid user interactions (swipe dismiss, expand, collapse)
 */
class NotificationConcurrencyAndLifecycleStressTest {

    private fun createNotification(
        id: String,
        pkg: String = "com.test.app",
        title: String = "Title $id",
        text: String = "Message $id",
        isPriority: Boolean = false,
        time: Long = System.currentTimeMillis()
    ) = NotificationInfo(
        id = id,
        packageName = pkg,
        appName = "TestApp",
        title = title,
        text = text,
        timestamp = time,
        isPriority = isPriority
    )

    private fun createTrack(title: String = "Test Song", artist: String = "Test Artist") =
        MusicTrack(title = title, artist = artist, packageName = "com.spotify.music")

    private val IslandState.Notification.activeNotification: NotificationInfo?
        get() = notifications.getOrNull(activeIndex)

    @Before
    fun setUp() {
        IslandStateManager.resetForTesting()
    }

    @After
    fun tearDown() {
        IslandStateManager.resetForTesting()
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario A: Burst processing (25 notifications posted rapidly)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioA_burstProcessingTwentyFiveNotificationsRemainsBoundedAndSafe() {
        // Post 25 notifications in rapid burst
        for (i in 1..25) {
            val notification = createNotification(id = "burst_$i", text = "Burst notification #$i", time = i.toLong())
            IslandStateManager.postNotification(notification, autoExpand = false, displaySeconds = 5)
        }

        val currentState = IslandStateManager.currentState.value
        assertTrue(
            "State should be Notification or NotificationWithMusic, was: $currentState",
            currentState is IslandState.Notification || currentState is IslandState.NotificationWithMusic
        )

        val queuedIds = IslandStateManager.notificationSourceIds()
        assertTrue("Queue must not exceed bounded max limit (20), actual size: ${queuedIds.size}", queuedIds.size <= 20)
        assertEquals("Queue should be capped at exactly 20 items", 20, queuedIds.size)

        val notifications = when (currentState) {
            is IslandState.Notification -> currentState.notifications
            is IslandState.NotificationWithMusic -> currentState.notifications
            else -> emptyList()
        }
        assertTrue("Rendered notifications must be <= 20", notifications.size <= 20)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario B: Concurrent state mutations (multi-threaded stress)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioB_concurrentMultiThreadedStateMutationsDoNotThrowOrCorruptState() {
        val threadCount = 10
        val operationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val errorRef = AtomicReference<Throwable?>(null)
        val completedOps = AtomicInteger(0)

        for (t in 0 until threadCount) {
            executor.execute {
                try {
                    startLatch.await()
                    for (i in 0 until operationsPerThread) {
                        val notifId = "thread_${t}_notif_$i"
                        when (i % 8) {
                            0 -> IslandStateManager.postNotification(
                                createNotification(id = notifId, text = "Msg $i"),
                                autoExpand = false,
                                displaySeconds = 3
                            )
                            1 -> IslandStateManager.updateNotificationArtwork(
                                notifId,
                                imagePath = "/cache/avatar_$notifId.jpg",
                                picturePath = null
                            )
                            2 -> IslandStateManager.postVolumeLevel(level = (i % 10) / 10f, isRinger = false)
                            3 -> IslandStateManager.postCharging(batteryPercent = 50 + (i % 40))
                            4 -> IslandStateManager.postRingerMode(RingerModeType.SILENT)
                            5 -> IslandStateManager.dismissActiveNotification()
                            6 -> IslandStateManager.selectNextNotification()
                            7 -> IslandStateManager.resolveNotificationIncomingCall(restoreInterrupted = true)
                        }
                        completedOps.incrementAndGet()
                    }
                } catch (t: Throwable) {
                    errorRef.compareAndSet(null, t)
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        // Fire all threads simultaneously
        startLatch.countDown()
        val finishedInTime = doneLatch.await(10, TimeUnit.SECONDS)
        executor.shutdownNow()

        assertTrue("All concurrent operations should finish within 10s", finishedInTime)
        val error = errorRef.get()
        if (error != null) {
            throw AssertionError("Concurrent mutation threw exception: ${error.message}", error)
        }
        assertEquals("Total operations expected", threadCount * operationsPerThread, completedOps.get())

        // Validate final state is intact
        assertNotNull(IslandStateManager.currentState.value)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario C: Late image result discarded after notification removal
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioC_lateImageResultDiscardedAfterNotificationRemoval() {
        val notificationId = "notif_late_test"
        IslandStateManager.postNotification(
            createNotification(id = notificationId, text = "Pending image"),
            autoExpand = false,
            displaySeconds = 5
        )
        assertTrue(IslandStateManager.notificationSourceIds().contains(notificationId))

        // Notification is dismissed/removed by user or timeout
        IslandStateManager.removeNotification(notificationId)
        assertFalse(
            "Notification must be removed from queue",
            IslandStateManager.notificationSourceIds().contains(notificationId)
        )

        // Asynchronous background extraction completes late and attempts to update artwork
        IslandStateManager.updateNotificationArtwork(
            notificationId = notificationId,
            imagePath = "/cache/disk_avatar_$notificationId.jpg",
            picturePath = "/cache/disk_pic_$notificationId.jpg"
        )

        // Verify notification is NOT revived or resurrected in the queue
        assertFalse(
            "Late image callback must not resurrect a removed notification",
            IslandStateManager.notificationSourceIds().contains(notificationId)
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario D: Notification update replacement in-place without reordering
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioD_notificationUpdateReplacesInPlaceWithoutReorderingOrLosingSelection() {
        val notif1 = createNotification("notif_1", title = "A", time = 1000L)
        val notif2 = createNotification("notif_2", title = "B", time = 2000L)
        val notif3 = createNotification("notif_3", title = "C", time = 3000L)

        IslandStateManager.postNotification(notif1, autoExpand = false, displaySeconds = 5)
        IslandStateManager.postNotification(notif2, autoExpand = false, displaySeconds = 5)
        IslandStateManager.postNotification(notif3, autoExpand = false, displaySeconds = 5)

        // Select notif_2 explicitly
        IslandStateManager.selectNotification("notif_2")

        val stateBeforeUpdate = IslandStateManager.currentState.value as IslandState.Notification
        assertEquals("notif_2 should be active selection", "notif_2", stateBeforeUpdate.activeNotification?.id)
        val orderBefore = stateBeforeUpdate.notifications.map { it.id }

        // Update artwork asynchronously in-place
        IslandStateManager.updateNotificationArtwork(
            notificationId = "notif_2",
            imagePath = "/cache/avatar_2.jpg",
            picturePath = "/cache/pic_2.jpg"
        )

        val stateAfterArtwork = IslandStateManager.currentState.value as IslandState.Notification
        assertEquals("Queue order must remain identical after artwork update", orderBefore, stateAfterArtwork.notifications.map { it.id })
        val updatedItem = stateAfterArtwork.notifications.first { it.id == "notif_2" }
        assertEquals("/cache/avatar_2.jpg", updatedItem.imagePath)
        assertEquals("/cache/pic_2.jpg", updatedItem.picturePath)
        assertEquals("Active selection must remain notif_2", "notif_2", stateAfterArtwork.activeNotification?.id)

        // Update notification body/title via posted event
        val notif2Updated = notif2.copy(text = "Updated message content")
        IslandStateManager.postNotification(notif2Updated, autoExpand = false, displaySeconds = 5)

        val stateAfterRepack = IslandStateManager.currentState.value as IslandState.Notification
        assertEquals("Queue size must remain 3 without duplicates", 3, stateAfterRepack.notifications.size)
        assertEquals("Queue order must be preserved", orderBefore, stateAfterRepack.notifications.map { it.id })
        assertEquals("Active selection must remain notif_2", "notif_2", stateAfterRepack.activeNotification?.id)
        assertEquals("Updated message content", stateAfterRepack.activeNotification?.text)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario E: Call interruption and restoration of base state
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioE_callInterruptionAndRestorationOfBaseState() {
        val track = createTrack()
        IslandStateManager.postMusicPlayback(track, isPlaying = true)
        assertTrue(IslandStateManager.currentState.value is IslandState.Music)

        // Post a notification while music is playing
        IslandStateManager.postNotification(createNotification("chat_msg"), autoExpand = false, displaySeconds = 5)
        val preCallState = IslandStateManager.currentState.value
        assertTrue("Pre-call state should include notification or music", preCallState is IslandState.NotificationWithMusic || preCallState is IslandState.Notification)

        // Incoming call arrives
        val caller = ContactInfo("Alice", "+1-555-0199")
        IslandStateManager.postIncomingCall(caller)

        val incomingState = IslandStateManager.currentState.value
        assertTrue("Island must immediately present IncomingCall", incomingState is IslandState.IncomingCall)
        assertEquals("Alice", (incomingState as IslandState.IncomingCall).contact.name)

        // Transient alert occurs (e.g. volume button pressed during ringing)
        IslandStateManager.postVolumeLevel(0.75f, isRinger = true)
        // Incoming call must NOT be wiped out by transient HUD
        val stateDuringTransient = IslandStateManager.currentState.value
        assertTrue("Incoming call must NOT be occluded by transient HUD", stateDuringTransient is IslandState.IncomingCall)

        // Call declined or rejected -> resolve incoming call
        IslandStateManager.resolveNotificationIncomingCall(restoreInterrupted = true)

        val restoredState = IslandStateManager.currentState.value
        assertTrue(
            "Interrupted state must be cleanly restored (not stuck on Call UI). Actual: $restoredState",
            restoredState is IslandState.NotificationWithMusic || restoredState is IslandState.Music || restoredState is IslandState.Notification
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario F: Process lifecycle / state reset (`resetForTesting`)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioF_processLifecycleAndStateResetForTesting() {
        // Populate manager with multiple active streams
        IslandStateManager.postMusicPlayback(createTrack(), isPlaying = true)
        IslandStateManager.postNotification(createNotification("n1"), autoExpand = true, displaySeconds = 10)
        IslandStateManager.postLiveActivity(
            LiveActivityInfo(id = "timer1", kind = LiveActivityKind.TIMER, title = "Timer", isRunning = true)
        )
        IslandStateManager.setPaywallActive(true)

        // Reset
        IslandStateManager.resetForTesting()

        assertEquals("State must reset to Minimal", IslandState.Minimal, IslandStateManager.currentState.value)
        assertTrue("Notification source IDs must be empty", IslandStateManager.notificationSourceIds().isEmpty())
        assertFalse("Timer running must be false", IslandStateManager.timerRunning.value)
        assertFalse("Paywall must be inactive", IslandStateManager.isPaywallActive.value)
    }

    @Test
    fun viewedNotificationsStayActiveAndOnlyExplicitPagerViewingClearsUnseenCount() {
        val ids = listOf("seen_one", "seen_two", "seen_three")
        ids.forEachIndexed { index, id ->
            IslandStateManager.postNotification(
                createNotification(id, time = 10_000L + index),
                autoExpand = false,
                displaySeconds = 60
            )
        }

        assertEquals(3, IslandStateManager.unseenNotificationCount.value)
        // Queue selection alone is not an explicit view while the island is compact.
        IslandStateManager.selectNotification("seen_two")
        assertEquals(3, IslandStateManager.unseenNotificationCount.value)

        IslandStateManager.openNotifications()
        assertEquals(2, IslandStateManager.unseenNotificationCount.value)
        ids.forEach(IslandStateManager::selectNotification)

        assertEquals(0, IslandStateManager.unseenNotificationCount.value)
        assertEquals(ids.toSet(), IslandStateManager.notificationSourceIds())

        IslandStateManager.collapseExpandedState()
        assertEquals(0, IslandStateManager.unseenNotificationCount.value)
        assertEquals(ids.toSet(), IslandStateManager.notificationSourceIds())
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario G: Source reconciliation (`reconcileNotificationSources`)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioG_sourceReconciliationPrunesDismissedPlatformNotifications() {
        for (i in 1..5) {
            IslandStateManager.postNotification(
                createNotification("notif_$i", text = "Message $i"),
                autoExpand = false,
                displaySeconds = 5
            )
        }
        assertEquals(setOf("notif_1", "notif_2", "notif_3", "notif_4", "notif_5"), IslandStateManager.notificationSourceIds())

        // Android NotificationListenerService reports that platform only retains notif_2 and notif_4
        IslandStateManager.reconcileNotificationSources(setOf("notif_2", "notif_4"))

        val reconciledIds = IslandStateManager.notificationSourceIds()
        assertEquals("Queue must only contain notifications retained by the platform", setOf("notif_2", "notif_4"), reconciledIds)

        val currentState = IslandStateManager.currentState.value as IslandState.Notification
        assertEquals(2, currentState.notifications.size)
        assertTrue(currentState.notifications.all { it.id == "notif_2" || it.id == "notif_4" })
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario H: Media restoration upon notification expiry
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioH_mediaRestorationUponNotificationExpiryOrDismissal() {
        val track = createTrack(title = "Starlight", artist = "Muse")
        IslandStateManager.postMusicPlayback(track, isPlaying = true)
        assertEquals(IslandState.Music(track = track, isPlaying = true, isExpanded = false), IslandStateManager.currentState.value)

        // Post a transient notification
        IslandStateManager.postNotification(createNotification("temp_note"), autoExpand = false, displaySeconds = 5)
        val stateWithNote = IslandStateManager.currentState.value
        assertTrue("State must reflect notification arrival", stateWithNote is IslandState.NotificationWithMusic || stateWithNote is IslandState.Notification)

        // Clear all notifications (simulating expiry/clearing)
        IslandStateManager.clearNotificationSources()

        val restoredState = IslandStateManager.currentState.value
        assertTrue("Media presentation must be smoothly restored", restoredState is IslandState.Music)
        val music = restoredState as IslandState.Music
        assertEquals("Starlight", music.track.title)
        assertTrue(music.isPlaying)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario I: Resource pressure / bounded artwork cache
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioI_resourcePressureAndBoundedArtworkCache() {
        // Enforce maximum cache bounds
        assertEquals(40, NotificationArtworkRepository.MAX_DISK_FILES)
        assertEquals(512, NotificationArtworkRepository.MAX_IMAGE_DIMENSION)
        assertTrue(NotificationArtworkRepository.PRUNE_INTERVAL_MILLIS > 0)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario J: Rapid user interactions (swipe dismiss, expand, collapse)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun scenarioJ_rapidUserInteractionsDoNotDeadlockOrThrow() {
        // Post 3 notifications
        for (i in 1..3) {
            IslandStateManager.postNotification(createNotification("user_notif_$i"), autoExpand = false, displaySeconds = 5)
        }

        // Execute rapid sequence of user interactions
        for (step in 0..50) {
            when (step % 7) {
                0 -> IslandStateManager.openNotifications(selectNext = true)
                1 -> IslandStateManager.selectNextNotification()
                2 -> IslandStateManager.selectPreviousNotification()
                3 -> IslandStateManager.handleSwipeDismiss()
                4 -> IslandStateManager.collapseToMinimal()
                5 -> IslandStateManager.onDeviceLocked()
                6 -> IslandStateManager.onDeviceUnlocked()
            }
        }

        // Assert completes cleanly and state remains coherent
        assertNotNull(IslandStateManager.currentState.value)
    }
}
