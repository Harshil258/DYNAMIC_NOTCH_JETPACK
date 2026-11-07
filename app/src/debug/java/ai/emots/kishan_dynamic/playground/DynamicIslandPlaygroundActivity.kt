package ai.emots.kishan_dynamic.playground

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/** Compiled only into debug. No dependency from the production navigation graph. */
class DynamicIslandPlaygroundActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            AppTheme(darkTheme = true) {
                DynamicIslandPlayground(onBack = { finish() }, onPhysicalOverlay = {
                    if (!Settings.canDrawOverlays(this)) {
                        PlaygroundSession.controller.message("Grant Android overlay access, then tap Start physical overlay again")
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    } else {
                        ContextCompat.startForegroundService(this, Intent(this, PlaygroundOverlayService::class.java))
                    }
                }, onStopOverlay = { stopService(Intent(this, PlaygroundOverlayService::class.java)) })
            }
        }
    }
    override fun onResume() { super.onResume(); PlaygroundSession.activity(true) }
    override fun onPause() { PlaygroundSession.activity(false); super.onPause() }
    override fun onDestroy() {
        if (!isChangingConfigurations && !PlaygroundSession.overlayRunning.value) PlaygroundSession.controller.cancelSequence()
        super.onDestroy()
    }
}
