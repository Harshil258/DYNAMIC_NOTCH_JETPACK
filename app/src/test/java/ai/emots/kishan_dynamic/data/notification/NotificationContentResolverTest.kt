package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationContentResolverTest {

    @Test
    fun bigTextIsReservedForExpandedCopy() {
        val content = resolveNotificationContent(
            appName = "Messages",
            title = "Ava",
            text = "Short preview",
            bigText = "The complete message with more detail",
            subText = "Today",
            summaryText = null,
            conversationTitle = null
        )

        assertEquals("Short preview", content.compactText)
        assertEquals("The complete message with more detail", content.expandedText)
        assertEquals("Today", content.subText)
    }

    @Test
    fun conversationAndSummaryProvideSafeFallbacks() {
        val content = resolveNotificationContent(
            appName = "Mail",
            title = " ",
            text = null,
            bigText = null,
            subText = null,
            summaryText = "2 new messages",
            conversationTitle = "Inbox"
        )

        assertEquals("Inbox", content.title)
        assertEquals("2 new messages", content.compactText)
        assertEquals("2 new messages", content.expandedText)
    }

    @Test
    fun inboxLinesBecomeTheExpandedConversationCopy() {
        val content = resolveNotificationContent(
            appName = "Messages",
            title = "Group chat",
            text = "3 new messages",
            bigText = null,
            subText = null,
            summaryText = null,
            conversationTitle = null,
            inboxLines = listOf("Ava: Are we still on?", "Leo: Yes, see you at eight.")
        )

        assertEquals("Ava: Are we still on?\nLeo: Yes, see you at eight.", content.expandedText)
        assertEquals(2, content.inboxLines.size)
    }
}
