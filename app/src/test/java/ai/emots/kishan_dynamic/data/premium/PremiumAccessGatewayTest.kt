package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumAccessGatewayTest {
    @Test
    fun defaultProviderNeverPretendsToGrantEntitlement() {
        val gateway = UnconfiguredPremiumAccessGateway()

        assertTrue(gateway.startPurchase("yearly") is PremiumAccessResult.Unavailable)
        assertTrue(gateway.showRewardedPass() is PremiumAccessResult.Unavailable)
        assertTrue(gateway.restorePurchases() is PremiumAccessResult.Unavailable)
    }
}
