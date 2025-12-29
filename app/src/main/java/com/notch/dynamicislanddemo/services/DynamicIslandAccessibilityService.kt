package com.notch.dynamicislanddemo.services

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.*
import android.graphics.PixelFormat
import android.media.session.MediaSessionManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.notch.dynamicislanddemo.models.ContactInfo
import com.notch.dynamicislanddemo.models.IslandState
import com.notch.dynamicislanddemo.models.MusicTrack
import com.notch.dynamicislanddemo.models.NotificationInfo
import com.notch.dynamicislanddemo.models.ActionParsable
import com.notch.dynamicislanddemo.ui.components.DynamicIsland
import com.notch.dynamicislanddemo.ui.components.CallAction
import com.notch.dynamicislanddemo.ui.components.MusicAction
import com.notch.dynamicislanddemo.utils.Logger
import com.notch.dynamicislanddemo.utils.Utils
import kotlinx.coroutines.*

/**
 * Modern AccessibilityService using Jetpack Compose for Dynamic Island overlay
 */
class DynamicIslandAccessibilityService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private const val TAG = "DynamicIslandService"
        private val ENABLED_ACCESSIBILITY_SERVICES = Settings.Secure
            .getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    }

    // Lifecycle management
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    // Core services
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val keyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    private val mediaSessionManager by lazy { getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferencesDataStore by lazy { com.notch.dynamicislanddemo.data.PreferencesDataStore(this) }

    // Coroutine scope
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // UI State with logging wrapper
    private var _currentState: IslandState = IslandState.Minimal
    // UI State
    private var currentState by mutableStateOf<IslandState>(IslandState.Minimal)

    // Views
    private var overlayView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    // State tracking
    private var isPhoneLocked = false
    private var isInFullScreen = false
    private var hasUserInteracted = false
    private var isCurrentlyCharging = false
    private val notifications = mutableListOf<NotificationInfo>()

    // Broadcast receivers
    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.d(TAG, "SystemReceiver: Received action=${intent?.action}")
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> handleBatteryChanged(intent)
                Intent.ACTION_SCREEN_ON -> {
                    Logger.d(TAG, "SystemReceiver: Screen ON")
                    handleScreenStateChanged()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Logger.d(TAG, "SystemReceiver: Screen OFF")
                    handleScreenStateChanged()
                }
                Intent.ACTION_USER_PRESENT -> {
                    Logger.d(TAG, "SystemReceiver: User present (unlocked)")
                    handleScreenStateChanged()
                }
            }
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val fromNotificationService = Utils.FROM_NOTIFICATION_SERVICE + context.packageName
            Logger.d(TAG, "NotificationReceiver: action=${intent?.action}, fromNotificationService=$fromNotificationService")

            if (intent?.action == fromNotificationService) {
                if ("ACTION_MEDIA_UPDATE" == intent.action || intent.hasExtra("is_playing")) {
                    Logger.d(TAG, "NotificationReceiver: Handling as MEDIA update")
                    handleMediaUpdate(intent)
                } else {
                    Logger.d(TAG, "NotificationReceiver: Handling as NOTIFICATION")
                    handleNotificationReceived(intent)
                }
            } else if (intent?.action == "ACTION_MEDIA_UPDATE") {
                Logger.d(TAG, "NotificationReceiver: Direct MEDIA update")
                handleMediaUpdate(intent)
            }
        }
    }

    // ====================================================================================
    // Logging Helpers
    // ====================================================================================

    private fun logStateDetails(state: IslandState) {
        when (state) {
            is IslandState.Minimal -> {
                Logger.d(TAG, "  State Details: Minimal island")
            }
            is IslandState.CompactMusic -> {
                Logger.d(TAG, "  State Details: CompactMusic - track='${state.track.title}' by '${state.track.artist}', isPlaying=${state.isPlaying}")
            }
            is IslandState.MusicPlayer -> {
                Logger.d(TAG, "  State Details: MusicPlayer - track='${state.track.title}' by '${state.track.artist}', isPlaying=${state.isPlaying}, progress=${state.track.progress}")
            }
            is IslandState.IncomingCall -> {
                Logger.d(TAG, "  State Details: IncomingCall - caller='${state.caller.name}', label='${state.caller.label}', actionsCount=${state.originalActions?.size ?: 0}")
            }
            is IslandState.OngoingCall -> {
//                Logger.d(TAG, "  State Details: OngoingCall - caller='${state.caller.name}', duration='${state.duration}', isMuted=${state.isMuted}")
            }
            is IslandState.Notification -> {
                Logger.d(TAG, "  State Details: Notification - count=${state.notifications.size}")
                state.notifications.forEachIndexed { index, notif ->
                    Logger.d(TAG, "    [$index] app='${notif.appName}', title='${notif.title}', msg='${notif.message?.take(50)}...'")
                }
            }
            is IslandState.NotificationWithMusic -> {
                Logger.d(TAG, "  State Details: NotificationWithMusic - notifCount=${state.notifications.size}, track='${state.track.title}', isPlaying=${state.isPlaying}")
            }
            is IslandState.Charging -> {
                Logger.d(TAG, "  State Details: Charging - percentage=${state.percentage}%, isLowBattery=${state.isLowBattery}")
            }
            is IslandState.ActionControl -> {
                Logger.d(TAG, "  State Details: ActionControl - tilesCount=${state.config.tiles.size}, notificationCount=${state.notificationCount}")
            }
            else -> {
                Logger.d(TAG, "  State Details: Unknown state type")
            }
        }
    }

    private fun logCurrentContext() {
        Logger.d(TAG, "=== CURRENT CONTEXT ===")
        Logger.d(TAG, "  currentState: ${currentState::class.simpleName}")
        Logger.d(TAG, "  notifications.size: ${notifications.size}")
        Logger.d(TAG, "  activeMusicTrack: ${activeMusicTrack?.title ?: "null"}")
        Logger.d(TAG, "  isMusicPlaying: $isMusicPlaying")
        Logger.d(TAG, "  isPhoneLocked: $isPhoneLocked")
        Logger.d(TAG, "  hasUserInteracted: $hasUserInteracted")
        Logger.d(TAG, "  isCurrentlyCharging: $isCurrentlyCharging")
        Logger.d(TAG, "=======================")
    }

    // ====================================================================================
    // Lifecycle Methods
    // ====================================================================================

    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.i(TAG, ">>> onServiceConnected() - Service connected to accessibility framework")
        initializeService()
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i(TAG, ">>> onCreate() - Service created")
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        Logger.d(TAG, "  Lifecycle state: CREATED")
    }

    override fun onDestroy() {
        Logger.i(TAG, ">>> onDestroy() - Service destroying")
        logCurrentContext()
        cleanupService()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        Logger.d(TAG, "  Lifecycle state: DESTROYED, scope cancelled")
        super.onDestroy()
    }

    override fun onInterrupt() {
        Logger.w(TAG, ">>> onInterrupt() - Service interrupted")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Log only significant events to avoid spam
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Logger.v(TAG, "AccessibilityEvent: WINDOW_STATE_CHANGED, package=${it.packageName}")
            }
        }
    }

    // ====================================================================================
    // Initialization
    // ====================================================================================

    private fun initializeService() {
        Logger.i(TAG, ">>> initializeService() - Starting initialization")
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        Logger.d(TAG, "  Lifecycle state: STARTED")

        initializeWindow()
        registerReceivers()

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        Logger.d(TAG, "  Lifecycle state: RESUMED")

        // Show minimal island after initialization if enabled
        mainHandler.postDelayed({
            serviceScope.launch {
                preferencesDataStore.islandEnabled.collect { enabled ->
                    Logger.d(TAG, "Preference: islandEnabled=$enabled")
                    if (enabled) {
                        if (currentState is IslandState.Hidden) {
                             currentState = IslandState.Minimal
                        }
                    } else {
                        currentState = IslandState.Hidden
                    }
                }
            }
        }, 500)
        
        // Follow position changes
        serviceScope.launch {
            preferencesDataStore.verticalOffset.collect { offset ->
                windowLayoutParams?.let { params ->
                    params.y = offset
                    Logger.d(TAG, "Position: updated vertical offset to $offset")
                    overlayView?.let { view ->
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            Logger.e(TAG, "Position: failed to update vertical layout", e)
                        }
                    }
                }
            }
        }
        
        serviceScope.launch {
            preferencesDataStore.horizontalOffset.collect { offset ->
                windowLayoutParams?.let { params ->
                    params.x = offset
                    Logger.d(TAG, "Position: updated horizontal offset to $offset")
                    overlayView?.let { view ->
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            Logger.e(TAG, "Position: failed to update horizontal layout", e)
                        }
                    }
                }
            }
        }

        Logger.i(TAG, "<<< initializeService() - Initialization complete")
    }

    private fun initializeWindow() {
        Logger.d(TAG, ">>> initializeWindow() - Creating overlay window")

        // Create layout params for overlay window
        windowLayoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        Logger.d(TAG, "  WindowLayoutParams created: type=ACCESSIBILITY_OVERLAY, gravity=TOP|CENTER")

        // Create Compose view
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@DynamicIslandAccessibilityService)
            setViewTreeSavedStateRegistryOwner(this@DynamicIslandAccessibilityService)

            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                    Logger.d(TAG, "Touch: ACTION_OUTSIDE detected, currentState=${currentState::class.simpleName}, hasUserInteracted=$hasUserInteracted")

                    // Only close if user has interacted with the notification
                    if ((currentState is IslandState.Notification ||
                                currentState is IslandState.NotificationWithMusic) && !hasUserInteracted) {
                        Logger.d(TAG, "Touch: Ignoring outside touch - user hasn't interacted yet")
                        collapseToAppropriateState()
                        return@setOnTouchListener true
                    }

                    // Close expanded states on outside touch
                    if (currentState !is IslandState.Minimal &&
                        currentState !is IslandState.CompactMusic &&
                        currentState !is IslandState.NotificationWithMusic) {
                        Logger.d(TAG, "Touch: Outside touch - collapsing to appropriate state")
                        collapseToAppropriateState()
                    }

