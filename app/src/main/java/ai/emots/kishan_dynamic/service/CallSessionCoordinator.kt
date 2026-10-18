package ai.emots.kishan_dynamic.service

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.repository.CallHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Lifecycle states of an in-progress or recently completed phone call session.
 */
enum class CallSessionState {
    IDLE,
    RINGING,
    DIALING_OR_OUTGOING,
    ACTIVE,
    ENDED,
    MISSED,
    REJECTED,
    UNKNOWN
}

/**
 * Immutable snapshot of an in-flight call session.
 */
data class ActiveCallSession(
    val sessionId: String,
    val contact: ContactInfo,
    val direction: CallDirection,
    val state: CallSessionState,
    val startedWallClockMillis: Long,
    val ringStartRealtime: Long = 0L,
    val offHookRealtime: Long = 0L,
    val endedRealtime: Long = 0L,
    val wasOffHook: Boolean = false,
    val wasAnswered: Boolean = false
)

/**
 * Centralized, thread-safe call session state machine.
 *
 * Resolves AUR-P0-003 by eliminating ephemeral instance fields from
 * BroadcastReceivers. Preserves caller context, monotonic call durations,
 * and session deduplication across the entire telephony lifecycle.
 */
object CallSessionCoordinator {

    private val sessionLock = Any()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @VisibleForTesting
    var elapsedRealtimeProvider: () -> Long = { SystemClock.elapsedRealtime() }

