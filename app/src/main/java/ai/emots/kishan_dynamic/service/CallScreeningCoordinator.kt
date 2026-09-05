package ai.emots.kishan_dynamic.service

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * Small process-safe bridge for caller identity that Android exposes through
 * the call-screening role but may omit from the legacy PHONE_STATE broadcast.
 */
object CallScreeningCoordinator {
    private var currentNumber: String? = null
    private var capturedAtMillis: Long = 0L

    @Synchronized
    fun capture(number: String?) {
        val normalized = number?.trim().orEmpty()
        if (normalized.isBlank()) return
        currentNumber = normalized
        capturedAtMillis = System.currentTimeMillis()
    }

    @Synchronized
    fun currentNumber(): String? {
        if (currentNumber == null || System.currentTimeMillis() - capturedAtMillis > RETENTION_MILLIS) {
            currentNumber = null
            capturedAtMillis = 0L
        }
        return currentNumber
    }

    @Synchronized
    fun clear() {
        currentNumber = null
        capturedAtMillis = 0L
    }

    private const val RETENTION_MILLIS = 30_000L
}

object CallScreeningRole {
    fun isAvailable(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && runCatching {
            context.getSystemService(RoleManager::class.java)
                ?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true
        }.getOrDefault(false)

    fun isHeld(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && runCatching {
            context.getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        }.getOrDefault(false)

    fun requestIntent(context: Context): Intent? =
        if (!isAvailable(context) || isHeld(context)) null else runCatching {
            context.getSystemService(RoleManager::class.java)
                ?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        }.getOrNull()
}

/** Monitoring-only screening service. It never blocks, rejects, or silences calls. */
class IslandCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        CallScreeningCoordinator.capture(callDetails.handle?.schemeSpecificPart)
        respondToCall(
            callDetails,
            CallScreeningService.CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        )
    }
}
