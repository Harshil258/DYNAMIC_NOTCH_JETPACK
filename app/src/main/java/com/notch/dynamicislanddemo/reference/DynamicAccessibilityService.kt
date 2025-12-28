/*
package ccom.notch.dynamicislanddemo.reference

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.core.animation.doOnEnd
import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.*
import com.dynamicisland.dynamicbarisland.R
import com.dynamicisland.dynamicbarisland.adaptar.UnifiedNotificationAdapter
import com.dynamicisland.dynamicbarisland.fragment.ControlToolsFragment
import com.dynamicisland.dynamicbarisland.models.*
import com.dynamicisland.dynamicbarisland.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.math.abs

class DynamicAccessibilityService : AccessibilityService() {

    // region Constants
    // endregion

    // region Sealed Classes for State Management
    sealed class IslandState {
        object Hidden : IslandState()
        object Small : IslandState()
        object Full : IslandState()
        object Controls : IslandState()
    }

    sealed class CameraPosition(val gravity: Int) {
        object Left : CameraPosition(Gravity.START or Gravity.TOP)
        object Center : CameraPosition(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        object Right : CameraPosition(Gravity.END or Gravity.TOP)

        companion object {
            fun fromInt(value: Int): CameraPosition = when (value) {
                1 -> Left
                2 -> Center
                3 -> Right
                else -> Center
            }
        }
    }
    // endregion

    // region Coroutine Scope
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    // endregion

    // region State Flows
    private val _islandState = MutableStateFlow<IslandState>(IslandState.Hidden)
    private val islandState: StateFlow<IslandState> = _islandState.asStateFlow()

    private val _phoneLocked = MutableStateFlow(false)
    private val _inFullScreen = MutableStateFlow(false)
    // endregion

    // region Lazy Properties
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val keyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    private val mediaSessionManager by lazy { getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager }

    val utils by lazy { Utils(this) }
    private val preferenceManager by lazy { MyPreferenceManager(this) }
    private val accebNodeUtil by lazy { AccebNodeUtil(this, utils.getStatusBarHeight(resources)) }
    val controlsActionHandler by lazy { ControlsActionHandler(this) }
    val controlToolsFragment by lazy { ControlToolsFragment(this) }

    private val scaledDensity get() = resources.displayMetrics.scaledDensity
    // endregion

    // region View References
    private var statusBarView: IslandParentView? = null
    private var statusBarParentView: View? = null
    private var islandParentLayout: LinearLayout? = null
    private var islandTopLayout: RelativeLayout? = null
    private var llControls: LinearLayoutCompat? = null
    private var rvIslandSmall: RecyclerView? = null
    // rvIslandBig removed - unified adapter handles both states
    // endregion

    // region Adapters
    private var unifiedAdapter: UnifiedNotificationAdapter? = null
    // endregion

    // region State Variables
    private var localLayoutParams: WindowManager.LayoutParams? = null
    private var currentNotification: Notification? = null
    var currentIndex = 0

    // Big island state tracking
    private var isAnimatingHeight = false
    private var currentBigIslandNotification: Notification? = null
    private var isProgrammaticScroll = false

    private var minIslandHeight = 30
    private var minCameIslandWidth = 150
    private var margin = 25
    private var startMargin = 20
    private var tempMargin = 0
    private var cameraCount = 0
    private var cameraPosition: CameraPosition = CameraPosition.Center

    private var isControlEnabled = true
    private var isShowingControls = false
    private var isClosingFull = false
    private var isClosingControls = false
    private var isSmartMenuEnabled = false
    private var useGlow = true
    private var useIphoneCallDesign = false
    private var performClick = false
    private var overrideStock = false
        set(value) {
            field = value
            Logger.d(Logger.TAG, "State: overrideStock set to: $value")
        }

    private var shadeName: String? = null
    private var currentPackageName: String? = null
    // endregion

    // region Lists
    // Single shared notification array used by both small and big island adapters
    private val notifications = arrayListOf<Notification?>()
    val tiles = arrayListOf<TileInfoModel?>()
    private var callPKG = ControlAppAccessList()
    private var filterPKG = ControlAppAccessList()
    // endregion

    // region Jobs
    private var animationJob: Job? = null
    private var mediaUpdateJob: Job? = null
    private var pendingJob: Job? = null
    private var autoCollapseJob: Job? = null
    private var currentExpandedNotificationKey: String? = null
    // endregion

    // region Animation Tracking
    private val activeAnimations =
        mutableMapOf<Pair<View, DynamicAnimation.ViewProperty>, SpringAnimation>()
    // endregion

    // region Content Observer
    private val contentObserver by lazy {
        object : ContentObserver(mainHandler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri == ENABLED_ACCESSIBILITY_SERVICES) {
                    val isEnabled =
                        MyHelper.checkAccessibilityEnabled(this@DynamicAccessibilityService)
                    Logger.d(Logger.TAG, "Accessibility State Changed: Enabled = $isEnabled")
                    if (!isEnabled) {
                        cleanUp()
                    }
                }
            }
        }
    }
    // endregion

    // region Broadcast Receivers
    private val systemInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Logger.d(Logger.TAG, "System Broadcast Received: ${intent.action}")
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> handleBatteryChanged(intent)
                "android.media.RINGER_MODE_CHANGED" -> handleRingerModeChanged()
                Intent.ACTION_USER_PRESENT,
                Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_SCREEN_ON -> handleScreenStateChange()
            }
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val expectedAction = Utils.FROM_NOTIFICATION_SERVICE + context.packageName
            if (intent?.action == expectedAction) {
                Logger.d(Logger.TAG, "Local Broadcast: Notification received from service")
                updateNotificationList(intent)
            }
        }
    }
    // endregion

    // region Preference Listener
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        key?.let {
            Logger.d(Logger.TAG, "Preference Changed: $key")
            handlePreferenceChange(it)
        }
    }
    // endregion

    // region Lifecycle Methods
    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.d(Logger.TAG, ">>> onServiceConnected: Initializing Dynamic Island Service")
        initializeService()
    }

    override fun onInterrupt() {
        Logger.e(Logger.TAG, ">>> onInterrupt: Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        Logger.d(Logger.TAG, ">>> onDestroy: Cleaning up service resources")
        cleanUp()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(Logger.TAG, "onStartCommand: flags=$flags, startId=$startId")
        intent?.getIntExtra("com.control.center.intent.MESSAGE", -1)?.takeIf { it == 0 }?.let {
            Logger.d(Logger.TAG, "Received command to disable self (Nougat+)")
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) disableSelf()
            }.onFailure {
                Logger.e(Logger.TAG, "Failed to disable self: ${it.message}")
            }
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logger.d(
            Logger.TAG,
            "Configuration Changed: FontScale=${newConfig.fontScale}, Orientation=${newConfig.orientation}"
        )
        updateFontScale(newConfig)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Logger.v(Logger.TAG, "onAccessibilityEvent: Received event type=${event.eventType} package=${event.packageName} class=${event.className}")
        handleAccessibilityEvent(event)
    }
    // endregion

    // region Initialization
    private fun initializeService() {
        try {
            Logger.d(Logger.TAG, "init: Setting theme and layout")
            setTheme(R.style.AppTheme)
            initializeShadeName()
            initializeLayoutParams()
            initializeViews()
            registerReceivers()
            registerPreferenceListener()
            initializeNotifications()
            setIslandPosition()
            loadInitialData()
        } catch (e: Exception) {
            Logger.e(Logger.TAG, "Error initializing service", e)
        }
    }

    private fun initializeShadeName() {
        shadeName = runCatching {
            packageManager.getResourcesForApplication(MyHelper.SYS_UI_PACK).run {
                getString(
                    getIdentifier(
                        "accessibility_desc_notification_shade",
                        "string",
                        MyHelper.SYS_UI_PACK
                    )
                )
            }
        }.getOrElse {
            Logger.w(Logger.TAG, "Failed to get shade name, using default")
            "Notification shade."
        }
        Logger.d(Logger.TAG, "Shade Name identified as: $shadeName")
    }

    private fun initializeLayoutParams() {
        Logger.d(Logger.TAG, "init: Configuring WindowManager LayoutParams")
        localLayoutParams = WindowManager.LayoutParams().apply {
            y = (preferenceManager.yPosOfIsland * scaledDensity).toInt()
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            flags = FLAG_NORMAL
            width = (MIN_ONE_CAM_WIDTH * scaledDensity).toInt()
            height = 0
            format = PixelFormat.TRANSLUCENT
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
        }
    }

    private fun initializeViews() {
        Logger.d(Logger.TAG, "init: Inflating and attaching views")
        statusBarView = (LayoutInflater.from(this)
            .inflate(R.layout.layout_notch, null) as IslandParentView).apply {
            setOnSystemUiVisibilityChangeListener { visibility ->
                val isFullScreen = (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
                Logger.d(Logger.TAG, "SystemUI Visibility: isFullScreen=$isFullScreen")
                _inFullScreen.value = isFullScreen
                handleFullScreenChange(isFullScreen)
            }
        }

        statusBarView?.let { view ->
            statusBarParentView = view.findViewById(R.id.statusbar_parent)
            islandParentLayout = view.findViewById<LinearLayout>(R.id.list_parent_layout).apply {
                clipToOutline = true
                layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)
            }
            llControls = view.findViewById<LinearLayoutCompat>(R.id.ll_controls).apply {
                layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)
            }
            islandTopLayout = view.findViewById(R.id.island_top_layout)

            statusBarParentView?.setOnClickListener {
                Logger.d(Logger.TAG, "Click: Status bar parent clicked")
                collapseToAppropriateState()
            }

            setupControlButtons(view)
        }

        runCatching {
            windowManager.addView(statusBarView, localLayoutParams)
            Logger.d(Logger.TAG, "init: View successfully added to WindowManager")
        }.onFailure {
            Logger.e(Logger.TAG, "init: Failed to add view to WindowManager", it)
            showToast("Unfortunately something didn't work. Please try again or contact the developer.")
        }
    }

    private fun setupControlButtons(view: IslandParentView) {
        view.findViewById<View>(R.id.settings_iv_left).apply {
            visibility = View.GONE
            setOnClickListener { toggleControls() }
        }
        view.findViewById<View>(R.id.settings_iv_right).apply {
            visibility = View.GONE
            setOnClickListener { toggleControls() }
        }

        // Set up listener to update button dimensions when list_parent_layout size changes
        islandParentLayout?.viewTreeObserver?.addOnGlobalLayoutListener {
            updateSettingsButtonDimensions()
        }
    }

    */
