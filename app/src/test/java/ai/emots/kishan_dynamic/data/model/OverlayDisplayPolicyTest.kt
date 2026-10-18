package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayDisplayPolicyTest {
    @Test
    fun lockedDeviceCanRenderOnlyWhenLockScreenVisibilityIsEnabled() {
        assertTrue(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = true,
                showOnLockScreen = true,
                deviceLocked = true
            ).shouldRender(isHiddenState = false)
        )
        assertFalse(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = true,
                showOnLockScreen = false,
                deviceLocked = true
            ).shouldRender(isHiddenState = false)
        )
    }

    @Test
    fun disabledOverlayAndHiddenStateNeverRender() {
        assertFalse(
            OverlayDisplayPolicy(
                islandEnabled = false,
                alwaysOnTop = true,
                showOnLockScreen = true,
                deviceLocked = false
            ).shouldRender(isHiddenState = false)
        )
        assertFalse(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = true,
                showOnLockScreen = true,
                deviceLocked = false
            ).shouldRender(isHiddenState = true)
        )
    }

    @Test
    fun alwaysOnTopDisabledSuppressesIdleStateWhileAllowingActiveEvents() {
        // When Always on Top is false and island is idle, shouldRender is FALSE (window is hidden & non-touchable)
        assertFalse(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = false,
                showOnLockScreen = true,
                deviceLocked = false
            ).shouldRender(isHiddenState = false, isIdleState = true)
        )

        // When Always on Top is false but an active event occurs (notification, call, hud), shouldRender is TRUE
        assertTrue(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = false,
                showOnLockScreen = true,
                deviceLocked = false
            ).shouldRender(isHiddenState = false, isIdleState = false)
        )

        // When Always on Top is true, idle state renders normally
        assertTrue(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = true,
                showOnLockScreen = true,
                deviceLocked = false
            ).shouldRender(isHiddenState = false, isIdleState = true)
        )
    }

    @Test
    fun systemSurfaceActiveSuppressesRendering() {
        assertFalse(
            OverlayDisplayPolicy(
                islandEnabled = true,
                alwaysOnTop = true,
                showOnLockScreen = true,
                deviceLocked = false,
                systemSurfaceActive = true
            ).shouldRender(isHiddenState = false, isIdleState = false)
        )
    }
}
