package ai.emots.kishan_dynamic.playground

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.savedstate.*
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/** Mock overlay only. Never delegates to production notification/media/call/ad handlers. */
class PlaygroundOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var manager: WindowManager
    private var view: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { updatePlacement() }
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_PLAYGROUND") stopSelf()
        return START_NOT_STICKY
    }
    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        savedState.performAttach(); savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) notificationManager.createNotificationChannel(
            NotificationChannel("island_playground", "Island Lab preview", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(this, 0, Intent(this, javaClass).setAction("STOP_PLAYGROUND"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val open = PendingIntent.getActivity(this, 1, Intent(this, DynamicIslandPlaygroundActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        startForeground(8042, NotificationCompat.Builder(this, "island_playground")
            .setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Island Lab · mock overlay")
            .setContentText("Stops automatically after 3 minutes. No real calls, notifications or ads.")
            .setContentIntent(open).setOngoing(true).addAction(0, "Stop preview", stop).build())
        PlaygroundSession.overlay(true)
        val display = getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        val displayContext = createDisplayContext(display)
        val windowContext = if (Build.VERSION.SDK_INT >= 30) displayContext.createWindowContext(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null) else displayContext
        manager = windowContext.getSystemService(WindowManager::class.java)
        params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                if (Build.VERSION.SDK_INT >= 28) layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        view = ComposeView(windowContext).apply {
            setViewTreeLifecycleOwner(this@PlaygroundOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PlaygroundOverlayService)
            setViewTreeViewModelStoreOwner(this@PlaygroundOverlayService)
            setOnApplyWindowInsetsListener { _, insets -> post { updatePlacement() }; insets }
            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) { PlaygroundSession.controller.expand(false); true } else false
            }
            setContent {
                val snapshot by PlaygroundSession.controller.snapshot.collectAsState()
                AppTheme(darkTheme = true) {
                    Box {
                        PlaygroundIsland(snapshot, PlaygroundSession.controller, modifier = Modifier.onSizeChanged { post { updatePlacement() } })
                        if (snapshot.options.guides) Canvas(Modifier.matchParentSize()) {
                            drawRect(Color.Cyan, style = Stroke(1f))
                            drawLine(Color.Cyan, androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                                androidx.compose.ui.geometry.Offset(size.width / 2, size.height), 1f)
                        }
                    }
                }
            }
        }
        runCatching { manager.addView(view, params) }.onFailure {
            PlaygroundSession.controller.message("Physical overlay unavailable: ${it.javaClass.simpleName}")
            stopSelf()
        }
        ContextCompat.registerReceiver(this, screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        scope.launch { PlaygroundSession.controller.snapshot.collectLatest { updatePlacement() } }
        scope.launch { delay(180_000); stopSelf() }
    }
    private fun updatePlacement() {
        val body = view ?: return
        val layout = params ?: return
        if (!body.isAttachedToWindow) return
        val snapshot = PlaygroundSession.controller.snapshot.value
        val locked = getSystemService(KeyguardManager::class.java).isKeyguardLocked
        val hidden = locked || !getSystemService(android.os.PowerManager::class.java).isInteractive || snapshot.state == IslandState.Hidden
        val metrics = body.resources.displayMetrics
        val window = if (Build.VERSION.SDK_INT >= 30) manager.maximumWindowMetrics else null
        val cutouts = if (Build.VERSION.SDK_INT >= 30) window?.windowInsets?.displayCutout?.boundingRects.orEmpty()
            else if (Build.VERSION.SDK_INT >= 28) body.rootWindowInsets?.displayCutout?.boundingRects.orEmpty() else emptyList()
        val bar = if (Build.VERSION.SDK_INT >= 30) window!!.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars()).top
            else @Suppress("DEPRECATION") (body.rootWindowInsets?.systemWindowInsetTop ?: 0)
        val placement = IslandPlacementPolicy.resolve(window?.bounds?.width() ?: metrics.widthPixels,
            window?.bounds?.height() ?: metrics.heightPixels, body.width, body.height,
            0, (11 * metrics.density).toInt(), snapshot.state.presentationMode == IslandPresentationMode.EXPANDED,
            bar, (11 * metrics.density).toInt(), (snapshot.options.cameraClearance * metrics.density).toInt(),
            cutouts.map { IslandCutout(it.left, it.top, it.right, it.bottom) })
        val width = if (hidden) 0 else WindowManager.LayoutParams.WRAP_CONTENT
        val height = if (hidden) 0 else WindowManager.LayoutParams.WRAP_CONTENT
        val flags = if (hidden) layout.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            else layout.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        body.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
        if (layout.x != placement.centerOffsetX || layout.y != placement.top || layout.width != width || layout.height != height || layout.flags != flags) {
            layout.x = placement.centerOffsetX; layout.y = placement.top; layout.width = width; layout.height = height; layout.flags = flags
            runCatching { manager.updateViewLayout(body, layout) }
        }
        PlaygroundSession.physicalInfo.value = "Actual device: ${window?.bounds?.width() ?: metrics.widthPixels}×${window?.bounds?.height() ?: metrics.heightPixels}px · density ${metrics.density} · ${cutouts.size} cutout(s) · window ${body.width}×${body.height}px · ${if (locked) "hidden on keyguard" else "unlocked"}"
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig); view?.requestApplyInsets(); view?.post { updatePlacement() }
    }
    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        view?.let { if (it.isAttachedToWindow) runCatching { manager.removeView(it) }; it.disposeComposition() }
        view = null; scope.cancel(); if (registry.currentState.isAtLeast(Lifecycle.State.CREATED)) registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY); viewModelStore.clear()
        PlaygroundSession.overlay(false)
        super.onDestroy()
    }
}