/**
     * Update settings button dimensions to match list_parent_layout height (making them square)
     *//*

    private fun updateSettingsButtonDimensions() {
        islandParentLayout?.let { layout ->
            val layoutHeight = layout.measuredHeight
            if (layoutHeight > 0) {
                statusBarView?.apply {
                    findViewById<View>(R.id.settings_iv_left)?.layoutParams =
                        (findViewById<View>(R.id.settings_iv_left)?.layoutParams as? RelativeLayout.LayoutParams)?.apply {
                            width = layoutHeight
                            height = layoutHeight
                        }
                    findViewById<View>(R.id.settings_iv_right)?.layoutParams =
                        (findViewById<View>(R.id.settings_iv_right)?.layoutParams as? RelativeLayout.LayoutParams)?.apply {
                            width = layoutHeight
                            height = layoutHeight
                        }
                }
            }
        }
    }

    private fun registerReceivers() {
        Logger.d(Logger.TAG, "init: Registering Broadcast Receivers")
        val systemFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction("android.media.RINGER_MODE_CHANGED")
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        }
        registerReceiver(systemInfoReceiver, systemFilter)

        val notificationFilter = IntentFilter().apply {
            addAction(Utils.FROM_NOTIFICATION_SERVICE + packageName)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(notificationReceiver, notificationFilter)
    }

    private fun registerPreferenceListener() {
        preferenceManager.sharePreference?.registerOnSharedPreferenceChangeListener(
            preferenceListener
        )
    }

    private fun loadInitialData() {
        useGlow = preferenceManager.glow
        useIphoneCallDesign = preferenceManager.getIphoneCall(this)
        Logger.d(Logger.TAG, "init: Loading initial data, Glow enabled: $useGlow, iPhone Call Design: $useIphoneCallDesign")
        serviceScope.launch(Dispatchers.IO) {
            utils.getQSIconList(this@DynamicAccessibilityService, tiles)
            Logger.d(Logger.TAG, "init: QS Icons loaded, count: ${tiles.size}")
        }
    }
    // endregion

    // region Battery & Ringer Handlers
    private fun handleBatteryChanged(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING
        Logger.d(Logger.TAG, "Battery: status=$status, isCharging=$isCharging")

        if (isCharging) {
            val batteryLevel = utils.batteryLevel
            var isNew = false
            val chargingNotification = notifications.findByType(MyHelper.TYPE_CHARGING)
                ?: Notification(MyHelper.TYPE_CHARGING, 0, 0).also {
                    notifications.add(it)
                    isNew = true
                }

            chargingNotification.apply {
                key = MyHelper.TYPE_CHARGING
                tv_text = "$batteryLevel%"
                color = ContextCompat.getColor(this@DynamicAccessibilityService, R.color.green_500)
                tv_title =
                    getString(if (batteryLevel < 100) R.string.charging_txt else R.string.full_txt)
            }
            Logger.d(Logger.TAG, "Battery: Showing charging notification at ${batteryLevel}%")


//            scrollToPosition(notifications.indexOf(chargingNotification))
            updateAdapter()
            if (isNew) {
                mainHandler.post {
                    val position = notifications.indexOf(chargingNotification)
                    Logger.d(
                        Logger.TAG,
                        "Battery: New charging session - Auto expanding   position   $position"
                    )
                    triggerAutoExpansion(chargingNotification)
                }
            }
        } else {
            if (notifications.removeByType(MyHelper.TYPE_CHARGING) != null) {
                Logger.d(Logger.TAG, "Battery: Removed charging notification")
                updateAdapter()
            }
        }
    }

    private fun handleRingerModeChanged() {
        val dndState = utils.getDndState()
        Logger.d(Logger.TAG, "Ringer: Mode changed, DND State=$dndState")
        notifications.removeByType(MyHelper.TYPE_SILENT)

        when (dndState) {
            0 -> addSilentNotification(R.drawable.silent, R.string.silent_txt, R.color.red_500)
            1 -> addSilentNotification(
                R.drawable.vibration_icon,
                R.string.vibrate,
                R.color.purple_400
            )
        }
        scrollToLastItem()
    }

    private fun addSilentNotification(iconRes: Int, titleRes: Int, colorRes: Int) {
        Logger.d(Logger.TAG, "Ringer: Adding silent/vibrate notification")
        notifications.add(Notification(MyHelper.TYPE_SILENT, iconRes, 0).apply {
            key = MyHelper.TYPE_SILENT
            tv_title = getString(titleRes)
            color = ContextCompat.getColor(this@DynamicAccessibilityService, colorRes)
        })
    }

    private fun handleScreenStateChange() {
        val isLocked = keyguardManager.inKeyguardRestrictedInputMode()
        Logger.d(Logger.TAG, "Screen: State Changed, Locked=$isLocked")
        _phoneLocked.value = isLocked

        if (isLocked && !MyPreferenceManager.getShowOnLock(this)) {
            Logger.d(Logger.TAG, "Screen: Locked & Pref says hide -> Closing Island")
            closeSmallIslandNotification(1)
            closeFullNotificationIsland()
        }
    }

    private fun handleFullScreenChange(isFullScreen: Boolean) {
        Logger.d(Logger.TAG, "Display: FullScreen changed to $isFullScreen")
        if (!isFullScreen) {
            showSmallIslandNotification()
        } else if (!MyPreferenceManager.getShowInFullScreen(this)) {
            Logger.d(Logger.TAG, "Display: FullScreen & Pref says hide -> Closing Island")
            closeSmallIslandNotification(2)
        }
    }
    // endregion

    // region Preference Handler
    private fun handlePreferenceChange(key: String) {
        when (key) {
            MyPreferenceManager.KEY_DEFAULT_COLOR,
            MyPreferenceManager.ENABLE_MUSIC_ANIM -> unifiedAdapter?.refreshData("Preference changed: $key")

            MyPreferenceManager.SHOW_IN_FULL_SCREEN,
            MyPreferenceManager.SHOW_IN_LOCK -> showTempBar()

            MyPreferenceManager.CONTROL_ENABLE -> enableControls(preferenceManager.controlEnabled)

            MyPreferenceManager.CAM_COUNT,
            MyPreferenceManager.CAM_POS -> {
                Logger.d(Logger.TAG, "Prefs: Camera Config Changed")
                setIslandPosition()
                showTempBar()
            }

            MyPreferenceManager.Y_POS -> {
                setYPosIsland()
                showTempBar()
            }

            MyPreferenceManager.Y_HEIGHT -> {
                updateIslandHeight()
                showTempBar()
            }

            MyPreferenceManager.CALL_FILTER_PKG -> callPKG = preferenceManager.getCallPkg(this)
            MyPreferenceManager.NOTI_FILTER_PKG -> filterPKG = preferenceManager.getFilterPkg(this)
            MyPreferenceManager.IPHONE_CALL -> useIphoneCallDesign =
                preferenceManager.getIphoneCall(this)

            MyPreferenceManager.ENABLE_GLOW -> useGlow = preferenceManager.glow
            MyPreferenceManager.POWER_MENU_ENABLE -> showSmartMenu(preferenceManager.smartMenu)
            MyPreferenceManager.SELECTED_CONTACTS -> controlToolsFragment.reloadContacts()
            MyPreferenceManager.SELECTED_APPS -> controlToolsFragment.reloadApps()
            MyPreferenceManager.SELECTED_CONTROLS -> controlToolsFragment.reloadControls()
        }
    }

    private fun updateIslandHeight() {
        minIslandHeight = preferenceManager.minHeight
        localLayoutParams?.height = (minIslandHeight * scaledDensity).toInt()
        updateWindowManager()
        islandParentLayout?.requestLayout()
    }

    fun tempHideAll() {
        Logger.d(Logger.TAG, "Action: tempHideAll called")
        if (isShowingControls) hideControls(21)
        if (isShowingFullIsland) closeFullNotificationIsland()
        if (isShowingSmall) closeSmallIslandNotification(3)
    }

    fun collapseToAppropriateState() {
        Logger.d(Logger.TAG, "Action: collapseToAppropriateState called")
        if (isShowingControls) {
            hideControls(30)
        } else if (isShowingFullIsland) {
            closeFullNotificationIsland()
        }
    }
    // endregion

    // region Accessibility Event Handler
    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        event.packageName?.toString()?.let { packageName ->
            currentPackageName = packageName

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && packageName != this.packageName) {
                if (shouldOverrideSystemUI(event)) {
                    // Too noisy to log normally, maybe verbose
                    return
                }

                if (shouldHandlePackage(packageName)) {
                    Logger.d(
                        Logger.TAG,
                        "AccEvent: Handling package specific logic for $packageName"
                    )
                    performClick = false
                    currentPackageName = packageName
                    pendingJob?.cancel()
                    pendingJob = serviceScope.launch {
                        delay(620)
                        handlePendingWindow()
                    }
                }
            }
        }
    }

    private fun shouldOverrideSystemUI(event: AccessibilityEvent): Boolean {
        return overrideStock &&
                MyHelper.SYS_UI_PACK == event.packageName.toString() &&
                event.text.isNotEmpty() &&
                shadeName == event.text.firstOrNull()?.toString()
    }

    private fun shouldHandlePackage(packageName: String): Boolean {
        return performClick && (packageName == MyHelper.SYS_UI_PACK ||
                packageName == VIVO_PACKAGE ||
                packageName == XIAOMI_PACKAGE)
    }

    private suspend fun handlePendingWindow() {
        val targetPackage = currentPackageName ?: return
        Logger.d(Logger.TAG, "AccEvent: Starting search for button in $targetPackage")

        repeat(40) { // Max 40 attempts (2 seconds)
            val nodeInfo = findNodeForPackage(targetPackage)
            if (nodeInfo != null) {
                val clicked = accebNodeUtil.findButtonAndClick(
                    nodeInfo,
                    NodeCheckerForSwitch(accebNodeUtil) as AccebNodeUtil.performActionListener<AccessibilityNodeInfo>,
                    buttonName
                )
                nodeInfo.recycle()
                closeStatusBar()

                if (!clicked) {
                    Logger.w(Logger.TAG, "AccEvent: Button not found/clicked, falling back")
                    hideControls(22)
                    launchIntent?.let { intent ->
                        runCatching { startActivity(intent) }
                            .onFailure {
                                Logger.e(
                                    Logger.TAG,
                                    "AccEvent: Failed to start activity",
                                    it
                                )
                            }
                    } ?: showToast("No setting found")
                } else {
                    Logger.d(Logger.TAG, "AccEvent: Button clicked successfully")
                }
                return
            }
            delay(50)
        }
        Logger.d(Logger.TAG, "AccEvent: Timeout searching for window")
    }

    private fun findNodeForPackage(packageName: String): AccessibilityNodeInfo? {
        return windows.asSequence()
            .filter { it.type != 4 }
            .mapNotNull { window ->
                runCatching { window.root }.getOrNull()?.also { window.recycle() }
            }
            .find { it.packageName == packageName }
    }
    // endregion

    // region Island Position & Display
    private fun setIslandPosition() {
        Logger.d(Logger.TAG, "Layout: Setting Island Position")
        minIslandHeight = preferenceManager.minHeight
        hideControlButtons()
        setupControlButtonListeners()

        startMargin = (preferenceManager.cameraMargin * scaledDensity).toInt()
        cameraPosition = CameraPosition.fromInt(preferenceManager.cameraPos)
        cameraCount = preferenceManager.cameraCount

        updateIslandWidthForCamera()
        setupLayoutMarginsForPosition()
        updateWindowManager()

        showSmartMenu(true)
        showEmptyIsland()
    }

    private fun updateIslandWidthForCamera() {
        val (width, marginValue) = when (cameraCount) {
            0 -> MIN_ZERO_CAM_WIDTH to 0
            1 -> MIN_ONE_CAM_WIDTH to (25 * scaledDensity).toInt()
            2 -> MIN_TWO_CAM_WIDTH to (50 * scaledDensity).toInt()
            3 -> MIN_THREE_CAM_WIDTH to (75 * scaledDensity).toInt()
            else -> MIN_ONE_CAM_WIDTH to 0
        }

        localLayoutParams?.width = (width * scaledDensity).toInt()
        minCameIslandWidth = localLayoutParams?.width ?: 0
        margin = marginValue
        Logger.d(
            Logger.TAG,
            "Layout: updateIslandWidthForCamera: CameraCount=$cameraCount -> Width=$minCameIslandWidth, Margin=$margin"
        )
    }

    private fun setupLayoutMarginsForPosition() {
        Logger.d(Logger.TAG, "Layout: setupLayoutMarginsForPosition: CameraPosition=$cameraPosition, Margin=$margin, StartMargin=$startMargin")
        rvIslandSmall?.attachPagerSnapHelper()

        val rvParams = (rvIslandSmall?.layoutParams as? LinearLayout.LayoutParams) ?: return
        val topParams = (islandTopLayout?.layoutParams as? LinearLayout.LayoutParams) ?: return

        when (cameraPosition) {
            CameraPosition.Left -> {
                rvParams.setMargins(left = margin)
                topParams.setMargins(left = startMargin)
                topParams.gravity = GravityCompat.START
            }

            CameraPosition.Center -> {
                rvParams.setMargins()
                topParams.setMargins()
                topParams.gravity = Gravity.CENTER
            }

            CameraPosition.Right -> {
                rvParams.setMargins(right = margin)
                topParams.setMargins(right = startMargin)
                topParams.gravity = GravityCompat.END
            }
        }

        rvIslandSmall?.layoutParams = rvParams
        islandTopLayout?.layoutParams = topParams
        islandParentLayout?.requestLayout()
        islandTopLayout?.requestLayout()

        serviceScope.launch {
            delay(100)
            localLayoutParams?.gravity = cameraPosition.gravity
            updateWindowManager()
        }
    }

    private fun setYPosIsland() {
        localLayoutParams?.y = (preferenceManager.yPosOfIsland * scaledDensity).toInt()
        updateWindowManager()
    }
    // endregion

    // region Controls
    private fun toggleControls() {
        Logger.d(Logger.TAG, "Action: toggleControls called (Visible=${llControls?.isVisible})")
        if (llControls?.isVisible == true) {
            hideControls(23)
        } else showControls()
    }

    fun showControls() {
        Logger.d(Logger.TAG, "Action: showControls called")
        unifiedAdapter?.refreshData("Show controls")
        isShowingControls = true

        // Animate window expansion with smooth interpolation
        val targetWidth = if (isPortrait) getDpToPx(300) else getDpToPx(400)

        // Use MATCH_PARENT for window to capture outside touches
        animateWindowSize(
            targetHeight = WindowManager.LayoutParams.MATCH_PARENT,
            targetWidth = WindowManager.LayoutParams.MATCH_PARENT,
            duration = 350L
        )

        // Animate ISDLAND layout width explicitly (don't let it go full screen)
        val currentIslandWidth = islandParentLayout?.width ?: minCameIslandWidth
        if (currentIslandWidth != targetWidth) {
             ValueAnimator.ofInt(currentIslandWidth, targetWidth).apply {
                duration = 350L
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    islandParentLayout?.layoutParams = topLayoutParams.apply {
                        width = animator.animatedValue as Int
                        height = getDpToPx(minIslandHeight)
                    }
                }
                start()
            }
        } else {
             islandParentLayout?.layoutParams = topLayoutParams.apply {
                 width = targetWidth
                 height = getDpToPx(minIslandHeight)
             }
        }

        llControls?.apply {
            // Set initial state for animation
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f

            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                width = targetWidth
                height = WindowManager.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER_HORIZONTAL
            }
            visibility = View.VISIBLE
            if (childCount == 0) addView(controlToolsFragment.view)

            // Smooth fade in + scale animation with spring
            post {
                animateViewIn(this, 50L)
            }
        }

        controlToolsFragment.showViews()
        controlsActionHandler.initializeViews(controlToolsFragment.controlsList)
    }

    fun hideControls(fromWhere: Int) {
        if (isClosingControls) return
        Logger.d(Logger.TAG, "Action: hideControls called  fromWhere   $fromWhere")
        isClosingControls = true

        controlToolsFragment.hideViews()
        unifiedAdapter?.refreshData("Hide controls from: $fromWhere")
        isClosingFull = true

        // Smoothly animate controls out
        llControls?.let { controls ->
            controls.animate()?.cancel()
            animateViewOut(controls, 0L) {
                // Callback after animation completes
                setFullIslandMargin(false)

                // Animate window size back to small
                val targetHeight = (minIslandHeight * scaledDensity).toInt()
                val targetWidth = minCameIslandWidth

                val isWindowFull = localLayoutParams?.width == WindowManager.LayoutParams.MATCH_PARENT

                if (isWindowFull) {
                     // Custom animation for full window mode
                      val currentIsland = islandParentLayout?.width ?: targetWidth
                      ValueAnimator.ofInt(currentIsland, targetWidth).apply {
                           duration = 300L
                           interpolator = DecelerateInterpolator()
                           addUpdateListener { animator ->
                               val animatedWidth = animator.animatedValue as Int

                               islandParentLayout?.layoutParams = topLayoutParams.apply {
                                   width = animatedWidth
                               }
                               // Update window height if needed (though existing animateWindowSize logic handled it, we do it here)
                               localLayoutParams?.height = targetHeight
                               updateWindowManager()
                           }
                           doOnEnd {
                                localLayoutParams?.width = targetWidth
                                updateWindowManager()

                                serviceScope.launch {
                                    delay(300)
                                    closedIsland()
                                    delay(200)
                                    isClosingFull = false
                                }
                           }
                           start()
                      }
                } else {
                    animateWindowSize(targetHeight, targetWidth, 300L)
                    serviceScope.launch {
                        delay(300)
                        closedIsland()
                        delay(200)
                        isClosingFull = false
                    }
                }
            }
        } ?: run {
            // Fallback if llControls is null
            setFullIslandMargin(false)
            serviceScope.launch {
                delay(800)
                closedIsland()
                delay(200)
                isClosingFull = false
            }
        }
    }

    private fun closedIsland() {
        Logger.d(Logger.TAG, "Action: Finalizing closedIsland state")
        llControls?.apply {
            layoutParams = (layoutParams as? LinearLayout.LayoutParams)?.apply {
                width = 0
                height = 0
            }
            visibility = View.GONE
        }

        localLayoutParams?.apply {
            height = getDpToPx(minIslandHeight)
            flags = FLAG_NORMAL
        }

        // Restore island to small state if there are notifications
        if (notifications.isNotEmpty()) {
            localLayoutParams?.width = minCameIslandWidth
            islandParentLayout?.layoutParams = topLayoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
        } else {
            // Only close the island if there are no notifications
            islandParentLayout?.layoutParams = topLayoutParams.apply {
                val size = (utils.convertDpToPixel(
                    1f,
                    this@DynamicAccessibilityService
                ) * scaledDensity).toInt()
                width = size
                height = size
            }
        }

        updateWindowManager()

        isShowingControls = false
        isClosingControls = false
    }

    private fun enableControls(enabled: Boolean) {
        Logger.d(Logger.TAG, "Config: enableControls=$enabled")
        isControlEnabled = enabled
        if (enabled) showTempBar() else closeSmallIslandNotification(4)
    }
    // endregion

    // region Island Visibility
    fun showSmallIslandNotification() {
        serviceScope.launch {
            delay(1000)

            if (notifications.isEmpty()) {
                if (isShowingFullIsland) closeFullNotificationIsland()
                // Do not close small island, show empty state instead
//                if (isShowingSmall) closeSmallIslandNotification(5)
//                return@launch
            }

            if (isShowingSmall) {
                showIslandViews()
                return@launch
            }

            if (!isControlEnabled) {
                Logger.d(Logger.TAG, "ShowSmall: Aborted (Controls disabled)")
                return@launch
            }
            if (_phoneLocked.value && !MyPreferenceManager.getShowOnLock(this@DynamicAccessibilityService)) {
                Logger.d(Logger.TAG, "ShowSmall: Aborted (Phone locked & Pref hidden)")
                return@launch
            }
            if (isShowingFullIsland) return@launch
            if (_inFullScreen.value && !MyPreferenceManager.getShowInFullScreen(this@DynamicAccessibilityService)) {
                Logger.d(Logger.TAG, "ShowSmall: Aborted (Fullscreen & Pref hidden)")
                return@launch
            }

            Logger.d(Logger.TAG, "ShowSmall: Displaying small island")
            displaySmallIsland()
        }
    }

    private fun showIslandViews() {
        islandTopLayout?.visibility = View.VISIBLE
        rvIslandSmall?.visibility = View.VISIBLE
        showGlowAnimation()
        showSmartMenu(isSmartMenuEnabled)
    }

    private fun displaySmallIsland() {
        showSmartMenu(isSmartMenuEnabled)
        showGlowAnimation()

        islandTopLayout?.visibility = View.VISIBLE

        // Show even if empty (placeholder)
//        if (notifications.isEmpty()) {
//            islandParentLayout?.layoutParams = topLayoutParams.apply { width = 0 }
//            return
//        }

        localLayoutParams?.apply {
            height = (preferenceManager.minHeight * scaledDensity).toInt()
            width = minCameIslandWidth
        }
        updateWindowManager()

        islandParentLayout?.layoutParams = topLayoutParams.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        // Smooth fade in animation for small island - no scale to avoid jitter
        rvIslandSmall?.apply {
            alpha = 0f
            visibility = View.VISIBLE

            // Simple fade with spring timing
            animate()
                .alpha(1f)
                .setDuration(250L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        unifiedAdapter?.takeIf { it.itemCount >= 1 }?.let {
//            rvIslandSmall?.scrollToPosition(it.itemCount - 1)
        }
    }

    fun showFullIslandNotification(position: Int) {
        if (position < 0 || position >= notifications.size) {
            Logger.e(Logger.TAG, "showFullIslandNotification: Invalid position=$position")
            return
        }

        val notification = notifications[position] ?: run {
            Logger.e(
                Logger.TAG,
                "showFullIslandNotification: Null notification at position=$position"
            )
            return
        }

        Logger.d(
            Logger.TAG,
            "Action: showFullIslandNotification at position=$position for ${notification.app_name}"
        )
        currentNotification = notification
        currentIndex = position

        when {
            isShowingControls -> hideControls(24)
            isClosingFull -> return
            notification.isLocal -> {
                Logger.d(Logger.TAG, "ShowFull: Aborted (Local notification)")
                return
            }

            else -> displayFullIsland(position)
        }
    }

    private fun displayFullIsland(position: Int) {
        if (position < 0 || position >= notifications.size) {
            Logger.e(Logger.TAG, "displayFullIsland: Invalid position=$position")
            return
        }

        val notification = notifications[position] ?: run {
            Logger.e(Logger.TAG, "displayFullIsland: Null notification at position=$position")
            return
        }

        Logger.d(Logger.TAG, "ShowFull: Displaying Full Island UI at position=$position")

        // Hide settings buttons and controls when expanding
        hideSettingsButtons()
        if (isShowingControls) {
            hideControls(25)
        }

        // Cancel any existing animations first
        // Legacy code hiding rvIslandSmall removed as it's now the unified view
        // rvIslandSmall?.animate()?.cancel()
        // islandParentLayout?.animate()?.cancel()
        // rvIslandSmall?.visibility = View.VISIBLE

        // Ensure it's visible
        rvIslandSmall?.visibility = View.VISIBLE
        rvIslandSmall?.alpha = 1f

        islandTopLayout?.visibility = View.VISIBLE

        if (isShowingFullIsland) {
            // Already showing full island, just scroll to the position
            currentBigIslandNotification = notification
            scrollToNotificationSafely(position)
            return
        }

        if (notification.template == "MediaStyle") {
            Logger.d(Logger.TAG, "ShowFull: Starting Media Update Job")
            startMediaUpdateJob()
        }

        setFullIslandMargin(true)
        setKeyboardFlag(true)

        // Animate window expansion
        val targetIslandWidth = (MAX_ISLAND_WIDTH * scaledDensity).toInt()

        // Use MATCH_PARENT for window to capture outside clicks
        animateWindowSize(
            targetHeight = WindowManager.LayoutParams.MATCH_PARENT,
            targetWidth = WindowManager.LayoutParams.MATCH_PARENT,
            duration = 350L
        )

        // Animate island layout width specifically
        val currentIslandWidth = islandParentLayout?.width ?: minCameIslandWidth
        if (currentIslandWidth != targetIslandWidth) {
            ValueAnimator.ofInt(currentIslandWidth, targetIslandWidth).apply {
                duration = 350L
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    islandParentLayout?.layoutParams = topLayoutParams.apply {
                        width = animator.animatedValue as Int
                    }
                }
                start()
            }
        } else {
            islandParentLayout?.layoutParams = topLayoutParams.apply {
                width = targetIslandWidth
            }
        }


        // Track current notification
        currentBigIslandNotification = notification
        unifiedAdapter?.refreshData("Show full island for notification: ${notification.key}")

        // Animate island expansion with spring effect
        islandParentLayout?.let { layout ->
            // Set initial state
            layout.scaleX = 0.96f
            layout.scaleY = 0.96f
            layout.alpha = 0.85f

            // Start with 100dp default height as requested
            val initialHeight = (100 * scaledDensity).toInt()
            layout.layoutParams = topLayoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = initialHeight
            }

            // Wait for layout to settle, then animate to WRAP_CONTENT
            layout.postDelayed({
                // Expand animations
                createSpringAnimation(layout, SpringAnimation.SCALE_X, 1f).start()
                createSpringAnimation(layout, SpringAnimation.SCALE_Y, 1f).start()
                layout.animate()
                    .alpha(1f)
                    .setDuration(250L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()

                // Animate height to wrapped content
                // We use a measure pass to determine target height
                layout.measure(
                    View.MeasureSpec.makeMeasureSpec(layout.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val targetHeight = layout.measuredHeight

                if (targetHeight > initialHeight) {
                    val animator = ValueAnimator.ofInt(initialHeight, targetHeight)
                    animator.duration = 300L
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { animation ->
                        layout.layoutParams.height = animation.animatedValue as Int
                        layout.requestLayout()
                    }
                    animator.doOnEnd {
                        // Switch to wrap content for final state
                        layout.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                        layout.requestLayout()
                    }
                    animator.start()
                } else {
                    layout.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                    layout.requestLayout()
                }
            }, 50L)
        }

        // Scroll to the clicked notification position after mode switch
        rvIslandSmall?.postDelayed({
            scrollToNotificationSafely(position)
        }, 100L)
    }


    */
