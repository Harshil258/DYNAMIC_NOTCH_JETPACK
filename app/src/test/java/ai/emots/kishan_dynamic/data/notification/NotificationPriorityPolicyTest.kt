package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPriorityPolicyTest {

    @Test
    fun classifiesReferenceLiveActivityCategories() {
        assertTrue(NotificationPriorityPolicy.isPriority(notification(category = "navigation")))
        assertTrue(NotificationPriorityPolicy.isPriority(notification(category = "timer")))
        assertTrue(NotificationPriorityPolicy.isPriority(notification(category = "transport")))
        assertTrue(NotificationPriorityPolicy.isPriority(notification(category = "call")))
    }

    @Test
    fun keepsRegularDismissibleNotificationOutOfSplitPresentation() {
        assertFalse(NotificationPriorityPolicy.isPriority(notification(category = "social")))
    }

    private fun notification(category: String) = NotificationInfo(
        id = category,
        packageName = "com.example.app",
        appName = "Example",
        title = "Update",
        text = "A message",
        category = category
    )
}
