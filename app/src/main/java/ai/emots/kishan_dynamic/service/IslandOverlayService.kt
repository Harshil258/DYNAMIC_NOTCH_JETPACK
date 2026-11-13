package ai.emots.kishan_dynamic.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.bluetooth.BluetoothAdapter
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
import android.net.wifi.WifiManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import ai.emots.kishan_dynamic.data.model.IslandPresentationMode
import ai.emots.kishan_dynamic.data.model.presentationMode
import ai.emots.kishan_dynamic.data.model.IslandInteractionAction
import ai.emots.kishan_dynamic.data.model.IslandInteractionPolicy
import ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy
import ai.emots.kishan_dynamic.data.model.OverlayDisplayPolicy
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.EntitlementManager
import ai.emots.kishan_dynamic.data.premium.PremiumExpiryPolicy
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.data.release.AppReleaseRuntimeConfig
import ai.emots.kishan_dynamic.data.repository.ActionIslandRepository
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionUtilityAction
import ai.emots.kishan_dynamic.MainActivity
import ai.emots.kishan_dynamic.ui.NotificationReplyActivity
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.IslandControlAction
import ai.emots.kishan_dynamic.data.analytics.AnalyticsEventNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

/**
 * System Accessibility Overlay Service that renders the Dynamic Island on top of all Android windows.
 */
