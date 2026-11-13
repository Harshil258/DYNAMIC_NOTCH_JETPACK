package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.content.Intent
import android.net.Uri
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.ui.components.live.IslandAdaptiveHeader
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Call summary presentation for the Dynamic Island HUD (V6 Specification).
 * Target geometry: 367×112dp, R42.
 *
 * Visual hierarchy:
 * - Top row: 40dp avatar START; caller name 15-16sp bold; subtitle indicates true call outcome
 *   ("Call ended · Incoming", "Outgoing call ended", "Missed call"); static duration END (neutral color, NOT green).
 * - Bottom row: Focused actions: 1 primary action ("Call back" / "Redial") if backed by capability,
 *   plus compact "Done" dismiss. Eliminates cramped 4-button dashboard.
 * - Production truth: Empty/null records do NOT fake caller identity or contact shortcuts.
 */
@Composable
internal fun CallSummaryExpandedContent(
    record: CallRecord?,
    onControlAction: (IslandControlAction) -> Unit
) {
    val isDemoPreview = record?.id == "gallery_summary" || (record == null && false)
    val name = record?.contactName?.ifBlank { null }
        ?: record?.phoneNumber?.ifBlank { null }
        ?: if (isDemoPreview) "Tamia Castillo" else "No recent caller"
    val phoneNumber = record?.phoneNumber?.ifBlank { null }
        ?: if (isDemoPreview) "+1 (555) 019-2834" else null
    val isMissed = record?.direction == CallDirection.MISSED
    val phaseText = callSummaryPhaseLabel(record, isDemoPreview)
    val subtitle = if (!phoneNumber.isNullOrBlank() && name != phoneNumber) {
        "$phaseText · $phoneNumber"
    } else {
        phaseText
    }
    val duration = if (isDemoPreview) "02:45" else callSummaryDurationLabel(record)
    val measuredDuration = isDemoPreview || (record != null && !isMissed && record.durationAccuracy != DurationAccuracy.UNKNOWN)
    val context = LocalContext.current
    val canRedial = isDemoPreview || IslandActionCapability.canDial(context, phoneNumber)
    val canSms = isDemoPreview || IslandActionCapability.canSendSms(context, phoneNumber)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top row: Avatar + Caller Info + Static Duration
        IslandAdaptiveHeader(
            identity = {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    ContactAvatar(
                        name = name,
                        avatarUri = record?.avatarUri,
                        modifier = Modifier.size(40.dp),
                        contentDescription = name
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = Color.White,
                            style = AppTheme.typography.body.copy(
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            color = if (isMissed) IslandColors.Orange else IslandColors.TextSecondary,
                            style = AppTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            status = {
                if (duration.isNotBlank() && !isMissed) {
                    Text(
                        text = duration,
                        color = IslandColors.TextSecondary, // Neutral color, never live green
                        style = AppTheme.typography.bodySmall.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = if (measuredDuration) FontFamily.Monospace else FontFamily.Default,
                            textDirection = TextDirection.Ltr
                        )
                    )
                }
            }
        )

        // Bottom row: Focused actions (Primary Redial/Call back + Quiet Done dismiss)
        // V6D mandate: Maximum two default controls with high visual hierarchy differentiation.
        IslandControlRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val showRedial = canRedial && canRenderIslandControl(IslandControlAction.CallSummaryRedial)
            if (showRedial) {
                IslandActionPill(
                    glyph = AppleGlyph.Phone,
                    label = if (isMissed) "Call back" else "Redial",
                    onClick = { onControlAction(IslandControlAction.CallSummaryRedial) },
                    modifier = Modifier.weight(1.35f),
                    style = IslandActionStyle.Primary,
                    glyphSize = 18.dp,
                    visualHeight = 38.dp,
                    contentDescription = if (isMissed) "Call back $name" else "Redial $name"
                )
            }
            IslandActionPill(
                glyph = AppleGlyph.Close,
                label = "Done",
                onClick = { onControlAction(IslandControlAction.CallSummaryDismiss) },
                modifier = Modifier.weight(if (showRedial) 0.95f else 1f),
                style = IslandActionStyle.Tertiary,
                glyphSize = 16.dp,
                visualHeight = 38.dp,
                contentDescription = "Dismiss call summary"
            )
        }
    }
}

@Composable
internal fun CallSummaryActionButton(
    glyph: AppleGlyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    IslandActionPill(
        glyph = glyph,
        label = label,
        onClick = onClick,
        modifier = modifier,
        style = if (isPrimary) IslandActionStyle.Primary else IslandActionStyle.Tertiary,
        glyphSize = 18.dp,
        visualHeight = 38.dp
    )
}

internal fun formatCallDuration(durationSeconds: Long): String {
    val minutes = (durationSeconds / 60).coerceAtLeast(0)
    val seconds = (durationSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(minutes, seconds)
}

/** Unknown/estimated call durations must not become an exact zero-length call in the UI. */
internal fun callSummaryDurationLabel(record: CallRecord?): String = when {
    record == null -> ""
    record.direction == CallDirection.MISSED -> "Missed"
    record.durationAccuracy == DurationAccuracy.UNKNOWN -> "Duration unavailable"
    record.durationAccuracy == DurationAccuracy.INFERRED_WALL_CLOCK -> "≈${formatCallDuration(record.durationSeconds)}"
    else -> formatCallDuration(record.durationSeconds)
}

/** Distinguish call direction from phase so ended calls never sound like active ringing. */
internal fun callSummaryPhaseLabel(record: CallRecord?, isDemoPreview: Boolean = false): String = when (record?.direction) {
    CallDirection.INCOMING -> "Call ended · Incoming"
    CallDirection.OUTGOING -> "Outgoing call ended"
    CallDirection.MISSED -> "Missed call"
    null -> if (isDemoPreview) "Call ended · Incoming" else "Call ended"
}
