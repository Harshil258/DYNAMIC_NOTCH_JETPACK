package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.BillingCrashlyticsReporter
import ai.emots.kishan_dynamic.data.analytics.BillingDiagnosticReporter
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.SubscriptionConversionTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PremiumBillingPlan(
    val planId: String,
    val productId: String,
    val productType: String,
    val defaultTitle: String,
    val defaultPrice: String,
    val defaultNote: String,
    val badge: String? = null
)

/** Formatted, UI-ready plan details reflecting localized store currency and trial terms. */
data class FormattedPlanDetails(
    val id: String,
    val title: String,
    val price: String,
    val note: String,
    val badge: String? = null,
    val freeTrialPeriod: String? = null,
    val productDetails: ProductDetails? = null,
    val offerToken: String? = null
)

class PremiumBillingCatalog(
    plans: List<PremiumBillingPlan> = defaultPremiumBillingPlans()
) {
    private val plansById = plans.associateBy { it.planId }
    val allPlans: List<PremiumBillingPlan> = plans

    fun plan(planId: String): PremiumBillingPlan? = plansById[planId]

    companion object {
        fun default(): PremiumBillingCatalog = PremiumBillingCatalog()
    }
}

fun defaultPremiumBillingPlans(): List<PremiumBillingPlan> = listOf(
    PremiumBillingPlan("weekly", "dynamic_island_weekly", BillingClient.ProductType.SUBS, "Weekly", "₹39", "Billed every week"),
    PremiumBillingPlan("monthly", "dynamic_island_monthly", BillingClient.ProductType.SUBS, "Monthly", "₹59", "Billed every month"),
    PremiumBillingPlan("yearly", "dynamic_island_yearly", BillingClient.ProductType.SUBS, "Yearly", "₹99", "Billed once a year", "Save 70%"),
    PremiumBillingPlan("lifetime", "dynamic_island_lifetime", BillingClient.ProductType.INAPP, "Lifetime", "₹149", "One-time payment", "Best value")
)

/**
 * Production-grade Google Play Billing Gateway for Dynamic Island Pro.
 * Pre-fetches localized pricing, manages subscription lifecycles, safely handles pending purchases,
 * and enforces transparent Google Play subscription management.
 */
