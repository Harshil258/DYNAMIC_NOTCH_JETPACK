package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Classifies notifications that deserve the live-activity compact treatment
 * and instant heads-up interrupt over ongoing background activities.
 *
 * Includes navigation, calls, alarms, timers, as well as critical
 * financial transactions (UPI, banking debit/credit) and OTP verification codes.
 */
object NotificationPriorityPolicy {

    private val priorityCategories = setOf(
        "navigation",
        "call",
        "missed_call",
        "transport",
        "alarm",
        "timer",
        "stopwatch"
    )

    val upiAndFinancePackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                       // PhonePe
        "net.one97.paytm",                       // Paytm
        "in.org.npci.upiapp",                    // BHIM UPI
        "com.sbi.upi",                           // SBI YONO / Pay
        "com.icicibank.pockets",                 // Pockets
        "com.csam.icici.bank.imobile",           // iMobile
        "com.hdfcbank.payzapp",                  // PayZapp
        "com.axis.mobile",                       // Axis Mobile
        "com.bankofbaroda.mconnect",             // BoB Mobile
        "com.kotak.mbusiness",                   // Kotak 811
        "com.cred.club",                         // CRED
        "com.mobikwik_new",                      // MobiKwik
        "com.freecharge.android"                 // Freecharge
    )

    val messagingPackages = setOf(
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.whatsapp",
        "org.telegram.messenger",
        "com.facebook.orca"
    )

    private val otpKeywords = listOf(
        "otp", "one time password", "verification code", "security code",
        "auth code", "is your verification code", "secret code", "login code"
    )
    private val otpRegex = Regex("""\b\d{4,8}\b""")

    private val paymentKeywords = listOf(
        "credited", "debited", "payment received", "paid to", "sent to",
        "transaction alert", "transferred", "money received", "upi ref",
        "inr", "rs.", "₹", "acct", "a/c", "spent", "withdrawn"
    )

    fun isOtp(notification: NotificationInfo): Boolean {
        val fullText = "${notification.title} ${notification.text} ${notification.expandedText}".lowercase()
        val hasKeyword = otpKeywords.any { fullText.contains(it) }
        return hasKeyword && otpRegex.containsMatchIn(fullText)
    }

    fun isFinancialTransaction(notification: NotificationInfo): Boolean {
        val pkg = notification.packageName.lowercase()
        val fullText = "${notification.title} ${notification.text} ${notification.expandedText}".lowercase()
        val isFinanceApp = upiAndFinancePackages.any { pkg.contains(it) }
        val hasPaymentWord = paymentKeywords.any { fullText.contains(it) }
        return isFinanceApp || (hasPaymentWord && (messagingPackages.any { pkg.contains(it) } || fullText.contains("bank") || fullText.contains("upi")))
    }

    fun isFinancialOrOtp(notification: NotificationInfo): Boolean =
        isOtp(notification) || isFinancialTransaction(notification)

    fun isPriority(notification: NotificationInfo): Boolean =
        notification.isPriority ||
            notification.isOngoing ||
            notification.category in priorityCategories ||
            notification.title.contains("incoming call", ignoreCase = true) ||
            notification.title.contains("missed call", ignoreCase = true) ||
            CallNotificationPolicy.isMissedCall(notification) ||
            isFinancialOrOtp(notification)

    fun recommendedDisplaySeconds(notification: NotificationInfo, userSeconds: Int): Int {
        if (isFinancialOrOtp(notification)) {
            return maxOf(userSeconds, 8) // Guarantee at least 8s for OTP/financial alerts
        }
        return userSeconds
    }
}
