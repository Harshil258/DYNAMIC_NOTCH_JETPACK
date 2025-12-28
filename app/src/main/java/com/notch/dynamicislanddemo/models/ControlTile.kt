package com.notch.dynamicislanddemo.models

/**
 * Control tile types for Action Control Center
 */
sealed class ControlTile {
    /**
     * System toggle controls (WiFi, Bluetooth, etc.)
     */
    data class SystemToggle(
        val type: SystemToggleType,
        val isEnabled: Boolean,
        val label: String
    ) : ControlTile()
    
    /**
     * App shortcut
     */
    data class AppShortcut(
        val appName: String,
        val packageName: String,
        val iconColor: androidx.compose.ui.graphics.Color
    ) : ControlTile()
    
    /**
     * Contact quick action
     */
    data class ContactShortcut(
        val contactName: String,
        val contactId: String,
        val photoUrl: String?
    ) : ControlTile()
    
    /**
     * Custom action tile
     */
    data class CustomAction(
        val label: String,
        val icon: String,
        val action: String
    ) : ControlTile()
}

enum class SystemToggleType {
    MOBILE_DATA,
    WIFI,
    BLUETOOTH,
    DARK_MODE,
    TORCH,
    AIRPLANE_MODE,
    DO_NOT_DISTURB,
    ROTATION_LOCK,
    HOTSPOT
}

/**
 * Action Control configuration
 */
data class ActionControlConfig(
    val tiles: List<ControlTile>,
    val brightnessLevel: Float = 0.5f,
    val volumeLevel: Float = 0.7f,
    val isEditing: Boolean = false
)
