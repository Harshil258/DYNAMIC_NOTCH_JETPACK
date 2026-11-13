package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.motion.pressScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import ai.emots.kishan_dynamic.ui.theme.contextualRadarLeadingInset
import ai.emots.kishan_dynamic.ui.theme.contextualRadarVerticalInset
import ai.emots.kishan_dynamic.ui.theme.navigationLaneTopInset
import ai.emots.kishan_dynamic.ui.theme.navigationBottomInset
import ai.emots.kishan_dynamic.ui.theme.navigationLaneInset
import ai.emots.kishan_dynamic.ui.theme.navigationContentInset
import ai.emots.kishan_dynamic.ui.theme.navigationInactiveColor
import ai.emots.kishan_dynamic.ui.theme.navigationSubtitleColor
import ai.emots.kishan_dynamic.ui.theme.videoRegionGap
import ai.emots.kishan_dynamic.ui.theme.mediaControlLeadingInset
import ai.emots.kishan_dynamic.ui.theme.mediaControlTrailingInset
import ai.emots.kishan_dynamic.ui.theme.deviceBatteryVerticalInset
import ai.emots.kishan_dynamic.ui.theme.deviceBatteryTrailingInset
import ai.emots.kishan_dynamic.ui.theme.deviceTransferLeadingInset
import ai.emots.kishan_dynamic.ui.theme.deviceTransferVerticalInset
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import kotlin.math.roundToInt

@Composable
internal fun AirplaneAlertContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(10.7.dp * scale))
                AppleIcon(glyph = AppleGlyph.Airplane, tint = Color(0xFFFB8B28), size = AppleGlyph.Airplane.figmaInkSize())
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turn Off Airplane Mode or Use",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = "Wi-Fi to Access Data",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
            IslandActionPill(
                label = "Open Settings",
                onClick = { onControlAction(IslandControlAction.OpenSettings) },
                glyph = AppleGlyph.Settings,
                style = IslandActionStyle.Secondary,
                visualHeight = 42.dp * scale,
                fontSize = 16.sp,
                contentDescription = "Open Settings"
            )
        }
    }
}

@Composable
internal fun ScreenMirroringContent(
    title: String = "",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.3.dp * scale))
                AppleIcon(glyph = AppleGlyph.ScreenMirroring, tint = Color(0xFF37A3DE), size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "Screen Mirroring" },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = subtitle.ifBlank { "MacBook Pro" },
                        color = Color(0xFF818383),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                AppleIcon(
                    glyph = AppleGlyph.Laptop,
                    tint = Color(0xFF8F8F8F),
                    size = 24.dp * scale,
                    modifier = Modifier.padding(end = 15.dp * scale)
                )
            }
            IslandActionPill(
                label = "Stop Mirroring",
                onClick = { onControlAction(IslandControlAction.ScreenMirroringStop) },
                glyph = AppleGlyph.Stop,
                style = IslandActionStyle.Destructive,
                accent = IslandSemanticAccent.Red,
                visualHeight = 42.dp * scale,
                fontSize = 16.sp,
                contentDescription = "Stop Screen Mirroring"
            )
        }
    }
}

@Composable
internal fun MobileDataContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp * scale,
                    end = 16.dp * scale,
                    top = 28.dp * scale,
                    bottom = 18.dp * scale
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(18.8.dp * scale))
                AppleIcon(
                    glyph = AppleGlyph.Cellular,
                    tint = Color(0xFF37C058),
                    size = 38.dp * scale
                )
                Spacer(modifier = Modifier.width(19.3.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mobile Data is Turned Off",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp * scale))
                    Text(
                        text = "Turn on mobile data or use Wi-Fi\nto access data.",
                        color = Color(0xFF818383),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.5.sp,
                    )
                }
            }

            IslandControlRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.md)
            ) {
                IslandActionPill(
                    label = "OK",
                    onClick = { onControlAction(IslandControlAction.MobileDataOk) },
                    modifier = Modifier.weight(1f),
                    style = IslandActionStyle.Tertiary,
                    visualHeight = 42.dp * scale,
                    fontSize = 16.sp,
                    contentDescription = "Dismiss mobile data notice"
                )
                IslandActionPill(
                    label = "Settings",
                    onClick = { onControlAction(IslandControlAction.MobileDataSettings) },
                    modifier = Modifier.weight(1f),
                    glyph = AppleGlyph.Settings,
                    style = IslandActionStyle.Primary,
                    accent = IslandSemanticAccent.Cyan,
                    visualHeight = 42.dp * scale,
                    fontSize = 16.sp,
                    contentDescription = "Open Cellular Settings"
                )
            }
        }
    }
}

