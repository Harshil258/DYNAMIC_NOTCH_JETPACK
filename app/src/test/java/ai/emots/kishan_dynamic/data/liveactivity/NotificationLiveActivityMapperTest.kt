package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationLiveActivityMapperTest {

    @Test
    fun mapsOngoingNavigationNotificationToNavigationActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "maps-1",
                packageName = "com.google.android.apps.maps",
                appName = "Maps",
                title = "Turn right",
                text = "Continue for 400 m",
                category = "navigation",
                isOngoing = true,
                progress = 2,
                progressMax = 4
            )
        )

        assertEquals(LiveActivityKind.NAVIGATION, activity?.kind)
        assertEquals(0.5f, activity?.progress)
        assertEquals("maps-1", activity?.id)
    }

    @Test
    fun mapsDeliveryNotificationToDeliveryActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "delivery-1",
                packageName = "com.swiggy.android",
                appName = "Swiggy",
                title = "Order #12",
                text = "Out for delivery",
                isOngoing = true
            )
        )

        assertEquals(LiveActivityKind.DELIVERY, activity?.kind)
        assertTrue(activity?.subtitle?.contains("delivery", ignoreCase = true) == true)
    }

    @Test
    fun mapsOngoingScreenRecordingNotification() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "recording-1",
                packageName = "android",
                appName = "System UI",
                title = "Screen recording",
                text = "Tap to stop recording",
                isOngoing = true,
                showChronometer = true
            )
        )

        assertEquals(LiveActivityKind.SCREEN_RECORDING, activity?.kind)
    }

    @Test
    fun mapsKnownVoiceRecorderNotificationOnlyForRecorderPackages() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "voice-1",
                packageName = "com.google.android.apps.recorder",
                appName = "Recorder",
                title = "Recording",
                text = "00:24",
                isOngoing = true
            )
        )

        assertEquals(LiveActivityKind.VOICE_MEMO, activity?.kind)
    }

    @Test
    fun mapsExplicitQuickShareNotificationToAirDropActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "share-1",
                packageName = "com.google.android.gms",
                appName = "Google Play services",
                title = "Quick Share",
                text = "Sending to Pixel",
                isOngoing = true,
                progress = 3,
                progressMax = 5
            )
        )

        assertEquals(LiveActivityKind.AIR_DROP, activity?.kind)
        assertEquals(0.6f, activity?.progress)
    }

    @Test
    fun mapsStrictOptionalSystemSurfacesFromKnownNotificationSources() {
        val satellite = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "satellite-1",
                packageName = "com.android.phone",
                appName = "Phone",
                title = "Emergency satellite connection",
                text = "Connected via satellite",
                isOngoing = true
            )
        )
        val findMy = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "find-my-1",
                packageName = "com.google.android.apps.adm",
                appName = "Find My Device",
                title = "Find My Device",
                text = "Device located",
                isOngoing = true
            )
        )
        val handoff = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "handoff-1",
                packageName = "com.microsoft.appmanager",
                appName = "Phone Link",
                title = "Continue on phone",
                text = "Pick up on your phone",
                isOngoing = true
            )
        )
        val mirroring = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "mirror-1",
                packageName = "com.android.systemui",
                appName = "System UI",
                title = "Screen mirroring",
                text = "Stop casting",
                isOngoing = true
            )
        )

        assertEquals(LiveActivityKind.SATELLITE, satellite?.kind)
        assertEquals(LiveActivityKind.FIND_MY, findMy?.kind)
        assertEquals(LiveActivityKind.MOVED_TO_IPHONE, handoff?.kind)
        assertEquals(LiveActivityKind.SCREEN_MIRRORING_ALERT, mirroring?.kind)
    }

    @Test
    fun doesNotPromoteOptionalSurfaceCopyFromUnknownPackage() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "ordinary-optional-copy",
                packageName = "com.example.messaging",
                appName = "Messages",
                title = "Screen mirroring guide",
                text = "Learn how to cast your screen",
                isOngoing = true
            )
        )

        assertNull(activity)
    }

    @Test
    fun mapsKnownTransitNotificationToTransitActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "transit-1",
                packageName = "com.google.android.apps.maps",
                appName = "Maps",
                title = "Bus 42",
                text = "Arrives at platform 3 in 4 min",
                category = "navigation",
                isOngoing = true
            )
        )

        assertEquals(LiveActivityKind.TRANSIT_ROUTE_ALERT, activity?.kind)
        assertEquals("Bus 42", activity?.title)
    }

    @Test
    fun mapsKnownFlightNotificationToFlightActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "flight-1",
                packageName = "com.flightaware.android.flightaware",
                appName = "FlightAware",
                title = "Flight AA 100",
                text = "Boarding at gate B14",
                isOngoing = true,
                progress = 4,
                progressMax = 10
            )
        )

        assertEquals(LiveActivityKind.FLIGHT, activity?.kind)
        assertEquals(0.4f, activity?.progress)
    }

    @Test
    fun mapsKnownSportsNotificationToSportsActivity() {
        val activity = NotificationLiveActivityMapper.map(
            NotificationInfo(
                id = "sports-1",
                packageName = "com.fotmob.android",
                appName = "FotMob",
                title = "Real Madrid vs Man City",
                text = "Score update · 2 - 1",
                isOngoing = true
            )
        )

        assertEquals(LiveActivityKind.SPORTS, activity?.kind)
        assertEquals("Real Madrid vs Man City", activity?.title)
    }

    @Test
    fun ignoresOrdinaryOrCompletedNotifications() {
        val notification = NotificationInfo(
            id = "ordinary",
            packageName = "com.example.app",
            appName = "Example",
            title = "Hello",
            text = "World",
            isOngoing = false
        )

        assertNull(NotificationLiveActivityMapper.map(notification))
    }
}
