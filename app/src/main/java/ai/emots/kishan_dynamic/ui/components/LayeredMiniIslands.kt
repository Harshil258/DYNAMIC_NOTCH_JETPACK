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
    val limit = when {
        !expanded || ownership == IslandInteractionOwnership.INTERRUPTED || availableHeightDp < 96 -> 0
        else -> (availableHeightDp / (96 * fontScale.coerceAtLeast(1f))).toInt().coerceIn(1, maxVisible.coerceIn(1, 3))
    }
    val pending = childNotices.filter { it.key != selectedKey && queue.notifications.any { n -> n.id == it.key } }
    val safeChildren = pending.take(limit).map { child ->
        val source = queue.notifications.first { it.id == child.key }
        val safe = ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy.notification(source,
            childPrivacy.deviceLocked, childPrivacy.hideSensitiveContent)
        child.copy(title = safe.title, text = safe.text)
    }
    return ChildStackPresentation(selectedKey, ownership, safeChildren, limit,
        (pending.size - limit).coerceAtLeast(0), redacted)
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
    val available = config.screenHeightDp - with(density) { mainHeight.toDp().value.toInt() } - 96
    val p = state.childPresentation(available, config.fontScale, redacted, if (config.screenWidthDp < 393) 2 else 3)
    val presentedLimit = if (enabled) p.visibleLimit else 0
    LaunchedEffect(presentedLimit) {
        if (state.visibleChildLimit != presentedLimit) onEvent(IslandEngineEvent.ChildVisibleLimitChanged(presentedLimit))
    }
    val children = if (enabled) p.children else emptyList()
    val retained = remember { mutableStateMapOf<String, ChildNotice>() }
    SideEffect {
        if (!enabled) retained.clear() else {
            children.forEach { retained[it.key] = it }
            // Coalesce only old exit visuals under pressure, never pending arrivals.
            val visibleKeys = children.map { it.key }.toSet()
            val keep = visibleKeys + retained.keys.filterNot { it in visibleKeys }.takeLast(3)
            retained.keys.toList().filterNot { it in keep }.forEach { retained.remove(it) }
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.onSizeChanged { mainHeight = it.height; mainWidth = it.width }) { main() }
        // Reserve no full-screen layer: only actual visible cards plus their spacing are measured.
        val order = (children.map { it.key } + retained.keys.filterNot { k -> children.any { it.key == k } }).distinct()
        LazyColumn(Modifier.width(with(density) { mainWidth.toDp() }.coerceAtLeast(48.dp))
            .heightIn(max = available.coerceAtLeast(0).dp), userScrollEnabled = false,
            horizontalAlignment = Alignment.CenterHorizontally) {
        items(if (enabled) order else emptyList(), key = { it }) { key ->
            val child = children.firstOrNull { it.key == key } ?: retained[key]
            if (child != null) key(key, child.arrivalToken) {
                val visible = children.any { it.key == key } && promoting?.first != key
                MiniNotificationIsland(child, visible, p.redacted || (state.childPrivacy.deviceLocked && (state.childPrivacy.hideSensitiveContent || !visible)),
                    width = with(density) { mainWidth.toDp() },
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null,
                        placementSpec = if (LocalReducedMotion.current) snap() else AppMotion.islandSpring()),
                    onHeld = { held -> onEvent(if (held) IslandEngineEvent.TouchChild(key) else IslandEngineEvent.ReleaseChild(key)) },
                    onPromote = { promoting = key to child.arrivalToken }, onPhase = { phase -> onEvent(IslandEngineEvent.ChildPhaseChanged(key, phase)) },
                    onMerged = { retained.remove(key) })
            }
        }
        }
        if (enabled && p.overflow > 0) {
            val next = state.childNotices.filter { it.key != state.selectedKey }.getOrNull(p.visibleLimit)
            Text("+${p.overflow} new · view next", style = AppTheme.typography.caption, color = Color.White,
                modifier = Modifier.width(with(density) { mainWidth.toDp() }.coerceAtLeast(48.dp))
                    .heightIn(min = 48.dp).clickable(enabled = next != null, role = Role.Button) {
                        next?.let { onPromote(it.key, it.arrivalToken) }
                    }.background(Color.Black, RoundedCornerShape(AppTheme.spacing.lg))
                    .wrapContentSize().padding(AppTheme.spacing.sm).testTag("child_overflow"))
        }
    }
}

