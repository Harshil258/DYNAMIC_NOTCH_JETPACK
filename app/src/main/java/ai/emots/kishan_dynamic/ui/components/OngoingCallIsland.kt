package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppWindowSize
import ai.emots.kishan_dynamic.ui.theme.IslandColors

private val iOSGreen = IslandColors.Green
private val iOSRed = IslandColors.Red
private val iOSDarkGray = IslandColors.Gray4

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
 * Ongoing call expanded card
 */
@Composable
fun OngoingCallIslandExpanded(
    name: String = "Tamia Castillo",
    label: String = "FaceTime Audio",
    duration: String = "02:45",
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaker by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(false) }
    var isSharePlay by remember { mutableStateOf(false) }

    val isCompact = AppTheme.windowSize == AppWindowSize.Compact
    val avatarSize = if (isCompact) 44.dp else 48.dp
    val buttonSize = if (isCompact) 44.dp else 48.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 14.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ROW 1: Avatar + Name + Subtitle + Info button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                IslandColors.Orange,
                                IslandColors.Red
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = name.take(1).uppercase(),
                    style = AppTheme.typography.h2,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(
                    text = name,
                    style = AppTheme.typography.islandTitle,
                    color = Color.White,
                    maxLines = 1
                )
                AppText(
                    text = "$label • $duration",
                    style = AppTheme.typography.islandSubtitle,
                    color = IslandColors.Gray,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Info,
                    tint = IslandColors.Gray,
                    size = 20.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ROW 2: 5 Action buttons from Figma Dynamic Island-2 & 3:
        // Speaker, Mic, Video, SharePlay, End Call
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Speaker Button
            IconButton(
                onClick = { isSpeaker = !isSpeaker },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isSpeaker) Color.White else IslandColors.ButtonGlass)
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Speaker,
                    tint = if (isSpeaker) Color.Black else Color.White,
                    size = 20.dp
                )
            }

            // 2. Mic Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else IslandColors.ButtonGlass)
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Microphone,
                    tint = if (isMuted) Color.Black else Color.White,
                    size = 20.dp
                )
            }

            // 3. Video Camera Button
            IconButton(
                onClick = { isVideoOn = !isVideoOn },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isVideoOn) Color.White else IslandColors.ButtonGlass)
            ) {
                AppleIcon(
                    glyph = AppleGlyph.FaceTimeVideo,
                    tint = if (isVideoOn) Color.Black else Color.White,
                    size = 20.dp
                )
            }

            // 4. SharePlay Button
            IconButton(
                onClick = { isSharePlay = !isSharePlay },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isSharePlay) Color.White else IslandColors.ButtonGlass)
            ) {
                AppleIcon(
                    glyph = AppleGlyph.SharePlay,
                    tint = if (isSharePlay) Color.Black else Color.White,
                    size = 20.dp
                )
            }

            // 5. End Call Button (Apple Red #FA3532)
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(IslandColors.CapsuleRed)
            ) {
                AppleIcon(
                    glyph = AppleGlyph.EndCall,
                    tint = Color.White,
                    size = 22.dp
                )
            }
        }
    }
}
