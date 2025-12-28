package com.notch.dynamicislanddemo.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Haptic feedback utility for iOS-like haptic responses
 */
object HapticFeedback {
    
    /**
     * Light impact haptic - for button taps
     */
    fun light(context: Context) {
        performHaptic(context, 10, 50)
    }
    
    /**
     * Medium impact haptic - for state changes
     */
    fun medium(context: Context) {
        performHaptic(context, 20, 100)
    }
    
    /**
     * Heavy impact haptic - for important actions
     */
    fun heavy(context: Context) {
        performHaptic(context, 30, 150)
    }
    
    /**
     * Selection haptic - for scrolling/swiping
     */
    fun selection(context: Context) {
        performHaptic(context, 5, 20)
    }
    
    /**
     * Success haptic - for successful actions
     */
    fun success(context: Context) {
        val pattern = longArrayOf(0, 50, 50, 100)
        val amplitudes = intArrayOf(0, 100, 0, 150)
        performPattern(context, pattern, amplitudes)
    }
    
    /**
     * Error haptic - for errors
     */
    fun error(context: Context) {
        val pattern = longArrayOf(0, 30, 30, 30, 30, 30)
        val amplitudes = intArrayOf(0, 150, 0, 150, 0, 150)
        performPattern(context, pattern, amplitudes)
    }
    
    private fun performHaptic(context: Context, amplitude: Int, duration: Long) {
        val vibrator = getVibrator(context)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(duration, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    private fun performPattern(context: Context, pattern: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }
    
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

/**
 * Extension function for View to trigger haptic feedback easily
 */
fun View.performHapticFeedback() {
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}