@Composable
internal fun TransitRouteContent(
    title: String = "Prague Main Train Station",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 22.dp * scale, bottom = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(glyph = AppleGlyph.TransitTrain, tint = Color.White, size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "Prague Main Train Station" },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = IslandColors.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            IslandActionPill(
                label = "End Route",
                onClick = { onControlAction(IslandControlAction.TransitEndRoute) },
                glyph = AppleGlyph.Close,
                style = IslandActionStyle.Destructive,
                accent = IslandSemanticAccent.Red,
                visualHeight = 42.dp * scale,
                fontSize = 16.sp,
                contentDescription = "End Route"
            )
        }
    }
}

/** Comparison fixture for Figma19:1034; live source navigation uses its own supplied fields. */
@Composable
internal fun TurnByTurnNavigationContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var activeLane by remember { mutableStateOf(3) }
    val tokens = AppTheme.island
    Column(Modifier.fillMaxWidth().padding(top = tokens.navigationLaneTopInset,
        bottom = tokens.navigationBottomInset), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        // Lane direction is physical. Locale direction must not turn a right maneuver into a left one.
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = tokens.navigationLaneInset),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val lanes = listOf(
                    Triple(AppleGlyph.NavigationLeft, IslandControlAction.NavigationLeft, "Left lane"),
                    Triple(AppleGlyph.NavigationStraight, IslandControlAction.NavigationStraight, "First straight lane"),
                    Triple(AppleGlyph.NavigationStraight, IslandControlAction.NavigationStraight, "Second straight lane"),
                    Triple(AppleGlyph.NavigationRight, IslandControlAction.NavigationRight, "Right lane")
                )
                lanes.forEachIndexed { index, (glyph, action, label) ->
                    androidx.compose.material3.IconButton(
                        onClick = { activeLane = index; onControlAction(action) },
                        enabled = canRenderIslandControl(action),
                        modifier = Modifier.size(tokens.minimumActionTarget).semantics { contentDescription = label }
                    ) {
                        AppleIcon(glyph, tint = if (activeLane == index) Color.White else IslandColors.navigationInactiveColor,
                            size = if (glyph == AppleGlyph.NavigationStraight) 34.dp else 33.dp)
                    }
                }
            }
        }
        Text("90 ft", color = Color.White,
            style = AppTheme.typography.body.copy(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
            modifier = Modifier.padding(horizontal = tokens.navigationContentInset))
        Row(Modifier.fillMaxWidth().padding(horizontal = tokens.navigationContentInset),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Image(painterResource(ai.emots.kishan_dynamic.R.drawable.navigation_shield),
                contentDescription = "Interstate 280", modifier = Modifier.size(51.dp,39.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f)) {
                Text("North", color = Color.White, style = AppTheme.typography.body.copy(
                    fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold))
                Text("San Francisco", color = IslandColors.navigationSubtitleColor, style = AppTheme.typography.bodySmall.copy(
                    fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium))
            }
        }
    }
}

@Composable
internal fun OngoingCallBannerContent() {
    OngoingCallIslandBanner(
        name = "Tamia Castillo",
        duration = "02:45"
    )
}

/** Reference recording banners own their vertical padding; no fillMaxSize in an unbounded viewport. */
@Composable
private fun RecordingStopControl(action: IslandControlAction, onControlAction: (IslandControlAction) -> Unit) {
    if (!canRenderIslandControl(action)) return
    val tokens = AppTheme.island
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier.size(tokens.recordingControlSize)
            .pressScale(interaction, pressedScale = 0.94f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = { onControlAction(action) }
            )
            .semantics { contentDescription = if (action == IslandControlAction.VoiceMemoStop) "Stop voice memo" else "Stop screen recording" },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(tokens.recordingStopSize).clip(RoundedCornerShape(5.dp)).background(IslandColors.CapsuleRed))
        }
    }
}

