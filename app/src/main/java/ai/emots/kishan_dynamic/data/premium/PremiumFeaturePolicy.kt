package ai.emots.kishan_dynamic.data.premium

/**
 * Central entitlement checks for settings that also change live overlay behavior.
 * Keeping this pure prevents a stale preference from becoming a premium bypass.
 */
object PremiumFeaturePolicy {
    const val QUICK_CONTROL = "quick_control"
    const val THEME_SETTINGS = "theme_settings"
    const val SOUND_SETTINGS = "sound_settings"
    const val CALL_END_DISABLE = "call_end_disable"
    const val CALL_END_CUSTOMIZATION = "call_end_customization"
    const val COMPACT_MUSIC = "compact_music"
    const val NOTIFICATION_DURATION = "notification_duration"
    const val DISPLAY_HORIZONTAL_OFFSET = "display_horizontal_offset"

    private val referencePremiumFeatures = setOf(
        QUICK_CONTROL,
        THEME_SETTINGS,
        SOUND_SETTINGS,
        CALL_END_DISABLE,
        CALL_END_CUSTOMIZATION,
        COMPACT_MUSIC,
        NOTIFICATION_DURATION,
        DISPLAY_HORIZONTAL_OFFSET
    )

    fun isLocked(featureId: String, isProActive: Boolean): Boolean =
        featureId in referencePremiumFeatures && !isProActive

    fun compactMusicControlsEnabled(
        isProActive: Boolean,
        requested: Boolean
    ): Boolean = isProActive && requested

    fun horizontalOffsetEnabled(isProActive: Boolean): Boolean = isProActive

    /**
     * All users (free and Pro) have complete control over enabling/disabling sound and ringer HUDs.
     * Prevents silent restarts of indicators when entitlements transition.
     */
    fun soundSettingEnabled(isProActive: Boolean, requested: Boolean): Boolean = requested

    /**
     * Resolves AUR-P0-004: All users (free and Pro) have complete, honest control
     * over whether call summaries appear. Free users are never forced to see call summaries.
     */
    fun callSummaryEnabled(isProActive: Boolean, requested: Boolean): Boolean = requested

    /**
     * Pro users can customize call summary timeout and action layouts.
     */
    fun callSummaryCustomizationEnabled(isProActive: Boolean): Boolean = isProActive
}
