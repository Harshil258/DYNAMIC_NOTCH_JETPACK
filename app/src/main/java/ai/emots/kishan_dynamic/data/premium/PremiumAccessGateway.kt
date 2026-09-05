package ai.emots.kishan_dynamic.data.premium

/** Result returned by the app's monetization boundary. */
sealed interface PremiumAccessResult {
    data class Unavailable(val message: String) : PremiumAccessResult
    data class Started(val message: String) : PremiumAccessResult
}

/**
 * Provider boundary for Play Billing, rewarded ads, and restore-purchases.
 * The UI depends on this contract; it never grants entitlement locally.
 */
interface PremiumAccessGateway {
    fun startPurchase(planId: String): PremiumAccessResult
    fun showRewardedPass(): PremiumAccessResult
    fun restorePurchases(): PremiumAccessResult
}

/**
 * Safe default until a store/ad provider is supplied by the release build.
 * Keeping this explicit prevents a preview action from masquerading as a
 * completed payment or rewarded-ad view.
 */
class UnconfiguredPremiumAccessGateway : PremiumAccessGateway {
    private val message = "Premium store access is not configured in this build yet."

    override fun startPurchase(planId: String): PremiumAccessResult = PremiumAccessResult.Unavailable(message)

    override fun showRewardedPass(): PremiumAccessResult = PremiumAccessResult.Unavailable(
        "Rewarded ads are not configured in this build yet."
    )

    override fun restorePurchases(): PremiumAccessResult = PremiumAccessResult.Unavailable(
        "Purchase restore is not configured in this build yet."
    )
}
