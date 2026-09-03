package ai.emots.kishan_dynamic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.auroraDataStore: DataStore<Preferences> by preferencesDataStore(name = "aurora_island_preferences")

/**
 * Reactive DataStore Preferences repository for Aurora Island settings.
 */
class AuroraPreferences(private val context: Context) {

    companion object {
        private val KEY_ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_VERTICAL_OFFSET = intPreferencesKey("vertical_offset")
        private val KEY_HORIZONTAL_OFFSET = intPreferencesKey("horizontal_offset")
        private val KEY_WIDTH_SCALE = floatPreferencesKey("width_scale")
        private val KEY_AUTO_EXPAND = booleanPreferencesKey("auto_expand")
        private val KEY_DISPLAY_DURATION = intPreferencesKey("display_duration")
        private val KEY_SWIPE_UP_DISMISS = booleanPreferencesKey("swipe_up_dismiss")
        private val KEY_VOLUME_HUD = booleanPreferencesKey("volume_hud")
        private val KEY_RINGER_MODE = booleanPreferencesKey("ringer_mode")
        private val KEY_PULSE_SCALE = floatPreferencesKey("pulse_scale")
        private val KEY_PULSE_DURATION = intPreferencesKey("pulse_duration")
        private val KEY_WAVEFORM_STYLE = stringPreferencesKey("waveform_style")
        private val KEY_ACTIVE_THEME = stringPreferencesKey("active_theme")
        private val KEY_LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val KEY_PRO_ACTIVE = booleanPreferencesKey("pro_active")
    }

    // Flows
    val islandEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ISLAND_ENABLED] ?: true }
    val onboardingCompleted: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }
    val verticalOffset: Flow<Int> = context.auroraDataStore.data.map { it[KEY_VERTICAL_OFFSET] ?: 12 }
    val horizontalOffset: Flow<Int> = context.auroraDataStore.data.map { it[KEY_HORIZONTAL_OFFSET] ?: 0 }
    val widthScale: Flow<Float> = context.auroraDataStore.data.map { it[KEY_WIDTH_SCALE] ?: 1.0f }
    val autoExpand: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_AUTO_EXPAND] ?: true }
    val displayDuration: Flow<Int> = context.auroraDataStore.data.map { it[KEY_DISPLAY_DURATION] ?: 5 }
    val swipeUpDismiss: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_SWIPE_UP_DISMISS] ?: true }
    val volumeHud: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_VOLUME_HUD] ?: true }
    val ringerMode: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_RINGER_MODE] ?: true }
    val pulseScale: Flow<Float> = context.auroraDataStore.data.map { it[KEY_PULSE_SCALE] ?: 1.15f }
    val pulseDuration: Flow<Int> = context.auroraDataStore.data.map { it[KEY_PULSE_DURATION] ?: 450 }
    val waveformStyle: Flow<String> = context.auroraDataStore.data.map { it[KEY_WAVEFORM_STYLE] ?: "Cyan Neon" }
    val activeTheme: Flow<String> = context.auroraDataStore.data.map { it[KEY_ACTIVE_THEME] ?: "Violet Horizon" }
    val languageCode: Flow<String> = context.auroraDataStore.data.map { it[KEY_LANGUAGE_CODE] ?: "en" }
    val isProActive: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_PRO_ACTIVE] ?: false }

    // Setters
    suspend fun setIslandEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_ISLAND_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.auroraDataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setVerticalOffset(offset: Int) {
        context.auroraDataStore.edit { it[KEY_VERTICAL_OFFSET] = offset }
    }

    suspend fun setHorizontalOffset(offset: Int) {
        context.auroraDataStore.edit { it[KEY_HORIZONTAL_OFFSET] = offset }
    }

    suspend fun setWidthScale(scale: Float) {
        context.auroraDataStore.edit { it[KEY_WIDTH_SCALE] = scale }
    }

    suspend fun setAutoExpand(autoExpand: Boolean) {
        context.auroraDataStore.edit { it[KEY_AUTO_EXPAND] = autoExpand }
    }

    suspend fun setDisplayDuration(duration: Int) {
        context.auroraDataStore.edit { it[KEY_DISPLAY_DURATION] = duration }
    }

    suspend fun setSwipeUpDismiss(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_SWIPE_UP_DISMISS] = enabled }
    }

    suspend fun setVolumeHud(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_VOLUME_HUD] = enabled }
    }

    suspend fun setRingerMode(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_RINGER_MODE] = enabled }
    }

    suspend fun setPulseScale(scale: Float) {
        context.auroraDataStore.edit { it[KEY_PULSE_SCALE] = scale }
    }

    suspend fun setPulseDuration(durationMs: Int) {
        context.auroraDataStore.edit { it[KEY_PULSE_DURATION] = durationMs }
    }

    suspend fun setWaveformStyle(style: String) {
        context.auroraDataStore.edit { it[KEY_WAVEFORM_STYLE] = style }
    }

    suspend fun setActiveTheme(theme: String) {
        context.auroraDataStore.edit { it[KEY_ACTIVE_THEME] = theme }
    }

    suspend fun setLanguageCode(code: String) {
        context.auroraDataStore.edit { it[KEY_LANGUAGE_CODE] = code }
    }

    suspend fun setProActive(active: Boolean) {
        context.auroraDataStore.edit { it[KEY_PRO_ACTIVE] = active }
    }
}
