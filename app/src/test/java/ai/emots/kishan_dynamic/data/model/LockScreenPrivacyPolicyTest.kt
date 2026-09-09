package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LockScreenPrivacyPolicyTest {

    @Test
    fun sensitiveNotificationIsRedactedOnlyWhileLocked() {
        val source = NotificationInfo(
            id = "otp",
            packageName = "test.bank",
            appName = "Bank",
            title = "OTP 123456",
            text = "Use 123456",
            imagePath = "/private/image",
            actions = listOf(NotificationActionInfo("copy", "Copy")),
            isSensitive = true
        )
        val redacted = LockScreenPrivacyPolicy.notification(source, true, true)

        assertEquals("Bank", redacted.title)
        assertEquals("New notification", redacted.text)
        assertEquals("Unlock to view", redacted.expandedText)
        assertNull(redacted.imagePath)
        assertEquals(emptyList<NotificationActionInfo>(), redacted.actions)
        assertEquals(source, LockScreenPrivacyPolicy.notification(source, false, true))
    }

    @Test
    fun explicitlyPublicNotificationCanRemainVisible() {
        val source = NotificationInfo(
            id = "weather",
            packageName = "test.weather",
            appName = "Weather",
            title = "Rain",
            text = "Starting soon",
            isSensitive = false
        )
        assertEquals(source, LockScreenPrivacyPolicy.notification(source, true, true))
    }

    @Test
    fun callIdentityIsMaskedWhileLocked() {
        val source = ContactInfo("Alice", "+123", "/avatar")
        val redacted = LockScreenPrivacyPolicy.contact(source, true, true, incoming = true)
        assertEquals("Incoming call", redacted?.name)
        assertEquals("", redacted?.phoneNumber)
        assertNull(redacted?.avatarUri)
    }
}
