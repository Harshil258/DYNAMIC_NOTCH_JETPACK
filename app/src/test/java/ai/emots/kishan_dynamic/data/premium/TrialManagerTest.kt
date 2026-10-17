package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TrialManagerTest {

    private val dayMillis = 24L * 60 * 60 * 1000L

    @Test
    fun uninitializedTrialReturnsInactiveSnapshot() {
        val snapshot = TrialSnapshot()
        assertFalse(snapshot.isActive)
        assertFalse(snapshot.isExpired)
        assertEquals(0, snapshot.remainingDays)
        assertFalse(snapshot.isTampered)
    }

    @Test
    fun activeTrialCalculatesRemainingDaysAndHoursCorrectly() {
        val now = System.currentTimeMillis()
        val start = now - TimeUnit.DAYS.toMillis(2)
        val expires = start + TimeUnit.DAYS.toMillis(7)

        val snapshot = TrialSnapshot(
            isActive = true,
            isExpired = false,
            startTimeMillis = start,
            expiresAtMillis = expires,
            durationDays = 7,
            remainingHours = 120,
            remainingDays = 5,
            isTampered = false
        )

        assertTrue(snapshot.isActive)
        assertFalse(snapshot.isExpired)
        assertEquals(5, snapshot.remainingDays)
        assertEquals(7, snapshot.durationDays)
        assertFalse(snapshot.isTampered)
    }

    @Test
    fun expiredTrialSetsActiveFalseAndExpiredTrue() {
        val now = System.currentTimeMillis()
        val start = now - TimeUnit.DAYS.toMillis(8)
        val expires = start + TimeUnit.DAYS.toMillis(7)

        val snapshot = TrialSnapshot(
            isActive = false,
            isExpired = true,
            startTimeMillis = start,
            expiresAtMillis = expires,
            durationDays = 7,
            remainingHours = 0,
            remainingDays = 0,
            isTampered = false
        )

        assertFalse(snapshot.isActive)
        assertTrue(snapshot.isExpired)
        assertEquals(0, snapshot.remainingDays)
    }

    @Test
    fun clockRollbackSetsTamperedTrueAndActiveFalse() {
        val now = System.currentTimeMillis()
        val start = now
        val expires = start + TimeUnit.DAYS.toMillis(7)

        val snapshot = TrialSnapshot(
            isActive = false,
            isExpired = false,
            startTimeMillis = start,
            expiresAtMillis = expires,
            durationDays = 7,
            remainingHours = 168,
            remainingDays = 7,
            isTampered = true
        )

        assertFalse(snapshot.isActive)
        assertTrue(snapshot.isTampered)
    }
}
