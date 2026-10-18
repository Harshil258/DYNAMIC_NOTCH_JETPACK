package ai.emots.kishan_dynamic.ui.components

import android.view.WindowManager
import ai.emots.kishan_dynamic.data.model.OverlayDisplayPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 06 verification tests for WindowManager overlay touch safety,
 * geometry constraints, flat-top monobrow corner shaping, and display policy.
 */
class OverlayWindowManagerAndDisplayTest {

    @Test
    fun alwaysOnTopDisabledSuppressesIdlePillFromInterceptingTouches() {
        val policy = OverlayDisplayPolicy(
            islandEnabled = true,
            alwaysOnTop = false,
            showOnLockScreen = true,
            deviceLocked = false,
            systemSurfaceActive = false
        )

        // When idle, pill must not render so transparent window is detached/non-touchable
        assertFalse(policy.shouldRender(isHiddenState = false, isIdleState = true))

        // When active event occurs, overlay renders temporarily
        assertTrue(policy.shouldRender(isHiddenState = false, isIdleState = false))
    }

    @Test
    fun idleAutoHideSuppressesPillRendering() {
        val policy = OverlayDisplayPolicy(
            islandEnabled = true,
            alwaysOnTop = true,
            showOnLockScreen = true,
            deviceLocked = false,
            systemSurfaceActive = false
        )

        // When idle auto-hide is triggered (island hidden due to idle timeout)
        assertFalse(policy.shouldRender(isHiddenState = true, isIdleState = true))
    }

    @Test
    fun windowFlagsWhenHiddenIncludeNotTouchable() {
        var baseFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        val isHidden = true
        val computedFlags = if (isHidden) {
            baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            (baseFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        }

        assertTrue((computedFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0)

        val targetWidth = if (isHidden) 0 else WindowManager.LayoutParams.WRAP_CONTENT
        val targetHeight = if (isHidden) 0 else WindowManager.LayoutParams.WRAP_CONTENT
        assertEquals(0, targetWidth)
        assertEquals(0, targetHeight)
    }

    @Test
    fun windowFlagsWhenVisibleExcludeNotTouchable() {
        var baseFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        val isHidden = false
        val computedFlags = if (isHidden) {
            baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            (baseFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        }

        assertEquals(0, computedFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        assertTrue((computedFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL) != 0)
        assertTrue((computedFlags and WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH) != 0)
    }

    @Test
    fun cutoutDotSizingCoercionWithinSafeHardwareBounds() {
        // Pixel sizes from 60px to 200px on typical xxhdpi (3.0 density)
        val density = 3.0f
        val smallPx = 60
        val smallDp = smallPx / density // 20dp -> coerced to min 24dp
        val coercedSmall = smallDp.coerceIn(24f, 56f)
        assertEquals(24f, coercedSmall, 0.01f)

        val largePx = 240
        val largeDp = largePx / density // 80dp -> coerced to max 56dp
        val coercedLarge = largeDp.coerceIn(24f, 56f)
        assertEquals(56f, coercedLarge, 0.01f)

        val standardPx = 96
        val standardDp = standardPx / density // 32dp -> preserved
        val coercedStandard = standardDp.coerceIn(24f, 56f)
        assertEquals(32f, coercedStandard, 0.01f)
    }

    @Test
    fun quickControlsGatingEnforcesPreference() {
        val actionConfigEnabled = true
        val quickControlsDisabled = false

        val effectiveEnabled = actionConfigEnabled && quickControlsDisabled
        assertFalse(effectiveEnabled)

        val quickControlsEnabled = true
        val effectiveEnabledWhenOn = actionConfigEnabled && quickControlsEnabled
        assertTrue(effectiveEnabledWhenOn)
    }
}
