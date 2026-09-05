package ai.emots.kishan_dynamic.data.notification

/** Normalized notification copy used by the island renderer. */
data class ResolvedNotificationContent(
    val title: String,
    val compactText: String,
    val expandedText: String,
    val subText: String?,
    val inboxLines: List<String> = emptyList()
)

/**
 * Chooses the richest available platform copy without coupling the domain to
 * Android Bundle details. Short text remains the compact value; big text is
 * reserved for the expanded experience.
 */
fun resolveNotificationContent(
    appName: String,
    title: String?,
    text: String?,
    bigText: String?,
    subText: String?,
    summaryText: String?,
    conversationTitle: String?,
    inboxLines: List<String> = emptyList()
): ResolvedNotificationContent {
    val shortText = text.clean() ?: summaryText.clean() ?: bigText.clean() ?: subText.clean().orEmpty()
    val cleanInboxLines = inboxLines.mapNotNull { it.clean() }
    val expandedText = cleanInboxLines.takeIf { it.isNotEmpty() }?.joinToString("\n")
        ?: bigText.clean()
        ?: text.clean()
        ?: summaryText.clean()
        ?: subText.clean().orEmpty()
    return ResolvedNotificationContent(
        title = title.clean() ?: conversationTitle.clean() ?: appName,
        compactText = shortText,
        expandedText = expandedText,
        subText = subText.clean(),
        inboxLines = cleanInboxLines
    )
}

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
