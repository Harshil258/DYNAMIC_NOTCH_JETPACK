package ai.emots.kishan_dynamic.data.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingCrashlyticsReporterTest {

    private lateinit var tracker: FakeAnalyticsTracker
    private lateinit var reporter: BillingCrashlyticsReporter

    @Before
    fun setUp() {
        tracker = FakeAnalyticsTracker()
        reporter = BillingCrashlyticsReporter(
            analyticsTracker = tracker,
            appVersion = "1.0.0",
            billingLibraryVersion = "8.3.0",
            minCooldownMillis = 60_000L
        )
    }

    @Test
    fun testOnConnectionLostLogsBillingConnectionLostEvent() {
        reporter.onConnectionLost(
            failureType = "service_disconnected",
            retryAttempt = 1,
            purchaseFlowActive = true
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_CONNECTION_LOST))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_CONNECTION_LOST).first()
        assertEquals("service_disconnected", event.parameters[AnalyticsParamNames.FAILURE_TYPE])
        assertEquals(1, event.parameters["retry_attempt"])
        assertEquals(true, event.parameters["purchase_flow_active"])
    }

    @Test
    fun testOnConnectionRestoredLogsBillingConnectionRestoredEvent() {
        reporter.onConnectionLost("service_disconnected", 1, false)
        reporter.onConnectionRestored(retryAttempt = 2)

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_CONNECTION_RESTORED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_CONNECTION_RESTORED).first()
        assertEquals(2, event.parameters["retry_attempt"])
    }

    @Test
    fun testOnConnectionFailedLogsBillingConnectionFailedEvent() {
        reporter.onConnectionFailed(
            responseCode = 3, // BILLING_UNAVAILABLE
            debugMessage = "Billing service is not available on device",
            purchaseFlowActive = true
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_CONNECTION_FAILED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_CONNECTION_FAILED).first()
        assertEquals(3, event.parameters[AnalyticsParamNames.RESPONSE_CODE])
        assertEquals("Billing service is not available on device", event.parameters[AnalyticsParamNames.REASON])
        assertEquals(true, event.parameters["purchase_flow_active"])
    }

    @Test
    fun testOnProductQueryFailedLogsBillingProductQueryFailedEvent() {
        reporter.onProductQueryFailed(
            responseCode = -1, // SERVICE_DISCONNECTED
            debugMessage = "Client is disconnected"
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_PRODUCT_QUERY_FAILED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_PRODUCT_QUERY_FAILED).first()
        assertEquals(-1, event.parameters[AnalyticsParamNames.RESPONSE_CODE])
        assertEquals("Client is disconnected", event.parameters[AnalyticsParamNames.REASON])
    }

    @Test
    fun testOnPurchaseFailedLogsBillingPurchaseFailedEvent() {
        reporter.onPurchaseFailed(
            responseCode = 6, // ERROR
            debugMessage = "Payment declined",
            planId = "pro_yearly"
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_PURCHASE_FAILED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_PURCHASE_FAILED).first()
        assertEquals(6, event.parameters[AnalyticsParamNames.RESPONSE_CODE])
        assertEquals("Payment declined", event.parameters[AnalyticsParamNames.REASON])
        assertEquals("pro_yearly", event.parameters[AnalyticsParamNames.PLAN_ID])
    }

    @Test
    fun testOnPurchaseRecoveredLogsBillingPurchaseRecoveredEvent() {
        reporter.onPurchaseRecovered("pro_lifetime")

        assertTrue(tracker.hasEvent(AnalyticsEventNames.BILLING_PURCHASE_RECOVERED))
        val event = tracker.getEvents(AnalyticsEventNames.BILLING_PURCHASE_RECOVERED).first()
        assertEquals("pro_lifetime", event.parameters[AnalyticsParamNames.PLAN_ID])
    }
}
