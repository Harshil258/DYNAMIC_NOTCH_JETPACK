package ai.emots.kishan_dynamic.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * System Accessibility Overlay Service that renders the Dynamic Island on top of all Android windows.
 */
class IslandOverlayService : AccessibilityService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var preferences: AuroraPreferences
    private var composeView: ComposeView? = null

    // Lifecycle requirements for Compose in a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferences = AuroraPreferences(applicationContext)

        initOverlayView()
        observePreferences()
    }

    private fun initOverlayView() {
        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@IslandOverlayService)
            setViewTreeViewModelStoreOwner(this@IslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@IslandOverlayService)

            setContent {
                val islandState by IslandStateManager.currentState.collectAsState()
                val isEnabled by preferences.islandEnabled.collectAsState(initial = true)
                val verticalOffset by preferences.verticalOffset.collectAsState(initial = 12)
                val horizontalOffset by preferences.horizontalOffset.collectAsState(initial = 0)
                val widthScale by preferences.widthScale.collectAsState(initial = 1.0f)

                // The island drops out of the cutout rather than popping in.
                androidx.compose.animation.AnimatedVisibility(
                    visible = isEnabled && islandState !is IslandState.Hidden,
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = verticalOffset.dp)
                                .offset(x = horizontalOffset.dp)
                                .scale(widthScale),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val demoState = mapToDemoState(islandState)
                            DynamicIslandPill(
                                state = demoState,
                                onTap = {
                                    IslandStateManager.toggleExpansion()
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

    private fun mapToDemoState(state: IslandState): IslandDemoState {
        return when (state) {
            is IslandState.Hidden, is IslandState.Minimal -> IslandDemoState.Minimal
            is IslandState.Music -> if (state.isExpanded) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact
            is IslandState.OngoingCall -> if (state.isExpanded) IslandDemoState.CallExpanded else IslandDemoState.CallCompact
            is IslandState.IncomingCall -> IslandDemoState.CallExpanded
            is IslandState.Notification -> if (state.isExpanded) IslandDemoState.NotificationExpanded else IslandDemoState.NotificationCompact
            is IslandState.NotificationWithMusic -> IslandDemoState.MusicCompact
            is IslandState.Charging -> IslandDemoState.ChargingCompact
            is IslandState.RingerMode -> IslandDemoState.Minimal
            is IslandState.RingerVolume, is IslandState.MediaVolume -> IslandDemoState.Minimal
            is IslandState.BluetoothDevice -> IslandDemoState.NotificationCompact
            is IslandState.ActionControl -> IslandDemoState.MusicExpanded
            is IslandState.PremiumExpiry -> IslandDemoState.NotificationCompact
        }
    }

    private fun observePreferences() {
        scope.launch {
            preferences.islandEnabled.collectLatest { enabled ->
                composeView?.visibility = if (enabled) View.VISIBLE else View.GONE
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
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
