package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallNotificationPolicyTest {

    @Test
    fun recognizesDialingCallCopy() {
        val notification = notification(title = "John", text = "Calling")

        assertTrue(CallNotificationPolicy.isCall(notification))
        assertTrue(CallNotificationPolicy.isDialing(notification))
    }

    @Test
    fun connectedCallCopyIsNotDialing() {
        val notification = notification(title = "Ongoing call", text = "02:14")

        assertTrue(CallNotificationPolicy.isCall(notification))
        assertFalse(CallNotificationPolicy.isDialing(notification))
    }

    @Test
    fun ordinaryNotificationIsIgnored() {
        val notification = notification(title = "Calling all runners", text = "New post")

        assertFalse(CallNotificationPolicy.isCall(notification))
    }

    @Test
    fun callStyleNotificationExposesAnswerAndDeclineActionIds() {
        val notification = notification(title = "Ava", text = "Incoming call")
            .copy(
                category = "general",
                template = "android.app.Notification\$CallStyle",
                actions = listOf(
                    ai.emots.kishan_dynamic.data.model.NotificationActionInfo("0", "Answer"),
                    ai.emots.kishan_dynamic.data.model.NotificationActionInfo("1", "Decline")
                )
            )

        assertTrue(CallNotificationPolicy.isIncoming(notification))
        assertTrue(CallNotificationPolicy.incomingActionIds(notification) == ("0" to "1"))
    }

    @Test
    fun callStyleNotificationExposesVoipHangUpAction() {
        val notification = notification(title = "Ava", text = "Call in progress")
            .copy(
                category = "general",
                template = "android.app.Notification\$CallStyle",
                actions = listOf(
                    ai.emots.kishan_dynamic.data.model.NotificationActionInfo("0", "Hang up")
                )
            )

        assertTrue(CallNotificationPolicy.isOngoing(notification))
        assertTrue(CallNotificationPolicy.ongoingActionId(notification) == "0")
    }

    private fun notification(title: String, text: String) = NotificationInfo(
        id = title,
        packageName = "com.example.dialer",
        appName = "Phone",
        title = title,
        text = text,
        category = "call"
    ).let { if (title == "Calling all runners") it.copy(category = "social") else it }
}
