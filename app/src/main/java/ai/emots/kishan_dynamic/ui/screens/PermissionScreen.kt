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
import ai.emots.kishan_dynamic.MainActivity
import ai.emots.kishan_dynamic.service.AutoStartManager
import ai.emots.kishan_dynamic.service.BackButtonManager
import ai.emots.kishan_dynamic.service.CallScreeningRole
import ai.emots.kishan_dynamic.service.PermissionUtils
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.model.SetupReadiness
import ai.emots.kishan_dynamic.data.model.SetupAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.repeatOnLifecycle
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
import ai.emots.kishan_dynamic.data.analytics.AnalyticsEventNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsParamNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
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
    onBack: () -> Unit = {},
    analyticsTracker: AnalyticsTracker = FirebaseAnalyticsTracker(LocalContext.current)
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
    var screeningGranted by remember { mutableStateOf(false) }

    var autoStartAvailable = remember { AutoStartManager.isAutoStartAvailable(context) }
    var autoStartDone by remember { mutableStateOf(AutoStartManager.isAutoStartDone(context)) }

    var isMonitoringAccessibility by remember { mutableStateOf(false) }
    var isMonitoringNotification by remember { mutableStateOf(false) }

    var explanationPermission by remember { mutableStateOf<PermissionItemData?>(null) }
    var isFinishing by remember { mutableStateOf(false) }
    var setupError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        analyticsTracker.logEvent(AnalyticsEventNames.PERMISSION_FLOW_STARTED)
    }

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
        screeningGranted = CallScreeningRole.isHeld(context)
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
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissionState()
                if (accessibilityGranted && notificationGranted) {
                    scope.launch {
                        preferences.setIslandEnabled(true)
                        preferences.setSetupDone(true)
                    }
                    PermissionUtils.sendPermissionsChangedBroadcast(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                refreshPermissionState()
                if (accessibilityGranted && notificationGranted) {
                    scope.launch {
                        preferences.setIslandEnabled(true)
                        preferences.setSetupDone(true)
                    }
                }
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

    // Reference-style continuous monitoring for Accessibility service activation in Settings
    LaunchedEffect(isMonitoringAccessibility) {
        if (isMonitoringAccessibility) {
            while (isMonitoringAccessibility) {
                delay(500)
                val currentAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                val currentWorking = PermissionUtils.isAccessibilityServiceWorking(context)
                accessibilityGranted = currentAccessibility
                isAccessibilityWorking = currentWorking

                if (currentAccessibility) {
                    isMonitoringAccessibility = false
                    PermissionUtils.isAwaitingAccessibilityReturn = false
                    BackButtonManager.disableBackButtonFor(2000L)

                    val currentNotification = PermissionUtils.isNotificationListenerEnabled(context)
                    notificationGranted = currentNotification
                    isNotificationWorking = PermissionUtils.isNotificationListenerWorking(context)

                    if (currentAccessibility && currentNotification) {
                        scope.launch {
                            preferences.setIslandEnabled(true)
                            preferences.setSetupDone(true)
                        }
                        PermissionUtils.sendPermissionsChangedBroadcast(context)
                    }

                    val intent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("permission_auto_resumed", true)
                    }
                    context.startActivity(intent)
                    break
                }
            }
        }
    }

    // Reference-style continuous monitoring for Notification access activation in Settings
    LaunchedEffect(isMonitoringNotification) {
        if (isMonitoringNotification) {
            while (isMonitoringNotification) {
                delay(500)
                val currentNotification = PermissionUtils.isNotificationListenerEnabled(context)
                val currentWorking = PermissionUtils.isNotificationListenerWorking(context)
                notificationGranted = currentNotification
                isNotificationWorking = currentWorking

                if (currentNotification) {
                    isMonitoringNotification = false
                    PermissionUtils.isAwaitingNotificationReturn = false
                    BackButtonManager.disableBackButtonFor(2000L)

                    val currentAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                    accessibilityGranted = currentAccessibility
                    isAccessibilityWorking = PermissionUtils.isAccessibilityServiceWorking(context)

                    if (currentAccessibility && currentNotification) {
                        scope.launch {
                            preferences.setIslandEnabled(true)
                            preferences.setSetupDone(true)
                        }
                        PermissionUtils.sendPermissionsChangedBroadcast(context)
                    }

                    val intent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("permission_auto_resumed", true)
                    }
                    context.startActivity(intent)
                    break
                }
            }
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // A short foreground-only settling window after Settings; broadcasts
            // handle later connection changes. No endless background polling.
            repeat(6) {
                refreshPermissionState()
                if ((!accessibilityGranted || isAccessibilityWorking) &&
                    (!notificationGranted || isNotificationWorking)) return@repeatOnLifecycle
                delay(500)
            }
        }
    }

    val permissions = remember(
        accessibilityGranted,
        notificationGranted,
        callGranted,
        contactsGranted,
        batteryGranted,
        screeningGranted,
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
                        description = if (autoStartDone) "Settings visited. Verify auto-start is enabled in your phone's settings." else "Review your phone's settings to allow the island to restart in the background.",
                        glyph = AppleGlyph.Settings,
                        // OEMs do not expose a reliable grant API; a visit isn't a grant.
                        isGranted = false,
                        isRequired = false,
                        privacyDetail = "Opens manufacturer system settings so the island service runs reliably without being killed."
                    )
                )
            }
            if (CallScreeningRole.isAvailable(context)) {
                add(PermissionItemData(
                    id = "screening", title = "Call identification",
                    description = "Optional Android role for identifying incoming calls.",
                    glyph = AppleGlyph.Phone, isGranted = screeningGranted,
                    isRequired = false,
                    privacyDetail = "Android shares call identification with the chosen app. This role is optional and does not block the island."
                ))
            }
        }
    }

    fun requestPermission(id: String) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.PERMISSION_RESULT,
            mapOf(
                AnalyticsParamNames.PERMISSION_TYPE to id,
                AnalyticsParamNames.ACTION to "requested"
            )
        )
        when (id) {
            "accessibility" -> {
                isMonitoringAccessibility = true
                PermissionUtils.openAccessibilitySettings(context) {
                    isMonitoringAccessibility = true
                }
            }
            "notification" -> {
                isMonitoringNotification = true
                PermissionUtils.openNotificationListenerSettings(context) {
                    isMonitoringNotification = true
                }
            }
            "telecom" -> {
                phonePermissionLauncher.launch(
                    buildList {
                        add(Manifest.permission.READ_PHONE_STATE)
                        add(Manifest.permission.CALL_PHONE)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) add(Manifest.permission.ANSWER_PHONE_CALLS)
                    }.toTypedArray()
                )
            }
            "contacts" -> contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            "screening" -> CallScreeningRole.requestIntent(context)?.let(callScreeningLauncher::launch)
            "battery" -> PermissionUtils.openBatteryOptimizationSettings(context)
            "autostart" -> {
                AutoStartManager.openAutoStart(context)
                autoStartDone = true
            }
        }
    }

    fun finishSetup() {
        refreshPermissionState()
        val currentReadiness = SetupReadiness(
            accessibilityGranted, isAccessibilityWorking,
            notificationGranted, isNotificationWorking
        )
        if ((!currentReadiness.ready && !currentReadiness.areRequiredGranted) || isFinishing) return
        analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_ACTIVATION_STARTED)
        isFinishing = true
        setupError = null
        scope.launch {
            try {
                preferences.setIslandEnabled(true)
                preferences.setSetupDone(true)
                onContinue()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                setupError = "Couldn't save setup. Please try again."
            } finally {
                isFinishing = false
            }
        }
    }

    val requiredPermissions = permissions.filter { it.isRequired }
    val optionalPermissions = permissions.filterNot { it.isRequired }
    val grantedCount = permissions.count { it.isGranted }
    val readiness = SetupReadiness(
        accessibilityGranted, isAccessibilityWorking,
        notificationGranted, isNotificationWorking
    )
    val areCoreGranted = readiness.areRequiredGranted
    val canProceed = readiness.ready || areCoreGranted

    val nextAction = when {
        canProceed -> "Continue"
        !accessibilityGranted -> "Enable screen overlay"
        !notificationGranted -> "Enable notification access"
        else -> "Continue"
    }
    val guidance = when {
        readiness.ready -> "Both required services are connected. Recommended access is optional."
        areCoreGranted -> "Required permissions granted in Settings. Services are connecting in the background."
        !accessibilityGranted -> "Allow the island to appear above other apps."
        !notificationGranted -> "Allow notifications to appear in the island."
        else -> "Grant required permissions to display the island."
    }

    AppScreen(
        topBar = {
            AppTopBar(
                title = "Permissions",
                subtitle = "What the island needs to work",
                onBack = onBack
            )
        },
        bottomAction = {
            setupError?.let { AppFootnote(it) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                AppButton(
                    text = if (isFinishing) "Starting…" else nextAction,
                    glyph = if (canProceed) AppleGlyph.Check else AppleGlyph.Shield,
                    enabled = !isFinishing,
                    onClick = {
                        if (canProceed) {
                            finishSetup()
                        } else if (!accessibilityGranted) {
                            requestPermission("accessibility")
                        } else if (!notificationGranted) {
                            requestPermission("notification")
                        } else {
                            finishSetup()
                        }
                    }
                )
                if (!canProceed) {
                    AppButton(
                        text = "Explore the app first",
                        glyph = AppleGlyph.ChevronRight,
                        style = AppButtonStyle.Secondary,
                        onClick = onContinue
                    )
                }
            }
        }
    ) {

        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = if (readiness.ready) {
                            "All required services ready"
                        } else if (areCoreGranted) {
                            "2 of 2 required permissions granted"
                        } else {
                            "${listOf(accessibilityGranted, notificationGranted).count { it }} of 2 required permissions granted"
                        },
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    AppText(
                        text = guidance,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                AppStatusPill(
                    text = if (readiness.ready) "Ready" else if (areCoreGranted) "Connecting…" else "Setup needed",
                    color = if (readiness.ready) AppTheme.colors.success else if (areCoreGranted) AppTheme.colors.accent else AppTheme.colors.warning
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppProgressBar(
                progress = if (readiness.ready) 1f else if (areCoreGranted) 0.9f else listOf(accessibilityGranted, notificationGranted).count { it } / 2f,
                color = if (readiness.ready) AppTheme.colors.success else AppTheme.colors.accent
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Required")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            requiredPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { explanationPermission = item },
                    isWorking = if (item.id == "accessibility") isAccessibilityWorking else isNotificationWorking,
                    onInfo = { explanationPermission = item }
                )
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Recommended")
        AppFootnote("Optional — you can continue without these. $grantedCount of ${permissions.size} access options enabled.")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            optionalPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { explanationPermission = item },
                    onInfo = { explanationPermission = item }
                )
            }
        }

        AppSectionSpacer()

        AppFootnote("Notification content, caller identities and media controls are processed entirely on your device. Anonymous crash reports and performance metrics help improve stability.")
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

}

@Composable
private fun PermissionCard(
    item: PermissionItemData,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
    isWorking: Boolean = item.isGranted
) {
    val accent = if (item.isGranted && isWorking) AppTheme.colors.success else AppTheme.colors.accent

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
                    .size(48.dp)
                    .semantics { contentDescription = "About ${item.title}" }
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onInfo,
                        role = Role.Button
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

        if (item.isGranted && isWorking) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(AppTheme.colors.success.copy(alpha = 0.14f))
                    .padding(vertical = AppTheme.spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 14.dp)
                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                AppText(
                    text = if (item.isRequired) "Access enabled · Connected" else "Access enabled",
                    style = AppTheme.typography.button,
                    color = AppTheme.colors.success
                )
            }
        } else {
            AppButton(
                text = if (item.id == "autostart") "Review settings" else if (item.isGranted) "Access enabled · Connecting…" else "Enable access",
                style = AppButtonStyle.Tonal,
                onClick = onToggle
            )
        }
    }
}
