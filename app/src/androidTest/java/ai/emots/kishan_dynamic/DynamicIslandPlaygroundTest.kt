package ai.emots.kishan_dynamic

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.emots.kishan_dynamic.playground.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@org.junit.runner.RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class DynamicIslandPlaygroundTest {
    @get:Rule val compose = createAndroidComposeRule<DynamicIslandPlaygroundActivity>()

    @Test fun debugWorkbenchUsesTheActualRendererForEveryMockFamily() {
        val controller = PlaygroundSession.controller
        for (case in PlaygroundCase.entries) {
            compose.runOnIdle { controller.select(case) }
            compose.waitForIdle()
            compose.onNodeWithTag("playground_preview").assertExists()
            if (case != PlaygroundCase.Hidden) compose.onNodeWithTag("island_surface", useUnmergedTree = true).assertExists()
        }
        compose.runOnIdle {
            controller.select(PlaygroundCase.Access)
            controller.options { it.copy(fontScale = 2f, rtl = true, motionOverride = true,
                motion = it.motion.copy(enabled = false)) }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Aurora Pro", useUnmergedTree = true).assertExists()
        compose.runOnIdle { controller.runChecks() }
        assertTrue(controller.checks.value.all { it.passed })
    }
    @Test fun geometryAndMotionControlsAffectTheProductionSurface() {
        val controller = PlaygroundSession.controller
        compose.runOnIdle {
            controller.options { PlaygroundOptions(guides = false, reducedMotion = true) }
            controller.select(PlaygroundCase.Compact)
        }
        compose.waitForIdle()
        val before = compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.width
        compose.runOnIdle { controller.options { it.copy(minimumWidth = 240f) } }
        compose.waitForIdle()
        val after = compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.width
        assertTrue("geometry overrides must change the real surface", after > before)
        compose.runOnIdle { controller.burst() }
        compose.waitUntil(10_000) { controller.snapshot.value.eventCount >= 30 && !controller.snapshot.value.sequenceRunning }
        assertTrue(controller.snapshot.value.feedback.contains("30 / 30"))
    }
    @Test fun workbenchTabsAndAccessibleKnobsAreInteractive() {
        val controller = PlaygroundSession.controller
        compose.runOnIdle { controller.options { PlaygroundOptions() }; controller.select(PlaygroundCase.Compact) }
        compose.onNodeWithText("Animations", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithTag("lab_slider_Damping ratio").performScrollTo()
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(.95f) }
        compose.runOnIdle { assertEquals(.95f, controller.snapshot.value.options.motion.damping, .01f) }
        compose.onNodeWithTag("playground_primary").performClick()
        compose.waitUntil(5_000) { !controller.snapshot.value.sequenceRunning }
        compose.onNodeWithText("States", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithTag("case_Access").performScrollTo().performClick()
        compose.onNodeWithText("Aurora Pro", useUnmergedTree = true).assertExists()
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val folder = java.io.File(context.filesDir, "island-playground-qa").apply { mkdirs() }
        java.io.File(folder, "access-preview.png").outputStream().use { output ->
            compose.onNodeWithTag("playground_preview").captureToImage().let {
                it.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
            }
        }
    }

}
