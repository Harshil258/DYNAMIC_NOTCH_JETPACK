package ai.emots.kishan_dynamic.playground

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.IslandMotionCurve
import ai.emots.kishan_dynamic.ui.kit.*
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun DynamicIslandPlayground(
    controller: PlaygroundController = PlaygroundSession.controller,
    onBack: () -> Unit = {}, onPhysicalOverlay: () -> Unit = {}, onStopOverlay: () -> Unit = {}
) {
    val snapshot by controller.snapshot.collectAsState()
    val results by controller.checks.collectAsState()
    val physical by PlaygroundSession.overlayRunning.collectAsState()
    val physicalInfo by PlaygroundSession.physicalInfo.collectAsState()
    var section by rememberSaveable { mutableIntStateOf(0) }
    var hostDetails by remember { mutableStateOf(false) }
    var measurements by remember { mutableStateOf("Waiting for layout") }
    val geometry = remember { PlaygroundGeometry() }
    val o = snapshot.options
    val sections = listOf("Live Preview", "States", "Animations", "Geometry", "Stress Tests", "Test Results")
    AppScreen(scrollable = false, topBar = {
        AppTopBar(title = "Dynamic Island Lab", subtitle = "Debug only · isolated mock data", onBack = onBack)
    }, bottomAction = {
        AppText(snapshot.feedback, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        AppButton(text = if (physical) "Stop physical overlay" else if (snapshot.sequenceRunning) "Stop sequence" else "Replay transition",
            onClick = { if (physical) onStopOverlay() else if (snapshot.sequenceRunning) controller.cancelSequence() else controller.replay() },
            modifier = Modifier.testTag("playground_primary"))
    }) {
        Column(Modifier.fillMaxSize()) {
            AppCard(padding = AppTheme.spacing.sm) {
                AppText("Simulation · ${o.width}dp · ${o.density}× density · ${(o.fontScale * 100).toInt()}% type",
                    style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                Box(Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                    PlaygroundPhonePreview(snapshot, controller, geometry) { measurements = it }
                }
                AppText("${snapshot.selected.title} · $measurements", style = AppTheme.typography.caption)
            }
            Spacer(Modifier.height(AppTheme.spacing.sm))
            AppScrollableTabs(sections, section, { section = it }, Modifier.testTag("playground_sections"))
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(vertical = AppTheme.spacing.md), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                when (section) {
                    0 -> {
                        AppSectionTitle("Phone simulation")
                        AppScrollableTabs(playgroundDevices.map { it.name }, o.deviceIndex, { index ->
                            val device = playgroundDevices[index]
                            controller.options { it.copy(deviceIndex = index, width = device.width, density = device.density, camera = device.camera) }
                        })
                        AppSegmented(CameraPreset.entries.map { it.name }, o.camera.ordinal,
                            { index -> controller.options { it.copy(camera = CameraPreset.entries[index]) } })
                        LabSlider("Simulated density", o.density, 1f..4f, "×") { value -> controller.options { it.copy(density = value) } }
                        LabSlider("Font scale", o.fontScale, .85f..2f, "×") { value -> controller.options { it.copy(fontScale = value) } }
                        LabToggle("RTL layout", o.rtl) { value -> controller.options { it.copy(rtl = value) } }
                        LabToggle("Reduced motion", o.reducedMotion) { value -> controller.options { it.copy(reducedMotion = value) } }
                        LabToggle("Bounds and camera-safe guides", o.guides) { value -> controller.options { it.copy(guides = value) } }
                        AppSectionTitle("Physical Android device")
                        AppText(physicalInfo, style = AppTheme.typography.caption)
                        AppText("Uses a real application-overlay window, this device’s density, font scale and cutout bounds. Mock actions stay isolated. Automatically stops after 3 minutes.",
                            style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                        AppButton(if (physical) "Physical overlay running" else "Start physical overlay", onClick = onPhysicalOverlay,
                            style = AppButtonStyle.Secondary, enabled = !physical, modifier = Modifier.testTag("start_physical_overlay"))
                    }
                    1 -> {
                        AppSectionTitle("State catalog · ${PlaygroundCase.entries.size} fixtures")
                        PlaygroundCase.entries.forEach { case ->
                            AppButton(case.title, { controller.select(case) }, style = if (snapshot.selected == case) AppButtonStyle.Tonal else AppButtonStyle.Secondary,
                                modifier = Modifier.testTag("case_${case.name}"))
                        }
                        AppButton("Expand current activity", { controller.expand(true) }, style = AppButtonStyle.Secondary)
                        AppButton("Collapse current activity", { controller.expand(false) }, style = AppButtonStyle.Secondary)
                    }
                    2 -> {
                        AppSectionTitle("Animation Lab")
                        LabToggle("Override production motion", o.motionOverride) { value -> controller.options { it.copy(motionOverride = value) } }
                        AppText(if (o.motionOverride) "Controls drive the renderer’s real animation specs." else "Unmodified production animation defaults are active.",
                            style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                        AppSegmented(IslandMotionCurve.entries.map { it.name }, o.motion.curve.ordinal,
                            { index -> controller.options { it.copy(motionOverride = true, motion = it.motion.copy(curve = IslandMotionCurve.entries[index])) } })
                        LabSlider("Duration / content fade", o.motion.durationMillis.toFloat(), 80f..800f, "ms") { v ->
                            controller.options { it.copy(motionOverride = true, motion = it.motion.copy(durationMillis = v.toInt())) } }
                        LabSlider("Spring stiffness", o.motion.stiffness, 50f..1500f, "") { v ->
                            controller.options { it.copy(motionOverride = true, motion = it.motion.copy(stiffness = v)) } }
                        LabSlider("Damping ratio", o.motion.damping, .4f..1.5f, "") { v ->
                            controller.options { it.copy(motionOverride = true, motion = it.motion.copy(damping = v)) } }
                        val speeds = listOf(.25f, .5f, 1f, 2f)
                        AppSegmented(listOf("0.25×", "0.5×", "1×", "2×"), speeds.indexOf(o.motion.speed).coerceAtLeast(0), { index ->
                            controller.options { it.copy(motionOverride = true, motion = it.motion.copy(speed = speeds[index])) } })
                        LabToggle("Disable animations", o.motionOverride && !o.motion.enabled) { disabled ->
                            controller.options { it.copy(motionOverride = true, motion = it.motion.copy(enabled = !disabled)) } }
                        AppText("Spring mode uses stiffness and damping; Smooth and Linear use duration/easing. Speed scales the actual specs. Duration also controls content fades.",
                            style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                        SequenceButtons(controller)
                    }
                    3 -> {
                        AppSectionTitle("Live geometry")
                        AppText("Measured island: $measurements · ${geometry.regions.size} observed regions", style = AppTheme.typography.bodySmall)
                        geometry.regions.values.distinct().forEach { (role, bounds) ->
                            AppText("$role: ${bounds.width.toInt()} × ${bounds.height.toInt()} on-screen px", style = AppTheme.typography.caption)
                        }
                        LabToggle("Manual size override", o.manualSize) { v -> controller.options { it.copy(manualSize = v) } }
                        GeometrySlider("Requested width", o.widthOverride, 48f..408f) { v -> controller.options { it.copy(manualSize = true, widthOverride = v) } }
                        GeometrySlider("Requested height", o.heightOverride, 48f..400f) { v -> controller.options { it.copy(manualSize = true, heightOverride = v) } }
                        GeometrySlider("Minimum width", o.minimumWidth, 0f..200f) { v -> controller.options { it.copy(minimumWidth = v) } }
                        GeometrySlider("Maximum width", o.maximumWidth, 96f..408f) { v -> controller.options { it.copy(maximumWidth = v) } }
                        GeometrySlider("Maximum height", o.maximumHeight, 96f..700f) { v -> controller.options { it.copy(maximumHeight = v) } }
                        GeometrySlider("Compact body height", o.compactHeight, 28f..60f) { v -> controller.options { it.copy(compactHeight = v, iconSize = minOf(it.iconSize, v - 8)) } }
                        GeometrySlider("Leading + trailing inset", o.horizontalPadding, 4f..32f) { v -> controller.options { it.copy(horizontalPadding = v) } }
                        GeometrySlider("Expanded horizontal + vertical inset", o.verticalPadding, 4f..32f) { v -> controller.options { it.copy(verticalPadding = v) } }
                        GeometrySlider("Compact / alert icon", o.iconSize, 12f..36f) { v -> controller.options { it.copy(iconSize = v, compactHeight = maxOf(it.compactHeight, v + 8)) } }
                        GeometrySlider("Icon / text gap", o.textGap, 4f..24f) { v -> controller.options { it.copy(textGap = v) } }
                        LabToggle("Override corner radius", o.overrideCorner) { v -> controller.options { it.copy(overrideCorner = v) } }
                        GeometrySlider("Corner radius", o.corner, 4f..48f) { v -> controller.options { it.copy(overrideCorner = true, corner = v) } }
                        GeometrySlider("Camera clearance", o.cameraClearance, 0f..24f) { v -> controller.options { it.copy(cameraClearance = v) } }
                        GeometrySlider("Action target size", o.actionSize, 50f..80f) { v -> controller.options { it.copy(actionSize = v) } }
                        GeometrySlider("Action / region spacing", o.actionGap, 4f..24f) { v -> controller.options { it.copy(actionGap = v) } }
                        AppText("Cyan: actual shell / centerlines. Magenta: token inset zones. Amber: measured action and artwork regions. Red: camera-safe boundary. Widths remain clamped by production status-side and window budgets.",
                            style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                        AppButton("Reset geometry and motion", { controller.options { current -> PlaygroundOptions(deviceIndex = current.deviceIndex,
                            width = current.width, density = current.density, camera = current.camera, fontScale = current.fontScale, rtl = current.rtl) } }, style = AppButtonStyle.Secondary)
                    }
                    4 -> {
                        AppSectionTitle("Edge cases and stress")
                        LabToggle("Long titles and descriptions", o.longText) { v -> controller.options { it.copy(longText = v) } }
                        LabToggle("Missing artwork / fallback icon", o.missingIcon) { v -> controller.options { it.copy(missingIcon = v) } }
                        LabToggle("Missing notification text", o.missingText) { v -> controller.options { it.copy(missingText = v) } }
                        AppSegmented(listOf("0 actions", "1 action", "2 actions", "4 actions"), listOf(0,1,2,4).indexOf(o.actionCount).coerceAtLeast(0), { i ->
                            controller.options { it.copy(actionCount = listOf(0,1,2,4)[i]) } })
                        AppText("Expand notification details to inspect every source action. All source IDs and content here are synthetic.", style = AppTheme.typography.caption)
                        AppButton("Burst: 30 rapid notifications", controller::burst, modifier = Modifier.testTag("run_burst"))
                        AppText("Applied mock events: ${snapshot.eventCount}. Rapid updates may coalesce to a frame, as in the production renderer.", style = AppTheme.typography.caption)
                        SequenceButtons(controller)
                    }
                    5 -> {
                        AppSectionTitle("Validation results")
                        AppButton("Run local policy checks", controller::runChecks, modifier = Modifier.testTag("run_local_checks"))
                        if (results.isNotEmpty()) AppText("${results.count { it.passed }} / ${results.size} local checks passed", style = AppTheme.typography.h3,
                            modifier = Modifier.testTag("local_check_summary"))
                        results.forEach { result -> AppText("${if (result.passed) "PASS" else "FAIL"} · ${result.name}\n${result.detail}",
                            style = AppTheme.typography.bodySmall, color = if (result.passed) AppTheme.colors.textPrimary else AppTheme.colors.error) }
                        AppSectionTitle("Host automation")
                        val context = LocalContext.current
                        val hostReport = remember { runCatching { context.assets.open("playground-test-results.json").bufferedReader().use { JSONObject(it.readText()) } }.getOrNull() }
                        AppText(hostReport?.optString("summary") ?: "No bundled host report. Run tools/test-island-playground.sh, then rebuild the debug APK.",
                            style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                        hostReport?.let { report ->
                            AppText("Report timestamp: ${report.optString("generated_at")} · snapshot, not a live CI connection", style = AppTheme.typography.caption)
                            AppButton(if (hostDetails) "Hide host case details" else "Show host case details", { hostDetails = !hostDetails }, style = AppButtonStyle.Secondary)
                            if (hostDetails) {
                                val cases = report.optJSONArray("cases")
                                if (cases != null) for (index in 0 until cases.length()) {
                                    val test = cases.getJSONObject(index)
                                    AppText("${test.optString("status").uppercase()} · ${test.optString("class").substringAfterLast('.')} · ${test.optString("name")}", style = AppTheme.typography.caption)
                                }
                            }
                        }
                        AppText("The local button checks pure mapping, interaction and geometry policies. It does not claim to run Compose instrumentation, screenshot comparisons, TalkBack or physical-device tests. The host script runs the actual Gradle / device suites.",
                            style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable private fun LabToggle(title: String, value: Boolean, change: (Boolean) -> Unit) =
    AppToggleRow(title = title, checked = value, onCheckedChange = change)
@Composable private fun LabSlider(title: String, value: Float, range: ClosedFloatingPointRange<Float>, suffix: String, change: (Float) -> Unit) =
    AppSlider(title = title, value = value, valueRange = range, onValueChange = change,
        modifier = Modifier.testTag("lab_slider_$title").semantics(mergeDescendants = true) {
            contentDescription = title
            progressBarRangeInfo = ProgressBarRangeInfo(value, range)
            setProgress { requested -> change(requested.coerceIn(range)); true }
        }, valueFormatter = { "${(it * 100).roundToInt() / 100f}$suffix" })
@Composable private fun GeometrySlider(title: String, value: Float, range: ClosedFloatingPointRange<Float>, change: (Float) -> Unit) =
    LabSlider(title, value, range, "dp", change)
@Composable private fun SequenceButtons(controller: PlaygroundController) {
    listOf("Expand / collapse", "Visibility", "Notification swap", "Multiple").forEach { name ->
        AppButton("Run $name", { controller.runSequence(name) }, style = AppButtonStyle.Secondary)
    }
}
