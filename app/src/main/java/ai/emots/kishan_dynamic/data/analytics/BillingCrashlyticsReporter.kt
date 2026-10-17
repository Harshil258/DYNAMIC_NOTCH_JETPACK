package ai.emots.kishan_dynamic.data.analytics

import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Stable exception type for grouping Google Play Billing connection drops in Crashlytics.
 * Uses consistent message templates without dynamic timestamps to avoid issue fragmentation.
 */
class BillingConnectionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Diagnostic reporting interface for Google Play Billing operations.
 */
interface BillingDiagnosticReporter {
    fun onConnectionLost(failureType: String, retryAttempt: Int, purchaseFlowActive: Boolean)
    fun onConnectionRestored(retryAttempt: Int)
    fun onConnectionFailed(responseCode: Int, debugMessage: String, purchaseFlowActive: Boolean)
    fun onProductQueryFailed(responseCode: Int, debugMessage: String)
    fun onPurchaseFailed(responseCode: Int, debugMessage: String, planId: String?)
    fun onPurchaseRecovered(planId: String)
}

/**
 * Production implementation of BillingDiagnosticReporter using Firebase Crashlytics
 * and AnalyticsTracker. Implements rate limiting to prevent Crashlytics spam during flapping.
 */
class BillingCrashlyticsReporter(
    private val analyticsTracker: AnalyticsTracker,
    private val appVersion: String = "1.0",
    private val billingLibraryVersion: String = "8.3.0",
    private val minCooldownMillis: Long = 60_000L
) : BillingDiagnosticReporter {

    @Volatile
    private var lastReportedFailureType: String? = null

    @Volatile
    private var lastReportedTimestamp: Long = 0L

    @Volatile
    private var isConnected: Boolean = false

    private fun getCrashlytics(): FirebaseCrashlytics? = runCatching {
        FirebaseCrashlytics.getInstance()
    }.getOrNull()

    override fun onConnectionLost(
        failureType: String,
        retryAttempt: Int,
        purchaseFlowActive: Boolean
    ) {
        isConnected = false
        val now = System.currentTimeMillis()

        // Track lifecycle diagnostic event via Analytics
        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_CONNECTION_LOST,
            mapOf(
                AnalyticsParamNames.FAILURE_TYPE to failureType,
                "retry_attempt" to retryAttempt,
                "purchase_flow_active" to purchaseFlowActive
            )
        )

        // Rate limiting: allow at most one non-fatal exception per failure episode or cooldown
        val shouldReportNonFatal = failureType != lastReportedFailureType ||
            (now - lastReportedTimestamp) > minCooldownMillis

        if (shouldReportNonFatal) {
            lastReportedFailureType = failureType
            lastReportedTimestamp = now

            getCrashlytics()?.let { crashlytics ->
                crashlytics.log("Play Billing connection dropped: $failureType (retry=$retryAttempt)")
                setDiagnosticCustomKeys(
                    crashlytics = crashlytics,
                    failureType = failureType,
                    billingPhase = if (purchaseFlowActive) "checkout_active" else "idle_background",
                    responseCode = -1,
                    connectionState = "disconnected",
                    retryAttempt = retryAttempt,
                    purchaseFlowActive = purchaseFlowActive
                )
                // Report grouping-friendly exception without dynamic variables in message
                val exception = BillingConnectionException("Google Play Billing connection dropped: $failureType")
                crashlytics.recordException(exception)
            }
            runCatching { Log.w(TAG, "Reported non-fatal billing connection drop to Crashlytics: $failureType") }
        }
    }

    override fun onConnectionRestored(retryAttempt: Int) {
        isConnected = true
        lastReportedFailureType = null

        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_CONNECTION_RESTORED,
            mapOf("retry_attempt" to retryAttempt)
        )

        getCrashlytics()?.let { crashlytics ->
            crashlytics.log("Play Billing connection successfully restored after $retryAttempt retries")
            crashlytics.setCustomKey("connection_state", "connected")
            crashlytics.setCustomKey("retry_attempt", retryAttempt)
        }
    }

    override fun onConnectionFailed(
        responseCode: Int,
        debugMessage: String,
        purchaseFlowActive: Boolean
    ) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_CONNECTION_FAILED,
            mapOf(
                AnalyticsParamNames.RESPONSE_CODE to responseCode,
                AnalyticsParamNames.REASON to debugMessage,
                "purchase_flow_active" to purchaseFlowActive
            )
        )

        getCrashlytics()?.let { crashlytics ->
            crashlytics.log("Play Billing connection setup failed: code=$responseCode, msg=$debugMessage")
            setDiagnosticCustomKeys(
                crashlytics = crashlytics,
                failureType = "setup_failed",
                billingPhase = "setup",
                responseCode = responseCode,
                connectionState = "failed",
                retryAttempt = 0,
                purchaseFlowActive = purchaseFlowActive
            )
            crashlytics.recordException(
                BillingConnectionException("Google Play Billing setup failed with code $responseCode")
            )
        }
    }

    override fun onProductQueryFailed(responseCode: Int, debugMessage: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_PRODUCT_QUERY_FAILED,
            mapOf(
                AnalyticsParamNames.RESPONSE_CODE to responseCode,
                AnalyticsParamNames.REASON to debugMessage
            )
        )

        getCrashlytics()?.let { crashlytics ->
            crashlytics.log("Play Billing product query failed: code=$responseCode, msg=$debugMessage")
            crashlytics.setCustomKey("billing_response_code", responseCode)
            crashlytics.setCustomKey("billing_phase", "query_products")
            crashlytics.recordException(
                BillingConnectionException("Play Billing queryProductDetails failed with code $responseCode")
            )
        }
    }

    override fun onPurchaseFailed(responseCode: Int, debugMessage: String, planId: String?) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_PURCHASE_FAILED,
            mapOf(
                AnalyticsParamNames.RESPONSE_CODE to responseCode,
                AnalyticsParamNames.REASON to debugMessage,
                AnalyticsParamNames.PLAN_ID to (planId ?: "unknown")
            )
        )

        getCrashlytics()?.let { crashlytics ->
            crashlytics.log("Play Billing purchase failed for plan=$planId, code=$responseCode: $debugMessage")
            crashlytics.setCustomKey("billing_response_code", responseCode)
            crashlytics.setCustomKey("billing_phase", "launch_flow")
            if (planId != null) crashlytics.setCustomKey("plan_id", planId)
        }
    }

    override fun onPurchaseRecovered(planId: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.BILLING_PURCHASE_RECOVERED,
            mapOf(AnalyticsParamNames.PLAN_ID to planId)
        )

        getCrashlytics()?.log("Play Billing purchase recovered/restored for plan=$planId")
    }

    private fun setDiagnosticCustomKeys(
        crashlytics: FirebaseCrashlytics,
        failureType: String,
        billingPhase: String,
        responseCode: Int,
        connectionState: String,
        retryAttempt: Int,
        purchaseFlowActive: Boolean
    ) {
        crashlytics.setCustomKey("component", "play_billing")
        crashlytics.setCustomKey("failure_type", failureType)
        crashlytics.setCustomKey("billing_phase", billingPhase)
        crashlytics.setCustomKey("billing_response_code", responseCode)
        crashlytics.setCustomKey("billing_library_version", billingLibraryVersion)
        crashlytics.setCustomKey("connection_state", connectionState)
        crashlytics.setCustomKey("retry_attempt", retryAttempt)
        crashlytics.setCustomKey("auto_reconnect_enabled", true)
        crashlytics.setCustomKey("app_version", appVersion)
        crashlytics.setCustomKey("android_sdk_version", Build.VERSION.SDK_INT)
        crashlytics.setCustomKey("subscription_screen", "PremiumScreen")
        crashlytics.setCustomKey("purchase_flow_active", purchaseFlowActive)
    }

    companion object {
        private const val TAG = "BillingCrashlytics"
    }
}
