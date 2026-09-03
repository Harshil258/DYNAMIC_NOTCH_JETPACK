package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
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

private val iOSGreen = Color(0xFF30D158)
private val iOSRed = Color(0xFFFF453A)
private val iOSDarkGray = Color(0xFF3A3A3C)

/**
 * Ongoing call compact main pill
 */
@Composable
fun OngoingCallIslandMain(
    duration: String = "0:45",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = iOSGreen,
            modifier = Modifier.size(16.dp)
        )

        AppText(
            text = duration,
            style = AppTheme.typography.islandTime,
            color = iOSGreen,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        WaveformBarsAnimation()
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
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iOSGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Ongoing call expanded card
 */
@Composable
fun OngoingCallIslandExpanded(
    name: String = "Tamia Castillo",
    label: String = "Phone",
    duration: String = "02:45",
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaker by remember { mutableStateOf(false) }

    val isCompact = AppTheme.windowSize == AppWindowSize.Compact
    val avatarSize = if (isCompact) 46.dp else 52.dp
    val buttonSize = if (isCompact) 46.dp else 52.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = AppTheme.spacing.xl,
                end = AppTheme.spacing.xl,
                top = AppTheme.spacing.lg,
                bottom = AppTheme.spacing.lg
            )
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
                                Color(0xFFFF6B35),
                                Color(0xFFE63946)
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

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

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
                    color = iOSGreen,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.dp, Color(0xFF636366), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF636366),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 18.dp))

        // ROW 2: Action buttons (Mute, Speaker, End Call)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else iOSDarkGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (isMuted) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Speaker Button
            IconButton(
                onClick = { isSpeaker = !isSpeaker },
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(if (isSpeaker) Color.White else iOSDarkGray)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speaker",
                    tint = if (isSpeaker) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(iOSRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
