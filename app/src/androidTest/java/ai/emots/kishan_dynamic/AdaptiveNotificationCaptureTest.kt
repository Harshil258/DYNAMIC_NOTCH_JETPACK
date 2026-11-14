package ai.emots.kishan_dynamic

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.playground.FigmaReferenceFixtures
import ai.emots.kishan_dynamic.playground.RealWorldNotificationFixtures
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Device evidence for the universal adaptive notification renderer: every
 * gallery fixture mode rendered through the production DynamicIslandPill path,
 * with the 86dp family floor asserted from measured island bounds.
 */
@RunWith(AndroidJUnit4::class)
class AdaptiveNotificationCaptureTest {
    @get:Rule val compose = createComposeRule()

    private val baseDir = File(
        InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
        "visual-redesign/06-adaptive-realworld"
    ).apply { mkdirs() }

    @Before
    fun setup() {
        ai.emots.kishan_dynamic.service.PermissionUtils.isAwaitingAccessibilityReturn = false
        IslandStateManager.setPaywallActive(true)
    }

    @After
    fun tearDown() {
        IslandStateManager.setPaywallActive(false)
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String): File {
        val file = File(baseDir, filename)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return file
    }

    private fun cropWithPadding(full: Bitmap, bounds: androidx.compose.ui.geometry.Rect, paddingPx: Int = 24): Bitmap {
        val left = (bounds.left.toInt() - paddingPx).coerceIn(0, full.width - 1)
        val top = (bounds.top.toInt() - paddingPx).coerceIn(0, full.height - 1)
        val right = (bounds.right.toInt() + paddingPx).coerceIn(left + 1, full.width)
        val bottom = (bounds.bottom.toInt() + paddingPx).coerceIn(top + 1, full.height)
        return Bitmap.createBitmap(full, left, top, right - left, bottom - top)
    }

    @Test
    fun captureRealWorldModes_heightContract() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val density = context.resources.displayMetrics.density
        val notifications = mutableStateOf<List<NotificationInfo>>(emptyList())

        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AppTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .testTag("capture_root"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            state = IslandDemoState.NotificationExpanded,
                            notifications = notifications.value,
                            notificationActiveIndex = 0,
                            unseenNotificationCount = notifications.value.size,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        fun settle() {
            compose.mainClock.advanceTimeBy(650)
            compose.waitForIdle()
        }

        fun islandBounds(): androidx.compose.ui.geometry.Rect =
            compose.onNodeWithTag("island_surface", useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot

        fun capture(name: String): JSONObject {
            val bounds = islandBounds()
            val full = compose.onRoot().captureToImage().asAndroidBitmap()
            val fullFile = saveBitmap(full, "$name-full.png")
            val cropFile = saveBitmap(cropWithPadding(full, bounds), "$name-crop.png")
            return JSONObject().apply {
                put("mode", name)
                put("width_dp", bounds.width / density)
                put("height_dp", bounds.height / density)
                put("width_px", bounds.width.toInt())
                put("height_px", bounds.height.toInt())
                put("full_file", fullFile.name)
                put("crop_file", cropFile.name)
            }
        }

        val referenceMessages = FigmaReferenceFixtures.messages(context)
        val pictureUri = FigmaReferenceFixtures.resourceUri(context, ai.emots.kishan_dynamic.R.drawable.figma_reference_message)
        val modes = buildList {
            add("00-figma-thumbnail" to referenceMessages)
            add("01-short-text" to listOf(
                NotificationInfo("short-1", FigmaReferenceFixtures.packageName, "Figma", "Sarah", "Meeting at 3pm", timestamp = 1)
            ))
            add("02-multi-line" to listOf(
                NotificationInfo("multi-1", FigmaReferenceFixtures.packageName, "Figma", "Mamad",
                    "Haji ina chie zadi\nto baz dahane mano\nservis kardi", timestamp = 1)
            ))
            add("03-conversation-media" to listOf(
                referenceMessages[1].copy(picturePath = pictureUri, text = "Photo shared in design-critique")
            ))
            RealWorldNotificationFixtures.modeLabels.forEachIndexed { mode, label ->
                if (mode < 4) return@forEachIndexed
                val slug = label.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                add("%02d-%s".format(mode, slug) to listOf(RealWorldNotificationFixtures.notification(mode, 0, context)))
            }
        }

        val failures = mutableListOf<String>()
        val measurements = JSONArray()

        modes.forEach { (name, single) ->
            compose.runOnIdle { notifications.value = single }
            settle()
            val measured = capture(name)
            measurements.put(measured)
            val heightDp = measured.getDouble("height_dp")

            if (heightDp < 85.5f) failures += "$name: measured ${heightDp}dp is below the 86dp family floor"
            if (name.startsWith("01-") && kotlin.math.abs(heightDp - 86f) > 1.5f) {
                failures += "$name: short text must settle at 86dp, measured ${heightDp}dp"
            }
            if (name.startsWith("00-") && heightDp < 125f) {
                failures += "$name: Figma thumbnail must be at least 126dp, measured ${heightDp}dp"
            }
            if (name.startsWith("10-") && heightDp < 126f) {
                failures += "$name: big picture must be at least 126dp, measured ${heightDp}dp"
            }

            val queue = if (name.startsWith("00-")) referenceMessages
            else if (name.startsWith("01-") || name.startsWith("02-") || name.startsWith("03-")) single
            else {
                val mode = name.substringBefore('-').toInt()
                (0 until 3).map { RealWorldNotificationFixtures.notification(mode, it, context) }
            }
            if (queue.size > 1) {
                compose.runOnIdle { notifications.value = queue }
                settle()
                val queueMeasured = capture("$name-queue3")
                measurements.put(queueMeasured)
                if (queueMeasured.getDouble("height_dp") < 85.5f) {
                    failures += "$name-queue3: measured height below the 86dp family floor"
                }
            }
        }

        File(baseDir, "measurements.json").writeText(measurements.toString(2))
        assertTrue(
            "Adaptive height contract violations:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }
}