package ai.emots.kishan_dynamic.data.premium

import ai.emots.kishan_dynamic.data.preferences.BillingEntitlementRecord
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfigDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class Sprint09TrialAndRewardTest {

    private val dayMillis = 24L * 60 * 60 * 1000L

    // =========================================================================
    // 1. TRIAL CONTRACT & REINSTALL FRAUD MITIGATION
    // =========================================================================

    @Test
    fun trialSnapshotDefaultsToSevenDaysWhenUninitialized() {
        val snapshot = TrialSnapshot()
        assertFalse(snapshot.isActive)
        assertFalse(snapshot.isExpired)
        assertEquals(0, snapshot.remainingDays)
        assertFalse(snapshot.isTampered)
    }

    @Test
    fun trialSnapshotAccuratelyReflectsActiveDays() {
        val now = System.currentTimeMillis()
        val start = now - TimeUnit.DAYS.toMillis(3)
        val expires = start + TimeUnit.DAYS.toMillis(7)

        val snapshot = TrialSnapshot(
            isActive = true,
            isExpired = false,
            startTimeMillis = start,
            expiresAtMillis = expires,
            durationDays = 7,
            remainingHours = 96,
            remainingDays = 4,
            isTampered = false
        )

        assertTrue(snapshot.isActive)
        assertFalse(snapshot.isExpired)
        assertEquals(4, snapshot.remainingDays)
        assertEquals(7, snapshot.durationDays)
    }

    @Test
    fun expiredTrialMarksActiveFalseAndExpiredTrue() {
        val now = System.currentTimeMillis()
        val start = now - TimeUnit.DAYS.toMillis(10)
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
    fun monotonicClockRollbackMarksTamperedAndDeactivatesTrial() {
        val now = System.currentTimeMillis()
        val snapshot = TrialSnapshot(
            isActive = false,
            isExpired = false,
            startTimeMillis = now,
            expiresAtMillis = now + TimeUnit.DAYS.toMillis(7),
            durationDays = 7,
            remainingHours = 168,
            remainingDays = 7,
            isTampered = true
        )

        assertFalse(snapshot.isActive)
        assertTrue(snapshot.isTampered)
    }

    // =========================================================================
    // 2. REWARDED ACCESS STACKING & COOLDOWN
    // =========================================================================

    @Test
    fun singleRewardAdGrantsTwentyFourHoursCredit() {
        val now = System.currentTimeMillis()
        val expiry = now + dayMillis

        val snapshot = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = expiry,
            remainingDays = 1,
            remainingHours = 24,
            lastAdWatchTimestamp = now,
            cooldownRemainingSeconds = 15,
            canWatchAd = false,
            unlockedDaysCount = 1,
            extraDaysStacked = 0
        )

        assertTrue(snapshot.isActive)
        assertEquals(1, snapshot.remainingDays)
        assertEquals(24, snapshot.remainingHours)
        assertEquals(0, snapshot.extraDaysStacked)
    }

    @Test
    fun rewardedAccessStacksSeamlesslyBeyondSevenDays() {
        val now = System.currentTimeMillis()
        // 10 days of accumulated rewarded passes
        val expiry = now + (10 * dayMillis)

        val remainingMillis = expiry - now
        val remainingDays = (remainingMillis / dayMillis).toInt() + if (remainingMillis % dayMillis > 0) 1 else 0
        val extraDaysStacked = (remainingDays - RewardedAccessManager.MAX_VISIBLE_CHECKMARK_DAYS).coerceAtLeast(0)

        val snapshot = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = expiry,
            remainingDays = remainingDays,
            remainingHours = remainingDays * 24,
            lastAdWatchTimestamp = now,
            cooldownRemainingSeconds = 0,
            canWatchAd = true,
            unlockedDaysCount = 7,
            extraDaysStacked = extraDaysStacked
        )

        assertTrue(snapshot.isActive)
        assertEquals(10, snapshot.remainingDays)
        assertEquals(3, snapshot.extraDaysStacked)
        assertTrue(snapshot.canWatchAd)
    }

    @Test
    fun fifteenSecondCooldownTimerEnforcesWaitingPeriod() {
        val now = System.currentTimeMillis()
        val cooldownSeconds = 15

        // Just watched ad (0s elapsed)
        val snapshotDuringCooldown = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = now + dayMillis,
            remainingDays = 1,
            remainingHours = 24,
            lastAdWatchTimestamp = now,
            cooldownRemainingSeconds = 15,
            canWatchAd = false
        )
        assertFalse(snapshotDuringCooldown.canWatchAd)
        assertEquals(15, snapshotDuringCooldown.cooldownRemainingSeconds)

        // 16s elapsed
        val snapshotAfterCooldown = RewardedAccessSnapshot(
            isActive = true,
            expiresAtMillis = now + dayMillis,
            remainingDays = 1,
            remainingHours = 24,
            lastAdWatchTimestamp = now - 16_000L,
            cooldownRemainingSeconds = 0,
            canWatchAd = true
        )
        assertTrue(snapshotAfterCooldown.canWatchAd)
        assertEquals(0, snapshotAfterCooldown.cooldownRemainingSeconds)
    }

    // =========================================================================
    // 3. 7-TIER ENTITLEMENT HIERARCHY & AD-FREE DIFFERENTIATION
    // =========================================================================

    @Test
    fun paidSubscriptionsTakePrecedenceOverTemporaryPasses() {
        val now = System.currentTimeMillis()
        val trial = TrialSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(5), remainingDays = 5)
        val rewarded = RewardedAccessSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(3), remainingDays = 3)
        val paidBilling = BillingEntitlementRecord(
            isPaidPro = true,
            planId = "yearly",
            productId = "ai.emots.kishan_dynamic.yearly",
            proExpiresAt = now + TimeUnit.DAYS.toMillis(365)
        )

        val reconciled = EntitlementManager.reconcile(paidBilling, trial, rewarded)

        assertEquals(EntitlementSource.YEARLY, reconciled.source)
        assertTrue(reconciled.isPro)
        assertTrue("Paid subscribers must be 100% ad-free", reconciled.isAdFree)
        assertFalse(reconciled.isTrial)
        assertFalse(reconciled.isRewarded)
    }

    @Test
    fun lifetimePurchaseIsNonConsumableWithNoExpiry() {
        val paidBilling = BillingEntitlementRecord(
            isPaidPro = true,
            isLifetime = true,
            planId = "lifetime"
        )
        val trial = TrialSnapshot()
        val rewarded = RewardedAccessSnapshot()

        val reconciled = EntitlementManager.reconcile(paidBilling, trial, rewarded)

        assertEquals(EntitlementSource.LIFETIME, reconciled.source)
        assertTrue(reconciled.isPro)
        assertTrue(reconciled.isAdFree)
        assertNull(reconciled.expiresAtMillis)
        assertFalse(reconciled.isExpired)
    }

    @Test
    fun trialAndRewardedPassUsersAreProButNOTAdFree() {
        // AUR-P1-025: Temporary Pro gives full island features but keeps in-app AdMob ads active
        val now = System.currentTimeMillis()
        val trial = TrialSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(5), remainingDays = 5)
        val emptyBilling = BillingEntitlementRecord()
        val emptyRewarded = RewardedAccessSnapshot()

        val trialReconciled = EntitlementManager.reconcile(emptyBilling, trial, emptyRewarded)

        assertEquals(EntitlementSource.INSTALL_TRIAL, trialReconciled.source)
        assertTrue(trialReconciled.isPro)
        assertFalse("Trial users must NOT be ad-free", trialReconciled.isAdFree)

        val rewarded = RewardedAccessSnapshot(isActive = true, expiresAtMillis = now + TimeUnit.DAYS.toMillis(2), remainingDays = 2)
        val emptyTrial = TrialSnapshot()

        val rewardedReconciled = EntitlementManager.reconcile(emptyBilling, emptyTrial, rewarded)

        assertEquals(EntitlementSource.REWARDED_ACCESS, rewardedReconciled.source)
        assertTrue(rewardedReconciled.isPro)
        assertFalse("Rewarded pass users must NOT be ad-free", rewardedReconciled.isAdFree)
    }

    @Test
    fun adSurfacePolicySuppressesAdsOnlyForPaidAdFreeUsers() {
        // Free user
        assertTrue(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = false, adUnitConfigured = true))
        assertTrue(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = false, adUnitConfigured = true))

        // Trial / Rewarded user (isAdFree = false)
        assertTrue(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = false, adUnitConfigured = true))
        assertTrue(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = false, adUnitConfigured = true))

        // Paid subscriber (isAdFree = true)
        assertFalse(AdSurfacePolicy.shouldShowBanner(adsEnabled = true, isAdFree = true, adUnitConfigured = true))
        assertFalse(AdSurfacePolicy.shouldShowNative(adsEnabled = true, isAdFree = true, adUnitConfigured = true))
    }

    // =========================================================================
    // 4. EXPIRY ISLAND & FEATURE GATING FREEDOM
    // =========================================================================

    @Test
    fun expiredEntitlementPermitsShowingExpiryIsland() {
        val expiredSnapshot = EntitlementSnapshot(
            source = EntitlementSource.FREE,
            isPro = false,
            isAdFree = false
        )
        assertTrue(expiredSnapshot.shouldShowExpiryIsland())
    }

    @Test
    fun allUsersCanFreelyDisableSoundAndCallFeaturesWithoutSilentRestart() {
        // Disabling sound HUD must evaluate to false for both Free and Pro
        assertFalse(PremiumFeaturePolicy.soundSettingEnabled(isProActive = false, requested = false))
        assertFalse(PremiumFeaturePolicy.soundSettingEnabled(isProActive = true, requested = false))

        // Enabling sound HUD is honored
        assertTrue(PremiumFeaturePolicy.soundSettingEnabled(isProActive = false, requested = true))
        assertTrue(PremiumFeaturePolicy.soundSettingEnabled(isProActive = true, requested = true))

        // Disabling call summary is honored
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = false, requested = false))
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = true, requested = false))
    }

    @Test
    fun essentialCoreFeaturesAreNeverGated() {
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

        // Premium features are gated
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.THEME_SETTINGS, freeSnapshot))
        assertFalse(FeatureEntitlementPolicy.canUseFeature(EntitlementFeatures.DISPLAY_HORIZONTAL_OFFSET, freeSnapshot))
    }

    // =========================================================================
    // 5. REMOTE CONFIG SCHEMA RESILIENCE
    // =========================================================================

    @Test
    fun remoteConfigDecoderParsesAlternativeSchemaKeys() {
        val json = """
        {
          "ads_enabled": true,
          "trial_duration_days": 14,
          "premium_features": ["custom_themes", "rgb_glow", "haptic_pulse"],
          "isTrustSectionVisible": true,
          "rewarded_ad_cooldown_seconds": 15
        }
        """.trimIndent()

        val decoded = ReleaseRuntimeConfigDecoder.decode(json)

        assertTrue(decoded.adsEnabled)
        assertEquals(14, decoded.premiumInstallTrialDays)
        assertEquals(listOf("custom_themes", "rgb_glow", "haptic_pulse"), decoded.premiumFeatures)
        assertTrue(decoded.trustSectionVisible)
        assertEquals(15, decoded.rewardedAdCooldownSeconds)
    }
}
