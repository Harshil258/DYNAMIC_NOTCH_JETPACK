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
        assertTrue(PremiumFeaturePolicy.soundSettingEnabled(false, false))
        assertTrue(PremiumFeaturePolicy.callSummaryEnabled(false, false))
        assertFalse(PremiumFeaturePolicy.horizontalOffsetEnabled(false))
    }
}
