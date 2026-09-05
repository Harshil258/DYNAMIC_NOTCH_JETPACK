package ai.emots.kishan_dynamic.data.liveactivity

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveActivitySurfacePolicyTest {
    @Test
    fun specializedSourceOwnsItsNotificationSurface() {
        assertTrue(
            LiveActivitySurfacePolicy.usesSpecializedSurface(
                activeNotificationId = "maps-route",
                specializedNotificationIds = setOf("maps-route")
            )
        )
    }

    @Test
    fun ordinaryNotificationsKeepTheNotificationSurface() {
        assertFalse(
            LiveActivitySurfacePolicy.usesSpecializedSurface(
                activeNotificationId = "chat-message",
                specializedNotificationIds = setOf("maps-route")
            )
        )
        assertFalse(
            LiveActivitySurfacePolicy.usesSpecializedSurface(
                activeNotificationId = null,
                specializedNotificationIds = setOf("maps-route")
            )
        )
    }
}
