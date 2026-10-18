package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
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
import androidx.compose.foundation.layout.fillMaxWidth
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
                AppleIcon(glyph = AppleGlyph.Airplane, tint = Color(0xFFFB8B28), size = 36.dp * scale)
                Spacer(modifier = Modifier.width(18.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turn Off Airplane Mode or Use",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = "Wi-Fi to Access Data",
                        color = Color(0xFFA4A4A9),
                        fontSize = (14f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF2C2C2D))
                    .clickable { onControlAction(IslandControlAction.OpenSettings) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open Settings",
                    color = Color.White,
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp * scale))
                    Text(
                        text = subtitle.ifBlank { "MacBook Pro" },
                        color = Color(0xFF818383),
                        fontSize = (13.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AppleIcon(
                    glyph = AppleGlyph.Laptop,
                    tint = Color(0xFF8F8F8F),
                    size = 24.dp * scale,
                    modifier = Modifier.padding(end = 15.dp * scale)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF1A1C2D))
                    .clickable { onControlAction(IslandControlAction.ScreenMirroringStop) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stop Mirroring",
                    color = Color(0xFF37A3DE),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                    glyph = AppleGlyph.PersonalHotspot,
                    tint = Color(0xFF37C058),
                    size = 38.dp * scale
                )
                Spacer(modifier = Modifier.width(19.3.dp * scale))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mobile Data is Turned Off",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (20f * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp * scale))
                    Text(
                        text = "Turn on mobile data or use Wi-Fi\nto access data.",
                        color = Color(0xFF818383),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = (16.5f * scale).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.5.dp * scale))
                        .background(Color(0xFF2C2C2D))
                        .clickable { onControlAction(IslandControlAction.MobileDataOk) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.5.dp * scale))
                        .background(Color(0xFF1A1C2D))
                        .clickable { onControlAction(IslandControlAction.MobileDataSettings) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Settings",
                        color = Color(0xFF37A3DE),
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
                        fontSize = (16.5f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = IslandColors.TextSecondary,
                            fontSize = (12f * scale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp * scale)
                    .clip(RoundedCornerShape(21.5.dp * scale))
                    .background(Color(0xFF1D1011))
                    .clickable { onControlAction(IslandControlAction.TransitEndRoute) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End Route",
                    color = Color(0xFFFA3532),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun TurnByTurnNavigationContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var activeLane by remember { mutableStateOf(3) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp * scale, vertical = 18.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(
                    glyph = AppleGlyph.NavigationLeft,
                    tint = if (activeLane == 0) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 0
                        onControlAction(IslandControlAction.NavigationLeft)
                    }
                )
                AppleIcon(
                    glyph = AppleGlyph.NavigationStraight,
                    tint = if (activeLane == 1) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 1
                        onControlAction(IslandControlAction.NavigationStraight)
                    }
                )
                AppleIcon(
                    glyph = AppleGlyph.NavigationStraight,
                    tint = if (activeLane == 2) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 2
                        onControlAction(IslandControlAction.NavigationStraight)
                    }
                )
                AppleIcon(
                    glyph = AppleGlyph.NavigationRight,
                    tint = if (activeLane == 3) Color.White else Color(0xFF838388),
                    size = 30.dp * scale,
                    modifier = Modifier.clickable {
                        activeLane = 3
                        onControlAction(IslandControlAction.NavigationRight)
                    }
                )
            }

            Text(
                text = "90 ft",
                color = Color.White,
                fontSize = (26f * scale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 51.dp * scale, height = 39.dp * scale)
                        .clip(RoundedCornerShape(6.dp * scale)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.navigation_shield),
                        contentDescription = "Interstate 280",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "North",
                        color = Color.White,
                        fontSize = (16.5f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "San Francisco",
                        color = Color(0xFFA4A4A9),
                        fontSize = (13.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.1).sp
                    )
                }
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

