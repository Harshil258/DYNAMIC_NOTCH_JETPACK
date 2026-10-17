package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo

enum class NotificationPresentationType { STANDARD, NAVIGATION, TRANSPORT, PROGRESS, PICTURE, TIMER }

/** Message prose and app names are not Android capabilities. */
object NotificationPresentationPolicy {
    fun classify(source: NotificationInfo): NotificationPresentationType = when {
        source.progressMax > 0 || source.isProgressIndeterminate -> NotificationPresentationType.PROGRESS
        source.showChronometer || source.chronometerBaseElapsedRealtime > 0 ||
            source.category == "timer" || source.category == "stopwatch" -> NotificationPresentationType.TIMER
        source.isOngoing && source.category == "navigation" -> NotificationPresentationType.NAVIGATION
        source.picturePath != null -> NotificationPresentationType.PICTURE
        // Android CATEGORY_TRANSPORT describes media, not a taxi reservation.
        else -> NotificationPresentationType.STANDARD
    }
}

/** Conservative extra lock-screen protection; never derives a payment outcome. */
object NotificationSensitiveContentPolicy {
    private val sensitiveCopy = Regex(
        "\\b(otp|one[ -]time (password|code)|verification code|security code|payment|transaction|debited|credited|account balance)\\b",
        RegexOption.IGNORE_CASE
    )
    fun requiresProtection(platformSensitive: Boolean, title: String, text: String): Boolean =
        platformSensitive || sensitiveCopy.containsMatchIn("$title $text")
}
