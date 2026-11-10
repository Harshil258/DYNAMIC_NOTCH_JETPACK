package ai.emots.kishan_dynamic

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.*
import java.io.File

/** Regression for the actual320dp/200% frame that ellipsized Tamia Castillo beside02:05. */
class CallSummaryLayoutInstrumentedTest {
    @get:Rule val compose=createComposeRule()
    private var previousSuppression=false
    @Before fun isolate() { previousSuppression=IslandStateManager.isPaywallActive.value;IslandStateManager.setPaywallActive(true) }
    @After fun restore() { IslandStateManager.setPaywallActive(previousSuppression) }
    private val record=CallRecord("layout-fixture","Tamia Castillo and a long contact identity for accessibility",
        "+15550192834",startedAtMillis=0L,durationSeconds=125L,direction=CallDirection.INCOMING)

    private fun render(value: CallRecord, controls: Set<IslandControlAction>?=null) {
        compose.setContent {
            val config=Configuration(LocalConfiguration.current).apply { screenWidthDp=320;screenHeightDp=850 }
            CompositionLocalProvider(LocalConfiguration provides config,LocalDensity provides Density(2f,2f),LocalReducedMotion provides true) {
                AppTheme(darkTheme=true) {
                    Box(Modifier.width(320.dp).height(850.dp)) {
                        DynamicIslandPill(IslandDemoState.CallSummaryExpanded,callSummary=value,availableControlActions=controls)
                    }
                }
            }
        }
        compose.waitForIdle()
    }
    private fun capture(name: String) {
        val i=InstrumentationRegistry.getInstrumentation()
        val image=i.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val file=File(i.targetContext.getExternalFilesDir(null),"aurora-regressions/$name.png").apply { parentFile?.mkdirs() }
        file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG,100,it)) };image.recycle()
    }
    @Test fun callerAndDurationRemainCompleteAtLargeType() {
        render(record)
        val layouts=mutableListOf<TextLayoutResult>()
        val name=compose.onNodeWithText(record.contactName,useUnmergedTree=true)
        name.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { Assert.assertTrue(it(layouts)) }
        val layout=layouts.single()
        Assert.assertTrue(layout.lineCount>1)
        Assert.assertFalse(layout.hasVisualOverflow)
        for (line in 0 until layout.lineCount) Assert.assertFalse(layout.isLineEllipsized(line))
        val duration=compose.onNodeWithText("02:05",useUnmergedTree=true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        Assert.assertTrue(duration.top>=name.fetchSemanticsNode().boundsInRoot.bottom)
        compose.onNodeWithText("Done").assertIsDisplayed()
        capture("call-summary-long-320-200")
    }
    @Test fun unavailableDurationAndNumberAreRepresentedHonestly() {
        render(record.copy(phoneNumber="",durationSeconds=0L,durationAccuracy=DurationAccuracy.UNKNOWN))
        compose.onNodeWithText("Duration unavailable").assertIsDisplayed()
        compose.onNodeWithText("00:00").assertDoesNotExist()
        for (label in listOf("Redial","SMS","WhatsApp")) compose.onNodeWithText(label).assertDoesNotExist()
        compose.onNodeWithText("Done").assertIsDisplayed()
        capture("call-summary-unknown-320-200")
    }
    @Test fun missingSourceCapabilitiesKeepDismissalAvailable() {
        render(record,emptySet())
        for (label in listOf("Redial","SMS","WhatsApp")) compose.onNodeWithText(label).assertDoesNotExist()
        compose.onNodeWithText("Done").assertIsDisplayed()
    }
}
