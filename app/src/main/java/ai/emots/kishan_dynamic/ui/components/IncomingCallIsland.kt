package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppWindowSize

/**
 * Incoming Call island with horizontal capsule layout matching reference app
 */
@Composable
fun IncomingCallIsland(
    name: String = "Tamia Castillo",
    label: String = "Mobile",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isCompact = AppTheme.windowSize == AppWindowSize.Compact
    val avatarSize = if (isCompact) 44.dp else 50.dp
    val buttonSize = if (isCompact) 44.dp else 50.dp
    val iconSize = if (isCompact) 24.dp else 28.dp

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Contact avatar + info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFFF8C42)
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

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppText(
                    text = label,
                    style = AppTheme.typography.islandSubtitle,
                    color = Color.White.copy(alpha = 0.6f)
                )
                AppText(
                    text = name,
                    style = AppTheme.typography.islandTitle,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

        // Action buttons (Decline & Accept)
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            IconButton(
                onClick = onDecline,
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Decline",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }

            IconButton(
                onClick = onAccept,
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759))
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Accept",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
