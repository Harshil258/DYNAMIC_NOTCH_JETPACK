package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/** Only real battery state is displayed; no invented wattage, charger model or ETA. */
@Composable
internal fun PowerStatusExpanded(percentage: Int, low: Boolean) {
    val value = percentage.coerceIn(0, 100)
    val tint = if (low) IslandColors.CapsuleRed else IslandColors.Green
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl, vertical = AppTheme.spacing.lg)) {
    val stacked = LocalDensity.current.fontScale > 1.3f || maxWidth < 240.dp
    val identity: @Composable () -> Unit = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        BatteryIcon(value, tint, Modifier.size(AppTheme.spacing.xxxl, AppTheme.spacing.lg))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Text(if (low) "Low battery" else "Charging", color = Color.White,
                style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold))
            Text(if (low) "Connect to power" else "Connected to power", color = tint,
                style = AppTheme.typography.bodySmall)
        }
      }
    }
    val metric: @Composable () -> Unit = {
        Text("$value%", maxLines = 1, softWrap = false, color = tint,
            style = AppTheme.typography.islandTime.copy(fontSize = 28.sp, lineHeight = 34.sp,
                fontFeatureSettings = "tnum", textDirection = TextDirection.Ltr))
    }
    if (stacked) Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        identity()
        metric()
    } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Box(Modifier.weight(1f)) { identity() }
        metric()
    }
    }
}