//                    if (currentState is IslandState.NotificationWithMusic) {
//                        Logger.e(TAG, "Touch: NotificationWithMusic outside touch - reverting to CompactMusic")
//                        val state = currentState as IslandState.NotificationWithMusic
//                        currentState = IslandState.CompactMusic(
//                            track = state.track,
//                            isPlaying = state.isPlaying
//                        )
//                    }
                    true
                } else {
                    false
                }
            }

            setContent {
                DynamicIsland(
                    state = currentState,
                    onMusicAction = { action ->
                        Logger.d(TAG, "MusicAction: $action")
                        cancelAutoClose()
                        when(action) {
                            MusicAction.PlayPause -> {
                                Logger.d(TAG, "MusicAction: Sending PLAY_PAUSE command")
                                val intent = Intent("ACTION_MEDIA_CONTROL").apply { putExtra("command", "PLAY_PAUSE") }
                                LocalBroadcastManager.getInstance(this@DynamicIslandAccessibilityService).sendBroadcast(intent)
                            }
                            MusicAction.Previous -> {
                                Logger.d(TAG, "MusicAction: Sending PREVIOUS command")
                                val intent = Intent("ACTION_MEDIA_CONTROL").apply { putExtra("command", "PREVIOUS") }
                                LocalBroadcastManager.getInstance(this@DynamicIslandAccessibilityService).sendBroadcast(intent)
                            }
                            MusicAction.Next -> {
                                Logger.d(TAG, "MusicAction: Sending NEXT command")
                                val intent = Intent("ACTION_MEDIA_CONTROL").apply { putExtra("command", "NEXT") }
                                LocalBroadcastManager.getInstance(this@DynamicIslandAccessibilityService).sendBroadcast(intent)
                            }
                            is MusicAction.Seek -> {
                                activeMusicTrack?.let { track ->
                                    val seekPosition = (action.position * track.rawDuration).toLong()
                                    Logger.d(TAG, "MusicAction: Seeking to position=${action.position}, ms=$seekPosition")
                                    val intent = Intent("ACTION_MEDIA_CONTROL").apply {
                                        putExtra("command", "SEEK")
                                        putExtra("seek_to", seekPosition)
                                    }
                                    LocalBroadcastManager.getInstance(this@DynamicIslandAccessibilityService).sendBroadcast(intent)
                                }
                            }
                            else -> {
                                Logger.d(TAG, "MusicAction: Unhandled action: $action")
                            }
                        }
                    },
                    onCallAction = { action ->
                        Logger.d(TAG, "CallAction: $action, currentState=${currentState::class.simpleName}")
                        if (currentState is IslandState.IncomingCall) {
                            val state = currentState as IslandState.IncomingCall
                            val originalActions = state.originalActions
                            Logger.d(TAG, "CallAction: Available actions count=${originalActions?.size ?: 0}")

                            if (!originalActions.isNullOrEmpty()) {
                                originalActions.forEachIndexed { index, act ->
                                    Logger.d(TAG, "CallAction: [$index] title='${act.title}'")
                                }

                                val actionToExecute = when(action) {
                                    CallAction.Accept -> {
                                        Logger.d(TAG, "CallAction: Looking for ACCEPT action")
                                        originalActions.find {
                                            val t = it.title.toString()
                                            t.contains("Answer", ignoreCase = true) || t.contains("Accept", ignoreCase = true)
                                        } ?: originalActions.firstOrNull()
                                    }
                                    CallAction.Decline -> {
                                        Logger.d(TAG, "CallAction: Looking for DECLINE action")
                                        originalActions.find {
                                            val t = it.title.toString()
                                            t.contains("Decline", ignoreCase = true) || t.contains("Reject", ignoreCase = true) || t.contains("Hang", ignoreCase = true)
                                        } ?: originalActions.lastOrNull()
                                    }
                                }

                                Logger.d(TAG, "CallAction: Selected action='${actionToExecute?.title}'")

                                try {
                                    actionToExecute?.actionIntent?.send()
                                    Logger.e(TAG, "CallAction: Action executed successfully")
                                    currentState = IslandState.Minimal
                                } catch (e: Exception) {
                                    Logger.e(TAG, "CallAction: Failed to execute action", e)
                                }
                            } else {
                                Logger.w(TAG, "CallAction: No original actions available")
                            }
                        }
                    },
                    onSilentModeToggle = {
                        Logger.d(TAG, "SilentModeToggle: Toggled")
                    },
                    onNotificationAction = { action ->
                        Logger.d(TAG, "NotificationAction: $action")
                        cancelAutoClose()
                        hasUserInteracted = true
                    },
                    onNotificationDismiss = {
                        Logger.d(TAG, "NotificationDismiss: Dismissing current notification")
                        if (currentState is IslandState.Notification) {
                            val state = currentState as IslandState.Notification
                            if (state.notifications.isNotEmpty()) {
                                collapseToAppropriateState()
                            }
                        }
                    },
                    onActionControlAppLaunch = { packageName ->
                        Logger.d(TAG, "ActionControlAppLaunch: Launching package='$packageName'")
                        try {
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                                Logger.i(TAG, "ActionControlAppLaunch: Successfully launched $packageName")
                            } else {
                                Logger.w(TAG, "ActionControlAppLaunch: No launch intent found for $packageName")
                            }
                        } catch (e: Exception) {
                            Logger.e(TAG, "ActionControlAppLaunch: Failed to launch $packageName", e)
                        }
                    },
                    onActionControlContact = { contactId ->
                        Logger.d(TAG, "ActionControlContact: contactId='$contactId'")
                    },
                    onActionControlBrightness = { value ->
                        Logger.d(TAG, "ActionControlBrightness: value=$value")
                    },
                    onActionControlVolume = { value ->
                        Logger.d(TAG, "ActionControlVolume: value=$value")
                    },
                    onNotificationsAction = {
                        Logger.e(TAG, "NotificationsAction: Showing ${notifications.size} notifications")
                        cancelAutoClose()
                        currentState = IslandState.Notification(notifications)
                    },
                    onLockAction = {
                        Logger.e(TAG, "LockAction: Locking screen")
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                        currentState = IslandState.Minimal
                    },
                    onCameraAction = {
                        Logger.d(TAG, "CameraAction: Opening camera")
                        try {
                            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            Logger.e(TAG, "CameraAction: Camera launched successfully")
                            currentState = IslandState.Minimal
                        } catch (e: Exception) {
                            Logger.e(TAG, "CameraAction: Failed to launch camera", e)
                        }
                    },
                    onSettingsAction = {
                        Logger.e(TAG, "SettingsAction: Opening quick settings")
                        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                        currentState = IslandState.Minimal
                    },
                    onActionControlToggle = { toggleType ->
                        Logger.e(TAG, "ActionControlToggle: type=$toggleType")
                        cancelAutoClose()
                        handleSystemToggle(toggleType)
                    },
                    onSideIslandTap = {
                        Logger.e(TAG, "SideIslandTap: Opening ActionControl")
                        cancelAutoClose()
                        currentState = IslandState.ActionControl(
                            config = com.notch.dynamicislanddemo.models.ActionControlConfig(
                                tiles = listOf(
                                    com.notch.dynamicislanddemo.models.ControlTile.SystemToggle(
                                        com.notch.dynamicislanddemo.models.SystemToggleType.WIFI, true, "WiFi"
                                    ),
                                    com.notch.dynamicislanddemo.models.ControlTile.SystemToggle(
                                        com.notch.dynamicislanddemo.models.SystemToggleType.BLUETOOTH, true, "Bluetooth"
                                    ),
                                    com.notch.dynamicislanddemo.models.ControlTile.SystemToggle(
                                        com.notch.dynamicislanddemo.models.SystemToggleType.TORCH, false, "Flashlight"
                                    ),
                                    com.notch.dynamicislanddemo.models.ControlTile.SystemToggle(
                                        com.notch.dynamicislanddemo.models.SystemToggleType.AIRPLANE_MODE, false, "Airplane"
                                    ),
                                    com.notch.dynamicislanddemo.models.ControlTile.SystemToggle(
                                        com.notch.dynamicislanddemo.models.SystemToggleType.DO_NOT_DISTURB, false, "DND"
                                    )
                                )
                            ),
                            notificationCount = notifications.size
                        )
                    },
                    onIslandTap = {
                        Logger.d(TAG, "IslandTap: Tapped on island, currentState=${currentState::class.simpleName}")
                        cancelAutoClose()
                        hasUserInteracted = true
                        when (currentState) {
                            is IslandState.Minimal -> {
                                Logger.d(TAG, "IslandTap: Minimal tapped, activeMusicTrack=${activeMusicTrack?.title}, isMusicPlaying=$isMusicPlaying")
                                if (activeMusicTrack != null && isMusicPlaying) {
                                    Logger.e(TAG, "IslandTap: Expanding to MusicPlayer")
                                    currentState = IslandState.MusicPlayer(
                                        track = activeMusicTrack!!,
                                        isPlaying = true
                                    )
                                }
                            }
                            is IslandState.CompactMusic -> {
                                Logger.e(TAG, "IslandTap: CompactMusic tapped, expanding to MusicPlayer")
                                currentState = IslandState.MusicPlayer(
                                    track = (currentState as IslandState.CompactMusic).track,
                                    isPlaying = (currentState as IslandState.CompactMusic).isPlaying
                                )
                            }
                            is IslandState.MusicPlayer -> {
                                Logger.e(TAG, "IslandTap: MusicPlayer tapped, collapsing to CompactMusic")
                                currentState = IslandState.CompactMusic(
                                    track = (currentState as IslandState.MusicPlayer).track,
                                    isPlaying = (currentState as IslandState.MusicPlayer).isPlaying
                                )
                            }
                            is IslandState.Notification -> {
                                Logger.d(TAG, "IslandTap: Notification tapped, launching tap action")
                                try {
                                    val state = currentState as IslandState.Notification
                                    state.notifications.firstOrNull()?.tapAction?.send()
                                    Logger.e(TAG, "IslandTap: Tap action sent successfully")
                                    currentState = IslandState.Minimal
                                } catch (e: Exception) {
                                    Logger.e(TAG, "IslandTap: Failed to send tap action", e)
                                }
                            }
                            is IslandState.NotificationWithMusic -> {
                                Logger.d(TAG, "IslandTap: NotificationWithMusic tapped, launching notification")
                                try {
                                    val state = currentState as IslandState.NotificationWithMusic
//                                    state.notifications.firstOrNull()?.tapAction?.send()
                                    Logger.e(TAG, "IslandTap: Tap action sent, reverting to MusicPlayer")
//                                    currentState = IslandState.CompactMusic(
//                                        track = state.track,
//                                        isPlaying = state.isPlaying
//                                    )
                                    currentState = IslandState.MusicPlayer(
                                        track = state.track,
                                        isPlaying = state.isPlaying
                                    )
                                } catch (e: Exception) {
                                    Logger.e(TAG, "IslandTap: Failed to send tap action", e)
                                }
                            }
                            is IslandState.IncomingCall -> {
                                Logger.d(TAG, "IslandTap: IncomingCall tapped (no action defined)")
                            }
                            else -> {
                                Logger.d(TAG, "IslandTap: Unhandled state: ${currentState::class.simpleName}")
                            }
                        }
                    }
                )
            }
        }
        Logger.d(TAG, "  ComposeView created and configured")

        // Add view to window manager
        try {
            windowManager.addView(overlayView, windowLayoutParams)
            Logger.i(TAG, "<<< initializeWindow() - Overlay view added to WindowManager successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "<<< initializeWindow() - Failed to add overlay view", e)
        }
    }

    private fun registerReceivers() {
        Logger.d(TAG, ">>> registerReceivers() - Registering broadcast receivers")

        // System broadcasts
        val systemFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(systemReceiver, systemFilter)
        Logger.d(TAG, "  Registered systemReceiver for: SCREEN_ON, SCREEN_OFF, USER_PRESENT, BATTERY_CHANGED")

        // Notification broadcasts
        val notificationFilter = IntentFilter().apply {
            addAction("${Utils.FROM_NOTIFICATION_SERVICE}${packageName}")
            addAction("ACTION_MEDIA_UPDATE")
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(notificationReceiver, notificationFilter)
        Logger.d(TAG, "  Registered notificationReceiver for: ${Utils.FROM_NOTIFICATION_SERVICE}${packageName}, ACTION_MEDIA_UPDATE")

        Logger.d(TAG, "<<< registerReceivers() - All receivers registered")
    }

    // ====================================================================================
    // Cleanup
    // ====================================================================================

    private fun cleanupService() {
        Logger.i(TAG, ">>> cleanupService() - Starting cleanup")

        try {
            unregisterReceiver(systemReceiver)
            Logger.d(TAG, "  Unregistered systemReceiver")
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(notificationReceiver)
            Logger.d(TAG, "  Unregistered notificationReceiver")
        } catch (e: Exception) {
            Logger.e(TAG, "  Failed to unregister receivers", e)
        }

        try {
            overlayView?.let {
                windowManager.removeView(it)
                Logger.d(TAG, "  Removed overlay view from WindowManager")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "  Failed to remove overlay view", e)
        }

        overlayView = null
        Logger.i(TAG, "<<< cleanupService() - Cleanup complete")
    }

    // ====================================================================================
    // Event Handlers
    // ====================================================================================

    private fun handleBatteryChanged(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = (level * 100 / scale.toFloat()).toInt()

        Logger.v(TAG, "Battery: status=$status, isCharging=$isCharging, wasCharging=$isCurrentlyCharging, level=$percentage%")

        // Only show notification when charging first connects
        if (isCharging && !isCurrentlyCharging) {
            Logger.e(TAG, "Battery: Charging CONNECTED at $percentage%")

            currentState = IslandState.Charging(
                percentage = percentage,
                isLowBattery = percentage < 20
            )

            // Auto-hide after 3 seconds
            mainHandler.postDelayed({
                if (currentState is IslandState.Charging) {
                    Logger.d(TAG, "Battery: Auto-hiding charging indicator")
                    collapseToAppropriateState()
                }
            }, 3000)
        } else if (!isCharging && isCurrentlyCharging) {
            Logger.i(TAG, "Battery: Charging DISCONNECTED at $percentage%")
        }

        isCurrentlyCharging = isCharging
    }

    private fun handleScreenStateChanged() {
        val wasLocked = isPhoneLocked
        isPhoneLocked = keyguardManager.inKeyguardRestrictedInputMode()
        Logger.d(TAG, "ScreenState: wasLocked=$wasLocked, isLocked=$isPhoneLocked")

        if (isPhoneLocked) {
            Logger.e(TAG, "ScreenState: Phone locked, setting state to Minimal")
            currentState = IslandState.Minimal
        }
    }

    // ====================================================================================
    // Notification Handling
    // ====================================================================================

    private data class NotificationDataParsed(
        val packageName: String,
        val id: String,
        val key: String,
        val groupKey: String,
        val title: String,
        val text: String,
        val bigText: String,
        val appName: String,
        val category: String?,
        val template: String?,
        val tag: String,
        val postTime: Long,
        val isAdded: Boolean,
        val isClearable: Boolean,
        val isOngoing: Boolean,
        val isGroup: Boolean,
        val contentIntent: android.app.PendingIntent?,
        val actions: ArrayList<ActionParsable>?
    )

    private var currentExpandedNotificationKey: String? = null

    private fun handleNotificationReceived(intent: Intent) {
        Logger.d(TAG, ">>> handleNotificationReceived()")
        val notificationData = parseNotificationIntent(intent)

        if (notificationData == null) {
            Logger.w(TAG, "<<< handleNotificationReceived() - Failed to parse notification intent")
            return
        }

        Logger.d(TAG, "Notification parsed: isAdded=${notificationData.isAdded}, package=${notificationData.packageName}")
        Logger.d(TAG, "  category=${notificationData.category}, template=${notificationData.template}")
        Logger.d(TAG, "  title='${notificationData.title}', text='${notificationData.text.take(50)}...'")
        Logger.d(TAG, "  key=${notificationData.key}, groupKey=${notificationData.groupKey}")
        Logger.d(TAG, "  isOngoing=${notificationData.isOngoing}, isClearable=${notificationData.isClearable}")
        Logger.d(TAG, "  actionsCount=${notificationData.actions?.size ?: 0}")

        if (notificationData.isAdded) {
            handleAddedNotification(notificationData)
        } else {
            handleRemovedNotification(notificationData.key)
        }

        Logger.d(TAG, "<<< handleNotificationReceived()")
    }

    private fun parseNotificationIntent(intent: Intent): NotificationDataParsed? {
        return try {
            val packageName = intent.getStringExtra("package")
            if (packageName == null) {
                Logger.w(TAG, "parseNotificationIntent: Missing package name")
                return null
            }

            val id = intent.getStringExtra("id") ?: packageName
            val key = intent.getStringExtra("key") ?: id
            val groupKey = intent.getStringExtra("group_key") ?: id

            NotificationDataParsed(
                packageName = packageName,
                id = id,
                key = key,
                groupKey = groupKey,
                title = intent.getStringExtra("title") ?: "",
                text = intent.getStringExtra("text") ?: "",
                bigText = intent.getStringExtra("bigText") ?: "",
                appName = intent.getStringExtra("appName") ?: "",
                category = intent.getStringExtra("category"),
                template = intent.getStringExtra("template"),
                tag = intent.getStringExtra("tag") ?: "",
                postTime = intent.getLongExtra("postTime", System.currentTimeMillis()),
                isAdded = intent.getBooleanExtra("isAdded", true),
                isClearable = intent.getBooleanExtra("isClearable", true),
                isOngoing = intent.getBooleanExtra("isOngoing", false),
                isGroup = intent.getBooleanExtra("isGroup", false),
                contentIntent = intent.getParcelableExtra("pendingIntent"),
                actions = intent.getParcelableArrayListExtra("actions")
            )
        } catch (e: Exception) {
            Logger.e(TAG, "parseNotificationIntent: Error parsing", e)
            null
        }
    }

    private fun handleAddedNotification(data: NotificationDataParsed) {
        Logger.d(TAG, ">>> handleAddedNotification() - ${data.title}")

        // Check for call
        if (data.category?.equals("call", ignoreCase = true) == true || data.category == "msg_call") {
            if (data.actions != null && data.actions.isNotEmpty()) {
                Logger.e(TAG, "CALL DETECTED: Showing IncomingCall UI for '${data.title}'")
                data.actions.forEachIndexed { index, action ->
                    Logger.d(TAG, "  Call action[$index]: title='${action.title}'")
                }
                currentState = IslandState.IncomingCall(
                    caller = ContactInfo(name = data.title, label = data.text, photoUrl = null),
                    originalActions = data.actions
                )
                Logger.d(TAG, "<<< handleAddedNotification() - Call UI shown")
                return
            } else {
                Logger.w(TAG, "Call notification without actions - ignoring")
            }
        }

        // Check for media
        if (data.category == "transport" || data.template?.contains("MediaStyle", ignoreCase = true) == true) {
            Logger.d(TAG, "<<< handleAddedNotification() - Skipping media notification (handled by media update)")
            return
        }

        // Regular notification
        val notificationInfo = NotificationInfo(
            id = data.key,
            groupKey = data.groupKey,
            category = data.category,
            template = data.template,
            appName = data.appName,
            title = data.title,
            message = data.text,
            bigText = data.bigText,
            timestamp = "Now",
            tapAction = data.contentIntent,
            originalActions = data.actions
        )

        val existingIndex = notifications.indexOfFirst { it.groupKey == data.groupKey }

        if (existingIndex != -1) {
            Logger.d(TAG, "Updating existing notification at index $existingIndex (groupKey: ${data.groupKey})")
            notifications[existingIndex] = notificationInfo
        } else {
            Logger.d(TAG, "Adding NEW notification (groupKey: ${data.groupKey})")
            notifications.add(0, notificationInfo)
            triggerAutoExpansion(notificationInfo)
        }

        Logger.d(TAG, "Total notifications: ${notifications.size}")
        notifications.forEachIndexed { index, notif ->
            Logger.v(TAG, "  [$index] ${notif.appName}: ${notif.title}")
        }

        updateNotificationDisplay()
        Logger.d(TAG, "<<< handleAddedNotification()")
    }

    private fun handleRemovedNotification(key: String) {
        Logger.d(TAG, ">>> handleRemovedNotification() - key=$key")

        val sizeBefore = notifications.size
        val wasRemoved = notifications.removeAll { it.id == key || it.groupKey == key }

        if (wasRemoved) {
            Logger.i(TAG, "Notification removed. Before: $sizeBefore, After: ${notifications.size}")
            logCurrentContext()

            when (currentState) {
                is IslandState.Notification -> {
                    if (notifications.isEmpty()) {
                        Logger.d(TAG, "No notifications left, collapsing")
                        collapseToAppropriateState()
                    } else {
                        Logger.e(TAG, "Updating notification display with ${notifications.size} remaining")
                        currentState = IslandState.Notification(notifications = notifications)
                    }
                }
                is IslandState.NotificationWithMusic -> {
                    if (notifications.isEmpty()) {
                        Logger.d(TAG, "No notifications left, checking for music")
                        if (activeMusicTrack != null && isMusicPlaying) {
                            Logger.e(TAG, "Music playing, showing CompactMusic")
                            currentState = IslandState.CompactMusic(track = activeMusicTrack!!, isPlaying = true)
                        } else {
                            Logger.e(TAG, "No music, going to Minimal")
                            currentState = IslandState.Minimal
                        }
                    } else {
                        val musicState = currentState as IslandState.NotificationWithMusic
                        Logger.e(TAG, "Updating NotificationWithMusic with ${notifications.size} remaining")
                        currentState = IslandState.NotificationWithMusic(
                            notifications = notifications,
                            track = musicState.track,
                            isPlaying = musicState.isPlaying
                        )
                    }
                }
                else -> {
                    Logger.d(TAG, "Not in notification state (${currentState::class.simpleName}), no UI update needed")
                }
            }
        } else {
            Logger.d(TAG, "No notification found with key=$key")
        }

        Logger.d(TAG, "<<< handleRemovedNotification()")
    }

    private fun triggerAutoExpansion(notification: NotificationInfo) {
        Logger.d(TAG, ">>> triggerAutoExpansion() - ${notification.title}")

        if (currentState is IslandState.IncomingCall || currentState is IslandState.OngoingCall) {
            Logger.d(TAG, "<<< triggerAutoExpansion() - Skipped (Call in progress)")
            return
        }

        val isCall = notification.category?.equals("call", ignoreCase = true) ?: false
        Logger.e(TAG, "AutoExpand: isCall=$isCall, hasUserInteracted=$hasUserInteracted")

        hasUserInteracted = false
        currentState = IslandState.Notification(notifications = notifications)
        currentExpandedNotificationKey = notification.id

        collapseJob?.cancel()
        Logger.d(TAG, "AutoExpand: Previous collapse job cancelled")

        if (!isCall) {
            Logger.d(TAG, "AutoExpand: Scheduling auto-collapse in 3000ms")
            collapseJob = serviceScope.launch {
                delay(3000)
                if (currentExpandedNotificationKey == notification.id && !hasUserInteracted) {
                    Logger.d(TAG, "AutoExpand: Auto-collapse triggered")
                    collapseToAppropriateState()
                } else {
                    Logger.d(TAG, "AutoExpand: Auto-collapse cancelled (key changed or user interacted)")
                }
            }
        } else {
            Logger.d(TAG, "AutoExpand: Call notification - no auto-collapse")
        }

        Logger.d(TAG, "<<< triggerAutoExpansion()")
    }

    private fun updateNotificationDisplay() {
        Logger.v(TAG, ">>> updateNotificationDisplay()")

        if (currentState is IslandState.IncomingCall || currentState is IslandState.OngoingCall) {
            Logger.v(TAG, "<<< updateNotificationDisplay() - Skipped (Call in progress)")
            return
        }

        if (currentState is IslandState.Notification) {
            Logger.e(TAG, "Updating notification state with ${notifications.size} notifications")
            currentState = IslandState.Notification(notifications = notifications)
        }

        Logger.v(TAG, "<<< updateNotificationDisplay()")
    }

    private fun collapseToAppropriateState() {
        Logger.d(TAG, ">>> collapseToAppropriateState()")
        logCurrentContext()

        when {
            notifications.isNotEmpty() && activeMusicTrack != null -> {
                Logger.e(TAG, "Collapsing to NotificationWithMusic (notifs=${notifications.size}, playing=$isMusicPlaying)")
                currentState = IslandState.NotificationWithMusic(
                    notifications = notifications,
                    track = activeMusicTrack!!,
                    isPlaying = isMusicPlaying
                )
            }
            activeMusicTrack != null && isMusicPlaying -> {
                Logger.e(TAG, "Collapsing to CompactMusic")
                currentState = IslandState.CompactMusic(track = activeMusicTrack!!, isPlaying = true)
            }
//            notifications.isNotEmpty() -> {
//                Logger.e(TAG, "Collapsing to Notification (${notifications.size} notifs)")
//                currentState = IslandState.Notification(notifications = notifications)
//            }
            else -> {
                Logger.e(TAG, "Collapsing to Minimal")
                currentState = IslandState.Minimal
            }
        }

        Logger.d(TAG, "<<< collapseToAppropriateState()")
    }

    private var activeMusicTrack: MusicTrack? = null
    private var isMusicPlaying = false
    private var collapseJob: Job? = null

    private fun cancelAutoClose() {
        if (collapseJob?.isActive == true) {
            Logger.d(TAG, "cancelAutoClose: Cancelling active collapse job")
            collapseJob?.cancel()
        }
        collapseJob = null
    }

    private fun handleMediaUpdate(intent: Intent) {
        val isPlaying = intent.getBooleanExtra("is_playing", false)
        val title = intent.getStringExtra("title") ?: "Unknown"
        val artist = intent.getStringExtra("artist") ?: "Unknown"
        val duration = intent.getLongExtra("duration", 0L)
        val currentTime = intent.getLongExtra("current_time", 0L)

        Logger.d(TAG, ">>> handleMediaUpdate()")
        Logger.d(TAG, "  isPlaying=$isPlaying, title='$title', artist='$artist'")
        Logger.d(TAG, "  duration=${formatTime(duration)}, currentTime=${formatTime(currentTime)}")
        Logger.d(TAG, "  progress=${if (duration > 0) "%.1f%%".format((currentTime.toFloat() / duration) * 100) else "0%"}")

        val wasPlaying = isMusicPlaying
        isMusicPlaying = isPlaying

        activeMusicTrack = MusicTrack(
            title = title,
            artist = artist,
            albumArtUrl = null,
            currentTime = formatTime(currentTime),
            totalDuration = formatTime(duration),
            progress = if (duration > 0) currentTime.toFloat() / duration else 0f,
            rawDuration = duration
        )

        Logger.d(TAG, "  wasPlaying=$wasPlaying, isMusicPlaying=$isMusicPlaying")
        Logger.d(TAG, "  currentState=${currentState::class.simpleName}")

        // Priority: Call > Notification (Temporary) > Music > Minimal
        if (currentState is IslandState.IncomingCall || currentState is IslandState.OngoingCall) {
            Logger.d(TAG, "<<< handleMediaUpdate() - Skipped (Call in progress)")
            return
        }

        // If notification is showing, upgrade to NotificationWithMusic if music is playing
        if (currentState is IslandState.Notification) {
            val notifState = currentState as IslandState.Notification
            // Upgrade if we have music track (playing or not)
            if (activeMusicTrack != null) {
                Logger.e(TAG, "Media: Upgrading Notification to NotificationWithMusic (playing=$isPlaying)")
                currentState = IslandState.NotificationWithMusic(
                    notifications = notifState.notifications,
                    track = activeMusicTrack!!,
                    isPlaying = isPlaying
                )
            }
            Logger.d(TAG, "<<< handleMediaUpdate()")
            return
        }

        // If already in NotificationWithMusic, update the music info
        if (currentState is IslandState.NotificationWithMusic) {
            val current = currentState as IslandState.NotificationWithMusic
            Logger.e(TAG, "Media: Updating NotificationWithMusic")
            currentState = IslandState.NotificationWithMusic(
                notifications = current.notifications,
                track = activeMusicTrack!!,
                isPlaying = isPlaying
            )
            Logger.d(TAG, "<<< handleMediaUpdate()")
            return
        }

        if (isPlaying) {
            Logger.e(TAG, "Media: Showing CompactMusic (playing)")
            collapseToAppropriateState()
        } else {
            Logger.e(TAG, "Media: Showing CompactMusic (paused)")
            collapseToAppropriateState()
        }

        Logger.d(TAG, "<<< handleMediaUpdate()")
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    // ====================================================================================
    // Demo Methods
    // ====================================================================================

    fun showMusicPlayer(track: MusicTrack) {
        Logger.e(TAG, "showMusicPlayer() - Demo: Showing music player for '${track.title}'")
        currentState = IslandState.MusicPlayer(
            track = track,
            isPlaying = true
        )
    }

    fun showIncomingCall(callerName: String, callerNumber: String) {
        Logger.e(TAG, "showIncomingCall() - Demo: Showing incoming call from '$callerName' ($callerNumber)")
        currentState = IslandState.IncomingCall(
            caller = ContactInfo(
                name = callerName,
                label = "Mobile",
                photoUrl = null
            )
        )
    }

    // ====================================================================================
    // System Toggles
    // ====================================================================================

    private fun handleSystemToggle(type: com.notch.dynamicislanddemo.models.SystemToggleType) {
        Logger.d(TAG, ">>> handleSystemToggle() - type=$type")
        try {
            when (type) {
                com.notch.dynamicislanddemo.models.SystemToggleType.WIFI -> {
                    Logger.d(TAG, "SystemToggle: Opening WiFi settings")
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                com.notch.dynamicislanddemo.models.SystemToggleType.BLUETOOTH -> {
                    Logger.d(TAG, "SystemToggle: Opening Bluetooth settings")
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                com.notch.dynamicislanddemo.models.SystemToggleType.TORCH -> {
                    Logger.d(TAG, "SystemToggle: Toggling flashlight (current=$isTorchOn)")
                    toggleFlashlight()
                }
                com.notch.dynamicislanddemo.models.SystemToggleType.AIRPLANE_MODE -> {
                    Logger.d(TAG, "SystemToggle: Opening Airplane mode settings")
                    startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                com.notch.dynamicislanddemo.models.SystemToggleType.DO_NOT_DISTURB -> {
                    Logger.d(TAG, "SystemToggle: Opening notifications")
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                }
                else -> {
                    Logger.d(TAG, "SystemToggle: Unhandled type, opening quick settings")
                    performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "SystemToggle: Error handling toggle", e)
        }
        Logger.d(TAG, "<<< handleSystemToggle()")
    }

    private var isTorchOn = false
    private fun toggleFlashlight() {
        Logger.d(TAG, ">>> toggleFlashlight() - current state: $isTorchOn")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val cameraManager = getSystemService(CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                val cameraId = cameraManager.cameraIdList[0]
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                Logger.i(TAG, "Flashlight toggled: now $isTorchOn")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to toggle flashlight", e)
            }
        } else {
            Logger.w(TAG, "Flashlight toggle requires API 23+")
        }
        Logger.d(TAG, "<<< toggleFlashlight()")
    }
}