/**
     * Safely scroll to a notification in the big island RecyclerView.
     * Waits for layout completion before scrolling to avoid race conditions.
     *//*

    private fun scrollToNotificationSafely(index: Int) {
        Logger.d(
            Logger.TAG,
            "scrollToNotificationSafely: 1  Scrolling to index=$index    rvIslandSmall       ${rvIslandSmall == null}"
        )

        val recyclerView = rvIslandSmall ?: run {
            Logger.d(
                Logger.TAG,
                "scrollToNotificationSafely: 2  Scrolling to index=$index    rvIslandSmall       ${rvIslandSmall == null}"
            )
            Logger.e(Logger.TAG, "scrollToNotificationSafely: rvIslandSmall is null")
            return
        }
        Logger.d(
            Logger.TAG,
            "scrollToNotificationSafely: 3  Scrolling to index=$index    rvIslandSmall       ${rvIslandSmall == null}"
        )

        // Validate index
        if (index < 0 || index >= notifications.size) {
            Logger.d(
                Logger.TAG,
                "scrollToNotificationSafely: 4  Scrolling to index=$index    rvIslandSmall       ${rvIslandSmall == null}"
            )
            Logger.e(
                Logger.TAG,
                "scrollToNotificationSafely: Invalid index=$index, listSize=${notifications.size}"
            )
            return
        }

        // Set flag to prevent unwanted height animations during scroll
        isProgrammaticScroll = true

        // Use addOnLayoutChangeListener for more reliable scroll timing
        // This ensures the RecyclerView has finished laying out items before scrolling
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                recyclerView.removeOnLayoutChangeListener(this)

                // Double-check index is still valid
                if (index >= 0 && index < (unifiedAdapter?.itemCount ?: 0)) {
                    Logger.d(
                        Logger.TAG,
                        "scrollToNotificationSafely: Layout complete, scrolling to index=$index"
                    )
                    recyclerView.scrollToPosition(index)

                    // Reset flag after scroll completes
                    recyclerView.postDelayed({
                        isProgrammaticScroll = false
                        Logger.d(
                            Logger.TAG,
                            "scrollToNotificationSafely: Scroll complete, reset flag"
                        )
                    }, 150L)
                } else {
                    isProgrammaticScroll = false
                    Logger.e(
                        Logger.TAG,
                        "scrollToNotificationSafely: Index became invalid after layout"
                    )
                }
            }
        })

        // Fallback: also use post() in case layout change doesn't fire
        recyclerView.post {
            if (isProgrammaticScroll && index >= 0 && index < (unifiedAdapter?.itemCount ?: 0)) {
                Logger.d(Logger.TAG, "scrollToNotificationSafely: Fallback scroll to index=$index")
                recyclerView.scrollToPosition(index)
            }
        }
    }

    fun closeFullNotificationIsland() {
        Logger.d(Logger.TAG, "Action: closeFullNotificationIsland called")
        mediaUpdateJob?.cancel()
        setKeyboardFlag(false)

        // Cancel any active animations first
        rvIslandSmall?.animate()?.cancel()
        islandParentLayout?.animate()?.cancel()
        heightAnimator?.cancel()
        heightAnimator = null

        // Reset state tracking
        isAnimatingHeight = false
        currentBigIslandNotification = null
        isProgrammaticScroll = false

        // Collapse all items in the unified adapter - this triggers the collapse animation
        unifiedAdapter?.collapseAll()
    }

    private fun transitionToSmallIsland() {
        Logger.d(Logger.TAG, "Action: transitionToSmallIsland")
        setFullIslandMargin(false)

        // Check if we should show small island or close completely
        if (notifications.isEmpty()) {
            closeSmallIslandNotification(6)
            return
        }

        // Smoothly animate window size change
        // Smoothly animate window size change
        val isWindowFull = localLayoutParams?.width == WindowManager.LayoutParams.MATCH_PARENT
        val currentWidth = if (isWindowFull) (islandParentLayout?.width ?: minCameIslandWidth) else (localLayoutParams?.width ?: minCameIslandWidth)
        val targetWidth = minCameIslandWidth

        // Animate width change smoothly
        ValueAnimator.ofInt(currentWidth, targetWidth).apply {
            duration = 300L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val animatedWidth = animator.animatedValue as Int

                if (isWindowFull) {
                    // Update internal layout width, keep window full
                    islandParentLayout?.layoutParams = topLayoutParams.apply {
                        width = animatedWidth
                    }
                    // Ensure height is correct
                    localLayoutParams?.height = (minIslandHeight * scaledDensity).toInt()
                    updateWindowManager()
                } else {
                    // Update window width directly
                     localLayoutParams?.apply {
                        width = animatedWidth
                        height = (minIslandHeight * scaledDensity).toInt()
                    }
                    updateWindowManager()
                }
            }
            doOnEnd {
                if (isWindowFull) {
                     // Snap window to small now
                     localLayoutParams?.apply {
                        width = minCameIslandWidth
                     }
                     updateWindowManager()
                }
                // After width animation completes, prepare small island
                prepareSmallIslandAppearance()
            }
            start()
        }
    }

    private fun prepareSmallIslandAppearance() {
        // Show settings buttons again when returning to small island
        showSmartMenu(isSmartMenuEnabled)

        // Reset island parent layout to full size for small island
        islandParentLayout?.layoutParams = topLayoutParams.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        // Reset scale
        islandParentLayout?.apply {
            scaleX = 1f
            scaleY = 1f
        }

        // Prepare small island for fade in
        rvIslandSmall?.apply {
            alpha = 0f
            visibility = View.VISIBLE

            // Scroll to correct position
            unifiedAdapter?.takeIf { it.itemCount > currentIndex }?.let {
//                scrollToPosition(currentIndex)
            }

            // Fade in smoothly
            postDelayed({
                animate()
                    .alpha(1f)
                    .setDuration(250L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, 50L)
        }
    }

    fun closeSmallIslandNotification(fromWhere: Int) {
        Logger.d(Logger.TAG, "Action: closeSmallIslandNotification called  $fromWhere")
        islandParentLayout?.layoutParams = topLayoutParams.apply {
            val size = (utils.convertDpToPixel(
                1f,
                this@DynamicAccessibilityService
            ) * scaledDensity).toInt()
            width = size
            height = size
        }

        setKeyboardFlag(false)

        serviceScope.launch {
            delay(500)
            minimizeIsland()
        }
    }

    private fun minimizeIsland() {
        Logger.d(Logger.TAG, "Action: minimizeIsland (Final Step)")
        runCatching {
            val size = (utils.convertDpToPixel(1f, this) * scaledDensity).toInt()
            localLayoutParams?.apply {
                height = size
                width = size
            }
            updateWindowManager()
            islandTopLayout?.visibility = View.GONE
        }.onFailure {
            Logger.e(Logger.TAG, "Error minimizing island", it)
        }
    }

    private fun showEmptyIsland() {
        Logger.d(Logger.TAG, "Action: showEmptyIsland")
        islandTopLayout?.visibility = View.VISIBLE
        localLayoutParams?.apply {
            height = getDpToPx(preferenceManager.minHeight)
            width = minWidth
        }
        updateWindowManager()

        islandParentLayout?.layoutParams = topLayoutParams.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    fun showTempBar() {
        Logger.d(Logger.TAG, "Action: showTempBar called")
        runCatching {
            islandTopLayout?.visibility = View.VISIBLE
            localLayoutParams?.apply {
                height = (preferenceManager.minHeight * scaledDensity).toInt()
                width = minCameIslandWidth
            }
            updateWindowManager()

            islandParentLayout?.layoutParams = topLayoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            mainHandler.removeCallbacks(autoHideRunnable)
            mainHandler.postDelayed(autoHideRunnable, 3000)
            showGlowAnimation()
        }.onFailure {
            Logger.e(Logger.TAG, "Error showing temp bar", it)
        }
    }

    private val autoHideRunnable = Runnable {
        if (notifications.isEmpty()) {
            Logger.d(Logger.TAG, "Action: Auto-hiding Temp Bar")
            closeSmallIslandNotification(7)
        }
    }
    // endregion

    // region Smart Menu
    fun showSmartMenu(enabled: Boolean) {
        Logger.d(Logger.TAG, "SmartMenu: enabled=$enabled")
        isSmartMenuEnabled = enabled

        if (enabled) {
            showEmptyIsland()
        } else if (notifications.isEmpty()) {
            closeSmallIslandNotification(8)
        }

        val showLeft = cameraPosition == CameraPosition.Right && preferenceManager.smartMenu
        val showRight = cameraPosition != CameraPosition.Right && preferenceManager.smartMenu

        statusBarView?.apply {
            // Smooth fade in for settings buttons
            findViewById<View>(R.id.settings_iv_left)?.apply {
                if (showLeft) {
                    if (visibility != View.VISIBLE || alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(200L)
                            .start()
                    }
                } else {
                    visibility = View.GONE
                }
            }

            findViewById<View>(R.id.settings_iv_right)?.apply {
                if (showRight) {
                    if (visibility != View.VISIBLE || alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(200L)
                            .start()
                    }
                } else {
                    visibility = View.GONE
                }
            }
        }

        unifiedAdapter?.submitNotifications(notifications.filterNotNull())
    }
    // endregion

    // region Notifications
    private fun initializeNotifications() {
        Logger.d(Logger.TAG, "init: Setting up Unified Notification Adapter")
        rvIslandSmall = statusBarView?.findViewById(R.id.rv_island_small)
        // rvIslandBig is no longer needed - unified adapter handles both states

        unifiedAdapter = UnifiedNotificationAdapter(
            this,
            rvIslandSmall,
            object : NotificationListener {
                override fun onItemClicked(notification: Notification?) {
                    Logger.d(Logger.TAG, "Click: Notification Item Clicked")
                    hideControls(25)
                }

                override fun onItemClicked(notification: Notification, i: Int) {
                    Logger.d(
                        Logger.TAG,
                        "Click: Notification Item Clicked at position $i: ${notification.app_name}"
                    )
                    currentIndex = i
                    Logger.d(Logger.TAG, "Click: Notification Item Clicked at position $i")

                    // User interaction: cancel any pending auto-collapse
                    autoCollapseJob?.cancel()

                    // Expand the notification item with animation
                    expandNotificationItem(i)
                }

                override fun onItemRemoved(notification: Notification?) {
                    notification?.let {
                        Logger.d(Logger.TAG, "Action: Removing notification: ${it.app_name}")
                        notifications.removeAll { n -> n?.key == it.key }
                        updateAdapter()
                    }
                }
            },
            onDisplayModeChanged = { isBigMode ->
                // Callback when display mode changes
                Logger.d(Logger.TAG, "Display mode changed: isBigMode=$isBigMode")
                if (isBigMode) {
                    animateParentToExpanded()
                } else {
                    animateParentToCollapsed()
                }
            },
            onItemBound = { position, notification ->
                // Callback when item binding completes - animate layout to adapt to new size
                Logger.d(Logger.TAG, "Item bound at position $position: ${notification.key}")

                animateIslandToContentSize()
            }
        ).also { adapter ->
            rvIslandSmall?.apply {
                this.adapter = adapter
                setHasFixedSize(false) // Items have variable heights when expanded
                addItemDecoration(
                    ItemOffsetDecoration2(
                        this@DynamicAccessibilityService,
                        R.dimen.small_margin
                    )
                )
                layoutManager = LinearLayoutManager(
                    this@DynamicAccessibilityService,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                attachPagerSnapHelper()

                // Add scroll listener to trigger height updates when scrolling settles
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        // When scroll state becomes IDLE, the snap has completed
                        // and we're settled at the ideal position
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Logger.d(Logger.TAG, "Scroll: RecyclerView settled at ideal position")

                            // Update current index based on visible item
                            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                            val firstPos = layoutManager?.findFirstVisibleItemPosition() ?: -1
                            if (firstPos != -1) {
                                currentIndex = firstPos
                                Logger.d(
                                    Logger.TAG,
                                    "Scroll: Updated currentIndex to $currentIndex"
                                )
                            }

                            // Trigger adapter refresh to update heights
                            adapter.notifyDataSetChanged()

                            // Also trigger height animation for the parent layout
                            animateIslandToContentSize()
                        }
                    }
                })
            }
            // Submit initial notification list
            adapter.submitNotifications(notifications.filterNotNull())
        }
    }

    */
