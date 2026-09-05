package ai.emots.kishan_dynamic.data.premium

/** Pure entitlement timing rules, kept independent from the ad SDK. */
object RewardedPassPolicy {
    /** Matches the reference product's default seven-day rewarded entitlement. */
    const val DEFAULT_HOURS = 7 * 24
    private const val MILLIS_PER_HOUR = 60L * 60L * 1000L

    fun expiresAt(nowMillis: Long, durationHours: Int): Long? {
        if (durationHours <= 0) return null
        return nowMillis + durationHours.toLong() * MILLIS_PER_HOUR
    }

    fun durationLabel(durationHours: Int): String {
        if (durationHours > 0 && durationHours % 24 == 0) {
            val days = durationHours / 24
            return "$days day${if (days == 1) "" else "s"}"
        }
        return "$durationHours hour${if (durationHours == 1) "" else "s"}"
    }
}
