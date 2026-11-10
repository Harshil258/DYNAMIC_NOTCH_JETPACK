package ai.emots.kishan_dynamic.ui.components.live

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.CompactIslandLayout
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/** Source-backed navigation; fields wrap at the destination width without canvas scaling. */
@Composable
fun NavigationIslandExpanded(
    info: LiveActivityInfo? = null,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    val distance = info?.primaryMetric?.ifBlank { null }
        ?: if (info == null) "200 ft" else info.title.ifBlank { "Navigation" }
    val roadName = info?.secondaryMetric?.ifBlank { null } ?: if (info == null) "I-280 North" else ""
    val destination = info?.secondaryText?.ifBlank { null } ?: if (info == null) "Downtown · San Francisco" else ""
    val status = info?.statusBadgeText?.ifBlank { null } ?: if (info == null) "IN 200 FT" else ""
    val maneuver = when (info?.maneuverType?.lowercase()) {
        "left", "sharp_left" -> AppleGlyph.NavigationLeft
        "straight" -> AppleGlyph.NavigationStraight
        else -> AppleGlyph.NavigationRight
    }
    Column(Modifier.fillMaxWidth().padding(AppTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        IslandAdaptiveHeader(identity = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(IslandColors.Green), contentAlignment = Alignment.Center) {
                    AppleIcon(maneuver, tint = Color.Black, size = 22.dp)
                }
                Text(distance, color = Color.White, style = AppTheme.typography.islandTime.copy(
                    fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            }
        }, status = {
            if (status.isNotBlank()) Text(status, color = IslandColors.Green,
                style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
        })
        if (roadName.isNotBlank() || destination.isNotBlank()) Row(
            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            if (roadName.contains("280")) Image(
                painterResource(ai.emots.kishan_dynamic.R.drawable.navigation_shield), contentDescription = "Interstate 280",
                modifier = Modifier.size(51.dp,39.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                if (roadName.isNotBlank()) Text(roadName, color = Color.White,
                    style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold))
                if (destination.isNotBlank()) Text(destination, color = IslandColors.Gray, style = AppTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Pixel-perfect Turn-by-Turn Navigation Island Compact state.
 */
@Composable
fun NavigationIslandCompact(info: LiveActivityInfo? = null) {
    val distance = info?.primaryMetric?.ifBlank { null }
        ?: "200m"

    val dominantGlyph = when (info?.maneuverType?.lowercase()) {
        "left", "sharp_left" -> AppleGlyph.NavigationLeft
        "straight" -> AppleGlyph.NavigationStraight
        else -> AppleGlyph.NavigationRight
    }

    CompactIslandLayout(
        leading = {
            AppleIcon(
                glyph = dominantGlyph,
                tint = IslandColors.Green,
                size = 15.dp
            )
        },
        trailing = {
            Text(
                text = distance,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.Green,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
