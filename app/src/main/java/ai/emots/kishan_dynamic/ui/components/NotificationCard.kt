package ai.emots.kishan_dynamic.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.NotificationActionGlyph
import ai.emots.kishan_dynamic.data.notification.NotificationActionVisual
import ai.emots.kishan_dynamic.data.notification.NotificationLayoutPolicy
import ai.emots.kishan_dynamic.data.notification.NotificationLayoutRecipe
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Universal adaptive notification card — the single expanded renderer shared by
 * the production overlay, the notification pager, and the debug gallery.
 *
 * The recipe comes from typed source capabilities (NotificationLayoutPolicy).
 * Every region is measured intrinsically: no canvas-ratio scaling, no fabricated
 * labels or progress, and absent regions occupy zero height.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdaptiveNotificationCard(
    notification: NotificationInfo,
    modifier: Modifier = Modifier,
    pagerLabel: String? = null,
    isReplying: Boolean = false,
    replyAction: NotificationActionInfo? = null,
    onSendReply: (String) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onActionSelected: (NotificationActionInfo) -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    onHeightChange: ((Dp) -> Unit)? = null
) {
    val density = LocalDensity.current
    val tokens = AppTheme.island
    val recipe = remember(notification) { NotificationLayoutPolicy.recipe(notification) }
    val dismissible = onDismiss != null && notification.isClearable

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth = maxWidth
        val fontScale = density.fontScale
        val narrow = cardWidth < 260.dp || fontScale > 1.3f
        val inset = if (recipe == NotificationLayoutRecipe.THUMBNAIL_ALERT) tokens.notificationContentInset else 16.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size -> with(density) { onHeightChange?.invoke(size.height.toDp()) } }
                .semantics {
                    contentDescription = notification.appName
                    if (dismissible) {
                        customActions = listOf(
                            CustomAccessibilityAction("Dismiss notification") { onDismiss?.invoke(); true }
                        )
                    }
                }
                .padding(horizontal = inset, vertical = inset)
                .testTag("adaptive_notification_card"),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            if (isReplying && replyAction != null) {
                NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                val preview = NotificationLayoutPolicy.bodyText(notification)
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        fontFamily = IslandReferenceFonts.Text,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = IslandColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                InlineReplyBox(
                    notificationKey = notification.id,
                    replyAction = replyAction,
                    onSendReply = onSendReply,
                    onCancelReply = onCancelReply
                )
                return@Column
            }

            when (recipe) {
                NotificationLayoutRecipe.THUMBNAIL_ALERT -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val textBlock: @Composable (Modifier) -> Unit = { textModifier ->
                            Column(textModifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                                NotificationHeadline(notification, maxLines = 1)
                                val body = NotificationLayoutPolicy.bodyText(notification)
                                if (body.isNotBlank()) {
                                    Text(
                                        text = body,
                                        fontFamily = IslandReferenceFonts.Display,
                                        fontSize = 16.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Light,
                                        color = IslandColors.TextSecondary,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (narrow) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                                NotificationThumbnailArtwork(notification)
                                textBlock(Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tokens.notificationImageTextGap)
                            ) {
                                NotificationThumbnailArtwork(notification)
                                textBlock(Modifier.weight(1f))
                            }
                        }
                        if (dismissible) {
                            NotificationDismissButton(
                                onDismiss = onDismiss!!,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                NotificationLayoutRecipe.LARGE_MEDIA -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationLargeMedia(notification)
                    NotificationTitleBody(notification)
                }

                NotificationLayoutRecipe.CONVERSATION -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationHeadline(notification, maxLines = 1)
                    NotificationConversationMessages(notification)
                }

                NotificationLayoutRecipe.INBOX_DIGEST -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationHeadline(notification, maxLines = 1)
                    NotificationInboxDigest(notification)
                }

                NotificationLayoutRecipe.PROGRESS -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) { NotificationHeadline(notification, maxLines = 1) }
                        if (notification.progressMax > 0) {
                            val percent = (notification.progress.toLong() * 100 / notification.progressMax)
                                .coerceIn(0, 100)
                            Text(
                                text = "$percent%",
                                fontFamily = IslandReferenceFonts.Text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                    IslandProgressBar(
                        fraction = if (notification.progressMax > 0) {
                            notification.progress.toFloat() / notification.progressMax.toFloat()
                        } else null
                    )
                    val body = NotificationLayoutPolicy.bodyText(notification)
                    if (body.isNotBlank() && body != notification.title) {
                        Text(
                            text = body,
                            fontFamily = IslandReferenceFonts.Text,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = IslandColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                NotificationLayoutRecipe.TIMER -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationHeadline(notification, maxLines = 1)
                    if (notification.chronometerBaseElapsedRealtime > 0L) {
                        NotificationChronometerText(
                            baseElapsedRealtime = notification.chronometerBaseElapsedRealtime,
                            countDown = notification.chronometerCountDown,
                            fontSize = 22.sp
                        )
                    }
                    val body = NotificationLayoutPolicy.bodyText(notification)
                    if (body.isNotBlank() && body != notification.title) {
                        Text(
                            text = body,
                            fontFamily = IslandReferenceFonts.Text,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = IslandColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                NotificationLayoutRecipe.NAVIGATION -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationHeadline(notification, maxLines = 2)
                    val detail = listOfNotNull(
                        notification.text.takeIf { it.isNotBlank() },
                        notification.subText
                    ).joinToString(" · ")
                    if (detail.isNotBlank()) {
                        Text(
                            text = detail,
                            fontFamily = IslandReferenceFonts.Text,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp,
                            color = IslandColors.TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                NotificationLayoutRecipe.GROUP_SUMMARY -> {
                    NotificationIdentityHeader(notification, pagerLabel, if (dismissible) onDismiss else null)
                    NotificationTitleBody(notification)
                    if (notification.groupChildCount > 1) {
                        Text(
                            text = "${notification.groupChildCount} notifications",
                            fontFamily = IslandReferenceFonts.Text,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = IslandColors.TextTertiary,
                            maxLines = 1
                        )
                    }
                }

                NotificationLayoutRecipe.TEXT_ALERT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotificationLeadingAvatar(
                            notification = notification,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                        ) {
                            NotificationHeadline(notification, maxLines = 1)
                            val body = NotificationLayoutPolicy.bodyText(notification)
                            val headline = NotificationLayoutPolicy.headline(notification)
                            if (body.isNotBlank() && body != headline) {
                                Text(
                                    text = body,
                                    fontFamily = IslandReferenceFonts.Text,
                                    fontSize = 15.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Light,
                                    color = IslandColors.TextSecondary
                                )
                            }
                        }
                        if (dismissible) {
                            NotificationDismissButton(
                                onDismiss = onDismiss!!,
                                modifier = Modifier.align(Alignment.Top)
                            )
                        }
                    }
                }
            }

            if (notification.actions.isNotEmpty()) {
                NotificationActionRow(
                    actions = notification.actions,
                    stackVertically = cardWidth < 240.dp || fontScale > 1.3f,
                    onActionSelected = onActionSelected
                )
            }
        }
    }
}

/**
 * Reports the small visual size while keeping the full accessible hit target.
 * The overflow is transparent hit clearance inside the card's own padding.
 */
private fun Modifier.visualWithTouchTarget(visualSize: Dp, targetSize: Dp): Modifier =
    this.layout { measurable, constraints ->
        val targetPx = targetSize.roundToPx()
        val visualPx = visualSize.roundToPx()
        val placeable = measurable.measure(Constraints.fixed(targetPx, targetPx))
        layout(visualPx, visualPx) {
            placeable.placeRelative(-(targetPx - visualPx) / 2, -(targetPx - visualPx) / 2)
        }
    }

@Composable
private fun NotificationDismissButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .visualWithTouchTarget(24.dp, AppTheme.island.minimumActionTarget)
            .clickable(role = Role.Button, onClick = onDismiss)
            .semantics { contentDescription = "Dismiss notification" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(IslandColors.Gray5)
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = AppleGlyph.Close, tint = IslandColors.TextSecondary, size = 9.dp)
        }
    }
}

