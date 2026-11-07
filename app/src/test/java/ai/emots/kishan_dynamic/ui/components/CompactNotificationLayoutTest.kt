package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class CompactNotificationLayoutTest {
    private val message = NotificationInfo("fixture", "test.app", "App", "A long private title", "A long private message")
    @Test fun collapsedStateUsesOnlyBoundedCountRatherThanPrivateCopy() {
        assertEquals("8", compactNotificationLabel(message, 8))
        assertEquals("99+", compactNotificationLabel(message, 1000))
        assertEquals("0", compactNotificationLabel(message, -1))
    }
    @Test fun genuineProgressReplacesCountWithoutDuplicatingBadges() {
        assertEquals("75%", compactNotificationLabel(message.copy(isOngoing = true, progress = 75, progressMax = 100), 8))
        assertEquals("8", compactNotificationLabel(message.copy(isOngoing = true, progress = 0, progressMax = 0), 8))
    }
}
