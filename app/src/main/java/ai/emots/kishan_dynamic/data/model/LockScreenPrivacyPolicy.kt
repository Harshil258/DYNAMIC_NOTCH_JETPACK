package ai.emots.kishan_dynamic.data.model

/** Pure presentation redaction: lock screen and classified sensitive unlocked previews. */
object LockScreenPrivacyPolicy {

    fun notification(
        source: NotificationInfo,
        deviceLocked: Boolean,
        hideSensitiveContent: Boolean
    ): NotificationInfo {
        val classifiedSensitive = ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy.isFinancialOrOtp(source)
        if (!deviceLocked && !(hideSensitiveContent && classifiedSensitive)) return source

        // PII & Financial Safety Contract (AUR-P2-016):
        // Financial balances, transaction amounts, and account numbers must NEVER
        // be displayed on the ambient lock screen to bystanders.
        if (deviceLocked && ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy.isFinancialTransaction(source)) {
            return source.copy(
                title = source.appName.ifBlank { "Transaction Alert" },
                text = "Payment received",
                expandedText = "Unlock to view transaction details",
                subText = null,
                inboxLines = emptyList(),
                imagePath = null,
                picturePath = null,
                actions = emptyList(),
                hasContentIntent = false
            )
        }

        if (!hideSensitiveContent || (!source.isSensitive && !classifiedSensitive)) return source
        return source.copy(
            title = source.appName,
            text = "New notification",
            expandedText = if (deviceLocked) "Unlock to view" else "Content hidden by privacy settings",
            subText = null,
            inboxLines = emptyList(),
            imagePath = null,
            picturePath = null,
            actions = emptyList(),
            hasContentIntent = false
        )
    }

    fun contact(
        source: ContactInfo?,
        deviceLocked: Boolean,
        hideSensitiveContent: Boolean,
        incoming: Boolean
    ): ContactInfo? {
        if (source == null || !deviceLocked || !hideSensitiveContent) return source
        return ContactInfo(
            name = if (incoming) "Incoming call" else "Active call",
            phoneNumber = "",
            avatarUri = null
        )
    }

    fun callRecord(
        source: CallRecord?,
        deviceLocked: Boolean,
        hideSensitiveContent: Boolean
    ): CallRecord? {
        if (source == null || !deviceLocked || !hideSensitiveContent) return source
        val fallbackName = when (source.direction) {
            CallDirection.INCOMING -> "Incoming call"
            CallDirection.OUTGOING -> "Outgoing call"
            CallDirection.MISSED -> "Missed call"
        }
        return source.copy(
            contactName = fallbackName,
            phoneNumber = "",
            avatarUri = null
        )
    }
}