/**
     * Expand a notification item with iOS-like animation
     *//*

    private fun expandNotificationItem(position: Int) {
        if (position < 0 || position >= notifications.size) {
            Logger.e(Logger.TAG, "expandNotificationItem: Invalid position=$position")
            return
        }

        val notification = notifications[position] ?: run {
            Logger.e(Logger.TAG, "expandNotificationItem: Null notification at position=$position")
            return
        }

        Logger.d(
            Logger.TAG,
            "Action: expandNotificationItem at position=$position for ${notification.app_name}"
        )
        currentNotification = notification
        currentIndex = position

        when {
            isShowingControls -> hideControls(24)
            isClosingFull -> return
            notification.isLocal && !notification.type.equals(
                MyHelper.TYPE_CHARGING,
                ignoreCase = true
            ) -> {
                Logger.d(Logger.TAG, "Expand: Aborted (Local notification)")
                return
            }
        }

        // Hide settings buttons when expanding
        hideSettingsButtons()

        if (notification.template == "MediaStyle") {
            Logger.d(Logger.TAG, "Expand: Starting Media Update Job")
            startMediaUpdateJob()
        }

        setKeyboardFlag(true)

        // Expand the item in the adapter
        unifiedAdapter?.expandItem(position)

        // Ensure we scroll to the expanded item
        scrollToNotificationSafely(position)
    }

    */
