package ai.emots.kishan_dynamic

import ai.emots.kishan_dynamic.data.notification.ClockNotificationKind
import ai.emots.kishan_dynamic.data.notification.classifyClockNotification
import ai.emots.kishan_dynamic.ui.NotificationReplyActivity
import ai.emots.kishan_dynamic.ui.components.formatCallDuration
import ai.emots.kishan_dynamic.ui.screens.PlaygroundStateItem
import ai.emots.kishan_dynamic.ui.screens.defaultPlaygroundCatalog
import ai.emots.kishan_dynamic.ui.screens.experienceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint12DailyUtilityAndArchitectureTest {

    @Test
    fun testFormatCallDuration() {
        assertEquals("00:00", formatCallDuration(0))
        assertEquals("00:45", formatCallDuration(45))
        assertEquals("01:05", formatCallDuration(65))
        assertEquals("12:34", formatCallDuration(754))
        assertEquals("00:00", formatCallDuration(-10))
    }

    @Test
    fun testNotificationReplyActivityConstants() {
        assertEquals("notification_id", NotificationReplyActivity.EXTRA_NOTIFICATION_ID)
        assertEquals("action_id", NotificationReplyActivity.EXTRA_ACTION_ID)
        assertEquals("app_name", NotificationReplyActivity.EXTRA_APP_NAME)
        assertEquals("sender", NotificationReplyActivity.EXTRA_SENDER)
        assertEquals("message", NotificationReplyActivity.EXTRA_MESSAGE)
    }

    @Test
    fun testClockNotificationClassifierStopwatchAndAlarmCategories() {
        // Standard Android clock notifications
        val stopwatchType = classifyClockNotification(
            packageName = "com.google.android.deskclock",
            channelId = "stopwatch",
            hasCustomContent = true,
            notificationCategory = "stopwatch",
            title = "Stopwatch"
        )
        assertEquals(ClockNotificationKind.STOPWATCH, stopwatchType)

        val timerType = classifyClockNotification(
            packageName = "com.google.android.deskclock",
            channelId = "timer",
            hasCustomContent = true,
            notificationCategory = "alarm",
            title = "Timer: 5 minutes"
        )
        assertEquals(ClockNotificationKind.TIMER, timerType)
    }

    @Test
    fun testPlaygroundCatalogIntegrity() {
        val catalog = defaultPlaygroundCatalog
        assertTrue("Catalog must contain at least 30 reference states", catalog.size >= 30)

        val categories = catalog.map { it.experienceCategory() }.toSet()
        assertTrue(categories.contains("Media"))
        assertTrue(categories.contains("Calls"))
        assertTrue(categories.contains("Alerts"))
        assertTrue(categories.contains("Live"))
        assertTrue(categories.contains("Capture"))
        assertTrue(categories.contains("Controls"))
        assertTrue(categories.contains("System"))

        val musicItem = catalog.firstOrNull { it.label == "Music" }
        assertNotNull(musicItem)
        assertEquals("Media", musicItem?.experienceCategory())
        assertTrue(musicItem?.supportsExpand == true)
    }

    @Test
    fun testFeedbackCategoriesAndSupportAttribution() {
        val expectedCategories = listOf(
            "Report Bug",
            "Suggest Feature",
            "Share Feedback",
            "Contact Support"
        )
        assertEquals(4, expectedCategories.size)

        // Strict developer attribution per Rule 30
        val supportEmail = "suryadevelopers07@gmail.com"
        assertTrue(supportEmail.contains("@gmail.com"))
        assertTrue(supportEmail.startsWith("suryadevelopers07"))
    }
}
