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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * Pixel-perfect Delivery Island Expanded state (resolves Screenshot A defects).
 */
@Composable
fun DeliveryIslandExpanded(
    info: LiveActivityInfo? = null,
    progressOverride: Float? = null,
    onCallAction: () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = (maxWidth / 367.dp).coerceIn(0.72f, 1.15f)

        val restaurant = info?.entityName?.ifBlank { null }
            ?: info?.title?.takeIf { it != "Delivery" && it.isNotBlank() }
            ?: "Chipotle Mexican Grill"
        val orderSubtitle = info?.secondaryText?.ifBlank { null }
            ?: "Order #8492 • 2 items"
        val statusText = info?.statusBadgeText?.ifBlank { null }
            ?: when {
                (progressOverride ?: info?.progress ?: 0.65f) >= 1f -> "DELIVERED"
                (progressOverride ?: info?.progress ?: 0.65f) >= 0.85f -> "ARRIVING"
                else -> "ON THE WAY"
            }
        val progress = (progressOverride ?: info?.progress ?: 0.65f).coerceIn(0f, 1f)
        val etaText = info?.primaryMetric?.ifBlank { null }
            ?: when {
                progress >= 1f -> "Delivered just now"
                progress >= 0.85f -> "Arriving now"
                else -> "Arriving in 12 mins"
            }
        val driverInfo = info?.secondaryMetric?.ifBlank { null }
            ?: "Marco • Vespa Sprint • 4.9"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp * scale, vertical = 2.dp * scale),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP ROW: Restaurant identity, order details & status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp * scale)
                            .clip(RoundedCornerShape(10.dp * scale))
                            .background(IslandColors.Orange.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(
                            glyph = AppleGlyph.Car,
                            tint = IslandColors.Orange,
                            size = 19.dp * scale
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp * scale))
                    Column {
                        Text(
                            text = restaurant,
                            color = Color.White,
                            fontSize = (14.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = orderSubtitle,
                            color = IslandColors.Gray,
                            fontSize = (11.5f * scale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp * scale))

                IslandStatusBadge(
                    text = statusText,
                    tint = if (progress >= 1f) IslandColors.Green else IslandColors.Orange,
                    fontSize = (9f * scale).sp
                )
            }

            // MIDDLE SECTION: Progress milestone bar with live progress
            Column(modifier = Modifier.fillMaxWidth()) {
                IslandLiveProgressBar(
                    progress = progress,
                    tint = if (progress >= 1f) IslandColors.Green else IslandColors.Orange,
                    trackHeight = 4.dp * scale
                )
                Spacer(modifier = Modifier.height(5.dp * scale))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (progress >= 1f) "Completed" else "Picked up",
                        fontSize = (10.5f * scale).sp,
                        color = IslandColors.Gray
                    )
                    Text(
                        text = etaText,
                        fontSize = (10.5f * scale).sp,
                        color = if (progress >= 1f) IslandColors.Green else IslandColors.Orange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // BOTTOM ROW: Driver identification & functional Call button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = driverInfo,
                        fontSize = (11.5f * scale).sp,
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(3.dp * scale))
                    AppleIcon(
                        glyph = AppleGlyph.Star,
                        tint = IslandColors.Yellow,
                        size = 11.dp * scale
                    )
                }

                IslandActionChip(
                    glyph = AppleGlyph.Phone,
                    backgroundColor = IslandColors.Green,
                    iconTint = Color.White,
                    onClick = onCallAction,
                    size = 28.dp * scale,
                    iconSize = 13.dp * scale,
                    contentDescription = "Call delivery driver"
                )
            }
        }
    }
}

/**
 * Pixel-perfect Delivery Island Compact state.
 */
@Composable
fun DeliveryIslandCompact(info: LiveActivityInfo? = null) {
    val eta = info?.primaryMetric?.ifBlank { null }
        ?: info?.subtitle?.takeIf { it.isNotBlank() && it != "Delivery" }
        ?: "12m"

    CompactIslandLayout(
        leading = {
            AppleIcon(
                glyph = AppleGlyph.Car,
                tint = IslandColors.Orange,
                size = 15.dp
            )
        },
        trailing = {
            Text(
                text = eta,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslandColors.Orange,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
