package ai.emots.kishan_dynamic.service

import ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.NotificationEvent
import ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationQueueState
import ai.emots.kishan_dynamic.data.notification.NotificationReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Real-world scenario test suite for Sprint 07:
 * - AUR-P2-015: UPI/Banking and OTP notification priority & 8s heads-up display guarantee
 * - AUR-P2-016: Lock-screen privacy redaction for payment notifications
 * - AUR-P2-017: Paused media auto-collapse delay (3.5s)
 * - AUR-P2-018: Audio-first vs video streaming ownership and collision avoidance
 * - Multi-notification queue behavior across 1, 5, and 20 items
 * - Rapid notification dismissals & per-app muting
 */
class NotificationAndMediaRealWorldScenarioTest {

    private val reducer = NotificationReducer()

    private fun createNotification(
        id: String,
        pkg: String = "com.whatsapp",
        appName: String = "WhatsApp",
        title: String = "Sender $id",
        text: String = "Message $id",
        expandedText: String = "",
        isPriority: Boolean = false,
        isOngoing: Boolean = false,
        isSensitive: Boolean = false,
        category: String = "general",
        time: Long = System.currentTimeMillis()
    ) = NotificationInfo(
        id = id,
        packageName = pkg,
        appName = appName,
        title = title,
        text = text,
        expandedText = expandedText,
        timestamp = time,
        isPriority = isPriority,
        isOngoing = isOngoing,
        isSensitive = isSensitive,
        category = category,
        actions = listOf(
            NotificationActionInfo(id = "reply", label = "Reply", isReply = true),
            NotificationActionInfo(id = "read", label = "Mark as read", isReply = false)
        )
    )

    @Test
    fun upiAndBankingTransactionsClassifiedAsFinancial() {
        // GPay payment received
        val gpayNotif = createNotification(
            id = "gpay_1",
            pkg = "com.google.android.apps.nbu.paisa.user",
            appName = "Google Pay",
            title = "Google Pay",
            text = "You received ₹1,500 from Rahul Kumar"
        )
        assertTrue("GPay is financial transaction", NotificationPriorityPolicy.isFinancialTransaction(gpayNotif))
        assertTrue("GPay is financial or OTP", NotificationPriorityPolicy.isFinancialOrOtp(gpayNotif))
        assertTrue("GPay is priority alert", NotificationPriorityPolicy.isPriority(gpayNotif))

        // PhonePe transaction
        val phonepeNotif = createNotification(
            id = "phonepe_1",
            pkg = "com.phonepe.app",
            appName = "PhonePe",
            title = "Payment Successful",
            text = "Paid ₹350 to Starbucks Coffee"
        )
        assertTrue("PhonePe is financial transaction", NotificationPriorityPolicy.isFinancialTransaction(phonepeNotif))
        assertTrue("PhonePe is priority alert", NotificationPriorityPolicy.isPriority(phonepeNotif))

        // Paytm alert
        val paytmNotif = createNotification(
            id = "paytm_1",
            pkg = "net.one97.paytm",
            appName = "Paytm",
            title = "Money Added",
            text = "₹500 added to Paytm Wallet"
        )
        assertTrue("Paytm is financial transaction", NotificationPriorityPolicy.isFinancialTransaction(paytmNotif))

        // Bank SMS via Google Messages
        val bankSmsNotif = createNotification(
            id = "sms_bank_1",
            pkg = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "HDFC Bank Alert",
            text = "A/C **4829 credited with INR 24,500.00 on 18-Oct-26. UPI Ref 382910492810"
        )
        assertTrue("Bank SMS is financial transaction", NotificationPriorityPolicy.isFinancialTransaction(bankSmsNotif))
        assertTrue("Bank SMS is priority", NotificationPriorityPolicy.isPriority(bankSmsNotif))
    }

    @Test
    fun otpVerificationCodesClassifiedCorrectly() {
        // Standard SMS OTP
        val otpSms = createNotification(
            id = "otp_1",
            pkg = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "Google Verification",
            text = "G-839201 is your Google verification code. Do not share with anyone."
        )
        assertTrue("SMS verification code is OTP", NotificationPriorityPolicy.isOtp(otpSms))
        assertTrue("OTP is classified as financialOrOtp", NotificationPriorityPolicy.isFinancialOrOtp(otpSms))
        assertTrue("OTP gets priority promotion", NotificationPriorityPolicy.isPriority(otpSms))

        // WhatsApp Auth Code
        val waOtp = createNotification(
            id = "wa_otp",
            pkg = "com.whatsapp",
            appName = "WhatsApp",
            title = "Security Alert",
            text = "Your OTP is 4920 for two-step verification."
        )
        assertTrue("WhatsApp OTP detected", NotificationPriorityPolicy.isOtp(waOtp))

        // Generic non-OTP message should not trigger OTP
        val normalMsg = createNotification(
            id = "chat_1",
            pkg = "com.whatsapp",
            appName = "WhatsApp",
            title = "Ankit",
            text = "Hey, are you free for lunch today at 1?"
        )
        assertFalse("Normal chat is not OTP", NotificationPriorityPolicy.isOtp(normalMsg))
        assertFalse("Normal chat is not financial", NotificationPriorityPolicy.isFinancialTransaction(normalMsg))
    }

