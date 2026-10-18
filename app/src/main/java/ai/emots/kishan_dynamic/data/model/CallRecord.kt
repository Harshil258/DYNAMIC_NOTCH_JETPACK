package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class DurationAccuracy {
    RELIABLE_MONOTONIC,
    INFERRED_WALL_CLOCK,
    UNKNOWN
}

@Immutable
data class CallRecord(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val avatarUri: String? = null,
    val startedAtMillis: Long,
    val durationSeconds: Long,
    val direction: CallDirection,
    val durationAccuracy: DurationAccuracy = DurationAccuracy.RELIABLE_MONOTONIC
)
