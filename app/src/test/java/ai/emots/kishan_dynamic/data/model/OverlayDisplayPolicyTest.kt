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
}
