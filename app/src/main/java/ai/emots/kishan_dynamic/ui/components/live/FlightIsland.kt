package ai.emots.kishan_dynamic.ui.components.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.CompactIslandLayout
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Pixel-perfect Flight Island Expanded state (resolves Screenshot B defects).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlightIslandExpanded(
    info: LiveActivityInfo? = null,
    progressOverride: Float? = null
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

        val flightTitle = info?.entityName?.ifBlank { null }
            ?: info?.title?.takeIf { it != "Flight in progress" && it.isNotBlank() }
            ?: "Delta Air Lines • DL 492"
        val routeDesc = info?.secondaryText?.ifBlank { null }
            ?: "San Francisco to New York"
        val progress = (progressOverride ?: info?.progress ?: 0.65f).coerceIn(0f, 1f)
        val statusText = info?.statusBadgeText?.ifBlank { null }
            ?: when {
                progress >= 1f -> "LANDED"
                progress >= 0.05f -> "IN FLIGHT"
                else -> "ON TIME"
            }
        val originCode = "SFO"
        val destCode = "JFK"
        val gateInfo = info?.primaryMetric?.ifBlank { null }
            ?: "Gate B14 • Terminal 2"
        val timeRemaining = info?.secondaryMetric?.ifBlank { null }
            ?: when {
                progress >= 1f -> "Landed at JFK"
                progress >= 0.75f -> "35m left"
                progress >= 0.5f -> "1h 20m left"
                else -> "2h 15m left"
            }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // TOP ROW: Airline, flight number, route & status badge
            IslandAdaptiveHeader(identity = {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(IslandColors.CapsuleCyan.copy(alpha = .22f)), contentAlignment = Alignment.Center) {
                        AppleIcon(AppleGlyph.Airplane, tint = IslandColors.CapsuleCyan, size = 19.dp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(flightTitle, color = Color.White, style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold))
                        Text(routeDesc, color = IslandColors.Gray, style = AppTheme.typography.bodySmall)
                    }
                }
            }, status = {
                IslandStatusBadge(text = statusText, tint = if (progress >= 1f) IslandColors.Green else IslandColors.CapsuleCyan, fontSize = 11.sp)
            })

            // MIDDLE SECTION: Airport codes with route track and moving airplane marker
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = originCode,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Aerodynamic Route Track with airplane glyph positioned along the line
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                        .height(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Background track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(IslandColors.Gray4)
                    )
                    // Active progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(IslandColors.CapsuleCyan)
                    )
                    // Aircraft marker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.08f, 0.96f)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(IslandColors.CapsuleCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Airplane,
                                tint = Color.Black,
                                size = 10.dp
                            )
                        }
                    }
                }

                Text(
                    text = destCode,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            }

            // Wrap the independent gate and ETA regions at large text sizes.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                Text(
                    text = gateInfo,
                    fontSize = 11.sp,
                    color = IslandColors.Gray,
                )
                Text(
                    text = timeRemaining,
                    fontSize = 11.5.sp,
                    color = IslandColors.CapsuleCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Pixel-perfect Flight Island Compact state.
 */
@Composable
fun FlightIslandCompact(info: LiveActivityInfo? = null) {
    val subtitle = info?.primaryMetric?.ifBlank { null }
        ?: info?.subtitle?.takeIf { it.isNotBlank() && it != "Flight status" }
        ?: "2h 15m"

    CompactIslandLayout(
        leading = {
            AppleIcon(
                glyph = AppleGlyph.Airplane,
                tint = IslandColors.CapsuleCyan,
                size = 15.dp
            )
        },
        trailing = {
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.CapsuleCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
