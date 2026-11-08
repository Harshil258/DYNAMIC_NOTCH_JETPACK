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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState

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
 * Inline reply input box with authentic Apple styling and direct FocusRequester.
 */
@Composable
fun InlineReplyBox(
    replyAction: NotificationActionInfo,
    onSendReply: (String) -> Unit,
    onCancelReply: () -> Unit,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    var replyText by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp * scale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp * scale))
                .background(Color(0xFF242426))
                .border(0.75.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp * scale))
                .padding(horizontal = 12.dp * scale, vertical = 8.dp * scale)
        ) {
            BasicTextField(
                value = replyText,
                onValueChange = { replyText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = (13.5f * scale).sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF007AFF)),
                maxLines = 2,
                decorationBox = { innerTextField ->
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Reply to ${replyAction.label.ifBlank { "message" }}...",
                            color = Color(0xFF8E8E93),
                            fontSize = (13.5f * scale).sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF3A3A3C))
                    .clickable(onClick = onCancelReply)
                    .padding(horizontal = 14.dp * scale, vertical = 6.dp * scale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = (12.5f * scale).sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp * scale))

            val canSend = replyText.isNotBlank()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (canSend) Color(0xFF007AFF) else Color(0xFF007AFF).copy(alpha = 0.35f))
                    .clickable(enabled = canSend) {
                        onSendReply(replyText.trim())
                    }
                    .padding(horizontal = 16.dp * scale, vertical = 6.dp * scale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send",
                    color = if (canSend) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = (12.5f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Notification expanded card (86dp capsule or 166dp inline reply sheet)
 * 
 * Layout: App icon / Avatar (44dp circle) + App name & Sender & Message on left,
 * Charcoal pill button [Reply] on right.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationIslandExpanded(
    appName: String = "WhatsApp",
    appPackageName: String? = null,
    imagePath: String? = null,
    sender: String = "Tamia Castillo",
    message: String = "Hey, are you free tonight? 🍕",
    expandedText: String? = null,
    inboxLines: List<String> = emptyList(),
    isCardExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
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
    isReplying: Boolean = false,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (20.dp * scale)
        val vPad = (12.dp * scale)

        if (isReplying && replyAction != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { size ->
                        with(density) {
                            onHeightChange?.invoke(size.height.toDp())
                        }
                    }
                    .padding(horizontal = hPad, vertical = (13.dp * scale)),
                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        NotificationArtwork(
                            imagePath = imagePath,
                            packageName = appPackageName,
                            modifier = Modifier
                                .size(20.dp * scale)
                                .clip(CircleShape),
                            contentDescription = "$appName icon"
                        )
                        AppText(
                            text = if (pagerLabel == null) "$appName · $sender" else "$appName · $sender · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (12f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (onDismiss != null) {
                        Box(
                            modifier = Modifier
                                .size(AppTheme.island.minimumActionTarget)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C2E))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Close,
                                tint = Color(0xFFA4A4A9),
                                size = 10.dp * scale
                            )
                        }
                    }
                }

                if (message.isNotBlank()) {
                    AppText(
                        text = message,
                        style = AppTheme.typography.caption,
                        fontSize = (12.5f * scale).sp,
                        color = Color(0xFF9A9A9A),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                InlineReplyBox(
                    replyAction = replyAction,
                    onSendReply = onSendReply,
                    onCancelReply = onCancelReply,
                    scale = scale
                )
            }
        } else if (isCardExpanded) {
            // STATE 2: EXPANDED DETAILED CARD (Zero wasted space, dynamic content height)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { size ->
                        with(density) {
                            onHeightChange?.invoke(size.height.toDp())
                        }
                    }
                    .padding(
                        start = hPad,
                        end = hPad,
                        top = (13.dp * scale),
                        bottom = if (pagerLabel != null) (26.dp * scale) else (14.dp * scale)
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                // 1. TOP HEADER ROW (App Icon, App Name, Pager count, Collapse chevron, Dismiss)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        NotificationArtwork(
                            imagePath = imagePath,
                            packageName = appPackageName,
                            modifier = Modifier
                                .size(20.dp * scale)
                                .clip(CircleShape),
                            contentDescription = "$appName icon"
                        )
                        AppText(
                            text = if (pagerLabel == null) appName else "$appName · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (12f * scale).sp,
                            color = Color(0xFFA4A4A9),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                    ) {
                        if (onToggleExpand != null) {
                            Box(
                                modifier = Modifier
                                    .size(AppTheme.island.minimumActionTarget)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2E))
                                    .clickable(onClick = onToggleExpand),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = AppleGlyph.ChevronRight,
                                    tint = Color.White,
                                    size = 11.dp * scale,
                                    modifier = Modifier.graphicsLayer { rotationZ = -90f }
                                )
                            }
                        }
                        if (onDismiss != null) {
                            Box(
                                modifier = Modifier
                                    .size(AppTheme.island.minimumActionTarget)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2E))
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = AppleGlyph.Close,
                                    tint = Color(0xFFA4A4A9),
                                    size = 10.dp * scale
                                )
                            }
                        }
                    }
                }

                // 2. MIDDLE MESSAGE BODY ROW (Avatar on left, Sender & full readable multi-line text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
                ) {
                    NotificationArtwork(
                        imagePath = imagePath,
                        packageName = appPackageName,
                        modifier = Modifier
                            .size(44.dp * scale)
                            .clip(CircleShape),
                        contentDescription = "$appName avatar"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp * scale)
                    ) {
                        AppText(
                            text = sender,
                            style = AppTheme.typography.islandTitle,
                            fontSize = (16f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        val fullMessageText = when {
                            inboxLines.isNotEmpty() -> inboxLines.joinToString("\n")
                            !expandedText.isNullOrBlank() -> expandedText
                            message.isNotBlank() -> message
                            else -> ""
                        }
                        if (fullMessageText.isNotBlank()) {
                            AppText(
                                text = fullMessageText,
                                style = AppTheme.typography.caption,
                                fontSize = (13.5f * scale).sp,
                                lineHeight = (18.5f * scale).sp,
                                color = Color(0xFFDDDDDD),
                                maxLines = 5,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        if (progress != null || isProgressIndeterminate) {
                            Spacer(modifier = Modifier.height(4.dp * scale))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp * scale)
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

                // 3. BOTTOM ACTIONS ROW (Dedicated horizontal row right underneath the message)
                if (actions.isNotEmpty() || actionLabel.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (actions.isNotEmpty()) {
                            actions.forEach { action ->
                                NotificationActionButton(
                                    label = action.label,
                                    scale = scale,
                                    onClick = { onActionSelected(action) }
                                )
                            }
                        } else if (actionLabel.isNotBlank()) {
                            NotificationActionButton(
                                label = actionLabel,
                                scale = scale,
                                onClick = onAction
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .onSizeChanged { size -> with(density) { onHeightChange?.invoke(size.height.toDp()) } }
                    .padding(
                        start = AppTheme.island.expandedContentInset,
                        end = AppTheme.island.expandedContentInset,
                        top = AppTheme.island.expandedContentInset,
                        bottom = if (pagerLabel != null) AppTheme.island.expandedContentInset + 14.dp else AppTheme.island.expandedContentInset
                    ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.island.expandedContentGap)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.island.expandedContentGap)
                ) {
                    NotificationArtwork(
                        imagePath = imagePath, packageName = appPackageName,
                        modifier = Modifier.size(AppTheme.island.expandedIconSize).clip(CircleShape),
                        contentDescription = "$appName icon"
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                        AppText(
                            text = if (pagerLabel == null) appName else "$appName · $pagerLabel",
                            style = AppTheme.typography.caption, color = IslandColors.TextSecondary
                        )
                        AppText(text = sender, style = AppTheme.typography.islandTitle,
                            color = Color.White, maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        if (message.isNotBlank()) AppText(
                            text = message, style = AppTheme.typography.bodySmall,
                            color = IslandColors.TextSecondary, maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (showChronometer && chronometerBaseElapsedRealtime > 0L) {
                            NotificationChronometerText(chronometerBaseElapsedRealtime, chronometerCountDown)
                        }
                    }
                }
                if (actions.isNotEmpty() || actionLabel.isNotBlank() || onToggleExpand != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        val primary = actions.firstOrNull()
                        if (primary != null) {
                            NotificationActionButton(primary.label, scale) { onActionSelected(primary) }
                        } else if (actionLabel.isNotBlank()) {
                            NotificationActionButton(actionLabel, scale, onAction)
                        }
                        if (onToggleExpand != null) NotificationActionButton("Expand", scale, onToggleExpand)
                    }
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
    ai.emots.kishan_dynamic.ui.kit.AppButton(
        text = label, onClick = onClick,
        style = ai.emots.kishan_dynamic.ui.kit.AppButtonStyle.Secondary,
        fillMaxWidth = false
    )
}

@Composable
private fun NotificationPagerButton(
    label: String,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(AppTheme.island.minimumActionTarget)
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
typealias NotificationTemplateType = ai.emots.kishan_dynamic.data.notification.NotificationPresentationType

/**
 * Intelligently classifies an incoming notification into its optimal Dynamic Island presentation.
 */
fun resolveNotificationTemplate(notification: NotificationInfo): NotificationTemplateType {
    return ai.emots.kishan_dynamic.data.notification.NotificationPresentationPolicy.classify(notification)
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
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
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
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    with(density) {
                        onHeightChange?.invoke(size.height.toDp())
                    }
                }
                .padding(
                    start = hPad,
                    end = hPad,
                    top = vPad,
                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
                ),
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
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (22.dp * scale)
        val vPad = (16.dp * scale)
        val iconSize = (44.dp * scale)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    with(density) {
                        onHeightChange?.invoke(size.height.toDp())
                    }
                }
                .padding(
                    start = hPad,
                    end = hPad,
                    top = vPad,
                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
                ),
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
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
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
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    with(density) {
                        onHeightChange?.invoke(size.height.toDp())
                    }
                }
                .padding(
                    start = hPad,
                    end = hPad,
                    top = vPad,
                    bottom = if (pagerLabel != null) vPad + 14.dp else vPad
                ),
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
                    PackageIcon(
                        packageName = notification.packageName,
                        modifier = Modifier
                            .size(iconSize * 0.72f)
                            .clip(CircleShape),
                        contentDescription = "${notification.appName} icon"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    val effectiveCaption = if (pagerLabel == null) {
                        notification.appName
                    } else {
                        "${notification.appName} · $pagerLabel"
                    }
                    val effectiveTitle = if (notification.title.isNotBlank() &&
                        !notification.title.equals(notification.appName, ignoreCase = true)
                    ) {
                        notification.title
                    } else if (notification.text.isNotBlank()) {
                        notification.text
                    } else {
                        "Progress"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            text = effectiveCaption,
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
                        text = effectiveTitle,
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
    isReplying: Boolean = false,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    pagerLabel: String? = null,
    isCardExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null,
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = (20.dp * scale)
        val vPad = (14.dp * scale)
        val thumbSize = (50.dp * scale)

        if (isReplying && replyAction != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { size ->
                        with(density) {
                            onHeightChange?.invoke(size.height.toDp())
                        }
                    }
                    .padding(horizontal = hPad, vertical = (14.dp * scale)),
                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                    ) {
                        NotificationArtwork(
                            imagePath = notification.imagePath ?: notification.picturePath,
                            packageName = notification.packageName,
                            modifier = Modifier
                                .size(24.dp * scale)
                                .clip(RoundedCornerShape(6.dp * scale)),
                            contentDescription = "${notification.appName} icon"
                        )
                        AppText(
                            text = if (pagerLabel == null) "${notification.appName} · ${notification.title}" else "${notification.appName} · ${notification.title} · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (12f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (onDismiss != null) {
                        AppText(
                            text = "Dismiss",
                            style = AppTheme.typography.caption,
                            fontSize = (11f * scale).sp,
                            color = Color(0xFF8E8E93),
                            modifier = Modifier.clickable(onClick = onDismiss)
                        )
                    }
                }

                if (notification.text.isNotBlank()) {
                    AppText(
                        text = notification.text,
                        style = AppTheme.typography.caption,
                        fontSize = (12f * scale).sp,
                        color = Color(0xFF9A9A9A),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                InlineReplyBox(
                    replyAction = replyAction,
                    onSendReply = onSendReply,
                    onCancelReply = onCancelReply,
                    scale = scale
                )
            }
        } else if (isCardExpanded || maxHeight > 130.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { size ->
                        with(density) {
                            onHeightChange?.invoke(size.height.toDp())
                        }
                    }
                    .padding(
                        start = hPad,
                        end = hPad,
                        top = (12.dp * scale),
                        bottom = if (pagerLabel != null) (26.dp * scale) else (14.dp * scale)
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                    ) {
                        NotificationArtwork(
                            imagePath = notification.imagePath,
                            packageName = notification.packageName,
                            modifier = Modifier
                                .size(20.dp * scale)
                                .clip(CircleShape),
                            contentDescription = "${notification.appName} icon"
                        )
                        AppText(
                            text = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (11f * scale).sp,
                            color = Color(0xFFA4A4A9),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                    ) {
                        if (onToggleExpand != null) {
                            Box(
                                modifier = Modifier
                                    .size(AppTheme.island.minimumActionTarget)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2E))
                                    .clickable(onClick = onToggleExpand),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = AppleGlyph.ChevronRight,
                                    tint = Color.White,
                                    size = 11.dp * scale,
                                    modifier = Modifier.graphicsLayer { rotationZ = -90f }
                                )
                            }
                        }
                        if (onDismiss != null && notification.isClearable) {
                            Box(
                                modifier = Modifier
                                    .size(AppTheme.island.minimumActionTarget)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2E))
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = AppleGlyph.Close,
                                    tint = Color(0xFFA4A4A9),
                                    size = 10.dp * scale
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp * scale)
                        .clip(RoundedCornerShape(12.dp * scale))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp * scale))
                        .background(Color(0xFF1C1C1E))
                ) {
                    NotificationArtwork(
                        imagePath = notification.picturePath ?: notification.imagePath,
                        packageName = notification.packageName,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "${notification.appName} preview"
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(32.dp * scale)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppText(
                            text = notification.title.ifBlank { notification.appName },
                            style = AppTheme.typography.islandTitle,
                            fontSize = (14.5f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (notification.text.isNotBlank()) {
                            AppText(
                                text = notification.text,
                                style = AppTheme.typography.caption,
                                fontSize = (11f * scale).sp,
                                color = Color(0xFF9A9A9A),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp * scale))

                    val action = notification.actions.firstOrNull()
                    NotificationActionButton(
                        label = action?.label ?: "View",
                        scale = scale,
                        onClick = { onAction?.invoke() }
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { size ->
                        with(density) {
                            onHeightChange?.invoke(size.height.toDp())
                        }
                    }
                    .padding(horizontal = hPad, vertical = vPad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = onToggleExpand != null, onClick = { onToggleExpand?.invoke() })
                ) {
                    NotificationArtwork(
                        imagePath = notification.picturePath ?: notification.imagePath,
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
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp * scale))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    val action = notification.actions.firstOrNull()
                    NotificationActionButton(
                        label = action?.label ?: "View",
                        scale = scale,
                        onClick = { onAction?.invoke() }
                    )

                    if (onToggleExpand != null) {
                        Box(
                            modifier = Modifier
                                .size(AppTheme.island.minimumActionTarget)
                                .clip(CircleShape)
                                .background(IslandColors.ButtonNeutral)
                                .clickable(onClick = onToggleExpand),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.ChevronRight,
                                tint = Color.White,
                                size = 12.dp * scale,
                                modifier = Modifier.graphicsLayer { rotationZ = 90f }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-notification horizontal pager with drag gesture detection, page counters,
 * automatic template selection per notification, and iOS page indicator dots.
 */
@Composable
fun NotificationIslandExpandedPager(
    notifications: List<NotificationInfo>,
    activeIndex: Int = 0,
    isCardExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    replyingNotificationId: String? = null,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onPageChanged: (Int) -> Unit = {},
    onActionSelected: (NotificationInfo, NotificationActionInfo) -> Unit = { _, _ -> },
    onDismiss: (NotificationInfo) -> Unit = {},
    onUserInteraction: () -> Unit = {},
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (notifications.isEmpty()) return

    // 1 item -> direct single-card expand without pager overhead
    if (notifications.size == 1) {
        val item = notifications[0]
        val isReplying = replyingNotificationId == item.id
        val currentReplyAction = if (isReplying) replyAction else null
        val template = resolveNotificationTemplate(item)

        Box(modifier = modifier.fillMaxWidth()) {
            when (template) {
                NotificationTemplateType.NAVIGATION -> {
                    NotificationNavigationCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = null,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
                    )
                }
                NotificationTemplateType.TRANSPORT -> {
                    NotificationTransportCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = null,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
                    )
                }
                NotificationTemplateType.PROGRESS -> {
                    NotificationProgressCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = null,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
                    )
                }
                NotificationTemplateType.PICTURE -> {
                    NotificationPictureCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        pagerLabel = null,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
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
                        pagerLabel = null,
                        onDismiss = { onDismiss(item) },
                        showChronometer = true,
                        chronometerBaseElapsedRealtime = item.chronometerBaseElapsedRealtime,
                        chronometerCountDown = item.chronometerCountDown,
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
                    )
                }
                NotificationTemplateType.STANDARD -> {
                    NotificationIslandExpanded(
                        appName = item.appName,
                        appPackageName = item.packageName,
                        imagePath = item.imagePath,
                        sender = item.title.ifBlank { item.appName },
                        message = item.text,
                        expandedText = item.expandedText,
                        inboxLines = item.inboxLines,
                        actionLabel = item.actions.firstOrNull()?.label.orEmpty(),
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        actions = item.actions,
                        onActionSelected = { act -> onActionSelected(item, act) },
                        pagerLabel = null,
                        onDismiss = if (item.isClearable) { { onDismiss(item) } } else null,
                        progress = if (item.progressMax > 0) item.progress.toFloat() / item.progressMax.toFloat() else null,
                        isProgressIndeterminate = item.isProgressIndeterminate,
                        showChronometer = item.showChronometer,
                        chronometerBaseElapsedRealtime = item.chronometerBaseElapsedRealtime,
                        chronometerCountDown = item.chronometerCountDown,
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> onHeightChange?.invoke(h) }
                    )
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, notifications.lastIndex),
        pageCount = { notifications.size }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var userPaging by remember { mutableStateOf(false) }
    val latestNotifications by rememberUpdatedState(notifications)
    val latestOnPageChanged by rememberUpdatedState(onPageChanged)
    val notificationIds = notifications.map { it.id }
    val selectedId = notifications.getOrNull(activeIndex)?.id

    LaunchedEffect(isDragged) {
        if (isDragged) {
            userPaging = true
            onUserInteraction()
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && userPaging) {
                userPaging = false
                val settledKey = pagerState.layoutInfo.visiblePagesInfo
                    .firstOrNull { it.index == pagerState.settledPage }?.key
                ai.emots.kishan_dynamic.data.notification.NotificationPagerSelectionPolicy
                    .settledIndex(latestNotifications.map { it.id }, settledKey)
                    ?.let { latestOnPageChanged(it) }
            }
        }
    }

    LaunchedEffect(selectedId, notificationIds) {
        if (!pagerState.isScrollInProgress && activeIndex in notifications.indices &&
            pagerState.currentPage != activeIndex) {
            pagerState.scrollToPage(activeIndex)
        }
    }

    val pageHeights = remember { mutableStateMapOf<String, androidx.compose.ui.unit.Dp>() }
    LaunchedEffect(notificationIds) {
        pageHeights.keys.toList().filterNot { it in notificationIds }.forEach { pageHeights.remove(it) }
    }
    val currentPage = pagerState.currentPage
    val activePageHeight = notifications.getOrNull(currentPage)?.id?.let { pageHeights[it] }
    LaunchedEffect(activePageHeight, isCardExpanded) {
        if (activePageHeight != null) {
            onHeightChange?.invoke(activePageHeight)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            key = { page -> notifications.getOrNull(page)?.id ?: page.toString() },
            beyondViewportPageCount = 1
        ) { page ->
            val item = notifications.getOrNull(page) ?: return@HorizontalPager
            val pagerLabel = if (notifications.size > 1) "${page + 1} of ${notifications.size}" else null
            val isReplying = replyingNotificationId == item.id
            val currentReplyAction = if (isReplying) replyAction else null
            val template = resolveNotificationTemplate(item)

            when (template) {
                NotificationTemplateType.NAVIGATION -> {
                    NotificationNavigationCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = pagerLabel,
                        onHeightChange = { h -> pageHeights[item.id] = h }
                    )
                }
                NotificationTemplateType.TRANSPORT -> {
                    NotificationTransportCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = pagerLabel,
                        onHeightChange = { h -> pageHeights[item.id] = h }
                    )
                }
                NotificationTemplateType.PROGRESS -> {
                    NotificationProgressCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        pagerLabel = pagerLabel,
                        onHeightChange = { h -> pageHeights[item.id] = h }
                    )
                }
                NotificationTemplateType.PICTURE -> {
                    NotificationPictureCard(
                        notification = item,
                        onAction = { item.actions.firstOrNull()?.let { onActionSelected(item, it) } },
                        onDismiss = { onDismiss(item) },
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        pagerLabel = pagerLabel,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> pageHeights[item.id] = h }
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
                        chronometerCountDown = item.chronometerCountDown,
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> pageHeights[item.id] = h }
                    )
                }
                NotificationTemplateType.STANDARD -> {
                    NotificationIslandExpanded(
                        appName = item.appName,
                        appPackageName = item.packageName,
                        imagePath = item.imagePath,
                        sender = item.title.ifBlank { item.appName },
                        message = item.text,
                        expandedText = item.expandedText,
                        inboxLines = item.inboxLines,
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
                        chronometerCountDown = item.chronometerCountDown,
                        isReplying = isReplying,
                        replyAction = currentReplyAction,
                        onSendReply = onSendReply,
                        onCancelReply = onCancelReply,
                        isCardExpanded = isCardExpanded,
                        onToggleExpand = onToggleExpand,
                        onHeightChange = { h -> pageHeights[item.id] = h }
                    )
                }
            }
        }

        // Pager Indicator Dots (authentic iOS UIPageControl style with sliding window)
        // Displayed for multi-notification expanded pager (2+ notifications) without requiring extra gesture
        if (notifications.size > 1 && replyingNotificationId == null) {
            val totalPages = notifications.size
            val maxVisibleDots = 7
            val (startIndex, endIndex) = if (totalPages <= maxVisibleDots) {
                0 to (totalPages - 1)
            } else {
                val half = maxVisibleDots / 2
                val start = (currentPage - half).coerceIn(0, totalPages - maxVisibleDots)
                start to (start + maxVisibleDots - 1)
            }

            val reducedMotion = ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in startIndex..endIndex) {
                    val isCurrent = i == currentPage
                    val isEdge = (i == startIndex && startIndex > 0) || (i == endIndex && endIndex < totalPages - 1)
                    val targetDotSize = when {
                        isCurrent -> 6.5.dp
                        isEdge -> 3.5.dp
                        else -> 5.dp
                    }
                    val targetAlpha = when {
                        isCurrent -> 1f
                        isEdge -> 0.22f
                        else -> 0.45f
                    }

                    val dotSize by androidx.compose.animation.core.animateDpAsState(
                        targetValue = targetDotSize,
                        animationSpec = if (reducedMotion) {
                            androidx.compose.animation.core.snap()
                        } else {
                            androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                            )
                        },
                        label = "dotSize_$i"
                    )
                    val dotAlpha by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = targetAlpha,
                        animationSpec = if (reducedMotion) {
                            androidx.compose.animation.core.snap()
                        } else {
                            androidx.compose.animation.core.spring(
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                            )
                        },
                        label = "dotAlpha_$i"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = dotAlpha))
                    )
                }
            }
        }
    }
}
