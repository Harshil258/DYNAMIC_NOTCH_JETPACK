package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.motion.rememberPressScale
import ai.emots.kishan_dynamic.ui.theme.AppTheme

private val CallGreen = Color(0xFF30D158)
private val CallRed = Color(0xFFFF453A)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF6B35), Color(0xFFFF8C42))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = name.take(1).uppercase(),
                    style = AppTheme.typography.h2,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(
                    text = name,
                    style = AppTheme.typography.islandTitle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AppText(
                    text = "$label · incoming",
                    style = AppTheme.typography.islandSubtitle,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CallActionButton(
                label = "Decline",
                glyph = AppleGlyph.Phone,
                container = CallRed,
                onClick = onDecline,
                rotated = true,
                modifier = Modifier.weight(1f)
            )
            CallActionButton(
                label = "Accept",
                glyph = AppleGlyph.Phone,
                container = CallGreen,
                onClick = onAccept,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CallActionButton(
    label: String,
    glyph: AppleGlyph,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotated: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interaction, pressedScale = 0.93f)

    Row(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(
            glyph = glyph,
            tint = Color.White,
            size = 17.dp,
            modifier = Modifier.graphicsLayer {
                rotationZ = if (rotated) 135f else 0f
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        AppText(
            text = label,
            style = AppTheme.typography.button,
            color = Color.White,
            maxLines = 1
        )
    }
}
