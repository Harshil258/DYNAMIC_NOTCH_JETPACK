package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/** Reference-only compositions. Production source-backed rendering takes precedence in the host. */
@Composable
internal fun TransportReferenceContent() {
    Column(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppleIcon(AppleGlyph.TransitTrain, tint = IslandColors.CapsuleCyan, size = 32.dp)
            Spacer(Modifier.width(AppTheme.spacing.md))
            Column(Modifier.weight(1f)) { ReferenceTitle("Route 24 · Central"); ReferenceSubtitle("Arriving at platform 2") }
            ReferenceMetric("3 min")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ReferenceSubtitle("Market Street"); ReferenceSubtitle("Central Station")
        }
        Box(Modifier.fillMaxWidth().height(4.dp).background(IslandColors.Gray4, RoundedCornerShape(2.dp)))
    }
}

@Composable
internal fun MovieReferenceContent() {
    Row(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
        Box(Modifier.size(64.dp, 96.dp).background(IslandColors.Gray4, RoundedCornerShape(AppTheme.spacing.sm)), contentAlignment = Alignment.Center) {
            AppleIcon(AppleGlyph.FaceTimeVideo, tint = Color.White, size = 32.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            ReferenceTitle("Evening screening")
            ReferenceSubtitle("Cinema 2 · 19:30")
            ReferenceMetric("Seat G12")
            ReferenceSubtitle("Ticket demonstration")
        }
    }
}

@Composable
internal fun SubscriptionReferenceContent() {
    Column(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            AppleIcon(AppleGlyph.Crown, tint = IslandColors.Orange, size = 32.dp)
            Column(Modifier.weight(1f)) { ReferenceTitle("Aurora Pro"); ReferenceSubtitle("Optional personalization") }
        }
        ReferenceSubtitle("Core notifications and recovery stay free.")
        ReferenceSubtitle("Localized prices are available in Google Play.")
    }
}

@Composable
internal fun NotificationImageReferenceContent() {
    Row(Modifier.fillMaxWidth().padding(AppTheme.spacing.xl), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Box(Modifier.size(103.dp, 86.dp).background(IslandColors.Gray4, RoundedCornerShape(AppTheme.spacing.xxl)), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(ai.emots.kishan_dynamic.R.drawable.shared_media_art),
                contentDescription = "Demonstration attachment",
                modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            ReferenceSubtitle("Messages · image attachment")
            ReferenceTitle("Project review")
            ReferenceSubtitle("The latest photo is ready for review.")
        }
    }
}

@Composable
internal fun TimerImageReferenceContent(timer: String) {
    Row(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        AppleIcon(AppleGlyph.Timer, tint = IslandColors.Orange, size = 40.dp)
        Column(Modifier.weight(1f)) { ReferenceTitle("Focus session"); ReferenceSubtitle("Timer demonstration") }
        ReferenceMetric(timer)
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

@Composable
internal fun ColorOptionsReferenceContent() {
    Column(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl, vertical = AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
        ReferenceTitle("Island colors")
        ReferenceSubtitle("Palette demonstration · preferences unchanged")
        val palettes = listOf(IslandColors.CapsuleCyan, IslandColors.Green, IslandColors.Orange,
            IslandColors.Purple, Color.White, IslandColors.Gray)
        palettes.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { color -> Box(Modifier.size(36.dp).background(color, RoundedCornerShape(AppTheme.spacing.sm))) }
            }
        }
    }
}

@Composable
internal fun GroupCallReferenceContent(participants: List<ai.emots.kishan_dynamic.data.model.ContactInfo>) {
    Row(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            participants.take(2).forEach { person ->
                Box(Modifier.size(AppTheme.spacing.xxxl).background(IslandColors.Gray4, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(person.name.split(' ').mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        style = AppTheme.typography.caption, color = Color.White)
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            ReferenceTitle("Group call")
            ReferenceSubtitle("${participants.size} participants")
        }
    }
}

@Composable
internal fun FlightTrackerReferenceContent(info: ai.emots.kishan_dynamic.data.model.LiveActivityInfo?, progress: Float?) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        ai.emots.kishan_dynamic.ui.components.live.FlightIslandExpanded(info, progress)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ReferenceSubtitle("Route progress")
            ReferenceMetric("${(((progress ?: info?.progress ?: .65f).coerceIn(0f,1f))*100).toInt()}%")
        }
    }
}
