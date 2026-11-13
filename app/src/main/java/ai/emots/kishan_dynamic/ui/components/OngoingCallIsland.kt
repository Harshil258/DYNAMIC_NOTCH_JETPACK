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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
                            fontSize = 20.sp,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.1).sp,
                        fontFamily = ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts.Text
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
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
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun OngoingCallIslandExpanded(
    name: String = "Tamia Castillo",
    avatarUri: String? = null,
    label: String = "Mobile Call",
    duration: String = "02:45",
    variant: OngoingCallVariant = OngoingCallVariant.Standard,
    isMutedState: Boolean? = null,
    isSpeakerState: Boolean? = null,
    isVideoOnState: Boolean? = null,
    isSharePlayState: Boolean? = null,
    onEndCall: () -> Unit = {},
    onControlAction: (IslandControlAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localMuted by remember(variant) { mutableStateOf(variant != OngoingCallVariant.SharedMedia) }
    var localSpeaker by remember(variant) { mutableStateOf(false) }
    var localVideoOn by remember(variant) { mutableStateOf(variant == OngoingCallVariant.SharedMedia) }
    var localSharePlay by remember(variant) { mutableStateOf(variant == OngoingCallVariant.SharedMedia) }

    val isMuted = isMutedState ?: localMuted
    val isSpeaker = isSpeakerState ?: localSpeaker
    val isVideoOn = isVideoOnState ?: localVideoOn
    val isSharePlay = isSharePlayState ?: localSharePlay

    BoxWithConstraints(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val buttonVisualSize = when (variant) {
            OngoingCallVariant.FaceTime -> AppTheme.island.callVideoControlSize
            OngoingCallVariant.SharedMedia -> AppTheme.island.sharedCallControlSize
            else -> AppTheme.island.callControlSize
        }
        val buttonTouchSize = buttonVisualSize.coerceAtLeast(AppTheme.island.minimumActionTarget)
        val iconGlyphSize = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.callControlGlyph
        val hPad = if (variant == OngoingCallVariant.SharedMedia) AppTheme.island.sharedCallContentInset else AppTheme.island.callContentInset
        val topPad = if (variant == OngoingCallVariant.SharedMedia) AppTheme.island.sharedCallTopInset else AppTheme.island.callTopInset
        val bottomPad = AppTheme.island.callBottomInset
        val avatarSize = if (variant == OngoingCallVariant.SharedMedia) AppTheme.island.mediaAlbumArtSize else AppTheme.island.callAvatarSize

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = hPad,
                    end = hPad,
                    top = topPad,
                    bottom = bottomPad
                ),
            verticalArrangement = Arrangement.spacedBy(if (variant == OngoingCallVariant.SharedMedia) AppTheme.island.sharedCallRegionGap else 14.dp)
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
                        ,
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
                        fontSize = 16.sp, lineHeight = 18.sp,
                        fontWeight = if (variant == OngoingCallVariant.SharedMedia) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = (-0.2377).sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = if (variant == OngoingCallVariant.SharedMedia) label else "$label",
                        color = Color(0xFF898989),
                        fontSize = 14.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.sp,
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

            // ROW 2: Balanced circular call action controls
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                // 1. Speaker Button (Audio route: Inactive #2C2C2E / Active White)
                if (canRenderIslandControl(IslandControlAction.CallToggleSpeaker)) {
                    IslandIconAction(
                        glyph = if (variant == OngoingCallVariant.FaceTime) AppleGlyph.AirPods else AppleGlyph.Speaker,
                        onClick = {
                            if (isSpeakerState == null) localSpeaker = !localSpeaker
                            onControlAction(IslandControlAction.CallToggleSpeaker)
                        },
                        style = IslandActionStyle.Toggle,
                        visualSize = buttonVisualSize,
                        touchTargetSize = buttonTouchSize,
                        iconSize = (if (variant == OngoingCallVariant.FaceTime) AppleGlyph.AirPods else AppleGlyph.Speaker).figmaInkSize(),
                        contentDescription = "Toggle speakerphone",
                        isActiveToggle = if (variant == OngoingCallVariant.FaceTime) true else isSpeaker
                    )
                }

                // 2. Microphone / Mute Button
                if (canRenderIslandControl(IslandControlAction.CallToggleMute)) {
                    IslandIconAction(
                        glyph = AppleGlyph.Microphone,
                        onClick = {
                            if (isMutedState == null) localMuted = !localMuted
                            onControlAction(IslandControlAction.CallToggleMute)
                        },
                        style = IslandActionStyle.Toggle,
                        visualSize = buttonVisualSize,
                        touchTargetSize = buttonTouchSize,
                        iconSize = AppleGlyph.Microphone.figmaInkSize(),
                        contentDescription = "Toggle microphone mute",
                        isActiveToggle = isMuted
                    )
                }

                // 3. FaceTime Video Camera Button
                if (canRenderIslandControl(IslandControlAction.CallToggleVideo)) {
                    IslandIconAction(
                        glyph = AppleGlyph.FaceTimeVideo,
                        onClick = {
                            if (isVideoOnState == null) localVideoOn = !localVideoOn
                            onControlAction(IslandControlAction.CallToggleVideo)
                        },
                        style = IslandActionStyle.Toggle,
                        visualSize = buttonVisualSize,
                        touchTargetSize = buttonTouchSize,
                        iconSize = AppleGlyph.FaceTimeVideo.figmaInkSize(),
                        contentDescription = "Toggle video camera",
                        isActiveToggle = isVideoOn
                    )
                }

                // 4. SharePlay Button
                if (canRenderIslandControl(IslandControlAction.CallToggleSharePlay)) {
                    IslandIconAction(
                        glyph = if (variant == OngoingCallVariant.SharedMedia) AppleGlyph.SharePlayScreen else AppleGlyph.SharePlay,
                        onClick = {
                            if (isSharePlayState == null) localSharePlay = !localSharePlay
                            onControlAction(IslandControlAction.CallToggleSharePlay)
                        },
                        style = IslandActionStyle.Toggle,
                        visualSize = buttonVisualSize,
                        touchTargetSize = buttonTouchSize,
                        iconSize = (if (variant == OngoingCallVariant.SharedMedia) AppleGlyph.SharePlayScreen else AppleGlyph.SharePlay).figmaInkSize(),
                        contentDescription = "Toggle SharePlay",
                        isActiveToggle = isSharePlay
                    )
                }

                // 5. End Call Button (Apple Red #FA3532 with phone-down icon)
                if (canRenderIslandControl(IslandControlAction.CallEnd)) {
                    IslandIconAction(
                        glyph = if (variant == OngoingCallVariant.SharedMedia) AppleGlyph.Close else AppleGlyph.EndCall,
                        onClick = { onControlAction(IslandControlAction.CallEnd) },
                        style = IslandActionStyle.Destructive,
                        visualSize = buttonVisualSize,
                        touchTargetSize = buttonTouchSize,
                        iconSize = (if (variant == OngoingCallVariant.SharedMedia) AppleGlyph.Close else AppleGlyph.EndCall).figmaInkSize(),
                        contentDescription = "End call"
                    )
                }
            }
        }
    }
}
