package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Charging/Battery island (main pill)
 */
@Composable
fun ChargingIslandMain(
    percentage: Int,
    isLowBattery: Boolean = false,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        percentage >= 20 -> Color(0xFF34C759)
        percentage >= 10 -> Color(0xFFFF9500)
        else -> Color(0xFFFF3B30)
    }
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = if (isLowBattery) "Low Battery" else "Charging",
            style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandSubtitle,
            color = Color.White
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.sm)
        ) {
            AppText(
                text = "$percentage%",
                style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandTitle,
                color = batteryColor
            )
            
            BatteryIcon(
                percentage = percentage,
                color = batteryColor,
                modifier = Modifier.size(width = 28.dp, height = 14.dp)
            )
        }
    }
}

/**
 * iOS-style Battery icon with rounded shell, nub, and inner fill
 */
@Composable
fun BatteryIcon(
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val bodyWidth = width * 0.85f
        val bodyHeight = height
        val cornerRadius = height * 0.35f
        
        val nubWidth = width * 0.08f
        val nubHeight = height * 0.35f
        val nubX = bodyWidth + 1.5.dp.toPx()
        val nubY = (height - nubHeight) / 2
        val nubCornerRadius = 1.5.dp.toPx()
        
        // 1. Battery shell background
        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
        
        // 2. Nub
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(nubX, nubY),
            size = androidx.compose.ui.geometry.Size(nubWidth, nubHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(nubCornerRadius, nubCornerRadius)
        )
        
        // 3. Inner fill
        val fillPadding = 1.5.dp.toPx()
        val maxFillWidth = bodyWidth - (fillPadding * 2)
        val fillWidth = maxFillWidth * (percentage / 100f)
        val fillHeight = bodyHeight - (fillPadding * 2)
        val fillCornerRadius = cornerRadius - fillPadding
        
        if (percentage > 0) {
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(fillPadding, fillPadding),
                size = androidx.compose.ui.geometry.Size(fillWidth.coerceAtLeast(fillHeight), fillHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(fillCornerRadius.coerceAtLeast(0f), fillCornerRadius.coerceAtLeast(0f))
            )
        }
    }
}

/**
 * Side island bubble with circular progress ring
 */
@Composable
fun ChargingIslandSide(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val ringColor = when {
        percentage >= 20 -> Color(0xFF34C759)
        percentage >= 10 -> Color(0xFFFF9500)
        else -> Color(0xFFFF3B30)
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.size(28.dp),
            color = ringColor,
            trackColor = ringColor.copy(alpha = 0.25f),
            strokeWidth = 2.5.dp
        )
    }
}
