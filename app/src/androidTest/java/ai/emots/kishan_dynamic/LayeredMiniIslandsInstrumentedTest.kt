package ai.emots.kishan_dynamic

import android.graphics.Bitmap
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.*
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Direct Codex/ADB exploration; fixture actions only, no Android notification APIs. */
@RunWith(AndroidJUnit4::class)
class LayeredMiniIslandsInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private val engine = IslandBehaviorEngine()
    private var state by mutableStateOf(IslandBehaviorState())
    private var now = 1000L
    private fun event(e: IslandEngineEvent) { state = engine.reduce(state, e, now++).state }
    private fun sample(id: String) = NotificationInfo(id = id, packageName = "fixture.$id", appName = when(id) {
        "main" -> "WhatsApp"; "child-1" -> "Telegram"; "child-2" -> "Gmail"; else -> "Instagram"
    }, title = "Alex · $id", text = "Synthetic preview for visual validation", timestamp = now++)
    @Before fun prepare() {
        IslandStateManager.setPaywallActive(true)
        ai.emots.kishan_dynamic.service.PermissionUtils.isAwaitingAccessibilityReturn = false
        event(IslandEngineEvent.IngestNotification(sample("main"), autoExpand = false))
        event(IslandEngineEvent.TapCompact)
    }
    @After fun finish() { IslandStateManager.setPaywallActive(false) }
    private fun render(width: Int = 360, font: Float = 1f, reduced: Boolean = true, rtl: Boolean = false) {
        compose.setContent {
            val config = Configuration(LocalConfiguration.current).apply { screenWidthDp = width; screenHeightDp = 800; fontScale = font }
            CompositionLocalProvider(LocalConfiguration provides config, LocalReducedMotion provides reduced,
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalDensity provides Density(minOf(LocalDensity.current.density, LocalConfiguration.current.screenWidthDp * LocalDensity.current.density / width), font)) {
                AppTheme(darkTheme = true) {
                    Box(Modifier.width(width.dp)) {
                        LayeredMiniIslands(state, onEvent = ::event,
                            onPromote = { key, token -> event(IslandEngineEvent.TapChild(key, token)) }) {
                            DynamicIslandPill(
                                state = IslandDemoState.NotificationExpanded,
                                notifications = state.queue.notifications.map { ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy.notification(it, state.childPrivacy.deviceLocked, state.childPrivacy.hideSensitiveContent) }, notificationActiveIndex = state.queue.activeIndex,
                                onNotificationSelect = { event(IslandEngineEvent.SelectPage(it)) },
                                modifier = Modifier.width(width.dp).testTag("fixture_main"))
                        }
                    }
                }
            }
        }
    }
    private fun arrive(n: Int) = compose.runOnIdle { repeat(n) { event(IslandEngineEvent.IngestNotification(sample("child-${it+1}"))) } }
    private fun shot(name: String) {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(target.filesDir, "task05-evidence").apply { mkdirs() }
        File(folder, "$name.png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun newestInsertionIsBelowMainAndMaintainsIdentity() {
        render(430); arrive(2)
        compose.onNodeWithTag("child_child-1").assertExists()
        val before = compose.onNodeWithTag("child_child-1").fetchSemanticsNode().boundsInRoot.top
        compose.runOnIdle { event(IslandEngineEvent.IngestNotification(sample("child-3"))) }
        compose.waitForIdle()
        val main = compose.onNodeWithTag("fixture_main", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val latest = compose.onNodeWithTag("child_child-3").fetchSemanticsNode().boundsInRoot
        val older = compose.onNodeWithTag("child_child-1").fetchSemanticsNode().boundsInRoot
        assertTrue(latest.top >= main.bottom); assertTrue(older.top > before)
        assertEquals("main", state.selectedKey); assertEquals(4, state.canonicalQueueCount)
        shot("three-card-stack")
    }
    @Test fun promotionTapAndExpiryKeepCanonicalPagerQueue() {
        render(); arrive(3)
        compose.onNodeWithTag("child_child-2").performClick()
        compose.waitUntil(5000) { state.selectedKey == "child-2" }
        compose.onNodeWithTag("child_child-2").assertDoesNotExist()
        assertEquals(4, state.canonicalQueueCount)
        val child = state.childNotices.first()
        compose.runOnIdle { event(IslandEngineEvent.ChildTimeout(child.key, child.token)) }
        compose.waitForIdle()
        assertTrue(state.queue.notifications.any { it.id == child.key }); assertEquals(4, state.canonicalQueueCount)
        shot("promoted-main-and-merged-child")
    }
    @Test fun burstOverflowCountsOnlyNewArrivals() {
        render(430); arrive(25)
        compose.onNodeWithTag("child_overflow").assertTextContains("+22", substring = true)
        assertEquals(26, state.canonicalQueueCount); assertEquals(25, state.pendingChildNoticesCount)
        shot("overflow-25-arrivals")
    }
    @Test fun narrowLargeTextRedactionAndReducedMotion() {
        render(320, 2f, rtl = true); arrive(3)
        compose.runOnIdle {
            event(IslandEngineEvent.UpdateNotification(sample("child-3").copy(
                title = "A deliberately long sender name for narrow RTL layout validation",
                text = "A multiline synthetic message preview that must remain readable and bounded at large font sizes.")))
        }
        compose.waitForIdle()
        shot("narrow-large-text-rtl")
        compose.runOnIdle { event(IslandEngineEvent.PrivacyChanged(ChildPrivacyPolicy(true, true))) }
        compose.onNodeWithTag("child_child-3").assertExists()
        compose.onAllNodesWithText("New notification", useUnmergedTree = true).onFirst().assertExists()
        shot("narrow-large-text-redaction")
    }
    @Test fun promotionCannotSelectReusedSourceIdentity() {
        render(reduced = false); arrive(1)
        compose.waitForIdle()
        compose.onNodeWithTag("child_child-1").assertExists()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("child_child-1").performClick()
        compose.runOnIdle {
            event(IslandEngineEvent.RemoveNotification("child-1"))
            event(IslandEngineEvent.IngestNotification(sample("child-1")))
        }
        compose.waitForIdle()
        // Advance beyond the promotion window while leaving the replacement queued.
        compose.mainClock.advanceTimeBy(1000)
        compose.mainClock.autoAdvance = true
        compose.waitUntil(5000) { state.childLifecycles["child-1"] != ChildLifecycle.PROMOTING }
        compose.runOnIdle { assertEquals("main", state.selectedKey); assertEquals(2, state.canonicalQueueCount) }
    }
    @Test fun horizontalPagerRemainsInteractiveWithChildren() {
        render(430); arrive(3)
        compose.onNodeWithTag("fixture_main", useUnmergedTree = true).performTouchInput { swipeRight() }
        compose.waitUntil(5000) { state.selectedKey != "main" }
        assertEquals(4, state.canonicalQueueCount)
        assertTrue(state.queue.notifications.any { it.id == "main" })
        shot("pager-swipe-with-children")
    }
    @Test fun captureActualEmergenceAndMergeFrames() {
        render(430, reduced = false)
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        arrive(1)
        compose.mainClock.advanceTimeBy(240)
        shot("emerging-frame")
        compose.mainClock.advanceTimeBy(1000)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        val child = state.childNotices.single()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { event(IslandEngineEvent.ChildTimeout(child.key, child.token)) }
        compose.mainClock.advanceTimeBy(96)
        shot("merge-frame")
        compose.mainClock.advanceTimeBy(1000)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertEquals(2, state.canonicalQueueCount)
    }

}
