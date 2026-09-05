package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDisplayDurationPolicyTest {
    @Test
    fun exposesTheReferenceDurationChoicesInOrder() {
        assertEquals(listOf(2, 3, 5, 8, 10), NotificationDisplayDurationPolicy.options)
        assertEquals(setOf(3, 5), NotificationDisplayDurationPolicy.freeOptions)
    }

    @Test
    fun preservesSupportedValuesAndRepairsLegacyValues() {
        NotificationDisplayDurationPolicy.options.forEach { seconds ->
            assertEquals(seconds, NotificationDisplayDurationPolicy.sanitize(seconds))
        }
        assertEquals(5, NotificationDisplayDurationPolicy.sanitize(4))
        assertEquals(5, NotificationDisplayDurationPolicy.sanitize(0))
        assertTrue(NotificationDisplayDurationPolicy.sanitize(99) in NotificationDisplayDurationPolicy.options)
    }
}
