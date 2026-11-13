package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.components.live.IslandAdaptiveHeader
import androidx.compose.ui.platform.LocalDensity

/**
 * Professional V6 Reference & Derived Activity Compositions.
 *
 * Adheres to the AURORA V6 Design Constitution:
 * - Specific, authentic recipes for derived states (MovieCard, SubscriptionPricing, TransportActivity)
 * - Strict 367dp family geometry targets with content-adaptive responsiveness
 * - Preserves ColorOptions as an intentional debug palette fixture
 */

/**
 * Transport Activity (NO_SOURCE, S5 Activity expanded, 367×148dp, R42).
 * Route/vehicle glyph START, origin→destination + milestone, ETA at END, progress track.
 */
@Composable
internal fun TransportReferenceContent(
    info: ai.emots.kishan_dynamic.data.model.LiveActivityInfo? = null,
    onControlAction: (IslandControlAction) -> Unit = {}
) {
    val isDemoPreview = info == null || info.id == "gallery_transport"
    val routeTitle = info?.title?.ifBlank { null } ?: if (isDemoPreview) "Route 24 · Central" else "Transit Route"
    val milestone = info?.secondaryText?.ifBlank { null } ?: if (isDemoPreview) "Arriving at Platform 2" else ""
    val eta = info?.primaryMetric?.ifBlank { null } ?: if (isDemoPreview) "3 min" else ""
    val origin = info?.secondaryMetric?.ifBlank { null } ?: if (isDemoPreview) "Market Street" else ""
    val destination = info?.entityName?.ifBlank { null } ?: if (isDemoPreview) "Central Station" else ""
    val progress = info?.progress ?: if (isDemoPreview) 0.65f else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: Vehicle Icon + Route Title + ETA at END
        IslandAdaptiveHeader(
            identity = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppleIcon(AppleGlyph.TransitTrain, tint = IslandColors.CapsuleCyan, size = 20.dp)
                    }
                    Column {
                        ReferenceTitle(routeTitle)
                        if (milestone.isNotBlank()) {
                            ReferenceSubtitle(milestone)
                        }
                    }
                }
            },
            status = {
                if (eta.isNotBlank()) {
                    ReferenceMetric(eta)
                }
            }
        )

        // Route corridor: Origin -> Destination (only when present)
        if (origin.isNotBlank() || destination.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = origin.ifBlank { "En route" },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (destination.isNotBlank()) {
                    AppleIcon(AppleGlyph.ChevronRight, tint = IslandColors.Gray, size = 12.dp)
                    Text(
                        text = destination,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Progress bar (only when present)
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334155))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(IslandColors.CapsuleCyan, Color(0xFF38BDF8))
                            )
                        )
                )
            }
        }
    }
}

/**
 * Movie Card (NO_SOURCE, S5 Activity expanded, 367×142dp, R42).
 * Poster 52×76dp START, movie/session, venue/date, seat badge, ticket action.
 */
@Composable
internal fun MovieReferenceContent(
    onOpenTicket: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Movie poster thumbnail
        Box(
            modifier = Modifier
                .size(52.dp, 76.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(AppleGlyph.FaceTimeVideo, tint = IslandColors.CapsuleCyan, size = 24.dp)
        }

        // Details column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ReferenceTitle("Evening screening")
            Text(
                text = "Cinema 2 · 19:30",
                color = IslandColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Seat Badge & Session Pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF222B3A))
                        .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Seat G12",
                        color = IslandColors.CapsuleCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Standard 2D",
                    color = IslandColors.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Ticket CTA
        IslandActionPill(
            label = "View Ticket",
            onClick = onOpenTicket,
            glyph = AppleGlyph.FaceId,
            style = IslandActionStyle.Secondary,
            accent = IslandSemanticAccent.Cyan,
            visualHeight = 36.dp,
            fontSize = 12.sp,
            contentDescription = "View movie ticket"
        )
    }
}

/**
 * Subscription Pricing (NO_SOURCE, S5 Activity expanded, 367×162dp, R42).
 * Aurora branding, real plan name, local price if loaded, 1 primary CTA, secondary dismiss.
 */