/** One identity block per card: sender artwork when present, app icon otherwise. */
@Composable
private fun NotificationIdentityHeader(
    notification: NotificationInfo,
    pagerLabel: String?,
    onDismiss: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (notification.imagePath != null) {
            NotificationArtwork(
                imagePath = notification.imagePath,
                packageName = notification.packageName,
                modifier = Modifier.size(22.dp).clip(CircleShape),
                contentDescription = "${notification.appName} sender artwork"
            )
        } else {
            PackageIcon(
                packageName = notification.packageName,
                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)),
                contentDescription = "${notification.appName} icon"
            )
        }
        val caption = if (pagerLabel == null) notification.appName else "${notification.appName} · $pagerLabel"
        Text(
            text = caption,
            fontFamily = IslandReferenceFonts.Text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = IslandColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (notification.subText != null) {
            Text(
                text = notification.subText,
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 11.sp,
                color = IslandColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 96.dp)
            )
        }
        if (onDismiss != null) {
            NotificationDismissButton(onDismiss = onDismiss)
        }
    }
}

/** Compact alert identity: sender artwork when present, source app icon otherwise. */
@Composable
private fun NotificationLeadingAvatar(notification: NotificationInfo, modifier: Modifier = Modifier) {
    if (notification.imagePath != null) {
        NotificationArtwork(
            imagePath = notification.imagePath,
            packageName = notification.packageName,
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape),
            contentDescription = "${notification.appName} sender artwork"
        )
    } else {
        PackageIcon(
            packageName = notification.packageName,
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentDescription = "${notification.appName} icon"
        )
    }
}

