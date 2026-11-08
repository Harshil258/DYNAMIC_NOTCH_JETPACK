package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.notification.*
import ai.emots.kishan_dynamic.ui.motion.*
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** A projection of the canonical engine snapshot, never a second notification queue. */
data class ChildStackPresentation(
    val selectedKey: String?, val ownership: IslandInteractionOwnership,
    val children: List<ChildNotice>, val visibleLimit: Int, val overflow: Int,
    val redacted: Boolean
)

fun IslandBehaviorState.childPresentation(availableHeightDp: Int, fontScale: Float, redacted: Boolean, maxVisible: Int = 3): ChildStackPresentation {
    val expanded = targetPresentation in setOf(IslandPresentationTarget.USER_EXPANDED,
        IslandPresentationTarget.PAGER, IslandPresentationTarget.TRANSIENT_PREVIEW, IslandPresentationTarget.REPLY)
    val effectiveFontScale = fontScale.coerceAtLeast(1f)
    val minRequiredHeight = (96 * effectiveFontScale).toInt()
    val limit = when {
        !expanded || ownership == IslandInteractionOwnership.INTERRUPTED || availableHeightDp < minRequiredHeight -> 0
        else -> (availableHeightDp / minRequiredHeight).coerceIn(1, maxVisible.coerceIn(1, 3))
    }
    val pending = childNotices.filter { it.key != selectedKey && queue.notifications.any { n -> n.id == it.key } }
    val safeChildren = pending.take(limit).map { child ->
        val source = queue.notifications.first { it.id == child.key }
        val safe = ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy.notification(source,
            childPrivacy.deviceLocked, childPrivacy.hideSensitiveContent)
        child.copy(title = safe.title, text = safe.text)
    }
    // When limit == 0, the secondary stack is collapsed; do not expose dangling overflow
    val overflow = if (limit == 0) 0 else (pending.size - limit).coerceAtLeast(0)
    return ChildStackPresentation(selectedKey, ownership, safeChildren, limit, overflow, redacted)
}

enum class ChildVisualPhase { QUEUED, EMERGING, SETTLING, VISIBLE, TOUCH_HELD, RELEASED, EXITING, MERGED, PROMOTING, MAIN_SELECTED }

