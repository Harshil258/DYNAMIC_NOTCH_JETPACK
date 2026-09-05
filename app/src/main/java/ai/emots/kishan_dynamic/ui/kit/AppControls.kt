package ai.emots.kishan_dynamic.ui.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient

// =============================================================================
// APP KIT CONTROLS — fully token driven, correct touch targets, light+dark safe.
// =============================================================================

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackWidth = 50.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    val thumbPadding = 3.dp
    val maxOffset = trackWidth - thumbSize - thumbPadding * 2

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else 0.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "switch_thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> AppTheme.colors.disabled
            checked -> AppTheme.colors.success
            else -> AppTheme.colors.controlTrack
        },
        label = "switch_track"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .border(0.5.dp, AppTheme.colors.border, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(if (enabled) AppTheme.colors.controlThumb else AppTheme.colors.textTertiary)
        )
    }
}

enum class AppButtonStyle { Primary, Secondary, Tonal, Destructive }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    glyph: AppleGlyph? = null,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "button_scale"
    )

    val container = when {
        !enabled -> AppTheme.colors.disabled
        style == AppButtonStyle.Primary -> AppTheme.colors.accent
        style == AppButtonStyle.Destructive -> AppTheme.colors.error
        style == AppButtonStyle.Tonal -> AppTheme.colors.accent.copy(alpha = 0.16f)
        else -> AppTheme.colors.surfaceElevated
    }
    val content = when {
        !enabled -> AppTheme.colors.textTertiary
        style == AppButtonStyle.Primary || style == AppButtonStyle.Destructive -> AppTheme.colors.onAccent
        style == AppButtonStyle.Tonal -> AppTheme.colors.accent
        else -> AppTheme.colors.textPrimary
    }
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    val containerBrush = if (enabled && style == AppButtonStyle.Primary) {
        AppTheme.colors.accentGradient()
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(container, container))
    }

    Row(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(scale)
            .clip(shape)
            .background(containerBrush, shape)
            .then(
                if (style == AppButtonStyle.Secondary) {
                    Modifier.border(0.5.dp, AppTheme.colors.border, shape)
                } else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .heightIn(min = 50.dp)
            .padding(horizontal = AppTheme.spacing.xl, vertical = AppTheme.spacing.md),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (glyph != null) {
            AppleIcon(glyph = glyph, tint = content, size = 16.dp)
            Spacer(modifier = Modifier.size(AppTheme.spacing.sm))
        }
        AppText(
            text = text,
            style = AppTheme.typography.button,
            color = content,
            maxLines = 1
        )
    }
}

/** iOS-style segmented control with a sliding indicator. */
@Composable
fun AppSegmented(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.radius.md)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(AppTheme.colors.surfaceVariant)
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(3.dp)
    ) {
        val count = options.size.coerceAtLeast(1)
        val segmentWidth = maxWidth / count
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.coerceIn(0, count - 1),
            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
            label = "segment_indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(AppTheme.radius.sm + 2.dp))
                .background(AppTheme.colors.surfaceElevated)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOptionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = option,
                        style = AppTheme.typography.button,
                        color = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Labelled slider with a live readout, draggable and tappable. */
@Composable
fun AppSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueFormatter: (Float) -> String = { it.toInt().toString() },
    enabled: Boolean = true,
    trailingLabel: String? = null,
    onLockedInteraction: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val span = (valueRange.endInclusive - valueRange.start).takeIf { it != 0f } ?: 1f
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

    fun emitFromX(x: Float) {
        if (!enabled) {
            onLockedInteraction?.invoke()
            return
        }
        if (trackWidthPx <= 0f) return
        val f = (x / trackWidthPx).coerceIn(0f, 1f)
        onValueChange(valueRange.start + f * span)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = title,
                style = AppTheme.typography.body,
                color = AppTheme.colors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingLabel != null) {
                    AppStatusPill(
                        text = trailingLabel,
                        color = AppTheme.colors.gold,
                        showDot = false,
                        modifier = Modifier.padding(end = AppTheme.spacing.sm)
                    )
                }
                AppText(
                    text = valueFormatter(value),
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) AppTheme.colors.accent else AppTheme.colors.textTertiary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(valueRange) {
                    detectTapGestures { offset -> emitFromX(offset.x) }
                }
                .pointerInput(valueRange) {
                    detectHorizontalDragGestures { change, _ ->
                        emitFromX(change.position.x)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) AppTheme.colors.controlTrack else AppTheme.colors.disabled
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) AppTheme.colors.accent else AppTheme.colors.textTertiary
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { trackWidthPx = it.width.toFloat() }
            )
            val trackWidthDp = with(density) { trackWidthPx.toDp() }
            val thumbOffset = (trackWidthDp * fraction - 11.dp)
                .coerceIn(0.dp, (trackWidthDp - 22.dp).coerceAtLeast(0.dp))
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) AppTheme.colors.controlThumb else AppTheme.colors.disabled
                    )
                    .border(0.5.dp, AppTheme.colors.border, CircleShape)
            )
        }
    }
}

/** Selectable square tile used for quick toggles and choices. */
@Composable
fun AppTile(
    label: String,
    glyph: AppleGlyph,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "tile_scale"
    )
    val shape = RoundedCornerShape(AppTheme.radius.lg)
    val tint = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary

    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                if (selected) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            AppTheme.colors.accent.copy(alpha = 0.18f),
                            AppTheme.colors.info.copy(alpha = 0.10f)
                        )
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surfaceVariant)
                    )
                },
                shape
            )
            .border(
                0.5.dp,
                if (selected) AppTheme.colors.accent.copy(alpha = 0.45f) else AppTheme.colors.border,
                shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = AppTheme.spacing.lg, horizontal = AppTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        AppleIcon(glyph = glyph, tint = tint, size = 22.dp)
        AppText(
            text = label,
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = if (selected) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
            maxLines = 1
        )
    }
}

/** Choice chip row that scrolls horizontally when needed. */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.radius.pill)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(AppTheme.colors.textPrimary, AppTheme.colors.textPrimary)
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surfaceVariant)
                    )
                },
                shape
            )
            .border(
                0.5.dp,
                if (selected) Color.Transparent else AppTheme.colors.border,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .heightIn(min = 36.dp)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = label,
            style = AppTheme.typography.button,
            color = if (selected) AppTheme.colors.background else AppTheme.colors.textSecondary,
            maxLines = 1
        )
    }
}

/** Slim linear progress used for setup and charging indicators. */
@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
    height: Dp = 6.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color ?: AppTheme.colors.accent)
        )
    }
}
