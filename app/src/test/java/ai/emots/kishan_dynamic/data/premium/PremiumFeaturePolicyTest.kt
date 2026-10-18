package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumFeaturePolicyTest {
    @Test
    fun compactMusicControlsRequireActivePro() {
        assertTrue(PremiumFeaturePolicy.compactMusicControlsEnabled(isProActive = true, requested = true))
        assertFalse(PremiumFeaturePolicy.compactMusicControlsEnabled(isProActive = false, requested = true))
        assertFalse(PremiumFeaturePolicy.compactMusicControlsEnabled(isProActive = true, requested = false))
    }

    @Test
    fun referenceFeatureIdsAreLockedOnlyForFreeUsers() {
        assertTrue(PremiumFeaturePolicy.isLocked(PremiumFeaturePolicy.SOUND_SETTINGS, false))
        assertTrue(PremiumFeaturePolicy.isLocked(PremiumFeaturePolicy.DISPLAY_HORIZONTAL_OFFSET, false))
        assertFalse(PremiumFeaturePolicy.isLocked(PremiumFeaturePolicy.THEME_SETTINGS, true))
    }

    @Test
    fun freeUsersRetainSafeDefaultsForPersistedPremiumSettings() {
        assertFalse(PremiumFeaturePolicy.soundSettingEnabled(isProActive = false, requested = false))
        assertTrue(PremiumFeaturePolicy.soundSettingEnabled(isProActive = false, requested = true))
        assertFalse(PremiumFeaturePolicy.horizontalOffsetEnabled(false))
    }

    @Test
    fun freeUsersCanFreelyOptOutOfCallSummaries() {
        // AUR-P0-004 verification: Both free and Pro users can toggle call summaries off
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = false, requested = false))
        assertTrue(PremiumFeaturePolicy.callSummaryEnabled(isProActive = false, requested = true))
        assertFalse(PremiumFeaturePolicy.callSummaryEnabled(isProActive = true, requested = false))
        assertTrue(PremiumFeaturePolicy.callSummaryEnabled(isProActive = true, requested = true))
    }

    @Test
    fun callSummaryCustomizationRequiresActivePro() {
        assertFalse(PremiumFeaturePolicy.callSummaryCustomizationEnabled(isProActive = false))
        assertTrue(PremiumFeaturePolicy.callSummaryCustomizationEnabled(isProActive = true))
    }
}
