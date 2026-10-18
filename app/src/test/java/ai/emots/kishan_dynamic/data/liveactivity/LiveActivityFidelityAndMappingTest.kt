package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.LiveCapabilityStatus
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.displayPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 08A verification test for Live Experience engine fidelity,
 * structured field mapping, capability classification, and prioritization.
 */
class LiveActivityFidelityAndMappingTest {

    @Test
    fun mapsNavigationWithStructuredFidelityFields() {
        val notif = NotificationInfo(
            id = "nav-test-1",
            packageName = "com.google.android.apps.maps",
            appName = "Google Maps",
            title = "In 200 ft · Turn right onto Mission St",
            text = "Toward 4th St · 12 min (2.4 mi)",
            category = "navigation",
            isOngoing = true,
            progress = 3,
            progressMax = 10
        )

        val activity = NotificationLiveActivityMapper.map(notif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.NAVIGATION, activity?.kind)
        assertTrue(activity?.isLiveSource == true)
        assertEquals("nav-test-1", activity?.id)
        assertNotNull(activity?.maneuverType)
        assertEquals("right", activity?.maneuverType)
        assertNotNull(activity?.primaryMetric)
        assertEquals("200 ft", activity?.primaryMetric)
        assertEquals("IN 200 FT", activity?.statusBadgeText)
    }

    @Test
    fun mapsNavigationTurnLeftManeuverCorrectly() {
        val notif = NotificationInfo(
            id = "nav-test-2",
            packageName = "com.waze",
            appName = "Waze",
            title = "In 500 m, turn left onto Grand Ave",
            text = "Route 101 North",
            category = "navigation",
            isOngoing = true
        )

        val activity = NotificationLiveActivityMapper.map(notif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.NAVIGATION, activity?.kind)
        assertEquals("left", activity?.maneuverType)
        assertEquals("500 m", activity?.primaryMetric)
    }

    @Test
    fun mapsDeliveryNotificationWithStructuredFidelityFields() {
        val notif = NotificationInfo(
            id = "delivery-zomato-1",
            packageName = "com.application.zomato",
            appName = "Zomato",
            title = "Burger King order arriving in 15 mins",
            text = "Rider Rahul is out for delivery",
            isOngoing = true
        )

        val activity = NotificationLiveActivityMapper.map(notif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.DELIVERY, activity?.kind)
        assertTrue(activity?.isLiveSource == true)
        assertEquals("OUT FOR DELIVERY", activity?.statusBadgeText)
        assertEquals("arriving in 15 mins", activity?.primaryMetric)
        assertEquals("Burger King", activity?.entityName)
    }

    @Test
    fun mapsSwiggyDeliveryDeliveredStatus() {
        val notif = NotificationInfo(
            id = "delivery-swiggy-2",
            packageName = "in.swiggy.android",
            appName = "Swiggy",
            title = "Order delivered!",
            text = "Enjoy your meal from Pizza Hut",
            isOngoing = true
        )

        val activity = NotificationLiveActivityMapper.map(notif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.DELIVERY, activity?.kind)
        assertEquals("DELIVERED", activity?.statusBadgeText)
        assertEquals(1.0f, activity?.progress)
    }

    @Test
    fun sportsActivityPreservesFootballAndRejectsCricket() {
        val footballNotif = NotificationInfo(
            id = "fotmob-football",
            packageName = "com.mobilefootie.fotmob",
            appName = "FotMob",
            title = "Real Madrid 2 - 1 Man City",
            text = "78' UEFA Champions League",
            isOngoing = true
        )

        val activity = NotificationLiveActivityMapper.map(footballNotif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.SPORTS, activity?.kind)
        assertEquals("2 - 1", activity?.primaryMetric)

        // STRICT SCOPE CONSTRAINT: Zero cricket
        val cricketNotif = NotificationInfo(
            id = "cricket-score-1",
            packageName = "com.cricbuzz.android",
            appName = "Cricbuzz",
            title = "IND vs AUS 240/4",
            text = "Cricket World Cup Live Score",
            isOngoing = true
        )

        val cricketActivity = NotificationLiveActivityMapper.map(cricketNotif)
        // Must NEVER map to a live activity
        assertNull("Cricket notifications must NEVER map to LiveActivity per project policy", cricketActivity)
    }

