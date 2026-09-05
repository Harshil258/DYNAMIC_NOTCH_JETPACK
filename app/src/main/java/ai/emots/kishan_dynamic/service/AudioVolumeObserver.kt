package ai.emots.kishan_dynamic.service

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ContentObserver observing system media volume and ringer changes to present floating notch HUD.
 */
class AudioVolumeObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(handler) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousVolume = -1
    private var previousRingerVolume = -1
    private val preferences = AuroraPreferences(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun register() {
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            this
        )
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        previousRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
        scope.cancel()
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        val maxRingerVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)

        if (currentRingerVolume != previousRingerVolume && maxRingerVolume > 0) {
            previousRingerVolume = currentRingerVolume
            scope.launch {
                val isProActive = preferences.isProActive.first()
                if (PremiumFeaturePolicy.soundSettingEnabled(
                        isProActive,
                        preferences.showVolumeIndicator.first()
                    )
                ) {
                    IslandStateManager.postVolumeLevel(
                        level = currentRingerVolume.toFloat() / maxRingerVolume.toFloat(),
                        isRinger = true
                    )
                }
            }
        } else if (currentVolume != previousVolume && maxVolume > 0) {
            previousVolume = currentVolume
            val levelRatio = currentVolume.toFloat() / maxVolume.toFloat()
            scope.launch {
                val isProActive = preferences.isProActive.first()
                if (PremiumFeaturePolicy.soundSettingEnabled(
                        isProActive,
                        preferences.showVolumeIndicator.first()
                    )
                ) {
                    IslandStateManager.postVolumeLevel(level = levelRatio, isRinger = false)
                }
            }
        }
    }
}
