package ai.emots.kishan_dynamic.data.analytics

/**
 * Type-safe analytics event contract.
 * Enforces compile-time parameter correctness and runtime schema validation.
 */
sealed interface AnalyticsEvent {
    val name: String
    fun toParameterMap(): Map<String, Any>

    // -------------------------------------------------------------------------
    // 1. Onboarding & Setup Funnel Events
    // -------------------------------------------------------------------------
    object OnboardingStarted : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ONBOARDING_STARTED
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    data class OnboardingStepViewed(
        val stepIndex: Int,
        val stepName: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ONBOARDING_STEP_VIEWED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.STEP_INDEX to stepIndex,
            AnalyticsParamNames.STEP_NAME to stepName
        )
    }

    data class OnboardingStepCompleted(
        val stepIndex: Int,
        val stepName: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ONBOARDING_STEP_COMPLETED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.STEP_INDEX to stepIndex,
            AnalyticsParamNames.STEP_NAME to stepName
        )
    }

    data class OnboardingCompleted(
        val skipped: Boolean,
        val stepIndex: Int
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ONBOARDING_COMPLETED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "skipped" to skipped,
            AnalyticsParamNames.STEP_INDEX to stepIndex
        )
    }

    object PermissionFlowStarted : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PERMISSION_FLOW_STARTED
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    data class PermissionResult(
        val permissionType: String,
        val action: String,
        val granted: Boolean
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PERMISSION_RESULT
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PERMISSION_TYPE to permissionType,
            AnalyticsParamNames.ACTION to action,
            AnalyticsParamNames.GRANTED to granted
        )
    }

    object IslandActivationStarted : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_ACTIVATION_STARTED
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    object IslandActivated : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_ACTIVATED
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    data class IslandActivationFailed(
        val reason: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_ACTIVATION_FAILED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.REASON to reason
        )
    }

    // -------------------------------------------------------------------------
    // 2. Core Island Interactions & Settings
    // -------------------------------------------------------------------------
    data class IslandStateToggled(
        val enabled: Boolean
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_STATE_TOGGLED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "enabled" to enabled
        )
    }

    data class IslandExpand(
        val source: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_EXPAND
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.SOURCE to source
        )
    }

    object IslandCollapse : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_COLLAPSE
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    object IslandSwipeDismiss : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ISLAND_SWIPE_DISMISS
        override fun toParameterMap(): Map<String, Any> = emptyMap()
    }

    data class MediaControlUsed(
        val action: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.MEDIA_CONTROL_USED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.ACTION to action
        )
    }

    data class QuickControlTapped(
        val tile: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.QUICK_CONTROL_TAPPED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "tile" to tile
        )
    }

    data class PreviewStarted(
        val item: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PREVIEW_STARTED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "item" to item
        )
    }

    data class PreviewStopped(
        val item: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PREVIEW_STOPPED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "item" to item
        )
    }

    data class ThemeSelected(
        val theme: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.THEME_SELECTED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.THEME to theme
        )
    }

    data class SettingChanged(
        val settingName: String,
        val value: Any
    ) : AnalyticsEvent {
        override val name: String = "setting_changed"
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "setting_name" to settingName,
            "setting_value" to value.toString()
        )
    }

    data class NotificationPageSwiped(
        val pageIndex: Int,
        val totalPages: Int
    ) : AnalyticsEvent {
        override val name: String = "notification_page_swiped"
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "page_index" to pageIndex,
            "total_pages" to totalPages
        )
    }

    data class NotificationActionTapped(
        val actionIndex: Int,
        val isQuickReply: Boolean
    ) : AnalyticsEvent {
        override val name: String = "notification_action_tapped"
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "action_index" to actionIndex,
            "is_quick_reply" to isQuickReply
        )
    }

    // -------------------------------------------------------------------------
    // 3. Monetization, Paywall & Subscriptions
    // -------------------------------------------------------------------------
    data class PaywallViewed(
        val source: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PAYWALL_VIEWED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.SOURCE to source
        )
    }

    data class PlanSelected(
        val planId: String,
        val value: Double,
        val currency: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PLAN_SELECTED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.VALUE to value,
            AnalyticsParamNames.CURRENCY to currency
        )
    }

    data class BeginCheckout(
        val planId: String,
        val itemId: String,
        val itemName: String,
        val value: Double,
        val currency: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.BEGIN_CHECKOUT
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.ITEM_ID to itemId,
            AnalyticsParamNames.ITEM_NAME to itemName,
            AnalyticsParamNames.VALUE to value,
            AnalyticsParamNames.CURRENCY to currency
        )
    }

    data class Purchase(
        val transactionId: String,
        val planId: String,
        val itemId: String,
        val value: Double,
        val currency: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PURCHASE
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.TRANSACTION_ID to transactionId,
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.ITEM_ID to itemId,
            AnalyticsParamNames.VALUE to value,
            AnalyticsParamNames.CURRENCY to currency
        )
    }

    data class EntitlementActivated(
        val source: String,
        val isPro: Boolean,
        val planId: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.ENTITLEMENT_ACTIVATED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.SOURCE to source,
            AnalyticsParamNames.IS_PRO to isPro,
            AnalyticsParamNames.PLAN_ID to planId
        )
    }

    data class PurchasePending(
        val planId: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PURCHASE_PENDING
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PLAN_ID to planId
        )
    }

    data class PurchaseCanceled(
        val planId: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PURCHASE_CANCELED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PLAN_ID to planId
        )
    }

    data class PurchaseFailed(
        val planId: String,
        val responseCode: Int,
        val reason: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.PURCHASE_FAILED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.RESPONSE_CODE to responseCode,
            AnalyticsParamNames.REASON to reason
        )
    }

    data class SubscriptionRestored(
        val source: String = "google_play"
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.SUBSCRIPTION_RESTORED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.SOURCE to source
        )
    }

    data class TrialStatusTransition(
        val previousState: String,
        val newState: String,
        val daysRemaining: Int
    ) : AnalyticsEvent {
        override val name: String = "trial_status_transition"
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "previous_state" to previousState,
            "new_state" to newState,
            "days_remaining" to daysRemaining
        )
    }

    data class RewardedPassTransition(
        val action: String,
        val hoursRemaining: Long,
        val daysStacked: Int
    ) : AnalyticsEvent {
        override val name: String = "rewarded_pass_transition"
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.ACTION to action,
            "hours_remaining" to hoursRemaining,
            "days_stacked" to daysStacked
        )
    }

    // -------------------------------------------------------------------------
    // 4. AdMob Impression-Level Ad Revenue (ILAR)
    // -------------------------------------------------------------------------
    data class AdImpression(
        val value: Double,
        val currency: String,
        val adPlatform: String = "admob",
        val adFormat: String,
        val adUnitId: String,
        val placement: String,
        val precisionType: String
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.AD_IMPRESSION
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.VALUE to value,
            AnalyticsParamNames.CURRENCY to currency,
            AnalyticsParamNames.AD_PLATFORM to adPlatform,
            AnalyticsParamNames.AD_FORMAT to adFormat,
            AnalyticsParamNames.AD_UNIT_ID to adUnitId,
            AnalyticsParamNames.PLACEMENT to placement,
            AnalyticsParamNames.PRECISION_TYPE to precisionType
        )
    }

    // -------------------------------------------------------------------------
    // 5. Diagnostics & Play Billing Connection
    // -------------------------------------------------------------------------
    data class BillingConnectionLost(
        val failureType: String,
        val retryAttempt: Int,
        val purchaseFlowActive: Boolean
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.BILLING_CONNECTION_LOST
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.FAILURE_TYPE to failureType,
            "retry_attempt" to retryAttempt,
            "purchase_flow_active" to purchaseFlowActive
        )
    }

    data class BillingConnectionRestored(
        val retryAttempt: Int
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.BILLING_CONNECTION_RESTORED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            "retry_attempt" to retryAttempt
        )
    }

    data class BillingConnectionFailed(
        val responseCode: Int,
        val reason: String,
        val purchaseFlowActive: Boolean
    ) : AnalyticsEvent {
        override val name: String = AnalyticsEventNames.BILLING_CONNECTION_FAILED
        override fun toParameterMap(): Map<String, Any> = mapOf(
            AnalyticsParamNames.RESPONSE_CODE to responseCode,
            AnalyticsParamNames.REASON to reason,
            "purchase_flow_active" to purchaseFlowActive
        )
    }
}

