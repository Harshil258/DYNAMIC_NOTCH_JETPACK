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
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.playground.FigmaReferenceFixtures
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.*
import java.io.File

/** Physical-device capture inventory. Mock sources; never sends calls/replies or changes phone settings. */
class AuroraAllConditionsCaptureTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder get() = File(context.getExternalFilesDir(null), "aurora-all-conditions")
    private var previousSuppression = false
    private data class Shot(val state: IslandDemoState = IslandDemoState.Idle, val scenario: String = "baseline",
        val dark: Boolean = true, val width: Int? = null, val font: Float = 1f, val rtl: Boolean = false)
    private var shot by mutableStateOf(Shot())
    private val failed = mutableListOf<String>()
    @Before fun isolate() {
        previousSuppression = IslandStateManager.isPaywallActive.value
        IslandStateManager.setPaywallActive(true)
        folder.mkdirs()
    }
    @After fun restore() { IslandStateManager.setPaywallActive(previousSuppression) }

    @Composable private fun Content() {
        val s = shot
        val view = LocalView.current
        DisposableEffect(view) {
            val previous = view.keepScreenOn
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = previous }
        }
        val nativeConfig = LocalConfiguration.current
        val nativeDensity = LocalDensity.current
        val config = Configuration(nativeConfig).apply {
            if (s.width != null) { screenWidthDp = s.width; screenHeightDp = if (s.width > 430) 360 else 850 }
        }
        val density = if (s.width == null) nativeDensity else Density(if (s.width > 430) 1.5f else 2f, s.font)
        CompositionLocalProvider(LocalConfiguration provides config, LocalDensity provides density,
            LocalLayoutDirection provides if (s.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalReducedMotion provides true,
            LocalIslandFixtureIcons provides mapOf(FigmaReferenceFixtures.packageName to R.drawable.figma_reference_app)) {
            AppTheme(darkTheme = s.dark) {
                val bg = if (s.dark) Color(0xFF0B0F19) else Color(0xFFF8F9FD)
                val fg = if (s.dark) Color.White else Color.Black
                Box((if (s.width == null) Modifier.fillMaxSize() else Modifier.width(s.width.dp).height(config.screenHeightDp.dp))
                    .background(bg).testTag("review_root")) {
                    val islandModifier = if (s.width == null) Modifier.fillMaxWidth().statusBarsPadding() else Modifier.fillMaxWidth()
                    Box(islandModifier, contentAlignment = Alignment.TopCenter) {
                        key(s) { if (s.scenario != "hidden") Island(s) }
                    }
                    if (s.width == null) Column(Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(16.dp)) {
                        Text(s.state.name, color = fg, fontSize = 16.sp)
                        Text("${s.scenario} · ${if (s.dark) "dark" else "light"} · native device\nSynthetic sources / actual production renderer", color = fg, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    @Composable private fun Island(s: Shot) {
        val long = "A deliberately long sender and activity identity for a complete visual review"
        var messages = FigmaReferenceFixtures.messages(context)
        val reply = NotificationActionInfo("review-reply", "Reply", isReply = true)
        when (s.scenario) {
            "single-message" -> messages = listOf(messages[1].copy(picturePath = null))
            "single-image" -> messages = listOf(messages[1])
            "long-content" -> messages = listOf(messages[1].copy(title = long, text = ("Long notification content must wrap and remain accessible. ").repeat(16), picturePath = null))
            "missing-content" -> messages = listOf(NotificationInfo("missing", "uninstalled.review.fixture", "", "", ""))
            "broken-image" -> messages = listOf(messages[1].copy(picturePath = "/missing/review-image.png", imagePath = "/missing/review-avatar.png"))
            "multi-actions", "reply-editor" -> messages = listOf(messages[1].copy(picturePath = null, actions = listOf(reply,
                NotificationActionInfo("read", "Mark as read"), NotificationActionInfo("archive", "Archive"), NotificationActionInfo("open", "Open conversation"))))
            "inbox" -> messages = listOf(messages[1].copy(picturePath = null, inboxLines = listOf("First inbox line", "Second inbox line", "Third inbox line")))
            "progress", "indeterminate" -> messages = listOf(messages[1].copy(picturePath = null, isOngoing = true, progress = 47,
                progressMax = 100, isProgressIndeterminate = s.scenario == "indeterminate", title = "Download", text = "Source-backed progress"))
            "redacted" -> messages = messages.map { LockScreenPrivacyPolicy.notification(it, true, true) }
            "many-messages" -> messages = List(12) { messages[it % messages.size].copy(id = "message-$it", title = "Message ${it + 1}") }
        }
        val count = s.scenario.removePrefix("count-").toIntOrNull() ?: messages.size
        val selected = when (s.scenario) { "pager-first" -> 0; "pager-last", "many-messages" -> messages.lastIndex; else -> minOf(1, messages.lastIndex) }
        val caller = when (s.scenario) {
            "long-content" -> ContactInfo(long, "")
            "missing-content", "redacted" -> ContactInfo(if (s.scenario == "redacted") "Private call" else "Unknown caller", "")
            else -> FigmaReferenceFixtures.caller(context)
        }
        val track = when (s.scenario) {
            "long-content" -> MusicTrack(long, long)
            "missing-content" -> MusicTrack("", "", albumArtUri = "/missing/art.png")
            else -> FigmaReferenceFixtures.music(context)
        }
        val controls = when (s.scenario) {
            "no-controls" -> emptySet()
            "carrier-controls" -> setOf(IslandControlAction.CallToggleSpeaker, IslandControlAction.CallToggleMute, IslandControlAction.CallEnd)
            else -> null
        }
        val sourceKind = if (s.scenario.startsWith("source-"))
            LiveActivityKind.entries.firstOrNull { s.scenario.startsWith("source-${it.name}-") } else null
        val activity = sourceKind?.let { LiveActivityInfo("review-source", it,
            if (s.scenario.endsWith("missing")) "" else "Source-backed ${it.name.lowercase().replace('_', ' ')}",
            subtitle = if (s.scenario.endsWith("missing")) "" else "Synthetic source details and status",
            progress = .47f, isExpanded = true, isRunning = !s.scenario.endsWith("paused"),
            sourceNotificationId = "review-source-notification", sourceActions = listOf(NotificationActionInfo("review-open", "Open")),
            sourceHasContentIntent = true, entityName = "Review activity", primaryMetric = "12 min", secondaryMetric = "47%", isLiveSource = true) }
        val battery = s.scenario.removePrefix("battery-").toIntOrNull() ?: if (s.state.name.startsWith("LowBattery")) 15 else 85
        DynamicIslandPill(s.state, modifier = Modifier.testTag("review_island"), notifications = messages,
            notificationActiveIndex = selected, unseenNotificationCount = count,
            replyingNotificationId = if (s.scenario == "reply-editor") messages.first().id else null,
            replyAction = if (s.scenario == "reply-editor") reply else null,
            callContact = caller,
            callParticipants = if (s.state == IslandDemoState.CallAvatars && s.scenario == "baseline") listOf(caller, ContactInfo("Jordan Lee", "")) else emptyList(),
            callSummary = if (s.state == IslandDemoState.CallSummaryExpanded) CallRecord("review", caller.name, "", startedAtMillis = 0,
                durationSeconds = if (s.scenario == "unknown-duration") 0 else 125, direction = if (s.scenario == "missed") CallDirection.MISSED else CallDirection.INCOMING,
                durationAccuracy = if (s.scenario == "unknown-duration") DurationAccuracy.UNKNOWN else DurationAccuracy.RELIABLE_MONOTONIC) else null,
            callDurationSeconds = if (s.scenario == "long-duration") 3661 else 125,
            showCallDuration = s.scenario != "unknown-duration", callIsDialing = s.scenario == "dialing", callCanDecline = s.scenario != "no-decline",
            musicTrack = track, musicIsPlaying = s.scenario != "paused", musicScrubberEnabled = s.scenario != "no-controls",
            timerLabel = if (s.scenario == "expired") "00:00" else if (s.scenario == "long-duration") "12:59:59" else "3:35",
            timerRunning = s.scenario != "paused", timerProgress = if (s.scenario == "expired") 0f else .72f,
            ringerMode = when (s.scenario) { "ringer-normal" -> RingerModeType.NORMAL; "ringer-vibrate" -> RingerModeType.VIBRATE; else -> RingerModeType.SILENT },
            ringerVolumeLevel = if (s.scenario == "zero") 0f else if (s.scenario == "full") 1f else .5f,
            mediaVolumeLevel = if (s.scenario == "zero") 0f else if (s.scenario == "full") 1f else .5f,
            mediaVolumeHudLevel = if (s.scenario == "zero") 0f else if (s.scenario == "full") 1f else .5f,
            bluetoothDeviceName = if (s.scenario == "long-content") long else "Studio headphones",
            bluetoothBatteryPercent = if (s.scenario == "battery-unknown") null else battery,
            bluetoothIsConnecting = s.scenario == "connecting", batteryPercent = battery,
            premiumHoursRemaining = if (s.scenario == "expired") 0 else if (s.scenario == "long-duration") 168 else 12,
            liveActivityInfo = activity, liveActivitySourceBacked = activity != null,
            liveActivityTitle = activity?.title ?: if (s.scenario == "long-content") long else "",
            liveActivitySubtitle = activity?.subtitle ?: if (s.scenario == "long-content") long else "",
            liveActivityProgress = activity?.progress,
            availableControlActions = controls, cameraCutoutMode = s.scenario == "cutout", flatTop = s.scenario == "flat-top")
    }
    private fun setup() { compose.setContent { Content() } }
    private fun capture(s: Shot, relative: String, fullscreen: Boolean) {
        try {
            compose.runOnIdle { shot = s }
            compose.waitForIdle()
            compose.onNodeWithTag("review_root").assertExists()
            if (s.scenario != "hidden") compose.onNodeWithTag("island_surface", useUnmergedTree = true).assertExists()
            if (s.state in listOf(IslandDemoState.NotificationExpanded, IslandDemoState.NotificationImage) &&
                s.scenario in listOf("baseline", "single-image", "pager-first", "pager-last", "many-messages")) {
                compose.waitUntil(10_000) { compose.onAllNodesWithTag("notification_artwork_loaded", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
            }
            instrumentation.waitForIdleSync()
            val bitmap = if (fullscreen) instrumentation.uiAutomation.takeScreenshot()
                ?: error("Physical screenshot returned null") else compose.onNodeWithTag("review_island", useUnmergedTree = true).captureToImage().asAndroidBitmap()
            val file = File(folder, relative).apply { parentFile?.mkdirs() }
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            File(folder, "captured.tsv").appendText("$relative\t${s.state}\t${s.scenario}\t${s.width ?: "native"}\t${s.font}\t${s.rtl}\t${s.dark}\t${bitmap.width}\t${bitmap.height}\n")
            if (fullscreen) bitmap.recycle()
        } catch (t: Throwable) {
            failed += "$relative: ${t.javaClass.simpleName}: ${t.message}"
            File(folder, "failures.txt").appendText(failed.last()+"\n")
        }
    }
    @Test fun nativeDeviceEveryStateAndContentConditions() {
        setup()
        for (dark in listOf(true, false)) for (state in IslandDemoState.entries)
            capture(Shot(state, dark = dark), "01-native-device/${if (dark) "dark" else "light"}/${state.name}.png", true)
        for (scenario in listOf("long-content", "missing-content", "no-controls")) for (state in IslandDemoState.entries)
            capture(Shot(state, scenario), "02-content-and-controls/$scenario/${state.name}.png", true)
        val extra = mutableListOf<Shot>()
        for (scenario in listOf("single-message", "single-image", "broken-image", "multi-actions", "reply-editor", "inbox", "progress", "indeterminate", "redacted", "many-messages", "pager-first", "pager-last"))
            extra += Shot(IslandDemoState.NotificationExpanded, scenario)
        for (state in listOf(IslandDemoState.NotificationCompact, IslandDemoState.NotificationStacked)) for (count in listOf(0,1,2,3,9,10,99,100)) extra += Shot(state,"count-$count")
        for (state in listOf(IslandDemoState.CallExpanded, IslandDemoState.FaceTimeCallExpanded, IslandDemoState.SharedMediaCallExpanded, IslandDemoState.CallCompact, IslandDemoState.CallAvatars))
            for (scenario in listOf("dialing", "long-duration", "unknown-duration", "carrier-controls", "no-decline", "redacted")) extra += Shot(state,scenario)
        for (scenario in listOf("missed", "unknown-duration")) extra += Shot(IslandDemoState.CallSummaryExpanded,scenario)
        for (state in listOf(IslandDemoState.MusicCompact, IslandDemoState.MusicExpanded)) extra += Shot(state,"paused")
        for (state in listOf(IslandDemoState.TimerCompact, IslandDemoState.TimerExpanded, IslandDemoState.TimerImage)) for (scenario in listOf("paused","expired","long-duration")) extra += Shot(state,scenario)
        for (state in listOf(IslandDemoState.ChargingCompact, IslandDemoState.ChargingExpanded, IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded, IslandDemoState.BluetoothConnected))
            for (value in listOf(0,1,19,20,99,100)) extra += Shot(state,"battery-$value")
        for (scenario in listOf("battery-unknown", "connecting")) extra += Shot(IslandDemoState.BluetoothConnected,scenario)
        for (scenario in listOf("ringer-normal", "ringer-vibrate")) extra += Shot(IslandDemoState.RingerModeExpanded,scenario)
        for (state in listOf(IslandDemoState.RingerVolumeCompact,IslandDemoState.MediaVolumeCompact)) for (scenario in listOf("zero","full")) extra += Shot(state,scenario)
        for (scenario in listOf("expired","long-duration")) extra += Shot(IslandDemoState.PremiumExpiryExpanded,scenario)
        for (state in listOf(IslandDemoState.Idle,IslandDemoState.NotificationCompact,IslandDemoState.MusicCompact,IslandDemoState.CallCompact)) for (scenario in listOf("cutout","flat-top")) extra += Shot(state,scenario)
        for (kind in LiveActivityKind.entries) for (variant in listOf("running", "paused", "missing")) {
            val state = resolveOverlayDemoState(IslandState.LiveActivity(LiveActivityInfo("source",kind,"Review",isExpanded=true)),1)
            extra += Shot(state,"source-${kind.name}-$variant")
        }
        extra += Shot(scenario = "hidden")
        for (s in extra) capture(s,"03-family-conditions/${s.state}/${s.scenario}.png",true)
        Assert.assertTrue(failed.joinToString("\n"), failed.isEmpty())
    }
    @Test fun responsiveEveryStateEveryWidthFontDirectionAndTheme() {
        setup()
        for (dark in listOf(true,false)) for (width in listOf(320,360,393,430,640)) for (font in listOf(1f,1.3f,1.5f,2f)) for (rtl in listOf(false,true)) {
            val config = "${if(dark) "dark" else "light"}/w$width-font${(font*100).toInt()}-${if(rtl) "rtl" else "ltr"}"
            for (state in IslandDemoState.entries) capture(Shot(state,dark=dark,width=width,font=font,rtl=rtl),"04-responsive/$config/${state.name}.png",false)
            println("CAPTURE_MATRIX $config complete; failures=${failed.size}")
        }
        Assert.assertTrue(failed.joinToString("\n"), failed.isEmpty())
    }
}
