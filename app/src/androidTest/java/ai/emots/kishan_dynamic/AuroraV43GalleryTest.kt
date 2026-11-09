package ai.emots.kishan_dynamic

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

/** Real-device Compose fixtures, distinct from accessibility-overlay verification. */
@RunWith(AndroidJUnit4::class)
class AuroraV43GalleryTest {
    @get:Rule val compose = createComposeRule()
    private var previousSuppression = false
    @Before fun isolateProductionOverlay() {
        previousSuppression = ai.emots.kishan_dynamic.service.IslandStateManager.isPaywallActive.value
        ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(true)
    }
    @After fun restoreProductionOverlay() { ai.emots.kishan_dynamic.service.IslandStateManager.setPaywallActive(previousSuppression) }
    @Test fun captureEveryStateAndResponsiveBounds() {
        var selected by mutableStateOf(IslandDemoState.Idle)
        var width by mutableIntStateOf(393)
        var font by mutableFloatStateOf(1f)
        var rtl by mutableStateOf(false)
        val observer = ai.emots.kishan_dynamic.playground.PlaygroundGeometry()
        val folder=File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),"aurora-v43").apply { mkdirs() }
        val measurements=StringBuilder("state,viewport,font_scale,rtl,width,height,target_width,target_height,radius\n")
        compose.setContent {
            val config=Configuration(LocalConfiguration.current).apply {screenWidthDp=width;screenHeightDp=if(width>430) 360 else 850}
            CompositionLocalProvider(LocalConfiguration provides config,LocalDensity provides Density(if(width>430) 1.5f else 2f,font),
                LocalLayoutDirection provides if(rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalReducedMotion provides true,LocalIslandGeometryObserver provides observer) {
                AppTheme(darkTheme=true) {
                    Box(Modifier.width(width.dp).height(config.screenHeightDp.dp).background(Color(0xFF0B0F19)),contentAlignment=Alignment.TopCenter) {
                        DynamicIslandPill(selected, modifier = Modifier.testTag("v43_complete"),
                        batteryPercent = if (selected in setOf(IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded)) 20 else 85,
                        callParticipants = if (selected == IslandDemoState.CallAvatars) listOf(ContactInfo("Alex Morgan", ""), ContactInfo("Jordan Lee", "")) else emptyList(),
                        ringerMode = if (selected == IslandDemoState.RingerModeExpanded) RingerModeType.NORMAL else RingerModeType.SILENT,
                            notifications=listOf(NotificationInfo("demo-message","demo.aurora","Messages","Design review","The latest changes are ready for review.")),
                            musicTrack=MusicTrack("Evening light","Aurora studio",packageName="demo.aurora"),
                            callContact=ContactInfo("Alex Morgan",""),bluetoothDeviceName="Studio headphones",bluetoothBatteryPercent=75)
                    }
                }
            }
        }
        try {
            for (viewport in listOf(393,320,360,430,640)) for (scale in listOf(1f,1.3f,1.5f,2f)) {
                for (state in IslandDemoState.entries) {
                    compose.runOnIdle {selected=state;width=viewport;font=scale;rtl=scale==2f}
                    compose.waitUntil(10000) {observer.regions.values.any {it.first=="Shell" && it.second.width>0}}
                    compose.waitForIdle()
                    val fixtureDensity = if (viewport > 430) 1.5f else 2f
                    val bounds=compose.onNodeWithTag("island_surface",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
                    Assert.assertTrue("$state $viewport overflow",bounds.width/fixtureDensity <= viewport+1)
                    Assert.assertTrue("$state empty",bounds.height>0)
                    val target=IslandFamilyGeometry.resolve(state,viewport.toFloat())
                    if (state == IslandDemoState.MobileDataAlert && viewport == 393 && scale == 1f) {
                        for (label in listOf("OK", "Settings")) {
                            val action = compose.onNode(hasText(label) and hasClickAction()).fetchSemanticsNode()
                            Assert.assertTrue("$label action target is below 48dp", action.boundsInRoot.height >= 95f)
                        }
                    }
                    measurements.append("${state.name},$viewport,$scale,$rtl,${bounds.width/fixtureDensity},${bounds.height/fixtureDensity},${target.width},${target.height},${target.radius}\n")
                    if(viewport==393 && scale in listOf(1f,2f)) {
                        val bitmap=compose.onNodeWithTag(if (state == IslandDemoState.NotificationWithMusicCompact) "v43_complete" else "island_surface",useUnmergedTree=true).captureToImage().asAndroidBitmap()
                        if (state == IslandDemoState.Minimal && scale == 1f) {
                            for (y in 16 until bitmap.height - 16) for (x in 24 until bitmap.width - 24) {
                                val pixel = bitmap.getPixel(x, y)
                                Assert.assertTrue("Owner Minimal contains an unapproved glyph/dot", android.graphics.Color.red(pixel) > android.graphics.Color.green(pixel))
                            }
                        }
                        File(folder,"${state.name}-${if(scale==1f) "baseline" else "large-rtl"}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                    }
                }
            }
        } finally {File(folder,"measurements.csv").writeText(measurements.toString())}
    }
    @Test fun replyDraftSurvivesComposerRemovalAndNeverCrossesSourceIdentity() {
        var show by mutableStateOf(true)
        var source by mutableStateOf("draft-fixture-one")
        val store = ai.emots.kishan_dynamic.data.notification.ReplyDraftStore()
        store.privacyRestricted(false)
        store.clear("draft-fixture-one"); store.clear("draft-fixture-two")
        compose.setContent {
            CompositionLocalProvider(LocalIslandReplyDraftStore provides store) {
            AppTheme(darkTheme = true) {
                if (show) InlineReplyBox(notificationKey = source,
                    replyAction = NotificationActionInfo("reply-fixture", "message", isReply = true),
                    onSendReply = {}, onCancelReply = {})
            }
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("Unsent review draft")
        compose.runOnIdle { show = false }; compose.waitForIdle()
        compose.runOnIdle { show = true }; compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertTextContains("Unsent review draft")
        compose.runOnIdle { source = "draft-fixture-two" }; compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
        compose.runOnIdle { store.privacyRestricted(true); show = false }; compose.waitForIdle()
        compose.runOnIdle { store.privacyRestricted(false); source = "draft-fixture-one"; show = true }; compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
    }
    @Test fun unsupportedMediaAndCallActionsHaveNoTapTargets() {
        var state by mutableStateOf(IslandDemoState.MusicExpanded)
        var actions = 0
        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AppTheme(darkTheme = true) {
                    DynamicIslandPill(state, availableControlActions = emptySet(), onControlAction = { actions++ })
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Next track").assertDoesNotExist()
        compose.onNodeWithContentDescription("Previous track").assertDoesNotExist()
        compose.onNodeWithContentDescription("Pause playback").assertDoesNotExist()
        compose.runOnIdle { state = IslandDemoState.CallAvatars }; compose.waitForIdle()
        compose.onNodeWithContentDescription("Accept call").assertDoesNotExist()
        compose.onNodeWithContentDescription("Decline call").assertDoesNotExist()
        compose.runOnIdle { state = IslandDemoState.SilentModeExpanded }; compose.waitForIdle()
        compose.onNodeWithText("Unmute").assertDoesNotExist()
        compose.runOnIdle { Assert.assertEquals(0, actions) }
    }
    @Test fun mediaPlaybackStatusWaitsForSourceAcknowledgement() {
        var playing by mutableStateOf(true)
        var commands = 0
        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AppTheme(darkTheme = true) {
                    DynamicIslandPill(IslandDemoState.MusicExpanded, musicIsPlaying = playing,
                        onControlAction = { if (it == IslandControlAction.MusicPlayPause) commands++ })
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Pause playback").performClick()
        compose.runOnIdle { Assert.assertEquals(1, commands) }
        compose.onNodeWithContentDescription("Pause playback").assertExists()
        compose.runOnIdle { playing = false }; compose.waitForIdle()
        compose.onNodeWithContentDescription("Play playback").assertExists()
    }
}