    @Test
    fun flightNotificationMapsToFlightLiveActivity() {
        val notif = NotificationInfo(
            id = "flight-dl-1",
            packageName = "com.delta.mobile.android",
            appName = "Fly Delta",
            title = "Flight DL 492 to JFK",
            text = "Gate B14 • In flight • Arriving on time",
            isOngoing = true
        )

        val activity = NotificationLiveActivityMapper.map(notif)
        assertNotNull(activity)
        assertEquals(LiveActivityKind.FLIGHT, activity?.kind)
        assertTrue(activity?.isLiveSource == true)
        assertEquals("DL 492", activity?.entityName)
    }

    @Test
    fun capabilityStatusReflectsHonestPlatformCapabilities() {
        // Platform events that are 100% always live
        val fullyLive = listOf(
            LiveActivityKind.TIMER
        )
        fullyLive.forEach { kind ->
            assertTrue(kind.displayPriority >= 90)
        }

        // Conditionally live (depend on app notifications or ongoing system services)
        val conditionallyLive = listOf(
            LiveActivityKind.NAVIGATION,
            LiveActivityKind.DELIVERY,
            LiveActivityKind.FLIGHT,
            LiveActivityKind.SPORTS,
            LiveActivityKind.VOICE_MEMO,
            LiveActivityKind.SCREEN_RECORDING,
            LiveActivityKind.AIR_DROP,
            LiveActivityKind.AIRPODS,
            LiveActivityKind.TRANSIT_ROUTE_ALERT
        )
        conditionallyLive.forEach { kind ->
            assertTrue(kind.displayPriority > 10)
        }

        // Preview only
        assertEquals(10, LiveActivityKind.SHORTCUT.displayPriority)
    }

    @Test
    fun liveActivityPrioritizationPreservesSafetyAndEmergency() {
        // Navigation (100) must outrank entertainment and delivery
        assertTrue(LiveActivityKind.NAVIGATION.displayPriority > LiveActivityKind.DELIVERY.displayPriority)
        assertTrue(LiveActivityKind.NAVIGATION.displayPriority > LiveActivityKind.SPORTS.displayPriority)
        assertTrue(LiveActivityKind.NAVIGATION.displayPriority > LiveActivityKind.FLIGHT.displayPriority)

        // Timers (90) outrank general sports (65) and delivery (75)
        assertTrue(LiveActivityKind.TIMER.displayPriority > LiveActivityKind.DELIVERY.displayPriority)
        assertTrue(LiveActivityKind.TIMER.displayPriority > LiveActivityKind.SPORTS.displayPriority)
    }

    @Test
    fun liveActivityInfoDefaultAndCopyIntegrity() {
        val info = LiveActivityInfo(
            id = "test-id",
            kind = LiveActivityKind.DELIVERY,
            title = "Test Delivery",
            subtitle = "On the way",
            progress = 0.5f,
            entityName = "Chipotle",
            secondaryText = "Order #123",
            statusBadgeText = "ON THE WAY",
            primaryMetric = "12 min",
            secondaryMetric = "Marco",
            actionPhoneNumber = "18005550199",
            isLiveSource = true
        )

        assertEquals("test-id", info.id)
        assertEquals(LiveActivityKind.DELIVERY, info.kind)
        assertEquals("Chipotle", info.entityName)
        assertEquals("18005550199", info.actionPhoneNumber)
        assertTrue(info.isLiveSource)

        val updated = info.copy(progress = 1.0f, statusBadgeText = "DELIVERED")
        assertEquals(1.0f, updated.progress)
        assertEquals("DELIVERED", updated.statusBadgeText)
        assertEquals("Chipotle", updated.entityName)
    }
}
