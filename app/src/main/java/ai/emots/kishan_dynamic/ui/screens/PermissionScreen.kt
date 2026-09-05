package ai.emots.kishan_dynamic.ui.screens

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ai.emots.kishan_dynamic.service.AutoStartManager
import ai.emots.kishan_dynamic.service.BackButtonManager
import ai.emots.kishan_dynamic.service.CallScreeningRole
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.model.requiredSetupIsComplete
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.dialogs.PermissionExplanationSheet
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppGlyphBadge
import ai.emots.kishan_dynamic.ui.kit.AppProgressBar
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PermissionItemData(
    val id: String,
    val title: String,
    val description: String,
    val glyph: AppleGlyph,
    val isGranted: Boolean,
    val isRequired: Boolean = true,
    val privacyDetail: String
)

@Composable
fun PermissionScreen(
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = remember { AuroraPreferences(context) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var accessibilityGranted by remember { mutableStateOf(false) }
    var isAccessibilityWorking by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var isNotificationWorking by remember { mutableStateOf(false) }
    var callGranted by remember { mutableStateOf(false) }
    var contactsGranted by remember { mutableStateOf(false) }
    var batteryGranted by remember { mutableStateOf(false) }

    val autoStartAvailable = remember { AutoStartManager.isAutoStartAvailable(context) }
    var autoStartDone by remember { mutableStateOf(AutoStartManager.isAutoStartDone(context)) }

    var isMonitoringAccessibility by remember { mutableStateOf(false) }
    var isMonitoringNotification by remember { mutableStateOf(false) }

    var explanationPermission by remember { mutableStateOf<PermissionItemData?>(null) }
    var showSetupCompleteSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = BackButtonManager.isBackButtonDisabled()) {
        // Prevent back press during transition cooldown
    }

    fun refreshPermissionState() {
        accessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(context)
        isAccessibilityWorking = PermissionUtils.isAccessibilityServiceWorking(context)
        notificationGranted = PermissionUtils.isNotificationListenerEnabled(context)
        isNotificationWorking = PermissionUtils.isNotificationListenerWorking(context)
        callGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED)
        contactsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        batteryGranted = PermissionUtils.isBatteryOptimizationIgnored(context)
        autoStartDone = AutoStartManager.isAutoStartDone(context)
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshPermissionState() }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshPermissionState() }

    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshPermissionState() }

    DisposableEffect(lifecycleOwner, context) {
        refreshPermissionState()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshPermissionState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                refreshPermissionState()
            }
        }
        val filter = IntentFilter(PermissionUtils.ACTION_PERMISSIONS_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LaunchedEffect(isMonitoringAccessibility, isMonitoringNotification) {
        while (isMonitoringAccessibility || isMonitoringNotification) {
            delay(500)
            refreshPermissionState()
            if (isMonitoringAccessibility && accessibilityGranted && isAccessibilityWorking) {
                isMonitoringAccessibility = false
            }
            if (isMonitoringNotification && notificationGranted && isNotificationWorking) {
                isMonitoringNotification = false
            }
        }
    }

    val permissions = remember(
        accessibilityGranted,
        notificationGranted,
        callGranted,
        contactsGranted,
        batteryGranted,
        autoStartAvailable,
        autoStartDone
    ) {
        buildList {
            add(
                PermissionItemData(
                    id = "accessibility",
                    title = "Screen overlay",
                    description = "Draws the island above the status bar and camera cutout.",
                    glyph = AppleGlyph.Sparkles,
                    isGranted = accessibilityGranted,
                    isRequired = true,
                    privacyDetail = "Only used to draw the island overlay. Keystrokes and screen contents are never read or logged."
                )
            )
            add(
                PermissionItemData(
                    id = "notification",
                    title = "Notification access",
                    description = "Shows messages, media and alerts inside the island.",
                    glyph = AppleGlyph.Bell,
                    isGranted = notificationGranted,
                    isRequired = true,
                    privacyDetail = "Notifications are read locally to render the banner. Nothing is transmitted off your phone."
                )
            )
            add(
                PermissionItemData(
                    id = "telecom",
                    title = "Phone & calling",
                    description = "Displays caller details, call controls and direct contact calls.",
                    glyph = AppleGlyph.Phone,
                    isGranted = callGranted,
                    isRequired = false,
                    privacyDetail = "Detects active calls and places calls only when you explicitly choose a contact shortcut. No audio is ever recorded."
                )
            )
            add(
                PermissionItemData(
                    id = "contacts",
                    title = "Contacts",
                    description = "Matches favorite contacts with caller identity and quick actions.",
                    glyph = AppleGlyph.Phone,
                    isGranted = contactsGranted,
                    isRequired = false,
                    privacyDetail = "Only contact names, numbers and optional photos are read locally for your selected shortcuts."
                )
            )
            add(
                PermissionItemData(
                    id = "battery",
                    title = "Background activity",
                    description = "Stops battery savers from closing the island service.",
                    glyph = AppleGlyph.Battery,
                    isGranted = batteryGranted,
                    isRequired = false,
                    privacyDetail = "Keeps the overlay alive so the island doesn't disappear inside heavy apps."
                )
            )
            if (autoStartAvailable) {
                add(
                    PermissionItemData(
                        id = "autostart",
                        title = "Auto-start in background",
                        description = "Keeps the island active after restarts and system memory cleanup.",
                        glyph = AppleGlyph.Settings,
                        isGranted = autoStartDone,
                        isRequired = false,
                        privacyDetail = "Opens manufacturer system settings so the island service runs reliably without being killed."
                    )
                )
            }
        }
    }

    fun requestPermission(id: String) {
        when (id) {
            "accessibility" -> {
                isMonitoringAccessibility = true
                PermissionUtils.openAccessibilitySettings(context)
            }
            "notification" -> {
                isMonitoringNotification = true
                PermissionUtils.openNotificationListenerSettings(context)
            }
            "telecom" -> {
                phonePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ANSWER_PHONE_CALLS
                    )
                )
                if (CallScreeningRole.isAvailable(context) && !CallScreeningRole.isHeld(context)) {
                    CallScreeningRole.requestIntent(context)?.let(callScreeningLauncher::launch)
                }
            }
            "contacts" -> contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            "battery" -> PermissionUtils.openBatteryOptimizationSettings(context)
            "autostart" -> {
                AutoStartManager.openAutoStart(context)
                autoStartDone = true
            }
        }
    }

    fun finishSetup() {
        BackButtonManager.disableBackButtonFor(2000L)
        scope.launch {
            preferences.setIslandEnabled(true)
            preferences.setSetupDone(true)
            onContinue()
        }
    }

    val requiredPermissions = permissions.filter { it.isRequired }
    val optionalPermissions = permissions.filterNot { it.isRequired }
    val grantedCount = permissions.count { it.isGranted }
    val canProceed = requiredSetupIsComplete(
        grantedRequiredCount = requiredPermissions.count { it.isGranted },
        requiredCount = requiredPermissions.size
    )

    AppScreen {
        AppTopBar(
            title = "Permissions",
            subtitle = "What the island needs to work",
            onBack = onBack
        )

        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = "$grantedCount of ${permissions.size} granted",
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    AppText(
                        text = if (canProceed) {
                            "Everything required is in place."
                        } else {
                            "Grant the required items below to activate the island."
                        },
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                AppStatusPill(
                    text = if (canProceed) "Ready" else "Incomplete",
                    color = if (canProceed) AppTheme.colors.success else AppTheme.colors.warning
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppProgressBar(
                progress = grantedCount.toFloat() / permissions.size.toFloat(),
                color = if (canProceed) AppTheme.colors.success else AppTheme.colors.accent
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Required")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            requiredPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { requestPermission(item.id) },
                    onInfo = { explanationPermission = item }
                )
            }
        }

        // Accessibility service alive verification
        if (accessibilityGranted && !isAccessibilityWorking) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppGlyphBadge(
                        glyph = AppleGlyph.Info,
                        tint = AppTheme.colors.warning,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        AppText(
                            text = "Service paused by system",
                            style = AppTheme.typography.body,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.warning
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                        AppText(
                            text = "Android paused the overlay. Turn it off and back on in Settings to revive.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                AppButton(
                    text = "Reactivate in Settings",
                    style = AppButtonStyle.Tonal,
                    onClick = {
                        isMonitoringAccessibility = true
                        PermissionUtils.openAccessibilitySettings(context)
                    }
                )
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Recommended")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            optionalPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { requestPermission(item.id) },
                    onInfo = { explanationPermission = item }
                )
            }
        }

        AppSectionSpacer()

        AppButton(
            text = if (canProceed) "Continue" else "Grant required permissions",
            glyph = if (canProceed) AppleGlyph.Check else AppleGlyph.Shield,
            style = if (canProceed) AppButtonStyle.Primary else AppButtonStyle.Secondary,
            enabled = canProceed,
            onClick = {
                if (canProceed) {
                    showSetupCompleteSheet = true
                }
            }
        )
        AppFootnote("Everything is processed on your device. No data ever leaves your phone.")
    }

    explanationPermission?.let { permission ->
        PermissionExplanationSheet(
            permission = permission,
            onDismiss = { explanationPermission = null },
            onGrantClick = {
                requestPermission(permission.id)
                explanationPermission = null
            }
        )
    }

    if (showSetupCompleteSheet) {
        AppSheet(
            onDismiss = {
                showSetupCompleteSheet = false
                finishSetup()
            },
            title = "Setup Complete",
            subtitle = "Dynamic Island is active",
            glyph = AppleGlyph.Check
        ) {
            AppText(
                text = "All required permissions are granted. Dynamic Island is ready to display music, calls, alerts, and live activities smoothly.",
                style = AppTheme.typography.body,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppButton(
                text = "Get Started",
                glyph = AppleGlyph.Sparkles,
                onClick = {
                    showSetupCompleteSheet = false
                    finishSetup()
                }
            )
        }
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItemData,
    onToggle: () -> Unit,
    onInfo: () -> Unit
) {
    val accent = if (item.isGranted) AppTheme.colors.success else AppTheme.colors.accent

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppGlyphBadge(glyph = item.glyph, tint = accent, size = 42.dp)
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = item.title,
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = item.description,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onInfo
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "i",
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        if (item.isGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(AppTheme.colors.success.copy(alpha = 0.14f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                    .padding(vertical = AppTheme.spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 14.dp)
                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                AppText(
                    text = "Granted",
                    style = AppTheme.typography.button,
                    color = AppTheme.colors.success
                )
            }
        } else {
            AppButton(
                text = "Grant access",
                style = AppButtonStyle.Tonal,
                onClick = onToggle
            )
        }
    }
}