@Composable
internal fun VoiceMemoExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    val tokens = AppTheme.island
    Row(
        Modifier.fillMaxWidth().wrapContentHeight().padding(start = tokens.recordingLeadingInset,
            end = tokens.recordingTrailingInset, top = tokens.recordingVerticalInset, bottom = tokens.recordingVerticalInset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.weight(1f).height(27.dp)) {
            val nativeWidth = 123.dp.toPx()
            val ratio = minOf(1f, size.width / nativeWidth)
            val heights = listOf(25,20,16,20,25,20,14,10,16,22,27,15,19,12) + List(11) { 3 }
            heights.forEachIndexed { index, height ->
                val w = 3.dp.toPx() * ratio
                val h = height.dp.toPx()
                drawRoundRect(
                    color = IslandColors.CapsuleRed.copy(alpha = if (index < 13) 1f else if (index == 13) .4f else .16f),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) size.width - index * 5.dp.toPx() * ratio - w
                        else index * 5.dp.toPx() * ratio, (size.height - h) / 2),
                    size = androidx.compose.ui.geometry.Size(w,h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2,w / 2)
                )
            }
        }
        Spacer(Modifier.width(AppTheme.spacing.sm))
        Text("00:06", color = IslandColors.CapsuleRed,
            style = AppTheme.typography.islandTime.copy(fontSize = 18.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
        if (canRenderIslandControl(IslandControlAction.VoiceMemoStop)) Spacer(Modifier.width(AppTheme.spacing.md))
        RecordingStopControl(IslandControlAction.VoiceMemoStop,onControlAction)
    }
}

@Composable
internal fun ScreenRecordingExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    val tokens = AppTheme.island
    val pulseState = rememberBreathing(min = .85f,max = 1f,durationMillis = 1000)
    Row(
        Modifier.fillMaxWidth().wrapContentHeight().padding(start = tokens.recordingLeadingInset,
            end = tokens.recordingTrailingInset,top = tokens.recordingVerticalInset,bottom = tokens.recordingVerticalInset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f),verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).graphicsLayer { alpha = pulseState.value }.clip(CircleShape).background(IslandColors.CapsuleRed))
                Text("00:06",color = IslandColors.CapsuleRed,
                    style = AppTheme.typography.islandTime.copy(fontSize = 18.sp,lineHeight = 18.sp,fontWeight = FontWeight.Normal))
            }
            Text("Screen Recording",color = Color.White,
                style = AppTheme.typography.body.copy(fontSize = 18.sp,lineHeight = 18.sp,fontWeight = FontWeight.Normal))
        }
        if (canRenderIslandControl(IslandControlAction.ScreenRecordingStop)) Spacer(Modifier.width(AppTheme.spacing.md))
        RecordingStopControl(IslandControlAction.ScreenRecordingStop,onControlAction)
    }
}