/** One window, wrap-content. Animation registry retains only outgoing rendered identities. */
@Composable
fun LayeredMiniIslands(
    state: IslandBehaviorState, modifier: Modifier = Modifier, redacted: Boolean = false, enabled: Boolean = true,
    onEvent: (IslandEngineEvent) -> Unit, onPromote: (String, Long) -> Unit,
    main: @Composable () -> Unit
) {
    val config = LocalConfiguration.current
    var promoting by remember { mutableStateOf<Pair<String, Long>?>(null) }
    val reduced = LocalReducedMotion.current
    val currentPromote by rememberUpdatedState(onPromote)
    LaunchedEffect(promoting) {
        val key = promoting ?: return@LaunchedEffect
        delay(if (reduced) 0 else AppMotion.DurationMedium.toLong())
        currentPromote(key.first, key.second); promoting = null
    }
    var mainWidth by remember { mutableIntStateOf(0) }
    var mainHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val mainWidthDp = with(density) { mainWidth.toDp() }
    val available = config.screenHeightDp - with(density) { mainHeight.toDp().value.toInt() } - 96
    val p = state.childPresentation(available, config.fontScale, redacted, if (config.screenWidthDp < 393) 2 else 3)
    val presentedLimit = if (enabled) p.visibleLimit else 0
    LaunchedEffect(presentedLimit) {
        if (state.visibleChildLimit != presentedLimit) onEvent(IslandEngineEvent.ChildVisibleLimitChanged(presentedLimit))
    }
    val children = if (enabled && presentedLimit > 0) p.children else emptyList()
    val retained = remember { mutableStateMapOf<String, ChildNotice>() }
    SideEffect {
        if (!enabled || presentedLimit == 0) retained.clear() else {
            children.forEach { retained[it.key] = it }
            // Coalesce only old exit visuals under pressure, never pending arrivals.
            val visibleKeys = children.map { it.key }.toSet()
            val keep = visibleKeys + retained.keys.filterNot { it in visibleKeys }.takeLast(3)
            retained.keys.toList().filterNot { it in keep }.forEach { retained.remove(it) }
        }
    }
    // Satellite capsule width: distinctly narrower than main expanded island (32dp inset relative to parent)
    val satelliteWidthDp = (mainWidthDp - 32.dp).coerceIn(AppTheme.island.satelliteWidthMin, AppTheme.island.satelliteWidthMax)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.onSizeChanged { mainHeight = it.height; mainWidth = it.width }) { main() }
        if (enabled && presentedLimit > 0 && available > 0) {
            val order = (children.map { it.key } + retained.keys.filterNot { k -> children.any { it.key == k } }).distinct()
            LazyColumn(
                modifier = Modifier
                    .width(mainWidthDp.coerceAtLeast(48.dp))
                    .heightIn(max = available.coerceAtLeast(0).dp),
                userScrollEnabled = false,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(order, key = { it }) { key ->
                    val child = children.firstOrNull { it.key == key } ?: retained[key]
                    if (child != null) key(key, child.arrivalToken) {
                        val visible = children.any { it.key == key } && promoting?.first != key
                        MiniNotificationIsland(
                            child = child,
                            visible = visible,
                            redacted = p.redacted || (state.childPrivacy.deviceLocked && (state.childPrivacy.hideSensitiveContent || !visible)),
                            width = satelliteWidthDp,
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                                placementSpec = if (LocalReducedMotion.current) snap() else AppMotion.islandSpring()
                            ),
                            onHeld = { held -> onEvent(if (held) IslandEngineEvent.TouchChild(key) else IslandEngineEvent.ReleaseChild(key)) },
                            onPromote = { promoting = key to child.arrivalToken },
                            onPhase = { phase -> onEvent(IslandEngineEvent.ChildPhaseChanged(key, phase)) },
                            onMerged = { retained.remove(key) }
                        )
                    }
                }
            }

            // Stage 2: Safe height calculation for overflow affordance
            val overflowHeightDp = (28 * config.fontScale.coerceAtLeast(1f) + 4).dp
            val estimatedCardsHeightDp = (children.size * 42 * config.fontScale.coerceAtLeast(1f) + (children.size - 1).coerceAtLeast(0) * 4).dp
            val canFitOverflow = (available.dp >= estimatedCardsHeightDp + overflowHeightDp)

            if (p.overflow > 0 && canFitOverflow) {
                val next = state.childNotices.filter { it.key != state.selectedKey }.getOrNull(p.visibleLimit)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .height(AppTheme.island.overflowHeight)
                        .clickable(
                            enabled = next != null,
                            role = Role.Button,
                            onClickLabel = "View next queued notification in island"
                        ) {
                            next?.let { onPromote(it.key, it.arrivalToken) }
                        }
                        .background(Color(0xFF0C0E14), RoundedCornerShape(AppTheme.island.overflowCorner))
                        .border(0.75.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(AppTheme.island.overflowCorner))
                        .padding(horizontal = 12.dp)
                        .testTag("child_overflow"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "+${p.overflow} more",
                        style = AppTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = Color.White
                    )
                    Text(
                        text = "·",
                        style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Tap to view",
                        style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniNotificationIsland(
    child: ChildNotice,
    visible: Boolean,
    redacted: Boolean,
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onHeld: (Boolean) -> Unit,
    onPhase: (ChildLifecycle) -> Unit,
    onPromote: () -> Unit,
    onMerged: () -> Unit
) {
    val reduced = LocalReducedMotion.current
    val source = remember { MutableInteractionSource() }
    val held by source.collectIsPressedAsState()
    var phase by remember { mutableStateOf(ChildVisualPhase.QUEUED) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(held) {
        if (held) {
            phase = ChildVisualPhase.TOUCH_HELD
            onHeld(true)
        } else {
            if (phase == ChildVisualPhase.TOUCH_HELD) phase = ChildVisualPhase.RELEASED
            onHeld(false)
        }
    }
    LaunchedEffect(visible) {
        if (visible) {
            phase = ChildVisualPhase.EMERGING
            onPhase(ChildLifecycle.EMERGING)
            shown = true
            delay(if (reduced) 0 else AppMotion.StaggerStep)
            phase = ChildVisualPhase.SETTLING
            onPhase(ChildLifecycle.SETTLING)
            delay(if (reduced) 0 else AppMotion.DurationMedium.toLong())
            phase = ChildVisualPhase.VISIBLE
            onPhase(ChildLifecycle.VISIBLE)
        } else {
            phase = ChildVisualPhase.EXITING
            onPhase(ChildLifecycle.EXITING)
            shown = false
            delay(if (reduced) 0 else AppMotion.DurationMedium.toLong())
            phase = ChildVisualPhase.MERGED
            onPhase(ChildLifecycle.MERGED)
            onMerged()
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = if (reduced) snap() else AppMotion.islandSpring(),
        label = "child_emerge_merge"
    )
    val shape = RoundedCornerShape(AppTheme.island.satelliteCorner)
    val copyAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = if (reduced) snap() else tween(AppMotion.DurationFast, AppMotion.StaggerStep.toInt()),
        label = "child_copy_stagger"
    )
    Row(
        modifier = modifier
            .width(width.coerceAtLeast(48.dp))
            .clickable(
                enabled = visible,
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClickLabel = "Read notification in main island"
            ) {
                phase = ChildVisualPhase.PROMOTING
                onPhase(ChildLifecycle.PROMOTING)
                onPromote()
            }
            .graphicsLayer {
                alpha = progress
                translationY = if (reduced) 0f else -size.height * (1f - progress)
                scaleX = if (reduced) 1f else 0.94f + 0.06f * progress
                scaleY = if (reduced) 1f else 0.50f + 0.50f * progress
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .background(Color(0xFF000000), shape)
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), shape)
            .height(AppTheme.island.satelliteHeight)
            .padding(horizontal = 10.dp)
            .testTag("child_${child.key}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        PackageIcon(
            packageName = child.packageName,
            modifier = Modifier
                .size(AppTheme.island.satelliteIconSize)
                .clip(RoundedCornerShape(4.dp)),
            contentDescription = "${child.appName} icon"
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = copyAlpha },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val displayTitle = if (redacted) "Notification" else child.title.ifBlank { child.appName }
            val displayText = if (redacted) "Hidden" else child.text

            Text(
                text = displayTitle,
                style = AppTheme.typography.caption.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (displayText.isNotBlank()) {
                Text(
                    text = "·",
                    style = AppTheme.typography.caption.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Text(
                    text = displayText,
                    style = AppTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFA4A4A9),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}
