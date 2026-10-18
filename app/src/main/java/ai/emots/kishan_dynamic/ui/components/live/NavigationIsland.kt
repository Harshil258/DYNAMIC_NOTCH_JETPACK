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
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Pixel-perfect Turn-by-Turn Navigation Island Expanded state (resolves Screenshot D defects).
 */
@Composable
fun NavigationIslandExpanded(
    info: LiveActivityInfo? = null,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.72f, 1.15f)

        val maneuver = info?.maneuverType ?: "right"
        val distance = info?.primaryMetric?.ifBlank { null }
            ?: "200 ft"
        val roadName = info?.secondaryMetric?.ifBlank { null }
            ?: "I-280 North"
        val destination = info?.secondaryText?.ifBlank { null }
            ?: "Downtown · San Francisco"
        val statusText = info?.statusBadgeText?.ifBlank { null }
            ?: "IN 200 FT"

        val dominantGlyph = when (maneuver.lowercase()) {
            "left", "sharp_left" -> AppleGlyph.NavigationLeft
            "straight" -> AppleGlyph.NavigationStraight
            else -> AppleGlyph.NavigationRight
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp * scale, vertical = 2.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP ROW: Primary Maneuver Stage + Status Badge + Lane Recommendation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Dominant Maneuver Stage
                    Box(
                        modifier = Modifier
                            .size(38.dp * scale)
                            .clip(CircleShape)
                            .background(IslandColors.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = dominantGlyph,
                            tint = Color.Black,
                            size = 22.dp * scale
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp * scale))
                    Column {
                        Text(
                            text = statusText,
                            fontSize = (10f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = IslandColors.Green,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = distance,
                            color = Color.White,
                            fontSize = (21f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Compact lane guidance indicator (non-competing)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp * scale))
                        .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp * scale, vertical = 4.dp * scale)
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.NavigationLeft,
                        tint = Color(0xFF64748B),
                        size = 14.dp * scale
                    )
                    AppleIcon(
                        glyph = AppleGlyph.NavigationStraight,
                        tint = Color(0xFF64748B),
                        size = 14.dp * scale
                    )
                    AppleIcon(
                        glyph = AppleGlyph.NavigationRight,
                        tint = Color.White,
                        size = 14.dp * scale
                    )
                }
            }

            // BOTTOM SECTION: Highway Shield badge / Destination / ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Highway Shield badge
                Box(
                    modifier = Modifier
                        .size(width = 38.dp * scale, height = 30.dp * scale)
                        .clip(RoundedCornerShape(5.dp * scale)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(ai.emots.kishan_dynamic.R.drawable.navigation_shield),
                        contentDescription = "Highway Shield",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.width(10.dp * scale))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = roadName,
                        color = Color.White,
                        fontSize = (14.5f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = destination,
                        color = Color(0xFFA4A4A9),
                        fontSize = (11.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
