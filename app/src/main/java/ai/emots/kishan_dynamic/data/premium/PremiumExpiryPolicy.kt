package ai.emots.kishan_dynamic.data.premium

/** Pure policy for deciding when the overlay should surface a premium reminder. */
object PremiumExpiryPolicy {
    const val WARNING_WINDOW_HOURS = 24L
    private const val MILLIS_PER_HOUR = 60L * 60L * 1000L

    fun remainingWarningHours(expiresAtMillis: Long, nowMillis: Long): Int? {
        val remainingMillis = expiresAtMillis - nowMillis
        if (remainingMillis <= 0L || remainingMillis > WARNING_WINDOW_HOURS * MILLIS_PER_HOUR) {
            return null
        }
        return (remainingMillis / MILLIS_PER_HOUR).toInt().coerceAtLeast(1)
    }
}
