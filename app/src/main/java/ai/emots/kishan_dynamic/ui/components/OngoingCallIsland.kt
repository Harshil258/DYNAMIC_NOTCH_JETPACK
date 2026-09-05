package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppWindowSize
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.R

private val iOSGreen = IslandColors.Green
private val iOSRed = IslandColors.Red
private val iOSDarkGray = IslandColors.Gray4

enum class OngoingCallVariant {
    Standard,
    FaceTime,
    SharedMedia
}

/**
 * Ongoing call compact main pill
 */
@Composable
fun OngoingCallIslandMain(
    duration: String = "0:45",
    modifier: Modifier = Modifier
) {
    // Leading capsule of the split call activity: just the running timer.
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AppleIcon(
            glyph = AppleGlyph.Phone,
            tint = iOSGreen,
            size = 12.dp
        )
        AppText(
            text = duration,
            style = AppTheme.typography.islandTime,
            color = iOSGreen,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Ongoing call compact side bubble
 */
@Composable
fun OngoingCallIslandSide(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        WaveformAnimation(
            modifier = Modifier.size(width = 17.dp, height = 14.dp),
            gradient = listOf(IslandColors.Mint, iOSGreen)
        )
    }
}

/**
 * Ongoing call compact banner (86dp capsule) matching `Dynamic Island-7.svg`.
 */
@Composable
fun OngoingCallIslandBanner(
    name: String = "Tamia Castillo",
    duration: String = "02:45",
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (56.dp * scale)
        val badgeSize = (41.4.dp * scale)
        val startPad = (18.dp * scale)
        val endPad = (18.dp * scale)
        val vPad = (15.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPad, end = endPad, top = vPad, bottom = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: 56dp Avatar with 14dp green audio dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(IslandColors.Orange, IslandColors.Yellow)
                                )
                            )
                            .border(0.75.dp, Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifEmpty { name.take(2).uppercase() },
                            style = AppTheme.typography.h2,
                            fontSize = (20f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Green audio badge on bottom-right of avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp * scale)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .padding(1.5.dp * scale)
                            .clip(CircleShape)
                            .background(IslandColors.CapsuleGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 9.dp * scale)
                    }
                }

                Spacer(modifier = Modifier.width(9.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = duration,
                        color = IslandColors.CapsuleGreen,
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.1).sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = (17f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            // RIGHT: Dark green badge (41.4dp circle) with green animated waveform
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Color(0xFF0B2F16)),
                contentAlignment = Alignment.Center
            ) {
                WaveformAnimation(
                    modifier = Modifier.size(width = 20.dp * scale, height = 16.dp * scale),
                    color = IslandColors.CapsuleGreen
                )
            }
        }
    }
}

/**
 * Ongoing call expanded card (166dp-172dp sheet) matching `Dynamic Island-13/14/15.svg`.
 */
