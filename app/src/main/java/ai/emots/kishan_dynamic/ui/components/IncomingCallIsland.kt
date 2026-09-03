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

private val CallGreen = Color(0xFF34C759)
private val CallRed = Color(0xFFFF3B30)

/**
 * INCOMING CALL sheet.
 *
 * Matches the iOS layout: identity on top (avatar, label, name), a full-width
 * pair of circular actions underneath. The avatar breathes gently while the
 * phone rings, which is what makes the state feel alive.
 */
@Composable
fun IncomingCallIsland(
    name: String = "Tamia Castillo",
    label: String = "Mobile",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pulse by rememberBreathing(min = 0.94f, max = 1.04f, durationMillis = 1400)
    val declineInteraction = remember { MutableInteractionSource() }
    val acceptInteraction = remember { MutableInteractionSource() }
    val declineScale by rememberPressScale(declineInteraction)
    val acceptScale by rememberPressScale(acceptInteraction)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Avatar + "Mobile" & "Tamia Castillo"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF6B35), Color(0xFFFF8C42))
                        )
                    )
                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = name.take(2).uppercase(),
                    style = AppTheme.typography.h2,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Mobile",
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // RIGHT: Red Decline Button + Green Accept Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Red Decline Button (50dp circle)
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                Box(modifier = Modifier.rotate(135f)) {
                    AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 22.dp)
                }
            }

            // Green Accept Button (50dp circle)
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                Box(modifier = Modifier.rotate(0f)) {
                    AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = 22.dp)
                }
            }
        }
    }
}