class PlayBillingPremiumAccessGateway(
    context: Context,
    private val catalog: PremiumBillingCatalog = PremiumBillingCatalog.default(),
    private val analyticsTracker: AnalyticsTracker = FirebaseAnalyticsTracker(context),
    private val diagnosticReporter: BillingDiagnosticReporter = BillingCrashlyticsReporter(analyticsTracker),
    private val conversionTracker: SubscriptionConversionTracker = SubscriptionConversionTracker(analyticsTracker)
) : PremiumAccessGateway {

    private val hostContext = context
    private val appContext = context.applicationContext
    private val preferences = AuroraPreferences(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var billingClient: BillingClient? = null
    private var pendingPlanId: String? = null
    private var pendingActivity: Activity? = null
    private var restorePending = false
    private var connectionRetryCount = 0

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _plans = MutableStateFlow<List<FormattedPlanDetails>>(
        catalog.allPlans.map { plan ->
            FormattedPlanDetails(
                id = plan.planId,
                title = plan.defaultTitle,
                price = plan.defaultPrice,
                note = plan.defaultNote,
                badge = plan.badge
            )
        }
    )
    val plans: StateFlow<List<FormattedPlanDetails>> = _plans.asStateFlow()

    private val cachedProductDetails = mutableMapOf<String, ProductDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach(::handlePurchase)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                conversionTracker.trackPurchaseCanceled(pendingPlanId ?: "unknown")
                reportStatus("Purchase cancelled.")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                reportStatus("Item already owned. Restoring access…")
                restorePurchases()
            }
            else -> {
                val planId = pendingPlanId ?: "unknown"
                val message = result.debugMessage.ifBlank { "Google Play could not complete the purchase." }
                conversionTracker.trackPurchaseFailed(planId, result.responseCode, message)
                diagnosticReporter.onPurchaseFailed(result.responseCode, message, planId)
                reportStatus(message)
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

        val formattedPlan = _plans.value.firstOrNull { it.id == planId }
        if (formattedPlan != null) {
            conversionTracker.trackCheckoutStarted(formattedPlan)
        }

        pendingPlanId = planId
        pendingActivity = activity

        val client = billingClient
        if (client?.isReady == true) {
            launchBillingFlowForPlan(client, plan, activity)
        } else {
            connect()
        }
        return PremiumAccessResult.Started("Opening Google Play…")
    }

    override fun showRewardedPass(): PremiumAccessResult = PremiumAccessResult.Unavailable(
        "Rewarded ads are not configured in this build yet."
    )

    override fun restorePurchases(): PremiumAccessResult {
        conversionTracker.trackSubscriptionRestored()
        val client = billingClient
        if (client?.isReady == true) {
            queryOwnedPurchases(client)
        } else {
            restorePending = true
            connect()
        }
        return PremiumAccessResult.Started("Checking Google Play purchases…")
    }

    fun trackPaywallViewed(source: String) {
        conversionTracker.trackPaywallViewed(source)
    }

    fun trackPlanSelected(plan: FormattedPlanDetails) {
        conversionTracker.trackPlanSelected(plan)
    }

    fun openManageSubscription(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun close() {
        pendingPlanId = null
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
                    connectionRetryCount++
                    diagnosticReporter.onConnectionFailed(
                        responseCode = result.responseCode,
                        debugMessage = result.debugMessage,
                        purchaseFlowActive = pendingPlanId != null
                    )
                    reportStatus(result.debugMessage.ifBlank { "Google Play Billing is unavailable." })
                    return
                }

                if (connectionRetryCount > 0) {
                    diagnosticReporter.onConnectionRestored(connectionRetryCount)
                }
                connectionRetryCount = 0

                // Eagerly prefetch localized pricing for all catalog plans
                queryAllProductDetails(client)

                if (restorePending) {
                    restorePending = false
                    queryOwnedPurchases(client)
                }

                pendingPlanId?.let { planId ->
                    catalog.plan(planId)?.let { plan ->
                        pendingActivity?.let { activity ->
                            launchBillingFlowForPlan(client, plan, activity)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                connectionRetryCount++
                val isPurchaseActive = pendingPlanId != null
                diagnosticReporter.onConnectionLost(
                    failureType = "service_disconnected",
                    retryAttempt = connectionRetryCount,
                    purchaseFlowActive = isPurchaseActive
                )
                reportStatus("Google Play Billing disconnected. Reconnecting…")
                scope.launch {
                    delay(2000L.coerceAtLeast(1000L * connectionRetryCount.coerceAtMost(5)))
                    connect()
                }
            }
        })
    }

    private fun queryAllProductDetails(client: BillingClient) {
        val subProducts = catalog.allPlans
            .filter { it.productType == BillingClient.ProductType.SUBS }
            .map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it.productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
        val inAppProducts = catalog.allPlans
            .filter { it.productType == BillingClient.ProductType.INAPP }
            .map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it.productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }

        val allProducts = subProducts + inAppProducts
        if (allProducts.isEmpty()) return

        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(allProducts).build()
        ) { result, response ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                response.productDetailsList.forEach { details ->
                    cachedProductDetails[details.productId] = details
                }
                updateFormattedPlans()
            } else {
                diagnosticReporter.onProductQueryFailed(result.responseCode, result.debugMessage)
            }
        }
    }

    private fun updateFormattedPlans() {
        val updated = catalog.allPlans.map { plan ->
            val details = cachedProductDetails[plan.productId]
            if (details != null) {
                when (plan.productType) {
                    BillingClient.ProductType.SUBS -> {
                        val offer = details.subscriptionOfferDetails?.firstOrNull()
                        val pricingPhase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                        val trialPhase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull {
                            it.priceAmountMicros == 0L
                        }
                        val trialText = trialPhase?.let {
                            "Free trial: ${it.billingPeriod}"
                        }
                        FormattedPlanDetails(
                            id = plan.planId,
                            title = plan.defaultTitle,
                            price = pricingPhase?.formattedPrice ?: plan.defaultPrice,
                            note = plan.defaultNote,
                            badge = plan.badge,
                            freeTrialPeriod = trialText,
                            productDetails = details,
                            offerToken = offer?.offerToken
                        )
                    }
                    BillingClient.ProductType.INAPP -> {
                        val oneTime = details.oneTimePurchaseOfferDetails
                        FormattedPlanDetails(
                            id = plan.planId,
                            title = plan.defaultTitle,
                            price = oneTime?.formattedPrice ?: plan.defaultPrice,
                            note = plan.defaultNote,
                            badge = plan.badge,
                            productDetails = details
                        )
                    }
                    else -> FormattedPlanDetails(
                        id = plan.planId,
                        title = plan.defaultTitle,
                        price = plan.defaultPrice,
                        note = plan.defaultNote,
                        badge = plan.badge
                    )
                }
            } else {
                FormattedPlanDetails(
                    id = plan.planId,
                    title = plan.defaultTitle,
                    price = plan.defaultPrice,
                    note = plan.defaultNote,
                    badge = plan.badge
                )
            }
        }
        _plans.value = updated
    }

    private fun launchBillingFlowForPlan(
        client: BillingClient,
        plan: PremiumBillingPlan,
        activity: Activity
    ) {
        val cached = cachedProductDetails[plan.productId]
        if (cached != null) {
            launchFlowWithDetails(client, plan, cached, activity)
        } else {
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
                cachedProductDetails[details.productId] = details
                updateFormattedPlans()
                launchFlowWithDetails(client, plan, details, activity)
            }
        }
    }

    private fun launchFlowWithDetails(
        client: BillingClient,
        plan: PremiumBillingPlan,
        details: ProductDetails,
        activity: Activity
    ) {
        val flowProduct = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (plan.productType == BillingClient.ProductType.SUBS) {
            val offerToken = details.subscriptionOfferDetails
                ?.firstOrNull()
                ?.offerToken
            if (offerToken == null) {
                reportStatus("No eligible offer is available for this plan.")
                return
            }
            flowProduct.setOfferToken(offerToken)
        }

        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(flowProduct.build()))
                .build()
        )
        pendingPlanId = null
        pendingActivity = null
    }

    private fun queryOwnedPurchases(client: BillingClient) {
        var foundAny = false
        val types = listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP)
        var completedQueries = 0

        types.forEach { type ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build()
            ) { result, purchases ->
                completedQueries++
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val activePurchases = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    if (activePurchases.isNotEmpty()) {
                        foundAny = true
                        activePurchases.forEach(::handlePurchase)
                    }
                }

                if (completedQueries == types.size) {
                    if (!foundAny) {
                        reportStatus("No active purchases found for this Google Play account.")
                    } else {
                        reportStatus("Purchases restored successfully.")
                    }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                val planId = pendingPlanId ?: purchase.products.firstOrNull() ?: "unknown"
                conversionTracker.trackPurchasePending(planId)
                reportStatus("Payment is pending bank verification. Premium will activate once payment settles.")
                return
            }
            Purchase.PurchaseState.PURCHASED -> {
                val client = billingClient ?: return
                if (!purchase.isAcknowledged) {
                    client.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    ) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            persistEntitlement(purchase)
                        } else {
                            reportStatus(result.debugMessage.ifBlank { "Purchase acknowledgement failed." })
                        }
                    }
                } else {
                    persistEntitlement(purchase)
                }
            }
            else -> Unit
        }
    }

    private fun persistEntitlement(purchase: Purchase) {
        scope.launch {
            preferences.setProEntitlement(source = "play_billing")
            val plan = _plans.value.firstOrNull { it.id == pendingPlanId }
                ?: _plans.value.firstOrNull { it.id == purchase.products.firstOrNull() }
            conversionTracker.trackVerifiedPurchase(purchase, plan)
            diagnosticReporter.onPurchaseRecovered(plan?.id ?: purchase.products.firstOrNull() ?: "unknown")
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
