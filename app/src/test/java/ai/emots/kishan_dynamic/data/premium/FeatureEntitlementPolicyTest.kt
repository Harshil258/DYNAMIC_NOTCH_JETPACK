package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureEntitlementPolicyTest {

    @Test
    fun essentialFeaturesAreNeverGatedForFreeUsers() {
        val freeSnapshot = EntitlementSnapshot(isPro = false)

        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.CORE_ISLAND_ACTIVATION, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.BASIC_NOTIFICATIONS, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.BASIC_MUSIC_CONTROLS, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.BASIC_NOTCH_ALIGNMENT, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.CALL_PRESENTATION, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.CHARGING_PRESENTATION, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.BLUETOOTH_PRESENTATION, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.PRIVACY_CONTROLS, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.PERMISSIONS_MANAGEMENT, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.RESTORE_PURCHASES, freeSnapshot))
    }

    @Test
    fun proFeaturesAreLockedForFreeUsers() {
        val freeSnapshot = EntitlementSnapshot(isPro = false)

        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.THEME_SETTINGS, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.DISPLAY_HORIZONTAL_OFFSET, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.SOUND_SETTINGS, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.CALL_END_DISABLE, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.COMPACT_MUSIC, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.NOTIFICATION_DURATION_EXTENDED, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.QUICK_CONTROL_UNLIMITED, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.AD_FREE, freeSnapshot))

        assertTrue(FeatureEntitlementPolicy.isFeatureLocked(EntitlementFeatures.THEME_SETTINGS, freeSnapshot))
        assertTrue(FeatureEntitlementPolicy.isFeatureLocked(EntitlementFeatures.AD_FREE, freeSnapshot))
    }

    @Test
    fun proFeaturesUnlockWhenProIsActive() {
        val proSnapshot = EntitlementSnapshot(isPro = true, isAdFree = true, source = EntitlementSource.MONTHLY)

        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.THEME_SETTINGS, proSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.DISPLAY_HORIZONTAL_OFFSET, proSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.SOUND_SETTINGS, proSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.COMPACT_MUSIC, proSnapshot))
        assertTrue(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.AD_FREE, proSnapshot))

        assertFalse(FeatureEntitlementPolicy.isFeatureLocked(EntitlementFeatures.AD_FREE, proSnapshot))
    }

    @Test
    fun proFeaturesLockWhenPassExpires() {
        val expiredSnapshot = EntitlementSnapshot(
            isPro = true,
            source = EntitlementSource.REWARDED_ACCESS,
            expiresAtMillis = System.currentTimeMillis() - 10_000L
        )

        assertTrue(expiredSnapshot.isExpired)
        assertFalse(expiredSnapshot.isActive)
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.THEME_SETTINGS, expiredSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.AD_FREE, expiredSnapshot))
    }

    @Test
    fun pendingPaymentDoesNotGrantEntitlement() {
        val pendingSnapshot = EntitlementSnapshot(
            isPro = true,
            isPendingPayment = true
        )

        assertFalse(pendingSnapshot.isActive)
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.AD_FREE, pendingSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.THEME_SETTINGS, pendingSnapshot))
    }

    @Test
    fun respectfulInterstitialCooldownBlocksBackToBackAds() {
        val now = 1_000_000L
        // Screen count 4 matches threshold, and cooldown (95s) has passed
        assertTrue(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = 4,
                screenEvery = 4,
                startFromScreen = 0,
                lastShownMillis = now - 95_000L,
                nowMillis = now
            )
        )

        // Screen count 8 matches threshold, but cooldown (30s) has NOT passed -> must return false
        assertFalse(
            InterstitialAdFrequencyPolicy.shouldShow(
                screenCount = 8,
                screenEvery = 4,
                startFromScreen = 0,
                lastShownMillis = now - 30_000L,
                nowMillis = now
            )
        )
    }
}