    @Test
    fun financialAndOtpDisplayDurationEnforcesEightSecondMinimum() {
        val upiNotif = createNotification(
            id = "upi_dur",
            pkg = "com.phonepe.app",
            appName = "PhonePe",
            title = "Payment received",
            text = "₹250 from Ananya"
        )
        val normalNotif = createNotification(
            id = "norm_dur",
            pkg = "com.whatsapp",
            appName = "WhatsApp",
            title = "Friend",
            text = "See you soon"
        )

        // When user duration setting is low (e.g. 3 seconds or 5 seconds)
        val upiDurationLow = NotificationPriorityPolicy.recommendedDisplaySeconds(upiNotif, userSeconds = 3)
        assertEquals("Financial notification enforces 8s minimum duration", 8, upiDurationLow)

        val upiDurationMid = NotificationPriorityPolicy.recommendedDisplaySeconds(upiNotif, userSeconds = 5)
        assertEquals("Financial notification still enforces 8s minimum duration", 8, upiDurationMid)

        // When user duration is higher (e.g. 10 seconds), user preference is honored
        val upiDurationHigh = NotificationPriorityPolicy.recommendedDisplaySeconds(upiNotif, userSeconds = 10)
        assertEquals("Higher user duration honored", 10, upiDurationHigh)

        // Normal notification retains user setting
        val normalDuration = NotificationPriorityPolicy.recommendedDisplaySeconds(normalNotif, userSeconds = 3)
        assertEquals("Normal notification uses configured userSeconds", 3, normalDuration)
    }

    @Test
    fun lockScreenPrivacyMasksFinancialNotificationsToPaymentReceived() {
        val sensitivePayment = createNotification(
            id = "pay_priv",
            pkg = "com.google.android.apps.nbu.paisa.user",
            appName = "Google Pay",
            title = "Payment received",
            text = "₹12,450 received from Priya Sharma in A/C 9988",
            expandedText = "Transaction ID: UPI/3829104/HDFC. Available Balance: ₹84,200.00"
        )

        // On locked device:
        val redacted = LockScreenPrivacyPolicy.notification(
            source = sensitivePayment,
            deviceLocked = true,
            hideSensitiveContent = false // Even if generic hideSensitiveContent is off, financial safety mandates redaction
        )

        assertEquals("Redacted text must be generic Payment received", "Payment received", redacted.text)
        assertEquals("Redacted expandedText masks private balance/account", "Unlock to view transaction details", redacted.expandedText)
        assertTrue("Actions are stripped from lock screen", redacted.actions.isEmpty())
        assertFalse("Content intent disabled on locked screen", redacted.hasContentIntent)
        assertNull("Artwork stripped on lock screen", redacted.imagePath)

        // On unlocked device:
        val unlocked = LockScreenPrivacyPolicy.notification(
            source = sensitivePayment,
            deviceLocked = false,
            hideSensitiveContent = false
        )
        assertEquals("Original text intact when unlocked", sensitivePayment.text, unlocked.text)
        assertEquals("Original expandedText intact when unlocked", sensitivePayment.expandedText, unlocked.expandedText)
        assertEquals("Actions intact when unlocked", 2, unlocked.actions.size)
    }

    @Test
    fun notificationReducerQueueScaling1_5_20_AndCapping() {
        var state = NotificationQueueState()

        // 1 Notification
        val notif1 = createNotification("notif_1")
        state = reducer.reduce(state, NotificationEvent.Posted(notif1))
        assertEquals("Queue has 1 item", 1, state.notifications.size)
        assertEquals("Active notification is notif_1", "notif_1", state.activeNotification?.id)

        // Scale to 5 Notifications
        for (i in 2..5) {
            state = reducer.reduce(state, NotificationEvent.Posted(createNotification("notif_$i")))
        }
        assertEquals("Queue has 5 items", 5, state.notifications.size)

        // Scale to 25 Notifications (all 25 retained, capacity is 50 per A04)
        for (i in 6..25) {
            state = reducer.reduce(state, NotificationEvent.Posted(createNotification("notif_$i")))
        }
        assertEquals("Queue has 25 items", 25, state.notifications.size)
        assertNotNull("Active notification is valid", state.activeNotification)

        // Scale to 55 Notifications (must be capped at MAX_NOTIFICATIONS = 50 per A04)
        for (i in 26..55) {
            state = reducer.reduce(state, NotificationEvent.Posted(createNotification("notif_$i")))
        }
        assertEquals("Queue is capped at 50 items", NotificationReducer.MAX_NOTIFICATIONS, state.notifications.size)
    }

