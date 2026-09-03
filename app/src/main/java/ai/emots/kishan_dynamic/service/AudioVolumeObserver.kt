package ai.emots.kishan_dynamic.service

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings

/**
 * ContentObserver observing system media volume and ringer changes to present floating notch HUD.
 */
class AudioVolumeObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(handler) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousVolume = -1

    fun register() {
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            this
        )
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        if (currentVolume != previousVolume && maxVolume > 0) {
            previousVolume = currentVolume
            val levelRatio = currentVolume.toFloat() / maxVolume.toFloat()
            IslandStateManager.postVolumeLevel(level = levelRatio, isRinger = false)
        }
    }
}