@Composable
internal fun VoiceMemoExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonSize = (50.5.dp * scale)
        val squareSize = (24.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 20.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                val barHeights = listOf(25, 20, 16, 20, 25, 20, 14, 10, 16, 22, 27, 15, 19)
                barHeights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp * scale)
                            .height((h.toFloat() * scale).dp)
                            .clip(RoundedCornerShape(1.5.dp * scale))
                            .background(Color(0xFFFA3532))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(3.dp * scale)
                        .height(12.dp * scale)
                        .clip(RoundedCornerShape(1.5.dp * scale))
                        .background(Color(0xFFFA3532).copy(alpha = 0.4f))
                )
                repeat(11) {
                    Box(
                        modifier = Modifier
                            .size(3.dp * scale)
                            .clip(RoundedCornerShape(1.5.dp * scale))
                            .background(Color(0xFF5A5A5A).copy(alpha = 0.4f))
                    )
                }
            }

            Text(
                text = "00:06",
                color = Color(0xFFFA3532),
                fontSize = (16.5f * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = 31.dp * scale)
            )

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .border(width = 2.5.dp * scale, color = Color.White, shape = CircleShape)
                    .clickable { onControlAction(IslandControlAction.VoiceMemoStop) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(squareSize)
                        .clip(RoundedCornerShape(4.dp * scale))
                        .background(Color(0xFFFA3532))
                )
            }
        }
    }
}

@Composable
internal fun ScreenRecordingExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    val pulseState = rememberBreathing(min = 0.85f, max = 1.0f, durationMillis = 1000)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonSize = (50.5.dp * scale)
        val squareSize = (24.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 20.dp * scale, top = 16.dp * scale, bottom = 16.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp * scale)
                            .graphicsLayer { alpha = pulseState.value }
                            .clip(CircleShape)
                            .background(Color(0xFFFA3532))
                    )
                    Text(
                        text = "00:06",
                        color = Color(0xFFFA3532),
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(3.dp * scale))

                Text(
                    text = "Screen Recording",
                    color = Color.White,
                    fontSize = (15f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp * scale))

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .border(width = 2.5.dp * scale, color = Color.White, shape = CircleShape)
                    .clickable { onControlAction(IslandControlAction.ScreenRecordingStop) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(squareSize)
                        .clip(RoundedCornerShape(4.dp * scale))
                        .background(Color(0xFFFA3532))
                )
            }
        }
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
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "100%",
                        color = Color(0xFFA4A4A9),
                        fontSize = (13f * scale).sp,
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp * scale, vertical = 15.dp * scale),
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
                        size = 22.dp * scale
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = "Do Not Disturb",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = "On",
                        color = Color(0xFF37A3DE),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(70.dp * scale)
                    .height(34.dp * scale)
                    .clip(RoundedCornerShape(17.dp * scale))
                    .background(Color(0xFF1E2854)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "On",
                    color = Color(0xFF37A3DE),
                    fontSize = (14f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun VideoRemoteExpandedContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.35f) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val thumbSize = (53.dp * scale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp * scale, bottom = 12.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp * scale, end = 35.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .clip(RoundedCornerShape(8.dp * scale))
                            .background(Color(0xFF2C2C2D))
                            .border(0.75.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp * scale))
                    )

                    Spacer(modifier = Modifier.width(21.4.dp * scale))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Video",
                            color = Color.White,
                            fontSize = (16f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(5.dp * scale))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppleIcon(glyph = AppleGlyph.Laptop, tint = Color(0xFF898989), size = 14.dp * scale)
                            Spacer(modifier = Modifier.width(5.dp * scale))
                            Text(
                                text = "Samsung TV",
                                color = Color(0xFF898989),
                                fontSize = (13.5f * scale).sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                LiveEqualizerMini(
                    width = 20.dp * scale,
                    height = 22.dp * scale,
                    color = Color.White,
                    animated = isPlaying
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp * scale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0:50",
                    color = Color(0xFF898989),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(14.dp * scale))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.5.dp * scale)
                        .clip(RoundedCornerShape(3.25.dp * scale))
                        .background(Color(0xFF3F3F3F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.25.dp * scale))
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp * scale))

                Text(
                    text = "-3:11",
                    color = Color(0xFF898989),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp * scale)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 8.8.dp * scale),
                    horizontalArrangement = Arrangement.spacedBy(27.dp * scale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = (progress - 0.05f).coerceAtLeast(0f)
                                onControlAction(IslandControlAction.VideoSkipBackward)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.SkipBack15,
                            tint = Color.White,
                            size = 36.dp * scale
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp * scale)
                            .clickable {
                                isPlaying = !isPlaying
                                onControlAction(IslandControlAction.VideoPlayPause)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = if (isPlaying) AppleGlyph.Pause else AppleGlyph.Play,
                            tint = Color.White,
                            size = 40.dp * scale
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp * scale)
                            .clickable {
                                progress = (progress + 0.05f).coerceAtMost(1f)
                                onControlAction(IslandControlAction.VideoSkipForward)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.SkipForward15,
                            tint = Color.White,
                            size = 36.dp * scale
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp * scale)
                        .size(40.dp * scale)
                        .clickable { onControlAction(IslandControlAction.MusicAirPlay) },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.AirPlay,
                        tint = Color.White,
                        size = 25.5.dp * scale
                    )
                }
            }
        }
    }
}