/**
     * Animate parent container to expanded state
     *//*

    private fun animateParentToExpanded() {
        Logger.d(Logger.TAG, "animateParentToExpanded")

        // Animate window width expansion
        val targetWidth = (MAX_ISLAND_WIDTH * scaledDensity).toInt()
        animateWindowSize(
            targetHeight = WindowManager.LayoutParams.MATCH_PARENT,
            targetWidth = targetWidth,
            duration = 350L
        )

        // Animate island parent layout
        islandParentLayout?.let { layout ->
            layout.scaleX = 0.96f
            layout.scaleY = 0.96f
            layout.alpha = 0.85f

            layout.layoutParams = topLayoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            layout.postDelayed({
                createSpringAnimation(layout, SpringAnimation.SCALE_X, 1f).start()
                createSpringAnimation(layout, SpringAnimation.SCALE_Y, 1f).start()
                layout.animate()
                    .alpha(1f)
                    .setDuration(250L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, 50L)
        }
    }

    */
/**
     * Animate parent container to collapsed state
     *//*

    private fun animateParentToCollapsed() {
        Logger.d(Logger.TAG, "animateParentToCollapsed")

        mediaUpdateJob?.cancel()
        setKeyboardFlag(false)

        // Reset state tracking
        isAnimatingHeight = false
        currentBigIslandNotification = null
        isProgrammaticScroll = false

        // Animate window size back to small
        val targetWidth = minCameIslandWidth

        val isWindowFull = localLayoutParams?.width == WindowManager.LayoutParams.MATCH_PARENT
        val currentWidth = if (isWindowFull) (islandParentLayout?.width ?: targetWidth) else (localLayoutParams?.width ?: targetWidth)

        ValueAnimator.ofInt(currentWidth, targetWidth).apply {
            duration = 300L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val animatedWidth = animator.animatedValue as Int

                if (isWindowFull) {
                    // Update internal layout width
                    islandParentLayout?.layoutParams = topLayoutParams.apply {
                        width = animatedWidth
                    }
                    // Ensure window height is correct
                    localLayoutParams?.height = (minIslandHeight * scaledDensity).toInt()
                    updateWindowManager()
                } else {
                     localLayoutParams?.apply {
                        width = animatedWidth
                        height = (minIslandHeight * scaledDensity).toInt()
                    }
                    updateWindowManager()
                }
            }
            doOnEnd {
                if (isWindowFull) {
                    localLayoutParams?.apply {
                        width = targetWidth
                    }
                    updateWindowManager()
                }

                // Show settings buttons again
                showSmartMenu(isSmartMenuEnabled)

                // Reset island parent layout
                islandParentLayout?.layoutParams = topLayoutParams.apply {
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                }

                islandParentLayout?.apply {
                    scaleX = 1f
                    scaleY = 1f
                }
            }
            start()
        }
    }

    // Store current animator to allow cancellation
    private var heightAnimator: ValueAnimator? = null

    */
