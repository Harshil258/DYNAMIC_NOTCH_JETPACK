package ai.emots.kishan_dynamic.ui.components.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.ui.components.CompactIslandLayout
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Pixel-perfect Football/Sports Island Expanded state (resolves Screenshot C defects).
 * NOTE: Strictly restricted to Football / Soccer. ZERO Cricket under all circumstances.
 */
@Composable
fun SportsIslandExpanded(
    info: LiveActivityInfo? = null,
    minuteOverride: Int? = null
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

        val teamBadgeSize = 32.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
        val stackedScore = LocalDensity.current.fontScale > 1.3f
        val competition = info?.entityName?.ifBlank { null }
            ?: info?.title?.takeIf { it != "Live match" && it.isNotBlank() }
            ?: "UEFA Champions League"
        val minute = minuteOverride ?: 78
        val statusLabel = info?.statusBadgeText?.ifBlank { null }
            ?: if (minute >= 90) "FULL TIME" else "$minute' LIVE"
        val isLive = minute < 90

        val homeName = "Real Madrid"
        val homeCode = "RMA"
        val homeScore = "2"
        val homeScorers = "Vini Jr 34', Jude 67'"

        val awayName = "Man City"
        val awayCode = "MCI"
        val awayScore = "1"
        val awayScorers = "De Bruyne 51'"

        val matchContext = info?.secondaryMetric?.ifBlank { null }
            ?: "Quarter-Final • 2nd Leg (Agg: 4 - 4)"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // TOP ROW: Competition headline & animated live indicator
            IslandAdaptiveHeader(identity = {
                Text(competition, style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }, status = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                    if (isLive) IslandLivePulseIndicator(color = IslandColors.Green)
                    Text(statusLabel, style = AppTheme.typography.bodySmall, color = if (isLive) IslandColors.Green else IslandColors.Gray)
                }
            })

            // MIDDLE SECTION: Scoreboard with team identity & prominent score
            if (stackedScore) Text(
                text = "$homeScore  -  $awayScore", fontSize = 23.sp,
                fontWeight = FontWeight.Bold, color = IslandColors.Yellow,
                fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                        Box(
                            modifier = Modifier
                                .size(teamBadgeSize)
                                .clip(CircleShape)
                                .background(Color(0xFF263238)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = homeCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = homeName,
                            textAlign = TextAlign.Center,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = homeScorers,
                        fontSize = 11.sp,
                        color = IslandColors.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Centered Monospace Score
                if (!stackedScore) Text(
                    text = "$homeScore  -  $awayScore",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Yellow,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 10.dp)
                )

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                        Box(
                            modifier = Modifier
                                .size(teamBadgeSize)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = awayCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = awayName,
                            textAlign = TextAlign.Center,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = awayScorers,
                        fontSize = 11.sp,
                        color = IslandColors.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // BOTTOM ROW: Competition Stage & Aggregate Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(IslandColors.Gray6)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = matchContext,
                    fontSize = 11.sp,
                    color = IslandColors.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Pixel-perfect Football/Sports Island Compact state.
 */
@Composable
fun SportsIslandCompact(
    info: LiveActivityInfo? = null,
    minuteOverride: Int? = null
) {
    val minute = minuteOverride ?: 78
    val largeText = LocalDensity.current.fontScale > 1.3f

    CompactIslandLayout(
        leading = {
            Text(
                text = "RMA 2",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "1 MCI",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Yellow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!largeText) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(IslandColors.Green)
                )
                Text(
                    text = "$minute'",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslandColors.Green
                )
                }
            }
        }
    )
}