@Composable
internal fun AirDropActivityContent(onControlAction: (IslandControlAction) -> Unit = {}) {
    var isPaused by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)
        val ringSize = (51.33.dp * scale)
        val innerBoxSize = (22.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp * scale, end = 16.dp * scale),
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

                Spacer(modifier = Modifier.width(10.dp * scale))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    Text(
                        text = "AirDrop",
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "from Chris",
                        color = Color(0xFFA4A4A9),
                        fontSize = (15f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clickable {
                        isPaused = !isPaused
                        onControlAction(IslandControlAction.AirDropPause)
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.67f * scale
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
                        .clip(RoundedCornerShape(6.22.dp * scale))
                        .background(Color(0xFF37A3DE).copy(alpha = if (isPaused) 0.55f else 1f))
                        .clickable {
                            isPaused = !isPaused
                            onControlAction(IslandControlAction.AirDropPause)
                        }
                )
            }
        }
    }
}

@Composable
internal fun AirPodsConnectedContent(
    title: String = "Ladislav's AirPods",
    subtitle: String = "Connected · 75%",
    batteryProgress: Float? = 0.75f,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)
        val ringSize = (48.6.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp * scale, end = 20.dp * scale),
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
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.airpods_art),
                        contentDescription = "AirPods",
                        modifier = Modifier.size(30.dp * scale),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = subtitle.substringBefore(" · ").ifBlank { "Connected" },
                        color = Color(0xFFA4A4A9),
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = title.ifBlank { "Ladislav's AirPods" },
                        color = Color.White,
                        fontSize = (16f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clickable { onControlAction(IslandControlAction.AirPodsOpen) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.42f * scale
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
                        sweepAngle = 360f * (batteryProgress ?: 0.75f).coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = batteryProgress?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                    color = Color(0xFF37C058),
                    fontSize = (13f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
internal fun SatelliteConnectionContent(
    title: String = "",
    subtitle: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)
        val badgeSize = (41.4.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp * scale, end = 18.dp * scale),
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

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column {
                    Text(
                        text = title.ifBlank { "CONNECTED" },
                        color = Color(0xFF37C058),
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = subtitle.ifBlank { "Keep pointing at Satellite" },
                        color = Color.White,
                        fontSize = (15.5f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    size = 18.dp * scale
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val radarSize = (56.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp * scale, end = 24.dp * scale)
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

            Spacer(modifier = Modifier.width(14.dp * scale))

            Text(
                text = title.ifBlank { "Find My iPhone Alert" },
                color = Color.White,
                fontSize = (17f * scale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun MovedToIPhoneContent(
    title: String = "",
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp * scale, end = 20.dp * scale),
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
                        modifier = Modifier.size(30.dp * scale),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(14.dp * scale))

                Text(
                    text = title.ifBlank { "Moved to iPhone" },
                    color = Color(0xFFA4A4A9),
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .width(70.dp * scale)
                    .height(34.dp * scale)
                    .clip(RoundedCornerShape(17.dp * scale))
                    .background(Color(0xFF1E2854))
                    .clickable { onControlAction(IslandControlAction.MovedUndo) },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Undo,
                    tint = Color(0xFF37A3DE),
                    size = 18.dp * scale
                )
            }
        }
    }
}

