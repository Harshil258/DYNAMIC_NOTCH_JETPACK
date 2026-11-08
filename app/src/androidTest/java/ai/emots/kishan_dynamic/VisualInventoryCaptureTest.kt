package ai.emots.kishan_dynamic

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect as AndroidRect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.*
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class VisualInventoryCaptureTest {
    @get:Rule val compose = createComposeRule()

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val baseDir = File("/sdcard/Download/visual-redesign").apply { mkdirs() }
    private val measurementsList = mutableListOf<JSONObject>()

    @Before
    fun setup() {
        ai.emots.kishan_dynamic.service.PermissionUtils.isAwaitingAccessibilityReturn = false
        IslandStateManager.setPaywallActive(true)
    }

    @After
    fun tearDown() {
        IslandStateManager.setPaywallActive(false)
        saveMeasurements()
    }

    private fun saveMeasurements() {
        if (measurementsList.isNotEmpty()) {
            val measurementsDir = File(baseDir, "11-measurements").apply { mkdirs() }
            val jsonArr = JSONArray()
            measurementsList.forEach { jsonArr.put(it) }
            File(measurementsDir, "state-measurements.json").writeText(jsonArr.toString(2))

            // Also CSV format
            val csv = StringBuilder()
            csv.append("state,folder,evidence_tier,width_dp,height_dp,left_dp,top_dp,width_px,height_px,left_px,top_px,full_file,crop_file\n")
            measurementsList.forEach { obj ->
                csv.append("${obj.optString("state")},${obj.optString("folder")},${obj.optString("evidence_tier")},")
                csv.append("${obj.optDouble("width_dp")},${obj.optDouble("height_dp")},${obj.optDouble("left_dp")},${obj.optDouble("top_dp")},")
                csv.append("${obj.optInt("width_px")},${obj.optInt("height_px")},${obj.optInt("left_px")},${obj.optInt("top_px")},")
                csv.append("${obj.optString("full_file")},${obj.optString("crop_file")}\n")
            }
            File(measurementsDir, "island-dimensions.csv").writeText(csv.toString())
        }
    }

    private fun saveBitmap(bitmap: Bitmap, relativeSubdir: String, filename: String): File {
        val dir = File(baseDir, relativeSubdir).apply { mkdirs() }
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun cropWithPadding(fullBitmap: Bitmap, bounds: androidx.compose.ui.geometry.Rect, paddingPx: Int = 16): Bitmap {
        val left = (bounds.left.toInt() - paddingPx).coerceIn(0, fullBitmap.width - 1)
        val top = (bounds.top.toInt() - paddingPx).coerceIn(0, fullBitmap.height - 1)
        val right = (bounds.right.toInt() + paddingPx).coerceIn(left + 1, fullBitmap.width)
        val bottom = (bounds.bottom.toInt() + paddingPx).coerceIn(top + 1, fullBitmap.height)
        val w = right - left
        val h = bottom - top
        return Bitmap.createBitmap(fullBitmap, left, top, w, h)
    }

    private fun sampleNotifications(count: Int): List<NotificationInfo> {
        val apps = listOf(
            Triple("com.whatsapp", "WhatsApp", "Alex Rivera"),
            Triple("com.google.android.gm", "Gmail", "GitHub Notifications"),
            Triple("com.slack", "Slack", "Sarah Chen"),
            Triple("org.telegram.messenger", "Telegram", "Design Team"),
            Triple("com.twitter.android", "X", "Product Updates")
        )
        return (0 until count).map { i ->
            val app = apps[i % apps.size]
            NotificationInfo(
                id = "notif-$i",
                packageName = app.first,
                appName = app.second,
                title = if (i == 0) app.third else "${app.third} #$i",
                text = when (i % 5) {
                    0 -> "Hey! Check out the new Dynamic Island update."
                    1 -> "Build #428 succeeded on branch codex/task-05."
                    2 -> "Can we review the Figma components before noon?"
                    3 -> "New message in #android-team discussion."
                    else -> "Your weekly summary is now available."
                },
                timestamp = System.currentTimeMillis() - (i * 60_000L)
            )
        }
    }

    // =========================================================================
    // TEST 1: ALL 60 STATES OF IslandDemoState
    // =========================================================================
    @Test
    fun captureAll60IslandStates() {
        val currentState = mutableStateOf(IslandDemoState.Idle)
        val notifs = sampleNotifications(3)

        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalReducedMotion provides true
            ) {
                AppTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .testTag("root_container"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            state = currentState.value,
                            notifications = notifs,
                            unseenNotificationCount = notifs.size,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        IslandDemoState.entries.forEach { state ->
            compose.runOnIdle { currentState.value = state }
            compose.waitForIdle()

            val fullBitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            val surfaceNode = try {
                compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode()
            } catch (e: Throwable) {
                null
            }
            val bounds = surfaceNode?.boundsInRoot ?: androidx.compose.ui.geometry.Rect(0f, 0f, 1080f, 200f)

            // Determine directory category
            val folder = when (state) {
                IslandDemoState.NotificationExpanded,
                IslandDemoState.NotificationImage -> "06-expanded-notifications"

                IslandDemoState.MusicCompact,
                IslandDemoState.MusicExpanded,
                IslandDemoState.VoiceMemoExpanded,
                IslandDemoState.ScreenRecordingExpanded,
                IslandDemoState.VideoRemoteExpanded -> "07-media-and-audio"

                IslandDemoState.CallCompact,
                IslandDemoState.CallExpanded,
                IslandDemoState.CallSummaryExpanded,
                IslandDemoState.FaceTimeCallExpanded,
                IslandDemoState.SharedMediaCallExpanded,
                IslandDemoState.OngoingCallBanner,
                IslandDemoState.CallAvatars,
                IslandDemoState.TimerCompact,
                IslandDemoState.TimerExpanded,
                IslandDemoState.TimerImage,
                IslandDemoState.DeliveryCompact,
                IslandDemoState.DeliveryExpanded,
                IslandDemoState.FlightCompact,
                IslandDemoState.FlightExpanded,
                IslandDemoState.FlightTrackerExpanded,
                IslandDemoState.SportsCompact,
                IslandDemoState.SportsExpanded,
                IslandDemoState.NavigationCompact,
                IslandDemoState.TransportActivity,
                IslandDemoState.MovieCard -> "08-telephony-and-timers"

                IslandDemoState.Minimal,
                IslandDemoState.NotificationCompact,
                IslandDemoState.NotificationStacked,
                IslandDemoState.NotificationWithMusicCompact -> "03-compact-and-minimal"

                else -> "02-idle-and-system"
            }

            val stateName = state.name
            val fullFile = saveBitmap(fullBitmap, folder, "$stateName-full.png")
            val croppedBitmap = cropWithPadding(fullBitmap, bounds, paddingPx = 24)
            val cropFile = saveBitmap(croppedBitmap, folder, "$stateName-crop.png")

            // If it's a compact or minimal state, also save a copy to 03-compact-and-minimal for index completeness
            if (state in listOf(IslandDemoState.MusicCompact, IslandDemoState.CallCompact, IslandDemoState.TimerCompact,
                    IslandDemoState.DeliveryCompact, IslandDemoState.FlightCompact, IslandDemoState.SportsCompact,
                    IslandDemoState.NavigationCompact, IslandDemoState.ChargingCompact, IslandDemoState.SilentModeCompact,
                    IslandDemoState.VibrateModeCompact, IslandDemoState.NormalModeCompact, IslandDemoState.RingerVolumeCompact,
                    IslandDemoState.MediaVolumeCompact, IslandDemoState.BluetoothConnected, IslandDemoState.BluetoothConnecting,
                    IslandDemoState.LowBatteryCompact)) {
                saveBitmap(fullBitmap, "03-compact-and-minimal", "$stateName-full.png")
                saveBitmap(croppedBitmap, "03-compact-and-minimal", "$stateName-crop.png")
            }

            // Measurements
            val density = 3.0f // Realme 8 physical density factor
            val obj = JSONObject().apply {
                put("state", stateName)
                put("folder", folder)
                put("evidence_tier", "DEVICE_COMPOSE")
                put("full_file", fullFile.relativeTo(baseDir).path)
                put("crop_file", cropFile.relativeTo(baseDir).path)
                put("left_px", bounds.left.toInt())
                put("top_px", bounds.top.toInt())
                put("width_px", bounds.width.toInt())
                put("height_px", bounds.height.toInt())
                put("left_dp", bounds.left / density)
                put("top_dp", bounds.top / density)
                put("width_dp", bounds.width / density)
                put("height_dp", bounds.height / density)
            }
            measurementsList.add(obj)
        }
    }

    // =========================================================================
    // TEST 2: 20 SPECIFIC NOTIFICATION SCENARIOS
    // =========================================================================
    @Test
    fun capture20NotificationScenarios() {
        val scenarioState = mutableStateOf<IslandDemoState>(IslandDemoState.NotificationExpanded)
        val scenarioNotifs = mutableStateOf<List<NotificationInfo>>(emptyList())
        val scenarioActiveIdx = mutableIntStateOf(0)
        val scenarioReplyingId = mutableStateOf<String?>(null)
        val scenarioReplyAction = mutableStateOf<NotificationActionInfo?>(null)

        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AppTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .testTag("scenario_root"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            state = scenarioState.value,
                            notifications = scenarioNotifs.value,
                            notificationActiveIndex = scenarioActiveIdx.intValue,
                            unseenNotificationCount = scenarioNotifs.value.size,
                            replyingNotificationId = scenarioReplyingId.value,
                            replyAction = scenarioReplyAction.value,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        data class NotifScenario(
            val id: String,
            val name: String,
            val state: IslandDemoState,
            val notifications: List<NotificationInfo>,
            val activeIdx: Int = 0,
            val replyingId: String? = null,
            val replyAction: NotificationActionInfo? = null
        )

        val scenarios = listOf(
            // 01: Single short notification
            NotifScenario(
                "01-single-short-whatsapp", "Single Short Notification (WhatsApp)",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo("s1", "com.whatsapp", "WhatsApp", "Alex", "Hey, are you free today?"))
            ),
            // 02: Single long notification
            NotifScenario(
                "02-single-long-email", "Single Long Notification (Gmail)",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo(
                    "s2", "com.google.android.gm", "Gmail", "Engineering Team",
                    "Sprint Review & Planning Meeting Notes: We reviewed the Q4 roadmap, backend migration timeline, and upcoming client deliverables. Please check attached document."
                ))
            ),
            // 03: Notification with action buttons
            NotifScenario(
                "03-notification-with-actions", "Notification with Actions (Slack)",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo(
                    "s3", "com.slack", "Slack", "Sarah Chen",
                    "Can you review the pull request before release?",
                    actions = listOf(
                        NotificationActionInfo("Reply", "Reply", isReply = true),
                        NotificationActionInfo("Mark as Read", "Mark as Read")
                    )
                ))
            ),
            // 04: Notification with large avatar
            NotifScenario(
                "04-notification-large-avatar", "Notification with Avatar",
                IslandDemoState.NotificationImage,
                listOf(NotificationInfo("s4", "com.instagram.android", "Instagram", "Elena Rostov", "Elena sent you a photo"))
            ),
            // 05: OTP notification with Copy Code action
            NotifScenario(
                "05-otp-copy-code", "OTP Notification with Copy Action",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo(
                    "s5", "com.google.android.apps.authenticator2", "Google", "Verification Code",
                    "Your Google verification code is 482910. Do not share with anyone.",
                    actions = listOf(NotificationActionInfo("Copy 482910", "Copy Code"))
                ))
            ),
            // 06: Financial transaction notification
            NotifScenario(
                "06-financial-transaction", "Financial Transaction Alert",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo(
                    "s6", "com.hdfcbank.android", "HDFC Bank", "Debit Alert",
                    "Rs. 3,450.00 debited from A/C *4821 on 08-NOV-26 at Swiggy. Avail Bal: Rs. 42,180.50"
                ))
            ),
            // 07: 2-notification pager
            NotifScenario(
                "07-pager-2-items", "2-Notification Pager (Newest First)",
                IslandDemoState.NotificationExpanded,
                sampleNotifications(2),
                activeIdx = 0
            ),
            // 08: 3-notification pager
            NotifScenario(
                "08-pager-3-items", "3-Notification Pager",
                IslandDemoState.NotificationExpanded,
                sampleNotifications(3),
                activeIdx = 0
            ),
            // 09: 5-notification pager
            NotifScenario(
                "09-pager-5-items", "5-Notification Pager",
                IslandDemoState.NotificationExpanded,
                sampleNotifications(5),
                activeIdx = 0
            ),
            // 10: 10-notification pager
            NotifScenario(
                "10-pager-10-items", "10-Notification Pager",
                IslandDemoState.NotificationExpanded,
                sampleNotifications(10),
                activeIdx = 0
            ),
            // 11: 50-notification maximum pager
            NotifScenario(
                "11-pager-50-max-items", "50-Notification Max Pager",
                IslandDemoState.NotificationExpanded,
                sampleNotifications(50),
                activeIdx = 0
            ),
            // 12: Notification dismissal animation
            NotifScenario(
                "12-pager-dismiss-state", "Pager Notification Dismissal State",
                IslandDemoState.NotificationStacked,
                sampleNotifications(3),
                activeIdx = 1
            ),
            // 13: Incoming notification arrival (compact)
            NotifScenario(
                "13-pager-incoming-arrival", "Incoming Notification Arrival (Compact)",
                IslandDemoState.NotificationCompact,
                sampleNotifications(4),
                activeIdx = 0
            ),
            // 14: Notification + Media concurrent
            NotifScenario(
                "14-notification-plus-media-compact", "Notification + Media Concurrent Compact",
                IslandDemoState.NotificationWithMusicCompact,
                sampleNotifications(2),
                activeIdx = 0
            ),
            // 15: Notification + Call concurrent
            NotifScenario(
                "15-notification-plus-call", "Notification + Call Concurrent",
                IslandDemoState.OngoingCallBanner,
                sampleNotifications(2),
                activeIdx = 0
            ),
            // 16: Notification + Timer concurrent
            NotifScenario(
                "16-notification-plus-timer", "Notification + Timer Concurrent",
                IslandDemoState.TimerCompact,
                sampleNotifications(2),
                activeIdx = 0
            ),
            // 17: Two background Live Activities (Timer + Music)
            NotifScenario(
                "17-live-activities-timer-music", "Timer + Music Concurrent Activities",
                IslandDemoState.TimerImage,
                sampleNotifications(1),
                activeIdx = 0
            ),
            // 18: Three background Live Activities
            NotifScenario(
                "18-three-concurrent-activities", "Three Concurrent Background Activities",
                IslandDemoState.TransportActivity,
                sampleNotifications(3),
                activeIdx = 0
            ),
            // 19: Non-expandable notification
            NotifScenario(
                "19-non-expandable-notification", "Non-Expandable Notification (Compact)",
                IslandDemoState.NotificationCompact,
                listOf(NotificationInfo("s19", "com.system.alert", "System", "Background Task", "Syncing data in background"))
            ),
            // 20: Privacy redaction (sensitive hidden)
            NotifScenario(
                "20-privacy-redacted-locked", "Privacy Redacted (Locked Device)",
                IslandDemoState.NotificationExpanded,
                listOf(NotificationInfo("s20", "com.whatsapp", "WhatsApp", "1 New Message", "Contents hidden on lockscreen", isSensitive = true))
            )
        )

        scenarios.forEach { s ->
            compose.runOnIdle {
                scenarioState.value = s.state
                scenarioNotifs.value = s.notifications
                scenarioActiveIdx.intValue = s.activeIdx
                scenarioReplyingId.value = s.replyingId
                scenarioReplyAction.value = s.replyAction
            }
            compose.waitForIdle()

            val fullBitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            val bounds = try {
                compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            } catch (e: Throwable) {
                androidx.compose.ui.geometry.Rect(0f, 0f, 1080f, 300f)
            }

            val fullFile = saveBitmap(fullBitmap, "04-notification-pager", "${s.id}-full.png")
            val croppedBitmap = cropWithPadding(fullBitmap, bounds, paddingPx = 24)
            val cropFile = saveBitmap(croppedBitmap, "04-notification-pager", "${s.id}-crop.png")

            // Measurements
            val density = 3.0f
            val obj = JSONObject().apply {
                put("state", s.id)
                put("folder", "04-notification-pager")
                put("evidence_tier", "DEVICE_COMPOSE")
                put("full_file", fullFile.relativeTo(baseDir).path)
                put("crop_file", cropFile.relativeTo(baseDir).path)
                put("left_px", bounds.left.toInt())
                put("top_px", bounds.top.toInt())
                put("width_px", bounds.width.toInt())
                put("height_px", bounds.height.toInt())
                put("left_dp", bounds.left / density)
                put("top_dp", bounds.top / density)
                put("width_dp", bounds.width / density)
                put("height_dp", bounds.height / density)
            }
            measurementsList.add(obj)
        }
    }

    // =========================================================================
    // TEST 3: LAYERED MINI-ISLANDS (TASK 05 FEATURE)
    // =========================================================================
    @Test
    fun captureLayeredMiniIslands() {
        val engine = IslandBehaviorEngine()
        var behaviorState by mutableStateOf(IslandBehaviorState())
        var now = 1000L

        fun sendEvent(e: IslandEngineEvent) {
            behaviorState = engine.reduce(behaviorState, e, now++).state
        }

        fun makeSample(id: String) = NotificationInfo(
            id = id,
            packageName = "pkg.$id",
            appName = when (id) {
                "n1" -> "WhatsApp"; "n2" -> "Telegram"; "n3" -> "Gmail"; else -> "Slack"
            },
            title = when (id) {
                "n1" -> "Sarah Miller"; "n2" -> "Alex Rivera"; "n3" -> "GitHub Team"; else -> "Product Lead"
            },
            text = "Testing layered mini-island presentation on hardware",
            timestamp = now++
        )

        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AppTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .testTag("layered_root"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LayeredMiniIslands(
                            state = behaviorState,
                            onEvent = { sendEvent(it) },
                            onPromote = { key, token -> sendEvent(IslandEngineEvent.TapChild(key, token)) }
                        ) {
                            DynamicIslandPill(
                                state = IslandDemoState.NotificationExpanded,
                                notifications = behaviorState.queue.notifications,
                                notificationActiveIndex = behaviorState.queue.activeIndex,
                                onNotificationSelect = { sendEvent(IslandEngineEvent.SelectPage(it)) },
                                modifier = Modifier.padding(top = 12.dp).testTag("layered_main")
                            )
                        }
                    }
                }
            }
        }

        // Case 1: 1 Mini-Island (Main + 1 child)
        compose.runOnIdle {
            sendEvent(IslandEngineEvent.IngestNotification(makeSample("n1"), autoExpand = false))
            sendEvent(IslandEngineEvent.TapCompact)
            sendEvent(IslandEngineEvent.IngestNotification(makeSample("n2"), autoExpand = false))
        }
        compose.waitForIdle()
        var full = compose.onRoot().captureToImage().asAndroidBitmap()
        var bounds = try { compose.onNodeWithTag("layered_main", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot } catch (e: Throwable) { androidx.compose.ui.geometry.Rect(0f, 0f, 1080f, 400f) }
        saveBitmap(full, "05-layered-mini-islands", "01-single-mini-island-full.png")
        saveBitmap(cropWithPadding(full, bounds, 36), "05-layered-mini-islands", "01-single-mini-island-crop.png")

        // Case 2: 2 Mini-Islands (Main + 2 children)
        compose.runOnIdle {
            sendEvent(IslandEngineEvent.IngestNotification(makeSample("n3"), autoExpand = false))
        }
        compose.waitForIdle()
        full = compose.onRoot().captureToImage().asAndroidBitmap()
        saveBitmap(full, "05-layered-mini-islands", "02-two-mini-islands-full.png")
        saveBitmap(cropWithPadding(full, bounds, 36), "05-layered-mini-islands", "02-two-mini-islands-crop.png")

        // Case 3: 3 Mini-Islands (Main + 3 children, maximum stack capacity)
        compose.runOnIdle {
            sendEvent(IslandEngineEvent.IngestNotification(makeSample("n4"), autoExpand = false))
        }
        compose.waitForIdle()
        full = compose.onRoot().captureToImage().asAndroidBitmap()
        saveBitmap(full, "05-layered-mini-islands", "03-three-mini-islands-full.png")
        saveBitmap(cropWithPadding(full, bounds, 36), "05-layered-mini-islands", "03-three-mini-islands-crop.png")

        // Case 4: Privacy redacted mini-islands
        compose.runOnIdle {
            sendEvent(IslandEngineEvent.PrivacyChanged(ChildPrivacyPolicy(deviceLocked = true, hideSensitiveContent = true)))
        }
        compose.waitForIdle()
        full = compose.onRoot().captureToImage().asAndroidBitmap()
        saveBitmap(full, "05-layered-mini-islands", "04-redacted-mini-islands-full.png")
        saveBitmap(cropWithPadding(full, bounds, 36), "05-layered-mini-islands", "04-redacted-mini-islands-crop.png")
    }

    // =========================================================================
    // TEST 4: ACCESSIBILITY, FONT SCALING, AND DENSITY VARIATIONS
    // =========================================================================
    @Test
    fun captureAccessibilityAndScaling() {
        data class ScalingConfig(val id: String, val widthDp: Int, val fontScale: Float, val rtl: Boolean, val landscape: Boolean = false)
        val configs = listOf(
            ScalingConfig("scale-font-100", 360, 1.0f, false),
            ScalingConfig("scale-font-150", 360, 1.5f, false),
            ScalingConfig("scale-font-200", 360, 2.0f, false),
            ScalingConfig("width-320dp-compact", 320, 1.0f, false),
            ScalingConfig("width-360dp-standard", 360, 1.0f, false),
            ScalingConfig("width-393dp-expanded", 393, 1.0f, false),
            ScalingConfig("layout-rtl-hebrew", 360, 1.0f, true),
            ScalingConfig("orientation-landscape-640dp", 640, 1.0f, false, landscape = true)
        )

        val currentConfig = mutableStateOf(configs[0])
        val notifs = sampleNotifications(3)

        compose.setContent {
            val cfg = currentConfig.value
            val configuration = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = cfg.widthDp
                screenHeightDp = if (cfg.landscape) 360 else 800
                fontScale = cfg.fontScale
            }
            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalDensity provides Density(3.0f, cfg.fontScale),
                LocalLayoutDirection provides if (cfg.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalReducedMotion provides true
            ) {
                AppTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .testTag("scaling_root"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            state = IslandDemoState.NotificationExpanded,
                            notifications = notifs,
                            unseenNotificationCount = notifs.size,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        configs.forEach { cfg ->
            compose.runOnIdle { currentConfig.value = cfg }
            compose.waitForIdle()

            val fullBitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            val bounds = try {
                compose.onNodeWithTag("island_surface", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            } catch (e: Throwable) {
                androidx.compose.ui.geometry.Rect(0f, 0f, 1080f, 300f)
            }

            val fullFile = saveBitmap(fullBitmap, "10-accessibility-and-scaling", "${cfg.id}-full.png")
            val croppedBitmap = cropWithPadding(fullBitmap, bounds, paddingPx = 24)
            val cropFile = saveBitmap(croppedBitmap, "10-accessibility-and-scaling", "${cfg.id}-crop.png")

            // Measurements
            val density = 3.0f
            val obj = JSONObject().apply {
                put("state", cfg.id)
                put("folder", "10-accessibility-and-scaling")
                put("evidence_tier", "DEVICE_COMPOSE")
                put("full_file", fullFile.relativeTo(baseDir).path)
                put("crop_file", cropFile.relativeTo(baseDir).path)
                put("width_dp", bounds.width / density)
                put("height_dp", bounds.height / density)
                put("font_scale", cfg.fontScale)
                put("rtl", cfg.rtl)
            }
            measurementsList.add(obj)
        }
    }

    // =========================================================================
    // TEST 5: ALL APPLICATION SCREENS IN NAVIGATION GRAPH
    // =========================================================================
    @Test
    fun captureAllAppScreens() {
        val currentIdx = mutableIntStateOf(0)

        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0B0F19))) {
                    when (currentIdx.intValue) {
                        0 -> ai.emots.kishan_dynamic.ui.screens.StartupSplashScreen()
                        1 -> ai.emots.kishan_dynamic.ui.screens.OnboardingScreen(onComplete = {}, onSkip = {})
                        2 -> ai.emots.kishan_dynamic.ui.screens.PermissionScreen(onContinue = {}, onBack = {})
                        3 -> ai.emots.kishan_dynamic.ui.screens.MainContainerScreen(initialTab = 0)
                        4 -> ai.emots.kishan_dynamic.ui.screens.MainContainerScreen(initialTab = 1)
                        5 -> ai.emots.kishan_dynamic.ui.screens.MainContainerScreen(initialTab = 2)
                        6 -> ai.emots.kishan_dynamic.ui.screens.MainContainerScreen(initialTab = 3)
                        7 -> ai.emots.kishan_dynamic.ui.screens.DisplaySettingsScreen(onBack = {}, onNavigateToPremium = {})
                        8 -> ai.emots.kishan_dynamic.ui.screens.ThemeSettingsScreen(
                            themeMode = ai.emots.kishan_dynamic.ui.theme.AppThemeMode.Dark,
                            onThemeModeChange = {},
                            onBack = {},
                            onNavigateToPremium = {}
                        )
                        9 -> ai.emots.kishan_dynamic.ui.screens.QuickControlScreen(onBack = {})
                        10 -> ai.emots.kishan_dynamic.ui.screens.NotificationSettingsScreen(onBack = {}, onNavigateToPremium = {})
                        11 -> ai.emots.kishan_dynamic.ui.screens.SoundSettingsScreen(onBack = {}, onNavigateToPremium = {})
                        12 -> ai.emots.kishan_dynamic.ui.screens.CallSettingsScreen(onBack = {}, onNavigateToPremium = {})
                        13 -> ai.emots.kishan_dynamic.ui.screens.MusicSettingsScreen(onBack = {}, onNavigateToPremium = {})
                        14 -> ai.emots.kishan_dynamic.ui.screens.BatterySettingsScreen(onBack = {})
                        15 -> ai.emots.kishan_dynamic.ui.screens.LanguageScreen(onBack = {}, onComplete = {})
                        16 -> ai.emots.kishan_dynamic.ui.screens.PremiumScreen(onBack = {})
                        17 -> ai.emots.kishan_dynamic.ui.screens.AboutScreen(onBack = {})
                        18 -> ai.emots.kishan_dynamic.ui.screens.FeedbackScreen(onBack = {})
                        19 -> ai.emots.kishan_dynamic.ui.gallery.ComponentGalleryScreen(isDarkTheme = true, onToggleTheme = {})
                        20 -> ai.emots.kishan_dynamic.playground.DynamicIslandPlayground(onBack = {})
                    }
                }
            }
        }

        val fileNames = listOf(
            "01-startup-splash.png",
            "02-onboarding.png",
            "03-permission-hub.png",
            "04-main-hub-tab0-island.png",
            "05-main-hub-tab1-features.png",
            "06-main-hub-tab2-live.png",
            "07-main-hub-tab3-settings.png",
            "08-display-settings.png",
            "09-theme-settings.png",
            "10-quick-control.png",
            "11-notification-settings.png",
            "12-sound-settings.png",
            "13-call-settings.png",
            "14-music-settings.png",
            "15-battery-settings.png",
            "16-language-selection.png",
            "17-premium-paywall.png",
            "18-about-screen.png",
            "19-feedback-screen.png",
            "20-component-gallery.png",
            "21-playground.png"
        )

        fileNames.forEachIndexed { index, name ->
            compose.runOnIdle { currentIdx.intValue = index }
            compose.waitForIdle()
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            saveBitmap(bitmap, "09-app-pages", name)
        }
    }
}
