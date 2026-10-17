package ai.emots.kishan_dynamic.data.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Thread-safe screen view deduplicator preventing redundant screen_view events
 * caused by Jetpack Compose recomposition or redundant state emissions.
 */
class ScreenDeduplicator(private val tracker: AnalyticsTracker) {
    @Volatile
    private var lastScreenName: String? = null

    @Synchronized
    fun onScreenVisible(screenName: String, screenClass: String = "MainActivity") {
        if (screenName.isNotBlank() && screenName != lastScreenName) {
            lastScreenName = screenName
            tracker.logScreen(screenName, screenClass)
        }
    }

    fun currentScreen(): String? = lastScreenName

    fun reset() {
        lastScreenName = null
    }
}

/**
 * Compose helper to safely observe screen views with automatic recomposition deduplication.
 */
@Composable
fun TrackScreenView(
    screenName: String,
    tracker: AnalyticsTracker,
    screenClass: String = "MainActivity"
) {
    LaunchedEffect(screenName) {
        tracker.logScreen(screenName, screenClass)
    }
}