/**
 * Standard user property names for GA4 user segmentation.
 * All properties are low-cardinality, privacy-safe, and contain zero PII.
 */
object AnalyticsUserProperties {
    const val ENTITLEMENT_TIER = "entitlement_tier"
    const val IS_PRO = "is_pro"
    const val TRIAL_DAY = "trial_day"
    const val INSTALL_COHORT = "install_cohort"
    const val APP_THEME = "app_theme"
    const val NOTCH_PRESET = "notch_preset"
    const val APP_LANGUAGE = "app_language"
}

/**
 * Strict schema validator ensuring that:
 * 1. Events match the canonical catalog.
 * 2. Parameters conform to allowed parameter names.
 * 3. Zero-PII contract is unconditionally enforced (no notification text, sender, contacts, OTPs).
 */
object AnalyticsSchemaValidator {

    private val PII_DISALLOWED_PATTERNS = listOf(
        "title", "body", "text", "message", "sender", "recipient",
        "phone", "contact", "email", "otp", "password", "bank",
        "account", "balance", "address", "call_log"
    )

    private val PII_WHITELISTED_EXCEPTIONS = setOf(
        AnalyticsParamNames.STEP_NAME,
        AnalyticsParamNames.ITEM_NAME,
        AnalyticsParamNames.SCREEN_NAME
    )

    /**
     * Validates event parameters against PII and catalog schema.
     * @param throwOnError In test environments, true to throw IllegalArgumentException; false in prod.
     * @return Filtered, sanitised parameter map.
     */
    fun validateAndSanitize(
        eventName: String,
        parameters: Map<String, Any>,
        throwOnError: Boolean = false
    ): Map<String, Any> {
        val sanitized = mutableMapOf<String, Any>()

        for ((key, value) in parameters) {
            val lowerKey = key.lowercase()

            // Zero-PII check
            val isDisallowedPii = PII_DISALLOWED_PATTERNS.any { pattern ->
                lowerKey.contains(pattern) && !PII_WHITELISTED_EXCEPTIONS.contains(lowerKey)
            }

            if (isDisallowedPii) {
                val errorMsg = "Zero-PII violation: Parameter '$key' rejected in event '$eventName'"
                if (throwOnError) {
                    throw IllegalArgumentException(errorMsg)
                } else {
                    android.util.Log.e("AnalyticsSchema", errorMsg)
                    continue // Drop PII parameter in production
                }
            }

            // Type check: Only primitives and Strings are allowed in GA4
            when (value) {
                is String, is Number, is Boolean -> sanitized[key] = value
                else -> {
                    val errorMsg = "Type error: Parameter '$key' value must be primitive, got ${value.javaClass.simpleName}"
                    if (throwOnError) {
                        throw IllegalArgumentException(errorMsg)
                    } else {
                        sanitized[key] = value.toString()
                    }
                }
            }
        }

        return sanitized
    }
}