/**
     * Animate island parent layout to match the actual content size of the current notification
     * This is called after holder.bind() completes to ensure the layout adapts to new content
     *//*

    private fun animateIslandToContentSize() {
        // Only animate in BIG mode when full island is showing
        if (unifiedAdapter?.isBigMode != true || !isShowingFullIsland) {
            return
        }

        islandParentLayout?.let { layout ->
            // Cancel any ongoing animation to allow immediate height change
            heightAnimator?.cancel()

            // Post to ensure view is laid out
            layout.post {
                // Force layout to use a fixed height temporarily to ensure proper remeasurement
                // This prevents WRAP_CONTENT from "sticking" to the previous size
                val currentHeight = layout.height
                if (currentHeight > 0) {
                    layout.layoutParams.height = currentHeight
                    layout.requestLayout()
                }

                // Post again to ensure the fixed height is applied before measuring
                layout.post {
                    // Measure the actual content height
                    layout.measure(
                        View.MeasureSpec.makeMeasureSpec(layout.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    val targetHeight = layout.measuredHeight
                    val measuredCurrentHeight = layout.height

                    // Only animate if there's a significant difference
                    if (abs(targetHeight - measuredCurrentHeight) > 10) {
                        Logger.d(
                            Logger.TAG,
                            "animateIslandToContentSize: Animating from $measuredCurrentHeight to $targetHeight"
                        )
                        isAnimatingHeight = true

                        heightAnimator = ValueAnimator.ofInt(measuredCurrentHeight, targetHeight)
                        heightAnimator?.apply {
                            duration = 250L
                            interpolator = DecelerateInterpolator(1.5f)
                            addUpdateListener { animation ->
                                layout.layoutParams.height = animation.animatedValue as Int
                                layout.requestLayout()
                            }
                            doOnEnd {
                                // Set to WRAP_CONTENT for final state
                                layout.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                                layout.requestLayout()
                                isAnimatingHeight = false
                                heightAnimator = null
                                Logger.d(
                                    Logger.TAG,
                                    "animateIslandToContentSize: Animation complete"
                                )
                            }
                            start()
                        }
                    } else {
                        Logger.d(
                            Logger.TAG,
                            "animateIslandToContentSize: No significant height change (current=$measuredCurrentHeight, target=$targetHeight)"
                        )
                        // Even if no significant change, ensure WRAP_CONTENT is set
                        layout.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                        layout.requestLayout()
                        isAnimatingHeight = false
                    }
                }
            }
        }
    }


    fun updateNotificationList(intent: Intent) {
        val notificationData = parseNotificationIntent(intent) ?: return
        var newKey: String? = null

        if (notificationData.isAdded) {
            Logger.d(Logger.TAG, "NotiUpdate: Added/Updated ${notificationData.packageName}")
            newKey = handleAddedNotification(notificationData)
        } else {
            Logger.d(Logger.TAG, "NotiUpdate: Removed ${notificationData.packageName}")
            handleRemovedNotification(notificationData.key)
        }

        if (!isFilterPkgFound(notificationData.packageName)) {
            handleNotificationDisplay(notificationData)
        } else {
            Logger.d(Logger.TAG, "NotiUpdate: Filtered out ${notificationData.packageName}")
        }

        // Post adapter updates to main thread to prevent race conditions
        // Use submitList        // Post adapter updates to main thread to prevent race conditions
        mainHandler.post {
            Logger.d(
                Logger.TAG,
                "NotiUpdate: Updating adapter with ${notifications.size} notifications. Auto-scroll key: $newKey"
            )
            updateAdapter(newKey)
//            rvIslandSmall?.smoothScrollToPosition(notifications.indexOfFirst { it?.key == newKey })
        }
    }

    private fun parseNotificationIntent(intent: Intent): NotificationData? {
        return runCatching {
            NotificationData(
                packageName = intent.getStringExtra("package") ?: return null,
                id = intent.getStringExtra("id") ?: "",
                key = intent.getStringExtra("key") ?: intent.getStringExtra("id") ?: "",
                groupKey = intent.getStringExtra("group_key") ?: intent.getStringExtra("id") ?: "",
                title = intent.getCharSequenceExtra("title") ?: "",
                text = intent.getCharSequenceExtra("text") ?: "",
                bigText = intent.getCharSequenceExtra("bigText") ?: "",
                appName = intent.getStringExtra("appName"),
                category = intent.getStringExtra("category"),
                template = intent.getStringExtra("template") ?: "",
                tag = intent.getStringExtra("tag") ?: "",
                color = getIconColor(intent.getIntExtra("color", -1)),
                postTime = intent.getLongExtra("postTime", System.currentTimeMillis()),
                isAdded = intent.getBooleanExtra("isAdded", true),
                isClearable = intent.getBooleanExtra("isClearable", true),
                isOngoing = intent.getBooleanExtra("isOngoing", false),
                isGroup = intent.getBooleanExtra("isGroup", false),
                isAppGroup = intent.getBooleanExtra("isAppGroup", false),
                isGroupConversation = intent.getBooleanExtra("isGroupConversation", false),
                progress = intent.getIntExtra("progress", 0),
                progressMax = intent.getIntExtra("progressMax", 0),
                progressIndeterminate = intent.getBooleanExtra("progressIndeterminate", false),
                showChronometer = intent.getBooleanExtra("showChronometer", false),
                icon = utils.getBitmapFromByteArray(intent.getByteArrayExtra("icon")),
                largeIcon = utils.getBitmapFromByteArray(intent.getByteArrayExtra("largeIcon")),
                picture = utils.getBitmapFromByteArray(intent.getByteArrayExtra("picture")),
                pendingIntent = intent.getParcelableExtra("pendingIntent"),
                actions = runCatching { intent.getParcelableArrayListExtra<ActionParsable>("actions") }.getOrNull(),
                uId = intent.getIntExtra("uId", 0),
                substName = intent.getCharSequenceExtra("substName"),
                subText = intent.getCharSequenceExtra("subText"),
                titleBig = intent.getCharSequenceExtra("titleBig"),
                summaryText = intent.getCharSequenceExtra("summaryText"),
                infoText = intent.getCharSequenceExtra("info_text")
            )
        }.getOrElse {
            Logger.e(Logger.TAG, "Error parsing notification intent", it)
            null
        }
    }

    private fun handleAddedNotification(data: NotificationData): String? {
        val notification = data.toNotification().apply {
            useIphoneCallDesign = this@DynamicAccessibilityService.useIphoneCallDesign
        }

        var addedKey: String? = null

        val existingIndex =
            notifications.indexOfFirst { it?.isLocal == false && it.groupKey == data.groupKey }

        if (existingIndex != -1) {
            val existing = notifications[existingIndex] ?: return null
            existing.apply {
                isClearable = data.isClearable
                progress = data.progress
                progressMax = data.progressMax
                progressIndeterminate = data.progressIndeterminate
            }

            if (data.groupKey == data.key || data.template == "InboxStyle" || data.tag.lowercase()
                    .contains("summary")
            ) {
                updateNotificationItem(notification, existingIndex)
            } else if (!isSameItem(notification)) {
                existing.keyMap[data.key] = notification
            }
        } else {
            notifications.add(notification)
            // Manual scroll removed - handled by adapter via submitNotifications key
            // scrollToLastItem()

            addedKey = notification.key

            // New notification added - trigger auto-expansion logic
            triggerAutoExpansion(notification)
        }
        return addedKey
    }

    private fun triggerAutoExpansion(notification: Notification) {
        // If island is fully expanded (by user), don't interrupt
        if (isShowingFullIsland && autoCollapseJob == null) {
            Logger.d(Logger.TAG, "AutoExpand: Skipped (User is interacting with expanded island)")
            return
        }

        // Proceed if island is collapsed or was auto-expanded
        if (!isShowingFullIsland || autoCollapseJob != null) {
            val index = notifications.indexOf(notification)
            if (index != -1) {
                Logger.d(
                    Logger.TAG,
                    "AutoExpand: Expanding new notification: ${notification.key}   index   $index"
                )

                // Cancel previous job
                autoCollapseJob?.cancel()

                // Expand immediately
                expandNotificationItem(index)
                currentExpandedNotificationKey = notification.key

                // Schedule auto-collapse ONLY for non-call notifications
                // Call notifications should stay expanded until user interaction
                val isCallNotification = notification.category.equals(NotificationCompat.CATEGORY_CALL, ignoreCase = true)
                if (!isCallNotification) {
                    autoCollapseJob = serviceScope.launch {
                        delay(4000)
                        if (currentExpandedNotificationKey == notification.key && isShowingFullIsland) {
                            Logger.d(Logger.TAG, "AutoExpand: Auto-collapsing after timeout")
                            closeFullNotificationIsland()
                        }
                    }
                } else {
                    Logger.d(Logger.TAG, "AutoExpand: Call notification - skipping auto-collapse")
                }
            }
        }
    }


    private fun handleRemovedNotification(key: String) {
        notifications.removeAll { !it!!.isLocal && it.key == key }
    }

    private fun handleNotificationDisplay(data: NotificationData) {
        val notification = notifications.lastOrNull() ?: return

        when {
            data.category.equals(NotificationCompat.CATEGORY_CALL, ignoreCase = true) -> {
                when {
                    MyPreferenceManager.getAutoCloseNoti(this) && data.isOngoing -> {
                        Logger.d(Logger.TAG, "NotiDisplay: Auto-closing heads up for call")
                        closeHeadsUpNotification(notification)
                    }

                    data.isOngoing -> {
                        // Always show call notifications in full island view (big view)
                        Logger.d(Logger.TAG, "NotiDisplay: Auto-expanding call notification to big view")
                        val position = notifications.indexOf(notification)
                        if (position >= 0) {
                            showFullIslandNotification(position)
                        }
                    }

                    else -> {
                        currentIndex = notifications.size - 1
                        closeFullNotificationIsland()
                    }
                }
            }

            else -> showSmallIslandNotification()
        }
    }

    private fun updateNotificationItem(source: Notification, index: Int) {
        notifications[index]?.apply {
            senderIcon = source.senderIcon
            icon = source.icon
            actions = source.actions
            pendingIntent = source.pendingIntent
            tv_title = source.tv_title
            tv_text = source.tv_text
            pack = source.pack
            postTime = source.postTime
            count = source.count
            bigText = source.bigText
            app_name = source.app_name
            isClearable = source.isClearable
            color = source.color
            picture = source.picture
            id = source.id
            template = source.template
            key = source.key
            groupKey = source.groupKey
            isAppGroup = source.isAppGroup
            isGroup = source.isGroup
            isOngoing = source.isOngoing
            isGroupConversation = source.isGroupConversation
            showChronometer = source.showChronometer
            progress = source.progress
            progressMax = source.progressMax
            progressIndeterminate = source.progressIndeterminate
            category = source.category
        }
    }

    private fun isSameItem(notification: Notification): Boolean {
        return (notification.pack == "com.whatsapp" && notification.template.isEmpty()) ||
                (notification.pack == "com.google.android.gm" && notification.id == "0")
    }

    fun isCallPkgFound(pkg: String?): Boolean =
        callPKG.controlAppModelList.any { it?.pkg.equals(pkg, ignoreCase = true) }

    fun isFilterPkgFound(pkg: String?): Boolean =
        filterPKG.controlAppModelList.any { it?.pkg.equals(pkg, ignoreCase = true) }
    // endregion

    // region Gestures
    fun closeHeadsUpNotification(notification: Notification) {
        serviceScope.launch {
            delay(GESTURE_DELAY)
            performSwipeGesture(isUp = true) {
                val position = notifications.indexOf(notification)
                if (position >= 0) {
                    showFullIslandNotification(position)
                }
            }
        }
    }

    fun upDownGesture(isUp: Boolean) {
        Logger.d(Logger.TAG, "Gesture: UpDown, isUp=$isUp")
        statusBarView?.visibility = View.GONE
        performSwipeGesture(isUp) {
            serviceScope.launch {
                delay(1000)
                statusBarView?.visibility = View.VISIBLE
            }
        }
    }

    private fun performSwipeGesture(isUp: Boolean, onComplete: () -> Unit) {
        Logger.d(Logger.TAG, "Gesture: Performing swipe, isUp=$isUp")
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels.toDouble()
        val x = ((displayMetrics.widthPixels / 2) + (displayMetrics.widthPixels / 4)).toFloat()
        val startY = if (isUp) screenHeight * 0.1 else screenHeight * 0.25
        val endY = if (isUp) screenHeight * 0.01 else screenHeight * 0.75

        val path = Path().apply {
            moveTo(x, startY.toFloat())
            lineTo(x, endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(StrokeDescription(path, 100, 50))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Logger.d(Logger.TAG, "Gesture: Completed")
                onComplete()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Logger.w(Logger.TAG, "Gesture: Cancelled")
            }
        }, null)
    }

    fun expandSystemPanel() {
        Logger.d(Logger.TAG, "Action: expandSystemPanel")
        val wasOverriding = overrideStock
        overrideStock = false
        performClick = true

        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            statusBarView?.visibility = View.GONE
            performGlobalAction(GLOBAL_ACTION_HOME)
            upDownGesture(false)
        } else {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        if (wasOverriding) {
            serviceScope.launch {
                delay(2000)
                overrideStock = true
            }
        }
    }

    fun closeStatusBar() {
        Logger.d(Logger.TAG, "Action: closeStatusBar")
        if (Build.VERSION.SDK_INT >= 31) {
            performGlobalAction(15)
        } else {
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS).putExtra("noRespond", true))
        }
    }
    // endregion

    // region Media
    private fun startMediaUpdateJob() {
        mediaUpdateJob?.cancel()
        mediaUpdateJob = serviceScope.launch {
            while (isActive) {
                updateMediaProgress()
                delay(MEDIA_UPDATE_INTERVAL)
            }
        }
    }

    private fun updateMediaProgress() {
        currentNotification?.apply {
            duration = getMediaDuration()
            position = getMediaPosition()

            if (duration > 0) {
                progressMax = 100
                progress = ((position.toFloat() / duration.toFloat()) * 100).toInt()
                progressIndeterminate = false
                val index = notifications.indexOf(this)
                if (index != -1) {
                    // Update media progress in the unified adapter
                    unifiedAdapter?.updateMediaInfo(index, this)
                }
            }
        }
    }

    private fun getMediaDuration(): Long = runCatching {
        mediaSessionManager?.getActiveSessions(
            ComponentName(applicationContext, MyNotificationService::class.java)
        )?.firstOrNull()?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)
    }.getOrNull() ?: 1L

    private fun getMediaPosition(): Long = runCatching {
        mediaSessionManager?.getActiveSessions(
            ComponentName(applicationContext, MyNotificationService::class.java)
        )?.firstOrNull()?.playbackState?.position
    }.getOrNull() ?: 0L
    // endregion

    // region Glow Animation
    private fun showGlowAnimation() {
        if (!useGlow) {
            Logger.d(Logger.TAG, "Animation: Glow skipped (disabled)")
            return
        }
        Logger.d(Logger.TAG, "Animation: Starting Glow Animation")

        animationJob?.cancel()
        animationJob = serviceScope.launch {
            val animationDrawable: AnimationDrawable = utils.getAnimationDrawable()
            islandParentLayout?.background = animationDrawable
            animationDrawable.start()

            delay(ANIMATION_DURATION)
            islandParentLayout?.background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_rectangle_notification,
                null
            )
        }
    }
    // endregion

    // region Animation Helpers (iPhone-like)
    */