@Composable
private fun NotificationHeadline(notification: NotificationInfo, maxLines: Int) {
    Text(
        text = NotificationLayoutPolicy.headline(notification),
        fontFamily = IslandReferenceFonts.Display,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun NotificationTitleBody(notification: NotificationInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        val headline = NotificationLayoutPolicy.headline(notification)
        val body = NotificationLayoutPolicy.bodyText(notification)
        if (headline.isNotBlank()) {
            NotificationHeadline(notification, maxLines = 2)
        }
        if (body.isNotBlank() && body != headline) {
            Text(
                text = body,
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Light,
                color = IslandColors.TextSecondary
            )
        } else if (headline.isBlank()) {
            Text(
                text = "New notification",
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 13.sp,
                color = IslandColors.TextSecondary
            )
        }
    }
}

/** Figma 27:992 leading attachment crop with the source app badge. */
@Composable
private fun NotificationThumbnailArtwork(notification: NotificationInfo) {
    val tokens = AppTheme.island
    Box(
        modifier = Modifier
            .size(tokens.notificationImageWidth, tokens.notificationImageHeight)
            .clip(RoundedCornerShape(tokens.notificationImageCorner))
            .testTag("notification_thumbnail")
    ) {
        NotificationArtwork(
            imagePath = notification.picturePath ?: notification.imagePath,
            packageName = notification.packageName,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "${notification.appName} attached image"
        )
        PackageIcon(
            packageName = notification.packageName,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(5.dp)
                .size(20.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black.copy(alpha = 0.55f), CircleShape),
            contentDescription = "${notification.appName} icon"
        )
    }
}

/** Genuine BigPicture attachment at its intrinsic aspect ratio, bounded for glanceability. */
@Composable
private fun NotificationLargeMedia(notification: NotificationInfo) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = notification.picturePath) {
        value = notification.picturePath?.let { path ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(path)
                    if (uri.scheme in setOf("content", "android.resource", "file")) {
                        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                    } else BitmapFactory.decodeFile(path)
                }.getOrNull()
            }
        }
    }
    val shape = RoundedCornerShape(AppTheme.island.notificationImageCorner)
    val loaded = bitmap
    if (loaded != null) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val ratio = loaded.width.toFloat() / loaded.height.coerceAtLeast(1).toFloat()
            val mediaHeight = (maxWidth.value / ratio).coerceIn(72f, 180f).dp
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = "${notification.appName} attached image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mediaHeight)
                    .clip(shape)
                    .testTag("notification_large_media"),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(shape)
                .background(IslandColors.Gray6)
        )
    }
}

