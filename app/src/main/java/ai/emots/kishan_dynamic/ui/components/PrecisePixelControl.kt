package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Precision pixel control with slider and fine-tune increment / decrement touch steppers.
 * Enforces 48dp minimum touch targets and clear value readouts.
 */
@Composable
fun PrecisePixelControl(
    title: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    unit: String = "px",
    enabled: Boolean = true,
    trailingLabel: String? = null,
    onLockedInteraction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableIntStateOf(value) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!dragging) draft = value
    }

    val isDark = AppTheme.isDark
    val btnBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else AppTheme.colors.textSecondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppText(
                    text = title,
                    style = AppTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                    color = textPrimary
                )
                if (trailingLabel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppTheme.colors.gold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        AppText(
                            text = trailingLabel,
                            style = AppTheme.typography.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            color = Color.Black
                        )
                    }
                }
            }

            AppText(
                text = "$draft $unit",
                style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) AppTheme.colors.primary else textSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement button (-1)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(btnBg)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Decrease $title by one $unit"
                    }
                    .clickable(enabled = enabled && draft > range.first) {
                        if (!enabled) {
                            onLockedInteraction?.invoke()
                            return@clickable
                        }
                        val next = (draft - 1).coerceIn(range)
                        draft = next
                        onChange(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "−",
                    style = AppTheme.typography.h3,
                    color = if (enabled && draft > range.first) textPrimary else textSecondary.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Slider
            AppSlider(
                title = title,
                value = draft.toFloat(),
                valueRange = range.first.toFloat()..range.last.toFloat(),
                enabled = enabled,
                trailingLabel = null,
                onLockedInteraction = onLockedInteraction,
                onValueChange = { newValue ->
                    dragging = true
                    draft = newValue.roundToInt().coerceIn(range)
                },
                modifier = Modifier.weight(1f)
            )

            LaunchedEffect(dragging) {
                if (!dragging) {
                    onChange(draft)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Increment button (+1)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(btnBg)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Increase $title by one $unit"
                    }
                    .clickable(enabled = enabled && draft < range.last) {
                        if (!enabled) {
                            onLockedInteraction?.invoke()
                            return@clickable
                        }
                        val next = (draft + 1).coerceIn(range)
                        draft = next
                        onChange(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "+",
                    style = AppTheme.typography.h3,
                    color = if (enabled && draft < range.last) textPrimary else textSecondary.copy(alpha = 0.4f)
                )
            }
        }
    }
}
