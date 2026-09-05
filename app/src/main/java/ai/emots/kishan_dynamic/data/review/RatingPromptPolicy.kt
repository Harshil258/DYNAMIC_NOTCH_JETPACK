package ai.emots.kishan_dynamic.data.review

/** Product-owned rules for the delayed, non-invasive review request. */
object RatingPromptPolicy {
    const val minimumAppOpens = 3
    const val maximumDismissals = 3
    const val dismissalCooldownMillis = 3L * 24L * 60L * 60L * 1000L

    fun shouldPrompt(
        appOpenCount: Int,
        hasRated: Boolean,
        dismissalCount: Int,
        lastDismissedAtMillis: Long,
        nowMillis: Long,
        enabled: Boolean = true
    ): Boolean {
        if (!enabled || hasRated) return false
        if (appOpenCount < minimumAppOpens || dismissalCount >= maximumDismissals) return false
        if (lastDismissedAtMillis > 0L && nowMillis - lastDismissedAtMillis < dismissalCooldownMillis) {
            return false
        }
        return true
    }
}
