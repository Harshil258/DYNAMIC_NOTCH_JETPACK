package ai.emots.kishan_dynamic.data.model

/** Pure redaction rules used only for lock-screen presentation. */
object LockScreenPrivacyPolicy {

    fun notification(
        source: NotificationInfo,
        deviceLocked: Boolean,
        hideSensitiveContent: Boolean
    ): NotificationInfo {
        if (!deviceLocked || !hideSensitiveContent || !source.isSensitive) return source
        return source.copy(
            title = source.appName,
            text = "New notification",
            expandedText = "Unlock to view",
            subText = null,
            inboxLines = emptyList(),
            imagePath = null,
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
}
