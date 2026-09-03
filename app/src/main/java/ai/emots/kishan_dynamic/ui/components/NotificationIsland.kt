package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.components.AppText

/**
 * Compact stacked notification main pill
 */
@Composable
fun CompactNotificationIslandMain(
    appName: String = "WhatsApp",
    sender: String = "John Doe",
    modifier: Modifier = Modifier
) {
    // The real capsule is 126pt wide: a single app badge plus a short label.
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(23.dp)
                .clip(CircleShape)
                .background(Color(0xFF25D366)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = AppleGlyph.Bell,
                tint = Color.White,
                size = 12.dp
            )
        }

        AppText(
            text = sender,
            style = AppTheme.typography.islandSubtitle,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Notification expanded card - MATCHING Dynamic Island-2.svg
 * 
 * Layout: Bell slash icon + "SilentMode" + "On" on left,
 * Charcoal pill button [Unmute] on right.
 */
@Composable
fun NotificationIslandExpanded(
    appName: String = "SilentMode",
    title: String = "On",
    message: String = "",
    onReply: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Bell slash icon + "SilentMode" + "On"
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(
                glyph = AppleGlyph.BellSlash,
                tint = Color.White,
                size = 28.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.Center) {
                AppText(
                    text = appName,
                    style = AppTheme.typography.caption,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = title,
                    style = AppTheme.typography.islandTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // RIGHT: Apple Dark Charcoal Glass Pill Button [ Unmute ]
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF2C2C2E))
                .padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Unmute",
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
