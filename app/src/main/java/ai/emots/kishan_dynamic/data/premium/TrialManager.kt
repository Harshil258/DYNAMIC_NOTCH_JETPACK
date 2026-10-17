package ai.emots.kishan_dynamic.data.premium

import android.content.Context
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * State representing the status of the installation trial.
 */
data class TrialSnapshot(
    val isActive: Boolean = false,
    val isExpired: Boolean = false,
    val startTimeMillis: Long = 0L,
    val expiresAtMillis: Long = 0L,
    val durationDays: Int = 0,
    val remainingHours: Int = 0,
    val remainingDays: Int = 0,
    val isTampered: Boolean = false
)

/**
 * Centralized Trial Manager coordinating the 7-day initial installation trial.
 * Enforces monotonic clock validation to guard against clock rollback tampering.
 */
class TrialManager(
    context: Context,
    private val preferences: AuroraPreferences = AuroraPreferences(context.applicationContext)
) {
    companion object {
        const val DEFAULT_TRIAL_DAYS = 7
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000L
    }

    val trialSnapshotFlow: Flow<TrialSnapshot> = combine(
        preferences.trialStartTime,
        preferences.trialExpiresAt,
        preferences.trialDurationDays,
        preferences.lastObservedTimestamp
    ) { startTime, expiresAt, durationDays, lastObserved ->
        calculateSnapshot(startTime, expiresAt, durationDays, lastObserved)
    }

    /**
     * Initializes or verifies the trial on app launch.
     * Starts the trial if it has never been initialized.
     */
    suspend fun checkOrStartTrial(
        runtimeConfig: ReleaseRuntimeConfig = ReleaseRuntimeConfig()
    ): TrialSnapshot {
        val now = System.currentTimeMillis()
        val currentStart = preferences.trialStartTime.first()
        val currentExpires = preferences.trialExpiresAt.first()
        val currentDuration = preferences.trialDurationDays.first()
        val lastObserved = preferences.lastObservedTimestamp.first()

        // 1. Clock rollback check
        val isClockRolledBack = lastObserved > 0L && now < (lastObserved - TimeUnit.MINUTES.toMillis(5))

        // Update observed timestamp monotonically
        val maxObserved = maxOf(now, lastObserved)
        preferences.updateObservedTimestamp(maxObserved)

        // 2. Start trial if not yet started
        if (currentStart == 0L || currentExpires == 0L) {
            val configDays = if (runtimeConfig.premiumInstallTrialDays > 0) {
                runtimeConfig.premiumInstallTrialDays
            } else {
                DEFAULT_TRIAL_DAYS
            }
            val expiresAt = now + (configDays * DAY_MILLIS)
            preferences.setTrial(startTime = now, durationDays = configDays, expiresAt = expiresAt)
            return TrialSnapshot(
                isActive = true,
                isExpired = false,
                startTimeMillis = now,
                expiresAtMillis = expiresAt,
                durationDays = configDays,
                remainingHours = configDays * 24,
                remainingDays = configDays,
                isTampered = false
            )
        }

        return calculateSnapshot(currentStart, currentExpires, currentDuration, maxObserved, isClockRolledBack)
    }

    suspend fun getSnapshot(): TrialSnapshot {
        val now = System.currentTimeMillis()
        val currentStart = preferences.trialStartTime.first()
        val currentExpires = preferences.trialExpiresAt.first()
        val currentDuration = preferences.trialDurationDays.first()
        val lastObserved = preferences.lastObservedTimestamp.first()
        val isClockRolledBack = lastObserved > 0L && now < (lastObserved - TimeUnit.MINUTES.toMillis(5))

        if (now > lastObserved) {
            preferences.updateObservedTimestamp(now)
        }

        return calculateSnapshot(currentStart, currentExpires, currentDuration, maxOf(now, lastObserved), isClockRolledBack)
    }

    internal fun calculateSnapshot(
        startTime: Long,
        expiresAt: Long,
        durationDays: Int,
        lastObserved: Long,
        clockRolledBack: Boolean = false
    ): TrialSnapshot {
        if (startTime == 0L || expiresAt == 0L) {
            return TrialSnapshot()
        }

        val effectiveNow = maxOf(System.currentTimeMillis(), lastObserved)
        val isExpired = effectiveNow >= expiresAt
        val remainingMillis = (expiresAt - effectiveNow).coerceAtLeast(0L)
        val remainingHours = (remainingMillis / (1000L * 60 * 60)).toInt()
        val remainingDays = (remainingMillis / DAY_MILLIS).toInt() + if (remainingMillis % DAY_MILLIS > 0) 1 else 0

        return TrialSnapshot(
            isActive = !isExpired && !clockRolledBack,
            isExpired = isExpired,
            startTimeMillis = startTime,
            expiresAtMillis = expiresAt,
            durationDays = durationDays,
            remainingHours = remainingHours,
            remainingDays = remainingDays,
            isTampered = clockRolledBack
        )
    }
}
