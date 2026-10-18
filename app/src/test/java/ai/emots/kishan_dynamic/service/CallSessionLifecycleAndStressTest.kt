package ai.emots.kishan_dynamic.service

import android.content.Context
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.CallNotificationPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
class CallSessionLifecycleAndStressTest {

    private val testContext: Context? = null
    private var simulatedRealtime = 100_000L
    private var simulatedWallClock = 1_700_000_000_000L

    @Before
    fun setUp() {
        CallSessionCoordinator.resetForTesting()
        simulatedRealtime = 100_000L
        simulatedWallClock = 1_700_000_000_000L
        CallSessionCoordinator.elapsedRealtimeProvider = { simulatedRealtime }
        CallSessionCoordinator.currentTimeMillisProvider = { simulatedWallClock }
    }

    @After
    fun tearDown() {
        CallSessionCoordinator.resetForTesting()
    }

    @Test
    fun incomingCallAnsweredAndEndedTracksDurationAndDirection() = runBlocking {
        // 1. Ringing
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")
        val ringingSession = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(ringingSession)
        assertEquals(CallSessionState.RINGING, ringingSession?.state)
        assertEquals(CallDirection.INCOMING, ringingSession?.direction)
        assertFalse(ringingSession!!.wasOffHook)

        // 2. Offhook (Answered) 4 seconds later
        simulatedRealtime += 4_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        val activeSession = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(activeSession)
        assertEquals(CallSessionState.ACTIVE, activeSession?.state)
        assertTrue(activeSession!!.wasOffHook)
        assertTrue(activeSession.wasAnswered)

        // 3. Idle (Call ended) 65 seconds later
        simulatedRealtime += 65_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals("+919876543210", record?.phoneNumber)
        assertEquals(CallDirection.INCOMING, record?.direction)
        assertEquals(65L, record?.durationSeconds)
    }

    @Test
    fun incomingCallRejectedRecordsMissedWithZeroDuration() = runBlocking {
        // 1. Ringing
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")

        // 2. User rejects call after 3 seconds of ringing (IDLE without OFFHOOK)
        simulatedRealtime += 3_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals("+919876543210", record?.phoneNumber)
        assertEquals(CallDirection.MISSED, record?.direction)
        assertEquals(0L, record?.durationSeconds)
    }

