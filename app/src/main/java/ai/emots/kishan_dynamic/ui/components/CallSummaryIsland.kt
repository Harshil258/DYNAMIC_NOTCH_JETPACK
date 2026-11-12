package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalDensity
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
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
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.ui.components.live.IslandAdaptiveHeader
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Call summary presentation for the Dynamic Island HUD.
 * Renders contact identity, call type, call duration or missed badge, and 1-tap quick action shortcuts:
 * - Direct redial or "Call back" for missed calls
 * - SMS messaging intent
 * - WhatsApp direct chat via wa.me normalization
 * - Dismiss / done action
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CallSummaryExpandedContent(
    record: CallRecord?,
    onControlAction: (IslandControlAction) -> Unit
) {
    val isDemoPreview = record == null
    val name = record?.contactName?.ifBlank { null } ?: if (isDemoPreview) "Tamia Castillo" else "Unknown caller"
    val phoneNumber = record?.phoneNumber?.ifBlank { null } ?: if (isDemoPreview) "+1 (555) 019-2834" else ""
    val isMissed = record?.direction == CallDirection.MISSED
    val callType = when (record?.direction) {
        CallDirection.INCOMING -> "Incoming call"
        CallDirection.OUTGOING -> "Outgoing call"
        CallDirection.MISSED -> "Missed call"
        null -> if (isDemoPreview) "Call ended" else "Call summary"
    }
    val duration = if (isDemoPreview) "02:45" else callSummaryDurationLabel(record)
    val measuredDuration = isDemoPreview || (record != null && !isMissed && record.durationAccuracy != DurationAccuracy.UNKNOWN)
    val contactActionsAvailable = isDemoPreview || phoneNumber.any(Char::isDigit)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IslandAdaptiveHeader(identity = {
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
                        name,
                        color = Color.White,
                        style = AppTheme.typography.body.copy(
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        if (phoneNumber.isBlank()) callType else "$callType · $phoneNumber",
                        color = IslandColors.TextSecondary,
                        style = AppTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }, status = {
            if (duration.isNotBlank()) Text(
                duration,
                color = when {
                    isMissed -> IslandColors.Orange
                    measuredDuration -> IslandColors.CapsuleGreen
                    else -> IslandColors.TextSecondary
                },
                style = AppTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (measuredDuration) FontFamily.Monospace else FontFamily.Default,
                    textDirection = TextDirection.Ltr
                )
            )
        })

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (LocalDensity.current.fontScale > 1.3f || maxWidth < 320.dp) 2 else 4
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (contactActionsAvailable && canRenderIslandControl(IslandControlAction.CallSummaryRedial)) CallSummaryActionButton(
                    glyph = AppleGlyph.Phone,
                    label = if (isMissed) "Call back" else "Redial",
                    onClick = { onControlAction(IslandControlAction.CallSummaryRedial) },
                    modifier = Modifier.weight(1f)
                )
                if (contactActionsAvailable && canRenderIslandControl(IslandControlAction.CallSummaryMessage)) CallSummaryActionButton(
                    glyph = AppleGlyph.Chat,
                    label = "SMS",
                    onClick = { onControlAction(IslandControlAction.CallSummaryMessage) },
                    modifier = Modifier.weight(1f)
                )
                if (contactActionsAvailable && canRenderIslandControl(IslandControlAction.CallSummaryWhatsApp)) CallSummaryActionButton(
                    glyph = AppleGlyph.Chat,
                    label = "WhatsApp",
                    onClick = { onControlAction(IslandControlAction.CallSummaryWhatsApp) },
                    modifier = Modifier.weight(1f)
                )
                CallSummaryActionButton(
                    glyph = AppleGlyph.Close,
                    label = "Done",
                    onClick = { onControlAction(IslandControlAction.CallSummaryDismiss) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun CallSummaryActionButton(
    glyph: AppleGlyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(IslandColors.Gray6)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(glyph = glyph, tint = IslandColors.CapsuleCyan, size = 13.dp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
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
