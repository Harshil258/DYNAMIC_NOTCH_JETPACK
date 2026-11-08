package ai.emots.kishan_dynamic.playground

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.*

class PlaygroundGeometry : IslandGeometryObserver {
    val regions = mutableStateMapOf<Any, Pair<String, Rect>>()
    override fun update(key: Any, region: String, bounds: Rect?) {
        if (bounds == null) regions.remove(key)
        else if (regions[key] != (region to bounds)) regions[key] = region to bounds
    }
}

@Composable
fun PlaygroundIsland(snapshot: PlaygroundSnapshot, controller: PlaygroundController,
                     observer: PlaygroundGeometry? = null, modifier: Modifier = Modifier) {
    val o = snapshot.options
    val base = AppTheme.island
    val tokens = base.copy(
        compactHeight = o.compactHeight.dp, compactMaxWidth = o.maximumWidth.dp,
        expandedMaxWidth = o.maximumWidth.dp, compactHorizontalInset = o.horizontalPadding.dp,
        expandedContentInset = o.verticalPadding.dp, compactIconSize = o.iconSize.dp,
        compactContentGap = o.textGap.dp, expandedContentGap = o.actionGap.dp,
        cameraClearance = o.cameraClearance.dp, minimumActionTarget = o.actionSize.dp
    )
    val settings = IslandRenderSettings(
        width = o.widthOverride.dp.takeIf { o.manualSize }, height = o.heightOverride.dp.takeIf { o.manualSize },
        minimumWidth = o.minimumWidth.dp, maximumHeight = o.maximumHeight.dp,
        corner = o.corner.dp.takeIf { o.overrideCorner }, motion = o.motion.takeIf { o.motionOverride }
    )
    val systemMotionDisabled = android.os.Build.VERSION.SDK_INT >= 26 && !android.animation.ValueAnimator.areAnimatorsEnabled()
    val reduced = o.reducedMotion || systemMotionDisabled || (o.motionOverride && !o.motion.enabled)
    CompositionLocalProvider(LocalAppIsland provides tokens, LocalIslandRenderSettings provides settings,
        LocalIslandGeometryObserver provides observer, LocalReducedMotion provides reduced) {
        // Same renderer, mapping, controls and motion code as the real overlay.
        val source = snapshot.state
        val notifications = when (source) {
            is IslandState.Notification -> source.notifications
            is IslandState.NotificationWithMusic -> source.notifications
            else -> emptyList()
        }
        val activity = (source as? IslandState.LiveActivity)?.activity
        val count = if (snapshot.selected == PlaygroundCase.Minimal) 1 else notifications.size.coerceAtLeast(1)
        val duration = if (o.motionOverride) o.motion.effectiveDuration else 180
        androidx.compose.runtime.key("shared-island") {
            AnimatedVisibility(visible = source != IslandState.Hidden,
                enter = islandVisibilityEnter(reduced, settings.motion),
                exit = islandVisibilityExit(reduced, settings.motion)) {
                LayeredMiniIslands(state = snapshot.behavior, modifier = modifier, redacted = snapshot.layeredRedaction,
                    enabled = source is IslandState.Notification && source.isExpanded,
                    onEvent = { controller.dispatchEngineEvent(it) },
                    onPromote = { key, token -> controller.dispatchEngineEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.TapChild(key, token)) }) {
                DynamicIslandPill(
                    state = resolveOverlayDemoState(source, count),
                    onTap = { controller.interaction() }, onLongPress = { controller.interaction(longPress = true) },
                    onCompanionTap = { controller.interaction(companion = true) },
                    onCompanionLongPress = { controller.interaction(longPress = true) },
                    onSwipeDismiss = { controller.expand(false) }, onControlAction = controller::control,
                    notifications = notifications, unseenNotificationCount = count,
                    notificationActiveIndex = (source as? IslandState.Notification)?.activeIndex ?: 0,
                    onNotificationSelect = controller::selectNotification,
                    onNotificationAction = { _, action -> if (action.id == "mock-retry") controller.select(PlaygroundCase.Success) else controller.message("Mock action: ${action.label} · no external action") },
                    onNotificationDismiss = { controller.select(PlaygroundCase.Idle) },
                    musicTrack = (source as? IslandState.Music)?.track ?: (source as? IslandState.NotificationWithMusic)?.track,
                    musicIsPlaying = (source as? IslandState.Music)?.isPlaying ?: true,
                    callContact = (source as? IslandState.IncomingCall)?.contact ?: (source as? IslandState.OngoingCall)?.contact,
                    callDurationSeconds = (source as? IslandState.OngoingCall)?.durationSeconds ?: 83,
                    batteryPercent = (source as? IslandState.Charging)?.batteryPercent ?: 85,
                    premiumHoursRemaining = 0, timerLabel = "%02d:%02d".format(snapshot.timerSeconds / 60, snapshot.timerSeconds % 60), timerProgress = snapshot.timerSeconds / 300f,
                    timerRunning = activity?.isRunning ?: true,
                    liveActivityTitle = activity?.title.orEmpty(), liveActivitySubtitle = if (snapshot.selected == PlaygroundCase.Recording) "%02d:%02d · simulated".format(snapshot.recordingSeconds / 60, snapshot.recordingSeconds % 60) else activity?.subtitle.orEmpty(),
                    liveActivityProgress = activity?.progress, liveActivityInfo = activity,
                    onMusicSeek = { controller.message("Mock seek: ${(it * 100).toInt()}%") },
                    onSystemTileClick = { controller.message("Mock tile: ${it.title} · system setting unchanged") },
                    onUtilityAction = { controller.message("Mock utility: ${it.name} · no external action") },
                    onOpenNotifications = { controller.select(PlaygroundCase.Expanded) }
                )
                }
            }
        }
    }
}

