package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Compact stacked notification main pill
 */
@Composable
fun CompactNotificationIslandMain(
    appName: String = "WhatsApp",
    sender: String = "John Doe",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF25D366)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        AppText(
            text = "$appName: $sender",
            style = AppTheme.typography.islandSubtitle,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * Notification expanded card
 */
@Composable
fun NotificationIslandExpanded(
    appName: String = "WhatsApp",
    timeAgo: String = "now",
    title: String = "John Doe",
    message: String = "Hey! Are we still meeting at 5:00 PM for the project review?",
    onReply: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.xl, vertical = AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF25D366)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            AppText(
                text = appName.uppercase(),
                style = AppTheme.typography.caption,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            AppText(
                text = timeAgo,
                style = AppTheme.typography.caption,
                color = Color.White.copy(alpha = 0.4f)
            )
        }

        // Message Title & Body
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = title,
                style = AppTheme.typography.islandTitle,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            AppText(
                text = message,
                style = AppTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Action Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF2C2C2E))
                    .clickable { onReply() },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "Reply",
                    style = AppTheme.typography.button,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF2C2C2E))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "Mark as Read",
                    style = AppTheme.typography.button,
                    color = Color.White
                )
            }
        }
    }
}
