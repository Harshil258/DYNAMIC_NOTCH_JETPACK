package ai.emots.kishan_dynamic.data.release

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Centralized Firebase Analytics Event Manager for Dynamic Island.
 * Tracks user engagement, island interactions, feature gates, monetization funnels,
 * and system events with robust null-safety and no-op fallback in offline or non-Google environments.
 */
object FirebaseEventManager {

    private const val TAG = "FirebaseEvents"
    private var analytics: FirebaseAnalytics? = null

    /**
     * Initialize the event manager with application context.
     */
    fun initialize(context: Context) {
        if (analytics == null) {
            runCatching {
                analytics = FirebaseAnalytics.getInstance(context.applicationContext)
                Log.d(TAG, "FirebaseEventManager initialized successfully")
            }.onFailure { error ->
                Log.w(TAG, "FirebaseEventManager initialization skipped or failed: ${error.message}")
            }
        }
    }

    private fun logEvent(eventName: String, params: Bundle? = null) {
        runCatching {
            analytics?.logEvent(eventName, params)
            Log.d(TAG, "Event logged: $eventName ${params?.let { "with params: $it" } ?: ""}")
        }.onFailure { error ->
            Log.w(TAG, "Failed to log event $eventName: ${error.message}")
        }
    }

    // ==================== SCREEN EVENTS ====================

    object Screens {
        const val SPLASH = "screen_splash"
        const val HOME = "screen_home"
        const val SETTINGS = "screen_settings"
        const val PREVIEW = "screen_preview"
        const val LANGUAGE = "screen_language"
        const val ONBOARDING = "screen_onboarding"
        const val PERMISSION = "screen_permission"
        const val DISPLAY_SETTINGS = "screen_display_settings"
        const val THEME_SETTINGS = "screen_theme_settings"
        const val CALL_SETTINGS = "screen_call_settings"
        const val MUSIC_SETTINGS = "screen_music_settings"
        const val NOTIFICATION_SETTINGS = "screen_notification_settings"
        const val BATTERY_SETTINGS = "screen_battery_settings"
        const val QUICK_CONTROL = "screen_quick_control"
        const val ABOUT = "screen_about"
        const val CALL_HISTORY = "screen_call_history"
        const val CALL_SUMMARY = "screen_call_summary"
        const val PREMIUM = "screen_premium"
        const val SOUND_SETTINGS = "screen_sound_settings"
        const val FEEDBACK = "screen_feedback"
    }

    fun trackScreenView(screenName: String) {
        logEvent(screenName)
    }

    // ==================== ISLAND EVENTS ====================

    object IslandEvents {
        const val TAP = "island_tap"
        const val EXPAND = "island_expand"
        const val COLLAPSE = "island_collapse"
        const val SWIPE_DISMISS = "island_swipe_dismiss"
        const val LONG_PRESS = "island_long_press"
        const val STATE_CHANGED = "island_state_changed"
    }

    fun trackIslandTap(state: String) {
        logEvent(IslandEvents.TAP, Bundle().apply {
            putString("island_state", state)
        })
    }

    fun trackIslandExpand(state: String) {
        logEvent(IslandEvents.EXPAND, Bundle().apply {
            putString("island_state", state)
        })
    }

    fun trackIslandCollapse(state: String) {
        logEvent(IslandEvents.COLLAPSE, Bundle().apply {
            putString("island_state", state)
        })
    }

    fun trackIslandSwipeDismiss(state: String) {
        logEvent(IslandEvents.SWIPE_DISMISS, Bundle().apply {
            putString("island_state", state)
        })
    }

    fun trackIslandLongPress(state: String) {
        logEvent(IslandEvents.LONG_PRESS, Bundle().apply {
            putString("island_state", state)
        })
    }

    fun trackIslandStateChanged(fromState: String, toState: String) {
        logEvent(IslandEvents.STATE_CHANGED, Bundle().apply {
            putString("from_state", fromState)
            putString("to_state", toState)
        })
    }

    // ==================== FEATURE TOGGLE EVENTS ====================

