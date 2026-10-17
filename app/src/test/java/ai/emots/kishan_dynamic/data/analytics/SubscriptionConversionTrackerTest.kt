package ai.emots.kishan_dynamic.data.analytics

import ai.emots.kishan_dynamic.data.premium.FormattedPlanDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubscriptionConversionTrackerTest {

    private lateinit var tracker: FakeAnalyticsTracker
    private lateinit var conversionTracker: SubscriptionConversionTracker

    @Before
    fun setUp() {
        tracker = FakeAnalyticsTracker()
        conversionTracker = SubscriptionConversionTracker(tracker)
    }

    @Test
    fun testTrackPaywallViewed() {
        conversionTracker.trackPaywallViewed("settings_hub")

        assertTrue(tracker.hasEvent(AnalyticsEventNames.PAYWALL_VIEWED))
        val event = tracker.getEvents(AnalyticsEventNames.PAYWALL_VIEWED).first()
        assertEquals("settings_hub", event.parameters[AnalyticsParamNames.SOURCE])
    }

    @Test
    fun testTrackPlanSelected() {
        val plan = FormattedPlanDetails(
            id = "pro_yearly",
            title = "Yearly Access",
            price = "₹599",
            note = "Best Value"
        )
        conversionTracker.trackPlanSelected(plan)

        assertTrue(tracker.hasEvent(AnalyticsEventNames.PLAN_SELECTED))
        val event = tracker.getEvents(AnalyticsEventNames.PLAN_SELECTED).first()
        assertEquals("pro_yearly", event.parameters[AnalyticsParamNames.PLAN_ID])
        assertEquals(599.0, event.parameters[AnalyticsParamNames.VALUE])
        assertEquals("INR", event.parameters[AnalyticsParamNames.CURRENCY])
    }

    @Test
    fun testTrackCheckoutStarted() {
        val plan = FormattedPlanDetails(
            id = "pro_monthly",
            title = "Monthly Access",
            price = "$4.99",
            note = "Flexible"
        )
        conversionTracker.trackCheckoutStarted(plan)

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BEGIN_CHECKOUT))
        assertTrue(tracker.hasEvent(AnalyticsEventNames.SUBSCRIPTION_CHECKOUT_STARTED))

        val checkout = tracker.getEvents(AnalyticsEventNames.BEGIN_CHECKOUT).first()
        assertEquals("pro_monthly", checkout.parameters[AnalyticsParamNames.PLAN_ID])
        assertEquals(4.99, checkout.parameters[AnalyticsParamNames.VALUE])
        assertEquals("USD", checkout.parameters[AnalyticsParamNames.CURRENCY])
    }

    @Test
    fun testTrackVerifiedPurchaseIsIdempotent() {
        val firstResult = conversionTracker.trackVerifiedPurchase(
            transactionId = "GPA.1234-5678-9012",
            planId = "pro_yearly",
            formattedPrice = "₹599"
        )
        assertTrue(firstResult)

        assertEquals(1, tracker.getEvents(AnalyticsEventNames.PURCHASE).size)
        assertEquals(1, tracker.getEvents(AnalyticsEventNames.ENTITLEMENT_ACTIVATED).size)

        val purchaseEvent = tracker.getEvents(AnalyticsEventNames.PURCHASE).first()
        assertEquals("GPA.1234-5678-9012", purchaseEvent.parameters[AnalyticsParamNames.TRANSACTION_ID])
        assertEquals("pro_yearly", purchaseEvent.parameters[AnalyticsParamNames.PLAN_ID])
        assertEquals(599.0, purchaseEvent.parameters[AnalyticsParamNames.VALUE])
        assertEquals("INR", purchaseEvent.parameters[AnalyticsParamNames.CURRENCY])

        // Second call with duplicate transaction ID MUST NOT log another purchase event
        val duplicateResult = conversionTracker.trackVerifiedPurchase(
            transactionId = "GPA.1234-5678-9012",
            planId = "pro_yearly",
            formattedPrice = "₹599"
        )
        assertFalse(duplicateResult)
        assertEquals(1, tracker.getEvents(AnalyticsEventNames.PURCHASE).size)
        assertEquals(1, tracker.getEvents(AnalyticsEventNames.ENTITLEMENT_ACTIVATED).size)

        // New distinct transaction ID must succeed
        val newResult = conversionTracker.trackVerifiedPurchase(
            transactionId = "GPA.9876-5432-1098",
            planId = "pro_monthly",
            formattedPrice = "$4.99"
        )
        assertTrue(newResult)
        assertEquals(2, tracker.getEvents(AnalyticsEventNames.PURCHASE).size)
    }

    @Test
    fun testParsePriceAndCurrency() {
        val (valInr, currInr) = SubscriptionConversionTracker.parsePriceAndCurrency("₹199")
        assertEquals(199.0, valInr, 0.001)
        assertEquals("INR", currInr)

        val (valUsd, currUsd) = SubscriptionConversionTracker.parsePriceAndCurrency("$9.99")
        assertEquals(9.99, valUsd, 0.001)
        assertEquals("USD", currUsd)

        val (valEur, currEur) = SubscriptionConversionTracker.parsePriceAndCurrency("€14.50")
        assertEquals(14.50, valEur, 0.001)
        assertEquals("EUR", currEur)

        val (valGbp, currGbp) = SubscriptionConversionTracker.parsePriceAndCurrency("£7.00")
        assertEquals(7.00, valGbp, 0.001)
        assertEquals("GBP", currGbp)

        val (valJpy, currJpy) = SubscriptionConversionTracker.parsePriceAndCurrency("¥1000")
        assertEquals(1000.0, valJpy, 0.001)
        assertEquals("JPY", currJpy)
    }

    @Test
    fun testPurchasePendingCanceledFailedAndRestored() {
        conversionTracker.trackPurchasePending("pro_yearly")
        assertTrue(tracker.hasEvent(AnalyticsEventNames.PURCHASE_PENDING))

        conversionTracker.trackPurchaseCanceled("pro_yearly")
        assertTrue(tracker.hasEvent(AnalyticsEventNames.PURCHASE_CANCELED))

        conversionTracker.trackPurchaseFailed("pro_yearly", 6, "Payment declined")
        assertTrue(tracker.hasEvent(AnalyticsEventNames.PURCHASE_FAILED))
        val failed = tracker.getEvents(AnalyticsEventNames.PURCHASE_FAILED).first()
        assertEquals(6, failed.parameters[AnalyticsParamNames.RESPONSE_CODE])
        assertEquals("Payment declined", failed.parameters[AnalyticsParamNames.REASON])

        conversionTracker.trackSubscriptionRestored()
        assertTrue(tracker.hasEvent(AnalyticsEventNames.SUBSCRIPTION_RESTORED))
    }
}