/**
     * Create spring animation for natural, bouncy motion like iPhone Dynamic Island
     *//*

    private fun createSpringAnimation(
        view: View,
        property: DynamicAnimation.ViewProperty,
        finalPosition: Float
    ): SpringAnimation {
        Logger.v(Logger.TAG, "Animation: Creating SpringAnimation for view ${view.id} property ${property.javaClass.simpleName} to $finalPosition")
        // Cancel any existing animation on this property for this view
        val key = Pair(view, property)
        activeAnimations[key]?.cancel()

        return SpringAnimation(view, property).apply {
            spring = SpringForce(finalPosition).apply {
                // Use higher stiffness for snappier iOS-like feel
                stiffness = SpringForce.STIFFNESS_HIGH
                // Medium bouncy for natural, smooth motion
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
            addEndListener { _, _, _, _ ->
                activeAnimations.remove(key)
            }
            activeAnimations[key] = this
        }
    }

    */
/**
     * Animate view in with smooth fade + scale (spring effect)
     *//*

    private fun animateViewIn(targetView: View, delay: Long = 0L) {
        Logger.d(Logger.TAG, "Animation: animateViewIn for ${targetView.id} with delay $delay")
        targetView.postDelayed({
            // Cancel any existing animations first
            targetView.animate().cancel()

            // Alpha fade in with spring timing
            targetView.animate()
                .alpha(1f)
                .setDuration(250L)
                .setInterpolator(DecelerateInterpolator())
                .start()

            // Spring scale animation for iOS-like feel
            createSpringAnimation(targetView, SpringAnimation.SCALE_X, 1f).start()
            createSpringAnimation(targetView, SpringAnimation.SCALE_Y, 1f).start()
        }, delay)
    }

    */
/**
     * Animate view out with smooth fade + scale
     *//*

    private fun animateViewOut(targetView: View, delay: Long = 0L, onComplete: () -> Unit = {}) {
        Logger.d(Logger.TAG, "Animation: animateViewOut for ${targetView.id} with delay $delay")
        targetView.postDelayed({
            // Cancel any existing animations first
            targetView.animate().cancel()

            // Alpha fade out
            targetView.animate()
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction(onComplete)
                .start()

            // Spring scale down for smooth exit
            createSpringAnimation(targetView, SpringAnimation.SCALE_X, 0.92f).start()
            createSpringAnimation(targetView, SpringAnimation.SCALE_Y, 0.92f).start()
        }, delay)
    }

    */
