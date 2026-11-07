package ai.emots.kishan_dynamic

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic Compose fixtures. These never post notifications or start ads/billing. */
@RunWith(AndroidJUnit4::class)
class IslandLayoutInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    @Before fun isolateFixtureFromLiveOverlay() {
        // Pointer injection must reach the synthetic fixture, never a live ad or
        // purchase CTA that happens to occupy the same screen coordinates.
        ai.emots.kishan_dynamic.service.PermissionUtils.isAwaitingAccessibilityReturn = false
        ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(true)
    }

    @After fun restoreLiveOverlay() {
        ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(false)
    }

    private data class Scenario(val width: Int = 393, val font: Float = 1f,
        val state: IslandDemoState = IslandDemoState.PremiumExpiryExpanded, val rtl: Boolean = false)

    @Test fun expiryAndCountFitAllWidthsAndFontSizesWithWorkingActions() {
        val scenario = mutableStateOf(Scenario())
        val actions = mutableListOf<IslandControlAction>()
        val notification = NotificationInfo("fixture", "test.app", "Messages", "A long fixture title", "A long fixture message")
        compose.setContent {
            val current = scenario.value
            val config = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = current.width; screenHeightDp = 850
            }
            CompositionLocalProvider(
                LocalConfiguration provides config, LocalDensity provides Density(1f, current.font),
                LocalLayoutDirection provides if (current.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalReducedMotion provides true
            ) {
                AppTheme(darkTheme = true) {
                    Column(Modifier.requiredWidth(current.width.dp).background(Color(0xFF243044))
                        .padding(vertical = 16.dp).testTag("fixture")) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            DynamicIslandPill(state = current.state, premiumHoursRemaining = 0,
                                notifications = listOf(notification), unseenNotificationCount = 8,
                                onControlAction = { actions += it })
                        }
                    }
                }
            }
        }
        for (width in listOf(320, 360, 393, 430)) for (font in listOf(1f, 2f)) for (rtl in listOf(false, true)) {
            compose.runOnIdle { scenario.value = Scenario(width, font, rtl = rtl) }
            compose.waitForIdle()
            val surface = compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            val watch = compose.onNodeWithText("Watch Ads").fetchSemanticsNode().boundsInRoot
            val buy = compose.onNodeWithText("Buy Premium").fetchSemanticsNode().boundsInRoot
            val title = compose.onNodeWithText("Aurora Pro access", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue("shell respects window gutters", surface.width <= width - 21f)
            if (font == 1f) assertTrue("content must not create a tall access card", surface.height <= 160f)
            assertTrue("title above actions: width=$width font=$font rtl=$rtl title=$title watch=$watch surface=$surface", title.bottom <= watch.top)
            assertTrue("actions inside surface", watch.bottom <= surface.bottom && buy.bottom <= surface.bottom)
            assertTrue("no overlap", watch.right <= buy.left || buy.right <= watch.left || watch.bottom <= buy.top || buy.bottom <= watch.top)
            // Text bounds are deliberately smaller than their enclosing 50dp buttons;
            // inspect the button semantics for actual touch targets.
            val buttons = compose.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes()
            assertTrue(buttons.size >= 2)
            buttons.forEach { assertTrue("48px target at density=1", it.boundsInRoot.height >= 48f) }
            if (!rtl && font == 1f) save("expiry_${width}")
            if (rtl && font == 2f && width == 320) save("expiry_320_200pct_rtl")
            val actionCountBefore = actions.size
            compose.onNodeWithText("Watch Ads").performClick()
            compose.onNodeWithText("Buy Premium").performClick()
            assertEquals("both fixture callbacks must run this iteration", actionCountBefore + 2, actions.size)
            assertEquals(listOf(IslandControlAction.PremiumWatchAd, IslandControlAction.PremiumBuy), actions.takeLast(2))
            compose.runOnIdle { scenario.value = Scenario(width, font, IslandDemoState.NotificationStacked, rtl) }
            compose.waitForIdle()
            val compact = compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue("compact count must not retain a 152dp reference width", compact.width < 100f)
            assertTrue("count remains inside compact shell", compose.onNodeWithText("8").fetchSemanticsNode().boundsInRoot.right <= compact.right)
            if (!rtl && font == 1f && width == 393) save("compact_count_393")
        }
        // Same measured shell for core activity families, including 200% type.
        for (state in listOf(IslandDemoState.MusicExpanded, IslandDemoState.CallExpanded,
            IslandDemoState.TimerExpanded, IslandDemoState.NotificationExpanded,
            IslandDemoState.RingerModeExpanded, IslandDemoState.ActionControlExpanded)) {
            for (width in listOf(320, 393, 430)) for (font in listOf(1f, 2f)) {
                compose.runOnIdle { scenario.value = Scenario(width = width, font = font, state = state) }
                compose.waitForIdle()
                val bounds = compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                assertTrue("$state has content", bounds.height >= 48f)
                assertTrue("$state fits viewport", bounds.height < 850f)
                if (font == 1f && width == 393) save("family_${state.name}")
                if (font == 2f && width == 320) save("family_${state.name}_320_200pct")
                if (state == IslandDemoState.MusicExpanded) {
                    val buttons = compose.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes()
                    buttons.forEach { assertTrue("media target stays 48dp", it.boundsInRoot.height >= 48f) }
                    val sorted = buttons.map { it.boundsInRoot }.sortedBy { it.left }
                    sorted.zipWithNext().forEach { (left, right) -> assertTrue("media buttons cannot overlap", left.right <= right.left) }
                }
            }
        }
    }

    private fun save(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.filesDir, "island-layout-qa").apply { mkdirs() }
        File(folder, "$name.png").outputStream().use {
            compose.onNodeWithTag("fixture").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
