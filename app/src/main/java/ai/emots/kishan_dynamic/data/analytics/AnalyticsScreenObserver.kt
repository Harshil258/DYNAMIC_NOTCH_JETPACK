package ai.emots.kishan_dynamic.data.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * CompositionLocal providing current AnalyticsTracker instance to UI hierarchy.
 */
val LocalAnalyticsTracker = compositionLocalOf<AnalyticsTracker?> { null }

/**
 * CompositionLocal providing current ScreenDeduplicator to UI hierarchy.
 */
val LocalScreenDeduplicator = compositionLocalOf<ScreenDeduplicator?> { null }

/**
 * Thread-safe screen view deduplicator preventing redundant screen_view events
 * caused by Jetpack Compose recomposition or redundant state emissions.
 */
class ScreenDeduplicator(private val tracker: AnalyticsTracker) {
    @Volatile
    private var lastScreenName: String? = null

    @Synchronized
    fun onScreenVisible(screenName: String, screenClass: String = "MainActivity"): Boolean {
        if (screenName.isNotBlank() && screenName != lastScreenName) {
            lastScreenName = screenName
            tracker.logScreen(screenName, screenClass)
            return true
        }
        return false
    }

    fun currentScreen(): String? = lastScreenName

    fun reset() {
        lastScreenName = null
    }
}

/**
 * Lifecycle-aware composable helper to safely observe screen views with
 * automatic recomposition deduplication and ON_RESUME lifecycle handling.
 */
@Composable
fun TrackScreenView(
    screenName: String,
    tracker: AnalyticsTracker? = null,
    screenClass: String = "MainActivity",
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val effectiveTracker = tracker ?: LocalAnalyticsTracker.current ?: remember(context) { FirebaseAnalyticsTracker(context) }
    val effectiveDeduplicator = LocalScreenDeduplicator.current ?: remember(effectiveTracker) { ScreenDeduplicator(effectiveTracker) }

    val currentScreenName by rememberUpdatedState(screenName)
    val currentScreenClass by rememberUpdatedState(screenClass)

    DisposableEffect(screenName, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                effectiveDeduplicator.onScreenVisible(currentScreenName, currentScreenClass)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            effectiveDeduplicator.onScreenVisible(currentScreenName, currentScreenClass)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