@Composable
fun MiniNotificationIsland(child: ChildNotice, visible: Boolean, redacted: Boolean, width: androidx.compose.ui.unit.Dp,
                           modifier: Modifier = Modifier, onHeld: (Boolean) -> Unit, onPhase: (ChildLifecycle) -> Unit, onPromote: () -> Unit, onMerged: () -> Unit) {
    val reduced = LocalReducedMotion.current
    val source = remember { MutableInteractionSource() }
    val held by source.collectIsPressedAsState()
    var phase by remember { mutableStateOf(ChildVisualPhase.QUEUED) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(held) { if (held) { phase = ChildVisualPhase.TOUCH_HELD; onHeld(true) }
        else { if (phase == ChildVisualPhase.TOUCH_HELD) phase = ChildVisualPhase.RELEASED; onHeld(false) } }
    LaunchedEffect(visible) {
        if (visible) {
            phase = ChildVisualPhase.EMERGING; onPhase(ChildLifecycle.EMERGING); shown = true
            delay(if (reduced) 0 else AppMotion.StaggerStep)
            phase = ChildVisualPhase.SETTLING; onPhase(ChildLifecycle.SETTLING)
            delay(if (reduced) 0 else AppMotion.DurationMedium.toLong())
            phase = ChildVisualPhase.VISIBLE; onPhase(ChildLifecycle.VISIBLE)
        } else {
            phase = ChildVisualPhase.EXITING; onPhase(ChildLifecycle.EXITING); shown = false
            delay(if (reduced) 0 else AppMotion.DurationMedium.toLong())
            phase = ChildVisualPhase.MERGED; onPhase(ChildLifecycle.MERGED); onMerged()
        }
    }
    val progress by animateFloatAsState(if (shown) 1f else 0f,
        if (reduced) snap() else AppMotion.islandSpring(), label = "child_emerge_merge")
    val shape = RoundedCornerShape(AppTheme.spacing.xxl)
    val copyAlpha by animateFloatAsState(if (shown) 1f else 0f,
        if (reduced) snap() else tween(AppMotion.DurationFast, AppMotion.StaggerStep.toInt()), label = "child_copy_stagger")
    Row(modifier.width(width.coerceAtLeast(48.dp))
        .clickable(enabled = visible, interactionSource = source, indication = null, role = Role.Button,
            onClickLabel = "Read notification in main island") { phase = ChildVisualPhase.PROMOTING; onPhase(ChildLifecycle.PROMOTING); onPromote() }
        .padding(top = AppTheme.spacing.sm, start = AppTheme.spacing.sm, end = AppTheme.spacing.sm)
        .graphicsLayer {
            alpha = progress; translationY = if (reduced) 0f else -size.height * (1 - progress)
            scaleX = if (reduced) 1f else .9f + .1f * progress
            scaleY = if (reduced) 1f else .35f + .65f * progress
            transformOrigin = TransformOrigin(.5f, 0f)
        }.background(Color.Black, shape).border(1.dp, Color.White.copy(alpha = .12f), shape)
        .heightIn(min = 48.dp).padding(AppTheme.spacing.md).testTag("child_${child.key}"),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        // Reliable fallback, including missing/invalid package artwork. Never performs external actions.
        PackageIcon(packageName = child.packageName, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(AppTheme.spacing.sm)), contentDescription = "${child.appName} icon")
        Column(Modifier.weight(1f).graphicsLayer { alpha = copyAlpha }) {
            Text(child.appName, style = AppTheme.typography.caption, color = Color.White.copy(alpha = .65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (redacted) "New notification" else child.title.ifBlank { child.appName },
                style = AppTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!redacted && child.text.isNotBlank()) Text(child.text, style = AppTheme.typography.bodySmall,
                color = Color.White.copy(alpha = .8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