@Composable
internal fun SubscriptionReferenceContent(
    planName: String = "Aurora Pro",
    priceText: String = "Google Play",
    onUpgrade: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: Crown Icon + Plan Title + Free notice
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D2310)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(AppleGlyph.Crown, tint = IslandColors.Orange, size = 22.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReferenceTitle(planName)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(IslandColors.Orange.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("PREMIUM", color = IslandColors.Orange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                ReferenceSubtitle("Sub-pixel notch alignment & custom themes")
            }
        }

        // Trust statement
        Text(
            text = "Core notifications, calls, and recovery always remain free.",
            color = IslandColors.TextSecondary,
            fontSize = 11.5.sp,
            lineHeight = 15.sp
        )

        // Action Row: Primary Upgrade + Secondary Dismiss
        IslandControlRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IslandActionPill(
                label = "Upgrade · $priceText",
                onClick = onUpgrade,
                modifier = Modifier.weight(1.3f),
                glyph = AppleGlyph.Crown,
                style = IslandActionStyle.Primary,
                accent = IslandSemanticAccent.Blue,
                visualHeight = 38.dp,
                fontSize = 12.5.sp,
                contentDescription = "Upgrade to Aurora Pro for $priceText"
            )

            IslandActionPill(
                label = "Maybe later",
                onClick = onDismiss,
                modifier = Modifier.weight(0.9f),
                style = IslandActionStyle.Tertiary,
                visualHeight = 38.dp,
                fontSize = 12.sp,
                contentDescription = "Dismiss upgrade offer"
            )
        }
    }
}

/**
 * Color Options (DIRECT, S5 Activity expanded, 367×185.3dp, R42).
 * Intentional swatches palette fixture for debug / theme preview.
 */
@Composable
internal fun ColorOptionsReferenceContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 185.3.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            ReferenceTitle("Island colors")
            ReferenceSubtitle("Palette demonstration · preferences unchanged")
        }

        val palettes = listOf(
            IslandColors.CapsuleCyan to "Cyan",
            IslandColors.Green to "Emerald",
            IslandColors.Orange to "Amber",
            IslandColors.Purple to "Purple",
            Color.White to "White",
            IslandColors.Gray to "Slate"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            palettes.forEach { (color, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(AppTheme.spacing.sm))
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(AppTheme.spacing.sm))
                    )
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = IslandColors.TextSecondary
                    )
                }
            }
        }

        Text(
            text = "Tap a theme in Settings to apply custom RGB styling.",
            color = IslandColors.Gray,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
internal fun GroupCallReferenceContent(participants: List<ai.emots.kishan_dynamic.data.model.ContactInfo>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            participants.take(2).forEach { person ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(IslandColors.Gray4, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        person.name.split(' ').mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        style = AppTheme.typography.caption,
                        color = Color.White
                    )
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            ReferenceTitle("Group call active")
            ReferenceSubtitle("${participants.size} participants connected")
        }
    }
}

@Composable
internal fun NotificationImageReferenceContent(
    notifications: List<ai.emots.kishan_dynamic.data.model.NotificationInfo>,
    activeIndex: Int,
    onHeightChange: ((androidx.compose.ui.unit.Dp) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sources = notifications.filter { it.picturePath != null }.ifEmpty {
        listOf(
            ai.emots.kishan_dynamic.data.model.NotificationInfo(
                "reference-image", "", "Messages", "Project review",
                "The latest photo is ready for review.",
                picturePath = "android.resource://${context.packageName}/${ai.emots.kishan_dynamic.R.drawable.shared_media_art}"
            )
        )
    }
    NotificationIslandExpandedPager(
        sources,
        activeIndex = activeIndex.coerceIn(0, sources.lastIndex),
        onHeightChange = onHeightChange
    )
}

@Composable
internal fun TimerImageReferenceContent(timer: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            AppleIcon(AppleGlyph.Timer, tint = IslandColors.Orange, size = 36.dp)
            Column {
                ReferenceTitle("Focus session")
                ReferenceSubtitle("Timer active")
            }
        }
        ReferenceMetric(timer)
    }
}

@Composable
internal fun FlightTrackerReferenceContent(info: ai.emots.kishan_dynamic.data.model.LiveActivityInfo?, progress: Float?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 144.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ai.emots.kishan_dynamic.ui.components.live.FlightIslandExpanded(info, progress)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ReferenceSubtitle("Route progress")
            ReferenceMetric("${(((progress ?: info?.progress ?: .65f).coerceIn(0f, 1f)) * 100).toInt()}%")
        }
    }
}

@Composable private fun ReferenceTitle(value: String) {
    Text(value, style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
}
@Composable private fun ReferenceSubtitle(value: String) {
    Text(value, style = AppTheme.typography.bodySmall, color = IslandColors.Gray)
}
@Composable private fun ReferenceMetric(value: String) {
    Text(value, style = AppTheme.typography.islandTime.copy(fontFeatureSettings = "tnum"), color = IslandColors.CapsuleCyan)
}
