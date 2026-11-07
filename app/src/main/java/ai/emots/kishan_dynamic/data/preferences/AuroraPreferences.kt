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
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Immutable snapshot of persisted Google Play billing state for proactive entitlement reconciliation.
 */
data class BillingEntitlementRecord(
    val isPaidPro: Boolean = false,
    val proExpiresAt: Long? = null,
    val proSource: String = "none",
    val planId: String? = null,
    val productId: String? = null,
    val isLifetime: Boolean = false,
    val lastVerifiedAt: Long? = null,
    val isAutoRenewing: Boolean = false,
    val reconciliationState: String = "verified"
)

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
        private val KEY_ISLAND_DISMISS_CANCELS_OS = booleanPreferencesKey("island_dismiss_cancels_os")
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
        private val KEY_PRO_PLAN_ID = stringPreferencesKey("pro_plan_id")
        private val KEY_PRO_PRODUCT_ID = stringPreferencesKey("pro_product_id")
        private val KEY_PRO_PURCHASE_TOKEN = stringPreferencesKey("pro_purchase_token")
        private val KEY_PRO_ORDER_ID = stringPreferencesKey("pro_order_id")
        private val KEY_PRO_LAST_VERIFIED_AT = longPreferencesKey("pro_last_verified_at")
        private val KEY_PRO_IS_LIFETIME = booleanPreferencesKey("pro_is_lifetime")
        private val KEY_PRO_AUTO_RENEWING = booleanPreferencesKey("pro_auto_renewing")
        private val KEY_PRO_RECONCILIATION_STATE = stringPreferencesKey("pro_reconciliation_state")
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

        // Trial, Rewarded & Feature Control Keys
        private val KEY_TRIAL_START_TIME = longPreferencesKey("trial_start_time")
        private val KEY_TRIAL_DURATION_DAYS = intPreferencesKey("trial_duration_days")
        private val KEY_TRIAL_EXPIRES_AT = longPreferencesKey("trial_expires_at")
        private val KEY_LAST_OBSERVED_TIMESTAMP = longPreferencesKey("last_observed_timestamp")
        private val KEY_REWARD_PREMIUM_EXPIRY = longPreferencesKey("reward_premium_expiry")
        private val KEY_LAST_AD_WATCH_TIMESTAMP = longPreferencesKey("last_ad_watch_timestamp")
        private val KEY_AD_REWARD_DAYS_JSON = stringPreferencesKey("ad_reward_days_json")
        private val KEY_QUICK_CONTROLS_ENABLED = booleanPreferencesKey("quick_controls_enabled")
        private val KEY_IDLE_AUTO_HIDE_ENABLED = booleanPreferencesKey("idle_auto_hide_enabled")
        private val KEY_IDLE_AUTO_HIDE_TIMEOUT_SECONDS = intPreferencesKey("idle_auto_hide_timeout_seconds")
        private val KEY_CAMERA_CUTOUT_MODE = booleanPreferencesKey("camera_cutout_mode")
        private val KEY_CUTOUT_SIZE_PX = intPreferencesKey("cutout_size_px")
        private val KEY_NOTCH_FLAT_TOP = booleanPreferencesKey("notch_flat_top")
        private val KEY_NOTCH_WIDTH_PX = intPreferencesKey("notch_width_px")
        private val KEY_NOTCH_HEIGHT_PX = intPreferencesKey("notch_height_px")
        private val KEY_NOTCH_BACKGROUND_ARGB = intPreferencesKey("notch_background_argb")
        private val KEY_NOTCH_FOREGROUND_ARGB = intPreferencesKey("notch_foreground_argb")
        private val KEY_BLOCKED_NOTIFICATION_PACKAGES = stringSetPreferencesKey("blocked_notification_packages")
        private val KEY_RECORDED_TRANSACTION_IDS = stringSetPreferencesKey("recorded_transaction_ids")
    }

    // Flows
    val recordedTransactionIds: Flow<Set<String>> = context.auroraDataStore.data.map {
        it[KEY_RECORDED_TRANSACTION_IDS] ?: emptySet()
    }
    val blockedNotificationPackages: Flow<Set<String>> = context.auroraDataStore.data.map {
        it[KEY_BLOCKED_NOTIFICATION_PACKAGES] ?: emptySet()
    }
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
    val islandDismissCancelsOs: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_ISLAND_DISMISS_CANCELS_OS] ?: false }
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
    val proPlanId: Flow<String?> = context.auroraDataStore.data.map { it[KEY_PRO_PLAN_ID] }
    val proProductId: Flow<String?> = context.auroraDataStore.data.map { it[KEY_PRO_PRODUCT_ID] }
    val proLastVerifiedAt: Flow<Long?> = context.auroraDataStore.data.map { it[KEY_PRO_LAST_VERIFIED_AT] }
    val proIsLifetime: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_PRO_IS_LIFETIME] ?: false }
    val proAutoRenewing: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_PRO_AUTO_RENEWING] ?: false }
    val proReconciliationState: Flow<String> = context.auroraDataStore.data.map { it[KEY_PRO_RECONCILIATION_STATE] ?: "verified" }

    val billingRecord: Flow<BillingEntitlementRecord> = context.auroraDataStore.data.map { preferences ->
        val enabled = preferences[KEY_PRO_ACTIVE] ?: false
        val isLifetime = preferences[KEY_PRO_IS_LIFETIME] ?: false
        val expiresAt = preferences[KEY_PRO_EXPIRES_AT]?.takeIf { it > 0L }
        val source = preferences[KEY_PRO_SOURCE] ?: "none"
        val planId = preferences[KEY_PRO_PLAN_ID]
        val productId = preferences[KEY_PRO_PRODUCT_ID]
        val lastVerifiedAt = preferences[KEY_PRO_LAST_VERIFIED_AT]
        val autoRenewing = preferences[KEY_PRO_AUTO_RENEWING] ?: false
        val reconciliationState = preferences[KEY_PRO_RECONCILIATION_STATE] ?: "verified"

        val now = System.currentTimeMillis()
        val isPaid = enabled && when {
            isLifetime -> true
            expiresAt != null -> expiresAt > now
            source.startsWith("play_billing") && (lastVerifiedAt ?: 0L) > 0L -> {
                // Bounded offline cache tolerance (7 days from last verified purchase for Google Play subscriptions)
                (now - lastVerifiedAt!!) <= 7 * 24 * 60 * 60 * 1000L
            }
            // Legacy migration: bounded 72-hour grace period while pending reconciliation
            source == "play_billing" || source == "legacy" -> {
                val legacyObserved = lastVerifiedAt ?: now
                (now - legacyObserved) <= 72 * 60 * 60 * 1000L
            }
            else -> false
        }

        BillingEntitlementRecord(
            isPaidPro = isPaid,
            proExpiresAt = expiresAt,
            proSource = source,
            planId = planId,
            productId = productId,
            isLifetime = isLifetime,
            lastVerifiedAt = lastVerifiedAt,
            isAutoRenewing = autoRenewing,
            reconciliationState = reconciliationState
        )
    }

    val isPaidPro: Flow<Boolean> = billingRecord.map { it.isPaidPro }
    val isAdFree: Flow<Boolean> = isPaidPro
    val isAdFreePaidUser: Flow<Boolean> = isAdFree
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

    // Trial, Rewarded & Feature Control Flows
    val trialStartTime: Flow<Long> = context.auroraDataStore.data.map { it[KEY_TRIAL_START_TIME] ?: 0L }
    val trialDurationDays: Flow<Int> = context.auroraDataStore.data.map { it[KEY_TRIAL_DURATION_DAYS] ?: 0 }
    val trialExpiresAt: Flow<Long> = context.auroraDataStore.data.map { it[KEY_TRIAL_EXPIRES_AT] ?: 0L }
    val lastObservedTimestamp: Flow<Long> = context.auroraDataStore.data.map { it[KEY_LAST_OBSERVED_TIMESTAMP] ?: 0L }
    val rewardPremiumExpiry: Flow<Long> = context.auroraDataStore.data.map { it[KEY_REWARD_PREMIUM_EXPIRY] ?: 0L }
    val isProActive: Flow<Boolean> = combine(
        billingRecord,
        trialExpiresAt,
        rewardPremiumExpiry
    ) { billing, trialExpires, rewardExpires ->
        val now = System.currentTimeMillis()
        billing.isPaidPro || trialExpires > now || rewardExpires > now
    }
    val lastAdWatchTimestamp: Flow<Long> = context.auroraDataStore.data.map { it[KEY_LAST_AD_WATCH_TIMESTAMP] ?: 0L }
    val adRewardDaysJson: Flow<String> = context.auroraDataStore.data.map { it[KEY_AD_REWARD_DAYS_JSON] ?: "{}" }
    val quickControlsEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_QUICK_CONTROLS_ENABLED] ?: true }
    val idleAutoHideEnabled: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_IDLE_AUTO_HIDE_ENABLED] ?: false }
    val idleAutoHideTimeoutSeconds: Flow<Int> = context.auroraDataStore.data.map { it[KEY_IDLE_AUTO_HIDE_TIMEOUT_SECONDS] ?: 10 }
    val cameraCutoutMode: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_CAMERA_CUTOUT_MODE] ?: false }
    val cutoutSizePx: Flow<Int> = context.auroraDataStore.data.map { it[KEY_CUTOUT_SIZE_PX] ?: 80 }
    val notchFlatTop: Flow<Boolean> = context.auroraDataStore.data.map { it[KEY_NOTCH_FLAT_TOP] ?: false }
    val notchWidthPx: Flow<Int> = context.auroraDataStore.data.map { it[KEY_NOTCH_WIDTH_PX] ?: -1 }
    val notchHeightPx: Flow<Int> = context.auroraDataStore.data.map { it[KEY_NOTCH_HEIGHT_PX] ?: -1 }
    val notchBackgroundArgb: Flow<Int> = context.auroraDataStore.data.map { it[KEY_NOTCH_BACKGROUND_ARGB] ?: 0xFF000000.toInt() }
    val notchForegroundArgb: Flow<Int> = context.auroraDataStore.data.map { it[KEY_NOTCH_FOREGROUND_ARGB] ?: 0xFFFFFFFF.toInt() }

    // Setters
    suspend fun setNotificationPackageBlocked(packageName: String, blocked: Boolean) {
        context.auroraDataStore.edit { preferences ->
            val current = preferences[KEY_BLOCKED_NOTIFICATION_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (blocked) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            preferences[KEY_BLOCKED_NOTIFICATION_PACKAGES] = current
        }
    }

    suspend fun clearAllBlockedNotificationPackages() {
        context.auroraDataStore.edit { preferences ->
            preferences.remove(KEY_BLOCKED_NOTIFICATION_PACKAGES)
        }
    }

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

    suspend fun setIslandDismissCancelsOs(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_ISLAND_DISMISS_CANCELS_OS] = enabled }
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

    /**
     * Persists verified Google Play billing entitlement with plan identity, verification timestamp,
     * and explicit lifetime/renewal flags.
     */
    suspend fun setVerifiedBillingEntitlement(
        planId: String,
        productId: String,
        isLifetime: Boolean,
        purchaseToken: String?,
        orderId: String?,
        isAutoRenewing: Boolean,
        expiresAt: Long? = null,
        lastVerifiedAt: Long = System.currentTimeMillis()
    ) {
        context.auroraDataStore.edit {
            it[KEY_PRO_ACTIVE] = true
            it[KEY_PRO_SOURCE] = if (isLifetime) "play_billing_lifetime" else "play_billing_$planId"
            it[KEY_PRO_PLAN_ID] = planId
            it[KEY_PRO_PRODUCT_ID] = productId
            it[KEY_PRO_IS_LIFETIME] = isLifetime
            it[KEY_PRO_AUTO_RENEWING] = isAutoRenewing
            it[KEY_PRO_LAST_VERIFIED_AT] = lastVerifiedAt
            it[KEY_PRO_RECONCILIATION_STATE] = "verified"
            if (purchaseToken != null) it[KEY_PRO_PURCHASE_TOKEN] = purchaseToken else it.remove(KEY_PRO_PURCHASE_TOKEN)
            if (orderId != null) it[KEY_PRO_ORDER_ID] = orderId else it.remove(KEY_PRO_ORDER_ID)
            if (expiresAt != null) it[KEY_PRO_EXPIRES_AT] = expiresAt else it.remove(KEY_PRO_EXPIRES_AT)
        }
    }

    /**
     * Authoritatively revokes paid Google Play Pro entitlement when Google Play confirms
     * no active subscription or lifetime purchase exists on this account.
     */
    suspend fun revokeProEntitlement() {
        context.auroraDataStore.edit {
            it[KEY_PRO_ACTIVE] = false
            it[KEY_PRO_SOURCE] = "none"
            it[KEY_PRO_IS_LIFETIME] = false
            it[KEY_PRO_AUTO_RENEWING] = false
            it[KEY_PRO_RECONCILIATION_STATE] = "revoked"
            it.remove(KEY_PRO_EXPIRES_AT)
            it.remove(KEY_PRO_PLAN_ID)
            it.remove(KEY_PRO_PRODUCT_ID)
            it.remove(KEY_PRO_PURCHASE_TOKEN)
            it.remove(KEY_PRO_ORDER_ID)
            it.remove(KEY_PRO_LAST_VERIFIED_AT)
        }
    }

    /**
     * Marks legacy ambiguous entitlement (source == "play_billing" without verified plan)
     * for verification against Google Play.
     */
    suspend fun markLegacyEntitlementPendingReconciliation(firstObserved: Long = System.currentTimeMillis()) {
        context.auroraDataStore.edit {
            it[KEY_PRO_RECONCILIATION_STATE] = "pending_reconciliation"
            if (it[KEY_PRO_LAST_VERIFIED_AT] == null) {
                it[KEY_PRO_LAST_VERIFIED_AT] = firstObserved
            }
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

    // Trial, Rewarded & Feature Control Setters
    suspend fun setTrial(startTime: Long, durationDays: Int, expiresAt: Long) {
        context.auroraDataStore.edit {
            it[KEY_TRIAL_START_TIME] = startTime
            it[KEY_TRIAL_DURATION_DAYS] = durationDays
            it[KEY_TRIAL_EXPIRES_AT] = expiresAt
            it[KEY_LAST_OBSERVED_TIMESTAMP] = startTime
        }
    }

    suspend fun updateObservedTimestamp(timestamp: Long) {
        context.auroraDataStore.edit {
            val current = it[KEY_LAST_OBSERVED_TIMESTAMP] ?: 0L
            if (timestamp > current) {
                it[KEY_LAST_OBSERVED_TIMESTAMP] = timestamp
            }
        }
    }

    suspend fun setRewardPremium(expiry: Long, lastWatchTime: Long, daysJson: String) {
        context.auroraDataStore.edit {
            it[KEY_REWARD_PREMIUM_EXPIRY] = expiry
            it[KEY_LAST_AD_WATCH_TIMESTAMP] = lastWatchTime
            it[KEY_AD_REWARD_DAYS_JSON] = daysJson
        }
    }

    suspend fun setLastAdWatchTimestamp(timestamp: Long) {
        context.auroraDataStore.edit { it[KEY_LAST_AD_WATCH_TIMESTAMP] = timestamp }
    }

    suspend fun setQuickControlsEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_QUICK_CONTROLS_ENABLED] = enabled }
    }

    suspend fun setIdleAutoHideEnabled(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_IDLE_AUTO_HIDE_ENABLED] = enabled }
    }

    suspend fun setIdleAutoHideTimeoutSeconds(seconds: Int) {
        context.auroraDataStore.edit { it[KEY_IDLE_AUTO_HIDE_TIMEOUT_SECONDS] = seconds }
    }

    suspend fun setCameraCutoutMode(enabled: Boolean) {
        context.auroraDataStore.edit { it[KEY_CAMERA_CUTOUT_MODE] = enabled }
    }

    suspend fun setCutoutSizePx(sizePx: Int) {
        context.auroraDataStore.edit { it[KEY_CUTOUT_SIZE_PX] = sizePx }
    }

    suspend fun setNotchFlatTop(flatTop: Boolean) {
        context.auroraDataStore.edit { it[KEY_NOTCH_FLAT_TOP] = flatTop }
    }

    suspend fun setNotchWidthPx(widthPx: Int) {
        context.auroraDataStore.edit { it[KEY_NOTCH_WIDTH_PX] = widthPx }
    }

    suspend fun setNotchHeightPx(heightPx: Int) {
        context.auroraDataStore.edit { it[KEY_NOTCH_HEIGHT_PX] = heightPx }
    }

    suspend fun setNotchBackgroundArgb(argb: Int) {
        context.auroraDataStore.edit { it[KEY_NOTCH_BACKGROUND_ARGB] = argb }
    }

    suspend fun setNotchForegroundArgb(argb: Int) {
        context.auroraDataStore.edit { it[KEY_NOTCH_FOREGROUND_ARGB] = argb }
    }

    suspend fun recordTransactionId(transactionId: String) {
        context.auroraDataStore.edit { preferences ->
            val current = preferences[KEY_RECORDED_TRANSACTION_IDS]?.toMutableSet() ?: mutableSetOf()
            current.add(transactionId)
            preferences[KEY_RECORDED_TRANSACTION_IDS] = current
        }
    }

    suspend fun resetNotchDisplayToDefaults() {
        context.auroraDataStore.edit {
            it[KEY_VERTICAL_OFFSET] = DEFAULT_VERTICAL_OFFSET_DP
            it[KEY_HORIZONTAL_OFFSET] = DEFAULT_HORIZONTAL_OFFSET_DP
            it[KEY_WIDTH_SCALE] = DEFAULT_WIDTH_SCALE
            it[KEY_NOTCH_FLAT_TOP] = false
            it.remove(KEY_NOTCH_WIDTH_PX)
            it.remove(KEY_NOTCH_HEIGHT_PX)
            it[KEY_NOTCH_BACKGROUND_ARGB] = 0xFF000000.toInt()
            it[KEY_NOTCH_FOREGROUND_ARGB] = 0xFFFFFFFF.toInt()
            it[KEY_IDLE_AUTO_HIDE_ENABLED] = false
            it[KEY_IDLE_AUTO_HIDE_TIMEOUT_SECONDS] = 10
            it[KEY_CAMERA_CUTOUT_MODE] = false
            it[KEY_CUTOUT_SIZE_PX] = 80
        }
    }
}
