package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.NotificationChronometerPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationLayoutPolicy
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState

/** Quiet window separating a true pager rest from the drag-to-fling handoff gap. */
private const val PagerSettleGraceMs = 120L

/** Injection seam keeps debug/editor fixtures out of the production in-memory draft store. */
val LocalIslandReplyDraftStore = androidx.compose.runtime.staticCompositionLocalOf {
    ai.emots.kishan_dynamic.data.notification.ReplyDraftMemory.store
}

/**
 * Inline reply input box with authentic Apple styling and direct FocusRequester.
 * Drafts persist per notification key + action id and never cross source identity.
 */
@Composable
fun InlineReplyBox(
    replyAction: NotificationActionInfo,
    onSendReply: (String) -> Unit,
    onCancelReply: () -> Unit,
    notificationKey: String? = null,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF242426))
                .border(0.75.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = replyText,
                onValueChange = { replyText = it; notificationKey?.let { key -> draftStore.write(key, replyAction.id, it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF007AFF)),
                maxLines = 2,
                decorationBox = { innerTextField ->
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Reply to ${replyAction.label.ifBlank { "message" }}...",
                            color = Color(0xFF8E8E93),
                            fontSize = 13.5.sp
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
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF3A3A3C))
                    .clickable(role = Role.Button, onClick = onCancelReply)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val canSend = replyText.isNotBlank()
            Box(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (canSend) Color(0xFF007AFF) else Color(0xFF007AFF).copy(alpha = 0.35f))
                    .clickable(enabled = canSend, role = Role.Button) {
                        onSendReply(replyText.trim())
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send",
                    color = if (canSend) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Compatibility bridge for legacy callers and instrumented fixtures.
 * Builds a typed source from the historical parameter list and delegates to the
 * universal [AdaptiveNotificationCard]; no second rendering path exists.
 */
@Composable
fun NotificationIslandExpanded(
    notificationKey: String? = null,
    appName: String = "WhatsApp",
    appPackageName: String? = null,
    imagePath: String? = null,
    sender: String = "",
    message: String = "",
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
    val effectiveActions = if (actions.isNotEmpty()) actions else if (actionLabel.isNotBlank()) {
        listOf(NotificationActionInfo(id = "legacy-action", label = actionLabel))
    } else emptyList()

    val source = NotificationInfo(
        id = notificationKey ?: "expanded-preview",
        packageName = appPackageName.orEmpty(),
        appName = appName,
        title = sender,
        text = message,
        expandedText = expandedText ?: message,
        inboxLines = inboxLines,
        imagePath = imagePath,
        picturePath = picturePath,
        actions = effectiveActions,
        progress = progress?.let { (it.coerceIn(0f, 1f) * 100).toInt() } ?: 0,
        progressMax = if (progress != null) 100 else 0,
        isProgressIndeterminate = isProgressIndeterminate,
        showChronometer = showChronometer,
        chronometerBaseElapsedRealtime = chronometerBaseElapsedRealtime,
        chronometerCountDown = chronometerCountDown
    )

    AdaptiveNotificationCard(
        notification = source,
        modifier = modifier,
        pagerLabel = pagerLabel,
        isReplying = isReplying,
        replyAction = replyAction,
        onSendReply = onSendReply,
        onCancelReply = onCancelReply,
        onActionSelected = { action ->
            if (actions.isEmpty() && action.id == "legacy-action") onAction() else onActionSelected(action)
        },
        onDismiss = onDismiss,
        onHeightChange = onHeightChange
    )
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
    val h = (36.dp * scale).coerceAtLeast(32.dp)
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
                .padding(horizontal = (14.dp * scale).coerceAtLeast(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
        ) {
            if (glyph != null) {
                AppleIcon(glyph = glyph, tint = textColor, size = (16.dp * scale).coerceAtLeast(14.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = (13f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Legacy presentation taxonomy, retained for classification diagnostics. */
typealias NotificationTemplateType = ai.emots.kishan_dynamic.data.notification.NotificationPresentationType

fun resolveNotificationTemplate(notification: NotificationInfo): NotificationTemplateType {
    return ai.emots.kishan_dynamic.data.notification.NotificationPresentationPolicy.classify(notification)
}

/** First-frame estimate only; measured content heights are authoritative. */
internal fun estimateNotificationPageHeight(item: NotificationInfo, isReplying: Boolean = false): androidx.compose.ui.unit.Dp =
    NotificationLayoutPolicy.estimatePageHeight(item, isReplying)

/**
 * Horizontal notification pager — the approved v7.1 interaction contract:
 * stable per-source keys, settled-page selection commit, non-collapsing drag
 * height, per-source measured heights, and the accessible count/dot rail.
 * Every page renders through the single universal [AdaptiveNotificationCard].
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

    val pagerState = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, notifications.lastIndex),
        pageCount = { notifications.size }
    )
    var userPaging by remember { mutableStateOf(false) }
    val latestNotifications by rememberUpdatedState(notifications)
    val latestOnPageChanged by rememberUpdatedState(onPageChanged)
    val latestActiveIndex by rememberUpdatedState(activeIndex)
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
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling || !userPaging) return@collect
                // Drag-end can report "not scrolling" for a moment before the fling
                // starts; committing there would select an intermediate page and
                // swallow the final settle. Only commit after a quiet rest window.
                delay(PagerSettleGraceMs)
                if (pagerState.isScrollInProgress || !userPaging) return@collect
                userPaging = false
                val settledKey = latestNotifications.getOrNull(pagerState.settledPage)?.id
                if (settledKey != null) {
                    val settledIndex = ai.emots.kishan_dynamic.data.notification.NotificationPagerSelectionPolicy
                        .settledIndex(latestNotifications.map { it.id }, settledKey)
                    if (settledIndex != null && settledIndex != latestActiveIndex) {
                        latestOnPageChanged(settledIndex)
                    }
                }
            }
    }

    LaunchedEffect(selectedId, notificationIds) {
        if (!pagerState.isScrollInProgress) {
            val targetPage = notifications.indexOfFirst { it.id == selectedId }
            if (targetPage in notifications.indices && pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    val pageHeights = remember { mutableStateMapOf<String, androidx.compose.ui.unit.Dp>() }
    LaunchedEffect(notificationIds) {
        pageHeights.keys.toList().filterNot { it in notificationIds }.forEach { pageHeights.remove(it) }
    }

    val currentPage = pagerState.currentPage.coerceIn(notifications.indices)
    val currentItem = notifications.getOrNull(currentPage)
    val currentMeasured = currentItem?.let {
        (pageHeights[it.id] ?: estimateNotificationPageHeight(it, replyingNotificationId == it.id)).coerceAtLeast(86.dp)
    } ?: 86.dp

    val activeHeight = if (pagerState.isScrollInProgress && notifications.size > 1) {
        val offset = pagerState.currentPageOffsetFraction
        val targetPage = (if (offset > 0) currentPage + 1 else if (offset < 0) currentPage - 1 else currentPage)
            .coerceIn(notifications.indices)
        val targetItem = notifications[targetPage]
        val targetMeasured = (pageHeights[targetItem.id] ?: estimateNotificationPageHeight(targetItem, replyingNotificationId == targetItem.id)).coerceAtLeast(86.dp)
        maxOf(currentMeasured, androidx.compose.ui.unit.lerp(currentMeasured, targetMeasured, kotlin.math.abs(offset)))
    } else {
        val settledIdx = pagerState.settledPage.coerceIn(notifications.indices)
        val settledItem = notifications.getOrNull(settledIdx)
        settledItem?.let {
            (pageHeights[it.id] ?: estimateNotificationPageHeight(it, replyingNotificationId == it.id)).coerceAtLeast(86.dp)
        } ?: currentMeasured
    }

    val viewportHeight = activeHeight.coerceIn(86.dp, (LocalConfiguration.current.screenHeightDp.dp - 64.dp).coerceAtLeast(86.dp))

    LaunchedEffect(viewportHeight) {
        onHeightChange?.invoke(viewportHeight)
    }

    val tokens = AppTheme.island
    val editing = replyingNotificationId != null
    val accessibleRail = LocalDensity.current.fontScale > 1.3f || LocalConfiguration.current.screenWidthDp < 330
    val countScale = LocalDensity.current.fontScale.coerceIn(1f, 2f)
    val railReserve = if (accessibleRail) tokens.notificationCountSize * countScale + AppTheme.spacing.lg else tokens.notificationRailReserve
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(viewportHeight)
            .testTag("notification_vertical_pager")
            .testTag("notification_horizontal_pager")
            .semantics { stateDescription = "Notification ${currentPage + 1} of ${notifications.size}" }
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = notifications.size > 1 && !editing,
            modifier = Modifier.fillMaxSize(),
            key = { page -> notifications.getOrNull(page)?.id ?: page.toString() },
            beyondViewportPageCount = 1
        ) { page ->
            val item = notifications.getOrNull(page) ?: return@HorizontalPager
            val pagerLabel = if (notifications.size > 1) "${page + 1} of ${notifications.size}" else null
            val isReplying = replyingNotificationId == item.id
            val currentReplyAction = if (isReplying) replyAction else null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = if (editing) 0.dp else railReserve)
                    .verticalScroll(rememberScrollState())
            ) {
                AdaptiveNotificationCard(
                    notification = item,
                    pagerLabel = pagerLabel,
                    isReplying = isReplying,
                    replyAction = currentReplyAction,
                    onSendReply = onSendReply,
                    onCancelReply = onCancelReply,
                    onActionSelected = { action -> onActionSelected(item, action) },
                    onDismiss = if (item.isClearable) {
                        { onDismiss(item) }
                    } else null,
                    onHeightChange = { h ->
                        val clamped = h.coerceAtLeast(86.dp)
                        val existing = pageHeights[item.id]
                        if (existing == null || kotlin.math.abs(existing.value - clamped.value) > 0.5f) {
                            pageHeights[item.id] = clamped
                        }
                    }
                )
            }
        }

        if (!editing) {
            val dotActiveIndex = if (pagerState.isScrollInProgress) {
                pagerState.settledPage.coerceIn(notifications.indices)
            } else {
                currentPage
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = tokens.notificationCountTop,
                        end = if (accessibleRail) AppTheme.spacing.sm else tokens.notificationCountEnd
                    )
                    .width(tokens.notificationCountSize * countScale)
                    .testTag("notification_vertical_indicator")
                    .semantics {
                        contentDescription = "${notifications.size} notifications"
                        stateDescription = "${currentPage + 1} of ${notifications.size}"
                        customActions = listOfNotNull(
                            if (currentPage > 0) CustomAccessibilityAction("Previous notification") {
                                onUserInteraction()
                                userPaging = true
                                scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                                true
                            } else null,
                            if (currentPage < notifications.lastIndex) CustomAccessibilityAction("Next notification") {
                                onUserInteraction()
                                userPaging = true
                                scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                                true
                            } else null
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NotificationCountSymbol(notifications.size)
                if (notifications.size > 1) {
                    Spacer(Modifier.height(tokens.notificationCountToDotsGap))
                    val start = (dotActiveIndex - 1).coerceIn(0, (notifications.size - 3).coerceAtLeast(0))
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.notificationDotGap)) {
                        for (i in start until minOf(start + 3, notifications.size)) {
                            Box(
                                Modifier
                                    .size(tokens.notificationDotSize)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (i == dotActiveIndex) 1f else .35f))
                                    .testTag("notification_dot_$i")
                            )
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