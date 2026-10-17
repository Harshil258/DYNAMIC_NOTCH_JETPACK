package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun IslandColorPicker(
    title: String,
    argb: Int,
    onSave: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    var draft by remember(argb) { mutableIntStateOf(argb) }

    val isDark = AppTheme.isDark
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else AppTheme.colors.textSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.md))
            .clickable {
                draft = argb
                open = true
            }
            .padding(vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            AppText(
                text = title,
                style = AppTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = textPrimary
            )
            AppText(
                text = String.format("#%06X", argb and 0xFFFFFF),
                style = AppTheme.typography.caption,
                color = textSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(argb))
                .border(
                    1.5.dp,
                    if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f),
                    CircleShape
                )
        )
    }

    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            Column(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppTheme.radius.xl))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.xl))
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppText(
                    text = title,
                    style = AppTheme.typography.h3,
                    color = textPrimary
                )

                // Large Preview Swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(AppTheme.radius.lg))
                        .background(Color(draft))
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.lg)),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = String.format("#%06X", draft and 0xFFFFFF),
                        style = AppTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = if (((draft ushr 16 and 255) * 0.299 + (draft ushr 8 and 255) * 0.587 + (draft and 255) * 0.114) > 150) Color.Black else Color.White
                    )
                }

                // RGB Channel Sliders
                listOf(
                    "Red" to 16,
                    "Green" to 8,
                    "Blue" to 0
                ).forEach { (label, shift) ->
                    val channel = (draft ushr shift) and 255
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AppText(text = label, style = AppTheme.typography.bodySmall, color = textPrimary)
                            AppText(text = "$channel", style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AppTheme.colors.primary)
                        }
                        AppSlider(
                            title = label,
                            value = channel.toFloat(),
                            valueRange = 0f..255f,
                            onValueChange = {
                                val mask = 255 shl shift
                                draft = (draft and mask.inv()) or (it.roundToInt().coerceIn(0, 255) shl shift) or 0xFF000000.toInt()
                            }
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppButton(
                        text = "Cancel",
                        style = AppButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { open = false }
                    )
                    AppButton(
                        text = "Save",
                        style = AppButtonStyle.Primary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSave(draft)
                            open = false
                        }
                    )
                }
            }
        }
    }
}
