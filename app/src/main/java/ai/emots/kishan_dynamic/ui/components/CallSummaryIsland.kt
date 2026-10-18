package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
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
@Composable
internal fun CallSummaryExpandedContent(
    record: CallRecord?,
    onControlAction: (IslandControlAction) -> Unit
) {
    val name = record?.contactName ?: "Call ended"
    val phoneNumber = record?.phoneNumber.orEmpty()
    val isMissed = record?.direction == CallDirection.MISSED
    val callType = when (record?.direction) {
        CallDirection.INCOMING -> "Incoming call"
        CallDirection.OUTGOING -> "Outgoing call"
        CallDirection.MISSED -> "Missed call"
        null -> "Call summary"
    }
    val duration = when {
        record == null -> ""
        isMissed -> "Missed"
        else -> formatCallDuration(record.durationSeconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = name,
                avatarUri = record?.avatarUri,
                modifier = Modifier.size(40.dp),
                contentDescription = name
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (phoneNumber.isBlank()) callType else "$callType · $phoneNumber",
                    color = IslandColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (duration.isNotBlank()) {
                Text(
                    text = duration,
                    color = if (isMissed) IslandColors.Orange else IslandColors.CapsuleGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CallSummaryActionButton(
                glyph = AppleGlyph.Phone,
                label = if (isMissed) "Call back" else "Redial",
                onClick = { onControlAction(IslandControlAction.CallSummaryRedial) },
                modifier = Modifier.weight(1f)
            )
            CallSummaryActionButton(
                glyph = AppleGlyph.Chat,
                label = "SMS",
                onClick = { onControlAction(IslandControlAction.CallSummaryMessage) },
                modifier = Modifier.weight(1f)
            )
            CallSummaryActionButton(
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

@Composable
internal fun CallSummaryActionButton(
    glyph: AppleGlyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(IslandColors.Gray6)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(glyph = glyph, tint = IslandColors.CapsuleCyan, size = 14.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

internal fun formatCallDuration(durationSeconds: Long): String {
    val minutes = (durationSeconds / 60).coerceAtLeast(0)
    val seconds = (durationSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(minutes, seconds)
}
