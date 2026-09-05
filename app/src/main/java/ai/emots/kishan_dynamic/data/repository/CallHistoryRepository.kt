package ai.emots.kishan_dynamic.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.callHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "call_history_preferences"
)

/** Device-local call history owned by the app, independent of the reference app. */
class CallHistoryRepository(private val context: Context) {

    private companion object {
        val RECORDS_KEY = stringPreferencesKey("records")
        const val RECORD_SEPARATOR = "\u001F"
        const val FIELD_SEPARATOR = "\u001E"
        const val MAX_RECORDS = 50
    }

    val records: Flow<List<CallRecord>> = context.callHistoryDataStore.data.map { preferences ->
        decode(preferences[RECORDS_KEY])
    }

    suspend fun add(record: CallRecord) {
        context.callHistoryDataStore.edit { preferences ->
            val updated = (listOf(record) + decode(preferences[RECORDS_KEY]))
                .distinctBy { it.id }
                .take(MAX_RECORDS)
            preferences[RECORDS_KEY] = encode(updated)
        }
    }

    suspend fun clear() {
        context.callHistoryDataStore.edit { it.remove(RECORDS_KEY) }
    }

    private fun encode(records: List<CallRecord>): String = records.joinToString(RECORD_SEPARATOR) { record ->
        listOf(
            record.id,
            record.contactName,
            record.phoneNumber,
            record.avatarUri.orEmpty(),
            record.startedAtMillis.toString(),
            record.durationSeconds.toString(),
            record.direction.name
        ).joinToString(FIELD_SEPARATOR) { value ->
            Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    private fun decode(value: String?): List<CallRecord> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(RECORD_SEPARATOR).mapNotNull { encoded ->
            val fields = encoded.split(FIELD_SEPARATOR).mapNotNull { field ->
                runCatching {
                    String(Base64.decode(field, Base64.NO_WRAP), Charsets.UTF_8)
                }.getOrNull()
            }
            if (fields.size != 7) return@mapNotNull null
            CallRecord(
                id = fields[0],
                contactName = fields[1],
                phoneNumber = fields[2],
                avatarUri = fields[3].ifBlank { null },
                startedAtMillis = fields[4].toLongOrNull() ?: return@mapNotNull null,
                durationSeconds = fields[5].toLongOrNull() ?: return@mapNotNull null,
                direction = runCatching { CallDirection.valueOf(fields[6]) }.getOrNull()
                    ?: return@mapNotNull null
            )
        }
    }
}
