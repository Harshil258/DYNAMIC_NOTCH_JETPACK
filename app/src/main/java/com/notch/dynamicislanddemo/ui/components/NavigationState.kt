package com.notch.dynamicislanddemo.ui.components

/**
 * Sealed class representing navigation destinations in the app
 */
sealed class NavigationDestination(val route: String) {
    object Home : NavigationDestination("home")
    object Preview : NavigationDestination("preview")
    object Settings : NavigationDestination("settings")
    object About : NavigationDestination("about")
    object DisplaySettings : NavigationDestination("display_settings")
    
    val isTab: Boolean
        get() = this !is DisplaySettings
    
    companion object {
        val values get() = listOf(Home, Preview, Settings, About, DisplaySettings)
        val tabValues get() = listOf(Home, Preview, Settings, About)
        
        fun fromIndex(index: Int): NavigationDestination {
            return when (index) {
                0 -> Home
                1 -> Preview
                2 -> Settings
                3 -> About
                4 -> DisplaySettings
                else -> Home
            }
        }
        
        fun toIndex(destination: NavigationDestination): Int {
            return when (destination) {
                Home -> 0
                Preview -> 1
                Settings -> 2
                About -> 3
                DisplaySettings -> 4
            }
        }
    }
}