    @VisibleForTesting
    var currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() }

    private var currentSession: ActiveCallSession? = null

    private val _activeCallSummary = MutableStateFlow<CallRecord?>(null)
    val activeCallSummary: StateFlow<CallRecord?> = _activeCallSummary.asStateFlow()

    private val _lastCompletedRecord = MutableStateFlow<CallRecord?>(null)
    val lastCompletedRecord: StateFlow<CallRecord?> = _lastCompletedRecord.asStateFlow()

    @VisibleForTesting
    fun resetForTesting() {
        synchronized(sessionLock) {
            currentSession = null
            _activeCallSummary.value = null
            _lastCompletedRecord.value = null
            elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
            currentTimeMillisProvider = { System.currentTimeMillis() }
        }
    }

    @VisibleForTesting
    fun currentSessionSnapshot(): ActiveCallSession? = synchronized(sessionLock) { currentSession }

    fun clearActiveCallSummary() {
        synchronized(sessionLock) {
            _activeCallSummary.value = null
        }
    }

    /**
     * Entrypoint called by [TelephonyStateReceiver].
     * Guarantees asynchronous contact lookup, session transitions, DataStore persistence,
     * and UI dispatch without risking BroadcastReceiver timeout.
     */
    fun onPhoneStateChanged(
        context: Context,
        stateStr: String?,
        incomingNumberExtra: String?,
        onComplete: (() -> Unit)? = null
    ) {
        val rawNumber = incomingNumberExtra?.trim()?.takeIf { it.isNotBlank() }
            ?: CallScreeningCoordinator.currentNumber()
            ?: "Unknown Caller"

        scope.launch {
            try {
                handleStateTransition(context, stateStr, rawNumber)
            } finally {
                onComplete?.invoke()
            }
        }
    }

    @VisibleForTesting
    suspend fun handleStateTransition(
        context: Context?,
        stateStr: String?,
        rawNumber: String
    ) {
        val normalizedNumber = if (rawNumber.isBlank()) "Unknown Caller" else rawNumber

        val upperState = stateStr?.trim()?.uppercase()
        when (upperState) {
            "RINGING", TelephonyManager.EXTRA_STATE_RINGING -> handleRinging(context, normalizedNumber)
            "OFFHOOK", TelephonyManager.EXTRA_STATE_OFFHOOK -> handleOffHook(context, normalizedNumber)
            "IDLE", TelephonyManager.EXTRA_STATE_IDLE -> handleIdle(context)
            else -> Unit
        }
    }

    private const val PREFS_ACTIVE_SESSION = "aurora_active_call_session"
    private const val KEY_SESSION_ID = "session_id"
    private const val KEY_CONTACT_NAME = "contact_name"
    private const val KEY_CONTACT_PHONE = "contact_phone"
    private const val KEY_CONTACT_AVATAR = "contact_avatar"
    private const val KEY_DIRECTION = "direction"
    private const val KEY_STATE = "state"
    private const val KEY_STARTED_WALL_CLOCK = "started_wall_clock"
    private const val KEY_RING_START_REALTIME = "ring_start_realtime"
    private const val KEY_OFFHOOK_REALTIME = "offhook_realtime"
    private const val KEY_WAS_OFFHOOK = "was_offhook"
    private const val KEY_WAS_ANSWERED = "was_answered"

    @VisibleForTesting
    var testPersistedSession: ActiveCallSession? = null

    @VisibleForTesting
    fun persistActiveSession(context: Context?, session: ActiveCallSession?) {
        testPersistedSession = session
        if (context == null || session == null) return
        runCatching {
            val prefs = context.getSharedPreferences(PREFS_ACTIVE_SESSION, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_SESSION_ID, session.sessionId)
                .putString(KEY_CONTACT_NAME, session.contact.name)
                .putString(KEY_CONTACT_PHONE, session.contact.phoneNumber)
                .putString(KEY_CONTACT_AVATAR, session.contact.avatarUri.orEmpty())
                .putString(KEY_DIRECTION, session.direction.name)
                .putString(KEY_STATE, session.state.name)
                .putLong(KEY_STARTED_WALL_CLOCK, session.startedWallClockMillis)
                .putLong(KEY_RING_START_REALTIME, session.ringStartRealtime)
                .putLong(KEY_OFFHOOK_REALTIME, session.offHookRealtime)
                .putBoolean(KEY_WAS_OFFHOOK, session.wasOffHook)
                .putBoolean(KEY_WAS_ANSWERED, session.wasAnswered)
                .apply()
        }
    }

    @VisibleForTesting
    fun restoreActiveSession(context: Context?): ActiveCallSession? {
        if (testPersistedSession != null) return testPersistedSession
        if (context == null) return null
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS_ACTIVE_SESSION, Context.MODE_PRIVATE)
            val sessionId = prefs.getString(KEY_SESSION_ID, null) ?: return@runCatching null
            val phone = prefs.getString(KEY_CONTACT_PHONE, null) ?: return@runCatching null
            val name = prefs.getString(KEY_CONTACT_NAME, phone) ?: phone
            val avatar = prefs.getString(KEY_CONTACT_AVATAR, "").takeIf { !it.isNullOrBlank() }
            val directionStr = prefs.getString(KEY_DIRECTION, CallDirection.INCOMING.name)
            val direction = runCatching { CallDirection.valueOf(directionStr!!) }.getOrDefault(CallDirection.INCOMING)
            val stateStr = prefs.getString(KEY_STATE, CallSessionState.ACTIVE.name)
            val state = runCatching { CallSessionState.valueOf(stateStr!!) }.getOrDefault(CallSessionState.ACTIVE)
            val startedWall = prefs.getLong(KEY_STARTED_WALL_CLOCK, 0L)
            val ringReal = prefs.getLong(KEY_RING_START_REALTIME, 0L)
            val offHookReal = prefs.getLong(KEY_OFFHOOK_REALTIME, 0L)
            val wasOffHook = prefs.getBoolean(KEY_WAS_OFFHOOK, false)
            val wasAnswered = prefs.getBoolean(KEY_WAS_ANSWERED, false)

            ActiveCallSession(
                sessionId = sessionId,
                contact = ContactInfo(name = name, phoneNumber = phone, avatarUri = avatar),
                direction = direction,
                state = state,
                startedWallClockMillis = startedWall,
                ringStartRealtime = ringReal,
                offHookRealtime = offHookReal,
                wasOffHook = wasOffHook,
                wasAnswered = wasAnswered
            )
        }.getOrNull()
    }

    @VisibleForTesting
    fun clearPersistedSession(context: Context?) {
        testPersistedSession = null
        if (context == null) return
        runCatching {
            context.getSharedPreferences(PREFS_ACTIVE_SESSION, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }

    private suspend fun handleRinging(context: Context?, phoneNumber: String) {
        val contact = resolveContact(context, phoneNumber)
        val nowRealtime = elapsedRealtimeProvider()
        val nowWallClock = currentTimeMillisProvider()

        var shouldNotifyIsland = false

        synchronized(sessionLock) {
            val existing = currentSession
            if (existing != null && existing.state == CallSessionState.RINGING) {
                // Deduplicate OEM duplicate RINGING broadcasts. Update contact if number became known.
                if (existing.contact.phoneNumber == "Unknown Caller" && phoneNumber != "Unknown Caller") {
                    val updated = existing.copy(contact = contact)
                    currentSession = updated
                    persistActiveSession(context, updated)
                }
                return@synchronized
            }

            val newSession = ActiveCallSession(
                sessionId = UUID.randomUUID().toString(),
                contact = contact,
                direction = CallDirection.INCOMING,
                state = CallSessionState.RINGING,
                startedWallClockMillis = nowWallClock,
                ringStartRealtime = nowRealtime,
                wasOffHook = false,
                wasAnswered = false
            )
            currentSession = newSession
            persistActiveSession(context, newSession)
            shouldNotifyIsland = true
        }

        if (shouldNotifyIsland) {
            if (context != null) {
                runCatching {
                    val preferences = AuroraPreferences(context)
                    if (preferences.callBannerEnabled.first()) {
                        IslandStateManager.postIncomingCall(contact)
                    }
                }
            } else {
                IslandStateManager.postIncomingCall(contact)
            }
        }
    }

    private suspend fun handleOffHook(context: Context?, fallbackNumber: String) {
        val nowRealtime = elapsedRealtimeProvider()
        val nowWallClock = currentTimeMillisProvider()

        var postOngoingContact: ContactInfo? = null
        var isDialing = false
        var showDuration = true

        synchronized(sessionLock) {
            val existing = currentSession
            when {
                existing != null && existing.state == CallSessionState.RINGING -> {
                    // Incoming call was ANSWERED by user
                    val updated = existing.copy(
                        state = CallSessionState.ACTIVE,
                        offHookRealtime = nowRealtime,
                        wasOffHook = true,
                        wasAnswered = true
                    )
                    currentSession = updated
                    persistActiveSession(context, updated)
                    postOngoingContact = updated.contact
                    isDialing = false
                }
                existing != null && (existing.state == CallSessionState.ACTIVE || existing.state == CallSessionState.DIALING_OR_OUTGOING) -> {
                    // Deduplicate repeated OFFHOOK broadcast
                    return@synchronized
                }
                else -> {
                    // Transition directly from IDLE to OFFHOOK -> Outgoing Call
                    val contact = resolveContact(context, fallbackNumber)
                    val newSession = ActiveCallSession(
                        sessionId = UUID.randomUUID().toString(),
                        contact = contact,
                        direction = CallDirection.OUTGOING,
                        state = CallSessionState.DIALING_OR_OUTGOING,
                        startedWallClockMillis = nowWallClock,
                        offHookRealtime = nowRealtime,
                        wasOffHook = true,
                        wasAnswered = true
                    )
                    currentSession = newSession
                    persistActiveSession(context, newSession)
                    postOngoingContact = contact
                    isDialing = true
                }
            }
        }

        postOngoingContact?.let { contact ->
            if (context != null) {
                runCatching {
                    val preferences = AuroraPreferences(context)
                    if (preferences.callBannerEnabled.first()) {
                        showDuration = preferences.callTimerEnabled.first()
                        IslandStateManager.postOngoingCall(
                            contact = contact,
                            durationSeconds = 0L,
                            showDuration = showDuration,
                            isDialing = isDialing
                        )
                    }
                }
            } else {
                IslandStateManager.postOngoingCall(
                    contact = contact,
                    durationSeconds = 0L,
                    showDuration = showDuration,
                    isDialing = isDialing
                )
            }
        }
    }

    private suspend fun handleIdle(context: Context?) {
        val nowRealtime = elapsedRealtimeProvider()
        val nowWallClock = currentTimeMillisProvider()
        var completedRecord: CallRecord? = null
        var wasConnected = false
        var wasRinging = false

        synchronized(sessionLock) {
            var session = currentSession
            if (session == null) {
                // Recover from process death if active session was persisted
                session = restoreActiveSession(context)
                currentSession = session
            }

            if (session == null || session.state == CallSessionState.IDLE) {
                // Deduplicate repeated IDLE broadcast
                clearPersistedSession(context)
                return@synchronized
            }

            wasConnected = session.wasOffHook
            wasRinging = session.state == CallSessionState.RINGING

            val (durationSeconds, accuracy) = if (session.wasOffHook) {
                val wallClockElapsedSeconds = ((nowWallClock - session.startedWallClockMillis) / 1000L).coerceAtLeast(0L)
                val monotonicElapsed = nowRealtime - session.offHookRealtime

                when {
                    session.offHookRealtime <= 0L -> wallClockElapsedSeconds to DurationAccuracy.INFERRED_WALL_CLOCK
                    monotonicElapsed < 0L -> {
                        // Monotonic clock reset indicating device reboot during call
                        wallClockElapsedSeconds to DurationAccuracy.INFERRED_WALL_CLOCK
                    }
                    else -> ((monotonicElapsed) / 1000L).coerceAtLeast(0L) to DurationAccuracy.RELIABLE_MONOTONIC
                }
            } else {
                0L to (if (session.state == CallSessionState.RINGING) DurationAccuracy.RELIABLE_MONOTONIC else DurationAccuracy.UNKNOWN)
            }

            val finalDirection = if (session.wasOffHook) {
                session.direction
            } else {
                CallDirection.MISSED
            }

            val finalState = when {
                session.wasOffHook -> CallSessionState.ENDED
                else -> CallSessionState.MISSED
            }

            val record = CallRecord(
                id = "${session.startedWallClockMillis}-${session.contact.phoneNumber}",
                contactName = session.contact.name,
                phoneNumber = session.contact.phoneNumber,
                avatarUri = session.contact.avatarUri,
                startedAtMillis = session.startedWallClockMillis,
                durationSeconds = durationSeconds,
                direction = finalDirection,
                durationAccuracy = accuracy
            )

            currentSession = session.copy(
                state = finalState,
                endedRealtime = nowRealtime
            )
            completedRecord = record
            _lastCompletedRecord.value = record
        }

        clearPersistedSession(context)
        CallScreeningCoordinator.clear()

        if (wasConnected) {
            IslandStateManager.endCall()
        } else if (wasRinging) {
            IslandStateManager.declineIncomingCall()
        }

        completedRecord?.let { record ->
            if (context != null) {
                // Save to persistent call history
                runCatching {
                    CallHistoryRepository(context).add(record)
                }

                val shouldShowSummary = runCatching {
                    val preferences = AuroraPreferences(context)
                    val isPro = preferences.isProActive.first()
                    val userRequested = preferences.callSummaryEnabled.first()
                    PremiumFeaturePolicy.callSummaryEnabled(
                        isProActive = isPro,
                        requested = userRequested
                    )
                }.getOrDefault(true)

                if (shouldShowSummary) {
                    _activeCallSummary.value = record
                    IslandStateManager.postCallSummary(record)

                    // If overlay service is not currently running or active, dispatch high-priority notification
                    val isOverlayActive = IslandOverlayService.isServiceReallyConnected()
                    if (!isOverlayActive) {
                        runCatching {
                            CallSummaryNotifier.showCallSummaryNotification(context, record)
                        }
                    }
                }
            } else {
                _activeCallSummary.value = record
                IslandStateManager.postCallSummary(record)
            }
        }

        // Reset session state back to IDLE after processing completion
        synchronized(sessionLock) {
            if (currentSession?.state == CallSessionState.ENDED ||
                currentSession?.state == CallSessionState.MISSED ||
                currentSession?.state == CallSessionState.REJECTED
            ) {
                currentSession = null
            }
        }
    }

    private fun resolveContact(context: Context?, phoneNumber: String): ContactInfo {
        val fallback = ContactInfo(name = phoneNumber, phoneNumber = phoneNumber)
        if (context == null ||
            phoneNumber == "Unknown Caller" ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) return fallback

        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )
        return runCatching {
            context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                ContactInfo(
                    name = cursor.getString(0).orEmpty().ifBlank { phoneNumber },
                    phoneNumber = phoneNumber,
                    avatarUri = cursor.getString(1)
                )
            } ?: fallback
        }.getOrDefault(fallback)
    }
}
