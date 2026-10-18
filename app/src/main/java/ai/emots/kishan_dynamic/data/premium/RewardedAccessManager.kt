package ai.emots.kishan_dynamic.data.premium

import android.content.Context
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Snapshot of Rewarded Ad-based premium access.
 */
data class RewardedAccessSnapshot(
    val isActive: Boolean = false,
    val expiresAtMillis: Long = 0L,
    val remainingDays: Int = 0,
    val remainingHours: Int = 0,
    val lastAdWatchTimestamp: Long = 0L,
    val cooldownRemainingSeconds: Int = 0,
    val canWatchAd: Boolean = true,
    val unlockedDaysCount: Int = 0,
    val extraDaysStacked: Int = 0,
    val checkmarks: List<Pair<String, Boolean>> = emptyList()
)

/**
 * Manages rewarded-ad-based temporary Premium access.
 * 1 ad = 24 hours of Pro features (stackable).
 * Maintains a 15-second cooldown between ads and 7-day progress indicators.
 */
class RewardedAccessManager(
    context: Context,
    private val preferences: AuroraPreferences = AuroraPreferences(context.applicationContext)
) {
    companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000L
        const val DEFAULT_COOLDOWN_SECONDS = 15
        const val MAX_VISIBLE_CHECKMARK_DAYS = 7
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val displayFormat = SimpleDateFormat("MMM d", Locale.US)
    }

    val rewardedSnapshotFlow: Flow<RewardedAccessSnapshot> = combine(
        preferences.rewardPremiumExpiry,
        preferences.lastAdWatchTimestamp,
        preferences.adRewardDaysJson
    ) { expiry, lastWatch, daysJson ->
        buildSnapshot(expiry, lastWatch, daysJson, DEFAULT_COOLDOWN_SECONDS)
    }

    /**
     * Records a watched rewarded ad, extending the reward expiry by [daysToAdd] (default 1 day = 24h).
     * Stacks onto current expiry if already active, or starts from now.
     */
    suspend fun recordAdReward(
        daysToAdd: Int = 1,
        cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS
    ): RewardedAccessSnapshot {
        val now = System.currentTimeMillis()
        val currentExpiry = preferences.rewardPremiumExpiry.first()
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val newExpiry = baseTime + (daysToAdd.toLong() * DAY_MILLIS)

        // Update 7-day progress map
        val daysMap = parseDaysMap(preferences.adRewardDaysJson.first()).toMutableMap()
        val cal = Calendar.getInstance()
        for (i in 0 until MAX_VISIBLE_CHECKMARK_DAYS) {
            val key = dateFormat.format(cal.time)
            if (daysMap[key] != true) {
                daysMap[key] = true
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val updatedJson = JSONObject(daysMap as Map<*, *>).toString()

        preferences.setRewardPremium(
            expiry = newExpiry,
            lastWatchTime = now,
            daysJson = updatedJson
        )

        return buildSnapshot(newExpiry, now, updatedJson, cooldownSeconds)
    }

    /**
     * Checks if cooldown has passed and ad can be watched.
     */
    suspend fun isEligibleForNextAd(cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS): Boolean {
        val lastWatch = preferences.lastAdWatchTimestamp.first()
        if (lastWatch == 0L) return true
        val elapsed = System.currentTimeMillis() - lastWatch
        return elapsed >= (cooldownSeconds * 1000L)
    }

    /**
     * Calculates remaining cooldown time in milliseconds.
     */
    suspend fun getRemainingCooldownMs(cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS): Long {
        val lastWatch = preferences.lastAdWatchTimestamp.first()
        if (lastWatch == 0L) return 0L
        val elapsed = System.currentTimeMillis() - lastWatch
        val totalCooldown = cooldownSeconds * 1000L
        val remaining = totalCooldown - elapsed
        return if (remaining > 0L) remaining else 0L
    }

    /**
     * Removes past dates from the 7-day checkmarks map.
     */
    suspend fun cleanupExpiredDays() {
        val daysJson = preferences.adRewardDaysJson.first()
        val daysMap = parseDaysMap(daysJson).toMutableMap()
        val todayKey = dateFormat.format(Date())

        val pastKeys = daysMap.keys.filter { it < todayKey }
        if (pastKeys.isNotEmpty()) {
            pastKeys.forEach { daysMap.remove(it) }
            val newJson = JSONObject(daysMap as Map<*, *>).toString()
            val expiry = preferences.rewardPremiumExpiry.first()
            val lastWatch = preferences.lastAdWatchTimestamp.first()
            preferences.setRewardPremium(expiry, lastWatch, newJson)
        }
    }

    suspend fun getSnapshot(cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS): RewardedAccessSnapshot {
        val expiry = preferences.rewardPremiumExpiry.first()
        val lastWatch = preferences.lastAdWatchTimestamp.first()
        val daysJson = preferences.adRewardDaysJson.first()
        return buildSnapshot(expiry, lastWatch, daysJson, cooldownSeconds)
    }

    internal fun buildSnapshot(
        expiry: Long,
        lastWatch: Long,
        daysJson: String,
        cooldownSeconds: Int
    ): RewardedAccessSnapshot {
        val now = System.currentTimeMillis()
        val isActive = expiry > now
        val remainingMillis = (expiry - now).coerceAtLeast(0L)
        val remainingHours = (remainingMillis / (1000L * 60 * 60)).toInt()
        val remainingDays = (remainingMillis / DAY_MILLIS).toInt() + if (remainingMillis % DAY_MILLIS > 0) 1 else 0

        val cooldownMs = if (lastWatch == 0L) 0L else (cooldownSeconds * 1000L - (now - lastWatch)).coerceAtLeast(0L)
        val cooldownSec = (cooldownMs / 1000L).toInt() + if (cooldownMs % 1000L > 0L) 1 else 0
        val canWatch = cooldownSec <= 0

        val daysMap = parseDaysMap(daysJson)
        val cal = Calendar.getInstance()
        val checkmarks = mutableListOf<Pair<String, Boolean>>()
        var unlockedCount = 0

        for (i in 0 until MAX_VISIBLE_CHECKMARK_DAYS) {
            val dateKey = dateFormat.format(cal.time)
            val dateLabel = displayFormat.format(cal.time)
            val unlocked = daysMap[dateKey] == true
            if (unlocked) unlockedCount++
            checkmarks.add(Pair(dateLabel, unlocked))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val extraDays = (remainingDays - MAX_VISIBLE_CHECKMARK_DAYS).coerceAtLeast(0)

        return RewardedAccessSnapshot(
            isActive = isActive,
            expiresAtMillis = expiry,
            remainingDays = remainingDays,
            remainingHours = remainingHours,
            lastAdWatchTimestamp = lastWatch,
            cooldownRemainingSeconds = cooldownSec,
            canWatchAd = canWatch,
            unlockedDaysCount = unlockedCount,
            extraDaysStacked = extraDays,
            checkmarks = checkmarks
        )
    }

    private fun parseDaysMap(json: String): Map<String, Boolean> {
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Boolean>()
            obj.keys().forEach { key ->
                map[key] = obj.optBoolean(key, false)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