@Composable
private fun NotificationConversationMessages(notification: NotificationInfo) {
    val messages = notification.messages.takeLast(3)
    val earlier = notification.messages.size - messages.size
    val headline = NotificationLayoutPolicy.headline(notification)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (earlier > 0) {
            Text(
                text = "+$earlier earlier ${if (earlier == 1) "message" else "messages"}",
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = IslandColors.TextTertiary,
                maxLines = 1
            )
        }
        messages.forEach { message ->
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                val sender = message.sender
                if (!sender.isNullOrBlank() && sender != headline) {
                    Text(
                        text = sender,
                        fontFamily = IslandReferenceFonts.Text,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = message.text,
                    fontFamily = IslandReferenceFonts.Text,
                    fontSize = 14.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = IslandColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NotificationInboxDigest(notification: NotificationInfo) {
    val lines = notification.inboxLines.take(5)
    val remaining = notification.inboxLines.size - lines.size
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        lines.forEach { line ->
            Text(
                text = line,
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = IslandColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (remaining > 0) {
            Text(
                text = "+$remaining more",
                fontFamily = IslandReferenceFonts.Text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = IslandColors.TextTertiary,
                maxLines = 1
            )
        }
    }
}

/** Determinate fill or honest indeterminate motion; never a fabricated percentage. */
@Composable
private fun IslandProgressBar(fraction: Float?) {
    val reducedMotion = ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion.current
    val trackShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(trackShape)
            .background(IslandColors.Gray4.copy(alpha = 0.55f))
            .testTag("notification_progress_bar")
    ) {
        if (fraction != null) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .background(Color.White)
            )
        } else if (!reducedMotion) {
            val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "indeterminate_progress")
            val offset by transition.animateFloat(
                initialValue = -1f,
                targetValue = 2.5f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(durationMillis = 1100, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                ),
                label = "indeterminate_offset"
            )
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .fillMaxSize()
                    .graphicsLayer { translationX = offset * size.width }
                    .background(Color.White)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationActionRow(
    actions: List<NotificationActionInfo>,
    stackVertically: Boolean,
    onActionSelected: (NotificationActionInfo) -> Unit
) {
    val firstForwardId = remember(actions) {
        actions.firstOrNull { NotificationLayoutPolicy.isForwardAction(it) }?.id
    }
    val content: @Composable () -> Unit = {
        actions.forEach { action ->
            val presentation = NotificationLayoutPolicy.actionPresentation(
                action = action,
                isFirstForwardAction = action.id == firstForwardId
            )
            NotificationSourceActionPill(
                action = action,
                presentation = presentation,
                fillWidth = stackVertically,
                onClick = { onActionSelected(action) }
            )
        }
    }
    if (stackVertically) {
        Column(
            modifier = Modifier.fillMaxWidth().testTag("notification_action_row"),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) { content() }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth().testTag("notification_action_row"),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) { content() }
    }
}

@Composable
private fun NotificationSourceActionPill(
    action: NotificationActionInfo,
    presentation: ai.emots.kishan_dynamic.data.notification.NotificationActionPresentation,
    fillWidth: Boolean,
    onClick: () -> Unit
) {
    var inFlight by remember(action.id) { mutableStateOf(false) }
    val style = when (presentation.style) {
        NotificationActionVisual.PRIMARY -> IslandActionStyle.Primary
        NotificationActionVisual.POSITIVE -> IslandActionStyle.Positive
        NotificationActionVisual.DESTRUCTIVE -> IslandActionStyle.Destructive
        NotificationActionVisual.SECONDARY -> IslandActionStyle.Secondary
    }
    val glyph = when (presentation.glyph) {
        NotificationActionGlyph.CHAT -> AppleGlyph.Chat
        NotificationActionGlyph.CHEVRON_RIGHT -> AppleGlyph.ChevronRight
        NotificationActionGlyph.PHONE -> AppleGlyph.Phone
        NotificationActionGlyph.CLOSE -> AppleGlyph.Close
        NotificationActionGlyph.CHECK -> AppleGlyph.Check
        NotificationActionGlyph.BELL_SLASH -> AppleGlyph.BellSlash
        else -> null
    }
    IslandActionPill(
        label = action.label,
        onClick = {
            if (!inFlight) {
                inFlight = true
                onClick()
            }
        },
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
        glyph = glyph,
        style = style,
        accent = if (style == IslandActionStyle.Secondary) IslandSemanticAccent.Neutral else null,
        visualHeight = 36.dp,
        minTouchTarget = AppTheme.island.minimumActionTarget,
        contentDescription = action.label,
        fillWidth = fillWidth
    )
}