    object FeatureEvents {
        const val ISLAND_TOGGLED = "feature_island_toggled"
        const val CALL_TOGGLED = "feature_call_toggled"
        const val MUSIC_TOGGLED = "feature_music_toggled"
        const val NOTIFICATION_TOGGLED = "feature_notification_toggled"
        const val BATTERY_TOGGLED = "feature_battery_toggled"
        const val QUICK_CONTROL_TOGGLED = "feature_quick_control_toggled"
    }

    fun trackFeatureToggle(featureName: String, enabled: Boolean) {
        logEvent("feature_${featureName}_toggled", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    fun trackIslandEnabled(enabled: Boolean) {
        logEvent(FeatureEvents.ISLAND_TOGGLED, Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    fun trackCallFeatureToggle(enabled: Boolean) {
        logEvent(FeatureEvents.CALL_TOGGLED, Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    fun trackMusicFeatureToggle(enabled: Boolean) {
        logEvent(FeatureEvents.MUSIC_TOGGLED, Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    fun trackNotificationFeatureToggle(enabled: Boolean) {
        logEvent(FeatureEvents.NOTIFICATION_TOGGLED, Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    fun trackBatteryFeatureToggle(enabled: Boolean) {
        logEvent(FeatureEvents.BATTERY_TOGGLED, Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    // ==================== CALL EVENTS ====================

    object CallEvents {
        const val INCOMING_SHOWN = "call_incoming_shown"
        const val ANSWERED = "call_answered"
        const val REJECTED = "call_rejected"
        const val ENDED = "call_ended"
        const val ONGOING_SHOWN = "call_ongoing_shown"
        const val SUMMARY_SHOWN = "call_summary_shown"
        const val SUMMARY_ACTION = "call_summary_action"
        const val SUMMARY_DISMISSED = "call_summary_dismissed"
    }

    fun trackIncomingCallShown(contactName: String? = null) {
        logEvent(CallEvents.INCOMING_SHOWN, Bundle().apply {
            putBoolean("has_contact", contactName != null)
        })
    }

    fun trackCallAnswered() {
        logEvent(CallEvents.ANSWERED)
    }

    fun trackCallRejected() {
        logEvent(CallEvents.REJECTED)
    }

    fun trackCallEnded(durationSeconds: Long) {
        logEvent(CallEvents.ENDED, Bundle().apply {
            putLong("duration_seconds", durationSeconds)
        })
    }

    fun trackOngoingCallShown() {
        logEvent(CallEvents.ONGOING_SHOWN)
    }

    fun trackCallSummaryShown() {
        logEvent(CallEvents.SUMMARY_SHOWN)
    }

    fun trackCallSummaryAction(action: String) {
        logEvent(CallEvents.SUMMARY_ACTION, Bundle().apply {
            putString("action", action)
        })
    }

    fun trackCallSummaryDismissed() {
        logEvent(CallEvents.SUMMARY_DISMISSED)
    }

    // ==================== MUSIC EVENTS ====================

    object MusicEvents {
        const val ISLAND_SHOWN = "music_island_shown"
        const val PLAY_PAUSE = "music_play_pause"
        const val NEXT = "music_next"
        const val PREVIOUS = "music_previous"
        const val EXPAND = "music_expand"
        const val COLLAPSE = "music_collapse"
        const val APP_OPENED = "music_app_opened"
    }

    fun trackMusicIslandShown(appName: String? = null) {
        logEvent(MusicEvents.ISLAND_SHOWN, Bundle().apply {
            appName?.let { putString("app_name", it) }
        })
    }

    fun trackMusicPlayPause(isPlaying: Boolean) {
        logEvent(MusicEvents.PLAY_PAUSE, Bundle().apply {
            putBoolean("is_playing", isPlaying)
        })
    }

    fun trackMusicNext() {
        logEvent(MusicEvents.NEXT)
    }

    fun trackMusicPrevious() {
        logEvent(MusicEvents.PREVIOUS)
    }

    fun trackMusicExpand() {
        logEvent(MusicEvents.EXPAND)
    }

    fun trackMusicAppOpened(appName: String) {
        logEvent(MusicEvents.APP_OPENED, Bundle().apply {
            putString("app_name", appName)
        })
    }

    // ==================== NOTIFICATION EVENTS ====================

    object NotificationEvents {
        const val SHOWN = "notification_shown"
        const val DISMISSED = "notification_dismissed"
        const val TAP = "notification_tap"
        const val ACTION_TAP = "notification_action_tap"
        const val STACKED_SHOWN = "notification_stacked_shown"
    }

    fun trackNotificationShown(appName: String, hasImage: Boolean = false) {
        logEvent(NotificationEvents.SHOWN, Bundle().apply {
            putString("app_name", appName)
            putBoolean("has_image", hasImage)
        })
    }

    fun trackNotificationDismissed(appName: String) {
        logEvent(NotificationEvents.DISMISSED, Bundle().apply {
            putString("app_name", appName)
        })
    }

    fun trackNotificationTap(appName: String) {
        logEvent(NotificationEvents.TAP, Bundle().apply {
            putString("app_name", appName)
        })
    }

    fun trackNotificationActionTap(appName: String, actionLabel: String) {
        logEvent(NotificationEvents.ACTION_TAP, Bundle().apply {
            putString("app_name", appName)
            putString("action_label", actionLabel)
        })
    }

    fun trackStackedNotificationsShown(count: Int) {
        logEvent(NotificationEvents.STACKED_SHOWN, Bundle().apply {
            putInt("count", count)
        })
    }

    // ==================== QUICK CONTROL EVENTS ====================

    object QuickControlEvents {
        const val TILE_TAP = "quick_control_tile_tap"
        const val CONFIGURED = "quick_control_configured"
        const val BRIGHTNESS_CHANGED = "quick_control_brightness"
    }

    fun trackQuickControlTileTap(tileName: String, newState: Boolean) {
        logEvent(QuickControlEvents.TILE_TAP, Bundle().apply {
            putString("tile_name", tileName)
            putBoolean("new_state", newState)
        })
    }

    fun trackQuickControlConfigured(tileCount: Int) {
        logEvent(QuickControlEvents.CONFIGURED, Bundle().apply {
            putInt("tile_count", tileCount)
        })
    }

    fun trackBrightnessChanged(level: Float) {
        logEvent(QuickControlEvents.BRIGHTNESS_CHANGED, Bundle().apply {
            putFloat("level", level)
        })
    }

    // ==================== SETTINGS EVENTS ====================

    object SettingsEvents {
        const val CHANGED = "setting_changed"
        const val THEME_CHANGED = "theme_changed"
        const val LANGUAGE_CHANGED = "language_changed"
        const val ISLAND_POSITION_CHANGED = "island_position_changed"
        const val ISLAND_SIZE_CHANGED = "island_size_changed"
    }

    fun trackSettingChanged(settingName: String, value: String) {
        logEvent(SettingsEvents.CHANGED, Bundle().apply {
            putString("setting_name", settingName)
            putString("value", value)
        })
    }

    fun trackThemeChanged(theme: String) {
        logEvent(SettingsEvents.THEME_CHANGED, Bundle().apply {
            putString("theme", theme)
        })
    }

    fun trackLanguageChanged(language: String) {
        logEvent(SettingsEvents.LANGUAGE_CHANGED, Bundle().apply {
            putString("language", language)
        })
    }

    fun trackIslandPositionChanged(position: String) {
        logEvent(SettingsEvents.ISLAND_POSITION_CHANGED, Bundle().apply {
            putString("position", position)
        })
    }

    fun trackIslandSizeChanged(size: Float) {
        logEvent(SettingsEvents.ISLAND_SIZE_CHANGED, Bundle().apply {
            putFloat("size", size)
        })
    }

    // ==================== PERMISSION EVENTS ====================

    object PermissionEvents {
        const val ACCESSIBILITY_GRANTED = "permission_accessibility_granted"
        const val ACCESSIBILITY_DENIED = "permission_accessibility_denied"
        const val NOTIFICATION_GRANTED = "permission_notification_granted"
        const val NOTIFICATION_DENIED = "permission_notification_denied"
        const val PERMISSION_SCREEN_SHOWN = "permission_screen_shown"
    }

    fun trackAccessibilityPermission(granted: Boolean) {
        logEvent(
            if (granted) PermissionEvents.ACCESSIBILITY_GRANTED
            else PermissionEvents.ACCESSIBILITY_DENIED
        )
    }

    fun trackNotificationPermission(granted: Boolean) {
        logEvent(
            if (granted) PermissionEvents.NOTIFICATION_GRANTED
            else PermissionEvents.NOTIFICATION_DENIED
        )
    }

    fun trackPermissionScreenShown() {
        logEvent(PermissionEvents.PERMISSION_SCREEN_SHOWN)
    }

    // ==================== ONBOARDING EVENTS ====================

    object OnboardingEvents {
        const val STARTED = "onboarding_started"
        const val PAGE_VIEWED = "onboarding_page_viewed"
        const val COMPLETED = "onboarding_completed"
        const val SKIPPED = "onboarding_skipped"
    }

    fun trackOnboardingStarted() {
        logEvent(OnboardingEvents.STARTED)
    }

    fun trackOnboardingPageViewed(pageIndex: Int, pageName: String) {
        logEvent(OnboardingEvents.PAGE_VIEWED, Bundle().apply {
            putInt("page_index", pageIndex)
            putString("page_name", pageName)
        })
    }

    fun trackOnboardingCompleted() {
        logEvent(OnboardingEvents.COMPLETED)
    }

    fun trackOnboardingSkipped(atPage: Int) {
        logEvent(OnboardingEvents.SKIPPED, Bundle().apply {
            putInt("skipped_at_page", atPage)
        })
    }

    // ==================== CHARGING/BATTERY EVENTS ====================

    object BatteryEvents {
        const val CHARGING_SHOWN = "battery_charging_shown"
        const val CHARGING_DISMISSED = "battery_charging_dismissed"
    }

    fun trackChargingIslandShown(batteryLevel: Int) {
        logEvent(BatteryEvents.CHARGING_SHOWN, Bundle().apply {
            putInt("battery_level", batteryLevel)
        })
    }

    fun trackChargingIslandDismissed() {
        logEvent(BatteryEvents.CHARGING_DISMISSED)
    }

    // ==================== APP EVENTS ====================

    object AppEvents {
        const val APP_OPENED = "app_opened"
        const val APP_BACKGROUNDED = "app_backgrounded"
        const val SERVICE_STARTED = "service_started"
        const val SERVICE_STOPPED = "service_stopped"
        const val NO_INTERNET_SHOWN = "no_internet_shown"
        const val NO_INTERNET_DISMISSED = "no_internet_dismissed"
    }

    fun trackAppOpened() {
        logEvent(AppEvents.APP_OPENED)
    }

    fun trackServiceStarted() {
        logEvent(AppEvents.SERVICE_STARTED)
    }

    fun trackServiceStopped() {
        logEvent(AppEvents.SERVICE_STOPPED)
    }

    fun trackNoInternetShown() {
        logEvent(AppEvents.NO_INTERNET_SHOWN)
    }

    fun trackNoInternetDismissed() {
        logEvent(AppEvents.NO_INTERNET_DISMISSED)
    }

    // ==================== AD EVENTS ====================

    object AdEvents {
        const val INTERSTITIAL_SHOWN = "ad_interstitial_shown"
        const val INTERSTITIAL_CLICKED = "ad_interstitial_clicked"
        const val NATIVE_SHOWN = "ad_native_shown"
        const val NATIVE_CLICKED = "ad_native_clicked"
        const val BANNER_SHOWN = "ad_banner_shown"
        const val REWARDED_SHOWN = "ad_rewarded_shown"
        const val REWARDED_EARNED = "ad_rewarded_earned"
    }

    fun trackInterstitialShown(placement: String) {
        logEvent(AdEvents.INTERSTITIAL_SHOWN, Bundle().apply {
            putString("placement", placement)
        })
    }

    fun trackNativeAdShown(placement: String) {
        logEvent(AdEvents.NATIVE_SHOWN, Bundle().apply {
            putString("placement", placement)
        })
    }

    fun trackBannerShown(placement: String) {
        logEvent(AdEvents.BANNER_SHOWN, Bundle().apply {
            putString("placement", placement)
        })
    }

    fun trackRewardedAdShown() {
        logEvent(AdEvents.REWARDED_SHOWN)
    }

    fun trackRewardedAdEarned(rewardType: String, rewardAmount: Int) {
        logEvent(AdEvents.REWARDED_EARNED, Bundle().apply {
            putString("reward_type", rewardType)
            putInt("reward_amount", rewardAmount)
        })
    }

    // ==================== PREMIUM & MONETIZATION EVENTS ====================

    object PremiumEvents {
        const val SCREEN_VIEWED = "premium_screen_viewed"
        const val PLAN_SELECTED = "premium_plan_selected"
        const val PURCHASE_STARTED = "premium_purchase_started"
        const val PURCHASE_SUCCESS = "premium_purchase_success"
        const val PURCHASE_FAILED = "premium_purchase_failed"
        const val RESTORE_STARTED = "premium_restore_started"
        const val RESTORE_SUCCESS = "premium_restore_success"
        const val RESTORE_FAILED = "premium_restore_failed"
        const val FEATURE_GATE_HIT = "premium_feature_gate_hit"
        const val EXPIRY_ISLAND_SHOWN = "premium_expiry_island_shown"
        const val EXPIRY_ISLAND_ACTION = "premium_expiry_island_action"
        const val AD_REWARD_DIALOG_SHOWN = "ad_reward_dialog_shown"
        const val AD_REWARD_WATCHED = "ad_reward_watched"
        const val AD_REWARD_CELEBRATION = "ad_reward_celebration"
        const val REMOVE_ADS_CTA_TAPPED = "remove_ads_cta_tapped"
    }

    fun trackPremiumScreenViewed(source: String) {
        logEvent(PremiumEvents.SCREEN_VIEWED, Bundle().apply {
            putString("source", source)
        })
    }

    fun trackPremiumPlanSelected(planId: String, planPrice: String = "") {
        logEvent(PremiumEvents.PLAN_SELECTED, Bundle().apply {
            putString("plan_id", planId)
            putString("plan_price", planPrice)
        })
    }

    fun trackPremiumPurchaseStarted(planId: String) {
        logEvent(PremiumEvents.PURCHASE_STARTED, Bundle().apply {
            putString("plan_id", planId)
        })
    }

    fun trackPremiumPurchaseSuccess(planId: String, isLifetime: Boolean) {
        logEvent(PremiumEvents.PURCHASE_SUCCESS, Bundle().apply {
            putString("plan_id", planId)
            putBoolean("is_lifetime", isLifetime)
        })
    }

    fun trackPremiumPurchaseFailed(planId: String, errorCode: String) {
        logEvent(PremiumEvents.PURCHASE_FAILED, Bundle().apply {
            putString("plan_id", planId)
            putString("error_code", errorCode)
        })
    }

    fun trackPremiumRestoreStarted() {
        logEvent(PremiumEvents.RESTORE_STARTED)
    }

    fun trackPremiumRestoreResult(success: Boolean) {
        logEvent(
            if (success) PremiumEvents.RESTORE_SUCCESS
            else PremiumEvents.RESTORE_FAILED
        )
    }

    fun trackFeatureGateHit(featureId: String, action: String = "redirect_premium") {
        logEvent(PremiumEvents.FEATURE_GATE_HIT, Bundle().apply {
            putString("feature_id", featureId)
            putString("action", action)
        })
    }

    fun trackExpiryIslandShown() {
        logEvent(PremiumEvents.EXPIRY_ISLAND_SHOWN)
    }

    fun trackExpiryIslandAction(action: String) {
        logEvent(PremiumEvents.EXPIRY_ISLAND_ACTION, Bundle().apply {
            putString("action", action)
        })
    }

    fun trackAdRewardDialogShown(source: String) {
        logEvent(PremiumEvents.AD_REWARD_DIALOG_SHOWN, Bundle().apply {
            putString("source", source)
        })
    }

    fun trackAdRewardWatched(dayNumber: Int) {
        logEvent(PremiumEvents.AD_REWARD_WATCHED, Bundle().apply {
            putInt("day_number", dayNumber)
        })
    }

    fun trackRemoveAdsCTATapped(placement: String) {
        logEvent(PremiumEvents.REMOVE_ADS_CTA_TAPPED, Bundle().apply {
            putString("placement", placement)
        })
    }
}
