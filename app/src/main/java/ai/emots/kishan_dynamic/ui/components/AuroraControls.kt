package ai.emots.kishan_dynamic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraMotion
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * iOS-style Frosted Glass Toggle Switch.
 */
@Composable
fun AuroraToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 26.dp
    val thumbPadding = 3.dp

    val targetThumbOffset = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding
    val animatedThumbOffset by animateDpAsState(
        targetValue = targetThumbOffset,
        animationSpec = AuroraMotion.ExpandSpringDp,
        label = "toggle_thumb_offset"
    )

    val animatedTrackColor by animateColorAsState(
        targetValue = if (checked) AuroraTheme.colors.primary else AuroraTheme.colors.controlTrack,
        label = "toggle_track_color"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(animatedTrackColor)
            .border(
                width = AuroraTheme.elevation.hairlineBorder,
                brush = AuroraTheme.colors.glassBorderBrush,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedThumbOffset)
                .size(thumbSize)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Precision Slider with glowing violet active fill.
 */
@Composable
fun AuroraSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = AuroraTheme.colors.primary,
            inactiveTrackColor = AuroraTheme.colors.controlTrack
        )
    )
}

/**
 * Filter / Simulation Chip with selected spring pop and active border.
 */
@Composable
fun AuroraChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1.0f,
        animationSpec = AuroraMotion.PressSpringFloat,
        label = "chip_scale"
    )

    val backgroundBrush = if (selected) {
        Brush.horizontalGradient(
            listOf(
                AuroraTheme.colors.primary.copy(alpha = 0.35f),
                AuroraTheme.colors.primarySoft.copy(alpha = 0.20f)
            )
        )
    } else {
        AuroraTheme.colors.glassSurfaceBrush
    }

    val borderBrush = if (selected) {
        Brush.horizontalGradient(
            listOf(
                AuroraTheme.colors.primary,
                AuroraTheme.colors.auraPink
            )
        )
    } else {
        AuroraTheme.colors.glassBorderBrush
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .clip(AuroraTheme.shapes.chip)
            .background(backgroundBrush)
            .border(
                width = if (selected) AuroraTheme.elevation.activeBorder else AuroraTheme.elevation.hairlineBorder,
                brush = borderBrush,
                shape = AuroraTheme.shapes.chip
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AuroraTheme.spacing.md, vertical = AuroraTheme.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (selected) AuroraTheme.colors.primary else AuroraTheme.colors.textSecondary,
                    modifier = Modifier.size(AuroraTheme.spacing.iconSmall)
                )
            }
            Text(
                text = label,
                style = AuroraTheme.typography.labelMedium,
                color = if (selected) AuroraTheme.colors.textPrimary else AuroraTheme.colors.textSecondary
            )
        }
    }
}

/**
 * Status Pill Badge (e.g. "PRO", "LIVE", "+3").
 */
@Composable
fun AuroraBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AuroraTheme.colors.primary,
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(AuroraTheme.shapes.pill)
            .background(backgroundColor)
            .padding(horizontal = AuroraTheme.spacing.sm, vertical = AuroraTheme.spacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AuroraTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Frosted Glass Segmented Control / Tab Switcher.
 */
@Composable
fun AuroraSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = AuroraTheme.shapes.pill,
        contentPadding = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val itemScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1.0f,
                    animationSpec = AuroraMotion.PressSpringFloat,
                    label = "tab_item_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(itemScale)
                        .height(38.dp)
                        .clip(AuroraTheme.shapes.pill)
                        .background(
                            if (isSelected) AuroraTheme.colors.primary
                            else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = AuroraTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else AuroraTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Frosted Glass Input Field.
 */
@Composable
fun AuroraInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderBrush = if (isFocused) {
        Brush.horizontalGradient(listOf(AuroraTheme.colors.primary, AuroraTheme.colors.auraPink))
    } else {
        AuroraTheme.colors.glassBorderBrush
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(AuroraTheme.shapes.cardSmall)
            .background(AuroraTheme.colors.glassSurfaceBrush)
            .border(
                width = if (isFocused) AuroraTheme.elevation.activeBorder else AuroraTheme.elevation.hairlineBorder,
                brush = borderBrush,
                shape = AuroraTheme.shapes.cardSmall
            )
            .padding(horizontal = AuroraTheme.spacing.lg),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused) AuroraTheme.colors.primary else AuroraTheme.colors.textTertiary,
                    modifier = Modifier.size(AuroraTheme.spacing.iconMedium)
                )
                Spacer(modifier = Modifier.width(AuroraTheme.spacing.md))
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AuroraTheme.typography.bodyMedium,
                        color = AuroraTheme.colors.textTertiary
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = AuroraTheme.typography.bodyMedium.copy(color = AuroraTheme.colors.textPrimary),
                    cursorBrush = SolidColor(AuroraTheme.colors.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Controls Dark Preview", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ControlsDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        var toggleState by remember { mutableStateOf(true) }
        var sliderState by remember { mutableFloatStateOf(0.6f) }
        var selectedChip by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dynamic Notch Overlay",
                    style = AuroraTheme.typography.titleMedium,
                    color = AuroraTheme.colors.textPrimary
                )
                AuroraToggle(checked = toggleState, onCheckedChange = { toggleState = it })
            }

            AuroraSlider(value = sliderState, onValueChange = { sliderState = it })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuroraChip(label = "Music", selected = selectedChip == 0, onClick = { selectedChip = 0 })
                AuroraChip(label = "Calls", selected = selectedChip == 1, onClick = { selectedChip = 1 })
                AuroraChip(label = "Notifs", selected = selectedChip == 2, onClick = { selectedChip = 2 })
                AuroraBadge(text = "PRO ACTIVE")
            }

            AuroraSegmentedControl(
                items = listOf("Alignment", "Audio", "Behaviors"),
                selectedIndex = 0,
                onItemSelected = {}
            )
        }
    }
}
