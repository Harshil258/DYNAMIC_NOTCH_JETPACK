package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.motion.rememberBreathing
import ai.emots.kishan_dynamic.ui.motion.rememberPressScale
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.contextualRadarVerticalInset
import ai.emots.kishan_dynamic.ui.theme.IslandColors

private val CallGreen = IslandColors.CapsuleGreen
private val CallRed = IslandColors.CapsuleRed

/**
 * INCOMING CALL banner (86dp capsule).
 *
 * 1:1 implementation matching iOS 17 Figma reference `Dynamic Island-6.svg`:
 * - 56dp circular caller avatar
 * - Subtitle "iPhone" / "Mobile" (#A4A4A9, 12sp) + Caller Name (white bold 17sp)
 * - 52dp Red Decline button (#FA3532) + 52dp Green Accept button (#37C058)
 */
@Composable
fun IncomingCallIsland(
    name: String = "Tamia Castillo", label: String = "iPhone", avatarUri: String? = null,
    onAccept: () -> Unit = {}, onDecline: () -> Unit = {}, canDecline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val pulse = rememberBreathing(min = .96f, max = 1.03f, durationMillis = 1400)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier.fillMaxWidth().wrapContentHeight()) {
        val stacked = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f || maxWidth < 300.dp
        val identity: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Box(Modifier.size(56.dp).graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
                    .clip(CircleShape), contentAlignment = Alignment.Center) {
                    ContactAvatar(name = name, avatarUri = avatarUri, modifier = Modifier.fillMaxSize(), contentDescription = name)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                    Text(label, color = IslandColors.Gray, style = AppTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 16.sp))
                    Text(name, color = Color.White, style = AppTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium))
                }
            }
        }
        val actions: @Composable () -> Unit = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                if (canDecline && canRenderIslandControl(IslandControlAction.CallDecline)) androidx.compose.material3.IconButton(
                    onClick = onDecline, modifier = Modifier.size(52.dp).clip(CircleShape).background(CallRed)
                        .semantics { contentDescription = "Decline call" }
                ) { AppleIcon(AppleGlyph.EndCall, tint = Color.White, size = AppleGlyph.EndCall.figmaInkSize()) }
                if (canRenderIslandControl(IslandControlAction.CallAccept)) androidx.compose.material3.IconButton(
                    onClick = onAccept, modifier = Modifier.size(52.dp).clip(CircleShape).background(CallGreen)
                        .semantics { contentDescription = "Accept call" }
                ) { AppleIcon(AppleGlyph.Phone, tint = Color.White, size = AppleGlyph.Phone.figmaInkSize()) }
            }
        }
        if (stacked) Column(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl,
            vertical = AppTheme.island.contextualRadarVerticalInset), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            identity()
            Box(Modifier.align(Alignment.End)) { actions() }
        } else Row(Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.xxl,
            vertical = AppTheme.island.contextualRadarVerticalInset), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Box(Modifier.weight(1f)) { identity() }
            actions()
        }
    }
}
