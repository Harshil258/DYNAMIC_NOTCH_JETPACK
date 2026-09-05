package ai.emots.kishan_dynamic.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.app.KeyguardManager
import android.media.AudioManager
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.OverlayDisplayPolicy
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumExpiryPolicy
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.repository.ActionIslandRepository
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionUtilityAction
import ai.emots.kishan_dynamic.MainActivity
import ai.emots.kishan_dynamic.ui.NotificationReplyActivity
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * System Accessibility Overlay Service that renders the Dynamic Island on top of all Android windows.
 */
class IslandOverlayService : AccessibilityService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var preferences: AuroraPreferences
    private lateinit var actionIslandRepository: ActionIslandRepository
    private lateinit var actionSystemController: ActionSystemController
    private lateinit var callSystemController: CallSystemController
    private lateinit var audioVolumeObserver: AudioVolumeObserver
    private lateinit var systemLiveActivityMonitor: SystemLiveActivityMonitor
    private lateinit var bluetoothAccessoryMonitor: BluetoothAccessoryMonitor
    private lateinit var mediaRouteActivityMonitor: MediaRouteActivityMonitor
    private lateinit var satelliteActivityMonitor: SatelliteActivityMonitor
    private var composeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private val deviceLocked = MutableStateFlow(false)
    private val systemIslandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> deviceLocked.value = true
                Intent.ACTION_SCREEN_ON -> syncDeviceLockState()
                Intent.ACTION_USER_PRESENT -> deviceLocked.value = false
                PermissionUtils.ACTION_PERMISSIONS_CHANGED -> {
                    syncFocusModeState()
                    syncDeviceLockState()
                }
                NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED -> {
                    val filter = intent.getIntExtra(
                        INTERRUPTION_FILTER_EXTRA,
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                    IslandStateManager.postFocusMode(
                        filter != NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                    val mode = when (intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)) {
                        AudioManager.RINGER_MODE_SILENT -> ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT
                        AudioManager.RINGER_MODE_VIBRATE -> ai.emots.kishan_dynamic.data.model.RingerModeType.VIBRATE
                        else -> ai.emots.kishan_dynamic.data.model.RingerModeType.NORMAL
                    }
                    scope.launch {
                        val preferences = AuroraPreferences(applicationContext)
                        val isProActive = preferences.isProActive.first()
                        val enabled = when (mode) {
                            ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT ->
                                PremiumFeaturePolicy.soundSettingEnabled(
                                    isProActive,
                                    preferences.showRingerModeIndicator.first()
                                ) && PremiumFeaturePolicy.soundSettingEnabled(
                                    isProActive,
                                    preferences.muteIndicatorEnabled.first()
                                )
                            ai.emots.kishan_dynamic.data.model.RingerModeType.VIBRATE ->
                                PremiumFeaturePolicy.soundSettingEnabled(
                                    isProActive,
                                    preferences.showRingerModeIndicator.first()
                                ) && PremiumFeaturePolicy.soundSettingEnabled(
                                    isProActive,
                                    preferences.vibrateIndicatorEnabled.first()
                                )
                            ai.emots.kishan_dynamic.data.model.RingerModeType.NORMAL ->
                                PremiumFeaturePolicy.soundSettingEnabled(
                                    isProActive,
                                    preferences.showRingerModeIndicator.first()
                                )
                        }
                        if (enabled) IslandStateManager.postRingerMode(mode)
                        else IslandStateManager.clearTransientSystemState()
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val canReadName = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    val batteryLevel = intent
                        .getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                        .takeIf { it in 0..100 }
                    if (::bluetoothAccessoryMonitor.isInitialized) {
                        bluetoothAccessoryMonitor.onBatteryLevel(device, batteryLevel ?: -1)
                    }
                    IslandStateManager.postBluetoothDevice(
                        deviceName = if (canReadName) device?.name ?: "Bluetooth device" else "Bluetooth device",
                        batteryPercent = batteryLevel
                    )
                }
                BLUETOOTH_PROFILE_CONNECTION_STATE_CHANGED_ACTION -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val canReadName = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    val state = intent.getIntExtra(
                        BluetoothProfile.EXTRA_STATE,
                        BluetoothProfile.STATE_DISCONNECTED
                    )
                    when (state) {
                        BluetoothProfile.STATE_CONNECTING -> IslandStateManager.postBluetoothDevice(
                            deviceName = if (canReadName) device?.name ?: "Bluetooth device" else "Bluetooth device",
                            isConnecting = true
                        )
                        BluetoothProfile.STATE_CONNECTED -> IslandStateManager.postBluetoothDevice(
                            deviceName = if (canReadName) device?.name ?: "Bluetooth device" else "Bluetooth device"
                        )
                        BluetoothProfile.STATE_DISCONNECTED -> IslandStateManager.clearBluetoothDevice()
                    }
                }
                BLUETOOTH_BATTERY_LEVEL_CHANGED_ACTION -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val batteryLevel = intent
                        .getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                        .takeIf { it in 0..100 }
                    if (::bluetoothAccessoryMonitor.isInitialized) {
                        bluetoothAccessoryMonitor.onBatteryLevel(device, batteryLevel ?: -1)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> IslandStateManager.clearBluetoothDevice()
            }
        }
    }

    // Lifecycle requirements for Compose in a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceActuallyConnected = true
        PermissionUtils.sendPermissionsChangedBroadcast(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceActuallyConnected = false
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferences = AuroraPreferences(applicationContext)
        actionIslandRepository = ActionIslandRepository(applicationContext)
        scope.launch(Dispatchers.IO) { actionIslandRepository.pruneMissingApps() }
        actionSystemController = ActionSystemController(applicationContext)
        callSystemController = CallSystemController(applicationContext)
        audioVolumeObserver = AudioVolumeObserver(applicationContext)
        audioVolumeObserver.register()
        systemLiveActivityMonitor = SystemLiveActivityMonitor(applicationContext, scope)
        systemLiveActivityMonitor.start()
        bluetoothAccessoryMonitor = BluetoothAccessoryMonitor(applicationContext, scope)
        bluetoothAccessoryMonitor.start()
        mediaRouteActivityMonitor = MediaRouteActivityMonitor(applicationContext)
        mediaRouteActivityMonitor.start()
        satelliteActivityMonitor = SatelliteActivityMonitor(applicationContext)
        satelliteActivityMonitor.start()

        syncDeviceLockState()
        registerSystemIslandReceiver()
        syncFocusModeState()
        observePreferences()
    }

    private fun syncFocusModeState() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        IslandStateManager.postFocusMode(
            manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    private fun syncDeviceLockState() {
        deviceLocked.value = getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true
    }

    companion object {
        private const val TAG = "IslandOverlayService"
        const val INTERRUPTION_FILTER_EXTRA = "android.app.extra.INTERRUPTION_FILTER"
        const val BLUETOOTH_BATTERY_LEVEL_CHANGED_ACTION =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        const val BLUETOOTH_PROFILE_CONNECTION_STATE_CHANGED_ACTION =
            "android.bluetooth.profile.action.CONNECTION_STATE_CHANGED"

        @Volatile
        var isServiceActuallyConnected = false

        fun isServiceReallyConnected(): Boolean = isServiceActuallyConnected
    }

    private fun registerSystemIslandReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(PermissionUtils.ACTION_PERMISSIONS_CHANGED)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BLUETOOTH_PROFILE_CONNECTION_STATE_CHANGED_ACTION)
            addAction(BLUETOOTH_BATTERY_LEVEL_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(
            this,
            systemIslandReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun initializeWindow() {
        if (composeView != null) return

        val initialVerticalOffset = runCatching {
            runBlocking { preferences.verticalOffset.first() }
        }.getOrDefault(AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP)

        val initialHorizontalOffset = runCatching {
            runBlocking {
                if (PremiumFeaturePolicy.horizontalOffsetEnabled(preferences.isProActive.first())) {
                    preferences.horizontalOffset.first()
                } else {
                    AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP
                }
            }
        }.getOrDefault(AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP)

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = initialHorizontalOffset
            y = initialVerticalOffset
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        windowLayoutParams = layoutParams

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@IslandOverlayService)
            setViewTreeViewModelStoreOwner(this@IslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@IslandOverlayService)

            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                    IslandStateManager.collapseExpandedState()
                    true
                } else {
                    false
                }
            }

            setContent {
                val islandState by IslandStateManager.currentState.collectAsState()
                val isEnabled by preferences.islandEnabled.collectAsState(initial = true)
                val alwaysOnTop by preferences.alwaysOnTop.collectAsState(initial = true)
                val showOnLockScreen by preferences.showOnLockScreen.collectAsState(initial = true)
                val animationSpeedNormal by preferences.animationSpeedNormal.collectAsState(initial = true)
                val compactMusicControls by preferences.compactMusicControls.collectAsState(initial = false)
                val isProActive by preferences.isProActive.collectAsState(initial = false)
                val isDeviceLocked by deviceLocked.collectAsState()
                val widthScale by preferences.widthScale.collectAsState(
                    initial = AuroraPreferences.DEFAULT_WIDTH_SCALE
                )
                val swipeUpDismiss by preferences.swipeUpDismiss.collectAsState(initial = true)
                val musicScrubberEnabled by preferences.musicScrubberEnabled.collectAsState(initial = true)
                val pulseScale by preferences.pulseScale.collectAsState(initial = 1.15f)
                val pulseDuration by preferences.pulseDuration.collectAsState(initial = 450)
                val hapticFeedbackEnabled by preferences.hapticFeedbackEnabled.collectAsState(initial = true)
                val actionConfig by actionIslandRepository.config.collectAsState(
                    initial = ai.emots.kishan_dynamic.data.model.ActionIslandConfig()
                )
                val actionSystemSnapshot by actionSystemController.snapshot.collectAsState()

                // The island drops out of the cutout rather than popping in.
                androidx.compose.animation.AnimatedVisibility(
                    visible = OverlayDisplayPolicy(
                        islandEnabled = isEnabled,
                        alwaysOnTop = alwaysOnTop,
                        showOnLockScreen = showOnLockScreen,
                        deviceLocked = isDeviceLocked
                    ).shouldRender(islandState is IslandState.Hidden),
                    enter = androidx.compose.animation.expandVertically(
                        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.islandSpring(),
                        expandFrom = Alignment.Top
                    ) + androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(180)
                    ),
                    exit = androidx.compose.animation.shrinkVertically(
                        animationSpec = ai.emots.kishan_dynamic.ui.motion.AppMotion.islandSpring(),
                        shrinkTowards = Alignment.Top
                    ) + androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(140)
                    )
                ) {
                    AppTheme(darkTheme = true) {
                        var brightnessLevel by remember {
                            mutableFloatStateOf(actionSystemController.readBrightness())
                        }
                        var mediaVolumeLevel by remember {
                            mutableFloatStateOf(actionSystemController.readMediaVolume())
                        }
                        Box(
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val renderedState = islandState
                            val compactMusicControlsEnabled = PremiumFeaturePolicy.compactMusicControlsEnabled(
                                isProActive = isProActive,
                                requested = compactMusicControls
                            )
                            val demoState = if (compactMusicControlsEnabled && renderedState is IslandState.Music) {
                                IslandDemoState.MusicCompact
                            } else {
                                mapToDemoState(renderedState)
                            }
                            val callContact = when (renderedState) {
                                is IslandState.IncomingCall -> renderedState.contact
                                is IslandState.OngoingCall -> renderedState.contact
                                else -> null
                            }
                            val callSummary = (renderedState as? IslandState.CallSummary)?.record
                            val callDurationSeconds = (renderedState as? IslandState.OngoingCall)?.durationSeconds ?: 0L
                            val showCallDuration = (renderedState as? IslandState.OngoingCall)?.showDuration ?: true
                            val callIsDialing = (renderedState as? IslandState.OngoingCall)?.isDialing == true
                            val musicState = renderedState as? IslandState.Music
                            val timerActivity = (renderedState as? IslandState.LiveActivity)?.activity?.takeIf {
                                it.kind == ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER
                            }
                            val liveActivity = (renderedState as? IslandState.LiveActivity)?.activity
                            val actionNotificationCount = (renderedState as? IslandState.ActionControl)?.notificationCount ?: 0
                            val notificationItems = when (renderedState) {
                                is IslandState.Notification -> renderedState.notifications
                                is IslandState.NotificationWithMusic -> renderedState.notifications
                                else -> emptyList()
                            }
                            val ringerVolumeState = renderedState as? IslandState.RingerVolume
                            val mediaVolumeState = renderedState as? IslandState.MediaVolume
                            val bluetoothState = renderedState as? IslandState.BluetoothDevice
                            val batteryState = renderedState as? IslandState.Charging
                            val premiumExpiryState = renderedState as? IslandState.PremiumExpiry
                            DynamicIslandPill(
                                state = demoState,
                                horizontalScale = widthScale,
                                onTap = {
                                    if (!(compactMusicControlsEnabled && islandState is IslandState.Music)) {
                                        if (actionConfig.isEnabled || islandState !is IslandState.Minimal) {
                                            IslandStateManager.toggleExpansion()
                                        }
                                    }
                                },
                                onCompanionTap = {
                                    when (val currentIsland = islandState) {
                                        is IslandState.Notification -> {
                                            if (currentIsland.notifications.size > 1) {
                                                IslandStateManager.selectNextNotification()
                                            } else {
                                                IslandStateManager.toggleExpansion()
                                            }
                                        }
                                        is IslandState.NotificationWithMusic -> {
                                            MediaPlaybackRegistry.snapshot.value?.let { snapshot ->
                                                IslandStateManager.postMusicPlayback(
                                                    track = ai.emots.kishan_dynamic.data.model.MusicTrack(
                                                        title = snapshot.title,
                                                        artist = snapshot.artist,
                                                        packageName = snapshot.packageName,
                                                        durationMs = snapshot.durationMs,
                                                        positionMs = snapshot.positionMs,
                                                        albumArtUri = snapshot.albumArtUri
                                                    ),
                                                    isPlaying = snapshot.isPlaying
                                                )
                                                IslandStateManager.toggleExpansion()
                                            } ?: IslandStateManager.toggleExpansion()
                                        }
                                        is IslandState.LiveActivity -> {
                                            if (currentIsland.activity.kind == ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER) {
                                                IslandStateManager.toggleTimer()
                                            } else {
                                                IslandStateManager.toggleExpansion()
                                            }
                                        }
                                        else -> IslandStateManager.toggleExpansion()
                                    }
                                },
                                onLongPress = {
                                    if (actionConfig.isEnabled) IslandStateManager.openActionControl()
                                },
                                onSwipeDismiss = {
                                    if (swipeUpDismiss) IslandStateManager.handleSwipeDismiss()
                                },
                                onControlAction = ::handleIslandControlAction,
                                onOpenNotifications = { IslandStateManager.openNotifications() },
                                actionApps = actionConfig.apps.filter { it.isEnabled },
                                actionSystemTiles = actionConfig.systemTiles,
                                actionSystemStates = actionSystemSnapshot.values,
                                actionContacts = actionConfig.contacts.filter { it.isEnabled },
                                actionCustomActions = actionConfig.customActions,
                                onActionAppClick = { packageName ->
                                    actionIslandRepository.launchApp(packageName)
                                },
                                onActionContactClick = { phoneNumber ->
                                    PhoneActionLauncher.openCall(this@IslandOverlayService, phoneNumber)
                                },
                                onCustomActionClick = { actionId ->
                                    actionSystemController.executeCustomAction(actionId)
                                },
                                onSystemTileClick = { tile ->
                                    if (tile == ActionSystemTile.SCREENSHOT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                                    } else {
                                        actionSystemController.execute(tile)
                                    }
                                },
                                notifications = notificationItems,
                                onNotificationAction = { notification, action ->
                                    if (action.isReply && NotificationActionRegistry.hasReplyTarget(notification.id, action.id)) {
                                        startActivity(
                                            Intent(this@IslandOverlayService, NotificationReplyActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .putExtra(NotificationReplyActivity.EXTRA_NOTIFICATION_ID, notification.id)
                                                .putExtra(NotificationReplyActivity.EXTRA_ACTION_ID, action.id)
                                                .putExtra(NotificationReplyActivity.EXTRA_APP_NAME, notification.appName)
                                                .putExtra(NotificationReplyActivity.EXTRA_SENDER, notification.title)
                                                .putExtra(NotificationReplyActivity.EXTRA_MESSAGE, notification.text)
                                        )
                                    } else {
                                        NotificationActionRegistry.send(notification.id, action.id)
                                    }
                                },
                                onNotificationSelect = { notificationId ->
                                    IslandStateManager.selectNotification(notificationId)
                                },
                                onNotificationDismiss = { notificationId ->
                                    IslandStateManager.removeNotification(notificationId)
                                    NotificationActionRegistry.remove(notificationId)
                                },
                                callContact = callContact,
                                callSummary = callSummary,
                                callDurationSeconds = callDurationSeconds,
                                showCallDuration = showCallDuration,
                                callIsDialing = callIsDialing,
                                musicTrack = musicState?.track,
                                musicIsPlaying = musicState?.isPlaying ?: true,
                                musicScrubberEnabled = musicScrubberEnabled,
                                pulseScale = if (isProActive) pulseScale else 1.15f,
                                pulseDurationMillis = if (isProActive) pulseDuration else 450,
                                hapticEnabled = if (isProActive) hapticFeedbackEnabled else true,
                                fastAnimations = !animationSpeedNormal,
                                onMusicSeek = { progress ->
                                    musicState?.track?.let { track ->
                                        if (track.durationMs > 0L) {
                                            MediaPlaybackRegistry.seekTo(
                                                (track.durationMs * progress.coerceIn(0f, 1f)).toLong()
                                            )
                                        }
                                    }
                                },
                                timerLabel = timerActivity?.subtitle ?: "03:35",
                                timerProgress = timerActivity?.progress ?: 0.72f,
                                timerRunning = timerActivity?.isRunning ?: true,
                                premiumHoursRemaining = premiumExpiryState?.hoursRemaining ?: 12,
                                liveActivityTitle = liveActivity?.title.orEmpty(),
                                liveActivitySubtitle = liveActivity?.subtitle.orEmpty(),
                                liveActivityProgress = liveActivity?.progress,
                                ringerMode = (renderedState as? IslandState.RingerMode)?.mode
                                    ?: ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT,
                                ringerVolumeLevel = ringerVolumeState?.volumeLevel ?: 0.5f,
                                brightnessLevel = brightnessLevel,
                                mediaVolumeLevel = mediaVolumeLevel,
                                mediaVolumeHudLevel = mediaVolumeState?.volumeLevel ?: mediaVolumeLevel,
                                bluetoothDeviceName = bluetoothState?.deviceName ?: "Bluetooth device",
                                bluetoothBatteryPercent = bluetoothState?.batteryPercent,
                                bluetoothIsConnecting = bluetoothState?.isConnecting == true,
                                batteryPercent = batteryState?.batteryPercent ?: 85,
                                onBrightnessChange = { value ->
                                    brightnessLevel = value
                                    actionSystemController.setBrightness(value)
                                },
                                onMediaVolumeChange = { value ->
                                    mediaVolumeLevel = value
                                    actionSystemController.setMediaVolume(value)
                                },
                                actionNotificationCount = actionNotificationCount,
                                onUtilityAction = { utility ->
                                    when (utility) {
                                        ActionUtilityAction.LOCK -> {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                                            } else {
                                                actionSystemController.openSettings()
                                            }
                                        }
                                        ActionUtilityAction.SETTINGS -> actionSystemController.openSettings()
                                        ActionUtilityAction.CAMERA -> startActivity(
                                            Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                        ActionUtilityAction.EDIT -> startActivity(
                                            Intent(applicationContext, MainActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .putExtra(MainActivity.EXTRA_OPEN_ACTION_ISLAND_EDITOR, true)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        try {
            windowManager.addView(composeView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeWindow() {
        composeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        composeView = null
        windowLayoutParams = null
    }

    private fun mapToDemoState(state: IslandState): IslandDemoState {
        return when (state) {
            is IslandState.Hidden, is IslandState.Minimal -> IslandDemoState.Minimal
            is IslandState.Music -> if (state.isExpanded) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
            is IslandState.OngoingCall -> if (state.isExpanded) IslandDemoState.CallExpanded else IslandDemoState.CallCompact
            is IslandState.CallSummary -> IslandDemoState.CallSummaryExpanded
            is IslandState.IncomingCall -> IslandDemoState.CallAvatars
            is IslandState.Notification -> when {
                state.isExpanded -> IslandDemoState.NotificationExpanded
                state.notifications.size > 1 -> IslandDemoState.NotificationStacked
                else -> IslandDemoState.NotificationCompact
            }
            is IslandState.NotificationWithMusic -> IslandDemoState.NotificationWithMusicCompact
            is IslandState.Charging -> if (state.batteryPercent < 20) IslandDemoState.LowBatteryCompact else IslandDemoState.ChargingCompact
            is IslandState.RingerMode -> if (state.isExpanded) {
                IslandDemoState.RingerModeExpanded
            } else when (state.mode) {
                ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT -> IslandDemoState.SilentModeCompact
                ai.emots.kishan_dynamic.data.model.RingerModeType.VIBRATE -> IslandDemoState.VibrateModeCompact
                ai.emots.kishan_dynamic.data.model.RingerModeType.NORMAL -> IslandDemoState.NormalModeCompact
            }
            is IslandState.RingerVolume -> IslandDemoState.RingerVolumeCompact
            is IslandState.MediaVolume -> IslandDemoState.MediaVolumeCompact
            is IslandState.BluetoothDevice -> if (state.isConnecting) {
                IslandDemoState.BluetoothConnecting
            } else {
                IslandDemoState.BluetoothConnected
            }
            is IslandState.ActionControl -> IslandDemoState.ActionControlExpanded
            is IslandState.PremiumExpiry -> IslandDemoState.PremiumExpiryExpanded
            is IslandState.LiveActivity -> state.activity.toDemoState()
        }
    }

    private fun ai.emots.kishan_dynamic.data.model.LiveActivityInfo.toDemoState(): IslandDemoState {
        return when (kind) {
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER -> if (isExpanded) IslandDemoState.TimerExpanded else IslandDemoState.TimerCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.DELIVERY -> if (isExpanded) IslandDemoState.DeliveryExpanded else IslandDemoState.DeliveryCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FLIGHT -> if (isExpanded) IslandDemoState.FlightExpanded else IslandDemoState.FlightCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SPORTS -> if (isExpanded) IslandDemoState.SportsExpanded else IslandDemoState.SportsCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.NAVIGATION -> if (isExpanded) IslandDemoState.ColorOptions else IslandDemoState.NavigationCompact
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VOICE_MEMO -> IslandDemoState.VoiceMemoExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_RECORDING -> IslandDemoState.ScreenRecordingExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SHORTCUT -> IslandDemoState.ShortcutExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FOCUS_MODE -> IslandDemoState.FocusModeExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIR_DROP -> IslandDemoState.AirDropActivity
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPODS -> IslandDemoState.AirPodsConnected
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SATELLITE -> IslandDemoState.SatelliteConnected
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.FIND_MY -> IslandDemoState.FindMyAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOVED_TO_IPHONE -> IslandDemoState.MovedToIPhone
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.VIDEO_REMOTE -> IslandDemoState.VideoRemoteExpanded
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.AIRPLANE_ALERT -> IslandDemoState.AirplaneAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.SCREEN_MIRRORING_ALERT -> IslandDemoState.ScreenMirroringAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.MOBILE_DATA_ALERT -> IslandDemoState.MobileDataAlert
            ai.emots.kishan_dynamic.data.model.LiveActivityKind.TRANSIT_ROUTE_ALERT -> IslandDemoState.TransitRouteAlert
        }
    }

    private fun handleIslandControlAction(action: IslandControlAction) {
        when (action) {
            IslandControlAction.MusicPlayPause -> MediaPlaybackRegistry.playPause()
            IslandControlAction.MusicNext -> MediaPlaybackRegistry.skipNext()
            IslandControlAction.MusicPrevious -> MediaPlaybackRegistry.skipPrevious()
            IslandControlAction.MusicAirPlay -> actionSystemController.openAudioOutputSettings()
            IslandControlAction.VideoSkipBackward -> MediaPlaybackRegistry.seekBy(-15_000L)
            IslandControlAction.VideoPlayPause -> MediaPlaybackRegistry.playPause()
            IslandControlAction.VideoSkipForward -> MediaPlaybackRegistry.seekBy(15_000L)
            IslandControlAction.OpenSettings -> actionSystemController.openSettings()
            IslandControlAction.MobileDataSettings -> actionSystemController.execute(ActionSystemTile.MOBILE_DATA)
            IslandControlAction.CallAccept -> {
                val incoming = IslandStateManager.currentState.value as? IslandState.IncomingCall
                if (incoming?.sourceNotificationId != null && incoming.acceptActionId != null) {
                    if (NotificationActionRegistry.send(incoming.sourceNotificationId, incoming.acceptActionId)) {
                        IslandStateManager.resolveNotificationIncomingCall()
                    }
                } else if (callSystemController.acceptIncomingCall()) {
                    IslandStateManager.acceptIncomingCall()
                }
            }
            IslandControlAction.CallDecline,
            IslandControlAction.CallEnd -> {
                val incoming = IslandStateManager.currentState.value as? IslandState.IncomingCall
                val ongoing = IslandStateManager.currentState.value as? IslandState.OngoingCall
                if (incoming?.sourceNotificationId != null) {
                    val sent = incoming.declineActionId?.let { actionId ->
                        NotificationActionRegistry.send(incoming.sourceNotificationId, actionId)
                    } ?: false
                    if (sent || incoming.declineActionId == null) {
                        IslandStateManager.resolveNotificationIncomingCall()
                    }
                } else if (ongoing?.sourceNotificationId != null && ongoing.endActionId != null) {
                    if (NotificationActionRegistry.send(ongoing.sourceNotificationId, ongoing.endActionId)) {
                        IslandStateManager.resolveNotificationOngoingCall()
                    }
                } else if (callSystemController.endActiveCall()) {
                    IslandStateManager.endCall()
                }
            }
            IslandControlAction.CallSummaryRedial -> {
                (IslandStateManager.currentState.value as? IslandState.CallSummary)?.record?.phoneNumber?.let { number ->
                    PhoneActionLauncher.openCall(this, number)
                }
                IslandStateManager.clearCallSummary()
            }
            IslandControlAction.CallSummaryMessage -> {
                (IslandStateManager.currentState.value as? IslandState.CallSummary)?.record?.phoneNumber?.let { number ->
                    startActivity(
                        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(number)}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                IslandStateManager.clearCallSummary()
            }
            IslandControlAction.CallSummaryDismiss -> IslandStateManager.clearCallSummary()
            IslandControlAction.CallToggleMute -> callSystemController.toggleMute()
            IslandControlAction.CallToggleSpeaker -> callSystemController.toggleSpeaker()
            IslandControlAction.SilentToggle -> {
                val currentMode = (IslandStateManager.currentState.value as? IslandState.RingerMode)?.mode
                val nextMode = if (currentMode == ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT) {
                    AudioManager.RINGER_MODE_NORMAL
                } else {
                    AudioManager.RINGER_MODE_SILENT
                }
                runCatching {
                    (getSystemService(AUDIO_SERVICE) as AudioManager).ringerMode = nextMode
                }
            }
            IslandControlAction.TimerToggle -> IslandStateManager.toggleTimer()
            IslandControlAction.TimerCancel -> IslandStateManager.cancelTimer()
            IslandControlAction.ScreenMirroringStop,
            IslandControlAction.MobileDataOk,
            IslandControlAction.TransitEndRoute,
            IslandControlAction.VoiceMemoStop,
            IslandControlAction.ScreenRecordingStop,
            IslandControlAction.AirDropPause,
            IslandControlAction.MovedUndo -> {
                val activity = (IslandStateManager.currentState.value as? IslandState.LiveActivity)?.activity
                val dispatched = activity?.sourceNotificationId != null &&
                    activity.sourceActionId != null &&
                    NotificationActionRegistry.send(
                        activity.sourceNotificationId,
                        activity.sourceActionId
                    )
                if (dispatched || activity != null) {
                    activity?.let { IslandStateManager.clearLiveActivity(it.id) }
                }
            }
            IslandControlAction.LiveActivityOpen -> {
                val activity = (IslandStateManager.currentState.value as? IslandState.LiveActivity)?.activity
                val sourceId = activity?.sourceNotificationId
                val actionId = activity?.sourceActionId
                if (sourceId != null && actionId != null &&
                    NotificationActionRegistry.send(sourceId, actionId)
                ) {
                    IslandStateManager.clearLiveActivity(activity.id)
                }
            }
            IslandControlAction.AirPodsOpen -> actionSystemController.execute(ActionSystemTile.BLUETOOTH)
            IslandControlAction.SatelliteMessage -> actionSystemController.openSettings()
            IslandControlAction.ShortcutOpen -> openMainActivity(MainActivity.EXTRA_OPEN_LIVE_ACTIVITY)
            IslandControlAction.PremiumWatchAd -> openMainActivity(MainActivity.EXTRA_OPEN_REWARDED_PASS)
            IslandControlAction.PremiumBuy -> openMainActivity(MainActivity.EXTRA_OPEN_PREMIUM)
            else -> Unit
        }
    }

    private fun openMainActivity(extra: String) {
        runCatching {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra(extra, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }

    private fun observePreferences() {
        // Lifecycle dynamic window attach/detach
        scope.launch {
            combine(
                preferences.islandEnabled,
                preferences.alwaysOnTop,
                preferences.showOnLockScreen,
                deviceLocked
            ) { enabled, alwaysOnTop, showOnLock, locked ->
                OverlayDisplayPolicy(
                    islandEnabled = enabled,
                    alwaysOnTop = alwaysOnTop,
                    showOnLockScreen = showOnLock,
                    deviceLocked = locked
                )
            }.collectLatest { policy ->
                val shouldAttach = policy.islandEnabled && (!policy.deviceLocked || policy.showOnLockScreen)
                if (shouldAttach) {
                    if (composeView == null) {
                        initializeWindow()
                    }
                } else {
                    if (composeView != null) {
                        removeWindow()
                    }
                }
            }
        }

        // Live vertical offset
        scope.launch {
            preferences.verticalOffset.collectLatest { offset ->
                windowLayoutParams?.let { params ->
                    params.y = offset
                    composeView?.let { view ->
                        runCatching { windowManager.updateViewLayout(view, params) }
                    }
                }
            }
        }

        // Live horizontal offset
        scope.launch {
            preferences.horizontalOffset
                .combine(preferences.isProActive) { offset, isProActive ->
                    if (PremiumFeaturePolicy.horizontalOffsetEnabled(isProActive)) {
                        offset
                    } else {
                        AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP
                    }
                }
                .collectLatest { offset ->
                    windowLayoutParams?.let { params ->
                        params.x = offset
                        composeView?.let { view ->
                            runCatching { windowManager.updateViewLayout(view, params) }
                        }
                    }
                }
        }

        // Dynamic window width for expanded vs compact states
        scope.launch {
            IslandStateManager.currentState.collectLatest { state ->
                windowLayoutParams?.let { params ->
                    val displayMetrics = resources.displayMetrics
                    val screenWidthPx = displayMetrics.widthPixels
                    val density = displayMetrics.density
                    val expandedWidthPx = (screenWidthPx - (16 * density)).toInt().coerceAtLeast(0)

                    val isExpanded = when (state) {
                        is IslandState.ActionControl -> true
                        is IslandState.CallSummary -> true
                        is IslandState.IncomingCall -> true
                        is IslandState.PremiumExpiry -> true
                        is IslandState.Music -> state.isExpanded
                        is IslandState.OngoingCall -> state.isExpanded
                        is IslandState.Notification -> state.isExpanded
                        is IslandState.RingerMode -> state.isExpanded
                        is IslandState.LiveActivity -> state.activity.isExpanded
                        else -> false
                    }

                    val targetWidthPx = if (isExpanded) {
                        expandedWidthPx
                    } else {
                        WindowManager.LayoutParams.WRAP_CONTENT
                    }

                    if (params.width != targetWidthPx) {
                        params.width = targetWidthPx
                        composeView?.let { view ->
                            runCatching { windowManager.updateViewLayout(view, params) }
                        }
                    }
                }
            }
        }

        scope.launch {
            preferences.proExpiresAt
                .combine(preferences.isProActive) { expiresAt, active ->
                    if (!active || expiresAt == null) null
                    else PremiumExpiryPolicy.remainingWarningHours(
                        expiresAtMillis = expiresAt,
                        nowMillis = System.currentTimeMillis()
                    )
                }
                .collectLatest { hoursRemaining ->
                    if (hoursRemaining != null) {
                        IslandStateManager.postPremiumExpiry(hoursRemaining)
                    }
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can be used for lock screen detection or window state changes
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceActuallyConnected = false
        unregisterReceiver(systemIslandReceiver)
        if (::audioVolumeObserver.isInitialized) audioVolumeObserver.unregister()
        if (::actionSystemController.isInitialized) actionSystemController.close()
        if (::systemLiveActivityMonitor.isInitialized) systemLiveActivityMonitor.close()
        if (::bluetoothAccessoryMonitor.isInitialized) bluetoothAccessoryMonitor.close()
        if (::mediaRouteActivityMonitor.isInitialized) mediaRouteActivityMonitor.close()
        if (::satelliteActivityMonitor.isInitialized) satelliteActivityMonitor.close()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        removeWindow()
    }
}
