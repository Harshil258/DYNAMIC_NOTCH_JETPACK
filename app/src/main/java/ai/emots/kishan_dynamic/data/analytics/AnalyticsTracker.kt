package ai.emots.kishan_dynamic.data.analytics

/**
 * Standard centralized analytics interface for Google Analytics 4 / Firebase Analytics.
 * Provides type-safe logging for core engagement, onboarding funnels,
 * monetization conversions, and non-sensitive UI interaction metrics.
 */
interface AnalyticsTracker {
    fun logEvent(name: String, parameters: Map<String, Any> = emptyMap())
    fun logScreen(screenName: String, screenClass: String = "MainActivity")
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String?)
}

/**
 * Canonical GA4 and application-specific event names.
 */
object AnalyticsEventNames {
    // GA4 Recommended Standard Events
    const val SCREEN_VIEW = "screen_view"
    const val BEGIN_CHECKOUT = "begin_checkout"
    const val PURCHASE = "purchase"
    const val SELECT_ITEM = "select_item"
    const val AD_IMPRESSION = "ad_impression"

    // Onboarding Funnel Events
    const val ONBOARDING_STARTED = "onboarding_started"
    const val ONBOARDING_STEP_VIEWED = "onboarding_step_viewed"
    const val ONBOARDING_STEP_COMPLETED = "onboarding_step_completed"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val PERMISSION_FLOW_STARTED = "permission_flow_started"
    const val PERMISSION_RESULT = "permission_result"
    const val ISLAND_ACTIVATION_STARTED = "island_activation_started"
    const val ISLAND_ACTIVATED = "island_activated"
    const val ISLAND_ACTIVATION_FAILED = "island_activation_failed"

    // Core Dynamic Island Feature Events
    const val ISLAND_STATE_TOGGLED = "island_state_toggled"
    const val ISLAND_EXPAND = "island_expand"
    const val ISLAND_COLLAPSE = "island_collapse"
    const val ISLAND_SWIPE_DISMISS = "island_swipe_dismiss"
    const val PREVIEW_STARTED = "preview_started"
    const val PREVIEW_STOPPED = "preview_stopped"
    const val THEME_SELECTED = "theme_selected"
    const val ALIGNMENT_CHANGED = "alignment_changed"
    const val MEDIA_CONTROL_USED = "media_control_used"
    const val QUICK_CONTROL_TAPPED = "quick_control_tapped"

    // Monetization & Subscription Funnel Events
    const val PAYWALL_VIEWED = "paywall_viewed"
    const val PLAN_SELECTED = "plan_selected"
    const val SUBSCRIPTION_CHECKOUT_STARTED = "subscription_checkout_started"
    const val PURCHASE_PENDING = "purchase_pending"
    const val PURCHASE_FAILED = "purchase_failed"
    const val PURCHASE_CANCELED = "purchase_canceled"
    const val SUBSCRIPTION_RESTORED = "subscription_restored"
    const val ENTITLEMENT_ACTIVATED = "entitlement_activated"

    // Diagnostic & Ad Revenue Events
    const val BILLING_CONNECTION_LOST = "billing_connection_lost"
    const val BILLING_CONNECTION_RESTORED = "billing_connection_restored"
    const val BILLING_CONNECTION_FAILED = "billing_connection_failed"
    const val BILLING_PRODUCT_QUERY_FAILED = "billing_product_query_failed"
    const val BILLING_PURCHASE_FAILED = "billing_purchase_failed"
    const val BILLING_PURCHASE_RECOVERED = "billing_purchase_recovered"
}

/**
 * Standard event parameter names.
 */
object AnalyticsParamNames {
    const val SCREEN_NAME = "screen_name"
    const val SCREEN_CLASS = "screen_class"
    const val VALUE = "value"
    const val CURRENCY = "currency"
    const val TRANSACTION_ID = "transaction_id"
    const val ITEM_ID = "item_id"
    const val ITEM_NAME = "item_name"
    const val PLAN_ID = "plan_id"
    const val SOURCE = "source"
    const val REASON = "reason"
    const val STEP_INDEX = "step_index"
    const val STEP_NAME = "step_name"
    const val PERMISSION_TYPE = "permission_type"
    const val GRANTED = "granted"
    const val IS_PRO = "is_pro"
    const val ISLAND_STATE = "island_state"
    const val ACTION = "action"
    const val THEME = "theme"
    const val PLACEMENT = "placement"
    const val AD_PLATFORM = "ad_platform"
    const val AD_FORMAT = "ad_format"
    const val AD_UNIT_ID = "ad_unit_id"
    const val PRECISION_TYPE = "precision_type"
    const val FAILURE_TYPE = "failure_type"
    const val RESPONSE_CODE = "response_code"
}
