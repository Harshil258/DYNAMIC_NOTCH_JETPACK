package ai.emots.kishan_dynamic

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.playground.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LayeredMiniIslandsAnimationBenchmarkTest {

    @Test fun benchmarkAnimationPerformanceUnderRepeatedTransitions() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation

        // Launch activity via ActivityScenario (preserves real hardware Choreographer without TestMonotonicFrameClock interception)
        val scenario = ActivityScenario.launch(DynamicIslandPlaygroundActivity::class.java)
        val controller = PlaygroundSession.controller

        // Allow initial activity layout to warm up
        SystemClock.sleep(1000)

        // Reset gfxinfo counters before profiling
        runShell(automation, "dumpsys gfxinfo com.island.dynamicnotch reset")
        SystemClock.sleep(250)

        // Launch the continuous 12-cycle animation benchmark on the app's main looper
        val latch = CountDownLatch(1)
        scenario.onActivity {
            controller.runLayeredAnimationBenchmark(cycles = 12) {
                latch.countDown()
            }
        }

        // Wait for the benchmark to execute all cycles (approx 32 seconds)
        val completed = latch.await(60, TimeUnit.SECONDS)
        assertTrue("Benchmark timed out before completing all cycles", completed)

        // Allow final frame buffer to flush
        SystemClock.sleep(500)

        // Capture full dumpsys gfxinfo and framestats
        val gfxOutput = runShell(automation, "dumpsys gfxinfo com.island.dynamicnotch")
        val framestatsOutput = runShell(automation, "dumpsys gfxinfo com.island.dynamicnotch framestats")

        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(target.filesDir, "task08-evidence").apply { mkdirs() }
        File(folder, "animation-benchmark.txt").writeText(gfxOutput)
        File(folder, "animation-framestats.txt").writeText(framestatsOutput)

        // Parse total frames and verify representative sample >= 500 frames
        val framesRegex = Regex("Total frames rendered:\\s*(\\d+)")
        val match = framesRegex.find(gfxOutput)
        assertNotNull("Could not find 'Total frames rendered' in gfxinfo output", match)
        val totalFrames = match!!.groupValues[1].toInt()

        assertTrue(
            "Benchmark rendered $totalFrames frames; required at least 500 frames for a representative sample",
            totalFrames >= 500
        )

        scenario.close()
    }

    private fun runShell(automation: android.app.UiAutomation, cmd: String): String {
        return automation.executeShellCommand(cmd).use { pfd ->
            java.io.FileInputStream(pfd.fileDescriptor).bufferedReader().readText()
        }
    }
}
