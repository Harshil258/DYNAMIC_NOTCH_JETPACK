package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class RewardedAccessManagerTest {

    @Test
    fun initialRewardedAccessReturnsInactiveSnapshot() {
        val snapshot = RewardedAccessSnapshot()
        assertFalse(snapshot.isActive)
        assertEquals(0, snapshot.remainingDays)
        assertEquals(0, snapshot.remainingHours)
        assertTrue(snapshot.canWatchAd)
        assertEquals(0, snapshot.cooldownRemainingSeconds)
    }

    @Test
    fun activeRewardPassHasPositiveRemainingDaysAndHours() {
        val now = System.currentTimeMillis()
        val expiry = now + TimeUnit.HOURS.toMillis(48)

        val snapshot = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = expiry,
            remainingDays = 2,
            remainingHours = 48,
            lastAdWatchTimestamp = now,
            cooldownRemainingSeconds = 12,
            canWatchAd = false,
            unlockedDaysCount = 2,
            checkmarks = emptyList()
        )

        assertTrue(snapshot.isActive)
        assertEquals(2, snapshot.remainingDays)
        assertEquals(48, snapshot.remainingHours)
        assertFalse(snapshot.canWatchAd)
        assertEquals(12, snapshot.cooldownRemainingSeconds)
    }

    @Test
    fun rewardCooldownBlocksImmediateWatching() {
        val snapshot = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24),
            remainingDays = 1,
            remainingHours = 24,
            lastAdWatchTimestamp = System.currentTimeMillis(),
            cooldownRemainingSeconds = 15,
            canWatchAd = false
        )

        assertFalse(snapshot.canWatchAd)
        assertEquals(15, snapshot.cooldownRemainingSeconds)
    }

    @Test
    fun rewardCooldownExpiryAllowsNextWatch() {
        val snapshot = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24),
            remainingDays = 1,
            remainingHours = 24,
            lastAdWatchTimestamp = System.currentTimeMillis() - 20_000L,
            cooldownRemainingSeconds = 0,
            canWatchAd = true
        )

        assertTrue(snapshot.canWatchAd)
        assertEquals(0, snapshot.cooldownRemainingSeconds)
    }
}
