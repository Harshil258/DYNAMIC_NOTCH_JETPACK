package ai.emots.kishan_dynamic.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat

/**
 * Narrow command boundary for phone controls exposed by the island.
 * The state manager remains UI/state-only; this class is the only place that
 * attempts to touch the active Telecom call or call audio route.
 */
class CallSystemController(private val context: Context) {

    private val telecomManager = context.getSystemService(TelecomManager::class.java)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun canControlCalls(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ANSWER_PHONE_CALLS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun acceptIncomingCall(): Boolean {
        if (!canControlCalls() || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return runCatching {
            telecomManager?.acceptRingingCall()
            telecomManager != null
        }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    fun endActiveCall(): Boolean {
        if (!canControlCalls() || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return runCatching { telecomManager?.endCall() == true }.getOrDefault(false)
    }

    fun toggleMute(): Boolean {
        val next = !audioManager.isMicrophoneMute
        return runCatching {
            audioManager.isMicrophoneMute = next
            audioManager.isMicrophoneMute == next
        }.getOrDefault(false)
    }

    fun toggleSpeaker(): Boolean {
        val next = !audioManager.isSpeakerphoneOn
        return runCatching {
            audioManager.isSpeakerphoneOn = next
            audioManager.isSpeakerphoneOn == next
        }.getOrDefault(false)
    }
}
