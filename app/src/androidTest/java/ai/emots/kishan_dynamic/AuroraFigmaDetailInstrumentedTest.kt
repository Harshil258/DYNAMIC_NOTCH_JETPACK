package ai.emots.kishan_dynamic

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.data.notification.ReplyDraftStore
import ai.emots.kishan_dynamic.playground.FigmaReferenceFixtures
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

/** Paths explored with directADB: vertical swipe advances source identity without shell dismissal. */
@RunWith(AndroidJUnit4::class)
class AuroraFigmaDetailInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private var previousSuppression = false
    @Before fun isolate() { previousSuppression = IslandStateManager.isPaywallActive.value; IslandStateManager.setPaywallActive(true) }
    @After fun restore() { IslandStateManager.setPaywallActive(previousSuppression) }
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Composable private fun Fixture(rtl: Boolean = false, content: @Composable () -> Unit) {
        val config = Configuration(LocalConfiguration.current).apply { screenWidthDp = 393; screenHeightDp = 850 }
        CompositionLocalProvider(LocalConfiguration provides config, LocalDensity provides Density(2f, 1f),
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalReducedMotion provides true,
            LocalIslandFixtureIcons provides mapOf(FigmaReferenceFixtures.packageName to R.drawable.figma_reference_app)) {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(393.dp).height(850.dp).background(Color(0xFF0B0F19)), contentAlignment = Alignment.TopCenter) { content() }
            }
        }
    }

    @Test fun verticalPagingPreservesIdentityAcrossArrivalUpdateAndRemovalInBothDirections() {
        var sources by mutableStateOf(FigmaReferenceFixtures.messages(context))
        var selectedKey by mutableStateOf("figma-selected")
        var rtl by mutableStateOf(false)
        var dismissals = 0
        compose.setContent {
            Fixture(rtl) {
                DynamicIslandPill(IslandDemoState.NotificationExpanded, notifications = sources,
                    notificationActiveIndex = sources.indexOfFirst { it.id == selectedKey }.coerceAtLeast(0),
                    onNotificationSelect = { key -> selectedKey = key },
                    onSwipeDismiss = { dismissals++ })
            }
        }
        for (direction in listOf(false, true)) {
            compose.runOnIdle { rtl = direction; sources = FigmaReferenceFixtures.messages(context); selectedKey = "figma-selected" }
            compose.waitForIdle()
            compose.onNodeWithTag("notification_vertical_pager").performTouchInput {
                if (!direction) swipeLeft(durationMillis = 450) else swipeRight(durationMillis = 450)
            }
            compose.waitForIdle()
            compose.waitUntil(5000) { selectedKey == "figma-next" }
            compose.onNodeWithTag("notification_vertical_pager").performTouchInput {
                if (!direction) swipeRight(durationMillis = 450) else swipeLeft(durationMillis = 450)
            }
            compose.waitUntil(5000) { selectedKey == "figma-selected" }
            compose.runOnIdle { sources = listOf(sources.first().copy(id = "new-arrival", title = "New arrival")) + sources }
            compose.waitForIdle()
            Assert.assertEquals("arrival must preserve source identity", "figma-selected", selectedKey)
            compose.onNodeWithText("Mamad").assertIsDisplayed()
            compose.runOnIdle { sources = sources.map { if (it.id == selectedKey) it.copy(title = "Updated selected source") else it } }
            compose.onNodeWithText("Updated selected source").assertIsDisplayed()
            compose.runOnIdle { sources = sources.filterNot { it.id == selectedKey }; selectedKey = "figma-next" }
            compose.waitForIdle()
            compose.onNodeWithText("Project update").assertIsDisplayed()
            Assert.assertEquals("page gesture must not dismiss the main island", 0, dismissals)
        }
    }

    @Test fun replyEditorOwnsVerticalGesturesAndRetainsDraftAcrossNewArrival() {
        val action = NotificationActionInfo("source-reply", "Reply", isReply = true)
        var sources by mutableStateOf(FigmaReferenceFixtures.messages(context).map { it.copy(actions = listOf(action)) })
        val drafts = ReplyDraftStore()
        var requestedPage: String? = null
        compose.setContent {
            Fixture {
                CompositionLocalProvider(LocalIslandReplyDraftStore provides drafts) {
                    DynamicIslandPill(IslandDemoState.NotificationExpanded, notifications = sources,
                        notificationActiveIndex = sources.indexOfFirst { it.id == "figma-selected" },
                        replyingNotificationId = "figma-selected", replyAction = action,
                        onNotificationSelect = { requestedPage = it })
                }
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("source-only draft")
        compose.onNodeWithTag("notification_vertical_pager").performTouchInput { swipeLeft(durationMillis = 450) }
        compose.runOnIdle { sources = listOf(sources.first().copy(id = "arrival")) + sources }
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("source-only draft")))
        Assert.assertNull("reply ownership must suppress page changes", requestedPage)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Test fun captureSameContentReferenceFamiliesAndExactGlyphInventory() {
        var state by mutableStateOf(IslandDemoState.NotificationExpanded)
        var glyphSheet by mutableStateOf(false)
        val sources = FigmaReferenceFixtures.messages(context)
        val glyphs = AppleGlyph.entries.filter { it.vector.name.startsWith("figma_") }
        val folder = File(context.getExternalFilesDir(null), "aurora-figma-detail").apply { mkdirs() }
        compose.setContent {
            Fixture {
                if (glyphSheet) FlowRow(Modifier.width(393.dp).testTag("figma_glyph_sheet"), maxItemsInEachRow = 6) {
                    glyphs.forEach { glyph ->
                        Column(Modifier.width(64.dp).height(76.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { AppleIcon(glyph, size = glyph.figmaInkSize()) }
                            Text(glyph.name, color = Color.White, fontSize = 8.sp, maxLines = 2)
                        }
                    }
                } else DynamicIslandPill(state, notifications = sources, notificationActiveIndex = 1, unseenNotificationCount = 3,
                    musicTrack = FigmaReferenceFixtures.music(context), musicIsPlaying = false, timerLabel = "3:35",
                    callContact = FigmaReferenceFixtures.caller(context))
            }
        }
        val measurements = StringBuilder("state,width,height\n")
        for (target in listOf(IslandDemoState.NotificationExpanded, IslandDemoState.NotificationImage, IslandDemoState.NotificationCompact,
            IslandDemoState.MusicCompact, IslandDemoState.MusicExpanded, IslandDemoState.CallExpanded,
            IslandDemoState.FaceTimeCallExpanded, IslandDemoState.SharedMediaCallExpanded, IslandDemoState.SilentModeExpanded, IslandDemoState.TimerExpanded, IslandDemoState.CallAvatars)) {
            compose.runOnIdle { state = target }
            compose.waitForIdle()
            if (target in listOf(IslandDemoState.NotificationExpanded, IslandDemoState.NotificationImage)) {
                compose.waitUntil(10000) { compose.onAllNodesWithTag("notification_artwork_loaded", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
            }
            if (target in listOf(IslandDemoState.CallExpanded, IslandDemoState.FaceTimeCallExpanded)) {
                compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Tamia Castillo", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
            }
            compose.waitForIdle()
            val node = compose.onNodeWithTag("island_surface", useUnmergedTree = true)
            val bounds = node.fetchSemanticsNode().boundsInRoot
            measurements.append("$target,${bounds.width/2},${bounds.height/2}\n")
            File(folder, "$target.png").outputStream().use { node.captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        File(folder, "measurements.csv").writeText(measurements.toString())
        Assert.assertEquals("all directly exported glyphs must be reachable", 37, glyphs.size)
        compose.runOnIdle { glyphSheet = true }
        compose.waitForIdle()
        File(folder, "exact-glyphs.png").outputStream().use { compose.onNodeWithTag("figma_glyph_sheet").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
