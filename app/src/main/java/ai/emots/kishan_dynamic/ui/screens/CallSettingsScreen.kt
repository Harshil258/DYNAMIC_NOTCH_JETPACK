package ai.emots.kishan_dynamic.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.dialogs.CallHistorySheet
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryDialog
import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.repository.CallHistoryRepository
import ai.emots.kishan_dynamic.service.CallScreeningRole
import ai.emots.kishan_dynamic.service.PhoneActionLauncher
import ai.emots.kishan_dynamic.ui.dialogs.CallSummaryData
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun CallSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { CallHistoryRepository(context) }
    val preferences = remember { AuroraPreferences(context) }
    val callRecords by repository.records.collectAsState(initial = emptyList())
    val callBannerEnabled by preferences.callBannerEnabled.collectAsState(initial = true)
    val callTimerEnabled by preferences.callTimerEnabled.collectAsState(initial = true)
    val callSummaryEnabled by preferences.callSummaryEnabled.collectAsState(initial = true)
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val effectiveCallSummaryEnabled = PremiumFeaturePolicy.callSummaryEnabled(
        isProActive = isProActive,
        requested = callSummaryEnabled
    )
    val scope = rememberCoroutineScope()
    val screeningAvailable = remember { CallScreeningRole.isAvailable(context) }
    var screeningHeld by remember { mutableStateOf(CallScreeningRole.isHeld(context)) }
    val screeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        screeningHeld = CallScreeningRole.isHeld(context)
    }
    var isExpandedCall by remember { mutableStateOf(true) }

    var showCallSummaryDialog by remember { mutableStateOf(false) }
    var showCallHistorySheet by remember { mutableStateOf(false) }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Phone calls",
            subtitle = "Caller HUD and live call timer",
            onBack = onBack
        )

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (isExpandedCall) "Tap the island to collapse" else "Tap the island to expand",
            minHeight = 190.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedCall) IslandDemoState.CallExpanded else IslandDemoState.CallCompact,
                onTap = { isExpandedCall = !isExpandedCall }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("During a call")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppToggleRow(
                title = "Incoming call banner",
                subtitle = "Caller name, photo, answer and decline",
                glyph = AppleGlyph.Phone,
                checked = callBannerEnabled,
                onCheckedChange = { scope.launch { preferences.setCallBannerEnabled(it) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Call duration timer",
                subtitle = "Live ticking timer while the call is active",
                glyph = AppleGlyph.Timer,
                checked = callTimerEnabled,
                onCheckedChange = { scope.launch { preferences.setCallTimerEnabled(it) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Summary when a call ends",
                subtitle = "Duration with redial and message shortcuts",
                glyph = AppleGlyph.History,
                checked = effectiveCallSummaryEnabled,
                trailingLabel = if (!isProActive) "PRO" else null,
                onCheckedChange = {
                    if (!isProActive && !it) {
                        onNavigateToPremium()
                    } else {
                        scope.launch { preferences.setCallSummaryEnabled(it) }
                    }
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Caller identity")
        AppListCard(modifier = Modifier.appReveal(3)) {
            AppNavRow(
                title = "Call screening role",
                subtitle = when {
                    screeningHeld -> "Improves caller identity when PHONE_STATE omits the number"
                    screeningAvailable -> "Optional Android role for more reliable caller identity"
                    else -> "Not available on this device"
                },
                value = when {
                    screeningHeld -> "Enabled"
                    screeningAvailable -> "Optional"
                    else -> "Unavailable"
                },
                glyph = AppleGlyph.Shield,
                accent = if (screeningHeld) AppTheme.colors.success else AppTheme.colors.info,
                onClick = {
                    CallScreeningRole.requestIntent(context)?.let(screeningLauncher::launch)
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Preview")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            AppButton(
                text = "Call summary",
                glyph = AppleGlyph.Phone,
                style = AppButtonStyle.Secondary,
                fillMaxWidth = false,
                modifier = Modifier.weight(1f),
                enabled = callRecords.isNotEmpty(),
                onClick = { showCallSummaryDialog = callRecords.isNotEmpty() }
            )
            AppButton(
                text = "Call history",
                glyph = AppleGlyph.History,
                style = AppButtonStyle.Secondary,
                fillMaxWidth = false,
                modifier = Modifier.weight(1f),
                onClick = { showCallHistorySheet = true }
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        AppButton(
            text = "Test an incoming call",
            glyph = AppleGlyph.Phone,
            onClick = { isExpandedCall = true }
        )
        AppFootnote("Call records stay on this device and are never uploaded.")
    }

    if (showCallSummaryDialog) {
        CallSummaryDialog(
            data = callRecords.first().toSummaryData(),
            onDismiss = { showCallSummaryDialog = false },
            onRedial = { PhoneActionLauncher.openCall(context, callRecords.first().phoneNumber) },
            onMessage = { context.openMessages(callRecords.first().phoneNumber) },
            onSave = { context.openContactSave(callRecords.first().contactName, callRecords.first().phoneNumber) },
            onCopy = { context.copyToClipboard(callRecords.first().phoneNumber) }
        )
    }

    if (showCallHistorySheet) {
        CallHistorySheet(
            records = callRecords,
            onDismiss = { showCallHistorySheet = false },
            onCallClick = { PhoneActionLauncher.openCall(context, it) },
            onClearHistory = { scope.launch { repository.clear() } }
        )
    }
}

private fun CallRecord.toSummaryData(): CallSummaryData = CallSummaryData(
    contactName = contactName,
    phoneNumber = phoneNumber,
    durationFormatted = if (direction == CallDirection.MISSED) "Missed" else {
        "%02d:%02d".format(durationSeconds / 60L, durationSeconds % 60L)
    },
    callType = when (direction) {
        CallDirection.INCOMING -> "Incoming call"
        CallDirection.OUTGOING -> "Outgoing call"
        CallDirection.MISSED -> "Missed call"
    },
    timestamp = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(startedAtMillis))
)

private fun Context.openMessages(phoneNumber: String) {
    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(phoneNumber)}")))
}

private fun Context.openContactSave(name: String, phoneNumber: String) {
    startActivity(
        Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
        }
    )
}

private fun Context.copyToClipboard(value: String) {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
        ClipData.newPlainText("Phone number", value)
    )
}
