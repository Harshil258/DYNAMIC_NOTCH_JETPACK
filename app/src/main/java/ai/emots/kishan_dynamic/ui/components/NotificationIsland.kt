package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import ai.emots.kishan_dynamic.data.model.NotificationInfo
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

/**
 * Specialized visual template styles derived from iOS 17 and reference usecases.
 */
enum class NotificationTemplateType {
    STANDARD,
    NAVIGATION,
    TRANSPORT,
    PROGRESS,
    PICTURE,
    TIMER
}

/**
 * Intelligently classifies an incoming notification into its optimal Dynamic Island presentation.
 */
fun resolveNotificationTemplate(notification: NotificationInfo): NotificationTemplateType {
    val category = notification.category.lowercase()
    val app = notification.appName.lowercase()
    val title = notification.title.lowercase()
    val text = notification.text.lowercase()
    val combined = "$title $text"

    return when {
        category == "navigation" || app.contains("maps") || combined.contains("turn right") ||
            combined.contains("turn left") || combined.contains("continue on") ||
            combined.contains("in 500 ft") || combined.contains("in 100 ft") ||
            combined.contains("exit ") -> NotificationTemplateType.NAVIGATION

        category == "transport" || app.contains("uber") || app.contains("lyft") ||
            app.contains("ola") || combined.contains("arriving in") ||
            combined.contains("driver is") || combined.contains("your ride") -> NotificationTemplateType.TRANSPORT

        notification.progressMax > 0 || notification.isProgressIndeterminate ||
            category == "progress" || combined.contains("downloading") -> NotificationTemplateType.PROGRESS

        notification.showChronometer || notification.chronometerBaseElapsedRealtime > 0L ||
            category == "stopwatch" || category == "timer" -> NotificationTemplateType.TIMER

        notification.imagePath != null -> NotificationTemplateType.PICTURE

        else -> NotificationTemplateType.STANDARD
    }
}

/**
 * Navigation Card Template: iOS Maps turn guidance with prominent direction arrow,
 * high-contrast white maneuver instructions, and ETA metrics.
 */
