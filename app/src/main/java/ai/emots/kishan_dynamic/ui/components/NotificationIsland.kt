package ai.emots.kishan_dynamic.ui.components

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.DragInteraction
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
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

/** Injection seam keeps debug/editor fixtures out of the production in-memory draft store. */
val LocalIslandReplyDraftStore = androidx.compose.runtime.staticCompositionLocalOf {
    ai.emots.kishan_dynamic.data.notification.ReplyDraftMemory.store
}

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
    notificationKey: String? = null,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val draftStore = LocalIslandReplyDraftStore.current
    var replyText by remember(notificationKey, replyAction.id) {
        mutableStateOf(notificationKey?.let { draftStore.read(it, replyAction.id) }.orEmpty())
    }
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
                onValueChange = { replyText = it; notificationKey?.let { key -> draftStore.write(key, replyAction.id, it) } },
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
                    .clickable(enabled = canSend, role = androidx.compose.ui.semantics.Role.Button) {
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
    notificationKey: String? = null,
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
    picturePath: String? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.68f, 1.15f)
        val hPad = 16.dp * scale
        val vPad = 12.dp * scale

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
                    .padding(horizontal = hPad, vertical = vPad),
                verticalArrangement = Arrangement.spacedBy(8.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp * scale),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        NotificationArtwork(
                            imagePath = imagePath,
                            packageName = appPackageName,
                            modifier = Modifier
                                .size(18.dp * scale)
                                .clip(RoundedCornerShape(4.dp * scale)),
                            contentDescription = "$appName icon"
                        )
                        AppText(
                            text = if (pagerLabel == null) "$appName · $sender" else "$appName · $sender · $pagerLabel",
                            style = AppTheme.typography.caption,
                            fontSize = (11.5f * scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (onDismiss != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp * scale)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C2E))
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Close,
                                tint = Color(0xFFA4A4A9),
                                size = 9.dp * scale
                            )
                        }
                    }
                }

                if (message.isNotBlank()) {
                    AppText(
                        text = message,
                        style = AppTheme.typography.caption,
                        fontSize = (12.5f * scale).sp,
                        color = Color(0xFFDDDDDD),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                InlineReplyBox(
                    notificationKey = notificationKey,
                    replyAction = replyAction,
                    onSendReply = onSendReply,
                    onCancelReply = onCancelReply,
                    scale = scale
                )
            }
        } else {
            val tokens = AppTheme.island
            val hasPicture = picturePath != null
            val stackArtwork = hasPicture && (density.fontScale > 1.3f || maxWidth < 260.dp)
            val vPad = if (hasPicture) tokens.notificationContentInset else 14.dp
            val hPad = if (hasPicture) tokens.notificationContentInset else 16.dp
            val bottomPad = if (hasPicture) tokens.notificationContentInset else if (actions.isNotEmpty() || actionLabel.isNotBlank()) 12.dp else 14.dp
            Column(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .onSizeChanged { size -> with(density) { onHeightChange?.invoke(size.height.toDp()) } }
                    .semantics {
                        contentDescription = appName
                        if (onDismiss != null) customActions = listOf(
                            CustomAccessibilityAction("Dismiss notification") { onDismiss(); true })
                    }
                    .padding(start = hPad, end = if (pagerLabel != null && !hasPicture) 36.dp else hPad, top = vPad, bottom = bottomPad),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                val artwork: @Composable () -> Unit = {
                    NotificationArtwork(imagePath = picturePath ?: imagePath, packageName = appPackageName,
                        modifier = (if (picturePath != null) Modifier.size(tokens.notificationImageWidth, tokens.notificationImageHeight)
                            .clip(RoundedCornerShape(tokens.notificationImageCorner))
                            else Modifier.size(tokens.callAvatarSize).clip(CircleShape)),
                        contentDescription = "$appName image")
                }
                val messageCopy: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                        AppText(text = sender, style = AppTheme.typography.islandTitle,
                            fontFamily = ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts.Display,
                            fontSize = 16.sp, lineHeight = 19.sp,
                            color = Color.White, fontWeight = FontWeight.Medium)
                        val fullMessageText = when {
                            inboxLines.isNotEmpty() -> inboxLines.joinToString("\n")
                            !expandedText.isNullOrBlank() -> expandedText
                            else -> message
                        }
                        if (fullMessageText.isNotBlank()) AppText(text = fullMessageText,
                            style = AppTheme.typography.body, fontFamily = ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts.Display,
                            fontSize = 16.sp, lineHeight = 17.sp,
                            fontWeight = FontWeight.Light, letterSpacing = (-.075).sp, color = IslandColors.TextSecondary)
                        if (showChronometer && chronometerBaseElapsedRealtime > 0L) {
                            NotificationChronometerText(baseElapsedRealtime = chronometerBaseElapsedRealtime,
                                countDown = chronometerCountDown)
                        }
                        if (progress != null || isProgressIndeterminate) {
                            if (progress != null) androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(), color = Color.White)
                            else androidx.compose.material3.LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(), color = Color.White)
                        }
                    }
                }
                if (stackArtwork) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                        artwork()
                        messageCopy()
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.notificationImageTextGap)) {
                        artwork()
                        Box(Modifier.weight(1f)) { messageCopy() }
                    }
                }
                if (actions.isNotEmpty() || actionLabel.isNotBlank()) {
                    FlowRow(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                        if (actions.isNotEmpty()) actions.forEach { action ->
                            NotificationActionButton(label = action.label, scale = 1f,
                                onClick = { onActionSelected(action) })
                        } else NotificationActionButton(label = actionLabel, scale = 1f, onClick = onAction)
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
fun IslandActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: AppleGlyph? = null,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    scale: Float = 1f
) {
    val reducedMotion = ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "chip_scale"
    )
    val h = (32.dp * scale).coerceAtLeast(28.dp)
    val r = h / 2
    val shape = RoundedCornerShape(r)
    val bg = when {
        isPrimary -> Color(0xFF0A84FF)
        isDestructive -> Color(0x33FA3532)
        else -> Color(0xFF2C2C2E)
    }
    val textColor = when {
        isPrimary -> Color.White
        isDestructive -> Color(0xFFFF453A)
        else -> Color.White
    }
    val borderBrush = when {
        isPrimary -> null
        isDestructive -> Brush.linearGradient(listOf(Color(0x66FA3532), Color(0x33FA3532)))
        else -> Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x14FFFFFF)))
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .graphicsLayer {
                    val s = if (reducedMotion) 1f else scaleState.value
                    scaleX = s
                    scaleY = s
                }
                .height(h)
                .clip(shape)
                .background(bg, shape)
                .then(if (borderBrush != null) Modifier.border(0.5.dp, borderBrush, shape) else Modifier)
                .padding(horizontal = (12.dp * scale).coerceAtLeast(10.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale)
        ) {
            if (glyph != null) {
                AppleIcon(glyph = glyph, tint = textColor, size = 13.dp * scale)
            }
            Text(
                text = text,
                color = textColor,
                fontSize = (12.5f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotificationActionButton(
    label: String,
    scale: Float,
    onClick: () -> Unit
) {
    val isDestructive = label.contains("Dismiss", ignoreCase = true) ||
        label.contains("Delete", ignoreCase = true) ||
        label.contains("Decline", ignoreCase = true) ||
        label.contains("Cancel", ignoreCase = true)
    val isPrimary = label.contains("Reply", ignoreCase = true) ||
        label.contains("Open", ignoreCase = true) ||
        label.contains("Join", ignoreCase = true) ||
        label.contains("Copy", ignoreCase = true) ||
        label.contains("Accept", ignoreCase = true)

    IslandActionChip(
        text = label,
        onClick = onClick,
        isPrimary = isPrimary,
        isDestructive = isDestructive,
        scale = scale
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
            .size((28.dp * scale).coerceAtLeast(24.dp))
            .clip(CircleShape)
            .background(Color(0xFF2C2C2E))
            .border(0.75.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp * scale, fontWeight = FontWeight.SemiBold)
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
        val hPad = (16.dp * scale)
        val vPad = (12.dp * scale)
        val iconSize = (40.dp * scale)

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
        val hPad = (16.dp * scale)
        val vPad = (12.dp * scale)
        val iconSize = (40.dp * scale)

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
        val hPad = (16.dp * scale)
        val vPad = (12.dp * scale)
        val iconSize = (40.dp * scale)

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
    onActionSelected: ((NotificationActionInfo) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    NotificationIslandExpanded(
        notificationKey = notification.id, appName = notification.appName,
        appPackageName = notification.packageName, imagePath = notification.imagePath,
        picturePath = notification.picturePath ?: notification.imagePath,
        sender = notification.title.ifBlank { notification.appName }, message = notification.text,
        expandedText = notification.expandedText, inboxLines = notification.inboxLines,
        isCardExpanded = isCardExpanded, onToggleExpand = onToggleExpand,
        onDismiss = onDismiss, actionLabel = "", actions = notification.actions,
        onActionSelected = { action -> onActionSelected?.invoke(action) ?: onAction?.invoke() },
        pagerLabel = pagerLabel, isReplying = isReplying, replyAction = replyAction,
        onSendReply = onSendReply, onCancelReply = onCancelReply,
        onHeightChange = onHeightChange, modifier = modifier
    )
}

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

    val pagerState = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, notifications.lastIndex),
        pageCount = { notifications.size }
    )
    var userPaging by remember { mutableStateOf(false) }
    val latestNotifications by rememberUpdatedState(notifications)
    val latestOnPageChanged by rememberUpdatedState(onPageChanged)
    val notificationIds = notifications.map { it.id }
    val selectedId = notifications.getOrNull(activeIndex)?.id

    LaunchedEffect(pagerState) {
        pagerState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                userPaging = true
                onUserInteraction()
            }
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

    val tokens = AppTheme.island
    val editing = replyingNotificationId != null
    val accessibleRail = LocalDensity.current.fontScale > 1.3f || LocalConfiguration.current.screenWidthDp < 330
    val countScale = LocalDensity.current.fontScale.coerceIn(1f, 2f)
    val railReserve = if (accessibleRail) tokens.notificationCountSize * countScale + AppTheme.spacing.lg else tokens.notificationRailReserve
    val currentNotification = notifications.getOrNull(currentPage)
    val hasPicture = currentNotification?.picturePath != null
    val defaultInitialHeight = if (hasPicture) tokens.notificationPagerMinHeight else 86.dp
    val viewportHeight = (activePageHeight ?: defaultInitialHeight)
        .coerceIn(86.dp, (LocalConfiguration.current.screenHeightDp.dp - 64.dp).coerceAtLeast(86.dp))
    val scope = rememberCoroutineScope()
    val pagingInput = remember(pagerState) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                // A scrollable page can transfer motion to its parent pager without
                // emitting the pager's own DragInteraction.Start.
                if (source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput && available.y != 0f) {
                    userPaging = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    Box(modifier = modifier.fillMaxWidth().height(viewportHeight).nestedScroll(pagingInput).testTag("notification_vertical_pager")
        .semantics { stateDescription = "Notification ${currentPage + 1} of ${notifications.size}" }) {
        VerticalPager(
            state = pagerState,
            userScrollEnabled = notifications.size > 1 && !editing,
            modifier = Modifier.fillMaxSize().padding(end = if (editing) 0.dp else railReserve),
            key = { page -> notifications.getOrNull(page)?.id ?: page.toString() },
            beyondViewportPageCount = 1
        ) { page ->
            val item = notifications.getOrNull(page) ?: return@VerticalPager
            val pagerLabel = if (notifications.size > 1) "${page + 1} of ${notifications.size}" else null
            val isReplying = replyingNotificationId == item.id
            val currentReplyAction = if (isReplying) replyAction else null
            val template = resolveNotificationTemplate(item)

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
                        onActionSelected = { action -> onActionSelected(item, action) },
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
                        notificationKey = item.id,
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
                        notificationKey = item.id,
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
        }

        if (!editing) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(top = tokens.notificationCountTop, end = if (accessibleRail) AppTheme.spacing.sm else tokens.notificationCountEnd)
                    .width(tokens.notificationCountSize * countScale)
                    .testTag("notification_vertical_indicator")
                    .semantics {
                        contentDescription = "${notifications.size} notifications"
                        stateDescription = "${currentPage + 1} of ${notifications.size}"
                        customActions = listOfNotNull(
                            if (currentPage > 0) CustomAccessibilityAction("Previous notification") {
                                onUserInteraction(); userPaging = true
                                scope.launch { pagerState.animateScrollToPage(currentPage - 1) }; true
                            } else null,
                            if (currentPage < notifications.lastIndex) CustomAccessibilityAction("Next notification") {
                                onUserInteraction(); userPaging = true
                                scope.launch { pagerState.animateScrollToPage(currentPage + 1) }; true
                            } else null)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NotificationCountSymbol(notifications.size)
                if (notifications.size > 1) {
                    Spacer(Modifier.height(tokens.notificationCountToDotsGap))
                    val start = (currentPage - 1).coerceIn(0, (notifications.size - 3).coerceAtLeast(0))
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.notificationDotGap)) {
                        for (i in start until minOf(start + 3, notifications.size)) {
                            Box(Modifier.size(tokens.notificationDotSize).clip(CircleShape)
                                .background(Color.White.copy(alpha = if (i == currentPage) 1f else .35f))
                                .testTag("notification_dot_$i"))
                        }
                    }
                }
            }
        }
    }
}

/** The outlined speech bubble and live count are distinct source-backed layers in Figma27:1007. */
@Composable
internal fun NotificationCountSymbol(count: Int, modifier: Modifier = Modifier) {
    val visualScale = LocalDensity.current.fontScale.coerceIn(1f, 2f)
    Box(modifier.size((if (count > 9) 28.dp else AppTheme.island.notificationCountSize) * visualScale), contentAlignment = Alignment.Center) {
        AppleIcon(AppleGlyph.Chat, size = (if (count > 9) 24.dp else 16.dp) * visualScale, tint = Color.White)
        Text(text = if (count > 99) "99+" else count.coerceAtLeast(0).toString(),
            fontSize = 10.sp, lineHeight = 12.sp,
            fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1)
    }
}