    @Test
    fun incomingCallMissedRingsAndStopsWithoutAnswer() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+911234567890")
        simulatedRealtime += 30_000L // 30 seconds of ringing
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+911234567890")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(CallDirection.MISSED, record?.direction)
        assertEquals(0L, record?.durationSeconds)
    }

    @Test
    fun outgoingCallStartedAndEndedTracksDuration() = runBlocking {
        // Outgoing calls transition directly from IDLE to OFFHOOK
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        val session = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(session)
        assertEquals(CallSessionState.DIALING_OR_OUTGOING, session?.state)
        assertEquals(CallDirection.OUTGOING, session?.direction)
        assertTrue(session!!.wasOffHook)

        // Call connects and lasts 45 seconds
        simulatedRealtime += 45_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(CallDirection.OUTGOING, record?.direction)
        assertEquals(45L, record?.durationSeconds)
    }

    @Test
    fun shortCallUnderTwoSecondsTrackedAccurately() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        simulatedRealtime += 1_800L // 1.8 seconds
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(1L, record?.durationSeconds)
    }

    @Test
    fun subSecondCallFloorsToZeroSeconds() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        simulatedRealtime += 400L // 0.4 seconds
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(0L, record?.durationSeconds)
    }

    @Test
    fun longCallOverOneHourTrackedAccurately() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        // 1 hour, 2 minutes, 15 seconds = 3735 seconds = 3,735,000 ms
        simulatedRealtime += 3_735_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(3735L, record?.durationSeconds)
    }

    @Test
    fun unknownCallerFallbackWhenNumberIsNull() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "")
        simulatedRealtime += 2_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals("Unknown Caller", record?.phoneNumber)
        assertEquals("Unknown Caller", record?.contactName)
    }

    @Test
    fun duplicateRingingBroadcastsDeduplicated() = runBlocking {
        // Initial Ringing
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")
        val sessionId1 = CallSessionCoordinator.currentSessionSnapshot()?.sessionId

        // Duplicate Ringing 500ms later from OEM
        simulatedRealtime += 500L
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")
        val sessionId2 = CallSessionCoordinator.currentSessionSnapshot()?.sessionId

        assertEquals(sessionId1, sessionId2)

        // Offhook and end
        simulatedRealtime += 2000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        simulatedRealtime += 10_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(10L, record?.durationSeconds)
    }

    @Test
    fun duplicateOffhookBroadcastsDeduplicated() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")
        simulatedRealtime += 1_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")

        // Duplicate OFFHOOK 1 second later
        simulatedRealtime += 1_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")

        // End call 9 seconds after the duplicate (10 seconds total from first offhook)
        simulatedRealtime += 9_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(10L, record?.durationSeconds)
    }

    @Test
    fun duplicateIdleBroadcastsIgnored() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        simulatedRealtime += 5_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val firstRecord = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(firstRecord)

        // Repeated IDLE broadcast
        simulatedRealtime += 1_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        // Coordinator session remains null/idle, no new/corrupted record created
        assertNull(CallSessionCoordinator.currentSessionSnapshot())
        assertEquals(firstRecord?.id, CallSessionCoordinator.lastCompletedRecord.value?.id)
    }

    @Test
    fun receiverRecreationSimulatesAndroidBehavior() = runBlocking {
        // In Android, each broadcast intent instantiates a NEW TelephonyStateReceiver.
        // We verify that state survives across receiver instances via CallSessionCoordinator.
        val receiver1 = TelephonyStateReceiver()
        val receiver2 = TelephonyStateReceiver()
        val receiver3 = TelephonyStateReceiver()

        // Receiver 1 receives RINGING
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+919876543210")
        val ringSnapshot = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(ringSnapshot)

        // Receiver 2 receives OFFHOOK
        simulatedRealtime += 3_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        val offhookSnapshot = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(offhookSnapshot)
        assertEquals(ringSnapshot?.sessionId, offhookSnapshot?.sessionId)
        assertTrue(offhookSnapshot!!.wasOffHook)

        // Receiver 3 receives IDLE
        simulatedRealtime += 15_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val finalRecord = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(finalRecord)
        assertEquals("+919876543210", finalRecord?.phoneNumber)
        assertEquals(15L, finalRecord?.durationSeconds)
        assertEquals(CallDirection.INCOMING, finalRecord?.direction)
    }

    @Test
    fun rapidConsecutiveCallsHandledCleanly() = runBlocking {
        // Call 1
        CallSessionCoordinator.handleStateTransition(testContext, "RINGING", "+911111111111")
        simulatedRealtime += 1_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+911111111111")
        simulatedRealtime += 5_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+911111111111")

        val record1 = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record1)
        assertEquals("+911111111111", record1?.phoneNumber)
        assertEquals(5L, record1?.durationSeconds)

        // Call 2 immediately starts
        simulatedRealtime += 2_000L
        simulatedWallClock += 2_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+912222222222")
        simulatedRealtime += 8_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+912222222222")

        val record2 = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record2)
        assertEquals("+912222222222", record2?.phoneNumber)
        assertEquals(8L, record2?.durationSeconds)
    }

    @Test
    fun whatsAppNumberFormattingNormalizesVariousInputs() {
        // 10-digit standard Indian mobile -> prefixes 91
        assertEquals("919876543210", CallActionLauncher.formatWhatsAppNumber("9876543210"))

        // Leading +91 with spaces and dashes
        assertEquals("919876543210", CallActionLauncher.formatWhatsAppNumber("+91 98765-43210"))

        // Leading 0 domestic trunk prefix
        assertEquals("919876543210", CallActionLauncher.formatWhatsAppNumber("09876543210"))

        // International 00 prefix
        assertEquals("919876543210", CallActionLauncher.formatWhatsAppNumber("00919876543210"))

        // US phone number (+1)
        assertEquals("14155552671", CallActionLauncher.formatWhatsAppNumber("+1 (415) 555-2671"))

        // Invalid numbers return null
        assertNull(CallActionLauncher.formatWhatsAppNumber("Unknown Caller"))
        assertNull(CallActionLauncher.formatWhatsAppNumber("Unknown"))
        assertNull(CallActionLauncher.formatWhatsAppNumber("Private Number"))
        assertNull(CallActionLauncher.formatWhatsAppNumber("123"))
        assertNull(CallActionLauncher.formatWhatsAppNumber(""))
    }

    @Test
    fun callActionLauncherOpenCallRejectsInvalidInput() {
        assertFalse(CallActionLauncher.openCall(testContext, ""))
        assertFalse(CallActionLauncher.openCall(testContext, "   "))
        assertFalse(CallActionLauncher.openCall(testContext, "Unknown Caller"))
    }

    @Test
    fun callActionLauncherOpenSmsRejectsInvalidInput() {
        assertFalse(CallActionLauncher.openSms(testContext, ""))
        assertFalse(CallActionLauncher.openSms(testContext, "Unknown Caller"))
    }

    @Test
    fun monotonicTimeImmunityToWallClockJump() = runBlocking {
        // Start call at wall clock 1,000,000 and realtime 10,000
        simulatedWallClock = 1_000_000L
        simulatedRealtime = 10_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")

        // User rolls back wall clock by 1 hour (daylight saving or manual clock rollback)
        simulatedWallClock = 500_000L // 500 seconds earlier in wall clock time!
        simulatedRealtime += 15_000L  // Real monotonic time advanced by 15 seconds

        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        // Duration MUST be positive 15 seconds, NOT negative or corrupt
        assertEquals(15L, record?.durationSeconds)
    }

    @Test
    fun freeUsersCanOptOutOfCallSummary() {
        // AUR-P0-004 verification
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = false, requested = false))
        assertTrue(PremiumFeaturePolicy.callSummaryEnabled(isProActive = false, requested = true))
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = true, requested = false))
        assertTrue(PremiumFeaturePolicy.callSummaryEnabled(isProActive = true, requested = true))
    }

    @Test
    fun concurrencyStressTest() = runBlocking {
        // Concurrently dispatch 20 call state changes from multiple coroutines
        val jobs = (1..20).map { i ->
            async(Dispatchers.Default) {
                CallSessionCoordinator.handleStateTransition(
                    testContext,
                    if (i % 2 == 0) "OFFHOOK" else "RINGING",
                    "+9198765432$i"
                )
            }
        }
        jobs.awaitAll()

        // Coordinator is still alive and safe
        val snapshot = CallSessionCoordinator.currentSessionSnapshot()
        assertNotNull(snapshot)

        // Transition to idle
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543200")
        assertNull(CallSessionCoordinator.currentSessionSnapshot())
    }

    @Test
    fun callDurationAccuracyReliableForStandardCall() = runBlocking {
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        simulatedRealtime += 45_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(45L, record?.durationSeconds)
        assertEquals(DurationAccuracy.RELIABLE_MONOTONIC, record?.durationAccuracy)
    }

    @Test
    fun deviceRebootDuringCallFallsBackToWallClock() = runBlocking {
        // Start call at realtime 500,000, wall clock 1,000,000
        simulatedRealtime = 500_000L
        simulatedWallClock = 1_000_000L
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")

        // Device reboots while call in progress: realtime resets to 10,000, wall clock advances by 60s
        simulatedRealtime = 10_000L // Reset! Now simulatedRealtime < offHookRealtime
        simulatedWallClock += 60_000L // 60 seconds later

        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals(60L, record?.durationSeconds)
        assertEquals(DurationAccuracy.INFERRED_WALL_CLOCK, record?.durationAccuracy)
    }

    @Test
    fun processDeathRecoveryRestoresActiveSessionOnIdle() = runBlocking {
        // Call begins
        CallSessionCoordinator.handleStateTransition(testContext, "OFFHOOK", "+919876543210")
        val savedSession = CallSessionCoordinator.testPersistedSession
        assertNotNull(savedSession)

        // Process is terminated by OS under memory pressure:
        // Reset in-memory coordinator state while preserving persisted session
        CallSessionCoordinator.resetForTesting()
        CallSessionCoordinator.elapsedRealtimeProvider = { simulatedRealtime }
        CallSessionCoordinator.currentTimeMillisProvider = { simulatedWallClock }
        CallSessionCoordinator.testPersistedSession = savedSession
        assertNull(CallSessionCoordinator.currentSessionSnapshot())

        // System broadcast IDLE arrives, waking app in new process
        simulatedRealtime += 90_000L
        simulatedWallClock += 90_000L
        CallSessionCoordinator.handleStateTransition(testContext, "IDLE", "+919876543210")

        // Active session was successfully restored from persisted store and recorded
        val record = CallSessionCoordinator.lastCompletedRecord.value
        assertNotNull(record)
        assertEquals("+919876543210", record?.phoneNumber)
        assertEquals(90L, record?.durationSeconds)
        assertEquals(DurationAccuracy.RELIABLE_MONOTONIC, record?.durationAccuracy)
        assertNull(CallSessionCoordinator.testPersistedSession)
    }

    @Test
    fun notificationPriorityPolicyElevatesMissedCalls() {
        val missedCallNotification = NotificationInfo(
            id = "notif-1",
            packageName = "com.google.android.dialer",
            appName = "Phone",
            title = "Missed call from Mom",
            text = "Mobile",
            category = "missed_call"
        )
        assertTrue(NotificationPriorityPolicy.isPriority(missedCallNotification))
        assertTrue(CallNotificationPolicy.isMissedCall(missedCallNotification))
    }
}