@Composable
fun NotificationNavigationCard(
    notification: NotificationInfo,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    pagerLabel: String? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (22.dp * scale)
        val vPad = (16.dp * scale)
        val iconSize = (44.dp * scale)

        val titleLower = notification.title.lowercase()
        val textLower = notification.text.lowercase()
        val glyph = when {
            titleLower.contains("left") || textLower.contains("left") -> AppleGlyph.NavigationLeft
            titleLower.contains("right") || textLower.contains("right") -> AppleGlyph.NavigationRight
            else -> AppleGlyph.NavigationStraight
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(12.dp * scale))
                        .background(Color(0xFF34C759)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = glyph,
                        tint = Color.White,
                        size = 24.dp * scale
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    AppText(
                        text = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel",
                        style = AppTheme.typography.caption,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFFA4A4A9),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(1.dp * scale))
                    AppText(
                        text = notification.title.ifBlank { "Turn Guidance" },
                        style = AppTheme.typography.islandTitle,
                        fontSize = (16.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (notification.text.isNotBlank() || notification.subText != null) {
                        Spacer(modifier = Modifier.height(1.dp * scale))
                        AppText(
                            text = listOfNotNull(notification.text.takeIf { it.isNotBlank() }, notification.subText).joinToString(" · "),
                            style = AppTheme.typography.caption,
                            fontSize = (11.5f * scale).sp,
                            color = Color(0xFF9A9A9A),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                val action = notification.actions.firstOrNull()
                NotificationActionButton(
                    label = action?.label ?: "Route",
                    scale = scale,
                    onClick = { onAction?.invoke() }
                )
                if (onDismiss != null && notification.isClearable) {
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

/**
 * Transport / Rideshare Card Template: Driver details, car glyph, ETA badge,
 * and quick contact action pill.
 */
@Composable
fun NotificationTransportCard(
    notification: NotificationInfo,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    pagerLabel: String? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (22.dp * scale)
        val vPad = (16.dp * scale)
        val iconSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .border(0.75.dp, Color(0x33FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Car,
                        tint = Color.White,
                        size = 22.dp * scale
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    AppText(
                        text = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel",
                        style = AppTheme.typography.caption,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFFA4A4A9),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(1.dp * scale))
                    AppText(
                        text = notification.title.ifBlank { "Ride In Progress" },
                        style = AppTheme.typography.islandTitle,
                        fontSize = (16.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (notification.text.isNotBlank() || notification.subText != null) {
                        Spacer(modifier = Modifier.height(1.dp * scale))
                        AppText(
                            text = listOfNotNull(notification.text.takeIf { it.isNotBlank() }, notification.subText).joinToString(" · "),
                            style = AppTheme.typography.caption,
                            fontSize = (11.5f * scale).sp,
                            color = Color(0xFF34C759),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                val action = notification.actions.firstOrNull()
                NotificationActionButton(
                    label = action?.label ?: "Details",
                    scale = scale,
                    onClick = { onAction?.invoke() }
                )
                if (onDismiss != null && notification.isClearable) {
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

/**
 * Progress / Download Card Template: Horizontal progress bar, percentage readout,
 * throughput speed, and cancel/pause control.
 */
@Composable
fun NotificationProgressCard(
    notification: NotificationInfo,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    pagerLabel: String? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (22.dp * scale)
        val vPad = (16.dp * scale)
        val iconSize = (44.dp * scale)

        val progressRatio = if (notification.progressMax > 0) {
            (notification.progress.toFloat() / notification.progressMax.toFloat()).coerceIn(0f, 1f)
        } else null
        val percentText = progressRatio?.let { "${(it * 100).toInt()}%" } ?: "Working"

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(12.dp * scale))
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Charging,
                        tint = Color(0xFF34C759),
                        size = 20.dp * scale
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            text = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (11f * scale).sp,
                            color = Color(0xFFA4A4A9),
                            fontWeight = FontWeight.Medium
                        )
                        AppText(
                            text = percentText,
                            style = AppTheme.typography.caption,
                            fontSize = (11f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp * scale))
                    AppText(
                        text = notification.title.ifBlank { "Download in progress" },
                        style = AppTheme.typography.islandTitle,
                        fontSize = (15.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp * scale))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp * scale)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0x553A3A3C))
                    ) {
                        if (progressRatio != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxSize()
                                    .background(Color.White)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .fillMaxSize()
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                val action = notification.actions.firstOrNull()
                NotificationActionButton(
                    label = action?.label ?: "Cancel",
                    scale = scale,
                    onClick = { onAction?.invoke() }
                )
                if (onDismiss != null && notification.isClearable) {
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

/**
 * Picture Notification Card Template: Visual media thumbnail with high-fidelity styling.
 */
@Composable
fun NotificationPictureCard(
    notification: NotificationInfo,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    pagerLabel: String? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (20.dp * scale)
        val vPad = (16.dp * scale)
        val thumbSize = (50.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                NotificationArtwork(
                    imagePath = notification.imagePath,
                    packageName = notification.packageName,
                    modifier = Modifier
                        .size(thumbSize)
                        .clip(RoundedCornerShape(12.dp * scale))
                        .border(0.75.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp * scale)),
                    contentDescription = "${notification.appName} thumbnail"
                )

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    AppText(
                        text = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel",
                        style = AppTheme.typography.caption,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFFA4A4A9),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(1.dp * scale))
                    AppText(
                        text = notification.title.ifBlank { notification.appName },
                        style = AppTheme.typography.islandTitle,
                        fontSize = (16.5f * scale).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (notification.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp * scale))
                        AppText(
                            text = notification.text,
                            style = AppTheme.typography.caption,
                            fontSize = (11.5f * scale).sp,
                            color = Color(0xFF9A9A9A),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp * scale))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                val action = notification.actions.firstOrNull()
                NotificationActionButton(
                    label = action?.label ?: "View",
                    scale = scale,
                    onClick = { onAction?.invoke() }
                )
                if (onDismiss != null && notification.isClearable) {
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

/**
 * Multi-notification horizontal pager with drag gesture detection, page counters,
 * and automatic template selection per notification.
 */
@Composable
fun NotificationIslandExpandedPager(
    notifications: List<NotificationInfo>,
    activeIndex: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    onActionSelected: (NotificationInfo, NotificationActionInfo) -> Unit = { _, _ -> },
    onDismiss: (NotificationInfo) -> Unit = {},
    onUserInteraction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (notifications.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, notifications.lastIndex),
        pageCount = { notifications.size }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged) {
        if (isDragged) {
            onUserInteraction()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex in 0..notifications.lastIndex && pagerState.currentPage != activeIndex) {
            pagerState.animateScrollToPage(activeIndex)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        val item = notifications[page]
        val pagerLabel = if (notifications.size > 1) "${page + 1}/${notifications.size}" else null
        val template = resolveNotificationTemplate(item)

        when (template) {
            NotificationTemplateType.NAVIGATION -> {
                NotificationNavigationCard(
                    notification = item,
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    onDismiss = { onDismiss(item) },
                    pagerLabel = pagerLabel
                )
            }
            NotificationTemplateType.TRANSPORT -> {
                NotificationTransportCard(
                    notification = item,
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    onDismiss = { onDismiss(item) },
                    pagerLabel = pagerLabel
                )
            }
            NotificationTemplateType.PROGRESS -> {
                NotificationProgressCard(
                    notification = item,
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    onDismiss = { onDismiss(item) },
                    pagerLabel = pagerLabel
                )
            }
            NotificationTemplateType.PICTURE -> {
                NotificationPictureCard(
                    notification = item,
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    onDismiss = { onDismiss(item) },
                    pagerLabel = pagerLabel
                )
            }
            NotificationTemplateType.TIMER -> {
                NotificationIslandExpanded(
                    appName = item.appName,
                    appPackageName = item.packageName,
                    imagePath = item.imagePath,
                    sender = item.title.ifBlank { item.appName },
                    message = item.text,
                    actionLabel = item.actions.firstOrNull()?.label ?: "Pause",
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    actions = item.actions,
                    onActionSelected = { act -> onActionSelected(item, act) },
                    pagerLabel = pagerLabel,
                    onDismiss = { onDismiss(item) },
                    showChronometer = true,
                    chronometerBaseElapsedRealtime = item.chronometerBaseElapsedRealtime,
                    chronometerCountDown = item.chronometerCountDown
                )
            }
            NotificationTemplateType.STANDARD -> {
                NotificationIslandExpanded(
                    appName = item.appName,
                    appPackageName = item.packageName,
                    imagePath = item.imagePath,
                    sender = item.title.ifBlank { item.appName },
                    message = listOfNotNull(
                        item.inboxLines.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                        item.subText,
                        item.expandedText.takeIf { it.isNotBlank() }
                    ).distinct().joinToString(" · "),
                    actionLabel = item.actions.firstOrNull()?.label.orEmpty(),
                    onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                    actions = item.actions,
                    onActionSelected = { act -> onActionSelected(item, act) },
                    pagerLabel = pagerLabel,
                    onDismiss = if (item.isClearable) { { onDismiss(item) } } else null,
                    progress = if (item.progressMax > 0) item.progress.toFloat() / item.progressMax.toFloat() else null,
                    isProgressIndeterminate = item.isProgressIndeterminate,
                    showChronometer = item.showChronometer,
                    chronometerBaseElapsedRealtime = item.chronometerBaseElapsedRealtime,
                    chronometerCountDown = item.chronometerCountDown
                )
            }
        }
    }
}

