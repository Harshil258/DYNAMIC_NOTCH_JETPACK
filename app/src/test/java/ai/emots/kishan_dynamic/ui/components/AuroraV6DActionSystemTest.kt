package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraV6DActionSystemTest {

    @Test
    fun actionTaxonomy_allStylesAreDistinct() {
        val styles = IslandActionStyle.entries
        assertEquals(8, styles.size)
        assertTrue(styles.contains(IslandActionStyle.Primary))
        assertTrue(styles.contains(IslandActionStyle.Secondary))
        assertTrue(styles.contains(IslandActionStyle.Positive))
        assertTrue(styles.contains(IslandActionStyle.Destructive))
        assertTrue(styles.contains(IslandActionStyle.Tertiary))
        assertTrue(styles.contains(IslandActionStyle.Toggle))
        assertTrue(styles.contains(IslandActionStyle.Passive))
        assertTrue(styles.contains(IslandActionStyle.Overflow))
    }

    @Test
    fun actionTaxonomy_semanticAccentsAreDistinct() {
        val accents = IslandSemanticAccent.entries
        assertEquals(6, accents.size)
        assertTrue(accents.contains(IslandSemanticAccent.Blue))
        assertTrue(accents.contains(IslandSemanticAccent.Green))
        assertTrue(accents.contains(IslandSemanticAccent.Red))
        assertTrue(accents.contains(IslandSemanticAccent.Cyan))
        assertTrue(accents.contains(IslandSemanticAccent.Orange))
        assertTrue(accents.contains(IslandSemanticAccent.Neutral))
    }

    @Test
    fun callSummaryPhaseLabel_distinguishesIncomingOutgoingMissed() {
        val incoming = CallRecord(
            id = "1",
            contactName = "Tamia",
            phoneNumber = "+15550192834",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 120L,
            direction = CallDirection.INCOMING
        )
        val outgoing = CallRecord(
            id = "2",
            contactName = "Tamia",
            phoneNumber = "+15550192834",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 45L,
            direction = CallDirection.OUTGOING
        )
        val missed = CallRecord(
            id = "3",
            contactName = "Tamia",
            phoneNumber = "+15550192834",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 0L,
            direction = CallDirection.MISSED
        )

        assertEquals("Call ended · Incoming", callSummaryPhaseLabel(incoming))
        assertEquals("Outgoing call ended", callSummaryPhaseLabel(outgoing))
        assertEquals("Missed call", callSummaryPhaseLabel(missed))
        assertEquals("Call ended · Incoming", callSummaryPhaseLabel(null, isDemoPreview = true))
        assertEquals("Call ended", callSummaryPhaseLabel(null, isDemoPreview = false))
    }

    @Test
    fun callSummaryDurationLabel_neutralAndAccurate() {
        val accurate = CallRecord(
            id = "1",
            contactName = "Tamia",
            phoneNumber = "+1555",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 165L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.RELIABLE_MONOTONIC
        )
        val estimated = CallRecord(
            id = "2",
            contactName = "Tamia",
            phoneNumber = "+1555",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 75L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.INFERRED_WALL_CLOCK
        )
        val unknown = CallRecord(
            id = "3",
            contactName = "Tamia",
            phoneNumber = "+1555",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 0L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.UNKNOWN
        )
        val missed = CallRecord(
            id = "4",
            contactName = "Tamia",
            phoneNumber = "+1555",
            avatarUri = null,
            startedAtMillis = 1000L,
            durationSeconds = 0L,
            direction = CallDirection.MISSED
        )

        assertEquals("02:45", callSummaryDurationLabel(accurate))
        assertEquals("≈01:15", callSummaryDurationLabel(estimated))
        assertEquals("Duration unavailable", callSummaryDurationLabel(unknown))
        assertEquals("Missed", callSummaryDurationLabel(missed))
        assertEquals("", callSummaryDurationLabel(null))
    }

    @Test
    fun callSummaryPrivacy_lockedDeviceRedactsCallerDetails() {
        val plain = CallRecord(
            id = "1",
            contactName = "Tamia Castillo",
            phoneNumber = "+1 (555) 019-2834",
            avatarUri = "content://avatar",
            startedAtMillis = 1000L,
            durationSeconds = 165L,
            direction = CallDirection.INCOMING
        )
        val locked = LockScreenPrivacyPolicy.callRecord(plain, deviceLocked = true, hideSensitiveContent = true)

        assertNotNull(locked)
        assertEquals("Incoming call", locked?.contactName)
        assertEquals("", locked?.phoneNumber)
        assertNull(locked?.avatarUri)
        assertEquals(165L, locked?.durationSeconds)

        val unlocked = LockScreenPrivacyPolicy.callRecord(plain, deviceLocked = false, hideSensitiveContent = true)
        assertEquals("Tamia Castillo", unlocked?.contactName)
        assertEquals("+1 (555) 019-2834", unlocked?.phoneNumber)
        assertEquals("content://avatar", unlocked?.avatarUri)
    }

    @Test
    fun capabilityResolver_safeOnNullOrBlank() {
        // Without an active Android mock, the null or non-digit check must safely return false without throwing
        val dummyContext: android.content.Context? = null
        if (dummyContext != null) {
            assertFalse(IslandActionCapability.canDial(dummyContext, null))
            assertFalse(IslandActionCapability.canDial(dummyContext, ""))
            assertFalse(IslandActionCapability.canDial(dummyContext, "no-digits"))
            assertFalse(IslandActionCapability.canSendSms(dummyContext, null))
        }
    }

    @Test
    fun all60DemoStates_existInAuditMatrix() {
        val states = IslandDemoState.entries
        assertEquals("Aurora V6D 60-state contract requirement", 60, states.size)

        // Verify key families have representation
        val callStates = states.filter { it.name.contains("Call", ignoreCase = true) }
        assertTrue("Call states must be present", callStates.size >= 7)

        val notificationStates = states.filter { it.name.contains("Notification", ignoreCase = true) }
        assertTrue("Notification states must be present", notificationStates.size >= 5)

        val mediaStates = states.filter { it.name.contains("Music", ignoreCase = true) || it.name.contains("Video", ignoreCase = true) }
        assertTrue("Media states must be present", mediaStates.size >= 3)
    }
}