/** Scales a virtual-density phone into the available tool pane, without changing its layout constraints. */
@Composable
private fun ScaledPhone(virtualWidthPx: Int, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        val child = measurables.single().measure(Constraints(minWidth = virtualWidthPx, maxWidth = virtualWidthPx))
        val scale = minOf(constraints.maxWidth.toFloat() / virtualWidthPx,
            if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() / child.height.coerceAtLeast(1) else 1f, 1f)
        layout(constraints.maxWidth, (child.height * scale).toInt()) {
            child.placeWithLayer(((constraints.maxWidth - virtualWidthPx * scale) / 2).toInt(), 0) {
                scaleX = scale; scaleY = scale; transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
fun PlaygroundPhonePreview(snapshot: PlaygroundSnapshot, controller: PlaygroundController,
                           geometry: PlaygroundGeometry, onSize: (String) -> Unit) {
    val o = snapshot.options
    var viewportBounds by remember { mutableStateOf(Rect.Zero) }
    var islandSize by remember { mutableStateOf(IntSize.Zero) }
    var phoneBounds by remember { mutableStateOf(Rect.Zero) }
    val config = Configuration(LocalConfiguration.current).apply { screenWidthDp = o.width; screenHeightDp = 850 }
    val cameraWidth = if (o.camera == CameraPreset.Pill) 64f else if (o.camera == CameraPreset.None) 0f else 24f
    val cameraLeft = if (o.camera == CameraPreset.Left) 16f else (o.width - cameraWidth) / 2
    val hole = IslandCutout((cameraLeft * o.density).toInt(), 0,
        ((cameraLeft + cameraWidth) * o.density).toInt(), (28 * o.density).toInt())
    val placement = IslandPlacementPolicy.resolve((o.width * o.density).toInt(), (850 * o.density).toInt(),
        islandSize.width, islandSize.height, 0, (11 * o.density).toInt(),
        snapshot.state.presentationMode == IslandPresentationMode.EXPANDED, (28 * o.density).toInt(),
        (11 * o.density).toInt(), (o.cameraClearance * o.density).toInt(),
        if (cameraWidth > 0) listOf(hole) else emptyList())
    val height = maxOf(140f, (placement.top + islandSize.height) / o.density + 20).coerceAtMost(700f)
    Box(Modifier.fillMaxWidth().testTag("playground_preview")
        .onGloballyPositioned { viewportBounds = it.boundsInRoot() }) {
        ScaledPhone((o.width * o.density).toInt()) {
            CompositionLocalProvider(LocalConfiguration provides config,
                LocalDensity provides Density(o.density, o.fontScale),
                LocalLayoutDirection provides if (o.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                AppTheme(darkTheme = true) {
                Box(Modifier.width(o.width.dp).height(height.dp).onGloballyPositioned { phoneBounds = it.boundsInRoot() }.background(AppTheme.colors.stageMid,
                    RoundedCornerShape(AppTheme.radius.lg)).border(1.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.lg))) {
                    Row(Modifier.fillMaxWidth().height(28.dp).padding(start = if (o.camera == CameraPreset.Left) (cameraLeft + cameraWidth + 8).dp else 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("12:00", style = AppTheme.typography.caption, color = Color.White)
                        Text("Wi-Fi   100%", style = AppTheme.typography.caption, color = Color.White)
                    }
                    if (cameraWidth > 0) Box(Modifier.offset(cameraLeft.dp, 2.dp).size(cameraWidth.dp, 24.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp)))
                    Box(Modifier.align(Alignment.TopCenter).offset(y = (placement.top / o.density).dp)
                        .wrapContentHeight(align = Alignment.Top, unbounded = true)) {
                        PlaygroundIsland(snapshot, controller, geometry, Modifier.onSizeChanged {
                            if (islandSize != it) { islandSize = it; onSize("${(it.width / o.density).toInt()} × ${(it.height / o.density).toInt()}dp") }
                        })
                    }
                    if (o.guides) Canvas(Modifier.matchParentSize()) {
                        val cyan = Color.Cyan.copy(alpha = .6f)
                        drawLine(cyan, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
                        drawLine(cyan, Offset(0f, 28.dp.toPx()), Offset(size.width, 28.dp.toPx()), 1.dp.toPx())
                        if (cameraWidth > 0) drawRect(Color.Red.copy(alpha = .8f),
                            Offset((cameraLeft - o.cameraClearance).dp.toPx(), 0f),
                            Size((cameraWidth + o.cameraClearance * 2).dp.toPx(), (28 + o.cameraClearance).dp.toPx()),
                            style = Stroke(1.dp.toPx()))
                    }
                }
                }
            }
        }
        if (o.guides) Canvas(Modifier.matchParentSize()) {
            geometry.regions.values.forEach { (name, rect) ->
                val color = if (name == "Shell") Color.Cyan else Color(0xFFFFCA6B)
                val local = rect.translate(-viewportBounds.left, -viewportBounds.top)
                drawRect(color.copy(alpha = .8f), local.topLeft, local.size, style = Stroke(1f))
                if (name == "Shell") {
                    val scale = phoneBounds.width / (o.width * o.density)
                    val inset = (if (snapshot.state.presentationMode == IslandPresentationMode.COMPACT) o.horizontalPadding else o.verticalPadding) * o.density * scale
                    val band = inset.coerceAtMost(local.width / 2)
                    drawRect(Color.Magenta.copy(alpha = .12f), local.topLeft, Size(band, local.height))
                    drawRect(Color.Magenta.copy(alpha = .12f), Offset(local.right - band, local.top), Size(band, local.height))
                }
            }
        }
    }
}
