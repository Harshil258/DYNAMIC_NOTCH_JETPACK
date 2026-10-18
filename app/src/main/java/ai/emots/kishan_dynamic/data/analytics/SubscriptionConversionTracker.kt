package ai.emots.kishan_dynamic.data.analytics

import ai.emots.kishan_dynamic.data.premium.FormattedPlanDetails
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

/**
 * Manages idempotent subscription and in-app purchase conversion tracking in GA4.
 * Guarantees that purchase conversions are logged exactly once per confirmed transaction,
 * preventing duplicate conversion inflated counts in Google Ads and GA4.
 */
class SubscriptionConversionTracker(
    private val analyticsTracker: AnalyticsTracker,
    initialRecordedTransactions: Set<String> = emptySet(),
    private val onTransactionRecorded: ((String) -> Unit)? = null
) {
    // Set of already-tracked transaction IDs / tokens
    private val recordedTransactions = java.util.Collections.synchronizedSet(initialRecordedTransactions.toMutableSet())

    fun markTransactionRecorded(transactionId: String) {
        recordedTransactions.add(transactionId)
    }

    fun isTransactionRecorded(transactionId: String): Boolean {
        return recordedTransactions.contains(transactionId)
    }

    fun trackPaywallViewed(source: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.PAYWALL_VIEWED,
            mapOf(AnalyticsParamNames.SOURCE to source)
        )
    }

    fun trackPlanSelected(plan: FormattedPlanDetails) {
        val parsed = parsePriceAndCurrency(plan.price)
        analyticsTracker.logEvent(
            AnalyticsEventNames.PLAN_SELECTED,
            mapOf(
                AnalyticsParamNames.PLAN_ID to plan.id,
                AnalyticsParamNames.VALUE to parsed.first,
                AnalyticsParamNames.CURRENCY to parsed.second
            )
        )
    }

    fun trackCheckoutStarted(plan: FormattedPlanDetails) {
        val parsed = parsePriceAndCurrency(plan.price)
        val params = mapOf(
            AnalyticsParamNames.PLAN_ID to plan.id,
            AnalyticsParamNames.ITEM_ID to plan.id,
            AnalyticsParamNames.ITEM_NAME to plan.title,
            AnalyticsParamNames.VALUE to parsed.first,
            AnalyticsParamNames.CURRENCY to parsed.second
        )
        // GA4 standard begin_checkout + domain subscription_checkout_started
        analyticsTracker.logEvent(AnalyticsEventNames.BEGIN_CHECKOUT, params)
        analyticsTracker.logEvent(AnalyticsEventNames.SUBSCRIPTION_CHECKOUT_STARTED, params)
    }

    @Synchronized
    fun trackVerifiedPurchase(
        transactionId: String,
        planId: String,
        formattedPrice: String
    ): Boolean {
        if (recordedTransactions.contains(transactionId)) {
            return false // Already recorded in this session or persistent store
        }
        recordedTransactions.add(transactionId)
        onTransactionRecorded?.invoke(transactionId)

        val parsed = parsePriceAndCurrency(formattedPrice)

        val params = mapOf(
            AnalyticsParamNames.TRANSACTION_ID to transactionId,
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.ITEM_ID to planId,
            AnalyticsParamNames.VALUE to parsed.first,
            AnalyticsParamNames.CURRENCY to parsed.second
        )

        analyticsTracker.logEvent(AnalyticsEventNames.PURCHASE, params)
        analyticsTracker.logEvent(
            AnalyticsEventNames.ENTITLEMENT_ACTIVATED,
            mapOf(
                AnalyticsParamNames.SOURCE to "play_billing",
                AnalyticsParamNames.IS_PRO to true,
                AnalyticsParamNames.PLAN_ID to planId
            )
        )
        return true
    }

    @Synchronized
    fun trackVerifiedPurchase(
        purchaseState: Int,
        transactionId: String,
        planId: String,
        formattedPrice: String
    ): Boolean {
        // Strict assertion: Pending or unspecified purchases MUST NOT trigger purchase conversion
        if (purchaseState != Purchase.PurchaseState.PURCHASED) {
            return false
        }
        return trackVerifiedPurchase(transactionId, planId, formattedPrice)
    }

    /**
     * Idempotently tracks a confirmed, settled purchase from Play Billing Purchase.
     * Strictly verifies that purchaseState == PURCHASED before conversion reporting.
     * Returns true if tracked, or false if already recorded or not in PURCHASED state.
     */
    @Synchronized
    fun trackVerifiedPurchase(
        purchase: Purchase,
        plan: FormattedPlanDetails?
    ): Boolean {
        val token = runCatching { purchase.purchaseToken }.getOrNull()
        val orderId = runCatching { purchase.orderId }.getOrNull()
        val transactionId = orderId?.takeIf { it.isNotBlank() } ?: token?.takeLast(16) ?: "unknown_tx"
        val products = runCatching { purchase.products }.getOrNull()
        val planId = plan?.id ?: products?.firstOrNull() ?: "unknown_plan"
        val price = plan?.price ?: "₹199"
        return trackVerifiedPurchase(purchase.purchaseState, transactionId, planId, price)
    }

    fun trackPurchasePending(planId: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.PURCHASE_PENDING,
            mapOf(AnalyticsParamNames.PLAN_ID to planId)
        )
    }

    fun trackPurchaseCanceled(planId: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.PURCHASE_CANCELED,
            mapOf(AnalyticsParamNames.PLAN_ID to planId)
        )
    }

    fun trackPurchaseFailed(planId: String, responseCode: Int, message: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.PURCHASE_FAILED,
            mapOf(
                AnalyticsParamNames.PLAN_ID to planId,
                AnalyticsParamNames.RESPONSE_CODE to responseCode,
                AnalyticsParamNames.REASON to message
            )
        )
    }

    fun trackSubscriptionRestored(source: String = "google_play") {
        analyticsTracker.logEvent(
            AnalyticsEventNames.SUBSCRIPTION_RESTORED,
            mapOf(AnalyticsParamNames.SOURCE to source)
        )
    }

    companion object {
        /**
         * Safely extracts numerical value and currency code from formatted price strings like "₹199" or "$4.99".
         */
        fun parsePriceAndCurrency(formattedPrice: String): Pair<Double, String> {
            val currency = when {
                formattedPrice.contains("₹") -> "INR"
                formattedPrice.contains("$") -> "USD"
                formattedPrice.contains("€") -> "EUR"
                formattedPrice.contains("£") -> "GBP"
                formattedPrice.contains("¥") -> "JPY"
                else -> "INR"
            }
            val numericString = formattedPrice.replace(Regex("[^0-9.]"), "")
            val value = numericString.toDoubleOrNull() ?: 0.0
            return Pair(value, currency)
        }
    }
}
