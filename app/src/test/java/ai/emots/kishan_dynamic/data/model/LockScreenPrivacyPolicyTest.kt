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

    @Test
    fun callRecordRedactsSensitiveCallerDataWhenLockedAndPrivacyEnabled() {
        val incomingRecord = CallRecord(
            id = "call-1",
            contactName = "Tamia Castillo",
            phoneNumber = "+1 (555) 019-2834",
            avatarUri = "/avatars/tamia.png",
            startedAtMillis = 1000L,
            durationSeconds = 165L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.RELIABLE_MONOTONIC
        )
        val redactedIncoming = LockScreenPrivacyPolicy.callRecord(incomingRecord, deviceLocked = true, hideSensitiveContent = true)
        assertEquals("Incoming call", redactedIncoming?.contactName)
        assertEquals("", redactedIncoming?.phoneNumber)
        assertNull(redactedIncoming?.avatarUri)
        assertEquals(165L, redactedIncoming?.durationSeconds)

        val outgoingRecord = incomingRecord.copy(direction = CallDirection.OUTGOING)
        val redactedOutgoing = LockScreenPrivacyPolicy.callRecord(outgoingRecord, deviceLocked = true, hideSensitiveContent = true)
        assertEquals("Outgoing call", redactedOutgoing?.contactName)
        assertEquals("", redactedOutgoing?.phoneNumber)

        val missedRecord = incomingRecord.copy(direction = CallDirection.MISSED)
        val redactedMissed = LockScreenPrivacyPolicy.callRecord(missedRecord, deviceLocked = true, hideSensitiveContent = true)
        assertEquals("Missed call", redactedMissed?.contactName)
        assertEquals("", redactedMissed?.phoneNumber)
    }

    @Test
    fun callRecordPreservesDataWhenUnlockedOrPrivacyDisabled() {
        val record = CallRecord(
            id = "call-1",
            contactName = "Tamia Castillo",
            phoneNumber = "+1 (555) 019-2834",
            avatarUri = "/avatars/tamia.png",
            startedAtMillis = 1000L,
            durationSeconds = 165L,
            direction = CallDirection.INCOMING
        )
        // When unlocked:
        assertEquals(record, LockScreenPrivacyPolicy.callRecord(record, deviceLocked = false, hideSensitiveContent = true))
        // When privacy setting disabled:
        assertEquals(record, LockScreenPrivacyPolicy.callRecord(record, deviceLocked = true, hideSensitiveContent = false))
        // When null:
        assertNull(LockScreenPrivacyPolicy.callRecord(null, deviceLocked = true, hideSensitiveContent = true))
    }
}