@Composable
fun OngoingCallIslandExpanded(
    name: String = "Tamia Castillo",
    avatarUri: String? = null,
    label: String = "FaceTime Audio",
    duration: String = "02:45",
    variant: OngoingCallVariant = OngoingCallVariant.Standard,
    onEndCall: () -> Unit = {},
    onControlAction: (IslandControlAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isMuted by remember(variant) { mutableStateOf(variant != OngoingCallVariant.SharedMedia) }
    var isSpeaker by remember(variant) { mutableStateOf(false) }
    var isVideoOn by remember(variant) { mutableStateOf(variant == OngoingCallVariant.SharedMedia) }
    var isSharePlay by remember(variant) { mutableStateOf(variant == OngoingCallVariant.SharedMedia) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonSize = (50.64.dp * scale)
        val hPad = ((if (variant == OngoingCallVariant.SharedMedia) 24.dp else 29.dp) * scale)
        val topPad = ((if (variant == OngoingCallVariant.SharedMedia) 32.dp else 34.dp) * scale)
        val bottomPad = (24.dp * scale)
        val avatarSize = ((if (variant == OngoingCallVariant.SharedMedia) 53.dp else 44.dp) * scale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = hPad,
                    end = hPad,
                    top = topPad,
                    bottom = bottomPad
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ROW 1: Avatar + Name + Subtitle + Info button (matches Dynamic Island-14.svg)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color(0xFF202124))
                        .border(0.75.dp, Color(0x33FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (variant == OngoingCallVariant.SharedMedia) {
                        Image(
                            painter = painterResource(R.drawable.shared_media_art),
                            contentDescription = "Shared media artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        ContactAvatar(
                            name = name,
                            avatarUri = avatarUri,
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = name
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(
                        (if (variant == OngoingCallVariant.SharedMedia) 21.4.dp else 9.dp) * scale
                    )
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = (17f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = if (variant == OngoingCallVariant.SharedMedia) label else "$label",
                        color = Color(0xFF898989),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.1).sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                if (variant != OngoingCallVariant.SharedMedia) {
                    Box(
                        modifier = Modifier.size(20.dp * scale),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Info,
                            tint = Color(0xFF777677),
                            size = 18.dp * scale
                        )
                    }
                }
            }

            // ROW 2: 5 action buttons. The supplied -13/-14/-15 files are
            // separate visual variants, so their selected controls are seeded
            // independently instead of being flattened into one card.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Speaker Button (Audio route: Inactive #2A292D / Active White)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(
                            if (variant == OngoingCallVariant.FaceTime) Color.White
                            else if (isSpeaker) Color.White else IslandColors.ButtonGlass
                        )
                        .clickable {
                            isSpeaker = !isSpeaker
                            onControlAction(IslandControlAction.CallToggleSpeaker)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (variant == OngoingCallVariant.FaceTime) AppleGlyph.AirPods else AppleGlyph.Speaker,
                        tint = if (variant == OngoingCallVariant.FaceTime || isSpeaker) Color.Black else Color.White,
                        size = if (variant == OngoingCallVariant.FaceTime) 29.dp * scale else 38.dp * scale
                    )
                }

                // 2. Microphone / Mute Button (Dynamic Island-14.svg: Active White background + Black mic)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White else IslandColors.ButtonGlass)
                        .clickable {
                            isMuted = !isMuted
                            onControlAction(IslandControlAction.CallToggleMute)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Microphone,
                        tint = if (isMuted) Color.Black else Color.White,
                        size = 33.dp * scale
                    )
                }

                // Generic Android Telecom calls do not expose portable video or
                // SharePlay commands. Keep those controls in the explicitly
                // app-owned FaceTime/SharedMedia preview variants only, so a
                // live phone call never presents a button that silently does
                // nothing.
                if (variant != OngoingCallVariant.Standard) {
                    // 3. FaceTime Video Camera Button (Inactive #2A292D / Active White)
                    Box(
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape)
                            .background(if (isVideoOn) Color.White else IslandColors.ButtonGlass)
                            .clickable {
                                isVideoOn = !isVideoOn
                                onControlAction(IslandControlAction.CallToggleVideo)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.FaceTimeVideo,
                            tint = if (isVideoOn) Color.Black else Color.White,
                            size = 36.dp * scale
                        )
                    }

                    // 4. SharePlay Button (Inactive #2A292D / Active White)
                    Box(
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape)
                            .background(if (isSharePlay) Color.White else IslandColors.ButtonGlass)
                            .clickable {
                                isSharePlay = !isSharePlay
                                onControlAction(IslandControlAction.CallToggleSharePlay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.SharePlay,
                            tint = if (isSharePlay) Color.Black else Color.White,
                            size = 40.dp * scale
                        )
                    }
                }

                // 5. End Call Button (Apple Red #FA3532 with 26dp white phone-down icon)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(IslandColors.CapsuleRed)
                        .clickable {
                            onEndCall()
                            onControlAction(IslandControlAction.CallEnd)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = if (variant == OngoingCallVariant.SharedMedia) AppleGlyph.Close else AppleGlyph.EndCall,
                        tint = Color.White,
                        size = 35.dp * scale
                    )
                }
            }
        }
    }
}
