package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class EntitlementManagerTest {

    @Test
    fun lifetimePurchaseTakesHighestPriority() {
        val now = System.currentTimeMillis()
        val trialActive = TrialSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(5))
        val rewardActive = RewardedAccessSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(2))

        val snapshot = EntitlementSnapshot(
            source = EntitlementSource.LIFETIME,
            isPro = true,
            isAdFree = true,
            expiresAtMillis = null,
            isPendingPayment = false,
            isTrial = false,
            isRewarded = false,
            remainingDays = 9999,
            planDisplayName = "Lifetime"
        )

        assertTrue(snapshot.isActive)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertEquals(EntitlementSource.LIFETIME, snapshot.source)
        assertEquals("Lifetime", snapshot.planDisplayName)
        assertFalse(snapshot.shouldShowExpiryIsland())
    }

    @Test
    fun yearlySubscriptionIsAdFreeAndPro() {
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(365)

        val snapshot = EntitlementSnapshot(
            source = EntitlementSource.YEARLY,
            isPro = true,
            isAdFree = true,
            expiresAtMillis = expiresAt,
            isPendingPayment = false,
            isTrial = false,
            isRewarded = false,
            remainingDays = 365,
            planDisplayName = "Yearly"
        )

        assertTrue(snapshot.isActive)
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.isAdFree)
        assertEquals(EntitlementSource.YEARLY, snapshot.source)
        assertFalse(snapshot.shouldShowExpiryIsland())
    }

    @Test
    fun trialAndRewardedAreNotAdFree() {
        val now = System.currentTimeMillis()
        val trialSnapshot = EntitlementSnapshot(
            source = EntitlementSource.INSTALL_TRIAL,
            isPro = true,
            isAdFree = false,
            expiresAtMillis = now + TimeUnit.DAYS.toMillis(7),
            isTrial = true,
            planDisplayName = "7-Day Trial"
        )

        assertTrue(trialSnapshot.isActive)
        assertTrue(trialSnapshot.isPro)
        assertFalse(trialSnapshot.isAdFree) // In-app ads still show for trial users
        assertFalse(trialSnapshot.shouldShowExpiryIsland())

        val rewardSnapshot = EntitlementSnapshot(
            source = EntitlementSource.REWARDED_ACCESS,
            isPro = true,
            isAdFree = false,
            expiresAtMillis = now + TimeUnit.DAYS.toMillis(2),
            isRewarded = true,
            planDisplayName = "Reward Pass"
        )

        assertTrue(rewardSnapshot.isActive)
        assertTrue(rewardSnapshot.isPro)
        assertFalse(rewardSnapshot.isAdFree)
        assertFalse(rewardSnapshot.shouldShowExpiryIsland())
    }

    @Test
    fun expiredAccessDefaultsToFreeTier() {
        val freeSnapshot = EntitlementSnapshot(
            source = EntitlementSource.FREE,
            isPro = false,
            isAdFree = false,
            expiresAtMillis = null,
            remainingDays = 0,
            planDisplayName = "Free"
        )

        assertFalse(freeSnapshot.isActive)
        assertFalse(freeSnapshot.isPro)
        assertFalse(freeSnapshot.isAdFree)
        assertEquals(EntitlementSource.FREE, freeSnapshot.source)
        assertTrue(freeSnapshot.shouldShowExpiryIsland())
    }
}