@Composable
internal fun ShortcutExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val iconSize = (38.dp * scale)
        val checkBadgeSize = (47.8.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp * scale, vertical = 17.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(iconSize), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(27.dp * scale)
                            .offset(y = 5.dp * scale)
                            .rotate(45f)
                            .clip(RoundedCornerShape(5.dp * scale))
                            .background(Color(0xFFA4A4A9))
                    )
                    Box(
                        modifier = Modifier
                            .size(27.dp * scale)
                            .offset(y = (-5).dp * scale)
                            .rotate(45f)
                            .clip(RoundedCornerShape(5.dp * scale))
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "Shortcut",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "100%",
                        color = Color(0xFFA4A4A9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(checkBadgeSize)
                    .clip(CircleShape)
                    .border(3.dp * scale, Color(0xFFF0F0F5), CircleShape)
                    .clickable { onControlAction(IslandControlAction.ShortcutOpen) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(25.dp * scale)) {
                    val stroke = 3.3.dp.toPx()
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.14f, size.height * 0.50f),
                        end = Offset(size.width * 0.39f, size.height * 0.76f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.39f, size.height * 0.76f),
                        end = Offset(size.width * 0.86f, size.height * 0.18f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
internal fun FocusModeExpandedContent() {
    Box(modifier = Modifier.fillMaxWidth()) {
        val avatarSize = (44.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.island.contextualRadarLeadingInset, vertical = AppTheme.island.contextualRadarVerticalInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color(0xFF5E5CE6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Moon,
                        tint = Color(0xFF5E5CE6),
                        size = 22.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Do Not Disturb",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "On",
                        color = Color(0xFF37A3DE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFF1E2854)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "On",
                    color = Color(0xFF37A3DE),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun VideoRemoteExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(.35f) }
    val routeVector = remember { videoRouteVector() }
    val largeType = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
    val deviceLabel: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            ai.emots.kishan_dynamic.ui.icons.IslandIcon(vector = routeVector, tint = IslandColors.Gray, size = 17.102.dp)
            Text("Samsung TV", color = IslandColors.Gray, style = AppTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 18.sp))
        }
    }
    Column(Modifier.fillMaxWidth().padding(top = AppTheme.spacing.xxxl, bottom = AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.island.videoRegionGap)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)) {
            Box(Modifier.size(53.dp).clip(RoundedCornerShape(AppTheme.spacing.sm)).background(IslandColors.Gray4))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                Text("Video", color = Color.White, style = AppTheme.typography.body.copy(fontSize = 16.sp,
                    lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
                if (!largeType) deviceLabel()
            }
            LiveEqualizerMini(width = 26.dp, height = 18.dp, color = IslandColors.Gray, animated = isPlaying)
        }
        if (largeType) Box(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl)) { deviceLabel() }
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Text("0:50", color = IslandColors.Gray, style = AppTheme.typography.caption.copy(fontSize = 12.sp, lineHeight = 18.sp))
            Box(Modifier.weight(1f).height(6.5.dp).clip(CircleShape).background(IslandColors.Gray4)) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(Color.White))
            }
            Text("-3:11", color = IslandColors.Gray, style = AppTheme.typography.caption.copy(fontSize = 12.sp, lineHeight = 18.sp))
        }
        }
        Row(Modifier.fillMaxWidth().padding(start = AppTheme.island.mediaControlLeadingInset,
            end = AppTheme.island.mediaControlTrailingInset), verticalAlignment = Alignment.CenterVertically) {
            // The reference reserves one leading control region before its transport cluster.
            Spacer(Modifier.weight(1f))
            val controls = listOf(
                Triple(IslandControlAction.VideoSkipBackward, AppleGlyph.SkipBack15, "Rewind 15 seconds"),
                Triple(IslandControlAction.VideoPlayPause, if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play, if (isPlaying) "Pause video" else "Play video"),
                Triple(IslandControlAction.VideoSkipForward, AppleGlyph.SkipForward15, "Forward 15 seconds"),
                Triple(IslandControlAction.MusicAirPlay, AppleGlyph.AirPlay, "Video output")
            )
            controls.forEach { (action, glyph, label) ->
                Box(Modifier.weight(1f).heightIn(min = AppTheme.island.minimumActionTarget), contentAlignment = Alignment.Center) {
                    if (canRenderIslandControl(action)) androidx.compose.material3.IconButton(
                        onClick = {
                            when (action) {
                                IslandControlAction.VideoPlayPause -> isPlaying = !isPlaying
                                IslandControlAction.VideoSkipBackward -> progress = (progress - .05f).coerceAtLeast(0f)
                                IslandControlAction.VideoSkipForward -> progress = (progress + .05f).coerceAtMost(1f)
                                else -> Unit
                            }
                            onControlAction(action)
                        }, modifier = Modifier.size(AppTheme.island.minimumActionTarget).semantics { contentDescription = label }
                    ) {
                        AppleIcon(glyph, tint = Color.White, size = when(action) {
                            IslandControlAction.VideoPlayPause -> 32.dp
                            IslandControlAction.MusicAirPlay -> 24.dp
                            else -> 29.dp
                        })
                    }
                }
            }
        }
    }
}

@Composable
internal fun AirDropActivityContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var isPaused by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        val radarSize = (56.dp)
        val buttonTouchSize = (56.dp)
        val ringSize = (56.dp)
        val innerBoxSize = (22.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.island.contextualRadarVerticalInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(ai.emots.kishan_dynamic.R.drawable.airdrop_icon),
                    contentDescription = "AirDrop",
                    modifier = Modifier.size(radarSize),
                    contentScale = ContentScale.FillBounds
                )

                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

                Text(
                    text = androidx.compose.ui.text.buildAnnotatedString {
                        append("AirDrop ")
                        pushStyle(androidx.compose.ui.text.SpanStyle(color = IslandColors.Gray))
                        append("from Chi")
                        pop()
                    },
                    modifier = Modifier.weight(1f), color = Color.White,
                    style = AppTheme.typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                )
            }

            Box(
                modifier = Modifier
                    .size(buttonTouchSize)
                    .clickable {
                        isPaused = !isPaused
                        onControlAction(IslandControlAction.AirDropPause)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(ringSize),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 4.42.dp.toPx()
                        val pad = strokeW / 2f
                        val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)

                        drawArc(
                            color = Color(0xFF37A3DE).copy(alpha = 0.5f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = strokeW)
                        )

                        drawArc(
                            color = Color(0xFF37A3DE),
                            startAngle = -90f,
                            sweepAngle = 190f,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(innerBoxSize)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF37A3DE).copy(alpha = if (isPaused) 0.55f else 1f))
                    )
                }
            }
        }
    }
}

