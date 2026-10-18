package ai.emots.kishan_dynamic.data.premium

import com.android.billingclient.api.BillingClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PremiumBillingCatalogTest {

    @Test
    fun defaultCatalogSeparatesSubscriptionsAndLifetimePurchase() {
        val catalog = PremiumBillingCatalog.default()

        assertEquals(BillingClient.ProductType.SUBS, catalog.plan("weekly")?.productType)
        assertEquals(BillingClient.ProductType.SUBS, catalog.plan("yearly")?.productType)
        assertEquals(BillingClient.ProductType.INAPP, catalog.plan("lifetime")?.productType)
    }

    @Test
    fun unknownPlansAreRejectedBeforeBillingFlow() {
        assertNull(PremiumBillingCatalog.default().plan("unknown"))
    }

    @Test
    fun defaultCatalogEnforcesAuthenticBadgesWithoutFabricatedDiscounts() {
        val catalog = PremiumBillingCatalog.default()
        assertEquals("Most popular", catalog.plan("yearly")?.badge)
        assertEquals("Lifetime access", catalog.plan("lifetime")?.badge)

        // Enforce zero fabricated savings or discount percentages
        catalog.allPlans.forEach { plan ->
            val badge = plan.badge ?: ""
            assert(!badge.contains("%")) { "Plan ${plan.planId} contains fabricated percentage discount: $badge" }
            assert(!badge.contains("Save", ignoreCase = true)) { "Plan ${plan.planId} contains fabricated savings claim: $badge" }
        }
    }
}
