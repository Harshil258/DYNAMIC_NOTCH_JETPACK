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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    canDecline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val pulseState = rememberBreathing(min = 0.96f, max = 1.03f, durationMillis = 1400)
    val declineInteraction = remember { MutableInteractionSource() }
    val acceptInteraction = remember { MutableInteractionSource() }
    val declineScale by rememberPressScale(declineInteraction)
    val acceptScale by rememberPressScale(acceptInteraction)

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val avatarSize = 40.dp * scale
        val buttonVisualSize = 38.dp * scale
        val buttonTouchSize = (44.dp * scale).coerceAtLeast(44.dp)
        val iconGlyphSize = 18.dp * scale
        val startPad = 16.dp * scale
        val endPad = 16.dp * scale
        val vPad = 12.dp * scale

        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                            scaleX = pulseState.value
                            scaleY = pulseState.value
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp * scale))
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            // RIGHT: Decline Button + Accept Button with 44dp hit areas and 38dp visual circles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                // Red Decline Button
                if (canDecline && canRenderIslandControl(IslandControlAction.CallDecline)) {
                    Box(
                        modifier = Modifier
                            .size(buttonTouchSize)
                            .clickable(
                                interactionSource = declineInteraction,
                                indication = null,
                                onClick = onDecline
                            )
                            .semantics { contentDescription = "Decline call" },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(buttonVisualSize)
                                .graphicsLayer {
                                    scaleX = declineScale
                                    scaleY = declineScale
                                }
                                .clip(CircleShape)
                                .background(CallRed),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(glyph = AppleGlyph.EndCall, tint = Color.White, size = iconGlyphSize)
                        }
                    }
                }

                // Green Accept Button
                if (canRenderIslandControl(IslandControlAction.CallAccept)) Box(
                    modifier = Modifier
                        .size(buttonTouchSize)
                        .clickable(
                            interactionSource = acceptInteraction,
                            indication = null,
                            onClick = onAccept
                        )
                        .semantics { contentDescription = "Accept call" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(buttonVisualSize)
                            .graphicsLayer {
                                scaleX = acceptScale
                                scaleY = acceptScale
                            }
                            .clip(CircleShape)
                            .background(CallGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(glyph = AppleGlyph.Phone, tint = Color.White, size = iconGlyphSize)
                    }
                }
            }
        }
    }
}