class IslandOverlayService : AccessibilityService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val analyticsTracker: AnalyticsTracker by lazy { FirebaseAnalyticsTracker(applicationContext) }
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var calibrationYdp = AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP
    private var calibrationXdp = AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP
    private var hardwareCutouts = emptyList<ai.emots.kishan_dynamic.data.model.IslandCutout>()
    private var statusBarBottomPx = 0

    private fun applyMeasuredWindowPlacement() {
        val view = composeView ?: return
        val params = windowLayoutParams ?: return
        if (view.width <= 0 || view.height <= 0 || params.width == 0) return
        val metrics = resources.displayMetrics
        val density = metrics.density
        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) windowManager.currentWindowMetrics.bounds else null
        val placement = ai.emots.kishan_dynamic.data.model.IslandPlacementPolicy.resolve(
            screenWidth = bounds?.width() ?: metrics.widthPixels,
            screenHeight = bounds?.height() ?: metrics.heightPixels,
            bodyWidth = view.width, bodyHeight = view.height,
            requestedX = (calibrationXdp * density).toInt(),
            requestedY = (calibrationYdp * density).toInt(),
            expanded = IslandStateManager.currentState.value.presentationMode == IslandPresentationMode.EXPANDED,
            statusBarBottom = statusBarBottomPx,
            edge = (islandLayoutTokens.expandedSideMargin.value * density).toInt(),
            clearance = (islandLayoutTokens.cameraClearance.value * density).toInt(),
            cutouts = hardwareCutouts
        )
        if (params.x != placement.centerOffsetX || params.y != placement.top) {
            params.x = placement.centerOffsetX
            params.y = placement.top
            if (view.isAttachedToWindow) runCatching { windowManager.updateViewLayout(view, params) }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        hardwareCutouts = emptyList()
        statusBarBottomPx = 0
        composeView?.requestApplyInsets()
        composeView?.post { applyMeasuredWindowPlacement() }
    }

    private val islandLayoutTokens = ai.emots.kishan_dynamic.ui.theme.AppIslandTokens()
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
    private var replyingNotificationId by mutableStateOf<String?>(null)
    private val systemSurfaceSuppressed = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val isIdleAutoHideActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    private var replyingAction by mutableStateOf<ai.emots.kishan_dynamic.data.model.NotificationActionInfo?>(null)

    private fun enableOverlayFocusable(): Boolean {
        return windowLayoutParams?.let { params ->
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            composeView?.let { view ->
                runCatching {
                    windowManager.updateViewLayout(view, params)
                    true
                }.getOrDefault(false)
            } ?: false
        } ?: false
    }

    private fun restoreOverlayNonFocusable() {
        windowLayoutParams?.let { params ->
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            composeView?.let { view ->
                runCatching { windowManager.updateViewLayout(view, params) }
            }
        }
    }

    private val deviceLocked = MutableStateFlow(false)
    private val systemIslandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    IslandStateManager.onDeviceLocked()
                    deviceLocked.value = true
                }
                Intent.ACTION_SCREEN_ON -> syncDeviceLockState()
                Intent.ACTION_USER_PRESENT -> {
                    deviceLocked.value = false
                    IslandStateManager.onDeviceUnlocked()
                }
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
                    if (::bluetoothAccessoryMonitor.isInitialized) {
                        bluetoothAccessoryMonitor.onBluetoothEvent()
                    }
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
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (::bluetoothAccessoryMonitor.isInitialized) {
                        bluetoothAccessoryMonitor.onBluetoothEvent()
                    }
                    if (::actionSystemController.isInitialized) {
                        actionSystemController.refresh()
                    }
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    if (::actionSystemController.isInitialized) {
                        actionSystemController.refresh()
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    IslandStateManager.clearBluetoothDevice()
                    if (::bluetoothAccessoryMonitor.isInitialized) {
                        bluetoothAccessoryMonitor.onBluetoothEvent()
                    }
                }
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
        analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_ACTIVATED)
        PermissionUtils.sendPermissionsChangedBroadcast(this)

        scope.launch {
            if (PermissionUtils.isNotificationListenerEnabled(applicationContext)) {
                preferences.setIslandEnabled(true)
                preferences.setSetupDone(true)
            }
        }

        if (PermissionUtils.isAwaitingAccessibilityReturn) {
            PermissionUtils.isAwaitingAccessibilityReturn = false
            try {
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("permission_auto_resumed", true)
                }
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to launch MainActivity onServiceConnected", e)
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceActuallyConnected = false
        PermissionUtils.sendPermissionsChangedBroadcast(this)
        removeWindow()
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
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
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

        val initialVerticalOffset = AuroraPreferences.DEFAULT_VERTICAL_OFFSET_DP
        val initialHorizontalOffset = AuroraPreferences.DEFAULT_HORIZONTAL_OFFSET_DP

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
            x = (initialHorizontalOffset * resources.displayMetrics.density).toInt()
            y = (initialVerticalOffset * resources.displayMetrics.density).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        windowLayoutParams = layoutParams

        composeView = ComposeView(this).apply {
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Overlay insets are window-relative and shrink when we move below
                    // the status bar. Use display metrics so placement cannot feed back
                    // into its own inset calculation and oscillate vertically.
                    val displayInsets = windowManager.maximumWindowMetrics.windowInsets
                    statusBarBottomPx = displayInsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.statusBars()).top
                    hardwareCutouts = displayInsets.displayCutout?.boundingRects.orEmpty().map {
                        ai.emots.kishan_dynamic.data.model.IslandCutout(it.left, it.top, it.right, it.bottom)
                    }
                } else {
                    val location = IntArray(2)
                    view.getLocationOnScreen(location)
                    @Suppress("DEPRECATION")
                    val topInset = insets.systemWindowInsetTop
                    if (topInset > 0) statusBarBottomPx = location[1] + topInset
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val rects = insets.displayCutout?.boundingRects.orEmpty()
                        if (rects.isNotEmpty()) hardwareCutouts = rects.map {
                            ai.emots.kishan_dynamic.data.model.IslandCutout(
                                it.left + location[0], it.top + location[1],
                                it.right + location[0], it.bottom + location[1])
                        }
                    }
                }
                post { applyMeasuredWindowPlacement() }
                insets
            }
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyMeasuredWindowPlacement() }
            setViewTreeLifecycleOwner(this@IslandOverlayService)
            setViewTreeViewModelStoreOwner(this@IslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@IslandOverlayService)

            setOnTouchListener { _, event ->
                isIdleAutoHideActive.value = false
                if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                    if (replyingNotificationId != null) {
                        replyingNotificationId = null
                        replyingAction = null
                        restoreOverlayNonFocusable()
                    }
                    IslandStateManager.collapseExpandedState()
                    true
                } else {
                    false
                }
            }

            setContent {
                val islandState by IslandStateManager.currentState.collectAsState()
                val childBehavior by IslandStateManager.behaviorPresentation.collectAsState()
                val mediaCapabilitySnapshot by MediaPlaybackRegistry.snapshot.collectAsState()
                val unseenNotificationCount by IslandStateManager.unseenNotificationCount.collectAsState()
                val isEnabled by preferences.islandEnabled.collectAsState(initial = true)
                val alwaysOnTop by preferences.alwaysOnTop.collectAsState(initial = true)
                val showOnLockScreen by preferences.showOnLockScreen.collectAsState(initial = true)
                val hideSensitiveContent by preferences.hideSensitiveContent.collectAsState(initial = true)
                val animationSpeedNormal by preferences.animationSpeedNormal.collectAsState(initial = true)
                val reduceMotion by preferences.reduceMotion.collectAsState(initial = false)
                val compactMusicControls by preferences.compactMusicControls.collectAsState(initial = false)
                val isProActive by preferences.isProActive.collectAsState(initial = false)
                val isDeviceLocked by deviceLocked.collectAsState()
                LaunchedEffect(isDeviceLocked, hideSensitiveContent) {
                    IslandStateManager.dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PrivacyChanged(
                        ai.emots.kishan_dynamic.data.notification.ChildPrivacyPolicy(isDeviceLocked, hideSensitiveContent)))
                }
                LaunchedEffect(replyingNotificationId) {
                    if (replyingNotificationId != null) IslandStateManager.dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.StartReply)
                    else if (IslandStateManager.behaviorState.ownership == ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.REPLY_ACTIVE)
                        IslandStateManager.dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.ExitReply)
                }
                val widthScale by preferences.widthScale.collectAsState(
                    initial = AuroraPreferences.DEFAULT_WIDTH_SCALE
                )
                val swipeUpDismiss by preferences.swipeUpDismiss.collectAsState(initial = true)
                val islandDismissCancelsOs by preferences.islandDismissCancelsOs.collectAsState(initial = false)
                val musicScrubberEnabled by preferences.musicScrubberEnabled.collectAsState(initial = true)
                val pulseScale by preferences.pulseScale.collectAsState(initial = 1.15f)
                val pulseDuration by preferences.pulseDuration.collectAsState(initial = 450)
                val hapticFeedbackEnabled by preferences.hapticFeedbackEnabled.collectAsState(initial = true)
                val actionConfig by actionIslandRepository.config.collectAsState(
                    initial = ai.emots.kishan_dynamic.data.model.ActionIslandConfig()
                )
                val actionSystemSnapshot by actionSystemController.snapshot.collectAsState()
                val observedMediaVolume by IslandStateManager.observedMediaVolume.collectAsState()

                val notchFlatTop by preferences.notchFlatTop.collectAsState(initial = false)
                val notchBgArgb by preferences.notchBackgroundArgb.collectAsState(initial = 0xFF000000.toInt())
                val notchFgArgb by preferences.notchForegroundArgb.collectAsState(initial = 0xFFFFFFFF.toInt())
                val idleAutoHideEnabled by preferences.idleAutoHideEnabled.collectAsState(initial = false)
                val idleAutoHideTimeoutSeconds by preferences.idleAutoHideTimeoutSeconds.collectAsState(initial = 10)
                val cameraCutoutMode by preferences.cameraCutoutMode.collectAsState(initial = false)
                val cutoutSizePx by preferences.cutoutSizePx.collectAsState(initial = 80)
                val quickControlsEnabled by preferences.quickControlsEnabled.collectAsState(initial = true)
                val isIdleAutoHide by isIdleAutoHideActive.collectAsState()

                val systemSurfaceActive by systemSurfaceSuppressed.collectAsState()
                val isIdleState = islandState is IslandState.Minimal || islandState is IslandState.Hidden || isIdleAutoHide
                val isHiddenState = islandState is IslandState.Hidden || isIdleAutoHide
                // System controls always win; source state continues independently.
                androidx.compose.animation.AnimatedVisibility(
                    visible = OverlayDisplayPolicy(
                        islandEnabled = isEnabled,
                        alwaysOnTop = alwaysOnTop,
                        showOnLockScreen = showOnLockScreen,
                        deviceLocked = isDeviceLocked,
                        systemSurfaceActive = systemSurfaceActive
                    ).shouldRender(isHiddenState = isHiddenState, isIdleState = isIdleState),
                    enter = ai.emots.kishan_dynamic.ui.components.islandVisibilityEnter(reduceMotion),
                    exit = ai.emots.kishan_dynamic.ui.components.islandVisibilityExit(reduceMotion)
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion provides reduceMotion
                    ) {
                        AppTheme(darkTheme = true) {
                        var brightnessLevel by remember {
                            mutableFloatStateOf(actionSystemController.readBrightness())
                        }
                        var mediaVolumeLevel by remember {
                            mutableFloatStateOf(actionSystemController.readMediaVolume())
                        }
                        LaunchedEffect(observedMediaVolume) {
                            observedMediaVolume?.let { mediaVolumeLevel = it }
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
                                mapToDemoState(renderedState, unseenNotificationCount)
                            }
                            val sourceCallContact = when (renderedState) {
                                is IslandState.IncomingCall -> renderedState.contact
                                is IslandState.OngoingCall -> renderedState.contact
                                else -> null
                            }
                            val callContact = LockScreenPrivacyPolicy.contact(
                                source = sourceCallContact,
                                deviceLocked = isDeviceLocked,
                                hideSensitiveContent = hideSensitiveContent,
                                incoming = renderedState is IslandState.IncomingCall
                            )
                            val callSummary = LockScreenPrivacyPolicy.callRecord(
                                source = (renderedState as? IslandState.CallSummary)?.record,
                                deviceLocked = isDeviceLocked,
                                hideSensitiveContent = hideSensitiveContent
                            )
                            val callDurationSeconds = (renderedState as? IslandState.OngoingCall)?.durationSeconds ?: 0L
                            val showCallDuration = (renderedState as? IslandState.OngoingCall)?.showDuration ?: true
                            val callIsDialing = (renderedState as? IslandState.OngoingCall)?.isDialing == true
                            val callCanDecline = (renderedState as? IslandState.IncomingCall)?.let { incoming ->
                                incoming.sourceNotificationId == null || incoming.declineActionId != null
                            } ?: true
                            val musicState = renderedState as? IslandState.Music
                            val playbackMask = if (musicState?.track?.packageName == mediaCapabilitySnapshot?.packageName)
                                mediaCapabilitySnapshot?.supportedActions ?: 0L else 0L
                            fun supportsMedia(mask: Long) = playbackMask and mask != 0L
                            val availableControls = IslandControlAction.entries.toMutableSet().apply {
                                if (!supportsMedia(android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS)) remove(IslandControlAction.MusicPrevious)
                                if (!supportsMedia(android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT)) remove(IslandControlAction.MusicNext)
                                val toggle = android.media.session.PlaybackState.ACTION_PLAY_PAUSE or
                                    if (musicState?.isPlaying == true) android.media.session.PlaybackState.ACTION_PAUSE else android.media.session.PlaybackState.ACTION_PLAY
                                if (!supportsMedia(toggle)) remove(IslandControlAction.MusicPlayPause)
                                // No Android source bridge exists for the Apple video/share controls.
                                removeAll(setOf(IslandControlAction.CallToggleVideo, IslandControlAction.CallToggleSharePlay,
                                    IslandControlAction.VideoSkipBackward, IslandControlAction.VideoPlayPause, IslandControlAction.VideoSkipForward))
                                val incoming = renderedState as? IslandState.IncomingCall
                                val ongoing = renderedState as? IslandState.OngoingCall
                                if (incoming == null || (incoming.acceptActionId == null && !callSystemController.canControlCalls())) remove(IslandControlAction.CallAccept)
                                if (incoming == null || (incoming.declineActionId == null && !callSystemController.canControlCalls())) remove(IslandControlAction.CallDecline)
                                if (ongoing == null || (ongoing.endActionId == null && !callSystemController.canControlCalls())) remove(IslandControlAction.CallEnd)
                                if (!(getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted)
                                    remove(IslandControlAction.SilentToggle)
                            }
                            val timerActivity = (renderedState as? IslandState.LiveActivity)?.activity?.takeIf {
                                it.kind == ai.emots.kishan_dynamic.data.model.LiveActivityKind.TIMER
                            }
                            val liveActivity = (renderedState as? IslandState.LiveActivity)?.activity
                            val liveActivityActionLabel = liveActivity?.sourceActionId?.let { actionId ->
                                liveActivity.sourceActions.firstOrNull { it.id == actionId }?.label
                                    ?: "Open".takeIf { actionId == "open" }
                            }.orEmpty()
                            val actionNotificationCount = (renderedState as? IslandState.ActionControl)?.notificationCount ?: 0
                            val sourceNotificationItems = when (renderedState) {
                                is IslandState.Notification -> renderedState.notifications
                                is IslandState.NotificationWithMusic -> renderedState.notifications
                                else -> emptyList()
                            }
                            val notificationItems = sourceNotificationItems.map { notification ->
                                LockScreenPrivacyPolicy.notification(
                                    source = notification,
                                    deviceLocked = isDeviceLocked,
                                    hideSensitiveContent = hideSensitiveContent
                                )
                            }
                            // Validate against surviving canonical sources while a call temporarily owns the surface.
                            val replyValidationItems = childBehavior.queue.notifications.map { source ->
                                LockScreenPrivacyPolicy.notification(source, isDeviceLocked, hideSensitiveContent)
                            }
                            val callOwnsPresentation = renderedState is IslandState.IncomingCall || renderedState is IslandState.OngoingCall
                            LaunchedEffect(callOwnsPresentation, replyingNotificationId) {
                                if (callOwnsPresentation) restoreOverlayNonFocusable()
                                else if (replyingNotificationId != null) enableOverlayFocusable()
                            }
                            LaunchedEffect(replyValidationItems, replyingNotificationId, replyingAction) {
                                if (replyingNotificationId != null &&
                                    !ai.emots.kishan_dynamic.data.notification.NotificationReplyLifecyclePolicy.canKeepEditor(
                                        replyingNotificationId, replyingAction?.id, replyValidationItems
                                    )) {
                                    replyingNotificationId?.let { ai.emots.kishan_dynamic.data.notification.ReplyDraftMemory.store.clear(it) }
                                    replyingNotificationId = null
                                    replyingAction = null
                                    restoreOverlayNonFocusable()
                                }
                            }
                            val notificationActiveIndex = when (renderedState) {
                                is IslandState.Notification -> renderedState.activeIndex
                                is IslandState.NotificationWithMusic -> renderedState.activeIndex
                                else -> 0
                            }
                            val ringerVolumeState = renderedState as? IslandState.RingerVolume
                            val mediaVolumeState = renderedState as? IslandState.MediaVolume
                            val bluetoothState = renderedState as? IslandState.BluetoothDevice
                            val batteryState = renderedState as? IslandState.Charging
                            val premiumExpiryState = renderedState as? IslandState.PremiumExpiry
                            ai.emots.kishan_dynamic.ui.components.LayeredMiniIslands(
                                state = childBehavior.copy(childPrivacy = ai.emots.kishan_dynamic.data.notification.ChildPrivacyPolicy(isDeviceLocked, hideSensitiveContent)),
                                redacted = isDeviceLocked && hideSensitiveContent,
                                enabled = (renderedState as? IslandState.Notification)?.isExpanded == true && replyingNotificationId == null,
                                onEvent = { IslandStateManager.dispatchBehaviorEvent(it) },
                                onPromote = IslandStateManager::promoteChild
                            ) {
                            DynamicIslandPill(
                                state = demoState,
                                availableControlActions = availableControls,
                                readablePresentationToken = childBehavior.activeToken.takeIf { childBehavior.ownership == ai.emots.kishan_dynamic.data.notification.IslandInteractionOwnership.TRANSIENT_PREVIEW },
                                onPresentationReadable = { IslandStateManager.dispatchBehaviorEvent(ai.emots.kishan_dynamic.data.notification.IslandEngineEvent.PreviewReadable(it)) },
                                horizontalScale = widthScale,
                                flatTop = notchFlatTop,
                                backgroundArgb = notchBgArgb,
                                foregroundArgb = notchFgArgb,
                                cameraCutoutMode = cameraCutoutMode,
                                cutoutSizePx = cutoutSizePx,
                                onTap = {
                                    isIdleAutoHideActive.value = false
                                    analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_EXPAND, mapOf("source" to "overlay_tap"))
                                    if (!(compactMusicControlsEnabled && islandState is IslandState.Music)) {
                                        performIslandInteraction(
                                            IslandInteractionPolicy.mainTap(islandState),
                                            actionConfig.isEnabled && quickControlsEnabled
                                        )
                                    }
                                },
                                onCompanionTap = {
                                    isIdleAutoHideActive.value = false
                                    performIslandInteraction(
                                        IslandInteractionPolicy.companionTap(islandState),
                                        actionConfig.isEnabled && quickControlsEnabled
                                    )
                                },
                                onLongPress = {
                                    isIdleAutoHideActive.value = false
                                    performIslandInteraction(
                                        IslandInteractionPolicy.mainLongPress(islandState),
                                        actionConfig.isEnabled && quickControlsEnabled
                                    )
                                },
                                onCompanionLongPress = {
                                    isIdleAutoHideActive.value = false
                                    performIslandInteraction(
                                        IslandInteractionPolicy.companionLongPress(islandState),
                                        actionConfig.isEnabled && quickControlsEnabled
                                    )
                                },
                                mainContentDescription = islandMainContentDescription(
                                    renderedState, unseenNotificationCount
                                ),
                                companionContentDescription = islandCompanionContentDescription(
                                    renderedState, unseenNotificationCount
                                ),
                                onSwipeDismiss = {
                                    analyticsTracker.logEvent(AnalyticsEventNames.ISLAND_SWIPE_DISMISS)
                                    if (swipeUpDismiss) {
                                        // U01 / PD-009 / PD-010: Non-destructive collapse of expanded card to compact; OS shade untouched
                                        IslandStateManager.collapseExpanded()
                                    }
                                },
                                onControlAction = { if (it in availableControls) handleIslandControlAction(it) },
                                onOpenNotifications = { IslandStateManager.openNotifications() },
                                actionApps = if (quickControlsEnabled) actionConfig.apps.filter { it.isEnabled } else emptyList(),
                                actionSystemTiles = if (quickControlsEnabled) actionConfig.systemTiles else emptyList(),
                                actionSystemStates = actionSystemSnapshot.values,
                                actionContacts = if (quickControlsEnabled) actionConfig.contacts.filter { it.isEnabled } else emptyList(),
                                actionCustomActions = if (quickControlsEnabled) actionConfig.customActions else emptyList(),
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
                                    analyticsTracker.logEvent(AnalyticsEventNames.QUICK_CONTROL_TAPPED, mapOf("tile" to tile.name.lowercase()))
                                    if (tile == ActionSystemTile.SCREENSHOT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                                    } else {
                                        actionSystemController.execute(tile)
                                    }
                                },
                                notifications = notificationItems,
                                notificationActiveIndex = notificationActiveIndex,
                                unseenNotificationCount = unseenNotificationCount,
                                replyingNotificationId = replyingNotificationId,
                                replyAction = replyingAction,
                                onSendReply = { replyText ->
                                    val notifId = replyingNotificationId
                                    val act = replyingAction
                                    val dispatched = if (notifId != null && act != null) {
                                        NotificationActionRegistry.sendReply(this@IslandOverlayService, notifId, act.id, replyText)
                                    } else false
                                    if (dispatched) {
                                        notifId?.let { ai.emots.kishan_dynamic.data.notification.ReplyDraftMemory.store.clear(it) }
                                        replyingNotificationId = null
                                        replyingAction = null
                                        restoreOverlayNonFocusable()
                                    }
                                    android.widget.Toast.makeText(this@IslandOverlayService,
                                        if (dispatched) "Reply handed to the app" else "Reply unavailable. Your draft is kept; open the app to reply.",
                                        android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onCancelReply = {
                                    replyingNotificationId = null
                                    replyingAction = null
                                    restoreOverlayNonFocusable()
                                },
                                onNotificationAction = { notification, action ->
                                    IslandStateManager.claimNotificationInteraction()
                                    if (action.isReply && NotificationActionRegistry.hasReplyTarget(notification.id, action.id)) {
                                        val focusGranted = enableOverlayFocusable()
                                        if (focusGranted) {
                                            replyingNotificationId = notification.id
                                            replyingAction = action
                                        } else {
                                            ai.emots.kishan_dynamic.ui.NotificationReplyActivity.launch(
                                                context = this@IslandOverlayService,
                                                notificationId = notification.id,
                                                actionId = action.id,
                                                appName = notification.appName,
                                                sender = notification.title,
                                                message = notification.text
                                            )
                                        }
                                    } else {
                                        if (!NotificationActionRegistry.send(notification.id, action.id)) {
                                            android.widget.Toast.makeText(this@IslandOverlayService,
                                                "This action is no longer available. Open the source app.",
                                                android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onNotificationSelect = { notificationId ->
                                    if (replyingNotificationId != null && replyingNotificationId != notificationId) {
                                        replyingNotificationId = null
                                        replyingAction = null
                                        restoreOverlayNonFocusable()
                                    }
                                    IslandStateManager.selectNotification(notificationId)
                                },
                                onNotificationDismiss = { notificationId ->
                                    if (replyingNotificationId == notificationId) {
                                        replyingNotificationId = null
                                        replyingAction = null
                                        restoreOverlayNonFocusable()
                                    }
                                    val notification = notificationItems.firstOrNull { it.id == notificationId }
                                    if (notification != null) {
                                        val shouldCancelOs = islandDismissCancelsOs && notification.isClearable
                                        // UX-C06 / PD-019: Default is remove from Island queue only; optional OS cancellation
                                        val osDismissRequested = IslandStateManager.onUserDismissNotification(
                                            notificationId,
                                            requestOsDismiss = shouldCancelOs
                                        )
                                        if (osDismissRequested) {
                                            IslandNotificationListener.instance?.cancelNotification(notificationId)
                                        }
                                    }
                                },
                                callContact = callContact,
                                callSummary = callSummary,
                                callDurationSeconds = callDurationSeconds,
                                showCallDuration = showCallDuration,
                                callIsDialing = callIsDialing,
                                callCanDecline = callCanDecline,
                                callIsMuted = runCatching { callSystemController.isMicrophoneMute }.getOrNull(),
                                callIsSpeaker = runCatching { callSystemController.isSpeakerphoneOn }.getOrNull(),
                                musicTrack = musicState?.track,
                                musicIsPlaying = musicState?.isPlaying ?: true,
                                musicScrubberEnabled = musicScrubberEnabled && supportsMedia(android.media.session.PlaybackState.ACTION_SEEK_TO),
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
                                liveActivityShowChronometer = liveActivity?.showChronometer == true,
                                liveActivityChronometerBase = liveActivity?.chronometerBaseElapsedRealtime ?: 0L,
                                liveActivityChronometerCountDown = liveActivity?.chronometerCountDown == true,
                                liveActivitySourceBacked = liveActivity?.sourceNotificationId != null,
                                liveActivityActionLabel = liveActivityActionLabel,
                                liveActivityInfo = liveActivity,
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
            }
        }

        try {
            windowManager.addView(composeView, layoutParams)
            composeView?.requestApplyInsets()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeWindow() {
        val view = composeView ?: return
        composeView = null
        windowLayoutParams = null
        view.setOnTouchListener(null)
        try {
            if (view.isAttachedToWindow) windowManager.removeView(view)
        } catch (_: Exception) {
            android.util.Log.w(NotificationDiagnostics.TAG, "event=window_remove outcome=failed")
        } finally {
            view.disposeComposition()
        }
    }

    private fun mapToDemoState(state: IslandState, unseenCount: Int): IslandDemoState =
        ai.emots.kishan_dynamic.ui.components.resolveOverlayDemoState(state, unseenCount)

    private fun handleIslandControlAction(action: IslandControlAction) {
        analyticsTracker.logEvent(AnalyticsEventNames.MEDIA_CONTROL_USED, mapOf("action" to action.name.lowercase()))
        when (action) {
            IslandControlAction.MusicPlayPause -> MediaPlaybackRegistry.playPause()
            IslandControlAction.MusicNext -> MediaPlaybackRegistry.skipNext()
            IslandControlAction.MusicPrevious -> MediaPlaybackRegistry.skipPrevious()
            IslandControlAction.MusicAirPlay -> actionSystemController.openAudioOutputSettings()
            IslandControlAction.VideoSkipBackward -> MediaPlaybackRegistry.seekBy(-15_000L)
            IslandControlAction.VideoPlayPause -> MediaPlaybackRegistry.playPause()
            IslandControlAction.VideoSkipForward -> MediaPlaybackRegistry.seekBy(15_000L)
            IslandControlAction.OpenSettings -> {
                actionSystemController.openSettings()
                currentLiveActivity()?.let { IslandStateManager.clearLiveActivity(it.id) }
            }
            IslandControlAction.MobileDataSettings -> {
                actionSystemController.execute(ActionSystemTile.MOBILE_DATA)
                currentLiveActivity()?.let { IslandStateManager.clearLiveActivity(it.id) }
            }
            IslandControlAction.CallAccept -> {
                val incoming = IslandStateManager.currentState.value as? IslandState.IncomingCall
                if (incoming?.sourceNotificationId != null && incoming.acceptActionId != null) {
                    if (NotificationActionRegistry.send(incoming.sourceNotificationId, incoming.acceptActionId)) {
                        IslandStateManager.resolveNotificationIncomingCall(restoreInterrupted = false)
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
                    if (sent) {
                        IslandStateManager.resolveNotificationIncomingCall()
                    }
                } else if (ongoing?.sourceNotificationId != null && ongoing.endActionId != null) {
                    if (NotificationActionRegistry.send(ongoing.sourceNotificationId, ongoing.endActionId)) {
                        IslandStateManager.resolveNotificationOngoingCall()
                    }
                } else if (callSystemController.endActiveCall()) {
                    if (incoming != null) IslandStateManager.declineIncomingCall()
                    else IslandStateManager.endCall()
                }
            }
            IslandControlAction.CallSummaryRedial -> {
                (IslandStateManager.currentState.value as? IslandState.CallSummary)?.record?.phoneNumber?.let { number ->
                    CallActionLauncher.openCall(this, number)
                }
                IslandStateManager.clearCallSummary()
            }
            IslandControlAction.CallSummaryMessage -> {
                (IslandStateManager.currentState.value as? IslandState.CallSummary)?.record?.phoneNumber?.let { number ->
                    CallActionLauncher.openSms(this, number)
                }
                IslandStateManager.clearCallSummary()
            }
            IslandControlAction.CallSummaryWhatsApp -> {
                (IslandStateManager.currentState.value as? IslandState.CallSummary)?.record?.phoneNumber?.let { number ->
                    CallActionLauncher.openWhatsApp(this, number)
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
            IslandControlAction.TimerToggle -> handleTimerToggle()
            IslandControlAction.TimerCancel -> handleLiveActivityCompletion("cancel", "stop", "end")
            IslandControlAction.ScreenMirroringStop,
            IslandControlAction.TransitEndRoute,
            IslandControlAction.VoiceMemoStop,
            IslandControlAction.ScreenRecordingStop -> {
                handleLiveActivityCompletion("stop", "end", "cancel")
            }
            IslandControlAction.AirDropPause -> handleTransferToggle()
            IslandControlAction.MovedUndo -> handleLiveActivityCompletion("undo", "revert")
            IslandControlAction.MobileDataOk -> {
                val activity = currentLiveActivity() ?: return
                IslandStateManager.clearLiveActivity(activity.id)
            }
            IslandControlAction.LiveActivityOpen -> {
                val activity = currentLiveActivity() ?: return
                sendLiveActivityAction(activity, "open", "view", "details", "continue")
            }
            IslandControlAction.LiveActivityPrimary -> {
                val activity = currentLiveActivity() ?: return
                val sourceId = activity.sourceNotificationId ?: return
                val actionId = activity.sourceActionId ?: return
                NotificationActionRegistry.send(sourceId, actionId)
            }
            IslandControlAction.AirPodsOpen -> {
                actionSystemController.execute(ActionSystemTile.BLUETOOTH)
                currentLiveActivity()?.let { IslandStateManager.clearLiveActivity(it.id) }
            }
            IslandControlAction.SatelliteMessage -> {
                actionSystemController.openSettings()
                currentLiveActivity()?.let { IslandStateManager.clearLiveActivity(it.id) }
            }
            IslandControlAction.ShortcutOpen -> openMainActivity(MainActivity.EXTRA_OPEN_LIVE_ACTIVITY)
            IslandControlAction.PremiumWatchAd -> openMainActivity(MainActivity.EXTRA_OPEN_REWARDED_PASS)
            IslandControlAction.PremiumBuy -> openMainActivity(MainActivity.EXTRA_OPEN_PREMIUM)
            else -> Unit
        }
    }

    private fun currentLiveActivity() =
        (IslandStateManager.currentState.value as? IslandState.LiveActivity)?.activity

    private fun handleTimerToggle() {
        val activity = currentLiveActivity() ?: return
        if (activity.sourceNotificationId == null) {
            IslandStateManager.toggleTimer()
            return
        }
        val keywords = if (activity.isRunning) arrayOf("pause") else arrayOf("resume", "start")
        if (sendLiveActivityAction(activity, *keywords)) {
            IslandStateManager.setTimerRunning(!activity.isRunning)
        }
    }

    private fun handleTransferToggle() {
        val activity = currentLiveActivity() ?: return
        if (activity.sourceNotificationId == null) {
            IslandStateManager.toggleLiveActivityRunning(activity.id)
            return
        }
        val keywords = if (activity.isRunning) arrayOf("pause") else arrayOf("resume", "continue")
        if (sendLiveActivityAction(activity, *keywords)) {
            IslandStateManager.toggleLiveActivityRunning(activity.id)
        }
    }

    private fun handleLiveActivityCompletion(vararg keywords: String) {
        val activity = currentLiveActivity() ?: return
        val canClear = if (activity.sourceNotificationId == null) {
            true
        } else {
            sendLiveActivityAction(activity, *keywords)
        }
        if (canClear) IslandStateManager.clearLiveActivity(activity.id)
    }

    private fun sendLiveActivityAction(
        activity: ai.emots.kishan_dynamic.data.model.LiveActivityInfo,
        vararg keywords: String
    ): Boolean {
        val sourceId = activity.sourceNotificationId ?: return false
        val matchingAction = activity.sourceActions.firstOrNull { action ->
            keywords.any { keyword -> action.label.contains(keyword, ignoreCase = true) }
        }
        val actionId = matchingAction?.id ?: "open".takeIf {
            activity.sourceHasContentIntent && keywords.any { keyword -> keyword == "open" }
        } ?: return false
        return NotificationActionRegistry.send(sourceId, actionId)
    }

    private fun performIslandInteraction(
        action: IslandInteractionAction,
        quickControlsEnabled: Boolean
    ) {
        when (action) {
            IslandInteractionAction.NONE -> Unit
            IslandInteractionAction.OPEN_QUICK_CONTROLS -> {
                val entitlement = EntitlementManager.getInstance(applicationContext).entitlement.value
                val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                val releaseConfig = AppReleaseRuntimeConfig.provider(applicationContext, isDebug).config.value
                if (entitlement.shouldShowExpiryIsland() && releaseConfig.premiumExpiryIslandEnabled) {
                    IslandStateManager.postPremiumExpiry(0)
                } else if (quickControlsEnabled) {
                    IslandStateManager.openActionControl()
                }
            }
            IslandInteractionAction.OPEN_NOTIFICATIONS -> IslandStateManager.openNotifications()
            IslandInteractionAction.OPEN_NEXT_NOTIFICATION -> {
                IslandStateManager.openNotifications(selectNext = true)
            }
            IslandInteractionAction.OPEN_MUSIC -> IslandStateManager.openMusic()
            IslandInteractionAction.TOGGLE_EXPANSION -> IslandStateManager.toggleExpansion()
        }
    }

    private fun islandMainContentDescription(state: IslandState, unseenCount: Int): String = when (state) {
        is IslandState.Minimal -> "Open Quick Controls"
        is IslandState.Notification -> {
            val notification = state.notifications.getOrNull(state.activeIndex)
                ?: state.notifications.firstOrNull()
            val position = if (state.notifications.size > 1) {
                ", ${state.activeIndex.coerceIn(0, state.notifications.lastIndex) + 1} of ${state.notifications.size}"
            } else ""
            val unseen = if (unseenCount > 0) ", $unseenCount unseen" else ""
            "Open notification from ${notification?.appName ?: "an app"}$position$unseen"
        }
        is IslandState.NotificationWithMusic -> if (state.isPlaying) {
            "Open media controls for ${state.track.title}"
        } else {
            "Open notifications"
        }
        is IslandState.Music -> "Open media controls for ${state.track.title}"
        is IslandState.OngoingCall -> "Expand call controls for ${state.contact.name}"
        is IslandState.LiveActivity -> "Expand ${state.activity.title}"
        is IslandState.RingerMode -> "Expand ringer controls"
        is IslandState.Charging -> "Battery ${state.batteryPercent} percent"
        is IslandState.RingerVolume -> "Ringer volume ${(state.volumeLevel * 100).toInt()} percent"
        is IslandState.MediaVolume -> "Media volume ${(state.volumeLevel * 100).toInt()} percent"
        is IslandState.BluetoothDevice -> "${state.deviceName} connected"
        else -> "Dynamic Island"
    }

    private fun islandCompanionContentDescription(state: IslandState, unseenCount: Int): String = when (state) {
        is IslandState.Notification -> {
            val nextIndex = if (state.notifications.isEmpty()) 0
                else (state.activeIndex.coerceIn(0, state.notifications.lastIndex) + 1) % state.notifications.size
            val next = state.notifications.getOrNull(nextIndex)
            "Open next notification${next?.appName?.let { " from $it" }.orEmpty()}"
        }
        is IslandState.NotificationWithMusic -> if (state.isPlaying) {
            if (unseenCount > 0) "Open $unseenCount unseen notifications" else "Open active notifications"
        } else {
            "Open media controls for ${state.track.title}"
        }
        is IslandState.Music -> "Open media controls for ${state.track.title}"
        is IslandState.OngoingCall -> "Expand call controls for ${state.contact.name}"
        is IslandState.LiveActivity -> "Expand ${state.activity.title}"
        is IslandState.Minimal -> "Open Quick Controls"
        else -> "Open companion activity"
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
                calibrationYdp = offset
                applyMeasuredWindowPlacement()
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
                    calibrationXdp = offset
                    applyMeasuredWindowPlacement()
                }
        }

        // Idle auto-hide timer
        scope.launch {
            kotlinx.coroutines.flow.combine(
                preferences.idleAutoHideEnabled,
                preferences.idleAutoHideTimeoutSeconds,
                IslandStateManager.currentState
            ) { enabled, timeoutSeconds, state ->
                Triple(enabled, timeoutSeconds, state)
            }.collectLatest { (enabled, timeoutSeconds, state) ->
                if (!enabled || state !is IslandState.Minimal) {
                    isIdleAutoHideActive.value = false
                } else {
                    kotlinx.coroutines.delay(timeoutSeconds * 1000L)
                    isIdleAutoHideActive.value = true
                }
            }
        }

        // Dynamic window width and touch bounds for expanded vs compact vs hidden states
        scope.launch {
            kotlinx.coroutines.flow.combine(
                kotlinx.coroutines.flow.combine(
                    IslandStateManager.currentState,
                    systemSurfaceSuppressed,
                    IslandStateManager.isPaywallActive
                ) { state, suppressed, paywall -> Triple(state, suppressed, paywall) },
                kotlinx.coroutines.flow.combine(
                    preferences.islandEnabled,
                    preferences.alwaysOnTop,
                    preferences.showOnLockScreen
                ) { enabled, alwaysOnTop, showOnLock -> Triple(enabled, alwaysOnTop, showOnLock) },
                kotlinx.coroutines.flow.combine(
                    deviceLocked,
                    isIdleAutoHideActive
                ) { locked, idleHidden -> Pair(locked, idleHidden) }
            ) { (state, suppressed, paywallActive), (enabled, alwaysOnTop, showOnLock), (locked, idleHidden) ->
                val isIdle = state is IslandState.Minimal || state is IslandState.Hidden || idleHidden
                val isHiddenState = state is IslandState.Hidden || idleHidden
                val shouldRender = OverlayDisplayPolicy(
                    islandEnabled = enabled,
                    alwaysOnTop = alwaysOnTop,
                    showOnLockScreen = showOnLock,
                    deviceLocked = locked,
                    systemSurfaceActive = suppressed
                ).shouldRender(isHiddenState = isHiddenState, isIdleState = isIdle)

                val isHidden = !shouldRender || paywallActive
                val isExpanded = state.presentationMode == ai.emots.kishan_dynamic.data.model.IslandPresentationMode.EXPANDED
                Pair(isHidden, isExpanded)
            }.collectLatest { (isHidden, _) ->
                windowLayoutParams?.let { params ->
                    val isHiddenNow = isHidden
                    // Some OEM compositors retain the last buffer of a zero-sized window.
                    // Hide the actual view as well: protected surfaces must never wait for
                    // a Compose exit animation or retain a stale visible overlay buffer.
                    composeView?.visibility = if (isHiddenNow) android.view.View.INVISIBLE else android.view.View.VISIBLE

                    val newFlags = if (isHiddenNow) {
                        params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    } else {
                        (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    }

                    val targetWidthPx = when {
                        isHiddenNow -> 0
                        else -> WindowManager.LayoutParams.WRAP_CONTENT
                    }
                    val targetHeightPx = when {
                        isHiddenNow -> 0
                        else -> WindowManager.LayoutParams.WRAP_CONTENT
                    }

                    if (params.width != targetWidthPx || params.height != targetHeightPx || params.flags != newFlags) {
                        params.flags = newFlags
                        params.width = targetWidthPx
                        params.height = targetHeightPx
                        composeView?.let { view ->
                            runCatching { windowManager.updateViewLayout(view, params) }
                            view.post { applyMeasuredWindowPlacement() }
                        }
                    }
                }
            }
        }

        scope.launch {
            EntitlementManager.getInstance(applicationContext).entitlement
                .collectLatest { snapshot ->
                    if (snapshot.isActive && snapshot.expiresAtMillis != null) {
                        val hoursRemaining = PremiumExpiryPolicy.remainingWarningHours(
                            expiresAtMillis = snapshot.expiresAtMillis,
                            nowMillis = System.currentTimeMillis()
                        )
                        if (hoursRemaining != null) {
                            IslandStateManager.postPremiumExpiry(hoursRemaining)
                        }
                    }
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentEvent = event ?: return
        if (currentEvent.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            currentEvent.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
        val currentPackage = currentEvent.packageName?.toString()
        val suppress = runCatching {
            windows.orEmpty().any { window ->
                if (!window.isFocused && !window.isActive) return@any false
                val ownOverlay = window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
                ai.emots.kishan_dynamic.data.model.OverlaySystemSurfacePolicy.shouldSuppress(
                    window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_SYSTEM,
                    currentPackage, ownOverlay
                )
            }
        }.getOrDefault(currentPackage == "com.android.systemui" || currentPackage?.endsWith(".permissioncontroller") == true)
        if (systemSurfaceSuppressed.value != suppress) {
            systemSurfaceSuppressed.value = suppress
            NotificationDiagnostics.surface(suppress)
            if (suppress) restoreOverlayNonFocusable()
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceActuallyConnected = false
        PermissionUtils.sendPermissionsChangedBroadcast(this)
        scope.cancel()
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
