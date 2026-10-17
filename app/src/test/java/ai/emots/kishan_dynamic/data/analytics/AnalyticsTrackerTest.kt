package ai.emots.kishan_dynamic.data.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyticsTrackerTest {

    private lateinit var tracker: FakeAnalyticsTracker
    private lateinit var deduplicator: ScreenDeduplicator

    @Before
    fun setUp() {
        tracker = FakeAnalyticsTracker()
        deduplicator = ScreenDeduplicator(tracker)
    }

    @Test
    fun testFakeAnalyticsTrackerLogsEventsAndParameters() {
        tracker.logEvent(
            AnalyticsEventNames.ONBOARDING_STARTED,
            mapOf("source" to "first_open", "version" to 1)
        )

        assertTrue(tracker.hasEvent(AnalyticsEventNames.ONBOARDING_STARTED))
        val logged = tracker.getEvents(AnalyticsEventNames.ONBOARDING_STARTED).first()
        assertEquals("first_open", logged.parameters["source"])
        assertEquals(1, logged.parameters["version"])
    }

    @Test
    fun testFakeAnalyticsTrackerUserPropertiesAndId() {
        tracker.setUserProperty("is_pro", "true")
        assertEquals("true", tracker.userProperties["is_pro"])

        tracker.setUserId("user_12345")
        assertEquals("user_12345", tracker.currentUserId)
    }

    @Test
    fun testScreenDeduplicatorPreventsDuplicateEvents() {
        deduplicator.onScreenVisible("onboarding_screen")
        deduplicator.onScreenVisible("onboarding_screen")
        deduplicator.onScreenVisible("onboarding_screen")

        assertEquals(1, tracker.screens.size)
        assertEquals("onboarding_screen", tracker.screens.first().name)
        assertEquals("onboarding_screen", deduplicator.currentScreen())
    }

    @Test
    fun testScreenDeduplicatorAllowsSubsequentDifferentScreens() {
        deduplicator.onScreenVisible("main_tab_island")
        deduplicator.onScreenVisible("main_tab_features")
        deduplicator.onScreenVisible("main_tab_island")

        assertEquals(3, tracker.screens.size)
        assertEquals("main_tab_island", tracker.screens[0].name)
        assertEquals("main_tab_features", tracker.screens[1].name)
        assertEquals("main_tab_island", tracker.screens[2].name)
    }

    @Test
    fun testScreenDeduplicatorIgnoresBlankScreenNames() {
        deduplicator.onScreenVisible("")
        deduplicator.onScreenVisible("   ")

        assertTrue(tracker.screens.isEmpty())
    }

    @Test
    fun testScreenDeduplicatorResetAllowsSameScreenAgain() {
        deduplicator.onScreenVisible("paywall_screen")
        assertEquals(1, tracker.screens.size)

        deduplicator.reset()
        deduplicator.onScreenVisible("paywall_screen")
        assertEquals(2, tracker.screens.size)
    }
}
