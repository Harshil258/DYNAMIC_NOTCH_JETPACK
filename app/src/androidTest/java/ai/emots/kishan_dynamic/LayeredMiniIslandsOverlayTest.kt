package ai.emots.kishan_dynamic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.provider.Settings
import android.view.InputDevice
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.playground.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Plain Android instrumentation: the service window must use the real frame clock. */
@RunWith(AndroidJUnit4::class)
class LayeredMiniIslandsOverlayTest {
    @get:Rule val activity = ActivityScenarioRule(DynamicIslandPlaygroundActivity::class.java)
    @After fun stop() { activity.scenario.onActivity { it.stopService(Intent(it, PlaygroundOverlayService::class.java)) } }
    private fun until(timeoutMs: Long = 10000, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return
            SystemClock.sleep(25)
        }
        fail("Physical state: ${PlaygroundSession.physicalInfo.value}; running=${PlaygroundSession.overlayRunning.value}; pending=${PlaygroundSession.controller.snapshot.value.behavior.pendingChildNoticesCount}; phases=${PlaygroundSession.controller.snapshot.value.behavior.childLifecycles.values}")
    }
    @Test fun physicalMockWindowWrapsStackAndOutsideStopButtonReceivesTouch() {
        val controller = PlaygroundSession.controller
        activity.scenario.onActivity {
            Assume.assumeTrue("Overlay permission required on authorized device", Settings.canDrawOverlays(it))
            controller.options { PlaygroundOptions(guides = false) }
            controller.layeredScenario(3)
            ContextCompat.startForegroundService(it, Intent(it, PlaygroundOverlayService::class.java))
        }
        until {
            PlaygroundSession.overlayRunning.value && PlaygroundSession.physicalInfo.value.contains("unlocked") &&
                controller.snapshot.value.behavior.childLifecycles.values.count { it == ai.emots.kishan_dynamic.data.notification.ChildLifecycle.VISIBLE } >= 2 &&
                Regex("window [1-9][0-9]*×[1-9][0-9]*px").containsMatchIn(PlaygroundSession.physicalInfo.value)
        }
        val info = PlaygroundSession.physicalInfo.value
        val match = Regex("window ([0-9]+)×([0-9]+)px").find(info)!!
        val height = match.groupValues[2].toInt()
        activity.scenario.onActivity { assertTrue("Not a full-screen catcher: $info", height < it.resources.displayMetrics.heightPixels * .8f) }
        assertEquals(4, controller.snapshot.value.behavior.canonicalQueueCount)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = File(instrumentation.targetContext.filesDir, "task05-evidence").apply { mkdirs() }
        File(folder, "physical-window-info.txt").writeText(info)
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            File(folder, "native-mock-overlay.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        // Dynamically ground the owner-app stop action; no direct callback or made-up pass-through.
        val automation = instrumentation.uiAutomation
        val serviceInfo = automation.serviceInfo
        serviceInfo.flags = serviceInfo.flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        automation.serviceInfo = serviceInfo
        var bounds: Rect? = null
        val lookupDeadline = SystemClock.uptimeMillis() + 2000
        while (bounds == null && SystemClock.uptimeMillis() < lookupDeadline) {
            val roots = automation.windows.mapNotNull { it.root } + listOfNotNull(automation.rootInActiveWindow)
            val node = roots.flatMap { it.findAccessibilityNodeInfosByText("Stop physical overlay") }
                .firstOrNull { it.isVisibleToUser }
            node?.let { Rect().apply { it.getBoundsInScreen(this) } }?.takeIf { !it.isEmpty }?.let { bounds = it }
            if (bounds == null) SystemClock.sleep(25)
        }
        // Coordinate fallback verified by direct ADB exploration on the authorized Realme.
        // Scale the observed text bounds from 1080×2400 for equivalent display geometry.
        val screen = instrumentation.targetContext.getSystemService(android.view.WindowManager::class.java).maximumWindowMetrics.bounds
        val target = bounds ?: Rect((screen.width() * 364 / 1080), (screen.height() * 2135 / 2400),
            (screen.width() * 717 / 1080), (screen.height() * 2158 / 2400))
        assertTrue("Stop action must be outside the native overlay's height budget", target.centerY() > height + 200)
        val down = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEachIndexed { index, action ->
            MotionEvent.obtain(down, down + index * 40, action, target.exactCenterX(), target.exactCenterY(), 0).useEvent {
                it.source = InputDevice.SOURCE_TOUCHSCREEN
                assertTrue(automation.injectInputEvent(it, true))
            }
        }
        until(5000) { !PlaygroundSession.overlayRunning.value }
        assertEquals(4, controller.snapshot.value.behavior.canonicalQueueCount)
    }
    private inline fun MotionEvent.useEvent(block: (MotionEvent) -> Unit) { try { block(this) } finally { recycle() } }
}
