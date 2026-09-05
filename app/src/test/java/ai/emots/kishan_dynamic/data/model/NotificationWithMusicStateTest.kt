package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationWithMusicStateTest {
    @Test
    fun splitStateRetainsTheWholePendingNotificationQueue() {
        val first = NotificationInfo(
            id = "first",
            packageName = "com.example.chat",
            appName = "Chat",
            title = "Maya",
            text = "See you soon"
        )
        val second = first.copy(id = "second", title = "Dev")
        val state = IslandState.NotificationWithMusic(
            notifications = listOf(first, second),
            track = MusicTrack(title = "Aurora", artist = "Kishan")
        )

        assertEquals(listOf("first", "second"), state.notifications.map(NotificationInfo::id))
    }
}
