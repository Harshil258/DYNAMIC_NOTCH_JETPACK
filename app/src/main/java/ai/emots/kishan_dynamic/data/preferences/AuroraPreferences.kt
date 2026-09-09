package ai.emots.kishan_dynamic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        const val DEFAULT_VERTICAL_OFFSET_DP = 11
        const val DEFAULT_HORIZONTAL_OFFSET_DP = 0
        const val DEFAULT_WIDTH_SCALE = 1.0f

        private val KEY_ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        private val KEY_SETUP_DONE = booleanPreferencesKey("setup_done")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_LANGUAGE_SELECTION_COMPLETED = booleanPreferencesKey("language_selection_completed")
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
        private val KEY_PRO_EXPIRES_AT = longPreferencesKey("pro_expires_at")
        private val KEY_PRO_SOURCE = stringPreferencesKey("pro_source")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_ALWAYS_ON_TOP = booleanPreferencesKey("always_on_top")
        private val KEY_SHOW_ON_LOCK_SCREEN = booleanPreferencesKey("show_on_lock_screen")
        private val KEY_HIDE_SENSITIVE_CONTENT = booleanPreferencesKey("hide_sensitive_content")
        private val KEY_ANIMATION_SPEED_NORMAL = booleanPreferencesKey("animation_speed_normal")
        private val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        private val KEY_COMPACT_MUSIC_CONTROLS = booleanPreferencesKey("compact_music_controls")
        private val KEY_SHOW_BATTERY_NOTIFICATIONS = booleanPreferencesKey("show_battery_notifications")
        private val KEY_CALL_BANNER = booleanPreferencesKey("call_banner")
        private val KEY_CALL_TIMER = booleanPreferencesKey("call_timer")
        private val KEY_CALL_SUMMARY = booleanPreferencesKey("call_summary")
        private val KEY_MUSIC_ISLAND = booleanPreferencesKey("music_island")
        private val KEY_MUSIC_SCRUBBER = booleanPreferencesKey("music_scrubber")
        private val KEY_CHARGING_ANIMATION = booleanPreferencesKey("charging_animation")
        private val KEY_LOW_BATTERY_ALERT = booleanPreferencesKey("low_battery_alert")
        private val KEY_MUTE_INDICATOR = booleanPreferencesKey("mute_indicator")
        private val KEY_VIBRATE_INDICATOR = booleanPreferencesKey("vibrate_indicator")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    // Flows
    val islandEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ISLAND_ENABLED] ?: true }
    val setupDone: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_SETUP_DONE] ?: false }
    val onboardingCompleted: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }
    val languageSelectionCompleted: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_LANGUAGE_SELECTION_COMPLETED] ?: false
    }
    val verticalOffset: Flow<Int> = context.auroraDataStore.data.map { it[KEY_VERTICAL_OFFSET] ?: DEFAULT_VERTICAL_OFFSET_DP }
    val horizontalOffset: Flow<Int> = context.auroraDataStore.data.map { it[KEY_HORIZONTAL_OFFSET] ?: DEFAULT_HORIZONTAL_OFFSET_DP }
    val widthScale: Flow<Float> = context.auroraDataStore.data.map { it[KEY_WIDTH_SCALE] ?: DEFAULT_WIDTH_SCALE }
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
    val proExpiresAt: Flow<Long?> = context.auroraDataStore.data.map { preferences ->
        preferences[KEY_PRO_EXPIRES_AT]?.takeIf { it > 0L }
    }
    val proSource: Flow<String> = context.auroraDataStore.data.map { it[KEY_PRO_SOURCE] ?: "none" }
    val isProActive: Flow<Boolean> = context.auroraDataStore.data.map { preferences ->
        val enabled = preferences[KEY_PRO_ACTIVE] ?: false
        val expiresAt = preferences[KEY_PRO_EXPIRES_AT] ?: 0L
        enabled && (expiresAt == 0L || expiresAt > System.currentTimeMillis())
    }
    val themeMode: Flow<String> = context.auroraDataStore.data.map { it[KEY_THEME_MODE] ?: "system" }
    val alwaysOnTop: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ALWAYS_ON_TOP] ?: true }
    val showOnLockScreen: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_SHOW_ON_LOCK_SCREEN] ?: true }
    val hideSensitiveContent: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_HIDE_SENSITIVE_CONTENT] ?: true
    }
    val animationSpeedNormal: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_ANIMATION_SPEED_NORMAL] ?: true
    }
    val reduceMotion: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_REDUCE_MOTION] ?: false
    }
    val compactMusicControls: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_COMPACT_MUSIC_CONTROLS] ?: false
    }
    val showBatteryNotifications: Flow<Boolean> = context.auroraDataStore.data.map {
        it[KEY_SHOW_BATTERY_NOTIFICATIONS] ?: true
    }
    val callBannerEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_CALL_BANNER] ?: true }
    val callTimerEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_CALL_TIMER] ?: true }
    val callSummaryEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_CALL_SUMMARY] ?: true }
    val musicIslandEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_MUSIC_ISLAND] ?: true }
    val musicScrubberEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_MUSIC_SCRUBBER] ?: true }
    val chargingAnimationEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_CHARGING_ANIMATION] ?: true }
    val lowBatteryAlertEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_LOW_BATTERY_ALERT] ?: true }
    val muteIndicatorEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_MUTE_INDICATOR] ?: true }
    val vibrateIndicatorEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_VIBRATE_INDICATOR] ?: true }
    val hapticFeedbackEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_HAPTIC_FEEDBACK] ?: true }

    // Reference-compatible names for the sound settings surface.
    val showRingerModeIndicator: Flow<Boolean> = ringerMode
    val showVolumeIndicator: Flow<Boolean> = volumeHud

    // Setters
    suspend fun setIslandEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_ISLAND_ENABLED] = enabled }
    }

    /** Records that the required permission hub was completed at least once. */
    suspend fun setSetupDone(done: Boolean) {
        context.auroraDataStore.edit { it[KEY_SETUP_DONE] = done }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.auroraDataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setLanguageSelectionCompleted(completed: Boolean) {
        context.auroraDataStore.edit { it[KEY_LANGUAGE_SELECTION_COMPLETED] = completed }
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
        context.auroraDataStore.edit {
            it[KEY_PRO_ACTIVE] = active
            if (!active) {
                it.remove(KEY_PRO_EXPIRES_AT)
                it[KEY_PRO_SOURCE] = "none"
            }
        }
    }

    /** Persists entitlement only after a trusted provider reports a purchase. */
    suspend fun setProEntitlement(source: String, expiresAt: Long? = null) {
        context.auroraDataStore.edit {
            it[KEY_PRO_ACTIVE] = true
            it[KEY_PRO_SOURCE] = source
            if (expiresAt == null) it.remove(KEY_PRO_EXPIRES_AT) else it[KEY_PRO_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.auroraDataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setAlwaysOnTop(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_ALWAYS_ON_TOP] = enabled }
    }

    suspend fun setShowOnLockScreen(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_SHOW_ON_LOCK_SCREEN] = enabled }
    }

    suspend fun setHideSensitiveContent(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_HIDE_SENSITIVE_CONTENT] = enabled }
    }

    suspend fun setAnimationSpeedNormal(normal: Boolean) {
        context.auroraDataStore.edit { it[KEY_ANIMATION_SPEED_NORMAL] = normal }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_REDUCE_MOTION] = enabled }
    }

    suspend fun setCompactMusicControls(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_COMPACT_MUSIC_CONTROLS] = enabled }
    }

    suspend fun setShowBatteryNotifications(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_SHOW_BATTERY_NOTIFICATIONS] = enabled }
    }

    suspend fun setCallBannerEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_CALL_BANNER] = enabled }
    }

    suspend fun setCallTimerEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_CALL_TIMER] = enabled }
    }

    suspend fun setCallSummaryEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_CALL_SUMMARY] = enabled }
    }

    suspend fun setMusicIslandEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_MUSIC_ISLAND] = enabled }
    }

    suspend fun setMusicScrubberEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_MUSIC_SCRUBBER] = enabled }
    }

    suspend fun setChargingAnimationEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_CHARGING_ANIMATION] = enabled }
    }

    suspend fun setLowBatteryAlertEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_LOW_BATTERY_ALERT] = enabled }
    }

    suspend fun setMuteIndicatorEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_MUTE_INDICATOR] = enabled }
    }

    suspend fun setVibrateIndicatorEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_VIBRATE_INDICATOR] = enabled }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setShowRingerModeIndicator(enabled: Boolean) = setRingerMode(enabled)

    suspend fun setShowVolumeIndicator(enabled: Boolean) = setVolumeHud(enabled)
}
