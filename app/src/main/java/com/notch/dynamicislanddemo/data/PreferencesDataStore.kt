package com.notch.dynamicislanddemo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * DataStore manager for app preferences
 */
class PreferencesDataStore(private val context: Context) {
    
    // Keys
    companion object {
        private val ISLAND_ENABLED_KEY = booleanPreferencesKey("island_enabled")
        private val SETUP_DONE_KEY = booleanPreferencesKey("setup_done")
        
        // Display
        private val ALWAYS_ON_TOP_KEY = booleanPreferencesKey("always_on_top")
        private val SHOW_ON_LOCK_SCREEN_KEY = booleanPreferencesKey("show_on_lock_screen")
        private val ANIMATION_SPEED_KEY = booleanPreferencesKey("animation_speed") // true = normal, false = fast
        
        // Behavior
        private val AUTO_EXPAND_KEY = booleanPreferencesKey("auto_expand")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        private val COMPACT_MUSIC_CONTROLS_KEY = booleanPreferencesKey("compact_music_controls")
        
        // Notifications
        private val SHOW_BATTERY_NOTIFICATIONS_KEY = booleanPreferencesKey("show_battery_notifications")
        
        // Positioning
        private val VERTICAL_OFFSET_KEY = androidx.datastore.preferences.core.intPreferencesKey("vertical_offset")
        private val HORIZONTAL_OFFSET_KEY = androidx.datastore.preferences.core.intPreferencesKey("horizontal_offset")
    }
    
    // Flows
    val islandEnabled: Flow<Boolean> = context.dataStore.data.map { it[ISLAND_ENABLED_KEY] ?: true }
    val setupDone: Flow<Boolean> = context.dataStore.data.map { it[SETUP_DONE_KEY] ?: false }
    
    val isAlwaysOnTop: Flow<Boolean> = context.dataStore.data.map { it[ALWAYS_ON_TOP_KEY] ?: true }
    val showOnLockScreen: Flow<Boolean> = context.dataStore.data.map { it[SHOW_ON_LOCK_SCREEN_KEY] ?: true }
    val animationSpeed: Flow<Boolean> = context.dataStore.data.map { it[ANIMATION_SPEED_KEY] ?: true }
    
    val autoExpand: Flow<Boolean> = context.dataStore.data.map { it[AUTO_EXPAND_KEY] ?: true }
    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { it[HAPTIC_FEEDBACK_KEY] ?: true }
    val compactMusicControls: Flow<Boolean> = context.dataStore.data.map { it[COMPACT_MUSIC_CONTROLS_KEY] ?: false }
    
    val showBatteryNotifications: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BATTERY_NOTIFICATIONS_KEY] ?: true }
    
    val verticalOffset: Flow<Int> = context.dataStore.data.map { it[VERTICAL_OFFSET_KEY] ?: 12 }
    val horizontalOffset: Flow<Int> = context.dataStore.data.map { it[HORIZONTAL_OFFSET_KEY] ?: 0 }
    
    // Setters
    suspend fun setIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ISLAND_ENABLED_KEY] = enabled }
    }
    
    suspend fun setSetupDone(done: Boolean) {
        context.dataStore.edit { it[SETUP_DONE_KEY] = done }
    }
    
    suspend fun setAlwaysOnTop(enabled: Boolean) {
        context.dataStore.edit { it[ALWAYS_ON_TOP_KEY] = enabled }
    }
    
    suspend fun setShowOnLockScreen(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_ON_LOCK_SCREEN_KEY] = enabled }
    }
    
    suspend fun setAnimationSpeed(normal: Boolean) {
        context.dataStore.edit { it[ANIMATION_SPEED_KEY] = normal }
    }
    
    suspend fun setAutoExpand(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_EXPAND_KEY] = enabled }
    }
    
    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[HAPTIC_FEEDBACK_KEY] = enabled }
    }
    
    suspend fun setCompactMusicControls(enabled: Boolean) {
        context.dataStore.edit { it[COMPACT_MUSIC_CONTROLS_KEY] = enabled }
    }
    
    suspend fun setShowBatteryNotifications(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_BATTERY_NOTIFICATIONS_KEY] = enabled }
    }
    
    suspend fun setVerticalOffset(offset: Int) {
        context.dataStore.edit { it[VERTICAL_OFFSET_KEY] = offset }
    }
    
    suspend fun setHorizontalOffset(offset: Int) {
        context.dataStore.edit { it[HORIZONTAL_OFFSET_KEY] = offset }
    }
}

