package ai.emots.kishan_dynamic

import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.playground.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LayeredMiniIslandsAnimationBenchmarkTest {
    @get:Rule val compose = createAndroidComposeRule<DynamicIslandPlaygroundActivity>()

    @Test fun benchmarkAnimationPerformanceUnderRepeatedTransitions() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val controller = PlaygroundSession.controller

        // Enable real animations with standard speed
        compose.runOnIdle {
            controller.options {
                it.copy(
                    reducedMotion = false,
                    motionOverride = false,
                    guides = false
                )
            }
        }
        compose.waitForIdle()

        // Reset gfxinfo counters before profiling
        runShell(automation, "dumpsys gfxinfo com.island.dynamicnotch reset")
        SystemClock.sleep(200)

        // Run 10 intensive cycles of expansion, pager navigation, child emergence, reorder, promotion, and collapse
        for (cycle in 1..10) {
            // 1. Load multi-notification pager (12 items)
            compose.runOnIdle { controller.loadNotificationScenario(12) }
            compose.waitForIdle()
            SystemClock.sleep(120)

            // 2. Horizontal pager navigation (3 swipes)
            for (p in 1..3) {
                compose.runOnIdle {
                    controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.NextPage)
                }
                compose.waitForIdle()
                SystemClock.sleep(80)
            }

            // 3. Sequential child arrivals (emergence and reordering)
            for (a in 1..3) {
                compose.runOnIdle { controller.addLayeredArrival() }
                compose.waitForIdle()
                SystemClock.sleep(80)
            }

            // 4. Touch and promote a child notice
            val childKey = controller.engineState.childNotices.firstOrNull()?.key
            if (childKey != null) {
                compose.runOnIdle { controller.promoteChild(childKey) }
                compose.waitForIdle()
                SystemClock.sleep(150)
            }

            // 5. Swipe-up collapse
            compose.runOnIdle { controller.expand(false) }
            compose.waitForIdle()
            SystemClock.sleep(120)

            // 6. Re-expand
            compose.runOnIdle { controller.expand(true) }
            compose.waitForIdle()
            SystemClock.sleep(120)
        }

        // Allow final frame buffer to flush
        compose.waitForIdle()
        SystemClock.sleep(300)

        // Capture full dumpsys gfxinfo
        val gfxOutput = runShell(automation, "dumpsys gfxinfo com.island.dynamicnotch")
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(target.filesDir, "task07-evidence").apply { mkdirs() }
        File(folder, "animation-benchmark.txt").writeText(gfxOutput)

        assertTrue(gfxOutput.contains("Total frames rendered"))
    }

    private fun runShell(automation: android.app.UiAutomation, cmd: String): String {
        return automation.executeShellCommand(cmd).use { pfd ->
            java.io.FileInputStream(pfd.fileDescriptor).bufferedReader().readText()
        }
    }
}
