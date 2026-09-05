package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.motion.rememberPressScale
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

private val CallGreen = IslandColors.CapsuleGreen
private val CallRed = IslandColors.CapsuleRed

/**
 * INCOMING CALL banner (86dp capsule).
 *
 * 1:1 implementation matching iOS 17 Figma reference `Dynamic Island-6.svg`:
 * - 56dp circular caller avatar
 * - Subtitle "iPhone" / "Mobile" (#A4A4A9, 12sp) + Caller Name (white bold 17sp)
 * - 52dp Red Decline button (#FA3532) + 52dp Green Accept button (#37C058)
 */
@Composable
fun IncomingCallIsland(
    name: String = "Tamia Castillo",
    label: String = "iPhone",
    avatarUri: String? = null,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pulse by rememberBreathing(min = 0.96f, max = 1.03f, durationMillis = 1400)
    val declineInteraction = remember { MutableInteractionSource() }
    val acceptInteraction = remember { MutableInteractionSource() }
    val declineScale by rememberPressScale(declineInteraction)
    val acceptScale by rememberPressScale(acceptInteraction)

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = (56.dp * scale)
        val buttonSize = (52.dp * scale)
        val startPad = (24.dp * scale)
        val endPad = (25.dp * scale)
        val vPad = (15.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPad, end = endPad, top = vPad, bottom = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: 56dp Avatar + Label & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(IslandColors.Orange, IslandColors.Yellow)
                            )
                        )
                        .border(0.75.dp, Color(0x33FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ContactAvatar(
                        name = name,
                        avatarUri = avatarUri,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = name
                    )
                }

                Spacer(modifier = Modifier.width(9.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        color = Color(0xFFA4A4A9),
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = (17f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            // RIGHT: Red Decline Button (52dp) + Green Accept Button (52dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
            ) {
                // Red Decline Button (52dp circle)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .graphicsLayer {
                            scaleX = declineScale
                            scaleY = declineScale
                        }
                        .clip(CircleShape)
                        .background(CallRed)
                        .clickable(
                            interactionSource = declineInteraction,
                            indication = null,
                            onClick = onDecline
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.EndCall, tint = Color.White, size = 35.dp * scale)
                }

                // Green Accept Button (52dp circle)
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .graphicsLayer {
                            scaleX = acceptScale
                            scaleY = acceptScale
                        }
                        .clip(CircleShape)
                        .background(CallGreen)
                        .clickable(
                            interactionSource = acceptInteraction,
                            indication = null,
                            onClick = onAccept
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 30.dp * scale)
                }
            }
        }
    }
}
