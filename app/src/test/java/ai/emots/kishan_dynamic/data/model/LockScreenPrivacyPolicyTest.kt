package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LockScreenPrivacyPolicyTest {

    @Test
    fun sensitiveOtpIsRedactedOnLockAndDuringProtectedUnlockedPreview() {
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
        assertEquals("New notification", LockScreenPrivacyPolicy.notification(source, false, true).text)
        assertEquals(source, LockScreenPrivacyPolicy.notification(source, false, false))
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
    @org.junit.Test fun unlockedOtpAndFinanceRespectSensitivePreviewPreference() {
        val otp = NotificationInfo(id = "otp-fixture", packageName = "demo.messages", appName = "Messages", title = "Security code", text = "Your verification code is 123456")
        val safe = LockScreenPrivacyPolicy.notification(otp, false, true)
        org.junit.Assert.assertEquals("New notification", safe.text)
        org.junit.Assert.assertFalse(safe.expandedText.contains("123456"))
        org.junit.Assert.assertEquals(otp, LockScreenPrivacyPolicy.notification(otp, false, false))
        val finance = otp.copy(packageName = "com.google.android.apps.nbu.paisa.user", title = "Transaction", text = "Payment received")
        org.junit.Assert.assertEquals("New notification", LockScreenPrivacyPolicy.notification(finance, false, true).text)
    }
}