@Composable
internal fun AirPodsConnectedContent(
    title: String = "Ladislav’s Air Pods",
    subtitle: String = "Connected · 75%",
    batteryProgress: Float? = 0.75f,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val avatarSize = (44.dp)
        val ringSize = 57.44.dp + AppTheme.spacing.xxl * (androidx.compose.ui.platform.LocalDensity.current.fontScale - 1f).coerceAtLeast(0f)

        val identity: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.airpods_art),
                        contentDescription = "AirPods",
                        modifier = Modifier.size(30.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = subtitle.substringBefore(" · ").ifBlank { "Connected" },
                        color = Color(0xFFA4A4A9),
                        fontSize = 14.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.1).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title.ifBlank { "Ladislav’s Air Pods" },
                        color = Color.White,
                        fontSize = 14.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                    )
                }
            }
        }
        val battery: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clickable { onControlAction(IslandControlAction.AirPodsOpen) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.42.dp.toPx()
                    val pad = strokeW / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)

                    drawArc(
                        color = Color(0xFF13351F),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )

                    drawArc(
                        color = Color(0xFF37C058),
                        startAngle = -90f,
                        sweepAngle = 360f * (batteryProgress ?: 0f).coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = batteryProgress?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                    color = Color(0xFF37C058),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts.Text
                )
            }
        }
        val contentModifier = Modifier.fillMaxWidth().padding(start = AppTheme.spacing.xxl,
            end = AppTheme.island.deviceBatteryTrailingInset, top = AppTheme.island.deviceBatteryVerticalInset,
            bottom = AppTheme.island.deviceBatteryVerticalInset)
        if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) {
            Column(contentModifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                identity()
                Box(Modifier.align(Alignment.End)) { battery() }
            }
        } else Row(contentModifier, verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Box(Modifier.weight(1f)) { identity() }
            battery()
        }
    }
}

@Composable
internal fun SatelliteConnectionContent(
    title: String = "",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val radarSize = (56.dp)
        val badgeSize = (41.4.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.island.contextualRadarLeadingInset, vertical = AppTheme.island.contextualRadarVerticalInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(radarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.satellite_radar),
                        contentDescription = "Satellite signal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "Connected" },
                        color = Color(0xFF37C058),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle.ifBlank { "Keep pointing at Satellite" },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.2).sp,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Color(0xFF0B2F16))
                    .clickable { onControlAction(IslandControlAction.SatelliteMessage) },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Chat,
                    tint = Color(0xFF37C058),
                    size = 18.dp
                )
            }
        }
    }
}

@Composable
internal fun FindMyAlertContent(
    title: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val radarSize = (56.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.island.contextualRadarLeadingInset, vertical = AppTheme.island.contextualRadarVerticalInset)
                .clickable { onControlAction(IslandControlAction.LiveActivityOpen) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(radarSize),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(ai.emots.kishan_dynamic.R.drawable.findmy_radar),
                    contentDescription = "Find My signal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            Text(
                text = title.ifBlank { "Find My iPhone Alert" },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.3).sp,
            )
        }
    }
}

@Composable
internal fun MovedToIPhoneContent(
    title: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val avatarSize = (44.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.island.deviceTransferLeadingInset, vertical = AppTheme.island.deviceTransferVerticalInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.moved_airpods_art),
                        contentDescription = "AirPods",
                        modifier = Modifier.size(30.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

                Text(
                    text = title.ifBlank { "Moved to iPhone" },
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFA4A4A9),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.2).sp,
                )
            }

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFF1E2854))
                    .clickable { onControlAction(IslandControlAction.MovedUndo) },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Undo,
                    tint = Color(0xFF37A3DE),
                    size = 18.dp
                )
            }
        }
    }
}


/** Screen/triangle routing ink from the original video label SVG; Samsung TV lettering excluded. */
private fun videoRouteVector() = ai.emots.kishan_dynamic.ui.icons.IslandVector("figma_video_route", listOf(
    ai.emots.kishan_dynamic.ui.icons.IslandPath(d = "M16.218 16.25L21.523 16.25C22.006 16.25 22.253 16.003 22.253 15.52L22.253 4.656C22.253 4.173 22.006 3.916 21.523 3.916L2.466 3.916C1.983 3.916 1.747 4.173 1.747 4.656L1.747 15.52C1.747 16.003 1.983 16.25 2.466 16.25L7.873 16.25L6.413 17.905L2.209 17.905C0.78 17.905 0 17.114 0 15.705L0 4.471C0 3.104 0.822 2.261 2.209 2.261L21.78 2.261C23.209 2.261 24 3.073 24 4.471L24 15.705C24 17.114 23.209 17.905 21.78 17.905L17.678 17.905L16.218 16.25ZM18.274 21.739L5.714 21.739C5.232 21.739 5.046 21.245 5.355 20.896L11.552 13.876C11.789 13.609 12.201 13.609 12.436 13.876L18.634 20.896C18.942 21.245 18.757 21.739 18.274 21.739Z", evenOdd = false),
))
