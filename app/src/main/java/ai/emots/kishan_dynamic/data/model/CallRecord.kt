package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

@Immutable
data class CallRecord(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val avatarUri: String? = null,
    val startedAtMillis: Long,
    val durationSeconds: Long,
    val direction: CallDirection
)