/**
     * Animate window size change smoothly
     *//*

    private fun animateWindowSize(targetHeight: Int, targetWidth: Int, duration: Long = 350L) {
        Logger.d(Logger.TAG, "Animation: animateWindowSize to W=$targetWidth H=$targetHeight duration=$duration")
        val currentWidth = localLayoutParams?.width ?: targetWidth
        val currentHeight = localLayoutParams?.height ?: targetHeight
        val screenWidth = resources.displayMetrics.widthPixels

        // Animate width
        if (targetWidth == WindowManager.LayoutParams.MATCH_PARENT) {
            if (currentWidth != WindowManager.LayoutParams.MATCH_PARENT) {
                // Expanding to full screen
                ValueAnimator.ofInt(currentWidth, screenWidth).apply {
                    this.duration = duration
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { animator ->
                        localLayoutParams?.width = animator.animatedValue as Int
                        updateWindowManager()
                    }
                    doOnEnd {
                        localLayoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
                        updateWindowManager()
                    }
                    start()
                }
            } else {
                 // Already match parent, ensure it stays set
                localLayoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
                updateWindowManager()
            }
        } else {
            // Animating to specific width
            val startWidth = if (currentWidth == WindowManager.LayoutParams.MATCH_PARENT) screenWidth else currentWidth

            if (startWidth != targetWidth) {
                ValueAnimator.ofInt(startWidth, targetWidth).apply {
                    this.duration = duration
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { animator ->
                        localLayoutParams?.width = animator.animatedValue as Int
                        updateWindowManager()
                    }
                    start()
                }
            } else {
                 localLayoutParams?.width = targetWidth
                 updateWindowManager()
            }
        }

        // Animate height if it's not MATCH_PARENT
        if (targetHeight != WindowManager.LayoutParams.MATCH_PARENT && currentHeight != targetHeight) {
            ValueAnimator.ofInt(currentHeight, targetHeight).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    localLayoutParams?.height = animator.animatedValue as Int
                    updateWindowManager()
                }
                start()
            }
        } else if (targetHeight == WindowManager.LayoutParams.MATCH_PARENT) {
            localLayoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
            updateWindowManager()
        }
    }
    // endregion

    // region Helpers
    private fun updateWindowManager() {
        statusBarView?.takeIf { it.isAttachedToWindow }?.let {
            runCatching {
                Logger.d(Logger.TAG, "WindowManager: Updating layout params: Width=${localLayoutParams?.width}, Height=${localLayoutParams?.height}, Y=${localLayoutParams?.y}")
                windowManager.updateViewLayout(it, localLayoutParams)
            }.onFailure {
                Logger.e(Logger.TAG, "WindowManager: Update failed", it)
            }
        }
    }

    private fun setFullIslandMargin(expanding: Boolean) {
        if (expanding) {
            tempMargin = margin
            margin = (25 * scaledDensity).toInt()
        } else {
            margin = tempMargin
        }

        val params = islandTopLayout?.layoutParams as? LinearLayout.LayoutParams ?: return
        when (cameraPosition) {
            CameraPosition.Left -> params.setMargins(left = margin)
            CameraPosition.Center -> params.setMargins()
            CameraPosition.Right -> params.setMargins(right = margin)
        }
        islandTopLayout?.layoutParams = params
    }

    private fun setKeyboardFlag(enabled: Boolean) {
        serviceScope.launch {
            delay(300)
            localLayoutParams?.flags = if (enabled) FLAG_KEYBOARD else FLAG_NORMAL
            updateWindowManager()
        }
    }

    private fun scrollToPosition(index: Int) {
        unifiedAdapter?.refreshData("Scroll to position: $index")
        if (!isShowingSmall) showSmallIslandNotification()
//        rvIslandSmall?.takeIf { unifiedAdapter?.itemCount ?: 0 > 0 }?.scrollToPosition(index)
    }

    private fun scrollToLastItem() {
        unifiedAdapter?.refreshData("Scroll to last item")
        unifiedAdapter?.takeIf { it.itemCount > 0 }?.let {
//            rvIslandSmall?.scrollToPosition(it.itemCount - 1)
        }
        showSmallIslandNotification()
    }

    private fun updateFontScale(config: Configuration) {
        if (config.fontScale > 1.3f) {
            Logger.d(Logger.TAG, "Font: Limiting font scale to 1.3f (was ${config.fontScale})")
            config.fontScale = 1.3f
            val displayMetrics = resources.displayMetrics
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.scaledDensity = config.fontScale * displayMetrics.density
            resources.updateConfiguration(config, displayMetrics)
        }
    }

    private fun cleanUp() {
        Logger.d(Logger.TAG, "cleanUp: Unregistering receivers and listeners")
        serviceScope.cancel()
        pendingJob?.cancel()
        mediaUpdateJob?.cancel()
        animationJob?.cancel()

        runCatching { contentResolver.unregisterContentObserver(contentObserver) }
        runCatching { unregisterReceiver(systemInfoReceiver) }
        runCatching {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
        }
        runCatching {
            preferenceManager.sharePreference?.unregisterOnSharedPreferenceChangeListener(
                preferenceListener
            )
        }
        runCatching { windowManager.removeView(statusBarView) }
    }

    private fun updateAdapter(scrollKey: String? = null) {
        val adapter = unifiedAdapter ?: return

        mainHandler.post {
            if (notifications.isEmpty()) {
                val emptyNoti = Notification().apply {
                    type = "TYPE_EMPTY"
                    key = "empty_placeholder"
                }
                adapter.submitNotifications(listOf(emptyNoti))
                showSmallIslandNotification()
            } else {
                adapter.submitNotifications(notifications.filterNotNull(), scrollKey)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideControlButtons() {
        statusBarView?.apply {
            findViewById<View>(R.id.settings_iv_left).visibility = View.GONE
            findViewById<View>(R.id.settings_iv_right).visibility = View.GONE
        }
    }

    private fun hideSettingsButtons() {
        statusBarView?.apply {
            findViewById<View>(R.id.settings_iv_left)?.animate()
                ?.alpha(0f)
                ?.setDuration(150L)
                ?.withEndAction {
                    findViewById<View>(R.id.settings_iv_left)?.visibility = View.GONE
                }
                ?.start()
            findViewById<View>(R.id.settings_iv_right)?.animate()
                ?.alpha(0f)
                ?.setDuration(150L)
                ?.withEndAction {
                    findViewById<View>(R.id.settings_iv_right)?.visibility = View.GONE
                }
                ?.start()
        }
    }

    private fun setupControlButtonListeners() {
        statusBarView?.apply {
            findViewById<View>(R.id.settings_iv_left).setOnClickListener { toggleControls() }
            findViewById<View>(R.id.settings_iv_right).setOnClickListener { toggleControls() }
        }
    }

    private fun getIconColor(color: Int): Int =
        if (ColorUtils.calculateLuminance(color) < 0.15) Color.rgb(240, 240, 240) else color

    fun getDpToPx(dp: Int): Int = (dp * scaledDensity).toInt()

    val isPortrait: Boolean
        get() = windowManager.defaultDisplay.rotation.let { it == 0 || it == 2 }

    val isShowingSmall: Boolean
        get() = localLayoutParams?.width == minCameIslandWidth && islandTopLayout?.isVisible == true

    val isShowingFullIsland: Boolean
        get() = localLayoutParams?.height == WindowManager.LayoutParams.MATCH_PARENT && islandTopLayout?.isVisible == true

    val minWidth: Int
        get() = when (cameraPosition) {
            CameraPosition.Left, CameraPosition.Right -> minCameIslandWidth + startMargin
            CameraPosition.Center -> minCameIslandWidth
        }

    val topLayoutParams: RelativeLayout.LayoutParams
        get() = islandParentLayout?.layoutParams as? RelativeLayout.LayoutParams
            ?: RelativeLayout.LayoutParams(0, 0)
    // endregion

    companion object {
        const val MAX_ISLAND_HEIGHT = 170
        const val MAX_ISLAND_HEIGHT_2 = 110
        const val MAX_ISLAND_HEIGHT_CALL = 90
        const val MAX_ISLAND_WIDTH = 350
        const val FLAG_NORMAL = 8913704
        const val FLAG_KEYBOARD = 8913696
        const val MIN_ZERO_CAM_WIDTH = 150
        const val MIN_ONE_CAM_WIDTH = 180
        const val MIN_TWO_CAM_WIDTH = 210
        const val MIN_THREE_CAM_WIDTH = 240
        const val ANIMATION_DURATION = 3000L
        const val MEDIA_UPDATE_INTERVAL = 1000L
        const val GESTURE_DELAY = 700L

        private const val VIVO_PACKAGE = "com.vivo.upslide"
        private const val XIAOMI_PACKAGE = "com.miui.home"

        val ENABLED_ACCESSIBILITY_SERVICES: Uri =
            Settings.Secure.getUriFor("enabled_accessibility_services")

        @JvmStatic
        var buttonName: String? = null

        @JvmStatic
        var launchIntent: Intent? = null
    }
    // endregion
}

// region Extension Functions
private fun MutableList<Notification?>.findByType(type: String): Notification? =
    find { it?.type == type }

private fun MutableList<Notification?>.removeByType(type: String): Boolean =
    removeAll { it?.type == type }

private fun RecyclerView.attachPagerSnapHelper() {
    runCatching {
        PagerSnapHelper().attachToRecyclerView(this)
    }
}

private fun LinearLayout.LayoutParams.setMargins(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0
) {
    leftMargin = left
    topMargin = top
    rightMargin = right
    bottomMargin = bottom
}

private val Notification.isIphoneCallStyle: Boolean
    get() = category.equals(NotificationCompat.CATEGORY_CALL, ignoreCase = true) &&
            isOngoing && useIphoneCallDesign
// endregion

// region Data Classes
data class NotificationData(
    val packageName: String,
    val id: String,
    val key: String,
    val groupKey: String,
    val title: CharSequence,
    val text: CharSequence,
    val bigText: CharSequence,
    val appName: String?,
    val category: String?,
    val template: String,
    val tag: String,
    val color: Int,
    val postTime: Long,
    val isAdded: Boolean,
    val isClearable: Boolean,
    val isOngoing: Boolean,
    val isGroup: Boolean,
    val isAppGroup: Boolean,
    val isGroupConversation: Boolean,
    val progress: Int,
    val progressMax: Int,
    val progressIndeterminate: Boolean,
    val showChronometer: Boolean,
    val icon: Bitmap?,
    val largeIcon: Bitmap?,
    val picture: Bitmap?,
    val pendingIntent: PendingIntent?,
    val actions: ArrayList<ActionParsable?>?,
    val uId: Int,
    val substName: CharSequence?,
    val subText: CharSequence?,
    val titleBig: CharSequence?,
    val summaryText: CharSequence?,
    val infoText: CharSequence?
) {
    fun toNotification(): Notification = Notification(
        id, icon, largeIcon, title, text, 0, packageName, postTime,
        pendingIntent, actions, bigText, appName, isClearable, color,
        picture, groupKey, key, isGroupConversation, isAppGroup,
        isGroup, isOngoing, tag, uId, template, substName,
        subText, titleBig, infoText, progressMax, progress,
        progressIndeterminate, summaryText, showChronometer, category
    )
}
// endregion*/
