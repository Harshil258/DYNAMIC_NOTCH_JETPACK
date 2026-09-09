package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.notification.NotificationChronometerPolicy
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
 * Notification expanded card (86dp capsule)
 * 
 * Layout: App icon / Avatar (44dp circle) + App name & Sender & Message on left,
 * Charcoal pill button [Reply] on right.
 */
@Composable
fun NotificationIslandExpanded(
    appName: String = "WhatsApp",
    appPackageName: String? = null,
    imagePath: String? = null,
    sender: String = "Tamia Castillo",
    message: String = "Hey, are you free tonight? 🍕",
    onReply: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    actionLabel: String = "Reply",
    onAction: () -> Unit = onReply,
    actions: List<NotificationActionInfo> = emptyList(),
    onActionSelected: (NotificationActionInfo) -> Unit = {},
    pagerLabel: String? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    progress: Float? = null,
    isProgressIndeterminate: Boolean = false,
    showChronometer: Boolean = false,
    chronometerBaseElapsedRealtime: Long = 0L,
    chronometerCountDown: Boolean = false,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (24.dp * scale)
        val vPad = (18.dp * scale)
        val iconSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: 44dp App Icon / Avatar + App Name + Sender + Message
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                NotificationArtwork(
                    imagePath = imagePath,
                    packageName = appPackageName,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape),
                    contentDescription = "$appName icon"
                )

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    AppText(
                        text = if (pagerLabel == null) appName else "$appName · $pagerLabel",
                        style = AppTheme.typography.caption,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFFA4A4A9),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(1.dp * scale))
                    AppText(
                        text = sender,
                        style = AppTheme.typography.islandTitle,
                        fontSize = (17.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (message.isNotEmpty() || showChronometer) {
                        Spacer(modifier = Modifier.height(1.dp * scale))
                        if (showChronometer && chronometerBaseElapsedRealtime > 0L) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp * scale)
                            ) {
                                if (message.isNotBlank()) {
                                    AppText(
                                        text = "$message ·",
                                        style = AppTheme.typography.caption,
                                        fontSize = (11f * scale).sp,
                                        color = Color(0xFF9A9A9A),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                NotificationChronometerText(
                                    baseElapsedRealtime = chronometerBaseElapsedRealtime,
                                    countDown = chronometerCountDown,
                                    fontSize = (11f * scale).sp,
                                    color = Color(0xFFFF9F0A)
                                )
                            }
                        } else {
                            AppText(
                                text = if (showChronometer) {
                                    if (message.isBlank()) "Live activity" else "$message · Live"
                                } else message,
                                style = AppTheme.typography.caption,
                                fontSize = (11f * scale).sp,
                                color = Color(0xFF9A9A9A),
                                maxLines = 3,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (progress != null || isProgressIndeterminate) {
                        Spacer(modifier = Modifier.height(4.dp * scale))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp * scale)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color(0x553A3A3C))
                        ) {
                            if (progress != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .fillMaxSize()
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            // RIGHT: Apple Dark Charcoal Glass Pill Button [ Reply ]
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                if (actions.isEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        if (onPrevious != null) {
                            NotificationPagerButton(
                                label = "‹",
                                scale = scale,
                                onClick = onPrevious
                            )
                        }
                        if (actionLabel.isNotBlank()) {
                            NotificationActionButton(
                                label = actionLabel,
                                scale = scale,
                                onClick = onAction
                            )
                        }
                        if (onNext != null) {
                            NotificationPagerButton(
                                label = "›",
                                scale = scale,
                                onClick = onNext
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp * scale)
                    ) {
                        if (onPrevious != null) {
                            NotificationPagerButton(
                                label = "‹",
                                scale = scale,
                                onClick = onPrevious
                            )
                        }
                        actions.forEach { action ->
                            NotificationActionButton(
                                label = action.label,
                                scale = scale,
                                onClick = { onActionSelected(action) }
                            )
                        }
                        if (onNext != null) {
                            NotificationPagerButton(
                                label = "›",
                                scale = scale,
                                onClick = onNext
                            )
                        }
                    }
                }
                if (onDismiss != null) {
                    AppText(
                        text = "Dismiss",
                        style = AppTheme.typography.caption,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.clickable(onClick = onDismiss)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationChronometerText(
    baseElapsedRealtime: Long,
    countDown: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    color: Color = Color(0xFFFF9F0A)
) {
    var value by remember(baseElapsedRealtime, countDown) {
        mutableStateOf(NotificationChronometerPolicy.format(0L))
    }

    LaunchedEffect(baseElapsedRealtime, countDown) {
        while (isActive) {
            value = NotificationChronometerPolicy.format(
                NotificationChronometerPolicy.elapsedSeconds(
                    baseElapsedRealtimeMillis = baseElapsedRealtime,
                    nowElapsedRealtimeMillis = android.os.SystemClock.elapsedRealtime(),
                    countDown = countDown
                )
            )
            delay(1_000L)
        }
    }

    AppText(
        text = value,
        style = AppTheme.typography.caption,
        fontSize = fontSize,
        color = color,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1
    )
}

@Composable
private fun NotificationActionButton(
    label: String,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(IslandColors.ButtonNeutral)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp * scale, vertical = 9.dp * scale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = (12.5f * scale).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun NotificationPagerButton(
    label: String,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp * scale)
            .clip(CircleShape)
            .background(IslandColors.ButtonNeutral)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 19.sp * scale, fontWeight = FontWeight.Bold)
    }
}
