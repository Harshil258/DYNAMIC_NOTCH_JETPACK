package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AnalyticsSink
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PremiumBillingPlan(
    val planId: String,
    val productId: String,
    val productType: String
)

/** Product IDs are supplied here so Play Console configuration stays outside UI code. */
class PremiumBillingCatalog(
    plans: List<PremiumBillingPlan> = defaultPremiumBillingPlans()
) {
    private val plansById = plans.associateBy { it.planId }

    fun plan(planId: String): PremiumBillingPlan? = plansById[planId]

    companion object {
        fun default(): PremiumBillingCatalog = PremiumBillingCatalog()
    }
}

fun defaultPremiumBillingPlans(): List<PremiumBillingPlan> = listOf(
    PremiumBillingPlan("weekly", "dynamic_island_weekly", BillingClient.ProductType.SUBS),
    PremiumBillingPlan("monthly", "dynamic_island_monthly", BillingClient.ProductType.SUBS),
    PremiumBillingPlan("yearly", "dynamic_island_yearly", BillingClient.ProductType.SUBS),
    PremiumBillingPlan("lifetime", "dynamic_island_lifetime", BillingClient.ProductType.INAPP)
)

/**
 * Google Play Billing adapter for the app-owned premium boundary.
 *
 * It only activates Pro after Play reports a PURCHASED item and never grants
 * access from a button click, preview, or failed store connection.
 */
class PlayBillingPremiumAccessGateway(
    context: Context,
    private val catalog: PremiumBillingCatalog = PremiumBillingCatalog.default(),
    private val analytics: AnalyticsSink = AppAnalytics
) : PremiumAccessGateway {

    private val hostContext = context
    private val appContext = context.applicationContext
    private val preferences = AuroraPreferences(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var billingClient: BillingClient? = null
    private var pendingPlan: PremiumBillingPlan? = null
    private var pendingActivity: Activity? = null
    private var restorePending = false
    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::handlePurchase)
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                analytics.track(AnalyticsEvent("premium_purchase_cancelled"))
                reportStatus("Purchase cancelled.")
            }
            else -> {
                analytics.track(
                    AnalyticsEvent("premium_purchase_failed", mapOf("response_code" to result.responseCode.toString()))
                )
                reportStatus(result.debugMessage.ifBlank { "Google Play could not complete the purchase." })
            }
        }
    }

    init {
        connect()
    }

    override fun startPurchase(planId: String): PremiumAccessResult {
        val plan = catalog.plan(planId)
            ?: return PremiumAccessResult.Unavailable("This premium plan is not configured.")
        val activity = hostContext.findActivity()
            ?: return PremiumAccessResult.Unavailable("Premium purchases require an active app screen.")

        analytics.track(AnalyticsEvent("premium_purchase_started", mapOf("plan" to plan.planId)))
        pendingPlan = plan
        pendingActivity = activity
        val client = billingClient
        if (client?.isReady == true) {
            queryAndLaunch(client, plan, activity)
        } else {
            connect()
        }
        return PremiumAccessResult.Started("Opening Google Play…")
    }

    override fun showRewardedPass(): PremiumAccessResult = PremiumAccessResult.Unavailable(
        "Rewarded ads are not configured in this build yet."
    )

    override fun restorePurchases(): PremiumAccessResult {
        analytics.track(AnalyticsEvent("premium_restore_started"))
        val client = billingClient
        if (client?.isReady == true) {
            queryOwnedPurchases(client)
        } else {
            restorePending = true
            connect()
        }
        return PremiumAccessResult.Started("Checking Google Play purchases…")
    }

    fun close() {
        pendingPlan = null
        pendingActivity = null
        restorePending = false
        billingClient?.endConnection()
        billingClient = null
        scope.coroutineContext.cancel()
    }

    private fun connect() {
        if (billingClient?.isReady == true) return
        val client = billingClient ?: BillingClient.newBuilder(appContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()
            .also { billingClient = it }

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    reportStatus(result.debugMessage.ifBlank { "Google Play Billing is unavailable." })
                    return
                }
                if (restorePending) {
                    restorePending = false
                    queryOwnedPurchases(client)
                }
                pendingPlan?.let { plan ->
                    pendingActivity?.let { activity -> queryAndLaunch(client, plan, activity) }
                }
            }

            override fun onBillingServiceDisconnected() {
                reportStatus("Google Play Billing disconnected. Try again when Play Store is available.")
            }
        })
    }

    private fun queryAndLaunch(
        client: BillingClient,
        plan: PremiumBillingPlan,
        activity: Activity
    ) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(plan.productId)
            .setProductType(plan.productType)
            .build()
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { result, response ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                reportStatus(result.debugMessage.ifBlank { "Premium products are unavailable." })
                return@queryProductDetailsAsync
            }
            val details = response.productDetailsList.firstOrNull()
            if (details == null) {
                reportStatus("This premium plan is not available in Google Play yet.")
                return@queryProductDetailsAsync
            }

            val flowProduct = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
            if (plan.productType == BillingClient.ProductType.SUBS) {
                val offerToken = details.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.offerToken
                if (offerToken == null) {
                    reportStatus("No eligible offer is available for this plan.")
                    return@queryProductDetailsAsync
                }
                flowProduct.setOfferToken(offerToken)
            }

            client.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(flowProduct.build()))
                    .build()
            )
            pendingPlan = null
            pendingActivity = null
        }
    }

    private fun queryOwnedPurchases(client: BillingClient) {
        listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP).forEach { type ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build()
            ) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.orEmpty().forEach(::handlePurchase)
                } else {
                    reportStatus(result.debugMessage.ifBlank { "Could not restore Google Play purchases." })
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val client = billingClient ?: return
        if (!purchase.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    reportStatus(result.debugMessage.ifBlank { "Purchase acknowledgement failed." })
                    return@acknowledgePurchase
                }
                persistEntitlement(purchase)
            }
        } else {
            persistEntitlement(purchase)
        }
    }

    private fun persistEntitlement(purchase: Purchase) {
        scope.launch {
            preferences.setProEntitlement(source = "play_billing")
            analytics.track(AnalyticsEvent("premium_purchase_success"))
            reportStatus("Premium is active on this Google Play account.")
        }
    }

    private fun reportStatus(message: String) {
        _status.value = message
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return current as? Activity
    }
}