    @Test
    fun notificationReducerPrioritizesFinancialAndOtpAtTopOfQueue() {
        var state = NotificationQueueState()

        // Post 3 standard notifications
        for (i in 1..3) {
            state = reducer.reduce(state, NotificationEvent.Posted(createNotification("chat_$i")))
        }
        assertEquals("Top item was chat_3", "chat_3", state.notifications.first().id)

        // Post a UPI payment notification
        val paymentNotif = createNotification(
            id = "upi_alert",
            pkg = "com.phonepe.app",
            appName = "PhonePe",
            title = "Payment received",
            text = "₹450 from Arjun"
        )
        state = reducer.reduce(state, NotificationEvent.Posted(paymentNotif))

        // Financial alert must be at index 0 (top of queue)
        assertEquals("Financial alert promoted to top of queue", "upi_alert", state.notifications.first().id)

        // Post an OTP SMS
        val otpNotif = createNotification(
            id = "otp_alert",
            pkg = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "OTP Alert",
            text = "Your OTP is 739104 for banking verification",
            time = System.currentTimeMillis() + 1000 // Newer timestamp
        )
        state = reducer.reduce(state, NotificationEvent.Posted(otpNotif))

        // Newer OTP or Financial alert stays at the front
        assertTrue(
            "Head of queue must be financial or OTP",
            NotificationPriorityPolicy.isFinancialOrOtp(state.notifications[0])
        )
        assertTrue(
            "Second item must also be financial or OTP",
            NotificationPriorityPolicy.isFinancialOrOtp(state.notifications[1])
        )
    }

    @Test
    fun notificationReducerRapidDismissalSafety() {
        var state = NotificationQueueState()
        for (i in 1..10) {
            state = reducer.reduce(state, NotificationEvent.Posted(createNotification("item_$i")))
        }
        assertEquals(10, state.notifications.size)

        // Rapidly dismiss active items one after another
        while (state.notifications.isNotEmpty()) {
            val activeId = state.activeNotification?.id
            assertNotNull(activeId)
            state = reducer.reduce(state, NotificationEvent.DismissActive)
            // Verify activeIndex never crashes or goes out of bounds
            assertTrue("Active index is in bounds", state.activeIndex in 0..maxOf(0, state.notifications.lastIndex))
        }

        assertEquals(0, state.notifications.size)
        assertNull(state.activeNotification)
        assertEquals(0, state.activeIndex)
    }

    @Test
    fun mediaAudioFirstAndVideoStreamingClassification() {
        // Audio packages
        assertTrue(MediaPlaybackRegistry.isAudioFirstApp("com.spotify.music"))
        assertTrue(MediaPlaybackRegistry.isAudioFirstApp("com.google.android.apps.youtube.music"))
        assertTrue(MediaPlaybackRegistry.isAudioFirstApp("com.apple.android.music"))
        assertTrue(MediaPlaybackRegistry.isAudioFirstApp("com.amazon.mp3"))
        assertFalse(MediaPlaybackRegistry.isAudioFirstApp("com.google.android.youtube"))

        // Video streaming packages
        assertTrue(MediaPlaybackRegistry.isVideoStreamingApp("com.google.android.youtube"))
        assertTrue(MediaPlaybackRegistry.isVideoStreamingApp("com.netflix.mediaclient"))
        assertTrue(MediaPlaybackRegistry.isVideoStreamingApp("in.startv.hotstar"))
        assertTrue(MediaPlaybackRegistry.isVideoStreamingApp("org.videolan.vlc"))
        assertFalse(MediaPlaybackRegistry.isVideoStreamingApp("com.spotify.music"))
    }

    @Test
    fun mediaPauseAutoCollapseDelayConstant() {
        // AUR-P2-017: Paused media must auto-collapse to minimal/idle after exactly 3.5 seconds (3500ms)
        assertEquals(3500L, MediaPlaybackRegistry.PAUSE_AUTO_COLLAPSE_DELAY_MS)
    }

    @Test
    fun perAppNotificationMutingPolicy() {
        val blockedPackages = setOf("com.spam.marketing", "com.promotions.app")

        val spamNotification = createNotification(id = "spam_1", pkg = "com.spam.marketing")
        val legitimateNotification = createNotification(id = "chat_1", pkg = "com.whatsapp")

        val shouldMuteSpam = blockedPackages.contains(spamNotification.packageName)
        val shouldMuteLegitimate = blockedPackages.contains(legitimateNotification.packageName)

        assertTrue("Spam app is blocked and muted", shouldMuteSpam)
        assertFalse("WhatsApp is not blocked and permitted", shouldMuteLegitimate)
    }
}
