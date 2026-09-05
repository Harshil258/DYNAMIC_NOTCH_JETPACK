package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialAdFrequencyPolicyTest {

    @Test
    fun showsOnConfiguredScreenBoundary() {
        assertTrue(InterstitialAdFrequencyPolicy.shouldShow(4, 0, 4, 3, 0))
        assertFalse(InterstitialAdFrequencyPolicy.shouldShow(5, 0, 4, 3, 0))
    }

    @Test
    fun showsOnConfiguredNavigationBoundary() {
        assertTrue(InterstitialAdFrequencyPolicy.shouldShow(1, 3, 4, 3, 0))
        assertFalse(InterstitialAdFrequencyPolicy.shouldShow(1, 2, 4, 3, 0))
    }

    @Test
    fun doesNotShowBeforeConfiguredStartScreen() {
        assertFalse(InterstitialAdFrequencyPolicy.shouldShow(2, 3, 4, 3, 4))
        assertTrue(InterstitialAdFrequencyPolicy.shouldShow(4, 3, 4, 3, 4))
    }

    @Test
    fun disabledIntervalsNeverTrigger() {
        assertFalse(InterstitialAdFrequencyPolicy.shouldShow(4, 4, 0, 0, 0))
    }
}
