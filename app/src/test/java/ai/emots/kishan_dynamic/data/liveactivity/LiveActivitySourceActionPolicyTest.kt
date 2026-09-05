package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveActivitySourceActionPolicyTest {

    @Test
    fun prefersUndoOrContinuationActionOverNotificationOpen() {
        val notification = notification(hasContentIntent = true).copy(
            actions = listOf(
                NotificationActionInfo("0", "Undo"),
                NotificationActionInfo("1", "Open")
            )
        )

        assertEquals("0", LiveActivitySourceActionPolicy.primaryActionId(notification))
    }

    @Test
    fun usesContentIntentWhenNoSemanticActionExists() {
        val notification = notification(hasContentIntent = true)

        assertEquals("open", LiveActivitySourceActionPolicy.primaryActionId(notification))
    }

    @Test
    fun doesNotInventSourceAction() {
        assertNull(LiveActivitySourceActionPolicy.primaryActionId(notification()))
    }

    private fun notification(hasContentIntent: Boolean = false) = NotificationInfo(
        id = "find-my",
        packageName = "com.google.android.apps.adm",
        appName = "Find My Device",
        title = "Device located",
        text = "Nearby",
        isOngoing = true,
        hasContentIntent = hasContentIntent
    )
}
